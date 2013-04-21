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
		"    constant #12 name_and_type: #13.#14 lambda ()Ljava/lang/Runnable;\n" + 
		"    constant #13 utf8: \"lambda\"\n" + 
		"    constant #14 utf8: \"()Ljava/lang/Runnable;\"\n" + 
		"    constant #15 invoke dynamic: #0 #12 lambda ()Ljava/lang/Runnable;\n" + 
		"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" + 
		"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" + 
		"    constant #18 utf8: \"LineNumberTable\"\n" + 
		"    constant #19 utf8: \"LocalVariableTable\"\n" + 
		"    constant #20 utf8: \"this\"\n" + 
		"    constant #21 utf8: \"LX;\"\n" + 
		"    constant #22 utf8: \"SourceFile\"\n" + 
		"    constant #23 utf8: \"X.java\"\n" + 
		"    constant #24 method_ref: #25.#27 java/lang/invoke/LambdaMetafactory.metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
		"    constant #25 class: #26 java/lang/invoke/LambdaMetafactory\n" + 
		"    constant #26 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
		"    constant #27 name_and_type: #28.#29 metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
		"    constant #28 utf8: \"metaFactory\"\n" + 
		"    constant #29 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
		"    constant #30 method handle: invokestatic (6) #24 \n" + 
		"    constant #31 utf8: \"BootstrapMethods\"\n" + 
		"    constant #32 interface_method_ref: #33.#35 java/lang/Runnable.run ()V\n" + 
		"    constant #33 class: #34 java/lang/Runnable\n" + 
		"    constant #34 utf8: \"java/lang/Runnable\"\n" + 
		"    constant #35 name_and_type: #36.#8 run ()V\n" + 
		"    constant #36 utf8: \"run\"\n" + 
		"    constant #37 method handle: invokeinterface (9) #32 \n" + 
		"    constant #38 method_ref: #39.#41 java/lang/Thread.yield ()V\n" + 
		"    constant #39 class: #40 java/lang/Thread\n" + 
		"    constant #40 utf8: \"java/lang/Thread\"\n" + 
		"    constant #41 name_and_type: #42.#8 yield ()V\n" + 
		"    constant #42 utf8: \"yield\"\n" + 
		"    constant #43 method handle: invokestatic (6) #38 \n" + 
		"    constant #44 method type: #8 ()V\n" + 
		"    constant #45 utf8: \"InnerClasses\"\n" + 
		"    constant #46 class: #47 java/lang/invoke/MethodHandles$Lookup\n" + 
		"    constant #47 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
		"    constant #48 class: #49 java/lang/invoke/MethodHandles\n" + 
		"    constant #49 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
		"    constant #50 utf8: \"Lookup\"\n" + 
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
		"     5  invokedynamic 0 lambda() : java.lang.Runnable [15]\n" + 
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
		"    [inner class info: #46 java/lang/invoke/MethodHandles$Lookup, outer class info: #48 java/lang/invoke/MethodHandles\n" + 
		"     inner name: #50 Lookup, accessflags: 25 public static final]\n" + 
		"Bootstrap methods:\n" + 
		"  0 : # 30 arguments: {#37,#43,#44}\n" + 
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
		"    constant #12 name_and_type: #13.#14 lambda ()LObjectConsumer;\n" + 
		"    constant #13 utf8: \"lambda\"\n" + 
		"    constant #14 utf8: \"()LObjectConsumer;\"\n" + 
		"    constant #15 invoke dynamic: #0 #12 lambda ()LObjectConsumer;\n" + 
		"    constant #16 field_ref: #1.#17 X.referenceExpression LObjectConsumer;\n" + 
		"    constant #17 name_and_type: #5.#6 referenceExpression LObjectConsumer;\n" + 
		"    constant #18 utf8: \"LineNumberTable\"\n" + 
		"    constant #19 utf8: \"LocalVariableTable\"\n" + 
		"    constant #20 utf8: \"this\"\n" + 
		"    constant #21 utf8: \"LX;\"\n" + 
		"    constant #22 utf8: \"SourceFile\"\n" + 
		"    constant #23 utf8: \"X.java\"\n" + 
		"    constant #24 method_ref: #25.#27 java/lang/invoke/LambdaMetafactory.metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
		"    constant #25 class: #26 java/lang/invoke/LambdaMetafactory\n" + 
		"    constant #26 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
		"    constant #27 name_and_type: #28.#29 metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
		"    constant #28 utf8: \"metaFactory\"\n" + 
		"    constant #29 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
		"    constant #30 method handle: invokestatic (6) #24 \n" + 
		"    constant #31 utf8: \"BootstrapMethods\"\n" + 
		"    constant #32 interface_method_ref: #33.#35 ObjectConsumer.consume (Ljava/lang/Object;)V\n" + 
		"    constant #33 class: #34 ObjectConsumer\n" + 
		"    constant #34 utf8: \"ObjectConsumer\"\n" + 
		"    constant #35 name_and_type: #36.#37 consume (Ljava/lang/Object;)V\n" + 
		"    constant #36 utf8: \"consume\"\n" + 
		"    constant #37 utf8: \"(Ljava/lang/Object;)V\"\n" + 
		"    constant #38 method handle: invokeinterface (9) #32 \n" + 
		"    constant #39 method_ref: #40.#42 Main.printIt (Ljava/lang/Object;)V\n" + 
		"    constant #40 class: #41 Main\n" + 
		"    constant #41 utf8: \"Main\"\n" + 
		"    constant #42 name_and_type: #43.#37 printIt (Ljava/lang/Object;)V\n" + 
		"    constant #43 utf8: \"printIt\"\n" + 
		"    constant #44 method handle: invokestatic (6) #39 \n" + 
		"    constant #45 method type: #37 (Ljava/lang/Object;)V\n" + 
		"    constant #46 utf8: \"InnerClasses\"\n" + 
		"    constant #47 class: #48 java/lang/invoke/MethodHandles$Lookup\n" + 
		"    constant #48 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
		"    constant #49 class: #50 java/lang/invoke/MethodHandles\n" + 
		"    constant #50 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
		"    constant #51 utf8: \"Lookup\"\n" + 
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
		"     5  invokedynamic 0 lambda() : ObjectConsumer [15]\n" + 
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
		"    [inner class info: #47 java/lang/invoke/MethodHandles$Lookup, outer class info: #49 java/lang/invoke/MethodHandles\n" + 
		"     inner name: #51 Lookup, accessflags: 25 public static final]\n" + 
		"Bootstrap methods:\n" + 
		"  0 : # 30 arguments: {#38,#44,#45}\n" + 
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
			"    constant #12 name_and_type: #13.#14 lambda ()LObjectToString;\n" + 
			"    constant #13 utf8: \"lambda\"\n" + 
			"    constant #14 utf8: \"()LObjectToString;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 lambda ()LObjectToString;\n" + 
			"    constant #16 field_ref: #1.#17 X.referenceExpression LObjectToString;\n" + 
			"    constant #17 name_and_type: #5.#6 referenceExpression LObjectToString;\n" + 
			"    constant #18 utf8: \"LineNumberTable\"\n" + 
			"    constant #19 utf8: \"LocalVariableTable\"\n" + 
			"    constant #20 utf8: \"this\"\n" + 
			"    constant #21 utf8: \"LX;\"\n" + 
			"    constant #22 utf8: \"SourceFile\"\n" + 
			"    constant #23 utf8: \"X.java\"\n" + 
			"    constant #24 method_ref: #25.#27 java/lang/invoke/LambdaMetafactory.metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #25 class: #26 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #26 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #27 name_and_type: #28.#29 metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #28 utf8: \"metaFactory\"\n" + 
			"    constant #29 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #30 method handle: invokestatic (6) #24 \n" + 
			"    constant #31 utf8: \"BootstrapMethods\"\n" + 
			"    constant #32 interface_method_ref: #33.#35 ObjectToString.makeString (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #33 class: #34 ObjectToString\n" + 
			"    constant #34 utf8: \"ObjectToString\"\n" + 
			"    constant #35 name_and_type: #36.#37 makeString (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #36 utf8: \"makeString\"\n" + 
			"    constant #37 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" + 
			"    constant #38 method handle: invokeinterface (9) #32 \n" + 
			"    constant #39 method_ref: #3.#40 java/lang/Object.toString ()Ljava/lang/String;\n" + 
			"    constant #40 name_and_type: #41.#42 toString ()Ljava/lang/String;\n" + 
			"    constant #41 utf8: \"toString\"\n" + 
			"    constant #42 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #43 method handle: invokevirtual (5) #39 \n" + 
			"    constant #44 method type: #37 (Ljava/lang/Object;)Ljava/lang/String;\n" + 
			"    constant #45 utf8: \"InnerClasses\"\n" + 
			"    constant #46 class: #47 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #47 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #48 class: #49 java/lang/invoke/MethodHandles\n" + 
			"    constant #49 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #50 utf8: \"Lookup\"\n" + 
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
			"     5  invokedynamic 0 lambda() : ObjectToString [15]\n" + 
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
			"    [inner class info: #46 java/lang/invoke/MethodHandles$Lookup, outer class info: #48 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #50 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 30 arguments: {#38,#43,#44}\n" + 
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
			"    constant #18 name_and_type: #19.#20 lambda (Ljava/lang/String;)LStringProducer;\n" + 
			"    constant #19 utf8: \"lambda\"\n" + 
			"    constant #20 utf8: \"(Ljava/lang/String;)LStringProducer;\"\n" + 
			"    constant #21 invoke dynamic: #0 #18 lambda (Ljava/lang/String;)LStringProducer;\n" + 
			"    constant #22 field_ref: #1.#23 X.referenceExpression LStringProducer;\n" + 
			"    constant #23 name_and_type: #7.#8 referenceExpression LStringProducer;\n" + 
			"    constant #24 utf8: \"LineNumberTable\"\n" + 
			"    constant #25 utf8: \"LocalVariableTable\"\n" + 
			"    constant #26 utf8: \"this\"\n" + 
			"    constant #27 utf8: \"LX;\"\n" + 
			"    constant #28 utf8: \"SourceFile\"\n" + 
			"    constant #29 utf8: \"X.java\"\n" + 
			"    constant #30 method_ref: #31.#33 java/lang/invoke/LambdaMetafactory.metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #31 class: #32 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #32 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #33 name_and_type: #34.#35 metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #34 utf8: \"metaFactory\"\n" + 
			"    constant #35 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #36 method handle: invokestatic (6) #30 \n" + 
			"    constant #37 utf8: \"BootstrapMethods\"\n" + 
			"    constant #38 interface_method_ref: #39.#41 StringProducer.produce ()Ljava/lang/String;\n" + 
			"    constant #39 class: #40 StringProducer\n" + 
			"    constant #40 utf8: \"StringProducer\"\n" + 
			"    constant #41 name_and_type: #42.#43 produce ()Ljava/lang/String;\n" + 
			"    constant #42 utf8: \"produce\"\n" + 
			"    constant #43 utf8: \"()Ljava/lang/String;\"\n" + 
			"    constant #44 method handle: invokeinterface (9) #38 \n" + 
			"    constant #45 method_ref: #46.#48 java/lang/String.toString ()Ljava/lang/String;\n" + 
			"    constant #46 class: #47 java/lang/String\n" + 
			"    constant #47 utf8: \"java/lang/String\"\n" + 
			"    constant #48 name_and_type: #49.#43 toString ()Ljava/lang/String;\n" + 
			"    constant #49 utf8: \"toString\"\n" + 
			"    constant #50 method handle: invokevirtual (5) #45 \n" + 
			"    constant #51 method type: #43 ()Ljava/lang/String;\n" + 
			"    constant #52 utf8: \"InnerClasses\"\n" + 
			"    constant #53 class: #54 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #55 class: #56 java/lang/invoke/MethodHandles\n" + 
			"    constant #56 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #57 utf8: \"Lookup\"\n" + 
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
			"    15  invokedynamic 0 lambda(java.lang.String) : StringProducer [21]\n" + 
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
			"    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #57 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 36 arguments: {#44,#50,#51}\n" + 
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
	verifyClassFile("// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
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
			"    constant #10 name_and_type: #11.#12 lambda ()LMainProducer;\n" + 
			"    constant #11 utf8: \"lambda\"\n" + 
			"    constant #12 utf8: \"()LMainProducer;\"\n" + 
			"    constant #13 invoke dynamic: #0 #10 lambda ()LMainProducer;\n" + 
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
			"    constant #25 method_ref: #26.#28 java/lang/invoke/LambdaMetafactory.metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #26 class: #27 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #27 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #28 name_and_type: #29.#30 metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #29 utf8: \"metaFactory\"\n" + 
			"    constant #30 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #31 method handle: invokestatic (6) #25 \n" + 
			"    constant #32 utf8: \"BootstrapMethods\"\n" + 
			"    constant #33 interface_method_ref: #34.#36 MainProducer.produce ()LMain;\n" + 
			"    constant #34 class: #35 MainProducer\n" + 
			"    constant #35 utf8: \"MainProducer\"\n" + 
			"    constant #36 name_and_type: #37.#38 produce ()LMain;\n" + 
			"    constant #37 utf8: \"produce\"\n" + 
			"    constant #38 utf8: \"()LMain;\"\n" + 
			"    constant #39 method handle: invokeinterface (9) #33 \n" + 
			"    constant #40 method_ref: #41.#20 Main.<init> ()V\n" + 
			"    constant #41 class: #42 Main\n" + 
			"    constant #42 utf8: \"Main\"\n" + 
			"    constant #43 method handle: newinvokespecial (8) #40 \n" + 
			"    constant #44 method type: #38 ()LMain;\n" + 
			"    constant #45 utf8: \"InnerClasses\"\n" + 
			"    constant #46 class: #47 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #47 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #48 class: #49 java/lang/invoke/MethodHandles\n" + 
			"    constant #49 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #50 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LMainProducer;\n" + 
			"  public static MainProducer allocatorExpression;\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()V\n" + 
			"  // Stack: 1, Locals: 0\n" + 
			"  static {};\n" + 
			"    0  invokedynamic 0 lambda() : MainProducer [13]\n" + 
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
			"    [inner class info: #46 java/lang/invoke/MethodHandles$Lookup, outer class info: #48 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #50 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 31 arguments: {#39,#43,#44}\n" + 
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
	String expected = 			"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" + 
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
			"    constant #12 name_and_type: #13.#14 lambda ()LFunction2;\n" + 
			"    constant #13 utf8: \"lambda\"\n" + 
			"    constant #14 utf8: \"()LFunction2;\"\n" + 
			"    constant #15 invoke dynamic: #0 #12 lambda ()LFunction2;\n" + 
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
			"    constant #27 method_ref: #28.#30 java/lang/invoke/LambdaMetafactory.metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #28 class: #29 java/lang/invoke/LambdaMetafactory\n" + 
			"    constant #29 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" + 
			"    constant #30 name_and_type: #31.#32 metaFactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" + 
			"    constant #31 utf8: \"metaFactory\"\n" + 
			"    constant #32 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" + 
			"    constant #33 method handle: invokestatic (6) #27 \n" + 
			"    constant #34 utf8: \"BootstrapMethods\"\n" + 
			"    constant #35 interface_method_ref: #36.#38 Function2.apply (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
			"    constant #36 class: #37 Function2\n" + 
			"    constant #37 utf8: \"Function2\"\n" + 
			"    constant #38 name_and_type: #39.#40 apply (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
			"    constant #39 utf8: \"apply\"\n" + 
			"    constant #40 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" + 
			"    constant #41 method handle: invokeinterface (9) #35 \n" + 
			"    constant #42 method_ref: #43.#45 Main.<init> (Ljava/lang/String;Ljava/lang/String;)V\n" + 
			"    constant #43 class: #44 Main\n" + 
			"    constant #44 utf8: \"Main\"\n" + 
			"    constant #45 name_and_type: #20.#46 <init> (Ljava/lang/String;Ljava/lang/String;)V\n" + 
			"    constant #46 utf8: \"(Ljava/lang/String;Ljava/lang/String;)V\"\n" + 
			"    constant #47 method handle: newinvokespecial (8) #42 \n" + 
			"    constant #48 utf8: \"(Ljava/lang/String;Ljava/lang/String;)LMain;\"\n" + 
			"    constant #49 method type: #48 (Ljava/lang/String;Ljava/lang/String;)LMain;\n" + 
			"    constant #50 utf8: \"InnerClasses\"\n" + 
			"    constant #51 class: #52 java/lang/invoke/MethodHandles$Lookup\n" + 
			"    constant #52 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" + 
			"    constant #53 class: #54 java/lang/invoke/MethodHandles\n" + 
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles\"\n" + 
			"    constant #55 utf8: \"Lookup\"\n" + 
			"  \n" + 
			"  // Field descriptor #6 LFunction2;\n" + 
			"  // Signature: LFunction2<LMain;Ljava/lang/String;Ljava/lang/String;>;\n" + 
			"  public static Function2 producer;\n" + 
			"  \n" + 
			"  // Method descriptor #10 ()V\n" + 
			"  // Stack: 1, Locals: 0\n" + 
			"  static {};\n" + 
			"    0  invokedynamic 0 lambda() : Function2 [15]\n" + 
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
			"    [inner class info: #51 java/lang/invoke/MethodHandles$Lookup, outer class info: #53 java/lang/invoke/MethodHandles\n" + 
			"     inner name: #55 Lookup, accessflags: 25 public static final]\n" + 
			"Bootstrap methods:\n" + 
			"  0 : # 33 arguments: {#41,#47,#49}\n" + 
			"}";
	verifyClassFile(expected, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public static Class testClass() {
	return Jsr335ClassFileTest.class;
}
}
