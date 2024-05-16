/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTest_1_3 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;

	public JavadocTest_1_3(String name) {
		super(name);
	}

	public static Class testClass() {
		return JavadocTest_1_3.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] {
//			"testBug70892conform1", "testBug70892conform2"
//		};
//		TESTS_NUMBERS = new int[] { 101283 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_3);
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
		}
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocCommentsVisibility != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		if (this.reportMissingJavadocTags != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	}


	/**
	 * Test fix for bug 70891: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70891">70891</a>
	 * These two tests fail for 1.5 source level but should pass for 1.3 or 1.4
	 * @see JavadocTest_1_3
	 */
	/* (non-Javadoc)
	 * Test @param for generic class type parameter
	 */
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Valid type parameter reference
						  * @param <E> Type
						  */
						 public class X<E> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)\r
					* @param <E> Type\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)\r
					public class X<E> {}\r
					               ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Valid type parameter reference
						  * @param <E> Type extends RuntimeException
						  */
						 public class X<E extends RuntimeException> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)\r
					* @param <E> Type extends RuntimeException\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)\r
					public class X<E extends RuntimeException> {}\r
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""

		);
	}
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Valid type parameter reference
						  * @param <T> Type parameter 1
						  * @param <U> Type parameter 2
						  * @param <V> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)\r
					* @param <T> Type parameter 1\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)\r
					* @param <U> Type parameter 2\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)\r
					* @param <V> Type parameter 3\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)\r
					public class X<T, U, V> {}\r
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <E> Type parameter
						  */
						 public class X {}\
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <E> Type parameter
					         ^^^
				Javadoc: Invalid param tag name
				----------
				"""
		);
	}
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <E> Type parameter
						  */
						 public class X<E, F> {}\
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <E> Type parameter
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					public class X<E, F> {}
					               ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <T> Type parameter 1
						  * @param <U> Type parameter 2
						  * @param <V> Type parameter 3
						  */
						 public class X<T> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <T> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)
					public class X<T> {}
					               ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <T> Type parameter 1
						  * @param <X> Type parameter 2
						  * @param <U> Type parameter 2
						  * @param <E> Type parameter 2
						  * @param <V> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <T> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <X> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 6)
					* @param <E> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in X.java (at line 7)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 9)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Valid type parameter reference
						  * @param <V> Type parameter 3
						  * @param <U> Type parameter 2
						  * @param <T> Type parameter 1
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)\r
					* @param <V> Type parameter 3\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)\r
					* @param <U> Type parameter 2\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)\r
					* @param <T> Type parameter 1\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)\r
					public class X<T, U, V> {}\r
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <U> Type parameter 1
						  * @param <E> Type parameter 2
						  * @param <V> Type parameter 2
						  * @param <U> Type parameter 2
						  * @param <T> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <E> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 6)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in X.java (at line 7)
					* @param <T> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 9)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <T> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <T> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <U> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <U> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <U> Type parameter 3
						  * @param <V> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <U> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 6)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <T> Type parameter 3
						  * @param <V> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <T> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 6)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <T> Type parameter 3
						  * @param <U> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <T> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <U> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 6)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <V> Type parameter 3
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <V> Type parameter 2
						  * @param <X> Type parameter 2
						  * @param <U> Type parameter 1
						  * @param <E> Type parameter 2
						  * @param <U> Type parameter 2
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <X> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 6)
					* @param <E> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in X.java (at line 7)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 9)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference
						  * @param <V> Type parameter 2
						  * @param
						  * @param <U> Type parameter 1
						  */
						 public class X<T, U, V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				3. ERROR in X.java (at line 5)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)
					public class X<T, U, V> {}
					               ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference: compile error
						  * @param <T> Type parameter 2
						  * @param <V> Type parameter 2
						  * @param <U> Type parameter 1
						  */
						 public class X<T, , V> {}\
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <T> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)
					public class X<T, , V> {}
					              ^^
				Syntax error on tokens, delete these tokens
				----------
				5. ERROR in X.java (at line 7)
					public class X<T, , V> {}
					               ^
				Syntax error, insert "ClassBody" to complete CompilationUnit
				----------
				"""
		);
	}
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Invalid type parameter reference: compile error
						  * @param <T> Type parameter 2
						  * @param <V> Type parameter 2
						  * @param <U> Type parameter 1
						  */
						 public class X<T, U, V extend Exception> {}\
						"""
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				* @param <T> Type parameter 2
				         ^^^
			Javadoc: Invalid param tag name
			----------
			2. ERROR in X.java (at line 4)
				* @param <V> Type parameter 2
				         ^^^
			Javadoc: Invalid param tag name
			----------
			3. ERROR in X.java (at line 5)
				* @param <U> Type parameter 1
				         ^^^
			Javadoc: Invalid param tag name
			----------
			4. ERROR in X.java (at line 7)
				public class X<T, U, V extend Exception> {}
				^^^^^^^^^^^^^^^^^^^^^^
			Syntax error on token(s), misplaced construct(s)
			----------
			5. ERROR in X.java (at line 7)
				public class X<T, U, V extend Exception> {}
				                       ^^^^^^
			extend cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 7)
				public class X<T, U, V extend Exception> {}
				                                       ^
			Syntax error on token ">", = expected
			----------
			7. ERROR in X.java (at line 7)
				public class X<T, U, V extend Exception> {}
				                                          ^
			Syntax error, insert ";" to complete CompilationUnit
			----------
			"""
		);
	}

	/* (non-Javadoc)
	 * Test @param for generic method type parameter
	 */
	public void test023() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Valid type parameter reference
						 * @param <E> Type
						 */
						public <E> void foo() {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)\r
					* @param <E> Type\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 6)\r
					public <E> void foo() {}\r
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Valid type parameter reference
						 * @param <E> Type extends RuntimeException
						 * @param val int
						 * @param obj Object
						 */
						public <E extends RuntimeException> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)\r
					* @param <E> Type extends RuntimeException\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 8)\r
					public <E extends RuntimeException> void foo(int val, Object obj) {}\r
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test025() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Valid type parameter reference
						 * @param val int
						 * @param obj Object
						 * @param <T> Type parameter 1
						 * @param <U> Type parameter 2
						 * @param <V> Type parameter 3
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)\r
					* @param <T> Type parameter 1\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 7)\r
					* @param <U> Type parameter 2\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 8)\r
					* @param <V> Type parameter 3\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 10)\r
					public <T, U, V> void foo(int val, Object obj) {}\r
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param val int
						 * @param <E> Type parameter
						 * @param obj Object
						 */
						public void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @param <E> Type parameter
					         ^^^
				Javadoc: Invalid param tag name
				----------
				"""
		);
	}
	public void test027() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param <E> Type parameter
						 */
						public <E, F> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <E> Type parameter
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 6)
					public <E, F> void foo(int val, Object obj) {}
					        ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				3. ERROR in X.java (at line 6)
					public <E, F> void foo(int val, Object obj) {}
					                           ^^^
				Javadoc: Missing tag for parameter val
				----------
				4. ERROR in X.java (at line 6)
					public <E, F> void foo(int val, Object obj) {}
					                                       ^^^
				Javadoc: Missing tag for parameter obj
				----------
				"""
		);
	}
	public void test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param <T> Type parameter 1
						 * @param <U> Type parameter 2
						 * @param <V> Type parameter 3
						 * @param xxx int
						 * @param Obj Object
						 */
						public <T> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <T> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 6)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)
					* @param xxx int
					         ^^^
				Javadoc: Parameter xxx is not declared
				----------
				5. ERROR in X.java (at line 8)
					* @param Obj Object
					         ^^^
				Javadoc: Parameter Obj is not declared
				----------
				6. ERROR in X.java (at line 10)
					public <T> void foo(int val, Object obj) {}
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				7. ERROR in X.java (at line 10)
					public <T> void foo(int val, Object obj) {}
					                        ^^^
				Javadoc: Missing tag for parameter val
				----------
				8. ERROR in X.java (at line 10)
					public <T> void foo(int val, Object obj) {}
					                                    ^^^
				Javadoc: Missing tag for parameter obj
				----------
				"""
		);
	}
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param <T> Type parameter 1
						 * @param <X> Type parameter 2
						 * @param val int
						 * @param <U> Type parameter 2
						 * @param <E> Type parameter 2
						 * @param obj Object
						 * @param <V> Type parameter 3
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <T> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					* @param <X> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 7)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 8)
					* @param <E> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in X.java (at line 10)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 12)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test030() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Valid type parameter reference
						 * @param <V> Type parameter 3
						 * @param obj Object
						 * @param <U> Type parameter 2
						 * @param val int
						 * @param <T> Type parameter 1
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)\r
					* @param <V> Type parameter 3\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 6)\r
					* @param <U> Type parameter 2\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 8)\r
					* @param <T> Type parameter 1\r
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 10)\r
					public <T, U, V> void foo(int val, Object obj) {}\r
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				3. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				"""
		);
	}
	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param <T> Type parameter 3
						 * @param val int
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <T> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 7)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				3. ERROR in X.java (at line 7)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				"""
		);
	}
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param obj Object
						 * @param <U> Type parameter 3
						 * @param <V> Type parameter 3
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @param <U> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 6)
					* @param <V> Type parameter 3
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				"""
		);
	}
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param val int
						 * @param <V> Type parameter 2
						 * @param <X> Type parameter 2
						 * @param <U> Type parameter 1
						 * @param Object obj
						 * @param <E> Type parameter 2
						 * @param <U> Type parameter 2
						 * @param val int
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 6)
					* @param <X> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 7)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 8)
					* @param Object obj
					         ^^^^^^
				Javadoc: Parameter Object is not declared
				----------
				5. ERROR in X.java (at line 9)
					* @param <E> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 10)
					* @param <U> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				7. ERROR in X.java (at line 11)
					* @param val int
					         ^^^
				Javadoc: Duplicate tag for parameter
				----------
				8. ERROR in X.java (at line 13)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				9. ERROR in X.java (at line 13)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				"""
		);
	}
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference
						 * @param <V> Type parameter 2
						 * @param
						 * @param <U> Type parameter 1
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				3. ERROR in X.java (at line 6)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				6. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				"""
		);
	}
	// TODO (david) recovery seems not to work properly here:
	// we should have type parameters in method declaration.
	public void test036() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference: compile error
						 * @param <T> Type parameter 2
						 * @param <V> Type parameter 2
						 * @param <U> Type parameter 1
						 * @param val int
						 * @param obj Object
						 */
						public <T, , V> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <T> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 6)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 10)
					public <T, , V> void foo(int val, Object obj) {}
					       ^^^^^^^^
				Syntax error on tokens, delete these tokens
				----------
				"""
		);
	}
	public void test037() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * Invalid type parameter reference: compile error
						 * @param <T> Type parameter 2
						 * @param <V> Type parameter 2
						 * @param <U> Type parameter 1
						 * @param val int
						 * @param obj Object
						 */
						public <T, U, V extends Exceptions> void foo(int val, Object obj) {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param <T> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					* @param <V> Type parameter 2
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 6)
					* @param <U> Type parameter 1
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 10)
					public <T, U, V extends Exceptions> void foo(int val, Object obj) {}
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in X.java (at line 10)
					public <T, U, V extends Exceptions> void foo(int val, Object obj) {}
					                        ^^^^^^^^^^
				Exceptions cannot be resolved to a type
				----------
				"""
		);
	}
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 /**
					  * Invalid type parameter reference
					  * @param < Type
					  * @param < Type for parameterization
					  * @param <> Type
					  * @param <?> Type
					  * @param <*> Type
					  */
					 public class X<E> {}\
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param < Type
					         ^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param < Type for parameterization
					         ^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <> Type
					         ^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 6)
					* @param <?> Type
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in X.java (at line 7)
					* @param <*> Type
					         ^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 9)
					public class X<E> {}
					               ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 /**
					  * Invalid type parameter reference
					  * @param <E Type parameter of class X
					  * @param E> Type
					  * @param <<E> Type
					  * @param <<<E> Type
					  * @param <E>> Type
					  */
					 public class X<E> {}\
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @param <E Type parameter of class X
					         ^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 4)
					* @param E> Type
					         ^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <<E> Type
					         ^^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 6)
					* @param <<<E> Type
					         ^^^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in X.java (at line 7)
					* @param <E>> Type
					         ^^^^
				Javadoc: Invalid param tag name
				----------
				6. ERROR in X.java (at line 9)
					public class X<E> {}
					               ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}

	public void test040() {
		runConformReferenceTest(
			new String[] {
				"X.java",
				"""
					/**
					 * @category
					 */
					public class X {
					}
					"""
			}
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
				"""
					/**
					 * @see G#G(Object)
					 * @see G#G(Exception)
					 */
					public class X extends G<Exception> {
						X(Exception exc) { super(exc);}
					}
					class G<E extends Exception> {
						G(E e) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					* @see G#G(Object)
					         ^^^^^^^^^
				Javadoc: The constructor G(Object) is undefined
				----------
				2. ERROR in X.java (at line 5)
					public class X extends G<Exception> {
					                         ^^^^^^^^^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				3. ERROR in X.java (at line 8)
					class G<E extends Exception> {
					        ^^^^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
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
				"""
					class ComparableUtils {
					   public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException
					    {
					        return 0;
					    }
					    public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)
					        throws ClassCastException
					    {
					        return 0;
					    }
					}
					public final class X { \s
						/** Tests the method{@link ComparableUtils#compareTo(Object, Object, Class)} and
						 *  {@link ComparableUtils#compareTo(Object, Object)}.
						 */
					    public void testCompareTo() {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException
					               ^^^^^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in X.java (at line 2)
					public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException
					                                                                                                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				3. ERROR in X.java (at line 6)
					public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)
					               ^^^^^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. WARNING in X.java (at line 6)
					public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)
					               ^
				The type parameter X is hiding the type X
				----------
				5. ERROR in X.java (at line 14)
					*  {@link ComparableUtils#compareTo(Object, Object)}.
					                          ^^^^^^^^^
				Javadoc: The method compareTo(Object, Object, Class<T>) in the type ComparableUtils is not applicable for the arguments (Object, Object)
				----------
				""");
	}

	/**
	 * Test fix for bug 83127: [1.5][javadoc][dom] Wrong / strange bindings for references in javadoc to methods with type variables as parameter types
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83127"
	 */
	public void testBug83127a() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					/**\s
					 * @see Test#add(T)\s
					 * @see #add(T)
					 * @see Test#Test(T)
					 * @see #Test(T)
					 *   - warning = "The method add(Object) in the type Test is not applicable for
					 *                the arguments (T)"
					 *   - method binding = Test.add(Object)
					 *   - parameter binding = T of A
					 */
					public class Test<T> {
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 2)
					* @see Test#add(T)\s
					            ^^^
				Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)
				----------
				2. ERROR in Test.java (at line 3)
					* @see #add(T)
					        ^^^
				Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)
				----------
				3. ERROR in Test.java (at line 4)
					* @see Test#Test(T)
					            ^^^^^^^
				Javadoc: The constructor Test(T) is undefined
				----------
				4. ERROR in Test.java (at line 5)
					* @see #Test(T)
					        ^^^^^^^
				Javadoc: The constructor Test(T) is undefined
				----------
				5. ERROR in Test.java (at line 11)
					public class Test<T> {
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				6. ERROR in Test.java (at line 18)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				7. ERROR in Test.java (at line 18)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}
	public void testBug83127b() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					/**\s
					 * @see Sub#add(T)
					 * @see Sub#Sub(T)
					 *   - warning = "The method add(Object) in the type Test is not applicable for
					 *                the arguments (T)"
					 *   - method binding = Test.add(Object)
					 *   - parameter binding = T of A
					 *     -> Do we need to change this as T natually resolved to TypeVariable?
					 *        As compiler raises a warning, it's perhaps not a problem now...
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 2)
					* @see Sub#add(T)
					           ^^^
				Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)
				----------
				2. ERROR in Test.java (at line 3)
					* @see Sub#Sub(T)
					           ^^^^^^
				Javadoc: The constructor Sub(T) is undefined
				----------
				3. ERROR in Test.java (at line 11)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in Test.java (at line 18)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in Test.java (at line 18)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}
	public void testBug83127c() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					/**\s
					 * @see Sub#add(E)\s
					 * @see Sub#Sub(E)
					 *   - warning = "E cannot be resolved to a type"
					 *   - method binding = null
					 *   - parameter binding = null
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 2)
					* @see Sub#add(E)\s
					               ^
				Javadoc: E cannot be resolved to a type
				----------
				2. ERROR in Test.java (at line 3)
					* @see Sub#Sub(E)
					               ^
				Javadoc: E cannot be resolved to a type
				----------
				3. ERROR in Test.java (at line 8)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in Test.java (at line 15)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in Test.java (at line 15)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}
	public void testBug83127d() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"""
					public class Unrelated1<E extends Number> {
						public Unrelated1(E e) {}
						public boolean add(E e) { return false; }
					}
					""",
				"Test.java",
				"""
					/**\s
					 * @see Unrelated1#add(E)
					 * @see Unrelated1#Unrelated1(E)
					 *   - warning = "E cannot be resolved to a type"
					 *   - method binding = null
					 *   - parameter binding = null
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Unrelated1.java (at line 1)
					public class Unrelated1<E extends Number> {
					                        ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				----------
				1. ERROR in Test.java (at line 2)
					* @see Unrelated1#add(E)
					                      ^
				Javadoc: E cannot be resolved to a type
				----------
				2. ERROR in Test.java (at line 3)
					* @see Unrelated1#Unrelated1(E)
					                             ^
				Javadoc: E cannot be resolved to a type
				----------
				3. ERROR in Test.java (at line 8)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in Test.java (at line 15)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in Test.java (at line 15)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}
	public void testBug83127e() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"""
					public class Unrelated1<E extends Number> {
						public Unrelated1(E e) {}
						public boolean add(E e) { return false; }
					}
					""",
				"Test.java",
				"""
					/**\s
					 * @see Unrelated1#add(Object)
					 * @see Unrelated1#Unrelated1(Object)
					 *   - warning = "The method add(Object) in the type Test is not applicable for
					 *                the arguments (Object)"
					 *   - method binding = Unrelated1.add(Number)
					 *   - parameter binding = java.lang.Object
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Unrelated1.java (at line 1)
					public class Unrelated1<E extends Number> {
					                        ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				----------
				1. ERROR in Test.java (at line 2)
					* @see Unrelated1#add(Object)
					                  ^^^
				Javadoc: The method add(Number) in the type Unrelated1 is not applicable for the arguments (Object)
				----------
				2. ERROR in Test.java (at line 3)
					* @see Unrelated1#Unrelated1(Object)
					                  ^^^^^^^^^^^^^^^^^^
				Javadoc: The constructor Unrelated1(Object) is undefined
				----------
				3. ERROR in Test.java (at line 9)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in Test.java (at line 15)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in Test.java (at line 15)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}
	public void testBug83127f() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"""
					public class Unrelated1<E extends Number> {
						public Unrelated1(E e) {}
						public boolean add(E e) { return false; }
					}
					""",
				"Test.java",
				"""
					/**\s
					 * @see Unrelated1#add(Number)
					 * @see Unrelated1#Unrelated1(Number)
					 *   - no warning
					 *   - method binding = Unrelated1.add(Number)
					 *   - parameter binding = java.lang.Number
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Unrelated1.java (at line 1)
					public class Unrelated1<E extends Number> {
					                        ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				----------
				1. ERROR in Test.java (at line 8)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in Test.java (at line 14)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				3. ERROR in Test.java (at line 14)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug83127g() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"""
					public class Unrelated1<E extends Number> {
						public Unrelated1(E e) {}
						public boolean add(E e) { return false; }
					}
					""",
				"Test.java",
				"""
					/**\s
					 * @see Unrelated1#add(Integer)
					 * @see Unrelated1#Unrelated1(Integer)
					 *   - warning = "The method add(Object) in the type Test is not applicable for
					 *                the arguments (Integer)"
					 *   - method binding = Unrelated1.add(Number)
					 *   - parameter binding = java.lang.Integer
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Unrelated1.java (at line 1)
					public class Unrelated1<E extends Number> {
					                        ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				----------
				1. ERROR in Test.java (at line 2)
					* @see Unrelated1#add(Integer)
					                  ^^^
				Javadoc: The method add(Number) in the type Unrelated1 is not applicable for the arguments (Integer)
				----------
				2. ERROR in Test.java (at line 3)
					* @see Unrelated1#Unrelated1(Integer)
					                  ^^^^^^^^^^^^^^^^^^^
				Javadoc: The constructor Unrelated1(Integer) is undefined
				----------
				3. ERROR in Test.java (at line 9)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in Test.java (at line 16)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				5. ERROR in Test.java (at line 16)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug83127h() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated2.java",
				"""
					public interface Unrelated2<E> {
						boolean add(E e);
					}
					""",
				"Test.java",
				"""
					/**\s
					 * @see Unrelated2#add(T)
					 *   - warning = "The method add(Object) in the type Test is not applicable for
					 *                the arguments (T)"
					 *   - method binding = Unrelated2.add(Object)
					 *   - parameter binding = T of A
					 *     -> Do we need to change this as T natually resolved to TypeVariable?
					 *        As compiler raises a warning, it's perhaps not a problem now...
					 */
					public class Test<T>{
						Test(T t) {}
					    public boolean add(T t) {
					        return true;
					    }
					}
					
					class Sub<E extends Number> extends Test<E> {
						Sub (E e) {super(null);}
					    public boolean add(E e) {
					        if (e.doubleValue() > 0)
					            return false;
					        return super.add(e);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in Unrelated2.java (at line 1)
					public interface Unrelated2<E> {
					                            ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				----------
				1. ERROR in Test.java (at line 2)
					* @see Unrelated2#add(T)
					                  ^^^
				Javadoc: The method add(Object) in the type Unrelated2 is not applicable for the arguments (T)
				----------
				2. ERROR in Test.java (at line 10)
					public class Test<T>{
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				3. ERROR in Test.java (at line 17)
					class Sub<E extends Number> extends Test<E> {
					          ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				4. ERROR in Test.java (at line 17)
					class Sub<E extends Number> extends Test<E> {
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}

	/**
	 * Bug 83393: [1.5][javadoc] reference to vararg method also considers non-array type as correct
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83393"
	 */
	public void testBug83393a() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						public void foo(int a, int b) {}\s
						public void foo(int a, int... args) {}
						public void foo(String... args) {}
						public void foo(Exception str, boolean... args) {}
					}
					""",
				"Valid.java",
				"""
					/**
					 * @see Test#foo(int, int)
					 * @see Test#foo(int, int[])
					 * @see Test#foo(int, int...)
					 * @see Test#foo(String[])
					 * @see Test#foo(String...)
					 * @see Test#foo(Exception, boolean[])
					 * @see Test#foo(Exception, boolean...)
					 */
					public class Valid {}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 3)\r
					public void foo(int a, int... args) {}\r
					                       ^^^^^^^^^^^
				Syntax error, varargs are only available if source level is 1.5 or greater
				----------
				2. ERROR in Test.java (at line 4)\r
					public void foo(String... args) {}\r
					                ^^^^^^^^^^^^^^
				Syntax error, varargs are only available if source level is 1.5 or greater
				----------
				3. ERROR in Test.java (at line 5)\r
					public void foo(Exception str, boolean... args) {}\r
					                               ^^^^^^^^^^^^^^^
				Syntax error, varargs are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug83393b() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						public void foo(int a, int b) {}\s
						public void foo(int a, int... args) {}
						public void foo(String... args) {}
						public void foo(Exception str, boolean... args) {}
					}
					""",
				"Invalid.java",
				"""
					/**
					 * @see Test#foo(int)
					 * @see Test#foo(int, int, int)
					 * @see Test#foo()
					 * @see Test#foo(String)
					 * @see Test#foo(String, String)
					 * @see Test#foo(Exception)
					 * @see Test#foo(Exception, boolean)
					 * @see Test#foo(Exception, boolean, boolean)
					 */
					public class Invalid {}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 3)
					public void foo(int a, int... args) {}
					                       ^^^^^^^^^^^
				Syntax error, varargs are only available if source level is 1.5 or greater
				----------
				2. ERROR in Test.java (at line 4)
					public void foo(String... args) {}
					                ^^^^^^^^^^^^^^
				Syntax error, varargs are only available if source level is 1.5 or greater
				----------
				3. ERROR in Test.java (at line 5)
					public void foo(Exception str, boolean... args) {}
					                               ^^^^^^^^^^^^^^^
				Syntax error, varargs are only available if source level is 1.5 or greater
				----------
				----------
				1. ERROR in Invalid.java (at line 2)
					* @see Test#foo(int)
					            ^^^
				Javadoc: The method foo(int, int) in the type Test is not applicable for the arguments (int)
				----------
				2. ERROR in Invalid.java (at line 3)
					* @see Test#foo(int, int, int)
					            ^^^
				Javadoc: The method foo(int, int) in the type Test is not applicable for the arguments (int, int, int)
				----------
				3. ERROR in Invalid.java (at line 4)
					* @see Test#foo()
					            ^^^
				Javadoc: The method foo(String[]) in the type Test is not applicable for the arguments ()
				----------
				4. ERROR in Invalid.java (at line 5)
					* @see Test#foo(String)
					            ^^^
				Javadoc: The method foo(String[]) in the type Test is not applicable for the arguments (String)
				----------
				5. ERROR in Invalid.java (at line 6)
					* @see Test#foo(String, String)
					            ^^^
				Javadoc: The method foo(int, int) in the type Test is not applicable for the arguments (String, String)
				----------
				6. ERROR in Invalid.java (at line 7)
					* @see Test#foo(Exception)
					            ^^^
				Javadoc: The method foo(Exception, boolean[]) in the type Test is not applicable for the arguments (Exception)
				----------
				7. ERROR in Invalid.java (at line 8)
					* @see Test#foo(Exception, boolean)
					            ^^^
				Javadoc: The method foo(Exception, boolean[]) in the type Test is not applicable for the arguments (Exception, boolean)
				----------
				8. ERROR in Invalid.java (at line 9)
					* @see Test#foo(Exception, boolean, boolean)
					            ^^^
				Javadoc: The method foo(Exception, boolean[]) in the type Test is not applicable for the arguments (Exception, boolean, boolean)
				----------
				"""
		);
	}

	/**
	 * Bug 86769: [javadoc] Warn/Error for 'Missing javadoc comments' doesn't recognize private inner classes
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=86769"
	 */
	public void testBug86769() {

		/* Deleted a completely meaningless test that could only serve as a torture test for the parser.
		   The test is still run in 1.5+ modes where it makes sense to run it - The test here was a nuisance
		   failing every time there is some serious grammar change that alters the semi-random behavior in
		   Diagnose Parser.
		 */
		return;
	}

	/**
	 * Bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
	 */
	public void testBug83804() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
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
					 * @deprecated accepted by javadoc.exe although javadoc 1.5 spec does not say that's a valid tag
					 * @other-tags are valid
					 */
					package pack;
					""",
				"pack/Test.java",
				"""
					/**
					 * Invalid javadoc
					 */
					package pack;
					public class Test {
						public int field;
						public void foo() {}
					}
					"""
			},
			""
		);
	}
	/**
	 * Bug 96237: [javadoc] Inner types must be qualified
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=96237"
	 */
	public void testBug96237_Public01() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runConformTest(
			new String[] {
				"comment6/Valid.java",
				"""
					package comment6;
					public class Valid {
					    /**
					     * @see Valid.Inner
					     */
					    public class Inner { }
					}
					/**
					 * See also {@link Valid.Inner}
					 */
					class Sub2 extends Valid { }"""
			}
		);
	}
	public void testBug96237_Public02() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"comment6/Invalid.java",
				"""
					package comment6;
					public class Invalid {
					    /**
					     * @see Inner
					     */
					    public class Inner { }
					}
					/**
					 * See also {@link Inner}\s
					 */
					class Sub1 extends Invalid { }
					"""
			},
			"""
				----------
				1. ERROR in comment6\\Invalid.java (at line 4)\r
					* @see Inner\r
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug96237_Public03() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"comment6a/def/Test.java",
				"""
					package comment6a.def;
					public class Test {
					    /**
					     * @see Inner
					     */
					    public class Inner { }
					}
					""",
				"comment6a/test/Invalid.java",
				"""
					package comment6a.test;
					import comment6a.def.Test;
					/**
					 * See also {@link Inner}
					 */
					public class Invalid extends Test {\s
					}""",
				"comment6a/test/Invalid2.java",
				"""
					package comment6a.test;
					import comment6a.def.Test;
					/**
					 * @see Test.Inner
					 */
					public class Invalid2 extends Test {\s
					}""",
				"comment6a/test/Valid.java",
				"""
					package comment6a.test;
					import comment6a.def.Test;
					/**
					 * @see comment6a.def.Test.Inner
					 */
					public class Valid extends Test {\s
					}"""
			},
			"""
				----------
				1. ERROR in comment6a\\def\\Test.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in comment6a\\test\\Invalid.java (at line 4)
					* See also {@link Inner}
					                  ^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in comment6a\\test\\Invalid2.java (at line 4)
					* @see Test.Inner
					       ^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug96237_Public04() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"comment6b/Invalid.java",
				"""
					package comment6b;
					
					/**
					 * @see Inner
					 */
					public class Invalid implements Test {\s
					}""",
				"comment6b/Test.java",
				"""
					package comment6b;
					public interface Test {
					    /**
					     * @see Inner
					     */
					    public class Inner { }
					}
					""",
				"comment6b/Valid.java",
				"""
					package comment6b;
					
					/**
					 * @see Test.Inner
					 */
					public class Valid implements Test {\s
					}"""
			},
			"""
				----------
				1. ERROR in comment6b\\Invalid.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in comment6b\\Test.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug96237_Public05() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/a/Test.java",
				"""
					package test.a;
					/**
					 * @see Inner
					 * @see Test.Inner
					 */
					public class Test {
						class Inner {}
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\a\\Test.java (at line 3)
					* @see Inner
					       ^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				2. ERROR in test\\a\\Test.java (at line 4)
					* @see Test.Inner
					       ^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				"""
		);
	}
	public void testBug96237_Public06() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/b/Test.java",
				"""
					package test.b;
					/**\s
					 * @see Inner.Level2
					 * @see Test.Inner.Level2
					 */
					public class Test {
						/**\s
						 * @see Level2
						 * @see Test.Inner.Level2
						 */
						public class Inner {
							class Level2 {}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\b\\Test.java (at line 3)
					* @see Inner.Level2
					       ^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				2. ERROR in test\\b\\Test.java (at line 4)
					* @see Test.Inner.Level2
					       ^^^^^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				3. ERROR in test\\b\\Test.java (at line 8)
					* @see Level2
					       ^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				4. ERROR in test\\b\\Test.java (at line 9)
					* @see Test.Inner.Level2
					       ^^^^^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				"""
		);
	}
	public void testBug96237_Public07() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/c/Test.java",
				"""
					package test.c;
					/**
					 * @see Inner.Level2.Level3
					 * @see Test.Inner.Level2.Level3
					 */
					public class Test {
						public class Inner {
							/**
							 * @see Level3
							 * @see Level2.Level3
							 * @see Inner.Level2.Level3
							 * @see Test.Inner.Level2.Level3
							 */
							public class Level2 {
								class Level3 {
								}
							}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\c\\Test.java (at line 3)
					* @see Inner.Level2.Level3
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				2. ERROR in test\\c\\Test.java (at line 4)
					* @see Test.Inner.Level2.Level3
					       ^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				3. ERROR in test\\c\\Test.java (at line 9)
					* @see Level3
					       ^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				4. ERROR in test\\c\\Test.java (at line 10)
					* @see Level2.Level3
					       ^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				5. ERROR in test\\c\\Test.java (at line 11)
					* @see Inner.Level2.Level3
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				6. ERROR in test\\c\\Test.java (at line 12)
					* @see Test.Inner.Level2.Level3
					       ^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				"""
		);
	}
	public void testBug96237_Public08() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/d/Reference.java",
				"""
					package test.d;
					class Reference {
					}
					""",
				"test/d/Test.java",
				"""
					package test.d;
					/**
					 * @see Secondary
					 * @see Reference
					 */
					public class Test {
					}
					class Secondary {}"""
			},
			"""
				----------
				1. ERROR in test\\d\\Test.java (at line 3)
					* @see Secondary
					       ^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				2. ERROR in test\\d\\Test.java (at line 4)
					* @see Reference
					       ^^^^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				"""
		);
	}
	public void testBug96237_Private01() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"comment6/Valid.java",
				"""
					package comment6;
					public class Valid {
					    /**
					     * @see Valid.Inner
					     */
					    public class Inner { }
					}
					/**
					 * See also {@link Valid.Inner}
					 */
					class Sub2 extends Valid { }"""
			}
		);
	}
	public void testBug96237_Private02() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runNegativeTest(
			new String[] {
				"comment6/Invalid.java",
				"""
					package comment6;
					public class Invalid {
					    /**
					     * @see Inner
					     */
					    public class Inner { }
					}
					/**
					 * See also {@link Inner}\s
					 */
					class Sub1 extends Invalid { }
					"""
			},
			"""
				----------
				1. ERROR in comment6\\Invalid.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in comment6\\Invalid.java (at line 9)
					* See also {@link Inner}\s
					                  ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug96237_Private03() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runNegativeTest(
			new String[] {
				"comment6a/def/Test.java",
					"""
						package comment6a.def;
						public class Test {
						    /**
						     * @see Inner
						     */
						    public class Inner { }
						}
						""",
				"comment6a/test/Invalid.java",
				"""
					package comment6a.test;
					import comment6a.def.Test;
					/**
					 * See also {@link Inner}
					 */
					public class Invalid extends Test {\s
					}""",
				"comment6a/test/Invalid2.java",
				"""
					package comment6a.test;
					import comment6a.def.Test;
					/**
					 * @see Test.Inner
					 */
					public class Invalid2 extends Test {\s
					}""",
				"comment6a/test/Valid.java",
				"""
					package comment6a.test;
					import comment6a.def.Test;
					/**
					 * @see comment6a.def.Test.Inner
					 */
					public class Valid extends Test {\s
					}"""
			},
			"""
				----------
				1. ERROR in comment6a\\def\\Test.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in comment6a\\test\\Invalid.java (at line 4)
					* See also {@link Inner}
					                  ^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in comment6a\\test\\Invalid2.java (at line 4)
					* @see Test.Inner
					       ^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug96237_Private04() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runNegativeTest(
			new String[] {
				"comment6b/Invalid.java",
				"""
					package comment6b;
					
					/**
					 * @see Inner
					 */
					public class Invalid implements Test {\s
					}""",
				"comment6b/Test.java",
				"""
					package comment6b;
					public interface Test {
					    /**
					     * @see Inner
					     */
					    public class Inner { }
					}
					""",
				"comment6b/Valid.java",
				"""
					package comment6b;
					
					/**
					 * @see Test.Inner
					 */
					public class Valid implements Test {\s
					}"""
			},
			"""
				----------
				1. ERROR in comment6b\\Invalid.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in comment6b\\Test.java (at line 4)
					* @see Inner
					       ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug96237_Private05() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/a/Test.java",
				"""
					package test.a;
					/**
					 * @see Inner
					 * @see Test.Inner
					 */
					public class Test {
						class Inner {}
					}
					"""
			}
		);
	}
	public void testBug96237_Private06() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/b/Test.java",
				"""
					package test.b;
					/**\s
					 * @see Inner.Level2
					 * @see Test.Inner.Level2
					 */
					public class Test {
						/**\s
						 * @see Level2
						 * @see Test.Inner.Level2
						 */
						public class Inner {
							class Level2 {}
						}
					}
					"""
			}
		);
	}
	public void testBug96237_Private07() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/c/Test.java",
				"""
					package test.c;
					/**
					 * @see Inner.Level2.Level3
					 * @see Test.Inner.Level2.Level3
					 */
					public class Test {
						public class Inner {
							/**
							 * @see Level3
							 * @see Level2.Level3
							 * @see Inner.Level2.Level3
							 * @see Test.Inner.Level2.Level3
							 */
							public class Level2 {
								class Level3 {
								}
							}
						}
					}
					"""
			}
		);
	}
	public void testBug96237_Private08() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/d/Reference.java",
				"""
					package test.d;
					class Reference {
					}
					""",
				"test/d/Test.java",
				"""
					package test.d;
					/**
					 * @see Secondary
					 * @see Reference
					 */
					public class Test {
					}
					class Secondary {}"""
			}
		);
	}

	/**
	 * Bug 95286: [1.5][javadoc] package-info.java incorrectly flags "Missing comment for public declaration"
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=95286"
	 */
	public void testBug95286_Default() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
		runConformTest(
			new String[] {
				"test/package-info.java",
				"""
					/**
					 * Javadoc for all package\s
					 */
					package test;
					"""
			}
		);
	}
	public void testBug95286_Private() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/package-info.java",
				"""
					/**
					 * Javadoc for all package\s
					 */
					package test;
					"""
			}
		);
	}

	/**
	 * Bug 95521: [1.5][javadoc] validation with @see tag not working for generic method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=95521"
	 */
	public void testBug95521() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					
					/** Test */
					public class X implements I {
						/**
						 * @see test.I#foo(java.lang.Class)
						 */
						public <T> G<T> foo(Class<T> stuffClass) {
							return null;
						}
					}
					/** Interface */
					interface I {
					    /**
					     * @param <T>
					     * @param stuffClass\s
					     * @return stuff
					     */
					    public <T extends Object> G<T> foo(Class<T> stuffClass);
					}
					/**\s
					 * @param <T>
					 */
					class G<T> {}
					"""
			},
			"""
				----------
				1. ERROR in test\\X.java (at line 8)
					public <T> G<T> foo(Class<T> stuffClass) {
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in test\\X.java (at line 8)
					public <T> G<T> foo(Class<T> stuffClass) {
					             ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				3. ERROR in test\\X.java (at line 8)
					public <T> G<T> foo(Class<T> stuffClass) {
					                          ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				4. ERROR in test\\X.java (at line 15)
					* @param <T>
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in test\\X.java (at line 19)
					public <T extends Object> G<T> foo(Class<T> stuffClass);
					        ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				6. ERROR in test\\X.java (at line 19)
					public <T extends Object> G<T> foo(Class<T> stuffClass);
					                            ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				7. ERROR in test\\X.java (at line 19)
					public <T extends Object> G<T> foo(Class<T> stuffClass);
					                                         ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				8. ERROR in test\\X.java (at line 22)
					* @param <T>
					         ^^^
				Javadoc: Invalid param tag name
				----------
				9. ERROR in test\\X.java (at line 24)
					class G<T> {}
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				""");
	}
	public void testBug95521b() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					
					/** Test */
					public class X {
					    /**
					     * @param <T>
					     * @param classT\s
					     */
						public <T> X(Class<T> classT) {
						}
					    /**
					     * @param <T>
					     * @param classT
					     * @return classT
					     */
						public <T> Class<T> foo(Class<T> classT) {
							return classT;
						}
					}
					/** Super class */
					class Y extends X {
						/**
						 * @see X#X(java.lang.Class)
						 */
						public <T> Y(Class<T> classT) {
							super(classT);
						}
					
						/**
						 * @see X#foo(java.lang.Class)
						 */
					    public <T extends Object> Class<T> foo(Class<T> stuffClass) {
					    	return null;
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @param <T>
					         ^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in test\\X.java (at line 9)
					public <T> X(Class<T> classT) {
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				3. ERROR in test\\X.java (at line 9)
					public <T> X(Class<T> classT) {
					                   ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				4. ERROR in test\\X.java (at line 12)
					* @param <T>
					         ^^^
				Javadoc: Invalid param tag name
				----------
				5. ERROR in test\\X.java (at line 16)
					public <T> Class<T> foo(Class<T> classT) {
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				6. ERROR in test\\X.java (at line 16)
					public <T> Class<T> foo(Class<T> classT) {
					                 ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				7. ERROR in test\\X.java (at line 16)
					public <T> Class<T> foo(Class<T> classT) {
					                              ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				8. ERROR in test\\X.java (at line 25)
					public <T> Y(Class<T> classT) {
					        ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				9. ERROR in test\\X.java (at line 25)
					public <T> Y(Class<T> classT) {
					                   ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				10. ERROR in test\\X.java (at line 25)
					public <T> Y(Class<T> classT) {
					                      ^^^^^^
				Javadoc: Missing tag for parameter classT
				----------
				11. ERROR in test\\X.java (at line 32)
					public <T extends Object> Class<T> foo(Class<T> stuffClass) {
					        ^^^^^^^^^^^^^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				12. ERROR in test\\X.java (at line 32)
					public <T extends Object> Class<T> foo(Class<T> stuffClass) {
					                                ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				13. ERROR in test\\X.java (at line 32)
					public <T extends Object> Class<T> foo(Class<T> stuffClass) {
					                                             ^
				Syntax error, parameterized types are only available if source level is 1.5 or greater
				----------
				""");
	}

	/**
	 * Bug 101283: [1.5][javadoc] Javadoc validation raises missing implementation in compiler
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=101283"
	 */
	public void testBug101283a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T, F> {
					
						/**
						 * @param <T> \s
						 * @param <F>
						 */
						static class Entry<L, R> {
							// empty
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<T, F> {
					               ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in X.java (at line 4)
					* @param <T> \s
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <F>
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)
					static class Entry<L, R> {
					                   ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug101283b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T, F> {
					
						/**
						 * @see T Variable\s
						 * @see F Variable
						 */
						static class Entry<L, R> {
							// empty
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<T, F> {
					               ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in X.java (at line 4)
					* @see T Variable\s
					       ^
				Javadoc: Invalid reference
				----------
				3. ERROR in X.java (at line 5)
					* @see F Variable
					       ^
				Javadoc: Invalid reference
				----------
				4. ERROR in X.java (at line 7)
					static class Entry<L, R> {
					                   ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug101283c() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T, F> {
					
						/**
						 * @param <T> \s
						 * @param <F>
						 */
						class Entry<L, R> {
							// empty
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<T, F> {
					               ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in X.java (at line 4)
					* @param <T> \s
					         ^^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <F>
					         ^^^
				Javadoc: Invalid param tag name
				----------
				4. ERROR in X.java (at line 7)
					class Entry<L, R> {
					            ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug101283d() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T, F> {
					
						/**
						 * @see T Variable\s
						 * @see F Variable
						 */
						class Entry<L, R> {
							// empty
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<T, F> {
					               ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				2. ERROR in X.java (at line 4)
					* @see T Variable\s
					       ^
				Javadoc: Invalid reference
				----------
				3. ERROR in X.java (at line 5)
					* @see F Variable
					       ^
				Javadoc: Invalid reference
				----------
				4. ERROR in X.java (at line 7)
					class Entry<L, R> {
					            ^^^^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	public void testBug101283e() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					/**
					 * @see #foo()
					 */
					public interface X<T> {
					
						public T foo();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public interface X<T> {
					                   ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
	// Verify that ProblemReasons.InheritedNameHidesEnclosingName is not reported as Javadoc error
	public void testBug101283g() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						int foo() { return 0; }
						class XX extends X2 {
							int bar() {
								return foo();
							}
						}
					}
					class X2 {
						int foo() {
							return 0;
						}
					}
					""",
				"test/Y.java",
				"""
					package test;
					public class Y {
						int foo;
						class YY extends Y2 {
						/**
						 *  @see #foo
						 */
							int bar() {
								return foo;
							}
						}
					}
					class Y2 {
						int foo;
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\X.java (at line 6)\r
					return foo();\r
					       ^^^
				The method foo is defined in an inherited type and an enclosing scope
				----------
				----------
				1. ERROR in test\\Y.java (at line 9)\r
					return foo;\r
					       ^^^
				The field foo is defined in an inherited type and an enclosing scope\s
				----------
				"""
		);
	}

	/**
	 * Bug 112346: [javadoc] {&#064;inheritedDoc} should be inactive for non-overridden method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=112346"
	 */
	public void testBug112346() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					/**
					 * Test references
					 * @see Test#field
					 * @see Test#foo()
					 */
					public class Test<T> {
						T field;
						T foo() { return null; }
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 6)
					public class Test<T> {
					                  ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				""");
	}

	/**
	 * Bug 119857: [javadoc] Some inner class references should be flagged as unresolved
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=119857"
	 */
	public void testBug119857() {
		runNegativeTest(
			new String[] {
				"DefaultInformationControl.java",
				"""
					public class DefaultInformationControl {
						public interface IInformationPresenter {
							/**
							 * Updates the given presentation of the given information and
							 * thereby may manipulate the information to be displayed. The manipulation
							 * could be the extraction of textual encoded style information etc. Returns the
							 * manipulated information.
							 *
							 * @param hoverInfo the information to be presented
							 * @param maxWidth the maximal width in pixels
							 * @param maxHeight the maximal height in pixels
							 *
							 * @return the manipulated information
							 * @deprecated As of 3.2, replaced by {@link IInformationPresenterExtension#updatePresentation(String, int, int)}
							 * 				see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38528 for details.
							 */
							String updatePresentation(String hoverInfo, int maxWidth, int maxHeight);
						}
						/**
						 * An information presenter determines the style presentation
						 * of information displayed in the default information control.
						 * The interface can be implemented by clients.
						 *\s
						 * @since 3.2
						 */
						public interface IInformationPresenterExtension {
						\t
							/**
							 * Updates the given presentation of the given information and
							 * thereby may manipulate the information to be displayed. The manipulation
							 * could be the extraction of textual encoded style information etc. Returns the
							 * manipulated information.
							 * <p>
							 * Replaces {@link IInformationPresenter#updatePresentation(String, int, int)}
							 * <em>Make sure that you do not pass in a <code>Display</code></em> until
							 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=38528 is fixed.
							 * </p>
							 *
							 * @param hoverInfo the information to be presented
							 * @param maxWidth the maximal width in pixels
							 * @param maxHeight the maximal height in pixels
							 *
							 * @return the manipulated information
							 */
							String updatePresentation(String hoverInfo, int maxWidth, int maxHeight);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in DefaultInformationControl.java (at line 14)
					* @deprecated As of 3.2, replaced by {@link IInformationPresenterExtension#updatePresentation(String, int, int)}
					                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in DefaultInformationControl.java (at line 34)
					* Replaces {@link IInformationPresenter#updatePresentation(String, int, int)}
					                  ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug119857_Fields() {
		runNegativeTest(
			new String[] {
				"TestFields.java",
				"""
					/**
					 * @see MyInnerClass#foo
					 */
					public class TestFields {
					    /**
					     * @see MyInnerClass#foo
					     */
					    public class MyInnerClass {
					            Object foo;
					    }
					}"""
			},
			"""
				----------
				1. ERROR in TestFields.java (at line 6)
					* @see MyInnerClass#foo
					       ^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug119857_Methods() {
		runNegativeTest(
			new String[] {
				"TestMethods.java",
				"""
					/**
					 * @see MyInnerClass#foo()
					 * @see MyInnerInterface#foo()
					 */
					public class TestMethods {
					    /**
					     * @see MyInnerInterface#foo()
					     */
					    public class MyInnerClass {
					            public void foo() {}
					    }
					    /**
					     * @see MyInnerClass#foo()
					     */
					    public interface MyInnerInterface {
					            public void foo();
					    }
					}"""
			},
			"""
				----------
				1. ERROR in TestMethods.java (at line 7)
					* @see MyInnerInterface#foo()
					       ^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in TestMethods.java (at line 13)
					* @see MyInnerClass#foo()
					       ^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug119857_Types() {
		runNegativeTest(
			new String[] {
				"TestTypes.java",
				"""
					/**
					 * @see MyInnerClass
					 * @see MyInnerInterface
					 */
					public class TestTypes {
						/**
						 * @see MyInnerInterface
						 */
						public class MyInnerClass {
						        public void foo() {}
						}
						/**
						 * @see MyInnerClass
						 */
						public interface MyInnerInterface {
						        public void foo();
						}
					}"""
			},
			"""
				----------
				1. ERROR in TestTypes.java (at line 7)
					* @see MyInnerInterface
					       ^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in TestTypes.java (at line 13)
					* @see MyInnerClass
					       ^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug119857_Private01() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"pack/Test.java",
				"""
					package pack;
					public class Test {
						static class Inner {
							public Object foo() { return null; }
						}
						public Inner field;
						/**\s
						 * @see Inner#foo()
						 */
						public Object foo() {
							return field.foo();
						}
					}
					"""
			}
		);
	}
	public void testBug119857_Public01() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"pack/Test.java",
				"""
					package pack;
					public class Test {
						static class Inner {
							public Object foo() { return null; }
						}
						public Inner field;
						/**\s
						 * @see Inner#foo()
						 */
						public Object foo() {
							return field.foo();
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in pack\\Test.java (at line 8)\r
					* @see Inner#foo()\r
					       ^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				"""
		);
	}
	public void testBug119857_Private02() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"test/Test.java",
				"""
					package test;
					public class Test {
						static class Inner1 {
							public Object foo() { return null; }
						}
						static class Inner2 {
							public Inner1 field;
							/**\s
							 * @see Inner1#foo()
							 */
							public Object foo() {
								return field.foo();
							}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\Test.java (at line 9)\r
					* @see Inner1#foo()\r
					       ^^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	public void testBug119857_Public02() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runConformTest(
			new String[] {
				"test/Test.java",
				"""
					package test;
					public class Test {
						static class Inner1 {
							public Object foo() { return null; }
						}
						static class Inner2 {
							public Inner1 field;
							/**\s
							 * @see Inner1#foo()
							 */
							public Object foo() {
								return field.foo();
							}
						}
					}
					"""
			}
		);
	}
	public void testBug119857_Public03() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"pack/Test.java",
				"""
					package pack;
					public class Test {
						static class Inner1 {
							public Object foo() { return null; }
						}
						public static class Inner2 {
							public Inner1 field;
							/**\s
							 * @see Inner1#foo()
							 */
							public Object foo() {
								return field.foo();
							}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in pack\\Test.java (at line 9)
					* @see Inner1#foo()
					       ^^^^^^
				Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference
				----------
				"""
		);
	}

	/**
	 * Bug 145007: [1.5][javadoc] Generics + Inner Class -> Javadoc "missing @throws" warning
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=145007"
	 */
	public void testBug145007() {
		runNegativeTest(
			new String[] {
				"TestClass.java",
				"""
					class TestClass<T> {
					    static class Test1 {
					        /**
					         * A simple method that demonstrates tag problems
					         *\s
					         * @return a string
					         * @throws MyException
					         *             if something goes wrong
					         */
					        public String getString() throws MyException {
					            throw new MyException();
					        }
					    }
					
					    static class MyException extends Exception {
					        private static final long serialVersionUID = 1L;
					    }
					}"""
			},
			"""
				----------
				1. ERROR in TestClass.java (at line 1)\r
					class TestClass<T> {\r
					                ^
				Syntax error, type parameters are only available if source level is 1.5 or greater
				----------
				"""
		);
	}
}
