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
 * AST node for a qualified name. A qualified name is defined recursively
 * as a simple name preceded by a name, which qualifies it. Expressing it this
 * way means that the qualifier and the simple name get their own AST nodes.
 * For JLS2, JLS3 or JLS4:
 * <pre>
 * QualifiedName:
 *    Name <b>.</b> SimpleName
 * </pre>
 * <p>
 * For JLS8:
 * <pre>
 * QualifiedName:
 *    Name <b>.</b> {Annotation} SimpleName
 * </pre>
 * </p>
 * <p>
 * See <code>FieldAccess</code> for guidelines on handling other expressions
 * that resemble qualified names.
 * </p>
 *
 * @see FieldAccess
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class QualifiedName extends Name {

	/**
	 * The "qualifier" structural property of this node type (child type: {@link Name}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor QUALIFIER_PROPERTY =
		new ChildPropertyDescriptor(QualifiedName.class, "qualifier", Name.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type (child type: {@link SimpleName}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(QualifiedName.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "annotations" structural property of this node type (child type: {@link Annotation}).
	 * @since 3.9
	 */
	public static final ChildListPropertyDescriptor ANNOTATIONS_PROPERTY =
		new ChildListPropertyDescriptor(QualifiedName.class, "annotations", Annotation.class, CYCLE_RISK); //$NON-NLS-1$
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
		List propertyList = new ArrayList(3);
		createPropertyList(QualifiedName.class, propertyList);
		addProperty(QUALIFIER_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(4);
		createPropertyList(QualifiedName.class, propertyList);
		addProperty(QUALIFIER_PROPERTY, propertyList);
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
	 * The identifier; lazily initialized; defaults to a unspecified, legal
	 * Java identifier.
	 */
	private Name qualifier = null;

	/**
	 * The name being qualified; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName name = null;

	/**
	 * Creates a new AST node for a qualified name owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	QualifiedName(AST ast) {
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
		if (property == QUALIFIER_PROPERTY) {
			if (get) {
				return getQualifier();
			} else {
				setQualifier((Name) child);
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
	final int getNodeType0() {
		return QUALIFIED_NAME;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		QualifiedName result = new QualifiedName(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setQualifier((Name) getQualifier().clone(target));
		result.setName((SimpleName) getName().clone(target));
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getQualifier());
			if (this.ast.apiLevel >= AST.JLS8) {
				acceptChildren(visitor, this.annotations);
			}
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the qualifier part of this qualified name.
	 *
	 * @return the qualifier part of this qualified name
	 */
	public Name getQualifier() {
		if (this.qualifier == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.qualifier == null) {
					preLazyInit();
					this.qualifier = new SimpleName(this.ast);
					postLazyInit(this.qualifier, QUALIFIER_PROPERTY);
				}
			}
		}
		return this.qualifier;
	}

	/**
	 * Sets the qualifier of this qualified name to the given name.
	 *
	 * @param qualifier the qualifier of this qualified name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setQualifier(Name qualifier) {
		if (qualifier == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.qualifier;
		preReplaceChild(oldChild, qualifier, QUALIFIER_PROPERTY);
		this.qualifier = qualifier;
		postReplaceChild(oldChild, qualifier, QUALIFIER_PROPERTY);
	}

	/**
	 * Returns the name part of this qualified name.
	 *
	 * @return the name being qualified
	 */
	public SimpleName getName() {
		if (this.name == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.name == null) {
					preLazyInit();
					this.name = new SimpleName(this.ast);
					postLazyInit(this.name, NAME_PROPERTY);
				}
			}
		}
		return this.name;
	}

	/**
	 * Sets the name part of this qualified name to the given simple name.
	 *
	 * @param name the identifier of this qualified name
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
		ASTNode oldChild = this.name;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.name = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on Name.
	 */
	void appendName(StringBuffer buffer) {
		getQualifier().appendName(buffer);
		buffer.append('.');
		getName().appendName(buffer);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NAME_NODE_SIZE + 2 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.name == null ? 0 : getName().treeSize())
			+ (this.qualifier == null ? 0 : getQualifier().treeSize())
			+ (this.annotations == null ? 0 : this.annotations.listSize());
	}
}

