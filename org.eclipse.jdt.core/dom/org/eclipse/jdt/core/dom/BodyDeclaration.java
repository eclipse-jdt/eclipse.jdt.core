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

/**
 * Abstract base class of all AST nodes that represent body declarations 
 * that may appear in the body of a class or interface declaration.
 * <p>
 * <pre>
 * ClassBodyDeclaration:
 *		ClassDeclaration
 *		InterfaceDeclaration
 *		MethodDeclaration
 * 		ConstructorDeclaration
 * 		FieldDeclaration
 * 		Initializer
 * InterfaceBodyDeclaration:
 *		ClassDeclaration
 *		InterfaceDeclaration
 *		MethodDeclaration
 * 		FieldDeclaration
 * </pre>
 * </p>
 * <p>
 * All types of body declarations carry modifiers, although they differ in
 * which modifiers are allowed. Most types of body declarations can carry a
 * doc comment; Initializer is the only ones that does not. The source range
 * for body declarations always includes the doc comment if present.
 * </p>
 * 
 * @since 2.0
 */
public abstract class BodyDeclaration extends ASTNode {
	
	/**
	 * The doc comment, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Javadoc optionalDocComment = null;

	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 * 
	 * @since 3.0
	 */
	private int modifiers = Modifier.NONE;
	
	/**
	 * Creates a new AST node for a body declaration node owned by the 
	 * given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	BodyDeclaration(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns the doc comment node.
	 * 
	 * @return the doc comment node, or <code>null</code> if none
	 */
	public Javadoc getJavadoc() {
		return optionalDocComment;
	}

	/**
	 * Sets or clears the doc comment node.
	 * 
	 * @param docComment the doc comment node, or <code>null</code> if none
	 * @exception IllegalArgumentException if the doc comment string is invalid
	 */
	public void setJavadoc(Javadoc docComment) {
		replaceChild(this.optionalDocComment, docComment, false);
		this.optionalDocComment = docComment;
	}

	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * The allowable modifiers differ for each type of body declaration.
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 * @since 3.0
	 */ 
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets the modifiers explicitly specified on this declaration.
	 * The allowable modifiers differ for each type of body declaration.
	 * 
	 * @param modifiers the given modifiers (bit-wise or of <code>Modifier</code> constants)
	 * @see Modifier
	 * @exception IllegalArgumentException if the modifiers are illegal
	 * @since 3.0
	 */ 
	public void setModifiers(int modifiers) {
		modifying();
		this.modifiers = modifiers;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}
}

