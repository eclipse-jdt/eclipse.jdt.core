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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class ProblemConstructorTest extends AbstractRegressionTest {

public ProblemConstructorTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return ProblemConstructorTest.class;
}

public void test001() {
	this.runNegativeTest(
		new String[] {
			"prs/Test1.java",
			"package prs;	\n" +
			"import java.io.IOException;	\n" +
			"public class Test1 {	\n" +
			"String s = 3;	\n" +
			"Test1() throws IOException {	\n" +
			"}	\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in prs\\Test1.java (at line 4)\n" + 
		"	String s = 3;	\n" + 
		"	       ^\n" + 
		"Type mismatch: cannot convert from int to String\n" + 
		"----------\n",
		null,
		true,
		null,
		true,
		false,
		false);

	this.runNegativeTest(
		new String[] {
			"prs/Test2.java",
			"package prs;	\n" +
			"import java.io.IOException;	\n" +
			"public class Test2 {	\n" +
			"public void foo() {	\n" +
			"try {	\n" +
			"Test1 t = new Test1();	\n" +
			"System.out.println();	\n" +
			"} catch(IOException e)	\n" +
			"{	\n" +
			"e.printStackTrace();	\n" +
			"}	\n" +
			"}	\n" +
			"}"
		},
		"",
		null,
		false);
}
// 49843
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public X();\n" + 
			"    public Y();\n" + 
			"    \n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public X();\n" + 
		"	       ^^^\n" + 
		"This method requires a body instead of a semicolon\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	public Y();\n" + 
		"	       ^^^\n" + 
		"Return type for the method is missing\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	public Y();\n" + 
		"	       ^^^\n" + 
		"This method requires a body instead of a semicolon\n" + 
		"----------\n");
}
}
