/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

public class TypeAnnotationTest extends AbstractRegressionTest {

	static { 
//		TESTS_NUMBERS = new int [] { 25 };
	}
	public static Class testClass() {
		return TypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}
	public TypeAnnotationTest(String testName){
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
		this.runConformTest(
				new String[] {
					"Marker.java",
					"import java.lang.annotation.Target;\n" + 
					"import static java.lang.annotation.ElementType.*;\n" + 
					"@Target(TYPE_USE)\n" + 
					"@interface Marker {}",
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"");
	}
	public void test004() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"import java.lang.annotation.Target;\n" + 
					"import static java.lang.annotation.ElementType.*;\n" + 
					"@Target(TYPE_PARAMETER)\n" + 
					"@interface Marker {}",
					"X.java",
					"public class X<@Marker T> {}",
				},
				"");
	}
	public void test005() throws Exception {
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
				"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	public class X<@Marker T> {}\n" + 
				"	               ^^^^^^^\n" + 
				"The annotation @Marker is disallowed for this location\n" + 
				"----------\n");
	}
	public void test006() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String id() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"Y.java",
				"class Y {}\n",
				"X.java",
				"public class X extends @A(id=\"Hello, World!\") @B @C('(') Y {\n" + 
				"}",
		},
		"");
	}
	public void test007() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String id() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
		},
		"");
	}
	public void test008() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String id() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" + 
				"	public boolean foo(String s) {\n" + 
				"		return (s instanceof @C('_') Object);\n" + 
				"	}\n" + 
				"	public Object foo1(String s) {\n" + 
				"		return new @B(3) @A(\"new Object\") Object();\n" + 
				"	}\n" + 
				"	public Class foo2(String s) {\n" + 
				"		return @B(4) Object.class;\n" + 
				"	}\n" + 
				"	public Class foo3(String s) {\n" + 
				"		return @A(\"int class literal\")  @B(5) int.class;\n" + 
				"	}\n" + 
				"}",
		},
		"");
	}
	public void test009() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String id() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" + 
				"	public boolean foo(Object o) {\n" + 
				"		return (o instanceof @C('_') Object[]);\n" + 
				"	}\n" + 
				"	public Object foo1(String s) {\n" + 
				"		return new @B(3) @A(\"new Object\") Object[] {};\n" + 
				"	}\n" + 
				"	public Class foo2(String s) {\n" + 
				"		return @B(4) Object[].class;\n" + 
				"	}\n" + 
				"	public Class foo3(String s) {\n" + 
				"		return @A(\"int class literal\")  @B(5) int[].class;\n" + 
				"	}\n" + 
				"}",
		},
		"");
	}
	public void test010() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"Y.java",
				"class Y<T> {}\n",
				"X.java",
				"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String> {\n" + 
				"}",
		},
		"");
	}
	public void test011() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"I.java",
				"interface I<T> {}\n",
				"J.java",
				"interface J<T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C('(') Integer> {}",
		},
		"");
	}
	// throws
	public void test012() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"E.java",
				"class E extends RuntimeException {}\n",
				"E1.java",
				"class E1 extends RuntimeException {}\n",
				"E2.java",
				"class E2 extends RuntimeException {}\n",
				"X.java",
				"public class X {\n" +
				"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C('(') E2 {}\n" +
				"}",
		},
		"");
	}
	// method receiver
	public void test013() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	void foo() @B(3) {}\n" + 
				"}",
		},
		"");
	}
	// method return type
	public void test014() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	@B(3) int foo() {\n" +
				"		return 1;\n" +
				"	}\n" + 
				"}",
		},
		"");
	}
	// field type
	public void test015() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	@B(3) int field;\n" +
				"}",
		},
		"");
	}
	// method parameter
	public void test016() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	int foo(@B(3) String s) {\n" +
				"		return s.length();\n" +
				"	}\n" + 
				"}",
		},
		"");
	}
	// method parameter generic or array
	public void test017() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	int foo(String @B(3) [] s) {\n" +
				"		return s.length;\n" +
				"	}\n" + 
				"}",
		},
		"");
	}
	// field type generic or array
	public void test018() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	int @B(3) [] field;\n" +
				"}",
		},
		"");
	}
	// class type parameter
	public void test019() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X<@A @B(3) T> {}",
		},
		"");
	}
	// method type parameter
	public void test020() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	<@A @B(3) T> void foo(T t) {}\n" + 
				"}",
		},
		"");
	}
	// class type parameter bound
	public void test021() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X<T extends @A String & @B(3) Cloneable>  {}",
		},
		"");
	}
	// class type parameter bound generic or array
	public void test022() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"");
	}
	// method type parameter bound
	public void test023() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"X.java",
				"public class X {\n" +
				"	<T extends @A String & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
	}
	// class type parameter bound generic or array
	public void test024() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X {\n" +
				"	<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
	}
	// local variable + generic or array
	public void test025() throws Exception {
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface A {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"B.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface B {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface C {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
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
		"");
	}
}
