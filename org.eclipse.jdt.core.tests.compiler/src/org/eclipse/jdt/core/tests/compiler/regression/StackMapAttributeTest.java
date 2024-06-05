/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 388800 - [1.8] adjust tests to 1.8 JRE
 *								Bug 412203 - [compiler] Internal compiler error: java.lang.IllegalArgumentException: info cannot be null
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "unchecked", "rawtypes" })
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
//		TESTS_NAMES = new String[] { "testBug412076_" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_6);
	}
	public void test001() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							X() {}
							X(double i) {
								this(i > 0 ? null : new Object());
								try {
									foo(6, false);
								} catch(Exception e) {
									e.printStackTrace();
								}
							}
							X(Object o) {}
							int foo(int i, boolean b) {
								try {
									if (b) {
										return i;
									}
									return i + 1;
								} catch(Exception e) {
									return 5;
								}
							}
							public static void main(String[] args) {
								new X().foo(2, false);
								System.out.println("SUCCESS");
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
				  // Method descriptor #14 (D)V
				  // Stack: 5, Locals: 4
				  X(double i);
				     0  aload_0 [this]
				     1  dload_1 [i]
				     2  dconst_0
				     3  dcmpl
				     4  ifle 11
				     7  aconst_null
				     8  goto 18
				    11  new java.lang.Object [3]
				    14  dup
				    15  invokespecial java.lang.Object() [8]
				    18  invokespecial X(java.lang.Object) [15]
				    21  aload_0 [this]
				    22  bipush 6
				    24  iconst_0
				    25  invokevirtual X.foo(int, boolean) : int [18]
				    28  pop
				    29  goto 37
				    32  astore_3 [e]
				    33  aload_3 [e]
				    34  invokevirtual java.lang.Exception.printStackTrace() : void [22]
				    37  return
				      Exception Table:
				        [pc: 21, pc: 29] -> 32 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 21, line: 6]
				        [pc: 29, line: 7]
				        [pc: 33, line: 8]
				        [pc: 37, line: 10]
				      Local variable table:
				        [pc: 0, pc: 38] local: this index: 0 type: X
				        [pc: 0, pc: 38] local: i index: 1 type: double
				        [pc: 33, pc: 37] local: e index: 3 type: java.lang.Exception
				      Stack map table: number of frames 4
				        [pc: 11, same_locals_1_stack_item, stack: {uninitialized_this}]
				        [pc: 18, full, stack: {uninitialized_this, java.lang.Object}, locals: {uninitialized_this, double}]
				        [pc: 32, full, stack: {java.lang.Exception}, locals: {X, double}]
				        [pc: 37, same]
				 \s
				  // Method descriptor #17 (Ljava/lang/Object;)V
				  // Stack: 1, Locals: 2
				  X(java.lang.Object o);
				    0  aload_0 [this]
				    1  invokespecial java.lang.Object() [8]
				    4  return
				      Line numbers:
				        [pc: 0, line: 11]
				      Local variable table:
				        [pc: 0, pc: 5] local: this index: 0 type: X
				        [pc: 0, pc: 5] local: o index: 1 type: java.lang.Object
				 \s
				  // Method descriptor #21 (IZ)I
				  // Stack: 2, Locals: 4
				  int foo(int i, boolean b);
				     0  iload_2 [b]
				     1  ifeq 6
				     4  iload_1 [i]
				     5  ireturn
				     6  iload_1 [i]
				     7  iconst_1
				     8  iadd
				     9  ireturn
				    10  astore_3 [e]
				    11  iconst_5
				    12  ireturn
				      Exception Table:
				        [pc: 0, pc: 5] -> 10 when : java.lang.Exception
				        [pc: 6, pc: 9] -> 10 when : java.lang.Exception
				      Line numbers:
				        [pc: 0, line: 14]
				        [pc: 4, line: 15]
				        [pc: 6, line: 17]
				        [pc: 10, line: 18]
				        [pc: 11, line: 19]
				      Local variable table:
				        [pc: 0, pc: 13] local: this index: 0 type: X
				        [pc: 0, pc: 13] local: i index: 1 type: int
				        [pc: 0, pc: 13] local: b index: 2 type: boolean
				        [pc: 11, pc: 13] local: e index: 3 type: java.lang.Exception
				      Stack map table: number of frames 2
				        [pc: 6, same]
				        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test002() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void foo(double d, boolean b) {
								double i;
								try {
									i = 0;
									i++;
									int j = (int) (i - 1);
									if (b) {
										double d1 = 0;
										if (!b) {
											d1 = 0;
										}
										double d2 = d + d1;
									}
									bar(j);
								} catch(NullPointerException e) {
									i = 2;
								} finally {
									i = 1;
								}
								long j = (long) (i + 1);
								int k = (int) j;
								k += j;
							}
							public static void bar(int i) {}
							public static void main(String[] args) {
								foo(0, true);
								System.out.println("SUCCESS");
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
				  // Method descriptor #15 (DZ)V
				  // Stack: 4, Locals: 11
				  public static void foo(double d, boolean b);
				     0  dconst_0
				     1  dstore_3 [i]
				     2  dload_3 [i]
				     3  dconst_1
				     4  dadd
				     5  dstore_3 [i]
				     6  dload_3 [i]
				     7  dconst_1
				     8  dsub
				     9  d2i
				    10  istore 5 [j]
				    12  iload_2 [b]
				    13  ifeq 32
				    16  dconst_0
				    17  dstore 6 [d1]
				    19  iload_2 [b]
				    20  ifne 26
				    23  dconst_0
				    24  dstore 6 [d1]
				    26  dload_0 [d]
				    27  dload 6 [d1]
				    29  dadd
				    30  dstore 8
				    32  iload 5 [j]
				    34  invokestatic X.bar(int) : void [16]
				    37  goto 58
				    40  astore 5 [e]
				    42  ldc2_w <Double 2.0> [20]
				    45  dstore_3 [i]
				    46  dconst_1
				    47  dstore_3 [i]
				    48  goto 60
				    51  astore 10
				    53  dconst_1
				    54  dstore_3 [i]
				    55  aload 10
				    57  athrow
				    58  dconst_1
				    59  dstore_3 [i]
				    60  dload_3 [i]
				    61  dconst_1
				    62  dadd
				    63  d2l
				    64  lstore 5 [j]
				    66  lload 5 [j]
				    68  l2i
				    69  istore 7 [k]
				    71  iload 7 [k]
				    73  i2l
				    74  lload 5 [j]
				    76  ladd
				    77  l2i
				    78  istore 7 [k]
				    80  return
				      Exception Table:
				        [pc: 0, pc: 37] -> 40 when : java.lang.NullPointerException
				        [pc: 0, pc: 46] -> 51 when : any
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 2, line: 6]
				        [pc: 6, line: 7]
				        [pc: 12, line: 8]
				        [pc: 16, line: 9]
				        [pc: 19, line: 10]
				        [pc: 23, line: 11]
				        [pc: 26, line: 13]
				        [pc: 32, line: 15]
				        [pc: 37, line: 16]
				        [pc: 42, line: 17]
				        [pc: 46, line: 19]
				        [pc: 51, line: 18]
				        [pc: 53, line: 19]
				        [pc: 55, line: 20]
				        [pc: 58, line: 19]
				        [pc: 60, line: 21]
				        [pc: 66, line: 22]
				        [pc: 71, line: 23]
				        [pc: 80, line: 24]
				      Local variable table:
				        [pc: 0, pc: 81] local: d index: 0 type: double
				        [pc: 0, pc: 81] local: b index: 2 type: boolean
				        [pc: 2, pc: 40] local: i index: 3 type: double
				        [pc: 46, pc: 51] local: i index: 3 type: double
				        [pc: 55, pc: 81] local: i index: 3 type: double
				        [pc: 12, pc: 37] local: j index: 5 type: int
				        [pc: 19, pc: 32] local: d1 index: 6 type: double
				        [pc: 42, pc: 46] local: e index: 5 type: java.lang.NullPointerException
				        [pc: 66, pc: 81] local: j index: 5 type: long
				        [pc: 71, pc: 81] local: k index: 7 type: int
				      Stack map table: number of frames 6
				        [pc: 26, append: {double, int, double}]
				        [pc: 32, chop 1 local(s)]
				        [pc: 40, full, stack: {java.lang.NullPointerException}, locals: {double, int}]
				        [pc: 51, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				        [pc: 58, append: {double}]
				        [pc: 60, same]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test003() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void foo(boolean b) {
								int i = 0;
								try {
									System.out.println("FINALLY");
									i++;
									int j = i -1;
									bar(j);
								} catch(NullPointerException e) {
									e.printStackTrace();
								} finally {
									System.out.println("FINALLY");
								}
							}
							public static void bar(int i) {}
						\t
							public static void main(String[] args) {
								foo(true);
								System.out.println("SUCCESS");
							}
						}""",
				},
				"""
					FINALLY
					FINALLY
					SUCCESS""");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"""
				  // Method descriptor #15 (Z)V
				  // Stack: 2, Locals: 4
				  public static void foo(boolean b);
				     0  iconst_0
				     1  istore_1 [i]
				     2  getstatic java.lang.System.out : java.io.PrintStream [16]
				     5  ldc <String "FINALLY"> [22]
				     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    10  iinc 1 1 [i]
				    13  iload_1 [i]
				    14  iconst_1
				    15  isub
				    16  istore_2 [j]
				    17  iload_2 [j]
				    18  invokestatic X.bar(int) : void [30]
				    21  goto 51
				    24  astore_2 [e]
				    25  aload_2 [e]
				    26  invokevirtual java.lang.NullPointerException.printStackTrace() : void [34]
				    29  getstatic java.lang.System.out : java.io.PrintStream [16]
				    32  ldc <String "FINALLY"> [22]
				    34  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    37  goto 59
				    40  astore_3
				    41  getstatic java.lang.System.out : java.io.PrintStream [16]
				    44  ldc <String "FINALLY"> [22]
				    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    49  aload_3
				    50  athrow
				    51  getstatic java.lang.System.out : java.io.PrintStream [16]
				    54  ldc <String "FINALLY"> [22]
				    56  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    59  return
				      Exception Table:
				        [pc: 2, pc: 21] -> 24 when : java.lang.NullPointerException
				        [pc: 2, pc: 29] -> 40 when : any
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 2, line: 5]
				        [pc: 10, line: 6]
				        [pc: 13, line: 7]
				        [pc: 17, line: 8]
				        [pc: 21, line: 9]
				        [pc: 25, line: 10]
				        [pc: 29, line: 12]
				        [pc: 40, line: 11]
				        [pc: 41, line: 12]
				        [pc: 49, line: 13]
				        [pc: 51, line: 12]
				        [pc: 59, line: 14]
				      Local variable table:
				        [pc: 0, pc: 60] local: b index: 0 type: boolean
				        [pc: 2, pc: 60] local: i index: 1 type: int
				        [pc: 17, pc: 21] local: j index: 2 type: int
				        [pc: 25, pc: 29] local: e index: 2 type: java.lang.NullPointerException
				      Stack map table: number of frames 4
				        [pc: 24, full, stack: {java.lang.NullPointerException}, locals: {int, int}]
				        [pc: 40, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				        [pc: 51, same]
				        [pc: 59, same]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test004() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void foo(boolean b) {
								C c;
								if (b) {
									c = new C1();
								} else {
									c = new C2();
								}
								System.out.println();
							}
							public static void main(String[] args) {
								foo(true);
								System.out.println("SUCCESS");
							}
						}
						class C {
							void foo() {}
						}
						class C1 extends C {
						}
						class C2 extends C {
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
				  // Method descriptor #15 (Z)V
				  // Stack: 2, Locals: 2
				  public static void foo(boolean b);
				     0  iload_0 [b]
				     1  ifeq 15
				     4  new C1 [16]
				     7  dup
				     8  invokespecial C1() [18]
				    11  astore_1 [c]
				    12  goto 23
				    15  new C2 [19]
				    18  dup
				    19  invokespecial C2() [21]
				    22  astore_1 [c]
				    23  getstatic java.lang.System.out : java.io.PrintStream [22]
				    26  invokevirtual java.io.PrintStream.println() : void [28]
				    29  return
				      Line numbers:
				        [pc: 0, line: 4]
				        [pc: 4, line: 5]
				        [pc: 12, line: 6]
				        [pc: 15, line: 7]
				        [pc: 23, line: 9]
				        [pc: 29, line: 10]
				      Local variable table:
				        [pc: 0, pc: 30] local: b index: 0 type: boolean
				        [pc: 12, pc: 15] local: c index: 1 type: C
				        [pc: 23, pc: 30] local: c index: 1 type: C
				      Stack map table: number of frames 2
				        [pc: 15, same]
				        [pc: 23, append: {C}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test005() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  public static void main(String args[]) {
						    int i = 0, j, k, l;
						    boolean b;
						    if (i == 0 && (j = ++i) > 0)
						      i += j;
						    while (true) {
						      k = 3;
						      break;
						    }\s
						    i -= k;
						    b = false && (i = l) > 0;
						    if (i > 0)
						      l = i;
						    else
						      l = k;
						    j = l;
						    if (i != -1 || j != 3 || k != 3 || l != 3)
						      System.out.println("FAILED");
						    System.out.println("SUCCESS");
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
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 6
				  public static void main(java.lang.String[] args);
				     0  iconst_0
				     1  istore_1 [i]
				     2  iload_1 [i]
				     3  ifne 19
				     6  iinc 1 1 [i]
				     9  iload_1 [i]
				    10  dup
				    11  istore_2 [j]
				    12  ifle 19
				    15  iload_1 [i]
				    16  iload_2 [j]
				    17  iadd
				    18  istore_1 [i]
				    19  iconst_3
				    20  istore_3 [k]
				    21  iload_1 [i]
				    22  iload_3 [k]
				    23  isub
				    24  istore_1 [i]
				    25  iconst_0
				    26  istore 5 [b]
				    28  iload_1 [i]
				    29  ifle 38
				    32  iload_1 [i]
				    33  istore 4 [l]
				    35  goto 41
				    38  iload_3 [k]
				    39  istore 4 [l]
				    41  iload 4 [l]
				    43  istore_2 [j]
				    44  iload_1 [i]
				    45  iconst_m1
				    46  if_icmpne 65
				    49  iload_2 [j]
				    50  iconst_3
				    51  if_icmpne 65
				    54  iload_3 [k]
				    55  iconst_3
				    56  if_icmpne 65
				    59  iload 4 [l]
				    61  iconst_3
				    62  if_icmpeq 73
				    65  getstatic java.lang.System.out : java.io.PrintStream [16]
				    68  ldc <String "FAILED"> [22]
				    70  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    73  getstatic java.lang.System.out : java.io.PrintStream [16]
				    76  ldc <String "SUCCESS"> [30]
				    78  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
				    81  return
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 2, line: 5]
				        [pc: 15, line: 6]
				        [pc: 19, line: 8]
				        [pc: 21, line: 9]
				        [pc: 22, line: 11]
				        [pc: 25, line: 12]
				        [pc: 28, line: 13]
				        [pc: 32, line: 14]
				        [pc: 38, line: 16]
				        [pc: 41, line: 17]
				        [pc: 44, line: 18]
				        [pc: 65, line: 19]
				        [pc: 73, line: 20]
				        [pc: 81, line: 21]
				      Local variable table:
				        [pc: 0, pc: 82] local: args index: 0 type: java.lang.String[]
				        [pc: 2, pc: 82] local: i index: 1 type: int
				        [pc: 12, pc: 19] local: j index: 2 type: int
				        [pc: 44, pc: 82] local: j index: 2 type: int
				        [pc: 21, pc: 82] local: k index: 3 type: int
				        [pc: 35, pc: 38] local: l index: 4 type: int
				        [pc: 41, pc: 82] local: l index: 4 type: int
				        [pc: 28, pc: 82] local: b index: 5 type: boolean
				      Stack map table: number of frames 5
				        [pc: 19, append: {int}]
				        [pc: 38, full, stack: {}, locals: {java.lang.String[], int, _, int, _, int}]
				        [pc: 41, full, stack: {}, locals: {java.lang.String[], int, _, int, int, int}]
				        [pc: 65, full, stack: {}, locals: {java.lang.String[], int, int, int, int, int}]
				        [pc: 73, same]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test006() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							void foo (int n) {
						   	synchronized (this) {
						       	switch (n) {
						       		case 1:
						       		throw new NullPointerException();
									}
								}
							}
						    public static void main(String args[]) {
						    	try {
							    	new X().foo(1);
						    	} catch(Exception e) {
							        System.out.println("SUCCESS");\s
						    	}
						    }\s
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
				  // Method descriptor #15 (I)V
				  // Stack: 2, Locals: 3
				  void foo(int n);
				     0  aload_0 [this]
				     1  dup
				     2  astore_2
				     3  monitorenter
				     4  iload_1 [n]
				     5  tableswitch default: 32
				          case 1: 24
				    24  new java.lang.NullPointerException [16]
				    27  dup
				    28  invokespecial java.lang.NullPointerException() [18]
				    31  athrow
				    32  aload_2
				    33  monitorexit
				    34  goto 40
				    37  aload_2
				    38  monitorexit
				    39  athrow
				    40  return
				      Exception Table:
				        [pc: 4, pc: 34] -> 37 when : any
				        [pc: 37, pc: 39] -> 37 when : any
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 4, line: 4]
				        [pc: 24, line: 6]
				        [pc: 32, line: 3]
				        [pc: 40, line: 9]
				      Local variable table:
				        [pc: 0, pc: 41] local: this index: 0 type: X
				        [pc: 0, pc: 41] local: n index: 1 type: int
				      Stack map table: number of frames 4
				        [pc: 24, append: {X}]
				        [pc: 32, same]
				        [pc: 37, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				        [pc: 40, chop 1 local(s)]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test007() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							StringBuffer foo2(boolean b) {
								System.out.println("foo2");
								return new StringBuffer(b ? "true" : "false");
							}
							public static void main(String[] args) {
								System.out.println("SUCCESS");
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
				  // Method descriptor #15 (Z)Ljava/lang/StringBuffer;
				  // Stack: 3, Locals: 2
				  java.lang.StringBuffer foo2(boolean b);
				     0  getstatic java.lang.System.out : java.io.PrintStream [16]
				     3  ldc <String "foo2"> [22]
				     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [23]
				     8  new java.lang.StringBuffer [29]
				    11  dup
				    12  iload_1 [b]
				    13  ifeq 21
				    16  ldc <String "true"> [31]
				    18  goto 23
				    21  ldc <String "false"> [33]
				    23  invokespecial java.lang.StringBuffer(java.lang.String) [35]
				    26  areturn
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 8, line: 4]
				      Local variable table:
				        [pc: 0, pc: 27] local: this index: 0 type: X
				        [pc: 0, pc: 27] local: b index: 1 type: boolean
				      Stack map table: number of frames 2
				        [pc: 21, full, stack: {uninitialized(8), uninitialized(8)}, locals: {X, int}]
				        [pc: 23, full, stack: {uninitialized(8), uninitialized(8), java.lang.String}, locals: {X, int}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141252
	public void test008() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								int foo = 0;
								String bar = "zero";
								System.out.println((foo != 0) ? foo : bar);
							}
							<T extends Comparable<?>> void foo(T foo) {
								T bar = null;
								System.out.println((foo != null) ? foo : bar);
							}\t
						}
						""",
				},
				"zero");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 3
				  public static void main(java.lang.String[] args);
				     0  iconst_0
				     1  istore_1 [foo]
				     2  ldc <String "zero"> [16]
				     4  astore_2 [bar]
				     5  getstatic java.lang.System.out : java.io.PrintStream [18]
				     8  iload_1 [foo]
				     9  ifeq 19
				    12  iload_1 [foo]
				    13  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [24]
				    16  goto 20
				    19  aload_2 [bar]
				    20  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [30]
				    23  return
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 2, line: 4]
				        [pc: 5, line: 5]
				        [pc: 23, line: 6]
				      Local variable table:
				        [pc: 0, pc: 24] local: args index: 0 type: java.lang.String[]
				        [pc: 2, pc: 24] local: foo index: 1 type: int
				        [pc: 5, pc: 24] local: bar index: 2 type: java.lang.String
				      Stack map table: number of frames 2
				        [pc: 19, full, stack: {java.io.PrintStream}, locals: {java.lang.String[], int, java.lang.String}]
				        [pc: 20, full, stack: {java.io.PrintStream, java.lang.Comparable}, locals: {java.lang.String[], int, java.lang.String}]
				 \s
				  // Method descriptor #48 (Ljava/lang/Comparable;)V
				  // Signature: <T::Ljava/lang/Comparable<*>;>(TT;)V
				  // Stack: 2, Locals: 3
				  void foo(java.lang.Comparable foo);
				     0  aconst_null
				     1  astore_2 [bar]
				     2  getstatic java.lang.System.out : java.io.PrintStream [18]
				     5  aload_1 [foo]
				     6  ifnull 13
				     9  aload_1 [foo]
				    10  goto 14
				    13  aload_2 [bar]
				    14  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [30]
				    17  return
				      Line numbers:
				        [pc: 0, line: 8]
				        [pc: 2, line: 9]
				        [pc: 17, line: 10]
				      Local variable table:
				        [pc: 0, pc: 18] local: this index: 0 type: X
				        [pc: 0, pc: 18] local: foo index: 1 type: java.lang.Comparable
				        [pc: 2, pc: 18] local: bar index: 2 type: java.lang.Comparable
				      Local variable type table:
				        [pc: 0, pc: 18] local: foo index: 1 type: T
				        [pc: 2, pc: 18] local: bar index: 2 type: T
				      Stack map table: number of frames 2
				        [pc: 13, full, stack: {java.io.PrintStream}, locals: {X, java.lang.Comparable, java.lang.Comparable}]
				        [pc: 14, full, stack: {java.io.PrintStream, java.lang.Comparable}, locals: {X, java.lang.Comparable, java.lang.Comparable}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test009() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							String s;
							X() {
						        int i = 0;
						        if (s == null) {
						        	System.out.print("PASSED");
						        } else {
						        	System.out.print("FAILED");
						        }
						        System.out.print("DONE" + i);
							}
						    public static void main(String argv[]) {
						    	new X();
						    }
						}""",
				},
				"PASSEDDONE0");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					this.complianceLevel < ClassFileConstants.JDK9 ?
				"""
					  // Method descriptor #8 ()V
					  // Stack: 4, Locals: 2
					  X();
					     0  aload_0 [this]
					     1  invokespecial java.lang.Object() [10]
					     4  iconst_0
					     5  istore_1 [i]
					     6  aload_0 [this]
					     7  getfield X.s : java.lang.String [12]
					    10  ifnonnull 24
					    13  getstatic java.lang.System.out : java.io.PrintStream [14]
					    16  ldc <String "PASSED"> [20]
					    18  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]
					    21  goto 32
					    24  getstatic java.lang.System.out : java.io.PrintStream [14]
					    27  ldc <String "FAILED"> [28]
					    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]
					    32  getstatic java.lang.System.out : java.io.PrintStream [14]
					    35  new java.lang.StringBuilder [30]
					    38  dup
					    39  ldc <String "DONE"> [32]
					    41  invokespecial java.lang.StringBuilder(java.lang.String) [34]
					    44  iload_1 [i]
					    45  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [36]
					    48  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [40]
					    51  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]
					    54  return
					      Line numbers:
					        [pc: 0, line: 3]
					        [pc: 4, line: 4]
					        [pc: 6, line: 5]
					        [pc: 13, line: 6]
					        [pc: 21, line: 7]
					        [pc: 24, line: 8]
					        [pc: 32, line: 10]
					        [pc: 54, line: 11]
					      Local variable table:
					        [pc: 0, pc: 55] local: this index: 0 type: X
					        [pc: 6, pc: 55] local: i index: 1 type: int
					      Stack map table: number of frames 2
					        [pc: 24, full, stack: {}, locals: {X, int}]
					        [pc: 32, same]
					"""
				:
					"""
						  X();
						     0  aload_0 [this]
						     1  invokespecial java.lang.Object() [10]
						     4  iconst_0
						     5  istore_1 [i]
						     6  aload_0 [this]
						     7  getfield X.s : java.lang.String [12]
						    10  ifnonnull 24
						    13  getstatic java.lang.System.out : java.io.PrintStream [14]
						    16  ldc <String "PASSED"> [20]
						    18  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]
						    21  goto 32
						    24  getstatic java.lang.System.out : java.io.PrintStream [14]
						    27  ldc <String "FAILED"> [28]
						    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]
						    32  getstatic java.lang.System.out : java.io.PrintStream [14]
						    35  iload_1 [i]
						    36  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [30]
						    41  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]
						    44  return
						      Line numbers:
						        [pc: 0, line: 3]
						        [pc: 4, line: 4]
						        [pc: 6, line: 5]
						        [pc: 13, line: 6]
						        [pc: 21, line: 7]
						        [pc: 24, line: 8]
						        [pc: 32, line: 10]
						        [pc: 44, line: 11]
						      Local variable table:
						        [pc: 0, pc: 45] local: this index: 0 type: X
						        [pc: 6, pc: 45] local: i index: 1 type: int
						      Stack map table: number of frames 2
						        [pc: 24, full, stack: {}, locals: {X, int}]
						        [pc: 32, same]
						""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test010() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								int[] tab = new int[0];
								Object o = tab;
								Object o1 = null;
								if (tab.length == 0) {
									System.out.println(tab.getClass());
								}
								o1 = tab.clone();
							}
						}""",
				},
				"class [I");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 4
				  public static void main(java.lang.String[] args);
				     0  iconst_0
				     1  newarray int [10]
				     3  astore_1 [tab]
				     4  aload_1 [tab]
				     5  astore_2 [o]
				     6  aconst_null
				     7  astore_3 [o1]
				     8  aload_1 [tab]
				     9  arraylength
				    10  ifne 23
				    13  getstatic java.lang.System.out : java.io.PrintStream [16]
				    16  aload_1 [tab]
				    17  invokevirtual java.lang.Object.getClass() : java.lang.Class [22]
				    20  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [26]
				    23  aload_1 [tab]
				    24  invokevirtual int[].clone() : java.lang.Object [32]
				    27  astore_3 [o1]
				    28  return
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 4, line: 4]
				        [pc: 6, line: 5]
				        [pc: 8, line: 6]
				        [pc: 13, line: 7]
				        [pc: 23, line: 9]
				        [pc: 28, line: 10]
				      Local variable table:
				        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]
				        [pc: 4, pc: 29] local: tab index: 1 type: int[]
				        [pc: 6, pc: 29] local: o index: 2 type: java.lang.Object
				        [pc: 8, pc: 29] local: o1 index: 3 type: java.lang.Object
				      Stack map table: number of frames 1
				        [pc: 23, append: {int[], java.lang.Object, java.lang.Object}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test011() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Object o;
						
							public static void main(String[] args) {
								X x;
								for (int i = 0; i < 10; i++) {
									if (i < 90) {
										x = new X();
										if (i > 4) {
											x.o = new Object();
										} else {
											x.o = "0";
										}
										switch (i) {
											case 0:
												if (x.o instanceof String) {
													System.out.print("1");
												}
												break;
											default: {
												Object diff = x.o;
												if (diff != null) {
													System.out.print("2");
												}
											}
										}
									}
								}
							}
						}""",
				},
				"1222222222");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"""
				  // Method descriptor #17 ([Ljava/lang/String;)V
				  // Stack: 3, Locals: 4
				  public static void main(java.lang.String[] args);
				      0  iconst_0
				      1  istore_2 [i]
				      2  goto 105
				      5  iload_2 [i]
				      6  bipush 90
				      8  if_icmpge 102
				     11  new X [1]
				     14  dup
				     15  invokespecial X() [18]
				     18  astore_1 [x]
				     19  iload_2 [i]
				     20  iconst_4
				     21  if_icmple 38
				     24  aload_1 [x]
				     25  new java.lang.Object [3]
				     28  dup
				     29  invokespecial java.lang.Object() [10]
				     32  putfield X.o : java.lang.Object [19]
				     35  goto 44
				     38  aload_1 [x]
				     39  ldc <String "0"> [21]
				     41  putfield X.o : java.lang.Object [19]
				     44  iload_2 [i]
				     45  tableswitch default: 85
				          case 0: 64
				     64  aload_1 [x]
				     65  getfield X.o : java.lang.Object [19]
				     68  instanceof java.lang.String [23]
				     71  ifeq 102
				     74  getstatic java.lang.System.out : java.io.PrintStream [25]
				     77  ldc <String "1"> [31]
				     79  invokevirtual java.io.PrintStream.print(java.lang.String) : void [33]
				     82  goto 102
				     85  aload_1 [x]
				     86  getfield X.o : java.lang.Object [19]
				     89  astore_3 [diff]
				     90  aload_3 [diff]
				     91  ifnull 102
				     94  getstatic java.lang.System.out : java.io.PrintStream [25]
				     97  ldc <String "2"> [39]
				     99  invokevirtual java.io.PrintStream.print(java.lang.String) : void [33]
				    102  iinc 2 1 [i]
				    105  iload_2 [i]
				    106  bipush 10
				    108  if_icmplt 5
				    111  return
				      Line numbers:
				        [pc: 0, line: 6]
				        [pc: 5, line: 7]
				        [pc: 11, line: 8]
				        [pc: 19, line: 9]
				        [pc: 24, line: 10]
				        [pc: 35, line: 11]
				        [pc: 38, line: 12]
				        [pc: 44, line: 14]
				        [pc: 64, line: 16]
				        [pc: 74, line: 17]
				        [pc: 82, line: 19]
				        [pc: 85, line: 21]
				        [pc: 90, line: 22]
				        [pc: 94, line: 23]
				        [pc: 102, line: 6]
				        [pc: 111, line: 29]
				      Local variable table:
				        [pc: 0, pc: 112] local: args index: 0 type: java.lang.String[]
				        [pc: 19, pc: 102] local: x index: 1 type: X
				        [pc: 2, pc: 111] local: i index: 2 type: int
				        [pc: 90, pc: 102] local: diff index: 3 type: java.lang.Object
				      Stack map table: number of frames 7
				        [pc: 5, full, stack: {}, locals: {java.lang.String[], _, int}]
				        [pc: 38, full, stack: {}, locals: {java.lang.String[], X, int}]
				        [pc: 44, same]
				        [pc: 64, same]
				        [pc: 85, same]
				        [pc: 102, full, stack: {}, locals: {java.lang.String[], _, int}]
				        [pc: 105, same]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test012() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								X x;
								Object o;
								for (int i = 0; i < 10; i++) {
									if (i < 90) {
										x = new X();
										if (i > 4) {
											o = new Object();
										} else {
											o = null;
										}
										switch (i) {
											case 0:
												if (o instanceof String) {
													System.out.print("1");
													return;
												} else {
													break;
												}
											default: {
												Object diff = o;
												if (diff != null) {
													System.out.print("2");
												}
												break;
											}
										}
										System.out.print("3");
									}
								}
							}
						}""",
				},
				"333332323232323");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 5
				  public static void main(java.lang.String[] args);
				      0  iconst_0
				      1  istore_3 [i]
				      2  goto 99
				      5  iload_3 [i]
				      6  bipush 90
				      8  if_icmpge 96
				     11  new X [1]
				     14  dup
				     15  invokespecial X() [16]
				     18  astore_1 [x]
				     19  iload_3 [i]
				     20  iconst_4
				     21  if_icmple 35
				     24  new java.lang.Object [3]
				     27  dup
				     28  invokespecial java.lang.Object() [8]
				     31  astore_2 [o]
				     32  goto 37
				     35  aconst_null
				     36  astore_2 [o]
				     37  iload_3 [i]
				     38  tableswitch default: 72
				          case 0: 56
				     56  aload_2 [o]
				     57  instanceof java.lang.String [17]
				     60  ifeq 88
				     63  getstatic java.lang.System.out : java.io.PrintStream [19]
				     66  ldc <String "1"> [25]
				     68  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
				     71  return
				     72  aload_2 [o]
				     73  astore 4 [diff]
				     75  aload 4 [diff]
				     77  ifnull 88
				     80  getstatic java.lang.System.out : java.io.PrintStream [19]
				     83  ldc <String "2"> [33]
				     85  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
				     88  getstatic java.lang.System.out : java.io.PrintStream [19]
				     91  ldc <String "3"> [35]
				     93  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
				     96  iinc 3 1 [i]
				     99  iload_3 [i]
				    100  bipush 10
				    102  if_icmplt 5
				    105  return
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 5, line: 6]
				        [pc: 11, line: 7]
				        [pc: 19, line: 8]
				        [pc: 24, line: 9]
				        [pc: 32, line: 10]
				        [pc: 35, line: 11]
				        [pc: 37, line: 13]
				        [pc: 56, line: 15]
				        [pc: 63, line: 16]
				        [pc: 71, line: 17]
				        [pc: 72, line: 22]
				        [pc: 75, line: 23]
				        [pc: 80, line: 24]
				        [pc: 88, line: 29]
				        [pc: 96, line: 5]
				        [pc: 105, line: 32]
				      Local variable table:
				        [pc: 0, pc: 106] local: args index: 0 type: java.lang.String[]
				        [pc: 19, pc: 96] local: x index: 1 type: X
				        [pc: 32, pc: 35] local: o index: 2 type: java.lang.Object
				        [pc: 37, pc: 96] local: o index: 2 type: java.lang.Object
				        [pc: 2, pc: 105] local: i index: 3 type: int
				        [pc: 75, pc: 88] local: diff index: 4 type: java.lang.Object
				      Stack map table: number of frames 8
				        [pc: 5, full, stack: {}, locals: {java.lang.String[], _, _, int}]
				        [pc: 35, full, stack: {}, locals: {java.lang.String[], X, _, int}]
				        [pc: 37, full, stack: {}, locals: {java.lang.String[], X, java.lang.Object, int}]
				        [pc: 56, same]
				        [pc: 72, same]
				        [pc: 88, same]
				        [pc: 96, full, stack: {}, locals: {java.lang.String[], _, _, int}]
				        [pc: 99, same]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test013() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"""
						public class X {
						
						  void foo() {
						      synchronized (this) {
						        int n=0;
						        try {
						           Thread.sleep(n);\s
						        } catch (Exception e ) {
						        }
						     }
						  }
						 \s
						  public static void main(String[] args) {}
						}""",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 4
			  void foo();
			     0  aload_0 [this]
			     1  dup
			     2  astore_1
			     3  monitorenter
			     4  iconst_0
			     5  istore_2 [n]
			     6  iload_2 [n]
			     7  i2l
			     8  invokestatic java.lang.Thread.sleep(long) : void [15]
			    11  goto 15
			    14  astore_3
			    15  aload_1
			    16  monitorexit
			    17  goto 23
			    20  aload_1
			    21  monitorexit
			    22  athrow
			    23  return
			      Exception Table:
			        [pc: 6, pc: 11] -> 14 when : java.lang.Exception
			        [pc: 4, pc: 17] -> 20 when : any
			        [pc: 20, pc: 22] -> 20 when : any
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 4, line: 5]
			        [pc: 6, line: 7]
			        [pc: 11, line: 8]
			        [pc: 15, line: 4]
			        [pc: 23, line: 11]
			      Local variable table:
			        [pc: 0, pc: 24] local: this index: 0 type: X
			        [pc: 6, pc: 15] local: n index: 2 type: int
			      Stack map table: number of frames 4
			        [pc: 14, full, stack: {java.lang.Exception}, locals: {X, X, int}]
			        [pc: 15, chop 1 local(s)]
			        [pc: 20, same_locals_1_stack_item, stack: {java.lang.Throwable}]
			        [pc: 23, chop 1 local(s)]
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test014() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"""
						public class X {
						    X() {
								final int i;
								synchronized (this) {
								    i = 8;
								}
						    } \s
						  public static void main(String[] args) {}
						}""",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 3
			  X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [8]
			     4  aload_0 [this]
			     5  dup
			     6  astore_2
			     7  monitorenter
			     8  bipush 8
			    10  istore_1 [i]
			    11  aload_2
			    12  monitorexit
			    13  goto 19
			    16  aload_2
			    17  monitorexit
			    18  athrow
			    19  return
			      Exception Table:
			        [pc: 8, pc: 13] -> 16 when : any
			        [pc: 16, pc: 18] -> 16 when : any
			      Line numbers:
			        [pc: 0, line: 2]
			        [pc: 4, line: 4]
			        [pc: 8, line: 5]
			        [pc: 11, line: 4]
			        [pc: 19, line: 7]
			      Local variable table:
			        [pc: 0, pc: 20] local: this index: 0 type: X
			        [pc: 11, pc: 16] local: i index: 1 type: int
			        [pc: 19, pc: 20] local: i index: 1 type: int
			      Stack map table: number of frames 2
			        [pc: 16, full, stack: {java.lang.Throwable}, locals: {X, _, X}]
			        [pc: 19, full, stack: {}, locals: {X, int}]
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test015() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"""
						public enum X {
						    a1(1), a2(5), a3(11);
						    int value;
						    X(int a) {
						        value = a;
						    }
						    int value () {
						    	return value;
						    }
						    public static void main(String argv[]) {
						    }
						    public static int foo() {
						        int val;
						        int res = 0;
						        int n = 0;
						        X[] vals = X.values();
								for (int i = 0, max = vals.length; i < max; i++) {
									X e = vals[i];
						           if ( n == 1) {
						               continue;
						           }
						           val = e.value();
									System.out.println(val);
						        }
						        return res;
						    }
						}""",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #40 ()I
			  // Stack: 2, Locals: 7
			  public static int foo();
			     0  iconst_0
			     1  istore_1 [res]
			     2  iconst_0
			     3  istore_2 [n]
			     4  invokestatic X.values() : X[] [46]
			     7  astore_3 [vals]
			     8  iconst_0
			     9  istore 4 [i]
			    11  aload_3 [vals]
			    12  arraylength
			    13  istore 5 [max]
			    15  goto 48
			    18  aload_3 [vals]
			    19  iload 4 [i]
			    21  aaload
			    22  astore 6 [e]
			    24  iload_2 [n]
			    25  iconst_1
			    26  if_icmpne 32
			    29  goto 45
			    32  aload 6 [e]
			    34  invokevirtual X.value() : int [50]
			    37  istore_0 [val]
			    38  getstatic java.lang.System.out : java.io.PrintStream [52]
			    41  iload_0 [val]
			    42  invokevirtual java.io.PrintStream.println(int) : void [58]
			    45  iinc 4 1 [i]
			    48  iload 4 [i]
			    50  iload 5 [max]
			    52  if_icmplt 18
			    55  iload_1 [res]
			    56  ireturn
			      Line numbers:
			        [pc: 0, line: 14]
			        [pc: 2, line: 15]
			        [pc: 4, line: 16]
			        [pc: 8, line: 17]
			        [pc: 18, line: 18]
			        [pc: 24, line: 19]
			        [pc: 29, line: 20]
			        [pc: 32, line: 22]
			        [pc: 38, line: 23]
			        [pc: 45, line: 17]
			        [pc: 55, line: 25]
			      Local variable table:
			        [pc: 38, pc: 45] local: val index: 0 type: int
			        [pc: 2, pc: 57] local: res index: 1 type: int
			        [pc: 4, pc: 57] local: n index: 2 type: int
			        [pc: 8, pc: 57] local: vals index: 3 type: X[]
			        [pc: 11, pc: 55] local: i index: 4 type: int
			        [pc: 15, pc: 55] local: max index: 5 type: int
			        [pc: 24, pc: 45] local: e index: 6 type: X
			      Stack map table: number of frames 4
			        [pc: 18, full, stack: {}, locals: {_, int, int, X[], int, int}]
			        [pc: 32, append: {X}]
			        [pc: 45, chop 1 local(s)]
			        [pc: 48, same]
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test016() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"""
						public enum X {
						    a1(1), a2(5), a3(11);
						    int value;
						    X(int a) {
						        value = a;
						    }
						    int value () {
						    	return value;
						    }
						    public static void main(String argv[]) {
						    }
						    public static int foo() {
						        int val;
						        int res = 0;
						        int n = 0;
						        for (X e : X.values()) {
						            if ( n == 1) {
						                continue;
						            }
						            val = e.value();
									 System.out.println(val);
						        }
						        return res;
						    }
						}""",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #40 ()I
			  // Stack: 2, Locals: 7
			  public static int foo();
			     0  iconst_0
			     1  istore_1 [res]
			     2  iconst_0
			     3  istore_2 [n]
			     4  invokestatic X.values() : X[] [46]
			     7  dup
			     8  astore 6
			    10  arraylength
			    11  istore 5
			    13  iconst_0
			    14  istore 4
			    16  goto 48
			    19  aload 6
			    21  iload 4
			    23  aaload
			    24  astore_3 [e]
			    25  iload_2 [n]
			    26  iconst_1
			    27  if_icmpne 33
			    30  goto 45
			    33  aload_3 [e]
			    34  invokevirtual X.value() : int [50]
			    37  istore_0 [val]
			    38  getstatic java.lang.System.out : java.io.PrintStream [52]
			    41  iload_0 [val]
			    42  invokevirtual java.io.PrintStream.println(int) : void [58]
			    45  iinc 4 1
			    48  iload 4
			    50  iload 5
			    52  if_icmplt 19
			    55  iload_1 [res]
			    56  ireturn
			      Line numbers:
			        [pc: 0, line: 14]
			        [pc: 2, line: 15]
			        [pc: 4, line: 16]
			        [pc: 25, line: 17]
			        [pc: 30, line: 18]
			        [pc: 33, line: 20]
			        [pc: 38, line: 21]
			        [pc: 45, line: 16]
			        [pc: 55, line: 23]
			      Local variable table:
			        [pc: 38, pc: 45] local: val index: 0 type: int
			        [pc: 2, pc: 57] local: res index: 1 type: int
			        [pc: 4, pc: 57] local: n index: 2 type: int
			        [pc: 25, pc: 45] local: e index: 3 type: X
			      Stack map table: number of frames 4
			        [pc: 19, full, stack: {}, locals: {_, int, int, _, int, int, X[]}]
			        [pc: 33, full, stack: {}, locals: {_, int, int, X, int, int, X[]}]
			        [pc: 45, full, stack: {}, locals: {_, int, int, _, int, int, X[]}]
			        [pc: 48, same]
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test017() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"""
						public class X {
							public static void main(String argv[]) {
								int i;
							\t
								switch (i = 0) {
									case 0 :
										i = 1;
										break;
									default :
										;
								}
								System.out.print(i);
							}
						}""",
            },
			"1");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] argv);
			     0  iconst_0
			     1  dup
			     2  istore_1 [i]
			     3  tableswitch default: 22
			          case 0: 20
			    20  iconst_1
			    21  istore_1 [i]
			    22  getstatic java.lang.System.out : java.io.PrintStream [16]
			    25  iload_1 [i]
			    26  invokevirtual java.io.PrintStream.print(int) : void [22]
			    29  return
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 20, line: 7]
			        [pc: 22, line: 12]
			        [pc: 29, line: 13]
			      Local variable table:
			        [pc: 0, pc: 30] local: argv index: 0 type: java.lang.String[]
			        [pc: 3, pc: 30] local: i index: 1 type: int
			      Stack map table: number of frames 2
			        [pc: 20, append: {int}]
			        [pc: 22, same]
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test018() {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"""
						import java.util.*;
						
						interface Sequence<Value_Type> extends Iterable<Value_Type>{
						
						    Value_Type get(int i);
						    int length();
						    Value_Type set(int i, Value_Type value);
						}
						
						class ArraySequence<Value_Type> implements Sequence<Value_Type> {
						
						    public ArraySequence(int length) {}
						    public Value_Type get(int i) {
						        return null;
						    }
						    public int length() {
						        return 0;
						    }
						    public Value_Type set(int i, Value_Type value) {
						        return value;
						    }
						    public Iterator<Value_Type> iterator() {
						        return null;
						    }
						}
						
						class BirBlock {
						    void setRole(IrBlock.Role role) {}
						}
						
						class CatchChain {
						    int dispatcherAddress() {
						        return 0;
						    }
						}
						
						class ExceptionHandlerInfo {
						    int handlerProgramCounter() {
						        return 0;
						    }
						}
						
						interface IrBlock {
						    enum Role {
						        EXCEPTION_DISPATCHER
						    }
						}
						
						class ClassMethodActor {
						    Sequence<ExceptionHandlerInfo> exceptionHandlerInfos() {
						        return null;
						    }
						}
						
						public class X {
						
						    private Sequence<CatchChain> _catchChains;
						    private ClassMethodActor _classMethodActor;
						
						    public Sequence<BirBlock> getExceptionDispatchers(final BirBlock[] blockMap) {
						        final ArraySequence<BirBlock> dispatchers = new ArraySequence<BirBlock>(_catchChains.length());
						        for (int i = 0; i < _catchChains.length(); i++) {
						            final BirBlock dispatcher = blockMap[_catchChains.get(i).dispatcherAddress()];
						            dispatcher.setRole(IrBlock.Role.EXCEPTION_DISPATCHER);
						            dispatchers.set(i, dispatcher);
						        }
						        for (ExceptionHandlerInfo exceptionHandlerInfo : _classMethodActor.exceptionHandlerInfos()) {
						            blockMap[exceptionHandlerInfo.handlerProgramCounter()].setRole(IrBlock.Role.EXCEPTION_DISPATCHER);
						        }
						        return dispatchers;
						    }
						    public static void main(String[] args) {
								System.out.print("SUCCESS");
							}
						}""",
            },
			"SUCCESS");
	}

	public void test019() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					final public class X
					{
					    final class MyClass
					    {
					        void method1(final String s)
					        {
					        }
					    }
					
					    Object method1()
					    {
					        try
					        {
					            final MyClass myClass = null;
					
					            try
					            {
					                return null;
					            }
					            catch (final Throwable ex)
					            {
					                myClass.method1(this == null ? "" : "");
					            }
					
					            return null;
					        }
					        finally
					        {
					            {
					            }
					        }
					    }
					    public static void main(String[] args) {
							System.out.print("SUCCESS");
						}
					}""",
            },
			"SUCCESS");
	}
	public void test020() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					import java.util.*;
					public class X {
					    public static Map make(boolean sorted) {
					        return (sorted) ? new TreeMap() : new HashMap();
					    }
					    public static void main(String[] args) {
					       make(false);
							System.out.print("SUCCESS");
					    }
					}""",
            },
			"SUCCESS");
	}
	// 155423
	public void test021() throws Exception {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					public class X {
					   {
					      if (true) throw new NullPointerException();
					   }
					   X() {
					      System.out.println();
					   }
					}""",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  X();
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [8]
			     4  new java.lang.NullPointerException [10]
			     7  dup
			     8  invokespecial java.lang.NullPointerException() [12]
			    11  athrow
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 4, line: 3]
			      Local variable table:
			        [pc: 0, pc: 12] local: this index: 0 type: X
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	// 157247
	public void test022() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					public class X {
						public static void main(String[] args) {
							String errorMessage;
							try {
								foo();
								errorMessage = "No exception thrown";
							} catch (Exception e) {
								if (e instanceof NullPointerException) {
									System.out.println("SUCCESS");
									return;
								}
								errorMessage = "Exception thrown" + e;
							}
							System.out.println(errorMessage);
						}
						public static void foo() {
							throw new NullPointerException();
						}
					}""",
            },
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							boolean a = true, x;
							if (a ? false : (x = true))
								a = x;
							System.out.println("SUCCESS");
						}
					}""",
			},
			"SUCCESS");
	}

	public void test024() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					public class X {
						public static final int MAX_PROPERTIES = 25;
						public C c = new C();
						void foo(int i) {
							final int len = c.foo2();
							A f = new A(" Test ", i, 1, MAX_PROPERTIES) {
								@Override
								public double bar() {
									return len;
								}
								@Override
								public String toString() {
									return "SUCCESS";
								}
							};
							System.out.println(f);
						}
						public static void main(String[] args) {
							new X().foo(0);
						}
					}""",
        		"A.java",
        		"""
					class A {
						A(String s, double d, double d1, double d2) {}
						public double bar() {
							return 0.0;
						}
					}""",
        		"C.java",
        		"""
					class C {
						public int foo2() {
							return 0;
						}
					}""",
            },
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169596
	public void test025() {
		this.runConformTest(
	        new String[] {
	    		"X.java",
	    		"""
					public class X {
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					\t
						void foo(Object[] o) {}
					
						void bar(boolean b) {
							foo(new Object[] {"", "", b ? "" : ""}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$\s
						}
					}"""
	        },
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static final Object EXIT_OK = new Object();
						public static final Object EXIT_RELAUNCH = new Object();
						public static final Object EXIT_RESTART = new Object();
						public static final int RETURN_RESTART = 1;
						public static final String PROP_EXIT_CODE = "";
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
						private static int createAndRunWorkbench(Display display, IDEWorkbenchAdvisor advisor) {
							return 0;
						}
					\s
					    public Object run(Object args) throws Exception {
					        Display display = createDisplay();
					        try {
					            Shell shell = new Shell(display, SWT.ON_TOP);
					            try {
					                if (!checkInstanceLocation(shell)) {
					                    Platform.endSplash();
					                    return EXIT_OK;
					                }
					            } finally {
					                if (shell != null) {
										shell.dispose();
									}
					            }
					            int returnCode = X.createAndRunWorkbench(display,
					                    new IDEWorkbenchAdvisor());
					            if (returnCode != X.RETURN_RESTART) {
									return EXIT_OK;
								}
					            return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH
					                    : EXIT_RESTART;
					        } finally {
					            if (display != null) {
									display.dispose();
								}
					        }
					    }
						private boolean checkInstanceLocation(Shell shell) {
							// TODO Auto-generated method stub
							return false;
						}
						private Display createDisplay() {
							// TODO Auto-generated method stub
							return null;
						}
					}""",
				"Display.java",
				"""
					class Display {
					
						public void dispose() {
							// TODO Auto-generated method stub
						\t
						}
					}""",
				"Shell.java",
				"""
					class Shell {
						public Shell(Display display, int i) {
							// TODO Auto-generated constructor stub
						}
					
						public void dispose() {
							// TODO Auto-generated method stub
						\t
						}
					}""",
				"Platform.java",
				"""
					class Platform {
					
						public static void endSplash() {
							// TODO Auto-generated method stub
						\t
						}
					}""",
				"SWT.java",
				"""
					class SWT {
						public static final int ON_TOP = 1;\s
					}""",
				"IDEWorkbenchAdvisor.java",
				"class IDEWorkbenchAdvisor {\n" +
				"}"
    	},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
	public void test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					import java.io.InputStream;
					public class X {
						private static final int BUF_SIZE = 8192;
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
						BundleActivator activator;
						BundleHost bundle;
						public byte[] getBytes() throws IOException {
							InputStream in = getInputStream();
							int length = (int) getSize();
							byte[] classbytes;
							int bytesread = 0;
							int readcount;
							if (Debug.DEBUG && Debug.DEBUG_LOADER)
								Debug.println("  about to read " + length + " bytes from " + getName()); //$NON-NLS-1$ //$NON-NLS-2$
							try {
								if (length > 0) {
									classbytes = new byte[length];
									for (; bytesread < length; bytesread += readcount) {
										readcount = in.read(classbytes, bytesread, length - bytesread);
										if (readcount <= 0)
											break;
									}
								} else {
									length = BUF_SIZE;
									classbytes = new byte[length];
									readloop: while (true) {
										for (; bytesread < length; bytesread += readcount) {
											readcount = in.read(classbytes, bytesread, length - bytesread);
											if (readcount <= 0)
												break readloop;
										}
										byte[] oldbytes = classbytes;
										length += BUF_SIZE;
										classbytes = new byte[length];
										System.arraycopy(oldbytes, 0, classbytes, 0, bytesread);
									}
								}
								if (classbytes.length > bytesread) {
									byte[] oldbytes = classbytes;
									classbytes = new byte[bytesread];
									System.arraycopy(oldbytes, 0, classbytes, 0, bytesread);
								}
							} finally {
								try {
									in.close();
								} catch (IOException ee) {
									// ignore
								}
							}
							return classbytes;
						}
						protected void stop(Throwable t) throws BundleException {
								String clazz = "";//(activator == null) ? "" : activator.getClass().getName(); //$NON-NLS-1$
								throw new BundleException(NLS.bind(Msg.BUNDLE_ACTIVATOR_EXCEPTION, new Object[] {clazz, "stop", bundle.getSymbolicName() == null ? "" + bundle.getBundleId() : bundle.getSymbolicName()}), t); //$NON-NLS-1$ //$NON-NLS-2$\s
						}
						private String getName() {
							// TODO Auto-generated method stub
							return null;
						}
						private int getSize() {
							// TODO Auto-generated method stub
							return 0;
						}
						private InputStream getInputStream() {
							// TODO Auto-generated method stub
							return null;
						}
					}""",
				"Debug.java",
				"""
					class Debug {
						public static final boolean DEBUG = false;
						public static final boolean DEBUG_LOADER = false;
						public static final boolean DEBUG_GENERAL = false;
						public static void println(String string) {
							// TODO Auto-generated method stub
						\t
						}
						public static void printStackTrace(Throwable t) {
							// TODO Auto-generated method stub
						\t
						}
					}""",
				"AccessController.java",
				"""
					class AccessController {
						static void doPrivileged(Object o) {
						}
					}""",
				"BundleException.java",
				"""
					class BundleException extends Exception {
						private static final long serialVersionUID = 5758882959559471648L;
					
						public BundleException(String bind, Throwable t) {
							// TODO Auto-generated constructor stub
						}
					}""",
				"PrivilegedExceptionAction.java",
				"class PrivilegedExceptionAction {\n" +
				"}",
				"BundleActivator.java",
				"""
					class BundleActivator {
						public void stop(X x) {
							// TODO Auto-generated method stub
						\t
						}
					}""",
				"BundleHost.java",
				"""
					class BundleHost {
						public Object getSymbolicName() {
							// TODO Auto-generated method stub
							return null;
						}
						public String getBundleId() {
							// TODO Auto-generated method stub
							return null;
						}
					}""",
				"NLS.java",
				"""
					class NLS {
						public static String bind(String bundleActivatorException, Object[] objects) {
							// TODO Auto-generated method stub
							return null;
						}
					}""",
				"PrivilegedActionException.java",
				"""
					class PrivilegedActionException extends Exception {
						private static final long serialVersionUID = 3919969055057660822L;
						public Throwable getException() {
							// TODO Auto-generated method stub
							return null;
						}
					}""",
				"Msg.java",
				"""
					class Msg {
						public static final String BUNDLE_ACTIVATOR_EXCEPTION = "";
					}"""
    	},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.StringReader;
					
					public class X {
						public void loadVariablesAndContainers() {
							// backward compatibility, consider persistent property\t
							String qName = "1";
							String xmlString = "2";
						\t
							try {
								if (xmlString != null){
									StringReader reader = new StringReader(xmlString);
									Object o;
									try {
										StringBuffer buffer = null;
										o = new Object();
									} catch(RuntimeException e) {
										return;
									} catch(Exception e){
										return;
									} finally {
										reader.close();
									}
									System.out.println(reader);
								}
							} catch(Exception e){
								// problem loading xml file: nothing we can do
							} finally {
								if (xmlString != null){
									System.out.println(xmlString);
								}
							}
						}
					
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}"""
		},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171472
	public void test029() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public Object foo() {
							Object status;
							try {
								status= bar();
							} catch (RuntimeException x) {
								status= foo2(x);
							} finally {
								System.out.println();
							}
							return status;
						}
						public Object bar() {
							return null;
						}
						public Object foo2(Exception e) {
							return null;
						}
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}"""
		},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171472
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Collections;
					import java.util.List;
					public class X {
					
						private static final String COMPUTE_COMPLETION_PROPOSALS= "computeCompletionProposals()"; //$NON-NLS-1$
						private Object fLastError;
						private boolean fIsReportingDelay;
						private CompletionProposalComputerRegistry fRegistry;
						public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
							if (!isEnabled())
								return Collections.EMPTY_LIST;
					
							IStatus status;
							try {
								IJavaCompletionProposalComputer computer= getComputer();
								if (computer == null) // not active yet
									return Collections.EMPTY_LIST;
							\t
								try {
									PerformanceStats stats= startMeter(context, computer);
									List proposals= computer.computeCompletionProposals(context, monitor);
									stopMeter(stats, COMPUTE_COMPLETION_PROPOSALS);
								\t
									if (proposals != null) {
										fLastError= computer.getErrorMessage();
										return proposals;
									}
								} finally {
									fIsReportingDelay= true;
								}
								status= createAPIViolationStatus(COMPUTE_COMPLETION_PROPOSALS);
							} catch (InvalidRegistryObjectException x) {
								status= createExceptionStatus(x);
							} catch (CoreException x) {
								status= createExceptionStatus(x);
							} catch (RuntimeException x) {
								status= createExceptionStatus(x);
							} finally {
								monitor.done();
							}
					
							fRegistry.informUser(this, status);
					
							return Collections.EMPTY_LIST;
						}
					
						private IStatus createExceptionStatus(Exception x) {
							// TODO Auto-generated method stub
							return null;
						}
					
						private IStatus createAPIViolationStatus(String computeCompletionProposals) {
							// TODO Auto-generated method stub
							return null;
						}
					
						private void stopMeter(PerformanceStats stats, String computeCompletionProposals) {
							// TODO Auto-generated method stub
						\t
						}
					
						private PerformanceStats startMeter(ContentAssistInvocationContext context, IJavaCompletionProposalComputer computer) {
							// TODO Auto-generated method stub
							return null;
						}
					
						private IJavaCompletionProposalComputer getComputer() throws CoreException, InvalidRegistryObjectException {
							// TODO Auto-generated method stub
							return null;
						}
					
						private boolean isEnabled() {
							// TODO Auto-generated method stub
							return false;
						}
					\t
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}""",
				"IProgressMonitor.java",
				"""
					interface IProgressMonitor {
						void done();
					}""",
				"ContentAssistInvocationContext.java",
				"class ContentAssistInvocationContext {\n" +
				"}",
				"IStatus.java",
				"interface IStatus {}",
				"IJavaCompletionProposalComputer.java",
				"""
					import java.util.List;
					interface IJavaCompletionProposalComputer {
						List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor);
						Object getErrorMessage();
					}""",
				"PerformanceStats.java",
				"class PerformanceStats {}",
				"CompletionProposalComputerRegistry.java",
				"""
					class CompletionProposalComputerRegistry {
						public void informUser(X x, IStatus status) {
						}
					}""",
				"InvalidRegistryObjectException.java",
				"""
					class InvalidRegistryObjectException extends Exception {
						private static final long serialVersionUID = 8943194846421947853L;
					}""",
				"CoreException.java",
				"""
					class CoreException extends Exception {
						private static final long serialVersionUID = 3996792687633449517L;
					}"""
		},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=168665
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						String s;
						X() {
							this.s = "";
						}
						X(String s, double d, double d2, double i) {
							this.s = s;
						}
						public static final int CONST = 1;
						public int foo() {
							return 0;
						}
						public double value(double d1) {
							return d1;
						}
						public void bar(int start) {
							final int len = foo();
							X x = new X("SUCCESS", start, 1, CONST) {
								@Override
								public double value(double newValue) {
									return len;
								}
							};
							System.out.println(x);
						}
						public static void main(String[] args) {
							new X().bar(1);
						}
						public String toString() {
							return this.s;
						}
					}""",
			},
			"SUCCESS");
	}
	public void test032() {
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String[] args) {
								int i = args.length;
						       X[] array = new X[] { i == 0 ? null : null };
								System.out.print("SUCCESS" + array.length);
						    }
						}""",
			},
		"SUCCESS1");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184102
	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public enum X {
						C { @Override public boolean test() { return true; } };
						static {
							for (int i = 0; i < 1; i++) {}
						}
						public boolean test() {
						return false;
						}
						public static void main(String[] args) {
							System.out.print("SUCCESS");
						}
					}""",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184102
	public void test034() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public enum X {
						C;
						static {
							for (int i = 0; i < 1; i++) {}
						}
						public boolean test() {
						return false;
						}
						public static void main(String[] args) {
							System.out.print("SUCCESS");
						}
					}""",
			},
		"SUCCESS");
	}

	// add more bytecode coverage: fneg, lneg, dneg, dstore_0, f2l, fstore_0, fstore_2, lstore_0 and saload
	public void test035() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static double foo() {
							double d = 3.0;
							d = -d;
							return d > 1.0 ? d : -d;
						}
					
						static float foo2() {
							float f = 3.0f;
							int i = 0;
							float f2 = f+ i;
							long l = (long) f;
							l += f2;
							return i == 0 ? f : -f + (float) l;
						}
					
						static long foo3() {
							long l = Long.MAX_VALUE - 3;
							boolean b = true;
							return b ? l : -l;
						}
					\t
						static short foo4() {
							short[] tab = new short[] { 1, 2, 3 };
							return tab.length == 3 ? tab[2] : (tab.length == 2 ? tab[1] : tab[0]);
						}
					
						public static void main(String args[]) {
							foo();
							foo2();
							foo3();
							foo4();
							System.out.print("SUCCESS");
						}
					}""",
			},
		"SUCCESS");
	}

	// fix verify error
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.Set;
					
					public class X {
						public static void main(String[] args) {
							System.out.print("SUCCESS");
						}
						public void foo(Object o, boolean b) {
							String[] models = new String[] {};
							Map map = null;
							Set set  = null;
							for (int n = 0; n < models.length; n++) {	bar(models[n]); }
							foo2(new Object(),
									set,
									map,
									!b);
						}
						void bar(String s) {}
						void foo2(Object o, Object s, Object m, boolean b) {}
					}""",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236336
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.print("SUCCESS");
						}
						String field;
						String field2;
						public void foo(int index, Object[] objs, Object[] objs2) {
							String methodName = "foo";
							int i = -1;
							try {
								switch (index) {
									case 1130: {
										int j = ((Integer) objs[0]).intValue();
										int k = ((Integer) objs[0]).intValue();
										{
											String s = field;
											String s2 = field2;
											synchronized (s2) {
												String s3 = s2;
												try {
													System.out.println(s);
													System.out.println(s2);
													System.out.println(s3);
												} finally {
													s2.toString();
												}
											}
											}
									}
									break;
									case 0 :
										System.out.println(methodName + i);
									break;
									case 1 :
										System.out.println(methodName + i);
									break;
									case 2 :
										System.out.println(methodName + i);
									break;
									case 3 :
										System.out.println(methodName + i);
									break;
									case 4 :
										System.out.println(methodName + i);
									break;
									case 5 :
										System.out.println(methodName + i);
									break;
									case 6 :
										System.out.println(methodName + i);
									break;
									case 7 :
										System.out.println(methodName + i);
									break;
									case 8 :
										System.out.println(methodName + i);
									break;
									case 9 :
										System.out.println(methodName + i);
									break;
									case 10 :
										System.out.println(methodName + i);
									break;
									case 11 :
										System.out.println(methodName + i);
									break;
									case 12 :
										System.out.println(methodName + i);
									break;
									case 13 :
										System.out.println(methodName + i);
									break;
									case 14 :
										System.out.println(methodName + i);
									break;
									case 15 :
										System.out.println(methodName + i);
									break;
									case 16 :
										System.out.println(methodName + i);
									break;
									case 17 :
										System.out.println(methodName + i);
									break;
									case 18 :
										System.out.println(methodName + i);
									break;
									case 19 :
										System.out.println(methodName + i);
									break;
									case 20 :
										System.out.println(methodName + i);
									break;
									case 21 :
										System.out.println(methodName + i);
									break;
									case 22 :
										System.out.println(methodName + i);
									break;
									case 23 :
										System.out.println(methodName + i);
									break;
									case 24 :
										System.out.println(methodName + i);
									break;
									case 25 :
										System.out.println(methodName + i);
									break;
									case 26 :
										System.out.println(methodName + i);
									break;
									case 27 :
										System.out.println(methodName + i);
									break;
									case 28 :
										System.out.println(methodName + i);
									break;
									case 29 :
										System.out.println(methodName + i);
									break;
									case 30 :
										System.out.println(methodName + i);
									break;
									case 31 :
										System.out.println(methodName + i);
									break;
									case 32 :
										System.out.println(methodName + i);
									break;
									case 33 :
										System.out.println(methodName + i);
									break;
									case 34 :
										System.out.println(methodName + i);
									break;
									case 35 :
										System.out.println(methodName + i);
									break;
									case 36 :
										System.out.println(methodName + i);
									break;
									case 37 :
										System.out.println(methodName + i);
									break;
									case 38 :
										System.out.println(methodName + i);
									break;
									case 39 :
										System.out.println(methodName + i);
									break;
									case 40 :
										System.out.println(methodName + i);
									break;
									case 41 :
										System.out.println(methodName + i);
									break;
									case 42 :
										System.out.println(methodName + i);
									break;
									case 43 :
										System.out.println(methodName + i);
									break;
									case 44 :
										System.out.println(methodName + i);
									break;
									case 45 :
										System.out.println(methodName + i);
									break;
									case 46 :
										System.out.println(methodName + i);
									break;
									case 47 :
										System.out.println(methodName + i);
									break;
									case 48 :
										System.out.println(methodName + i);
									break;
									case 49 :
										System.out.println(methodName + i);
									break;
									case 50 :
										System.out.println(methodName + i);
									break;
									case 51 :
										System.out.println(methodName + i);
									break;
									case 52 :
										System.out.println(methodName + i);
									break;
									case 53 :
										System.out.println(methodName + i);
									break;
									case 54 :
										System.out.println(methodName + i);
									break;
									case 55 :
										System.out.println(methodName + i);
									break;
									case 56 :
										System.out.println(methodName + i);
									break;
									case 57 :
										System.out.println(methodName + i);
									break;
									case 58 :
										System.out.println(methodName + i);
									break;
									case 59 :
										System.out.println(methodName + i);
									break;
									case 60 :
										System.out.println(methodName + i);
									break;
									case 61 :
										System.out.println(methodName + i);
									break;
									case 62 :
										System.out.println(methodName + i);
									break;
									case 63 :
										System.out.println(methodName + i);
									break;
									case 64 :
										System.out.println(methodName + i);
									break;
									case 65 :
										System.out.println(methodName + i);
									break;
									case 66 :
										System.out.println(methodName + i);
									break;
									case 67 :
										System.out.println(methodName + i);
									break;
									case 68 :
										System.out.println(methodName + i);
									break;
									case 69 :
										System.out.println(methodName + i);
									break;
									case 70 :
										System.out.println(methodName + i);
									break;
									case 71 :
										System.out.println(methodName + i);
									break;
									case 72 :
										System.out.println(methodName + i);
									break;
									case 73 :
										System.out.println(methodName + i);
									break;
									case 74 :
										System.out.println(methodName + i);
									break;
									case 75 :
										System.out.println(methodName + i);
									break;
									case 76 :
										System.out.println(methodName + i);
									break;
									case 77 :
										System.out.println(methodName + i);
									break;
									case 78 :
										System.out.println(methodName + i);
									break;
									case 79 :
										System.out.println(methodName + i);
									break;
									case 80 :
										System.out.println(methodName + i);
									break;
									case 81 :
										System.out.println(methodName + i);
									break;
									case 82 :
										System.out.println(methodName + i);
									break;
									case 83 :
										System.out.println(methodName + i);
									break;
									case 84 :
										System.out.println(methodName + i);
									break;
									case 85 :
										System.out.println(methodName + i);
									break;
									case 86 :
										System.out.println(methodName + i);
									break;
									case 87 :
										System.out.println(methodName + i);
									break;
									case 88 :
										System.out.println(methodName + i);
									break;
									case 89 :
										System.out.println(methodName + i);
									break;
									case 90 :
										System.out.println(methodName + i);
									break;
									case 91 :
										System.out.println(methodName + i);
									break;
									case 92 :
										System.out.println(methodName + i);
									break;
									case 93 :
										System.out.println(methodName + i);
									break;
									case 94 :
										System.out.println(methodName + i);
									break;
									case 95 :
										System.out.println(methodName + i);
									break;
									case 96 :
										System.out.println(methodName + i);
									break;
									case 97 :
										System.out.println(methodName + i);
									break;
									case 98 :
										System.out.println(methodName + i);
									break;
									case 99 :
										System.out.println(methodName + i);
									break;
									case 100 :
										System.out.println(methodName + i);
									break;
									case 101 :
										System.out.println(methodName + i);
									break;
									case 102 :
										System.out.println(methodName + i);
									break;
									case 103 :
										System.out.println(methodName + i);
									break;
									case 104 :
										System.out.println(methodName + i);
									break;
									case 105 :
										System.out.println(methodName + i);
									break;
									case 106 :
										System.out.println(methodName + i);
									break;
									case 107 :
										System.out.println(methodName + i);
									break;
									case 108 :
										System.out.println(methodName + i);
									break;
									case 109 :
										System.out.println(methodName + i);
									break;
									case 110 :
										System.out.println(methodName + i);
									break;
									case 111 :
										System.out.println(methodName + i);
									break;
									case 112 :
										System.out.println(methodName + i);
									break;
									case 113 :
										System.out.println(methodName + i);
									break;
									case 114 :
										System.out.println(methodName + i);
									break;
									case 115 :
										System.out.println(methodName + i);
									break;
									case 116 :
										System.out.println(methodName + i);
									break;
									case 117 :
										System.out.println(methodName + i);
									break;
									case 118 :
										System.out.println(methodName + i);
									break;
									case 119 :
										System.out.println(methodName + i);
									break;
									case 120 :
										System.out.println(methodName + i);
									break;
									case 121 :
										System.out.println(methodName + i);
									break;
									case 122 :
										System.out.println(methodName + i);
									break;
									case 123 :
										System.out.println(methodName + i);
									break;
									case 124 :
										System.out.println(methodName + i);
									break;
									case 125 :
										System.out.println(methodName + i);
									break;
									case 126 :
										System.out.println(methodName + i);
									break;
									case 127 :
										System.out.println(methodName + i);
									break;
									case 128 :
										System.out.println(methodName + i);
									break;
									case 129 :
										System.out.println(methodName + i);
									break;
									case 130 :
										System.out.println(methodName + i);
									break;
									case 131 :
										System.out.println(methodName + i);
									break;
									case 132 :
										System.out.println(methodName + i);
									break;
									case 133 :
										System.out.println(methodName + i);
									break;
									case 134 :
										System.out.println(methodName + i);
									break;
									case 135 :
										System.out.println(methodName + i);
									break;
									case 136 :
										System.out.println(methodName + i);
									break;
									case 137 :
										System.out.println(methodName + i);
									break;
									case 138 :
										System.out.println(methodName + i);
									break;
									case 139 :
										System.out.println(methodName + i);
									break;
									case 140 :
										System.out.println(methodName + i);
									break;
									case 141 :
										System.out.println(methodName + i);
									break;
									case 142 :
										System.out.println(methodName + i);
									break;
									case 143 :
										System.out.println(methodName + i);
									break;
									case 144 :
										System.out.println(methodName + i);
									break;
									case 145 :
										System.out.println(methodName + i);
									break;
									case 146 :
										System.out.println(methodName + i);
									break;
									case 147 :
										System.out.println(methodName + i);
									break;
									case 148 :
										System.out.println(methodName + i);
									break;
									case 149 :
										System.out.println(methodName + i);
									break;
									case 150 :
										System.out.println(methodName + i);
									break;
									case 151 :
										System.out.println(methodName + i);
									break;
									case 152 :
										System.out.println(methodName + i);
									break;
									case 153 :
										System.out.println(methodName + i);
									break;
									case 154 :
										System.out.println(methodName + i);
									break;
									case 155 :
										System.out.println(methodName + i);
									break;
									case 156 :
										System.out.println(methodName + i);
									break;
									case 157 :
										System.out.println(methodName + i);
									break;
									case 158 :
										System.out.println(methodName + i);
									break;
									case 159 :
										System.out.println(methodName + i);
									break;
									case 160 :
										System.out.println(methodName + i);
									break;
									case 161 :
										System.out.println(methodName + i);
									break;
									case 162 :
										System.out.println(methodName + i);
									break;
									case 163 :
										System.out.println(methodName + i);
									break;
									case 164 :
										System.out.println(methodName + i);
									break;
									case 165 :
										System.out.println(methodName + i);
									break;
									case 166 :
										System.out.println(methodName + i);
									break;
									case 167 :
										System.out.println(methodName + i);
									break;
									case 168 :
										System.out.println(methodName + i);
									break;
									case 169 :
										System.out.println(methodName + i);
									break;
									case 170 :
										System.out.println(methodName + i);
									break;
									case 171 :
										System.out.println(methodName + i);
									break;
									case 172 :
										System.out.println(methodName + i);
									break;
									case 173 :
										System.out.println(methodName + i);
									break;
									case 174 :
										System.out.println(methodName + i);
									break;
									case 175 :
										System.out.println(methodName + i);
									break;
									case 176 :
										System.out.println(methodName + i);
									break;
									case 177 :
										System.out.println(methodName + i);
									break;
									case 178 :
										System.out.println(methodName + i);
									break;
									case 179 :
										System.out.println(methodName + i);
									break;
									case 180 :
										System.out.println(methodName + i);
									break;
									case 181 :
										System.out.println(methodName + i);
									break;
									case 182 :
										System.out.println(methodName + i);
									break;
									case 183 :
										System.out.println(methodName + i);
									break;
									case 184 :
										System.out.println(methodName + i);
									break;
									case 185 :
										System.out.println(methodName + i);
									break;
									case 186 :
										System.out.println(methodName + i);
									break;
									case 187 :
										System.out.println(methodName + i);
									break;
									case 188 :
										System.out.println(methodName + i);
									break;
									case 189 :
										System.out.println(methodName + i);
									break;
									case 190 :
										System.out.println(methodName + i);
									break;
									case 191 :
										System.out.println(methodName + i);
									break;
									case 192 :
										System.out.println(methodName + i);
									break;
									case 193 :
										System.out.println(methodName + i);
									break;
									case 194 :
										System.out.println(methodName + i);
									break;
									case 195 :
										System.out.println(methodName + i);
									break;
									case 196 :
										System.out.println(methodName + i);
									break;
									case 197 :
										System.out.println(methodName + i);
									break;
									case 198 :
										System.out.println(methodName + i);
									break;
									case 199 :
										System.out.println(methodName + i);
									break;
									case 200 :
										System.out.println(methodName + i);
									break;
									case 201 :
										System.out.println(methodName + i);
									break;
									case 202 :
										System.out.println(methodName + i);
									break;
									case 203 :
										System.out.println(methodName + i);
									break;
									case 204 :
										System.out.println(methodName + i);
									break;
									case 205 :
										System.out.println(methodName + i);
									break;
									case 206 :
										System.out.println(methodName + i);
									break;
									case 207 :
										System.out.println(methodName + i);
									break;
									case 208 :
										System.out.println(methodName + i);
									break;
									case 209 :
										System.out.println(methodName + i);
									break;
									case 210 :
										System.out.println(methodName + i);
									break;
									case 211 :
										System.out.println(methodName + i);
									break;
									case 212 :
										System.out.println(methodName + i);
									break;
									case 213 :
										System.out.println(methodName + i);
									break;
									case 214 :
										System.out.println(methodName + i);
									break;
									case 215 :
										System.out.println(methodName + i);
									break;
									case 216 :
										System.out.println(methodName + i);
									break;
									case 217 :
										System.out.println(methodName + i);
									break;
									case 218 :
										System.out.println(methodName + i);
									break;
									case 219 :
										System.out.println(methodName + i);
									break;
									case 220 :
										System.out.println(methodName + i);
									break;
									case 221 :
										System.out.println(methodName + i);
									break;
									case 222 :
										System.out.println(methodName + i);
									break;
									case 223 :
										System.out.println(methodName + i);
									break;
									case 224 :
										System.out.println(methodName + i);
									break;
									case 225 :
										System.out.println(methodName + i);
									break;
									case 226 :
										System.out.println(methodName + i);
									break;
									case 227 :
										System.out.println(methodName + i);
									break;
									case 228 :
										System.out.println(methodName + i);
									break;
									case 229 :
										System.out.println(methodName + i);
									break;
									case 230 :
										System.out.println(methodName + i);
									break;
									case 231 :
										System.out.println(methodName + i);
									break;
									case 232 :
										System.out.println(methodName + i);
									break;
									case 233 :
										System.out.println(methodName + i);
									break;
									case 234 :
										System.out.println(methodName + i);
									break;
									case 235 :
										System.out.println(methodName + i);
									break;
									case 236 :
										System.out.println(methodName + i);
									break;
									case 237 :
										System.out.println(methodName + i);
									break;
									case 238 :
										System.out.println(methodName + i);
									break;
									case 239 :
										System.out.println(methodName + i);
									break;
									case 240 :
										System.out.println(methodName + i);
									break;
									case 241 :
										System.out.println(methodName + i);
									break;
									case 242 :
										System.out.println(methodName + i);
									break;
									case 243 :
										System.out.println(methodName + i);
									break;
									case 244 :
										System.out.println(methodName + i);
									break;
									case 245 :
										System.out.println(methodName + i);
									break;
									case 246 :
										System.out.println(methodName + i);
									break;
									case 247 :
										System.out.println(methodName + i);
									break;
									case 248 :
										System.out.println(methodName + i);
									break;
									case 249 :
										System.out.println(methodName + i);
									break;
									case 250 :
										System.out.println(methodName + i);
									break;
									case 251 :
										System.out.println(methodName + i);
									break;
									case 252 :
										System.out.println(methodName + i);
									break;
									case 253 :
										System.out.println(methodName + i);
									break;
									case 254 :
										System.out.println(methodName + i);
									break;
									case 255 :
										System.out.println(methodName + i);
									break;
									case 256 :
										System.out.println(methodName + i);
									break;
									case 257 :
										System.out.println(methodName + i);
									break;
									case 258 :
										System.out.println(methodName + i);
									break;
									case 259 :
										System.out.println(methodName + i);
									break;
									case 260 :
										System.out.println(methodName + i);
									break;
									case 261 :
										System.out.println(methodName + i);
									break;
									case 262 :
										System.out.println(methodName + i);
									break;
									case 263 :
										System.out.println(methodName + i);
									break;
									case 264 :
										System.out.println(methodName + i);
									break;
									case 265 :
										System.out.println(methodName + i);
									break;
									case 266 :
										System.out.println(methodName + i);
									break;
									case 267 :
										System.out.println(methodName + i);
									break;
									case 268 :
										System.out.println(methodName + i);
									break;
									case 269 :
										System.out.println(methodName + i);
									break;
									case 270 :
										System.out.println(methodName + i);
									break;
									case 271 :
										System.out.println(methodName + i);
									break;
									case 272 :
										System.out.println(methodName + i);
									break;
									case 273 :
										System.out.println(methodName + i);
									break;
									case 274 :
										System.out.println(methodName + i);
									break;
									case 275 :
										System.out.println(methodName + i);
									break;
									case 276 :
										System.out.println(methodName + i);
									break;
									case 277 :
										System.out.println(methodName + i);
									break;
									case 278 :
										System.out.println(methodName + i);
									break;
									case 279 :
										System.out.println(methodName + i);
									break;
									case 280 :
										System.out.println(methodName + i);
									break;
									case 281 :
										System.out.println(methodName + i);
									break;
									case 282 :
										System.out.println(methodName + i);
									break;
									case 283 :
										System.out.println(methodName + i);
									break;
									case 284 :
										System.out.println(methodName + i);
									break;
									case 285 :
										System.out.println(methodName + i);
									break;
									case 286 :
										System.out.println(methodName + i);
									break;
									case 287 :
										System.out.println(methodName + i);
									break;
									case 288 :
										System.out.println(methodName + i);
									break;
									case 289 :
										System.out.println(methodName + i);
									break;
									case 290 :
										System.out.println(methodName + i);
									break;
									case 291 :
										System.out.println(methodName + i);
									break;
									case 292 :
										System.out.println(methodName + i);
									break;
									case 293 :
										System.out.println(methodName + i);
									break;
									case 294 :
										System.out.println(methodName + i);
									break;
									case 295 :
										System.out.println(methodName + i);
									break;
									case 296 :
										System.out.println(methodName + i);
									break;
									case 297 :
										System.out.println(methodName + i);
									break;
									case 298 :
										System.out.println(methodName + i);
									break;
									case 299 :
										System.out.println(methodName + i);
									break;
									case 300 :
										System.out.println(methodName + i);
									break;
									case 301 :
										System.out.println(methodName + i);
									break;
									case 302 :
										System.out.println(methodName + i);
									break;
									case 303 :
										System.out.println(methodName + i);
									break;
									case 304 :
										System.out.println(methodName + i);
									break;
									case 305 :
										System.out.println(methodName + i);
									break;
									case 306 :
										System.out.println(methodName + i);
									break;
									case 307 :
										System.out.println(methodName + i);
									break;
									case 308 :
										System.out.println(methodName + i);
									break;
									case 309 :
										System.out.println(methodName + i);
									break;
									case 310 :
										System.out.println(methodName + i);
									break;
									case 311 :
										System.out.println(methodName + i);
									break;
									case 312 :
										System.out.println(methodName + i);
									break;
									case 313 :
										System.out.println(methodName + i);
									break;
									case 314 :
										System.out.println(methodName + i);
									break;
									case 315 :
										System.out.println(methodName + i);
									break;
									case 316 :
										System.out.println(methodName + i);
									break;
									case 317 :
										System.out.println(methodName + i);
									break;
									case 318 :
										System.out.println(methodName + i);
									break;
									case 319 :
										System.out.println(methodName + i);
									break;
									case 320 :
										System.out.println(methodName + i);
									break;
									case 321 :
										System.out.println(methodName + i);
									break;
									case 322 :
										System.out.println(methodName + i);
									break;
									case 323 :
										System.out.println(methodName + i);
									break;
									case 324 :
										System.out.println(methodName + i);
									break;
									case 325 :
										System.out.println(methodName + i);
									break;
									case 326 :
										System.out.println(methodName + i);
									break;
									case 327 :
										System.out.println(methodName + i);
									break;
									case 328 :
										System.out.println(methodName + i);
									break;
									case 329 :
										System.out.println(methodName + i);
									break;
									case 330 :
										System.out.println(methodName + i);
									break;
									case 331 :
										System.out.println(methodName + i);
									break;
									case 332 :
										System.out.println(methodName + i);
									break;
									case 333 :
										System.out.println(methodName + i);
									break;
									case 334 :
										System.out.println(methodName + i);
									break;
									case 335 :
										System.out.println(methodName + i);
									break;
									case 336 :
										System.out.println(methodName + i);
									break;
									case 337 :
										System.out.println(methodName + i);
									break;
									case 338 :
										System.out.println(methodName + i);
									break;
									case 339 :
										System.out.println(methodName + i);
									break;
									case 340 :
										System.out.println(methodName + i);
									break;
									case 341 :
										System.out.println(methodName + i);
									break;
									case 342 :
										System.out.println(methodName + i);
									break;
									case 343 :
										System.out.println(methodName + i);
									break;
									case 344 :
										System.out.println(methodName + i);
									break;
									case 345 :
										System.out.println(methodName + i);
									break;
									case 346 :
										System.out.println(methodName + i);
									break;
									case 347 :
										System.out.println(methodName + i);
									break;
									case 348 :
										System.out.println(methodName + i);
									break;
									case 349 :
										System.out.println(methodName + i);
									break;
									case 350 :
										System.out.println(methodName + i);
									break;
									case 351 :
										System.out.println(methodName + i);
									break;
									case 352 :
										System.out.println(methodName + i);
									break;
									case 353 :
										System.out.println(methodName + i);
									break;
									case 354 :
										System.out.println(methodName + i);
									break;
									case 355 :
										System.out.println(methodName + i);
									break;
									case 356 :
										System.out.println(methodName + i);
									break;
									case 357 :
										System.out.println(methodName + i);
									break;
									case 358 :
										System.out.println(methodName + i);
									break;
									case 359 :
										System.out.println(methodName + i);
									break;
									case 360 :
										System.out.println(methodName + i);
									break;
									case 361 :
										System.out.println(methodName + i);
									break;
									case 362 :
										System.out.println(methodName + i);
									break;
									case 363 :
										System.out.println(methodName + i);
									break;
									case 364 :
										System.out.println(methodName + i);
									break;
									case 365 :
										System.out.println(methodName + i);
									break;
									case 366 :
										System.out.println(methodName + i);
									break;
									case 367 :
										System.out.println(methodName + i);
									break;
									case 368 :
										System.out.println(methodName + i);
									break;
									case 369 :
										System.out.println(methodName + i);
									break;
									case 370 :
										System.out.println(methodName + i);
									break;
									case 371 :
										System.out.println(methodName + i);
									break;
									case 372 :
										System.out.println(methodName + i);
									break;
									case 373 :
										System.out.println(methodName + i);
									break;
									case 374 :
										System.out.println(methodName + i);
									break;
									case 375 :
										System.out.println(methodName + i);
									break;
									case 376 :
										System.out.println(methodName + i);
									break;
									case 377 :
										System.out.println(methodName + i);
									break;
									case 378 :
										System.out.println(methodName + i);
									break;
									case 379 :
										System.out.println(methodName + i);
									break;
									case 380 :
										System.out.println(methodName + i);
									break;
									case 381 :
										System.out.println(methodName + i);
									break;
									case 382 :
										System.out.println(methodName + i);
									break;
									case 383 :
										System.out.println(methodName + i);
									break;
									case 384 :
										System.out.println(methodName + i);
									break;
									case 385 :
										System.out.println(methodName + i);
									break;
									case 386 :
										System.out.println(methodName + i);
									break;
									case 387 :
										System.out.println(methodName + i);
									break;
									case 388 :
										System.out.println(methodName + i);
									break;
									case 389 :
										System.out.println(methodName + i);
									break;
									case 390 :
										System.out.println(methodName + i);
									break;
									case 391 :
										System.out.println(methodName + i);
									break;
									case 392 :
										System.out.println(methodName + i);
									break;
									case 393 :
										System.out.println(methodName + i);
									break;
									case 394 :
										System.out.println(methodName + i);
									break;
									case 395 :
										System.out.println(methodName + i);
									break;
									case 396 :
										System.out.println(methodName + i);
									break;
									case 397 :
										System.out.println(methodName + i);
									break;
									case 398 :
										System.out.println(methodName + i);
									break;
									case 399 :
										System.out.println(methodName + i);
									break;
									case 400 :
										System.out.println(methodName + i);
									break;
									case 401 :
										System.out.println(methodName + i);
									break;
									case 402 :
										System.out.println(methodName + i);
									break;
									case 403 :
										System.out.println(methodName + i);
									break;
									case 404 :
										System.out.println(methodName + i);
									break;
									case 405 :
										System.out.println(methodName + i);
									break;
									case 406 :
										System.out.println(methodName + i);
									break;
									case 407 :
										System.out.println(methodName + i);
									break;
									case 408 :
										System.out.println(methodName + i);
									break;
									case 409 :
										System.out.println(methodName + i);
									break;
									case 410 :
										System.out.println(methodName + i);
									break;
									case 411 :
										System.out.println(methodName + i);
									break;
									case 412 :
										System.out.println(methodName + i);
									break;
									case 413 :
										System.out.println(methodName + i);
									break;
									case 414 :
										System.out.println(methodName + i);
									break;
									case 415 :
										System.out.println(methodName + i);
									break;
									case 416 :
										System.out.println(methodName + i);
									break;
									case 417 :
										System.out.println(methodName + i);
									break;
									case 418 :
										System.out.println(methodName + i);
									break;
									case 419 :
										System.out.println(methodName + i);
									break;
									case 420 :
										System.out.println(methodName + i);
									break;
									case 421 :
										System.out.println(methodName + i);
									break;
									case 422 :
										System.out.println(methodName + i);
									break;
									case 423 :
										System.out.println(methodName + i);
									break;
									case 424 :
										System.out.println(methodName + i);
									break;
									case 425 :
										System.out.println(methodName + i);
									break;
									case 426 :
										System.out.println(methodName + i);
									break;
									case 427 :
										System.out.println(methodName + i);
									break;
									case 428 :
										System.out.println(methodName + i);
									break;
									case 429 :
										System.out.println(methodName + i);
									break;
									case 430 :
										System.out.println(methodName + i);
									break;
									case 431 :
										System.out.println(methodName + i);
									break;
									case 432 :
										System.out.println(methodName + i);
									break;
									case 433 :
										System.out.println(methodName + i);
									break;
									case 434 :
										System.out.println(methodName + i);
									break;
									case 435 :
										System.out.println(methodName + i);
									break;
									case 436 :
										System.out.println(methodName + i);
									break;
									case 437 :
										System.out.println(methodName + i);
									break;
									case 438 :
										System.out.println(methodName + i);
									break;
									case 439 :
										System.out.println(methodName + i);
									break;
									case 440 :
										System.out.println(methodName + i);
									break;
									case 441 :
										System.out.println(methodName + i);
									break;
									case 442 :
										System.out.println(methodName + i);
									break;
									case 443 :
										System.out.println(methodName + i);
									break;
									case 444 :
										System.out.println(methodName + i);
									break;
									case 445 :
										System.out.println(methodName + i);
									break;
									case 446 :
										System.out.println(methodName + i);
									break;
									case 447 :
										System.out.println(methodName + i);
									break;
									case 448 :
										System.out.println(methodName + i);
									break;
									case 449 :
										System.out.println(methodName + i);
									break;
									case 450 :
										System.out.println(methodName + i);
									break;
									case 451 :
										System.out.println(methodName + i);
									break;
									case 452 :
										System.out.println(methodName + i);
									break;
									case 453 :
										System.out.println(methodName + i);
									break;
									case 454 :
										System.out.println(methodName + i);
									break;
									case 455 :
										System.out.println(methodName + i);
									break;
									case 456 :
										System.out.println(methodName + i);
									break;
									case 457 :
										System.out.println(methodName + i);
									break;
									case 458 :
										System.out.println(methodName + i);
									break;
									case 459 :
										System.out.println(methodName + i);
									break;
									case 460 :
										System.out.println(methodName + i);
									break;
									case 461 :
										System.out.println(methodName + i);
									break;
									case 462 :
										System.out.println(methodName + i);
									break;
									case 463 :
										System.out.println(methodName + i);
									break;
									case 464 :
										System.out.println(methodName + i);
									break;
									case 465 :
										System.out.println(methodName + i);
									break;
									case 466 :
										System.out.println(methodName + i);
									break;
									case 467 :
										System.out.println(methodName + i);
									break;
									case 468 :
										System.out.println(methodName + i);
									break;
									case 469 :
										System.out.println(methodName + i);
									break;
									case 470 :
										System.out.println(methodName + i);
									break;
									case 471 :
										System.out.println(methodName + i);
									break;
									case 472 :
										System.out.println(methodName + i);
									break;
									case 473 :
										System.out.println(methodName + i);
									break;
									case 474 :
										System.out.println(methodName + i);
									break;
									case 475 :
										System.out.println(methodName + i);
									break;
									case 476 :
										System.out.println(methodName + i);
									break;
									case 477 :
										System.out.println(methodName + i);
									break;
									case 478 :
										System.out.println(methodName + i);
									break;
									case 479 :
										System.out.println(methodName + i);
									break;
									case 480 :
										System.out.println(methodName + i);
									break;
									case 481 :
										System.out.println(methodName + i);
									break;
									case 482 :
										System.out.println(methodName + i);
									break;
									case 483 :
										System.out.println(methodName + i);
									break;
									case 484 :
										System.out.println(methodName + i);
									break;
									case 485 :
										System.out.println(methodName + i);
									break;
									case 486 :
										System.out.println(methodName + i);
									break;
									case 487 :
										System.out.println(methodName + i);
									break;
									case 488 :
										System.out.println(methodName + i);
									break;
									case 489 :
										System.out.println(methodName + i);
									break;
									case 490 :
										System.out.println(methodName + i);
									break;
									case 491 :
										System.out.println(methodName + i);
									break;
									case 492 :
										System.out.println(methodName + i);
									break;
									case 493 :
										System.out.println(methodName + i);
									break;
									case 494 :
										System.out.println(methodName + i);
									break;
									case 495 :
										System.out.println(methodName + i);
									break;
									case 496 :
										System.out.println(methodName + i);
									break;
									case 497 :
										System.out.println(methodName + i);
									break;
									case 498 :
										System.out.println(methodName + i);
									break;
									case 499 :
										System.out.println(methodName + i);
									break;
									case 500 :
										System.out.println(methodName + i);
									break;
									case 501 :
										System.out.println(methodName + i);
									break;
									case 502 :
										System.out.println(methodName + i);
									break;
									case 503 :
										System.out.println(methodName + i);
									break;
									case 504 :
										System.out.println(methodName + i);
									break;
									case 505 :
										System.out.println(methodName + i);
									break;
									case 506 :
										System.out.println(methodName + i);
									break;
									case 507 :
										System.out.println(methodName + i);
									break;
									case 508 :
										System.out.println(methodName + i);
									break;
									case 509 :
										System.out.println(methodName + i);
									break;
									case 510 :
										System.out.println(methodName + i);
									break;
									case 511 :
										System.out.println(methodName + i);
									break;
									case 512 :
										System.out.println(methodName + i);
									break;
									case 513 :
										System.out.println(methodName + i);
									break;
									case 514 :
										System.out.println(methodName + i);
									break;
									case 515 :
										System.out.println(methodName + i);
									break;
									case 516 :
										System.out.println(methodName + i);
									break;
									case 517 :
										System.out.println(methodName + i);
									break;
									case 518 :
										System.out.println(methodName + i);
									break;
									case 519 :
										System.out.println(methodName + i);
									break;
									case 520 :
										System.out.println(methodName + i);
									break;
									case 521 :
										System.out.println(methodName + i);
									break;
									case 522 :
										System.out.println(methodName + i);
									break;
									case 523 :
										System.out.println(methodName + i);
									break;
									case 524 :
										System.out.println(methodName + i);
									break;
									case 525 :
										System.out.println(methodName + i);
									break;
									case 526 :
										System.out.println(methodName + i);
									break;
									case 527 :
										System.out.println(methodName + i);
									break;
									case 528 :
										System.out.println(methodName + i);
									break;
									case 529 :
										System.out.println(methodName + i);
									break;
									case 530 :
										System.out.println(methodName + i);
									break;
									case 531 :
										System.out.println(methodName + i);
									break;
									case 532 :
										System.out.println(methodName + i);
									break;
									case 533 :
										System.out.println(methodName + i);
									break;
									case 534 :
										System.out.println(methodName + i);
									break;
									case 535 :
										System.out.println(methodName + i);
									break;
									case 536 :
										System.out.println(methodName + i);
									break;
									case 537 :
										System.out.println(methodName + i);
									break;
									case 538 :
										System.out.println(methodName + i);
									break;
									case 539 :
										System.out.println(methodName + i);
									break;
									case 540 :
										System.out.println(methodName + i);
									break;
									case 541 :
										System.out.println(methodName + i);
									break;
									case 542 :
										System.out.println(methodName + i);
									break;
									case 543 :
										System.out.println(methodName + i);
									break;
									case 544 :
										System.out.println(methodName + i);
									break;
									case 545 :
										System.out.println(methodName + i);
									break;
									case 546 :
										System.out.println(methodName + i);
									break;
									case 547 :
										System.out.println(methodName + i);
									break;
									case 548 :
										System.out.println(methodName + i);
									break;
									case 549 :
										System.out.println(methodName + i);
									break;
									case 550 :
										System.out.println(methodName + i);
									break;
									case 551 :
										System.out.println(methodName + i);
									break;
									case 552 :
										System.out.println(methodName + i);
									break;
									case 553 :
										System.out.println(methodName + i);
									break;
									case 554 :
										System.out.println(methodName + i);
									break;
									case 555 :
										System.out.println(methodName + i);
									break;
									case 556 :
										System.out.println(methodName + i);
									break;
									case 557 :
										System.out.println(methodName + i);
									break;
									case 558 :
										System.out.println(methodName + i);
									break;
									case 559 :
										System.out.println(methodName + i);
									break;
									case 560 :
										System.out.println(methodName + i);
									break;
									case 561 :
										System.out.println(methodName + i);
									break;
									case 562 :
										System.out.println(methodName + i);
									break;
									case 563 :
										System.out.println(methodName + i);
									break;
									case 564 :
										System.out.println(methodName + i);
									break;
									case 565 :
										System.out.println(methodName + i);
									break;
									case 566 :
										System.out.println(methodName + i);
									break;
									case 567 :
										System.out.println(methodName + i);
									break;
									case 568 :
										System.out.println(methodName + i);
									break;
									case 569 :
										System.out.println(methodName + i);
									break;
									case 570 :
										System.out.println(methodName + i);
									break;
									case 571 :
										System.out.println(methodName + i);
									break;
									case 572 :
										System.out.println(methodName + i);
									break;
									case 573 :
										System.out.println(methodName + i);
									break;
									case 574 :
										System.out.println(methodName + i);
									break;
									case 575 :
										System.out.println(methodName + i);
									break;
									case 576 :
										System.out.println(methodName + i);
									break;
									case 577 :
										System.out.println(methodName + i);
									break;
									case 578 :
										System.out.println(methodName + i);
									break;
									case 579 :
										System.out.println(methodName + i);
									break;
									case 580 :
										System.out.println(methodName + i);
									break;
									case 581 :
										System.out.println(methodName + i);
									break;
									case 582 :
										System.out.println(methodName + i);
									break;
									case 583 :
										System.out.println(methodName + i);
									break;
									case 584 :
										System.out.println(methodName + i);
									break;
									case 585 :
										System.out.println(methodName + i);
									break;
									case 586 :
										System.out.println(methodName + i);
									break;
									case 587 :
										System.out.println(methodName + i);
									break;
									case 588 :
										System.out.println(methodName + i);
									break;
									case 589 :
										System.out.println(methodName + i);
									break;
									case 590 :
										System.out.println(methodName + i);
									break;
									case 591 :
										System.out.println(methodName + i);
									break;
									case 592 :
										System.out.println(methodName + i);
									break;
									case 593 :
										System.out.println(methodName + i);
									break;
									case 594 :
										System.out.println(methodName + i);
									break;
									case 595 :
										System.out.println(methodName + i);
									break;
									case 596 :
										System.out.println(methodName + i);
									break;
									case 597 :
										System.out.println(methodName + i);
									break;
									case 598 :
										System.out.println(methodName + i);
									break;
									case 599 :
										System.out.println(methodName + i);
									break;
									case 600 :
										System.out.println(methodName + i);
									break;
									case 601 :
										System.out.println(methodName + i);
									break;
									case 602 :
										System.out.println(methodName + i);
									break;
									case 603 :
										System.out.println(methodName + i);
									break;
									case 604 :
										System.out.println(methodName + i);
									break;
									case 605 :
										System.out.println(methodName + i);
									break;
									case 606 :
										System.out.println(methodName + i);
									break;
									case 607 :
										System.out.println(methodName + i);
									break;
									case 608 :
										System.out.println(methodName + i);
									break;
									case 609 :
										System.out.println(methodName + i);
									break;
									case 610 :
										System.out.println(methodName + i);
									break;
									case 611 :
										System.out.println(methodName + i);
									break;
									case 612 :
										System.out.println(methodName + i);
									break;
									case 613 :
										System.out.println(methodName + i);
									break;
									case 614 :
										System.out.println(methodName + i);
									break;
									case 615 :
										System.out.println(methodName + i);
									break;
									case 616 :
										System.out.println(methodName + i);
									break;
									case 617 :
										System.out.println(methodName + i);
									break;
									case 618 :
										System.out.println(methodName + i);
									break;
									case 619 :
										System.out.println(methodName + i);
									break;
									case 620 :
										System.out.println(methodName + i);
									break;
									case 621 :
										System.out.println(methodName + i);
									break;
									case 622 :
										System.out.println(methodName + i);
									break;
									case 623 :
										System.out.println(methodName + i);
									break;
									case 624 :
										System.out.println(methodName + i);
									break;
									case 625 :
										System.out.println(methodName + i);
									break;
									case 626 :
										System.out.println(methodName + i);
									break;
									case 627 :
										System.out.println(methodName + i);
									break;
									case 628 :
										System.out.println(methodName + i);
									break;
									case 629 :
										System.out.println(methodName + i);
									break;
									case 630 :
										System.out.println(methodName + i);
									break;
									case 631 :
										System.out.println(methodName + i);
									break;
									case 632 :
										System.out.println(methodName + i);
									break;
									case 633 :
										System.out.println(methodName + i);
									break;
									case 634 :
										System.out.println(methodName + i);
									break;
									case 635 :
										System.out.println(methodName + i);
									break;
									case 636 :
										System.out.println(methodName + i);
									break;
									case 637 :
										System.out.println(methodName + i);
									break;
									case 638 :
										System.out.println(methodName + i);
									break;
									case 639 :
										System.out.println(methodName + i);
									break;
									case 640 :
										System.out.println(methodName + i);
									break;
									case 641 :
										System.out.println(methodName + i);
									break;
									case 642 :
										System.out.println(methodName + i);
									break;
									case 643 :
										System.out.println(methodName + i);
									break;
									case 644 :
										System.out.println(methodName + i);
									break;
									case 645 :
										System.out.println(methodName + i);
									break;
									case 646 :
										System.out.println(methodName + i);
									break;
									case 647 :
										System.out.println(methodName + i);
									break;
									case 648 :
										System.out.println(methodName + i);
									break;
									case 649 :
										System.out.println(methodName + i);
									break;
									case 650 :
										System.out.println(methodName + i);
									break;
									case 651 :
										System.out.println(methodName + i);
									break;
									case 652 :
										System.out.println(methodName + i);
									break;
									case 653 :
										System.out.println(methodName + i);
									break;
									case 654 :
										System.out.println(methodName + i);
									break;
									case 655 :
										System.out.println(methodName + i);
									break;
									case 656 :
										System.out.println(methodName + i);
									break;
									case 657 :
										System.out.println(methodName + i);
									break;
									case 658 :
										System.out.println(methodName + i);
									break;
									case 659 :
										System.out.println(methodName + i);
									break;
									case 660 :
										System.out.println(methodName + i);
									break;
									case 661 :
										System.out.println(methodName + i);
									break;
									case 662 :
										System.out.println(methodName + i);
									break;
									case 663 :
										System.out.println(methodName + i);
									break;
									case 664 :
										System.out.println(methodName + i);
									break;
									case 665 :
										System.out.println(methodName + i);
									break;
									case 666 :
										System.out.println(methodName + i);
									break;
									case 667 :
										System.out.println(methodName + i);
									break;
									case 668 :
										System.out.println(methodName + i);
									break;
									case 669 :
										System.out.println(methodName + i);
									break;
									case 670 :
										System.out.println(methodName + i);
									break;
									case 671 :
										System.out.println(methodName + i);
									break;
									case 672 :
										System.out.println(methodName + i);
									break;
									case 673 :
										System.out.println(methodName + i);
									break;
									case 674 :
										System.out.println(methodName + i);
									break;
									case 675 :
										System.out.println(methodName + i);
									break;
									case 676 :
										System.out.println(methodName + i);
									break;
									case 677 :
										System.out.println(methodName + i);
									break;
									case 678 :
										System.out.println(methodName + i);
									break;
									case 679 :
										System.out.println(methodName + i);
									break;
									case 680 :
										System.out.println(methodName + i);
									break;
									case 681 :
										System.out.println(methodName + i);
									break;
									case 682 :
										System.out.println(methodName + i);
									break;
									case 683 :
										System.out.println(methodName + i);
									break;
									case 684 :
										System.out.println(methodName + i);
									break;
									case 685 :
										System.out.println(methodName + i);
									break;
									case 686 :
										System.out.println(methodName + i);
									break;
									case 687 :
										System.out.println(methodName + i);
									break;
									case 688 :
										System.out.println(methodName + i);
									break;
									case 689 :
										System.out.println(methodName + i);
									break;
									case 690 :
										System.out.println(methodName + i);
									break;
									case 691 :
										System.out.println(methodName + i);
									break;
									case 692 :
										System.out.println(methodName + i);
									break;
									case 693 :
										System.out.println(methodName + i);
									break;
									case 694 :
										System.out.println(methodName + i);
									break;
									case 695 :
										System.out.println(methodName + i);
									break;
									case 696 :
										System.out.println(methodName + i);
									break;
									case 697 :
										System.out.println(methodName + i);
									break;
									case 698 :
										System.out.println(methodName + i);
									break;
									case 699 :
										System.out.println(methodName + i);
									break;
									case 700 :
										System.out.println(methodName + i);
									break;
									case 701 :
										System.out.println(methodName + i);
									break;
									case 702 :
										System.out.println(methodName + i);
									break;
									case 703 :
										System.out.println(methodName + i);
									break;
									case 704 :
										System.out.println(methodName + i);
									break;
									case 705 :
										System.out.println(methodName + i);
									break;
									case 706 :
										System.out.println(methodName + i);
									break;
									case 707 :
										System.out.println(methodName + i);
									break;
									case 708 :
										System.out.println(methodName + i);
									break;
									case 709 :
										System.out.println(methodName + i);
									break;
									case 710 :
										System.out.println(methodName + i);
									break;
									case 711 :
										System.out.println(methodName + i);
									break;
									case 712 :
										System.out.println(methodName + i);
									break;
									case 713 :
										System.out.println(methodName + i);
									break;
									case 714 :
										System.out.println(methodName + i);
									break;
									case 715 :
										System.out.println(methodName + i);
									break;
									case 716 :
										System.out.println(methodName + i);
									break;
									case 717 :
										System.out.println(methodName + i);
									break;
									case 718 :
										System.out.println(methodName + i);
									break;
									case 719 :
										System.out.println(methodName + i);
									break;
									case 720 :
										System.out.println(methodName + i);
									break;
									case 721 :
										System.out.println(methodName + i);
									break;
									case 722 :
										System.out.println(methodName + i);
									break;
									case 723 :
										System.out.println(methodName + i);
									break;
									case 724 :
										System.out.println(methodName + i);
									break;
									case 725 :
										System.out.println(methodName + i);
									break;
									case 726 :
										System.out.println(methodName + i);
									break;
									case 727 :
										System.out.println(methodName + i);
									break;
									case 728 :
										System.out.println(methodName + i);
									break;
									case 729 :
										System.out.println(methodName + i);
									break;
									case 730 :
										System.out.println(methodName + i);
									break;
									case 731 :
										System.out.println(methodName + i);
									break;
									case 732 :
										System.out.println(methodName + i);
									break;
									case 733 :
										System.out.println(methodName + i);
									break;
									case 734 :
										System.out.println(methodName + i);
									break;
									case 735 :
										System.out.println(methodName + i);
									break;
									case 736 :
										System.out.println(methodName + i);
									break;
									case 737 :
										System.out.println(methodName + i);
									break;
									case 738 :
										System.out.println(methodName + i);
									break;
									case 739 :
										System.out.println(methodName + i);
									break;
									case 740 :
										System.out.println(methodName + i);
									break;
									case 741 :
										System.out.println(methodName + i);
									break;
									case 742 :
										System.out.println(methodName + i);
									break;
									case 743 :
										System.out.println(methodName + i);
									break;
									case 744 :
										System.out.println(methodName + i);
									break;
									case 745 :
										System.out.println(methodName + i);
									break;
									case 746 :
										System.out.println(methodName + i);
									break;
									case 747 :
										System.out.println(methodName + i);
									break;
									case 748 :
										System.out.println(methodName + i);
									break;
									case 749 :
										System.out.println(methodName + i);
									break;
									case 750 :
										System.out.println(methodName + i);
									break;
									case 751 :
										System.out.println(methodName + i);
									break;
									case 752 :
										System.out.println(methodName + i);
									break;
									case 753 :
										System.out.println(methodName + i);
									break;
									case 754 :
										System.out.println(methodName + i);
									break;
									case 755 :
										System.out.println(methodName + i);
									break;
									case 756 :
										System.out.println(methodName + i);
									break;
									case 757 :
										System.out.println(methodName + i);
									break;
									case 758 :
										System.out.println(methodName + i);
									break;
									case 759 :
										System.out.println(methodName + i);
									break;
									case 760 :
										System.out.println(methodName + i);
									break;
									case 761 :
										System.out.println(methodName + i);
									break;
									case 762 :
										System.out.println(methodName + i);
									break;
									case 763 :
										System.out.println(methodName + i);
									break;
									case 764 :
										System.out.println(methodName + i);
									break;
									case 765 :
										System.out.println(methodName + i);
									break;
									case 766 :
										System.out.println(methodName + i);
									break;
									case 767 :
										System.out.println(methodName + i);
									break;
									case 768 :
										System.out.println(methodName + i);
									break;
									case 769 :
										System.out.println(methodName + i);
									break;
									case 770 :
										System.out.println(methodName + i);
									break;
									case 771 :
										System.out.println(methodName + i);
									break;
									case 772 :
										System.out.println(methodName + i);
									break;
									case 773 :
										System.out.println(methodName + i);
									break;
									case 774 :
										System.out.println(methodName + i);
									break;
									case 775 :
										System.out.println(methodName + i);
									break;
									case 776 :
										System.out.println(methodName + i);
									break;
									case 777 :
										System.out.println(methodName + i);
									break;
									case 778 :
										System.out.println(methodName + i);
									break;
									case 779 :
										System.out.println(methodName + i);
									break;
									case 780 :
										System.out.println(methodName + i);
									break;
									case 781 :
										System.out.println(methodName + i);
									break;
									case 782 :
										System.out.println(methodName + i);
									break;
									case 783 :
										System.out.println(methodName + i);
									break;
									case 784 :
										System.out.println(methodName + i);
									break;
									case 785 :
										System.out.println(methodName + i);
									break;
									case 786 :
										System.out.println(methodName + i);
									break;
									case 787 :
										System.out.println(methodName + i);
									break;
									case 788 :
										System.out.println(methodName + i);
									break;
									case 789 :
										System.out.println(methodName + i);
									break;
									case 790 :
										System.out.println(methodName + i);
									break;
									case 791 :
										System.out.println(methodName + i);
									break;
									case 792 :
										System.out.println(methodName + i);
									break;
									case 793 :
										System.out.println(methodName + i);
									break;
									case 794 :
										System.out.println(methodName + i);
									break;
									case 795 :
										System.out.println(methodName + i);
									break;
									case 796 :
										System.out.println(methodName + i);
									break;
									case 797 :
										System.out.println(methodName + i);
									break;
									case 798 :
										System.out.println(methodName + i);
									break;
									case 799 :
										System.out.println(methodName + i);
									break;
									case 800 :
										System.out.println(methodName + i);
									break;
									case 801 :
										System.out.println(methodName + i);
									break;
									case 802 :
										System.out.println(methodName + i);
									break;
									case 803 :
										System.out.println(methodName + i);
									break;
									case 804 :
										System.out.println(methodName + i);
									break;
									case 805 :
										System.out.println(methodName + i);
									break;
									case 806 :
										System.out.println(methodName + i);
									break;
									case 807 :
										System.out.println(methodName + i);
									break;
									case 808 :
										System.out.println(methodName + i);
									break;
									case 809 :
										System.out.println(methodName + i);
									break;
									case 810 :
										System.out.println(methodName + i);
									break;
									case 811 :
										System.out.println(methodName + i);
									break;
									case 812 :
										System.out.println(methodName + i);
									break;
									case 813 :
										System.out.println(methodName + i);
									break;
									case 814 :
										System.out.println(methodName + i);
									break;
									case 815 :
										System.out.println(methodName + i);
									break;
									case 816 :
										System.out.println(methodName + i);
									break;
									case 817 :
										System.out.println(methodName + i);
									break;
									case 818 :
										System.out.println(methodName + i);
									break;
									case 819 :
										System.out.println(methodName + i);
									break;
									case 820 :
										System.out.println(methodName + i);
									break;
									case 821 :
										System.out.println(methodName + i);
									break;
									case 822 :
										System.out.println(methodName + i);
									break;
									case 823 :
										System.out.println(methodName + i);
									break;
									case 824 :
										System.out.println(methodName + i);
									break;
									case 825 :
										System.out.println(methodName + i);
									break;
									case 826 :
										System.out.println(methodName + i);
									break;
									case 827 :
										System.out.println(methodName + i);
									break;
									case 828 :
										System.out.println(methodName + i);
									break;
									case 829 :
										System.out.println(methodName + i);
									break;
									case 830 :
										System.out.println(methodName + i);
									break;
									case 831 :
										System.out.println(methodName + i);
									break;
									case 832 :
										System.out.println(methodName + i);
									break;
									case 833 :
										System.out.println(methodName + i);
									break;
									case 834 :
										System.out.println(methodName + i);
									break;
									case 835 :
										System.out.println(methodName + i);
									break;
									case 836 :
										System.out.println(methodName + i);
									break;
									case 837 :
										System.out.println(methodName + i);
									break;
									case 838 :
										System.out.println(methodName + i);
									break;
									case 839 :
										System.out.println(methodName + i);
									break;
									case 840 :
										System.out.println(methodName + i);
									break;
									case 841 :
										System.out.println(methodName + i);
									break;
									case 842 :
										System.out.println(methodName + i);
									break;
									case 843 :
										System.out.println(methodName + i);
									break;
									case 844 :
										System.out.println(methodName + i);
									break;
									case 845 :
										System.out.println(methodName + i);
									break;
									case 846 :
										System.out.println(methodName + i);
									break;
									case 847 :
										System.out.println(methodName + i);
									break;
									case 848 :
										System.out.println(methodName + i);
									break;
									case 849 :
										System.out.println(methodName + i);
									break;
									case 850 :
										System.out.println(methodName + i);
									break;
									case 851 :
										System.out.println(methodName + i);
									break;
									case 852 :
										System.out.println(methodName + i);
									break;
									case 853 :
										System.out.println(methodName + i);
									break;
									case 854 :
										System.out.println(methodName + i);
									break;
									case 855 :
										System.out.println(methodName + i);
									break;
									case 856 :
										System.out.println(methodName + i);
									break;
									case 857 :
										System.out.println(methodName + i);
									break;
									case 858 :
										System.out.println(methodName + i);
									break;
									case 859 :
										System.out.println(methodName + i);
									break;
									case 860 :
										System.out.println(methodName + i);
									break;
									case 861 :
										System.out.println(methodName + i);
									break;
									case 862 :
										System.out.println(methodName + i);
									break;
									case 863 :
										System.out.println(methodName + i);
									break;
									case 864 :
										System.out.println(methodName + i);
									break;
									case 865 :
										System.out.println(methodName + i);
									break;
									case 866 :
										System.out.println(methodName + i);
									break;
									case 867 :
										System.out.println(methodName + i);
									break;
									case 868 :
										System.out.println(methodName + i);
									break;
									case 869 :
										System.out.println(methodName + i);
									break;
									case 870 :
										System.out.println(methodName + i);
									break;
									case 871 :
										System.out.println(methodName + i);
									break;
									case 872 :
										System.out.println(methodName + i);
									break;
									case 873 :
										System.out.println(methodName + i);
									break;
									case 874 :
										System.out.println(methodName + i);
									break;
									case 875 :
										System.out.println(methodName + i);
									break;
									case 876 :
										System.out.println(methodName + i);
									break;
									case 877 :
										System.out.println(methodName + i);
									break;
									case 878 :
										System.out.println(methodName + i);
									break;
									case 879 :
										System.out.println(methodName + i);
									break;
									case 880 :
										System.out.println(methodName + i);
									break;
									case 881 :
										System.out.println(methodName + i);
									break;
									case 882 :
										System.out.println(methodName + i);
									break;
									case 883 :
										System.out.println(methodName + i);
									break;
									case 884 :
										System.out.println(methodName + i);
									break;
									case 885 :
										System.out.println(methodName + i);
									break;
									case 886 :
										System.out.println(methodName + i);
									break;
									case 887 :
										System.out.println(methodName + i);
									break;
									case 888 :
										System.out.println(methodName + i);
									break;
									case 889 :
										System.out.println(methodName + i);
									break;
									case 890 :
										System.out.println(methodName + i);
									break;
									case 891 :
										System.out.println(methodName + i);
									break;
									case 892 :
										System.out.println(methodName + i);
									break;
									case 893 :
										System.out.println(methodName + i);
									break;
									case 894 :
										System.out.println(methodName + i);
									break;
									case 895 :
										System.out.println(methodName + i);
									break;
									case 896 :
										System.out.println(methodName + i);
									break;
									case 897 :
										System.out.println(methodName + i);
									break;
									case 898 :
										System.out.println(methodName + i);
									break;
									case 899 :
										System.out.println(methodName + i);
									break;
									case 900 :
										System.out.println(methodName + i);
									break;
									case 901 :
										System.out.println(methodName + i);
									break;
									case 902 :
										System.out.println(methodName + i);
									break;
									case 903 :
										System.out.println(methodName + i);
									break;
									case 904 :
										System.out.println(methodName + i);
									break;
									case 905 :
										System.out.println(methodName + i);
									break;
									case 906 :
										System.out.println(methodName + i);
									break;
									case 907 :
										System.out.println(methodName + i);
									break;
									case 908 :
										System.out.println(methodName + i);
									break;
									case 909 :
										System.out.println(methodName + i);
									break;
									case 910 :
										System.out.println(methodName + i);
									break;
									case 911 :
										System.out.println(methodName + i);
									break;
									case 912 :
										System.out.println(methodName + i);
									break;
									case 913 :
										System.out.println(methodName + i);
									break;
									case 914 :
										System.out.println(methodName + i);
									break;
									case 915 :
										System.out.println(methodName + i);
									break;
									case 916 :
										System.out.println(methodName + i);
									break;
									case 917 :
										System.out.println(methodName + i);
									break;
									case 918 :
										System.out.println(methodName + i);
									break;
									case 919 :
										System.out.println(methodName + i);
									break;
									case 920 :
										System.out.println(methodName + i);
									break;
									case 921 :
										System.out.println(methodName + i);
									break;
									case 922 :
										System.out.println(methodName + i);
									break;
									case 923 :
										System.out.println(methodName + i);
									break;
									case 924 :
										System.out.println(methodName + i);
									break;
									case 925 :
										System.out.println(methodName + i);
									break;
									case 926 :
										System.out.println(methodName + i);
									break;
									case 927 :
										System.out.println(methodName + i);
									break;
									case 928 :
										System.out.println(methodName + i);
									break;
									case 929 :
										System.out.println(methodName + i);
									break;
									case 930 :
										System.out.println(methodName + i);
									break;
									case 931 :
										System.out.println(methodName + i);
									break;
									case 932 :
										System.out.println(methodName + i);
									break;
									case 933 :
										System.out.println(methodName + i);
									break;
									case 934 :
										System.out.println(methodName + i);
									break;
									case 935 :
										System.out.println(methodName + i);
									break;
									case 936 :
										System.out.println(methodName + i);
									break;
									case 937 :
										System.out.println(methodName + i);
									break;
									case 938 :
										System.out.println(methodName + i);
									break;
									case 939 :
										System.out.println(methodName + i);
									break;
									case 940 :
										System.out.println(methodName + i);
									break;
									case 941 :
										System.out.println(methodName + i);
									break;
									case 942 :
										System.out.println(methodName + i);
									break;
									case 943 :
										System.out.println(methodName + i);
									break;
									case 944 :
										System.out.println(methodName + i);
									break;
									case 945 :
										System.out.println(methodName + i);
									break;
									case 946 :
										System.out.println(methodName + i);
									break;
									case 947 :
										System.out.println(methodName + i);
									break;
									case 948 :
										System.out.println(methodName + i);
									break;
									case 949 :
										System.out.println(methodName + i);
									break;
									case 950 :
										System.out.println(methodName + i);
									break;
									case 951 :
										System.out.println(methodName + i);
									break;
									case 952 :
										System.out.println(methodName + i);
									break;
									case 953 :
										System.out.println(methodName + i);
									break;
									case 954 :
										System.out.println(methodName + i);
									break;
									case 955 :
										System.out.println(methodName + i);
									break;
									case 956 :
										System.out.println(methodName + i);
									break;
									case 957 :
										System.out.println(methodName + i);
									break;
									case 958 :
										System.out.println(methodName + i);
									break;
									case 959 :
										System.out.println(methodName + i);
									break;
									case 960 :
										System.out.println(methodName + i);
									break;
									case 961 :
										System.out.println(methodName + i);
									break;
									case 962 :
										System.out.println(methodName + i);
									break;
									case 963 :
										System.out.println(methodName + i);
									break;
									case 964 :
										System.out.println(methodName + i);
									break;
									case 965 :
										System.out.println(methodName + i);
									break;
									case 966 :
										System.out.println(methodName + i);
									break;
									case 967 :
										System.out.println(methodName + i);
									break;
									case 968 :
										System.out.println(methodName + i);
									break;
									case 969 :
										System.out.println(methodName + i);
									break;
									case 970 :
										System.out.println(methodName + i);
									break;
									case 971 :
										System.out.println(methodName + i);
									break;
									case 972 :
										System.out.println(methodName + i);
									break;
									case 973 :
										System.out.println(methodName + i);
									break;
									case 974 :
										System.out.println(methodName + i);
									break;
									case 975 :
										System.out.println(methodName + i);
									break;
									case 976 :
										System.out.println(methodName + i);
									break;
									case 977 :
										System.out.println(methodName + i);
									break;
									case 978 :
										System.out.println(methodName + i);
									break;
									case 979 :
										System.out.println(methodName + i);
									break;
									case 980 :
										System.out.println(methodName + i);
									break;
									case 981 :
										System.out.println(methodName + i);
									break;
									case 982 :
										System.out.println(methodName + i);
									break;
									case 983 :
										System.out.println(methodName + i);
									break;
									case 984 :
										System.out.println(methodName + i);
									break;
									case 985 :
										System.out.println(methodName + i);
									break;
									case 986 :
										System.out.println(methodName + i);
									break;
									case 987 :
										System.out.println(methodName + i);
									break;
									case 988 :
										System.out.println(methodName + i);
									break;
									case 989 :
										System.out.println(methodName + i);
									break;
									case 990 :
										System.out.println(methodName + i);
									break;
									case 991 :
										System.out.println(methodName + i);
									break;
									case 992 :
										System.out.println(methodName + i);
									break;
									case 993 :
										System.out.println(methodName + i);
									break;
									case 994 :
										System.out.println(methodName + i);
									break;
									case 995 :
										System.out.println(methodName + i);
									break;
									case 996 :
										System.out.println(methodName + i);
									break;
									case 997 :
										System.out.println(methodName + i);
									break;
									case 998 :
										System.out.println(methodName + i);
									break;
									case 999 :
										System.out.println(methodName + i);
									break;
									case 1000 :
										System.out.println(methodName + i);
									break;
									case 1001 :
										System.out.println(methodName + i);
									break;
									case 1002 :
										System.out.println(methodName + i);
									break;
									case 1003 :
										System.out.println(methodName + i);
									break;
									case 1004 :
										System.out.println(methodName + i);
									break;
									case 1005 :
										System.out.println(methodName + i);
									break;
									case 1006 :
										System.out.println(methodName + i);
									break;
									case 1007 :
										System.out.println(methodName + i);
									break;
									case 1008 :
										System.out.println(methodName + i);
									break;
									case 1009 :
										System.out.println(methodName + i);
									break;
									case 1010 :
										System.out.println(methodName + i);
									break;
									case 1011 :
										System.out.println(methodName + i);
									break;
									case 1012 :
										System.out.println(methodName + i);
									break;
									case 1013 :
										System.out.println(methodName + i);
									break;
									case 1014 :
										System.out.println(methodName + i);
									break;
									case 1015 :
										System.out.println(methodName + i);
									break;
									case 1016 :
										System.out.println(methodName + i);
									break;
									case 1017 :
										System.out.println(methodName + i);
									break;
									case 1018 :
										System.out.println(methodName + i);
									break;
									case 1019 :
										System.out.println(methodName + i);
									break;
									case 1020 :
										System.out.println(methodName + i);
									break;
									case 1021 :
										System.out.println(methodName + i);
									break;
									case 1022 :
										System.out.println(methodName + i);
									break;
									case 1023 :
										System.out.println(methodName + i);
									break;
									case 1024 :
										System.out.println(methodName + i);
									break;
									case 1025 :
										System.out.println(methodName + i);
									break;
									case 1026 :
										System.out.println(methodName + i);
									break;
									case 1027 :
										System.out.println(methodName + i);
									break;
									case 1028 :
										System.out.println(methodName + i);
									break;
									case 1029 :
										System.out.println(methodName + i);
									break;
									case 1030 :
										System.out.println(methodName + i);
									break;
									case 1031 :
										System.out.println(methodName + i);
									break;
									case 1032 :
										System.out.println(methodName + i);
									break;
									case 1033 :
										System.out.println(methodName + i);
									break;
									case 1034 :
										System.out.println(methodName + i);
									break;
									case 1035 :
										System.out.println(methodName + i);
									break;
									case 1036 :
										System.out.println(methodName + i);
									break;
									case 1037 :
										System.out.println(methodName + i);
									break;
									case 1038 :
										System.out.println(methodName + i);
									break;
									case 1039 :
										System.out.println(methodName + i);
									break;
									case 1040 :
										System.out.println(methodName + i);
									break;
									case 1041 :
										System.out.println(methodName + i);
									break;
									case 1042 :
										System.out.println(methodName + i);
									break;
									case 1043 :
										System.out.println(methodName + i);
									break;
									case 1044 :
										System.out.println(methodName + i);
									break;
									case 1045 :
										System.out.println(methodName + i);
									break;
									case 1046 :
										System.out.println(methodName + i);
									break;
									case 1047 :
										System.out.println(methodName + i);
									break;
									case 1048 :
										System.out.println(methodName + i);
									break;
									case 1049 :
										System.out.println(methodName + i);
									break;
									case 1050 :
										System.out.println(methodName + i);
									break;
									case 1051 :
										System.out.println(methodName + i);
									break;
									case 1052 :
										System.out.println(methodName + i);
									break;
									case 1053 :
										System.out.println(methodName + i);
									break;
									case 1054 :
										System.out.println(methodName + i);
									break;
									case 1055 :
										System.out.println(methodName + i);
									break;
									case 1056 :
										System.out.println(methodName + i);
									break;
									case 1057 :
										System.out.println(methodName + i);
									break;
									case 1058 :
										System.out.println(methodName + i);
									break;
									case 1059 :
										System.out.println(methodName + i);
									break;
									case 1060 :
										System.out.println(methodName + i);
									break;
									case 1061 :
										System.out.println(methodName + i);
									break;
									case 1062 :
										System.out.println(methodName + i);
									break;
									case 1063 :
										System.out.println(methodName + i);
									break;
									case 1064 :
										System.out.println(methodName + i);
									break;
									case 1065 :
										System.out.println(methodName + i);
									break;
									case 1066 :
										System.out.println(methodName + i);
									break;
									case 1067 :
										System.out.println(methodName + i);
									break;
									case 1068 :
										System.out.println(methodName + i);
									break;
									case 1069 :
										System.out.println(methodName + i);
									break;
									case 1070 :
										System.out.println(methodName + i);
									break;
									case 1071 :
										System.out.println(methodName + i);
									break;
									case 1072 :
										System.out.println(methodName + i);
									break;
									case 1073 :
										System.out.println(methodName + i);
									break;
									case 1074 :
										System.out.println(methodName + i);
									break;
									case 1075 :
										System.out.println(methodName + i);
									break;
									case 1076 :
										System.out.println(methodName + i);
									break;
									case 1077 :
										System.out.println(methodName + i);
									break;
									case 1078 :
										System.out.println(methodName + i);
									break;
									case 1079 :
										System.out.println(methodName + i);
									break;
									case 1080 :
										System.out.println(methodName + i);
									break;
									case 1081 :
										System.out.println(methodName + i);
									break;
									case 1082 :
										System.out.println(methodName + i);
									break;
									case 1083 :
										System.out.println(methodName + i);
									break;
									case 1084 :
										System.out.println(methodName + i);
									break;
									case 1085 :
										System.out.println(methodName + i);
									break;
									case 1086 :
										System.out.println(methodName + i);
									break;
									case 1087 :
										System.out.println(methodName + i);
									break;
									case 1088 :
										System.out.println(methodName + i);
									break;
									case 1089 :
										System.out.println(methodName + i);
									break;
									case 1090 :
										System.out.println(methodName + i);
									break;
									case 1091 :
										System.out.println(methodName + i);
									break;
									case 1092 :
										System.out.println(methodName + i);
									break;
									case 1093 :
										System.out.println(methodName + i);
									break;
									case 1094 :
										System.out.println(methodName + i);
									break;
									case 1095 :
										System.out.println(methodName + i);
									break;
									case 1096 :
										System.out.println(methodName + i);
									break;
									case 1097 :
										System.out.println(methodName + i);
									break;
									case 1098 :
										System.out.println(methodName + i);
									break;
									case 1099 :
										System.out.println(methodName + i);
									break;
									case 1100 :
										System.out.println(methodName + i);
									break;
									case 1101 :
										System.out.println(methodName + i);
									break;
									case 1102 :
										System.out.println(methodName + i);
									break;
									case 1103 :
										System.out.println(methodName + i);
									break;
									case 1104 :
										System.out.println(methodName + i);
									break;
									case 1105 :
										System.out.println(methodName + i);
									break;
									case 1106 :
										System.out.println(methodName + i);
									break;
									case 1107 :
										System.out.println(methodName + i);
									break;
									case 1108 :
										System.out.println(methodName + i);
									break;
									case 1109 :
										System.out.println(methodName + i);
									break;
									case 1110 :
										System.out.println(methodName + i);
									break;
									case 1111 :
										System.out.println(methodName + i);
									break;
									case 1112 :
										System.out.println(methodName + i);
									break;
									case 1113 :
										System.out.println(methodName + i);
									break;
									case 1114 :
										System.out.println(methodName + i);
									break;
									case 1115 :
										System.out.println(methodName + i);
									break;
									case 1116 :
										System.out.println(methodName + i);
									break;
									case 1117 :
										System.out.println(methodName + i);
									break;
									case 1118 :
										System.out.println(methodName + i);
									break;
									case 1119 :
										System.out.println(methodName + i);
									break;
									case 1120 :
										System.out.println(methodName + i);
									break;
									case 1121 :
										System.out.println(methodName + i);
									break;
									case 1122 :
										System.out.println(methodName + i);
									break;
									case 1123 :
										System.out.println(methodName + i);
									break;
									case 1124 :
										System.out.println(methodName + i);
									break;
									case 1125 :
										System.out.println(methodName + i);
									break;
									case 1126 :
										System.out.println(methodName + i);
									break;
									case 1127 :
										System.out.println(methodName + i);
									break;
									case 1128 :
										System.out.println(methodName + i);
									break;
									case 1129 :
										System.out.println(methodName + i);
									break;
								}
							} catch(Exception e) {
								e.printStackTrace();
							}
						}\
					}""",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=238923
	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						{
							for (boolean b : new boolean[] {}) {}
						}
						public X() {}
						public X(boolean b) {}
						public static void main(String[] args) {
							System.out.print("SUCCESS");
						}
					}""",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=237931
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public String[][] foo(String s) {
							return
								new String[][] { {" ", s != null ? s : "" },
									{" ", s != null ? s : "" },
									{" ", s != null ? s : "" },
									{" ", s != null ? s : "" } };
						}
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}""",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251539
	public void test040() throws Exception {
		this.runConformTest(
			new String[] {
				"I.java",
				"""
					public interface I {
					
						public Object foo();
					
						public static class B implements I {
							public Object foo() {
								return X.myI.foo();
							}
						}
					}""",
				"X.java",
				"""
					public class X {
						public static final I myI = new I.B() {
							int a = 0;
							int b = 1;
						};
					
						private Object bar2() {
							return null;
						}
						private Object bar() {
							Object o = bar2();
							if (o != null) {
								o.toString();
							}
							return null;
						}
					
					}""",
			},
		"");
		String expectedOutput =
			"  // Method descriptor #23 ()Ljava/lang/Object;\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  private java.lang.Object bar();\n" +
			"     0  aload_0 [this]\n" +
			"     1  " +
			(isMinimumCompliant(ClassFileConstants.JDK11) ? "invokevirtual" : "invokespecial") +
			" X.bar2() : java.lang.Object [25]\n" +
			"     4  astore_1 [o]\n" +
			"     5  aload_1 [o]\n" +
			"     6  ifnull 14\n" +
			"     9  aload_1 [o]\n" +
			"    10  invokevirtual java.lang.Object.toString() : java.lang.String [27]\n" +
			"    13  pop\n" +
			"    14  aconst_null\n" +
			"    15  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"        [pc: 5, line: 12]\n" +
			"        [pc: 9, line: 13]\n" +
			"        [pc: 14, line: 15]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: this index: 0 type: X\n" +
			"        [pc: 5, pc: 16] local: o index: 1 type: java.lang.Object\n" +
			"      Stack map table: number of frames 1\n" +
			"        [pc: 14, append: {java.lang.Object}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251539
	public void test041() throws Exception {
		this.runConformTest(
			new String[] {
				"I.java",
				"""
					public interface I {
					
						public Object foo();
					
						public static class B implements I {
							public Object foo() {
								return String.valueOf(X.myI.foo()) + String.valueOf(X.myU.foo());
							}
						}
					}""",
				"X.java",
				"""
					public class X {
						public static final I myI = new I.B() {
							int a = 0;
							int b = 1;
						};
						public static final I myU = new I.B() {
							int a = 0;
							int b = 1;
							int c = 2;
						};
						private Object bar2() {
							return null;
						}
						private Object bar() {
							Object o = bar2();
							if (o != null) {
								o.toString();
							}
							return null;
						}
					}""",
			},
		"");

		String expectedOutput =
			"  // Method descriptor #29 ()Ljava/lang/Object;\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  private java.lang.Object bar();\n" +
			"     0  aload_0 [this]\n" +
			"     1  " +
			(isMinimumCompliant(ClassFileConstants.JDK11) ? "invokevirtual" : "invokespecial") +
			" X.bar2() : java.lang.Object [31]\n" +
			"     4  astore_1 [o]\n" +
			"     5  aload_1 [o]\n" +
			"     6  ifnull 14\n" +
			"     9  aload_1 [o]\n" +
			"    10  invokevirtual java.lang.Object.toString() : java.lang.String [33]\n" +
			"    13  pop\n" +
			"    14  aconst_null\n" +
			"    15  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 15]\n" +
			"        [pc: 5, line: 16]\n" +
			"        [pc: 9, line: 17]\n" +
			"        [pc: 14, line: 19]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: this index: 0 type: X\n" +
			"        [pc: 5, pc: 16] local: o index: 1 type: java.lang.Object\n" +
			"      Stack map table: number of frames 1\n" +
			"        [pc: 14, append: {java.lang.Object}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260031
	public void test042() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						private static void foo(Class<?> c, int n) { }
						public static void main(String... args) {
							foo(Integer.class, (args == null ? -1 : 1));
						}
					}""",
			},
		"");

		String expectedOutput =
			"""
			  // Stack: 2, Locals: 1
			  public static void main(java.lang.String... args);
			     0  ldc <Class java.lang.Integer> [26]
			     2  aload_0 [args]
			     3  ifnonnull 10
			     6  iconst_m1
			     7  goto 11
			    10  iconst_1
			    11  invokestatic X.foo(java.lang.Class, int) : void [28]
			    14  return
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 14, line: 5]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			      Stack map table: number of frames 2
			        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Class}]
			        [pc: 11, full, stack: {java.lang.Class, int}, locals: {java.lang.String[]}]
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=279183
	public void test043() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println("ok");
						}
						private static int willNotVerify() {
							int limit = 100;
							int match;
							int result = 200;
							do {
								if (limit > 0) {
									continue;
								}
								match = 0;
								while (++match < 100) {
									System.out.println();
								}
							} while (--limit != 0);
							return result;
						}
					}""",
			},
		"ok");

		String expectedOutput =
			"""
			  // Method descriptor #33 ()I
			  // Stack: 2, Locals: 3
			  private static int willNotVerify();
			     0  bipush 100
			     2  istore_0 [limit]
			     3  sipush 200
			     6  istore_2 [result]
			     7  iload_0 [limit]
			     8  ifle 14
			    11  goto 34
			    14  iconst_0
			    15  istore_1 [match]
			    16  goto 25
			    19  getstatic java.lang.System.out : java.io.PrintStream [16]
			    22  invokevirtual java.io.PrintStream.println() : void [34]
			    25  iinc 1 1 [match]
			    28  iload_1 [match]
			    29  bipush 100
			    31  if_icmplt 19
			    34  iinc 0 -1 [limit]
			    37  iload_0 [limit]
			    38  ifne 7
			    41  iload_2 [result]
			    42  ireturn
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 3, line: 8]
			        [pc: 7, line: 10]
			        [pc: 11, line: 11]
			        [pc: 14, line: 13]
			        [pc: 16, line: 14]
			        [pc: 19, line: 15]
			        [pc: 25, line: 14]
			        [pc: 34, line: 17]
			        [pc: 41, line: 18]
			      Local variable table:
			        [pc: 3, pc: 43] local: limit index: 0 type: int
			        [pc: 16, pc: 34] local: match index: 1 type: int
			        [pc: 7, pc: 43] local: result index: 2 type: int
			      Stack map table: number of frames 5
			        [pc: 7, full, stack: {}, locals: {int, _, int}]
			        [pc: 14, same]
			        [pc: 19, full, stack: {}, locals: {int, int, int}]
			        [pc: 25, same]
			        [pc: 34, full, stack: {}, locals: {int, _, int}]
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=279183
	public void test044() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println("ok");
						}
						private static int willNotVerify() {
							int limit = 100;
							int match;
							int result = 200;
							do {
								if (limit > 0) {
									continue;
								}
								match = 0;
								while (++match < 100) {
									// empty
								}
							} while (--limit != 0);
							return result;
						}
					}""",
			},
		"ok");

		String expectedOutput =
			"""
			  // Method descriptor #33 ()I
			  // Stack: 2, Locals: 3
			  private static int willNotVerify();
			     0  bipush 100
			     2  istore_0 [limit]
			     3  sipush 200
			     6  istore_2 [result]
			     7  iload_0 [limit]
			     8  ifle 14
			    11  goto 25
			    14  iconst_0
			    15  istore_1 [match]
			    16  iinc 1 1 [match]
			    19  iload_1 [match]
			    20  bipush 100
			    22  if_icmplt 16
			    25  iinc 0 -1 [limit]
			    28  iload_0 [limit]
			    29  ifne 7
			    32  iload_2 [result]
			    33  ireturn
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 3, line: 8]
			        [pc: 7, line: 10]
			        [pc: 11, line: 11]
			        [pc: 14, line: 13]
			        [pc: 16, line: 14]
			        [pc: 25, line: 17]
			        [pc: 32, line: 18]
			      Local variable table:
			        [pc: 3, pc: 34] local: limit index: 0 type: int
			        [pc: 16, pc: 25] local: match index: 1 type: int
			        [pc: 7, pc: 34] local: result index: 2 type: int
			      Stack map table: number of frames 4
			        [pc: 7, full, stack: {}, locals: {int, _, int}]
			        [pc: 14, same]
			        [pc: 16, full, stack: {}, locals: {int, int, int}]
			        [pc: 25, full, stack: {}, locals: {int, _, int}]
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=279183
	public void test045() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String args[]) {
							int i;
							do {
							} while ((i = 2) < 0);
							if (i != 2) {
								System.out.println("FAILED");
							} else {
								System.out.println("SUCCESS");
							}
						}
					}""",
			},
		"SUCCESS");

		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_2
			     1  dup
			     2  istore_1 [i]
			     3  iflt 0
			     6  iload_1 [i]
			     7  iconst_2
			     8  if_icmpeq 22
			    11  getstatic java.lang.System.out : java.io.PrintStream [16]
			    14  ldc <String "FAILED"> [22]
			    16  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
			    19  goto 30
			    22  getstatic java.lang.System.out : java.io.PrintStream [16]
			    25  ldc <String "SUCCESS"> [30]
			    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
			    30  return
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 3, line: 4]
			        [pc: 6, line: 6]
			        [pc: 11, line: 7]
			        [pc: 19, line: 8]
			        [pc: 22, line: 9]
			        [pc: 30, line: 11]
			      Local variable table:
			        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 31] local: i index: 1 type: int
			      Stack map table: number of frames 3
			        [pc: 0, same]
			        [pc: 22, append: {int}]
			        [pc: 30, same]
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// 298250
	public void test046() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	class E1 extends RuntimeException {\n" +
				"		private static final long serialVersionUID = 1L;\n" +
				"	}\n" +
				"	static Object bar() {\n" +
				"		return new Object() {\n" +
				"			public void foo() {\n" +
				"				if (condition())\n" +
				"					throw new E1();\n" +
				"			}\n" +
				"		};\n" +
				"	}\n" +
				"	static boolean condition() {\n" +
				"		return false;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					throw new E1();
					      ^^^^^^^^
				No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).
				----------
				""",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324848
	public void test047() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void test() {
							final boolean x = true;
							new Runnable() {
								@Override
								public void run() {
									synchronized (X.this) {
										System.out.println(x);
									}
								}
							};
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					synchronized (X.this) {
					              ^^^^^^
				No enclosing instance of the type X is accessible in scope
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=351653
	public void test048() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] p) {
					        int i;
					        try {
					          if (p == null || p == null)
					            return;
					          i = 0;
					        } finally {
					            i = 0;
					        }
					    }
					}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=351653
	public void test049() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					import java.io.InputStream;
					
					public class X implements Runnable {
					
						private boolean contentEquals(final String src, final String tar)
								throws IOException {
							if (src == null && tar == null) {
								return true;
							}
							if (!isFile(src) || !isFile(tar))
								throw new IOException("cannot compare non-files");
							if (size(src) != size(tar))
								return false;
							final byte[] baSrc = new byte[8192];
							final byte[] baTar = new byte[baSrc.length];
							int lrSrc;
							int lrTar;
							InputStream isSrc = null;
							InputStream isTar = null;
							try {
								isSrc = newInputStream(src);
								if (isSrc == null)
									return false;
								isTar = newInputStream(tar);
								if (isTar == null)
									return false;
								do {
									lrSrc = isSrc.read(baSrc);
									lrTar = isTar.read(baTar);
									if (lrSrc != lrTar)
										return false;
									for (int i = 0; i < lrSrc; i++)
										if (baSrc[i] != baTar[i])
											return false;
								} while ((lrSrc >= 0) && (lrSrc == lrTar));
							} finally {
								try {
									close(isSrc);
								} finally {
									close(isTar);
								}
							}
							return true;
						}
						private void close(final InputStream isSrc) {
						}
						private boolean isFile(final String src) {
							return false;
						}
						public void run() {
							try {
								System.out.println(contentEquals(null, null));
							} catch (final IOException e) {
								e.printStackTrace();
							}
						}
						static InputStream newInputStream(String path) {
							return null;
						}
						static int size(String path) {
							return 0;
						}
						public static void main(final String[] args) {
							new X().run();
						}
					}"""
			},
			"true");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=352145
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.File;
					import java.io.FileFilter;
					import java.io.FileNotFoundException;
					import java.io.IOException;
					import java.util.ArrayList;
					import java.util.List;
					
					public class X {
					
						public static final List<File> copyDir(List<File> lf,
								final boolean overwrite, final boolean recursive,
								final boolean returnSrc, final File src, final File tar,
								final FileFilter filter) throws IOException {
							if (!src.isDirectory())
								throw new FileNotFoundException("not a directory: " + src);
							if (!tar.isDirectory())
								throw new FileNotFoundException("not a directory: " + tar);
							final File[] fa = src.listFiles();
							if (fa == null)
								throw new FileNotFoundException("directory not accessible: " + src);
							if (lf == null)
								lf = new ArrayList<File>(fa.length);
							for (final File f : fa) {
								final File right = new File(tar, f.getName());
								if (f.isDirectory()) {
									if (recursive && filter.accept(f)) {
										if (!right.exists())
											right.mkdir();
										copyDir(lf, overwrite, recursive, returnSrc, f, right,
												filter);
									}
								} else {
									if (overwrite || (!right.exists() && filter.accept(f))) {
										lf.add(returnSrc ? f : right);
									}
								}
							}
							return lf;
						}
					
						public static void main(final String[] args) {
							System.out.println("SUCCESS");
						}
					
					}"""
			},
			"SUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=352145
	public void test051() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.File;
					import java.io.IOException;
					import java.util.List;
					
					public class X {
					
						public static final List<File> copyDir(int j, List<File> lf,
								final boolean returnSrc, final File[] fa) throws IOException {
							if (lf == null)
								lf = null;
							for (int i = 0, max = fa.length; i < max; i++) {
								final File f = fa[i];
								final File right = new File(f.getName());
								if (f.isDirectory()) {
								} else {
									lf.add(returnSrc ? f : right);
								}
							}
							return lf;
						}
					
						public static void main(final String[] args) {
							System.out.println("SUCCESS");
						}
					
					}"""
			},
			"SUCCESS");
	}
	public void test052() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						X(int i) {}
						void foo() {}
						public static void main(String[] args) {
							new X(args.length == 2 ? 1 : 2).foo();
							System.out.println("SUCCESS");
						}
					}""",
			},
			"SUCCESS");
	}
	// 352665
	public void test053() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		customOptions.put(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION, JavaCore.IGNORE);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static {
							for(int i = 0; i < 10; i++){
								A a = new A();
								a.foo();
							}
						}
						private class A {
							private A() {
							}
							void foo() {}
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					A a = new A();
					      ^^^^^^^
				No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).
				----------
				""",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354052
	public void test054() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					public static void foo() {
					     X z;
					     while ((z = getObject()) != null) {
					         z.bar();
					     }
						  System.out.println("SUCCESS");
					 }
					 public void bar() {}
					 public static X getObject() {
					     return null;
					 }
					   public static void main(String[] args) {
					       new X().foo();
					   }
					}""",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=359495
	public void testBug359495a() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.List;
						import java.util.concurrent.locks.Lock;
						import java.util.Arrays;
						import java.util.concurrent.locks.ReentrantLock;
						public class X {
							public static void main(String[] args) {
								final Lock lock = new ReentrantLock();
								final List<String> strings = Arrays.asList(args);
								lock.lock();
								try{
									for (final String string:strings){
										return;
									}
									return;
								} finally {
									lock.unlock();
								}\
							}
						}""",
				},
				"");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 6
				  public static void main(java.lang.String[] args);
				     0  new java.util.concurrent.locks.ReentrantLock [16]
				     3  dup
				     4  invokespecial java.util.concurrent.locks.ReentrantLock() [18]
				     7  astore_1 [lock]
				     8  aload_0 [args]
				     9  invokestatic java.util.Arrays.asList(java.lang.Object[]) : java.util.List [19]
				    12  astore_2 [strings]
				    13  aload_1 [lock]
				    14  invokeinterface java.util.concurrent.locks.Lock.lock() : void [25] [nargs: 1]
				    19  aload_2 [strings]
				    20  invokeinterface java.util.List.iterator() : java.util.Iterator [30] [nargs: 1]
				    25  astore 4
				    27  aload 4
				    29  invokeinterface java.util.Iterator.hasNext() : boolean [36] [nargs: 1]
				    34  ifeq 55
				    37  aload 4
				    39  invokeinterface java.util.Iterator.next() : java.lang.Object [42] [nargs: 1]
				    44  checkcast java.lang.String [46]
				    47  astore_3 [string]
				    48  aload_1 [lock]
				    49  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [48] [nargs: 1]
				    54  return
				    55  aload_1 [lock]
				    56  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [48] [nargs: 1]
				    61  return
				    62  astore 5
				    64  aload_1 [lock]
				    65  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [48] [nargs: 1]
				    70  aload 5
				    72  athrow
				      Exception Table:
				        [pc: 19, pc: 48] -> 62 when : any
				      Line numbers:
				        [pc: 0, line: 7]
				        [pc: 8, line: 8]
				        [pc: 13, line: 9]
				        [pc: 19, line: 11]
				        [pc: 48, line: 16]
				        [pc: 54, line: 12]
				        [pc: 55, line: 16]
				        [pc: 61, line: 14]
				        [pc: 62, line: 15]
				        [pc: 64, line: 16]
				        [pc: 70, line: 17]
				      Local variable table:
				        [pc: 0, pc: 73] local: args index: 0 type: java.lang.String[]
				        [pc: 8, pc: 73] local: lock index: 1 type: java.util.concurrent.locks.Lock
				        [pc: 13, pc: 73] local: strings index: 2 type: java.util.List
				        [pc: 48, pc: 55] local: string index: 3 type: java.lang.String
				      Local variable type table:
				        [pc: 13, pc: 73] local: strings index: 2 type: java.util.List<java.lang.String>
				      Stack map table: number of frames 2
				        [pc: 55, append: {java.util.concurrent.locks.Lock, java.util.List}]
				        [pc: 62, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				""" ;

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=359495
	public void testBug359495b() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.List;
						import java.util.Iterator;
						import java.util.concurrent.locks.Lock;
						import java.util.Arrays;
						import java.util.concurrent.locks.ReentrantLock;
						public class X {
							public static void main(String[] args) {
								final Lock lock = new ReentrantLock();
								final List<String> strings = Arrays.asList(args);
								lock.lock();
								try{
									for (Iterator i = strings.iterator(); i.hasNext();){
										return;
									}
									return;
								} finally {
									lock.unlock();
								}\
							}
						}""",
				},
				"");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 5
				  public static void main(java.lang.String[] args);
				     0  new java.util.concurrent.locks.ReentrantLock [16]
				     3  dup
				     4  invokespecial java.util.concurrent.locks.ReentrantLock() [18]
				     7  astore_1 [lock]
				     8  aload_0 [args]
				     9  invokestatic java.util.Arrays.asList(java.lang.Object[]) : java.util.List [19]
				    12  astore_2 [strings]
				    13  aload_1 [lock]
				    14  invokeinterface java.util.concurrent.locks.Lock.lock() : void [25] [nargs: 1]
				    19  aload_2 [strings]
				    20  invokeinterface java.util.List.iterator() : java.util.Iterator [30] [nargs: 1]
				    25  astore_3 [i]
				    26  aload_3 [i]
				    27  invokeinterface java.util.Iterator.hasNext() : boolean [36] [nargs: 1]
				    32  ifeq 42
				    35  aload_1 [lock]
				    36  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [42] [nargs: 1]
				    41  return
				    42  aload_1 [lock]
				    43  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [42] [nargs: 1]
				    48  return
				    49  astore 4
				    51  aload_1 [lock]
				    52  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [42] [nargs: 1]
				    57  aload 4
				    59  athrow
				      Exception Table:
				        [pc: 19, pc: 35] -> 49 when : any
				      Line numbers:
				        [pc: 0, line: 8]
				        [pc: 8, line: 9]
				        [pc: 13, line: 10]
				        [pc: 19, line: 12]
				        [pc: 35, line: 17]
				        [pc: 41, line: 13]
				        [pc: 42, line: 17]
				        [pc: 48, line: 15]
				        [pc: 49, line: 16]
				        [pc: 51, line: 17]
				        [pc: 57, line: 18]
				      Local variable table:
				        [pc: 0, pc: 60] local: args index: 0 type: java.lang.String[]
				        [pc: 8, pc: 60] local: lock index: 1 type: java.util.concurrent.locks.Lock
				        [pc: 13, pc: 60] local: strings index: 2 type: java.util.List
				        [pc: 26, pc: 42] local: i index: 3 type: java.util.Iterator
				      Local variable type table:
				        [pc: 13, pc: 60] local: strings index: 2 type: java.util.List<java.lang.String>
				      Stack map table: number of frames 2
				        [pc: 42, append: {java.util.concurrent.locks.Lock, java.util.List}]
				        [pc: 49, same_locals_1_stack_item, stack: {java.lang.Throwable}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=362591
	public void test055() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								testError(3, 4, "d");
							}
							public static void testError(Number n0, Number n1, String refValue) {
								Number result = refValue.equals("ttt") ? n0 : (n1 == null ? null : n1.intValue());
								System.out.println(String.valueOf(result));
							}
						}""",
				},
				"4");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"""
				  // Method descriptor #27 (Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/String;)V
				  // Stack: 2, Locals: 4
				  public static void testError(java.lang.Number n0, java.lang.Number n1, java.lang.String refValue);
				     0  aload_2 [refValue]
				     1  ldc <String "ttt"> [30]
				     3  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [32]
				     6  ifeq 13
				     9  aload_0 [n0]
				    10  goto 28
				    13  aload_1 [n1]
				    14  ifnonnull 21
				    17  aconst_null
				    18  goto 28
				    21  aload_1 [n1]
				    22  invokevirtual java.lang.Number.intValue() : int [38]
				    25  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [16]
				    28  astore_3 [result]
				    29  getstatic java.lang.System.out : java.io.PrintStream [44]
				    32  aload_3 [result]
				    33  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [50]
				    36  invokevirtual java.io.PrintStream.println(java.lang.String) : void [53]
				    39  return
				      Line numbers:
				        [pc: 0, line: 6]
				        [pc: 29, line: 7]
				        [pc: 39, line: 8]
				      Local variable table:
				        [pc: 0, pc: 40] local: n0 index: 0 type: java.lang.Number
				        [pc: 0, pc: 40] local: n1 index: 1 type: java.lang.Number
				        [pc: 0, pc: 40] local: refValue index: 2 type: java.lang.String
				        [pc: 29, pc: 40] local: result index: 3 type: java.lang.Number
				      Stack map table: number of frames 3
				        [pc: 13, same]
				        [pc: 21, same]
				        [pc: 28, same_locals_1_stack_item, stack: {java.lang.Number}]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test055a() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String[] args) {
						        Object o = args != null ? args : (args == null ? null : args.length);
						    }
						}
						""",
				},
				"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=366999
	public void test056() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.io.BufferedReader;
						import java.io.Closeable;
						import java.io.File;
						import java.io.FileReader;
						import java.io.IOException;
						
						public class X {
						
							static class C implements Closeable {
								@Override
								public void close() throws IOException {
									//
								}
							}
						
							int run() throws IOException {
								int lcnt = 0;
								try (C c = new C();) {
									try (final BufferedReader br = new BufferedReader(new FileReader(
											new File("logging.properties")))) {
										String s = null;
										while ((s = br.readLine()) != null)
											lcnt++;
										return lcnt;
									}
								} finally {
									System.out.println("read " + lcnt + " lines");
								}
							}
						
							public static void main(final String[] args) throws IOException {
								System.out.println("SUCCESS");
							}
						}""",
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test057() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            for (;;) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						""",
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test058() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            for (;true;) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test059() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            for (;false;) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
					"""
						----------
						1. WARNING in X.java (at line 4)
							label1: do {
							^^^^^^
						The label label1 is never explicitly referenced
						----------
						2. ERROR in X.java (at line 5)
							for (;false;) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
							              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						Unreachable code
						----------
						3. ERROR in X.java (at line 10)
							} while (s != null);
							         ^
						The local variable s may not have been initialized
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test060() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            for (; 5 < 10;) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test061() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        int five = 5, ten = 10;
						        String s;
						        label1: do {
						            for (; five < ten;) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
					"""
						----------
						1. WARNING in X.java (at line 9)
							continue label1;
							^^^^^^^^^^^^^^^^
						Dead code
						----------
						2. ERROR in X.java (at line 11)
							} while (s != null);
							         ^
						The local variable s may not have been initialized
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test062() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public void run() {
					        final int five = 5, ten = 10;
					        String s;
					        label1: do {
					            for (; five < ten;) {
					                s = "";
					                if (s == null)\s
					                    continue label1;
					            }
					        } while (s != null);
					}
					    public static void main(String [] args) {
							System.out.println("SUCCESS");
					    }
					}
					"""				},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test063() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public void run() {
					        final int five = 5, ten = 10;
					        String s;
					        label1: do {
					            for (; five > ten;) {
					                s = "";
					                if (s == null)\s
					                    continue label1;
					            }
					        } while (s != null);
					}
					    public static void main(String [] args) {
							System.out.println("SUCCESS");
					    }
					}
					"""				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						label1: do {
						^^^^^^
					The label label1 is never explicitly referenced
					----------
					2. ERROR in X.java (at line 6)
						for (; five > ten;) {
					                s = "";
					                if (s == null)\s
					                    continue label1;
					            }
						                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unreachable code
					----------
					3. ERROR in X.java (at line 11)
						} while (s != null);
						         ^
					The local variable s may not have been initialized
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test064() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            while (true) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						""",
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test065() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            while (false) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
					"""
						----------
						1. WARNING in X.java (at line 4)
							label1: do {
							^^^^^^
						The label label1 is never explicitly referenced
						----------
						2. ERROR in X.java (at line 5)
							while (false) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
							              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						Unreachable code
						----------
						3. ERROR in X.java (at line 10)
							} while (s != null);
							         ^
						The local variable s may not have been initialized
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test066() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        String s;
						        label1: do {
						            while(5 < 10) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test067() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void run() {
						        int five = 5, ten = 10;
						        String s;
						        label1: do {
						            while (five < ten) {
						                s = "";
						                if (s == null)\s
						                    continue label1;
						            }
						        } while (s != null);
						}
						    public static void main(String [] args) {
								System.out.println("SUCCESS");
						    }
						}
						"""				},
					"""
						----------
						1. WARNING in X.java (at line 9)
							continue label1;
							^^^^^^^^^^^^^^^^
						Dead code
						----------
						2. ERROR in X.java (at line 11)
							} while (s != null);
							         ^
						The local variable s may not have been initialized
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test068() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public void run() {
					        final int five = 5, ten = 10;
					        String s;
					        label1: do {
					            while (five < ten) {
					                s = "";
					                if (s == null)\s
					                    continue label1;
					            }
					        } while (s != null);
					}
					    public static void main(String [] args) {
							System.out.println("SUCCESS");
					    }
					}
					"""				},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test069() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public void run() {
					        final int five = 5, ten = 10;
					        String s;
					        label1: do {
					            while (five > ten) {
					                s = "";
					                if (s == null)\s
					                    continue label1;
					            }
					        } while (s != null);
					}
					    public static void main(String [] args) {
							System.out.println("SUCCESS");
					    }
					}
					"""				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						label1: do {
						^^^^^^
					The label label1 is never explicitly referenced
					----------
					2. ERROR in X.java (at line 6)
						while (five > ten) {
					                s = "";
					                if (s == null)\s
					                    continue label1;
					            }
						                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unreachable code
					----------
					3. ERROR in X.java (at line 11)
						} while (s != null);
						         ^
					The local variable s may not have been initialized
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test070() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Arrays;
					import java.util.Iterator;
					import java.util.List;
					import java.util.Properties;
					import org.w3c.dom.*;
					public class X extends Object {
					        public static void main(String [] args) {
					            System.out.println ("SUCCESS");
					        }
						private static class Handler extends Object {
							public int getStuff() {
								return 1;
							}
							public void handle(Element element) {
								Properties properties = new Properties();
								NamedNodeMap atts = element.getAttributes();
								if (atts != null) {
									for (int a = 0; a < atts.getLength(); a++) {
										Node att = atts.item(a);
										String name = att.getNodeName();
										String value = att.getNodeValue();
										if ("foo".equals(name)) {
											name = value;
										} else {
											if (!"bar".equals(name))
												continue;
											name = value;
										}
										properties.put(name, value);
									}
								}
								label0: do {
									Node node;
									String nodeName;
									label1: do {
										for (Iterator i = (new ArrayList(1)).iterator(); i
												.hasNext(); members.add(equals(node))) {
											node = (Node) i.next();
											nodeName = "" + equals(node.getNodeName());
											if (!"foo".equals(nodeName))
												continue label1;
										}
										break label0;
									} while (!"bar".equals(nodeName));
									Iterator i = (new ArrayList(1)).iterator();
									while (i.hasNext()) {
										Node n = (Node) i.next();
										String name = toString() + n.getNodeName();
										if ("wtf".equals(name)) {
											String propertyName = (toString() + n.getAttributes()
													.getNamedItem("broken")).trim();
											String value = toString() + n;
											properties.put(propertyName, value);
										}
									}
								} while (true);
								propertiesBuilder.equals(properties);
								builder.equals(propertiesBuilder.hashCode());
								builder.equals(members);
							}
							private final Object c;
							private Object builder;
							private List members;
							private Object propertiesBuilder;
							public Handler(Object c) {
								this.c = c;
								builder = Arrays.asList(Object.class);
								builder.equals("foo");
								builder.equals("bar");
								members = new ArrayList();
								propertiesBuilder = Arrays.asList(Object.class);
								Object beanDefinition = propertiesBuilder.toString();
								Object holder = new String("stirng");
								Arrays.asList(holder, c.toString());
							}
						}
						public X() {
						}
						protected Object parseInternal(Element element, Object c) {
							Handler h = new Handler(c);
							h.handle(element);
							return h.getStuff();
						}
					}
					"""
				},
				"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
	// Verify the generated code does not have same branch target for the 2 return statements
	public void testBug380313() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							public void foo() throws Exception {
							        int i = 1;
							        try {
							            if (i == 1) {
							                int n = bar();
							                if (n == 35)
							                   return;
							            } else {
							                throw new Exception();
							            }
							            if (i == 0)
							               return;
							        } finally {
							            bar();
							        }
							    }
							
							    private int bar() {
							        return 0;
							    }
							
							    public static void main(String[] args) {
									System.out.println("SUCCESS");
							    }
							}
							"""
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String xBarCall = isMinimumCompliant(ClassFileConstants.JDK11) ?
					"invokevirtual X.bar() : int [18]\n" : "invokespecial X.bar() : int [18]\n";
			String expectedOutput =
					"  // Method descriptor #6 ()V\n" +
					"  // Stack: 2, Locals: 4\n" +
					"  public void foo() throws java.lang.Exception;\n" +
					"     0  iconst_1\n" +
					"     1  istore_1 [i]\n" +
					"     2  iload_1 [i]\n" +
					"     3  iconst_1\n" +
					"     4  if_icmpne 24\n" +
					"     7  aload_0 [this]\n" +
					"     8  " + xBarCall +
					"    11  istore_2 [n]\n" +
					"    12  iload_2 [n]\n" +
					"    13  bipush 35\n" +
					"    15  if_icmpne 32\n" +
					"    18  aload_0 [this]\n" +
					"    19  " + xBarCall +
					"    22  pop\n" +
					"    23  return\n" +
					"    24  new java.lang.Exception [16]\n" +
					"    27  dup\n" +
					"    28  invokespecial java.lang.Exception() [22]\n" +
					"    31  athrow\n" +
					"    32  iload_1 [i]\n" +
					"    33  ifne 50\n" +
					"    36  aload_0 [this]\n" +
					"    37  " + xBarCall +
					"    40  pop\n" +
					"    41  return\n" +
					"    42  astore_3\n" +
					"    43  aload_0 [this]\n" +
					"    44  " + xBarCall +
					"    47  pop\n" +
					"    48  aload_3\n" +
					"    49  athrow\n" +
					"    50  aload_0 [this]\n" +
					"    51  " + xBarCall +
					"    54  pop\n" +
					"    55  return\n" +
					"      Exception Table:\n" +
					"        [pc: 2, pc: 18] -> 42 when : any\n" +
					"        [pc: 24, pc: 36] -> 42 when : any\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 3]\n" +
					"        [pc: 2, line: 5]\n" +
					"        [pc: 7, line: 6]\n" +
					"        [pc: 12, line: 7]\n" +
					"        [pc: 18, line: 15]\n" +
					"        [pc: 23, line: 8]\n" +
					"        [pc: 24, line: 10]\n" +
					"        [pc: 32, line: 12]\n" +
					"        [pc: 36, line: 15]\n" +
					"        [pc: 41, line: 13]\n" +
					"        [pc: 42, line: 14]\n" +
					"        [pc: 43, line: 15]\n" +
					"        [pc: 48, line: 16]\n" +
					"        [pc: 50, line: 15]\n" +
					"        [pc: 55, line: 17]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 56] local: this index: 0 type: X\n" +
					"        [pc: 2, pc: 56] local: i index: 1 type: int\n" +
					"        [pc: 12, pc: 24] local: n index: 2 type: int\n" +
					"      Stack map table: number of frames 4\n" +
					"        [pc: 24, append: {int}]\n" +
					"        [pc: 32, same]\n" +
					"        [pc: 42, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
					"        [pc: 50, same]\n";
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
	// Verify the generated code does not have same branch target for the 2 return statements
	public void testBug380313b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.io.FileInputStream;
							import java.io.IOException;
							public class X {
							public void foo() throws Exception {
							        int i = 1;
							        try {
							            try (FileInputStream fis = new FileInputStream("")) {
											 if (i == 2)\
							                	return;
							 			 }
							            if (i == 35)\s
							                return;
							        } catch(IOException e) {
							            bar();
							        } finally {
							            bar();
							        }
							    }
							
							    private int bar() {
							        return 0;
							    }
							
							    public static void main(String[] args) {
									System.out.println("SUCCESS");
							    }
							}
							"""
					},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String xBarCall = (isMinimumCompliant(ClassFileConstants.JDK11) ?
					"invokevirtual" : "invokespecial") + " X.bar() : int [28]\n";
			String expectedOutput =
					"  // Method descriptor #6 ()V\n" +
					"  // Stack: 3, Locals: 6\n" +
					"  public void foo() throws java.lang.Exception;\n" +
					"      0  iconst_1\n" +
					"      1  istore_1 [i]\n" +
					"      2  aconst_null\n" +
					"      3  astore_2\n" +
					"      4  aconst_null\n" +
					"      5  astore_3\n" +
					"      6  new java.io.FileInputStream [18]\n" +
					"      9  dup\n" +
					"     10  ldc <String \"\"> [20]\n" +
					"     12  invokespecial java.io.FileInputStream(java.lang.String) [22]\n" +
					"     15  astore 4 [fis]\n" +
					"     17  iload_1 [i]\n" +
					"     18  iconst_2\n" +
					"     19  if_icmpne 38\n" +
					"     22  aload 4 [fis]\n" +
					"     24  ifnull 32\n" +
					"     27  aload 4 [fis]\n" +
					"     29  invokevirtual java.io.FileInputStream.close() : void [25]\n" +
					"     32  aload_0 [this]\n" + // return 1
					"     33  " + xBarCall +
					"     36  pop\n" +
					"     37  return\n" +
					"     38  aload 4 [fis]\n" +
					"     40  ifnull 86\n" +
					"     43  aload 4 [fis]\n" +
					"     45  invokevirtual java.io.FileInputStream.close() : void [25]\n" +
					"     48  goto 86\n" +
					"     51  astore_2\n" +
					"     52  aload 4 [fis]\n" +
					"     54  ifnull 62\n" +
					"     57  aload 4 [fis]\n" +
					"     59  invokevirtual java.io.FileInputStream.close() : void [25]\n" +
					"     62  aload_2\n" +
					"     63  athrow\n" +
					"     64  astore_3\n" +
					"     65  aload_2\n" +
					"     66  ifnonnull 74\n" +
					"     69  aload_3\n" +
					"     70  astore_2\n" +
					"     71  goto 84\n" +
					"     74  aload_2\n" +
					"     75  aload_3\n" +
					"     76  if_acmpeq 84\n" +
					"     79  aload_2\n" +
					"     80  aload_3\n" +
					"     81  invokevirtual java.lang.Throwable.addSuppressed(java.lang.Throwable) : void [32]\n" +
					"     84  aload_2\n" +
					"     85  athrow\n" +
					"     86  iload_1 [i]\n" +
					"     87  bipush 35\n" +
					"     89  if_icmpne 122\n" +
					"     92  aload_0 [this]\n" + 	// return 2
					"     93  " + xBarCall +
					"     96  pop\n" +
					"     97  return\n" +
					"     98  astore_2 [e]\n" +
					"     99  aload_0 [this]\n" +
					"    100  " + xBarCall +
					"    103  pop\n" +
					"    104  aload_0 [this]\n" +
					"    105  " + xBarCall +
					"    108  pop\n" +
					"    109  goto 127\n" +
					"    112  astore 5\n" +
					"    114  aload_0 [this]\n" +
					"    115  " + xBarCall +
					"    118  pop\n" +
					"    119  aload 5\n" +
					"    121  athrow\n" +
					"    122  aload_0 [this]\n" +
					"    123  " + xBarCall +
					"    126  pop\n" +
					"    127  return\n" +
					"      Exception Table:\n" +
					"        [pc: 17, pc: 22] -> 51 when : any\n" +
					"        [pc: 6, pc: 32] -> 64 when : any\n" +
					"        [pc: 38, pc: 64] -> 64 when : any\n" +
					"        [pc: 2, pc: 32] -> 98 when : java.io.IOException\n" +
					"        [pc: 38, pc: 92] -> 98 when : java.io.IOException\n" +
					"        [pc: 2, pc: 32] -> 112 when : any\n" +
					"        [pc: 38, pc: 92] -> 112 when : any\n" +
					"        [pc: 98, pc: 104] -> 112 when : any\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 5]\n" +
					"        [pc: 2, line: 7]\n" +
					"        [pc: 17, line: 8]\n" +
					"        [pc: 22, line: 9]\n" +
					"        [pc: 32, line: 15]\n" +
					"        [pc: 37, line: 8]\n" +
					"        [pc: 38, line: 9]\n" +
					"        [pc: 86, line: 10]\n" +
					"        [pc: 92, line: 15]\n" +
					"        [pc: 97, line: 11]\n" +
					"        [pc: 98, line: 12]\n" +
					"        [pc: 99, line: 13]\n" +
					"        [pc: 104, line: 15]\n" +
					"        [pc: 112, line: 14]\n" +
					"        [pc: 114, line: 15]\n" +
					"        [pc: 119, line: 16]\n" +
					"        [pc: 122, line: 15]\n" +
					"        [pc: 127, line: 17]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 128] local: this index: 0 type: X\n" +
					"        [pc: 2, pc: 128] local: i index: 1 type: int\n" +
					"        [pc: 17, pc: 62] local: fis index: 4 type: java.io.FileInputStream\n" +
					"        [pc: 99, pc: 104] local: e index: 2 type: java.io.IOException\n" +
					"      Stack map table: number of frames 12\n" +
					"        [pc: 32, full, stack: {}, locals: {X, int, java.lang.Throwable, java.lang.Throwable, java.io.FileInputStream}]\n" +
					"        [pc: 38, same]\n" +
					"        [pc: 51, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
					"        [pc: 62, chop 1 local(s)]\n" +
					"        [pc: 64, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
					"        [pc: 74, same]\n" +
					"        [pc: 84, same]\n" +
					"        [pc: 86, chop 2 local(s)]\n" +
					"        [pc: 98, same_locals_1_stack_item, stack: {java.io.IOException}]\n" +
					"        [pc: 112, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
					"        [pc: 122, same]\n" +
					"        [pc: 127, same]\n";
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
	// Verify the reduced range of locals.
	public void testBug380927() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public final static Object f() {
							        final Object a = null;
							        Object b;
							        label: do {
							            switch (0) {
							            case 1: {
							                b = a;
							            }
							                break;
							            default:
							                break label;
							            }
							        } while (true);
							        return a;
							    }
							    public static void main(final String[] args) {
							        f();
							        System.out.println("SUCCESS");
							    }
							}
							"""
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
				  // Method descriptor #15 ()Ljava/lang/Object;
				  // Stack: 1, Locals: 2
				  public static final java.lang.Object f();
				     0  aconst_null
				     1  astore_0 [a]
				     2  iconst_0
				     3  tableswitch default: 25
				          case 1: 20
				    20  aload_0 [a]
				    21  astore_1 [b]
				    22  goto 2
				    25  aload_0 [a]
				    26  areturn
				      Line numbers:
				        [pc: 0, line: 3]
				        [pc: 2, line: 6]
				        [pc: 20, line: 8]
				        [pc: 22, line: 10]
				        [pc: 25, line: 15]
				      Local variable table:
				        [pc: 2, pc: 27] local: a index: 0 type: java.lang.Object
				        [pc: 22, pc: 25] local: b index: 1 type: java.lang.Object
				      Stack map table: number of frames 3
				        [pc: 2, append: {java.lang.Object}]
				        [pc: 20, same]
				        [pc: 25, same]
				 \s
				""";
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=385593#c1
	public void test385593_1() throws Exception {
		this.runConformTest(
			new String[] {
					"stackmap/StackMapTableFormatError.java",
					"""
						package stackmap;
						
						import java.util.Collection;
						import java.util.Collections;
						
						/**
						 * If compiled with Eclipse (compiler target >= 1.6) this snippet causes the
						 * error "java.lang.ClassFormatError: StackMapTable format error: bad
						 * verification type" when executed with JaCoCo code coverage. JaCoCo seems to
						 * get confused by unexpected stackmap frames generated by ECJ.
						 */
						public class StackMapTableFormatError {
						
							public static Object exec(Collection<Object> set, Object a,
									boolean b) {
								for (Object e : set) {
									if (a != null && (e == null || b)) {
										continue;
									}
									return null;
								}
								return null;
							}
						\t
							public static void main(String[] args) {
								exec(Collections.emptySet(), null, false);
							}
						
						}
						"""
			});

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR+File.separator+"stackmap"+File.separator+"StackMapTableFormatError.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"""
			  // Method descriptor #15 (Ljava/util/Collection;Ljava/lang/Object;Z)Ljava/lang/Object;
			  // Signature: (Ljava/util/Collection<Ljava/lang/Object;>;Ljava/lang/Object;Z)Ljava/lang/Object;
			  // Stack: 1, Locals: 5
			  public static java.lang.Object exec(java.util.Collection set, java.lang.Object a, boolean b);
			     0  aload_0 [set]
			     1  invokeinterface java.util.Collection.iterator() : java.util.Iterator [18] [nargs: 1]
			     6  astore 4
			     8  goto 36
			    11  aload 4
			    13  invokeinterface java.util.Iterator.next() : java.lang.Object [24] [nargs: 1]
			    18  astore_3 [e]
			    19  aload_1 [a]
			    20  ifnull 34
			    23  aload_3 [e]
			    24  ifnull 36
			    27  iload_2 [b]
			    28  ifeq 34
			    31  goto 36
			    34  aconst_null
			    35  areturn
			    36  aload 4
			    38  invokeinterface java.util.Iterator.hasNext() : boolean [30] [nargs: 1]
			    43  ifne 11
			    46  aconst_null
			    47  areturn
			      Line numbers:
			        [pc: 0, line: 16]
			        [pc: 19, line: 17]
			        [pc: 31, line: 18]
			        [pc: 34, line: 20]
			        [pc: 36, line: 16]
			        [pc: 46, line: 22]
			      Local variable table:
			        [pc: 0, pc: 48] local: set index: 0 type: java.util.Collection
			        [pc: 0, pc: 48] local: a index: 1 type: java.lang.Object
			        [pc: 0, pc: 48] local: b index: 2 type: boolean
			        [pc: 19, pc: 36] local: e index: 3 type: java.lang.Object
			      Local variable type table:
			        [pc: 0, pc: 48] local: set index: 0 type: java.util.Collection<java.lang.Object>
			      Stack map table: number of frames 3
			        [pc: 11, full, stack: {}, locals: {java.util.Collection, java.lang.Object, int, _, java.util.Iterator}]
			        [pc: 34, full, stack: {}, locals: {java.util.Collection, java.lang.Object, int, java.lang.Object, java.util.Iterator}]
			        [pc: 36, full, stack: {}, locals: {java.util.Collection, java.lang.Object, int, _, java.util.Iterator}]\
			""";
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=385593#c11
	public void test385593_2() throws Exception {
		this.runConformTest(
			new String[] {
					"snippet/X.java",
					"""
						package snippet;
						
						
						public class X {\s
							private void foo(boolean delete) {\s
								\s
								String s = bar();\s
								StringBuffer buffer =new StringBuffer();\s
								\s
								try {\s
									\s
									String[] datas = new String[] { "" };\s
									Object[] data= new Object[] { s };\s
									try {\s
										buffer.append(datas).append(data);\s
									} catch (Exception e) {\s
										if (e != null)\s
											throw e;\s
										return;\s
									}\s
										\s
									if (delete)\s
										buffer.delete(0, buffer.length());\s
									\s
								} catch (Exception x) {\s
								} finally {\s
									buffer = null;\s
								}\s
							}\s
							\s
							String bar() {\s
								return "";\s
							}\s
							\s
							public static void main(String[] args) {\s
								new X().foo(false);\s
								System.out.println("SUCCESS");\s
							}\s
						}
						"""
			});

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR+File.separator+"snippet"+File.separator+"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"""
			Stack map table: number of frames 7
			        [pc: 49, full, stack: {java.lang.Exception}, locals: {snippet.X, int, java.lang.String, java.lang.StringBuffer, java.lang.String[], java.lang.Object[]}]
			        [pc: 59, append: {java.lang.Exception}]
			        [pc: 62, chop 1 local(s)]
			        [pc: 79, full, stack: {java.lang.Exception}, locals: {snippet.X, int, java.lang.String, java.lang.StringBuffer}]
			        [pc: 86, same_locals_1_stack_item, stack: {java.lang.Throwable}]
			        [pc: 93, same]
			        [pc: 95, same]
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
	// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=394718
	public void test394718() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						public class X
						{
							public static Boolean test() throws Exception
							{
								try
								{
									for (int i = 0; i < 1; i++)
									{
										long status = System.currentTimeMillis();
										if (status < 0)
											return false;
										if (status == 1)
											return false;
									}
								\t
									return false;
								}
								finally
								{
									System.currentTimeMillis();
								}
							}
						\t
							public static void main(String[] args) throws Exception
							{
								System.out.print("Starting");
								test();
								System.out.println("Done");
							}
						}"""
			},
			"StartingDone");
	}

	// https://bugs.eclipse.org/412203
	public void testBug412203_a() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // using <>
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		this.runConformTest(
				new String[] {
					"X2.java",
					"""
						import java.util.*;
						
						import org.eclipse.jdt.annotation.NonNullByDefault;
						
						class Y {
							public Y() { }
						}
						
						@NonNullByDefault
						abstract class X1 {
						
							private Object a;
							private Object b;
							private Object c;
							private Object d;
							private Object e;
							private Object f;
						
							protected Object name;
						
							@SuppressWarnings("null")
							protected X1() {
								super ();
							}
						
						}
						public class X2 extends X1 {
						
						
							public static final int ID = 4711;
						
							private Object x;
							private Object y;
							private Object z;
						
							private Runnable runable = new Runnable () {
								@Override
								public void run () {
									// whatever
								}
							};
						
							private void init () {
								final Object selector = new Object ();
								this.name = new Object ();
								LinkedList<Character> invalidCharactersList = new LinkedList<> ();
								char[] invalidCharacters = new char[invalidCharactersList.size ()];
								for (int i = 0; i < invalidCharacters.length; i++) {
									invalidCharacters[i] = invalidCharactersList.get (i).charValue ();
								}
								Y inputVerifier = new Y();
							}
						
						}
						""",
				},
				"",
				getLibsWithNullAnnotations(ClassFileConstants.JDK1_7),
				true/*flush*/,
				null/*vmArgs*/,
				options,
				null/*requestor*/,
				true/*skipJavac*/);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X2.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"""
				  // Method descriptor #16 ()V
				  // Stack: 4, Locals: 5
				  private void init();
				     0  new java.lang.Object [32]
				     3  dup
				     4  invokespecial java.lang.Object() [34]
				     7  astore_1 [selector]
				     8  aload_0 [this]
				     9  new java.lang.Object [32]
				    12  dup
				    13  invokespecial java.lang.Object() [34]
				    16  putfield X2.name : java.lang.Object [35]
				    19  new java.util.LinkedList [38]
				    22  dup
				    23  invokespecial java.util.LinkedList() [40]
				    26  astore_2 [invalidCharactersList]
				    27  aload_2 [invalidCharactersList]
				    28  invokevirtual java.util.LinkedList.size() : int [41]
				    31  newarray char [5]
				    33  astore_3 [invalidCharacters]
				    34  iconst_0
				    35  istore 4 [i]
				    37  goto 59
				    40  aload_3 [invalidCharacters]
				    41  iload 4 [i]
				    43  aload_2 [invalidCharactersList]
				    44  iload 4 [i]
				    46  invokevirtual java.util.LinkedList.get(int) : java.lang.Object [45]
				    49  checkcast java.lang.Character [49]
				    52  invokevirtual java.lang.Character.charValue() : char [51]
				    55  castore
				    56  iinc 4 1 [i]
				    59  iload 4 [i]
				    61  aload_3 [invalidCharacters]
				    62  arraylength
				    63  if_icmplt 40
				    66  new Y [55]
				    69  dup
				    70  invokespecial Y() [57]
				    73  astore 4 [inputVerifier]
				    75  return
				      Line numbers:
				        [pc: 0, line: 44]
				        [pc: 8, line: 45]
				        [pc: 19, line: 46]
				        [pc: 27, line: 47]
				        [pc: 34, line: 48]
				        [pc: 40, line: 49]
				        [pc: 56, line: 48]
				        [pc: 66, line: 51]
				        [pc: 75, line: 52]
				      Local variable table:
				        [pc: 0, pc: 76] local: this index: 0 type: X2
				        [pc: 8, pc: 76] local: selector index: 1 type: java.lang.Object
				        [pc: 27, pc: 76] local: invalidCharactersList index: 2 type: java.util.LinkedList
				        [pc: 34, pc: 76] local: invalidCharacters index: 3 type: char[]
				        [pc: 37, pc: 66] local: i index: 4 type: int
				        [pc: 75, pc: 76] local: inputVerifier index: 4 type: Y
				      Local variable type table:
				        [pc: 27, pc: 76] local: invalidCharactersList index: 2 type: java.util.LinkedList<java.lang.Character>
				      Stack map table: number of frames 2
				        [pc: 40, full, stack: {}, locals: {X2, java.lang.Object, java.util.LinkedList, char[], int}]
				        [pc: 59, same]
				""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/412203
	// yet simplified version - using FieldReference
	public void testBug412203_b() throws Exception {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		this.runConformTest(
				new String[] {
					"X2.java",
					"""
						import java.util.LinkedList;
						
						import org.eclipse.jdt.annotation.NonNull;
						
						abstract class X1 {
							protected @NonNull Object name = new Object();
						}
						
						public class X2 extends X1 {
							void init () {
								this.name = new Object ();
								LinkedList<Character> l = new LinkedList<Character> ();
								char[] cs = new char[l.size ()];
								for (int i = 0; i < cs.length; i++) {
									cs[i] = l.get (i).charValue ();
								}
								Object o2 = new Object();
							}
						}
						""",
				},
				"",
				getLibsWithNullAnnotations(ClassFileConstants.JDK1_7),
				true/*flush*/,
				null/*vmArgs*/,
				options,
				null/*requestor*/,
				true/*skipJavac*/);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X2.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"""
				  // Method descriptor #6 ()V
				  // Stack: 4, Locals: 4
				  void init();
				     0  aload_0 [this]
				     1  new java.lang.Object [15]
				     4  dup
				     5  invokespecial java.lang.Object() [17]
				     8  putfield X2.name : java.lang.Object [18]
				    11  new java.util.LinkedList [22]
				    14  dup
				    15  invokespecial java.util.LinkedList() [24]
				    18  astore_1 [l]
				    19  aload_1 [l]
				    20  invokevirtual java.util.LinkedList.size() : int [25]
				    23  newarray char [5]
				    25  astore_2 [cs]
				    26  iconst_0
				    27  istore_3 [i]
				    28  goto 48
				    31  aload_2 [cs]
				    32  iload_3 [i]
				    33  aload_1 [l]
				    34  iload_3 [i]
				    35  invokevirtual java.util.LinkedList.get(int) : java.lang.Object [29]
				    38  checkcast java.lang.Character [33]
				    41  invokevirtual java.lang.Character.charValue() : char [35]
				    44  castore
				    45  iinc 3 1 [i]
				    48  iload_3 [i]
				    49  aload_2 [cs]
				    50  arraylength
				    51  if_icmplt 31
				    54  new java.lang.Object [15]
				    57  dup
				    58  invokespecial java.lang.Object() [17]
				    61  astore_3 [o2]
				    62  return
				      Line numbers:
				        [pc: 0, line: 11]
				        [pc: 11, line: 12]
				        [pc: 19, line: 13]
				        [pc: 26, line: 14]
				        [pc: 31, line: 15]
				        [pc: 45, line: 14]
				        [pc: 54, line: 17]
				        [pc: 62, line: 18]
				      Local variable table:
				        [pc: 0, pc: 63] local: this index: 0 type: X2
				        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList
				        [pc: 26, pc: 63] local: cs index: 2 type: char[]
				        [pc: 28, pc: 54] local: i index: 3 type: int
				        [pc: 62, pc: 63] local: o2 index: 3 type: java.lang.Object
				      Local variable type table:
				        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList<java.lang.Character>
				      Stack map table: number of frames 2
				        [pc: 31, append: {java.util.LinkedList, char[], int}]
				        [pc: 48, same]
				}""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/412203
	// yet simplified version - using SingleNameReference
	public void testBug412203_c() throws Exception {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		this.runConformTest(
				new String[] {
					"X2.java",
					"""
						import java.util.LinkedList;
						
						import org.eclipse.jdt.annotation.NonNull;
						
						abstract class X1 {
							protected @NonNull Object name = new Object();
						}
						
						public class X2 extends X1 {
							void init () {
								name = new Object ();
								LinkedList<Character> l = new LinkedList<Character> ();
								char[] cs = new char[l.size ()];
								for (int i = 0; i < cs.length; i++) {
									cs[i] = l.get (i).charValue ();
								}
								Object o2 = new Object();
							}
						}
						""",
				},
				"",
				getLibsWithNullAnnotations(ClassFileConstants.JDK1_7),
				true/*flush*/,
				null/*vmArgs*/,
				options,
				null/*requestor*/,
				true/*skipJavac*/);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X2.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"""
				  // Method descriptor #6 ()V
				  // Stack: 4, Locals: 4
				  void init();
				     0  aload_0 [this]
				     1  new java.lang.Object [15]
				     4  dup
				     5  invokespecial java.lang.Object() [17]
				     8  putfield X2.name : java.lang.Object [18]
				    11  new java.util.LinkedList [22]
				    14  dup
				    15  invokespecial java.util.LinkedList() [24]
				    18  astore_1 [l]
				    19  aload_1 [l]
				    20  invokevirtual java.util.LinkedList.size() : int [25]
				    23  newarray char [5]
				    25  astore_2 [cs]
				    26  iconst_0
				    27  istore_3 [i]
				    28  goto 48
				    31  aload_2 [cs]
				    32  iload_3 [i]
				    33  aload_1 [l]
				    34  iload_3 [i]
				    35  invokevirtual java.util.LinkedList.get(int) : java.lang.Object [29]
				    38  checkcast java.lang.Character [33]
				    41  invokevirtual java.lang.Character.charValue() : char [35]
				    44  castore
				    45  iinc 3 1 [i]
				    48  iload_3 [i]
				    49  aload_2 [cs]
				    50  arraylength
				    51  if_icmplt 31
				    54  new java.lang.Object [15]
				    57  dup
				    58  invokespecial java.lang.Object() [17]
				    61  astore_3 [o2]
				    62  return
				      Line numbers:
				        [pc: 0, line: 11]
				        [pc: 11, line: 12]
				        [pc: 19, line: 13]
				        [pc: 26, line: 14]
				        [pc: 31, line: 15]
				        [pc: 45, line: 14]
				        [pc: 54, line: 17]
				        [pc: 62, line: 18]
				      Local variable table:
				        [pc: 0, pc: 63] local: this index: 0 type: X2
				        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList
				        [pc: 26, pc: 63] local: cs index: 2 type: char[]
				        [pc: 28, pc: 54] local: i index: 3 type: int
				        [pc: 62, pc: 63] local: o2 index: 3 type: java.lang.Object
				      Local variable type table:
				        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList<java.lang.Character>
				      Stack map table: number of frames 2
				        [pc: 31, append: {java.util.LinkedList, char[], int}]
				        [pc: 48, same]
				}""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void testBug5409021() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"\n" +
					"	public static void main(String[] args) {\n" +
					"		X error = new X();\n" +
					"		error.reproduce(\"hello\");\n" +
					"		System.out.println(\"DONE\");\n" +
					"	}\n" +
					"	\n" +
					"	public Object reproduce(Object param) throws RuntimeException {\n" +
					"		Object local;\n" +
					"		try {\n" +
					"			return param; \n" +
					"		} \n" +
					"		catch (RuntimeException e) {\n" +
					"			return null;\n" +
					"		} \n" +
					"		finally {\n" +
					"			if (param != null) {\n" +
					"				System.out.println(\"FINALLY\");\n" +
					"			}\n" +
					"			local = null;\n" +
					"		}\n" +
					"	}\n" +
					"}\n" +
					"",
				},
				"FINALLY\n" +
				"DONE");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);
			String expectedOutput =
					"""
				  // Stack: 2, Locals: 6
				  public java.lang.Object reproduce(java.lang.Object param) throws java.lang.RuntimeException;
				     0  aload_1 [param]
				     1  astore 5
				     3  aload_1 [param]
				     4  ifnull 15
				     7  getstatic java.lang.System.out : java.io.PrintStream [23]
				    10  ldc <String "FINALLY"> [43]
				    12  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]
				    15  aconst_null
				    16  astore_2 [local]
				    17  aload 5
				    19  areturn
				    20  astore_3 [e]
				    21  aload_1 [param]
				    22  ifnull 33
				    25  getstatic java.lang.System.out : java.io.PrintStream [23]
				    28  ldc <String "FINALLY"> [43]
				    30  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]
				    33  aconst_null
				    34  astore_2 [local]
				    35  aconst_null
				    36  areturn
				    37  astore 4
				    39  aload_1 [param]
				    40  ifnull 51
				    43  getstatic java.lang.System.out : java.io.PrintStream [23]
				    46  ldc <String "FINALLY"> [43]
				    48  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]
				    51  aconst_null
				    52  astore_2 [local]
				    53  aload 4
				    55  athrow
				      Exception Table:
				        [pc: 0, pc: 3] -> 20 when : java.lang.RuntimeException
				        [pc: 0, pc: 3] -> 37 when : any
				        [pc: 20, pc: 21] -> 37 when : any
				      Line numbers:
				        [pc: 0, line: 12]
				        [pc: 3, line: 18]
				        [pc: 7, line: 19]
				        [pc: 15, line: 21]
				        [pc: 17, line: 12]
				        [pc: 20, line: 14]
				        [pc: 21, line: 18]
				        [pc: 25, line: 19]
				        [pc: 33, line: 21]
				        [pc: 35, line: 15]
				        [pc: 37, line: 17]
				        [pc: 39, line: 18]
				        [pc: 43, line: 19]
				        [pc: 51, line: 21]
				        [pc: 53, line: 22]
				      Local variable table:
				        [pc: 0, pc: 56] local: this index: 0 type: X
				        [pc: 0, pc: 56] local: param index: 1 type: java.lang.Object
				        [pc: 17, pc: 20] local: local index: 2 type: java.lang.Object
				        [pc: 35, pc: 37] local: local index: 2 type: java.lang.Object
				        [pc: 53, pc: 56] local: local index: 2 type: java.lang.Object
				        [pc: 21, pc: 37] local: e index: 3 type: java.lang.RuntimeException
				      Stack map table: number of frames 5
				        [pc: 15, full, stack: {}, locals: {X, java.lang.Object, _, _, _, java.lang.Object}]
				        [pc: 20, full, stack: {java.lang.RuntimeException}, locals: {X, java.lang.Object}]
				        [pc: 33, full, stack: {}, locals: {X, java.lang.Object, _, java.lang.RuntimeException}]
				        [pc: 37, full, stack: {java.lang.Throwable}, locals: {X, java.lang.Object}]
				        [pc: 51, full, stack: {}, locals: {X, java.lang.Object, _, _, java.lang.Throwable}]
				}""";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test551368() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					interface A {
					}
					class B implements A {
						public C c;
					\t
						protected B original() {
							return this;
						}
					}
					class C {
						C parent;
						A context;
					}
					class F extends C {
					\t
					}
					class G extends C {
					\t
					}
					abstract class D implements A {
						public F c;
					}
					class E implements A {
						public G c;
					}
					public class X {
						boolean foo(A a) {
							if (a instanceof B && a != ((B) a).original())
								return true;
							C aC = a instanceof D ? ((D) a).c :
								a instanceof E ? ((E) a).c :\s
								a instanceof B ? ((B) a).c :
									null;
							return aC != null ? foo(aC.parent.context) : false;
						}
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}""",
            },
			"SUCCESS");
	}
	public void test551368_2() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					public class X {
					
						int size;
						char[][][] elements;
					
						public X() {
							this.size = 0;
							this.elements = new char[10][][];
						}
					
						public void insertIntoArray(char[][][] target) {
						}
					
						public void add(char[][] newElement) {
							insertIntoArray(this.size < this.elements.length ? this.elements : new char[this.elements.length * 2][][]);
						}
					
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}""",
            },
			"SUCCESS");
	}
	public void test551368_3() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					class B {
						public boolean bar() {
							return false;
						}
						public void foo() {}
					}
					public class X {
					\t
						public B foo(boolean test) {
							B b =
								test ?
									new B() {
										@Override
										public boolean bar() {
											return true;
										}
									} :
								new B();
							b.foo();
							return b;
						}
					
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}""",
            },
			"SUCCESS");
	}
	public void test558844() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"""
					public class X {
					\t
						public static void main( String[] args ) {
							System.out.println(new X().getText());
						}
					
						public String getText() {
							Long lValue1 = getValue1();
							Long lValue2 = getValue2();
							return ( isValue1() ? "" : ( lValue1 == null ? "" : lValue1.toString() ) + "-" ) + ( lValue2 == null ? "" : lValue2.toString() );
						}
					
						private Long getValue1() {
							return Long.valueOf( 1 );
						}
					
						private Long getValue2() {
							return Long.valueOf( 1 );
						}
					
						private boolean isValue1() {
							return false;
						}
					}""",
            },
			"1-1");
	}
	public void test562854() {
		this.runConformTest(
			new String[] {
				"bug/Bug.java",
				"""
					package bug;
					public class Bug {
						public static void main(String[] args) {
							F(args.length > 1 ? pkg.Base.derived1() : pkg.Base.derived2());
						}
					
						static void F(pkg.Base base) {
							System.out.println(base.getClass().getCanonicalName());
						}
					}""",
				"pkg/Base.java",
				"""
					package pkg;
					public abstract class Base {
						public static Derived1 derived1() {
							return new Derived1();
						}
					
						public static Derived2 derived2() {
							return new Derived2();
						}
					}""",
				"pkg/Derived1.java",
				"package pkg;\n" +
				"class Derived1 extends Base {}",
				"pkg/Derived2.java",
				"package pkg;\n" +
				"class Derived2 extends Derived1 {}",
			},
			"pkg.Derived2");
	}
	public void test562854_2() {
		this.runConformTest(
			new String[] {
				"bug/Bug.java",
				"""
					package bug;
					public class Bug {
						public static void main(String[] args) {
							F(args.length > 1 ? pkg.Base.derived1() : pkg.Base.derived2());
						}
					
						static void F(pkg.Base base) {
							System.out.println(base.getClass().getCanonicalName());
						}
					}""",
				"pkg/Base.java",
				"""
					package pkg;
					public abstract class Base {
						public static Derived1 derived1() {
							return new Derived1();
						}
					
						public static Derived2 derived2() {
							return new Derived2();
						}
					}""",
				"pkg/Derived1.java",
				"package pkg;\n" +
				"public class Derived1 extends Base {}",
				"pkg/Derived2.java",
				"package pkg;\n" +
				"public class Derived2 extends Derived1 {}",
			},
			"pkg.Derived2");
	}
}
