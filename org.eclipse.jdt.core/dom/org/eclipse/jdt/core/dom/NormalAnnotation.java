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

import java.util.List;

/**
 * Normal annotation node (added in 3.0 API).
 * <p>
 * <pre>
 * NormalAnnotation:
 *   <b>@</b> TypeName <b>(</b> [ MemberValuePair { <b>,</b> MemberValuePair } ] <b>)</b>
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
public final class NormalAnnotation extends Annotation {
	/**
	 * The list of member value pairs (element type: 
	 * <code MemberValuePair</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList values = 
		new ASTNode.NodeList(true, MemberValuePair.class);

	/**
	 * Creates a new unparented normal annotation node owned 
	 * by the given AST.  By default, the annotation has an
	 * unspecified type name and an empty list of member value
	 * pairs.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	NormalAnnotation(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return NORMAL_ANNOTATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		NormalAnnotation result = new NormalAnnotation(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setTypeName((Name) ASTNode.copySubtree(target, getTypeName()));
		result.values().addAll(ASTNode.copySubtrees(target, values()));
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
			acceptChildren(visitor, this.values);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live list of member value pairs in this annotation.
	 * Adding and removing nodes from this list affects this node
	 * dynamically. All nodes in this list must be 
	 * {@link MemberValuePair}s; attempts to add any other 
	 * type of node will trigger an exception.
	 * 
	 * @return the live list of member value pairs in this 
	 *    annotation (element type: <code>MemberValuePair</code>)
	 */ 
	public List values() {
		return this.values;
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
			+ values.listSize();
	}
}
