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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTest_1_5 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;

	public JavadocTest_1_5(String name) {
		super(name);
	}

	public static Class javadocTestClass() {
		return JavadocTest_1_5.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug83127";
//		TESTS_NAMES = new String[] { "testBug83127a" };
//		TESTS_NUMBERS = new int[] { 83804 };
//		TESTS_RANGE = new int[] { 23, -1 };
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
	 * These two tests fail for 1.5 source level but should pass for 1.3 or 1.4
	 * @see JavadocTest_1_4
	 */
	public void test001() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
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
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* {@value \"invalid\"}\n" + 
				"	          ^^^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* {@value <a href=\"invalid\">invalid</a>} invalid\n" + 
				"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	* {@value #field}\n" + 
				"	           ^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	* {@value #foo}\n" + 
				"	           ^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 6)\n" + 
				"	* {@value #foo()}\n" + 
				"	           ^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n"
		);
	}
	public void test002() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
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
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* {@value \"invalid}\n" + 
				"	          ^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* {@value <a href}\n" + 
				"	          ^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	* {@value <a href=\"invalid\">invalid</a} invalid\n" + 
				"	          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	* {@value #fild}\n" + 
				"	           ^^^^\n" + 
				"Javadoc: fild cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 6)\n" + 
				"	* {@value #fo}\n" + 
				"	           ^^\n" + 
				"Javadoc: fo cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 7)\n" + 
				"	* {@value #f()}\n" + 
				"	           ^\n" + 
				"Javadoc: The method f() is undefined for the type X\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 7)\n" + 
				"	* {@value #f()}\n" + 
				"	           ^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n"
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
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <E> Type\n" + 
					"  */\n" + 
					" public class X<E> {}"
			}
		);
	}
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <E> Type extends RuntimeException\n" + 
					"  */\n" + 
					" public class X<E extends RuntimeException> {}"
			}
		);
	}
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <T> Type parameter 1\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			}
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
				"	   ^^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	public class X<E, F> {}\n" + 
				"	                  ^\n" + 
				"Javadoc: Missing tag for parameter F\n" + 
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
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: U cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	          ^\n" + 
				"Javadoc: V cannot be resolved to a type\n" + 
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
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Parameter X is not declared\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: E cannot be resolved to a type\n" + 
				"----------\n"
		);
	}
	public void test010() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" + 
					"  * Valid type parameter reference\n" + 
					"  * @param <V> Type parameter 3\n" + 
					"  * @param <U> Type parameter 2\n" + 
					"  * @param <T> Type parameter 1\n" + 
					"  */\n" + 
					" public class X<T, U, V> {}"
			}
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
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: E cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
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
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                  ^\n" + 
				"Javadoc: Missing tag for parameter U\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                     ^\n" + 
				"Javadoc: Missing tag for parameter V\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                  ^\n" + 
				"Javadoc: Missing tag for parameter U\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                     ^\n" + 
				"Javadoc: Missing tag for parameter V\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                     ^\n" + 
				"Javadoc: Missing tag for parameter V\n" + 
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
				"1. ERROR in X.java (at line 6)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
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
				"1. ERROR in X.java (at line 6)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                  ^\n" + 
				"Javadoc: Missing tag for parameter U\n" + 
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
				"1. ERROR in X.java (at line 6)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                     ^\n" + 
				"Javadoc: Missing tag for parameter V\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	                  ^\n" + 
				"Javadoc: Missing tag for parameter U\n" + 
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
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Parameter X is not declared\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: E cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 9)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
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
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param\n" + 
				"	   ^^^^^\n" + 
				"Javadoc: Missing parameter name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, U, V> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
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
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: V cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	          ^\n" + 
				"Javadoc: U cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, , V> {}\n" + 
				"	                  ^\n" + 
				"Syntax error on token \",\", delete this token\n" + 
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
				"1. ERROR in X.java (at line 7)\n" + 
				"	public class X<T, U, V extend Exception> {}\n" + 
				"	                       ^^^^^^\n" + 
				"Syntax error on token \"extend\", extends expected\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
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
		this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" + 
					"	 * Valid type parameter reference\n" + 
					"	 * @param <E> Type\n" + 
					"	 */\n" + 
					"	public <E> void foo() {}\n" +
					"}"
			}
		);
	}
	public void test024() {
		this.runConformTest(
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
			}
		);
	}
	public void test025() {
		this.runConformTest(
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
			}
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
				"	   ^^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
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
				"1. ERROR in X.java (at line 6)\n" + 
				"	public <E, F> void foo(int val, Object obj) {}\n" + 
				"	           ^\n" + 
				"Javadoc: Missing tag for parameter F\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	public <E, F> void foo(int val, Object obj) {}\n" + 
				"	                           ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: U cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param <V> Type parameter 3\n" + 
				"	          ^\n" + 
				"Javadoc: V cannot be resolved to a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @param xxx int\n" + 
				"	         ^^^\n" + 
				"Javadoc: Parameter xxx is not declared\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @param Obj Object\n" + 
				"	         ^^^\n" + 
				"Javadoc: Parameter Obj is not declared\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 10)\n" + 
				"	public <T> void foo(int val, Object obj) {}\n" + 
				"	                        ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Parameter X is not declared\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: E cannot be resolved to a type\n" + 
				"----------\n"
		);
	}
	public void test030() {
		this.runConformTest(
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
			}
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
				"	        ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	           ^\n" + 
				"Javadoc: Missing tag for parameter U\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	              ^\n" + 
				"Javadoc: Missing tag for parameter V\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                              ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 5)\n" + 
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
				"1. ERROR in X.java (at line 7)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	           ^\n" + 
				"Javadoc: Missing tag for parameter U\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	              ^\n" + 
				"Javadoc: Missing tag for parameter V\n" + 
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
				"1. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
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
				"1. ERROR in X.java (at line 6)\n" + 
				"	* @param <X> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Parameter X is not declared\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	* @param Object obj\n" + 
				"	         ^^^^^^\n" + 
				"Javadoc: Parameter Object is not declared\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 9)\n" + 
				"	* @param <E> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: E cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 10)\n" + 
				"	* @param <U> Type parameter 2\n" + 
				"	          ^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 11)\n" + 
				"	* @param val int\n" + 
				"	         ^^^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 13)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 13)\n" + 
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
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param\n" + 
				"	   ^^^^^\n" + 
				"Javadoc: Missing parameter name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	        ^\n" + 
				"Javadoc: Missing tag for parameter T\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	public <T, U, V> void foo(int val, Object obj) {}\n" + 
				"	                              ^^^\n" + 
				"Javadoc: Missing tag for parameter val\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
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
				"	   ^^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	* @param <V> Type parameter 2\n" + 
				"	   ^^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	* @param <U> Type parameter 1\n" + 
				"	   ^^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 10)\n" + 
				"	public <T, , V> void foo(int val, Object obj) {}\n" + 
				"	           ^\n" + 
				"Syntax error on token \",\", delete this token\n" + 
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
				"1. ERROR in X.java (at line 10)\n" + 
				"	public <T, U, V extends Exceptions> void foo(int val, Object obj) {}\n" + 
				"	                        ^^^^^^^^^^\n" + 
				"Exceptions cannot be resolved to a type\n" + 
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
				"	         ^^^^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param < Type for parameterization\n" + 
				"	         ^^^^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <> Type\n" + 
				"	         ^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <?> Type\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <*> Type\n" + 
				"	         ^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<E> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter E\n" + 
				"----------\n"
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" /**\n" + 
					"  * Invalid type parameter reference\n" + 
					"  * @param <E Type\n" + 
					"  * @param E> Type\n" + 
					"  * @param <<E> Type\n" + 
					"  * @param <<<E> Type\n" + 
					"  * @param <E>> Type\n" + 
					"  */\n" + 
					" public class X<E> {}"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @param <E Type\n" + 
				"	         ^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	* @param E> Type\n" + 
				"	         ^^\n" + 
				"Javadoc: Invalid param tag name\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @param <<E> Type\n" + 
				"	         ^^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	* @param <<<E> Type\n" + 
				"	         ^^^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	* @param <E>> Type\n" + 
				"	         ^^^^\n" + 
				"Javadoc: Invalid param tag type parameter name\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 9)\n" + 
				"	public class X<E> {}\n" + 
				"	               ^\n" + 
				"Javadoc: Missing tag for parameter E\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 80257: [javadoc] Invalid missing reference warning on @see or @link tags
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80257"
	 */
	public void testBug80257() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
				" * @see G#G(Object)\n" + 
				" * @see G#G(Exception)\n" + 
				" */\n" + 
				"public class X extends G<Exception> {\n" + 
				"	X(Exception exc) { super(exc);}\n" + 
				"}\n" + 
				"class G<E extends Exception> {\n" + 
				"	G(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	* @see G#G(Object)\n" + 
			"	         ^^^^^^^^^\n" + 
			"Javadoc: The constructor G(Object) is undefined\n" + 
			"----------\n"
		);
	}

	/**
	 * Test fix for bug 82514: [1.5][javadoc] Problem with generics in javadoc
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82514"
	 */
	public void testBug82514() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class ComparableUtils {\n" + 
				"   public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException\n" + 
				"    {\n" + 
				"        return 0;\n" + 
				"    }\n" + 
				"    public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)\n" + 
				"        throws ClassCastException\n" + 
				"    {\n" + 
				"        return 0;\n" + 
				"    }\n" + 
				"}\n" + 
				"public final class X {  \n" + 
				"	/** Tests the method{@link ComparableUtils#compareTo(Object, Object, Class)} and\n" + 
				"	 *  {@link ComparableUtils#compareTo(Object, Object)}.\n" + 
				"	 */\n" + 
				"    public void testCompareTo() {}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	*  {@link ComparableUtils#compareTo(Object, Object)}.\n" + 
			"	                          ^^^^^^^^^\n" + 
			"Javadoc: Bound mismatch: The generic method compareTo(X, X) of type ComparableUtils is not applicable for the arguments (Object, Object) since the type Object is not a valid substitute for the bounded parameter <X extends Comparable<? super X>>\n" + 
			"----------\n"
		);
	}

	/**
	 * Test fix for bug 83127: [1.5][javadoc][dom] Wrong / strange bindings for references in javadoc to methods with type variables as parameter types
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83127"
	 */
	public void testBug83127a() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" + 
				" * @see Test#add(T) \n" + 
				" * @see #add(T)\n" + 
				" * @see Test#Test(T)\n" + 
				" * @see #Test(T)\n" + 
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" + 
				" *                the arguments (T)\"\n" + 
				" *   - method binding = Test.add(Object)\n" + 
				" *   - parameter binding = T of A\n" + 
				" */\n" + 
				"public class Test<T> {\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Test#add(T) \n" + 
			"	            ^^^\n" + 
			"Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 3)\n" + 
			"	* @see #add(T)\n" + 
			"	        ^^^\n" + 
			"Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)\n" + 
			"----------\n" + 
			"3. ERROR in Test.java (at line 4)\n" + 
			"	* @see Test#Test(T)\n" + 
			"	            ^^^^^^^\n" + 
			"Javadoc: The constructor Test(T) is undefined\n" + 
			"----------\n" + 
			"4. ERROR in Test.java (at line 5)\n" + 
			"	* @see #Test(T)\n" + 
			"	        ^^^^^^^\n" + 
			"Javadoc: The constructor Test(T) is undefined\n" + 
			"----------\n"
		);
	}
	public void testBug83127b() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" + 
				" * @see Sub#add(T)\n" + 
				" * @see Sub#Sub(T)\n" + 
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" + 
				" *                the arguments (T)\"\n" + 
				" *   - method binding = Test.add(Object)\n" + 
				" *   - parameter binding = T of A\n" + 
				" *     -> Do we need to change this as T natually resolved to TypeVariable?\n" + 
				" *        As compiler raises a warning, it\'s perhaps not a problem now...\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Sub#add(T)\n" + 
			"	           ^^^\n" + 
			"Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 3)\n" + 
			"	* @see Sub#Sub(T)\n" + 
			"	           ^^^^^^\n" + 
			"Javadoc: The constructor Sub(T) is undefined\n" + 
			"----------\n"
		);
	}
	public void testBug83127c() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" + 
				" * @see Sub#add(E) \n" + 
				" * @see Sub#Sub(E)\n" + 
				" *   - warning = \"E cannot be resolved to a type\"\n" + 
				" *   - method binding = null\n" + 
				" *   - parameter binding = null\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Sub#add(E) \n" + 
			"	               ^\n" + 
			"Javadoc: E cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 3)\n" + 
			"	* @see Sub#Sub(E)\n" + 
			"	               ^\n" + 
			"Javadoc: E cannot be resolved to a type\n" + 
			"----------\n"
		);
	}
	public void testBug83127d() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" + 
				"	public Unrelated1(E e) {}\n" + 
				"	public boolean add(E e) { return false; }\n" + 
				"}\n",
				"Test.java",
				"/** \n" + 
				" * @see Unrelated1#add(E)\n" + 
				" * @see Unrelated1#Unrelated1(E)\n" + 
				" *   - warning = \"E cannot be resolved to a type\"\n" + 
				" *   - method binding = null\n" + 
				" *   - parameter binding = null\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Unrelated1#add(E)\n" + 
			"	                      ^\n" + 
			"Javadoc: E cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 3)\n" + 
			"	* @see Unrelated1#Unrelated1(E)\n" + 
			"	                             ^\n" + 
			"Javadoc: E cannot be resolved to a type\n" + 
			"----------\n"
		);
	}
	public void testBug83127e() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" + 
				"	public Unrelated1(E e) {}\n" + 
				"	public boolean add(E e) { return false; }\n" + 
				"}\n",
				"Test.java",
				"/** \n" + 
				" * @see Unrelated1#add(Object)\n" + 
				" * @see Unrelated1#Unrelated1(Object)\n" + 
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" + 
				" *                the arguments (Object)\"\n" + 
				" *   - method binding = Unrelated1.add(Number)\n" + 
				" *   - parameter binding = java.lang.Object\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Unrelated1#add(Object)\n" + 
			"	                  ^^^\n" + 
			"Javadoc: The method add(Number) in the type Unrelated1 is not applicable for the arguments (Object)\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 3)\n" + 
			"	* @see Unrelated1#Unrelated1(Object)\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: The constructor Unrelated1(Object) is undefined\n" + 
			"----------\n"
		);
	}
	public void testBug83127f() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" + 
				"	public Unrelated1(E e) {}\n" + 
				"	public boolean add(E e) { return false; }\n" + 
				"}\n",
				"Test.java",
				"/** \n" + 
				" * @see Unrelated1#add(Number)\n" + 
				" * @see Unrelated1#Unrelated1(Number)\n" + 
				" *   - no warning\n" + 
				" *   - method binding = Unrelated1.add(Number)\n" + 
				" *   - parameter binding = java.lang.Number\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			}
		);
	}
	public void testBug83127g() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" + 
				"	public Unrelated1(E e) {}\n" + 
				"	public boolean add(E e) { return false; }\n" + 
				"}\n",
				"Test.java",
				"/** \n" + 
				" * @see Unrelated1#add(Integer)\n" + 
				" * @see Unrelated1#Unrelated1(Integer)\n" + 
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" + 
				" *                the arguments (Integer)\"\n" + 
				" *   - method binding = Unrelated1.add(Number)\n" + 
				" *   - parameter binding = java.lang.Integer\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"	public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Unrelated1#add(Integer)\n" + 
			"	                  ^^^\n" + 
			"Javadoc: The method add(Number) in the type Unrelated1 is not applicable for the arguments (Integer)\n" + 
			"----------\n" + 
			"2. ERROR in Test.java (at line 3)\n" + 
			"	* @see Unrelated1#Unrelated1(Integer)\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^^\n" + 
			"Javadoc: The constructor Unrelated1(Integer) is undefined\n" + 
			"----------\n"
		);
	}
	public void testBug83127h() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated2.java",
				"public interface Unrelated2<E> {\n" + 
				"	boolean add(E e);\n" + 
				"}\n",
				"Test.java",
				"/** \n" + 
				" * @see Unrelated2#add(T)\n" + 
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" + 
				" *                the arguments (T)\"\n" + 
				" *   - method binding = Unrelated2.add(Object)\n" + 
				" *   - parameter binding = T of A\n" + 
				" *     -> Do we need to change this as T natually resolved to TypeVariable?\n" + 
				" *        As compiler raises a warning, it\'s perhaps not a problem now...\n" + 
				" */\n" + 
				"public class Test<T>{\n" + 
				"	Test(T t) {}\n" + 
				"    public boolean add(T t) {\n" + 
				"        return true;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class Sub<E extends Number> extends Test<E> {\n" + 
				"	Sub (E e) {super(null);}\n" + 
				"    public boolean add(E e) {\n" + 
				"        if (e.doubleValue() > 0)\n" + 
				"            return false;\n" + 
				"        return super.add(e);\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Test.java (at line 2)\n" + 
			"	* @see Unrelated2#add(T)\n" + 
			"	                  ^^^\n" + 
			"Javadoc: The method add(Object) in the type Unrelated2 is not applicable for the arguments (T)\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 83393: [1.5][javadoc] reference to vararg method also considers non-array type as correct
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83393"
	 */
	public void testBug83393a() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	public void foo(int a, int b) {} \n" + 
				"	public void foo(int a, int... args) {}\n" + 
				"	public void foo(String... args) {}\n" + 
				"	public void foo(Exception str, boolean... args) {}\n" + 
				"}\n",
				"Valid.java",
				"/**\n" + 
				" * @see Test#foo(int, int)\n" + 
				" * @see Test#foo(int, int[])\n" + 
				" * @see Test#foo(int, int...)\n" + 
				" * @see Test#foo(String[])\n" + 
				" * @see Test#foo(String...)\n" + 
				" * @see Test#foo(Exception, boolean[])\n" + 
				" * @see Test#foo(Exception, boolean...)\n" + 
				" */\n" + 
				"public class Valid {}\n"
			}
		);
	}
	public void testBug83393b() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	public void foo(int a, int b) {} \n" + 
				"	public void foo(int a, int... args) {}\n" + 
				"	public void foo(String... args) {}\n" + 
				"	public void foo(Exception str, boolean... args) {}\n" + 
				"}\n",
				"Invalid.java",
				"/**\n" + 
				" * @see Test#foo(int)\n" + 
				" * @see Test#foo(int, int, int)\n" + 
				" * @see Test#foo()\n" + 
				" * @see Test#foo(String)\n" + 
				" * @see Test#foo(String, String)\n" + 
				" * @see Test#foo(Exception)\n" + 
				" * @see Test#foo(Exception, boolean)\n" + 
				" * @see Test#foo(Exception, boolean, boolean)\n" + 
				" */\n" + 
				"public class Invalid {}\n"
			},
			"----------\n" + 
			"1. ERROR in Invalid.java (at line 2)\n" + 
			"	* @see Test#foo(int)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(int, int...) in the type Test is not applicable for the arguments (int)\n" + 
			"----------\n" + 
			"2. ERROR in Invalid.java (at line 3)\n" + 
			"	* @see Test#foo(int, int, int)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(int, int...) in the type Test is not applicable for the arguments (int, int, int)\n" + 
			"----------\n" + 
			"3. ERROR in Invalid.java (at line 4)\n" + 
			"	* @see Test#foo()\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(String...) in the type Test is not applicable for the arguments ()\n" + 
			"----------\n" + 
			"4. ERROR in Invalid.java (at line 5)\n" + 
			"	* @see Test#foo(String)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(String...) in the type Test is not applicable for the arguments (String)\n" + 
			"----------\n" + 
			"5. ERROR in Invalid.java (at line 6)\n" + 
			"	* @see Test#foo(String, String)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(String...) in the type Test is not applicable for the arguments (String, String)\n" + 
			"----------\n" + 
			"6. ERROR in Invalid.java (at line 7)\n" + 
			"	* @see Test#foo(Exception)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception)\n" + 
			"----------\n" + 
			"7. ERROR in Invalid.java (at line 8)\n" + 
			"	* @see Test#foo(Exception, boolean)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception, boolean)\n" + 
			"----------\n" + 
			"8. ERROR in Invalid.java (at line 9)\n" + 
			"	* @see Test#foo(Exception, boolean, boolean)\n" + 
			"	            ^^^\n" + 
			"Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception, boolean, boolean)\n" + 
			"----------\n"
		);
	}

	/**
	 * Bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
	 */
	public void testBug83804() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
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
				" * @deprecated accepted by javadoc.exe although javadoc 1.5 spec does not say that's a valid tag\n" + 
				" * @other-tags are valid\n" + 
				" */\n" + 
				"package pack;\n",
				"pack/Test.java",
				"/**\n" + 
				" * Invalid javadoc\n" + 
				" */\n" + 
				"package pack;\n" + 
				"public class Test {\n" + 
				"	public int field;\n" + 
				"	public void foo() {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in pack\\package-info.java (at line 4)\n" + 
			"	* @see Unknown\n" + 
			"	       ^^^^^^^\n" + 
			"Javadoc: Unknown cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in pack\\package-info.java (at line 6)\n" + 
			"	* @see Test#unknown()\n" + 
			"	            ^^^^^^^\n" + 
			"Javadoc: The method unknown() is undefined for the type Test\n" + 
			"----------\n" + 
			"3. ERROR in pack\\package-info.java (at line 8)\n" + 
			"	* @see Test#unknown\n" + 
			"	            ^^^^^^^\n" + 
			"Javadoc: unknown cannot be resolved or is not a field\n" + 
			"----------\n" + 
			"4. ERROR in pack\\package-info.java (at line 9)\n" + 
			"	* @param unexpected\n" + 
			"	   ^^^^^\n" + 
			"Javadoc: Unexpected tag\n" + 
			"----------\n" + 
			"5. ERROR in pack\\package-info.java (at line 10)\n" + 
			"	* @throws unexpected\n" + 
			"	   ^^^^^^\n" + 
			"Javadoc: Unexpected tag\n" + 
			"----------\n" + 
			"6. ERROR in pack\\package-info.java (at line 11)\n" + 
			"	* @return unexpected \n" + 
			"	   ^^^^^^\n" + 
			"Javadoc: Unexpected tag\n" + 
			"----------\n"
		);
	}
}
