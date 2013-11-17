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
//	TESTS_NAMES = new String[] { "test007"};
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
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"goo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		final boolean x = true;\n" +
				"		goo(()-> goo((I)null));\n" +
				"	}\n" +
				"	int f() {\n" +
				"		final boolean x = true;\n" +
				"		while (x);\n" +
				"	}\n" +
				"}\n",
			},
			"goo(I)");
}
public void test003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   static final boolean f = true;\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		final boolean x = true;\n" +
				"		goo(()-> { \n" +
				"			final boolean y = true;\n" +
				"			while (y); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (x); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (f); \n" +
				"			});\n" +
				"	}\n" +
				"}\n",
			},
			"goo(J)\n" +
			"goo(J)\n" +
			"goo(J)");
}
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   static boolean f = true;\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean x = true;\n" +
				"		goo(()-> { \n" +
				"			boolean y = true;\n" +
				"			while (y); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (x); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (f); \n" +
				"			});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	goo(()-> { \n" + 
			"	^^^\n" + 
			"The method goo(J) in the type X is not applicable for the arguments (() -> {\n" + 
			"  boolean y = true;\n" + 
			"  while (y)    ;\n" + 
			"})\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 15)\n" + 
			"	goo(()-> { \n" + 
			"	^^^\n" + 
			"The method goo(J) in the type X is not applicable for the arguments (() -> {\n" + 
			"  while (x)    ;\n" + 
			"})\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 18)\n" + 
			"	goo(()-> { \n" + 
			"	^^^\n" + 
			"The method goo(J) in the type X is not applicable for the arguments (() -> {\n" + 
			"  while (f)    ;\n" + 
			"})\n" + 
			"----------\n");
}
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   final boolean f = true;\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		final boolean x = true;\n" +
				"		goo(()-> { \n" +
				"			final boolean y = true;\n" +
				"			while (y); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (x); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (f); \n" +
				"			});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 19)\n" + 
			"	while (f); \n" + 
			"	       ^\n" + 
			"Cannot make a static reference to the non-static field f\n" + 
			"----------\n");
}
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" public static interface StringToInt {\n" +
				"  	int stoi(String s);\n" +
				" }\n" +
				" public static interface ReduceInt {\n" +
				"     int reduce(int a, int b);\n" +
				" }\n" +
				" void foo(StringToInt s) { }\n" +
				" void bar(ReduceInt r) { }\n" +
				" void bar() {\n" +
				"     bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK\n" +
				"     foo(s -> s.length());\n" +
				"     foo((s) -> s.length());\n" +
				"     foo((String s) -> s.length()); //SingleVariableDeclaration is OK\n" +
				"     bar((x, y) -> x+y);\n" +
				" }\n" +
				"}\n",
			},
			"");
}
public void test007() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		goo(()-> 10); \n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	goo(()-> 10); \n" + 
			"	         ^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n");
}
}