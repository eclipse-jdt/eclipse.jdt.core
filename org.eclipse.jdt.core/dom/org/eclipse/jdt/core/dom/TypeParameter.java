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
 * Type parameter node (added in 3.0 API).
 * <pre>
 * TypeParameter:
 *    TypeVariable [ <b>extends</b> Type { <b>&</b> Type } ]
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
public class TypeParameter extends ASTNode {
	/** 
	 * The type variable node; lazily initialized; defaults to an unspecfied, 
	 * but legal, name.
	 */
	private SimpleName typeVariableName = null;
	
	/**
	 * The type bounds (element type: <code>Type</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList typeBounds =
		new ASTNode.NodeList(false, Type.class);
	
	/**
	 * Creates a new unparented node for a parameterized type owned by the
	 * given AST. By default, an unspecified, but legal, type variable name, 
	 * and no type bounds.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TypeParameter(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TYPE_PARAMETER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TypeParameter result = new TypeParameter(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setName((SimpleName) ((ASTNode) getName()).clone(target));
		result.typeBounds().addAll(
			ASTNode.copySubtrees(target, typeBounds()));
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
			acceptChildren(visitor, typeBounds);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of the type variable declared in this type parameter.
	 * 
	 * @return the name of the type variable
	 */ 
	public SimpleName getName() {
		if (typeVariableName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return typeVariableName;
	}
	
	/**
	 * Sets the name of the type variable of this type parameter to the given
	 * name.
	 * 
	 * @param typeName the new name of this type parameter 
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeVariableName, typeName, false);
		this.typeVariableName = typeName;
	}

	/**
	 * Returns the live ordered list of type bounds of this type parameter.
	 * For the type parameter to be plausible, there can be at most one
	 * class in the list, and it must be first, and the remaining ones must be
	 * interfaces; the list should not contain primitive types (but array types
	 * and parameterized types are allowed).
	 * 
	 * @return the live list of type bounds
	 *    (element type: <code>Type</code>)
	 */ 
	public List typeBounds() {
		return typeBounds;
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
			+ (typeVariableName == null ? 0 : getName().treeSize())
			+ typeBounds.listSize();
	}
}

