/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * Type declaration AST node type. A type declaration
 * is the union of a class declaration and an interface declaration.
 *
 * <pre>
 * TypeDeclaration:
 * 		ClassDeclaration
 * 		InterfaceDeclaration
 * 		EnumDeclaration
 * ClassDeclaration:
 *      [ Javadoc ] { Modifier } <b>class</b> Identifier
 *			[ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ]
 *			[ <b>extends</b> Type ]
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b> { ClassBodyDeclaration | <b>;</b> } <b>}</b>
 * InterfaceDeclaration:
 *      [ Javadoc ] { Modifier } <b>interface</b> Identifier
 *			[ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ]
 *			[ <b>extends</b> Type { <b>,</b> Type } ]
 * 			<b>{</b> { InterfaceBodyDeclaration | <b>;</b> } <b>}</b>
 * EnumDeclaration:
 *      [ Javadoc ] { Modifier } <b>enum</b> Identifier
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b>
 *               [ EnumConstantDeclaration [ <b>,</b> EnumConstantDeclaration ] ]
 *               [ <b>;</b> { ClassBodyDeclaration | <b>;</b> } ]
 *          <b>}</b>
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), or the
 * first character of the "class", "interface", or "enum" keyword (if no
 * modifiers). The source range extends through the last character of the "}"
 * token following the body declarations.
 * </p>
 * <p>
 * Note: Enum declarations are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
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
public class TypeDeclaration extends BodyDeclaration {
	
	/**
	 * Mask containing all legal modifiers for this construct.
	 */
	private static final int LEGAL_MODIFIERS = 
		Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
		| Modifier.STATIC | Modifier.FINAL | Modifier.ABSTRACT
		| Modifier.STRICTFP;
		
	/**
	 * <code>true</code> for an interface, <code>false</code> for a class.
	 * Defaults to class.
	 */
	private boolean isInterface = false;
	
	/**
	 * <code>true</code> for an enumeration, <code>false</code> for a class.
	 * Defaults to class. This field is ignored for interfaces.
	 * <p>
	 * Note: Enum declarations are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @since 2.2
	 */
	private boolean isEnumeration = false;
	
	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 */
	private int modifiers = Modifier.NONE;
	
	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal Java class identifier.
	 */
	private SimpleName typeName = null;

	/**
	 * The type paramters (element type: <code>TypeParameter</code>). 
	 * Defaults to an empty list.
	 * @since 2.2
	 */
	private ASTNode.NodeList typeParameters =
		new ASTNode.NodeList(false, TypeParameter.class);

	/**
	 * The optional superclass type; <code>null</code> if none.
	 * Defaults to none. Note that this field is not used for
	 * interface declarations.
	 * @since 2.2
	 */
	private Type optionalSuperclassType = null;

	/**
	 * The superinterface types (element type: <code>Type</code>). 
	 * Defaults to an empty list.
	 * @since 2.2
	 */
	private ASTNode.NodeList superInterfaceTypes =
		new ASTNode.NodeList(false, Type.class);

	/**
	 * The body declarations (element type: <code>BodyDeclaration</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList bodyDeclarations = 
		new ASTNode.NodeList(true, BodyDeclaration.class);

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
		result.setModifiers(getModifiers());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setInterface(isInterface());
		result.setEnumeration(isEnumeration());
		result.setName((SimpleName) getName().clone(target));
		result.setSuperclass(
			(Name) ASTNode.copySubtree(target, getSuperclass()));
		result.superInterfaces().addAll(
			ASTNode.copySubtrees(target, superInterfaces()));
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
			acceptChild(visitor, getName());
			acceptChildren(visitor, typeParameters);
			acceptChild(visitor, getSuperclassType());
			acceptChildren(visitor, superInterfaceTypes);
			acceptChildren(visitor, bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns whether this type declaration declares a class or an 
	 * interface.
	 * 
	 * @return <code>true</code> if this is an interface declaration,
	 *    and <code>false</code> if this is a class or enumeration declaration
	 */ 
	public boolean isInterface() {
		return isInterface;
	}
	
	/**
	 * Sets whether this type declaration declares a class or an 
	 * interface.
	 * 
	 * @param isInterface <code>true</code> if this is an interface
	 *    declaration, and <code>false</code> if this is a class or enumeration
	 * 	  declaration
	 */ 
	public void setInterface(boolean isInterface) {
		modifying();
		this.isInterface = isInterface;
	}

	/**
	 * Returns whether this type declaration declares a class or an 
	 * enumeration. Note that this property is not relevant for interfaces.
	 * <p>
	 * Note: Enum declarations are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return <code>true</code> if this is an enumeration declaration,
	 *    and <code>false</code> if this is a class declaration
	 * @since 2.2
	 */ 
	public boolean isEnumeration() {
		return isEnumeration;
	}
	
	/**
	 * Sets whether this type declaration declares a class or an 
	 * enumeration. Note that this property is not relevant for interfaces.
	 * <p>
	 * Note: Enum declarations are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @param isEnumeration <code>true</code> if this is an enumeration
	 *    declaration, and <code>false</code> if this is a class
	 * 	  declaration
	 * @since 2.2
	 */ 
	public void setEnumeration(boolean isEnumeration) {
		modifying();
		this.isEnumeration = isEnumeration;
	}

	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * Note that deprecated is not included.
	 * </p>
	 * 
	 * @return the bit-wise or of Modifier constants
	 * @see Modifier
	 */ 
	public int getModifiers() {
		return modifiers;
	}
	
	/**
	 * Sets the modifiers explicitly specified on this declaration.
	 * <p>
	 * The following modifiers are valid for types: public, private, protected,
	 * static, final, abstract, and strictfp.
	 * </p>
	 * <p>
	 * Only a subset of modifiers are legal in any given situation.
	 * Note that deprecated is not included.
	 * </p>
	 * 
	 * @param modifiers the bit-wise or of Modifier constants
	 * @see Modifier
	 * @exception IllegalArgumentException if the modifiers are illegal
	 */ 
	public void setModifiers(int modifiers) {
		if ((modifiers & ~LEGAL_MODIFIERS) != 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.modifiers = modifiers;
	}

	/**
	 * Returns the name of the type declared in this type declaration.
	 * 
	 * @return the type name node
	 */ 
	public SimpleName getName() {
		if (typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return typeName;
	}
		
	/**
	 * Sets the name of the type declared in this type declaration to the
	 * given name.
	 * 
	 * @param typeName the new type name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeName, typeName, false);
		this.typeName = typeName;
	}

	/**
	 * Returns the live ordered list of type parameters of this type 
	 * declaration. This list is non-empty for parameterized types.
	 * <p>
	 * Note: Support for generic types is an experimental language feature 
	 * under discussion in JSR-014 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the live list of type parameters
	 *    (element type: <code>TypeParameter</code>)
	 * @since 2.2
	 */ 
	public List typeParameters() {
		return typeParameters;
	}
	
	/**
	 * Returns the name of the superclass declared in this type
	 * declaration, or <code>null</code> if there is none.
	 * <p>
	 * Note that this child is not relevant for interface and
	 * enumeration declarations (although it does still figure in subtree
	 * equality comparisons).
	 * </p>
	 * 
	 * @return the superclass name node, or <code>null</code> if 
	 *    there is none
	 * @deprecated Replaced by <code>getSuperclassType</code>, which returns
	 * a <code>Type</code> instead of a <code>Name</code>.
	 */ 
	public Name getSuperclass() {
		// implement deprecated method in terms of get/setSuperclassType
		Type superclassType = getSuperclassType();
		if (superclassType == null) {
			// return null if no superclass type
			return null;
		} else if (superclassType instanceof SimpleType) {
			// no problem - extract name from SimpleType
			SimpleType t = (SimpleType) superclassType;
			return t.getName();
		} else if ((superclassType instanceof ParameterizedType)
			     || (superclassType instanceof QualifiedType)) {
			// compatibility issue
			// back-level clients know nothing of new node types added in 2.1
			// take this opportunity to inform client of problem
			throw new RuntimeException("Deprecated AST API method cannot handle newer node types"); //$NON-NLS-1$
		} else {
			// compatibility issue
			// AST is bogus - illegal for type to be array or primitive type
			// take this opportunity to inform client of problem
			throw new RuntimeException("Deprecated AST API method cannot handle malformed AST"); //$NON-NLS-1$
		}
	}

	/**
	* Returns the superclass declared in this type
	* declaration, or <code>null</code> if there is none.
	* <p>
	* Note that this child is not relevant for interface and
	* enumeration declarations (although it does still figure in subtree
	* equality comparisons).
	* </p>
	* 
	* @return the superclass type node, or <code>null</code> if 
	*    there is none
	* @since 2.2
	*/ 
	public Type getSuperclassType() {
		return this.optionalSuperclassType;
	}

	/**
	 * Sets or clears the name of the superclass declared in this type
	 * declaration.
	 * <p>
	 * Note that this child is not relevant for interface and
	 * enumeration declarations (although it does still figure in subtree
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
	 */ 
	public void setSuperclass(Name superclassName) {
		// implement deprecated method in terms of get/setSuperclassType
		if (superclassName == null) {
			setSuperclassType(null);
		} else {
			Type superclassType = getSuperclassType();
			if (superclassType instanceof SimpleType) {
				// if possible edit name in SimpleType
				SimpleType s = (SimpleType) superclassType;
				s.setName(superclassName);
				// give type node same range as name node
				s.setSourceRange(
					superclassName.getStartPosition(),
					superclassName.getLength());
				// note that only s will be modified(), not the TypeDecl node
			} else {
				// all other cases - wrap name in a SimpleType and replace superclassType
				Type newT = getAST().newSimpleType(superclassName);
				// give new type node same range as name node
				newT.setSourceRange(
					superclassName.getStartPosition(),
					superclassName.getLength());
				setSuperclassType(newT);
			}
		}
	}

	/**
	 * Sets or clears the superclass declared in this type
	 * declaration.
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
	 * @since 2.2
	 */ 
	public void setSuperclassType(Type superclassType) {
		replaceChild(this.optionalSuperclassType, superclassType, true);
		this.optionalSuperclassType = superclassType;
 	}

	/**
	 * Returns the live ordered list of names of superinterfaces of this type 
	 * declaration. For a class or enumeration declaration, these are the names
	 * of the interfaces that this class implements; for an interface
	 * declaration, these are the names of the interfaces that this interface
	 * extends.
	 * 
	 * @return the live list of interface names
	 *    (element type: <code>Name</code>)
	 * @deprecated Replaced by <code>superInterfaceTypes</code>, which contains
	 * a list of <code>Type</code>s instead of <code>Name</code>s.
	 */ 
	public List superInterfaces() {
		// implement deprecated method in terms of superInterfaceTypes()
		// return special implementation of List<Name> in terms of List<Type>
		return new AbstractList() {
			/**
			 * @see java.util.AbstractCollection#size()
			 */
			public int size() {
				return superInterfaceTypes().size();
			}
		
			/**
			 * @see AbstractList#get(int)
			 */
			public Object get(int index) {
				Type t = (Type) superInterfaceTypes().get(index);
				if (t instanceof SimpleType) {
					// old client reading an old style element
					SimpleType s = (SimpleType) t;
					return s.getName();
				} else if ((t instanceof ParameterizedType)
					     || (t instanceof QualifiedType)) {
					// compatibility issue
					// back-level clients know nothing of new node types added in 2.1
					// take this opportunity to inform client of problem
					throw new RuntimeException("Deprecated AST API method (TypeDeclaration.superinterfaces()) cannot handle newer node types"); //$NON-NLS-1$
				} else {
					// compatibility issue
					// AST is bogus - illegal for type to be array or primitive type
					// take this opportunity to inform client of problem
					throw new RuntimeException("Deprecated AST API method (TypeDeclaration.superinterfaces()) cannot handle malformed AST"); //$NON-NLS-1$
				}
			}
		
			/**
			 * @see List#set(int, java.lang.Object)
			 */
			public Object set(int index, Object element) {
				if (!(element instanceof Name)) {
					throw new IllegalArgumentException();
				}
				Type oldType = (Type) superInterfaceTypes().get(index);
				Name newName = (Name) element;
				if (oldType instanceof SimpleType) {
					// old client operating on old style element
					SimpleType s = (SimpleType) oldType;
					Name oldName = s.getName();
					if (oldName != element) {
						s.setName(newName);
						// give type node same range as name node
						s.setSourceRange(
							newName.getStartPosition(),
							newName.getLength());
					}
					return oldName;
				} else {
					// old client replaced a new-fangled element
					Type newType = getAST().newSimpleType(newName);
					// give new type node same range as name node
					newType.setSourceRange(
						newName.getStartPosition(),
						newName.getLength());
					superInterfaceTypes().set(index, newType);
					// no choice but to return old new-fangled element
					return oldType;
				}
			}
			
			/**
			 * @see List#add(int, java.lang.Object)
			 */
			public void add(int index, Object element) {
				if (!(element instanceof Name)) {
					throw new IllegalArgumentException();
				}
				Name newName = (Name) element;
				Type newType = getAST().newSimpleType(newName);
				// give new type node same range as name node
				newType.setSourceRange(
					newName.getStartPosition(),
					newName.getLength());
				superInterfaceTypes().add(index, newType);
			}
			
			/**
			 * @see List#remove(int)
			 */
			public Object remove(int index) {
				Object result = superInterfaceTypes().remove(index);
				if (result instanceof SimpleType) {
					// old client operating on old style element
					SimpleType s = (SimpleType) result;
					Name oldName = s.getName();
					// make sure that oldName has no parent afterwards
					s.setName(getAST().newSimpleName("deleted")); //$NON-NLS-1$
					return oldName;
				} else {
					// old client removing a new-fangled element
					// take a chance that they ignore result
					return result;
				}
			}
		};
	}
	
	/**
	 * Returns the live ordered list of superinterfaces of this type 
	 * declaration. For a class declaration, these are the interfaces
	 * that this class implements; for an interface declaration,
	 * these are the interfaces that this interface extends.
	 * 
	 * @return the live list of interface types
	 *    (element type: <code>Type</code>)
	 * @since 2.2
	 */ 
	public List superInterfaceTypes() {
		return superInterfaceTypes;
	}
	
	/**
	 * Returns the live ordered list of body declarations of this type 
	 * declaration. For a class declaration, these are the
	 * initializer, field, method, constructor, and member type
	 * declarations; for an interface declaration, these are the constant,
	 * method, and member type declarations. For an enumeration declaration, 
	 * these are the enum constant declarations, which are always at the 
	 * front of the list, followed by any initializer, field, method,
	 * constructor, and member type declarations.
	 * 
	 * @return the live list of body declarations
	 *    (element type: <code>BodyDeclaration</code>)
	 */ 
	public List bodyDeclarations() {
		return bodyDeclarations;
	}
	
	/**
	 * Returns the ordered list of field declarations of this type 
	 * declaration. For a class declaration, these are the
	 * field declarations; for an interface declaration, these are
	 * the constant declarations; for an enum declaration, these are
	 * the explicitly declared field declarations (excludes enum
	 * constant declarations).
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
	 * Returns the ordered list of enum constant declarations of this enum
	 * declaration. This method is not relevant for class and interface 
	 * declarations, for which enum constant declarations are meaningless.
	 * <p>
	 * This convenience method returns this node's enum constant declarations
	 * with non-enum constants filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * <p>
	 * Note: Enum declarations are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the (possibly empty) list of enum constant declarations
	 * @since 2.2
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
	 * Returns whether this type declaration is a package member (that is,
	 * a top-level type).
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a compilation unit node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a compilation unit node, and <code>false</code> otherwise
	 */ 
	public boolean isPackageMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof CompilationUnit);
	}

	/**
	 * Returns whether this type declaration is a type member.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration node, an anonymous 
	 * class declaration, or an enumeration constant declaration.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration node, a class instance creation node, or an
	 *   enum constant declaration, and <code>false</code> otherwise
	 */ 
	public boolean isMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof TypeDeclaration)
			|| (parent instanceof AnonymousClassDeclaration)
			|| (parent instanceof EnumConstantDeclaration);
	}

	/**
	 * Returns whether this type declaration is a local type.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration statement node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration statement node, and <code>false</code> otherwise
	 */ 
	public boolean isLocalTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof TypeDeclarationStatement);
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
		   : (isEnumeration()
		        ? "enum " : "class ")); //$NON-NLS-2$//$NON-NLS-1$
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
		return super.memSize() + 7 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize() + 
			 (getJavadoc() == null ? 0 : getJavadoc().treeSize()) +
			 (typeName == null ? 0 : getName().treeSize()) +
			 typeParameters.listSize() +
			 (optionalSuperclassType == null ? 0 : getSuperclassType().treeSize())+ 
			 superInterfaceTypes.listSize() +
			 bodyDeclarations.listSize();
	}
}

