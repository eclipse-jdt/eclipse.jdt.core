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
 * Abstract subclass for type declaration, enum declaration,
 * and annotation type declaration AST node types.
 * <pre>
 * AbstractTypeDeclaration:
 * 		TypeDeclaration
 * 		EnumDeclaration
 * 		AnnotationTypeDeclaration
 * </pre>
 * <p>
 * Note: Support for annotation metadata is an experimental language feature 
 * under discussion in JSR-175 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * <p>
 * Note: Enum declarations are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public abstract class AbstractTypeDeclaration extends BodyDeclaration {
	
	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal Java class identifier.
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */
	SimpleName typeName = null;

	/**
	 * The body declarations (element type: <code>BodyDeclaration</code>).
	 * Defaults to an empty list.
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */
	ASTNode.NodeList bodyDeclarations = 
		new ASTNode.NodeList(true, BodyDeclaration.class);

	/**
	 * Creates a new AST node for an abstract type declaration owned by the given 
	 * AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AbstractTypeDeclaration(AST ast) {
		super(ast);
	}

	/**
	 * Returns the name of the type declared in this type declaration.
	 * 
	 * @return the type name node
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */ 
	public SimpleName getName() {
		if (this.typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.typeName;
	}
		
	/**
	 * Sets the name of the type declared in this type declaration to the
	 * given name.
	 * 
	 * @param typeName the new type name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */ 
	public void setName(SimpleName typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeName, typeName, false);
		this.typeName = typeName;
	}

	/**
	 * Returns the live ordered list of body declarations of this type 
	 * declaration.
	 * 
	 * @return the live list of body declarations
	 *    (element type: <code>BodyDeclaration</code>)
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */ 
	public List bodyDeclarations() {
		return this.bodyDeclarations;
	}
	
	/**
	 * Returns whether this type declaration is a package member (that is,
	 * a top-level type).
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a compilation unit node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a compilation unit node, and <code>false</code> otherwise
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */
	public boolean isPackageMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof CompilationUnit);
	}

	/**
	 * Returns whether this type declaration is a type member.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration node, an anonymous 
	 * class declaration, or an enumeration constant declaration.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration node, a class instance creation node, or an
	 *   enum constant declaration, and <code>false</code> otherwise
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */
	public boolean isMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof AbstractTypeDeclaration)
			|| (parent instanceof AnonymousClassDeclaration)
			|| (parent instanceof EnumConstantDeclaration);
	}

	/**
	 * Returns whether this type declaration is a local type.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration statement node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration statement node, and <code>false</code> otherwise
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */
	public boolean isLocalTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof TypeDeclarationStatement);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 2 * 4;
	}
	
}
