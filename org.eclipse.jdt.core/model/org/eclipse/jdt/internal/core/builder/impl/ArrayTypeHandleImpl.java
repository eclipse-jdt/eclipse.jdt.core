package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public class ArrayTypeHandleImpl extends TypeImpl {
	TypeImpl fElementType;
	int fNestingDepth;
	/**
	 * Creates a new ArrayTypeHandleImpl
	 * @param name name of the type
	 * @param depth depth of array nesting
	 */
	ArrayTypeHandleImpl(TypeImpl type, int depth) {
		fNestingDepth = depth;
		fElementType = type;
	}
	/**
	 * Appends the signature for this type to the StringBuffer.
	 * If includeUnnamed is true, then the identifiers for unnamed packages
	 * are included, preceded by '$'.  Otherwise, they are excluded.
	 */
	void appendSignature(StringBuffer sb, boolean includeUnnamed) {

		for (int i = fNestingDepth; --i >= 0;) {
			sb.append('[');
		}
		fElementType.appendSignature(sb, includeUnnamed);
	}
	/**
	 * Appends the VM signature of the type to the StringBuffer.  
	 */
	void appendVMSignature(StringBuffer sb) {
		for (int i = getNestingDepth(); --i >= 0;) {
			sb.append('[');
		}
		getElementType().appendVMSignature(sb);
	}
	/**
	 * Compares this object against the specified object.
	 *	Returns true if the objects are the same.
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ArrayTypeHandleImpl)) return false;

		ArrayTypeHandleImpl array = (ArrayTypeHandleImpl) o;
		return fElementType.equals(array.fElementType) &&
			fNestingDepth == array.fNestingDepth;
	}
/**
 * Returns a Type object representing an array type with
 * the type represented by this object as its component type.
 * This is a handle-only method.
 */
public org.eclipse.jdt.internal.core.builder.IType getArrayHandle() {
	return new ArrayTypeHandleImpl(fElementType, fNestingDepth + 1);
}
/**
 * If this class represents an array type, returns the Type
 * object representing the component type of the array; otherwise
 * returns null. The component type of an array may itself be
 * an array type.
 * This is a handle-only method.
 */
public org.eclipse.jdt.internal.core.builder.IType getComponentType() {
	if (fNestingDepth == 1)
		return fElementType;
	return new ArrayTypeHandleImpl(fElementType, fNestingDepth - 1);
}
/**
 * If this is an array type, answer its element type (the leaf non-array type),
 * otherwise answer this type.
 */

TypeImpl getElementType() {
	return fElementType;
}
	public JavaDevelopmentContextImpl getInternalDC() {
		return fElementType.getInternalDC();
	}
/**
 * Returns the fully-qualified name of the type (class, interface,
 * array, or primitive) represented by this object, as a String.
 * For classes and interfaces, the name is the VM class name, 
 * including the package name.
 * For inner classes, the name is as described in the 
 * <em>Inner Classes Specification</em>.
 * For array types, the name is the name of the component type, followed by '[]'.
 * For primitive types, the name is the keyword for the primitive type.
 * This is a handle-only method.
 */
public String getName() {
	String name = fElementType.getName();
	for (int i = 0; i < fNestingDepth; i++)
		name += "[]"/*nonNLS*/;
	return name;
}
/**
 * Return the array nesting depth
 */
int getNestingDepth() {
	return fNestingDepth;
}
/**
 * getSimpleName method comment.
 */
public String getSimpleName() {
	String simpleName = fElementType.getSimpleName();
	for (int i = 0; i < fNestingDepth; i++)
		simpleName += "[]"/*nonNLS*/;
	return simpleName;
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
	return getInternalDC().getRootClassHandle();
}
	/**
	 * Returns a consistent hash code for this object
	 */
	public int hashCode() {
		return fElementType.hashCode() + (fNestingDepth * 131);
	}
	/**
	 * Returns a state specific version of this handle in the given state.
	 */
	public IHandle inState(IState s) throws org.eclipse.jdt.internal.core.builder.StateSpecificException {
		
		return new ArrayTypeHandleImplSWH((StateImpl) s, this);
	}
/**
 * If this Type object represents an array type, returns true,
 * otherwise returns false.
 * This is a handle-only method.
 */
public boolean isArray() {
	return true;
}
/**
 * Set the array nesting depth
 */
void setNestingDepth(int depth) {
	fNestingDepth = depth;
}
}
