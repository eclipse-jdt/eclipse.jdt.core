/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Parser specialized for decoding javadoc annotations
 */
public class AnnotationParser {

	// recognized tags
	public static final char[] TAG_DEPRECATED = "deprecated".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_PARAM = "param".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_RETURN = "return".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_THROWS = "throws".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_EXCEPTION = "exception".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SEE = "see".toCharArray(); //$NON-NLS-1$
	
	Scanner scanner;
	Parser sourceParser;
	Annotation annotation;
	int[] index = new int[]{ 0 };
	char[] source;
	
	int currentTokenType;
	
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	
	AnnotationParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		if (this.sourceParser.options.checkAnnotation) {
			this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null);
		}
		this.identifierStack = new char[30][];
		this.identifierLengthStack = new int[30];
		this.identifierPositionStack = new long[30];
	}

	/**
	 * Returns true if tag @deprecated is present in annotation.
	 * 
	 * If annotation checking is enabled, will also construct an Annotation node, which will
	 * be stored into Parser.annotation slot for being consumed later on.
	 */
	public boolean checkDeprecation(int annotationStart, int annotationEnd) {
	
		boolean foundDeprecated = false;
		try {
			this.source = this.sourceParser.scanner.source;
			if (this.sourceParser.options.checkAnnotation) {
				this.annotation = new Annotation(annotationStart, annotationEnd);
				this.identifierPtr = -1;
			} else {
				this.annotation = null;
			} 
		
			int firstLineNumber = this.sourceParser.scanner.getLineNumber(annotationStart);
			int lastLineNumber = this.sourceParser.scanner.getLineNumber(annotationEnd);
					
			// scan line per line, since tags must be at beginning of lines only
			nextLine: for (int line = firstLineNumber; line <= lastLineNumber; line++) {
				boolean foundStar = false;
				int lineStart = line == firstLineNumber 
						? annotationStart + 3 		// skip leading /**
						:  this.sourceParser.scanner.getLineStart(line);
				this.index[0] = lineStart;
				int lineEnd = line == lastLineNumber
						? annotationEnd - 2 		// remove trailing */
						:  this.sourceParser.scanner.getLineEnd(line);
				while (this.index[0] < lineEnd) {
					char nextCharacter = readChar(); // consider unicodes
					switch(nextCharacter) {
						case '@' :
							if (this.annotation == null) {
								if ((readChar() == 'd')
									&& (readChar() == 'e')
									&& (readChar() == 'p')
									&& (readChar() == 'r')
									&& (readChar() == 'e')
									&& (readChar() == 'c')
									&& (readChar() == 'a')
									&& (readChar() == 't')
									&& (readChar() == 'e')
									&& (readChar() == 'd')) {
									// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
									nextCharacter = readChar();
									if (Character.isWhitespace(nextCharacter) || nextCharacter == '*') {
										foundDeprecated = true;
										break nextLine; // done
									}
								}
								continue nextLine;
							} 
							this.scanner.resetTo(this.index[0], lineEnd);
							try {
								switch (this.scanner.getNextToken()) {
									case  TerminalTokens.TokenNameIdentifier :
										char[] tag = this.scanner.getCurrentIdentifierSource();
										if (CharOperation.equals(tag, TAG_DEPRECATED)) {
											foundDeprecated = true;
										} else if (CharOperation.equals(tag, TAG_PARAM)) {
											parseParam();
										} else if (CharOperation.equals(tag, TAG_THROWS) || CharOperation.equals(tag, TAG_EXCEPTION)) {
											parseThrows();
										} else if (CharOperation.equals(tag, TAG_SEE)) {
											parseSee();
										}
										break;
									case TerminalTokens.TokenNamereturn :
										parseReturn();
								}
							} catch (InvalidInputException e) {
	 							// ignore
							}
							continue nextLine;
						case '*' :
							if (foundStar) continue nextLine;
							foundStar = true;
							break;
						default :
							if (!CharOperation.isWhitespace(nextCharacter)) continue nextLine;
					}
				}
			}
		} finally {
			this.source = null; // release source as soon as finished
		}		
		return foundDeprecated;
	}

	char readChar() {

		char c = this.source[this.index[0]++];
		if (c == '\\') {
				int c1, c2, c3, c4;
				this.index[0]++;
				while (this.source[this.index[0]] == 'u') this.index[0]++;
				if (!(((c1 = Character.getNumericValue(this.source[this.index[0]++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(this.source[this.index[0]++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(this.source[this.index[0]++])) > 15 || c3 < 0)
					|| ((c4 = Character.getNumericValue(this.source[this.index[0]++])) > 15 || c4 < 0))) {
						c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
				}
		}
		return c;
	}
	
	int readToken() throws InvalidInputException {
		if (this.currentTokenType < 0) this.currentTokenType = this.scanner.getNextToken();
		return this.currentTokenType;
	}	
	
	/**
	 * Helper method reading one token, and consuming it if matching expectation.
	 */
	boolean readToken(int expectedTokenType) throws InvalidInputException {
		if (this.currentTokenType < 0) this.currentTokenType = this.scanner.getNextToken();
		if (this.currentTokenType == expectedTokenType) {
			this.consumeToken();
			return true;
		} 
		return false;
	}
	void consumeToken() {
		this.currentTokenType = -1; // flush token cache
	}

	void parseSingleName() throws InvalidInputException {
		if (readToken(TerminalTokens.TokenNameIdentifier)) {
			pushIdentifier();
			return;
		}
		throw new InvalidInputException();
		
	}
	void parseQualifiedName() throws InvalidInputException {
		nextToken: for (int iToken = 0;; iToken++) {
			int token = readToken();
			switch (token) {
				
				case TerminalTokens.TokenNameIdentifier :
					if ((iToken % 2) > 0) break nextToken; // identifiers must be odd tokens
					consumeToken();
					pushIdentifier();
					if (iToken > 0) {
						this.identifierLengthStack[--this.identifierLengthPtr]++; // name . ident
					}
					break;
					
				case TerminalTokens.TokenNameDOT :
					if ((iToken % 2) == 0) break nextToken; // dots must be even tokens
					consumeToken();
					break;
					
				default :
					break nextToken;
			}
		}
	}

	void parseParam() {
		// to be continued
	}
	
	void parseReturn() {
		// to be continued
	}
	
	void parseSee() {
		// to be continued
	}
	
	void parseThrows() {
		// to be continued
	}
	
	protected void pushIdentifier() {
		/*push the consumeToken on the identifier stack.
		Increase the total number of identifier in the stack.
		identifierPtr points on the next top */
	
		try {
			this.identifierStack[++this.identifierPtr] = this.scanner.getCurrentIdentifierSource();
			this.identifierPositionStack[this.identifierPtr] = 
				(((long) this.scanner.startPosition) << 32) + (this.scanner.currentPosition - 1); 
		} catch (IndexOutOfBoundsException e) {
			/*---stack reallaocation (identifierPtr is correct)---*/
			int oldStackLength = this.identifierStack.length;
			char[][] oldStack = this.identifierStack;
			this.identifierStack = new char[oldStackLength + 20][];
			System.arraycopy(oldStack, 0, this.identifierStack, 0, oldStackLength);
			this.identifierStack[this.identifierPtr] = this.scanner.getCurrentTokenSource();
			/*identifier position stack*/
			long[] oldPos = this.identifierPositionStack;
			this.identifierPositionStack = new long[oldStackLength + 20];
			System.arraycopy(oldPos, 0, this.identifierPositionStack, 0, oldStackLength);
			this.identifierPositionStack[this.identifierPtr] = 
				(((long) this.scanner.startPosition) << 32) + (this.scanner.currentPosition - 1); 
		}
	
		try {
			this.identifierLengthStack[++this.identifierLengthPtr] = 1;
		} catch (IndexOutOfBoundsException e) {
			/*---stack reallocation (identifierLengthPtr is correct)---*/
			int oldStackLength = this.identifierLengthStack.length;
			int oldStack[] = this.identifierLengthStack;
			this.identifierLengthStack = new int[oldStackLength + 10];
			System.arraycopy(oldStack, 0, this.identifierLengthStack, 0, oldStackLength);
			this.identifierLengthStack[this.identifierLengthPtr] = 1;
		}
	}
	
}
