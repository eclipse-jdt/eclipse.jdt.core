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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class LocalStaticsTest_15 extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug566715_003"};
	}

	public static Class<?> testClass() {
		return LocalStaticsTest_15.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public LocalStaticsTest_15(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
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
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("15");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("15"));
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
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("15") :
			JavacTestOptions.forReleaseWithPreview("15", javacAdditionalTestOptions);
		runner.runWarningTest();
	}

	@SuppressWarnings("unused")
	private static void verifyClassFile(String expectedOutput, String classFileName, int mode)
			throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}

	public void testBug566284_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   interface F {\n"+
				"     static int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(F.create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}


	public void testBug566284_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   record R() {\n"+
				"     static int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(R.create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}
	public void testBug566284_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" static int si;\n"+
				" int nsi;\n"+
				"\n"+
				" void m() {\n"+
				"   int li;\n"+
				"\n"+
				"   interface F {\n"+
				"     static int fi = 0;\n"+
				"\n"+
				"     default void foo(int i) {\n"+
				"       System.out.println(li); // error, local variable of method of outer enclosing class\n"+
				"       System.out.println(nsi); // error, non-static member\n"+
				"       System.out.println(fi); // ok, static member of current class\n"+
				"       System.out.println(si); // ok, static member of enclosing class\n"+
				"       System.out.println(i); // ok, local variable of current method\n"+
				"     }\n"+
				"\n"+
				"     static void bar(int lo) {\n"+
				"       int k = lo; // ok\n"+
				"       int j = fi; // ok\n"+
				"       I myI = s -> lo; // ok, local var of method\n"+
				"     }\n"+
				"\n"+
				"     static void bar2(int lo) {\n"+
				"       I myI = s -> li; // error - local var of outer class\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	System.out.println(li); // error, local variable of method of outer enclosing class\n" +
			"	                   ^^\n" +
			"Cannot make a static reference to the non-static variable li from a local record\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	System.out.println(nsi); // error, non-static member\n" +
			"	                   ^^^\n" +
			"Cannot make a static reference to the non-static field nsi\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 26)\n" +
			"	I myI = s -> li; // error - local var of outer class\n" +
			"	             ^^\n" +
			"Cannot make a static reference to the non-static variable li from a local record\n" +
			"----------\n"
			);
	}

	public void testBug566518_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   int f = switch (5) {\n"+
				"			case 5: {\n"+
				"				interface I{\n"+
				"					\n"+
				"				}\n"+
				"				class C implements I{\n"+
				"					public int j = 5;\n"+
				"				}\n"+
				"				\n"+
				"				yield new C().j;\n"+
				"			}\n"+
				"			default:\n"+
				"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
				"			};\n"+
				"	System.out.println(f);\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}"
			},
			"5");
	}

	public void testBug566518_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   class F {\n"+
				"     int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(new F().create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}

	public void testBug566518_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public interface X {\n"+
				" static void foo() {\n"+
				"   class F {\n"+
				"     int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(new F().create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}

	public void testBug566518_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public interface X {\n"+
				" static void foo() {\n"+
				"   interface F {\n"+
				"     static int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(F.create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}

	public void testBug566518_005() {
		runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo() {\n"+
					"   int f = switch (5) {\n"+
					"			case 5: {\n"+
					"				interface I{\n"+
					"					\n"+
					"				}\n"+
					"				class C implements I{\n"+
					"					public int j = 5;\n"+
					"				}\n"+
					"				\n"+
					"				yield new C().j;\n"+
					"			}\n"+
					"			default:\n"+
					"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
					"			};\n"+
					"	System.out.println(f);\n"+
					"	class C1 implements I{\n"+
					"		public int j = 5;\n"+
					"	}\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   X.foo();\n"+
					" }\n"+
					"}\n"
			},
			"----------\n"+
			"1. ERROR in X.java (at line 18)\n"+
			"	class C1 implements I{\n"+
			"	                    ^\n" +
		    "I cannot be resolved to a type\n"+
		 	"----------\n"
			);
	}

	public void testBug566518_006() {
		runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n"+
				" A, B, C;\n"+
				" public void foo() {\n"+
				"   int f = switch (5) {\n"+
				"			case 5: {\n"+
				"				interface I{\n"+
				"					\n"+
				"				}\n"+
				"				class C implements I{\n"+
				"					public int j = 5;\n"+
				"				}\n"+
				"				\n"+
				"				yield new C().j;\n"+
				"			}\n"+
				"			default:\n"+
				"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
				"			};\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   X x = X.A;\n"+
				"	System.out.println();\n"+
				" }\n"+
				"}"
			},
			"");
	}
	// 6.5.5.1
	public void testBug566715_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				" static void foo() {\n"+
				"	interface I {\n"+
				"		X<T> supply();\n"+
				"	}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n"+
			"1. WARNING in X.java (at line 3)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	X<T> supply();\n" +
			"	  ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
		 	"----------\n"
			);
	}
	// 6.5.5.1
	public void testBug566715_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	interface I {\n"+
				"		X<T> supply();\n"+
				"	}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n"+
			"1. WARNING in X.java (at line 3)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	X<T> supply();\n" +
			"	  ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
		 	"----------\n"
			);
	}
	// 6.5.5.1
	public void testBug566715_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	record R(X<T> x) {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	record R(X<T> x) {}\n" +
			"	           ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
		 	"----------\n"
			);
	}
	// 9.1.1/14.3
	public void testBug566720_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	public interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	public interface I {}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1/14.3
	public void testBug566720_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	private interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	private interface I {}\n" +
			"	                  ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1
	public void testBug566720_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	protected interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	protected interface I {}\n" +
			"	                    ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1
	public void testBug566720_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	final interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	final interface I {}\n" +
			"	                ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1
	public void testBug566720_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	static interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	static interface I {}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
}