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

/**
 * Type node for a wildcard type (added in 3.0 API).
 * <pre>
 * WildcardType:
 *    <b>?</b> [ ( <b>extends</b> | <b>super</b>) Type ] 
 * </pre>
 * <p>
 * Not all node arrangements will represent legal Java constructs. In particular,
 * it is nonsense if a wildcard type node appears anywhere other than as an
 * argument of a <code>ParameterizedType</code> node.
 * </p>
 * <p>
 * Note: Support for generic types is an experimental language feature 
 * under discussion in JSR-014 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class WildcardType extends Type {
	/** 
	 * The optional type bound node; <code>null</code> if none;
	 * defaults to none.
	 */
	private Type optionalBound = null;
	
	/**
	 * Indicates whether the wildcard bound is an upper bound
	 * ("extends") as opposed to a lower bound ("super"). 
	 * Always <code>true</code> when there is no bound.
	 * Defaults to <code>true</code> initially.
	 */
	private boolean isUpperBound = true;

	/**
	 * Creates a new unparented node for a wildcard type owned by the
	 * given AST. By default, no bound.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	WildcardType(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return WILDCARD_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		WildcardType result = new WildcardType(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setBound((Type) ASTNode.copySubtree(target, getBound()), isUpperBound());
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
			acceptChild(visitor, getBound());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns whether this wildcard type is an upper bound
	 * ("extends") as opposed to a lower bound ("super").
	 * <p>
	 * Note that this property is irrelevant for wildcards
	 * that do not have a bound; this method always returns 
	 * <code>true</code> in that case.
	 * </p>
	 * 
	 * @return <code>true</code> if an upper bound,
	 *    and <code>false</code> if a lower bound
	 * @see #setBound
	 */ 
	public boolean isUpperBound() {
		return isUpperBound;
	}
	
	/**
	 * Returns the bound of this wildcard type if it has one.
	 * If {@link #isUpperBound isUpperBound} returns true, this
	 * is an upper bound ("? extends B"); if it returns false, this
	 * is a lower bound ("? super B").
	 * 
	 * @return the bound of this wildcard type, or <code>null</code>
	 * if none
	 * @see #setBound
	 */ 
	public Type getBound() {
		return this.optionalBound;
	}
	
	/**
	 * Sets the bound of this wildcard type to the given type and
	 * marks it as an upper or a lower bound.
	 * 
	 * @param type the new bound of this wildcard type, or <code>null</code>
	 * if none
	 * @param isUpperBound <code>true</code> for an upper bound ("? extends B"),
	 * and <code>false</code> for a lower bound ("? super B"); ignored if the
	 * given bound is <code>null</code>
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @see #getBound
	 * @see #isUpperBound
	 */ 
	public void setBound(Type type, boolean isUpperBound) {
		// a WildcardType may occur inside an WildcardType - must check cycles
		replaceChild(this.optionalBound, type, true);
		this.optionalBound = type;
		this.isUpperBound = (type == null) ? true : isUpperBound;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
		memSize()
		+ (optionalBound == null ? 0 : getBound().treeSize());
	}
}

