package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.builder.*;

import java.util.Hashtable;

public class ClassOrInterfaceHandleImplSWH extends TypeImplSWH {
	ClassOrInterfaceHandleImpl fHandle;
	/**
	 * Creates a new method handle in the given state
	 */
	ClassOrInterfaceHandleImplSWH(
		StateImpl state,
		ClassOrInterfaceHandleImpl handle)
		throws NotPresentException {
		if (state == null)
			throw new NotPresentException();
		fState = state;
		fHandle = handle;
	}

	/**
	 * Returns the IBinaryType for this class or interface.
	 * Throws a not present exception if the type is not present.
	 */
	protected IBinaryType getBinaryType() throws NotPresentException {
		return fState.getBinaryType(getTypeStructureEntry());
	}

	/**
	 * Returns the IBinaryType for this class or interface. 
	 */
	protected IBinaryType getBinaryType(TypeStructureEntry tsEntry) {
		return fState.getBinaryType(tsEntry);
	}

	/**
	 * Returns a Constructor object that represents the specified
	 * constructor of the class represented by this object. 
	 * The parameterTypes parameter is an array of Type objects that
	 * identify the constructor's formal parameter types, in declared
	 * order.
	 * Returns null if this type does not represent a class or interface.
	 * This is a handle-only method; the specified constructor may
	 * or may not actually be present in the class or interface.
	 */
	public IConstructor getConstructorHandle(IType[] parameterTypes) {
		IType[] params = new IType[parameterTypes.length];
		for (int i = 0; i < params.length; i++)
			params[i] = (IType) parameterTypes[i].nonStateSpecific();
		return (IConstructor) fHandle.getConstructorHandle(params).inState(fState);
	}

	/**
	 * @see IType#getCRC
	 */
	public int getCRC() throws NotPresentException {
		TypeStructureEntry tsEntry = getTypeStructureEntry();
		int crc = tsEntry.getCRC32();
		if (crc == 0) {
			/* If it's a class file, then read the binary from the workspace and compute the CRC */
			if (tsEntry.isBinary()) {
				byte[] binary = fState.getElementContentBytes(tsEntry.getSourceEntry());
				crc = fState.getBinaryOutput().crc32(binary);
				tsEntry.setCRC32(crc);
			}
		}
		return crc;
	}

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
	 */
	public IType[] getDeclaredClasses() throws NotPresentException {
		TypeStructureEntry tsEntry = getTypeStructureEntry();
		IBinaryType binaryType = getBinaryType(tsEntry);
		char[][] names = BinaryStructure.getInnerTypes(binaryType);
		int len = names.length;
		IType[] types = new IType[len];
		int count = 0;
		for (int i = 0; i < len; i++) {
			IType type =
				(IType) fState
					.typeNameToHandle(tsEntry, BinaryStructure.convertTypeName(names[i]))
					.inState(fState);
			if (!type.isLocal()) {
				types[count++] = type;
			}
		}
		if (count < len) {
			System.arraycopy(types, 0, types = new IType[count], 0, count);
		}
		return types;
	}

	/**
	 * Returns an array of Constructor objects representing all the
	 * constructors declared by the class represented by this 
	 * object. These are public, protected, default (package) access,
	 * and private constructors.  Returns an array of length 0 if this
	 * object represents an interface, an array type or a primitive type.
	 * The resulting Constructors are in no particular order.
	 */
	public IConstructor[] getDeclaredConstructors() throws NotPresentException {
		IBinaryMethod[] methods = getBinaryType().getMethods();
		if (methods == null)
			return new IConstructor[0];

		// count constructors
		int n = 0;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isConstructor()) {
				++n;
			}
		}
		IConstructor[] result = new IConstructor[n];
		int k = 0;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isConstructor()) {
				result[k++] =
					(IConstructor) BinaryStructure
						.getConstructorHandle(methods[i], fHandle)
						.inState(fState);
			}
		}
		return result;
	}

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
	 */
	public IField[] getDeclaredFields() throws NotPresentException {
		IBinaryField[] fields = getBinaryType().getFields();
		if (fields == null)
			return new IField[0];
		IField[] handles = new IField[fields.length];
		for (int i = 0; i < fields.length; i++) {
			handles[i] =
				(IField) BinaryStructure.getFieldHandle(fields[i], fHandle).inState(fState);
		}
		return handles;
	}

	/**
	 * Returns an array of Method objects representing all the methods
	 * declared by the class or interface represented by this
	 * object. This includes public, protected, default (package)
	 * access, and private methods, but excludes inherited
	 * methods. Returns an array of length 0 if the class or interface
	 * declares no methods, or if this object represents an
	 * array type or primitive type.
	 * The resulting Methods are in no particular order.
	 */
	public IMethod[] getDeclaredMethods() throws NotPresentException {
		IBinaryMethod[] methods = getBinaryType().getMethods();
		if (methods == null)
			return new IMethod[0];

		// count methods
		int n = 0;
		for (int i = 0; i < methods.length; i++) {
			if (!methods[i].isConstructor()
				&& methods[i].getSelector()[0] != '<') { // TBD: need IBinaryMethod.isClinit()
				++n;
			}
		}
		IMethod[] result = new IMethod[n];
		int k = 0;
		for (int i = 0; i < methods.length; i++) {
			if (!methods[i].isConstructor()
				&& methods[i].getSelector()[0] != '<') { // TBD: need IBinaryMethod.isClinit()
				result[k++] =
					(IMethod) BinaryStructure.getMethodHandle(methods[i], fHandle).inState(fState);
			}
		}
		return result;
	}

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
	 */
	public int getDeclaredModifiers() throws NotPresentException {

		return getModifiers();
	}

	/**
	 * Returns the declared name of the class or interface represented 
	 * by this object, as a String.
	 * The name is the simple, unqualified name used in the source code.
	 * If this represents an inner class, it does not include the names
	 * of any containing classes.
	 * If this represents an anonymous class, it returns a String of length 0.
	 * If this does not represent a class or interface, it returns 
	 * a String of length 0.
	 */
	public String getDeclaredName() throws NotPresentException {

		if (isAnonymous()) {
			return "";
		}

		String name = fHandle.getSimpleName();
		return name.substring(name.lastIndexOf('$') + 1);
	}

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
	 */
	public IType getDeclaringClass() {
		TypeStructureEntry tsEntry = getTypeStructureEntry();
		IBinaryType binaryType = getBinaryType(tsEntry);
		char[] enclosing = BinaryStructure.getEnclosingTypeName(binaryType);
		if (enclosing == null) {
			return null;
		}
		return (IType) fState
			.typeNameToHandle(tsEntry, BinaryStructure.convertTypeName(enclosing))
			.inState(fState);
	}

	/**
	 * Returns a Field object that represents the specified
	 * member field of the class or interface represented 
	 * by this object. The name parameter is a String specifying
	 * the simple name of the desired field.
	 * Returns null if this type does not represent a class or interface.
	 * This is a handle-only method; the specified field may
	 * or may not actually be present in the class or interface.
	 */
	public IField getFieldHandle(String name) {
		return (IField) ((IType) fHandle).getFieldHandle(name).inState(fState);
	}

	/**
	  * Returns the non state specific handle
	  */
	protected TypeImpl getHandle() {
		return fHandle;
	}

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
	 */
	public IType[] getImplementingClasses(IImageContext imageContext)
		throws NotPresentException {

		return getSubtypes(imageContext, true, false);
	}

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
	 */
	public IType[] getInterfaces() throws NotPresentException {
		char[][] interfaces = getBinaryType().getInterfaceNames();
		if (interfaces == null) {
			return new IType[0];
		}
		int len = interfaces.length;
		IType[] results = new IType[len];
		if (len > 0) {
			TypeStructureEntry tsEntry = getTypeStructureEntry();
			for (int i = 0; i < len; i++) {
				results[i] = (IType) BinaryStructure.getType(fState, tsEntry, interfaces[i]);
			}
		}
		return results;
	}

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
	 */
	public IMethod getMethodHandle(String name, IType[] parameterTypes) {
		IType[] paramNSS = new IType[parameterTypes.length];
		// non-state-specific handles
		for (int i = 0; i < parameterTypes.length; i++)
			paramNSS[i] = (IType) parameterTypes[i].nonStateSpecific();

		return (IMethod) ((IType) fHandle).getMethodHandle(name, paramNSS).inState(
			fState);
	}

	/**
	 * Returns the compiled Java language modifiers for this class or
	 * interface, encoded in an integer. The modifiers consist of the
	 * Java Virtual Machine's constants for public, protected,
	 * private, and final; they should be decoded using the
	 * methods of class Modifier.
	 * The result may not correspond to the modifiers as declared in
	 * the source, since the compiler may change them (in particular,
	 * for inner classes).  The <code>getDeclaredModifiers()</code>
	 * method should be used if the original modifiers are needed.
	 * Returns 0 if this type does not represent a class or interface.
	 */
	public int getModifiers() throws NotPresentException {
		int flags = getBinaryType().getModifiers();
		return flags & (0xFFFF & ~IConstants.AccInterface & ~IConstants.AccSuper);
		// clear out special flags and interface flag
	}

	/**
	 * Returns the Package in which this class or interface is declared. 
	 * Returns null if this object represents a primitive type or array type.
	 * This is a handle-only method.
	 */
	public IPackage getPackage() {
		return (IPackage) fHandle.getPackage().inState(fState);
	}

	/**
	 * Returns a SourceFragment describing the fragment of source
	 * from which this member is derived.
	 * Returns null if this type represent a primitive type or an
	 * array type, or if this type is not derived directly from source
	 * (e.g. a fictional type, which is created by the image builder).
	 */
	public ISourceFragment getSourceFragment() throws NotPresentException {
		return getTypeStructureEntry().getSourceFragment();
	}

	/**
	 * Returns an array of Type objects representing the
	 * classes in the given ImageContext which are direct subclasses of
	 * this class.
	 * Returns an array of length 0 if this object does not represent
	 * a class.
	 * The resulting Types are in no particular order.
	 * See <em>The Java Language Specification</em> sections 8.1.3 and 20.3.4
	 * for more details.
	 */
	public IType[] getSubclasses(IImageContext imageContext)
		throws NotPresentException {

		return getSubtypes(imageContext, true, false);
	}

	/**
	 * Returns an array of Type objects representing the
	 * interfaces in the given ImageContext which are direct subinterfaces of
	 * this interface.  
	 * Returns an array of length 0 if this object does not represent
	 * an interface.
	 * The resulting Types are in no particular order.
	 * See <em>The Java Language Specification</em> section 9.1.3
	 * for more details.
	 */
	public IType[] getSubinterfaces(IImageContext imageContext)
		throws NotPresentException {

		return getSubtypes(imageContext, false, true);
	}

	/**
	 * Returns an array of Type objects representing the
	 * types in the given ImageContext which are direct subtypes of this interface.
	 * @param includeClasses true iff classes are candidates
	 * @param includeInterfaces true iff interfaces are candidates
	 */
	IType[] getSubtypes(
		IImageContext imageContext,
		boolean includeClasses,
		boolean includeInterfaces)
		throws NotPresentException {

		Hashtable table = fState.getSubtypesTable(imageContext);
		TypeStructureEntry[] subtypes = (TypeStructureEntry[]) table.get(this);
		if (subtypes == null) {
			if (!isPresent())
				throw new NotPresentException();
			else
				return new IType[0];
		}

		int numSubtypes = subtypes.length;
		int count = 0;
		IType[] result = new IType[numSubtypes];
		for (int i = 0, n = numSubtypes; i < n; i++) {
			IType type = (IType) subtypes[i].getType().inState(fState);
			if (type.isInterface()) {
				if (!includeInterfaces)
					continue;
			} else {
				if (!includeClasses)
					continue;
			}
			result[count++] = type;
		}

		if (count < numSubtypes)
			System.arraycopy(result, 0, result = new IType[count], 0, count);
		return result;
	}

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
	 */
	public IType getSuperclass() throws NotPresentException {
		return BinaryStructure.getType(
			fState,
			getTypeStructureEntry(),
			getBinaryType().getSuperclassName());
	}

	/**
	 * Returns the type structure entry for this class or interface.  Throws a
	 * not present exception if the type is not present.
	 */
	public TypeStructureEntry getTypeStructureEntry() throws NotPresentException {
		TypeStructureEntry tsEntry = fState.getTypeStructureEntry(fHandle, true);
		if (tsEntry != null) {
			return tsEntry;
		}
		throw new NotPresentException();
	}

	/**
	 * Returns true if this object represents an anonymous class,
	 * false otherwise.
	 * An anonymous class is a local inner class with no declared name.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 */
	public boolean isAnonymous()
		throws org.eclipse.jdt.internal.core.builder.NotPresentException {
		return BinaryStructure.isAnonymous(getBinaryType());
	}

	/**
	 * Return true if this represents a binary class or interface, false otherwise.
	 * A binary type is one which is in .class file format in the source tree.
	 * Returns false if this represents a primitive type or an array type.
	 */
	public boolean isBinary() throws NotPresentException {
		return getTypeStructureEntry().isBinary();
	}

	/**
	 * Determines if this object represents a class type.
	 * This returns false if this object represents an interface,
	 * an array type, or a primitive type.
	 */
	public boolean isClass() throws NotPresentException {
		return !isInterface();
	}

	/**
	 * Returns true if the type represented by this object is
	 * deprecated, false otherwise.  A deprecated object is one that
	 * has a deprecated tag in its doc comment.
	 * Returns false if this represents a primitive type or an array type.
	 */
	public boolean isDeprecated() throws NotPresentException {
		return (getBinaryType().getModifiers() & IConstants.AccDeprecated) != 0;
	}

	/**
	 * Returns true if this object represents an inner class or interface,
	 * false otherwise.
	 * An inner class is one which can only be created in the context of
	 * an instance of its outer class.	This does not include package member
	 * classes or other top-level classes.	Such a class cannot be static.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 */
	public boolean isInnerClass() throws NotPresentException {
		return !isTopLevel();
	}

	/**
	 * Determines if this object represents an interface type.
	 * This returns false if this object represents a class,
	 * an array type, or a primitive type.
	 */
	public boolean isInterface() throws NotPresentException {
		return getBinaryType().isInterface();
	}

	/**
	 * Returns true if this object represents a local inner class,
	 * false otherwise.
	 * A local inner class is an inner class which is defined in the body of
	 * a method or other block, not as a class field.
	 * <p>
	 * See the <em>Java Inner Classes Specification</em> for more details.
	 */
	public boolean isLocal() throws NotPresentException {
		return BinaryStructure.isLocal(getBinaryType());
	}

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
	 */
	public boolean isPackageMember() throws NotPresentException {
		return BinaryStructure.isPackageMember(getBinaryType());
	}

	/**
	 * A class or interface type is present if:
	 *	   - its package is present, and
	 *	   - the package declares a type of the same name.
	 */
	public boolean isPresent() {
		return fState.getTypeStructureEntry(fHandle, true) != null;
	}

	/**
	 * Returns true if the type represented by this object is
	 * synthetic, false otherwise.  A synthetic object is one that
	 * was invented by the compiler, but was not declared in the source.
	 * See <em>The Inner Classes Specification</em>.
	 * A synthetic object is not the same as a fictitious object.
	 */
	public boolean isSynthetic() throws NotPresentException {
		return (getBinaryType().getModifiers() & IConstants.AccSynthetic) != 0;
	}

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
	 */
	public boolean isTopLevel()
		throws org.eclipse.jdt.internal.core.builder.NotPresentException {
		IBinaryType binaryType = getBinaryType();
		if (BinaryStructure.isPackageMember(binaryType)) {
			return true;
		}
		if (BinaryStructure.isLocal(binaryType)) {
			return false;
		}
		char[] enclosingType = BinaryStructure.getEnclosingTypeName(binaryType);
		if (enclosingType == null) {
			return true;
		}
		if ((binaryType.getModifiers() & IConstants.AccStatic) == 0) {
			return false;
		}
		TypeStructureEntry tsEntry = getTypeStructureEntry();
		IType enclosing =
			(IType) fState.typeNameToHandle(tsEntry, new String(enclosingType)).inState(
				fState);
		return enclosing.isTopLevel();
	}

}
