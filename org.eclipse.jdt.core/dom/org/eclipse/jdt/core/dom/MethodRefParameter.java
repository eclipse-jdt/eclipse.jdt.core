/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 * AST node for a parameter within a method reference ({@link MethodRef}).
 * These nodes only occur within doc comments ({@link Javadoc}).
 * <pre>
 * MethodRefParameter:
 * 		Type [ Identifier ]
 * </pre>
 * 
 * @see Javadoc
 * @since 3.0
 */
public class MethodRefParameter extends ASTNode {
	
	/**
	 * The "type" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY = 
		new ChildPropertyDescriptor(MethodRefParameter.class, "type", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(MethodRefParameter.class, "name", SimpleName.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(MethodRefParameter.class);
		addProperty(TYPE_PROPERTY);
		addProperty(NAME_PROPERTY);
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
	 * The type; lazily initialized; defaults to a unspecified,
	 * legal type.
	 */
	private Type type = null;

	/**
	 * The parameter name, or <code>null</code> if none; none by
	 * default.
	 */
	private SimpleName optionalParameterName = null;

	/**
	 * Creates a new AST node for a method referenece parameter owned by the given 
	 * AST. By default, the node has an unspecified (but legal) type, 
	 * and no parameter name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodRefParameter(AST ast) {
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
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((Type) child);
				return null;
			}
		}
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
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
		return METHOD_REF_PARAMETER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		MethodRefParameter result = new MethodRefParameter(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setType((Type) ASTNode.copySubtree(target, getType()));
		result.setName((SimpleName) ASTNode.copySubtree(target, getName()));
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
			acceptChild(visitor, getType());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the paramter type.
	 * 
	 * @return the parameter type
	 */ 
	public Type getType() {
		if (this.type == null) {
			preLazyInit();
			this.type = this.ast.newPrimitiveType(PrimitiveType.INT);
			postLazyInit(this.type, TYPE_PROPERTY);
		}
		return this.type;
	}

	/**
	 * Sets the paramter type to the given type.
	 * 
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the type is <code>null</code></li>
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
	 * Returns the parameter name, or <code>null</code> if there is none.
	 * 
	 * @return the parameter name node, or <code>null</code> if there is none
	 */ 
	public SimpleName getName() {
		return this.optionalParameterName;
	}
	
	/**
	 * Sets or clears the parameter name.
	 * 
	 * @param name the parameter name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		ASTNode oldChild = this.optionalParameterName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.optionalParameterName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
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
			+ (this.type == null ? 0 : getType().treeSize())
			+ (this.optionalParameterName == null ? 0 : getName().treeSize());
	}
}
