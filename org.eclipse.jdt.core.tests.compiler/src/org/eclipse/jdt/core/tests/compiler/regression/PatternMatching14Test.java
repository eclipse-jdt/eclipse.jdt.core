/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class PatternMatching14Test extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 14 --enable-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test022" };
	}
	
	public static Class<?> testClass() {
		return PatternMatching14Test.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public PatternMatching14Test(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews,
				preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
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
	public void test001() {
		Map<String, String> options = getCompilerOptions(false);
		runNegativeTest(
				new String[] {
						"X1.java",
						"public class X1 {\n" +
						"  public void foo(Object obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X1.java (at line 3)\n" + 
				"	if (obj instanceof String s) {\n" + 
				"	                   ^^^^^^^^\n" + 
				"Instanceof Pattern is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test002() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X2 {\n" +
						"  public void foo(Integer obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X2.java (at line 4)\n" + 
				"	if (obj instanceof String s) {\n" + 
				"	    ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Incompatible conditional operand types Integer and String\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X3 {\n" +
						"  public void foo(Number num) {\n" +
						"		if (num instanceof Integer s) {\n" +
						"		} else if (num instanceof String) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X3.java (at line 5)\n" + 
				"	} else if (num instanceof String) {\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Incompatible conditional operand types Number and String\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X4 {\n" +
						"  public void foo(Object obj) {\n" +
						"		String s = null;\n" +
						"		if (obj instanceof Integer s) {\n" +
						"		} else if (obj instanceof String) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X4.java (at line 5)\n" + 
				"	if (obj instanceof Integer s) {\n" + 
				"	                           ^\n" + 
				"Duplicate local variable s\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X5 {\n" +
						"@SuppressWarnings(\"preview\")\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(s);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"	}\n" +
						"}\n",
				},
				"100",
				options);
	}
	public void test006() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X6.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6 {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(s);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"abcd",
				options);
	}
	public void test006a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6a {\n" +
						"	public static void foo(Object obj) {\n" +
						"		if (obj != null) {\n" +
						"			if (obj instanceof Integer i) {\n" +
						"				System.out.print(i);\n" +
						"			} else if (obj instanceof String s) {\n" +
						"				System.out.print(i);\n" +
						"			}\n " +
						"		}\n " +
						"		System.out.print(i);\n" +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X6a.java (at line 8)\n" + 
				"	System.out.print(i);\n" + 
				"	                 ^\n" + 
				"i cannot be resolved to a variable\n" + 
				"----------\n" + 
				"2. ERROR in X6a.java (at line 11)\n" + 
				"	System.out.print(i);\n" + 
				"	                 ^\n" + 
				"i cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6b {\n" +
						"	public static void foo(Object obj) {\n" +
						"		if (obj != null) {\n" +
						"			if (obj instanceof Integer i) {\n" +
						"				System.out.print(i);\n" +
						"			} else if (obj instanceof String s) {\n" +
						"				System.out.print(i);\n" +
						"			}\n " +
						"		}\n " +
						"		System.out.print(s);\n" +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X6b.java (at line 8)\n" + 
				"	System.out.print(i);\n" + 
				"	                 ^\n" + 
				"i cannot be resolved to a variable\n" + 
				"----------\n" + 
				"2. ERROR in X6b.java (at line 11)\n" + 
				"	System.out.print(s);\n" + 
				"	                 ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6c {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X6c.java (at line 7)\n" + 
				"	System.out.print(i);\n" + 
				"	                 ^\n" + 
				"i cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6d {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X6d.java (at line 7)\n" + 
				"	System.out.print(i);\n" + 
				"	                 ^\n" + 
				"i cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X7 {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X7.java (at line 7)\n" + 
				"	System.out.print(i);\n" + 
				"	                 ^\n" + 
				"i cannot be resolved to a variable\n" + 
				"----------\n",
				"X7.java:4: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.\n" + 
				"		if (obj instanceof Integer i) {\n" + 
				"		                           ^\n" + 
				"X7.java:6: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.\n" + 
				"		} else if (obj instanceof String s) {\n" + 
				"		                                 ^\n" + 
				"X7.java:7: error: cannot find symbol\n" + 
				"			System.out.print(i);\n" + 
				"			                 ^\n" + 
				"  symbol:   variable i\n" + 
				"  location: class X7\n" + 
				"1 error\n" + 
				"2 warnings",
				null,
				true,
				options);
	}
	public void test008() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X8.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X8 {\n" +
						"  	public static void foo(Object b) {\n" + 
						"		Object c = null;\n" + 
						"		if (b != c) {\n" + 
						"			if ((b instanceof String s) && (s.length() != 0))\n" + 
						"				System.out.println(\"s:\" + s);\n" + 
						"			else \n" + 
						"				System.out.println(\"b:\" + b);\n" + 
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X9 {\n" +
						"  	public static void foo(Object b) {\n" + 
						"		Object c = null;\n" + 
						"		if (b != c) {\n" + 
						"			if ((b instanceof String s) && (s.length() != 0))\n" + 
						"				System.out.println(\"s:\" + s);\n" + 
						"			else if ((b instanceof Integer i2))\n" + 
						"				System.out.println(\"i2:\" + i2);\n" + 
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X10 {\n" +
						"  	public static void foo(Object b) {\n" + 
						"		Object c = null;\n" + 
						"		if (b != c) {\n" + 
						"			if (b != null && (b instanceof String s))\n" + 
						"				System.out.println(\"s:\" + s);\n" + 
						"			else " + 
						"				System.out.println(\"b:\" + b);\n" + 
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X11 {\n" +
						"  	public static void foo(Object b) {\n" + 
						"		Object c = null;\n" + 
						"		if (b == null && (b instanceof String s)) {\n" + 
						"		} else {" + 
						"		}\n" + 
						"		System.out.println(s);\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X11.java (at line 7)\n" + 
				"	System.out.println(s);\n" + 
				"	                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X12 {\n" +
						"  	public static void foo(Object b) {\n" + 
						"		Object c = null;\n" + 
						"		if (b != c) {\n" + 
						"			if (b == null && (b instanceof String s)) {\n" + 
						"				System.out.println(\"s:\" + s);\n" + 
						"			} else { " + 
						"				System.out.println(\"b:\" + b);\n" + 
						"			}\n" + 
						"			s = null;\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X12.java (at line 10)\n" + 
				"	s = null;\n" + 
				"	^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X13 {\n" +
						"  	public static void foo(Object b) {\n" + 
						"		Object c = null;\n" + 
						"		if (b != c) {\n" + 
						"			if (b == null && (b instanceof String s))\n" + 
						"				System.out.println(\"s:\" + s);\n" + 
						"			else " + 
						"				System.out.println(\"b:\" + b);\n" + 
						"			System.out.println(s);\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X13.java (at line 9)\n" + 
				"	System.out.println(s);\n" + 
				"	                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (!(o instanceof String s)) {\n" + 
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X14.java (at line 5)\n" + 
				"	System.out.print(\"then:\" + s);\n" + 
				"	                           ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (!(o instanceof String s)) {\n" + 
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X14.java (at line 5)\n" + 
				"	System.out.print(\"then:\" + s);\n" + 
				"	                           ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (!!(o instanceof String s)) {\n" + 
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X14.java (at line 7)\n" + 
				"	System.out.print(\"else:\" + s);\n" + 
				"	                           ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (o == null) {\n" + 
						"			System.out.print(\"null\");\n" +
						"		} else if(!(o instanceof String s)) {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X14.java (at line 7)\n" + 
				"	System.out.print(\"else:\" + s);\n" + 
				"	                           ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014d() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (o == null) {\n" + 
						"			System.out.print(\"null\");\n" +
						"		} else if(!!(o instanceof String s)) {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"else:abcd",
				options);
	}
	public void test014e() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14a {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		 if (!(!(o instanceof String s))) {\n" + 
						"			System.out.print(\"s:\" + s);\n" +
						"		} else {\n" + 
						"		}\n" + 
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (!(o instanceof String s)) {\n" + 
						"			throw new IllegalArgumentException();\n" +
						"		} else {\n" + 
						"			System.out.print(\"s:\" + s);\n" + 
						"		}\n" + 
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15a {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (!(o instanceof String s)) {\n" + 
						"			throw new IllegalArgumentException();\n" +
						"		}\n" + 
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15b {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		if (!(o instanceof String s)) {\n" + 
						"			//throw new IllegalArgumentException();\n" +
						"		} else {\n" + 
						"			System.out.print(\"s:\" + s);\n" + 
						"		}\n" + 
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X15b.java (at line 9)\n" + 
				"	System.out.print(s);\n" + 
				"	                 ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X16 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		boolean b = (o instanceof String[] s && s.length == 1);\n" + 
						"		System.out.print(b);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(new String[]{\"one\"});\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				options);
	}
	public void test017() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X17.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X17 {\n" +
						"  	public static void foo(Object o) {\n" + 
						"		boolean b = (o instanceof String[] s && s.length == 1);\n" + 
						"		System.out.print(s[0]);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(new String[]{\"one\"});\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X17.java (at line 5)\n" + 
				"	System.out.print(s[0]);\n" + 
				"	                 ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X18 {\n" +
						"  public static void main(String[] obj) {\n" +
						"		boolean a = true;\n" +
						"		{\n" + 
						"			boolean b = (obj instanceof String[] s && s.length == 0);\n" + 
						"			System.out.print(b + \",\");\n" + 
						"		}\n" + 
						"		boolean b = a ? false : (obj instanceof String[] s && s.length == 0);\n" + 
						"		System.out.print(b);\n" +
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X19 {\n" +
						"  public static void main(String[] obj) {\n" +
						"		boolean a = true;\n" +
						"		boolean b = a ? false : (obj instanceof String[] s && s.length == 0);\n" + 
						"		System.out.print(b + \",\");\n" +
						"		{\n" + 
						"			b = (obj instanceof String[] s && s.length == 0);\n" + 
						"			System.out.print(b);\n" + 
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"false,true",
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test020() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X20.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X20 {\n" +
						"  public static void main(String[] o) {\n" +
						"		boolean b = (o instanceof String[] s) && s instanceof String[] s2;\n" + 
						"		System.out.print(b);\n" + 
						"	}\n" +
						"}\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X21 {\n" +
						"  public static void main(String[] o) {\n" +
						"		boolean b = (o instanceof String[] s) && s instanceof String[] s2;\n" + 
						"		System.out.print(s);\n" +
						"		System.out.print(s2);\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X21.java (at line 5)\n" + 
				"	System.out.print(s);\n" + 
				"	                 ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n" + 
				"2. ERROR in X21.java (at line 6)\n" + 
				"	System.out.print(s2);\n" + 
				"	                 ^^\n" + 
				"s2 cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while ((o instanceof String s) && s.length() > 0) {\n" + 
						"			o = s.substring(0, s.length() - 1);\n" +
						"			System.out.println(s);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"one\non\no",
				options);
	}
	/* Test pattern expressions in a while statement with break
	 */
	public void test023() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s) && s.length() > 0) {\n" + 
						"			System.out.println(s);\n" +
						"			break;\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X23.java (at line 7)\n" + 
				"	while (!(o instanceof String s) && s.length() > 0) {\n" + 
				"	                                   ^\n" + 
				"s cannot be resolved\n" + 
				"----------\n" + 
				"2. ERROR in X23.java (at line 8)\n" + 
				"	System.out.println(s);\n" + 
				"	                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test pattern expressions in a while statement with no break
	 */
	public void test023a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s) && s.length() > 0) {\n" + 
						"			System.out.println(s);\n" +
						"			//break;\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X23.java (at line 7)\n" + 
				"	while (!(o instanceof String s) && s.length() > 0) {\n" + 
				"	                                   ^\n" + 
				"s cannot be resolved\n" + 
				"----------\n" + 
				"2. ERROR in X23.java (at line 8)\n" + 
				"	System.out.println(s);\n" + 
				"	                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
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
						"@SuppressWarnings(\"preview\")\n" +
						"public class X24a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s)) {\n" + 
						"			throw new IllegalArgumentException();\n" +
						"		}\n" + 
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				options);
	}
	/* Test that we reject two patter variables with same name in the same
	 * equals expression
	 */
	public void test025() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X25.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X25 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" + 
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) != p instanceof String s) {\n" + 
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X25.java (at line 7)\n" + 
				"	if ((o instanceof String s) != p instanceof String s) {\n" + 
				"	                                                   ^\n" + 
				"Duplicate local variable s\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that we reject two patter variables with same name in the same
	 * equals expression
	 */
	public void test026() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X26 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" + 
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) == p instanceof String s) {\n" + 
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X26.java (at line 7)\n" + 
				"	if ((o instanceof String s) == p instanceof String s) {\n" + 
				"	                                                   ^\n" + 
				"Duplicate local variable s\n" + 
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test027() {
		runConformTest(
				new String[] {
						"X27.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X27 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {\n" + 
						"			System.out.println(s[i]);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"",
				getCompilerOptions(true));
	}
	public void test028() {
		runConformTest(
				new String[] {
						"X28.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X28 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\", \"two\"});\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {\n" + 
						"			System.out.println(s[i]);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	public void test029() {
		runConformTest(
				new String[] {
						"X29.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X29 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\", \"two\"});\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s) && s.length > 0 && i < s.length; i = (s != null ? i + 1 : i)) {\n" + 
						"			System.out.println(s[i]);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	/*
	 * Test that pattern variables are accepted in initialization of
	 * a for statement
	 * TODO: Javac rejects this
	 */
	public void test030() {
		runConformTest(
				new String[] {
						"X30.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X30 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0, length = (obj instanceof String s) ? s.length() : 0; i < length; i++) {\n" + 
						"			System.out.print(s.charAt(i));\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test031() {
		runNegativeTest(
				new String[] {
						"X31.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X31 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" + 
						"			System.out.println(s[i]);\n" +
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X31.java (at line 7)\n" + 
				"	for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" + 
				"	                                               ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n" + 
				"2. ERROR in X31.java (at line 7)\n" + 
				"	for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" + 
				"	                                                                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n" + 
				"3. ERROR in X31.java (at line 8)\n" + 
				"	System.out.println(s[i]);\n" + 
				"	                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test032() {
		runConformTest(
				new String[] {
						"X32.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X32 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" + 
						"		int i = 0;\n" + 
						"		switch(i) {\n" + 
						"		case 0:\n" + 
						"			res = (obj instanceof String s) ? s : null;\n" + 
						"		default:\n" + 
						"			break;\n" + 
						"		}\n" + 
						"		System.out.println(res);\n" + 
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	/*
	 * FAILURE: Javac rejects this code, but we accept.
	 */
	public void test033() {
		runConformTest(
				new String[] {
						"X33.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X33 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" + 
						"		int i = 0;\n" + 
						"		switch(i) {\n" + 
						"		case 0:\n" + 
						"			res = (obj instanceof String s) ? s : null;\n" + 
						"			res = s.substring(1);\n" + 
						"		default:\n" + 
						"			break;\n" + 
						"		}\n" + 
						"		System.out.println(res);\n" + 
						"	}\n" +
						"}\n",
				},
				"ne",
				getCompilerOptions(true));
	}
	public void test034() {
		runNegativeTest(
				new String[] {
						"X34.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X34 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" + 
						"		String result = switch(i) {\n" + 
						"			case 0 -> {\n" + 
						"				result = (obj instanceof String s) ? s : null;\n" + 
						"				yield result;\n" + 
						"			}\n" + 
						"			default -> {\n" + 
						"				yield result;\n" + 
						"			}\n" + 
						"		};\n" + 
						"		System.out.println(result);\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X34.java (at line 14)\n" + 
				"	yield result;\n" + 
				"	      ^^^^^^\n" + 
				"The local variable result may not have been initialized\n" + 
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test035() {
		runNegativeTest(
				new String[] {
						"X35.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X35 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" + 
						"		String result = switch(i) {\n" + 
						"			case 0 -> {\n" + 
						"				result = (obj instanceof String s) ? s : null;\n" + 
						"				yield s;\n" + 
						"			}\n" + 
						"			default -> {\n" + 
						"				yield s;\n" + 
						"			}\n" + 
						"		};\n" + 
						"		System.out.println(result);\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X35.java (at line 11)\n" + 
				"	yield s;\n" + 
				"	      ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n" + 
				"2. ERROR in X35.java (at line 14)\n" + 
				"	yield s;\n" + 
				"	      ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test036() {
		runConformTest(
				new String[] {
						"X36.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X36 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" + 
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" + 
						"		String result = switch(i) {\n" + 
						"			default -> {\n" + 
						"				result = (obj instanceof String s) ? s : null;\n" + 
						"				yield result;\n" + 
						"			}\n" + 
						"		};\n" + 
						"		System.out.println(result);\n" + 
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test037() {
		runNegativeTest(
				new String[] {
						"X37.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X37 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"abcd\"});\n" + 
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ; i++) {\n" + 
						"			System.out.println(s[i]);\n" + 
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X37.java (at line 8)\n" + 
				"	System.out.println(s[i]);\n" + 
				"	                   ^^^^\n" + 
				"The type of the expression must be an array type but it resolved to String\n" + 
				"----------\n",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test038() {
		runNegativeTest(
				new String[] {
						"X38.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X38 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"abcd\"});\n" + 
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ; i++) {\n" + 
						"			throw new IllegalArgumentException();\n" + 
						"		}\n" + 
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" + 
				"1. ERROR in X38.java (at line 10)\n" + 
				"	System.out.println(s);\n" + 
				"	                   ^\n" + 
				"s cannot be resolved to a variable\n" + 
				"----------\n",
				null,
				true,
				getCompilerOptions(true));
	}
	/*
	 * Failing with VerifyError
	 */
	public void _test039() {
		runConformTest(
				new String[] {
						"X39.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X39 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\"});;\n" + 
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; i < obj.length && (obj[i] instanceof String s) && i < s.length(); i++) {\n" + 
						"			System.out.println(s);\n" + 
						"		}\n" + 
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
}