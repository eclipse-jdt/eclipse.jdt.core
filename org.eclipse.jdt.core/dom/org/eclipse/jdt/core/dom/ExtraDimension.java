/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
 * The extra dimension node. The extra dimensions, represented as <b>[]</b>, are allowed to have
 * type annotations. This node type is supported only in JLS8 or later.
 * <p>
 * The extra dimension node is used to represent extra dimensions in the following nodes:
 * <pre>
 * 	SingleVariableDeclaration
 * 	VariableDeclarationFragment
 * 	MethodDeclaration
 * </pre>
 * For JLS8:
 * <pre>
 * ExtraDimension:
 * 	{ Annotations } <b>[]</b>
 * </pre>
 *</p>
 * @see AST#newExtraDimension()
 * @since 3.9
 */
public class ExtraDimension extends ASTNode {


	/**
	 * The "annotations" structural property of this node type (child type: {@link Annotation}).
	 * @since 3.9
	 */
	public static final ChildListPropertyDescriptor ANNOTATIONS_PROPERTY =
		new ChildListPropertyDescriptor(ExtraDimension.class, "annotations", Annotation.class, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.9
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(ExtraDimension.class, propertyList);
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
	 * @since 3.9
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS_8_0;
	}

	/**
	 * Create a new instance of ExtraDimension node (Supported only in level
	 * JLS8 or above).  
	 *
	 * @param ast
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2, JLS3 or JLS4 AST
	 * @since 3.9
	 */
	ExtraDimension(AST ast) {
		super(ast);
		unsupportedIn2_3_4();
		this.annotations = new ASTNode.NodeList(ANNOTATIONS_PROPERTY);
	}

	/**
	 * The list of annotations for this dimension (element type:
	 * {@link Annotation}).
	 */
	ASTNode.NodeList annotations = null;
	
	/**
	 * Returns the live ordered list of annotations for this dimension.
	 *
	 * @return the live list of annotations (element type: {@link Annotation})
	 * @since 3.9
	 */
	public List annotations() {
		return this.annotations;
	}

	List internalStructuralPropertiesForType(int apiLevel) {
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

	int getNodeType0() {
		return EXTRA_DIMENSION;
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	ASTNode clone0(AST target) {
		ExtraDimension result = new ExtraDimension(target);
		result.annotations.addAll(
				ASTNode.copySubtrees(target, annotations()));
		return result;
	}

	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, this.annotations);
		}
		visitor.endVisit(this);
	}

	int treeSize() {
		int size = memSize()
				+ this.annotations.listSize();
			return size;
	}

	int memSize() {
		return BASE_NODE_SIZE + 4;
	}
}
