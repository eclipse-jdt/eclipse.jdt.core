/*******************************************************************************
 * Copyright (c) 2013, 2020 GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FlowAnalysisTest8 extends AbstractNullAnnotationTest {

//Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "testReferenceExpression" };
//	TESTS_NUMBERS = new int[] { 561 };
//	TESTS_RANGE = new int[] { 1, 2049 };
}

public FlowAnalysisTest8(String name) {
	super(name);
}

public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public static Class testClass() {
	return FlowAnalysisTest8.class;
}

// Lambda with elided args inherits null contract from the super method
public void testLambda_01() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface ISAM {
					@NonNull String toString(@NonNull String prefix, @Nullable Object o);
				}
				""",
			"X.java",
			"""
				public class X {
					void test() {
						ISAM printer = (p,o) -> p.concat(o.toString());
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. WARNING in X.java (at line 3)
				ISAM printer = (p,o) -> p.concat(o.toString());
				                        ^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'
			----------
			2. ERROR in X.java (at line 3)
				ISAM printer = (p,o) -> p.concat(o.toString());
				                                 ^
			Potential null pointer access: this expression has a '@Nullable' type
			----------
			""");
}

// Lambda with declared args violates null contract of super
public void testLambda_02() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface ISAM {
					void process(@NonNull Object nn, @Nullable Object n, Object u);
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void test() {
						ISAM printer = (@NonNull  Object o1, @NonNull 	Object o2, @NonNull	 Object o3) -> System.out.println(2);
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 4)
				ISAM printer = (@NonNull  Object o1, @NonNull 	Object o2, @NonNull	 Object o3) -> System.out.println(2);
				                                     ^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter o2, inherited method from ISAM declares this parameter as @Nullable
			----------
			2. ERROR in X.java (at line 4)
				ISAM printer = (@NonNull  Object o1, @NonNull 	Object o2, @NonNull	 Object o3) -> System.out.println(2);
				                                              	           ^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter o3, inherited method from ISAM does not constrain this parameter
			----------
			""");
}

// Lambda with declared args inherits / modifies contract of super
public void testLambda_03() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface ISAM {
					void process(@NonNull Object nn, @Nullable Object n, Object u);
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void test() {
						ISAM printer1 = (Object 		  o1, 			Object o2, 			 Object o3)\s
											-> System.out.println(o1.toString()+o2.toString()+o3.toString());
						ISAM printer3 = (@Nullable Object o1, @Nullable Object o2, @Nullable Object o3)\s
											-> System.out.println(o1.toString()+o2.toString()+o3.toString());
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 5)
				-> System.out.println(o1.toString()+o2.toString()+o3.toString());
				                                    ^^
			Potential null pointer access: The variable o2 may be null at this location
			----------
			2. ERROR in X.java (at line 7)
				-> System.out.println(o1.toString()+o2.toString()+o3.toString());
				                      ^^
			Potential null pointer access: this expression has a '@Nullable' type
			----------
			3. ERROR in X.java (at line 7)
				-> System.out.println(o1.toString()+o2.toString()+o3.toString());
				                                    ^^
			Potential null pointer access: this expression has a '@Nullable' type
			----------
			4. ERROR in X.java (at line 7)
				-> System.out.println(o1.toString()+o2.toString()+o3.toString());
				                                                  ^^
			Potential null pointer access: this expression has a '@Nullable' type
			----------
			""");
}

// Lambda with declared args has illegal @NonNull an primitive argument
public void testLambda_04() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"""
				public interface ISAM {
					void process(int i);
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void test() {
						ISAM printer1 = (@NonNull int i)\s
											-> System.out.println(i);
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 4)
				ISAM printer1 = (@NonNull int i)\s
				                 ^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type int
			----------
			""");
}

// Lambda inherits null contract and has block with return statement
public void testLambda_05() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"ISAM.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface ISAM {
					@NonNull String toString(Object o);
				}
				""",
			"X.java",
			"""
				public class X {
					void test() {
						ISAM printer = (o) -> {
							System.out.print(13);
							return null; // error
						};
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 5)
				return null; // error
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			""");
}
// Lambda has no descriptor (overriding method from Object), don't bail out with NPE during analysis
public void testLambda_05a() {
	Map customOptions = getCompilerOptions();
	runNegativeTest(
		new String[] {
			"ISAM.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface ISAM {
					@NonNull String toString();
				}
				""",
			"X.java",
			"""
				public class X {
					void test() {
						ISAM printer = () -> {
							System.out.print(13);
							return null;
						};
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				ISAM printer = () -> {
				               ^^^^^
			The target type of this expression must be a functional interface
			----------
			""",
		this.LIBS,
		true /*flush*/,
		customOptions);
}
// Test flows with ReferenceExpression regarding:
// - definite assignment
// - unused local
public void testReferenceExpression1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runNegativeTest(
		new String[] {
			 "I.java",
			 """
				public interface I {
					public void bar();
				}
				""",
			 "X.java",
			 """
				public class X {
					public void moo() {}
					public static void soo() {}
					void testAssignment() {
						X x;
						I i = x::moo; // x is unassigned
						i.bar();
						I i2 = X::soo;
					}
					void testStatic() {
						X xs;
						I is = xs::soo;
					}
					void testUse() {
						X x1 = this, x2 = this; // x2 is not used, only x is
						I i = x1::moo;
						i.bar();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				I i = x::moo; // x is unassigned
				      ^
			The local variable x may not have been initialized
			----------
			2. ERROR in X.java (at line 12)
				I is = xs::soo;
				       ^^^^^^^
			The method soo() from the type X should be accessed in a static way\s
			----------
			3. ERROR in X.java (at line 15)
				X x1 = this, x2 = this; // x2 is not used, only x is
				             ^^
			The value of the local variable x2 is not used
			----------
			""",
		null/*libs*/, true/*flush*/, options);
}
public void testReferenceExpression_null_1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			 "I.java",
			 """
				public interface I {
					public void foo();
				}
				""",
			 "X.java",
			 """
				public class X {
					public void bar() {}
					void test() {
						X x = null;
						I i = x::bar;
						i.foo();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				I i = x::bar;
				      ^
			Null pointer access: The variable x can only be null at this location
			----------
			""",
		null/*libs*/, true/*flush*/, options);
}
public void testReferenceExpression_nullAnnotation_1() {
	runNegativeTestWithLibs(
		new String[] {
			 "I.java",
			 """
				import org.eclipse.jdt.annotation.*;
				public interface I {
					public @NonNull String foo(@Nullable Object s);
				}
				""",
			 "X.java",
			 """
				import org.eclipse.jdt.annotation.*;
				public class X {
					public @Nullable String bar(@NonNull Object s) { return s.toString(); }
					void test() {
						I i = this::bar;
						System.out.print(i.foo(null));
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				I i = this::bar;
				      ^^^^^^^^^
			Null type mismatch at parameter 1: required '@NonNull Object' but provided '@Nullable Object' via method descriptor I.foo(Object)
			----------
			2. ERROR in X.java (at line 5)
				I i = this::bar;
				      ^^^^^^^^^
			Null type mismatch at method return type: Method descriptor I.foo(Object) promises '@NonNull String' but referenced method provides '@Nullable String'
			----------
			""");
}
public void testReferenceExpression_nullAnnotation_2() {
	runWarningTestWithLibs(
		true, /* skipJavac */
		new String[] {
			 "I.java",
			 """
				import org.eclipse.jdt.annotation.*;
				public interface I {
					public @NonNull String foo(@Nullable Object s);
				}
				""",
			 "X.java",
			 """
				public class X {
					public String bar(Object s) { return s.toString(); }
					void test() {
						I i = this::bar;
						System.out.print(i.foo(null));
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in X.java (at line 4)
				I i = this::bar;
				      ^^^^^^^^^
			Null type safety at method return type: Method descriptor I.foo(Object) promises \'@NonNull String\' but referenced method provides \'String\'
			----------
			""");
}
public void testReferenceExpression_nullAnnotation_3() {
	runNegativeTest(
		new String[] {
			 "I.java",
			 """
				import org.eclipse.jdt.annotation.*;
				public interface I {
					public @NonNull String foo(Object s);
				}
				""",
			 "X.java",
			 """
				import org.eclipse.jdt.annotation.*;
				public class X {
					public @NonNull String bar(@NonNull Object s) { return ""; }
					void test() {
						I i = this::bar;
						System.out.print(i.foo(null));
					}
					Zork zork;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				I i = this::bar;
				      ^^^^^^^^^
			Null type safety: parameter 1 provided via method descriptor I.foo(Object) needs unchecked conversion to conform to '@NonNull Object'
			----------
			2. ERROR in X.java (at line 8)
				Zork zork;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		this.LIBS,
		true /*flush*/,
		getCompilerOptions());
}
public void testBug535308a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 """
					public class X {
						public int someTest() {
							boolean unused = false;
							final boolean thisIsFalse = false;
							if (getSomeValue() == thisIsFalse) {
								return 0;
							}
							return 1;
						}
						private boolean getSomeValue() {
							return true;
						}
					}"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 3)
					boolean unused = false;
					        ^^^^^^
				The value of the local variable unused is not used
				----------
				""";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 """
					public class X {
						public int someTest() {
							boolean unused = false;
							final boolean thisIsFalse = false;
							if (getSomeValue() != thisIsFalse) {
								return 0;
							}
							return 1;
						}
					
						private boolean getSomeValue() {
							return true;
						}
					}"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 3)
					boolean unused = false;
					        ^^^^^^
				The value of the local variable unused is not used
				----------
				""";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308c() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 """
					public class X {
						public int someTest() {
							boolean unused = false;
							final boolean thisIsFalse = false;
							if (thisIsFalse != getSomeValue()) {
								return 0;
							}
							return 1;
						}
					
						private boolean getSomeValue() {
							return true;
						}
					}"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 3)
					boolean unused = false;
					        ^^^^^^
				The value of the local variable unused is not used
				----------
				""";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308d() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 """
					public class X {
						public int someTest() {
							boolean unused = false;
							final boolean thisIsFalse = false;
							if (thisIsFalse == getSomeValue()) {
								return 0;
							}
							return 1;
						}
					
						private boolean getSomeValue() {
							return true;
						}
					}"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 3)
					boolean unused = false;
					        ^^^^^^
				The value of the local variable unused is not used
				----------
				""";
	runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug535308e() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
	runner.testFiles =
			new String[] {
				 "X.java",
				 """
					public class X {
						public int someTest() {
							boolean used = false;
							final boolean thisIsFalse = false;
							if (used == getSomeValue()) {
								return 0;
							}
							return 1;
						}
					
						private boolean getSomeValue() {
							return true;
						}
					}"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 4)
					final boolean thisIsFalse = false;
					              ^^^^^^^^^^^
				The value of the local variable thisIsFalse is not used
				----------
				""";
			runner.classLibraries =
			this.LIBS;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug474080() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.io.IOException;
				interface Config {
					double getTime(long l);
				}
				class MarketMaker {
					static double closeTime;
				}
				public class Test {
					 boolean stopped;
				    Config config;
				    long tick() { return 0L; }
				    void doStuff() {}
				    public void start() {
				        new Thread(() -> {
				            while (eventSequence.running) {
				                try
				                    {
				                    double t = config.getTime(tick());
				                    if ((t >= MarketMaker.closeTime)) {
				                        if ((! stopped)) {
				                            try {
				                                System.out.println("Stopping...");
				                                doStuff();
				                                Thread.sleep(250);
				                                System.out.println("Simulation finished");
				                            } catch (InterruptedException e) {
				                                e.printStackTrace();
				                            }
				                            stopped = true;
				                        }
				                    }
				                }
				                catch (IOException e) {
				                    e.printStackTrace();
				                }
				            }
				        }).start();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 15)
				while (eventSequence.running) {
				       ^^^^^^^^^^^^^
			eventSequence cannot be resolved to a variable
			----------
			""");
}
}
