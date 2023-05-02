package org.eclipse.jdt.internal.compiler.ast;

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
//		char[] source = this.fragments[length].source;
//		for (int j = 0; j < source.length; j++) {
//			Util.appendEscapedChar(output, source[j], true);
//		}
		output.append('\"');
		return output;
	}
}
