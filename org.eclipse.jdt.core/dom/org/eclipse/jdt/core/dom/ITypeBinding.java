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
 * <li>a parameterized type reference - represents a reference to a
 * parameterized type with type arguments (which may include wildcards)</li>
 * <li>a raw type reference - represents a (legacy) reference to a parameterized
 * type without any type arguments</li>
 * <li>a wildcard type - represents a wild card used as a type argument in
 * a parameterized type reference</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Note: Support for new language features proposed for the upcoming 1.5
 * release of J2SE is tentative and subject to change.
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
	 * Returns whether this type binding represents a class type.
	 *
	 * @return <code>true</code> if this object represents a class,
	 *    and <code>false</code> otherwise
	 */
	public boolean isClass();
	
	/**
	 * Returns whether this type binding represents an interface type.
	 *
	 * @return <code>true</code> if this object represents an interface,
	 *    and <code>false</code> otherwise
	 */
	public boolean isInterface();
	
	/**
	 * Returns whether this type binding represents an enum type.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents an enum type,
	 *    and <code>false</code> otherwise
	 * @since 3.0
	 */
	public boolean isEnum();
	
	/**
	 * Returns whether this type binding represents an annotation type.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents an annotation type,
	 *    and <code>false</code> otherwise
	 * @since 3.0
	 */
	public boolean isAnnotation();
	
	/**
	 * Returns the type parameters of this class or interface type binding.
	 * <p>
	 * Note that type parameters only occur on the declaring class or
	 * interface; e.g., <code>Collection&lt;T&gt;</code>. Do not confuse
	 * them with type arguments which only occur on references; 
	 * e.g., <code>Collection&lt;String&gt;</code>.
	 * </p> 
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the list of binding for the type variables for the type
	 * parameters of this type, or otherwise the empty list
	 * @see #isTypeVariable()
	 * @since 3.0
	 */
	public ITypeBinding[] getTypeParameters();
	
	/**
	 * Returns whether this type binding represents a type variable.
	 * Type variables bindings carry the type variable's bounds.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 * 
	 * @return <code>true</code> if this type binding is for a type variable,
	 *   and <code>false</code> otherwise
	 * @see #getTypeBounds()
	 * @since 3.0
	 */
	public boolean isTypeVariable();
	
	/**
	 * Returns the type bounds of this type variable.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the list of type bindings for this type variable, or otherwise
	 * the empty list
	 * @see #isTypeVariable()
	 * @since 3.0
	 */
	public ITypeBinding[] getTypeBounds();
	
	/**
	 * Returns whether this type binding represents a parameterized 
	 * type reference.
	 * <p>
	 * For example, a AST type like 
	 * <code>Collection&lt;String&gt;</code> typically resolves to a
	 * parameterized type binding whose erasure is a type binding for the class 
	 * <code>java.util.Collection</code> and whose type argument is a type
	 * binding for the class <code>java.util.Collection</code>.
	 * </p>
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents a parameterized
	 * type reference, and <code>false</code> otherwise
	 * @see #getTypeArguments()
	 * @see #getErasure()
	 * @since 3.0
	 */
	public boolean isParameterizedType();
	
	/**
	 * Returns the type arguments of the parameterized type reference.
	 * <p>
	 * Note that type arguments only occur on type references; 
	 * e.g., <code>Collection&lt;String&gt;</code>.
	 * Do not confuse with type parameters which only occur on the
	 * declaring class or interface; e.g., <code>Collection&lt;T&gt;</code>.
	 * </p> 
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the list of type bindings for the type arguments of this
	 * parameterized type, or otherwise the empty list
	 * @since 3.0
	 */
	public ITypeBinding[] getTypeArguments();
	
	/**
	 * Returns the erasure of this type reference. For a parameterized
	 * type reference or a raw type reference, returns the type binding for
	 * the class or interface where the referenced type is declared.
	 * Returns this type binding for types other than parameterized types.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the erasure type
	 * @see #isRawType()
	 * @see #isParameterizedType()
	 * @since 3.0
	 */
	public ITypeBinding getErasure();
	
	/**
	 * Returns whether this type binding represents a raw type reference. 
	 * A raw type is a unparameterized (legacy) reference to a type declared
	 * with type parameters.
	 * <p>
	 * For example, a AST type like 
	 * <code>Collection</code> typically resolves to a
	 * raw type binding whose erasure is a type binding for the class 
	 * <code>java.util.Collection</code>.
	 * </p>
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents a raw type,
	 *    and <code>false</code> otherwise
	 * @see #getErasure()
	 * @since 3.0
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
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents a wildcard type,
	 *    and <code>false</code> otherwise
	 * @since 3.0
	 * @see #getBound()
	 * @see #isUpperbound()
	 */
	public boolean isWildcardType();
	
	/**
	 * Returns the bound of this wildcard type if it has one.
	 * Returns <code>null</code> if this is not a wildcard type.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 * 
	 * @return the bound of this wildcard type, or <code>null</code> if none
	 * @see #isWildcardType()
	 * @see #isUpperbound()
	 * @since 3.0
	 */
	public ITypeBinding getBound();
	
	/**
	 * Returns whether this wildcard type is an upper bound
	 * ("extends") as opposed to a lower bound ("super").
	 * Note that this property is only relevant for wildcards
	 * that have a bound.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this wildcard type has a bound that is
	 * an upper bound, and <code>false</code> in all other cases
	 * @see #isWildcardType()
	 * @see #getBound()
	 * @since 3.0
	 */
	public boolean isUpperbound();
	
	/**
	 * Returns the unqualified name of the type represented by this binding
	 * if it has one.
	 * <p>
	 * For named classes, interfaces, enums, and annotation types, this is the
	 * simple name of the type; if the type is parameterized, the name is
	 * followed by the simple names of the type variables surrounded
	 * by "&lt;&gt;" and separated by "," (the type bounds are not included).
	 * For primitive types, the name is the keyword for the primitive type. For
	 * array types, the name is the name of the component type (as computed by
	 * this method) followed by "[]". If this represents an
	 * anonymous class, it returns an empty string (note that it is impossible
	 * to have an array type with an anonymous class as element type). For the
	 * null type, it returns "null".
	 * For type variables, this is the name of the type variable.
	 * For parameterized type references, this is the simple name of the
	 * erasure type followed by the names of the type arguments (as computed by
	 * this method) surrounded by "&lt;&gt;" and separated by ",".
	 * For raw type references, this is the simple name of the erasure type.
	 * For wildcard types, this is "?" followed by the name of the bound 
	 * (as computed by this method) when present.
	 * </p>
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
	 * a type variable, a parameterized type reference, a raw type reference,
	 * or a wildcard type
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
	 * or statement in which this type is declared. Array types,
	 * primitive types, the null type, top-level types, type variables,
	 * parameterized type references, raw type references, and wildcard types
	 * have no declaring class.
	 * </p>
	 * 
	 * @return the binding of the type that declares this type, or
	 * <code>null</code> if none
	 */
	public ITypeBinding getDeclaringClass();
	
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
	 * an annotation type, a parameterized type reference, a raw type
	 * reference, or a wildcard type, then <code>null</code> is returned.
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
	 * a parameterized type reference, a raw type reference, or a wildcard type,
	 * this method returns an array of length 0.
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
	 * Returns 0 if this type does not represent a class or interface.
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
	 * binding represents an array type, a primitive type, a wildcard type,
	 * a parameterized type reference, a raw type reference, or the null type.
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
	 * constructors declared for this class, interface, or enum. These include
	 * public, protected, default (package-private) access, and private methods. 
	 * Synthetic methods and constructors may or may not be included. Returns
	 * an empty list if the class, interface, or enum declares no methods or 
	 * constructors, or if this type binding represents some other kind of type
	 * bindings. The resulting bindings are in no particular order.
	 * 
	 * @return the list of method bindings for the methods and constructors
	 *   declared by this class, interface, or enum type, or the empty list if
	 * this type does not declare any methods or constructors
	 * TODO (jeem) - should result include bindings for annotation type members?
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
	 * <li>For top-level types, the fully qualified name is the name of
	 * the type (as computed by {@link #getName()}) preceded by the package
	 * name (or unqualified if in a default package) and a ".".
	 * Example: <code>"java.lang.String"</code>.</li>
	 * <li>For members of top-level types, the fully qualified name is the
	 * simple name of the type preceded by the fully qualified name of the
	 * enclosing type (as computed by this method) and a ".".
	 * Example: <code>"java.io.ObjectInputStream.GetField"</code>.</li>
	 * <li>For primitive types, the fully qualified name is the keyword for
	 * the primitive type.
	 * Example: <code>"int"</code>.</li>
	 * <li>For array types whose component type has a fully qualified name, 
	 * the fully qualified name is the fully qualified name of the component
	 * type (as computed by this method) followed by "[]".
	 * Example: <code>"java.lang.String[]"</code>.</li>
	 * <li>For the null type, the fully qualified name is the string 
	 * "null".</li>
	 * <li>Local types (including anonymous classes) and members of local
	 * types do not have a fully qualified name. For these types, and array
	 * types thereof, this method returns an empty string.</li>
	 * <li>For type variables, the fully qualified name is just the name of the
	 * type variable (type bounds are not included).
	 * Example: <code>"X"</code>.</li>
	 * <li>For raw type references, the fully qualified name is the 
	 * fully qualified name of the type but with the type parameters
	 * omitted.
	 * Example: <code>"java.util.Collection"</code>.</li>
	 * <li>For parameterized type references, the fully qualified name is the 
	 * fully qualified name of the erasure type followed by the fully qualified
	 * names of the type arguments surrounded by "&lt;&gt;" and separated by ",".
	 * Example: <code>"java.util.Collection&lt;java.lang.String&gt;"</code>.
	 * </li>
	 * <li>For wildcard types, the fully qualified name is "?" followed by the
	 * fully qualified name of the bound (as computed by this method) when
	 * present.
	 * Example: <code>"? extends java.lang.Object"</code>.
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
