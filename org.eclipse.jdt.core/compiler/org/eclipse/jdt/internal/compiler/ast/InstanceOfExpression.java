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
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#reportIllegalCast(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
	 */
	public void reportIllegalCast(Scope scope, TypeBinding castType, TypeBinding expressionType) {
		scope.problemReporter().notCompatibleTypesError(this, expressionType, castType);
	}
	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		TypeBinding expressionType = expression.resolveType(scope);
		TypeBinding checkedType = type.resolveType(scope);
		if (expressionType == null || checkedType == null)
			return null;

		if (checkedType.isTypeVariable() || checkedType.isParameterizedType() || checkedType.isGenericType()) {
			scope.problemReporter().illegalInstanceOfGenericType(checkedType, this);
		} else {
			checkCastTypesCompatibility(scope, checkedType, expressionType, null);
		}
		return this.resolvedType = BooleanBinding;
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#tagAsUnnecessaryCast(Scope,TypeBinding)
	 */
	public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
		scope.problemReporter().unnecessaryInstanceof(this, castType);
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			expression.traverse(visitor, scope);
			type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
