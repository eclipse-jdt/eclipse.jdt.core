/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

public class SelectionJavadocModelTests extends AbstractJavaModelTests {
	
	IJavaElement element;

	public SelectionJavadocModelTests(String name) {
		super(name, 3);
//		this.endChar = "";
		this.displayName = true;
	}

	static {
//		TESTS_NUMBERS = new int[] { 90266 };
//		TESTS_RANGE = new int[] { 13, 16 };
	}

	public static Test suite() {
		return buildTestSuite(SelectionJavadocModelTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();	
		setUpJavaProject("Tests", "1.5");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("Tests");
		super.tearDownSuite();
	}

	void setUnit(String name, String source) throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/src/"+name, source);
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
		"foo() {key=LTest;.foo()V} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]"
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
			"foo() {key=LTest;.foo()V} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]"
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]"
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]"
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
			"field {key=LTest;.field} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]"
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
			"field {key=LTest;.field} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]"
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"field {key=LTest;.field} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo(int, String) {key=LTest;.foo(ILjava/lang/String;)V} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"String {key=Ljava/lang/String;} [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo(int, String) {key=LTest;.foo(ILjava/lang/String;)V} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"String {key=Ljava/lang/String;} [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"field {key=LTest;.field} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo(int, String) {key=LTest;.foo(ILjava/lang/String;)V} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"String {key=Ljava/lang/String;} [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo(int, String) {key=LTest;.foo(ILjava/lang/String;)V} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"String {key=Ljava/lang/String;} [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"Test {key=Ltest/junit/Test;} [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]\n" + 
			"Test {key=Ltest/junit/Test;} [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]\n" + 
			"field {key=Ltest/junit/Test;.field} [in Test [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]]\n" + 
			"Test {key=Ltest/junit/Test;} [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]\n" + 
			"foo(Object[]) {key=Ltest/junit/Test;.foo([Ljava/lang/Object;)V} [in Test [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]]\n" + 
			"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"Test {key=Ltest/junit/Test;} [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]\n" + 
			"Test {key=Ltest/junit/Test;} [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]\n" + 
			"field {key=Ltest/junit/Test;.field} [in Test [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]]\n" + 
			"Test {key=Ltest/junit/Test;} [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]\n" + 
			"foo(Object[]) {key=Ltest/junit/Test;.foo([Ljava/lang/Object;)V} [in Test [in [Working copy] Test.java [in test.junit [in src [in Tests]]]]]\n" + 
			"Object {key=Ljava/lang/Object;} [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"RuntimeException {key=Ljava/lang/RuntimeException;} [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"InterruptedException {key=Ljava/lang/InterruptedException;} [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"RuntimeException {key=Ljava/lang/RuntimeException;} [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"InterruptedException {key=Ljava/lang/InterruptedException;} [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"xxx [in foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"str [in foo(int, String) [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]",
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
			"Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Field {key=LTest~Field;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo {key=LTest~Field;.foo} [in Field [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Field {key=LTest~Field;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo {key=LTest~Field;.foo} [in Field [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo {key=LTest~Field;.foo} [in Field [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo {key=LTest~Field;.foo} [in Field [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Field {key=LTest~Field;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo {key=LTest~Field;.foo} [in Field [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]",
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
			"Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Method {key=LTest~Method;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo(int, String) {key=LTest~Method;.foo(ILjava/lang/String;)V} [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Method {key=LTest~Method;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo(int, String) {key=LTest~Method;.foo(ILjava/lang/String;)V} [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo(int, String) {key=LTest~Method;.foo(ILjava/lang/String;)V} [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo(int, String) {key=LTest~Method;.foo(ILjava/lang/String;)V} [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Method {key=LTest~Method;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"foo(int, String) {key=LTest~Method;.foo(ILjava/lang/String;)V} [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"xxx [in foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"str [in foo(int, String) [in Method [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"RuntimeException {key=Ljava/lang/RuntimeException;} [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"InterruptedException {key=Ljava/lang/InterruptedException;} [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest~Other;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest~Other;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]",
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
			"Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Field {key=LTest$Field;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo {key=LTest$Field;.foo} [in Field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"Field {key=LTest$Field;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo {key=LTest$Field;.foo} [in Field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"foo {key=LTest$Field;.foo} [in Field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"Field {key=LTest$Field;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo {key=LTest$Field;.foo} [in Field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Field {key=LTest$Field;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo {key=LTest$Field;.foo} [in Field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]",
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
			"Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Method {key=LTest$Method;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo() {key=LTest$Method;.foo()V} [in Method [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"Method {key=LTest$Method;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo() {key=LTest$Method;.foo()V} [in Method [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"foo() {key=LTest$Method;.foo()V} [in Method [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"Method {key=LTest$Method;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo() {key=LTest$Method;.foo()V} [in Method [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Method {key=LTest$Method;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"foo() {key=LTest$Method;.foo()V} [in Method [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]",
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest$Other;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest$Other;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest$Other;} [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]",
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
			"Field {key=LTest$74;} [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"foo {key=LTest$74;.foo} [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"foo {key=LTest$74;.foo} [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"Field {key=LTest$74;} [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"foo {key=LTest$74;.foo} [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]",
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
			"Method {key=LTest$77;} [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"foo() {key=LTest$77;.foo()V} [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"foo() {key=LTest$77;.foo()V} [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"Method {key=LTest$77;} [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]\n" + 
			"foo() {key=LTest$77;.foo()V} [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]",
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest$85;} [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]",
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
			"Field {key=LTest$95;} [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"foo {key=LTest$95;.foo} [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]]\n" + 
			"foo {key=LTest$95;.foo} [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]]\n" + 
			"Field {key=LTest$95;} [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"foo {key=LTest$95;.foo} [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]]",
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
			"Method {key=LTest$98;} [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"foo() {key=LTest$98;.foo()V} [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]]\n" + 
			"foo() {key=LTest$98;.foo()V} [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]]\n" + 
			"Method {key=LTest$98;} [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]\n" + 
			"foo() {key=LTest$98;.foo()V} [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]]",
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
			"Test {key=LTest;} [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"Other {key=LTest$107;} [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]]]",
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
			"field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]\n" + 
			"Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]\n" + 
			"field [in Test [in [Working copy] Test.java [in <default> [in src [in Tests]]]]]",
			elements
		);
	}

	/**
	 * Bug 90266: [select] Code select returns null when there's a string including a slash on same line
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90266"
	 */
	public void testBug90266_String() throws JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/Tests/b90266/Test.java",
			"package b90266;\n" + 
			"public class Test {\n" + 
			"	public int field;\n" + 
			"	public void foo(String str, int i) {}\n" +
			"	public void bar() {\n" + 
			"		foo(\"String including / (slash)\", this.field)\n" + 
			"	}\n" + 
			"}\n"
		);
		int[] selectionPositions = selectionInfo(workingCopies[0], "field", 2);
		IJavaElement[] elements = workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field {key=LTest;.field} [in Test [in [Working copy] Test.java [in <default> [in b90266 [in Tests]]]]]",
			elements
		);
	}
	public void testBug90266_Char() throws JavaModelException {
		workingCopies = new ICompilationUnit[1];
		workingCopies[0] = getWorkingCopy("/Tests/b90266/Test.java",
			"package b90266;\n" + 
			"public class Test {\n" + 
			"	public int field;\n" + 
			"	public void foo(Char c, int i) {}\n" +
			"	public void bar() {\n" + 
			"		foo('/', this.field)\n" + 
			"	}\n" + 
			"}\n"
		);
		int[] selectionPositions = selectionInfo(workingCopies[0], "field", 2);
		IJavaElement[] elements = workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field {key=LTest;.field} [in Test [in [Working copy] Test.java [in <default> [in b90266 [in Tests]]]]]",
			elements
		);
	}
}
