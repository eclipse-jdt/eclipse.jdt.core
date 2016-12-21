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
import java.util.Iterator;
import java.util.List;

/**
 * Module declaration AST node type representing the module descriptor file
 *
 * <pre>
 * ModuleDeclaration:
 *  [ Javadoc ] { ExtendedModifier } <b>module</b> Name <b>{</b>
 *        [ ExportsStatement | RequiresStatement | UsesStatement | ProvidesStatement ]
 *  <b>}</b>
 * </pre>
 * <p>
 * </p>
 *
 * @since 3.13 BETA_JAVA9
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class ModuleDeclaration extends ASTNode {

	/**
	 * The "javadoc" structural property of this node type (child type: {@link Javadoc}).
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY =
			new ChildPropertyDescriptor(ModuleDeclaration.class, "javadoc", Javadoc.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "modifiers" structural property of this node type (element type: {@link IExtendedModifier}).
	 */
	public static final ChildListPropertyDescriptor MODIFIERS_PROPERTY =
			new ChildListPropertyDescriptor(ModuleDeclaration.class, "modifiers", IExtendedModifier.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type (child type: {@link Name}).
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(ModuleDeclaration.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "moduleStatements" structural property of this node type (element type: {@link ModuleStatement}).
	 */
	public static final ChildListPropertyDescriptor MODULE_STATEMENTS_PROPERTY =
		new ChildListPropertyDescriptor(ModuleDeclaration.class, "moduleStatements", ModuleStatement.class, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_9_0;

	static {
		List properyList = new ArrayList(5);
		createPropertyList(ModuleDeclaration.class, properyList);
		addProperty(JAVADOC_PROPERTY, properyList);
		addProperty(MODIFIERS_PROPERTY, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(MODULE_STATEMENTS_PROPERTY, properyList);
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
	 * The doc comment, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Javadoc optionalDocComment = null;

	/**
	 * The extended modifiers (element type: {@link IExtendedModifier}).
	 * defaults to an empty list
	 * (see constructor).
	 *
	 */
	private ASTNode.NodeList modifiers = null;

	/**
	 * The referenced module name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private Name name = null;

	/**
	 * The list of statements (element type: {@link ModuleStatement}).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList moduleStatements = new ASTNode.NodeList(MODULE_STATEMENTS_PROPERTY);

	ModuleDeclaration(AST ast) {
		super(ast);
		unsupportedBelow9();
		this.modifiers = new ASTNode.NodeList(MODIFIERS_PROPERTY);
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Name) child);
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
		if (property == MODIFIERS_PROPERTY) {
			return modifiers();
		}
		if (property == MODULE_STATEMENTS_PROPERTY) {
			return moduleStatements();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	int getNodeType0() {
		return MODULE_DECLARATION;
	}

	@SuppressWarnings("unchecked")
	@Override
	ASTNode clone0(AST target) {
		ModuleDeclaration result = new ModuleDeclaration(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setJavadoc((Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		result.setName((SimpleName) getName().clone(target));
		result.moduleStatements().addAll(ASTNode.copySubtrees(target, moduleStatements()));
		return result;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getJavadoc());
			acceptChildren(visitor, this.modifiers);
			acceptChild(visitor, getName());
			acceptChildren(visitor, this.moduleStatements);
		}
		visitor.endVisit(this);

	}
	/**
	 * Returns the doc comment node.
	 *
	 * @return the doc comment node, or <code>null</code> if none
	 */
	public Javadoc getJavadoc() {
		return this.optionalDocComment;
	}

	/**
	 * Sets or clears the doc comment node.
	 *
	 * @param docComment the doc comment node, or <code>null</code> if none
	 * @exception IllegalArgumentException if the doc comment string is invalid
	 */
	public void setJavadoc(Javadoc docComment) {
		ChildPropertyDescriptor p = JAVADOC_PROPERTY;
		ASTNode oldChild = this.optionalDocComment;
		preReplaceChild(oldChild, docComment, p);
		this.optionalDocComment = docComment;
		postReplaceChild(oldChild, docComment, p);
	}

	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 *  this method is a convenience method that
	 * computes these flags from {@link #modifiers()}.
	 * </p>
	 *
	 * @return the bit-wise "or" of <code>Modifier</code> constants
	 * @see Modifier
	 */
	public int getModifiers() {
		// convenience method -
		// performance could be improved by caching computed flags
		// but this would require tracking changes to this.modifiers
		int computedmodifierFlags = Modifier.NONE;
		for (Iterator it = modifiers().iterator(); it.hasNext(); ) {
			Object x = it.next();
			if (x instanceof Modifier) {
				computedmodifierFlags |= ((Modifier) x).getKeyword().toFlagValue();
			}
		}
		return computedmodifierFlags;
	}

	/**
	 * Returns the name of this module declaration.
	 *
	 * @return the module name
	 */
	public Name getName()  {
		if (this.name == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.name == null) {
					preLazyInit();
					this.name =this.ast.newQualifiedName(
							new SimpleName(this.ast), new SimpleName(this.ast));
					postLazyInit(this.name, NAME_PROPERTY);
				}
			}
		}
		return this.name;
	}

	/**
	 * Sets the module name in to the given name.
	 *
	 * @param name the new module name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.name;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.name = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/**
	 * Returns the live ordered list of modifiers and annotations
	 * of this declaration.
	 *
	 * @return the live list of modifiers and annotations
	 *    (element type: {@link IExtendedModifier})
	 */
	public List modifiers() {
		return this.modifiers;
	}

	/**
	 * Returns the live list of statements in this module. Adding and
	 * removing nodes from this list affects this node dynamically.
	 * All nodes in this list must be <code>Statement</code>s;
	 * attempts to add any other type of node will trigger an
	 * exception.
	 *
	 * @return the live list of statements in this module declaration
	 *    (element type: {@link ModuleStatement})
	 */
	public List moduleStatements() {
		return this.moduleStatements;
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 4 * 4;
	}

	@Override
	int treeSize() {
		return	memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ this.modifiers.listSize()
			+ (this.name == null ? 0 : getName().treeSize())
			+ this.moduleStatements.listSize();
	}
}
