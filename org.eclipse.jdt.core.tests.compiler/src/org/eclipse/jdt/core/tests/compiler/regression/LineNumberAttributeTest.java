/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
public class LineNumberAttributeTest extends AbstractRegressionTest {

public LineNumberAttributeTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173800
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X next;
				
					X(X next) {
						this.next = next;
					}
				
					public static void main(String args[]) {
						try {
							X x = new X(new X(new X(null)));
							x.
								next.
									next.
										next.
											next.
												next.
													toString();
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
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
		  // Method descriptor #19 ([Ljava/lang/String;)V
		  // Stack: 7, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  new X [1]
		     3  dup
		     4  new X [1]
		     7  dup
		     8  new X [1]
		    11  dup
		    12  aconst_null
		    13  invokespecial X(X) [20]
		    16  invokespecial X(X) [20]
		    19  invokespecial X(X) [20]
		    22  astore_1 [x]
		    23  aload_1 [x]
		    24  getfield X.next : X [13]
		    27  getfield X.next : X [13]
		    30  getfield X.next : X [13]
		    33  getfield X.next : X [13]
		    36  getfield X.next : X [13]
		    39  invokevirtual java.lang.Object.toString() : java.lang.String [22]
		    42  pop
		    43  goto 55
		    46  astore_1 [e]
		    47  getstatic java.lang.System.out : java.io.PrintStream [26]
		    50  ldc <String "SUCCESS"> [32]
		    52  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
		    55  return
		      Exception Table:
		        [pc: 0, pc: 43] -> 46 when : java.lang.NullPointerException
		      Line numbers:
		        [pc: 0, line: 10]
		        [pc: 23, line: 11]
		        [pc: 24, line: 12]
		        [pc: 27, line: 13]
		        [pc: 30, line: 14]
		        [pc: 33, line: 15]
		        [pc: 36, line: 16]
		        [pc: 39, line: 17]
		        [pc: 43, line: 18]
		        [pc: 47, line: 19]
		        [pc: 55, line: 21]
		      Local variable table:
		        [pc: 0, pc: 56] local: args index: 0 type: java.lang.String[]
		        [pc: 23, pc: 43] local: x index: 1 type: X
		        [pc: 47, pc: 55] local: e index: 1 type: java.lang.NullPointerException
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173800
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X x;
				
					X next;
				
					X(X next) {
						this.next = next;
					}
				
					public static void main(String args[]) {
						X x = new X(new X(new X(null)));
						x.x = x;
						x.foo();
					}
				
					public void foo() {
						try {
							this.
								x.
									next.
										next.
											next.
												next.
													next.
														toString();
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
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
		  // Method descriptor #13 ()V
		  // Stack: 2, Locals: 2
		  public void foo();
		     0  aload_0 [this]
		     1  getfield X.x : X [23]
		     4  getfield X.next : X [14]
		     7  getfield X.next : X [14]
		    10  getfield X.next : X [14]
		    13  getfield X.next : X [14]
		    16  getfield X.next : X [14]
		    19  invokevirtual java.lang.Object.toString() : java.lang.String [30]
		    22  pop
		    23  goto 35
		    26  astore_1 [e]
		    27  getstatic java.lang.System.out : java.io.PrintStream [34]
		    30  ldc <String "SUCCESS"> [40]
		    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [42]
		    35  return
		      Exception Table:
		        [pc: 0, pc: 23] -> 26 when : java.lang.NullPointerException
		      Line numbers:
		        [pc: 0, line: 18]
		        [pc: 1, line: 19]
		        [pc: 4, line: 20]
		        [pc: 7, line: 21]
		        [pc: 10, line: 22]
		        [pc: 13, line: 23]
		        [pc: 16, line: 24]
		        [pc: 19, line: 25]
		        [pc: 23, line: 26]
		        [pc: 27, line: 27]
		        [pc: 35, line: 29]
		      Local variable table:
		        [pc: 0, pc: 36] local: this index: 0 type: X
		        [pc: 27, pc: 35] local: e index: 1 type: java.lang.NullPointerException
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=509027
public void testBug509027() throws Exception {
	runConformTest(
		new String[] {
			"linenumber/Test.java",
			"package linenumber;\n" +
			"\n" +
			"public class Test {\n" +
			"	int[] f = { 1, // linebreak\n" +
			"			2 };\n" +
			"}\n" +
			""
		}
	);

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator + "linenumber"+ File.separator  +"Test.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #8 ()V
		  // Stack: 5, Locals: 1
		  public Test();
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [10]
		     4  aload_0 [this]
		     5  iconst_2
		     6  newarray int [10]
		     8  dup
		     9  iconst_0
		    10  iconst_1
		    11  iastore
		    12  dup
		    13  iconst_1
		    14  iconst_2
		    15  iastore
		    16  putfield linenumber.Test.f : int[] [12]
		    19  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		        [pc: 14, line: 5]
		        [pc: 19, line: 3]
		      Local variable table:
		        [pc: 0, pc: 20] local: this index: 0 type: linenumber.Test
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=520714
public void testBug520714() throws Exception {
	runConformTest(
		new String[] {
			"TestAnon.java",
			"""
				public class TestAnon {
					void f1() {
						new Object() {
						};
					}
				}"""
		}
	);

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"TestAnon$1.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"      Local variable table:\n" +
			"";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
public static Class testClass() {
	return LineNumberAttributeTest.class;
}
}
