package org.eclipse.jdt.internal.compiler.ast;

public abstract class MagicLiteral extends Literal {
	public MagicLiteral(int s, int e) {
		super(s, e);
	}

	public boolean isValidJavaStatement() {
		//should never be reach, but with a bug in the ast tree....
		//see comment on the Statement class

		return false;
	}

	/**
	 * source method comment.
	 */
	public char[] source() {
		return null;
	}

	public String toStringExpression() {

		return new String(source());
	}

}
