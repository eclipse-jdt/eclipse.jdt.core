package org.eclipse.jdt.internal.compiler.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class StringConstant extends Constant {
	public String value;
    
public StringConstant(String value) {
	this.value = value ;
}
public boolean compileTimeEqual(StringConstant right){
	//String are intermed in th compiler==>thus if two string constant
	//get to be compared, it is an equal on the vale which is done

	return true ;}
public String stringValue() {
	//spec 15.17.11

	//the next line do not go into the toString() send....!
	return value ;

	/*
	String s = value.toString() ;
	if (s == null)
		return "null";
	else
		return s;
	*/
	
}
public String toString(){

	return "(String)\"" + value +"\""; } //$NON-NLS-2$ //$NON-NLS-1$
public int typeID() {
	return T_String;
}
}
