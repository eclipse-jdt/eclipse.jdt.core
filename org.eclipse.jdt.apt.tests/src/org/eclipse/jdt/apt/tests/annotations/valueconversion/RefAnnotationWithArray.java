/*******************************************************************************
 * Copyright (c) 2005, 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.valueconversion;

public @interface RefAnnotationWithArray {
	boolean[] booleans(); 
	byte[] bytes();
	short[] shorts(); 
	int[] ints();
	long[] longs();
	float[] floats();
	double[] doubles();
	char[] chars();
	String str() default "string";
}
