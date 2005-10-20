/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.JavadocParser;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Parser specialized for decoding javadoc comments which includes cursor location for code completion.
 */
public class CompletionJavadocParser extends JavadocParser {
	
	// Initialize lengthes for block and inline tags tables
	public final static int INLINE_ALL_TAGS_LENGTH;
	public final static int BLOCK_ALL_TAGS_LENGTH;
	static {
		int length = 0;
		for (int i=0; i<INLINE_TAGS_LENGTH; i++) {
			length += INLINE_TAGS[i].length;
		}
		INLINE_ALL_TAGS_LENGTH  = length;
		length = 0;
		for (int i=0; i<BLOCK_TAGS_LENGTH; i++) {
			length += BLOCK_TAGS[i].length;
		}
		BLOCK_ALL_TAGS_LENGTH = length;
	}
	
	// Level tags are array of inline/block tags depending on compilation source level
	char[][][] levelTags = new char[2][][];
	int[] levelTagsLength = new int[2];
	
	// Completion specific info
	int cursorLocation;
	CompletionOnJavadoc completionNode = null;
	boolean pushText = false;

	public CompletionJavadocParser(CompletionParser sourceParser) {
		super(sourceParser);
		this.scanner = new CompletionScanner(ClassFileConstants.JDK1_3);
		this.kind = COMPLETION_PARSER;
		this.reportProblems = false;
		initLevelTags();
	}

	/*
	 * Do not parse comment if completion location is not included.
	 */
	public boolean checkDeprecation(int commentPtr) {
		this.cursorLocation = ((CompletionParser)sourceParser).cursorLocation;
		CompletionScanner completionScanner = (CompletionScanner)this.scanner;
		completionScanner.cursorLocation = this.cursorLocation;
		completionScanner.completionIdentifier = null;
		this.javadocStart = this.sourceParser.scanner.commentStarts[commentPtr];
		this.javadocEnd = this.sourceParser.scanner.commentStops[commentPtr];
		if (this.javadocStart <= this.cursorLocation && this.cursorLocation <= this.javadocEnd) {
			if (CompletionEngine.DEBUG) {
				System.out.println("COMPLETION in Javadoc:"); //$NON-NLS-1$
			}
			this.firstTagPosition = 1;
			super.checkDeprecation(commentPtr);
		} else {
			this.docComment = null;
		}
		return false;
	}

	/*
	 * Replace stored Javadoc node with specific completion one.
	 */
	protected boolean commentParse() {
		this.docComment = new CompletionJavadoc(this.javadocStart, this.javadocEnd);
		return super.commentParse();
	}

	/*
	 * Create argument expression. If it includes completion location, create and store completion node.
	 */
	protected Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object typeRef, long[] dimPositions, long argNamePos) throws InvalidInputException {
		// Create argument as we may need it after
		char[] argName = name==null ? CharOperation.NO_CHAR : name;
		Expression expression = (Expression) super.createArgumentReference(argName, dim, isVarargs, typeRef, dimPositions, argNamePos);
		// See if completion location is in argument
		int refStart = ((TypeReference)typeRef).sourceStart;
		int refEnd = ((TypeReference)typeRef).sourceEnd;
		boolean inCompletion = (refStart <= this.cursorLocation && this.cursorLocation <= refEnd) // completion cursor is between first and last stacked identifiers
			|| ((refStart == (refEnd+1) && refEnd == this.cursorLocation)); // or it's a completion on empty token
		if (this.completionNode == null && inCompletion) {
			JavadocArgumentExpression javadocArgument = (JavadocArgumentExpression) expression;
			TypeReference expressionType = javadocArgument.argument.type;
			if (expressionType instanceof JavadocSingleTypeReference) {
				this.completionNode = new CompletionOnJavadocSingleTypeReference((JavadocSingleTypeReference) expressionType);
			} else if (expressionType instanceof JavadocQualifiedTypeReference) {
				this.completionNode = new CompletionOnJavadocQualifiedTypeReference((JavadocQualifiedTypeReference) expressionType);
			}
			if (CompletionEngine.DEBUG) {
				System.out.println("	completion argument="+completionNode); //$NON-NLS-1$
			}
			return this.completionNode;
		}
		return expression;
	}

	/*
	 * Create field reference. If it includes completion location, create and store completion node.
	 */
	protected Object createFieldReference(Object receiver) throws InvalidInputException {
		int refStart = (int) (this.identifierPositionStack[0] >>> 32);
		int refEnd = (int) this.identifierPositionStack[0];
		boolean inCompletion = (refStart <= (this.cursorLocation+1) && this.cursorLocation <= refEnd) // completion cursor is between first and last stacked identifiers
			|| ((refStart == (refEnd+1) && refEnd == this.cursorLocation)) // or it's a completion on empty token
			|| (this.memberStart == this.cursorLocation); // or it's a completion just after the member separator with an identifier after the cursor
		if (inCompletion) {
			JavadocFieldReference fieldRef = (JavadocFieldReference) super.createFieldReference(receiver);
			char[] name = this.sourceParser.compilationUnit.getMainTypeName();
			TypeDeclaration typeDecl = getParsedTypeDeclaration();
			if (typeDecl != null) {
				name = typeDecl.name;
			}
			this.completionNode = new CompletionOnJavadocFieldReference(fieldRef, this.memberStart, name);
			if (CompletionEngine.DEBUG) {
				System.out.println("	completion field="+completionNode); //$NON-NLS-1$
			}
			return this.completionNode;
		}
		return super.createFieldReference(receiver);
	}

	/*
	 * Verify if method identifier positions include completion location.
	 * If so, create method reference and store it.
	 * Otherwise return null as we do not need this reference.
	 */
	protected Object createMethodReference(Object receiver, List arguments) throws InvalidInputException {
		int refStart = (int) (this.identifierPositionStack[0] >>> 32);
		int refEnd = (int) this.identifierPositionStack[0];
		boolean inCompletion = (refStart <= (this.cursorLocation+1) && this.cursorLocation <= refEnd) // completion cursor is between first and last stacked identifiers
			|| ((refStart == (refEnd+1) && refEnd == this.cursorLocation)) // or it's a completion on empty token
			|| (this.memberStart == this.cursorLocation); // or it's a completion just after the member separator with an identifier after the cursor
		if (inCompletion) {
			ASTNode node = (ASTNode) super.createMethodReference(receiver, arguments);
			if (node instanceof JavadocMessageSend) {
				this.completionNode = new CompletionOnJavadocMessageSend((JavadocMessageSend)node, this.memberStart);
			} else if (node instanceof JavadocAllocationExpression) {
				this.completionNode = new CompletionOnJavadocAllocationExpression((JavadocAllocationExpression)node, this.memberStart);
			}
			if (CompletionEngine.DEBUG) {
				System.out.println("	completion method="+completionNode); //$NON-NLS-1$
			}
			return this.completionNode;
		}
		return super.createMethodReference(receiver, arguments);
	}

	/*
	 * Create type reference. If it includes completion location, create and store completion node.
	 */
	protected Object createTypeReference(int primitiveToken) {
		// Need to create type ref in case it was needed by members
		int nbIdentifiers = this.identifierLengthStack[this.identifierLengthPtr];
		int startPtr = this.identifierPtr - (nbIdentifiers-1);
		int refStart = (int) (this.identifierPositionStack[startPtr] >>> 32);
		int refEnd = (int) this.identifierPositionStack[this.identifierPtr];
		boolean inCompletion = (refStart <= (this.cursorLocation+1) && this.cursorLocation <= refEnd) // completion cursor is between first and last stacked identifiers
			|| ((refStart == (refEnd+1) && refEnd == this.cursorLocation)); // or it's a completion on empty token
		if (!inCompletion) {
			return super.createTypeReference(primitiveToken);
		}
		this.identifierLengthPtr--;
		if (nbIdentifiers == 1) { // Single Type ref
			this.completionNode = new CompletionOnJavadocSingleTypeReference(
						this.identifierStack[this.identifierPtr],
						this.identifierPositionStack[this.identifierPtr],
						this.tagSourceStart,
						this.tagSourceEnd);
			if (this.identifierStack[this.identifierPtr] == CompletionScanner.EmptyCompletionIdentifier) {
				return null;
			}
		} else if (nbIdentifiers > 1) { // Qualified Type ref
			// See if completion is in qualification
			int size = nbIdentifiers;
			if (this.tagSourceStart <= this.cursorLocation && this.cursorLocation <= this.tagSourceEnd) {
				for (size=0; size<nbIdentifiers; size++) {
					int start = (int) (this.identifierPositionStack[size] >>> 32);
					int end = (int) this.identifierPositionStack[size];
					if (start <= this.cursorLocation && this.cursorLocation <= end) {
						size++;
						break;
					}
				}
			}
			
			// Copy only tokens necessary for completion
			char[][] tokens = new char[size-1][];
			System.arraycopy(this.identifierStack, startPtr, tokens, 0, size-1);
			long[] positions = new long[size];
			System.arraycopy(this.identifierPositionStack, startPtr, positions, 0, size);
			this.completionNode = new CompletionOnJavadocQualifiedTypeReference(tokens, this.identifierStack[this.identifierPtr], positions, this.tagSourceStart, this.tagSourceEnd);
		}
		this.identifierPtr -= nbIdentifiers;

		if (CompletionEngine.DEBUG) {
			System.out.println("	completion partial qualified type="+completionNode); //$NON-NLS-1$
		}
		return this.completionNode;
	}

	/*
	 * Get possible tags for a given prefix.
	 */
	private char[][][] possibleTags(char[] prefix, boolean lnStarted) {
		char[][][] possibleTags = new char[2][][];
		if (lnStarted) {
			possibleTags[BLOCK_IDX] = new char[0][];
		} else {
			System.arraycopy(this.levelTags[BLOCK_IDX], 0, possibleTags[BLOCK_IDX] = new char[this.levelTagsLength[BLOCK_IDX]][], 0, this.levelTagsLength[BLOCK_IDX]);
		}
		System.arraycopy(this.levelTags[INLINE_IDX], 0, possibleTags[INLINE_IDX] = new char[this.levelTagsLength[INLINE_IDX]][], 0, this.levelTagsLength[INLINE_IDX]);
		if (prefix == null) return possibleTags;
		if (possibleTags != null) {
			int kinds = levelTags.length;
			for (int k=0; k<kinds; k++) {
				int length = possibleTags[k].length, size = 0;
				int indexes[] = new int[length];
				for (int i=0; i<length; i++) {
					if (CharOperation.prefixEquals(prefix, possibleTags[k][i])) {
						indexes[size++] = i;
					}
				}
				char[][] tags = new char[size][];
				for (int i=0; i<size; i++) {
					tags[i] = possibleTags[k][indexes[i]];
				}
				possibleTags[k] = tags;
			}
			return possibleTags;
		}
		return null;
	}

	private CompletionJavadoc getCompletionJavadoc() {
		return (CompletionJavadoc)this.docComment;
	}

	private CompletionParser getCompletionParser() {
		return (CompletionParser)this.sourceParser;
	}

	/*
	 * Init tags arrays for current source level.
	 */
	private void initLevelTags() {
		int level = ((int)(this.sourceLevel >>> 16)) - ClassFileConstants.MAJOR_VERSION_1_1 + 1;
		// Init block tags
		this.levelTags[BLOCK_IDX] = new char[BLOCK_ALL_TAGS_LENGTH][];
		this.levelTagsLength[BLOCK_IDX] = 0;
		for (int i=0; i<=level; i++) {
			int length = BLOCK_TAGS[i].length;
			System.arraycopy(BLOCK_TAGS[i], 0, this.levelTags[BLOCK_IDX], this.levelTagsLength[BLOCK_IDX], length);
			this.levelTagsLength[BLOCK_IDX] += length;
		}
		if (this.levelTagsLength[BLOCK_IDX] < BLOCK_ALL_TAGS_LENGTH) {
			System.arraycopy(this.levelTags[BLOCK_IDX], 0, this.levelTags[BLOCK_IDX] = new char[this.levelTagsLength[BLOCK_IDX]][], 0, this.levelTagsLength[BLOCK_IDX]);
		}
		// Init inline tags
		this.levelTags[INLINE_IDX] = new char[INLINE_ALL_TAGS_LENGTH][];
		this.levelTagsLength[INLINE_IDX]= 0;
		for (int i=0; i<=level; i++) {
			int length = INLINE_TAGS[i].length;
			System.arraycopy(INLINE_TAGS[i], 0, this.levelTags[INLINE_IDX], this.levelTagsLength[INLINE_IDX], length);
			this.levelTagsLength[INLINE_IDX] += length;
		}
		if (this.levelTagsLength[INLINE_IDX] < INLINE_ALL_TAGS_LENGTH) {
			System.arraycopy(this.levelTags[INLINE_IDX], 0, this.levelTags[INLINE_IDX] = new char[this.levelTagsLength[INLINE_IDX]][], 0, this.levelTagsLength[INLINE_IDX]);
		}
	}
	/*
	 * Parse argument in @see tag method reference
	 */
	protected Object parseArguments(Object receiver) throws InvalidInputException {

		// Init
		int modulo = 0; // should be 2 for (Type,Type,...) or 3 for (Type arg,Type arg,...)
		int iToken = 0;
		char[] argName = null;
		List arguments = new ArrayList(10);
		Object typeRef = null;
		int dim = 0;
		boolean isVarargs = false;
		long[] dimPositions = new long[20]; // assume that there won't be more than 20 dimensions...
		char[] name = null;
		long argNamePos = -1;
		
		// Parse arguments declaration if method reference
		nextArg : while (this.index < this.scanner.eofPosition) {

			// Read argument type reference
			try {
				typeRef = parseQualifiedName(false);
				if (this.abort) return null; // May be aborted by specialized parser
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
				if (firstArg && getCurrentTokenType() == TerminalTokens.TokenNameRPAREN) {
					this.lineStarted = true;
					return createMethodReference(receiver, null);
				}
				Object methodRef = createMethodReference(receiver, arguments);
				return syntaxRecoverEmptyArgumentType(methodRef);
			}
			if (this.index >= this.scanner.eofPosition) {
				Object argument = createArgumentReference(this.scanner.getCurrentIdentifierSource(), 0, false, typeRef, null, (((long)this.scanner.getCurrentTokenStartPosition())<<32)+this.scanner.getCurrentTokenEndPosition());
				return syntaxRecoverArgumentType(receiver, arguments, argument);
			}
			if (this.index >= this.cursorLocation) {
				if (this.completionNode instanceof CompletionOnJavadocSingleTypeReference) {
					CompletionOnJavadocSingleTypeReference singleTypeReference = (CompletionOnJavadocSingleTypeReference) this.completionNode;
					if (singleTypeReference.token == null || singleTypeReference.token.length == 0) {
						Object methodRef = createMethodReference(receiver, arguments);
						return syntaxRecoverEmptyArgumentType(methodRef);
					}
				}
				if (this.completionNode instanceof CompletionOnJavadocQualifiedTypeReference) {
					CompletionOnJavadocQualifiedTypeReference qualifiedTypeReference = (CompletionOnJavadocQualifiedTypeReference) this.completionNode;
					if (qualifiedTypeReference.tokens == null || qualifiedTypeReference.tokens.length < qualifiedTypeReference.sourcePositions.length) {
						Object methodRef = createMethodReference(receiver, arguments);
						return syntaxRecoverEmptyArgumentType(methodRef);
					}
				}
			}
			iToken++;

			// Read possible additional type info
			dim = 0;
			isVarargs = false;
			if (readToken() == TerminalTokens.TokenNameLBRACKET) {
				// array declaration
				int dimStart = this.scanner.getCurrentTokenStartPosition();
				while (readToken() == TerminalTokens.TokenNameLBRACKET) {
					consumeToken();
					if (readToken() != TerminalTokens.TokenNameRBRACKET) {
						break nextArg;
					}
					consumeToken();
					dimPositions[dim++] = (((long) dimStart) << 32) + this.scanner.getCurrentTokenEndPosition();
				}
			} else if (readToken() == TerminalTokens.TokenNameELLIPSIS) {
				// ellipsis declaration
				int dimStart = this.scanner.getCurrentTokenStartPosition();
				dimPositions[dim++] = (((long) dimStart) << 32) + this.scanner.getCurrentTokenEndPosition();
				consumeToken();
				isVarargs = true;
			}

			// Read argument name
			argNamePos = -1;
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
			name = argName == null ? CharOperation.NO_CHAR : argName;
			if (token == TerminalTokens.TokenNameCOMMA) {
				// Create new argument
				Object argument = createArgumentReference(name, dim, isVarargs, typeRef, dimPositions, argNamePos);
				if (this.abort) return null; // May be aborted by specialized parser
				arguments.add(argument);
				consumeToken();
				iToken++;
			} else if (token == TerminalTokens.TokenNameRPAREN) {
				// Create new argument
				Object argument = createArgumentReference(name, dim, isVarargs, typeRef, dimPositions, argNamePos);
				if (this.abort) return null; // May be aborted by specialized parser
				arguments.add(argument);
				consumeToken();
				return createMethodReference(receiver, arguments);
			} else {
				Object argument = createArgumentReference(name, dim, isVarargs, typeRef, dimPositions, argNamePos);
				return syntaxRecoverArgumentType(receiver, arguments, argument);
			}
		}

		// Something wrong happened => Invalid input
		throw new InvalidInputException();
	}

		protected boolean parseParam() throws InvalidInputException {
			int startPosition = this.index;
			int endPosition = this.index;
			long namePosition = (((long)startPosition)<<32) + endPosition;
			this.identifierPtr = -1;
			boolean valid = super.parseParam();
			if (this.identifierPtr > 2) return valid;
			// See if expression is concerned by completion
			char[] name = null;
			CompletionScanner completionScanner = (CompletionScanner) this.scanner;
			boolean isTypeParam = false;
			if (this.identifierPtr < 0) {
				// workaround, empty token should set an empty identifier by scanner and so identifierPtr should be == 0
				if (completionScanner.getCurrentIdentifierSource() == CompletionScanner.EmptyCompletionIdentifier) {
					namePosition = completionScanner.completedIdentifierStart;
					startPosition = completionScanner.completedIdentifierStart;
					endPosition = completionScanner.completedIdentifierEnd;
				}
			} else {
				char[] identifier = null;
				switch (this.identifierPtr) {
					case 2:
						if (!valid && completionScanner.getCurrentIdentifierSource() == CompletionScanner.EmptyCompletionIdentifier) {
							valid = pushParamName(true);
						}
					case 1:
						isTypeParam = true;
						identifier = this.identifierStack[1];
						namePosition = this.identifierPositionStack[1];
						break;
					case 0:
						identifier = this.identifierStack[0];
						namePosition = this.identifierPositionStack[0];
						isTypeParam = identifier.length > 0 && identifier[0] == '<';
						break;
				}
				if (identifier.length > 0 && Character.isJavaIdentifierPart(identifier[0])) {
					name = identifier;
				}
				startPosition = (int)(this.identifierPositionStack[0]>>32);
				endPosition = (int)this.identifierPositionStack[this.identifierPtr];
			}
			boolean inCompletion = (startPosition <= (this.cursorLocation+1) && this.cursorLocation <= endPosition) // completion cursor is between first and last stacked identifiers
				|| ((startPosition == (endPosition+1) && endPosition == this.cursorLocation)); // or it's a completion on empty token
			if (inCompletion) {
				if (this.completionNode == null) {
					if (isTypeParam) {
						this.completionNode = new CompletionOnJavadocTypeParamReference(name, namePosition, startPosition, endPosition);
					} else {
						this.completionNode = new CompletionOnJavadocParamNameReference(name, namePosition, startPosition, endPosition);
					}
					if (CompletionEngine.DEBUG) {
						System.out.println("	completion param="+completionNode); //$NON-NLS-1$
					}
				} else if (this.completionNode instanceof CompletionOnJavadocParamNameReference) {
					CompletionOnJavadocParamNameReference paramNameRef = (CompletionOnJavadocParamNameReference)this.completionNode;
					int nameStart = (int) (namePosition>>32);
					paramNameRef.sourceStart = nameStart;
					int nameEnd = (int) namePosition;
					if (nameStart<this.cursorLocation && this.cursorLocation<nameEnd) {
						paramNameRef.sourceEnd = this.cursorLocation + 1;
					} else {
						paramNameRef.sourceEnd = nameEnd;
					}
					paramNameRef.tagSourceStart = startPosition;
					paramNameRef.tagSourceEnd = endPosition;
				} else if (this.completionNode instanceof CompletionOnJavadocTypeParamReference) {
					CompletionOnJavadocTypeParamReference typeParamRef = (CompletionOnJavadocTypeParamReference)this.completionNode;
					int nameStart = (int) (namePosition>>32);
					typeParamRef.sourceStart = nameStart;
					int nameEnd = (int) namePosition;
					if (nameStart<this.cursorLocation && this.cursorLocation<nameEnd) {
						typeParamRef.sourceEnd = this.cursorLocation + 1;
					} else {
						typeParamRef.sourceEnd = nameEnd;
					}
					typeParamRef.tagSourceStart = startPosition;
					typeParamRef.tagSourceEnd = endPosition;
				}
			}
			return valid;
		}

	/*(non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseTag(int)
	 */
	protected boolean parseTag(int previousPosition) throws InvalidInputException {
		char current = peekChar();
		int startPosition = this.inlineTagStarted ? this.inlineTagStart : previousPosition;
		switch (current) {
			case '*' :
			case '\u000c' :
			case ' ' :
			case '\t' :
			case '\r' :
			case '\n' :
				int end = this.index - 1;
				if (startPosition <= this.cursorLocation && this.cursorLocation <= end) {
					// completion on empty '@' => all tags
					long position = (((long)end)<<32) + end;
					char[][][] tags = possibleTags(null, this.lineStarted);
					if (tags != null) {
						this.completionNode = new CompletionOnJavadocTag(null, position, startPosition, end, tags);
					}
				}
				break;
			default:
				boolean ls = this.lineStarted;
				readTokenSafely();
				end = this.index - 1;
				if (startPosition <= this.cursorLocation && this.cursorLocation <= end) {
					if (this.inlineTagStarted && this.scanner.currentCharacter == '}') {
						end = this.scanner.currentPosition;
					}
					long position = (((long)this.scanner.getCurrentTokenStartPosition())<<32) + end;
					char[] prefix = this.scanner.getCurrentIdentifierSource();
					char[][][] tags = possibleTags(prefix, ls);
					if (tags != null) {
						this.completionNode = new CompletionOnJavadocTag(prefix, position, startPosition, end, tags);
					}
				}
				break;
		}
		return super.parseTag(previousPosition);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseThrows()
	 */
	protected boolean parseThrows() {
		try {
			Object typeRef = parseQualifiedName(true);
			if (this.completionNode != null) {
				this.completionNode.addCompletionFlags(CompletionOnJavadoc.EXCEPTION);
			}
			return pushThrowName(typeRef);
		} catch (InvalidInputException ex) {
			// ignore
		}
		return false;
	}

	/*
	 * Push param name reference. If it includes completion location, create and store completion node.
	 */
	protected boolean pushParamName(boolean isTypeParam) {
		if (super.pushParamName(isTypeParam)) {
			Expression expression = (Expression) astStack[astPtr];
			// See if expression is concerned by completion
			if (expression.sourceStart <= (this.cursorLocation+1) && this.cursorLocation <= expression.sourceEnd) {
				if (isTypeParam) {
					this.completionNode = new CompletionOnJavadocTypeParamReference((JavadocSingleTypeReference)expression);
				} else {
					this.completionNode = new CompletionOnJavadocParamNameReference((JavadocSingleNameReference)expression);
				}
				if (CompletionEngine.DEBUG) {
					System.out.println("	completion param="+completionNode); //$NON-NLS-1$
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Push text. If it includes completion location, then rescan line to see if there's a possible
	 * reference under the cursor location.
	 *
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushText(int, int)
	 */
	protected void pushText(int start, int end) {
		if (start <= this.cursorLocation && this.cursorLocation <= end) {
			this.scanner.resetTo(start, end);
			boolean tokenizeWhiteSpace = this.scanner.tokenizeWhiteSpace;
			this.scanner.tokenizeWhiteSpace = true;
			try {
				Object typeRef = null;
				this.pushText = true;

				// Get reference tokens
				while (!this.scanner.atEnd() && this.completionNode == null && !this.abort) {
					int token = readTokenSafely();
					switch (token) {
						case TerminalTokens.TokenNameStringLiteral :
							int strStart = 0, strEnd = 0;
							if ((strStart=this.scanner.getCurrentTokenStartPosition()+1) <= this.cursorLocation &&
								this.cursorLocation <= (strEnd=this.scanner.getCurrentTokenEndPosition()-1))
							{
								this.scanner.resetTo(strStart, strEnd);
							}
							consumeToken();
							break;
						case TerminalTokens.TokenNameERROR :
							if (this.scanner.currentCharacter == '#') { // @see ...#member
								consumeToken();
								Object member = null;
								try {
									this.scanner.tokenizeWhiteSpace = false;
									member = parseMember(typeRef);
								} catch (InvalidInputException e) {
									consumeToken();
								}
								this.scanner.tokenizeWhiteSpace = true;
								if (this.completionNode != null) {
									int flags = CompletionOnJavadoc.TEXT|CompletionOnJavadoc.ONLY_INLINE_TAG;
									if (member instanceof JavadocMessageSend) {
										JavadocMessageSend msgSend = (JavadocMessageSend) member;
										this.completionNode = new CompletionOnJavadocMessageSend(msgSend, this.memberStart, flags);
										if (CompletionEngine.DEBUG) {
											System.out.println("	new completion method="+completionNode); //$NON-NLS-1$
										}
									} else if (member instanceof JavadocAllocationExpression) {
										JavadocAllocationExpression alloc = (JavadocAllocationExpression) member;
										this.completionNode = new CompletionOnJavadocAllocationExpression(alloc, this.memberStart, flags);
										if (CompletionEngine.DEBUG) {
											System.out.println("	new completion method="+completionNode); //$NON-NLS-1$
										}
									} else {
										this.completionNode.addCompletionFlags(flags);
									}
								}
							}
							break;
						case TerminalTokens.TokenNameIdentifier :
								try {
									this.scanner.tokenizeWhiteSpace = false;
									typeRef = parseQualifiedName(true);
								} catch (InvalidInputException e) {
									consumeToken();
								}
								this.scanner.tokenizeWhiteSpace = true;
							break;
						case TerminalTokens.TokenNameAT:
							consumeToken();
							try {
								this.scanner.tokenizeWhiteSpace = false;
								int startPosition = this.scanner.getCurrentTokenStartPosition();
								parseTag(startPosition);
								if (this.completionNode != null) {
									if (this.inlineTagStarted) {
										/* May be to replace invalid @value tag inside text?
										if (this.completionNode instanceof CompletionOnJavadocSingleTypeReference) {
											CompletionOnJavadocSingleTypeReference singleTypeReference = (CompletionOnJavadocSingleTypeReference) this.completionNode;
											singleTypeReference.tagSourceStart = startPosition;
											switch (this.tagValue) {
												case TAG_VALUE_VALUE:
//													singleTypeReference.completionFlags |= ONLY_INLINE_TAG;
													if (this.sourceLevel < ClassFileConstants.JDK1_5) singleTypeReference.completionFlags |= REPLACE_TAG;
													break;
											}
										} else if (this.completionNode instanceof CompletionOnJavadocQualifiedTypeReference) {
											CompletionOnJavadocQualifiedTypeReference qualifiedTypeRef = (CompletionOnJavadocQualifiedTypeReference) this.completionNode;
											qualifiedTypeRef.tagSourceStart = startPosition;
											switch (this.tagValue) {
												case TAG_VALUE_VALUE:
													singleTypeReference.completionFlags |= ONLY_INLINE_TAG;
													if (this.sourceLevel < ClassFileConstants.JDK1_5) qualifiedTypeRef.completionFlags |= REPLACE_TAG;
													break;
											}
										}
//										*/
									} else {
										/* May be to replace non-inline tag inside text?
										if (this.completionNode instanceof CompletionOnJavadocSingleTypeReference) {
											CompletionOnJavadocSingleTypeReference singleTypeReference = (CompletionOnJavadocSingleTypeReference) this.completionNode;
											singleTypeReference.tagSourceStart = startPosition;
											switch (this.tagValue) {
												case TAG_LINK_VALUE:
												case TAG_LINKPLAIN_VALUE:
													singleTypeReference.completionFlags |= ONLY_INLINE_TAG;
												case TAG_SEE_VALUE:
													singleTypeReference.completionFlags |= REPLACE_TAG;
													break;
											}
										} else if (this.completionNode instanceof CompletionOnJavadocQualifiedTypeReference) {
											CompletionOnJavadocQualifiedTypeReference qualifiedTypeRef = (CompletionOnJavadocQualifiedTypeReference) this.completionNode;
											qualifiedTypeRef.tagSourceStart = startPosition;
											switch (this.tagValue) {
												case TAG_LINK_VALUE:
												case TAG_LINKPLAIN_VALUE:
													qualifiedTypeRef.completionFlags |= ONLY_INLINE_TAG;
												case TAG_SEE_VALUE:
													qualifiedTypeRef.completionFlags |= REPLACE_TAG;
													break;
											}
										}
//										*/
									}
								}
							} catch (InvalidInputException e) {
								consumeToken();
							}
							this.scanner.tokenizeWhiteSpace = true;
							break;
						default :
							consumeToken();
							typeRef = null;
							break;
					}
				}
			}
			finally {
				this.scanner.tokenizeWhiteSpace = tokenizeWhiteSpace;
				this.pushText = false;
			}

			// Reset position to avoid missing tokens when new line was encountered
			this.index = end;
			this.scanner.currentPosition = end;
			consumeToken();

			if (this.completionNode != null) {
				this.completionNode.addCompletionFlags(CompletionOnJavadoc.TEXT);
			}
		}
	}

	/* 
	 * Recover syntax on invalid qualified name.
	 */
	protected Object syntaxRecoverQualifiedName() throws InvalidInputException {
		int idLength = this.identifierLengthStack[this.identifierLengthPtr--];
		char[][] tokens = new char[idLength][];
		int startPtr = this.identifierPtr-idLength+1;
		System.arraycopy(this.identifierStack, startPtr, tokens, 0, idLength);
		long[] positions = new long[idLength+1];
		System.arraycopy(this.identifierPositionStack, startPtr, positions, 0, idLength);
		positions[idLength] = (((long)this.tokenPreviousPosition)<<32) + this.tokenPreviousPosition;
		this.completionNode = new CompletionOnJavadocQualifiedTypeReference(tokens, CharOperation.NO_CHAR, positions, this.tagSourceStart, this.tagSourceEnd);
		this.identifierPtr -= idLength;

		if (CompletionEngine.DEBUG) {
			System.out.println("	completion partial qualified type="+completionNode); //$NON-NLS-1$
		}
		return this.completionNode;
	}

	/* 
	 * Recover syntax on type argument in invalid method/constructor reference
	 */
	protected Object syntaxRecoverArgumentType(Object receiver, List arguments, Object argument) throws InvalidInputException {
		if (this.completionNode != null && !this.pushText) {
			this.completionNode.addCompletionFlags(CompletionOnJavadoc.BASE_TYPES);
			if (this.completionNode instanceof CompletionOnJavadocSingleTypeReference) {
				char[] token = ((CompletionOnJavadocSingleTypeReference)this.completionNode).token;
				if (token != null && token.length > 0) {
					return this.completionNode;
				}
			} else {
				return this.completionNode;
			}
		}
		if (this.completionNode instanceof CompletionOnJavadocSingleTypeReference) {
			CompletionOnJavadocSingleTypeReference singleTypeReference = (CompletionOnJavadocSingleTypeReference) this.completionNode;
			if (singleTypeReference.token != null && singleTypeReference.token.length > 0) {
				arguments.add(argument);
			}
		} else if (this.completionNode instanceof CompletionOnJavadocQualifiedTypeReference) {
			CompletionOnJavadocQualifiedTypeReference qualifiedTypeReference = (CompletionOnJavadocQualifiedTypeReference) this.completionNode;
			if (qualifiedTypeReference.tokens != null && qualifiedTypeReference.tokens.length == qualifiedTypeReference.sourcePositions.length) {
				arguments.add(argument);
			}
		}
		Object methodRef = createMethodReference(receiver, arguments);
		if (methodRef instanceof JavadocMessageSend) {
			JavadocMessageSend msgSend = (JavadocMessageSend) methodRef;
			if (this.index > this.cursorLocation) {
				msgSend.sourceEnd = this.tokenPreviousPosition-1;
			}
			this.completionNode = new CompletionOnJavadocMessageSend(msgSend, this.memberStart);
		} else if (methodRef instanceof JavadocAllocationExpression) {
			JavadocAllocationExpression allocExp = (JavadocAllocationExpression) methodRef;
			if (this.index > this.cursorLocation) {
				allocExp.sourceEnd = this.tokenPreviousPosition-1;
			}
			this.completionNode = new CompletionOnJavadocAllocationExpression(allocExp, this.memberStart);
		}
		if (CompletionEngine.DEBUG) {
			System.out.println("	completion method="+completionNode); //$NON-NLS-1$
		}
		return this.completionNode;
	}

	/*
	 * Recover syntax on empty type argument in invalid method/constructor reference
	 */
	protected Object syntaxRecoverEmptyArgumentType(Object methodRef) throws InvalidInputException {
		if (methodRef instanceof JavadocMessageSend) {
			JavadocMessageSend msgSend = (JavadocMessageSend) methodRef;
			if (this.index > this.cursorLocation) {
				msgSend.sourceEnd = this.tokenPreviousPosition-1;
			}
			this.completionNode = new CompletionOnJavadocMessageSend(msgSend, this.memberStart);
		} else if (methodRef instanceof JavadocAllocationExpression) {
			JavadocAllocationExpression allocExp = (JavadocAllocationExpression) methodRef;
			if (this.index > this.cursorLocation) {
				allocExp.sourceEnd = this.tokenPreviousPosition-1;
			}
			this.completionNode = new CompletionOnJavadocAllocationExpression(allocExp, this.memberStart);
		}
		if (CompletionEngine.DEBUG) {
			System.out.println("	completion method="+completionNode); //$NON-NLS-1$
		}
		return this.completionNode;
	}

	/*
	 * Store completion node into doc comment.
	 */
	protected void updateDocComment() {
		super.updateDocComment();
		getCompletionParser().assistNodeParent = this.docComment;
		if (completionNode instanceof Expression) {
			getCompletionParser().assistNode = (ASTNode) this.completionNode;
			getCompletionJavadoc().completionNode = (Expression) completionNode;
		}
	}
}
