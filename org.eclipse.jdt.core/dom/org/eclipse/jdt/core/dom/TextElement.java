/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 * AST node for a text element within a doc comment.
 * <pre>
 * TextElement:
 *     Characters
 * </pre>
 * 
 * @see Javadoc
 * @since 3.0
 */
public final class TextElement extends ASTNode implements IDocElement {

	/**
	 * Canonical empty string.
	 */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * The text element; defaults to the empty string.
	 */
	private String text = EMPTY_STRING;
	
	/**
	 * Creates a new AST node for a text element owned by the given AST.
	 * The new node has an empty text string.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TextElement(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TEXT_ELEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TextElement result = new TextElement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setText(getText());
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
	 * Returns this node's text.
	 * 
	 * @return the text of this node
	 */ 
	public String getText() {
		return this.text;
	}
	
	/**
	 * Sets the text of this node to the given value.
	 * 
	 * @param text the text of this node
	 * @exception IllegalArgumentException if the text is null
	 */ 
	public void setText(String text) {
		if (text == null) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.text = text;
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		if (this.text != EMPTY_STRING) {
			// everything but our empty string costs
			size += stringSize(this.text);
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

