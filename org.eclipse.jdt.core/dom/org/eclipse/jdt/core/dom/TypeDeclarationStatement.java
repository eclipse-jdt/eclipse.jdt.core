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

/**
 * Local type declaration statement AST node type.
 * <p>
 * This kind of node is used to convert a type declaration
 * node into a statement node by wrapping it.
 * </p>
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * TypeDeclarationStatement:
 *    TypeDeclaration
 * </pre>
 * For 3.0 (corresponding to JLS3), the kinds of type declarations
 * grew to include enum and annotation type declarations:
 * <pre>
 * TypeDeclarationStatement:
 *    TypeDeclaration
 *    EnumDeclaration
 *    AnnotationTypeDeclaration
 * </pre>
 * 
 * @since 2.0
 */
public class TypeDeclarationStatement extends Statement {
	
	/**
	 * The type declaration; lazily initialized; defaults to a unspecified, 
	 * but legal, type declaration.
	 */
	private AbstractTypeDeclaration typeDecl = null;

	/**
	 * Creates a new unparented local type declaration statement node owned 
	 * by the given AST. By default, the local type declaration is an
	 * unspecified, but legal, type declaration.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TypeDeclarationStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TYPE_DECLARATION_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TypeDeclarationStatement result = 
			new TypeDeclarationStatement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.copyLeadingComment(this);
		result.setDeclaration(
			(AbstractTypeDeclaration) getDeclaration().clone(target));
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
			acceptChild(visitor, getDeclaration());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the abstract type declaration of this local type declaration
	 * statement (2.0 API only).
	 * 
	 * @return the type declaration node
	 * @since 3.0
	 */ 
	public AbstractTypeDeclaration getDeclaration() {
		if (this.typeDecl == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setDeclaration(new TypeDeclaration(getAST()));
			getAST().setModificationCount(count);
		}
		return this.typeDecl;
	}
		
	/**
	 * Sets the abstract type declaration of this local type declaration
	 * statement (2.0 API only).
	 * 
	 * @param decl the type declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 * @since 3.0
	 */ 
	public void setDeclaration(AbstractTypeDeclaration decl) {
		if (decl == null) {
			throw new IllegalArgumentException();
		}
		// a TypeDeclarationStatement may occur inside an 
		// TypeDeclaration - must check cycles
		replaceChild(this.typeDecl, decl, true);
		this.typeDecl= decl;
	}
	
	/**
	 * Returns the type declaration of this local type declaration
	 * statement (added in 3.0 API).
	 * 
	 * @return the type declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by 
	 * <code>getDeclaration</code>,
	 * which returns <code>AbstractTypeDeclaration</code> instead of 
	 * <code>TypeDeclaration</code>.
	 */ 
	public TypeDeclaration getTypeDeclaration() {
	    supportedOnlyIn2();
		return (TypeDeclaration) getDeclaration();
	}
		
	/**
	 * Sets the type declaration of this local type declaration
	 * statement (added in 3.0 API).
	 * 
	 * @param decl the type declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by 
	 * <code>setDeclaration</code>,
	 * which takes <code>AbstractTypeDeclaration</code> instead of 
	 * <code>TypeDeclaration</code>.
	 */ 
	public void setTypeDeclaration(TypeDeclaration decl) {
	    supportedOnlyIn2();
		// forward to non-deprecated replacement method
		setDeclaration(decl);
	}
	
	/**
	 * Resolves and returns the binding for the class or interface declared in
	 * this type declaration statement.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		// forward request to the wrapped type declaration
		AbstractTypeDeclaration d = getDeclaration();
		if (d instanceof TypeDeclaration) {
			return ((TypeDeclaration) d).resolveBinding();
		} else if (d instanceof AnnotationTypeDeclaration) {
			return ((AnnotationTypeDeclaration) d).resolveBinding();
		} else {
			// shouldn't happen
			return null;
		}
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
			+ (this.typeDecl == null ? 0 : getDeclaration().treeSize());
	}
}

