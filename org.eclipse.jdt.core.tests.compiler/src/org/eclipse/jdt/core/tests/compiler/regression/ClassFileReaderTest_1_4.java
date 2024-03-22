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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_1_4 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 78, 79 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_4);
	}
	public static Class testClass() {
		return ClassFileReaderTest_1_4.class;
	}

	public ClassFileReaderTest_1_4(String name) {
		super(name);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=15051
	 */
	public void test001() throws Exception {
		String source =
			"""
			public class A001 {
				private int i = 6;
				public int foo() {
					class A {
						int get() {
							return i;
						}
					}
					return new A().get();
				}
			};""";
		String expectedOutput =
			"""
			  // Method descriptor #19 ()I
			  // Stack: 3, Locals: 1
			  public int foo();
			     0  new A001$1$A [20]
			     3  dup
			     4  aload_0 [this]
			     5  invokespecial A001$1$A(A001) [22]
			     8  invokevirtual A001$1$A.get() : int [25]
			    11  ireturn
			      Line numbers:
			        [pc: 0, line: 9]
			      Local variable table:
			        [pc: 0, pc: 12] local: this index: 0 type: A001
			""";
		checkClassFile("A001", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25188
	 */
	public void test002() throws Exception {
		String source =
			"""
			public class A002 {
				public static void main(String[] args) {
					System.out.println(); /* \\u000d: CARRIAGE RETURN */
					System.out.println();
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 1
			  public static void main(java.lang.String[] args);
			     0  getstatic java.lang.System.out : java.io.PrintStream [16]
			     3  invokevirtual java.io.PrintStream.println() : void [22]
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 6, line: 4]
			        [pc: 12, line: 5]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			""";
		checkClassFile("A002", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26098
	 */
	public void test003() throws Exception {
		String source =
			"""
			public class A003 {
			
				public int bar() {
					return 0;
				}
			\t
				public void foo() {
					System.out.println(bar());
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ()I
			  // Stack: 1, Locals: 1
			  public int bar();
			    0  iconst_0
			    1  ireturn
			      Line numbers:
			        [pc: 0, line: 4]
			      Local variable table:
			        [pc: 0, pc: 2] local: this index: 0 type: A003
			 \s
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  public void foo();
			     0  getstatic java.lang.System.out : java.io.PrintStream [17]
			     3  aload_0 [this]
			     4  invokevirtual A003.bar() : int [23]
			     7  invokevirtual java.io.PrintStream.println(int) : void [25]
			    10  return
			      Line numbers:
			        [pc: 0, line: 8]
			        [pc: 10, line: 9]
			      Local variable table:
			        [pc: 0, pc: 11] local: this index: 0 type: A003
			""";
		checkClassFile("A003", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test004() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 6)\s
					   && !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  bipush 6
			     8  if_icmpne 22
			    11  iload_1 [b]
			    12  ifne 22
			    15  getstatic java.lang.System.out : java.io.PrintStream [16]
			    18  iload_2 [i]
			    19  invokevirtual java.io.PrintStream.println(int) : void [22]
			    22  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 11, line: 6]
			        [pc: 15, line: 7]
			        [pc: 22, line: 9]
			      Local variable table:
			        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 23] local: b index: 1 type: boolean
			        [pc: 5, pc: 23] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test005() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   && true) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpne 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test006() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   && false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			    0  bipush 6
			    2  istore_1 [i]
			    3  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 8]
			      Local variable table:
			        [pc: 0, pc: 4] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 4] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test007() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (true
					   && !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifne 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test008() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (false
					   && !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			    0  iconst_0
			    1  istore_1 [b]
			    2  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 8]
			      Local variable table:
			        [pc: 0, pc: 3] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 3] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test009() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 6)\s
					   || !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  bipush 6
			     8  if_icmpeq 15
			    11  iload_1 [b]
			    12  ifne 22
			    15  getstatic java.lang.System.out : java.io.PrintStream [16]
			    18  iload_2 [i]
			    19  invokevirtual java.io.PrintStream.println(int) : void [22]
			    22  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 11, line: 6]
			        [pc: 15, line: 7]
			        [pc: 22, line: 9]
			      Local variable table:
			        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 23] local: b index: 1 type: boolean
			        [pc: 5, pc: 23] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test010() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   || true) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  getstatic java.lang.System.out : java.io.PrintStream [16]
			     6  iload_1 [i]
			     7  invokevirtual java.io.PrintStream.println(int) : void [22]
			    10  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 6]
			        [pc: 10, line: 8]
			      Local variable table:
			        [pc: 0, pc: 11] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 11] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test011() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   || false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpne 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test012() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (true
					   || !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			    0  iconst_0
			    1  istore_1 [b]
			    2  getstatic java.lang.System.out : java.io.PrintStream [16]
			    5  invokevirtual java.io.PrintStream.println() : void [22]
			    8  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 6]
			        [pc: 8, line: 8]
			      Local variable table:
			        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 9] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test013() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (false
					   || !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifne 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test014() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 6)\s
					   == !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  bipush 6
			     8  if_icmpne 15
			    11  iconst_1
			    12  goto 16
			    15  iconst_0
			    16  iload_1 [b]
			    17  ifeq 24
			    20  iconst_0
			    21  goto 25
			    24  iconst_1
			    25  if_icmpne 35
			    28  getstatic java.lang.System.out : java.io.PrintStream [16]
			    31  iload_2 [i]
			    32  invokevirtual java.io.PrintStream.println(int) : void [22]
			    35  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 16, line: 6]
			        [pc: 28, line: 7]
			        [pc: 35, line: 9]
			      Local variable table:
			        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 36] local: b index: 1 type: boolean
			        [pc: 5, pc: 36] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test015() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   == true) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpne 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test016() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   == false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpeq 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test017() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (true
					   == !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifne 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test018() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (false
					   == !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifeq 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 * http:  //bugs.eclipse.org/bugs/show_bug.cgi?id=26881
	 */
	public void test019() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 5)
						? b : !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  iconst_5
			     7  if_icmpne 17
			    10  iload_1 [b]
			    11  ifeq 28
			    14  goto 21
			    17  iload_1 [b]
			    18  ifne 28
			    21  getstatic java.lang.System.out : java.io.PrintStream [16]
			    24  iload_2 [i]
			    25  invokevirtual java.io.PrintStream.println(int) : void [22]
			    28  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 10, line: 6]
			        [pc: 21, line: 7]
			        [pc: 28, line: 9]
			      Local variable table:
			        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 29] local: b index: 1 type: boolean
			        [pc: 5, pc: 29] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test020() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						>= 5) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iconst_5
			     5  if_icmplt 15
			     8  getstatic java.lang.System.out : java.io.PrintStream [16]
			    11  iload_1 [i]
			    12  invokevirtual java.io.PrintStream.println(int) : void [22]
			    15  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 4, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 8]
			      Local variable table:
			        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 16] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test021() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						>= 0) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iflt 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test022() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (0
						>= i) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  ifgt 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 5]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test023() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						> 0) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  ifle 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test024() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (0
						> i) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  ifge 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 5]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test025() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						> 5) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iconst_5
			     5  if_icmple 15
			     8  getstatic java.lang.System.out : java.io.PrintStream [16]
			    11  iload_1 [i]
			    12  invokevirtual java.io.PrintStream.println(int) : void [22]
			    15  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 4, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 8]
			      Local variable table:
			        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 16] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test026() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						< 0) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  ifge 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test027() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (0
						< i) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  ifle 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 5]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test028() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						< 5) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iconst_5
			     5  if_icmpge 15
			     8  getstatic java.lang.System.out : java.io.PrintStream [16]
			    11  iload_1 [i]
			    12  invokevirtual java.io.PrintStream.println(int) : void [22]
			    15  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 4, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 8]
			      Local variable table:
			        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 16] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test029() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						<= 0) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  ifgt 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test030() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (0
						<= i) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iflt 14
			     7  getstatic java.lang.System.out : java.io.PrintStream [16]
			    10  iload_1 [i]
			    11  invokevirtual java.io.PrintStream.println(int) : void [22]
			    14  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 5]
			        [pc: 7, line: 6]
			        [pc: 14, line: 8]
			      Local variable table:
			        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 15] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test031() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						<= 5) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iconst_5
			     5  if_icmpgt 15
			     8  getstatic java.lang.System.out : java.io.PrintStream [16]
			    11  iload_1 [i]
			    12  invokevirtual java.io.PrintStream.println(int) : void [22]
			    15  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 4, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 8]
			      Local variable table:
			        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 16] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test032() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if (i
						<= 5) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  iconst_5
			     5  if_icmpgt 15
			     8  getstatic java.lang.System.out : java.io.PrintStream [16]
			    11  iload_1 [i]
			    12  invokevirtual java.io.PrintStream.println(int) : void [22]
			    15  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 4, line: 5]
			        [pc: 8, line: 6]
			        [pc: 15, line: 8]
			      Local variable table:
			        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 16] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test033() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 6)\s
					   & !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  bipush 6
			     8  if_icmpne 15
			    11  iconst_1
			    12  goto 16
			    15  iconst_0
			    16  iload_1 [b]
			    17  ifeq 24
			    20  iconst_0
			    21  goto 25
			    24  iconst_1
			    25  iand
			    26  ifeq 36
			    29  getstatic java.lang.System.out : java.io.PrintStream [16]
			    32  iload_2 [i]
			    33  invokevirtual java.io.PrintStream.println(int) : void [22]
			    36  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 16, line: 6]
			        [pc: 29, line: 7]
			        [pc: 36, line: 9]
			      Local variable table:
			        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 37] local: b index: 1 type: boolean
			        [pc: 5, pc: 37] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test034() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   & true) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpne 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test035() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   & false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			    0  bipush 6
			    2  istore_1 [i]
			    3  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 8]
			      Local variable table:
			        [pc: 0, pc: 4] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 4] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test036() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (true
					   & !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifne 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test037() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (false
					   & !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			    0  iconst_0
			    1  istore_1 [b]
			    2  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 8]
			      Local variable table:
			        [pc: 0, pc: 3] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 3] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test038() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 6)\s
					   | !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  bipush 6
			     8  if_icmpne 15
			    11  iconst_1
			    12  goto 16
			    15  iconst_0
			    16  iload_1 [b]
			    17  ifeq 24
			    20  iconst_0
			    21  goto 25
			    24  iconst_1
			    25  ior
			    26  ifeq 36
			    29  getstatic java.lang.System.out : java.io.PrintStream [16]
			    32  iload_2 [i]
			    33  invokevirtual java.io.PrintStream.println(int) : void [22]
			    36  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 16, line: 6]
			        [pc: 29, line: 7]
			        [pc: 36, line: 9]
			      Local variable table:
			        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 37] local: b index: 1 type: boolean
			        [pc: 5, pc: 37] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test039() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   | true) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  getstatic java.lang.System.out : java.io.PrintStream [16]
			     6  iload_1 [i]
			     7  invokevirtual java.io.PrintStream.println(int) : void [22]
			    10  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 6]
			        [pc: 10, line: 8]
			      Local variable table:
			        [pc: 0, pc: 11] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 11] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test040() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   | false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpne 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test041() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (true
					   | !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			    0  iconst_0
			    1  istore_1 [b]
			    2  getstatic java.lang.System.out : java.io.PrintStream [16]
			    5  invokevirtual java.io.PrintStream.println() : void [22]
			    8  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 6]
			        [pc: 8, line: 8]
			      Local variable table:
			        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 9] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test042() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (false
					   | !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifne 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test043() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					int i = 6;
					if ((i == 6)\s
					   ^ !b) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  bipush 6
			     4  istore_2 [i]
			     5  iload_2 [i]
			     6  bipush 6
			     8  if_icmpne 15
			    11  iconst_1
			    12  goto 16
			    15  iconst_0
			    16  iload_1 [b]
			    17  ifeq 24
			    20  iconst_0
			    21  goto 25
			    24  iconst_1
			    25  ixor
			    26  ifeq 36
			    29  getstatic java.lang.System.out : java.io.PrintStream [16]
			    32  iload_2 [i]
			    33  invokevirtual java.io.PrintStream.println(int) : void [22]
			    36  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 4]
			        [pc: 5, line: 5]
			        [pc: 16, line: 6]
			        [pc: 29, line: 7]
			        [pc: 36, line: 9]
			      Local variable table:
			        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 37] local: b index: 1 type: boolean
			        [pc: 5, pc: 37] local: i index: 2 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test044() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   ^ true) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpeq 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test045() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					int i = 6;
					if ((i == 6)\s
					   ^ false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  bipush 6
			     2  istore_1 [i]
			     3  iload_1 [i]
			     4  bipush 6
			     6  if_icmpne 16
			     9  getstatic java.lang.System.out : java.io.PrintStream [16]
			    12  iload_1 [i]
			    13  invokevirtual java.io.PrintStream.println(int) : void [22]
			    16  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 3, line: 4]
			        [pc: 9, line: 6]
			        [pc: 16, line: 8]
			      Local variable table:
			        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
			        [pc: 3, pc: 17] local: i index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test046() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (true
					   ^ !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifeq 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test047() throws Exception {
		String source =
			"""
			public class A {
				public static void main(String[] args) {
					boolean b = false;
					if (false
					   ^ !b) {   \t
					   	System.out.println();
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 1, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_0
			     1  istore_1 [b]
			     2  iload_1 [b]
			     3  ifne 12
			     6  getstatic java.lang.System.out : java.io.PrintStream [16]
			     9  invokevirtual java.io.PrintStream.println() : void [22]
			    12  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 2, line: 5]
			        [pc: 6, line: 6]
			        [pc: 12, line: 8]
			      Local variable table:
			        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]
			        [pc: 2, pc: 13] local: b index: 1 type: boolean
			""";
		checkClassFile("A", source, expectedOutput);
	}

	public void test048() throws Exception {
		String source =
			"""
			public class A {
			
				static int foo(boolean bool) {
				  int j;
				  try {
				    if (bool) return 1;
				    j = 2;
				  } finally {
				    j = 3;
				  }
				  return j;
				}
			
				public static void main(String[] args) {
					foo(false);
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #15 (Z)I
			  // Stack: 1, Locals: 4
			  static int foo(boolean bool);
			     0  iload_0 [bool]
			     1  ifeq 9
			     4  jsr 20
			     7  iconst_1
			     8  ireturn
			     9  iconst_2
			    10  istore_1 [j]
			    11  goto 25
			    14  astore_3
			    15  jsr 20
			    18  aload_3
			    19  athrow
			    20  astore_2
			    21  iconst_3
			    22  istore_1 [j]
			    23  ret 2
			    25  jsr 20
			    28  iload_1 [j]
			    29  ireturn
			      Exception Table:
			        [pc: 0, pc: 7] -> 14 when : any
			        [pc: 9, pc: 14] -> 14 when : any
			        [pc: 25, pc: 28] -> 14 when : any
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 9, line: 7]
			        [pc: 11, line: 8]
			        [pc: 18, line: 10]
			        [pc: 20, line: 8]
			        [pc: 21, line: 9]
			        [pc: 23, line: 10]
			        [pc: 28, line: 11]
			      Local variable table:
			        [pc: 0, pc: 30] local: bool index: 0 type: boolean
			        [pc: 11, pc: 14] local: j index: 1 type: int
			        [pc: 23, pc: 30] local: j index: 1 type: int
			""";
		checkClassFile("A", source, expectedOutput);
	}

	public void test049() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					foo();
				}
				static void foo() {
					int i = 5;
					if ((i == 6) && false) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  static void foo();
			    0  iconst_5
			    1  istore_0 [i]
			    2  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 2, line: 10]
			      Local variable table:
			        [pc: 2, pc: 3] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test050() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					foo();
				}
				static void foo() {
					int i = 5;
					if ((i == 6) && false) {}
					else {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo();
			     0  iconst_5
			     1  istore_0 [i]
			     2  getstatic java.lang.System.out : java.io.PrintStream [21]
			     5  iload_0 [i]
			     6  invokevirtual java.io.PrintStream.println(int) : void [27]
			     9  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 2, line: 9]
			        [pc: 9, line: 11]
			      Local variable table:
			        [pc: 2, pc: 10] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test051() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					bar();
				}
				static void bar() {
					int i = 6;
					if ((i == 6) || true) {
					} else {
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  static void bar();
			    0  bipush 6
			    2  istore_0 [i]
			    3  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 3, line: 11]
			      Local variable table:
			        [pc: 3, pc: 4] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test052() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					bar();
				}
				static void bar() {
					int i = 6;
					if ((i == 6) || true) {
					   	System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar();
			     0  bipush 6
			     2  istore_0 [i]
			     3  getstatic java.lang.System.out : java.io.PrintStream [21]
			     6  iload_0 [i]
			     7  invokevirtual java.io.PrintStream.println(int) : void [27]
			    10  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 3, line: 8]
			        [pc: 10, line: 10]
			      Local variable table:
			        [pc: 3, pc: 11] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test053() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					foo2();
				}
				static void foo2() {
					int i = 5;
					if ((i == 6) && (boom() && false)) {
					   	System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo2();
			     0  iconst_5
			     1  istore_0 [i]
			     2  iload_0 [i]
			     3  bipush 6
			     5  if_icmpne 12
			     8  invokestatic X.boom() : boolean [26]
			    11  pop
			    12  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 2, line: 10]
			        [pc: 12, line: 13]
			      Local variable table:
			        [pc: 2, pc: 13] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test054() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					foo2();
				}
				static void foo2() {
					int i = 5;
					if ((i == 6) && (boom() && false)) {
					} else {
					   	System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo2();
			     0  iconst_5
			     1  istore_0 [i]
			     2  iload_0 [i]
			     3  bipush 6
			     5  if_icmpne 12
			     8  invokestatic X.boom() : boolean [26]
			    11  pop
			    12  getstatic java.lang.System.out : java.io.PrintStream [28]
			    15  iload_0 [i]
			    16  invokevirtual java.io.PrintStream.println(int) : void [34]
			    19  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 2, line: 10]
			        [pc: 12, line: 12]
			        [pc: 19, line: 14]
			      Local variable table:
			        [pc: 2, pc: 20] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test055() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					bar2();
				}
				static void bar2() {
					int i = 6;
					if ((i == 6) || (boom() || true)) {
					} else {
						System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar2();
			     0  bipush 6
			     2  istore_0 [i]
			     3  iload_0 [i]
			     4  bipush 6
			     6  if_icmpeq 13
			     9  invokestatic X.boom() : boolean [26]
			    12  pop
			    13  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 3, line: 10]
			        [pc: 13, line: 14]
			      Local variable table:
			        [pc: 3, pc: 14] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test056() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					bar2();
				}
				static void bar2() {
					int i = 6;
					if ((i == 6) || (boom() || true)) {
						System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar2();
			     0  bipush 6
			     2  istore_0 [i]
			     3  iload_0 [i]
			     4  bipush 6
			     6  if_icmpeq 13
			     9  invokestatic X.boom() : boolean [26]
			    12  pop
			    13  getstatic java.lang.System.out : java.io.PrintStream [28]
			    16  iload_0 [i]
			    17  invokevirtual java.io.PrintStream.println(int) : void [34]
			    20  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 3, line: 10]
			        [pc: 13, line: 11]
			        [pc: 20, line: 13]
			      Local variable table:
			        [pc: 3, pc: 21] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test057() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					foo3();
				}
				static void foo3() {
					int i = 5;
					if (false && (i == 6)) {
					   	System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  static void foo3();
			    0  iconst_5
			    1  istore_0 [i]
			    2  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 2, line: 10]
			      Local variable table:
			        [pc: 2, pc: 3] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test058() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					foo3();
				}
				static void foo3() {
					int i = 5;
					if (false && (i == 6)) {
					} else {
					   	System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo3();
			     0  iconst_5
			     1  istore_0 [i]
			     2  getstatic java.lang.System.out : java.io.PrintStream [21]
			     5  iload_0 [i]
			     6  invokevirtual java.io.PrintStream.println(int) : void [27]
			     9  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 2, line: 9]
			        [pc: 9, line: 11]
			      Local variable table:
			        [pc: 2, pc: 10] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test059() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					bar3();
				}
				static void bar3() {
					int i = 6;
					if (true || (i == 6)) {
					} else {
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  static void bar3();
			    0  bipush 6
			    2  istore_0 [i]
			    3  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 3, line: 11]
			      Local variable table:
			        [pc: 3, pc: 4] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test060() throws Exception {
		String source =
			"""
			public class X {
				public static void main(String[] args) {
					bar3();
				}
				static void bar3() {
					int i = 6;
					if (true || (i == 6)) {
					   System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar3();
			     0  bipush 6
			     2  istore_0 [i]
			     3  getstatic java.lang.System.out : java.io.PrintStream [21]
			     6  iload_0 [i]
			     7  invokevirtual java.io.PrintStream.println(int) : void [27]
			    10  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 3, line: 8]
			        [pc: 10, line: 10]
			      Local variable table:
			        [pc: 3, pc: 11] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test061() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					foo4();
				}
				static void foo4() {
					int i = 5;
					if ((false && boom()) && (i == 6)) {   \t
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  static void foo4();
			    0  iconst_5
			    1  istore_0 [i]
			    2  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 2, line: 13]
			      Local variable table:
			        [pc: 2, pc: 3] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test062() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					foo4();
				}
				static void foo4() {
					int i = 5;
					if ((false && boom()) && (i == 6)) {
					} else {  \t
					   System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo4();
			     0  iconst_5
			     1  istore_0 [i]
			     2  getstatic java.lang.System.out : java.io.PrintStream [26]
			     5  iload_0 [i]
			     6  invokevirtual java.io.PrintStream.println(int) : void [32]
			     9  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 2, line: 12]
			        [pc: 9, line: 14]
			      Local variable table:
			        [pc: 2, pc: 10] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test063() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					bar4();
				}
				static void bar4() {
					int i = 6;
					if ((true || boom()) || (i == 6)) {
					} else {
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 1, Locals: 1
			  static void bar4();
			    0  bipush 6
			    2  istore_0 [i]
			    3  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 3, line: 14]
			      Local variable table:
			        [pc: 3, pc: 4] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test064() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					bar4();
				}
				static void bar4() {
					int i = 6;
					if ((true || boom()) || (i == 6)) {
					   	System.out.println(i);
					   }
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar4();
			     0  bipush 6
			     2  istore_0 [i]
			     3  getstatic java.lang.System.out : java.io.PrintStream [26]
			     6  iload_0 [i]
			     7  invokevirtual java.io.PrintStream.println(int) : void [32]
			    10  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 3, line: 11]
			        [pc: 10, line: 13]
			      Local variable table:
			        [pc: 3, pc: 11] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test065() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					foo5();
				}
				static void foo5() {
					int i = 5;
					if (((i == 6) && (boom() && false)) && false) {
						System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo5();
			     0  iconst_5
			     1  istore_0 [i]
			     2  iload_0 [i]
			     3  bipush 6
			     5  if_icmpne 12
			     8  invokestatic X.boom() : boolean [26]
			    11  pop
			    12  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 2, line: 10]
			        [pc: 12, line: 13]
			      Local variable table:
			        [pc: 2, pc: 13] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test066() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					foo5();
				}
				static void foo5() {
					int i = 5;
					if (((i == 6) && (boom() && false)) && false) {
					} else {
						System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void foo5();
			     0  iconst_5
			     1  istore_0 [i]
			     2  iload_0 [i]
			     3  bipush 6
			     5  if_icmpne 12
			     8  invokestatic X.boom() : boolean [26]
			    11  pop
			    12  getstatic java.lang.System.out : java.io.PrintStream [28]
			    15  iload_0 [i]
			    16  invokevirtual java.io.PrintStream.println(int) : void [34]
			    19  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 2, line: 10]
			        [pc: 12, line: 12]
			        [pc: 19, line: 14]
			      Local variable table:
			        [pc: 2, pc: 20] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test067() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					bar5();
				}
				static void bar5() {
					int i = 6;
					if (((i == 6) || (boom() || true)) && true) {
					} else {
						System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar5();
			     0  bipush 6
			     2  istore_0 [i]
			     3  iload_0 [i]
			     4  bipush 6
			     6  if_icmpeq 13
			     9  invokestatic X.boom() : boolean [26]
			    12  pop
			    13  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 3, line: 10]
			        [pc: 13, line: 14]
			      Local variable table:
			        [pc: 3, pc: 14] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	public void test068() throws Exception {
		String source =
			"""
			public class X {
				static boolean boom() {\s
					throw new NullPointerException();
				}
				public static void main(String[] args) {
					bar5();
				}
				static void bar5() {
					int i = 6;
					if (((i == 6) || (boom() || true)) && true) {
						System.out.println(i);
					}
				}
			}""";
		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 2, Locals: 1
			  static void bar5();
			     0  bipush 6
			     2  istore_0 [i]
			     3  iload_0 [i]
			     4  bipush 6
			     6  if_icmpeq 13
			     9  invokestatic X.boom() : boolean [26]
			    12  pop
			    13  getstatic java.lang.System.out : java.io.PrintStream [28]
			    16  iload_0 [i]
			    17  invokevirtual java.io.PrintStream.println(int) : void [34]
			    20  return
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 3, line: 10]
			        [pc: 13, line: 11]
			        [pc: 20, line: 13]
			      Local variable table:
			        [pc: 3, pc: 21] local: i index: 0 type: int
			""";
		checkClassFile("X", source, expectedOutput);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47886
	 */
	public void test069() throws Exception {
		String source =
			"public interface I {\n" +
			"}";
		String expectedOutput =
			"""
			// Compiled from I.java (version 1.2 : 46.0, no super bit)
			public abstract interface I {
			  Constant pool:
			    constant #1 class: #2 I
			    constant #2 utf8: "I"
			    constant #3 class: #4 java/lang/Object
			    constant #4 utf8: "java/lang/Object"
			    constant #5 utf8: "SourceFile"
			    constant #6 utf8: "I.java"
			}""";
		checkClassFile("I", source, expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test072() throws Exception {
		String source =
			"""
			package p;
			public abstract class X {
				public static final double CONST = Double.POSITIVE_INFINITY;
				X(X x) {}
				int foo() { return 0; }
				double foo2() { return 0; }
				byte foo3() { return 0; }
				char foo4() { return 0; }
				float foo5() { return 0; }
				long foo6() { return 0; }
				short foo7() { return 0; }
				Object foo8() { return null; }
				boolean foo9() { return false; }
				void foo10() {}
				native void foo11();
				abstract String foo12();
			}""";
		String expectedOutput =
			"""
			package p;
			public abstract class X {
			 \s
			  public static final double CONST = 1.0 / 0.0;
			 \s
			  X(p.X x) {
			  }
			 \s
			  int foo() {
			    return 0;
			  }
			 \s
			  double foo2() {
			    return 0;
			  }
			 \s
			  byte foo3() {
			    return 0;
			  }
			 \s
			  char foo4() {
			    return 0;
			  }
			 \s
			  float foo5() {
			    return 0;
			  }
			 \s
			  long foo6() {
			    return 0;
			  }
			 \s
			  short foo7() {
			    return 0;
			  }
			 \s
			  java.lang.Object foo8() {
			    return null;
			  }
			 \s
			  boolean foo9() {
			    return false;
			  }
			 \s
			  void foo10() {
			  }
			 \s
			  native void foo11();
			 \s
			  abstract java.lang.String foo12();
			}""";
		checkClassFile("p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test073() throws Exception {
		String source =
			"""
			public class X {
				public static final double CONST = Double.POSITIVE_INFINITY;
				X(X x) {}
			}""";
		String expectedOutput =
			"""
			public class X {
			 \s
			  public static final double CONST = 1.0 / 0.0;
			 \s
			  X(X x) {
			  }
			}""";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test074() throws Exception {
		String source =
			"""
			package p;
			public class X {
				public static final double CONST = Double.POSITIVE_INFINITY;
				X(X x) {}
			}""";
		String expectedOutput =
			"""
			package p;
			public class X {
			 \s
			  public static final double CONST = 1.0 / 0.0;
			 \s
			  X(X x) {
			  }
			}""";
		checkClassFile("p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test075() throws Exception {
		String source =
			"""
			package p;
			public class X {
				public static final String CONST = "";
				X(X x) {}
			}""";
		String expectedOutput =
			"""
			package p;
			public class X {
			 \s
			  public static final String CONST = "";
			 \s
			  X(X x) {
			  }
			}""";
		checkClassFile("p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test076() throws Exception {
		String source =
			"""
			public class X {
				void foo() {
					try {
						System.out.println("Hello");
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}""";
		String expectedOutput =
			"      Exception Table:\n" +
			"        [pc: 0, pc: 8] -> 11 when : Exception\n";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=34373
	public void test077() throws Exception {
		String source =
			"""
			package p;
			public class X {
				private static class A {}
			}""";
		String expectedOutput =
			"private static class p.X$A {\n";
		checkClassFile("p", "X", "X$A", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT, false);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102473
	public void test078() throws Exception {
		String source =
			"""
			public class X {
				X(int i, int j) {}
				void foo(String s, double d) {}
			}""";
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertTrue("At least one method", length > 0);
		for (int i = 0; i < length; i++) {
			char[][] argNames = methodInfos[i].getArgumentNames();
			assertNotNull("No names", argNames);
			assertEquals("Wrong size", 2, argNames.length);
			if (CharOperation.equals(methodInfos[i].getSelector(), "<init>".toCharArray())) {
				assertEquals("Wrong argument name", "i", new String(argNames[0]));
				assertEquals("Wrong argument name", "j", new String(argNames[1]));
			} else if (CharOperation.equals(methodInfos[i].getSelector(), "foo".toCharArray())) {
				assertEquals("Wrong argument name", "s", new String(argNames[0]));
				assertEquals("Wrong argument name", "d", new String(argNames[1]));
			}
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102473
	public void test079() throws Exception {
		String source =
			"""
			public class X {
				X(int i, int j) {}
				void foo(String s, double d) throws Exception {
					try {
						System.out.println(s + d);
					} catch(Exception e) {
						e.printStackTrace();
						throw e;
					} finally {
						System.out.println("done");
					}
				}
			}""";
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertTrue("At least one method", length > 0);
		for (int i = 0; i < length; i++) {
			char[][] argNames = methodInfos[i].getArgumentNames();
			assertNotNull("No names", argNames);
			assertEquals("Wrong size", 2, argNames.length);
			if (CharOperation.equals(methodInfos[i].getSelector(), "<init>".toCharArray())) {
				assertEquals("Wrong argument name", "i", new String(argNames[0]));
				assertEquals("Wrong argument name", "j", new String(argNames[1]));
			} else if (CharOperation.equals(methodInfos[i].getSelector(), "foo".toCharArray())) {
				assertEquals("Wrong argument name", "s", new String(argNames[0]));
				assertEquals("Wrong argument name", "d", new String(argNames[1]));
			}
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102473
	public void test080() throws Exception {
		String source =
			"""
			public class X {
				X(int i, int j) {}
				void foo(String s, double d) throws Exception {
					try {
						int k = 0;
						System.out.println(s + d + k);
					} catch(Exception e) {
						e.printStackTrace();
						throw e;
					} finally {
						System.out.println("done");
					}
				}
			}""";
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertTrue("At least one method", length > 0);
		for (int i = 0; i < length; i++) {
			char[][] argNames = methodInfos[i].getArgumentNames();
			assertNotNull("No names", argNames);
			assertEquals("Wrong size", 2, argNames.length);
			if (CharOperation.equals(methodInfos[i].getSelector(), "<init>".toCharArray())) {
				assertEquals("Wrong argument name", "i", new String(argNames[0]));
				assertEquals("Wrong argument name", "j", new String(argNames[1]));
			} else if (CharOperation.equals(methodInfos[i].getSelector(), "foo".toCharArray())) {
				assertEquals("Wrong argument name", "s", new String(argNames[0]));
				assertEquals("Wrong argument name", "d", new String(argNames[1]));
			}
		}
	}

}
