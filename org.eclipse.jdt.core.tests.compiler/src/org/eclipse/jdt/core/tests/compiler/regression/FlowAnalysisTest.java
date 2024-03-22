/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *     							bug 236385 - [compiler] Warn for potential programming problem if an object is created but not used
 *      						bug 349326 - [1.7] new warning for missing try-with-resources
 *      						bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
 *								bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *								bug 391517 - java.lang.VerifyError on code that runs correctly in Eclipse 3.7 and eclipse 3.6
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FlowAnalysisTest extends AbstractRegressionTest {
static {
//	TESTS_NAMES = new String[] { "testBug380313" };
//	TESTS_NUMBERS = new int[] { 43 };
}
public FlowAnalysisTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

private boolean checkSwitchAllowedLevel() {
	return this.complianceLevel >= ClassFileConstants.JDK14;
}
public void test001() {
	this.runNegativeTest(new String[] {
		"X.java", // =================
		"""
			public class X {
				public String foo(int i) {
					if (true) {
						return null;
					}
					if (i > 0) {
						return null;
					}
				}\t
			}
			""",
	},
	"""
		----------
		1. ERROR in X.java (at line 2)
			public String foo(int i) {
			              ^^^^^^^^^^
		This method must return a result of type String
		----------
		2. WARNING in X.java (at line 6)
			if (i > 0) {
					return null;
				}
			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Dead code
		----------
		""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test() {
				        int c1, c2;
				        while ((char) (c1 = 0) == 1) {}
				        if (c1 == 0) {} // silent
				        if (c2 == 0) {} // complain
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (c2 == 0) {} // complain
				    ^^
			The local variable c2 may not have been initialized
			----------
			""",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test() {
				        int c1, c2;
				        while ((char) (c1 = 0) == 1) ;
				        if (c1 == 0) {} // silent
				        if (c2 == 0) {} // complain
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (c2 == 0) {} // complain
				    ^^
			The local variable c2 may not have been initialized
			----------
			""",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test() {
				        int c1, c2;
				        for (;(char) (c1 = 0) == 1;) ;
				        if (c1 == 0) {} // silent
				        if (c2 == 0) {} // complain
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (c2 == 0) {} // complain
				    ^^
			The local variable c2 may not have been initialized
			----------
			""",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test() {
				        int c1, c2;
				        do ; while ((char) (c1 = 0) == 1);
				        if (c1 == 0) {} // silent
				        if (c2 == 0) {} // complain
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (c2 == 0) {} // complain
				    ^^
			The local variable c2 may not have been initialized
			----------
			""",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// basic scenario
public void test006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				        case 1:
				            System.out.println(1); // complain: possible fall-through
				            break;
				        case 2:
				            System.out.println(3); // silent because of break
				            return;
				        case 3:                            // silent because of return
				        case 4:                            // silent because grouped cases
				        default:
				            System.out.println("default"); //$NON-NLS-1$
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// SuppressWarnings effect - explicit fallthrough token
public void test007() {
	if (this.complianceLevel == ClassFileConstants.JDK1_5) {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings("fallthrough")
					    public void test(int p) {
					        switch (p) {
					        case 0:
					            System.out.println(0); // silent because first case
					        case 1:
					            System.out.println(1); // silent because of SuppressWarnings
					        }
					    }
					    void foo() {
							Zork z;
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null, true, options);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (1) - fake reachable is seen as reachable
public void test008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				            if (true) {
				              return;
				            }
				        case 1:
				            System.out.println(1);
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 9)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (2)
public void test009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test(int p, boolean b) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				            if (b) {
				              return;
				            }
				            else {
				              return;
				            }
				        case 1:
				            System.out.println(1);
				        }
				    }
				}"""
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// deep return (3), limit: cannot recognize that we won't return
public void test010() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p, boolean b) {
				        switch (p) {
				        case 0:
				            System.exit(0);
				        case 1:
				            System.out.println(1);
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// SuppressWarnings effect - implicit, using all token
public void test011() {
	if (this.complianceLevel == ClassFileConstants.JDK1_5) {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings("all")
					    public void test(int p) {
					        switch (p) {
					        case 0:
					            System.out.println(0); // silent because first case
					        case 1:
					            System.out.println(1); // silent because of SuppressWarnings
					        }
					    }
						Zork z;
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null, true, options);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment
public void _test012() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				            // on purpose fall-through
				        case 1:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - default label
public void _test013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				            // on purpose fall-through
				        default:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// basic scenario: default label
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				        default:
				            System.out.println(1); // complain: possible fall-through
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				default:
				^^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// skip because of comment - variants
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				            // on purpose fall-through
				
				        case 1:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 8)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// skip because of comment - variants
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				            // on purpose fall-through
				            /* other comment */
				        case 1:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 8)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - variants
public void _test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				// on purpose fall-through
				        case 1:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127730
// [compiler] skip fall-through case warning when the fall-through is documented
// skip because of comment - variants
public void _test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				            //
				        case 1:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		"",
		null, true, null, options, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// conditioned break
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public void test(int p, boolean b) {
				        switch (p) {
				        case 0:
				            if (b) {
				              break;
				            }
				        case 1:
				            System.out.println(1); // silent because of comment alone on its line above\s
				        }
				    }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 8)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// default reporting is ignore
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0); // silent because first case
				        case 1:
				            System.out.println(1); // silent because default level is ignore
				        }
				    }
					Zork z;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// problem category
public void test021() {
	if (ProblemReporter.getProblemCategory(ProblemSeverities.Warning, IProblem.FallthroughCase) !=
			CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM) {
		fail("bad category for fall-through case problem");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128840
public void test022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if (true)
				            ;
				        else
				            ;
					}
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				;
				^
			Empty control-flow statement
			----------
			2. ERROR in X.java (at line 6)
				;
				^
			Empty control-flow statement
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						final X x;
						while (true) {
							if (true) {
								break;
							}
							x = new X();
						}
						x.foo();
					}
					public void foo() {
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				x = new X();
				^^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 10)
				x.foo();
				^
			The local variable x may not have been initialized
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132974
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo(boolean b) {
				    final Object l;
				    do {
				      if (b) {
				        l = new Object();
				        break;
				      }
				    } while (false);
				    l.toString();
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				l.toString();
				^
			The local variable l may not have been initialized
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135602
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.print("[starting]");
						X l = new X();
						l.testLoop();
						System.out.println("[finished]");
					}
				
					public void testLoop() {
						int loops = 0;
				
						do {
							System.out.print("[Loop " + loops + "]");
							if (loops > 2) {
								return;
							}
				
							if (loops < 4) {
								++loops;
								continue;\s
							}
						} while (false);
					}
				
				}
				"""
		},
		"[starting][Loop 0][finished]");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=137298
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o1) {
				    int a00, a01, a02, a03, a04, a05, a06, a07, a08, a09;
				    int a10, a11, a12, a13, a14, a15, a16, a17, a18, a19;
				    int a20, a21, a22, a23, a24, a25, a26, a27, a28, a29;
				    int a30, a31, a32, a33, a34, a35, a36, a37, a38, a39;
				    int a40, a41, a42, a43, a44, a45, a46, a47, a48, a49;
				    int a50, a51, a52, a53, a54, a55, a56, a57, a58, a59;
				    int a60, a61, a62, a63, a64, a65, a66, a67, a68, a69;
				    String s;
				    Object o2 = o1;
				    if (o2 == null) {
				      s = "";
				    }
				    System.out.println(s);
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				System.out.println(s);
				                   ^
			The local variable s may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Non-recursive approach for deep binary expressions. Check that the
// flow analysis doesn't break.
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    String s;
				    if (args.length == 0) {
				      s = "s";
				    } else {
				      s = args[0];
				    }
				    System.out.println(s + "-" + s + "-" + s + "-" +
				                       s + "-" + s + "-" + s + "-" +
				                       s + "-" + s + "-" + s + "-" +
				                       s + "-" + s + "-" + s + "-" +
				                       s + "-" + s + "-" + s + "-");
				  }
				}"""
		},
		"s-s-s-s-s-s-s-s-s-s-s-s-s-s-s-");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423
public void test028() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   {
				      if (true) throw new NullPointerException();
				   }
				}
				"""
		},
		"");
	// check no default return opcode is appended
	String expectedOutput =
		"""
		  public X();
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  new java.lang.NullPointerException [10]
		     7  dup
		     8  invokespecial java.lang.NullPointerException() [12]
		    11  athrow
		      Line numbers:
		        [pc: 0, line: 1]
		        [pc: 4, line: 3]
		      Local variable table:
		        [pc: 0, pc: 12] local: this index: 0 type: X
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test029() throws Exception {
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
				}
				""", // =================
		},
		"");
	// check no default return opcode is appended
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test030() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y {
					Y(Object o) {
						System.out.print(o);
					}
				}
				
				public class X extends Y {
					{
						if (true)
							throw new NullPointerException();
					}
				
					X() {
						super(new Object() {
							public String toString() {
								return "SUCCESS:";
							}
						});
						System.out.println();
					}
					public static void main(String[] args) {
						try {
							new X();
						} catch(NullPointerException e) {
							System.out.println("caught:NPE");
						}
					}
				}
				""", // =================
		},
		"SUCCESS:caught:NPE");
	// check no default return opcode is appended
	String expectedOutput =
		"""
		  // Method descriptor #6 ()V
		  // Stack: 3, Locals: 1
		  X();
		     0  aload_0 [this]
		     1  new X$1 [8]
		     4  dup
		     5  invokespecial X$1() [10]
		     8  invokespecial Y(java.lang.Object) [12]
		    11  new java.lang.NullPointerException [15]
		    14  dup
		    15  invokespecial java.lang.NullPointerException() [17]
		    18  athrow
		      Line numbers:
		        [pc: 0, line: 14]
		        [pc: 11, line: 10]
		      Local variable table:
		        [pc: 0, pc: 19] local: this index: 0 type: X
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y {
					Y(Object o) {
					}
				}
				
				public class X extends Y {
					final int blank;
					{
						if (true)
							throw new NullPointerException();
					}
				
					X() {
						super(new Object() {});
					}
				}
				""", // =================
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y {
					Y(int i) {
					}
				}
				
				public class X extends Y {
					final int blank;
					{
						if (true)
							throw new NullPointerException();
					}
				
					X() {
						super(blank = 0);
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				super(blank = 0);
				      ^^^^^
			Cannot refer to an instance field blank while explicitly invoking a constructor
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155423 - variation
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y {
					Y(int i) {
					}
				}
				public class X extends Y {
					final int blank;
					{
						if (true)
							throw new NullPointerException();
					}
					X() {
						super(0);
						blank = 0;
					}
				}
				""", // =================
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162918
public void test034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo1() {
				    switch (1) {
				    case 0:
				      final int i = 1;
				    case i: // should complain: i not initialized
				      System.out.println(i); // should complain: i not initialized
				    }
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				case i: // should complain: i not initialized
				     ^
			The local variable i may not have been initialized
			----------
			2. ERROR in X.java (at line 7)
				System.out.println(i); // should complain: i not initialized
				                   ^
			The local variable i may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162918
// variant
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo2() {
				    switch (1) {
				    case 0:
				      int j = 0;
				    case 1:
				      System.out.println(j); // should complain: j not initialized
				    }
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				System.out.println(j); // should complain: j not initialized
				                   ^
			The local variable j may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162918
// variant - not a flow analysis issue per se, contrast with 34 and 35 above
public void test036() {
	String src =
		"""
		public class X {
		  void foo3() {
		    switch (1) {
		    case 0:
		      class Local {
		      }
		      ;
		    case 1:
		      new Local();
		    }
		  }
		}""";
	if (this.complianceLevel <= ClassFileConstants.JDK1_3) {
		this.runConformTest(
				new String[] {
					"X.java",
					src
				},
				""
			);
	} else {
		this.runNegativeTest(
			new String[] {
				"X.java",
				src
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					new Local();
					    ^^^^^
				Local cannot be resolved to a type
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    if (false) {
				      String s;
				      System.out.println(s);
				    }
				  }
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				if (false) {
			      String s;
			      System.out.println(s);
			    }
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 5)
				System.out.println(s);
				                   ^
			The local variable s may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant: the declaration is outside of the fake reachable block
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    String s;
				    if (false) {
				      System.out.println(s);
				    }
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant with deeper nesting
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    if (false) {
				      String s;
				      if (System.out != null) {
				        System.out.println(s);
				      }
				    }
				  }
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				if (false) {
			      String s;
			      if (System.out != null) {
			        System.out.println(s);
			      }
			    }
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 6)
				System.out.println(s);
				                   ^
			The local variable s may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant - checking duplicate initialization of final variables
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    final String s = "";
				    if (false) {
				      s = "";
				    }
				  }
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				if (false) {
			      s = "";
			    }
				           ^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 5)
				s = "";
				^
			The final local variable s cannot be assigned. It must be blank and not using a compound assignment
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant - checking duplicate initialization of final variables
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    final String s;
				    s = "";
				    if (false) {
				      s = "";
				    }
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166641
// variant - checking duplicate initialization of final variables
public void test042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    final String s;
				    if (false) {
				      s = "";
				    }
				    s = "";
				  }
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				if (false) {
			      s = "";
			    }
				           ^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 7)
				s = "";
				^
			The final local variable s may already have been assigned
			----------
			""");
}
// switch and definite assignment
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public abstract class X {
				  public static void main(String[] args) {
				    for (int i = 0; i < 3; i++) {
				      System.out.print(i);
				      switch (i) {
				        case 1:
				          final int j;
				          j = 1;
				          System.out.println(j);
				          break;
				        case 2:
				          j = 2;
				          System.out.println(j);
				      }
				    }
				  }
				}
				""",
		},
		"011\n22");
}
// switch and definite assignment
public void test044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X {
				  public static void main(String[] args) {
				    for (int i = 0; i < 3; i++) {
				      System.out.print(i);
				      switch (i) {
				        case 1:
				          final int j = 1;
				          System.out.println(j);
				          break;
				        case 2:
				          j = 2;
				          System.out.println(j);
				      }
				    }
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				j = 2;
				^
			The final local variable j cannot be assigned. It must be blank and not using a compound assignment
			----------
			""");
}
// switch and definite assignment
// **
public void test045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X {
				  public static void main(String[] args) {
				    switch (args.length) {
				      case 1:
				        final int j = 1;
				      case 2:
				        switch (5) {
				          case j:
				        }
				    }
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				case j:
				     ^
			The local variable j may not have been initialized
			----------
			""");
}
// for and definite assignment
public void test046() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public abstract class X {
				  public static void main(String args[]) {
				    for (final int i; 0 < (i = 1); i = i + 1) {
				      System.out.println(i);
				      break;
				    }
				  }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				for (final int i; 0 < (i = 1); i = i + 1) {
				                               ^^^^^^^^^
			Dead code
			----------
			""",
		"1",
		"",
		JavacTestOptions.JavacHasABug.JavacBug4660984);
}
// do while and named labels
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() {
				    done: do
				      break done;
				    while (false);
				    System.out.println();
				  }
				}
				""",
		},
		"");
}
// labeled loop
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200158
// contrast this with test049
public void test048() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  private static final boolean b = false;
				  public Object foo() {
				    if (b) {
				      label: while (bar()) {
				      }
				      return null;
				    }
				    return null;
				  }
				  boolean bar() {
				    return false;
				  }
				}
				"""
			},
		false /* expectingCompilerErrors */,
		"""
			----------
			1. WARNING in X.java (at line 5)
				label: while (bar()) {
				^^^^^
			The label label is never explicitly referenced
			----------
			""",
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// labeled loop
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200158
// variant: this one passes
public void test049() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  private static final boolean b = false;
				  public Object foo() {
				    if (b) {
				      while (bar()) {
				      }
				      return null;
				    }
				    return null;
				  }
				  boolean bar() {
				    return false;
				  }
				}
				"""
			},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235781
public void test050_definite_assigment_and_if_true() {
	runConformTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  final int i;
				  X() {
				    if (true) {
				      throw new NullPointerException();
				    }
				  }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235781
// variant
public void test051_definite_assigment_and_if_true() {
	runConformTest(
		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				  X() {
				    final int i;
				    if (true) {
				      throw new NullPointerException();
				    }
				    System.out.println(i);
				  }
				}
				"""
		}
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399
public void test052() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo(boolean b) {
						if (b && false) {
							int i = 0; // deadcode
							return;  // 1
						}
						return;
						return;
					}
				}\t
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				if (b && false) {
						int i = 0; // deadcode
						return;  // 1
					}
				                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 8)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test053() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo(boolean b) {
						if (false && b) {
							int j = 0; // deadcode
							return; // 2
						}
						return;
						return;
					}
				}\t
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				if (false && b) {
				             ^
			Dead code
			----------
			2. WARNING in X.java (at line 3)
				if (false && b) {
						int j = 0; // deadcode
						return; // 2
					}
				                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 8)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test054() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo(boolean b) {
						while (true) {
							if (true) break;
							int k = 0; // deadcode
						}
						return;
						return;
					}
				}\t
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				int k = 0; // deadcode
				^^^^^^^^^^
			Dead code
			----------
			2. ERROR in X.java (at line 8)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test055() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo(boolean b) {
						if (true || b) {
							int l = 0; // deadcode
							return; // 2a
						}	\t
						return;
						return;
					}
				}\t
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				if (true || b) {
				            ^
			Dead code
			----------
			2. WARNING in X.java (at line 7)
				return;
				^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 8)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test056() {
	if (this.complianceLevel < ClassFileConstants.JDK1_4) {
		runNegativeTest(
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
						void bar() {
							return;
							{
								return; // 3
							}
						}
						void baz() {
							return;
							{
							}
						}\t
						void baz2() {
							return;
							; // 4
						}\t
					}\t
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					{
							return; // 3
						}
					^^^^^^^^^^^^^^^^^^^^^
				Unreachable code
				----------
				2. ERROR in X.java (at line 10)
					{
						}
					^^^^^
				Unreachable code
				----------
				""");
		return;
	}
	runNegativeTest(
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
						void bar() {
							return;
							{
								return; // 3
							}
						}
						void baz() {
							return;
							{
							}
						}\t
						void baz2() {
							return;
							; // 4
						}\t
					}\t
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					{
							return; // 3
						}
					^^^^^^^^^^^^^^^^^^^^^
				Unreachable code
				----------
				2. ERROR in X.java (at line 10)
					{
						}
					^^^^^
				Unreachable code
				----------
				3. ERROR in X.java (at line 15)
					; // 4
					^
				Unreachable code
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110544
public void test057() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo(int x, int[] array) {
						for (int i = 0;\s
						     i < array.length;\s
						     i++) {//dead code
							if (x == array[i])
								return;
							else
								break;
						}
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				i++) {//dead code
				^^^
			Dead code
			----------
			2. WARNING in X.java (at line 9)
				break;
				^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test058() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo() {
						if (false) {
							class Local {
								int i = 12;
								{   i++; }
								void method() {
									if (false)
										System.out.println();
									return;
									return;
								}
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				if (false) {
						class Local {
							int i = 12;
							{   i++; }
							void method() {
								if (false)
									System.out.println();
								return;
								return;
							}
						}
					}
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			2. WARNING in X.java (at line 4)
				class Local {
				      ^^^^^
			The type Local is never used locally
			----------
			3. WARNING in X.java (at line 7)
				void method() {
				     ^^^^^^^^
			The method method() from the type Local is never used locally
			----------
			4. ERROR in X.java (at line 11)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test059() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo(boolean b) {
						int i = false && b ? 0 : 1;
						if (false) {
							int j = false && b ? 0 : 1;
						}
						return;
						return;
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				int i = false && b ? 0 : 1;
				                 ^
			Dead code
			----------
			2. WARNING in X.java (at line 3)
				int i = false && b ? 0 : 1;
				                     ^
			Dead code
			----------
			3. WARNING in X.java (at line 4)
				if (false) {
						int j = false && b ? 0 : 1;
					}
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			4. ERROR in X.java (at line 8)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=48399 - variation
public void test060() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					static final boolean DEBUG = false;
					static final int DEBUG_LEVEL = 0;
					boolean check() { return true; }
					void foo(boolean b) {
						if (DEBUG)
							System.out.println("fake reachable1"); //$NON-NLS-1$
						if (DEBUG && b)
							System.out.println("fake reachable2"); //$NON-NLS-1$
						if (DEBUG && check())
							System.out.println("fake reachable3"); //$NON-NLS-1$
						if (b && DEBUG)
							System.out.println("fake reachable4"); //$NON-NLS-1$
						if (check() && DEBUG)
							System.out.println("fake reachable5"); //$NON-NLS-1$
						if (DEBUG_LEVEL > 1)\s
							System.out.println("fake reachable6"); //$NON-NLS-1$
						return;
						return;
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				if (DEBUG && b)
				             ^
			Dead code
			----------
			2. WARNING in X.java (at line 9)
				System.out.println("fake reachable2"); //$NON-NLS-1$
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. WARNING in X.java (at line 10)
				if (DEBUG && check())
				             ^^^^^^^
			Dead code
			----------
			4. WARNING in X.java (at line 11)
				System.out.println("fake reachable3"); //$NON-NLS-1$
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			5. WARNING in X.java (at line 13)
				System.out.println("fake reachable4"); //$NON-NLS-1$
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			6. WARNING in X.java (at line 15)
				System.out.println("fake reachable5"); //$NON-NLS-1$
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			7. WARNING in X.java (at line 17)
				System.out.println("fake reachable6"); //$NON-NLS-1$
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			8. ERROR in X.java (at line 19)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265962
public void test061() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        private static final boolean isIS() {
				                return System.currentTimeMillis()<0 ;
				        }
				        public static void main(String[] args) {
				                do {
				                        return;
				                } while(isIS() && false);
				        }
				}
				""", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"""
		  public static void main(java.lang.String[] args);
		    0  return
		      Line numbers:
		        [pc: 0, line: 7]
		      Local variable table:
		        [pc: 0, pc: 1] local: args index: 0 type: java.lang.String[]
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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265962 - variation
public void test062() {
	runNegativeTest(
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				        private static final boolean isIS() {
				                return System.currentTimeMillis()<0 ;
				        }
				        public static void main(String[] args) {
				                do {
				                        return;
				                } while(isIS() && false);
				                return;
				        }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				return;
				^^^^^^^
			Unreachable code
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
public void test063() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }\s
					public void foo() {\
						if (bar())
							new IllegalArgumentException("You must not bar!");
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				new IllegalArgumentException("You must not bar!");
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The allocated object is never used
			----------
			""",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// non-throwable type
public void test064() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }\s
					public void foo() {\
						if (bar())
							new String("You must not bar!");
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				new String("You must not bar!");
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The allocated object is never used
			----------
			""",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// warning suppressed
public void test065() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }
				   @SuppressWarnings("unused")
					public void foo() {\
						if (bar())
							new IllegalArgumentException("You must not bar!");
					}
				}""",
		},
		"" /* expectedOutputString */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		compilerOptions /* customOptions */,
		null /* clientRequestor */);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// warning ignored (default)
public void test066() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }
					public void foo() {\
						if (bar())
							new IllegalArgumentException("You must not bar!");
					}
				}""",
		},
		"" /* expectedOutputString */);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// instance is assigned
public void test067() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }
				   Throwable t;
					public void foo() {\
						t = new IllegalArgumentException("You must not bar!");
					}
				}""",
		},
		"" /* expectedOutputString */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		compilerOptions /* customOptions */,
		null /* clientRequestor */);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
// method invoked
public void test068() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }
					public void foo() {\
						if (bar())
							new IllegalArgumentException("You must not bar!").printStackTrace();
					}
				}""",
		},
		"" /* expectedOutputString */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		compilerOptions /* customOptions */,
		null /* clientRequestor */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236385
//anonymous type
public void test069() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   boolean bar() { return false; }\s
					public void foo() {\
						if (bar())
							new Object() {};
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				new Object() {};
				^^^^^^^^^^^^^^^
			The allocated object is never used
			----------
			""",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322154
public void test070() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				    private X (){
				        boolean flagSet = true;
				        Object first = true ? null : "";       \s
				        Object second = flagSet || first == null ? null :
				            new Object() {};
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				Object first = true ? null : "";       \s
				                             ^^
			Dead code
			----------
			""",
		null /* classLibraries */,
		true /* shouldFlushOutputDirectory */,
		compilerOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324154
public void test071() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				public class X {
				  static {
				    try {
				      while(true) {
				          if (true)
				              throw new NumberFormatException();
				          else
				              throw new IOException();
				      }
				    } catch(IOException e ) {
				        // empty
				    }\s
				  }\s
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				throw new IOException();
				^^^^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			2. WARNING in X.java (at line 9)
				throw new IOException();
				^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Warn uninitialized variable in deadcode if deadcode has been inferred
// by null analysis
public void testBug338234a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        int i;
				        String str = null;
				        if (str != null)
				            i++;   \s
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				i++;   \s
				^^^
			Dead code
			----------
			2. ERROR in X.java (at line 6)
				i++;   \s
				^
			The local variable i may not have been initialized
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Don't warn uninitialized variable in deadcode if deadcode has not been inferred
// by null analysis
public void testBug338234b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        int i;
				        l: {
							if(false)
								break l;
				        	return;
						 }
				        i++;   \s
				    }
				}
				"""
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Warn uninitialized field in deadcode if deadcode has been inferred
// by null analysis
public void testBug338234c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public final int field1;
				    {
				        int i;
				        String str = null;
						 if(str != null)
							i = field1;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public final int field1;
				                 ^^^^^^
			The blank final field field1 may not have been initialized
			----------
			2. WARNING in X.java (at line 7)
				i = field1;
				^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 7)
				i = field1;
				    ^^^^^^
			The blank final field field1 may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
// Warn uninitialized field in deadcode if deadcode has been inferred
// by null analysis
public void testBug338234d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(boolean b) {
				        int i;
						 String str = null;
				        if(b){
						 	if(str == null)
								return;
						 } else {
							i = 2;
						 }
						 i++;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				i++;
				^
			The local variable i may not have been initialized
			----------
			""");
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// variant < 1.7 using Closeable: not closed
public void testCloseable1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.File;
					import java.io.FileReader;
					import java.io.IOException;
					public class X {
					    void foo() throws IOException {
					        File file = new File("somefile");
					        FileReader fileReader = new FileReader(file); // not closed
					        char[] in = new char[50];
					        fileReader.read(in);
					    }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 7)
					FileReader fileReader = new FileReader(file); // not closed
					           ^^^^^^^^^^
				Resource leak: 'fileReader' is never closed
				----------
				""",
			null, true, options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// variant < 1.7 using Closeable: resource is closed, cannot suggest try-with-resources < 1.7
public void testCloseable2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.File;
					import java.io.FileReader;
					import java.io.IOException;
					public class X {
					    void foo() throws IOException {
					        File file = new File("somefile");
					        FileReader fileReader = new FileReader(file); // not closed
					        char[] in = new char[50];
					        fileReader.read(in);
					        fileReader.close();
					    }
					}
					"""
			},
			"",
			null, true, null, options, null);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// return/break/continue inside anonymous class inside try-catch inside initializer
public void testLocalClassInInitializer1() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    static {
					        final int i=4;
					        try {
					            Runnable runner = new Runnable() {
					                public void run() {
					                    switch (i) {\
					                        case 4: break;
					                    }
					                    int j = i;
					                    while (j++ < 10) {
					                        if (j == 2) continue;
					                        if (j == 4) break;
					                        if (j == 6) return;
					                    }
					                }
					            };
					        } catch (RuntimeException re) {}
					    }
					}
					"""
			},
			"");
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// break/continue illegally inside anonymous class inside loop (loop is out of scope for break/continue)
public void testLocalClassInInitializer2() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    void f () {
					        while (true) {
					            class Inner1 {
					                { if (true) break; }
					            }
					            new Inner1();
					        }
					    }\s
					    void g () {
					        outer: for (int i=1;true;i++) {
					            class Inner2 {
					                int j = 3;
					                void foo () {
					                  if (2 == j) continue outer;
					                  else continue;
					                }
					            }
					            new Inner2().foo();
					        }
					    }\s
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					{ if (true) break; }
					            ^^^^^^
				break cannot be used outside of a loop or a switch
				----------
				2. WARNING in X.java (at line 11)
					outer: for (int i=1;true;i++) {
					^^^^^
				The label outer is never explicitly referenced
				----------
				3. ERROR in X.java (at line 15)
					if (2 == j) continue outer;
					            ^^^^^^^^^^^^^^^
				The label outer is missing
				----------
				4. ERROR in X.java (at line 16)
					else continue;
					     ^^^^^^^^^
				continue cannot be used outside of a loop
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
// Verify that the code runs fine with all compliance levels.
public void testBug380313() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					public void foo() throws Exception {
					        int i = 1;
					        int j = 2;
					        try {
					            if ((bar() == 1)) {
					                if ((i == 1)) {
					                    int n = bar();
					                    if (n == 35) {
					                        j = 2;
					                    } else {
					                        if (bar() > 0)
					                            return;
					                    }
					                } else {
					                    throw new Exception();
					                }
					            } else {
					                throw new Exception();
					            }
					            if (bar() == 0)
					                return;
					        } finally {
					            bar();
					        }
					    }
					
					    private int bar() {
					        return 0;
					    }
					
					    public static void main(String[] args) {
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
// try with resources
// Verify that the code runs fine with all compliance levels.
public void testBug380313b() {
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
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380750
// verify that s0 is not reported as uninitialized
public void testBug380750() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(String[] args) {
							String s0;
							for(String s : singleton(s0="")) {
								System.out.println(s);
							}
							System.out.println(s0);
						}
						String[] singleton(String s) {
							return new String[] {s};
						}
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391517
// java.lang.VerifyError on code that runs correctly in Eclipse 3.7 and eclipse 3.6
public void testBug391517() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.PrintWriter;
					
					public class X {
					
						private static final int CONSTANT = 0;
					
						public static void main(String[] args) {
							// TODO Auto-generated method stub
					
						}
					
						static void addStackTrace(String prefix) {
							if (CONSTANT == 0) {
								return;
							}
							PrintWriter pw = null;
							new Exception().printStackTrace(pw);
							if (bar() == null) {
								System.out.println();
							}
						}
					
						static Object bar() {
							return null;
						}
					}"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=415997
// Bug 415997 - java.lang.VerifyError: Expecting a stackmap frame at branch target
public void testBug415997a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Object o = null;
						if (o == null)
							if (true)
								return;
					}
				}"""
		},
		"");
}
public void testBug415997b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Object o = null;
						if (o == null) {}
						else
							if (true)
								return;
					}
				}"""
		},
		"");
}
public void testBug415997c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) throws Exception {
						System.out.println(ParseExpr11());
					}
					static final public Object ParseExpr11() throws Exception {
						Object expr;
						Object op = null;
						expr = ParseVarExpr();
						if (op == null) {
							if (true)
								return expr;
						}
						{
							throw new Exception("++/-- not supported in TUD Bantam Java.");
						}
					}
					private static Object ParseVarExpr() {
						// TODO Auto-generated method stub
						return "test";
					}
				}"""
		},
		"test");
}
public void testBug499809() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {
					static void foo( ) {
						String _a, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z, a, b,
						c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, s0, s1, s2, s3, s4, s5, s6, s7;
						Object ob = new Object();
						int int1 = 0, int2 = 2, int3, int4;
						if (ob != null) {
							int4 = 1;
						}
					}
					public static void main(String[] args) {
						System.out.println("Done");
					}
				}
				"""
		},
		"Done");
}
public void testBug499809a() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {
					static void foo( ) {
						String _a, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z, a, b,
						c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, s0, s1, s2, s3, s4, s5, s6, s7;
						Object ob = new Object();
						int int1 = 0, int2 = 2, int3, int4;
						if (ob == null) {
							int1 = 1;
						} else {
							int4 = 1;
						}
					}
					public static void main(String[] args) {
						System.out.println("Done");
					}
				}
				"""
		},
		"Done");
}
//Bug 506315 - ASTParser.createASTs() in StackMapFrame.addStackItem throws IllegalArgumentException
public void testBug506315() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.Consumer;
				public class Test {
				    public void test(String method) {
				        String str;
				        if (!method.equals("")) {
				        	str = "String";
				        	str.concat(method);
				        }
				        new Consumer<String>() {
				            public void accept(String s) {
				            	str = "String";
				            }
				        };
				    }
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in Test.java (at line 11)
					str = "String";
					^^^
				Cannot refer to the non-final local variable str defined in an enclosing scope
				----------
				"""
				:
			"""
				----------
				1. ERROR in Test.java (at line 11)
					str = "String";
					^^^
				Local variable str defined in an enclosing scope must be final or effectively final
				----------
				""");
}
public void _testBug533435() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public interface X {}\n"
		}, new ASTVisitor() {
            public boolean visit(TypeDeclaration typeDeclaration,
                                 CompilationUnitScope scope) {
                if (new String(typeDeclaration.name).equals("X")) {
                    typeDeclaration.methods =
                            new AbstractMethodDeclaration[0];
                    typeDeclaration.fields = new FieldDeclaration[0];
                    scope.referenceContext.analyseCode();
                    //should not fail
                }
                return true;
            }
        });
}
public void testBug537804_comment0() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses an annotation
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test
				{
					private boolean dummy;
				
				//Test
					void testMethod()
					{
						@SuppressWarnings("unused")
						boolean action;
				
						OUTER:
						{
							while (true)
							{
								if (dummy)
									break OUTER;
				
								action = true;
								break;
							}
				
							return;
						}
				
						return;
					}
				
				//Main Method
					public static void main(String[] arguments)
					{
						//No operation
					}
				}
				"""
		});
}
public void testBug537804_comment5() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test
				{
					private boolean dummy;
				
				//Test
					void testMethod()
					{
						boolean action;
				
						OUTER:
						{
							while (true)
							{
								if (dummy)
									break OUTER;
				
								action = true;
								break;
							}
				
							if (action) //Okay.
								noOp();
				
							return;
						}
				
						if (action) //Missing error: 'action' may not be initialized!
							noOp();
				
						return;
					}
					void noOp()
					{
						//No operation
					}
				
				//Main Method
					public static void main(String[] arguments)
					{
						//No operation
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 27)
				if (action) //Missing error: \'action\' may not be initialized!
				    ^^^^^^
			The local variable action may not have been initialized
			----------
			""");
}
public void testBug548318_001() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 11)
			yield k;
			      ^
		The local variable k may not have been initialized
		----------
		2. ERROR in X.java (at line 14)
			return k + it;
			       ^
		The local variable k may not have been initialized
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						final int k;
				
						int it = switch (i) {\s
						case 1  ->   {
							k = 1;
							yield k ;
						}
						default -> {
							yield k;
						}
						};
						return k + it;
					}
				
					public boolean bar() {
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
		};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);

}
public void testBug548318_002() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 5)
			k = switch (i) {\s
			^
		The final local variable k may already have been assigned
		----------
		2. ERROR in X.java (at line 11)
			yield k;
			      ^
		The local variable k may not have been initialized
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						final int k;
				
						k = switch (i) {\s
						case 1  ->   {
							k = 1;
							yield k ;
						}
						default -> {
							yield k;
						}
						};
						return k;
					}
				
					public boolean bar() {
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
		};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/*
 * k is definitely assigned - no errors on that front.
 */
public void testBug548318_003() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 23)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						final int k;
				
						int it = switch (i) {\s
						case 1  ->   {
							k = 1;
							yield k ;
						}
						case 2  ->   {
							k = 2;
							yield k ;
						}
						default -> {
							k = 3;
							yield k;
						}
						};
						return k;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
public void testBug548318_004() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 7)
			k = 1;
			^
		The final local variable k cannot be assigned. It must be blank and not using a compound assignment
		----------
		2. ERROR in X.java (at line 11)
			k = 2;
			^
		The final local variable k cannot be assigned. It must be blank and not using a compound assignment
		----------
		3. ERROR in X.java (at line 15)
			k = 3;
			^
		The final local variable k cannot be assigned. It must be blank and not using a compound assignment
		----------
		4. ERROR in X.java (at line 23)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						final int k = 1;
				
						int it = switch (i) {\s
						case 1  ->   {
							k = 1;
							yield k ;
						}
						case 2  ->   {
							k = 2;
							yield k ;
						}
						default -> {
							k = 3;
							yield k;
						}
						};
						return k;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
public void testBug548318_005() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 11)
			yield k ;
			      ^
		The local variable k may not have been initialized
		----------
		2. ERROR in X.java (at line 18)
			return k;
			       ^
		The local variable k may not have been initialized
		----------
		3. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						final int k;
				
						int it = switch (i) {\s
						case 1  ->   {
							k = 1;
							yield k ;
						}
						case 2  ->   {
							yield k ;
						}
						default -> {
							k = 3;
							yield k;
						}
						};
						return k;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is definitely assigned after a switch expression when true iff for every value yield statement with
 * expression e in the switch block that may exit the switch expression, V is definitely assigned after e when true.
 * V is definitely assigned after a switch expression when false iff for every value yield statement with
 * expression e in the switch block that may exit the switch expression, V is definitely assigned after e when false.
 */
public void testBug548318_006() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i) {
						case 1 :
							v = 1;
							yield true;
						case 2 : {
							v = 2;
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is definitely unassigned after a switch expression when true iff for every value yield statement with expression
 * e in the switch block that may exit the switch expression, V is definitely unassigned before the value yield
 * statement and V is definitely unassigned after e when true.
 * V is definitely unassigned after a switch expression when false iff for every value yield statement with expression
 * e in the switch block that may exit the switch expression, V is definitely unassigned before the value yield
 * statement and V is definitely unassigned after e when false.
 */
public void testBug548318_007() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 18)
			return v + d;
			       ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i) {
						case 1 :
							//v = 1;
							yield true;
						case 2 : {
							//v = 2;
							yield true;
						}
						default : {
							//v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the selector expression iff V is [un]assigned before the switch statement.
 */
public void testBug548318_008() {
	if (!checkSwitchAllowedLevel())
		return;
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v = 1;
						boolean b = switch (i) {
						case 1 :
							//v = 1;
							yield true;
						case 2 : {
							//v = 2;
							yield true;
						}
						default : {
							//v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the selector expression iff V is [un]assigned before the switch statement.
 */
public void testBug548318_009() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 4)
			boolean b = switch (v) {
			                    ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (v) {
						case 1 :
							v = 1;
							yield true;
						case 2 : {
							v = 2;
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the first statement of the first switch labeled statement group in the switch block
 * iff V is [un]assigned after the selector expression.
 */
public void testBug548318_010() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i + (v =1)) {
						case 1 :
							v += 1;
							yield true;
						case 2 : {
							v = 2;
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the first statement of the first switch labeled statement group in the switch block
 * iff V is [un]assigned after the selector expression.
 */
public void testBug548318_011() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 6)
			v += 1;
			^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i) {
						case 1 :
							v += 1;
							yield true;
						case 2 : {
							v = 2;
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the first statement of any switch labeled statement group other than the first iff
 * V is [un]assigned after the selector expression and V is [un]assigned after the preceding statement.
 * and V is [un]assigned after the preceding statement
 */
public void testBug548318_012() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i + (v =1)) {
						case 1 :
							v = 1;
							yield true;
						case 2 : {
							v += 2;
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the first statement of any switch labeled statement group other than the first iff
 * V is [un]assigned after the selector expression and V is [un]assigned after the preceding statement.
 * and V is [un]assigned after the preceding statement"
 */
public void testBug548318_012b() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 15)
			return v + d;
			       ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i) {
						case 1 :i =1;
						case 2 : {
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before the first statement of any switch labeled statement group other than the first iff
 * V is [un]assigned after the selector expression and V is [un]assigned after the preceding statement.
 * and V is [un]assigned after the preceding statement" needs to be checked
 */
public void testBug548318_013() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 9)
			v += 2;
			^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 22)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						boolean b = switch (i) {
						case 1 :
							v = 1;
							yield true;
						case 2 : {
							v += 2;
							yield true;
						}
						default : {
							v = 3;
							yield false;
						}
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * The following rules apply only if the switch block of a switch expression consists of switch labeled rules:
 * V is definitely assigned after a switch expression when true iff for every switch labeled rule one of the following is true:
 * 		It is a switch labeled expression e and V is definitely assigned after e when true.
 * 		It is a switch labeled block b and for every value yield statement expression e contained in b that may exit the switch expression,
 * 			V is definitely assigned after e when true.
 * 		It is a switch labeled throw statement.
 *
 * V is definitely assigned after a switch expression when false iff for every switch labeled rule one of the following is true:
 * 		It is a switch labeled expression e and V is definitely assigned after e when false.
 * 		It is a switch labeled block b and for every value yield statement expression e contained in b that may exit the switch expression,
 * 		V is definitely assigned after e when false.
 * 		It is a switch labeled throw statement.
 */
public void testBug548318_014() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 23)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				public class X {
					public static int foo(int i) throws IOException {
						int v;
						boolean b = switch (i ) {
						case 0 -> (v = 1) != 0;
						case 1 -> (v = 1) == 0;
						case 2 -> {
							v = 2;
							yield true;
						}
						case 3 -> {
							v = 3;
							yield false;
						}
						default -> throw new IOException();
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * The following rules apply only if the switch block of a switch expression consists of switch labeled rules:
 * V is definitely unassigned after a switch expression when true iff for every switch labeled rule one of the following is true:
 * 		It is a switch labeled expression e and V is definitely unassigned after e when true .
 * 		It is a switch labeled block b and for every value yield statement expression e contained in b that
 * 		 may exit the switch expression, V is definitely unassigned before the value yield statement and
 * 		     V is definitely unassigned after e when true.
 * 		It is a switch labeled throw statement.
 *
 * V is definitely unassigned after a switch expression when false iff for every switch labeled rule one of the following is true:
 * 		It is a switch labeled expression e and V is definitely unassigned after e when false.
 * 		It is a switch labeled block b and for every value yield statement expression e contained in b that may
 * 		exit the switch expression,	V is definitely unassigned before the value yield statement and V is definitely unassigned
 * 			after e when false.
 * 		It is a switch labeled throw statement.
 */
public void testBug548318_015() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 17)
			return v + d;
			       ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 21)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				public class X {
					public static int foo(int i) throws IOException {
						int v;
						boolean b = switch (i ) {
						case 0 ->  true;
						case 1 -> false;
						case 2 -> {
							yield true;
						}
						case 3 -> {
							yield false;
						}
						default -> throw new IOException();
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * V is [un]assigned before any switch labeled expression or statement in the switch
 * block iff V is [un]assigned after the selector expression.
 */
public void testBug548318_016() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 14)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				public class X {
					public static int foo(int i) throws IOException {
						int v;
						boolean b = switch ((v = 1)) {
						case 0 ->  v != 0;
						default -> throw new IOException();
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.7 , Suppose that the switch expression has result expressions e1, â€¦, en, all of
 * which are boolean-valued.
 * The following rules apply only if the switch block of a switch expression consists of switch labeled rules:
 * V is [un]assigned before any switch labeled expression or statement in the switch
 * block iff V is [un]assigned after the selector expression.
 */
public void testBug548318_017() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 6)
			case 0 ->  v != 0;
			           ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 10)
			return v + d;
			       ^
		The local variable v may not have been initialized
		----------
		3. ERROR in X.java (at line 14)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				public class X {
					public static int foo(int i) throws IOException {
						int v;
						boolean b = switch (i) {
						case 0 ->  v != 0;
						default -> throw new IOException();
						};
						int d = b == true ? 0 : 1;\s
						return v + d;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, V is [un]assigned after a switch expression (15.28) iff all of the following are true:
 * 	V is [un]assigned before every yield statement that may exit the switch statement.
 * 	For each switch labeled rule (14.11.1) in the switch block, V is [un]assigned after the
 *          expression, block, or throw statement of the switch labeled rule.
 */
public void testBug548318_018() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 20)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						int t = switch (i) {
						case 0 : {
							v = 1; // definitely assigned before yield
							yield v;
						}
						case 2 : {
							yield v =1; // definitely assigned after e
						}
						default : {
							yield v = 2;
						}
						};
						return v + t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, V is [un]assigned after a switch expression (15.28) iff all of the following are true:
 * 	V is [un]assigned before every yield statement that may exit the switch statement.
 * 	For each switch labeled rule (14.11.1) in the switch block, V is [un]assigned after the
 *          expression, block, or throw statement of the switch labeled rule.
 */
public void testBug548318_019() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 15)
			return v + t;
			       ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						int t = switch (i) {
						case 0 : {
							yield 1;
						}
						case 2 : {
							yield 2;
						}
						default : {
							yield 3;
						}
						};
						return v + t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * V is [un]assigned before the selector expression iff V is [un]assigned before the switch statement.
 */
public void testBug548318_020() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v =1;
						int t = switch (v) {
						case 0 : {
							yield 1;
						}
						case 2 : {
							yield 2;
						}
						default : {
							yield 3;
						}
						};
						return t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * V is [un]assigned before the selector expression iff V is [un]assigned before the switch statement.
 */
public void testBug548318_021() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 4)
			int t = switch (v) {
			                ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						int t = switch (v) {
						case 0 : {
							yield 1;
						}
						case 2 : {
							yield 2;
						}
						default : {
							yield 3;
						}
						};
						return t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * 	V is [un]assigned before the first block statement of a switch labeled statement group (14.11.1) of a switch expression
 * iff both of following are true:
 * V is [un]assigned after the selector expression of the switch statement.
 * If the switch labeled statement group is not the first in the switch block,
 * V is [un]assigned after the last block statement of the preceding switch labeled statement group.
 */
public void testBug548318_022() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v =1;
						int t = switch (v) {
						case 0 : {
							yield v;
						}
						case 2 : {
							yield 2;
						}
						default : {
							yield 3;
						}
						};
						return t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * 	V is [un]assigned before the first block statement of a switch labeled statement group (14.11.1) of a switch expression
 * iff both of following are true:
 * V is [un]assigned after the selector expression of the switch statement.
 * If the switch labeled statement group is not the first in the switch block,
 * V is [un]assigned after the last block statement of the preceding switch labeled statement group.
 */
public void testBug548318_023() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 6)
			yield v;
			      ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v;
						int t = switch (i) {
						case 0 : {
							yield v;
						}
						case 2 : {
							yield 2;
						}
						default : {
							yield 3;
						}
						};
						return t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * 	V is [un]assigned before the first block statement of a switch labeled statement group (14.11.1) of a switch expression
 * iff both of following are true:
 * V is [un]assigned after the selector expression of the switch statement.
 * If the switch labeled statement group is not the first in the switch block,
 * V is [un]assigned after the last block statement of the preceding switch labeled statement group.
 */
public void testBug548318_024() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 9)
			yield v;
			      ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 19)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				public class X {
					public static int foo(int i) {
						int v ;
						int t = switch (i) {
						case 0 : {
							yield 1;
						}
						case 2 : {
							yield v;
						}
						default : {
							yield 3;
						}
						};
						return t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						System.out.println(foo(3));
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, V is [un]assigned after a switch expression (15.28) iff all of the following are true:
 * 	V is [un]assigned before every yield statement that may exit the switch statement.
 * 	For each switch labeled rule (14.11.1) in the switch block, V is [un]assigned after the
 *          expression, block, or throw statement of the switch labeled rule.
 */
public void testBug548318_025() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 20)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				
				public class X {
					public static int foo(int i) throws IOException {
						int v ;
						int t = switch (i) {
						case 0 -> v = 1;
						case 2 -> {
							if (i > 1) {
								yield v = 2;
							}
							yield v = 3;
						}
						default -> throw new IOException();
						};
						return v + t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, V is [un]assigned after a switch expression (15.28) iff all of the following are true:
 * 	V is [un]assigned before every yield statement that may exit the switch statement.
 * 	For each switch labeled rule (14.11.1) in the switch block, V is [un]assigned after the
 *          expression, block, or throw statement of the switch labeled rule.
 */
public void testBug548318_026() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 16)
			return v + t;
			       ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 20)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				
				public class X {
					public static int foo(int i) throws IOException {
						int v ;
						int t = switch (i) {
						case 0 ->  1;
						case 2 -> {
							if (i > 1) {
								yield  2;
							}
							yield 3;
						}
						default -> throw new IOException();
						};
						return v + t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * V is [un]assigned before the expression, block, or throw statement of a switch labeled rule of a
 * switch expression iff V is [un]assigned after the selector expression of the switch expression.
 */
public void testBug548318_027() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 20)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				
				public class X {
					public static int foo(int i) throws IOException {
						int v ;
						int t = switch (v = 1) {
						case 0 ->  v;
						case 2 -> {
							if (i > 1) {
								yield  2;
							}
							yield 3;
						}
						default -> throw new IOException();
						};
						return v + t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.1.8, Suppose that the switch expression has result expressions e1, â€¦, en, not all of
 * which are boolean-valued.
 * V is [un]assigned before the expression, block, or throw statement of a switch labeled rule of a
 * switch expression iff V is [un]assigned after the selector expression of the switch expression.
 */
public void testBug548318_028() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 7)
			case 0 ->  v;
			           ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 20)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				
				public class X {
					public static int foo(int i) throws IOException {
						int v ;
						int t = switch (i) {
						case 0 ->  v;
						case 2 -> {
							if (i > 1) {
								yield  2;
							}
							yield 3;
						}
						default -> throw new IOException();
						};
						return t;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.2.9, [tests the second rule - assigned]
 * V is [un]assigned after a switch statement (14.11) iff all of the following are true:
 *     V is [un]assigned before every break statement that may exit the switch statement.
 *     For each switch labeled rule (14.11.1) in the switch block, V is [un]assigned after the
 *         expression, block, or throw statement of the switch labeled rule.
 *     If there is a switch labeled statement group (14.11.1) in the switch block, then V is [un]assigned
 *         after the last block statement of the last switch labeled statement group.
 *     If there is no default label in the switch block, or if the switch block ends with a switch label
 *        followed by }, then V is [un]assigned after the selector expression
 */
public void testBug548318_029() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 24)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				
				public class X {
					public static int foo(int i) throws IOException {
						int v ;
						switch (i) {
						case 0 -> {
							v = 0;
						}
						case 2 -> {
							if (i > 1) {
								v =  2;
								break;
							}
							v = 3;
							break;
						}
						default -> throw new IOException();
						};
						return v;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
/**
 * From JLS 13 16.2.9, [tests the second rule - unassigned]
 * V is [un]assigned after a switch statement (14.11) iff all of the following are true:
 *     V is [un]assigned before every break statement that may exit the switch statement.
 *     For each switch labeled rule (14.11.1) in the switch block, V is [un]assigned after the
 *         expression, block, or throw statement of the switch labeled rule.
 *     If there is a switch labeled statement group (14.11.1) in the switch block, then V is [un]assigned
 *         after the last block statement of the last switch labeled statement group.
 *     If there is no default label in the switch block, or if the switch block ends with a switch label
 *        followed by }, then V is [un]assigned after the selector expression
 */
public void testBug548318_030() {
	if (!checkSwitchAllowedLevel())
		return;
	setPresetPreviewOptions();
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 20)
			return v;
			       ^
		The local variable v may not have been initialized
		----------
		2. ERROR in X.java (at line 24)
			Zork();
			^^^^
		The method Zork() is undefined for the type X
		----------
		""";
	String[] testFiles = new String[] {
			"X.java", // =================
			"""
				import java.io.IOException;
				
				public class X {
					public static int foo(int i) throws IOException {
						int v ;
						switch (i) {
						case 0 -> {
							v = 0;
						}
						case 2 -> {
							if (i > 1) {
								v =  2;
								break;
							}
					//		v = 3;
							break;
						}
						default -> throw new IOException();
						};
						return v;
					}
				\t
					public boolean bar() {
						Zork();
						return true;
					}
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				""",
	};
	this.runNegativeTest(
			testFiles,
			expectedProblemLog,
			null,
			true);
}
public static Class testClass() {
	return FlowAnalysisTest.class;
}
}

