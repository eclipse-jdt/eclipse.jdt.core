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
 * Character literal nodes.
 * 
 * @since 2.0
 */
public class CharacterLiteral extends Expression {

	/**
	 * The "escapedValue" structural property of this node type.
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor ESCAPED_VALUE_PROPERTY = 
		new SimplePropertyDescriptor(CharacterLiteral.class, "escapedValue", String.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(CharacterLiteral.class);
		addProperty(ESCAPED_VALUE_PROPERTY);
		PROPERTY_DESCRIPTORS = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.LEVEL_*</code>LEVEL

	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
			
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
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == ESCAPED_VALUE_PROPERTY) {
			if (get) {
				return getEscapedValue();
			} else {
				setEscapedValue((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return CHARACTER_LITERAL;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		CharacterLiteral result = new CharacterLiteral(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
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
		visitor.visit(this);
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
		return this.escapedValue;
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
	 * @exception IllegalArgumentException if the argument is incorrect
	 */ 
	public void setEscapedValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		Scanner scanner = this.ast.scanner;
		char[] source = value.toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case TerminalTokens.TokenNameCharacterLiteral:
					break;
				default:
					throw new IllegalArgumentException();
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException();
		}
		preValueChange(ESCAPED_VALUE_PROPERTY);
		this.escapedValue = value;
		postValueChange(ESCAPED_VALUE_PROPERTY);
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
	 * @exception IllegalArgumentException if the literal value cannot be converted
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
			if (len == 4) {
				char nextChar = s.charAt(2);
				switch(nextChar) {
					case 'b' :
						return '\b';
					case 't' :
						return '\t';
					case 'n' :
						return '\n';
					case 'f' :
						return '\f';
					case 'r' :
						return '\r';
					case '\"':
						return '\"';
					case '\'':
						return '\'';
					case '\\':
						return '\\';
					case '0' :
						return '\0';
					case '1' :
						return '\1';
					case '2' :
						return '\2';
					case '3' :
						return '\3';
					case '4' :
						return '\4';
					case '5' :
						return '\5';
					case '6' :
						return '\6';
					case '7' :
						return '\7';
					default:
						throw new IllegalArgumentException("illegal character literal");//$NON-NLS-1$
				}
			} else if (len == 8) {
				//handle the case of unicode.
				int currentPosition = 2;
				int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
				if (s.charAt(currentPosition++) == 'u') {
					if ((c1 = Character.getNumericValue(s.charAt(currentPosition++))) > 15
						|| c1 < 0
						|| (c2 = Character.getNumericValue(s.charAt(currentPosition++))) > 15
						|| c2 < 0
						|| (c3 = Character.getNumericValue(s.charAt(currentPosition++))) > 15
						|| c3 < 0
						|| (c4 = Character.getNumericValue(s.charAt(currentPosition++))) > 15
						|| c4 < 0){
						throw new IllegalArgumentException("illegal character literal");//$NON-NLS-1$
					} else {
						return (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
					}
				} else {
					throw new IllegalArgumentException("illegal character literal");//$NON-NLS-1$
				}
			} else {
				throw new IllegalArgumentException("illegal character literal");//$NON-NLS-1$
			}
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
		
		b.append('\''); // opening delimiter
		switch(value) {
			case '\b' :
				b.append("\\b"); //$NON-NLS-1$
				break;
			case '\t' :
				b.append("\\t"); //$NON-NLS-1$
				break;
			case '\n' :
				b.append("\\n"); //$NON-NLS-1$
				break;
			case '\f' :
				b.append("\\f"); //$NON-NLS-1$
				break;
			case '\r' :
				b.append("\\r"); //$NON-NLS-1$
				break;
			case '\"':
				b.append("\\\""); //$NON-NLS-1$
				break;
			case '\'':
				b.append("\\\'"); //$NON-NLS-1$
				break;
			case '\\':
				b.append("\\\\"); //$NON-NLS-1$
				break;
			case '\0' :
				b.append("\\0"); //$NON-NLS-1$
				break;
			case '\1' :
				b.append("\\1"); //$NON-NLS-1$
				break;
			case '\2' :
				b.append("\\2"); //$NON-NLS-1$
				break;
			case '\3' :
				b.append("\\3"); //$NON-NLS-1$
				break;
			case '\4' :
				b.append("\\4"); //$NON-NLS-1$
				break;
			case '\5' :
				b.append("\\5"); //$NON-NLS-1$
				break;
			case '\6' :
				b.append("\\6"); //$NON-NLS-1$
				break;
			case '\7' :
				b.append("\\7"); //$NON-NLS-1$
				break;			
			default:
				b.append(value);
		}
		b.append('\''); // closing delimiter
		setEscapedValue(b.toString());
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4 + stringSize(escapedValue);
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

