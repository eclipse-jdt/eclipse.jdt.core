/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class RecordPatternTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 21");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testRecPatExhaust018" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return RecordPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public RecordPatternTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(false);
	}
	protected String[] getDefaultClassPaths() {
		String[] libs = DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
		if (this.extraLibPath != null) {
			String[] l = new String[libs.length + 1];
			System.arraycopy(libs, 0, l, 0, libs.length);
			l[libs.length] = this.extraLibPath;
			return l;
		}
		return libs;
	}
	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(false, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(false));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE21Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {}, JAVAC_OPTIONS);
	}
	protected void runConformTest(
			String[] testFiles,
			String expectedOutputString,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				null /* no custom options */,
				false /* do not perform statements recovery */,
				null /* no custom requestor */,
				// compiler results
				false /* expecting no compiler errors */,
				null /* do not check compiler log */,
				// runtime options
				false /* do not force execution */,
				vmArguments /* vm arguments */,
				// runtime results
				expectedOutputString /* expected output string */,
				null /* do not check error string */,
				// javac options
				JavacTestOptions.DEFAULT /* default javac test options */);
		}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	/*
	 * Basic tests that accept a valid record pattern and make the pattern variable available
	 */
	public void test001() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr) ) {
					        System.out.println("Upper-left corner:");
					    }
					  }
					  public static void main(String[] obj) {
					    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
					                               new ColoredPoint(new Point(10, 15), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"Upper-left corner:",
				options);
	}
	// Test that pattern variables are allowed for the nested patterns (not just the outermost record pattern)
	public void test002() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr)  -> {
					        		yield 1;
					        }
					        default -> 0;
					    };
					    System.out.println(res);
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"1");
	}
	public void test003() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr)) {
					        System.out.println("Upper-left corner: " + r1);
					    }
					  }
					  public static void main(String[] obj) {
					    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
					    new ColoredPoint(new Point(10, 15), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						System.out.println("Upper-left corner: " + r1);
						                                           ^^
					r1 cannot be resolved to a variable
					----------
					""");
	}
	public void test004() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr)) {
					    }
					  }
					  public static void main(String[] obj) {
					    print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE),
					    new ColoredPoint(new Point(10, 15), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE),
						                                         ^^^^^^^^^
					PointTypo cannot be resolved to a type
					----------
					""");
	}
	// Test that non record types are reported in a record pattern
	public void test005() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c),
						    									ColoredPoint lr)) {
						        System.out.println("Upper-left corner: ");
						    }
					  }
					}
					class Point{}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c),
						                                        ^^^^^
					Only record types are permitted in a record pattern
					----------
					""");
	}
	// Test that record patterns that don't have same no of patterns as record components are reported
	public void test006() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(int i), Color c),
						    									ColoredPoint lr)) {
						        System.out.println("Upper-left corner: ");
						    }
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (r instanceof Rectangle(ColoredPoint(Point(int i), Color c),
						                                        ^^^^^^^^^^^^
					Record pattern should match the signature of the record declaration
					----------
					""");
	}
	public void test007() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(String o1, String o2), Color c),
						    									ColoredPoint lr)) {
						        System.out.println("Upper-left corner: " );
						    }
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (r instanceof Rectangle(ColoredPoint(Point(String o1, String o2), Color c),
						                                              ^^^^^^^^^
					Record component with type int is not compatible with type java.lang.String
					----------
					2. ERROR in X.java (at line 3)
						if (r instanceof Rectangle(ColoredPoint(Point(String o1, String o2), Color c),
						                                                         ^^^^^^^^^
					Record component with type int is not compatible with type java.lang.String
					----------
					""");
	}
	// Test that pattern types that don't match record component's types are reported
	public void test008() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c), ColoredPoint lr, Object obj)) {
						        System.out.println("Upper-left corner: " );
						    }
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c), ColoredPoint lr, Object obj)) {
						                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Record pattern should match the signature of the record declaration
					----------
					""");
	}
	// Test that A pattern p dominates a record pattern with type R if p is unconditional at R.
	//	case Rectangle c -> {
	//		yield 0;
	//	}
	//	case Rectangle(ColoredPoint(Point(int x, int y), Color c),
	//			ColoredPoint(Point(int x1, int y1), Color c1)) r1 -> {
	//		yield r1.lowerRight().p().y();
	//	}
	public void test009() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    int res = switch(r) {
					       	case Rectangle c -> {
								yield 0;
							}
							case Rectangle(ColoredPoint(Point(int x, int y), Color c),
									ColoredPoint(Point(int x1, int y1), Color c1)) -> {
								yield 1;
							}
					    };
					    System.out.println(res);
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c),
									ColoredPoint(Point(int x1, int y1), Color c1)) -> {
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The switch statement cannot have more than one unconditional pattern
					----------
					2. ERROR in X.java (at line 7)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c),
									ColoredPoint(Point(int x1, int y1), Color c1)) -> {
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that nested pattern variables from record patterns are in scope in the case block
	public void test10() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void printLowerRight(Rectangle r) {
					    int res = switch(r) {
					       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
					                               ColoredPoint lr)  -> {
					    				System.out.println("x= " + x);
					    				System.out.println("y= " + y);
					    				System.out.println("lr= " + lr);
					    				System.out.println("lr.c()= " + lr.c());
					    				System.out.println("lr.p()= " + lr.p());
					    				System.out.println("lr.p().x()= " + lr.p().x());
					    				System.out.println("lr.p().y()= " + lr.p().y());
					    				System.out.println("c= " + c);
					        		yield x;
					        }
					        default -> 0;
					    };
					    System.out.println("Returns: " + res);
					  }
					  public static void main(String[] args) {
					    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),
					        new ColoredPoint(new Point(30, 10), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					x= 15
					y= 5
					lr= ColoredPoint[p=Point[x=30, y=10], c=RED]
					lr.c()= RED
					lr.p()= Point[x=30, y=10]
					lr.p().x()= 30
					lr.p().y()= 10
					c= BLUE
					Returns: 15""");
	}
	// Test that nested pattern variables from record patterns are in not scope outside the case block
	public void test11() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    int res = switch(r) {
							case Rectangle(ColoredPoint(Point(int x, int y), Color c),
									ColoredPoint(Point(int x1, int y1), Color c1)) -> {
								yield 1;
							}
							default -> {yield x;}\
					    };
					    System.out.println(res);
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						default -> {yield x;}    };
						                  ^
					x cannot be resolved to a variable
					----------
					""");
	}
	// Test that nested pattern variables from record patterns are in not scope outside the case block
	public void test12() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
							  static void print(Rectangle r) {
							    int res = switch(r) {
									case Rectangle(ColoredPoint(Point(int x, int y), Color c),
											ColoredPoint(Point(int x1, int y1), Color c1)) -> {
										yield 1;
									}
									default -> {yield x1;}\
							    };
							    System.out.println(res);
							  }
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
			},
				"""
					----------
					1. ERROR in X.java (at line 8)
						default -> {yield x1;}    };
						                  ^^
					x1 cannot be resolved to a variable
					----------
					""");
	}
	// Test that when expressions are supported and pattern variables are available inside when expressions
	public void test13() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
							  public static void printLowerRight(Rectangle r) {
							    int res = switch(r) {
							       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
							                               ColoredPoint lr)  when x > 0 -> {
							        		yield 1;
							        }
							       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
							                               ColoredPoint lr)  when x <= 0 -> {
							        		yield -1;
							        }
							        default -> 0;
							    };
							    System.out.println("Returns: " + res);
							  }
							  public static void main(String[] args) {
							    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
							        new ColoredPoint(new Point(30, 10), Color.RED)));
							    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),
							        new ColoredPoint(new Point(30, 10), Color.RED)));
							  }
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
				"Returns: -1\n" +
				"Returns: 1");
	}
	// Test that record patterns with 1 record components are accepted
	public void test14() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
					  public static void print(Record r) {
					    int res = switch(r) {
					       case Record(int x) -> x ;
					        default -> 0;
					    };
					    System.out.println("Returns: " + res);
					  }
					  public static void main(String[] args) {
					    print(new Record(3));
					  }
					}
					record Record(int x) {}
					"""
						},
				"Returns: 3");
	}
	// Test that record patterns with 0 record components are accepted
	public void test15() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
						 @SuppressWarnings("preview")
						public static void print(Pair p) {
					    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i)) ) {
								 System.out.println(n1);
							 } else {
								 System.out.println("ELSE");
							 }
					  }
					  public static void main(String[] args) {
					    print(new Pair(new Teacher("123"), new Student("abc", 1)));
					  }
					}
					sealed interface Person permits Student, Teacher {
					    String name();
					}
					 record Student(String name, Integer id) implements Person {}
					 record Teacher(String name) implements Person {}
					 record Pair(Person s, Person s1) {}
					"""
						},
				"abc");
	}
	// Should not reach IF or throw CCE.
	// Should reach ELSE
	public void test16() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
						 @SuppressWarnings("preview")
						public static void print(Pair p) {
					    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i))) {
								 System.out.println("IF");
							 } else {
								 System.out.println("ELSE");
							 }
					  }
					  public static void main(String[] args) {
					    print(new Pair(new Student("abc", 1), new Teacher("123")));
					  }
					}
					sealed interface Person permits Student, Teacher {
					    String name();
					}
					 record Student(String name, Integer id) implements Person {}
					 record Teacher(String name) implements Person {}
					 record Pair(Person s, Person s1) {}
					"""
						},
				"ELSE");
	}
	public void test17() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
						 @SuppressWarnings("preview")
						public static void print(Pair p) {
					    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i))) {
								 System.out.println(n1.getClass().getTypeName() + ":" + n1 + "," + i);
							 } else {
								 System.out.println("ELSE");
							 }
					  }
					  public static void main(String[] args) {
					    print(new Pair(new Teacher("123"), new Student("abc", 10)));
					  }
					}
					sealed interface Person permits Student, Teacher {
					    String name();
					}
					 record Student(String name, Integer id) implements Person {}
					 record Teacher(String name) implements Person {}
					 record Pair(Person s, Person s1) {}
					"""
						},
				"java.lang.String:abc,10");
	}
	// Same as 17(), but base type instead of wrapper
	public void test18() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
						 @SuppressWarnings("preview")
						public static void print(Pair p) {
					    if (p instanceof Pair(Teacher(Object n), Student(Object n1, int i))) {
								 System.out.println(n1.getClass().getTypeName() + ":" + n1 + "," + i);
							 } else {
								 System.out.println("ELSE");
							 }
					  }
					  public static void main(String[] args) {
					    print(new Pair(new Teacher("123"), new Student("abc", 10)));
					  }
					}
					sealed interface Person permits Student, Teacher {
					    String name();
					}
					 record Student(String name, int id) implements Person {}
					 record Teacher(String name) implements Person {}
					 record Pair(Person s, Person s1) {}
					"""
						},
				"java.lang.String:abc,10");
	}
	public void test19() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
								 @SuppressWarnings("preview")
								public static void print(Pair p) {
									 int res1 = switch(p) {
									 	case Pair(Student(Object n1, int i), Teacher(Object n)) -> {
							              	   yield i;
							                 }
									 	default -> -1;
									 };
									 System.out.println(res1);
							  }
								 public static void main(String[] args) {
									print(new Pair( new Student("abc", 15), new Teacher("123")));
									print(new Pair( new Teacher("123"), new Student("abc", 1)));
								}
							}
							sealed interface Person permits Student, Teacher {
							    String name();
							}
							 record Student(String name, int id) implements Person {}
							 record Teacher(String name) implements Person {}
							 record Pair(Person s, Person s1) {}
							"""
						},
				"15\n"
				+ "-1");
	}
	// Test that Object being pattern-checked works in switch-case
	public void test20() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
								 @SuppressWarnings("preview")
								public static void print(Object p) {
									 int res1 = switch(p) {
									 	case Pair(Student(Object n1, int i), Teacher(Object n)) -> {
							              	   yield i;
							                 }
									 	default -> -1;
									 };
									 System.out.println(res1);
							  }
								 public static void main(String[] args) {
									print(new Pair( new Student("abc", 15), new Teacher("123")));
									print(new Pair( new Teacher("123"), new Student("abc", 1)));
								}
							}
							sealed interface Person permits Student, Teacher {
							    String name();
							}
							 record Student(String name, int id) implements Person {}
							 record Teacher(String name) implements Person {}
							 record Pair(Person s, Person s1) {}
							"""
						},
				"15\n"
				+ "-1");
	}
	// // Test that Object being pattern-checked works in 'instanceof'
	public void test21() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")\
					public class X {
						 @SuppressWarnings("preview")
						public static void print(Object p) {
					    if (p instanceof Pair(Student(Object n1, int i), Teacher(Object n))) {
					      System.out.println(i);
					    }
					  }
						 public static void main(String[] args) {
							print(new Pair( new Student("abc", 15), new Teacher("123")));
							print(new Pair( new Teacher("123"), new Student("abc", 1)));
						}
					}
					sealed interface Person permits Student, Teacher {
					    String name();
					}
					 record Student(String name, int id) implements Person {}
					 record Teacher(String name) implements Person {}
					 record Pair(Person s, Person s1) {}
					"""
						},
				"15");
	}
	// Nested record pattern with a simple (constant) 'when' clause
	public void test22() {
		runConformTest(new String[] {
				"X.java",
					"""
						public class X {
						  public static void printLowerRight(Rectangle r) {
						    int res = switch(r) {
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr) when x > 1 -> {
						                            	   System.out.println("one");
						        		yield x;
						        }
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr) when x <= 0 -> {
						                            	   System.out.println("two");\t
						        		yield x;
						        }
						        default -> 0;
						    };
						    System.out.println("Returns: " + res);
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
						},
				"""
					two
					Returns: 0
					one
					Returns: 5""",
				getCompilerOptions(false),
				null,
				JavacTestOptions.SKIP); // Javac crashes. Let's skip for no
	}
	// Nested record pattern with a method invocation in a 'when' clause
	public void test23 () {
		runConformTest(new String[] {
				"X.java",
					"""
						@SuppressWarnings("preview")\
						public class X {
						  public static void printLowerRight(Rectangle r) {
						    int res = switch(r) {
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                               ColoredPoint lr) when x > value() -> {
						                            	   System.out.println("one");
						        		yield x;
						        }
						        default -> 0;
						    };
						    System.out.println("Returns: " + res);
						  }
						  public static int value() {
						    return 0;
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
						},
				"""
					Returns: 0
					one
					Returns: 5""");
	}
	// Nested record pattern with another switch expression + record pattern in a 'when' clause
	// Failing now.
	public void test24() {
		runConformTest(new String[] {
				"X.java",
					"""
						public class X {
						  @SuppressWarnings("preview")
						  public static void printLowerRight(Object r) {
						    int res = switch(r) {
						       case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						    		   				ColoredPoint lr) when x >
														       switch(r) {
														       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1) -> 2;
														       	 default -> 3;
														       }
														       	-> x;
														       default -> 0;
						    			};
						    			System.out.println("Returns: " + res);
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
						},
				"Returns: 0\n" +
				"Returns: 5");
	}
	public void test24a() {
		runConformTest(new String[] {
				"X.java",
					"""
						public class X {
						  @SuppressWarnings("preview")
						  public static void printLowerRight(Object r) {
						    	  int x = 0;
						       if (r instanceof Rectangle(ColoredPoint c,  ColoredPoint lr) && x < switch(r) {
						    	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1)  -> 2;
						    	 default -> 3;
							  }) {
								  System.out.println("IF");
							  }
						  }
						  public static void main(String[] args) {
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),
						        new ColoredPoint(new Point(30, 10), Color.RED)));
						  }
						}
						record Point(int x, int y) {}
						enum Color { RED, GREEN, BLUE }
						record ColoredPoint(Point p, Color c) {}
						record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
						},
				"IF\n" +
				"IF");
	}
	//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/157
	public void test25() {
		String currentWorkingDirectoryPath = System.getProperty("user.dir");
		this.extraLibPath = currentWorkingDirectoryPath + File.separator + "libtest25.jar";
		try {
		Util.createJar(
			new String[] {
				"p/RecordPattern1.java;\n",
				"""
					package p;
					public class RecordPattern1 {}
					record Point(int x, int y) {}
					enum Color {
						RED, GREEN, BLUE
					}
					""",
				"p/ColoredPoint.java",
				"package p;\n"
				+ "public record ColoredPoint(Point p, Color c) {}\n",
				"p/Rectangle.java",
				"package p;\n"
				+ "public record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}\n",
			},
			this.extraLibPath,
			JavaCore.VERSION_21);
		this.runConformTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							public class X {
								public static void printLowerRight(Rectangle r) {
									int res = switch(r) {
									case Rectangle(ColoredPoint(Point(int x, int y), Color c),
											ColoredPoint lr)  -> {
												yield 1;
											}
											default -> 0;
									};
									System.out.println(res);
								}
								public static void main(String[] args) {
										    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),
										        new ColoredPoint(new Point(30, 10), Color.RED)));
										  }
							}
							"""
				},
				"1",
				getCompilerOptions(false),
				new String[0],
				JavacTestOptions.SKIP); // Too complicated to pass extra lib to Javac, let's skip
		} catch (IOException e) {
			System.err.println("RecordPatternTest.test25() could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(this.extraLibPath).delete();
		}
	}
	// Test that pattern variables declared in instanceof can't be used in a switch/case
	public void test26() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
							  static void print(Rectangle r) {
							    	if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),
										ColoredPoint lr) && x > (switch(r) {
																	case Rectangle(ColoredPoint(Point(int x, int y), Color c),
																			ColoredPoint lr) -> {
																				yield 1;
																			}
																			default -> 0;
																			})) {
									System.out.println(x);
								  }
								}
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
			},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                                      ^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 5)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                                             ^
					A pattern variable with the same name is already defined in the statement
					----------
					3. ERROR in X.java (at line 5)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c),
						                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					4. ERROR in X.java (at line 6)
						ColoredPoint lr) -> {
						             ^^
					A pattern variable with the same name is already defined in the statement
					""");
	}
	// Test that pattern variables declared in switch/case can't be used in an instanceof expression part of the 'when' clause
	// not relevant anymore since named record patterns are not there - 20
	public void test27() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
							  static void print(Rectangle r) {
								int res = switch(r) {
									case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {
											yield 1;
										}
										default -> 0;
								};
								}
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
			},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {
						                                                                                                                      ^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 4)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {
						                                                                                                                             ^
					A pattern variable with the same name is already defined in the statement
					----------
					3. ERROR in X.java (at line 4)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {
						                                                                                                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					4. ERROR in X.java (at line 4)
						case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {
						                                                                                                                                       ^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}
	// Test nested record patterns in 'instanceof' within a swith-case with similar record pattern
	public void test28() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					  static void print(Rectangle r) {
					    int res = switch(r) {
							case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),
									ColoredPoint lr1)) -> {
									yield lr1.p().y();
								}
								default -> 0;
					     };
					   System.out.println(res);
					  }
					  public static void main(String[] args) {
					    print(new Rectangle(new ColoredPoint(new Point(1,1), Color.RED), new ColoredPoint(new Point(5,5), Color.RED)));
					  }
					}
					record Point(int x, int y) {}
					enum Color { RED, GREEN, BLUE }
					record ColoredPoint(Point p, Color c) {}
					record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
			},
			"5");
	}
	// Test that a simple type pattern dominates a following record pattern of the same type
	public void test29() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							        case R r -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
								}
							}
							record R(int i) {}"""
		},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case R(int a) -> 0;
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that an identical record pattern dominates another record pattern
	public void test30() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							        case R(int a) -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
								}
							}
							record R(int i) {}"""
		},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case R(int a) -> 0;
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that a type pattern with 'when' does not dominate a record pattern of the same type
	public void test31() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
								public boolean predicate() { return true; }
								public void foo(Object o) {
							       int res = switch (o) {
							        case R r when predicate() -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
							       System.out.println(res);
								}
							  public static void main(String[] args) {
							    (new X()).foo(new R(10));
							  }
							}
							record R(int i) {}"""
		},
			"1");
	}
	// Test that a type pattern with 'when' does not dominate a record pattern of the same type
	public void test31a() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
								public boolean predicate() { return false; }
								public void foo(Object o) {
							       int res = switch (o) {
							        case R r when predicate() -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
							       System.out.println(res);
								}
							  public static void main(String[] args) {
							    (new X()).foo(new R(10));
							  }
							}
							record R(int i) {}"""
		},
			"0");
	}
	// Test that a record pattern with 'when' does not dominate an identical record pattern
	public void test32() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
								public boolean predicate() { return true; }
								public void foo(Object o) {
							       int res = switch (o) {
							        case R(int a)  when predicate() -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
							       System.out.println(res);
								}
							  public static void main(String[] args) {
							    (new X()).foo(new R(10));
							  }
							}
							record R(int i) {}"""
		},
			"1");
	}
	// Test that a record pattern with 'when' does not dominate an identical record pattern
	public void test32a() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
						public boolean predicate() { return false; }
						public void foo(Object o) {
					       int res = switch (o) {
					        case R(int a)  when predicate() -> 1;
					        case R(int a) -> 0;
					        default -> -1;
					       };
					       System.out.println(res);
						}
					  public static void main(String[] args) {
					    (new X()).foo(new R(10));
					  }
					}
					record R(int i) {}"""
		},
			"0");
	}
	// Test that a parenthesized type pattern dominates a record pattern of the same type
	public void test33() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							        case R r -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
								}
							}
							record R(int i) {}"""
		},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case R(int a) -> 0;
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that a parenthesized record pattern dominates an identical record pattern
	public void test34() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							        case R(int a) -> 1;
							        case R(int a) -> 0;
							        default -> -1;
							       };
								}
							}
							record R(int i) {}"""
		},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case R(int a) -> 0;
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that pattern dominance is reported on identical nested record pattern
	public void test35() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 0;
							       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 1;
							        default -> -1;
							       };
							       System.out.println(res);
								}
								public static void main(String[] args) {
									(new X()).foo(new Pair(new Teacher("123"), new Student("abc", 10)));
								}
							}
							record R(int i) {}
							sealed interface Person permits Student, Teacher {
								String name();
							}
							record Student(String name, Integer id) implements Person {}
							record Teacher(String name) implements Person {}
							record Pair(Person s, Person s1) {} """
		},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 1;
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that pattern dominance is reported on identical nested record pattern
	public void test36() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 0;
							       case Pair(Teacher(Object n), Student(String n1, Integer i)) -> 1;
							        default -> -1;
							       };
							       System.out.println(res);
								}
								public static void main(String[] args) {
									(new X()).foo(new Pair(new Teacher("123"), new Student("abc", 10)));
								}
							}
							record R(int i) {}
							sealed interface Person permits Student, Teacher {
								String name();
							}
							record Student(String name, Integer id) implements Person {}
							record Teacher(String name) implements Person {}
							record Pair(Person s, Person s1) {} """
		},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case Pair(Teacher(Object n), Student(String n1, Integer i)) -> 1;
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that pattern dominance is reported on identical nested record pattern
	public void test37() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
								@SuppressWarnings("preview")
								public void foo(Object o) {
							       int res = switch (o) {
							       case Pair(Teacher(Object n), Student(String n1, Integer i)) -> 0;
							       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 1;
							        default -> -1;
							       };
							       System.out.println(res);
								}
								public static void main(String[] args) {
									(new X()).foo(new Pair(new Teacher("123"), new Student("abc", 10)));
								}
							}
							record R(int i) {}
							sealed interface Person permits Student, Teacher {
								String name();
							}
							record Student(String name, Integer id) implements Person {}
							record Teacher(String name) implements Person {}
							record Pair(Person s, Person s1) {} """
		},
				"0");
	}
	// Test that null is not matched to any pattern
	public void test38() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
							  static void print(Rectangle r) {
							    int res = switch(r) {
									case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),
											ColoredPoint lr1)) -> {
											yield lr1.p().y();
										}
										default -> 0;
							     };
							   System.out.println(res);
							  }
							  public static void main(String[] args) {
							    print(new Rectangle(new ColoredPoint(null, Color.RED), new ColoredPoint(new Point(5,5), Color.RED)));
							  }
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
		"0");
	}
	public void test39() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
							  static void print(Rectangle r) {
							    int res = switch(r) {
									case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),
											ColoredPoint lr1)) -> {
											yield lr1.p().y();
										}
										default -> 0;
							     };
							   System.out.println(res);
							  }
							  public static void main(String[] args) {
							    print(new Rectangle(null, null));
							  }
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
		"0");
	}
	public void test40() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
							  static void print(Rectangle r) {
							    int res = switch(r) {
									case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),
											ColoredPoint lr1)) -> {
											yield lr1.p().y();
										}
										default -> 0;
							     };
							   System.out.println(res);
							  }
							  public static void main(String[] args) {
							    try {
									  print(null);
								  } catch(NullPointerException e) {
									  System.out.println("NPE with " + e.toString());
								  }
							  }
							}
							record Point(int x, int y) {}
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
		"NPE with java.lang.NullPointerException");
	}
	public void test41() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
							  @SuppressWarnings("preview")
							  public static void printLowerRight(Object r) {
							    long res = switch(r) {
							       case Rectangle(ColoredPoint(Point(var x, long y), Color c),\s
							    		   				ColoredPoint lr) when x >\s
															       switch(r) {
															       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1) -> 2; \s
															       	 default -> 10;  \s
															       }\s
															       	-> x + 10; \s
															       default -> 0;    \s
							    			};   \s
							    			System.out.println("Returns: " + res);
							  }\s
							  public static void main(String[] args) {
								printLowerRight(new Rectangle(new ColoredPoint(new Point(2, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN)));\s
							  }
							}
							record Point(long x, long y) {}   \s
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}\s
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
		"Returns: 0");
	}
	public void test42() {
		runConformTest(new String[] {
				"X.java",
						"""
							public class X {
							  @SuppressWarnings("preview")
							  public static void printLowerRight(Object r) {
							    long res = switch(r) {
							       case Rectangle(ColoredPoint(Point(var x, long y), Color c),\s
							    		   				ColoredPoint lr) when x >\s
															       switch(r) {
															       	 case Rectangle(ColoredPoint c1,  var lr1)  -> lr1.p().x();
															       	 default -> 10;  \s
															       }\s
															       	-> x + 10; \s
															       default -> 0;    \s
							    			};   \s
							    			System.out.println("Returns: " + res);
							  }\s
							  public static void main(String[] args) {
								printLowerRight(new Rectangle(new ColoredPoint(new Point(10, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN)));\s
							  }
							}
							record Point(long x, long y) {}   \s
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}\s
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
		"Returns: 20");
	}
	public void test43() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
							  @SuppressWarnings("preview")
							  public static void printLowerRight(Object r) {
							    long res = switch(r) {
							       case Rectangle(ColoredPoint(Point(var x, long y), Color c),\s
							    		   				ColoredPoint lr) when x >\s
															       switch(r) {
															       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1)  -> 2; \s
															       	 default -> 10;  \s
															       }\s
															       	-> x + 10; \s
															       default -> 0;    \s
							    			};   \s
							    			System.out.println("Returns: " + res);
							  }\s
							  public static void main(String[] args) {
								printLowerRight(new Rectangle(new ColoredPoint(new Point(2, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN)));\s
							  }
							}
							record Point(int x, int y) {}   \s
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}\s
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case Rectangle(ColoredPoint(Point(var x, long y), Color c),\s
					                                         ^^^^^^
				Record component with type int is not compatible with type long
				----------
				""");
	}
	public void test44() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
							  @SuppressWarnings("preview")
							  public static void printLowerRight(Object r) {
							    long res = switch(r) {
							       case Rectangle(ColoredPoint(Point(var x, int y), Color c),\s
							    		   				ColoredPoint lr) when x >\s
															       switch(r) {
															       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1)  -> 2; \s
															       	 default -> 10;  \s
															       }\s
															       	-> x + 10; \s
															       default -> 0;    \s
							    			};   \s
							    			System.out.println("Returns: " + res);
							  }\s
							  public static void main(String[] args) {
								printLowerRight(new Rectangle(new ColoredPoint(new Point(2, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN)));\s
							  }
							}
							record Point(long x, long y) {}   \s
							enum Color { RED, GREEN, BLUE }
							record ColoredPoint(Point p, Color c) {}\s
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"""
		},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case Rectangle(ColoredPoint(Point(var x, int y), Color c),\s
					                                         ^^^^^
				Record component with type long is not compatible with type int
				----------
				""");
	}
	public void test45() {
		runNegativeTest(new String[] {
				"X.java",
						"""
							public class X {
								static void print(Object r) {
									switch (r) {
										case Rectangle(var a, var b) when (r instanceof Rectangle(ColoredPoint upperLeft2, ColoredPoint lowerRight)):
											System.out.println(r);// error should not be reported here
										break;
									}
								}
							}
							record ColoredPoint() {}
							record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {} """
		},
			"""
				----------
				1. ERROR in X.java (at line 3)
					switch (r) {
					        ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void test46() {
		runConformTest(new String[] {
			"X.java",
				"""
					  @SuppressWarnings("preview")
					public class X {
					  static void printGenericBoxString1(Box<Object> objectBox) {
						  if (objectBox instanceof Box<Object>(String s)) {
							  System.out.println(s);\s
						  }
					  }
					static void printGenericBoxString2(Box<String> stringBox) {
					    if (stringBox instanceof Box<String>(var s)) {
					      System.out.println(s);
					    }
					  }
					public static void main(String[] args) {
						printGenericBoxString1(new Box("Hello"));
						Object o = new Integer(10);
						Box<Object> box = new Box(o);
						printGenericBoxString1(box);
					}
					}
					record Box<T>(T t) {} """
			},
				"Hello");
	}
	public void test47() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					  @SuppressWarnings("preview")
					public class X {
					  static void printGenericBoxString1(Box<Object> objectBox) {
					    if (objectBox instanceof Box<String>(String s)) {
					      System.out.println(s); // this one should report an unsafe cast error
					    }
					  }
					  public static void main(String[] args) {}
					}
					record Box<T>(T t) {} """
			},
				"""
					----------
					1. ERROR in X.java (at line 4)
						if (objectBox instanceof Box<String>(String s)) {
						    ^^^^^^^^^
					Type Box<Object> cannot be safely cast to Box<String>
					----------
					""");
	}
	public void test48() {
		runConformTest(new String[] {
			"X.java",
				"""
					  @SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							erroneousTest1(new Box<>("A"));
							erroneousTest2(new Box<>("B"));
						}
						static void erroneousTest1(Box<Object> bo) {
							if (bo instanceof Box(var s)) {
								System.out.println("I'm a box of " + s.getClass().getName());
							}
						}
						static void erroneousTest2(Box b) {
							if (b instanceof Box(var t)) {
								System.out.println("I'm a box of " + t.getClass().getName());
							}
						}
						record Box<T> (T t) {
						}
					}"""
			},
				"I\'m a box of java.lang.String\n" +
				"I\'m a box of java.lang.String");
	}
	public void testIssue690_1() {
		runNegativeTest(new String[] {
				"X.java",
					"""
						public class X {
							public void foo(Object s) {
								switch (s) {
									case R(Integer i1, Double i1) -> {}
									case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}
									default -> {}
								}
							}
						}\s
						record R(Integer i1, Double i2) {}
						record OuterR(R r1, R r2) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case R(Integer i1, Double i1) -> {}
						                          ^^
					A pattern variable with the same name is already defined in the statement
					----------
					2. ERROR in X.java (at line 5)
						case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}
						                                                ^^
					A pattern variable with the same name is already defined in the statement
					----------
					3. ERROR in X.java (at line 5)
						case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}
						                                                           ^^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}
	public void testIssue690_2() {
		runNegativeTest(new String[] {
				"X.java",
					"""
						public class X {
							public void foo(Object s) {
								if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i3, Double i4))) {\s
										System.out.println("IF");
								}
								if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i1, Double i4))) {\s
										System.out.println("SECOND IF");
								}
							}
						}\s
						record R(Integer i1, Double i2) {}
						record OuterR(R r1, R r2) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i1, Double i4))) {\s
						                                                            ^^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}

	public void testIssue691_1() {
		runNegativeTest(new String[] {
				"X.java",
					"""
						public class X {
							public void foo(Number s) {
								switch (s) {
									case R(Integer i1, Integer i2) -> {}
									default -> {}
								}
							}
							public void foo(Object o) {
								switch (o) {
									case R(Number i1, Integer i2) -> {}
									default -> {}
								}
							}
							public void bar(Object o) {
								switch (o) {
								case R(Integer i1, Integer i2)-> {}
									default -> {}
								}
							}
						}\s
						record R(Integer i1, Integer i2) {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case R(Integer i1, Integer i2) -> {}
						     ^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from Number to R
					----------
					""");
	}
	public void testRemoveNamedRecordPatterns_001() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					 public static void foo(Rectangle r) {
					   int res = switch (r) {
					     case Rectangle(int x, int y) r -> 1;
					     default -> 0;
					   };
					   System.out.println(res);
					 }
					 public static void main(String[] args) {
					   foo(new Rectangle(10, 20));
					 }
					}
					record Rectangle(int x, int y) {
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Rectangle(int x, int y) r -> 1;
						                             ^
					Syntax error on token "r", delete this token
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2004
	// [Patterns] ECJ generates suspect code for switching over patterns
	public void testIssue2004() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				record A() {}
					record R<T>(T t) {}
					public class X {
					    private static boolean foo(R<? super A> r) {
					       return switch(r) {
					            case R(var x) -> true;
					            default -> false;
					       };
					    }
					    public static void main(String argv[]) {
					       System.out.println(foo(new R<A>(null)));
					    }
				}
				"""
				},
				"true");
	}
	public void testRecordPatternTypeInference_001() {
		runNegativeTest(new String[] {
			"X.java",
			"""
				import java.util.function.UnaryOperator;
				record Mapper<T>(T in, T out) implements UnaryOperator<T> {
				    public T apply(T arg) { return in.equals(arg) ? out : null; }
				}
				public class X {
				 void test(UnaryOperator<? extends CharSequence> op) {
				     if (op instanceof Mapper(var in, var out)) {
				         boolean shorter = out.length() < in.length();
				     }
				 }\s
				 Zork();
				}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^^^
				Return type for the method is missing
				----------
				2. ERROR in X.java (at line 11)
					Zork();
					^^^^^^
				This method requires a body instead of a semicolon
				----------
				""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796
	// [Patterns] Record Patterns can cause VerifyError
	public void testGH1796() {
		runConformTest(new String[] {
			"X.java",
			"""
			public record X(int i) {

			  public static void main(String[] args) {
			    new Printer().print(new X(42), new StringBuilder());
			  }

			  static final class Printer {

			    private void print(X e, StringBuilder buffer) {
			      if (e instanceof X(int i)) {
			          System.out.println(i);
			      }
			    }
			  }
			}
			"""
			},
			"42");
	}
	public void testRecordPatternTypeInference_002() {
		runConformTest(new String[] {
			"X.java",
			"""
				import java.util.function.UnaryOperator;
				record Mapper<T>(T in) implements UnaryOperator<T> {
				    public T apply(T arg) { return in.equals(arg) ? in : null; }
				}
				public class X {
				 @SuppressWarnings("preview")
				 public static boolean test(UnaryOperator<? extends CharSequence> op) {
				     if (op instanceof Mapper(var in)) {
				         return in.length() > 0;
				     }
				   return false;
				 }
				 public static void main(String[] args) {
				   Mapper<CharSequence> op = new Mapper<>(new String("abcd"));
				   System.out.println(test(op));
				 }
				}"""
			},
			"true");
	}
	public void testRecordPatternTypeInference_003() {
		runConformTest(new String[] {
			"X.java",
				"""
					  @SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							foo(new Box<>("B"));
						}
						static void foo(Box b) {
							if (b instanceof Box(var t)) {
								System.out.println("I'm a box of " + t.getClass().getName());
							}
						}
						record Box<T> (T t) {
						}
					}"""
			},
				"I\'m a box of java.lang.String");
	}
	public void testRecordPatternTypeInference_004() {
		runConformTest(new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				interface I {int a();}
				record RecB(int a) implements I {}
				record R<T>(T a) {}
				public class X {
				    private static boolean test(List<R<? extends I>> list) {
				        if (list.get(0) instanceof R(var a))
				         return a.a() > 0;
				        return false;
				    } \s
				    public static void main(String... args) {
				        List<R<? extends I>> list = new ArrayList<>();
				        list.add(new R<>(new RecB(2)));
				        System.out.println(test(list));
				    }
				}"""
			},
			"true");
	}
	public void testRecordPatternTypeInference_005() {
		runConformTest(new String[] {
			"X.java",
			"""
				interface I {int a();}
				record RecB(int a) implements I {}
				record R<T>(T a) {}
				public class X {
				    private static boolean test(R<? extends I> op) {
				        if (op instanceof R(var a)) {
				         return a.a() > 0;
				        }
				        return false;
				    } \s
				    public static void main(String[] args) {
				        R<? extends I> op = new R<>(new RecB(2));
				        System.out.println(test(op));
				    }
				}"""
			},
			"true");
	}
	public void testRecordPatternTypeInference_006() {
		runConformTest(new String[] {
			"X.java",
			"""
				public class X {
				     public static <P> boolean test(P p) {
				         if (p instanceof R(var a)) {
				              return a.len() > 0;
				         }
				         return false;
				     }
				     public static void main(String argv[]) {
				         System.out.println(test(new R<>(new Y())));
				     }
				}
				record R<T extends Y>(T a) {}
				class Y {
				 public int len() { return 10;}
				}"""
			},
			"true");
	}
	public void testRecordPatternTypeInference_007() {
		runConformTest(new String[] {
			"X.java",
			"""
				interface I {
				   int a();
				}
				record R<T>(T a) {}
				public class X {
				    public static boolean test(R<?> p) {
				        if (p instanceof R(var a)) {
				             return a instanceof I;
				        }
				        return false;\s
				    }
				    public static void main(String argv[]) {
				       System.out.println(test(new R<>((I) () -> 0)));
				    }
				}"""
			},
			"true");
	}
	public void testRecordPatternTypeInference_008() {
		runConformTest(new String[] {
			"X.java",
			"""
				interface I {int a();}
				record R<T>(T a) {}
				public class X {
				    public static boolean test(R<I> p) {
				        return switch (p) {
				            case R(var a) -> a instanceof I;
				            default ->  false;
				        };
				    }
				    public static void main(String argv[]) {
				       System.out.println(test(new R<>((I) () -> 0)));
				    }
				}"""
			},
			"true");
	}
	public void testRecordPatternTypeInference_009() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					interface I {
					   int a();
					}
					record R<T>(T a) {}
					public class X {
					    private static boolean test(R<? extends I> p) {
					        if (p instanceof R(String a)) {
					             return a instanceof String;
					        }
					        return true;
					    }
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>((I) () -> 0)));\s
					    }
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						if (p instanceof R(String a)) {
						                   ^^^^^^^^
					Record component with type capture#2-of ? extends I is not compatible with type java.lang.String
					----------
					""");
	}
	public void testRecordPatternTypeInference_010() {
		runConformTest(new String[] {
				"X.java",
				"""
					interface I {
					   int a();
					}
					record R<T>(T a) {}
					public class X {
					    private static boolean test(R<?> p) {
					        if (p instanceof R(String a)) {
					             return a instanceof String;
					        }
					        return true;
					    }
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>((I) () -> 0)));\s
					    }
					}"""
				},
				"true");
	}
	public void testRecordPatternTypeInference_011() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					interface I {
					   int a();
					}
					
					record R<T>(T a) {}
					
					public class X {
					
					    private static boolean test(R<? extends I> p) {
					        if (p instanceof R<>(String a)) {
					             return a instanceof String;
					        }
					        return true;
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>((I) () -> 0)));\s
					    }
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						if (p instanceof R<>(String a)) {
						    ^^^^^^^^^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types R<capture#1-of ? extends I> and R
					----------
					""");
	}
	public void testIssue900_1() {
		runConformTest(new String[] {
				"X.java",
				"""
					class X {
						record Box<T>(T t) {}
						// no issues
						static void test1(Box<String> bo) {
							if (bo instanceof Box<String>(var s)) {
								System.out.println("String " + s);
							}
						}
						// no issues
						static void test2(Box<String> bo) {
						    if (bo instanceof Box(var s)) {    // Inferred to be Box<String>(var s)
						        System.out.println("String " + s);
						    }
						}
						// "Errors occurred during the build": "info cannot be null"
						static void test3(Box<Box<String>> bo) {
						    if (bo instanceof Box<Box<String>>(Box(var s))) {       \s
						        System.out.println("String " + s.getClass().toString());
						    }
						}   \s
						// "Errors occurred during the build": "info cannot be null"
						static void test4(Box<Box<String>> bo) {
						    if (bo instanceof Box(Box(var s))) {   \s
						        System.out.println("String " + s);
						    }
						}
						public static void main(String[] args) {
							Box<Box<String>> bo = new Box(new Box(""));
							test3(bo);
						}
					}"""
				},
				"String class java.lang.String");
	}
	// The following code is accepted by ECJ, but it should really reject the code
	// at Box(String s1, String s2)
	public void _testIssue900_2() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					class X {
						record Box<T, U>(T t1, U t2) {}
						static void test3(Box<Box<String, Integer>, Box<Integer, String>> bo) {
						    if (bo instanceof Box<Box<String, Integer>, Box<Integer, String>>(Box(String s1, String s2), Box b1)) {       \s
						        System.out.println("String " + s1.getClass().toString());
						    }
						}   \s
						public static void main(String[] args) {
							Box<Box<String, Integer>, Box<Integer, String>> bo = new Box(new Box("", Integer.valueOf(0)), new Box(Integer.valueOf(0), "")); \s
							test3(bo);
						}
					}"""
				},
				"");
	}
	public void testIssue900_3() {
		Map<String,String> options = getCompilerOptions(false);
		String old1 = options.get(CompilerOptions.OPTION_ReportRawTypeReference);
		String old2 = options.get(CompilerOptions.OPTION_ReportUncheckedTypeOperation);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
		try {
			runNegativeTest(new String[] {
					"X.java",
					"""
						class X {
							record Box<T, U>(T t, U u) {}
							static void test3(Box<Box<String>> bo) {
							    if (bo instanceof Box<Box<String>>(Box(var s1, String s2), Box b1)) {       \s
							        System.out.println("String " + s1.getClass().toString());
							    }
							}   \s
							public static void main(String[] args) {
								Box<Box<String, Integer>, Box<Integer, String>> bo = new Box(new Box("", Integer.valueOf(0)), new Box(Integer.valueOf(0), ""));
								test3(bo);
							}
						}"""
			},
				"""
					----------
					1. ERROR in X.java (at line 3)
						static void test3(Box<Box<String>> bo) {
						                      ^^^
					Incorrect number of arguments for type X.Box<T,U>; it cannot be parameterized with arguments <String>
					----------
					2. ERROR in X.java (at line 4)
						if (bo instanceof Box<Box<String>>(Box(var s1, String s2), Box b1)) {       \s
						                      ^^^
					Incorrect number of arguments for type X.Box<T,U>; it cannot be parameterized with arguments <String>
					----------
					3. ERROR in X.java (at line 10)
						test3(bo);
						^^^^^
					The method test3(X.Box<X.Box<String,Integer>,X.Box<Integer,String>>) is undefined for the type X
					----------
					""",
				"",
				null,
				false,
				options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportRawTypeReference, old1);
			options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, old2);
		}
	}
	public void testRecordPatternMatchException_001() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X  {
					
					    public record R(int x) {
					        public int x() {
					         return x < 10 ? 10/x : x;
					        }
					    }
					
					    @SuppressWarnings("preview")
					 private static int foo(Object o) {
					        int ret = -1;
					        try {
					            if (o instanceof R(int x)) {
					                ret = 100;
					            }
					        } catch (MatchException e) {
					            ret += 100;
					        }
					          return ret;
					    }\s
					    public static void main(String argv[]) {\s
					        System.out.println(X.foo(new R(0)));\s
					    }\s
					}"""
				},
				"99");
	}
	public void testRecordPatternMatchException_001_1() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X  {
					
					    public record R(int x) {
					        public int x() {
					         return x < 10 ? 10/x : x;
					        }
					    }
					
					    @SuppressWarnings("preview")
					 private static int foo(Object o) {
					        int ret = -1;
					        try {
					            if (o instanceof R(int x)) {
					                ret = 100;
					            }
					            return 10;
					        } catch (MatchException e) {
					            ret += 100;
					        }
					          return ret;
					    }\s
					    public static void main(String argv[]) {\s
					        System.out.println(X.foo(new R(0)));\s
					    }\s
					}"""
				},
				"99");
	}
	public void testRecordPatternMatchException_002() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					
					 public record R1(int x) {
					 }
					
					 @SuppressWarnings("preview")
					 public static int bar(Object o) {
					   int res = 100;
					   if (o instanceof R1(int x)) {
					     res = x;
					   }
					     return res;\s
					 }           \s
					
					 public static void main(String argv[]) {
					   R1 r = new R1(0);
					   int result = bar(r);  \s
					   System.out.println(result); \s
					 }
					}"""
				},
				"0");
	}
	public void testRecordPatternMatchException_003() {
		runConformTest(new String[] {
				"X.java",
				"""
					record R(int x) {}
					
					public class X {
					
					 @SuppressWarnings("preview")
					 private int foo(Object o) {
					   int ret = 10;
					   try {
					     if (o instanceof R(int x)) {
					       ret = x;
					     }
					   } catch (MatchException e) {
					     ret = -1;
					   }
					   return ret;
					 }
					
					 public static void main(String argv[]) {
					   int res = new X().foo(new R(100));
					   System.out.println(res);
					 }
					}"""
				},
				"100");
	}
	public void testRecordPatternMatchException_004() {
		runConformTest(new String[] {
				"X.java",
				"""
					record R(int x) {}
					
					public class X {
					
					 @SuppressWarnings("preview")
					 private static int foo(Object o) {
					   int ret = 10;
					   try {
					     if (o instanceof R(int x)) {
					       ret = x;
					     }
					   } catch (MatchException e) {
					     ret = -1;
					   }
					   return ret;
					 }
					
					 public static void main(String argv[]) {
					   int res = foo(new R(100));
					   System.out.println(res);
					 }
					}"""
				},
				"100");
	}
	public void testRecordPatternMatchException_005() {
		runConformTest(new String[] {
				"X.java",
				"""
					 public class X {
					
					 public record R1(int x) {}
					
					 public record R2(int x) {}
					
					 public static void main(String argv[]) {
					   R1 r = new R1(0);
					   try {
					     if (r instanceof R1(int x)) {
					       System.out.println("matched");
					     }
					   } catch (MatchException e) {
					     System.out.println("caught exception");
					   }
					
					   if (r instanceof R1(int x)) {
					     System.out.println("hello    ");
					   }
					
					   System.out.println("done");
					 }
					}"""
				},
				"""
					matched
					hello   \s
					done""");
	}
	public void testRecordPatternMatchException_006() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					
					 public record R1(int x) {
					   public int x(){
					     return x < 10 ? 10 / x : x;
					   }
					 }
					 public record R2(R1 r1) {
					  \s
					 }
					
					 @SuppressWarnings("preview")
					 public static int bar(Object o) {
					   int res = 100;
					   if (o instanceof R2(R1 r1)) {
					     res = r1.x();
					   }
					   System.out.print(false);
					     return res;\s
					 }           \s
					
					 public static void main(String argv[]) {
					   R1 r = new R1(0);
					   int result = bar(r);  \s
					   System.out.println(result); \s
					 }     \s
					}"""
				},
				"false100");
	}
	public void testRecordPatternMatchException_007() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					
					 public record R1(int x) {
					 }
					 public record R2(R1 r1) {
					  \s
					 }
					
					 @SuppressWarnings("preview")
					 public static int bar(Object o) {
					   int res = 100;
					   if (o instanceof R2(R1 r1)) {
					     res = r1.x();
					   }
					     return res;\s
					 }
					
					 public static void main(String argv[]) {
					   R1 r = new R1(0);
					   int result = bar(r);  \s
					   System.out.println(result); \s
					 }     \s
					}"""
				},
				"100");
	}
	public void testRecordPatternMatchException_008() {
		runConformTest(new String[] {
				"X.java",
				"""
					record R(Integer a) {
					    static R newRecord() {
					        return new R(5);
					    }
					}
					
					public class X  {
					
					    @SuppressWarnings("preview")
					       private int test(Object o) {
					        int ret = 0;
					        try {
					            switch (o) {
					                case R(Integer a) -> ret =  a;
					                default -> ret =  8;
					            }
					        } catch (MatchException ex) {
					            ret = -1;
					        }
					        return ret;
					    }\s
					
					    public static void main(String argv[]) {
					        X test = new X();
					        int res = test.test(R.newRecord());
					        System.out.println(res);
					}\s
					}"""
				},
				"5");
	}
	public void testRecordPatternMatchException_009() {
		runConformTest(new String[] {
				"X.java",
				"""
					record R(Y s) {}
					class Y{}
					public class X  extends Y{
					
					    @SuppressWarnings({ "preview", "unused" })
					 public boolean foo(R r) {
					        boolean ret = false; // keep this unused variable to see the error.\s
					        switch (r) {
					            case R(X s) : {
					             return true;
					            }
					            default : {
					                return false;
					            }
					        }
					    } \s
					
					    public static void main(String argv[]) {
					        X x = new X();
					        System.out.println(x.foo(new R(x)));
					    }
					}"""

				},
				"true");
	}
	public void testIssue1224_1() {
		runNegativeTest(new String[] {
			"X.java",
			"""
				interface I {}
				class Class implements I {}
				record Record(I s) {}
				public class X {
				 @SuppressWarnings("preview")
				    public static void foo(Record exp) {
				        switch (exp) {
				            case Record(Class s) -> {break;}
				        }
				    }
				}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					switch (exp) {
					        ^^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testIssue1224_2() {
		runConformTest(new String[] {
			"X.java",
			"""
				interface I {}
				class Class implements I {}
				record Record(I s) {}
				public class X {
				 @SuppressWarnings("preview")
				  public static void foo(Record exp) {
				    switch (exp) {
				      case Record(I s) -> {break;}
				    }
				  }
					public static void main(String[] args) {
						foo(new Record(new Class()));
					}
				}"""
			},
			"");
	}
	public void testIssue1224_3() {
		runNegativeTest(new String[] {
			"X.java",
			"""
				interface I {}
				record Record(long l) {}
				public class X {
				 @SuppressWarnings("preview")
				    public void foo(Record exp) {
				        switch (exp) {
				            case Record(int i) -> {break;}
				        }
				    }
				}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					switch (exp) {
					        ^^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				2. ERROR in X.java (at line 7)
					case Record(int i) -> {break;}
					            ^^^^^
				Record component with type long is not compatible with type int
				----------
				""");
	}
	public void testIssue1224_4() {
		runNegativeTest(new String[] {
			"X.java",
			"""
				record Record<T>(Object o, T x){}
				public class X {
				 @SuppressWarnings("preview")
				    public static void foo(Record<String> rec) {
				        switch (rec) {
				            case Record<String>(Object o, StringBuilder s) -> {break;}
				        }
				    }
				}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					switch (rec) {
					        ^^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				2. ERROR in X.java (at line 6)
					case Record<String>(Object o, StringBuilder s) -> {break;}
					                              ^^^^^^^^^^^^^^^
				Record component with type String is not compatible with type java.lang.StringBuilder
				----------
				""");
	}
	public void testIssue1224_5() {
		runConformTest(new String[] {
			"X.java",
			"""
				record Record<T>(Object o, T x){}
				public class X {
				 @SuppressWarnings("preview")
				    public static void foo(Record<String> rec) {
				        switch (rec) {
				            case Record<String>(Object o, String s) -> {\
				                System.out.println(s);\
				                break;\
				            }
				        }
				    }
					public static void main(String[] args) {
						foo(new Record<String>(args, "PASS"));
					}
				}"""
			},
			"PASS");
	}
	public void testIssue1224_6() {
		runConformTest(new String[] {
			"X.java",
			"""
				record Record(String s){}
				public class X {
				 @SuppressWarnings("preview")
				    public static void foo(Record rec) {
				        switch (rec) {
				            case Record(String s) when true -> {\
				                System.out.println(s);\
				                break;\
				            }
				            default -> {}\
				        }
				    }
					public static void main(String[] args) {
						foo(new Record("PASS"));
					}
				}"""
			},
				"PASS");
	}
	public void testIssue1224_7() {
		runConformTest(new String[] {
			"X.java",
			"""
				interface I<T> {
				    T a();
				}
				record Record<T>(T a, T b) implements I<T> {}
				public class X {
					public static void main(String[] args) {
						foo(new Record(2, 3));
					}
					static void foo(I i) {
				        int res = 0;
				        switch (i) {
				            case Record(Integer a, Integer b) -> {
				                res = a + b;
				                break;
				            }
				            default -> {
				                res = 0;
				                break;
				            }
				        }
						System.out.println(res);
				    }
				}"""
			},
				"5");
	}
	// Fails with VerifyError since we allow the switch now but don't
	// generate a label/action for implicit default.
	public void testIssue1224_8() {
		runConformTest(new String[] {
			"X.java",
			"""
				record Record(int a) {}
				public class X {
				 @SuppressWarnings("preview")
				  public boolean foo(Record rec) {
				        boolean res = switch (rec) {
				            case Record(int a) : {
				                yield a == 0;\s
				            }
				        };
				        return res;
				    }
				    public static void main(String argv[]) {
				        X t = new X();
				        if (t.foo(new Record(0))) {
				            System.out.println("SUCCESS");
				            return;
				        }
				        System.out.println("FAIL");
				    }
				}"""
			},
			"SUCCESS");
	}
	public void testRecPatExhaust001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					final class C implements I {}
					record Box(I i) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 public static int testExhaustiveRecordPatterns(Box box) {
					     return switch (box) {     // Exhaustive!
					         case Box(A a) -> 0;
					         case Box(B b) -> 1;
					         case Box(C c) -> 2;
					    };
					 }\s
					\s
					    public static void main(String argv[]) {
					     Box b = new Box(new A());
					        System.out.println(testExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"0");
	}
	public void testRecPatExhaust002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					final class C implements I {}
					record Box(I i) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 public static int testExhaustiveRecordPatterns(Box box) {
					     return switch (box) {     // Not Exhaustive!
					         case Box(A a) -> 0;
					         case Box(B b) -> 1;
					    };
					 }\s
					\s
					    public static void main(String argv[]) {
					     Box b = new Box(new A());
					        System.out.println(testExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (box) {     // Not Exhaustive!
					               ^^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					final class C implements I {}
					sealed interface J permits D, E, F {}
					final class D   implements J {}
					final class E   implements J {}
					final class F implements J {}
					record Box(I i, J j) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 public static int testExhaustiveRecordPatterns(Box box) {
					     return switch (box) {     // Not Exhaustive!
					         case Box(A a, D d) -> 0;
					         case Box(A a, E e) -> 10;
					         case Box(A a, F f) -> 20;
					         case Box(B b, D d) -> 1;
					         case Box(B b, E e) -> 11;
					         case Box(B b, F f) -> 21;
					         case Box(C c, D d) -> 2;
					         case Box(C c, E e) -> 12;
					         case Box(C c, F f) -> 22;
					    };
					 }\s
					\s
					    public static void main(String argv[]) {
					     Box b = new Box(new A(), new D());
					        System.out.println(testExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"0");
	}
	public void testRecPatExhaust004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					final class C implements I {}
					sealed interface J permits D, E, F {}
					final class D   implements J {}
					final class E   implements J {}
					final class F implements J {}
					record Box(I i, J j) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 public static int testExhaustiveRecordPatterns(Box box) {
					     return switch (box) {     // Not Exhaustive!
					         case Box(A a, D d) -> 0;
					         case Box(A a, E e) -> 0;
					         case Box(A a, F f) -> 0;
					         case Box(B b, D d) -> 1;
					         case Box(B b, E e) -> 1;
					         case Box(B b, F f) -> 1;
					         case Box(C c, D d) -> 2;
					         case Box(C c, F f) -> 2;
					    };
					 }\s
					\s
					    public static void main(String argv[]) {
					     Box b = new Box(new A(), new D());
					        System.out.println(testExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 16)
					return switch (box) {     // Not Exhaustive!
					               ^^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust005() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					record C(int j) implements I {}  // Implicitly final
					record Box(I i) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 public static int testExhaustiveRecordPatterns(Box box) {
					     return switch (box) {     // Exhaustive!
					         case Box(A a) -> 0;
					         case Box(B b) -> 1;
					         case Box(C c) -> 2;
					    };
					 }\s
					\s
					    public static void main(String argv[]) {
					     Box b = new Box(new A());
					        System.out.println(testExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"0");
	}
	public void testRecPatExhaust006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					record C(int j) implements I {}  // Implicitly final
					record Box(I i) {}
					
					public class X {
					
					 @SuppressWarnings("preview")
					 public static int testExhaustiveRecordPatterns(Box box) {
					     return switch (box) {     // Not Exhaustive!
					         case Box(A a) -> 0;
					         case Box(B b) -> 1;
					    };
					 }\s
					\s
					    public static void main(String argv[]) {
					     Box b = new Box(new A());
					        System.out.println(testExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					return switch (box) {     // Not Exhaustive!
					               ^^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					record C(int j) implements I {}  // Implicitly final
					record R(I i, I j) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 private static int testNonExhaustiveRecordPatterns(R p) {
					     return switch (p) {     // Not Exhaustive!
					         case R(A a1, A a2) -> 0;
					         case R(B b1, B b2) -> 1;
					    };
					 }\s
					    public static void main(String argv[]) {
					     R b = new R(new A(), new B());
					        System.out.println(testNonExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (p) {     // Not Exhaustive!
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B, C {}
					final class A   implements I {}
					final class B   implements I {}
					record C(int j) implements I {}  // Implicitly final
					record R(I i, I j) {}
					
					
					public class X {
					
					 @SuppressWarnings("preview")
					 private static int testNonExhaustiveRecordPatterns(R p) {
					     return switch (p) {     // Not Exhaustive!
					         case R(A a1, A a2) -> 0;
					         case R(B b1, B b2) -> 1;
					         case R(C c1, C c2) -> 2;
					    };
					 }\s
					    public static void main(String argv[]) {
					     R b = new R(new A(), new B());
					        System.out.println(testNonExhaustiveRecordPatterns(b));
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (p) {     // Not Exhaustive!
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust009() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					record Test<T>(Object o, T x) {}
					
					public class X {
					 @SuppressWarnings("preview")
					 static int testExhaustiveRecordPattern(Test<String> r) {
					   return switch (r) { // Exhaustive!
					   case Test<String>(Object o, String s) -> 0;
					   };
					 }
					
					 public static void main(String[] args) {
					   System.out.println(testExhaustiveRecordPattern(new Test<String>(args, null)));
					 }
					}"""
			},
			"0");
	}
	public void testRecPatExhaust010() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					record R(Object t, Object u) {}
					
					public class X {
					
					    public static int foo(R r) {
					        return\s
					          switch (r) {
					            case R(String x, Integer y) -> 1;
					            case R(Object x, Integer y) -> 2;
					            case R(Object x, Object y) -> 42;
					        };
					    }
					
					    public static void main(String argv[]) {
					     System.out.println(foo(new R(new String(), new Object())));
					    }
					}"""
			},
			"42");
	}
	// implicit permitted - interface
	public void testRecPatExhaust011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {}
					
					final class A implements I {}
					final class B implements I {}
					
					record R(I d) {}
					
					public class X {
					
					    @SuppressWarnings("preview")
					       public static int foo(R r) {
					        return switch (r) {
					            case R(A x) -> 1;
					        };
					    }\s
					
					    public static void main(String argv[]) {
					       System.out.println(X.foo(new R(new A())));
					    }\s
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (r) {
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	// implicit permitted - class
	public void testRecPatExhaust012() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed class C {}
					
					final class A extends C {}
					final class B extends C {}
					
					record R(C c) {}
					
					public class X {
					
					    @SuppressWarnings("preview")
					       public static int foo(R r) {
					        return switch (r) {
					            case R(A x) -> 1;
					        };
					    }\s
					
					    public static void main(String argv[]) {
					       System.out.println(X.foo(new R(new A())));
					    }\s
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (r) {
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	// implicit permitted - class - the class C missing
	public void testRecPatExhaust013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed class C {}
					
					final class A extends C {}
					final class B extends C {}
					
					record R(C c) {}
					
					public class X {
					
					    @SuppressWarnings("preview")
					       public static int foo(R r) {
					        return switch (r) {
					            case R(A x) -> 1;
					            case R(B x) -> 1;
					        };
					    }\s
					
					    public static void main(String argv[]) {
					       System.out.println(X.foo(new R(new A())));
					    }\s
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (r) {
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust014() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {}
					sealed class C {}
					final class A extends C implements I {}
					final class B extends C implements I {}
					
					record R(C c, I i){}
					public class X {\s
					
					 @SuppressWarnings("preview")
					 public static int foo(R r) {
					       return switch (r) {
					            case R(A x, A y) -> 1;
					            case R(A x, B y) -> 42;
					            case R(B x, A y)-> 3;
					            case R(B x, B y)-> 4;
					        };
					    }
					    public static void main(String argv[]) {
					     System.out.println(X.foo(new R(new A(), new B())));
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					return switch (r) {
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecPatExhaust015() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I {}
					sealed class C {}
					final class A extends C implements I {}
					final class B extends C implements I {}
					
					record R(C c, I i){}
					public class X {\s
					
					    @SuppressWarnings("preview")
					 public static int foo(R r) {
					       return switch (r) {
					            case R(A x, A y) -> 1;
					            case R(A x, B y) -> 42;
					            case R(B x, A y) -> 3;
					            case R(B x, B y) -> 4;
					            case R(C x, A y) -> 5;
					            case R(C x, B y) -> 6;
					       };
					    }
					    public static void main(String argv[]) {
					     System.out.println(X.foo(new R(new A(), new B())));
					    }
					}""",
			},
			"42");
	}
	public void testRecPatExhaust016() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed abstract class C permits A, B {}
					final class A extends C {}
					final class B extends C {}
					record R(C x, A y) {}
					
					public class X {
					    public static int foo(R r) {
					        return switch (r) {
					            case R(A x, A y) -> 42;
					            case R(B y, A x) -> 2;
					        };
					    }
					
					    public static void main(String argv[]) {
					       System.out.println(X.foo(new R(new A(), new A())));
					    }
					}""",
			},
			"42");
	}
	public void testRecPatExhaust017() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B {}
					sealed interface B extends I {}
					
					final class A implements I {}
					
					record R1() implements B {}
					record R2(I i) {}
					
					public class X {
					
					    public static int foo(R2 r) {
					        return switch (r) {
					            case R2(A a) -> 42;
					            case R2(B a) -> 1;
					        };
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(X.foo(new R2(new A())));;
					    }
					
					}""",
			},
			"42");
	}
	public void testRecPatExhaust018() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A, B {}
					sealed interface B extends I {}
					
					final class A implements I {}
					
					record R1() implements B {}
					record R2(I i) {}
					
					public class X {
					
					    public static int foo(R2 r) {
					        return switch (r) {
					            case R2(A a) -> 42;
					        };
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(X.foo(new R2(new A())));;
					    }
					
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return switch (r) {
					               ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testRecordPatternTypeInference_012() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					record R<T>(T t) {}
					
					public final class X implements I {
					
					    private static boolean test(R<? extends I> r) {
					        if (r instanceof R(X x)) {
					             return (x instanceof X);
					        }
					        return true;
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>(null)));
					    }
					}""",
			},
			"true");
	}
	public void testRecordPatternTypeInference_013() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					record R<T>(T t) {}
					
					public class X implements I {
					
					    private static boolean test(R<? extends I> r) {
					        if (r instanceof R(X x)) {
					             return (x instanceof X);
					        }
					        return true;
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>(null)));
					    }
					}""",
			},
			"true");
	}

	// a subclass of X could implement I - positive test case
	public void testRecordPatternTypeInference_014() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					record R<T>(T t) {}
					
					public class X {
					
					    private static boolean test(R<? extends I> r) {
					        if (r instanceof R(X x)) {
					             return (x instanceof X);
					        }
					        return true;
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>(null)));
					    }
					}""",
			},
			"true");
	}
	public void testRecordPatternTypeInference_015() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					record R<T>(T t) {}
					
					public final class X {
					
					    private static boolean test(R<? extends I> r) {
					        if (r instanceof R(X x)) {
					             return (x instanceof X);
					        }
					        return true;
					    }
					
					    public static void main(String argv[]) {
					        System.out.println(test(new R<>(null)));
					        Zork();
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					if (r instanceof R(X x)) {
					                   ^^^
				Record component with type capture#2-of ? extends I is not compatible with type X
				----------
				2. ERROR in X.java (at line 15)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testRecordPatternTypeInference_016() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					record R<T>(T t) {}
					
					public final class X {
					
					    private static boolean bar(R<? extends I> r) {
					       return switch(r) {
					               case R(X x) -> false;
					               default -> true;
					       };
					    }\s
					
					    public static void main(String argv[]) {
					        System.out.println(bar(new R<>(null)));
					        Zork();
					    }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					case R(X x) -> false;
					       ^^^
				Record component with type capture#2-of ? extends I is not compatible with type X
				----------
				2. ERROR in X.java (at line 15)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testIssue1328_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public int foo(Object o) {
						        return switch (o) {
						            case String s when false -> 1;
						            case String s when true != true -> 2;
						            case String s when false == true -> 3;
						            case String s when 0 != 0 -> 3;
						            default -> 0;
						        };
						    }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case String s when false -> 1;
						                   ^^^^^
					A case label guard cannot have a constant expression with value as \'false\'
					----------
					2. WARNING in X.java (at line 5)
						case String s when true != true -> 2;
						                   ^^^^^^^^^^^^
					Comparing identical expressions
					----------
					3. ERROR in X.java (at line 5)
						case String s when true != true -> 2;
						                   ^^^^^^^^^^^^
					A case label guard cannot have a constant expression with value as \'false\'
					----------
					4. ERROR in X.java (at line 6)
						case String s when false == true -> 3;
						                   ^^^^^^^^^^^^^
					A case label guard cannot have a constant expression with value as \'false\'
					----------
					5. WARNING in X.java (at line 7)
						case String s when 0 != 0 -> 3;
						                   ^^^^^^
					Comparing identical expressions
					----------
					6. ERROR in X.java (at line 7)
						case String s when 0 != 0 -> 3;
						                   ^^^^^^
					A case label guard cannot have a constant expression with value as \'false\'
					----------
					""");
	}
	public void testIssue1328_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public int foo(Character c) {
						        int result = 0;
						        switch (c) {
						            case Character p when p.equals("c") -> {
						                result = 6;
						            }
						        };
						        return result;
						    }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						switch (c) {
						        ^
					An enhanced switch statement should be exhaustive; a default label expected
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1792
	// [Patterns][records] Error in JDT Core during AST creation: info cannot be null
	public void testGH1792() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				    private record R(Object o) {}
				    static void f(R [] rs) {
				        int i = 0;
				        while (rs[i++] instanceof R(String o)) {
				            System.out.println(o);
				        }
				    }
				    public static void main(String [] args) {
				    	R [] rs = { new R("So"), new R("far"), new R("so"), new R("good!"), null };
				    	f(rs);
				    }
				}
				"""
				},
				"""
					So
					far
					so
					good!""");
	}
	public void testIssue1336_1() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					record R<T> ( T t) {}
					public final class X {
					private static boolean foo(R<?> r) {
						if (r instanceof R(String s)) {
							return true;
						}
						return false;
					}

					public static void main(String argv[]) {
						System.out.println(foo(new R<>(new String("hello"))));
					}

					}
					"""
			});
	}
	public void testIssue1336_2() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					sealed interface I<TI> {}
					sealed interface J<TJ> {}
					class A {}
					record R<TR>(TR t) implements I<TR>, J<TR>{}

					public class X<TX extends I<? extends A> & J<? extends A>> {

						public boolean foo(TX t) {
							return switch(t) {
								case R(A a) -> true;
								default -> false;
							};
						}

						public static void main(String argv[]) {
						   System.out.println(new X<R<? extends A>>().foo(new R<>(new A())));
						}
					}
					"""
				});
	}
	public void testIssue1732_01() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
				record R(int x, int y) {}

				public class X {

					public int foo(R r) {
						return switch (r) {
							case R() -> 0;
						};
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						return switch (r) {
						               ^
					A switch expression should have a default case
					----------
					2. ERROR in X.java (at line 7)
						case R() -> 0;
						     ^^^
					Record pattern should match the signature of the record declaration
					----------
					""");
	}
	public void testIssue1732_02() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
				record R(int x, int y) {}

				public class X {

					public int foo(R r) {
						return switch (r) {
							case R(int x) -> 0;
						};
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						return switch (r) {
						               ^
					A switch expression should have a default case
					----------
					2. ERROR in X.java (at line 7)
						case R(int x) -> 0;
						     ^^^^^^^^
					Record pattern should match the signature of the record declaration
					----------
					""");
	}
	public void testIssue1732_03() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
				record R(int x, int y) {}

				public class X {

					public int foo(R r) {
						return switch (r) {
							case R(int x, int y, int z) -> 0;
						};
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						return switch (r) {
						               ^
					A switch expression should have a default case
					----------
					2. ERROR in X.java (at line 7)
						case R(int x, int y, int z) -> 0;
						     ^^^^^^^^^^^^^^^^^^^^^^
					Record pattern should match the signature of the record declaration
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1788
	// Inference issue between the diamond syntax and pattern matching (switch on objects)
	public void testGHI1788() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  final static class Entry<K,V> {
				    K key;
				    V value;

				    public Entry(K key, V value) {
				      this.key = key;
				      this.value = value;
				    }

				    public K key() { return key; }
				    public V value() { return value; }
				    public void value(V value) { this.value = value; }
				    @Override
				    public String toString() {
				    	return "Key = " + key + " Value = " + value;
				    }
				  }

				  sealed interface I<V> {}
				  record A<V>(V value) implements I<V> {}
				  record B<V>(V value) implements I<V> {}

				  private static <K, V> Entry<K,V> foo(Entry<K, I<V>> entry) {
				    return new Entry<>(entry.key(), switch (entry.value()) {
				      case A<V>(V value) -> {
				        entry.value(new B<>(value));
				        yield value;
				      }
				      case B<V>(V value) -> value;
				    });
				  }

				  public static void main(String[] args) {
					  Entry<String, I<String>> entry = new Entry<>("KEY", new A<>("VALUE"));
					  System.out.println(entry);
					  System.out.println(foo(entry));
					  System.out.println(entry);
				  }
				}
				"""
				},
				"""
					Key = KEY Value = A[value=VALUE]
					Key = KEY Value = VALUE
					Key = KEY Value = B[value=VALUE]""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1835
	// AssertionError at org.eclipse.jdt.internal.compiler.ast.YieldStatement.addSecretYieldResultValue(YieldStatement.java:120)
	public void testGH1835_minimal() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						try {
							String s = switch (new Object()) {
							case Integer i  -> "i";
							case String y  -> "y";
							default -> {
								try {
									throw new Exception();
								} catch (Exception e) {
									throw new RuntimeException("Expected");
								}
							}
							};
						} catch (RuntimeException e) {
							System.out.print("Caught runtime Exception ");
							if (e.getMessage().equals("Expected"))
								System.out.println ("(expected)");
							else
							 	System.out.println ("(unexpected!!!)");
						}
					}
				}
				"""
				},
				"Caught runtime Exception (expected)");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1835
	// AssertionError at org.eclipse.jdt.internal.compiler.ast.YieldStatement.addSecretYieldResultValue(YieldStatement.java:120)
	public void testGH1835() {
		runConformTest(
				new String[] {
				"Reproducer.java",
				"""
				public class Reproducer {

				    public class DataX {
				        String data = "DataX";
				    }

				    public class DataY {
				        String data1 = "DataY";
				    }

				    record X(DataX data) {}
				    record Y(DataY data) {}

				    Reproducer() {
				        DataX dataX = new DataX();
				        DataY dataY = new DataY();
				        X x = new X(dataX);
				        Y y = new Y(dataY);

				        foo(x);
				        foo(y);
				        foo(null);
				        foo("");
				    }

				    void foo(Object obj) {
				        String s = switch (obj) {
				            case X(var x) when x != null -> x.data;
				            case Y(var x) when x != null -> x.data1;
				            case null, default -> {
				                try {
				                    if (obj == null) yield "switch on null";
				                    throw new Exception();
				                } catch (Exception e) {
				                    yield "default threw exception";
				                }
				            }
				        };
				        System.out.println("s = " + s);
				    }
				    public static void main(String[] args) {
						new Reproducer();
					}

				}
				"""
				},
				"""
					s = DataX
					s = DataY
					s = switch on null
					s = default threw exception""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796
	// [Patterns] Record Patterns can cause VerifyError
	public void testGH1796_full() {
		runConformTest(
				new String[] {
				"com/acme/Reproducer.java",
				"""
				package com.acme;

				import java.util.Objects;

				import com.acme.Reproducer.Expression.ConstantExpression;
				import com.acme.Reproducer.Expression.PlusExpression;
				import com.acme.Reproducer.Expression.TimesExpression;

				public class Reproducer {

				  public static void main(String[] args) {
				    SExpressionPrinter printer = new SExpressionPrinter21Record();
				    Expression twoPlusThree = new PlusExpression(new ConstantExpression(2), new ConstantExpression(3));
				    System.out.println(printer.print(twoPlusThree));
				  }

				  interface SExpressionPrinter {

				    String print(Expression e);

				  }

				  static final class SExpressionPrinter21Record implements SExpressionPrinter {

				    @Override
				    public String print(Expression e) {
				      Objects.requireNonNull(e);
				      StringBuilder buffer = new StringBuilder();
				      printTo(e, buffer);
				      return buffer.toString();
				    }

				    private void printTo(Expression e, StringBuilder buffer) {
				      if (e instanceof ConstantExpression(int i)) {
				        buffer.append(i);
				      } else {
				        buffer.append('(');
				        if (e instanceof PlusExpression(Expression a, Expression b)) {
				          buffer.append("+ ");
				          printTo(a, buffer);
				          buffer.append(' ');
				          printTo(b, buffer);
				        }
				        if (e instanceof TimesExpression(Expression a, Expression b)) {
				          buffer.append("* ");
				          printTo(a, buffer);
				          buffer.append(' ');
				          printTo(b, buffer);
				        }
				        buffer.append(')');
				      }
				    }
				  }

				  sealed interface Expression {

				    int evaluate();

				    record ConstantExpression(int i) implements Expression {

				      @Override
				      public int evaluate() {
				        return i;
				      }

				    }

				    record PlusExpression(Expression a, Expression b) implements Expression {

				      @Override
				      public int evaluate() {
				        return Math.addExact(a.evaluate(), b.evaluate());
				      }

				    }

				    record TimesExpression(Expression a, Expression b) implements Expression {

				      @Override
				      public int evaluate() {
				        return Math.multiplyExact(a.evaluate(), b.evaluate());
				      }

				    }

				  }

				}
				"""
				},
				"(+ 2 3)");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796
	// [Patterns] Record Patterns can cause VerifyError
	public void testGH1796_reporter_reduced() {
		runConformTest(
				new String[] {
				"com/acme/Reproducer.java",
				"""
				package com.acme;

				import com.acme.Reproducer.Expression.ConstantExpression;

				public class Reproducer {

				  public static void main(String[] args) {
				    SExpressionPrinter printer = new SExpressionPrinter();
				    Expression constant = new ConstantExpression(2);
				    System.out.println(printer.print(constant));
				  }

				  static final class SExpressionPrinter {

				    public String print(Expression e) {
				      StringBuilder buffer = new StringBuilder();
				      printTo(e, buffer);
				      return buffer.toString();
				    }

				    private void printTo(Expression e, StringBuilder buffer) {
				      if (e instanceof ConstantExpression(int i)) {
				        buffer.append(i);
				      }
				    }
				  }

				  sealed interface Expression {

				    record ConstantExpression(int i) implements Expression {

				    }

				  }

				}
				"""
				},
				"2");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_method() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							if (new R(0) instanceof R(int i)) {
							}
						} catch (Throwable t) {
							System.out.println("Caught: " + t.getClass().getName());
						}
						if (new R(10) instanceof R(int i)) {

						}
					}
				}
				"""
				},
				"Caught: java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_instance_initializer() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					{
						if (new R(0) instanceof R(int i)) {

						}
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X();
						} catch (MatchException me) {
							System.out.println("Caught MatchException");
						}
					}
				}
				"""
				},
				"Caught MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	// javac reports ArithmeticException but that looks wrong
	public void testGH1977_instance_field() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					boolean b = new R(0) instanceof R(int i);

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X();
						} catch (MatchException me) {
							System.out.println("Caught MatchException");
						}
					}
				}
				"""
				},
				"Caught MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_constructor() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					X() {
						if (new R(0) instanceof R(int i)) {

						}
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X();
						} catch (MatchException me) {
							System.out.println("Caught MatchException");
						}
					}
				}
				"""
				},
				"Caught MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_static_initializer() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					class Y {
						static {
							if (new R(0) instanceof R(int i)) {

							}
						}
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X().new Y();
						} catch (ExceptionInInitializerError me) {
							System.out.println("ExceptionInInitializerError caused by " + me.getCause().getClass().getName());
						}
					}
				}
				"""
				},
				"ExceptionInInitializerError caused by java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	// javac reports ExceptionInInitializerError caused by java.lang.ArithmeticException but that looks wrong
	public void testGH1977_static_field() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					class Y {
						static boolean b = new R(0) instanceof R(int i);
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X().new Y();
						} catch (ExceptionInInitializerError me) {
							System.out.println("ExceptionInInitializerError caused by " + me.getCause().getClass().getName());
						}
					}
				}
				"""
				},
				"ExceptionInInitializerError caused by java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/300
	// Revisit code generation for record patterns
	public void testIssue300() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					record Outer(Middle m1, Middle m2) {

					}

					record Middle (Inner i1, Inner i2) {

					}

					record Inner(String s, Long l, int k) {

					}

					public static void main(String[] args) {
						Outer o = new Outer(new Middle(new Inner("Hello", 11L, 22), new Inner(" World", 22L, 44)), new Middle(new Inner(" How is", 33L, 66), new Inner(" life?", 44L, 88)));
						if (o instanceof Outer(Middle(Inner(String s1, Long l1, int i1), Inner(String s2, Long l2, int i2)), Middle(Inner(String s3, Long l3, int i3), Inner(String s4, Long l4, int i4)))) {
							System.out.println(s1 + s2 + s3 + s4);
							System.out.println(l1 + l2 + l3 + l4);
							System.out.println(i1 + i2 + i3 + i4);
						}
					}
				}
				"""
				},
				"""
					Hello World How is life?
					110
					220""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1804
	// Revisit code generation for record patterns
	public void testIssue1804() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(new Paper(0));
						boolean b = false;
						switch (p) {
							case Box(Paper a) -> {
								b = true;
								break;
							}
							default -> {
								b = false;
								break;
							}
						}
						System.out.println(b);
					}
				}
				"""
				},
				"true");
	}
	public void testIssue1804_0() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(null);
						boolean res = b instanceof Box(Paper a);
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is false");
	}
	public void testIssue1804_1() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box(Paper a);
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	public void testIssue1804_2() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box(Paper a) && a == null;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is false");
	}
	public void testIssue1804_3() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box b = new Box<>(null);
						System.out.println(b instanceof Box(Paper a));
						System.out.println(b instanceof Box(Object a));
					}
				}
				""" }, "false\ntrue");
	}
	public void testIssue1804_4() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(new Paper(0));
						boolean b = false;
						switch (p) {
							case Box(Paper a) -> {
								b = true;
								break;
							}
							default -> {
								b = false;
								break;
							}
						}
						System.out.println(b);
					}
				}
				""" }, "true");
	}
	public void testIssue1804_5() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(null);
						boolean b = false;
						switch (p) {
							case Box(Paper a) -> {
								b = true;
								break;
							}
							default -> {
								b = false;
								break;
							}
						}
						System.out.println(b);
					}
				}
				""" }, "false");
	}
	public void testIssue1804_6() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a, T b) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(new Paper(0), new Paper(1));
						boolean c = false;
						switch (p) {
							case Box(Paper a, Paper b) -> {
								System.out.println(a.color);
								System.out.println(b.color);
								c = true;
								break;
							}
							default -> {
								c = false;
								break;
							}
						}
						System.out.println(c);
					}
				}
				""" }, "0\n1\ntrue");
	}
	public void testIssue1804_7() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box box;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	public void testIssue1804_8() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box(Paper paper) && paper.color != 0;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is false");
	}
	public void testIssue1804_9() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						boolean res = new Box<>(new Paper(0)) instanceof Box(Paper(int c));
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	public void testIssue1804_10() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						boolean res = new Box<>(new Paper(0)) instanceof Box(Paper(int c)) && c == 0;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1985
	// [Patterns][records] ECJ fails to generate code to deconstruct record in pattern
	public void testIssue1985() {
		runConformTest(new String[] { "X.java", """
				public class X {

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							boolean b = new R(0) instanceof R(int i);
						} catch (Throwable t) {
							System.out.println(t.getClass().getName());
						}
					}
				}
				""" },
				"java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1985
	// [Patterns][records] ECJ fails to generate code to deconstruct record in pattern
	public void testIssue1985_2() {
		runConformTest(new String[] { "X.java", """
				public class X {

					boolean b = new R(0) instanceof R(int i);
					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
				            try {
				        	new X();
				            } catch (Throwable t) {
				        	System.out.println(t.getClass().getName());
				            }
					}
				}
				""" },
				"java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2001
	// [Patterns][records] ECJ fails to reject incompatible pattern types.
	public void testIssue2001() {
		runNegativeTest(new String[] { "X.java",
				"""
				public class X {
					record R1(Long color) {}
					record R2(short color) {}

					public static void main(String[] args) {
						Object o = new R1(10L);
						if (o instanceof R1(long d)) {
							System.out.println(d);
						}
						if (o instanceof R2(Short d)) {
							System.out.println(d);
						}
						if (o instanceof R2(int d)) {
							System.out.println(d);
						}
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						if (o instanceof R1(long d)) {
						                    ^^^^^^
					Record component with type Long is not compatible with type long
					----------
					2. ERROR in X.java (at line 10)
						if (o instanceof R2(Short d)) {
						                    ^^^^^^^
					Record component with type short is not compatible with type java.lang.Short
					----------
					3. ERROR in X.java (at line 13)
						if (o instanceof R2(int d)) {
						                    ^^^^^
					Record component with type short is not compatible with type int
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1999
	// [Patterns][records] Instanceof with record deconstruction patterns should never be flagged as unnecessary
	public void testIssue1999() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		runNegativeTest(new String[] {
				"X.java",
				"""
				interface I {
				}

				final class A implements I {
				}

				final class B implements I {
				}

				record R(I x, I y) {
				}

				public class X {
					public static boolean foo(R r) {
						if (r instanceof R(A a1, A a2))  // don't warn here.
							return true;
						A a = null;
						if (a instanceof A) {} // warn here
						if (a instanceof A a1) {} // don't warn here
						return false;
					}

					public static void main(String argv[]) {
						System.out.println(X.foo(new R(new A(), new A())));
						System.out.println(X.foo(new R(new A(), new B())));
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 18)
						if (a instanceof A) {} // warn here
						    ^^^^^^^^^^^^^^
					The expression of type A is already an instance of type A
					----------
					""",
				null, true, options);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				        return (r instanceof R<?>(X x));
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_2() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				         return (r instanceof R<? extends T>(X x));
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_3() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				    	return switch (r) {
				    		case R<?>(X x) -> true;
				    		default -> false;
				    	};
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_4() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				    	return switch (r) {
				    		case R<? extends T>(X x) -> true;
				    		default -> false;
				    	};
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_5() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				    	return switch (r) {
				    		case R<? extends T>(Integer i) -> true;
				    		default -> false;
				    	};
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"false");
	}

	public void testIllegalFallThrough() {
		runNegativeTest(new String[] { "X.java", """
				public class X {
					record Point (int x, int y) {}

				  static void foo(Object o) {
				    switch (o) {
				      case Integer i_1: System.out.println("Integer");
				      case Point(int a, int b) : System.out.println("String");
				      default: System.out.println("Object");
				    }
				  }
				}
				""" },
				"""
					----------
					1. ERROR in X.java (at line 7)
						case Point(int a, int b) : System.out.println("String");
						^^^^^^^^^^^^^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2118
	// [Patterns] ECJ allows illegal modifiers with RecordPattern
	public void testIllegalModifiers() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					record Point (int x, int y) {}

					static void foo(Object o) {
					    if (o instanceof public String) {}   // javac error, ecj error
					    if (o instanceof public String s) {} // javac error, ecj error
					    if (o instanceof public Point(int a, final int b)) {} // javac error, ECJ - NO ERROR!
					    if (o instanceof Point(public int a, final int b)) {} // javac error, ecj error

					    if (o instanceof final String) {}  // javac error, ecj error
					    if (o instanceof final String s) {} // javac NO error, ecj NO error
					    if (o instanceof final Point(int a, int b)) {} // javac NO error, ecj NO error

					    switch (o) {
					      case public Point(int a, int b) : System.out.println("String"); // javac error, ECJ: NO ERROR!
					      case public Object o1: System.out.println("Default"); // both compilers error
					    }
					    switch (o) {
					      case final Point(int a, int b) : System.out.println("String"); // NO ERROR in either
					      case final Object o2: System.out.println("Default");
					    }
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (o instanceof public String) {}   // javac error, ecj error
						                 ^^^^^^^^^^^^^
					Syntax error, modifiers are not allowed here
					----------
					2. ERROR in X.java (at line 6)
						if (o instanceof public String s) {} // javac error, ecj error
						                               ^
					Illegal modifier for the pattern variable s; only final is permitted
					----------
					3. ERROR in X.java (at line 7)
						if (o instanceof public Point(int a, final int b)) {} // javac error, ECJ - NO ERROR!
						                 ^^^^^^
					Syntax error, modifiers are not allowed here
					----------
					4. ERROR in X.java (at line 8)
						if (o instanceof Point(public int a, final int b)) {} // javac error, ecj error
						                                  ^
					Illegal modifier for the pattern variable a; only final is permitted
					----------
					5. ERROR in X.java (at line 10)
						if (o instanceof final String) {}  // javac error, ecj error
						                 ^^^^^^^^^^^^
					Syntax error, modifiers are not allowed here
					----------
					6. ERROR in X.java (at line 12)
						if (o instanceof final Point(int a, int b)) {} // javac NO error, ecj NO error
						                 ^^^^^
					Syntax error, modifiers are not allowed here
					----------
					7. ERROR in X.java (at line 15)
						case public Point(int a, int b) : System.out.println("String"); // javac error, ECJ: NO ERROR!
						     ^^^^^^
					Syntax error, modifiers are not allowed here
					----------
					8. ERROR in X.java (at line 16)
						case public Object o1: System.out.println("Default"); // both compilers error
						                   ^^
					Illegal modifier for the pattern variable o1; only final is permitted
					----------
					9. ERROR in X.java (at line 19)
						case final Point(int a, int b) : System.out.println("String"); // NO ERROR in either
						     ^^^^^
					Syntax error, modifiers are not allowed here
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2119
	// [Patterns] ECJ allows record pattern to have dimensions
	public void testIssue2119() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					record Point (int x, int y) {}

					static void foo(Object o) {
						if (o instanceof Point [](int x, int y)) {}
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (o instanceof Point [](int x, int y)) {}
						                 ^^^^^^^^^^^^^^^^^^^^^^
					A record pattern may not specify dimensions
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2119
	// [Patterns] ECJ allows record pattern to have dimensions
	public void testIssue2119_2() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					record Point (int x, int y) {}

					static void foo(Object o) {
						if (o instanceof Point (int x, int y) []) {}
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (o instanceof Point (int x, int y) []) {}
						                                      ^^
					Syntax error on tokens, delete these tokens
					----------
					""");
	}
}