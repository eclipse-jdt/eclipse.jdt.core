package org.eclipse.jdt.internal.compiler.impl;

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
		return "null"/*nonNLS*/;
	else
		return s;
}
public String toString(){

	if (this == NotAConstant) return "(Constant) NotAConstant"/*nonNLS*/ ;
	return "(double)"/*nonNLS*/ + value ; }
public int typeID() {
	return T_double;
}
}
