/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class ForeachStatementTest extends AbstractRegressionTest {
	
public ForeachStatementTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	return options;
}
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//	testsNames = new String[] { "Bug51529a", "Bug51529b" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
//	testsNumbers = new int[] { 22 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//	testsRange = new int[] { 21, 50 };
//	testsRange = new int[] { -1, 50 }; // run all tests with a number less or equals to 50
//	testsRange = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
}
public static Test suite() {
	if (testsNames != null || testsNumbers!=null || testsRange!=null) {
		return new RegressionTestSetup(buildTestSuite(testClass()), highestComplianceLevels());
	}
	return setupSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        \n" + 
			"        for (char c : \"SUCCESS\".toCharArray()) {\n" + 
			"            System.out.print(c);\n" + 
			"        }\n" + 
			"        System.out.println();\n" + 
			"    }\n" + 
			"}\n",
		},
		"SUCCESS");
}
public void test002() { 
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        \n" + 
			"        for (int value : new int[] {value}) {\n" + 
			"            System.out.println(value);\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	for (int value : new int[] {value}) {\n" + 
		"	                            ^^^^^\n" + 
		"The local variable value may not have been initialized\n" + 
		"----------\n");
}
public void test003() { 
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        \n" + 
			"        for (int value : value) {\n" + 
			"            System.out.println(value);\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	for (int value : value) {\n" + 
		"	                 ^^^^^\n" + 
		"Can only iterate over an array or an instance of java.lang.Iterable\n" + 
		"----------\n");
}
public void test004() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
			"		int sum = 0;\n" + 
			"		loop: for (final int e : tab) {\n" + 
			"			sum += e;\n" + 
			"			if (e == 3) {\n" + 
			"				break loop;\n" + 
			"			}\n" + 
			"		}\n" + 
			"		System.out.println(sum);\n" + 
			"	}\n" + 
			"}\n",
		},
		"6");
}
public void test005() { 
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"	    final int i;\n" + 
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
			"		int sum = 0;\n" + 
			"		loop: for (final int e : tab) {\n" + 
			"			sum += e;\n" + 
			"			if (e == 3) {\n" + 
			"			    i = 1;\n" + 
			"				break loop;\n" + 
			"			}\n" + 
			"		}\n" + 
			"		System.out.println(sum + i);\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(sum + i);\n" + 
		"	                         ^\n" + 
		"The local variable i may not have been initialized\n" + 
		"----------\n");
}
public void test006() { 
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"	    final int i;\n" + 
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
			"		loop: for (final int e : tab) {\n" + 
			"		    i = e;\n" + 
			"			if (e == 3) {\n" + 
			"			    i = 1;\n" + 
			"				break loop;\n" + 
			"			}\n" + 
			"		}\n" + 
			"		System.out.println(i);\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	i = e;\n" + 
		"	^\n" + 
		"The final local variable i may already have been assigned\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	i = 1;\n" + 
		"	^\n" + 
		"The final local variable i may already have been assigned\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(i);\n" + 
		"	                   ^\n" + 
		"The local variable i may not have been initialized\n" + 
		"----------\n");
}
public void test007() {
	Map customOptions = this.getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"	    int i;\n" + 
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
			"		for (final int e : tab) {\n" + 
			"		    i = e;\n" + 
			"		}\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS",
		null,
		true,
		null,
		customOptions);
	
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 7\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  bipush 9\n" + 
		"     2  newarray #10 int\n" + 
		"     4  dup\n" + 
		"     5  iconst_0\n" + 
		"     6  iconst_1\n" + 
		"     7  iastore\n" + 
		"     8  dup\n" + 
		"     9  iconst_1\n" + 
		"    10  iconst_2\n" + 
		"    11  iastore\n" + 
		"    12  dup\n" + 
		"    13  iconst_2\n" + 
		"    14  iconst_3\n" + 
		"    15  iastore\n" + 
		"    16  dup\n" + 
		"    17  iconst_3\n" + 
		"    18  iconst_4\n" + 
		"    19  iastore\n" + 
		"    20  dup\n" + 
		"    21  iconst_4\n" + 
		"    22  iconst_5\n" + 
		"    23  iastore\n" + 
		"    24  dup\n" + 
		"    25  iconst_5\n" + 
		"    26  bipush 6\n" + 
		"    28  iastore\n" + 
		"    29  dup\n" + 
		"    30  bipush 6\n" + 
		"    32  bipush 7\n" + 
		"    34  iastore\n" + 
		"    35  dup\n" + 
		"    36  bipush 7\n" + 
		"    38  bipush 8\n" + 
		"    40  iastore\n" + 
		"    41  dup\n" + 
		"    42  bipush 8\n" + 
		"    44  bipush 9\n" + 
		"    46  iastore\n" + 
		"    47  astore_2\n" + 
		"    48  aload_2\n" + 
		"    49  astore 6\n" + 
		"    51  iconst_0\n" + 
		"    52  istore 4\n" + 
		"    54  aload 6\n" + 
		"    56  arraylength\n" + 
		"    57  istore 5\n" + 
		"    59  goto 73\n" + 
		"    62  aload 6\n" + 
		"    64  iload 4\n" + 
		"    66  iaload\n" + 
		"    67  istore_3\n" + 
		"    68  iload_3\n" + 
		"    69  istore_1\n" + 
		"    70  iinc 4 1\n" + 
		"    73  iload 4\n" + 
		"    75  iload 5\n" + 
		"    77  if_icmplt 62\n" + 
		"    80  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    83  ldc #23 <String \"SUCCESS\">\n" + 
		"    85  invokevirtual #29 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
		"    88  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 5]\n" + 
		"        [pc: 48, line: 6]\n" + 
		"        [pc: 68, line: 7]\n" + 
		"        [pc: 70, line: 6]\n" + 
		"        [pc: 80, line: 9]\n" + 
		"        [pc: 88, line: 10]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 89] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 70, pc: 73] local: i index: 1 type: I\n" + 
		"        [pc: 48, pc: 89] local: tab index: 2 type: [I\n" + 
		"        [pc: 68, pc: 80] local: e index: 3 type: I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}
public void test008() { 
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(Iterable col) {\n" + 
			"		for (X x : col) {\n" + 
			"			System.out.println(x);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for (X x : col) {\n" + 
		"	           ^^^\n" + 
		"Type mismatch: cannot convert from element type Object to X\n" + 
		"----------\n");
}
public void test009() { 
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(Iterable<String> col) {\n" + 
			"		for (X x : col) {\n" + 
			"			System.out.println(x);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for (X x : col) {\n" + 
		"	           ^^^\n" + 
		"Type mismatch: cannot convert from element type String to X\n" + 
		"----------\n");
}
/*
 * Test implicit conversion to float. If missing, VerifyError
 */
public void test010() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" + 
			"		int sum = 0;\n" + 
			"		loop: for (final float e : tab) {\n" + 
			"			sum += e;\n" + 
			"			if (e == 3) {\n" + 
			"				break loop;\n" + 
			"			}\n" + 
			"		}\n" + 
			"		System.out.println(sum);\n" + 
			"	}\n" + 
			"}\n",
		},
		"6");
}
/*
 * Cannot convert int[] to int
 */
public void test011() { 
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"	public static void main(String[] args) {\n" + 
				"		int[][] tab = new int[][] {\n" + 
				"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" + 
				"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" + 
				"		};\n" + 
				"		loop: for (final int e : tab) {\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	loop: for (final int e : tab) {\n" + 
			"	                         ^^^\n" + 
			"Type mismatch: cannot convert from element type int[] to int\n" + 
			"----------\n");
}
/*
 * Ensure access to int[]
 */
public void test012() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[][] tab = new int[][] {\n" + 
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" + 
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" + 
			"		};\n" + 
			"		for (final int[] e : tab) {\n" + 
			"			System.out.print(e.length);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"99");
}
/*
 * Ensure access to int[]
 */
public void test013() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[][] tab = new int[][] {\n" + 
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" + 
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" + 
			"		};\n" + 
			"		for (final int[] e : tab) {\n" + 
			"			System.out.print(e[0]);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"11");
}
/*
 * Empty block action
 */
public void test014() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1 };\n" + 
			"		for (final int e : tab) {\n" + 
			"		}\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 2\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray #10 int\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1\n" + 
		"     8  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    11  ldc #23 <String \"SUCCESS\">\n" + 
		"    13  invokevirtual #29 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
		"    16  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 7]\n" + 
		"        [pc: 16, line: 8]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 17] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 17] local: tab index: 1 type: [I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}	
}
/*
 * Empty statement action
 */
public void test015() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1 };\n" + 
			"		for (final int e : tab);\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 2\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray #10 int\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1\n" + 
		"     8  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    11  ldc #23 <String \"SUCCESS\">\n" + 
		"    13  invokevirtual #29 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
		"    16  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 6]\n" + 
		"        [pc: 16, line: 7]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 17] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 17] local: tab index: 1 type: [I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}
/*
 * Empty block action
 */
public void test016() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1 };\n" + 
			"		for (final int e : tab) {;\n" + 
			"		}\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray #10 int\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1\n" + 
		"     8  aload_1\n" + 
		"     9  astore 4\n" + 
		"    11  iconst_0\n" + 
		"    12  istore_2\n" + 
		"    13  aload 4\n" + 
		"    15  arraylength\n" + 
		"    16  istore_3\n" + 
		"    17  goto 23\n" + 
		"    20  iinc 2 1\n" + 
		"    23  iload_2\n" + 
		"    24  iload_3\n" + 
		"    25  if_icmplt 20\n" + 
		"    28  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    31  ldc #23 <String \"SUCCESS\">\n" + 
		"    33  invokevirtual #29 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
		"    36  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 5]\n" + 
		"        [pc: 28, line: 7]\n" + 
		"        [pc: 36, line: 8]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 37] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 37] local: tab index: 1 type: [I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}	
}
/*
 * Ensure access to int[]
 */
public void test017() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1 };\n" + 
			"		for (final int e : tab) {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
}
/*
 * Break the loop
 */
public void test018() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] { 1 };\n" + 
			"		for (final int e : tab) {\n" +
			"			System.out.println(e);\n" + 
			"			break;\n" +
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"1");
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 6\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray #10 int\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1\n" + 
		"     8  aload_1\n" + 
		"     9  astore 5\n" + 
		"    11  iconst_0\n" + 
		"    12  istore_3\n" + 
		"    13  aload 5\n" + 
		"    15  arraylength\n" + 
		"    16  istore 4\n" + 
		"    18  goto 36\n" + 
		"    21  aload 5\n" + 
		"    23  iload_3\n" + 
		"    24  iaload\n" + 
		"    25  istore_2\n" + 
		"    26  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    29  iload_2\n" + 
		"    30  invokevirtual #27 <Method java/io/PrintStream.println(I)V>\n" + 
		"    33  goto 42\n" + 
		"    36  iload_3\n" + 
		"    37  iload 4\n" + 
		"    39  if_icmplt 21\n" + 
		"    42  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 5]\n" + 
		"        [pc: 26, line: 6]\n" + 
		"        [pc: 33, line: 7]\n" + 
		"        [pc: 36, line: 5]\n" + 
		"        [pc: 42, line: 9]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 43] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 43] local: tab index: 1 type: [I\n" + 
		"        [pc: 26, pc: 42] local: e index: 2 type: I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}
/*
 * Break the loop
 */
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] {};\n" + 
			"		System.out.print(\"SUC\");\n" + 
			"		for (final int e : tab) {\n" +
			"			System.out.print(\"1x\");\n" + 
			"			break;\n" +
			"		}\n" + 
			"		System.out.println(\"CESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
	
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_0\n" + 
		"     1  newarray #10 int\n" + 
		"     3  astore_1\n" + 
		"     4  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"     7  ldc #23 <String \"SUC\">\n" + 
		"     9  invokevirtual #29 <Method java/io/PrintStream.print(Ljava/lang/String;)V>\n" + 
		"    12  aload_1\n" + 
		"    13  astore 4\n" + 
		"    15  iconst_0\n" + 
		"    16  istore_2\n" + 
		"    17  aload 4\n" + 
		"    19  arraylength\n" + 
		"    20  istore_3\n" + 
		"    21  goto 35\n" + 
		"    24  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    27  ldc #31 <String \"1x\">\n" + 
		"    29  invokevirtual #29 <Method java/io/PrintStream.print(Ljava/lang/String;)V>\n" + 
		"    32  goto 40\n" + 
		"    35  iload_2\n" + 
		"    36  iload_3\n" + 
		"    37  if_icmplt 24\n" + 
		"    40  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    43  ldc #33 <String \"CESS\">\n" + 
		"    45  invokevirtual #36 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
		"    48  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 4, line: 5]\n" + 
		"        [pc: 12, line: 6]\n" + 
		"        [pc: 24, line: 7]\n" + 
		"        [pc: 32, line: 8]\n" + 
		"        [pc: 35, line: 6]\n" + 
		"        [pc: 40, line: 10]\n" + 
		"        [pc: 48, line: 11]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 49] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 4, pc: 49] local: tab index: 1 type: [I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}	
}
/*
 * Break the loop
 */
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    \n" + 
			"	public static void main(String[] args) {\n" + 
			"		int[] tab = new int[] {};\n" + 
			"		System.out.print(\"SUC\");\n" + 
			"		loop: for (final int e : tab) {\n" + 
			"			System.out.print(\"1x\");\n" +
			"			continue loop;\n" +
			"		}\n" + 
			"		System.out.println(\"CESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
	
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_0\n" + 
		"     1  newarray #10 int\n" + 
		"     3  astore_1\n" + 
		"     4  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"     7  ldc #23 <String \"SUC\">\n" + 
		"     9  invokevirtual #29 <Method java/io/PrintStream.print(Ljava/lang/String;)V>\n" + 
		"    12  aload_1\n" + 
		"    13  astore 4\n" + 
		"    15  iconst_0\n" + 
		"    16  istore_2\n" + 
		"    17  aload 4\n" + 
		"    19  arraylength\n" + 
		"    20  istore_3\n" + 
		"    21  goto 35\n" + 
		"    24  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    27  ldc #31 <String \"1x\">\n" + 
		"    29  invokevirtual #29 <Method java/io/PrintStream.print(Ljava/lang/String;)V>\n" + 
		"    32  iinc 2 1\n" + 
		"    35  iload_2\n" + 
		"    36  iload_3\n" + 
		"    37  if_icmplt 24\n" + 
		"    40  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    43  ldc #33 <String \"CESS\">\n" + 
		"    45  invokevirtual #36 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
		"    48  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 4, line: 5]\n" + 
		"        [pc: 12, line: 6]\n" + 
		"        [pc: 24, line: 7]\n" + 
		"        [pc: 35, line: 6]\n" + 
		"        [pc: 40, line: 10]\n" + 
		"        [pc: 48, line: 11]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 49] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 4, pc: 49] local: tab index: 1 type: [I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}
public void test021() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		int sum = 0;\n" +
			"		int i = 0;\n" +
			"		loop1: while(true) {\n" +
			"			i++;\n" +
			"			loop: for (final int e : tab) {\n" +
			"				sum += e;\n" +
			"				if (i == 3) {\n" +
			"					break loop1;\n" +
			"				} else if (e == 5) {\n" +
			"					break loop;\n" +
			"				} else {\n" +
			"					continue;\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(sum);\n" +
			"	}\n" +
			"}",
		},
		"31");
		
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 8\n" + 
		"  public static void main(String[] args);\n" + 
		"      0  bipush 9\n" + 
		"      2  newarray #10 int\n" + 
		"      4  dup\n" + 
		"      5  iconst_0\n" + 
		"      6  iconst_1\n" + 
		"      7  iastore\n" + 
		"      8  dup\n" + 
		"      9  iconst_1\n" + 
		"     10  iconst_2\n" + 
		"     11  iastore\n" + 
		"     12  dup\n" + 
		"     13  iconst_2\n" + 
		"     14  iconst_3\n" + 
		"     15  iastore\n" + 
		"     16  dup\n" + 
		"     17  iconst_3\n" + 
		"     18  iconst_4\n" + 
		"     19  iastore\n" + 
		"     20  dup\n" + 
		"     21  iconst_4\n" + 
		"     22  iconst_5\n" + 
		"     23  iastore\n" + 
		"     24  dup\n" + 
		"     25  iconst_5\n" + 
		"     26  bipush 6\n" + 
		"     28  iastore\n" + 
		"     29  dup\n" + 
		"     30  bipush 6\n" + 
		"     32  bipush 7\n" + 
		"     34  iastore\n" + 
		"     35  dup\n" + 
		"     36  bipush 7\n" + 
		"     38  bipush 8\n" + 
		"     40  iastore\n" + 
		"     41  dup\n" + 
		"     42  bipush 8\n" + 
		"     44  bipush 9\n" + 
		"     46  iastore\n" + 
		"     47  astore_1\n" + 
		"     48  iconst_0\n" + 
		"     49  istore_2\n" + 
		"     50  iconst_0\n" + 
		"     51  istore_3\n" + 
		"     52  iinc 3 1\n" + 
		"     55  aload_1\n" + 
		"     56  astore 7\n" + 
		"     58  iconst_0\n" + 
		"     59  istore 5\n" + 
		"     61  aload 7\n" + 
		"     63  arraylength\n" + 
		"     64  istore 6\n" + 
		"     66  goto 101\n" + 
		"     69  aload 7\n" + 
		"     71  iload 5\n" + 
		"     73  iaload\n" + 
		"     74  istore 4\n" + 
		"     76  iload_2\n" + 
		"     77  iload 4\n" + 
		"     79  iadd\n" + 
		"     80  istore_2\n" + 
		"     81  iload_3\n" + 
		"     82  iconst_3\n" + 
		"     83  if_icmpne 89\n" + 
		"     86  goto 111\n" + 
		"     89  iload 4\n" + 
		"     91  iconst_5\n" + 
		"     92  if_icmpne 98\n" + 
		"     95  goto 108\n" + 
		"     98  iinc 5 1\n" + 
		"    101  iload 5\n" + 
		"    103  iload 6\n" + 
		"    105  if_icmplt 69\n" + 
		"    108  goto 52\n" + 
		"    111  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    114  iload_2\n" + 
		"    115  invokevirtual #27 <Method java/io/PrintStream.println(I)V>\n" + 
		"    118  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 48, line: 4]\n" + 
		"        [pc: 50, line: 5]\n" + 
		"        [pc: 52, line: 7]\n" + 
		"        [pc: 55, line: 8]\n" + 
		"        [pc: 76, line: 9]\n" + 
		"        [pc: 81, line: 10]\n" + 
		"        [pc: 86, line: 11]\n" + 
		"        [pc: 89, line: 12]\n" + 
		"        [pc: 95, line: 13]\n" + 
		"        [pc: 101, line: 8]\n" + 
		"        [pc: 108, line: 6]\n" + 
		"        [pc: 111, line: 19]\n" + 
		"        [pc: 118, line: 20]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 119] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 48, pc: 119] local: tab index: 1 type: [I\n" + 
		"        [pc: 50, pc: 119] local: sum index: 2 type: I\n" + 
		"        [pc: 52, pc: 119] local: i index: 3 type: I\n" + 
		"        [pc: 76, pc: 108] local: e index: 4 type: I\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}
public void test022() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		ArrayList<Integer> arrayList = new ArrayList<Integer>();\n" + 
			"		for (int i = 0; i < 10; i++) {\n" + 
			"			arrayList.add(new Integer(i));\n" + 
			"		}\n" + 
			"		int sum = 0;\n" + 
			"		for (Integer e : arrayList) {\n" + 
			"			sum += e.intValue();\n" + 
			"		}\n" + 
			"		System.out.println(sum);\n" + 
			"	}\n" + 
			"}",
		},
		"45");
		
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 6\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  new #17 java/util/ArrayList\n" + 
		"     3  dup\n" + 
		"     4  invokespecial #18 <Method java/util/ArrayList.<init>()V>\n" + 
		"     7  astore_1\n" + 
		"     8  iconst_0\n" + 
		"     9  istore_2\n" + 
		"    10  goto 29\n" + 
		"    13  aload_1\n" + 
		"    14  new #20 java/lang/Integer\n" + 
		"    17  dup\n" + 
		"    18  iload_2\n" + 
		"    19  invokespecial #23 <Method java/lang/Integer.<init>(I)V>\n" + 
		"    22  invokevirtual #27 <Method java/util/ArrayList.add(Ljava/lang/Object;)Z>\n" + 
		"    25  pop\n" + 
		"    26  iinc 2 1\n" + 
		"    29  iload_2\n" + 
		"    30  bipush 10\n" + 
		"    32  if_icmplt 13\n" + 
		"    35  iconst_0\n" + 
		"    36  istore_2\n" + 
		"    37  aload_1\n" + 
		"    38  dup\n" + 
		"    39  astore 5\n" + 
		"    41  invokevirtual #32 <Method java/util/ArrayList.iterator()Ljava/util/Iterator;>\n" + 
		"    44  astore 4\n" + 
		"    46  goto 67\n" + 
		"    49  aload 4\n" + 
		"    51  invokeinterface [nargs : 1] #38 <Interface method java/util/Iterator.next()Ljava/lang/Object;>\n" + 
		"    56  checkcast #20 java/lang/Integer\n" + 
		"    59  astore_3\n" + 
		"    60  iload_2\n" + 
		"    61  aload_3\n" + 
		"    62  invokevirtual #42 <Method java/lang/Integer.intValue()I>\n" + 
		"    65  iadd\n" + 
		"    66  istore_2\n" + 
		"    67  aload 4\n" + 
		"    69  invokeinterface [nargs : 1] #46 <Interface method java/util/Iterator.hasNext()Z>\n" + 
		"    74  ifne 49\n" + 
		"    77  getstatic #52 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    80  iload_2\n" + 
		"    81  invokevirtual #57 <Method java/io/PrintStream.println(I)V>\n" + 
		"    84  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 5]\n" + 
		"        [pc: 8, line: 6]\n" + 
		"        [pc: 13, line: 7]\n" + 
		"        [pc: 26, line: 6]\n" + 
		"        [pc: 35, line: 9]\n" + 
		"        [pc: 37, line: 10]\n" + 
		"        [pc: 60, line: 11]\n" + 
		"        [pc: 67, line: 10]\n" + 
		"        [pc: 77, line: 13]\n" + 
		"        [pc: 84, line: 14]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 85] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 85] local: arrayList index: 1 type: Ljava/util/ArrayList;\n" + 
		"        [pc: 10, pc: 35] local: i index: 2 type: I\n" + 
		"        [pc: 37, pc: 85] local: sum index: 2 type: I\n" + 
		"        [pc: 60, pc: 77] local: e index: 3 type: Ljava/lang/Integer;\n" + 
		"      Local variable type table:\n" + 
		"        [pc: 8, pc: 85] local: arrayList index: 1 type: Ljava/util/ArrayList<Ljava/lang/Integer;>;\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}

/*
 * Type mismatch, using non parameterized collection type (indirectly implementing parameterized type)
 */
public void test023() { 
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" + 
				"\n" + 
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		for (Thread s : new AX()) {\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX implements Iterable<String> {\n" + 
				"    \n" + 
				"   public Iterator<String> iterator() {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	for (Thread s : new AX()) {\n" + 
			"	                ^^^^^^^^\n" + 
			"Type mismatch: cannot convert from element type String to Thread\n" + 
			"----------\n");
}
public void test024() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		String[] tab = new String[] {\"SUCCESS\"};\n" + 
			"		List list = new ArrayList();\n" + 
			"		for (String arg : tab) {		\n" + 
			"			list.add(arg);\n" + 
			"		}\n" + 
			"		for (Object arg: list) {\n" + 
			"			System.out.print(arg);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"SUCCESS");
		
	String expectedOutput =
		"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 7\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  anewarray #17 java/lang/String\n" + 
		"     4  dup\n" + 
		"     5  iconst_0\n" + 
		"     6  ldc #19 <String \"SUCCESS\">\n" + 
		"     8  aastore\n" + 
		"     9  astore_1\n" + 
		"    10  new #21 java/util/ArrayList\n" + 
		"    13  dup\n" + 
		"    14  invokespecial #22 <Method java/util/ArrayList.<init>()V>\n" + 
		"    17  astore_2\n" + 
		"    18  aload_1\n" + 
		"    19  astore 6\n" + 
		"    21  iconst_0\n" + 
		"    22  istore 4\n" + 
		"    24  aload 6\n" + 
		"    26  arraylength\n" + 
		"    27  istore 5\n" + 
		"    29  goto 49\n" + 
		"    32  aload 6\n" + 
		"    34  iload 4\n" + 
		"    36  aaload\n" + 
		"    37  astore_3\n" + 
		"    38  aload_2\n" + 
		"    39  aload_3\n" + 
		"    40  invokeinterface [nargs : 2] #28 <Interface method java/util/List.add(Ljava/lang/Object;)Z>\n" + 
		"    45  pop\n" + 
		"    46  iinc 4 1\n" + 
		"    49  iload 4\n" + 
		"    51  iload 5\n" + 
		"    53  if_icmplt 32\n" + 
		"    56  aload_2\n" + 
		"    57  dup\n" + 
		"    58  astore 5\n" + 
		"    60  invokeinterface [nargs : 1] #33 <Interface method java/util/List.iterator()Ljava/util/Iterator;>\n" + 
		"    65  astore 4\n" + 
		"    67  goto 85\n" + 
		"    70  aload 4\n" + 
		"    72  invokeinterface [nargs : 1] #39 <Interface method java/util/Iterator.next()Ljava/lang/Object;>\n" + 
		"    77  astore_3\n" + 
		"    78  getstatic #45 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
		"    81  aload_3\n" + 
		"    82  invokevirtual #51 <Method java/io/PrintStream.print(Ljava/lang/Object;)V>\n" + 
		"    85  aload 4\n" + 
		"    87  invokeinterface [nargs : 1] #55 <Interface method java/util/Iterator.hasNext()Z>\n" + 
		"    92  ifne 70\n" + 
		"    95  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 6]\n" + 
		"        [pc: 10, line: 7]\n" + 
		"        [pc: 18, line: 8]\n" + 
		"        [pc: 38, line: 9]\n" + 
		"        [pc: 46, line: 8]\n" + 
		"        [pc: 56, line: 11]\n" + 
		"        [pc: 78, line: 12]\n" + 
		"        [pc: 85, line: 11]\n" + 
		"        [pc: 95, line: 14]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 96] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 10, pc: 96] local: tab index: 1 type: [Ljava/lang/String;\n" + 
		"        [pc: 18, pc: 96] local: list index: 2 type: Ljava/util/List;\n" + 
		"        [pc: 38, pc: 56] local: arg index: 3 type: Ljava/lang/String;\n" + 
		"        [pc: 78, pc: 95] local: arg index: 3 type: Ljava/lang/Object;\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}
public void test025() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void bug(List<String> lines) {\n" + 
			"        for (int i=0; i<1; i++) {\n" + 
			"           for (String test: lines) {\n" + 
			"                System.out.print(test);\n" + 
			"           }\n" + 
			"        }\n" +
			"    }\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	ArrayList<String> tab = new ArrayList<String>();\n" + 
			"    	tab.add(\"SUCCESS\");\n" + 
			"    	bug(tab);\n" + 
			"    }\n" + 
			"}",
		},
		"SUCCESS");
}
// 68440 - verify error due to local variable invalid slot sharing
public void test026() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    Object[] array = {\n" + 
			"    };\n" + 
			"    void test() {\n" + 
			"        for (Object object : array) {\n" + 
			"            String str = object.toString();\n" + 
			"            str += \"\";\n" + // force 'str' to be preserved during codegen
			"        }\n" + 
			"    }\n" + 
			"    public static void main(String[] args) {\n" + 
			"        new X().test();\n" + 
			"		System.out.println(\"SUCCESS\");\n" +
			"    }\n" + 
			"}\n",
		},
		"SUCCESS");
}
// 68863 - missing local variable attribute after foreach statement
public void test027() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"    Object[] array = {\n" + 
			"    };\n" + 			
			"		java.util.ArrayList i;	\n" + 
			"		for (Object object : array) {\n" + 
			"			if (args == null) {\n" + 
			"				i = null;\n" + 
			"				break;\n" + 
			"			}\n" + 
			"			return;\n" + 
			"		};\n" + 
			"		System.out.println(\"SUCCESS\");	\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
		
	String expectedOutput =
			"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 5\n" + 
			"  public static void main(String[] args);\n" + 
			"     0  iconst_0\n" + 
			"     1  anewarray #4 java/lang/Object\n" + 
			"     4  astore_1\n" + 
			"     5  aload_1\n" + 
			"     6  astore 4\n" + 
			"     8  iconst_0\n" + 
			"     9  istore_2\n" + 
			"    10  aload 4\n" + 
			"    12  arraylength\n" + 
			"    13  istore_3\n" + 
			"    14  goto 27\n" + 
			"    17  aload_0\n" + 
			"    18  ifnonnull 26\n" + 
			"    21  aconst_null\n" + 
			"    22  pop\n" + 
			"    23  goto 32\n" + 
			"    26  return\n" + 
			"    27  iload_2\n" + 
			"    28  iload_3\n" + 
			"    29  if_icmplt 17\n" + 
			"    32  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
			"    35  ldc #23 <String \"SUCCESS\">\n" + 
			"    37  invokevirtual #29 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
			"    40  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 5, line: 6]\n" + 
			"        [pc: 17, line: 7]\n" + 
			"        [pc: 21, line: 8]\n" + 
			"        [pc: 23, line: 9]\n" + 
			"        [pc: 26, line: 11]\n" + 
			"        [pc: 27, line: 6]\n" + 
			"        [pc: 32, line: 13]\n" + 
			"        [pc: 40, line: 14]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 41] local: args index: 0 type: [Ljava/lang/String;\n" + 
			"        [pc: 5, pc: 41] local: array index: 1 type: [Ljava/lang/Object;\n" + 
			"}";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
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
	}
}

public static Class testClass() {
	return ForeachStatementTest.class;
}
}
