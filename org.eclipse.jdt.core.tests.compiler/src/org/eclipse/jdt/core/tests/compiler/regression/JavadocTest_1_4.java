/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTest_1_4 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;

	public JavadocTest_1_4(String name) {
		super(name);
	}

	public static Class javadocTestClass() {
		return JavadocTest_1_4.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] {
//			"testBug70892conform1", "testBug70892conform2"
//		};
//		TESTS_NUMBERS = new int[] { 21, 22, 36 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildTestSuite(javadocTestClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, reportInvalidJavadoc);
		if (reportMissingJavadocComments != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportInvalidJavadoc);
		if (reportMissingJavadocTags != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		reportInvalidJavadoc = CompilerOptions.ERROR;
		reportMissingJavadocTags = CompilerOptions.ERROR;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
	}

	/**
	 * Test fix for bug 70892: [1.5][Javadoc] Compiler should parse reference for inline tag @value
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70892">70892</a>
	 * These two tests pass for 1.3 or 1.4 source level but should fail for 1.5
	 * @see JavadocTest_1_5
	 */
	public void test001() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * {@value \"invalid\"}\n" + 
					" * {@value <a href=\"invalid\">invalid</a>} invalid\n" + 
					" * {@value #field}\n" + 
					" * {@value #foo}\n" + 
					" * {@value #foo()}\n" + 
					" */\n" + 
					"public class X {\n" + 
					"	int field;\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			""	// No failure in fact...
		);
	}
	public void test002() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * {@value \"invalid}\n" + 
					" * {@value <a href}\n" + 
					" * {@value <a href=\"invalid\">invalid</a} invalid\n" + 
					" * {@value #fild}\n" + 
					" * {@value #fo}\n" + 
					" * {@value #f()}\n" + 
					" */\n" + 
					"public class X {\n" + 
					"	int field;\n" + 
					"	void foo() {}\n" + 
					"}\n"	
			},
			""	// No failure in fact...
		);
	}

	/**
	 * Test fix for bug 70891: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70891">70891</a>
	 * These two tests fail for 1.5 source level but should pass for 1.3 or 1.4
	 * @see JavadocTest_1_4
	 */
	/* (non-Javadoc)
	 * Test @param for generic class type parameter
	 */
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <E> Type\n" + 
					"  */\n" + 
					" public class X<E> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\r\n" + 
				"	* @param <E> Type\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\r\n" + 
				"	public class X<E> {}\r\n" + 
				"	               ^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <E> Type extends RuntimeException\n" + 
					"  */\n" + 
					" public class X<E extends RuntimeException> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\r\n" + 
				"	* @param <E> Type extends RuntimeException\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\r\n" + 
				"	public class X<E extends RuntimeException> {}\r\n" + 
				"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"

		);
	}
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <T> Type parameter 1\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\r\n" + 
				"	* @param <T> Type parameter 1\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\r\n" + 
				"	* @param <U> Type parameter 2\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\r\n" + 
				"	* @param <V> Type parameter 3\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\r\n" + 
				"	public class X<T, U, V> {}\r\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <E> Type parameter\n" + 
					"  */\n" + 
					" public class X {}",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <E> Type parameter\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n"
		);
	}
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <E> Type parameter\n" + 
					"  */\n" + 
					" public class X<E, F> {}",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <E> Type parameter\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<E, F> {}\n" + 
				"	               ^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <T> Type parameter 1\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	public class X<T> {}\n" + 
				"	               ^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <T> Type parameter 1\n" + 
					"  * @param <X> Type parameter 2\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <E> Type parameter 2\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <T> Type parameter 1\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\r\n" + 
				"	* @param <V> Type parameter 3\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\r\n" + 
				"	* @param <U> Type parameter 2\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\r\n" + 
				"	* @param <T> Type parameter 1\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\r\n" + 
				"	public class X<T, U, V> {}\r\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <U> Type parameter 1\n" + 
					"  * @param <E> Type parameter 2\n" + 
					"  * @param <V> Type parameter 2\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <T> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <T> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <T> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <U> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <U> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <U> Type parameter 3\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <U> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <T> Type parameter 3\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <T> Type parameter 3\n" + 
					"  * @param <U> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <U> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <V> Type parameter 2\n" + 
					"  * @param <X> Type parameter 2\n" + 
					"  * @param <U> Type parameter 1\n" + 
					"  * @param <E> Type parameter 2\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <V> Type parameter 2\n" + 
					"  * @param\n" + 
					"  * @param <U> Type parameter 1\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param\n" + 
				"	   ^^^^^\n" + 
				"Javadoc: Missing parameter name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference: compile error\n" + 
					"  * @param <T> Type parameter 2\n" + 
					"  * @param <V> Type parameter 2\n" + 
					"  * @param <U> Type parameter 1\n" + 
					"  */\n" + 
					" public class X<T, , V> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, , V> {}\n" + 
				"	              ^^\n" + 
				"Syntax error on tokens, delete these tokens\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, , V> {}\n" + 
				"	               ^\n" + 
				"Syntax error, insert \"ClassBody\" to complete CompilationUnit\n" + 
				"----------\n"
		);
	}
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Invalid type parameter reference: compile error\n" + 
					"  * @param <T> Type parameter 2\n" + 
					"  * @param <V> Type parameter 2\n" + 
					"  * @param <U> Type parameter 1\n" + 
					"  */\n" + 
					" public class X<T, U, V extend Exception> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <T> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, U, V extend Exception> {}\n" + 
				"	              ^^^^^^\n" + 
				"Syntax error on tokens, delete these tokens\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, U, V extend Exception> {}\n" + 
				"	                   ^\n" + 
				"Syntax error, insert \"ClassBody\" to complete CompilationUnit\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, U, V extend Exception> {}\n" + 
				"	                       ^^^^^^\n" + 
				"extend cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	/* (non-Javadoc)
	 * Test @param for generic method type parameter
	 */
	public void test023() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Valid type parameter reference\n" + 
					"	 * @param <E> Type\n" + 
					"	 */\n" + 
					"	public <E> void foo() {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\r\n" + 
				"	* @param <E> Type\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\r\n" + 
				"	public <E> void foo() {}\r\n" + 
				"	        ^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Valid type parameter reference\n" + 
					"	 * @param <E> Type extends RuntimeException\n" + 
					"	 * @param val int\n" + 
					"	 * @param obj Object\n" + 
					"	 */\n" + 
					"	public <E extends RuntimeException> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\r\n" + 
				"	* @param <E> Type extends RuntimeException\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\r\n" + 
				"	public <E extends RuntimeException> void foo(int val, Object obj) {}\r\n" + 
				"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test025() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Valid type parameter reference\n" + 
					"	 * @param val int\n" + 
					"	 * @param obj Object\n" + 
					"	 * @param <T> Type parameter 1\n" + 
					"	 * @param <U> Type parameter 2\n" + 
					"	 * @param <V> Type parameter 3\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 6)\r\n" + 
				"	* @param <T> Type parameter 1\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\r\n" + 
				"	* @param <U> Type parameter 2\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\r\n" + 
				"	* @param <V> Type parameter 3\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 10)\r\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\r\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param val int\n" + 
					"	 * @param <E> Type parameter\n" + 
					"	 * @param obj Object\n" + 
					"	 */\n" + 
					"	public void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param <E> Type parameter\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n"
		);
	}
	public void test027() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param <E> Type parameter\n" + 
					"	 */\n" + 
					"	public <E, F> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <E> Type parameter\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	public <E, F> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	public <E, F> void foo(int val, Object obj) {}\n" + 
				"	                           ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	public <E, F> void foo(int val, Object obj) {}\n" + 
				"	                                       ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}
	public void test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param <T> Type parameter 1\n" + 
					"	 * @param <U> Type parameter 2\n" + 
					"	 * @param <V> Type parameter 3\n" + 
					"	 * @param xxx int\n" + 
					"	 * @param Obj Object\n" + 
					"	 */\n" + 
					"	public <T> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <T> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	* @param xxx int\n" + 
				"	         ^^^\n" + 
				"Javadoc: Parameter xxx is not declared\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 8)\n" + 
				"	* @param Obj Object\n" + 
				"	         ^^^\n" + 
				"Javadoc: Parameter Obj is not declared\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	public <T> void foo(int val, Object obj) {}\n" + 
				"	        ^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 10)\n" + 
				"	public <T> void foo(int val, Object obj) {}\n" + 
				"	                        ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 10)\n" + 
				"	public <T> void foo(int val, Object obj) {}\n" + 
				"	                                    ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param <T> Type parameter 1\n" + 
					"	 * @param <X> Type parameter 2\n" + 
					"	 * @param val int\n" + 
					"	 * @param <U> Type parameter 2\n" + 
					"	 * @param <E> Type parameter 2\n" + 
					"	 * @param obj Object\n" + 
					"	 * @param <V> Type parameter 3\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <T> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 10)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 12)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test030() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Valid type parameter reference\n" + 
					"	 * @param <V> Type parameter 3\n" + 
					"	 * @param obj Object\n" + 
					"	 * @param <U> Type parameter 2\n" + 
					"	 * @param val int\n" + 
					"	 * @param <T> Type parameter 1\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\r\n" + 
				"	* @param <V> Type parameter 3\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\r\n" + 
				"	* @param <U> Type parameter 2\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\r\n" + 
				"	* @param <T> Type parameter 1\r\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 10)\r\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\r\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                              ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                                          ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}
	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param <T> Type parameter 3\n" + 
					"	 * @param val int\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <T> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                                          ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param obj Object\n" + 
					"	 * @param <U> Type parameter 3\n" + 
					"	 * @param <V> Type parameter 3\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                              ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n"
		);
	}
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param val int\n" + 
					"	 * @param <V> Type parameter 2\n" + 
					"	 * @param <X> Type parameter 2\n" + 
					"	 * @param <U> Type parameter 1\n" + 
					"	 * @param Object obj\n" + 
					"	 * @param <E> Type parameter 2\n" + 
					"	 * @param <U> Type parameter 2\n" + 
					"	 * @param val int\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @param Object obj\n" + 
				"	         ^^^^^^\n" + 
				"Javadoc: Parameter Object is not declared\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 11)\n" + 
				"	* @param val int\n" + 
				"	         ^^^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 13)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 13)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                                          ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference\n" + 
					"	 * @param <V> Type parameter 2\n" + 
					"	 * @param\n" + 
					"	 * @param <U> Type parameter 1\n" + 
					"	 */\n" + 
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param\n" + 
				"	   ^^^^^\n" + 
				"Javadoc: Missing parameter name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                              ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                                          ^^^\n" + 
				"Javadoc: Missing tag for parameter obj\n" + 
				"----------\n"
		);
	}
	// TODO (david) recovery seems not to work properly here:
	// we should have type parameters in method declaration.
	public void test036() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference: compile error\n" + 
					"	 * @param <T> Type parameter 2\n" + 
					"	 * @param <V> Type parameter 2\n" + 
					"	 * @param <U> Type parameter 1\n" + 
					"	 * @param val int\n" + 
					"	 * @param obj Object\n" + 
					"	 */\n" + 
					"	public <T, , V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <T> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 10)\n" + 
				"	public <T, , V> void foo(int val, Object obj) {}\n" + 
				"	       ^^^^^^^^\n" + 
				"Syntax error on tokens, delete these tokens\n" + 
				"----------\n"
		);
	}
	public void test037() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Invalid type parameter reference: compile error\n" + 
					"	 * @param <T> Type parameter 2\n" + 
					"	 * @param <V> Type parameter 2\n" + 
					"	 * @param <U> Type parameter 1\n" + 
					"	 * @param val int\n" + 
					"	 * @param obj Object\n" + 
					"	 */\n" + 
					"	public <T, U, V extends Exceptions> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <T> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 10)\n" + 
				"	public <T, U, V extends Exceptions> void foo(int val, Object obj) {}\n" + 
				"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param < Type\n" + 
					"  * @param < Type for parameterization\n" + 
					"  * @param <> Type\n" + 
					"  * @param <?> Type\n" + 
					"  * @param <*> Type\n" + 
					"  */\n" + 
					" public class X<E> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param < Type\n" + 
				"	         ^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param < Type for parameterization\n" + 
				"	         ^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <> Type\n" + 
				"	         ^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <?> Type\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <*> Type\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<E> {}\n" + 
				"	               ^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <E Type parameter of class X\n" + 
					"  * @param E> Type\n" + 
					"  * @param <<E> Type\n" + 
					"  * @param <<<E> Type\n" + 
					"  * @param <E>> Type\n" + 
					"  */\n" + 
					" public class X<E> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <E Type parameter of class X\n" + 
				"	         ^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param E> Type\n" + 
				"	         ^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <<E> Type\n" + 
				"	         ^^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <<<E> Type\n" + 
				"	         ^^^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <E>> Type\n" + 
				"	         ^^^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<E> {}\n" + 
				"	               ^\n" + 
				"Syntax error, type parameters are only available if source level is 1.5\n" + 
				"----------\n"
		);
	}
}
