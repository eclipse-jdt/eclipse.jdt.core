/*******************************************************************************
 * Copyright (c) 2023 IBM corporation and others.
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
import java.io.IOException;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
public class StringConcatTest extends AbstractComparableTest {

	static {
		///	TESTS_NAMES = new String[] { "test001" };
	}

	public StringConcatTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(StringConcatTest.class, F_1_8);
	}

	private String getClassFileContents( String classFileName, int mode) throws IOException,
	ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		return result;
	}
	private void verifyOutput(String result, String expectedOutput, boolean positive) {
		int index = result.indexOf(expectedOutput);
		if (positive) {
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
				System.out.println("...");
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} else {
			if (index != -1) {
				assertEquals("Unexpected contents", "", result);
			}
		}
	}
	private void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean positive) throws IOException, ClassFormatException {
		if (this.complianceLevel < ClassFileConstants.JDK9)
			return;
		String result = getClassFileContents(classFileName, mode);
		verifyOutput(result, expectedOutput, positive);
	}

	public void test001() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  public static void main(String[] args) {
						    int one = 1;
						    float two = 2;
						    double three = 3d;
						    long four = 4L;
						    byte five = (byte) 5;
						    short six = 6;
						    char seven = 7;
						    boolean b = false;
						    String s = "one=" + one + ", two=" + two + ", three=" + three +", four=" + four +", five=" + five + ", six=" + six + ", seven=" + seven + ", boolean b=" + b;
						    s += ".";
						    System.out.println(s);
						  }
						} """,
				},
				"one=1, two=2.0, three=3.0, four=4, five=5, six=6, seven=, boolean b=false."
			);
		String expectedOutput = """
			  // Stack: 10, Locals: 12
			  public static void main(java.lang.String[] args);
			     0  iconst_1
			     1  istore_1 [one]
			     2  fconst_2
			     3  fstore_2 [two]
			     4  ldc2_w <Double 3.0> [16]
			     7  dstore_3 [three]
			     8  ldc2_w <Long 4> [18]
			    11  lstore 5 [four]
			    13  iconst_5
			    14  istore 7 [five]
			    16  bipush 6
			    18  istore 8 [six]
			    20  bipush 7
			    22  istore 9 [seven]
			    24  iconst_0
			    25  istore 10 [b]
			    27  iload_1 [one]
			    28  fload_2 [two]
			    29  dload_3 [three]
			    30  lload 5 [four]
			    32  iload 7 [five]
			    34  iload 8 [six]
			    36  iload 9 [seven]
			    38  iload 10 [b]
			    40  invokedynamic 0 makeConcatWithConstants(int, float, double, long, byte, short, char, boolean) : java.lang.String [20]
			    45  astore 11 [s]
			    47  aload 11 [s]
			    49  invokedynamic 1 makeConcatWithConstants(java.lang.String) : java.lang.String [24]
			    54  astore 11 [s]
			    56  getstatic java.lang.System.out : java.io.PrintStream [27]
			    59  aload 11 [s]
			    61  invokevirtual java.io.PrintStream.println(java.lang.String) : void [33]
			    64  return
			""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
	// Test string concat for a simple BinaryExpression
	public void test002() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  public static void main(String[] args) {
							  new X().foo();
						  }
						  public void foo() {
							  int one = 1;
							  String s = "one=" + one;
							  System.out.println(s);
						  }
						}""",
				},
				"one=1"
			);
		String expectedOutput = """
			  // Stack: 2, Locals: 3
			  public void foo();
			     0  iconst_1
			     1  istore_1 [one]
			     2  iload_1 [one]
			     3  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [22]
			     8  astore_2 [s]
			     9  getstatic java.lang.System.out : java.io.PrintStream [26]
			    12  aload_2 [s]
			    13  invokevirtual java.io.PrintStream.println(java.lang.String) : void [32]
			    16  return
			""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
	// Test binary expression whose first operand is a field and therefore already loaded into the stack
	public void test003() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							String v;
						  public static void main(String[] args) {
							  new X().foo();
						  }
						  public void foo() {
							  int one = 1;
							    float two = 2;
							    double three = 3d;
							    long four = 4L;
							    byte five = (byte) 5;
							    short six = 6;
							    char seven = 7;
							    boolean b = false;
							  v += ",one=" + one + ", two=" + two + ", three=" + three +", four=" + four +", five=" + five + ", six=" + six + ", seven=" + seven + ", boolean b=" + b;
							  System.out.println(this.v);
						  }
						}""",
				},
				"null,one=1, two=2.0, three=3.0, four=4, five=5, six=6, seven=, boolean b=false"
			);
		String expectedOutput = """
			  // Stack: 12, Locals: 11
			  public void foo();
			     0  iconst_1
			     1  istore_1 [one]
			     2  fconst_2
			     3  fstore_2 [two]
			     4  ldc2_w <Double 3.0> [24]
			     7  dstore_3 [three]
			     8  ldc2_w <Long 4> [26]
			    11  lstore 5 [four]
			    13  iconst_5
			    14  istore 7 [five]
			    16  bipush 6
			    18  istore 8 [six]
			    20  bipush 7
			    22  istore 9 [seven]
			    24  iconst_0
			    25  istore 10 [b]
			    27  aload_0 [this]
			    28  dup
			    29  getfield X.v : java.lang.String [28]
			    32  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [30]
			    35  iload_1 [one]
			    36  fload_2 [two]
			    37  dload_3 [three]
			    38  lload 5 [four]
			    40  iload 7 [five]
			    42  iload 8 [six]
			    44  iload 9 [seven]
			    46  iload 10 [b]
			    48  invokedynamic 0 makeConcatWithConstants(java.lang.String, int, float, double, long, byte, short, char, boolean) : java.lang.String [36]
			    53  putfield X.v : java.lang.String [28]
			    56  getstatic java.lang.System.out : java.io.PrintStream [40]
			    59  aload_0 [this]
			    60  getfield X.v : java.lang.String [28]
			    63  invokevirtual java.io.PrintStream.println(java.lang.String) : void [46]
			    66  return
			""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
	// Test a binary expression (with string concat) whose one operand is not a string type
	public void test004() throws IOException, ClassFormatException {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							int count = 0;
						  public static void main(String[] args) {
							  new X().foo();
						  }
						  public void foo() {
							  System.out.println(this.toString());
						  }
						  public String toString() {
							  return "count=" + (this.count + 1);
						  }
						} """,
				},
				"count=1"
			);
		String expectedOutput = """
			  // Stack: 2, Locals: 1
			  public java.lang.String toString();
			     0  aload_0 [this]
			     1  getfield X.count : int [12]
			     4  iconst_1
			     5  iadd
			     6  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [42]
			    11  areturn
			""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}

	// Test for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1201
	public void test005() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void main(String[] args) {
									int first = 11;
									int second = 42;
									String actual =
											"X " + 0 + " " +
											"a " + first + " " +
											"a " + first + " " +
											"X " + 1 + " " +
											"b " + second + " " +
											"b " + second + " " +
											"b " + second + " " +
											"b " + second;
									System.out.println(actual);
								}
							}
							""",
				},
				"X 0 a 11 a 11 X 1 b 42 b 42 b 42 b 42"
				);
		String expectedOutput = """
			  // Stack: 6, Locals: 4
			  public static void main(java.lang.String[] args);
			     0  bipush 11
			     2  istore_1 [first]
			     3  bipush 42
			     5  istore_2 [second]
			     6  iload_1 [first]
			     7  iload_1 [first]
			     8  iload_2 [second]
			     9  iload_2 [second]
			    10  iload_2 [second]
			    11  iload_2 [second]
			    12  invokedynamic 0 makeConcatWithConstants(int, int, int, int, int, int) : java.lang.String [16]
			    17  astore_3 [actual]
			    18  getstatic java.lang.System.out : java.io.PrintStream [20]
			    21  aload_3 [actual]
			    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [26]
			    25  return
			""";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
	}
}