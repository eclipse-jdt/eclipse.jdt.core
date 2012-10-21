/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Type node for a named class type, a named interface type, or a type variable.
 * <p>
 * This kind of node is used to convert a name (<code>Name</code>) into a type
 * (<code>Type</code>) by wrapping it.
 * </p>
 *
 * In JLS8, the SimpleType may have optional annotations.
 * 
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SimpleType extends Type {

	/**
	 * The "name" structural property of this node type (child type: {@link Name}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(SimpleType.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "annotations" structural property of this node type (child type: {@link Annotation}).
	 * @since 3.9
	 */
	public static final ChildListPropertyDescriptor ANNOTATIONS_PROPERTY =
		new ChildListPropertyDescriptor(SimpleType.class, "annotations", Annotation.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.9
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(SimpleType.class, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(3);
		createPropertyList(SimpleType.class, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(ANNOTATIONS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_8_0 = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		switch (apiLevel) {
			case AST.JLS2_INTERNAL :
			case AST.JLS3_INTERNAL :
			case AST.JLS4:
				return PROPERTY_DESCRIPTORS;
			default :
				return PROPERTY_DESCRIPTORS_8_0;
		}
	}

	/**
	 * The type name node; lazily initialized; defaults to a type with
	 * an unspecfied, but legal, name.
	 */
	private Name typeName = null;

	/**
	 * Creates a new unparented node for a simple type owned by the given AST.
	 * By default, an unspecified, but legal, name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	SimpleType(AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.JLS8) {
			this.annotations = new ASTNode.NodeList(ANNOTATIONS_PROPERTY);
		}
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
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == ANNOTATIONS_PROPERTY) {
			return annotations();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
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
	final int getNodeType0() {
		return SIMPLE_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		SimpleType result = new SimpleType(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setName((Name) (getName()).clone(target));
		if (this.ast.apiLevel >= AST.JLS8) {
			result.annotations = new ASTNode.NodeList(ANNOTATIONS_PROPERTY);
			result.annotations.addAll(
					ASTNode.copySubtrees(target, annotations()));
		}
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			if (this.ast.apiLevel >= AST.JLS8) {
				acceptChildren(visitor, this.annotations);
			}
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the name of this simple type.
	 *
	 * @return the name of this simple type
	 */
	public Name getName() {
		if (this.typeName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.typeName == null) {
					preLazyInit();
					this.typeName = new SimpleName(this.ast);
					postLazyInit(this.typeName, NAME_PROPERTY);
				}
			}
		}
		return this.typeName;
	}

	/**
	 * Sets the name of this simple type to the given name.
	 *
	 * @param typeName the new name of this simple type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
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
			+ (this.annotations == null ? 0 : this.annotations.listSize())
			+ (this.typeName == null ? 0 : getName().treeSize());
	}
}

