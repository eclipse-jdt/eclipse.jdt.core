/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CastExpression extends Expression {

	public Expression expression;
	public Expression type;
	public boolean needRuntimeCheckcast;

	//expression.implicitConversion holds the cast for baseType casting 
	public CastExpression(Expression e, Expression t) {
		expression = e;
		type = t;

		//due to the fact an expression may start with ( and that a cast also start with (
		//the field is an expression....it can be a TypeReference OR a NameReference Or
		//an expression <--this last one is invalid.......

		// :-( .............

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

	public final void areTypesCastCompatible(
		BlockScope scope,
		TypeBinding castType,
		TypeBinding expressionType) {

		// see specifications 5.5
		// handle errors and process constant when needed

		// if either one of the type is null ==>
		// some error has been already reported some where ==>
		// we then do not report an obvious-cascade-error.

		needRuntimeCheckcast = false;
		if (castType == null || expressionType == null) return;

		// identity conversion cannot be performed upfront, due to side-effects
		// like constant propagation
				
		if (castType.isBaseType()) {
			if (expressionType.isBaseType()) {
				if (expressionType == castType) {
					expression.implicitWidening(castType, expressionType);
					constant = expression.constant; //use the same constant
					return;
				}
				if (expressionType.isCompatibleWith(castType)
					|| BaseTypeBinding.isNarrowing(castType.id, expressionType.id)) {
					expression.implicitConversion = (castType.id << 4) + expressionType.id;
					if (expression.constant != Constant.NotAConstant)
						constant = expression.constant.castTo(expression.implicitConversion);
					return;
				}
			}
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return;
		}

		//-----------cast to something which is NOT a base type--------------------------	
		if (expressionType == NullBinding) {
			//	if (castType.isArrayType()){ // 26903 - need checkcast when casting null to array type
			//		needRuntimeCheckcast = true;
			//	}
			return; //null is compatible with every thing
		}
		if (expressionType.isBaseType()) {
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return;
		}

		if (expressionType.isArrayType()) {
			if (castType == expressionType) return; // identity conversion

			if (castType.isArrayType()) {
				//------- (castType.isArray) expressionType.isArray -----------
				TypeBinding exprElementType = ((ArrayBinding) expressionType).elementsType(scope);
				if (exprElementType.isBaseType()) {
					// <---stop the recursion------- 
					if (((ArrayBinding) castType).elementsType(scope) == exprElementType)
						needRuntimeCheckcast = true;
					else
						scope.problemReporter().typeCastError(this, castType, expressionType);
					return;
				}
				// recursively on the elements...
				areTypesCastCompatible(
					scope,
					((ArrayBinding) castType).elementsType(scope),
					exprElementType);
				return;
			} else if (
				castType.isClass()) {
				//------(castType.isClass) expressionType.isArray ---------------	
				if (scope.isJavaLangObject(castType))
					return;
			} else { //------- (castType.isInterface) expressionType.isArray -----------
				if (scope.isJavaLangCloneable(castType) || scope.isJavaIoSerializable(castType)) {
					needRuntimeCheckcast = true;
					return;
				}
			}
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return;
		}

		if (expressionType.isClass()) {
			if (castType.isArrayType()) {
				// ---- (castType.isArray) expressionType.isClass -------
				if (scope.isJavaLangObject(expressionType)) { // potential runtime error
					needRuntimeCheckcast = true;
					return;
				}
			} else if (castType.isClass()) { // ----- (castType.isClass) expressionType.isClass ------
				if (expressionType.isCompatibleWith(castType)){ // no runtime error
					if (castType.id == T_String) constant = expression.constant; // (String) cst is still a constant
					return;
				}
				if (castType.isCompatibleWith(expressionType)) {
					// potential runtime  error
					needRuntimeCheckcast = true;
					return;
				}
			} else { // ----- (castType.isInterface) expressionType.isClass -------  
				if (((ReferenceBinding) expressionType).isFinal()) {
					// no subclass for expressionType, thus compile-time check is valid
					if (expressionType.isCompatibleWith(castType)) 
						return;
				} else { // a subclass may implement the interface ==> no check at compile time
					needRuntimeCheckcast = true;
					return;
				}
			}
			scope.problemReporter().typeCastError(this, castType, expressionType);
			return;
		}

		//	if (expressionType.isInterface()) { cannot be anything else
		if (castType.isArrayType()) {
			// ----- (castType.isArray) expressionType.isInterface ------
			if (scope.isJavaLangCloneable(expressionType)
				|| scope.isJavaIoSerializable(expressionType)) // potential runtime error
				needRuntimeCheckcast = true;
			else
				scope.problemReporter().typeCastError(this, castType, expressionType);
			return;
		} else if (castType.isClass()) { // ----- (castType.isClass) expressionType.isInterface --------
			if (scope.isJavaLangObject(castType)) // no runtime error
				return;
			if (((ReferenceBinding) castType).isFinal()) {
				// no subclass for castType, thus compile-time check is valid
				if (!castType.isCompatibleWith(expressionType)) {
					// potential runtime error
					scope.problemReporter().typeCastError(this, castType, expressionType);
					return;
				}
			}
		} else { // ----- (castType.isInterface) expressionType.isInterface -------
			if (castType == expressionType) return; // identity conversion
			if (Scope.compareTypes(castType, expressionType) == NotRelated) {
				MethodBinding[] castTypeMethods = ((ReferenceBinding) castType).methods();
				MethodBinding[] expressionTypeMethods =
					((ReferenceBinding) expressionType).methods();
				int exprMethodsLength = expressionTypeMethods.length;
				for (int i = 0, castMethodsLength = castTypeMethods.length; i < castMethodsLength; i++)
					for (int j = 0; j < exprMethodsLength; j++) {
						if ((castTypeMethods[i].returnType != expressionTypeMethods[j].returnType)
								&& (castTypeMethods[i].selector == expressionTypeMethods[j].selector)
								&& castTypeMethods[i].areParametersEqual(expressionTypeMethods[j])) {
							scope.problemReporter().typeCastError(this, castType, expressionType);
						}
					}
			}
		}
		needRuntimeCheckcast = true;
		return;
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
		if (constant != NotAConstant) {
			if (valueRequired
				|| needRuntimeCheckcast) { // Added for: 1F1W9IG: IVJCOM:WINNT - Compiler omits casting check
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

	public TypeBinding resolveType(BlockScope scope) {
		// compute a new constant if the cast is effective

		// due to the fact an expression may start with ( and that a cast can also start with (
		// the field is an expression....it can be a TypeReference OR a NameReference Or
		// any kind of Expression <-- this last one is invalid.......

		constant = Constant.NotAConstant;
		implicitConversion = T_undefined;
		if ((type instanceof TypeReference) || (type instanceof NameReference)) {
			this.resolvedType = type.resolveType(scope);
			TypeBinding castedExpressionType = expression.resolveType(scope);
			if (this.resolvedType != null && castedExpressionType != null) {
				areTypesCastCompatible(scope, this.resolvedType, castedExpressionType);
			}
			return this.resolvedType;
		} else { // expression as a cast !!!!!!!!
			TypeBinding castedExpressionType = expression.resolveType(scope);
			if (castedExpressionType == null) return null;
			scope.problemReporter().invalidTypeReference(type);
			return null;
		}
	}

	public String toStringExpression() {

		return "(" + type.toString(0) + ") " + //$NON-NLS-2$ //$NON-NLS-1$
		expression.toStringExpression();
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			type.traverse(visitor, blockScope);
			expression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
