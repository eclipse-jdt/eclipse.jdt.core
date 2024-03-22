/*******************************************************************************
 * Copyright (c) 2011, 2023 IBM Corporation and others.
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
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 415541 - [1.8][compiler] Type annotations in the body of static initializer get dropped
 *                          Bug 415543 - [1.8][compiler] Incorrect bound index in RuntimeInvisibleTypeAnnotations attribute
 *                          Bug 415397 - [1.8][compiler] Type Annotations on wildcard type argument dropped
 *                          Bug 415399 - [1.8][compiler] Type annotations on constructor results dropped by the code generator
 *                          Bug 415470 - [1.8][compiler] Type annotations on class declaration go vanishing
 *                          Bug 414384 - [1.8] type annotation on abbreviated inner class is not marked as inner type
 *     Jesper S Moller - Contributions for
 *                          Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSR308SpecSnippetTests extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 19 };
//		TESTS_NAMES = new String [] { "test033" };
	}
	public static Class testClass() {
		return JSR308SpecSnippetTests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public JSR308SpecSnippetTests(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	@Override
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}

	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.List;
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					class Document {}
					public class X {
						Map<@NonNull String, @NonEmpty List<@Readonly Document>> files;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @NonNull(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #11 @NonEmpty(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			      #12 @Readonly(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// note, javac 8b100 emits offset incorrectly.
	public void test002() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					class Document {}
					public class X {
						static void foo(X o) {
							o.<@NonNull String>m("...");
						}
						<T> void m(String s) {}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @NonNull(
			        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT
			        offset = 3
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test003() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.Collection;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Existing {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					class File {}
					class X<F extends @Existing File> {\s
						Collection<? super @Existing File> c;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @Existing(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), WILDCARD]
			      )
			 \s
			  // Method descriptor #12 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [14]
			    4  return
			      Line numbers:
			        [pc: 0, line: 11]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			      Local variable type table:
			        [pc: 0, pc: 5] local: this index: 0 type: X<F>
			
			  RuntimeInvisibleTypeAnnotations:\s
			    #10 @Existing(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 0
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test004() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.List;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Existing {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					class File {}
					abstract class X<T> implements @Readonly List<@Readonly T> { }
					""",
		},
		"");
		String pos = isJRE21Plus ? "28" : "23";
		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #" + pos + " @Readonly(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"    )\n" +
				"    #" + pos + " @Readonly(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test005() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.List;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Critical {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					class TemperatureException extends RuntimeException{}
					class X {
						void monitorTemperature() throws @Critical TemperatureException {}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @Critical(
			        target type = 0x17 THROWS
			        throws index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test006() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Interned {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					@Target(TYPE_USE)
					@interface Tainted {}
					class MyObject {
						class NestedClass {}
					}
					class List<T> {}
					class X {
						static void monitorTemperature(MyObject myVar) {
							new <String> @Interned MyObject();
							new @NonEmpty @Readonly List<String>();
							myVar.new @Tainted NestedClass();
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #33 @Interned(
			        target type = 0x44 NEW
			        offset = 0
			      )
			      #34 @NonEmpty(
			        target type = 0x44 NEW
			        offset = 6
			      )
			      #35 @Readonly(
			        target type = 0x44 NEW
			        offset = 6
			      )
			      #36 @Tainted(
			        target type = 0x44 NEW
			        offset = 12
			        location = [INNER_TYPE]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test007() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.Map;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface Readonly {}
					@Target(TYPE_USE)
					@interface Tainted {}
					class MyObject {
						class NestedClass {}
					}
					class List<T> {}
					class X {
							Map.@NonNull Entry e;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @NonNull(
			        target type = 0x13 FIELD
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test008() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface A {}
					@Target(TYPE_USE)
					@interface B {}
					class MyObject {
						class NestedClass {}
					}
					class List<T> {}
					class Type1 {}
					interface Type2 {}
					class X {
						static void monitorTemperature(Object myObject) {
							String myString = (@NonNull String) myObject;
							Type1 x = (@A Type1 & @B Type2) null;
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #29 @NonNull(
			        target type = 0x47 CAST
			        offset = 1
			        type argument index = 0
			      )
			      #30 @B(
			        target type = 0x47 CAST
			        offset = 6
			        type argument index = 1
			      )
			      #31 @A(
			        target type = 0x47 CAST
			        offset = 9
			        type argument index = 0
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test009() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface A {}
					@Target(TYPE_USE)
					@interface B {}
					class MyObject {
						class NestedClass {}
					}
					class List<T> {}
					class Type1 {}
					interface Type2 {}
					class X {
						static void monitorTemperature(Object myObject) {
							boolean isNonNull = myObject instanceof @NonNull String;
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #23 @NonNull(
			        target type = 0x43 INSTANCEOF
			        offset = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test010() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.Arrays;
					import java.util.Date;
					import java.util.List;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					@Target(TYPE_USE)
					@interface English {}
					@Target(TYPE_USE)
					@interface Vernal {}
					@Target(TYPE_USE)
					@interface NonNegative {}
					class MyObject {
						class NestedClass {}
					}
					class Type1 {}
					interface I {
						int f(Date d);
					}
					interface J {
						int f(List l);
					}
					interface K {
						void s(int [] ia);
					}
					class X {
						static void monitorTemperature(Object myObject) {
							I i = @Vernal Date::getDay;
							J j  = List<@English String>::size;
							K k = Arrays::<@NonNegative Integer>sort;
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #36 @Vernal(
			        target type = 0x46 METHOD_REFERENCE
			        offset = 0
			      )
			      #37 @English(
			        target type = 0x46 METHOD_REFERENCE
			        offset = 6
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #38 @NonNegative(
			        target type = 0x4b METHOD_REFERENCE_TYPE_ARGUMENT
			        offset = 12
			        type argument index = 0
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test011() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.*;
					import java.io.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Immutable { int value() default 0; }
					class X {
						List<@Immutable ? extends Comparable<X>> a;
						List<? extends @Immutable Comparable<X>> b;
						List<@Immutable(1) ? extends @Immutable(2) Comparable<X>> c;
						Map<@Immutable(1) ? extends Comparable<X>,@Immutable(2) ? extends @Immutable(3) Serializable> d;
					}
					""",
		},
		"");
		// javac b100
		// Field a:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(): FIELD, location=[TYPE_ARGUMENT(0)]
		// Field b:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(): FIELD, location=[TYPE_ARGUMENT(0), WILDCARD]
		// Field c:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(#12=I#13): FIELD, location=[TYPE_ARGUMENT(0)]
		//    1: #9(#12=I#14): FIELD, location=[TYPE_ARGUMENT(0), WILDCARD]
		// Field d:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(#12=I#13): FIELD, location=[TYPE_ARGUMENT(0)]
		//    1: #9(#12=I#14): FIELD, location=[TYPE_ARGUMENT(1)]
		//    2: #9(#12=I#18): FIELD, location=[TYPE_ARGUMENT(1), WILDCARD]
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "a"
			    constant #6 utf8: "Ljava/util/List;"
			    constant #7 utf8: "Signature"
			    constant #8 utf8: "Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;"
			    constant #9 utf8: "RuntimeInvisibleTypeAnnotations"
			    constant #10 utf8: "LImmutable;"
			    constant #11 utf8: "b"
			    constant #12 utf8: "c"
			    constant #13 utf8: "value"
			    constant #14 integer: 1
			    constant #15 integer: 2
			    constant #16 utf8: "d"
			    constant #17 utf8: "Ljava/util/Map;"
			    constant #18 utf8: "Ljava/util/Map<+Ljava/lang/Comparable<LX;>;+Ljava/io/Serializable;>;"
			    constant #19 integer: 3
			    constant #20 utf8: "<init>"
			    constant #21 utf8: "()V"
			    constant #22 utf8: "Code"
			    constant #23 method_ref: #3.#24 java/lang/Object.<init> ()V
			    constant #24 name_and_type: #20.#21 <init> ()V
			    constant #25 utf8: "LineNumberTable"
			    constant #26 utf8: "LocalVariableTable"
			    constant #27 utf8: "this"
			    constant #28 utf8: "LX;"
			    constant #29 utf8: "SourceFile"
			    constant #30 utf8: "X.java"
			 \s
			  // Field descriptor #6 Ljava/util/List;
			  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;
			  java.util.List a;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @Immutable(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			 \s
			  // Field descriptor #6 Ljava/util/List;
			  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;
			  java.util.List b;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @Immutable(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), WILDCARD]
			      )
			 \s
			  // Field descriptor #6 Ljava/util/List;
			  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;
			  java.util.List c;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @Immutable(
			        #13 value=(int) 1 (constant type)
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #10 @Immutable(
			        #13 value=(int) 2 (constant type)
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), WILDCARD]
			      )
			 \s
			  // Field descriptor #17 Ljava/util/Map;
			  // Signature: Ljava/util/Map<+Ljava/lang/Comparable<LX;>;+Ljava/io/Serializable;>;
			  java.util.Map d;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @Immutable(
			        #13 value=(int) 1 (constant type)
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #10 @Immutable(
			        #13 value=(int) 2 (constant type)
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			      #10 @Immutable(
			        #13 value=(int) 3 (constant type)
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), WILDCARD]
			      )
			 \s
			  // Method descriptor #21 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [23]
			    4  return
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test012() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					class Document {}
					class X {
						@Readonly Document [][] docs1 = new @Readonly Document [2][12]; // array of arrays of read-only documents
						Document @Readonly [][] docs2 = new Document @Readonly [2][12]; // read-only array of arrays of documents
						Document[] @Readonly [] docs3 = new Document[2] @Readonly [12]; // array of read-only arrays of documents
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  Document[][] docs1;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @Readonly(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			 \s
			  // Field descriptor #6 [[LDocument;
			  Document[][] docs2;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @Readonly(
			        target type = 0x13 FIELD
			      )
			 \s
			  // Field descriptor #6 [[LDocument;
			  Document[][] docs3;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @Readonly(
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			 \s
			  // Method descriptor #12 ()V
			  // Stack: 3, Locals: 1
			  X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [14]
			     4  aload_0 [this]
			     5  iconst_2
			     6  bipush 12
			     8  multianewarray Document[][] [16]
			    12  putfield X.docs1 : Document[][] [17]
			    15  aload_0 [this]
			    16  iconst_2
			    17  bipush 12
			    19  multianewarray Document[][] [16]
			    23  putfield X.docs2 : Document[][] [19]
			    26  aload_0 [this]
			    27  iconst_2
			    28  bipush 12
			    30  multianewarray Document[][] [16]
			    34  putfield X.docs3 : Document[][] [21]
			    37  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 4, line: 7]
			        [pc: 15, line: 8]
			        [pc: 26, line: 9]
			        [pc: 37, line: 6]
			      Local variable table:
			        [pc: 0, pc: 38] local: this index: 0 type: X
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @Readonly(
			        target type = 0x44 NEW
			        offset = 8
			        location = [ARRAY, ARRAY]
			      )
			      #8 @Readonly(
			        target type = 0x44 NEW
			        offset = 19
			      )
			      #8 @Readonly(
			        target type = 0x44 NEW
			        offset = 30
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test013() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Immutable {}
					class Document {}
					class X {
						@Immutable X() {
						}
					}
					""",
		},
		"");
		// javac b100 gives:
		//		RuntimeInvisibleTypeAnnotations:
		//		      0: #9(): METHOD_RETURN
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<init>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #5.#6 <init> ()V
			    constant #10 utf8: "LineNumberTable"
			    constant #11 utf8: "LocalVariableTable"
			    constant #12 utf8: "this"
			    constant #13 utf8: "LX;"
			    constant #14 utf8: "RuntimeInvisibleTypeAnnotations"
			    constant #15 utf8: "LImmutable;"
			    constant #16 utf8: "SourceFile"
			    constant #17 utf8: "X.java"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [8]
			    4  return
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 4, line: 8]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			    RuntimeInvisibleTypeAnnotations:\s
			      #15 @Immutable(
			        target type = 0x14 METHOD_RETURN
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test014() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Immutable {}
					class Document {}
					interface I {
						void foo();
					}
					class X {
						void foo (X this, X this) {
						}
						static void foo (X this) {
						}
						I i = (X this) -> {};
					}
					class Y<T> {
						void foo(X this) {}
						void foo(Y this, int x) {}
						class Z {
							void foo(Y<T>.Z this) {}
						}
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				void foo (X this, X this) {
				                    ^^^^
			Only the first formal parameter may be declared explicitly as \'this\'
			----------
			2. ERROR in X.java (at line 12)
				static void foo (X this) {
				                   ^^^^
			Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors
			----------
			3. ERROR in X.java (at line 14)
				I i = (X this) -> {};
				      ^^^^^^^^^^^
			Lambda expression\'s signature does not match the signature of the functional interface method foo()
			----------
			4. ERROR in X.java (at line 14)
				I i = (X this) -> {};
				         ^^^^
			Lambda expressions cannot declare a this parameter
			----------
			5. ERROR in X.java (at line 17)
				void foo(X this) {}
				         ^
			The declared type of the explicit \'this\' parameter is expected to be Y<T>
			----------
			6. WARNING in X.java (at line 18)
				void foo(Y this, int x) {}
				         ^
			Y is a raw type. References to generic type Y<T> should be parameterized
			----------
			7. ERROR in X.java (at line 18)
				void foo(Y this, int x) {}
				         ^
			The declared type of the explicit \'this\' parameter is expected to be Y<T>
			----------
			""");
	}
	public void test015() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Immutable {}
					@Target(TYPE_USE)
					@interface Readonly {}
					class Document {}
					interface I {
						void foo();
					}
					class X {
						class Y {
							void foo(@Immutable X.@Readonly Y this) {
							}
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #21 @Immutable(
			        target type = 0x15 METHOD_RECEIVER
			      )
			      #22 @Readonly(
			        target type = 0x15 METHOD_RECEIVER
			        location = [INNER_TYPE]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test016() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface A {}
					@Target(TYPE_USE)
					@interface B {}
					@Target(TYPE_USE)
					@interface C {}
					public class X {}
					class Outer {
					    class Middle {
					        class Inner {
					            void innerMethod(@A Outer.@B Middle.@C Inner this) { }
					        }
					    }
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #21 @A(
			        target type = 0x15 METHOD_RECEIVER
			      )
			      #22 @B(
			        target type = 0x15 METHOD_RECEIVER
			        location = [INNER_TYPE]
			      )
			      #23 @C(
			        target type = 0x15 METHOD_RECEIVER
			        location = [INNER_TYPE, INNER_TYPE]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer$Middle$Inner.class", "Inner", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test017() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Result {}
					@Target(TYPE_USE)
					@interface Receiver {}
					class Document {}
					interface I {
						void foo();
					}
					class X {
						class Y {
							 Y(@Receiver X X.this, boolean b) { }
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #22 @Receiver(
			        target type = 0x15 METHOD_RECEIVER
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test018() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*; \s
					@interface Receiver {}
					class Document {}
					interface I {
						void foo();
					}
					class X {
						void foo(@Receiver X this) {}
						class Y {
							 Y(@Receiver X X.this, boolean b) { }
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					void foo(@Receiver X this) {}
					         ^^^^^^^^^
				Annotation types that do not specify explicit target element types cannot be applied here
				----------
				2. ERROR in X.java (at line 11)
					Y(@Receiver X X.this, boolean b) { }
					  ^^^^^^^^^
				Annotation types that do not specify explicit target element types cannot be applied here
				----------
				""");
	}
	public void test019() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					class X<@Readonly T> {
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @Readonly(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 0
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.@NotAllowed Date; // illegal!
					import @IllegalSyntax java.util.Date; // illegal syntax
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Even {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface NotAllowed {}
					@Target(TYPE_USE)
					@interface IllegalSyntax {}
					@Target(TYPE_USE)
					@interface Legal {}
					class X {
						static int staticField;
						static class StaticNestedClass {}
						void foo() {
							Object o = @Even int.class; // illegal!
							o = int @NonEmpty [].class; // illegal!
							int x = @IllegalSyntax X.staticField;
							StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;
							X.@Legal StaticNestedClass lsnc = (X.@Legal StaticNestedClass) null;
						}
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				import java.util.@NotAllowed Date; // illegal!
				                 ^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			2. ERROR in X.java (at line 2)
				import @IllegalSyntax java.util.Date; // illegal syntax
				       ^^^^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			3. WARNING in X.java (at line 2)
				import @IllegalSyntax java.util.Date; // illegal syntax
				                      ^^^^^^^^^^^^^^
			The import java.util.Date is never used
			----------
			4. ERROR in X.java (at line 19)
				Object o = @Even int.class; // illegal!
				           ^^^^^
			Syntax error, type annotations are illegal here
			----------
			5. ERROR in X.java (at line 20)
				o = int @NonEmpty [].class; // illegal!
				        ^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			6. ERROR in X.java (at line 21)
				int x = @IllegalSyntax X.staticField;
				        ^^^^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			7. ERROR in X.java (at line 22)
				StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;
				                         ^^^^^^^^^^^^^^
			Type annotations are not allowed on type names used to access static members
			----------
			""");
	}
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.@NotAllowed Date; // illegal!
					import @IllegalSyntax java.util.Date; // illegal syntax
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Even {}
					@Target(TYPE_USE)
					@interface NonEmpty {}
					@Target(TYPE_USE)
					@interface NotAllowed {}
					@Target(TYPE_USE)
					@interface IllegalSyntax {}
					@Target(TYPE_USE)
					@interface Legal {}
					interface I {
						int f(Y y);
					}
					class Y {
						int f;
						int x(Y y) {}
					}
					class X extends Y {
						static int staticField;
						static class StaticNestedClass {}
						void foo() {
							Object o = @Even int.class; // illegal!
							o = int @NonEmpty [].class; // illegal!
							int x = @IllegalSyntax X.staticField;
							StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;
							X.@Legal StaticNestedClass lsnc = (X.@Legal StaticNestedClass) null;
							int x2 = @IllegalSyntax X.super.f;
							I i = @IllegalSyntax X.super::x;
						}
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				import java.util.@NotAllowed Date; // illegal!
				                 ^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			2. ERROR in X.java (at line 2)
				import @IllegalSyntax java.util.Date; // illegal syntax
				       ^^^^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			3. WARNING in X.java (at line 2)
				import @IllegalSyntax java.util.Date; // illegal syntax
				                      ^^^^^^^^^^^^^^
			The import java.util.Date is never used
			----------
			4. ERROR in X.java (at line 26)
				Object o = @Even int.class; // illegal!
				           ^^^^^
			Syntax error, type annotations are illegal here
			----------
			5. ERROR in X.java (at line 27)
				o = int @NonEmpty [].class; // illegal!
				        ^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			6. ERROR in X.java (at line 28)
				int x = @IllegalSyntax X.staticField;
				        ^^^^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			7. ERROR in X.java (at line 29)
				StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;
				                         ^^^^^^^^^^^^^^
			Type annotations are not allowed on type names used to access static members
			----------
			8. ERROR in X.java (at line 31)
				int x2 = @IllegalSyntax X.super.f;
				         ^^^^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			9. ERROR in X.java (at line 32)
				I i = @IllegalSyntax X.super::x;
				      ^^^^^^^^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			""");
	}
	public void test022() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					class X {
						@Readonly int foo() @Readonly [] {
							return null;
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #17 @Readonly(
			        target type = 0x14 METHOD_RETURN
			        location = [ARRAY]
			      )
			      #17 @Readonly(
			        target type = 0x14 METHOD_RETURN
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test023() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					interface X {
						default @Readonly int foo() @Readonly [] {
							return null;
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #13 @Readonly(
			        target type = 0x14 METHOD_RETURN
			        location = [ARRAY]
			      )
			      #13 @Readonly(
			        target type = 0x14 METHOD_RETURN
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test024() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					@Target(TYPE_USE)
					@interface Critical {}
					class X {
						void foo() {
							try {
					           System.out.println();
							} catch (@Readonly NullPointerException | @Critical ArrayIndexOutOfBoundsException e) {
							}
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #34 @Readonly(
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 0
			      )
			      #35 @Critical(
			        target type = 0x42 EXCEPTION_PARAMETER
			        exception table index = 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test025() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					@Target(TYPE_USE)
					@interface Critical {}
					class X {
						void foo(@Readonly int [] [] @Critical ... x) {
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @Readonly(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			      #20 @Critical(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test026() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Readonly {}
					@Target(TYPE_USE)
					@interface Critical {}
					class X {
						void foo(@Readonly int [] [] @Critical ... x) {
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @Readonly(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			      #20 @Critical(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [ARRAY, ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test027() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					class X {
						@NonNull String var1, arr2[];
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @NonNull(
			        target type = 0x13 FIELD
			      )
			 \s
			  // Field descriptor #10 [Ljava/lang/String;
			  java.lang.String[] arr2;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @NonNull(
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test028() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.List;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface NonNull {}
					class X<@NonNull T> {
						<@NonNull K> void foo() {}
						List<@NonNull ?> l;
					}
					""",
		},
		"");
		// javac b100
		// On the type declaration:
		//  RuntimeInvisibleTypeAnnotations:
		//   0: #9(): CLASS_TYPE_PARAMETER, param_index=0
		// On the method:
	    //  RuntimeInvisibleTypeAnnotations:
	    //   0: #9(): METHOD_TYPE_PARAMETER, param_index=0
		// On the field:
		//  RuntimeInvisibleTypeAnnotations:
		//   0: #9(): FIELD, location=[TYPE_ARGUMENT(0)]
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			// Signature: <T:Ljava/lang/Object;>Ljava/lang/Object;
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "l"
			    constant #6 utf8: "Ljava/util/List;"
			    constant #7 utf8: "Signature"
			    constant #8 utf8: "Ljava/util/List<*>;"
			    constant #9 utf8: "RuntimeInvisibleTypeAnnotations"
			    constant #10 utf8: "LNonNull;"
			    constant #11 utf8: "<init>"
			    constant #12 utf8: "()V"
			    constant #13 utf8: "Code"
			    constant #14 method_ref: #3.#15 java/lang/Object.<init> ()V
			    constant #15 name_and_type: #11.#12 <init> ()V
			    constant #16 utf8: "LineNumberTable"
			    constant #17 utf8: "LocalVariableTable"
			    constant #18 utf8: "this"
			    constant #19 utf8: "LX;"
			    constant #20 utf8: "LocalVariableTypeTable"
			    constant #21 utf8: "LX<TT;>;"
			    constant #22 utf8: "foo"
			    constant #23 utf8: "<K:Ljava/lang/Object;>()V"
			    constant #24 utf8: "SourceFile"
			    constant #25 utf8: "X.java"
			    constant #26 utf8: "<T:Ljava/lang/Object;>Ljava/lang/Object;"
			 \s
			  // Field descriptor #6 Ljava/util/List;
			  // Signature: Ljava/util/List<*>;
			  java.util.List l;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @NonNull(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			 \s
			  // Method descriptor #12 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [14]
			    4  return
			      Line numbers:
			        [pc: 0, line: 6]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			      Local variable type table:
			        [pc: 0, pc: 5] local: this index: 0 type: X<T>
			 \s
			  // Method descriptor #12 ()V
			  // Signature: <K:Ljava/lang/Object;>()V
			  // Stack: 0, Locals: 1
			  void foo();
			    0  return
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			      Local variable type table:
			        [pc: 0, pc: 1] local: this index: 0 type: X<T>
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @NonNull(
			        target type = 0x1 METHOD_TYPE_PARAMETER
			        type parameter index = 0
			      )
			
			  RuntimeInvisibleTypeAnnotations:\s
			    #10 @NonNull(
			      target type = 0x0 CLASS_TYPE_PARAMETER
			      type parameter index = 0
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test029() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface TAnno { }
					@Target({METHOD}) @interface MAnno { }
					@Target({METHOD, TYPE_USE}) @interface MTAnno { }
					@Target({FIELD}) @interface FAnno { }
					@Target({FIELD, TYPE_USE}) @interface FTAnno { }
					class X {
					@FAnno Object field4; // legal, one field annotation
					@TAnno Object field5; // legal, one type annotation
					@FTAnno Object field6; // legal, one field annotation and one type annotation
					@FAnno java.lang.Object field7; // legal, one field annotation
					@TAnno java.lang.Object field8; // illegal
					@FTAnno java.lang.Object field9; // legal, one field annotation
					java.lang. @FAnno Object field10; // illegal
					java.lang. @TAnno Object field11; // legal, one type annotation
					java.lang. @FTAnno Object field12; // legal, one type annotation
					@MAnno void myMethod1() { } // legal, one method annotation
					@TAnno void myMethod2() { } // illegal
					@MTAnno void myMethod3() { } // legal, one method annotation
					@MAnno Object myMethod4() {  } // legal, one method annotation
					@TAnno Object myMethod5() { } // legal, one type annotation
					@MTAnno Object myMethod6() {  } // legal, one method annotation and one type annotation
					@MAnno java.lang.Object myMethod7() {  } // legal, one method annotation
					@TAnno java.lang.Object myMethod8() {  } // illegal
					@MTAnno java.lang.Object myMethod9() {  } // legal, one method annotation
					java.lang. @MAnno Object myMethod10() { } // illegal
					java.lang. @TAnno Object myMethod11() {  } // legal, one type annotation
					java.lang. @MTAnno Object myMethod12() {  } // legal, one type annotation
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				@TAnno java.lang.Object field8; // illegal
				^^^^^^
			Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
			----------
			2. ERROR in X.java (at line 15)
				java.lang. @FAnno Object field10; // illegal
				           ^^^^^^
			The annotation @FAnno is disallowed for this location
			----------
			3. ERROR in X.java (at line 19)
				@TAnno void myMethod2() { } // illegal
				^^^^^^
			Type annotation is illegal for a method that returns void
			----------
			4. ERROR in X.java (at line 25)
				@TAnno java.lang.Object myMethod8() {  } // illegal
				^^^^^^
			Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
			----------
			5. ERROR in X.java (at line 27)
				java.lang. @MAnno Object myMethod10() { } // illegal
				           ^^^^^^
			The annotation @MAnno is disallowed for this location
			----------
			""");
	}
	public void test030() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface TypeAnnotation { }
					@Target({TYPE}) @interface Annotation { }
					@Annotation @TypeAnnotation class X {
					}
					""",
		},
		"");
		// javac b100 produces:
		//		  RuntimeInvisibleAnnotations:
		//			    0: #11() LAnnotation;
		//			    1: #12() LTypeAnnotation;
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<init>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #5.#6 <init> ()V
			    constant #10 utf8: "LineNumberTable"
			    constant #11 utf8: "LocalVariableTable"
			    constant #12 utf8: "this"
			    constant #13 utf8: "LX;"
			    constant #14 utf8: "SourceFile"
			    constant #15 utf8: "X.java"
			    constant #16 utf8: "RuntimeInvisibleAnnotations"
			    constant #17 utf8: "LAnnotation;"
			    constant #18 utf8: "LTypeAnnotation;"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [8]
			    4  return
			      Line numbers:
			        [pc: 0, line: 5]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			
			  RuntimeInvisibleAnnotations:\s
			    #17 @Annotation(
			    )
			    #18 @TypeAnnotation(
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test030a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Retention(RetentionPolicy.RUNTIME)
					@Target({TYPE_USE}) @interface TypeAnnotation { }
					@Retention(RetentionPolicy.RUNTIME)
					@Target({TYPE}) @interface Annotation { }
					@Annotation @TypeAnnotation class X {
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<init>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #5.#6 <init> ()V
			    constant #10 utf8: "LineNumberTable"
			    constant #11 utf8: "LocalVariableTable"
			    constant #12 utf8: "this"
			    constant #13 utf8: "LX;"
			    constant #14 utf8: "SourceFile"
			    constant #15 utf8: "X.java"
			    constant #16 utf8: "RuntimeVisibleAnnotations"
			    constant #17 utf8: "LAnnotation;"
			    constant #18 utf8: "LTypeAnnotation;"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [8]
			    4  return
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			
			  RuntimeVisibleAnnotations:\s
			    #17 @Annotation(
			    )
			    #18 @TypeAnnotation(
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test030b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Retention(RetentionPolicy.RUNTIME)
					@Target({TYPE_PARAMETER}) @interface TypeAnnotation { }
					@Retention(RetentionPolicy.RUNTIME)
					@Target({TYPE}) @interface Annotation { }
					@Annotation @TypeAnnotation class X {
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				@Annotation @TypeAnnotation class X {
				            ^^^^^^^^^^^^^^^
			The annotation @TypeAnnotation is disallowed for this location
			----------
			""");
	}
	public void test030c() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Retention(RetentionPolicy.RUNTIME)
					@Target({TYPE_USE,TYPE_PARAMETER}) @interface TypeAnnotation { }
					@Retention(RetentionPolicy.RUNTIME)
					@Target({TYPE}) @interface Annotation { }
					@Annotation @TypeAnnotation class X {
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<init>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #5.#6 <init> ()V
			    constant #10 utf8: "LineNumberTable"
			    constant #11 utf8: "LocalVariableTable"
			    constant #12 utf8: "this"
			    constant #13 utf8: "LX;"
			    constant #14 utf8: "SourceFile"
			    constant #15 utf8: "X.java"
			    constant #16 utf8: "RuntimeVisibleAnnotations"
			    constant #17 utf8: "LAnnotation;"
			    constant #18 utf8: "LTypeAnnotation;"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [8]
			    4  return
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			
			  RuntimeVisibleAnnotations:\s
			    #17 @Annotation(
			    )
			    #18 @TypeAnnotation(
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that annotations in initializer code are not attached to the field.
	public void test031() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					class X {
						X x = new @NonNull X();
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [10]
			     4  aload_0 [this]
			     5  new X [1]
			     8  dup
			     9  invokespecial X() [12]
			    12  putfield X.x : X [13]
			    15  return
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 4, line: 5]
			        [pc: 15, line: 4]
			      Local variable table:
			        [pc: 0, pc: 16] local: this index: 0 type: X
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @NonNull(
			        target type = 0x44 NEW
			        offset = 5
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// Test co-existence of parameter annotations and type annotations.
	public void test032() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					@Target({PARAMETER}) @interface ParameterAnnot { }
					class X {
						void foo(@NonNull X this, @ParameterAnnot @NonNull X x) {
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  void foo(X x);
			    0  return
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: x index: 1 type: X
			    RuntimeInvisibleParameterAnnotations:\s
			      Number of annotations for parameter 0: 1
			        #17 @ParameterAnnot(
			        )
			    RuntimeInvisibleTypeAnnotations:\s
			      #20 @NonNull(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #20 @NonNull(
			        target type = 0x15 METHOD_RECEIVER
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// Test type annotations in initializer code.
	public void test033() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					class X {
						static {
							Object o = (@NonNull Object) new @NonNull Object();
						}
						{
							new @NonNull Object();
						}
						X() {
						}
						X (int x) {
						}
					}
					""",
		},
		"");
		// javac b100
		// For the annotations in the static {...} the clinit has:
		//		RuntimeInvisibleTypeAnnotations:
		//	        0: #11(): CAST, offset=0, type_index=0
		//	        1: #11(): NEW, offset=0
		// javac is skipping production of the cast so offset is 0. JDT is currently always producing the
		// checkcast for an annotated cast so the offset is 7.

		// For the annotations in the initializer {...} the constructors both have:
		//	      RuntimeInvisibleTypeAnnotations:
		//	          0: #11(): NEW, offset=4

		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<clinit>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #10.#6 <init> ()V
			    constant #10 utf8: "<init>"
			    constant #11 utf8: "LineNumberTable"
			    constant #12 utf8: "LocalVariableTable"
			    constant #13 utf8: "RuntimeInvisibleTypeAnnotations"
			    constant #14 utf8: "LNonNull;"
			    constant #15 utf8: "this"
			    constant #16 utf8: "LX;"
			    constant #17 utf8: "(I)V"
			    constant #18 utf8: "x"
			    constant #19 utf8: "I"
			    constant #20 utf8: "SourceFile"
			    constant #21 utf8: "X.java"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static {};
			     0  new java.lang.Object [3]
			     3  dup
			     4  invokespecial java.lang.Object() [8]
			     7  checkcast java.lang.Object [3]
			    10  astore_0
			    11  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 11, line: 7]
			    RuntimeInvisibleTypeAnnotations:\s
			      #14 @NonNull(
			        target type = 0x44 NEW
			        offset = 0
			      )
			      #14 @NonNull(
			        target type = 0x47 CAST
			        offset = 7
			        type argument index = 0
			      )
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [8]
			     4  new java.lang.Object [3]
			     7  invokespecial java.lang.Object() [8]
			    10  return
			      Line numbers:
			        [pc: 0, line: 11]
			        [pc: 4, line: 9]
			        [pc: 10, line: 12]
			      Local variable table:
			        [pc: 0, pc: 11] local: this index: 0 type: X
			    RuntimeInvisibleTypeAnnotations:\s
			      #14 @NonNull(
			        target type = 0x44 NEW
			        offset = 4
			      )
			 \s
			  // Method descriptor #17 (I)V
			  // Stack: 1, Locals: 2
			  X(int x);
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [8]
			     4  new java.lang.Object [3]
			     7  invokespecial java.lang.Object() [8]
			    10  return
			      Line numbers:
			        [pc: 0, line: 13]
			        [pc: 4, line: 9]
			        [pc: 10, line: 14]
			      Local variable table:
			        [pc: 0, pc: 11] local: this index: 0 type: X
			        [pc: 0, pc: 11] local: x index: 1 type: int
			    RuntimeInvisibleTypeAnnotations:\s
			      #14 @NonNull(
			        target type = 0x44 NEW
			        offset = 4
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test034() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					class X <T extends @NonNull Comparable> {
					}
					""",
		},
		"");
		// javac b100
		//		  RuntimeInvisibleTypeAnnotations:
		//			    0: #13(): CLASS_TYPE_PARAMETER_BOUND, param_index=0, bound_index=1
		// bound_index is 1 because the bound is an interface, not a class
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			// Signature: <T::Ljava/lang/Comparable;>Ljava/lang/Object;
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<init>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #5.#6 <init> ()V
			    constant #10 utf8: "LineNumberTable"
			    constant #11 utf8: "LocalVariableTable"
			    constant #12 utf8: "this"
			    constant #13 utf8: "LX;"
			    constant #14 utf8: "LocalVariableTypeTable"
			    constant #15 utf8: "LX<TT;>;"
			    constant #16 utf8: "SourceFile"
			    constant #17 utf8: "X.java"
			    constant #18 utf8: "Signature"
			    constant #19 utf8: "<T::Ljava/lang/Comparable;>Ljava/lang/Object;"
			    constant #20 utf8: "RuntimeInvisibleTypeAnnotations"
			    constant #21 utf8: "LNonNull;"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [8]
			    4  return
			      Line numbers:
			        [pc: 0, line: 4]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			      Local variable type table:
			        [pc: 0, pc: 5] local: this index: 0 type: X<T>
			
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @NonNull(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 1
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// Bug 415543 - Incorrect bound index in RuntimeInvisibleTypeAnnotations attribute
	public void test034b() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.io.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					
					class X <T extends Comparable & @NonNull Serializable> {
					  <T extends @NonNull Comparable> void one(T t) {}
					  <T extends Comparable & @NonNull Serializable> void two(T t) {}
					  <T extends @NonNull Comparable & @NonNull Serializable> void three(T t) {}
					  <T extends Object & @NonNull Serializable> void four(T t) {}
					  <T extends Object & @NonNull Serializable & @NonNull Runnable> void five(T t) {}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			// Compiled from X.java (version 1.8 : 52.0, super bit)
			// Signature: <T::Ljava/lang/Comparable;:Ljava/io/Serializable;>Ljava/lang/Object;
			class X {
			  Constant pool:
			    constant #1 class: #2 X
			    constant #2 utf8: "X"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "<init>"
			    constant #6 utf8: "()V"
			    constant #7 utf8: "Code"
			    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V
			    constant #9 name_and_type: #5.#6 <init> ()V
			    constant #10 utf8: "LineNumberTable"
			    constant #11 utf8: "LocalVariableTable"
			    constant #12 utf8: "this"
			    constant #13 utf8: "LX;"
			    constant #14 utf8: "LocalVariableTypeTable"
			    constant #15 utf8: "LX<TT;>;"
			    constant #16 utf8: "one"
			    constant #17 utf8: "(Ljava/lang/Comparable;)V"
			    constant #18 utf8: "Signature"
			    constant #19 utf8: "<T::Ljava/lang/Comparable;>(TT;)V"
			    constant #20 utf8: "t"
			    constant #21 utf8: "Ljava/lang/Comparable;"
			    constant #22 utf8: "TT;"
			    constant #23 utf8: "RuntimeInvisibleTypeAnnotations"
			    constant #24 utf8: "LNonNull;"
			    constant #25 utf8: "two"
			    constant #26 utf8: "<T::Ljava/lang/Comparable;:Ljava/io/Serializable;>(TT;)V"
			    constant #27 utf8: "three"
			    constant #28 utf8: "four"
			    constant #29 utf8: "(Ljava/lang/Object;)V"
			    constant #30 utf8: "<T:Ljava/lang/Object;:Ljava/io/Serializable;>(TT;)V"
			    constant #31 utf8: "Ljava/lang/Object;"
			    constant #32 utf8: "five"
			    constant #33 utf8: "<T:Ljava/lang/Object;:Ljava/io/Serializable;:Ljava/lang/Runnable;>(TT;)V"
			    constant #34 utf8: "SourceFile"
			    constant #35 utf8: "X.java"
			    constant #36 utf8: "<T::Ljava/lang/Comparable;:Ljava/io/Serializable;>Ljava/lang/Object;"
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [8]
			    4  return
			      Line numbers:
			        [pc: 0, line: 6]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			      Local variable type table:
			        [pc: 0, pc: 5] local: this index: 0 type: X<T>
			 \s
			  // Method descriptor #17 (Ljava/lang/Comparable;)V
			  // Signature: <T::Ljava/lang/Comparable;>(TT;)V
			  // Stack: 0, Locals: 2
			  void one(java.lang.Comparable t);
			    0  return
			      Line numbers:
			        [pc: 0, line: 7]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Comparable
			      Local variable type table:
			        [pc: 0, pc: 1] local: this index: 0 type: X<T>
			        [pc: 0, pc: 1] local: t index: 1 type: T
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 1
			      )
			 \s
			  // Method descriptor #17 (Ljava/lang/Comparable;)V
			  // Signature: <T::Ljava/lang/Comparable;:Ljava/io/Serializable;>(TT;)V
			  // Stack: 0, Locals: 2
			  void two(java.lang.Comparable t);
			    0  return
			      Line numbers:
			        [pc: 0, line: 8]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Comparable
			      Local variable type table:
			        [pc: 0, pc: 1] local: this index: 0 type: X<T>
			        [pc: 0, pc: 1] local: t index: 1 type: T
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 2
			      )
			 \s
			  // Method descriptor #17 (Ljava/lang/Comparable;)V
			  // Signature: <T::Ljava/lang/Comparable;:Ljava/io/Serializable;>(TT;)V
			  // Stack: 0, Locals: 2
			  void three(java.lang.Comparable t);
			    0  return
			      Line numbers:
			        [pc: 0, line: 9]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Comparable
			      Local variable type table:
			        [pc: 0, pc: 1] local: this index: 0 type: X<T>
			        [pc: 0, pc: 1] local: t index: 1 type: T
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 1
			      )
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 2
			      )
			 \s
			  // Method descriptor #29 (Ljava/lang/Object;)V
			  // Signature: <T:Ljava/lang/Object;:Ljava/io/Serializable;>(TT;)V
			  // Stack: 0, Locals: 2
			  void four(java.lang.Object t);
			    0  return
			      Line numbers:
			        [pc: 0, line: 10]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Object
			      Local variable type table:
			        [pc: 0, pc: 1] local: this index: 0 type: X<T>
			        [pc: 0, pc: 1] local: t index: 1 type: T
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 1
			      )
			 \s
			  // Method descriptor #29 (Ljava/lang/Object;)V
			  // Signature: <T:Ljava/lang/Object;:Ljava/io/Serializable;:Ljava/lang/Runnable;>(TT;)V
			  // Stack: 0, Locals: 2
			  void five(java.lang.Object t);
			    0  return
			      Line numbers:
			        [pc: 0, line: 11]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Object
			      Local variable type table:
			        [pc: 0, pc: 1] local: this index: 0 type: X<T>
			        [pc: 0, pc: 1] local: t index: 1 type: T
			    RuntimeInvisibleTypeAnnotations:\s
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 1
			      )
			      #24 @NonNull(
			        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND
			        type parameter index = 0 type parameter bound index = 2
			      )
			
			  RuntimeInvisibleTypeAnnotations:\s
			    #24 @NonNull(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 2
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test035() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					
					class X {
						void foo() {
							@NonNull X [] x = new X[10];
							System.out.println(x);
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  void foo();
			     0  bipush 10
			     2  anewarray X [1]
			     5  astore_1 [x]
			     6  getstatic java.lang.System.out : java.io.PrintStream [15]
			     9  aload_1 [x]
			    10  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [21]
			    13  return
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 6, line: 8]
			        [pc: 13, line: 9]
			      Local variable table:
			        [pc: 0, pc: 14] local: this index: 0 type: X
			        [pc: 6, pc: 14] local: x index: 1 type: X[]
			    RuntimeInvisibleTypeAnnotations:\s
			      #30 @NonNull(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 6, pc: 14] index: 1
			        location = [ARRAY]
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// test that parameter index does not include explicit this parameter.
	public void test036() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;\s
					@Target({TYPE_USE}) @interface NonNull { }
					class X  {
						void foo(@NonNull X this, @NonNull X x) {
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  void foo(X x);
			    0  return
			      Line numbers:
			        [pc: 0, line: 6]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: x index: 1 type: X
			    RuntimeInvisibleTypeAnnotations:\s
			      #18 @NonNull(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #18 @NonNull(
			        target type = 0x15 METHOD_RECEIVER
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test037() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Readonly {
						String value() default "default";
					}
					public class X {
						X [] x = new @Readonly X @Readonly [10];
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  public X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [10]
			     4  aload_0 [this]
			     5  bipush 10
			     7  anewarray X [1]
			    10  putfield X.x : X[] [12]
			    13  return
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 4, line: 8]
			        [pc: 13, line: 7]
			      Local variable table:
			        [pc: 0, pc: 14] local: this index: 0 type: X
			    RuntimeVisibleTypeAnnotations:\s
			      #19 @Readonly(
			        target type = 0x44 NEW
			        offset = 7
			        location = [ARRAY]
			      )
			      #19 @Readonly(
			        target type = 0x44 NEW
			        offset = 7
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// test anonymous class, the class itself should have class_extends target ?
	public void test038() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Readonly {
						String value() default "default";
					}
					public class X {
						X x = new @Readonly X() {
						};
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  public X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [10]
			     4  aload_0 [this]
			     5  new X$1 [12]
			     8  dup
			     9  aload_0 [this]
			    10  invokespecial X$1(X) [14]
			    13  putfield X.x : X [17]
			    16  return
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 4, line: 8]
			        [pc: 16, line: 7]
			      Local variable table:
			        [pc: 0, pc: 17] local: this index: 0 type: X
			    RuntimeVisibleTypeAnnotations:\s
			      #23 @Readonly(
			        target type = 0x44 NEW
			        offset = 5
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test039() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.List;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Readonly {
						String value() default "default";
					}
					public class X  {\s
						void foo(List<@Readonly ?> l) {
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  void foo(java.util.List l);
			    0  return
			      Line numbers:
			        [pc: 0, line: 10]
			      Local variable table:
			        [pc: 0, pc: 1] local: this index: 0 type: X
			        [pc: 0, pc: 1] local: l index: 1 type: java.util.List
			      Local variable type table:
			        [pc: 0, pc: 1] local: l index: 1 type: java.util.List<?>
			    RuntimeVisibleTypeAnnotations:\s
			      #23 @Readonly(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [TYPE_ARGUMENT(0)]
			      )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test040() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Readonly {
						String value() default "default";
					}
					class X {
						class Y {}
						void foo() {
							@Readonly X x = new X();
							x.new @Readonly Y();
						}
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			    RuntimeVisibleTypeAnnotations:\s
			      #27 @Readonly(
			        target type = 0x44 NEW
			        offset = 8
			        location = [INNER_TYPE]
			      )
			      #27 @Readonly(
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 8, pc: 21] index: 1
			      )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test041() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@interface A {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface B {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface C {
						String value() default "default";
					}
					class X<T extends @A Object & @B Comparable, U extends @C Cloneable> {
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @A(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 0
			    )
			    #22 @B(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 0 type parameter bound index = 1
			    )
			    #23 @C(
			      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND
			      type parameter index = 1 type parameter bound index = 1
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type path tests.
	public void test042() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.Map;
					import java.util.List;
					@Target(ElementType.TYPE_USE)
					@interface A {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface B {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface C {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface D {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface E {
						String value() default "default";
					}
					class X {
						@A Map <@B ? extends @C String, @D List<@E Object>> f;
					}
					""",
		},
		"");
		String expectedOutput =
				"""
			  java.util.Map f;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @A(
			        target type = 0x13 FIELD
			      )
			      #11 @B(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #12 @C(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), WILDCARD]
			      )
			      #13 @D(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			      #14 @E(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// Bug 414384 - [1.8] type annotation on abbreviated inner class is not marked as inner type
	public void test043() throws Exception {
		this.runConformTest(
			new String[] {
				"pkg/Clazz.java",
				"""
					package pkg;
					import java.lang.annotation.*;
					import static java.lang.annotation.ElementType.*;
					
					@Target({TYPE_USE}) @interface P { }
					@Target({TYPE_USE}) @interface O { }
					@Target({TYPE_USE}) @interface I { }
					
					public abstract class Clazz {
					  public class Inner {}
					  public abstract void n1(@I Inner i1);
					  public abstract void n2(@O Clazz.@I Inner i2);
					  public abstract void n3(pkg.@O Clazz.@I Inner i3);
					}
					""",
		},
		"");
		// javac b100 produces for the methods:
		//		  public abstract void n1(pkg.Clazz$Inner);
		//		    RuntimeInvisibleTypeAnnotations:
		//		      0: #14(): METHOD_FORMAL_PARAMETER, param_index=0, location=[INNER_TYPE]
		//
		//		  public abstract void n2(pkg.Clazz$Inner);
		//		    RuntimeInvisibleTypeAnnotations:
		//		      0: #14(): METHOD_FORMAL_PARAMETER, param_index=0, location=[INNER_TYPE]
		//		      1: #16(): METHOD_FORMAL_PARAMETER, param_index=0
		//
		//		  public abstract void n3(pkg.Clazz$Inner);
		//		    RuntimeInvisibleTypeAnnotations:
		//		      0: #14(): METHOD_FORMAL_PARAMETER, param_index=0, location=[INNER_TYPE]
		//		      1: #16(): METHOD_FORMAL_PARAMETER, param_index=0
		String expectedOutput =
				"""
			  // Method descriptor #15 (Lpkg/Clazz$Inner;)V
			  public abstract void n1(pkg.Clazz.Inner arg0);
			    RuntimeInvisibleTypeAnnotations:\s
			      #17 @pkg.I(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [INNER_TYPE]
			      )
			 \s
			  // Method descriptor #15 (Lpkg/Clazz$Inner;)V
			  public abstract void n2(pkg.Clazz.Inner arg0);
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @pkg.O(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #17 @pkg.I(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [INNER_TYPE]
			      )
			 \s
			  // Method descriptor #15 (Lpkg/Clazz$Inner;)V
			  public abstract void n3(pkg.Clazz.Inner arg0);
			    RuntimeInvisibleTypeAnnotations:\s
			      #19 @pkg.O(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #17 @pkg.I(
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [INNER_TYPE]
			      )
			
			  Inner classes:
			    [inner class info: #24 pkg/Clazz$Inner, outer class info: #1 pkg/Clazz
			     inner name: #26 Inner, accessflags: 1 public]
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "pkg" + File.separator + "Clazz.class", "pkg.Clazz", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test044() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@interface I {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface F {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface G {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface H {
						String value() default "default";
					}
					class X {
						@I String @F [] @G [] @H [] f;
					}
					""",
		},
		"");

		String expectedOutput =
				"""
			  // Field descriptor #6 [[[Ljava/lang/String;
			  java.lang.String[][][] f;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @I(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY, ARRAY]
			      )
			      #9 @F(
			        target type = 0x13 FIELD
			      )
			      #10 @G(
			        target type = 0x13 FIELD
			        location = [ARRAY]
			      )
			      #11 @H(
			        target type = 0x13 FIELD
			        location = [ARRAY, ARRAY]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test045() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@interface M {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface L {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface K {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface J {
						String value() default "default";
					}
					class O1 {
						class O2 {
							class O3 {
								class Nested {}
							}
						}
					}
					class X {
						@M O1.@L O2.@K O3.@J Nested f = null;
					}
					""",
		},
		"");

		String expectedOutput =
				"""
			  // Field descriptor #6 LO1$O2$O3$Nested;
			  O1$O2$O3$Nested f;
			    RuntimeInvisibleTypeAnnotations:\s
			      #8 @M(
			        target type = 0x13 FIELD
			      )
			      #9 @L(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE]
			      )
			      #10 @K(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, INNER_TYPE]
			      )
			      #11 @J(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test046() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.Map;
					import java.util.List;
					@Target(ElementType.TYPE_USE)
					@interface A {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface B {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface C {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface D {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface E {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface F {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface G {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface H {
						String value() default "default";
					}
					class Document {}
					class X {
						@A Map<@B Comparable<@F Object @C [] @D [] @E[]>, @G List<@H Document>> f;
					}
					""",
		},
		"");

		String expectedOutput =
				"""
			  java.util.Map f;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @A(
			        target type = 0x13 FIELD
			      )
			      #11 @B(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0)]
			      )
			      #12 @F(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]
			      )
			      #13 @C(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]
			      )
			      #14 @D(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY]
			      )
			      #15 @E(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]
			      )
			      #16 @G(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1)]
			      )
			      #17 @H(
			        target type = 0x13 FIELD
			        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test047() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@interface A {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface B {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface C {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface D {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface E {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface F {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface G {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface H {
						String value() default "default";
					}
					class O1 {
						class O2<S, T> {
							class O3 {
								class Nested<K, V> {
								}
							}
						}
					}
						class S {}
						class T {}
						class U {}
						class V {}
					class X {
						@H O1.@E O2<@F S, @G T>.@D O3.@A Nested<@B U, @C V> f;
					}
					""",
		},
		"");

		String expectedOutput =
			"""
			  // Field descriptor #6 LO1$O2$O3$Nested;
			  // Signature: LO1$O2<LS;LT;>.O3.Nested<LU;LV;>;
			  O1$O2$O3$Nested f;
			    RuntimeInvisibleTypeAnnotations:\s
			      #10 @H(
			        target type = 0x13 FIELD
			      )
			      #11 @E(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE]
			      )
			      #12 @D(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, INNER_TYPE]
			      )
			      #13 @A(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]
			      )
			      #14 @F(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, TYPE_ARGUMENT(0)]
			      )
			      #15 @G(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, TYPE_ARGUMENT(1)]
			      )
			      #16 @B(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]
			      )
			      #17 @C(
			        target type = 0x13 FIELD
			        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(1)]
			      )
			 \s
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test048() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.List;
					import java.util.Map;
					@Target(ElementType.TYPE_USE)
					@interface Readonly {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface NonNull {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface NonEmpty {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface D {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface E {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface F {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface G {
						String value() default "default";
					}
					@Target(ElementType.TYPE_USE)
					@interface H {
						String value() default "default";
					}
					
					abstract class X implements @Readonly Map<@NonNull String, @NonEmpty List<@NonNull @Readonly String>> {}
					""",
		},
		"");

		String expectedOutput =
				"""
			  RuntimeInvisibleTypeAnnotations:\s
			    #21 @Readonly(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			    )
			    #22 @NonNull(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(0)]
			    )
			    #23 @NonEmpty(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(1)]
			    )
			    #22 @NonNull(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]
			    )
			    #21 @Readonly(
			      target type = 0x10 CLASS_EXTENDS
			      type index = 0
			      location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]
			    )
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test049() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface B { int value() default -1; }
					public class X {
					    class Y {
					    }
					    @B(1) X. @B(2) Y xy;
					    void foo(@B(3) X. @B(4) Y xy) {
					        @B(5) X. @B(6) Y local = null;\s
					    }
					}
					""",
		},
		"");

		String expectedOutput =
				"""
			  // Field descriptor #6 LX$Y;
			  X$Y xy;
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @B(
			        #9 value=(int) 1 (constant type)
			        target type = 0x13 FIELD
			      )
			      #8 @B(
			        #9 value=(int) 2 (constant type)
			        target type = 0x13 FIELD
			        location = [INNER_TYPE]
			      )
			 \s
			  // Method descriptor #13 ()V
			  // Stack: 1, Locals: 1
			  public X();
			    0  aload_0 [this]
			    1  invokespecial java.lang.Object() [15]
			    4  return
			      Line numbers:
			        [pc: 0, line: 5]
			      Local variable table:
			        [pc: 0, pc: 5] local: this index: 0 type: X
			 \s
			  // Method descriptor #22 (LX$Y;)V
			  // Stack: 1, Locals: 3
			  void foo(X.Y xy);
			    0  aconst_null
			    1  astore_2 [local]
			    2  return
			      Line numbers:
			        [pc: 0, line: 10]
			        [pc: 2, line: 11]
			      Local variable table:
			        [pc: 0, pc: 3] local: this index: 0 type: X
			        [pc: 0, pc: 3] local: xy index: 1 type: X.Y
			        [pc: 2, pc: 3] local: local index: 2 type: X.Y
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @B(
			        #9 value=(int) 5 (constant type)
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 2, pc: 3] index: 2
			      )
			      #8 @B(
			        #9 value=(int) 6 (constant type)
			        target type = 0x40 LOCAL_VARIABLE
			        local variable entries:
			          [pc: 2, pc: 3] index: 2
			        location = [INNER_TYPE]
			      )
			    RuntimeVisibleTypeAnnotations:\s
			      #8 @B(
			        #9 value=(int) 3 (constant type)
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			      )
			      #8 @B(
			        #9 value=(int) 4 (constant type)
			        target type = 0x16 METHOD_FORMAL_PARAMETER
			        method parameter index = 0
			        location = [INNER_TYPE]
			      )
			
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
}
