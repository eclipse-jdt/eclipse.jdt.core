/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
 * Type node for an array type.
 * <p>
 * Array types are expressed in a recursive manner, one dimension at a time.
 * </p>
 * <pre>
 * ArrayType:
 *    Type { Annotation } <b>'['</b> <b>']'</b>
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ArrayType extends AnnotatableType {

	/**
	 * The "componentType" structural property of this node type (child type: {@link Type}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor COMPONENT_TYPE_PROPERTY =
		new ChildPropertyDescriptor(ArrayType.class, "componentType", Type.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "annotations" structural property of this node type (element type: {@link Annotation}).
	 * @since 3.9
	 */
	public static final ChildListPropertyDescriptor ANNOTATIONS_PROPERTY =
			internalAnnotationsPropertyFactory(ArrayType.class);
	
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
		createPropertyList(ArrayType.class, propertyList);
		addProperty(COMPONENT_TYPE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);

		propertyList = new ArrayList(3);
		createPropertyList(ArrayType.class, propertyList);
		addProperty(COMPONENT_TYPE_PROPERTY, propertyList);
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
			case AST.JLS4_INTERNAL:
				return PROPERTY_DESCRIPTORS;
			default :
				return PROPERTY_DESCRIPTORS_8_0;
		}
	}

	/**
	 * The component type; lazily initialized; defaults to a simple type with
	 * an unspecified, but legal, name.
	 */
	private Type componentType = null;

	/**
	 * Creates a new unparented node for an array type owned by the given AST.
	 * By default, a 1-dimensional array of an unspecified simple type.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	ArrayType(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on AnnotatableType.
	 * @since 3.9
	 */
	final ChildListPropertyDescriptor internalAnnotationsProperty() {
		return ANNOTATIONS_PROPERTY;
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
		if (property == COMPONENT_TYPE_PROPERTY) {
			if (get) {
				return getComponentType();
			} else {
				setComponentType((Type) child);
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
		return ARRAY_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		ArrayType result = new ArrayType(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setComponentType((Type) getComponentType().clone(target));
		if (this.ast.apiLevel >= AST.JLS8) {
			result.annotations().addAll(
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getComponentType());
			if (this.ast.apiLevel >= AST.JLS8) {
				acceptChildren(visitor, this.annotations);
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the component type of this array type. The component type
	 * may be another array type.
	 *
	 * @return the component type node
	 */
	public Type getComponentType() {
		if (this.componentType == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.componentType == null) {
					preLazyInit();
					this.componentType = new SimpleType(this.ast);
					postLazyInit(this.componentType, COMPONENT_TYPE_PROPERTY);
				}
			}
		}
		return this.componentType;
	}

	/**
	 * Sets the component type of this array type. The component type
	 * may be another array type.
	 *
	 * @param componentType the component type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setComponentType(Type componentType) {
		if (componentType == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.componentType;
		preReplaceChild(oldChild, componentType, COMPONENT_TYPE_PROPERTY);
		this.componentType = componentType;
		postReplaceChild(oldChild, componentType, COMPONENT_TYPE_PROPERTY);
	}

	/**
	 * Returns the element type of this array type. The element type is
	 * never an array type.
	 * <p>
	 * This is a convenience method that descends a chain of nested array types
	 * until it reaches a non-array type.
	 * </p>
	 *
	 * @return the component type node
	 */
	public Type getElementType() {
		Type t = getComponentType();
		while (t.isArrayType()) {
			t = ((ArrayType) t).getComponentType();
		}
		return t;
	}

	/**
	 * Returns the number of dimensions in this array type.
	 * <p>
	 * This is a convenience method that descends a chain of nested array types
	 * until it reaches a non-array type.
	 * </p>
	 *
	 * @return the number of dimensions (always positive)
	 */
	public int getDimensions() {
		Type t = getComponentType();
		int dimensions = 1; // always include this array type
		while (t.isArrayType()) {
			dimensions++;
			t = ((ArrayType) t).getComponentType();
		}
		return dimensions;
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
			+ (this.componentType == null ? 0 : getComponentType().treeSize())
			+ (this.annotations == null ? 0 : this.annotations.listSize());
	}
}

