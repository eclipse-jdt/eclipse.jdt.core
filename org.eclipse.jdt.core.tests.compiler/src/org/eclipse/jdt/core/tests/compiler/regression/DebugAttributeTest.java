/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
import java.io.File;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

@SuppressWarnings({ "rawtypes" })
public class DebugAttributeTest extends AbstractRegressionTest {

	public DebugAttributeTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return DebugAttributeTest.class;
	}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=124212
 */
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        String s;
				        if(args.length == 0) {
				          s = "SUCCESS";
				        } else {
				          return;
				        }
				        System.out.println(s);
				    }
				}""",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		      Local variable table:
		        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 11] local: s index: 1 type: java.lang.String
		        [pc: 12, pc: 20] local: s index: 1 type: java.lang.String
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=205046
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.print("line 1");
						myBlock: {
							System.out.print("line 2");
							if (false) {
								break myBlock;
							}
							System.out.print("line 3");
						}
						System.out.print("line 4");
					}\
				}""",
		},
		"line 1line 2line 3line 4");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  getstatic java.lang.System.out : java.io.PrintStream [16]
		     3  ldc <String "line 1"> [22]
		     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		     8  getstatic java.lang.System.out : java.io.PrintStream [16]
		    11  ldc <String "line 2"> [30]
		    13  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    16  getstatic java.lang.System.out : java.io.PrintStream [16]
		    19  ldc <String "line 3"> [32]
		    21  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    24  getstatic java.lang.System.out : java.io.PrintStream [16]
		    27  ldc <String "line 4"> [34]
		    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    32  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 8, line: 5]
		        [pc: 16, line: 9]
		        [pc: 24, line: 11]
		        [pc: 32, line: 12]
		      Local variable table:
		        [pc: 0, pc: 33] local: args index: 0 type: java.lang.String[]
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=258950
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				import java.util.Iterator;
				public class X {
					public static void main(String[] args) {
						List l = new ArrayList();
						List l2 = new ArrayList();
						l.add(new X());
						for (Iterator iterator = l.iterator(); iterator.hasNext() ;) {
							l2.add(((X) iterator.next()).toString()
								.substring(3));
						}
						for (Iterator iterator = l2.iterator(); iterator.hasNext() ;) {
							System.out.println(iterator.next());
						}
					}\
					public String toString() {
						return "NO_SUCCESS";
					}
				}""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 4
		  public static void main(java.lang.String[] args);
		      0  new java.util.ArrayList [16]
		      3  dup
		      4  invokespecial java.util.ArrayList() [18]
		      7  astore_1 [l]
		      8  new java.util.ArrayList [16]
		     11  dup
		     12  invokespecial java.util.ArrayList() [18]
		     15  astore_2 [l2]
		     16  aload_1 [l]
		     17  new X [1]
		     20  dup
		     21  invokespecial X() [19]
		     24  invokeinterface java.util.List.add(java.lang.Object) : boolean [20] [nargs: 2]
		     29  pop
		     30  aload_1 [l]
		     31  invokeinterface java.util.List.iterator() : java.util.Iterator [26] [nargs: 1]
		     36  astore_3 [iterator]
		     37  goto 63
		     40  aload_2 [l2]
		     41  aload_3 [iterator]
		     42  invokeinterface java.util.Iterator.next() : java.lang.Object [30] [nargs: 1]
		     47  checkcast X [1]
		     50  invokevirtual X.toString() : java.lang.String [36]
		     53  iconst_3
		     54  invokevirtual java.lang.String.substring(int) : java.lang.String [40]
		     57  invokeinterface java.util.List.add(java.lang.Object) : boolean [20] [nargs: 2]
		     62  pop
		     63  aload_3 [iterator]
		     64  invokeinterface java.util.Iterator.hasNext() : boolean [46] [nargs: 1]
		     69  ifne 40
		     72  aload_2 [l2]
		     73  invokeinterface java.util.List.iterator() : java.util.Iterator [26] [nargs: 1]
		     78  astore_3 [iterator]
		     79  goto 94
		     82  getstatic java.lang.System.out : java.io.PrintStream [50]
		     85  aload_3 [iterator]
		     86  invokeinterface java.util.Iterator.next() : java.lang.Object [30] [nargs: 1]
		     91  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [56]
		     94  aload_3 [iterator]
		     95  invokeinterface java.util.Iterator.hasNext() : boolean [46] [nargs: 1]
		    100  ifne 82
		    103  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 8, line: 7]
		        [pc: 16, line: 8]
		        [pc: 30, line: 9]
		        [pc: 40, line: 10]
		        [pc: 53, line: 11]
		        [pc: 57, line: 10]
		        [pc: 63, line: 9]
		        [pc: 72, line: 13]
		        [pc: 82, line: 14]
		        [pc: 94, line: 13]
		        [pc: 103, line: 16]
		""";

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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262717
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X{
					public class Inner {
						public void foo() {
							int i = 0;
							final int NEW = 1;
							if (i == NEW) {
								System.out.println();
							}
							bar();
						}
					}
					public void bar() {
						System.out.println("SUCCESS");
					}
					public static void main(String[] args) {
						new X().new Inner().foo();
					}
				}""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		    22  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 2, line: 5]
		        [pc: 4, line: 6]
		        [pc: 9, line: 7]
		        [pc: 15, line: 9]
		        [pc: 22, line: 10]
		      Local variable table:
		        [pc: 0, pc: 23] local: this index: 0 type: X.Inner
		        [pc: 2, pc: 23] local: i index: 1 type: int
		        [pc: 4, pc: 23] local: NEW index: 2 type: int
		""";

	File f = new File(OUTPUT_DIR + File.separator + "X$Inner.class");
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
}
}
