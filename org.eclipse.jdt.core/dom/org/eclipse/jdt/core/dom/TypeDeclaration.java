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

import java.util.Iterator;
import java.util.List;

/**
 * Type declaration AST node type. A type declaration
 * is the union of a class declaration and an interface declaration.
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * TypeDeclaration:
 * 		ClassDeclaration
 * 		InterfaceDeclaration
 * ClassDeclaration:
 *      [ Javadoc ] { Modifier } <b>class</b> Identifier
 *			[ <b>extends</b> Type]
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b> { ClassBodyDeclaration | <b>;</b> } <b>}</b>
 * InterfaceDeclaration:
 *      [ Javadoc ] { Modifier } <b>interface</b> Identifier
 *			[ <b>extends</b> Type { <b>,</b> Type } ]
 * 			<b>{</b> { InterfaceBodyDeclaration | <b>;</b> } <b>}</b>
 * </pre>
 * For 3.0 (corresponding to JLS3), type parameters and reified modifiers
 * (and annotations) were added, and the superclass type name and superinterface 
 * types names are generalized to type so that parameterized types can be 
 * referenced:
 * <pre>
 * TypeDeclaration:
 * 		ClassDeclaration
 * 		InterfaceDeclaration
 * ClassDeclaration:
 *      [ Javadoc ] { ExtendedModifier } <b>class</b> Identifier
 *			[ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ]
 *			[ <b>extends</b> Type ]
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b> { ClassBodyDeclaration | <b>;</b> } <b>}</b>
 * InterfaceDeclaration:
 *      [ Javadoc ] { ExtendedModifier } <b>interface</b> Identifier
 *			[ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ]
 *			[ <b>extends</b> Type { <b>,</b> Type } ]
 * 			<b>{</b> { InterfaceBodyDeclaration | <b>;</b> } <b>}</b>
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier or annotation (if any), or the
 * first character of the "class" or "interface" keyword (if no
 * modifiers or annotations). The source range extends through the last character of the "}"
 * token following the body declarations.
 * </p>
 * <p>
 * Note: Support for generic types is an experimental language feature 
 * under discussion in JSR-014 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 2.0
 */
public class TypeDeclaration extends AbstractTypeDeclaration {
	
	/**
	 * <code>true</code> for an interface, <code>false</code> for a class.
	 * Defaults to class.
	 */
	private boolean isInterface = false;
	
	/**
	 * The type paramters (element type: <code>TypeParameter</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList typeParameters = null;

	/**
	 * The optional superclass name; <code>null</code> if none.
	 * Defaults to none. Note that this field is not used for
	 * interface declarations. Not used in 3.0.
	 */
	private Name optionalSuperclassName = null;

	/**
	 * The superinterface names (element type: <code>Name</code>). 
	 * 2.0 only; defaults to an empty list. Not used in 3.0.
	 * (see constructor).
	 * 
	 */
	private ASTNode.NodeList superInterfaceNames = null;

	/**
	 * The optional superclass type; <code>null</code> if none.
	 * Defaults to none. Note that this field is not used for
	 * interface declarations. Null in 2.0. Added in 3.0.
	 * @since 3.0
	 */
	private Type optionalSuperclassType = null;

	/**
	 * The superinterface types (element type: <code>Type</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList superInterfaceTypes = null;

	/**
	 * Creates a new AST node for a type declaration owned by the given 
	 * AST. By default, the type declaration is for a class of an
	 * unspecified, but legal, name; no modifiers; no javadoc; 
	 * no type parameters; no superclass or superinterfaces; and an empty list
	 * of body declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TypeDeclaration(AST ast) {
		super(ast);
		if (ast.API_LEVEL == AST.LEVEL_2_0) {
			this.superInterfaceNames = new ASTNode.NodeList(false, Name.class);
		}
		if (ast.API_LEVEL >= AST.LEVEL_3_0) {
			this.typeParameters = new ASTNode.NodeList(false, TypeParameter.class);
			this.superInterfaceTypes = new ASTNode.NodeList(false, Type.class);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TYPE_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TypeDeclaration result = new TypeDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (getAST().API_LEVEL == AST.LEVEL_2_0) {
			result.setModifiers(getModifiers());
			result.setSuperclass(
					(Name) ASTNode.copySubtree(target, getSuperclass()));
			result.superInterfaces().addAll(
					ASTNode.copySubtrees(target, superInterfaces()));
		}
		result.setInterface(isInterface());
		result.setName((SimpleName) getName().clone(target));
		if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(
					ASTNode.copySubtrees(target, typeParameters()));
			result.setSuperclassType(
					(Type) ASTNode.copySubtree(target, getSuperclassType()));
			result.superInterfaceTypes().addAll(
					ASTNode.copySubtrees(target, superInterfaceTypes()));
		}
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
			if (getAST().API_LEVEL == AST.LEVEL_2_0) {
				acceptChild(visitor, getJavadoc());
				acceptChild(visitor, getName());
				acceptChild(visitor, getSuperclass());
				acceptChildren(visitor, this.superInterfaceNames);
				acceptChildren(visitor, this.bodyDeclarations);
			}
			if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
				acceptChild(visitor, getJavadoc());
				acceptChildren(visitor, this.modifiers);
				acceptChild(visitor, getName());
				acceptChildren(visitor, this.typeParameters);
				acceptChild(visitor, getSuperclassType());
				acceptChildren(visitor, this.superInterfaceTypes);
				acceptChildren(visitor, this.bodyDeclarations);
			}
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns whether this type declaration declares a class or an 
	 * interface.
	 * 
	 * @return <code>true</code> if this is an interface declaration,
	 *    and <code>false</code> if this is a class declaration
	 */ 
	public boolean isInterface() {
		return this.isInterface;
	}
	
	/**
	 * Sets whether this type declaration declares a class or an 
	 * interface.
	 * 
	 * @param isInterface <code>true</code> if this is an interface
	 *    declaration, and <code>false</code> if this is a class
	 * 	  declaration
	 */ 
	public void setInterface(boolean isInterface) {
		modifying();
		this.isInterface = isInterface;
	}

	/**
	 * Returns the live ordered list of type parameters of this type 
	 * declaration (added in 3.0 API). This list is non-empty for parameterized types.
	 * <p>
	 * Note: Support for generic types is an experimental language feature 
	 * under discussion in JSR-014 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the live list of type parameters
	 *    (element type: <code>TypeParameter</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List typeParameters() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeParameters == null) {
			unsupportedIn2();
		}
		return this.typeParameters;
	}
	
	/**
	 * Returns the name of the superclass declared in this type
	 * declaration, or <code>null</code> if there is none (2.0 API only).
	 * <p>
	 * Note that this child is not relevant for interface 
	 * declarations (although it does still figure in subtree
	 * equality comparisons).
	 * </p>
	 * 
	 * @return the superclass name node, or <code>null</code> if 
	 *    there is none
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by
	 * <code>getSuperclassType</code>,
	 * which returns a <code>Type</code> instead of a <code>Name</code>.
	 */ 
	public Name getSuperclass() {
	    supportedOnlyIn2();
		return this.optionalSuperclassName;
	}

	/**
	* Returns the superclass declared in this type
	* declaration, or <code>null</code> if there is none (added in 3.0 API).
	* <p>
	* Note that this child is not relevant for interface 
	* declarations (although it does still figure in subtree
	* equality comparisons).
	* </p>
	* 
	* @return the superclass type node, or <code>null</code> if 
	*    there is none
	* @exception UnsupportedOperationException if this operation is used in
	* a 2.0 AST
	* @since 3.0
	*/ 
	public Type getSuperclassType() {
	    unsupportedIn2();
		return this.optionalSuperclassType;
	}

	/**
	 * Sets or clears the name of the superclass declared in this type
	 * declaration (2.0 API only).
	 * <p>
	 * Note that this child is not relevant for interface 
	 * declarations (although it does still figure in subtree
	 * equality comparisons).
	 * </p>
	 * 
	 * @param superclassName the superclass name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) deprecated In the 3.0 API, this method is replaced by <code>setType</code>,
	 * which expects a <code>Type</code> instead of a <code>Name</code>.
	 */ 
	public void setSuperclass(Name superclassName) {
	    supportedOnlyIn2();
		replaceChild(this.optionalSuperclassName, superclassName, false);
		this.optionalSuperclassName = superclassName;
	}

	/**
	 * Sets or clears the superclass declared in this type
	 * declaration (added in 3.0 API).
	 * <p>
	 * Note that this child is not relevant for interface declarations
	 * (although it does still figure in subtree equality comparisons).
	 * </p>
	 * 
	 * @param superclassType the superclass type node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public void setSuperclassType(Type superclassType) {
	    unsupportedIn2();
		replaceChild(this.optionalSuperclassType, superclassType, true);
		this.optionalSuperclassType = superclassType;
 	}

	/**
	 * Returns the live ordered list of names of superinterfaces of this type 
	 * declaration (2.0 API only). For a class declaration, these are the names
	 * of the interfaces that this class implements; for an interface
	 * declaration, these are the names of the interfaces that this interface
	 * extends.
	 * 
	 * @return the live list of interface names
	 *    (element type: <code>Name</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by 
	 * <code>superInterfaceTypes()</code>
	 */ 
	public List superInterfaces() {
		// more efficient than just calling supportedOnlyIn2() to check
		if (this.superInterfaceNames == null) {
			supportedOnlyIn2();
		}
		return this.superInterfaceNames;
	}
	
	/**
	 * Returns the live ordered list of superinterfaces of this type 
	 * declaration (added in 3.0 API). For a class declaration, these are the interfaces
	 * that this class implements; for an interface declaration,
	 * these are the interfaces that this interface extends.
	 * 
	 * @return the live list of interface types
	 *    (element type: <code>Type</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List superInterfaceTypes() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.superInterfaceTypes == null) {
			unsupportedIn2();
		}
		return this.superInterfaceTypes;
	}
	
	/**
	 * Returns the ordered list of field declarations of this type 
	 * declaration. For a class declaration, these are the
	 * field declarations; for an interface declaration, these are
	 * the constant declarations.
	 * <p>
	 * This convenience method returns this node's body declarations
	 * with non-fields filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of field declarations
	 */ 
	public FieldDeclaration[] getFields() {
		List bd = bodyDeclarations();
		int fieldCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof FieldDeclaration) {
				fieldCount++;
			}
		}
		FieldDeclaration[] fields = new FieldDeclaration[fieldCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof FieldDeclaration) {
				fields[next++] = (FieldDeclaration) decl;
			}
		}
		return fields;
	}

	/**
	 * Returns the ordered list of method declarations of this type 
	 * declaration.
	 * <p>
	 * This convenience method returns this node's body declarations
	 * with non-methods filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of method (and constructor) 
	 *    declarations
	 */ 
	public MethodDeclaration[] getMethods() {
		List bd = bodyDeclarations();
		int methodCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof MethodDeclaration) {
				methodCount++;
			}
		}
		MethodDeclaration[] methods = new MethodDeclaration[methodCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methods[next++] = (MethodDeclaration) decl;
			}
		}
		return methods;
	}

	/**
	 * Returns the ordered list of member type declarations of this type 
	 * declaration.
	 * <p>
	 * This convenience method returns this node's body declarations
	 * with non-types filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of member type declarations
	 */ 
	public TypeDeclaration[] getTypes() {
		List bd = bodyDeclarations();
		int typeCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof TypeDeclaration) {
				typeCount++;
			}
		}
		TypeDeclaration[] memberTypes = new TypeDeclaration[typeCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof TypeDeclaration) {
				memberTypes[next++] = (TypeDeclaration) decl;
			}
		}
		return memberTypes;
	}

	/**
	 * Resolves and returns the binding for the class or interface declared in
	 * this type declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		return getAST().getBindingResolver().resolveType(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("TypeDeclaration["); //$NON-NLS-1$
		buffer.append(isInterface()
		   ? "interface " //$NON-NLS-1$
		   : "class "); //$NON-NLS-2$//$NON-NLS-1$
		buffer.append(getName().getIdentifier());
		buffer.append(" "); //$NON-NLS-1$
		for (Iterator it = bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.appendDebugString(buffer);
			if (it.hasNext()) {
				buffer.append(";"); //$NON-NLS-1$
			}
		}
		buffer.append("]"); //$NON-NLS-1$
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 6 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ (this.typeName == null ? 0 : getName().treeSize())
			+ (this.typeParameters == null ? 0 : this.typeParameters.listSize())
			+ (this.optionalSuperclassName == null ? 0 : getSuperclass().treeSize())
			+ (this.optionalSuperclassType == null ? 0 : getSuperclassType().treeSize())
			+ (this.superInterfaceNames == null ? 0 : this.superInterfaceNames.listSize())
			+ (this.superInterfaceTypes == null ? 0 : this.superInterfaceTypes.listSize())
			+ this.bodyDeclarations.listSize();
	}
}

