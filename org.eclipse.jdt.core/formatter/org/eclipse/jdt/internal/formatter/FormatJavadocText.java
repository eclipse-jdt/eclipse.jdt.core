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

import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;

/**
 * Represents text inside a javadoc comment block.
 * <p>
 * Text may be simple as <code>Line inside a javadoc comment block</code>
 * or may be a html tag. Note that to minimize memory footprint, only text
 * positions are stored.
 * </p><p>
 * Simple text may have one or several lines. When it has several lines, the
 * positions of the line breaks are also stored in the {@link #separators} array.
 * </p><p>
 * When text has html tags, then they are stored in {@link #htmlNodes} array
 * in a recursive way.
 * </p>
 */
public class FormatJavadocText extends FormatJavadocNode implements IJavaDocTagConstants {

	long[] separators;
	int separatorsPtr = -1;
	private int htmlTagIndex = -1;
	int linesBefore = 0;
	FormatJavadocNode[] htmlNodes;
	int htmlNodesPtr = -1;
	int depth = 0;

public FormatJavadocText(int start, int end, int htmlIndex, int htmlDepth) {
	super(start, end);
	this.htmlTagIndex = htmlIndex;
	this.depth = htmlDepth;
}

/*
 * Append a text to current one.
 * If the given text is not an html tag or is a closing tag, then just append to
 * the current text recording the separators. Otherwise, create a new html tag
 * child node.
 */
void appendText(FormatJavadocText text) {
	if (this.depth == text.depth) {
		addSeparator(text);
		this.sourceEnd = text.sourceEnd;
		if (text.isClosingHtmlTag()) {
			// close the tag
			this.htmlTagIndex = text.htmlTagIndex;
		}
	} else {
		appendNode(text);
	}
	if (text.isHtmlTag()) {
		switch (text.htmlTagIndex & JAVADOC_TAGS_ID_MASK) {
			case JAVADOC_CODE_TAGS_ID:
				text.linesBefore = this.htmlNodesPtr == -1 ? 0 : 2;
				break;
			case JAVADOC_SEPARATOR_TAGS_ID:
				text.linesBefore = 1;
				break;
	    	case JAVADOC_SINGLE_BREAK_TAG_ID:
	    	case JAVADOC_BREAK_TAGS_ID:
				if (!text.isClosingHtmlTag()) text.linesBefore = 1;
		}
	}
}
void appendNode(FormatJavadocNode node) {
	if (++this.htmlNodesPtr == 0) { // lazy initialization
		this.htmlNodes = new FormatJavadocText[DEFAULT_ARRAY_SIZE];
	} else {
		if (this.htmlNodesPtr == this.htmlNodes.length) {
			System.arraycopy(this.htmlNodes, 0, (this.htmlNodes= new FormatJavadocText[this.htmlNodes.length + 1]), 0, this.htmlNodesPtr);
		}
	}
	addSeparator(node);
	this.htmlNodes[this.htmlNodesPtr] = node;
	this.sourceEnd = node.sourceEnd;
}

private void addSeparator(FormatJavadocNode node) {
	// Just append the text
	if (++this.separatorsPtr == 0) { // lazy initialization
		this.separators = new long[DEFAULT_ARRAY_SIZE];
	} else { // resize if needed
		if (this.separatorsPtr == this.separators.length) {
			System.arraycopy(this.separators, 0, (this.separators = new long[this.separators.length + DEFAULT_ARRAY_SIZE]), 0, this.separatorsPtr);
		}
	}
	this.separators[this.separatorsPtr] = (((long) this.sourceEnd) << 32) + node.sourceStart;
}

void clean() {
	int length = this.separators == null ? 0 : this.separators.length;
	if (this.separatorsPtr != (length-1)) {
		System.arraycopy(this.separators, 0, this.separators = new long[this.separatorsPtr+1], 0, this.separatorsPtr+1);
	}
	length = this.htmlNodes == null ? 0 : this.htmlNodes.length;
	if (this.htmlNodesPtr != (length-1)) {
		System.arraycopy(this.htmlNodes, 0, this.htmlNodes = new FormatJavadocText[this.htmlNodesPtr+1], 0, this.htmlNodesPtr+1);
		for (int i=0; i<=this.htmlNodesPtr; i++) {
			this.htmlNodes[i].clean();
		}
	}
}

void closeTag() {
	this.htmlTagIndex |= JAVADOC_CLOSED_TAG;
}	

int getHtmlTagIndex() {
	return this.htmlTagIndex & JAVADOC_TAGS_INDEX_MASK;
}

int getHtmlTagID() {
	return this.htmlTagIndex & JAVADOC_TAGS_ID_MASK;
}

/**
 * Returns whether the text is a closing html tag or not.
 * 
 * @return <code>true</code> if the node is an html tag and has '/' before its
 * 	name (e.g. </bla>), <code>false</code> otherwise.
 */
public boolean isClosingHtmlTag() {
	return this.htmlTagIndex != -1 && (this.htmlTagIndex & JAVADOC_CLOSED_TAG) != 0;
}

/**
 * Returns whether the text is a html tag or not.
 * 
 * @return <code>true</code> if the node is a html tag, <code>false</code>
 * 	otherwise.
 */
public boolean isHtmlTag() {
	return this.htmlTagIndex != -1;
}

/**
 * Returns whether the node is an immutable html tag or not.
 * <p>
 * The text in an immutable tags is <b>never</b> formatted.
 * </p>
 * 
 * @return <code>true</code> if the node is an immutable tag,
 *		<code>false</code> otherwise.
 */
public boolean isImmutableHtmlTag() {
	return this.htmlTagIndex != -1 && (this.htmlTagIndex & JAVADOC_TAGS_ID_MASK) == JAVADOC_IMMUTABLE_TAGS_ID;
	
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.formatter.FormatJavadocNode#isText()
 */
public boolean isText() {
	return true;
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("	").append("[FJText] - at offset: " + this.sourceStart).append(" end position: " + this.sourceEnd).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	if (this.separatorsPtr > -1) {
		buffer.append("	").append("Number of text sections: " + (this.separatorsPtr + 1) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	} else {
		buffer.append("	").append("NO TEXT SEPARATOR" + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	return buffer.toString();
}
}
