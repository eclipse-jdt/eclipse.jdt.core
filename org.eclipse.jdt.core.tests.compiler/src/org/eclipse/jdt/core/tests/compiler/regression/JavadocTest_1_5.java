/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
public class JavadocTest_1_5 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;

	public JavadocTest_1_5(String name) {
		super(name);
	}

	public static Class testClass() {
		return JavadocTest_1_5.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug331872d" };
//		TESTS_NUMBERS = new int[] { 101283 };
//		TESTS_RANGE = new int[] { 23, -1 };
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
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
		if (this.reportMissingJavadocTags != null)  {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
			if (this.reportMissingJavadocTagsOverriding != null) {
				options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, this.reportMissingJavadocTagsOverriding);
			}
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		}
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.ENABLED);
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
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
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
					"""
						 /**
						  * Valid type parameter reference
						  * @param <E> Type
						  */
						 public class X<E> {}\
						"""
			}
		);
	}
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
					"""
						 /**
						  * Valid type parameter reference
						  * @param <E> Type extends RuntimeException
						  */
						 public class X<E extends RuntimeException> {}\
						"""
			}
		);
	}
	public void test005() {
		this.runConformTest(
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
			}
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
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					public class X<E, F> {}
					                  ^
				Javadoc: Missing tag for parameter F
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param <U> Type parameter 2
					          ^
				Javadoc: U cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					* @param <V> Type parameter 3
					          ^
				Javadoc: V cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param <X> Type parameter 2
					          ^
				Javadoc: Parameter X is not declared
				----------
				2. ERROR in X.java (at line 6)
					* @param <E> Type parameter 2
					          ^
				Javadoc: E cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test010() {
		this.runConformTest(
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
			}
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
				1. ERROR in X.java (at line 4)
					* @param <E> Type parameter 2
					          ^
				Javadoc: E cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 6)
					* @param <U> Type parameter 2
					          ^
				Javadoc: Duplicate tag for parameter
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
					               ^
				Javadoc: Missing tag for parameter T
				----------
				2. ERROR in X.java (at line 4)
					public class X<T, U, V> {}
					                  ^
				Javadoc: Missing tag for parameter U
				----------
				3. ERROR in X.java (at line 4)
					public class X<T, U, V> {}
					                     ^
				Javadoc: Missing tag for parameter V
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					                  ^
				Javadoc: Missing tag for parameter U
				----------
				2. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					                     ^
				Javadoc: Missing tag for parameter V
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					               ^
				Javadoc: Missing tag for parameter T
				----------
				2. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					                     ^
				Javadoc: Missing tag for parameter V
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 6)
					public class X<T, U, V> {}
					               ^
				Javadoc: Missing tag for parameter T
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 6)
					public class X<T, U, V> {}
					                  ^
				Javadoc: Missing tag for parameter U
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 6)
					public class X<T, U, V> {}
					                     ^
				Javadoc: Missing tag for parameter V
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					               ^
				Javadoc: Missing tag for parameter T
				----------
				2. ERROR in X.java (at line 5)
					public class X<T, U, V> {}
					                  ^
				Javadoc: Missing tag for parameter U
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param <X> Type parameter 2
					          ^
				Javadoc: Parameter X is not declared
				----------
				2. ERROR in X.java (at line 6)
					* @param <E> Type parameter 2
					          ^
				Javadoc: E cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 7)
					* @param <U> Type parameter 2
					          ^
				Javadoc: Duplicate tag for parameter
				----------
				4. ERROR in X.java (at line 9)
					public class X<T, U, V> {}
					               ^
				Javadoc: Missing tag for parameter T
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				2. ERROR in X.java (at line 7)
					public class X<T, U, V> {}
					               ^
				Javadoc: Missing tag for parameter T
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param <V> Type parameter 2
					          ^
				Javadoc: V cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					* @param <U> Type parameter 1
					          ^
				Javadoc: U cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 7)
					public class X<T, , V> {}
					                  ^
				Syntax error on token ",", delete this token
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
				1. ERROR in X.java (at line 7)
					public class X<T, U, V extend Exception> {}
					                       ^^^^^^
				Syntax error on token "extend", extends expected
				----------
				2. ERROR in X.java (at line 7)
					public class X<T, U, V extend Exception> {}
					                       ^^^^^^
				extend cannot be resolved to a type
				----------
				"""
		);
	}

	/* (non-Javadoc)
	 * Test @param for generic method type parameter
	 */
	public void test023() {
		this.runConformTest(
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
			}
		);
	}
	public void test024() {
		this.runConformTest(
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
			}
		);
	}
	public void test025() {
		this.runConformTest(
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
			}
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
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 6)
					public <E, F> void foo(int val, Object obj) {}
					           ^
				Javadoc: Missing tag for parameter F
				----------
				2. ERROR in X.java (at line 6)
					public <E, F> void foo(int val, Object obj) {}
					                           ^^^
				Javadoc: Missing tag for parameter val
				----------
				3. ERROR in X.java (at line 6)
					public <E, F> void foo(int val, Object obj) {}
					                                       ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					* @param <U> Type parameter 2
					          ^
				Javadoc: U cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 6)
					* @param <V> Type parameter 3
					          ^
				Javadoc: V cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 7)
					* @param xxx int
					         ^^^
				Javadoc: Parameter xxx is not declared
				----------
				4. ERROR in X.java (at line 8)
					* @param Obj Object
					         ^^^
				Javadoc: Parameter Obj is not declared
				----------
				5. ERROR in X.java (at line 10)
					public <T> void foo(int val, Object obj) {}
					                        ^^^
				Javadoc: Missing tag for parameter val
				----------
				6. ERROR in X.java (at line 10)
					public <T> void foo(int val, Object obj) {}
					                                    ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					* @param <X> Type parameter 2
					          ^
				Javadoc: Parameter X is not declared
				----------
				2. ERROR in X.java (at line 8)
					* @param <E> Type parameter 2
					          ^
				Javadoc: E cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test030() {
		this.runConformTest(
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
			}
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
					        ^
				Javadoc: Missing tag for parameter T
				----------
				2. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					           ^
				Javadoc: Missing tag for parameter U
				----------
				3. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					              ^
				Javadoc: Missing tag for parameter V
				----------
				4. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				5. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 7)
					public <T, U, V> void foo(int val, Object obj) {}
					           ^
				Javadoc: Missing tag for parameter U
				----------
				2. ERROR in X.java (at line 7)
					public <T, U, V> void foo(int val, Object obj) {}
					              ^
				Javadoc: Missing tag for parameter V
				----------
				3. ERROR in X.java (at line 7)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^
				Javadoc: Missing tag for parameter T
				----------
				2. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 6)
					* @param <X> Type parameter 2
					          ^
				Javadoc: Parameter X is not declared
				----------
				2. ERROR in X.java (at line 8)
					* @param Object obj
					         ^^^^^^
				Javadoc: Parameter Object is not declared
				----------
				3. ERROR in X.java (at line 9)
					* @param <E> Type parameter 2
					          ^
				Javadoc: E cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 10)
					* @param <U> Type parameter 2
					          ^
				Javadoc: Duplicate tag for parameter
				----------
				5. ERROR in X.java (at line 11)
					* @param val int
					         ^^^
				Javadoc: Duplicate tag for parameter
				----------
				6. ERROR in X.java (at line 13)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^
				Javadoc: Missing tag for parameter T
				----------
				7. ERROR in X.java (at line 13)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 5)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				2. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					        ^
				Javadoc: Missing tag for parameter T
				----------
				3. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				4. ERROR in X.java (at line 8)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 5)
					* @param <V> Type parameter 2
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in X.java (at line 6)
					* @param <U> Type parameter 1
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in X.java (at line 10)
					public <T, , V> void foo(int val, Object obj) {}
					           ^
				Syntax error on token ",", delete this token
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
				1. ERROR in X.java (at line 10)
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
					         ^^^^^^
				Javadoc: Invalid param tag type parameter name
				----------
				2. ERROR in X.java (at line 4)
					* @param < Type for parameterization
					         ^^^^^^
				Javadoc: Invalid param tag type parameter name
				----------
				3. ERROR in X.java (at line 5)
					* @param <> Type
					         ^^
				Javadoc: Invalid param tag type parameter name
				----------
				4. ERROR in X.java (at line 6)
					* @param <?> Type
					         ^^^
				Javadoc: Invalid param tag type parameter name
				----------
				5. ERROR in X.java (at line 7)
					* @param <*> Type
					         ^^^
				Javadoc: Invalid param tag type parameter name
				----------
				6. ERROR in X.java (at line 9)
					public class X<E> {}
					               ^
				Javadoc: Missing tag for parameter E
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 /**
					  * Invalid type parameter reference
					  * @param <E Type
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
					* @param <E Type
					         ^^
				Javadoc: Invalid param tag type parameter name
				----------
				2. ERROR in X.java (at line 4)
					* @param E> Type
					         ^^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 5)
					* @param <<E> Type
					         ^^^^
				Javadoc: Invalid param tag type parameter name
				----------
				4. ERROR in X.java (at line 6)
					* @param <<<E> Type
					         ^^^^^
				Javadoc: Invalid param tag type parameter name
				----------
				5. ERROR in X.java (at line 7)
					* @param <E>> Type
					         ^^^^
				Javadoc: Invalid param tag type parameter name
				----------
				6. ERROR in X.java (at line 9)
					public class X<E> {}
					               ^
				Javadoc: Missing tag for parameter E
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Test fix for bug 82514: [1.5][javadoc] Problem with generics in javadoc
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82514"
	 */
	// FAIL ERRMSG
	public void _testBug82514() {
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
				1. WARNING in X.java (at line 6)
					public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)
					               ^
				The type parameter X is hiding the type X
				----------
				2. ERROR in X.java (at line 14)
					*  {@link ComparableUtils#compareTo(Object, Object)}.
					                          ^^^^^^^^^
				Javadoc: Bound mismatch: The generic method compareTo(X, X) of type ComparableUtils is not applicable for the arguments (Object, Object). The inferred type Object is not a valid substitute for the bounded parameter <X extends Comparable<? super X>>
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
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
					 *   - warning = "The method add(Number) in the type Sub is not applicable for
					 *                the arguments (T)"
					 *   - method binding = Sub.add(Number)
					 *   - parameter binding = T of A
					 *     -> Do we need to change this as T natually resolved to TypeVariable?
					 *        As compiler raises a warning, it\'s perhaps not a problem now...
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
				Javadoc: The method add(Number) in the type Sub is not applicable for the arguments (T)
				----------
				2. ERROR in Test.java (at line 3)
					* @see Sub#Sub(T)
					           ^^^^^^
				Javadoc: The constructor Sub(T) is undefined
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127f() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
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
			}
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
					 *        As compiler raises a warning, it\'s perhaps not a problem now...
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
					* @see Unrelated2#add(T)
					                  ^^^
				Javadoc: The method add(Object) in the type Unrelated2 is not applicable for the arguments (T)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
			}
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
				1. ERROR in Invalid.java (at line 2)
					* @see Test#foo(int)
					            ^^^
				Javadoc: The method foo(int, int...) in the type Test is not applicable for the arguments (int)
				----------
				2. ERROR in Invalid.java (at line 3)
					* @see Test#foo(int, int, int)
					            ^^^
				Javadoc: The method foo(int, int...) in the type Test is not applicable for the arguments (int, int, int)
				----------
				3. ERROR in Invalid.java (at line 4)
					* @see Test#foo()
					            ^^^
				Javadoc: The method foo(String...) in the type Test is not applicable for the arguments ()
				----------
				4. ERROR in Invalid.java (at line 5)
					* @see Test#foo(String)
					            ^^^
				Javadoc: The method foo(String...) in the type Test is not applicable for the arguments (String)
				----------
				5. ERROR in Invalid.java (at line 6)
					* @see Test#foo(String, String)
					            ^^^
				Javadoc: The method foo(String...) in the type Test is not applicable for the arguments (String, String)
				----------
				6. ERROR in Invalid.java (at line 7)
					* @see Test#foo(Exception)
					            ^^^
				Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception)
				----------
				7. ERROR in Invalid.java (at line 8)
					* @see Test#foo(Exception, boolean)
					            ^^^
				Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception, boolean)
				----------
				8. ERROR in Invalid.java (at line 9)
					* @see Test#foo(Exception, boolean, boolean)
					            ^^^
				Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception, boolean, boolean)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				"""
					/**
					 * Valid javadoc.
					 * @see pack.Test
					 * @see Unknown
					 * @see pack.Test#foo()
					 * @see pack.Test#unknown()
					 * @see pack.Test#field
					 * @see pack.Test#unknown
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
			"""
				----------
				1. ERROR in pack\\package-info.java (at line 4)
					* @see Unknown
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				2. ERROR in pack\\package-info.java (at line 6)
					* @see pack.Test#unknown()
					                 ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Test
				----------
				3. ERROR in pack\\package-info.java (at line 8)
					* @see pack.Test#unknown
					                 ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				4. ERROR in pack\\package-info.java (at line 9)
					* @param unexpected
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				5. ERROR in pack\\package-info.java (at line 10)
					* @throws unexpected
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				6. ERROR in pack\\package-info.java (at line 11)
					* @return unexpected\s
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 86769: [javadoc] Warn/Error for 'Missing javadoc comments' doesn't recognize private inner classes
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=86769"
	 */
	public void _testBug86769() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"E.java",
				"""
					public enum E {
						A,
						DC{
							public void foo() {}
						};
						E() {}
						public void foo() {}
						private enum Epriv {
							Apriv,
							Cpriv {
								public void foo() {}
							};
							Epriv() {}
							public void foo() {}
						}
						enum Edef {
							Adef,
							Cdef {
								public void foo() {}
							};
							Edef() {}
							public void foo() {}
						}
						protected enum Epro {
							Apro,
							Cpro {
								public void foo() {}
							};
							Epro() {}
							public void foo() {}
						}
						public enum Epub {
							Apub,
							Cpub {
								public void foo() {}
							};
							Epub() {}
							public void foo() {}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in E.java (at line 1)
					public enum E {
					            ^
				Javadoc: Missing comment for public declaration
				----------
				2. ERROR in E.java (at line 2)
					A,
					^
				Javadoc: Missing comment for public declaration
				----------
				3. ERROR in E.java (at line 3)
					DC{
					^^
				Javadoc: Missing comment for public declaration
				----------
				4. ERROR in E.java (at line 7)
					public void foo() {}
					            ^^^^^
				Javadoc: Missing comment for public declaration
				----------
				5. ERROR in E.java (at line 32)
					public enum Epub {
					            ^^^^
				Javadoc: Missing comment for public declaration
				----------
				6. ERROR in E.java (at line 33)
					Apub,
					^^^^
				Javadoc: Missing comment for public declaration
				----------
				7. ERROR in E.java (at line 34)
					Cpub {
					^^^^
				Javadoc: Missing comment for public declaration
				----------
				8. ERROR in E.java (at line 38)
					public void foo() {}
					            ^^^^^
				Javadoc: Missing comment for public declaration
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
		runConformTest(
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
			}
		);
	}
	public void testBug95521b() {
		runConformTest(
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
			}
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
		runConformTest(
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
			}
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in comment6\\Invalid.java (at line 9)
					* See also {@link Inner}\s
					                  ^^^^^
				Javadoc: Invalid member type qualification
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param <T> \s
					          ^
				Javadoc: Parameter T is not declared
				----------
				2. ERROR in X.java (at line 5)
					* @param <F>
					          ^
				Javadoc: Parameter F is not declared
				----------
				3. ERROR in X.java (at line 7)
					static class Entry<L, R> {
					                   ^
				Javadoc: Missing tag for parameter L
				----------
				4. ERROR in X.java (at line 7)
					static class Entry<L, R> {
					                      ^
				Javadoc: Missing tag for parameter R
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @see T Variable\s
					       ^
				Javadoc: Invalid reference
				----------
				2. ERROR in X.java (at line 5)
					* @see F Variable
					       ^
				Javadoc: Invalid reference
				----------
				3. ERROR in X.java (at line 7)
					static class Entry<L, R> {
					                   ^
				Javadoc: Missing tag for parameter L
				----------
				4. ERROR in X.java (at line 7)
					static class Entry<L, R> {
					                      ^
				Javadoc: Missing tag for parameter R
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @param <T> \s
					          ^
				Javadoc: Parameter T is not declared
				----------
				2. ERROR in X.java (at line 5)
					* @param <F>
					          ^
				Javadoc: Parameter F is not declared
				----------
				3. ERROR in X.java (at line 7)
					class Entry<L, R> {
					            ^
				Javadoc: Missing tag for parameter L
				----------
				4. ERROR in X.java (at line 7)
					class Entry<L, R> {
					               ^
				Javadoc: Missing tag for parameter R
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
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
				1. ERROR in X.java (at line 4)
					* @see T Variable\s
					       ^
				Javadoc: Invalid reference
				----------
				2. ERROR in X.java (at line 5)
					* @see F Variable
					       ^
				Javadoc: Invalid reference
				----------
				3. ERROR in X.java (at line 7)
					class Entry<L, R> {
					            ^
				Javadoc: Missing tag for parameter L
				----------
				4. ERROR in X.java (at line 7)
					class Entry<L, R> {
					               ^
				Javadoc: Missing tag for parameter R
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// Verify duplicate test case: bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=102735
	public void testBug101283e() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public interface Test<V, R extends Component<?>, C extends
					Test<V, R, C>> extends Control<SelectModel<V>, C>
					{
						public interface ValueRepresentationStrategy<VV, RR extends Component<?>> extends ComponentFactory<VV, RR>
						{
							/**This value must be equal to the ID of the component returned by the {@link
							ComponentFactory#createComponent(V)} method.*/
							public String getID(final VV value);
						}
					}
					class Component<T> {}
					interface Control<U, V> {}
					class SelectModel<V> {}
					interface ComponentFactory <U, V> {
						public void createComponent(V v);
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 7)
					ComponentFactory#createComponent(V)} method.*/
					                                 ^
				Javadoc: Cannot make a static reference to the non-static type variable V
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug101283f() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runConformTest(
			new String[] {
				"Test.java",
				"""
					public interface Test<V, R extends Component<?>, C extends
					Test<V, R, C>> extends Control<SelectModel<V>, C>
					{
						public interface ValueRepresentationStrategy<VV, RR extends Component<?>> extends ComponentFactory<VV, RR>
						{
							/**This value must be equal to the ID of the component returned by the {@link
							ComponentFactory#createComponent(Object)} method.*/
							public String getID(final VV value);
						}
					}
					class Component<T> {}
					interface Control<U, V> {}
					class SelectModel<V> {}
					interface ComponentFactory <U, V> {
						public void createComponent(V v);
					}
					"""
			}
		);
	}
	// Verify that ProblemReasons.InheritedNameHidesEnclosingName is not reported as Javadoc error
	public void testBug101283g() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runConformTest(
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
			}
		);
	}

	/**
	 * Bug 112346: [javadoc] {&#064;inheritedDoc} should be inactive for non-overridden method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=112346"
	 */
	public void testBug112346() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
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
			}
		);
	}

	/**
	 * Bug 119857: [javadoc] Some inner class references should be flagged as unresolved
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=119857"
	 */
	public void testBug119857() {
		runConformTest(
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
			}
		);
	}
	public void testBug119857_Fields() {
		runConformTest(
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
			}
		);
	}
	public void testBug119857_Methods() {
		runConformTest(
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
			}
		);
	}
	public void testBug119857_Types() {
		runConformTest(
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
			}
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
				Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug119857_Private02() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
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
				Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 132430: [1.5][javadoc] Unwanted missing tag warning for overridden method with parameter containing type variable
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=132430"
	 */
	public void testBug132430() {
		runConformTest(
			new String[] {
				"A.java",
				"""
					public class A<E> {
					    /**
					     * @param object
					     */
					    public void aMethod(E object) {}
					}""",
				"B.java",
				"""
					public class B<E> extends A<E> {
						/**
						 * @see A#aMethod(java.lang.Object)
						 */
						@Override
						public void aMethod(E object) {
							super.aMethod(object);
						}
					}
					"""
			}
		);
	}
	public void testBug132430b() {
		runConformTest(
			new String[] {
				"A.java",
				"""
					public class A<E> {
					    /**
					     * @param object
					     */
					    public void aMethod(E object) {}
					}""",
				"B.java",
				"""
					public class B<E> extends A<E> {
						/**
						 * @see A#aMethod(java.lang.Object)
						 */
						public void aMethod(E object) {
							super.aMethod(object);
						}
					}
					"""
			}
		);
	}
	public void testBug132430c() {
		runConformTest(
			new String[] {
				"A.java",
				"""
					public class A<E> {
					    /**
					     * @param object
					     */
					    public void aMethod(E object) {}
					}""",
				"B.java",
				"""
					public class B<E> extends A<E> {
						/**
						 * Empty comment
						 */
						@Override
						public void aMethod(E object) {
							super.aMethod(object);
						}
					}
					"""
			}
		);
	}

	/**
	 * Bug 145007: [1.5][javadoc] Generics + Inner Class -> Javadoc "missing @throws" warning
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=145007"
	 */
	public void testBug145007() {
		runConformTest(
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
			}
		);
	}


	/**
	 * Bug 87500: [1.5][javadoc][options] Add a 'Consider enum values' option to warn/error on 'Missing javadoc comments'.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=87500"
	 */
	public void testBug87500a() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(
			new String[] {
				"A.java",
				"""
					enum A {
						clubs,
						diamonds,
						hearts,
						spades
					}
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 1)
					enum A {
					     ^
				Javadoc: Missing comment for default declaration
				----------
				2. ERROR in A.java (at line 2)
					clubs,
					^^^^^
				Javadoc: Missing comment for default declaration
				----------
				3. ERROR in A.java (at line 3)
					diamonds,
					^^^^^^^^
				Javadoc: Missing comment for default declaration
				----------
				4. ERROR in A.java (at line 4)
					hearts,
					^^^^^^
				Javadoc: Missing comment for default declaration
				----------
				5. ERROR in A.java (at line 5)
					spades
					^^^^^^
				Javadoc: Missing comment for default declaration
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug87500b() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"A.java",
				"""
					enum A {
						clubs,
						diamonds,
						hearts,
						spades
					}
					"""
			});
	}

	/**
	 * Bug 204749  [1.5][javadoc] NPE in JavadocQualifiedTypeReference
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=204749"
	 */
	public void testBug204749a() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					    /** @see T.R */
					    void foo() {}
					}"""
				},
			"""
				----------
				1. ERROR in X.java (at line 2)
					/** @see T.R */
					         ^^^
				Javadoc: Invalid reference
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug204749b() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		this.reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					    /** @see T.R */
					    void foo() {}
					}"""
			}
		);
	}

	/**
	 * Bug 209936  Missing code implementation in the compiler on inner classes
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209936"
	 */
	public void testBug209936a() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public abstract class X extends Y {
						protected class A extends Member {
							/**
							 * @see Member#foo(Object, Object)
							 */
							public void foo(Object source, Object data) {}
						}
					}""",
				"p/Y.java",
				"""
					package p;
					import p1.Z;
					public abstract class Y extends Z<Object> {
					}""",
				"p1/Z.java",
				"""
					package p1;
					public abstract class Z<T> {
						protected class Member {
							protected void foo(Object source, Object data) {
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 5)
					* @see Member#foo(Object, Object)
					       ^^^^^^
				Javadoc: Invalid member type qualification
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936b() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public abstract class X extends Y {
						protected class A extends Member {
							/**
							 * @see Member#foo(Object, Object)
							 */
							public void foo(Object source, Object data) {}
						}
					}""",
				"p/Y.java",
				"""
					package p;
					
					import p1.Z;
					public abstract class Y extends Z<Object> {}""",
				"p1/Z.java",
				"""
					package p1;
					public abstract class Z<T> {
						protected class Member {
							protected void foo(Object source, Object data) {}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 5)
					* @see Member#foo(Object, Object)
					       ^^^^^^
				Javadoc: Invalid member type qualification
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberImplicitReference() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2<T> {
								public class A3<U> {
									public class A4<V> {
										public void foo(V v) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2<T> extends A2<T> {
								public class X3<U> extends A3<U> {
									public class X4<V> extends A4<V> {
										/**
								 		 * @see #foo(Object)
								 		 * @see #foo(V)
										 */
										public void foo(V v) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 10)
					* @see #foo(V)
					        ^^^
				Javadoc: The method foo(Object) in the type X.X1.X2.X3.X4 is not applicable for the arguments (V)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberSingleReference() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2<T> {
								public class A3<U> {
									public class A4<V> {
										public void foo(V v) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2<T> extends A2<T> {
								public class X3<U> extends A3<U> {
									public class X4<V> extends A4<V> {
										/**
								 		 * @see A4#foo(V)
								 		 * @see A4#foo(Object)
										 */
										public void myFoo(V v) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see A4#foo(V)
					          ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (V)
				----------
				2. ERROR in p2\\X.java (at line 10)
					* @see A4#foo(Object)
					       ^^
				Javadoc: Invalid member type qualification
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberQualifiedSingleReference() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2<T> {
								public class A3<U> {
									public class A4<V> {
										public void foo(V v) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2<T> extends A2<T> {
								public class X3<U> extends A3<U> {
									public class X4<V> extends A4<V> {
										/**
								 		 * @see A3.A4#foo(V)
								 		 * @see p1.A.A1.A2.A3.A4#foo(Object)
										 */
										public void foo(V v) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see A3.A4#foo(V)
					             ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (V)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberFullyQualifiedSingleReference() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2<T> {
								public class A3<U> {
									public class A4<V> {
										public void foo(V v) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2<T> extends A2<T> {
								public class X3<U> extends A3<U> {
									public class X4<V> extends A4<V> {
										/**
								 		 * @see p1.A.A1.A2.A3.A4#foo(V)
								 		 * @see p1.A.A1.A2.A3.A4#foo(Object)
										 */
										public void foo(V v) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see p1.A.A1.A2.A3.A4#foo(V)
					                        ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (V)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberImplicitReference() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2<T> {
								public class A3<U> {
									public class A4 {
										public void foo(U u) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2<T> extends A2<T> {
								public class X3<U> extends A3<U> {
									public class X4 extends A4 {
										/**
								 		 * @see #foo(Object)
								 		 * @see #foo(U u)
										 */
										public void foo(U u) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 10)\r
					* @see #foo(U u)\r
					        ^^^
				Javadoc: The method foo(Object) in the type X.X1.X2.X3.X4 is not applicable for the arguments (U)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference1(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2<T> {
								public class A3 {
									public class A4 {
										public void foo(T t) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2<T> extends A2<T> {
								public class X3 extends A3 {
									public class X4 extends A4 {
										/**
								 		 * @see A4#foo(Object)
								 		 * @see A4#foo(T)
										 */
										public void foo(T t) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see A4#foo(Object)
					       ^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in p2\\X.java (at line 10)
					* @see A4#foo(T)
					          ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (T)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference2(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A<R> {
						public class A1<S> {
							public class A2 {
								public class A3 {
									public class A4 {
										public void foo(S s) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2 extends A2 {
								public class X3 extends A3 {
									public class X4 extends A4 {
										/**
								 		 * @see A4#foo(Object)
								 		 * @see A4#foo(S)
										 */
										public void foo(S s) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see A4#foo(Object)
					       ^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in p2\\X.java (at line 10)
					* @see A4#foo(S)
					          ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (S)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference3(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A {
						public class A1 {
							public class A2<T> {
								public class A3 {
									public class A4 {
										public void foo(T t) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X extends A {
						public class X1 extends A1 {
							public class X2<T> extends A2<T> {
								public class X3 extends A3 {
									public class X4 extends A4 {
										/**
								 		 * @see A4#foo(Object)
								 		 * @see A4#foo(T)
										 */
										public void foo(T t) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see A4#foo(Object)
					       ^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in p2\\X.java (at line 10)
					* @see A4#foo(T)
					          ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (T)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference4(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					public class A {
						public class A1 {
							public class A2<T> {
								public class A3 {
									public class A4 {
										public void foo(T t) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X extends A {
						public class X1 extends A1 {
							public class X2<T> extends A2<T> {
								public class X3 extends A3 {
									public class X4 extends A4 {
										/**
								 		 * @see A4#foo(Object)
								 		 * @see A4#foo(T)
										 */
										public void foo(T t) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 9)
					* @see A4#foo(Object)
					       ^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in p2\\X.java (at line 10)
					* @see A4#foo(T)
					          ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (T)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberQualifiedSingleReference1() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runConformTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					
					public class A<R> {
						public class A1<S> {
							public class A2 {
								public class A3 {
									public class A4 {
										public void foo(S s) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2 extends A2 {
								public class X3 extends A3 {
									public class X4 extends A4 {
										/**
								 		 * @see p1.A.A1.A2.A3.A4#foo(Object)
										 */
										public void foo(S s) {}
									}
								}
							}
						}
					}"""
			}
		);
	}

	public void testBug209936_MemberQualifiedSingleReference2() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runConformTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					
					public class A<R> {
						public class A1<S> {
							public class A2 {
								public class A3 {
									public class A4 {
										public class A5 {
											public class A6 {
												public void foo(S s) {}
											}
										}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1<S> extends A1<S> {
							public class X2 extends A2 {
								public class X3 extends A3 {
									public class X4 extends A4 {
										public class X5 extends A5 {
											public class X6 extends A6 {
												/**
								 				 * @see p1.A.A1.A2.A3.A4.A5.A6#foo(Object)
												 */
												public void foo(S s) {}
											}
										}
									}
								}
							}
						}
					}"""
			}
		);
	}

	public void testBug209936_MemberFullyQualifiedSingleReference() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					
					public class A<R> {
						public class A1 {
							public class A2 {
								public class A3 {
									public class A4 {
										public void foo(R r) {}
									}
								}
							}
						}
					}""",
				"p2/X.java",
				"""
					package p2;
					import p1.A;
					public class X<R> extends A<R> {
						public class X1 extends A1 {
							public class X2 extends A2 {
								public class X3 extends A3 {
									public class X4 extends A4 {
										/**
								 		 * @see p1.A.A1.A2.A3.A4#foo(Object)
								 		 * @see p1.A.A1.A2.A3.A4#foo(R)
										 */
										public void foo(R r) {}
									}
								}
							}
						}
					}"""
			},
			"""
				----------
				1. ERROR in p2\\X.java (at line 10)\r
					* @see p1.A.A1.A2.A3.A4#foo(R)\r
					                        ^^^
				Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (R)
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, verify that we complain about @inheritDoc
	// being used in package level javadoc.
	public void testBug247037a() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
				"""
					/**
					 * {@inheritDoc}
					 * @since {@inheritDoc}
					 * @blah {@inheritDoc}
					 */
					package pack;
					"""
			},
			"""
				----------
				1. ERROR in pack\\package-info.java (at line 2)
					* {@inheritDoc}
					    ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in pack\\package-info.java (at line 3)
					* @since {@inheritDoc}
					           ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in pack\\package-info.java (at line 4)
					* @blah {@inheritDoc}
					          ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, verify that we complain about @inheritDoc
	// being used in package level javadoc (variation)
	public void testBug247037b() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
				"""
					/**
					 * @return {@inheritDoc}
					 * @param blah {@inheritDoc}
					 */
					package pack;
					"""
			},
			"""
				----------
				1. ERROR in pack\\package-info.java (at line 2)
					* @return {@inheritDoc}
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in pack\\package-info.java (at line 2)
					* @return {@inheritDoc}
					            ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in pack\\package-info.java (at line 3)
					* @param blah {@inheritDoc}
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in pack\\package-info.java (at line 3)
					* @param blah {@inheritDoc}
					                ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	}
	/**
	 * bug 286918:[javadoc] Compiler should warn when @see and @link tag references in package-info.java don't have fully qualified names
	 * test that in a package-info.java file
	 * 				1. References to valid packages are ACCEPTED without any warnings or errors
	 * 				2. References to valid Java elements (including the ones in the same package) without qualified names are REPORTED as errors
	 * 				3. References to valid Java elements with qualified names are ACCEPTED
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286918"
	 */
	public void testBug284333() {
		runNegativeTest(new String[]{
				"goo/bar/package-info.java",
				"""
					/**
					*/
					package goo.bar;
					""",
				"foo/bar/ClassInSamePackage.java",
				"""
					package foo.bar;
					public class ClassInSamePackage {
						public static int SOME_FIELD;\s
					}
					""",
				"foo/bar/goo/ClassInSubPackage.java",
				"""
					package foo.bar.goo;
					public class ClassInSubPackage {
						public static void foo() {\s
					   }
					}
					""",
				"foo/ClassInEnclosingPackage.java",
				"""
					package foo;
					public class ClassInEnclosingPackage {
						public static int SOME_FIELD;\s
					}
					""",
				"foo/bar/package-info.java",
				"""
					/**
					 * @see ClassInSamePackage#SOME_FIELD
					 * @see foo.bar.ClassInSamePackage#SOME_FIELD
					 * @see ClassInSamePackage#SOME_FIELD
					 * @see ClassInSubPackage#foo
					 * @see foo.bar.goo.ClassInSubPackage#foo
					 * @see ClassInSubPackage#foo
					 * @see ClassInEnclosingPackage
					 * @see foo.ClassInEnclosingPackage
					 * @see ClassInEnclosingPackage
					 * @see foo.bar
					 * @see goo.bar
					 * @see foo.bar.goo
					 */
					package foo.bar;
					"""
		},
		"""
			----------
			1. ERROR in foo\\bar\\package-info.java (at line 2)
				* @see ClassInSamePackage#SOME_FIELD
				       ^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid reference
			----------
			2. ERROR in foo\\bar\\package-info.java (at line 4)
				* @see ClassInSamePackage#SOME_FIELD
				       ^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid reference
			----------
			3. ERROR in foo\\bar\\package-info.java (at line 5)
				* @see ClassInSubPackage#foo
				       ^^^^^^^^^^^^^^^^^
			Javadoc: ClassInSubPackage cannot be resolved to a type
			----------
			4. ERROR in foo\\bar\\package-info.java (at line 7)
				* @see ClassInSubPackage#foo
				       ^^^^^^^^^^^^^^^^^
			Javadoc: ClassInSubPackage cannot be resolved to a type
			----------
			5. ERROR in foo\\bar\\package-info.java (at line 8)
				* @see ClassInEnclosingPackage
				       ^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: ClassInEnclosingPackage cannot be resolved to a type
			----------
			6. ERROR in foo\\bar\\package-info.java (at line 10)
				* @see ClassInEnclosingPackage
				       ^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: ClassInEnclosingPackage cannot be resolved to a type
			----------
			""");
	}

	/**
	 * Additional tests for "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286918"
	 * test that in a non package-info.java file
	 * 				2. References without qualified names to valid Java elements in the same package are ACCEPTED
	 * 	 			2. References without qualified names to valid Java elements in other packages are REPORTED
	 * 				3. References with qualified names to valid Java elements are accepted
	 */
	public void testBug284333a() {
		runNegativeTest(new String[]{
				"goo/bar/package-info.java",
				"""
					/**
					*/
					package goo.bar;
					""",
				"foo/bar/ClassInSamePackage.java",
				"""
					package foo.bar;
					public class ClassInSamePackage {
						public static int SOME_FIELD;\s
					}
					""",
				"foo/bar/goo/ClassInSubPackage.java",
				"""
					package foo.bar.goo;
					public class ClassInSubPackage {
						public static void foo() {\s
					   }
					}
					""",
				"foo/ClassInEnclosingPackage.java",
				"""
					package foo;
					public class ClassInEnclosingPackage {
						public static int SOME_FIELD;\s
					}
					""",
				"foo/bar/NotAPackageInfo.java",
				"""
					package foo.bar;
					/**
					 * @see ClassInSamePackage#SOME_FIELD
					 * @see foo.bar.ClassInSamePackage#SOME_FIELD
					 * @see ClassInSamePackage#SOME_FIELD
					 */
					 public class NotAPackageInfo {
					/**
					 * @see ClassInSubPackage#foo
					 * @see foo.bar.goo.ClassInSubPackage#foo
					 * @see ClassInSubPackage#foo
					 */
						public static int SOME_FIELD = 0;
					/**
					 * @see ClassInEnclosingPackage
					 * @see foo.ClassInEnclosingPackage
					 * @see ClassInEnclosingPackage
					 */
					 	public static void foo() {
						}
						\
					 }
					"""
		},
		"""
			----------
			1. ERROR in foo\\bar\\NotAPackageInfo.java (at line 9)
				* @see ClassInSubPackage#foo
				       ^^^^^^^^^^^^^^^^^
			Javadoc: ClassInSubPackage cannot be resolved to a type
			----------
			2. ERROR in foo\\bar\\NotAPackageInfo.java (at line 11)
				* @see ClassInSubPackage#foo
				       ^^^^^^^^^^^^^^^^^
			Javadoc: ClassInSubPackage cannot be resolved to a type
			----------
			3. ERROR in foo\\bar\\NotAPackageInfo.java (at line 15)
				* @see ClassInEnclosingPackage
				       ^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: ClassInEnclosingPackage cannot be resolved to a type
			----------
			4. ERROR in foo\\bar\\NotAPackageInfo.java (at line 17)
				* @see ClassInEnclosingPackage
				       ^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: ClassInEnclosingPackage cannot be resolved to a type
			----------
			""");
	}
	/**
	 * Additional tests for "https://bugs.eclipse.org/bugs/show_bug.cgi?id=284333"
	 * test that in a non package-info.java file
	 * 	 			2. References without qualified names to imported Java elements in other packages are ACCEPTED
	 * 				3. References with qualified names to valid Java elements are ACCEPTED
	 */
	public void testBug284333b() {
		runConformTest(new String[] {
				"goo/bar/package-info.java",
				"""
					/**
					*/
					package goo.bar;
					""",
				"foo/bar/ClassInSamePackage.java",
				"""
					package foo.bar;
					public class ClassInSamePackage {
						public static int SOME_FIELD;\s
					}
					""",
				"foo/bar/goo/ClassInSubPackage.java",
				"""
					package foo.bar.goo;
					public class ClassInSubPackage {
						public static void foo() {\s
					   }
					}
					""",
				"foo/ClassInEnclosingPackage.java",
				"""
					package foo;
					public class ClassInEnclosingPackage {
						public static int SOME_FIELD;\s
					}
					""",
				"foo/bar/NotAPackageInfo.java",
				"""
					package foo.bar;
					import foo.*;
					import foo.bar.goo.*;
					/**
					 * @see ClassInSamePackage#SOME_FIELD
					 * @see foo.bar.ClassInSamePackage#SOME_FIELD
					 * @see ClassInSamePackage#SOME_FIELD
					 * @see goo.bar
					 */
					 public class NotAPackageInfo {
					/**
					 * @see ClassInSubPackage#foo
					 * @see foo.bar.goo.ClassInSubPackage#foo
					 * @see ClassInSubPackage#foo
					 * @see goo.bar
					 */
						public static int SOME_FIELD = 0;
					/**
					 * @see ClassInEnclosingPackage
					 * @see foo.ClassInEnclosingPackage
					 * @see ClassInEnclosingPackage
					 * @see goo.bar
					 */
					 	public static void foo() {
						}
						\
					 }
					"""
		});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322581
	// To test the javadoc option CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters
	public void testBug322581a() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"""
					 public class X {
						/**
						 * javadoc
						 */
						public <T, U, V> void foo(int val, Object obj) {}
					}"""
			},
			null,
			options,
			"""
				----------
				1. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					                              ^^^
				Javadoc: Missing tag for parameter val
				----------
				2. ERROR in X.java (at line 5)
					public <T, U, V> void foo(int val, Object obj) {}
					                                          ^^^
				Javadoc: Missing tag for parameter obj
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322581
	// To test the javadoc option CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters
	public void testBug322581b() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"ListCallable.java",
				"""
					 import java.util.Collections;
					 import java.util.List;
					 import java.util.concurrent.Callable;
					/**
					 * Callable that returns a list.
					 */
					public abstract class ListCallable<V> implements Callable<List<V>> { // good warning
						public abstract List<V> call() throws Exception;
					    /**
						 * Returns a {@link ListCallable} that wraps the result from calling <code>callable</code>.
					    * @param callable the {@link Callable} to wrap
						 * @return the wrapper
					    */
						public static <T> ListCallable<T> from(final Callable<T> callable) { // don't warn
							return new ListCallable<T>() {
								@Override
								public List<T> call() throws Exception {
									return Collections.singletonList(callable.call());
								}
							};
						}
					}"""
			},
			null,
			options,
			"""
				----------
				1. ERROR in ListCallable.java (at line 7)
					public abstract class ListCallable<V> implements Callable<List<V>> { // good warning
					                                   ^
				Javadoc: Missing tag for parameter V
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"""
					/**
					 * @param <p> the given type parameter
					 */
					public class X<p> {
						/**
						 * @param o the given object
						 * @see #foo(p.O[])
						 */
						public void foo(Object o) {
						}
					}"""
			},
			null,
			options,
			"""
				----------
				1. ERROR in X.java (at line 7)
					* @see #foo(p.O[])
					            ^^^
				Illegal qualified access from the type parameter p
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872b() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"""
					/**
					 * @param <p> the given type parameter
					 */
					public class X<p> {
						/**
						 * @param o the given object
						 * @see #foo(O[])
						 */
						public void foo(Object o) {
						}
					}"""
			},
			null,
			options,
			"""
				----------
				1. ERROR in X.java (at line 7)
					* @see #foo(O[])
					            ^
				Javadoc: O[] cannot be resolved to a type
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872c() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"""
					/**
					 * @param <p> the given type parameter
					 */
					public class X<p> {
						/**
						 * @param o the given object
						 * @see #foo(test.O[])
						 */
						public void foo(Object o) {
						}
					}"""
			},
			null,
			options,
			"""
				----------
				1. ERROR in X.java (at line 7)
					* @see #foo(test.O[])
					            ^^^^^^
				Javadoc: test[] cannot be resolved to a type
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872d() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"""
					/**
					 * @param <p> the given type parameter
					 */
					public class X<p> {
						/**
						 * @param o the given object
						 * @see #foo(test.O)
						 */
						public void foo(Object o) {
						}
					}"""
			},
			null,
			options,
			"""
				----------
				1. ERROR in X.java (at line 7)
					* @see #foo(test.O)
					            ^^^^^^
				Javadoc: test cannot be resolved to a type
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}
