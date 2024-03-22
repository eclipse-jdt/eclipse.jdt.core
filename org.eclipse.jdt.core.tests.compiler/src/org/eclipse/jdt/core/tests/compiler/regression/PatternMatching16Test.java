/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class PatternMatching16Test extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 16 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test027" };
	}

	public static Class<?> testClass() {
		return PatternMatching16Test.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public PatternMatching16Test(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()
				&& preview) {
			defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE16Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test000a() {
		Map<String, String> options = getCompilerOptions(false);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		runNegativeTest(
				new String[] {
						"X1.java",
						"""
							public class X1 {
							  public void foo(Object obj) {
									if (obj instanceof String s) {
									}
							 \
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X1.java (at line 3)
						if (obj instanceof String s) {
						                   ^^^^^^^^
					The Java feature 'Pattern Matching in instanceof Expressions' is only available with source level 16 and above
					----------
					""",
				null,
				true,
				options);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
	}
	public void test000b() {
		if (this.complianceLevel < ClassFileConstants.getLatestJDKLevel())
			return;
		Map<String, String> options = getCompilerOptions(true);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
		runNegativeTest(
				new String[] {
						"X1.java",
						"""
							public class X1 {
							  public void foo(Object obj) {
									if (obj instanceof String s) {
									}
							 \
								}
							}
							""",
				},
				"----------\n" +
				"1. ERROR in X1.java (at line 0)\n" +
				"	public class X1 {\n" +
				"	^\n" +
				"Preview features enabled at an invalid source release level 14, preview can be enabled only at source level "+PREVIEW_ALLOWED_LEVEL+"\n" +
				"----------\n",
				null,
				true,
				options);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
	}
	// No longer negative since pattern matching is a standard feature now.
	public void test001() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X1.java",
						"""
							public class X1 {
							  public void foo(Object obj) {
									if (obj instanceof String s) {
									}
							 \
								}
							  public static void main(String[] obj) {
								}
							}
							""",
				},
				"",
				options);
	}
	public void test002() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X2.java",
						"""
							@SuppressWarnings("preview")
							public class X2 {
							  public void foo(Integer obj) {
									if (obj instanceof String s) {
									}
							 \
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X2.java (at line 4)
						if (obj instanceof String s) {
						    ^^^^^^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types Integer and String
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test003() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X3.java",
						"""
							@SuppressWarnings("preview")
							public class X3 {
							  public void foo(Number num) {
									if (num instanceof Integer s) {
									} else if (num instanceof String) {
									}
							 \
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X3.java (at line 5)
						} else if (num instanceof String) {
						           ^^^^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types Number and String
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test003a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X3.java",
						"""
							@SuppressWarnings("preview")
							public class X3 {
							  public void foo(Number num) {
									if (num instanceof int) {
									}
							 \
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X3.java (at line 4)
						if (num instanceof int) {
						    ^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types Number and int
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test004() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X4.java",
						"""
							@SuppressWarnings("preview")
							public class X4 {
							  public void foo(Object obj) {
									String s = null;
									if (obj instanceof Integer s) {
									} else if (obj instanceof String) {
									}
							 \
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X4.java (at line 5)
						if (obj instanceof Integer s) {
						                           ^
					Duplicate local variable s
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test005() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X5.java",
						"""
							@SuppressWarnings("preview")
							public class X5 {
							@SuppressWarnings("preview")
							  public static void foo(Object obj) {
									if (obj instanceof Integer i) {
										System.out.print(i);
									} else if (obj instanceof String s) {
										System.out.print(s);
									}
							 \
								}
							  public static void main(String[] obj) {
									foo(100);
								}
							}
							""",
				},
				"100",
				options);
	}
	public void test006() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X6.java",
						"""
							@SuppressWarnings("preview")
							public class X6 {
							  public static void foo(Object obj) {
									if (obj instanceof Integer i) {
										System.out.print(i);
									} else if (obj instanceof String s) {
										System.out.print(s);
									}
							 \
								}
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"abcd",
				options);
	}
	public void test006a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6a.java",
						"""
							@SuppressWarnings("preview")
							public class X6a {
								public static void foo(Object obj) {
									if (obj != null) {
										if (obj instanceof Integer i) {
											System.out.print(i);
										} else if (obj instanceof String s) {
											System.out.print(i);
										}
							 \
									}
							 \
									System.out.print(i);
								}
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X6a.java (at line 8)
						System.out.print(i);
						                 ^
					i cannot be resolved to a variable
					----------
					2. ERROR in X6a.java (at line 11)
						System.out.print(i);
						                 ^
					i cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test006b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6b.java",
						"""
							@SuppressWarnings("preview")
							public class X6b {
								public static void foo(Object obj) {
									if (obj != null) {
										if (obj instanceof Integer i) {
											System.out.print(i);
										} else if (obj instanceof String s) {
											System.out.print(i);
										}
							 \
									}
							 \
									System.out.print(s);
								}
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X6b.java (at line 8)
						System.out.print(i);
						                 ^
					i cannot be resolved to a variable
					----------
					2. ERROR in X6b.java (at line 11)
						System.out.print(s);
						                 ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test006c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6c.java",
						"""
							@SuppressWarnings("preview")
							public class X6c {
							  public static void foo(Object obj) {
									if (obj instanceof Integer i) {
										System.out.print(i);
									} else if (obj instanceof String s) {
										System.out.print(i);
									}
							 \
								}
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X6c.java (at line 7)
						System.out.print(i);
						                 ^
					i cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test006d() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6d.java",
						"""
							@SuppressWarnings("preview")
							public class X6d {
							  public static void foo(Object obj) {
									if (obj instanceof Integer i) {
										System.out.print(i);
									} else {
										System.out.print(i);
									}
							 \
								}
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X6d.java (at line 7)
						System.out.print(i);
						                 ^
					i cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test007() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X7.java",
						"""
							@SuppressWarnings("preview")
							public class X7 {
							  public static void foo(Object obj) {
									if (obj instanceof Integer i) {
										System.out.print(i);
									} else if (obj instanceof String s) {
										System.out.print(i);
									}
							 \
								}
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X7.java (at line 7)
						System.out.print(i);
						                 ^
					i cannot be resolved to a variable
					----------
					""",
				"""
					X7.java:4: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.
							if (obj instanceof Integer i) {
							                           ^
					X7.java:6: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.
							} else if (obj instanceof String s) {
							                                 ^
					X7.java:7: error: cannot find symbol
								System.out.print(i);
								                 ^
					  symbol:   variable i
					  location: class X7
					1 error
					2 warnings""",
				null,
				true,
				options);
	}
	public void test008() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X8.java",
						"""
							@SuppressWarnings("preview")
							public class X8 {
							  	public static void foo(Object b) {
									Object c = null;
									if (b != c) {
										if ((b instanceof String s) && (s.length() != 0))
											System.out.println("s:" + s);
										else\s
											System.out.println("b:" + b);
									}
								}\
							  public static void main(String[] obj) {
									foo(100);
									foo("abcd");
								}
							}
							""",
				},
				"b:100\n" +
				"s:abcd",
				options);
	}
	public void test009() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X9.java",
						"""
							@SuppressWarnings("preview")
							public class X9 {
							  	public static void foo(Object b) {
									Object c = null;
									if (b != c) {
										if ((b instanceof String s) && (s.length() != 0))
											System.out.println("s:" + s);
										else if ((b instanceof Integer i2))
											System.out.println("i2:" + i2);
									}
								}\
							  public static void main(String[] obj) {
									foo(100);
									foo("abcd");
								}
							}
							""",
				},
				"i2:100\n" +
				"s:abcd",
				options);
	}
	public void test010() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X10.java",
						"""
							@SuppressWarnings("preview")
							public class X10 {
							  	public static void foo(Object b) {
									Object c = null;
									if (b != c) {
										if (b != null && (b instanceof String s))
											System.out.println("s:" + s);
										else \
											System.out.println("b:" + b);
									}
								}\
							  public static void main(String[] obj) {
									foo(100);
									foo("abcd");
								}
							}
							""",
				},
				"b:100\n" +
				"s:abcd",
				options);
	}
	public void test011() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X11.java",
						"""
							@SuppressWarnings("preview")
							public class X11 {
							  	public static void foo(Object b) {
									Object c = null;
									if (b == null && (b instanceof String s)) {
									} else {\
									}
									System.out.println(s);
								}\
							  public static void main(String[] obj) {
									foo(100);
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X11.java (at line 7)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test012() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X12.java",
						"""
							@SuppressWarnings("preview")
							public class X12 {
							  	public static void foo(Object b) {
									Object c = new Object();
									if (b != c) {
										if (b == null && (b instanceof String s)) {
											System.out.println("s:" + s);
										} else {
											System.out.println("b:" + b);
										}
										s = null;
									}
								}\
							  public static void main(String[] obj) {
									foo(100);
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X12.java (at line 11)
						s = null;
						^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test013() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X13.java",
						"""
							@SuppressWarnings("preview")
							public class X13 {
							  	public static void foo(Object b) {
									Object c = null;
									if (b != c) {
										if (b == null && (b instanceof String s))
											System.out.println("s:" + s);
										else \
											System.out.println("b:" + b);
										System.out.println(s);
									}
								}\
							  public static void main(String[] obj) {
									foo(100);
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X13.java (at line 9)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test014() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14.java",
						"""
							@SuppressWarnings("preview")
							public class X14 {
							  	public static void foo(Object o) {
									if (!(o instanceof String s)) {
										System.out.print("then:" + s);
									} else {
										System.out.print("else:" + s);
									}
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X14.java (at line 5)
						System.out.print("then:" + s);
						                           ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test014a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14a.java",
						"""
							@SuppressWarnings("preview")
							public class X14a {
							  	public static void foo(Object o) {
									if (!(o instanceof String s)) {
										System.out.print("then:" + s);
									} else {
										System.out.print("else:" + s);
									}
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X14a.java (at line 5)
						System.out.print("then:" + s);
						                           ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test014b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14b.java",
						"""
							@SuppressWarnings("preview")
							public class X14b {
							  	public static void foo(Object o) {
									if (!!(o instanceof String s)) {
										System.out.print("then:" + s);
									} else {
										System.out.print("else:" + s);
									}
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X14b.java (at line 7)
						System.out.print("else:" + s);
						                           ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test014c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14c.java",
						"""
							@SuppressWarnings("preview")
							public class X14c {
							  	public static void foo(Object o) {
									if (o == null) {
										System.out.print("null");
									} else if(!(o instanceof String s)) {
										System.out.print("else:" + s);
									}
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X14c.java (at line 7)
						System.out.print("else:" + s);
						                           ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test014d() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14d.java",
						"""
							@SuppressWarnings("preview")
							public class X14d {
							  	public static void foo(Object o) {
									if (o == null) {
										System.out.print("null");
									} else if(!!(o instanceof String s)) {
										System.out.print("else:" + s);
									}
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"else:abcd",
				options);
	}
	public void test014e() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14a.java",
						"""
							@SuppressWarnings("preview")
							public class X14a {
							  	public static void foo(Object o) {
									 if (!(!(o instanceof String s))) {
										System.out.print("s:" + s);
									} else {
									}
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"s:abcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if doesn't complete
	 * normally, then the variable is available beyond the if statement
	 */
	public void test015() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X15.java",
						"""
							@SuppressWarnings("preview")
							public class X15 {
							  	public static void foo(Object o) {
									if (!(o instanceof String s)) {
										throw new IllegalArgumentException();
									} else {
										System.out.print("s:" + s);
									}
									System.out.print(s);
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"s:abcdabcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if doesn't complete
	 * normally, then the variable is available beyond the if statement
	 */
	public void test015a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X15a.java",
						"""
							@SuppressWarnings("preview")
							public class X15a {
							  	public static void foo(Object o) {
									if (!(o instanceof String s)) {
										throw new IllegalArgumentException();
									}
									System.out.print(s);
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"abcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if completes
	 * normally, then the variable is not available beyond the if statement
	 */
	public void test015b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X15b.java",
						"""
							@SuppressWarnings("preview")
							public class X15b {
							  	public static void foo(Object o) {
									if (!(o instanceof String s)) {
										//throw new IllegalArgumentException();
									} else {
										System.out.print("s:" + s);
									}
									System.out.print(s);
								}\
							  public static void main(String[] obj) {
									foo("abcd");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X15b.java (at line 9)
						System.out.print(s);
						                 ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test016() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X16.java",
						"""
							@SuppressWarnings("preview")
							public class X16 {
							  	public static void foo(Object o) {
									boolean b = (o instanceof String[] s && s.length == 1);
									System.out.print(b);
								}\
							  public static void main(String[] obj) {
									foo(new String[]{"one"});
								}
							}
							""",
				},
				"true",
				options);
	}
	public void test017() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X17.java",
						"""
							@SuppressWarnings("preview")
							public class X17 {
							  	public static void foo(Object o) {
									boolean b = (o instanceof String[] s && s.length == 1);
									System.out.print(s[0]);
								}\
							  public static void main(String[] obj) {
									foo(new String[]{"one"});
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X17.java (at line 5)
						System.out.print(s[0]);
						                 ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/* Test that the scopes of pattern variable in a block doesn't affect
	 * another outside but declared after the block
	 */
	public void test018() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X18.java",
						"""
							@SuppressWarnings("preview")
							public class X18 {
							  public static void main(String[] obj) {
							  		foo(obj);
							  }
							  public static void foo(Object[] obj) {
									boolean a = true;
									{
										boolean b = (obj instanceof String[] s && s.length == 0);
										System.out.print(b + ",");
									}
									boolean b = a ? false : (obj instanceof String[] s && s.length == 0);
									System.out.print(b);
								}
							}
							""",
				},
				"true,false",
				options);
	}
	/* Test that the scopes of pattern variable in a block doesn't affect
	 * another outside but declared before the block
	 */
	public void test019() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X19.java",
						"""
							@SuppressWarnings("preview")
							public class X19 {
							  public static void main(String[] obj) {
							  		foo(obj);
							  }
							  public static void foo(Object[] obj) {
									boolean a = true;
									boolean b = a ? false : (obj instanceof String[] s && s.length == 0);
									System.out.print(b + ",");
									{
										b = (obj instanceof String[] s && s.length == 0);
										System.out.print(b);
									}
								}
							}
							""",
				},
				"false,true",
				options);
	}
	/* Test that we still detect duplicate pattern variable declarations
	 */
	public void test019b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X19b.java",
						"""
							@SuppressWarnings("preview")
							public class X19b {
							  public static void main(String[] obj) {
							  		foo(obj);
							  }
							  public static void foo(Object[] obj) {
									boolean a = true;
									if (obj instanceof String[] s && s.length == 0) {
										boolean b = (obj instanceof String[] s && s.length == 0);
										System.out.print(b);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X19b.java (at line 9)
						boolean b = (obj instanceof String[] s && s.length == 0);
						                                     ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/* Test that we report subtypes of pattern variables used in the same stmt
	 * As of Java 19, we no longer report error for the above
	 */
	public void test020() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X20.java",
						"""
							public class X20 {
							  public static void main(String[] obj) {
							  		foo(obj);
							  }
							  public static void foo(Object[] o) {
									boolean b = (o instanceof String[] s) && s instanceof CharSequence[] s2;
									System.out.print(b1);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X20.java (at line 7)
						System.out.print(b1);
						                 ^^
					b1 cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test020a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X20.java",
						"""
							@SuppressWarnings("preview")
							public class X20 {
							  public static void main(String[] obj) {
							  		foo(obj);
							  }
							  public static void foo(Object[] o) {
									boolean b = (o instanceof CharSequence[] s) && s instanceof String[] s2;
									System.out.print(b);
								}
							}
							""",
				},
				"true",
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test021() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X21.java",
						"""
							@SuppressWarnings("preview")
							public class X21 {
							  public static void main(String[] obj) {
							  		foo(obj);
							  }
							  public static void foo(Object[] o) {
									boolean b = (o instanceof CharSequence[] s) && s instanceof String[] s2;
									System.out.print(s);
									System.out.print(s2);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X21.java (at line 8)
						System.out.print(s);
						                 ^
					s cannot be resolved to a variable
					----------
					2. ERROR in X21.java (at line 9)
						System.out.print(s2);
						                 ^^
					s2 cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/* Test that we allow pattern expressions in a while statement
	 */
	public void test022() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X22.java",
						"""
							@SuppressWarnings("preview")
							public class X22 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									while ((o instanceof String s) && s.length() > 0) {
										o = s.substring(0, s.length() - 1);
										System.out.println(s);
									}
								}
							}
							""",
				},
				"one\non\no",
				options);
	}
	public void test022a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X22a.java",
						"""
							@SuppressWarnings("preview")
							public class X22a {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									do {
										o = s.substring(0, s.length() - 1);
										System.out.println(s);
									} while ((o instanceof String s) && s.length() > 0);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X22a.java (at line 8)
						o = s.substring(0, s.length() - 1);
						    ^
					s cannot be resolved
					----------
					2. ERROR in X22a.java (at line 8)
						o = s.substring(0, s.length() - 1);
						                   ^
					s cannot be resolved
					----------
					3. ERROR in X22a.java (at line 9)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				null,
				true,
				options);
	}
	public void test022b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X22b.java",
						"""
							@SuppressWarnings("preview")
							public class X22b {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									do {
										// nothing
									} while ((o instanceof String s));
									System.out.println(s);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X22b.java (at line 10)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				null,
				true,
				options);
	}
	public void test022c() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X22c.java",
						"""
							@SuppressWarnings("preview")
							public class X22c {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									do {
										// nothing
									} while (!(o instanceof String s));
									System.out.println(s);
								}
							}
							""",
				},
				"one",
				options);
	}
	/* Test pattern expressions in a while statement with break
	 */
	public void test023() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23.java",
						"""
							@SuppressWarnings("preview")
							public class X23 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									while (!(o instanceof String s) && s.length() > 0) {
										System.out.println(s);
										break;
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X23.java (at line 7)
						while (!(o instanceof String s) && s.length() > 0) {
						                                   ^
					s cannot be resolved
					----------
					2. ERROR in X23.java (at line 8)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test023a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23a.java",
						"""
							@SuppressWarnings("preview")
							public class X23a {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									do {
										System.out.println(s);
										break;
									} while (!(o instanceof String s) && s.length() > 0);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X23a.java (at line 8)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					2. ERROR in X23a.java (at line 10)
						} while (!(o instanceof String s) && s.length() > 0);
						                                     ^
					s cannot be resolved
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/* Test pattern expressions in a while statement with no break
	 */
	public void test023b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23b.java",
						"""
							@SuppressWarnings("preview")
							public class X23b {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									while (!(o instanceof String s) && s.length() > 0) {
										System.out.println(s);
										//break;
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X23b.java (at line 7)
						while (!(o instanceof String s) && s.length() > 0) {
						                                   ^
					s cannot be resolved
					----------
					2. ERROR in X23b.java (at line 8)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	// Same as above but with do while
	public void test023c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23c.java",
						"""
							@SuppressWarnings("preview")
							public class X23c {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									do {
										System.out.println(s);
										//break;
									}while (!(o instanceof String s) && s.length() > 0);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X23c.java (at line 8)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					2. ERROR in X23c.java (at line 10)
						}while (!(o instanceof String s) && s.length() > 0);
						                                    ^
					s cannot be resolved
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test024a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X24a.java",
						"""
							@SuppressWarnings("preview")
							public class X24a {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									while (!(o instanceof String s)) {
										throw new IllegalArgumentException();
									}
									System.out.println(s);
								}
							}
							""",
				},
				"one",
				options);
	}
	public void test024b() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X24a.java",
						"""
							@SuppressWarnings("preview")
							public class X24a {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									for (;!(o instanceof String s);) {
										 throw new IllegalArgumentException();
									}
									System.out.println(s);
								}
							}
							""",
				},
				"one",
				options);
	}
	/*
	 * It's not a problem to define the same var in two operands of a binary expression,
	 * but then it is not in scope below.
	 */
	public void test025() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X25.java",
						"""
							@SuppressWarnings("preview")
							public class X25 {
							  public static void main(String[] o) {
									foo("one", "two");
								}
							  public static void foo(Object o, Object p) {
									if ((o instanceof String s) != p instanceof String s) {
										System.out.print("s:" + s);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X25.java (at line 8)
						System.out.print("s:" + s);
						                        ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test025a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X25.java",
						"""
							@SuppressWarnings("preview")
							public class X25 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									if ( (o instanceof String a) || (! (o instanceof String a)) ) {
										System.out.print("a:" + a);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X25.java (at line 8)
						System.out.print("a:" + a);
						                        ^
					a cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test025b() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X25.java",
						"""
							@SuppressWarnings("preview")
							public class X25 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object o) {
									if ( (o instanceof String a) || (! (o instanceof String a)) ) {
										System.out.println("none");
									} else {
										System.out.print("a:" + a);
									}
								}
							}
							""",
				},
				"none",
				options);
	}
	public void test025c() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X25.java",
						"""
							@SuppressWarnings("preview")
							public class X25 {
							  public static void main(String[] o) {
									foo("one", new Integer(0));
								}
							  public static void foo(Object o, Object p) {
									if ( (o instanceof String a) || (! (p instanceof String a)) ) {
										System.out.println("none");
									} else {
										System.out.print("a:" + a);
									}
								}
							}
							""",
				},
				"none",
				options);
	}
	/*
	 * It's not allowed to have two pattern variables with same name in the
	 * same scope
	 */
	public void test026() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"""
							@SuppressWarnings("preview")
							public class X26 {
							  public static void main(String[] o) {
									foo("one", "two");
								}
							  public static void foo(Object o, Object p) {
									if ((o instanceof String s) && (p instanceof String s)) {
										System.out.print("s:" + s);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X26.java (at line 7)
						if ((o instanceof String s) && (p instanceof String s)) {
						                                                    ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/*
	 * It's not allowed to have two pattern variables with same name in the
	 * same scope
	 */
	public void test026a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"""
							@SuppressWarnings("preview")
							public class X26 {
							  public static void main(String[] o) {
									foo("one", "two");
								}
							  public static void foo(Object o, Object p) {
									if ((o instanceof String s) && (!(o instanceof String s))) {
										System.out.print("s:" + s);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X26.java (at line 7)
						if ((o instanceof String s) && (!(o instanceof String s))) {
						                                                      ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				"",
				null,
				true,
				options);
	}
	/*
	 * It's not a problem to define the same var in two operands of a binary expression,
	 * but then it is not in scope below.
	 */
	public void test026b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"""
							@SuppressWarnings("preview")
							public class X26 {
							  public static void main(String[] o) {
									foo("one", "two");
								}
							  public static void foo(Object o, Object p) {
									if ((o instanceof String s) == p instanceof String s) {
										System.out.print("s:" + s);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X26.java (at line 8)
						System.out.print("s:" + s);
						                        ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				options);
	}
	public void test027() {
		runConformTest(
				new String[] {
						"X27.java",
						"""
							@SuppressWarnings("preview")
							public class X27 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {
										System.out.println(s[i]);
									}
								}
							}
							""",
				},
				"",
				getCompilerOptions(true));
	}
	public void test028() {
		runConformTest(
				new String[] {
						"X28.java",
						"""
							@SuppressWarnings("preview")
							public class X28 {
							  public static void main(String[] o) {
									foo(new String[] {"one", "two"});
								}
							  public static void foo(Object obj) {
									for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {
										System.out.println(s[i]);
									}
								}
							}
							""",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	public void test029() {
		runConformTest(
				new String[] {
						"X29.java",
						"""
							@SuppressWarnings("preview")
							public class X29 {
							  public static void main(String[] o) {
									foo(new String[] {"one", "two"});
								}
							  public static void foo(Object obj) {
									for(int i = 0; (obj instanceof String[] s) && s.length > 0 && i < s.length; i = (s != null ? i + 1 : i)) {
										System.out.println(s[i]);
									}
								}
							}
							""",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	/*
	 * Test that pattern variables are accepted in initialization of a for statement,
	 * but unavailable in the body if uncertain which if instanceof check was true
	 */
	public void test030() {
		runNegativeTest(
				new String[] {
						"X30.java",
						"""
							@SuppressWarnings("preview")
							public class X30 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									for(int i = 0, length = (obj instanceof String s) ? s.length() : 0; i < length; i++) {
										System.out.print(s.charAt(i));
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X30.java (at line 8)
						System.out.print(s.charAt(i));
						                 ^
					s cannot be resolved
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test031() {
		runNegativeTest(
				new String[] {
						"X31.java",
						"""
							@SuppressWarnings("preview")
							public class X31 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {
										System.out.println(s[i]);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X31.java (at line 7)
						for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {
						                                               ^
					s cannot be resolved to a variable
					----------
					2. ERROR in X31.java (at line 7)
						for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {
						                                                                   ^
					s cannot be resolved to a variable
					----------
					3. ERROR in X31.java (at line 8)
						System.out.println(s[i]);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test032() {
		runConformTest(
				new String[] {
						"X32.java",
						"""
							@SuppressWarnings("preview")
							public class X32 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									String res = null;
									int i = 0;
									switch(i) {
									case 0:
										res = (obj instanceof String) ? null : null;
									default:
										break;
									}
									System.out.println(res);
								}
							}
							""",
				},
				"null",
				getCompilerOptions(true));
	}
	public void test032a() {
		runConformTest(
				new String[] {
						"X32.java",
						"""
							@SuppressWarnings("preview")
							public class X32 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									String res = null;
									int i = 0;
									switch(i) {
									case 0:
										res = (obj instanceof String s) ? s : null;
									default:
										break;
									}
									System.out.println(res);
								}
							}
							""",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test033() {
		runNegativeTest(
				new String[] {
						"X33.java",
						"""
							@SuppressWarnings("preview")
							public class X33 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									String res = null;
									int i = 0;
									switch(i) {
									case 0:
										res = (obj instanceof String s) ? s : null;
										res = s.substring(1);
									default:
										break;
									}
									System.out.println(res);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X33.java (at line 12)
						res = s.substring(1);
						      ^
					s cannot be resolved
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test034() {
		runNegativeTest(
				new String[] {
						"X34.java",
						"""
							@SuppressWarnings("preview")
							public class X34 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									int i = 0;
									String result = switch(i) {
										case 0 -> {
											result = (obj instanceof String s) ? s : null;
											yield result;
										}
										default -> {
											yield result;
										}
									};
									System.out.println(result);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X34.java (at line 14)
						yield result;
						      ^^^^^^
					The local variable result may not have been initialized
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test035() {
		runNegativeTest(
				new String[] {
						"X35.java",
						"""
							@SuppressWarnings("preview")
							public class X35 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									int i = 0;
									String result = switch(i) {
										case 0 -> {
											result = (obj instanceof String s) ? s : null;
											yield s;
										}
										default -> {
											yield s;
										}
									};
									System.out.println(result);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X35.java (at line 11)
						yield s;
						      ^
					s cannot be resolved to a variable
					----------
					2. ERROR in X35.java (at line 14)
						yield s;
						      ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test036() {
		runConformTest(
				new String[] {
						"X36.java",
						"""
							@SuppressWarnings("preview")
							public class X36 {
							  public static void main(String[] o) {
									foo("one");
								}
							  public static void foo(Object obj) {
									int i = 0;
									String result = switch(i) {
										default -> {
											result = (obj instanceof String s) ? s : null;
											yield result;
										}
									};
									System.out.println(result);
								}
							}
							""",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test037() {
		runNegativeTest(
				new String[] {
						"X37.java",
						"""
							@SuppressWarnings("preview")
							public class X37 {
							  public static void main(String[] o) {
									foo(new String[] {"abcd"});
								}
							  public static void foo(Object[] obj) {
									for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ; i++) {
										System.out.println(s[i]);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X37.java (at line 8)
						System.out.println(s[i]);
						                   ^^^^
					The type of the expression must be an array type but it resolved to String
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test038() {
		runNegativeTest(
				new String[] {
						"X38.java",
						"""
							@SuppressWarnings("preview")
							public class X38 {
							  public static void main(String[] o) {
									foo(new String[] {"abcd"});
								}
							  public static void foo(Object[] obj) {
									for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ;) {
										throw new IllegalArgumentException();
									}
									System.out.println(s);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X38.java (at line 10)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test039() {
		runConformTest(
				new String[] {
						"X39.java",
						"""
							@SuppressWarnings("preview")
							public class X39 {
							  public static void main(String[] o) {
									foo(new String[] {"one"});;
								}
							  public static void foo(Object[] obj) {
									for(int i = 0; i < obj.length && (obj[i] instanceof String s) && i < s.length(); i++) {
										System.out.println(s);
									}
								}
							}
							""",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test040() {
		runConformTest(
				new String[] {
						"X40.java",
						"""
							@SuppressWarnings("preview")
							public class X40 {
								String a;
							    Object o1 = "x";
							    public static void main(String argv[]) {
							        System.out.println(new X40().foo());
							    }
							    public String foo() {
							        String res = "";
							    	 Object o2 = "x";
							        if (o1 instanceof String s) {\s
							            res = "then_" + s;
							        } else {
							            res = "else_";
							        }
							        return res;
							    }
							}
							""",
				},
				"then_x",
				getCompilerOptions(true));
	}
	public void test041() {
		runConformTest(
				new String[] {
						"X41.java",
						"""
							@SuppressWarnings("preview")
							public class X41 {
								String a;
							    Object o1 = "x";
							    public static void main(String argv[]) {
							        System.out.println(new X41().foo());
							    }
							    public String foo() {
							        String res = "";
							        Object o2 = "x";
							        if ( !(o1 instanceof String s) || !o1.equals(s) ) {\s
							            res = "then_";
							        } else {
							            res = "else_" + s;
							        }
							        return res;
							    }
							}
							""",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void test042() {
		runConformTest(
				new String[] {
						"X42.java",
						"""
							@SuppressWarnings("preview")
							public class X42 {
								 String a;
							    Object o1 = "x";
							    public static void main(String argv[]) {
							        System.out.println(new X42().foo());
							    }
							    public String foo() {
							        String res = "";
							        Object o2 = o1;
							        if ( !(o1 instanceof String s) || !o1.equals(s) ) {\s
							            res = "then_";
							        } else {
							            res = "else_" + s;
							        }
							        return res;
							    }
							}
							""",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void test043() {
		runConformTest(
				new String[] {
						"X43.java",
						"""
							@SuppressWarnings("preview")
							public class X43 {
								 public static void main(String argv[]) {
									System.out.println(new X43().foo("foo", "test"));
								}
								public boolean foo(Object obj, String s) {
									class Inner {
										public boolean foo(Object obj) {
											if (obj instanceof String s) {
												// s is shadowed now
												if (!"foo".equals(s))
													return false;
											}
											// s is not shadowed
											return "test".equals(s);
										}
									}
									return new Inner().foo(obj);
								}
							}
							""",
				},
				"true",
				getCompilerOptions(true));
	}
	public void test044() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X44.java",
						"""
							@SuppressWarnings("preview")
							class Inner<T> {
							    public boolean foo(Object obj) {
							        if (obj instanceof Inner<?> p) {
							            return true;
							        }
							        return false;
							    }
							}\s
							public class X44  {
							    public static void main(String argv[]) {
							    	Inner<String> param = new Inner<>();
							    	System.out.println(new Inner<String>().foo(param));
							    }
							}
							""",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test045() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X45.java",
						"""
							@SuppressWarnings("preview")
							public class X45 {
							    Object s = "test";
							    boolean result = s instanceof String s1;
								 public static void main(String argv[]) {
							    	System.out.println("true");
							    }
							}
							""",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test046() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X46.java",
						"""
							@SuppressWarnings("preview")
							public class X46 {
							    Object s = "test";
							    boolean result = (s instanceof String s1 && s1 != null);
								 public static void main(String argv[]) {
							    	System.out.println("true");
							    }
							}
							""",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test047() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"""
							public class InstanceOfPatternTest {
								public static void main(String[] args) {
									if (getChars() instanceof String s) {
										System.out.println(s);
									}
								}
								static CharSequence getChars() {
									return "xyz";
								}
							}
							""",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test048() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"""
							public class InstanceOfPatternTest {
								public static void main(String[] args) {
									if (getChars() instanceof String s) {
										System.out.println(s);
									}
								}
								static CharSequence getChars() {
									return "xyz";
								}
							}
							""",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test049() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"""
							public class InstanceOfPatternTest {
								public static void main(String[] args) {
									if ( ((CharSequence) getChars()) instanceof String s) {
										System.out.println(s);
									}
								}
								static Object getChars() {
									return "xyz";
								}
							}
							""",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test050() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runNegativeTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"""
							@SuppressWarnings("preview")
							public class InstanceOfPatternTest {
								public static void main(String[] args) {
									if ( ((s) -> {return s;}) instanceof I s) {
										System.out.println(s);
									}
								}
							}\s
							interface I {
								public String foo(String s);
							}
							""",
				},
				"""
					----------
					1. ERROR in InstanceOfPatternTest.java (at line 4)
						if ( ((s) -> {return s;}) instanceof I s) {
						     ^^^^^^^
					The target type of this expression must be a functional interface
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test051() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"""
							public class InstanceOfPatternTest {
								static String STR = "2";
								public static void main(String[] args) {
									if ( switch(STR) {
											case "1" -> (CharSequence) "one";
											default -> (CharSequence) "Unknown";
										  }\s
											instanceof String s) {
										System.out.println(s);
									}
								}
							}
							""",
				},
				"Unknown",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test052() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public static void main(String args[]) {
									String result = null;
									Object obj = "abc";
									int i = switch (0) {
										case 1 -> {
											yield 1;
										}
										default -> {
											for (int j = 0; !(obj instanceof String s);) {
												obj = null;
											}
											result = s;
											System.out.println(result);
											yield 2;
										}
									};
									System.out.println(i);
								}
							}
							""",
				},
				"abc\n" +
				"2",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void testBug562392a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X<T> {
								public boolean foo(T obj) {
									if (obj instanceof String s) {
										System.out.println(s);
									}
									return true;
								}
								public static void main(String argv[]) {
									String s = "x";
									System.out.println(new X<Object>().foo(s));
								}
							}
							""",
				},
				"x\n" +
				"true",
				compilerOptions);
		}
	public void testBug562392b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X<T> {
								public boolean foo(Object obj) {
							        if (obj instanceof T) {
							            return false;
							        }
							        return true;
							    }
								public static void main(String argv[]) {
									System.out.println("");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (obj instanceof T) {
						    ^^^
					Type Object cannot be safely cast to T
					----------
					""",
				"""
					X.java:4: error: Object cannot be safely cast to T
					        if (obj instanceof T) {
					            ^
					  where T is a type-variable:
					    T extends Object declared in class X""",
				null,
				true,
				compilerOptions);
		}
	public void testBug562392c() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X<T> {
								public boolean foo(Object obj) {
							        if (obj instanceof T t) {
							            return false;
							        }
							        return true;
							    }
								public static void main(String argv[]) {
									System.out.println("");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (obj instanceof T t) {
						    ^^^
					Type Object cannot be safely cast to T
					----------
					""",
				"""
					X.java:4: error: Object cannot be safely cast to T
					        if (obj instanceof T t) {
					            ^
					  where T is a type-variable:
					    T extends Object declared in class X""",
				null,
				true,
				compilerOptions);
		}
	public void testBug562392d() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X<T> {
								public boolean foo(Object obj) {
							        if (null instanceof T t) {
							            return false;
							        }
							        return true;
							    }
								public static void main(String argv[]) {
									System.out.println(abc);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						System.out.println(abc);
						                   ^^^
					abc cannot be resolved to a variable
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392e() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X<T> {
								public boolean foo(X<?> obj) {
							        if (obj instanceof X<String> p) {
							            return true;
							        }
							        return false;
							    }
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (obj instanceof X<String> p) {
						    ^^^
					Type X<capture#1-of ?> cannot be safely cast to X<String>
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392f() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							class Outer<T> {
							    static class Inner<T> {
							    }
							}
							@SuppressWarnings({"preview", "rawtypes"})
							class X<T> {
							    public boolean foo(Outer.Inner obj) {
							        if (obj instanceof Outer<?> p) {
							            return true;
							        }
							        return false;
							    }
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						if (obj instanceof Outer<?> p) {
						    ^^^^^^^^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types Outer.Inner and Outer<?>
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392g() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							class Outer<T> {
							    static class Inner<T> {
							    }
							}
							@SuppressWarnings({"preview", "rawtypes"})
							class X<T> {
							    public boolean foo(Object obj) {
							        if (obj instanceof Outer.Inner<?> p) {
							            return true;
							        }
							        return false;
							    }
								public static void main(String argv[]) {
									Outer.Inner inn = new Outer.Inner();
							    	System.out.println(new X<String>().foo(inn));
								}
							}
							""",
				},
				"true",
				compilerOptions);
	}
	public void testBug562392h() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings({"rawtypes"})
							class Y extends X {}
							@SuppressWarnings({"rawtypes"})
							public class X<T> {
								public boolean foo(X[] obj) {
							        if (obj instanceof Y[] p) {
							            return true;
							        }
							        return false;
							    }
								public static void main(String argv[]) {
									Object[] param = {new X()};
							       System.out.println(new X<String>().foo(param));
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						System.out.println(new X<String>().foo(param));
						                                   ^^^
					The method foo(X[]) in the type X<String> is not applicable for the arguments (Object[])
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392i() {
		Map<String, String> options = getCompilerOptions(false);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		runNegativeTest(
				new String[] {
						"Test.java",
						"""
							import java.util.ArrayList;
							import java.util.List;
							import java.util.function.Function;
							import java.util.function.UnaryOperator;
							@SuppressWarnings({"preview"})
							public class Test<T> {
							    public boolean foo(Function<ArrayList<T>, ArrayList<T>> obj) {
							        if (obj instanceof UnaryOperator<? extends List<T>>) {
							            return false;
							        }
							        return true;
							    }
							}
							""",
				},
				"""
					----------
					1. ERROR in Test.java (at line 8)
						if (obj instanceof UnaryOperator<? extends List<T>>) {
						    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Cannot perform instanceof check against parameterized type UnaryOperator<? extends List<T>>. Use the form UnaryOperator<?> instead since further generic type information will be erased at runtime
					----------
					""",
					"",
					null,
					true,
					options);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
	}
	public void testBug562392j() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"Test.java",
						"""
							import java.util.ArrayList;
							import java.util.List;
							import java.util.function.Function;
							import java.util.function.UnaryOperator;
							@SuppressWarnings({"preview", "rawtypes"})
							public class Test<T> {
							    public boolean foo(Function<ArrayList<T>, ArrayList<T>> obj) {
							        if (obj instanceof UnaryOperator<? extends List<T>>) {
							            return false;
							        }
							        return true;
							    }
								public static void main(String argv[]) {
							       System.out.println("");
								}
							}
							""",
				},
				"",
				compilerOptions);
	}
	public void test053() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public static void main(String argv[]) {
									Object obj = "x";
									if (obj instanceof String s) {
										System.out.println(s);
									}
									String s = "y";
									System.out.println(s);
								}
							}
							""",
				},
				"x\n" +
				"y",
				compilerOptions);
	}
	public void test054() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public static void main(String argv[]) {
									Object obj = "x";
									while (!(obj instanceof String s)) {
										String s = "y";
										System.out.println(s);
									}
									System.out.println(s);
								}
							}
							""",
				},
				"x",
				compilerOptions);
	}
	public void test055() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							public static void main(String argv[]) {
									String result = "";
									Object obj = "abc";
									for (; !(obj instanceof String a);) {
										String a = "";
										result = a;
										obj = null;
									}
									if (!result.equals("abc")) {
										System.out.println("PASS");
									} else {
										System.out.println("FAIL");
									}
								}
							}
							""",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in for loop
	public void test056() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
								public static int impl(I a) {
									return a.foo("Default");
								}
								public static void main(String argv[]) {
									String result = "";
									Object obj = "a";
									for (int i = 0; !(obj instanceof String a); i = impl(a -> a.length())) {
										obj = null;
									}
									if (!result.equals(""))
										System.out.println("FAIL");
									else
										System.out.println("PASS");
								}
							}
							interface I {
								int foo(String s);
							}
							""",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in for loop (block)
	public void test056a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
								public static int impl(I a) {
									return a.foo("Default");
								}
								public static void main(String argv[]) {
									String result = "";
									Object obj = "a";
									for (int i = 0; !(obj instanceof String a); i = impl(x -> {
																						String a = "";
																						return a.length();
																					})) {
										obj = null;
									}
									if (!result.equals(""))
										System.out.println("FAIL");
									else
										System.out.println("PASS");
								}
							}
							interface I {
								int foo(String s);
							}
							""",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in if
	public void test056b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
								public static int impl(I a) {
									return a.foo("Default");
								}
								public static void main(String argv[]) {
									String result = "";
									Object obj = "a";
									if (!(obj instanceof String a)) {
										  int i = impl(a -> a.length());
									}
									if (!result.equals(""))
										System.out.println("FAIL");
									else
										System.out.println("PASS");
								}
							}
							interface I {
								int foo(String s);
							}
							""",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in if
	public void test056d() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
								public static int impl(I a) {
									return a.foo("Default");
								}
								public static void main(String argv[]) {
									String result = "";
									Object obj = "a";
									for (int i = 0; (obj instanceof String a); i = impl(a -> a.length())) {
										obj = null;
									}
									if (!result.equals(""))
										System.out.println("FAIL");
									else
										System.out.println("PASS");
								}
							}
							interface I {
								int foo(String s);
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						for (int i = 0; (obj instanceof String a); i = impl(a -> a.length())) {
						                                                    ^
					Lambda expression\'s parameter a cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	/*
	 * Test we report only one duplicate variable, i.e., in THEN stmt
	 * where pattern variable is in scope.
	 */
	public void test057() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public static void main(String argv[]) {
									Object obj = "x";
									if (obj instanceof String s) {
										String s = "";
										System.out.println(s);
									}
									String s = "y";
									System.out.println(s);
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						String s = "";
						       ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
		}
	public void test058() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public static void main(String[] s) {
									Object obj = "x";
									if (obj instanceof String[] s && s.length > 0) {
										System.out.println(s[0]);
									}
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (obj instanceof String[] s && s.length > 0) {
						                            ^
					Duplicate local variable s
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	public void test059() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 static int count;
							 public static void main(String[] args) {
							   int i = 10;
							   if (foo() instanceof String s) {
							     ++i;
							   }
							   System.out.println("count:"+X.count+" i:"+i);
							 }
							 public static Object foo() {
							   ++X.count;
							   return new Object();
							 } \s
							}""",
				},
				"count:1 i:10",
				compilerOptions);
	}
	public void test060() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 static int count;
							 public static void main(String[] args) {
							   int i = 10;
							   if (foo() instanceof String s) {
							     ++i;
							   }
							   System.out.println("count:"+X.count+" i:"+i);
							 }
							 public static Object foo() {
							   ++X.count;
							   return new String("hello");
							 } \s
							}""",
				},
				"count:1 i:11",
				compilerOptions);
	}
	public void test061() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 static int count;
							 static String STR = "FAIL";
							 @SuppressWarnings("preview")
							 public static void main(String[] args) {
							   if ( switch(STR) {
							       default -> (CharSequence)"PASS";
							       } instanceof String s) {
							     System.out.println(s);
							   }
							 }
							}""",
				},
				"PASS",
				compilerOptions);
	}
	public void test062() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 @SuppressWarnings("preview")
							 public void foo(Object o) {
							   int len  = (o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());
							 }
							  public int test(FI fi) {
								  return fi.length("");
							  }\s
							  interface FI {
								  public int length(String str);
							  }\
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						int len  = (o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());
						                                          ^
					Lambda expression\'s parameter p cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	// Same as above, but pattern variable in scope in false of conditional expression
	public void test063() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 @SuppressWarnings("preview")
							 public void foo(Object o) {
							   int len  = !(o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());
							 }
							  public int test(FI fi) {
								  return fi.length("");
							  }\s
							  interface FI {
								  public int length(String str);
							  }\
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						int len  = !(o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());
						                                                                   ^
					Lambda expression\'s parameter p cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	// Test that pattern variables are seen by body of lamda expressions
	public void test063a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 @SuppressWarnings("preview")
							 public void foo(Object o) {
							   int len  = (o instanceof String p) ? test(p1 -> p.length()) : test(p2 -> p.length());
							 }
							  public int test(FI fi) {
								  return fi.length("");
							  }\s
							  interface FI {
								  public int length(String str);
							  }\
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						int len  = (o instanceof String p) ? test(p1 -> p.length()) : test(p2 -> p.length());
						                                                                         ^
					p cannot be resolved
					----------
					""",
				"",
				null,
				true,
				compilerOptions);
	}
	// Test that pattern variables are seen by body of anonymous class creation
	public void test063b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 @SuppressWarnings("preview")
							 public void foo(Object o) {
									int len = (o instanceof String p) ? test(new X.FI() {
										@Override
										public int length(String p1) {
											return p.length();
										}
									}) : test(new X.FI() {
										@Override
										public int length(String p2) {
											return p.length();
										}
									});
								}
								public int test(FI fi) {
									return fi.length("");
								}
								interface FI {
									public int length(String str);
								}\
							}""",
					},
					"""
						----------
						1. ERROR in X.java (at line 12)
							return p.length();
							       ^
						p cannot be resolved
						----------
						""",
					"",
					null,
					true,
					compilerOptions);
	}
	// Test that pattern variables are shadowed by parameters in an anonymous class
	// creation
	public void test063c() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void main(String argv[]) {
									System.out.println(new X().foo("test"));
								}
							 @SuppressWarnings("preview")
							 public int foo(Object o) {
									int len = (o instanceof String p) ? test(new X.FI() {
										String s = p; // allowed
										@Override
										public int length(String p) {
											return p.length();
										}
									}) : test(new X.FI() {
										@Override
										public int length(String p) {
											return p.length();
										}
									});
									return len;
								}
								public int test(FI fi) {
									return fi.length("fi");
								}
								interface FI {
									public int length(String str);
								}\
							}""",
					},
					"2",
					compilerOptions);
	}
	public void test064() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							 	public static void main(String argv[]) {
									System.out.println(new X().foo("foo", "test"));
								}
								public boolean foo(Object obj, String s) {
									class Inner {
										public boolean foo(Object obj) {
											if (obj instanceof String s) {
												// s is shadowed now
												if ("foo".equals(s))
													return false;
											} else if (obj instanceof String s) {\s
											}
											// s is not shadowed
											return "test".equals(s);
										}
									}
									return new Inner().foo(obj);
								}\
							}""",
				},
				"false",
				compilerOptions);
	}
	public void test065() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							 	public static void main(String argv[]) {
									new X().foo("foo");
								}
								public void foo(Object o) {
									if ((o instanceof String s)) {
										System.out.println("if:" + s);
									} else {
										throw new IllegalArgumentException();
									}
									System.out.println("after:" + s);
								}\
							}""",
				},
				"if:foo\n" +
				"after:foo",
				compilerOptions);
	}
	public void test066() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							    protected Object x = "FIELD X";
							    public void f(Object obj, boolean b) {
							        if ((x instanceof String x)) {
							            System.out.println(x.toLowerCase());
							        }
							    }
								public static void main(String[] args) {
									new X().f(Integer.parseInt("1"), false);
								}
							}""",
				},
				"field x",
				compilerOptions);
	}
	public void test067() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							    protected Object x = "FIELD X";
							    public void f(Object obj, boolean b) {
							        if ((x instanceof String x) && x.length() > 0) {
							            System.out.println(x.toLowerCase());
							        }
							    }
								public static void main(String[] args) {
									new X().f(Integer.parseInt("1"), false);
								}
							}""",
				},
				"field x",
				compilerOptions);
	}
	public void test068() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							    static void foo(Object o) {
									if (o instanceof X x || o instanceof X) {
							            System.out.println("X");
									}
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"X",
				compilerOptions);
	}
	public void test069() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							class XPlus extends X {}
							public class X {
							    static void foo(Object o) {
									if (o instanceof X x && x instanceof XPlus x) {
							            System.out.println("X");
									}
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (o instanceof X x && x instanceof XPlus x) {
						                                           ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				null,
				true,
				compilerOptions);
	}

	public void test070() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							    static void foo(Object o) {
									if (o instanceof X x || o instanceof X x) {
							            System.out.println("X");
									}
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)\r
						if (o instanceof X x || o instanceof X x) {\r
						                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}

	public void test071() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							    static void foo(Object o) {
									if (o instanceof X x || o instanceof X x) {
							            System.out.println(x);
									}
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (o instanceof X x || o instanceof X x) {
						                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 5)
						System.out.println(x);
						                   ^
					x cannot be resolved to a variable
					----------
					""",
				null,
				true,
				compilerOptions);
	}

	public void test072() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							    static void foo(Object o) {
									if (o instanceof X x || o instanceof X x) {
										throw new IllegalArgumentException();
									}
							     System.out.println(x);
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (o instanceof X x || o instanceof X x) {
						                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 7)
						System.out.println(x);
						                   ^
					x cannot be resolved to a variable
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	public void test073() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							 static void foo(Object o) {
									try {
										if (!(o instanceof X x) || x != null || x!= null) { // allowed\s
											throw new IllegalArgumentException();
										}
							    	 	System.out.println(x); // allowed\s
								  	} catch (Throwable e) {
								  		e.printStackTrace(System.out);
								  	}
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"""
					java.lang.IllegalArgumentException
						at X.foo(X.java:6)
						at X.main(X.java:14)""",
				compilerOptions);
	}
	public void test074() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							   static void foo(Object o) {
									if (!(o instanceof X x) || x != null || x!= null) {
							     	System.out.println(x); // not allowed
									}
								  }
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						System.out.println(x); // not allowed
						                   ^
					x cannot be resolved to a variable
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	public void test075() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							 public boolean isMyError(Exception e) {
							        return e instanceof MyError my && (my.getMessage().contains("something") || my.getMessage().contains("somethingelse"));
							 }
								public static void main(String[] args) {
									System.out.println("hello");
								}
							}
							class MyError extends Exception {}
							""",
				},
				"hello",
				compilerOptions);
	}
	public void test076() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							@SuppressWarnings("preview")
							public class X {
							   static void foo(Object o) {
								   if ( (! (o instanceof String a)) || (o instanceof String a) ) {
									   // Nothing
								   }
								  }
								public static void main(String[] args) {
									System.out.println("hello");
								}
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if ( (! (o instanceof String a)) || (o instanceof String a) ) {
						                                                         ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	// Test that a non final pattern variable can be assigned again
	public void test077() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
								"""
									public class X {
									    static void foo(Object o) {
											if (o instanceof X x) {
												x = null;
									         System.out.println(x);
											}
										}
										public static void main(String[] args) {
											foo(new X());
										}
									}""",
				},
				"null",
				compilerOptions);
	}
	// Test that a final pattern variable cannot be assigned again
	public void test078() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
								"""
									public class X {
									    static void foo(Object o) {
											if (o instanceof final X x) {
												x = null;
											}
										}
										public static void main(String[] args) {
											foo(new X());
										}
									}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						x = null;
						^
					The pattern variable x is final and cannot be assigned again
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	public void test079() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
								"""
									public class X {
									    static void foo(Object o) {
											if (o instanceof public X x) {
												x = null;
											}
										}
										public static void main(String[] args) {
											foo(new X());
										}
									}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (o instanceof public X x) {
						                          ^
					Illegal modifier for the pattern variable x; only final is permitted
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	// test that we allow final for a pattern instanceof variable
	public void test080() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static void foo(Object o) {
									if (o instanceof final X x) {
							            System.out.println("X");
									}
								}
								public static void main(String[] args) {
									foo(new X());
								}
							}""",
				},
				"X",
				compilerOptions);
	}
	public void test081() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
								"""
									public class X<T> {
										public void foo(T o) {
											// Rejected
											boolean b1 = (o instanceof String a) ? (o instanceof String a) : false;
											boolean b2 = !(o instanceof String a) ? (o instanceof String a) : false;
											boolean b3 = (o instanceof String a) ? !(o instanceof String a) : false;
											boolean b4 = !(o instanceof String a) ? !(o instanceof String a) : false;
										\t
											boolean b5 = (o instanceof String a) ? true : (o instanceof String a);
											boolean b6 = !(o instanceof String a) ? true : (o instanceof String a);
											boolean b7 = (o instanceof String a) ? true : !(o instanceof String a);
											boolean b8 = !(o instanceof String a) ? true : !(o instanceof String a);
										\t
											boolean b9 = (o instanceof String) ? (o instanceof String a) : (o instanceof String a);
											boolean b10 = (o instanceof String) ? !(o instanceof String a) : !(o instanceof String a);
										\t
											// These are allowed
											boolean b11 = (o instanceof String) ? !(o instanceof String a) : !!(o instanceof String a);
											boolean b12 = (o instanceof String) ? !(o instanceof String a) : (o instanceof String a);
											boolean b21 = (o instanceof String a) ? false : ((o instanceof String a) ? false : true);\s
										}\s
									}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						boolean b1 = (o instanceof String a) ? (o instanceof String a) : false;
						                                                            ^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 5)
						boolean b2 = !(o instanceof String a) ? (o instanceof String a) : false;
						                                                             ^
					A pattern variable with the same name is already defined in the statement
					----------
					3. ERROR in X.java (at line 6)
						boolean b3 = (o instanceof String a) ? !(o instanceof String a) : false;
						                                                             ^
					A pattern variable with the same name is already defined in the statement
					----------
					4. ERROR in X.java (at line 7)
						boolean b4 = !(o instanceof String a) ? !(o instanceof String a) : false;
						                                                              ^
					A pattern variable with the same name is already defined in the statement
					----------
					5. ERROR in X.java (at line 9)
						boolean b5 = (o instanceof String a) ? true : (o instanceof String a);
						                                                                   ^
					A pattern variable with the same name is already defined in the statement
					----------
					6. ERROR in X.java (at line 10)
						boolean b6 = !(o instanceof String a) ? true : (o instanceof String a);
						                                                                    ^
					A pattern variable with the same name is already defined in the statement
					----------
					7. ERROR in X.java (at line 11)
						boolean b7 = (o instanceof String a) ? true : !(o instanceof String a);
						                                                                    ^
					A pattern variable with the same name is already defined in the statement
					----------
					8. ERROR in X.java (at line 12)
						boolean b8 = !(o instanceof String a) ? true : !(o instanceof String a);
						                                                                     ^
					A pattern variable with the same name is already defined in the statement
					----------
					9. ERROR in X.java (at line 14)
						boolean b9 = (o instanceof String) ? (o instanceof String a) : (o instanceof String a);
						                                                                                    ^
					A pattern variable with the same name is already defined in the statement
					----------
					10. ERROR in X.java (at line 15)
						boolean b10 = (o instanceof String) ? !(o instanceof String a) : !(o instanceof String a);
						                                                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	public void testBug570831a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
							"""
								public class X {
									public static void run() {
										String s = "s";
										Object o = null;
										{
											while (!(o instanceof String v)) {
												o = null;
											}
											s = s + v; // allowed
										}
										for (int i = 0; i < 1; i++) {
											s = s + v; // not allowed
										}
									}
								}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 12)
						s = s + v; // not allowed
						        ^
					v cannot be resolved to a variable
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	public void testBug570831b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
							"""
								public class X {
									public static void run() {
										String s = "s";
										Object o = null;
										{
											int local = 0;
											while (!(o instanceof String v)) {
												o = null;
											}
											s = s + v; // allowed
										}
										for (int i = 0; i < 1; i++) {
											s = s + v; // not allowed
										}
									}
								}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						s = s + v; // not allowed
						        ^
					v cannot be resolved to a variable
					----------
					""",
				null,
				true,
				compilerOptions);
	}
	public void testBug572380_1() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X1.java",
						"""
							
							public class X1 {
							    boolean b1, b2, b3;
							
							    static boolean bubbleOut(Object obj) {
								       return obj instanceof X1 that && that.b1 && that.b2 && that.b3;
							    }
							
							    static boolean propagateTrueIn(Object obj) {
							        return obj instanceof X1 that && (that.b1 && that.b2 && that.b3);
							    }
							
							    public static void main(String[] obj) {
							        var ip = new X1();
							        ip.b1 = ip.b2 = ip.b3 = true;
							        System.out.println(bubbleOut(ip) && propagateTrueIn(ip));
							    }
							
							}
							""",
				},
				"true",
				options);
	}
	public void testBug572380_2() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X1.java",
						"""
							
							public class X1 {
							    boolean b1, b2, b3;
							    static boolean testErrorOr(Object obj) {
							        return (!(obj instanceof X1 that)) || that.b1 && that.b2;
							    }
							   \s
							    public static void main(String[] obj) {
							        var ip = new X1();
							        ip.b1 = ip.b2 = ip.b3 = true;
							        System.out.println(testErrorOr(ip));
							    }
							
							}
							""",
				},
				"true",
				options);
	}
    public void testBug574892() {
        Map<String, String> options = getCompilerOptions(false);
        runConformTest(
                new String[] {
                        "X1.java",
                        """
							
							public class X1 {
							    static boolean testConditional(Object obj) {
							        return obj instanceof Integer other
							                && ( other.intValue() > 100
							                   ? other.intValue() < 200 : other.intValue() < 50);
							    }
							    public static void main(String[] obj) {
							        System.out.println(testConditional(101));
							    }
							}
							""",
                },
                "true",
                options);
    }
	public void testBug572431_1() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
										   static public void something () {
										      boolean bool = true;
										      Object object = null;
										      if (object instanceof String string) {
										      } else if (bool && object instanceof Integer integer) {
										      }
										   }
										   static public void main (String[] args) throws Exception {
										   }
										}""",
				},
				"",
				options);

	}
	public void testBug572431_2() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static public void something () {
							    boolean bool = true;
							    Object object = null;
							    if (object instanceof String string) {
							    } else if (bool) {
							      if (object instanceof Integer integer) {
							      }
							    }
							  }
							  static public void main (String[] args) throws Exception {
							  }
							}""",
				},
				"",
				options);

	}
	public void testBug572431_3() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static public void something () {
							    boolean bool = true;
							    Object object = null;
							    if (bool && object instanceof Integer i) {
								   }
							  }
							  static public void main (String[] args) throws Exception {
							  }
							}""",
				},
				"",
				options);

	}
	public void testBug572431_4() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static public void something () {
							    boolean bool = true;
							    Object object = null;
							    if (!(object instanceof Integer i)) {
								   }
							  }
							  static public void main (String[] args) throws Exception {
							  }
							}""",
				},
				"",
				options);

	}
	public void testBug572431_5() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static public void something () {
							    boolean bool = true;
							    Object object = null;
							    if (false) {
								   } else if (!(object instanceof Integer i)) {
								   }
							  }
							  static public void main (String[] args) throws Exception {
							  }
							}""",
				},
				"",
				options);

	}
	public void testBug572431_6() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  static public void something () {
							    boolean bool = true;
									Object object = null;
									for (int i = 0; i < 10; i++) {
										if (object instanceof String string) {
											System.out.println(i);
										} else if (bool) {
											if (i == 4) continue;
											System.out.println(i);
										}
									}
							  }
							  static public void main (String[] args) throws Exception {
							  }
							}""",
				},
				"",
				options);

	}
	public void testBug573880() {
		if (this.complianceLevel < ClassFileConstants.JDK17)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
							"""
								public class X {
									public void foo(Object o) {
										if (o instanceof var s) {
											System.out.println(s);
										}
									}
								}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (o instanceof var s) {
						                 ^^^
					\'var\' is not allowed here
					----------
					""",
				null,
				true,
				compilerOptions);
	}
    public void testBug574906() {
        Map<String, String> options = getCompilerOptions(false);
        runConformTest(
                new String[] {
                        "X1.java",
                        """
							
							public class X1 {
							    static boolean testConditional(Object obj) {
							        return obj instanceof Number oNum && oNum.intValue() < 0 && !(oNum instanceof Integer);
							    }
							    public static void main(String[] obj) {
							        System.out.println(testConditional(-2f));
							    }
							}
							""",
                },
                "true",
                options);
    }
    public void testBug575035() throws ClassFormatException, IOException {
        Map<String, String> options = getCompilerOptions(false);
    	String source =
    			"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			\s
			public class Test {
			    @Target({ ElementType.LOCAL_VARIABLE})
			    @Retention(RetentionPolicy.RUNTIME)
			    @interface Var {}
			    @Target({ ElementType.TYPE_USE})
			    @Retention(RetentionPolicy.RUNTIME)
			    @interface Type {}
			    public static void main(String[] args) {\
			        @Var @Type String y = "OK: ";
			        if (((Object)"local") instanceof @Var @Type String x) {
			            System.out.println(new StringBuilder(y).append(x));
			        }
			    }
			}""";
    	String expectedOutput =  """
			  public static void main(String[] args);
			     0  ldc <String "OK: "> [16]
			     2  astore_1 [y]
			     3  ldc <String "local"> [18]
			     5  dup
			     6  astore_3
			     7  instanceof String [20]
			    10  ifeq 36
			    13  aload_3
			    14  checkcast String [20]
			    17  astore_2 [x]
			    18  getstatic System.out : PrintStream [22]
			    21  new StringBuilder [28]
			    24  dup
			    25  aload_1 [y]
			    26  invokespecial StringBuilder(String) [30]
			    29  aload_2 [x]
			    30  invokevirtual StringBuilder.append(String) : StringBuilder [33]
			    33  invokevirtual PrintStream.println(Object) : void [37]
			    36  return
			      Line numbers:
			        [pc: 0, line: 13]
			        [pc: 3, line: 14]
			        [pc: 18, line: 15]
			        [pc: 36, line: 17]
			      Local variable table:
			        [pc: 0, pc: 37] local: args index: 0 type: String[]
			        [pc: 3, pc: 37] local: y index: 1 type: String
			        [pc: 18, pc: 36] local: x index: 2 type: String
			      Stack map table: number of frames 1
			        [pc: 36, append: {String}]
			    RuntimeVisibleTypeAnnotations:\s
			      #50 @Type(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 3, pc: 37] index: 1
			      )
			      #50 @Type(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 18, pc: 36] index: 2
			      )
			
			  Inner classes:
			    [inner class info: #54 Test$Type, outer class info: #1 Test
			     inner name: #56 Type, accessflags: 9736 abstract static],
			    [inner class info: #57 Test$Var, outer class info: #1 Test
			     inner name: #59 Var, accessflags: 9736 abstract static]
			
			Nest Members:
			   #54 Test$Type,
			   #57 Test$Var
			}""";
    	checkClassFile("Test", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
        runConformTest(
                new String[] {
                        "Test.java",
                        source,
                },
                "OK: local",
                options);

    }
	public void testBug578628_1() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		runNegativeTest(
				new String[] {
						"X.java",
							"""
								public class X {
								    public static Object str = "a";
								    public static void foo() {
								    	if (str instanceof (String a && a == null)) {
								            System.out.println(true);
								        } else {
								        	System.out.println(false);
								        }
								    }\s
								    public static void main(String[] argv) {
								    	foo();
								    }
								}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (str instanceof (String a && a == null)) {
						                   ^
					Syntax error on token "(", delete this token
					----------
					2. ERROR in X.java (at line 4)
						if (str instanceof (String a && a == null)) {
						                                          ^
					Syntax error on token ")", delete this token
					----------
					""",
				false);
	}
	public void testBug578628_1a() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"""
								public class X {
								    public static Object str = "a";
								    public static void foo() {
								    	if (str instanceof String a && a == null) {
								            System.out.println(true);
								        } else {
								        	System.out.println(false);
								        }
								    }\s
								    public static void main(String[] argv) {
								    	foo();
								    }
								}""",
				},
				"false",
				compilerOptions);
	}
	public void testBug578628_2() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"""
								public class X {
								    public static Object str = "a";
								    public static void foo() {
								    	if (str instanceof String a && a != null) {
								            System.out.println(true);
								        } else {
								        	System.out.println(false);
								        }
								    }\s
								    public static void main(String[] argv) {
								    	foo();
								    }
								}""",
				},
				"true",
				compilerOptions);
	}
	public void testBug578628_3() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"""
								public class X {
								    public static Object str = "a";
								    public static void foo() {
								    	bar(str instanceof String a && a == null);
								    }\s
								    public static void bar(boolean arg) {
								    	System.out.println(arg);
								    }
								    public static void main(String[] argv) {
								    	foo();
								    }
								}""",
				},
				"false",
				compilerOptions);
	}
	public void testBug578628_4() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"""
								public class X {
								    public static Object str = "a";
								public static void foo() {
								    	boolean b = switch (str) {
								    		case String s -> {
								    			yield (str instanceof String a && a != null);
								    		}
								    		default -> false;
								    	};
								    	System.out.println(b);
								    }
								    public static void main(String[] argv) {
								    	foo();
								    }
								}""",
				},
				"true",
				compilerOptions);
	}
	public void testGH1726() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							record A(int x) {
							}

							public static int foo(Object a) {
								return a instanceof A(int x) ? x : 1;
							}

							public static void main(String [] args) {
								System.out.println("" + foo(new A(1234)) + foo(args));
							}
						}
						""",
				},
				"12341",
				compilerOptions);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1725
	// [21] Wrongly needing a default case for a switch expression
	public void testGH1725() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							public abstract sealed class A permits B, C {
							}

							public final class C extends A {
							}

							public abstract sealed class B extends A permits D {
							}

							public final class D extends B {
							}

							public String foo(A a) {
								return switch (a) {
									case D d -> "1234";
									case C c -> "6789";
								};
							}
							public static void main(String [] args) {
								System.out.println(new X().foo(new X().new D()) + new X().foo(new X().new C()));
							}
						}
						""",
				},
				"12346789",
				compilerOptions);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1725
	// [21] Wrongly needing a default case for a switch expression
	public void testGH1725_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;

		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
							public abstract sealed class A permits B, C {
							}

							public final class C extends A {
							}

							public sealed class B extends A permits D {
							}

							public final class D extends B {
							}

							public String foo(A a) {
								return switch (a) {
									case D d -> "1234";
									// case B b -> "blah";
									case C c -> "6789";
								};
							}
							public static void main(String [] args) {
								System.out.println(new X().foo(new X().new D()) + new X().foo(new X().new C()));
							}
						}
						""",
				},
				"""
				----------
				1. ERROR in X.java (at line 15)
					return switch (a) {
					               ^
				A switch expression should have a default case
				----------
				""",
				false);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1759
	// Pattern variable is not recognized in AND_AND_Expression
	public void testGHI1759() {

		runConformTest(
				new String[] {
						"X.java",
						"""
						import java.util.Arrays;
						import java.util.List;
						public class X {
							public static void main(String [] args) {
						        Object obj = "test";
						        List<String> values = Arrays.asList("fail", "test", "pass");
						        if (obj instanceof String str && values.stream().anyMatch(str::equalsIgnoreCase)) {
						            System.out.println(str);
						        }
						    }

						}
						""",
				},
				"test");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1759
	// Pattern variable is not recognized in AND_AND_Expression
	public void testGHI1759_2() {

		runConformTest(
				new String[] {
						"X.java",
						"""
						import java.util.Arrays;
						import java.util.List;
						public class X {
							public static void main(String [] args) {
						        Object obj = "test";
						        List<String> values = Arrays.asList("fail", "test", "pass");
						        if (!(obj instanceof String str) || values.stream().anyMatch(str::equalsIgnoreCase)) {
						            System.out.println(obj);
						        }
						    }

						}
						""",
				},
				"test");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1759
	// Pattern variable is not recognized in AND_AND_Expression
	public void testGHI1759_3() {

		runConformTest(
				new String[] {
						"X.java",
						"""
						import java.util.Arrays;
						import java.util.List;
						public class X {
						    public static void main(String [] args) {
						        Object obj = "test";
						        List<String> values = Arrays.asList("fail", "test", "pass");
						        if (obj instanceof String str) {
						        	if (values.stream().anyMatch(str::equalsIgnoreCase)) {
						        		System.out.println(str);
						        	}
						        }
						    }
						}
						""",
				},
				"test");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1485
	// ECJ hangs when pattern matching code is used in a nested conditional expression.
	public void testGHI1485() {

		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    static class PvsVariable {
						    }

						    static class PvsVariableNext_1 extends PvsVariable {
						    }

						    static class PvsVariableNext_2 extends PvsVariableNext_1 {
						    }

						    static class PvsVariableNext_3 extends PvsVariableNext_2 {
						    }

						    static class PvsVariableNext_4 extends PvsVariableNext_3 {
						    }

						    public static void main(String[] args) {
						        final var pvsVariable = new PvsVariableNext_3();
						        final Object origVar =
						                pvsVariable instanceof PvsVariableNext_1 var_first
						                    ? "PvsVariableNext_1"
						                    : (pvsVariable instanceof PvsVariableNext_2 var_second &&
						                       pvsVariable instanceof PvsVariableNext_3 var_three &&
						                       true) ? "PvsVariableNext_2 && PvsVariableNext_3"
						                                : "None";
						        System.out.println(origVar);
						    }
						}
						""",
				},
				"PvsVariableNext_1");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1887
	// [Patterns] ECJ tolerates erroneous redeclaration of pattern bindings in some cases
	public void testGHI1887() {

		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X<T> {
							public void foo(T o) {
								/*
								 * 6.3.1 Scope for Pattern Variables in Expressions
								 * 6.3.1.1 Conditional-And Operator &&
								 *
								 * It is a compile-time error if any of the following conditions hold:
									• A pattern variable is both (i) introduced by a when true and (ii) introduced by
									b when true.
									• A pattern variable is both (i) introduced by a when false and (ii) introduced by
									b when false.
								 */
								boolean b = o instanceof String a && o instanceof Double a;   // Error: correct
								b = !(o instanceof String a) && !(o instanceof Double a);     // <<<----- Error NOT reported by ECJ. Javac complains

								/*
								 * 6.3.1.2 Conditional-Or Operator ||
								 *
								 * It is a compile-time error if any of the following conditions hold:
									• A pattern variable is both (i) introduced by a when true and (ii) introduced by
									b when true.
									• A pattern variable is both (i) introduced by a when false and (ii) introduced by
									b when false.
								 */
								b =  o instanceof String a || o instanceof Double a;      // <<<----- Error NOT reported by ECJ. Javac complains
								b =  !(o instanceof String a) || !(o instanceof Double a); // Error: correct

								/*
								 * 6.3.1.4 Conditional Operator a ? b : c
								 *
								 * It is a compile-time error if any of the following conditions hold:
									• A pattern variable is both (i) introduced by a when true and (ii) introduced by
									c when true.
									• A pattern variable is both (i) introduced by a when true and (ii) introduced by
									c when false.
									• A pattern variable is both (i) introduced by a when false and (ii) introduced by
									b when true.
									• A pattern variable is both (i) introduced by a when false and (ii) introduced by
									b when false.
									• A pattern variable is both (i) introduced by b when true and (ii) introduced by
									c when true.
									• A pattern variable is both (i) introduced by b when false and (ii) introduced by
									c when false.
								 */

								b = o instanceof String a ? true : o instanceof String a;  // error correctly reported
								b = o instanceof String a ? true : !(o instanceof String a); // error correctly reported
								b = !(o instanceof String a) ? o instanceof String a : true; // error correctly reported
								b = !(o instanceof String a) ? !(o instanceof String a) : true; // error correctly reported
								b = b ? (o instanceof String a) : (o instanceof String a); // error correctly reported
								b = b ? !(o instanceof String a) : !(o instanceof String a); // error correctly reported

							}
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						boolean b = o instanceof String a && o instanceof Double a;   // Error: correct
						                                                         ^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 14)
						b = !(o instanceof String a) && !(o instanceof Double a);     // <<<----- Error NOT reported by ECJ. Javac complains
						                                                      ^
					A pattern variable with the same name is already defined in the statement
					----------
					3. ERROR in X.java (at line 25)
						b =  o instanceof String a || o instanceof Double a;      // <<<----- Error NOT reported by ECJ. Javac complains
						                                                  ^
					A pattern variable with the same name is already defined in the statement
					----------
					4. ERROR in X.java (at line 26)
						b =  !(o instanceof String a) || !(o instanceof Double a); // Error: correct
						                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					5. ERROR in X.java (at line 46)
						b = o instanceof String a ? true : o instanceof String a;  // error correctly reported
						                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					6. ERROR in X.java (at line 47)
						b = o instanceof String a ? true : !(o instanceof String a); // error correctly reported
						                                                         ^
					A pattern variable with the same name is already defined in the statement
					----------
					7. ERROR in X.java (at line 48)
						b = !(o instanceof String a) ? o instanceof String a : true; // error correctly reported
						                                                   ^
					A pattern variable with the same name is already defined in the statement
					----------
					8. ERROR in X.java (at line 49)
						b = !(o instanceof String a) ? !(o instanceof String a) : true; // error correctly reported
						                                                     ^
					A pattern variable with the same name is already defined in the statement
					----------
					9. ERROR in X.java (at line 50)
						b = b ? (o instanceof String a) : (o instanceof String a); // error correctly reported
						                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					10. ERROR in X.java (at line 51)
						b = b ? !(o instanceof String a) : !(o instanceof String a); // error correctly reported
						                                                         ^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1406
	// [Patterns] Bizarre code generation for type test patterns
    public void testGH1406() throws ClassFormatException, IOException {
        Map<String, String> options = getCompilerOptions(false);
    	String source =
    			"""
    			public class X {
					public static void main(String[] args) {
						Object o = null;
						if (o instanceof String x) {

						}
					}
				}
    			""";
    	String expectedOutput =
    			"""
			  public static void main(String[] args);
			     0  aconst_null
			     1  astore_1 [o]
			     2  aload_1 [o]
			     3  instanceof String [16]
			     6  ifeq 14
			     9  aload_1 [o]
			    10  checkcast String [16]
			    13  astore_2
			    14  return
			""";
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
        runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "",
                options);
    }
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1406
	// [Patterns] Bizarre code generation for type test patterns
    public void testGH1406_2() throws ClassFormatException, IOException {
        Map<String, String> options = getCompilerOptions(false);
    	String source =
    			"""
    			public class X {
				    Object o = "Helo";
				    public void foo() {
				        if (o instanceof String s) {

				        }
				    }

				    public static void main(String [] args) {
				        new X().foo();
				    }
				}
    			""";
    	String expectedOutput =
    			 """
			  public void foo();
			     0  aload_0 [this]
			     1  getfield X.o : Object [14]
			     4  dup
			     5  astore_2
			     6  instanceof String [21]
			     9  ifeq 17
			    12  aload_2
			    13  checkcast String [21]
			    16  astore_1
			    17  return
			""";
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
        runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "",
                options);

    }
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							static public Object a0 = "a";

							public static void main(String argv[]) {
								String res = "";
								Object ax = a0;

								if ( (! (ax instanceof String a)) && (ax instanceof String a) ) {
									res += "t" + a; // after then
								} else {
									res += "e" + ""; // after else
								}
								if (!res.equals("e")) {
									System.out.println("Got: " + res + ", expected: e");
								} else {
									System.out.println("OK!");
								}
							}
						}
						""",
				},
				"OK!");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_2() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = (o instanceof String) ? (o instanceof String a) : (! (o instanceof String  a));
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "true");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_3() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = (o instanceof String) ? !(o instanceof String a) : (o instanceof String  a);
							  	System.out.println(b);
							}
						}
						""",
				},
				"false\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_4() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = (o instanceof String) ? !!(o instanceof String a) : (o instanceof String  c);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_5() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = (o instanceof String) ? (o instanceof String a) : (o instanceof String  c);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_6() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = (o instanceof String) ? (o instanceof String a) : !!(o instanceof String  c);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_7() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = (o instanceof String s1) ? (o instanceof String s2) : (o instanceof String  s3);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_8() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = !(o instanceof String s1) ? (o instanceof String s2) : (o instanceof String  s3);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_9() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = !!(o instanceof String s1) ? (o instanceof String s2) : (o instanceof String  s3);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "false");
	}
	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1889
	public void testIssue1889_10() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						  public static void main(String[] o) {
								foo("one");
								foo(new X());
							}
						  public static void foo(Object o) {
							  	boolean b  = !!(o instanceof String s1) ? !!!!(o instanceof String s2) : !!!!!!!(o instanceof String  s3);
							  	System.out.println(b);
							}
						}
						""",
				},
				"true\n"
				+ "true");
	}
	public void testWhileLoop() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							void foo(Object o) {
								while (o instanceof String s) {
									System.out.println("while");
									return;
								}
								System.out.println("!while");
							}
							public static void main(String [] args) {
							    new X().foo("");
							     new X().foo(null);
							}
						}
						""",
				},
				"while\n"
				+ "!while");
	}
	public void testForLoop() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							void foo(Object o) {
								for(; (o instanceof String s);) {
									System.out.println("for");
									return;
								}
								System.out.println("!for");
							}
							public static void main(String [] args) {
							    new X().foo("");
							     new X().foo(null);
							}
						}
						""",
				},
				"for\n"
				+ "!for");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2104
	// [Patterns] Missing boxing conversion after instanceof leads to verify error
	public void testBoxing() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {

							public static void foo(Boolean b) {
								System.out.println(b);
							}

							public static void main(String [] args) {
								Object o = new Object();
								foo(o instanceof String);
								foo("Hello" instanceof String);
								foo(o instanceof String s);
								foo("Hello" instanceof String s);
							}
						}
						""",
				},
				"""
					false
					true
					false
					true""");
	}
}
