/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								bug 403086 - [compiler][null] include the effect of 'assert' in syntactic null analysis for fields
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class UnaryExpression extends OperatorExpression {

	public Expression expression;
	public Constant optimizedBooleanConstant;
	private int trueInitStateIndex = -1;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding syntheticAccessor = null;
	public TypeBinding expectedType = null;//Operator overload, for generic function call

	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	public UnaryExpression(Expression expression, int operator) {
		this.expression = expression;
		this.bits |= operator << OperatorSHIFT; // encode operator
	}

	public String getMethodName() {
		switch ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
			case NOT:
				return "logicalNot"; //$NON-NLS-1$
			case MINUS :
				return "neg"; //$NON-NLS-1$
			case TWIDDLE :
				return "complement"; //$NON-NLS-1$
			case PLUS :
				return "plus"; //$NON-NLS-1$

		}
		return ""; //$NON-NLS-1$
	}

	public MethodBinding getMethodBindingForOverload(BlockScope scope) {
		TypeBinding tb = null;

		if(this.expression.resolvedType == null)
			tb = this.expression.resolveType(scope);
		else
			tb = this.expression.resolvedType;

		final TypeBinding targetType = tb;

		OperatorOverloadInvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite(){
			@Override
			public TypeBinding invocationTargetType() {
				return targetType;
			}
			@Override
			public Expression[] arguments() {
				return null;
			}
		};

		String ms = getMethodName();

		MethodBinding mb2 = scope.getMethod(tb, ms.toCharArray(), new TypeBinding[]{}, fakeInvocationSite);
		return mb2;
	}

	public void generateOperatorOverloadCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.expression.generateCode(currentScope, codeStream, true);
		if ((!this.expression.resolvedType.isBaseType())) {
			codeStream.checkcast(this.expression.resolvedType);
		}
		generateOperatorOverloadCodeSimple(currentScope, codeStream, valueRequired);
	}

	public void generateOperatorOverloadCodeSimple(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (this.appropriateMethodForOverload.hasSubstitutedParameters() || this.appropriateMethodForOverload.hasSubstitutedReturnType()) {
			TypeBinding tbo = this.appropriateMethodForOverload.returnType;
			MethodBinding mb3 = this.appropriateMethodForOverload.original();
			MethodBinding final_mb = mb3;
			codeStream.checkcast(final_mb.declaringClass);
			codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());
			if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
				codeStream.checkcast(tbo);
			}
		} else {
			MethodBinding original = this.appropriateMethodForOverload.original();
			if(original.isPrivate()){
				codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticAccessor, null /* default declaringClass */);
			}
			else{
				codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
			}
			if (!this.appropriateMethodForOverload.returnType.isBaseType()) codeStream.checkcast(this.appropriateMethodForOverload.returnType);
		}

		String rvn = new String(this.resolvedType.constantPoolName());

		if (!valueRequired) {
			if (this.resolvedType.id != TypeBinding.VOID.id) {
				if ((this.resolvedType.isEquivalentTo(TypeBinding.DOUBLE))
						|| (this.resolvedType.isEquivalentTo(TypeBinding.LONG))
						|| (rvn.equals("java/lang/Double")) //$NON-NLS-1$
						|| (rvn.equals("java/lang/Long"))) { //$NON-NLS-1$
					codeStream.pop2();
				}
				else {
					codeStream.pop();
				}
			}
		}
	}

	@Override
	public FlowInfo analyseCode(
			BlockScope currentScope,
			FlowContext flowContext,
			FlowInfo flowInfo) {

		if(this.appropriateMethodForOverload != null){
			MethodBinding original = this.appropriateMethodForOverload.original();
			if(original.isPrivate()){
				this.syntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}

		if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
			flowContext.tagBits ^= FlowContext.INSIDE_NEGATION;
			flowInfo = this.expression.
				analyseCode(currentScope, flowContext, flowInfo).
				asNegatedCondition();
			flowContext.tagBits ^= FlowContext.INSIDE_NEGATION;
			this.trueInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo.initsWhenTrue());
		} else {
			flowInfo = this.expression.
				analyseCode(currentScope, flowContext, flowInfo);
		}
		this.expression.checkNPE(currentScope, flowContext, flowInfo);
		return flowInfo;
	}

	@Override
	protected void updateFlowOnBooleanResult(FlowInfo flowInfo, boolean result) {
		if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT)
			this.expression.updateFlowOnBooleanResult(flowInfo, !result);
	}

	@Override
	public Constant optimizedBooleanConstant() {

		return this.optimizedBooleanConstant == null
				? this.constant
				: this.optimizedBooleanConstant;
	}

	/**
	 * Code generation for an unary operation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	@Override
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		BranchLabel falseLabel, endifLabel;
		if (this.constant != Constant.NotAConstant) {
			// inlined value
			if (valueRequired) {
				codeStream.generateConstant(this.constant, this.implicitConversion);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		switch ((this.bits & OperatorMASK) >> OperatorSHIFT) {
			case NOT :
				switch ((this.expression.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) /* runtime type */ {
					case T_boolean :
						// ! <boolean>
						// Generate code for the condition
						this.expression.generateOptimizedBoolean(
							currentScope,
							codeStream,
							null,
							(falseLabel = new BranchLabel(codeStream)),
							valueRequired);
						if (valueRequired) {
							if (this.trueInitStateIndex != -1) {
								codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.trueInitStateIndex);
							}
							codeStream.iconst_0();
							if (falseLabel.forwardReferenceCount() > 0) {
								codeStream.goto_(endifLabel = new BranchLabel(codeStream));
								falseLabel.place();
								codeStream.iconst_1();
								endifLabel.place();
							}
						} else { // 6596: if (!(a && b)){} - must still place falseLabel
							falseLabel.place();
						}
						break;
					default:
						generateOperatorOverloadCode(currentScope,codeStream,valueRequired);
				}
				break;
			case TWIDDLE :
				switch ((this.expression.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4 /* runtime */) {
					case T_int :
						// ~int
						this.expression.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired) {
							codeStream.iconst_m1();
							codeStream.ixor();
						}
						break;
					case T_long :
						this.expression.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired) {
							codeStream.ldc2_w(-1L);
							codeStream.lxor();
						}
						break;
					default:
						generateOperatorOverloadCode(currentScope,codeStream,valueRequired);
				}
				break;
			case MINUS :
				// - <num>
				if (this.constant != Constant.NotAConstant) {
					if (valueRequired) {
						switch ((this.expression.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4){ /* runtime */
							case T_int :
								codeStream.generateInlinedValue(this.constant.intValue() * -1);
								break;
							case T_float :
								codeStream.generateInlinedValue(this.constant.floatValue() * -1.0f);
								break;
							case T_long :
								codeStream.generateInlinedValue(this.constant.longValue() * -1L);
								break;
							case T_double :
								codeStream.generateInlinedValue(this.constant.doubleValue() * -1.0);
						}
					}
				} else {
					this.expression.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired) {
						switch ((this.expression.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4){ /* runtime type */
							case T_int :
								codeStream.ineg();
								break;
							case T_float :
								codeStream.fneg();
								break;
							case T_long :
								codeStream.lneg();
								break;
							case T_double :
								codeStream.dneg();
								break;
							default:
								generateOperatorOverloadCodeSimple(currentScope,codeStream,valueRequired);
						}
					}
				}
				break;
			case PLUS :
				this.expression.generateCode(currentScope, codeStream, valueRequired);
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Boolean operator code generation
	 *	Optimized operations are: {@code &&, ||, <, <=, >, >=, &, |, ^ }
	 */
	@Override
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		BranchLabel trueLabel,
		BranchLabel falseLabel,
		boolean valueRequired) {

		if ((this.constant != Constant.NotAConstant) && (this.constant.typeID() == T_boolean)) {
			super.generateOptimizedBoolean(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		}
		if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
			if (((this.expression.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) != T_boolean) {

				this.expression.generateCode(currentScope, codeStream, valueRequired);
				generateOperatorOverloadCode(currentScope,codeStream,valueRequired);

				codeStream.ineg();

				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifne(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifeq(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}

				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);

			} else {
				this.expression.generateOptimizedBoolean(
						currentScope,
						codeStream,
						falseLabel,
						trueLabel,
						valueRequired);
			}
		} else {
			super.generateOptimizedBoolean(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
		}
	}

	@Override
	public StringBuilder printExpressionNoParenthesis(int indent, StringBuilder output) {

		output.append(operatorToString()).append(' ');
		return this.expression.printExpression(0, output);
	}
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		boolean expressionIsCast;
		if ((expressionIsCast = this.expression instanceof CastExpression) == true) this.expression.bits |= DisableUnnecessaryCastCheck; // will check later on
		TypeBinding expressionType = this.expression.resolveType(scope);
		if (expressionType == null) {
			this.constant = Constant.NotAConstant;
			return null;
		}
		int expressionTypeID = expressionType.id;
		// autoboxing support
		boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		if (use15specifics) {
			if (!expressionType.isBaseType()) {
				expressionTypeID = scope.environment().computeBoxingType(expressionType).id;
			}
		}
		//if (expressionTypeID > 15) {
		//	this.constant = Constant.NotAConstant;
		//	scope.problemReporter().invalidOperator(this, expressionType);
		//	return null;
		//}

		int tableId;
		switch ((this.bits & OperatorMASK) >> OperatorSHIFT) {
			case NOT :
				tableId = AND_AND;
				break;
			case TWIDDLE :
				tableId = LEFT_SHIFT;
				break;
			default :
				tableId = MINUS;
		} //+ and - cases

		// the code is an int
		// (cast)  left   Op (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8    <<4       <<0
		int operatorSignature = 0;
		if (expressionTypeID <= 15)	{
			operatorSignature = OperatorSignatures[tableId][(expressionTypeID << 4) + expressionTypeID];
		}
		this.expression.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), expressionType);
		this.bits |= operatorSignature & 0xF;
		switch (operatorSignature & 0xF) { // only switch on possible result type.....
			case T_boolean :
				this.resolvedType = TypeBinding.BOOLEAN;
				break;
			case T_byte :
				this.resolvedType = TypeBinding.BYTE;
				break;
			case T_char :
				this.resolvedType = TypeBinding.CHAR;
				break;
			case T_double :
				this.resolvedType = TypeBinding.DOUBLE;
				break;
			case T_float :
				this.resolvedType = TypeBinding.FLOAT;
				break;
			case T_int :
				this.resolvedType = TypeBinding.INT;
				break;
			case T_long :
				this.resolvedType = TypeBinding.LONG;
				break;
			default : //error........
				this.appropriateMethodForOverload = getMethodBindingForOverload(scope);
				if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
					scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
				if (!this.appropriateMethodForOverload.isValidBinding()) {
					this.constant = Constant.NotAConstant;
					if (expressionTypeID != T_undefined)
						scope.problemReporter().invalidOperator(this, expressionType);
					return null;
				}
				if((this.appropriateMethodForOverload.modifiers & ClassFileConstants.AccStatic) != 0) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
					return null;
				}
				this.expression.computeConversion(scope, this.expression.resolvedType, this.expression.resolvedType);
				this.resolvedType =  this.appropriateMethodForOverload.returnType;
				break;
		}
		// compute the constant when valid
		if (this.expression.constant != Constant.NotAConstant) {
			this.constant =
				Constant.computeConstantOperation(
					this.expression.constant,
					expressionTypeID,
					(this.bits & OperatorMASK) >> OperatorSHIFT);
		} else {
			this.constant = Constant.NotAConstant;
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT
					|| ((this.bits & OperatorMASK) >> OperatorSHIFT) == TWIDDLE
					|| ((this.bits & OperatorMASK) >> OperatorSHIFT) == MINUS
					|| ((this.bits & OperatorMASK) >> OperatorSHIFT) == PLUS) {
				Constant cst = this.expression.optimizedBooleanConstant();
				if (cst != Constant.NotAConstant) {
					this.optimizedBooleanConstant = BooleanConstant.fromValue(!cst.booleanValue());
				} else {
					if (this.expression.resolvedType.id == T_null)
						scope.problemReporter().invalidOperator(this, expressionType);
				}
			}
		}
		if (expressionIsCast) {
		// check need for operand cast
			CastExpression.checkNeedForArgumentCast(scope, tableId, operatorSignature, this.expression, expressionTypeID);
		}
		return this.resolvedType;
	}

	@Override
	public LocalVariableBinding[] bindingsWhenFalse() {
		return ((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT ?
				this.expression.bindingsWhenTrue(): NO_VARIABLES;
	}

	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		return ((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT ?
				this.expression.bindingsWhenFalse(): NO_VARIABLES;
	}


	@Override
	public void traverse(
    		ASTVisitor visitor,
    		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.expression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
