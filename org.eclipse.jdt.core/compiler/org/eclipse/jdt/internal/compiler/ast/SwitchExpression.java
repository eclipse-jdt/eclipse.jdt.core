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

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class SwitchExpression extends Expression implements IPolyExpression {

	public Expression expression;
	public SwitchExprArm[] exprArms;
	
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		// TODO
	}
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("switch ("); //$NON-NLS-1$
		this.expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		for (SwitchExprArm arm : this.exprArms)
			arm.print(0, output);
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}