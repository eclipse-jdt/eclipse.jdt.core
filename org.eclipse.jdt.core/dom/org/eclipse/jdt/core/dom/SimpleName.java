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
	 * Hold a pointer to the ast used to create the node
	 */
	private static Scanner scanner = new Scanner();
	

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
		return result;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	boolean equalSubtrees(Object other) {
		if (!(other instanceof SimpleName)) {
			return false;
		}
		SimpleName o = (SimpleName) other;
		return getIdentifier().equals(o.getIdentifier());
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
	 * of the Java language.
	 * <p>
	 * [Issue: Include specification of legal Java identifier.]
	 * </p>
	 * 
	 * @param identifier the identifier of this node
	 * @exception $precondition-violation:invalid-java-identifier$
	 */ 
	public void setIdentifier(String identifier) {
		if (identifier == null) {
			throw new IllegalArgumentException();
		}
		if (!isJavaIdentifier(identifier)) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.identifier = identifier;
	}
	
	/**
	 * Table of keywords and literals (element type <code>String</code>) that 
	 * may not be used as identifiers. See Java Language Specification §3.9.
	 * The "assert" keyword, which was added in 1.4, is <b>not</b> included.
	 */
	private static final Set KEYWORDS;
	static {
		KEYWORDS = new HashSet(50);
		KEYWORDS.addAll(
			Arrays.asList(
				new String[] {// literals
		"true", "false", "null", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		"abstract", "default", "if", "private", "this", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"boolean", "do", "implements", "protected", "throw",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"break", "double", "import", "public", "throws",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"byte", "else", "instanceof", "return", "transient",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"case", "extends", "int", "short", "try",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"catch", "final", "interface", "static", "void",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"char", "finally", "long", "strictfp", "volatile",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"class", "float", "native", "super", "while",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
		"const", "for", "new", "switch",//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		"continue", "goto", "package", "synchronized"}));//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}

	/**
	 * Returns whether the given string is a valid Java identifier.
	 * <p>
	 * Java Language Specification (§3.8):
	 * "An identifier is an unlimited-length sequence of Java letters and 
	 * Java digits, the first of which must be a Java letter. An identifier 
	 * cannot have the same spelling (Unicode character sequence) as a 
	 * keyword (§3.9), boolean literal (§3.10.3), or the null literal (§3.10.7).
	 * </p>
	 * <p>
	 * Letters and digits may be drawn from the entire Unicode character set, 
	 * which supports most writing scripts in use in the world today, including 
	 * the large sets for Chinese, Japanese, and Korean. This allows 
	 * programmers to use identifiers in their programs that are written in 
	 * their native languages. A "Java letter" is a character for which the 
	 * method Character.isJavaIdentifierStart returns true. A "Java 
	 * letter-or-digit" is a character for which the method 
	 * Character.isJavaIdentifierPart returns true."
	 * </p>
	 * <p>
	 * Note that "assert", which was added as a keyword in 1.4, is considered
	 * to be a legal Java identifier.
	 * </p>
	 * 
	 * @param identifier the alleged identifier
	 * @return <code>true</code> if a valid identifier, and <code>false</code> 
	 *    if not valid
	 */
	public static boolean isJavaIdentifier(String identifier) {
		// FIXME
		// assert won't be considered as a keyword
		char[] source = identifier.toCharArray();
		// the scanner is already initialized
		scanner.setSourceBuffer(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case Scanner.TokenNameIdentifier:
					return true;
			}
		} catch(InvalidInputException e) {
		}
		return false;
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

