/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public class ForEachStatementWithRecordPattern extends ForeachStatement {

	public static final char[] SecretRecordPatternVariableName = " recordPatternVar".toCharArray(); //$NON-NLS-1$

	public RecordPattern recordPattern;


	public ForEachStatementWithRecordPattern(RecordPattern recordPattern, int start) {
		super(new LocalDeclaration(
					ForEachStatementWithRecordPattern.SecretRecordPatternVariableName, 0, 0),
					start);
		this.recordPattern = recordPattern;
		this.elementVariable.type = this.recordPattern.type;
	}

	public void actionTransform() {
		this.action = createSyntheticBody(this.action);
	}
	private Statement createSyntheticBody(Statement action1) {
		if (action1 == null || this.recordPattern == null)
			return null;

		SwitchStatement switchStatement = new SwitchStatement();
		switchStatement.containsPatterns = true;
		switchStatement.containsNull = true;
		switchStatement.expression = new SingleNameReference(this.elementVariable.name, 0);

		List<Statement> stmts = new ArrayList<>();

		stmts.add(new CaseStatement(this.recordPattern, 0, 0));
		stmts.add(this.action);
		stmts.add(new BreakStatement(null, 0, 0));

		stmts.add(new CaseStatement(0, 0, new Expression[] {new NullLiteral(0, 0), new FakeDefaultLiteral(0, 0)}));

//		AllocationExpression allocationExpression = new AllocationExpression();
//		allocationExpression.type = new SingleTypeReference("NullPointerException".toCharArray(), 0); //$NON-NLS-1$;
//		AllocationExpression tmp = new AllocationExpression();
//		tmp.arguments = new Expression[2];
//		tmp.arguments[0] = new NullLiteral(0, 0);
//		tmp.arguments[1] = allocationExpression;
//		tmp.type = new SingleTypeReference("MatchException".toCharArray(), 0); //$NON-NLS-1$;

		//TODO: Need to enable MatchException
		AllocationExpression allocationExpression = new AllocationExpression();
		allocationExpression.type = new SingleTypeReference("NullPointerException".toCharArray(), 0); //$NON-NLS-1$;
		stmts.add(new ThrowStatement(allocationExpression , 0, 0));

		switchStatement.statements = stmts.toArray(new Statement[0]);

		return switchStatement;
	}

	@Override
	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output).append("for ("); //$NON-NLS-1$
		this.recordPattern.printExpression(0, output);
		output.append(" : ");//$NON-NLS-1$
		if (this.collection != null) {
			this.collection.print(0, output).append(") "); //$NON-NLS-1$
		} else {
			output.append(')');
		}
		//block
		if (this.action == null) {
			output.append(';');
		} else {
			output.append('\n');
			this.action.printStatement(indent + 1, output);
		}
		return output;
	}


	@Override
	public void resolve(BlockScope upperScope) {
		super.resolve(upperScope);
		if (containsPatternVariable()) {
			LocalVariableBinding[] variables = this.recordPattern.getPatternVariablesWhenTrue();
			if (variables != null)
				this.addPatternVariablesWhenTrue(variables);
		}
	}
	@Override
	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.elementVariable.traverse(visitor, this.scope);
			this.recordPattern.traverse(visitor, blockScope);
			if (this.collection != null) {
				this.collection.traverse(visitor, this.scope);
			}
			if (this.action != null) {
				this.action.traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}

	@Override
	public boolean doesNotCompleteNormally() {
		return false; // may not be entered at all.
	}

	@Override
	public boolean canCompleteNormally() {
		return true;
	}
	@Override
	public boolean containsPatternVariable() {
		return this.recordPattern.containsPatternVariable();
	}

}
