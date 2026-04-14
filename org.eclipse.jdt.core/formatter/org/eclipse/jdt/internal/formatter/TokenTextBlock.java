package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.compiler.parser.TerminalToken;

public class TokenTextBlock extends Token {

	private boolean hasReplace = false;

	public TokenTextBlock(int sourceStart, int sourceEnd, TerminalToken tokenType) {
		super(sourceStart, sourceEnd, tokenType);
	}

	public boolean hasReplace() {
		return this.hasReplace;
	}

	public void addNewlineReplace() {
		this.hasReplace = true;
	}
}
