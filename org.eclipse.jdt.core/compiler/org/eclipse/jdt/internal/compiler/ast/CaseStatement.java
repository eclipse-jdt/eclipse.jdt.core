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

public class CaseStatement extends Statement {
	
	public Expression constantExpression;
	public CaseLabel targetLabel;
	public CaseStatement(Expression constantExpression, int sourceEnd, int sourceStart) {
		this.constantExpression = constantExpression;
		this.sourceEnd = sourceEnd;
		this.sourceStart = sourceStart;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		if (constantExpression != null) {
			if (constantExpression.constant == NotAConstant) {
				currentScope.problemReporter().caseExpressionMustBeConstant(constantExpression);
			}
			this.constantExpression.analyseCode(currentScope, flowContext, flowInfo);
		}
		return flowInfo;
	}

	/**
	 * Case code generation
	 *
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		targetLabel.place();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * No-op : should use resolveCase(...) instead.
	 */
	public void resolve(BlockScope scope) {
	}

	public Constant resolveCase(
		BlockScope scope,
		TypeBinding switchType,
		SwitchStatement switchStatement) {

		if (constantExpression == null) {
			// remember the default case into the associated switch statement
			if (switchStatement.defaultCase != null)
				scope.problemReporter().duplicateDefaultCase(this);
	
			// on error the last default will be the selected one .... (why not) ....	
			switchStatement.defaultCase = this;
			return null;
		}
		// add into the collection of cases of the associated switch statement
		switchStatement.cases[switchStatement.caseCount++] = this;
		TypeBinding caseType = constantExpression.resolveType(scope);
		if (caseType == null || switchType == null) return null;
		if (constantExpression.isConstantValueOfTypeAssignableToType(caseType, switchType))
			return constantExpression.constant;
		if (caseType.isCompatibleWith(switchType))
			return constantExpression.constant;
		scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
			constantExpression,
			caseType,
			switchType);
		return null;
	}

	public String toString(int tab) {

		String s = tabString(tab);
		if (constantExpression == null) {
			s = s + "default : "; //$NON-NLS-1$
		} else {
		s = s + "case " + constantExpression.toStringExpression() + " : "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			if (constantExpression != null) constantExpression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
