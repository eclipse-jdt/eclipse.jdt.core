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
 *   <b>/&ast;&ast;</b> { DocElement } <b>&ast;/</b>
 * DocElement:
 *   TextElement
 *   TagElement
 * 	 Name
 *   MethodRef
 *   MemberRef
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
	 * The list of doc elements (element type: <code>IDocElement</code>). 
	 * Defaults to an empty list.
	 * @since 3.0
	 */
	private ASTNode.NodeList fragments = 
		new ASTNode.NodeList(true, IDocElement.class);

	/**
	 * Creates a new AST node for a doc comment owned by the given AST.
	 * The new node has an empty list of fragments (and, for backwards
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
		result.fragments().addAll(ASTNode.copySubtrees(target, fragments()));
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
			acceptChildren(visitor, fragments);
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
	 * See {@link #fragments() fragments}.
	 */
	public String getComment() {
		return comment;
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
	 * See {@link #fragments() fragments}.
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
	 * Returns the live list of fragments that make up this doc 
	 * comment.
	 * <p>
	 * The fragments cover everything except the starting and ending
	 * comment delimiters, and generally omit leading whitespace 
	 * (including a leading "&ast;") and embedded line breaks.
	 * The first fragment of a typical doc comment is generally a 
	 * {@link TextElement TextElement} containing the text up to
	 * the first top-level doc tag, and each subsequent element is a 
	 * {@link TagElement TagElement} representing a top-level doc
	 * tag (e.g., "@param", "@return", "@see").
	 * If there is no text preceding the first top-level doc tag,
	 * then the first fragment represents the first top-level doc tag.
	 * When the text preceding the first top-level doc tag contains
	 * an inline tag enclosed in braces (e.g., an "@link"), then
	 * the first fragment is a {@link TagElement TagElement}
	 * with a <code>null</code> tag name with its own fragments,
	 * one of which will be a {@link TagElement TagElement}
	 * for any embedded tag located in the preamble.
	 * </p>
	 * <p>
	 * Adding and removing nodes from this list affects this node
	 * dynamically. The nodes in this list may be of various
	 * types, including {@link TextElement TextElement}, 
	 * {@link TagElement TagElement}, {@link Name Name}, 
	 * {@link MemberRef MemberRef}, and {@link MethodRef MethodRef}.
	 * Clients should assume that the list of types may grow in
	 * the future, and write their code to deal with unexpected
	 * nodes types. However, attempts to add a non-proscribed type
	 * of node will trigger an exception.
	 * </p>
	 * 
	 * @return the live list of doc elements in this doc comment
	 * @since 3.0
	 */ 
	public List fragments() {
		return fragments;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = super.memSize() + 2 * 4;
		if (comment != MINIMAL_DOC_COMMENT) {
			// anything other than the default string takes space
			size += stringSize(comment);
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + fragments.listSize();
	}
}
