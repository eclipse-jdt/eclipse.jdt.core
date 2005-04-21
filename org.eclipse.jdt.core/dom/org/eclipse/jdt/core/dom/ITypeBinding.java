/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * A type binding represents fully-resolved type. There are a number of
 * different kinds of type bindings:
 * <ul>
 * <li>a class - represents the class declaration;
 * possibly with type parameters</li>
 * <li>an interface - represents the class declaration;
 * possibly with type parameters</li>
 * <li>an enum - represents the enum declaration (enum types do not have
 * have type parameters)</li>
 * <li>an annotation - represents the annotation type declaration 
 * (annotation types do not have have type parameters)</li>
 * <li>an array type - array types are referenced but not explicitly 
 * declared</li>
 * <li>a primitive type (including the special return type <code>void</code>)
 * - primitive types are referenced but not explicitly declared</li>
 * <li>the null type - this is the special type of <code>null</code></li>
 * <li>a type variable - represents the declaration of a type variable;
 * possibly with type bounds</li>
 * <li>a wildcard type - represents a wild card used as a type argument in
 * a parameterized type reference</li>
 * <li>a raw type - represents a legacy non-parameterized reference to
 * a generic type</li>
 * <li>a parameterized type - represents an copy of a type declaration
 * with substitutions for its type parameters</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see ITypeBinding#getDeclaredTypes()
 * @since 2.0
 */
public interface ITypeBinding extends IBinding {
	
	/**
	 * Returns the binary name of this type binding.
	 * The binary name of a class is defined in the Java Language 
	 * Specification 2nd edition, section 13.1.
	 * <p>
	 * Note that in some cases, the binary name may be unavailable.
	 * This may happen, for example, for a local type declared in 
	 * unreachable code.
	 * </p>
	 *
	 * @return the binary name of this type, or <code>null</code> 
	 * if the binary name is unknown
	 * @since 3.0
	 */
	public String getBinaryName();
	
	/**
	 * Returns whether this type binding represents a primitive type.
	 * <p>
	 * There are nine predefined type bindings to represent the eight primitive
	 * types and <code>void</code>. These have the same names as the primitive
	 * types that they represent, namely boolean, byte, char, short, int,
	 * long, float, and double, and void.
	 * </p>
	 * 
	 * @return <code>true</code> if this type binding is for a primitive type,
	 *   and <code>false</code> otherwise
	 */
	public boolean isPrimitive();

	/**
	 * Returns whether this type binding represents the null type.
	 * <p>
	 * The null type is the type of a <code>NullLiteral</code> node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type binding is for the null type,
	 *   and <code>false</code> otherwise
	 */
	public boolean isNullType();
	
	/**
	 * Returns whether this type binding represents an array type.
	 *
	 * @return <code>true</code> if this type binding is for an array type,
	 *   and <code>false</code> otherwise
	 * @see #getElementType()
	 * @see #getDimensions()
	 */
	public boolean isArray();
	
	/**
	 * Returns the binding representing the element type of this array type,
	 * or <code>null</code> if this is not an array type binding. The element
	 * type of an array is never itself an array type.
	 *
	 * @return the element type binding, or <code>null</code> if this is
	 *   not an array type
	 */
	public ITypeBinding getElementType();
	
	/**
	 * Returns the dimensionality of this array type, or <code>0</code> if this
	 * is not an array type binding.
	 *
	 * @return the number of dimension of this array type binding, or 
	 *   <code>0</code> if this is not an array type
	 */
	public int getDimensions();
	
	/**
	 * Returns whether this type is assigment compatible with the given type,
	 * as specified in section 5.2 of <em>The Java Language 
	 * Specification, Second Edition</em> (JLS2).
	 * 
	 * @param type the type to check compatibility against
	 * @return <code>true</code> if this type is assigment compatible with the
	 * given type, and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isAssignmentCompatible(ITypeBinding type);
	
	/**
	 * Returns whether this type is cast compatible with the given type,
	 * as specified in section 5.5 of <em>The Java Language 
	 * Specification, Second Edition</em> (JLS2).
	 * 
	 * @param type the type to check compatibility against
	 * @return <code>true</code> if this type is cast compatible with the
	 * given type, and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isCastCompatible(ITypeBinding type);

	/**
	 * Returns whether this type binding represents a class type.
	 *
	 * @return <code>true</code> if this object represents a class,
	 *    and <code>false</code> otherwise
	 */
	public boolean isClass();
	
	/**
	 * Returns whether this type binding represents an interface type.
	 * <p>
	 * Note that an interface can also be an annotation type.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents an interface,
	 *    and <code>false</code> otherwise
	 */
	public boolean isInterface();
	
	/**
	 * Returns whether this type binding represents an enum type.
	 *
	 * @return <code>true</code> if this object represents an enum type,
	 *    and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isEnum();
	
	/**
	 * Returns whether this type binding represents an annotation type.
	 * <p>
	 * Note that an annotation type is always an interface.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents an annotation type,
	 *    and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isAnnotation();
	
	/**
	 * Returns the type parameters of this class or interface type binding.
	 * <p>
	 * Note that type parameters only occur on the binding of the
	 * declaring generic class or interface; e.g., <code>Collection&lt;T&gt;</code>.
	 * Type bindings corresponding to a raw or parameterized reference to a generic
	 * type do not carry type parameters (they instead have non-empty type arguments
	 * and non-trivial erasure).
	 * </p> 
	 *
	 * @return the list of binding for the type variables for the type
	 * parameters of this type, or otherwise the empty list
	 * @see #isTypeVariable()
	 * @since 3.1
	 */
	// TODO (jeem) - clarify whether binding for a generic type instance carries a copy of the generic type's type parameters as well as type arguments
	public ITypeBinding[] getTypeParameters();
	
	/**
	 * Returns whether this type binding represents a declaration of
	 * a generic class or interface.
	 * <p>
	 * Note that type parameters only occur on the binding of the
	 * declaring generic class or interface; e.g., <code>Collection&lt;T&gt;</code>.
	 * Type bindings corresponding to a raw or parameterized reference to a generic
	 * type do not carry type parameters (they instead have non-empty type arguments
	 * and non-trivial erasure).
	 * This method is fully equivalent to <code>getTypeParameters().length &gt; 0)</code>.
	 * </p>
	 * <p>
	 * Note that {@link #isGenericType()},
	 * {@link #isParameterizedType()},
	 * and {@link #isRawType()} are mutually exclusive.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding represents a 
	 * declaration of a generic class or interface, and <code>false</code> otherwise
	 * @see #getTypeParameters()
	 * @since 3.1
	 */
	public boolean isGenericType();
	
	/**
	 * Returns whether this type binding represents a type variable.
	 * Type variables bindings carry the type variable's bounds.
	 * 
	 * @return <code>true</code> if this type binding is for a type variable,
	 *   and <code>false</code> otherwise
	 * @see #getName()
	 * @see #getTypeBounds()
	 * @since 3.1
	 */
	public boolean isTypeVariable();
	
	/**
	 * Returns the type bounds of this type variable.
     * <p>
     * Note that the first type bound is always a class type. If the type
     * variable does not explicitly declare a class type bound, the first
     * type bound will be the binding for <code>java.lang.Object</code>.
     * </p>
	 *
	 * @return the list of type bound for this type variable, or otherwise
	 * the empty list
	 * @see #isTypeVariable()
	 * @since 3.1
	 */
	public ITypeBinding[] getTypeBounds();
	
	/**
	 * Returns whether this type binding represents an instance of
	 * a generic type corresponding to a parameterized type reference.
	 * <p>
	 * For example, an AST type like 
	 * <code>Collection&lt;String&gt;</code> typically resolves to a
	 * type binding whose type argument is the type binding for the
	 * class <code>java.lang.String</code> and whose erasure is the type
	 * binding for the generic type <code>java.util.Collection</code>.
	 * </p>
	 * Note that {@link #isGenericType()},
	 * {@link #isParameterizedType()},
	 * and {@link #isRawType()} are mutually exclusive.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding represents a 
	 * an instance of a generic type corresponding to a parameterized
	 * type reference, and <code>false</code> otherwise
	 * @see #getTypeArguments()
	 * @see #getTypeDeclaration()
	 * @since 3.1
	 */
	public boolean isParameterizedType();
	
	/**
	 * Returns the type arguments of this generic type instance, or the
	 * empty list for other type bindings.
	 * <p>
	 * Note that type arguments only occur on a type binding that represents
	 * an instance of a generic type corresponding to a parameterized type
	 * reference (e.g., <code>Collection&lt;String&gt;</code>) or to a raw
	 * type reference (e.g., <code>Collection</code>) to a generic type.
	 * Do not confuse these with type parameters which only occur on the
	 * type binding corresponding directly to the declaration of the 
	 * generic class or interface (e.g., <code>Collection&lt;T&gt;</code>).
	 * </p> 
	 *
	 * @return the list of type bindings for the type arguments used to
	 * instantiate the corrresponding generic type, or otherwise the empty list
	 * @see #getTypeDeclaration()
	 * @see #isGenericType()
	 * @see #isParameterizedType()
	 * @see #isRawType()
	 * @since 3.1
	 */
	public ITypeBinding[] getTypeArguments();
	
	/**
	 * Returns the erasure of this type binding.
	 * <ul>
	 * <li>For parameterized types ({@link #isParameterizedType()})
	 * - returns the binding for the corresponding generic type.</li>
	 * <li>For raw types ({@link #isRawType()})
	 * - returns the binding for the corresponding generic type.</li>
	 * <li>For wildcard types ({@link #isWildcardType()})
	 * - returns the binding for the upper bound if it has one and
	 * java.lang.Object in other cases.
	 * </li>
	 * <li>For type variables ({@link #isTypeVariable()})
	 * - returns the binding for the erasure of the leftmost bound
	 * if it has bounds and java.lang.Object if it does not.</li>
	 * <li>For all other type bindings - returns the identical binding.</li>
	 * </ul>
	 *
	 * @return the erasure type binding
	 * @since 3.1
	 */
	public ITypeBinding getErasure();
	
	/**
	 * Returns the binding for the type declaration corresponding to this type
	 * binding. For parameterized types ({@link #isParameterizedType()})
	 * and raw types ({@link #isRawType()}), this method returns the binding
	 * for the corresponding generic type. For other type bindings, this
	 * returns the same binding.
	 *
	 * @return the type binding
	 * @since 3.1
	 */
	public ITypeBinding getTypeDeclaration();
	
	/**
	 * Returns whether this type binding represents an instance of
	 * a generic type corresponding to a raw type reference.
	 * <p>
	 * For example, an AST type like 
	 * <code>Collection</code> typically resolves to a
	 * type binding whose type argument is the type binding for
	 * the class <code>java.lang.Object</code> (the
	 * default bound for the single type parameter of 
	 * <code>java.util.Collection</code>) and whose erasure is the
	 * type binding for the generic type
	 * <code>java.util.Collection</code>.
	 * </p>
	 * <p>
	 * Note that {@link #isGenericType()},
	 * {@link #isParameterizedType()},
	 * and {@link #isRawType()} are mutually exclusive.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding represents a 
	 * an instance of a generic type corresponding to a raw
	 * type reference, and <code>false</code> otherwise
	 * @see #getTypeDeclaration()
	 * @see #getTypeArguments()
	 * @since 3.1
	 */
	public boolean isRawType();
	
	/**
	 * Returns whether this type binding represents a wildcard type. A wildcard
	 * type occus only as an argument to a parameterized type reference.
	 * <p>
	 * For example, a AST type like 
	 * <code>Collection&lt;? extends Object&gt;</code> typically resolves to a
	 * parameterized type binding whose type argument is a wildcard type
	 * with upper type bound <code>java.util.Object/code>.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents a wildcard type,
	 *    and <code>false</code> otherwise
	 * @since 3.1
	 * @see #getBound()
	 * @see #isUpperbound()
	 */
	public boolean isWildcardType();
	
	/**
	 * Returns the bound of this wildcard type if it has one.
	 * Returns <code>null</code> if this is not a wildcard type.
	 * 
	 * @return the bound of this wildcard type, or <code>null</code> if none
	 * @see #isWildcardType()
	 * @see #isUpperbound()
	 * @since 3.1
	 */
	public ITypeBinding getBound();
	
	/**
	 * Returns whether this wildcard type is an upper bound
	 * ("extends") as opposed to a lower bound ("super").
	 * Note that this property is only relevant for wildcards
	 * that have a bound.
	 *
	 * @return <code>true</code> if this wildcard type has a bound that is
	 * an upper bound, and <code>false</code> in all other cases
	 * @see #isWildcardType()
	 * @see #getBound()
	 * @since 3.1
	 */
	public boolean isUpperbound();
	
	/**
	 * Returns the unqualified name of the type represented by this binding
	 * if it has one.
	 * <ul>
	 * <li>For top-level types, member types, and local types,
	 * the name is the simple name of the type.
	 * Example: <code>"String"</code> or <code>"Collection"</code>.
	 * Note that the type parameters of a generic type are not included.</li>
	 * <li>For primitive types, the name is the keyword for the primitive type.
	 * Example: <code>"int"</code>.</li>
	 * <li>For the null type, the name is the string "null".</li>
	 * <li>For anonymous classes, which do not have a name,
	 * this method returns an empty string.</li>
	 * <li>For array types, the name is the unqualified name of the component
	 * type (as computed by this method) followed by "[]".
	 * Example: <code>"String[]"</code>. Note that the component type is never an
	 * an anonymous class.</li>
	 * <li>For type variables, the name is just the simple name of the
	 * type variable (type bounds are not included).
	 * Example: <code>"X"</code>.</li>
	 * <li>For type bindings that correspond to particular instances of a generic
	 * type arising from a parameterized type reference,
	 * the name is the unqualified name of the erasure type (as computed by this method)
	 * followed by the names (again, as computed by this method) of the type arguments
	 * surrounded by "&lt;&gt;" and separated by ",".
	 * Example: <code>"Collection&lt;String&gt;"</code>.
	 * </li>
	 * <li>For type bindings that correspond to particular instances of a generic
	 * type arising from a raw type reference, the name is the unqualified name of
	 * the erasure type (as computed by this method).
	 * Example: <code>"Collection"</code>.</li>
	 * <li>For wildcard types, the name is "?" optionally followed by 
	 * a single space followed by the keyword "extends" or "super"
	 * followed a single space followed by the name of the bound (as computed by
	 * this method) when present.
	 * Example: <code>"? extends InputStream"</code>.
	 * </li>
	 * </ul> 
	 * 
	 * @return the unqualified name of the type represented by this binding,
	 * or the empty string if it has none
	 * @see #getQualifiedName()
	 */
	public String getName();
			
	/**
	 * Returns the binding for the package in which this type is declared.
	 * 
	 * @return the binding for the package in which this class, interface,
	 * enum, or annotation type is declared, or <code>null</code> if this type
	 * binding represents a primitive type, an array type, the null type, 
	 * a type variable, or a wildcard type.
	 */
	public IPackageBinding getPackage();
	
	/**
	 * Returns the type binding representing the class, interface, or enum
	 * that declares this binding.
	 * <p>
	 * The declaring class of a member class, interface, enum, annotation
	 * type is the class, interface, or enum type of which it is a member.
	 * The declaring class of a local class or interface (including anonymous
	 * classes) is the innermost class or interface containing the expression
	 * or statement in which this type is declared.
	 * </p>
	 * <p>The declaring class of a type variable is the class in which the type variable
	 * is declared if it is declared on a type. It returns <code>null</code> otherwise.
	 * </p>
	 * <p>Array types, primitive types, the null type, top-level types,
	 * and wildcard types have no declaring class.
	 * </p>
	 * 
	 * @return the binding of the type that declares this type, or
	 * <code>null</code> if none
	 */
	public ITypeBinding getDeclaringClass();

	/**
	 * Returns the method binding representing the method that declares this binding
	 * of a local type or type variable.
	 * <p>
	 * The declaring method of a local class or interface (including anonymous
	 * classes) is the innermost method containing the expression or statement in
	 * which this type is declared. Returns <code>null</code> if the type
	 * is declared in an initializer.
	 * </p>
	 * <p>
	 * The declaring method of a type variable is the method in which the type
	 * variable is declared if it is declared on a method. It
	 * returns <code>null</code> otherwise.
	 * </p>
	 * <p>Array types, primitive types, the null type, top-level types,
	 * and wildcard types have no declaring method.
	 * </p>
	 * 
	 * @return the binding of the method that declares this type, or
	 * <code>null</code> if none
	 */
	public IMethodBinding getDeclaringMethod();

	/**
	 * Returns the type binding for the superclass of the type represented
	 * by this class binding.
	 * <p>
	 * If this type binding represents any class other than the class
	 * <code>java.lang.Object</code>, then the type binding for the direct
	 * superclass of this class is returned. If this type binding represents
	 * the class <code>java.lang.Object</code>, then <code>null</code> is
	 * returned.
	 * <p>
	 * Loops that ascend the class hierarchy need a suitable termination test.
	 * Rather than test the superclass for <code>null</code>, it is more 
	 * transparent to check whether the class is <code>Object</code>, by 
	 * comparing whether the class binding is identical to 
	 * <code>ast.resolveWellKnownType("java.lang.Object")</code>.
	 * </p>
	 * <p>
	 * If this type binding represents an interface, an array type, a
	 * primitive type, the null type, a type variable, an enum type,
	 * an annotation type, or a wildcard type, then <code>null</code>
	 * is returned.
	 * </p>
	 *
	 * @return the superclass of the class represented by this type binding,
	 *    or <code>null</code> if none
	 * @see AST#resolveWellKnownType(String)
	 */
	public ITypeBinding getSuperclass();
	
	/**
	 * Returns a list of type bindings representing the direct superinterfaces
	 * of the class, interface, or enum type represented by this type binding. 
	 * <p>
	 * If this type binding represents a class or enum type, the return value
	 * is an array containing type bindings representing all interfaces
	 * directly implemented by this class. The number and order of the interface
	 * objects in the array corresponds to the number and order of the interface
	 * names in the <code>implements</code> clause of the original declaration
	 * of this type.
	 * </p>
	 * <p>
	 * If this type binding represents an interface, the array contains 
	 * type bindings representing all interfaces directly extended by this
	 * interface. The number and order of the interface objects in the array 
	 * corresponds to the number and order of the interface names in the 
	 * <code>extends</code> clause of the original declaration of this interface. 
	 * </p>
	 * <p>
	 * If the class or enum implements no interfaces, or the interface extends 
	 * no interfaces, or if this type binding represents an array type, a
	 * primitive type, the null type, a type variable, an annotation type, 
	 * or a wildcard type, this method returns an array of length 0.
	 * </p>
	 *
	 * @return the list of type bindings for the interfaces extended by this
	 *   class or enum, or interfaces extended by this interface, or otherwise 
	 *   the empty list
	 */
	public ITypeBinding[] getInterfaces();
		
	/**
	 * Returns the compiled modifiers for this class, interface, enum,
	 * or annotation type binding.
	 * The result may not correspond to the modifiers as declared in the
	 * original source, since the compiler may change them (in particular, 
	 * for inner class emulation). The <code>getDeclaredModifiers</code> method
	 * should be used if the original modifiers are needed. 
	 * Returns 0 if this type does not represent a class, interface, enum, or annotation
	 * type.
	 * 
	 * @return the compiled modifiers for this type binding or 0
	 * if this type does not represent a class, interface, enum, or annotation
	 * type
	 * @see #getDeclaredModifiers()
	 */
	public int getModifiers();
	
	/**
	 * Returns the declared modifiers for this class or interface binding
	 * as specified in the original source declaration of the class or 
	 * interface. The result may not correspond to the modifiers in the compiled
	 * binary, since the compiler may change them (in particular, for inner 
	 * class emulation). The <code>getModifiers</code> method should be used if
	 * the compiled modifiers are needed. Returns -1 if this type does not 
	 * represent a class or interface.
	 *
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see #getModifiers()
	 * @see Modifier
	 */
	public int getDeclaredModifiers();
	
	/**
	 * Returns whether this type is subtype compatible with the given type,
	 * as specified in section 4.10 of <em>The Java Language 
	 * Specification, Third Edition</em> (JLS3).
	 * 
	 * @param type the type to check compatibility against
	 * @return <code>true</code> if this type is subtype compatible with the
	 * given type, and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isSubTypeCompatible(ITypeBinding type);
	
	/**
	 * Returns whether this type binding represents a top-level class,
	 * interface, enum, or annotation type.
	 * <p>
	 * A top-level type is any type whose declaration does not occur within the
	 * body of another type declaration. The set of top level types is disjoint
	 * from the set of nested types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a top-level class,
	 *   interface, enum, or annotation type, and <code>false</code> otherwise
	 */
	public boolean isTopLevel();

	/**
	 * Returns whether this type binding represents a nested class, interface,
	 * enum, or annotation type.
	 * <p>
	 * A nested type is any type whose declaration occurs within
	 * the body of another. The set of nested types is disjoint from the set of
	 * top-level types. Nested types further subdivide into member types, local
	 * types, and anonymous types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a nested class,
	 *   interface, enum, or annotation type, and <code>false</code> otherwise
	 */
	public boolean isNested();

	/**
	 * Returns whether this type binding represents a member class or
	 * interface.
	 * <p>
	 * A member type is any type declared as a member of
	 * another type. A member type is a subspecies of nested
	 * type, and mutually exclusive with local types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a member class,
	 *   interface, enum, or annotation type, and <code>false</code> otherwise
	 */
	public boolean isMember();
	
	/**
	 * Returns whether this type binding represents a local class.
	 * <p>
	 * A local class is any nested class or enum type not declared as a member
	 * of another class or interface. A local class is a subspecies of nested
	 * type, and mutually exclusive with member types. Note that anonymous
	 * classes are a subspecies of local classes.
	 * </p>
	 * <p>
	 * Also note that interfaces and annotation types cannot be local.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a local class or
	 * enum type, and <code>false</code> otherwise
	 */
	public boolean isLocal();
	
	/**
	 * Returns whether this type binding represents an anonymous class.
	 * <p>
	 * An anonymous class is a subspecies of local class, and therefore mutually
	 * exclusive with member types. Note that anonymous classes have no name 
	 * (<code>getName</code> returns the empty string).
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for an anonymous class,
	 *   and <code>false</code> otherwise
	 */
	public boolean isAnonymous();

	/**
	 * Returns a list of type bindings representing all the types declared as
	 * members of this class, interface, or enum type. 
	 * These include public, protected, default (package-private) access,
	 * and private classes, interfaces, enum types, and annotation types
	 * declared by the type, but excludes inherited types. Returns an empty
	 * list if the type declares no type members, or if this type
	 * binding represents an array type, a primitive type, a type variable,
	 * a wildcard type, or the null type.
	 * The resulting bindings are in no particular order.
	 * 
	 * @return the list of type bindings for the member types of this type,
	 *   or the empty list if this type does not have member types
	 */
	public ITypeBinding[] getDeclaredTypes();
	
	/**
	 * Returns a list of bindings representing all the fields declared
	 * as members of this class, interface, or enum type. These include public, 
	 * protected, default (package-private) access, and private fields declared
	 * by the class, but excludes inherited fields. Synthetic fields may or
	 * may not be included.
	 * Returns an empty list if the class, interface, or enum declares no fields,
	 * and for other kinds of type bindings that do not directly have members.
	 * The resulting bindings are in no particular order.
	 * 
	 * @return the list of bindings for the field members of this type,
	 *   or the empty list if this type does not have field members
	 */
	public IVariableBinding[] getDeclaredFields();
	
	/**
	 * Returns a list of method bindings representing all the methods and 
	 * constructors declared for this class, interface, enum, or annotation
	 * type. These include public, protected, default (package-private) access,
	 * and private methods Synthetic methods and constructors may or may not be
	 * included. Returns an empty list if the class, interface, or enum,
	 * type declares no methods or constructors, if the annotation type declares
	 * no members, or if this type binding represents some other kind of type
	 * binding. The resulting bindings are in no particular order.
	 * 
	 * @return the list of method bindings for the methods and constructors
	 *   declared by this class, interface, enum type, or annotation type, 
	 *   or the empty list if this type does not declare any methods or constructors
	 */
	public IMethodBinding[] getDeclaredMethods();
	
	/**
	 * Returns whether this type binding originated in source code.
	 * Returns <code>false</code> for all primitive types, the null type,
	 * array types, and for all classes, interfaces, enums, annotation
	 * types, type variables, parameterized type references,
	 * raw type references, and wildcard types, whose information came from a
	 * pre-compiled binary class file.
	 * 
	 * @return <code>true</code> if the type is in source code,
	 *    and <code>false</code> otherwise
	 */
	public boolean isFromSource();
	
	/**
	 * Returns the fully qualified name of the type represented by this 
	 * binding if it has one.
	 * <ul>
	 * <li>For top-level types, the fully qualified name is the simple name of
	 * the type preceded by the package name (or unqualified if in a default package)
	 * and a ".".
	 * Example: <code>"java.lang.String"</code> or <code>"java.util.Collection"</code>.
	 * Note that the type parameters of a generic type are not included.</li>
	 * <li>For members of top-level types, the fully qualified name is the
	 * simple name of the type preceded by the fully qualified name of the
	 * enclosing type (as computed by this method) and a ".".
	 * Example: <code>"java.io.ObjectInputStream.GetField"</code>.
	 * If the binding is for a member type that corresponds to a particular instance
	 * of a generic type arising from a parameterized type reference, the simple
	 * name of the type is followed by the fully qualified names of the type arguments
	 * (as computed by this method) surrounded by "&lt;&gt;" and separated by ",".
	 * Example: <code>"pkg.Outer.Inner&lt;java.lang.String&gt;"</code>.
	 * </li>
	 * <li>For primitive types, the fully qualified name is the keyword for
	 * the primitive type.
	 * Example: <code>"int"</code>.</li>
	 * <li>For the null type, the fully qualified name is the string 
	 * "null".</li>
	 * <li>Local types (including anonymous classes) and members of local
	 * types do not have a fully qualified name. For these types, and array
	 * types thereof, this method returns an empty string.</li>
	 * <li>For array types whose component type has a fully qualified name, 
	 * the fully qualified name is the fully qualified name of the component
	 * type (as computed by this method) followed by "[]".
	 * Example: <code>"java.lang.String[]"</code>.</li>
	 * <li>For type variables, the fully qualified name is just the name of the
	 * type variable (type bounds are not included).
	 * Example: <code>"X"</code>.</li>
	 * <li>For type bindings that correspond to particular instances of a generic
	 * type arising from a parameterized type reference,
	 * the fully qualified name is the fully qualified name of the erasure
	 * type followed by the fully qualified names of the type arguments surrounded by "&lt;&gt;" and separated by ",".
	 * Example: <code>"java.util.Collection&lt;java.lang.String&gt;"</code>.
	 * </li>
	 * <li>For type bindings that correspond to particular instances of a generic
	 * type arising from a raw type reference,
	 * the fully qualified name is the fully qualified name of the erasure type.
	 * Example: <code>"java.util.Collection"</code>. Note that the
	 * the type parameters are omitted.</li>
	 * <li>For wildcard types, the fully qualified name is "?" optionally followed by 
	 * a single space followed by the keyword "extends" or "super" 
	 * followed a single space followed by the fully qualified name of the bound
	 * (as computed by this method) when present.
	 * Example: <code>"? extends java.io.InputStream"</code>.
	 * </li>
	 * </ul>
	 * 
	 * @return the fully qualified name of the type represented by this 
	 *    binding, or the empty string if it has none
	 * @see #getName()
	 * @since 2.1
	 */
	public String getQualifiedName();
}
