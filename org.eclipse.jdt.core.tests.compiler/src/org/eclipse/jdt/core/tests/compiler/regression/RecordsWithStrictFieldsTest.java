/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
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
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Replicas of tests from {@link RecordsRestrictedClassTest} whose results differ when run
 * at 28 with preview enabled, i.e. when record component fields are strictly initialized
 * (ACC_STRICT_INIT) and are assigned before chaining to java.lang.Record().
 */
@RunJavac
@PreviewTest
public class RecordsWithStrictFieldsTest extends AbstractRegressionTestCommon {

	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug550750_018" };
	}

	public static Class<?> testClass() {
		return RecordsWithStrictFieldsTest.class;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_28);
	}

	public RecordsWithStrictFieldsTest(String testName) {
		super(testName);
	}

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 28");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = getCompilerOptions(true);
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}

	@Override
	protected JavacTestOptions getJavacTestOptions() {
		return JAVAC_OPTIONS;
	}

	public void testBug550750_018() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point(int myInt, char myChar){\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The field \'myInt\' must be initialized before chaining to the super class constructor\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	public Point(int myInt, char myChar){\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The field \'myChar\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	public void testBug550750_022() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return;\n" +
			"	^^^^^^^\n" +
			"The body of a compact constructor must not contain a return statement\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	return;\n" +
			"	^^^^^^^\n" +
			"return; statement not allowed in an early construction context\n" +
			"----------\n");
	}

	public void testBug553152_007() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point(Integer myInt, int myZ) {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The field \'myZ\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	public void testBug553152_012() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return;\n" +
			"	^^^^^^^\n" +
			"The body of a compact constructor must not contain a return statement\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	return;\n" +
			"	^^^^^^^\n" +
			"return; statement not allowed in an early construction context\n" +
			"----------\n");
	}

	public void testBug553152_014() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The field \'myInt\' must be initialized before chaining to the super class constructor\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The field \'myZ\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	public void testBug553152_015() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	this.Point(0);\n" +
			"	^^^^\n" +
			"Cannot use \'this\' in an early construction context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	public Point(Integer myInt) {}\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^\n" +
			"A non-canonical constructor must invoke another constructor of the same class\n" +
			"----------\n");
	}

	public void testBug553153_002() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	if (this.myInt > 0)  // conditional assignment\n" +
			"	    ^^^^^^^^^^\n" +
			"Cannot refer to field myInt in an early construction context\n" +
			"----------\n");
	}

	public void testBug553153_004() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point(int myInt, char myChar) {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The field \'myChar\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
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
				"  1 : # 59 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;\n" +
				"	Method arguments:\n" +
				"		#1 Car\n" +
				"		#60 model;year\n" +
				"		#62 REF_getField model:Ljava/lang/String;\n" +
				"		#63 REF_getField year:I";
		verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
		"  // Field descriptor #8 Ljava/lang/String;\n" +
		"  private final strict_init java.lang.String model;\n" +
		"  \n" +
		"  // Field descriptor #10 I\n" +
		"  private final strict_init int year;\n" +
		"  \n" +
		"  // Method descriptor #12 (Ljava/lang/String;I)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  public Car(java.lang.String model, int year);\n" +
		"     0  aload_0 [this]\n" +
		"     1  aload_0 [this]\n" +
		"     2  aload_1 [model]\n" +
		"     3  putfield Car.model : java.lang.String [14]\n" +
		"     6  aload_0 [this]\n" +
		"     7  iload_2 [year]\n" +
		"     8  putfield Car.year : int [16]\n" +
		"    11  invokespecial java.lang.Record() [18]\n" +
		"    14  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 14, line: 4]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 15] local: this index: 0 type: Car\n" +
		"        [pc: 0, pc: 15] local: model index: 1 type: java.lang.String\n" +
		"        [pc: 0, pc: 15] local: year index: 2 type: int\n" +
		"      Method Parameters:\n" +
		"        mandated model\n" +
		"        mandated year\n" +
		"  \n";
		verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug561778_001() throws Exception {
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
				"""
				public record X<T>(T value) {
				}"""
			},
			"0");
		String expectedOutput =
				"  // Method descriptor #32 (Ljava/lang/Object;)V\n" +
				"  // Signature: (TT;)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  public X(java.lang.Object value);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [value]\n" +
				"     2  putfield X.value : java.lang.Object [12]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [34]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        value\n" +
				"\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #9 ()Ljava/lang/Object;\n" +
				"  // Signature: ()TT;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.Object value();\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_003() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCF int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD})
				@interface RCF {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @RCF(\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @RCF(\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_004() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCF int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCF {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #8 @RCF(\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #8 @RCF(\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_005() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RF int myInt, char myChar) {
				}
				@Target({ElementType.FIELD})
				@interface RF {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @RF(\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"// Component descriptor #10 C\n" +
				"char myChar;\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_006() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RF int myInt, char myChar) {
				}
				@Target({ElementType.FIELD})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RF {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #8 @RF(\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"// Component descriptor #10 C\n" +
				"char myChar;\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_007() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCFU int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.TYPE_USE})
				@interface RCFU {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @RCFU(\n" +
				"      )\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @RCFU(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @RCFU(\n" +
				"    )\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #8 @RCFU(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_008() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCFU int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCFU {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #8 @RCFU(\n" +
				"      )\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @RCFU(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #8 @RCFU(\n" +
				"    )\n" +
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #8 @RCFU(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_013() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCMU int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})
				@interface RCMU {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [14]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"      )\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @RCMU(\n" +
				"    )\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #8 @RCMU(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_014() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCMU int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCMU {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [14]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #8 @RCMU(\n" +
				"    )\n" +
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #8 @RCMU(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_015() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				      Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@T int myInt, char myChar) {
				}

				@Target({ElementType.TYPE_USE})
				@interface T {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [13]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  ";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #8 @T(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  Point(int myInt, char myChar);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [myInt]\n" +
				"     2  putfield Point.myInt : int [13]\n" +
				"     5  aload_0 [this]\n" +
				"     6  iload_2 [myChar]\n" +
				"     7  putfield Point.myChar : char [18]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [36]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        myInt\n" +
				"        myChar\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_016() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@T int myInt, char myChar) {
				}

				@Target({ElementType.TYPE_USE})
				@Retention(RetentionPolicy.RUNTIME)
				@interface T {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [13]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  ";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #8 @T(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  Point(int myInt, char myChar);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [myInt]\n" +
				"     2  putfield Point.myInt : int [13]\n" +
				"     5  aload_0 [this]\n" +
				"     6  iload_2 [myChar]\n" +
				"     7  putfield Point.myChar : char [18]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [36]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        myInt\n" +
				"        myChar\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @T(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_017() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCP int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
				@interface RCP {}
				"""
			},
			"100");
		String expectedOutput =
				"  Point(int myInt, char myChar);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [myInt]\n" +
				"     2  putfield Point.myInt : int [11]\n" +
				"     5  aload_0 [this]\n" +
				"     6  iload_2 [myChar]\n" +
				"     7  putfield Point.myChar : char [16]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [36]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        myInt\n" +
				"        myChar\n" +
				"    RuntimeInvisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #35 @RCP(\n" +
				"        )\n" +
				"      Number of annotations for parameter 1: 0\n" +
				"\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #35 @RCP(\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_018() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@RCP int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
				@Retention(RetentionPolicy.RUNTIME)
				@interface RCP {}
				"""
			},
			"100");
		String expectedOutput =
				"  Point(int myInt, char myChar);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [myInt]\n" +
				"     2  putfield Point.myInt : int [11]\n" +
				"     5  aload_0 [this]\n" +
				"     6  iload_2 [myChar]\n" +
				"     7  putfield Point.myChar : char [16]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [36]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        myInt\n" +
				"        myChar\n" +
				"    RuntimeVisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #35 @RCP(\n" +
				"        )\n" +
				"      Number of annotations for parameter 1: 0\n" +
				"\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #35 @RCP(\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_019() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@Annot int myInt, char myChar) {
				}
				@interface Annot {}
				"""
			},
			"100");
		String expectedOutput =
				"  // Field descriptor #6 I\n" +
				"  private final strict_init int myInt;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @Annot(\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  Point(int myInt, char myChar);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [myInt]\n" +
				"     2  putfield Point.myInt : int [13]\n" +
				"     5  aload_0 [this]\n" +
				"     6  iload_2 [myChar]\n" +
				"     7  putfield Point.myChar : char [18]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [37]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        myInt\n" +
				"        myChar\n" +
				"    RuntimeInvisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #8 @Annot(\n" +
				"        )\n" +
				"      Number of annotations for parameter 1: 0\n" +
				"\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [13]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @Annot(\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @Annot(\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug562439_020() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class X {
				  public static void main(String[] args){
				         Point p = new Point(100, 'a');
				      System.out.println(p.myInt());
				  }
				}

				record Point(@Annot int myInt, char myChar) {
				}
				@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
				@Retention(RetentionPolicy.RUNTIME)
				@interface Annot {}
				"""
			},
			"100");
		String expectedOutput =
				"  Point(int myInt, char myChar);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [myInt]\n" +
				"     2  putfield Point.myInt : int [11]\n" +
				"     5  aload_0 [this]\n" +
				"     6  iload_2 [myChar]\n" +
				"     7  putfield Point.myChar : char [16]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [36]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        myInt\n" +
				"        myChar\n" +
				"    RuntimeVisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #35 @Annot(\n" +
				"        )\n" +
				"      Number of annotations for parameter 1: 0\n" +
				"\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 I\n" +
				"int myInt;\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #35 @Annot(\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug564146_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public record X() {
				 public X() {
				   this(10);
				 }
				}"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	this(10);\n" +
			"	^^^^^^^^^\n" +
			"The constructor X(int) is undefined\n" +
			"----------\n");
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
		String expectedOutput =
				"  \n" +
				"  // Method descriptor #49 ([I)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  X(int[] j);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [j]\n" +
				"     2  putfield X.j : int[] [31]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [50]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        j\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @MyAnnot(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  public int[] j();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield X.j : int[] [31]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @MyAnnot(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
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
		String expectedOutput =
				"  \n" +
				"  // Method descriptor #49 ([I)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  X(int... j);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [j]\n" +
				"     2  putfield X.j : int[] [31]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [50]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        j\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @MyAnnot(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  public int[] j();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield X.j : int[] [31]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @MyAnnot(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug572204_007() throws Exception {
		runConformTest(
			new String[] {
				"R.java",
				"""
				import java.lang.annotation.*;
				@Target(ElementType.PARAMETER)
				@Retention(RetentionPolicy.RUNTIME)
				@interface I {}
				record R(@I String... s) {
				 public static void main(String[] args) {
				   System.out.println("helo");
				 }
				}
				"""
			},
			"helo");
		String expectedOutput =
				"  \n" +
				"  // Method descriptor #8 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  R(java.lang.String... s);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [s]\n" +
				"     2  putfield R.s : java.lang.String[] [28]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [48]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        s\n" +
				"    RuntimeVisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #47 @I(\n" +
				"        )\n";
		verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  \n" +
				"  // Method descriptor #27 ()[Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.String[] s();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield R.s : java.lang.String[] [28]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n";
		verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug573195_001() throws Exception {
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
		String expectedOutput =
				"  // Method descriptor #12 (I)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  protected X$R(int i);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [i]\n" +
				"     2  putfield X$R.i : int [20]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [36]\n" +
				"     9  return\n";
		verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testBug574284_001() throws Exception {
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
		String expectedOutput =
				"  // Method descriptor #14 (Z[I)V\n" +
				"  // Stack: 2, Locals: 3\n" +
				"  X$Rec(boolean isHidden, int... indexes);\n" +
				"     0  aload_0 [this]\n" +
				"     1  iload_1 [isHidden]\n" +
				"     2  putfield X$Rec.isHidden : boolean [21]\n" +
				"     5  aload_0 [this]\n" +
				"     6  aload_2 [indexes]\n" +
				"     7  putfield X$Rec.indexes : int[] [24]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Record() [41]\n" +
				"    14  return\n";
		verifyClassFile(expectedOutput, "X$Rec.class", ClassFileBytesDisassembler.SYSTEM);
	}

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

				    public static void main(String [] args) throws Exception {
				        AnnotatedParameterizedType listField = (AnnotatedParameterizedType) Record.class.getDeclaredMethod("list").getAnnotatedReturnType();
				        assertDoesNotThrow(listField::getAnnotatedActualTypeArguments, "Should not throw duplicate annotation exception.");
				    }
				}
				"""
			},
			"");
		String expectedOutput =
				"  // Field descriptor #6 Ljava/util/List;\n" +
				"  // Signature: Ljava/util/List<Ljava/lang/String;>;\n" +
				"  private final strict_init java.util.List list;\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @Ann(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #10 @Ann(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #34 (Ljava/util/List;)V\n" +
				"  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  Record(java.util.List list);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [list]\n" +
				"     2  putfield Record.list : java.util.List [14]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [36]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        list\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @Ann(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #10 @Ann(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"\n";
		verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  // Method descriptor #11 ()Ljava/util/List;\n" +
				"  // Signature: ()Ljava/util/List<Ljava/lang/String;>;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.util.List list();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Record.list : java.util.List [14]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #10 @Ann(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"      #10 @Ann(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"// Component descriptor #6 Ljava/util/List;\n" +
				"// Signature: Ljava/util/List<Ljava/lang/String;>;\n" +
				"java.util.List list;\n" +
				"  RuntimeVisibleTypeAnnotations: \n" +
				"    #10 @Ann(\n" +
				"      target type = 0x13 FIELD\n" +
				"    )\n" +
				"    #10 @Ann(\n" +
				"      target type = 0x13 FIELD\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testIssue365_001() throws Exception {
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
				    }}
				"""
			},
			"0");
		String expectedOutput =
				"  // Method descriptor #20 (Ljava/util/List;)V\n" +
				"  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  public A(java.util.List names);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [names]\n" +
				"     2  putfield A.names : java.util.List [46]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Record() [64]\n" +
				"     9  return\n";
		verifyClassFile(expectedOutput, "A.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testIssue3951() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				import java.lang.annotation.Annotation;
				import java.util.List;

				public record X (List<Class<? extends Annotation>> classes) {
					public X {

					}

					public static void main(String [] args) {
						System.out.println("Ok!");
					}
				}
				"""
			},
			"Ok!");
		String expectedOutput =
			"  // Field descriptor #6 Ljava/util/List;\n" +
			"  // Signature: Ljava/util/List<Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;>;\n" +
			"  private final strict_init java.util.List classes;\n" +
			"  \n" +
			"  // Method descriptor #10 (Ljava/util/List;)V\n" +
			"  // Signature: (Ljava/util/List<Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;>;)V\n" +
			"  // Stack: 3, Locals: 2\n" +
			"  public X(java.util.List classes);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_0 [this]\n" +
			"     2  aload_1 [classes]\n" +
			"     3  putfield X.classes : java.util.List [13]\n" +
			"     6  invokespecial java.lang.Record() [15]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 9, line: 7]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 10] local: classes index: 1 type: java.util.List\n" +
			"      Local variable type table:\n" +
			"        [pc: 0, pc: 10] local: classes index: 1 type: java.util.List<java.lang.Class<? extends java.lang.annotation.Annotation>>\n" +
			"      Method Parameters:\n" +
			"        mandated classes\n" +
			"  \n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testIssue4146_3() throws Exception {
		runConformTest(
			new String[] {
				"Segment.java",
				"""
				import jackson.stuff.JacksonXmlProperty;
				import jackson.stuff.JsonInclude;

				public record Segment(@JacksonXmlProperty(isAttribute = true) String id,
				                      String source,
				                      @JsonInclude(JsonInclude.Include.NON_NULL) String target) {

					public static void main(String [] args) {
						System.out.println("OK!");
					}
				}
				""",
				"jackson/stuff/JacksonXmlProperty.java",
				"""
				package jackson.stuff;
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
				@Retention(RetentionPolicy.RUNTIME)
				public @interface JacksonXmlProperty {
				    boolean isAttribute() default false;
				}
				""",
				"jackson/stuff/JsonInclude.java",
				"""
				package jackson.stuff;

				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;

				@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
				@JacksonAnnotation
				public @interface JsonInclude {
				    public enum Include {
				    	ALWAYS,
				        NON_NULL;
				    }
				    public Include value() default Include.ALWAYS;
				}
				""",
				"jackson/stuff/JacksonAnnotation.java",
				"""
				package jackson.stuff;

				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				@Target({ElementType.ANNOTATION_TYPE})
				@Retention(RetentionPolicy.RUNTIME)
				public @interface JacksonAnnotation {

				}
				"""
			},
			"OK!");

		String expectedOutput =
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  private final strict_init java.lang.String id;\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #8 @jackson.stuff.JacksonXmlProperty(\n" +
				"        #9 isAttribute=true (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  private final strict_init java.lang.String source;\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  private final strict_init java.lang.String target;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #14 @jackson.stuff.JsonInclude(\n" +
				"        #15 value=jackson.stuff.JsonInclude.Include.NON_NULL(enum type #16.#17)\n" +
				"      )\n";
		verifyClassFile(expectedOutput, "Segment.class", ClassFileBytesDisassembler.SYSTEM);

		expectedOutput =
				"  // Method descriptor #39 ()Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.String id();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Segment.id : java.lang.String [40]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #8 @jackson.stuff.JacksonXmlProperty(\n" +
				"        #9 isAttribute=true (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #39 ()Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.String source();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Segment.source : java.lang.String [42]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"  \n" +
				"  // Method descriptor #39 ()Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.String target();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Segment.target : java.lang.String [44]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #14 @jackson.stuff.JsonInclude(\n" +
				"        #15 value=jackson.stuff.JsonInclude.Include.NON_NULL(enum type #16.#17)\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Segment.class", ClassFileBytesDisassembler.SYSTEM);

		expectedOutput =
				"  // Method descriptor #61 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 4\n" +
				"  public Segment(java.lang.String id, java.lang.String source, java.lang.String target);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [id]\n" +
				"     2  putfield Segment.id : java.lang.String [40]\n" +
				"     5  aload_0 [this]\n" +
				"     6  aload_2 [source]\n" +
				"     7  putfield Segment.source : java.lang.String [42]\n" +
				"    10  aload_0 [this]\n" +
				"    11  aload_3 [target]\n" +
				"    12  putfield Segment.target : java.lang.String [44]\n" +
				"    15  aload_0 [this]\n" +
				"    16  invokespecial java.lang.Record() [64]\n" +
				"    19  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        id\n" +
				"        source\n" +
				"        target\n" +
				"    RuntimeVisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #8 @jackson.stuff.JacksonXmlProperty(\n" +
				"          #9 isAttribute=true (constant type)\n" +
				"        )\n" +
				"      Number of annotations for parameter 1: 0\n" +
				"      Number of annotations for parameter 2: 0\n" +
				"    RuntimeInvisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 0\n" +
				"      Number of annotations for parameter 1: 0\n" +
				"      Number of annotations for parameter 2: 1\n" +
				"        #14 @jackson.stuff.JsonInclude(\n" +
				"          #15 value=jackson.stuff.JsonInclude.Include.NON_NULL(enum type #16.#17)\n" +
				"        )\n" +
				"\n";
		verifyClassFile(expectedOutput, "Segment.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testIssue4616_fuller() throws Exception {
		runConformTest(
			new String[] {
				"test/Broken.java",
				"""
				package test;

				import test.Schema.RequiredMode;

				import java.util.Map;

				public record Broken(

				        @Schema(description = "str", requiredMode = RequiredMode.NOT_REQUIRED) @JsonProperty("str")  String externalAccountId,

				        @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") String productId,

				        @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") Broken.UpsertEnvironmentDto environment,

				        @Schema(description = \"""
				            str\""", requiredMode = RequiredMode.NOT_REQUIRED) @JsonProperty("str") Map<String, String> metadata) {
				    public record UpsertEnvironmentDto(

				            @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") String id,

				            @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") String url) {

				    }
				    public static void main(String [] args) {
					    System.out.println("OK!");
					}
				}


				@interface JsonProperty {
					String value();
				}

				@interface Schema {

				    RequiredMode requiredMode();

				    String description();

				    enum RequiredMode {
				        REQUIRED,
				        NOT_REQUIRED,
				    }
				}
				"""
			},
			"OK!");
		String expectedOutput =
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  private final strict_init java.lang.String externalAccountId;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/lang/String;\n" +
				"  private final strict_init java.lang.String productId;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #19 Ltest/Broken$UpsertEnvironmentDto;\n" +
				"  private final strict_init test.Broken$UpsertEnvironmentDto environment;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #21 Ljava/util/Map;\n" +
				"  // Signature: Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;\n" +
				"  private final strict_init java.util.Map metadata;\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #25 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"    0  getstatic java.lang.System.out : java.io.PrintStream [27]\n" +
				"    3  ldc <String \"OK!\"> [33]\n" +
				"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [35]\n" +
				"    8  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 25]\n" +
				"        [pc: 8, line: 26]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" +
				"  \n" +
				"  // Method descriptor #45 ()Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.String externalAccountId();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield test.Broken.externalAccountId : java.lang.String [46]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #45 ()Ljava/lang/String;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.lang.String productId();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield test.Broken.productId : java.lang.String [48]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #50 ()Ltest/Broken$UpsertEnvironmentDto;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public test.Broken.UpsertEnvironmentDto environment();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield test.Broken.environment : test.Broken.UpsertEnvironmentDto [51]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #53 ()Ljava/util/Map;\n" +
				"  // Signature: ()Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public java.util.Map metadata();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield test.Broken.metadata : java.util.Map [55]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 16]\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @test.Schema(\n" +
				"        #9 description=\"str\" (constant type)\n" +
				"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"      )\n" +
				"      #14 @test.JsonProperty(\n" +
				"        #15 value=\"str\" (constant type)\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #45 ()Ljava/lang/String;\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  public final java.lang.String toString();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokedynamic 0 toString(test.Broken) : java.lang.String [58]\n" +
				"    6  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"  \n" +
				"  // Method descriptor #62 ()I\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  public final int hashCode();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokedynamic 0 hashCode(test.Broken) : int [63]\n" +
				"    6  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"  \n" +
				"  // Method descriptor #67 (Ljava/lang/Object;)Z\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  public final boolean equals(java.lang.Object arg0);\n" +
				"    0  aload_0 [this]\n" +
				"    1  aload_1 [arg0]\n" +
				"    2  invokedynamic 0 equals(test.Broken, java.lang.Object) : boolean [68]\n" +
				"    7  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"  \n" +
				"  // Method descriptor #72 (Ljava/lang/String;Ljava/lang/String;Ltest/Broken$UpsertEnvironmentDto;Ljava/util/Map;)V\n" +
				"  // Signature: (Ljava/lang/String;Ljava/lang/String;Ltest/Broken$UpsertEnvironmentDto;Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;)V\n" +
				"  // Stack: 2, Locals: 5\n" +
				"  public Broken(java.lang.String externalAccountId, java.lang.String productId, test.Broken.UpsertEnvironmentDto environment, java.util.Map metadata);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [externalAccountId]\n" +
				"     2  putfield test.Broken.externalAccountId : java.lang.String [46]\n" +
				"     5  aload_0 [this]\n" +
				"     6  aload_2 [productId]\n" +
				"     7  putfield test.Broken.productId : java.lang.String [48]\n" +
				"    10  aload_0 [this]\n" +
				"    11  aload_3 [environment]\n" +
				"    12  putfield test.Broken.environment : test.Broken.UpsertEnvironmentDto [51]\n" +
				"    15  aload_0 [this]\n" +
				"    16  aload 4 [metadata]\n" +
				"    18  putfield test.Broken.metadata : java.util.Map [55]\n" +
				"    21  aload_0 [this]\n" +
				"    22  invokespecial java.lang.Record() [75]\n" +
				"    25  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        externalAccountId\n" +
				"        productId\n" +
				"        environment\n" +
				"        metadata\n" +
				"    RuntimeInvisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 2\n" +
				"        #8 @test.Schema(\n" +
				"          #9 description=\"str\" (constant type)\n" +
				"          #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"        )\n" +
				"        #14 @test.JsonProperty(\n" +
				"          #15 value=\"str\" (constant type)\n" +
				"        )\n" +
				"      Number of annotations for parameter 1: 2\n" +
				"        #8 @test.Schema(\n" +
				"          #9 description=\"str\" (constant type)\n" +
				"          #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"        )\n" +
				"        #14 @test.JsonProperty(\n" +
				"          #15 value=\"str\" (constant type)\n" +
				"        )\n" +
				"      Number of annotations for parameter 2: 2\n" +
				"        #8 @test.Schema(\n" +
				"          #9 description=\"str\" (constant type)\n" +
				"          #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"        )\n" +
				"        #14 @test.JsonProperty(\n" +
				"          #15 value=\"str\" (constant type)\n" +
				"        )\n" +
				"      Number of annotations for parameter 3: 2\n" +
				"        #8 @test.Schema(\n" +
				"          #9 description=\"str\" (constant type)\n" +
				"          #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"        )\n" +
				"        #14 @test.JsonProperty(\n" +
				"          #15 value=\"str\" (constant type)\n" +
				"        )\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #96 java/lang/invoke/MethodHandles$Lookup, outer class info: #98 java/lang/invoke/MethodHandles\n" +
				"     inner name: #100 Lookup, accessflags: 57 public static final],\n" +
				"    [inner class info: #101 test/Broken$UpsertEnvironmentDto, outer class info: #1 test/Broken\n" +
				"     inner name: #103 UpsertEnvironmentDto, accessflags: 57 public static final],\n" +
				"    [inner class info: #104 test/Schema$RequiredMode, outer class info: #106 test/Schema\n" +
				"     inner name: #108 RequiredMode, accessflags: 16441 public static final]\n" +
				"\n" +
				"Nest Members:\n" +
				"   #101 test/Broken$UpsertEnvironmentDto\n" +
				"\n" +
				"Record: #Record\n" +
				"Components:\n" +
				"  \n" +
				"// Component descriptor #6 Ljava/lang/String;\n" +
				"java.lang.String externalAccountId;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @test.Schema(\n" +
				"      #9 description=\"str\" (constant type)\n" +
				"      #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"    )\n" +
				"    #14 @test.JsonProperty(\n" +
				"      #15 value=\"str\" (constant type)\n" +
				"    )\n" +
				"// Component descriptor #6 Ljava/lang/String;\n" +
				"java.lang.String productId;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @test.Schema(\n" +
				"      #9 description=\"str\" (constant type)\n" +
				"      #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"    )\n" +
				"    #14 @test.JsonProperty(\n" +
				"      #15 value=\"str\" (constant type)\n" +
				"    )\n" +
				"// Component descriptor #19 Ltest/Broken$UpsertEnvironmentDto;\n" +
				"test.Broken$UpsertEnvironmentDto environment;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @test.Schema(\n" +
				"      #9 description=\"str\" (constant type)\n" +
				"      #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
				"    )\n" +
				"    #14 @test.JsonProperty(\n" +
				"      #15 value=\"str\" (constant type)\n" +
				"    )\n" +
				"// Component descriptor #21 Ljava/util/Map;\n" +
				"// Signature: Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;\n" +
				"java.util.Map metadata;\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #8 @test.Schema(\n" +
				"      #9 description=\"str\" (constant type)\n" +
				"      #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
				"    )\n" +
				"    #14 @test.JsonProperty(\n" +
				"      #15 value=\"str\" (constant type)\n" +
				"    )\n";
		verifyClassFile(expectedOutput, "test/Broken.class", ClassFileBytesDisassembler.SYSTEM);
	}

	public void testIssue4622() throws Exception {
		runConformTest(
			new String[] {
				"test/Record.java",
				"""
				package test;

				import java.lang.annotation.Target;

				public record Record(
				        Patch<@ValidUrlTemplate(value = UrlValidationType.AA, message = "{}") String> labelUrl) {

				        public static void main(String [] args) {
					    	System.out.println("OK!");
					}
				}

				class Patch<T> {
				    public T field;
				}

				enum UrlValidationType {
				    AA, BB
				}

				@Target(java.lang.annotation.ElementType.TYPE_USE)
				@interface ValidUrlTemplate {
				    UrlValidationType value();
				    String message();
				}
				"""
			},
			"OK!");
		String expectedOutput =
				"  // Field descriptor #6 Ltest/Patch;\n" +
				"  // Signature: Ltest/Patch<Ljava/lang/String;>;\n" +
				"  private final strict_init test.Patch labelUrl;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @test.ValidUrlTemplate(\n" +
				"        #11 value=test.UrlValidationType.AA(enum type #12.#13)\n" +
				"        #14 message=\"{}\" (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"    0  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
				"    3  ldc <String \"OK!\"> [25]\n" +
				"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [27]\n" +
				"    8  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"        [pc: 8, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" +
				"  \n" +
				"  // Method descriptor #37 ()Ltest/Patch;\n" +
				"  // Signature: ()Ltest/Patch<Ljava/lang/String;>;\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public test.Patch labelUrl();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield test.Record.labelUrl : test.Patch [39]\n" +
				"    4  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @test.ValidUrlTemplate(\n" +
				"        #11 value=test.UrlValidationType.AA(enum type #12.#13)\n" +
				"        #14 message=\"{}\" (constant type)\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )";
		verifyClassFile(expectedOutput, "test/Record.class", ClassFileBytesDisassembler.SYSTEM);
	}
}