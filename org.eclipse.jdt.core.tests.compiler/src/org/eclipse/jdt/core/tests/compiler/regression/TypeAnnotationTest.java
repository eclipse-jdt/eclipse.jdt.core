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

import java.io.File;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

import junit.framework.Test;

public class TypeAnnotationTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
	}
	public static Class testClass() {
		return TypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public TypeAnnotationTest(String testName){
		super(testName);
	}
//	// superclass
//	public void test001() throws Exception {
//		this.runConformTest(
//				new String[] {
//					"Marker.java",
//					"import java.lang.annotation.Target;\n" + 
//					"import static java.lang.annotation.ElementType.*;\n" + 
//					"@Target(TYPE_USE)\n" + 
//					"@interface Marker {}",
//					"X.java",
//					"public class X extends @Marker Object {}",
//				},
//				"");
//		String expectedOutput =
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #17 @Marker(\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = -1\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// type parameter
//	public void test002() throws Exception {
//		this.runConformTest(
//				new String[] {
//					"Marker.java",
//					"import java.lang.annotation.Target;\n" + 
//					"import static java.lang.annotation.ElementType.*;\n" + 
//					"@Target(TYPE_PARAMETER)\n" + 
//					"@interface Marker {}",
//					"X.java",
//					"public class X<@Marker T> {}",
//				},
//				"");
//		String expectedOutput =
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #21 @Marker(\n" + 
//			"      target type = 0x22 CLASS_TYPE_PARAMETER\n" + 
//			"      type parameter index = 0\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// superclass
//	public void test003() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String id() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"Y.java",
//				"class Y {}\n",
//				"X.java",
//				"public class X extends @A(id=\"Hello, World!\") @B @C('(') Y {\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #19 @A(\n" + 
//			"      #20 id=\"Hello, World!\" (constant type)\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = -1\n" + 
//			"    )\n" + 
//			"    #22 @C(\n" + 
//			"      #23 value=\'(\' (constant type)\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = -1\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #17 @B(\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = -1\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// super interfaces
//	public void test004() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String id() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"I.java",
//				"interface I {}\n",
//				"J.java",
//				"interface J {}\n",
//				"X.java",
//				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #23 @A(\n" + 
//			"      #24 id=\"Hello, World!\" (constant type)\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = 0\n" + 
//			"    )\n" + 
//			"    #26 @C(\n" + 
//			"      #27 value=\'(\' (constant type)\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = 1\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #21 @B(\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = 1\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// class literal
//	public void test005() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"I.java",
//				"interface I {}\n",
//				"J.java",
//				"interface J {}\n",
//				"X.java",
//				"public class X {\n" + 
//				"	public boolean foo(String s) {\n" + 
//				"		boolean b = (s instanceof @C('_') Object);\n" + 
//				"		Object o = new @B(3) @A(\"new Object\") Object();\n" + 
//				"		Class<?> c = @B(4) Object.class;\n" + 
//				"		Class<?> c2 = @A(\"int class literal\")  @B(5) int.class;\n" + 
//				"		System.out.println(o.toString() + c.toString() + c2.toString());\n" + 
//				"		return b;\n" + 
//				"	}\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"    RuntimeVisibleTypeAnnotations: \n" + 
//			"      #73 @C(\n" + 
//			"        #68 value=\'_\' (constant type)\n" + 
//			"        target type = 0x2 TYPE_INSTANCEOF\n" + 
//			"        offset = 1\n" + 
//			"      )\n" + 
//			"      #75 @A(\n" + 
//			"        #68 value=\"new Object\" (constant type)\n" + 
//			"        target type = 0x4 OBJECT_CREATION\n" + 
//			"        offset = 5\n" + 
//			"      )\n" + 
//			"      #75 @A(\n" + 
//			"        #68 value=\"int class literal\" (constant type)\n" + 
//			"        target type = 0x1e CLASS_LITERAL\n" + 
//			"        offset = 17\n" + 
//			"      )\n" + 
//			"    RuntimeInvisibleTypeAnnotations: \n" + 
//			"      #67 @B(\n" + 
//			"        #68 value=(int) 3 (constant type)\n" + 
//			"        target type = 0x4 OBJECT_CREATION\n" + 
//			"        offset = 5\n" + 
//			"      )\n" + 
//			"      #67 @B(\n" + 
//			"        #68 value=(int) 4 (constant type)\n" + 
//			"        target type = 0x1e CLASS_LITERAL\n" + 
//			"        offset = 13\n" + 
//			"      )\n" + 
//			"      #67 @B(\n" + 
//			"        #68 value=(int) 5 (constant type)\n" + 
//			"        target type = 0x1e CLASS_LITERAL\n" + 
//			"        offset = 17\n" + 
//			"      )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// class literal generic and array
//	public void test006() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"I.java",
//				"interface I {}\n",
//				"J.java",
//				"interface J {}\n",
//				"X.java",
//				"public class X {\n" + 
//				"	public boolean foo(Object o) {\n" + 
//				"		boolean b = (o instanceof @C('_') Object[]);\n" + 
//				"		Object o1 = new @B(3) @A(\"new Object\") Object[] {};\n" + 
//				"		Class<?> c = @B(4) Object[].class;\n" + 
//				"		Class<?> c2 = @A(\"int class literal\")  @B(5) int[].class;\n" + 
//				"		System.out.println(o1.toString() + c.toString() + c2.toString());\n" + 
//				"		return b;\n" + 
//				"	}\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"    RuntimeVisibleTypeAnnotations: \n" + 
//			"      #70 @C(\n" + 
//			"        #66 value=\'_\' (constant type)\n" + 
//			"        target type = 0x2 TYPE_INSTANCEOF\n" + 
//			"        offset = 1\n" + 
//			"      )\n" + 
//			"      #72 @A(\n" + 
//			"        #66 value=\"int class literal\" (constant type)\n" + 
//			"        target type = 0x1e CLASS_LITERAL\n" + 
//			"        offset = 14\n" + 
//			"      )\n" + 
//			"    RuntimeInvisibleTypeAnnotations: \n" + 
//			"      #65 @B(\n" + 
//			"        #66 value=(int) 4 (constant type)\n" + 
//			"        target type = 0x1e CLASS_LITERAL\n" + 
//			"        offset = 10\n" + 
//			"      )\n" + 
//			"      #65 @B(\n" + 
//			"        #66 value=(int) 5 (constant type)\n" + 
//			"        target type = 0x1e CLASS_LITERAL\n" + 
//			"        offset = 14\n" + 
//			"      )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// parameterized superclass
//	public void test007() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"Y.java",
//				"class Y<T> {}\n",
//				"X.java",
//				"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String> {\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #21 @A(\n" + 
//			"      #22 value=\"Hello, World!\" (constant type)\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = -1\n" + 
//			"    )\n" + 
//			"    #24 @C(\n" + 
//			"      #22 value=\'(\' (constant type)\n" + 
//			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
//			"      type index = -1\n" + 
//			"      locations = {0}\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #19 @B(\n" + 
//			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
//			"      type index = -1\n" + 
//			"      locations = {0}\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	public void test008() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"I.java",
//				"interface I<T> {}\n",
//				"J.java",
//				"interface J<U,T> {}\n",
//				"X.java",
//				"public class X implements I<@A(\"Hello, World!\") String>, @B J<String, @C('(') Integer> {}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #25 @A(\n" + 
//			"      #26 value=\"Hello, World!\" (constant type)\n" + 
//			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
//			"      type index = 0\n" + 
//			"      locations = {0}\n" + 
//			"    )\n" + 
//			"    #28 @C(\n" + 
//			"      #26 value=\'(\' (constant type)\n" + 
//			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
//			"      type index = 1\n" + 
//			"      locations = {1}\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #23 @B(\n" + 
//			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
//			"      type index = 1\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// throws
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
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #25 @A(\n" + 
			"        #26 value=\"Hello, World!\" (constant type)\n" + 
			"        target type = 0x16 THROWS\n" + 
			"        throws index = 0\n" + 
			"      )\n" + 
			"      #28 @C(\n" + 
			"        #26 value=\'(\' (constant type)\n" + 
			"        target type = 0x16 THROWS\n" + 
			"        throws index = 2\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #23 @B(\n" + 
			"        target type = 0x16 THROWS\n" + 
			"        throws index = 2\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
//	// method receiver
//	public void test010() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"X.java",
//				"public class X {\n" + 
//				"	void foo() @B(3) {}\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"    RuntimeInvisibleTypeAnnotations: \n" + 
//			"      #16 @B(\n" + 
//			"        #17 value=(int) 3 (constant type)\n" + 
//			"        target type = 0x6 METHOD_RECEIVER\n" + 
//			"      )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// method return type
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
				"X.java",
				"public class X {\n" + 
				"	@B(3) @A(value=\"test\") int foo() {\n" +
				"		return 1;\n" +
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #21 @A(\n" + 
			"        #18 value=\"test\" (constant type)\n" + 
			"        target type = 0xa METHOD_RETURN_TYPE\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #17 @B(\n" + 
			"        #18 value=(int) 3 (constant type)\n" + 
			"        target type = 0xa METHOD_RETURN_TYPE\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// field type
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
				"X.java",
				"public class X {\n" + 
				"	@B(3) @A int field;\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #12 @A(\n" + 
			"        target type = 0xe FIELD\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #8 @B(\n" + 
			"        #9 value=(int) 3 (constant type)\n" + 
			"        target type = 0xe FIELD\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
//	// method parameter
//	public void test013() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"X.java",
//				"public class X {\n" + 
//				"	int foo(@B(3) String s) {\n" +
//				"		return s.length();\n" +
//				"	}\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"    RuntimeInvisibleTypeAnnotations: \n" + 
//			"      #25 @B(\n" + 
//			"        #26 value=(int) 3 (constant type)\n" + 
//			"        target type = 0xc METHOD_PARAMETER\n" + 
//			"        method parameter index = 0\n" + 
//			"      )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// method parameter generic or array
//	public void test014() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"X.java",
//				"public class X {\n" + 
//				"	int foo(String @A [] @B(3) [] s) {\n" +
//				"		return s.length;\n" +
//				"	}\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"    RuntimeVisibleTypeAnnotations: \n" + 
//			"      #23 @A(\n" + 
//			"        target type = 0xc METHOD_PARAMETER\n" + 
//			"        method parameter index = 0\n" + 
//			"      )\n" + 
//			"    RuntimeInvisibleTypeAnnotations: \n" + 
//			"      #19 @B(\n" + 
//			"        #20 value=(int) 3 (constant type)\n" + 
//			"        target type = 0xd METHOD_PARAMETER_GENERIC_OR_ARRAY\n" + 
//			"        method parameter index = 0\n" + 
//			"        locations = {0}\n" + 
//			"      )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// field type generic or array
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
				"	@A int [] @B(3) [] field;\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #12 @A(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #8 @B(\n" + 
			"        #9 value=(int) 3 (constant type)\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {0}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
//	// class type parameter
//	public void test016() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_PARAMETER)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_PARAMETER)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"X.java",
//				"public class X<@A @B(3) T> {}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #25 @A(\n" + 
//			"      target type = 0x22 CLASS_TYPE_PARAMETER\n" + 
//			"      type parameter index = 0\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #21 @B(\n" + 
//			"      #22 value=(int) 3 (constant type)\n" + 
//			"      target type = 0x22 CLASS_TYPE_PARAMETER\n" + 
//			"      type parameter index = 0\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// method type parameter
	public void test017() throws Exception {
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
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #27 @A(\n" + 
			"        target type = 0x20 METHOD_TYPE_PARAMETER\n" + 
			"        type parameter index = 0\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #23 @B(\n" + 
			"        #24 value=(int) 3 (constant type)\n" + 
			"        target type = 0x20 METHOD_TYPE_PARAMETER\n" + 
			"        type parameter index = 0\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
//	// class type parameter bound
//	public void test018() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"X.java",
//				"public class X<T extends @A String & @B(3) Cloneable> {}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #25 @A(\n" + 
//			"      target type = 0x10 CLASS_TYPE_PARAMETER_BOUND\n" + 
//			"      type parameter index = 0 type parameter bound index = 0\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #21 @B(\n" + 
//			"      #22 value=(int) 3 (constant type)\n" + 
//			"      target type = 0x10 CLASS_TYPE_PARAMETER_BOUND\n" + 
//			"      type parameter index = 0 type parameter bound index = 1\n" + 
//			"    )\n" ;
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
//	// class type parameter bound generic or array
//	public void test019() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//				"B.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(CLASS)\n" + 
//				"@interface B {\n" + 
//				"	int value() default -1;\n" + 
//				"}",
//				"C.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface C {\n" + 
//				"	char value() default '-';\n" + 
//				"}\n",
//				"Y.java",
//				"public class Y<T> {}",
//				"X.java",
//				"public class X<U, T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
//		},
//		"");
//		String expectedOutput =
//			"  RuntimeVisibleTypeAnnotations: \n" + 
//			"    #25 @A(\n" + 
//			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
//			"      type parameter index = 1 type parameter bound index = 0\n" + 
//			"      locations = {0,2}\n" + 
//			"    )\n" + 
//			"    #26 @C(\n" + 
//			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
//			"      type parameter index = 1 type parameter bound index = 0\n" + 
//			"      locations = {0}\n" + 
//			"    )\n" + 
//			"  RuntimeInvisibleTypeAnnotations: \n" + 
//			"    #21 @B(\n" + 
//			"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
//			"      type parameter index = 1 type parameter bound index = 0\n" + 
//			"      locations = {0,1}\n" + 
//			"    )\n" + 
//			"    #21 @B(\n" + 
//			"      #22 value=(int) 3 (constant type)\n" + 
//			"      target type = 0x10 CLASS_TYPE_PARAMETER_BOUND\n" + 
//			"      type parameter index = 1 type parameter bound index = 1\n" + 
//			"    )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// method type parameter bound
	public void test020() throws Exception {
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
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X {\n" +
				"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #27 @A(\n" + 
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" + 
			"        type parameter index = 0 type parameter bound index = 0\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #23 @B(\n" + 
			"        #24 value=(int) 3 (constant type)\n" + 
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" + 
			"        type parameter index = 0 type parameter bound index = 1\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// class type parameter bound generic or array
	public void test021() throws Exception {
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
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X {\n" +
				"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #27 @A(\n" + 
			"        target type = 0x13 METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
			"        type parameter index = 0 type parameter bound index = 0\n" + 
			"        locations = {0,2}\n" + 
			"      )\n" + 
			"      #28 @C(\n" + 
			"        target type = 0x13 METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
			"        type parameter index = 0 type parameter bound index = 0\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #23 @B(\n" + 
			"        target type = 0x13 METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
			"        type parameter index = 0 type parameter bound index = 0\n" + 
			"        locations = {0,1}\n" + 
			"      )\n" + 
			"      #23 @B(\n" + 
			"        #24 value=(int) 3 (constant type)\n" + 
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" + 
			"        type parameter index = 0 type parameter bound index = 1\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// local variable + generic or array
	public void test022() throws Exception {
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
				"	String[][] bar() {\n" + 
				"		return new String[][] {};" +
				"	}\n" + 
				"	void foo(String s) {\n" + 
				"		@C int i;\n" + 
				"		@A String [] @B(3)[] tab = bar();\n" + 
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
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #49 @C(\n" + 
			"        target type = 0x8 LOCAL_VARIABLE\n" + 
			"        local variable entries:\n" + 
			"          [pc: 11, pc: 24] index: 2\n" + 
			"          [pc: 34, pc: 46] index: 2\n" + 
			"      )\n" + 
			"      #50 @A(\n" + 
			"        target type = 0x9 LOCAL_VARIABLE_GENERIC_OR_ARRAY\n" + 
			"        local variable entries:\n" + 
			"          [pc: 5, pc: 46] index: 3\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #45 @B(\n" + 
			"        #46 value=(int) 3 (constant type)\n" + 
			"        target type = 0x9 LOCAL_VARIABLE_GENERIC_OR_ARRAY\n" + 
			"        local variable entries:\n" + 
			"          [pc: 5, pc: 46] index: 3\n" + 
			"        locations = {0}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type argument constructor call
	public void test023() throws Exception {
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
				"	<T> X(T t) {\n" + 
				"	}\n" + 
				"	public Object foo() {\n" + 
				"		X x = new <@A @B(1) String>X(null);\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #31 @A(\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 5\n" + 
			"        type argument index = 0\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #27 @B(\n" + 
			"        #28 value=(int) 1 (constant type)\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 5\n" + 
			"        type argument index = 0\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type argument constructor call generic or array
	public void test024() throws Exception {
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
				"	<T, U> X(T t, U u) {\n" + 
				"	}\n" + 
				"	public Object foo() {\n" + 
				"		X x = new <@A Integer, @A String @C [] @B(1)[]>X(null, null);\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #33 @A(\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 6\n" + 
			"        type argument index = 0\n" + 
			"      )\n" + 
			"      #33 @A(\n" + 
			"        target type = 0x19 TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY\n" + 
			"        offset = 6\n" + 
			"        type argument index = 1\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"      #34 @C(\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 6\n" + 
			"        type argument index = 1\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #29 @B(\n" + 
			"        #30 value=(int) 1 (constant type)\n" + 
			"        target type = 0x19 TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY\n" + 
			"        offset = 6\n" + 
			"        type argument index = 1\n" + 
			"        locations = {0}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type argument method call
	public void test025() throws Exception {
		this.runConformTest(
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
		},
		"SUCCESS");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #52 @A(\n" + 
			"        target type = 0x1a TYPE_ARGUMENT_METHOD_CALL\n" + 
			"        offset = 13\n" + 
			"        type argument index = 0\n" + 
			"      )\n" + 
			"      #53 @C(\n" + 
			"        #49 value=\'-\' (constant type)\n" + 
			"        target type = 0x1a TYPE_ARGUMENT_METHOD_CALL\n" + 
			"        offset = 13\n" + 
			"        type argument index = 1\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #48 @B(\n" + 
			"        #49 value=(int) 1 (constant type)\n" + 
			"        target type = 0x1a TYPE_ARGUMENT_METHOD_CALL\n" + 
			"        offset = 13\n" + 
			"        type argument index = 0\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// check locations
	public void test026() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"public class X {\n" + 
				"	@H String @E[] @F[] @G[] field;\n" + 
				"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
				"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
				"}",
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
				"D.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface D {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"E.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface E {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"F.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface F {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"G.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface G {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"H.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface H {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
		},
		"");
		String expectedOutput =
			"  // Field descriptor #6 [[[Ljava/lang/String;\n" + 
			"  java.lang.String[][][] field;\n" + 
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #11 @H(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {2}\n" + 
			"      )\n" + 
			"      #12 @F(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #8 @E(\n" + 
			"        target type = 0xe FIELD\n" + 
			"      )\n" + 
			"      #9 @G(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"  \n" + 
			"  // Field descriptor #14 Ljava/util/Map;\n" + 
			"  // Signature: Ljava/util/Map<Ljava/lang/String;Ljava/util/List<Ljava/lang/Object;>;>;\n" + 
			"  java.util.Map field2;\n" + 
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #18 @A(\n" + 
			"        target type = 0xe FIELD\n" + 
			"      )\n" + 
			"      #19 @C(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"      #20 @D(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1,0}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #17 @B(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"  \n" + 
			"  // Field descriptor #14 Ljava/util/Map;\n" + 
			"  // Signature: Ljava/util/Map<Ljava/lang/String;[[[Ljava/lang/String;>;\n" + 
			"  java.util.Map field3;\n" + 
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #18 @A(\n" + 
			"        target type = 0xe FIELD\n" + 
			"      )\n" + 
			"      #11 @H(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1,2}\n" + 
			"      )\n" + 
			"      #12 @F(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1,0}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #17 @B(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"      #8 @E(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"      #9 @G(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1,1}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// check locations
	public void test027() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@H java.lang.String @E[] @F[] @G[] field;\n" + 
				"}",
				"E.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface E {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"F.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface F {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"G.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(CLASS)\n" + 
				"@interface G {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"H.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface H {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #11 @H(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {2}\n" + 
			"      )\n" + 
			"      #12 @F(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #8 @E(\n" + 
			"        target type = 0xe FIELD\n" + 
			"      )\n" + 
			"      #9 @G(\n" + 
			"        target type = 0xf FIELD_GENERIC_OR_ARRAY\n" + 
			"        locations = {1}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// cast
	public void test028() throws Exception {
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
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X {\n" + 
				"	public void foo(Object o) {\n" + 
				"		if (o instanceof String[][]) {\n" +
				"			String[][] tab = (@C('_') @B(3) String[] @A[]) o;\n" +
				"			System.out.println(tab.length);\n" +
				"		}\n" + 
				"		System.out.println(o);\n" +
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #41 @C(\n" + 
			"        #38 value=\'_\' (constant type)\n" + 
			"        target type = 0x1 TYPE_CAST_GENERIC_OR_ARRAY\n" + 
			"        offset = 8\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"      #43 @A(\n" + 
			"        target type = 0x1 TYPE_CAST_GENERIC_OR_ARRAY\n" + 
			"        offset = 8\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #37 @B(\n" + 
			"        #38 value=(int) 3 (constant type)\n" + 
			"        target type = 0x1 TYPE_CAST_GENERIC_OR_ARRAY\n" + 
			"        offset = 8\n" + 
			"        locations = {1}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// qualified allocation expression with type arguments
	public void test029() throws Exception {
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
				"D.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface D {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"X.java",
				"public class X {\n" + 
				"	class Y {\n" + 
				"		<T, U> Y(T t, U u) {}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Y y = new X().new <@D() @A(value = \"hello\") String, @B X> Y(\"SUCCESS\", null);\n" + 
				"		System.out.println(y);\n" + 
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #47 @D(\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 19\n" + 
			"        type argument index = 0\n" + 
			"      )\n" + 
			"      #48 @A(\n" + 
			"        #49 value=\"hello\" (constant type)\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 19\n" + 
			"        type argument index = 0\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #45 @B(\n" + 
			"        target type = 0x18 TYPE_ARGUMENT_CONSTRUCTOR_CALL\n" + 
			"        offset = 19\n" + 
			"        type argument index = 1\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// local + wildcard
	// qualified allocation expression with type arguments
	public void test030() throws Exception {
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
				"D.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_USE)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface D {\n" + 
				"	char value() default '-';\n" + 
				"}\n",
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.HashMap;\n" +
				"@SuppressWarnings({\"unchecked\",\"rawtypes\"})\n" + 
				"public class X {\n" + 
				"	Object newMap(Object o) {\n" + 
				"		Map<@A Object, ? super @C Map<@B String, @D Comparable>> map;\n" + 
				"		if (o == null) {\n" + 
				"			map = null;\n" + 
				"			System.out.println(map);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No map yet\");\n" + 
				"		}\n" + 
				"		map = new HashMap();\n" + 
				"		return map;\n" + 
				"	} \n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #46 @A(\n" + 
			"        target type = 0x9 LOCAL_VARIABLE_GENERIC_OR_ARRAY\n" + 
			"        local variable entries:\n" + 
			"          [pc: 6, pc: 16] index: 2\n" + 
			"          [pc: 32, pc: 34] index: 2\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"      #47 @C(\n" + 
			"        target type = 0x1c WILDCARD_BOUND\n" + 
			"        wildcard location type = 0x9 LOCAL_VARIABLE_GENERIC_OR_ARRAY\n" + 
			"          local variable entries:\n" + 
			"                [pc: 6, pc: 16] index: 2\n" + 
			"                [pc: 32, pc: 34] index: 2\n" + 
			"              wildcard locations = {1}\n" + 
			"      )\n" + 
			"      #48 @D(\n" + 
			"        target type = 0x1d WILDCARD_BOUND_GENERIC_OR_ARRAY\n" + 
			"        wildcard location type = 0x9 LOCAL_VARIABLE_GENERIC_OR_ARRAY\n" + 
			"          local variable entries:\n" + 
			"                [pc: 6, pc: 16] index: 2\n" + 
			"                [pc: 32, pc: 34] index: 2\n" + 
			"              wildcard locations = {1}\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #44 @B(\n" + 
			"        target type = 0x1d WILDCARD_BOUND_GENERIC_OR_ARRAY\n" + 
			"        wildcard location type = 0x9 LOCAL_VARIABLE_GENERIC_OR_ARRAY\n" + 
			"          local variable entries:\n" + 
			"                [pc: 6, pc: 16] index: 2\n" + 
			"                [pc: 32, pc: 34] index: 2\n" + 
			"              wildcard locations = {1}\n" + 
			"        locations = {0}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// method type parameter bound generic or array
	public void test031() throws Exception {
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
				"@Retention(CLASS)\n" + 
				"@interface C {\n" + 
				"	int value() default -1;\n" + 
				"}",
				"D.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@Retention(RUNTIME)\n" + 
				"@interface D {\n" + 
				"	String value() default \"default\";\n" + 
				"}\n",
				"Z.java",
				"public class Z<T> {}",
				"X.java",
				"public class X {\n" +
				"	<@D U, T extends Z<@A String @C[][]@B[]> & @B(3) Cloneable> void foo(U u, T t) {}\n" +
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #31 @D(\n" + 
			"        target type = 0x20 METHOD_TYPE_PARAMETER\n" + 
			"        type parameter index = 0\n" + 
			"      )\n" + 
			"      #32 @A(\n" + 
			"        target type = 0x13 METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
			"        type parameter index = 1 type parameter bound index = 0\n" + 
			"        locations = {0,2}\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #26 @C(\n" + 
			"        target type = 0x13 METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
			"        type parameter index = 1 type parameter bound index = 0\n" + 
			"        locations = {0}\n" + 
			"      )\n" + 
			"      #27 @B(\n" + 
			"        target type = 0x13 METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY\n" + 
			"        type parameter index = 1 type parameter bound index = 0\n" + 
			"        locations = {0,1}\n" + 
			"      )\n" + 
			"      #27 @B(\n" + 
			"        #28 value=(int) 3 (constant type)\n" + 
			"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" + 
			"        type parameter index = 1 type parameter bound index = 1\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type argument method call and generic or array
	public void test032() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	static <T, U> T foo(T t, U u) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void bar() {\n" +
				"		System.out.println(X.<@A String[] @B(1) [], @C('-') X>foo(new String[][]{{\"SUCCESS\"}}, null)[0]);\n" +
				"	}\n" +
				"}\n",
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
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #52 @A(\n" + 
			"        target type = 0x1b TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY\n" + 
			"        offset = 20\n" + 
			"        type argument index = 0\n" + 
			"        locations = {1}\n" + 
			"      )\n" + 
			"      #53 @C(\n" + 
			"        #49 value=\'-\' (constant type)\n" + 
			"        target type = 0x1a TYPE_ARGUMENT_METHOD_CALL\n" + 
			"        offset = 20\n" + 
			"        type argument index = 1\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #48 @B(\n" + 
			"        #49 value=(int) 1 (constant type)\n" + 
			"        target type = 0x1b TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY\n" + 
			"        offset = 20\n" + 
			"        type argument index = 0\n" + 
			"        locations = {0}\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
//	// superclass
//	public void test033() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"Marker.java",
//				"@interface Marker {}",
//				"X.java",
//				"public class X extends @Marker Object {}",
//			},
//			"");
//	}
	// superclass
	public void test034() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Marker.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"@Target(TYPE_PARAMETER)\n" + 
				"@interface Marker {}",
				"X.java",
				"public class X extends @Marker Object {}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X extends @Marker Object {}\n" + 
			"	                       ^^^^^^^\n" + 
			"The annotation @Marker is disallowed for this location\n" + 
			"----------\n");
	}
//	// annotation on catch variable
//	public void test035() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"X.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"public class X {\n" + 
//				"	public static void main(String[] args) {\n" + 
//				"		@A Exception test = new Exception() {\n" +
//				"			private static final long serialVersionUID = 1L;\n" +
//				"			@Override\n" +
//				"			public String toString() {\n" +
//				"				return \"SUCCESS\";\n" +
//				"			}\n" +
//				"		};\n" + 
//				"		try {\n" + 
//				"			System.out.println(test);\n" + 
//				"		} catch(@A Exception e) {\n" + 
//				"			e.printStackTrace();\n" + 
//				"		}\n" + 
//				"	}\n" + 
//				"}",
//				"A.java",
//				"import java.lang.annotation.Target;\n" + 
//				"import static java.lang.annotation.ElementType.*;\n" + 
//				"import java.lang.annotation.Retention;\n" + 
//				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
//				"@Target(TYPE_USE)\n" + 
//				"@Retention(RUNTIME)\n" + 
//				"@interface A {\n" + 
//				"	String value() default \"default\";\n" + 
//				"}\n",
//		},
//		"SUCCESS");
//		String expectedOutput =
//			"    RuntimeVisibleTypeAnnotations: \n" + 
//			"      #44 @A(\n" + 
//			"        target type = 0x8 LOCAL_VARIABLE\n" + 
//			"        local variable entries:\n" + 
//			"          [pc: 8, pc: 24] index: 1\n" + 
//			"      )\n" + 
//			"      #44 @A(\n" + 
//			"        target type = 0x8 LOCAL_VARIABLE\n" + 
//			"        local variable entries:\n" + 
//			"          [pc: 19, pc: 23] index: 2\n" + 
//			"      )\n";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// annotation on catch variable
	public void test036() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.Retention;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"import static java.lang.annotation.RetentionPolicy.*;\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		@B int j = 9;\n" + 
				"		try {\n" + 
				"			System.out.print(\"SUCCESS\" + j);\n" + 
				"		} catch(@A Exception e) {\n" + 
				"		}\n" + 
				"		@B int k = 3;\n" + 
				"		System.out.println(k);\n" + 
				"	}\n" + 
				"}",
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
				"	String value() default \"default\";\n" + 
				"}\n",
		},
		"SUCCESS93");
		String expectedOutput =
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #56 @B(\n" + 
			"        target type = 0x8 LOCAL_VARIABLE\n" + 
			"        local variable entries:\n" + 
			"          [pc: 3, pc: 39] index: 1\n" + 
			"      )\n" + 
			"      #56 @B(\n" + 
			"        target type = 0x8 LOCAL_VARIABLE\n" + 
			"        local variable entries:\n" + 
			"          [pc: 31, pc: 39] index: 2\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// make sure annotation without target appears twice when set on a method declaration
	public void test037() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\r\n" + 
				"import static java.lang.annotation.ElementType.*;\r\n" + 
				"\r\n" + 
				"@Target(METHOD)\r\n" + 
				"@interface Annot {\r\n" + 
				"	int value() default 0;\r\n" + 
				"}\r\n" + 
				"public class X {\r\n" + 
				"	@Annot(4)\r\n" + 
				"	public void foo() {\r\n" + 
				"	}\r\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"  public void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" + 
			"    RuntimeInvisibleAnnotations: \n" + 
			"      #16 @Annot(\n" + 
			"        #17 value=(int) 4 (constant type)\n" + 
			"      )\n" + 
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// make sure annotation without target appears twice when set on a method declaration
	public void test038() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Annot {\r\n" + 
				"	int value() default 0;\r\n" + 
				"}\r\n" + 
				"public class X {\r\n" + 
				"	@Annot(4)\r\n" + 
				"	public void foo() {\r\n" + 
				"	}\r\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  public void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" + 
			"    RuntimeInvisibleAnnotations: \n" + 
			"      #16 @Annot(\n" + 
			"        #17 value=(int) 4 (constant type)\n" + 
			"      )\n" + 
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
//	// make sure annotation without target appears twice when set on a method declaration
//	public void test039() throws Exception {
//		this.runConformTest(
//			new String[] {
//				"X.java",
//				"@interface Annot {\r\n" + 
//				"	int value() default 0;\r\n" + 
//				"}\r\n" + 
//				"public class X {\r\n" + 
//				"	@Annot(4)\r\n" + 
//				"	public int foo() {\r\n" + 
//				"		return 0;\r\n" + 
//				"	}\r\n" + 
//				"}",
//		},
//		"");
//		String expectedOutput =
//			"  public int foo();\n" + 
//			"    0  iconst_0\n" + 
//			"    1  ireturn\n" + 
//			"      Line numbers:\n" + 
//			"        [pc: 0, line: 7]\n" + 
//			"      Local variable table:\n" + 
//			"        [pc: 0, pc: 2] local: this index: 0 type: X\n" + 
//			"    RuntimeInvisibleAnnotations: \n" + 
//			"      #17 @Annot(\n" + 
//			"        #18 value=(int) 4 (constant type)\n" + 
//			"      )\n" + 
//			"    RuntimeInvisibleTypeAnnotations: \n" + 
//			"      #17 @Annot(\n" + 
//			"        #18 value=(int) 4 (constant type)\n" + 
//			"        target type = 0xa METHOD_RETURN_TYPE\n" + 
//			"      )\n" + 
//			"}";
//		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
//	}
	// make sure annotation without target appears twice when set on a method declaration
	public void test040() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\r\n" + 
				"import static java.lang.annotation.ElementType.*;\r\n" + 
				"\r\n" + 
				"@Target(METHOD)\r\n" + 
				"@interface Annot {\r\n" + 
				"	int value() default 0;\r\n" + 
				"}\r\n" + 
				"public class X {\r\n" + 
				"	@Annot(4)\r\n" + 
				"	public int foo() {\r\n" + 
				"		return 0;\r\n" + 
				"	}\r\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"  public int foo();\n" + 
			"    0  iconst_0\n" + 
			"    1  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 2] local: this index: 0 type: X\n" + 
			"    RuntimeInvisibleAnnotations: \n" + 
			"      #17 @Annot(\n" + 
			"        #18 value=(int) 4 (constant type)\n" + 
			"      )\n" + 
			"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
}
