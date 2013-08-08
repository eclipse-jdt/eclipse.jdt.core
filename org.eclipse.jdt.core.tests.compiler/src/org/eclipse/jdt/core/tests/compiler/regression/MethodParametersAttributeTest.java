/*******************************************************************************
 * Copyright (c) 2013 Jesper Steen Moeller and others.
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
 *     Jesper Steen Moeller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

public class MethodParametersAttributeTest extends AbstractRegressionTest {
	public MethodParametersAttributeTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return MethodParametersAttributeTest.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug359495" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	
	String originalSource =
		"import java.util.concurrent.Callable;\n" + 
		"\n" + 
		"public class ParameterNames {\n" + 
		"	\n" + 
		"	public void someMethod(int simple, final double complex) {\n" + 
		"	}\n" + 
		"	\n" + 
		"	public Callable<String> makeInnerWithCapture(final String finalMessage, String mutableMessage) {\n" + 
		"		return new Callable<String>()  {\n" + 
		"			public String call() throws Exception {\n" + 
		"				return finalMessage;\n" + 
		"			}\n" + 
		"		};\n" + 
		"	}\n" + 
		"\n" + 
		"	public int localMath(final String finalMessage, String mutableMessage) {\n" + 
		"		int capturedB = 42;\n" + 
		"		\n" + 
		"		class Local {\n" + 
		"			int fieldA;\n" + 
		"			Local(int a) {\n" + 
		"				this.fieldA = a;\n" + 
		"			}\n" + 
		"			int calculate(final int parameterC) {\n" + 
		"				return  this.fieldA + capturedB + parameterC;\n" + 
		"			}\n" + 
		"		}\n" + 
		"		\n" + 
		"		return new Local(2).calculate(3);\n" + 
		"	}\n" + 
		"\n" + 
		"}\n" + 
		"";

	public void test001() throws Exception {

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames.class";
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"// Compiled from ParameterNames.java (version 1.8 : 52.0, super bit)\n" + 
					"public class ParameterNames {\n" + 
					"  \n" + 
					"  // Method descriptor #12 ()V\n" + 
					"  // Stack: 1, Locals: 1\n" + 
					"  public ParameterNames();\n" + 
					"    0  aload_0 [this]\n" + 
					"    1  invokespecial java.lang.Object() [1]\n" + 
					"    4  return\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 3]\n" + 
					"  \n" + 
					"  // Method descriptor #16 (ID)V\n" + 
					"  // Stack: 0, Locals: 4\n" + 
					"  public void someMethod(int simple, double complex);\n" + 
					"    0  return\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 6]\n" + 
					"      Method Parameters:\n" + 
					"        simple\n" + 
					"        final complex\n" + 
					"  \n" + 
					"  // Method descriptor #21 (Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable;\n" + 
					"  // Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable<Ljava/lang/String;>;\n" + 
					"  // Stack: 4, Locals: 3\n" + 
					"  public java.util.concurrent.Callable makeInnerWithCapture(java.lang.String finalMessage, java.lang.String mutableMessage);\n" + 
					"     0  new ParameterNames$1 [2]\n" + 
					"     3  dup\n" + 
					"     4  aload_0 [this]\n" + 
					"     5  aload_1 [finalMessage]\n" + 
					"     6  invokespecial ParameterNames$1(ParameterNames, java.lang.String) [3]\n" + 
					"     9  areturn\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 9]\n" + 
					"      Method Parameters:\n" + 
					"        final finalMessage\n" + 
					"        mutableMessage\n" + 
					"  \n" + 
					"  // Method descriptor #27 (Ljava/lang/String;Ljava/lang/String;)I\n" + 
					"  // Stack: 5, Locals: 4\n" + 
					"  public int localMath(java.lang.String finalMessage, java.lang.String mutableMessage);\n" + 
					"     0  bipush 42\n" + 
					"     2  istore_3\n" + 
					"     3  new ParameterNames$1Local [4]\n" + 
					"     6  dup\n" + 
					"     7  aload_0 [this]\n" + 
					"     8  iconst_2\n" + 
					"     9  iload_3\n" + 
					"    10  invokespecial ParameterNames$1Local(ParameterNames, int, int) [5]\n" + 
					"    13  iconst_3\n" + 
					"    14  invokevirtual ParameterNames$1Local.calculate(int) : int [6]\n" + 
					"    17  ireturn\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 17]\n" + 
					"        [pc: 3, line: 29]\n" + 
					"      Method Parameters:\n" + 
					"        final finalMessage\n" + 
					"        mutableMessage\n" + 
					"\n" + 
					"  Inner classes:\n" + 
					"    [inner class info: #4 ParameterNames$1Local, outer class info: #0\n" + 
					"     inner name: #9 Local, accessflags: 0 default],\n" + 
					"    [inner class info: #2 ParameterNames$1, outer class info: #0\n" + 
					"     inner name: #0, accessflags: 0 default]\n" + 
					"}";


			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test002() throws Exception {

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames$1.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"// Compiled from ParameterNames.java (version 1.8 : 52.0, super bit)\n" + 
			"// Signature: Ljava/lang/Object;Ljava/util/concurrent/Callable<Ljava/lang/String;>;\n" + 
			"class ParameterNames$1 implements java.util.concurrent.Callable {\n" + 
			"  \n" + 
			"  // Field descriptor #9 Ljava/lang/String;\n" + 
			"  final synthetic java.lang.String val$finalMessage;\n" + 
			"  \n" + 
			"  // Field descriptor #11 LParameterNames;\n" + 
			"  final synthetic ParameterNames this$0;\n" + 
			"  \n" + 
			"  // Method descriptor #13 (LParameterNames;Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  ParameterNames$1(ParameterNames this$0, java.lang.String val$finalMessage);\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  aload_1 [this$0]\n" + 
			"     2  putfield ParameterNames$1.this$0 : ParameterNames [1]\n" + 
			"     5  aload_0 [this]\n" + 
			"     6  aload_2 [val$finalMessage]\n" + 
			"     7  putfield ParameterNames$1.val$finalMessage : java.lang.String [2]\n" + 
			"    10  aload_0 [this]\n" + 
			"    11  invokespecial java.lang.Object() [3]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"      Method Parameters:\n" + 
			"        final mandated this$0\n" + 
			"        final synthetic val$finalMessage\n" + 
			"  \n" + 
			"  // Method descriptor #18 ()Ljava/lang/String;\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public java.lang.String call() throws java.lang.Exception;\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  getfield ParameterNames$1.val$finalMessage : java.lang.String [2]\n" + 
			"    4  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 11]\n" + 
			"  \n" + 
			"  // Method descriptor #21 ()Ljava/lang/Object;\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public bridge synthetic java.lang.Object call() throws java.lang.Exception;\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  invokevirtual ParameterNames$1.call() : java.lang.String [4]\n" + 
			"    4  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #5 ParameterNames$1, outer class info: #0\n" + 
			"     inner name: #0, accessflags: 0 default]\n" + 
			"  Enclosing Method: #27  #28 ParameterNames.makeInnerWithCapture(Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable;\n" + 
			"}";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test003() throws Exception {

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames$1Local.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"// Compiled from ParameterNames.java (version 1.8 : 52.0, super bit)\n" + 
			"class ParameterNames$1Local {\n" + 
			"  \n" + 
			"  // Field descriptor #8 I\n" + 
			"  int fieldA;\n" + 
			"  \n" + 
			"  // Field descriptor #8 I\n" + 
			"  final synthetic int val$capturedB;\n" + 
			"  \n" + 
			"  // Field descriptor #11 LParameterNames;\n" + 
			"  final synthetic ParameterNames this$0;\n" + 
			"  \n" + 
			"  // Method descriptor #13 (LParameterNames;II)V\n" + 
			"  // Signature: (I)V\n" + 
			"  // Stack: 2, Locals: 4\n" + 
			"  ParameterNames$1Local(ParameterNames this$0, int val$capturedB, int a);\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  aload_1 [this$0]\n" + 
			"     2  putfield ParameterNames$1Local.this$0 : ParameterNames [1]\n" + 
			"     5  aload_0 [this]\n" + 
			"     6  iload_3 [a]\n" + 
			"     7  putfield ParameterNames$1Local.val$capturedB : int [2]\n" + 
			"    10  aload_0 [this]\n" + 
			"    11  invokespecial java.lang.Object() [3]\n" + 
			"    14  aload_0 [this]\n" + 
			"    15  iload_2 [val$capturedB]\n" + 
			"    16  putfield ParameterNames$1Local.fieldA : int [4]\n" + 
			"    19  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 21]\n" + 
			"        [pc: 14, line: 22]\n" + 
			"        [pc: 19, line: 23]\n" + 
			"      Method Parameters:\n" + 
			"        final mandated this$0\n" + 
			"        final synthetic val$capturedB\n" + 
			"        a\n" + 
			"  \n" + 
			"  // Method descriptor #21 (I)I\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  int calculate(int parameterC);\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  getfield ParameterNames$1Local.fieldA : int [4]\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  getfield ParameterNames$1Local.val$capturedB : int [2]\n" + 
			"     8  iadd\n" + 
			"     9  iload_1 [parameterC]\n" + 
			"    10  iadd\n" + 
			"    11  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 25]\n" + 
			"      Method Parameters:\n" + 
			"        final parameterC\n" + 
			"\n" + 
			"  Inner classes:\n" + 
			"    [inner class info: #5 ParameterNames$1Local, outer class info: #0\n" + 
			"     inner name: #33 Local, accessflags: 0 default]\n" + 
			"  Enclosing Method: #26  #27 ParameterNames.localMath(Ljava/lang/String;Ljava/lang/String;)I\n" + 
			"}";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test004() throws Exception {

		// Test the results of the ClassFileReader
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames.class";
		
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = ClassFileReader.read(path);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertEquals("Must have four methods", 4, length);
		assertEquals("finalMessage", new String(methodInfos[2].getArgumentNames()[0]));
		assertEquals("mutableMessage", new String(methodInfos[2].getArgumentNames()[1]));
	}

	public void test005() throws Exception {
		// Test the results of the ClassFileReader where some of the paramers are synthetic and/or mandated
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames$1Local.class";
		
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = ClassFileReader.read(path);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertEquals("Must have two methods", 2, length);
		assertEquals("this$0", new String(methodInfos[0].getArgumentNames()[0]));
		assertEquals("val$capturedB", new String(methodInfos[0].getArgumentNames()[1]));
	}

}
