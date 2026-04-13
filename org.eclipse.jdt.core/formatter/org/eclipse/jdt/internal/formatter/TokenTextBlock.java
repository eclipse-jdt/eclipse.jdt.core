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

/*	@Override
	public String toString(String source) {
		// Should I assume is a textBlock?
		int i = 0;
		StringBuilder sb = new StringBuilder();
		sb.append(source.substring(this.originalStart, this.originalStart + 2));
		sb.append(source.substring(this.originalStart + 3, this.originalEnd-3));
		if(source.charAt(this.originalEnd-3) != '\n') {
			sb.append('\n');
			i++;
			sb.append(source.substring(this.originalEnd-3, this.originalEnd));
		}
		return sb.toString();
		//return source.substring(this.originalStart, this.originalEnd + 1);
	}*/

}
