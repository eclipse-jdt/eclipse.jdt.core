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
 *    Type <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b>
 * </pre>
 * The first type may be a simple type or a qualified type;
 * other kinds of types are meaningless.
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
	 * @since 3.0
	 * @deprecated Replaced by TYPE_PROPERTY
	 * TODO (jeem) - Remove before M9
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(ParameterizedType.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "type" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY = 
		new ChildPropertyDescriptor(ParameterizedType.class, "type", Type.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "typeArguments" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor TYPE_ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(ParameterizedType.class, "typeArguments", Type.class, CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(ParameterizedType.class);
		addProperty(NAME_PROPERTY);
		addProperty(TYPE_PROPERTY);
		addProperty(TYPE_ARGUMENTS_PROPERTY);
		PROPERTY_DESCRIPTORS = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.LEVEL_*</code>LEVEL

	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
			
	/** 
	 * @since 3.0
	 * @deprecated Replaced by TYPE_PROPERTY
	 * TODO (jeem) - Remove before M9
	 */
	private Name typeName = null;
	
	/** 
	 * The type node; lazily initialized; defaults to an unspecfied, but legal,
	 * type.
	 */
	private Type type = null;
	
	/**
	 * The type arguments (element type: <code>Type</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList typeArguments =
		new ASTNode.NodeList(TYPE_ARGUMENTS_PROPERTY);
	
	/**
	 * Creates a new unparented node for a parameterized type owned by the
	 * given AST. By default, an unspecified, but legal, type, and no type
	 * arguments.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ParameterizedType(AST ast) {
		super(ast);
	    unsupportedIn2();
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
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Name) child);
				return null;
			}
		}
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((Type) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == TYPE_ARGUMENTS_PROPERTY) {
			return typeArguments();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
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
	ASTNode clone0(AST target) {
		ParameterizedType result = new ParameterizedType(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setName((Name) ((ASTNode) getName()).clone(target));
		result.setType((Type) ((ASTNode) getType()).clone(target));
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
			acceptChild(visitor, getType());
			acceptChildren(visitor, this.typeArguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * @since 3.0
	 * deprecated Replaced by getType(), which returns a Type
	 * TODO (jeem) - Remove before M9
	 */ 
	public Name getName() {
		if (this.typeName == null) {
			preLazyInit();
			this.typeName = new SimpleName(this.ast);
			postLazyInit(this.typeName, NAME_PROPERTY);
		}
		return this.typeName;
	}
	
	/**
	 * @since 3.0
	 * @deprecated Replaced by setType(), which takes a Type
	 * TODO (jeem) - Remove before M9
	 */ 
	public void setName(Name typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.typeName;
		preReplaceChild(oldChild, typeName, NAME_PROPERTY);
		this.typeName = typeName;
		postReplaceChild(oldChild, typeName, NAME_PROPERTY);
	}

	/**
	 * Returns the type of this parameterized type.
	 * 
	 * @return the type of this parameterized type
	 */ 
	public Type getType() {
		if (this.type == null) {
			preLazyInit();
			this.type = new SimpleType(this.ast);
			postLazyInit(this.type, TYPE_PROPERTY);
		}
		return this.type;
	}
	
	/**
	 * Sets the type of this parameterized type.
	 * 
	 * @param type the new type of this parameterized type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.type;
		preReplaceChild(oldChild, type, TYPE_PROPERTY);
		this.type = type;
		postReplaceChild(oldChild, type, TYPE_PROPERTY);
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
		return this.typeArguments;
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
			+ (this.type == null ? 0 : getType().treeSize())
			+ this.typeArguments.listSize();
	}
}

