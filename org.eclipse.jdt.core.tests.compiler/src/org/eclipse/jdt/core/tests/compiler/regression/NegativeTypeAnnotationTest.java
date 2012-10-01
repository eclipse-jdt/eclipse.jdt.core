/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
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

public class NegativeTypeAnnotationTest extends AbstractRegressionTest {

	static { 
//		TESTS_NUMBERS = new int [] { 35 };
	}
	public static Class testClass() {
		return NegativeTypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public NegativeTypeAnnotationTest(String testName){
		super(testName);
	}
	public void test001() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker2 Object {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker2 Object {}\n" + 
				"	                        ^^^^^^^\n" + 
				"Marker2 cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test002() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.Serializable;\n" +
					"public class X implements @Marker2 Serializable {\n" +
					"	private static final long serialVersionUID = 1L;\n" +
					"}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public class X implements @Marker2 Serializable {\n" + 
				"	                           ^^^^^^^\n" + 
				"Marker2 cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test003() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker Object {}\n" + 
				"	                        ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test004() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X<@Marker T> {}\n" + 
				"	                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test005() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X<@Marker T> {}\n" + 
				"	                ^^^^^^\n" + 
				"Marker cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test006() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class Y {}\n",
				"X.java",
				"public class X extends @A(id=\"Hello, World!\") @B @C('(') Y {\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X extends @A(id=\"Hello, World!\") @B @C(\'(\') Y {\n" + 
		"	                        ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X extends @A(id=\"Hello, World!\") @B @C(\'(\') Y {\n" + 
		"	                                               ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	public class X extends @A(id=\"Hello, World!\") @B @C(\'(\') Y {\n" + 
		"	                                                  ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n");
	}
	public void test007() throws Exception {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X implements @A(id=\"Hello, World!\") I, @B @C(\'(\') J {}\n" + 
		"	                           ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X implements @A(id=\"Hello, World!\") I, @B @C(\'(\') J {}\n" + 
		"	                                                     ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	public class X implements @A(id=\"Hello, World!\") I, @B @C(\'(\') J {}\n" + 
		"	                                                        ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n");
	}
	public void test010() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class Y<T> {}\n",
				"X.java",
				"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String> {\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String> {\n" + 
		"	                        ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String> {\n" + 
		"	                                              ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String> {\n" + 
		"	                                                 ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n");
	}
	public void test011() throws Exception {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I<T> {}\n",
				"J.java",
				"interface J<T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C('(') Integer> {}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C(\'(\') Integer> {}\n" + 
		"	                             ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C(\'(\') Integer> {}\n" + 
		"	                                                           ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C(\'(\') Integer> {}\n" + 
		"	                                                                ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n");
	}
	// throws
	public void test012() throws Exception {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"class E extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"E1.java",
				"class E1 extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"E2.java",
				"class E2 extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"X.java",
				"public class X {\n" +
				"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C('(') E2 {}\n" +
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C(\'(\') E2 {}\n" + 
		"	                   ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C(\'(\') E2 {}\n" + 
		"	                                              ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C(\'(\') E2 {}\n" + 
		"	                                                 ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n");
	}
	// method receiver
	public void test013() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(@B(3) X this) {}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(@B(3) X this) {}\n" + 
		"	          ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// method return type
	public void test014() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@B(3) int foo() {\n" +
				"		return 1;\n" +
				"	}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	@B(3) int foo() {\n" + 
		"	 ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// field type
	public void test015() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@B(3) int field;\n" +
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	@B(3) int field;\n" + 
		"	 ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// method parameter
	public void test016() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int foo(@B(3) String s) {\n" +
				"		return s.length();\n" +
				"	}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	int foo(@B(3) String s) {\n" + 
		"	         ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// method parameter generic or array
	public void test017() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int foo(String @B(3) [] s) {\n" +
				"		return s.length;\n" +
				"	}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	int foo(String @B(3) [] s) {\n" + 
		"	                ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// field type generic or array
	public void test018() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int @B(3) [] field;\n" +
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	int @B(3) [] field;\n" + 
		"	     ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// class type parameter
	public void test019() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<@A @B(3) T> {}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X<@A @B(3) T> {}\n" + 
		"	                ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X<@A @B(3) T> {}\n" + 
		"	                   ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// method type parameter
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<@A @B(3) T> void foo(T t) {}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	<@A @B(3) T> void foo(T t) {}\n" + 
		"	  ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	<@A @B(3) T> void foo(T t) {}\n" + 
		"	     ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// class type parameter bound
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X<T extends @A Z & @B(3) Cloneable> {}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X<T extends @A Z & @B(3) Cloneable> {}\n" + 
		"	                          ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X<T extends @A Z & @B(3) Cloneable> {}\n" + 
		"	                                 ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// class type parameter bound generic or array
	public void test022() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" + 
		"	                            ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" + 
		"	                                      ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 1)\n" + 
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" + 
		"	                                            ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 1)\n" + 
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" + 
		"	                                                    ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// method type parameter bound
	public void test023() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X {\n" +
				"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" + 
		"	            ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" + 
		"	                   ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// class type parameter bound generic or array
	public void test024() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X {\n" +
				"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" + 
		"	              ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" + 
		"	                   ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" + 
		"	                         ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 2)\n" + 
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" + 
		"	                                 ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// local variable + generic or array
	public void test025() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(String s) {\n" + 
				"		@C int i;\n" + 
				"		@A String [] @B(3)[] tab = new String[][] {};\n" + 
				"		if (tab != null) {\n" + 
				"			i = 0;\n" + 
				"			System.out.println(i + tab.length);\n" + 
				"		} else {\n" + 
				"			System.out.println(tab.length);\n" + 
				"		}\n" + 
				"		i = 4;\n" + 
				"		System.out.println(-i + tab.length);\n" + 
				"	}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	@C int i;\n" + 
		"	 ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	@A String [] @B(3)[] tab = new String[][] {};\n" + 
		"	 ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	@A String [] @B(3)[] tab = new String[][] {};\n" + 
		"	              ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// type argument constructor call
	public void test026() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T> X(T t) {\n" + 
				"	}\n" + 
				"	public Object foo() {\n" + 
				"		X x = new <@A @B(1) String>X(null);\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	X x = new <@A @B(1) String>X(null);\n" + 
		"	            ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	X x = new <@A @B(1) String>X(null);\n" + 
		"	               ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// type argument constructor call generic or array
	public void test027() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T> X(T t) {\n" + 
				"	}\n" + 
				"	public Object foo() {\n" + 
				"		X x = new <@A @B(1) String>X(null);\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	X x = new <@A @B(1) String>X(null);\n" + 
		"	            ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	X x = new <@A @B(1) String>X(null);\n" + 
		"	               ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n");
	}
	// type argument method call and generic or array
	public void test028() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	static <T, U> T foo(T t, U u) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	System.out.println(X.<@A @B(1) String[], @C(\'-\') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" + 
		"	                       ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	System.out.println(X.<@A @B(1) String[], @C(\'-\') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" + 
		"	                          ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	System.out.println(X.<@A @B(1) String[], @C(\'-\') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" + 
		"	                                          ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n");
	}
	public void test029() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker2 Object {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker2 Object {}\n" + 
				"	                        ^^^^^^^\n" + 
				"Marker2 cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test030() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.Serializable;\n" +
					"public class X implements @Marker2 Serializable {\n" +
					"	private static final long serialVersionUID = 1L;\n" +
					"}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public class X implements @Marker2 Serializable {\n" + 
				"	                           ^^^^^^^\n" + 
				"Marker2 cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test031() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"import java.lang.annotation.Target;\n" + 
					"import static java.lang.annotation.ElementType.*;\n" + 
					"@Target(TYPE_USE)\n" + 
					"@interface Marker {}",
					"X.java",
					"public class X<@Marker T> {}",
				},
				/* TODO(Srikanth/Jay) when JSR308 enabled runtime becomes available for testing, the first error message should be deleted. */
				"----------\n" + 
				"1. ERROR in Marker.java (at line 3)\n" + 
				"	@Target(TYPE_USE)\n" + 
				"	        ^^^^^^^^\n" + 
				"TYPE_USE cannot be resolved to a variable\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X<@Marker T> {}\n" + 
				"	               ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n");
	}
	public void test032() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"@interface Marker {}",
					"X.java",
					"public class X<@Marker T> {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X<@Marker T> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Only annotation types that explicitly specify TYPE_PARAMETER as a possible target element type can be applied here\n" + 
				"----------\n");
	}
	public void test033() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"@interface Marker {}",
					"Y.java",
					"public class Y {}",
					"X.java",
					"public class X extends @Marker Y {}",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X extends @Marker Y {}\n" + 
				"	                       ^^^^^^^\n" + 
				"Only annotation types that explicitly specify TYPE_USE as a possible target element type can be applied here\n" + 
				"----------\n");
	}
	// check locations
	public void test034() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"public class X {\n" + 
				"	@H String @E[] @F[] @G[] field;\n" + 
				"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
				"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"	 ^\n" + 
		"H cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"	           ^\n" + 
		"E cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"	                ^\n" + 
		"F cannot be resolved to a type\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"	                     ^\n" + 
		"G cannot be resolved to a type\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 5)\n" + 
		"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
		"	 ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 5)\n" + 
		"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
		"	        ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 5)\n" + 
		"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
		"	                   ^\n" + 
		"C cannot be resolved to a type\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 5)\n" + 
		"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
		"	                           ^\n" + 
		"D cannot be resolved to a type\n" + 
		"----------\n" + 
		"9. ERROR in X.java (at line 6)\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"	 ^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"10. ERROR in X.java (at line 6)\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"	        ^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"11. ERROR in X.java (at line 6)\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"	                   ^\n" + 
		"H cannot be resolved to a type\n" + 
		"----------\n" + 
		"12. ERROR in X.java (at line 6)\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"	                             ^\n" + 
		"E cannot be resolved to a type\n" + 
		"----------\n" + 
		"13. ERROR in X.java (at line 6)\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"	                                  ^\n" + 
		"F cannot be resolved to a type\n" + 
		"----------\n" + 
		"14. ERROR in X.java (at line 6)\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"	                                       ^\n" + 
		"G cannot be resolved to a type\n" + 
		"----------\n");
	}
	// check locations
	public void test035() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"public class X {\n" + 
				"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
				"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
		"	 ^\n" + 
		"H cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
		"	                     ^\n" + 
		"E cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
		"	                          ^\n" + 
		"F cannot be resolved to a type\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
		"	                               ^\n" + 
		"G cannot be resolved to a type\n" + 
		"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383884 -- Compiler tolerates illegal dimension annotation in class literal expressions
	public void test036() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
				"    System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
				"    System.out.println(int [] [] [] [] [].class);\n" +
				"    System.out.println(X [] [] [] [] [].class);\n" +
				"  }\n" +
				"}\n" +
				"@interface Empty {\n" +
				"}\n" +
				"@interface NonEmpty {\n" +
				"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" + 
		"	                       ^^^^^^^^^\n" + 
		"Syntax error, type annotations are illegal here\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" + 
		"	                                       ^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type annotations are illegal here\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" + 
		"	                                                              ^^^^^^^^^\n" + 
		"Syntax error, type annotations are illegal here\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" + 
		"	                     ^^^^^^^^^\n" + 
		"Syntax error, type annotations are illegal here\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 4)\n" + 
		"	System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" + 
		"	                                     ^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type annotations are illegal here\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 4)\n" + 
		"	System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" + 
		"	                                                            ^^^^^^^^^\n" + 
		"Syntax error, type annotations are illegal here\n" + 
		"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383950
	// [1.8][compiler] Type annotations must have target type meta annotation TYPE_USE
	public void test037() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"@interface Marker {}\n" +
					"@Marker	// line 2: Don't complain \n" +
					"public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" +
					"	public @Marker Object foo(@Marker Object obj) {  // 4: Don't complain on both\n" +
					"		return null;\n" +
					"	}\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" + 
				"	               ^^^^^^^\n" + 
				"Only annotation types that explicitly specify TYPE_PARAMETER as a possible target element type can be applied here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" + 
				"	                                   ^^^^^^^\n" + 
				"Only annotation types that explicitly specify TYPE_USE as a possible target element type can be applied here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383950
	// [1.8][compiler] Type annotations must have target type meta annotation TYPE_USE
	public void test038() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.Target;\n" + 
						"import static java.lang.annotation.ElementType.*;\n" + 
						"@Target({PACKAGE, TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER, LOCAL_VARIABLE})\n" + 
						"@interface Marker {}\n" +
						"public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" +
						"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 5)\n" + 
					"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" + 
					"	               ^^^^^^^\n" + 
					"The annotation @Marker is disallowed for this location\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 5)\n" + 
					"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" + 
					"	                                   ^^^^^^^\n" + 
					"The annotation @Marker is disallowed for this location\n" + 
					"----------\n");
	}
	// JSR 308: "It is not permitted to annotate the type name in an import statement."
	public void test039() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import @Marker java.lang.String; // Compilation error \n" +
						"public class X { \n" +
						"}\n" + 
						"@interface Marker {}\n"
					}, 
					"----------\n" + 
					"1. ERROR in X.java (at line 1)\n" + 
					"	import @Marker java.lang.String; // Compilation error \n" + 
					"	       ^^^^^^^\n" + 
					"Syntax error, type annotations are illegal here\n" + 
					"----------\n");
	}
	// Test that type name can't be left out in a cast expression with an annotations 
	public void test040() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X { \n" +
						"	public void foo(Object myObject) {\n" +
						"		String myString = (@NonNull) myObject;" +
						"	}\n" +
						"}\n" + 
						"@interface NonNull {}\n"
					}, 
					"----------\n" + 
					"1. ERROR in X.java (at line 3)\n" + 
					"	String myString = (@NonNull) myObject;	}\n" + 
					"	                   ^\n" + 
					"Syntax error on token \"@\", delete this token\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385111
	// [1.8][compiler] Compiler fails to flag undefined annotation type. 
	public void test0385111() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.util.ArrayList;\n" +
						"import java.util.List;\n" +
						"public class X {\n" +
						"    public void foo(String fileName) {\n" +
						"        List<String> l = new @MissingTypeNotIgnored ArrayList<String>();\n" +
						"        List<String> l1 = new @MissingTypeIgnored ArrayList<>();\n" +
						"    }\n" +
						"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 5)\n" + 
					"	List<String> l = new @MissingTypeNotIgnored ArrayList<String>();\n" + 
					"	                      ^^^^^^^^^^^^^^^^^^^^^\n" + 
					"MissingTypeNotIgnored cannot be resolved to a type\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 6)\n" + 
					"	List<String> l1 = new @MissingTypeIgnored ArrayList<>();\n" + 
					"	                       ^^^^^^^^^^^^^^^^^^\n" + 
					"MissingTypeIgnored cannot be resolved to a type\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385111
	// Test to exercise assorted cleanup along with bug fix. 
	public void test0385111a() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    public void foo(String fileName) {\n" +
						"        try (@Annot X x = null; @Annot X x2 = null) {\n"+
						"        } catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {\n" +
						"        }\n" +
						"    }\n" +
						"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 3)\n" + 
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" + 
					"	      ^^^^^\n" + 
					"Annot cannot be resolved to a type\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" + 
					"	            ^\n" + 
					"The resource type X does not implement java.lang.AutoCloseable\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 3)\n" + 
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" + 
					"	                         ^^^^^\n" + 
					"Annot cannot be resolved to a type\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 3)\n" + 
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" + 
					"	                               ^\n" + 
					"The resource type X does not implement java.lang.AutoCloseable\n" + 
					"----------\n" + 
					"5. ERROR in X.java (at line 4)\n" + 
					"	} catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {\n" + 
					"	          ^^^^^\n" + 
					"Annot cannot be resolved to a type\n" + 
					"----------\n" + 
					"6. ERROR in X.java (at line 4)\n" + 
					"	} catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {\n" + 
					"	                                        ^^^^^\n" + 
					"Annot cannot be resolved to a type\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913() {
		this.runNegativeTest(
				new String[]{
						"X.java",
						"public class X {\n" +
						"	public void foo(Object obj, X this) {}\n" +
						"	public void foo(Object obj1, X this, Object obj2) {}\n" +
						"	public void foo(Object obj, Object obj2, Object obj3, X this) {}\n" +
						"	class Y {\n" +
						"		Y(Object obj, Y Y.this){}\n" +
						"	}\n" +
						"}"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public void foo(Object obj, X this) {}\n" + 
				"	                              ^^^^\n" + 
				"Only the first formal parameter may be declared explicitly as 'this'\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	public void foo(Object obj1, X this, Object obj2) {}\n" + 
				"	                               ^^^^\n" + 
				"Only the first formal parameter may be declared explicitly as 'this'\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	public void foo(Object obj, Object obj2, Object obj3, X this) {}\n" + 
				"	                                                        ^^^^\n" + 
				"Only the first formal parameter may be declared explicitly as 'this'\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	Y(Object obj, Y Y.this){}\n" + 
				"	                  ^^^^\n" + 
				"Only the first formal parameter may be declared explicitly as 'this'\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913b() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"public class Outer {\n" +
						"    Outer(Outer Outer.this) {}\n" +
						"    Outer(Outer this, int i) {}\n" +
						"    class Inner<K,V> {\n" +
						"        class InnerMost<T> {\n" +
						"            InnerMost(Outer.Inner this) {}\n" +
						"            InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" +
						"            InnerMost(Outer Outer.this, float f) {}\n" +
						"            InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}\n" +
						"            InnerMost(Inner<K,V> Outer.Inner.InnerMost.this, int i) {}\n" +
						"            InnerMost(Outer.Inner<K, V> this, float f, int i) {}\n" +
						"            InnerMost(Outer.Inner<K,V> Inner.this, long l) {}\n" +
						"        }\n" +
						"    }\n" +
						"}\n"},
						"----------\n" + 
						"1. ERROR in Outer.java (at line 2)\n" + 
						"	Outer(Outer Outer.this) {}\n" + 
						"	                  ^^^^\n" + 
						"Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" + 
						"----------\n" + 
						"2. ERROR in Outer.java (at line 3)\n" + 
						"	Outer(Outer this, int i) {}\n" + 
						"	            ^^^^\n" + 
						"Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" + 
						"----------\n" + 
						"3. WARNING in Outer.java (at line 6)\n" + 
						"	InnerMost(Outer.Inner this) {}\n" + 
						"	          ^^^^^^^^^^^\n" + 
						"Outer.Inner is a raw type. References to generic type Outer.Inner<K,V> should be parameterized\n" + 
						"----------\n" + 
						"4. ERROR in Outer.java (at line 6)\n" + 
						"	InnerMost(Outer.Inner this) {}\n" + 
						"	          ^^^^^^^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" + 
						"----------\n" + 
						"5. ERROR in Outer.java (at line 6)\n" + 
						"	InnerMost(Outer.Inner this) {}\n" + 
						"	                      ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner\n" + 
						"----------\n" + 
						"6. WARNING in Outer.java (at line 7)\n" + 
						"	InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" + 
						"	          ^^^^^^^^^^^\n" + 
						"Outer.Inner is a raw type. References to generic type Outer.Inner<K,V> should be parameterized\n" + 
						"----------\n" + 
						"7. ERROR in Outer.java (at line 7)\n" + 
						"	InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" + 
						"	          ^^^^^^^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" + 
						"----------\n" + 
						"8. ERROR in Outer.java (at line 8)\n" + 
						"	InnerMost(Outer Outer.this, float f) {}\n" + 
						"	          ^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" + 
						"----------\n" + 
						"9. ERROR in Outer.java (at line 8)\n" + 
						"	InnerMost(Outer Outer.this, float f) {}\n" + 
						"	                      ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner\n" + 
						"----------\n" + 
						"10. ERROR in Outer.java (at line 9)\n" + 
						"	InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}\n" + 
						"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" + 
						"----------\n" + 
						"11. ERROR in Outer.java (at line 9)\n" + 
						"	InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}\n" + 
						"	                                                              ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner\n" + 
						"----------\n" + 
						"12. ERROR in Outer.java (at line 10)\n" + 
						"	InnerMost(Inner<K,V> Outer.Inner.InnerMost.this, int i) {}\n" + 
						"	                                           ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner\n" + 
						"----------\n" + 
						"13. ERROR in Outer.java (at line 11)\n" + 
						"	InnerMost(Outer.Inner<K, V> this, float f, int i) {}\n" + 
						"	                            ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner\n" + 
						"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913c() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"public class Outer {\n" +
						"    class Inner<K,V> {\n" +
						"        class InnerMost<T> {\n" +
						"            public void foo(Outer Outer.this) {}\n" +
						"            public void foo(Inner<K,V> Inner.this, int i) {}\n" +
						"            public void foo(InnerMost this, int i, int j) {}\n" +
						"            public void foo(Inner.InnerMost<T> this, Object obj) {}\n" +
						"            public void foo(InnerMost<T> this, float f) {}\n" +
						"            public void foo(Inner<K,V>.InnerMost<T> this, long l) {}\n" +
						"            public void foo(Outer.Inner<K,V>.InnerMost<T> this, float f, float ff) {}\n" +
						"            public void foo(InnerMost<T> Outer.Inner.InnerMost.this, int i, float f) {}\n" +
						"        }\n" +
						"    }\n" +
						"}\n"},
						"----------\n" + 
						"1. ERROR in Outer.java (at line 4)\n" + 
						"	public void foo(Outer Outer.this) {}\n" + 
						"	                ^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" + 
						"----------\n" + 
						"2. ERROR in Outer.java (at line 4)\n" + 
						"	public void foo(Outer Outer.this) {}\n" + 
						"	                            ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner.InnerMost\n" + 
						"----------\n" + 
						"3. ERROR in Outer.java (at line 5)\n" + 
						"	public void foo(Inner<K,V> Inner.this, int i) {}\n" + 
						"	                ^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" + 
						"----------\n" + 
						"4. ERROR in Outer.java (at line 5)\n" + 
						"	public void foo(Inner<K,V> Inner.this, int i) {}\n" + 
						"	                                 ^^^^\n" + 
						"The explicit 'this' parameter is expected to be qualified with Outer.Inner.InnerMost\n" + 
						"----------\n" + 
						"5. WARNING in Outer.java (at line 6)\n" + 
						"	public void foo(InnerMost this, int i, int j) {}\n" + 
						"	                ^^^^^^^^^\n" + 
						"Outer.Inner.InnerMost is a raw type. References to generic type Outer.Inner<K,V>.InnerMost<T> should be parameterized\n" + 
						"----------\n" + 
						"6. ERROR in Outer.java (at line 6)\n" + 
						"	public void foo(InnerMost this, int i, int j) {}\n" + 
						"	                ^^^^^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" + 
						"----------\n" + 
						"7. ERROR in Outer.java (at line 7)\n" + 
						"	public void foo(Inner.InnerMost<T> this, Object obj) {}\n" + 
						"	                ^^^^^^^^^^^^^^^\n" + 
						"The member type Outer.Inner.InnerMost<T> must be qualified with a parameterized type, since it is not static\n" + 
						"----------\n" + 
						"8. ERROR in Outer.java (at line 7)\n" + 
						"	public void foo(Inner.InnerMost<T> this, Object obj) {}\n" + 
						"	                ^^^^^^^^^^^^^^^\n" + 
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" + 
						"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913d() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"import java.lang.annotation.Target;\n" + 
						"import static java.lang.annotation.ElementType.*;\n" + 
						"public class Outer {\n" +
						"    class Inner<K,V> {\n" +
						"		public Inner(@Missing Outer Outer.this) {}\n" +
						"        class InnerMost<T> {\n" +
						"            public void bar() {\n" +
						"                new AnonymousInner() {\n" +
						"                    public void foobar(AnonymousInner this) {}\n" +
						"                };\n" +
						"            }\n" +
						"            void bar(int i) {\n" +
						"                class Local {\n" +
						"                    public int hashCode(Local this, int k) { return 0; }\n" +
						"                    public int hashCode(Outer.Local this) { return 0; }\n" +
						"                }\n" +
						"            }\n" +
						"        }\n" +
						"    }\n" +
						"    static class StaticNested {\n" +
						"        public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}\n" +
						"    }\n" +
						"    public static void foo(@Marker Outer this) {}\n" +
						"    public void foo(@Missing Outer this, int i) {}\n" +
						"}\n" +
						"interface AnonymousInner {\n" +
						"    public void foobar(AnonymousInner this);\n" +
						"}\n" +
						"@Target(TYPE_USE)\n" + 
						"@interface Marker {}"},
							"----------\n" + 
							"1. ERROR in Outer.java (at line 5)\n" + 
							"	public Inner(@Missing Outer Outer.this) {}\n" + 
							"	              ^^^^^^^\n" + 
							"Missing cannot be resolved to a type\n" + 
							"----------\n" + 
							"2. ERROR in Outer.java (at line 9)\n" + 
							"	public void foobar(AnonymousInner this) {}\n" + 
							"	                                  ^^^^\n" + 
							"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" + 
							"----------\n" + 
							"3. ERROR in Outer.java (at line 15)\n" + 
							"	public int hashCode(Outer.Local this) { return 0; }\n" + 
							"	                    ^^^^^^^^^^^\n" + 
							"Outer.Local cannot be resolved to a type\n" + 
							"----------\n" + 
							"4. ERROR in Outer.java (at line 21)\n" + 
							"	public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}\n" + 
							"	                    ^^^^^^^\n" + 
							"The annotation @Marker is disallowed for this location\n" + 
							"----------\n" + 
							"5. ERROR in Outer.java (at line 21)\n" + 
							"	public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}\n" + 
							"	                                                                  ^^^^\n" + 
							"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" + 
							"----------\n" + 
							"6. ERROR in Outer.java (at line 23)\n" + 
							"	public static void foo(@Marker Outer this) {}\n" + 
							"	                       ^^^^^^^\n" + 
							"The annotation @Marker is disallowed for this location\n" + 
							"----------\n" + 
							"7. ERROR in Outer.java (at line 23)\n" + 
							"	public static void foo(@Marker Outer this) {}\n" + 
							"	                                     ^^^^\n" + 
							"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" + 
							"----------\n" + 
							"8. ERROR in Outer.java (at line 24)\n" + 
							"	public void foo(@Missing Outer this, int i) {}\n" + 
							"	                 ^^^^^^^\n" + 
							"Missing cannot be resolved to a type\n" + 
							"----------\n" + 
							"9. ERROR in Outer.java (at line 29)\n" + 
							"	@Target(TYPE_USE)\n" + 
							"	        ^^^^^^^^\n" + 
							"TYPE_USE cannot be resolved to a variable\n" + 
							"----------\n");
	}
	public void test0383908() {
		this.runNegativeTest(
				new String[]{"X.java",
				"public class X { \n" +
				"	void foo(X this) {}\n" +
				"   void foo() {}\n" +
				"}\n" +
				"class Y {\n" +
				"	void foo(Y this) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Y().foo();\n" +
				"	}\n" +
				"}"}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	void foo(X this) {}\n" + 
				"	     ^^^^^^^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	void foo() {}\n" + 
				"	     ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested package names.
	public void test383596() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
				"public class X {\n" +
				"}"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	           ^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 1)\n" + 
			"	package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                                           ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested package names.
	public void test383596a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
				"public class X {\n" +
				"}"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	^^^^^^^\n" + 
			"Package annotations must be in file package-info.java\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                   ^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 1)\n" + 
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                                ^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 1)\n" + 
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                                                   ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested import names.
	public void test039b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
				"public class X {\n" +
				"}"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	       ^\n" + 
			"The import p cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	          ^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                       ^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" + 
			"	                                          ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested import names.
	public void test383596b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
				"public class X {\n" +
				"}"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
			"	       ^\n" + 
			"The import p cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
			"	          ^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
			"	                       ^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 1)\n" + 
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
			"	                                          ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error, type annotations are illegal here\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested static import names.
	public void test041() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" +
						"public class X {\n" +
						"}"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" + 
				"	              ^\n" + 
				"The import p cannot be resolved\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" + 
				"	                 ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" + 
				"	                              ^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" + 
				"	                                                 ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested static import names.
	public void test042() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
						"public class X {\n" +
						"}"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
				"	              ^\n" + 
				"The import p cannot be resolved\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
				"	                 ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
				"	                              ^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 1)\n" + 
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" + 
				"	                                                 ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit this.
	// Much water has flown under the bridge. The grammar itself does not allow annotations in qualified name in explicit this.
	// We now use the production UnannotatableName instead of plain Name. 
	public void test043() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   class Y {\n" +
			    "       class Z {\n" +
				"           Z(X. @Marker Y  X.Y.this) {\n" +
				"           }\n" +
				"       }\n" +
				"    }\n" +
				"}"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	Z(X. @Marker Y  X.Y.this) {\n" + 
			"	      ^^^^^^\n" + 
			"Marker cannot be resolved to a type\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call -- super form
	public void test044() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static X x;\n" +
						"	public class InnerBar {\n" +
						"	}\n" +
						"	public class SubInnerBar extends InnerBar {\n" +
						"		SubInnerBar() {\n" +
						"			X.@Marker x. @Marker @Marker @Marker x.super();\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.super();\n" + 
				"	  ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.super();\n" + 
				"	             ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.super();\n" + 
				"	                                     ^\n" + 
				"The static field X.x should be accessed in a static way\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call, super form with explicit type arguments
	public void test045() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static X x;\n" +
						"	public class InnerBar {\n" +
						"	}\n" +
						"	public class SubInnerBar extends InnerBar {\n" +
						"		SubInnerBar() {\n" +
						"			X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" + 
				"	  ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" + 
				"	             ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" + 
				"	                                     ^\n" + 
				"The static field X.x should be accessed in a static way\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 7)\n" + 
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" + 
				"	                                        ^^^^^^\n" + 
				"Unused type arguments for the non generic constructor X.InnerBar() of type X.InnerBar; it should not be parameterized with arguments <String>\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call - this form
	public void test046() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	Bar bar;\n" +
						"	class Bar {\n" +
						"		//static Bar x;\n" +
						"		public class InnerBar {\n" +
						"			InnerBar(Bar x) {\n" +
						"			}\n" +
						"		}\n" +
						"		public class SubInnerBar extends InnerBar {\n" +
						"			SubInnerBar() {\n" +
						"				X. @Marker bar.this();\n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 11)\n" + 
				"	X. @Marker bar.this();\n" + 
				"	^^^^^^^^^^^^^^\n" + 
				"Illegal enclosing instance specification for type X.Bar.SubInnerBar\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 11)\n" + 
				"	X. @Marker bar.this();\n" + 
				"	^^^^^^^^^^^^^^\n" + 
				"Cannot make a static reference to the non-static field X.bar\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	X. @Marker bar.this();\n" + 
				"	   ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call, this form with explicit type arguments
	public void test047() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	Bar bar;\n" +
						"	class Bar {\n" +
						"		//static Bar x;\n" +
						"		public class InnerBar {\n" +
						"			InnerBar(Bar x) {\n" +
						"			}\n" +
						"		}\n" +
						"		public class SubInnerBar extends InnerBar {\n" +
						"			SubInnerBar() {\n" +
						"				X.@Marker bar.<String>this();\n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 11)\n" + 
				"	X.@Marker bar.<String>this();\n" + 
				"	^^^^^^^^^^^^^\n" + 
				"Illegal enclosing instance specification for type X.Bar.SubInnerBar\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 11)\n" + 
				"	X.@Marker bar.<String>this();\n" + 
				"	^^^^^^^^^^^^^\n" + 
				"Cannot make a static reference to the non-static field X.bar\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	X.@Marker bar.<String>this();\n" + 
				"	  ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 11)\n" + 
				"	X.@Marker bar.<String>this();\n" + 
				"	               ^^^^^^\n" + 
				"Unused type arguments for the non generic constructor X.Bar.SubInnerBar() of type X.Bar.SubInnerBar; it should not be parameterized with arguments <String>\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in PrimaryNoNewArray
	public void test048() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	X bar;\n" +
						"	private void foo(X x) {\n" +
						"		System.out.println((x. @Marker bar));\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	System.out.println((x. @Marker bar));\n" + 
				"	                       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified this.
	public void test049() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	class Y {\n" +
						"		class Z {\n" +
						"			void foo() {\n" +
						"				Object o = X.@Marker Y.this; \n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	Object o = X.@Marker Y.this; \n" + 
				"	             ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified super.
	public void test050() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public class Y  {\n" +
						"		public void foo() {\n" +
						"			X. @Marker Y.super.hashCode();\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	X. @Marker Y.super.hashCode();\n" + 
				"	   ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in Name.class
	public void test051() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public class Y  {\n" +
						"		public void foo() {\n" +
						"			Class<?> c = X. @Marker @Illegal Y.class;\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	Class<?> c = X. @Marker @Illegal Y.class;\n" + 
				"	                ^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in Name [].class.
	public void test052() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public class Y  {\n" +
						"		public void foo() {\n" +
						"			Class<?> c = X. @Marker @Another Y @YetMore [].class;\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	Class<?> c = X. @Marker @Another Y @YetMore [].class;\n" + 
				"	                ^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	Class<?> c = X. @Marker @Another Y @YetMore [].class;\n" + 
				"	                                   ^^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in binary expressions with qualified names.
	public void test053() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    static int x;\n" +
						"    static boolean fb;\n" +
						"	 public void foo(boolean b) {\n" +
						"		x = (X.@Marker x * 10);\n" +
						"		x = (X.@Marker x / 10);\n" +
						"		x = (X.@Marker x % 10);\n" +
						"		x = (X.@Marker x + 10);\n" +
						"		x = (X.@Marker x - 10);\n" +
						"		x = (X.@Marker x << 10);\n" +
						"		x = (X.@Marker x >> 10);\n" +
						"		x = (X.@Marker x >>> 10);\n" +
						"		b = (X.@Marker x < 10);\n" +
						"		b = (X.@Marker x > 10);\n" +
						"		b = (X.@Marker x <= 10);\n" +
						"		b = (X.@Marker x >= 10);\n" +
						"		b = (X.@Marker x instanceof Object);\n" +
						"		b = (X.@Marker x == 10);\n" +
						"		b = (X.@Marker x != 10);\n" +
						"		x = (X.@Marker x & 10);\n" +
						"		x = (X.@Marker x ^ 10);\n" +
						"		x = (X.@Marker x | 10);\n" +
						"		fb = (X.@Marker fb && true);\n" +
						"		fb = (X.@Marker fb || true);\n" +
						"		x = (X.@Marker fb ? 10 : 10);\n" +
						"	 }\n" +
						"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	x = (X.@Marker x * 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	x = (X.@Marker x / 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	x = (X.@Marker x % 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	x = (X.@Marker x + 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	x = (X.@Marker x - 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	x = (X.@Marker x << 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 11)\n" + 
				"	x = (X.@Marker x >> 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 12)\n" + 
				"	x = (X.@Marker x >>> 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 13)\n" + 
				"	b = (X.@Marker x < 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 14)\n" + 
				"	b = (X.@Marker x > 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"11. ERROR in X.java (at line 15)\n" + 
				"	b = (X.@Marker x <= 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"12. ERROR in X.java (at line 16)\n" + 
				"	b = (X.@Marker x >= 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"13. ERROR in X.java (at line 17)\n" + 
				"	b = (X.@Marker x instanceof Object);\n" + 
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Incompatible conditional operand types int and Object\n" + 
				"----------\n" + 
				"14. ERROR in X.java (at line 17)\n" + 
				"	b = (X.@Marker x instanceof Object);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"15. ERROR in X.java (at line 18)\n" + 
				"	b = (X.@Marker x == 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"16. ERROR in X.java (at line 19)\n" + 
				"	b = (X.@Marker x != 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"17. ERROR in X.java (at line 20)\n" + 
				"	x = (X.@Marker x & 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"18. ERROR in X.java (at line 21)\n" + 
				"	x = (X.@Marker x ^ 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"19. ERROR in X.java (at line 22)\n" + 
				"	x = (X.@Marker x | 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"20. ERROR in X.java (at line 23)\n" + 
				"	fb = (X.@Marker fb && true);\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"21. ERROR in X.java (at line 24)\n" + 
				"	fb = (X.@Marker fb || true);\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"22. ERROR in X.java (at line 25)\n" + 
				"	x = (X.@Marker fb ? 10 : 10);\n" + 
				"	       ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in annotations with qualified names.
	   This test is disabled. Now the grammar itself forbids annotations in the said place by using the production
	   AnnotationName ::= '@' UnannotatableName. We don't want to add tests that will be fragile and unstable due to 
	   syntax. If a construct is provably not parsed at the grammar level, that ought to be good enough.
	*/
	public void test054() throws Exception {
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names used as annotation values.
	public void test055() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"@interface Annot {\n" +
					"	String bar();\n" +
					"}\n" +
					"@Annot(bar = X. @Marker s)\n" +
					"public class X {\n" +
					"	final static String s = \"\";\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	@Annot(bar = X. @Marker s)\n" + 
				"	                ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names that are postfix expressions.
	public void test056() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static int x;\n" +
					"    int foo() {\n" +
					"        return X.@Marker x;\n" +
					"    }\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	return X.@Marker x;\n" + 
				"	         ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names used in array access.
	public void test057() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static int x[];\n" +
					"    int foo() {\n" +
					"        return X.@Marker x[0];\n" +
					"    }\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	return X.@Marker x[0];\n" + 
				"	         ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name with type arguments used in method invocation.
	public void test058() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static X x;\n" +
					"    int foo() {\n" +
					"        return X.@Marker x.<String> foo();\n" +
					"    }\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	return X.@Marker x.<String> foo();\n" + 
				"	         ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 4)\n" + 
				"	return X.@Marker x.<String> foo();\n" + 
				"	                    ^^^^^^\n" + 
				"Unused type arguments for the non generic method foo() of type X; it should not be parameterized with arguments <String>\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in method invocation.
	public void test059() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static X x;\n" +
					"    int foo() {\n" +
					"        return X.@Marker x. @Blah foo();\n" +
					"    }\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	return X.@Marker x. @Blah foo();\n" + 
				"	         ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	return X.@Marker x. @Blah foo();\n" + 
				"	                    ^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in class instance creation
	public void test060() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static Y y;\n" +
					"    class Y {\n" +
					"        class Z {\n" +
					"            void foo() {\n" +
					"                Z z = X. @Marker y.new Z();\n" +
					"            }\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	Z z = X. @Marker y.new Z();\n" + 
				"	         ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in class instance creation
	public void test061() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static X x;\n" +
					"    X getX() {\n" +
					"        return (X.@Marker x);\n" +
					"    }\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	return (X.@Marker x);\n" + 
				"	          ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	public void test062() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public <T> @Marker Object foo() {\n" +
					"	}\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public <T> @Marker Object foo() {\n" + 
				"	           ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	public void test063() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = @Marker int.class;\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	Object o = @Marker int.class;\n" + 
				"	           ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	public void test064() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"@interface X {\n" +
					"	<T> @Marker String foo();\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	<T> @Marker String foo();\n" + 
				"	    ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	<T> @Marker String foo();\n" + 
				"	                   ^^^^^\n" + 
				"Annotation attributes cannot be generic\n" + 
				"----------\n");
	}
	public void test065() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = new <String> @Marker X();\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	Object o = new <String> @Marker X();\n" + 
				"	                ^^^^^^\n" + 
				"Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	Object o = new <String> @Marker X();\n" + 
				"	                        ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	public void test066() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = new X().new <String> @Marker X();\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	Object o = new X().new <String> @Marker X();\n" + 
				"	                                ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	Object o = new X().new <String> @Marker X();\n" + 
				"	                                ^^^^^^^^^\n" + 
				"X.X cannot be resolved to a type\n" + 
				"----------\n");
	}
	public void test067() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = x.new <String> @Marker X() {};\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	Object o = x.new <String> @Marker X() {};\n" + 
				"	           ^\n" + 
				"x cannot be resolved to a variable\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	Object o = x.new <String> @Marker X() {};\n" + 
				"	                          ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	public void test068() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = new <String> @Marker X() {};\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				}, 
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	Object o = new <String> @Marker X() {};\n" + 
				"	                ^^^^^^\n" + 
				"Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	Object o = new <String> @Marker X() {};\n" + 
				"	                        ^^^^^^^\n" + 
				"Syntax error, type annotations are illegal here\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385293
	public void test069() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"class X<final T> {\n" +
					"	Object o = (Object) (public X<final String>) null;\n" + 
					"}\n"
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	class X<final T> {\n" + 
				"	        ^^^^^\n" + 
				"Syntax error on token \"final\", delete this token\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 2)\n" + 
				"	Object o = (Object) (public X<final String>) null;\n" + 
				"	                     ^^^^^^\n" + 
				"Syntax error on token \"public\", delete this token\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 2)\n" + 
				"	Object o = (Object) (public X<final String>) null;\n" + 
				"	                              ^^^^^\n" + 
				"Syntax error on token \"final\", delete this token\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388085
	public void test0388085() {
		this.runNegativeTest(
				new String[] {"X.java",
						"class X {\n" +
						"	public void main() {\n" +
						"		final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;" +
						"		one = null;\n" +
						"	}\n" +
						"}\n" +
						"class One<R> {}\n" +
						"class Two<S> {}\n" +
						"class Three<T> {}\n" +
						"class Four<U, V> {}\n"},
							"----------\n" + 
							"1. ERROR in X.java (at line 3)\n" + 
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
							"	           ^^^^^^\n" + 
							"Marker cannot be resolved to a type\n" + 
							"----------\n" + 
							"2. ERROR in X.java (at line 3)\n" + 
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
							"	                                 ^^^^^^\n" + 
							"Marker cannot be resolved to a type\n" + 
							"----------\n" + 
							"3. ERROR in X.java (at line 3)\n" + 
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
							"	                                                                      ^^^^^^\n" + 
							"Marker cannot be resolved to a type\n" + 
							"----------\n" + 
							"4. ERROR in X.java (at line 3)\n" + 
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
							"	                                                                                              ^^^^^^\n" + 
							"Marker cannot be resolved to a type\n" + 
							"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388085
	public void test0388085a() {
		this.runNegativeTest(
				new String[] {"X.java",
						"import java.lang.annotation.Target;\n" + 
						"import static java.lang.annotation.ElementType.*;\n" + 
						"class X {\n" +
						"	public void main() {\n" +
						"		final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;" +
						"		one = null;\n" +
						"	}\n" +
						"}\n" +
						"class One<R> {}\n" +
						"class Two<S> {}\n" +
						"class Three<T> {}\n" +
						"class Four<U, V> {}\n" +
						"@interface Marker {}"},
						"----------\n" + 
						"1. ERROR in X.java (at line 5)\n" + 
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
						"	          ^^^^^^^\n" + 
						"Only annotation types that explicitly specify TYPE_USE as a possible target element type can be applied here\n" + 
						"----------\n" + 
						"2. ERROR in X.java (at line 5)\n" + 
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
						"	                                ^^^^^^^\n" + 
						"Only annotation types that explicitly specify TYPE_USE as a possible target element type can be applied here\n" + 
						"----------\n" + 
						"3. ERROR in X.java (at line 5)\n" + 
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
						"	                                                                     ^^^^^^^\n" + 
						"Only annotation types that explicitly specify TYPE_USE as a possible target element type can be applied here\n" + 
						"----------\n" + 
						"4. ERROR in X.java (at line 5)\n" + 
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" + 
						"	                                                                                             ^^^^^^^\n" + 
						"Only annotation types that explicitly specify TYPE_USE as a possible target element type can be applied here\n" + 
						"----------\n");
	}

}
