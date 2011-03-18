/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Type node for a disjunctive type.
 * <pre>
 * DisjunctiveType:
 *    Type <b>|</b> Type { <b>|</b> Type }
 * </pre>
 * <p>
 * This kind of node is used inside a catch formal parameter type.
 * </p>
 *
 * @since 3.7
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DisjunctiveType extends Type {

	/**
	 * The "types" structural property of this node type (element type: {@link Type}).
	 */
	public static final ChildListPropertyDescriptor TYPES_PROPERTY =
		new ChildListPropertyDescriptor(DisjunctiveType.class, "types", Type.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(4);
		createPropertyList(DisjunctiveType.class, propertyList);
		addProperty(TYPES_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * The list of types (element type: {@link Type}).  Defaults to an empty list.
	 */
	private ASTNode.NodeList types = new ASTNode.NodeList(TYPES_PROPERTY);

	/**
	 * Creates a new unparented node for a disjunctive type owned by the given AST.
	 * By default, it has no types.<p>
	 * 
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	DisjunctiveType(AST ast) {
		super(ast);
		supportedOnlyIn4();
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
		if (property == TYPES_PROPERTY) {
			return types();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return DISJUNCTIVE_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		DisjunctiveType result = new DisjunctiveType(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.types().addAll(
				ASTNode.copySubtrees(target, types()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, this.types);
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the live list of types in this disjunctive type.
	 * Adding and removing nodes from this list affects this node
	 * dynamically. All nodes in this list must be
	 * <code>Type</code>s; attempts to add any other
	 * type of node will trigger an exception.
	 *
	 * @return the live list of types in this disjunctive type (element type: {@link Type})
	 */
	public List types() {
		return this.types;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 1 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ this.types.listSize();
	}
}

