package org.eclipse.jdt.internal.core.builder;

public interface IType extends IMember {


	/**
	 * Compares this Type handle against the specified object.	Returns
	 * true if the objects are the same.  Two Types handles are the same if
	 * they both represent a class or interface and have the same fully qualified name,
	 * or if they both represent the same primitive type,
	 * or if they both represent an array type and have the same component types.
	 * See Handle.equals() for more details.
	 *
	 * @see Handle#equals
	 * @see Handle#hashCode
	 */
	boolean equals(Object obj);
	/**
	 * Returns a Type object representing an array type with
	 * the type represented by this object as its component type.
	 * This is a handle-only method.
	 *
	 * @see #getComponentType()
	 */
	IType getArrayHandle();
	/**
	 * If this class represents an array type, returns the Type
	 * object representing the component type of the array; otherwise
	 * returns null. The component type of an array may itself be
	 * an array type.
	 * This is a handle-only method.
	 *
	 * See <em>The Java Language Specification</em>, section 10, for details.
	 */
	IType getComponentType();
	/**
	 * Returns a Constructor object that represents the specified
	 * constructor of the class represented by this object. 
	 * The parameterTypes parameter is an array of Type objects that
	 * identify the constructor's formal parameter types, in declared
	 * order.
	 * Returns null if this type does not represent a class or interface.
	 * This is a handle-only method; the specified constructor may
	 * or may not actually be present in the class or interface.
	 *
	 * @see 	IConstructor
	 */
	IConstructor getConstructorHandle(IType[] parameterTypes);
	/**
	 * Returns a 32-bit CRC of the binary of the class or interface represented
	 * by this object.  The result should be treated as a 32-bit unsigned value.
	 * Returns 0 if this object does not represent a class or interface, or
	 * if the type is present but has no binary (for example, in the case of compiler
	 * errors).
	 * 
	 * @exception NotPresentException if this class or interface is not present.
	 */
	int getCRC() throws NotPresentException;
	/**
	 * Returns an array of Type objects representing all the classes
	 * and interfaces declared as members of the class represented by
	 * this object. This includes public, protected, default
	 * (package) access, and private classes and interfaces declared
	 * by the class, but excludes inherited classes and interfaces.
	 * Returns an array of length 0 if the class declares no classes
	 * or interfaces as members, or if this object represents an
	 * array type or primitive type.
	 * The resulting Types are in no particular order.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see #getDeclaringClass
	 * @see #isPackageMember
	 */
	IType[] getDeclaredClasses() throws NotPresentException;
	/**
	 * Returns an array of Constructor objects representing all the
	 * constructors declared by the class represented by this 
	 * object. These are public, protected, default (package) access,
	 * and private constructors.  Returns an array of length 0 if this
	 * object represents an interface, an array type or a primitive type.
	 * The resulting Constructors are in no particular order.
	 *
	 * <p>See <em>The Java Language Specification</em>, section 8.2.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see IMember#getDeclaringClass
	 */
	IConstructor[] getDeclaredConstructors() throws NotPresentException;
	/**
	 * Returns an array of Field objects representing all the fields
	 * declared by the class or interface represented by this 
	 * object. This includes public, protected, default (package)
	 * access, and private fields, but excludes inherited
	 * fields. Returns an array of length 0 if the class or interface
	 * declares no fields, or if this object represents a
	 * primitive type or an array type (the implicit <code>length</code>
	 * field of array types is not considered to be a declared field).
	 * The resulting Fields are in no particular order.
	 * <p>
	 * See <em>The Java Language Specification</em>, sections 8.2 and
	 * 8.3.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see IMember#getDeclaringClass
	 */
	IField[] getDeclaredFields() throws NotPresentException;
	/**
	 * Returns an array of Method objects representing all the methods
	 * declared by the class or interface represented by this
	 * object. This includes public, protected, default (package)
	 * access, and private methods, but excludes inherited
	 * methods. Returns an array of length 0 if the class or interface
	 * declares no methods, or if this object represents an
	 * array type or primitive type.
	 * The resulting Methods are in no particular order.
	 *
	 * <p>See <em>The Java Language Specification</em>, section 8.2.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see IMember#getDeclaringClass
	 */
	IMethod[] getDeclaredMethods() throws NotPresentException;
	/**
	 * Returns the declared Java language modifiers, as specified in the declaration
	 * of this class or interface, encoded in an integer. 
	 * The modifiers consist of the Java Virtual Machine's constants 
	 * for public, protected, private, and final; they should be decoded 
	 * using the methods of class Modifier.
	 * The result may not correspond to the modifiers in the compiled
	 * binary, since the compiler may change them (in particular,
	 * for inner classes).  The <code>getModifiers()</code>
	 * method should be used if the compiled modifiers are needed.
	 * Returns 0 if this type does not represent a class or interface.
	 *
	 * <p>The modifier encodings are defined in <em>The Java Virtual
	 * Machine Specification</em>, table 4.1.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see #getModifiers
	 * @see Modifier
	 */
	int getDeclaredModifiers() throws NotPresentException;
   /**
	 * Returns the declared name of the class or interface represented 
	 * by this object, as a String.
	 * The name is the simple, unqualified name used in the source code.
	 * If this represents an inner class, it does not include the names
	 * of any containing classes.
	 * If this represents an anonymous class, it returns a String of length 0.
	 * If this does not represent a class or interface, it returns 
	 * a String of length 0.
	 *
	 * @return	the declared name of the class or interface
	 *			represented by this object.
	 * @exception NotPresentException if this class or interface is not present.
	 */
	String getDeclaredName() throws NotPresentException;
	/**
	 * If the class or interface represented by this Type object is
	 * a member of another class or interface (i.e. it is a nested class), 
	 * this returns the Type object representing the class or interface 
	 * of which it is a member (its <em>declaring class</em>).
	 * If this class or interface is a local class, returns the Type
	 * object representing the class containing the member in which
	 * this class is declared.
	 * Returns null if this class or interface is not a nested class,
	 * or if this type does not represent a class or interface.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see #getDeclaredClasses
	 * @see #isPackageMember
	 */
	IType getDeclaringClass() throws NotPresentException;
	/**
	 * Returns a Field object that represents the specified
	 * member field of the class or interface represented 
	 * by this object. The name parameter is a String specifying
	 * the simple name of the desired field.
	 * Returns null if this type does not represent a class or interface.
	 * This is a handle-only method; the specified field may
	 * or may not actually be present in the class or interface.
	 *
	 * @see IField
	 */
	IField getFieldHandle(String name);
	/**
	 * Returns an array of Type objects representing the
	 * classes in the given ImageContext which directly implement this interface.
	 * A class is said to directly implement this interface if the interface
	 * appears in the <code>implements</code> clause of the class's declaration.
	 * Although all array types are considered to implement the <code>Cloneable</code>
	 * interface, this method never returns array types, only class types.
	 * Returns an array of length 0 if this object does not represent
	 * an interface.
	 * The resulting Types are in no particular order.
  	 * See <em>The Java Language Specification</em> section 8.1.4
	 * for more details.
	 * 
	 * @param imageContext the ImageContext in which to restrict the search.
	 * @exception NotPresentException if this interface is not present.
	 */
	IType[] getImplementingClasses(IImageContext imageContext) throws NotPresentException;
	/**
	 * Returns an array of Type objects representing the direct
	 * superinterfaces of the class or interface represented by this object. 
	 * <p>
	 * If this object represents a class, the return value is an array 
	 * containing objects representing all interfaces directly implemented by the 
	 * class. The order of the interface objects in the array corresponds 
	 * to the order of the interface names in the <code>implements</code> 
	 * clause of the declaration of the class represented by this object. 
	 * <p>
	 * If this object represents an interface, the array contains 
	 * objects representing all interfaces directly extended by the interface. 
	 * The order of the interface objects in the array corresponds to the 
	 * order of the interface names in the <code>extends</code> clause of 
	 * the declaration of the interface represented by this object. 
	 * <p>
	 * If the class or interface implements no interfaces, or if this 
	 * object represents neither a class nor an interface, this method 
	 * returns an array of length 0. 
	 *
  	 * See <em>The Java Language Specification</em> sections 8.1.4 and 9.1.3
	 * for more details.
	 *
	 * @return	an array of interfaces implemented by this class.
	 * @exception NotPresentException if this class or interface is not present.
	 */
	IType[] getInterfaces() throws NotPresentException;
	/**
	 * Returns a Method object that represents the specified
	 * member method of the class or interface represented 
	 * by this object. The name parameter is a String specifying the
	 * simple name the desired method, and the parameterTypes
	 * parameter is an array of Type objects that identify the
	 * method's formal parameter types, in declared order.
	 * Returns null if this type does not represent a class or interface.
	 * This is a handle-only method; the specified method may
	 * or may not actually be present in the class or interface.
	 *
	 * @see 	IMethod
	 */
	IMethod getMethodHandle(String name, IType[] parameterTypes);
	/**
	 * Returns the compiled Java language modifiers for this class or
	 * interface, encoded in an integer. The modifiers consist of the
	 * Java Virtual Machine's constants for public, protected,
	 * private, and final; they should be decoded using the
	 * methods of class Flags.
	 * The result may not correspond to the modifiers as declared in
	 * the source, since the compiler may change them (in particular,
	 * for inner classes).  The <code>getDeclaredModifiers()</code>
	 * method should be used if the original modifiers are needed.
	 * Returns 0 if this type does not represent a class or interface.
	 *
	 * <p>The modifier encodings are defined in <em>The Java Virtual
	 * Machine Specification</em>, table 4.1.
	 *
	 * @exception NotPresentException if this class or interface is not present.
	 *
	 * @see #getDeclaredModifiers
	 * @see Flags
	 */
	int getModifiers() throws NotPresentException;
	/**
	 * Returns the fully-qualified name of the type (class, interface,
	 * array, or primitive) represented by this object, as a String.
	 * For classes and interfaces, the name is the VM class name, 
	 * including the package name.
	 * For inner classes, the name is as described in the 
	 * <em>Inner Classes Specification</em>.
	 * For array types, the name is the name of the component type, followed by "[]".
	 * For primitive types, the name is the keyword for the primitive type.
	 * This is a handle-only method.
	 *
	 * @return	the fully qualified name of the type represented by this object.
	 */
	String getName();
	/**
	 * Returns the Package in which this class or interface is declared. 
	 * Returns null if this object represents a primitive type or array type.
	 * This is a handle-only method.
	 */
	IPackage getPackage();
	/**
	 * Returns the simple name of the type (class, interface, array, 
	 * or primitive) represented by this object, as a String.
	 * For classes, interfaces, inner classes and anonymous classes,
	 * this is the VM class name, excluding the package name.
	 * For array types, this is the simple name of the component type, followed by "[]".
	 * For primitive types, this is the keyword for the primitive type.
	 * This is a handle-only method.
	 *
	 * @return	the simple name of the type represented by this object.
	 */
	String getSimpleName();
	/**
	 * Returns a SourceFragment describing the fragment of source
	 * from which this type is derived.
	 * Returns null if this type represent a primitive type or an
	 * array type, or if this type is not derived directly from source
	 * (e.g. a fictional type, which is created by the image builder).
	 *
	 * @exception NotPresentException if the member is not present.
	 */
	ISourceFragment getSourceFragment() throws NotPresentException;
	/**
	 * Returns an array of Type objects representing the
	 * classes in the given ImageContext which are direct subclasses of
	 * this class.
	 * Returns an array of length 0 if this object does not represent
	 * a class.
	 * The resulting Types are in no particular order.
  	 * See <em>The Java Language Specification</em> sections 8.1.3 and 20.3.4
	 * for more details.
	 * 
	 * @param imageContext the ImageContext in which to restrict the search.
	 * @exception NotPresentException if this class is not present.
	 */
	IType[] getSubclasses(IImageContext imageContext) throws NotPresentException;
	/**
	 * Returns an array of Type objects representing the
	 * interfaces in the given ImageContext which are direct subinterfaces of
	 * this interface.  
	 * Returns an array of length 0 if this object does not represent
	 * an interface.
	 * The resulting Types are in no particular order.
  	 * See <em>The Java Language Specification</em> section 9.1.3
	 * for more details.
	 * 
	 * @param imageContext the ImageContext in which to restrict the search.
	 * @exception NotPresentException if this interface is not present.
	 */
	IType[] getSubinterfaces(IImageContext imageContext) throws NotPresentException;
	/**
	 * If this object represents any class other than the class 
	 * <code>java.lang.Object</code>, then the object that represents 
	 * the direct superclass of that class is returned.
	 * <p>
	 * If this object represents the class <code>java.lang.Object</code> 
	 * or this object represents an interface or a primitive type, 
	 * <code>null</code> is returned. 
	 * If this object represents an array type, then the Type that represents
	 * class <code>java.lang.Object</code> is returned.
	 * <p>
  	 * See <em>The Java Language Specification</em> sections 8.1.3 and 20.3.4
	 * for more details.
	 *
	 * @return	the superclass of the class represented by this object.
	 * @exception NotPresentException if this type is not present.
	 */
	IType getSuperclass() throws NotPresentException;
	/**
	 * Returns true if this object represents an anonymous class,
	 * false otherwise.
	 * An anonymous class is a local inner class with no declared name.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 *
	 * @exception NotPresentException if this class is not present.
	 *
	 * @see #isLocal()
	 */
	boolean isAnonymous() throws NotPresentException;
	/**
	 * If this Type object represents an array type, returns true,
	 * otherwise returns false.
	 * This is a handle-only method.
	 *
	 * @see #isClass
	 * @see #isInterface
	 * @see #isPrimitive
	 */
	boolean isArray();
	/**
	 * Return <code>true</code> if this represents a binary class or interface, 
	 * <code>false</code> otherwise.
	 * A binary type is one which is in .class file format in the workspace.
	 * Returns <code>false</code> if this represents a primitive type or an array type.
	 *
	 * @return	<code>true</code> if this object represents a binary class or interface;
	 *			<code>false</code> otherwise.
	 * @exception NotPresentException if this type is not present.
	 */
	boolean isBinary() throws NotPresentException;
	/**
	 * Determines if this object represents a class type.
	 * This returns false if this object represents an interface,
	 * an array type, or a primitive type.
	 *
	 * @return	<code>true</code> if this object represents a class;
	 *			<code>false</code> otherwise.
	 * @exception NotPresentException if this type is not present.
	 *
	 * @see #isArray
	 * @see #isInterface
	 * @see #isPrimitive
	 */
	boolean isClass() throws NotPresentException;
	/**
	 * Return <code>true</code> if this represents a deprecated member,
	 * <code>false</code> otherwise.
	 * A deprecated object is one that has a 'deprecated' tag in 
	 * its doc comment.
	 * Returns <code>false</code> if this represents a primitive type or an array type.
	 *
	 * @return	<code>true</code> if this object represents a deprecated member;
	 *			<code>false</code> otherwise.
	 * @exception NotPresentException if this type is not present.
	 */
	boolean isDeprecated() throws NotPresentException;
	/**
	 * Returns true if this object represents an inner class or interface,
	 * false otherwise.
	 * An inner class is one which can only be created in the context of
	 * an instance of its outer class.	This does not include package member
	 * classes or other top-level classes.	Such a class cannot be static.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 *
	 * @exception NotPresentException if this class is not present.
	 *
	 * @see #isTopLevel()
	 */
	boolean isInnerClass() throws NotPresentException;
	/**
	 * Determines if this object represents an interface type.
	 * This returns false if this object represents a class,
	 * an array type, or a primitive type.
	 *
	 * @return	<code>true</code> if this object represents an interface;
	 *			<code>false</code> otherwise.
	 * @exception NotPresentException if this type is not present.
	 *
	 * @see #isArray
	 * @see #isClass
	 * @see #isPrimitive
	 */
	boolean isInterface() throws NotPresentException;
	/**
	 * Returns true if this object represents a local inner class,
	 * false otherwise.
	 * A local inner class is an inner class which is defined in the body of
	 * a method or other block, not as a class field.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 *
	 * @exception NotPresentException if this class is not present.
	 *
	 * @see #isInnerClass()
	 */
	boolean isLocal() throws NotPresentException;
	/**
	 * Returns true if this object represents a class or interface
	 * which is declared as a package member (i.e. a 'normal' class 
	 * as in JDK 1.02).  Returns false otherwise.
	 * In particular, this method returns false if this object represents a
	 * top-level class which is declared as a member of a class.
	 * For the sake of consistent terminology, a class which is 
	 * not a package member is considered 'nested', whether or not 
	 * it is top-level.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 *
	 * @exception NotPresentException if this class is not present.
	 * @see #isTopLevel()
	 */
	boolean isPackageMember() throws NotPresentException;
	/**
	 * Primitive types are always present.
	 * A class or interface type is present if:
	 * <ul>
	 * <li>its package is present, and
	 * <li>the package declares a type of the same name
	 * </ul>
	 * An array type is present if and only if its component type is present.
	 * See Handle.isPresent() for more details.
	 *
	 * @see IHandle#isPresent
	 */
	boolean isPresent();
	/**
	 * Determines if the specified Type object represents a primitive Java
	 * type.
	 * This is a handle-only method.
	 *
	 * <p>There are nine predefined Type objects to represent the eight
	 * primitive Java types and void.  These are created by the Java
	 * Virtual Machine, and have the same names as the primitive types
	 * that they represent, namely boolean, byte, char, short, int,
	 * long, float, and double, and void.
	 *
	 * <p>These objects may only be accessed via the following methods, 
	 * and are the only objects for which this method returns true.
	 *
	 * @see #isArray
	 * @see #isClass
	 * @see #isInterface
	 * @see IImage#booleanType()
	 * @see IImage#charType()
	 * @see IImage#byteType()
	 * @see IImage#shortType()
	 * @see IImage#intType()
	 * @see IImage#longType()
	 * @see IImage#floatType()
	 * @see IImage#doubleType()
	 * @see IImage#voidType()
	 */
	boolean isPrimitive();
	/**
	 * Returns true if the type represented by this object is
	 * synthetic, false otherwise.  A synthetic object is one that
	 * was invented by the compiler, but was not declared in the source.
	 * See <em>The Inner Classes Specification</em>.
	 * A synthetic object is not the same as a fictional object.
	 * 
	 * @exception NotPresentException if the type is not present.
	 *
	 * @see Handle#isFictional
	 */
	boolean isSynthetic() throws NotPresentException;
	/**
	 * Returns true if this object represents a top-level class or interface,
	 * false otherwise.
	 * A top-level class is declared either as a package member or as a 
	 * static member of another top-level class.  Unlike inner classes, 
	 * instances of top-level classes are not created in the context of 
	 * another object.
	 * Given the appropriate access modifiers, a top-level class can be 
	 * referred to directly by a qualified name.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 *
	 * @exception NotPresentException if this class is not present.
	 *
	 * @see #isInnerClass()
	 */
	boolean isTopLevel() throws NotPresentException;
	/**
	 * Converts the object to a string. 
	 * If this represents a class or interface, the string representation 
	 * is the  string <code>"type"</code> followed by a space and then 
	 * the fully qualified name of the class or interface. 
	 * If this represents a primitive type, the string representation
	 * is the keyword for the primitive type.
	 * If this represents an array type, the string representation is
	 * that of its component type followed by <code>"[]"</code>.
	 *
	 * @return	a string representation of this class object. 
	 * @see IHandle#toString
	 */
	String toString();
}
