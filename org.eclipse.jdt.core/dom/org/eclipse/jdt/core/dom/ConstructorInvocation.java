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
 * Alternate constructor invocation statement AST node type.
 *
 * <pre>
 * ConstructorInvocation:
 *		<b>this</b> <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b> <b>;</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ConstructorInvocation extends Statement {
	
	/**
	 * The "arguments" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor ARGUMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(ConstructorInvocation.class, "arguments", Expression.class, CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(ConstructorInvocation.class);
		addProperty(ARGUMENTS_PROPERTY);
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
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(ARGUMENTS_PROPERTY);

	/**
	 * Creates a new AST node for an alternate constructor invocation statement
	 * owned by the given AST. By default, an empty list of arguments.
	 * 
	 * @param ast the AST that is to own this node
	 */
	ConstructorInvocation(AST ast) {
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
		return CONSTRUCTOR_INVOCATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		ConstructorInvocation result = new ConstructorInvocation(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.copyLeadingComment(this);
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
			acceptChildren(visitor, this.arguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of argument expressions in this alternate
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
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ this.arguments.listSize();
	}
}

