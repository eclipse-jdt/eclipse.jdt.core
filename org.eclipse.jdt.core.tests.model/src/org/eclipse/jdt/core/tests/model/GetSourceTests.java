/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GetSourceTests extends ModifyingResourceTests {

	ICompilationUnit cu;

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "TypeParameterBug73884" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 13 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
	}

	public GetSourceTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(GetSourceTests.class);
	}
	/**
	 * Test the field constant
	 */
	private IField getConstantField(String fieldName) {
		IType type = getCompilationUnit("/P/p/Constants.java").getType("Constants");
		IField field = type.getField(fieldName);
		return field;
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P");
		createFolder("/P/p");
		createFile(
			"/P/p/X.java",
			"package p;\n" +
			"import java.lang.*;\n" +
			"public class X {\n" +
			"  public Object field;\n" +
			"  private int s\\u0069ze;\n" +
			"  void foo(String s) {\n" +
			"    final int var1 = 2;\n" +
			"    Object var2;\n" +
			"    for (int i = 0;  i < 10; i++) {}\n" +
			"  }\n" +
			"  private int bar() {\n" +
			"    return 1;\n" +
			"  }\n" +
			"  /**\n" +
			"   * Returns the size.\n" +
			"   * @return\n" +
			"   *     the size\n" +
			"   */\n" +
			"  int getSiz\\u0065 () {\n" +
			"    return this.size;\n" +
			"  }\n" +
			"}"
		);
		this.cu = getCompilationUnit("/P/p/X.java");
		String cuSource =
			"package p;\n" +
			"public class Constants {\n" +
			"  static final long field1 = 938245798324893L;\n" +
			"  static final long field2 = 938245798324893l;\n" +
			"  static final long field3 = 938245798324893;\n" +
			"  static final char field4 = ' ';\n" +
			"  static final double field5 = 938245798324893D;\n" +
			"  static final float field6 = 123456f;\n" +
			"  static final String field7 = \"simple string\";\n" +
			"  static final java.lang.String field8 = \"qualified string\";\n" +
			"  static final int field9 = 1<<0;\n" +
			"}";
		createFile("/P/p/Constants.java", cuSource);
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}

	/**
	 * Ensure the source for a field contains the modifiers, field
	 * type, name, and terminator.
	 */
	public void testField() throws JavaModelException {
		IType type = this.cu.getType("X");
		IField field = type.getField("field");

		String actualSource = field.getSource();
		String expectedSource = "public Object field;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	public void testFieldConstant01() throws CoreException {
		IField field = getConstantField("field1");

		Object constant = field.getConstant();
		Long value = (Long) constant;
		assertEquals("Wrong value", 938245798324893l, value.longValue());
	}

	public void testFieldConstant02() throws CoreException {
		IField field = getConstantField("field2");

		Object constant = field.getConstant();
		Long value = (Long) constant;
		assertEquals("Wrong value", 938245798324893l, value.longValue());
	}

	public void testFieldConstant03() throws CoreException {
		IField field = getConstantField("field3");

		Object constant = field.getConstant();
		Long value = (Long) constant;
		assertEquals("Wrong value", 938245798324893l, value.longValue());
	}

	public void testFieldConstant04() throws CoreException {
		IField field = getConstantField("field4");

		Object constant = field.getConstant();
		Character character = (Character) constant;
		assertEquals("Wrong value", ' ', character.charValue());
	}

	public void testFieldConstant05() throws CoreException {
		IField field = getConstantField("field5");

		Object constant = field.getConstant();
		Double double1 = (Double) constant;
		assertEquals("Wrong value", 938245798324893l, double1.doubleValue(), 0.01);
	}

	public void testFieldConstant06() throws CoreException {
		IField field = getConstantField("field6");

		Object constant = field.getConstant();
		Float float1 = (Float) constant;
		assertEquals("Wrong value", 123456, float1.floatValue(), 0.01f);
	}

	public void testFieldConstant07() throws CoreException {
		IField field = getConstantField("field7");
		assertEquals("Wrong value", "\"simple string\"", field.getConstant());
	}

	public void testFieldConstant08() throws CoreException {
		IField field = getConstantField("field8");
		assertEquals("Wrong value", "\"qualified string\"", field.getConstant());
	}

	public void testFieldConstant09() throws CoreException {
		IField field = getConstantField("field9");

		Object constant = field.getConstant();
		assertNull("Should not be a constant", constant);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406836
	public void testFieldConstants406836() throws CoreException {
		try {
			String cuSource =
					"package p;\n" +
					"public class A{\n" +
					"    final long CONST = 1;\n" +
					"    final long NON_TRIVIAL_INITIALIZER_NON_CONST = 2<<8;\n" +
					"    static long STATIC_NOT_CONST = 3;\n" +
					"    final int NON_COMPILE_TIME_CONSTANT = new Integer(4).intValue();\n" +
					"}\n" +
					"interface B{\n" +
					"	final long CONST = 1;\n" +
					"	final long NON_TRIVIAL_INITIALIZER_NON_CONST = 2<<8;\n" +
					"	static long STATIC_NOT_CONST = 3;\n" +
					"	final int NON_COMPILE_TIME_CONSTANT = new Integer(4).intValue();\n" +
					"}\n";
			createFile("/P/p/A.java", cuSource);
			IType type = getCompilationUnit("/P/p/A.java").getType("A");

			Object constant = type.getField("CONST").getConstant();
			Long value = (Long) constant;
			assertEquals("Wrong value", 1, value.intValue());

			constant = type.getField("NON_TRIVIAL_INITIALIZER_NON_CONST").getConstant();
			assertNull("Should not be a constant", constant);

			constant = type.getField("STATIC_NOT_CONST").getConstant();
			assertNull("Should not be a constant", constant);

			constant = type.getField("NON_COMPILE_TIME_CONSTANT").getConstant();
			assertNull("Should not be a constant", constant);

			type = getCompilationUnit("/P/p/A.java").getType("B");

			constant = type.getField("CONST").getConstant();
			value = (Long) constant;
			assertEquals("Wrong value", 1, value.intValue());

			constant = type.getField("NON_TRIVIAL_INITIALIZER_NON_CONST").getConstant();
			assertNull("Should not be a constant", constant);

			constant = type.getField("STATIC_NOT_CONST").getConstant();
			value = (Long) constant;
			assertEquals("Wrong value", 3, value.intValue());

			constant = type.getField("NON_COMPILE_TIME_CONSTANT").getConstant();
			assertNull("Should not be a constant", constant);
		} finally {
			deleteFile("/P/p/A.java");
		}
	}

	/*
	 * Ensures that the Javadoc range for a method is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232944 )
	 */
	public void testJavadocRange01() throws CoreException {
		try {
			String cuSource =
				"package p;\n" +
				"class A{\n" +
				"    /**\n" +
				"     * swsw\n" +
				"     */\n" +
				"    void m(){\n" +
				"    }\n" +
				"}";
			createFile("/P/p/A.java", cuSource);
			IMethod method = getCompilationUnit("/P/p/A.java").getType("A").getMethod("m", new String[0]);
			assertSourceEquals(
				"Unexpected Javadoc'",
				"/**\n" +
				"     * swsw\n" +
				"     */",
				getSource(cuSource, method.getJavadocRange()));
		} finally {
			deleteFile("/P/p/A.java");
		}
	}

	/*
	 * Ensures that the Javadoc range for a class is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232944 )
	 */
	public void testJavadocRange02() throws CoreException {
		try {
			String cuSource =
				"package p;\n" +
				"/** X */class A {}";
			createFile("/P/p/A.java", cuSource);
			IType type = getCompilationUnit("/P/p/A.java").getType("A");
			assertSourceEquals(
				"Unexpected Javadoc'",
				"/** X */",
				getSource(cuSource, type.getJavadocRange()));
		} finally {
			deleteFile("/P/p/A.java");
		}
	}

	/**
	 * Ensure the source for an import contains the 'import' keyword,
	 * name, and terminator.
	 */
	public void testImport() throws JavaModelException {
		IImportDeclaration i = this.cu.getImport("java.lang.*");

		String actualSource = i.getSource();
		String expectedSource = "import java.lang.*;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable1() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "var1 = 2;", "var1");

		String actualSource = var.getSource();
		String expectedSource = "final int var1 = 2;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable2() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "var2;", "var2");

		String actualSource = var.getSource();
		String expectedSource = "Object var2;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable3() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "i = 0;", "i");

		String actualSource = var.getSource();
		String expectedSource = "int i = 0"; // semi-colon is not part of the local declaration in a for statement
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable4() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "s) {", "s");

		String actualSource = var.getSource();
		String expectedSource = "String s";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/**
	 * Ensure the source for a method contains the modifiers, return
	 * type, selector, and terminator.
	 */
	public void testMethod() throws JavaModelException {
		IType type = this.cu.getType("X");
		IMethod method= type.getMethod("bar", new String[0]);

		String actualSource = method.getSource();
		String expectedSource =
			"private int bar() {\n" +
			"    return 1;\n" +
			"  }";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures that the name range for an annotation is correct.
	 */
	public void testNameRange01() throws CoreException { // was testAnnotationNameRange1
		try {
			String cuSource =
				"package p;\n" +
				"@ MyAnnot (1)\n" +
				"public class Y {\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IAnnotation annotation = getCompilationUnit("/P/p/Y.java").getType("Y").getAnnotation("MyAnnot");
			assertSourceEquals(
				"Unexpected source'",
				"MyAnnot",
				getNameSource(cuSource, annotation));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures that the name range for an annotation is correct.
	 */
	public void testNameRange02() throws CoreException { // was testAnnotationNameRange2
		try {
			String cuSource =
				"package p;\n" +
				"@x.  y  .  z.MyAnnot (1)\n" +
				"public class Y {\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IAnnotation annotation = getCompilationUnit("/P/p/Y.java").getType("Y").getAnnotation("x.y.z.MyAnnot");
			assertSourceEquals(
				"Unexpected source'",
				"x.  y  .  z.MyAnnot",
				getNameSource(cuSource, annotation));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures that the name range for an annotation on a local variable is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=209823)
	 */
	public void testNameRange03() throws CoreException { // was testAnnotationNameRange3
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    @MyAnnot int local;\n" +
				"  }\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IAnnotation annotation = getLocalVariable(getCompilationUnit("/P/p/Y.java"), "local", "local").getAnnotation("MyAnnot");
			assertSourceEquals(
				"Unexpected source'",
				"MyAnnot",
				getNameSource(cuSource, annotation));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the name range for an anonymous class is correct.
	 * (regression test for bug 44450 Strange name range for anonymous classes)
	 */
	public void testNameRange04() throws CoreException { // was testNameRangeAnonymous
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    Y y = new Y() {};\n" +
				"    class C {\n" +
				"    }\n"+
				"  }\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IType anonymous = getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("foo", new String[0]).getType("", 1);

			String actualSource = getNameSource(cuSource, anonymous);
			String expectedSource = "Y";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the name range for a type parameter is correct.
	 */
	public void testNameRange05() throws CoreException { // was testNameRangeTypeParameter1
		try {
			String cuSource =
				"package p;\n" +
				"public class Y<T extends String> {\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			ITypeParameter typeParameter = getCompilationUnit("/P/p/Y.java").getType("Y").getTypeParameter("T");
			assertSourceEquals(
				"Unexpected source'",
				"T",
				getNameSource(cuSource, typeParameter));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the name range for a type parameter is correct.
	 */
	public void testNameRange06() throws CoreException { // was testNameRangeTypeParameter2
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  <T extends String, U extends StringBuffer & Runnable> void foo() {} \n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			ITypeParameter typeParameter = getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("foo", new String[0]).getTypeParameter("U");
			assertSourceEquals(
				"Unexpected source'",
				"U",
				getNameSource(cuSource, typeParameter));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the name range for a method with syntax errors in its header is correct.
	 * (regression test for bug 43139 Delete member in Outliner not working)
	 */
	public void testNameRange07() throws CoreException { // was testNameRangeMethodWithSyntaxError
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void static bar() {}\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IMethod method= getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("bar", new String[0]);

			String actualSource = getNameSource(cuSource, method);
			String expectedSource = "bar";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the name range for an anonymous enum constant is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=226313 )
	 */
	public void testNameRange08() throws CoreException {
		try {
			createJavaProject("P15", new String[] {""}, new String[0], "", CompilerOptions.getFirstSupportedJavaVersion());
			String cuSource =
				"public enum X {\n" +
				"  GREEN() {\n" +
				"  };\n" +
				"}";
			createFile("/P15/X.java", cuSource);
			IType type = getCompilationUnit("/P15/X.java").getType("X").getField("GREEN").getType("", 1);
			assertSourceEquals(
				"Unexpected source'",
				"GREEN",
				getNameSource(cuSource, type));
		} finally {
			deleteProject("P15");
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=150980
	 */
	public void testNameRange09() throws CoreException { // was testNameRangeAnonymous
		try {
			String cuSource =
				"package p . q . r. s ;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    Y y = new Y() {};\n" +
				"    class C {\n" +
				"    }\n"+
				"  }\n" +
				"}";
			createFolder("/P/p/q/r/s/");
			createFile("/P/p/q/r/s/Y.java", cuSource);
			final IPackageDeclaration[] packageDeclarations = getCompilationUnit("/P/p/q/r/s/Y.java").getPackageDeclarations();
			assertEquals("Wrong size", 1, packageDeclarations.length);

			String actualSource = getNameSource(cuSource, packageDeclarations[0]);
			String expectedSource = "p . q . r. s";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/p/q/r/s/Y.java");
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=150980
	 */
	public void testNameRange10() throws CoreException { // was testNameRangeAnonymous
		try {
			String cuSource =
				"import java . lang . * ;\n" +
				"public class Y {\n" +
				"}";
			createFile("/P/Y.java", cuSource);
			final IImportDeclaration[] imports = getCompilationUnit("/P/Y.java").getImports();
			assertEquals("Wrong size", 1, imports.length);

			String actualSource = getNameSource(cuSource, imports[0]);
			String expectedSource = "java . lang . *";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/Y.java");
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=150980
	 */
	public void testNameRange11() throws CoreException { // was testNameRangeAnonymous
		try {
			String cuSource =
				"import java . lang  .  Object  ;\n" +
				"public class Y {\n" +
				"}";
			createFile("/P/Y.java", cuSource);
			final IImportDeclaration[] imports = getCompilationUnit("/P/Y.java").getImports();
			assertEquals("Wrong size", 1, imports.length);

			String actualSource = getNameSource(cuSource, imports[0]);
			String expectedSource = "java . lang  .  Object";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/Y.java");
		}
	}

	/*
	 * Ensures that the source range for an annotation on a local variable is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=209823)
	 */
	public void testSourceRange01() throws CoreException { // was testAnnotationSourceRange
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    @MyAnnot int local;\n" +
				"  }\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IAnnotation annotation = getLocalVariable(getCompilationUnit("/P/p/Y.java"), "local", "local").getAnnotation("MyAnnot");
			assertSourceEquals(
				"Unexpected source'",
				"@MyAnnot",
				getSource(cuSource, annotation.getSourceRange()));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures that the source range for an anonymous type is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=207775)
	 */
	public void testSourceRange02() throws CoreException { // was testAnonymousSourceRange
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    new Object() {};\n" +
				"  }\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IType type = getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("foo", new String[0]).getType("", 1);
			assertSourceEquals(
				"Unexpected source'",
				"new Object() {}",
				getSource(cuSource, type.getSourceRange()));
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the source range for an anonymous enum constant is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=226313 )
	 */
	public void testSourceRange03() throws CoreException {
		try {
			createJavaProject("P15", new String[] {""}, new String[0], "", CompilerOptions.getFirstSupportedJavaVersion());
			String cuSource =
				"public enum X {\n" +
				"  GREEN() {};\n" +
				"}";
			createFile("/P15/X.java", cuSource);
			IType type = getCompilationUnit("/P15/X.java").getType("X").getField("GREEN").getType("", 1);
			assertSourceEquals(
				"Unexpected source'",
				"GREEN() {}",
				getSource(cuSource, type.getSourceRange()));
		} finally {
			deleteProject("P15");
		}
	}

	/*
	 * Ensures the source for a type parameter is correct.
	 */
	public void testTypeParameter1() throws CoreException {
		try {
			String cuSource =
				"package p;\n" +
				"public class Y<T extends String> {\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			ITypeParameter typeParameter = getCompilationUnit("/P/p/Y.java").getType("Y").getTypeParameter("T");
			assertSourceEquals(
				"Unexpected source'",
				"T extends String",
				typeParameter.getSource());
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/*
	 * Ensures the source for a type parameter is correct.
	 */
	public void testTypeParameter2() throws CoreException {
		try {
			String cuSource =
				"package p;\n" +
				"public class Y {\n" +
				"  <T extends String, U extends StringBuffer & Runnable> void foo() {} \n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			ITypeParameter typeParameter = getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("foo", new String[0]).getTypeParameter("U");
			assertSourceEquals(
				"Unexpected source'",
				"U extends StringBuffer & Runnable",
				typeParameter.getSource());
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}

	/**
	 * Ensure the source for a field contains the modifiers, field
	 * type, name, and terminator, and unicode characters.
	 */
	public void testUnicodeField() throws JavaModelException {
		IType type = this.cu.getType("X");
		IField field = type.getField("size");

		String actualSource = field.getSource();
		String expectedSource = "private int s\\u0069ze;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/**
	 * Ensure the source for a field contains the modifiers, field
	 * type, name, and terminator, and unicode characters.
	 */
	public void testUnicodeMethod() throws JavaModelException {
		IType type = this.cu.getType("X");
		IMethod method= type.getMethod("getSize", null);

		String actualSource = method.getSource();
		String expectedSource =
			"/**\n" +
			"   * Returns the size.\n" +
			"   * @return\n" +
			"   *     the size\n" +
			"   */\n" +
			"  int getSiz\\u0065 () {\n" +
			"    return this.size;\n" +
			"  }";
		assertSourceEquals("Unexpected source", expectedSource, actualSource);
	}
}
