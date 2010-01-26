/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
//		TESTS_NUMBERS = new int [] { 9 };
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
	// superclass
	public void test001() throws Exception {
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
		String expectedOutput =
			"  RuntimeInvisibleTypeAnnotations: \n" + 
			"    #17 @Marker(\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = -1\n" + 
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type parameter
	public void test002() throws Exception {
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
		String expectedOutput =
			"  RuntimeInvisibleTypeAnnotations: \n" + 
			"    #21 @Marker(\n" + 
			"      target type = 0x22 CLASS_TYPE_PARAMETER\n" + 
			"      type parameter index = 0\n" + 
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// superclass
	public void test003() throws Exception {
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
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" + 
			"    #19 @A(\n" + 
			"      #20 id=\"Hello, World!\" (constant type)\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = -1\n" + 
			"    )\n" + 
			"    #22 @C(\n" + 
			"      #23 value=\'(\' (constant type)\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = -1\n" + 
			"    )\n" + 
			"  RuntimeInvisibleTypeAnnotations: \n" + 
			"    #17 @B(\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = -1\n" + 
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// super interfaces
	public void test004() throws Exception {
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
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" + 
			"    #23 @A(\n" + 
			"      #24 id=\"Hello, World!\" (constant type)\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = 0\n" + 
			"    )\n" + 
			"    #26 @C(\n" + 
			"      #27 value=\'(\' (constant type)\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = 1\n" + 
			"    )\n" + 
			"  RuntimeInvisibleTypeAnnotations: \n" + 
			"    #21 @B(\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = 1\n" + 
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// class literal
	public void test005() throws Exception {
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
				"	public boolean foo(String s) {\n" + 
				"		boolean b = (s instanceof @C('_') Object);\n" + 
				"		Object o = new @B(3) @A(\"new Object\") Object();\n" + 
				"		Class<?> c = @B(4) Object.class;\n" + 
				"		Class<?> c2 = @A(\"int class literal\")  @B(5) int.class;\n" + 
				"		System.out.println(o.toString() + c.toString() + c2.toString());\n" + 
				"		return b;\n" + 
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #73 @C(\n" + 
			"        #68 value=\'_\' (constant type)\n" + 
			"        target type = 0x2 TYPE_INSTANCEOF\n" + 
			"        offset = 1\n" + 
			"      )\n" + 
			"      #75 @A(\n" + 
			"        #68 value=\"new Object\" (constant type)\n" + 
			"        target type = 0x4 OBJECT_CREATION\n" + 
			"        offset = 5\n" + 
			"      )\n" + 
			"      #75 @A(\n" + 
			"        #68 value=\"int class literal\" (constant type)\n" + 
			"        target type = 0x1e CLASS_LITERAL\n" + 
			"        offset = 17\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #67 @B(\n" + 
			"        #68 value=(int) 3 (constant type)\n" + 
			"        target type = 0x4 OBJECT_CREATION\n" + 
			"        offset = 5\n" + 
			"      )\n" + 
			"      #67 @B(\n" + 
			"        #68 value=(int) 4 (constant type)\n" + 
			"        target type = 0x1e CLASS_LITERAL\n" + 
			"        offset = 13\n" + 
			"      )\n" + 
			"      #67 @B(\n" + 
			"        #68 value=(int) 5 (constant type)\n" + 
			"        target type = 0x1e CLASS_LITERAL\n" + 
			"        offset = 17\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// class literal generic and array
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
				"	public boolean foo(Object o) {\n" + 
				"		boolean b = (o instanceof @C('_') Object[]);\n" + 
				"		Object o1 = new @B(3) @A(\"new Object\") Object[] {};\n" + 
				"		Class<?> c = @B(4) Object[].class;\n" + 
				"		Class<?> c2 = @A(\"int class literal\")  @B(5) int[].class;\n" + 
				"		System.out.println(o1.toString() + c.toString() + c2.toString());\n" + 
				"		return b;\n" + 
				"	}\n" + 
				"}",
		},
		"");
		String expectedOutput =
			"    RuntimeVisibleTypeAnnotations: \n" + 
			"      #70 @C(\n" + 
			"        #66 value=\'_\' (constant type)\n" + 
			"        target type = 0x2 TYPE_INSTANCEOF\n" + 
			"        offset = 1\n" + 
			"      )\n" + 
			"      #72 @A(\n" + 
			"        #66 value=\"int class literal\" (constant type)\n" + 
			"        target type = 0x1e CLASS_LITERAL\n" + 
			"        offset = 14\n" + 
			"      )\n" + 
			"    RuntimeInvisibleTypeAnnotations: \n" + 
			"      #65 @B(\n" + 
			"        #66 value=(int) 4 (constant type)\n" + 
			"        target type = 0x1e CLASS_LITERAL\n" + 
			"        offset = 10\n" + 
			"      )\n" + 
			"      #65 @B(\n" + 
			"        #66 value=(int) 5 (constant type)\n" + 
			"        target type = 0x1e CLASS_LITERAL\n" + 
			"        offset = 14\n" + 
			"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// parameterized superclass
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
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" + 
			"    #21 @A(\n" + 
			"      #22 value=\"Hello, World!\" (constant type)\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = -1\n" + 
			"    )\n" + 
			"    #24 @C(\n" + 
			"      #22 value=\'(\' (constant type)\n" + 
			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
			"      type index = -1\n" + 
			"      locations = {0}\n" + 
			"    )\n" + 
			"  RuntimeInvisibleTypeAnnotations: \n" + 
			"    #19 @B(\n" + 
			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
			"      type index = -1\n" + 
			"      locations = {0}\n" + 
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
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
				"interface J<U,T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>, @B J<String, @C('(') Integer> {}",
		},
		"");
		String expectedOutput =
			"  RuntimeVisibleTypeAnnotations: \n" + 
			"    #25 @A(\n" + 
			"      #26 value=\"Hello, World!\" (constant type)\n" + 
			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
			"      type index = 0\n" + 
			"      locations = {0}\n" + 
			"    )\n" + 
			"    #28 @C(\n" + 
			"      #26 value=\'(\' (constant type)\n" + 
			"      target type = 0x15 CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY\n" + 
			"      type index = 1\n" + 
			"      locations = {1}\n" + 
			"    )\n" + 
			"  RuntimeInvisibleTypeAnnotations: \n" + 
			"    #23 @B(\n" + 
			"      target type = 0x14 CLASS_EXTENDS_IMPLEMENTS\n" + 
			"      type index = 1\n" + 
			"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
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
	// method receiver
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
				"X.java",
				"public class X {\n" + 
				"	void foo() @B(3) {}\n" + 
				"}",
		},
		"");
	}
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
				"	@B(3) int foo() {\n" +
				"		return 1;\n" +
				"	}\n" + 
				"}",
		},
		"");
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
				"	@B(3) int field;\n" +
				"}",
		},
		"");
	}
	// method parameter
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
				"	int foo(@B(3) String s) {\n" +
				"		return s.length();\n" +
				"	}\n" + 
				"}",
		},
		"");
	}
	// method parameter generic or array
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
				"	int foo(String @B(3) [] s) {\n" +
				"		return s.length;\n" +
				"	}\n" + 
				"}",
		},
		"");
	}
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
				"	int @B(3) [] field;\n" +
				"}",
		},
		"");
	}
	// class type parameter
	public void test016() throws Exception {
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
	}
	// class type parameter bound
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
				"public class X<T extends @A String & @B(3) Cloneable> {}",
		},
		"");
	}
	// class type parameter bound generic or array
	public void test019() throws Exception {
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
				"public class Y<T> {}",
				"X.java",
				"public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"");
	}
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
				"	<T> X(T t) {\n" + 
				"	}\n" + 
				"	public Object foo() {\n" + 
				"		X x = new <@A @B(1) String>X(null);\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}",
		},
		"");
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
				"	<T> X(T t) {\n" + 
				"	}\n" + 
				"	public Object foo() {\n" + 
				"		X x = new <@A @B(1) String>X(null);\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}",
		},
		"");
	}
	// type argument method call and generic or array
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
	}
}
