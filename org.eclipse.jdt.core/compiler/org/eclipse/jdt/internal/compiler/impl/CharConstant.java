package org.eclipse.jdt.internal.compiler.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class CharConstant extends Constant {
	char value;
public CharConstant(char value) {
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
	
	String s = new Character(value).toString() ;
	if (s == null)
		return "null"/*nonNLS*/;
	else
		return s;
}
public String toString(){

	return "(char)"/*nonNLS*/ + value ; }
public int typeID() {
	return T_char;
}
}
