/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class ShortConstant extends Constant {
	short value;
public ShortConstant(short value) {
	this.value = value;
}
public byte byteValue() {
	return (byte) value;
}
public char charValue() {
	return (char) value;
}
public double doubleValue() {
	return value;
}
public float floatValue() {
	return value;
}
public int intValue() {
	return value;
}
public long longValue() {
	return value;
}
public short shortValue() {
	return value;
}
public String stringValue() {
	//spec 15.17.11
	
	String s = new Integer(value).toString() ;
	if (s == null)
		return "null"; //$NON-NLS-1$
	else
		return s;
}
public String toString(){

	return "(short)" + value ; } //$NON-NLS-1$
public int typeID() {
	return T_short;
}
}
