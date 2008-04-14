/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.JavadocParser;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;

/**
 * Internal parser used for formatting javadoc comments.
 */
public class FormatterCommentParser extends JavadocParser implements IJavaDocTagConstants {
	char[][] htmlTags;
	int htmlTagsPtr = -1;
	
FormatterCommentParser(Parser sourceParser) {
	super(sourceParser);
	this.kind = FORMATTER_COMMENT_PARSER | TEXT_PARSE;
	this.reportProblems = false;
	this.checkDocComment = true;
}

public boolean parse(int start, int end) {

	// Init
	this.javadocStart = start;
	this.javadocEnd = end;
	this.firstTagPosition = this.javadocStart;

	// parse comment
	boolean valid = commentParse();

	return valid && this.docComment != null;
}

public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("FormatterCommentParser\n"); //$NON-NLS-1$
	buffer.append(super.toString());
	return buffer.toString();
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createArgumentReference(char[], int, boolean, java.lang.Object, long[], long)
 */
protected Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object ref, long[] dimPositions, long argNamePos) throws InvalidInputException {
	FormatJavadocReference typeRef = (FormatJavadocReference) ref;
	if (dim > 0) {
		typeRef.sourceEnd = (int) dimPositions[dim-1];
	}
	if (argNamePos >= 0) typeRef.sourceEnd = (int) argNamePos;
	return ref;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createFieldReference(java.lang.Object)
 */
protected Object createFieldReference(Object receiver) throws InvalidInputException {
	int start = receiver == null ? this.memberStart : ((FormatJavadocReference)receiver).sourceStart;
	return new FormatJavadocReference(start, (int) this.identifierPositionStack[0]);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createMethodReference(java.lang.Object, java.util.List)
 */
protected Object createMethodReference(Object receiver, List arguments) throws InvalidInputException {

	// Get intermediate arguments positions
	long[] positions = null;
	if (arguments != null) {
		int size = arguments.size();
		positions = new long[size];
		for (int i=0; i<size; i++) {
			FormatJavadocReference reference = (FormatJavadocReference) arguments.get(i);
			positions[i] = (((long) reference.sourceStart) << 32) + reference.sourceEnd;
		}
	}

	// Build the node
	FormatJavadocReference reference = receiver == null
		? new FormatJavadocReference(this.memberStart, this.scanner.getCurrentTokenEndPosition())
		: new FormatJavadocReference(((FormatJavadocReference) receiver).sourceStart, this.scanner.getCurrentTokenEndPosition());
	reference.positions = positions;
	return reference;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createTag()
 */
protected void createTag() {
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, this.tagValue);
	if (this.inlineTagStarted) {
		FormatJavadocBlock previousBlock = null;
		if (this.astPtr == -1) {
			previousBlock = new FormatJavadocBlock(this.inlineTagStart, this.tagSourceEnd, NO_TAG_VALUE);
			pushOnAstStack(previousBlock, true);
		} else {
			previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		}
		previousBlock.addBlock(block, this.htmlTagsPtr == -1 ? 0 : this.htmlTagsPtr);
	} else {
		pushOnAstStack(block, true);
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#createTypeReference(int)
 */
protected Object createTypeReference(int primitiveToken) {
	int size = this.identifierLengthStack[this.identifierLengthPtr];
	if (size == 0) return null;
	if (size == 1) { 
		return new FormatJavadocReference(this.identifierPositionStack[this.identifierPtr]);
	}
	long[] positions = new long[size];
	System.arraycopy(this.identifierPositionStack, this.identifierPtr - size + 1, positions, 0, size);
	return new FormatJavadocReference(positions);
}

/*
 * Parse an HTML tag expected to be either opening (e.g. <tag_name> ) or
 * closing (e.g. </tag_name>).
 */
protected boolean parseHtmlTag(int previousPosition, int endTextPosition) throws InvalidInputException {
    boolean closing = false;
    boolean valid = false;
    boolean incremented = false;
    int start = this.scanner.currentPosition;
	try {
	    int token = readTokenAndConsume();
	    char[] htmlTag;
	    int htmlIndex;
	    switch (token) {
	    	case TerminalTokens.TokenNameIdentifier:
	    		// HTML tag opening
				htmlTag = this.scanner.getCurrentIdentifierSource();
				htmlIndex = htmlTagIndex(htmlTag);
				if ((htmlIndex & JAVADOC_TAGS_ID_MASK) > JAVADOC_SINGLE_TAGS_ID) {
		    		if (this.htmlTagsPtr == -1 || !CharOperation.equals(this.htmlTags[this.htmlTagsPtr], htmlTag, false)) {
						if (++this.htmlTagsPtr == 0) { // lazy initialization
							this.htmlTags = new char[AST_STACK_INCREMENT][];
						} else { // resize if needed
							if (this.htmlTagsPtr == this.htmlTags.length) {
								System.arraycopy(this.htmlTags, 0, (this.htmlTags = new char[this.htmlTags.length + AST_STACK_INCREMENT][]), 0, this.htmlTagsPtr);
							}
						}
						this.htmlTags[this.htmlTagsPtr] = htmlTag;
						incremented = true;
		    		}
				}
		    	break;
	    	case TerminalTokens.TokenNameDIVIDE:
	    		// HTML tag closing
	    		if (this.htmlTagsPtr == -1) return false;
	    		htmlTag = this.htmlTags[this.htmlTagsPtr];
				htmlIndex = htmlTagIndex(htmlTag);
	    		if ((token = readTokenAndConsume()) != TerminalTokens.TokenNameIdentifier || !CharOperation.equals(htmlTag, this.scanner.getCurrentIdentifierSource(), false)) {
	    			this.abort = true;
	    			return valid;
	    		}
				// set closing flag
				htmlIndex |= JAVADOC_CLOSED_TAG;
				closing = true;
	    		break;
	    	default:
    			return valid;
	    }
	    if ((token = readTokenAndConsume()) != TerminalTokens.TokenNameGREATER) {
	    	// invalid syntax
			return valid;
	    }
		if (this.lineStarted && this.textStart != -1 && this.textStart < endTextPosition) {
			pushText(this.textStart, endTextPosition, -1, closing ? this.htmlTagsPtr : (this.htmlTagsPtr < 1 ? 0 : this.htmlTagsPtr-1));
		}
		pushText(previousPosition, this.index, htmlIndex, this.htmlTagsPtr);
		this.textStart = -1;
		valid = true;
	}
	finally {
		if (valid) {
			if (closing) this.htmlTagsPtr--;
		} else if (!this.abort) {
	    	if (incremented) {
	    		this.htmlTagsPtr--;
	    		if (this.htmlTagsPtr == -1) this.htmlTags = null;
	    	}
	    	this.scanner.resetTo(start, this.scanner.eofPosition-1);
	    	this.index = start;
		}
	}
    return valid;
}

/*
 * Return the html tag index in the various arrays of IJavaDocTagConstants.
 * The returned int is set as follow:
 * 	- the array index is set on bits 0 to 7
 * 	- the tag category is set on bit 8 to 15 (0xFF00 if no array includes the tag)
 */
private int htmlTagIndex(char[] htmlTag) {
	int length = htmlTag == null ? 0 : htmlTag.length;
	if (length > 0) {
		for (int i=0, max=JAVADOC_SINGLE_BREAK_TAG.length; i<max; i++) {
			char[] tag = JAVADOC_SINGLE_BREAK_TAG[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return JAVADOC_SINGLE_BREAK_TAG_ID + i;
			}
		}
		for (int i=0, max=JAVADOC_CODE_TAGS.length; i<max; i++) {
			char[] tag = IJavaDocTagConstants.JAVADOC_CODE_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return JAVADOC_CODE_TAGS_ID + i;
			}
		}
		for (int i=0, max=IJavaDocTagConstants.JAVADOC_BREAK_TAGS.length; i<max; i++) {
			char[] tag = IJavaDocTagConstants.JAVADOC_BREAK_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return JAVADOC_BREAK_TAGS_ID + i;
			}
		}
		for (int i=0, max=IJavaDocTagConstants.JAVADOC_IMMUTABLE_TAGS.length; i<max; i++) {
			char[] tag = IJavaDocTagConstants.JAVADOC_IMMUTABLE_TAGS[i];
			if (length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return JAVADOC_IMMUTABLE_TAGS_ID + i;
			}
		}
		for (int i=0, max=IJavaDocTagConstants.JAVADOC_SEPARATOR_TAGS.length; i<max; i++) {
			char[] tag = IJavaDocTagConstants.JAVADOC_SEPARATOR_TAGS[i];
			if (htmlTag[0] == tag[0] && length == tag.length && CharOperation.equals(htmlTag, tag, false)) {
				return JAVADOC_SEPARATOR_TAGS_ID + i;
			}
		}
	}
	return JAVADOC_TAGS_ID_MASK;
}

/*
 * Parse @return tag declaration
 */
protected boolean parseReturn() {
	createTag();
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#parseTag(int)
 */
protected boolean parseTag(int previousPosition) throws InvalidInputException {
	boolean valid = super.parseTag(previousPosition);
	this.textStart = -1;
	consumeToken();
	// the javadoc parser may not create tag for some valid tags: force tag creation for such tag. 
	if (valid && (this.tagValue == TAG_INHERITDOC_VALUE || this.tagValue == TAG_DEPRECATED_VALUE)) {
		valid = false;
	}
	if (!valid) {
		createTag();
	}
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#pushParamName(boolean)
 */
protected boolean pushParamName(boolean isTypeParam) {
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, TAG_PARAM_VALUE);
	FormatJavadocReference reference;
	if (isTypeParam) {
		reference = new FormatJavadocReference((int) (this.identifierPositionStack[0] >>> 32), (int) this.identifierPositionStack[2]);
		reference.positions = new long[3];
		System.arraycopy(this.identifierPositionStack, 0, reference.positions, 0, 3);
	} else {
		reference = new FormatJavadocReference(this.identifierPositionStack[0]);
	}
	block.reference = reference;
	block.sourceEnd = reference.sourceEnd;
	pushOnAstStack(block, true);
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.JavadocParser#pushSeeRef(java.lang.Object)
 */
protected boolean pushSeeRef(Object statement) {
	FormatJavadocReference reference = (FormatJavadocReference) statement;
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, this.tagValue);
	block.reference = reference;
	block.sourceEnd = reference.sourceEnd;
	if (this.inlineTagStarted) {
		block.sourceStart = this.inlineTagStart;
		FormatJavadocBlock previousBlock = null;
		if (this.astPtr == -1) {
			previousBlock = new FormatJavadocBlock(this.inlineTagStart, this.tagSourceEnd, NO_TAG_VALUE);
			previousBlock.sourceEnd = reference.sourceEnd;
			pushOnAstStack(previousBlock, true);
		} else {
			previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		}
		previousBlock.addBlock(block, this.htmlTagsPtr == -1 ? 0 : this.htmlTagsPtr);
		block.flags |= FormatJavadocBlock.INLINED;
	} else {
		pushOnAstStack(block, true);
	}
	
	return true;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushText(int, int)
 */
protected void pushText(int start, int end) {
	pushText(start, end, -1, this.htmlTagsPtr == -1 ? 0 : this.htmlTagsPtr);
}

private void pushText(int start, int end, int htmlIndex, int htmlDepth) {
	
	// Search previous tag on which to add the text element
	FormatJavadocBlock previousBlock = null;
	int previousStart = start;
	if (this.astPtr == -1) {
		previousBlock = new FormatJavadocBlock(start, start, NO_TAG_VALUE);
		pushOnAstStack(previousBlock, true);
	} else {
		previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		previousStart = previousBlock.sourceStart;
	}
	
	// If we're in a inline tag, then retrieve previous tag in its fragments
	if (this.inlineTagStarted) {
		if (previousBlock.nodes == null) {
			// no existing fragment => just add the element
		} else {
			// If last fragment is a tag, then use it as previous tag
			FormatJavadocNode lastNode = previousBlock.nodes[previousBlock.nodesPtr];
			if (!lastNode.isText()) {
				previousBlock = (FormatJavadocBlock) lastNode;
				previousStart = previousBlock.sourceStart;
			}
		}
	}

	// Add the text
	int textEnd = end;
	if (this.javadocTextEnd > 0 && end >= this.javadocTextEnd) {
		// Special case on javadoc text end, need to retrieve the space
		// position by rescanning the text
		int restart = this.spacePosition == -1 ? start : this.spacePosition;
		this.scanner.resetTo(restart, end-1/* before last star*/);
		try {
			if (this.scanner.getNextToken() == TerminalTokens.TokenNameEOF) {
				textEnd = this.spacePosition;
			}
		}
		catch (InvalidInputException iie) {
			// do nothing
		}
	}
	FormatJavadocText text = new FormatJavadocText(start, textEnd-1, htmlIndex, htmlDepth);
	previousBlock.addText(text);
	previousBlock.sourceStart = previousStart;
	int lineStart = this.scanner.getLineNumber(start);
	int blockLine = this.scanner.getLineNumber(previousBlock.sourceStart);
	if (lineStart == blockLine) {
		previousBlock.flags |= FormatJavadocBlock.TEXT_ON_TAG_LINE;
	}
	this.textStart = -1;
}

/*
 * (non-Javadoc)
 * 
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#pushThrowName(java.lang.Object)
 */
protected boolean pushThrowName(Object typeRef) {
	FormatJavadocBlock block = new FormatJavadocBlock(this.tagSourceStart, this.tagSourceEnd, this.tagValue);
	block.reference = (FormatJavadocReference) typeRef;
	block.sourceEnd = block.reference.sourceEnd;
	pushOnAstStack(block, true);
	return true;
}

/*
 * (non-Javadoc)
 * Will update the inline tag position (end position) once tag was fully parsed.
 * @see org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser#refreshInlineTagPosition(int)
 */
protected void refreshInlineTagPosition(int previousPosition) {
	if (this.astPtr != -1) {
		FormatJavadocBlock previousBlock = (FormatJavadocBlock) this.astStack[this.astPtr];
		if (this.inlineTagStarted) {
			previousBlock.sourceEnd = previousPosition;
			FormatJavadocNode lastNode = previousBlock.getLastNode();
			if (lastNode != null && !lastNode.isText()) {
				lastNode.sourceEnd = previousPosition;
			}
		}
	}
}

/*
 * Add stored tag elements to associated comment.
 * Clean all blocks (i.e. resize arrays to avoid null slots)
 * Set extra information on block about line relative positions.
 */
protected void updateDocComment() {
	int length = this.astPtr + 1;
	FormatJavadoc formatJavadoc = new FormatJavadoc(this.javadocStart, this.javadocEnd, length);
	if (length > 0) {
		formatJavadoc.blocks = new FormatJavadocBlock[length];
		for (int i=0; i<length; i++) {
			FormatJavadocBlock block = (FormatJavadocBlock) this.astStack[i];
			block.clean();
			int blockStart = this.scanner.getLineNumber(block.sourceStart);
			int blockEnd = this.scanner.getLineNumber(block.sourceEnd);
			if (blockStart == blockEnd) {
				block.flags |= FormatJavadocBlock.ONE_LINE_TAG;
			}
			formatJavadoc.blocks[i] = block;
			if (i== 0) {
				block.flags |= FormatJavadocBlock.FIRST;
			}
		}
	}
	formatJavadoc.textStart = this.javadocTextStart;
	formatJavadoc.textEnd = this.javadocTextEnd;
	formatJavadoc.lineStart = this.scanner.getLineNumber(this.javadocTextStart);
	formatJavadoc.lineEnd = this.scanner.getLineNumber(this.javadocTextEnd);
	FormatJavadocBlock firstBlock = formatJavadoc.getFirstBlock();
	if (firstBlock != null) {
		int blockLine = this.scanner.getLineNumber(firstBlock.sourceStart);
		if (formatJavadoc.lineStart == blockLine) {
			firstBlock.flags |= FormatJavadocBlock.ON_HEADER_LINE;
		}
	}
	this.docComment = formatJavadoc;
}
}
