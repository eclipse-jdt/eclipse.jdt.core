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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class InstanceOfExpression extends OperatorExpression {

	public Expression expression;
	public TypeReference type;

	public InstanceOfExpression(
		Expression expression,
		TypeReference type,
		int operator) {

		this.expression = expression;
		this.type = type;
		this.bits |= operator << OperatorSHIFT;
		this.sourceStart = expression.sourceStart;
		this.sourceEnd = type.sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return expression
			.analyseCode(currentScope, flowContext, flowInfo)
			.unconditionalInits();
	}

	public final boolean areTypesCastCompatible(
		BlockScope scope,
		TypeBinding castType,
		TypeBinding expressionType) {

		//	see specifications p.68
		//A more cpmplete version of this method is provided on
		//CastExpression (it deals with constant and need runtime checkcast)

		if (castType == expressionType) return true;
		
		//by grammatical construction, the first test is ALWAYS false
		//if (castTb.isBaseType())
		//{	if (expressionTb.isBaseType())
		//	{	if (expression.isConstantValueOfTypeAssignableToType(expressionTb,castTb))
		//		{	return true;}
		//		else
		//		{	if (expressionTb==castTb)
		//			{	return true;}
		//			else 
		//			{	if (scope.areTypesCompatible(expressionTb,castTb))
		//				{	return true; }
		//				
		//				if (BaseTypeBinding.isNarrowing(castTb.id,expressionTb.id))
		//				{	return true;}
		//				return false;}}}
		//	else
		//	{	return false; }}
		//else
		{ //-------------checkcast to something which is NOT a basetype----------------------------------	

			//null is compatible with every thing .... 
			if (NullBinding == expressionType) {
				return true;
			}
			if (expressionType.isArrayType()) {
				if (castType.isArrayType()) {
					//------- (castTb.isArray) expressionTb.isArray -----------
					TypeBinding expressionEltTb = ((ArrayBinding) expressionType).elementsType(scope);
					if (expressionEltTb.isBaseType())
						// <---stop the recursion------- 
						return ((ArrayBinding) castType).elementsType(scope) == expressionEltTb;
					//recursivly on the elts...
					return areTypesCastCompatible(
						scope,
						((ArrayBinding) castType).elementsType(scope),
						expressionEltTb);
				}
				if (castType.isClass()) {
					//------(castTb.isClass) expressionTb.isArray ---------------	
					if (scope.isJavaLangObject(castType))
						return true;
					return false;
				}
				if (castType.isInterface()) {
					//------- (castTb.isInterface) expressionTb.isArray -----------
					if (scope.isJavaLangCloneable(castType) || scope.isJavaIoSerializable(castType)) {
						return true;
					}
					return false;
				}

				return false;
			}
			if (expressionType.isBaseType()) {
				return false;
			}
			if (expressionType.isClass()) {
				if (castType.isArrayType()) {
					// ---- (castTb.isArray) expressionTb.isClass -------
					if (scope.isJavaLangObject(expressionType)) {
						return true;
					} else {
						return false;
					}
				}
				if (castType.isClass()) { // ----- (castTb.isClass) expressionTb.isClass ------ 
					if (expressionType.isCompatibleWith(castType))
						return true;
					else {
						if (castType.isCompatibleWith(expressionType)) {
							return true;
						}
						return false;
					}
				}
				if (castType.isInterface()) {
					// ----- (castTb.isInterface) expressionTb.isClass -------  
					if (((ReferenceBinding) expressionType).isFinal()) {
						//no subclass for expressionTb, thus compile-time check is valid
						if (expressionType.isCompatibleWith(castType))
							return true;
						return false;
					} else {
						return true;
					}
				}

				return false;
			}
			if (expressionType.isInterface()) {
				if (castType.isArrayType()) {
					// ----- (castTb.isArray) expressionTb.isInterface ------
					if (scope.isJavaLangCloneable(expressionType)
						|| scope.isJavaIoSerializable(expressionType))
						//potential runtime error
						{
						return true;
					}
					return false;
				}
				if (castType.isClass()) {
					// ----- (castTb.isClass) expressionTb.isInterface --------
					if (scope.isJavaLangObject(castType))
						return true;
					if (((ReferenceBinding) castType).isFinal()) {
						//no subclass for castTb, thus compile-time check is valid
						if (castType.isCompatibleWith(expressionType)) {
							return true;
						}
						return false;
					}
					return true;
				}
				if (castType.isInterface()) {
					// ----- (castTb.isInterface) expressionTb.isInterface -------
					if ((Scope.compareTypes(castType, expressionType) == NotRelated)) {
						MethodBinding[] castTbMethods = ((ReferenceBinding) castType).methods();
						int castTbMethodsLength = castTbMethods.length;
						MethodBinding[] expressionTbMethods =
							((ReferenceBinding) expressionType).methods();
						int expressionTbMethodsLength = expressionTbMethods.length;
						for (int i = 0; i < castTbMethodsLength; i++) {
							for (int j = 0; j < expressionTbMethodsLength; j++) {
								if (castTbMethods[i].selector == expressionTbMethods[j].selector) {
									if (castTbMethods[i].returnType != expressionTbMethods[j].returnType) {
										if (castTbMethods[i].areParametersEqual(expressionTbMethods[j])) {
											return false;
										}
									}
								}
							}
						}
					}
					return true;
				}

				return false;
			} 

			return false;
		}
	}
	/**
	 * Code generation for instanceOfExpression
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
		expression.generateCode(currentScope, codeStream, true);
		codeStream.instance_of(type.resolvedType);
		if (!valueRequired)
			codeStream.pop();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		TypeBinding expressionType = expression.resolveType(scope);
		TypeBinding checkType = type.resolveType(scope);
		if (expressionType == null || checkType == null)
			return null;

		if (!areTypesCastCompatible(scope, checkType, expressionType)) {
			scope.problemReporter().notCompatibleTypesError(this, expressionType, checkType);
			return null;
		}
		this.resolvedType = BooleanBinding;
		return BooleanBinding;
	}

	public String toStringExpressionNoParenthesis() {

		return expression.toStringExpression() + " instanceof " + //$NON-NLS-1$
		type.toString(0);
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			expression.traverse(visitor, scope);
			type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
