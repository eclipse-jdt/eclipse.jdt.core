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

import java.util.Iterator;
import java.util.List;

/**
 * Enum declaration AST node type (added in 3.0 API).
 *
 * <pre>
 * EnumDeclaration:
 *      [ Javadoc ] { ExtendedModifier } <b>enum</b> Identifier
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b>
 *               [ EnumConstantDeclaration { <b>,</b> EnumConstantDeclaration } ] [ <b>,</b> ]
 *               [ <b>;</b> { ClassBodyDeclaration | <b>;</b> } ]
 *          <b>}</b>
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier or annotation (if present), or the
 * first character of the "enum" keyword (if no
 * modifiers or annotations). The source range extends through the last
 * character of the "}" token following the body declarations.
 * </p>
 * <p>
 * Note: Enum declarations are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class EnumDeclaration extends AbstractTypeDeclaration {
	
	/**
	 * The "javadoc" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(EnumDeclaration.class);

	/**
	 * The "modifiers" structural property of this node type (added in 3.0 API).
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
		internalModifiers2PropertyFactory(EnumDeclaration.class);
	
	/**
	 * The "name" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		internalNamePropertyFactory(EnumDeclaration.class);

	/**
	 * The "superInterfaceTypes" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor SUPER_INTERFACE_TYPES_PROPERTY = 
		new ChildListPropertyDescriptor(EnumDeclaration.class, "superInterfaceTypes", Type.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "bodyDeclarations" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor BODY_DECLARATIONS_PROPERTY = 
		internalBodyDeclarationPropertyFactory(EnumDeclaration.class);
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(EnumDeclaration.class);
		addProperty(JAVADOC_PROPERTY);
		addProperty(MODIFIERS2_PROPERTY);
		addProperty(NAME_PROPERTY);
		addProperty(BODY_DECLARATIONS_PROPERTY);
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
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
			
	/**
	 * The superinterface types (element type: <code>Type</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList superInterfaceTypes =
		new ASTNode.NodeList(SUPER_INTERFACE_TYPES_PROPERTY);

	/**
	 * Creates a new AST node for an enum declaration owned by the given 
	 * AST. By default, the enum declaration has an unspecified, but legal,
	 * name; no modifiers; no javadoc; no superinterfaces; 
	 * and an empty list of body declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	EnumDeclaration(AST ast) {
		super(ast);
	    unsupportedIn2();
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
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == MODIFIERS2_PROPERTY) {
			return modifiers();
		}
		if (property == SUPER_INTERFACE_TYPES_PROPERTY) {
			return superInterfaceTypes();
		}
		if (property == BODY_DECLARATIONS_PROPERTY) {
			return bodyDeclarations();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	final ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	final ChildListPropertyDescriptor internalModifiers2Property() {
		return MODIFIERS2_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	final SimplePropertyDescriptor internalModifiersProperty() {
		// this property will not be asked for (node type did not exist in 2.0)
		return null;
	}

	/* (omit javadoc for this method)
	 * Method declared on AbstractTypeDeclaration.
	 */
	final ChildPropertyDescriptor internalNameProperty() {
		return NAME_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on AbstractTypeDeclaration.
	 */
	final ChildListPropertyDescriptor internalBodyDeclarationsProperty() {
		return BODY_DECLARATIONS_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ENUM_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		EnumDeclaration result = new EnumDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		result.setName((SimpleName) getName().clone(target));
		result.superInterfaceTypes().addAll(
			ASTNode.copySubtrees(target, superInterfaceTypes()));
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
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
			acceptChild(visitor, getJavadoc());
			acceptChildren(visitor, this.modifiers);
			acceptChild(visitor, getName());
			acceptChildren(visitor, this.superInterfaceTypes);
			acceptChildren(visitor, this.bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of superinterfaces of this enum
	 * declaration.
	 * 
	 * @return the live list of super interface types
	 *    (element type: <code>Type</code>)
	 */ 
	public List superInterfaceTypes() {
		return this.superInterfaceTypes;
	}
	
	/**
	 * Returns the ordered list of enum constant declarations of this enum
	 * declaration.
	 * <p>
	 * This convenience method returns this node's enum constant declarations
	 * with non-enum constants filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of enum constant declarations
	 */ 
	public EnumConstantDeclaration[] getEnumConstants() {
		List bd = bodyDeclarations();
		int enumCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof EnumConstantDeclaration) {
				enumCount++;
			}
		}
		EnumConstantDeclaration[] enumConstants = new EnumConstantDeclaration[enumCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof EnumConstantDeclaration) {
				enumConstants[next++] = (EnumConstantDeclaration) decl;
			}
		}
		return enumConstants;
	}

	/**
	 * Resolves and returns the binding for the enum declared in
	 * this enum declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveType(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ this.modifiers.listSize()
			+ (this.typeName == null ? 0 : getName().treeSize())
			+ this.superInterfaceTypes.listSize()
			+ this.bodyDeclarations.listSize();
	}
}

