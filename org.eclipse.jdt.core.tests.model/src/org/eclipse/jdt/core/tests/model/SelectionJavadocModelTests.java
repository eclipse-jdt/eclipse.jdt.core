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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class SelectionJavadocModelTests extends AbstractJavaModelTests {

	IJavaElement element;

	public SelectionJavadocModelTests(String name) {
		super(name, 3);
//		this.endChar = "";
		this.displayName = true;
	}

	static {
//		TESTS_PREFIX = "testBug";
//		TESTS_NUMBERS = new int[] { 86380 };
//		TESTS_RANGE = new int[] { 13, 16 };
	}

	public static Test suite() {
		return buildModelTestSuite(SelectionJavadocModelTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setUpJavaProject("Tests", "1.5");
	}
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("Tests");
		super.tearDownSuite();
	}

	void setUnit(String name, String source) throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/"+name, source);
	}

	void assertElementEquals(String message, String expected) {
		assertElementEquals(message, expected, this.element);
	}

	void assertSelectionIsEmpty(ICompilationUnit unit, String selection) throws JavaModelException {
		assertSelectionIsEmpty(unit, selection, 1);
	}

	void assertSelectionIsEmpty(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		int[] selectionPositions = selectionInfo(unit, selection, occurences);
		IJavaElement[] elements = unit.codeSelect(selectionPositions[0], selectionPositions[1]);
		assertTrue("Selection should be empty", elements == null || elements.length == 0);
	}

	public void test01() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** @see #foo() */\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test02() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** {@link #foo() foo} */\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test03() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** @see Test */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		this.element = selectType(this.workingCopies[0], "Test", 2);
		assertElementEquals("Invalid selected type",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test04() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/** Javadoc {@link Test} */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		this.element = selectType(this.workingCopies[0], "Test", 2);
		assertElementEquals("Invalid selected type",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test05() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	int field;\n" +
			"	/** @see #field */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		this.element = selectField(this.workingCopies[0], "field", 2);
		assertElementEquals("Invalid selected field",
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test06() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	int field;\n" +
			"	/**{@link #field}*/\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		this.element = selectField(this.workingCopies[0], "field", 2);
		assertElementEquals("Invalid selected field",
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test07() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Test#field\n" +
			"	 * @see #foo(int, String)\n" +
			"	 * @see Test#foo(int, String)\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(0, \"\");\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(int x, String s) {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[7];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectField(this.workingCopies[0], "field");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "String");
		elements[4] = selectType(this.workingCopies[0], "Test", 3);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[6] = selectType(this.workingCopies[0], "String", 2);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test08() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * First {@link #foo(int, String)}\n" +
			"	 * Second {@link Test#foo(int, String) method foo}\n" +
			"	 * Third {@link Test#field field}\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(0, \"\");\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(int x, String s) {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[7];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectField(this.workingCopies[0], "field");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "String");
		elements[4] = selectType(this.workingCopies[0], "Test", 3);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[6] = selectType(this.workingCopies[0], "String", 2);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test09() throws JavaModelException {
		setUnit("test/junit/Test.java",
			"package test.junit;\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see test.junit.Test\n" +
			"	 * @see test.junit.Test#field\n" +
			"	 * @see test.junit.Test#foo(Object[] array)\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(null);\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(Object[] array) {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[6];
		assertSelectionIsEmpty(this.workingCopies[0], "test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 2);
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "test", 3);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 3);
		elements[1] = selectType(this.workingCopies[0], "Test", 3);
		elements[2] = selectField(this.workingCopies[0], "field");
		assertSelectionIsEmpty(this.workingCopies[0], "test", 4);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 4);
		elements[3] = selectType(this.workingCopies[0], "Test", 4);
		elements[4] = selectMethod(this.workingCopies[0], "foo");
		elements[5] = selectType(this.workingCopies[0], "Object");
		assertSelectionIsEmpty(this.workingCopies[0], "array");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]\n" +
			"Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]\n" +
			"field [in Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]]\n" +
			"Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]\n" +
			"foo(Object[]) [in Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]]\n" +
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test10() throws JavaModelException {
		setUnit("test/junit/Test.java",
			"package test.junit;\n" +
			"public class Test {\n" +
			"	/** Javadoc {@linkplain test.junit.Test}\n" +
			"	 * {@linkplain test.junit.Test#field field}\n" +
			"	 * last line {@linkplain test.junit.Test#foo(Object[] array) foo(Object[])}\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"		foo(null);\n" +
			"	}\n" +
			"	int field;\n" +
			"	void foo(Object[] array) {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[6];
		assertSelectionIsEmpty(this.workingCopies[0], "test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 2);
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "test", 3);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 3);
		elements[1] = selectType(this.workingCopies[0], "Test", 3);
		elements[2] = selectField(this.workingCopies[0], "field");
		assertSelectionIsEmpty(this.workingCopies[0], "test", 4);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 4);
		elements[3] = selectType(this.workingCopies[0], "Test", 4);
		elements[4] = selectMethod(this.workingCopies[0], "foo");
		elements[5] = selectType(this.workingCopies[0], "Object");
		assertSelectionIsEmpty(this.workingCopies[0], "array");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]\n" +
			"Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]\n" +
			"field [in Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]]\n" +
			"Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]\n" +
			"foo(Object[]) [in Test [in [Working copy] Test.java [in test.junit [in <project root> [in Tests]]]]]\n" +
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test11() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @throws RuntimeException runtime exception\n" +
			"	 * @throws InterruptedException interrupted exception\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectType(this.workingCopies[0], "RuntimeException");
		elements[1] = selectType(this.workingCopies[0], "InterruptedException");
		assertElementsEqual("Invalid selection(s)",
			"RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test12() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @exception RuntimeException runtime exception\n" +
			"	 * @exception InterruptedException interrupted exception\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectType(this.workingCopies[0], "RuntimeException");
		elements[1] = selectType(this.workingCopies[0], "InterruptedException");
		assertElementsEqual("Invalid selection(s)",
			"RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test13() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	/**\n" +
			"	 * @param xxx integer param\n" +
			"	 * @param str string param\n" +
			"	 */\n" +
			"	void foo(int xxx, String str) {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectLocalVariable(this.workingCopies[0], "xxx");
		elements[1] = selectLocalVariable(this.workingCopies[0], "str");
		assertElementsEqual("Invalid selection(s)",
			"xxx [in foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"str [in foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test14() throws JavaModelException {
		setUnit("Test.java",
			"/**\n" +
			" * Javadoc of {@link Test}\n" +
			" * @see Field#foo\n" +
			" */\n" +
			"public class Test {}\n" +
			"/**\n" +
			" * Javadoc on {@link Field} to test selection in javadoc field references\n" +
			" * @see #foo\n" +
			" */\n" +
			"class Field {\n" +
			"	/**\n" +
			"	 * Javadoc on {@link #foo} to test selection in javadoc field references\n" +
			"	 * @see #foo\n" +
			"	 * @see Field#foo\n" +
			"	 */\n" +
			"	int foo;\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[9];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Field");
		elements[2] = selectField(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Field", 2);
		elements[4] = selectField(this.workingCopies[0], "foo", 2);
		elements[5] = selectField(this.workingCopies[0], "foo", 3);
		elements[6] = selectField(this.workingCopies[0], "foo", 4);
		elements[7] = selectType(this.workingCopies[0], "Field", 4);
		elements[8] = selectField(this.workingCopies[0], "foo", 5);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]",
			elements
		);
	}

	public void test15() throws JavaModelException {
		setUnit("Test.java",
			"/**\n" +
			" * Javadoc of {@link Test}\n" +
			" * @see Method#foo\n" +
			" */\n" +
			"public class Test {}\n" +
			"/**\n" +
			" * Javadoc on {@link Method} to test selection in javadoc method references\n" +
			" * @see #foo\n" +
			" */\n" +
			"class Method {\n" +
			"	/**\n" +
			"	 * Javadoc on {@link #foo} to test selection in javadoc method references\n" +
			"	 * @see #foo\n" +
			"	 * @see Method#foo\n" +
			"	 */\n" +
			"	void bar() {}\n" +
			"	/**\n" +
			"	 * Method with parameter and throws clause to test selection in javadoc\n" +
			"	 * @param xxx TODO\n" +
			"	 * @param str TODO\n" +
			"	 * @throws RuntimeException blabla\n" +
			"	 * @throws InterruptedException bloblo\n" +
			"	 */\n" +
			"	void foo(int xxx, String str) throws RuntimeException, InterruptedException {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[13];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Method");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Method", 2);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 3);
		elements[6] = selectMethod(this.workingCopies[0], "foo", 4);
		elements[7] = selectType(this.workingCopies[0], "Method", 4);
		elements[8] = selectMethod(this.workingCopies[0], "foo", 5);
		elements[9] = selectLocalVariable(this.workingCopies[0], "xxx");
		elements[10] = selectLocalVariable(this.workingCopies[0], "str");
		elements[11] = selectType(this.workingCopies[0], "RuntimeException");
		elements[12] = selectType(this.workingCopies[0], "InterruptedException");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"xxx [in foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"str [in foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			elements
		);
	}

	public void test16() throws JavaModelException {
		setUnit("Test.java",
			"/**\n" +
			" * Javadoc of {@link Test}\n" +
			" * @see Other\n" +
			" */\n" +
			"public class Test {}\n" +
			"/**\n" +
			" * Javadoc of {@link Other}\n" +
			" * @see Test\n" +
			" */\n" +
			"class Other {}\n"
		);
		IJavaElement[] elements = new IJavaElement[4];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectType(this.workingCopies[0], "Other");
		elements[2] = selectType(this.workingCopies[0], "Test", 3);
		elements[3] = selectType(this.workingCopies[0], "Other", 2);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]",
			elements
		);
	}

	public void test17() throws JavaModelException {
		setUnit("Test.java",
			"/**\n" +
			" * @see Test.Field#foo\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Field#foo\n" +
			"	 */\n" +
			"	class Field {\n" +
			"		/**\n" +
			"		 * @see #foo\n" +
			"		 * @see Field#foo\n" +
			"		 * @see Test.Field#foo\n" +
			"		 */\n" +
			"		int foo;\n" +
			"	}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[11];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Field");
		elements[2] = selectField(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Field", 2);
		elements[4] = selectField(this.workingCopies[0], "foo", 2);
		elements[5] = selectField(this.workingCopies[0], "foo", 3);
		elements[6] = selectType(this.workingCopies[0], "Field", 4);
		elements[7] = selectField(this.workingCopies[0], "foo", 4);
		elements[8] = selectType(this.workingCopies[0], "Test", 3);
		elements[9] = selectType(this.workingCopies[0], "Field", 5);
		elements[10] = selectField(this.workingCopies[0], "foo", 5);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test18() throws JavaModelException {
		setUnit("Test.java",
			"/**\n" +
			" * @see Test.Method#foo()\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Method#foo()\n" +
			"	 */\n" +
			"	class Method {\n" +
			"		/**\n" +
			"		 * @see #foo()\n" +
			"		 * @see Method#foo()\n" +
			"		 * @see Test.Method#foo()\n" +
			"		 */\n" +
			"		void foo() {}\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[11];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Method");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Method", 2);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 3);
		elements[6] = selectType(this.workingCopies[0], "Method", 4);
		elements[7] = selectMethod(this.workingCopies[0], "foo", 4);
		elements[8] = selectType(this.workingCopies[0], "Test", 3);
		elements[9] = selectType(this.workingCopies[0], "Method", 5);
		elements[10] = selectMethod(this.workingCopies[0], "foo", 5);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test19() throws JavaModelException {
		setUnit("Test.java",
			"/**\n" +
			" * @see Test.Other\n" +
			" */\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Test\n" +
			"	 * @see Other\n" +
			"	 * @see Test.Other\n" +
			"	 */\n" +
			"	class Other {}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[6];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectType(this.workingCopies[0], "Other");
		elements[2] = selectType(this.workingCopies[0], "Test", 3);
		elements[3] = selectType(this.workingCopies[0], "Other", 2);
		elements[4] = selectType(this.workingCopies[0], "Test", 4);
		elements[5] = selectType(this.workingCopies[0], "Other", 3);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]",
			elements
		);
	}

	public void test20() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		/**\n" +
			"		 * @see Field#foo\n" +
			"		 */\n" +
			"		class Field {\n" +
			"			/**\n" +
			"			 * @see #foo\n" +
			"			 * @see Field#foo\n" +
			"			 */\n" +
			"			int foo;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Field");
		elements[1] = selectField(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Field", 3);
		elements[4] = selectField(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"foo [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"foo [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"foo [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]",
			elements
		);
	}

	public void test21() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		/**\n" +
			"		 * @see Method#foo()\n" +
			"		 */\n" +
			"		class Method {\n" +
			"			/**\n" +
			"			 * @see #foo()\n" +
			"			 * @see Method#foo()\n" +
			"			 */\n" +
			"			void foo() {}\n" +
			"		}\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Method");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Method", 3);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"foo() [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"foo() [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]\n" +
			"foo() [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]",
			elements
		);
	}

	public void test22() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		/**\n" +
			"		 * @see Test\n" +
			"		 * @see Other\n" +
			"		 */\n" +
			"		class Other {}\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Other");
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test23() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		new Object() {\n" +
			"			/**\n" +
			"			 * @see Field#foo\n" +
			"			 */\n" +
			"			class Field {\n" +
			"				/**\n" +
			"				 * @see #foo\n" +
			"				 * @see Field#foo\n" +
			"				 */\n" +
			"				int foo;\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Field");
		elements[1] = selectField(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Field", 3);
		elements[4] = selectField(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]\n" +
			"foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]\n" +
			"Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]",
			elements
		);
	}

	public void test24() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		new Object() {\n" +
			"			/**\n" +
			"			 * @see Method#foo()\n" +
			"			 */\n" +
			"			class Method {\n" +
			"				/**\n" +
			"				 * @see #foo()\n" +
			"				 * @see Method#foo()\n" +
			"				 */\n" +
			"				void foo() {}\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Method");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Method", 3);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]\n" +
			"foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]\n" +
			"Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]\n" +
			"foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]",
			elements
		);
	}

	public void test25() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	void bar() {\n" +
			"		new Object() {\n" +
			"			/**\n" +
			"			 * @see Test\n" +
			"			 * @see Other\n" +
			"			 */\n" +
			"			class Other {}\n" +
			"		};\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Other");
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"Other [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]",
			elements
		);
	}

	public void test26() throws JavaModelException {
		setUnit("Test.java",
			"public class Test {\n" +
			"	static int field;\n" +
			"	/** \n" +
			"	 * First {@value #field}" +
			"	 * Second {@value Test#field}" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[3];
		elements[0] = selectField(this.workingCopies[0], "field");
		elements[1] = selectType(this.workingCopies[0], "Test");
		elements[2] = selectField(this.workingCopies[0], "field");
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]\n" +
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]\n" +
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * Bug 86380: [1.5][search][annot] Add support to find references inside annotations on a package declaration
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86380"
	 */
	public void testBug86380() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy("/Tests/b86380/package-info.java",
			"/**\n" +
			" * Valid javadoc.\n" +
			" * @see Test\n" +
			" * @see Unknown\n" +
			" * @see Test#foo()\n" +
			" * @see Test#unknown()\n" +
			" * @see Test#field\n" +
			" * @see Test#unknown\n" +
			" * @param unexpected\n" +
			" * @throws unexpected\n" +
			" * @return unexpected \n" +
			" */\n" +
			"package b86380;\n"
		);
		this.workingCopies[1] = getWorkingCopy("/Tests/b86380/Test.java",
			"/**\n" +
			" * Invalid javadoc\n" +
			" */\n" +
			"package b86380;\n" +
			"public class Test {\n" +
			"	public int field;\n" +
			"	public void foo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[3];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "field");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in b86380 [in <project root> [in Tests]]]]\n" +
			"foo() [in Test [in [Working copy] Test.java [in b86380 [in <project root> [in Tests]]]]]\n" +
			"field [in Test [in [Working copy] Test.java [in b86380 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * Bug 90266: [select] Code select returns null when there's a string including a slash on same line
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90266"
	 */
	public void testBug90266_String() throws JavaModelException {
		setUnit("b90266/Test.java",
			"package b90266;\n" +
			"public class Test {\n" +
			"	public int field;\n" +
			"	public void foo(String str, int i) {}\n" +
			"	public void bar() {\n" +
			"		foo(\"String including / (slash)\", this.field)\n" +
			"	}\n" +
			"}\n"
		);
		int[] selectionPositions = selectionInfo(this.workingCopies[0], "field", 2);
		IJavaElement[] elements = this.workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.java [in b90266 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug90266_Char() throws JavaModelException {
		setUnit("b90266/Test.java",
			"package b90266;\n" +
			"public class Test {\n" +
			"	public int field;\n" +
			"	public void foo(Char c, int i) {}\n" +
			"	public void bar() {\n" +
			"		foo('/', this.field)\n" +
			"	}\n" +
			"}\n"
		);
		int[] selectionPositions = selectionInfo(this.workingCopies[0], "field", 2);
		IJavaElement[] elements = this.workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.java [in b90266 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * @bug 165701: [model] No hint for ambiguous javadoc
	 * @test Ensure that no exception is thrown while selecting method in javadoc comment
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165701"
	 */
	public void testBug165701() throws JavaModelException {
		setUnit("b165701/Test.java",
			"package b165701;\n" +
			"/**\n" +
			" * @see #fooo(int)\n" +
			" */\n" +
			"public class Test {\n" +
			"	public void foo() {}\n" +
			"}\n"
		);
		int[] selectionPositions = selectionInfo(this.workingCopies[0], "fooo", 1);
		IJavaElement[] elements = this.workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in b165701 [in <project root> [in Tests]]]]",
			elements
		);
	}

	/**
	 * @bug 165794: [model] No hint for ambiguous javadoc
	 * @test Ensure that no exception is thrown while selecting method in javadoc comment
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165794"
	 */
	public void testBug165794() throws JavaModelException {
		setUnit("b165794/Test.java",
			"package b165794;\n" +
			"/**\n" +
			" * No reasonable hint for resolving the {@link #getMax(A)}.\n" +
			" */\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Extends Number method.\n" +
			"     * @see #getMax(A ipZ)\n" +
			"     */\n" +
			"    public <T extends Y> T getMax(final A<T> ipY) {\n" +
			"        return ipY.t();\n" +
			"    }\n" +
			"    \n" +
			"    /**\n" +
			"     * Extends Exception method.\n" +
			"     * @see #getMax(A ipY)\n" +
			"     */\n" +
			"    public <T extends Z> T getMax(final A<T> ipZ) {\n" +
			"        return ipZ.t();\n" +
			"    }\n" +
			"}\n" +
			"class A<T> {\n" +
			"	T t() { return null; }\n" +
			"}\n" +
			"class Y {}\n" +
			"class Z {}"
		);
		int[] selectionPositions = selectionInfo(this.workingCopies[0], "getMax", 1);
		IJavaElement[] elements = this.workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"getMax(A<T>) [in X [in [Working copy] Test.java [in b165794 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * Bug 171802: [javadoc][select] F3 does not work on method which have deprecated type as argument
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=171802"
	 */
	public void testBug171802() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171802/Y.java",
			"package b171802;\n" +
			"\n" +
			"/**\n" +
			" * @deprecated\n" +
			" */\n" +
			"public class Y {\n" +
			"\n" +
			"}\n"
		);
		this.workingCopies[1] = getWorkingCopy("/Tests/b171802/X.java",
			"package b171802;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @deprecated Use {@link #bar(char[], Y)}\n" +
			"	 * instead\n" +
			"	 */\n" +
			"	void foo(char[] param1, Y param2) {}\n" +
			"\n" +
			"	/**\n" +
			"	 * @deprecated\n" +
			"	 */\n" +
			"	void bar(char[] param1, Y param2) {}\n" +
			"\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[1], "bar");
		assertElementsEqual("Invalid selection(s)",
			"bar(char[], Y) [in X [in [Working copy] X.java [in b171802 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * @bug 191322: [javadoc] @see or @link reference to method without signature fails to resolve to base class method
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=191322"
	 */
	public void testBug191322a() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322b() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"}\n" +
			"class Y extends X {}\n" +
			"class W extends Y {}\n" +
			"class Z extends W {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322c() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public interface X {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 */\n" +
			"	void hoo();\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322d() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public interface X {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface Y extends X {}\n" +
			"abstract class W implements Y {}\n" +
			"abstract class Z extends W {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322e() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"	class Y {\n" +
			"		/**\n" +
			"		 * @see #foo\n" +
			"		 */\n" +
			"		void hoo() {}\n" +
			"	}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322f() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"	void foo(String str) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 3);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322g() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo(String str) {}\n" +
			"	void foo() {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 3);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322h() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo(String str) {}\n" +
			"	void foo(int x) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 3);
		assertElementsEqual("Invalid selection(s)",
			"foo(String) [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322i() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"public class X {\n" +
			"	void foo(String str) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo(String) [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322j1() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"interface X {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface Y {\n" +
			"	void foo(int i);\n" +
			"}\n" +
			"abstract class Z implements X, Y {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 3);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322j2() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"interface X {\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface Y {\n" +
			"	void foo();\n" +
			"}\n" +
			"abstract class Z implements X, Y {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 3);
		assertElementsEqual("Invalid selection(s)",
			"foo() [in Y [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug191322j3() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"package b191322;\n" +
			"interface X {\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface Y {\n" +
			"	void foo(String str);\n" +
			"}\n" +
			"abstract class Z implements X, Y {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "foo", 3);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X [in [Working copy] X.java [in b191322 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171019
	// To verify that inheritDoc tag is recognized as a valid selection and
	// pressing F3 on it navigates to the overriden method with the javadoc according to spec
	// as documented in org.eclipse.jdt.internal.codeassist.SelectionEngine.InheritDocVisitor
	public void testBug171019() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171019/X.java",
			"package b171019;\n" +
			"interface X {\n" +
			"   /**\n" +
			"	 * Main desc of foo..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface Y extends X {\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 */\n" +
			"	void foo(int x);\n\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to Y.foo(String)
			"	 */\n" +
			"	void foo(String s);\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		elements[1] = selectMethod(this.workingCopies[0], "@inheritDoc", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]\n" +
			"foo(String) [in Y [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171019
	// To verify that inheritDoc tag is recognized as a valid selection and
	// pressing F3 on it navigates to the overriden method with the javadoc according to spec
	// as documented in org.eclipse.jdt.internal.codeassist.SelectionEngine.InheritDocVisitor
	public void testBug171019b() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171019/X.java",
			"package b171019;\n" +
			"interface X {\n" +
			"   /**\n" +
			"	 * Main desc of foo..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"class X1 implements X{\n" +
			"	void foo(int x){}\n" +
			"}\n" +
			"class Y extends X1 {\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 */\n" +
			"	void foo(int x);\n\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to Y.foo(String)
			"	 */\n" +
			"	void foo(String s);\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[2];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		elements[1] = selectMethod(this.workingCopies[0], "@inheritDoc", 2);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]\n" +
			"foo(String) [in Y [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171019
	// To verify that inheritDoc tag is recognized as a valid selection and
	// pressing F3 on it navigates to the overriden method with the javadoc according to spec
	// as documented in org.eclipse.jdt.internal.codeassist.SelectionEngine.InheritDocVisitor
	public void testBug171019c() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171019/X.java",
			"package b171019;\n" +
			"interface X1 {\n" +
			"   /**\n" +
			"	 * Main desc of foo in X1..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface X2 {\n" +
			"   /**\n" +
			"	 * Main desc of foo in X2..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"class X implements X1 {\n" +
			"   /**\n" +
			"	 * X desc of foo..\n" +
			"	 */\n" +
			"	void foo(int x){}\n" +
			"}\n" +
			"class Y extends X implements X2 {\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to X2.foo(int)
			"	 */\n" +
			"	void foo(int x);\n\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X2 [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171019
	// To verify that inheritDoc tag is recognized as a valid selection and
	// pressing F3 on it navigates to the overriden method with the javadoc according to spec
	// as documented in org.eclipse.jdt.internal.codeassist.SelectionEngine.InheritDocVisitor
	public void testBug171019d() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171019/X.java",
			"package b171019;\n" +
			"interface X1 {\n" +
			"   /**\n" +
			"	 * Main desc of foo in X1..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface X2 {\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"class X implements X1 {\n" +
			"   /**\n" +
			"	 * X desc of foo..\n" +
			"	 */\n" +
			"	void foo(int x){}\n" +
			"}\n" +
			"class Y extends X implements X2 {\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 */\n" +
			"	void foo(int x);\n\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171019
	// To verify that inheritDoc tag is recognized as a valid selection and
	// pressing F3 on it navigates to the overriden method with the javadoc according to spec
	// as documented in org.eclipse.jdt.internal.codeassist.SelectionEngine.InheritDocVisitor
	public void testBug171019e() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171019/X.java",
			"package b171019;\n" +
			"interface X {\n" +
			"   /**\n" +
			"	 * Main desc of foo..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface Y {\n" +
			"	void foo(String str);\n" +
			"}\n" +
			"abstract class Z implements X, Y {\n" +
			"	/**\n" +
			"	 * {@inheritDoc}\n" +	// navigates to X.foo(int)
			"	 */\n" +
			"	void foo(int x) {\n" +
			"	}\n" +
			"}"
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171019
	// To verify that inheritDoc tag is recognized as a valid selection and
	// pressing F3 on it navigates to the overriden method with the javadoc according to spec
	// as documented in org.eclipse.jdt.internal.codeassist.SelectionEngine.InheritDocVisitor
	// Here the inheritDoc should work when it occurs inside another valid block tag viz.
	// @param, @throws, @exception, @return
	public void testBug171019f() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b171019/X.java",
			"package b171019;\n" +
			"interface X {\n" +
			"   /**\n" +
			"	 * Main desc of foo..\n" +
			"	 */\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"interface Y extends X {\n" +
			"   /**\n" +
			"	 * {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 * @param {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 * @return {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 * @throws {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 * @exception {@inheritDoc}\n" +	// should navigate to X.foo(int)
			"	 */\n" +
			"	void foo(int x);\n\n" +
			"}\n"
		);
		IJavaElement[] elements = new IJavaElement[4];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		elements[1] = selectMethod(this.workingCopies[0], "@inheritDoc", 2);
		elements[2] = selectMethod(this.workingCopies[0], "@inheritDoc", 3);
		elements[3] = selectMethod(this.workingCopies[0], "@inheritDoc", 4);
		assertElementsEqual("Invalid selection(s)",
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]\n" +
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]\n" +
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]\n" +
			"foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400767
	public void testBug400767() throws Exception {
		String content = "package test;\n"
				+ "import b400767.ETest;\n"
				+ "public class Bug {\n"
				+ "	Bug() {\n"
				+ "		doSomethingUsingOtherPackage();\n"
				+ "	}\n"
				+ "	public void addComponentListener(ComponentListener listener) {}\n"
				+ "	private void doSomethingUsingOtherPackage() {\n"
				+ "		for (ETest val : ETest.values()) {\n"
				+ "			System.out.println(val.name());\n"
				+ "		}\n"
				+ "		Bug bug = new Bug();\n"
				+ "		bug.addComponentListener(new ComponentAdapter() {\n"
				+ "			/**\n"
				+ "			 * @see ComponentAdapter#componentShown(ComponentEvent)\n"
				+ "			 */\n"
				+ "			@Override\n"
				+ "			public void componentShown(ComponentEvent e) {\n"
				+ "				super.componentShown(e);\n"
				+ "			}\n"
				+ "		});\n"
				+ "	}\n"
				+ "}\n"
				+ "interface ComponentListener {\n"
				+ "    public void componentShown(ComponentEvent e);\n"
				+ "}\n"
				+ "class ComponentAdapter implements ComponentListener {\n"
				+ "	public void componentShown(ComponentEvent e) { }\n"
				+ "}\n"
				+ "class ComponentEvent {}";
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[2] =  getWorkingCopy("/Tests/test/ETest.java", content);
		content = "/**\n "
				+ "* This package is used by another package and will cause an error.\n"
				+ " */\n"
				+ "package b400767;";
		// package-info is physically required at some point. Just put it to move forward.
		createFolder("/Tests/b400767");
		createFile("/Tests/b400767/package-info.java", content);
		this.workingCopies[0] = getWorkingCopy("/Tests/b400767/package-info.java", content);
		content = "package b400767;\n"
				+ "public enum ETest {\n"
				+ "	VAL1, VAL2, VAL3;\n"
				+ "}";
		this.workingCopies[1] = getWorkingCopy("/Tests/b400767/ETest.java", content);
		final IJavaElement[] selection = new IJavaElement[1];
		final ICompilationUnit[] copy = this.workingCopies;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					selection[0] = selectMethod(copy[2], "componentShown");
				} catch (JavaModelException e) {
					e.printStackTrace();
					fail("Shouldn't be an exception");
				}
			}
		});
		t.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		if (t.isAlive()) {
			fail("Thread shouldn't still be running");
		}
		assertElementEquals("Should return a valid element",
							"componentShown(ComponentEvent) [in ComponentAdapter [in [Working copy] ETest.java [in test [in <project root> [in Tests]]]]]", selection[0]);
	}
}
