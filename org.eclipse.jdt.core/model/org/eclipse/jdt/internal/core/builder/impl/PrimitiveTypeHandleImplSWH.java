package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public class PrimitiveTypeHandleImplSWH extends TypeImplSWH {
	PrimitiveTypeHandleImpl fHandle;
	/**
	 * Creates a new method handle in the given state
	 */
	PrimitiveTypeHandleImplSWH(StateImpl state, IType handle) {
		fState = state;
		try {
			fHandle = (PrimitiveTypeHandleImpl)handle;
		} catch (ClassCastException e) {
			throw new StateSpecificException();
		}
	}
	/**
	  * Returns the non state specific handle
	  */
	protected TypeImpl getHandle() {
		return fHandle;
	}
	/**
	 * Primitive types are always present.
	 */
	public boolean isPresent() {
		return true;
	}
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
	 */
	public boolean isPrimitive() {
		return true;
	}
}
