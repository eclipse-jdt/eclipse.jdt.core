/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nick Teryaev - fix for bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=40752)
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class CastExpression extends Expression {

	public Expression expression;
	public Expression type;
	public TypeBinding expectedType; // when assignment conversion to a given expected type: String s = (String) t;
	
	//expression.implicitConversion holds the cast for baseType casting 
	public CastExpression(Expression expression, Expression type) {
		this.expression = expression;
		this.type = type;
		type.bits |= IgnoreRawTypeCheck; // no need to worry about raw type usage
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return expression
			.analyseCode(currentScope, flowContext, flowInfo)
			.unconditionalInits();
	}

	/**
	 * Complain if assigned expression is cast, but not actually used as such, e.g. Object o = (List) object;
	 */
	public static void checkNeedForAssignedCast(BlockScope scope, TypeBinding expectedType, CastExpression rhs) {
	
		if (scope.compilerOptions().getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
	
		TypeBinding castedExpressionType = rhs.expression.resolvedType;
		//	int i = (byte) n; // cast still had side effect
		// double d = (float) n; // cast to float is unnecessary
		if (castedExpressionType == null || rhs.resolvedType.isBaseType()) return; 
		//if (castedExpressionType.id == T_null) return; // tolerate null expression cast
		if (castedExpressionType.isCompatibleWith(expectedType)) {
			scope.problemReporter().unnecessaryCast(rhs); 
		}
	}
	
	/**
	 * Casting an enclosing instance will considered as useful if removing it would actually bind to a different type
	 */
	public static void checkNeedForEnclosingInstanceCast(BlockScope scope, Expression enclosingInstance, TypeBinding enclosingInstanceType, TypeBinding memberType) {
	
		if (scope.compilerOptions().getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
		
		TypeBinding castedExpressionType = ((CastExpression)enclosingInstance).expression.resolvedType;
		if (castedExpressionType == null) return; // cannot do better
		// obvious identity cast
		if (castedExpressionType == enclosingInstanceType) { 
			scope.problemReporter().unnecessaryCast((CastExpression)enclosingInstance);
		} else if (castedExpressionType == TypeBinding.NULL){
			return; // tolerate null enclosing instance cast
		} else {
			TypeBinding alternateEnclosingInstanceType = castedExpressionType; 
			if (castedExpressionType.isBaseType() || castedExpressionType.isArrayType()) return; // error case
			if (memberType == scope.getMemberType(memberType.sourceName(), (ReferenceBinding) alternateEnclosingInstanceType)) {
				scope.problemReporter().unnecessaryCast((CastExpression)enclosingInstance);
			}
		}
	}

	/**
	 * Only complain for identity cast, since other type of casts may be useful: e.g.  ~((~(long) 0) << 32)  is different from: ~((~0) << 32) 
	 */
	public static void checkNeedForArgumentCast(BlockScope scope, int operator, int operatorSignature, Expression expression, int expressionTypeId) {
	
		if (scope.compilerOptions().getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
	
		// check need for left operand cast
		int alternateLeftTypeId = expressionTypeId;
		if ((expression.bits & UnnecessaryCast) == 0 && expression.resolvedType.isBaseType()) {
			// narrowing conversion on base type may change value, thus necessary
			return;
		} else  {
			TypeBinding alternateLeftType = ((CastExpression)expression).expression.resolvedType;
			if (alternateLeftType == null) return; // cannot do better
			if ((alternateLeftTypeId = alternateLeftType.id) == expressionTypeId) { // obvious identity cast
				scope.problemReporter().unnecessaryCast((CastExpression)expression); 
				return;
			} else if (alternateLeftTypeId == T_null) {
				alternateLeftTypeId = expressionTypeId;  // tolerate null argument cast
				return;
			}
		}
/*		tolerate widening cast in unary expressions, as may be used when combined in binary expressions (41680)
		int alternateOperatorSignature = OperatorExpression.OperatorSignatures[operator][(alternateLeftTypeId << 4) + alternateLeftTypeId];
		// (cast)  left   Op (cast)  right --> result
		//  1111   0000       1111   0000     1111
		//  <<16   <<12       <<8    <<4       <<0
		final int CompareMASK = (0xF<<16) + (0xF<<8) + 0xF; // mask hiding compile-time types
		if ((operatorSignature & CompareMASK) == (alternateOperatorSignature & CompareMASK)) { // same promotions and result
			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); 
		}
*/		
	}
		
	/**
	 * Cast expressions will considered as useful if removing them all would actually bind to a different method
	 * (no fine grain analysis on per casted argument basis, simply separate widening cast from narrowing ones)
	 */
	public static void checkNeedForArgumentCasts(BlockScope scope, Expression receiver, TypeBinding receiverType, MethodBinding binding, Expression[] arguments, TypeBinding[] argumentTypes, final InvocationSite invocationSite) {
	
		if (scope.compilerOptions().getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
		
		int length = argumentTypes.length;

		// iterate over arguments, and retrieve original argument types (before cast)
		TypeBinding[] rawArgumentTypes = argumentTypes;
		for (int i = 0; i < length; i++) {
			Expression argument = arguments[i];
			if (argument instanceof CastExpression) {
 				// narrowing conversion on base type may change value, thus necessary
				if ((argument.bits & UnnecessaryCast) == 0 && argument.resolvedType.isBaseType()) {
					continue;
				}		
				TypeBinding castedExpressionType = ((CastExpression)argument).expression.resolvedType;
				if (castedExpressionType == null) return; // cannot do better
				// obvious identity cast
				if (castedExpressionType == argumentTypes[i]) { 
					scope.problemReporter().unnecessaryCast((CastExpression)argument);
				} else if (castedExpressionType == TypeBinding.NULL){
					continue; // tolerate null argument cast
				} else if ((argument.implicitConversion & BOXING) != 0) {
					continue; // boxing has a side effect: (int) char   is not boxed as simple char
				} else {
					if (rawArgumentTypes == argumentTypes) {
						System.arraycopy(rawArgumentTypes, 0, rawArgumentTypes = new TypeBinding[length], 0, length);
					}
					// retain original argument type
					rawArgumentTypes[i] = castedExpressionType; 
				}
			}				
		}
		// perform alternate lookup with original types
		if (rawArgumentTypes != argumentTypes) {
			checkAlternateBinding(scope, receiver, receiverType, binding, arguments, argumentTypes, rawArgumentTypes, invocationSite);
		}
	}

	/**
	 * Check binary operator casted arguments 
	 */
	public static void checkNeedForArgumentCasts(BlockScope scope, int operator, int operatorSignature, Expression left, int leftTypeId, boolean leftIsCast, Expression right, int rightTypeId, boolean rightIsCast) {

		if (scope.compilerOptions().getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;

		// check need for left operand cast
		int alternateLeftTypeId = leftTypeId;
		if (leftIsCast) {
			if ((left.bits & UnnecessaryCast) == 0 && left.resolvedType.isBaseType()) {
 				// narrowing conversion on base type may change value, thus necessary
 				leftIsCast = false;
			} else  {
				TypeBinding alternateLeftType = ((CastExpression)left).expression.resolvedType;
				if (alternateLeftType == null) return; // cannot do better
				if ((alternateLeftTypeId = alternateLeftType.id) == leftTypeId) { // obvious identity cast
					scope.problemReporter().unnecessaryCast((CastExpression)left); 
					leftIsCast = false;
				} else if (alternateLeftTypeId == T_null) {
					alternateLeftTypeId = leftTypeId;  // tolerate null argument cast
					leftIsCast = false;
				}
			}
		}
		// check need for right operand cast
		int alternateRightTypeId = rightTypeId;
		if (rightIsCast) {
			if ((right.bits & UnnecessaryCast) == 0 && right.resolvedType.isBaseType()) {
 				// narrowing conversion on base type may change value, thus necessary
 				rightIsCast = false;
			} else {
				TypeBinding alternateRightType = ((CastExpression)right).expression.resolvedType;
				if (alternateRightType == null) return; // cannot do better
				if ((alternateRightTypeId = alternateRightType.id) == rightTypeId) { // obvious identity cast
					scope.problemReporter().unnecessaryCast((CastExpression)right); 
					rightIsCast = false;
				} else if (alternateRightTypeId == T_null) {
					alternateRightTypeId = rightTypeId;  // tolerate null argument cast
					rightIsCast = false;
				}
			}	
		}
		if (leftIsCast || rightIsCast) {
			if (alternateLeftTypeId > 15 || alternateRightTypeId > 15) { // must convert String + Object || Object + String
				if (alternateLeftTypeId == T_JavaLangString) {
					alternateRightTypeId = T_JavaLangObject;
				} else if (alternateRightTypeId == T_JavaLangString) {
					alternateLeftTypeId = T_JavaLangObject;
				} else {
					return; // invalid operator
				}
			}
			int alternateOperatorSignature = OperatorExpression.OperatorSignatures[operator][(alternateLeftTypeId << 4) + alternateRightTypeId];
			// (cast)  left   Op (cast)  right --> result
			//  1111   0000       1111   0000     1111
			//  <<16   <<12       <<8    <<4       <<0
			final int CompareMASK = (0xF<<16) + (0xF<<8) + 0xF; // mask hiding compile-time types
			if ((operatorSignature & CompareMASK) == (alternateOperatorSignature & CompareMASK)) { // same promotions and result
				if (leftIsCast) scope.problemReporter().unnecessaryCast((CastExpression)left); 
				if (rightIsCast) scope.problemReporter().unnecessaryCast((CastExpression)right);
			}
		}
	}

	private static void checkAlternateBinding(BlockScope scope, Expression receiver, TypeBinding receiverType, MethodBinding binding, Expression[] arguments, TypeBinding[] originalArgumentTypes, TypeBinding[] alternateArgumentTypes, final InvocationSite invocationSite) {

			InvocationSite fakeInvocationSite = new InvocationSite(){	
				public TypeBinding[] genericTypeArguments() { return null; }
				public boolean isSuperAccess(){ return invocationSite.isSuperAccess(); }
				public boolean isTypeAccess() { return invocationSite.isTypeAccess(); }
				public void setActualReceiverType(ReferenceBinding actualReceiverType) { /* ignore */}
				public void setDepth(int depth) { /* ignore */}
				public void setFieldIndex(int depth){ /* ignore */}
				public int sourceStart() { return 0; }
				public int sourceEnd() { return 0; }
			};	
			MethodBinding bindingIfNoCast;
			if (binding.isConstructor()) {
				bindingIfNoCast = scope.getConstructor((ReferenceBinding)receiverType, alternateArgumentTypes, fakeInvocationSite);
			} else {
				bindingIfNoCast = receiver.isImplicitThis()
					? scope.getImplicitMethod(binding.selector, alternateArgumentTypes, fakeInvocationSite)
					: scope.getMethod(receiverType, binding.selector, alternateArgumentTypes, fakeInvocationSite); 	
			}
			if (bindingIfNoCast == binding) {
				int argumentLength = originalArgumentTypes.length;
				if (binding.isVarargs()) {
					int paramLength = binding.parameters.length;
				   if (paramLength == argumentLength) {
						int varargsIndex = paramLength - 1;
						ArrayBinding varargsType = (ArrayBinding) binding.parameters[varargsIndex];
						TypeBinding lastArgType = alternateArgumentTypes[varargsIndex];
						// originalType may be compatible already, but cast mandated to clarify between varargs/non-varargs call
						int dimensions = lastArgType.dimensions();
						if (varargsType.dimensions < dimensions) {
							return;
						} else if (varargsType.dimensions == dimensions) {
							if (lastArgType.isCompatibleWith(varargsType.elementsType())
									&& lastArgType.isCompatibleWith(varargsType)) {
								return;
							}
						}						
				   }
				}
				for (int i = 0; i < argumentLength; i++) {
					if (originalArgumentTypes[i] != alternateArgumentTypes[i]) {
						scope.problemReporter().unnecessaryCast((CastExpression)arguments[i]);
					}
				}
			}	
	}
	
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (match == castType) {
			if (!isNarrowing && match == this.resolvedType.leafComponentType()) { // do not tag as unnecessary when recursing through upper bounds
				tagAsUnnecessaryCast(scope, castType);
			}
			return true;
		}
		boolean isCastingToBoundParameterized;
		if (match != null && (
				(isCastingToBoundParameterized = castType.isBoundParameterizedType())
				|| 	expressionType.isBoundParameterizedType())) {
			
			if (match.isProvablyDistinctFrom(isNarrowing ? expressionType : castType, 0)) {
				return false; 
			}
			if (isCastingToBoundParameterized 
					&& (isNarrowing ? !expressionType.isEquivalentTo(match) : !match.isEquivalentTo(castType))) {
				this.bits |= UnsafeCast;
				return true;
			} else if ((castType.tagBits & TagBits.HasDirectWildcard) == 0
					&& (!match.isParameterizedType() || expressionType.isRawType())) {
				this.bits |= UnsafeCast;
				return true;
			}
		} else if (isNarrowing) {
			TypeBinding leafType = castType.leafComponentType();
			if (expressionType.id == T_JavaLangObject && castType.isArrayType() && leafType.isBoundParameterizedType()) {
				this.bits |= UnsafeCast;
				return true;
			}
			if (leafType.isTypeVariable()) {
				this.bits |= UnsafeCast;
				return true;
			}
		}
		if (!isNarrowing && match == this.resolvedType.leafComponentType()) { // do not tag as unnecessary when recursing through upper bounds
			tagAsUnnecessaryCast(scope, castType);
		}
		return true;
	}	
	
	/**
	 * Cast expression code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
	
		int pc = codeStream.position;
		boolean needRuntimeCheckcast = (this.bits & GenerateCheckcast) != 0;
		if (constant != Constant.NotAConstant) {
			if (valueRequired || needRuntimeCheckcast) { // Added for: 1F1W9IG: IVJCOM:WINNT - Compiler omits casting check
				codeStream.generateConstant(constant, implicitConversion);
				if (needRuntimeCheckcast) {
					codeStream.checkcast(this.resolvedType);
					if (valueRequired) {
						codeStream.generateImplicitConversion(this.implicitConversion);
					} else {
						codeStream.pop();
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		expression.generateCode(
			currentScope,
			codeStream,
			valueRequired || needRuntimeCheckcast);
		if (needRuntimeCheckcast 
				&& this.expression.postConversionType(currentScope) != this.resolvedType) { // no need to issue a checkcast if already done as genericCast
			codeStream.checkcast(this.resolvedType);
			if (valueRequired) {
				codeStream.generateImplicitConversion(implicitConversion);
			} else {
				codeStream.pop();
			}
		} else {
			if (valueRequired)
				codeStream.generateImplicitConversion(implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public Expression innermostCastedExpression(){ 
		Expression current = this.expression;
		while (current instanceof CastExpression) {
			current = ((CastExpression) current).expression;
		}
		return current;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#localVariableBinding()
	 */
	public LocalVariableBinding localVariableBinding() {
		return this.expression.localVariableBinding();
	}
	
	public int nullStatus(FlowInfo flowInfo) {
		return this.expression.nullStatus(flowInfo);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#optimizedBooleanConstant()
	 */
	public Constant optimizedBooleanConstant() {
		switch(this.resolvedType.id) {
			case T_boolean :
			case T_JavaLangBoolean :
				return this.expression.optimizedBooleanConstant();
		}
		return Constant.NotAConstant;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {

		output.append('(');
		type.print(0, output).append(") "); //$NON-NLS-1$
		return expression.printExpression(0, output);
	}

	public TypeBinding resolveType(BlockScope scope) {
		// compute a new constant if the cast is effective

		// due to the fact an expression may start with ( and that a cast can also start with (
		// the field is an expression....it can be a TypeReference OR a NameReference Or
		// any kind of Expression <-- this last one is invalid.......

		constant = Constant.NotAConstant;
		implicitConversion = T_undefined;

		if ((type instanceof TypeReference) || (type instanceof NameReference)
				&& ((type.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT) == 0) { // no extra parenthesis around type: ((A))exp

			TypeBinding castType = this.resolvedType = type.resolveType(scope);
			//expression.setExpectedType(this.resolvedType); // needed in case of generic method invocation			
			TypeBinding expressionType = expression.resolveType(scope);
			if (castType != null) {
				if (expressionType != null) {
					boolean isLegal = checkCastTypesCompatibility(scope, castType, expressionType, this.expression);
					if (isLegal) {
						this.expression.computeConversion(scope, castType, expressionType);
						if ((this.bits & UnsafeCast) != 0) { // unsafe cast
							scope.problemReporter().unsafeCast(this, scope);
						} else {
//							if (castType.isRawType() && scope.compilerOptions().reportRawTypeReference){
//								scope.problemReporter().rawTypeReference(this.type, castType);			
//							}
							if ((this.bits & (UnnecessaryCast|DisableUnnecessaryCastCheck)) == UnnecessaryCast) { // unnecessary cast 
								if (!isIndirectlyUsed()) // used for generic type inference or boxing ?
									scope.problemReporter().unnecessaryCast(this);
							}
						}
					} else { // illegal cast
						scope.problemReporter().typeCastError(this, castType, expressionType);
						this.bits |= DisableUnnecessaryCastCheck; // disable further secondary diagnosis
					}
				}
				this.resolvedType = castType.capture(scope, this.sourceEnd);
			}
			return this.resolvedType;
		} else { // expression as a cast
			TypeBinding expressionType = expression.resolveType(scope);
			if (expressionType == null) return null;
			scope.problemReporter().invalidTypeReference(type);
			return null;
		}
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#setExpectedType(org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
	 */
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	/**
	 * Determines whether apparent unnecessary cast wasn't actually used to
	 * perform return type inference of generic method invocation or boxing.
	 */
	private boolean isIndirectlyUsed() {
		if (this.expression instanceof MessageSend) {
			MethodBinding method = ((MessageSend)this.expression).binding;
			if (method instanceof ParameterizedGenericMethodBinding
						&& ((ParameterizedGenericMethodBinding)method).inferredReturnType) {
				if (this.expectedType == null) 
					return true;
				if (this.resolvedType != this.expectedType)
					return true;
			}
		}
		if (this.expectedType != null && this.resolvedType.isBaseType() && !this.resolvedType.isCompatibleWith(this.expectedType)) {
			// boxing: Short s = (short) _byte
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#tagAsNeedCheckCast()
	 */
	public void tagAsNeedCheckCast() {
		this.bits |= GenerateCheckcast;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#tagAsUnnecessaryCast(Scope, TypeBinding)
	 */
	public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
		if (this.expression.resolvedType == null) return; // cannot do better if expression is not bound
		this.bits |= UnnecessaryCast;
	}
	
	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			type.traverse(visitor, blockScope);
			expression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
