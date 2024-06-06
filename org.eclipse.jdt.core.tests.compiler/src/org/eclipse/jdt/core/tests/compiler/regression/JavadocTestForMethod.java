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
public class JavadocTestForMethod extends JavadocTest {
	public JavadocTestForMethod(String name) {
		super(name);
	}
	public static Class javadocTestClass() {
		return JavadocTestForMethod.class;
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "Bug51529a", "Bug51529b" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 117, 124, 132, 137 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 21, 50 };
//		TESTS_RANGE = new int[] { -1, 50 }; // run all tests with a number less or equals to 50
//		TESTS_RANGE = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		return options;
	}

	/* (non-Javadoc)
	 * Test @deprecated tag
	 */
	public void test001() {
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						{
							new Z().foo();
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   *\s
					   * **   ** ** ** @deprecated */
						public void foo() {\s
						}
					}
					""",
				},
			"""
				----------
				1. WARNING in X.java (at line 4)
					new Z().foo();
					        ^^^^^
				The method foo() from the type Z is deprecated
				----------
				""",
				null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}

	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/** @deprecated */
						int x;
						/**
						 * @see #x
						 */
						void foo() {
						}
					}
					""",
				"Y.java",
				"""
					/** @deprecated */
					public class Y {
						int y;
						/**
						 * @see X#x
						 * @see Y
						 * @see Y#y
						 */
						void foo() {
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
						int z;
						/**
						 * @see X#x
						 * @see Y
						 * @see Y#y
						 */
						void foo() {
						}
					}
					""" },
		"""
			----------
			1. ERROR in Z.java (at line 4)
				* @see X#x
				         ^
			Javadoc: The field X.x is deprecated
			----------
			2. ERROR in Z.java (at line 5)
				* @see Y
				       ^
			Javadoc: The type Y is deprecated
			----------
			3. ERROR in Z.java (at line 6)
				* @see Y#y
				       ^
			Javadoc: The type Y is deprecated
			----------
			4. ERROR in Z.java (at line 6)
				* @see Y#y
				         ^
			Javadoc: The field Y.y is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
			);
	}

	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						{
							new Z().foo(2);
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   * Valid tags with deprecation at end
					   *
					   * @param x Valid param tag
					   * @return Valid return tag
					   * @throws NullPointerException Valid throws tag
					   * @exception IllegalArgumentException Valid throws tag
					   * @see X Valid see tag
					   * @deprecated
					   */
						public String foo(int x) {\s
							return "";
						}
					}
					""",
				},
		"""
			----------
			1. WARNING in X.java (at line 4)
				new Z().foo(2);
				        ^^^^^^
			The method foo(int) from the type Z is deprecated
			----------
			"""
				);
	}

	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						{
							new Z().foo(2);
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   * Invalid javadoc tags with valid deprecation at end
					   *
					   * @param
					   * @return String
					   * @throws Unknown
					   * @see "Invalid
					   * @see Unknown
					   * @param x
					   * @deprecated
					   */
						public String foo(int x) {\s
							return "";
						}
					}
					""",
				},
		"""
			----------
			1. WARNING in X.java (at line 4)
				new Z().foo(2);
				        ^^^^^^
			The method foo(int) from the type Z is deprecated
			----------
			----------
			1. ERROR in Z.java (at line 5)
				* @param
				   ^^^^^
			Javadoc: Missing parameter name
			----------
			2. ERROR in Z.java (at line 7)
				* @throws Unknown
				          ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			3. ERROR in Z.java (at line 8)
				* @see "Invalid
				       ^^^^^^^^
			Javadoc: Invalid reference
			----------
			4. ERROR in Z.java (at line 9)
				* @see Unknown
				       ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			5. ERROR in Z.java (at line 10)
				* @param x
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			6. ERROR in Z.java (at line 13)
				public String foo(int x) {\s
				                      ^
			Javadoc: Missing tag for parameter x
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
				);
	}

	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						{
							new Z().foo(2);
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   * Valid tags with deprecation at beginning
					   *
					   * @deprecated
					   * @param x Valid param tag
					   * @return Valid return tag
					   * @exception IllegalArgumentException Valid throws tag
					   * @throws NullPointerException Valid throws tag
					   * @see X Valid see tag
					   */
						public String foo(int x) {\s
							return "";
						}
					}
					""",
				},
		"""
			----------
			1. WARNING in X.java (at line 4)
				new Z().foo(2);
				        ^^^^^^
			The method foo(int) from the type Z is deprecated
			----------
			"""
				);
	}

	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						{
							new Z().foo(2);
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   * Invalid javadoc tags with valid deprecation at beginning
					   *
					   * @deprecated
					   * @param
					   * @return String
					   * @throws Unknown
					   * @exception IllegalArgumentException Valid throws tag
					   * @see "Invalid
					   * @see Unknown
					   * @param x
					   */
						public String foo(int x) {\s
							return "";
						}
					}
					""",
				},
		"""
			----------
			1. WARNING in X.java (at line 4)
				new Z().foo(2);
				        ^^^^^^
			The method foo(int) from the type Z is deprecated
			----------
			----------
			1. ERROR in Z.java (at line 6)
				* @param
				   ^^^^^
			Javadoc: Missing parameter name
			----------
			2. ERROR in Z.java (at line 8)
				* @throws Unknown
				          ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			3. ERROR in Z.java (at line 10)
				* @see "Invalid
				       ^^^^^^^^
			Javadoc: Invalid reference
			----------
			4. ERROR in Z.java (at line 11)
				* @see Unknown
				       ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			5. ERROR in Z.java (at line 12)
				* @param x
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			6. ERROR in Z.java (at line 14)
				public String foo(int x) {\s
				                      ^
			Javadoc: Missing tag for parameter x
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
					public class X {
					\t
						{
							new Z().foo(2);
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   * Valid tags with deprecation in the middle
					   *
					   * @param x Valid param tag
					   * @return Valid return tag
					   * @deprecated
					   * @exception IllegalArgumentException Valid throws tag
					   * @throws NullPointerException Valid throws tag
					   * @see X Valid see tag
					   */
						public String foo(int x) {\s
							return "";
						}
					}
					""",
				},
		"""
			----------
			1. WARNING in X.java (at line 4)
				new Z().foo(2);
				        ^^^^^^
			The method foo(int) from the type Z is deprecated
			----------
			"""
				);
	}

	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						{
							new Z().foo(2);
						}
					}
					""",
				"Z.java",
				"""
					public class Z {
					  /**\s
					   * Invalid javadoc tags with valid deprecation in the middle
					   *
					   * @param
					   * @return String
					   * @throws Unknown
					   * @exception IllegalArgumentException Valid throws tag
					   * @see "Invalid
					   * @deprecated
					   * @see Unknown
					   */
						public String foo(int x) {\s
							return "";
						}
					}
					""",
				},
		"""
			----------
			1. WARNING in X.java (at line 4)
				new Z().foo(2);
				        ^^^^^^
			The method foo(int) from the type Z is deprecated
			----------
			----------
			1. ERROR in Z.java (at line 5)
				* @param
				   ^^^^^
			Javadoc: Missing parameter name
			----------
			2. ERROR in Z.java (at line 7)
				* @throws Unknown
				          ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			3. ERROR in Z.java (at line 9)
				* @see "Invalid
				       ^^^^^^^^
			Javadoc: Invalid reference
			----------
			4. ERROR in Z.java (at line 11)
				* @see Unknown
				       ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			5. ERROR in Z.java (at line 13)
				public String foo(int x) {\s
				                      ^
			Javadoc: Missing tag for parameter x
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
				);
	}

	/* (non-Javadoc)
	 * Test @param tag
	 */
	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid @param: no tags, no args
						 * Valid @throws/@exception: no tags, no thrown exception
						 */
						public void p_foo() {
						}
					}
					""" });
	}

	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void p_foo() {
						}
					}
					""" });
	}

	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param declaration: no arguments, 2 declared tags
						 * @param x
						 * 			Invalid param: not an argument on 2 lines
						 * @param x Invalid param: not an argument
						 */
						public void p_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param x
					         ^
				Javadoc: Parameter x is not declared
				----------
				2. ERROR in X.java (at line 6)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						/**
						 * Valid @param declaration: 3 arguments, 3 tags in right order
						 * @param a Valid param
						 * @param b Valid param\s
						 * @param c Valid param
						 */
						public void p_foo(int a, int b, int c) {
						}
					}
					""" });
	}

	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param declaration: 3 arguments, 3 correct tags in right order + 2 additional
						 * @param a Valid param
						 * @param x Invalid param: not an argument
						 * @param b Valid param\s
						 * @param x Invalid param: not an argument
						 * @param c Valid param
						 */
						public void p_foo(char a, char b, char c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				2. ERROR in X.java (at line 7)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid @param declaration: 3 arguments, 3 tags in wrong order
						 * @param c Valid param, not well placed
						 * @param b Valid param, not well placed\s
						 * @param a Valid param, not well placed
						 */
						public void p_foo(long a, long b, long c) {
						}
					}
					""" });
	}

	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param declaration: 3 arguments, 3 correct tags in wrong order + 1 duplicate tag + 1 additional
						 * @param c Valid param, not well placed
						 * @param a Valid param, not well placed
						 * @param b Valid param, not well placed\s
						 * @param a Invalid param: duplicated
						 * @param x Invalid param: not an argument
						 */
						public void p_foo(float a, float b, float c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 7)
					* @param a Invalid param: duplicated
					         ^
				Javadoc: Duplicate tag for parameter
				----------
				2. ERROR in X.java (at line 8)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: all arguments are not documented
						 */
						public void p_foo(double a, double b, double c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					public void p_foo(double a, double b, double c) {
					                         ^
				Javadoc: Missing tag for parameter a
				----------
				2. ERROR in X.java (at line 5)
					public void p_foo(double a, double b, double c) {
					                                   ^
				Javadoc: Missing tag for parameter b
				----------
				3. ERROR in X.java (at line 5)
					public void p_foo(double a, double b, double c) {
					                                             ^
				Javadoc: Missing tag for parameter c
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: b and c arguments are not documented
						 * @param a Valid param
						 */
						public void p_foo(int a, char b, long c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					public void p_foo(int a, char b, long c) {
					                              ^
				Javadoc: Missing tag for parameter b
				----------
				2. ERROR in X.java (at line 6)
					public void p_foo(int a, char b, long c) {
					                                      ^
				Javadoc: Missing tag for parameter c
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: a and c arguments are not documented
						 * @param b Valid param
						 */
						public void p_foo(int a, char b, long c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					public void p_foo(int a, char b, long c) {
					                      ^
				Javadoc: Missing tag for parameter a
				----------
				2. ERROR in X.java (at line 6)
					public void p_foo(int a, char b, long c) {
					                                      ^
				Javadoc: Missing tag for parameter c
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test023() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: a and b arguments are not documented
						 * @param c Valid param
						 */
						public void p_foo(int a, char b, long c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					public void p_foo(int a, char b, long c) {
					                      ^
				Javadoc: Missing tag for parameter a
				----------
				2. ERROR in X.java (at line 6)
					public void p_foo(int a, char b, long c) {
					                              ^
				Javadoc: Missing tag for parameter b
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: c argument is not documented
						 * @param a Valid param
						 * @param b Valid param
						 */
						public void p_foo(int a, char b, long c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 7)
					public void p_foo(int a, char b, long c) {
					                                      ^
				Javadoc: Missing tag for parameter c
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test025() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: a argument is not documented + b and c are not well placed
						 * @param c Valid param
						 * @param b Valid param
						 */
						public void p_foo(int a, char b, long c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 7)
					public void p_foo(int a, char b, long c) {
					                      ^
				Javadoc: Missing tag for parameter a
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: b argument is not documented + a and c are not well placed
						 * @param c Valid param
						 * @param a Valid param
						 */
						public void p_foo(int a, char b, long c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 7)
					public void p_foo(int a, char b, long c) {
					                              ^
				Javadoc: Missing tag for parameter b
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test030() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: mix of all possible errors (missing a, not argument tag and duplicated)
						 * @param c Valid param
						 * @param x Invalid param: not an argument
						 * @param b Valid param
						 * @param c Invalid param: duplicated
						 */
						public void p_foo(double a, long b, int c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @param x Invalid param: not an argument
					         ^
				Javadoc: Parameter x is not declared
				----------
				2. ERROR in X.java (at line 7)
					* @param c Invalid param: duplicated
					         ^
				Javadoc: Duplicate tag for parameter
				----------
				3. ERROR in X.java (at line 9)
					public void p_foo(double a, long b, int c) {
					                         ^
				Javadoc: Missing tag for parameter a
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: missing parameter name
						 * @param
						 */
						public void p_foo(String a) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				2. ERROR in X.java (at line 6)
					public void p_foo(String a) {
					                         ^
				Javadoc: Missing tag for parameter a
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: missing parameter name + valid param\s
						 * @param
						 * @param x
						 */
						public void p_foo(int x) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: missing parameter names + valid params\s
						 * @param h
						 * @param
						 * @param h
						 * @param
						 */
						public void p_foo(java.util.Hashtable h, float f) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				2. ERROR in X.java (at line 6)
					* @param h
					         ^
				Javadoc: Duplicate tag for parameter
				----------
				3. ERROR in X.java (at line 7)
					* @param
					   ^^^^^
				Javadoc: Missing parameter name
				----------
				4. ERROR in X.java (at line 9)
					public void p_foo(java.util.Hashtable h, float f) {
					                                               ^
				Javadoc: Missing tag for parameter f
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: missing parameter name + valid param\s
						 * @param *
						 * @param ?
						 */
						public void p_foo(int x) {
						}
					}
					"""
				},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param *
					         ^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 5)
					* @param ?
					         ^
				Javadoc: Invalid param tag name
				----------
				3. ERROR in X.java (at line 7)
					public void p_foo(int x) {
					                      ^
				Javadoc: Missing tag for parameter x
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
						 * Valid @param but compiler errors
						 * @param a Valid param
						 * @param b Valid param
						 * @param c Valid param
						 */
						public void p_foo(inr a, int b, int c) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 8)
					public void p_foo(inr a, int b, int c) {
					                  ^^^
				inr cannot be resolved to a type
				----------
				""");
	}

	public void test036() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param + compiler errors
						 * @param b Valid param
						 * @param b Valid param
						 * @param c Valid param
						 */
						public void p_foo(inr a, inx b, inq c) {
						}
					}
					""" },
				"""
					----------
					1. ERROR in X.java (at line 5)
						* @param b Valid param
						         ^
					Javadoc: Duplicate tag for parameter
					----------
					2. ERROR in X.java (at line 8)
						public void p_foo(inr a, inx b, inq c) {
						                  ^^^
					inr cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 8)
						public void p_foo(inr a, inx b, inq c) {
						                      ^
					Javadoc: Missing tag for parameter a
					----------
					4. ERROR in X.java (at line 8)
						public void p_foo(inr a, inx b, inq c) {
						                         ^^^
					inx cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 8)
						public void p_foo(inr a, inx b, inq c) {
						                                ^^^
					inq cannot be resolved to a type
					----------
					""");
	}

	public void test037() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @param: class reference instead of param name
						 * @param java.lang.Hashtable
						 */
						public void p_foo(int x) {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @param java.lang.Hashtable
					         ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid param tag name
				----------
				2. ERROR in X.java (at line 6)
					public void p_foo(int x) {
					                      ^
				Javadoc: Missing tag for parameter x
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Hashtable;
					public class X {
						/**
						 * Invalid @param: class reference instead of param name + unused import
						 * @param Hashtable
						 */
						public void p_foo(int x) {
						}
					}
					""" },
			"""
				----------
				1. WARNING in X.java (at line 1)
					import java.util.Hashtable;
					       ^^^^^^^^^^^^^^^^^^^
				The import java.util.Hashtable is never used
				----------
				2. ERROR in X.java (at line 5)
					* @param Hashtable
					         ^^^^^^^^^
				Javadoc: Parameter Hashtable is not declared
				----------
				3. ERROR in X.java (at line 7)
					public void p_foo(int x) {
					                      ^
				Javadoc: Missing tag for parameter x
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/* (non-Javadoc)
	 * Test @throws/@exception tag
	 */
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid @throws tags: documented exception are unchecked
						 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)
						 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)
						 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)
						 */
						public void t_foo() {
						}
					}
					""" });
	}

	public void test051() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws IllegalArgumenException.. Invalid exception: invalid class name
						 * @exception IllegalArgumen..Exception.. Invalid exception: invalid class name
						 */
						public void t_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws IllegalArgumenException.. Invalid exception: invalid class name
					         ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid class name
				----------
				2. ERROR in X.java (at line 4)
					* @exception IllegalArgumen..Exception.. Invalid exception: invalid class name
					            ^^^^^^^^^^^^^^^^^
				Javadoc: Invalid class name
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test052() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws java.awt.AWTexception Invalid exception: unknown type
						 * @throws IOException Invalid exception: unknown type
						 */
						public void t_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws java.awt.AWTexception Invalid exception: unknown type
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: java.awt.AWTexception cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					* @throws IOException Invalid exception: unknown type
					          ^^^^^^^^^^^
				Javadoc: IOException cannot be resolved to a type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test053() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					public class X {
						/**
						 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
						 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
						 */
						public void t_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception EOFException is not declared
				----------
				2. ERROR in X.java (at line 5)
					* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception FileNotFoundException is not declared
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test055() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid @throws tags: documented exception are unchecked but method throws an unknown exception
						 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)
						 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)
						 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)
						 */
						public void t_foo() throws InvalidException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 9)
					public void t_foo() throws InvalidException {
					                           ^^^^^^^^^^^^^^^^
				InvalidException cannot be resolved to a type
				----------
				""");
	}

	public void test056() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws IllegalArgumenException._ Invalid exception: invalid class name
						 * @exception IllegalArgumen.*.Exception.. Invalid exception: invalid class name
						 */
						public void t_foo() throws InvalidException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws IllegalArgumenException._ Invalid exception: invalid class name
					          ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: IllegalArgumenException cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					* @exception IllegalArgumen.*.Exception.. Invalid exception: invalid class name
					            ^^^^^^^^^^^^^^^^^
				Javadoc: Invalid class name
				----------
				3. ERROR in X.java (at line 6)
					public void t_foo() throws InvalidException {
					                           ^^^^^^^^^^^^^^^^
				InvalidException cannot be resolved to a type
				----------
				""");
	}

	public void test057() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws java.awt.AWTexception Invalid exception: unknown type
						 * @throws IOException Invalid exception: unknown type
						 */
						public void t_foo() throws InvalidException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws java.awt.AWTexception Invalid exception: unknown type
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: java.awt.AWTexception cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					* @throws IOException Invalid exception: unknown type
					          ^^^^^^^^^^^
				Javadoc: IOException cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					public void t_foo() throws InvalidException {
					                           ^^^^^^^^^^^^^^^^
				InvalidException cannot be resolved to a type
				----------
				""");
	}

	public void test058() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					public class X {
						/**
						 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
						 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
						 */
						public void t_foo() throws InvalidException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception EOFException is not declared
				----------
				2. ERROR in X.java (at line 5)
					* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception FileNotFoundException is not declared
				----------
				3. ERROR in X.java (at line 7)
					public void t_foo() throws InvalidException {
					                           ^^^^^^^^^^^^^^^^
				InvalidException cannot be resolved to a type
				----------
				""");
	}

	public void test060() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @throws tags: documented exception are unchecked but thrown exception is not documented
						 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)
						 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)
						 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)
						 */
						public void t_foo() throws IllegalAccessException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 9)
					public void t_foo() throws IllegalAccessException {
					                           ^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing tag for declared exception IllegalAccessException
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test061() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws /IllegalArgumenException.. Invalid exception: invalid class name
						 * @exception .IllegalArgumen..Exception.. Invalid exception: invalid class name
						 */
						public void t_foo() throws IllegalAccessException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws /IllegalArgumenException.. Invalid exception: invalid class name
					   ^^^^^^
				Javadoc: Missing class name
				----------
				2. ERROR in X.java (at line 4)
					* @exception .IllegalArgumen..Exception.. Invalid exception: invalid class name
					            ^^
				Javadoc: Invalid class name
				----------
				3. ERROR in X.java (at line 6)
					public void t_foo() throws IllegalAccessException {
					                           ^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing tag for declared exception IllegalAccessException
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test062() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws java.awt.AWTexception Invalid exception: unknown type
						 * @throws IOException Invalid exception: unknown type
						 */
						public void t_foo() throws IllegalAccessException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* @throws java.awt.AWTexception Invalid exception: unknown type
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: java.awt.AWTexception cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					* @throws IOException Invalid exception: unknown type
					          ^^^^^^^^^^^
				Javadoc: IOException cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					public void t_foo() throws IllegalAccessException {
					                           ^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing tag for declared exception IllegalAccessException
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test063() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					public class X {
						/**
						 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
						 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
						 * @throws IOException Invalid exception: unknown type
						 */
						public void t_foo() throws IllegalAccessException {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception EOFException is not declared
				----------
				2. ERROR in X.java (at line 5)
					* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
					          ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Exception FileNotFoundException is not declared
				----------
				3. ERROR in X.java (at line 6)
					* @throws IOException Invalid exception: unknown type
					          ^^^^^^^^^^^
				Javadoc: IOException cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 8)
					public void t_foo() throws IllegalAccessException {
					                           ^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing tag for declared exception IllegalAccessException
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test065() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid @throws tags: documented exception are unchecked but some thrown exception are invalid
						 * @throws IllegalAccessException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws IllegalArgumentException Valid unchecked exception (java.lang.Runtime subclass)
						 * @exception NullPointerException Valid unchecked exception (java.lang.Runtime subclass)
						 * @throws java.awt.AWTError Valid unchecked exception (java.lang.Error subclass)
						 * @exception OutOfMemoryError Valid unchecked exception (java.lang.Runtime subclass)
						 */
						public void t_foo() throws
							IllegalAccessException,\s
							InvalidException,\s
							String,\s
							IllegalArgumentException
						{}
					}
					""" },
					"""
						----------
						1. ERROR in X.java (at line 12)
							InvalidException,\s
							^^^^^^^^^^^^^^^^
						InvalidException cannot be resolved to a type
						----------
						2. ERROR in X.java (at line 13)
							String,\s
							^^^^^^
						No exception of type String can be thrown; an exception type must be a subclass of Throwable
						----------
						""");
	}

	public void test066() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws %IllegalArgumenException Invalid exception: invalid class name
						 * @exception (IllegalArgumen Invalid exception: invalid class name
						 * @exception "IllegalArgumen Invalid exception: invalid class name
						 */
						public void t_foo() throws
							IllegalAccessException,\s
							InvalidException,\s
							String,\s
							IllegalArgumentException
						{}
					}
					""" },
					"""
						----------
						1. ERROR in X.java (at line 3)
							* @throws %IllegalArgumenException Invalid exception: invalid class name
							   ^^^^^^
						Javadoc: Missing class name
						----------
						2. ERROR in X.java (at line 4)
							* @exception (IllegalArgumen Invalid exception: invalid class name
							   ^^^^^^^^^
						Javadoc: Missing class name
						----------
						3. ERROR in X.java (at line 5)
							* @exception "IllegalArgumen Invalid exception: invalid class name
							   ^^^^^^^^^
						Javadoc: Missing class name
						----------
						4. ERROR in X.java (at line 8)
							IllegalAccessException,\s
							^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalAccessException
						----------
						5. ERROR in X.java (at line 9)
							InvalidException,\s
							^^^^^^^^^^^^^^^^
						InvalidException cannot be resolved to a type
						----------
						6. ERROR in X.java (at line 10)
							String,\s
							^^^^^^
						No exception of type String can be thrown; an exception type must be a subclass of Throwable
						----------
						7. ERROR in X.java (at line 11)
							IllegalArgumentException
							^^^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalArgumentException
						----------
						""");
	}

	public void test067() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @throws java.awt.AWTexception Invalid exception: unknown type
						 * @throws IOException Invalid exception: unknown type
						 */
						public void t_foo() throws
							IllegalAccessException,\s
							InvalidException,\s
							String,\s
							IllegalArgumentException
						{}
					}
					""" },
					"""
						----------
						1. ERROR in X.java (at line 3)
							* @throws java.awt.AWTexception Invalid exception: unknown type
							          ^^^^^^^^^^^^^^^^^^^^^
						Javadoc: java.awt.AWTexception cannot be resolved to a type
						----------
						2. ERROR in X.java (at line 4)
							* @throws IOException Invalid exception: unknown type
							          ^^^^^^^^^^^
						Javadoc: IOException cannot be resolved to a type
						----------
						3. ERROR in X.java (at line 7)
							IllegalAccessException,\s
							^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalAccessException
						----------
						4. ERROR in X.java (at line 8)
							InvalidException,\s
							^^^^^^^^^^^^^^^^
						InvalidException cannot be resolved to a type
						----------
						5. ERROR in X.java (at line 9)
							String,\s
							^^^^^^
						No exception of type String can be thrown; an exception type must be a subclass of Throwable
						----------
						6. ERROR in X.java (at line 10)
							IllegalArgumentException
							^^^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalArgumentException
						----------
						""");
	}

	public void test068() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					public class X {
						/**
						 * @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
						 * @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
						 */
						public void t_foo() throws
							IllegalAccessException,\s
							InvalidException,\s
							String,\s
							IllegalArgumentException
						{}
					}
					""" },
					"""
						----------
						1. ERROR in X.java (at line 4)
							* @throws java.io.EOFException Invalid exception: known exception but neither thrown nor unchecked
							          ^^^^^^^^^^^^^^^^^^^^
						Javadoc: Exception EOFException is not declared
						----------
						2. ERROR in X.java (at line 5)
							* @throws FileNotFoundException Invalid exception: known exception but neither thrown nor unchecked
							          ^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Exception FileNotFoundException is not declared
						----------
						3. ERROR in X.java (at line 8)
							IllegalAccessException,\s
							^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalAccessException
						----------
						4. ERROR in X.java (at line 9)
							InvalidException,\s
							^^^^^^^^^^^^^^^^
						InvalidException cannot be resolved to a type
						----------
						5. ERROR in X.java (at line 10)
							String,\s
							^^^^^^
						No exception of type String can be thrown; an exception type must be a subclass of Throwable
						----------
						6. ERROR in X.java (at line 11)
							IllegalArgumentException
							^^^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalArgumentException
						----------
						""");
	}

	public void test069() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.FileNotFoundException;
					public class X {
						/**
						 */
						public void t_foo() throws
							IllegalAccessException,\s
							InvalidException,\s
							String,\s
							java.io.EOFException,\s
							FileNotFoundException,\s
							IOException,\s
							IllegalArgumentException
						{}
					}
					""" },
					"""
						----------
						1. ERROR in X.java (at line 6)
							IllegalAccessException,\s
							^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalAccessException
						----------
						2. ERROR in X.java (at line 7)
							InvalidException,\s
							^^^^^^^^^^^^^^^^
						InvalidException cannot be resolved to a type
						----------
						3. ERROR in X.java (at line 8)
							String,\s
							^^^^^^
						No exception of type String can be thrown; an exception type must be a subclass of Throwable
						----------
						4. ERROR in X.java (at line 9)
							java.io.EOFException,\s
							^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception EOFException
						----------
						5. ERROR in X.java (at line 10)
							FileNotFoundException,\s
							^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception FileNotFoundException
						----------
						6. ERROR in X.java (at line 11)
							IOException,\s
							^^^^^^^^^^^
						IOException cannot be resolved to a type
						----------
						7. ERROR in X.java (at line 12)
							IllegalArgumentException
							^^^^^^^^^^^^^^^^^^^^^^^^
						Javadoc: Missing tag for declared exception IllegalArgumentException
						----------
						""");
	}

	/* (non-Javadoc)
	 * Test @return tag
	 */
	public void test070() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid return declaration
						 *
						 * @return Return an int
						 */
						public int s_foo() {
						  return 0;
						}
					}
					""" });
	}

	public void test071() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid empty return declaration
						 *
						 * @return string
						 */
						public String s_foo() {
						  return "";
						}
					}
					""" });
	}

	public void test072() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid return declaration
						 *
						 * @return Vector A list of things
						 */
						public java.util.Vector s_foo() {
						  return new java.util.Vector();
						}
					}
					""" });
	}

	public void test073() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Missing return declaration
						 */
						public Object[] s_foo() {
						  return new Object[0];
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					public Object[] s_foo() {
					       ^^^^^^^^
				Javadoc: Missing tag for return type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test074() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid return declaration
						 *
						 * @return double
						 * @return Dimension
						 */
						public double s_foo() {
						  return 3.14;
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @return Dimension
					   ^^^^^^
				Javadoc: Duplicate tag for return type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid return declaration
						 *
						 * @return Invalid return on void method
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @return Invalid return on void method
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid return declaration
						 *
						 * @return Invalid return on void method
						 * @return
						 * @return Invalid return on void method
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @return Invalid return on void method
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 6)
					* @return
					   ^^^^^^
				Javadoc: Duplicate tag for return type
				----------
				3. ERROR in X.java (at line 7)
					* @return Invalid return on void method
					   ^^^^^^
				Javadoc: Duplicate tag for return type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/* (non-Javadoc)
	 * Test @see tag
	 */
	// String references
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid string references\s
						 *
						 * @see "
						 * @see "unterminated string
						 * @see "invalid string""
						 * @see "invalid" no text allowed after the string
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see "
					       ^
				Javadoc: Invalid reference
				----------
				2. ERROR in X.java (at line 6)
					* @see "unterminated string
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid reference
				----------
				3. ERROR in X.java (at line 7)
					* @see "invalid string""
					                       ^
				Javadoc: Unexpected text
				----------
				4. ERROR in X.java (at line 8)
					* @see "invalid" no text allowed after the string
					                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid string references\s
						 *
						 * @see "Valid normal string"
						 * @see "Valid \\"string containing\\" \\"double-quote\\""
						 */
						public void s_foo() {
						}
					}
					""" });
	}

	// URL Link references
	public void test085() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid URL link references\s
						 *
						 * @see <
						 * @see <a
						 * @see <a hre
						 * @see <a href
						 * @see <a href=
						 * @see <a href="
						 * @see <a href="invalid
						 * @see <a href="invalid"
						 * @see <a href="invalid">
						 * @see <a href="invalid">invalid
						 * @see <a href="invalid">invalid<
						 * @see <a href="invalid">invalid</
						 * @see <a href="invalid">invalid</a
						 * @see <a href="invalid">invalid</a> no text allowed after the href
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see <
					       ^
				Javadoc: Malformed link reference
				----------
				2. ERROR in X.java (at line 6)
					* @see <a
					       ^^
				Javadoc: Malformed link reference
				----------
				3. ERROR in X.java (at line 7)
					* @see <a hre
					       ^^^^^^
				Javadoc: Malformed link reference
				----------
				4. ERROR in X.java (at line 8)
					* @see <a href
					       ^^^^^^^
				Javadoc: Malformed link reference
				----------
				5. ERROR in X.java (at line 9)
					* @see <a href=
					       ^^^^^^^^
				Javadoc: Malformed link reference
				----------
				6. ERROR in X.java (at line 10)
					* @see <a href="
					       ^^^^^^^^^
				Javadoc: Malformed link reference
				----------
				7. ERROR in X.java (at line 11)
					* @see <a href="invalid
					       ^^^^^^^^^^^^^^^^
				Javadoc: Malformed link reference
				----------
				8. ERROR in X.java (at line 12)
					* @see <a href="invalid"
					       ^^^^^^^^^^^^^^^^^
				Javadoc: Malformed link reference
				----------
				9. ERROR in X.java (at line 13)
					* @see <a href="invalid">
					       ^^^^^^^^^^^^^^^^^^
				Javadoc: Malformed link reference
				----------
				10. ERROR in X.java (at line 14)
					* @see <a href="invalid">invalid
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Malformed link reference
				----------
				11. ERROR in X.java (at line 15)
					* @see <a href="invalid">invalid<
					                                ^
				Javadoc: Malformed link reference
				----------
				12. ERROR in X.java (at line 16)
					* @see <a href="invalid">invalid</
					                                ^^
				Javadoc: Malformed link reference
				----------
				13. ERROR in X.java (at line 17)
					* @see <a href="invalid">invalid</a
					                                ^^^
				Javadoc: Malformed link reference
				----------
				14. ERROR in X.java (at line 18)
					* @see <a href="invalid">invalid</a> no text allowed after the href
					                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test086() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Valid URL references\s
						 *
						 * @see <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</a>
						 * @see <A HREF = "http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</A>
						 * @see <a hReF = "http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</A>
						 */
						public void s_foo() {
						}
					}
					""" });
	}

	public void test087() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid URL references\s
						 *
						 * @see <a xref="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Invalid URL link reference</a>
						 * @see <b href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Invalid URL link reference</a>
						 * @see <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Invalid URL link reference</b>
						 */
						public void s_foo() {
						}
					}
					""" },
				"""
					----------
					1. ERROR in X.java (at line 5)
						* @see <a xref="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Invalid URL link reference</a>
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Javadoc: Malformed link reference
					----------
					2. ERROR in X.java (at line 6)
						* @see <b href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Invalid URL link reference</a>
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Javadoc: Malformed link reference
					----------
					3. ERROR in X.java (at line 7)
						* @see <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Invalid URL link reference</b>
						                                                                                                                         ^^^^
					Javadoc: Malformed link reference
					----------
					""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	// @see Classes references
	public void test090() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Valid local classes references\s
						 *
						 * @see Visibility Valid ref: local class\s
						 * @see Visibility.VcPublic Valid ref: visible inner class of local class\s
						 * @see AbstractVisibility.AvcPublic Valid ref: visible inner class of local class\s
						 * @see test.Visibility Valid ref: local class\s
						 * @see test.Visibility.VcPublic Valid ref: visible inner class of local class\s
						 * @see test.AbstractVisibility.AvcPublic Valid ref: visible inner class of local class\s
						 */
						public void s_foo() {
						}
					}
					""" });
	}

	public void test091() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid local classes references\s
						 *
						 * @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
						 * @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
						 * @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
						 * @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
						 * @see Unknown Invalid ref: unknown class\s
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.Visibility.VcPrivate Invalid ref: non visible inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.VcPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.Visibility.AvcPrivate Invalid ref: non visible inherited inner class of local class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.Visibility.AvcPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see Unknown Invalid ref: unknown class\s
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test092() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
					public class X {
						/**
						 * Valid external classes references\s
						 *
						 * @see VisibilityPublic Valid ref: visible class through import => no warning on import
						 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class\s
						 */
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test093() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
					public class X {
						/**
						 * Invalid external classes references\s
						 *
						 * @see VisibilityPackage Invalid ref: non visible class\s
						 * @see VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class\s
						 * @see VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class\s
						 * @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					* @see VisibilityPackage Invalid ref: non visible class\s
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 8)
					* @see VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 9)
					* @see VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				4. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				""");
	}

	public void test094() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Valid external classes references\s
						 *
						 * @see test.copy.VisibilityPublic Valid ref: visible class through import => no warning on import
						 * @see test.copy.VisibilityPublic.VpPublic Valid ref: visible inner class in visible class\s
						 */
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test095() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid external classes references\s
						 *
						 * @see test.copy.VisibilityPackage Invalid ref: non visible class\s
						 * @see test.copy.VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class\s
						 * @see test.copy.VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class\s
						 * @see test.copy.VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPackage Invalid ref: non visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPackage.VpPrivate Invalid ref: non visible inner class in non visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPackage.VpPublic Invalid ref: visible inner class in non visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPublic.VpPrivate Invalid ref: non visible inner class in visible class\s
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				""");
	}

	// @see Field references
	public void test100() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						int x;
						/**
						 * Valid local class field references
						 *
						 * @see #x Valid ref: visible field
						 * @see Visibility#vf_public Valid ref: visible field
						 * @see Visibility.VcPublic#vf_public Valid ref: visible field in visible inner class
						 */
						public void s_foo() {
						}
					}
					""" });
	}

	public void test101() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid local class field references
						 *
						 * @see Visibility#unknown Invalid ref: non existent field
						 * @see Visibility#vf_private Invalid ref: non visible field
						 * @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)
						 * @see Visibility.VcPrivate#vf_private Invalid ref: non visible inner class (non visible field)
						 * @see Visibility.VcPrivate#vf_public Invalid ref: non visible inner class (public field)
						 * @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class
						 * @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#unknown Invalid ref: non existent field
					                  ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility#vf_private Invalid ref: non visible field
					                  ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.VcPrivate#unknown Invalid ref: non visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.VcPrivate#vf_private Invalid ref: non visible inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see Visibility.VcPrivate#vf_public Invalid ref: non visible inner class (public field)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see Visibility.VcPublic#unknown Invalid ref: non existent field in visible inner class
					                           ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see Visibility.VcPublic#vf_private Invalid ref: non visible field in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				""");
	}

	public void test102() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Valid super class field references in the same package
						 *
						 * @see Visibility#avf_public Valid ref: visible inherited field
						 * @see AbstractVisibility.AvcPublic#avf_public Valid ref: visible field of visible inner class
						 */
						public void s_foo() {
						}
					}
					""" });
	}

	public void test103() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid super class field references in the same package
						 *
						 * @see Visibility#avf_private Invalid ref: non visible inherited field
						 * @see Visibility.AvcPrivate#avf_private Invalid ref: inherited non visible inner class (non visible field)
						 * @see Visibility.AvcPrivate#avf_public Invalid ref: inherited non visible inner class (visible field)
						 * @see Visibility.AvcPublic#avf_private Invalid ref: non visible field of inherited visible inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#avf_private Invalid ref: non visible inherited field
					                  ^^^^^^^^^^^
				Javadoc: The field avf_private is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility.AvcPrivate#avf_private Invalid ref: inherited non visible inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.AvcPrivate#avf_public Invalid ref: inherited non visible inner class (visible field)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.AvcPublic#avf_private Invalid ref: non visible field of inherited visible inner class
					                            ^^^^^^^^^^^
				Javadoc: The field avf_private is not visible
				----------
				""");
	}

	public void test104() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see VisibilityPackage#unknown Invalid ref: non visible class (non existent field)
						 * @see VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)
						 * @see VisibilityPackage#vf_public Invalid ref: non visible class (visible field)
						 * @see VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)
						 * @see VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)
						 * @see VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)
						 * @see VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)
						 * @see VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)
						 * @see VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. WARNING in test\\X.java (at line 2)
					import test.copy.*;
					       ^^^^^^^^^
				The import test.copy is never used
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPackage#unknown Invalid ref: non visible class (non existent field)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see VisibilityPackage#vf_public Invalid ref: non visible class (visible field)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				8. ERROR in test\\X.java (at line 13)
					* @see VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				9. ERROR in test\\X.java (at line 14)
					* @see VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				10. ERROR in test\\X.java (at line 15)
					* @see VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				""");
	}

	public void test105() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see test.copy.VisibilityPackage#unknown Invalid ref: non visible class (non existent field)
						 * @see test.copy.VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)
						 * @see test.copy.VisibilityPackage#vf_public Invalid ref: non visible class (visible field)
						 * @see test.copy.VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)
						 * @see test.copy.VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)
						 * @see test.copy.VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)
						 * @see test.copy.VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)
						 * @see test.copy.VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)
						 * @see test.copy.VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPackage#unknown Invalid ref: non visible class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPackage#vf_private Invalid ref: non visible class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPackage#vf_public Invalid ref: non visible class (visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPackage.VpPrivate#unknown Invalid ref: non visible class and non visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see test.copy.VisibilityPackage.VpPrivate#vf_private Invalid ref: non visible class and non visible inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see test.copy.VisibilityPackage.VpPrivate#vf_public Invalid ref: non visible class and non visible inner class (visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see test.copy.VisibilityPackage.VpPublic#unknown Invalid ref: non visible class and visible inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				8. ERROR in test\\X.java (at line 13)
					* @see test.copy.VisibilityPackage.VpPublic#vf_private Invalid ref: non visible class and visible inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				9. ERROR in test\\X.java (at line 14)
					* @see test.copy.VisibilityPackage.VpPublic#vf_public Invalid ref: non visible class and visible inner class (visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				""");
	}

	public void test106() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
						 * @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
						 * @see VisibilityPublic#vf_public Valid ref to not visible field of other package class
						 * @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
						 * @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)
						 * @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)
						 * @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
						 * @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
						 * @see VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
					       ^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
					       ^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see VisibilityPublic#vf_public Valid ref to not visible field of other package class
					       ^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				8. ERROR in test\\X.java (at line 13)
					* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				9. ERROR in test\\X.java (at line 14)
					* @see VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class
					       ^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: VisibilityPublic cannot be resolved to a type
				----------
				""");
	}

	public void test107() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see VisibilityPublic#vf_public Valid ref to visible field of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vf_public Fully Qualified valid ref to visible field of other package public inner class
						 */
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test108() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
						 * @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
						 * @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
						 * @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)
						 * @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)
						 * @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
						 * @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
						 */
						public void s_foo() {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#unknown Invalid ref to non existent field of other package class
					                        ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				2. ERROR in test\\X.java (at line 8)
					* @see VisibilityPublic#vf_private Invalid ref to not visible field of other package class
					                        ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				3. ERROR in test\\X.java (at line 9)
					* @see VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 11)
					* @see VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 12)
					* @see VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
					                                 ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				7. ERROR in test\\X.java (at line 13)
					* @see VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
					                                 ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				"""
		);
	}

	public void test109() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see test.copy.VisibilityPublic#vf_public Valid ref to not visible field of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vf_public Valid ref to not visible field of other package public inner class
						 */
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test110() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package non visible class fields references
						 *
						 * @see test.copy.VisibilityPublic#unknown Invalid ref to non existent field of other package class
						 * @see test.copy.VisibilityPublic#vf_private Invalid ref to not visible field of other package class
						 * @see test.copy.VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
						 * @see test.copy.VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)
						 * @see test.copy.VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)
						 * @see test.copy.VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
						 * @see test.copy.VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
						 */
						public void s_foo() {
						}
					}
					"""},
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPublic#unknown Invalid ref to non existent field of other package class
					                                  ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPublic#vf_private Invalid ref to not visible field of other package class
					                                  ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPublic.VpPrivate#unknown Invalid ref to a non visible other package private inner class (non existent field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPublic.VpPrivate#vf_private Invalid ref to a non visible other package private inner class (non visible field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see test.copy.VisibilityPublic.VpPrivate#vf_public Invalid ref to a non visible other package private inner class (public field)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see test.copy.VisibilityPublic.VpPublic#unknown Invalid ref to non existent field of other package public inner class
					                                           ^^^^^^^
				Javadoc: unknown cannot be resolved or is not a field
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see test.copy.VisibilityPublic.VpPublic#vf_private Invalid ref to not visible field of other package public inner class
					                                           ^^^^^^^^^^
				Javadoc: The field vf_private is not visible
				----------
				"""
		);
	}

	// @see local method references
	public void test115() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Valid local methods references with array
						 *\s
						 * @see #smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) Valid local method reference
						 * @see #smr_foo(char[], int[][], String[][][], Vector[][][][]) Valid local method reference
						 * @see #smr_foo(char[],int[][],java.lang.String[][][],java.util.Vector[][][][]) Valid local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {
						}
					}
					""" });
	}

	public void test116() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references with array (wrong brackets peer)
						 *\s
						 * @see #smr_foo(char[ , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[]], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][, Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][]], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][[], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[]][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[[][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][][) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][]]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][[]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][]][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][[][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[]][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[[][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector][][][]) Invalid ref: invalid arguments declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @see #smr_foo(char[ , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in X.java (at line 7)
					* @see #smr_foo(char] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in X.java (at line 8)
					* @see #smr_foo(char[] , int[][, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in X.java (at line 9)
					* @see #smr_foo(char[] , int[]], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in X.java (at line 10)
					* @see #smr_foo(char[] , int[[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in X.java (at line 11)
					* @see #smr_foo(char[] , int][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in X.java (at line 12)
					* @see #smr_foo(char[] , int[][], String[][][, Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				8. ERROR in X.java (at line 13)
					* @see #smr_foo(char[] , int[][], String[][]], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				9. ERROR in X.java (at line 14)
					* @see #smr_foo(char[] , int[][], String[][[], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				10. ERROR in X.java (at line 15)
					* @see #smr_foo(char[] , int[][], String[]][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				11. ERROR in X.java (at line 16)
					* @see #smr_foo(char[] , int[][], String[[][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				12. ERROR in X.java (at line 17)
					* @see #smr_foo(char[] , int[][], String][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				13. ERROR in X.java (at line 18)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][][) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				14. ERROR in X.java (at line 19)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][]]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				15. ERROR in X.java (at line 20)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][[]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				16. ERROR in X.java (at line 21)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][]][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				17. ERROR in X.java (at line 22)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][[][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				18. ERROR in X.java (at line 23)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[]][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				19. ERROR in X.java (at line 24)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[[][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				20. ERROR in X.java (at line 25)
					* @see #smr_foo(char[] , int[][], String[][][], Vector][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test117() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references with array (non applicable arrays)
						 *\s
						 * @see #smr_foo(char , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String, Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector) Invalid ref: invalid arguments declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @see #smr_foo(char , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char, int[][], String[][][], Vector[][][][])
				----------
				2. ERROR in X.java (at line 7)
					* @see #smr_foo(char[] , int[], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[], String[][][], Vector[][][][])
				----------
				3. ERROR in X.java (at line 8)
					* @see #smr_foo(char[] , int, String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int, String[][][], Vector[][][][])
				----------
				4. ERROR in X.java (at line 9)
					* @see #smr_foo(char[] , int[][], String[][], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][], Vector[][][][])
				----------
				5. ERROR in X.java (at line 10)
					* @see #smr_foo(char[] , int[][], String[], Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[], Vector[][][][])
				----------
				6. ERROR in X.java (at line 11)
					* @see #smr_foo(char[] , int[][], String, Vector[][][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String, Vector[][][][])
				----------
				7. ERROR in X.java (at line 12)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[][][])
				----------
				8. ERROR in X.java (at line 13)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[][])
				----------
				9. ERROR in X.java (at line 14)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[]) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector[])
				----------
				10. ERROR in X.java (at line 15)
					* @see #smr_foo(char[] , int[][], String[][][], Vector) Invalid ref: invalid arguments declaration
					        ^^^^^^^
				Javadoc: The method smr_foo(char[], int[][], String[][][], Vector[][][][]) in the type X is not applicable for the arguments (char[], int[][], String[][][], Vector)
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test118() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references with array (non applicable arrays)
						 *\s
						 * @see #smr_foo(char[1] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[2][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][3], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[4][][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][5][], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][6], Vector[][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[7][][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][8][][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][9][]) Invalid ref: invalid arguments declaration
						 * @see #smr_foo(char[] , int[][], String[][][], Vector[][][][10]) Invalid ref: invalid arguments declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(char[] array, int[][] matrix, String[][][] dim, Vector[][][][] extra) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @see #smr_foo(char[1] , int[][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in X.java (at line 7)
					* @see #smr_foo(char[] , int[2][], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in X.java (at line 8)
					* @see #smr_foo(char[] , int[][3], String[][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in X.java (at line 9)
					* @see #smr_foo(char[] , int[][], String[4][][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in X.java (at line 10)
					* @see #smr_foo(char[] , int[][], String[][5][], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in X.java (at line 11)
					* @see #smr_foo(char[] , int[][], String[][][6], Vector[][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in X.java (at line 12)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[7][][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				8. ERROR in X.java (at line 13)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][8][][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				9. ERROR in X.java (at line 14)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][9][]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				10. ERROR in X.java (at line 15)
					* @see #smr_foo(char[] , int[][], String[][][], Vector[][][][10]) Invalid ref: invalid arguments declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test120() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Valid local methods references
						 *\s
						 * @see #smr_foo() Valid local method reference
						 * @see #smr_foo(boolean, int, byte, short, char, long, float, double) Valid local method reference
						 * @see #smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see #smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) Valid local method reference
						 * @see #smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference
						 * @see #smr_foo(String, String, int) Valid local method reference
						 * @see #smr_foo(java.lang.String, String, int) Valid local method reference  \s
						 * @see #smr_foo(String, java.lang.String, int) Valid local method reference  \s
						 * @see #smr_foo(java.lang.String, java.lang.String, int) Valid local method reference  \s
						 * @see #smr_foo(String x,String y,int z) Valid local method reference  \s
						 * @see #smr_foo(java.lang.String x,String y, int z) Valid local method reference  \s
						 * @see #smr_foo(String x,java.lang.String y,int z) Valid local method reference  \s
						 * @see #smr_foo(java.lang.String x,java.lang.String y,int z) Valid local method reference  \s
						 * @see #smr_foo(java.util.Hashtable,java.util.Vector,boolean) Valid local method reference
						 * @see #smr_foo(java.util.Hashtable,Vector,boolean) Valid local method reference
						 * @see #smr_foo(java.util.Hashtable a, java.util.Vector b, boolean c) Valid local method reference
						 * @see #smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" });
	}

	public void test121() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #unknown() Invalid ref: undefined local method reference
						 * @see #smrfoo() Invalid ref: undefined local method reference
						 * @see #smr_FOO() Invalid ref: undefined local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #unknown() Invalid ref: undefined local method reference
					        ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				2. ERROR in X.java (at line 6)
					* @see #smrfoo() Invalid ref: undefined local method reference
					        ^^^^^^
				Javadoc: The method smrfoo() is undefined for the type X
				----------
				3. ERROR in X.java (at line 7)
					* @see #smr_FOO() Invalid ref: undefined local method reference
					        ^^^^^^^
				Javadoc: The method smr_FOO() is undefined for the type X
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test122() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(boolean, int, byte, short, char, long, float) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean, int, byte, short, char, long) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean, int, byte, short, char) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean, int, byte, short) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean, int, byte) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean, int) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean) Invalid ref: local method not applicable
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #smr_foo(boolean, int, byte, short, char, long, float) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short, char, long, float)
				----------
				2. ERROR in X.java (at line 6)
					* @see #smr_foo(boolean, int, byte, short, char, long) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short, char, long)
				----------
				3. ERROR in X.java (at line 7)
					* @see #smr_foo(boolean, int, byte, short, char) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short, char)
				----------
				4. ERROR in X.java (at line 8)
					* @see #smr_foo(boolean, int, byte, short) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte, short)
				----------
				5. ERROR in X.java (at line 9)
					* @see #smr_foo(boolean, int, byte) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int, byte)
				----------
				6. ERROR in X.java (at line 10)
					* @see #smr_foo(boolean, int) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean, int)
				----------
				7. ERROR in X.java (at line 11)
					* @see #smr_foo(boolean) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (boolean)
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test123() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(int, String, String) Invalid ref: local method not applicable
						 * @see #smr_foo(String, int, String) Invalid ref: local method not applicable
						 * @see #smr_foo(String, String) Invalid ref: local method not applicable
						 * @see #smr_foo(String) Invalid ref: local method not applicable
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #smr_foo(int, String, String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (int, String, String)
				----------
				2. ERROR in X.java (at line 6)
					* @see #smr_foo(String, int, String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, int, String)
				----------
				3. ERROR in X.java (at line 7)
					* @see #smr_foo(String, String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, String)
				----------
				4. ERROR in X.java (at line 8)
					* @see #smr_foo(String) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String)
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test124() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable
						 * @see #smr_foo(java.util.Hashtable,boolean,java.util.Vector) Invalid ref: local method not applicable
						 * @see #smr_foo(boolean,java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable
						 * @see #smr_foo(java.util.Hashtable) Invalid ref: local method not applicable
						 * @see #smr_foo(java.util.Vector) Invalid ref: local method not applicable
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #smr_foo(java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, Vector)
				----------
				2. ERROR in X.java (at line 6)
					* @see #smr_foo(java.util.Hashtable,boolean,java.util.Vector) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, boolean, Vector)
				----------
				3. ERROR in X.java (at line 7)
					* @see #smr_foo(boolean,java.util.Hashtable,java.util.Vector) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (boolean, Hashtable, Vector)
				----------
				4. ERROR in X.java (at line 8)
					* @see #smr_foo(java.util.Hashtable) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable)
				----------
				5. ERROR in X.java (at line 9)
					* @see #smr_foo(java.util.Vector) Invalid ref: local method not applicable
					        ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Vector)
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test125() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(boolean,int i,byte y,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int, byte y, short s, char c, long l, float f, double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int i,byte,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int i,byte y,short,char c,long l,float f,double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int i,byte y,short s,char,long l,float f,double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int i,byte y,short s,char c,long,float f,double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float,double d) Invalid reference: mixed argument declaration
						 * @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float f,double) Invalid reference: mixed argument declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #smr_foo(boolean,int i,byte y,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in X.java (at line 6)
					* @see #smr_foo(boolean b,int, byte y, short s, char c, long l, float f, double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in X.java (at line 7)
					* @see #smr_foo(boolean b,int i,byte,short s,char c,long l,float f,double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in X.java (at line 8)
					* @see #smr_foo(boolean b,int i,byte y,short,char c,long l,float f,double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in X.java (at line 9)
					* @see #smr_foo(boolean b,int i,byte y,short s,char,long l,float f,double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in X.java (at line 10)
					* @see #smr_foo(boolean b,int i,byte y,short s,char c,long,float f,double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in X.java (at line 11)
					* @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float,double d) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				8. ERROR in X.java (at line 12)
					* @see #smr_foo(boolean b,int i,byte y,short s,char c,long l,float f,double) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test126() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(String,String y,int z) Invalid reference: mixed argument declaration
						 * @see #smr_foo(java.lang.String x,String, int z) Invalid reference: mixed argument declaration
						 * @see #smr_foo(String x,java.lang.String y,int) Invalid reference: mixed argument declaration
						 * @see #smr_foo(java.lang.String,java.lang.String,int z) Invalid reference: mixed argument declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see #smr_foo(String,String y,int z) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in X.java (at line 6)
					* @see #smr_foo(java.lang.String x,String, int z) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in X.java (at line 7)
					* @see #smr_foo(String x,java.lang.String y,int) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in X.java (at line 8)
					* @see #smr_foo(java.lang.String,java.lang.String,int z) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test127() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see #smr_foo(Hashtable,java.util.Vector,boolean) Invalid reference: unresolved argument type
						 * @see #smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type
						 * @see #smr_foo(Hashtable a, java.util.Vector b, boolean c) Invalid reference: unresolved argument type
						 * @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
						 * @see #smr_foo(java.util.Hashtable a, java.util.Vector b, boolean) Invalid reference: mixed argument declaration
						 * @see #smr_foo(java.util.Hashtable, Vector, boolean c) Invalid reference: mixed argument declaration
						 * @see #smr_foo(Hashtable a, java.util.Vector, boolean c) Invalid reference: mixed argument declaration
						 * @see #smr_foo(Hashtable, Vector b, boolean c) Invalid reference: mixed argument declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @see #smr_foo(Hashtable,java.util.Vector,boolean) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 7)
					* @see #smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 8)
					* @see #smr_foo(Hashtable a, java.util.Vector b, boolean c) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 9)
					* @see #smr_foo(Hashtable a, Vector b, boolean c) Invalid reference: unresolved argument type
					                ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 10)
					* @see #smr_foo(java.util.Hashtable a, java.util.Vector b, boolean) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in X.java (at line 11)
					* @see #smr_foo(java.util.Hashtable, Vector, boolean c) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in X.java (at line 12)
					* @see #smr_foo(Hashtable a, java.util.Vector, boolean c) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				8. ERROR in X.java (at line 13)
					* @see #smr_foo(Hashtable, Vector b, boolean c) Invalid reference: mixed argument declaration
					               ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test130() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Valid local methods references
						 *\s
						 * @see X#smr_foo() Valid local method reference
						 * @see X#smr_foo(boolean, int, byte, short, char, long, float, double) Valid local method reference
						 * @see X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see X#smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) Valid local method reference
						 * @see X#smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference
						 * @see X#smr_foo(String, String, int) Valid local method reference
						 * @see X#smr_foo(java.lang.String, String, int) Valid local method reference  \s
						 * @see X#smr_foo(String, java.lang.String, int) Valid local method reference  \s
						 * @see X#smr_foo(java.lang.String, java.lang.String, int) Valid local method reference  \s
						 * @see X#smr_foo(String x,String y,int z) Valid local method reference  \s
						 * @see X#smr_foo(java.lang.String x,String y, int z) Valid local method reference  \s
						 * @see X#smr_foo(String x,java.lang.String y,int z) Valid local method reference  \s
						 * @see X#smr_foo(java.lang.String x,java.lang.String y,int z) Valid local method reference  \s
						 * @see X#smr_foo(java.util.Hashtable,java.util.Vector,boolean) Valid local method reference
						 * @see X#smr_foo(java.util.Hashtable,Vector,boolean) Valid local method reference
						 * @see X#smr_foo(java.util.Hashtable a, java.util.Vector b, boolean c) Valid local method reference
						 * @see X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" });
	}

	public void test131() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see X#unknown() Invalid ref: undefined local method reference
						 * @see X#smrfoo() Invalid ref: undefined local method reference
						 * @see X#smr_FOO() Invalid ref: undefined local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 5)
					* @see X#unknown() Invalid ref: undefined local method reference
					         ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				2. ERROR in X.java (at line 6)
					* @see X#smrfoo() Invalid ref: undefined local method reference
					         ^^^^^^
				Javadoc: The method smrfoo() is undefined for the type X
				----------
				3. ERROR in X.java (at line 7)
					* @see X#smr_FOO() Invalid ref: undefined local method reference
					         ^^^^^^^
				Javadoc: The method smr_FOO() is undefined for the type X
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test132() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see X#smr_foo(Object) Invalid ref: local method not applicable
						 * @see X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable
						 * @see X#smr_foo(String, int) Invalid ref: local method not applicable
						 * @see X#smr_foo(String) Invalid ref: local method not applicable
						 * @see X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @see X#smr_foo(Object) Invalid ref: local method not applicable
					         ^^^^^^^
				Javadoc: The method smr_foo() in the type X is not applicable for the arguments (Object)
				----------
				2. ERROR in X.java (at line 7)
					* @see X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable
					         ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (int, byte, short, char, long, float, double)
				----------
				3. ERROR in X.java (at line 8)
					* @see X#smr_foo(String, int) Invalid ref: local method not applicable
					         ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, int)
				----------
				4. ERROR in X.java (at line 9)
					* @see X#smr_foo(String) Invalid ref: local method not applicable
					         ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String)
				----------
				5. ERROR in X.java (at line 10)
					* @see X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable
					         ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, Vector)
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test133() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration
						 * @see X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration
						 * @see X#smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type
						 * @see X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in X.java (at line 6)
					* @see X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration
					                ^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in X.java (at line 7)
					* @see X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration
					                ^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in X.java (at line 8)
					* @see X#smr_foo(Hashtable,Vector,boolean) Invalid reference: unresolved argument type
					                 ^^^^^^^^^
				Javadoc: Hashtable cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 9)
					* @see X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration
					                ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test135() {
		this.runConformTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"""
					package test.deep.qualified.name.p;
					import java.util.Vector;
					public class X {
						/**
						 * Valid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.X#smr_foo() Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(boolean, int, byte, short, char, long, float, double) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(boolean,int,byte,short,char,long,float,double) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(boolean a1,int a2,byte a3,short a4,char a5,long a6,float a7,double a8) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(String, String, int) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String, String, int) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(String, java.lang.String, int) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String, java.lang.String, int) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(String x,String y,int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String x,String y, int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(String x,java.lang.String y,int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(java.lang.String x,java.lang.String y,int z) Valid local method reference  \s
						 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,java.util.Vector,boolean) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,Vector,boolean) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable a, java.util.Vector b, boolean c) Valid local method reference
						 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable a, Vector b, boolean c) Valid local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" });
	}

	public void test136() {
		this.runNegativeTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"""
					package test.deep.qualified.name.p;
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.X#unknown() Invalid ref: undefined local method reference
						 * @see test.deep.qualified.name.p.X#smrfoo() Invalid ref: undefined local method reference
						 * @see test.deep.qualified.name.p.X#smr_FOO() Invalid ref: undefined local method reference
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 6)
					* @see test.deep.qualified.name.p.X#unknown() Invalid ref: undefined local method reference
					                                    ^^^^^^^
				Javadoc: The method unknown() is undefined for the type X
				----------
				2. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 7)
					* @see test.deep.qualified.name.p.X#smrfoo() Invalid ref: undefined local method reference
					                                    ^^^^^^
				Javadoc: The method smrfoo() is undefined for the type X
				----------
				3. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 8)
					* @see test.deep.qualified.name.p.X#smr_FOO() Invalid ref: undefined local method reference
					                                    ^^^^^^^
				Javadoc: The method smr_FOO() is undefined for the type X
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test137() {
		this.runNegativeTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"""
					package test.deep.qualified.name.p;
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.X#smr_foo(Object) Invalid ref: local method not applicable
						 * @see test.deep.qualified.name.p.X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable
						 * @see test.deep.qualified.name.p.X#smr_foo(String, int) Invalid ref: local method not applicable
						 * @see test.deep.qualified.name.p.X#smr_foo(String) Invalid ref: local method not applicable
						 * @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo() {
						}
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 7)
					* @see test.deep.qualified.name.p.X#smr_foo(Object) Invalid ref: local method not applicable
					                                    ^^^^^^^
				Javadoc: The method smr_foo() in the type X is not applicable for the arguments (Object)
				----------
				2. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 8)
					* @see test.deep.qualified.name.p.X#smr_foo(int, byte, short, char, long, float, double) Invalid ref: local method not applicable
					                                    ^^^^^^^
				Javadoc: The method smr_foo(boolean, int, byte, short, char, long, float, double) in the type X is not applicable for the arguments (int, byte, short, char, long, float, double)
				----------
				3. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 9)
					* @see test.deep.qualified.name.p.X#smr_foo(String, int) Invalid ref: local method not applicable
					                                    ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String, int)
				----------
				4. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 10)
					* @see test.deep.qualified.name.p.X#smr_foo(String) Invalid ref: local method not applicable
					                                    ^^^^^^^
				Javadoc: The method smr_foo(String, String, int) in the type X is not applicable for the arguments (String)
				----------
				5. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 11)
					* @see test.deep.qualified.name.p.X#smr_foo(java.util.Hashtable,Vector) Invalid ref: local method not applicable
					                                    ^^^^^^^
				Javadoc: The method smr_foo(Hashtable, Vector, boolean) in the type X is not applicable for the arguments (Hashtable, Vector)
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test138() {
		this.runNegativeTest(
			new String[] {
				"test/deep/qualified/name/p/X.java",
				"""
					package test.deep.qualified.name.p;
					import java.util.Vector;
					public class X {
						/**
						 * Invalid local methods references
						 *\s
						 * @see test.deep.qualified.name.p.X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration
						 * @see test.deep.qualified.name.p.X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration
						 * @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration
						 * @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration
						 */ \s
						public void s_foo() {
						}
					
						// Empty methods definition for reference
						public void smr_foo(boolean b, int i, byte y, short s, char c, long l, float f, double d) {
						}
						public void smr_foo(String str1, java.lang.String str2, int i) {
						}
						public void smr_foo(java.util.Hashtable h, java.util.Vector v, boolean b) {
						}
					}
					""" },
			"""
				----------
				1. WARNING in test\\deep\\qualified\\name\\p\\X.java (at line 2)
					import java.util.Vector;
					       ^^^^^^^^^^^^^^^^
				The import java.util.Vector is never used
				----------
				2. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 7)
					* @see test.deep.qualified.name.p.X#smr_foo(boolean,int i,byte,short s,char,long l,float,double d) Invalid reference: mixed argument declaration
					                                           ^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 8)
					* @see test.deep.qualified.name.p.X#smr_foo(String,String y,int) Invalid reference: mixed argument declaration
					                                           ^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 9)
					* @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration
					                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\deep\\qualified\\name\\p\\X.java (at line 10)
					* @see test.deep.qualified.name.p.X#smr_foo(Hashtable,Vector,boolean b) Invalid reference: mixed argument declaration
					                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test140() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Valid package class methods references
						 *\s
						 * @see Visibility#vm_public() Valid ref: visible method
						 * @see Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class
						 * @see test.Visibility#vm_public() Valid ref: visible method
						 * @see test.Visibility.VcPublic#vm_public() Valid ref: visible method in visible inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" });
	}

	public void test141() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package class methods references (non-existence)
						 *\s
						 * @see Visibility#unknown() Invalid ref: non-existent method
						 * @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
						 * @see Unknown#vm_public() Invalid ref: non-existent class
						 * @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#unknown() Invalid ref: non-existent method
					                  ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility.VcPublic#unknown() Invalid ref: non existent method in visible inner class
					                           ^^^^^^^
				Javadoc: The method unknown() is undefined for the type Visibility.VcPublic
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Unknown#vm_public() Invalid ref: non-existent class
					       ^^^^^^^
				Javadoc: Unknown cannot be resolved to a type
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.Unknown#vm_public() Invalid ref: non existent inner class
					       ^^^^^^^^^^^^^^^^^^
				Javadoc: Visibility.Unknown cannot be resolved to a type
				----------
				""");
	}

	public void test142() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package class methods references (non-visible)
						 *\s
						 * @see Visibility#vm_private() Invalid ref: non-visible method
						 * @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
						 * @see Visibility.VcPrivate#vm_private() Invalid ref: non visible inner class (non visible method)
						 * @see Visibility.VcPrivate#vm_private(boolean, String) Invalid ref: non visible inner class (non applicable method)
						 * @see Visibility.VcPrivate#vm_public() Invalid ref: non visible inner class (visible method)
						 * @see Visibility.VcPrivate#vm_public(Object, float) Invalid ref: non visible inner class (non applicable visible method)
						 * @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#vm_private() Invalid ref: non-visible method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility.VcPrivate#unknown() Invalid ref: non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.VcPrivate#vm_private() Invalid ref: non visible inner class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.VcPrivate#vm_private(boolean, String) Invalid ref: non visible inner class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see Visibility.VcPrivate#vm_public() Invalid ref: non visible inner class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see Visibility.VcPrivate#vm_public(Object, float) Invalid ref: non visible inner class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.VcPrivate is not visible
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see Visibility.VcPublic#vm_private() Invalid ref: non visible method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() from the type Visibility.VcPublic is not visible
				----------
				""");
	}

	public void test143() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package class methods references (non-applicable)
						 *\s
						 * @see Visibility#vm_private(int) Invalid ref: non-applicable method
						 * @see Visibility#vm_public(String) Invalid ref: non-applicable method
						 * @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
						 * @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#vm_private(int) Invalid ref: non-applicable method
					                  ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility is not applicable for the arguments (int)
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility#vm_public(String) Invalid ref: non-applicable method
					                  ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility is not applicable for the arguments (String)
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.VcPublic#vm_private(Integer, byte) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^^
				Javadoc: The method vm_private() in the type Visibility.VcPublic is not applicable for the arguments (Integer, byte)
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.VcPublic#vm_public(Double z, Boolean x) Invalid ref: non applicable method in visible inner class
					                           ^^^^^^^^^
				Javadoc: The method vm_public() in the type Visibility.VcPublic is not applicable for the arguments (Double, Boolean)
				----------
				""");
	}

	public void test144() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package class methods references (invalid arguments)
						 *\s
						 * @see Visibility#vm_private(,) Invalid ref: invalid argument declaration
						 * @see Visibility#vm_public(,String) Invalid ref: invalid argument declaration
						 * @see Visibility.VcPrivate#vm_private(char, double d) Invalid ref: invalid argument declaration
						 * @see Visibility.VcPrivate#vm_public(#) Invalid ref: invalid argument declaration
						 * @see Visibility.VcPublic#vm_private(a a a) Invalid ref: invalid argument declaration
						 * @see Visibility.VcPublic#vm_public(####) Invalid ref: Invalid ref: invalid argument declaration
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#vm_private(,) Invalid ref: invalid argument declaration
					                            ^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility#vm_public(,String) Invalid ref: invalid argument declaration
					                           ^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.VcPrivate#vm_private(char, double d) Invalid ref: invalid argument declaration
					                                      ^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.VcPrivate#vm_public(#) Invalid ref: invalid argument declaration
					                                     ^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see Visibility.VcPublic#vm_private(a a a) Invalid ref: invalid argument declaration
					                                     ^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see Visibility.VcPublic#vm_public(####) Invalid ref: Invalid ref: invalid argument declaration
					                                    ^^
				Javadoc: Invalid parameters declaration
				----------
				""");
	}

	public void test145() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Valid package super class methods references
						 *\s
						 * @see Visibility#avm_public() Valid ref: visible inherited method
						 * @see AbstractVisibility.AvcPublic#avm_public() Valid ref: visible method in visible inner class
						 * @see test.Visibility#avm_public() Valid ref: visible inherited method
						 * @see test.AbstractVisibility.AvcPublic#avm_public() Valid ref: visible method in visible inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" });
	}

	public void test146() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package super class methods references (non-visible)
						 *\s
						 * @see Visibility#avm_private() Invalid ref: non-visible inherited method
						 * @see Visibility.AvcPrivate#unknown() Invalid ref: non visible inherited inner class (non existent method)
						 * @see Visibility.AvcPrivate#avm_private() Invalid ref: non visible inherited inner class (non visible method)
						 * @see Visibility.AvcPrivate#avm_private(boolean, String) Invalid ref: non visible inherited inner class (non applicable method)
						 * @see Visibility.AvcPrivate#avm_public() Invalid ref: non visible inherited inner class (visible method)
						 * @see Visibility.AvcPrivate#avm_public(Object, float) Invalid ref: non visible inherited inner class (non applicable visible method)
						 * @see Visibility.AvcPublic#avm_private() Invalid ref: non visible inherited method in visible inherited inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#avm_private() Invalid ref: non-visible inherited method
					                  ^^^^^^^^^^^
				Javadoc: The method avm_private() from the type AbstractVisibility is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility.AvcPrivate#unknown() Invalid ref: non visible inherited inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.AvcPrivate#avm_private() Invalid ref: non visible inherited inner class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.AvcPrivate#avm_private(boolean, String) Invalid ref: non visible inherited inner class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see Visibility.AvcPrivate#avm_public() Invalid ref: non visible inherited inner class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see Visibility.AvcPrivate#avm_public(Object, float) Invalid ref: non visible inherited inner class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type Visibility.AvcPrivate is not visible
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see Visibility.AvcPublic#avm_private() Invalid ref: non visible inherited method in visible inherited inner class
					                            ^^^^^^^^^^^
				Javadoc: The method avm_private() from the type AbstractVisibility.AvcPublic is not visible
				----------
				""");
	}

	public void test147() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package super class methods references (non-applicable)
						 *\s
						 * @see Visibility#avm_private(int) Invalid ref: non-applicable inherited method
						 * @see Visibility#avm_public(String) Invalid ref: non-applicable inherited method
						 * @see Visibility.AvcPublic#avm_private(Integer, byte) Invalid ref: non applicable inherited method in visible inner class
						 * @see Visibility.AvcPublic#avm_public(Double z, Boolean x) Invalid ref: non applicable inherited method in visible inner class
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#avm_private(int) Invalid ref: non-applicable inherited method
					                  ^^^^^^^^^^^
				Javadoc: The method avm_private() in the type AbstractVisibility is not applicable for the arguments (int)
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility#avm_public(String) Invalid ref: non-applicable inherited method
					                  ^^^^^^^^^^
				Javadoc: The method avm_public() in the type AbstractVisibility is not applicable for the arguments (String)
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.AvcPublic#avm_private(Integer, byte) Invalid ref: non applicable inherited method in visible inner class
					                            ^^^^^^^^^^^
				Javadoc: The method avm_private() in the type AbstractVisibility.AvcPublic is not applicable for the arguments (Integer, byte)
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.AvcPublic#avm_public(Double z, Boolean x) Invalid ref: non applicable inherited method in visible inner class
					                            ^^^^^^^^^^
				Javadoc: The method avm_public() in the type AbstractVisibility.AvcPublic is not applicable for the arguments (Double, Boolean)
				----------
				""");
	}

	public void test148() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid package super class methods references (invalid arguments)
						 *\s
						 * @see Visibility#avm_private(,,,,) Invalid ref: invalid argument declaration
						 * @see Visibility#avm_public(String,,,) Invalid ref: invalid argument declaration
						 * @see Visibility.AvcPrivate#avm_private(char c, double) Invalid ref: invalid argument declaration
						 * @see Visibility.AvcPrivate#avm_public(*) Invalid ref: invalid argument declaration
						 * @see Visibility.AvcPublic#avm_private(a a a) Invalid ref: invalid argument declaration
						 * @see Visibility.AvcPublic#avm_public(*****) Invalid ref: invalid argument declaration
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see Visibility#avm_private(,,,,) Invalid ref: invalid argument declaration
					                             ^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see Visibility#avm_public(String,,,) Invalid ref: invalid argument declaration
					                            ^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see Visibility.AvcPrivate#avm_private(char c, double) Invalid ref: invalid argument declaration
					                                        ^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see Visibility.AvcPrivate#avm_public(*) Invalid ref: invalid argument declaration
					                                       ^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see Visibility.AvcPublic#avm_private(a a a) Invalid ref: invalid argument declaration
					                                       ^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see Visibility.AvcPublic#avm_public(*****) Invalid ref: invalid argument declaration
					                                      ^^
				Javadoc: Invalid parameters declaration
				----------
				""");
	}

	public void test150() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.*;
					public class X {
						/**
						 * Invalid other package non visible class methods references (non existent/visible arguments)
						 *\s
						 * @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 * @see VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)
						 * @see VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)
						 * @see VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)
						 * @see VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)
						 * @see VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)
						 * @see VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)
						 * @see VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)
						 * @see VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)
						 * @see VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)
						 * @see VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)
						 * @see VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)
						 * @see VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)
						 * @see VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)
						 * @see VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. WARNING in test\\X.java (at line 2)
					import test.copy.*;
					       ^^^^^^^^^
				The import test.copy is never used
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)
					       ^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				8. ERROR in test\\X.java (at line 13)
					* @see VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				9. ERROR in test\\X.java (at line 14)
					* @see VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				10. ERROR in test\\X.java (at line 15)
					* @see VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				11. ERROR in test\\X.java (at line 16)
					* @see VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				12. ERROR in test\\X.java (at line 17)
					* @see VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				13. ERROR in test\\X.java (at line 18)
					* @see VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				14. ERROR in test\\X.java (at line 19)
					* @see VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				15. ERROR in test\\X.java (at line 20)
					* @see VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				16. ERROR in test\\X.java (at line 21)
					* @see VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPackage is not visible
				----------
				""");
	}

	public void test151() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPackage;
					public class X {
						/**
						 * Invalid other package non visible class methods references (invalid arguments)
						 *\s
						 * @see VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration
						 * @see VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration
						 * @see VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration
						 * @see VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration
						 * @see VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration
						 * @see VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 2)
					import test.copy.VisibilityPackage;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The type test.copy.VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration
					                                   ^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration
					                                  ^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration
					                                             ^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration
					                                            ^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration
					                                            ^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration
					                                           ^^
				Javadoc: Invalid parameters declaration
				----------
				""",
				JavacTestOptions.DEFAULT);
	}

	public void test152() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other fully qualified name package non visible class methods references (non existent/visible arguments)
						 *\s
						 * @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
						 * @see test.copy.VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)
						 * @see test.copy.VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)
						 * @see test.copy.VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)
						 * @see test.copy.VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)
						 * @see test.copy.VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)
						 * @see test.copy.VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)
						 * @see test.copy.VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)
						 * @see test.copy.VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)
						 * @see test.copy.VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)
						 * @see test.copy.VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)
						 * @see test.copy.VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)
						 * @see test.copy.VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)
						 * @see test.copy.VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)
						 * @see test.copy.VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPackage#unknown() Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPackage#vm_private() Invalid ref: non visible class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPackage#vm_private(boolean) Invalid ref: non visible class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPackage#vm_public() Invalid ref: non visible class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see test.copy.VisibilityPackage#vm_public(long,long,long,int) Invalid ref: non visible class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see test.copy.VisibilityPackage.VpPrivate#unknown() Invalid ref: non visible class and non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see test.copy.VisibilityPackage.VpPrivate#vm_private() Invalid ref: non visible class and non visible inner class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				8. ERROR in test\\X.java (at line 13)
					* @see test.copy.VisibilityPackage.VpPrivate#vm_private(boolean, String) Invalid ref: non visible class and non visible inner class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				9. ERROR in test\\X.java (at line 14)
					* @see test.copy.VisibilityPackage.VpPrivate#vm_public() Invalid ref: non visible class and non visible inner class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				10. ERROR in test\\X.java (at line 15)
					* @see test.copy.VisibilityPackage.VpPrivate#vm_public(Object, float) Invalid ref: non visible class and non visible inner class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				11. ERROR in test\\X.java (at line 16)
					* @see test.copy.VisibilityPackage.VpPublic#unknown() Invalid ref: non visible class and visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				12. ERROR in test\\X.java (at line 17)
					* @see test.copy.VisibilityPackage.VpPublic#vm_private() Invalid ref: non visible class and visible inner class (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				13. ERROR in test\\X.java (at line 18)
					* @see test.copy.VisibilityPackage.VpPublic#vm_private(boolean, String) Invalid ref: non visible class and visible inner class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				14. ERROR in test\\X.java (at line 19)
					* @see test.copy.VisibilityPackage.VpPublic#vm_public() Invalid ref: non visible class and visible inner class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				15. ERROR in test\\X.java (at line 20)
					* @see test.copy.VisibilityPackage.VpPublic#vm_public(Object, float) Invalid ref: non visible class and visible inner class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPackage is not visible
				----------
				""");
	}

	public void test153() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other fully qualified name package non visible class methods references (invalid arguments)
						 *\s
						 * @see test.copy.VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration
						 */ \s
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPackage#vm_private(boolean-) Invalid ref: invalid argument declaration
					                                             ^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPackage#vm_public(long, int() Invalid ref: invalid argument declaration
					                                            ^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPackage.VpPrivate#vm_private(char, a double d()) Invalid ref: invalid argument declaration
					                                                       ^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPackage.VpPrivate#vm_public(()) Invalid ref: invalid argument declaration
					                                                      ^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see test.copy.VisibilityPackage.VpPublic#vm_private(char double) Invalid ref: invalid argument declaration
					                                                      ^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see test.copy.VisibilityPackage.VpPublic#vm_public((((() Invalid ref: invalid argument declaration
					                                                     ^^
				Javadoc: Invalid parameters declaration
				----------
				""");
	}

	public void test154() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public class X {
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
						public void s_foo() {
						}
					}
					"""
			}
		);
	}

	public void test155() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-existent)
						 *\s
						 * @see VisibilityPublic#unknown() Invalid ref: non existent method
						 * @see VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#unknown() Invalid ref: non existent method
					                        ^^^^^^^
				Javadoc: The method unknown() is undefined for the type VisibilityPublic
				----------
				2. ERROR in test\\X.java (at line 8)
					* @see VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class
					                                 ^^^^^^^
				Javadoc: The method unknown() is undefined for the type VisibilityPublic.VpPublic
				----------
				""");
	}

	public void test156() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-visible)
						 *\s
						 * @see VisibilityPublic#vm_private() Invalid ref: non visible method in visible class
						 * @see VisibilityPublic#vm_public() Valid ref: visible method in visible class
						 * @see VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)
						 * @see VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)
						 * @see VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)
						 * @see VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)
						 * @see VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)
						 * @see VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#vm_private() Invalid ref: non visible method in visible class
					                        ^^^^^^^^^^
				Javadoc: The method vm_private() from the type VisibilityPublic is not visible
				----------
				2. ERROR in test\\X.java (at line 9)
					* @see VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 11)
					* @see VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 12)
					* @see VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 13)
					* @see VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type VisibilityPublic.VpPrivate is not visible
				----------
				7. ERROR in test\\X.java (at line 14)
					* @see VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class
					                                 ^^^^^^^^^^
				Javadoc: The method vm_private() from the type VisibilityPublic.VpPublic is not visible
				----------
				""");
	}

	public void test157() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-applicable)
						 *\s
						 * @see VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class
						 * @see VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class
						 * @see VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class
						 * @see VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class
					                        ^^^^^^^^^^
				Javadoc: The method vm_private() in the type VisibilityPublic is not applicable for the arguments (boolean)
				----------
				2. ERROR in test\\X.java (at line 8)
					* @see VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class
					                        ^^^^^^^^^
				Javadoc: The method vm_public() in the type VisibilityPublic is not applicable for the arguments (long, long, long, int)
				----------
				3. ERROR in test\\X.java (at line 9)
					* @see VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class
					                                 ^^^^^^^^^^
				Javadoc: The method vm_private() in the type VisibilityPublic.VpPublic is not applicable for the arguments (boolean, String)
				----------
				4. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)
					                                 ^^^^^^^^^
				Javadoc: The method vm_public() in the type VisibilityPublic.VpPublic is not applicable for the arguments (Object, float)
				----------
				""");
	}

	public void test158() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-existent)
						 *\s
						 * @see VisibilityPublic#vm_private("boolean") Invalid ref: invalid argument declaration
						 * @see VisibilityPublic#vm_public(long, "int) Invalid ref: invalid argument definition
						 * @see VisibilityPublic.VpPrivate#vm_private(double d()) Invalid ref: invalid argument declaration
						 * @see VisibilityPublic.VpPrivate#vm_public(") Invalid ref: invalid argument declaration
						 * @see VisibilityPublic.VpPublic#vm_private(d()) Invalid ref: invalid argument declaration
						 * @see VisibilityPublic.VpPublic#vm_public(205) Invalid ref: invalid argument declaration
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. WARNING in test\\X.java (at line 2)
					import test.copy.VisibilityPublic;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				The import test.copy.VisibilityPublic is never used
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see VisibilityPublic#vm_private("boolean") Invalid ref: invalid argument declaration
					                                  ^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see VisibilityPublic#vm_public(long, "int) Invalid ref: invalid argument definition
					                                 ^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see VisibilityPublic.VpPrivate#vm_private(double d()) Invalid ref: invalid argument declaration
					                                            ^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see VisibilityPublic.VpPrivate#vm_public(") Invalid ref: invalid argument declaration
					                                           ^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see VisibilityPublic.VpPublic#vm_private(d()) Invalid ref: invalid argument declaration
					                                           ^^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see VisibilityPublic.VpPublic#vm_public(205) Invalid ref: invalid argument declaration
					                                          ^^^^
				Javadoc: Invalid parameters declaration
				----------
				""");
	}

	public void test159() {
		runConformReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Valid other package visible class methods references\s
						 *\s
						 * @see test.copy.VisibilityPublic#vm_public() Valid ref to not visible method of other package class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public() Valid ref to visible method of other package public inner class
						 */
						public void s_foo() {
						}
					}
					""" });
	}

	public void test160() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-existent)
						 *\s
						 * @see test.copy.VisibilityPublic#unknown() Invalid ref: non existent method
						 * @see test.copy.VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPublic#unknown() Invalid ref: non existent method
					                                  ^^^^^^^
				Javadoc: The method unknown() is undefined for the type VisibilityPublic
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPublic.VpPublic#unknown() Invalid ref: non existent method of visible inner class
					                                           ^^^^^^^
				Javadoc: The method unknown() is undefined for the type VisibilityPublic.VpPublic
				----------
				""");
	}

	public void test161() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-visible)
						 *\s
						 * @see test.copy.VisibilityPublic#vm_private() Invalid ref: non visible method in visible class
						 * @see test.copy.VisibilityPublic#vm_public() Valid ref: visible method in visible class
						 * @see test.copy.VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)
						 * @see test.copy.VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)
						 * @see test.copy.VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)
						 * @see test.copy.VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)
						 * @see test.copy.VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)
						 * @see test.copy.VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPublic#vm_private() Invalid ref: non visible method in visible class
					                                  ^^^^^^^^^^
				Javadoc: The method vm_private() from the type VisibilityPublic is not visible
				----------
				2. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPublic.VpPrivate#unknown() Invalid ref: non visible inner class (non existent method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				3. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPublic.VpPrivate#vm_private() Invalid ref: non visible inner class in visible class and (non visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				4. ERROR in test\\X.java (at line 10)
					* @see test.copy.VisibilityPublic.VpPrivate#vm_private(boolean, String) Invalid ref: non visible inner class in visible class (non applicable method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				5. ERROR in test\\X.java (at line 11)
					* @see test.copy.VisibilityPublic.VpPrivate#vm_public() Invalid ref: non visible inner class in visible class (visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				6. ERROR in test\\X.java (at line 12)
					* @see test.copy.VisibilityPublic.VpPrivate#vm_public(Object, float) Invalid ref: non visible inner class in visible class (non applicable visible method)
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: The type test.copy.VisibilityPublic.VpPrivate is not visible
				----------
				7. ERROR in test\\X.java (at line 13)
					* @see test.copy.VisibilityPublic.VpPublic#vm_private() Invalid ref: non visible method in visible inner class
					                                           ^^^^^^^^^^
				Javadoc: The method vm_private() from the type VisibilityPublic.VpPublic is not visible
				----------
				""");
	}

	public void test162() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-applicable)
						 *\s
						 * @see test.copy.VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class
						 * @see test.copy.VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @see test.copy.VisibilityPublic#vm_private(boolean) Invalid ref: non applicable method in visible class
					                                  ^^^^^^^^^^
				Javadoc: The method vm_private() in the type VisibilityPublic is not applicable for the arguments (boolean)
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPublic#vm_public(long,long,long,int) Invalid ref: non applicable method in visible class
					                                  ^^^^^^^^^
				Javadoc: The method vm_public() in the type VisibilityPublic is not applicable for the arguments (long, long, long, int)
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPublic.VpPublic#vm_private(boolean, String) Invalid ref: non applicable method in visible inner class
					                                           ^^^^^^^^^^
				Javadoc: The method vm_private() in the type VisibilityPublic.VpPublic is not applicable for the arguments (boolean, String)
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPublic.VpPublic#vm_public(Object, float) Invalid ref: visible inner class (non applicable visible method)
					                                           ^^^^^^^^^
				Javadoc: The method vm_public() in the type VisibilityPublic.VpPublic is not applicable for the arguments (Object, float)
				----------
				""");
	}

	public void test163() {
		this.runNegativeReferenceTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import test.copy.VisibilityPublic;
					public class X {
						/**
						 * Invalid other package visible class methods references (non-existent)
						 *\s
						 * @see test.copy.VisibilityPublic#vm_private("") Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPublic#vm_public(\""") Invalid ref: invalid argument definition
						 * @see test.copy.VisibilityPublic.VpPrivate#vm_private(String d()) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPublic.VpPrivate#vm_public([) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPublic.VpPublic#vm_private([]) Invalid ref: invalid argument declaration
						 * @see test.copy.VisibilityPublic.VpPublic#vm_public(char[], int[],]) Invalid ref: invalid argument declaration
						 */
						public void s_foo() {
						}
					}
					""" },
			"""
				----------
				1. WARNING in test\\X.java (at line 2)
					import test.copy.VisibilityPublic;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				The import test.copy.VisibilityPublic is never used
				----------
				2. ERROR in test\\X.java (at line 7)
					* @see test.copy.VisibilityPublic#vm_private("") Invalid ref: invalid argument declaration
					                                            ^^^
				Javadoc: Invalid parameters declaration
				----------
				3. ERROR in test\\X.java (at line 8)
					* @see test.copy.VisibilityPublic#vm_public(\""") Invalid ref: invalid argument definition
					                                           ^^^
				Javadoc: Invalid parameters declaration
				----------
				4. ERROR in test\\X.java (at line 9)
					* @see test.copy.VisibilityPublic.VpPrivate#vm_private(String d()) Invalid ref: invalid argument declaration
					                                                      ^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				5. ERROR in test\\X.java (at line 10)
					* @see test.copy.VisibilityPublic.VpPrivate#vm_public([) Invalid ref: invalid argument declaration
					                                                     ^^
				Javadoc: Invalid parameters declaration
				----------
				6. ERROR in test\\X.java (at line 11)
					* @see test.copy.VisibilityPublic.VpPublic#vm_private([]) Invalid ref: invalid argument declaration
					                                                     ^^
				Javadoc: Invalid parameters declaration
				----------
				7. ERROR in test\\X.java (at line 12)
					* @see test.copy.VisibilityPublic.VpPublic#vm_public(char[], int[],]) Invalid ref: invalid argument declaration
					                                                    ^^^^^^^^^^^^^^^^
				Javadoc: Invalid parameters declaration
				----------
				""");
	}

	public void test164() {
		this.runNegativeReferenceTest(
			new String[] {
				"X.java",
				"""
					package test;
					public class X {
						/**
						 * Invalid param and throws tags
						 *\s
						 * @param
						 * @throws
						 */
						public void s_foo(int a) throws Exception {
						}
					}
					""" },
					"""
						----------
						1. ERROR in X.java (at line 6)
							* @param
							   ^^^^^
						Javadoc: Missing parameter name
						----------
						2. ERROR in X.java (at line 7)
							* @throws
							   ^^^^^^
						Javadoc: Missing class name
						----------
						3. ERROR in X.java (at line 9)
							public void s_foo(int a) throws Exception {
							                      ^
						Javadoc: Missing tag for parameter a
						----------
						4. ERROR in X.java (at line 9)
							public void s_foo(int a) throws Exception {
							                                ^^^^^^^^^
						Javadoc: Missing tag for declared exception Exception
						----------
						""");
	}
}
