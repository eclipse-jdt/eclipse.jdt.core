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
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

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
	
	// tags expected positions
	public final static int ORDERED_TAGS_NUMBER = 3;
	public final static int PARAM_TAG_EXPECTED_ORDER = 0;
	public final static int THROWS_TAG_EXPECTED_ORDER = 1;
	public final static int SEE_TAG_EXPECTED_ORDER = 2;
	
	// Public fields
	public Annotation annotation;
	public boolean checkAnnotation;
	public Scanner scanner;
	
	// Private fields
	private int currentTokenType = -1;
	private Parser sourceParser;
	private int index, tagSourceStart, tagSourceEnd, lineEnd;
	private char[] source;
	
	// Identifier stack
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	// Ast stack
	protected static int AstStackIncrement = 10;
	protected int astPtr;
	protected AstNode[] astStack;
	protected int astLengthPtr;
	protected int[] astLengthStack;

	AnnotationParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		this.checkAnnotation = this.sourceParser.options.getSeverity(CompilerOptions.InvalidAnnotation) != ProblemSeverities.Ignore;
		this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null);
		this.identifierStack = new char[10][];
		this.identifierPositionStack = new long[10];
		this.identifierLengthStack = new int[20];
		this.astStack = new AstNode[20];
		this.astLengthStack = new int[30];
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in annotation.
	 * 
	 * If annotation checking is enabled, will also construct an Annotation node, which will be stored into Parser.annotation
	 * slot for being consumed later on.
	 */
	public boolean checkDeprecation(int annotationStart, int annotationEnd) {

		boolean foundDeprecated = false;
		try {
			this.source = this.sourceParser.scanner.source;
			if (this.checkAnnotation) {
				this.annotation = new Annotation(annotationStart, annotationEnd);
				this.astLengthPtr = -1;
				this.astPtr = -1;
				this.currentTokenType = -1;
			} else {
				this.annotation = null;
			}

			int firstLineNumber = this.sourceParser.scanner.getLineNumber(annotationStart);
			int lastLineNumber = this.sourceParser.scanner.getLineNumber(annotationEnd);

			// scan line per line, since tags must be at beginning of lines only
			nextLine : for (int line = firstLineNumber; line <= lastLineNumber; line++) {
				int lineStart = line == firstLineNumber
						? annotationStart + 3 // skip leading /**
						: this.sourceParser.scanner.getLineStart(line);
				this.index = lineStart;
				this.lineEnd = line == lastLineNumber
						? annotationEnd - 2 // remove trailing */
						: this.sourceParser.scanner.getLineEnd(line);
				while (this.index < this.lineEnd) {
					char nextCharacter = readChar(); // consider unicodes
					switch (nextCharacter) {
						case '@' :
							if (!this.checkAnnotation) {
								if ((readChar() == 'd') &&
									(readChar() == 'e') &&
									(readChar() == 'p') &&
									(readChar() == 'r') &&
									(readChar() == 'e') &&
									(readChar() == 'c') &&
									(readChar() == 'a') &&
									(readChar() == 't') &&
									(readChar() == 'e') &&
									(readChar() == 'd')) {
									// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
									nextCharacter = readChar();
									if (Character.isWhitespace(nextCharacter) || nextCharacter == '*') {
										foundDeprecated = true;
										break nextLine; // done
									}
								}
								continue nextLine;
							}
							this.scanner.resetTo(this.index, this.lineEnd);
							this.currentTokenType = -1; // flush token cache at line begin
							try {
								int tk = readTokenAndConsume();
								this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
								this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
								switch (tk) {
									case TerminalTokens.TokenNameIdentifier :
										char[] tag = this.scanner.getCurrentIdentifierSource();
										if (CharOperation.equals(tag, TAG_DEPRECATED)) {
											foundDeprecated = true;
										} else if (CharOperation.equals(tag, TAG_PARAM)) {
											parseParam();
										} else if (CharOperation.equals(tag, TAG_EXCEPTION)) {
											parseThrows();
										} else if (CharOperation.equals(tag, TAG_SEE)) {
											parseSee();
										}
										break;
									case TerminalTokens.TokenNamereturn :
										parseReturn();
										break;
									case TerminalTokens.TokenNamethrows :
										parseThrows();
										break;
								}
							} catch (InvalidInputException e) {
								consumeToken();
							}
							continue nextLine;
						case '*' :
							break;
						default :
							if (!CharOperation.isWhitespace(nextCharacter)) {
								continue nextLine;
							}
					}
				}
			}
		} finally {
			if (this.checkAnnotation) {
				updateAnnotation();
			}
			this.source = null; // release source as soon as finished
		}
		return foundDeprecated;
	}

	private void consumeToken() {
		this.currentTokenType = -1; // flush token cache
	}

	private int getEndPosition() {
		if (this.scanner.getCurrentTokenEndPosition() >= this.lineEnd) {
			return this.lineEnd - 1;
		} else {
			return this.scanner.getCurrentTokenEndPosition();
		}
	}

	/*
	 * Parse argument in @see tag method reference
	 */
	private AnnotationMessageSend parseArguments(TypeReference receiver) throws InvalidInputException {

		int modulo = 0; // should be 2 for (Type,Type,...) or 3 for (Type arg,Type arg,...)
		int iToken = 0;
		char[] argName = null;
		int ptr = astPtr;
		int lptr = astLengthPtr;

		// Parse arguments declaration if method reference
		nextArg : while (this.index < this.scanner.eofPosition) {

			// Read argument type reference
			TypeReference typeRef;
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
					AnnotationMessageSend msg = new AnnotationMessageSend(identifierStack[0], identifierPositionStack[0]);
					msg.receiver = receiver;
					return msg;
				}
				break nextArg;
			}
			int argStart = typeRef.sourceStart;
			int argEnd = typeRef.sourceEnd;
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
				long pos = ((long) typeRef.sourceStart) << 32 + typeRef.sourceEnd;
				if (typeRef instanceof AnnotationSingleTypeReference) {
					AnnotationSingleTypeReference singleRef = (AnnotationSingleTypeReference) typeRef;
					typeRef = new AnnotationArraySingleTypeReference(singleRef.token, dim, pos);
				} else {
					AnnotationQualifiedTypeReference qualifRef = (AnnotationQualifiedTypeReference) typeRef;
					typeRef = new AnnotationArrayQualifiedTypeReference(qualifRef, dim);
				}
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
				AnnotationArgumentExpression expr = new AnnotationArgumentExpression(name, argStart, argEnd, typeRef);
				pushOnAstStack(expr, firstArg);
				consumeToken();
				iToken++;
			} else if (token == TerminalTokens.TokenNameRPAREN) {
				AnnotationArgumentExpression expr = new AnnotationArgumentExpression(name, argStart, argEnd, typeRef);
				pushOnAstStack(expr, firstArg);
				int size = astLengthStack[astLengthPtr--];
				AnnotationArgumentExpression[] arguments = new AnnotationArgumentExpression[size];
				for (int i = (size - 1); i >= 0; i--) {
					arguments[i] = (AnnotationArgumentExpression) astStack[astPtr--];
				}
				AnnotationMessageSend msg = new AnnotationMessageSend(identifierStack[0], identifierPositionStack[0], arguments);
				msg.receiver = receiver;
				return msg;
			} else {
				break nextArg;
			}
		}

		// Invalid input: reset ast stacks pointers
		consumeToken();
		if (iToken > 0) {
			this.astPtr = ptr;
			this.astLengthPtr = lptr;
		}
		throw new InvalidInputException();
	}

	/*
	 * Parse an URL link reference in @see tag
	 */
	private boolean parseHref() throws InvalidInputException {
		int start = this.scanner.getCurrentTokenStartPosition();
		//int end = this.scanner.getCurrentTokenEndPosition();
		if (readTokenAndConsume() == TerminalTokens.TokenNameIdentifier) {
			//end = this.index-1;
			if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'a'}, false)
					&& readTokenAndConsume() == TerminalTokens.TokenNameIdentifier) {
				//end = this.index - 1;
				try {
					if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'h', 'r', 'e', 'f'}, false) &&
						readTokenAndConsume() == TerminalTokens.TokenNameEQUAL &&
						readTokenAndConsume() == TerminalTokens.TokenNameStringLiteral &&
						readTokenAndConsume() == TerminalTokens.TokenNameGREATER) {
						while (readTokenAndConsume() != TerminalTokens.TokenNameLESS) {
							if (this.scanner.currentPosition >= this.lineEnd) {
								this.sourceParser.problemReporter().annotationInvalidSeeUrlReference(start, this.lineEnd - 1);
								return false;
							}
						}
						if (readTokenAndConsume() == TerminalTokens.TokenNameDIVIDE	&&
							readTokenAndConsume() == TerminalTokens.TokenNameIdentifier) {
							//end = this.index - 1;
							if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'a'}, false)	&&
								readTokenAndConsume() == TerminalTokens.TokenNameGREATER) {
								// Valid href
								return true;
							}
						}
					}
				} catch (InvalidInputException ex) {
					// Place to change end position for error report
					//end = getEndPosition();
				}
			}
		}
		this.sourceParser.problemReporter().annotationInvalidSeeUrlReference(start, this.lineEnd - 1);
		return false;
	}

	/*
	 * Parse a method reference in @see tag
	 */
	private Expression parseMember(TypeReference receiver) throws InvalidInputException {
		this.identifierPtr = -1;
		this.identifierLengthPtr = -1;
		int start = this.scanner.getCurrentTokenStartPosition();
		if (readTokenAndConsume() == TerminalTokens.TokenNameIdentifier) {
			pushIdentifier(true);
			if (readTokenAndConsume() == TerminalTokens.TokenNameLPAREN) {
				start = this.scanner.currentPosition;
				AnnotationMessageSend msg = null;
				try {
					msg = parseArguments(receiver);
					msg.tagSourceStart = this.tagSourceStart;
					msg.tagSourceEnd = this.tagSourceEnd;
				} catch (InvalidInputException e) {
					int end = this.scanner.getCurrentTokenEndPosition() < this.lineEnd ?
							this.scanner.getCurrentTokenEndPosition() :
							this.scanner.getCurrentTokenStartPosition();
					end = end < this.lineEnd ? end : (this.lineEnd - 1);
					this.sourceParser.problemReporter()	.annotationInvalidSeeReferenceArgs(start, end);
				}
				return msg;
			}
			AnnotationFieldReference field = new AnnotationFieldReference(identifierStack[0], identifierPositionStack[0]);
			field.receiver = receiver;
			field.tagSourceStart = this.tagSourceStart;
			field.tagSourceEnd = this.tagSourceEnd;
			return field;
		}
		this.sourceParser.problemReporter().annotationInvalidSeeReference(start, getEndPosition());
		return null;
	}

	/*
	 * Parse @param tag declaration
	 */
	private void parseParam() {

		// Store current token state
		int start = this.tagSourceStart;
		int end = this.tagSourceEnd;

		try {
			// Push identifier next
			int token = readTokenAndConsume();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					AnnotationSingleNameReference argument = new AnnotationSingleNameReference(this.scanner.getCurrentIdentifierSource(),
							this.scanner.getCurrentTokenStartPosition(),
							this.scanner.getCurrentTokenEndPosition());
					argument.tagSourceStart = this.tagSourceStart;
					argument.tagSourceEnd = this.tagSourceEnd;
					pushParamName(argument);
					return;
				case TerminalTokens.TokenNameEOF :
					//end = scanner.eofPosition-2;
					break;
				default :
					start = scanner.getCurrentTokenStartPosition();
					end = scanner.getCurrentTokenEndPosition();
					break;
			}
		} catch (InvalidInputException e) {
			end = getEndPosition();
		}

		// Report problem
		this.sourceParser.problemReporter().annotationMissingParamName(start, end);
		consumeToken();
	}

	/*
	 * Parse a qualified name and built a type reference if the syntax is valid.
	 */
	private TypeReference parseQualifiedName(boolean reset) throws InvalidInputException {

		// Reset identifier stack if requested
		if (reset) {
			this.identifierPtr = -1;
			this.identifierLengthPtr = -1;
		}

		// Scan tokens
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
					consumeToken();
					break nextToken;

				default :
					if (iToken == 0) {
						return null;
					}
					if ((iToken % 2) == 0) { // dots must be followed by an identifier
						throw new InvalidInputException();
					}
					break nextToken;
			}
		}

		// Build type reference from read tokens
		TypeReference typeRef = null;
		int size = this.identifierLengthStack[this.identifierLengthPtr--];
		if (size == 1) { // Single Type ref
			typeRef = new AnnotationSingleTypeReference(
						identifierStack[this.identifierPtr],
						identifierPositionStack[this.identifierPtr],
						this.tagSourceStart,
						this.tagSourceEnd);
		} else if (size > 1) { // Qualified Type ref
			char[][] tokens = new char[size][];
			System.arraycopy(this.identifierStack, this.identifierPtr - size + 1, tokens, 0, size);
			long[] positions = new long[size];
			System.arraycopy(this.identifierPositionStack, this.identifierPtr - size + 1, positions, 0, size);
			typeRef = new AnnotationQualifiedTypeReference(tokens, positions, this.tagSourceStart, this.tagSourceEnd);
		}
		this.identifierPtr -= size;
		return typeRef;
	}

	/*
	 * Parse a reference in @see tag
	 */
	private Expression parseReference() throws InvalidInputException {
		TypeReference typeRef = null;
		nextToken : while (this.index < this.scanner.eofPosition) {
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameStringLiteral :
					// @see "string"
					int start = this.scanner.getCurrentTokenStartPosition();
					if (typeRef == null) {
						consumeToken();
						try {
							if (readToken() == TerminalTokens.TokenNameEOF) {
								return null;
							}
						} catch (InvalidInputException e) {// Do nothing as we want to underline from the beginning of the string
						}
					}
					this.sourceParser.problemReporter().annotationInvalidSeeReference(start, this.lineEnd - 1);
					return null;
				case TerminalTokens.TokenNameLESS :
					// @see "<a href="URL#Value">label</a>
					consumeToken();
					start = this.scanner.getCurrentTokenStartPosition();
					if (parseHref()) {
						if (typeRef == null) {
							consumeToken();
							try {
								if (readToken() == TerminalTokens.TokenNameEOF) {
									return null;
								}
							} catch (InvalidInputException e) {// Do nothing as we want to underline from the beginning of the href
							}
						}
						this.sourceParser.problemReporter().annotationInvalidSeeReference(start, this.lineEnd - 1);
					}
					return null;
				case TerminalTokens.TokenNameERROR :
					consumeToken();
					if (this.scanner.currentCharacter == '#') { // @see ...#member
						return parseMember(typeRef);
					}
					break nextToken;
				case TerminalTokens.TokenNameIdentifier :
					if (typeRef == null) {
						typeRef = parseQualifiedName(true);
						break;
					}
				default :
					break nextToken;
			}
		}
		if (typeRef == null) {
			this.sourceParser.problemReporter().annotationMissingSeeReference(this.tagSourceStart, this.tagSourceEnd);
		}
		return typeRef;
	}
	
	/*
	 * Parse @return tag declaration
	 */
	private void parseReturn() {
		if (this.annotation.returnStatement == null) {
			this.annotation.returnStatement = new AnnotationReturnStatement(scanner.getCurrentTokenStartPosition(),
					scanner.getCurrentTokenEndPosition(),
					scanner.getRawTokenSourceEnd());
		} else {
			this.sourceParser.problemReporter().annotationInvalidReturnTag(
					scanner.getCurrentTokenStartPosition(),
					scanner.getCurrentTokenEndPosition(),
					false);
		}
	}

	/*
	 * Parse @see tag declaration
	 */
	private void parseSee() {
		int start = this.scanner.currentPosition;
		try {
			Expression msgRef = parseReference();
			if (msgRef != null) {
				pushSeeRef(msgRef);
			}
		} catch (InvalidInputException ex) {
			this.sourceParser.problemReporter().annotationInvalidSeeReference(start, getEndPosition());
		} finally {
			consumeToken();
		}
	}

	/*
	 * Parse @throws tag declaration
	 */
	private void parseThrows() {
		int start = this.scanner.currentPosition;
		try {
			TypeReference typeRef = parseQualifiedName(true);
			if (typeRef == null) {
				this.sourceParser.problemReporter().annotationMissingThrowsClassName(this.tagSourceStart, this.tagSourceEnd);
			} else {
				pushThrowName(typeRef);
			}
		} catch (InvalidInputException ex) {
			this.sourceParser.problemReporter().annotationInvalidThrowsClass(start, getEndPosition());
		} finally {
			consumeToken();
		}
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
	private void pushOnAstStack(AstNode node, boolean newLength) {

		if (node == null) {
			this.astLengthStack[++this.astLengthPtr] = 0;
			return;
		}

		try {
			this.astStack[++this.astPtr] = node;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = this.astStack.length;
			AstNode[] oldStack = this.astStack;
			this.astStack = new AstNode[oldStackLength + AstStackIncrement];
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
	private void pushParamName(AnnotationSingleNameReference arg) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(arg, true);
		} else {
			// Verify that no @throws has been declared before
			for (int i=THROWS_TAG_EXPECTED_ORDER; i<=this.astLengthPtr; i+=ORDERED_TAGS_NUMBER) {
				if (this.astLengthStack[i] != 0) {
					this.sourceParser.problemReporter().annotationUnexpectedTag(arg.tagSourceStart, arg.tagSourceEnd);
					return;
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
			}
		}
	}

	/*
	 * Push a reference statement in ast node stack.
	 */
	private void pushSeeRef(Statement statement) {
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
			}
		}
	}

	private void pushThrowName(TypeReference typeRef) {
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
			}
		}
	}

	private char readChar() {

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
		buffer.append("check annotation: ").append(this.checkAnnotation).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("annotation: ").append(this.annotation).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		return buffer.toString();
	}
	/*
	 * Fill annotation fields with information in ast nodes stack.
	 */
	private void updateAnnotation() {
		if (this.astLengthPtr == -1) {
			return;
		}

		// Initialize arrays
		int[] sizes = new int[ORDERED_TAGS_NUMBER];
		for (int i=0; i<=this.astLengthPtr; i++) {
			sizes[i%ORDERED_TAGS_NUMBER] += this.astLengthStack[i];
		}
		this.annotation.references = new Expression[sizes[SEE_TAG_EXPECTED_ORDER]];
		this.annotation.thrownExceptions = new TypeReference[sizes[THROWS_TAG_EXPECTED_ORDER]];
		this.annotation.parameters = new AnnotationSingleNameReference[sizes[PARAM_TAG_EXPECTED_ORDER]];
		
		// Store nodes in arrays
		while (this.astLengthPtr >= 0) {
			int ptr = this.astLengthPtr % ORDERED_TAGS_NUMBER;
			// Starting with the stack top, so get references (eg. Expression) coming from @see declarations
			if (ptr == SEE_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.annotation.references[--sizes[ptr]] = (Expression) this.astStack[astPtr--];
				}
			}

			// Then continuing with class names (eg. TypeReference) coming from @throw/@exception declarations
			else if (ptr == THROWS_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.annotation.thrownExceptions[--sizes[ptr]] = (TypeReference) this.astStack[astPtr--];
				}
			}

			// Finally, finishing with parameters nales (ie. Argument) coming from @param declaration
			else if (ptr == PARAM_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.annotation.parameters[--sizes[ptr]] = (AnnotationSingleNameReference) this.astStack[astPtr--];
				}
			}
		}
	}
}