/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class BooleanTest extends AbstractRegressionTest {

public BooleanTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public Object getAccessibleSelection(int i) {
			    int c, d;
			    if ((this == null) || ((d = 4) > 0)) {
			      c = 2;
			    }
			    else {
			      if (this == null) {
			        c = 3;
			        i++;
			      }
			      i++;
			    }
			    return null;
			  }
			  public String getAccessibleSelection2(int i) {
			    int c, d;
			    return ((this == null) || ((d = 4) > 0))
			      ? String.valueOf(c = 2)
			      : String.valueOf(i++);\s
			  }
			}
			""",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/H.java",
		"""
			package p;
			public class H {
			  Thread fPeriodicSaveThread;
			  public void bar() {
			    int a = 0, b = 0;
			    if (a == 0 || (b = 2) == 2) {
			      //a = 1;
			    }
			    System.out.println(b);
			    if (b != 0) {
			      System.err.println("<bar>b should be equal to 0.");
			      System.exit(-1);
			    }
			  }
			  public void bar2() {
			    int a = 0, b = 0;
			    if (a == 1 && (b = 2) == 2) {
			      //a = 1;
			    }
			    System.out.println(b);
			    if (b != 0) {
			      System.err.println("<bar2>b should be equal to 0.");
			      System.exit(-1);
			    }
			  }
			  public static void main(String[] args) {
			    new H().bar();
			    new H().bar2();
			  }
			}
			""",
	});
}
public void test003() {
	this.runConformTest(new String[] {
		"p/I.java",
		"""
			package p;
			/**
			 * This test0 should run without producing a java.lang.ClassFormatError
			 */
			public class I {
			  public static void main(String[] args) {
			    int i = 1, j;
			    if (((i > 0) || ((j = 10) > j--)) && (i < 12)) {
			      System.out.println(i);
			    }
			  }
			  public static void main1(String[] args) {
			    int i = 1, j;
			    if (((i < 12) && ((j = 10) > j--)) || (i > 0)) {
			      System.out.println(i);
			    }
			  }
			  public static void main2(String[] args) {
			    int i = 1, j;
			    if (((i < 12) && ((j = 10) > j--)) && (i > 0)) {
			      System.out.println(i);
			    }
			  }
			}
			""",
	});
}
public void test004() {
	this.runConformTest(new String[] {
		"p/J.java",
		"""
			package p;
			/**
			 * This test0 should run without producing a java.lang.ClassFormatError
			 */
			public class J {
			  public static void main(String[] args) {
			    int i = 1, j;
			    if (((i > 0) || ((j = 10) > j--)) && (i < 12)) {
			      System.out.println(i);
			    }
			  }
			}
			""",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/M.java",
		"""
			package p;
			public class M {
			  public static void main(String[] args) {
			    int a = 0, b = 0;
			    if (a == 0 || (b = 2) == 2) {
			    }
			    if (b != 0) {
			      System.out.println("b should be equal to zero");
			      System.exit(-1);
			    }
			  }
			}
			""",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/Q.java",
		"""
			package p;
			/**
			 * This test0 should run without producing a java.lang.VerifyError
			 */
			public class Q {
			  boolean bar() {
			    if (false && foo()) {
			      return true;
			    }
			    return false;
			  }
			  boolean foo() {
			    return true;
			  }
			  public static void main(String[] args) {
			    new Q().bar();
			  }
			}
			""",
	});
}

// Bug 6596
public void test007() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object t;
					public static void main(String args[]) {
						new Test().testMethod();
						System.out.println("SUCCESS");
					}
					private void testMethod(){
						boolean a = false;
						boolean b = false;
						if (!(a&&b)){}
					}
				}
				""",
		},
		"SUCCESS");
}
// Bug 6596
public void test008() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object t;
					public static void main(String args[]) {
						new Test().testMethod();
						System.out.println("SUCCESS");
					}
					private void testMethod(){
						boolean a = false;
						boolean b = false;
						if (!(a||b)){}
					}
				}
				""",
		},
		"SUCCESS");
}
// Bug 6596
public void test009() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object t;
					public static void main(String args[]) {
						new Test().testMethod();
						System.out.println("SUCCESS");
					}
					private void testMethod(){
						final boolean a = false;
						boolean b = false;
						if (!(a&&b)){}
					}
				}
				""",
		},
		"SUCCESS");
}

// Bug 6596
public void test010() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object t;
					public static void main(String args[]) {
						new Test().testMethod();
						System.out.println("SUCCESS");
					}
					private void testMethod(){
						boolean a = false;
						boolean b = false;
						if (a == b){}
					}
				}
				""",
		},
		"SUCCESS");
}

// Bug 46675
public void test011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = null;
						boolean b = s != null && (s.length() == 0 ? TestConst.c1 : TestConst.c2);
						if (!b) System.out.println("SUCCESS");
					}
				
					public static class TestConst {
						public static final boolean c1 = true;
						public static final boolean c2 = true;
					}
				}""",
		},
		"SUCCESS");
}

// Bug 46675 - variation
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = "aaa";
						boolean b = s != null && (s.length() == 0 ? TestConst.c1 : TestConst.c2);
						if (b) System.out.println("SUCCESS");
					}
				
					public static class TestConst {
						public static final boolean c1 = true;
						public static final boolean c2 = true;
					}
				}""",
		},
		"SUCCESS");
}

// Bug 46675 - variation
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = "aaa";
						boolean b = s == null || (s.length() == 0 ? TestConst.c1 : TestConst.c2);
						if (!b) System.out.println("SUCCESS");
					}
				
					public static class TestConst {
						public static final boolean c1 = false;
						public static final boolean c2 = false;
					}
				}""",
		},
		"SUCCESS");
}

// Bug 47881
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X  {
				
				    public static void main(String args[]) {
						boolean b = true;
						b = b && false;                \s
						if (b) {
							System.out.println("FAILED");
						} else {
							System.out.println("SUCCESS");
						}
				    }
				}
				
				""",
		},
		"SUCCESS");
}

// Bug 47881 - variation
public void test015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X  {
				
				    public static void main(String args[]) {
						boolean b = true;
						b = b || true;                \s
						if (b) {
							System.out.println("SUCCESS");
						} else {
							System.out.println("FAILED");
						}
				    }
				}
				
				""",
		},
		"SUCCESS");
}
// Bug 47881 - variation
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X  {
				
				    public static void main(String args[]) {
						boolean b = false;
						b = b && true;                \s
						if (b) {
							System.out.println("FAILED");
						} else {
							System.out.println("SUCCESS");
						}
				    }
				}
				
				""",
		},
		"SUCCESS");
}

// Bug 47881 - variation
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X  {
				
				    public static void main(String args[]) {
						boolean b = true;
						b = b || false;                \s
						if (b) {
							System.out.println("SUCCESS");
						} else {
							System.out.println("FAILED");
						}
				    }
				}
				
				""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120
public void test018() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0++) || true) != ((true && true) && (!(false || true)))));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  getstatic X.f0 : float [26]
		    10  fconst_1
		    11  fadd
		    12  putstatic X.f0 : float [26]
		    15  iconst_1
		    16  invokevirtual java.io.PrintStream.println(boolean) : void [28]
		    19  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 16, line: 8]
		        [pc: 19, line: 10]
		      Local variable table:
		        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 20] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test019() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0++) || false) != true));
				  }
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 5, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  lload_1 [l11]
		     8  l2f
		     9  getstatic X.f0 : float [26]
		    12  dup
		    13  fconst_1
		    14  fadd
		    15  putstatic X.f0 : float [26]
		    18  fcmpg
		    19  ifge 26
		    22  iconst_0
		    23  goto 27
		    26  iconst_1
		    27  invokevirtual java.io.PrintStream.println(boolean) : void [28]
		    30  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 27, line: 8]
		        [pc: 30, line: 10]
		      Local variable table:
		        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 31] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test020() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) | true) != false));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_1
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test021() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) && false) != true));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_1
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test022() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) & false) != true));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_1
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120
public void test023() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0++) || true) == ((true && true) && (!(false || true)))));
				  }
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  getstatic X.f0 : float [26]
		    10  fconst_1
		    11  fadd
		    12  putstatic X.f0 : float [26]
		    15  iconst_0
		    16  invokevirtual java.io.PrintStream.println(boolean) : void [28]
		    19  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 16, line: 8]
		        [pc: 19, line: 10]
		      Local variable table:
		        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 20] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test024() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0++) || false) == true));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 5, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  lload_1 [l11]
		     8  l2f
		     9  getstatic X.f0 : float [26]
		    12  dup
		    13  fconst_1
		    14  fadd
		    15  putstatic X.f0 : float [26]
		    18  fcmpg
		    19  ifge 26
		    22  iconst_1
		    23  goto 27
		    26  iconst_0
		    27  invokevirtual java.io.PrintStream.println(boolean) : void [28]
		    30  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 27, line: 8]
		        [pc: 30, line: 10]
		      Local variable table:
		        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 31] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test025() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) | true) == false));
				  }
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_0
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test026() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) && false) == true));
				  }
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_0
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
		}""";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test027() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) & false) == true));
				  }
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_0
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test028() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0) || true) == false));
				  }
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  iconst_0
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]
		    11  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 8, line: 8]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 12] local: l11 index: 1 type: long
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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test029() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				   	System.out.println(
				   			((foo() || bar()) || true) && false); 	\t
				  }
				  static boolean foo(){\s
					  System.out.print("foo");
					  return false;
				  }
				  static boolean bar(){
					  System.out.print("bar");
					  return true;
				  }
				}
				""",
		},
		"foobarfalse");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  getstatic java.lang.System.out : java.io.PrintStream [18]
		     3  invokestatic X.foo() : boolean [24]
		     6  ifne 13
		     9  invokestatic X.bar() : boolean [28]
		    12  pop
		    13  iconst_0
		    14  invokevirtual java.io.PrintStream.println(boolean) : void [31]
		    17  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 3, line: 7]
		        [pc: 14, line: 6]
		        [pc: 17, line: 8]
		      Local variable table:
		        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test030() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static float f0;
				 \s
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				   \s
				    System.out.println(
				        (((l11 < f0++) || true) == ((foo() || bar()) || true)));
				  }
				  static boolean foo() {
					  System.out.print("foo");
					  return false;
				  }
				  static boolean bar() {
					  System.out.print("bar");
					  return true;
				  }
				}
				""",
		},
		"foobartrue");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  getstatic X.f0 : float [26]
		    10  fconst_1
		    11  fadd
		    12  putstatic X.f0 : float [26]
		    15  invokestatic X.foo() : boolean [28]
		    18  ifne 25
		    21  invokestatic X.bar() : boolean [32]
		    24  pop
		    25  iconst_1
		    26  invokevirtual java.io.PrintStream.println(boolean) : void [35]
		    29  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 8]
		        [pc: 7, line: 9]
		        [pc: 26, line: 8]
		        [pc: 29, line: 10]
		      Local variable table:
		        [pc: 0, pc: 30] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 30] local: l11 index: 1 type: long
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451
public void test031() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public float f0;
				
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				    X x = new X();
				    System.out.println(
				        (((l11 < x.f0) || true) != false));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput = this.complianceLevel == ClassFileConstants.JDK1_3
		?	"""
			  // Method descriptor #17 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 4
			  public static void main(java.lang.String[] args);
			     0  ldc2_w <Long -26> [18]
			     3  lstore_1 [l11]
			     4  new X [1]
			     7  dup
			     8  invokespecial X() [20]
			    11  astore_3 [x]
			    12  getstatic java.lang.System.out : java.io.PrintStream [21]
			    15  aload_3 [x]
			    16  invokevirtual java.lang.Object.getClass() : java.lang.Class [27]
			    19  pop
			    20  iconst_1
			    21  invokevirtual java.io.PrintStream.println(boolean) : void [31]
			    24  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 4, line: 7]
			        [pc: 12, line: 8]
			        [pc: 15, line: 9]
			        [pc: 21, line: 8]
			        [pc: 24, line: 10]
			      Local variable table:
			        [pc: 0, pc: 25] local: args index: 0 type: java.lang.String[]
			        [pc: 4, pc: 25] local: l11 index: 1 type: long
			        [pc: 12, pc: 25] local: x index: 3 type: X
			"""

		:	"""
			  // Method descriptor #17 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 4
			  public static void main(java.lang.String[] args);
			     0  ldc2_w <Long -26> [18]
			     3  lstore_1 [l11]
			     4  new X [1]
			     7  dup
			     8  invokespecial X() [20]
			    11  astore_3 [x]
			    12  getstatic java.lang.System.out : java.io.PrintStream [21]
			    15  aload_3 [x]
			    16  getfield X.f0 : float [27]
			    19  pop
			    20  iconst_1
			    21  invokevirtual java.io.PrintStream.println(boolean) : void [29]
			    24  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 4, line: 7]
			        [pc: 12, line: 8]
			        [pc: 15, line: 9]
			        [pc: 21, line: 8]
			        [pc: 24, line: 10]
			      Local variable table:
			        [pc: 0, pc: 25] local: args index: 0 type: java.lang.String[]
			        [pc: 4, pc: 25] local: l11 index: 1 type: long
			        [pc: 12, pc: 25] local: x index: 3 type: X
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451 - variation
public void test032() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  static float f0;
				
				  public static void main(String[] args)
				  {
				    long l11 = -26;
				    System.out.println(
				        (((l11 < (f0=13)) || true) != false));
				  }
				}
				""",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
			"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long -26> [18]
		     3  lstore_1 [l11]
		     4  getstatic java.lang.System.out : java.io.PrintStream [20]
		     7  ldc <Float 13.0> [26]
		     9  putstatic X.f0 : float [27]
		    12  iconst_1
		    13  invokevirtual java.io.PrintStream.println(boolean) : void [29]
		    16  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 4, line: 7]
		        [pc: 7, line: 8]
		        [pc: 13, line: 7]
		        [pc: 16, line: 9]
		      Local variable table:
		        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 17] local: l11 index: 1 type: long
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

public void test033() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean b = true;
						System.out.print(b ^ b);
						System.out.println(b ^ true);
					}\s
				}
				""",
		},
		"falsefalse");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [b]
		     2  getstatic java.lang.System.out : java.io.PrintStream [16]
		     5  iload_1 [b]
		     6  iload_1 [b]
		     7  ixor
		     8  invokevirtual java.io.PrintStream.print(boolean) : void [22]
		    11  getstatic java.lang.System.out : java.io.PrintStream [16]
		    14  iload_1 [b]
		    15  iconst_1
		    16  ixor
		    17  invokevirtual java.io.PrintStream.println(boolean) : void [28]
		    20  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 4]
		        [pc: 11, line: 5]
		        [pc: 20, line: 6]
		      Local variable table:
		        [pc: 0, pc: 21] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 21] local: b index: 1 type: boolean
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
public void test034() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean b = true;
						if ((b ^ true) || b) {
							System.out.println("SUCCESS");
						}
					}\s
				}
				""",
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 2
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [b]
		     2  iload_1 [b]
		     3  ifeq 10
		     6  iload_1 [b]
		     7  ifeq 18
		    10  getstatic java.lang.System.out : java.io.PrintStream [16]
		    13  ldc <String "SUCCESS"> [22]
		    15  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    18  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 4]
		        [pc: 10, line: 5]
		        [pc: 18, line: 7]
		      Local variable table:
		        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 19] local: b index: 1 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451 - variation
public void test035() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static float f0;
					public static void main(String[] args) {
						System.out.println((X.f0 > 0 || true) == false);
					}\s
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #17 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		    0  getstatic java.lang.System.out : java.io.PrintStream [18]
		    3  iconst_0
		    4  invokevirtual java.io.PrintStream.println(boolean) : void [24]
		    7  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 7, line: 5]
		      Local variable table:
		        [pc: 0, pc: 8] local: args index: 0 type: java.lang.String[]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451 - variation
public void test036() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					float f0;
					public static void main(String[] args) {
						new X().foo();
					}
					void foo() {
						System.out.println((this.f0 > 0 || true) == false);
					}\s
				}
				""",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #8 ()V
		  // Stack: 2, Locals: 1
		  void foo();
		    0  getstatic java.lang.System.out : java.io.PrintStream [24]
		    3  iconst_0
		    4  invokevirtual java.io.PrintStream.println(boolean) : void [30]
		    7  return
		      Line numbers:
		        [pc: 0, line: 7]
		        [pc: 7, line: 8]
		      Local variable table:
		        [pc: 0, pc: 8] local: this index: 0 type: X
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147024
public void test037() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				 public class X {
				 public static final boolean T = true;
					public static final boolean F = false;
				\t
					public boolean getFlagBT() {
						boolean b = this.T;
						if (this.T)
							return true;
						else
							return false;
					}
				
					public int getFlagIT() {
						boolean b = this.T;
						if (this.T)
							return 0;
						else
							return 1;
					}
				
					public boolean getFlagBF() {
						boolean b = this.F;
						if (this.F)
							return true;
						else
							return false;
					}
				
					public int getFlagIF() {
						boolean b = this.F;
						if (this.F)
							return 0;
						else
							return 1;
					}
					public boolean getFlagBT2() {
						boolean b = T;
						if (T)
							return true;
						else
							return false;
					}
				
					public int getFlagIT2() {
						boolean b = T;
						if (T)
							return 0;
						else
							return 1;
					}
				
					public boolean getFlagBF2() {
						boolean b = F;
						if (F)
							return true;
						else
							return false;
					}
				
					public int getFlagIF2() {
						boolean b = F;
						if (F)
							return 0;
						else
							return 1;
					}
					public boolean getFlagBT3() {
						X self = this;
						boolean b = self.T;
						if (self.T)
							return true;
						else
							return false;
					}
				
					public int getFlagIT3() {
						X self = this;
						boolean b = self.T;
						if (self.T)
							return 0;
						else
							return 1;
					}
				
					public boolean getFlagBF3() {
						X self = this;
						boolean b = self.F;
						if (self.F)
							return true;
						else
							return false;
					}
					public int getFlagIF3() {
						X self = this;
						boolean b = self.F;
						if (self.F)
							return 0;
						else
							return 1;
					}
					public static void main(String[] args) {
						System.out.println("It worked.");
					}
				}""", // =================
		},
		"It worked.");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #21 ()Z
		  // Stack: 1, Locals: 2
		  public boolean getFlagBT();
		    0  iconst_1
		    1  istore_1 [b]
		    2  iconst_1
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 2, line: 8]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 2
		  public int getFlagIT();
		    0  iconst_1
		    1  istore_1 [b]
		    2  iconst_0
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 14]
		        [pc: 2, line: 16]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #21 ()Z
		  // Stack: 1, Locals: 2
		  public boolean getFlagBF();
		    0  iconst_0
		    1  istore_1 [b]
		    2  iconst_0
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 22]
		        [pc: 2, line: 26]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 2
		  public int getFlagIF();
		    0  iconst_0
		    1  istore_1 [b]
		    2  iconst_1
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 30]
		        [pc: 2, line: 34]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #21 ()Z
		  // Stack: 1, Locals: 2
		  public boolean getFlagBT2();
		    0  iconst_1
		    1  istore_1 [b]
		    2  iconst_1
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 37]
		        [pc: 2, line: 39]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 2
		  public int getFlagIT2();
		    0  iconst_1
		    1  istore_1 [b]
		    2  iconst_0
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 45]
		        [pc: 2, line: 47]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #21 ()Z
		  // Stack: 1, Locals: 2
		  public boolean getFlagBF2();
		    0  iconst_0
		    1  istore_1 [b]
		    2  iconst_0
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 53]
		        [pc: 2, line: 57]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 2
		  public int getFlagIF2();
		    0  iconst_0
		    1  istore_1 [b]
		    2  iconst_1
		    3  ireturn
		      Line numbers:
		        [pc: 0, line: 61]
		        [pc: 2, line: 65]
		      Local variable table:
		        [pc: 0, pc: 4] local: this index: 0 type: X
		        [pc: 2, pc: 4] local: b index: 1 type: boolean
		 \s
		  // Method descriptor #21 ()Z
		  // Stack: 1, Locals: 3
		  public boolean getFlagBT3();
		    0  aload_0 [this]
		    1  astore_1 [self]
		    2  iconst_1
		    3  istore_2 [b]
		    4  iconst_1
		    5  ireturn
		      Line numbers:
		        [pc: 0, line: 68]
		        [pc: 2, line: 69]
		        [pc: 4, line: 71]
		      Local variable table:
		        [pc: 0, pc: 6] local: this index: 0 type: X
		        [pc: 2, pc: 6] local: self index: 1 type: X
		        [pc: 4, pc: 6] local: b index: 2 type: boolean
		 \s
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 3
		  public int getFlagIT3();
		    0  aload_0 [this]
		    1  astore_1 [self]
		    2  iconst_1
		    3  istore_2 [b]
		    4  iconst_0
		    5  ireturn
		      Line numbers:
		        [pc: 0, line: 77]
		        [pc: 2, line: 78]
		        [pc: 4, line: 80]
		      Local variable table:
		        [pc: 0, pc: 6] local: this index: 0 type: X
		        [pc: 2, pc: 6] local: self index: 1 type: X
		        [pc: 4, pc: 6] local: b index: 2 type: boolean
		 \s
		  // Method descriptor #21 ()Z
		  // Stack: 1, Locals: 3
		  public boolean getFlagBF3();
		    0  aload_0 [this]
		    1  astore_1 [self]
		    2  iconst_0
		    3  istore_2 [b]
		    4  iconst_0
		    5  ireturn
		      Line numbers:
		        [pc: 0, line: 86]
		        [pc: 2, line: 87]
		        [pc: 4, line: 91]
		      Local variable table:
		        [pc: 0, pc: 6] local: this index: 0 type: X
		        [pc: 2, pc: 6] local: self index: 1 type: X
		        [pc: 4, pc: 6] local: b index: 2 type: boolean
		 \s
		  // Method descriptor #24 ()I
		  // Stack: 1, Locals: 3
		  public int getFlagIF3();
		    0  aload_0 [this]
		    1  astore_1 [self]
		    2  iconst_0
		    3  istore_2 [b]
		    4  iconst_1
		    5  ireturn
		      Line numbers:
		        [pc: 0, line: 94]
		        [pc: 2, line: 95]
		        [pc: 4, line: 99]
		      Local variable table:
		        [pc: 0, pc: 6] local: this index: 0 type: X
		        [pc: 2, pc: 6] local: self index: 1 type: X
		        [pc: 4, pc: 6] local: b index: 2 type: boolean
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
public void test038() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				 public class X {
					static boolean foo() { System.out.print("[foo]"); return false; }
					static boolean bar() { System.out.print("[bar]"); return true; }
					public static void main(String[] args) {
						if ((foo() || bar()) && false) {
							return;
						}
						System.out.println("[done]");
					}
				}""", // =================
		},
		"[foo][bar][done]");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #34 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  invokestatic X.foo() : boolean [35]
		     3  ifne 10
		     6  invokestatic X.bar() : boolean [37]
		     9  pop
		    10  getstatic java.lang.System.out : java.io.PrintStream [16]
		    13  ldc <String "[done]"> [39]
		    15  invokevirtual java.io.PrintStream.println(java.lang.String) : void [41]
		    18  return
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965
public void test039() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                boolean a = true, b;
				                if (a ? false : (b = true))
				                        a = b;
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [a]
		     2  iload_1 [a]
		     3  ifeq 9
		     6  goto 17
		     9  iconst_1
		    10  dup
		    11  istore_2 [b]
		    12  ifeq 17
		    15  iload_2 [b]
		    16  istore_1 [a]
		    17  getstatic java.lang.System.out : java.io.PrintStream [16]
		    20  ldc <String "SUCCESS"> [22]
		    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    25  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 4]
		        [pc: 15, line: 5]
		        [pc: 17, line: 6]
		        [pc: 25, line: 7]
		      Local variable table:
		        [pc: 0, pc: 26] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 26] local: a index: 1 type: boolean
		        [pc: 12, pc: 17] local: b index: 2 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965 - variation
public void test040() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args) {
				                boolean a = true, b = false;
				                if (!(a ? true : (b = true)))
				                        a = b;
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [a]
		     2  iconst_0
		     3  istore_2 [b]
		     4  iload_1 [a]
		     5  ifeq 11
		     8  goto 19
		    11  iconst_1
		    12  dup
		    13  istore_2 [b]
		    14  ifne 19
		    17  iload_2 [b]
		    18  istore_1 [a]
		    19  getstatic java.lang.System.out : java.io.PrintStream [16]
		    22  ldc <String "SUCCESS"> [22]
		    24  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    27  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		        [pc: 17, line: 5]
		        [pc: 19, line: 6]
		        [pc: 27, line: 7]
		      Local variable table:
		        [pc: 0, pc: 28] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 28] local: a index: 1 type: boolean
		        [pc: 4, pc: 28] local: b index: 2 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965 - variation
public void test041() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean a = true, b = false;
						if (a ? true : (b = false))
							a = b;
						System.out.println("SUCCESS");
					}
				}
				""", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [a]
		     2  iconst_0
		     3  istore_2 [b]
		     4  iload_1 [a]
		     5  ifeq 11
		     8  goto 17
		    11  iconst_0
		    12  dup
		    13  istore_2 [b]
		    14  ifeq 19
		    17  iload_2 [b]
		    18  istore_1 [a]
		    19  getstatic java.lang.System.out : java.io.PrintStream [16]
		    22  ldc <String "SUCCESS"> [22]
		    24  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    27  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		        [pc: 17, line: 5]
		        [pc: 19, line: 6]
		        [pc: 27, line: 7]
		      Local variable table:
		        [pc: 0, pc: 28] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 28] local: a index: 1 type: boolean
		        [pc: 4, pc: 28] local: b index: 2 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965 - variation
public void test042() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean a = true, b;
						if (a ? (b = true) : false)
						a = b;
						System.out.println("SUCCESS");
				    }       \s
				}
				""", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [a]
		     2  iload_1 [a]
		     3  ifeq 14
		     6  iconst_1
		     7  dup
		     8  istore_2 [b]
		     9  ifeq 14
		    12  iload_2 [b]
		    13  istore_1 [a]
		    14  getstatic java.lang.System.out : java.io.PrintStream [16]
		    17  ldc <String "SUCCESS"> [22]
		    19  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    22  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 4]
		        [pc: 12, line: 5]
		        [pc: 14, line: 6]
		        [pc: 22, line: 7]
		      Local variable table:
		        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 23] local: a index: 1 type: boolean
		        [pc: 9, pc: 14] local: b index: 2 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567
public void test043() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					boolean b;
					X(boolean b1) {
						if (b1 || (false && b1)) {
							System.out.println(b);
						}
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #8 (Z)V
		  // Stack: 2, Locals: 2
		  X(boolean b1);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [10]
		     4  iload_1 [b1]
		     5  ifne 11
		     8  goto 21
		    11  getstatic java.lang.System.out : java.io.PrintStream [13]
		    14  aload_0 [this]
		    15  getfield X.b : boolean [19]
		    18  invokevirtual java.io.PrintStream.println(boolean) : void [21]
		    21  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		        [pc: 11, line: 5]
		        [pc: 21, line: 7]
		      Local variable table:
		        [pc: 0, pc: 22] local: this index: 0 type: X
		        [pc: 0, pc: 22] local: b1 index: 1 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test044() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(boolean b1) {
						if (b1 || !(true || b1)) {
							System.out.println(b1);
						}
					} \t
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #6 (Z)V
		  // Stack: 2, Locals: 2
		  X(boolean b1);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  iload_1 [b1]
		     5  ifne 11
		     8  goto 18
		    11  getstatic java.lang.System.out : java.io.PrintStream [11]
		    14  iload_1 [b1]
		    15  invokevirtual java.io.PrintStream.println(boolean) : void [17]
		    18  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 4, line: 3]
		        [pc: 11, line: 4]
		        [pc: 18, line: 6]
		      Local variable table:
		        [pc: 0, pc: 19] local: this index: 0 type: X
		        [pc: 0, pc: 19] local: b1 index: 1 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test045() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(boolean b1, boolean b2){
						if (b1 || ((b1 && b2) && false)) {
							System.out.println(b1);\t
						}
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 (ZZ)V
		  // Stack: 2, Locals: 3
		  void foo(boolean b1, boolean b2);
		     0  iload_1 [b1]
		     1  ifne 15
		     4  iload_1 [b1]
		     5  ifeq 22
		     8  iload_2 [b2]
		     9  ifeq 22
		    12  goto 22
		    15  getstatic java.lang.System.out : java.io.PrintStream [16]
		    18  iload_1 [b1]
		    19  invokevirtual java.io.PrintStream.println(boolean) : void [22]
		    22  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 15, line: 4]
		        [pc: 22, line: 6]
		      Local variable table:
		        [pc: 0, pc: 23] local: this index: 0 type: X
		        [pc: 0, pc: 23] local: b1 index: 1 type: boolean
		        [pc: 0, pc: 23] local: b2 index: 2 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test046() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo2(boolean b1, boolean b2){
						if (b1 || ((b1 || b2) && false)) {
							System.out.println(b1);\t
						}
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 (ZZ)V
		  // Stack: 2, Locals: 3
		  void foo2(boolean b1, boolean b2);
		     0  iload_1 [b1]
		     1  ifne 15
		     4  iload_1 [b1]
		     5  ifne 22
		     8  iload_2 [b2]
		     9  ifeq 22
		    12  goto 22
		    15  getstatic java.lang.System.out : java.io.PrintStream [16]
		    18  iload_1 [b1]
		    19  invokevirtual java.io.PrintStream.println(boolean) : void [22]
		    22  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 15, line: 4]
		        [pc: 22, line: 6]
		      Local variable table:
		        [pc: 0, pc: 23] local: this index: 0 type: X
		        [pc: 0, pc: 23] local: b1 index: 1 type: boolean
		        [pc: 0, pc: 23] local: b2 index: 2 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test047() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(boolean b1) {
						int i;
						if (((b1 && false) && true) || true) {
							System.out.println(b1);
						}
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #6 (Z)V
		  // Stack: 2, Locals: 2
		  X(boolean b1);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  getstatic java.lang.System.out : java.io.PrintStream [11]
		     7  iload_1 [b1]
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [17]
		    11  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 4, line: 5]
		        [pc: 11, line: 7]
		      Local variable table:
		        [pc: 0, pc: 12] local: this index: 0 type: X
		        [pc: 0, pc: 12] local: b1 index: 1 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test048() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(boolean b1) {
						int i;
						if (((false && b1) && false) || true) {
							System.out.println(b1);
						}
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #6 (Z)V
		  // Stack: 2, Locals: 2
		  X(boolean b1);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  getstatic java.lang.System.out : java.io.PrintStream [11]
		     7  iload_1 [b1]
		     8  invokevirtual java.io.PrintStream.println(boolean) : void [17]
		    11  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 4, line: 5]
		        [pc: 11, line: 7]
		      Local variable table:
		        [pc: 0, pc: 12] local: this index: 0 type: X
		        [pc: 0, pc: 12] local: b1 index: 1 type: boolean
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test049() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(boolean b1) {
						int i;
						if (((b1 && b1) && false) || true) {
							System.out.println(b1);
						}
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #6 (Z)V
		  // Stack: 2, Locals: 2
		  X(boolean b1);
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  iload_1 [b1]
		     5  ifeq 8
		     8  getstatic java.lang.System.out : java.io.PrintStream [11]
		    11  iload_1 [b1]
		    12  invokevirtual java.io.PrintStream.println(boolean) : void [17]
		    15  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 4, line: 4]
		        [pc: 8, line: 5]
		        [pc: 15, line: 7]
		      Local variable table:
		        [pc: 0, pc: 16] local: this index: 0 type: X
		        [pc: 0, pc: 16] local: b1 index: 1 type: boolean
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

public void test050() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean t1 = true, t2 = true;
						if (t1){
						    if (t2){
						       return;
						    }
						    // dead goto bytecode
						}else{
							System.out.println();
						}	\t
					}
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 1, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [t1]
		     2  iconst_1
		     3  istore_2 [t2]
		     4  iload_1 [t1]
		     5  ifeq 13
		     8  iload_2 [t2]
		     9  ifeq 19
		    12  return
		    13  getstatic java.lang.System.out : java.io.PrintStream [16]
		    16  invokevirtual java.io.PrintStream.println() : void [22]
		    19  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		        [pc: 8, line: 5]
		        [pc: 12, line: 6]
		        [pc: 13, line: 10]
		        [pc: 19, line: 12]
		      Local variable table:
		        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 20] local: t1 index: 1 type: boolean
		        [pc: 4, pc: 20] local: t2 index: 2 type: boolean
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
public static Class testClass() {
	return BooleanTest.class;
}
}
