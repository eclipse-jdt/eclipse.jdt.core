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

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImplicitDocTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArgumentExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocArraySingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.JavadocQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

/**
 * Parser specialized for decoding javadoc comments
 */
public class JavadocParser extends AbstractCommentParser {

	// Public fields
	public Javadoc docComment;
	
	// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
	// Store param references for tag with invalid syntax
	private int invalidParamReferencesPtr = -1;
	private JavadocSingleNameReference[] invalidParamReferencesStack;

	// Store current tag stack pointer
	private int currentAstPtr= -2;

	public JavadocParser(Parser sourceParser) {
		super(sourceParser);
		this.checkDocComment = this.sourceParser.options.docCommentSupport;
		this.kind = COMPIL_PARSER;
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in javadoc comment.
	 * 
	 * If javadoc checking is enabled, will also construct an Javadoc node, which will be stored into Parser.javadoc
	 * slot for being consumed later on.
	 */
	public boolean checkDeprecation(int javadocStart, int javadocEnd) {

		try {
			this.source = this.sourceParser.scanner.source;
			this.index = javadocStart +3;
			this.endComment = javadocEnd - 2;
			if (this.checkDocComment) {
				// Initialization
				this.scanner.lineEnds = this.sourceParser.scanner.lineEnds;
				this.scanner.linePtr = this.sourceParser.scanner.linePtr;
				this.lineEnds = this.scanner.lineEnds;
				this.docComment = new Javadoc(javadocStart, javadocEnd);
				commentParse(javadocStart, javadocEnd);
			} else {
				// Init javadoc if necessary
				if (this.sourceParser.options.getSeverity(CompilerOptions.MissingJavadocComments) != ProblemSeverities.Ignore) {
					this.docComment = new Javadoc(javadocStart, javadocEnd);
				} else {
					this.docComment = null;
				}
				
				// Parse comment
				int firstLineNumber = this.sourceParser.scanner.getLineNumber(javadocStart);
				int lastLineNumber = this.sourceParser.scanner.getLineNumber(javadocEnd);
	
				// scan line per line, since tags must be at beginning of lines only
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
						    default : 
						        if (Character.isWhitespace(c)) {
						            continue nextCharacter;
						        }
						        break;
						    case '*' :
						        continue nextCharacter;
						    case '@' :
						        if ((readChar() == 'd') && (readChar() == 'e') &&
										(readChar() == 'p') && (readChar() == 'r') &&
										(readChar() == 'e') && (readChar() == 'c') &&
										(readChar() == 'a') && (readChar() == 't') &&
										(readChar() == 'e') && (readChar() == 'd')) {
									// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
									c = readChar();
									if (Character.isWhitespace(c) || c == '*') {
										return true;
									}
						        }
						}
			        	continue nextLine;
					}
				}
				return false;
			}
		} finally {
			this.source = null; // release source as soon as finished
		}
		return this.deprecated;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("check javadoc: ").append(this.checkDocComment).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("javadoc: ").append(this.docComment).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(super.toString());
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createArgumentReference(char[], java.lang.Object, int)
	 */
	protected Object createArgumentReference(char[] name, int dim, Object typeRef, long[] dimPositions, long argNamePos) throws InvalidInputException {
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
			if (dim > 0) argEnd = (int) dimPositions[dim-1];
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
				char[] name = this.sourceParser.compilationUnit.compilationResult.compilationUnit.getMainTypeName();
				typeRef = new ImplicitDocTypeReference(name, this.memberStart);
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
				char[] name = this.sourceParser.compilationUnit.compilationResult.compilationUnit.getMainTypeName();
				isConstructor = CharOperation.equals(this.identifierStack[0], name);
				typeRef = new ImplicitDocTypeReference(name, this.memberStart);
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
					return alloc;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0]);
					msg.receiver = typeRef;
					msg.tagValue = this.tagValue;
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
					return alloc;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0], expressions);
					msg.receiver = typeRef;
					msg.tagValue = this.tagValue;
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
					this.scanner.getCurrentTokenEndPosition(),
					this.scanner.getRawTokenSourceEnd());
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
	 * Parse @return tag declaration
	 */
	protected boolean parseReturn() {
		if (this.returnStatement == null) {
			this.returnStatement = createReturnStatement();
			this.currentAstPtr = this.astPtr;
			return true;
		}
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocDuplicatedReturnTag(
				this.scanner.getCurrentTokenStartPosition(),
				this.scanner.getCurrentTokenEndPosition());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseTag(int)
	 */
	protected boolean parseTag(int previousPosition) throws InvalidInputException {
		boolean valid = false;

		// In case of previous return tag, set it to not empty if parsing an inline tag
		if (this.currentAstPtr != -2 && this.returnStatement != null) {
			this.currentAstPtr = -2;
			JavadocReturnStatement javadocReturn = (JavadocReturnStatement) this.returnStatement;
			javadocReturn.empty = javadocReturn.empty && !this.inlineTagStarted;
		}

		// Read tag name
		int token = readTokenAndConsume();
		this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
		this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
		char[] tag = this.scanner.getCurrentIdentifierSource(); // first token is either an identifier or a keyword

		// Decide which parse to perform depending on tag name
		this.tagValue = NO_TAG_VALUE;
		switch (token) {
			case TerminalTokens.TokenNameIdentifier :
				switch (tag[0]) {
					case 'd':
						if (CharOperation.equals(tag, TAG_DEPRECATED)) {
							this.deprecated = true;
							valid = true;
							this.tagValue = TAG_DEPRECATED_VALUE;
						}
					break;
					case 'i':
						if (CharOperation.equals(tag, TAG_INHERITDOC)) {
							// inhibits inherited flag when tags have been already stored
							// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51606
							// Note that for DOM_PARSER, nodes stack may be not empty even no '@' tag
							// was encountered in comment. But it cannot be the case for COMPILER_PARSER
							// and so is enough as it is only this parser which signals the missing tag warnings...
							this.inherited = this.astPtr==-1;
							valid = true;
							this.tagValue = TAG_INHERITDOC_VALUE;
						}
					break;
					case 'p':
						if (CharOperation.equals(tag, TAG_PARAM)) {
							this.tagValue = TAG_PARAM_VALUE;
							valid = parseParam();
						}
					break;
					case 'e':
						if (CharOperation.equals(tag, TAG_EXCEPTION)) {
							this.tagValue = TAG_EXCEPTION_VALUE;
							valid = parseThrows();
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
					case 'l':
						if (CharOperation.equals(tag, TAG_LINK)) {
							this.tagValue = TAG_LINK_VALUE;
							if (this.inlineTagStarted) {
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
					case 'v':
						if (CharOperation.equals(tag, TAG_VALUE) && this.sourceParser != null && this.sourceParser.options.sourceLevel >= ClassFileConstants.JDK1_5) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#parseTagName()
	 */
	protected void createTag() {
		this.tagValue = TAG_OTHERS_VALUE;
	}

	/*
	 * Push a param name in ast node stack.
	 */
	protected boolean pushParamName() {
		// Create name reference
		JavadocSingleNameReference nameRef = new JavadocSingleNameReference(this.scanner.getCurrentIdentifierSource(),
				this.scanner.getCurrentTokenStartPosition(),
				this.scanner.getCurrentTokenEndPosition());
		nameRef.tagSourceStart = this.tagSourceStart;
		nameRef.tagSourceEnd = this.tagSourceEnd;
		// Push ref on stack
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(nameRef, true);
		} else {
			// Verify that no @throws has been declared before
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushText(int, int)
	 */
	protected void pushText(int start, int end) {
		// In case of previous return tag, verify that text make it not empty
		if (this.currentAstPtr != -2 && this.returnStatement != null) {
			int position = this.index;
			this.index = start;
			boolean empty = true;
			boolean star = false;
			char ch = readChar();
			// Look for first character other than white or '*'
			if (Character.isWhitespace(ch) || start>(this.tagSourceEnd+1)) {
				while (this.index <= end && empty) {
					if (!star) {
						empty = Character.isWhitespace(ch) || ch == '*';
						star = ch == '*';
					} else if (ch != '*') {
						empty = false;
						break;
					}
					ch = readChar();
				}
			}
			// Store result in previous return tag
			((JavadocReturnStatement)this.returnStatement).empty = empty;
			// Reset position and current ast ptr if we are on a different tag than previous return one
			this.index = position;
			if (this.currentAstPtr != this.astPtr) {
				this.currentAstPtr = -2;
			}
		}
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
	 * Fill associated comment fields with ast nodes information stored in stack.
	 */
	protected void updateDocComment() {
		
		// Set inherited flag
		this.docComment.inherited = this.inherited;

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
		this.docComment.references = new Expression[sizes[SEE_TAG_EXPECTED_ORDER]];
		this.docComment.thrownExceptions = new TypeReference[sizes[THROWS_TAG_EXPECTED_ORDER]];
		this.docComment.parameters = new JavadocSingleNameReference[sizes[PARAM_TAG_EXPECTED_ORDER]];

		// Store nodes in arrays
		while (this.astLengthPtr >= 0) {
			int ptr = this.astLengthPtr % ORDERED_TAGS_NUMBER;
			// Starting with the stack top, so get references (eg. Expression) coming from @see declarations
			if (ptr == SEE_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.docComment.references[--sizes[ptr]] = (Expression) this.astStack[this.astPtr--];
				}
			}

			// Then continuing with class names (eg. TypeReference) coming from @throw/@exception declarations
			else if (ptr == THROWS_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.docComment.thrownExceptions[--sizes[ptr]] = (TypeReference) this.astStack[this.astPtr--];
				}
			}

			// Finally, finishing with parameters nales (ie. Argument) coming from @param declaration
			else if (ptr == PARAM_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.docComment.parameters[--sizes[ptr]] = (JavadocSingleNameReference) this.astStack[this.astPtr--];
				}
			}
		}
	}
}
