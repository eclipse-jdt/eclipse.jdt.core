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

/**
 * String literal nodes.
 * 
 * @since 2.0
 */
public class StringLiteral extends Expression {

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
	 * literal for the empty string.
	 */
	private String escapedValue = "\"\"";//$NON-NLS-1$

	/**
	 * Creates a new unparented string literal node owned by the given AST.
	 * By default, the string literal denotes the empty string.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	StringLiteral(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		StringLiteral result = new StringLiteral(target);
		result.setEscapedValue(getEscapedValue());
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	boolean equalSubtrees(Object other) {
		if (!(other instanceof StringLiteral)) {
			return false;
		}
		StringLiteral o = (StringLiteral) other;
		return ASTNode.equals(getEscapedValue(), o.getEscapedValue());
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the string value of this literal node to the given string
	 * literal token. The token is the sequence of characters that would appear
	 * in the source program, including enclosing double quotes and embedded
	 * escapes.
	 * 
	 * @return the string literal token, including enclosing double
	 *    quotes and embedded escapes
	 */ 
	public String getEscapedValue() {
		return escapedValue;
	}
		
	/**
	 * Sets the string value of this literal node to the given string literal
	 * token. The token is the sequence of characters that would appear in the
	 * source program, including enclosing double quotes and embedded escapes.
	 * For example,
	 * <ul>
	 * <li><code>""</code> <code>setLiteral("\"\"")</code></li>
	 * <li><code>"hello world"</code> <code>setLiteral("\"hello world\"")</code></li>
	 * <li><code>"boo\nhoo"</code> <code>setLiteral("\"boo\\nhoo\"")</code></li>
	 * </ul>
	 * 
	 * @param token the string literal token, including enclosing double
	 *    quotes and embedded escapes
	 * @exception $precondition-violation:invalid-argument$
	 */ 
	public void setEscapedValue(String token) {
		if (token == null || token.length() < 2
		|| !token.startsWith("\"") || ! token.endsWith("\"")) {//$NON-NLS-1$//$NON-NLS-2$
			throw new IllegalArgumentException();
		}
		modifying();
		this.escapedValue = token;
	}

	/**
	 * Returns the value of this literal node. 
	 * <p>
	 * For example,
	 * <code>
	 * <pre>
	 * StringLiteral s;
	 * s.setEscapedValue("\"hello\\nworld\"");
	 * assert s.getLiteralValue().equals("hello\nworld");
	 * </pre>
	 * </p>
	 * <p>
	 * Note that this is a convenience method that converts from the stored 
	 * string literal token returned by <code>getEscapedLiteral</code>.
	 * </p>
	 * 
	 * @return the string value without enclosing double quotes and embedded
	 *    escapes
	 * @exception $postcondition-violation:invalid-literal$
	 */ 
	public String getLiteralValue() {
		String s = getEscapedValue();
		int len = s.length();
		if (len < 2 || s.charAt(0) != '\"' || s.charAt(len-1) != '\"' ) {
			throw new IllegalArgumentException();
		}
		StringBuffer b = new StringBuffer(len - 2);
		for (int i = 1; i< len - 1; i++) {
			char c = s.charAt(i);
			if (c == '\"') {
				throw new IllegalArgumentException();
			}
			if (c == '\\') {
				// legal: b, t, n, f, r, ", ', \, 0, 1, 2, 3, 4, 5, 6, or 7
				// FIXME
				throw new RuntimeException("not implemented yet");//$NON-NLS-1$
			}
			b.append(c);
		}
		return b.toString();			
	}

	/**
	 * Sets the value of this literal node. 
	 * <p>
	 * For example,
	 * <code>
	 * <pre>
	 * StringLiteral s;
	 * s.setLiteralValue("hello\nworld");
	 * assert s.getEscapedValue("\"hello\\nworld\"");
	 * assert s.getLiteralValue().equals("hello\nworld");
	 * </pre>
	 * </p>
	 * <p>
	 * Note that this is a convenience method that converts to the stored 
	 * string literal token acceptable to <code>setEscapedLiteral</code>.
	 * </p>
	 * 
	 * @param literal the string value without enclosing double quotes and 
	 *    embedded escapes
	 * @exception $precondition-violation:invalid-argument$
	 */
	public void setLiteralValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		int len = value.length();
		StringBuffer b = new StringBuffer(len + 2);
		
		// FIXME - this does not do Unicode escaping
		b.append('\"'); // opening delimiter
		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);
			int p = SPECIALS.indexOf(c);
			if (p >= 0) {
				b.append('\\');
				b.append(QUOTED_SPECIALS.charAt(p));
			} else {
				b.append(c);
			}
		}
		b.append('\"'); // closing delimiter
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

