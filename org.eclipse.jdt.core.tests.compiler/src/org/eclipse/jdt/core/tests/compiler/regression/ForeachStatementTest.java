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
import junit.framework.TestSuite;

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
public static Test suite() {
	if (false) {
		TestSuite suite = new TestSuite();
		suite.addTest(new ForeachStatementTest("test021"));
		return suite;
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
			"    59  iload 4\n" + 
			"    61  iload 5\n" + 
			"    63  if_icmpge 80\n" + 
			"    66  aload 6\n" + 
			"    68  iload 4\n" + 
			"    70  iaload\n" + 
			"    71  istore_3\n" + 
			"    72  iload_3\n" + 
			"    73  istore_1\n" + 
			"    74  iinc 4 1\n" + 
			"    77  goto 59\n" + 
			"    80  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"    83  ldc #23 <String \"SUCCESS\">\n" + 
			"    85  invokevirtual #29 <Method java.io.PrintStream#println(java.lang.String arg) void>\n" + 
			"    88  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"        [pc: 48, line: 6]\n" + 
			"        [pc: 72, line: 7]\n" + 
			"        [pc: 74, line: 6]\n" + 
			"        [pc: 80, line: 9]\n" + 
			"        [pc: 88, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 89] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 74, pc: 89] local: i index: 1 type: int\n" + 
			"        [pc: 48, pc: 89] local: tab index: 2 type: int[]\n" + 
			"        [pc: 72, pc: 80] local: e index: 3 type: int\n";
	
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
// TODO (kent) disabled until binary support is added for generics
public void _test009() { 
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
		"     8  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"    11  ldc #23 <String \"SUCCESS\">\n" + 
		"    13  invokevirtual #29 <Method java.io.PrintStream#println(java.lang.String arg) void>\n" + 
		"    16  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 7]\n" + 
		"        [pc: 16, line: 8]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 8, pc: 17] local: tab index: 1 type: int[]\n";
	
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
		"     8  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"    11  ldc #23 <String \"SUCCESS\">\n" + 
		"    13  invokevirtual #29 <Method java.io.PrintStream#println(java.lang.String arg) void>\n" + 
		"    16  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 6]\n" + 
		"        [pc: 16, line: 7]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 8, pc: 17] local: tab index: 1 type: int[]\n";
	
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
			"    17  iload_2\n" + 
			"    18  iload_3\n" + 
			"    19  if_icmpge 28\n" + 
			"    22  iinc 2 1\n" + 
			"    25  goto 17\n" + 
			"    28  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"    31  ldc #23 <String \"SUCCESS\">\n" + 
			"    33  invokevirtual #29 <Method java.io.PrintStream#println(java.lang.String arg) void>\n" + 
			"    36  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"        [pc: 8, line: 5]\n" + 
			"        [pc: 28, line: 7]\n" + 
			"        [pc: 36, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 8, pc: 37] local: tab index: 1 type: int[]\n";
	
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
			"		for (final int e : tab) {" +
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
			"		for (final int e : tab) {" +
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
			"    18  iload_3\n" + 
			"    19  iload 4\n" + 
			"    21  if_icmpge 36\n" + 
			"    24  aload 5\n" + 
			"    26  iload_3\n" + 
			"    27  iaload\n" + 
			"    28  istore_2\n" + 
			"    29  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"    32  iload_2\n" + 
			"    33  invokevirtual #27 <Method java.io.PrintStream#println(int arg) void>\n" + 
			"    36  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"        [pc: 8, line: 5]\n" + 
			"        [pc: 29, line: 5]\n" + 
			"        [pc: 36, line: 8]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 8, pc: 37] local: tab index: 1 type: int[]\n" + 
			"        [pc: 29, pc: 36] local: e index: 2 type: int\n";
	
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
			"		for (final int e : tab) {" +
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
			"     4  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"     7  ldc #23 <String \"SUC\">\n" + 
			"     9  invokevirtual #29 <Method java.io.PrintStream#print(java.lang.String arg) void>\n" + 
			"    12  aload_1\n" + 
			"    13  astore 4\n" + 
			"    15  iconst_0\n" + 
			"    16  istore_2\n" + 
			"    17  aload 4\n" + 
			"    19  arraylength\n" + 
			"    20  istore_3\n" + 
			"    21  iload_2\n" + 
			"    22  iload_3\n" + 
			"    23  if_icmpge 34\n" + 
			"    26  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"    29  ldc #31 <String \"1x\">\n" + 
			"    31  invokevirtual #29 <Method java.io.PrintStream#print(java.lang.String arg) void>\n" + 
			"    34  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"    37  ldc #33 <String \"CESS\">\n" + 
			"    39  invokevirtual #36 <Method java.io.PrintStream#println(java.lang.String arg) void>\n" + 
			"    42  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"        [pc: 4, line: 5]\n" + 
			"        [pc: 12, line: 6]\n" + 
			"        [pc: 26, line: 6]\n" + 
			"        [pc: 34, line: 9]\n" + 
			"        [pc: 42, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 43] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 4, pc: 43] local: tab index: 1 type: int[]\n";
	
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
			"		loop: for (final int e : tab) {" +
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
		"     4  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"     7  ldc #23 <String \"SUC\">\n" + 
		"     9  invokevirtual #29 <Method java.io.PrintStream#print(java.lang.String arg) void>\n" + 
		"    12  aload_1\n" + 
		"    13  astore 4\n" + 
		"    15  iconst_0\n" + 
		"    16  istore_2\n" + 
		"    17  aload 4\n" + 
		"    19  arraylength\n" + 
		"    20  istore_3\n" + 
		"    21  iload_2\n" + 
		"    22  iload_3\n" + 
		"    23  if_icmpge 40\n" + 
		"    26  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"    29  ldc #31 <String \"1x\">\n" + 
		"    31  invokevirtual #29 <Method java.io.PrintStream#print(java.lang.String arg) void>\n" + 
		"    34  iinc 2 1\n" + 
		"    37  goto 21\n" + 
		"    40  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"    43  ldc #33 <String \"CESS\">\n" + 
		"    45  invokevirtual #36 <Method java.io.PrintStream#println(java.lang.String arg) void>\n" + 
		"    48  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 4, line: 5]\n" + 
		"        [pc: 12, line: 6]\n" + 
		"        [pc: 26, line: 6]\n" + 
		"        [pc: 40, line: 9]\n" + 
		"        [pc: 48, line: 10]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 49] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 4, pc: 49] local: tab index: 1 type: int[]\n";
	
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
		"     66  iload 5\n" + 
		"     68  iload 6\n" + 
		"     70  if_icmpge 108\n" + 
		"     73  aload 7\n" + 
		"     75  iload 5\n" + 
		"     77  iaload\n" + 
		"     78  istore 4\n" + 
		"     80  iload_2\n" + 
		"     81  iload 4\n" + 
		"     83  iadd\n" + 
		"     84  istore_2\n" + 
		"     85  iload_3\n" + 
		"     86  iconst_3\n" + 
		"     87  if_icmpne 93\n" + 
		"     90  goto 111\n" + 
		"     93  iload 4\n" + 
		"     95  iconst_5\n" + 
		"     96  if_icmpne 102\n" + 
		"     99  goto 108\n" + 
		"    102  iinc 5 1\n" + 
		"    105  goto 66\n" + 
		"    108  goto 52\n" + 
		"    111  getstatic #21 <Field java.lang.System#out java.io.PrintStream>\n" + 
		"    114  iload_2\n" + 
		"    115  invokevirtual #27 <Method java.io.PrintStream#println(int arg) void>\n" + 
		"    118  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 48, line: 4]\n" + 
		"        [pc: 50, line: 5]\n" + 
		"        [pc: 52, line: 7]\n" + 
		"        [pc: 55, line: 8]\n" + 
		"        [pc: 80, line: 9]\n" + 
		"        [pc: 85, line: 10]\n" + 
		"        [pc: 90, line: 11]\n" + 
		"        [pc: 93, line: 12]\n" + 
		"        [pc: 99, line: 13]\n" + 
		"        [pc: 102, line: 8]\n" + 
		"        [pc: 108, line: 6]\n" + 
		"        [pc: 111, line: 19]\n" + 
		"        [pc: 118, line: 20]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 119] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 48, pc: 119] local: tab index: 1 type: int[]\n" + 
		"        [pc: 50, pc: 119] local: sum index: 2 type: int\n" + 
		"        [pc: 52, pc: 119] local: i index: 3 type: int\n" + 
		"        [pc: 80, pc: 108] local: e index: 4 type: int\n";
	
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
// TODO (olivier) add tests to challenge break/continue support in foreach
public static Class testClass() {
	return ForeachStatementTest.class;
}
}
