/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.parser.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * AST node for a simple name. A simple name is an identifier other than
 * a keyword, boolean literal ("true", "false") or null literal ("null").
 * <p>
 * Range 0: first character through last character of identifier.
 * </p>
 * <pre>
 * SimpleName:
 *     Identifier
 * </pre>
 * 
 * @since 2.0
 */
public class SimpleName extends Name {

	/**
	 * An unspecified (but externally observable) legal Java identifier.
	 */
	private static final String MISSING_IDENTIFIER = "MISSING";//$NON-NLS-1$
	
	/**
	 * The identifier; defaults to a unspecified, legal Java identifier.
	 */
	private String identifier = MISSING_IDENTIFIER;
	
	/**
	 * Creates a new AST node for a simple name owned by the given AST.
	 * The new node has an unspecified, legal Java identifier.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SimpleName(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SimpleName result = new SimpleName(target);
		result.setIdentifier(getIdentifier());
		int startPosition = getStartPosition();
		int length = getLength();
		if (startPosition >= 0 && length > 0) {
			result.setSourceRange(startPosition, length);
		}
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
		visitor.endVisit(this);
	}

	/**
	 * Returns this node's identifier.
	 * 
	 * @return the identifier of this node
	 */ 
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Sets the identifier of this node to the given value.
	 * The identifier should be legal according to the rules
	 * of the Java language. Note that keywords are not legal
	 * identifiers.
	 * <p>
	 * Note that the list of keywords may depend on the version of the
	 * language (determined when the AST object was created).
	 * </p>
	 * 
	 * @param identifier the identifier of this node
	 * @exception $precondition-violation:invalid-java-identifier$
	 * @see AST#AST(java.util.Map)
	 */ 
	public void setIdentifier(String identifier) {
		if (identifier == null) {
			throw new IllegalArgumentException();
		}
		Scanner scanner = getAST().scanner;
		char[] source = identifier.toCharArray();
		scanner.setSourceBuffer(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case Scanner.TokenNameIdentifier:
					break;
				default:
					throw new IllegalArgumentException();
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.identifier = identifier;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		if (identifier != null) {
			size += HEADERS + 2 * 4 + HEADERS + 2 * identifier.length();
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

