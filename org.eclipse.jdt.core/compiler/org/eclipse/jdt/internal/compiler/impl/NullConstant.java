package org.eclipse.jdt.internal.compiler.impl;

public class NullConstant extends Constant {
	public static final NullConstant Default = new NullConstant();

	final static String NullString = new StringBuffer(4).append((String)null).toString();
private NullConstant() {
}
public String stringValue() {
	
	return NullString;
}
public String toString(){

	return "(null)"/*nonNLS*/ + null ; }
public int typeID() {
	return T_null;
}
}
