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
	Scanner scanner;
	Parser sourceParser;
	Annotation annotation;
	int index, tagSourceStart, tagSourceEnd, lineEnd;
	char[] source;
	boolean checkAnnotation;

	int currentTokenType = -1;

	// Identifier stack
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	// Ast stack
	static int AstStackIncrement = 10;
	protected int astPtr;
	protected AstNode[] astStack;
	protected int astLengthPtr;
	protected int[] astLengthStack;

	AnnotationParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		this.checkAnnotation = this.sourceParser.options.getSeverity(CompilerOptions.InvalidAnnotation) != ProblemSeverities.Ignore;
		if (this.checkAnnotation) {
			this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null);
		}
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
							if (this.annotation == null) {
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
			updateAnnotation();
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

	private AnnotationMessageSend parseArguments(TypeReference receiver) throws InvalidInputException {

		int modulo = 0; // should be 2 for (Type,Type,...) and 3 for (Type arg,Type arg,...)
		int iToken = 0;
		char[] argName = null;
		int ptr = astPtr;
		int lptr = astLengthPtr;

		// Parse arguments declaration if method reference
		nextArg : while (this.index < this.scanner.eofPosition) {

			// Read argument type reference
			int argStart = this.scanner.getCurrentTokenStartPosition();
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
				AnnotationArgumentExpression expr = new AnnotationArgumentExpression(name, argStart, this.scanner
						.getCurrentTokenStartPosition()
						- 1, typeRef);
				pushOnAstStack(expr, firstArg);
				consumeToken();
				iToken++;
			} else if (token == TerminalTokens.TokenNameRPAREN) {
				AnnotationArgumentExpression expr = new AnnotationArgumentExpression(name,
						argStart,
						this.scanner.getCurrentTokenStartPosition()- 1,
						typeRef);
				pushOnAstStack(expr, (iToken == (modulo - 1)));
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

	private void parseParam() throws InvalidInputException {

		// Store current token state
		int start = this.tagSourceStart;
		int end = this.tagSourceEnd;

		try {
			// Push identifier next
			int token = readTokenAndConsume();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					AnnotationArgument argument = new AnnotationArgument(this.scanner.getCurrentIdentifierSource(),
							this.scanner.getCurrentTokenStartPosition(),
							this.scanner.getCurrentTokenEndPosition());
					argument.declarationSourceStart = this.tagSourceStart;
					argument.declarationSourceEnd = this.tagSourceEnd;
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

	private void parseThrows() throws InvalidInputException {
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

	private void parseSee() throws InvalidInputException {
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
	 * add a new obj on top of the ast stack
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

	private void pushParamName(Argument arg) {
		// TODO: (frederic) To be changed when mixed tags declaration will be accepted
		switch (this.astLengthPtr) {
			case -1 :
				// push first param name
				pushOnAstStack(arg, true);
				break;
			case 0 :
				// push other param name
				pushOnAstStack(arg, false);
				break;
			default :
				this.sourceParser.problemReporter().annotationUnexpectedTag(arg.declarationSourceStart, arg.declarationSourceEnd);
		}
	}

	private void pushSeeRef(Statement statement) {
		// TODO: (frederic) To be changed when mixed tags declaration will be accepted
		switch (this.astLengthPtr) {
			case -1 :
				// no @param previously declared, nor @throw/@exception
				pushOnAstStack(null, true); // push 0 for parameters size
			case 0 :
				// no @throw/@exception previously declared
				pushOnAstStack(null, true); // push 0 for thrownExceptions size
			case 1 :
				// push first reference
				pushOnAstStack(statement, true);
				break;
			case 2 :
				// push other reference
				pushOnAstStack(statement, false);
				break;
			default :
				this.sourceParser.problemReporter().annotationUnexpectedTag(statement.sourceStart, statement.sourceEnd);
		}
	}

	private void pushThrowName(TypeReference typeRef) {
		// TODO: (frederic) To be changed when mixed tags declaration will be accepted
		switch (this.astLengthPtr) {
			case -1 :
				// no @param previously declared
				pushOnAstStack(null, true); // push 0 for parameters size
			case 0 :
				// push first class name
				pushOnAstStack(typeRef, true);
				break;
			case 1 :
				// push other class name
				pushOnAstStack(typeRef, false);
				break;
			default :
				this.sourceParser.problemReporter().annotationUnexpectedTag(typeRef.sourceStart, typeRef.sourceEnd);
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

	/*
	 * Fill annotation fields with information in ast nodes stack.
	 */
	private void updateAnnotation() {
		while (this.astLengthPtr >= 0) {
			// Starting with the stack top, so get references (eg. Expression) coming from @see declarations
			if (this.astLengthPtr == 2) {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					this.annotation.references = new Expression[size];
					for (int i = (size - 1); i >= 0; i--) {
						this.annotation.references[i] = (Expression) this.astStack[astPtr--];
					}
				}
			}

			// Then continuing with class names (eg. TypeReference) coming from @throw/@exception declarations
			else if (this.astLengthPtr == 1) {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					this.annotation.thrownExceptions = new TypeReference[size];
					for (int i = (size - 1); i >= 0; i--) {
						this.annotation.thrownExceptions[i] = (TypeReference) this.astStack[astPtr--];
					}
				}
			}

			// Finally, finishing with parameters nales (ie. Argument) coming from @param declaration
			else if (this.astLengthPtr == 0) {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					this.annotation.parameters = new AnnotationArgument[size];
					for (int i = (size - 1); i >= 0; i--) {
						this.annotation.parameters[i] = (AnnotationArgument) this.astStack[astPtr--];
					}
				}
			}

			// Flag all nodes got from other ast length stack pointer values as invalid....
			// TODO: (frederic) To be changed when mixed tags declaration will be accepted
			else {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						AstNode node = this.astStack[astPtr--];
						this.sourceParser.problemReporter().annotationUnexpectedTag(node.sourceStart, node.sourceEnd);
					}
				}
			}
		}
	}
}