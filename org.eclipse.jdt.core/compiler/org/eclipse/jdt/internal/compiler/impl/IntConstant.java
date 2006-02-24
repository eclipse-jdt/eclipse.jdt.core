/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class IntConstant extends Constant {
	
	int value;
	
	public static final IntConstant MINUS_FOUR = new IntConstant(-4);
	public static final IntConstant MINUS_THREE = new IntConstant(-3);
	public static final IntConstant MINUS_TWO = new IntConstant(-2);
	public static final IntConstant MINUS_ONE = new IntConstant(-1);
	public static final IntConstant ZERO = new IntConstant(0);
	public static final IntConstant ONE = new IntConstant(1);
	public static final IntConstant TWO = new IntConstant(2);
	public static final IntConstant THREE = new IntConstant(3);
	public static final IntConstant FOUR = new IntConstant(4);
	public static final IntConstant FIVE = new IntConstant(5);
	public static final IntConstant SIX = new IntConstant(6);
	public static final IntConstant SEVEN = new IntConstant(7);
	public static final IntConstant EIGHT= new IntConstant(8);
	public static final IntConstant NINE = new IntConstant(9);
	public static final IntConstant TEN = new IntConstant(10);
	
	public IntConstant(int value) {
		this.value = value;
	}
	
	public byte byteValue() {
		return (byte) value;
	}
	
	public char charValue() {
		return (char) value;
	}
	
	public double doubleValue() {
		return value; // implicit cast to return type
	}
	
	public float floatValue() {
		return value; // implicit cast to return type
	}
	
	public int intValue() {
		return value;
	}
	
	public long longValue() {
		return value; // implicit cast to return type
	}
	
	public short shortValue() {
		return (short) value;
	}
	
	public String stringValue() {
		//spec 15.17.11
		return String.valueOf(this.value);
	}

	public String toString() {
		return "(int)" + value; //$NON-NLS-1$
	} 

	public int typeID() {
		return T_int;
	}
}
