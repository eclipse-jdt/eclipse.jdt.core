/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

public class StringLiteral extends Literal {

	private char[] source;
	private final int lineNumber;

	public StringLiteral(char[] token, int start, int end, int lineNumber) {
		super(start, end);
		this.source = token;
		this.lineNumber = lineNumber - 1; // line number is 1 based
	}

	public StringLiteral(int s, int e) {
		this(null, s, e, 1);
	}

	@Override
	public void computeConstant() {
		this.constant = StringConstant.fromValue(String.valueOf(this.source));
	}

	public ExtendedStringLiteral extendWith(CharLiteral lit) {
		return new ExtendedStringLiteral(append(this.source(), new char[] { lit.value }),
				this.sourceStart, lit.sourceEnd, this.getLineNumber() + 1);
	}

	public ExtendedStringLiteral extendWith(StringLiteral lit) {
		return new ExtendedStringLiteral(append(this.source(), lit.source()), this.sourceStart,
				lit.sourceEnd, this.getLineNumber() + 1);
	}
	protected static char[] append(char[] source, char[] source2) {
		char[] result = Arrays.copyOfRange(source, 0, source.length + source2.length);
		System.arraycopy(source2, 0, result, source.length, source2.length);
		return result;
	}


	/**
	 * Add the lit source to mine, just as if it was mine
	 */
	public StringLiteralConcatenation extendsWith(StringLiteral lit) {
		return new StringLiteralConcatenation(this, lit);
	}

	/**
	 * Code generation for string literal
	 */
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			codeStream.ldc(this.constant.stringValue());
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	@Override
	public TypeBinding literalType(BlockScope scope) {
		return scope.getJavaLangString();
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		// handle some special char.....
		output.append('\"');
		for (char s : source()) {
			Util.appendEscapedChar(output, s, true);
		}
		output.append('\"');
		return output;
	}

	@Override
	public char[] source() {
		return Arrays.copyOf(this.source, this.source.length);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	public void setSource(char[] source) {
		this.source = source;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}
}
