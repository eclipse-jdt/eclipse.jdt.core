/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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

public class SwitchPatternTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug574525_01"};

	}

	private static String previewLevel = "17";

	public static Class<?> testClass() {
		return SwitchPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public SwitchPatternTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel) :
			JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel, javacAdditionalTestOptions);
		runner.runWarningTest();
	}

	// TODO: Change the error messages after implementing post grammar processing parts
	public void testBug573516_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i     -> System.out.println(\"String:\");\n"+
				"     case String s     -> System.out.println(\"String: Hello World!\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				" }\n"+
				"}",
			},
			"String: Hello World!");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void testBug573516_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s     -> System.out.println(\"String:\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void testBug573516_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s  : System.out.println(\"String:\"); break;\n"+
				"     case Integer i  : System.out.println(\"Integer:\");break;\n"+
				"     default       : System.out.println(\"Object\");break;\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void testBug573516_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t && t > 0 -> System.out.println(\"Integer && t > 0\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void testBug573516_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void _testBug573516_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t, String s && s.length > 0, X x && x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void _testBug573516_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t, String : System.out.println(\"Error should be flagged for String\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void _testBug573516_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	public void testBug573516_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case default : System.out.println(\"Default\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case default : System.out.println(\"Default\");\n" +
			"	             ^\n" +
			"Type mismatch: cannot convert from void to int\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	// Enable after code gen is fixed for switch pattern case default
	public void _testBug573516_010() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case String s, default : System.out.println(\"Error should be flagged for String and default\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	// Enable after code gen is fixed for switch pattern case default
	public void _testBug573516_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case var s : System.out.println(\"Error should be ANY_PATTERN\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	// TODO: Change the error messages after implementing post grammar processing parts
	// Enable after code gen is fixed for switch pattern case default
	public void testBug574228_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case 1: System.out.println(\"Integer\"); break;\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				"   public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"     Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case 1: System.out.println(\"Integer\"); break;\n" +
			"	     ^\n" +
			"Type mismatch: cannot convert from int to Object\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}

	public void _testBug573936_01() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"     case String s && s.length()>1: \n"+
					"       System.out.println(\"String s && s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;// error no fallthrough allowed in pattern\n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"     foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"ERROR: NO FALL THROUGH ALLOWED IN PATTERN CASES");
		}
	public void testBug573939_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer s : System.out.println(\"Integer\");\n"+
					"     case String s1: System.out.println(\"String \");\n"+
					"     default : System.out.println(\"Object\");\n"+
					"   }\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World\");\n"+
					"   Zork();\n"+
					" }\n"+
					"}\n"+
					"class Y {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n");
		}
	public void testBug573939_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: System.out.println(\"Integer\"); break;\n"+
					"     case String s && s.length()>1: System.out.println(\"String > 1\"); break;\n"+
					"     case String s1: System.out.println(\"String\"); break;\n"+
					"     case X x: System.out.println(\"X\"); break;\n"+
					"     default : System.out.println(\"Object\");\n"+
					"   }\n"+
					" }\n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"   foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String > 1\n" +
				"String\n" +
				"Object");
		}
	public void testBug573939_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case String s && s.length()>1: \n"+
					"       System.out.println(\"String s && s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;\n"+
					"     case String s1: \n"+
					"       System.out.println(\"String\"); \n"+
					"       System.out.println(s1);\n"+
					"       break; \n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"     foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String s && s.length()>1\n" +
				"Hello World!\n" +
				"String\n" +
				"H\n" +
				"Object");
		}
	// same local variable name in case pattern - scope
	// TODO: Bug 573937 to take care
	public void _testBug573939_03b() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case String s && s.length()>1: \n"+
					"       System.out.println(\"String s && s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;\n"+
					"     case String s: \n"+
					"       System.out.println(\"String\"); \n"+
					"       System.out.println(s);\n"+
					"       break; \n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"     foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String s && s.length()>1\n" +
				"Hello World!\n" +
				"String\n" +
				"H\n" +
				"Object");
		}
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    switch (args.length) {\n" +
				"      case 1:\n" +
				"        final int j = 1;\n" +
				"      case 2:\n" +
				"        switch (5) {\n" +
				"          case j:\n" +
				"        }\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	case j:\n" +
			"	     ^\n" +
			"The local variable j may not have been initialized\n" +
			"----------\n");
	}
	public void testBug574525_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"     foo(null);\n"+
					" }\n"+
					"}",
				},
				"NULL");
		}
	public void testBug574525_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case String s && s.length()>1: \n"+
					"       System.out.println(\"String s && s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;\n"+
					"     case String s1: \n"+
					"       System.out.println(\"String\"); \n"+
					"       System.out.println(s1);\n"+
					"       break; \n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"   foo(null);\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String s && s.length()>1\n" +
				"Hello World!\n" +
				"NULL\n" +
				"Object");
		}
	public void testBug574525_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Integer o) {\n"+
					"   switch (o) {\n"+
					"     case 10: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(o); \n"+
					"       break; \n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(o); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"     foo(0);\n"+
					" }\n"+
					"}",
				},
				"0");
		}
	public void testBug574525_04() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(int o) {\n"+
					"   switch (o) {\n"+
					"     case 10: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(o); \n"+
					"       break; \n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(o); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"     foo(0);\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case null:\n" +
				"	     ^^^^\n" +
				"Type mismatch: cannot convert from null to int\n" +
				"----------\n");
		}
	public void testBug574538_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(Integer.valueOf(11));\n"+
					"   foo(Integer.valueOf(9));\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i && i>10:\n"+
					"     System.out.println(\"Greater than 10:\" + o);\n"+
					"     break;\n"+
					"   case Integer j && j>0:\n"+
					"     System.out.println(\"Greater than 0:\" + o);\n"+
					"     break;\n"+
					"   default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Greater than 10:11\n" +
				"Greater than 0:9");
		}
	public void testBug574538_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo1(Integer.valueOf(10));\n"+
					"   foo1(Integer.valueOf(11));\n"+
					"   foo1(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo1(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i&&i>10 -> System.out.println(\"Greater than 10:\");\n"+
					"   case String s&&s.equals(\"ff\") -> System.out.println(\"String:\" + s);\n"+
					"   default -> System.out.println(\"Object:\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Object:10\n" +
				"Greater than 10:\n" +
				"Object:Hello World!");
		}
}