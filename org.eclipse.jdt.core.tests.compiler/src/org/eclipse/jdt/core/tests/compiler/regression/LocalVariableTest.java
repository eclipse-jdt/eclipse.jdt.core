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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalVariableTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug537033" };
}
public LocalVariableTest(String name) {
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
			        int foo(){
			                int i;
			                return 1;
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
			  void foo() {
			    String temp;
			    try {
			      return;
			    }
			    catch (Exception e){
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
			  void foo() {
			    String temp;
			    try {
			      return;
			    }
			    catch (Exception e) {
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
			  {
			     int i = 1;
			    System.out.println(i);
			  }
			  X(int j){
			  }
			}
			""",
	});
}
public void test005() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  int j;\n" +
		"  void f1() {\n" +
		"    int l;\n" +
		"    switch (j) {\n" +
		"      case 0 :\n" +
		"        l = 10;\n" +
		"		 l++;\n" + // at least one read usage
		"        break;\n" +
		"      case 1 :\n" +
		"        l = 20;\n" +
		"        break;\n" +
		"      case 2 :\n" +
		"        l = 30;\n" +
		"        break;\n" +
		"      default :\n" +
		"        l = 10;\n" +
		"        break;\n" +
		"    }\n" +
		"  }\n" +
		"  public static void main(String args[]) {\n" +
		"  }\n" +
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/Truc.java",
		"""
			package p;
			public class Truc{
			   void foo(){
			      final int i;\s
				   i = 1;
			      if (false) i = 2;
			   }\s
				public static void main(java.lang.String[] args) {
			  		System.out.println("SUCCESS");\s
				}\t
			}""",
	},
	"SUCCESS");
}

public void test007() {
	this.runConformTest(new String[] {
		"p/A.java",
		"""
			package p;
			import p.helper.Y;
			class A extends Y {
			  class Y {
			    int j = i;// i is a protected member inherited from Y
			  }
			}""",

		"p/helper/Y.java",
		"""
			package p.helper;
			public class Y {
			  protected int i = 10;
			  public inner in = new inner();
			   \s
			  protected class inner {
			    public int  f() {
			      return 20;
			    }
			  }
			}""",

	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127078
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					class Y {
						Y innerY;
				
						int longMemberName;
					}
				
					static public void main(String args[]) {
						Y y;
						System.out.println(y.innerY.longMemberName);
					}
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 10)
					System.out.println(y.innerY.longMemberName);
					                   ^
				The local variable y may not have been initialized
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127078
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					class Y {
						int longMemberName;
					}
				
					static public void main(String args[]) {
						Y y;
						System.out.println(y.longMemberName);
					}
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 8)
					System.out.println(y.longMemberName);
					                   ^
				The local variable y may not have been initialized
				----------
				""");
}
public void test010() {
	Map options = getCompilerOptions();
	options.put(
		CompilerOptions.OPTION_DocCommentSupport,
		CompilerOptions.ENABLED);
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			/**
			 * @see Y
			 */
			public class X {
			}""",
		"p/Y.java",
		"""
			package p;
			class Z {
			}""",
	},
	"",
	null,
	true,
	null,
	options,
	null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144426
public void test011() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static void main(String[] args) {
					                int x = 2;
					                if (true) {
					                        int x = 4;
					                }
					        }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					int x = 4;
					    ^
				Duplicate local variable x
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void test012() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static void main(String[] args) {
					                int x = x = 0;
					                if (true) {
					                        int x = x = 1;
					                }
					        }
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					int x = x = 0;
					    ^^^^^^^^^
				The assignment to variable x has no effect
				----------
				2. ERROR in X.java (at line 5)
					int x = x = 1;
					    ^
				Duplicate local variable x
				----------
				3. WARNING in X.java (at line 5)
					int x = x = 1;
					    ^^^^^^^^^
				The assignment to variable x has no effect
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858 - variation
//check variable collision resiliance (catch argument)
// variable collision should not interfere with exception collision
public void test013() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static void main(String[] args) {
					                int x = 2;
					                try {
					                \t
					                } catch(Exception x) {
					                } catch(Exception e) {
					                }
					        }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					} catch(Exception x) {
					                  ^
				Duplicate parameter x
				----------
				2. ERROR in X.java (at line 7)
					} catch(Exception e) {
					        ^^^^^^^^^
				Unreachable catch block for Exception. It is already handled by the catch block for Exception
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858 - variation
public void test014() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(){
							int x = 0;
							String x = "";
							x.toString();
						  }
						}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					String x = "";
					       ^
				Duplicate local variable x
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157379
public void test015() {
	Map options = getCompilerOptions();
	if (this.complianceLevel == ClassFileConstants.JDK1_3) return;
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static boolean test() {
					                boolean b = false;
					                assert b = true;
					                return false;
					        }
					        public static void main(String[] args) {
					                test();
					                System.out.println("SUCCESS");
					        }
					}
					""",
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X extends Parent implements Doable {
					/**
					 * @param value
					 */
					void foo(int value) { // X#foo(...)
					}
					void bar(int value) { // X#bar(...)
					}
				
					void top(int value) { /* X#top(...)*/}
					void parent(int value) { /* X#parent(...) */}
					public void doit(int value) { /* X#doit(...) */}
				}
				abstract class Top {
					/**
					 * @param value
					 */
					abstract void top(int value); // Top#top(...)
				}
				abstract class Parent extends Top {
					/**
					 * @param value
					 */
					void parent(int value) { /* Parent#parent(...) */}
				}
				interface Doable {
					/**
					 * @param value
					 */
					void doit (int value); // Doable#doit(...)
				}""", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X extends Parent implements Doable {
					/**
					 * @param value
					 */
					void foo(int value) { // X#foo(...)
					}
					void bar(int value) { // X#bar(...)
					}
				
					void top(int value) { /* X#top(...)*/}
					void parent(int value) { /* X#parent(...) */}
					public void doit(int value) { /* X#doit(...) */}
				}
				abstract class Top {
					/**
					 * @param value
					 */
					abstract void top(int value); // Top#top(...)
				}
				abstract class Parent extends Top {
					/**
					 * @param value
					 */
					void parent(int value) { /* Parent#parent(...) */}
				}
				interface Doable {
					/**
					 * @param value
					 */
					void doit (int value); // Doable#doit(...)
				}""", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 24)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X extends Parent implements Doable {
					/**
					 * @param value
					 */
					void foo(int value) { // X#foo(...)
					}
					void bar(int value) { // X#bar(...)
					}
				
					void top(int value) { /* X#top(...)*/}
					void parent(int value) { /* X#parent(...) */}
					public void doit(int value) { /* X#doit(...) */}
				}
				abstract class Top {
					/**
					 * @param value
					 */
					abstract void top(int value); // Top#top(...)
				}
				abstract class Parent extends Top {
					/**
					 * @param value
					 */
					void parent(int value) { /* Parent#parent(...) */}
				}
				interface Doable {
					/**
					 * @param value
					 */
					void doit (int value); // Doable#doit(...)
				}""", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 24)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.ENABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X extends Parent implements Doable {
					/**
					 * @param value
					 */
					void foo(int value) { // X#foo(...)
					}
					void bar(int value) { // X#bar(...)
					}
				
					void top(int value) { /* X#top(...)*/}
					void parent(int value) { /* X#parent(...) */}
					public void doit(int value) { /* X#doit(...) */}
				}
				abstract class Top {
					/**
					 * @param value
					 */
					abstract void top(int value); // Top#top(...)
				}
				abstract class Parent extends Top {
					/**
					 * @param value
					 */
					void parent(int value) { /* Parent#parent(...) */}
				}
				interface Doable {
					/**
					 * @param value
					 */
					void doit (int value); // Doable#doit(...)
				}""", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	void top(int value) { /* X#top(...)*/}\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	void parent(int value) { /* X#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 12)\n" +
		"	public void doit(int value) { /* X#doit(...) */}\n" +
		"	                     ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 24)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test020() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.ENABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X extends Parent implements Doable {
					/** @param value */
					void foo(int value) { // X#foo(...)
					}
					void bar(int value) { // X#bar(...)
					}
				
					/** @param value */
					void top(int value) { /* X#top(...)*/}
					/** @param value */
					void parent(int value) { /* X#parent(...) */}
					/** @param value */
					public void doit(int value) { /* X#doit(...) */}
				}
				abstract class Top {
					/** @param value */
					abstract void top(int value); // Top#top(...)
				}
				abstract class Parent extends Top {
					/** @param value */
					void parent(int value) { /* Parent#parent(...) */}
				}
				interface Doable {
					/** @param value */
					void doit (int value); // Doable#doit(...)
				}""", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	void top(int value) { /* X#top(...)*/}\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	void parent(int value) { /* X#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 13)\n" +
		"	public void doit(int value) { /* X#doit(...) */}\n" +
		"	                     ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 21)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//No error message for exception parameter not being used.
public void test412119a() {
	runConformTest(new String[] {
			"p/X.java",
			"""
				package p;
				class X {
				\t
					void somethingDangerous() {}
					void foo() {
						try {
							somethingDangerous();
						} catch(Exception e) {
							throw new RuntimeException();
						}
					}
				}
				""",
		});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//Error message for exception parameter not being used.
public void test412119b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.ERROR);
	runner.testFiles =
			new String[] {
				"p/X.java",
				"package p;\n" +
				"class X {\n" +
				"	void somethingDangerous() {}\n" +
				"	void foo() {\n" +
				"		try {\n" +
				"			somethingDangerous();\n" +
				"		} catch(Exception e) {\n" +
				"				throw new RuntimeException();\n" +
				"		}\n" +
				"		try {\n" +
				"			somethingDangerous();\n" +

				// Exception thrown under a true boolean expression
				"		} catch(Exception e1) {\n" +
				"				if (true)\n" +
				"					throw new RuntimeException(e1);\n" +
				"		}\n" +

				// Catch clause parameter used.
				"		try {\n" +
				"			somethingDangerous();\n" +
				"		} catch(Exception e2) {\n" +
				"			throw new RuntimeException(e2);\n" +
				"		}\n" +
				"    }\n" +
				"}\n",
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p\\X.java (at line 7)
					} catch(Exception e) {
					                  ^
				The value of the exception parameter e is not used
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//Multi-catch parameters.
public void test412119c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.ERROR);
	runner.testFiles =
			new String[] {
				"p/X.java",
				"""
					package p;
					class X {
					class Z2 extends Exception {
						private static final long serialVersionUID = 1L;}
					class Z1 extends Exception {
						private static final long serialVersionUID = 1L;}
						void somethingDangerous(int x, int y) throws Z1, Z2 {
							if (x < 1)
								throw new Z1();
							if (y > 1)\s
								throw new Z2();
						}
						void foo(int x, int y) {
							try {
								somethingDangerous(x, y);
							} catch(Z2|Z1 z) {
								throw new RuntimeException();
							}
							try {
								somethingDangerous(x, y);
							} catch(Z2|Z1 z2) {
								throw new RuntimeException(z2);
							}
						}
					}
					"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p\\X.java (at line 16)
					} catch(Z2|Z1 z) {
					              ^
				The value of the exception parameter z is not used
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//Suppress Warnings.
public void test412119d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					class X {
						@SuppressWarnings("unused")
						void foo(int x) {}
						void somethingDangerous() {}
						@SuppressWarnings("unused")
						void foo3() {
							try {
								somethingDangerous();
							} catch(Exception e) {
								throw new RuntimeException();
							}
						}
					}
					""",
			},
			"" ,
			null,
			true,
			options);
}
public void testBug537033() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"ShowBug.java",
			"""
				import java.util.concurrent.Callable;
				
				public class ShowBug {
				    private static abstract class X {
				        abstract void x(int val);
				    }
				
				    public ShowBug() {
				        final X x = new X() {
				            void x(int val) {
				                if (val > 0) {
				                    // (1) The local variable x may not have been initialized
				                    x.x(val - 1);
				                }
				            }
				        };
				
				        new Callable<Void>() {
				            public Void call() {
				                // (2) Missing code implementation in the compiler
				                x.x(10);         \s
				                return null;
				            }
				        }.call();
				    }
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK11
		?
		"""
			----------
			1. WARNING in ShowBug.java (at line 9)
				final X x = new X() {
				                ^^^
			Access to enclosing constructor ShowBug.X() is emulated by a synthetic accessor method
			----------
			2. ERROR in ShowBug.java (at line 13)
				x.x(val - 1);
				^
			The local variable x may not have been initialized
			----------
			"""
		:
		"""
			----------
			1. ERROR in ShowBug.java (at line 13)
				x.x(val - 1);
				^
			The local variable x may not have been initialized
			----------
			""");
}
public static Class testClass() {
	return LocalVariableTest.class;
}
}
