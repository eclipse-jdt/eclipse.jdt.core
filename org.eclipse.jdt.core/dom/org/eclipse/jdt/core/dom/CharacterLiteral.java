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

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * Character literal nodes.
 * 
 * @since 2.0
 */
public class CharacterLiteral extends Expression {

	/**
	 * String of characters that have a special escape equivalent.
	 * Paralleled by QUOTED_SPECIALS.
	 */
	private static final String SPECIALS = "\b\t\n\f\r\"\'\\";//$NON-NLS-1$

	/**
	 * String of single-letter escape equivalents.
	 * Parallel to SPECIALS.
	 */
	private static final String QUOTED_SPECIALS = "btnfr\"\'\\";//$NON-NLS-1$

	/**
	 * The literal string, including quotes and escapes; defaults to the 
	 * literal for the character 'X'.
	 */
	private String escapedValue = "\'X\'";//$NON-NLS-1$

	/**
	 * Creates a new unparented character literal node owned by the given AST.
	 * By default, the character literal denotes an unspecified character.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CharacterLiteral(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		CharacterLiteral result = new CharacterLiteral(target);
		result.setEscapedValue(getEscapedValue());
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
	 * Returns the string value of this literal node. The value is the sequence
	 * of characters that would appear in the source program, including
	 * enclosing single quotes and embedded escapes.
	 * 
	 * @return the escaped string value, including enclosing single quotes
	 *    and embedded escapes
	 */ 
	public String getEscapedValue() {
		return escapedValue;
	}
		
	/**
	 * Sets the string value of this literal node. The value is the sequence
	 * of characters that would appear in the source program, including
	 * enclosing single quotes and embedded escapes. For example,
	 * <ul>
	 * <li><code>'a'</code> <code>setEscapedValue("\'a\'")</code></li>
	 * <li><code>'\n'</code> <code>setEscapedValue("\'\\n\'")</code></li>
	 * </ul>
	 * 
	 * @param value the string value, including enclosing single quotes
	 *    and embedded escapes
	 * @exception $precondition-violation:invalid-argument$
	 */ 
	public void setEscapedValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		Scanner scanner = getAST().scanner;
		char[] source = value.toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case Scanner.TokenNameCharacterLiteral:
					break;
				default:
					throw new IllegalArgumentException();
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.escapedValue = value;
	}

	/**
	 * Returns the value of this literal node. 
	 * <p>
	 * For example,
	 * <code>
	 * <pre>
	 * CharacterLiteral s;
	 * s.setEscapedValue("\'x\'");
	 * assert s.charValue() == 'x';
	 * </pre>
	 * </p>
	 * 
	 * @return the character value without enclosing quotes and embedded
	 *    escapes
	 * @exception $postcondition-violation:invalid-literal$
	 */ 
	public char charValue() {
		String s = getEscapedValue();
		int len = s.length();
		if (len < 2 || s.charAt(0) != '\'' || s.charAt(len-1) != '\'' ) {
			throw new IllegalArgumentException();
		}
		char c = s.charAt(1);
		if (c == '\'') {
			throw new IllegalArgumentException();
		}
		if (c == '\\') {
			// legal: b, t, n, f, r, ", ', \, 0, 1, 2, 3, 4, 5, 6, or 7
			// FIXME
			throw new RuntimeException("not implemented yet");//$NON-NLS-1$
		}
		return c;
	}

	/**
	 * Sets the value of this character literal node to the given character. 
	 * <p>
	 * For example,
	 * <code>
	 * <pre>
	 * CharacterLiteral s;
	 * s.setCharValue('x');
	 * assert s.charValue() == 'x';
	 * assert s.getEscapedValue("\'x\'");
	 * </pre>
	 * </p>
	 * 
	 * @param value the character value
	 */
	public void setCharValue(char value) {
		StringBuffer b = new StringBuffer(3);
		
		// FIXME - this does not do Unicode escaping
		b.append('\''); // opening delimiter
		int p = SPECIALS.indexOf(value);
		if (p >= 0) {
			b.append('\\');
			b.append(QUOTED_SPECIALS.charAt(p));
		} else {
			b.append(value);
		}
		b.append('\''); // closing delimiter
		setEscapedValue(b.toString());
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		if (escapedValue != null) {
			size += HEADERS + 2 * 4 + HEADERS + 2 * escapedValue.length();
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

