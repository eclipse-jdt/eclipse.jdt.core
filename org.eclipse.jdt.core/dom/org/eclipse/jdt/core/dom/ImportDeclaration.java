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
 * Import declaration AST node type.
 *
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * ImportDeclaration:
 *    <b>import</b> Name [ <b>.</b> <b>*</b> ] <b>;</b>
 * </pre>
 * For 3.0 (corresponding to JLS3), static was added:
 * <pre>
 * ImportDeclaration:
 *    <b>import</b> [ <b>static</b> ] Name [ <b>.</b> <b>*</b> ] <b>;</b>
 * </pre>
 * 
 * <p>
 * Note: Static imports are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * @since 2.0
 */
public class ImportDeclaration extends ASTNode {
	
	/**
	 * The "name" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(ImportDeclaration.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "onDemand" structural property of this node type.
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor ON_DEMAND_PROPERTY = 
		new SimplePropertyDescriptor(ImportDeclaration.class, "onDemand", boolean.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * The "static" structural property of this node type (added in 3.0 API).
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor STATIC_PROPERTY = 
		new SimplePropertyDescriptor(ImportDeclaration.class, "static", boolean.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		createPropertyList(ImportDeclaration.class);
		addProperty(NAME_PROPERTY);
		addProperty(ON_DEMAND_PROPERTY);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList();
		
		createPropertyList(ImportDeclaration.class);
		addProperty(STATIC_PROPERTY);
		addProperty(NAME_PROPERTY);
		addProperty(ON_DEMAND_PROPERTY);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.LEVEL_&ast;</code> constants

	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.LEVEL_2_0) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
			
	/**
	 * The import name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private Name importName = null;

	/**
	 * On demand versus single type import; defaults to single type import.
	 */
	private boolean onDemand = false;

	/**
	 * Static versus regular; defaults to regular import.
	 * Added in 3.0; not used in 2.0.
	 * @since 3.0
	 */
	private boolean isStatic = false;

	/**
	 * Creates a new AST node for an import declaration owned by the
	 * given AST. The import declaration initially is a regular (non-static)
	 * single type import for an unspecified, but legal, Java type name.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ImportDeclaration(AST ast) {
		super(ast);
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
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == ON_DEMAND_PROPERTY) {
			if (get) {
				return isOnDemand();
			} else {
				setOnDemand(value);
				return false;
			}
		}
		if (property == STATIC_PROPERTY) {
			if (get) {
				return isStatic();
			} else {
				setStatic(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Name) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return IMPORT_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		ImportDeclaration result = new ImportDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setOnDemand(isOnDemand());
		if (this.ast.apiLevel >= AST.LEVEL_3_0) {
			result.setStatic(isStatic());
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
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name imported by this declaration.
	 * <p>
	 * For a regular on-demand import, this is the name of a package. 
	 * For a static on-demand import, this is the qualified name of
	 * a type. For a regular single-type import, this is the qualified name
	 * of a type. For a static single-type import, this is the qualified name
	 * of a static member of a type.
	 * </p>
	 * 
	 * @return the imported name node
	 */ 
	public Name getName()  {
		if (this.importName == null) {
			preLazyInit();
			this.importName =this.ast.newQualifiedName(
					new SimpleName(this.ast), new SimpleName(this.ast));
			postLazyInit(this.importName, NAME_PROPERTY);
		}
		return importName;
	}
	
	/**
	 * Sets the name of this import declaration to the given name.
	 * <p>
	 * For a regular on-demand import, this is the name of a package. 
	 * For a static on-demand import, this is the qualified name of
	 * a type. For a regular single-type import, this is the qualified name
	 * of a type. For a static single-type import, this is the qualified name
	 * of a static member of a type.
	 * </p>
	 * 
	 * @param name the new import name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.importName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.importName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}
		
	/**
	 * Returns whether this import declaration is an on-demand or a 
	 * single-type import.
	 * 
	 * @return <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */ 
	public boolean isOnDemand() {
		return onDemand;
	}
		
	/**
	 * Sets whether this import declaration is an on-demand or a 
	 * single-type import.
	 * 
	 * @param onDemand <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */ 
	public void setOnDemand(boolean onDemand) {
		preValueChange(ON_DEMAND_PROPERTY);
		this.onDemand = onDemand;
		postValueChange(ON_DEMAND_PROPERTY);
	}
	
	/**
	 * Returns whether this import declaration is a static import (added in 3.0 API).
	 * <p>
	 * Note: Static imports are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return <code>true</code> if this is a static import,
	 *    and <code>false</code> if this is a regular import
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public boolean isStatic() {
		unsupportedIn2();
		return isStatic;
	}
		
	/**
	 * Sets whether this import declaration is a static import (added in 3.0 API).
	 * <p>
	 * Note: Static imports are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @param isStatic <code>true</code> if this is a static import,
	 *    and <code>false</code> if this is a regular import
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public void setStatic(boolean isStatic) {
		unsupportedIn2();
		preValueChange(STATIC_PROPERTY);
		this.isStatic = isStatic;
		postValueChange(STATIC_PROPERTY);
	}
	
	/**
	 * Resolves and returns the binding for the package or type imported by
	 * this import declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the package binding (for on-demand imports) or type binding
	 *    (for single-type imports), or <code>null</code> if the binding cannot
	 *    be resolved
	 */	
	public IBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveImport(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (importName == null ? 0 : getName().treeSize());
	}
}

