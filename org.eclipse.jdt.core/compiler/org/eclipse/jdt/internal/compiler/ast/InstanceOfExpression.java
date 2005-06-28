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

		flowInfo = expression
			.analyseCode(currentScope, flowContext, flowInfo)
			.unconditionalInits();
		expression.checkNullStatus(currentScope, flowContext, flowInfo, FlowInfo.NON_NULL);
		return flowInfo;
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
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		} else {
			codeStream.pop();
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		expression.printExpression(indent, output).append(" instanceof "); //$NON-NLS-1$
		return type.print(0, output);
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		TypeBinding expressionType = expression.resolveType(scope);
		TypeBinding checkedType = type.resolveType(scope, true /* check bounds*/);
		if (expressionType == null || checkedType == null)
			return null;

		if (checkedType.isTypeVariable() || checkedType.isBoundParameterizedType() || checkedType.isGenericType()) {
			scope.problemReporter().illegalInstanceOfGenericType(checkedType, this);
		} else {
			boolean isLegal = checkCastTypesCompatibility(scope, checkedType, expressionType, null);
			if (!isLegal) {
				scope.problemReporter().notCompatibleTypesError(this, expressionType, checkedType);
			}
		}
		return this.resolvedType = BooleanBinding;
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#tagAsUnnecessaryCast(Scope,TypeBinding)
	 */
	public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
		// null is not instanceof Type, recognize direct scenario
		if (expression.resolvedType != NullBinding)
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
