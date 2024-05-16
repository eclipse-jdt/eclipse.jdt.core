/*******************************************************************************
 * Copyright (c) 2019, 2024 IBM Corporation and others.
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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class RecordsRestrictedClassTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssue1641"};
	}

	public static Class<?> testClass() {
		return RecordsRestrictedClassTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public RecordsRestrictedClassTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptionsWithPreviewIfApplicable() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forRelease("16");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		if (!isJRE16Plus)
			return;
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.DEFAULT);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forRelease("16") :
			JavacTestOptions.forRelease("16", javacAdditionalTestOptions);
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
	private void verifyOutputNegative(String result, String expectedOutput) {
		verifyOutput(result, expectedOutput, false);
	}
	private void verifyOutput(String result, String expectedOutput, boolean positive) {
		int index = result.indexOf(expectedOutput);
		if (positive) {
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
				System.out.println("...");
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} else {
			if (index != -1) {
				assertEquals("Unexpected contents", "", result);
			}
		}
	}
	private String getClassFileContents( String classFileName, int mode) throws IOException,
	ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		return result;
	}

	public void testBug550750_001() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int x, int y){
							}
							"""
				},
			"0");
	}
	public void testBug550750_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					abstract record Point(int x, int y){
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					abstract record Point(int x, int y){
					                ^^^^^
				Illegal modifier for the record Point; only public, final and strictfp are permitted
				----------
				""");
	}
	/* A record declaration is implicitly final. It is permitted for the declaration of
	 * a record type to redundantly specify the final modifier. */
	public void testBug550750_003() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							final record Point(int x, int y){
							}
							"""
				},
			"0");
	}
	public void testBug550750_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					final final record Point(int x, int y){
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					final final record Point(int x, int y){
					                   ^^^^^
				Duplicate modifier for the type Point
				----------
				""");
	}
	public void testBug550750_005() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							final record Point(int x, int y){
							}
							"""
				},
			"0");
	}
	public void testBug550750_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public public record X(int x, int y){\n"+
			"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public public record X(int x, int y){
					                     ^
				Duplicate modifier for the type X
				----------
				""");
	}
	public void testBug550750_007() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							final record Point(int x, int y){
							  public void foo() {}
							}
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							"""
				},
			"0");
	}
	public void testBug550750_008() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							final record Point(int x, int y){
							  public Point {}
							}
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							"""
				},
			"0");
	}
	public void testBug550750_009() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							final record Point(int x, int y){
							  public Point {}
							  public void foo() {}
							}
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}"""
				},
			"0");
	}
	 /* nested record implicitly static*/
	public void testBug550750_010() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							  record Point(int x, int y){
							  }
							}
							"""
				},
			"0");
	}
	 /* nested record explicitly static*/
	public void testBug550750_011() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							  static record Point(int x, int y){
							  }
							}
							"""
				},
			"0");
	}
	public void testBug550750_012() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int ... x){
							}
							"""
				},
			"0");
	}
	public void testBug550750_013() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.Target;
							import java.lang.annotation.ElementType;
							record Point(@MyAnnotation int myInt, char myChar) {}
							 @Target({ElementType.FIELD, ElementType.TYPE})
							 @interface MyAnnotation {}
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							"""
				},
			"0");
	}
	public void testBug550750_014() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) {
							  public int myInt(){
							     return this.myInt;
							  }
							}
							"""
				},
			"0");
	}
	public void testBug550750_015() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							  public int myInt(){
							     return this.myInt;
							  }
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug550750_016() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug550750_017() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							  public Point(int myInt, char myChar){
							     this.myInt = myInt;
							     this.myChar = myChar;
							  }
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug550750_018() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							  public Point(int myInt, char myChar){
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public Point(int myInt, char myChar){
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The blank final field myChar may not have been initialized
				----------
				2. ERROR in X.java (at line 7)
					public Point(int myInt, char myChar){
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The blank final field myInt may not have been initialized
				----------
				""");
	}
	public void testBug550750_019() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							  private Point {
							     this.myInt = myInt;
							     this.myChar = myChar;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					private Point {
					        ^^^^^
				Cannot reduce the visibility of a canonical constructor Point from that of the record
				----------
				""");
	}
	public void testBug550750_020() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							  protected Point {
							     this.myInt = myInt;
							     this.myChar = myChar;
							  }
							}
							interface I {}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						this.myInt = myInt;
						^^^^^^^^^^
					Illegal explicit assignment of a final field myInt in compact constructor
					----------
					2. ERROR in X.java (at line 9)
						this.myChar = myChar;
						^^^^^^^^^^^
					Illegal explicit assignment of a final field myChar in compact constructor
					----------
					""");
	}
	public void testBug550750_022() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) implements I {
							  public Point {
							     this.myInt = myInt;
							     this.myChar = myChar;
							     return;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 10)
					return;
					^^^^^^^
				The body of a compact constructor must not contain a return statement
				----------
				""");
	}
	public void testBug550750_023() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int finalize) implements I {
							  public Point {
							     this.myInt = myInt;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 6)
					record Point(int myInt, int finalize) implements I {
					                            ^^^^^^^^
				Illegal component name finalize in record Point;
				----------
				""");
	}
	public void testBug550750_024() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int finalize, int myZ) implements I {
							  public Point {
							     this.myInt = myInt;
							     this.myZ = myZ;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 6)
					record Point(int myInt, int finalize, int myZ) implements I {
					                            ^^^^^^^^
				Illegal component name finalize in record Point;
				----------
				""");
	}
	public void testBug550750_025() {
		getPossibleComplianceLevels();
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ, int myZ) implements I {
							  public Point {
							     this.myInt = myInt;
							     this.myZ = myZ;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 6)
					record Point(int myInt, int myZ, int myZ) implements I {
					                            ^^^
				Duplicate component myZ in record
				----------
				2. ERROR in X.java (at line 6)
					record Point(int myInt, int myZ, int myZ) implements I {
					                                     ^^^
				Duplicate component myZ in record
				----------
				3. ERROR in X.java (at line 6)
					record Point(int myInt, int myZ, int myZ) implements I {
					                                     ^^^
				Duplicate parameter myZ
				----------
				""");
	}
	public void testBug550750_026() {
		getPossibleComplianceLevels();
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myInt, int myInt, int myZ) implements I {
							  public Point {
							     this.myInt = myInt;
							     this.myZ = myZ;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 6)
					record Point(int myInt, int myInt, int myInt, int myZ) implements I {
					                 ^^^^^
				Duplicate component myInt in record
				----------
				2. ERROR in X.java (at line 6)
					record Point(int myInt, int myInt, int myInt, int myZ) implements I {
					                            ^^^^^
				Duplicate component myInt in record
				----------
				3. ERROR in X.java (at line 6)
					record Point(int myInt, int myInt, int myInt, int myZ) implements I {
					                            ^^^^^
				Duplicate parameter myInt
				----------
				4. ERROR in X.java (at line 6)
					record Point(int myInt, int myInt, int myInt, int myZ) implements I {
					                                       ^^^^^
				Duplicate component myInt in record
				----------
				5. ERROR in X.java (at line 6)
					record Point(int myInt, int myInt, int myInt, int myZ) implements I {
					                                       ^^^^^
				Duplicate parameter myInt
				----------
				""");
	}
	public void testBug550750_027() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  static final int z;
							  public Point {
							     this.myInt = myInt;
							     this.myZ = myZ;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					static final int z;
					                 ^
				The blank final field z may not have been initialized
				----------
				2. ERROR in X.java (at line 9)
					this.myInt = myInt;
					^^^^^^^^^^
				Illegal explicit assignment of a final field myInt in compact constructor
				----------
				3. ERROR in X.java (at line 10)
					this.myZ = myZ;
					^^^^^^^^
				Illegal explicit assignment of a final field myZ in compact constructor
				----------
				""");
	}
	public void testBug550750_028() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  int z;
							  public Point {
							     this.myInt = myInt;
							     this.myZ = myZ;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					int z;
					    ^
				User declared non-static fields z are not permitted in a record
				----------
				""");
	}
	public void testBug550750_029() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  public Point {
							     this.myInt = myInt;
							     this.myZ = myZ;
							  }
							  public native void foo();
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 11)
					public native void foo();
					                   ^^^^^
				Illegal modifier native for method foo; native methods are not allowed in record
				----------
				""");
	}
	public void testBug550750_030() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  {
							     System.out.println(0);
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					{
				     System.out.println(0);
				  }
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Instance Initializer is not allowed in a record declaration
				----------
				""");
	}
	public void testBug550750_031() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  static {
							     System.out.println(0);
							  }
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug550750_032() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class record {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class record {
					      ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""");
	}
	public void testBug550750_033() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X<record> {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X<record> {
					        ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""");
	}
	public void testBug550750_034() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							  public <record> void foo(record args){}
							}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 5)
					public <record> void foo(record args){}
					        ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				2. ERROR in X.java (at line 5)
					public <record> void foo(record args){}
					                         ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""");
	}
	public void testBug550750_035() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							  public void foo(record args){}
							}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 5)
					public void foo(record args){}
					                ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""");
	}
	public void testBug550750_036() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							     I lambda = (record r) -> {};
							  }
							}
							interface I {
							  public void apply(int i);
							}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 4)
					I lambda = (record r) -> {};
					           ^^^^^^^^^^^^^
				This lambda expression refers to the missing type record
				----------
				2. ERROR in X.java (at line 4)
					I lambda = (record r) -> {};
					            ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""");
	}
	public void testBug550750_037() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(){
							}
							"""
				},
			"0");
	}
	public void testBug550750_038() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(){
							   public Point {}
							}
							"""
				},
			"0");
	}
	public void testBug550750_039() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(){
							   public Point() {}
							}
							"""
				},
			"0");
	}
	public void testBug550750_040() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(){
							   private int f;
							   public Point() {}
							}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					private int f;
					            ^
				User declared non-static fields f are not permitted in a record
				----------
				""");
	}
	public void testBug550750_041() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(){
							   static int f;
							   public Point() {}
							}
							"""
				},
			"0");
	}
	public void testBug553152_001() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  public char myInt() {;
							     return 'c';
							  }
							  public int getmyInt() {;
							     return this.myInt;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public char myInt() {;
					       ^^^^
				Illegal return type of accessor; should be the same as the declared type int of the record component
				----------
				""");
	}
	public void testBug553152_002() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public java.lang.Integer myInt() {;
							     return this.myInt;
							  }
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug553152_003() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  public <T> int myInt() {;
							     return this.myInt;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public <T> int myInt() {;
					               ^^^^^^^
				The accessor method must not be generic
				----------
				""");
	}
	public void testBug553152_004() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  private int myInt() {;
							     return this.myInt;
							  }
							  /* package */ int myZ() {;
							     return this.myZ;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					private int myInt() {;
					            ^^^^^^^
				The accessor method must be declared public
				----------
				2. ERROR in X.java (at line 10)
					/* package */ int myZ() {;
					                  ^^^^^
				The accessor method must be declared public
				----------
				""");
	}
	public void testBug553152_005() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  public int myInt() throws Exception {;
							     return this.myInt;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public int myInt() throws Exception {;
					           ^^^^^^^^^^^^^^^^^^^^^^^^
				Throws clause not allowed for explicitly declared accessor method
				----------
				""");
	}
	public void testBug553152_006() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point(Integer myInt, int myZ) {
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug553152_007() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point(Integer myInt, int myZ) {
							     this.myInt = 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public Point(Integer myInt, int myZ) {
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The blank final field myZ may not have been initialized
				----------
				""");
	}
	public void testBug553152_008() {
		getPossibleComplianceLevels();
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point {
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							  public Point(Integer myInt, int myZ) {
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 8)
					this.myInt = 0;
					^^^^^^^^^^
				Illegal explicit assignment of a final field myInt in compact constructor
				----------
				2. ERROR in X.java (at line 9)
					this.myZ = 0;
					^^^^^^^^
				Illegal explicit assignment of a final field myZ in compact constructor
				----------
				3. ERROR in X.java (at line 11)
					public Point(Integer myInt, int myZ) {
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Duplicate method Point(Integer, int) in type Point
				----------
				""");
	}
	public void testBug553152_009() {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  Point(Integer myInt, int myZ) {
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"0");
	}
	public void testBug553152_010() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public <T> Point(Integer myInt, int myZ) {
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public <T> Point(Integer myInt, int myZ) {
					           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Canonical constructor Point of a record declaration should not be generic
				----------
				""");
	}
	public void testBug553152_011() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point(Integer myInt, int myZ) throws Exception {
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public Point(Integer myInt, int myZ) throws Exception {
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Throws clause not allowed for canonical constructor Point
				----------
				""");
	}
	public void testBug553152_012() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point {
							     this.myInt = 0;
							     this.myZ = 0;
							     return;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 10)
					return;
					^^^^^^^
				The body of a compact constructor must not contain a return statement
				----------
				""");
	}
	public void testBug553152_013() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point(Integer myInt, int myZ) {
							     this.myInt = 0;
							     this.myZ = 0;
							     I i = () -> { return;};
							     Zork();
							  }
							  public void apply() {}
							}
							interface I { void apply();}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type Point
				----------
				""");
	}
	public void testBug553152_014() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point(Integer myInt, int myZ) {
							     super();
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 8)
					super();
					^^^^^^^^
				The body of a canonical constructor must not contain an explicit constructor call
				----------
				""");
	}
	public void testBug553152_015() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point(Integer myInt, int myZ) {
							     this.Point(0);
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							  public Point(Integer myInt) {}
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 8)
					this.Point(0);
					     ^^^^^
				The method Point(int) is undefined for the type Point
				----------
				2. ERROR in X.java (at line 12)
					public Point(Integer myInt) {}
					       ^^^^^^^^^^^^^^^^^^^^
				A non-canonical constructor must start with an explicit invocation to a constructor
				----------
				""");
	}
	public void testBug553152_016() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(Integer myInt, int myZ) implements I {
							  public Point {
							     super();
							     this.myInt = 0;
							     this.myZ = 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 8)
					super();
					^^^^^^^^
				The body of a compact constructor must not contain an explicit constructor call
				----------
				""");
	}
	public void testBug553152_017() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public class Inner {
					    record Point(int myInt, char myChar) {}
					  }
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					"""
		},
		"0");
	}
	public void testBug553152_018() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.Target;
							import java.lang.annotation.ElementType;
							class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, char myChar) {}
							 @Target({ElementType.FIELD, ElementType.TYPE})
							 @interface MyAnnotation {}
							"""
				},
			"0");
	}
	public void testBug553152_019() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main(String[] args){
							     System.out.println(0);
							  }
							}
							record Point(int myInt, int myZ) implements I {
							  public static int myInt() {;
							     return 0;
							  }
							}
							interface I {}
							"""
				},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public static int myInt() {;
					                  ^^^^^^^
				The accessor method must not be static
				----------
				""");
	}
public void testBug553153_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					record Point(int myInt, char myChar) implements I {
					  public Point {
						this.myInt = myInt;
						if (this.myInt > 0)  // conditional assignment
							this.myChar = myChar;
					  }
					}
					interface I {}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				this.myInt = myInt;
				^^^^^^^^^^
			Illegal explicit assignment of a final field myInt in compact constructor
			----------
			2. ERROR in X.java (at line 9)
				if (this.myInt > 0)  // conditional assignment
				         ^^^^^
			The blank final field myInt may not have been initialized
			----------
			3. ERROR in X.java (at line 10)
				this.myChar = myChar;
				^^^^^^^^^^^
			Illegal explicit assignment of a final field myChar in compact constructor
			----------
			""");
}
public void testBug553153_003() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				record Point(int myInt, char myChar) implements I {
				  static int f;
				  public Point {
				  }
				}
				interface I {}
				"""
		},
	 "0");
}
public void testBug553153_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				record Point(int myInt, char myChar) implements I {
				  public Point(int myInt, char myChar) {
					this.myInt = myInt;
				  }
				}
				interface I {}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 7)
			public Point(int myInt, char myChar) {
			       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		The blank final field myChar may not have been initialized
		----------
		""");
}
public void testBug558069_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						private record Point(){
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					private record Point(){
					               ^^^^^
				Illegal modifier for the record Point; only public, final and strictfp are permitted
				----------
				""");
}
public void testBug558069_002() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				private record Point(){
				}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
	 "0");
}
public void testBug558069_003() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				private record Point(int myInt){
				}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
	 "0");
}
public void testBug558343_001() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				private record Point(int myInt){
				  @Override
				  public boolean equals(Object obj){
				     return false;
				  }
				}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
	 "0");
}
public void testBug558343_002() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				private record Point(int myInt){
				  @Override
				  public int hashCode(){
				     return java.util.Arrays.hashCode(new int[]{Integer.valueOf(this.myInt).hashCode()});
				  }
				}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
	 "0");
}
public void testBug558343_003() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				record Point(int myInt){
				  @Override
				  public String toString(){
				     return "Point@1";
				  }
				}
				"""
		},
	 "0");
}
public void testBug558343_004() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args){
				     System.out.println(new Point(0).myInt());
				  }
				}
				record Point(int myInt){
				  @Override
				  public String toString(){
				     return "Point@1";
				  }
				}
				"""
		},
	 "0");
}
public void testBug558494_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args){
				     System.out.println(new Point(0).heyPinkCity());
				  }
				}
				record Point(int heyPinkCity){
				  @Override
				  public String toString(){
				     return "Point@1";
				  }
				}
				"""
		},
	 "0");
	String expectedOutput = """
		Record: #Record
		Components:
		 \s
		// Component descriptor #6 I
		int heyPinkCity;
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558494_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args){
				     System.out.println(new Point().toString());
				  }
				}
				record Point(){
				  @Override
				  public String toString(){
				     return "Point@1";
				  }
				}
				"""
		},
	 "Point@1");
	String expectedOutput = """
		Record: #Record
		Components:
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558494_003() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				record Forts(String...wonders){
				}
				public class X {
				       public static void main(String[] args) {
				               Forts p = new Forts(new String[] {"Amber", "Nahargarh", "Jaigarh"});
				               if (!p.toString().startsWith("Forts[wonders=[Ljava.lang.String;@"))
				                   System.out.println("Error");
				       }
				}
				"""
		},
		"");
	String expectedOutput = """
		Record: #Record
		Components:
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Forts.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558494_004() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				record Forts(int x, String[] wonders){
				}
				public class X {
				       public static void main(String[] args) {
				               Forts p = new Forts(3, new String[] {"Amber", "Nahargarh", "Jaigarh"});
				               if (!p.toString().startsWith("Forts[x=3, wonders=[Ljava.lang.String;@"))
				                   System.out.println("Error");
				       }
				}
				"""
		},
		"");
	String expectedOutput =
			"""
		Record: #Record
		Components:
		 \s
		// Component descriptor #6 I
		int x;
		// Component descriptor #8 [Ljava/lang/String;
		java.lang.String[] wonders;
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Forts.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558764_001() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import java.lang.annotation.ElementType;
						record Point(@MyAnnotation int myInt, char myChar) {}
						 @Target({ElementType.FIELD})
						 @interface MyAnnotation {}
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						"""
			},
		"0");
}
public void testBug558764_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import java.lang.annotation.ElementType;
						record Point(@MyAnnotation int myInt, char myChar) {}
						 @Target({ElementType.TYPE})
						 @interface MyAnnotation {}
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					record Point(@MyAnnotation int myInt, char myChar) {}
					             ^^^^^^^^^^^^^
				The annotation @MyAnnotation is disallowed for this location
				----------
				""");
}
public void testBug558764_003() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import java.lang.annotation.ElementType;
						record Point(@MyAnnotation int myInt, char myChar) {}
						 @Target({ElementType.RECORD_COMPONENT})
						 @interface MyAnnotation {}
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						"""
			},
		"0");
}
public void testBug558764_004() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import java.lang.annotation.ElementType;
						record Point(@MyAnnotation int myInt, char myChar) {}
						 @Target({ElementType.RECORD_COMPONENT})
						 @interface MyAnnotation {}
						class X {
						  public @MyAnnotation String f = "hello";
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					public @MyAnnotation String f = "hello";
					       ^^^^^^^^^^^^^
				The annotation @MyAnnotation is disallowed for this location
				----------
				""");
}
public void testBug553567_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						class X extends Record{
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X extends Record{
					                ^^^^^^
				The type X may not subclass Record explicitly
				----------
				""");
}
public void testBug553567_002() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						class Record {
						}
						"""
			},
		"0");
}
public void testBug559281_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record X(void k) {}"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X(void k) {}
					              ^
				void is an invalid type for the component k of a record
				----------
				""");
}
public void testBug559281_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record X(int clone, int wait) {}"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					record X(int clone, int wait) {}
					             ^^^^^
				Illegal component name clone in record X;
				----------
				2. ERROR in X.java (at line 1)
					record X(int clone, int wait) {}
					                        ^^^^
				Illegal component name wait in record X;
				----------
				""");
}
public void testBug559448_001() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						record Point(int x, int... y){
						}
						"""
			},
		"0");
}
public void testBug559448_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						record Point(int... x, int y){
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					record Point(int... x, int y){
					                    ^
				The variable argument type int of the record Point must be the last parameter
				----------
				2. ERROR in X.java (at line 6)
					record Point(int... x, int y){
					                    ^
				The variable argument type int of the method Point must be the last parameter
				----------
				""");
}
public void testBug559448_003() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						record Point(int... x, int... y){
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					record Point(int... x, int... y){
					                    ^
				The variable argument type int of the record Point must be the last parameter
				----------
				2. ERROR in X.java (at line 6)
					record Point(int... x, int... y){
					                    ^
				The variable argument type int of the method Point must be the last parameter
				----------
				""");
}
public void testBug559574_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						record X(int x, int XX3) {
						       public XX3  {}
						       public XX3(int x, int y, int z) {
						               this.x = x;
						               this.y = y;
						       }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public XX3  {}
					       ^^^
				Return type for the method is missing
				----------
				2. ERROR in X.java (at line 3)
					public XX3(int x, int y, int z) {
					       ^^^^^^^^^^^^^^^^^^^^^^^^
				Return type for the method is missing
				----------
				3. WARNING in X.java (at line 3)
					public XX3(int x, int y, int z) {
					               ^
				The parameter x is hiding a field from type X
				----------
				4. ERROR in X.java (at line 5)
					this.y = y;
					     ^
				y cannot be resolved or is not a field
				----------
				""");
}
public void testBug559992_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						record R() {
						  public R throws Exception {
						  }
						}
						"""
			},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public R throws Exception {
				       ^^^^^^^^^^^^^^^^^^
			Throws clause not allowed for canonical constructor R
			----------
			""");
}
public void testBug559992_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						record R() {
						  public R() throws Exception {
						  }
						}
						"""
			},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public R() throws Exception {
				       ^^^^^^^^^^^^^^^^^^^^
			Throws clause not allowed for canonical constructor R
			----------
			""");
}
public void testBug560256_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				final protected record Point(int x, int y){
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				final protected record Point(int x, int y){
				                       ^^^^^
			Illegal modifier for the record Point; only public, final and strictfp are permitted
			----------
			""");
}
public void testBug560256_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				native record Point(int x, int y){
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				native record Point(int x, int y){
				              ^^^^^
			Illegal modifier for the record Point; only public, final and strictfp are permitted
			----------
			""");
}
public void testBug560256_003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				  class Inner {
					  record Point(int x, int y){}
				  }
				}""",
		},
		"0");
}
public void testBug560256_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  static class Inner {
					  native record Point(int x, int y){}
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				native record Point(int x, int y){}
				              ^^^^^
			Illegal modifier for the record Point; only public, private, protected, static, final and strictfp are permitted
			----------
			""");
}
public void testBug560531_001() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						record Point<T>(T t){
						}
						"""
			},
		"0");
}
public void testBug560531_002() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						record R <T extends Integer, S extends String> (int x, T t, S s){
						}
						"""
			},
		"0");
}
public void testBug560569_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface Rentable { int year(); }
				record Car(String model, int year) implements Rentable {
				  public Car {
				  }
				  public String toString() {
				    return model + " " + year;
				  }
				}
				record Camel(int year) implements Rentable { }
				
				class X {
				       String model;
				       int year;
				       public String toString() {
				          return model + " " + year;
				       }
				       public static void main(String[] args) {
				               Car car = new Car("Maruti", 2000);
				               System.out.println(car.hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
	String expectedOutput =
			(this.complianceLevel < ClassFileConstants.JDK9) ?
				"""
					  0 : # 69 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;
						Method arguments:
							#1 Car
							#70 model;year
							#72 REF_getField model:Ljava/lang/String;
							#73 REF_getField year:I
					"""
			:
				"""
					  1 : # 59 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;
						Method arguments:
							#1 Car
							#60 model;year
							#62 REF_getField model:Ljava/lang/String;
							#63 REF_getField year:I\
					""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = 			"""
		  // Method descriptor #12 (Ljava/lang/String;I)V
		  // Stack: 2, Locals: 3
		  public Car(java.lang.String model, int year);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [14]
		     4  aload_0 [this]
		     5  aload_1 [model]
		     6  putfield Car.model : java.lang.String [17]
		     9  aload_0 [this]
		    10  iload_2 [year]
		    11  putfield Car.year : int [19]
		    14  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		      Local variable table:
		        [pc: 0, pc: 15] local: this index: 0 type: Car
		        [pc: 0, pc: 15] local: model index: 1 type: java.lang.String
		        [pc: 0, pc: 15] local: year index: 2 type: int
		      Method Parameters:
		        mandated model
		        mandated year
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560496_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				record R () {}\s
				class X {
				       public static void main(String[] args) {
				               System.out.println(new R().hashCode());
				       }
				}
				"""
		},
	 "0");
	String expectedOutput =
			"public final int hashCode();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560496_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				strictfp record R () {}\s
				class X {
				       public static void main(String[] args) {
				               System.out.println(new R().hashCode());
				       }
				}
				"""
		},
	 "0");
	String expectedOutput =
			"public final strictfp int hashCode();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560797_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				strictfp record R (int x, int y) {}\s
				class X {
				       public static void main(String[] args) {
				               System.out.println(new R(100, 200).hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
	String expectedOutput =
			"public strictfp int x();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560797_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				strictfp record R (int x, int y) {\s
				public int x() { return this.x;}
				}
				class X {
				       public static void main(String[] args) {
				               System.out.println(new R(100, 200).hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
	String expectedOutput =
			"public strictfp int x();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560798_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.Target;
				import java.lang.annotation.ElementType;
				@Target({ElementType.PARAMETER})
				@interface MyAnnot {}
				record R(@MyAnnot()  int i, int j) {}
				class X {
				       public static void main(String[] args) {
				           System.out.println(new R(100, 200).hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
}
public void testBug560798_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.Target;
				import java.lang.annotation.ElementType;
				@Target({ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE,
					ElementType.MODULE, ElementType.PACKAGE, ElementType.TYPE, ElementType.TYPE_PARAMETER})
				@interface MyAnnot {}
				record R(@MyAnnot()  int i, int j) {}
				class X {
				       public static void main(String[] args) {
				       }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				record R(@MyAnnot()  int i, int j) {}
				         ^^^^^^^^
			The annotation @MyAnnot is disallowed for this location
			----------
			""");
}
public void testBug560798_003() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.Target;
				import java.lang.annotation.ElementType;
				@Target({ElementType.METHOD})
				@interface MyAnnot {}
				record R(@MyAnnot()  int i, int j) {}
				class X {
				       public static void main(String[] args) {
				           System.out.println(new R(100, 200).hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
}
public void testBug560798_004() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.Target;
				import java.lang.annotation.ElementType;
				@Target({ElementType.RECORD_COMPONENT})
				@interface MyAnnot {}
				record R(@MyAnnot()  int i, int j) {}
				class X {
				       public static void main(String[] args) {
				           System.out.println(new R(100, 200).hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
}
public void testBug560798_005() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.Target;
				import java.lang.annotation.ElementType;
				@Target({ElementType.TYPE_USE})
				@interface MyAnnot {}
				record R(@MyAnnot()  int i, int j) {}
				class X {
				       public static void main(String[] args) {
				           System.out.println(new R(100, 200).hashCode() != 0);
				       }
				}
				"""
		},
	 "true");
}
public void testBug560893_001() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					interface I{
					record R(int x, int y) {}
					}
					class X {
					       public static void main(String[] args) {
					           System.out.println(0);
					       }
					}
					"""
			},
		 "0");
}
public void testBug560893_002() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					           record R(int x, int y) {}
					           System.out.println(0);
					       }
					}
					"""
			},
		 "0");
}
public void testBug560893_003() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					           record R(int x, int y) {}
					           R r =  new R(100,200);
					           System.out.println(r.x());
					       }
					}
					"""
			},
		 "100");
}
public void testBug560893_004() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					           record R(int x, int y) {
					               static int i;
					       	}
					           R r =  new R(100,200);
					           System.out.println(r.x());
					       }
					}
					"""
			},
		 "100");
}
public void testBug560893_005() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					           record R(int x, int y) {
					               static int i;
					               public void ff() {
					                	int jj;
					       		}
					               static int ii;
					       	}
					           R r =  new R(100,200);
					           System.out.println(r.x());
					       }
					}
					"""
			},
		 "100");
}
public void testBug560893_006() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					           record R(int x, int y) {}
					           R r =  new R(100,200);
					           System.out.println(r.x());
					       }
					}
					"""
			},
		 "100");
}
public void testBug560893_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				    static int si;
				    int nsi;
				
				    void m() {
				        int li;
				
				        record R(int r) {
				            void print() {
				                System.out.println(li);  // error, local variable
				                System.out.println(nsi); // error, non-static member
				                System.out.println(si);  // ok, static member of enclosing class
				            }
				        }
				        R r = new R(10);
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				System.out.println(li);  // error, local variable
				                   ^^
			Cannot make a static reference to the non-static variable li
			----------
			2. ERROR in X.java (at line 11)
				System.out.println(nsi); // error, non-static member
				                   ^^^
			Cannot make a static reference to the non-static field nsi
			----------
			""");
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug558718_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				record R() {}
				       ^
			The Java feature 'Records' is only available with source level 16 and above
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug558718_002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				record R() {}
				       ^
			The Java feature 'Records' is only available with source level 16 and above
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug558718_003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
	"""
		----------
		1. ERROR in X.java (at line 1)
			record R() {}
			       ^
		The Java feature 'Records' is only available with source level 16 and above
		----------
		""",
		null,
		true,
		options
	);
}
public void testBug56180_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				record R () {}\s
				class X {
				       public static void main(String[] args) {
				               System.out.println(new R().toString());
				       }
				}
				"""
		},
	 "R[]");
	String expectedOutput =
			" public final java.lang.String toString();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug561528_001() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						interface Node<N> {}
						
						record R <N extends Node<?>> (N value){
						}
						"""
			},
		"0");
}
public void testBug561528_002() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						interface Node<N> {}
						
						record R <N extends Node<N>> (R<N> parent, N element){
						}
						"""
			},
		"0");
}
public void testBug561528_003() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						interface Node<N> {}
						
						interface AB<N> {}
						
						record R <N extends Node<AB<N>>> (N value){
						}
						"""
			},
		"0");
}
public void testBug561528_004() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						interface Node<N> {}
						
						interface AB<N> {}
						
						interface CD<N> {}
						
						record R <N extends Node<AB<CD<N>>>> (N value){
						}
						"""
			},
		"0");
}
public void testBug561528_005() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						interface Node<N> {}
						
						interface AB<N> {}
						
						interface CD<N> {}
						
						record R <N extends Node<AB<CD<N>>>>> (N value){
						}
						"""
			},
		"""
			----------
			1. ERROR in X.java (at line 12)
				record R <N extends Node<AB<CD<N>>>>> (N value){
				                                ^^^
			Syntax error on token ">>>", >> expected
			----------
			""",
		null,
		true
	);
}
public void testBug561778_001() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"XTest.java",
					"""
						public class XTest{
							static <T> T test(X<T> box) {
								return box.value(); /* */
							}
						   public static void main(String[] args) {
						       System.out.println(0);
						   }
						}
						""",
					"X.java",
					"public record X<T>(T value) {\n" +
					"}"
			},
		"0");
	String expectedOutput =
			"""
		  // Method descriptor #10 (Ljava/lang/Object;)V
		  // Signature: (TT;)V
		  // Stack: 2, Locals: 2
		  public X(java.lang.Object value);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [13]
		     4  aload_0 [this]
		     5  aload_1 [value]
		     6  putfield X.value : java.lang.Object [16]
		     9  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		        [pc: 0, pc: 10] local: value index: 1 type: java.lang.Object
		      Local variable type table:
		        [pc: 0, pc: 10] local: this index: 0 type: X<T>
		        [pc: 0, pc: 10] local: value index: 1 type: T
		      Method Parameters:
		        value
		 \s
		  // Method descriptor #25 ()Ljava/lang/Object;
		  // Signature: ()TT;
		  // Stack: 1, Locals: 1
		  public java.lang.Object value();
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug561778_002() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"XTest.java",
					"""
						public class XTest{
							static <T> Y<T> test(X<T> box) {
								return box.value(); /* */
							}
						   public static void main(String[] args) {
						       System.out.println(0);
						   }
						}
						""",
					"X.java",
					"""
						public record X<T>(Y<T> value) {
						}
						class Y<T> {
						}"""
			},
		"0");
	String expectedOutput =
			"""
		  // Method descriptor #25 ()LY;
		  // Signature: ()LY<TT;>;
		  // Stack: 1, Locals: 1
		  public Y value();
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562219_001() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					       public static void main(String[] args) {
					               @SuppressWarnings("unused")
					               class Y {
					                       @SuppressWarnings("preview")
					                       class Z {
					                               record R() {
					                                      \s
					                               }
					                       }
					               }
					       }
					}
					"""
			},
		"");
}
public void testBug562219_002() {
	runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        @SuppressWarnings("unused")
					        class Y {
					           record R() {}
					        }
					    }
					}
					"""
			},
		""
	);
}
/*
 * Test that annotation with implicit target as METHOD are included in the
 * generated bytecode on the record component and its accessor method
 */
public void test562250a() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.*;
						import java.lang.reflect.*;
						
						record Point(@Annot int a) {
						}
						@Retention(RetentionPolicy.RUNTIME)
						@interface Annot {
						}
						public class X {
							public static void main(String[] args) throws Exception {
									Class<?> cls = Class.forName("Point");
									RecordComponent[] recordComponents = cls.getRecordComponents();
									for (RecordComponent recordComponent : recordComponents) {
										Annotation[] annotations = recordComponent.getAnnotations();
										System.out.println("RecordComponents:");
										for (Annotation annot : annotations) {
											System.out.println(annot);
										}
										Method accessor = recordComponent.getAccessor();
										System.out.println("Accessors:");
										annotations =accessor.getAnnotations();
										for (Annotation annot : annotations) {
											System.out.println(annot);
										}
									}
							}
						}"""
			},
		"""
			RecordComponents:
			@Annot()
			Accessors:
			@Annot()""");
}
/*
 * Test that annotation with explicit target as METHOD are included in the
 * generated bytecode on its accessor method (and not on record component)
 */
public void test562250b() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.*;
						import java.lang.reflect.*;
						
						record Point(@Annot int a) {
						}
						@Target({ElementType.METHOD})
						@Retention(RetentionPolicy.RUNTIME)
						@interface Annot {
						}
						public class X {
							public static void main(String[] args) throws Exception {
									Class<?> cls = Class.forName("Point");
									RecordComponent[] recordComponents = cls.getRecordComponents();
									for (RecordComponent recordComponent : recordComponents) {
										Annotation[] annotations = recordComponent.getAnnotations();
										System.out.println("RecordComponents:");
										for (Annotation annot : annotations) {
											System.out.println(annot);
										}
										Method accessor = recordComponent.getAccessor();
										System.out.println("Accessors:");
										annotations =accessor.getAnnotations();
										for (Annotation annot : annotations) {
											System.out.println(annot);
										}
									}
							}
						}"""
			},
		"""
			RecordComponents:
			Accessors:
			@Annot()""");
}
/*
 * Test that even though annotations with FIELD as a target are permitted by the
 * compiler on a record component, the generated bytecode doesn't contain these annotations
 * on the record component.
 */
public void test562250c() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.*;
						import java.lang.reflect.*;
						
						record Point(@Annot int a) {
						}
						@Target({ElementType.FIELD})
						@Retention(RetentionPolicy.RUNTIME)
						@interface Annot {
						}
						public class X {
							public static void main(String[] args) throws Exception {
									Class<?> cls = Class.forName("Point");
									RecordComponent[] recordComponents = cls.getRecordComponents();
									for (RecordComponent recordComponent : recordComponents) {
										Annotation[] annotations = recordComponent.getAnnotations();
										System.out.println("RecordComponents:");
										for (Annotation annot : annotations) {
											System.out.println(annot);
										}
										Method accessor = recordComponent.getAccessor();
										System.out.println("Accessors:");
										annotations =accessor.getAnnotations();
										for (Annotation annot : annotations) {
											System.out.println(annot);
										}
									}
							}
						}"""
			},
		"RecordComponents:\n" +
		"Accessors:");
}
public void testBug562439_001() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				      Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RC int myInt, char myChar) {\s
				}  \s
				
				@Target({ElementType.RECORD_COMPONENT})
				@interface RC {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		Record: #Record
		Components:
		 \s
		// Component descriptor #6 I
		int myInt;
		  RuntimeInvisibleAnnotations:\s
		    #62 @RC(
		    )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_002() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RC int myInt, char myChar) {\s
				}  \s
				
				@Target({ElementType.RECORD_COMPONENT})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RC {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		Record: #Record
		Components:
		 \s
		// Component descriptor #6 I
		int myInt;
		  RuntimeVisibleAnnotations:\s
		    #62 @RC(
		    )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_003() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCF int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD})
				@interface RCF {}
				"""
		},
		"100");
	String expectedOutput = """
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeInvisibleAnnotations:\s
		      #8 @RCF(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeInvisibleAnnotations:\s
				    #8 @RCF(
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_004() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCF int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCF {}
				"""
		},
		"100");
	String expectedOutput = """
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeVisibleAnnotations:\s
		      #8 @RCF(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleAnnotations:\s
				    #8 @RCF(
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_005() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RF int myInt, char myChar) {\s
				}  \s
				@Target({ElementType.FIELD})
				@interface RF {}
				"""
		},
		"100");
	String expectedOutput = """
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeInvisibleAnnotations:\s
		      #8 @RF(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				// Component descriptor #10 C
				char myChar;
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_006() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RF int myInt, char myChar) {\s
				}  \s
				@Target({ElementType.FIELD})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RF {}
				"""
		},
		"100");
	String expectedOutput = """
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeVisibleAnnotations:\s
		      #8 @RF(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				// Component descriptor #10 C
				char myChar;
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_007() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCFU int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.TYPE_USE})
				@interface RCFU {}
				"""
		},
		"100");
	String expectedOutput = 			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeInvisibleAnnotations:\s
		      #8 @RCFU(
		      )
		    RuntimeInvisibleTypeAnnotations:\s
		      #8 @RCFU(
		        target type = 0x13 FIELD
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = 			"""
		Record: #Record
		Components:
		 \s
		// Component descriptor #6 I
		int myInt;
		  RuntimeInvisibleAnnotations:\s
		    #8 @RCFU(
		    )
		  RuntimeInvisibleTypeAnnotations:\s
		    #8 @RCFU(
		      target type = 0x13 FIELD
		    )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_008() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCFU int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCFU {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeVisibleAnnotations:\s
		      #8 @RCFU(
		      )
		    RuntimeVisibleTypeAnnotations:\s
		      #8 @RCFU(
		        target type = 0x13 FIELD
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleAnnotations:\s
				    #8 @RCFU(
				    )
				  RuntimeVisibleTypeAnnotations:\s
				    #8 @RCFU(
				      target type = 0x13 FIELD
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_009() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCM int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})
				@interface RCM {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 1
		  public int myInt();
		    0  aload_0 [this]
		    1  getfield Point.myInt : int [15]
		    4  ireturn
		      Line numbers:
		        [pc: 0, line: 11]
		    RuntimeInvisibleAnnotations:\s
		      #26 @RCM(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeInvisibleAnnotations:\s
				    #26 @RCM(
				    )
				// Component descriptor #8 C
				char myChar;
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_010() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCM int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCM {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  public int myInt();
		    0  aload_0 [this]
		    1  getfield Point.myInt : int [15]
		    4  ireturn
		      Line numbers:
		        [pc: 0, line: 13]
		    RuntimeVisibleAnnotations:\s
		      #26 @RCM(
		      )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleAnnotations:\s
				    #26 @RCM(
				    )
				// Component descriptor #8 C
				char myChar;
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_011() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@M int myInt, char myChar) {\s
				}  \s
				@Target({ElementType.METHOD})
				@interface M {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 1
		  public int myInt();
		    0  aload_0 [this]
		    1  getfield Point.myInt : int [15]
		    4  ireturn
		      Line numbers:
		        [pc: 0, line: 11]
		    RuntimeInvisibleAnnotations:\s
		      #26 @M(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				// Component descriptor #8 C
				char myChar;
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_012() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@M int myInt, char myChar) {\s
				}  \s
				@Target({ElementType.METHOD})
				@Retention(RetentionPolicy.RUNTIME)
				@interface M {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  public int myInt();
		    0  aload_0 [this]
		    1  getfield Point.myInt : int [15]
		    4  ireturn
		      Line numbers:
		        [pc: 0, line: 13]
		    RuntimeVisibleAnnotations:\s
		      #26 @M(
		      )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				// Component descriptor #8 C
				char myChar;
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_013() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCMU int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})
				@interface RCMU {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeInvisibleTypeAnnotations:\s
		      #8 @RCMU(
		        target type = 0x13 FIELD
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  // Method descriptor #26 ()I
				  // Stack: 1, Locals: 1
				  public int myInt();
				    0  aload_0 [this]
				    1  getfield Point.myInt : int [17]
				    4  ireturn
				      Line numbers:
				        [pc: 0, line: 11]
				    RuntimeInvisibleAnnotations:\s
				      #8 @RCMU(
				      )
				    RuntimeInvisibleTypeAnnotations:\s
				      #8 @RCMU(
				        target type = 0x14 METHOD_RETURN
				      )
				 \s
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				// Component descriptor #6 I
				int myInt;
				  RuntimeInvisibleAnnotations:\s
				    #8 @RCMU(
				    )
				  RuntimeInvisibleTypeAnnotations:\s
				    #8 @RCMU(
				      target type = 0x13 FIELD
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_014() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCMU int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCMU {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeVisibleTypeAnnotations:\s
		      #8 @RCMU(
		        target type = 0x13 FIELD
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  // Method descriptor #26 ()I
				  // Stack: 1, Locals: 1
				  public int myInt();
				    0  aload_0 [this]
				    1  getfield Point.myInt : int [17]
				    4  ireturn
				      Line numbers:
				        [pc: 0, line: 13]
				    RuntimeVisibleAnnotations:\s
				      #8 @RCMU(
				      )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleAnnotations:\s
				    #8 @RCMU(
				    )
				  RuntimeVisibleTypeAnnotations:\s
				    #8 @RCMU(
				      target type = 0x13 FIELD
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_015() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				      Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@T int myInt, char myChar) {\s
				}  \s
				
				@Target({ElementType.TYPE_USE})
				@interface T {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeInvisibleTypeAnnotations:\s
		      #8 @T(
		        target type = 0x13 FIELD
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  // Method descriptor #26 ()I
				  // Stack: 1, Locals: 1
				  public int myInt();
				    0  aload_0 [this]
				    1  getfield Point.myInt : int [17]
				    4  ireturn
				      Line numbers:
				        [pc: 0, line: 11]
				    RuntimeInvisibleTypeAnnotations:\s
				      #8 @T(
				        target type = 0x14 METHOD_RETURN
				      )
				  \
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeInvisibleTypeAnnotations:\s
				    #8 @T(
				      target type = 0x13 FIELD
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  Point(int myInt, char myChar);
				     0  aload_0 [this]
				     1  invokespecial java.lang.Record() [14]
				     4  aload_0 [this]
				     5  iload_1 [myInt]
				     6  putfield Point.myInt : int [17]
				     9  aload_0 [this]
				    10  iload_2 [myChar]
				    11  putfield Point.myChar : char [19]
				    14  return
				      Line numbers:
				        [pc: 0, line: 11]
				      Local variable table:
				        [pc: 0, pc: 15] local: this index: 0 type: Point
				        [pc: 0, pc: 15] local: myInt index: 1 type: int
				        [pc: 0, pc: 15] local: myChar index: 2 type: char
				      Method Parameters:
				        myInt
				        myChar
				    RuntimeInvisibleTypeAnnotations:\s
				      #8 @T(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				      )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_016() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@T int myInt, char myChar) {\s
				}  \s
				
				@Target({ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface T {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeVisibleTypeAnnotations:\s
		      #8 @T(
		        target type = 0x13 FIELD
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  public int myInt();
				    0  aload_0 [this]
				    1  getfield Point.myInt : int [17]
				    4  ireturn
				      Line numbers:
				        [pc: 0, line: 13]
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @T(
				        target type = 0x14 METHOD_RETURN
				      )
				  \
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleTypeAnnotations:\s
				    #8 @T(
				      target type = 0x13 FIELD
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  Point(int myInt, char myChar);
				     0  aload_0 [this]
				     1  invokespecial java.lang.Record() [14]
				     4  aload_0 [this]
				     5  iload_1 [myInt]
				     6  putfield Point.myInt : int [17]
				     9  aload_0 [this]
				    10  iload_2 [myChar]
				    11  putfield Point.myChar : char [19]
				    14  return
				      Line numbers:
				        [pc: 0, line: 13]
				      Local variable table:
				        [pc: 0, pc: 15] local: this index: 0 type: Point
				        [pc: 0, pc: 15] local: myInt index: 1 type: int
				        [pc: 0, pc: 15] local: myChar index: 2 type: char
				      Method Parameters:
				        myInt
				        myChar
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @T(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				      )
				 \s
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_017() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCP int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
				@interface RCP {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  Point(int myInt, char myChar);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [14]
		     4  aload_0 [this]
		     5  iload_1 [myInt]
		     6  putfield Point.myInt : int [17]
		     9  aload_0 [this]
		    10  iload_2 [myChar]
		    11  putfield Point.myChar : char [19]
		    14  return
		      Line numbers:
		        [pc: 0, line: 11]
		      Local variable table:
		        [pc: 0, pc: 15] local: this index: 0 type: Point
		        [pc: 0, pc: 15] local: myInt index: 1 type: int
		        [pc: 0, pc: 15] local: myChar index: 2 type: char
		      Method Parameters:
		        myInt
		        myChar
		    RuntimeInvisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #12 @RCP(
		        )
		      Number of annotations for parameter 1: 0
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeInvisibleAnnotations:\s
				    #12 @RCP(
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_018() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@RCP int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCP {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  Point(int myInt, char myChar);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [14]
		     4  aload_0 [this]
		     5  iload_1 [myInt]
		     6  putfield Point.myInt : int [17]
		     9  aload_0 [this]
		    10  iload_2 [myChar]
		    11  putfield Point.myChar : char [19]
		    14  return
		      Line numbers:
		        [pc: 0, line: 13]
		      Local variable table:
		        [pc: 0, pc: 15] local: this index: 0 type: Point
		        [pc: 0, pc: 15] local: myInt index: 1 type: int
		        [pc: 0, pc: 15] local: myChar index: 2 type: char
		      Method Parameters:
		        myInt
		        myChar
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #12 @RCP(
		        )
		      Number of annotations for parameter 1: 0
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleAnnotations:\s
				    #12 @RCP(
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_019() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@Annot int myInt, char myChar) {\s
				}  \s
				@interface Annot {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  // Field descriptor #6 I
		  private final int myInt;
		    RuntimeInvisibleAnnotations:\s
		      #8 @Annot(
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  Point(int myInt, char myChar);
				     0  aload_0 [this]
				     1  invokespecial java.lang.Record() [15]
				     4  aload_0 [this]
				     5  iload_1 [myInt]
				     6  putfield Point.myInt : int [18]
				     9  aload_0 [this]
				    10  iload_2 [myChar]
				    11  putfield Point.myChar : char [20]
				    14  return
				      Line numbers:
				        [pc: 0, line: 11]
				      Local variable table:
				        [pc: 0, pc: 15] local: this index: 0 type: Point
				        [pc: 0, pc: 15] local: myInt index: 1 type: int
				        [pc: 0, pc: 15] local: myChar index: 2 type: char
				      Method Parameters:
				        myInt
				        myChar
				    RuntimeInvisibleParameterAnnotations:\s
				      Number of annotations for parameter 0: 1
				        #8 @Annot(
				        )
				      Number of annotations for parameter 1: 0
				 \s
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				  // Method descriptor #27 ()I
				  // Stack: 1, Locals: 1
				  public int myInt();
				    0  aload_0 [this]
				    1  getfield Point.myInt : int [18]
				    4  ireturn
				      Line numbers:
				        [pc: 0, line: 11]
				    RuntimeInvisibleAnnotations:\s
				      #8 @Annot(
				      )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeInvisibleAnnotations:\s
				    #8 @Annot(
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_020() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				                         \s
				public class X {\s
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }\s
				}
				
				record Point(@Annot int myInt, char myChar) {\s
				}  \s
				@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
				@Retention(RetentionPolicy.RUNTIME)
				@interface Annot {}
				"""
		},
		"100");
	String expectedOutput =
			"""
		  Point(int myInt, char myChar);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [14]
		     4  aload_0 [this]
		     5  iload_1 [myInt]
		     6  putfield Point.myInt : int [17]
		     9  aload_0 [this]
		    10  iload_2 [myChar]
		    11  putfield Point.myChar : char [19]
		    14  return
		      Line numbers:
		        [pc: 0, line: 13]
		      Local variable table:
		        [pc: 0, pc: 15] local: this index: 0 type: Point
		        [pc: 0, pc: 15] local: myInt index: 1 type: int
		        [pc: 0, pc: 15] local: myChar index: 2 type: char
		      Method Parameters:
		        myInt
		        myChar
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #12 @Annot(
		        )
		      Number of annotations for parameter 1: 0
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"""
				Record: #Record
				Components:
				 \s
				// Component descriptor #6 I
				int myInt;
				  RuntimeVisibleAnnotations:\s
				    #12 @Annot(
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug563178_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				record Point(final int x, int y){
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				record Point(final int x, int y){
				                       ^
			A record component x cannot have modifiers
			----------
			""");
}
public void testBug563183_001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  public X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  public X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  protected X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				protected X() {}
				          ^^^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  protected X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				protected X {}
				          ^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  /*package */ X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/*package */ X() {}
				             ^^^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  /*package */ X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/*package */ X {}
				             ^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  private X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private X() {}
				        ^^^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X() {
				  private X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private X {}
				        ^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    public R() {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    public R {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    protected R() {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    protected R {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    /*package */ R() {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				/*package */ R() {}
				             ^^^
			Cannot reduce the visibility of a canonical constructor R from that of the record
			----------
			""");
}
public void testBug563183_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    /*package */ R {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				/*package */ R {}
				             ^
			Cannot reduce the visibility of a canonical constructor R from that of the record
			----------
			""");
}
public void testBug563183_015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    private R() {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				private R() {}
				        ^^^
			Cannot reduce the visibility of a canonical constructor R from that of the record
			----------
			""");
}
public void testBug563183_016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  protected record R() {
				    private R {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				private R {}
				        ^
			Cannot reduce the visibility of a canonical constructor R from that of the record
			----------
			""");
}
public void testBug563183_017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/*package */ record X() {
				  public X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/*package */ record X() {
				  public X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				record X() {
				  protected X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				record X() {
				  protected X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				 record X() {
				  /*package */ X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				 record X() {
				  /*package */ X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				record X() {
				  private X() {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private X() {}
				        ^^^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				record X() {
				  private X {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private X {}
				        ^
			Cannot reduce the visibility of a canonical constructor X from that of the record
			----------
			""");
}
public void testBug563183_025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  private record R() {
				    public R() {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  private record R() {
				    protected R {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  private record R() {
				    /* package */ R() {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563183_028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  private record R() {
				    private R {}
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug563184_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				record X(int angel) {
				  X(int devil) {
				     this.angel = devil;
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				X(int devil) {
				      ^^^^^
			Illegal parameter name devil in canonical constructor, expected angel, the corresponding component name
			----------
			""");
}
public void testBug563184_002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				record X(int myInt) {
				  X(int myInt) {
				     this.myInt = myInt;
				  }
				  X(int i, int j) {
				    this(i);
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}""",
		},
		"0");
}
public void testBug562637_001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public record X(int i) {
				    public X {
				            i = i/2;
				    }
				    public static void main(String[] args) {
				            System.out.println(new X(10).i());
				    }
				}""",
		},
		"5");
}
	public void testBug563181_01() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							public class X {\s
							  public static void main(String[] args){}
							}
							record Point(@RCMU int myInt, char myChar) {\s
							  public int myInt(){
							     return this.myInt;
							  }
							}  \s
							@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})
							@interface RCMU {}
							"""
				},
				"");
		String expectedOutput =
				"""
			  // Method descriptor #26 ()I
			  // Stack: 1, Locals: 1
			  public int myInt();
			    0  aload_0 [this]
			    1  getfield Point.myInt : int [17]
			    4  ireturn
			      Line numbers:
			        [pc: 0, line: 8]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: Point
			 \s
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug563181_02() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							public class X {\s
							  public static void main(String[] args){}
							}
							record Point(@RCMU int myInt, char myChar) {
							  @RCMU public int myInt(){
							     return this.myInt;
							  }
							}
							@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})
							@interface RCMU {}
							"""
				},
				"");
		String expectedOutput =
				"""
			  // Method descriptor #26 ()I
			  // Stack: 1, Locals: 1
			  public int myInt();
			    0  aload_0 [this]
			    1  getfield Point.myInt : int [17]
			    4  ireturn
			      Line numbers:
			        [pc: 0, line: 8]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: Point
			    RuntimeInvisibleAnnotations:\s
			      #8 @RCMU(
			      )
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @RCMU(
			        target type = 0x14 METHOD_RETURN
			      )
			 \s
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug563181_03() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.*;
							public class X {\s
							  public static void main(String[] args){}
							}
							record Point(@TypeAnnot @SimpleAnnot int myInt, char myChar) {}
							@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})
							@Retention(RetentionPolicy.RUNTIME)
							@interface SimpleAnnot {}
							@Target({ ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
							@Retention(RetentionPolicy.RUNTIME)
							@interface TypeAnnot {}
							"""
				},
				"");
		String expectedOutput =
				"""
			  // Method descriptor #26 ()I
			  // Stack: 1, Locals: 1
			  public int myInt();
			    0  aload_0 [this]
			    1  getfield Point.myInt : int [17]
			    4  ireturn
			      Line numbers:
			        [pc: 0, line: 5]
			    RuntimeVisibleAnnotations:\s
			      #28 @SimpleAnnot(
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @TypeAnnot(
			        target type = 0x14 METHOD_RETURN
			      )
			 \s
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug563181_04() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.*;
							public class X {\s
							  public static void main(String[] args){}
							}
							record Point(@TypeAnnot @SimpleAnnot int myInt, char myChar) {
							  @TypeAnnot @SimpleAnnot public int myInt(){
							     return this.myInt;
							  }
							}
							@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})
							@Retention(RetentionPolicy.RUNTIME)
							@interface SimpleAnnot {}
							@Target({ ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
							@Retention(RetentionPolicy.RUNTIME)
							@interface TypeAnnot {}
							"""
				},
				"");
		String expectedOutput =
				"""
			 // Method descriptor #26 ()I
			  // Stack: 1, Locals: 1
			  public int myInt();
			    0  aload_0 [this]
			    1  getfield Point.myInt : int [17]
			    4  ireturn
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: Point
			    RuntimeVisibleAnnotations:\s
			      #28 @SimpleAnnot(
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @TypeAnnot(
			        target type = 0x14 METHOD_RETURN
			      )
			 \s
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					  public record R() {}
					  public static void main(String[] args){}
					}
					"""
			},
		"");
		String expectedOutput =
				"""
			  // Stack: 1, Locals: 1
			  public X$R();
			    0  aload_0 [this]
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_002() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					  record R() {}
					  public static void main(String[] args){}
					}
					"""
			},
		"");
		String expectedOutput =
				"""
			  // Stack: 1, Locals: 1
			  X$R();
			    0  aload_0 [this]
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_003() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					  protected record R() {}
					  public static void main(String[] args){}
					}
					"""
			},
		"");
		String expectedOutput =
				"""
			  // Stack: 1, Locals: 1
			  protected X$R();
			    0  aload_0 [this]
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_004() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					  private record R() {}
					  public static void main(String[] args){}
					}
					"""
			},
		"");
		String expectedOutput =
				"""
			  // Stack: 1, Locals: 1
			  private X$R();
			    0  aload_0 [this]
			""";
		RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564146_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public record X(int i) {
					  public X() {
					    this.i = 10;
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public X() {
					       ^^^
				A non-canonical constructor must start with an explicit invocation to a constructor
				----------
				""");
	}
	public void testBug564146_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public record X(int i) {
					  public X() {
					    super();
					    this.i = 10;
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public X() {
					       ^^^
				A non-canonical constructor must start with an explicit invocation to a constructor
				----------
				""");
	}
	public void testBug564146_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public record X(int i) {
					  public X(int i) {
					    this.i = 10;
					    Zork();
					  }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug564146_004() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public record X(int i) {
					 public X() {
					   this(10);
					 }
					 public static void main(String[] args) {
					   System.out.println(new X().i());
					 }
					}"""
				},
			"10");
	}
	public void testBug564146_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public record X() {
					 public X(int i) {
					   this(10);
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					this(10);
					^^^^^^^^^
				Recursive constructor invocation X(int)
				----------
				""");
	}
	public void testBug564146_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public record X() {
					 public X() {
					   System.out.println(10);
					   this(10);
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					this(10);
					^^^^^^^^^
				The body of a canonical constructor must not contain an explicit constructor call
				----------
				""");
	}
	public void testBug564146_007() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public record X(int i) {
					 public X() {
					   this(10);
					 }
					 public X(int i, int k) {
					   this();
					 }
					 public static void main(String[] args) {
					   System.out.println(new X(2, 3).i());
					 }
					}"""
				},
			"10");
	}

public void testBug564672_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X extends record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				class record {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 6)
				class record {}
				      ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X extends record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X implements record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				interface record {}
				;\
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 5)
				interface record {}
				          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X implements record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y extends record {
				  }
				  class record {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				class record {}
				      ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y extends record {
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y implements record {
				  }
				  interface record {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				interface record {}
				          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y implements record {
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Y extends record {
				}
				interface record {}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 3)
				interface record {}
				          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Y extends record {
				}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  interface Y extends record {
				  }
				  interface record {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				interface record {}
				          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  interface Y extends record {
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  class Y extends record {
				  }
				  class record {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				class record {}
				      ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  class Y extends record {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  class Y implements record {
				  }
				  interface record {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				interface record {}
				          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  class Y implements record {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  interface Y extends record {
				  }
				  interface record {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				interface record {}
				          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  interface Y extends record {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static record a(int i, int j) {
						record r=new record(i,j);
						return r;
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				record r=new record(i,j);
				^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 3)
				record r=new record(i,j);
				             ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			3. ERROR in X.java (at line 4)
				return r;
				^^^^^^
			Syntax error on token "return", byte expected
			----------
			""");
}
public void testBug564672_020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					class record {};
					static record a(int i, int j) {
						record r=new record();
						return r;
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class record {};
				      ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				record r=new record();
				^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			3. ERROR in X.java (at line 4)
				record r=new record();
				             ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			4. ERROR in X.java (at line 5)
				return r;
				^^^^^^
			Syntax error on token "return", byte expected
			----------
			""");
}
public void testBug564672_021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					interface IPoint {
					}
					record Point(int x, int y) implements IPoint {}
					static IPoint a(int i, int j) {
						Point record=new Point(i,j);
						return record;
					}
					public static void main(String[] args) {
						System.out.println(a(5,10));
					}
				}
				"""
		},
		"Point[x=5, y=10]");
}
public void testBug564672_022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					record R(int i){}\s
					interface IPoint {
						record a(int i) {
				       	System.out.println(0);
				           return new R(i);
						}
					}
					record Point(int x, int y) implements IPoint {}
					static IPoint a(int i, int j) {
						Point record=new Point(i,j);
						record.a(1);
						return record;
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				System.out.println(0);
				          ^
			Syntax error on token ".", @ expected after this token
			----------
			2. ERROR in X.java (at line 5)
				System.out.println(0);
			           return new R(i);
				                   ^^^^^^^^^^^^^^^^^^^^^
			Syntax error on tokens, delete these tokens
			----------
			3. ERROR in X.java (at line 6)
				return new R(i);
				              ^
			Syntax error, insert ")" to complete SingleMemberAnnotation
			----------
			4. ERROR in X.java (at line 6)
				return new R(i);
				              ^
			Syntax error, insert "SimpleName" to complete QualifiedName
			----------
			5. ERROR in X.java (at line 6)
				return new R(i);
				              ^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			6. ERROR in X.java (at line 6)
				return new R(i);
				              ^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			7. ERROR in X.java (at line 12)
				record.a(1);
				       ^
			The method a(int) is undefined for the type X.Point
			----------
			""");
}
public void testBug564672_023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					interface IPoint {
					}
					record Point(int x, int y) implements IPoint {}
					static IPoint a(int i, int j) throws record{
						Point record=new Point(i,j);
						return record;
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				static IPoint a(int i, int j) throws record{
				                                     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X() throws record {}\s
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				X() throws record {}\s
				           ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
					int a() throws record;\s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int a() throws record;\s
				               ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;\
				public class X {
					List<record> R = new List<record>();
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				List<record> R = new List<record>();
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 2)
				List<record> R = new List<record>();
				                     ^^^^
			Cannot instantiate the type List<record>
			----------
			3. ERROR in X.java (at line 2)
				List<record> R = new List<record>();
				                          ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_027() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<S> {
					void print(S arg);
				}
				public class X implements I<record>{
					void print(record arg){
						System.out.println(arg);
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				public class X implements I<record>{
				             ^
			The type X must implement the inherited abstract method I<record>.print(record)
			----------
			2. ERROR in X.java (at line 4)
				public class X implements I<record>{
				                            ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			3. ERROR in X.java (at line 5)
				void print(record arg){
				           ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_028() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y<record> {
					void equal(record R) {}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class Y<record> {
				        ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 2)
				void equal(record R) {}
				           ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_029() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y<record> {
					Y(record R) {}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class Y<record> {
				        ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 2)
				Y(record R) {}
				  ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static record i= 0;
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				static record i= 0;
				       ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					record i=0;
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				record i=0;
				^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static int sum(record i, int param){
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				static int sum(record i, int param){
				               ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(record i, int param){
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				X(record i, int param){
				  ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int sum(record i, int num);
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int sum(record i, int num);
				        ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_035() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface Greetings {
					  void greet(String head, String tail);
					}
					public class X {
					  public static void main(String[] args) {
					    Greetings g = (record, y) -> {
					      System.out.println(record + y);
					    };
					    g.greet("Hello, ", "World!");
					  }
					}
					""",
			},
			"Hello, World!"
			);
}
public void testBug564672_036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y {
					int sum(record this, int i, int num) {}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int sum(record this, int i, int num) {}
				        ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static record i;
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				static record i;
				       ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						for (record i = 0; i<10; i++) {
							System.out.println(0);
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				for (record i = 0; i<10; i++) {
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int rec[] = {1,2,3,4,5,6,7,8,9};
						for (record i: rec) {
							System.out.println(0);
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				for (record i: rec) {
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				for (record i: rec) {
				               ^^^
			Type mismatch: cannot convert from element type int to record
			----------
			""");
}
public void testBug564672_040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try (record i = 0){
						}
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				try (record i = 0){
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try{
						}
						catch (record e) {}
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				catch (record e) {}
				       ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				record Point(record x, int i) { }
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				record Point(record x, int i) { }
				^
			record cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				record Point(record x, int i) { }
				             ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Point {
					<T> Point(T i) {
					}
					Point (int i, int j) {
						<record> this(null);
					}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				<record> this(null);
				 ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 5)
				<record> this(null);
				         ^^^^^^^^^^^
			The constructor Point(record) refers to the missing type record
			----------
			""");
}
public void testBug564672_044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Point {
					<T> Point(T i) {
					}
				}
				class PointEx extends Point {
					PointEx (int i, int j) {
						<record> super(null);
					}
				;\
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				<record> super(null);
				 ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 7)
				<record> super(null);
				         ^^^^^^^^^^^^
			The constructor Point(record) refers to the missing type record
			----------
			""");
}
public void testBug564672_045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y {
					void m1() {}\s
					void m2() {
						this.<record>m1();\
					}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				this.<record>m1();	}
				      ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. WARNING in X.java (at line 4)
				this.<record>m1();	}
				      ^^^^^^
			Unused type arguments for the non generic method m1() of type Y; it should not be parameterized with arguments <record>
			----------
			""");
}
public void testBug564672_046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y{
					void a() {
						System.out.println("1");
					}
				}
				public class X {
					public static void main(String[] args) {
						new <record>Y().a();
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				new <record>Y().a();
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. WARNING in X.java (at line 8)
				new <record>Y().a();
				     ^^^^^^
			Unused type arguments for the non generic constructor Y() of type Y; it should not be parameterized with arguments <record>
			----------
			""");
}
public void testBug564672_047() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Y{}
				
				public class X {
					public static void main(String[] args) {
						new <record>Y() {
							void a() {
								System.out.println("1");
							}
						}.a();
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				new <record>Y() {
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. WARNING in X.java (at line 5)
				new <record>Y() {
				     ^^^^^^
			Unused type arguments for the non generic constructor Object() of type Object; it should not be parameterized with arguments <record>
			----------
			""");
}
public void testBug564672_048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y{}
				
				public class X {
					public static void main(String[] args) {
						new <record>Y() {
							void a() {
								System.out.println("1");
							}
						}.a();
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				new <record>Y() {
				     ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. WARNING in X.java (at line 5)
				new <record>Y() {
				     ^^^^^^
			Unused type arguments for the non generic constructor Y() of type Y; it should not be parameterized with arguments <record>
			----------
			""");
}
public void testBug564672_049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						record[] y= new record[3];\s
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				record[] y= new record[3];\s
				^^^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 3)
				record[] y= new record[3];\s
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_050() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s="Hello";
						record y= (record)s;\s
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				record y= (record)s;\s
				^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 4)
				record y= (record)s;\s
				           ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_051() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s="Hello";
						if (s instanceof record) {\s
							System.out.println(1);
						}
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (s instanceof record) {\s
				                 ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
public void testBug564672_052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				
				public class X {
					public static void main(String[] args) {
						List<String> messages = Arrays.asList("hello", "java", "testers!");
						messages.forEach(record::length);
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				messages.forEach(record::length);
				                 ^^^^^^
			record cannot be resolved
			----------
			""");
}
public void testBug564672_053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				
				public class X {
					public static void main(String[] args) {
						List<String> messages = Arrays.asList("hello", "java", "testers!");
						messages.stream().map(record::new).toArray(record[]::new);
						System.out.println(0);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				messages.stream().map(record::new).toArray(record[]::new);
				                      ^^^^^^
			record cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 7)
				messages.stream().map(record::new).toArray(record[]::new);
				                                           ^^^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""");
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X extends record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				class record {}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X extends record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X implements record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				interface record {}
				;"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X implements record {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y extends record {
				  }
				  class record {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y extends record {
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_007() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y implements record {
				  }
				  interface record {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  class Y implements record {
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Y extends record {
				}
				interface record {}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_010() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Y extends record {
				}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_011() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  interface Y extends record {
				  }
				  interface record {}
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_012() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  interface Y extends record {
				  }
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Z {
				  class Y extends record {
				  }
				  class record {}
				}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  class Y extends record {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends record {
				                ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Z {
				  class Y implements record {
				  }
				  interface record {}
				}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  class Y implements record {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y implements record {
				                   ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Z {
				  interface Y extends record {
				  }
				  interface record {}
				}
				class X {
				  public static void main(String[] args){
				     System.out.println(0);
				  }
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				  interface Y extends record {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface Y extends record {
				                    ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static record a(int i, int j) {
						record r=new record(i,j);
						return r;
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				record r=new record(i,j);
				^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 3)
				record r=new record(i,j);
				             ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			3. ERROR in X.java (at line 4)
				return r;
				^^^^^^
			Syntax error on token "return", byte expected
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_020() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					class record {}
				
					static record a(int i, int j) {
						record r = new X().new record();
						return r;
					}
				
					public static void main(String[] args) {
						System.out.println(0);
					}
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_021() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X() throws record {}\s
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				
				class record extends Exception {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Y {
					int a() throws record;
				}
				
				class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				
				class record extends Exception {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_023() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class X {
					List<record> R = new ArrayList<record>();
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record{}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_024() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<S> {
					void print(S arg);
				}
				
				public class X implements I<record> {
					public void print(record arg) {
						System.out.println(arg);
					}
				
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_025() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y<record> {
					void equal(record R) {}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_026() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y<record> {
					Y(record R) {}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_027() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static record i;
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_028() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					record i = new record(0);
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {
					int i;
					record (int i) {
						this.i=i;
					}
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_029() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static int sum(record i, int param) {
						return 1;
					}
				
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record{}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_030() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(record i, int param){
					}
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record{}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_031() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					int sum(record i, int num);
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record{}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_032() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface Greetings {
					  void greet(String head, String tail);
					}
					public class X {
					  public static void main(String[] args) {
					    Greetings g = (record, y) -> {
					      System.out.println(record + y);
					    };
					    g.greet("Hello, ", "World!");
					  }
					}
					""",
			},
			"Hello, World!",
			options
		);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_033() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				
				class record {
					int sum(record this, int i, int num) {
						return 0;
					}
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_034() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static Rec record;
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class Rec {}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_035() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Iterator;
				import java.util.List;
				
				public class X {
					public static void main(String[] args) {
						int rec[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						String s="";
						List <record> recList= new ArrayList<>();
						for (int i:rec) {
							recList.add(new record(i));
						}
						for (Iterator<record> i =recList.iterator(); i.hasNext();) {
							s=s+i.next()+" ";
						}
						System.out.println(0);
					}
				}
				
				class record {
					int i;
					record (int i) {
						this.i=i;
					}
					public String toString (){
						return Integer.toString(i);
					}
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_036() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class X {
					public static void main(String[] args) {
						int rec[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						String s="";
						List <record> recList= new ArrayList<>();
						for (int i:rec) {
							recList.add(new record(i));
						}
						for (record i : recList) {
							s=s+i+" ";
						}
						System.out.println(0);
					}
				}
				
				class record {
					int i;
					record (int i) {
						this.i=i;
					}
					public String toString (){
						return Integer.toString(i);
					}
				}
				"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_037() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try (record i = new record (0)){
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println(0);
					}
				}
				class record implements AutoCloseable{
					int i;
					record (int i) {
						this.i=i;
					}
					@Override
					public void close() throws Exception {}
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_038() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						try {
							throw new record();
						} catch (record e) {
							System.out.println("0");
						}
					}
				}
				
				class record extends Exception {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_039() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				record Point(record x, int i) { }
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				record Point(record x, int i) { }
				             ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			2. ERROR in X.java (at line 7)
				class record {}
				      ^^^^^^
			'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
			----------
			""",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_040() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Point {
					<T> Point(T i) {
					}
					Point (int i, int j) {
						<record> this(null);
					}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_041() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Point {
					<T> Point(T i) {
					}
				}
				class PointEx extends Point {
					PointEx (int i, int j) {
						<record> super(null);
					}
				;\
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_042() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y {
					<T> void m1() {}\s
					void m2() {
						this.<record>m1();\
					}
				}
				public class X {
					public static void main(String[] args) {
						System.out.println(0);
					}
				}
				class record {}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_043() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y{
					<T> Y() {}
					void a() {
						System.out.println("1");
					}
				}
				public class X {
					public static void main(String[] args) {
						new <record>Y().a();
					}
				}
				class record {}"""
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_044() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Y{
				}
				
				class Z implements Y {
					<T> Z() {
					\t
					}
				}
				
				public class X {
					public static void main(String[] args) {
						new <record>Z() {
							void a() {
								System.out.println("1");
							}
						}.a();
					}
				}
				class record {}"""
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_045() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y{\
					<T> Y() {
					}\
				}
				
				public class X {
					public static void main(String[] args) {
						new <record>Y() {
							void a() {
								System.out.println("1");
							}
						}.a();
					}
				}
				class record {}"""
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_046() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						record[] y= new record[3];\s
						System.out.println(0);
					}
				}\
				class record {}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_047() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						StrRec s = new StrRec("Hello");
						record y = (record) s;
						System.out.println(0);
					}
				}
				
				class record {
				}
				
				class StrRec extends record {
					String s;
				
					StrRec(String s) {
						this.s = s;
					}
				}"""
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_048() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						StrRec s=new StrRec("Hello");
						if (s instanceof record) {\s
							System.out.println(1);
						}
					}
				}
				class record {}
				
				class StrRec extends record {
					String s;
				
					StrRec(String s) {
						this.s = s;
					}
				}"""
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_049() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<String> messages = Arrays.asList("hello", "java", "testers!");
					\t
						messages.stream().map(record::new).toArray(record[]::new);;
						System.out.println(0);
					}
				}
				class record {
					String s;
				
					record(String s) {
						this.s = s;
					}
				}"""
		},
		"0",
		options
	);
}
public void testBug565388_001() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public non-sealed record X() {}\n"
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public non-sealed record X() {}
				                         ^
			Illegal modifier for the record X; only public, final and strictfp are permitted
			----------
			""",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug565388_002() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public sealed record X() {}\n"
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public sealed record X() {}
				                     ^
			Illegal modifier for the record X; only public, final and strictfp are permitted
			----------
			""",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug565786_001() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        System.out.println(0);
				   }
				}
				interface I {
				    record R() {}
				}""",
		},
		"0");
	String expectedOutput =
			"""
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public I$R();
		""";
	verifyClassFile(expectedOutput, "I$R.class", ClassFileBytesDisassembler.SYSTEM);
}
// Test that without an explicit canonical constructor, we
// report the warning on the record type.
public void testBug563182_01() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				record Point<T> (T ... args) { // 1
				                       ^^^^
			Type safety: Potential heap pollution via varargs parameter args
			----------
			""",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of an explicit canonical constructor that is NOT annotated with @SafeVarargs,
// we don't report the warning on the record type but report on the explicit canonical constructor
public void testBug563182_02() {
	getPossibleComplianceLevels();
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
						Point(T ... args) { // 2
							this.args = args;
						}
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Point(T ... args) { // 2
				            ^^^^
			Type safety: Potential heap pollution via varargs parameter args
			----------
			""",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of an explicit canonical constructor that IS annotated with @SafeVarargs,
//we don't report the warning on neither the record type nor the explicit canonical constructor
public void testBug563182_03() {
	getPossibleComplianceLevels();
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
						@SafeVarargs
						Point(T ... args) { // 2
							this.args = args;
						}
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a compact canonical constructor that is NOT annotated with @SafeVarargs,
//we don't report the warning on the compact canonical constructor but report on the record type
public void testBug563182_04() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
						Point { // 2
						}
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				record Point<T> (T ... args) { // 1
				                       ^^^^
			Type safety: Potential heap pollution via varargs parameter args
			----------
			""",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a compact canonical constructor that IS annotated with @SafeVarargs,
//we don't report the warning on neither the record type nor the compact canonical constructor
public void testBug563182_05() {
	getPossibleComplianceLevels();
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
						@SafeVarargs
						Point { // 2
						}
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a non-canonical constructor that is annotated with @SafeVarargs,
//we don't report the warning on the non-canonical constructor but report on the record type
public void testBug563182_06() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
						@SafeVarargs
						Point (String s, T ... t) {
							this(t);
						}
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				record Point<T> (T ... args) { // 1
				                       ^^^^
			Type safety: Potential heap pollution via varargs parameter args
			----------
			""",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a non-canonical constructor that is NOT annotated with @SafeVarargs,
//we don't report the warning on the non-canonical constructor but report on the record type
public void testBug563182_07() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					record Point<T> (T ... args) { // 1
						Point (String s, T ... t) {
							this(t);
						}
					}
				   public static void main(String[] args) {}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				record Point<T> (T ... args) { // 1
				                       ^^^^
			Type safety: Potential heap pollution via varargs parameter args
			----------
			2. WARNING in X.java (at line 3)
				Point (String s, T ... t) {
				                       ^
			Type safety: Potential heap pollution via varargs parameter t
			----------
			""",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
	public void testBug563186_01() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  private record Point(int myInt){
						  	 @Override
						  	 public int myInt(){
						      return this.myInt;
						    }
						  }
						    public static void main(String[] args) {
						        System.out.println(0);
						   }
						}
						"""
				},
			 "0");
	}
	public void testBug563186_02() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  private record Point(int myInt){
						  	 public int myInt(){
						      return this.myInt;
						    }
						  }
						    public static void main(String[] args) {
						        System.out.println(0);
						   }
						}
						"""
				},
			 "0");
	}
	public void testBug563186_03() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  private record Point(int myInt){
						  	 @Override
						  	 public int myInt(int i){
						      return this.myInt;
						    }
						  }
						    public static void main(String[] args) {
						        System.out.println(0);
						   }
						}
						"""
				},
				"""
					----------
					1. WARNING in X.java (at line 2)
						private record Point(int myInt){
						               ^^^^^
					The type X.Point is never used locally
					----------
					2. ERROR in X.java (at line 4)
						public int myInt(int i){
						           ^^^^^^^^^^^^
					The method myInt(int) of type X.Point must override or implement a supertype method
					----------
					""",
				null,
				true,
				new String[] {"--enable-preview"},
				getCompilerOptions());
	}
	public void testBug563186_04() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  private record Point(int myInt){
						  	 public int myInt(int i){
						      return this.myInt;
						    }
						  }
						    public static void main(String[] args) {
						        System.out.println(0);
						   }
						}
						"""
				},
			 "0");
	}
	public void testBug565732_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public record X {\n" +
					"} "
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public record X {
						              ^
					Syntax error, insert "RecordHeader" to complete RecordHeaderPart
					----------
					""",
				null,
				true,
				new String[] {"--enable-preview"},
				getCompilerOptions());
	}
	public void testBug565732_02() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public record X<T> {\n" +
					"} "
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public record X<T> {
						                 ^
					Syntax error, insert "RecordHeader" to complete RecordHeaderPart
					----------
					""",
				null,
				true,
				new String[] {"--enable-preview"},
				getCompilerOptions());
	}
	// Test that a record without any record components was indeed compiled
	// to be a record at runtime
	public void testBug565732_03() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public record X() {
							public static void main(String[] args) {
								System.out.println(X.class.getSuperclass().getName());
							}
						}"""
				},
			 "java.lang.Record");
	}
	// Test that a record without any record components was indeed compiled
	// to be a record at runtime
	public void testBug565732_04() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public record X<T>() {
							public static void main(String[] args) {
								System.out.println(X.class.getSuperclass().getName());
							}
						}"""
				},
			 "java.lang.Record");
	}
	// Test that a "record" can be used as a method name and invoked inside a record
	public void testBug565732_05() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public record X<T>() {
							public static void main(String[] args) {
								record();
							}
							public static void record() {
								System.out.println("record()");
							}
						}"""
				},
			 "record()");
	}
	// Test that a "record" can be used as a label and invoked inside a record
	public void testBug565732_06() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public record X<T>() {
							public static void main(String[] args) {
								boolean flag = true;
								record: {
									if (flag) {
										System.out.println("record:");
										flag = false;
										break record;
									}
								}
							}
						}"""
				},
			 "record:");
	}
	public void testBug565732_07() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							record R {};
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						record R {};
						       ^
					Syntax error, insert "RecordHeader" to complete RecordHeaderPart
					----------
					""",
				null,
				true,
				new String[] {"--enable-preview"},
				getCompilerOptions());
	}
	public void testBug565732_08() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								System.out.println(R.class.getSuperclass().getName());
							}
							record R() {};
						}"""
				},
			 "java.lang.Record");
	}
	public void testBug565830_01() {
		runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				    void bar() throws Exception {
				        record Bar(int x) implements java.io.Serializable {
				            void printMyFields() {
				                for (var field : this.getClass().getDeclaredFields()) {
				                    System.out.println(field);
				                }
				            }
				        }
				        var bar = new Bar(1);
				        bar.printMyFields();
				        new java.io.ObjectOutputStream(java.io.OutputStream.nullOutputStream()).writeObject(bar);
				    }
				    public static void main(String[] args) throws Exception {
				        new X().bar();
				    }
				}""",
		},
		"private final int X$1Bar.x");
	}
public void testBug566063_001() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					    void bar() throws Exception {
					        enum E {
					               ONE,
					               TWO
					        }
					        interface I {}
					        record Bar(E x) implements I{}
					        E e = new Bar(E.ONE).x();
					        System.out.println(e);
					    }
					    public static void main(String[] args) throws Exception {
					       new X().bar();
					    }
					}"""
			},
			"ONE",
			options
		);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug566063_002() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    void bar() throws Exception {
					        static enum E {
					               ONE,
					               TWO
					        }
					        interface I {}
					        record Bar(E x) implements I{}
					        E e = new Bar(E.ONE).x();
					        System.out.println(e);
					    }
					    public static void main(String[] args) throws Exception {
					       new X().bar();
					    }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					static enum E {
					            ^
				Illegal modifier for local enum E; no explicit modifier is permitted
				----------
				""",
			null,
			true,
			options
		);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug566063_003() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    void bar() throws Exception {
					        static enum E {
					               ONE,
					               TWO
					        }
					        static interface I {}
					        static record Bar(E x) implements I{}
					        E e = new Bar(E.ONE).x();
					        System.out.println(e);
					    }
					    public static void main(String[] args) throws Exception {
					       new X().bar();
					    }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					static enum E {
					            ^
				Illegal modifier for local enum E; no explicit modifier is permitted
				----------
				2. ERROR in X.java (at line 7)
					static interface I {}
					                 ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				3. ERROR in X.java (at line 8)
					static record Bar(E x) implements I{}
					              ^^^
				A local class or interface Bar is implicitly static; cannot have explicit static declaration
				----------
				""",
			null,
			true,
			options
		);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug566063_004() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						    void bar() throws Exception {
						        enum E {
						               ONE,
						               TWO
						        }
								 interface I {}
						        record Bar(E x) implements I{}
						        E e = new Bar(E.ONE).x();
						        System.out.println(e);
						    }
						    public static void main(String[] args) throws Exception {
						       new X().bar();
						    }
						}"""
				},
				"ONE");
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug566418_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"""
				public class X {
				 static void foo() {
				   record R() {
				     static int create(int lo) {
				       return lo;
				     }
				   }
				   System.out.println(R.create(0));
				   }
				   Zork();
				}""",
		},
	"""
		----------
		1. ERROR in X.java (at line 10)
			Zork();
			^^^^^^
		Return type for the method is missing
		----------
		2. ERROR in X.java (at line 10)
			Zork();
			^^^^^^
		This method requires a body instead of a semicolon
		----------
		""",
		null,
		true,
		options
	);
}
public void testBug565787_01() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public record X(String s)   {
				    public X  {
				        s.codePoints()
				        .forEach(cp -> System.out.println((java.util.function.Predicate<String>) ""::equals));
				    }
				    public static void main(String[] args) {
				        X a = new X("");
				        a.equals(a);
				    }
				}""",
		},
		"");
}
public void testBug566554_01() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				@SuppressWarnings("preview")
				public class Main {
					public static void main(String[] args) {
						final Margin margins = new Margin(0);
						System.out.println(margins.left());\s
					}
				}
				record Margin(int left) {
					public Margin left(int value) {
						return new Margin(value);
					}
					public String toString() {
						return "Margin[left=" + this.left + "]";
					}
				}""",
		},
		"0");
}
public void testBug566554_02() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				@SuppressWarnings("preview")
				public class Main {
					public static void main(String[] args) {
						final Margin margins = new Margin(0);
						System.out.println(margins.left());\s
					}
				}
				record Margin(int left) {
					public Margin left(int value) {
						return new Margin(value);
					}
					public int left() {
						return this.left;
					}
					public String toString() {
						return "Margin[left=" + this.left + "]";
					}
				}""",
		},
		"0");
}
public void testBug566554_03() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				@SuppressWarnings("preview")
				public class Main {
					public static void main(String[] args) {
						final Margin margins = new Margin(0);
						System.out.println(margins.left(0));\s
					}
				}
				record Margin(int left) {
					public Margin left(int value) {
						return new Margin(value);
					}
					public int left() {
						return this.left;
					}
					public String toString() {
						return "Margin[left=" + this.left + "]";
					}
				}""",
		},
		"Margin[left=0]");
}
public void testBug566554_04() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"""
				@SuppressWarnings("preview")
				public class Main {
					public static void main(String[] args) {
						final Margin margins = new Margin(0);
						int l = margins.left(0);\s
					}
				}
				record Margin(int left) {
					public Margin left(int value) {
						return new Margin(value);
					}
					public int left() {
						return this.left;
					}
				}""",
		},
		"""
			----------
			1. ERROR in Main.java (at line 5)
				int l = margins.left(0);\s
				        ^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Margin to int
			----------
			""");
}
public void testBug567731_001() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  non-sealed record R() {}
				  public static void main(String[] args) {
					  sealed record B() { } \s
				  }\
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				non-sealed record R() {}
				                  ^
			Illegal modifier for the record R; only public, private, protected, static, final and strictfp are permitted
			----------
			2. ERROR in X.java (at line 4)
				sealed record B() { } \s
				              ^
			Illegal modifier for the local record B; only final and strictfp are permitted
			----------
			""",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug567731_002() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	Map<String, String> options = getCompilerOptionsWithPreviewIfApplicable();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  sealed record R1() {}
				  public static void main(String[] args) {
					  non-sealed record R2() { } \s
				  }\
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				sealed record R1() {}
				              ^^
			Illegal modifier for the record R1; only public, private, protected, static, final and strictfp are permitted
			----------
			2. ERROR in X.java (at line 4)
				non-sealed record R2() { } \s
				                  ^^
			Illegal modifier for the local record R2; only final and strictfp are permitted
			----------
			""",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
}
public void testBug566846_1() {
	runNegativeTest(
			new String[] {
				"X.java",
				"public record X;\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public record X;
					^
				The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above
				----------
				2. ERROR in X.java (at line 1)
					public record X;
					^
				Implicitly declared class must have a candidate main method
				----------
				3. ERROR in X.java (at line 1)
					public record X;
					       ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""",
			null,
			true,
			new String[] {"--enable-preview"},
			getCompilerOptions());
}
public void testBug566846_2() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					}\s
					record R1;
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X {
					^
				The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above
				----------
				2. ERROR in X.java (at line 1)
					public class X {
					^
				Implicitly declared class must have a candidate main method
				----------
				3. ERROR in X.java (at line 3)
					record R1;
					^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""",
			null,
			true,
			new String[] {"--enable-preview"},
			getCompilerOptions());
}
public void testBug561199_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.ERROR);
	runNegativeTest(
			new String[] {
				"R.java",
				"record R() implements java.io.Serializable {}\n",
				"X.java",
				"class X implements java.io.Serializable {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X implements java.io.Serializable {}
					      ^
				The serializable class X does not declare a static final serialVersionUID field of type long
				----------
				""",
			null,
			true,
			new String[] {"--enable-preview"},
			options);
}
public void testBug568922_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   @SuppressWarnings("preview")
					   record R() {
					     R  {
					       super();
					       System.out.println("helo");
					     }
					   }
					   new R();
					 }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					super();
					^^^^^^^^
				The body of a compact constructor must not contain an explicit constructor call
				----------
				""",
			null,
			true,
			new String[] {"--enable-preview"},
			getCompilerOptions());
}
public void testBug568922_002() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				   @SuppressWarnings("preview")
				   record R() {
				     R  {
				       System.out.println("helo");
				     }
				   }
				   new R();
				 }
				}"""
		},
		"helo");
}
public void testBug570243_001() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.reflect.Parameter;
				 \s
				public record X(int myCompOne) {
				       public static void main(String[] x1) {
				        try {
				            Parameter param = Class.forName("X").getConstructors()[0].getParameters()[0];
				               System.out.println(param.getType().getSimpleName()+" "+ param.getName());
				        } catch(ClassNotFoundException e) {
				               // do nothing
				        }
				       }
				}"""
		},
		"int myCompOne");
}
public void testBug570243_002() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.reflect.Parameter;
				 \s
				public record X(int myCompOne, char myCompChar) {
				       public static void main(String[] x1) {
				        try {
				            Parameter[] params = Class.forName("X").getConstructors()[0].getParameters();
				            for (Parameter param : params)
				               System.out.println(param.getType().getSimpleName()+" "+ param.getName());
				        } catch(ClassNotFoundException e) {
				               // do nothing
				        }
				       }
				}"""
		},
		"int myCompOne\n"+
		"char myCompChar");
}
public void testBug570243_003() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.reflect.Parameter;
				 \s
				public record X(int myCompOne, char ...myCompChar) {
				       public static void main(String[] x1) {
				        try {
				            Parameter[] params = Class.forName("X").getConstructors()[0].getParameters();
				            for (Parameter param : params)
				               System.out.println(param.getType().getSimpleName()+" "+ param.getName());
				        } catch(ClassNotFoundException e) {
				               // do nothing
				        }
				       }
				}"""
		},
		"int myCompOne\n"+
		"char[] myCompChar");
}
public void testBug570230_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"public record X(int marr[]) {}"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public record X(int marr[]) {}
					                    ^^^^
				Extended dimensions are illegal for a record component
				----------
				""");
}
public void testBug571015_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					record R() {
					       R(I<T> ... t) {}
					}
					interface I{}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					R(I<T> ... t) {}
					  ^
				The type I is not generic; it cannot be parameterized with arguments <T>
				----------
				2. ERROR in X.java (at line 2)
					R(I<T> ... t) {}
					    ^
				T cannot be resolved to a type
				----------
				""");
}
public void testBug571015_002() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					record R() {
					       R(I<X> ... t) {}
					}
					interface I<T>{}
					class X{}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					R(I<X> ... t) {}
					^^^^^^^^^^^^^
				A non-canonical constructor must start with an explicit invocation to a constructor
				----------
				2. WARNING in X.java (at line 2)
					R(I<X> ... t) {}
					           ^
				Type safety: Potential heap pollution via varargs parameter t
				----------
				""");
}
public void testBug571038_1() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				   System.out.println("hello");
				 }
				}
				record MyRecord<T> (MyIntf<T>... t) {
					public MyRecord(MyIntf<T>... t) {
						this.t = null;
					}
				}
				interface MyIntf<T> {}
				"""
		},
	 "hello");
	String expectedOutput = """
		  // Method descriptor #25 ()[LMyIntf;
		  // Signature: ()[LMyIntf<TT;>;
		  // Stack: 1, Locals: 1
		  public MyIntf[] t();
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571038_2() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				   System.out.println("hello");
				 }
				}
				record MyRecord<T> (MyIntf<T>... t) {
					@SafeVarargs
					public MyRecord(MyIntf<T>... t) {
						this.t = null;
					}
				}
				interface MyIntf<T> {}
				"""
		},
	 "hello");
	String expectedOutput = """
		  // Method descriptor #27 ()[LMyIntf;
		  // Signature: ()[LMyIntf<TT;>;
		  // Stack: 1, Locals: 1
		  public MyIntf[] t();
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571038_3() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.*;
				public class X {
				 public static void main(String[] args) {
				   System.out.println("hello");
				 }
				}
				record MyRecord<T> (MyIntf<T>... t) {
					@SafeVarargs
					public MyRecord(@MyAnnot MyIntf<T>... t) {
						this.t = null;
					}
				}
				interface MyIntf<T> {}
				@Retention(RetentionPolicy.RUNTIME)
				@interface MyAnnot {}
				"""
		},
	 "hello");
	String expectedOutput = """
		  // Method descriptor #29 ()[LMyIntf;
		  // Signature: ()[LMyIntf<TT;>;
		  // Stack: 1, Locals: 1
		  public MyIntf[] t();
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571038_4() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.*;
				public class X {
				 public static void main(String[] args) {
				   System.out.println("hello");
				 }
				}
				record MyRecord<T> (MyIntf<T>... t) {
					@SafeVarargs
					public MyRecord(MyIntf<@MyAnnot T>... t) {
						this.t = null;
					}
				}
				interface MyIntf<T> {}
				@Retention(RetentionPolicy.RUNTIME)
				@java.lang.annotation.Target(ElementType.TYPE_USE)
				@interface MyAnnot {}
				"""
		},
	 "hello");
	String expectedOutput = """
		  // Method descriptor #29 ()[LMyIntf;
		  // Signature: ()[LMyIntf<TT;>;
		  // Stack: 1, Locals: 1
		  public MyIntf[] t();
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571454() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String argv[]) {
						       R rec = new R(3);
								if (rec.x() == 3) {
									// do nothing
								}
						    }
						}
						""",
					"R.java",
					"""
						record R(int x) {
						       R {
						               super();
						       }
						}""",
				},
	        """
				----------
				1. ERROR in R.java (at line 3)
					super();
					^^^^^^^^
				The body of a compact constructor must not contain an explicit constructor call
				----------
				""");
}
public void testBug570399_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				    R r1 = new R( 2, 3); // Wrong error: The constructor MyRecord(int, int) is undefined
				    R r2 = new R();      // works
				    int total = r1.x()+r2.x()+r1.y()+r2.y();
				    System.out.println("Hi"+total);
				  }
				}""",
			"R.java",
			"""
				public record R(int x, int y) {
				    R() {
				        this(0, 0);
				    }
				}""",
		},
	 "Hi5");
}
public void testBug570399_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				record R(int x) {
				}
				public class X {
				 public static void main(String[] args) {
				    R r2 = new R(5);      // works
				    int total = r2.x();
				    System.out.println("Hi"+total);
				  }
				}""",
		},
	 "Hi5");
}
public void testBug571141_1() {
	runConformTest(new String[] { "X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				   System.out.println("helo");
				 }
				}
				record MyRecord(boolean equals){
				    public boolean equals() {
				        return equals;
				    }
				}""" },
		"helo");
}
public void testBug571141_2() {
	runConformTest(new String[] { "X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				   System.out.println("helo");
				 }
				}
				record MyRecord(boolean equals){
				    public boolean equals() {
				        return equals;
				    }
				    public boolean equals(Object obj) {
				      return equals;
				    }\s
				}""" },
		"helo");
}
public void testBug571141_3() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"""
				public class X {
				 public static void main(String[] args) {
				   System.out.println("helo");
				 }
				}
				record MyRecord(boolean b){
				    public boolean equals(Object other) {
				        return true;
				    }
				}""" },
		"helo");
	String unExpectedOutput =
			 "  public final boolean equals(java.lang.Object arg0);\n"
			 + "    0  aload_0 [this]\n"
			 + "    1  aload_1 [arg0]\n"
			 + "    2  invokedynamic 0 equals(MyRecord, java.lang.Object) : boolean [35]\n"
			 + "    7  ireturn\n"
			 + "";
	String rFile = getClassFileContents("MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
	verifyOutputNegative(rFile, unExpectedOutput);
}
public void testBugLazyCanon_001() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"""
				record X(int xyz, int y2k) {
				 public X(int xyz, int y2k) {
				     this.xyz = xyz;
				     this.y2k = y2k;
				   }
				 public static void main(String[] args) {
				   System.out.println(new X(33,1).xyz());
				 }
				}"""
	},
		"33");
}
public void testBugLazyCanon_002() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"""
				record X(int xyz, int y2k) {
				 public static void main(String[] args) {
				   System.out.println(new X(33,1).xyz());
				 }
				}"""
	},
		"33");
}
public void testBugLazyCanon_003() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"""
				class X {
				  record Point (int  args) {
				    Point (String s, int t) {
				      this(t);
				    }
				  }
				   public static void main(String[] args) {
				    System.out.println(new X.Point(null, 33).args());
				   \s
				   }
				}"""
	},
	"33");
}
public void testBugLazyCanon_004() throws IOException, ClassFormatException {
	runConformTest(new String[] {
			"X.java",
			"""
				record X<T> (T args) {
				 public static void main(String[] args) {
				   System.out.println(new X<Integer>(100).args());
				  \s
				 }
				}"""
	},
	"100");
}
public void testBugLazyCanon_005() throws IOException, ClassFormatException {
	runConformTest(new String[] {
			"X.java",
			"""
				record X<T> (T args) {
				 X(String s, T t) {
				   this(t);
				 }
				 public static void main(String[] args) {
				   System.out.println(100);
				  \s
				 }
				}"""
	},
	"100");
}
public void testBugLazyCanon_006() throws IOException, ClassFormatException {
	runConformTest(new String[] {
			"X.java",
			"""
				record X<T> (T args) {
				 X(String s, T t) {
				   this(t);
				 }
				 public static void main(String[] args) {
				   System.out.println(new X<Integer>(100).args());
				  \s
				 }
				}"""
	},
	"100");
}
public void testBug571765_001() {
	this.runNegativeTest(
			new String[] {
					"module-info.java",
					"public record R() {}\n",
				},
			"""
				----------
				1. ERROR in module-info.java (at line 1)
					public record R() {}
					^
				The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above
				----------
				2. ERROR in module-info.java (at line 1)
					public record R() {}
					^
				Implicitly declared class must have a candidate main method
				----------
				3. ERROR in module-info.java (at line 1)
					public record R() {}
					       ^^^^^^
				'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16
				----------
				""");
}
public void testBug571905_01() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.*;
				record X( int @MyAnnot [] j) {
				 public static void main(String[] args) {
				   System.out.println("helo");
				 }
				}
				@Target({ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface MyAnnot {}
				"""
		},
	 "helo");
	String expectedOutput = // constructor
			"""
		 \s
		  // Method descriptor #10 ([I)V
		  // Stack: 2, Locals: 2
		  X(int[] j);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [12]
		     4  aload_0 [this]
		     5  aload_1 [j]
		     6  putfield X.j : int[] [15]
		     9  return
		      Line numbers:
		        [pc: 0, line: 2]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		        [pc: 0, pc: 10] local: j index: 1 type: int[]
		      Method Parameters:
		        j
		    RuntimeVisibleTypeAnnotations:\s
		      #8 @MyAnnot(
		        target type = 0x16 METHOD_FORMAL_PARAMETER
		        method parameter index = 0
		      )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = // accessor
			"""
				  public int[] j();
				    0  aload_0 [this]
				    1  getfield X.j : int[] [15]
				    4  areturn
				      Line numbers:
				        [pc: 0, line: 2]
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @MyAnnot(
				        target type = 0x14 METHOD_RETURN
				      )
				""" ;
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571905_02() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.*;
				record X( int @MyAnnot ... j) {
				 public static void main(String[] args) {
				   System.out.println("helo");
				 }
				}
				@Target({ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface MyAnnot {}
				"""
		},
	 "helo");
	String expectedOutput = // constructor
			"""
		 \s
		  // Method descriptor #10 ([I)V
		  // Stack: 2, Locals: 2
		  X(int... j);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [12]
		     4  aload_0 [this]
		     5  aload_1 [j]
		     6  putfield X.j : int[] [15]
		     9  return
		      Line numbers:
		        [pc: 0, line: 2]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		        [pc: 0, pc: 10] local: j index: 1 type: int[]
		      Method Parameters:
		        j
		    RuntimeVisibleTypeAnnotations:\s
		      #8 @MyAnnot(
		        target type = 0x16 METHOD_FORMAL_PARAMETER
		        method parameter index = 0
		      )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = // accessor
			"""
				  public int[] j();
				    0  aload_0 [this]
				    1  getfield X.j : int[] [15]
				    4  areturn
				      Line numbers:
				        [pc: 0, line: 2]
				    RuntimeVisibleTypeAnnotations:\s
				      #8 @MyAnnot(
				        target type = 0x14 METHOD_RETURN
				      )
				""" ;
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug572204_001() {
	runNegativeTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {}\n"
				},
				"""
					----------
					1. ERROR in R.java (at line 1)
						record R (@SafeVarargs String... s) {}
						                                 ^
					@SafeVarargs annotation cannot be applied to record component without explicit accessor method s
					----------
					""");
}
public void testBug572204_002() {
	runConformTest(
			new String[] {
					"R.java",
					"""
						record R (@SafeVarargs String... s) {
						 public static void main(String[] args) {
						   System.out.println("helo");
						 }
						 public String[] s() {
						  return this.s;
						 }
						}
						"""
				},
				"helo");
}
public void testBug572204_003() {
	runNegativeTest(
			new String[] {
					"R.java",
					"""
						record R (@SafeVarargs String... s) {
						 public static void main(String[] args) {
						   System.out.println("helo");
						 }
						 R (@SafeVarargs String... s) {
						   this.s=s;
						 }
						}
						"""
				},
				"""
					----------
					1. ERROR in R.java (at line 1)
						record R (@SafeVarargs String... s) {
						                                 ^
					@SafeVarargs annotation cannot be applied to record component without explicit accessor method s
					----------
					2. ERROR in R.java (at line 5)
						R (@SafeVarargs String... s) {
						   ^^^^^^^^^^^^
					The annotation @SafeVarargs is disallowed for this location
					----------
					""");
}
public void testBug572204_004() {
	runNegativeTest(
			new String[] {
					"R.java",
					"""
						record R (@SafeVarargs String... s) {
						 public static void main(String[] args) {
						   System.out.println("helo");
						 }
						 R (@SafeVarargs String... s) {
						   this.s=s;
						 }
						 public String[] s() {
						  return this.s;
						 }
						}
						"""
				},
			"""
				----------
				1. ERROR in R.java (at line 5)
					R (@SafeVarargs String... s) {
					   ^^^^^^^^^^^^
				The annotation @SafeVarargs is disallowed for this location
				----------
				""");
}
public void testBug572204_005() {
	runNegativeTest(
			new String[] {
					"R.java",
					"""
						record R (@SafeVarargs String... s) {
						@SafeVarargs\
						 R (String... s) {
						   this.s = s;
						 }
						}
						"""
				},
				"""
					----------
					1. ERROR in R.java (at line 1)
						record R (@SafeVarargs String... s) {
						                                 ^
					@SafeVarargs annotation cannot be applied to record component without explicit accessor method s
					----------
					""");
}
public void testBug572204_006() {
	runConformTest(
			new String[] {
					"R.java",
					"""
						record R (@SafeVarargs String... s) {
						 public static void main(String[] args) {
						   System.out.println("helo");
						 }
						@SafeVarargs\
						 R (String... s) {
						   this.s = s;
						 }
						 public String[] s() {
						  return this.s;
						 }
						}
						"""
				},
			"helo");
}
public void testBug572204_007() throws Exception {
	runConformTest(
			new String[] {
					"R.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.PARAMETER)\s
						@Retention(RetentionPolicy.RUNTIME)
						@interface I {}\r
						record R(@I String... s) {
						 public static void main(String[] args) {
						   System.out.println("helo");
						 }
						}
						"""
				},
				"helo");
	String expectedOutput = // constructor
			"""
		 \s
		  // Method descriptor #8 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 2
		  R(java.lang.String... s);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [12]
		     4  aload_0 [this]
		     5  aload_1 [s]
		     6  putfield R.s : java.lang.String[] [15]
		     9  return
		      Line numbers:
		        [pc: 0, line: 5]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: R
		        [pc: 0, pc: 10] local: s index: 1 type: java.lang.String[]
		      Method Parameters:
		        s
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #10 @I(
		        )
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = // accessor
			"""
				 \s
				  // Method descriptor #38 ()[Ljava/lang/String;
				  // Stack: 1, Locals: 1
				  public java.lang.String[] s();
				    0  aload_0 [this]
				    1  getfield R.s : java.lang.String[] [15]
				    4  areturn
				      Line numbers:
				        [pc: 0, line: 5]
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug572934_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.ENABLED);
	//This test should not report any error
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public record X(int param) {
					public X(int param) {
						this.param = param;
					}
					public static void main(String[] args) {
						X abc= new X(10);
						System.out.println(abc.param());
					}
				}
				"""
		},
		"10",
		options
	);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
}
public void testBug572934_002() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X(int param) {
					public X(int param) {
						this.param = param;
					}
					public void main(int param) {
						System.out.println(param);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public void main(int param) {
				                     ^^^^^
			The parameter param is hiding a field from type X
			----------
			""",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
}
public void testBug572934_003() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public record X(int param) {
					public X(int param) {
						this.param = param;
					}\
					public void setParam(int param) {
					\t
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				}	public void setParam(int param) {
				 	                         ^^^^^
			The parameter param is hiding a field from type X
			----------
			""",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
}
public void testBug573195_001() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    protected record R(int i) {
						        public R(int i, int j) {
						            this(i);
						        }
						    }
						    public static void main(String[] args) {
						   R r = new R(1, 2);
						   System.out.println(r.i());
						 }
						}"""
				},
				"1");
	String expectedOutput = // constructor
			"""
		  // Method descriptor #8 (I)V
		  // Stack: 2, Locals: 2
		  protected X$R(int i);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [10]
		     4  aload_0 [this]
		     5  iload_1 [i]
		     6  putfield X$R.i : int [13]
		     9  return
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
}

public void testBug574284_001() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						    public static void main(String[] args) {
						        new X.Rec(false); // fails
						        new X.Rec(false, new int[0]);
						        System.out.println(0);
						    }
						
						    record Rec(boolean isHidden, int... indexes) {
						        Rec(int... indexes) {
						            this(false, indexes);
						        }
						    }
						}"""
			},
		"0");
	String expectedOutput = // constructor
			"""
		  // Method descriptor #10 (Z[I)V
		  // Stack: 2, Locals: 3
		  X$Rec(boolean isHidden, int... indexes);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [12]
		     4  aload_0 [this]
		     5  iload_1 [isHidden]
		     6  putfield X$Rec.isHidden : boolean [15]
		     9  aload_0 [this]
		    10  aload_2 [indexes]
		    11  putfield X$Rec.indexes : int[] [17]
		    14  return
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X$Rec.class", ClassFileBytesDisassembler.SYSTEM);

}
public void testBug574284_002() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						    public static void main(String[] args) {
						        new X.Rec(false); // fails
						        new X.Rec(false, new int[0]);
						        System.out.println(0);
						    }
						
						    record Rec(boolean isHidden, int... indexes) {
						    }
						}"""
			},
		"0");
}

public void testBug574282_001() {
	runConformTest(
			new String[] {
					"X.java",
					"""
						record Rec(String name) {
						
						    Rec() {
						        this("");
						    }
						
						    @Override
						    public boolean equals(Object obj) {
						        return false;
						    }
						}
						public class X {
						  public static void main(String[] args){
						     System.out.println(0);
						  }
						}
						"""
			},
		"0");
}
public void testBug576519_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X extends Point{
				  public X(int x, int y){
				    \s
				  }
				}
				record Point(int x, int y){
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X extends Point{
				                ^^^^^
			The record Point cannot be the superclass of X; a record is final and cannot be extended
			----------
			""");
}
public void testBug577251_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  record Entry<T> (int value, Entry<T> entry) {
				     Entry(int value, Entry entry) { // Entry is a raw type here
				  }
				}
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Entry(int value, Entry entry) { // Entry is a raw type here
				                 ^^^^^
			X.Entry is a raw type. References to generic type X.Entry<T> should be parameterized
			----------
			2. ERROR in X.java (at line 3)
				Entry(int value, Entry entry) { // Entry is a raw type here
				                 ^^^^^
			Erasure incompatibility in argument X.Entry of canonical constructor in record
			----------
			""");
}
public void testBug576806_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	this.runNegativeTest(
		false /* skipJavac */,
		new JavacTestOptions("Xlint:empty"),
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args){
					     System.out.println(0);
					  }
					}
					record Empty(){
					}
					record DocumentedEmpty(){
					  // intentionally empty
					}
					record Point(int x, int y){
					}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				record Empty(){
			}
				             ^^^^
			Empty block should be documented
			----------
			""",
		null,
		false,
		options);
}

public void testIssue365_001() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			new String[] {
					"A.java",
					"""
						import java.util.Collections;
						import java.util.List;
						public record A(List<String> names) {
						    public A(String name) {
						        this(Collections.singletonList(name));
						    }
						    public static void main(String[] args) {
						        System.out.println(0);
						    }\
						}
						"""
			},
		"0");
	String expectedOutput = // constructor
			"""
		  // Method descriptor #10 (Ljava/util/List;)V
		  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V
		  // Stack: 2, Locals: 2
		  public A(java.util.List names);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Record() [13]
		     4  aload_0 [this]
		     5  aload_1 [names]
		     6  putfield A.names : java.util.List [16]
		     9  return
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "A.class", ClassFileBytesDisassembler.SYSTEM);

}

/**
 * Test that the following code doesn't result in generating byte code after the throw statement:
 * <pre>
 * record X(String s) {
 *    X {
 *        throw new RuntimeException();
 *    }
 * }
 * </pre>
 */
public void testRecordConstructorWithExceptionGh487() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			// test directory preparation
			true /* should flush output directory */,
			new String[] { /* test files */
					"X.java",
					"""
					public record X(String s) {
					    public X {
					        throw new RuntimeException();
					    }
					    public static void main(String[] args) throws Exception {
					        new X("");
					    }
					}
					""",
			},
			// compiler results
			"" /* expected compiler log */,
			// runtime results
			"" /* expected output string */,
			"""
			java.lang.RuntimeException
				at X.<init>(X.java:3)
				at X.main(X.java:6)
			""" /* expected error string */,
			// javac options
			JavacTestOptions.forRelease("16"));
	String expectedOutput = // constructor
			"""
			  // Method descriptor #8 (Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public X(java.lang.String s);
			     0  aload_0 [this]
			     1  invokespecial java.lang.Record() [10]
			     4  new java.lang.RuntimeException [13]
			     7  dup
			     8  invokespecial java.lang.RuntimeException() [15]
			    11  athrow
			""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1092
// Duplicate Annotation Error for Records
public void testGH1092() throws Exception {
	runConformTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.*;
						import java.lang.annotation.Target;
						import java.util.List;
						import java.lang.reflect.AnnotatedParameterizedType;
						
						@Target(ElementType.TYPE_USE)
						@Retention(RetentionPolicy.RUNTIME)
						@interface Ann {
						}
						
						record Record(
						    @Ann
						    List<@Ann String> list
						) {
						}
						
						public class X {
						
							static void assertDoesNotThrow(Runnable exe, String message) {
								exe.run();
							}
						\t
						    public static void main(String [] args) throws Exception {
						        AnnotatedParameterizedType listField = (AnnotatedParameterizedType) Record.class.getDeclaredMethod("list").getAnnotatedReturnType();
						        assertDoesNotThrow(listField::getAnnotatedActualTypeArguments, "Should not throw duplicate annotation exception.");
						    }
						}
						"""
				},
		"");

	// verify annotations on field
	String expectedOutput =
			"""
		  // Field descriptor #6 Ljava/util/List;
		  // Signature: Ljava/util/List<Ljava/lang/String;>;
		  private final java.util.List list;
		    RuntimeVisibleTypeAnnotations:\s
		      #10 @Ann(
		        target type = 0x13 FIELD
		      )
		      #10 @Ann(
		        target type = 0x13 FIELD
		        location = [TYPE_ARGUMENT(0)]
		      )
		 \s
		""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);

	// verify annotations on constructor
	expectedOutput =
			"""
				  // Method descriptor #12 (Ljava/util/List;)V
				  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V
				  // Stack: 2, Locals: 2
				  Record(java.util.List list);
				     0  aload_0 [this]
				     1  invokespecial java.lang.Record() [15]
				     4  aload_0 [this]
				     5  aload_1 [list]
				     6  putfield Record.list : java.util.List [18]
				     9  return
				      Line numbers:
				        [pc: 0, line: 11]
				      Local variable table:
				        [pc: 0, pc: 10] local: this index: 0 type: Record
				        [pc: 0, pc: 10] local: list index: 1 type: java.util.List
				      Local variable type table:
				        [pc: 0, pc: 10] local: list index: 1 type: java.util.List<java.lang.String>
				      Method Parameters:
				        list
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @Ann(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				      )
				      #10 @Ann(
				        target type = 0x16 METHOD_FORMAL_PARAMETER
				        method parameter index = 0
				        location = [TYPE_ARGUMENT(0)]
				      )
				 \s
				""" ;
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);

	// verify annotations on accessor
	expectedOutput =
			"""
				  // Method descriptor #26 ()Ljava/util/List;
				  // Signature: ()Ljava/util/List<Ljava/lang/String;>;
				  // Stack: 1, Locals: 1
				  public java.util.List list();
				    0  aload_0 [this]
				    1  getfield Record.list : java.util.List [18]
				    4  areturn
				      Line numbers:
				        [pc: 0, line: 13]
				    RuntimeVisibleTypeAnnotations:\s
				      #10 @Ann(
				        target type = 0x14 METHOD_RETURN
				      )
				      #10 @Ann(
				        target type = 0x14 METHOD_RETURN
				        location = [TYPE_ARGUMENT(0)]
				      )
				 \s
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);

	// verify annotations on record component
	expectedOutput =
			"""
				// Component descriptor #6 Ljava/util/List;
				// Signature: Ljava/util/List<Ljava/lang/String;>;
				java.util.List list;
				  RuntimeVisibleTypeAnnotations:\s
				    #10 @Ann(
				      target type = 0x13 FIELD
				    )
				    #10 @Ann(
				      target type = 0x13 FIELD
				      location = [TYPE_ARGUMENT(0)]
				    )
				""";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576719
// Useless warning in compact constructor of a record
public void testBug576719() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"Rational.java",
			"""
				public record Rational(int num, int denom) {
				    public Rational {
				        int gcd = gcd(num, denom);
				        num /= gcd;
				        denom /= gcd;
				    }
				   \s
				    private static int gcd(int a, int b) {
				        a = 10;
				        throw new UnsupportedOperationException();
				    }
				}
				""",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in Rational.java (at line 9)
				a = 10;
				^
			The parameter a should not be assigned
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void testGH1258() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
			public class Main {
				public static void main(String[] args) {
				MyRecord test = new MyRecord(0, 0);
				System.out.println(test.field1());
				}
			}

			@Deprecated(since = MyRecord.STATIC_VALUE)
			record MyRecord(int field1, int field2) {
				public static final String STATIC_VALUE = "test";
			}
			"""});
}
public void testIssue1218_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					 record R(T x);
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					record R(T x);
					         ^
				Cannot make a static reference to the non-static type T
				----------
				2. ERROR in X.java (at line 2)
					record R(T x);
					            ^
				Syntax error, insert "RecordBody" to complete ClassBodyDeclarations
				----------
				""");
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_001() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
	record InterfaceInRecord() {
	    sealed interface I {
	        enum Empty implements I {
	            INSTANCE;
	        }
	        record Single(double value) implements I {
	        }
	    }
	}
		class X {
		void foo() {
			Zork();
		}
	}

		"""},
		"""
			----------
			1. ERROR in X.java (at line 12)
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
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_002() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
			record InterfaceInRecord() {
			    sealed interface I  {
			        final class C implements I {
			        }
			    }
			}
			class X {
				void foo() {
					Zork();
				}
			}

		"""},
		"""
			----------
			1. ERROR in X.java (at line 9)
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
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_003() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		enum InterfaceInEnum {
        	INSTANCE;
			sealed interface I  {
		      	final class C implements I {}
			}
			final class D implements I {}
			final class E implements InterfaceInEnum.I {}
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"""
			----------
			1. ERROR in X.java (at line 11)
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
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_004() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		enum InterfaceInEnum {
        	INSTANCE;
			sealed interface I  {
		      	final class C implements I {}
			}
			final class D implements I {}
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"""
			----------
			1. ERROR in X.java (at line 10)
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
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testIssue1641_005() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		interface I  {
			enum E {
				First {}
			};
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"""
			----------
			1. ERROR in X.java (at line 8)
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
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testIssue1641_006() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		interface I  {
			enum E {
				First {
					@SuppressWarnings("unused")
					enum F {
						FirstOne {
							interface J {
								enum G {
									FirstTwo {}
								}
							}
						}
					};
				}
			};
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"""
			----------
			1. ERROR in X.java (at line 19)
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
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1806
// Parameters of compact canonical constructor are not marked as mandated
public void testGH1806() {
	runConformTest(
			new String[] {
					"MyRecord.java",
					"""
					public record MyRecord(int a) {

						public static void main(String[] args) {
							var ctor = MyRecord.class.getConstructors()[0];
							System.out.println(ctor.getParameters()[0].isImplicit());
						}

						public MyRecord {

						}

					}
					"""
			},
		"true");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1939
// [records] Class MethodBinding has a NullPointerException
public void testGH1939() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					public class X {

					    interface Foo {}

					    interface A {
					        <T extends Foo> Class<T> clazz() ;
					    }

					    record AA<T extends Foo>( Class<T> clazz ) implements A {}

					    public static void main(String [] args) {
					        System.out.println("OK!");
					    }
					}
					"""
			},
		"OK!");
}
}
