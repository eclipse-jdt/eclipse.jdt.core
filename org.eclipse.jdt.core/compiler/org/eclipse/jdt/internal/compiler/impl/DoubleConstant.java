package org.eclipse.jdt.internal.compiler.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class DoubleConstant extends Constant {
	double value;
public DoubleConstant(double value) {
	this.value = value;
}
public byte byteValue() {
	return (byte) value;
}
public char charValue() {
	return (char) value;
}
public double doubleValue() {
	return (double) value;
}
public float floatValue() {
	return (float) value;
}
public int intValue() {
	return (int) value;
}
public long longValue() {
	return (long) value;
}
public short shortValue() {
	return (short) value;
}
public String stringValue() {
	//spec 15.17.11
	
	String s = new Double(value).toString() ;
	if (s == null)
		return "null"; //$NON-NLS-1$
	else
		return s;
}
public String toString(){

	if (this == NotAConstant) return "(Constant) NotAConstant" ; //$NON-NLS-1$
	return "(double)" + value ; } //$NON-NLS-1$
public int typeID() {
	return T_double;
}
}
