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
 * Method invocation expression AST node type.
 *
 * <pre>
 * MethodInvocation:
 *		[ Expression <b>.</b> ] Identifier 
 * 			<b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 * </pre>
 * 
 * @since 2.0
 */
public class MethodInvocation extends Expression {
	
	/**
	 * The "expression" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY = 
		new ChildPropertyDescriptor(MethodInvocation.class, "expression", Expression.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(MethodInvocation.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "arguments" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(MethodInvocation.class, "arguments", Expression.class, CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(MethodInvocation.class);
		addProperty(EXPRESSION_PROPERTY);
		addProperty(NAME_PROPERTY);
		addProperty(ARGUMENTS_PROPERTY);
		PROPERTY_DESCRIPTORS = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the AST.LEVEL_* constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
			
	/**
	 * The expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The method name; lazily initialized; defaults to a unspecified,
	 * legal Java method name.
	 */
	private SimpleName methodName = null;
	
	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(ARGUMENTS_PROPERTY);

	/**
	 * Creates a new AST node for a method invocation expression owned by the 
	 * given AST. By default, no expression, an unspecified, but legal, method
	 * name, and an empty list of arguments.
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodInvocation(AST ast) {
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
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
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
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return METHOD_INVOCATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		MethodInvocation result = new MethodInvocation(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setName((SimpleName) getName().clone(target));
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
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
			acceptChild(visitor, getExpression());
			acceptChild(visitor, getName());
			acceptChildren(visitor, this.arguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this method invocation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return this.optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this method invocation expression.
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
	 * Returns the name of the method invoked in this expression.
	 * 
	 * @return the method name node
	 */ 
	public SimpleName getName() {
		if (this.methodName == null) {
			preLazyInit();
			this.methodName = new SimpleName(this.ast);
			postLazyInit(this.methodName, NAME_PROPERTY);
		}
		return this.methodName;
	}
	
	/**
	 * Sets the name of the method invoked in this expression to the
	 * given name.
	 * 
	 * @param name the new method name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.methodName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.methodName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/**
	 * Returns the live ordered list of argument expressions in this method
	 * invocation expression.
	 * 
	 * @return the live list of argument expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return this.arguments;
	}

	/**
	 * Resolves and returns the binding for the method invoked by this
	 * expression.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the method binding, or <code>null</code> if the binding cannot
	 * be resolved
	 * @since 2.1
	 */
	public IMethodBinding resolveMethodBinding() {
		return this.ast.getBindingResolver().resolveMethod(this);
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
		return 
			memSize()
			+ (this.optionalExpression == null ? 0 : getExpression().treeSize())
			+ (this.methodName == null ? 0 : getName().treeSize())
			+ this.arguments.listSize();
	}
}

