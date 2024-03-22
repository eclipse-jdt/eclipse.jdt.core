/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for bug 374605 - Unreasonable warning for enum-based switch statements
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SwitchTest extends AbstractRegressionTest {

	private static final long JDKLevelSupportingStringSwitch = ClassFileConstants.JDK1_7;

static {
//	TESTS_NUMBERS = new int[] { 22 };
//	TESTS_NAMES = new String[] { "testFor356002", "testFor356002_2", "testFor356002_3" };
}
public SwitchTest(String name) {
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
			  public static void main(String args[]) {
			    foo();
			  }
			  public static void foo() {
			    try {
			      switch(0) {
			      case 0 :
			      case 1 - (1 << 31) :
			      case (1 << 30) :
			      }
			    } catch (OutOfMemoryError e) {
			    }
			  }
			}
			""",
	});
}
public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  int k;
			  public void foo() {
			    int c;
			    switch (k) {
			      default :
			        c = 2;
			        break;
			      case 2 :
			        c = 3;
			        break;
			    }
			  }
			}
			""",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  int i = 0;
			  void foo() {
			    switch (i) {
			      case 1 :
			        {
			          int j;
			          break;
			        }
			    }
			  }
			}
			""",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public static int foo() {
			    int i = 0, j;
			    switch (i) {
			      default :
			        int k = 2;
			        j = k;
			    }
			    if (j != -2) {
			      return 1;
			    }
			    return 0;
			  }
			}
			""",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/BugJavaCase.java",
		"""
			package p;
			class BugJavaCase {
			  public static final int BC_ZERO_ARG = 1;
			  public void test01(int i) {
			    switch (i) {
			      case BC_ZERO_ARG :
			        System.out.println("i = " + i);
			        break;
			    }
			  }
			}
			""",
	});
}


public void test006() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public static void main(String args[]) {
			    foo();\s
			  }\s
			\s
			  public static void foo() {\s
			    char x = 5;
			    final short b = 5;
			    int a;
			   \s
			    switch (x) {
			      case b:        // compile time error
			        a = 0;
			        break;\s
			      default:
			        a=1;
			    }
			   \s
			  }
			}
			""",
	});
}

public void test007() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				class X {
				  void v() {
				    switch (1) {
				      case (int) (1.0 / 0.0) :
				        break;
				      case (int) (2.0 / 0.0) :
				        break;
				    }
				  }
				}""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 5)
				case (int) (1.0 / 0.0) :
				^^^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in p\\X.java (at line 7)
				case (int) (2.0 / 0.0) :
				^^^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			"""
	);
}
public void test008() {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					switch(args.length){
					}
					System.out.println("SUCCESS");
				}
			}
			""",
	},
	"SUCCESS");
}
public void test009() {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
			    public static void main(String argv[]) {
			        switch (81391861) {
			        case (81391861) :
			        	System.out.println("SUCCESS");
			            break;
			        default:
			        	System.out.println("FAILED");
			        }
			    }
			}
			""",
	},
	"SUCCESS");
}
public void test010() {
	String newMessage =
			"""
		----------
		1. ERROR in X.java (at line 4)
			switch(this){
			       ^^^^
		Cannot switch on a value of type X. Only convertible int values, strings or enum variables are permitted
		----------
		2. ERROR in X.java (at line 6)
			Zork z;
			^^^^
		Zork cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 11)
			switch(x){
			       ^
		x cannot be resolved to a variable
		----------
		4. ERROR in X.java (at line 13)
			Zork z;
			^^^^
		Zork cannot be resolved to a type
		----------
		""";
	String oldMessage =
			"""
		----------
		1. ERROR in X.java (at line 4)
			switch(this){
			       ^^^^
		Cannot switch on a value of type X. Only convertible int values or enum variables are permitted
		----------
		2. ERROR in X.java (at line 6)
			Zork z;
			^^^^
		Zork cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 11)
			switch(x){
			       ^
		x cannot be resolved to a variable
		----------
		4. ERROR in X.java (at line 13)
			Zork z;
			^^^^
		Zork cannot be resolved to a type
		----------
		""";
	String java21Plus = """
		----------
		1. ERROR in X.java (at line 4)
			switch(this){
			       ^^^^
		An enhanced switch statement should be exhaustive; a default label expected
		----------
		2. ERROR in X.java (at line 5)
			case 0 :\s
			     ^
		Type mismatch: cannot convert from int to X
		----------
		3. ERROR in X.java (at line 6)
			Zork z;
			^^^^
		Zork cannot be resolved to a type
		----------
		4. ERROR in X.java (at line 11)
			switch(x){
			       ^
		x cannot be resolved to a variable
		----------
		5. ERROR in X.java (at line 13)
			Zork z;
			^^^^
		Zork cannot be resolved to a type
		----------
		""";
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
			\t
				void foo(){
					switch(this){
						case 0 :\s
							Zork z;
					}
				}
			\t
				void bar(){
					switch(x){
						case 0 :\s
							Zork z;
					}
				}\t
			}
			""",
	},
	this.complianceLevel >= JDKLevelSupportingStringSwitch ? (this.complianceLevel >= ClassFileConstants.JDK21 ? java21Plus : newMessage) : oldMessage);

}
public void test011() {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String args[]) {
					switch (args.length) {
						case 1 :
							System.out.println();
						case 3 :
							break;
						default :
					}
					System.out.println("SUCCESS");
				}
			}
			""",
	},
	"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86813
public void test012() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
			  public static void main(String[] args) {
			    boolean x= true;
			    try {
			      int i= 1;
			      switch (i) { // <-- breakpoint here
			        case 1:
			          break;      //step 1\s
			        case 2:
			          x = false;   //step 2\s
			          break;
			      }
			    }catch(Exception e) {
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
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_1
		     1  istore_1 [x]
		     2  iconst_1
		     3  istore_2 [i]
		     4  iload_2 [i]
		     5  tableswitch default: 33
		          case 1: 28
		          case 2: 31
		    28  goto 37
		    31  iconst_0
		    32  istore_1 [x]
		    33  goto 37
		    36  astore_2
		    37  getstatic java.lang.System.out : java.io.PrintStream [16]
		    40  ldc <String "SUCCESS"> [22]
		    42  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    45  return
		      Exception Table:
		        [pc: 2, pc: 33] -> 36 when : java.lang.Exception
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 2, line: 5]
		        [pc: 4, line: 6]
		        [pc: 28, line: 8]
		        [pc: 31, line: 10]
		        [pc: 33, line: 13]
		        [pc: 37, line: 15]
		        [pc: 45, line: 16]
		      Local variable table:
		        [pc: 0, pc: 46] local: args index: 0 type: java.lang.String[]
		        [pc: 2, pc: 46] local: x index: 1 type: boolean
		        [pc: 4, pc: 33] local: i index: 2 type: int
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
public void test013() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
			
				public static void main(String[] args) {
					X x;
					Object o = null;
					for (int i = 0; i < 10; i++) {
						if (i < 90) {
							x = new X();
							if (i > 4) {
								o = new Object();
							} else {
								o = null;
							}
							switch (2) {
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
							System.out.print("3");			\t
						}
					}
				}
			}
			""",
	},
	"333332323232323");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 5
			  public static void main(java.lang.String[] args);
			      0  aconst_null
			      1  astore_2 [o]
			      2  iconst_0
			      3  istore_3 [i]
			      4  goto 103
			      7  iload_3 [i]
			      8  bipush 90
			     10  if_icmpge 100
			     13  new X [1]
			     16  dup
			     17  invokespecial X() [16]
			     20  astore_1 [x]
			     21  iload_3 [i]
			     22  iconst_4
			     23  if_icmple 37
			     26  new java.lang.Object [3]
			     29  dup
			     30  invokespecial java.lang.Object() [8]
			     33  astore_2 [o]
			     34  goto 39
			     37  aconst_null
			     38  astore_2 [o]
			     39  iconst_2
			     40  tableswitch default: 76
			          case 0: 60
			     60  aload_2 [o]
			     61  instanceof java.lang.String [17]
			     64  ifeq 92
			     67  getstatic java.lang.System.out : java.io.PrintStream [19]
			     70  ldc <String "1"> [25]
			     72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
			     75  return
			     76  aload_2 [o]
			     77  astore 4 [diff]
			     79  aload 4 [diff]
			     81  ifnull 92
			     84  getstatic java.lang.System.out : java.io.PrintStream [19]
			     87  ldc <String "2"> [33]
			     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
			     92  getstatic java.lang.System.out : java.io.PrintStream [19]
			     95  ldc <String "3"> [35]
			     97  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
			    100  iinc 3 1 [i]
			    103  iload_3 [i]
			    104  bipush 10
			    106  if_icmplt 7
			    109  return
			      Line numbers:
			        [pc: 0, line: 5]
			        [pc: 2, line: 6]
			        [pc: 7, line: 7]
			        [pc: 13, line: 8]
			        [pc: 21, line: 9]
			        [pc: 26, line: 10]
			        [pc: 34, line: 11]
			        [pc: 37, line: 12]
			        [pc: 39, line: 14]
			        [pc: 60, line: 16]
			        [pc: 67, line: 17]
			        [pc: 75, line: 18]
			        [pc: 76, line: 23]
			        [pc: 79, line: 24]
			        [pc: 84, line: 25]
			        [pc: 92, line: 30]
			        [pc: 100, line: 6]
			        [pc: 109, line: 33]
			      Local variable table:
			        [pc: 0, pc: 110] local: args index: 0 type: java.lang.String[]
			        [pc: 21, pc: 100] local: x index: 1 type: X
			        [pc: 2, pc: 110] local: o index: 2 type: java.lang.Object
			        [pc: 4, pc: 109] local: i index: 3 type: int
			        [pc: 79, pc: 92] local: diff index: 4 type: java.lang.Object
			"""
		:
			"""
				  // Method descriptor #15 ([Ljava/lang/String;)V
				  // Stack: 2, Locals: 5
				  public static void main(java.lang.String[] args);
				      0  aconst_null
				      1  astore_2 [o]
				      2  iconst_0
				      3  istore_3 [i]
				      4  goto 103
				      7  iload_3 [i]
				      8  bipush 90
				     10  if_icmpge 100
				     13  new X [1]
				     16  dup
				     17  invokespecial X() [16]
				     20  astore_1 [x]
				     21  iload_3 [i]
				     22  iconst_4
				     23  if_icmple 37
				     26  new java.lang.Object [3]
				     29  dup
				     30  invokespecial java.lang.Object() [8]
				     33  astore_2 [o]
				     34  goto 39
				     37  aconst_null
				     38  astore_2 [o]
				     39  iconst_2
				     40  tableswitch default: 76
				          case 0: 60
				     60  aload_2 [o]
				     61  instanceof java.lang.String [17]
				     64  ifeq 92
				     67  getstatic java.lang.System.out : java.io.PrintStream [19]
				     70  ldc <String "1"> [25]
				     72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
				     75  return
				     76  aload_2 [o]
				     77  astore 4 [diff]
				     79  aload 4 [diff]
				     81  ifnull 92
				     84  getstatic java.lang.System.out : java.io.PrintStream [19]
				     87  ldc <String "2"> [33]
				     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
				     92  getstatic java.lang.System.out : java.io.PrintStream [19]
				     95  ldc <String "3"> [35]
				     97  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]
				    100  iinc 3 1 [i]
				    103  iload_3 [i]
				    104  bipush 10
				    106  if_icmplt 7
				    109  return
				      Line numbers:
				        [pc: 0, line: 5]
				        [pc: 2, line: 6]
				        [pc: 7, line: 7]
				        [pc: 13, line: 8]
				        [pc: 21, line: 9]
				        [pc: 26, line: 10]
				        [pc: 34, line: 11]
				        [pc: 37, line: 12]
				        [pc: 39, line: 14]
				        [pc: 60, line: 16]
				        [pc: 67, line: 17]
				        [pc: 75, line: 18]
				        [pc: 76, line: 23]
				        [pc: 79, line: 24]
				        [pc: 84, line: 25]
				        [pc: 92, line: 30]
				        [pc: 100, line: 6]
				        [pc: 109, line: 33]
				      Local variable table:
				        [pc: 0, pc: 110] local: args index: 0 type: java.lang.String[]
				        [pc: 21, pc: 100] local: x index: 1 type: X
				        [pc: 2, pc: 110] local: o index: 2 type: java.lang.Object
				        [pc: 4, pc: 109] local: i index: 3 type: int
				        [pc: 79, pc: 92] local: diff index: 4 type: java.lang.Object
				      Stack map table: number of frames 8
				        [pc: 7, full, stack: {}, locals: {java.lang.String[], _, java.lang.Object, int}]
				        [pc: 37, full, stack: {}, locals: {java.lang.String[], X, java.lang.Object, int}]
				        [pc: 39, same]
				        [pc: 60, same]
				        [pc: 76, same]
				        [pc: 92, same]
				        [pc: 100, full, stack: {}, locals: {java.lang.String[], _, java.lang.Object, int}]
				        [pc: 103, same]
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
				void foo1(int i) {
					switch (i) {
						case 0://OK
						case 1://OK
							System.out.println();
							//$FALL-THROUGH$
						case 2://OK
							System.out.println(); //$FALL-THROUGH$
						case 3://OK
							System.out.println();
							//$FALL-THROUGH$ - some allowed explanation
						case 4://OK
						case 5://OK
							System.out.println();
							//$FALL-THROUGH$ - not last comment, thus inoperant
							// last comment is not fall-through explanation
						case 6://WRONG
							//$FALL-THROUGH$ - useless since not leading the case
							System.out.println();
							/*$FALL-THROUGH$ - block comment, is also allowed */
						case 7://OK
							System.out.println("aa"); //$NON-NLS-1$
					}
				}
			}
			""",
	},
	"""
		----------
		1. ERROR in X.java (at line 18)
			case 6://WRONG
			^^^^^^
		Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
		----------
		""",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257 - variation
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
				void foo1(int i) {
					switch (i) {
						case 0://OK
						case 1://OK
							System.out.println();
							//	  $FALL-THROUGH$
						case 2://OK
							System.out.println(); // 	 $FALL-THROUGH$
						case 3://OK
							System.out.println();
							//	$FALL-THROUGH$ - some allowed explanation
						case 4://OK
						case 5://OK
							System.out.println();
							// $FALL-THROUGH$ - not last comment, thus inoperant
							// last comment is not fall-through explanation
						case 6://WRONG
							// $FALL-THROUGH$ - useless since not leading the case
							System.out.println();
							/* $FALL-THROUGH$ - block comment, is also allowed */
						case 7://OK
							System.out.println("aa"); //$NON-NLS-1$
					}
				}
			}
			""",
	},
	"""
		----------
		1. ERROR in X.java (at line 18)
			case 6://WRONG
			^^^^^^
		Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
		----------
		""",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257 - variation
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
				void foo1(int i) {
					switch (i) {
						case 0://OK
						case 1://OK
							System.out.println();
							//	  $FALL-THROUGH - missing trailing $ in tag
						case 2://WRONG
							System.out.println();
					}
				}
			}
			""",
	},
	"""
		----------
		1. ERROR in X.java (at line 8)
			case 2://WRONG
			^^^^^^
		Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
		----------
		""",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257 - variation
public void test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
				void foo1(char previousChar) {
					switch(previousChar) {
						case \'/\':
							if (previousChar == \'*\') {
								// End of javadoc
								break;
								//$FALL-THROUGH$ into default case
							}
						default :
					}
				}
			}
			""",
	},
	"""
		----------
		1. ERROR in X.java (at line 10)
			default :
			^^^^^^^
		Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
		----------
		""",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286682
public void test018() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public static void foo(int i) {\s
			    switch (i) {
			    }
			  }
			}
			""",
	},
	new ASTVisitor() {
		public boolean visit(SingleNameReference reference, BlockScope scope) {
			assertNotNull("No scope", scope);
			return true;
		}
	}
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test019() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					try {
						switch((Integer) null) {};
						System.out.println("FAILED");
					} catch(NullPointerException e) {
						System.out.println("SUCCESS");
					}
				}
			}
			""",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test020() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					try {
						switch(foo()) {};
						System.out.println("FAILED");
					} catch(NullPointerException e) {
						System.out.println("SUCCESS");
					}
				}\
				static Integer foo() {
					return (Integer) null;
				}
			}
			""",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test021() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					try {
						switch((Character) null) {
							default: System.out.println("FAILED");
						}
					} catch(NullPointerException e) {
						System.out.println("SUCCESS");
					}
				}
			}
			""",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test022() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					java.math.RoundingMode mode = null;
					try {
						switch (mode) {}
						System.out.println("FAILED");
					} catch(NullPointerException e) {
						System.out.println("SUCCESS");
					}
				}
			}
			""",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test023() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					java.math.RoundingMode mode = java.math.RoundingMode.FLOOR;
					try {
						switch (mode) {
							default: System.out.println("SUCCESS");
						}
					} catch(NullPointerException e) {
						System.out.println("FAILED");
					}
				}
			}
			""",
	},
	"SUCCESS");
}

// JDK7: Strings in Switch.
public void testStringSwitchAtJDK6() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 4)
				default: return args;
				         ^^^^^^^^^^^^
			Void methods cannot return a value
			----------
			""";
		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 3)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			2. ERROR in X.java (at line 4)
				default: return args;
				         ^^^^^^^^^^^^
			Void methods cannot return a value
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						switch(args[0]) {
						default: return args;
						}
					}
				}
				""",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}

//JDK7: Strings in Switch.
public void testCaseTypeMismatch() {
	String newMessage =
		"""
		----------
		1. ERROR in X.java (at line 4)
			case 123: break;
			     ^^^
		Type mismatch: cannot convert from int to String
		----------
		2. ERROR in X.java (at line 5)
			case (byte) 1: break;
			     ^^^^^^^^
		Type mismatch: cannot convert from byte to String
		----------
		3. ERROR in X.java (at line 6)
			case (char) 2: break;
			     ^^^^^^^^
		Type mismatch: cannot convert from char to String
		----------
		4. ERROR in X.java (at line 7)
			case (short)3: break;
			     ^^^^^^^^
		Type mismatch: cannot convert from short to String
		----------
		5. ERROR in X.java (at line 8)
			case (int) 4: break;
			     ^^^^^^^
		Type mismatch: cannot convert from int to String
		----------
		6. ERROR in X.java (at line 9)
			case (long) 5: break;
			     ^^^^^^^^
		Type mismatch: cannot convert from long to String
		----------
		7. ERROR in X.java (at line 10)
			case (float) 6: break;
			     ^^^^^^^^^
		Type mismatch: cannot convert from float to String
		----------
		8. ERROR in X.java (at line 11)
			case (double) 7: break;
			     ^^^^^^^^^^
		Type mismatch: cannot convert from double to String
		----------
		9. ERROR in X.java (at line 12)
			case (boolean) 8: break;
			     ^^^^^^^^^^^
		Cannot cast from int to boolean
		----------
		10. ERROR in X.java (at line 12)
			case (boolean) 8: break;
			     ^^^^^^^^^^^
		Type mismatch: cannot convert from boolean to String
		----------
		""";
		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 3)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			2. ERROR in X.java (at line 12)
				case (boolean) 8: break;
				     ^^^^^^^^^^^
			Cannot cast from int to boolean
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						switch(args[0]) {
						case 123: break;
				       case (byte) 1: break;
				       case (char) 2: break;
				       case (short)3: break;
				       case (int) 4: break;
				       case (long) 5: break;
				       case (float) 6: break;
				       case (double) 7: break;
				       case (boolean) 8: break;
						}
					}
				}
				""",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testCaseTypeMismatch2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String newMessage =
		"""
		----------
		1. ERROR in X.java (at line 7)
			case Days.Sunday: break;
			     ^^^^^^^^^^^
		Type mismatch: cannot convert from Days to String
		----------
		""";
		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 6)
				switch ("Sunday") {
				        ^^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				enum Days { Sunday, Monday, Tuesday, Wednesday, Thuresday, Friday, Satuday };
				
				public class X {
				
				    public static void main(String argv[]) {
				        switch ("Sunday") {
				            case Days.Sunday: break;
				        }
				    }
				}
				""",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testCaseTypeMismatch3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String newMessage =
		"""
		----------
		1. ERROR in X.java (at line 7)
			case "0": break;
			     ^^^
		Type mismatch: cannot convert from String to int
		----------
		2. ERROR in X.java (at line 10)
			case "Sunday": break;
			     ^^^^^^^^
		Type mismatch: cannot convert from String to Days
		----------
		3. ERROR in X.java (at line 13)
			case "0": break;
			     ^^^
		Type mismatch: cannot convert from String to Integer
		----------
		""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				enum Days { Sunday, Monday, Tuesday, Wednesday, Thuresday, Friday, Satuday };
				
				public class X {
				
				    public static void main(String argv[]) {
				        switch (argv.length) {
				            case "0": break;
				        }
				        switch(Days.Sunday) {
				            case "Sunday": break;
				        }
				        switch (Integer.valueOf(argv.length)) {
				            case "0": break;
				        }
				    }
				}
				""",
		},
		newMessage);
}
// JDK7: Strings in Switch.
public void testDuplicateCase() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 4)
				case "123": break;
				^^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 5)
				case "123": break;
				^^^^^^^^^^
			Duplicate case
			----------
			3. ERROR in X.java (at line 6)
				default: return args;
				         ^^^^^^^^^^^^
			Void methods cannot return a value
			----------
			""";

		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 3)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			2. ERROR in X.java (at line 6)
				default: return args;
				         ^^^^^^^^^^^^
			Void methods cannot return a value
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String [] args) {
						switch(args[0]) {
						case "123": break;
						case "123": break;
				       default: return args;
						}
					}
				}
				""",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}

// JDK7: Strings in Switch.
public void testDuplicateCase2() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 9)
				case "123": break;
				^^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 10)
				case "123": break;
				^^^^^^^^^^
			Duplicate case
			----------
			3. ERROR in X.java (at line 11)
				case "1" + "2" + "3": break;
				^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			4. ERROR in X.java (at line 13)
				case local: break;
				^^^^^^^^^^
			Duplicate case
			----------
			5. ERROR in X.java (at line 14)
				case field: break;
				^^^^^^^^^^
			Duplicate case
			----------
			6. ERROR in X.java (at line 15)
				case ifield: break;
				     ^^^^^^
			Cannot make a static reference to the non-static field ifield
			----------
			7. ERROR in X.java (at line 16)
				case inffield: break;
				     ^^^^^^^^
			Cannot make a static reference to the non-static field inffield
			----------
			8. ERROR in X.java (at line 19)
				default: break;
				^^^^^^^
			The default case is already defined
			----------
			""";

		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 8)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			2. ERROR in X.java (at line 15)
				case ifield: break;
				     ^^^^^^
			Cannot make a static reference to the non-static field ifield
			----------
			3. ERROR in X.java (at line 16)
				case inffield: break;
				     ^^^^^^^^
			Cannot make a static reference to the non-static field inffield
			----------
			4. ERROR in X.java (at line 19)
				default: break;
				^^^^^^^
			The default case is already defined
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				    static final String field = "123";
				    final String ifield = "123";
				    String inffield = "123";
				    static String nffield = "123";
				    public static void main(String [] args, final String argument) {
				        final String local = "123";
					switch(args[0]) {
					   case "123": break;
				      case "\u0031\u0032\u0033": break;
					   case "1" + "2" + "3": break;
				           default: break;
					   case local: break;
				           case field: break;
				           case ifield: break;
				           case inffield: break;
				           case nffield: break;
				           case argument: break;
				           default: break;
					}
				    }
				}
				"""
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testVariableCase() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 7)
				case local: break;
				     ^^^^^
			case expressions must be constant expressions
			----------
			2. ERROR in X.java (at line 8)
				case argument: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			3. ERROR in X.java (at line 9)
				case inffield: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			4. ERROR in X.java (at line 10)
				case nffield: break;
				     ^^^^^^^
			case expressions must be constant expressions
			----------
			5. ERROR in X.java (at line 11)
				case argument: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			""";

		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 6)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				    String inffield = "123";
				    static String nffield = "123";
				    public void main(String [] args, final String argument) {
				        String local = "123";
					switch(args[0]) {
					   case local: break;
					   case argument: break;
				      case inffield: break;
				      case nffield: break;
				      case argument: break;
					}
				    }
				}
				"""
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testVariableCaseFinal() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 8)
				case argument: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			2. ERROR in X.java (at line 11)
				case argument: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			""";

		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 6)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				    final String inffield = "12312";
				    final static String nffield = "123123";
				    public void main(String [] args, final String argument) {
				        final String local = "1233";
					switch(args[0]) {
					   case local: break;
					   case argument: break;
				      case inffield: break;
				      case nffield: break;
				      case argument: break;
					}
				    }
				}
				"""
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
//JDK7: Strings in Switch.
public void testNullCase() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 7)
				case local: break;
				     ^^^^^
			case expressions must be constant expressions
			----------
			2. ERROR in X.java (at line 8)
				case argument: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			3. ERROR in X.java (at line 9)
				case inffield: break;
				     ^^^^^^^^
			case expressions must be constant expressions
			----------
			4. ERROR in X.java (at line 10)
				case nffield: break;
				     ^^^^^^^
			case expressions must be constant expressions
			----------
			5. ERROR in X.java (at line 11)
				case (String) null: break;
				     ^^^^^^^^^^^^^
			case expressions must be constant expressions
			----------
			6. ERROR in X.java (at line 12)
				case true ? (String) null : (String) null : break;
				     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			case expressions must be constant expressions
			----------
			7. WARNING in X.java (at line 12)
				case true ? (String) null : (String) null : break;
				                            ^^^^^^^^^^^^^
			Dead code
			----------
			""";

		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 6)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				    final String inffield = null;
				    final static String nffield = null;
				    public void main(String [] args, final String argument) {
				        final String local = null;
					switch(args[0]) {
					   case local: break;
					   case argument: break;
				      case inffield: break;
				      case nffield: break;
				      case (String) null: break;
				      case true ? (String) null : (String) null : break;
					}
				    }
				}
				"""
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testDuplicateCase3() {
		String newMessage =
			"""
			----------
			1. ERROR in X.java (at line 9)
				case "123": break;
				^^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 10)
				case "1" + "2" + "3": break;
				^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			3. ERROR in X.java (at line 12)
				case local: break;
				^^^^^^^^^^
			Duplicate case
			----------
			4. ERROR in X.java (at line 13)
				case field: break;
				^^^^^^^^^^
			Duplicate case
			----------
			5. ERROR in X.java (at line 14)
				case ifield: break;
				^^^^^^^^^^^
			Duplicate case
			----------
			6. ERROR in X.java (at line 18)
				default: break;
				^^^^^^^
			The default case is already defined
			----------
			""";

		String oldMessage =
			"""
			----------
			1. ERROR in X.java (at line 8)
				switch(args[0]) {
				       ^^^^^^^
			Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
			----------
			2. ERROR in X.java (at line 18)
				default: break;
				^^^^^^^
			The default case is already defined
			----------
			""";

		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				    static final String field = "123";
				    final String ifield = "123";
				    String inffield = "123";
				    static String nffield = "123";
				    public  void main(String [] args, final String argument) {
				        final String local = "123";
					switch(args[0]) {
					   case "123": break;
					   case "1" + "2" + "3": break;
				           default: break;
					   case local: break;
				           case field: break;
				           case ifield: break;
				           case inffield: break;
				           case nffield: break;
				           case argument: break;
				           default: break;
					}
				    }
				}
				"""
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}

public void testDuplicateHashCode() {
	String errorMsg =
		"""
		----------
		1. ERROR in testDuplicateHashCode.java (at line 5)
			switch (dispatcher) {
			        ^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testDuplicateHashCode.java",
		"""
			public class testDuplicateHashCode {
				public static void main(String[] argv) {
					String dispatcher = "\u0000";
					outer: for (int i = 0; i < 100; i++) {
						switch (dispatcher) {
						case "\u0000":
							System.out.print("1 ");
							break;
						case "\u0000\u0000":
							System.out.print("2 ");
							break;
						case "\u0000\u0000\u0000":
							System.out.print("3 ");
							break;
						case "\u0000\u0000\u0000\u0000":
							System.out.print("4 ");
							break;
						case "\u0000\u0000\u0000\u0000\u0000":
							System.out.print("5 ");
							break;
						default:
							System.out.println("Default");
							break outer;
						case "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000":
							System.out.print("8 ");
							break;
						case "\u0000\u0000\u0000\u0000\u0000\u0000\u0000":
							System.out.print("7 ");
							break;
						case "\u0000\u0000\u0000\u0000\u0000\u0000":
							System.out.print("6 ");
							break;
						}
						dispatcher += "\u0000";
					}
				}
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "1 2 3 4 5 6 7 8 Default");
	}
}
public void testDuplicateHashCode2() {
	String errorMsg =
		"""
		----------
		1. ERROR in testDuplicateHashCode.java (at line 5)
			switch (dispatcher) {
			        ^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testDuplicateHashCode.java",
		"""
			public class testDuplicateHashCode {
				public static void main(String[] argv) {
					String dispatcher = "\u0000";
					outer: while(true) {
						switch (dispatcher) {
						case "\u0000":
							System.out.print("1 ");
			               dispatcher += "\u0000\u0000";
							break;
						case "\u0000\u0000":
							System.out.print("2 ");
			               dispatcher = "";
							break;
						case "\u0000\u0000\u0000":
							System.out.print("3 ");
			               dispatcher += "\u0000\u0000";
							break;
						case "\u0000\u0000\u0000\u0000":
							System.out.print("4 ");
			               dispatcher = "\u0000\u0000";
							break;
						case "\u0000\u0000\u0000\u0000\u0000":
							System.out.print("5 ");
			               dispatcher += "\u0000\u0000";
							break;
						default:
							System.out.println("Default");
							break outer;
						case "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000":
							System.out.print("8 ");
			               dispatcher = "\u0000\u0000\u0000\u0000\u0000\u0000";
							break;
						case "\u0000\u0000\u0000\u0000\u0000\u0000\u0000":
							System.out.print("7 ");
			               dispatcher += "\u0000";
							break;
						case "\u0000\u0000\u0000\u0000\u0000\u0000":
							System.out.print("6 ");
			               dispatcher = "\u0000\u0000\u0000\u0000";
							break;
						}
					}
				}
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "1 3 5 7 8 6 4 2 Default");
	}
}
public void testSwitchOnNull() {
	String errorMsg =
		"""
		----------
		1. ERROR in testSwitchOnNull.java (at line 13)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		2. ERROR in testSwitchOnNull.java (at line 23)
			switch ((String) null) {
			        ^^^^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		3. ERROR in testSwitchOnNull.java (at line 33)
			switch (someMethod()) {
			        ^^^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		4. ERROR in testSwitchOnNull.java (at line 40)
			switch (nullString) {
			        ^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		5. ERROR in testSwitchOnNull.java (at line 47)
			switch (someMethod()) {
			        ^^^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testSwitchOnNull.java",
		"""
			public class testSwitchOnNull {
			
			    private static String someMethod() {
			        return null;
			    }
			
			    static String nullString = null;
			    public static void main(String [] args) {
			
			        String s = null;
			
			        try {
			            switch (s) {
			                default:\s
			                    System.out.println("OOPS");
				            break;
			            }
			            System.out.println("OOPS");
			        } catch (NullPointerException e) {
			            System.out.print("NPE1");
			        }
			        try {
			            switch ((String) null) {
			                default:\s
			                    System.out.println("OOPS");
				            break;
			            }
			            System.out.println("OOPS");
			        } catch (NullPointerException e) {
			            System.out.print("NPE2");
			        }
			        try {
			            switch (someMethod()) {
			            }
			            System.out.println("OOPS");
			        } catch (NullPointerException e) {
			            System.out.print("NPE3");
			        }
			        try {
			            switch (nullString) {
			            }
			            System.out.println("OOPS");
			        } catch (NullPointerException e) {
			            System.out.print("NPE4");
			        }
			        try {
			            switch (someMethod()) {
			                default:\s
			                    System.out.println("OOPS");
				            break;
			            }
			            System.out.println("OOPS");
			        } catch (NullPointerException e) {
			            System.out.print("NPE5");
			        }
			    }
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "NPE1NPE2NPE3NPE4NPE5");
	}
}
public void testSideEffect() {
	String errorMsg =
		"""
		----------
		1. ERROR in testSideEffect.java (at line 11)
			switch(dispatcher()) {
			       ^^^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testSideEffect.java",
		"""
			public class testSideEffect {
			    static boolean firstTime = true;
				private static String dispatcher() {
			    	if (!firstTime) {
					System.out.print("OOPS");
			    	}
			    	firstTime = false;
			    	return "\u0000";
			    }
			    public static void main(String [] args) {
			    		switch(dispatcher()) {
			    		case "\u0000\u0000": break;
			    		case "\u0000\u0000\u0000":	break;
			    		case "\u0000\u0000\u0000\u0000": break;
			    		case "\u0000\u0000\u0000\u0000\u0000": break;
			    		default: System.out.println("DONE");
			    		}
			    }
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testFallThrough() {
	String errorMsg =
		"""
		----------
		1. ERROR in testFallThrough.java (at line 11)
			switch(s = dispatcher()) {
			       ^^^^^^^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testFallThrough.java",
		"""
			public class testFallThrough {
			    static int index = -1;
			    static String string = "0123456789*";
			    private static String dispatcher() {
			    	index++;
			     	return string.substring(index,index + 1);
			    }
			    public static void main(String [] args) {
			    	outer: while (true) {
			    		String s = null;
			    		switch(s = dispatcher()) {
			    		case "2":
			    		case "0":
			    		case "4":
			    		case "8":
			    		case "6":
			    				System.out.print(s + "(even) ");
			    				break;
			    		case "1":
			    		case "3":
			    		case "9":
			    		case "5":
			    		case "7":
			    				System.out.print(s + "(odd) ");
			    				break;
			    		default: System.out.print("DONE");
			    				break outer;
			    		}
			    	}
			    }
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "0(even) 1(odd) 2(even) 3(odd) 4(even) 5(odd) 6(even) 7(odd) 8(even) 9(odd) DONE");
	}
}
public void testFallThrough2() {
	String errorMsg =
		"""
		----------
		1. ERROR in testFallThrough.java (at line 11)
			switch(s = dispatcher()) {
			       ^^^^^^^^^^^^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testFallThrough.java",
		"""
			public class testFallThrough {
			    static int index = -1;
			    static String string = "0123456789*";
			    private static String dispatcher() {
			    	index++;
			     	return string.substring(index,index + 1);
			    }
			    public static void main(String [] args) {
			    	outer: while (true) {
			    		String s = null;
			    		switch(s = dispatcher()) {
			    		case "4": System.out.print(s);
			    		case "3": System.out.print(s);
			    		case "2": System.out.print(s);
			    		case "1": System.out.print(s + " ");
			    		case "0": break;
			    		default: System.out.print("DONE");
			    				break outer;
			    		}
			    	}
			    }
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "1 22 333 4444 DONE");
	}
}
public void testMarysLamb() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}

	String errorMsg =
		"""
		----------
		1. ERROR in testMarysLamb.java (at line 4)
			switch(s) {
			       ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testMarysLamb.java",
		"""
			public class testMarysLamb {
			    public static void main(String [] args) {
			    	for (String s : new String [] { "Mary", "Had", "A", "Little", "Lamb" }) {
			    		switch(s) {
			    			default: System.out.print(s + " ");
			    		}
			    	}
			    }
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "Mary Had A Little Lamb");
	}
}
public void testBreakOut() {
	String errorMsg =
		"""
		----------
		1. ERROR in testBreakOut.java (at line 5)
			switch(s) {
			       ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"testBreakOut.java",
		"""
			public class testBreakOut {
			    public static void main(String [] args) {
			    	junk: while (true) {
			    		String s = "";
			    		switch(s) {
			    		case "7":
			    				System.out.print(s + "(odd) ");
			    				break;
			    		default: System.out.print("DONE");
			    				 break junk;
			    		}
			    	}
			    }
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testMultipleSwitches() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String errorMsg =
		"""
		----------
		1. ERROR in X.java (at line 6)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		2. ERROR in X.java (at line 35)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		3. ERROR in X.java (at line 51)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			
				public static void main(String[] args) {
				\t
					for (String s: new String [] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "DONE"}) {
						switch (s) {
						case "Sunday" :\s
							System.out.print("Sunday");
							break;
						case "Monday" :
							System.out.print("Monday");
							break;
						case "Tuesday" :
							System.out.print("Tuesday");
							break;
						case "Wednesday":
							System.out.print("Wednesday");
							break;
						case "Thursday":
							System.out.print("Thursday");
							break;
						case "Friday":
							System.out.print("Friday");
							break;
						case "Saturday":
							System.out.print("Saturday");
							break;
						default:
							System.out.print(" ---- ");
							break;
						}
					}
				 \s
					for (String s: new String [] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "DONE"}) {
						switch (s) {
						case "Sunday" :\s
						case "Monday" :
						case "Tuesday" :
						case "Wednesday":
						case "Thursday":
						case "Friday":
						case "Saturday":
							System.out.print(s);
							break;
						default:
							System.out.print(" ---- ");
							break;
						}\t
					}
					for (String s: new String [] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "DONE"}) {
						switch (s) {
						case "Saturday":
						case "Sunday" :\s
							System.out.print("Holiday");
							break;
						case "Monday" :
						case "Tuesday" :
						case "Wednesday":
						case "Thursday":
						case "Friday":
							System.out.print("Workday");
							break;
						default:
							System.out.print(" DONE");
							break;
						}
					}
				}
			
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "SundayMondayTuesdayWednesdayThursdayFridaySaturday ---- SundayMondayTuesdayWednesdayThursdayFridaySaturday ---- HolidayWorkdayWorkdayWorkdayWorkdayWorkdayHoliday DONE");
	}
}
public void testNestedSwitches() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String errorMsg =
		"""
		----------
		1. ERROR in X.java (at line 4)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		2. ERROR in X.java (at line 7)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		3. ERROR in X.java (at line 18)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					for (String s: new String [] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "DONE"}) {
						switch (s) {
						case "Saturday":
						case "Sunday" :\s
							switch (s) {
								case "Saturday" : System.out.println ("Saturday is a holiday"); break;
								case "Sunday"  :  System.out.println ("Sunday is a holiday"); break;
								default:          System.out.println("Broken");
							}
							break;
						case "Monday" :
						case "Tuesday" :
						case "Wednesday":
						case "Thursday":
						case "Friday":
							switch (s) {
								case "Monday" :  System.out.println ("Monday is a workday"); break;
								case "Tuesday" : System.out.println ("Tuesday is a workday"); break;
								case "Wednesday": System.out.println ("Wednesday is a workday"); break;
								case "Thursday": System.out.println ("Thursday is a workday"); break;
								case "Friday":System.out.println ("Friday is a workday"); break;
								default: System.out.println("Broken");
							}
							break;
						default:
							System.out.println("DONE");
							break;
						}
					}
				}
			}
			""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, """
			Sunday is a holiday
			Monday is a workday
			Tuesday is a workday
			Wednesday is a workday
			Thursday is a workday
			Friday is a workday
			Saturday is a holiday
			DONE""");
	}
}
public void testFor356002() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 6)
			switch (foo()) {
			        ^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
				private static String foo() {
					return "";
				}
				public static void main(String[] args) {
					switch (foo()) {
						default: {
							int j = 0;
							if (j <= 0)
								System.out.println("DONE");
						}
						return;
					}
				}
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testFor356002_2() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 3)
			switch ("") {
			        ^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					switch ("") {
						default: {
							int j = 0;
							if (j <= 0)
								System.out.println("DONE");
						}
						return;
					}
				}
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testFor356002_3() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 7)
			switch (foo()) {
			        ^^^^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
				private static String foo() {
					return null;
				}
				public static void main(String[] args) {
					try {
						switch (foo()) {
							default: {
								int j = 0;
								if (j <= 0)
									;
							}
							return;
						}
					} catch(NullPointerException e) {
						System.out.println("DONE");
					}
				}
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testBug374605() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					class X {
					  void v(int i) {
					    switch (i) {
					      case 1 :
					        break;
					      case 2 :
					        break;
					    }
					  }
					}""",
			},
			"""
				----------
				1. WARNING in p\\X.java (at line 4)
					switch (i) {
					        ^
				The switch statement should have a default case
				----------
				""",
			null,
			true,
			options
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927() {
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
					        System.out.println("Success");
					    }
					}
					""",
			},
			"Success");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public final static Object f() {
					        final Object a = null;
					        Object b;
					        label: while (true) {
					            switch (0) {
					            case 1: {
					                b = a;
					            }
					                break;
					            default:
					                break label;
					            }
					        }
					        return a;
					    }
					    public static void main(final String[] args) {
					        f();
					        System.out.println("Success");
					    }
					}
					""",
			},
			"Success");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public final static Object f() {
					        final Object a = null;
					        Object b;
					        label: for(;;) {
					            switch (0) {
					            case 1: {
					                b = a;
					            }
					                break;
					            default:
					                break label;
					            }
					        }
					        return a;
					    }
					    public static void main(final String[] args) {
					        f();
					        System.out.println("Success");
					    }
					}
					""",
			},
			"Success");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public final static Object f() {
					        final Object a = null;
					        Object b;
					        label: for(int i : new int [] { 10 }) {
					            switch (0) {
					            case 1: {
					                b = a;
					            }
					                break;
					            default:
					                break label;
					            }
					        }
					        return a;
					    }
					    public static void main(final String[] args) {
					        f();
					        System.out.println("Success");
					    }
					}
					""",
			},
			"Success");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        Object b;
					        label: do {
					            switch (0) {
					            case 1:
					                b = null;
					                break;
					            default:
					                break label;
					            }
					        } while (true);
					        System.out.println(b);
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					System.out.println(b);
					                   ^
				The local variable b may not have been initialized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        Object b;
					        label: while (true) {
					            switch (0) {
					            case 1:
					                b = null;
					                break;
					            default:
					                break label;
					            }
					        }
					        System.out.println(b);
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					System.out.println(b);
					                   ^
				The local variable b may not have been initialized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927f() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        Object b;
					        label: for(;;) {
					            switch (0) {
					            case 1:
					                b = null;
					                break;
					            default:
					                break label;
					            }
					        }
					        System.out.println(b);
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					System.out.println(b);
					                   ^
				The local variable b may not have been initialized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927g() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        Object b;
					        label: for(int i : new int [] { 10 }) {
					            switch (0) {
					            case 1:
					                b = null;
					                break;
					            default:
					                break label;
					            }
					        }
					        System.out.println(b);
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					System.out.println(b);
					                   ^
				The local variable b may not have been initialized
				----------
				""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383629
// To check that code gen is ok
public void testBug383629() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					  public static void main(String[] args) {
					    char  chc;        \s
					     do {     \s
					        if (args == null) {     \s
					           switch ('a') {    \s
					           case '\\n':     \s
					                 chc = 'b';
					           }              \s
					        } else {           \s
					           switch ('a') {      \s
					              case '\\r':
					           }         \s
					        }
					     } while (false);
					     System.out.println("Done");
					  }
				}""",
		}); // custom requestor

	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK1_6 ?
			"      Local variable table:\n" +
			"        [pc: 0, pc: 61] local: args index: 0 type: java.lang.String[]\n":
				"""
					      Local variable table:
					        [pc: 0, pc: 61] local: args index: 0 type: java.lang.String[]
					      Stack map table: number of frames 4
					        [pc: 24, same]
					        [pc: 27, same]
					        [pc: 30, same]
					        [pc: 52, same]
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

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381172
// To check that code gen is ok
public void testBug381172() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args){
				        System.out.println("Test");
				    }
				    public void method() {
				        try {
				            int rc;
				            switch ( 0 )
				            {
				                case 0:
				                    rc = 0;
				                    setRC( rc );
				                    break;
				                case 1:
				                    rc = 1;
				                    setRC( 0 );
				                    break;
				                case 2:
				                    rc = 2;
				                    setRC( 0 );
				                    break;
				                default:
				                    break;
				            }
				        }
				        catch ( final Exception ex ) {}
				    }
				    private void setRC(int rc) {}
				}""",
		}); // custom requestor

	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK1_6 ?
			"""
				      Local variable table:
				        [pc: 0, pc: 1] local: this index: 0 type: X
				        [pc: 0, pc: 1] local: rc index: 1 type: int
				""":
				"""
					      Local variable table:
					        [pc: 0, pc: 63] local: this index: 0 type: X
					        [pc: 30, pc: 38] local: rc index: 1 type: int
					        [pc: 40, pc: 48] local: rc index: 1 type: int
					        [pc: 50, pc: 58] local: rc index: 1 type: int
					      Stack map table: number of frames 6
					        [pc: 28, same]
					        [pc: 38, same]
					        [pc: 48, same]
					        [pc: 58, same]
					        [pc: 61, same_locals_1_stack_item, stack: {java.lang.Exception}]
					        [pc: 62, same]
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383643, NPE in problem reporter.
public void test383643() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    void foo() {
						        String s;
						        switch (p) {
						            case ONE:
						                s= "1";
						                break;
						            case TWO:
						                s= "2";
						                break;
						        }
						
						        s.toString();
						    }
						}
						""",
				},
			"""
				----------
				1. ERROR in X.java (at line 4)
					switch (p) {
					        ^
				p cannot be resolved to a variable
				----------
				2. WARNING in X.java (at line 4)
					switch (p) {
					        ^
				The switch statement should have a default case
				----------
				3. ERROR in X.java (at line 5)
					case ONE:
					     ^^^
				ONE cannot be resolved to a variable
				----------
				4. ERROR in X.java (at line 8)
					case TWO:
					     ^^^
				TWO cannot be resolved to a variable
				----------
				""",
			null,
			true,
			options
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=387146 - the fall-through comment is ignored
public void test387146a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
				private Object someLock;
				public void foo1(int i) {
					switch (i) {
					case 1:
						synchronized (someLock) {
							System.out.println();
						}
						//$FALL-THROUGH$
					case 2:
						System.out.println();
						break;
					default:
						System.out.println();
					}
				}
			}
			""",
	},
	"",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=387146 - the fall-through comment is respected
public void test387146b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
				private boolean someFlag;
				public void foo1(int i) {
					switch (i) {
					case 1:
						if (someFlag) {
							System.out.println();
						}
						//$FALL-THROUGH$
					case 2:
						System.out.println();
						break;
					default:
						System.out.println();
					}
				}
			}
			""",
	},
	"",
	null,
	true,
	options);
}
//JDK7: Strings in Switch.
public void test393537() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 3)
			switch ("") {
			        ^^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					switch ("") {
						case "":
						default:
					}
				}
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "");
	}
}
//JDK7: Strings in Switch.
public void test410892() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 5)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			   public void testFunction(String s) {
			        int var1 = 0;
			        int var2 = 0;
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		this.runConformTest(sourceFiles, options);
	}
}
//JDK7: Strings in Switch.
public void test410892_2() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 5)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			   public X(String s) {
			        int var1 = 0;
			        int var2 = 0;
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		this.runConformTest(sourceFiles, options);
	}
}
//JDK7: Strings in Switch.
public void test410892_3() {
	String errorMsg =
			"""
		----------
		1. ERROR in X.java (at line 6)
			switch (s) {
			        ^
		Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted
		----------
		""";

	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			   static {
			        int var1 = 0;
			        int var2 = 0;
			        String s = "test2";
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		this.runConformTest(sourceFiles, options);
	}
}
//JDK7: Strings in Switch.
public void test410892_4() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	String errorMsg =
			"""
		----------
		1. WARNING in X.java (at line 4)
			int var2 = 0;
			    ^^^^
		The value of the local variable var2 is not used
		----------
		""";
	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			   public void testFunction(String s) {
			        int var1 = 0;
			        int var2 = 0;
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel >= JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles,
			errorMsg,
			null,
			true,
			options);
	}
}
//JDK7: Strings in Switch.
public void test410892_5() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	String errorMsg =
			"""
		----------
		1. WARNING in X.java (at line 4)
			int var2 = 0;
			    ^^^^
		The value of the local variable var2 is not used
		----------
		""";
	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			   public X(String s) {
			        int var1 = 0;
			        int var2 = 0;
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel >= JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles,
			errorMsg,
			null,
			true,
			options);
	}
}
//JDK7: Strings in Switch.
public void test410892_6() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	String errorMsg =
			"""
		----------
		1. WARNING in X.java (at line 4)
			int var2 = 0;
			    ^^^^
		The value of the local variable var2 is not used
		----------
		""";
	String [] sourceFiles =
		new String[] {
		"X.java",
		"""
			public class X {
			   static {
			        int var1 = 0;
			        int var2 = 0;
			        String s = "Test2";
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel >= JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles,
			errorMsg,
			null,
			true,
			options);
	}
}
public void test526911() {
	String [] sourceFiles =
		new String[] {
		"Main.java",
		"""
			public class Main {
				public static void main(String[] args) {
					new Main().run();
				}
			\t
				private void run() {
					V v = new VA();
					I i = I.create(v);
					System.out.printf("%d %d", i.m1(), i.m2());
				}
			}
			""",
		"XI.java",
		"""
			public class XI implements I {
				V v;
				public XI(V v) {
					this.v = v;
				}
				@Override
				public int m1() {
					return 1;
				}
				@Override
				public int m2() {
					return 11;
				}
			}
			""",
		"YI.java",
		"""
			public class YI implements I {
				V v;
				public YI(V v) {
					this.v = v;
				}
				@Override
				public int m1() {
					return 2;
				}
				@Override
				public int m2() {
					return 22;
				}
			}
			""",
		"V.java",
		"""
			public class V {
				public enum T { A, B, C }
				private T t;
				public V(T t) {
					this.t = t;
				}
				public T getT() { return t; }
			}
			class VA extends V {
				VA() {
					super(T.A);
				}
			}""",
		"I.java",
		"""
			enum H { X, Y }
			public interface I {
				public static final int i = 0;
				public int m1();
				public int m2();
				public static I create(V v) {\s
					V.T t = v.getT();
					H h = getH(t);
					switch (h) { // depending on H i need different implementations of I. XI and YI provide them
					case X:
						return new XI(v);
					case Y:
						return new YI(v);
					default:
						throw new Error();
					}\t
				}
				static H getH(V.T t) { // different T's require different H's to handle them
					switch (t) {
					case A:
						return H.X;
					case B:
					case C:
						return H.Y;
					}
					throw new Error();
				}
			}""",
		"X.java",
		"""
			public class X {
			   static {
			        int var1 = 0;
			        int var2 = 0;
			        String s = "Test2";
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(sourceFiles, "1 11");
	}
}
public void test526911a() {
	// target 1.8, run with 9, should work fine
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
	String [] sourceFiles =
		new String[] {
		"Main.java",
		"""
			public class Main {
				public static void main(String[] args) {
					new Main().run();
				}
			\t
				private void run() {
					V v = new VA();
					I i = I.create(v);
					System.out.printf("%d %d", i.m1(), i.m2());
				}
			}
			""",
		"XI.java",
		"""
			public class XI implements I {
				V v;
				public XI(V v) {
					this.v = v;
				}
				@Override
				public int m1() {
					return 1;
				}
				@Override
				public int m2() {
					return 11;
				}
			}
			""",
		"YI.java",
		"""
			public class YI implements I {
				V v;
				public YI(V v) {
					this.v = v;
				}
				@Override
				public int m1() {
					return 2;
				}
				@Override
				public int m2() {
					return 22;
				}
			}
			""",
		"V.java",
		"""
			public class V {
				public enum T { A, B, C }
				private T t;
				public V(T t) {
					this.t = t;
				}
				public T getT() { return t; }
			}
			class VA extends V {
				VA() {
					super(T.A);
				}
			}""",
		"I.java",
		"""
			enum H { X, Y }
			public interface I {
				public static final int i = 0;
				public int m1();
				public int m2();
				public static I create(V v) {\s
					V.T t = v.getT();
					H h = getH(t);
					switch (h) { // depending on H i need different implementations of I. XI and YI provide them
					case X:
						return new XI(v);
					case Y:
						return new YI(v);
					default:
						throw new Error();
					}\t
				}
				static H getH(V.T t) { // different T's require different H's to handle them
					switch (t) {
					case A:
						return H.X;
					case B:
					case C:
						return H.Y;
					}
					throw new Error();
				}
			}""",
		"X.java",
		"""
			public class X {
			   static {
			        int var1 = 0;
			        int var2 = 0;
			        String s = "Test2";
			        switch (s) {
			        case "test":\s
			            var2 = ++var1 % 2;
			            break;
			        }
			   }
			}""",
	};
	this.runConformTest(sourceFiles, "1 11", options);
}
public void testBug533475() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runConformTest(
		new String[] {
			"SwitchBug.java",
			"""
				public class SwitchBug {
				    static class MyClass {
				        private static final Object C = "";
				
				        public enum State {
				            ENABLED(C); // pass null constant
				
				            State(Object value) {
				            } // value can be ignored
				        }
				
				        /* unused method with switch statement IN SAME CLASS */
				        private void unusedMethod() {
				            switch (State.ENABLED) {
				            case ENABLED:
				                break;
				            }
				        }
				    }
				   \s
				    public static void main(String[] args) {
				        // access enum values from an other class
				        MyClass.State.values();
				        System.out.println("It runs.");
				    }
				}
				"""
		});
}
public void testBug545518() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8 || this.complianceLevel >= ClassFileConstants.JDK12)
		return;
	String message =
			"""
		----------
		1. ERROR in X.java (at line 5)
			case "ABC", (false ? (String) "c" : (String) "d") : break;
			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Multi-constant case labels supported from Java 14 onwards only
		----------
		""";

	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String [] args) {
				  	 String arg = "ABD";
				    switch(arg) {
				      case "ABC", (false ? (String) "c" : (String) "d") : break;
					 }
				  }
				}
				"""
		},
		message);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576093
public void testBug576093a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						import java.util.HashMap;
						import java.util.Map;
						import java.util.Map.Entry;
						
						public class X {
							public static void main(String[] args) {
								Map<Z, Object> map = new HashMap<>();
								for (Entry<Z, Object> entry : map.entrySet()) {
									switch (entry.getKey()) {
									default:
										break;
									}
								}
								System.out.println("Success");
							}
							enum Z {
								A
							}
						}""",
			},
			"Success");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576093
public void testBug576093b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return;
	}
	this.runConformTest(
			new String[] {
				"X2.java",
				"""
					import java.util.Optional;
					
					public class X2 {
						public static void main(String[] args) {
							Optional<Z> o = Optional.of(Z.A);
							switch (o.get()) {
							default:
								break;
							}
							System.out.println("Success");
						}
						enum Z {
							A
						}
					}""",
			},
			"Success");
}
public void testBug443576_1() {
	if (this.complianceLevel < ClassFileConstants.JDK11) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
			    public enum E { A, B; }
			    public String f(E e) {
			        switch (e) {
			            case A: return "a";
			                break; //<-- ERROR: Unreachable code
			            case B: return "b";   //<-- WARNING: Switch case may be entered by falling through previous case
			                break; //<-- ERROR: Unreachable code
			            default: return "?";  //<-- WARNING: Switch case may be entered by falling through previous case
			                break; //<-- ERROR: Unreachable code
			        }
			    } \s
			}""",
	},
		"""
			----------
			1. ERROR in X.java (at line 6)
				break; //<-- ERROR: Unreachable code
				^^^^^^
			Unreachable code
			----------
			2. ERROR in X.java (at line 8)
				break; //<-- ERROR: Unreachable code
				^^^^^^
			Unreachable code
			----------
			3. ERROR in X.java (at line 10)
				break; //<-- ERROR: Unreachable code
				^^^^^^
			Unreachable code
			----------
			""",
	null,
	true,
	options);
}
// Same as above, but keep swap the return and break statements
public void testBug443576_2() {
	if (this.complianceLevel < ClassFileConstants.JDK11) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
			    public enum E { A, B; }
			    public String f(E e) {
			        switch (e) {
			            case A: break;
			                return "a"; //<-- ERROR: Unreachable code
			            default: return "?";  //<-- WARNING: Switch case may be entered by falling through previous case
			                break; //<-- ERROR: Unreachable code
			        }
			    } \s
			}""",
	},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public String f(E e) {
					              ^^^^^^
				This method must return a result of type String
				----------
				2. ERROR in X.java (at line 6)
					return "a"; //<-- ERROR: Unreachable code
					^^^^^^^^^^^
				Unreachable code
				----------
				3. ERROR in X.java (at line 8)
					break; //<-- ERROR: Unreachable code
					^^^^^^
				Unreachable code
				----------
				""",
	null,
	true,
	options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1782
// [Follow up of #1773] For classic string switch, emitted code wastes two local variable slots
public void testGHI1782() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return;

	this.runConformTest(new String[] {
		"X.java",
		"""
		public class X {
			public static void main(String[] args) {
				String hello = "Hello";
				switch (hello) {
					case "Hello" :
						String world = "world!";
						System.out.println("Hello " + world);
				}
			}
		}
		""",
	},
	"Hello world!");

	String expectedOutput =
			"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 4
		  public static void main(java.lang.String[] args);
		     0  ldc <String "Hello"> [16]
		     2  astore_1 [hello]
		     3  aload_1 [hello]
		     4  dup
		     5  astore_2
		     6  invokevirtual java.lang.String.hashCode() : int [18]
		     9  lookupswitch default: 55
		          case 69609650: 28
		    28  aload_2
		    29  ldc <String "Hello"> [16]
		    31  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [24]
		    34  ifne 40
		    37  goto 55
		    40  ldc <String "world!"> [28]
		    42  astore_3 [world]
		    43  getstatic java.lang.System.out : java.io.PrintStream [30]
		    46  aload_3 [world]
		    47  invokedynamic 0 makeConcatWithConstants(java.lang.String) : java.lang.String [36]
		    52  invokevirtual java.io.PrintStream.println(java.lang.String) : void [40]
		    55  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 3, line: 4]
		        [pc: 40, line: 6]
		        [pc: 43, line: 7]
		        [pc: 55, line: 9]
		      Local variable table:
		        [pc: 0, pc: 56] local: args index: 0 type: java.lang.String[]
		        [pc: 3, pc: 56] local: hello index: 1 type: java.lang.String
		        [pc: 43, pc: 55] local: world index: 3 type: java.lang.String
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
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2190
// [Enhanced switch] Case null disallowed when switching on arrays
public void testIssue2190() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;

	this.runConformTest(new String[] {
		"X.java",
		"""
		public class X {
			public static void main(String[] args) {
				int[] aa = {1};
				switch (aa) {
				    case null -> System.out.println("AA");
				    default -> System.out.println("BB");
				}

				int[] cc = null;
				switch (cc) {
				    case null -> System.out.println("AA");
				    default -> System.out.println("BB");
				}
			}
		}
		""",
	},
	"BB\n"
	+ "AA");
}

public static Class testClass() {
	return SwitchTest.class;
}
}

