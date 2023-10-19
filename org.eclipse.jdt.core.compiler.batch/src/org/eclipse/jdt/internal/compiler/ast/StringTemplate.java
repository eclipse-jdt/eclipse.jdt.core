/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.Util;

public class StringTemplate extends Expression {
	private StringLiteral[] fragments;
	private Expression[] values;
	public StringTemplate(StringLiteral[] fragments, Expression[] values) {
		this.fragments = fragments;
		this.values = values;
	}

    public StringLiteral[] fragments() {
		return this.fragments;
    }
    public Expression[] values() {
		return this.values;
    }
	@Override
	public void resolve(BlockScope scope) {
		if (this.constant != Constant.NotAConstant) {
			this.constant = Constant.NotAConstant;
		}
		for (StringLiteral frag : this.fragments) {
			frag.resolveType(scope);
		}
		for (Expression ex : this.values) {
			ex.resolveType(scope);
		}
	}
	private void generateNewTemplateBootstrap(CodeStream codeStream) {
		int index = codeStream.classFile.recordBootstrapMethod(this);
		StringBuilder signature = new StringBuilder("("); //$NON-NLS-1$
		for(int i = 0; i < this.values.length; i++) {
			signature.append(this.values[i].resolvedType.signature());
		}
		signature.append(")Ljava/lang/StringTemplate;"); //$NON-NLS-1$
		codeStream.invokeDynamic(index,
				2, //
				1, // int
				ConstantPool.PROCESS,
				signature.toString().toCharArray(),
				TypeIds.T_int,
				TypeBinding.INT);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		for (Expression exp : this.values) {
			exp.generateCode(currentScope, codeStream, valueRequired);
		}

		generateNewTemplateBootstrap(codeStream);
		int pc = codeStream.position;
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		int length = this.values.length;
		output.append('\"');
		for (int i = 0; i < length; i++) {
			char[] source = this.fragments[i].source;
			for (int j = 0; j < source.length; j++) {
				Util.appendEscapedChar(output, source[j], true);
			}
			output.append("\\{"); //$NON-NLS-1$
			this.values[i].printExpression(0, output);
			output.append("}"); //$NON-NLS-1$
		}
		output.append('\"');
		return output;
	}
}
