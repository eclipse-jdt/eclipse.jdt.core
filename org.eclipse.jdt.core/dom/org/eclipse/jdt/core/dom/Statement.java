/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/**
 * Abstract base class of AST nodes that represent statements.
 * There are many kinds of statements.
 * <p>
 * The grammar combines both Statement and BlockStatement.
 * <pre>
 * Statement:
 *    Block
 *    IfStatement
 *    ForStatement
 *    WhileStatement
 *    DoStatement
 *    TryStatement
 *    SwitchStatement
 *    SynchronizedStatement
 *    ReturnStatement
 *    ThrowStatement
 *    BreakStatement
 *    ContinueStatement
 *    EmptyStatement
 *    ExpressionStatement
 *    LabeledStatement
 *    AssertStatement
 *    VariableDeclarationStatement
 *    TypeDeclarationStatement
 *    ConstructorInvocation
 *    SuperConstructorInvocation
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Statement extends ASTNode {
	
	/**
	 * The leading comment, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private String optionalLeadingComment = null;
	
	/**
	 * Creates a new AST node for a statement owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Statement(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns the leading comment string, including the starting
	 * and ending comment delimiters, and any embedded line breaks.
	 * <p>
	 * A leading comment is a comment that appears before the statement.
	 * It may be either a traditional comment or an end-of-line comment.
	 * Traditional comments must begin with "/&#42;, may contain line breaks,
	 * and must end with "&#42;/. End-of-line comments must begin with "//",
	 * must end with a line delimiter (as per JLS 3.7), and must not contain
	 * line breaks.
	 * </p>
	 * 
	 * @return the comment string, or <code>null</code> if none
	 */
	public String getLeadingComment() {
		return optionalLeadingComment;
	}

	/**
	 * Sets or clears the leading comment string. The comment
	 * string must include the starting and ending comment delimiters,
	 * and any embedded linebreaks.
	 * <p>
	 * A leading comment is a comment that appears before the statement.
	 * It may be either a traditional comment or an end-of-line comment.
	 * Traditional comments must begin with "/&#42;, may contain line breaks,
	 * and must end with "&#42;/. End-of-line comments must begin with "//",
	 * must end with a line delimiter (as per JLS 3.7), and must not contain
	 * line breaks.
	 * </p>
	 * <p>
	 * Examples:
	 * <code>
	 * <pre>
	 * setLeadingComment("/&#42; traditional comment &#42;/");  // correct
	 * setLeadingComment("missing comment delimiters");  // wrong
	 * setLeadingComment("/&#42; unterminated traditional comment ");  // wrong
	 * setLeadingComment("/&#42; broken\n traditional comment &#42;/");  // correct
	 * setLeadingComment("// end-of-line comment\n");  // correct
	 * setLeadingComment("// end-of-line comment without line terminator");  // wrong
	 * setLeadingComment("// broken\n end-of-line comment\n");  // wrong
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param comment the comment string, or <code>null</code> if none
	 * @exception IllegalArgumentException if the comment string is invalid
	 */
	public void setLeadingComment(String comment) {
		if (comment != null) {
			char[] source = comment.toCharArray();
			IScanner scanner = ToolFactory.createScanner(true, true, false, false);
			scanner.resetTo(0, source.length);
			scanner.setSource(source);
			try {
				int token;
				boolean onlyOneComment = false;
				while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
					switch(token) {
						case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
						case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
						case ITerminalSymbols.TokenNameCOMMENT_LINE :
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
		}
		modifying();
		this.optionalLeadingComment = comment;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		String s = getLeadingComment();
		if (s != null) {
			size += HEADERS + 2 * 4 + HEADERS + 2 * s.length();
		}
		return size;
	}
}	

