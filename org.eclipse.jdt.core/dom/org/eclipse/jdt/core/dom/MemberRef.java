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
 * AST node for a member reference within a doc comment
 * ({@link Javadoc Javadoc}). The principal uses of these are in "@see" and "@link"
 * tag elements, for references to field members (and occasionally to method
 * and constructor members).
 * <pre>
 * MemberRef:
 * 		[ Name ] <b>#</b> Identifier
 * </pre>
 * 
 * @see Javadoc
 * @since 3.0
 */
public class MemberRef extends ASTNode implements IDocElement {
	
	/**
	 * The optional qualifier; <code>null</code> for none; defaults to none.
	 */
	private Name optionalQualifier = null;

	/**
	 * The member name; lazily initialized; defaults to a unspecified,
	 * legal Java method name.
	 */
	private SimpleName memberName = null;
	
	/**
	 * Creates a new AST node for a member reference owned by the given 
	 * AST. By default, the method reference is for a member with an
	 * unspecified, but legal, name; and no qualifier.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	MemberRef(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return MEMBER_REF;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		MemberRef result = new MemberRef(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setQualifier((Name) ASTNode.copySubtree(target, getQualifier()));
		result.setName((SimpleName) ASTNode.copySubtree(target, getName()));
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
			acceptChild(visitor, getQualifier());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the qualifier of this member reference, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the qualifier name node, or <code>null</code> if there is none
	 */ 
	public Name getQualifier() {
		return optionalQualifier;
	}
	
	/**
	 * Sets or clears the qualifier of this member reference.
	 * 
	 * @param name the qualifier name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setQualifier(Name name) {
		// a MemberRef cannot occur inside a Name - no cycle check
		replaceChild(this.optionalQualifier, name, false);
		this.optionalQualifier = name;
	}

	/**
	 * Returns the name of the referenced member.
	 * 
	 * @return the member name node
	 */ 
	public SimpleName getName() {
		if (memberName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return memberName;
	}
	
	/**
	 * Sets the name of the referenced member to the given name.
	 * 
	 * @param name the new member name node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the name is <code>null</code></li>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.memberName, name, false);
		this.memberName = name;
	}

	/**
	 * Resolves and returns the binding for the entity referred to by
	 * this member reference.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public final IBinding resolveBinding() {
		return getAST().getBindingResolver().resolveReference(this);
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
			+ (optionalQualifier == null ? 0 : getQualifier().treeSize())
			+ (memberName == null ? 0 : getName().treeSize());
	}
}

