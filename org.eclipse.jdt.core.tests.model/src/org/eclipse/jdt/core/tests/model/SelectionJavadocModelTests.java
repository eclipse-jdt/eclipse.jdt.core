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
			"""
				public class Test {
					/** @see #foo() */
					void bar() {
						foo();
					}
					void foo() {}
				}
				"""
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test02() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					/** {@link #foo() foo} */
					void bar() {
						foo();
					}
					void foo() {}
				}
				"""
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test03() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					/** @see Test */
					void foo() {}
				}
				"""
		);
		this.element = selectType(this.workingCopies[0], "Test", 2);
		assertElementEquals("Invalid selected type",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test04() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					/** Javadoc {@link Test} */
					void foo() {}
				}
				"""
		);
		this.element = selectType(this.workingCopies[0], "Test", 2);
		assertElementEquals("Invalid selected type",
			"Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test05() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					int field;
					/** @see #field */
					void foo() {}
				}
				"""
		);
		this.element = selectField(this.workingCopies[0], "field", 2);
		assertElementEquals("Invalid selected field",
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test06() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					int field;
					/**{@link #field}*/
					void foo() {}
				}
				"""
		);
		this.element = selectField(this.workingCopies[0], "field", 2);
		assertElementEquals("Invalid selected field",
			"field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test07() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					/**
					 * @see Test#field
					 * @see #foo(int, String)
					 * @see Test#foo(int, String)
					 */
					void bar() {
						foo(0, "");
					}
					int field;
					void foo(int x, String s) {}
				}
				"""
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
			"""
				public class Test {
					/**
					 * First {@link #foo(int, String)}
					 * Second {@link Test#foo(int, String) method foo}
					 * Third {@link Test#field field}
					 */
					void bar() {
						foo(0, "");
					}
					int field;
					void foo(int x, String s) {}
				}
				"""
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
			"""
				package test.junit;
				public class Test {
					/**
					 * @see test.junit.Test
					 * @see test.junit.Test#field
					 * @see test.junit.Test#foo(Object[] array)
					 */
					void bar() {
						foo(null);
					}
					int field;
					void foo(Object[] array) {}
				}
				"""
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
			"""
				package test.junit;
				public class Test {
					/** Javadoc {@linkplain test.junit.Test}
					 * {@linkplain test.junit.Test#field field}
					 * last line {@linkplain test.junit.Test#foo(Object[] array) foo(Object[])}
					 */
					void bar() {
						foo(null);
					}
					int field;
					void foo(Object[] array) {}
				}
				"""
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
			"""
				public class Test {
					/**
					 * @throws RuntimeException runtime exception
					 * @throws InterruptedException interrupted exception
					 */
					void foo() {}
				}
				"""
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
			"""
				public class Test {
					/**
					 * @exception RuntimeException runtime exception
					 * @exception InterruptedException interrupted exception
					 */
					void foo() {}
				}
				"""
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
			"""
				public class Test {
					/**
					 * @param xxx integer param
					 * @param str string param
					 */
					void foo(int xxx, String str) {}
				}
				"""
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
			"""
				/**
				 * Javadoc of {@link Test}
				 * @see Field#foo
				 */
				public class Test {}
				/**
				 * Javadoc on {@link Field} to test selection in javadoc field references
				 * @see #foo
				 */
				class Field {
					/**
					 * Javadoc on {@link #foo} to test selection in javadoc field references
					 * @see #foo
					 * @see Field#foo
					 */
					int foo;
				}
				"""
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
			"""
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				foo [in Field [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]""",
			elements
		);
	}

	public void test15() throws JavaModelException {
		setUnit("Test.java",
			"""
				/**
				 * Javadoc of {@link Test}
				 * @see Method#foo
				 */
				public class Test {}
				/**
				 * Javadoc on {@link Method} to test selection in javadoc method references
				 * @see #foo
				 */
				class Method {
					/**
					 * Javadoc on {@link #foo} to test selection in javadoc method references
					 * @see #foo
					 * @see Method#foo
					 */
					void bar() {}
					/**
					 * Method with parameter and throws clause to test selection in javadoc
					 * @param xxx TODO
					 * @param str TODO
					 * @throws RuntimeException blabla
					 * @throws InterruptedException bloblo
					 */
					void foo(int xxx, String str) throws RuntimeException, InterruptedException {}
				}
				"""
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
			"""
				/**
				 * Javadoc of {@link Test}
				 * @see Other
				 */
				public class Test {}
				/**
				 * Javadoc of {@link Other}
				 * @see Test
				 */
				class Other {}
				"""
		);
		IJavaElement[] elements = new IJavaElement[4];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectType(this.workingCopies[0], "Other");
		elements[2] = selectType(this.workingCopies[0], "Test", 3);
		elements[3] = selectType(this.workingCopies[0], "Other", 2);
		assertElementsEqual("Invalid selection(s)",
			"""
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Other [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Other [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]""",
			elements
		);
	}

	public void test17() throws JavaModelException {
		setUnit("Test.java",
			"""
				/**
				 * @see Test.Field#foo
				 */
				public class Test {
					/**
					 * @see Field#foo
					 */
					class Field {
						/**
						 * @see #foo
						 * @see Field#foo
						 * @see Test.Field#foo
						 */
						int foo;
					}
				}
				"""
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
			"""
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo [in Field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]""",
			elements
		);
	}

	public void test18() throws JavaModelException {
		setUnit("Test.java",
			"""
				/**
				 * @see Test.Method#foo()
				 */
				public class Test {
					/**
					 * @see Method#foo()
					 */
					class Method {
						/**
						 * @see #foo()
						 * @see Method#foo()
						 * @see Test.Method#foo()
						 */
						void foo() {}
					}
				}"""
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
			"""
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				foo() [in Method [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]""",
			elements
		);
	}

	public void test19() throws JavaModelException {
		setUnit("Test.java",
			"""
				/**
				 * @see Test.Other
				 */
				public class Test {
					/**
					 * @see Test
					 * @see Other
					 * @see Test.Other
					 */
					class Other {}
				}"""
		);
		IJavaElement[] elements = new IJavaElement[6];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectType(this.workingCopies[0], "Other");
		elements[2] = selectType(this.workingCopies[0], "Test", 3);
		elements[3] = selectType(this.workingCopies[0], "Other", 2);
		elements[4] = selectType(this.workingCopies[0], "Test", 4);
		elements[5] = selectType(this.workingCopies[0], "Other", 3);
		assertElementsEqual("Invalid selection(s)",
			"""
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Other [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Other [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				Other [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]""",
			elements
		);
	}

	public void test20() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						/**
						 * @see Field#foo
						 */
						class Field {
							/**
							 * @see #foo
							 * @see Field#foo
							 */
							int foo;
						}
					}
				}
				"""
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Field");
		elements[1] = selectField(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Field", 3);
		elements[4] = selectField(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"""
				Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				foo [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				foo [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				foo [in Field [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]""",
			elements
		);
	}

	public void test21() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						/**
						 * @see Method#foo()
						 */
						class Method {
							/**
							 * @see #foo()
							 * @see Method#foo()
							 */
							void foo() {}
						}
					}
				}"""
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Method");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Method", 3);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"""
				Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				foo() [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				foo() [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]
				foo() [in Method [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]""",
			elements
		);
	}

	public void test22() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						/**
						 * @see Test
						 * @see Other
						 */
						class Other {}
					}
				}"""
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
			"""
				public class Test {
					void bar() {
						new Object() {
							/**
							 * @see Field#foo
							 */
							class Field {
								/**
								 * @see #foo
								 * @see Field#foo
								 */
								int foo;
							}
						};
					}
				}
				"""
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Field");
		elements[1] = selectField(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Field", 3);
		elements[4] = selectField(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"""
				Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]
				foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]
				Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]""",
			elements
		);
	}

	public void test24() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						new Object() {
							/**
							 * @see Method#foo()
							 */
							class Method {
								/**
								 * @see #foo()
								 * @see Method#foo()
								 */
								void foo() {}
							}
						};
					}
				}"""
		);
		IJavaElement[] elements = new IJavaElement[5];
		elements[0] = selectType(this.workingCopies[0], "Method");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Method", 3);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"""
				Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]
				foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]
				Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]
				foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]]]]""",
			elements
		);
	}

	public void test25() throws JavaModelException {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						new Object() {
							/**
							 * @see Test
							 * @see Other
							 */
							class Other {}
						};
					}
				}"""
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
			"""
				public class Test {
					static int field;
					/**\s
					 * First {@value #field}\
					 * Second {@value Test#field}\
					 */
					void foo() {}
				}
				"""
		);
		IJavaElement[] elements = new IJavaElement[3];
		elements[0] = selectField(this.workingCopies[0], "field");
		elements[1] = selectType(this.workingCopies[0], "Test");
		elements[2] = selectField(this.workingCopies[0], "field");
		assertElementsEqual("Invalid selection(s)",
			"""
				field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]
				Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]
				field [in Test [in [Working copy] Test.java [in <default> [in <project root> [in Tests]]]]]""",
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
			"""
				/**
				 * Valid javadoc.
				 * @see Test
				 * @see Unknown
				 * @see Test#foo()
				 * @see Test#unknown()
				 * @see Test#field
				 * @see Test#unknown
				 * @param unexpected
				 * @throws unexpected
				 * @return unexpected\s
				 */
				package b86380;
				"""
		);
		this.workingCopies[1] = getWorkingCopy("/Tests/b86380/Test.java",
			"""
				/**
				 * Invalid javadoc
				 */
				package b86380;
				public class Test {
					public int field;
					public void foo() {}
				}
				"""
		);
		IJavaElement[] elements = new IJavaElement[3];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "field");
		assertElementsEqual("Invalid selection(s)",
			"""
				Test [in [Working copy] Test.java [in b86380 [in <project root> [in Tests]]]]
				foo() [in Test [in [Working copy] Test.java [in b86380 [in <project root> [in Tests]]]]]
				field [in Test [in [Working copy] Test.java [in b86380 [in <project root> [in Tests]]]]]""",
			elements
		);
	}

	/**
	 * Bug 90266: [select] Code select returns null when there's a string including a slash on same line
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90266"
	 */
	public void testBug90266_String() throws JavaModelException {
		setUnit("b90266/Test.java",
			"""
				package b90266;
				public class Test {
					public int field;
					public void foo(String str, int i) {}
					public void bar() {
						foo("String including / (slash)", this.field)
					}
				}
				"""
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
			"""
				package b90266;
				public class Test {
					public int field;
					public void foo(Char c, int i) {}
					public void bar() {
						foo('/', this.field)
					}
				}
				"""
		);
		int[] selectionPositions = selectionInfo(this.workingCopies[0], "field", 2);
		IJavaElement[] elements = this.workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.java [in b90266 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * bug165701: [model] No hint for ambiguous javadoc
	 * test Ensure that no exception is thrown while selecting method in javadoc comment
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165701"
	 */
	public void testBug165701() throws JavaModelException {
		setUnit("b165701/Test.java",
			"""
				package b165701;
				/**
				 * @see #fooo(int)
				 */
				public class Test {
					public void foo() {}
				}
				"""
		);
		int[] selectionPositions = selectionInfo(this.workingCopies[0], "fooo", 1);
		IJavaElement[] elements = this.workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.java [in b165701 [in <project root> [in Tests]]]]",
			elements
		);
	}

	/**
	 * bug165794: [model] No hint for ambiguous javadoc
	 * test Ensure that no exception is thrown while selecting method in javadoc comment
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165794"
	 */
	public void testBug165794() throws JavaModelException {
		setUnit("b165794/Test.java",
			"""
				package b165794;
				/**
				 * No reasonable hint for resolving the {@link #getMax(A)}.
				 */
				public class X {
				    /**
				     * Extends Number method.
				     * @see #getMax(A ipZ)
				     */
				    public <T extends Y> T getMax(final A<T> ipY) {
				        return ipY.t();
				    }
				   \s
				    /**
				     * Extends Exception method.
				     * @see #getMax(A ipY)
				     */
				    public <T extends Z> T getMax(final A<T> ipZ) {
				        return ipZ.t();
				    }
				}
				class A<T> {
					T t() { return null; }
				}
				class Y {}
				class Z {}"""
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
			"""
				package b171802;
				
				/**
				 * @deprecated
				 */
				public class Y {
				
				}
				"""
		);
		this.workingCopies[1] = getWorkingCopy("/Tests/b171802/X.java",
			"""
				package b171802;
				
				public class X {
					/**
					 * @deprecated Use {@link #bar(char[], Y)}
					 * instead
					 */
					void foo(char[] param1, Y param2) {}
				
					/**
					 * @deprecated
					 */
					void bar(char[] param1, Y param2) {}
				
				}
				"""
		);
		IJavaElement[] elements = new IJavaElement[1];
		elements[0] = selectMethod(this.workingCopies[1], "bar");
		assertElementsEqual("Invalid selection(s)",
			"bar(char[], Y) [in X [in [Working copy] X.java [in b171802 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * bug191322: [javadoc] @see or @link reference to method without signature fails to resolve to base class method
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=191322"
	 */
	public void testBug191322a() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/b191322/X.java",
			"""
				package b191322;
				public class X {
					void foo() {}
				}
				class Y extends X {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				public class X {
					void foo() {}
				}
				class Y extends X {}
				class W extends Y {}
				class Z extends W {
					/**
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				public interface X {
					void foo();
				}
				interface Y extends X {
					/**
					 * {@link #foo}
					 */
					void hoo();
				}
				"""
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
			"""
				package b191322;
				public interface X {
					void foo();
				}
				interface Y extends X {}
				abstract class W implements Y {}
				abstract class Z extends W {
					/**
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				public class X {
					void foo() {}
					class Y {
						/**
						 * @see #foo
						 */
						void hoo() {}
					}
				}
				"""
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
			"""
				package b191322;
				public class X {
					void foo() {}
					void foo(String str) {}
				}
				class Y extends X {
					/**
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				public class X {
					void foo(String str) {}
					void foo() {}
				}
				class Y extends X {
					/**
					 * {@link #foo}
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				public class X {
					void foo(String str) {}
					void foo(int x) {}
				}
				class Y extends X {
					/**
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				public class X {
					void foo(String str) {}
				}
				class Y extends X {
					/**
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
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
			"""
				package b191322;
				interface X {
					void foo();
				}
				interface Y {
					void foo(int i);
				}
				abstract class Z implements X, Y {
					/**
					 * @see #foo
					 */
					void bar() {
					}
				}"""
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
			"""
				package b191322;
				interface X {
					void foo(int x);
				}
				interface Y {
					void foo();
				}
				abstract class Z implements X, Y {
					/**
					 * @see #foo
					 */
					void bar() {
					}
				}"""
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
			"""
				package b191322;
				interface X {
					void foo(int x);
				}
				interface Y {
					void foo(String str);
				}
				abstract class Z implements X, Y {
					/**
					 * @see #foo
					 */
					void bar() {
					}
				}"""
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
			"""
				package b171019;
				interface X {
				   /**
					 * Main desc of foo..
					 */
					void foo(int x);
				}
				interface Y extends X {
				   /**
					 * {@inheritDoc}
					 */
					void foo(int x);
				
				   /**
					 * {@inheritDoc}
					 */
					void foo(String s);
				}
				"""
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
			"""
				package b171019;
				interface X {
				   /**
					 * Main desc of foo..
					 */
					void foo(int x);
				}
				class X1 implements X{
					void foo(int x){}
				}
				class Y extends X1 {
				   /**
					 * {@inheritDoc}
					 */
					void foo(int x);
				
				   /**
					 * {@inheritDoc}
					 */
					void foo(String s);
				}
				"""
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
			"""
				package b171019;
				interface X1 {
				   /**
					 * Main desc of foo in X1..
					 */
					void foo(int x);
				}
				interface X2 {
				   /**
					 * Main desc of foo in X2..
					 */
					void foo(int x);
				}
				class X implements X1 {
				   /**
					 * X desc of foo..
					 */
					void foo(int x){}
				}
				class Y extends X implements X2 {
				   /**
					 * {@inheritDoc}
					 */
					void foo(int x);
				
				}
				"""
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
			"""
				package b171019;
				interface X1 {
				   /**
					 * Main desc of foo in X1..
					 */
					void foo(int x);
				}
				interface X2 {
					void foo(int x);
				}
				class X implements X1 {
				   /**
					 * X desc of foo..
					 */
					void foo(int x){}
				}
				class Y extends X implements X2 {
				   /**
					 * {@inheritDoc}
					 */
					void foo(int x);
				
				}
				"""
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
			"""
				package b171019;
				interface X {
				   /**
					 * Main desc of foo..
					 */
					void foo(int x);
				}
				interface Y {
					void foo(String str);
				}
				abstract class Z implements X, Y {
					/**
					 * {@inheritDoc}
					 */
					void foo(int x) {
					}
				}"""
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
			"""
				package b171019;
				interface X {
				   /**
					 * Main desc of foo..
					 */
					void foo(int x);
				}
				interface Y extends X {
				   /**
					 * {@inheritDoc}
					 * @param {@inheritDoc}
					 * @return {@inheritDoc}
					 * @throws {@inheritDoc}
					 * @exception {@inheritDoc}
					 */
					void foo(int x);
				
				}
				"""
		);
		IJavaElement[] elements = new IJavaElement[4];
		elements[0] = selectMethod(this.workingCopies[0], "@inheritDoc", 1);
		elements[1] = selectMethod(this.workingCopies[0], "@inheritDoc", 2);
		elements[2] = selectMethod(this.workingCopies[0], "@inheritDoc", 3);
		elements[3] = selectMethod(this.workingCopies[0], "@inheritDoc", 4);
		assertElementsEqual("Invalid selection(s)",
			"""
				foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]
				foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]
				foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]
				foo(int) [in X [in [Working copy] X.java [in b171019 [in <project root> [in Tests]]]]]""",
			elements
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400767
	public void testBug400767() throws Exception {
		String content = """
			package test;
			import b400767.ETest;
			public class Bug {
				Bug() {
					doSomethingUsingOtherPackage();
				}
				public void addComponentListener(ComponentListener listener) {}
				private void doSomethingUsingOtherPackage() {
					for (ETest val : ETest.values()) {
						System.out.println(val.name());
					}
					Bug bug = new Bug();
					bug.addComponentListener(new ComponentAdapter() {
						/**
						 * @see ComponentAdapter#componentShown(ComponentEvent)
						 */
						@Override
						public void componentShown(ComponentEvent e) {
							super.componentShown(e);
						}
					});
				}
			}
			interface ComponentListener {
			    public void componentShown(ComponentEvent e);
			}
			class ComponentAdapter implements ComponentListener {
				public void componentShown(ComponentEvent e) { }
			}
			class ComponentEvent {}""";
		this.wcOwner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[2] =  getWorkingCopy("/Tests/test/ETest.java", content);
		content = """
			/**
			 \
			* This package is used by another package and will cause an error.
			 */
			package b400767;""";
		// package-info is physically required at some point. Just put it to move forward.
		createFolder("/Tests/b400767");
		createFile("/Tests/b400767/package-info.java", content);
		this.workingCopies[0] = getWorkingCopy("/Tests/b400767/package-info.java", content);
		content = """
			package b400767;
			public enum ETest {
				VAL1, VAL2, VAL3;
			}""";
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
