/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SealedTypesTests extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug564498_6"};
	}

	public static Class<?> testClass() {
		return SealedTypesTests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public SealedTypesTests(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
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
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("17");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("17"));
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
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") :
			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
		runner.runWarningTest();
	}

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
				"""
					sealed class Y permits X{}
					non-sealed class X extends Y {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					""",
			},
			"0");
	}
	public void testBug563430_001a() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					non-sealed class X extends Y {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					""",
				"Y.java",
				"sealed class Y permits X{}\n",
			},
			"0");
	}
	public void testBug563430_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I extends SI{}
					non-sealed class X implements SI{
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					sealed interface SI permits X, I{}
					non-sealed interface I2 extends I{}
					"""
			},
			"0");
	}
	public void testBug562715_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed class X permits Y {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					non-sealed class Y extends X {
					}
					"""
			},
			"100");
	}
	public void testBug562715_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public sealed class X {
					  public static void main(String[] args){
					     int sealed = 100;
					     System.out.println(sealed);
					  }
					}
					non-sealed class Y extends X {
					}
					"""
			},
			"100");
	}
	public void testBug562715_003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed public class X {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					non-sealed class Y extends X {
					}
					"""
			},
			"100");
	}
	public void testBug562715_004() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {}
					sealed public class X<T> {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					non-sealed class Y<T> extends X<T> {
					}
					non-sealed interface I2 extends I {}
					"""
			},
			"100");
	}
	public void testBug562715_004a() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed public class X<T> {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					non-sealed class Y extends X {
					}
					"""
			},
			"100");
	}
	public void testBug562715_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed public sealed class X {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					sealed public sealed class X {
					                           ^
				Duplicate modifier for the type X
				----------
				2. ERROR in X.java (at line 1)
					sealed public sealed class X {
					                           ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				""");
	}
	public void testBug562715_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X {
					  public static sealed void main(String[] args){
					     System.out.println(100);
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed class X {
					                    ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				2. ERROR in X.java (at line 2)
					public static sealed void main(String[] args){
					              ^^^^^^
				Syntax error on token "sealed", static expected
				----------
				""");
	}
	public void testBug562715_007() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed @MyAnnot public class X {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					@interface MyAnnot {}
					non-sealed class Y extends X{}"""
			},
			"100");
	}
	public void testBug562715_008() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed class X permits Y {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					sealed class Y extends X {}
					final class Z extends Y {}
					"""
			},
			"100");
	}
	public void testBug562715_009() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed class X permits Y,Z {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					sealed class Y extends X {}
					final class Z extends X {}
					final class Y2 extends Y {}
					"""
			},
			"100");
	}
	public void testBug562715_010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X permits {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed class X permits {
					                    ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				2. ERROR in X.java (at line 1)
					public sealed class X permits {
					                      ^^^^^^^
				Syntax error on token "permits", { expected
				----------
				3. ERROR in X.java (at line 1)
					public sealed class X permits {
					                              ^
				Syntax error, insert "}" to complete Block
				----------
				""");
	}
	// TODO : Enable after error flag code implemented
	public void testBug562715_011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed enum Natural {ONE, TWO}
					public sealed class X {
					  public static sealed void main(String[] args){
					     System.out.println(100);
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					sealed enum Natural {ONE, TWO}
					            ^^^^^^^
				Illegal modifier for the enum Natural; only public is permitted
				----------
				2. ERROR in X.java (at line 2)
					public sealed class X {
					                    ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				3. ERROR in X.java (at line 3)
					public static sealed void main(String[] args){
					              ^^^^^^
				Syntax error on token "sealed", static expected
				----------
				""");
	}
	public void testBug562715_xxx() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed record R() {}
					public sealed class X {
					  public static sealed void main(String[] args){
					     System.out.println(100);
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					sealed record R() {}
					              ^
				Illegal modifier for the record R; only public, final and strictfp are permitted
				----------
				2. ERROR in X.java (at line 2)
					public sealed class X {
					                    ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				3. ERROR in X.java (at line 3)
					public static sealed void main(String[] args){
					              ^^^^^^
				Syntax error on token "sealed", static expected
				----------
				""");
	}
	public void testBug563806_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X permits Y, Z{
					}
					class Y {}
					class Z {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed class X permits Y, Z{
					                              ^
				Permitted class Y does not declare X as direct super class
				----------
				2. ERROR in X.java (at line 1)
					public sealed class X permits Y, Z{
					                                 ^
				Permitted class Z does not declare X as direct super class
				----------
				""");
	}
	public void testBug563806_002() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed class X permits Y{
					}
					class Y {}
					class Z extends X{}""",
				"p1/A.java",
				"package p1;\n"+
				"public sealed class A extends X{}",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed class X permits Y{
					                              ^
				Permitted class Y does not declare p1.X as direct super class
				----------
				2. ERROR in p1\\X.java (at line 5)
					class Z extends X{}
					      ^
				The class Z with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
				----------
				3. ERROR in p1\\X.java (at line 5)
					class Z extends X{}
					                ^
				The type Z extending a sealed class X should be a permitted subtype of X
				----------
				----------
				1. ERROR in p1\\A.java (at line 2)
					public sealed class A extends X{}
					                    ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares A as its direct superclass or superinterface
				----------
				2. ERROR in p1\\A.java (at line 2)
					public sealed class A extends X{}
					                              ^
				The type A extending a sealed class X should be a permitted subtype of X
				----------
				""");
	}
	public void testBug563806_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed interface X permits Y, Z{
					}
					class Y implements X{}
					class Z {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed interface X permits Y, Z{
					                                     ^
				Permitted type Z does not declare X as direct super interface\s
				----------
				2. ERROR in X.java (at line 3)
					class Y implements X{}
					      ^
				The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
				----------
				""");
	}
	public void testBug563806_004() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, Z, Q{
					}
					class Y implements X{}
					interface Z {}""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, Z, Q{
					                                     ^
				Permitted type Z does not declare p1.X as direct super interface\s
				----------
				2. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, Z, Q{
					                                        ^
				Q cannot be resolved to a type
				----------
				3. ERROR in p1\\X.java (at line 4)
					class Y implements X{}
					      ^
				The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
				----------
				""");
	}
	public void testBug563806_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X permits Y, Y{
					}
					class Y extends X {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed class X permits Y, Y{
					                                 ^
				Duplicate type Y for the type X in the permits clause
				----------
				2. ERROR in X.java (at line 3)
					class Y extends X {}
					      ^
				The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
				----------
				""");
	}
	public void testBug563806_006() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed class X permits Y, p1.Y{
					}
					class Y extends X {}""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed class X permits Y, p1.Y{
					                                 ^^^^
				Duplicate type Y for the type X in the permits clause
				----------
				2. ERROR in p1\\X.java (at line 4)
					class Y extends X {}
					      ^
				The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
				----------
				""");
	}
	public void testBug563806_007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					}
					non-sealed class Y extends X {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					non-sealed class Y extends X {}
					                 ^
				A class Y declared as non-sealed should have either a sealed direct superclass or a sealed direct superinterface
				----------
				""");
	}
	public void testBug563806_008() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y {
					}
					class Y implements X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"non-sealed public interface Y {}",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 4)
					class Y implements X{}
					      ^
				The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
				----------
				----------
				1. ERROR in p2\\Y.java (at line 2)
					non-sealed public interface Y {}
					                            ^
				An interface Y declared as non-sealed should have a sealed direct superinterface
				----------
				""");
	}
	public void testBug563806_009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public sealed class X {
					  public static void main(String[] args){
					     System.out.println(100);
					  }
					}
					final class Y extends X {}""",
			},
			"100");
	}
	public void testBug563806_010() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed class X permits Y {
					}
					final class Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			},
			"""
				----------
				1. ERROR in p2\\Y.java (at line 2)
					public final class Y extends p1.X{}
					                             ^^^^
				Sealed type X and sub type Y in an unnamed module should be declared in the same package p1
				----------
				""");
	}
	public void testBug563806_011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {\s
					    System.out.println("0");
					  }
					}
					sealed interface Y {
					}
					final class Z implements Y {}""",
			},
			"0");
	}
	public void testBug563806_012() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y {
					}
					final class Y implements X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			},
			"""
				----------
				1. ERROR in p2\\Y.java (at line 2)
					public final class Y implements p1.X{}
					                                ^^^^
				Sealed type X and sub type Y in an unnamed module should be declared in the same package p1
				----------
				""");
	}
	public void testBug563806_013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed interface X {
					}
					interface Y extends X {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					interface Y extends X {}
					          ^
				The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed
				----------
				""");
	}
	public void testBug563806_014() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y {
					}
					interface Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public interface Y extends p1.X{}",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 4)
					interface Y extends X{}
					          ^
				The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed
				----------
				----------
				1. ERROR in p2\\Y.java (at line 2)
					public interface Y extends p1.X{}
					                 ^
				The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed
				----------
				2. ERROR in p2\\Y.java (at line 2)
					public interface Y extends p1.X{}
					                           ^^^^
				Sealed type X and sub type Y in an unnamed module should be declared in the same package p1
				----------
				""");
	}
	public void testBug563806_015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X permits Y{
					}
					final class Y extends X {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X permits Y{
					             ^
				A type declaration X that has a permits clause should have a sealed modifier
				----------
				""");
	}
	public void testBug563806_016() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public class X permits Y {
					}
					final class Y extends X{}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public class X permits Y {
					             ^
				A type declaration X that has a permits clause should have a sealed modifier
				----------
				""");
	}
	public void testBug563806_017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public interface X permits Y{
					}
					final class Y implements X {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public interface X permits Y{
					                 ^
				A type declaration X that has a permits clause should have a sealed modifier
				----------
				""");
	}
	public void testBug563806_018() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public interface X permits Y {
					}
					final class Y implements X{}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public interface X permits Y {
					                 ^
				A type declaration X that has a permits clause should have a sealed modifier
				----------
				""");
	}
	public void testBug563806_019() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed class X permits Y, p2.Y {
					}
					final class Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed class X permits Y, p2.Y {
					                                 ^^^^
				Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X
				----------
				""");
	}
	public void testBug563806_020() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, p2.Y {
					}
					final class Y implements X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, p2.Y {
					                                     ^^^^
				Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X
				----------
				""");
	}
	public void testBug563806_021() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, p2.Y {
					}
					non-sealed interface Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public non-sealed interface Y extends p1.X{}",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, p2.Y {
					                                     ^^^^
				Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X
				----------
				""");
	}
	public void testBug563806_022() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"""
					module mod.one {
					requires mod.two;
					}
					""",
				"mod.two/module-info.java",
				"""
					module mod.two {
					exports p2;
					}
					""",
				"p1/X.java",
				"""
					package p1;
					public sealed class X permits Y, p2.Y {
					}
					final class Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y {}",
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed class X permits Y, p2.Y {
					                                 ^^^^
				Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X
				----------
				2. ERROR in p1\\X.java (at line 2)
					public sealed class X permits Y, p2.Y {
					                                 ^^^^
				Permitted class Y does not declare p1.X as direct super class
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug563806_023() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"""
					module mod.one {
					requires mod.two;
					}
					""",
				"mod.two/module-info.java",
				"""
					module mod.two {
					exports p2;
					}
					""",
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, p2.Y {
					}
					final class Y implements X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y {}",
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, p2.Y {
					                                     ^^^^
				Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X
				----------
				2. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, p2.Y {
					                                     ^^^^
				Permitted type Y does not declare p1.X as direct super interface\s
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug563806_024() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"""
					module mod.one {
					requires mod.two;
					}
					""",
				"mod.two/module-info.java",
				"""
					module mod.two {
					exports p2;
					}
					""",
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, p2.Y {
					}
					non-sealed interface Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public interface Y {}",
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, p2.Y {
					                                     ^^^^
				Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X
				----------
				2. ERROR in p1\\X.java (at line 2)
					public sealed interface X permits Y, p2.Y {
					                                     ^^^^
				Permitted type Y does not declare p1.X as direct super interface\s
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug563806_025() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.one", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"""
					package p1;
					public sealed class X permits Y, p2.Y {
					}
					final class Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			};
		runner.runConformTest();
	}
	public void testBug563806_026() {
		associateToModule("mod.one", "p1/X.java", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, p2.Y {
					}
					final class Y implements X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			};
		runner.runConformTest();
	}
	public void testBug563806_027() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.one", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"""
					package p1;
					public sealed interface X permits Y, p2.Y {
					}
					non-sealed interface Y extends X{}
					""",
				"p2/Y.java",
				"package p2;\n"+
				"public non-sealed interface Y extends p1.X {}",
			};
		runner.runConformTest();
	}
	public void testBug563806_028() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public non-sealed enum X {
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public non-sealed enum X {
					                       ^
				Illegal modifier for the enum X; only public is permitted
				----------
				""");
	}
	public void testBug563806_029() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed enum X {
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed enum X {
					                   ^
				Illegal modifier for the enum X; only public is permitted
				----------
				""");
	}
	public void testBug563806_030() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public class X {
					static sealed enum Y {}
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 3)
					static sealed enum Y {}
					                   ^
				Illegal modifier for the member enum Y; only public, protected, private & static are permitted
				----------
				""");
	}
	public void testBug563806_031() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public class X {
					static non-sealed enum Y {}
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 3)
					static non-sealed enum Y {}
					                       ^
				Illegal modifier for the member enum Y; only public, protected, private & static are permitted
				----------
				""");
	}
	public void testBug563806_032() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed non-sealed interface X {
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed non-sealed interface X {
					                                   ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				2. ERROR in p1\\X.java (at line 2)
					public sealed non-sealed interface X {
					                                   ^
				An interface X is declared both sealed and non-sealed
				----------
				""");
	}
	public void testBug563806_033() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public sealed  @interface X {
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public sealed  @interface X {
					       ^^^^^^
				Syntax error on token "sealed", static expected
				----------
				""");
	}
	public void testBug563806_034() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public  non-sealed @interface X {
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public  non-sealed @interface X {
					                              ^
				An interface X declared as non-sealed should have a sealed direct superinterface
				----------
				""");
	}
	public void testBug563806_035() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public  non-sealed interface X {
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					public  non-sealed interface X {
					                             ^
				An interface X declared as non-sealed should have a sealed direct superinterface
				----------
				""");
	}
	public void testBug563806_036() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public  class X {
					  public void foo() {
					    sealed class Y{}
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 4)
					sealed class Y{}
					             ^
				Illegal modifier for the local class Y; only abstract or final is permitted
				----------
				""");
	}
	public void testBug563806_037() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public  class X {
					  public void foo() {
					    non-sealed class Y{}
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 4)
					non-sealed class Y{}
					                 ^
				Illegal modifier for the local class Y; only abstract or final is permitted
				----------
				""");
	}
	public void testBug563806_038() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public  class X {
					  public void foo() {
					    non-sealed sealed class Y{}
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 4)
					non-sealed sealed class Y{}
					                        ^
				Illegal modifier for the local class Y; only abstract or final is permitted
				----------
				""");
	}
	public void testBug563806_039() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					sealed class A{}
					public  class X {
					  public void foo() {
					    class Y extends A{}
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 2)
					sealed class A{}
					             ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares A as its direct superclass or superinterface
				----------
				2. ERROR in p1\\X.java (at line 5)
					class Y extends A{}
					                ^
				A local class Y cannot have a sealed direct superclass or a sealed direct superinterface A
				----------
				""");
	}
	public void testBug564191_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					sealed class X permits Y, Z{
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					final class Y extends X{}
					final class Z extends X{}
					""",
			},
			"0");
		String expectedOutput =
				"""
			PermittedSubclasses:
			   #33 p1/Y,
			   #35 p1/Z
			}""";
		verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted (top-level) types make it to the .class file
	public void testBug564190_1() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						final class Y extends X{}
						final class Z extends X{}
						""",
				},
				"0");
			String expectedOutput =
					"""
				PermittedSubclasses:
				   #33 p1/Y,
				   #35 p1/Z
				}""";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted final (member) types make it to the .class file
	public void testBug564190_2() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						  final class Y extends X{}
						  final class Z extends X{}
						}""",
				},
				"0");
			String expectedOutput =
					"""
				PermittedSubclasses:
				   #33 p1/X$Y,
				   #35 p1/X$Z
				}""";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted non-sealed (member) types make it to the .class file
	public void testBug564190_3() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						  non-sealed class Y extends X{}
						  non-sealed class Z extends X{}
						}""",
				},
				"0");
			String expectedOutput =
					"""
				PermittedSubclasses:
				   #33 p1/X$Y,
				   #35 p1/X$Z
				}""";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted member type is reported without final, sealed or non-sealed
	public void testBug564190_4() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X  {
							class Y extends X {}
							final class Z extends Y {}
						}""",
				},
				"""
					----------
					1. ERROR in p1\\X.java (at line 3)
						class Y extends X {}
						      ^
					The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
					----------
					""");
	}
	// Test that implicit permitted member type with implicit permitted types
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_5() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X {
							sealed class Y extends X {}
							final class Z {}
						}""",
				},
				"""
					----------
					1. ERROR in p1\\X.java (at line 3)
						sealed class Y extends X {}
						             ^
					Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares Y as its direct superclass or superinterface
					----------
					""");
	}
	// Test that implicit permitted member type with explicit permits clause
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_6() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X  {
							sealed class Y extends X permits Z {}
							final class Z {}
						}""",
				},
				"""
					----------
					1. ERROR in p1\\X.java (at line 3)
						sealed class Y extends X permits Z {}
						                                 ^
					Permitted class Z does not declare p1.X.Y as direct super class
					----------
					""");
	}
	// Test that implicit permitted member type with explicit permits clause
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_7() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed interface SI {}",
				},
				"""
					----------
					1. ERROR in p1\\X.java (at line 2)
						sealed interface SI {}
						                 ^^
					Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares SI as its direct superclass or superinterface
					----------
					""");
	}
	public void testBug564450_001() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						sealed class X permits Y{
						}""",
					"p1/Y.java",
					"""
						package p1;
						class Y extends X {
						}""",
				},
				"""
					----------
					1. ERROR in p1\\Y.java (at line 2)
						class Y extends X {
						      ^
					The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed
					----------
					""");
	}
	public void testBug564047_001() throws CoreException, IOException {
		String outputDirectory = Util.getOutputDirectory();
		String lib1Path = outputDirectory + File.separator + "lib1.jar";
		try {
		Util.createJar(
				new String[] {
					"p/Y.java",
					"package p;\n" +
					"public sealed class Y permits Z{}",
					"p/Z.java",
					"package p;\n" +
					"public final class Z extends Y{}",
				},
				lib1Path,
				JavaCore.VERSION_17,
				false);
		String[] libs = getDefaultClassPaths();
		int len = libs.length;
		System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
		libs[len] = lib1Path;
		this.runNegativeTest(
				new String[] {
					"src/p/X.java",
					"""
						package p;
						public class X extends Y {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}""",
				},
				"""
					----------
					1. ERROR in src\\p\\X.java (at line 2)
						public class X extends Y {
						             ^
					The class X with a sealed direct superclass or a sealed direct superinterface Y should be declared either final, sealed, or non-sealed
					----------
					2. ERROR in src\\p\\X.java (at line 2)
						public class X extends Y {
						                       ^
					The type X extending a sealed class Y should be a permitted subtype of Y
					----------
					""",
				libs,
		        true);
		} catch (IOException e) {
			System.err.println("could not write to current working directory ");
		} finally {
			new File(lib1Path).delete();
		}

	}
	public void testBug564492_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args){
					     new Y(){};
					  }
					}
					sealed class Y{}
					final class Z extends Y {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new Y(){};
					    ^
				An anonymous class cannot subclass a sealed type Y
				----------
				""");
	}
	public void testBug564492_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X  {
					   public static void main(String[] args) {
					        IY y = new IY(){};
					   }
					}
					sealed interface I {}
					sealed interface IY extends I {}
					final class Z implements IY{}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					IY y = new IY(){};
					           ^^
				An anonymous class cannot subclass a sealed type IY
				----------
				""");
	}
	public void testBug564492_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X permits A.Y {
					       public static void main(String[] args) {
					               new A.Y() {};
					       }
					}
					\s
					class A {
					       static sealed class Y extends X permits Z {}
					       final class Z extends Y{}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new A.Y() {};
					    ^^^
				An anonymous class cannot subclass a sealed type A.Y
				----------
				""");
	}
	public void testBug564492_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public  class X {
					       public static void main(String[] args) {
					               new A.IY() {};
					       }
					}
					\s
					class A {
					       sealed interface I permits IY{}
					       sealed interface IY extends I permits Z {}
					       final class Z implements IY{}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new A.IY() {};
					    ^^^^
				An anonymous class cannot subclass a sealed type A.IY
				----------
				""");
	}
	public void testBug564498_1() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						public sealed class X permits A.Y {
							public static void main(String[] args) {}
						}
						class A {
							sealed class Y extends X {
								final class SubInnerY extends Y {}
							}\s
							final class Z extends Y {}
						}""",
				},
				"");
			String expectedOutput =
					"""
				PermittedSubclasses:
				   #24 p1/A$Y$SubInnerY,
				   #26 p1/A$Z
				}""";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
			expectedOutput =
					"PermittedSubclasses:\n" +
					"   #21 p1/A$Y\n";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_2() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						public sealed class X permits A.Y {
							public static void main(String[] args) {}
						}
						class A {
							sealed class Y extends X {}\s
							final class Z extends Y {}
						   final class SubY extends Y {}\
						}""",
				},
				"");
			String expectedOutput =
					"""
				PermittedSubclasses:
				   #22 p1/A$Z,
				   #24 p1/A$SubY
				}""";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
			expectedOutput =
					"PermittedSubclasses:\n" +
					"   #21 p1/A$Y\n";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_3() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						public sealed class X permits A.Y {
							public static void main(String[] args) {}
						}
						class A {
							sealed class Y extends X {
								final class SubInnerY extends Y {}
							}\s
							final class Z extends Y {}
						   final class SubY extends Y {}\
						}""",
				},
				"");
			String expectedOutput =
					"""
				PermittedSubclasses:
				   #24 p1/A$Y$SubInnerY,
				   #26 p1/A$Z,
				   #28 p1/A$SubY
				""";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_4() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						public sealed class X permits A.Y {
							public static void main(String[] args) {}
						}
						class A {
							sealed class Y extends X permits Y.SubInnerY {
								final class SubInnerY extends Y {}
							}\s
						}""",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #24 p1/A$Y$SubInnerY\n";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Reject references of membertype without qualifier of enclosing type in permits clause
	public void testBug564498_5() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"""
						package p1;
						public sealed class X permits A.Y {
							public static void main(String[] args) {}
						}
						class A {
							sealed class Y extends X permits SubInnerY {
								final class SubInnerY extends Y {}
							}\s
						}""",
				},
				"""
					----------
					1. ERROR in p1\\X.java (at line 7)
						final class SubInnerY extends Y {}
						                              ^
					The type SubInnerY extending a sealed class A.Y should be a permitted subtype of A.Y
					----------
					""");
	}
	// accept references of membertype without qualifier of enclosing type in permits clause
	// provided it is imported
	public void testBug564498_6() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"p1/X.java",
						"""
							package p1;
							import p1.Y.Z;
							public class X {
								public static void main(String[] args) {}
							}
							sealed class Y permits Z {
								final class Z extends Y {}
							}""",
				},
				"");
	}
	public void testBug564613_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
					       public boolean permits( String s ) {
					               return true;
					       }
					       public static void main(String[] args) {
					               boolean b = new X().permits("hello");
					               System.out.println(b ? "Hello" : "World");
					       }
					}""",
			},
			"Hello");
	}
	public void testBug564613_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X permits permits Y, Z {}
					final class Y extends X{}
					final class Z extends X{}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed class X permits permits Y, Z {}
					                      ^^^^^^^
				Syntax error on token "permits", delete this token
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_001() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class permits {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class permits {
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type permits
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class permits {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class permits {
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type permits
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_003() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  permits p;
					  void foo() {
					    Zork();
					  }
					}""",
				"permits.java",
				"public class permits {\n"+
				"}",
			},
			"""
				----------
				----------
				1. ERROR in X.java (at line 2)
					permits p;
					^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				----------
				1. ERROR in permits.java (at line 1)
					public class permits {
					             ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  permits p;
					  void foo() {
					    Zork();
					  }
					}""",
				"permits.java",
				"public class permits {\n"+
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					permits p;
					^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				----------
				1. ERROR in permits.java (at line 1)
					public class permits {
					             ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_005() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<permits> {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X<permits> {
					        ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X<permits>
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<permits> {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X<permits> {
					        ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X<permits>
				----------
				""");
	}
	public void testBug564638_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X extends permits {
					  void foo() {
					    Zork();
					  }
					}
					class permits {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends permits {
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					class permits {
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_008() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X extends permits {
					  void foo() {
					    Zork();
					  }
					}
					class permits {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends permits {
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					class permits {
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X implements permits {
					  void foo() {
					    Zork();
					  }
					}
					interface permits {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X implements permits {
					                   ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface permits {
					          ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_010() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X implements permits {
					  void foo() {
					    Zork();
					  }
					}
					interface permits {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X implements permits {
					                   ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface permits {
					          ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X extends permits {
					  default void foo() {
					    Zork();
					  }
					}
					interface permits {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface X extends permits {
					                    ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface permits {
					          ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_012() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X extends permits {
					  default void foo() {
					    Zork();
					  }
					}
					interface permits {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface X extends permits {
					                    ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface permits {
					          ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X extends {
					  permits foo() {
					    Zork();
					    return null;
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends {
					        ^^^^^^^
				Syntax error on token "extends", Type expected after this token
				----------
				2. ERROR in X.java (at line 2)
					permits foo() {
					^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_014() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  permits foo() {
					    Zork();
					    return null;
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					permits foo() {
					^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_015() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X  {
					  void foo() throws permits{
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo() throws permits{
					                  ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_016() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  void foo() throws permits{
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo() throws permits{
					                  ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_017() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X <T extends permits> {
					  <T> void foo(T extends permits) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X <T extends permits> {
					                   ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 1)
					class X <T extends permits> {
					                            ^
				Syntax error, insert "}" to complete ClassBody
				----------
				3. WARNING in X.java (at line 2)
					<T> void foo(T extends permits) {
					 ^
				The type parameter T is hiding the type T
				----------
				4. ERROR in X.java (at line 2)
					<T> void foo(T extends permits) {
					               ^^^^^^^
				Syntax error on token "extends", delete this token
				----------
				5. ERROR in X.java (at line 5)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_018() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X <T extends permits>{
					  <T> void foo(T extends permits) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X <T extends permits>{
					                   ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 1)
					class X <T extends permits>{
					                           ^
				Syntax error, insert "}" to complete ClassBody
				----------
				4. WARNING in X.java (at line 2)
					<T> void foo(T extends permits) {
					 ^
				The type parameter T is hiding the type T
				----------
				5. ERROR in X.java (at line 2)
					<T> void foo(T extends permits) {
					               ^^^^^^^
				Syntax error on token "extends", delete this token
				----------
				6. ERROR in X.java (at line 5)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_019() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					enum X {
					  ONE(1);
					  private final permits p;
					  X(int p) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					private final permits p;
					              ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_020() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					enum X {
					  ONE(1);
					  private final permits p;
					  X(int p) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					private final permits p;
					              ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_021() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    I i = (permits p)-> {};\n" +
//				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  void apply(Object o);\n" +
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					I i = (permits p)-> {};
					      ^^^^^^^^^^^^^
				This lambda expression refers to the missing type permits
				----------
				2. ERROR in X.java (at line 3)
					I i = (permits p)-> {};
					       ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_022() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    I i = (permits p)-> {};
					    Zork();
					  }
					}
					interface I {
					  void apply(Object o);
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					I i = (permits p)-> {};
					      ^^^^^^^^^^^^^
				This lambda expression refers to the missing type permits
				----------
				2. ERROR in X.java (at line 3)
					I i = (permits p)-> {};
					       ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				4. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_023() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(permits this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(permits this) {}
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_024() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(permits this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(permits this) {}
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_025() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(permits this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(permits this) {}
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_026() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(permits this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(permits this) {}
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_027() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  class permits {
					     public void foo(permits this) {}
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class permits {
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					public void foo(permits this) {}
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_028() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  class permits {
					     public void foo(permits this) {}
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class permits {
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					public void foo(permits this) {}
					                ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_029() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    permits p;
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					permits p;
					^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_030() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    permits p;
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					permits p;
					^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_031() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    for (permits i = 0; i < 10; ++i) {}\s
					  }
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (permits i = 0; i < 10; ++i) {}\s
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 6)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_032() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    for (permits i = 0; i < 10; ++i) {}\s
					  }
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (permits i = 0; i < 10; ++i) {}\s
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 6)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_033() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(permits[] args) {
					    for (permits p : args) {}\s
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static void main(permits[] args) {
					                        ^^^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					for (permits p : args) {}\s
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_034() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(permits[] args) {
					    for (permits p : args) {}\s
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static void main(permits[] args) {
					                        ^^^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					for (permits p : args) {}\s
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_035() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (permits y = new Y()) {
					                      \s
					               } catch (Exception e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					try (permits y = new Y()) {
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_036() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (permits y = new Y()) {
					                      \s
					               } catch (Exception e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					try (permits y = new Y()) {
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_037() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (Y y = new Y()) {
					                      \s
					               } catch (permits e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					} catch (permits e) {
					         ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_038() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (Y y = new Y()) {
					                      \s
					               } catch (permits e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					} catch (permits e) {
					         ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_039() {
		runNegativeTest(
			new String[] {
				"X.java",
				"record X(permits p) {\n"+
				"}\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X(permits p) {
					^
				permits cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 1)
					record X(permits p) {
					         ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_040() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"record X(permits p) {\n"+
				"}\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X(permits p) {
					^
				permits cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 1)
					record X(permits p) {
					         ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_041() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					               <permits>this(t);
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					<permits>this(t);
					 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 5)
					<permits>this(t);
					         ^^^^^^^^
				The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_042() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					               <permits>this(t);
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					<permits>this(t);
					 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 5)
					<permits>this(t);
					         ^^^^^^^^
				The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_043() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					           new <permits>X(t).foo();
					       }
					       public void foo() {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					new <permits>X(t).foo();
					^^^^^^^^^^^^^^^^^
				The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)
				----------
				2. ERROR in X.java (at line 5)
					new <permits>X(t).foo();
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_044() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					           new <permits>X(t).foo();
					       }
					       public void foo() {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					new <permits>X(t).foo();
					^^^^^^^^^^^^^^^^^
				The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)
				----------
				2. ERROR in X.java (at line 5)
					new <permits>X(t).foo();
					     ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_045() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new X();
					               x.<permits>foo(0);
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					x.<permits>foo(0);
					   ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 6)
					x.<permits>foo(0);
					           ^^^
				The parameterized method <permits>foo(permits) of type X is not applicable for the arguments (Integer)
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_046() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new X();
					               x.<permits>foo(0);
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					x.<permits>foo(0);
					   ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 6)
					x.<permits>foo(0);
					           ^^^
				The parameterized method <permits>foo(permits) of type X is not applicable for the arguments (Integer)
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_047() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new permits();
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					X x = new permits();
					          ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_048() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new permits();
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					X x = new permits();
					          ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_049() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public X() {
					               new permits() {
					                       @Override
					                       void foo() {}
					               }.foo();
					       }
					}
					abstract class permits {
					       abstract void foo();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new permits() {
					    ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 9)
					abstract class permits {
					               ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_050() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public X() {
					       new permits() {
					          @Override
					          void foo() {
					            Zork();
					          }
					       }.foo();
					       }
					}
					abstract class permits {
					       abstract void foo();
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new permits() {
					    ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 6)
					Zork();
					^^^^
				The method Zork() is undefined for the type new permits(){}
				----------
				3. ERROR in X.java (at line 11)
					abstract class permits {
					               ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_051() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public X() {
					    Object[] p = new permits[10];
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object[] p = new permits[10];
					                 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_052() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public X() {
					    Object[] p = new permits[10];
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object[] p = new permits[10];
					                 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_053() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   new X().foo((permits) null);
					 }
					 private void foo(permits o) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new X().foo((permits) null);
					        ^^^
				The method foo(permits) from the type X refers to the missing type permits
				----------
				2. ERROR in X.java (at line 3)
					new X().foo((permits) null);
					             ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 5)
					private void foo(permits o) {}
					                 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_054() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   new X().foo((permits) null);
					 }
					 private void foo(permits o) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new X().foo((permits) null);
					        ^^^
				The method foo(permits) from the type X refers to the missing type permits
				----------
				2. ERROR in X.java (at line 3)
					new X().foo((permits) null);
					             ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				4. ERROR in X.java (at line 5)
					private void foo(permits o) {}
					                 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_055() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 private void foo(Object o) {
					   if (o instanceof permits) {}
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					if (o instanceof permits) {}
					                 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_056() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 private void foo(Object o) {
					   if (o instanceof permits) {}
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					if (o instanceof permits) {}
					                 ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638_057() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   @SuppressWarnings("unused")
					   I i = permits :: new;
					   Zork();
					 }
					}
					class permits{}
					interface I {
					 Object gen();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				2. ERROR in X.java (at line 8)
					class permits{}
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_058() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   @SuppressWarnings("unused")
					   I i = permits :: new;
					   Zork();
					 }
					}
					class permits{}
					interface I {
					 Object gen();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				2. ERROR in X.java (at line 8)
					class permits{}
					      ^^^^^^^
				'permits' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_001() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class sealed {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class sealed {
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type sealed
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class sealed {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class sealed {
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type sealed
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_003() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  sealed p;
					  void foo() {
					    Zork();
					  }
					}""",
				"sealed.java",
				"public class sealed {\n"+
				"}",
			},
			"""
				----------
				----------
				1. ERROR in X.java (at line 2)
					sealed p;
					^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				----------
				1. ERROR in sealed.java (at line 1)
					public class sealed {
					             ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  sealed p;
					  void foo() {
					    Zork();
					  }
					}""",
				"sealed.java",
				"public class sealed {\n"+
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					sealed p;
					^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				----------
				1. ERROR in sealed.java (at line 1)
					public class sealed {
					             ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_005() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<sealed> {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X<sealed> {
					        ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X<sealed>
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<sealed> {
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X<sealed> {
					        ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X<sealed>
				----------
				""");
	}
	public void testBug564638b_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X extends sealed {
					  void foo() {
					    Zork();
					  }
					}
					class sealed {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends sealed {
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					class sealed {
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_008() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X extends sealed {
					  void foo() {
					    Zork();
					  }
					}
					class sealed {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends sealed {
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					class sealed {
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X implements sealed {
					  void foo() {
					    Zork();
					  }
					}
					interface sealed {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X implements sealed {
					                   ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface sealed {
					          ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_010() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X implements sealed {
					  void foo() {
					    Zork();
					  }
					}
					interface sealed {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X implements sealed {
					                   ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface sealed {
					          ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X extends sealed {
					  default void foo() {
					    Zork();
					  }
					}
					interface sealed {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface X extends sealed {
					                    ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface sealed {
					          ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_012() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X extends sealed {
					  default void foo() {
					    Zork();
					  }
					}
					interface sealed {
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface X extends sealed {
					                    ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				3. ERROR in X.java (at line 6)
					interface sealed {
					          ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X extends {
					  sealed foo() {
					    Zork();
					    return null;
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends {
					        ^^^^^^^
				Syntax error on token "extends", Type expected after this token
				----------
				2. ERROR in X.java (at line 2)
					sealed foo() {
					^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_014() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  sealed foo() {
					    Zork();
					    return null;
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					sealed foo() {
					^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_015() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X  {
					  void foo() throws sealed{
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo() throws sealed{
					                  ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_016() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  void foo() throws sealed{
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo() throws sealed{
					                  ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_017() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X <T extends sealed> {
					  <T> void foo(T extends sealed) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X <T extends sealed> {
					                   ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 1)
					class X <T extends sealed> {
					                           ^
				Syntax error, insert "}" to complete ClassBody
				----------
				3. WARNING in X.java (at line 2)
					<T> void foo(T extends sealed) {
					 ^
				The type parameter T is hiding the type T
				----------
				4. ERROR in X.java (at line 2)
					<T> void foo(T extends sealed) {
					               ^^^^^^^
				Syntax error on token "extends", delete this token
				----------
				5. ERROR in X.java (at line 5)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_018() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X <T extends sealed>{
					  <T> void foo(T extends sealed) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X <T extends sealed>{
					                   ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 1)
					class X <T extends sealed>{
					                          ^
				Syntax error, insert "}" to complete ClassBody
				----------
				4. WARNING in X.java (at line 2)
					<T> void foo(T extends sealed) {
					 ^
				The type parameter T is hiding the type T
				----------
				5. ERROR in X.java (at line 2)
					<T> void foo(T extends sealed) {
					               ^^^^^^^
				Syntax error on token "extends", delete this token
				----------
				6. ERROR in X.java (at line 5)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_019() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					enum X {
					  ONE(1);
					  private final sealed p;
					  X(int p) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					private final sealed p;
					              ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_020() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					enum X {
					  ONE(1);
					  private final sealed p;
					  X(int p) {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					private final sealed p;
					              ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_021() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    I i = (sealed p)-> {};\n" +
//				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  void apply(Object o);\n" +
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					I i = (sealed p)-> {};
					      ^^^^^^^^^^^^
				This lambda expression refers to the missing type sealed
				----------
				2. ERROR in X.java (at line 3)
					I i = (sealed p)-> {};
					       ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_022() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    I i = (sealed p)-> {};
					    Zork();
					  }
					}
					interface I {
					  void apply(Object o);
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					I i = (sealed p)-> {};
					      ^^^^^^^^^^^^
				This lambda expression refers to the missing type sealed
				----------
				2. ERROR in X.java (at line 3)
					I i = (sealed p)-> {};
					       ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				4. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_023() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(sealed this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(sealed this) {}
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_024() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(sealed this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(sealed this) {}
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_025() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(sealed this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(sealed this) {}
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_026() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public void foo(sealed this) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(sealed this) {}
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_027() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  class sealed {
					     public void foo(sealed this) {}
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class sealed {
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					public void foo(sealed this) {}
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_028() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  class sealed {
					     public void foo(sealed this) {}
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class sealed {
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					public void foo(sealed this) {}
					                ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_029() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    sealed p;
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					sealed p;
					^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_030() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    sealed p;
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					sealed p;
					^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_031() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    for (sealed i = 0; i < 10; ++i) {}\s
					  }
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (sealed i = 0; i < 10; ++i) {}\s
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 6)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_032() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					    for (sealed i = 0; i < 10; ++i) {}\s
					  }
					  void foo() {
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (sealed i = 0; i < 10; ++i) {}\s
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 6)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_033() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(sealed[] args) {
					    for (sealed p : args) {}\s
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static void main(sealed[] args) {
					                        ^^^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 3)
					for (sealed p : args) {}\s
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_034() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(sealed[] args) {
					    for (sealed p : args) {}\s
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static void main(sealed[] args) {
					                        ^^^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 3)
					for (sealed p : args) {}\s
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_035() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (sealed y = new Y()) {
					                      \s
					               } catch (Exception e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					try (sealed y = new Y()) {
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_036() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (sealed y = new Y()) {
					                      \s
					               } catch (Exception e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					try (sealed y = new Y()) {
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_037() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (Y y = new Y()) {
					                      \s
					               } catch (sealed e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					} catch (sealed e) {
					         ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_038() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					               try (Y y = new Y()) {
					                      \s
					               } catch (sealed e) {
					                       e.printStackTrace();
					               } finally {
					                      \s
					               }
					       }
					}
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					} catch (sealed e) {
					         ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_039() {
		runNegativeTest(
			new String[] {
				"X.java",
				"record X(sealed p) {\n"+
				"}\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X(sealed p) {
					^
				sealed cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 1)
					record X(sealed p) {
					         ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_040() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"record X(sealed p) {\n"+
				"}\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X(sealed p) {
					^
				sealed cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 1)
					record X(sealed p) {
					         ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_041() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					               <sealed>this(t);
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					<sealed>this(t);
					 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 5)
					<sealed>this(t);
					        ^^^^^^^^
				The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_042() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					               <sealed>this(t);
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					<sealed>this(t);
					 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 5)
					<sealed>this(t);
					        ^^^^^^^^
				The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_043() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					           new <sealed>X(t).foo();
					       }
					       public void foo() {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					new <sealed>X(t).foo();
					^^^^^^^^^^^^^^^^
				The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)
				----------
				2. ERROR in X.java (at line 5)
					new <sealed>X(t).foo();
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_044() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> X(T t) {}
					      \s
					       public X(int t, char c) {
					           new <sealed>X(t).foo();
					       }
					       public void foo() {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					new <sealed>X(t).foo();
					^^^^^^^^^^^^^^^^
				The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)
				----------
				2. ERROR in X.java (at line 5)
					new <sealed>X(t).foo();
					     ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_045() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new X();
					               x.<sealed>foo(0);
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					x.<sealed>foo(0);
					   ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 6)
					x.<sealed>foo(0);
					          ^^^
				The parameterized method <sealed>foo(sealed) of type X is not applicable for the arguments (Integer)
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_046() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new X();
					               x.<sealed>foo(0);
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					x.<sealed>foo(0);
					   ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 6)
					x.<sealed>foo(0);
					          ^^^
				The parameterized method <sealed>foo(sealed) of type X is not applicable for the arguments (Integer)
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_047() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new sealed();
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					X x = new sealed();
					          ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_048() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public <T> void foo(T t) {}
					      \s
					       public X() {
					               X x = new sealed();
					       }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					X x = new sealed();
					          ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_049() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public X() {
					               new sealed() {
					                       @Override
					                       void foo() {}
					               }.foo();
					       }
					}
					abstract class sealed {
					       abstract void foo();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new sealed() {
					    ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 9)
					abstract class sealed {
					               ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_050() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public X() {
					       new sealed() {
					          @Override
					          void foo() {
					            Zork();
					          }
					       }.foo();
					       }
					}
					abstract class sealed {
					       abstract void foo();
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new sealed() {
					    ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				2. ERROR in X.java (at line 6)
					Zork();
					^^^^
				The method Zork() is undefined for the type new sealed(){}
				----------
				3. ERROR in X.java (at line 11)
					abstract class sealed {
					               ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_051() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public X() {
					    Object[] p = new sealed[10];
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object[] p = new sealed[10];
					                 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_052() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public X() {
					    Object[] p = new sealed[10];
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object[] p = new sealed[10];
					                 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_053() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   new X().foo((sealed) null);
					 }
					 private void foo(sealed o) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new X().foo((sealed) null);
					        ^^^
				The method foo(sealed) from the type X refers to the missing type sealed
				----------
				2. ERROR in X.java (at line 3)
					new X().foo((sealed) null);
					             ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				3. ERROR in X.java (at line 5)
					private void foo(sealed o) {}
					                 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_054() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   new X().foo((sealed) null);
					 }
					 private void foo(sealed o) {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new X().foo((sealed) null);
					        ^^^
				The method foo(sealed) from the type X refers to the missing type sealed
				----------
				2. ERROR in X.java (at line 3)
					new X().foo((sealed) null);
					             ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				4. ERROR in X.java (at line 5)
					private void foo(sealed o) {}
					                 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_055() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 private void foo(Object o) {
					   if (o instanceof sealed) {}
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					if (o instanceof sealed) {}
					                 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_056() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 private void foo(Object o) {
					   if (o instanceof sealed) {}
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					if (o instanceof sealed) {}
					                 ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug564638b_057() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   @SuppressWarnings("unused")
					   I i = sealed :: new;
					   Zork();
					 }
					}
					class sealed{}
					interface I {
					 Object gen();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				2. ERROR in X.java (at line 8)
					class sealed{}
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_058() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 public static void main(String[] args) {
					   @SuppressWarnings("unused")
					   I i = sealed :: new;
					   Zork();
					 }
					}
					class sealed{}
					interface I {
					 Object gen();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				2. ERROR in X.java (at line 8)
					class sealed{}
					      ^^^^^^
				'sealed' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug565561_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public sealed class X permits Outer.Inner {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					class Outer{
					   final class Inner extends X{}
					}""",
			},
			"0");
		String expectedOutput =
			"""
			  Inner classes:
			    [inner class info: #33 Outer$Inner, outer class info: #36 Outer
			     inner name: #38 Inner, accessflags: 16 final]
			
			PermittedSubclasses:
			   #33 Outer$Inner
			}""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565116_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"permits/X.java",
				"""
					package permits;
					class X {
					  public static void main(String[] args) {
					    X x = new permits.X();
					  }
					}""",
			},
			"");
	}
	public void testBug565638_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed class X {
					  public static void main(String[] args) {
					    System.out.println(0);
					  }
					}
					final class Outer {
					    final class Inner extends X{
					  }
					}""",
			},
			"0");
	}
	public void testBug565782_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits X {}
					enum X implements I {
					    ONE {};
					    public static void main(String[] args) {
					        System.out.println(0);
					   }
					}""",
			},
			"0");
		String expectedOutput =
				"""
			PermittedSubclasses:
			   #14 X$1
			}""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_002() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits X {}
					public enum X implements I {
					    ONE ;
					    public static void main(String[] args) {
					        System.out.println(0);
					   }
					}""",
			},
			"0");
		String expectedOutput =	"public final enum X implements I {\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_003() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {}
					enum X implements I {
					    ONE {};
					    public static void main(String[] args) {
					        System.out.println(0);
					   }
					}""",
			},
			"0");
		String expectedOutput =
				"""
			PermittedSubclasses:
			   #14 X$1
			}""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_004() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {}
					class X {
						enum E implements I {
					   	ONE {};
						}
					   public static void main(String[] args) {
					      	System.out.println(0);
					   }
					}""",
			},
			"0");
		String expectedOutput =
				"""
			PermittedSubclasses:
			   #14 X$E$1
			}""";
		verifyClassFile(expectedOutput, "X$E.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_005() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits X {}
					enum X implements I {
					    ONE {},
					    TWO {},
					    THREE {};
					    public static void main(String[] args) {
					        System.out.println(0);
					   }
					}""",
			},
			"0");
		String expectedOutput =
				"""
			PermittedSubclasses:
			   #16 X$1,
			   #25 X$2,
			   #31 X$3
			}""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565847_001() {
		Map<String, String> options =getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);

		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public sealed class X  permits Y {\
					Zork();
					}
					final class  Y extends X{}
					sealed interface I{}
					final class Z implements I{}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed class X  permits Y {Zork();
					                                  ^^^^^^
				Return type for the method is missing
				----------
				2. ERROR in X.java (at line 1)
					public sealed class X  permits Y {Zork();
					                                  ^^^^^^
				This method requires a body instead of a semicolon
				----------
				""",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "rawtypes" })
	public void testBug566979_001() {
		Map options = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public sealed void main(String[] args){ }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public sealed void main(String[] args){ }
					       ^^^^^^
				Syntax error on token "sealed", static expected
				----------
				""",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug566979_002() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public sealed void main(String[] args){ }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public sealed void main(String[] args){ }
					       ^^^^^^
				Syntax error on token "sealed", static expected
				----------
				""",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "rawtypes" })
	public void testBug566980_001() {
		Map options = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public permits void main(String[] args){ }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public permits void main(String[] args){ }
					               ^^^^
				Syntax error on token "void", delete this token
				----------
				""",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug566980_002() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public permits void main(String[] args){ }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public permits void main(String[] args){ }
					               ^^^^
				Syntax error on token "void", delete this token
				----------
				""",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "rawtypes" })
	public void testBug566846_001() {
		Map options = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"record X;\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X;
					^
				The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above
				----------
				2. ERROR in X.java (at line 1)
					record X;
					^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				3. ERROR in X.java (at line 1)
					record X;
					^
				Implicitly declared class must have a candidate main method
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug568428_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        sealed interface I {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					sealed interface I {}
					                 ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				"""
		);
	}
	public void testBug568428_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        non-sealed interface I {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					non-sealed interface I {}
					                     ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				"""
		);
	}
	public void testBug568514_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        sealed enum I {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					sealed enum I {}
					            ^
				Illegal modifier for local enum I; no explicit modifier is permitted
				----------
				"""
		);
	}
	public void testBug568514_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        non-sealed enum I {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					non-sealed enum I {}
					                ^
				Illegal modifier for local enum I; no explicit modifier is permitted
				----------
				"""
		);
	}
	public void testBug568758_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed interface X{}\n",
				"Y.java",
				"public final class Y implements X{}",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public sealed interface X{}
					                        ^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface
				----------
				----------
				1. ERROR in Y.java (at line 1)
					public final class Y implements X{}
					                                ^
				The type Y that implements a sealed interface X should be a permitted subtype of X
				----------
				""");
	}
	public void testBug569522_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  sealed interface Foo<T> permits Bar { }
					  final class Bar<T> implements Foo<T> { }
					  public static void main(String[] args) {
					       System.out.println("");
					  }
					}""",
			},
			"");
	}
	public void testBug569522_002() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  sealed class Foo<T> permits Bar { }
					  final class Bar<T> extends Foo<T> { }
					  public static void main(String[] args) {
					       System.out.println("");
					  }
					}""",
			},
			"");
	}
	public void testBug570359_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Modifier;
					
					sealed interface I {
					 void foo();
					}
					
					class Y {
					 enum E implements I {
					   ONE() {
					     public void foo() {
					     }
					   };
					 }
					}
					
					public class X {
					 public static void main(String argv[]) {
					   Class<? extends Y.E> c = Y.E.ONE.getClass();
					   System.out.println(c != null ? (c.getModifiers() & Modifier.FINAL) != 0 : false);
					 }
					}""",
			},
			"true");
		String expectedOutput = "final enum Y$E$1 {\n";
		SealedTypesTests.verifyClassFile(expectedOutput, "Y$E$1.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"""
					  Inner classes:
					    [inner class info: #3 Y$E, outer class info: #20 Y
					     inner name: #22 E, accessflags: 17416 abstract static],
					    [inner class info: #1 Y$E$1, outer class info: #0
					     inner name: #0, accessflags: 16400 final]
					  Enclosing Method: #3  #0 Y$E
					""";
		SealedTypesTests.verifyClassFile(expectedOutput, "Y$E$1.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug568854_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
					   sealed interface Foo permits A {}
					   record A() implements Foo {}
					   record B() implements Foo {}
					 }\
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					record B() implements Foo {}
					                      ^^^
				The type B that implements a sealed interface X.Foo should be a permitted subtype of X.Foo
				----------
				""");
	}
	public void testBug568854_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 sealed interface Foo permits X.A {}
					 public class X {
					   record A() implements Foo {}
					   record B() implements Foo {}
					 }\
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					record B() implements Foo {}
					                      ^^^
				The type B that implements a sealed interface Foo should be a permitted subtype of Foo
				----------
				""");
	}
	public void testBug568854_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 sealed interface Foo permits A {}
					 record A() implements Foo {}
					 record B() implements Foo {}
					 public class X {
					 }\
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					record B() implements Foo {}
					                      ^^^
				The type B that implements a sealed interface Foo should be a permitted subtype of Foo
				----------
				""");
	}
	public void testBug568854_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
					   sealed interface Foo permits A {}
					   class A implements Foo {}
					   final class B implements Foo {}
					 }\
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					class A implements Foo {}
					      ^
				The class A with a sealed direct superclass or a sealed direct superinterface X.Foo should be declared either final, sealed, or non-sealed
				----------
				2. ERROR in X.java (at line 4)
					final class B implements Foo {}
					                         ^^^
				The type B that implements a sealed interface X.Foo should be a permitted subtype of X.Foo
				----------
				""");
	}
	public void testBug568854_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 sealed interface Foo permits X.A {}
					 public class X {
					   class A implements Foo {}
					   final class B implements Foo {}
					 }\
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					class A implements Foo {}
					      ^
				The class A with a sealed direct superclass or a sealed direct superinterface Foo should be declared either final, sealed, or non-sealed
				----------
				2. ERROR in X.java (at line 4)
					final class B implements Foo {}
					                         ^^^
				The type B that implements a sealed interface Foo should be a permitted subtype of Foo
				----------
				""");
	}
	public void testBug568854_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					 sealed interface Foo permits A {}
					 class A implements Foo {}
					 final class B implements Foo {}
					 public class X {
					 }\
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class A implements Foo {}
					      ^
				The class A with a sealed direct superclass or a sealed direct superinterface Foo should be declared either final, sealed, or non-sealed
				----------
				2. ERROR in X.java (at line 3)
					final class B implements Foo {}
					                         ^^^
				The type B that implements a sealed interface Foo should be a permitted subtype of Foo
				----------
				""");
	}
	public void testBug568854_007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A {}
					final class A implements I {}
					enum B {
					   ONE {
					     class Y implements I {}
					   }
					}
					public class    X {
					 public static void main(String[] args) {
					   class Z implements I{}
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					class Y implements I {}
					                   ^
				A local class Y cannot have a sealed direct superclass or a sealed direct superinterface I
				----------
				2. ERROR in X.java (at line 10)
					class Z implements I{}
					                   ^
				A local class Z cannot have a sealed direct superclass or a sealed direct superinterface I
				----------
				""");
	}
	public void testBug568854_008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits X.A {}
					public class    X {
					final class A implements I {}
					enum B {
					   ONE {
					     class Y implements I {}
					   }
					}
					 public static void main(String[] args) {
					   class Z implements I{}
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					class Y implements I {}
					                   ^
				A local class Y cannot have a sealed direct superclass or a sealed direct superinterface I
				----------
				2. ERROR in X.java (at line 10)
					class Z implements I{}
					                   ^
				A local class Z cannot have a sealed direct superclass or a sealed direct superinterface I
				----------
				""");
	}
	public void testBug571332_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {
					       void foo();
					}
					non-sealed interface I1 extends I {}
					public class X {
					    public static void main(String argv[]) {
					        I lambda = () -> {};
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					I lambda = () -> {};
					           ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
	}
	public void testBug570605_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						sealed class Y {}
						non-sealed class Z extends Y {}
						public class X {
						 public void foo() {
						        record R()  {
						            class L extends Y {}
						        }
						    }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						class L extends Y {}
						                ^
					A local class L cannot have a sealed direct superclass or a sealed direct superinterface Y
					----------
					""");
	}
	public void testBug570218_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					sealed class A permits X {}
					final class X extends A implements I {\s
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					""",
			},
			"0");
	}
	public void testBug570218_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits X{}
					class A  {}
					final class X extends A implements I {\s
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					""",
			},
			"0");
	}
	public void testBug572205_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X{
					  public static void main(String[] args) {
						 class Circle implements Shape{}
					  }
					  sealed interface Shape {}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					class Circle implements Shape{}
					                        ^^^^^
				A local class Circle cannot have a sealed direct superclass or a sealed direct superinterface X.Shape
				----------
				2. ERROR in X.java (at line 5)
					sealed interface Shape {}
					                 ^^^^^
				Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares Shape as its direct superclass or superinterface
				----------
				""");
	}
	public void testBug573450_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						sealed interface Foo permits Foo.Bar {
							interface Interface {}
							record Bar() implements Foo, Interface { }
						}
						public class X {\s
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}""",
				},
				"0");
	}

	public void testBug573450_002() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						interface Interface {}
						sealed interface Foo extends Interface permits Foo.Bar {
							record Bar() implements Foo, Interface {}
						}
						public class X {\s
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}"""
				},
				"0");
	}
	public void testBug573450_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						sealed interface Interface extends Foo{}
						sealed interface Foo extends Interface permits Foo.Bar, Interface {
							record Bar() implements Foo, Interface {}\s
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						sealed interface Interface extends Foo{}
						                 ^^^^^^^^^
					The hierarchy of the type Interface is inconsistent
					----------
					2. ERROR in X.java (at line 2)
						sealed interface Foo extends Interface permits Foo.Bar, Interface {
						                             ^^^^^^^^^
					Cycle detected: a cycle exists in the type hierarchy between Foo and Interface
					----------
					3. ERROR in X.java (at line 3)
						record Bar() implements Foo, Interface {}\s
						       ^^^
					The hierarchy of the type Bar is inconsistent
					----------
					""");
	}
	public void testBug573450_004() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public sealed class X permits X.Y {
							final class Y extends X {}
							public static void main(String[] args){
								System.out.println(0);
							}
						}"""
				},
				"0");
	}
	public void testBug573450_005() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public sealed class X permits Y {
							final class Y extends X {}
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						final class Y extends X {}
						                      ^
					The type Y extending a sealed class X should be a permitted subtype of X
					----------
					""");
	}
	public void testBug578619_1() {
		runConformTest(
				new String[] {
						"Bug578619.java",
						"""
							public class Bug578619 {
								public static void main(String[] args) {
									System.out.println("Hola");
								}
							}
							sealed interface I1 permits I2, I3 {}
							non-sealed interface I2 extends I1 {}
							non-sealed interface I3 extends I2, I1 {}"""
				},
				"Hola");
	}
	public void testBug578619_2() {
		runNegativeTest(
				new String[] {
						"Bug578619.java",
						"""
							public class Bug578619 {
								public static void main(String[] args) {
									System.out.println("Hola");
								}
							}
							sealed interface I1 permits I2, I3 {}
							non-sealed interface I2 extends I1 {}
							non-sealed interface I3 extends I2 {}"""
				},
				"""
					----------
					1. ERROR in Bug578619.java (at line 6)
						sealed interface I1 permits I2, I3 {}
						                                ^^
					Permitted type I3 does not declare I1 as direct super interface\s
					----------
					2. ERROR in Bug578619.java (at line 8)
						non-sealed interface I3 extends I2 {}
						                     ^^
					An interface I3 declared as non-sealed should have a sealed direct superinterface
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							sealed interface I permits J {}\r
							record J<T>() implements I {}
							public class X {
							    public static void main(String [] args) {
							        J j; K k;
							    }
							}
							"""
				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						J j; K k;
						^
					J is a raw type. References to generic type J<T> should be parameterized
					----------
					2. ERROR in X.java (at line 5)
						J j; K k;
						     ^
					K cannot be resolved to a type
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378_2() {
		runNegativeTest(
				new String[] {
						"X.java",
						"sealed interface I permits J<Object> {}\r\n" +
						"record J<T>() implements I {}\n"
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						sealed interface I permits J<Object> {}
						                             ^^^^^^
					Type arguments are not allowed here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378_3() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							sealed interface I permits J<Object>.K<String> {}\r
							final class J<T> {
							    final class K<P> implements I {}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						sealed interface I permits J<Object>.K<String> {}
						                             ^^^^^^
					Type arguments are not allowed here
					----------
					2. ERROR in X.java (at line 1)
						sealed interface I permits J<Object>.K<String> {}
						                                       ^^^^^^
					Type arguments are not allowed here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378_4() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							sealed interface I permits J.K<String> {}\r
							final class J<T> {
							    final static class K<P> implements I {}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						sealed interface I permits J.K<String> {}
						                               ^^^^^^
					Type arguments are not allowed here
					----------
					""");
	}
}
