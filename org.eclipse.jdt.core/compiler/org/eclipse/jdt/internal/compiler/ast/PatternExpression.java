/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class PatternExpression extends Expression {

	public Pattern pattern;

	public PatternExpression(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		return this.printExpression(indent, output);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return this.pattern != null ? this.pattern.print(indent, output) : output;
	}
	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		// do nothing by default
	}
}