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
	protected boolean parseComment(int javadocStart, int javadocEnd) {

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
			this.lineEnd = (this.linePtr == this.lastLinePtr) ? this.endComment : javadocStart;
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
					this.lineStarted = false;
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
						boolean valid = false;
						// Start tag parsing only if we have a java identifier start character and if we are on line beginning or at inline tag beginning
						if (Character.isJavaIdentifierStart(peekChar()) && (!this.lineStarted || previousChar == '{')) {
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
								int token = readTokenAndConsume();
								this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
								this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
								char[] tag = this.scanner.getCurrentIdentifierSource(); // first token is either an identifier or a keyword
								if (this.kind == DOM_PARSER) {
									// For DOM parser, try to get tag name other than java identifier
									// (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51660)
									int tk = token;
									int le = this.lineEnd;
									char pc = peekChar();
									tagNameToken: while (tk != TerminalTokens.TokenNameERROR && tk != TerminalTokens.TokenNameEOF) {
										this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
										token = tk;
										// !, ", #, %, &, ', -, :, <, >
										if (Character.isWhitespace(pc)) break;
										switch (pc) {
											case '}':
											case '!':
											case '#':
											case '%':
											case '&':
											case '\'':
											case '-':
											case '<' :
											case '>':
												break tagNameToken;
										}
										tk = readTokenAndConsume();
										pc = peekChar();
									}
									int length = this.tagSourceEnd-this.tagSourceStart+1;
									tag = new char[length];
									System.arraycopy(this.source, this.tagSourceStart, tag, 0, length);
									this.index = this.tagSourceEnd+1;
									this.scanner.currentPosition = this.tagSourceEnd+1;
									this.tagSourceStart = previousPosition;
									this.lineEnd = le;
								}
								switch (token) {
									case TerminalTokens.TokenNameIdentifier :
										if (CharOperation.equals(tag, TAG_DEPRECATED)) {
											this.deprecated = true;
											if (this.kind == DOM_PARSER) {
												valid = parseTag();
											} else {
												valid = true;
											}
										} else if (CharOperation.equals(tag, TAG_INHERITDOC)) {
											// inhibits inherited flag when tags have been already stored
											// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51606
											// Note that for DOM_PARSER, nodes stack may be not empty even no '@' tag
											// was encountered in comment. But it cannot be the case for COMPILER_PARSER
											// and so is enough as it is only this parser which signals the missing tag warnings...
											this.inherited = this.astPtr==-1;
											if (this.kind == DOM_PARSER) {
												valid = parseTag();
											} else {
												valid = true;
											}
										} else if (CharOperation.equals(tag, TAG_PARAM)) {
											valid = parseParam();
										} else if (CharOperation.equals(tag, TAG_EXCEPTION)) {
											valid = parseThrows(false);
										} else if (CharOperation.equals(tag, TAG_SEE)) {
											if (this.inlineTagStarted) {
												// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
												// Cannot have @see inside inline comment
												valid = false;
												if (this.sourceParser != null)
													this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
											} else {
												valid = parseSee(false);
											}
										} else if (CharOperation.equals(tag, TAG_LINK)) {
											if (this.inlineTagStarted) {
												valid = parseSee(false);
											} else {
												// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
												// Cannot have @link outside inline comment
												valid = false;
												if (this.sourceParser != null)
													this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
											}
										} else if (CharOperation.equals(tag, TAG_LINKPLAIN)) {
											if (this.inlineTagStarted) {
												valid = parseSee(true);
											} else {
												valid = parseTag();
											}
										} else {
											valid = parseTag();
										}
										break;
									case TerminalTokens.TokenNamereturn :
										valid = parseReturn();
										break;
									case TerminalTokens.TokenNamethrows :
										valid = parseThrows(true);
										break;
									default:
										if (this.kind == DOM_PARSER) {
											switch (token) {
												case TerminalTokens.TokenNameabstract:
												case TerminalTokens.TokenNameassert:
												case TerminalTokens.TokenNameboolean:
												case TerminalTokens.TokenNamebreak:
												case TerminalTokens.TokenNamebyte:
												case TerminalTokens.TokenNamecase:
												case TerminalTokens.TokenNamecatch:
												case TerminalTokens.TokenNamechar:
												case TerminalTokens.TokenNameclass:
												case TerminalTokens.TokenNamecontinue:
												case TerminalTokens.TokenNamedefault:
												case TerminalTokens.TokenNamedo:
												case TerminalTokens.TokenNamedouble:
												case TerminalTokens.TokenNameelse:
												case TerminalTokens.TokenNameextends:
												case TerminalTokens.TokenNamefalse:
												case TerminalTokens.TokenNamefinal:
												case TerminalTokens.TokenNamefinally:
												case TerminalTokens.TokenNamefloat:
												case TerminalTokens.TokenNamefor:
												case TerminalTokens.TokenNameif:
												case TerminalTokens.TokenNameimplements:
												case TerminalTokens.TokenNameimport:
												case TerminalTokens.TokenNameinstanceof:
												case TerminalTokens.TokenNameint:
												case TerminalTokens.TokenNameinterface:
												case TerminalTokens.TokenNamelong:
												case TerminalTokens.TokenNamenative:
												case TerminalTokens.TokenNamenew:
												case TerminalTokens.TokenNamenull:
												case TerminalTokens.TokenNamepackage:
												case TerminalTokens.TokenNameprivate:
												case TerminalTokens.TokenNameprotected:
												case TerminalTokens.TokenNamepublic:
												case TerminalTokens.TokenNameshort:
												case TerminalTokens.TokenNamestatic:
												case TerminalTokens.TokenNamestrictfp:
												case TerminalTokens.TokenNamesuper:
												case TerminalTokens.TokenNameswitch:
												case TerminalTokens.TokenNamesynchronized:
												case TerminalTokens.TokenNamethis:
												case TerminalTokens.TokenNamethrow:
												case TerminalTokens.TokenNametransient:
												case TerminalTokens.TokenNametrue:
												case TerminalTokens.TokenNametry:
												case TerminalTokens.TokenNamevoid:
												case TerminalTokens.TokenNamevolatile:
												case TerminalTokens.TokenNamewhile:
													valid = parseTag();
													break;
											}
										}
								}
								this.textStart = this.index;
								if (!valid) {
									// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
									// do not stop the inline tag when error is encountered to get text after
									validComment = false;
									// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
									// for DOM AST node, store tag as text in case of invalid syntax
									if (this.kind == DOM_PARSER) {
										parseTag();
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
						// do nothing for '*' character
						break;
					default :
						if (!CharOperation.isWhitespace(nextCharacter)) {
							if (!this.lineStarted) {
								this.textStart = previousPosition;
							}
							this.lineStarted = true;
						}
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
	protected abstract Object createTypeReference(int primitiveToken);
	
	private int getEndPosition() {
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
					char pc = peekChar();
					if (!Character.isWhitespace(pc) && (!this.inlineTagStarted || pc != '}')) {
						if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, this.lineEnd);
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
				char pc = peekChar();
				if (!Character.isWhitespace(pc) && (!this.inlineTagStarted || pc != '}')) {
					if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMalformedSeeReference(start, this.lineEnd);
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

		// Something Invalid input: reset ast stacks pointers
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
							if (readToken() == TerminalTokens.TokenNameGREATER) {
								consumeToken(); // update line end as new lines are allowed in URL description
								while (readToken() != TerminalTokens.TokenNameLESS) {
									if (this.scanner.currentPosition >= this.scanner.eofPosition || this.scanner.currentCharacter == '@') {
										if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
										return false;
									}
									consumeToken();
								}
								this.currentTokenType = -1; // do not update line end
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
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
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
			if (this.currentTokenType != -1) {
				this.index = previousPosition;
				this.scanner.currentPosition = previousPosition;
				this.currentTokenType = -1;
			}
			return createFieldReference(receiver);
		}
		int end = getEndPosition() - 1;
		end = start > end ? getEndPosition() : end;
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, end);
		// Reset position: we want to rescan last token
		if (this.currentTokenType != -1) {
			this.index = getEndPosition();
			this.scanner.currentPosition = getEndPosition();
			this.currentTokenType = -1;
		}
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
					end = getEndPosition();
					if (end < start) start = this.tagSourceStart;
					break;
			}
		} catch (InvalidInputException e) {
			end = getEndPosition();
		}

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
		int previousPosition = -1;
		nextToken : for (int iToken = 0; ; iToken++) {
			previousPosition = this.index;
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
							this.index = previousPosition;
							this.scanner.currentPosition = previousPosition;
							this.currentTokenType = -1;
						}
						throw new InvalidInputException();
					}
					break nextToken;
			}
		}
		// Reset position: we want to rescan last token
		if (this.currentTokenType != -1) {
			this.index = previousPosition;
			this.scanner.currentPosition = previousPosition;
			this.currentTokenType = -1;
		}
		return createTypeReference(primitiveToken);
	}

	/*
	 * Parse a reference in @see tag
	 */
	protected boolean parseReference(boolean plain) throws InvalidInputException {
		Object typeRef = null;
		Object reference = null;
		int previousPosition = -1;
		nextToken : while (this.index < this.scanner.eofPosition) {
			previousPosition = this.index;
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameStringLiteral :
					// @see "string"
					int start = this.scanner.getCurrentTokenStartPosition();
					if (typeRef == null) {
						consumeToken();
						while (Character.isWhitespace(this.source[this.index])) {
							if (this.source[this.index] == '\r' || this.source[this.index] == '\n') {
								if (this.kind == DOM_PARSER) {
									parseTag();
									pushText(previousPosition, this.index);
								}
								return true;
							}
							this.index++;
						}
					}
					if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, this.lineEnd);
					return false;
				case TerminalTokens.TokenNameLESS :
					// @see "<a href="URL#Value">label</a>
					consumeToken();
					start = this.scanner.getCurrentTokenStartPosition();
					if (parseHref()) {
						if (typeRef == null) {
							consumeToken();
							while (Character.isWhitespace(this.source[this.index])) {
								if (this.source[this.index] == '\r' || this.source[this.index] == '\n') {
									if (this.kind == DOM_PARSER) {
										parseTag();
										pushText(previousPosition, this.index);
									}
									return true;
								}
								this.index++;
							}
						}
						if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, this.lineEnd);
					}
					return false;
				case TerminalTokens.TokenNameERROR :
					if (this.scanner.currentCharacter == '#') { // @see ...#member
						consumeToken();
						reference = parseMember(typeRef);
						if (reference != null) {
							return pushSeeRef(reference, plain);
						}
						return false;
					}
					break nextToken;
				case TerminalTokens.TokenNameIdentifier :
					if (typeRef == null) {
						typeRef = parseQualifiedName(true);
						break;
					}
					break nextToken;
				default :
					break nextToken;
			}
		}
		
		// Verify that we got a reference
		if (reference == null) reference = typeRef;
		if (reference == null) {
			if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingSeeReference(this.tagSourceStart, this.tagSourceEnd);
			return false;
		}

		// Verify that line end does not start with an open parenthese (which could be a constructor reference wrongly written...)
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=47215
		int start = this.scanner.getCurrentTokenStartPosition();
		try {
			int token = readToken();
			if (token != TerminalTokens.TokenNameLPAREN) {
				// Reset position: we want to rescan last token
				if (this.currentTokenType != -1) {
					this.index = previousPosition;
					this.scanner.currentPosition = previousPosition;
					this.currentTokenType = -1;
				}
				return pushSeeRef(reference, plain);
			}
		} catch (InvalidInputException e) {
			// Do nothing as we report an error after
		}
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, this.lineEnd);
		return false;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected abstract boolean parseReturn();

	/*
	 * Parse @see tag declaration
	 */
	protected boolean parseSee(boolean plain) {
		int start = this.scanner.currentPosition;
		try {
			return parseReference(plain);
		} catch (InvalidInputException ex) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, getEndPosition());
		}
		return false;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected abstract boolean parseTag();

	/*
	 * Parse @throws tag declaration
	 */
	protected boolean parseThrows(boolean real) {
		int start = this.scanner.currentPosition;
		try {
			Object typeRef = parseQualifiedName(true);
			if (typeRef == null) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingThrowsClassName(this.tagSourceStart, this.tagSourceEnd);
			} else {
				return pushThrowName(typeRef, real);
			}
		} catch (InvalidInputException ex) {
			if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidThrowsClass(start, getEndPosition());
		}
		return false;
	}
	
	/*
	 * push the consumeToken on the identifier stack. Increase the total number of identifier in the stack.
	 */
	protected void pushIdentifier(boolean newLength) {

		try {
			this.identifierStack[++this.identifierPtr] = this.scanner.getCurrentIdentifierSource();
			this.identifierPositionStack[this.identifierPtr] = (((long) this.scanner.startPosition) << 32)
					+ (this.scanner.currentPosition - 1);
		} catch (IndexOutOfBoundsException e) {
			//---stack reallaocation (identifierPtr is correct)---
			int oldStackLength = this.identifierStack.length;
			char[][] oldStack = this.identifierStack;
			this.identifierStack = new char[oldStackLength + 10][];
			System.arraycopy(oldStack, 0, this.identifierStack, 0, oldStackLength);
			this.identifierStack[this.identifierPtr] = this.scanner.getCurrentTokenSource();
			// identifier position stack
			long[] oldPos = this.identifierPositionStack;
			this.identifierPositionStack = new long[oldStackLength + 10];
			System.arraycopy(oldPos, 0, this.identifierPositionStack, 0, oldStackLength);
			this.identifierPositionStack[this.identifierPtr] = (((long) this.scanner.startPosition) << 32)
					+ (this.scanner.currentPosition - 1);
		}

		if (newLength) {
			try {
				this.identifierLengthStack[++this.identifierLengthPtr] = 1;
			} catch (IndexOutOfBoundsException e) {
				/* ---stack reallocation (identifierLengthPtr is correct)--- */
				int oldStackLength = this.identifierLengthStack.length;
				int oldStack[] = this.identifierLengthStack;
				this.identifierLengthStack = new int[oldStackLength + 10];
				System.arraycopy(oldStack, 0, this.identifierLengthStack, 0, oldStackLength);
				this.identifierLengthStack[this.identifierLengthPtr] = 1;
			}
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

		try {
			this.astStack[++this.astPtr] = node;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = this.astStack.length;
			Object[] oldStack = this.astStack;
			this.astStack = new Object[oldStackLength + AstStackIncrement];
			System.arraycopy(oldStack, 0, this.astStack, 0, oldStackLength);
			this.astPtr = oldStackLength;
			this.astStack[this.astPtr] = node;
		}

		if (newLength) {
			try {
				this.astLengthStack[++this.astLengthPtr] = 1;
			} catch (IndexOutOfBoundsException e) {
				int oldStackLength = this.astLengthStack.length;
				int[] oldPos = this.astLengthStack;
				this.astLengthStack = new int[oldStackLength + AstStackIncrement];
				System.arraycopy(oldPos, 0, this.astLengthStack, 0, oldStackLength);
				this.astLengthStack[this.astLengthPtr] = 1;
			}
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
	protected abstract boolean pushSeeRef(Object statement, boolean plain);

	protected abstract void pushText(int start, int end);
	protected void refreshInlineTagPosition(int previousPosition) {
		// do nothing by default
	}

	/*
	 * Return current character without move index position.
	 */
	private char peekChar() {
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
	 * Push a throws type ref in ast node stack.
	 */
	protected abstract boolean pushThrowName(Object typeRef, boolean real);

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
			this.currentTokenType = this.scanner.getNextToken();
			if (this.scanner.currentPosition > (this.lineEnd+1) && // be sure to be on next line (lineEnd is still on the same line)
				this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
				while (this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
					this.currentTokenType = this.scanner.getNextToken();
				}
			}
			this.index = this.scanner.currentPosition;
		}
		return this.currentTokenType;
	}

	private int readTokenAndConsume() throws InvalidInputException {
		int token = readToken();
		consumeToken();
		return token;
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
		//	+ "" //$NON-NLS-1$
		buffer.append(end);

		return buffer.toString();
	}

	/*
	 * Update line end
	 */
	protected void updateLineEnd() {
		while (this.index > (this.lineEnd+1)) { // be sure to be on next line (lineEnd is still on the same line)
			if (this.linePtr < this.lastLinePtr) {
				this.lineEnd = getLineEnd(++this.linePtr) - 1;
			} else {
				this.lineEnd = this.endComment;
				return;
			}
			this.lineStarted= false;
		}
	}
	protected abstract void updateDocComment();

	/*
	 * Search the line number corresponding to a specific position.
	 * See Scanner
	 */
	public final int getLineNumber(int position) {
	
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

	/*
	 * Search the source position corresponding to the end of a given line number.
	 * See Scanner
	 */
	public final int getLineEnd(int lineNumber) {
	
		if (this.lineEnds == null) 
			return -1;
		if (lineNumber > this.lineEnds.length+1) 
			return -1;
		if (lineNumber <= 0) 
			return -1;
		if (lineNumber == this.lineEnds.length + 1) 
			return this.scanner.eofPosition;
		return this.lineEnds[lineNumber-1]; // next line start one character behind the lineEnd of the previous line
	}
}
