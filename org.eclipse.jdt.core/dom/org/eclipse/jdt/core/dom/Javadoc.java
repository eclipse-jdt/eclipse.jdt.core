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

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * AST node for a Javadoc comment.
 * 
 * @since 2.0
 */
public class Javadoc extends ASTNode {

	/**
	 * The javadoc comment string, including opening and closing comment 
	 * delimiters; defaults to an unspecified, but legal, Javadoc comment.
	 */
	private String comment = "/** */";//$NON-NLS-1$
	
	/**
	 * Creates a new AST node for a Javadoc comment owned by the given AST.
	 * The new node has an unspecified, but legal, Javadoc comment.
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
		visitor.visit(this);
		visitor.endVisit(this);
	}

	/**
	 * Returns the Javadoc comment string, including the starting
	 * and ending comment delimiters, and any embedded line breaks.
	 * 
	 * @return the javadoc comment string
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets or clears the Javadoc comment string. The documentation
	 * string must include the starting and ending comment delimiters,
	 * and any embedded line breaks.
	 * 
	 * @param javadocComment the javadoc comment string
	 * @exception IllegalArgumentException if the Java comment string is invalid
	 */
	public void setComment(String javadocComment) {
		if (javadocComment == null) {
			throw new IllegalArgumentException();
		}
		char[] source = javadocComment.toCharArray();
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
		this.comment = javadocComment;
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		size += HEADERS + 2 * 4 + HEADERS + 2 * comment.length();
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

