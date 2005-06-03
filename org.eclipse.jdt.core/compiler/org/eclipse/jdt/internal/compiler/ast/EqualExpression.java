/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class EqualExpression extends BinaryExpression {

	public EqualExpression(Expression left, Expression right,int operator) {
		super(left,right,operator);
	}
	public void checkNullComparison(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, FlowInfo initsWhenTrue, FlowInfo initsWhenFalse) {
		
		LocalVariableBinding local = this.left.localVariableBinding();
		if (local != null) {
			checkVariableComparison(scope, flowContext, flowInfo, initsWhenTrue, initsWhenFalse, local, right.nullStatus(flowInfo), this.left);
		}
		local = this.right.localVariableBinding();
		if (local != null) {
			checkVariableComparison(scope, flowContext, flowInfo, initsWhenTrue, initsWhenFalse, local, left.nullStatus(flowInfo), this.right);
		}
	}
	private void checkVariableComparison(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, FlowInfo initsWhenTrue, FlowInfo initsWhenFalse, LocalVariableBinding local, int nullStatus, Expression reference) {
		switch (nullStatus) {
			case FlowInfo.NULL :
				flowContext.recordUsingNullReference(scope, local, reference, FlowInfo.NULL, flowInfo);
				if (((this.bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
					initsWhenTrue.markAsDefinitelyNull(local); // from thereon it is set
					initsWhenFalse.markAsDefinitelyNonNull(local); // from thereon it is set
				} else {
					initsWhenTrue.markAsDefinitelyNonNull(local); // from thereon it is set
					initsWhenFalse.markAsDefinitelyNull(local); // from thereon it is set
				}
				break;
			case FlowInfo.NON_NULL :
				flowContext.recordUsingNullReference(scope, local, reference, FlowInfo.NON_NULL, flowInfo);
				if (((this.bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
					initsWhenTrue.markAsDefinitelyNonNull(local); // from thereon it is set
				}
				break;
		}	
	}
	
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		if (((bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
			if ((left.constant != NotAConstant) && (left.constant.typeID() == T_boolean)) {
				if (left.constant.booleanValue()) { //  true == anything
					//  this is equivalent to the right argument inits 
					return right.analyseCode(currentScope, flowContext, flowInfo);
				} else { // false == anything
					//  this is equivalent to the right argument inits negated
					return right.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
				}
			}
			if ((right.constant != NotAConstant) && (right.constant.typeID() == T_boolean)) {
				if (right.constant.booleanValue()) { //  anything == true
					//  this is equivalent to the right argument inits 
					return left.analyseCode(currentScope, flowContext, flowInfo);
				} else { // anything == false
					//  this is equivalent to the right argument inits negated
					return left.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
				}
			}
			return right.analyseCode(
				currentScope, flowContext, 
				left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits()).unconditionalInits();
		} else { //NOT_EQUAL :
			if ((left.constant != NotAConstant) && (left.constant.typeID() == T_boolean)) {
				if (!left.constant.booleanValue()) { //  false != anything
					//  this is equivalent to the right argument inits 
					return right.analyseCode(currentScope, flowContext, flowInfo);
				} else { // true != anything
					//  this is equivalent to the right argument inits negated
					return right.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
				}
			}
			if ((right.constant != NotAConstant) && (right.constant.typeID() == T_boolean)) {
				if (!right.constant.booleanValue()) { //  anything != false
					//  this is equivalent to the right argument inits 
					return left.analyseCode(currentScope, flowContext, flowInfo);
				} else { // anything != true
					//  this is equivalent to the right argument inits negated
					return left.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
				}
			}
			return right.analyseCode(
				currentScope, flowContext, 
				left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits()).asNegatedCondition().unconditionalInits();
		}
	}
	
	public final void computeConstant(TypeBinding leftType, TypeBinding rightType) {
		if ((this.left.constant != NotAConstant) && (this.right.constant != NotAConstant)) {
			this.constant =
				Constant.computeConstantOperationEQUAL_EQUAL(
					left.constant,
					leftType.id,
					right.constant,
					rightType.id);
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT_EQUAL)
				constant = Constant.fromValue(!constant.booleanValue());
		} else {
			this.constant = NotAConstant;
			// no optimization for null == null
		}
	}
	/**
	 * Normal == or != code generation.
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	
		if (constant != NotAConstant) {
			int pc = codeStream.position;
			if (valueRequired) 
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		Label falseLabel;
		bits |= OnlyValueRequiredMASK;
		generateOptimizedBoolean(
			currentScope, 
			codeStream, 
			null, 
			falseLabel = new Label(codeStream), 
			valueRequired);
		if (falseLabel.hasForwardReferences()) {
			if (valueRequired){
				// comparison is TRUE 
				codeStream.iconst_1();
				if ((bits & ValueForReturnMASK) != 0){
					codeStream.generateImplicitConversion(this.implicitConversion);
					codeStream.generateReturnBytecode(this);
					// comparison is FALSE
					falseLabel.place();
					codeStream.iconst_0();
				} else {
					Label endLabel = new Label(codeStream);
					codeStream.goto_(endLabel);
					codeStream.decrStackSize(1);
					// comparison is FALSE
					falseLabel.place();
					codeStream.iconst_0();
					endLabel.place();
				}
				codeStream.generateImplicitConversion(implicitConversion);
			} else {
				falseLabel.place();
			}	
		}
	}
	/**
	 * Boolean operator code generation
	 *	Optimized operations are: == and !=
	 */
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	
		if (constant != Constant.NotAConstant) {
			super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			return;
		}
		if (((bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
			if ((left.implicitConversion & COMPILE_TYPE_MASK) /*compile-time*/ == T_boolean) {
				generateOptimizedBooleanEqual(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			} else {
				generateOptimizedNonBooleanEqual(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			}
		} else {
			if ((left.implicitConversion & COMPILE_TYPE_MASK) /*compile-time*/ == T_boolean) {
				generateOptimizedBooleanEqual(currentScope, codeStream, falseLabel, trueLabel, valueRequired);
			} else {
				generateOptimizedNonBooleanEqual(currentScope, codeStream, falseLabel, trueLabel, valueRequired);
			}
		}
	}
	/**
	 * Boolean generation for == with boolean operands
	 *
	 * Note this code does not optimize conditional constants !!!!
	 */
	public void generateOptimizedBooleanEqual(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	
		// optimized cases: true == x, false == x
		if (left.constant != NotAConstant) {
			boolean inline = left.constant.booleanValue();
			right.generateOptimizedBoolean(currentScope, codeStream, (inline ? trueLabel : falseLabel), (inline ? falseLabel : trueLabel), valueRequired);
			return;
		} // optimized cases: x == true, x == false
		if (right.constant != NotAConstant) {
			boolean inline = right.constant.booleanValue();
			left.generateOptimizedBoolean(currentScope, codeStream, (inline ? trueLabel : falseLabel), (inline ? falseLabel : trueLabel), valueRequired);
			return;
		}
		// default case
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					codeStream.if_icmpeq(trueLabel);
				}
			} else {
				// implicit falling through the TRUE case
				if (trueLabel == null) {
					codeStream.if_icmpne(falseLabel);
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
		// reposition the endPC
		codeStream.updateLastRecordedEndPC(currentScope, codeStream.position);					
	}
	/**
	 * Boolean generation for == with non-boolean operands
	 *
	 */
	public void generateOptimizedNonBooleanEqual(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	
		int pc = codeStream.position;
		Constant inline;
		if ((inline = right.constant) != NotAConstant) {
			// optimized case: x == 0
			if ((((left.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) == T_int) && (inline.intValue() == 0)) {
				left.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicit falling through the FALSE case
							codeStream.ifeq(trueLabel);
						}
					} else {
						// implicit falling through the TRUE case
						if (trueLabel == null) {
							codeStream.ifne(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		if ((inline = left.constant) != NotAConstant) {
			// optimized case: 0 == x
			if ((((left.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) == T_int)
				&& (inline.intValue() == 0)) {
				right.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicit falling through the FALSE case
							codeStream.ifeq(trueLabel);
						}
					} else {
						// implicit falling through the TRUE case
						if (trueLabel == null) {
							codeStream.ifne(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// null cases
		// optimized case: x == null
		if (right instanceof NullLiteral) {
			if (left instanceof NullLiteral) {
				// null == null
				if (valueRequired) {
						if ((bits & OnlyValueRequiredMASK) != 0) {
							if (((bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
								codeStream.iconst_1();
							} else {
								codeStream.iconst_0();
							}
						} else {
							if (falseLabel == null) {
								// implicit falling through the FALSE case
								if (trueLabel != null) {
									codeStream.goto_(trueLabel);
								}
							}
					}
				}
			} else {
				left.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicit falling through the FALSE case
							codeStream.ifnull(trueLabel);
						}
					} else {
						// implicit falling through the TRUE case
						if (trueLabel == null) {
							codeStream.ifnonnull(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		} else if (left instanceof NullLiteral) { // optimized case: null == x
			right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifnull(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifnonnull(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
	
		// default case
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					switch ((left.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) { // operand runtime type
						case T_int :
							codeStream.if_icmpeq(trueLabel);
							break;
						case T_float :
							codeStream.fcmpl();
							codeStream.ifeq(trueLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifeq(trueLabel);
							break;
						case T_double :
							codeStream.dcmpl();
							codeStream.ifeq(trueLabel);
							break;
						default :
							codeStream.if_acmpeq(trueLabel);
					}
				}
			} else {
				// implicit falling through the TRUE case
				if (trueLabel == null) {
					switch ((left.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) { // operand runtime type
						case T_int :
							codeStream.if_icmpne(falseLabel);
							break;
						case T_float :
							codeStream.fcmpl();
							codeStream.ifne(falseLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifne(falseLabel);
							break;
						case T_double :
							codeStream.dcmpl();
							codeStream.ifne(falseLabel);
							break;
						default :
							codeStream.if_acmpne(falseLabel);
					}
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	public boolean isCompactableOperation() {
		return false;
	}
	public TypeBinding resolveType(BlockScope scope) {
	
			boolean leftIsCast, rightIsCast;
			if ((leftIsCast = left instanceof CastExpression) == true) left.bits |= IgnoreNeedForCastCheckMASK; // will check later on
			TypeBinding originalLeftType = left.resolveType(scope);
	
			if ((rightIsCast = right instanceof CastExpression) == true) right.bits |= IgnoreNeedForCastCheckMASK; // will check later on
			TypeBinding originalRightType = right.resolveType(scope);
	
		// always return BooleanBinding
		if (originalLeftType == null || originalRightType == null){
			constant = NotAConstant;		
			return null;
		}
	
		// autoboxing support
		boolean use15specifics = scope.compilerOptions().sourceLevel >= JDK1_5;
		TypeBinding leftType = originalLeftType, rightType = originalRightType;
		if (use15specifics) {
			if (leftType != NullBinding && leftType.isBaseType()) {
				if (!rightType.isBaseType()) {
					rightType = scope.environment().computeBoxingType(rightType);
				}
			} else {
				if (rightType != NullBinding && rightType.isBaseType()) {
					leftType = scope.environment().computeBoxingType(leftType);
				}
			}
		}
		// both base type
		if (leftType.isBaseType() && rightType.isBaseType()) {
			int leftTypeID = leftType.id;
			int rightTypeID = rightType.id;
	
			// the code is an int
			// (cast)  left   == (cast)  right --> result
			//  0000   0000       0000   0000      0000
			//  <<16   <<12       <<8    <<4       <<0
			int operatorSignature = OperatorSignatures[EQUAL_EQUAL][ (leftTypeID << 4) + rightTypeID];
			left.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), originalLeftType);
			right.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 8) & 0x0000F), originalRightType);
			bits |= operatorSignature & 0xF;		
			if ((operatorSignature & 0x0000F) == T_undefined) {
				constant = Constant.NotAConstant;
				scope.problemReporter().invalidOperator(this, leftType, rightType);
				return null;
			}
			// check need for operand cast
			if (leftIsCast || rightIsCast) {
				CastExpression.checkNeedForArgumentCasts(scope, EQUAL_EQUAL, operatorSignature, left, leftType.id, leftIsCast, right, rightType.id, rightIsCast);
			}
			computeConstant(leftType, rightType);
			return this.resolvedType = BooleanBinding;
		}
	
		// Object references 
		// spec 15.20.3
		if ((!leftType.isBaseType() || leftType == NullBinding) // cannot compare: Object == (int)0
				&& (!rightType.isBaseType() || rightType == NullBinding)
				&& (this.checkCastTypesCompatibility(scope, leftType, rightType, null) 
						|| this.checkCastTypesCompatibility(scope, rightType, leftType, null))) {

			// (special case for String)
			if ((rightType.id == T_JavaLangString) && (leftType.id == T_JavaLangString)) {
				computeConstant(leftType, rightType);
			} else {
				constant = NotAConstant;
			}
			TypeBinding objectType = scope.getJavaLangObject();
			left.computeConversion(scope, objectType, leftType);
			right.computeConversion(scope, objectType, rightType);
			// check need for operand cast
			boolean unnecessaryLeftCast = (left.bits & UnnecessaryCastMASK) != 0;
			boolean unnecessaryRightCast = (right.bits & UnnecessaryCastMASK) != 0;
			if (unnecessaryLeftCast || unnecessaryRightCast) {
				TypeBinding alternateLeftType = unnecessaryLeftCast ? ((CastExpression)left).expression.resolvedType : leftType;
				TypeBinding alternateRightType = unnecessaryRightCast ? ((CastExpression)right).expression.resolvedType : rightType;
				if (this.checkCastTypesCompatibility(scope, alternateLeftType, alternateRightType, null) 
						|| this.checkCastTypesCompatibility(scope, alternateRightType, alternateLeftType, null)) {
					if (unnecessaryLeftCast) scope.problemReporter().unnecessaryCast((CastExpression)left); 
					if (unnecessaryRightCast) scope.problemReporter().unnecessaryCast((CastExpression)right);
				}
			}
			return this.resolvedType = BooleanBinding;
		}
		constant = NotAConstant;
		scope.problemReporter().notCompatibleTypesError(this, leftType, rightType);
		return null;
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			left.traverse(visitor, scope);
			right.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
