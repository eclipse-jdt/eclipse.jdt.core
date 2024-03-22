/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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
 *
 *******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class InstanceofExpressionTest extends AbstractRegressionTest {

	public InstanceofExpressionTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return InstanceofExpressionTest.class;
	}

	static {
	//	TESTS_NAMES = new String [] { "testIssue2101" };
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=341828
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.InputStream;
					public class X {
					    void foo(InputStream is) {
					    if (is instanceof FileInputStream)
					        System.out.println("Hello");
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (is instanceof FileInputStream)
					                  ^^^^^^^^^^^^^^^
				FileInputStream cannot be resolved to a type
				----------
				"""
		);
	}

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						while (!(parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 3
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  goto 13
			     8  aload_2 [parent]
			     9  invokevirtual X$Control.getParent() : X$Control [16]
			    12  astore_2 [parent]
			    13  aload_2 [parent]
			    14  instanceof X$ExpandableComposite [22]
			    17  ifne 24
			    20  aload_2 [parent]
			    21  ifnonnull 8
			    24  aload_2 [parent]
			    25  instanceof X$ExpandableComposite [22]
			    28  ifeq 36
			    31  aload_2 [parent]
			    32  checkcast X$ExpandableComposite [22]
			    35  areturn
			    36  aconst_null
			    37  areturn
			""";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_2() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						if (!(parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 3
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  aload_2 [parent]
			     6  instanceof X$ExpandableComposite [22]
			     9  ifne 21
			    12  aload_2 [parent]
			    13  ifnull 21
			    16  aload_2 [parent]
			    17  invokevirtual X$Control.getParent() : X$Control [16]
			    20  astore_2 [parent]
			    21  aload_2 [parent]
			    22  instanceof X$ExpandableComposite [22]
			    25  ifeq 33
			    28  aload_2 [parent]
			    29  checkcast X$ExpandableComposite [22]
			    32  areturn
			    33  aconst_null
			    34  areturn
			""";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_3() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						while ((parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 3
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  goto 13
			     8  aload_2 [parent]
			     9  invokevirtual X$Control.getParent() : X$Control [16]
			    12  astore_2 [parent]
			    13  aload_2 [parent]
			    14  instanceof X$ExpandableComposite [22]
			    17  ifeq 24
			    20  aload_2 [parent]
			    21  ifnonnull 8
			    24  aload_2 [parent]
			    25  instanceof X$ExpandableComposite [22]
			    28  ifeq 36
			    31  aload_2 [parent]
			    32  checkcast X$ExpandableComposite [22]
			    35  areturn
			    36  aconst_null
			    37  areturn
			""";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_4() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						if ((parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 3
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  aload_2 [parent]
			     6  instanceof X$ExpandableComposite [22]
			     9  ifeq 21
			    12  aload_2 [parent]
			    13  ifnull 21
			    16  aload_2 [parent]
			    17  invokevirtual X$Control.getParent() : X$Control [16]
			    20  astore_2 [parent]
			    21  aload_2 [parent]
			    22  instanceof X$ExpandableComposite [22]
			    25  ifeq 33
			    28  aload_2 [parent]
			    29  checkcast X$ExpandableComposite [22]
			    32  areturn
			    33  aconst_null
			    34  areturn
			""";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_5() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent = control.getParent();
						boolean b = parent instanceof ExpandableComposite;
						if ((b) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 4
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  aload_2 [parent]
			     6  instanceof X$ExpandableComposite [22]
			     9  istore_3 [b]
			    10  iload_3 [b]
			    11  ifeq 23
			    14  aload_2 [parent]
			    15  ifnull 23
			    18  aload_2 [parent]
			    19  invokevirtual X$Control.getParent() : X$Control [16]
			    22  astore_2 [parent]
			    23  aload_2 [parent]
			    24  instanceof X$ExpandableComposite [22]
			    27  ifeq 35
			    30  aload_2 [parent]
			    31  checkcast X$ExpandableComposite [22]
			    34  areturn
			    35  aconst_null
			    36  areturn
			""";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_6() throws ClassFormatException, IOException {
     	String source =
     			"""
 				public class X {

 					class ExpandableComposite extends Control {}

 					class Control {

 						public X.Control getParent() {
 							return null;
 						}}

 					protected ExpandableComposite getParentExpandableComposite(Control control) {
 						Control parent = control.getParent();
 						boolean b = !(parent instanceof ExpandableComposite);
 						if ((b) && parent != null) {
 							parent= parent.getParent();
 						}
 						if (parent instanceof ExpandableComposite) {
 							return (ExpandableComposite) parent;
 						}
 						return null;
 					}
 					public static void main(String [] args) {
 					    System.out.println("Done!");
 					}
 				}
 				""";

     	String expectedOutput =
     			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 4
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  aload_2 [parent]
			     6  instanceof X$ExpandableComposite [22]
			     9  ifeq 16
			    12  iconst_0
			    13  goto 17
			    16  iconst_1
			    17  istore_3 [b]
			    18  iload_3 [b]
			    19  ifeq 31
			    22  aload_2 [parent]
			    23  ifnull 31
			    26  aload_2 [parent]
			    27  invokevirtual X$Control.getParent() : X$Control [16]
			    30  astore_2 [parent]
			    31  aload_2 [parent]
			    32  instanceof X$ExpandableComposite [22]
			    35  ifeq 43
			    38  aload_2 [parent]
			    39  checkcast X$ExpandableComposite [22]
			    42  areturn
			    43  aconst_null
			    44  areturn
			""";
     	runConformTest(
                 new String[] {
                         "X.java",
                         source,
                 },
                 "Done!");
     	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_7() throws ClassFormatException, IOException {
      	String source =
      			"""
  				public class X {

  					class ExpandableComposite extends Control {}

  					class Control {

  						public X.Control getParent() {
  							return null;
  						}}

  					protected ExpandableComposite getParentExpandableComposite(Control control) {
  						Control parent = control.getParent();
  						boolean b = !(parent instanceof ExpandableComposite) ? false : true;
  						if ((b) && parent != null) {
  							parent= parent.getParent();
  						}
  						if (parent instanceof ExpandableComposite) {
  							return (ExpandableComposite) parent;
  						}
  						return null;
  					}
  					public static void main(String [] args) {
  					    System.out.println("Done!");
  					}
  				}
  				""";

      	String expectedOutput =
      			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 4
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  aload_2 [parent]
			     6  instanceof X$ExpandableComposite [22]
			     9  ifne 16
			    12  iconst_0
			    13  goto 17
			    16  iconst_1
			    17  istore_3 [b]
			    18  iload_3 [b]
			    19  ifeq 31
			    22  aload_2 [parent]
			    23  ifnull 31
			    26  aload_2 [parent]
			    27  invokevirtual X$Control.getParent() : X$Control [16]
			    30  astore_2 [parent]
			    31  aload_2 [parent]
			    32  instanceof X$ExpandableComposite [22]
			    35  ifeq 43
			    38  aload_2 [parent]
			    39  checkcast X$ExpandableComposite [22]
			    42  areturn
			    43  aconst_null
			    44  areturn
			""";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_8() throws ClassFormatException, IOException {
      	String source =
      			"""
  				public class X {

  					class ExpandableComposite extends Control {}

  					class Control {

  						public X.Control getParent() {
  							return null;
  						}}

  					protected ExpandableComposite getParentExpandableComposite(Control control) {
  						Control parent = control.getParent();
  						boolean b = (parent instanceof ExpandableComposite) ? false : true;
  						if ((b) && parent != null) {
  							parent= parent.getParent();
  						}
  						if (parent instanceof ExpandableComposite) {
  							return (ExpandableComposite) parent;
  						}
  						return null;
  					}
  					public static void main(String [] args) {
  					    System.out.println("Done!");
  					}
  				}
  				""";

      	String expectedOutput =
      			"""
			  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;
			  // Stack: 1, Locals: 4
			  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);
			     0  aload_1 [control]
			     1  invokevirtual X$Control.getParent() : X$Control [16]
			     4  astore_2 [parent]
			     5  aload_2 [parent]
			     6  instanceof X$ExpandableComposite [22]
			     9  ifeq 16
			    12  iconst_0
			    13  goto 17
			    16  iconst_1
			    17  istore_3 [b]
			    18  iload_3 [b]
			    19  ifeq 31
			    22  aload_2 [parent]
			    23  ifnull 31
			    26  aload_2 [parent]
			    27  invokevirtual X$Control.getParent() : X$Control [16]
			    30  astore_2 [parent]
			    31  aload_2 [parent]
			    32  instanceof X$ExpandableComposite [22]
			    35  ifeq 43
			    38  aload_2 [parent]
			    39  checkcast X$ExpandableComposite [22]
			    42  areturn
			    43  aconst_null
			    44  areturn
			""";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_9() throws ClassFormatException, IOException {
      	String source =
      			"""
  				public class X {
					public static void main(String[] args) {
						Object o = new Object();
						if (!(o instanceof String) && false) {
							System.out.println("not string");
						}
						if ((o instanceof String) && false) {
							System.out.println("not string");
						}
						System.out.println("Done!");
					}
  				}
  				""";

      	String expectedOutput =
      			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(String[] args);
			     0  new Object [3]
			     3  dup
			     4  invokespecial Object() [8]
			     7  astore_1 [o]
			     8  aload_1 [o]
			     9  instanceof String [16]
			    12  pop
			    13  aload_1 [o]
			    14  instanceof String [16]
			    17  pop
			    18  getstatic System.out : PrintStream [18]
			    21  ldc <String "Done!"> [24]
			    23  invokevirtual PrintStream.println(String) : void [26]
			    26  return
			""";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2101
    // [Patterns] Secret local variable slots appear to be reaped later than they should be
    public void testIssue2101() throws ClassFormatException, IOException {
    	if (this.complianceLevel < ClassFileConstants.JDK16)
    		return;

      	String source =
      			"""
				public class X {

					Object type;

					public void setParent(String parent) {
						if (type != null) {
							if (type instanceof String ctype) {
								String comp = ctype;
								if (comp != null) {
									System.out.println();
								}
							}
						}

						String[] atts = null;
						for (String attribute : atts) {
							System.out.println();
						}
					}

					public static void main(String [] args) {
					    System.out.println("Done!");
					}

					X() {}

				}
  				""";

      	String expectedOutput =
      			"""
			  // Method descriptor #8 (Ljava/lang/String;)V
			  // Stack: 2, Locals: 7
			  public void setParent(String parent);
			     0  aload_0 [this]
			     1  getfield X.type : Object [10]
			     4  ifnull 38
			     7  aload_0 [this]
			     8  getfield X.type : Object [10]
			    11  dup
			    12  astore_3
			    13  instanceof String [12]
			    16  ifeq 38
			    19  aload_3
			    20  checkcast String [12]
			    23  astore_2 [ctype]
			    24  aload_2 [ctype]
			    25  astore 4 [comp]
			    27  aload 4 [comp]
			    29  ifnull 38
			    32  getstatic System.out : PrintStream [14]
			    35  invokevirtual PrintStream.println() : void [20]
			    38  aconst_null
			    39  astore_2 [atts]
			    40  aload_2 [atts]
			    41  dup
			    42  astore 6
			    44  arraylength
			    45  istore 5
			    47  iconst_0
			    48  istore 4
			    50  goto 68
			    53  aload 6
			    55  iload 4
			    57  aaload
			    58  astore_3 [attribute]
			    59  getstatic System.out : PrintStream [14]
			    62  invokevirtual PrintStream.println() : void [20]
			    65  iinc 4 1
			    68  iload 4
			    70  iload 5
			    72  if_icmplt 53
			    75  return
			      Line numbers:
			        [pc: 0, line: 6]
			        [pc: 7, line: 7]
			        [pc: 24, line: 8]
			        [pc: 27, line: 9]
			        [pc: 32, line: 10]
			        [pc: 38, line: 15]
			        [pc: 40, line: 16]
			        [pc: 59, line: 17]
			        [pc: 65, line: 16]
			        [pc: 75, line: 19]
			      Local variable table:
			        [pc: 0, pc: 76] local: this index: 0 type: X
			        [pc: 0, pc: 76] local: parent index: 1 type: String
			        [pc: 24, pc: 38] local: ctype index: 2 type: String
			        [pc: 27, pc: 38] local: comp index: 4 type: String
			        [pc: 40, pc: 76] local: atts index: 2 type: String[]
			        [pc: 59, pc: 65] local: attribute index: 3 type: String
			      Stack map table: number of frames 3
			        [pc: 38, same]
			        [pc: 53, full, stack: {}, locals: {X, String, String[], _, int, int, String[]}]
			        [pc: 68, same]
			""";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
}