/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * Flatten string literal
 */
public class StringLiteralConcatenation extends StringLiteral {
	private static final int INITIAL_SIZE = 5;
	private final StringLiteral[] literals;
	private final int counter;

	/**
	 * Build a two-strings literal
	 */
	public StringLiteralConcatenation(StringLiteral str1, StringLiteral str2) {
		super(StringLiteral.append(str1.source(), str2.source()), str1.sourceStart, str2.sourceEnd,
				str1.getLineNumber() + 1);
		if (str1 instanceof StringLiteralConcatenation s1) {
			this.literals = Arrays.copyOf(s1.literals, s1.literals.length + 1);
			this.counter = s1.counter + 1;
		} else {
			this.literals = new StringLiteral[INITIAL_SIZE];
			this.literals[0] = str1;
			this.counter = 2;
		}
		this.literals[this.counter - 1] = str2;
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append("StringLiteralConcatenation{"); //$NON-NLS-1$
		for (StringLiteral lit : getLiterals()) {
			lit.printExpression(indent, output);
			output.append("+\n");//$NON-NLS-1$
		}
		return output.append('}');
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			for (StringLiteral lit : getLiterals()) {
				lit.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	public StringLiteral[] getLiterals() {
		return Arrays.copyOf(this.literals, this.counter);
	}
}
