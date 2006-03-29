/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class NumericTest extends AbstractRegressionTest {
	
public NumericTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/B.java",
		"package p;\n" + 
		"public class B {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int offset = -8;\n" + 
		"    int temp = 0 - offset;\n" + 
		"    offset = 0 - offset;  // This is the problem line\n" + 
		"    System.out.println(\"offset: \" + offset);\n" + 
		"    System.out.println(\"temp: \" + temp);\n" + 
		"    if (offset != temp ) {\n" + 
		"      System.err.println(\"offset (\" + offset + \") should be equal to temp (\" + temp + \").\");\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/Y.java",
		"package p;\n" + 
		"public class Y {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int clockend = 0;\n" + 
		"    clockend += 128;\n" + 
		"    if(clockend < 0) {\n" + 
		"      System.out.println(clockend);\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133738
public void test003() {
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"  int i1 = -2147483648;\n" + 
		"  int i2 = -(2147483648);\n" + 
		"}",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 3)\n" + 
	"	int i2 = -(2147483648);\n" + 
	"	          ^^^^^^^^^^^^\n" + 
	"The literal 2147483648 of type int is out of range \n" + 
	"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133738
public void test004() {
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"  long l1 = -9223372036854775808L;\n" + 
		"  long l2 = -(9223372036854775808L);\n" + 
		"}",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 3)\n" + 
	"	long l2 = -(9223372036854775808L);\n" + 
	"	           ^^^^^^^^^^^^^^^^^^^^^^\n" + 
	"The literal 9223372036854775808L of type long is out of range \n" + 
	"----------\n");
}
public static Class testClass() {
	return NumericTest.class;
}
}
