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
import org.eclipse.jdt.internal.compiler.ast.Annotation;

/**
 * Parser specialized for decoding javadoc annotations
 */
public class AnnotationParser {

	Scanner scanner;
	Parser sourceParser;
	Annotation annotation;
	int startPosition, currentPosition;
	char currentCharacter;
	
	AnnotationParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		this.scanner = sourceParser.scanner;
	}

	/**
	 * Returns true if tag @deprecated is present in annotation
	 */
	public boolean checkDeprecation(int annotationStart, int annotationEnd) {
	
		char[] source = this.scanner.source;
		boolean foundDeprecated = false;
		if (false/*this.sourceParser.checkAnnotation*/) {
			this.annotation = new Annotation(annotationStart, annotationEnd);
		} else {
			this.annotation = null;
		} 
	
		int firstLineNumber = this.scanner.getLineNumber(annotationStart);
		int lastLineNumber = this.scanner.getLineNumber(annotationEnd);
		int[] index = new int[]{ 0 };
				
		// scan line per line, since tags are supposed to be at beginning of lines only
		nextLine: for (int line = firstLineNumber; line <= lastLineNumber; line++) {
			int lineStart = this.scanner.getLineStart(line);
			if (line == firstLineNumber) lineStart = annotationStart + 3; // skip leading /**
			int lineEnd = this.scanner.getLineEnd(line);
			if (line == lastLineNumber) lineEnd = annotationEnd - 2; // remove trailing */
			index[0] = lineStart;
			boolean foundStar = false;
			while (index[0] < lineEnd) {
				char nextCharacter = getNextCharacter(source, index); // consider unicodes
				switch(nextCharacter) {
					case '@' :
						if (this.annotation == null) {
							if ((getNextCharacter(source, index) == 'd')
								&& (getNextCharacter(source, index) == 'e')
								&& (getNextCharacter(source, index) == 'p')
								&& (getNextCharacter(source, index) == 'r')
								&& (getNextCharacter(source, index) == 'e')
								&& (getNextCharacter(source, index) == 'c')
								&& (getNextCharacter(source, index) == 'a')
								&& (getNextCharacter(source, index) == 't')
								&& (getNextCharacter(source, index) == 'e')
								&& (getNextCharacter(source, index) == 'd')) {
								// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
								nextCharacter = getNextCharacter(source, index);
								if (Character.isWhitespace(nextCharacter) || nextCharacter == '*') {
									foundDeprecated = true;
									break nextLine; // done
								}
							}
							continue nextLine;
						} 
/*
 						// parse annotation
						int tagStart = index[0], tagEnd = 0;
						try {
							saveScannerState();
							this.scanner.startPosition = index[0];
							this.scanner.currentPosition = index[0];
							this.scanner.currentCharacter = -1;
							// read tag
							parseTag();
						} finally {
							loadScannerState();
						}						
						do {
							nextCharacter = getNextCharacter(source, index);
							// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk
							if (Character.isWhitespace(nextCharacter) || nextCharacter == '*') break; 
							this.annotationBuffer[bufferLength++] = nextCharacter;
							tagEnd = index[0];
						} while (index[0] < lineEnd);
						if (bufferLength == 0) continue nextLine;
						char[] tag = null;
						switch(this.annotationBuffer[0]) {
							case 'd' :
								if (CharOperation.equals(Annotation.TAG_DEPRECATED, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_DEPRECATED;
									foundDeprecated = true;
								}
								break;
							case 'e' :
								if (CharOperation.equals(Annotation.TAG_EXCEPTION, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_EXCEPTION;
								}
								break;
							case 't' :
								if (CharOperation.equals(Annotation.TAG_THROWS, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_THROWS;
								}
								break;					
							case 's' :
								if (CharOperation.equals(Annotation.TAG_SEE, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_SEE;
								} else if (CharOperation.equals(Annotation.TAG_SINCE, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_SINCE;
								}
								break;					
							case 'p' :
								if (CharOperation.equals(Annotation.TAG_PARAM, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_PARAM;
								}
								break;					
							case 'r' :
								if (CharOperation.equals(Annotation.TAG_RETURN, this.annotationBuffer, 0, bufferLength)) {
									tag = Annotation.TAG_RETURN;
								}
								break;					
						}
						if (tag == null) {
							tag = new char[bufferLength];
							System.arraycopy(this.annotationBuffer, 0, tag, 0, bufferLength);
						}
						// read argument
						int argumentStart = index[0];
						while (argumentStart < lineEnd && Character.isWhitespace(source[argumentStart])) argumentStart++; // trim argument leading spaces
						int argumentEnd = lineEnd-1;
						while (argumentEnd > argumentStart && Character.isWhitespace(source[argumentEnd])) argumentEnd--; // trim argument trailing spaces
						bufferLength = 0;
						for (index[0] = argumentStart; index[0] <= argumentEnd;) {
							this.annotationBuffer[bufferLength++] = getNextCharacter(source, index);
						}					
						char[] argument = new char[bufferLength];
						System.arraycopy(this.annotationBuffer, 0, argument, 0, bufferLength);
						annotation.record(tag, argument, tagStart, argumentEnd);
*/						
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
		
		return foundDeprecated;
	}

	protected char getNextCharacter(char[] source, int[] index) {
		char nextCharacter = source[index[0]++];
		if (nextCharacter == '\\') {
				int c1, c2, c3, c4;
				index[0]++;
				while (source[index[0]] == 'u') index[0]++;
				if (!(((c1 = Character.getNumericValue(source[index[0]++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(source[index[0]++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(source[index[0]++])) > 15 || c3 < 0)
					|| ((c4 = Character.getNumericValue(source[index[0]++])) > 15 || c4 < 0))) {
						nextCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
				}
		}
		return nextCharacter;
	}

	/**
	 * Reuse scanner at same point, storing its positions before, and restoring them at the end
	 */
	char[] readIdentifier() {
	
		if (!Character.isJavaIdentifierStart(this.currentCharacter)) return null;
		char[] identifier = null;
		this.scanner.currentCharacter = this.currentCharacter;
		this.scanner.startPosition = this.startPosition;
		this.scanner.currentPosition = this.currentPosition;
		
		int token = this.scanner.scanIdentifierOrKeyword();		
		if (token == TerminalTokens.TokenNameIdentifier) {
			identifier = this.scanner.getCurrentIdentifierSource();
		}
		return identifier;
	}
	void saveScannerState() {
		this.currentCharacter = this.scanner.currentCharacter;
		this.startPosition = this.scanner.startPosition;
		this.currentPosition = this.scanner.currentPosition;
	}
	void loadScannerState() {
			this.scanner.currentCharacter = this.currentCharacter;
			this.scanner.startPosition = this.startPosition;
			this.scanner.currentPosition = this.currentPosition;
	}
}
