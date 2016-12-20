/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses statement AST node type.
 * <pre>
 * UsesStatement:
 *     <b>uses</b> Name <b>;</b>
 * </pre>
 *
 * @since 3.13 BETA_JAVA9
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class UsesStatement extends ModuleStatement {

	/**
	 * The "type" structural property of this node type (child type: {@link Name}).
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY =
		new ChildPropertyDescriptor(UsesStatement.class, "type", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_9_0;

	static {
		List properyList = new ArrayList(2);
		createPropertyList(UsesStatement.class, properyList);
		addProperty(TYPE_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_9_0 = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS_9_0;
	}

	/**
	 * The module name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private Type type = null;

	/**
	 * Creates a new AST node for an uses statement owned by the
	 * given AST. The uses statement initially is
	 * for an unspecified, but legal, Java type name.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	UsesStatement(AST ast) {
		super(ast);
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
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

	@Override
	final int getNodeType0() {
		return USES_STATEMENT;
	}

	@Override
	ASTNode clone0(AST target) {
		UsesStatement result = new UsesStatement(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setType((Type) getType().clone(target));
		return result;
	}

	@Override
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getType());
		}
		visitor.endVisit(this);
	}


	/**
	 * Returns the type in this statement
	 *
	 * @return the type
	 */
	public Type getType()  {
		if (this.type == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.type == null) {
					preLazyInit();
					this.type = this.ast.newPrimitiveType(PrimitiveType.INT);
					postLazyInit(this.type, TYPE_PROPERTY);
				}
			}
		}
		return this.type;
	}

	/**
	 * Sets the type in uses statement
	 *
	 * @param type the new type in uses
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

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.type == null ? 0 : getType().treeSize());
	}

}
