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

public class SealedTypes15Tests extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug563178"};
	}

	public static Class<?> testClass() {
		return SealedTypes15Tests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public SealedTypes15Tests(String testName){
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
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

	public void testBug563430_001() {
		runConformTest(
			new String[] {
				"X.java",
				"non-sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n"
			},
			"0");
	}
	public void testBug563430_002() {
		runConformTest(
			new String[] {
				"X.java",
				"non-sealed interface I {}\n"+
				"non-sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n"
			},
			"0");
	}
	public void testBug562715_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     int sealed = 100;\n" +
				"     System.out.println(sealed);\n" +
				"  }\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_003() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_004() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"sealed public class X<T> {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	sealed public sealed class X {\n" +
			"	                           ^\n" +
			"Duplicate modifier for the type X\n" +
			"----------\n");
	}
	public void testBug562715_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public static sealed void main(String[] args){\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug562715_007() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed @MyAnnot public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"@interface MyAnnot {}\n"
			},
			"100");
	}
	public void testBug562715_008() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"class Y extends X {}\n"
			},
			"100");
	}
	public void testBug562715_009() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y,Z {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"class Y extends X {}\n" +
				"class Z extends X {}\n"
			},
			"100");
	}
	public void testBug562715_010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits {\n" +
			"	                      ^^^^^^^\n" +
			"Syntax error on token \"permits\", ClassType expected after this token\n" +
			"----------\n");
	}
	// TODO : Enable after error flag code implemented
	public void _testBug562715_011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed enum Natural {ONE, TWO}\n"+
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	EXPECTED ERROR IN RECORD\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void _testBug562715_xxx() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed record R() {}\n"+
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	EXPECTED ERROR IN RECORD\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
}