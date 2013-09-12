/*******************************************************************************
 * Copyright (c) 2013 Jesper Steen Moller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation
 *            Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;

public class Jsr335ClassFileTest extends AbstractComparableTest {

public Jsr335ClassFileTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.8
 */
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test055" };
//	TESTS_NUMBERS = new int[] { 50, 51, 52, 53 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
private void verifyClassFile(String expectedOutput, String classFileName, int mode) throws IOException,
	ClassFormatException {
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
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.run();\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Runnable referenceExpression = Thread::yield;\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
		"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
		"public class X {\n" + 
		"  Constant pool:\n" + 
		"    constant #1 class: #2 X\n" + 
		"    constant #2 utf8: \"X\"\n" + 
		"    constant #3 class: #4 java/lang/Object\n" + 
		"    constant #4 utf8: \"java/lang/Object\"\n" + 
		"    constant #5 utf8: \"referenceExpression\"\n" + 
		"    constant #6 utf8: \"Ljava/lang/Runnable;\"\n" + 
		"    constant #7 utf8: \"<init>\"\n" + 
		"    constant #8 utf8: \"()V\"\n" + 
		"    constant #9 utf8: \"Code\"\n" + 
		"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" + 
		"    constant #11 name_and_type: #7.#8 <init> ()V\n" + 
		"    constant #12 name_and_type: #13.#14 run ()Ljava/lang/Runnable;\n" + 
		"    constant #13 utf8: \"run\"\n" + 
		"    constant #14 utf8: \"()Ljava/lang/Runnable;\"\n" + 
		"    constant #15 invoke dynamic: #0 #12 run ()Ljava/lang/Runnable;\n" + 
		"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" + 
		"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" + 
		"    constant #18 utf8: \"LineNumberTable\"\n" + 
		"    constant #19 utf8: \"LocalVariableTable\"\n" + 
		"    constant #20 utf8: \"this\"\n" + 
		"    constant #21 utf8: \"LX;\"\n" + 
		"    constant #22 utf8: \"SourceFile\"\n" + 
		"    constant #23 utf8: \"X.java\"\n" + 
		"    constant #24 method_ref: #25.#27 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
		"    constant #25 class: #26 java/lang/invoke/LambdaMetafactory\n" + 
		"    constant #26 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
		"    constant #27 name_and_type: #28.#29 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
		"    constant #28 utf8: \"metafactory\"\n" + 
		"    constant #29 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
		"    constant #30 method handle: invokestatic (6) #24 \n" + 
		"    constant #31 utf8: \"BootstrapMethods\"\n" + 
		"    constant #32 method type: #8 ()V\n" + 
		"    constant #33 method_ref: #34.#36 java/lang/Thread.yield ()V\n" + 
		"    constant #34 class: #35 java/lang/Thread\n" + 
		"    constant #35 utf8: \"java/lang/Thread\"\n" + 
		"    constant #36 name_and_type: #37.#8 yield ()V\n" + 
		"    constant #37 utf8: \"yield\"\n" + 
		"    constant #38 method handle: invokestatic (6) #33 \n" + 
		"    constant #39 method type: #8 ()V\n" + 
		"    constant #40 utf8: \"InnerClasses\"\n" + 
		"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" + 
		"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
		"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" + 
		"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
		"    constant #45 utf8: \"Lookup\"\n" + 
		"  \n" + 
		"  // Field descriptor #6 Ljava/lang/Runnable;\n" + 
		"  public java.lang.Runnable referenceExpression;\n" + 
		"  \n" + 
		"  // Method descriptor #8 ()V\n" + 
		"  // Stack: 2, Locals: 1\n" + 
		"  public X();\n" + 
		"     0  aload_0 [this]\n" + 
		"     1  invokespecial java.lang.Object() [10]\n" + 
		"     4  aload_0 [this]\n" + 
		"     5  invokedynamic 0 run() : java.lang.Runnable [15]\n" + 
		"    10  putfield X.referenceExpression : java.lang.Runnable [16]\n" + 
		"    13  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 1]\n" + 
		"        [pc: 4, line: 2]\n" + 
		"        [pc: 13, line: 1]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
		"\n" + 
		"  Inner classes:\n" + 
		"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" + 
		"     inner name: #45 Lookup, accessflags: 25 public static final]\n" + 
		"Bootstrap methods:\n" + 
		"  0 : # 30 arguments: {#32,#38,#39}\n" + 
		"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.consume(\"SUCCESS\");\n" +
			"    }\n" +
			"    public static void printIt(Object o) {\n" +
			"        System.out.println(o.toString());\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public ObjectConsumer referenceExpression = Main::printIt;\n" +
			"}\n",
			"ObjectConsumer.java",
			"public interface ObjectConsumer {\n" +
			"    void consume(Object obj);\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"referenceExpression\"\n" + 
			"    constant #6 utf8: \"LObjectConsumer;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"()V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" + 
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" + 
			"    constant #12 name_and_type: #13.#14 consume ()LObjectConsumer;\n" + 
			"    constant #13 utf8: \"consume\"\n" + 
			"    constant #14 utf8: \"()LObjectConsumer;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 consume ()LObjectConsumer;\n" + 
			"    constant #16 field_ref: #1.#17 X.referenceExpression LObjectConsumer;\n" + 
			"    constant #17 name_and_type: #5.#6 referenceExpression LObjectConsumer;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"this\"\n" + 
			"    constant #21 utf8: \"LX;\"\n" + 
			"    constant #22 utf8: \"SourceFile\"\n" + 
			"    constant #23 utf8: \"X.java\"\n" + 
			"    constant #24 method_ref: #25.#27 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #25 class: #26 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #26 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #27 name_and_type: #28.#29 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #28 utf8: \"metafactory\"\n" + 
			"    constant #29 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #30 method handle: invokestatic (6) #24 \n" + 
			"    constant #31 utf8: \"BootstrapMethods\"\n" + 
			"    constant #32 utf8: \"(Ljava/lang/Object;)V\"\n" + 
			"    constant #33 method type: #32 (Ljava/lang/Object;)V\n" + 
			"    constant #34 method_ref: #35.#37 Main.printIt (Ljava/lang/Object;)V\n" + 
			"    constant #35 class: #36 Main\n" + 
			"    constant #36 utf8: \"Main\"\n" + 
			"    constant #37 name_and_type: #38.#32 printIt (Ljava/lang/Object;)V\n" + 
			"    constant #38 utf8: \"printIt\"\n" + 
			"    constant #39 method handle: invokestatic (6) #34 \n" + 
			"    constant #40 method type: #32 (Ljava/lang/Object;)V\n" + 
			"    constant #41 utf8: \"InnerClasses\"\n" + 
			"    constant #42 class: #43 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #43 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #44 class: #45 java/lang/invoke/MethodHandles\n" + 
			"    constant #45 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #46 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LObjectConsumer;\n" + 
			"  public ObjectConsumer referenceExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [10]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 consume() : ObjectConsumer [15]\n" + 
			"    10  putfield X.referenceExpression : ObjectConsumer [16]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 13, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #42 java/lang/invoke/MethodHandles$Lookup, outer class info: #44 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #46 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 30 arguments: {#33,#39,#40}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().referenceExpression.makeString(new Main()));\n" +
			"    }\n" +
			"    @Override\n" +
			"    public String toString() {\n" +
			"        return \"SUCCESS\";\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public ObjectToString referenceExpression = Object::toString;\n" +
			"}\n",
			"ObjectToString.java",
			"public interface ObjectToString {\n" +
			"    String makeString(Object obj);\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"referenceExpression\"\n" + 
			"    constant #6 utf8: \"LObjectToString;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"()V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" + 
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" + 
			"    constant #12 name_and_type: #13.#14 makeString ()LObjectToString;\n" + 
			"    constant #13 utf8: \"makeString\"\n" + 
			"    constant #14 utf8: \"()LObjectToString;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 makeString ()LObjectToString;\n" + 
			"    constant #16 field_ref: #1.#17 X.referenceExpression LObjectToString;\n" + 
			"    constant #17 name_and_type: #5.#6 referenceExpression LObjectToString;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"this\"\n" + 
			"    constant #21 utf8: \"LX;\"\n" + 
			"    constant #22 utf8: \"SourceFile\"\n" + 
			"    constant #23 utf8: \"X.java\"\n" + 
			"    constant #24 method_ref: #25.#27 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #25 class: #26 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #26 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #27 name_and_type: #28.#29 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #28 utf8: \"metafactory\"\n" + 
			"    constant #29 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #30 method handle: invokestatic (6) #24 \n" + 
			"    constant #31 utf8: \"BootstrapMethods\"\n" + 
			"    constant #32 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" + 
			"    constant #33 method type: #32 (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #34 method_ref: #3.#35 java/lang/Object.toString ()Ljava/lang/String;\n" + 
			"    constant #35 name_and_type: #36.#37 toString ()Ljava/lang/String;\n" + 
			"    constant #36 utf8: \"toString\"\n" + 
			"    constant #37 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #38 method handle: invokevirtual (5) #34 \n" + 
			"    constant #39 method type: #32 (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #40 utf8: \"InnerClasses\"\n" + 
			"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" + 
			"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #45 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LObjectToString;\n" + 
			"  public ObjectToString referenceExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [10]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 makeString() : ObjectToString [15]\n" + 
			"    10  putfield X.referenceExpression : ObjectToString [16]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 13, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #45 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 30 arguments: {#33,#38,#39}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().referenceExpression.produce());\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    String s = \"SUCCESS\";\n"+
			"    public StringProducer referenceExpression = s::toString;\n" +
			"}\n",
			"StringProducer.java",
			"public interface StringProducer {\n" +
			"    String produce();\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"s\"\n" + 
			"    constant #6 utf8: \"Ljava/lang/String;\"\n" + 
			"    constant #7 utf8: \"referenceExpression\"\n" + 
			"    constant #8 utf8: \"LStringProducer;\"\n" + 
			"    constant #9 utf8: \"<init>\"\n" + 
			"    constant #10 utf8: \"()V\"\n" + 
			"    constant #11 utf8: \"Code\"\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" + 
			"    constant #14 string: #15 \"SUCCESS\"\n" + 
			"    constant #15 utf8: \"SUCCESS\"\n" + 
			"    constant #16 field_ref: #1.#17 X.s Ljava/lang/String;\n" + 
			"    constant #17 name_and_type: #5.#6 s Ljava/lang/String;\n" + 
			"    constant #18 name_and_type: #19.#20 produce (Ljava/lang/String;)LStringProducer;\n" + 
			"    constant #19 utf8: \"produce\"\n" + 
			"    constant #20 utf8: \"(Ljava/lang/String;)LStringProducer;\"\n" + 
			"    constant #21 invoke dynamic: #0 #18 produce (Ljava/lang/String;)LStringProducer;\n" + 
			"    constant #22 field_ref: #1.#23 X.referenceExpression LStringProducer;\n" + 
			"    constant #23 name_and_type: #7.#8 referenceExpression LStringProducer;\n" + 
			"    constant #24 utf8: \"LineNumberTable\"\n" + 
			"    constant #25 utf8: \"LocalVariableTable\"\n" + 
			"    constant #26 utf8: \"this\"\n" + 
			"    constant #27 utf8: \"LX;\"\n" + 
			"    constant #28 utf8: \"SourceFile\"\n" + 
			"    constant #29 utf8: \"X.java\"\n" + 
			"    constant #30 method_ref: #31.#33 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #31 class: #32 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #32 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #33 name_and_type: #34.#35 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #34 utf8: \"metafactory\"\n" + 
			"    constant #35 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #36 method handle: invokestatic (6) #30 \n" + 
			"    constant #37 utf8: \"BootstrapMethods\"\n" + 
			"    constant #38 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #39 method type: #38 ()Ljava/lang/String;\n" + 
			"    constant #40 method_ref: #41.#43 java/lang/String.toString ()Ljava/lang/String;\n" + 
			"    constant #41 class: #42 java/lang/String\n" + 
			"    constant #42 utf8: \"java/lang/String\"\n" + 
			"    constant #43 name_and_type: #44.#38 toString ()Ljava/lang/String;\n" + 
			"    constant #44 utf8: \"toString\"\n" + 
			"    constant #45 method handle: invokevirtual (5) #40 \n" + 
			"    constant #46 method type: #38 ()Ljava/lang/String;\n" + 
			"    constant #47 utf8: \"InnerClasses\"\n" + 
			"    constant #48 class: #49 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #49 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #50 class: #51 java/lang/invoke/MethodHandles\n" + 
			"    constant #51 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #52 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 Ljava/lang/String;\n" + 
			"  java.lang.String s;\n" + 
			"  \n" + 
			"  // Field descriptor #8 LStringProducer;\n" + 
			"  public StringProducer referenceExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [12]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  ldc <String \"SUCCESS\"> [14]\n" + 
			"     7  putfield X.s : java.lang.String [16]\n" + 
			"    10  aload_0 [this]\n" + 
			"    11  aload_0 [this]\n" + 
			"    12  getfield X.s : java.lang.String [16]\n" + 
			"    15  invokedynamic 0 produce(java.lang.String) : StringProducer [21]\n" + 
			"    20  putfield X.referenceExpression : StringProducer [22]\n" + 
			"    23  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 10, line: 3]\n" + 
			"        [pc: 23, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 24] local: this index: 0 type: X\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #48 java/lang/invoke/MethodHandles$Lookup, outer class info: #50 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #52 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 36 arguments: {#39,#45,#46}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test005() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(X.allocatorExpression.produce());\n" +
			"    }\n" +
			"    @Override\n" +
			"    public String toString() {\n" +
			"        return \"SUCCESS\";" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public static MainProducer allocatorExpression = Main::new;\n" +
			"}\n",
			"MainProducer.java",
			"public interface MainProducer {\n" +
			"    Main produce();\n" +
			"}\n",
		},
	"SUCCESS"
	);
	verifyClassFile(			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"allocatorExpression\"\n" + 
			"    constant #6 utf8: \"LMainProducer;\"\n" + 
			"    constant #7 utf8: \"<clinit>\"\n" + 
			"    constant #8 utf8: \"()V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 name_and_type: #11.#12 produce ()LMainProducer;\n" + 
			"    constant #11 utf8: \"produce\"\n" + 
			"    constant #12 utf8: \"()LMainProducer;\"\n" + 
			"    constant #13 invoke dynamic: #0 #10 produce ()LMainProducer;\n" + 
			"    constant #14 field_ref: #1.#15 X.allocatorExpression LMainProducer;\n" + 
			"    constant #15 name_and_type: #5.#6 allocatorExpression LMainProducer;\n" + 
			"    constant #16 utf8: \"LineNumberTable\"\n" + 
			"    constant #17 utf8: \"LocalVariableTable\"\n" + 
			"    constant #18 utf8: \"<init>\"\n" + 
			"    constant #19 method_ref: #3.#20 java/lang/Object.<init> ()V\n" + 
			"    constant #20 name_and_type: #18.#8 <init> ()V\n" + 
			"    constant #21 utf8: \"this\"\n" + 
			"    constant #22 utf8: \"LX;\"\n" + 
			"    constant #23 utf8: \"SourceFile\"\n" + 
			"    constant #24 utf8: \"X.java\"\n" + 
			"    constant #25 method_ref: #26.#28 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #26 class: #27 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #27 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #28 name_and_type: #29.#30 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #29 utf8: \"metafactory\"\n" + 
			"    constant #30 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #31 method handle: invokestatic (6) #25 \n" + 
			"    constant #32 utf8: \"BootstrapMethods\"\n" + 
			"    constant #33 utf8: \"()LMain;\"\n" + 
			"    constant #34 method type: #33 ()LMain;\n" + 
			"    constant #35 method_ref: #36.#20 Main.<init> ()V\n" + 
			"    constant #36 class: #37 Main\n" + 
			"    constant #37 utf8: \"Main\"\n" + 
			"    constant #38 method handle: newinvokespecial (8) #35 \n" + 
			"    constant #39 method type: #33 ()LMain;\n" + 
			"    constant #40 utf8: \"InnerClasses\"\n" + 
			"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" + 
			"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #45 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LMainProducer;\n" + 
			"  public static MainProducer allocatorExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 1, Locals: 0\n" + 
			"  static {};\n" + 
			"    0  invokedynamic 0 produce() : MainProducer [13]\n" + 
			"    5  putstatic X.allocatorExpression : MainProducer [14]\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 2]\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public X();\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokespecial java.lang.Object() [19]\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #45 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 31 arguments: {#34,#38,#39}\n" + 
			"}", "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test006() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    String s1, s2;\n" +
			"    public Main(String val1, String val2) {" +
			"        s1 = val1;\n" +
			"        s2 = val2;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        Main m = X.producer.apply(\"SUCC\", \"ESS\");\n" +
			"        System.out.println(m);\n" +
			"    }\n" +
			"    public String toString() {\n" +
			"        return s1 + s2;" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"        public static Function2<Main, String, String> producer = Main::new;\n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",
		},
	"SUCCESS"
	);
	String expected =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"producer\"\n" + 
			"    constant #6 utf8: \"LFunction2;\"\n" + 
			"    constant #7 utf8: \"Signature\"\n" + 
			"    constant #8 utf8: \"LFunction2<LMain;Ljava/lang/String;Ljava/lang/String;>;\"\n" + 
			"    constant #9 utf8: \"<clinit>\"\n" + 
			"    constant #10 utf8: \"()V\"\n" + 
			"    constant #11 utf8: \"Code\"\n" + 
			"    constant #12 name_and_type: #13.#14 apply ()LFunction2;\n" + 
			"    constant #13 utf8: \"apply\"\n" + 
			"    constant #14 utf8: \"()LFunction2;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 apply ()LFunction2;\n" + 
			"    constant #16 field_ref: #1.#17 X.producer LFunction2;\n" + 
			"    constant #17 name_and_type: #5.#6 producer LFunction2;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"<init>\"\n" + 
			"    constant #21 method_ref: #3.#22 java/lang/Object.<init> ()V\n" + 
			"    constant #22 name_and_type: #20.#10 <init> ()V\n" + 
			"    constant #23 utf8: \"this\"\n" + 
			"    constant #24 utf8: \"LX;\"\n" + 
			"    constant #25 utf8: \"SourceFile\"\n" + 
			"    constant #26 utf8: \"X.java\"\n" + 
			"    constant #27 method_ref: #28.#30 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #28 class: #29 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #29 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #30 name_and_type: #31.#32 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #31 utf8: \"metafactory\"\n" + 
			"    constant #32 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #33 method handle: invokestatic (6) #27 \n" + 
			"    constant #34 utf8: \"BootstrapMethods\"\n" + 
			"    constant #35 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" + 
			"    constant #36 method type: #35 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
			"    constant #37 method_ref: #38.#40 Main.<init> (Ljava/lang/String;Ljava/lang/String;)V\n" + 
			"    constant #38 class: #39 Main\n" + 
			"    constant #39 utf8: \"Main\"\n" + 
			"    constant #40 name_and_type: #20.#41 <init> (Ljava/lang/String;Ljava/lang/String;)V\n" + 
			"    constant #41 utf8: \"(Ljava/lang/String;Ljava/lang/String;)V\"\n" + 
			"    constant #42 method handle: newinvokespecial (8) #37 \n" + 
			"    constant #43 utf8: \"(Ljava/lang/String;Ljava/lang/String;)LMain;\"\n" + 
			"    constant #44 method type: #43 (Ljava/lang/String;Ljava/lang/String;)LMain;\n" + 
			"    constant #45 utf8: \"InnerClasses\"\n" + 
			"    constant #46 class: #47 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #47 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #48 class: #49 java/lang/invoke/MethodHandles\n" + 
			"    constant #49 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #50 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LFunction2;\n" + 
			"  // Signature: LFunction2<LMain;Ljava/lang/String;Ljava/lang/String;>;\n" + 
			"  public static Function2 producer;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 1, Locals: 0\n" + 
			"  static {};\n" + 
			"    0  invokedynamic 0 apply() : Function2 [15]\n" + 
			"    5  putstatic X.producer : Function2 [16]\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 2]\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public X();\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokespecial java.lang.Object() [21]\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #46 java/lang/invoke/MethodHandles$Lookup, outer class info: #48 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #50 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 33 arguments: {#36,#42,#44}\n" + 
			"}";
	verifyClassFile(expected, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test007() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.run();\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Runnable referenceExpression = () -> {" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    };\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"referenceExpression\"\n" + 
			"    constant #6 utf8: \"Ljava/lang/Runnable;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"()V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" + 
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" + 
			"    constant #12 name_and_type: #13.#14 run ()Ljava/lang/Runnable;\n" + 
			"    constant #13 utf8: \"run\"\n" + 
			"    constant #14 utf8: \"()Ljava/lang/Runnable;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 run ()Ljava/lang/Runnable;\n" + 
			"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" + 
			"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"this\"\n" + 
			"    constant #21 utf8: \"LX;\"\n" + 
			"    constant #22 utf8: \"lambda$0\"\n" + 
			"    constant #23 field_ref: #24.#26 java/lang/System.out Ljava/io/PrintStream;\n" + 
			"    constant #24 class: #25 java/lang/System\n" + 
			"    constant #25 utf8: \"java/lang/System\"\n" + 
			"    constant #26 name_and_type: #27.#28 out Ljava/io/PrintStream;\n" + 
			"    constant #27 utf8: \"out\"\n" + 
			"    constant #28 utf8: \"Ljava/io/PrintStream;\"\n" + 
			"    constant #29 string: #30 \"SUCCESS\"\n" + 
			"    constant #30 utf8: \"SUCCESS\"\n" + 
			"    constant #31 method_ref: #32.#34 java/io/PrintStream.println (Ljava/lang/String;)V\n" + 
			"    constant #32 class: #33 java/io/PrintStream\n" + 
			"    constant #33 utf8: \"java/io/PrintStream\"\n" + 
			"    constant #34 name_and_type: #35.#36 println (Ljava/lang/String;)V\n" + 
			"    constant #35 utf8: \"println\"\n" + 
			"    constant #36 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #37 utf8: \"SourceFile\"\n" + 
			"    constant #38 utf8: \"X.java\"\n" + 
			"    constant #39 method_ref: #40.#42 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #40 class: #41 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #41 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #42 name_and_type: #43.#44 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #43 utf8: \"metafactory\"\n" + 
			"    constant #44 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #45 method handle: invokestatic (6) #39 \n" + 
			"    constant #46 utf8: \"BootstrapMethods\"\n" + 
			"    constant #47 method type: #8 ()V\n" + 
			"    constant #48 method_ref: #1.#49 X.lambda$0 ()V\n" + 
			"    constant #49 name_and_type: #22.#8 lambda$0 ()V\n" + 
			"    constant #50 method handle: invokestatic (6) #48 \n" + 
			"    constant #51 method type: #8 ()V\n" + 
			"    constant #52 utf8: \"InnerClasses\"\n" + 
			"    constant #53 class: #54 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #55 class: #56 java/lang/invoke/MethodHandles\n" + 
			"    constant #56 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #57 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 Ljava/lang/Runnable;\n" + 
			"  public java.lang.Runnable referenceExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [10]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 run() : java.lang.Runnable [15]\n" + 
			"    10  putfield X.referenceExpression : java.lang.Runnable [16]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 13, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 0\n" + 
			"  private static synthetic void lambda$0();\n" + 
			"    0  getstatic java.lang.System.out : java.io.PrintStream [23]\n" + 
			"    3  ldc <String \"SUCCESS\"> [29]\n" + 
			"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 2]\n" + 
			"        [pc: 8, line: 3]\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #57 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 45 arguments: {#47,#50,#51}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test007a() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.run();\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Runnable referenceExpression = () -> System.out.println(\"SUCCESS\");\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"referenceExpression\"\n" + 
			"    constant #6 utf8: \"Ljava/lang/Runnable;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"()V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" + 
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" + 
			"    constant #12 name_and_type: #13.#14 run ()Ljava/lang/Runnable;\n" + 
			"    constant #13 utf8: \"run\"\n" + 
			"    constant #14 utf8: \"()Ljava/lang/Runnable;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 run ()Ljava/lang/Runnable;\n" + 
			"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" + 
			"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"this\"\n" + 
			"    constant #21 utf8: \"LX;\"\n" + 
			"    constant #22 utf8: \"lambda$0\"\n" + 
			"    constant #23 field_ref: #24.#26 java/lang/System.out Ljava/io/PrintStream;\n" + 
			"    constant #24 class: #25 java/lang/System\n" + 
			"    constant #25 utf8: \"java/lang/System\"\n" + 
			"    constant #26 name_and_type: #27.#28 out Ljava/io/PrintStream;\n" + 
			"    constant #27 utf8: \"out\"\n" + 
			"    constant #28 utf8: \"Ljava/io/PrintStream;\"\n" + 
			"    constant #29 string: #30 \"SUCCESS\"\n" + 
			"    constant #30 utf8: \"SUCCESS\"\n" + 
			"    constant #31 method_ref: #32.#34 java/io/PrintStream.println (Ljava/lang/String;)V\n" + 
			"    constant #32 class: #33 java/io/PrintStream\n" + 
			"    constant #33 utf8: \"java/io/PrintStream\"\n" + 
			"    constant #34 name_and_type: #35.#36 println (Ljava/lang/String;)V\n" + 
			"    constant #35 utf8: \"println\"\n" + 
			"    constant #36 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #37 utf8: \"SourceFile\"\n" + 
			"    constant #38 utf8: \"X.java\"\n" + 
			"    constant #39 method_ref: #40.#42 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #40 class: #41 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #41 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #42 name_and_type: #43.#44 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #43 utf8: \"metafactory\"\n" + 
			"    constant #44 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #45 method handle: invokestatic (6) #39 \n" + 
			"    constant #46 utf8: \"BootstrapMethods\"\n" + 
			"    constant #47 method type: #8 ()V\n" + 
			"    constant #48 method_ref: #1.#49 X.lambda$0 ()V\n" + 
			"    constant #49 name_and_type: #22.#8 lambda$0 ()V\n" + 
			"    constant #50 method handle: invokestatic (6) #48 \n" + 
			"    constant #51 method type: #8 ()V\n" + 
			"    constant #52 utf8: \"InnerClasses\"\n" + 
			"    constant #53 class: #54 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #55 class: #56 java/lang/invoke/MethodHandles\n" + 
			"    constant #56 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #57 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 Ljava/lang/Runnable;\n" + 
			"  public java.lang.Runnable referenceExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [10]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 run() : java.lang.Runnable [15]\n" + 
			"    10  putfield X.referenceExpression : java.lang.Runnable [16]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 13, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 0\n" + 
			"  private static synthetic void lambda$0();\n" + 
			"    0  getstatic java.lang.System.out : java.io.PrintStream [23]\n" + 
			"    3  ldc <String \"SUCCESS\"> [29]\n" + 
			"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 2]\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #57 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 45 arguments: {#47,#50,#51}\n" + 
			"}";
	
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test008() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().lambda.get());\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public java.util.function.Supplier<String> lambda = () -> { return \"SUCCESS\"; }; \n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"lambda\"\n" + 
			"    constant #6 utf8: \"Ljava/util/function/Supplier;\"\n" + 
			"    constant #7 utf8: \"Signature\"\n" + 
			"    constant #8 utf8: \"Ljava/util/function/Supplier<Ljava/lang/String;>;\"\n" + 
			"    constant #9 utf8: \"<init>\"\n" + 
			"    constant #10 utf8: \"()V\"\n" + 
			"    constant #11 utf8: \"Code\"\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" + 
			"    constant #14 name_and_type: #15.#16 get ()Ljava/util/function/Supplier;\n" + 
			"    constant #15 utf8: \"get\"\n" + 
			"    constant #16 utf8: \"()Ljava/util/function/Supplier;\"\n" + 
			"    constant #17 invoke dynamic: #0 #14 get ()Ljava/util/function/Supplier;\n" + 
			"    constant #18 field_ref: #1.#19 X.lambda Ljava/util/function/Supplier;\n" + 
			"    constant #19 name_and_type: #5.#6 lambda Ljava/util/function/Supplier;\n" + 
			"    constant #20 utf8: \"LineNumberTable\"\n" + 
			"    constant #21 utf8: \"LocalVariableTable\"\n" + 
			"    constant #22 utf8: \"this\"\n" + 
			"    constant #23 utf8: \"LX;\"\n" + 
			"    constant #24 utf8: \"lambda$0\"\n" + 
			"    constant #25 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #26 string: #27 \"SUCCESS\"\n" + 
			"    constant #27 utf8: \"SUCCESS\"\n" + 
			"    constant #28 utf8: \"SourceFile\"\n" + 
			"    constant #29 utf8: \"X.java\"\n" + 
			"    constant #30 method_ref: #31.#33 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #31 class: #32 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #32 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #33 name_and_type: #34.#35 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #34 utf8: \"metafactory\"\n" + 
			"    constant #35 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #36 method handle: invokestatic (6) #30 \n" + 
			"    constant #37 utf8: \"BootstrapMethods\"\n" + 
			"    constant #38 utf8: \"()Ljava/lang/Object;\"\n" + 
			"    constant #39 method type: #38 ()Ljava/lang/Object;\n" + 
			"    constant #40 method_ref: #1.#41 X.lambda$0 ()Ljava/lang/String;\n" + 
			"    constant #41 name_and_type: #24.#25 lambda$0 ()Ljava/lang/String;\n" + 
			"    constant #42 method handle: invokestatic (6) #40 \n" + 
			"    constant #43 method type: #25 ()Ljava/lang/String;\n" + 
			"    constant #44 utf8: \"InnerClasses\"\n" + 
			"    constant #45 class: #46 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #46 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #47 class: #48 java/lang/invoke/MethodHandles\n" + 
			"    constant #48 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #49 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 Ljava/util/function/Supplier;\n" + 
			"  // Signature: Ljava/util/function/Supplier<Ljava/lang/String;>;\n" + 
			"  public java.util.function.Supplier lambda;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [12]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 get() : java.util.function.Supplier [17]\n" + 
			"    10  putfield X.lambda : java.util.function.Supplier [18]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 13, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #25 ()Ljava/lang/String;\n" + 
			"  // Stack: 1, Locals: 0\n" + 
			"  private static synthetic java.lang.String lambda$0();\n" + 
			"    0  ldc <String \"SUCCESS\"> [26]\n" + 
			"    2  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 2]\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #45 java/lang/invoke/MethodHandles$Lookup, outer class info: #47 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #49 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 36 arguments: {#39,#42,#43}\n" + 
			"}"
;

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test009() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().concat.apply(\"SUCC\",\"ESS\"));\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Function2<String, String, String> concat = (s1, s2) -> { return s1 + s2; }; \n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",

		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"concat\"\n" + 
			"    constant #6 utf8: \"LFunction2;\"\n" + 
			"    constant #7 utf8: \"Signature\"\n" + 
			"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" + 
			"    constant #9 utf8: \"<init>\"\n" + 
			"    constant #10 utf8: \"()V\"\n" + 
			"    constant #11 utf8: \"Code\"\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" + 
			"    constant #14 name_and_type: #15.#16 apply ()LFunction2;\n" + 
			"    constant #15 utf8: \"apply\"\n" + 
			"    constant #16 utf8: \"()LFunction2;\"\n" + 
			"    constant #17 invoke dynamic: #0 #14 apply ()LFunction2;\n" + 
			"    constant #18 field_ref: #1.#19 X.concat LFunction2;\n" + 
			"    constant #19 name_and_type: #5.#6 concat LFunction2;\n" + 
			"    constant #20 utf8: \"LineNumberTable\"\n" + 
			"    constant #21 utf8: \"LocalVariableTable\"\n" + 
			"    constant #22 utf8: \"this\"\n" + 
			"    constant #23 utf8: \"LX;\"\n" + 
			"    constant #24 utf8: \"lambda$0\"\n" + 
			"    constant #25 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" + 
			"    constant #26 class: #27 java/lang/StringBuilder\n" + 
			"    constant #27 utf8: \"java/lang/StringBuilder\"\n" + 
			"    constant #28 method_ref: #29.#31 java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #29 class: #30 java/lang/String\n" + 
			"    constant #30 utf8: \"java/lang/String\"\n" + 
			"    constant #31 name_and_type: #32.#33 valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #32 utf8: \"valueOf\"\n" + 
			"    constant #33 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" + 
			"    constant #34 method_ref: #26.#35 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" + 
			"    constant #35 name_and_type: #9.#36 <init> (Ljava/lang/String;)V\n" + 
			"    constant #36 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #37 method_ref: #26.#38 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" + 
			"    constant #38 name_and_type: #39.#40 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" + 
			"    constant #39 utf8: \"append\"\n" + 
			"    constant #40 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" + 
			"    constant #41 method_ref: #26.#42 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" + 
			"    constant #42 name_and_type: #43.#44 toString ()Ljava/lang/String;\n" + 
			"    constant #43 utf8: \"toString\"\n" + 
			"    constant #44 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #45 utf8: \"s1\"\n" + 
			"    constant #46 utf8: \"Ljava/lang/String;\"\n" + 
			"    constant #47 utf8: \"s2\"\n" + 
			"    constant #48 utf8: \"SourceFile\"\n" + 
			"    constant #49 utf8: \"X.java\"\n" + 
			"    constant #50 method_ref: #51.#53 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #51 class: #52 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #52 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #53 name_and_type: #54.#55 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #54 utf8: \"metafactory\"\n" + 
			"    constant #55 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #56 method handle: invokestatic (6) #50 \n" + 
			"    constant #57 utf8: \"BootstrapMethods\"\n" + 
			"    constant #58 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" + 
			"    constant #59 method type: #58 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
			"    constant #60 method_ref: #1.#61 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #61 name_and_type: #24.#25 lambda$0 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #62 method handle: invokestatic (6) #60 \n" + 
			"    constant #63 method type: #25 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #64 utf8: \"InnerClasses\"\n" + 
			"    constant #65 class: #66 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #66 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #67 class: #68 java/lang/invoke/MethodHandles\n" + 
			"    constant #68 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #69 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LFunction2;\n" + 
			"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" + 
			"  public Function2 concat;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [12]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 apply() : Function2 [17]\n" + 
			"    10  putfield X.concat : Function2 [18]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 2]\n" + 
			"        [pc: 13, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #25 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"  // Stack: 3, Locals: 2\n" + 
			"  private static synthetic java.lang.String lambda$0(java.lang.String s1, java.lang.String s2);\n" + 
			"     0  new java.lang.StringBuilder [26]\n" + 
			"     3  dup\n" + 
			"     4  aload_0 [s1]\n" + 
			"     5  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [28]\n" + 
			"     8  invokespecial java.lang.StringBuilder(java.lang.String) [34]\n" + 
			"    11  aload_1 [s2]\n" + 
			"    12  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [37]\n" + 
			"    15  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [41]\n" + 
			"    18  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 2]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 19] local: s1 index: 0 type: java.lang.String\n" + 
			"        [pc: 0, pc: 19] local: s2 index: 1 type: java.lang.String\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #65 java/lang/invoke/MethodHandles$Lookup, outer class info: #67 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #69 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 56 arguments: {#59,#62,#63}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test010() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().concat.apply(\"UCC\",\"ESS\"));\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Function2<String, String, String> concat; \n" +
			"    {\n" +
			"        String s0 = new String(\"S\");\n" +
			"        concat = (s1, s2) -> { return s0 + s1 + s2; }; \n" +
			"    }\n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",

		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"concat\"\n" + 
			"    constant #6 utf8: \"LFunction2;\"\n" + 
			"    constant #7 utf8: \"Signature\"\n" + 
			"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" + 
			"    constant #9 utf8: \"<init>\"\n" + 
			"    constant #10 utf8: \"()V\"\n" + 
			"    constant #11 utf8: \"Code\"\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" + 
			"    constant #14 class: #15 java/lang/String\n" + 
			"    constant #15 utf8: \"java/lang/String\"\n" + 
			"    constant #16 string: #17 \"S\"\n" + 
			"    constant #17 utf8: \"S\"\n" + 
			"    constant #18 method_ref: #14.#19 java/lang/String.<init> (Ljava/lang/String;)V\n" + 
			"    constant #19 name_and_type: #9.#20 <init> (Ljava/lang/String;)V\n" + 
			"    constant #20 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #21 name_and_type: #22.#23 apply (Ljava/lang/String;)LFunction2;\n" + 
			"    constant #22 utf8: \"apply\"\n" + 
			"    constant #23 utf8: \"(Ljava/lang/String;)LFunction2;\"\n" + 
			"    constant #24 invoke dynamic: #0 #21 apply (Ljava/lang/String;)LFunction2;\n" + 
			"    constant #25 field_ref: #1.#26 X.concat LFunction2;\n" + 
			"    constant #26 name_and_type: #5.#6 concat LFunction2;\n" + 
			"    constant #27 utf8: \"LineNumberTable\"\n" + 
			"    constant #28 utf8: \"LocalVariableTable\"\n" + 
			"    constant #29 utf8: \"this\"\n" + 
			"    constant #30 utf8: \"LX;\"\n" + 
			"    constant #31 utf8: \"s0\"\n" + 
			"    constant #32 utf8: \"Ljava/lang/String;\"\n" + 
			"    constant #33 utf8: \"lambda$0\"\n" + 
			"    constant #34 utf8: \"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" + 
			"    constant #35 class: #36 java/lang/StringBuilder\n" + 
			"    constant #36 utf8: \"java/lang/StringBuilder\"\n" + 
			"    constant #37 method_ref: #14.#38 java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #38 name_and_type: #39.#40 valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #39 utf8: \"valueOf\"\n" + 
			"    constant #40 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" + 
			"    constant #41 method_ref: #35.#19 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" + 
			"    constant #42 method_ref: #35.#43 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" + 
			"    constant #43 name_and_type: #44.#45 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" + 
			"    constant #44 utf8: \"append\"\n" + 
			"    constant #45 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" + 
			"    constant #46 method_ref: #35.#47 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" + 
			"    constant #47 name_and_type: #48.#49 toString ()Ljava/lang/String;\n" + 
			"    constant #48 utf8: \"toString\"\n" + 
			"    constant #49 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #50 utf8: \"s1\"\n" + 
			"    constant #51 utf8: \"s2\"\n" + 
			"    constant #52 utf8: \"SourceFile\"\n" + 
			"    constant #53 utf8: \"X.java\"\n" + 
			"    constant #54 method_ref: #55.#57 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #55 class: #56 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #56 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #57 name_and_type: #58.#59 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #58 utf8: \"metafactory\"\n" + 
			"    constant #59 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #60 method handle: invokestatic (6) #54 \n" + 
			"    constant #61 utf8: \"BootstrapMethods\"\n" + 
			"    constant #62 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" + 
			"    constant #63 method type: #62 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
			"    constant #64 method_ref: #1.#65 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #65 name_and_type: #33.#34 lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #66 method handle: invokestatic (6) #64 \n" + 
			"    constant #67 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" + 
			"    constant #68 method type: #67 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #69 utf8: \"InnerClasses\"\n" + 
			"    constant #70 class: #71 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #71 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #72 class: #73 java/lang/invoke/MethodHandles\n" + 
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #74 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LFunction2;\n" + 
			"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" + 
			"  public Function2 concat;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 3, Locals: 2\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [12]\n" + 
			"     4  new java.lang.String [14]\n" + 
			"     7  dup\n" + 
			"     8  ldc <String \"S\"> [16]\n" + 
			"    10  invokespecial java.lang.String(java.lang.String) [18]\n" + 
			"    13  astore_1 [s0]\n" + 
			"    14  aload_0 [this]\n" + 
			"    15  aload_1 [s0]\n" + 
			"    16  invokedynamic 0 apply(java.lang.String) : Function2 [24]\n" + 
			"    21  putfield X.concat : Function2 [25]\n" + 
			"    24  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 4]\n" + 
			"        [pc: 14, line: 5]\n" + 
			"        [pc: 24, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 25] local: this index: 0 type: X\n" + 
			"        [pc: 14, pc: 24] local: s0 index: 1 type: java.lang.String\n" + 
			"  \n" + 
			"  // Method descriptor #34 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"  // Stack: 3, Locals: 3\n" + 
			"  private static synthetic java.lang.String lambda$0(java.lang.String arg0, java.lang.String s1, java.lang.String s2);\n" + 
			"     0  new java.lang.StringBuilder [35]\n" + 
			"     3  dup\n" + 
			"     4  aload_0 [arg0]\n" + 
			"     5  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [37]\n" + 
			"     8  invokespecial java.lang.StringBuilder(java.lang.String) [41]\n" + 
			"    11  aload_1 [s1]\n" + 
			"    12  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" + 
			"    15  aload_2 [s2]\n" + 
			"    16  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" + 
			"    19  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [46]\n" + 
			"    22  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 23] local: s1 index: 1 type: java.lang.String\n" + 
			"        [pc: 0, pc: 23] local: s2 index: 2 type: java.lang.String\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #70 java/lang/invoke/MethodHandles$Lookup, outer class info: #72 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #74 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 60 arguments: {#63,#66,#68}\n" + 
			"}"
;

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test011() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().concat.apply(\"UCC\",\"ESS\"));\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Function2<String, String, String> concat; \n" +
			"    {\n" +
			"        String s0 = new String(\"S\");\n" +
			"        concat = (s1, s2) -> s0 + s1 + s2; \n" +
			"    }\n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",

		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"concat\"\n" + 
			"    constant #6 utf8: \"LFunction2;\"\n" + 
			"    constant #7 utf8: \"Signature\"\n" + 
			"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" + 
			"    constant #9 utf8: \"<init>\"\n" + 
			"    constant #10 utf8: \"()V\"\n" + 
			"    constant #11 utf8: \"Code\"\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" + 
			"    constant #14 class: #15 java/lang/String\n" + 
			"    constant #15 utf8: \"java/lang/String\"\n" + 
			"    constant #16 string: #17 \"S\"\n" + 
			"    constant #17 utf8: \"S\"\n" + 
			"    constant #18 method_ref: #14.#19 java/lang/String.<init> (Ljava/lang/String;)V\n" + 
			"    constant #19 name_and_type: #9.#20 <init> (Ljava/lang/String;)V\n" + 
			"    constant #20 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #21 name_and_type: #22.#23 apply (Ljava/lang/String;)LFunction2;\n" + 
			"    constant #22 utf8: \"apply\"\n" + 
			"    constant #23 utf8: \"(Ljava/lang/String;)LFunction2;\"\n" + 
			"    constant #24 invoke dynamic: #0 #21 apply (Ljava/lang/String;)LFunction2;\n" + 
			"    constant #25 field_ref: #1.#26 X.concat LFunction2;\n" + 
			"    constant #26 name_and_type: #5.#6 concat LFunction2;\n" + 
			"    constant #27 utf8: \"LineNumberTable\"\n" + 
			"    constant #28 utf8: \"LocalVariableTable\"\n" + 
			"    constant #29 utf8: \"this\"\n" + 
			"    constant #30 utf8: \"LX;\"\n" + 
			"    constant #31 utf8: \"s0\"\n" + 
			"    constant #32 utf8: \"Ljava/lang/String;\"\n" + 
			"    constant #33 utf8: \"lambda$0\"\n" + 
			"    constant #34 utf8: \"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" + 
			"    constant #35 class: #36 java/lang/StringBuilder\n" + 
			"    constant #36 utf8: \"java/lang/StringBuilder\"\n" + 
			"    constant #37 method_ref: #14.#38 java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #38 name_and_type: #39.#40 valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #39 utf8: \"valueOf\"\n" + 
			"    constant #40 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" + 
			"    constant #41 method_ref: #35.#19 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" + 
			"    constant #42 method_ref: #35.#43 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" + 
			"    constant #43 name_and_type: #44.#45 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" + 
			"    constant #44 utf8: \"append\"\n" + 
			"    constant #45 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" + 
			"    constant #46 method_ref: #35.#47 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" + 
			"    constant #47 name_and_type: #48.#49 toString ()Ljava/lang/String;\n" + 
			"    constant #48 utf8: \"toString\"\n" + 
			"    constant #49 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #50 utf8: \"s1\"\n" + 
			"    constant #51 utf8: \"s2\"\n" + 
			"    constant #52 utf8: \"SourceFile\"\n" + 
			"    constant #53 utf8: \"X.java\"\n" + 
			"    constant #54 method_ref: #55.#57 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #55 class: #56 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #56 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #57 name_and_type: #58.#59 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #58 utf8: \"metafactory\"\n" + 
			"    constant #59 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #60 method handle: invokestatic (6) #54 \n" + 
			"    constant #61 utf8: \"BootstrapMethods\"\n" + 
			"    constant #62 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" + 
			"    constant #63 method type: #62 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
			"    constant #64 method_ref: #1.#65 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #65 name_and_type: #33.#34 lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #66 method handle: invokestatic (6) #64 \n" + 
			"    constant #67 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" + 
			"    constant #68 method type: #67 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"    constant #69 utf8: \"InnerClasses\"\n" + 
			"    constant #70 class: #71 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #71 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #72 class: #73 java/lang/invoke/MethodHandles\n" + 
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #74 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LFunction2;\n" + 
			"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" + 
			"  public Function2 concat;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 3, Locals: 2\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [12]\n" + 
			"     4  new java.lang.String [14]\n" + 
			"     7  dup\n" + 
			"     8  ldc <String \"S\"> [16]\n" + 
			"    10  invokespecial java.lang.String(java.lang.String) [18]\n" + 
			"    13  astore_1 [s0]\n" + 
			"    14  aload_0 [this]\n" + 
			"    15  aload_1 [s0]\n" + 
			"    16  invokedynamic 0 apply(java.lang.String) : Function2 [24]\n" + 
			"    21  putfield X.concat : Function2 [25]\n" + 
			"    24  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"        [pc: 4, line: 4]\n" + 
			"        [pc: 14, line: 5]\n" + 
			"        [pc: 24, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 25] local: this index: 0 type: X\n" + 
			"        [pc: 14, pc: 24] local: s0 index: 1 type: java.lang.String\n" + 
			"  \n" + 
			"  // Method descriptor #34 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" + 
			"  // Stack: 3, Locals: 3\n" + 
			"  private static synthetic java.lang.String lambda$0(java.lang.String arg0, java.lang.String s1, java.lang.String s2);\n" + 
			"     0  new java.lang.StringBuilder [35]\n" + 
			"     3  dup\n" + 
			"     4  aload_0 [arg0]\n" + 
			"     5  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [37]\n" + 
			"     8  invokespecial java.lang.StringBuilder(java.lang.String) [41]\n" + 
			"    11  aload_1 [s1]\n" + 
			"    12  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" + 
			"    15  aload_2 [s2]\n" + 
			"    16  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" + 
			"    19  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [46]\n" + 
			"    22  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 23] local: s1 index: 1 type: java.lang.String\n" + 
			"        [pc: 0, pc: 23] local: s2 index: 2 type: java.lang.String\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #70 java/lang/invoke/MethodHandles$Lookup, outer class info: #72 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #74 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 60 arguments: {#63,#66,#68}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406627,  [1.8][compiler][codegen] Annotations on lambda parameters go the way of /dev/null
public void test012() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"interface I {\n" +
				"	void doit (Object o, Object p);\n" +
				"}\n" +
				"public class X {\n" +
				"   public static void main(String [] args) {\n" +
				"   int local1 = 0,  local2 = 1;\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"       int j = args.length + local1 + local2;\n" +
				"	};\n" +
				"}\n" +
				"}\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Annotation {\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"<init>\"\n" + 
			"    constant #6 utf8: \"()V\"\n" + 
			"    constant #7 utf8: \"Code\"\n" + 
			"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" + 
			"    constant #9 name_and_type: #5.#6 <init> ()V\n" + 
			"    constant #10 utf8: \"LineNumberTable\"\n" + 
			"    constant #11 utf8: \"LocalVariableTable\"\n" + 
			"    constant #12 utf8: \"this\"\n" + 
			"    constant #13 utf8: \"LX;\"\n" + 
			"    constant #14 utf8: \"main\"\n" + 
			"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" + 
			"    constant #16 name_and_type: #17.#18 doit ([Ljava/lang/String;II)LI;\n" + 
			"    constant #17 utf8: \"doit\"\n" + 
			"    constant #18 utf8: \"([Ljava/lang/String;II)LI;\"\n" + 
			"    constant #19 invoke dynamic: #0 #16 doit ([Ljava/lang/String;II)LI;\n" + 
			"    constant #20 utf8: \"args\"\n" + 
			"    constant #21 utf8: \"[Ljava/lang/String;\"\n" + 
			"    constant #22 utf8: \"local1\"\n" + 
			"    constant #23 utf8: \"I\"\n" + 
			"    constant #24 utf8: \"local2\"\n" + 
			"    constant #25 utf8: \"i\"\n" + 
			"    constant #26 utf8: \"LI;\"\n" + 
			"    constant #27 utf8: \"lambda$0\"\n" + 
			"    constant #28 utf8: \"([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\"\n" + 
			"    constant #29 utf8: \"RuntimeVisibleParameterAnnotations\"\n" + 
			"    constant #30 utf8: \"LAnnotation;\"\n" + 
			"    constant #31 utf8: \"o\"\n" + 
			"    constant #32 utf8: \"Ljava/lang/Object;\"\n" + 
			"    constant #33 utf8: \"p\"\n" + 
			"    constant #34 utf8: \"SourceFile\"\n" + 
			"    constant #35 utf8: \"X.java\"\n" + 
			"    constant #36 method_ref: #37.#39 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #37 class: #38 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #38 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #39 name_and_type: #40.#41 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #40 utf8: \"metafactory\"\n" + 
			"    constant #41 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #42 method handle: invokestatic (6) #36 \n" + 
			"    constant #43 utf8: \"BootstrapMethods\"\n" + 
			"    constant #44 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)V\"\n" + 
			"    constant #45 method type: #44 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #46 method_ref: #1.#47 X.lambda$0 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #47 name_and_type: #27.#28 lambda$0 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #48 method handle: invokestatic (6) #46 \n" + 
			"    constant #49 method type: #44 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #50 utf8: \"InnerClasses\"\n" + 
			"    constant #51 class: #52 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #52 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #53 class: #54 java/lang/invoke/MethodHandles\n" + 
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #55 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public X();\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokespecial java.lang.Object() [8]\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 3, Locals: 4\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [local1]\n" + 
			"     2  iconst_1\n" + 
			"     3  istore_2 [local2]\n" + 
			"     4  aload_0 [args]\n" + 
			"     5  iload_1 [local1]\n" + 
			"     6  iload_2 [local2]\n" + 
			"     7  invokedynamic 0 doit(java.lang.String[], int, int) : I [19]\n" + 
			"    12  astore_3 [i]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 10]\n" + 
			"        [pc: 4, line: 11]\n" + 
			"        [pc: 13, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 14] local: local1 index: 1 type: int\n" + 
			"        [pc: 4, pc: 14] local: local2 index: 2 type: int\n" + 
			"        [pc: 13, pc: 14] local: i index: 3 type: I\n" + 
			"  \n" + 
			"  // Method descriptor #28 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" + 
			"  // Stack: 2, Locals: 6\n" + 
			"  private static synthetic void lambda$0(java.lang.String[] arg0, int arg1, int arg2, java.lang.Object o, java.lang.Object p);\n" + 
			"    0  aload_0 [arg0]\n" + 
			"    1  arraylength\n" + 
			"    2  iload_1 [arg1]\n" + 
			"    3  iadd\n" + 
			"    4  iload_2 [arg2]\n" + 
			"    5  iadd\n" + 
			"    6  istore 5\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 12]\n" + 
			"        [pc: 8, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 9] local: o index: 3 type: java.lang.Object\n" + 
			"        [pc: 0, pc: 9] local: p index: 4 type: java.lang.Object\n" + 
			"    RuntimeVisibleParameterAnnotations: \n" + 
			"      Number of annotations for parameter 0: 0\n" + 
			"      Number of annotations for parameter 1: 0\n" + 
			"      Number of annotations for parameter 2: 0\n" + 
			"      Number of annotations for parameter 3: 1\n" + 
			"        #30 @Annotation(\n" + 
			"        )\n" + 
			"      Number of annotations for parameter 4: 1\n" + 
			"        #30 @Annotation(\n" + 
			"        )\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #51 java/lang/invoke/MethodHandles$Lookup, outer class info: #53 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #55 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 42 arguments: {#45,#48,#49}\n" + 
			"}"
;

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406627,  [1.8][compiler][codegen] Annotations on lambda parameters go the way of /dev/null
public void test013() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"interface I {\n" +
				"	void doit (Object o, Object p);\n" +
				"}\n" +
				"public class X {\n" +
				"   public static void main(String [] args) {\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"	};\n" +
				"}\n" +
				"}\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Annotation {\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"<init>\"\n" + 
			"    constant #6 utf8: \"()V\"\n" + 
			"    constant #7 utf8: \"Code\"\n" + 
			"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" + 
			"    constant #9 name_and_type: #5.#6 <init> ()V\n" + 
			"    constant #10 utf8: \"LineNumberTable\"\n" + 
			"    constant #11 utf8: \"LocalVariableTable\"\n" + 
			"    constant #12 utf8: \"this\"\n" + 
			"    constant #13 utf8: \"LX;\"\n" + 
			"    constant #14 utf8: \"main\"\n" + 
			"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" + 
			"    constant #16 name_and_type: #17.#18 doit ()LI;\n" + 
			"    constant #17 utf8: \"doit\"\n" + 
			"    constant #18 utf8: \"()LI;\"\n" + 
			"    constant #19 invoke dynamic: #0 #16 doit ()LI;\n" + 
			"    constant #20 utf8: \"args\"\n" + 
			"    constant #21 utf8: \"[Ljava/lang/String;\"\n" + 
			"    constant #22 utf8: \"i\"\n" + 
			"    constant #23 utf8: \"LI;\"\n" + 
			"    constant #24 utf8: \"lambda$0\"\n" + 
			"    constant #25 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)V\"\n" + 
			"    constant #26 utf8: \"RuntimeVisibleParameterAnnotations\"\n" + 
			"    constant #27 utf8: \"LAnnotation;\"\n" + 
			"    constant #28 utf8: \"o\"\n" + 
			"    constant #29 utf8: \"Ljava/lang/Object;\"\n" + 
			"    constant #30 utf8: \"p\"\n" + 
			"    constant #31 utf8: \"SourceFile\"\n" + 
			"    constant #32 utf8: \"X.java\"\n" + 
			"    constant #33 method_ref: #34.#36 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #34 class: #35 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #35 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #36 name_and_type: #37.#38 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #37 utf8: \"metafactory\"\n" + 
			"    constant #38 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #39 method handle: invokestatic (6) #33 \n" + 
			"    constant #40 utf8: \"BootstrapMethods\"\n" + 
			"    constant #41 method type: #25 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #42 method_ref: #1.#43 X.lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #43 name_and_type: #24.#25 lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #44 method handle: invokestatic (6) #42 \n" + 
			"    constant #45 method type: #25 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #46 utf8: \"InnerClasses\"\n" + 
			"    constant #47 class: #48 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #48 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #49 class: #50 java/lang/invoke/MethodHandles\n" + 
			"    constant #50 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #51 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public X();\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokespecial java.lang.Object() [8]\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  invokedynamic 0 doit() : I [19]\n" + 
			"    5  astore_1 [i]\n" + 
			"    6  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 10]\n" + 
			"        [pc: 6, line: 12]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 7] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 6, pc: 7] local: i index: 1 type: I\n" + 
			"  \n" + 
			"  // Method descriptor #25 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"  // Stack: 0, Locals: 2\n" + 
			"  private static synthetic void lambda$0(java.lang.Object o, java.lang.Object p);\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: o index: 0 type: java.lang.Object\n" + 
			"        [pc: 0, pc: 1] local: p index: 1 type: java.lang.Object\n" + 
			"    RuntimeVisibleParameterAnnotations: \n" + 
			"      Number of annotations for parameter 0: 1\n" + 
			"        #27 @Annotation(\n" + 
			"        )\n" + 
			"      Number of annotations for parameter 1: 1\n" + 
			"        #27 @Annotation(\n" + 
			"        )\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #47 java/lang/invoke/MethodHandles$Lookup, outer class info: #49 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #51 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 39 arguments: {#41,#44,#45}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test014() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"interface I {\n" +
				"	void doit (Object o, Object p);\n" +
				"}\n" +
				"public class X {\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"	};\n" +
				"   public static void main(String [] args) {\n" +
				"   int local1 = 0,  local2 = 1;\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"       int j = args.length + local1 + local2;\n" +
				"	};\n" +
				"}\n" +
				"}\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Annotation {\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"i\"\n" + 
			"    constant #6 utf8: \"LI;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"()V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" + 
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" + 
			"    constant #12 name_and_type: #13.#14 doit ()LI;\n" + 
			"    constant #13 utf8: \"doit\"\n" + 
			"    constant #14 utf8: \"()LI;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 doit ()LI;\n" + 
			"    constant #16 field_ref: #1.#17 X.i LI;\n" + 
			"    constant #17 name_and_type: #5.#6 i LI;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"this\"\n" + 
			"    constant #21 utf8: \"LX;\"\n" + 
			"    constant #22 utf8: \"main\"\n" + 
			"    constant #23 utf8: \"([Ljava/lang/String;)V\"\n" + 
			"    constant #24 name_and_type: #13.#25 doit ([Ljava/lang/String;II)LI;\n" + 
			"    constant #25 utf8: \"([Ljava/lang/String;II)LI;\"\n" + 
			"    constant #26 invoke dynamic: #1 #24 doit ([Ljava/lang/String;II)LI;\n" + 
			"    constant #27 utf8: \"args\"\n" + 
			"    constant #28 utf8: \"[Ljava/lang/String;\"\n" + 
			"    constant #29 utf8: \"local1\"\n" + 
			"    constant #30 utf8: \"I\"\n" + 
			"    constant #31 utf8: \"local2\"\n" + 
			"    constant #32 utf8: \"lambda$0\"\n" + 
			"    constant #33 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)V\"\n" + 
			"    constant #34 utf8: \"RuntimeVisibleParameterAnnotations\"\n" + 
			"    constant #35 utf8: \"LAnnotation;\"\n" + 
			"    constant #36 utf8: \"o\"\n" + 
			"    constant #37 utf8: \"Ljava/lang/Object;\"\n" + 
			"    constant #38 utf8: \"p\"\n" + 
			"    constant #39 utf8: \"lambda$1\"\n" + 
			"    constant #40 utf8: \"([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\"\n" + 
			"    constant #41 utf8: \"SourceFile\"\n" + 
			"    constant #42 utf8: \"X.java\"\n" + 
			"    constant #43 method_ref: #44.#46 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #44 class: #45 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #45 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #46 name_and_type: #47.#48 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #47 utf8: \"metafactory\"\n" + 
			"    constant #48 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #49 method handle: invokestatic (6) #43 \n" + 
			"    constant #50 utf8: \"BootstrapMethods\"\n" + 
			"    constant #51 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #52 method_ref: #1.#53 X.lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #53 name_and_type: #32.#33 lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #54 method handle: invokestatic (6) #52 \n" + 
			"    constant #55 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #56 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #57 method_ref: #1.#58 X.lambda$1 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #58 name_and_type: #39.#40 lambda$1 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #59 method handle: invokestatic (6) #57 \n" + 
			"    constant #60 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"    constant #61 utf8: \"InnerClasses\"\n" + 
			"    constant #62 class: #63 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #63 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #64 class: #65 java/lang/invoke/MethodHandles\n" + 
			"    constant #65 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #66 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LI;\n" + 
			"  I i;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public X();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial java.lang.Object() [10]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokedynamic 0 doit() : I [15]\n" + 
			"    10  putfield X.i : I [16]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"        [pc: 4, line: 9]\n" + 
			"        [pc: 13, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #23 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 3, Locals: 4\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [local1]\n" + 
			"     2  iconst_1\n" + 
			"     3  istore_2 [local2]\n" + 
			"     4  aload_0 [args]\n" + 
			"     5  iload_1 [local1]\n" + 
			"     6  iload_2 [local2]\n" + 
			"     7  invokedynamic 1 doit(java.lang.String[], int, int) : I [26]\n" + 
			"    12  astore_3 [i]\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 12]\n" + 
			"        [pc: 4, line: 13]\n" + 
			"        [pc: 13, line: 16]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 14] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 14] local: local1 index: 1 type: int\n" + 
			"        [pc: 4, pc: 14] local: local2 index: 2 type: int\n" + 
			"        [pc: 13, pc: 14] local: i index: 3 type: I\n" + 
			"  \n" + 
			"  // Method descriptor #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" + 
			"  // Stack: 0, Locals: 2\n" + 
			"  private static synthetic void lambda$0(java.lang.Object o, java.lang.Object p);\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: o index: 0 type: java.lang.Object\n" + 
			"        [pc: 0, pc: 1] local: p index: 1 type: java.lang.Object\n" + 
			"    RuntimeVisibleParameterAnnotations: \n" + 
			"      Number of annotations for parameter 0: 1\n" + 
			"        #35 @Annotation(\n" + 
			"        )\n" + 
			"      Number of annotations for parameter 1: 1\n" + 
			"        #35 @Annotation(\n" + 
			"        )\n" + 
			"  \n" + 
			"  // Method descriptor #40 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" + 
			"  // Stack: 2, Locals: 6\n" + 
			"  private static synthetic void lambda$1(java.lang.String[] arg0, int arg1, int arg2, java.lang.Object o, java.lang.Object p);\n" + 
			"    0  aload_0 [arg0]\n" + 
			"    1  arraylength\n" + 
			"    2  iload_1 [arg1]\n" + 
			"    3  iadd\n" + 
			"    4  iload_2 [arg2]\n" + 
			"    5  iadd\n" + 
			"    6  istore 5\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 14]\n" + 
			"        [pc: 8, line: 15]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 9] local: o index: 3 type: java.lang.Object\n" + 
			"        [pc: 0, pc: 9] local: p index: 4 type: java.lang.Object\n" + 
			"    RuntimeVisibleParameterAnnotations: \n" + 
			"      Number of annotations for parameter 0: 0\n" + 
			"      Number of annotations for parameter 1: 0\n" + 
			"      Number of annotations for parameter 2: 0\n" + 
			"      Number of annotations for parameter 3: 1\n" + 
			"        #35 @Annotation(\n" + 
			"        )\n" + 
			"      Number of annotations for parameter 4: 1\n" + 
			"        #35 @Annotation(\n" + 
			"        )\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #62 java/lang/invoke/MethodHandles$Lookup, outer class info: #64 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #66 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 49 arguments: {#51,#54,#55},\n" + 
			"  1 : # 49 arguments: {#56,#59,#60}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation for intersection cast.
public void test015() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main( String [] args) { \n" +
				"		I i = (I & java.io.Serializable) () -> {};\n" +
				"	}\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"public class X {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X\n" + 
			"    constant #2 utf8: \"X\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"<init>\"\n" + 
			"    constant #6 utf8: \"()V\"\n" + 
			"    constant #7 utf8: \"Code\"\n" + 
			"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" + 
			"    constant #9 name_and_type: #5.#6 <init> ()V\n" + 
			"    constant #10 utf8: \"LineNumberTable\"\n" + 
			"    constant #11 utf8: \"LocalVariableTable\"\n" + 
			"    constant #12 utf8: \"this\"\n" + 
			"    constant #13 utf8: \"LX;\"\n" + 
			"    constant #14 utf8: \"main\"\n" + 
			"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" + 
			"    constant #16 name_and_type: #17.#18 foo ()LI;\n" + 
			"    constant #17 utf8: \"foo\"\n" + 
			"    constant #18 utf8: \"()LI;\"\n" + 
			"    constant #19 invoke dynamic: #0 #16 foo ()LI;\n" + 
			"    constant #20 utf8: \"args\"\n" + 
			"    constant #21 utf8: \"[Ljava/lang/String;\"\n" + 
			"    constant #22 utf8: \"i\"\n" + 
			"    constant #23 utf8: \"LI;\"\n" + 
			"    constant #24 utf8: \"lambda$0\"\n" + 
			"    constant #25 utf8: \"SourceFile\"\n" + 
			"    constant #26 utf8: \"X.java\"\n" + 
			"    constant #27 method_ref: #28.#30 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #28 class: #29 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #29 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #30 name_and_type: #31.#32 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #31 utf8: \"metafactory\"\n" + 
			"    constant #32 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #33 method handle: invokestatic (6) #27 \n" + 
			"    constant #34 utf8: \"BootstrapMethods\"\n" + 
			"    constant #35 method type: #6 ()V\n" + 
			"    constant #36 method_ref: #1.#37 X.lambda$0 ()V\n" + 
			"    constant #37 name_and_type: #24.#6 lambda$0 ()V\n" + 
			"    constant #38 method handle: invokestatic (6) #36 \n" + 
			"    constant #39 method type: #6 ()V\n" + 
			"    constant #40 utf8: \"InnerClasses\"\n" + 
			"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" + 
			"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #45 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public X();\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokespecial java.lang.Object() [8]\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  invokedynamic 0 foo() : I [19]\n" + 
			"    5  astore_1 [i]\n" + 
			"    6  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 6, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 7] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 6, pc: 7] local: i index: 1 type: I\n" + 
			"  \n" + 
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 0\n" + 
			"  private static synthetic void lambda$0();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #45 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 33 arguments: {#35,#38,#39}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406392, [1.8][compiler][codegen] Improve identification of lambdas that must capture enclosing instance
public void test016() throws Exception {
	// This test proves that when a lambda body references a type variable of an enclosing method, it can still be emitted as a static method. 
	this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"}\n" +
				"public class X  {\n" +
				"	<T> void foo() {\n" +
				"		class Y {\n" +
				"			T goo() {\n" +
				"				((I) () -> {\n" +
				"	    			T t = null;\n" +
				"		    		System.out.println(\"Lambda\");\n" +
				"				}).doit();\n" +
				"				return null;\n" +
				"			}\n" +
				"		}\n" +
				"		new Y().goo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().<String>foo(); \n" +
				"	}\n" +
				"}\n",
		},
		"Lambda");

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"class X$1Y {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X$1Y\n" + 
			"    constant #2 utf8: \"X$1Y\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"this$0\"\n" + 
			"    constant #6 utf8: \"LX;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"(LX;)V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 field_ref: #1.#11 X$1Y.this$0 LX;\n" + 
			"    constant #11 name_and_type: #5.#6 this$0 LX;\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #7.#14 <init> ()V\n" + 
			"    constant #14 utf8: \"()V\"\n" + 
			"    constant #15 utf8: \"LineNumberTable\"\n" + 
			"    constant #16 utf8: \"LocalVariableTable\"\n" + 
			"    constant #17 utf8: \"this\"\n" + 
			"    constant #18 utf8: \"LX$1Y;\"\n" + 
			"    constant #19 utf8: \"goo\"\n" + 
			"    constant #20 utf8: \"()Ljava/lang/Object;\"\n" + 
			"    constant #21 utf8: \"Signature\"\n" + 
			"    constant #22 utf8: \"()TT;\"\n" + 
			"    constant #23 name_and_type: #24.#25 doit ()LI;\n" + 
			"    constant #24 utf8: \"doit\"\n" + 
			"    constant #25 utf8: \"()LI;\"\n" + 
			"    constant #26 invoke dynamic: #0 #23 doit ()LI;\n" + 
			"    constant #27 interface_method_ref: #28.#30 I.doit ()V\n" + 
			"    constant #28 class: #29 I\n" + 
			"    constant #29 utf8: \"I\"\n" + 
			"    constant #30 name_and_type: #24.#14 doit ()V\n" + 
			"    constant #31 utf8: \"lambda$0\"\n" + 
			"    constant #32 field_ref: #33.#35 java/lang/System.out Ljava/io/PrintStream;\n" + 
			"    constant #33 class: #34 java/lang/System\n" + 
			"    constant #34 utf8: \"java/lang/System\"\n" + 
			"    constant #35 name_and_type: #36.#37 out Ljava/io/PrintStream;\n" + 
			"    constant #36 utf8: \"out\"\n" + 
			"    constant #37 utf8: \"Ljava/io/PrintStream;\"\n" + 
			"    constant #38 string: #39 \"Lambda\"\n" + 
			"    constant #39 utf8: \"Lambda\"\n" + 
			"    constant #40 method_ref: #41.#43 java/io/PrintStream.println (Ljava/lang/String;)V\n" + 
			"    constant #41 class: #42 java/io/PrintStream\n" + 
			"    constant #42 utf8: \"java/io/PrintStream\"\n" + 
			"    constant #43 name_and_type: #44.#45 println (Ljava/lang/String;)V\n" + 
			"    constant #44 utf8: \"println\"\n" + 
			"    constant #45 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #46 utf8: \"t\"\n" + 
			"    constant #47 utf8: \"Ljava/lang/Object;\"\n" + 
			"    constant #48 utf8: \"LocalVariableTypeTable\"\n" + 
			"    constant #49 utf8: \"TT;\"\n" + 
			"    constant #50 utf8: \"SourceFile\"\n" + 
			"    constant #51 utf8: \"X.java\"\n" + 
			"    constant #52 utf8: \"EnclosingMethod\"\n" + 
			"    constant #53 class: #54 X\n" + 
			"    constant #54 utf8: \"X\"\n" + 
			"    constant #55 name_and_type: #56.#14 foo ()V\n" + 
			"    constant #56 utf8: \"foo\"\n" + 
			"    constant #57 method_ref: #58.#60 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #58 class: #59 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #59 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #60 name_and_type: #61.#62 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #61 utf8: \"metafactory\"\n" + 
			"    constant #62 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #63 method handle: invokestatic (6) #57 \n" + 
			"    constant #64 utf8: \"BootstrapMethods\"\n" + 
			"    constant #65 method type: #14 ()V\n" + 
			"    constant #66 method_ref: #1.#67 X$1Y.lambda$0 ()V\n" + 
			"    constant #67 name_and_type: #31.#14 lambda$0 ()V\n" + 
			"    constant #68 method handle: invokestatic (6) #66 \n" + 
			"    constant #69 method type: #14 ()V\n" + 
			"    constant #70 utf8: \"InnerClasses\"\n" + 
			"    constant #71 utf8: \"Y\"\n" + 
			"    constant #72 class: #73 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #74 class: #75 java/lang/invoke/MethodHandles\n" + 
			"    constant #75 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #76 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LX;\n" + 
			"  final synthetic X this$0;\n" + 
			"  \n" + 
			"  // Method descriptor #8 (LX;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  X$1Y(X arg0);\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  aload_1 [arg0]\n" + 
			"     2  putfield X$1Y.this$0 : X [10]\n" + 
			"     5  aload_0 [this]\n" + 
			"     6  invokespecial java.lang.Object() [12]\n" + 
			"     9  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 10] local: this index: 0 type: new X(){}\n" + 
			"  \n" + 
			"  // Method descriptor #20 ()Ljava/lang/Object;\n" + 
			"  // Signature: ()TT;\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  java.lang.Object goo();\n" + 
			"     0  invokedynamic 0 doit() : I [26]\n" + 
			"     5  invokeinterface I.doit() : void [27] [nargs: 1]\n" + 
			"    10  aconst_null\n" + 
			"    11  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"        [pc: 5, line: 11]\n" + 
			"        [pc: 10, line: 12]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 12] local: this index: 0 type: new X(){}\n" + 
			"  \n" + 
			"  // Method descriptor #14 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  private static synthetic void lambda$0();\n" + 
			"     0  aconst_null\n" + 
			"     1  astore_0 [t]\n" + 
			"     2  getstatic java.lang.System.out : java.io.PrintStream [32]\n" + 
			"     5  ldc <String \"Lambda\"> [38]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [40]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"        [pc: 10, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 10] local: t index: 0 type: java.lang.Object\n" + 
			"      Local variable type table:\n" + 
			"        [pc: 2, pc: 10] local: t index: 0 type: T\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #1 X$1Y, outer class info: #0\n" + 
			"     inner name: #71 Y, accessflags: 0 default],\n" + 
			"    [inner class info: #72 java/lang/invoke/MethodHandles$Lookup, outer class info: #74 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #76 Lookup, accessflags: 25 public static final]\n" + 
			"  Enclosing Method: #53  #55 X.foo()V\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 63 arguments: {#65,#68,#69}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406392, [1.8][compiler][codegen] Improve identification of lambdas that must capture enclosing instance
public void test017() throws Exception {
	// This test proves that when a lambda body references a type variable of an enclosing class, it can still be emitted as a static method. 
	this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"}\n" +
				"public class X<T>  {\n" +
				"	void foo() {\n" +
				"		class Y {\n" +
				"			T goo() {\n" +
				"				((I) () -> {\n" +
				"				T t = null;\n" +
				"				System.out.println(\"Lambda\");     \n" +
				"				}).doit();\n" +
				"				return null;\n" +
				"			}\n" +
				"		}\n" +
				"		new Y().goo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().<String>foo(); \n" +
				"	}\n" +
				"}\n",
		},
		"Lambda");

	String expectedOutput =
			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
			"class X$1Y {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 X$1Y\n" + 
			"    constant #2 utf8: \"X$1Y\"\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: \"java/lang/Object\"\n" + 
			"    constant #5 utf8: \"this$0\"\n" + 
			"    constant #6 utf8: \"LX;\"\n" + 
			"    constant #7 utf8: \"<init>\"\n" + 
			"    constant #8 utf8: \"(LX;)V\"\n" + 
			"    constant #9 utf8: \"Code\"\n" + 
			"    constant #10 field_ref: #1.#11 X$1Y.this$0 LX;\n" + 
			"    constant #11 name_and_type: #5.#6 this$0 LX;\n" + 
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" + 
			"    constant #13 name_and_type: #7.#14 <init> ()V\n" + 
			"    constant #14 utf8: \"()V\"\n" + 
			"    constant #15 utf8: \"LineNumberTable\"\n" + 
			"    constant #16 utf8: \"LocalVariableTable\"\n" + 
			"    constant #17 utf8: \"this\"\n" + 
			"    constant #18 utf8: \"LX$1Y;\"\n" + 
			"    constant #19 utf8: \"goo\"\n" + 
			"    constant #20 utf8: \"()Ljava/lang/Object;\"\n" + 
			"    constant #21 utf8: \"Signature\"\n" + 
			"    constant #22 utf8: \"()TT;\"\n" + 
			"    constant #23 name_and_type: #24.#25 doit ()LI;\n" + 
			"    constant #24 utf8: \"doit\"\n" + 
			"    constant #25 utf8: \"()LI;\"\n" + 
			"    constant #26 invoke dynamic: #0 #23 doit ()LI;\n" + 
			"    constant #27 interface_method_ref: #28.#30 I.doit ()V\n" + 
			"    constant #28 class: #29 I\n" + 
			"    constant #29 utf8: \"I\"\n" + 
			"    constant #30 name_and_type: #24.#14 doit ()V\n" + 
			"    constant #31 utf8: \"lambda$0\"\n" + 
			"    constant #32 field_ref: #33.#35 java/lang/System.out Ljava/io/PrintStream;\n" + 
			"    constant #33 class: #34 java/lang/System\n" + 
			"    constant #34 utf8: \"java/lang/System\"\n" + 
			"    constant #35 name_and_type: #36.#37 out Ljava/io/PrintStream;\n" + 
			"    constant #36 utf8: \"out\"\n" + 
			"    constant #37 utf8: \"Ljava/io/PrintStream;\"\n" + 
			"    constant #38 string: #39 \"Lambda\"\n" + 
			"    constant #39 utf8: \"Lambda\"\n" + 
			"    constant #40 method_ref: #41.#43 java/io/PrintStream.println (Ljava/lang/String;)V\n" + 
			"    constant #41 class: #42 java/io/PrintStream\n" + 
			"    constant #42 utf8: \"java/io/PrintStream\"\n" + 
			"    constant #43 name_and_type: #44.#45 println (Ljava/lang/String;)V\n" + 
			"    constant #44 utf8: \"println\"\n" + 
			"    constant #45 utf8: \"(Ljava/lang/String;)V\"\n" + 
			"    constant #46 utf8: \"t\"\n" + 
			"    constant #47 utf8: \"Ljava/lang/Object;\"\n" + 
			"    constant #48 utf8: \"LocalVariableTypeTable\"\n" + 
			"    constant #49 utf8: \"TT;\"\n" + 
			"    constant #50 utf8: \"SourceFile\"\n" + 
			"    constant #51 utf8: \"X.java\"\n" + 
			"    constant #52 utf8: \"EnclosingMethod\"\n" + 
			"    constant #53 class: #54 X\n" + 
			"    constant #54 utf8: \"X\"\n" + 
			"    constant #55 name_and_type: #56.#14 foo ()V\n" + 
			"    constant #56 utf8: \"foo\"\n" + 
			"    constant #57 method_ref: #58.#60 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #58 class: #59 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #59 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #60 name_and_type: #61.#62 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #61 utf8: \"metafactory\"\n" + 
			"    constant #62 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #63 method handle: invokestatic (6) #57 \n" + 
			"    constant #64 utf8: \"BootstrapMethods\"\n" + 
			"    constant #65 method type: #14 ()V\n" + 
			"    constant #66 method_ref: #1.#67 X$1Y.lambda$0 ()V\n" + 
			"    constant #67 name_and_type: #31.#14 lambda$0 ()V\n" + 
			"    constant #68 method handle: invokestatic (6) #66 \n" + 
			"    constant #69 method type: #14 ()V\n" + 
			"    constant #70 utf8: \"InnerClasses\"\n" + 
			"    constant #71 utf8: \"Y\"\n" + 
			"    constant #72 class: #73 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #74 class: #75 java/lang/invoke/MethodHandles\n" + 
			"    constant #75 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #76 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LX;\n" + 
			"  final synthetic X this$0;\n" + 
			"  \n" + 
			"  // Method descriptor #8 (LX;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  X$1Y(X arg0);\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  aload_1 [arg0]\n" + 
			"     2  putfield X$1Y.this$0 : X [10]\n" + 
			"     5  aload_0 [this]\n" + 
			"     6  invokespecial java.lang.Object() [12]\n" + 
			"     9  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 10] local: this index: 0 type: new X(){}\n" + 
			"  \n" + 
			"  // Method descriptor #20 ()Ljava/lang/Object;\n" + 
			"  // Signature: ()TT;\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  java.lang.Object goo();\n" + 
			"     0  invokedynamic 0 doit() : I [26]\n" + 
			"     5  invokeinterface I.doit() : void [27] [nargs: 1]\n" + 
			"    10  aconst_null\n" + 
			"    11  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"        [pc: 5, line: 11]\n" + 
			"        [pc: 10, line: 12]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 12] local: this index: 0 type: new X(){}\n" + 
			"  \n" + 
			"  // Method descriptor #14 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  private static synthetic void lambda$0();\n" + 
			"     0  aconst_null\n" + 
			"     1  astore_0 [t]\n" + 
			"     2  getstatic java.lang.System.out : java.io.PrintStream [32]\n" + 
			"     5  ldc <String \"Lambda\"> [38]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [40]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"        [pc: 10, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 10] local: t index: 0 type: java.lang.Object\n" + 
			"      Local variable type table:\n" + 
			"        [pc: 2, pc: 10] local: t index: 0 type: T\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #1 X$1Y, outer class info: #0\n" + 
			"     inner name: #71 Y, accessflags: 0 default],\n" + 
			"    [inner class info: #72 java/lang/invoke/MethodHandles$Lookup, outer class info: #74 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #76 Lookup, accessflags: 25 public static final]\n" + 
			"  Enclosing Method: #53  #55 X.foo()V\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 63 arguments: {#65,#68,#69}\n" + 
			"}";

	verifyClassFile(expectedOutput, "X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
}
public static Class testClass() {
	return Jsr335ClassFileTest.class;
}
}
