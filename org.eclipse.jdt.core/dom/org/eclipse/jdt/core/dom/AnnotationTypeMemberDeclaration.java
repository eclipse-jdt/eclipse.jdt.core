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
 * Annotation type member declaration AST node type (added in 3.0 API).
 * <pre>
 * AnnotationTypeMemberDeclaration:
 *   [ Javadoc ] { ExtendedModifier }
 *       Type Identifier <b>(</b> <b>)</b> [ <b>default</b> Expression ] <b>;</b>
 * </pre>
 * <p>
 * Note that annotation type member declarations are only meaningful as
 * elements of {@link AnnotationTypeDeclaration#members AnnotationTypeDeclaration.members}.
 * </p>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), 
 * or the first character of the member type (no modifiers). 
 * The source range extends through the last character of the
 * ";" token.
 * </p>
 * <p>
 * Note: Support for annotation metadata is an experimental language feature 
 * under discussion in JSR-175 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class AnnotationTypeMemberDeclaration extends BodyDeclaration {
	
	/**
	 * The "javadoc" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(AnnotationTypeMemberDeclaration.class);

	/**
	 * The "modifiers" structural property of this node type (added in 3.0 API).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
		internalModifiers2PropertyFactory(AnnotationTypeMemberDeclaration.class);
	
	/**
	 * The "name" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(AnnotationTypeMemberDeclaration.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "type" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY = 
		new ChildPropertyDescriptor(AnnotationTypeMemberDeclaration.class, "type", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "default" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor DEFAULT_PROPERTY = 
		new ChildPropertyDescriptor(AnnotationTypeMemberDeclaration.class, "default", Expression.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(AnnotationTypeMemberDeclaration.class);
		addProperty(JAVADOC_PROPERTY);
		addProperty(MODIFIERS2_PROPERTY);
		addProperty(NAME_PROPERTY);
		addProperty(TYPE_PROPERTY);
		addProperty(DEFAULT_PROPERTY);
		PROPERTY_DESCRIPTORS = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the AST.LEVEL_* constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
						
	/**
	 * The member name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName memberName = null;

	/**
	 * The member type; lazily initialized; defaults to int.
	 */
	private Type memberType = null;
	
	/**
	 * The optional default expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalDefaultValue = null;
	
	/**
	 * Creates a new AST node for an annotation type member declaration owned 
	 * by the given AST. By default, the declaration is for a member of an
	 * unspecified, but legal, name; no modifiers; no javadoc;
	 * an unspecified value type; and no default value.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AnnotationTypeMemberDeclaration(AST ast) {
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
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((Type) child);
				return null;
			}
		}
		if (property == DEFAULT_PROPERTY) {
			if (get) {
				return getDefault();
			} else {
				setDefault((Expression) child);
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
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ANNOTATION_TYPE_MEMBER_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		AnnotationTypeMemberDeclaration result = new AnnotationTypeMemberDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		result.setType((Type) ASTNode.copySubtree(target, getType()));
		result.setName((SimpleName) getName().clone(target));
		result.setDefault((Expression) ASTNode.copySubtree(target, getDefault()));
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
			acceptChild(visitor, getType());
			acceptChild(visitor, getName());
			acceptChild(visitor, getDefault());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of the annotation type member declared in this declaration.
	 * 
	 * @return the member name node
	 */ 
	public SimpleName getName() {
		if (this.memberName == null) {
			preLazyInit();
			this.memberName = new SimpleName(this.ast);
			postLazyInit(this.memberName, NAME_PROPERTY);
		}
		return this.memberName;
	}
	
	/**
	 * Sets the name of the annotation type member declared in this declaration to the
	 * given name.
	 * 
	 * @param memberName the new member name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName memberName) {
		if (memberName == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.memberName;
		preReplaceChild(oldChild, memberName, NAME_PROPERTY);
		this.memberName = memberName;
		postReplaceChild(oldChild, memberName, NAME_PROPERTY);
	}

	/**
	 * Returns the type of the annotation type member declared in this 
	 * declaration.
	 * 
	 * @return the type of the member
	 */ 
	public Type getType() {
		if (this.memberType == null) {
			preLazyInit();
			this.memberType = this.ast.newPrimitiveType(PrimitiveType.INT);
			postLazyInit(this.memberType, TYPE_PROPERTY);
		}
		return this.memberType;
	}

	/**
	 * Sets the type of the annotation type member declared in this declaration
	 * to the given type.
	 * 
	 * @param type the new member type
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
		ASTNode oldChild = this.memberType;
		preReplaceChild(oldChild, type, TYPE_PROPERTY);
		this.memberType = type;
		postReplaceChild(oldChild, type, TYPE_PROPERTY);
	}

	/**
	 * Returns the default value of this annotation type member, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getDefault() {
		return this.optionalDefaultValue;
	}
	
	/**
	 * Sets or clears the default value of this annotation type member.
	 * 
	 * @param defaultValue the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setDefault(Expression defaultValue) {
		// a AnnotationTypeMemberDeclaration may occur inside an Expression - must check cycles
		ASTNode oldChild = this.optionalDefaultValue;
		preReplaceChild(oldChild, defaultValue, DEFAULT_PROPERTY);
		this.optionalDefaultValue = defaultValue;
		postReplaceChild(oldChild, defaultValue, DEFAULT_PROPERTY);
	}
	
	/**
	 * Resolves and returns the binding for the annotation type member declared
	 * in this declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IVariableBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveMember(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ this.modifiers.listSize()
			+ (this.memberName == null ? 0 : getName().treeSize())
			+ (this.memberType == null ? 0 : getType().treeSize())
			+ (this.optionalDefaultValue == null ? 0 : getDefault().treeSize());
	}
}

