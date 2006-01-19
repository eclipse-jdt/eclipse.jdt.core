/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

import junit.framework.Test;

public class StackMapAttributeTest extends AbstractRegressionTest {
	public StackMapAttributeTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return StackMapAttributeTest.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug83127a" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 23, -1 };
	}
	public static Test suite() {
		return buildTestSuiteUniqueCompliance(testClass(), COMPLIANCE_1_6);
	}
	public void test001() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	X() {}\n" + 
					"	X(double i) {\n" + 
					"		this(i > 0 ? null : new Object());\n" + 
					"		try {\n" + 
					"			foo(6, false);\n" + 
					"		} catch(Exception e) {\n" + 
					"			e.printStackTrace();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	X(Object o) {}\n" + 
					"	int foo(int i, boolean b) {\n" + 
					"		try {\n" + 
					"			if (b) {\n" + 
					"				return i;\n" + 
					"			}\n" + 
					"			return i + 1;\n" + 
					"		} catch(Exception e) {\n" + 
					"			return 5;\n" + 
					"		}\n" + 
					"	}\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new X().foo(2, false);\n" + 
					"		System.out.println(\"SUCCESS\");\n" + 
					"	}\n" + 
					"}",
				},
				"SUCCESS");
				
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String actualOutput = null;
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				actualOutput =
					disassembler.disassemble(
						classFileBytes,
						"\n",
						ClassFileBytesDisassembler.DETAILED); 
			} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
			
			String expectedOutput = 
				"  // Method descriptor #14 (D)V\n" + 
				"  // Stack: 5, Locals: 4\n" + 
				"  X(double i);\n" + 
				"     0  aload_0 [this]\n" + 
				"     1  dload_1 [i]\n" + 
				"     2  dconst_0\n" + 
				"     3  dcmpl\n" + 
				"     4  ifle 11\n" + 
				"     7  aconst_null\n" + 
				"     8  goto 18\n" + 
				"    11  new java.lang.Object [3]\n" + 
				"    14  dup\n" + 
				"    15  invokespecial java.lang.Object() [8]\n" + 
				"    18  invokespecial X(java.lang.Object) [15]\n" + 
				"    21  aload_0 [this]\n" + 
				"    22  bipush 6\n" + 
				"    24  iconst_0\n" + 
				"    25  invokevirtual X.foo(int, boolean) : int [18]\n" + 
				"    28  pop\n" + 
				"    29  goto 37\n" + 
				"    32  astore_3 [e]\n" + 
				"    33  aload_3 [e]\n" + 
				"    34  invokevirtual java.lang.Exception.printStackTrace() : void [22]\n" + 
				"    37  return\n" + 
				"      Exception Table:\n" + 
				"        [pc: 21, pc: 32] -> 32 when : java.lang.Exception\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 4]\n" + 
				"        [pc: 21, line: 6]\n" + 
				"        [pc: 32, line: 7]\n" + 
				"        [pc: 33, line: 8]\n" + 
				"        [pc: 37, line: 10]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 38] local: this index: 0 type: X\n" + 
				"        [pc: 0, pc: 38] local: i index: 1 type: double\n" + 
				"        [pc: 33, pc: 37] local: e index: 3 type: java.lang.Exception\n" + 
				"      Stack map table: number of frames 4\n" + 
				"        [pc: 11, same_locals_1_stack_item, stack: {uninitialized_this}]\n" + 
				"        [pc: 18, full, stack: {uninitialized_this, java.lang.Object}, locals: {uninitialized_this, double}]\n" + 
				"        [pc: 32, full, stack: {java.lang.Exception}, locals: {X, double}]\n" + 
				"        [pc: 37, same]\n" + 
				"  \n" + 
				"  // Method descriptor #17 (Ljava/lang/Object;)V\n" + 
				"  // Stack: 1, Locals: 2\n" + 
				"  X(java.lang.Object o);\n" + 
				"    0  aload_0 [this]\n" + 
				"    1  invokespecial java.lang.Object() [8]\n" + 
				"    4  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 11]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" + 
				"        [pc: 0, pc: 5] local: o index: 1 type: java.lang.Object\n" + 
				"  \n" + 
				"  // Method descriptor #21 (IZ)I\n" + 
				"  // Stack: 2, Locals: 4\n" + 
				"  int foo(int i, boolean b);\n" + 
				"     0  iload_2 [b]\n" + 
				"     1  ifeq 6\n" + 
				"     4  iload_1 [i]\n" + 
				"     5  ireturn\n" + 
				"     6  iload_1 [i]\n" + 
				"     7  iconst_1\n" + 
				"     8  iadd\n" + 
				"     9  ireturn\n" + 
				"    10  astore_3 [e]\n" + 
				"    11  iconst_5\n" + 
				"    12  ireturn\n" + 
				"      Exception Table:\n" + 
				"        [pc: 0, pc: 10] -> 10 when : java.lang.Exception\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 14]\n" + 
				"        [pc: 4, line: 15]\n" + 
				"        [pc: 6, line: 17]\n" + 
				"        [pc: 10, line: 18]\n" + 
				"        [pc: 11, line: 19]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 13] local: this index: 0 type: X\n" + 
				"        [pc: 0, pc: 13] local: i index: 1 type: int\n" + 
				"        [pc: 0, pc: 13] local: b index: 2 type: boolean\n" + 
				"        [pc: 11, pc: 13] local: e index: 3 type: java.lang.Exception\n" + 
				"      Stack map table: number of frames 2\n" + 
				"        [pc: 6, same]\n" + 
				"        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]\n";
			
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test002() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static void foo(double d, boolean b) {\n" + 
					"		double i;\n" + 
					"		try {\n" + 
					"			i = 0;\n" + 
					"			i++;\n" + 
					"			int j = (int) (i - 1);\n" + 
					"			if (b) {\n" + 
					"				double d1 = 0;\n" + 
					"				if (!b) {\n" + 
					"					d1 = 0;\n" + 
					"				}\n" + 
					"				double d2 = d + d1;\n" + 
					"			}\n" + 
					"			bar(j);\n" + 
					"		} catch(NullPointerException e) {\n" + 
					"			i = 2;\n" + 
					"		} finally {\n" + 
					"			i = 1;\n" + 
					"		}\n" + 
					"		long j = (long) (i + 1);\n" + 
					"		int k = (int) j;\n" + 
					"		k += j;\n" + 
					"	}\n" + 
					"	public static void bar(int i) {}\n" + 
					"	public static void main(String[] args) {\n" + 
					"		foo(0, true);\n" + 
					"		System.out.println(\"SUCCESS\");\n" + 
					"	}\n" + 
					"}",
				},
				"SUCCESS");
				
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String actualOutput = null;
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				actualOutput =
					disassembler.disassemble(
						classFileBytes,
						"\n",
						ClassFileBytesDisassembler.DETAILED); 
			} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
			
			String expectedOutput = 
				"  // Method descriptor #15 (DZ)V\n" + 
				"  // Stack: 4, Locals: 11\n" + 
				"  public static void foo(double d, boolean b);\n" + 
				"     0  dconst_0\n" + 
				"     1  dstore_3 [i]\n" + 
				"     2  dload_3 [i]\n" + 
				"     3  dconst_1\n" + 
				"     4  dadd\n" + 
				"     5  dstore_3 [i]\n" + 
				"     6  dload_3 [i]\n" + 
				"     7  dconst_1\n" + 
				"     8  dsub\n" + 
				"     9  d2i\n" + 
				"    10  istore 5 [j]\n" + 
				"    12  iload_2 [b]\n" + 
				"    13  ifeq 32\n" + 
				"    16  dconst_0\n" + 
				"    17  dstore 6 [d1]\n" + 
				"    19  iload_2 [b]\n" + 
				"    20  ifne 26\n" + 
				"    23  dconst_0\n" + 
				"    24  dstore 6 [d1]\n" + 
				"    26  dload_0 [d]\n" + 
				"    27  dload 6 [d1]\n" + 
				"    29  dadd\n" + 
				"    30  dstore 8\n" + 
				"    32  iload 5 [j]\n" + 
				"    34  invokestatic X.bar(int) : void [16]\n" + 
				"    37  goto 56\n" + 
				"    40  astore 5 [e]\n" + 
				"    42  ldc2_w <Double 2.0> [20]\n" + 
				"    45  dstore_3 [i]\n" + 
				"    46  goto 56\n" + 
				"    49  astore 10\n" + 
				"    51  dconst_1\n" + 
				"    52  dstore_3 [i]\n" + 
				"    53  aload 10\n" + 
				"    55  athrow\n" + 
				"    56  dconst_1\n" + 
				"    57  dstore_3 [i]\n" + 
				"    58  dload_3 [i]\n" + 
				"    59  dconst_1\n" + 
				"    60  dadd\n" + 
				"    61  d2l\n" + 
				"    62  lstore 5 [j]\n" + 
				"    64  lload 5 [j]\n" + 
				"    66  l2i\n" + 
				"    67  istore 7 [k]\n" + 
				"    69  iload 7 [k]\n" + 
				"    71  i2l\n" + 
				"    72  lload 5 [j]\n" + 
				"    74  ladd\n" + 
				"    75  l2i\n" + 
				"    76  istore 7 [k]\n" + 
				"    78  return\n" + 
				"      Exception Table:\n" + 
				"        [pc: 0, pc: 40] -> 40 when : java.lang.NullPointerException\n" + 
				"        [pc: 0, pc: 49] -> 49 when : any\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 5]\n" + 
				"        [pc: 2, line: 6]\n" + 
				"        [pc: 6, line: 7]\n" + 
				"        [pc: 12, line: 8]\n" + 
				"        [pc: 16, line: 9]\n" + 
				"        [pc: 19, line: 10]\n" + 
				"        [pc: 23, line: 11]\n" + 
				"        [pc: 26, line: 13]\n" + 
				"        [pc: 32, line: 15]\n" + 
				"        [pc: 40, line: 16]\n" + 
				"        [pc: 42, line: 17]\n" + 
				"        [pc: 49, line: 18]\n" + 
				"        [pc: 51, line: 19]\n" + 
				"        [pc: 53, line: 20]\n" + 
				"        [pc: 56, line: 19]\n" + 
				"        [pc: 58, line: 21]\n" + 
				"        [pc: 64, line: 22]\n" + 
				"        [pc: 69, line: 23]\n" + 
				"        [pc: 78, line: 24]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 79] local: d index: 0 type: double\n" + 
				"        [pc: 0, pc: 79] local: b index: 2 type: boolean\n" + 
				"        [pc: 2, pc: 40] local: i index: 3 type: double\n" + 
				"        [pc: 46, pc: 49] local: i index: 3 type: double\n" + 
				"        [pc: 53, pc: 56] local: i index: 3 type: double\n" + 
				"        [pc: 58, pc: 79] local: i index: 3 type: double\n" + 
				"        [pc: 12, pc: 40] local: j index: 5 type: int\n" + 
				"        [pc: 19, pc: 32] local: d1 index: 6 type: double\n" + 
				"        [pc: 42, pc: 46] local: e index: 5 type: java.lang.NullPointerException\n" + 
				"        [pc: 64, pc: 79] local: j index: 5 type: long\n" + 
				"        [pc: 69, pc: 79] local: k index: 7 type: int\n" + 
				"      Stack map table: number of frames 5\n" + 
				"        [pc: 26, append: {double, int, double}]\n" + 
				"        [pc: 32, chop 1 local(s)]\n" + 
				"        [pc: 40, full, stack: {java.lang.NullPointerException}, locals: {double, int}]\n" + 
				"        [pc: 49, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
				"        [pc: 56, append: {double}]\n";
			
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test003() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static void foo(boolean b) {\n" + 
					"		int i = 0;\n" + 
					"		try {\n" + 
					"			System.out.println(\"FINALLY\");\n" + 
					"			i++;\n" + 
					"			int j = i -1;\n" + 
					"			bar(j);\n" + 
					"		} catch(NullPointerException e) {\n" + 
					"			e.printStackTrace();\n" + 
					"		} finally {\n" + 
					"			System.out.println(\"FINALLY\");\n" + 
					"		}\n" + 
					"	}\n" + 
					"	public static void bar(int i) {}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		foo(true);\n" + 
					"		System.out.println(\"SUCCESS\");\n" + 
					"	}\n" + 
					"}",
				},
				"FINALLY\n" + 
				"FINALLY\n" + 
				"SUCCESS");
				
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String actualOutput = null;
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				actualOutput =
					disassembler.disassemble(
						classFileBytes,
						"\n",
						ClassFileBytesDisassembler.DETAILED); 
			} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
			
			String expectedOutput = 
				"  // Method descriptor #15 (Z)V\n" + 
				"  // Stack: 2, Locals: 4\n" + 
				"  public static void foo(boolean b);\n" + 
				"     0  iconst_0\n" + 
				"     1  istore_1 [i]\n" + 
				"     2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
				"     5  ldc <String \"FINALLY\"> [22]\n" + 
				"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
				"    10  iinc 1 1 [i]\n" + 
				"    13  iload_1 [i]\n" + 
				"    14  iconst_1\n" + 
				"    15  isub\n" + 
				"    16  istore_2 [j]\n" + 
				"    17  iload_2 [j]\n" + 
				"    18  invokestatic X.bar(int) : void [30]\n" + 
				"    21  goto 43\n" + 
				"    24  astore_2 [e]\n" + 
				"    25  aload_2 [e]\n" + 
				"    26  invokevirtual java.lang.NullPointerException.printStackTrace() : void [34]\n" + 
				"    29  goto 43\n" + 
				"    32  astore_3\n" + 
				"    33  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
				"    36  ldc <String \"FINALLY\"> [22]\n" + 
				"    38  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
				"    41  aload_3\n" + 
				"    42  athrow\n" + 
				"    43  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
				"    46  ldc <String \"FINALLY\"> [22]\n" + 
				"    48  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
				"    51  return\n" + 
				"      Exception Table:\n" + 
				"        [pc: 2, pc: 24] -> 24 when : java.lang.NullPointerException\n" + 
				"        [pc: 2, pc: 32] -> 32 when : any\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 3]\n" + 
				"        [pc: 2, line: 5]\n" + 
				"        [pc: 10, line: 6]\n" + 
				"        [pc: 13, line: 7]\n" + 
				"        [pc: 17, line: 8]\n" + 
				"        [pc: 24, line: 9]\n" + 
				"        [pc: 25, line: 10]\n" + 
				"        [pc: 32, line: 11]\n" + 
				"        [pc: 33, line: 12]\n" + 
				"        [pc: 41, line: 13]\n" + 
				"        [pc: 43, line: 12]\n" + 
				"        [pc: 51, line: 14]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 52] local: b index: 0 type: boolean\n" + 
				"        [pc: 2, pc: 52] local: i index: 1 type: int\n" + 
				"        [pc: 17, pc: 24] local: j index: 2 type: int\n" + 
				"        [pc: 25, pc: 29] local: e index: 2 type: java.lang.NullPointerException\n" + 
				"      Stack map table: number of frames 3\n" + 
				"        [pc: 24, full, stack: {java.lang.NullPointerException}, locals: {int, int}]\n" + 
				"        [pc: 32, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
				"        [pc: 43, same]\n";
			
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test004() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static void foo(boolean b) {\n" + 
					"		C c;\n" + 
					"		if (b) {\n" + 
					"			c = new C1();\n" + 
					"		} else {\n" + 
					"			c = new C2();\n" + 
					"		}\n" + 
					"		System.out.println();\n" + 
					"	}\n" + 
					"	public static void main(String[] args) {\n" + 
					"		foo(true);\n" + 
					"		System.out.println(\"SUCCESS\");\n" + 
					"	}\n" + 
					"}\n" +
					"class C {\n" + 
					"	void foo() {}\n" + 
					"}\n" + 
					"class C1 extends C {\n" + 
					"}\n" + 
					"class C2 extends C {\n" + 
					"}",
				},
				"SUCCESS");
				
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String actualOutput = null;
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				actualOutput =
					disassembler.disassemble(
						classFileBytes,
						"\n",
						ClassFileBytesDisassembler.DETAILED); 
			} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
			
			String expectedOutput = 
				"  // Method descriptor #15 (Z)V\n" + 
				"  // Stack: 2, Locals: 2\n" + 
				"  public static void foo(boolean b);\n" + 
				"     0  iload_0 [b]\n" + 
				"     1  ifeq 15\n" + 
				"     4  new C1 [16]\n" + 
				"     7  dup\n" + 
				"     8  invokespecial C1() [18]\n" + 
				"    11  astore_1 [c]\n" + 
				"    12  goto 23\n" + 
				"    15  new C2 [19]\n" + 
				"    18  dup\n" + 
				"    19  invokespecial C2() [21]\n" + 
				"    22  astore_1 [c]\n" + 
				"    23  getstatic java.lang.System.out : java.io.PrintStream [22]\n" + 
				"    26  invokevirtual java.io.PrintStream.println() : void [28]\n" + 
				"    29  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 4]\n" + 
				"        [pc: 4, line: 5]\n" + 
				"        [pc: 15, line: 7]\n" + 
				"        [pc: 23, line: 9]\n" + 
				"        [pc: 29, line: 10]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 30] local: b index: 0 type: boolean\n" + 
				"        [pc: 12, pc: 15] local: c index: 1 type: C\n" + 
				"        [pc: 23, pc: 30] local: c index: 1 type: C\n" + 
				"      Stack map table: number of frames 2\n" + 
				"        [pc: 15, same]\n" + 
				"        [pc: 23, append: {C}]\n";
			
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test005() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"  public static void main(String args[]) {\n" + 
					"    int i = 0, j, k, l;\n" + 
					"    boolean b;\n" + 
					"    if (i == 0 && (j = ++i) > 0)\n" + 
					"      i += j;\n" + 
					"    while (true) {\n" + 
					"      k = 3;\n" + 
					"      break;\n" + 
					"    } \n" + 
					"    i -= k;\n" + 
					"    b = false && (i = l) > 0;\n" + 
					"    if (i > 0)\n" + 
					"      l = i;\n" + 
					"    else\n" + 
					"      l = k;\n" + 
					"    j = l;\n" + 
					"    if (i != -1 || j != 3 || k != 3 || l != 3)\n" + 
					"      System.out.println(\"FAILED\");\n" + 
					"    System.out.println(\"SUCCESS\");\n" + 
					"  }\n" + 
					"}",
				},
				"SUCCESS");
				
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String actualOutput = null;
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				actualOutput =
					disassembler.disassemble(
						classFileBytes,
						"\n",
						ClassFileBytesDisassembler.DETAILED); 
			} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
			
			String expectedOutput = 
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
				"  // Stack: 2, Locals: 6\n" + 
				"  public static void main(java.lang.String[] args);\n" + 
				"     0  iconst_0\n" + 
				"     1  istore_1 [i]\n" + 
				"     2  iload_1 [i]\n" + 
				"     3  ifne 19\n" + 
				"     6  iinc 1 1 [i]\n" + 
				"     9  iload_1 [i]\n" + 
				"    10  dup\n" + 
				"    11  istore_2 [j]\n" + 
				"    12  ifle 19\n" + 
				"    15  iload_1 [i]\n" + 
				"    16  iload_2 [j]\n" + 
				"    17  iadd\n" + 
				"    18  istore_1 [i]\n" + 
				"    19  iconst_3\n" + 
				"    20  istore_3 [k]\n" + 
				"    21  iload_1 [i]\n" + 
				"    22  iload_3 [k]\n" + 
				"    23  isub\n" + 
				"    24  istore_1 [i]\n" + 
				"    25  iconst_0\n" + 
				"    26  istore 5 [b]\n" + 
				"    28  iload_1 [i]\n" + 
				"    29  ifle 38\n" + 
				"    32  iload_1 [i]\n" + 
				"    33  istore 4 [l]\n" + 
				"    35  goto 41\n" + 
				"    38  iload_3 [k]\n" + 
				"    39  istore 4 [l]\n" + 
				"    41  iload 4 [l]\n" + 
				"    43  istore_2 [j]\n" + 
				"    44  iload_1 [i]\n" + 
				"    45  iconst_m1\n" + 
				"    46  if_icmpne 65\n" + 
				"    49  iload_2 [j]\n" + 
				"    50  iconst_3\n" + 
				"    51  if_icmpne 65\n" + 
				"    54  iload_3 [k]\n" + 
				"    55  iconst_3\n" + 
				"    56  if_icmpne 65\n" + 
				"    59  iload 4 [l]\n" + 
				"    61  iconst_3\n" + 
				"    62  if_icmpeq 73\n" + 
				"    65  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
				"    68  ldc <String \"FAILED\"> [22]\n" + 
				"    70  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
				"    73  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
				"    76  ldc <String \"SUCCESS\"> [30]\n" + 
				"    78  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
				"    81  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 3]\n" + 
				"        [pc: 2, line: 5]\n" + 
				"        [pc: 15, line: 6]\n" + 
				"        [pc: 19, line: 8]\n" + 
				"        [pc: 21, line: 11]\n" + 
				"        [pc: 25, line: 12]\n" + 
				"        [pc: 28, line: 13]\n" + 
				"        [pc: 32, line: 14]\n" + 
				"        [pc: 38, line: 16]\n" + 
				"        [pc: 41, line: 17]\n" + 
				"        [pc: 44, line: 18]\n" + 
				"        [pc: 65, line: 19]\n" + 
				"        [pc: 73, line: 20]\n" + 
				"        [pc: 81, line: 21]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 82] local: args index: 0 type: java.lang.String[]\n" + 
				"        [pc: 2, pc: 82] local: i index: 1 type: int\n" + 
				"        [pc: 12, pc: 19] local: j index: 2 type: int\n" + 
				"        [pc: 44, pc: 82] local: j index: 2 type: int\n" + 
				"        [pc: 21, pc: 82] local: k index: 3 type: int\n" + 
				"        [pc: 35, pc: 38] local: l index: 4 type: int\n" + 
				"        [pc: 41, pc: 82] local: l index: 4 type: int\n" + 
				"        [pc: 28, pc: 82] local: b index: 5 type: boolean\n" + 
				"      Stack map table: number of frames 5\n" + 
				"        [pc: 19, append: {int}]\n" + 
				"        [pc: 38, full, stack: {}, locals: {java.lang.String[], int, _, int, _, int}]\n" + 
				"        [pc: 41, full, stack: {}, locals: {java.lang.String[], int, _, int, int, int}]\n" + 
				"        [pc: 65, full, stack: {}, locals: {java.lang.String[], int, int, int, int, int}]\n" + 
				"        [pc: 73, same]\n";
			
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	
	public void test006() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	void foo (int n) {\n" + 
					"   	synchronized (this) {\n" + 
					"       	switch (n) {\n" + 
					"       		case 1:\n" + 
					"       		throw new NullPointerException();\n" + 
					"			}\n" + 
					"		}\n" + 
					"	}\n" + 
					"    public static void main(String args[]) {\n" + 
					"    	try {\n" + 
					"	    	new X().foo(1);\n" + 
					"    	} catch(Exception e) {\n" + 
					"	        System.out.println(\"SUCCESS\"); \n" + 
					"    	}\n" + 
					"    } \n" + 
					"}",
				},
				"SUCCESS");
				
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String actualOutput = null;
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				actualOutput =
					disassembler.disassemble(
						classFileBytes,
						"\n",
						ClassFileBytesDisassembler.DETAILED); 
			} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
			
			String expectedOutput = 
				"  // Method descriptor #15 (I)V\n" + 
				"  // Stack: 2, Locals: 3\n" + 
				"  void foo(int n);\n" + 
				"     0  aload_0 [this]\n" + 
				"     1  dup\n" + 
				"     2  astore_2\n" + 
				"     3  monitorenter\n" + 
				"     4  iload_1 [n]\n" + 
				"     5  tableswitch default: 32\n" + 
				"          case 1: 24\n" + 
				"    24  new java.lang.NullPointerException [16]\n" + 
				"    27  dup\n" + 
				"    28  invokespecial java.lang.NullPointerException() [18]\n" + 
				"    31  athrow\n" + 
				"    32  aload_2\n" + 
				"    33  monitorexit\n" + 
				"    34  goto 40\n" + 
				"    37  aload_2\n" + 
				"    38  monitorexit\n" + 
				"    39  athrow\n" + 
				"    40  return\n" + 
				"      Exception Table:\n" + 
				"        [pc: 4, pc: 34] -> 37 when : any\n" + 
				"        [pc: 37, pc: 39] -> 37 when : any\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 3]\n" + 
				"        [pc: 4, line: 4]\n" + 
				"        [pc: 24, line: 6]\n" + 
				"        [pc: 32, line: 3]\n" + 
				"        [pc: 40, line: 9]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 41] local: this index: 0 type: X\n" + 
				"        [pc: 0, pc: 41] local: n index: 1 type: int\n" + 
				"      Stack map table: number of frames 4\n" + 
				"        [pc: 24, append: {X}]\n" + 
				"        [pc: 32, same]\n" + 
				"        [pc: 37, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
				"        [pc: 40, chop 1 local(s)]\n";
			
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
}
