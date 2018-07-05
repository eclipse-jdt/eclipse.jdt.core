/*******************************************************************************
 * Copyright (c) 2018 GK Software SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

public class SwitchExpression extends Expression {

	public Expression expression;
	public Statement[] caseExpressions; // Sequence of CaseStatement & BreakExpression
	public CaseStatement[] caseLabels;
	
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("switch ("); //$NON-NLS-1$
		this.expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		if (this.caseExpressions != null) {
			for (int i = 0; i < this.caseExpressions.length; i++) {
				output.append('\n');
				Statement statement = this.caseExpressions[i];
				statement.printStatement(statement instanceof CaseStatement ? indent : indent+2, output);
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}
