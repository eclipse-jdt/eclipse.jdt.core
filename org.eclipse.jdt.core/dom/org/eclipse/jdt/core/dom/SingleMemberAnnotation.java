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
 * Single member annotation node (added in 3.0 API). The single member annotation 
 * "@foo(bar)" is equivalent to the normal annotation "@foo(value=bar)". 
 * <p>
 * <pre>
 * SingleMemberAnnotation:
 *   <b>@</b> TypeName <b>(</b> Expression  <b>)</b>
 * </pre>
 * </p>
 * <p>
 * Note: Support for annotation metadata is an experimental language feature 
 * under discussion in JSR-175 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * @since 3.0
 */
public final class SingleMemberAnnotation extends Annotation {
	/**
	 * The value; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression value = null;

	/**
	 * Creates a new unparented normal annotation node owned 
	 * by the given AST.  By default, the annotation has an
	 * unspecified type name and an unspecified value.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SingleMemberAnnotation(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SINGLE_MEMBER_ANNOTATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SingleMemberAnnotation result = new SingleMemberAnnotation(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setTypeName((Name) ASTNode.copySubtree(target, getTypeName()));
		result.setValue((Expression) ASTNode.copySubtree(target, getValue()));
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
			acceptChild(visitor, getTypeName());
			acceptChild(visitor, getValue());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the value of this annotation.
	 * 
	 * @return the value node
	 */ 
	public Expression getValue() {
		if (this.value == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setValue(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.value;
	}
		
	/**
	 * Sets the value of this annotation.
	 * 
	 * @param value the new value
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setValue(Expression value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		// a SingleMemberAnnotation may occur inside an Expression 
		// must check cycles
		replaceChild(this.value, value, true);
		this.value = value;
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
		return
			memSize()
			+ getTypeName().treeSize()
			+ (this.value == null ? 0 : getValue().treeSize());
	}
}
