/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Parser specialized for decoding javadoc comments
 */
public abstract class AbstractCommentParser {

	// recognized tags
	public static final char[] TAG_DEPRECATED = "deprecated".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_PARAM = "param".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_RETURN = "return".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_THROWS = "throws".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_EXCEPTION = "exception".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SEE = "see".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LINK = "link".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LINKPLAIN = "linkplain".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_INHERITDOC = "inheritDoc".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_VALUE = "value".toCharArray(); //$NON-NLS-1$

	// tags value
	public static final int NO_TAG_VALUE = 0;
	public static final int TAG_DEPRECATED_VALUE = 1;
	public static final int TAG_PARAM_VALUE = 2;
	public static final int TAG_RETURN_VALUE = 3;
	public static final int TAG_THROWS_VALUE = 4;
	public static final int TAG_EXCEPTION_VALUE = 5;
	public static final int TAG_SEE_VALUE = 6;
	public static final int TAG_LINK_VALUE = 7;
	public static final int TAG_LINKPLAIN_VALUE = 8;
	public static final int TAG_INHERITDOC_VALUE = 9;
	public static final int TAG_VALUE_VALUE = 10;
	public static final int TAG_OTHERS_VALUE = 11;
	protected int tagValue = NO_TAG_VALUE;
	
	// tags expected positions
	public final static int ORDERED_TAGS_NUMBER = 3;
	public final static int PARAM_TAG_EXPECTED_ORDER = 0;
	public final static int THROWS_TAG_EXPECTED_ORDER = 1;
	public final static int SEE_TAG_EXPECTED_ORDER = 2;
	
	// Kind of comment parser
	public final static int COMPIL_PARSER = 0x00000001;
	public final static int DOM_PARSER = 0x00000002;
	
	// Public fields
	public Scanner scanner;
	public boolean checkDocComment = false;
	
	// Protected fields
	protected boolean inherited, deprecated;
	protected char[] source;
	protected int index, endComment, lineEnd;
	protected int tokenPreviousPosition, lastIdentifierEndPosition, starPosition;
	protected int textStart, memberStart;
	protected int tagSourceStart, tagSourceEnd;
	protected int inlineTagStart;
	protected Parser sourceParser;
	protected Object returnStatement;
	protected boolean lineStarted = false, inlineTagStarted = false;
	protected int kind;
	protected int[] lineEnds;
	
	// Private fields
	private int currentTokenType = -1;
	
	// Line pointers
	private int linePtr, lastLinePtr;
	
	// Identifier stack
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	// Ast stack
	protected static int AstStackIncrement = 10;
	protected int astPtr;
	protected Object[] astStack;
	protected int astLengthPtr;
	protected int[] astLengthStack;

	protected AbstractCommentParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null, true/*taskCaseSensitive*/);
		this.identifierStack = new char[20][];
		this.identifierPositionStack = new long[20];
		this.identifierLengthStack = new int[10];
		this.astStack = new Object[30];
		this.astLengthStack = new int[20];
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in javadoc comment.
	 * 
	 * If javadoc checking is enabled, will also construct an Javadoc node, which will be stored into Parser.javadoc
	 * slot for being consumed later on.
	 */
	protected boolean commentParse(int javadocStart, int javadocEnd) {

		boolean validComment = true;
		try {
			// Init scanner position
			this.scanner.resetTo(javadocStart, javadocEnd);
			this.endComment = javadocEnd;
			this.index = javadocStart;
			readChar(); // starting '/'
			int previousPosition = this.index;
			readChar(); // first '*'
			char nextCharacter= readChar(); // second '*'
			
			// Init local variables
			this.astLengthPtr = -1;
			this.astPtr = -1;
			this.currentTokenType = -1;
			this.inlineTagStarted = false;
			this.inlineTagStart = -1;
			this.lineStarted = false;
			this.returnStatement = null;
			this.inherited = false;
			this.deprecated = false;
			this.linePtr = getLineNumber(javadocStart);
			this.lastLinePtr = getLineNumber(javadocEnd);
			this.lineEnd = (this.linePtr == this.lastLinePtr) ? this.endComment : this.scanner.getLineEnd(this.linePtr);
			this.textStart = -1;
			char previousChar = 0;
			int invalidTagLineEnd = -1;
			int invalidInlineTagLineEnd = -1;
			
			// Loop on each comment character
			while (this.index < this.endComment) {
				previousPosition = this.index;
				previousChar = nextCharacter;
				
				// Calculate line end (cannot use this.scanner.linePtr as scanner does not parse line ends again)
				if (this.index > (this.lineEnd+1)) {
					updateLineEnd();
				}
				
				// Read next char only if token was consumed
				if (this.currentTokenType < 0) {
					nextCharacter = readChar(); // consider unicodes
				} else {
					previousPosition = this.scanner.getCurrentTokenStartPosition();
					switch (this.currentTokenType) {
						case TerminalTokens.TokenNameRBRACE:
							nextCharacter = '}';
							break;
						case TerminalTokens.TokenNameMULTIPLY:
							nextCharacter = '*';
							break;
					default:
							nextCharacter = this.scanner.currentCharacter;
					}
					consumeToken();
				}
			
				if (this.index >= this.endComment) {
					break;
				}
				
				switch (nextCharacter) {
					case '@' :
						// Start tag parsing only if we are on line beginning or at inline tag beginning
						if ((!this.lineStarted || previousChar == '{')) {
							this.lineStarted = true;
							if (this.inlineTagStarted) {
								this.inlineTagStarted = false;
								// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53279
								// Cannot have @ inside inline comment
								if (this.sourceParser != null) {
									int end = previousPosition<invalidInlineTagLineEnd ? previousPosition : invalidInlineTagLineEnd;
									this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, end);
								}
								validComment = false;
								if (this.lineStarted && this.textStart != -1 && this.textStart < previousPosition) {
									pushText(this.textStart, previousPosition);
								}
								if (this.kind == DOM_PARSER) refreshInlineTagPosition(previousPosition);
							}
							if (previousChar == '{') {
								if (this.textStart != -1 && this.textStart < this.inlineTagStart) {
									pushText(this.textStart, this.inlineTagStart);
								}
								this.inlineTagStarted = true;
								invalidInlineTagLineEnd = this.lineEnd;
							} else if (this.textStart != -1 && this.textStart < invalidTagLineEnd) {
								pushText(this.textStart, invalidTagLineEnd);
							}
							this.scanner.resetTo(this.index, this.endComment);
							this.currentTokenType = -1; // flush token cache at line begin
							try {
								if (!parseTag(previousPosition)) {
									// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
									// do not stop the inline tag when error is encountered to get text after
									validComment = false;
									// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
									// for DOM AST node, store tag as text in case of invalid syntax
									if (this.kind == DOM_PARSER) {
										createTag();
										this.textStart = this.tagSourceEnd+1;
										invalidTagLineEnd  = this.lineEnd;
									}
								}
							} catch (InvalidInputException e) {
								consumeToken();
							}
						}
						break;
					case '\r':
					case '\n':
						if (this.lineStarted && this.textStart < previousPosition) {
							pushText(this.textStart, previousPosition);
						}
						this.lineStarted = false;
						// Fix bug 51650
						this.textStart = -1;
						break;
					case '}' :
						if (this.inlineTagStarted) {
							if (this.lineStarted && this.textStart != -1 && this.textStart < previousPosition) {
								pushText(this.textStart, previousPosition);
							}
							if (this.kind == DOM_PARSER) refreshInlineTagPosition(previousPosition);
							this.textStart = this.index;
							this.inlineTagStarted = false;
						} else {
							if (!this.lineStarted) {
								this.textStart = previousPosition;
							}
						}
						this.lineStarted = true;
						break;
					case '{' :
						if (this.inlineTagStarted) {
							this.inlineTagStarted = false;
							// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53279
							// Cannot have opening brace in inline comment
							if (this.sourceParser != null) {
								int end = previousPosition<invalidInlineTagLineEnd ? previousPosition : invalidInlineTagLineEnd;
								this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, end);
							}
							if (this.lineStarted && this.textStart != -1 && this.textStart < previousPosition) {
								pushText(this.textStart, previousPosition);
							}
							if (this.kind == DOM_PARSER) refreshInlineTagPosition(previousPosition);
						}
						if (!this.lineStarted) {
							this.textStart = previousPosition;
						}
						this.lineStarted = true;
						this.inlineTagStart = previousPosition;
						break;
					case '*' :
					case '\u000c' :	/* FORM FEED               */
					case ' ' :			/* SPACE                   */
					case '\t' :			/* HORIZONTAL TABULATION   */
						// do nothing for space or '*' characters
						break;
					default :
						if (!this.lineStarted) {
							this.textStart = previousPosition;
						}
						this.lineStarted = true;
						break;
				}
			}
			// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53279
			// Cannot leave comment inside inline comment
			if (this.inlineTagStarted) {
				this.inlineTagStarted = false;
				if (this.sourceParser != null) {
					int end = previousPosition<invalidInlineTagLineEnd ? previousPosition : invalidInlineTagLineEnd;
					if (this.index >= this.endComment) end = invalidInlineTagLineEnd;
					this.sourceParser.problemReporter().javadocUnterminatedInlineTag(this.inlineTagStart, end);
				}
				if (this.lineStarted && this.textStart != -1 && this.textStart < previousPosition) {
					pushText(this.textStart, previousPosition);
				}
				if (this.kind == DOM_PARSER) {
					refreshInlineTagPosition(previousPosition);
				}
			} else if (this.lineStarted && this.textStart < previousPosition) {
				pushText(this.textStart, previousPosition);
			}
			updateDocComment();
		} catch (Exception ex) {
			validComment = false;
		}
		return validComment;
	}

	private void consumeToken() {
		this.currentTokenType = -1; // flush token cache
		updateLineEnd();
	}

	protected abstract Object createArgumentReference(char[] name, int dim, Object typeRef, long[] dimPos, long argNamePos) throws InvalidInputException;
	protected abstract Object createFieldReference(Object receiver) throws InvalidInputException;
	protected abstract Object createMethodReference(Object receiver, List arguments) throws InvalidInputException;
	protected Object createReturnStatement() { return null; }
	protected abstract void createTag();
	protected abstract Object createTypeReference(int primitiveToken);
	
	private int getIndexPosition() {
		if (this.index > this.lineEnd) {
			return this.lineEnd;
		} else {
			return this.index-1;
		}
	}

	/**
	 * Search the line number corresponding to a specific position.
	 * Warning: returned position is 1-based index!
	 * @see Scanner#getLineNumber(int) We cannot directly use this method
	 * when linePtr field is not initialized.
	 */
	private int getLineNumber(int position) {
	
		if (this.scanner.linePtr != -1) {
			return this.scanner.getLineNumber(position);
		}
		if (this.lineEnds == null)
			return 1;
		int length = this.lineEnds.length;
		if (length == 0)
			return 1;
		int g = 0, d = length - 1;
		int m = 0;
		while (g <= d) {
			m = (g + d) /2;
			if (position < this.lineEnds[m]) {
				d = m-1;
			} else if (position > this.lineEnds[m]) {
				g = m+1;
			} else {
				return m + 1;
			}
		}
		if (position < this.lineEnds[m]) {
			return m+1;
		}
		return m+2;
	}

	private int getTokenEndPosition() {
		if (this.scanner.getCurrentTokenEndPosition() > this.lineEnd) {
			return this.lineEnd;
		} else {
			return this.scanner.getCurrentTokenEndPosition();
		}
	}
	
	/*
	 * Parse argument in @see tag method reference
	 */
	private Object parseArguments(Object receiver) throws InvalidInputException {

		// Init
		int modulo = 0; // should be 2 for (Type,Type,...) or 3 for (Type arg,Type arg,...)
		int iToken = 0;
		char[] argName = null;
		List arguments = new ArrayList(10);
		int start = this.scanner.getCurrentTokenStartPosition();
		
		// Parse arguments declaration if method reference
		nextArg : while (this.index < this.scanner.eofPosition) {

			// Read argument type reference
			Object typeRef;
			try {
				typeRef = parseQualifiedName(false);
			} catch (InvalidInputException e) {
				break nextArg;
			}
			boolean firstArg = modulo == 0;
			if (firstArg) { // verify position
				if (iToken != 0)
					break nextArg;
			} else if ((iToken % modulo) != 0) {
					break nextArg;
			}
			if (typeRef == null) {
				if (firstArg && this.currentTokenType == TerminalTokens.TokenNameRPAREN) {
					// verify characters after arguments declaration (expecting white space or end comment)
					if (!verifySpaceOrEndComment()) {
						int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
						if (this.source[end]=='\n') end--;
						if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, end);
						return null;
					}
					this.lineStarted = true;
					return createMethodReference(receiver, null);
				}
				break nextArg;
			}
			iToken++;

			// Read possible array declaration
			int dim = 0;
			long[] dimPositions = new long[20]; // assume that there won't be more than 20 dimensions...
			if (readToken() == TerminalTokens.TokenNameLBRACKET) {
				int dimStart = this.scanner.getCurrentTokenStartPosition();
				while (readToken() == TerminalTokens.TokenNameLBRACKET) {
					consumeToken();
					if (readToken() != TerminalTokens.TokenNameRBRACKET) {
						break nextArg;
					}
					consumeToken();
					dimPositions[dim++] = (((long) dimStart) << 32) + this.scanner.getCurrentTokenEndPosition();
				}
			}

			// Read argument name
			long argNamePos = -1;
			if (readToken() == TerminalTokens.TokenNameIdentifier) {
				consumeToken();
				if (firstArg) { // verify position
					if (iToken != 1)
						break nextArg;
				} else if ((iToken % modulo) != 1) {
						break nextArg;
				}
				if (argName == null) { // verify that all arguments name are declared
					if (!firstArg) {
						break nextArg;
					}
				}
				argName = this.scanner.getCurrentIdentifierSource();
				argNamePos = (((long)this.scanner.getCurrentTokenStartPosition())<<32)+this.scanner.getCurrentTokenEndPosition();
				iToken++;
			} else if (argName != null) { // verify that no argument name is declared
				break nextArg;
			}
			
			// Verify token position
			if (firstArg) {
				modulo = iToken + 1;
			} else {
				if ((iToken % modulo) != (modulo - 1)) {
					break nextArg;
				}
			}

			// Read separator or end arguments declaration
			int token = readToken();
			char[] name = argName == null ? new char[0] : argName;
			if (token == TerminalTokens.TokenNameCOMMA) {
				// Create new argument
				Object argument = createArgumentReference(name, dim, typeRef, dimPositions, argNamePos);
				arguments.add(argument);
				consumeToken();
				iToken++;
			} else if (token == TerminalTokens.TokenNameRPAREN) {
				// verify characters after arguments declaration (expecting white space or end comment)
				if (!verifySpaceOrEndComment()) {
					int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
					if (this.source[end]=='\n') end--;
					if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, end);
					return null;
				}
				// Create new argument
				Object argument = createArgumentReference(name, dim, typeRef, dimPositions, argNamePos);
				arguments.add(argument);
				consumeToken();
				return createMethodReference(receiver, arguments);
			} else {
				break nextArg;
			}
		}

		// Something wrong happened => Invalid input
		throw new InvalidInputException();
	}

	/*
	 * Parse an URL link reference in @see tag
	 */
	private boolean parseHref() throws InvalidInputException {
		int start = this.scanner.getCurrentTokenStartPosition();
		if (Character.toLowerCase(readChar()) == 'a') {
			this.scanner.currentPosition = this.index;
			if (readToken() == TerminalTokens.TokenNameIdentifier) {
				this.currentTokenType = -1; // do not update line end
				try {
					if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'h', 'r', 'e', 'f'}, false) &&
						readToken() == TerminalTokens.TokenNameEQUAL) {
						this.currentTokenType = -1; // do not update line end
						if (readToken() == TerminalTokens.TokenNameStringLiteral) {
							this.currentTokenType = -1; // do not update line end
							// Skip all characters after string literal until closing '>' (see bug 68726)
							while (this.index <= this.lineEnd && readToken() != TerminalTokens.TokenNameGREATER) {
								this.currentTokenType = -1; // do not update line end
							}
							if (this.currentTokenType == TerminalTokens.TokenNameGREATER) {
								consumeToken(); // update line end as new lines are allowed in URL description
								while (readToken() != TerminalTokens.TokenNameLESS) {
									if (this.scanner.currentPosition >= this.scanner.eofPosition || this.scanner.currentCharacter == '@') {
										// Reset position: we want to rescan last token
										this.index = this.tokenPreviousPosition;
										this.scanner.currentPosition = this.tokenPreviousPosition;
										this.currentTokenType = -1;
										// Signal syntax error
										if (this.tagValue != TAG_VALUE_VALUE && this.sourceParser != null)
											this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
										return false;
									}
									consumeToken();
								}
								this.currentTokenType = -1; // do not update line end
								start = this.scanner.getCurrentTokenStartPosition();
								if (readChar() == '/') {
									if (Character.toLowerCase(readChar()) == 'a') {
										if (readChar() == '>') {
											// Valid href
											return true;
										}
									}
								}
							}
						}
					}
				} catch (InvalidInputException ex) {
					// Do nothing as we want to keep positions for error message
				}
			}
		}
		// Reset position: we want to rescan last token
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;
		// Signal syntax error
		if (this.tagValue != TAG_VALUE_VALUE && this.sourceParser != null)
			this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
		return false;
	}

	/*
	 * Parse a method reference in @see tag
	 */
	private Object parseMember(Object receiver) throws InvalidInputException {
		// Init
		this.identifierPtr = -1;
		this.identifierLengthPtr = -1;
		int start = this.scanner.getCurrentTokenStartPosition();
		this.memberStart = start;
	
		// Get member identifier
		if (readToken() == TerminalTokens.TokenNameIdentifier) {
			consumeToken();
			pushIdentifier(true);
			// Look for next token to know whether it's a field or method reference
			int previousPosition = this.index;
			if (readToken() == TerminalTokens.TokenNameLPAREN) {
				consumeToken();
				start = this.scanner.getCurrentTokenStartPosition();
				try {
					return parseArguments(receiver);
				} catch (InvalidInputException e) {
					int end = this.scanner.getCurrentTokenEndPosition() < this.lineEnd ?
							this.scanner.getCurrentTokenEndPosition() :
							this.scanner.getCurrentTokenStartPosition();
					end = end < this.lineEnd ? end : this.lineEnd;
					if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReferenceArgs(start, end);
				}
				return null;
			}
	
			// Reset position: we want to rescan last token
			this.index = previousPosition;
			this.scanner.currentPosition = previousPosition;
			this.currentTokenType = -1;
	
			// Verify character(s) after identifier (expecting space or end comment)
			if (!verifySpaceOrEndComment()) {
				int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
				if (this.source[end]=='\n') end--;
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, end);
				return null;
			}
			return createFieldReference(receiver);
		}
		int end = getTokenEndPosition() - 1;
		end = start > end ? start : end;
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidReference(start, end);
		// Reset position: we want to rescan last token
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;
		return null;
	}

	/*
	 * Parse @param tag declaration
	 */
	protected boolean parseParam() {

		// Store current token state
		int start = this.tagSourceStart;
		int end = this.tagSourceEnd;

		try {
			// Push identifier next
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					consumeToken();
					return pushParamName();
				case TerminalTokens.TokenNameEOF :
					break;
				default :
					start = this.scanner.getCurrentTokenStartPosition();
					end = getTokenEndPosition();
					if (end < start) start = this.tagSourceStart;
					break;
			}
		} catch (InvalidInputException e) {
			end = getTokenEndPosition();
		}

		// Reset position to avoid missing tokens when new line was encountered
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;

		// Report problem
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingParamName(start, end);
		return false;
	}

	/*
	 * Parse a qualified name and built a type reference if the syntax is valid.
	 */
	protected Object parseQualifiedName(boolean reset) throws InvalidInputException {

		// Reset identifier stack if requested
		if (reset) {
			this.identifierPtr = -1;
			this.identifierLengthPtr = -1;
		}

		// Scan tokens
		int primitiveToken = -1;
		nextToken : for (int iToken = 0; ; iToken++) {
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					if (((iToken % 2) > 0)) { // identifiers must be odd tokens
						break nextToken;
					}
					pushIdentifier(iToken == 0);
					consumeToken();
					break;

				case TerminalTokens.TokenNameDOT :
					if ((iToken % 2) == 0) { // dots must be even tokens
						throw new InvalidInputException();
					}
					consumeToken();
					break;

				case TerminalTokens.TokenNamevoid :
				case TerminalTokens.TokenNameboolean :
				case TerminalTokens.TokenNamebyte :
				case TerminalTokens.TokenNamechar :
				case TerminalTokens.TokenNamedouble :
				case TerminalTokens.TokenNamefloat :
				case TerminalTokens.TokenNameint :
				case TerminalTokens.TokenNamelong :
				case TerminalTokens.TokenNameshort :
					if (iToken > 0) {
						throw new InvalidInputException();
					}
					pushIdentifier(true);
					primitiveToken = token;
					consumeToken();
					break nextToken;

				default :
					if (iToken == 0) {
						return null;
					}
					if ((iToken % 2) == 0) { // cannot leave on a dot
						// Reset position: we want to rescan last token
						if (this.kind == DOM_PARSER && this.currentTokenType != -1) {
							this.index = this.tokenPreviousPosition;
							this.scanner.currentPosition = this.tokenPreviousPosition;
							this.currentTokenType = -1;
						}
						throw new InvalidInputException();
					}
					break nextToken;
			}
		}
		// Reset position: we want to rescan last token
		if (this.currentTokenType != -1) {
			this.index = this.tokenPreviousPosition;
			this.scanner.currentPosition = this.tokenPreviousPosition;
			this.currentTokenType = -1;
		}
		this.lastIdentifierEndPosition = (int) this.identifierPositionStack[this.identifierPtr];
		return createTypeReference(primitiveToken);
	}

	/*
	 * Parse a reference in @see tag
	 */
	protected boolean parseReference() throws InvalidInputException {
		int currentPosition = this.scanner.currentPosition;
		try {
			Object typeRef = null;
			Object reference = null;
			int previousPosition = -1;
			int typeRefStartPosition = -1;
			
			// Get reference tokens
			nextToken : while (this.index < this.scanner.eofPosition) {
				previousPosition = this.index;
				int token = readToken();
				switch (token) {
					case TerminalTokens.TokenNameStringLiteral : // @see "string"
						consumeToken();
						int start = this.scanner.getCurrentTokenStartPosition();
						if (this.tagValue == TAG_VALUE_VALUE) {
							if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidValueReference(start, getTokenEndPosition());
						} else {
							// If typeRef != null we may raise a warning here to let user know there's an unused reference...
							// Currently as javadoc 1.4.2 ignore it, we do the same (see bug 69302)
							if (typeRef != null) {
								start = this.tagSourceEnd+1;
								previousPosition = start;
								typeRef = null;
							}
							// verify end line (expecting empty or end comment)
							if (verifyEndLine(previousPosition)) {
								return true;
							}
							if (this.sourceParser != null) this.sourceParser.problemReporter().javadocUnexpectedText(this.scanner.currentPosition, this.lineEnd);
						}
						return false;
					case TerminalTokens.TokenNameLESS : // @see "<a href="URL#Value">label</a>
						consumeToken();
						start = this.scanner.getCurrentTokenStartPosition();
						if (parseHref()) {
							consumeToken();
							// If typeRef != null we may raise a warning here to let user know there's an unused reference...
							// Currently as javadoc 1.4.2 ignore it, we do the same (see bug 69302)
							if (typeRef != null) {
								start = this.tagSourceEnd+1;
								previousPosition = start;
								typeRef = null;
							}
							// verify end line (expecting empty or end comment)
							if (verifyEndLine(previousPosition)) {
								return true;
							}
							if (this.tagValue != TAG_VALUE_VALUE && this.sourceParser != null) {
//								this.sourceParser.problemReporter().javadocInvalidReference(start, this.lineEnd);
								if (this.sourceParser != null) this.sourceParser.problemReporter().javadocUnexpectedText(this.scanner.currentPosition, this.lineEnd);
							}
						}
						if (this.tagValue == TAG_VALUE_VALUE && this.sourceParser != null) {
							this.sourceParser.problemReporter().javadocInvalidValueReference(start, getIndexPosition());
						}
						return false;
					case TerminalTokens.TokenNameERROR :
						if (this.scanner.currentCharacter == '#') { // @see ...#member
							consumeToken();
							reference = parseMember(typeRef);
							if (reference != null) {
								return pushSeeRef(reference);
							}
							return false;
						}
						break nextToken;
					case TerminalTokens.TokenNameIdentifier :
						if (typeRef == null) {
							typeRefStartPosition = this.scanner.getCurrentTokenStartPosition();
							typeRef = parseQualifiedName(true);
							break;
						}
					default :
						break nextToken;
				}
			}

			// Verify that we got a reference
			if (reference == null) reference = typeRef;
			if (reference == null) {
				this.index = this.tokenPreviousPosition;
				this.scanner.currentPosition = this.tokenPreviousPosition;
				this.currentTokenType = -1;
				if (this.tagValue == TAG_VALUE_VALUE) {
					if (this.kind == DOM_PARSER) createTag();
					return true;
				}
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingReference(this.tagSourceStart, this.tagSourceEnd);
				return false;
			}

			// Reset position at the end of type reference
			this.index = this.lastIdentifierEndPosition+1;
			this.scanner.currentPosition = this.index;
			this.currentTokenType = -1;

			// In case of @value, we have an invalid reference (only static field refs are valid for this tag)
			if (this.tagValue == TAG_VALUE_VALUE) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidReference(typeRefStartPosition, this.lineEnd);
				return false;
			}

			// Verify that line end does not start with an open parenthese (which could be a constructor reference wrongly written...)
			// See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=47215
			char ch = peekChar();
			if (ch == '(') {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingHashCharacter(typeRefStartPosition, this.lineEnd, String.valueOf(this.source, typeRefStartPosition, this.lineEnd-typeRefStartPosition+1));
				return false;
			}

			// Verify that we get white space after reference
			if (!verifySpaceOrEndComment()) {
				this.index = this.tokenPreviousPosition;
				this.scanner.currentPosition = this.tokenPreviousPosition;
				this.currentTokenType = -1;
				int end = this.starPosition == -1 ? this.lineEnd : this.starPosition;
				if (this.source[end]=='\n') end--;
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMalformedSeeReference(typeRefStartPosition, end);
				return false;
			}
			
			// Everything is OK, store reference
			return pushSeeRef(reference);
		}
		catch (InvalidInputException ex) {
			if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidReference(currentPosition, getTokenEndPosition());
		}
		// Reset position to avoid missing tokens when new line was encountered
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;
		return false;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected abstract boolean parseReturn();

	/*
	 * Parse @see tag declaration
	 *
	protected boolean parseSee(int value) {
		int start = this.scanner.currentPosition;
		try {
			return parseReference(value);
		} catch (InvalidInputException ex) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, getEndPosition());
		}
		// Reset position to avoid missing tokens when new line was encountered
		this.index = this.tokenPreviousPosition;
		this.scanner.currentPosition = this.tokenPreviousPosition;
		this.currentTokenType = -1;
		return false;
	}
	*/

	/*
	 * Parse tag declaration
	 */
	protected abstract boolean parseTag(int previousPosition) throws InvalidInputException;

	/*
	 * Parse @throws tag declaration
	 */
	protected boolean parseThrows() {
		int start = this.scanner.currentPosition;
		try {
			Object typeRef = parseQualifiedName(true);
			if (typeRef == null) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingThrowsClassName(this.tagSourceStart, this.tagSourceEnd);
			} else {
				return pushThrowName(typeRef);
			}
		} catch (InvalidInputException ex) {
			if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidThrowsClass(start, getTokenEndPosition());
		}
		return false;
	}

	/*
	 * Return current character without move index position.
	 */
	protected char peekChar() {
		int idx = this.index;
		char c = this.source[idx++];
		if (c == '\\' && this.source[idx] == 'u') {
			int c1, c2, c3, c4;
			idx++;
			while (this.source[idx] == 'u')
				idx++;
			if (!(((c1 = Character.getNumericValue(this.source[idx++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(this.source[idx++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(this.source[idx++])) > 15 || c3 < 0) || ((c4 = Character.getNumericValue(this.source[idx++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			}
		}
		return c;
	}

	/*
	 * push the consumeToken on the identifier stack. Increase the total number of identifier in the stack.
	 */
	protected void pushIdentifier(boolean newLength) {

		int stackLength = this.identifierStack.length;
		if (++this.identifierPtr >= stackLength) {
			System.arraycopy(
				this.identifierStack, 0,
				this.identifierStack = new char[stackLength + 10][], 0,
				stackLength);
			System.arraycopy(
				this.identifierPositionStack, 0,
				this.identifierPositionStack = new long[stackLength + 10], 0,
				stackLength);
		}
		this.identifierStack[this.identifierPtr] = this.scanner.getCurrentIdentifierSource();
		this.identifierPositionStack[this.identifierPtr] = (((long) this.scanner.startPosition) << 32) + (this.scanner.currentPosition - 1);

		if (newLength) {
			stackLength = this.identifierLengthStack.length;
			if (++this.identifierLengthPtr >= stackLength) {
				System.arraycopy(
					this.identifierLengthStack, 0,
					this.identifierLengthStack = new int[stackLength + 10], 0,
					stackLength);
			}
			this.identifierLengthStack[this.identifierLengthPtr] = 1;
		} else {
			this.identifierLengthStack[this.identifierLengthPtr]++;
		}
	}

	/*
	 * Add a new obj on top of the ast stack.
	 * If new length is required, then add also a new length in length stack.
	 */
	protected void pushOnAstStack(Object node, boolean newLength) {

		if (node == null) {
			this.astLengthStack[++this.astLengthPtr] = 0;
			return;
		}

		int stackLength = this.astStack.length;
		if (++this.astPtr >= stackLength) {
			System.arraycopy(
				this.astStack, 0,
				this.astStack = new Object[stackLength + AstStackIncrement], 0,
				stackLength);
			this.astPtr = stackLength;
		}
		this.astStack[this.astPtr] = node;

		if (newLength) {
			stackLength = this.astLengthStack.length;
			if (++this.astLengthPtr >= stackLength) {
				System.arraycopy(
					this.astLengthStack, 0,
					this.astLengthStack = new int[stackLength + AstStackIncrement], 0,
					stackLength);
			}
			this.astLengthStack[this.astLengthPtr] = 1;
		} else {
			this.astLengthStack[this.astLengthPtr]++;
		}
	}

	/*
	 * Push a param name in ast node stack.
	 */
	protected abstract boolean pushParamName();

	/*
	 * Push a reference statement in ast node stack.
	 */
	protected abstract boolean pushSeeRef(Object statement);

	/*
	 * Push a text element in ast node stack
	 */
	protected abstract void pushText(int start, int end);

	/*
	 * Push a throws type ref in ast node stack.
	 */
	protected abstract boolean pushThrowName(Object typeRef);

	/*
	 * Read current character and move index position.
	 * Warning: scanner position is unchanged using this method!
	 */
	protected char readChar() {
	
		char c = this.source[this.index++];
		if (c == '\\' && this.source[this.index] == 'u') {
			int c1, c2, c3, c4;
			int pos = this.index;
			this.index++;
			while (this.source[this.index] == 'u')
				this.index++;
			if (!(((c1 = Character.getNumericValue(this.source[this.index++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(this.source[this.index++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(this.source[this.index++])) > 15 || c3 < 0) || ((c4 = Character.getNumericValue(this.source[this.index++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			} else {
				// TODO (frederic) currently reset to previous position, perhaps signal a syntax error would be more appropriate
				this.index = pos;
			}
		}
		return c;
	}

	/*
	 * Read token only if previous was consumed
	 */
	private int readToken() throws InvalidInputException {
		if (this.currentTokenType < 0) {
			this.tokenPreviousPosition = this.scanner.currentPosition;
			this.currentTokenType = this.scanner.getNextToken();
			if (this.scanner.currentPosition > (this.lineEnd+1)) { // be sure to be on next line (lineEnd is still on the same line)
				this.lineStarted = false;
				while (this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
					this.currentTokenType = this.scanner.getNextToken();
				}
			}
			this.index = this.scanner.currentPosition;
			this.lineStarted = true; // after having read a token, line is obviously started...
		}
		return this.currentTokenType;
	}

	protected int readTokenAndConsume() throws InvalidInputException {
		int token = readToken();
		consumeToken();
		return token;
	}
	
	/*
	 * Refresh start position and length of an inline tag.
	 */
	protected void refreshInlineTagPosition(int previousPosition) {
		// do nothing by default
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		int startPos = this.scanner.currentPosition<this.index ? this.scanner.currentPosition : this.index;
		int endPos = this.scanner.currentPosition<this.index ? this.index : this.scanner.currentPosition;
		if (startPos == this.source.length)
			return "EOF\n\n" + new String(this.source); //$NON-NLS-1$
		if (endPos > this.source.length)
			return "behind the EOF\n\n" + new String(this.source); //$NON-NLS-1$
	
		char front[] = new char[startPos];
		System.arraycopy(this.source, 0, front, 0, startPos);
	
		int middleLength = (endPos - 1) - startPos + 1;
		char middle[];
		if (middleLength > -1) {
			middle = new char[middleLength];
			System.arraycopy(
				this.source, 
				startPos, 
				middle, 
				0, 
				middleLength);
		} else {
			middle = CharOperation.NO_CHAR;
		}
		
		char end[] = new char[this.source.length - (endPos - 1)];
		System.arraycopy(
			this.source, 
			(endPos - 1) + 1, 
			end, 
			0, 
			this.source.length - (endPos - 1) - 1);
		
		buffer.append(front);
		if (this.scanner.currentPosition<this.index) {
			buffer.append("\n===============================\nScanner current position here -->"); //$NON-NLS-1$
		} else {
			buffer.append("\n===============================\nParser index here -->"); //$NON-NLS-1$
		}
		buffer.append(middle);
		if (this.scanner.currentPosition<this.index) {
			buffer.append("<-- Parser index here\n===============================\n"); //$NON-NLS-1$
		} else {
			buffer.append("<-- Scanner current position here\n===============================\n"); //$NON-NLS-1$
		}
		buffer.append(end);

		return buffer.toString();
	}

	/*
	 * Update 
	 */
	protected abstract void updateDocComment();

	/*
	 * Update line end
	 */
	protected void updateLineEnd() {
		while (this.index > (this.lineEnd+1)) { // be sure to be on next line (lineEnd is still on the same line)
			if (this.linePtr < this.lastLinePtr) {
				this.lineEnd = this.scanner.getLineEnd(++this.linePtr) - 1;
			} else {
				this.lineEnd = this.endComment;
				return;
			}
		}
	}

	/*
	 * Verify that end of the line only contains space characters or end of comment.
	 * Note that end of comment may be preceeding by several contiguous '*' chars.
	 */
	private boolean verifyEndLine(int textPosition) {
		int startPosition = this.index;
		int previousPosition = this.index;
		this.starPosition = -1;
		char ch = readChar();
		nextChar: while (true) {
			switch (ch) {
				case '\r':
				case '\n':
					if (this.kind == DOM_PARSER) {
						createTag();
						pushText(textPosition, previousPosition);
					}
					this.index = previousPosition;
					return true;
				case '\u000c' :	/* FORM FEED               */
				case ' ' :			/* SPACE                   */
				case '\t' :			/* HORIZONTAL TABULATION   */
					if (this.starPosition >= 0) break nextChar;
					break;
				case '*':
					this.starPosition = previousPosition;
					break;
				case '/':
					if (this.starPosition >= textPosition) {
						if (this.kind == DOM_PARSER) {
							createTag();
							pushText(textPosition, this.starPosition);
						}
						return true;
					}
				default :
					// leave loop
					break nextChar;
				
			}
			previousPosition = this.index;
			ch = readChar();
		}
		this.index = startPosition;
		return false;
	}

	/*
	 * Verify characters after a name matches one of following conditions:
	 * 	1- first character is a white space
	 * 	2- first character is a closing brace *and* we're currently parsing an inline tag
	 * 	3- are the end of comment (several contiguous star ('*') characters may be
	 * 	    found before the last slash ('/') character).
	 */
	private boolean verifySpaceOrEndComment() {
		int startPosition = this.index;
		// Whitespace or inline tag closing brace
		char ch = peekChar();
		switch (ch) {
			case '}':
				return this.inlineTagStarted;
			default:
				if (Character.isWhitespace(ch)) {
					return true;
				}
		}
		// End of comment
		int previousPosition = this.index;
		this.starPosition = -1;
		ch = readChar();
		nextChar: while (this.index<this.source.length) {
			switch (ch) {
				case '*':
					// valid whatever the number of star before last '/'
					this.starPosition = previousPosition;
					break;
				case '/':
					if (this.starPosition >= startPosition) { // valid only if a star was previous character
						return true;
					}
				default :
					// invalid whatever other character, even white spaces
					this.index = startPosition;
					return false;
				
			}
			previousPosition = this.index;
			ch = readChar();
		}
		this.index = startPosition;
		return false;
	}
}
