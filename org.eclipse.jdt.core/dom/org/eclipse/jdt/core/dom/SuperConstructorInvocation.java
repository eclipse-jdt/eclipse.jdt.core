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
 * Super constructor invocation statement AST node type.
 * For 2.0 (corresponding to JLS2): * <pre>
 * SuperConstructorInvocation:
 *     [ Expression <b>.</b> ] <b>super</b>
 *         <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b> <b>;</b>
 * </pre>
 * For 3.0 (corresponding to JLS3), type arguments are added:
 * <pre>
 * SuperConstructorInvocation:
 *     [ Expression <b>.</b> ]
 *         [ <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b> ]
 *         <b>super</b> <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b> <b>;</b>
 * </pre>
 * 
 * @since 2.0
 */
public class SuperConstructorInvocation extends Statement {
	
	/**
	 * The "expression" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY = 
		new ChildPropertyDescriptor(SuperConstructorInvocation.class, "expression", Expression.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "typeArguments" structural property of this node type (added in 3.0 API).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor TYPE_ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(SuperConstructorInvocation.class, "typeArguments", Type.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "arguments" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(SuperConstructorInvocation.class, "arguments", Expression.class, CYCLE_RISK); //$NON-NLS-1$
	
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
		createPropertyList(SuperConstructorInvocation.class);
		addProperty(EXPRESSION_PROPERTY);
		addProperty(ARGUMENTS_PROPERTY);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList();
		
		createPropertyList(SuperConstructorInvocation.class);
		addProperty(EXPRESSION_PROPERTY);
		addProperty(TYPE_ARGUMENTS_PROPERTY);
		addProperty(ARGUMENTS_PROPERTY);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants

	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
			
	/**
	 * The expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The type arguments (element type: <code>Type</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList typeArguments = null;

	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(ARGUMENTS_PROPERTY);

	/**
	 * Creates a new AST node for an super constructor invocation statement
	 * owned by the given AST. By default, no type arguments, and an empty list
	 * of arguments.
	 * 
	 * @param ast the AST that is to own this node
	 */
	SuperConstructorInvocation(AST ast) {
		super(ast);	
		if (ast.apiLevel >= AST.JLS3) {
			this.typeArguments = new ASTNode.NodeList(TYPE_ARGUMENTS_PROPERTY);
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
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
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
		if (property == ARGUMENTS_PROPERTY) {
			return arguments();
		}
		if (property == TYPE_ARGUMENTS_PROPERTY) {
			return typeArguments();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return SUPER_CONSTRUCTOR_INVOCATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		SuperConstructorInvocation result = new SuperConstructorInvocation(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.copyLeadingComment(this);
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		if (this.ast.apiLevel >= AST.JLS3) {
			result.typeArguments().addAll(ASTNode.copySubtrees(target, typeArguments()));
		}
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
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
			acceptChild(visitor, getExpression());
			if (this.ast.apiLevel >= AST.JLS3) {
				acceptChildren(visitor, this.typeArguments);
			}
			acceptChildren(visitor, this.arguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this super constructor invocation statement,
	 * or <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return this.optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this super constructor invocation
	 * statement.
	 * 
	 * @param expression the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		ASTNode oldChild = this.optionalExpression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.optionalExpression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	/**
	 * Returns the live ordered list of type arguments of this constructor
	 * invocation (added in 3.0 API).
	 * <p>
	 * Note: This API element is only needed for dealing with Java code that uses
	 * new language features of J2SE 1.5. It is included in anticipation of J2SE
	 * 1.5 support, which is planned for the next release of Eclipse after 3.0, and
	 * may change slightly before reaching its final form.
	 * </p>
	 * 
	 * @return the live list of type arguments
	 *    (element type: <code>Type</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List typeArguments() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeArguments == null) {
			unsupportedIn2();
		}
		return this.typeArguments;
	}
	
	/**
	 * Returns the live ordered list of argument expressions in this super
	 * constructor invocation statement.
	 * 
	 * @return the live list of argument expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return this.arguments;
	}

	/**
	 * Resolves and returns the binding for the constructor invoked by this
	 * expression.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the constructor binding, or <code>null</code> if the binding
	 *    cannot be resolved
	 */	
	public IMethodBinding resolveConstructorBinding() {
		return this.ast.getBindingResolver().resolveConstructor(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize()
		+ (this.optionalExpression == null ? 0 : getExpression().treeSize())
		+ (this.typeArguments == null ? 0 : this.typeArguments.listSize())
		+ (this.arguments == null ? 0 : this.arguments.listSize());
	}
}
