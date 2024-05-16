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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for Bug 331872 - [compiler] NPE in Scope.createArrayType when attempting qualified access from type parameter
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 409247 - [1.8][compiler] Verify error with code allocating multidimensional array
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ArrayTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int[] { 18 };
	}
	public ArrayTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ArrayTest.class;
	}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  int[] x= new int[] {,};
			}
			""",
	});
}

/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test002() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
				    public static void main(String[] args) {
				        float[] tab = new float[] {-0.0f};
				        System.out.print(tab[0]);
				    }
				}""",
		},
		"-0.0");
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test003() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
				    public static void main(String[] args) {
				        float[] tab = new float[] {0.0f};
				        System.out.print(tab[0]);
				    }
				}""",
		},
		"0.0");
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test004() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
				    public static void main(String[] args) {
				        int[] tab = new int[] {-0};
				        System.out.print(tab[0]);
				    }
				}""",
		},
		"0");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=37387
 */
public void test005() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					 private static final Object X[] = new Object[]{null,null};
				    public static void main(String[] args) {
						System.out.println("SUCCESS");
				    }
				}
				""",
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
		  static {};
		    0  iconst_2
		    1  anewarray java.lang.Object [3]
		    4  putstatic X.X : java.lang.Object[] [9]
		    7  return
		      Line numbers:
		        [pc: 0, line: 2]
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=80597
 */
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						char[][][] array = new char[][][10];
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				char[][][] array = new char[][][10];
				                                ^^
			Cannot specify an array dimension after an empty dimension
			----------
			""");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=85203
 */
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static long lfield;
				\t
					public static void main(String[] args) {
						lfield = args.length;
						lfield = args(args).length;
					\t
					}
					static String[] args(String[] args) {
						return args;
					}
				}
				""",
		},
		"");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=85125
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public String getTexts(int i) [] {
						String[] texts = new String[1];
						return texts;\s
					}
				    public static void main(String[] args) {
						System.out.println("SUCCESS");
				    }
				}
				""",
		},
		"SUCCESS");
}
// check deep resolution of faulty initializer (no array expected type)
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=120263
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						X x = { 10, zork() };
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X x = { 10, zork() };
				      ^^^^^^^^^^^^^^
			Type mismatch: cannot convert from int[] to X
			----------
			2. ERROR in X.java (at line 3)
				X x = { 10, zork() };
				            ^^^^
			The method zork() is undefined for the type X
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=124101
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					int i = {};
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				int i = {};
				        ^^
			Type mismatch: cannot convert from Object[] to int
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148807 - variation
public void test011() throws Exception {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_5) {
		// there is a bug on 1.4 VMs which make them fail verification (see 148807)
		return;
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							try {
								Object[][] all = new String[1][];
								all[0] = new Object[0];
							} catch (ArrayStoreException e) {
								System.out.println("SUCCESS");
							}
						}
					}""", // =================
			},
			"SUCCESS");
		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 3, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  iconst_1
			     1  anewarray java.lang.String[] [16]
			     4  astore_1 [all]
			     5  aload_1 [all]
			     6  iconst_0
			     7  iconst_0
			     8  anewarray java.lang.Object [3]
			    11  aastore
			    12  goto 24
			    15  astore_1 [e]
			    16  getstatic java.lang.System.out : java.io.PrintStream [18]
			    19  ldc <String "SUCCESS"> [24]
			    21  invokevirtual java.io.PrintStream.println(java.lang.String) : void [26]
			    24  return
			      Exception Table:
			        [pc: 0, pc: 12] -> 15 when : java.lang.ArrayStoreException
			      Line numbers:
			        [pc: 0, line: 4]
			        [pc: 5, line: 5]
			        [pc: 12, line: 6]
			        [pc: 16, line: 7]
			        [pc: 24, line: 9]
			      Local variable table:
			        [pc: 0, pc: 25] local: args index: 0 type: java.lang.String[]
			        [pc: 5, pc: 12] local: all index: 1 type: java.lang.Object[][]
			        [pc: 16, pc: 24] local: e index: 1 type: java.lang.ArrayStoreException
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148807 - variation
public void test012() throws Exception {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_5) {
		// there is a bug on 1.4 VMs which make them fail verification (see 148807)
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Map;
				
				public class X {
					Map fValueMap;
				
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					public Object[][] getAllChoices() {
						Object[][] all = new String[this.fValueMap.size()][];
						return all;
					}
				}""", // =================,
		},
		"SUCCESS");
	String expectedOutput =
	"""
		  // Method descriptor #35 ()[[Ljava/lang/Object;
		  // Stack: 1, Locals: 2
		  public java.lang.Object[][] getAllChoices();
		     0  aload_0 [this]
		     1  getfield X.fValueMap : java.util.Map [36]
		     4  invokeinterface java.util.Map.size() : int [38] [nargs: 1]
		     9  anewarray java.lang.String[] [44]
		    12  astore_1 [all]
		    13  aload_1 [all]
		    14  areturn
		      Line numbers:
		        [pc: 0, line: 10]
		        [pc: 13, line: 11]
		      Local variable table:
		        [pc: 0, pc: 15] local: this index: 0 type: X
		        [pc: 13, pc: 15] local: all index: 1 type: java.lang.Object[][]
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
//check resolution of faulty initializer
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179477
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    String[] m(String arg) {
				        System.out.println(argument + argument);
				        return new String[] { argument + argument, argument/*no problem*/ };
				    }
				}""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				System.out.println(argument + argument);
				                   ^^^^^^^^
			argument cannot be resolved to a variable
			----------
			2. ERROR in X.java (at line 3)
				System.out.println(argument + argument);
				                              ^^^^^^^^
			argument cannot be resolved to a variable
			----------
			3. ERROR in X.java (at line 4)
				return new String[] { argument + argument, argument/*no problem*/ };
				                      ^^^^^^^^
			argument cannot be resolved to a variable
			----------
			4. ERROR in X.java (at line 4)
				return new String[] { argument + argument, argument/*no problem*/ };
				                                 ^^^^^^^^
			argument cannot be resolved to a variable
			----------
			5. ERROR in X.java (at line 4)
				return new String[] { argument + argument, argument/*no problem*/ };
				                                           ^^^^^^^^
			argument cannot be resolved to a variable
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247307
// Check return type of array#clone()
public void test014() throws Exception {
	Map optionsMap = getCompilerOptions();
	CompilerOptions options = new CompilerOptions(optionsMap);
	if (options.complianceLevel > ClassFileConstants.JDK1_4) {
		// check that #clone() return type is changed ONLY from -source 1.5 only (independant from compliance level)
		optionsMap.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	}
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(long[] longs) throws Exception {
							long[] other = longs.clone();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					long[] other = longs.clone();
					               ^^^^^^^^^^^^^
				Type mismatch: cannot convert from Object to long[]
				----------
				""",
			null,
			true,
			optionsMap);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247307 - variation
//Check return type of array#clone()
public void test015() throws Exception {
	if ( new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(long[] longs) throws Exception {
						long[] other = longs.clone();
					}
				}
				""",
		},
		"");
}
//https:bugs.eclipse.org/bugs/show_bug.cgi?id=247307 - variation
//Check constant pool declaring class of array#clone()
public void test016() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(long[] longs) throws Exception {
							Object other = longs.clone();
						}
					}
					""",
			},
			"");
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =	new CompilerOptions(getCompilerOptions()).sourceLevel <= ClassFileConstants.JDK1_4
		?	"""
			  // Method descriptor #15 ([J)V
			  // Stack: 1, Locals: 3
			  void foo(long[] longs) throws java.lang.Exception;
			    0  aload_1 [longs]
			    1  invokevirtual java.lang.Object.clone() : java.lang.Object [19]
			    4  astore_2 [other]
			    5  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 5, line: 4]
			"""
		:	"""
			  // Method descriptor #15 ([J)V
			  // Stack: 1, Locals: 3
			  void foo(long[] longs) throws java.lang.Exception;
			    0  aload_1 [longs]
			    1  invokevirtual long[].clone() : java.lang.Object [19]
			    4  astore_2 [other]
			    5  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 5, line: 4]
			""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
	return;
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247307 - variation
//Check constant pool declaring class of array#clone()
public void test017() throws Exception {
	Map optionsMap = getCompilerOptions();
	CompilerOptions options = new CompilerOptions(optionsMap);
	if (options.complianceLevel > ClassFileConstants.JDK1_4) {
		// check that #clone() return type is changed ONLY from -source 1.5 only (independant from compliance level)
		optionsMap.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(long[] longs) throws Exception {
							Object other = longs.clone();
						}
					}
					""",
			},
			"",
			null,
			true,
			null,
			optionsMap,
			null);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([J)V
		  // Stack: 1, Locals: 3
		  void foo(long[] longs) throws java.lang.Exception;
		    0  aload_1 [longs]
		    1  invokevirtual java.lang.Object.clone() : java.lang.Object [19]
		    4  astore_2 [other]
		    5  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 5, line: 4]
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/331872 -  [compiler] NPE in Scope.createArrayType when attempting qualified access from type parameter
public void test018() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<p> {
					void foo(p.O[] elems)  {
					}
				   void bar() {
				        foo(new Object[0]);
				   }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				void foo(p.O[] elems)  {
				         ^^^^^
			Illegal qualified access from the type parameter p
			----------
			2. ERROR in X.java (at line 5)
				foo(new Object[0]);
				^^^
			The method foo(Object[]) is undefined for the type X<p>
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409247 - [1.8][compiler] Verify error with code allocating multidimensional array
public void test019() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						X [][][] x = new X[10][10][];
						System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");
}
}
