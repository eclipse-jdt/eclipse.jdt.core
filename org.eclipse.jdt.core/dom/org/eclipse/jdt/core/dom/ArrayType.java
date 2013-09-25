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
 * Array types are expressed in a recursive manner, one dimension at a time. From JLS8 onwards,
 * instead of this recursive manner, arrays are represented by a base element type (which cannot
 * be an Array type) and a list of dimensions each of which may have a list of annotations.
 * </p>
 * <pre>
 * ArrayType: 
 *    Type ExtraDimension { ExtraDimension }
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ArrayType extends Type {

	/**
	 * The "componentType" structural property of this node type (child type: {@link Type}).
	 * Not supported from JLS8 onwards.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor COMPONENT_TYPE_PROPERTY =
		new ChildPropertyDescriptor(ArrayType.class, "componentType", Type.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "elementType" structural property of this node type (child type: {@link Type}) (added in JLS8 API).
	 * @since 3.9 BETA_JAVA8
	 */
	public static final ChildPropertyDescriptor ELEMENT_TYPE_PROPERTY =
			new ChildPropertyDescriptor(ArrayType.class, "elementType", Type.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$	
	
	/**
	 * The "dimensions" structural property of this node type (element type: {@link ExtraDimension}) (added in JLS8 API).
	 * @since 3.9 BETA_JAVA8
	 */
	public static final ChildListPropertyDescriptor DIMENSIONS_PROPERTY =
			new ChildListPropertyDescriptor(ArrayType.class, "dimensions", ExtraDimension.class, CYCLE_RISK); //$NON-NLS-1$	
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
	 * @since 3.9 BETA_JAVA8
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(ArrayType.class, propertyList);
		addProperty(COMPONENT_TYPE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);

		propertyList = new ArrayList(3);
		createPropertyList(ArrayType.class, propertyList);
		addProperty(ELEMENT_TYPE_PROPERTY, propertyList);
		addProperty(DIMENSIONS_PROPERTY, propertyList);
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
	 * an unspecified, but legal, name. reused for element type from JLS8 onwards.
	 */
	private Type componentType = null;

	/**
	 * List of extra dimensions this node has with optional annotations
	 * (element type: {@link ExtraDimension}).
	 * Null before JLS8. Added in JLS8; defaults to a list with one element
	 * (see constructor).
	 * 
	 * @since 3.9 BETA_JAVA8
	 */
	private ASTNode.NodeList dimensions = null;

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
		if (ast.apiLevel >= AST.JLS8) {
			this.dimensions = new ASTNode.NodeList(DIMENSIONS_PROPERTY);
			// single dimension array is the default
			this.dimensions().add(this.ast.newExtraDimension());
		}
	}

	/**
	* Creates a new unparented node for an array type owned by the given AST.
	* <p>
	* N.B. This constructor is private.
	* </p>
	*
	* @param ast the AST that is to own this node
	* @param dimensions no of dimensions - can be zero
	*
	* @since 3.9 BETA_JAVA8
	*/
	private ArrayType(AST ast, int dimensions) {
		super(ast);
		unsupportedIn2_3_4();
		this.dimensions = new ASTNode.NodeList(DIMENSIONS_PROPERTY);
		for (int i = 0; i < dimensions; ++i) {
			this.dimensions().add(this.ast.newExtraDimension());
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
		if (property == DIMENSIONS_PROPERTY) {
			return dimensions();
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
		} else if (property == ELEMENT_TYPE_PROPERTY) {
			if (get) {
				return getElementType();
			} else {
				setElementType((Type) child);
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
		ArrayType result;
		if (this.ast.apiLevel < AST.JLS8) {
			result = new ArrayType(target);
			result.setComponentType((Type) getComponentType().clone(target));			
		} else {
			result = new ArrayType(target, 0);
			result.setElementType((Type) getElementType().clone(target));
			result.dimensions().addAll(
					ASTNode.copySubtrees(target, dimensions()));
		}
		result.setSourceRange(getStartPosition(), getLength());
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
			if (this.ast.apiLevel < AST.JLS8) {
				acceptChild(visitor, getComponentType());				
			} else {
				acceptChild(visitor, getElementType());
				acceptChildren(visitor, this.dimensions);
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the component type of this array type. The component type
	 * may be another array type.
	 *
	 * @return the component type node
	 * @deprecated from JLS8 and later, the recursive structure is not valid
	 */
	public Type getComponentType() {
		supportedOnlyIn2_3_4();
		return internalGetComponentType(COMPONENT_TYPE_PROPERTY);
	}

	private Type internalGetComponentType(ChildPropertyDescriptor property) {
		if (this.componentType == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.componentType == null) {
					preLazyInit();
					this.componentType = new SimpleType(this.ast);
					postLazyInit(this.componentType, property);
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
	 * @deprecated from JLS8 and later, the recursive structure is not valid
	 */
	public void setComponentType(Type componentType) {
		supportedOnlyIn2_3_4();
		if (componentType == null) {
			throw new IllegalArgumentException();
		}
		internalSetComponentType(componentType, COMPONENT_TYPE_PROPERTY);
	}

	private void internalSetComponentType(Type type, ChildPropertyDescriptor property) {
		ASTNode oldChild = this.componentType;
		preReplaceChild(oldChild, type, property);
		this.componentType = type;
		postReplaceChild(oldChild, type, property);
	}

	/**
	 * Returns the element type of this array type. The element type is
	 * never an array type.
	 * <p>
	 * This is a convenience method that descends a chain of nested array types
	 * until it reaches a non-array type until JLS4. From JLS8 and later, this returns the
	 * element type directly.
	 * </p>
	 *
	 * @return the element type node
	 */
	public Type getElementType() {
		if (this.ast.apiLevel() < AST.JLS8) {
			Type t = getComponentType();
			while (t.isArrayType()) {
				t = ((ArrayType) t).getComponentType();
			}
			return t;
		}
		return internalGetComponentType(ELEMENT_TYPE_PROPERTY);
	}

	/**
	 * Sets the element type of the array.
	 *
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>the node is an array type</li>
	 * </ul>
	 * @since 3.9 BETA_JAVA8
	 */
	public void setElementType(Type type) {
		if (type == null || type instanceof ArrayType) {
			throw new IllegalArgumentException();
		}
		internalSetComponentType(type, ELEMENT_TYPE_PROPERTY);
	}

	/**
	 * Returns the number of dimensions in this array type.
	 * <p>
	 * This is a convenience method that descends a chain of nested array types
	 * until it reaches a non-array type (until JLS4). From JLS8 onwards, 
	 * this returns the size of the dimensions list.
	 * </p>
	 *
	 * @return the number of dimensions (always positive)
	 */
	public int getDimensions() {
		if (this.ast.apiLevel() >= AST.JLS8) {
			return dimensions().size();
		}
		Type t = getComponentType();
		int dimension = 1; // always include this array type
		while (t.isArrayType()) {
			dimension++;
			t = ((ArrayType) t).getComponentType();
		}
		return dimension;			
	}

	/**
	 * Returns the dimension d in this array type.
	 * <p>
	 * This is a convenience method that returns the dimension at the given number d.
	 * </p>
	 * @param d dimension no
	 * @return Dimension at number d, null if d out of range
	 * 
	 * @since 3.9 BETA_JAVA8
	 */
	public ExtraDimension getDimensionAt(int d) {
		unsupportedIn2_3_4();
		ExtraDimension extraDimension = null;
		int n = getDimensions() - 1;
		if (d >= 0 && d <= n) {
			extraDimension = (ExtraDimension) dimensions().get(n - d);
		}
		return extraDimension;
	}

	/**
	 * Returns the live ordered list of dimensions with optional annotations (added in JLS8 API).
	 * 
	 * @return the live list of dimensions with optional annotations (element type: {@link ExtraDimension})
	 * @exception UnsupportedOperationException if this operation is used below JLS8
	 * @since 3.9 BETA_JAVA8
	 */
	public List dimensions() {
		// more efficient than just calling unsupportedIn2_3_4() to check
		if (this.dimensions == null) {
			unsupportedIn2_3_4();
		}
		return this.dimensions;
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
			+ (this.componentType == null ? 0 : (this.ast.apiLevel() < AST.JLS8 ? getComponentType().treeSize() : getElementType().treeSize())
			+ (this.dimensions == null ? 0 : this.dimensions.listSize()));
	}
}

