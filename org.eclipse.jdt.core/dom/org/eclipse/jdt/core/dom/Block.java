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

/**
 * Block statement AST node type.
 *
 * <pre>
 * Block:
 *    <b>{</b> { Statement } <b>}</b>
 * </pre>
 * 
 * @since 2.0
 */
public class Block extends Statement {
	
	/**
	 * The "statements" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor STATEMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(Block.class, "statements", Statement.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(Block.class);
		addProperty(STATEMENTS_PROPERTY);
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
	 * The list of statements (element type: <code>Statement</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList statements = 
		new ASTNode.NodeList(STATEMENTS_PROPERTY);

	/**
	 * Creates a new unparented block node owned by the given AST.
	 * By default, the block is empty.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Block(AST ast) {
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
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == STATEMENTS_PROPERTY) {
			return statements();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return BLOCK;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		Block result = new Block(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.copyLeadingComment(this);
		result.statements().addAll(
			ASTNode.copySubtrees(target, statements()));
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
			acceptChildren(visitor, this.statements);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live list of statements in this block. Adding and
	 * removing nodes from this list affects this node dynamically.
	 * All nodes in this list must be <code>Statement</code>s;
	 * attempts to add any other type of node will trigger an
	 * exception.
	 * 
	 * @return the live list of statements in this block
	 *    (element type: <code>Statement</code>)
	 */ 
	public List statements() {
		return this.statements;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + this.statements.listSize();
	}
}

