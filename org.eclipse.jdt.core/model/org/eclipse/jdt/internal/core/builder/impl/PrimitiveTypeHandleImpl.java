package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;

import java.util.Hashtable;

public class PrimitiveTypeHandleImpl extends TypeImpl implements IType {
	JavaDevelopmentContextImpl fDevelopmentContext;
	int fTypeCode;

	/*
	 * Type codes
	 */
	static final int TC_BOOLEAN = 0;
	static final int TC_BYTE = 1;
	static final int TC_CHAR = 2;
	static final int TC_SHORT = 3;
	static final int TC_INT = 4;
	static final int TC_LONG = 5;
	static final int TC_FLOAT = 6;
	static final int TC_DOUBLE = 7;
	static final int TC_ARRAY = 9;
	static final int TC_CLASS = 10;
	static final int TC_VOID = 11;
	/**
	 * Creates a new primitive type with the given type code
	 */
	PrimitiveTypeHandleImpl(JavaDevelopmentContextImpl dc, char typeCode) {
		fDevelopmentContext = dc;
		switch (typeCode) {
			case 'I' :
				fTypeCode = TC_INT;
				break;
			case 'F' :
				fTypeCode = TC_FLOAT;
				break;
			case 'V' :
				fTypeCode = TC_VOID;
				break;
			case 'Z' :
				fTypeCode = TC_BOOLEAN;
				break;
			case 'B' :
				fTypeCode = TC_BYTE;
				break;
			case 'S' :
				fTypeCode = TC_SHORT;
				break;
			case 'C' :
				fTypeCode = TC_CHAR;
				break;
			case 'J' :
				fTypeCode = TC_LONG;
				break;
			case 'D' :
				fTypeCode = TC_DOUBLE;
				break;
		}
	}

	/**
	 * Appends the signature for this type to the StringBuffer
	 * If includeUnnamed is true, then the identifiers for unnamed packages
	 * are included, preceded by '$'.  Otherwise, they are excluded.
	 */
	void appendSignature(StringBuffer sb, boolean includeUnnamed) {
		char sig;
		switch (fTypeCode) {
			case TC_BOOLEAN :
				sig = 'Z';
				break;
			case TC_BYTE :
				sig = 'B';
				break;
			case TC_CHAR :
				sig = 'C';
				break;
			case TC_DOUBLE :
				sig = 'D';
				break;
			case TC_FLOAT :
				sig = 'F';
				break;
			case TC_INT :
				sig = 'I';
				break;
			case TC_LONG :
				sig = 'J';
				break;
			case TC_SHORT :
				sig = 'S';
				break;
			case TC_VOID :
				sig = 'V';
				break;
			default :
				Assert.isTrue(false, "invalid type code");
				sig = ' ';
		}
		sb.append(sig);
	}

	/**
	 * Appends the VM signature of the type to the StringBuffer.  
	 */
	void appendVMSignature(StringBuffer sb) {
		sb.append(typeSignature(fTypeCode));
	}

	/**
	 * Compares this object against the specified object.
	 *	Returns true if the objects are the same.
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PrimitiveTypeHandleImpl))
			return false;

		PrimitiveTypeHandleImpl prim = (PrimitiveTypeHandleImpl) o;
		return fTypeCode == prim.fTypeCode
			&& fDevelopmentContext.equals(prim.fDevelopmentContext);
	}

	public JavaDevelopmentContextImpl getInternalDC() {
		return fDevelopmentContext;
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
		return getSimpleName();
	}

	/**
	 * Returns the simple name of the type (class, interface, array, 
	 * or primitive) represented by this object, as a String.
	 * For classes and interfaces, this is the VM class name, 
	 * excluding the package name.
	 * For array types, this is the simple name of the component type, followed by '[]'.
	 * For primitive types, this is the keyword for the primitive type.
	 * This is a handle-only method.
	 */
	public String getSimpleName() {
		switch (fTypeCode) {
			case TC_BOOLEAN :
				return "boolean";
			case TC_BYTE :
				return "byte";
			case TC_CHAR :
				return "char";
			case TC_DOUBLE :
				return "double";
			case TC_FLOAT :
				return "float";
			case TC_INT :
				return "int";
			case TC_LONG :
				return "long";
			case TC_SHORT :
				return "short";
			case TC_VOID :
				return "void";
			default :
				Assert.isTrue(false, "invalid type code");
		}
		return null;
	}

	int getTypeCode() {
		return fTypeCode;
	}

	/**
	 * Returns the VM signature of the type.  
	 */
	String getVMSignature() {
		return typeSignature(fTypeCode);
	}

	/**
	 * Returns a consistent hash code for this object
	 */
	public int hashCode() {
		return fTypeCode;
	}

	/**
	 * Returns a state specific version of this handle in the given state.
	 */
	public IHandle inState(IState s)
		throws org.eclipse.jdt.internal.core.builder.StateSpecificException {

		return new PrimitiveTypeHandleImplSWH((StateImpl) s, this);
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

	/**
	 * Returns a string representation of the package.  For debugging purposes
	 * only (NON-NLS).
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Convert a base type code to the VM naming format
	 */
	protected static String typeSignature(int typeCode) {
		switch (typeCode) {
			case TC_VOID :
				return "V";
			case TC_BOOLEAN :
				return "Z";
			case TC_BYTE :
				return "B";
			case TC_CHAR :
				return "C";
			case TC_SHORT :
				return "S";
			case TC_INT :
				return "I";
			case TC_LONG :
				return "J";
			case TC_FLOAT :
				return "F";
			case TC_DOUBLE :
				return "D";
			default :
				return "unknown";
		}
	}

}
