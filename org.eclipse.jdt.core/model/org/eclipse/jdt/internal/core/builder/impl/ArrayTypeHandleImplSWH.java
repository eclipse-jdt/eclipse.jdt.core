package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public class ArrayTypeHandleImplSWH extends TypeImplSWH {
	ArrayTypeHandleImpl fHandle;
	/**
	 * Creates a new method handle in the given state
	 */
	public ArrayTypeHandleImplSWH(StateImpl state, ArrayTypeHandleImpl handle) {
		fState = state;
		fHandle = handle;
	}
/**
 * If this class represents an array type, returns the Type
 * object representing the component type of the array; otherwise
 * returns null. The component type of an array may itself be
 * an array type.
 * This is a handle-only method.
 */
public IType getComponentType() {
	return (IType) fHandle.getComponentType().inState(fState);
}
/**
 * If this is an array type, answer its element type (the leaf non-array type),
 * otherwise answer this type.
 */

TypeImplSWH getElementType() {
	return (TypeImplSWH) fHandle.getElementType().inState(fState);
}
	/**
	  * Returns the non state specific handle
	  */
	 protected TypeImpl getHandle() {
		 return fHandle;
	 }
/**
 * Return the array nesting depth
 */
int getNestingDepth() {
	return fHandle.fNestingDepth;
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
	return (IType) ((JavaDevelopmentContextImpl)getDevelopmentContext()).getRootClassHandle().inState(fState);
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
 * If this Type object represents an array type, returns true,
 * otherwise returns false.
 * This is a handle-only method.
 */
public boolean isPresent() {
	return getElementType().isPresent();
}
}
