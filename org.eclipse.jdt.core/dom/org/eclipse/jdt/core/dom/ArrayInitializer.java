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
 * Array initializer AST node type.
 *
 * <pre>
 * ArrayInitializer:
 * 		<b>{</b> [ Expression { <b>,</b> Expression} [ <b>,</b> ]] <b>}</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ArrayInitializer extends Expression {
	
	/**
	 * The "expressions" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor EXPRESSIONS_PROPERTY = 
		new ChildListPropertyDescriptor(ArrayInitializer.class, "expressions", Expression.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(ArrayInitializer.class);
		addProperty(EXPRESSIONS_PROPERTY);
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
	 * The list of expressions (element type:
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList expressions =
		new ASTNode.NodeList(EXPRESSIONS_PROPERTY);

	/**
	 * Creates a new AST node for an array initializer owned by the 
	 * given AST. By default, the list of expressions is empty.
	 * 
	 * @param ast the AST that is to own this node
	 */
	ArrayInitializer(AST ast) {
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
		if (property == EXPRESSIONS_PROPERTY) {
			return expressions();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ARRAY_INITIALIZER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		ArrayInitializer result = new ArrayInitializer(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.expressions().addAll(ASTNode.copySubtrees(target, expressions()));
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
			acceptChildren(visitor, this.expressions);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of expressions in this array initializer.
	 * 
	 * @return the live list of expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List expressions() {
		return this.expressions;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + this.expressions.listSize();
	}
}

