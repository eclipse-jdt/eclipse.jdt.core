/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser.diagnose;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

public class LexStream implements TerminalTokens {
	public class Token{
		int kind;
		char[] name;
		int start;
		int end;
		int line;
		
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(name).append('[').append(kind).append(']');
			buffer.append('{').append(start).append(',').append(end).append('}').append(line);
			return buffer.toString();
		}

	}

	private int tokenCacheIndex;
	private int tokenCacheEOFIndex;
	private Token[] tokenCache;

	private int currentIndex = -1;

	private Scanner scanner;
	private int[] intervalStartToSkip;
	private int[] intervalEndToSkip;
	
	public LexStream(int size, Scanner scanner, int[] intervalStartToSkip, int[] intervalEndToSkip, int firstToken, int init, int eof) {
		this.tokenCache = new Token[size];
		this.tokenCacheIndex = 0;
		this.tokenCacheEOFIndex = Integer.MAX_VALUE;
		this.tokenCache[0] = new Token();
		this.tokenCache[0].kind = firstToken;
		this.tokenCache[0].name = CharOperation.NO_CHAR;
		this.tokenCache[0].start = init;
		this.tokenCache[0].end = init;
		this.tokenCache[0].line = 0;
		
		this.intervalStartToSkip = intervalStartToSkip;
		this.intervalEndToSkip = intervalEndToSkip;
		
		//scanner.recordLineSeparator = true;
		scanner.resetTo(init, eof);
		this.scanner = scanner;
	}
	
	private void readTokenFromScanner(){
		int length = tokenCache.length;
		boolean tokenNotFound = true;
		while(tokenNotFound) {
			try {
				int tokenKind =  scanner.getNextToken();
				if(tokenKind != TokenNameEOF) {
					int start = scanner.getCurrentTokenStartPosition();
					int end = scanner.getCurrentTokenEndPosition();
					if(!Util.isInInterval(start, end, intervalStartToSkip, intervalEndToSkip)) {
						Token token = new Token();
						token.kind = tokenKind;
						token.name = scanner.getCurrentTokenSource();
						token.start = start;
						token.end = end;
						token.line = scanner.getLineNumber(end);
						
						tokenCache[++tokenCacheIndex % length] = token;
						
						tokenNotFound = false;
					}
				} else {
					int start = scanner.getCurrentTokenStartPosition();
					int end = scanner.getCurrentTokenEndPosition();
					Token token = new Token();
					token.kind = tokenKind;
					token.name = CharOperation.NO_CHAR;
					token.start = start;
					token.end = end;
					token.line = scanner.getLineNumber(end);
					
					tokenCache[++tokenCacheIndex % length] = token;
					
					tokenCacheEOFIndex = tokenCacheIndex;
					tokenNotFound = false;
				}
			} catch (InvalidInputException e) {
				// return next token
			}
		}
	}
	
	public Token token(int index) {
		if(this.tokenCacheEOFIndex >= 0 && index > this.tokenCacheEOFIndex) {
			return token(this.tokenCacheEOFIndex);
		}
		int length = tokenCache.length;
		if(index > this.tokenCacheIndex) {
			int tokensToRead = index - this.tokenCacheIndex;
			while(tokensToRead-- != 0) {
				readTokenFromScanner();
			}
		} else if(this.tokenCacheIndex - length > index) {
			return null;
		}
		
		return tokenCache[index % length];
	}
	
	
	
	public int getToken() {
		return currentIndex = next(currentIndex);
	}
	
	public int previous(int tokenIndex) {
		return tokenIndex > 0 ? tokenIndex - 1 : 0;
	}

	public int next(int tokenIndex) {
		return tokenIndex < this.tokenCacheEOFIndex ? tokenIndex + 1 : this.tokenCacheEOFIndex;
	}

	public boolean afterEol(int i) {
		return i < 1 ? true : line(i - 1) < line(i);
	}
	
	public void reset() {
		currentIndex = -1;
	}
	
	public void reset(int i) {
		currentIndex = previous(i);
	}

	public int badtoken() {
		return 0;
	}

	public int kind(int tokenIndex) {
		return token(tokenIndex).kind;
	}
	
	public char[] name(int tokenIndex) {
		return token(tokenIndex).name;
	}

	public int line(int tokenIndex) {
		return token(tokenIndex).line;
	}
	
	public int start(int tokenIndex) {
		return token(tokenIndex).start;
	}
	
	public int end(int tokenIndex) {
		return token(tokenIndex).end;
	}
	
	public boolean isInsideStream(int index) {
		if(this.tokenCacheEOFIndex >= 0 && index > this.tokenCacheEOFIndex) {
			return false;
		} else if(index > this.tokenCacheIndex) {
			return true;
		} else if(this.tokenCacheIndex - tokenCache.length > index) {
			return false;
		} else {
			return true;
		}
	}
}
