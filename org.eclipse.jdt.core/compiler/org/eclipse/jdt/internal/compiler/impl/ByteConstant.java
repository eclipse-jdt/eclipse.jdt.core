package org.eclipse.jdt.internal.compiler.impl;

public class ByteConstant extends Constant {
	byte value;
public ByteConstant(byte value) {
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
	
	String s = new Integer(value).toString() ;
	if (s == null)
		return "null";
	else
		return s;
}
public String toString(){

	return "(byte)" + value ; }
public int typeID() {
	return T_byte;
}
}
