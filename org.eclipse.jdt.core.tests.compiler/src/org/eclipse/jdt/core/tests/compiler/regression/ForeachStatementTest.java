/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *								Bug 388800 - [1.8] adjust tests to 1.8 JRE
 *								bug 393719 - [compiler] inconsistent warnings on iteration variables
 *     Jesper S Moller -  Contribution for
 *								bug 401853 - Eclipse Java compiler creates invalid bytecode (java.lang.VerifyError)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ForeachStatementTest extends AbstractComparableTest {

public ForeachStatementTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test055" };
//	TESTS_NUMBERS = new int[] { 50, 51, 52, 53 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildComparableTestSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				       \s
				        for (char c : "SUCCESS".toCharArray()) {
				            System.out.print(c);
				        }
				        System.out.println();
				    }
				}
				""",
		},
		"SUCCESS");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				       \s
				        for (int value : new int[] {value}) {
				            System.out.println(value);
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				for (int value : new int[] {value}) {
				                            ^^^^^
			value cannot be resolved to a variable
			----------
			""");
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				       \s
				        for (int value : value) {
				            System.out.println(value);
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				for (int value : value) {
				                 ^^^^^
			value cannot be resolved to a variable
			----------
			""");
}
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						int sum = 0;
						loop: for (final int e : tab) {
							sum += e;
							if (e == 3) {
								break loop;
							}
						}
						System.out.println(sum);
					}
				}
				""",
		},
		"6");
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					    final int i;
						int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						int sum = 0;
						loop: for (final int e : tab) {
							sum += e;
							if (e == 3) {
							    i = 1;
								break loop;
							}
						}
						System.out.println(sum + i);
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				System.out.println(sum + i);
				                         ^
			The local variable i may not have been initialized
			----------
			""");
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
					    final int i;
						int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						loop: for (final int e : tab) {
						    i = e;
							if (e == 3) {
							    i = 1;
								break loop;
							}
						}
						System.out.println(i);
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				i = e;
				^
			The final local variable i may already have been assigned
			----------
			2. ERROR in X.java (at line 9)
				i = 1;
				^
			The final local variable i may already have been assigned
			----------
			3. ERROR in X.java (at line 13)
				System.out.println(i);
				                   ^
			The local variable i may not have been initialized
			----------
			""");
}
public void test007() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
					    int i;
						int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						for (final int e : tab) {
						    i = e;
						}
						System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 7
		  public static void main(java.lang.String[] args);
		     0  bipush 9
		     2  newarray int [10]
		     4  dup
		     5  iconst_0
		     6  iconst_1
		     7  iastore
		     8  dup
		     9  iconst_1
		    10  iconst_2
		    11  iastore
		    12  dup
		    13  iconst_2
		    14  iconst_3
		    15  iastore
		    16  dup
		    17  iconst_3
		    18  iconst_4
		    19  iastore
		    20  dup
		    21  iconst_4
		    22  iconst_5
		    23  iastore
		    24  dup
		    25  iconst_5
		    26  bipush 6
		    28  iastore
		    29  dup
		    30  bipush 6
		    32  bipush 7
		    34  iastore
		    35  dup
		    36  bipush 7
		    38  bipush 8
		    40  iastore
		    41  dup
		    42  bipush 8
		    44  bipush 9
		    46  iastore
		    47  astore_2 [tab]
		    48  aload_2 [tab]
		    49  dup
		    50  astore 6
		    52  arraylength
		    53  istore 5
		    55  iconst_0
		    56  istore 4
		    58  goto 72
		    61  aload 6
		    63  iload 4
		    65  iaload
		    66  istore_3 [e]
		    67  iload_3 [e]
		    68  istore_1
		    69  iinc 4 1
		    72  iload 4
		    74  iload 5
		    76  if_icmplt 61
		    79  getstatic java.lang.System.out : java.io.PrintStream [16]
		    82  ldc <String "SUCCESS"> [22]
		    84  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    87  return
		      Line numbers:
		        [pc: 0, line: 5]
		        [pc: 48, line: 6]
		        [pc: 67, line: 7]
		        [pc: 69, line: 6]
		        [pc: 79, line: 9]
		        [pc: 87, line: 10]
		      Local variable table:
		        [pc: 0, pc: 88] local: args index: 0 type: java.lang.String[]
		        [pc: 48, pc: 88] local: tab index: 2 type: int[]
		        [pc: 67, pc: 69] local: e index: 3 type: int
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
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(Iterable col) {
						for (X x : col) {
							System.out.println(x);
						}
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				void foo(Iterable col) {
				         ^^^^^^^^
			Iterable is a raw type. References to generic type Iterable<T> should be parameterized
			----------
			2. ERROR in X.java (at line 3)
				for (X x : col) {
				           ^^^
			Type mismatch: cannot convert from element type Object to X
			----------
			""");
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(Iterable<String> col) {
						for (X x : col) {
							System.out.println(x);
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				for (X x : col) {
				           ^^^
			Type mismatch: cannot convert from element type String to X
			----------
			""");
}
/*
 * Test implicit conversion to float. If missing, VerifyError
 */
public void test010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						int sum = 0;
						loop: for (final float e : tab) {
							sum += e;
							if (e == 3) {
								break loop;
							}
						}
						System.out.println(sum);
					}
				}
				""",
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
				"""
					public class X {
					   \s
						public static void main(String[] args) {
							int[][] tab = new int[][] {
								new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
								new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
							};
							loop: for (final int e : tab) {
							}
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					loop: for (final int e : tab) {
					                         ^^^
				Type mismatch: cannot convert from element type int[] to int
				----------
				""");
}
/*
 * Ensure access to int[]
 */
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[][] tab = new int[][] {
							new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
							new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
						};
						for (final int[] e : tab) {
							System.out.print(e.length);
						}
					}
				}
				""",
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
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[][] tab = new int[][] {
							new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
							new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
						};
						for (final int[] e : tab) {
							System.out.print(e[0]);
						}
					}
				}
				""",
		},
		"11");
}
/*
 * Empty block action
 */
public void test014() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1 };
						for (final int e : tab) {
						}
						System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  newarray int [10]
		     3  dup
		     4  iconst_0
		     5  iconst_1
		     6  iastore
		     7  astore_1 [tab]
		     8  getstatic java.lang.System.out : java.io.PrintStream [16]
		    11  ldc <String "SUCCESS"> [22]
		    13  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    16  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 8, line: 7]
		        [pc: 16, line: 8]
		      Local variable table:
		        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 17] local: tab index: 1 type: int[]
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
/*
 * Empty statement action
 */
public void test015() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1 };
						for (final int e : tab);
						System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  newarray int [10]
		     3  dup
		     4  iconst_0
		     5  iconst_1
		     6  iastore
		     7  astore_1 [tab]
		     8  getstatic java.lang.System.out : java.io.PrintStream [16]
		    11  ldc <String "SUCCESS"> [22]
		    13  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    16  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 8, line: 6]
		        [pc: 16, line: 7]
		      Local variable table:
		        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 17] local: tab index: 1 type: int[]
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
/*
 * Empty block action
 */
public void test016() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1 };
						for (final int e : tab) {;
						}
						System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 5
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  newarray int [10]
		     3  dup
		     4  iconst_0
		     5  iconst_1
		     6  iastore
		     7  astore_1 [tab]
		     8  aload_1 [tab]
		     9  dup
		    10  astore 4
		    12  arraylength
		    13  istore_3
		    14  iconst_0
		    15  istore_2
		    16  goto 22
		    19  iinc 2 1
		    22  iload_2
		    23  iload_3
		    24  if_icmplt 19
		    27  getstatic java.lang.System.out : java.io.PrintStream [16]
		    30  ldc <String "SUCCESS"> [22]
		    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    35  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 8, line: 5]
		        [pc: 27, line: 7]
		        [pc: 35, line: 8]
		      Local variable table:
		        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 36] local: tab index: 1 type: int[]
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
/*
 * Ensure access to int[]
 */
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1 };
						for (final int e : tab) {
							System.out.println("SUCCESS");
						}
					}
				}
				""",
		},
		"SUCCESS");
}
/*
 * Break the loop
 */
public void test018() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] { 1 };
						for (final int e : tab) {
							System.out.println(e);
							break;
						}
					}
				}
				""",
		},
		"1");
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 4
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  newarray int [10]
		     3  dup
		     4  iconst_0
		     5  iconst_1
		     6  iastore
		     7  astore_1 [tab]
		     8  aload_1 [tab]
		     9  dup
		    10  astore_3
		    11  arraylength
		    12  ifeq 26
		    15  aload_3
		    16  iconst_0
		    17  iaload
		    18  istore_2 [e]
		    19  getstatic java.lang.System.out : java.io.PrintStream [16]
		    22  iload_2 [e]
		    23  invokevirtual java.io.PrintStream.println(int) : void [22]
		    26  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 8, line: 5]
		        [pc: 19, line: 6]
		        [pc: 26, line: 9]
		      Local variable table:
		        [pc: 0, pc: 27] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 27] local: tab index: 1 type: int[]
		        [pc: 19, pc: 26] local: e index: 2 type: int
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
/*
 * Break the loop
 */
public void test019() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] {};
						System.out.print("SUC");
						for (final int e : tab) {
							System.out.print("1x");
							break;
						}
						System.out.println("CESS");
					}
				}
				""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_0
		     1  newarray int [10]
		     3  astore_1 [tab]
		     4  getstatic java.lang.System.out : java.io.PrintStream [16]
		     7  ldc <String "SUC"> [22]
		     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    12  aload_1 [tab]
		    13  dup
		    14  astore_2
		    15  arraylength
		    16  ifeq 27
		    19  getstatic java.lang.System.out : java.io.PrintStream [16]
		    22  ldc <String "1x"> [30]
		    24  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    27  getstatic java.lang.System.out : java.io.PrintStream [16]
		    30  ldc <String "CESS"> [32]
		    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
		    35  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 4, line: 5]
		        [pc: 12, line: 6]
		        [pc: 19, line: 7]
		        [pc: 27, line: 10]
		        [pc: 35, line: 11]
		      Local variable table:
		        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 36] local: tab index: 1 type: int[]
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
/*
 * Break the loop
 */
public void test020() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   \s
					public static void main(String[] args) {
						int[] tab = new int[] {};
						System.out.print("SUC");
						loop: for (final int e : tab) {
							System.out.print("1x");
							continue loop;
						}
						System.out.println("CESS");
					}
				}
				""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 5
		  public static void main(java.lang.String[] args);
		     0  iconst_0
		     1  newarray int [10]
		     3  astore_1 [tab]
		     4  getstatic java.lang.System.out : java.io.PrintStream [16]
		     7  ldc <String "SUC"> [22]
		     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    12  aload_1 [tab]
		    13  dup
		    14  astore 4
		    16  arraylength
		    17  istore_3
		    18  iconst_0
		    19  istore_2
		    20  goto 34
		    23  getstatic java.lang.System.out : java.io.PrintStream [16]
		    26  ldc <String "1x"> [30]
		    28  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    31  iinc 2 1
		    34  iload_2
		    35  iload_3
		    36  if_icmplt 23
		    39  getstatic java.lang.System.out : java.io.PrintStream [16]
		    42  ldc <String "CESS"> [32]
		    44  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
		    47  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 4, line: 5]
		        [pc: 12, line: 6]
		        [pc: 23, line: 7]
		        [pc: 31, line: 6]
		        [pc: 39, line: 10]
		        [pc: 47, line: 11]
		      Local variable table:
		        [pc: 0, pc: 48] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 48] local: tab index: 1 type: int[]
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
public void test021() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
						int sum = 0;
						int i = 0;
						loop1: while(true) {
							i++;
							loop: for (final int e : tab) {
								sum += e;
								if (i == 3) {
									break loop1;
								} else if (e == 5) {
									break loop;
								} else {
									continue;
								}
							}
						}
						System.out.println(sum);
					}
				}""",
		},
		"31");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 8
		  public static void main(java.lang.String[] args);
		      0  bipush 9
		      2  newarray int [10]
		      4  dup
		      5  iconst_0
		      6  iconst_1
		      7  iastore
		      8  dup
		      9  iconst_1
		     10  iconst_2
		     11  iastore
		     12  dup
		     13  iconst_2
		     14  iconst_3
		     15  iastore
		     16  dup
		     17  iconst_3
		     18  iconst_4
		     19  iastore
		     20  dup
		     21  iconst_4
		     22  iconst_5
		     23  iastore
		     24  dup
		     25  iconst_5
		     26  bipush 6
		     28  iastore
		     29  dup
		     30  bipush 6
		     32  bipush 7
		     34  iastore
		     35  dup
		     36  bipush 7
		     38  bipush 8
		     40  iastore
		     41  dup
		     42  bipush 8
		     44  bipush 9
		     46  iastore
		     47  astore_1 [tab]
		     48  iconst_0
		     49  istore_2 [sum]
		     50  iconst_0
		     51  istore_3 [i]
		     52  iinc 3 1 [i]
		     55  aload_1 [tab]
		     56  dup
		     57  astore 7
		     59  arraylength
		     60  istore 6
		     62  iconst_0
		     63  istore 5
		     65  goto 100
		     68  aload 7
		     70  iload 5
		     72  iaload
		     73  istore 4 [e]
		     75  iload_2 [sum]
		     76  iload 4 [e]
		     78  iadd
		     79  istore_2 [sum]
		     80  iload_3 [i]
		     81  iconst_3
		     82  if_icmpne 88
		     85  goto 110
		     88  iload 4 [e]
		     90  iconst_5
		     91  if_icmpne 97
		     94  goto 52
		     97  iinc 5 1
		    100  iload 5
		    102  iload 6
		    104  if_icmplt 68
		    107  goto 52
		    110  getstatic java.lang.System.out : java.io.PrintStream [16]
		    113  iload_2 [sum]
		    114  invokevirtual java.io.PrintStream.println(int) : void [22]
		    117  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 48, line: 4]
		        [pc: 50, line: 5]
		        [pc: 52, line: 7]
		        [pc: 55, line: 8]
		        [pc: 75, line: 9]
		        [pc: 80, line: 10]
		        [pc: 85, line: 11]
		        [pc: 88, line: 12]
		        [pc: 94, line: 13]
		        [pc: 97, line: 8]
		        [pc: 107, line: 6]
		        [pc: 110, line: 19]
		        [pc: 117, line: 20]
		      Local variable table:
		        [pc: 0, pc: 118] local: args index: 0 type: java.lang.String[]
		        [pc: 48, pc: 118] local: tab index: 1 type: int[]
		        [pc: 50, pc: 118] local: sum index: 2 type: int
		        [pc: 52, pc: 118] local: i index: 3 type: int
		        [pc: 75, pc: 97] local: e index: 4 type: int
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
public void test022() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
					public static void main(String[] args) {
						ArrayList<Integer> arrayList = new ArrayList<Integer>();
						for (int i = 0; i < 10; i++) {
							arrayList.add(new Integer(i));
						}
						int sum = 0;
						for (Integer e : arrayList) {
							sum += e.intValue();
						}
						System.out.println(sum);
					}
				}""",
		},
		"45");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 5
		  public static void main(java.lang.String[] args);
		     0  new java.util.ArrayList [16]
		     3  dup
		     4  invokespecial java.util.ArrayList() [18]
		     7  astore_1 [arrayList]
		     8  iconst_0
		     9  istore_2 [i]
		    10  goto 29
		    13  aload_1 [arrayList]
		    14  new java.lang.Integer [19]
		    17  dup
		    18  iload_2 [i]
		    19  invokespecial java.lang.Integer(int) [21]
		    22  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [24]
		    25  pop
		    26  iinc 2 1 [i]
		    29  iload_2 [i]
		    30  bipush 10
		    32  if_icmplt 13
		    35  iconst_0
		    36  istore_2 [sum]
		    37  aload_1 [arrayList]
		    38  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [28]
		    41  astore 4
		    43  goto 64
		    46  aload 4
		    48  invokeinterface java.util.Iterator.next() : java.lang.Object [32] [nargs: 1]
		    53  checkcast java.lang.Integer [19]
		    56  astore_3 [e]
		    57  iload_2 [sum]
		    58  aload_3 [e]
		    59  invokevirtual java.lang.Integer.intValue() : int [38]
		    62  iadd
		    63  istore_2 [sum]
		    64  aload 4
		    66  invokeinterface java.util.Iterator.hasNext() : boolean [42] [nargs: 1]
		    71  ifne 46
		    74  getstatic java.lang.System.out : java.io.PrintStream [46]
		    77  iload_2 [sum]
		    78  invokevirtual java.io.PrintStream.println(int) : void [52]
		    81  return
		      Line numbers:
		        [pc: 0, line: 5]
		        [pc: 8, line: 6]
		        [pc: 13, line: 7]
		        [pc: 26, line: 6]
		        [pc: 35, line: 9]
		        [pc: 37, line: 10]
		        [pc: 57, line: 11]
		        [pc: 64, line: 10]
		        [pc: 74, line: 13]
		        [pc: 81, line: 14]
		      Local variable table:
		        [pc: 0, pc: 82] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 82] local: arrayList index: 1 type: java.util.ArrayList
		        [pc: 10, pc: 35] local: i index: 2 type: int
		        [pc: 37, pc: 82] local: sum index: 2 type: int
		        [pc: 57, pc: 64] local: e index: 3 type: java.lang.Integer
		      Local variable type table:
		        [pc: 8, pc: 82] local: arrayList index: 1 type: java.util.ArrayList<java.lang.Integer>
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

/*
 * Type mismatch, using non parameterized collection type (indirectly implementing parameterized type)
 */
public void test023() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Iterator;
					
					public class X {
					    public static void main(String[] args) {
							for (Thread s : new AX()) {
							}
						}
					}
					
					class AX implements Iterable<String> {
					   \s
					   public Iterator<String> iterator() {
					        return null;
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					for (Thread s : new AX()) {
					                ^^^^^^^^
				Type mismatch: cannot convert from element type String to Thread
				----------
				""");
}
public void test024() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				
				public class X {
					public static void main(String[] args) {
						String[] tab = new String[] {"SUCCESS"};
						List list = new ArrayList();
						for (String arg : tab) {	\t
							list.add(arg);
						}
						for (Object arg: list) {
							System.out.print(arg);
						}
					}
				}""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 4, Locals: 7
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  anewarray java.lang.String [16]
		     4  dup
		     5  iconst_0
		     6  ldc <String "SUCCESS"> [18]
		     8  aastore
		     9  astore_1 [tab]
		    10  new java.util.ArrayList [20]
		    13  dup
		    14  invokespecial java.util.ArrayList() [22]
		    17  astore_2 [list]
		    18  aload_1 [tab]
		    19  dup
		    20  astore 6
		    22  arraylength
		    23  istore 5
		    25  iconst_0
		    26  istore 4
		    28  goto 48
		    31  aload 6
		    33  iload 4
		    35  aaload
		    36  astore_3 [arg]
		    37  aload_2 [list]
		    38  aload_3 [arg]
		    39  invokeinterface java.util.List.add(java.lang.Object) : boolean [23] [nargs: 2]
		    44  pop
		    45  iinc 4 1
		    48  iload 4
		    50  iload 5
		    52  if_icmplt 31
		    55  aload_2 [list]
		    56  invokeinterface java.util.List.iterator() : java.util.Iterator [29] [nargs: 1]
		    61  astore 4
		    63  goto 81
		    66  aload 4
		    68  invokeinterface java.util.Iterator.next() : java.lang.Object [33] [nargs: 1]
		    73  astore_3 [arg]
		    74  getstatic java.lang.System.out : java.io.PrintStream [39]
		    77  aload_3 [arg]
		    78  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [45]
		    81  aload 4
		    83  invokeinterface java.util.Iterator.hasNext() : boolean [51] [nargs: 1]
		    88  ifne 66
		    91  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 10, line: 7]
		        [pc: 18, line: 8]
		        [pc: 37, line: 9]
		        [pc: 45, line: 8]
		        [pc: 55, line: 11]
		        [pc: 74, line: 12]
		        [pc: 81, line: 11]
		        [pc: 91, line: 14]
		      Local variable table:
		        [pc: 0, pc: 92] local: args index: 0 type: java.lang.String[]
		        [pc: 10, pc: 92] local: tab index: 1 type: java.lang.String[]
		        [pc: 18, pc: 92] local: list index: 2 type: java.util.List
		        [pc: 37, pc: 45] local: arg index: 3 type: java.lang.String
		        [pc: 74, pc: 81] local: arg index: 3 type: java.lang.Object
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
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				
				public class X {
					public static void bug(List<String> lines) {
				        for (int i=0; i<1; i++) {
				           for (String test: lines) {
				                System.out.print(test);
				           }
				        }
				    }
				    public static void main(String[] args) {
				    	ArrayList<String> tab = new ArrayList<String>();
				    	tab.add("SUCCESS");
				    	bug(tab);
				    }
				}""",
		},
		"SUCCESS");
}
// 68440 - verify error due to local variable invalid slot sharing
public void test026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    Object[] array = {
				    };
				    void test() {
				        for (Object object : array) {
				            String str = object.toString();
				            str += "";
				        }
				    }
				    public static void main(String[] args) {
				        new X().test();
						System.out.println("SUCCESS");
				    }
				}
				""",
		},
		"SUCCESS");
}
// 68863 - missing local variable attribute after foreach statement
public void test027() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				    Object[] array = {
				    };
						java.util.ArrayList i;\t
						for (Object object : array) {
							if (args == null) {
								i = null;
								break;
							}
							return;
						};
						System.out.println("SUCCESS");\t
					}
				}
				""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_0
		     1  anewarray java.lang.Object [3]
		     4  astore_1 [array]
		     5  aload_1 [array]
		     6  dup
		     7  astore_2
		     8  arraylength
		     9  ifeq 22
		    12  aload_0 [args]
		    13  ifnonnull 21
		    16  aconst_null
		    17  pop
		    18  goto 22
		    21  return
		    22  getstatic java.lang.System.out : java.io.PrintStream [16]
		    25  ldc <String "SUCCESS"> [22]
		    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    30  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 5, line: 6]
		        [pc: 12, line: 7]
		        [pc: 16, line: 8]
		        [pc: 18, line: 9]
		        [pc: 21, line: 11]
		        [pc: 22, line: 13]
		        [pc: 30, line: 14]
		      Local variable table:
		        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]
		        [pc: 5, pc: 31] local: array index: 1 type: java.lang.Object[]
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
//72760 - missing local variable attribute after foreach statement
public void test028() throws Exception {
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
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 5
		  public static void main(java.lang.String[] args);
		      0  new java.util.ArrayList [16]
		      3  dup
		      4  invokespecial java.util.ArrayList() [18]
		      7  astore_1 [slist]
		      8  aload_1 [slist]
		      9  new java.util.ArrayList [16]
		     12  dup
		     13  invokespecial java.util.ArrayList() [18]
		     16  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		     19  pop
		     20  aload_1 [slist]
		     21  iconst_0
		     22  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		     25  checkcast java.util.ArrayList [16]
		     28  ldc <String "SU"> [27]
		     30  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		     33  pop
		     34  aload_1 [slist]
		     35  iconst_0
		     36  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		     39  checkcast java.util.ArrayList [16]
		     42  ldc <String "C"> [29]
		     44  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		     47  pop
		     48  aload_1 [slist]
		     49  iconst_0
		     50  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		     53  checkcast java.util.ArrayList [16]
		     56  ldc <String "C"> [29]
		     58  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		     61  pop
		     62  aload_1 [slist]
		     63  new java.util.ArrayList [16]
		     66  dup
		     67  invokespecial java.util.ArrayList() [18]
		     70  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		     73  pop
		     74  aload_1 [slist]
		     75  iconst_1
		     76  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		     79  checkcast java.util.ArrayList [16]
		     82  ldc <String "E"> [31]
		     84  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		     87  pop
		     88  aload_1 [slist]
		     89  iconst_1
		     90  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		     93  checkcast java.util.ArrayList [16]
		     96  ldc <String "S"> [33]
		     98  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		    101  pop
		    102  aload_1 [slist]
		    103  iconst_1
		    104  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		    107  checkcast java.util.ArrayList [16]
		    110  ldc <String "S"> [33]
		    112  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]
		    115  pop
		    116  iconst_0
		    117  istore_2 [i]
		    118  goto 168
		    121  aload_1 [slist]
		    122  iload_2 [i]
		    123  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]
		    126  checkcast java.util.ArrayList [16]
		    129  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [35]
		    132  astore 4
		    134  goto 155
		    137  aload 4
		    139  invokeinterface java.util.Iterator.next() : java.lang.Object [39] [nargs: 1]
		    144  checkcast java.lang.String [45]
		    147  astore_3 [s]
		    148  getstatic java.lang.System.out : java.io.PrintStream [47]
		    151  aload_3 [s]
		    152  invokevirtual java.io.PrintStream.print(java.lang.String) : void [53]
		    155  aload 4
		    157  invokeinterface java.util.Iterator.hasNext() : boolean [59] [nargs: 1]
		    162  ifne 137
		    165  iinc 2 1 [i]
		    168  iload_2 [i]
		    169  aload_1 [slist]
		    170  invokevirtual java.util.ArrayList.size() : int [63]
		    173  if_icmplt 121
		    176  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 8, line: 8]
		        [pc: 20, line: 9]
		        [pc: 34, line: 10]
		        [pc: 48, line: 11]
		        [pc: 62, line: 13]
		        [pc: 74, line: 14]
		        [pc: 88, line: 15]
		        [pc: 102, line: 16]
		        [pc: 116, line: 18]
		        [pc: 121, line: 19]
		        [pc: 148, line: 20]
		        [pc: 155, line: 19]
		        [pc: 165, line: 18]
		        [pc: 176, line: 23]
		      Local variable table:
		        [pc: 0, pc: 177] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 177] local: slist index: 1 type: java.util.ArrayList
		        [pc: 118, pc: 176] local: i index: 2 type: int
		        [pc: 148, pc: 155] local: s index: 3 type: java.lang.String
		      Local variable type table:
		        [pc: 8, pc: 177] local: slist index: 1 type: java.util.ArrayList<java.util.ArrayList<java.lang.String>>
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
				
				    public static void main(String args[]) {
				        ArrayList<Integer> arr = new ArrayList<Integer>();
				    	 arr.add(0);
				    	 arr.add(1);
						 int counter = 0;
				        // tested statement:
				        for (int i : arr){
				            ++counter;
				        }
				        System.out.print("SUCCESS");
				    }
				}""",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
				
				    public static void main(String args[]) {
				        int[] arr = new int[2];
				    	 arr[0]= 0;
				    	 arr[1]= 1;
						 int counter = 0;
				        // tested statement:
				        for (int i : arr){
				            ++counter;
				        }
				        System.out.print("SUCCESS");
				    }
				}""",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
				
				    public static void main(String args[]) {
				        ArrayList arr = new ArrayList();
				    	 arr.add(new Object());
						 int counter = 0;
				        // tested statement:
				        for (Object o : arr){
				            ++counter;
				        }
				        System.out.print("SUCCESS");
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
				public class X<T> {
					abstract class Member implements Iterable<String> {
					}
					void foo(Member m) {
						for(String s : m) {
							return;
						}\s
					}
				}
				""",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					abstract class Member implements Iterable<String> {
					}
					void foo(Member m) {
						for(String s : m) {
							return;
						}\s
					}
				}
				""",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783 - variation
public void test034() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				
				public class X <T extends Bar> {
					public static void main(String[] args) {
						new X<Bar>().foo(new Bar());
					}
					void foo(T t) {
						for (String s : t) {
							System.out.print(s);
						}
						System.out.println();
					}
				}
				class ArrayIterator<T> implements Iterator<T> {
					T[] values;
					int count;
					ArrayIterator(T[] values) {
						this.values = values;
						this.count = 0;
					}
					public boolean hasNext() {
						return this.count < this.values.length;
					}
					public T next() {
						if (this.count >= this.values.length) throw new NoSuchElementException();
						T value = this.values[this.count];
						this.values[this.count++] = null; // clear
						return value;
					}
					public void remove() {
					}
				}
				class Bar implements Iterable<String> {
					public Iterator<String> iterator() {
						return new ArrayIterator<String>(new String[]{"a","b"});
					}
				}
				""",
		},
		"ab");
	// 	ensure proper declaring class (Bar): 1  invokevirtual Bar.iterator() : java.util.Iterator  [33]
	String expectedOutput =
		"""
		  // Method descriptor #25 (LBar;)V
		  // Signature: (TT;)V
		  // Stack: 2, Locals: 4
		  void foo(Bar t);
		     0  aload_1 [t]
		     1  invokevirtual Bar.iterator() : java.util.Iterator [30]
		     4  astore_3
		     5  goto 25
		     8  aload_3
		     9  invokeinterface java.util.Iterator.next() : java.lang.Object [34] [nargs: 1]
		    14  checkcast java.lang.String [40]
		    17  astore_2 [s]
		    18  getstatic java.lang.System.out : java.io.PrintStream [42]
		    21  aload_2 [s]
		    22  invokevirtual java.io.PrintStream.print(java.lang.String) : void [48]
		    25  aload_3
		    26  invokeinterface java.util.Iterator.hasNext() : boolean [54] [nargs: 1]
		    31  ifne 8
		    34  getstatic java.lang.System.out : java.io.PrintStream [42]
		    37  invokevirtual java.io.PrintStream.println() : void [58]
		    40  return
		      Line numbers:
		        [pc: 0, line: 8]
		        [pc: 18, line: 9]
		        [pc: 25, line: 8]
		        [pc: 34, line: 11]
		        [pc: 40, line: 12]
		      Local variable table:
		        [pc: 0, pc: 41] local: this index: 0 type: X
		        [pc: 0, pc: 41] local: t index: 1 type: Bar
		        [pc: 18, pc: 25] local: s index: 2 type: java.lang.String
		      Local variable type table:
		        [pc: 0, pc: 41] local: this index: 0 type: X<T>
		        [pc: 0, pc: 41] local: t index: 1 type: T
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783 - variation
public void test035() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				
				public class X <T extends IFoo> {
					public static void main(String[] args) {
						new X<IFoo>().foo(new Bar());
					}
					void foo(T t) {
						for (String s : t) {
							System.out.print(s);
						}
						System.out.println();
					}
				}
				class ArrayIterator<T> implements Iterator<T> {
					T[] values;
					int count;
					ArrayIterator(T[] values) {
						this.values = values;
						this.count = 0;
					}
					public boolean hasNext() {
						return this.count < this.values.length;
					}
					public T next() {
						if (this.count >= this.values.length) throw new NoSuchElementException();
						T value = this.values[this.count];
						this.values[this.count++] = null; // clear
						return value;
					}
					public void remove() {
					}
				}
				interface IFoo extends Iterable<String> {
				}
				class Bar implements IFoo {
					public Iterator<String> iterator() {
						return new ArrayIterator<String>(new String[]{"a","b"});
					}
				}
				""",
		},
		"ab");
	// 	ensure proper declaring class (IFoo): 1  invokeinterface IFoo.iterator() : java.util.Iterator  [35] [nargs: 1]
	String expectedOutput =
		"""
		  // Method descriptor #25 (LIFoo;)V
		  // Signature: (TT;)V
		  // Stack: 2, Locals: 4
		  void foo(IFoo t);
		     0  aload_1 [t]
		     1  invokeinterface IFoo.iterator() : java.util.Iterator [30] [nargs: 1]
		     6  astore_3
		     7  goto 27
		    10  aload_3
		    11  invokeinterface java.util.Iterator.next() : java.lang.Object [36] [nargs: 1]
		    16  checkcast java.lang.String [42]
		    19  astore_2 [s]
		    20  getstatic java.lang.System.out : java.io.PrintStream [44]
		    23  aload_2 [s]
		    24  invokevirtual java.io.PrintStream.print(java.lang.String) : void [50]
		    27  aload_3
		    28  invokeinterface java.util.Iterator.hasNext() : boolean [56] [nargs: 1]
		    33  ifne 10
		    36  getstatic java.lang.System.out : java.io.PrintStream [44]
		    39  invokevirtual java.io.PrintStream.println() : void [60]
		    42  return
		      Line numbers:
		        [pc: 0, line: 8]
		        [pc: 20, line: 9]
		        [pc: 27, line: 8]
		        [pc: 36, line: 11]
		        [pc: 42, line: 12]
		      Local variable table:
		        [pc: 0, pc: 43] local: this index: 0 type: X
		        [pc: 0, pc: 43] local: t index: 1 type: IFoo
		        [pc: 20, pc: 27] local: s index: 2 type: java.lang.String
		      Local variable type table:
		        [pc: 0, pc: 43] local: this index: 0 type: X<T>
		        [pc: 0, pc: 43] local: t index: 1 type: T
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783
public void test036() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.Iterator;
				import java.util.List;
				
				public class X implements Iterable<String>, Runnable {
					public <T extends Runnable & Iterable<String>> void foo(T t) {
						for (String s : t)
							System.out.print(s);
					}
					public void run() {	/* */ }
					private List<String> list = Arrays.asList(new String[] { "a", "b" });
					public Iterator<String> iterator() {
						return this.list.iterator();
					}
					public static void main(String... args) {
						X x = new X();
						x.foo(x);
					}
				}""",
		},
		"ab");
	String expectedOutput =
		"""
		  // Method descriptor #37 (Ljava/lang/Runnable;)V
		  // Signature: <T::Ljava/lang/Runnable;:Ljava/lang/Iterable<Ljava/lang/String;>;>(TT;)V
		  // Stack: 2, Locals: 4
		  public void foo(java.lang.Runnable t);
		     0  aload_1 [t]
		     1  checkcast java.lang.Iterable [5]
		     4  invokeinterface java.lang.Iterable.iterator() : java.util.Iterator [39] [nargs: 1]
		     9  astore_3
		    10  goto 30
		    13  aload_3
		    14  invokeinterface java.util.Iterator.next() : java.lang.Object [43] [nargs: 1]
		    19  checkcast java.lang.String [18]
		    22  astore_2 [s]
		    23  getstatic java.lang.System.out : java.io.PrintStream [49]
		    26  aload_2 [s]
		    27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [55]
		    30  aload_3
		    31  invokeinterface java.util.Iterator.hasNext() : boolean [61] [nargs: 1]
		    36  ifne 13
		    39  return
		      Line numbers:
		        [pc: 0, line: 7]
		        [pc: 23, line: 8]
		        [pc: 30, line: 7]
		        [pc: 39, line: 9]
		      Local variable table:
		        [pc: 0, pc: 40] local: this index: 0 type: X
		        [pc: 0, pc: 40] local: t index: 1 type: java.lang.Runnable
		        [pc: 23, pc: 30] local: s index: 2 type: java.lang.String
		      Local variable type table:
		        [pc: 0, pc: 40] local: t index: 1 type: T
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783
public void test037() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.Iterator;
				import java.util.List;
				import java.util.ArrayList;
				
				public class X {
					public static <T extends ArrayList<String>> void foo(T t) {
						for (String s : t)
							System.out.print(s);
					}
					private static ArrayList<String> list = new ArrayList<String>();
					static {
						list.addAll(Arrays.asList(new String[] { "a", "b" }));
					}
					public static void main(String... args) {
						foo(list);
					}
				}""",
		},
		"ab");

	String expectedOutput =
		"""
		  // Method descriptor #41 (Ljava/util/ArrayList;)V
		  // Signature: <T:Ljava/util/ArrayList<Ljava/lang/String;>;>(TT;)V
		  // Stack: 2, Locals: 3
		  public static void foo(java.util.ArrayList t);
		     0  aload_0 [t]
		     1  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [43]
		     4  astore_2
		     5  goto 25
		     8  aload_2
		     9  invokeinterface java.util.Iterator.next() : java.lang.Object [47] [nargs: 1]
		    14  checkcast java.lang.String [19]
		    17  astore_1 [s]
		    18  getstatic java.lang.System.out : java.io.PrintStream [53]
		    21  aload_1 [s]
		    22  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]
		    25  aload_2
		    26  invokeinterface java.util.Iterator.hasNext() : boolean [65] [nargs: 1]
		    31  ifne 8
		    34  return
		      Line numbers:
		        [pc: 0, line: 8]
		        [pc: 18, line: 9]
		        [pc: 25, line: 8]
		        [pc: 34, line: 10]
		      Local variable table:
		        [pc: 0, pc: 35] local: t index: 0 type: java.util.ArrayList
		        [pc: 18, pc: 25] local: s index: 1 type: java.lang.String
		      Local variable type table:
		        [pc: 0, pc: 35] local: t index: 0 type: T
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=119175
public void test038() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashSet;
				public class X {
					public static void main(String[] args) {
						X x = new X();
						x.foo();
						System.out.println("SUCCESS");\t
					}
					public void foo() {
					    for(Object o : new HashSet<Object>()) {
					    	System.out.println(o);
					    	continue;
					    }
					}
				}""",
		},
		"SUCCESS");

	String expectedOutput =
		"""
		  // Method descriptor #6 ()V
		  // Stack: 2, Locals: 3
		  public void foo();
		     0  new java.util.HashSet [37]
		     3  dup
		     4  invokespecial java.util.HashSet() [39]
		     7  invokevirtual java.util.HashSet.iterator() : java.util.Iterator [40]
		    10  astore_2
		    11  goto 28
		    14  aload_2
		    15  invokeinterface java.util.Iterator.next() : java.lang.Object [44] [nargs: 1]
		    20  astore_1 [o]
		    21  getstatic java.lang.System.out : java.io.PrintStream [20]
		    24  aload_1 [o]
		    25  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [50]
		    28  aload_2
		    29  invokeinterface java.util.Iterator.hasNext() : boolean [53] [nargs: 1]
		    34  ifne 14
		    37  return
		      Line numbers:
		        [pc: 0, line: 9]
		        [pc: 21, line: 10]
		        [pc: 28, line: 9]
		        [pc: 37, line: 13]
		      Local variable table:
		        [pc: 0, pc: 38] local: this index: 0 type: X
		        [pc: 21, pc: 28] local: o index: 1 type: java.lang.Object
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test039() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashSet;
				import java.util.Set;
				import java.util.Iterator;
				
				public class X {
				
				        public static void main(String[] args) {
				                for (Object o : initForEach()) {
				                }
				        }
				
						static class MyIterator<T> implements Iterator<T> {
							Iterator<T> iterator;
						\t
							MyIterator(Iterator<T> it) {
								this.iterator = it;
							}
							public boolean hasNext() {
								System.out.println("hasNext");
								return this.iterator.hasNext();
							}		\t
							public T next() {
								System.out.println("next");
								return this.iterator.next();
							}
							public void remove() {
								System.out.println("remove");
								this.iterator.remove();
							}
						}
					\t
				        static Set<Object> initForEach()        {
				                System.out.println("initForEach");
				                HashSet<Object> set = new HashSet<Object>() {
				                	private static final long serialVersionUID = 1L;
				                	public Iterator<Object> iterator() {
				                		System.out.println("iterator");
				                		return new MyIterator<Object>(super.iterator());
				                	}
				                };
				                for (int i = 0; i < 3; i++) set.add(i);
				                return set;
				        }
				}""",
		},
		"""
			initForEach
			iterator
			hasNext
			next
			hasNext
			next
			hasNext
			next
			hasNext""");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 1, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  invokestatic X.initForEach() : java.util.Set [16]
		     3  invokeinterface java.util.Set.iterator() : java.util.Iterator [20] [nargs: 1]
		     8  astore_1
		     9  goto 19
		    12  aload_1
		    13  invokeinterface java.util.Iterator.next() : java.lang.Object [26] [nargs: 1]
		    18  pop
		    19  aload_1
		    20  invokeinterface java.util.Iterator.hasNext() : boolean [32] [nargs: 1]
		    25  ifne 12
		    28  return
		      Line numbers:
		        [pc: 0, line: 8]
		        [pc: 28, line: 10]
		      Local variable table:
		        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test040() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashSet;
				import java.util.Set;
				import java.util.Iterator;
				
				public class X {
				
				        public static void main(String[] args) {
				                for (Object o : initForEach()) {
				                }
				        }
				
						static class MyIterator<T> implements Iterator<T> {
							Iterator<T> iterator;
						\t
							MyIterator(Iterator<T> it) {
								this.iterator = it;
							}
							public boolean hasNext() {
								System.out.println("hasNext");
								return this.iterator.hasNext();
							}		\t
							public T next() {
								System.out.println("next");
								return this.iterator.next();
							}
							public void remove() {
								System.out.println("remove");
								this.iterator.remove();
							}
						}
					\t
				        static Set<Object> initForEach()        {
				                System.out.println("initForEach");
				                HashSet<Object> set = new HashSet<Object>() {
				                	private static final long serialVersionUID = 1L;
				                	public Iterator<Object> iterator() {
				                		System.out.println("iterator");
				                		return new MyIterator<Object>(super.iterator());
				                	}
				                };
				                for (int i = 0; i < 3; i++) set.add(i);
				                return set;
				        }
				}""",
		},
		"""
			initForEach
			iterator
			hasNext
			next
			hasNext
			next
			hasNext
			next
			hasNext""",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 1, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  invokestatic X.initForEach() : java.util.Set [16]
		     3  invokeinterface java.util.Set.iterator() : java.util.Iterator [20] [nargs: 1]
		     8  astore_2
		     9  goto 19
		    12  aload_2
		    13  invokeinterface java.util.Iterator.next() : java.lang.Object [26] [nargs: 1]
		    18  astore_1
		    19  aload_2
		    20  invokeinterface java.util.Iterator.hasNext() : boolean [32] [nargs: 1]
		    25  ifne 12
		    28  return
		      Line numbers:
		        [pc: 0, line: 8]
		        [pc: 28, line: 10]
		      Local variable table:
		        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test041() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                for (int i : initForEach()) {
				                }
				        }
				        static int[] initForEach() {
				                System.out.println("initForEach");
				                return new int[] {1, 2, 3, 4};
				        }
				}""",
		},
		"initForEach");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 1, Locals: 1
		  public static void main(java.lang.String[] args);
		    0  invokestatic X.initForEach() : int[] [16]
		    3  pop
		    4  return
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test042() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                for (int i : initForEach()) {
				                }
				        }
				        static int[] initForEach() {
				                System.out.println("initForEach");
				                return new int[] {1, 2, 3, 4};
				        }
				}""",
		},
		"initForEach",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 5
		  public static void main(java.lang.String[] args);
		     0  invokestatic X.initForEach() : int[] [16]
		     3  dup
		     4  astore 4
		     6  arraylength
		     7  istore_3
		     8  iconst_0
		     9  istore_2
		    10  goto 21
		    13  aload 4
		    15  iload_2
		    16  iaload
		    17  istore_1
		    18  iinc 2 1
		    21  iload_2
		    22  iload_3
		    23  if_icmplt 13
		    26  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 26, line: 5]
		      Local variable table:
		        [pc: 0, pc: 27] local: args index: 0 type: java.lang.String[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test043() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
							 foo();
				        }
				        public static void foo() {
				                for (int i : initForEach()) {
				                }
				        }
				        static int[] initForEach() {
				                System.out.println("initForEach");
				                return new int[] {1, 2, 3, 4};
				        }
				}""",
		},
		"initForEach");

	String expectedOutput =
		"""
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 0
		  public static void foo();
		    0  invokestatic X.initForEach() : int[] [21]
		    3  pop
		    4  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test044() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
							 foo();
				        }
				        public static void foo() {
				                for (int i : initForEach()) {
				                }
				        }
				        static int[] initForEach() {
				                System.out.println("initForEach");
				                return new int[] {1, 2, 3, 4};
				        }
				}""",
		},
		"initForEach",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"""
		  // Method descriptor #6 ()V
		  // Stack: 2, Locals: 4
		  public static void foo();
		     0  invokestatic X.initForEach() : int[] [21]
		     3  dup
		     4  astore_3
		     5  arraylength
		     6  istore_2
		     7  iconst_0
		     8  istore_1
		     9  goto 19
		    12  aload_3
		    13  iload_1
		    14  iaload
		    15  istore_0
		    16  iinc 1 1
		    19  iload_1
		    20  iload_2
		    21  if_icmplt 12
		    24  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 24, line: 8]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test045() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                for (int i : initForEach()) {
				                	System.out.print(\'a\');
				                }
				        }
				        static int[] initForEach() {
				                return new int[] {1, 2, 3, 4};
				        }
				}""",
		},
		"aaaa",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 5
		  public static void main(java.lang.String[] args);
		     0  invokestatic X.initForEach() : int[] [16]
		     3  dup
		     4  astore 4
		     6  arraylength
		     7  istore_3
		     8  iconst_0
		     9  istore_2
		    10  goto 29
		    13  aload 4
		    15  iload_2
		    16  iaload
		    17  istore_1 [i]
		    18  getstatic java.lang.System.out : java.io.PrintStream [20]
		    21  bipush 97
		    23  invokevirtual java.io.PrintStream.print(char) : void [26]
		    26  iinc 2 1
		    29  iload_2
		    30  iload_3
		    31  if_icmplt 13
		    34  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 18, line: 4]
		        [pc: 26, line: 3]
		        [pc: 34, line: 6]
		      Local variable table:
		        [pc: 0, pc: 35] local: args index: 0 type: java.lang.String[]
		        [pc: 18, pc: 26] local: i index: 1 type: int
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test046() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                for (int i : initForEach()) {
				                	System.out.print(\'a\');
				                }
				        }
				        static int[] initForEach() {
				                return new int[] {1, 2, 3, 4};
				        }
				}""",
		},
		"aaaa",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 4
		  public static void main(java.lang.String[] args);
		     0  invokestatic X.initForEach() : int[] [16]
		     3  dup
		     4  astore_3
		     5  arraylength
		     6  istore_2
		     7  iconst_0
		     8  istore_1
		     9  goto 23
		    12  getstatic java.lang.System.out : java.io.PrintStream [20]
		    15  bipush 97
		    17  invokevirtual java.io.PrintStream.print(char) : void [26]
		    20  iinc 1 1
		    23  iload_1
		    24  iload_2
		    25  if_icmplt 12
		    28  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 12, line: 4]
		        [pc: 20, line: 3]
		        [pc: 28, line: 6]
		      Local variable table:
		        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471
public void test047() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo3(int[] array) {
						for (int i : array) {
							System.out.println(i);
							break;
						}
					}
				}
				""", // =================
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 ([I)V
		  // Stack: 2, Locals: 4
		  void foo3(int[] array);
		     0  aload_1 [array]
		     1  dup
		     2  astore_3
		     3  arraylength
		     4  ifeq 18
		     7  aload_3
		     8  iconst_0
		     9  iaload
		    10  istore_2 [i]
		    11  getstatic java.lang.System.out : java.io.PrintStream [16]
		    14  iload_2 [i]
		    15  invokevirtual java.io.PrintStream.println(int) : void [22]
		    18  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 11, line: 4]
		        [pc: 18, line: 7]
		      Local variable table:
		        [pc: 0, pc: 19] local: this index: 0 type: X
		        [pc: 0, pc: 19] local: array index: 1 type: int[]
		        [pc: 11, pc: 18] local: i index: 2 type: int
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test048() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo3(java.util.List<String> ls) {
						for (String s : ls) {
							System.out.println(s);
							break;
						}
					}
				}
				""", // =================
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 (Ljava/util/List;)V
		  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V
		  // Stack: 2, Locals: 4
		  void foo3(java.util.List ls);
		     0  aload_1 [ls]
		     1  invokeinterface java.util.List.iterator() : java.util.Iterator [18] [nargs: 1]
		     6  astore_3
		     7  aload_3
		     8  invokeinterface java.util.Iterator.hasNext() : boolean [24] [nargs: 1]
		    13  ifeq 33
		    16  aload_3
		    17  invokeinterface java.util.Iterator.next() : java.lang.Object [30] [nargs: 1]
		    22  checkcast java.lang.String [34]
		    25  astore_2 [s]
		    26  getstatic java.lang.System.out : java.io.PrintStream [36]
		    29  aload_2 [s]
		    30  invokevirtual java.io.PrintStream.println(java.lang.String) : void [42]
		    33  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 26, line: 4]
		        [pc: 33, line: 7]
		      Local variable table:
		        [pc: 0, pc: 34] local: this index: 0 type: X
		        [pc: 0, pc: 34] local: ls index: 1 type: java.util.List
		        [pc: 26, pc: 33] local: s index: 2 type: java.lang.String
		      Local variable type table:
		        [pc: 0, pc: 34] local: ls index: 1 type: java.util.List<java.lang.String>
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test049() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo3(java.util.List l) {
						for (Object o : l) {
							System.out.println(o);
							break;
						}
					}
				}
				""", // =================
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #15 (Ljava/util/List;)V
		  // Stack: 2, Locals: 4
		  void foo3(java.util.List l);
		     0  aload_1 [l]
		     1  invokeinterface java.util.List.iterator() : java.util.Iterator [16] [nargs: 1]
		     6  astore_3
		     7  aload_3
		     8  invokeinterface java.util.Iterator.hasNext() : boolean [22] [nargs: 1]
		    13  ifeq 30
		    16  aload_3
		    17  invokeinterface java.util.Iterator.next() : java.lang.Object [28] [nargs: 1]
		    22  astore_2 [o]
		    23  getstatic java.lang.System.out : java.io.PrintStream [32]
		    26  aload_2 [o]
		    27  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [38]
		    30  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 23, line: 4]
		        [pc: 30, line: 7]
		      Local variable table:
		        [pc: 0, pc: 31] local: this index: 0 type: X
		        [pc: 0, pc: 31] local: l index: 1 type: java.util.List
		        [pc: 23, pc: 30] local: o index: 2 type: java.lang.Object
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test050() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					private T values;
					public X(T values) {
						this.values = values;
					}
					public T getValues() {
						return values;
					}
					public static void main(String[] args) {
						X<short[]> x = new X<short[]>(new short[] { 1, 2, 3, 4, 5 });
						for (int i : x.getValues()) {
							System.out.print(i);
						}
					}
				}""",
		},
		"12345");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test051() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					private T values;
					public X(T values) {
						this.values = values;
					}
					public T getValues() {
						return values;
					}
					public static void main(String[] args) {
						X<short[]> x = new X<short[]>(new short[] { 1, 2, 3, 4, 5 });
						for (long l : x.getValues()) {
							System.out.print(l);
						}
					}
				}""",
		},
		"12345");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test052() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					private T values;
					public X(T values) {
						this.values = values;
					}
					public T getValues() {
						return values;
					}
					public static void main(String[] args) {
						X<Short[]> x = new X<Short[]>(new Short[] { 1, 2, 3, 4, 5 });
						for (int i : x.getValues()) {
							System.out.print(i);
						}
					}
				}""",
		},
		"12345");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test053() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					private T values;
					public X(T values) {
						this.values = values;
					}
					public T getValues() {
						return values;
					}
					public static void main(String[] args) {
						X<Short[]> x = new X<Short[]>(new Short[] { 1, 2, 3, 4, 5 });
						for (long i : x.getValues()) {
							System.out.print(i);
						}
					}
				}""",
		},
		"12345");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321085
public void test054() throws Exception {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.HashSet;
					import java.util.Set;
					public class X {
					    void foo() {
					       HashSet<String> x = new HashSet<String>();
					        x.add("a");
					        HashSet<Integer> y = new HashSet<Integer>();
					        y.add(1);
					        Set<String> [] OK= new Set[] { x, y };
					        for (Set<String> BUG : new Set[] { x, y }) {
					            for (String str : BUG)
					                System.out.println(str);
					        }
					        Set [] set = new Set[] { x, y };
					        for (Set<String> BUG : set) {
					            for (String str : BUG)
					                System.out.println(str);
					        }
					    }
					    Zork z;
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 9)
					Set<String> [] OK= new Set[] { x, y };
					                   ^^^^^^^^^^^^^^^^^^
				Type safety: The expression of type Set[] needs unchecked conversion to conform to Set<String>[]
				----------
				2. WARNING in X.java (at line 10)
					for (Set<String> BUG : new Set[] { x, y }) {
					                       ^^^^^^^^^^^^^^^^^^
				Type safety: Elements of type Set need unchecked conversion to conform to Set<String>
				----------
				3. WARNING in X.java (at line 14)
					Set [] set = new Set[] { x, y };
					^^^
				Set is a raw type. References to generic type Set<E> should be parameterized
				----------
				4. WARNING in X.java (at line 15)
					for (Set<String> BUG : set) {
					                       ^^^
				Type safety: Elements of type Set need unchecked conversion to conform to Set<String>
				----------
				5. ERROR in X.java (at line 20)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/393719
// like test054 but suppressing the warnings.
public void test055() throws Exception {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.HashSet;
					import java.util.Set;
					public class X {
					    void foo() {
					       HashSet<String> x = new HashSet<String>();
					        x.add("a");
					        HashSet<Integer> y = new HashSet<Integer>();
					        y.add(1);
					        @SuppressWarnings("unchecked") Set<String> [] OK= new Set[] { x, y };
					        for (@SuppressWarnings("unchecked") Set<String> BUG : new Set[] { x, y }) {
					            for (String str : BUG)
					                System.out.println(str);
					        }
					        @SuppressWarnings({"rawtypes", "unchecked"}) Set [] set = new Set[] { x, y };
					        for (@SuppressWarnings("unchecked") Set<String> BUG : set) {
					            for (String str : BUG)
					                System.out.println(str);
					        }
					    }
					    Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 20)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/393719
// "unchecked" warning against the collection (raw Iterable)
public void test056() throws Exception {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					    void testRawType(@SuppressWarnings("rawtypes") List<List> lists) {
							List<String> stringList = lists.get(0); // (1)
							for (List<String> strings : lists)      // (2)
								stringList = strings;
							for (@SuppressWarnings("unchecked") List<String> strings : lists) // no warning
								stringList = strings;
							System.out.println(stringList.get(0));
						 }
					    Zork z;
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					List<String> stringList = lists.get(0); // (1)
					                          ^^^^^^^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				2. WARNING in X.java (at line 5)
					for (List<String> strings : lists)      // (2)
					                            ^^^^^
				Type safety: Elements of type List need unchecked conversion to conform to List<String>
				----------
				3. ERROR in X.java (at line 11)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401853
// Eclipse Java compiler creates invalid bytecode (java.lang.VerifyError)
public void test057() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
					public static void main(String[] argv) {
						for (long l : new ArrayList<Long>()) {}
					}
				}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"""
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 3]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 2
		  public static void main(java.lang.String[] argv);
		     0  new java.util.ArrayList [16]
		     3  dup
		     4  invokespecial java.util.ArrayList() [18]
		     7  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [19]
		    10  astore_1
		    11  goto 27
		    14  aload_1
		    15  invokeinterface java.util.Iterator.next() : java.lang.Object [23] [nargs: 1]
		    20  checkcast java.lang.Long [29]
		    23  invokevirtual java.lang.Long.longValue() : long [31]
		    26  pop2
		    27  aload_1
		    28  invokeinterface java.util.Iterator.hasNext() : boolean [35] [nargs: 1]
		    33  ifne 14
		    36  return
		      Line numbers:
		        [pc: 0, line: 5]
		        [pc: 36, line: 6]
		      Local variable table:
		        [pc: 0, pc: 37] local: argv index: 0 type: java.lang.String[]
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425632, [1.8][compiler] Compiler gets the scope of enhanced for loop's expression wrong.
public void test425632() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static int[] i = {1, 2, 3};
						public static void main(String [] args) {
							for (int i : i) {
								System.out.println(i);
							}
						}
					}
					"""
			},
			"1\n2\n3");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=508215
public void testBug508215() throws Exception {
	this.runConformTest(
		new String[] {
				"linenumber/DebugErrorVarargs1Arg.java",
				"package linenumber;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class DebugErrorVarargs1Arg {\n" +
				"	public static void main(String[] args) {\n" +
				"		for (Integer i : Arrays.asList(1)) {\n" +
				"			System.out.println(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"",
		}
	);

	String expectedOutput =
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  anewarray java.lang.Integer [16]\n" +
		"     4  dup\n" +
		"     5  iconst_0\n" +
		"     6  iconst_1\n" +
		"     7  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [18]\n" +
		"    10  aastore\n" +
		"    11  invokestatic java.util.Arrays.asList(java.lang.Object[]) : java.util.List [22]\n" +
		"    14  invokeinterface java.util.List.iterator() : java.util.Iterator [28] [nargs: 1]\n" +
		"    19  astore_2\n" +
		"    20  goto 40\n" +
		"    23  aload_2\n" +
		"    24  invokeinterface java.util.Iterator.next() : java.lang.Object [34] [nargs: 1]\n" +
		"    29  checkcast java.lang.Integer [16]\n" +
		"    32  astore_1 [i]\n" +
		"    33  getstatic java.lang.System.out : java.io.PrintStream [40]\n" +
		"    36  aload_1 [i]\n" +
		"    37  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [46]\n" +
		"    40  aload_2\n" +
		"    41  invokeinterface java.util.Iterator.hasNext() : boolean [52] [nargs: 1]\n" +
		"    46  ifne 23\n" +
		"    49  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 33, line: 7]\n" +
		"        [pc: 40, line: 6]\n" +
		"        [pc: 49, line: 9]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 50] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 33, pc: 40] local: i index: 1 type: java.lang.Integer\n" +
		"";

	File f = new File(OUTPUT_DIR + File.separator + "linenumber" + File.separator + "DebugErrorVarargs1Arg.class");
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
public static Class testClass() {
	return ForeachStatementTest.class;
}
}
