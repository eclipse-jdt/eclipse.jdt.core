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
 * The array dimension info node. The array dimension, represented as <b>[]</b>, can have
 * type annotations. It may also include an <code>Expression</code> depending on where it appears.
 * The only node that supports a dimension info with an expression is <code>ArrayCreation</code>.
 * This node type is supported only in JLS8 or later.
 * <p>
 * The dimension info node is used to represent extra dimensions in the following nodes:
 * <pre>
 * 	SingleVariableDeclaration
 * 	VariableDeclarationFragment
 * 	MethodDeclaration
 * </pre>
 * For JLS8:
 * <pre>
 * DimensionInfo:
 * 	{ Annotations } <b>[</b> [ Expression ] <b>]</b>
 * </pre>
 *</p>
 * @see AST#newDimensionInfo()
 * @since 3.9
 */
public class DimensionInfo extends ASTNode {


	/**
	 * The "annotations" structural property of this node type (child type: {@link Annotation}).
	 * @since 3.9
	 */
	public static final ChildListPropertyDescriptor ANNOTATIONS_PROPERTY =
		new ChildListPropertyDescriptor(DimensionInfo.class, "annotations", Annotation.class, NO_CYCLE_RISK); //$NON-NLS-1$


	/**
	 * The "expression" structural property of this node type (element type: {@link Expression}).
	 * @since 3.9
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY =
			new ChildPropertyDescriptor(DimensionInfo.class, "expression", Expression.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$


	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.9
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(DimensionInfo.class, propertyList);
		addProperty(ANNOTATIONS_PROPERTY, propertyList);
		addProperty(EXPRESSION_PROPERTY, propertyList);
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
	 * Create a new instance of DimensionInfo node (Supported only in level
	 * JLS8 or above).  
	 *
	 * @param ast
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2, JLS3 or JLS4 AST
	 * @since 3.9
	 */
	DimensionInfo(AST ast) {
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

	private Expression expression = null;

	/**
	 * Sets the expression to this dimension. This operation will have
	 * effect only if this dimension info node is part of an array creation
	 * node.
	 *
	 * @param expression the expression for this dimension
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 *
	 * @since 3.9
	 */
	public void setExpression(Expression expression) {
		// an ArrayCreation cannot occur inside a ArrayType - cycles not possible
		ASTNode oldChild = this.expression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.expression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	/**
	 * Returns the expression in the dimension info or null if not applicable.
	 *
	 * @return the expression
	 * @since 3.9
	 */
	public Expression expression() {
		return this.expression;
	}

	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return expression();
			} else {
				setExpression((Expression) child);
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
		if (property == ANNOTATIONS_PROPERTY) {
			return annotations();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	int getNodeType0() {
		return DIMENSION_INFO;
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	ASTNode clone0(AST target) {
		DimensionInfo result = new DimensionInfo(target);
		result.annotations.addAll(
				ASTNode.copySubtrees(target, annotations()));
		if (this.expression != null) {
			result.setExpression((Expression)expression().clone(target));
		}
		return result;
	}

	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, this.annotations);
			if (this.expression != null) {
				acceptChild(visitor, this.expression);
			}
		}
		visitor.endVisit(this);
	}

	int treeSize() {
		int size = memSize()
				+ this.annotations.listSize()
				+ (this.expression == null ? 0 : this.expression.treeSize());
			return size;
	}

	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}
}
