/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
public class OverloadResolutionTest8 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testSuperReference03"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public OverloadResolutionTest8(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public static Class testClass() {
	return OverloadResolutionTest8.class;
}

public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo(int [] a);\n" +
				"}\n" +
				"interface J  {\n" +
				"	int foo(int a);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo((a)->a.length));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	System.out.println(foo((a)->a.length));\n" + 
			"	                   ^^^\n" + 
			"The method foo(I) is ambiguous for the type X\n" + 
			"----------\n"
			);
}
}