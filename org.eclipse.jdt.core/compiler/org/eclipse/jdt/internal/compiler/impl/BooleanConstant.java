package org.eclipse.jdt.internal.compiler.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class BooleanConstant extends Constant {
	boolean value;

	
public BooleanConstant(boolean value) {
	this.value = value;
}
public boolean booleanValue() {
	return (boolean) value;
}
public String stringValue() {
	//spec 15.17.11
	
	String s = new Boolean(value).toString() ;
	if (s == null)
		return "null"/*nonNLS*/;
	else
		return s;
}
public String toString(){

	return "(boolean)"/*nonNLS*/ + value ; }
public int typeID() {
	return T_boolean;
}
}
