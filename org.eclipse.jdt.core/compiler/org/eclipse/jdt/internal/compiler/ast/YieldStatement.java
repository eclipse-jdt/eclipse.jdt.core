/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;


public class YieldStatement extends BranchStatement {

	public Expression expression;
	public SwitchExpression switchExpression;
	public boolean isImplicit; // dom ast should not use this field - to be used only by Parser.

	public YieldStatement(Expression exp, int sourceStart, int e) {
		super(null, sourceStart, e);
		this.expression = exp;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// PLACEHOLDER STUB
		return FlowInfo.DEAD_END;
	}

	@Override
	protected void generateExpressionResultCode(BlockScope currentScope, CodeStream codeStream) {
		// PLACEHOLDER STUB
	}

	@Override
	protected void adjustStackSize(BlockScope currentScope, CodeStream codeStream) {
		// PLACEHOLDER STUB
	}

	@Override
	public void resolve(BlockScope scope) {
		// PLACEHOLDER STUB
	}

	@Override
	public TypeBinding resolveExpressionType(BlockScope scope) {
		// PLACEHOLDER STUB
		return null;
	}

	@Override
	public StringBuffer printStatement(int tab, StringBuffer output) {
		printIndent(tab, output).append("yield"); //$NON-NLS-1$
		if (this.expression != null) {
			output.append(' ');
			this.expression.printExpression(tab, output);
		}
		return output.append(';');
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope blockscope) {
		// PLACEHOLDER STUB
	}

	@Override
	public boolean doesNotCompleteNormally() {
		return true;
	}
}
