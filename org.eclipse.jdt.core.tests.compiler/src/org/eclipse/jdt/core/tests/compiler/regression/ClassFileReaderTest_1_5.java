/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_1_5 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 16 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}
	public static Class testClass() {
		return ClassFileReaderTest_1_5.class;
	}

	public ClassFileReaderTest_1_5(String name) {
		super(name);
	}

	/**
	 * @deprecated
	 */
	private void checkClassFileUsingInputStream(String directoryName, String className, String source, String expectedOutput, int mode) throws IOException {
		compileAndDeploy(source, directoryName, className, false);
		BufferedInputStream inputStream = null;
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, className + ".class");
			inputStream = new BufferedInputStream(new FileInputStream(f));
			IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(inputStream, IClassFileReader.ALL);
			assertNotNull(classFileReader);
			String result = ToolFactory.createDefaultClassFileDisassembler().disassemble(classFileReader, "\n", mode);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
			removeTempClass(className);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test001() throws Exception {
		String source =
			"""
			public class X {
				X(String s) {
				}
				public void foo(int i, long l, String[][]... args) {
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V
			  // Stack: 0, Locals: 5
			  public void foo(int i, long l, java.lang.String[][]... args);
			    0  return
			      Line numbers:
			        [pc: 0, line: 5]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: i index: 1 type: int
			        [pc: 0, pc: 1] local: l index: 2 type: long
			        [pc: 0, pc: 1] local: args index: 4 type: java.lang.String[][][]
			}""";
		checkClassFile("X", source, expectedOutput);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76472
	 */
	public void test002() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					long[] tab = new long[] {};
					System.out.println(tab.clone());
					System.out.println(tab.clone());
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  newarray long [11]
			     3  astore_1 [tab]
			     4  getstatic java.lang.System.out : java.io.PrintStream [16]
			     7  aload_1 [tab]
			     8  invokevirtual long[].clone() : java.lang.Object [22]
			    11  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]
			    14  getstatic java.lang.System.out : java.io.PrintStream [16]
			    17  aload_1 [tab]
			    18  invokevirtual long[].clone() : java.lang.Object [22]
			    21  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]
			    24  return
			""";
		checkClassFile("X", source, expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111420
	public void test003() throws Exception {
		String source =
			"""
			public class Y<W, U extends java.io.Reader & java.io.Serializable> {
			  U field;
			  String field2;
			  <T> Y(T t) {}
			  <T> T foo(T t, String... s) {
			    return t;
			  }
			}""";
		String expectedOutput =
			"""
			public class Y<W,U extends Reader & Serializable> {
			 \s
			  U field;
			 \s
			  String field2;
			 \s
			  <T> Y(T t) {
			  }
			 \s
			  <T> T foo(T t, String... s) {
			    return null;
			  }
			}""";
		checkClassFile("", "Y", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111420
	public void test004() throws Exception {
		String source =
			"""
			public class Y<W, U extends java.io.Reader & java.io.Serializable> {
			  U field;
			  String field2;
			  <T> Y(T t) {}
			  <T> T foo(T t, String... s) {
			    return t;
			  }
			}""";
		String expectedOutput =
			"""
			public class Y<W,U extends java.io.Reader & java.io.Serializable> {
			 \s
			  U field;
			 \s
			  java.lang.String field2;
			 \s
			  <T> Y(T t) {
			  }
			 \s
			  <T> T foo(T t, java.lang.String... s) {
			    return null;
			  }
			}""";
		checkClassFile("", "Y", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test005() throws Exception {
		String source =
			"""
			public class X {
				X(String s) {
				}
				public static void foo(int i, long l, String[][]... args) {
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V
			  // Stack: 0, Locals: 4
			  public static void foo(int i, long l, java.lang.String[][]... args);
			    0  return
			      Line numbers:
			        [pc: 0, line: 5]
			      Local variable table:
			        [pc: 0, pc: 1] local: i index: 0 type: int
			        [pc: 0, pc: 1] local: l index: 1 type: long
			        [pc: 0, pc: 1] local: args index: 3 type: java.lang.String[][][]
			}""";
		checkClassFile("X", source, expectedOutput);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 */
	public void test006() throws Exception {
		String source =
			"""
			public enum X {\s
			\t
				BLEU(10),
				BLANC(20),
				ROUGE(30);
				X(int i) {}
			}
			""";
		String expectedOutput =
			"""
			public enum X {
			 \s
			  BLEU(0),
			 \s
			  BLANC(0),
			 \s
			  ROUGE(0),;
			 \s
			  private X(int i) {
			  }
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 * TODO corner case that doesn't produce the right source
	 */
	public void test007() throws Exception {
		String source =
			"""
			public enum X {
				BLEU(0) {
					public String colorName() {
						return "BLEU";
					}
				},
				BLANC(1) {
					public String colorName() {
						return "BLANC";
					}
				},
				ROUGE(2) {
					public String colorName() {
						return "ROUGE";
					}
				},;
			\t
				X(int i) {
				}
				abstract public String colorName();
			}""";
		String expectedOutput =
			"""
			public enum X {
			 \s
			  BLEU(0),
			 \s
			  BLANC(0),
			 \s
			  ROUGE(0),;
			 \s
			  private X(int i) {
			  }
			 \s
			  public abstract java.lang.String colorName();
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 * TODO corner case that doesn't produce the right source
	 */
	public void test008() throws Exception {
		String source =
			"""
			interface I {
				String colorName();
			}
			public enum X implements I {
				BLEU(0) {
					public String colorName() {
						return "BLEU";
					}
				},
				BLANC(1) {
					public String colorName() {
						return "BLANC";
					}
				},
				ROUGE(2) {
					public String colorName() {
						return "ROUGE";
					}
				},;
			\t
				X(int i) {
				}
			}""";
		String expectedOutput =
			"""
			public enum X implements I {
			 \s
			  BLEU(0),
			 \s
			  BLANC(0),
			 \s
			  ROUGE(0),;
			 \s
			  private X(int i) {
			  }
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111767
	 */
	public void test009() throws Exception {
		String source =
			"""
			@interface X {
				String firstName();
				String lastName() default "Smith";
			}
			""";
		String expectedOutput =
			"""
			abstract @interface X {
			 \s
			  public abstract java.lang.String firstName();
			 \s
			  public abstract java.lang.String lastName() default "Smith";
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111767
	 * @deprecated Using deprecated API
	 */
	public void test010() throws Exception {
		String source =
			"""
			@interface X {
				String firstName();
				String lastName() default "Smith";
			}
			""";
		String expectedOutput =
			"""
			abstract @interface X {
			 \s
			  public abstract java.lang.String firstName();
			 \s
			  public abstract java.lang.String lastName() default "Smith";
			}""";
		checkClassFileUsingInputStream("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203577
	 */
	public void test011() throws Exception {
		String source =
			"""
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			
			@Target(value={})
			@Retention(value=RetentionPolicy.RUNTIME)
			public @interface X {}""";
		String expectedOutput =
			"""
			public abstract @interface X extends java.lang.annotation.Annotation {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 class: #6 java/lang/annotation/Annotation
			    constant #6 utf8: "java/lang/annotation/Annotation"
			    constant #7 utf8: "SourceFile"
			    constant #8 utf8: "X.java"
			    constant #9 utf8: "RuntimeVisibleAnnotations"
			    constant #10 utf8: "Ljava/lang/annotation/Target;"
			    constant #11 utf8: "value"
			    constant #12 utf8: "Ljava/lang/annotation/Retention;"
			    constant #13 utf8: "Ljava/lang/annotation/RetentionPolicy;"
			    constant #14 utf8: "RUNTIME"
			
			  RuntimeVisibleAnnotations:\s
			    #10 @java.lang.annotation.Target(
			      #11 value=[
			        ]
			    )
			    #12 @java.lang.annotation.Retention(
			      #11 value=java.lang.annotation.RetentionPolicy.RUNTIME(enum type #13.#14)
			    )
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203609
	 */
	public void test012() throws Exception {
		String source =
			"@Deprecated\n" +
			"package p;";
		String expectedOutput =
			"abstract interface p.package-info {\n" +
			"}";
		if (this.complianceLevel > ClassFileConstants.JDK1_5) {
			expectedOutput = "abstract synthetic interface p.package-info {\n" +
			"}";
		}
		checkClassFile("p", "package-info", source, expectedOutput, ClassFileBytesDisassembler.DEFAULT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=217907
	 */
	public void test013() throws Exception {
		String source =
			"""
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			
			@Target(value={})
			@Retention(value=RetentionPolicy.RUNTIME)
			public @interface X {}""";
		String expectedOutput =
			"""
			public abstract @interface X extends Annotation {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 class: #6 java/lang/annotation/Annotation
			    constant #6 utf8: "java/lang/annotation/Annotation"
			    constant #7 utf8: "SourceFile"
			    constant #8 utf8: "X.java"
			    constant #9 utf8: "RuntimeVisibleAnnotations"
			    constant #10 utf8: "Ljava/lang/annotation/Target;"
			    constant #11 utf8: "value"
			    constant #12 utf8: "Ljava/lang/annotation/Retention;"
			    constant #13 utf8: "Ljava/lang/annotation/RetentionPolicy;"
			    constant #14 utf8: "RUNTIME"
			
			  RuntimeVisibleAnnotations:\s
			    #10 @Target(
			      #11 value=[
			        ]
			    )
			    #12 @Retention(
			      #11 value=RetentionPolicy.RUNTIME(enum type #13.#14)
			    )
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.SYSTEM | ClassFileBytesDisassembler.COMPACT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=217907
	 */
	public void test014() throws Exception {
		String source =
			"""
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			
			@Target(value={})
			@Retention(value=RetentionPolicy.RUNTIME)
			public @interface X {}""";
		String expectedOutput =
			"""
			@Target(value={})
			@Retention(value=RetentionPolicy.RUNTIME)
			public abstract @interface X extends Annotation {
			
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=217910
	 */
	public void test015() throws Exception {
		String source =
			"""
			import java.lang.annotation.Retention;
			import static java.lang.annotation.RetentionPolicy.*;
			public class X {
			        public void foo(@Deprecated @Annot(2) int i) {}
			}
			@Retention(CLASS)
			@interface Annot {
			        int value() default -1;
			}""";
		String expectedOutput =
			"  public void foo(@Deprecated @Annot(value=(int) 2) int i);";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=286405
	 */
	public void test016() throws Exception {
		String source =
			"public @interface MonAnnotation {\n" +
			"	String test1() default \"\\0\";\n" +
			"	char test2() default '\\0';\n" +
			"}\n" +
			"";
		String expectedOutput =
			"  public abstract char test2() default \'\\u0000\';";
		checkClassFile("", "MonAnnotation", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}

	public void testBug504031() throws Exception {
		String source =
				"""
			package test;
			@RunWith(JUnitPlatform.class)
			@SelectPackages("test.dynamic.TODO")
			public class AllTests {
				@Test
				void test1() {
				}
			}\s
			
			@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
			@interface RunWith {
			    Class<? extends Runner> value();
			}
			@interface SelectPackages {
			}
			class Runner {}
			@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
			@interface Test {}
			""";

		String expectedOutput =
				"@RunWith(value=JUnitPlatform)\n" +
				"public class test.AllTests {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 test/AllTests\n" +
				"    constant #2 utf8: \"test/AllTests\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"<init>\"\n" +
				"    constant #6 utf8: \"()V\"\n" +
				"    constant #7 utf8: \"Code\"\n" +
				"    constant #8 class: #9 java/lang/Error\n" +
				"    constant #9 utf8: \"java/lang/Error\"\n" +
				"    constant #10 string: #11 \"Unresolved compilation problems: \\n\\tJUnitPlatform cannot be resolved to a type\\n\\tClass<JUnitPlatform> cannot be resolved to a type\\n\\tThe attribute value is undefined for the annotation type SelectPackages\\n\"\n" +
				"    constant #11 utf8: \"Unresolved compilation problems: \\n\\tJUnitPlatform cannot be resolved to a type\\n\\tClass<JUnitPlatform> cannot be resolved to a type\\n\\tThe attribute value is undefined for the annotation type SelectPackages\\n\"\n" +
				"    constant #12 method_ref: #8.#13 java/lang/Error.<init> (Ljava/lang/String;)V\n" +
				"    constant #13 name_and_type: #5.#14 <init> (Ljava/lang/String;)V\n" +
				"    constant #14 utf8: \"(Ljava/lang/String;)V\"\n" +
				"    constant #15 utf8: \"LineNumberTable\"\n" +
				"    constant #16 utf8: \"LocalVariableTable\"\n" +
				"    constant #17 utf8: \"this\"\n" +
				"    constant #18 utf8: \"Ltest/AllTests;\"\n" +
				"    constant #19 utf8: \"test1\"\n" +
				"    constant #20 utf8: \"RuntimeVisibleAnnotations\"\n" +
				"    constant #21 utf8: \"Ltest/Test;\"\n" +
				"    constant #22 string: #23 \"Unresolved compilation problem: \\n\"\n" +
				"    constant #23 utf8: \"Unresolved compilation problem: \\n\"\n" +
				"    constant #24 utf8: \"SourceFile\"\n" +
				"    constant #25 utf8: \"AllTests.java\"\n" +
				"    constant #26 utf8: \"RuntimeInvisibleAnnotations\"\n" + // unused but tolerated
				"    constant #27 utf8: \"Ltest/SelectPackages;\"\n" + // unused but tolerated
				"    constant #28 utf8: \"value\"\n" +
				"    constant #29 utf8: \"Ltest/RunWith;\"\n" +
				"    constant #30 utf8: \"LJUnitPlatform;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 3, Locals: 1\n" +
				"  public AllTests();\n" +
				"     0  new Error [8]\n" +
				"     3  dup\n" +
				"     4  ldc <String \"Unresolved compilation problems: \\n\\tJUnitPlatform cannot be resolved to a type\\n\\tClass<JUnitPlatform> cannot be resolved to a type\\n\\tThe attribute value is undefined for the annotation type SelectPackages\\n\"> [10]\n" +
				"     6  invokespecial Error(String) [12]\n" +
				"     9  athrow\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 10] local: this index: 0 type: AllTests\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 3, Locals: 1\n" +
				"  @Test\n" +
				"  void test1();\n" +
				"     0  new Error [8]\n" +
				"     3  dup\n" +
				"     4  ldc <String \"Unresolved compilation problem: \\n\"> [22]\n" +
				"     6  invokespecial Error(String) [12]\n" +
				"     9  athrow\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 10] local: this index: 0 type: AllTests\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #21 @Test(\n" +
				"      )\n" +
				"\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #29 @RunWith(\n" +
				"      #28 value=JUnitPlatform (#30 class type)\n" +
				"    )\n" +
				"}";
		int mode = ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT | ClassFileBytesDisassembler.SYSTEM;
		checkClassFile("test", "AllTests", "AllTests", source, expectedOutput, mode, true/*suppress expected errors*/);
	}

}
