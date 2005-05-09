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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class ForeachStatementTest extends AbstractComparableTest {
	
public ForeachStatementTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	return options;
}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 31 };
//		TESTS_RANGE = new int[] { 21, 50 };
//	}
	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
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
		customOptions,
		null/*no custom requestor*/);
	
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 7\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  bipush 9\n" + 
		"     2  newarray int [10]\n" + 
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
		"    47  astore_2 [tab]\n" + 
		"    48  aload_2 [tab]\n" + 
		"    49  astore [local_6]\n" + 
		"    51  iconst_0\n" + 
		"    52  istore [local_4]\n" + 
		"    54  aload [local_6]\n" + 
		"    56  arraylength\n" + 
		"    57  istore [local_5]\n" + 
		"    59  goto 73\n" + 
		"    62  aload [local_6]\n" + 
		"    64  iload [local_4]\n" + 
		"    66  iaload\n" + 
		"    67  istore_3 [e]\n" + 
		"    68  iload_3 [e]\n" + 
		"    69  istore_1 [i]\n" + 
		"    70  iinc 4 1 [local_4]\n" + 
		"    73  iload [local_4]\n" + 
		"    75  iload [local_5]\n" + 
		"    77  if_icmplt 62\n" + 
		"    80  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    83  ldc <String \"SUCCESS\"> [23]\n" + 
		"    85  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [29]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 2\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray int [10]\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1 [tab]\n" + 
		"     8  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    11  ldc <String \"SUCCESS\"> [23]\n" + 
		"    13  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [29]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 2\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray int [10]\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1 [tab]\n" + 
		"     8  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    11  ldc <String \"SUCCESS\"> [23]\n" + 
		"    13  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [29]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray int [10]\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1 [tab]\n" + 
		"     8  aload_1 [tab]\n" + 
		"     9  astore [local_4]\n" + 
		"    11  iconst_0\n" + 
		"    12  istore_2 [local_2]\n" + 
		"    13  aload [local_4]\n" + 
		"    15  arraylength\n" + 
		"    16  istore_3 [local_3]\n" + 
		"    17  goto 23\n" + 
		"    20  iinc 2 1 [local_2]\n" + 
		"    23  iload_2 [local_2]\n" + 
		"    24  iload_3 [local_3]\n" + 
		"    25  if_icmplt 20\n" + 
		"    28  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    31  ldc <String \"SUCCESS\"> [23]\n" + 
		"    33  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [29]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 6\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  newarray int [10]\n" + 
		"     3  dup\n" + 
		"     4  iconst_0\n" + 
		"     5  iconst_1\n" + 
		"     6  iastore\n" + 
		"     7  astore_1 [tab]\n" + 
		"     8  aload_1 [tab]\n" + 
		"     9  astore [local_5]\n" + 
		"    11  iconst_0\n" + 
		"    12  istore_3 [local_3]\n" + 
		"    13  aload [local_5]\n" + 
		"    15  arraylength\n" + 
		"    16  istore [local_4]\n" + 
		"    18  goto 36\n" + 
		"    21  aload [local_5]\n" + 
		"    23  iload_3 [local_3]\n" + 
		"    24  iaload\n" + 
		"    25  istore_2 [e]\n" + 
		"    26  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    29  iload_2 [e]\n" + 
		"    30  invokevirtual java/io/PrintStream.println(I)V [27]\n" + 
		"    33  goto 42\n" + 
		"    36  iload_3 [local_3]\n" + 
		"    37  iload [local_4]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_0\n" + 
		"     1  newarray int [10]\n" + 
		"     3  astore_1 [tab]\n" + 
		"     4  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"     7  ldc <String \"SUC\"> [23]\n" + 
		"     9  invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V [29]\n" + 
		"    12  aload_1 [tab]\n" + 
		"    13  astore [local_4]\n" + 
		"    15  iconst_0\n" + 
		"    16  istore_2 [local_2]\n" + 
		"    17  aload [local_4]\n" + 
		"    19  arraylength\n" + 
		"    20  istore_3 [local_3]\n" + 
		"    21  goto 35\n" + 
		"    24  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    27  ldc <String \"1x\"> [31]\n" + 
		"    29  invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V [29]\n" + 
		"    32  goto 40\n" + 
		"    35  iload_2 [local_2]\n" + 
		"    36  iload_3 [local_3]\n" + 
		"    37  if_icmplt 24\n" + 
		"    40  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    43  ldc <String \"CESS\"> [33]\n" + 
		"    45  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [36]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_0\n" + 
		"     1  newarray int [10]\n" + 
		"     3  astore_1 [tab]\n" + 
		"     4  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"     7  ldc <String \"SUC\"> [23]\n" + 
		"     9  invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V [29]\n" + 
		"    12  aload_1 [tab]\n" + 
		"    13  astore [local_4]\n" + 
		"    15  iconst_0\n" + 
		"    16  istore_2 [local_2]\n" + 
		"    17  aload [local_4]\n" + 
		"    19  arraylength\n" + 
		"    20  istore_3 [local_3]\n" + 
		"    21  goto 35\n" + 
		"    24  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    27  ldc <String \"1x\"> [31]\n" + 
		"    29  invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V [29]\n" + 
		"    32  iinc 2 1 [local_2]\n" + 
		"    35  iload_2 [local_2]\n" + 
		"    36  iload_3 [local_3]\n" + 
		"    37  if_icmplt 24\n" + 
		"    40  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    43  ldc <String \"CESS\"> [33]\n" + 
		"    45  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [36]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 8\n" + 
		"  public static void main(String[] args);\n" + 
		"      0  bipush 9\n" + 
		"      2  newarray int [10]\n" + 
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
		"     47  astore_1 [tab]\n" + 
		"     48  iconst_0\n" + 
		"     49  istore_2 [sum]\n" + 
		"     50  iconst_0\n" + 
		"     51  istore_3 [i]\n" + 
		"     52  iinc 3 1 [i]\n" + 
		"     55  aload_1 [tab]\n" + 
		"     56  astore [local_7]\n" + 
		"     58  iconst_0\n" + 
		"     59  istore [local_5]\n" + 
		"     61  aload [local_7]\n" + 
		"     63  arraylength\n" + 
		"     64  istore [local_6]\n" + 
		"     66  goto 101\n" + 
		"     69  aload [local_7]\n" + 
		"     71  iload [local_5]\n" + 
		"     73  iaload\n" + 
		"     74  istore [e]\n" + 
		"     76  iload_2 [sum]\n" + 
		"     77  iload [e]\n" + 
		"     79  iadd\n" + 
		"     80  istore_2 [sum]\n" + 
		"     81  iload_3 [i]\n" + 
		"     82  iconst_3\n" + 
		"     83  if_icmpne 89\n" + 
		"     86  goto 111\n" + 
		"     89  iload [e]\n" + 
		"     91  iconst_5\n" + 
		"     92  if_icmpne 98\n" + 
		"     95  goto 108\n" + 
		"     98  iinc 5 1 [local_5]\n" + 
		"    101  iload [local_5]\n" + 
		"    103  iload [local_6]\n" + 
		"    105  if_icmplt 69\n" + 
		"    108  goto 52\n" + 
		"    111  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    114  iload_2 [sum]\n" + 
		"    115  invokevirtual java/io/PrintStream.println(I)V [27]\n" + 
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  new java/util/ArrayList [17]\n" + 
		"     3  dup\n" + 
		"     4  invokespecial java/util/ArrayList.<init>()V [18]\n" + 
		"     7  astore_1 [arrayList]\n" + 
		"     8  iconst_0\n" + 
		"     9  istore_2 [i]\n" + 
		"    10  goto 29\n" + 
		"    13  aload_1 [arrayList]\n" + 
		"    14  new java/lang/Integer [20]\n" + 
		"    17  dup\n" + 
		"    18  iload_2 [i]\n" + 
		"    19  invokespecial java/lang/Integer.<init>(I)V [23]\n" + 
		"    22  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [27]\n" + 
		"    25  pop\n" + 
		"    26  iinc 2 1 [i]\n" + 
		"    29  iload_2 [i]\n" + 
		"    30  bipush 10\n" + 
		"    32  if_icmplt 13\n" + 
		"    35  iconst_0\n" + 
		"    36  istore_2 [sum]\n" + 
		"    37  aload_1 [arrayList]\n" + 
		"    38  invokevirtual java/util/ArrayList.iterator()Ljava/util/Iterator; [31]\n" + 
		"    41  astore [local_4]\n" + 
		"    43  goto 64\n" + 
		"    46  aload [local_4]\n" + 
		"    48  invokeinterface java/util/Iterator.next()Ljava/lang/Object; [37] [nargs: 1]\n" + 
		"    53  checkcast java/lang/Integer [20]\n" + 
		"    56  astore_3 [e]\n" + 
		"    57  iload_2 [sum]\n" + 
		"    58  aload_3 [e]\n" + 
		"    59  invokevirtual java/lang/Integer.intValue()I [41]\n" + 
		"    62  iadd\n" + 
		"    63  istore_2 [sum]\n" + 
		"    64  aload [local_4]\n" + 
		"    66  invokeinterface java/util/Iterator.hasNext()Z [45] [nargs: 1]\n" + 
		"    71  ifne 46\n" + 
		"    74  getstatic java/lang/System.out Ljava/io/PrintStream; [51]\n" + 
		"    77  iload_2 [sum]\n" + 
		"    78  invokevirtual java/io/PrintStream.println(I)V [56]\n" + 
		"    81  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 5]\n" + 
		"        [pc: 8, line: 6]\n" + 
		"        [pc: 13, line: 7]\n" + 
		"        [pc: 26, line: 6]\n" + 
		"        [pc: 35, line: 9]\n" + 
		"        [pc: 37, line: 10]\n" + 
		"        [pc: 57, line: 11]\n" + 
		"        [pc: 64, line: 10]\n" + 
		"        [pc: 74, line: 13]\n" + 
		"        [pc: 81, line: 14]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 82] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 82] local: arrayList index: 1 type: Ljava/util/ArrayList;\n" + 
		"        [pc: 10, pc: 35] local: i index: 2 type: I\n" + 
		"        [pc: 37, pc: 82] local: sum index: 2 type: I\n" + 
		"        [pc: 57, pc: 74] local: e index: 3 type: Ljava/lang/Integer;\n" + 
		"      Local variable type table:\n" + 
		"        [pc: 8, pc: 82] local: arrayList index: 1 type: Ljava/util/ArrayList<Ljava/lang/Integer;>;\n";
	
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 4, Locals: 7\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  anewarray java/lang/String [17]\n" + 
		"     4  dup\n" + 
		"     5  iconst_0\n" + 
		"     6  ldc <String \"SUCCESS\"> [19]\n" + 
		"     8  aastore\n" + 
		"     9  astore_1 [tab]\n" + 
		"    10  new java/util/ArrayList [21]\n" + 
		"    13  dup\n" + 
		"    14  invokespecial java/util/ArrayList.<init>()V [22]\n" + 
		"    17  astore_2 [list]\n" + 
		"    18  aload_1 [tab]\n" + 
		"    19  astore [local_6]\n" + 
		"    21  iconst_0\n" + 
		"    22  istore [local_4]\n" + 
		"    24  aload [local_6]\n" + 
		"    26  arraylength\n" + 
		"    27  istore [local_5]\n" + 
		"    29  goto 49\n" + 
		"    32  aload [local_6]\n" + 
		"    34  iload [local_4]\n" + 
		"    36  aaload\n" + 
		"    37  astore_3 [arg]\n" + 
		"    38  aload_2 [list]\n" + 
		"    39  aload_3 [arg]\n" + 
		"    40  invokeinterface java/util/List.add(Ljava/lang/Object;)Z [28] [nargs: 2]\n" + 
		"    45  pop\n" + 
		"    46  iinc 4 1 [local_4]\n" + 
		"    49  iload [local_4]\n" + 
		"    51  iload [local_5]\n" + 
		"    53  if_icmplt 32\n" + 
		"    56  aload_2 [list]\n" + 
		"    57  invokeinterface java/util/List.iterator()Ljava/util/Iterator; [32] [nargs: 1]\n" + 
		"    62  astore [local_4]\n" + 
		"    64  goto 82\n" + 
		"    67  aload [local_4]\n" + 
		"    69  invokeinterface java/util/Iterator.next()Ljava/lang/Object; [38] [nargs: 1]\n" + 
		"    74  astore_3 [arg]\n" + 
		"    75  getstatic java/lang/System.out Ljava/io/PrintStream; [44]\n" + 
		"    78  aload_3 [arg]\n" + 
		"    79  invokevirtual java/io/PrintStream.print(Ljava/lang/Object;)V [50]\n" + 
		"    82  aload [local_4]\n" + 
		"    84  invokeinterface java/util/Iterator.hasNext()Z [54] [nargs: 1]\n" + 
		"    89  ifne 67\n" + 
		"    92  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 6]\n" + 
		"        [pc: 10, line: 7]\n" + 
		"        [pc: 18, line: 8]\n" + 
		"        [pc: 38, line: 9]\n" + 
		"        [pc: 46, line: 8]\n" + 
		"        [pc: 56, line: 11]\n" + 
		"        [pc: 75, line: 12]\n" + 
		"        [pc: 82, line: 11]\n" + 
		"        [pc: 92, line: 14]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 93] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 10, pc: 93] local: tab index: 1 type: [Ljava/lang/String;\n" + 
		"        [pc: 18, pc: 93] local: list index: 2 type: Ljava/util/List;\n" + 
		"        [pc: 38, pc: 56] local: arg index: 3 type: Ljava/lang/String;\n" + 
		"        [pc: 75, pc: 92] local: arg index: 3 type: Ljava/lang/Object;\n";
	
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
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  iconst_0\n" + 
		"     1  anewarray java/lang/Object [4]\n" + 
		"     4  astore_1 [array]\n" + 
		"     5  aload_1 [array]\n" + 
		"     6  astore [local_4]\n" + 
		"     8  iconst_0\n" + 
		"     9  istore_2 [local_2]\n" + 
		"    10  aload [local_4]\n" + 
		"    12  arraylength\n" + 
		"    13  istore_3 [local_3]\n" + 
		"    14  goto 27\n" + 
		"    17  aload_0 [args]\n" + 
		"    18  ifnonnull 26\n" + 
		"    21  aconst_null\n" + 
		"    22  pop\n" + 
		"    23  goto 32\n" + 
		"    26  return\n" + 
		"    27  iload_2 [local_2]\n" + 
		"    28  iload_3 [local_3]\n" + 
		"    29  if_icmplt 17\n" + 
		"    32  getstatic java/lang/System.out Ljava/io/PrintStream; [21]\n" + 
		"    35  ldc <String \"SUCCESS\"> [23]\n" + 
		"    37  invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V [29]\n" + 
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
		"        [pc: 5, pc: 41] local: array index: 1 type: [Ljava/lang/Object;\n";
	
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
//72760 - missing local variable attribute after foreach statement
public void test028() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"    	ArrayList<ArrayList<String>> slist = new ArrayList<ArrayList<String>>();\n" + 
			"    	\n" + 
			"    	slist.add(new ArrayList<String>());\n" + 
			"    	slist.get(0).add(\"SU\");\n" + 
			"    	slist.get(0).add(\"C\");\n" + 
			"    	slist.get(0).add(\"C\");\n" + 
			"    	\n" + 
			"    	slist.add(new ArrayList<String>());\n" + 
			"    	slist.get(1).add(\"E\");\n" + 
			"    	slist.get(1).add(\"S\");\n" + 
			"    	slist.get(1).add(\"S\");\n" + 
			"    	\n" + 
			"    	for (int i=0; i<slist.size(); i++){\n" + 
			"    		for (String s : slist.get(i)){\n" + 
			"    			System.out.print(s);\n" + 
			"    		}\n" + 
			"    	}\n" + 
			"    } \n" + 
			"} \n" + 
			"",
		},
		"SUCCESS");
		
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 3, Locals: 5\n" + 
		"  public static void main(String[] args);\n" + 
		"      0  new java/util/ArrayList [17]\n" + 
		"      3  dup\n" + 
		"      4  invokespecial java/util/ArrayList.<init>()V [18]\n" + 
		"      7  astore_1 [slist]\n" + 
		"      8  aload_1 [slist]\n" + 
		"      9  new java/util/ArrayList [17]\n" + 
		"     12  dup\n" + 
		"     13  invokespecial java/util/ArrayList.<init>()V [18]\n" + 
		"     16  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"     19  pop\n" + 
		"     20  aload_1 [slist]\n" + 
		"     21  iconst_0\n" + 
		"     22  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"     25  checkcast java/util/ArrayList [17]\n" + 
		"     28  ldc <String \"SU\"> [28]\n" + 
		"     30  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"     33  pop\n" + 
		"     34  aload_1 [slist]\n" + 
		"     35  iconst_0\n" + 
		"     36  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"     39  checkcast java/util/ArrayList [17]\n" + 
		"     42  ldc <String \"C\"> [30]\n" + 
		"     44  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"     47  pop\n" + 
		"     48  aload_1 [slist]\n" + 
		"     49  iconst_0\n" + 
		"     50  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"     53  checkcast java/util/ArrayList [17]\n" + 
		"     56  ldc <String \"C\"> [30]\n" + 
		"     58  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"     61  pop\n" + 
		"     62  aload_1 [slist]\n" + 
		"     63  new java/util/ArrayList [17]\n" + 
		"     66  dup\n" + 
		"     67  invokespecial java/util/ArrayList.<init>()V [18]\n" + 
		"     70  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"     73  pop\n" + 
		"     74  aload_1 [slist]\n" + 
		"     75  iconst_1\n" + 
		"     76  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"     79  checkcast java/util/ArrayList [17]\n" + 
		"     82  ldc <String \"E\"> [32]\n" + 
		"     84  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"     87  pop\n" + 
		"     88  aload_1 [slist]\n" + 
		"     89  iconst_1\n" + 
		"     90  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"     93  checkcast java/util/ArrayList [17]\n" + 
		"     96  ldc <String \"S\"> [34]\n" + 
		"     98  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"    101  pop\n" + 
		"    102  aload_1 [slist]\n" + 
		"    103  iconst_1\n" + 
		"    104  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"    107  checkcast java/util/ArrayList [17]\n" + 
		"    110  ldc <String \"S\"> [34]\n" + 
		"    112  invokevirtual java/util/ArrayList.add(Ljava/lang/Object;)Z [22]\n" + 
		"    115  pop\n" + 
		"    116  iconst_0\n" + 
		"    117  istore_2 [i]\n" + 
		"    118  goto 168\n" + 
		"    121  aload_1 [slist]\n" + 
		"    122  iload_2 [i]\n" + 
		"    123  invokevirtual java/util/ArrayList.get(I)Ljava/lang/Object; [26]\n" + 
		"    126  checkcast java/util/ArrayList [17]\n" + 
		"    129  invokevirtual java/util/ArrayList.iterator()Ljava/util/Iterator; [38]\n" + 
		"    132  astore [local_4]\n" + 
		"    134  goto 155\n" + 
		"    137  aload [local_4]\n" + 
		"    139  invokeinterface java/util/Iterator.next()Ljava/lang/Object; [44] [nargs: 1]\n" + 
		"    144  checkcast java/lang/String [46]\n" + 
		"    147  astore_3 [s]\n" + 
		"    148  getstatic java/lang/System.out Ljava/io/PrintStream; [52]\n" + 
		"    151  aload_3 [s]\n" + 
		"    152  invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V [58]\n" + 
		"    155  aload [local_4]\n" + 
		"    157  invokeinterface java/util/Iterator.hasNext()Z [62] [nargs: 1]\n" + 
		"    162  ifne 137\n" + 
		"    165  iinc 2 1 [i]\n" + 
		"    168  iload_2 [i]\n" + 
		"    169  aload_1 [slist]\n" + 
		"    170  invokevirtual java/util/ArrayList.size()I [66]\n" + 
		"    173  if_icmplt 121\n" + 
		"    176  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 6]\n" + 
		"        [pc: 8, line: 8]\n" + 
		"        [pc: 20, line: 9]\n" + 
		"        [pc: 34, line: 10]\n" + 
		"        [pc: 48, line: 11]\n" + 
		"        [pc: 62, line: 13]\n" + 
		"        [pc: 74, line: 14]\n" + 
		"        [pc: 88, line: 15]\n" + 
		"        [pc: 102, line: 16]\n" + 
		"        [pc: 116, line: 18]\n" + 
		"        [pc: 121, line: 19]\n" + 
		"        [pc: 148, line: 20]\n" + 
		"        [pc: 155, line: 19]\n" + 
		"        [pc: 165, line: 18]\n" + 
		"        [pc: 176, line: 23]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 177] local: args index: 0 type: [Ljava/lang/String;\n" + 
		"        [pc: 8, pc: 177] local: slist index: 1 type: Ljava/util/ArrayList;\n" + 
		"        [pc: 118, pc: 176] local: i index: 2 type: I\n" + 
		"        [pc: 148, pc: 165] local: s index: 3 type: Ljava/lang/String;\n" + 
		"      Local variable type table:\n" + 
		"        [pc: 8, pc: 177] local: slist index: 1 type: Ljava/util/ArrayList<Ljava/util/ArrayList<Ljava/lang/String;>;>;\n";
	
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test029() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"        ArrayList<Integer> arr = new ArrayList<Integer>();\n" + 
			"    	 arr.add(0);\n" + 
			"    	 arr.add(1);\n" + 
			"		 int counter = 0;\n" + 
			"        // tested statement:\n" + 
			"        for (int i : arr){\n" + 
			"            ++counter;\n" + 
			"        }\n" + 
			"        System.out.print(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test030() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"        int[] arr = new int[2];\n" + 
			"    	 arr[0]= 0;\n" + 
			"    	 arr[1]= 1;\n" + 
			"		 int counter = 0;\n" + 
			"        // tested statement:\n" + 
			"        for (int i : arr){\n" + 
			"            ++counter;\n" + 
			"        }\n" + 
			"        System.out.print(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test031() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"\n" + 
			"public class X {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"        ArrayList arr = new ArrayList();\n" + 
			"    	 arr.add(new Object());\n" + 
			"		 int counter = 0;\n" + 
			"        // tested statement:\n" + 
			"        for (Object o : arr){\n" + 
			"            ++counter;\n" + 
			"        }\n" + 
			"        System.out.print(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}",
		},
		"SUCCESS");
}
public void test032() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" + 
			"	abstract class Member implements Iterable<String> {\n" + 
			"	}\n" + 
			"	void foo(Member m) {\n" + 
			"		for(String s : m) {\n" + 
			"			return;\n" + 
			"		} \n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public static Class testClass() {
	return ForeachStatementTest.class;
}
}
