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
 * Package declaration AST node type.
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * PackageDeclaration:
 *    <b>package</b> Name <b>;</b>
 * </pre>
 * For 3.0 (corresponding to JLS3), annotations were added:
 * <pre>
 * PackageDeclaration:
 *    { Annotation } <b>package</b> Name <b>;</b>
 * </pre>
 * 
 * @since 2.0
 */
public class PackageDeclaration extends ASTNode {
	
	/**
	 * The annotations (element type: <code>Annotation</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList annotations = null;
	
	/**
	 * The package name; lazily initialized; defaults to a unspecified,
	 * legal Java package identifier.
	 */
	private Name packageName = null;

	/**
	 * Creates a new AST node for a package declaration owned by the
	 * given AST. The package declaration initially has an unspecified,
	 * but legal, Java identifier; and an empty list of annotations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	PackageDeclaration(AST ast) {
		super(ast);
		if (ast.API_LEVEL >= AST.LEVEL_3_0) {
			this.annotations = new ASTNode.NodeList(true, Annotation.class);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return PACKAGE_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		PackageDeclaration result = new PackageDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
			result.annotations().addAll(ASTNode.copySubtrees(target, annotations()));
		}
		result.setName((Name) getName().clone(target));
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
			if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
				acceptChildren(visitor, this.annotations);
			}
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of annotations of this 
	 * package declaration (added in 3.0 API).
	 * <p>
	 * Note: Support for annotation metadata is an experimental language feature 
	 * under discussion in JSR-175 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the live list of annotations
	 *    (element type: <code>Annotation</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List annotations() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.annotations == null) {
			unsupportedIn2();
		}
		return this.annotations;
	}
	
	/**
	 * Returns the package name of this package declaration.
	 * 
	 * @return the package name node
	 */ 
	public Name getName() {
		if (this.packageName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.packageName;
	}
	
	/**
	 * Sets the package name of this package declaration to the given name.
	 * 
	 * @param name the new package name
	 * @exception IllegalArgumentException if`:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.packageName, name, false);
		this.packageName = name;
	}
	
	/**
	 * Resolves and returns the binding for the package declared in this package
	 * declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IPackageBinding resolveBinding() {
		return getAST().getBindingResolver().resolvePackage(this);
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
			+ (this.annotations == null ? 0 : this.annotations.listSize())
			+ (this.packageName == null ? 0 : getName().treeSize());
	}
}

