/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nick Teryaev - fix for bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=40752)
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
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

		//due to the fact an expression may start with ( and that a cast also start with (
		//the field is an expression....it can be a TypeReference OR a NameReference Or
		//an expression <--this last one is invalid.......

		//if (type instanceof TypeReference )
		//	flag = IsTypeReference ;
		//else
		//	if (type instanceof NameReference)
		//		flag = IsNameReference ;
		//	else
		//		flag = IsExpression ;

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
	 * Returns false if the cast is unnecessary
	 */
	public final boolean checkCastTypesCompatibility(
		BlockScope scope,
		TypeBinding castType,
		TypeBinding expressionType) {
	
		// see specifications 5.5
		// handle errors and process constant when needed
	
		// if either one of the type is null ==>
		// some error has been already reported some where ==>
		// we then do not report an obvious-cascade-error.
	
		if (castType == null || expressionType == null) return true;
	
		// identity conversion cannot be performed upfront, due to side-effects
		// like constant propagation
				
		if (castType.isBaseType()) {
			if (expressionType.isBaseType()) {
				if (expressionType == castType) {
					constant = expression.constant; //use the same constant
					return false;
				}
				boolean necessary = false;
				if (expressionType.isCompatibleWith(castType)
						|| (necessary = BaseTypeBinding.isNarrowing(castType.id, expressionType.id))) {
					expression.implicitConversion = (castType.id << 4) + expressionType.id;
					if (expression.constant != Constant.NotAConstant) {
						constant = expression.constant.castTo(expression.implicitConversion);
					}
					return necessary;
					
				}
			}
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return true;
		}
	
		//-----------cast to something which is NOT a base type--------------------------	
		if (expressionType == NullBinding) {
			//	if (castType.isArrayType()){ // 26903 - need checkcast when casting null to array type
			//		needRuntimeCheckcast = true;
			//	}
			return false; //null is compatible with every thing
		}
		if (expressionType.isBaseType()) {
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return true;
		}
	
		if (expressionType.isArrayType()) {
			if (castType == expressionType) return false; // identity conversion
	
			if (castType.isArrayType()) {
				//------- (castType.isArray) expressionType.isArray -----------
				TypeBinding exprElementType = ((ArrayBinding) expressionType).elementsType();
				if (exprElementType.isBaseType()) {
					// <---stop the recursion------- 
					if (((ArrayBinding) castType).elementsType() == exprElementType) {
						this.bits |= NeedRuntimeCheckCastMASK;
					} else {
						scope.problemReporter().typeCastError(this, castType, expressionType);
					}
					return true;
				}
				// recursively on the elements...
				return checkCastTypesCompatibility(
					scope,
					((ArrayBinding) castType).elementsType(),
					exprElementType);
			} else if (
				castType.isClass()) {
				//------(castType.isClass) expressionType.isArray ---------------	
				if (castType.id == T_Object) {
					return false;
				}
			} else { //------- (castType.isInterface) expressionType.isArray -----------
				if (castType.id == T_JavaLangCloneable || castType.id == T_JavaIoSerializable) {
					this.bits |= NeedRuntimeCheckCastMASK;
					return true;
				}
			}
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return true;
		}
	
		if (expressionType.isClass()) {
			if (castType.isArrayType()) {
				// ---- (castType.isArray) expressionType.isClass -------
				if (expressionType.id == T_Object) { // potential runtime error
					this.bits |= NeedRuntimeCheckCastMASK;
					return true;
				}
			} else if (castType.isClass()) { // ----- (castType.isClass) expressionType.isClass ------
				if (expressionType.isCompatibleWith(castType)){ // no runtime error
					if (castType.id == T_String) constant = expression.constant; // (String) cst is still a constant
					if (castType.isParameterizedType() || castType.isGenericType()) {
						if (castType.erasure() == expressionType.erasure() && castType != expressionType) {
							scope.problemReporter().unsafeCast(this);
						}
					}
					return false;
				}
				if (castType.isCompatibleWith(expressionType)) {
					// potential runtime  error
					this.bits |= NeedRuntimeCheckCastMASK;
					if (castType.isParameterizedType() || castType.isGenericType()) {
						ReferenceBinding match = ((ReferenceBinding)castType).findSuperTypeErasingTo((ReferenceBinding)expressionType.erasure());
						if (match != null && !match.isParameterizedType() && !match.isGenericType()) {
							scope.problemReporter().unsafeCast(this);
						}
					}
					return true;
				}
			} else { // ----- (castType.isInterface) expressionType.isClass -------  
				if (expressionType.isCompatibleWith(castType)) {
					if (castType.isParameterizedType() || castType.isGenericType()) {
						if (castType.erasure() == expressionType.erasure() && castType != expressionType) {
							scope.problemReporter().unsafeCast(this);
						}
					}
					return false;
				}
				if (!((ReferenceBinding) expressionType).isFinal()) {
					// a subclass may implement the interface ==> no check at compile time
					this.bits |= NeedRuntimeCheckCastMASK;
					if (castType.isParameterizedType() || castType.isGenericType()) {
						ReferenceBinding match = ((ReferenceBinding)castType).findSuperTypeErasingTo((ReferenceBinding)expressionType.erasure());
						if (match != null && !match.isParameterizedType() && !match.isGenericType()) {
							scope.problemReporter().unsafeCast(this);
						}
					}
					return true;				    
				}
				// no subclass for expressionType, thus compile-time check is valid
			}
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return true;
		}
	
		//	if (expressionType.isInterface()) { cannot be anything else
		if (castType.isArrayType()) {
			// ----- (castType.isArray) expressionType.isInterface ------
			if (expressionType.id == T_JavaLangCloneable
					|| expressionType.id == T_JavaIoSerializable) {// potential runtime error
				this.bits |= NeedRuntimeCheckCastMASK;
			} else {
				scope.problemReporter().typeCastError(this, castType, expressionType);
			}
			return true;
		} else if (castType.isClass()) { // ----- (castType.isClass) expressionType.isInterface --------
			if (castType.id == T_Object) { // no runtime error
				return false;
			}
			if (((ReferenceBinding) castType).isFinal()) {
				// no subclass for castType, thus compile-time check is valid
				if (!castType.isCompatibleWith(expressionType)) {
					// potential runtime error
					scope.problemReporter().typeCastError(this, castType, expressionType);
					return true;
				}
			}
		} else { // ----- (castType.isInterface) expressionType.isInterface -------
			if (expressionType.isCompatibleWith(castType)) {
				if (castType.isParameterizedType() || castType.isGenericType()) {
					if (castType.erasure() == expressionType.erasure() && castType != expressionType) {
						scope.problemReporter().unsafeCast(this);
					}
				}
				return false; 
			}
			if (castType.isCompatibleWith(expressionType)) {
				if (castType.isParameterizedType() || castType.isGenericType()) {
					ReferenceBinding match = ((ReferenceBinding)castType).findSuperTypeErasingTo((ReferenceBinding)expressionType.erasure());
					if (match != null && !match.isParameterizedType() && !match.isGenericType()) {
						scope.problemReporter().unsafeCast(this);
					}
				}				
			} else {
				MethodBinding[] castTypeMethods = ((ReferenceBinding) castType).methods();
				MethodBinding[] expressionTypeMethods =
					((ReferenceBinding) expressionType).methods();
				int exprMethodsLength = expressionTypeMethods.length;
				for (int i = 0, castMethodsLength = castTypeMethods.length; i < castMethodsLength; i++)
					for (int j = 0; j < exprMethodsLength; j++) {
						if ((castTypeMethods[i].returnType != expressionTypeMethods[j].returnType)
								&& (CharOperation.equals(castTypeMethods[i].selector, expressionTypeMethods[j].selector))
								&& castTypeMethods[i].areParametersEqual(expressionTypeMethods[j])) {
							scope.problemReporter().typeCastError(this, castType, expressionType);
						}
					}
			}
		}
		this.bits |= NeedRuntimeCheckCastMASK;
		return true;
	}

	/**
	 * Casting an enclosing instance will considered as useful if removing it would actually bind to a different type
	 */
	public static void checkNeedForEnclosingInstanceCast(BlockScope scope, Expression enclosingInstance, TypeBinding enclosingInstanceType, TypeBinding memberType) {
	
		if (scope.environment().options.getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
		
		TypeBinding castedExpressionType = ((CastExpression)enclosingInstance).expression.resolvedType;
		if (castedExpressionType == null) return; // cannot do better
		// obvious identity cast
		if (castedExpressionType == enclosingInstanceType) { 
			scope.problemReporter().unnecessaryCast((CastExpression)enclosingInstance);
		} else if (castedExpressionType == NullBinding){
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
	
		if (scope.environment().options.getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
	
		// check need for left operand cast
		int alternateLeftTypeId = expressionTypeId;
		if ((expression.bits & UnnecessaryCastMask) == 0 && expression.resolvedType.isBaseType()) {
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
	
		if (scope.environment().options.getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;
		
		int length = argumentTypes.length;

		// iterate over arguments, and retrieve original argument types (before cast)
		TypeBinding[] rawArgumentTypes = argumentTypes;
		for (int i = 0; i < length; i++) {
			Expression argument = arguments[i];
			if (argument instanceof CastExpression) {
 				// narrowing conversion on base type may change value, thus necessary
				if ((argument.bits & UnnecessaryCastMask) == 0 && argument.resolvedType.isBaseType()) {
					continue;
				}		
				TypeBinding castedExpressionType = ((CastExpression)argument).expression.resolvedType;
				if (castedExpressionType == null) return; // cannot do better
				// obvious identity cast
				if (castedExpressionType == argumentTypes[i]) { 
					scope.problemReporter().unnecessaryCast((CastExpression)argument);
				} else if (castedExpressionType == NullBinding){
					continue; // tolerate null argument cast
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

		if (scope.environment().options.getSeverity(CompilerOptions.UnnecessaryTypeCheck) == ProblemSeverities.Ignore) return;

		// check need for left operand cast
		int alternateLeftTypeId = leftTypeId;
		if (leftIsCast) {
			if ((left.bits & UnnecessaryCastMask) == 0 && left.resolvedType.isBaseType()) {
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
			if ((right.bits & UnnecessaryCastMask) == 0 && right.resolvedType.isBaseType()) {
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
				if (alternateLeftTypeId == T_String) {
					alternateRightTypeId = T_Object;
				} else if (alternateRightTypeId == T_String) {
					alternateLeftTypeId = T_Object;
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
				if (leftIsCast) scope.problemReporter().unnecessaryCastForArgument((CastExpression)left,  TypeBinding.wellKnownType(scope, left.implicitConversion >> 4)); 
				if (rightIsCast) scope.problemReporter().unnecessaryCastForArgument((CastExpression)right, TypeBinding.wellKnownType(scope,  right.implicitConversion >> 4));
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
				for (int i = 0, length = originalArgumentTypes.length; i < length; i++) {
					if (originalArgumentTypes[i] != alternateArgumentTypes[i]) {
						scope.problemReporter().unnecessaryCastForArgument((CastExpression)arguments[i], binding.parameters[i]);
					}
				}
			}	
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
		boolean needRuntimeCheckcast = (this.bits & NeedRuntimeCheckCastMASK) != 0;
		if (constant != NotAConstant) {
			if (valueRequired || needRuntimeCheckcast) { // Added for: 1F1W9IG: IVJCOM:WINNT - Compiler omits casting check
				codeStream.generateConstant(constant, implicitConversion);
				if (needRuntimeCheckcast) {
					codeStream.checkcast(this.resolvedType);
					if (!valueRequired)
						codeStream.pop();
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		expression.generateCode(
			currentScope,
			codeStream,
			valueRequired || needRuntimeCheckcast);
		if (needRuntimeCheckcast) {
			codeStream.checkcast(this.resolvedType);
			if (!valueRequired)
				codeStream.pop();
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

			this.resolvedType = type.resolveType(scope);
			expression.setExpectedType(this.resolvedType); // needed in case of generic method invocation			
			TypeBinding expressionType = expression.resolveType(scope);
			if (this.resolvedType != null && expressionType != null) {
				boolean necessary = checkCastTypesCompatibility(scope, this.resolvedType, expressionType);
				expression.computeConversion(scope, this.resolvedType, expressionType);
				if (!necessary && this.expression.resolvedType != null) { // cannot do better if expression is not bound
					this.bits |= UnnecessaryCastMask;
					if ((this.bits & IgnoreNeedForCastCheckMASK) == 0) {
						if (!usedForGenericMethodReturnTypeInference()) // used for generic type inference ?
							scope.problemReporter().unnecessaryCast(this);
					}
				}
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
	 * perform return type inference of generic method invocation.
	 */
	private boolean usedForGenericMethodReturnTypeInference() {
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
		return false;
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
