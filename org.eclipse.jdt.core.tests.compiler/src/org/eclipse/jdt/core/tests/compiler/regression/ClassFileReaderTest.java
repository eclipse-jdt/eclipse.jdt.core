/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class ClassFileReaderTest extends AbstractComparableTest {
	private static final String EVAL_DIRECTORY = Util.getOutputDirectory()  + File.separator + "eval";
	private static final String SOURCE_DIRECTORY = Util.getOutputDirectory()  + File.separator + "source";
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 72, 73, 74 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildTestSuite(testClass());
	}
	public static Class testClass() {
		return ClassFileReaderTest.class;
	}

	public ClassFileReaderTest(String name) {
		super(name);
	}
	private void checkClassFile(String compliance, String directoryName, String className, String source, String expectedOutput, int mode) {
		compileAndDeploy(compliance, source, directoryName, className);
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, className + ".class");
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
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		} finally {
			removeTempClass(className);
		}
	}
	private void checkClassFile(String compliance, String className, String source, String expectedOutput) {
		this.checkClassFile(compliance, "", className, source, expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	private void checkClassFile(String className, String source, String expectedOutput) {
		checkClassFile("1.4", className, source, expectedOutput);
	}
	
	public void compileAndDeploy(String compliance, String source, String directoryName, String className) {
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		if (directoryName != null && directoryName.length() != 0) {
			directory = new File(SOURCE_DIRECTORY, directoryName);
			if (!directory.exists()) {
				if (!directory.mkdirs()) {
					System.out.println("Could not create " + directory);
					return;
				}
			}
		}
		String fileName = directory.getAbsolutePath() + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuffer buffer = new StringBuffer();
		if (JavaCore.VERSION_1_5.equals(compliance)) {
			buffer
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EVAL_DIRECTORY)
			.append("\" -1.5 -preserveAllLocals -nowarn -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(SOURCE_DIRECTORY)
			.append("\"");
		} else {
			buffer
				.append("\"")
				.append(fileName)
				.append("\" -d \"")
				.append(EVAL_DIRECTORY)
				.append("\" -1.4 -source 1.3 -target 1.2 -preserveAllLocals -nowarn -g -classpath \"")
				.append(Util.getJavaClassLibsAsString())
				.append(SOURCE_DIRECTORY)
				.append("\"");
		}
		org.eclipse.jdt.internal.compiler.batch.Main.compile(buffer.toString());
	}
	
	public void compileAndDeploy(String source, String className) {
		compileAndDeploy("1.4", source, "", className);
	}

	public void removeTempClass(String className) {
		File dir = new File(SOURCE_DIRECTORY);
		String[] fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					new File(SOURCE_DIRECTORY + File.separator + fileNames[i]).delete();
				}
			}
		}
		
		dir = new File(EVAL_DIRECTORY);
		fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					new File(EVAL_DIRECTORY + File.separator + fileNames[i]).delete();
				}
			}
		}
	
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=15051
	 */
	public void test001() {
		String source =
			"public class A001 {\n" +
			"	private int i = 6;\n" +
			"	public int foo() {\n" +
			"		class A {\n" +
			"			int get() {\n" +
			"				return i;\n" +
			"			}\n" +
			"		}\n" +
			"		return new A().get();\n" +
			"	}\n" +
			"};";
		String expectedOutput = 
			"  // Method descriptor #19 ()I\n" + 
			"  // Stack: 3, Locals: 1\n" + 
			"  public int foo();\n" + 
			"     0  new A001$1$A [20]\n" + 
			"     3  dup\n" + 
			"     4  aload_0 [this]\n" + 
			"     5  invokespecial A001$1$A(A001) [22]\n" + 
			"     8  invokevirtual A001$1$A.get() : int [25]\n" + 
			"    11  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 12] local: this index: 0 type: A001\n"; 
		checkClassFile("A001", source, expectedOutput);
	}			

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25188
	 */
	public void test002() {
		String source =
			"public class A002 {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(); /* \\u000d: CARRIAGE RETURN */\n" +
			"		System.out.println();\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     3  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 6, line: 4]\n" + 
			"        [pc: 12, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n";
		checkClassFile("A002", source, expectedOutput);
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26098
	 */
	public void test003() {
		String source =
			"public class A003 {\n" +
			"\n" +
			"	public int bar() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	\n" +
			"	public void foo() {\n" +
			"		System.out.println(bar());\n" +
			"	}\n" +
			"}";
		String expectedOutput = 
			"  // Method descriptor #15 ()I\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public int bar();\n" + 
			"    0  iconst_0\n" + 
			"    1  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 2] local: this index: 0 type: A003\n" + 
			"  \n" + 
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  public void foo();\n" + 
			"     0  getstatic java.lang.System.out : java.io.PrintStream [17]\n" + 
			"     3  aload_0 [this]\n" + 
			"     4  invokevirtual A003.bar() : int [23]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(int) : void [25]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"        [pc: 10, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 11] local: this index: 0 type: A003\n";
		checkClassFile("A003", source, expectedOutput);
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test004() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   && !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  bipush 6\n" + 
			"     8  if_icmpne 22\n" + 
			"    11  iload_1 [b]\n" + 
			"    12  ifne 22\n" + 
			"    15  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    18  iload_2 [i]\n" + 
			"    19  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    22  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 11, line: 6]\n" + 
			"        [pc: 15, line: 7]\n" + 
			"        [pc: 22, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 23] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 23] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test005() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   && true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpne 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test006() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   && false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  bipush 6\n" + 
			"    2  istore_1 [i]\n" + 
			"    3  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 4] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 4] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test007() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   && !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput = 
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifne 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test008() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   && !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput = 
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  iconst_0\n" + 
			"    1  istore_1 [b]\n" + 
			"    2  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 3] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 3] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test009() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   || !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  bipush 6\n" + 
			"     8  if_icmpeq 15\n" + 
			"    11  iload_1 [b]\n" + 
			"    12  ifne 22\n" + 
			"    15  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    18  iload_2 [i]\n" + 
			"    19  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    22  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 11, line: 6]\n" + 
			"        [pc: 15, line: 7]\n" + 
			"        [pc: 22, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 23] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 23] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test010() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   || true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 9\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test011() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   || false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpne 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test012() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   || !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  iconst_0\n" + 
			"    1  istore_1 [b]\n" + 
			"    2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    5  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 6]\n" + 
			"        [pc: 8, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 9] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test013() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   || !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifne 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test014() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   == !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  bipush 6\n" + 
			"     8  if_icmpne 15\n" + 
			"    11  iconst_1\n" + 
			"    12  goto 16\n" + 
			"    15  iconst_0\n" + 
			"    16  iload_1 [b]\n" + 
			"    17  ifeq 24\n" + 
			"    20  iconst_0\n" + 
			"    21  goto 25\n" + 
			"    24  iconst_1\n" + 
			"    25  if_icmpne 35\n" + 
			"    28  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    31  iload_2 [i]\n" + 
			"    32  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    35  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 16, line: 6]\n" + 
			"        [pc: 28, line: 7]\n" + 
			"        [pc: 35, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 36] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 36] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test015() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   == true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpne 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test016() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   == false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test017() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   == !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifne 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test018() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   == !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifeq 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 * http:  //bugs.eclipse.org/bugs/show_bug.cgi?id=26881
	 */
	public void test019() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 5)\n" +
			"			? b : !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  iconst_5\n" + 
			"     7  if_icmpne 17\n" + 
			"    10  iload_1 [b]\n" + 
			"    11  ifeq 28\n" + 
			"    14  goto 21\n" + 
			"    17  iload_1 [b]\n" + 
			"    18  ifne 28\n" + 
			"    21  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    24  iload_2 [i]\n" + 
			"    25  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    28  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 10, line: 6]\n" + 
			"        [pc: 21, line: 7]\n" + 
			"        [pc: 28, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 29] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 29] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test020() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			>= 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iconst_5\n" + 
			"     5  if_icmplt 15\n" + 
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    11  iload_1 [i]\n" + 
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    15  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 4, line: 5]\n" + 
			"        [pc: 8, line: 6]\n" + 
			"        [pc: 15, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test021() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			>= 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iflt 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test022() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			>= i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  ifgt 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 5]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test023() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			> 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  ifle 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test024() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			> i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  ifge 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 5]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test025() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			> 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iconst_5\n" + 
			"     5  if_icmple 15\n" + 
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    11  iload_1 [i]\n" + 
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    15  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 4, line: 5]\n" + 
			"        [pc: 8, line: 6]\n" + 
			"        [pc: 15, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test026() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			< 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  ifge 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test027() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			< i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  ifle 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 5]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test028() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			< 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iconst_5\n" + 
			"     5  if_icmpge 15\n" + 
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    11  iload_1 [i]\n" + 
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    15  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 4, line: 5]\n" + 
			"        [pc: 8, line: 6]\n" + 
			"        [pc: 15, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test029() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			<= 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  ifgt 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test030() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			<= i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iflt 14\n" + 
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    10  iload_1 [i]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 5]\n" + 
			"        [pc: 7, line: 6]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test031() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			<= 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iconst_5\n" + 
			"     5  if_icmpgt 15\n" + 
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    11  iload_1 [i]\n" + 
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    15  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 4, line: 5]\n" + 
			"        [pc: 8, line: 6]\n" + 
			"        [pc: 15, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test032() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			<= 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  iconst_5\n" + 
			"     5  if_icmpgt 15\n" + 
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    11  iload_1 [i]\n" + 
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    15  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 4, line: 5]\n" + 
			"        [pc: 8, line: 6]\n" + 
			"        [pc: 15, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}		

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test033() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   & !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  bipush 6\n" + 
			"     8  if_icmpne 15\n" + 
			"    11  iconst_1\n" + 
			"    12  goto 16\n" + 
			"    15  iconst_0\n" + 
			"    16  iload_1 [b]\n" + 
			"    17  ifeq 24\n" + 
			"    20  iconst_0\n" + 
			"    21  goto 25\n" + 
			"    24  iconst_1\n" + 
			"    25  iand\n" + 
			"    26  ifeq 36\n" + 
			"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    32  iload_2 [i]\n" + 
			"    33  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    36  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 16, line: 6]\n" + 
			"        [pc: 29, line: 7]\n" + 
			"        [pc: 36, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 37] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 37] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test034() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   & true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpne 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test035() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   & false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  bipush 6\n" + 
			"    2  istore_1 [i]\n" + 
			"    3  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 4] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 4] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test036() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   & !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifne 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test037() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   & !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  iconst_0\n" + 
			"    1  istore_1 [b]\n" + 
			"    2  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 3] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 3] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test038() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   | !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  bipush 6\n" + 
			"     8  if_icmpne 15\n" + 
			"    11  iconst_1\n" + 
			"    12  goto 16\n" + 
			"    15  iconst_0\n" + 
			"    16  iload_1 [b]\n" + 
			"    17  ifeq 24\n" + 
			"    20  iconst_0\n" + 
			"    21  goto 25\n" + 
			"    24  iconst_1\n" + 
			"    25  ior\n" + 
			"    26  ifeq 36\n" + 
			"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    32  iload_2 [i]\n" + 
			"    33  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    36  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 16, line: 6]\n" + 
			"        [pc: 29, line: 7]\n" + 
			"        [pc: 36, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 37] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 37] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test039() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   | true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     6  iload_1 [i]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 6]\n" + 
			"        [pc: 10, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 11] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 11] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test040() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   | false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpne 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test041() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   | !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"    0  iconst_0\n" + 
			"    1  istore_1 [b]\n" + 
			"    2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    5  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    8  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 6]\n" + 
			"        [pc: 8, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 9] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test042() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   | !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifne 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test043() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   ^ !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  bipush 6\n" + 
			"     4  istore_2 [i]\n" + 
			"     5  iload_2 [i]\n" + 
			"     6  bipush 6\n" + 
			"     8  if_icmpne 15\n" + 
			"    11  iconst_1\n" + 
			"    12  goto 16\n" + 
			"    15  iconst_0\n" + 
			"    16  iload_1 [b]\n" + 
			"    17  ifeq 24\n" + 
			"    20  iconst_0\n" + 
			"    21  goto 25\n" + 
			"    24  iconst_1\n" + 
			"    25  ixor\n" + 
			"    26  ifeq 36\n" + 
			"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    32  iload_2 [i]\n" + 
			"    33  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    36  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 16, line: 6]\n" + 
			"        [pc: 29, line: 7]\n" + 
			"        [pc: 36, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 37] local: b index: 1 type: boolean\n" + 
			"        [pc: 5, pc: 37] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test044() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   ^ true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test045() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   ^ false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iload_1 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpne 16\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    12  iload_1 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 9, line: 6]\n" + 
			"        [pc: 16, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test046() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   ^ !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifeq 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test047() {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   ^ !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  istore_1 [b]\n" + 
			"     2  iload_1 [b]\n" + 
			"     3  ifne 12\n" + 
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 2, line: 5]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 12, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	public void test048() {
		String source =
			"public class A {\n" +
			"\n" +
			"	static int foo(boolean bool) {\n" +
			"	  int j;\n" +
			"	  try {\n" +
			"	    if (bool) return 1;\n" +
			"	    j = 2;\n" +
			"	  } finally {\n" +
			"	    j = 3;\n" +
			"	  }\n" +
			"	  return j;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		foo(false);\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 (Z)I\n" + 
			"  // Stack: 1, Locals: 4\n" + 
			"  static int foo(boolean bool);\n" + 
			"     0  iload_0 [bool]\n" + 
			"     1  ifeq 9\n" + 
			"     4  jsr 20\n" + 
			"     7  iconst_1\n" + 
			"     8  ireturn\n" + 
			"     9  iconst_2\n" + 
			"    10  istore_1 [j]\n" + 
			"    11  goto 25\n" + 
			"    14  astore_3\n" + 
			"    15  jsr 20\n" + 
			"    18  aload_3\n" + 
			"    19  athrow\n" + 
			"    20  astore_2\n" + 
			"    21  iconst_3\n" + 
			"    22  istore_1 [j]\n" + 
			"    23  ret 2\n" + 
			"    25  jsr 20\n" + 
			"    28  iload_1 [j]\n" + 
			"    29  ireturn\n" + 
			"      Exception Table:\n" + 
			"        [pc: 0, pc: 7] -> 14 when : any\n" + 
			"        [pc: 9, pc: 14] -> 14 when : any\n" + 
			"        [pc: 25, pc: 28] -> 14 when : any\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 9, line: 7]\n" + 
			"        [pc: 14, line: 8]\n" + 
			"        [pc: 18, line: 10]\n" + 
			"        [pc: 20, line: 8]\n" + 
			"        [pc: 21, line: 9]\n" + 
			"        [pc: 23, line: 10]\n" + 
			"        [pc: 28, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 30] local: bool index: 0 type: boolean\n" + 
			"        [pc: 11, pc: 14] local: j index: 1 type: int\n" + 
			"        [pc: 23, pc: 30] local: j index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}
	
	public void test049() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo();\n" + 
			"	}\n" + 
			"	static void foo() {\n" + 
			"		int i = 5;\n" + 
			"		if ((i == 6) && false) {   	\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  static void foo();\n" + 
			"    0  iconst_5\n" + 
			"    1  istore_0 [i]\n" + 
			"    2  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 3] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}
	
	public void test050() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo();\n" + 
			"	}\n" + 
			"	static void foo() {\n" + 
			"		int i = 5;\n" + 
			"		if ((i == 6) && false) {}\n" + 
			"		else {   	\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  iload_0 [i]\n" + 
			"     3  bipush 6\n" + 
			"     5  if_icmpne 8\n" + 
			"     8  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
			"    11  iload_0 [i]\n" + 
			"    12  invokevirtual java.io.PrintStream.println(int) : void [27]\n" + 
			"    15  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 2, line: 7]\n" + 
			"        [pc: 8, line: 9]\n" + 
			"        [pc: 15, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 16] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test051() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar();\n" + 
			"	}\n" + 
			"	static void bar() {\n" + 
			"		int i = 6;\n" + 
			"		if ((i == 6) || true) {\n" + 
			"		} else {\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  static void bar();\n" + 
			"    0  bipush 6\n" + 
			"    2  istore_0 [i]\n" + 
			"    3  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 3, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 4] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}
	
	public void test052() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar();\n" + 
			"	}\n" + 
			"	static void bar() {\n" + 
			"		int i = 6;\n" + 
			"		if ((i == 6) || true) {\n" + 
			"		   	System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  iload_0 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 9\n" + 
			"     9  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
			"    12  iload_0 [i]\n" + 
			"    13  invokevirtual java.io.PrintStream.println(int) : void [27]\n" + 
			"    16  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 3, line: 7]\n" + 
			"        [pc: 9, line: 8]\n" + 
			"        [pc: 16, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 17] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test053() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo2();\n" + 
			"	}\n" + 
			"	static void foo2() {\n" + 
			"		int i = 5;\n" + 
			"		if ((i == 6) && (boom() && false)) {\n" + 
			"		   	System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo2();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  iload_0 [i]\n" + 
			"     3  bipush 6\n" + 
			"     5  if_icmpne 12\n" + 
			"     8  invokestatic X.boom() : boolean [26]\n" + 
			"    11  pop\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"        [pc: 12, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 13] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test054() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo2();\n" + 
			"	}\n" + 
			"	static void foo2() {\n" + 
			"		int i = 5;\n" + 
			"		if ((i == 6) && (boom() && false)) {\n" + 
			"		} else {\n" + 
			"		   	System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo2();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  iload_0 [i]\n" + 
			"     3  bipush 6\n" + 
			"     5  if_icmpne 14\n" + 
			"     8  invokestatic X.boom() : boolean [26]\n" + 
			"    11  ifeq 14\n" + 
			"    14  getstatic java.lang.System.out : java.io.PrintStream [28]\n" + 
			"    17  iload_0 [i]\n" + 
			"    18  invokevirtual java.io.PrintStream.println(int) : void [34]\n" + 
			"    21  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"        [pc: 14, line: 12]\n" + 
			"        [pc: 21, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 22] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}
	
	public void test055() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar2();\n" + 
			"	}\n" + 
			"	static void bar2() {\n" + 
			"		int i = 6;\n" + 
			"		if ((i == 6) || (boom() || true)) {\n" + 
			"		} else {\n" + 
			"			System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar2();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  iload_0 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 13\n" + 
			"     9  invokestatic X.boom() : boolean [26]\n" + 
			"    12  pop\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 3, line: 10]\n" + 
			"        [pc: 13, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 14] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test056() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar2();\n" + 
			"	}\n" + 
			"	static void bar2() {\n" + 
			"		int i = 6;\n" + 
			"		if ((i == 6) || (boom() || true)) {\n" + 
			"			System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar2();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  iload_0 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 15\n" + 
			"     9  invokestatic X.boom() : boolean [26]\n" + 
			"    12  ifne 15\n" + 
			"    15  getstatic java.lang.System.out : java.io.PrintStream [28]\n" + 
			"    18  iload_0 [i]\n" + 
			"    19  invokevirtual java.io.PrintStream.println(int) : void [34]\n" + 
			"    22  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 3, line: 10]\n" + 
			"        [pc: 15, line: 11]\n" + 
			"        [pc: 22, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 23] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test057() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo3();\n" + 
			"	}\n" + 
			"	static void foo3() {\n" + 
			"		int i = 5;\n" + 
			"		if (false && (i == 6)) {\n" + 
			"		   	System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  static void foo3();\n" + 
			"    0  iconst_5\n" + 
			"    1  istore_0 [i]\n" + 
			"    2  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 3] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}
	
	public void test058() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo3();\n" + 
			"	}\n" + 
			"	static void foo3() {\n" + 
			"		int i = 5;\n" + 
			"		if (false && (i == 6)) {\n" + 
			"		} else {\n" + 
			"		   	System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo3();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
			"     5  iload_0 [i]\n" + 
			"     6  invokevirtual java.io.PrintStream.println(int) : void [27]\n" + 
			"     9  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 2, line: 9]\n" + 
			"        [pc: 9, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 10] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test059() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar3();\n" + 
			"	}\n" + 
			"	static void bar3() {\n" + 
			"		int i = 6;\n" + 
			"		if (true || (i == 6)) {\n" + 
			"		} else {\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  static void bar3();\n" + 
			"    0  bipush 6\n" + 
			"    2  istore_0 [i]\n" + 
			"    3  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 3, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 4] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test060() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar3();\n" + 
			"	}\n" + 
			"	static void bar3() {\n" + 
			"		int i = 6;\n" + 
			"		if (true || (i == 6)) {\n" + 
			"		   System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar3();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
			"     6  iload_0 [i]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(int) : void [27]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 3, line: 8]\n" + 
			"        [pc: 10, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 11] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test061() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo4();\n" + 
			"	}\n" + 
			"	static void foo4() {\n" + 
			"		int i = 5;\n" + 
			"		if ((false && boom()) && (i == 6)) {   	\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  static void foo4();\n" + 
			"    0  iconst_5\n" + 
			"    1  istore_0 [i]\n" + 
			"    2  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 3] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}
	
	public void test062() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo4();\n" + 
			"	}\n" + 
			"	static void foo4() {\n" + 
			"		int i = 5;\n" + 
			"		if ((false && boom()) && (i == 6)) {\n" + 
			"		} else {  	\n" + 
			"		   System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo4();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  getstatic java.lang.System.out : java.io.PrintStream [26]\n" + 
			"     5  iload_0 [i]\n" + 
			"     6  invokevirtual java.io.PrintStream.println(int) : void [32]\n" + 
			"     9  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 12]\n" + 
			"        [pc: 9, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 10] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test063() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar4();\n" + 
			"	}\n" + 
			"	static void bar4() {\n" + 
			"		int i = 6;\n" + 
			"		if ((true || boom()) || (i == 6)) {\n" + 
			"		} else {\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  static void bar4();\n" + 
			"    0  bipush 6\n" + 
			"    2  istore_0 [i]\n" + 
			"    3  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 3, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 4] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test064() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar4();\n" + 
			"	}\n" + 
			"	static void bar4() {\n" + 
			"		int i = 6;\n" + 
			"		if ((true || boom()) || (i == 6)) {\n" + 
			"		   	System.out.println(i);\n" + 
			"		   }\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar4();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  getstatic java.lang.System.out : java.io.PrintStream [26]\n" + 
			"     6  iload_0 [i]\n" + 
			"     7  invokevirtual java.io.PrintStream.println(int) : void [32]\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 3, line: 11]\n" + 
			"        [pc: 10, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 11] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test065() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo5();\n" + 
			"	}\n" + 
			"	static void foo5() {\n" + 
			"		int i = 5;\n" + 
			"		if (((i == 6) && (boom() && false)) && false) {\n" + 
			"			System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo5();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  iload_0 [i]\n" + 
			"     3  bipush 6\n" + 
			"     5  if_icmpne 12\n" + 
			"     8  invokestatic X.boom() : boolean [26]\n" + 
			"    11  pop\n" + 
			"    12  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"        [pc: 12, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 13] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}
	
	public void test066() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		foo5();\n" + 
			"	}\n" + 
			"	static void foo5() {\n" + 
			"		int i = 5;\n" + 
			"		if (((i == 6) && (boom() && false)) && false) {\n" + 
			"		} else {\n" + 
			"			System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void foo5();\n" + 
			"     0  iconst_5\n" + 
			"     1  istore_0 [i]\n" + 
			"     2  iload_0 [i]\n" + 
			"     3  bipush 6\n" + 
			"     5  if_icmpne 14\n" + 
			"     8  invokestatic X.boom() : boolean [26]\n" + 
			"    11  ifeq 14\n" + 
			"    14  getstatic java.lang.System.out : java.io.PrintStream [28]\n" + 
			"    17  iload_0 [i]\n" + 
			"    18  invokevirtual java.io.PrintStream.println(int) : void [34]\n" + 
			"    21  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 2, line: 10]\n" + 
			"        [pc: 14, line: 12]\n" + 
			"        [pc: 21, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 2, pc: 22] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test067() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar5();\n" + 
			"	}\n" + 
			"	static void bar5() {\n" + 
			"		int i = 6;\n" + 
			"		if (((i == 6) || (boom() || true)) && true) {\n" + 
			"		} else {\n" + 
			"			System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar5();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  iload_0 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 13\n" + 
			"     9  invokestatic X.boom() : boolean [26]\n" + 
			"    12  pop\n" + 
			"    13  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 3, line: 10]\n" + 
			"        [pc: 13, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 14] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test068() {
		String source =
			"public class X {\n" + 
			"	static boolean boom() { \n" + 
			"		throw new NullPointerException();\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		bar5();\n" + 
			"	}\n" + 
			"	static void bar5() {\n" + 
			"		int i = 6;\n" + 
			"		if (((i == 6) || (boom() || true)) && true) {\n" + 
			"			System.out.println(i);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  static void bar5();\n" + 
			"     0  bipush 6\n" + 
			"     2  istore_0 [i]\n" + 
			"     3  iload_0 [i]\n" + 
			"     4  bipush 6\n" + 
			"     6  if_icmpeq 15\n" + 
			"     9  invokestatic X.boom() : boolean [26]\n" + 
			"    12  ifne 15\n" + 
			"    15  getstatic java.lang.System.out : java.io.PrintStream [28]\n" + 
			"    18  iload_0 [i]\n" + 
			"    19  invokevirtual java.io.PrintStream.println(int) : void [34]\n" + 
			"    22  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"        [pc: 3, line: 10]\n" + 
			"        [pc: 15, line: 11]\n" + 
			"        [pc: 22, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 23] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47886
	 */
	public void test069() {
		String source =
			"public interface I {\n" + 
			"}";
		String expectedOutput =
			"// Compiled from I.java (version 1.2 : 46.0, no super bit)\n" + 
			"public abstract interface I extends java.lang.Object {\n" + 
			"  Constant pool:\n" + 
			"    constant #1 class: #2 I\n" + 
			"    constant #2 utf8: I\n" + 
			"    constant #3 class: #4 java/lang/Object\n" + 
			"    constant #4 utf8: java/lang/Object\n" + 
			"    constant #5 utf8: SourceFile\n" + 
			"    constant #6 utf8: I.java\n" + 
			"}";
		checkClassFile("I", source, expectedOutput);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test070() {
		String source =
			"public class X {\n" +
			"	X(String s) {\n" +
			"	}\n" +
			"	public void foo(int i, long l, String[][]... args) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V\n" + 
			"  // Stack: 0, Locals: 5\n" + 
			"  public void foo(int i, long l, java.lang.String[][]... arg);\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" + 
			"        [pc: 0, pc: 1] local: i index: 1 type: int\n" + 
			"        [pc: 0, pc: 1] local: l index: 2 type: long\n" + 
			"        [pc: 0, pc: 1] local: args index: 4 type: java.lang.String[][][]\n" + 
			"}";
		checkClassFile("1.5", "X", source, expectedOutput);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76472
	 */
	public void test071() {
		String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		long[] tab = new long[] {};\n" + 
			"		System.out.println(tab.clone());\n" + 
			"		System.out.println(tab.clone());\n" + 
			"	}\n" + 
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  newarray long [11]\n" + 
			"     3  astore_1 [tab]\n" + 
			"     4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"     7  aload_1 [tab]\n" + 
			"     8  invokevirtual long[].clone() : java.lang.Object [22]\n" + 
			"    11  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]\n" + 
			"    14  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    17  aload_1 [tab]\n" + 
			"    18  invokevirtual long[].clone() : java.lang.Object [22]\n" + 
			"    21  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]\n" + 
			"    24  return\n";
		checkClassFile("1.5", "X", source, expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test072() {
		String source =
			"package p;\n" +
			"public class X {\n" + 
			"	public static final double CONST = Double.POSITIVE_INFINITY;\n" +
			"	X(X x) {}\n" +
			"	int foo() { return 0; }\n" +
			"	double foo2() { return 0; }\n" +
			"	byte foo3() { return 0; }\n" +
			"	char foo4() { return 0; }\n" +
			"	float foo5() { return 0; }\n" +
			"	long foo6() { return 0; }\n" +
			"	short foo7() { return 0; }\n" +
			"	Object foo8() { return null; }\n" +
			"	boolean foo9() { return false; }\n" +
			"	void foo10() {}\n" +
			"}";
		String expectedOutput =
			"package p;\n" + 
			"public class X extends java.lang.Object {\n" + 
			"  \n" + 
			"  public static final double CONST = 1.0 / 0.0;\n" + 
			"  \n" + 
			"  X(p.X x) {\n" + 
			"  }\n" + 
			"  \n" + 
			"  int foo() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  double foo2() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  byte foo3() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  char foo4() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  float foo5() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  long foo6() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  short foo7() {\n" + 
			"    return 0;\n" + 
			"  }\n" + 
			"  \n" + 
			"  java.lang.Object foo8() {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  \n" + 
			"  boolean foo9() {\n" + 
			"    return false;\n" + 
			"  }\n" + 
			"  \n" + 
			"  void foo10() {\n" + 
			"  }\n" + 
			"}";
		checkClassFile("1.4", "p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test073() {
		String source =
			"public class X {\n" + 
			"	public static final double CONST = Double.POSITIVE_INFINITY;\n" +
			"	X(X x) {}\n" +
			"}";
		String expectedOutput =
			"public class X extends java.lang.Object {\n" + 
			"  \n" + 
			"  public static final double CONST = 1.0 / 0.0;\n" + 
			"  \n" + 
			"  X(X x) {\n" + 
			"  }\n" + 
			"}";
		checkClassFile("1.4", "", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test074() {
		String source =
			"package p;\n" +
			"public class X {\n" + 
			"	public static final double CONST = Double.POSITIVE_INFINITY;\n" +
			"	X(X x) {}\n" +
			"}";
		String expectedOutput =
			"package p;\n" + 
			"public class X extends Object {\n" + 
			"  \n" + 
			"  public static final double CONST = 1.0 / 0.0;\n" + 
			"  \n" + 
			"  X(X x) {\n" + 
			"  }\n" + 
			"}";
		checkClassFile("1.4", "p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}
}
