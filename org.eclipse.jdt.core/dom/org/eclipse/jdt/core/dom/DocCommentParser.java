/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Internal parser used for decoding doc comments.
 * 
 * @since 3.0
 */
class DocCommentParser extends AbstractCommentParser {

	// recognized tags
	public static final String STR_TAG_DEPRECATED = "@deprecated"; //$NON-NLS-1$
	public static final String STR_TAG_PARAM = "@param"; //$NON-NLS-1$
	public static final String STR_TAG_RETURN = "@return"; //$NON-NLS-1$
	public static final String STR_TAG_THROWS = "@throws"; //$NON-NLS-1$
	public static final String STR_TAG_EXCEPTION = "@exception"; //$NON-NLS-1$
	public static final String STR_TAG_SEE = "@see"; //$NON-NLS-1$
	public static final String STR_TAG_LINK = "@link"; //$NON-NLS-1$
	public static final String STR_TAG_LINKPLAIN = "@linkplain"; //$NON-NLS-1$
	public static final String STR_TAG_INHERITDOC = "@inheritDoc"; //$NON-NLS-1$

	// Public fields
	private Javadoc docComment;
	private AST ast;
	
	// Private fields

	DocCommentParser(AST ast, Scanner scanner) {
		super(null);
		this.ast = ast;
		this.scanner = scanner;
		this.kind = DOM_PARSER;
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in annotation.
	 * 
	 * If annotation checking is enabled, will also construct an Annotation node, which will be stored into Parser.annotation
	 * slot for being consumed later on.
	 */
	public Javadoc parse(int[] positions) {
		return parse(positions[0], positions[1]-positions[0]);
	}
	public Javadoc parse(int start, int length) {

		// Init
		this.source = this.scanner.source;
		this.lineEnds = this.scanner.lineEnds;
		this.docComment = this.ast.newJavadoc();
		
		// Parse
		parseComment(start, start+length-1);
		this.docComment.setSourceRange(start, length);
		setComment(start, length);  // backward compatibility
		return this.docComment;
	}

	/**
	 * Sets the comment starting at the given position and with the given length.
	 * <p>
	 * Note the only purpose of this method is to hide deprecated warnings.
	 * @deprecated mark deprecated to hide deprecated usage
	 */
	private void setComment(int start, int length) {
		this.docComment.setComment(new String(this.source, start, length));
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("javadoc: ").append(this.docComment).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(super.toString());
		return buffer.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createArgumentReference(char[], java.lang.Object, int)
	 */
	protected Object createArgumentReference(char[] name, int dim, Object typeRef, long dimPos, long argNamePos) throws InvalidInputException {
		try {
			MethodRefParameter argument = this.ast.newMethodRefParameter();
			ASTNode node = (ASTNode) typeRef;
			int argStart = node.getStartPosition();
			int argEnd = node.getStartPosition()+node.getLength()-1;
			if (dimPos >= 0) argEnd = (int) dimPos;
			if (argNamePos >= 0) argEnd = (int) argNamePos;
			if (name.length != 0) {
				SimpleName argName = this.ast.newSimpleName(new String(name));
				argument.setName(argName);
				int argNameStart = (int) (argNamePos >>> 32);
				argName.setSourceRange(argNameStart, argEnd-argNameStart+1);
			}
			Type argType = null;
			if (node.getNodeType() == ASTNode.PRIMITIVE_TYPE) {
				argType = (PrimitiveType) node;
				if (dim > 0) {
					argType = this.ast.newArrayType(argType, dim);
					argType.setSourceRange(argStart, ((int) dimPos)-argStart+1);
				}
			} else {
				Name argTypeName = (Name) node;
				argType = this.ast.newSimpleType(argTypeName);
				if (dim > 0) {
					argType = this.ast.newArrayType(argType, dim);
					argType.setSourceRange(argStart, ((int) dimPos)-argStart+1);
				} else {
					argType.setSourceRange(argStart, node.getLength());
				}
			}
			argument.setType(argType);
			argument.setSourceRange(argStart, argEnd - argStart + 1);
			return argument;
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
			MemberRef fieldRef = this.ast.newMemberRef();
			SimpleName fieldName = this.ast.newSimpleName(new String(this.identifierStack[0]));
			fieldRef.setName(fieldName);
			int start = (int) (this.identifierPositionStack[0] >>> 32);
			int length = ((int) this.identifierPositionStack[0]) - start + 1;
			fieldName.setSourceRange(start, length);
			if (receiver == null) {
				fieldRef.setSourceRange(start, length);
			} else {
				Name typeRef = (Name) receiver;
				fieldRef.setQualifier(typeRef);
				int end = fieldName.getStartPosition()+fieldName.getLength()-1;
				fieldRef.setSourceRange(typeRef.getStartPosition(), end-typeRef.getStartPosition()+1);
			}
			return fieldRef;
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
			// Create method ref
			MethodRef methodRef = this.ast.newMethodRef();
			SimpleName methodName = this.ast.newSimpleName(new String(this.identifierStack[0]));
			methodRef.setName(methodName);
			int start = (int) (this.identifierPositionStack[0] >>> 32);
			int length = ((int) this.identifierPositionStack[0]) - start + 1;
			methodName.setSourceRange(start, length);
			// Set qualifier
			int end = methodName.getStartPosition()+methodName.getLength()-1;
			if (receiver == null) {
				methodRef.setSourceRange(start, length);
			} else {
				Name typeRef = (Name) receiver;
				methodRef.setQualifier(typeRef);
				methodRef.setSourceRange(typeRef.getStartPosition(), end-typeRef.getStartPosition()+1);
			}
			// Add arguments
			if (arguments != null) {
				Iterator parameters = arguments.listIterator();
				while (parameters.hasNext()) {
					MethodRefParameter param = (MethodRefParameter) parameters.next();
					methodRef.parameters().add(param);
					end = param.getStartPosition()+param.getLength()-1;
				}
			}
			methodRef.setSourceRange(start, end-start+1);
			return methodRef;
		}
		catch (ClassCastException ex) {
				throw new InvalidInputException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#createTypeReference()
	 */
	protected Object createTypeReference(int primitiveToken) {
		int size = this.identifierLengthStack[this.identifierLengthPtr--];
		String[] identifiers = new String[size];
		int pos = this.identifierPtr - size + 1;
		for (int i = 0; i < size; i++) {
			identifiers[i] = new String(this.identifierStack[pos+i]);
		}
		ASTNode typeRef = null;
		if (primitiveToken == -1) {
			typeRef = this.ast.newName(identifiers);
		} else {
			switch (primitiveToken) {
				case TerminalTokens.TokenNamevoid :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.VOID);
					break;
				case TerminalTokens.TokenNameboolean :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.BOOLEAN);
					break;
				case TerminalTokens.TokenNamebyte :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.BYTE);
					break;
				case TerminalTokens.TokenNamechar :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.CHAR);
					break;
				case TerminalTokens.TokenNamedouble :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.DOUBLE);
					break;
				case TerminalTokens.TokenNamefloat :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.FLOAT);
					break;
				case TerminalTokens.TokenNameint :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.INT);
					break;
				case TerminalTokens.TokenNamelong :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.LONG);
					break;
				case TerminalTokens.TokenNameshort :
					typeRef = this.ast.newPrimitiveType(PrimitiveType.SHORT);
					break;
				default:
					// should not happen
					return null;
			}
		}
		int start = (int) (this.identifierPositionStack[pos] >>> 32);
		int end = (int) this.identifierPositionStack[this.identifierPtr];
		typeRef.setSourceRange(start, end-start+1);
		this.identifierPtr -= size;
		return typeRef;
	}

	/*
	 * Parse @return tag declaration
	 */
	protected boolean parseReturn() {
		return parseTag();
	}

	/*
	 * Parse @return tag declaration
	 */
	protected boolean parseTag() {
		TagElement tag = this.ast.newTagElement();
		int length = this.scanner.currentPosition-this.tagSourceStart;
		tag.setTagName(new String(this.source, this.tagSourceStart, length));
		tag.setSourceRange(this.tagSourceStart, length);
		pushOnAstStack(tag, true);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushParamName(java.lang.Object)
	 */
	protected boolean pushParamName() {
		SimpleName name = this.ast.newSimpleName(new String(this.scanner.getCurrentIdentifierSource()));
		name.setSourceRange(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition()-this.scanner.getCurrentTokenStartPosition()+1);
		TagElement paramTag = this.ast.newTagElement();
		paramTag.setTagName(STR_TAG_PARAM);
		paramTag.setSourceRange(this.tagSourceStart, this.scanner.getCurrentTokenEndPosition()-this.tagSourceStart+1);
		paramTag.fragments().add(name);
		pushOnAstStack(paramTag, true);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushSeeRef(java.lang.Object)
	 */
	protected boolean pushSeeRef(Object statement, boolean plain) {
		TagElement seeTag = this.ast.newTagElement();
		ASTNode node = (ASTNode) statement;
		seeTag.fragments().add(node);
		int end = node.getStartPosition()+node.getLength()-1;
		seeTag.setSourceRange(this.tagSourceStart, end-this.tagSourceStart+1);
		if (this.inlineTagStarted) {
			if (plain) {
				seeTag.setTagName(STR_TAG_LINKPLAIN);
			} else {
				seeTag.setTagName(STR_TAG_LINK);
			}
			TagElement previousTag = null;
			if (this.astPtr == -1) {
				previousTag = this.ast.newTagElement();
				pushOnAstStack(previousTag, true);
			} else {
				previousTag = (TagElement) this.astStack[this.astPtr];
			}
			previousTag.fragments().add(seeTag);
		} else {
			seeTag.setTagName(STR_TAG_SEE);
			pushOnAstStack(seeTag, true);
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushText(int, int)
	 */
	protected void pushText(int start, int end) {
		TextElement text = this.ast.newTextElement();
		text.setText(new String( this.source, start, end-start+1));
		text.setSourceRange(start, end-start+1);
		TagElement previousTag = null;
		int previousStart = start;
		if (this.astPtr == -1) {
			previousTag = this.ast.newTagElement();
			pushOnAstStack(previousTag, true);
		} else {
			previousTag = (TagElement) this.astStack[this.astPtr];
			previousStart = previousTag.getStartPosition();
		}
		if (this.inlineTagStarted) {
			if (previousTag.fragments().size() == 0) {
				TagElement inlineTag = this.ast.newTagElement();
				previousTag.fragments().add(inlineTag);
				previousTag = inlineTag;
			} else {
				ASTNode inlineTag = (ASTNode) previousTag.fragments().get(previousTag.fragments().size()-1);
				if (inlineTag.getNodeType() == ASTNode.TAG_ELEMENT) {
					previousTag = (TagElement) inlineTag;
					previousStart = previousTag.getStartPosition();
				}
			}
		}
		previousTag.fragments().add(text);
		previousTag.setSourceRange(previousStart, end-previousStart+1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushThrowName(java.lang.Object)
	 */
	protected boolean pushThrowName(Object typeRef, boolean real) {
		TagElement throwsTag = this.ast.newTagElement();
		if (real) {
			throwsTag.setTagName(STR_TAG_THROWS);
		} else {
			throwsTag.setTagName(STR_TAG_EXCEPTION);
		}
		throwsTag.setSourceRange(this.tagSourceStart, this.scanner.getCurrentTokenEndPosition()-this.tagSourceStart+1);
		throwsTag.fragments().add(typeRef);
		pushOnAstStack(throwsTag, true);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#updateDocComment()
	 */
	protected void updateDocComment() {
		for (int idx = 0; idx <= this.astPtr; idx++) {
			this.docComment.tags().add(this.astStack[idx]);
		}
	}
}
