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

package org.eclipse.jdt.core.dom;

import java.util.List;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * AST node for a Javadoc-style doc comment.
 * <pre>
 * Javadoc:
 *   <b>/&ast;&ast;</b> { TagElement } <b>&ast;/</b>
 * </pre>
 * 
 * @since 2.0
 */
public class Javadoc extends Comment {
	
	/**
	 * Canonical minimal doc comment.
     * @since 3.0
	 */
	private static final String MINIMAL_DOC_COMMENT = "/** */";//$NON-NLS-1$

	/**
	 * The doc comment string, including opening and closing comment 
	 * delimiters; defaults to a minimal Javadoc comment.
	 * @deprecated The comment string was replaced in the 3.0 release
	 * by a representation of the structure of the doc comment.
	 * For backwards compatibility, it is still funcational as before.
	 */
	private String comment = MINIMAL_DOC_COMMENT;
	
	/**
	 * The list of tag elements (element type: <code>TagElement</code>). 
	 * Defaults to an empty list.
	 * @since 3.0
	 */
	private ASTNode.NodeList tags = 
		new ASTNode.NodeList(true, TagElement.class);

	/**
	 * Creates a new AST node for a doc comment owned by the given AST.
	 * The new node has an empty list of tag elements (and, for backwards
	 * compatability, an unspecified, but legal, doc comment string).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Javadoc(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return JAVADOC;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		Javadoc result = new Javadoc(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setComment(getComment());
		result.tags().addAll(ASTNode.copySubtrees(target, tags()));
		return result;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChildren(visitor, this.tags);
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the doc comment string, including the starting
	 * and ending comment delimiters, and any embedded line breaks.
	 * 
	 * @return the doc comment string
	 * @deprecated The comment string was replaced in the 3.0 release
	 * by a representation of the structure of the doc comment.
	 * See {@link #tags() tags}.
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * Sets or clears the doc comment string. The documentation
	 * string must include the starting and ending comment delimiters,
	 * and any embedded line breaks.
	 * 
	 * @param docComment the doc comment string
	 * @exception IllegalArgumentException if the Java comment string is invalid
	 * @deprecated The comment string was replaced in the 3.0 release
	 * by a representation of the structure of the doc comment.
	 * See {@link #tags() tags}.
	 */
	public void setComment(String docComment) {
		if (docComment == null) {
			throw new IllegalArgumentException();
		}
		char[] source = docComment.toCharArray();
		Scanner scanner = this.getAST().scanner;
		scanner.resetTo(0, source.length);
		scanner.setSource(source);
		try {
			int token;
			boolean onlyOneComment = false;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (onlyOneComment) {
							throw new IllegalArgumentException();
						}
						onlyOneComment = true;
						break;
					default:
						onlyOneComment = false;
				}
			}
			if (!onlyOneComment) {
				throw new IllegalArgumentException();
			}
		} catch (InvalidInputException e) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.comment = docComment;
	}
		
	/**
	 * Returns the live list of tag elements that make up this doc 
	 * comment.
	 * <p>
	 * The tag elements cover everything except the starting and ending
	 * comment delimiters, and generally omit leading whitespace 
	 * (including a leading "&ast;") and embedded line breaks.
	 * The first tag element of a typical doc comment represents
	 * all the material before the first explicit doc tag; this
	 * first tag element has a <code>null</code> tag name and
	 * generally contains 1 or more {@link TextElement TextElement}s,
	 * and possibly interspersed with tag elements for nested tags
	 * like "{@link String String}".
	 * Subsequent tag elements represent successive top-level doc
	 * tag (e.g., "@param", "@return", "@see").
	 * </p>
	 * <p>
	 * Adding and removing nodes from this list affects this node
	 * dynamically.
	 * </p>
	 * 
	 * @return the live list of tag elements in this doc comment
	 * (element type: <code>TagElement</code>)
	 * @since 3.0
	 */ 
	public List tags() {
		return this.tags;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = super.memSize() + 2 * 4;
		if (this.comment != MINIMAL_DOC_COMMENT) {
			// anything other than the default string takes space
			size += stringSize(this.comment);
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + this.tags.listSize();
	}
}
