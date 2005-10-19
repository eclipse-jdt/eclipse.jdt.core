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
package org.eclipse.jdt.internal.compiler.parser;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Parser specialized for decoding javadoc comments
 */
public class JavadocParser extends AbstractCommentParser {

	// Public fields
	public Javadoc docComment;
	
	// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
	// Store param references for tag with invalid syntax
	private int invalidParamReferencesPtr = -1;
	private ASTNode[] invalidParamReferencesStack;

	public JavadocParser(Parser sourceParser) {
		super(sourceParser);
		this.kind = COMPIL_PARSER;
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in javadoc comment.
	 * 
	 * If javadoc checking is enabled, will also construct an Javadoc node, which will be stored into Parser.javadoc
	 * slot for being consumed later on.
	 */
	public boolean checkDeprecation(int commentPtr) {

		// Store javadoc positions
		this.javadocStart = this.sourceParser.scanner.commentStarts[commentPtr];
		this.javadocEnd = this.sourceParser.scanner.commentStops[commentPtr]-1;
		this.firstTagPosition = this.sourceParser.scanner.commentTagStarts[commentPtr];

		// Init javadoc if necessary
		if (this.checkDocComment) {
			this.docComment = new Javadoc(javadocStart, javadocEnd);
		} else {
			this.docComment = null;
		}
		
		// If there's no tag in javadoc, return without parsing it
		if (this.firstTagPosition == 0) {
			switch (this.kind) {
				case COMPIL_PARSER:
				case SOURCE_PARSER:
					return false;
			}
		}

		// Parse
		try {
			this.source = this.sourceParser.scanner.source;
			if (this.checkDocComment) {
				// Initialization
				this.scanner.lineEnds = this.sourceParser.scanner.lineEnds;
				this.scanner.linePtr = this.sourceParser.scanner.linePtr;
				this.lineEnds = this.scanner.lineEnds;
				commentParse();
			} else {
				
				// Parse comment
				int firstLineNumber = this.sourceParser.scanner.getLineNumber(javadocStart);
				int lastLineNumber = this.sourceParser.scanner.getLineNumber(javadocEnd);
				this.index = javadocStart +3;
	
				// scan line per line, since tags must be at beginning of lines only
				this.deprecated = false;
				nextLine : for (int line = firstLineNumber; line <= lastLineNumber; line++) {
					int lineStart = line == firstLineNumber
							? javadocStart + 3 // skip leading /**
							: this.sourceParser.scanner.getLineStart(line);
					this.index = lineStart;
					this.lineEnd = line == lastLineNumber
							? javadocEnd - 2 // remove trailing * /
							: this.sourceParser.scanner.getLineEnd(line);
					nextCharacter : while (this.index < this.lineEnd) {
						char c = readChar(); // consider unicodes
						switch (c) {
							case '*' :
							case '\u000c' :	/* FORM FEED               */
							case ' ' :			/* SPACE                   */
							case '\t' :			/* HORIZONTAL TABULATION   */
							case '\n' :			/* LINE FEED   */
							case '\r' :			/* CR */
								// do nothing for space or '*' characters
						        continue nextCharacter;
						    case '@' :
						    	parseSimpleTag();
						    	if (this.tagValue == TAG_DEPRECATED_VALUE) {
						    		if (this.abort) break nextCharacter;
						    	}
						}
			        	continue nextLine;
					}
				}
				return this.deprecated;
			}
		} finally {
			this.source = null; // release source as soon as finished
		}
		return this.deprecated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createArgumentReference(char[], java.lang.Object, int)
	 */
	protected Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object typeRef, long[] dimPositions, long argNamePos) throws InvalidInputException {
		try {
			TypeReference argTypeRef = (TypeReference) typeRef;
			if (dim > 0) {
				long pos = (((long) argTypeRef.sourceStart) << 32) + argTypeRef.sourceEnd;
				if (typeRef instanceof JavadocSingleTypeReference) {
					JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) typeRef;
					argTypeRef = new JavadocArraySingleTypeReference(singleRef.token, dim, pos);
				} else {
					JavadocQualifiedTypeReference qualifRef = (JavadocQualifiedTypeReference) typeRef;
					argTypeRef = new JavadocArrayQualifiedTypeReference(qualifRef, dim);
				}
			}
			int argEnd = argTypeRef.sourceEnd;
			if (dim > 0) {
				argEnd = (int) dimPositions[dim-1];
				if (isVarargs) {
					argTypeRef.bits |= ASTNode.IsVarArgs; // set isVarArgs
				}
			}
			if (argNamePos >= 0) argEnd = (int) argNamePos;
			return new JavadocArgumentExpression(name, argTypeRef.sourceStart, argEnd, argTypeRef);
		}
		catch (ClassCastException ex) {
			throw new InvalidInputException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createFieldReference()
	 */
	protected Object createFieldReference(Object receiver) throws InvalidInputException {
		try {
			// Get receiver type
			TypeReference typeRef = (TypeReference) receiver;
			if (typeRef == null) {
				char[] name = this.sourceParser.compilationUnit.getMainTypeName();
				typeRef = new JavadocImplicitTypeReference(name, this.memberStart);
			}
			// Create field
			JavadocFieldReference field = new JavadocFieldReference(this.identifierStack[0], this.identifierPositionStack[0]);
			field.receiver = typeRef;
			field.tagSourceStart = this.tagSourceStart;
			field.tagSourceEnd = this.tagSourceEnd;
			field.tagValue = this.tagValue;
			return field;
		}
		catch (ClassCastException ex) {
			throw new InvalidInputException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createMethodReference(java.lang.Object[])
	 */
	protected Object createMethodReference(Object receiver, List arguments) throws InvalidInputException {
		try {
			// Get receiver type
			TypeReference typeRef = (TypeReference) receiver;
			// Decide whether we have a constructor or not
			boolean isConstructor = false;
			if (typeRef == null) {
				char[] name = this.sourceParser.compilationUnit.getMainTypeName();
				TypeDeclaration typeDecl = getParsedTypeDeclaration();
				if (typeDecl != null) {
					name = typeDecl.name;
				}
				isConstructor = CharOperation.equals(this.identifierStack[0], name);
				typeRef = new JavadocImplicitTypeReference(name, this.memberStart);
			} else {
				char[] name = null;
				if (typeRef instanceof JavadocSingleTypeReference) {
					name = ((JavadocSingleTypeReference)typeRef).token;
				} else if (typeRef instanceof JavadocQualifiedTypeReference) {
					char[][] tokens = ((JavadocQualifiedTypeReference)typeRef).tokens;
					name = tokens[tokens.length-1];
				} else {
					throw new InvalidInputException();
				}
				isConstructor = CharOperation.equals(this.identifierStack[0], name);
			}
			// Create node
			if (arguments == null) {
				if (isConstructor) {
					JavadocAllocationExpression alloc = new JavadocAllocationExpression(this.identifierPositionStack[0]);
					alloc.type = typeRef;
					alloc.tagValue = this.tagValue;
					alloc.sourceEnd = this.scanner.getCurrentTokenEndPosition();
					return alloc;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0]);
					msg.receiver = typeRef;
					msg.tagValue = this.tagValue;
					msg.sourceEnd = this.scanner.getCurrentTokenEndPosition();
					return msg;
				}
			} else {
				JavadocArgumentExpression[] expressions = new JavadocArgumentExpression[arguments.size()];
				arguments.toArray(expressions);
				if (isConstructor) {
					JavadocAllocationExpression alloc = new JavadocAllocationExpression(this.identifierPositionStack[0]);
					alloc.arguments = expressions;
					alloc.type = typeRef;
					alloc.tagValue = this.tagValue;
					alloc.sourceEnd = this.scanner.getCurrentTokenEndPosition();
					return alloc;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0], expressions);
					msg.receiver = typeRef;
					msg.tagValue = this.tagValue;
					msg.sourceEnd = this.scanner.getCurrentTokenEndPosition();
					return msg;
				}
			}
		}
		catch (ClassCastException ex) {
			throw new InvalidInputException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createReturnStatement()
	 */
	protected Object createReturnStatement() {
		return new JavadocReturnStatement(this.scanner.getCurrentTokenStartPosition(),
					this.scanner.getCurrentTokenEndPosition());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseTagName()
	 */
	protected void createTag() {
		this.tagValue = TAG_OTHERS_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createTypeReference()
	 */
	protected Object createTypeReference(int primitiveToken) {
		TypeReference typeRef = null;
		int size = this.identifierLengthStack[this.identifierLengthPtr--];
		if (size == 1) { // Single Type ref
			typeRef = new JavadocSingleTypeReference(
						this.identifierStack[this.identifierPtr],
						this.identifierPositionStack[this.identifierPtr],
						this.tagSourceStart,
						this.tagSourceEnd);
		} else if (size > 1) { // Qualified Type ref
			char[][] tokens = new char[size][];
			System.arraycopy(this.identifierStack, this.identifierPtr - size + 1, tokens, 0, size);
			long[] positions = new long[size];
			System.arraycopy(this.identifierPositionStack, this.identifierPtr - size + 1, positions, 0, size);
			typeRef = new JavadocQualifiedTypeReference(tokens, positions, this.tagSourceStart, this.tagSourceEnd);
		}
		this.identifierPtr -= size;
		return typeRef;
	}

	/*
	 * Get current parsed type declaration.
	 */
	protected TypeDeclaration getParsedTypeDeclaration() {
		int ptr = this.sourceParser.astPtr;
		while (ptr >= 0) {
			Object node = this.sourceParser.astStack[ptr];
			if (node instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) node;
				if (typeDecl.bodyEnd == 0) { // type declaration currenly parsed
					return typeDecl;
				}
			}
			ptr--;
		}
		return null;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected boolean parseReturn() {
		if (this.returnStatement == null) {
			this.returnStatement = createReturnStatement();
			return true;
		}
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocDuplicatedReturnTag(
				this.scanner.getCurrentTokenStartPosition(),
				this.scanner.getCurrentTokenEndPosition());
		return false;
	}


	protected void parseSimpleTag() {
		
		// Read first char
		// readChar() code is inlined to balance additional method call in checkDeprectation(int)
		char first = this.source[this.index++];
		if (first == '\\' && this.source[this.index] == 'u') {
			int c1, c2, c3, c4;
			int pos = this.index;
			this.index++;
			while (this.source[this.index] == 'u')
				this.index++;
			if (!(((c1 = Character.getNumericValue(this.source[this.index++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(this.source[this.index++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(this.source[this.index++])) > 15 || c3 < 0) || ((c4 = Character.getNumericValue(this.source[this.index++])) > 15 || c4 < 0))) {
				first = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			} else {
				this.index = pos;
			}
		}

		// switch on first tag char
		switch (first) {
			case 'd':
		        if ((readChar() == 'e') &&
						(readChar() == 'p') && (readChar() == 'r') &&
						(readChar() == 'e') && (readChar() == 'c') &&
						(readChar() == 'a') && (readChar() == 't') &&
						(readChar() == 'e') && (readChar() == 'd')) {
					// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
					char c = readChar();
					if (Character.isWhitespace(c) || c == '*') {
						this.abort = true;
			    		this.deprecated = true;
						this.tagValue = TAG_DEPRECATED_VALUE;
					}
		        }
				break;
		}
	}

	protected boolean parseTag(int previousPosition) throws InvalidInputException {
		boolean valid = false;
	
		// Read tag name
		int token = readTokenAndConsume();
		if (this.index >= this.scanner.eofPosition) {
			this.tagSourceStart = previousPosition;
			this.tagSourceEnd = this.tokenPreviousPosition;
			if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidTag(this.tagSourceStart, this.tagSourceEnd);
			return false;
		}
		this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
		this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
	
		// Try to get tag name other than java identifier
		// (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51660)
		char pc = peekChar();
		boolean validTag = false;
		switch (token) {
			case TerminalTokens.TokenNameIdentifier:
			case TerminalTokens.TokenNamereturn:
			case TerminalTokens.TokenNamethrows:
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
				validTag= true;
		}
		tagNameToken: while (token != TerminalTokens.TokenNameEOF && this.index < this.scanner.eofPosition) {
			// !, ", #, %, &, ', -, :, <, >, * chars and spaces are not allowed in tag names
			switch (pc) {
				case '}':
				case '*': // break for '*' as this is perhaps the end of comment (bug 65288)
					break tagNameToken;
				case '!':
				case '#':
				case '%':
				case '&':
				case '\'':
				case '"':
				case ':':
				// case '-': allowed in tag names as this character is often used in doclets (bug 68087)
				case '<':
				case '>':
					readChar();
					this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
					validTag = false;
					break;
				default:
					if (pc == ' ' || Character.isWhitespace(pc)) break tagNameToken;
					this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
					token = readTokenAndConsume();
			}
			pc = peekChar();
		}
		if (!validTag) {
			this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
			if (this.reportProblems) this.sourceParser.problemReporter().javadocInvalidTag(this.tagSourceStart, this.tagSourceEnd);
			return false;
		}
		int length = this.tagSourceEnd-this.tagSourceStart+1;
		char[] tag = new char[length];
		System.arraycopy(this.source, this.tagSourceStart, tag, 0, length);
		this.index = this.tagSourceEnd+1;
		this.scanner.currentPosition = this.tagSourceEnd+1;

		// Decide which parse to perform depending on tag name
		this.tagValue = NO_TAG_VALUE;
		switch (token) {
			case TerminalTokens.TokenNameIdentifier :
				if (length == 0) break; // may happen for some parser (completion for example)
				switch (tag[0]) {
					case 'c':
						if (CharOperation.equals(tag, TAG_CATEGORY)) {
							this.tagValue = TAG_CATEGORY_VALUE;
							valid = parseIdentifierTag(false); // TODO (frederic) reconsider parameter value when @category will be significant in spec
						}
						break;
					case 'd':
						if (CharOperation.equals(tag, TAG_DEPRECATED)) {
							this.deprecated = true;
							valid = true;
							this.tagValue = TAG_DEPRECATED_VALUE;
						}
						break;
					case 'e':
						if (CharOperation.equals(tag, TAG_EXCEPTION)) {
							this.tagValue = TAG_EXCEPTION_VALUE;
							valid = parseThrows();
						}
						break;
					case 'i':
						if (CharOperation.equals(tag, TAG_INHERITDOC)) {
							// inhibits inherited flag when tags have been already stored
							// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51606
							// Note that for DOM_PARSER, nodes stack may be not empty even no '@' tag
							// was encountered in comment. But it cannot be the case for COMPILER_PARSER
							// and so is enough as it is only this parser which signals the missing tag warnings...
							if (this.astPtr==-1) {
								this.inheritedPositions = (((long) this.tagSourceStart) << 32) + this.tagSourceEnd;
							}
							valid = true;
							this.tagValue = TAG_INHERITDOC_VALUE;
						}
						break;
					case 'l':
						if (CharOperation.equals(tag, TAG_LINK)) {
							this.tagValue = TAG_LINK_VALUE;
							if (this.inlineTagStarted || this.kind == COMPLETION_PARSER) {
								valid= parseReference();
							} else {
								// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
								// Cannot have @link outside inline comment
								valid = false;
								if (this.sourceParser != null)
									this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
							}
						} else if (CharOperation.equals(tag, TAG_LINKPLAIN)) {
							this.tagValue = TAG_LINKPLAIN_VALUE;
							if (this.inlineTagStarted) {
								valid = parseReference();
							} else {
								valid = false;
								if (this.sourceParser != null)
									this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
							}
						}
						break;
					case 'p':
						if (CharOperation.equals(tag, TAG_PARAM)) {
							this.tagValue = TAG_PARAM_VALUE;
							valid = parseParam();
						}
						break;
					case 's':
						if (CharOperation.equals(tag, TAG_SEE)) {
							if (this.inlineTagStarted) {
								// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53290
								// Cannot have @see inside inline comment
								valid = false;
								if (this.sourceParser != null)
									this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
							} else {
								this.tagValue = TAG_SEE_VALUE;
								valid = parseReference();
							}
						}
						break;
					case 'v':
						if (this.sourceLevel >= ClassFileConstants.JDK1_5 && CharOperation.equals(tag, TAG_VALUE)) {
							this.tagValue = TAG_VALUE_VALUE;
							if (this.inlineTagStarted) {
								valid = parseReference();
							} else {
								valid = false;
								if (this.sourceParser != null)
									this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
							}
						} else {
							createTag();
						}
						break;
					default:
						createTag();
						break;
				}
				break;
			case TerminalTokens.TokenNamereturn :
				this.tagValue = TAG_RETURN_VALUE;
				valid = parseReturn();
				/* verify characters after return tag (we're expecting text description)
				if(!verifyCharsAfterReturnTag(this.index)) {
					if (this.sourceParser != null) {
						int end = this.starPosition == -1 || this.lineEnd<this.starPosition ? this.lineEnd : this.starPosition;
						this.sourceParser.problemReporter().javadocEmptyReturnTag(this.tagSourceStart, end);
					}
				}
				*/
				break;
			case TerminalTokens.TokenNamethrows :
				this.tagValue = TAG_THROWS_VALUE;
				valid = parseThrows();
				break;
		}
		this.textStart = this.index;
		return valid;
	}

	/*
	 * Push a param name in ast node stack.
	 */
	protected boolean pushParamName(boolean isTypeParam) {
		// Create param reference
		ASTNode nameRef = null;
		if (isTypeParam) {
			JavadocSingleTypeReference ref = new JavadocSingleTypeReference(this.identifierStack[1],
				this.identifierPositionStack[1],
				this.tagSourceStart,
				this.tagSourceEnd);
			nameRef = ref;
		} else {
			JavadocSingleNameReference ref = new JavadocSingleNameReference(this.identifierStack[0],
				this.identifierPositionStack[0],
				this.tagSourceStart,
				this.tagSourceEnd);
			nameRef = ref;
		}
		// Push ref on stack
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(nameRef, true);
		} else {
			// Verify that no @throws has been declared before
			if (!isTypeParam) { // do not verify for type parameters as @throws may be invalid tag (when declared in class)
				for (int i=THROWS_TAG_EXPECTED_ORDER; i<=this.astLengthPtr; i+=ORDERED_TAGS_NUMBER) {
					if (this.astLengthStack[i] != 0) {
						if (this.sourceParser != null) this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
						// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
						// store invalid param references in specific array
						if (this.invalidParamReferencesPtr == -1l) {
							this.invalidParamReferencesStack = new JavadocSingleNameReference[10];
						}
						int stackLength = this.invalidParamReferencesStack.length;
						if (++this.invalidParamReferencesPtr >= stackLength) {
							System.arraycopy(
								this.invalidParamReferencesStack, 0,
								this.invalidParamReferencesStack = new JavadocSingleNameReference[stackLength + AstStackIncrement], 0,
								stackLength);
						}
						this.invalidParamReferencesStack[this.invalidParamReferencesPtr] = nameRef;
						return false;
					}
				}
			}
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push another param name
					pushOnAstStack(nameRef, false);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push new param name
					pushOnAstStack(nameRef, true);
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
	protected boolean pushSeeRef(Object statement) {
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

	/*
	 * Push a throws type ref in ast node stack.
	 */
	protected boolean pushThrowName(Object typeRef) {
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

	/*
	 * Refresh return statement
	 */
	protected void refreshReturnStatement() {
		((JavadocReturnStatement) this.returnStatement).empty = false;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("check javadoc: ").append(this.checkDocComment).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("javadoc: ").append(this.docComment).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(super.toString());
		return buffer.toString();
	}

	/*
	 * Fill associated comment fields with ast nodes information stored in stack.
	 */
	protected void updateDocComment() {
		
		// Set inherited positions
		this.docComment.inheritedPositions = this.inheritedPositions;

		// Set return node if present
		if (this.returnStatement != null) {
			this.docComment.returnStatement = (JavadocReturnStatement) this.returnStatement;
		}
		
		// Copy array of invalid syntax param tags
		if (this.invalidParamReferencesPtr >= 0) {
			this.docComment.invalidParameters = new JavadocSingleNameReference[this.invalidParamReferencesPtr+1];
			System.arraycopy(this.invalidParamReferencesStack, 0, this.docComment.invalidParameters, 0, this.invalidParamReferencesPtr+1);
		}

		// If no nodes stored return
		if (this.astLengthPtr == -1) {
			return;
		}

		// Initialize arrays
		int[] sizes = new int[ORDERED_TAGS_NUMBER];
		for (int i=0; i<=this.astLengthPtr; i++) {
			sizes[i%ORDERED_TAGS_NUMBER] += this.astLengthStack[i];
		}
		this.docComment.seeReferences = new Expression[sizes[SEE_TAG_EXPECTED_ORDER]];
		this.docComment.exceptionReferences = new TypeReference[sizes[THROWS_TAG_EXPECTED_ORDER]];
		int paramRefPtr = sizes[PARAM_TAG_EXPECTED_ORDER];
		this.docComment.paramReferences = new JavadocSingleNameReference[paramRefPtr];
		int paramTypeParamPtr = sizes[PARAM_TAG_EXPECTED_ORDER];
		this.docComment.paramTypeParameters = new JavadocSingleTypeReference[paramTypeParamPtr];

		// Store nodes in arrays
		while (this.astLengthPtr >= 0) {
			int ptr = this.astLengthPtr % ORDERED_TAGS_NUMBER;
			// Starting with the stack top, so get references (eg. Expression) coming from @see declarations
			switch(ptr) {
				case SEE_TAG_EXPECTED_ORDER:
					int size = this.astLengthStack[this.astLengthPtr--];
					for (int i=0; i<size; i++) {
						this.docComment.seeReferences[--sizes[ptr]] = (Expression) this.astStack[this.astPtr--];
					}
					break;

				// Then continuing with class names (eg. TypeReference) coming from @throw/@exception declarations
				case THROWS_TAG_EXPECTED_ORDER:
					size = this.astLengthStack[this.astLengthPtr--];
					for (int i=0; i<size; i++) {
						this.docComment.exceptionReferences[--sizes[ptr]] = (TypeReference) this.astStack[this.astPtr--];
					}
					break;

				// Finally, finishing with parameters nales (ie. Argument) coming from @param declaration
				case PARAM_TAG_EXPECTED_ORDER:
					size = this.astLengthStack[this.astLengthPtr--];
					for (int i=0; i<size; i++) {
						Expression reference = (Expression) this.astStack[this.astPtr--];
						if (reference instanceof JavadocSingleNameReference)
							this.docComment.paramReferences[--paramRefPtr] = (JavadocSingleNameReference) reference;
						else if (reference instanceof JavadocSingleTypeReference)
							this.docComment.paramTypeParameters[--paramTypeParamPtr] = (JavadocSingleTypeReference) reference;
					}
					break;
			}
		}
		
		// Resize param tag references arrays
		if (paramRefPtr == 0) { // there's no type parameters references
			this.docComment.paramTypeParameters = null;
		} else if (paramTypeParamPtr == 0) { // there's no names references
			this.docComment.paramReferences = null;
		} else { // there both of references => resize arrays
			int size = sizes[PARAM_TAG_EXPECTED_ORDER];
			System.arraycopy(this.docComment.paramReferences, paramRefPtr, this.docComment.paramReferences = new JavadocSingleNameReference[size - paramRefPtr], 0, size - paramRefPtr);
			System.arraycopy(this.docComment.paramTypeParameters, paramTypeParamPtr, this.docComment.paramTypeParameters = new JavadocSingleTypeReference[size - paramTypeParamPtr], 0, size - paramTypeParamPtr);
		}
	}
}
