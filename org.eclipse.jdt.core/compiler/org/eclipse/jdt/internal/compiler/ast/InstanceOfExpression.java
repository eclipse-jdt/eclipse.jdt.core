/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
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
	/**
	 * Returns false if the instanceof unnecessary
	 */
	public final boolean checkCastTypesCompatibility(
		BlockScope scope,
		TypeBinding castType,
		TypeBinding expressionType) {
	
		//A more complete version of this method is provided on
		//CastExpression (it deals with constant and need runtime checkcast)

		if (castType == expressionType) return false;
		
		//by grammatical construction, the base type check is not necessary

		if (castType == null || expressionType == null) return true;
	
		//-----------cast to something which is NOT a base type--------------------------	
		if (expressionType == NullBinding) {
			//	if (castType.isArrayType()){ // 26903 - need checkcast when casting null to array type
			//		needRuntimeCheckcast = true;
			//	}
			return false; //null is compatible with every thing
		}
		if (expressionType.isBaseType()) {
			scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
			return true;
		}
	
		if (expressionType.isArrayType()) {
			if (castType == expressionType) return false; // identity conversion
	
			if (castType.isArrayType()) {
				//------- (castType.isArray) expressionType.isArray -----------
				TypeBinding exprElementType = ((ArrayBinding) expressionType).elementsType(scope);
				if (exprElementType.isBaseType()) {
					// <---stop the recursion------- 
					if (((ArrayBinding) castType).elementsType(scope) != exprElementType)
						scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
					return true;
				}
				// recursively on the elements...
				return checkCastTypesCompatibility(
					scope,
					((ArrayBinding) castType).elementsType(scope),
					exprElementType);
			} else if (
				castType.isClass()) {
				//------(castType.isClass) expressionType.isArray ---------------	
				if (castType.id == T_Object) {
					return false;
				}
			} else { //------- (castType.isInterface) expressionType.isArray -----------
				if (castType.id == T_JavaLangCloneable || castType.id == T_JavaIoSerializable) {
					return true;
				}
			}
			scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
			return true;
		}
	
		if (expressionType.isClass()) {
			if (castType.isArrayType()) {
				// ---- (castType.isArray) expressionType.isClass -------
				if (expressionType.id == T_Object) { // potential runtime error
					return true;
				}
			} else if (castType.isClass()) { // ----- (castType.isClass) expressionType.isClass ------
				if (expressionType.isCompatibleWith(castType)){ // no runtime error
					return false;
				}
				if (castType.isCompatibleWith(expressionType)) {
					// potential runtime  error
					return true;
				}
			} else { // ----- (castType.isInterface) expressionType.isClass -------  
				if (expressionType.isCompatibleWith(castType)) 
					return false;
				if (!((ReferenceBinding) expressionType).isFinal()) {
				    // a subclass may implement the interface ==> no check at compile time
					return true;
				}
				// no subclass for expressionType, thus compile-time check is valid
			}
			scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
			return true;
		}
	
		//	if (expressionType.isInterface()) { cannot be anything else
		if (castType.isArrayType()) {
			// ----- (castType.isArray) expressionType.isInterface ------
			if (!(expressionType.id == T_JavaLangCloneable
					|| expressionType.id == T_JavaIoSerializable)) {// potential runtime error
				scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
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
					scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
					return true;
				}
			}
		} else { // ----- (castType.isInterface) expressionType.isInterface -------
			if (expressionType.isCompatibleWith(castType)) { 
				return false;
			}
			if (!castType.isCompatibleWith(expressionType)) {
				MethodBinding[] castTypeMethods = ((ReferenceBinding) castType).methods();
				MethodBinding[] expressionTypeMethods =
					((ReferenceBinding) expressionType).methods();
				int exprMethodsLength = expressionTypeMethods.length;
				for (int i = 0, castMethodsLength = castTypeMethods.length; i < castMethodsLength; i++)
					for (int j = 0; j < exprMethodsLength; j++) {
						if ((castTypeMethods[i].returnType != expressionTypeMethods[j].returnType)
								&& CharOperation.equals(castTypeMethods[i].selector, expressionTypeMethods[j].selector)
								&& castTypeMethods[i].areParametersEqual(expressionTypeMethods[j])) {
							scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
						}
					}
			}
		}
		return true;
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

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		expression.printExpression(indent, output).append(" instanceof "); //$NON-NLS-1$
		return type.print(0, output);
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		TypeBinding expressionType = expression.resolveType(scope);
		TypeBinding checkType = type.resolveType(scope);
		if (expressionType == null || checkType == null)
			return null;

		boolean necessary = checkCastTypesCompatibility(scope, checkType, expressionType);
		if (!necessary) {
			scope.problemReporter().unnecessaryInstanceof(this, checkType);
		}
		return this.resolvedType = BooleanBinding;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			expression.traverse(visitor, scope);
			type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
