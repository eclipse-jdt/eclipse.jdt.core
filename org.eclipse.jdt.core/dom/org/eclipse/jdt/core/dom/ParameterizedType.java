/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
 * Type node for a parameterized type (added in 3.0 API).
 * <pre>
 * ParameterizedType:
 *    Name <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b>
 * </pre>
 * <p>
 * Note: Support for generic types is an experimental language feature 
 * under discussion in JSR-014 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class ParameterizedType extends Type {
	/** 
	 * The type name node; lazily initialized; defaults to a type with
	 * an unspecfied, but legal, name.
	 */
	private Name typeName = null;
	
	/**
	 * The type arguments (element type: <code>Type</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList typeArguments =
		new ASTNode.NodeList(true, Type.class);
	
	/**
	 * Creates a new unparented node for a parameterized type owned by the
	 * given AST. By default, an unspecified, but legal, name, and no type
	 * arguments.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ParameterizedType(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return PARAMETERIZED_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ParameterizedType result = new ParameterizedType(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setName((Name) ((ASTNode) getName()).clone(target));
		result.typeArguments().addAll(
			ASTNode.copySubtrees(target, typeArguments()));
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getName());
			acceptChildren(visitor, typeArguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of this parameterized type.
	 * 
	 * @return the name of this parameterized type
	 */ 
	public Name getName() {
		if (typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return typeName;
	}
	
	/**
	 * Sets the name of this parameterized type to the given name.
	 * 
	 * @param typeName the new name of this parameterized type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeName, typeName, false);
		this.typeName = typeName;
	}

	/**
	 * Returns the live ordered list of type arguments of this parameterized 
	 * type. For the parameterized type to be plausible, the list should contain
	 * at least one element and not contain primitive types.
	 * 
	 * @return the live list of type arguments
	 *    (element type: <code>Type</code>)
	 */ 
	public List typeArguments() {
		return typeArguments;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (typeName == null ? 0 : getName().treeSize())
			+ typeArguments.listSize();
	}
}

