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
	private int invParamsPtr = -1;
	private JavadocSingleNameReference[] invParamsStack;

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
				parseComment(javadocStart, javadocEnd);
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
					JavadocAllocationExpression expr = new JavadocAllocationExpression(this.identifierPositionStack[0]);
					expr.type = typeRef;
					return expr;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0]);
					msg.receiver = typeRef;
					return msg;
				}
			} else {
				JavadocArgumentExpression[] expressions = new JavadocArgumentExpression[arguments.size()];
				arguments.toArray(expressions);
				if (isConstructor) {
					JavadocAllocationExpression alloc = new JavadocAllocationExpression(this.identifierPositionStack[0]);
					alloc.arguments = expressions;
					alloc.type = typeRef;
					return alloc;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0], expressions);
					msg.receiver = typeRef;
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
			return true;
		}
		if (this.sourceParser != null) this.sourceParser.problemReporter().javadocDuplicatedReturnTag(
				this.scanner.getCurrentTokenStartPosition(),
				this.scanner.getCurrentTokenEndPosition());
		return false;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected boolean parseTag() {
		return true;
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
					// store param references in specific array
					if (this.invParamsPtr == -1l) {
						this.invParamsStack = new JavadocSingleNameReference[10];
					}
					int stackLength = this.invParamsStack.length;
					if (++this.invParamsPtr >= stackLength) {
						System.arraycopy(
							this.invParamsStack, 0,
							this.invParamsStack = new JavadocSingleNameReference[stackLength + AstStackIncrement], 0,
							stackLength);
					}
					this.invParamsStack[this.invParamsPtr] = nameRef;
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
	protected boolean pushSeeRef(Object statement, boolean plain) {
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
		// compiler does not matter of text
	}

	/*
	 * Push a throws type ref in ast node stack.
	 */
	protected boolean pushThrowName(Object typeRef, boolean real) {
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
		if (this.invParamsPtr >= 0) {
			this.docComment.invalidParameters = new JavadocSingleNameReference[this.invParamsPtr+1];
			System.arraycopy(this.invParamsStack, 0, this.docComment.invalidParameters, 0, this.invParamsPtr+1);
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
