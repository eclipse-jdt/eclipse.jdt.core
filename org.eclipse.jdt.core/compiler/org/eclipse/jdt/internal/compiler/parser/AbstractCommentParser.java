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
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
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
	
	// Public fields
	public Scanner scanner;
	
	// Protected fields
	protected boolean inherited, deprecated;
	protected char[] source;
	protected int index, endComment, lineEnd;
	protected int tagSourceStart, tagSourceEnd;
	protected Parser sourceParser;
	protected Object returnStatement;
	protected boolean lineStarted = false, inlineTagStarted = false;
	
	// Private fields
	private int currentTokenType = -1;
	
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
		this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null);
		this.identifierStack = new char[10][];
		this.identifierPositionStack = new long[10];
		this.identifierLengthStack = new int[20];
		this.astStack = new ASTNode[20];
		this.astLengthStack = new int[30];
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in javadoc comment.
	 * 
	 * If javadoc checking is enabled, will also construct an Javadoc node, which will be stored into Parser.javadoc
	 * slot for being consumed later on.
	 */
	public boolean checkDeprecation(int javadocStart, int javadocEnd) {

		boolean validComment = true;
		try {
			// Init
			this.index = javadocStart +3;
			this.endComment = javadocEnd - 2;
			this.astLengthPtr = -1;
			this.astPtr = -1;
			this.currentTokenType = -1;
			this.scanner.resetTo(this.index, this.endComment);
			this.inlineTagStarted = false;
			this.lineStarted = false;
			char nextCharacter= 0, previousChar;
			int charPosition = -1, inlineStartPosition = 0;
			this.returnStatement = null;
			this.inherited = false;
			this.deprecated = false;
			initLineEnd();
			
			// Loop on each comment character
			while (this.index < this.endComment) {
				int previousPosition = this.index;
				previousChar = nextCharacter;
				
				// Calculate line end (cannot use this.scanner.linePtr as scanner does not parse line ends again)
				if (this.index > this.lineEnd) {
					updateLineEnd();
					this.lineStarted = false;
				}
				
				// Read next char only if token was consumed
				if (this.currentTokenType < 0) {
					nextCharacter = readChar(); // consider unicodes
				} else {
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
				
				switch (nextCharacter) {
					case '@' :
						boolean valid = false;
						// Start tag parsing only if we are on line beginning or at inline tag beginning
						if (!this.lineStarted || previousChar == '{') {
							this.lineStarted = true;
							if (this.inlineTagStarted) {
								this.inlineTagStarted = false;
								if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidTag(inlineStartPosition, charPosition);
								validComment = false;
							} else {
								if (previousChar == '{') {
									this.inlineTagStarted = true;
								}
								this.scanner.resetTo(this.index, this.endComment);
								this.currentTokenType = -1; // flush token cache at line begin
								try {
									int tk = readTokenAndConsume();
									this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
									this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
									switch (tk) {
										case TerminalTokens.TokenNameIdentifier :
											char[] tag = this.scanner.getCurrentIdentifierSource();
											if (CharOperation.equals(tag, TAG_DEPRECATED)) {
												this.deprecated = true;
												valid = true;
											} else if (CharOperation.equals(tag, TAG_INHERITDOC)) {
												this.inherited = true;
												valid = true;
											} else if (CharOperation.equals(tag, TAG_PARAM)) {
												valid = parseParam();
											} else if (CharOperation.equals(tag, TAG_EXCEPTION)) {
												valid = parseThrows();
											} else if (CharOperation.equals(tag, TAG_SEE) ||
													CharOperation.equals(tag, TAG_LINK) ||
													CharOperation.equals(tag, TAG_LINKPLAIN)) {
												valid = parseSee();
											} else {
												valid = parseTag();
											}
											break;
										case TerminalTokens.TokenNamereturn :
											valid = parseReturn();
											break;
										case TerminalTokens.TokenNamethrows :
											valid = parseThrows();
											break;
									}
									if (!valid) {
										this.inlineTagStarted = false;
										validComment = false;
									}
								} catch (InvalidInputException e) {
									consumeToken();
								}
							}
						}
						break;
					case '\r':
					case '\n':
						this.lineStarted = false;
						break;
					case '}' :
						if (this.inlineTagStarted) this.inlineTagStarted = false;
						this.lineStarted = true;
						charPosition = previousPosition;
						break;
					case '{' :
						if (this.inlineTagStarted) {
							this.inlineTagStarted = false;
							if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidTag(inlineStartPosition, this.index);
						} else {
							inlineStartPosition = previousPosition;
						}
						break;
					case '*' :
						charPosition = previousPosition;
						break;
					default :
						charPosition = previousPosition;
						if (!this.lineStarted && !CharOperation.isWhitespace(nextCharacter)) {
							this.lineStarted = true;
						}
				}
			}
		} finally {
			updateJavadoc();
		}
		return validComment;
	}

	private void consumeToken() {
		this.currentTokenType = -1; // flush token cache
		updateLineEnd();
	}

	protected abstract Object createArgumentReference(char[] name, int dim, Object typeRef, int argEnd) throws InvalidInputException;
	protected abstract Object createFieldReference(Object receiver) throws InvalidInputException;
	protected abstract Object createMethodReference(Object receiver, List arguments) throws InvalidInputException;
	protected abstract Object createSingleNameReference();
	protected abstract Object createReturnStatement();
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
					return createMethodReference(receiver, null);
				}
				break nextArg;
			}
			int argEnd = this.scanner.getCurrentTokenEndPosition();
			iToken++;

			// Read possible array declaration
			int dim = 0;
			if (readToken() == TerminalTokens.TokenNameLBRACKET) {
				while (readToken() == TerminalTokens.TokenNameLBRACKET) {
					consumeToken();
					if (readToken() != TerminalTokens.TokenNameRBRACKET) {
						break nextArg;
					}
					consumeToken();
					dim++;
				}
				argEnd = this.scanner.getCurrentTokenEndPosition();
			}

			// Read argument name
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
				argEnd = this.scanner.getCurrentTokenEndPosition();
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
				Object argument = createArgumentReference(name, dim, typeRef, argEnd);
				arguments.add(argument);
				consumeToken();
				iToken++;
			} else if (token == TerminalTokens.TokenNameRPAREN) {
				// Create new argument
				Object argument = createArgumentReference(name, dim, typeRef, argEnd);
				arguments.add(argument);
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
				consumeToken();
				try {
					if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'h', 'r', 'e', 'f'}, false) &&
						readToken() == TerminalTokens.TokenNameEQUAL) {
						consumeToken();
						if (readToken() == TerminalTokens.TokenNameStringLiteral) {
							consumeToken();
							if (readToken() == TerminalTokens.TokenNameGREATER) {
								consumeToken();
								while (readToken() != TerminalTokens.TokenNameLESS) {
									if (this.scanner.currentPosition >= this.scanner.eofPosition || this.scanner.currentCharacter == '@') {
										if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
										return false;
									}
									consumeToken();
								}
								consumeToken();
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

		// Get member identifier
		if (readToken() == TerminalTokens.TokenNameIdentifier) {
			consumeToken();
			pushIdentifier(true);
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
			return createFieldReference(receiver);
		}
		int end = getEndPosition() - 1;
		end = start > end ? getEndPosition() : end;
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, end);
		return null;
	}

	/*
	 * Parse @param tag declaration
	 */
	private boolean parseParam() {

		// Store current token state
		int start = this.tagSourceStart;
		int end = this.tagSourceEnd;

		try {
			// Push identifier next
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					consumeToken();
					return pushParamName(createSingleNameReference());
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
	private Object parseQualifiedName(boolean reset) throws InvalidInputException {

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
						throw new InvalidInputException();
					}
					break nextToken;
			}
		}
		return createTypeReference(primitiveToken);
	}

	/*
	 * Parse a reference in @see tag
	 */
	private boolean parseReference() throws InvalidInputException {
		Object typeRef = null;
		Object reference = null;
		nextToken : while (this.index < this.scanner.eofPosition) {
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameStringLiteral :
					// @see "string"
					int start = this.scanner.getCurrentTokenStartPosition();
					if (typeRef == null) {
						consumeToken();
						if (this.source[this.index] == '\r' || this.source[this.index] == '\n') {
							return true;
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
							if (this.source[this.index] == '\r' || this.source[this.index] == '\n') {
								return true;
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
							return pushSeeRef(reference);
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
				return pushSeeRef(reference);
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
	private boolean parseReturn() {
		if (this.returnStatement == null) {
			this.returnStatement = createReturnStatement();
			return true;
		} else {
			if (this.sourceParser != null) this.sourceParser.problemReporter().javadocDuplicatedReturnTag(
					this.scanner.getCurrentTokenStartPosition(),
					this.scanner.getCurrentTokenEndPosition());
			return false;
		}
	}

	/*
	 * Parse @see tag declaration
	 */
	private boolean parseSee() {
		int start = this.scanner.currentPosition;
		try {
			return parseReference();
		} catch (InvalidInputException ex) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidSeeReference(start, getEndPosition());
		}
		return false;
	}
	
	/*
	 * Parse @return tag declaration
	 */
	private boolean parseTag() {
		return true;
	}

	/*
	 * Parse @throws tag declaration
	 */
	private boolean parseThrows() {
		int start = this.scanner.currentPosition;
		try {
			Object typeRef = parseQualifiedName(true);
			if (typeRef == null) {
				if (this.sourceParser != null) this.sourceParser.problemReporter().javadocMissingThrowsClassName(this.tagSourceStart, this.tagSourceEnd);
			} else {
				return pushThrowName(typeRef);
			}
		} catch (InvalidInputException ex) {
			if (this.sourceParser != null) this.sourceParser.problemReporter().javadocInvalidThrowsClass(start, getEndPosition());
		}
		return false;
	}
	
	/*
	 * push the consumeToken on the identifier stack. Increase the total number of identifier in the stack.
	 */
	private void pushIdentifier(boolean newLength) {

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
	private void pushOnAstStack(Object node, boolean newLength) {

		if (node == null) {
			this.astLengthStack[++this.astLengthPtr] = 0;
			return;
		}

		try {
			this.astStack[++this.astPtr] = node;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = this.astStack.length;
			Object[] oldStack = this.astStack;
			this.astStack = new ASTNode[oldStackLength + AstStackIncrement];
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
	private boolean pushParamName(Object arg) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(arg, true);
		} else {
			// Verify that no @throws has been declared before
			for (int i=THROWS_TAG_EXPECTED_ORDER; i<=this.astLengthPtr; i+=ORDERED_TAGS_NUMBER) {
				if (this.astLengthStack[i] != 0) {
					if (this.sourceParser != null) this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
					return false;
				}
			}
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push another param name
					pushOnAstStack(arg, false);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push new param name
					pushOnAstStack(arg, true);
					break;
				default:
					return false;
			}
		}
		return true;
	}

	/*
	 * Push a reference statement in ast node stack.
	 */
	private boolean pushSeeRef(Object statement) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(null, true);
			pushOnAstStack(null, true);
			pushOnAstStack(statement, true);
		} else {
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push empty @throws tag and new @see tag
					pushOnAstStack(null, true);
					pushOnAstStack(statement, true);
					break;
				case THROWS_TAG_EXPECTED_ORDER :
					// previous push was a @throws tag => push new @see tag
					pushOnAstStack(statement, true);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push another @see tag
					pushOnAstStack(statement, false);
					break;
				default:
					return false;
			}
		}
		return true;
	}

	private boolean pushThrowName(Object typeRef) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(null, true);
			pushOnAstStack(typeRef, true);
		} else {
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push new @throws tag
					pushOnAstStack(typeRef, true);
					break;
				case THROWS_TAG_EXPECTED_ORDER :
					// previous push was a @throws tag => push another @throws tag
					pushOnAstStack(typeRef, false);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push empty @param and new @throws tags
					pushOnAstStack(null, true);
					pushOnAstStack(typeRef, true);
					break;
				default:
					return false;
			}
		}
		return true;
	}

	protected char readChar() {

		char c = this.source[this.index++];
		if (c == '\\') {
			int c1, c2, c3, c4;
			this.index++;
			while (this.source[this.index] == 'u')
				this.index++;
			if (!(((c1 = Character.getNumericValue(this.source[this.index++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(this.source[this.index++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(this.source[this.index++])) > 15 || c3 < 0) || ((c4 = Character.getNumericValue(this.source[this.index++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
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
			if (this.scanner.currentPosition > (this.lineEnd+1) && this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
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
	protected abstract void initLineEnd();
	protected abstract void updateLineEnd();
	protected abstract void updateJavadoc();

}
