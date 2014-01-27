/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

public class ConditionalExpressionTest extends AbstractRegressionTest {

	public ConditionalExpressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test003" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ConditionalExpressionTest.class;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100162
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    final boolean isA = true;\n" +
				"    public static void main(String[] args) {\n" +
				"        X x = new X();\n" +
				"        System.out.print(x.isA ? \"SUCCESS\" : \"FAILURE\");\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107193
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class RecipeElement {\n" +
				"    public static final RecipeElement[] NO_CHILDREN= new RecipeElement[0]; \n" +
				"}\n" +
				"class Ingredient extends RecipeElement { }\n" +
				"class X extends RecipeElement {\n" +
				"    private Ingredient[] fIngredients;\n" +
				"    public RecipeElement[] getChildren() {\n" +
				"        return fIngredients == null ? NO_CHILDREN : fIngredients;\n" +
				"    }\n" +
				"}",
			},
			""
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426078, Bug 426078 - [1.8] VerifyError when conditional expression passed as an argument
	public void test003() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	boolean isOdd(boolean what) {\n" +
				"		return square(what ? new Integer(1) : new Integer(2)) % 2 == 1; // trouble here\n" +
				"	}\n" +
				"	<T> int square(int i) {\n" +
				"		return i * i;\n" +
				"	}\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(new X().isOdd(true));\n" +
				"	}\n" +
				"}\n",
			},
			"true"
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423685, - [1.8] poly conditional expression must not use lub
	public void test004() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		if (this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"class A{/**/}\n" +
						"class B extends A {/**/}\n" +
						"class G<T> {\n" +
						"	G<B> gb=null;\n" +
						"	G<? super A> gsa=null;\n" +
						"	G<? super B> l = (true)? gsa : gb;\n" +
						"}\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(\"OK\");\n" +
						"	}\n" +
						"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 6)\n" + 
					"	G<? super B> l = (true)? gsa : gb;\n" + 
					"	                 ^^^^^^^^^^^^^^^^\n" + 
					"Type mismatch: cannot convert from G<capture#2-of ? extends Object> to G<? super B>\n" + 
					"----------\n"
				);
		} else {
			this.runConformTest(
					new String[] {
							"X.java",
							"class A{/**/}\n" +
							"class B extends A {/**/}\n" +
							"class G<T> {\n" +
							"	G<B> gb=null;\n" +
							"	G<? super A> gsa=null;\n" +
							"	G<? super B> l = (true)? gsa : gb;\n" +
							"}\n" +
							"public class X {\n" +
							"	public static void main(String[] args) {\n" +
							"		System.out.println(\"OK\");\n" +
							"	}\n" +
							"}\n",
					},
					"OK"
					);
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425181, - Cast expression in ternary operation reported as incompatible
	public void test005() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		if (this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"public class X {\n" +
						"    public static void main(String args[]) {\n" +
						"    	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
						"       System.out.println(\"OK\");\n" +
						"    }\n" +
						"}\n" +
						"interface I<T> {}\n" +
						"interface J<T> extends I<T> {}\n",
					},
					"----------\n" + 
					"1. WARNING in X.java (at line 3)\n" + 
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" + 
					"	          ^\n" + 
					"J is a raw type. References to generic type J<T> should be parameterized\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" + 
					"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
					"Type mismatch: cannot convert from I<capture#1-of ? extends I> to I<? super J>\n" + 
					"----------\n" + 
					"3. WARNING in X.java (at line 3)\n" + 
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" + 
					"	                           ^\n" + 
					"I is a raw type. References to generic type I<T> should be parameterized\n" + 
					"----------\n" + 
					"4. WARNING in X.java (at line 3)\n" + 
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" + 
					"	                                         ^\n" + 
					"J is a raw type. References to generic type J<T> should be parameterized\n" + 
					"----------\n"
				);
		} else {
			this.runConformTest(
					new String[] {
					"X.java",
					"public class X {\n" +
					"    public static void main(String args[]) {\n" +
					"    	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
					"       System.out.println(\"OK\");\n" +
					"    }\n" +
					"}\n" +
					"interface I<T> {}\n" +
					"interface J<T> extends I<T> {}\n",
					},
					"OK"
					);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426315, - [1.8][compiler] UnsupportedOperationException with conditional expression
	public void test006() {
		this.runConformTest(
				new String[] {
					"X.java",
						"public class X {\n" +
						"	static int foo(Object x) {\n" +
						"		return 0;\n" +
						"	}\n" +
						"	static int foo(int e) { \n" +
						"		return 1; \n" +
						"	}\n" +
						" 	public static void main(String args[]) {\n" +
						" 		Object x = new Object();\n" +
						"		System.out.println(foo(true ? x : new int[0]) != 0);\n" +
						"	}\n" +
						"}\n",
				},
				"false"
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426680, - [1.8][compiler] Incorrect handling of poly conditional leads to CCE
	public void test007() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
				new String[] {
						"X.java",
						"interface BinaryOperation<T> {\n" +
						"    T operate(T x, T y);\n" +
						"}\n" +
						"class StringCatenation implements BinaryOperation<String> { \n" +
						"    public String operate(String x, String y) { return x + y; }\n" +
						"}\n" +
						"public class X {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	foo(false ? (a,b)->a+b :new StringCatenation());\n" +
						"    }\n" +
						"    static void foo(BinaryOperation<Integer> x) {\n" +
						"       x.operate(5, 15);\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	foo(false ? (a,b)->a+b :new StringCatenation());\n" + 
				"	                        ^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type mismatch: cannot convert from StringCatenation to BinaryOperation<Integer>\n" + 
				"----------\n"
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426680, - [1.8][compiler] Incorrect handling of poly conditional leads to CCE
	public void test008() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
				new String[] {
						"X.java",
						"interface BinaryOperation<T> {\n" +
						"    T operate(T x, T y);\n" +
						"}\n" +
						"class StringCatenation implements BinaryOperation<String> { \n" +
						"    public String operate(String x, String y) { return x + y; }\n" +
						"}\n" +
						"public class X {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	foo(false ? new StringCatenation() : (a,b)->a+b);\n" +
						"    }\n" +
						"    static void foo(BinaryOperation<Integer> x) {\n" +
						"       x.operate(5, 15);\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	foo(false ? new StringCatenation() : (a,b)->a+b);\n" + 
				"	            ^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type mismatch: cannot convert from StringCatenation to BinaryOperation<Integer>\n" + 
				"----------\n"
				);
	}
}
