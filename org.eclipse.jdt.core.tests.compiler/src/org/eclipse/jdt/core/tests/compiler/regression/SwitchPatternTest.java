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
//		TESTS_NAMES = new String[] { "testBug573516_007"};
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
		runConformTest(testFiles, expectedOutput, "", getCompilerOptions());
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(testFiles, expectedOutput, "", customOptions);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, String errorOutput) {
		runConformTest(testFiles, expectedOutput, errorOutput, getCompilerOptions());
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, String expectedErrorOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.expectedErrorString = expectedErrorOutput;
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
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                ^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                          ^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_006() {
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
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s && s.length > 0, X x && x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s && s.length > 0, X x && x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                              ^^^^^^\n" +
			"length cannot be resolved or is not a field\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s && s.length > 0, X x && x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                                          ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" + 			"----------\n");
	}
	public void testBug573516_007() {
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
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for String\");\n" +
			"	                ^^^^^^\n" +
			"String cannot be resolved to a variable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for String\");\n" +
			"	                ^^^^^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_008() {
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
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n" +
			"	                ^^^^^^\n" +
			"String cannot be resolved to a variable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n" +
			"	                ^^^^^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
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
			"1. ERROR in X.java (at line 5)\n" +
			"	default : System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_010() {
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
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, default : System.out.println(\"Error should be flagged for String and default\");\n" +
			"	                       ^\n" +
			"A switch label may not have both a pattern case label element and a default case label element.\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	default : System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_011() {
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
			"1. ERROR in X.java (at line 4)\n" +
			"	case var s : System.out.println(\"Error should be ANY_PATTERN\");\n" +
			"	     ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
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
			"The constant case label element is not compatible with switch expression type Object\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}

	public void testBug573936_01() {
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
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case String s && s.length()>1: \n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern case label \n" +
				"----------\n");
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
				"1. ERROR in X.java (at line 5)\n" +
				"	case String s1: System.out.println(\"String \");\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern case label \n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
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
	public void testBug573939_03b() {
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

	public void testBug574549_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"    case default:\n"+
					"     System.out.println(\"Object: \" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Object: Hello World!");
	}
	public void testBug574549_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(Integer.valueOf(11));\n"+
					"   foo(Integer.valueOf(9));\n"+
					"   foo(\"Hello World!\");\n"+
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
					"   case default:\n"+
					"     System.out.println(\"Give Me Some SunShine:\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Greater than 10:11\n" +
				"Greater than 0:9\n" +
				"Give Me Some SunShine:Hello World!");
	}
	public void testBug574549_03() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i :\n"+
					"     System.out.println(\"Integer:\" + o);\n"+
					"     break;\n"+
					"   case default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   case default:\n"+
					"     System.out.println(\"Give me Some Sunshine\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	case default:\n" +
				"	           ^^\n" +
				"The default case is already defined\n" +
				"----------\n");
	}
	public void testBug574549_04() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i :\n"+
					"     System.out.println(\"Integer:\" + o);\n"+
					"     break;\n"+
					"   case default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   default:\n"+
					"     System.out.println(\"Give me Some Sunshine\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	default:\n" +
				"	^^^^^^^\n" +
				"The default case is already defined\n" +
				"----------\n");
	}
	// Test that when a pattern variable is unused and when the OPTION_PreserveUnusedLocal
	// option is used, no issue is reported at runtime.
	public void testBug573937_1() {
		Map<String,String> options = getCompilerOptions();
		String opt = options.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		try {
			options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.DISABLED);
			this.runConformTest(
				new String[] {
				"X.java",
					"public class X {\n"
						+ "	public static void main(String[] args) {\n"
						+ "		System.out.println(\"Hello\");\n"
						+ "	}\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
					},
					"Hello",
					options);
		} finally {
			options.put(CompilerOptions.OPTION_PreserveUnusedLocal, opt);
		}
	}
	// A simple pattern variable in a case is not visible in the
	// following case statement
	public void testBug573937_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				System.out.println(s);\n"
						+ "				break;\n"
						+ "			case Integer i:\n"
						+ "				System.out.println(s);\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Same as above, but without break statement
	public void testBug573937_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				System.out.println(s);\n"
						+ "			case Integer i:\n"
						+ "				System.out.println(s);\n"
						+ "			default:\n"
						+ "				System.out.println(s);\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that compiler rejects attempts to redeclare local variable
	// with same name as a pattern variable
	public void testBug573937_4() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				String s = null;\n"
						+ "				System.out.println(s);\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	String s = null;\n" +
				"	       ^\n" +
				"Duplicate local variable s\n" +
				"----------\n");
	}
	// Test that compiler allows local variable with same name as a
	// pattern variable in a different case statement
	public void testBug573937_5() {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
								+ "	public static void foo(Object o) {\n"
								+ "		switch (o) {\n"
								+ "			case String s:\n"
								+ "				System.out.println(s);\n"
								+ "				break;\n"
								+ "			default:\n"
								+ "				String s = null;\n"
								+ "				break;\n"
								+ "		}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(\"hello\");\n"
								+ "	}\n"
								+ "}",
				},
				"hello");
	}
	// Test that a pattern variable can't use name of an already existing local
	// variable
	public void testBug573937_6() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case Object o:\n"
						+ "				System.out.println(o);\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Object o:\n" +
				"	            ^\n" +
				"Duplicate local variable o\n" +
				"----------\n");
	}
	// Test that compiler rejects attempts to redeclare another pattern
	// variable (instanceof) with same name as that a pattern variable in
	// that case statement
	public void testBug573937_7() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s1:\n"
						+ "				if (o instanceof String s1) {\n"
						+ "					System.out.println(s1);\n"
						+ "				}\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (o instanceof String s1) {\n" +
				"	                        ^^\n" +
				"Duplicate local variable s1\n" +
				"----------\n");
	}
	// Test that when multiple case statements declare pattern variables
	// with same name, correct ones are used in their respective scopes.
	public void testBug573937_8() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s1:\n"
						+ "				System.out.println(s1.length());\n"
						+ "				break;\n"
						+ "			case Integer s1:\n"
						+ "				System.out.println(s1.length());\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	System.out.println(s1.length());\n" +
				"	                      ^^^^^^\n" +
				"The method length() is undefined for the type Integer\n" +
				"----------\n");
	}
	// Test that a pattern variable declared in the preceding case statement
	// can't be used in the case statement itself
	public void testBug573937_9() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case Integer i1:\n"
						+ "				break;\n"
						+ "			case String s1 && s1.length() > i1:\n"
						+ "					System.out.println(s1.length());\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String s1 && s1.length() > i1:\n" +
				"	                                ^^\n" +
				"i1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that redefining pattern variables with null is allowed
	// and produce expected result (NPE) when run.
	public void testBug573937_10() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"public class X {\n"
				+ "@SuppressWarnings(\"null\")"
				+ "	public static void foo(Object o) {\n"
				+ "		switch (o) {\n"
				+ "			case String s1 && s1.length() == 0:\n"
				+ "					break;"
				+ "			case String s1:\n"
				+ "					s1 = null;\n"
				+ "					System.out.println(s1.length());\n"
				+ "				break;\n"
				+ "			default:\n"
				+ "				break;\n"
				+ "		}\n"
				+ "	}\n"
				+ "	public static void main(String[] args) {\n"
				+ "		foo(\"hello\");\n"
				+ "	}\n"
				+ "}",
		};
		runner.expectedErrorString = "Exception in thread \"main\" java.lang.NullPointerException: Cannot invoke \"String.length()\" because \"s1\" is null\n" +
										"	at X.foo(X.java:7)\n" +
										"	at X.main(X.java:14)";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	// Test that a pattern variable is allowed in a switch label throw
	// statement and when run, produces expected result
	public void testBug573937_11() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"public class X {\n"
				+ "	public static void foo(Object o) throws Exception {\n"
				+ "		switch (o) {\n"
				+ "			case String s1:\n"
				+ "				throw new Exception(s1);\n"
				+ "			default:\n"
				+ "				break;\n"
				+ "		}\n"
				+ "	}\n"
				+ "	public static void main(String[] args) throws Exception {\n"
				+ "		foo(\"hello\");\n"
				+ "	}\n"
				+ "} ",
		};
		runner.expectedErrorString = "Exception in thread \"main\" java.lang.Exception: hello\n" +
				"	at X.foo(X.java:5)\n" +
				"	at X.main(X.java:11)";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	// A non effectively final referenced from the RHS of the guarding expression
	// is reported by the compiler.
	public void testBug574612_1() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 && o1.length() > len:\n"
						+ "			len = 0;\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String o1 && o1.length() > len:\n" +
				"	                                ^^^\n" +
				"Local variable len referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	// A non effectively final referenced from the LHS of the guarding expression
	// is reported by the compiler.
	public void testBug574612_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 && len < o1.length():\n"
						+ "			len = 0;\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String o1 && len < o1.length():\n" +
				"	                  ^^^\n" +
				"Local variable len referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	// An explicitly final local variable, also referenced in a guarding expression of a pattern
	// and later on re-assigned is only reported for the explicit final being modified
	public void testBug574612_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		final int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 && len < o1.length():\n"
						+ "			len = 0;\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	len = 0;\n" +
				"	^^^\n" +
				"The final local variable len cannot be assigned. It must be blank and not using a compound assignment\n" +
				"----------\n");
	}
	public void testBug574612_4() {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 && len < o1.length():\n"
						+ "			System.out.println(o1);\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "	public static void main(String[] args) throws Exception {\n"
						+ "		foo(\"hello\");\n"
						+ "	}\n"
						+ "} ",
				},
				"hello");
	}
	public void testBug574719_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100 ));\n"+
				" }\n"+
				"}",
			},
			"1");
	}
	public void testBug574719_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0, default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100 ));\n"+
				" }\n"+
				"}",
			},
			"1");
	}
	public void testBug574719_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"1");
	}
	public void testBug574719_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0  : k = 2; break;\n"+
				"     case default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"2");
	}
	public void testBug574719_005() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0  : k = 2; break;\n"+
				"     case 1, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"2");
	}
	public void testBug574719_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0  : k = 2; break;\n"+
				"     case 1, default, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	case 1, default, default   : k = 1;\n" +
			"	                       ^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574719_007() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 10, default: k = 1;break;\n"+
				"     case 0  : k = 2; break;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				"   System.out.println(foo(10));\n"+
				" }\n"+
				"}",
			},
			"1\n"+
			"2\n"+
			"1");
	}
	public void testBug574561_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     default, default  : k = 2; break;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	default, default  : k = 2; break;\n" +
			"	       ^\n" +
			"Syntax error on token \",\", : expected\n" +
			"----------\n");
	}
	public void testBug574561_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case default, 1, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	                       ^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574561_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case default, 1, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	                       ^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574793_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(int o) {\n"+
				"   switch (o) {\n"+
				"     case null  -> System.out.println(\"null\");\n"+
				"     case 20  -> System.out.println(\"20\");\n"+
				"   }\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"   case \"F\"  :\n"+
				"     break;\n"+
				"   case 2 :\n"+
				"     break;\n"+
				"   default:\n"+
				"     break;\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case null  -> System.out.println(\"null\");\n" +
			"	     ^^^^\n" +
			"Type mismatch: cannot convert from null to int\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	case \"F\"  :\n" +
			"	     ^^^\n" +
			"The constant case label element is not compatible with switch expression type Object\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	case 2 :\n" +
			"	     ^\n" +
			"The constant case label element is not compatible with switch expression type Object\n" +
			"----------\n");
	}
	public void testBug574559_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case 1, Integer i  -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case 1, Integer i  -> System.out.println(o);\n" +
			"	        ^^^^^^^^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n");
	}
	public void testBug574559_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  Integer i, 30  -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case  Integer i, 30  -> System.out.println(o);\n" +
			"	                 ^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has one statement)
	public void testBug573940_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"public void foo(Number n) {\n"
					+ "	switch (n) {\n"
					+ "	case Integer i :\n"
					+ "		System.out.println(i);\n"
					+ "	case Float f :\n"
					+ "		System.out.println(f);\n"
					+ "	}\n"
					+ "}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Float f :\n" +
				"	^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern case label \n" +
				"----------\n");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has zero statement)
	public void testBug573940_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"public void foo(Number n) {\n"
					+ "	switch (n) {\n"
					+ "	case Integer i :\n"
					+ "	case Float f :\n"
					+ "		System.out.println(f);\n"
					+ "     break;\n"
					+ "	}\n"
					+ "}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case Float f :\n" +
				"	^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern case label \n" +
				"----------\n");
	}
	// Test that falling through from a pattern to a default is allowed
	public void testBug573940_3() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "public static void foo(Number n) {\n"
					+ "		switch (n) {\n"
					+ "		case Integer i :\n"
					+ "			System.out.println(i);\n"
					+ "		default:\n"
					+ "			System.out.println(\"null\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "public static void main(String[] args) {\n"
					+ "		foo(Integer.valueOf(5));\n"
					+ "	}\n"
					+ "}",
				},
				"5\n" +
				"null");
	}
	// Test that a case statement with pattern is allowed when statement group ends
	// with an Throw statement instead of a break statement
	public void testBug573940_4() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "public static void foo(Number n) {\n"
					+ "		switch (n) {\n"
					+ "		case Integer i :\n"
					+ "			throw new IllegalArgumentException();\n"
					+ "		default:\n"
					+ "			System.out.println(\"null\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "public static void main(String[] args) {\n"
					+ "		try{\n"
					+ "			foo(Integer.valueOf(5));\n"
					+ "		} catch(Exception t) {\n"
					+ "		 	t.printStackTrace();\n"
					+ "		}\n"
					+ "	}\n"
					+ "}",
				},
				"",
				"java.lang.IllegalArgumentException\n" +
				"	at X.foo(X.java:5)\n" +
				"	at X.main(X.java:12)");
	}
	// Test that switch expression with pattern variables is reported when a case statement
	// doesn't return any value.
	public void testBug573940_5() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	public static void foo(Number n) {\n"
					+ "		int j = \n"
					+ "			switch (n) {\n"
					+ "			case Integer i -> {\n"
					+ "			}\n"
					+ "			default -> {\n"
					+ "				yield 1;\n"
					+ "			}\n"
					+ "		};\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	}\n" +
				"	^^\n" +
				"A switch labeled block in a switch expression should not complete normally\n" +
				"----------\n");
	}
	public void testBug574564_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(new String(\"Hello\"));\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case var i  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n");
	}
	public void testBug574564_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(new String(\"Hello\"));\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case var i, var j, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	            ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	            ^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                   ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                   ^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n");
	}
	public void testBug574564_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case var i, 10  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i, 10  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, 10  -> System.out.println(0);\n" +
			"	            ^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n");
	}
	public void testBug574564_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case var i, 10, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	            ^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	                ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	                ^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n");
	}
	public void testBug574564_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  10, null, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  10, null, var k  -> System.out.println(0);\n" +
			"	                ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  10, null, var k  -> System.out.println(0);\n" +
			"	                ^^^^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n");
	}
	public void testBug574564_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  default, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  default, var k  -> System.out.println(0);\n" +
			"	               ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  default, var k  -> System.out.println(0);\n" +
			"	               ^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element.\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	default -> System.out.println(o);\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574564_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  default, default, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	                     ^^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	                        ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	                        ^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element.\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	default -> System.out.println(o);\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574564_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  default, 1, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  default, 1, var k  -> System.out.println(0);\n" +
			"	                  ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  default, 1, var k  -> System.out.println(0);\n" +
			"	                  ^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element.\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case  default, 1, var k  -> System.out.println(0);\n" +
			"	                  ^^^^^\n" +
			"Constant case label elements and pattern case label elements cannot be present in a switch label\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	default -> System.out.println(o);\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574564_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s, default, Integer i  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                      ^\n" +
			"A switch label may not have both a pattern case label element and a default case label element.\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                        ^^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element.\n" +
			"----------\n");
	}
	public void testBug574564_010() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s, default, Integer i  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	            ^\n" +
			"Syntax error on token \"s\", delete this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                                ^\n" +
			"Syntax error on token \"i\", delete this token\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testBug574564_011() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case null  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case null  -> System.out.println(0);\n" +
			"	     ^^^^\n" +
			"Pattern Matching in Switch is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testBug574564_012() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case 1, default, null  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case 1, default, null  -> System.out.println(0);\n" +
			"	               ^\n" +
			"Pattern Matching in Switch is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case 1, default, null  -> System.out.println(0);\n" +
			"	                 ^^^^\n" +
			"Pattern Matching in Switch is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testBug574564_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case default, default -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case default, default -> System.out.println(0);\n" +
			"	              ^^^^^^^\n" +
			"Syntax error on token \"default\", -> expected after this token\n" +
			"----------\n");
	}
	public void testBug574563_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case null, null  -> System.out.println(o);\n"+
				"     default  -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case null, null  -> System.out.println(o);\n" +
			"	^^^^^^^^^^^^^^^\n" +
			"Duplicate case\n" +
			"----------\n");
	}
	public void testBug574563_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case null, Integer i  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574563_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i, null  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574563_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case null, Integer i && i > 10 -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case null, Integer i && i > 10 -> System.out.println(0);\n" +
			"	           ^^^^^^^^^^^^^^^^^^^\n" +
			"A null case label and patterns can co-exist only if the pattern is a type pattern\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574563_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i && i > 10, null  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer i && i > 10, null  -> System.out.println(0);\n" +
			"	                          ^^^^\n" +
			"A null case label and patterns can co-exist only if the pattern is a type pattern\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug575030_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(String o) {\n"+
					"   switch (o) {\n"+
					"     case String s -> System.out.println(s);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Hello World!");
	}
}
