package org.eclipse.jdt.internal.compiler.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class NullConstant extends Constant {
	public static final NullConstant Default = new NullConstant();

	final static String NullString = new StringBuffer(4).append((String)null).toString();
private NullConstant() {
}
public String stringValue() {
	
	return NullString;
}
public String toString(){

	return "(null)" + null ; } //$NON-NLS-1$
public int typeID() {
	return T_null;
}
}
