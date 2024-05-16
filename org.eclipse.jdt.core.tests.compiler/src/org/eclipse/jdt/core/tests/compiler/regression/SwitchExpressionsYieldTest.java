/*******************************************************************************
 * Copyright (c) 2019, 2023 IBM Corporation and others.
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

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;


public class SwitchExpressionsYieldTest extends AbstractRegressionTest {
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "571833" };
	}

	public static Class<?> testClass() {
		return SwitchExpressionsYieldTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public SwitchExpressionsYieldTest(String testName){
		super(testName);
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forRelease(JavaCore.VERSION_14);
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forRelease(JavaCore.VERSION_14));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forRelease(JavaCore.VERSION_14) :
			JavacTestOptions.forRelease(JavaCore.VERSION_14, javacAdditionalTestOptions);
		runner.runWarningTest();
	}
	public void testBug544073_000() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							
								public static int yield() {
									return 1;
								}
								public static int foo(int val) {
									int k = switch (val) {
									case 1 -> { yield 1; }
									default -> { yield 2; }
									};
									return k;
								}
								public static void main(String[] args) {
									System.out.println(X.foo(1));
								}
							}
							"""
				},
				"1");
	}
	public void testBug544073_001() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static int twice(int i) {
									int tw = switch (i) {
										case 0 -> i * 0;
										case 1 -> 2;
										default -> 3;
									};
									return tw;
								}
								public static void main(String[] args) {
									System.out.print(twice(3));
								}
							}
							"""
				},
				"3");
	}
	public void testBug544073_002() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static int twice(int i) throws Exception {
									int tw = switch (i) {
										case 0 -> 0;
										case 1 -> {\s
											System.out.println("do_not_print");
											yield 1;
										}\s
										case 3 -> throw new Exception();
										default -> throw new Exception();
									};
									return tw;
								}
								public static void main(String[] args) {
									try {
									    try {
											System.out.print(twice(3));
										} catch (Exception e) {
											System.out.print("Got Exception - expected");
										}
									} catch (Exception e) {
									System.out.print("Got Exception");
									}
								}
							}
							"""
				},
				"Got Exception - expected");
	}
	public void testBug544073_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    twice(1);
					  }
						public static int twice(int i) {
							int tw = switch (i) {
							};
							return tw;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					int tw = switch (i) {
						};
					         ^^^^^^^^^^^^^^^^
				A switch expression should have a non-empty switch block
				----------
				2. ERROR in X.java (at line 6)
					int tw = switch (i) {
					                 ^
				A switch expression should have a default case
				----------
				""");
	}
	public void testBug544073_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    twice(1);
					  }
						public static int twice(int i) {
							int tw = switch (i) {
								case 0 -> 0;
								case 1 -> {\s
									System.out.println("heel");
									yield 1;
								}\s
								case "hello" -> throw new java.io.IOException("hello");
								default -> throw new java.io.IOException("world");
							};
							return tw;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					case "hello" -> throw new java.io.IOException("hello");
					     ^^^^^^^
				Type mismatch: cannot convert from String to int
				----------
				""");
	}
	public void testBug544073_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    twice(1);
					  }
						public static int twice(int i) {
							int tw = switch (i) {
								case 0 -> 0;
								case 1 -> {\s
									System.out.println("heel");
									yield 1;
								}\s
							    case 2 -> 2;
							};
							return tw;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					int tw = switch (i) {
					                 ^
				A switch expression should have a default case
				----------
				""");
	}
	/**
	 * Add a test case for enum
	 * If the type of the selector expression is an enum type,
	 * then the set of all the case constants associated with the switch block
	 *  must contain all the enum constants of that enum type
	 *  Add a missing enum test case
	 */
	public void testBug544073_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					public class X {
					  public static void main(String[] args) {
					  }
						public static int twice(int i) {
							int tw = switch (i) {
								case 0 -> 0;
								case 1 -> {\s
									System.out.println("heel");
									yield 1;
								}\s
							//	case 2 -> 2;
								case "hello" -> throw new IOException("hello");
							};
							return tw;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					int tw = switch (i) {
					                 ^
				A switch expression should have a default case
				----------
				2. ERROR in X.java (at line 13)
					case "hello" -> throw new IOException("hello");
					     ^^^^^^^
				Type mismatch: cannot convert from String to int
				----------
				""");
	}
	/*
	 * should compile - test for adding additional nesting in variables
	 * dev note: ref consumeToken().case Switch
	 */
	public void testBug544073_007() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static int foo(int i) {
									int tw =\s
									switch (i) {
										case 1 ->\s
										 {
							 				int z = 100;
							 				yield z;
										}
										default -> {
											yield 12;
										}
									};
									return tw;
								}
								public static void main(String[] args) {
									System.out.print(foo(1));
								}
							}
							"""
				},
				"100");
	}
	public void testBug544073_009() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void bar(int  i) {
							switch (i) {
							case 1 -> System.out.println("hello");
							default -> System.out.println("DEFAULT");
							}
						}
						public static void main(String[] args) {
							bar(1);
						}
					}
					"""
			},
			"hello");
	}
	public void testBug544073_010() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						static int twice(int i) {
							switch (i) {
								case 0 -> i * 0;
								case 1 -> 2;
								default -> 3;
							}
							return 0;
						}
						public static void main(String[] args) {
							System.out.print(twice(3));
						}
					}
					""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				case 0 -> i * 0;
				          ^^^^^
			Invalid expression as statement
			----------
			2. ERROR in X.java (at line 5)
				case 1 -> 2;
				          ^
			Invalid expression as statement
			----------
			3. ERROR in X.java (at line 6)
				default -> 3;
				           ^
			Invalid expression as statement
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_011() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						static int twice(int i) {
							switch (i) {
								default -> 3;
							}
							return 0;
						}
						public static void main(String[] args) {
							System.out.print(twice(3));
						}
					}
					""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				default -> 3;
				           ^
			Invalid expression as statement
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_012() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					
						static int twice(int i) {
							switch (i) {
								default -> 3;
							}
							return 0;
						}
						public static void main(String[] args) {
							System.out.print(twice(3));
						}
					}
					""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 5)
				default -> 3;
				           ^
			Invalid expression as statement
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_013() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String release = options.get(CompilerOptions.OPTION_Release);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
		try {
			String[] testFiles = new String[] {
					"X.java",
					"""
						public class X {
							static int twice(int i) {
								switch (i) {
									default -> 3;
								}
								return 0;
							}
							public static void main(String[] args) {
								System.out.print(twice(3));
							}
						}
						""",
			};

			String expectedProblemLog =
					"----------\n" +
					"1. ERROR in X.java (at line 0)\n" +
					"	public class X {\n" +
					"	^\n" +
					"Preview features enabled at an invalid source release level "+CompilerOptions.VERSION_11+", preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
					"----------\n";
			this.runNegativeTest(
					testFiles,
					expectedProblemLog,
					null,
					true,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_Source, release);
		}
	}
	public void testBug544073_014() {
			String[] testFiles = new String[] {
					"X.java",
					"""
						public class X {
							public static int foo(int i) {
								int v;
								int t = switch (i) {
								case 0 : {
									yield 0;
								}
								default :v = 2;
								};
								return t;
							}
						\t
							public boolean bar() {
								return true;
							}
							public static void main(String[] args) {
								System.out.println(foo(3));
							}
						}
						""",
			};

			String expectedProblemLog =
					"""
				----------
				1. ERROR in X.java (at line 8)
					default :v = 2;
					            ^^
				A switch labeled block in a switch expression should not complete normally
				----------
				""";
			this.runNegativeTest(
					testFiles,
					expectedProblemLog);
	}
	public void testBug544073_015() {
		// switch expression is not a Primary
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.testFiles = new String[] {
			"X.java",
			"""
				public class X {
					void test(int i) {
						System.out.println(switch (i) {
							case 1 -> "one";
							default -> null;
						}.toLowerCase());
					}
					public static void main(String[] args) {
						new X().test(1);
					}
				}
				"""
		};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 6)
						}.toLowerCase());
						 ^
					Syntax error on token ".", , expected
					----------
					""";
		runner.runNegativeTest();
	}
	public void testBug544073_016() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static int foo(Day day) {
					
							var len= switch (day) {
								case SUNDAY-> 6;
								default -> 10;
							};
					
							return len;
						}
					
						public static void main(String[] args) {
							System.out.println(foo(Day.SUNDAY));
						}
					}
					enum Day {
						MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
					}
					"""
			},
			"6");
	}
	/*
	 * A simple multi constant case statement, compiled and run as expected
	 */
	public void testBug544073_017() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					public static void bar(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY:\s
								System.out.println(Day.SUNDAY);
								break;
							case MONDAY : System.out.println(Day.MONDAY);
										break;
							}
						}\
						public static void main(String[] args) {
							bar(Day.SATURDAY);
						}
					}
					enum Day { SATURDAY, SUNDAY, MONDAY;}""",
		};

		String expectedProblemLog =
				"SUNDAY";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * A simple multi constant case statement, compiler reports missing enum constants
	 */
	public void testBug544073_018() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
					public static void bar(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY:\s
								System.out.println(Day.SUNDAY);
								break;
							case MONDAY : System.out.println(Day.MONDAY);
										break;
							}
						}\
					}
					enum Day { SATURDAY, SUNDAY, MONDAY, TUESDAY;}""",
		};

		String expectedProblemLog =
						"""
			----------
			1. WARNING in X.java (at line 5)
				switch (day) {
				        ^^^
			The enum constant TUESDAY needs a corresponding case label in this enum switch on Day
			----------
			""";
		this.runWarningTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * A simple multi constant case statement with duplicate enums
	 */
	public void testBug544073_019() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					public static void bar(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY:\s
								System.out.println(Day.SUNDAY);
								break;
							case SUNDAY : System.out.println(Day.SUNDAY);
										break;
							}
						}\
						public static void main(String[] args) {
							bar(Day.SATURDAY);
						}
					}
					enum Day { SATURDAY, SUNDAY, MONDAY;}""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				case SATURDAY, SUNDAY:\s
				^^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 7)
				case SUNDAY : System.out.println(Day.SUNDAY);
				^^^^^^^^^^^
			Duplicate case
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * A simple multi constant case statement with duplicate enums
	 */
	public void testBug544073_020() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					public static void bar(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY:\s
								System.out.println(Day.SUNDAY);
								break;
							case SUNDAY, SATURDAY :\s
								System.out.println(Day.SUNDAY);
								break;
							}
						}\
					}
					enum Day { SATURDAY, SUNDAY, MONDAY;}""",
		};

		String expectedProblemLog =
						"""
			----------
			1. WARNING in X.java (at line 3)
				switch (day) {
				        ^^^
			The enum constant MONDAY needs a corresponding case label in this enum switch on Day
			----------
			2. ERROR in X.java (at line 4)
				case SATURDAY, SUNDAY:\s
				^^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			3. ERROR in X.java (at line 7)
				case SUNDAY, SATURDAY :\s
				^^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			4. ERROR in X.java (at line 7)
				case SUNDAY, SATURDAY :\s
				^^^^^^^^^^^^^^^^^^^^^
			Duplicate case
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 */
	public void testBug544073_021() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					public static void bar(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY:\s
								System.out.println(Day.SUNDAY);
								break;
							case TUESDAY : System.out.println(Day.SUNDAY);
										break;
							}
						}\
						public static void main(String[] args) {
							bar(Day.SATURDAY);
						}
					}
					enum Day { SATURDAY, SUNDAY, MONDAY, TUESDAY;}""",
		};

		String expectedProblemLog =
				"""
			----------
			1. WARNING in X.java (at line 3)
				switch (day) {
				        ^^^
			The enum constant MONDAY needs a corresponding case label in this enum switch on Day
			----------
			""";
		this.runWarningTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_022() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					public static void bar(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY:\s
								System.out.println(day);
								break;
							case MONDAY : System.out.println(0);
										break;
							}
						}\
						public static void main(String[] args) {
							bar(Day.SATURDAY);
							bar(Day.MONDAY);
							bar(Day.SUNDAY);
						}
					}
					enum Day { SATURDAY, SUNDAY, MONDAY;}""",
		};

		String expectedProblemLog =
				"""
			SATURDAY
			0
			SUNDAY""";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Simple switch case with string literals
	 */
	public void testBug544073_023() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							bar("a");
							bar("b");
							bar("c");
							bar("d");
						}
						public static void bar(String s) {
							switch(s) {
							case "a":
							case "b":
								System.out.println("A/B");
								break;
							case "c":
								System.out.println("C");
								break;
							default:
								System.out.println("NA");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			A/B
			A/B
			C
			NA""";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_024() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							bar("a");
							bar("b");
							bar("c");
							bar("d");
						}
						public static void bar(String s) {
							switch(s) {
							case "a", "b":
								System.out.println("A/B");
								break;
							case "c":
								System.out.println("C");
								break;
							default:
								System.out.println("NA");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			A/B
			A/B
			C
			NA""";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch with multi constant case statements with string literals
	 * two string literals with same hashcode
	 */
	public void testBug544073_025() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							bar("FB");
							bar("Ea");
							bar("c");
							bar("D");
						}
						public static void bar(String s) {
							switch(s) {
							case "FB", "c":
								System.out.println("A");
								break;
							case "Ea":
								System.out.println("B");
								break;
							default:
								System.out.println("NA");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			A
			B
			A
			NA""";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch with multi constant case statements with integer constants
	 */
	public void testBug544073_026() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							bar(1);
							bar(2);
							bar(3);
							bar(4);
							bar(5);
						}
						public static void bar(int i) {
							switch (i) {
							case 1, 3:\s
								System.out.println("Odd");
								break;
							case 2, 4:\s
								System.out.println("Even");
								break;
							default:
								System.out.println("Out of range");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			Odd
			Even
			Odd
			Even
			Out of range""";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant with mixed constant types, reported
	 */
	public void testBug544073_027() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public static void bar(int i) {
							switch (i) {
							case 1, 3:\s
								System.out.println("Odd");
								break;
							case "2":\s
								System.out.println("Even");
								break;
							default:
									System.out.println("Out of range");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 9)
				case "2":\s
				     ^^^
			Type mismatch: cannot convert from String to int
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant without break statement, reported
	 */
	public void testBug544073_028() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public static void bar(int i) {
							switch (i) {
							case 1, 3:\s
								System.out.println("Odd");
							case 2, 4:\s
								System.out.println("Even");
								break;
							default:
									System.out.println("Out of range");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. WARNING in X.java (at line 8)
				case 2, 4:\s
				^^^^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			""";
		this.runWarningTest(
				testFiles,
				expectedProblemLog,
				options,
				"-Xlint:fallthrough");
	}
	/*
	 * Switch multi-constant without yield statement, reported
	 */
	public void testBug544073_029() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public static void bar(int i) {
							switch (i) {
							case 1, 3:\s
								System.out.println("Odd");
							case 2, 4:\s
								System.out.println("Even");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. WARNING in X.java (at line 5)
				switch (i) {
				        ^
			The switch statement should have a default case
			----------
			""";
		this.runWarningTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Switch multi-constant with duplicate int constants
	 */
	public void testBug544073_030() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public static void bar(int i) {
							switch (i) {
							case 1, 3:\s
								System.out.println("Odd");
							case 3, 4:\s
								System.out.println("Odd");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 6)
				case 1, 3:\s
				^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 8)
				case 3, 4:\s
				^^^^^^^^^
			Duplicate case
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant with duplicate String literals
	 */
	public void testBug544073_031() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public static void bar(String s) {
							switch (s) {
							case "a", "b":\s
								System.out.println("Odd");
							case "b", "c":\s
								System.out.println("Odd");
							}
						}
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 6)
				case "a", "b":\s
				^^^^^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 8)
				case "b", "c":\s
				^^^^^^^^^^^^^
			Duplicate case
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant with illegal qualified enum constant
	 */
	public void testBug544073_032() {
		if (this.complianceLevel >= ClassFileConstants.JDK21)
			return;
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public static void bar(Num s) {
							switch (s) {
							case ONE, Num.TWO:\s
								System.out.println("Odd");
							}
						}
					}
					enum Num { ONE, TWO}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 6)
				case ONE, Num.TWO:\s
				          ^^^^^^^
			The qualified case label Num.TWO must be replaced with the unqualified enum constant TWO
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_033() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public void bar(int s) {
							int j = switch (s) {
								case 1, 2, 3 -> (s+1);
								default -> j;
							};
						}
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 5)
				default -> j;
				           ^
			The local variable j may not have been initialized
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_034() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
						public void bar(int s) {
							int j = 0;\
							j = switch (s) {
								case 1, 2, 3 -> (s+1);
								default -> j;
							};
						}
					}
					""",
		};
		this.runConformTest(
				testFiles,
				"");
	}
	public void testBug544073_035() {
		// TODO: Fix me
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					
					public class X {
						public static int foo(int i) throws IOException {
							int t = switch (i) {
							case 0 : {
								yield 0;
							}
							case 2 : {
								break;
							}
							default : yield 10;
							};
							return t;
						}
					\t
						public boolean bar() {
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
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					break;
					^^^^^^
				Breaking out of switch expressions not permitted
				----------
				""");
	}
	public void testBug544073_036() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					
						public static void bar(int  i) {
							i = switch (i+0) {
								default: System.out.println(0);
							}; \
						}
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				i = switch (i+0) {
						default: System.out.println(0);
					}; 	}
				    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			A switch expression should have at least one result expression
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_037() {
		String[] testFiles = new String[] {
			"X.java",
			"""
				public class X {
					void test(int i) {
						need(switch (i) {
							case 1 -> "";
							default -> i == 3 ? null : "";
						});\s
					}
					void need(String s) {
						System.out.println(s.toLowerCase());
					}
					public static void main(String[] args) {
						new X().need("Hello World");
					}
				}
				"""
		};
		String expectedOutput = "hello world";
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_038() {
		String[] testFiles = new String[] {
			"X.java",
			"""
				public class X {
					void test(int i) {
						need(switch (i) {
							case 1: yield "";
							default: yield i == 3 ? null : "";
						});\s
					}
					void need(String s) {
						System.out.println(s.toLowerCase());
					}
					public static void main(String[] args) {
						new X().need("Hello World");
					}
				}
				"""
		};
		String expectedOutput = "hello world";
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_039() {
		String[] testFiles = new String[] {
			"X.java",
			"""
				interface I0 { void i(); }
				interface I1 extends I0 {}
				interface I2 extends I0 {}
				public class X {
					I1 n1() { return null; }
					<I extends I2> I n2() { return null; }
					<M> M m(M m) { return m; }
					void test(int i, boolean b) {
						m(switch (i) {
							case 1 -> n1();
							default -> b ? n1() : n2();
						}).i();\s
					}
					public static void main(String[] args) {
						try {
							new X().test(1, true);
						} catch (NullPointerException e) {
							System.out.println("NPE as expected");
						}
					}
				}
				"""
		};
		String expectedOutput = "NPE as expected";
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_040() {
		String[] testFiles = new String[] {
			"X.java",
			"""
				import java.util.function.Supplier;
				interface I0 { void i(); }
				interface I1 extends I0 {}
				interface I2 extends I0 {}
				public class X {
					I1 n1() { return null; }
					<I extends I2> I n2() { return null; }
					<M> M m(Supplier<M> m) { return m.get(); }
					void test(int i, boolean b) {
						m(switch (i) {
							case 1 -> this::n1;
							default -> this::n2;
						}).i();\s
					}
					public static void main(String[] args) {
						try {
							new X().test(1, true);
						} catch (NullPointerException e) {
							System.out.println("NPE as expected");
						}
					}
				}
				"""
		};
		String expectedOutput = "NPE as expected";
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_041() {
		// require resolving/inferring of poly-switch-expression during ASTNode.resolvePolyExpressionArguments()
		String[] testFiles = new String[] {
			"X.java",
			"""
				public class X {
						void test(int i) {
						need(switch (i) {
							case 1 -> 1.0f;
							default -> i == 3 ? 3 : 5.0d;
						});\s
					}
					<N extends Number> void need(N s) {
						System.out.println(s.toString());
					}
					public static void main(String[] args) {
						new X().need(3);
					}
				}
				"""
		};
		String expectedOutput = "3";
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_042() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							static int twice(int i) throws Exception {
								switch (i) {
									case 0 -> System.out.println("hellow");
									case 1 -> foo();
									default -> throw new Exception();
								};
								return 0;
							}
						
							static int foo() {
								System.out.println("inside foo");
								return 1;
							}
						
							public static void main(String[] args) {
								try {
									System.out.print(twice(1));
								} catch (Exception e) {
									System.out.print("Got Exception");
								}
							}
						}"""
			},
			"inside foo\n"
			+ "0");
	}
	public void testBug544073_043() {
		runConformTest(
			new String[] {
					"X.java",
					"enum SomeDays {\n" +
					"	Mon, Wed, Fri\n" +
					"}\n" +
					"\n" +
					"public class X {\n" +
					"	int testEnum(boolean b) {\n" +
					"		SomeDays day = b ? SomeDays.Mon : null;\n" +
					"		return switch(day) {\n" +
					"			case Mon -> 1;\n" +
					"			case Wed -> 2;\n" +
					"			case Fri -> 3;\n" +
					"		};\n" +
					"	}\n" +
					"\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(new X().testEnum(true));\n" +
					"	}\n" +
					"}\n" +
					""
			},
			"1");
	}
	public void testBug544073_044() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						static int foo(int i) {
							switch (i) {
								default -> 3; // should flag an error
							\t
							};
							return 0;
						}
						public static void main(String[] args) {
							foo(1);
						}
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				default -> 3; // should flag an error
				           ^
			Invalid expression as statement
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_045() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public void foo(int i) {
								int j = switch (i) {
									case 1 -> i;
									default -> i;
								};
								System.out.println(j);
							}
						\t
							public static void main(String[] args) {
								new X().foo(1);
							}
						}"""
			},
			"1");
	}
	public void testBug544073_046() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public void foo(int i) {
								long j = switch (i) {
									case 1 -> 10L;
									default -> 20L;
								};
								System.out.println(j);
							}
						\t
							public static void main(String[] args) {
								new X().foo(1);
							}
						}"""
			},
			"10");
	}
	public void testBug544073_047() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public int foo(String s) throws Exception {
								int i = switch (s) {
									case "hello" -> 1;
									default -> throw new Exception();
								};
								return i;
							}
						
							public static void main(String[] argv) {
								try {
									System.out.print(new X().foo("hello"));
								} catch (Exception e) {
									//
								}
							}
						}"""
			},
			"1");
	}
	public void testBug544073_048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public void foo(Day day) {
					    	var today = 1;
					    	today =  switch (day) {
					    		      case SATURDAY,SUNDAY :
					    		         today=1;
					    		         yield today;
					    		      case MONDAY,TUESDAY,WEDNESDAY,THURSDAY :
					    			 today=2;
					    			 yield today;
					    		};
					    }
					    public static void main(String argv[]) {
					    	new X().foo(Day.FRIDAY);
					    }
					}
					
					enum Day {
						SUNDAY,
						MONDAY,
						TUESDAY,
						WEDNESDAY,
						THURSDAY,
						FRIDAY,
						SATURDAY
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					today =  switch (day) {
					                 ^^^
				A Switch expression should cover all possible values
				----------
				""");
	}
	public void testBug544073_049() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public void foo(int i ) {
						        boolean b = switch (i) {
						            case 0 -> i == 1;
						            default -> true;
						        };
						        System.out.println( b ? " true" : "false");
						    }
						    public static void main(String[] argv) {
						    	new X().foo(0);
						    }
						}"""
			},
			"false");
	}
	public void testBug544073_050() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public void foo(String s) {
						        try {
						            int i = switch (s) {
						                case "hello" -> 0;
						                default -> 2;
						            };
						        } finally {
						        	System.out.println(s);
						        }
						    }
						    public static void main(String argv[]) {
						    	new X().foo("hello");
						    }
						}"""
			},
			"hello");
	}
	public void testBug544073_051() {
		Map<String, String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public void foo(String s) {
						        try {
						            int i = switch (s) {
						                case "hello" -> 0;
						                default -> 2;
						            };
						        } finally {
						        	System.out.println(s);
						        }
						    }
						    public static void main(String argv[]) {
						    	new X().foo("hello");
						    }
						}"""
			},
			"hello");
	}
	public void testBug544073_052() {
		Map<String, String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public void foo(String s) {
						        try {
						            long l = switch (s) {
						                case "hello" -> 0;
						                default -> 2;
						            };
						        } finally {
						        	System.out.println(s);
						        }
						    }
						    public static void main(String argv[]) {
						    	new X().foo("hello");
						    }
						}"""
			},
			"hello");
	}
	public void testBug544073_053() {
		Map<String, String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public int foo(int i)  {
						        int j = (switch (i) {
						            case 1 -> 1;
						            default -> 2;
						        });
						        return j;
						    }
						    public static void main(String[] argv) {
						    	new X().foo(1);
						    }
						}
						"""
			},
			"",
			customOptions);
	}
	public void testBug544073_054() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							enum MyEnum {
								FIRST;
							}
						
							public void foo(MyEnum myEnum) {
								int i = switch (myEnum) {
									case FIRST ->  1;
								};
									System.out.println( "i:" + i);
							}
						
							public static void main(String argv[]) {
								new X().foo(MyEnum.FIRST);
							}
						}"""
			},
			"i:1");
	}
	public void testBug544073_055() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							enum MyEnum {
								FIRST;
							}
						
							public void foo(MyEnum myEnum) {
								int i = switch (myEnum) {
									case FIRST ->  1;
									default ->  0;
								};
									System.out.println( "i:" + i);
							}
						
							public static void main(String argv[]) {
								new X().foo(MyEnum.FIRST);
							}
						}"""
			},
			"i:1");
	}
	public void testBug544073_056() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
					    public int foo(int i) {
					    	var v = switch(i) {
					    	case 0 -> x;
					    	default -> 1;
					    	};
					    	return v;
					    }
					    public static void main(String[] argv) {
					       System.out.println(new X().foo(0));
					    }
					}""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				case 0 -> x;
				          ^
			x cannot be resolved to a variable
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_057() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public int foo(int i) {
						    	int v = switch(i) {
						    	case 0 -> switch(i) {
						    			case 0 -> 0;
						    			default -> 1;
						    		};
						    	default -> 1;
						    	};
						    	return v;
						    }
						    public static void main(String[] argv) {
						       System.out.println(new X().foo(0));
						    }
						}"""
			},
			"0");
	}
	public void testBug544073_058() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public int foo(int i) {
						    	int v = switch(switch(i) {
						        		default -> 1;
						        		}) {
						        	default -> 1;
						        };
						       return v;
						    }
						
						    public static void main(String[] argv) {
						       System.out.println(new X().foo(0));
						    }
						}
						"""
			},
			"1");
	}
	public void testBug544073_059() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public static int foo(int i) {
								boolean v = switch (i) {
									case 1: i = 10; yield true;
									default: yield false;
								};
								return v ? 0 : 1;
							}
							public static void main(String[] argv) {
								System.out.println(X.foo(0));
							}
						}"""
			},
			"1");
	}
	public void testBug544073_060() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public static int foo(int i) {
								boolean v = switch (i) {
									case 1: i++; yield true;
									default: yield false;
								};
								return v ? 0 : 1;
							}
							public static void main(String[] argv) {
								System.out.println(X.foo(1));
							}
						}"""
			},
			"0");
	}
	public void testBug544073_061() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public static int foo(int i) {
								boolean v = switch (i) {
									case 1: i+= 10; yield true;
									default: yield false;
								};
								return v ? 0 : 1;
							}
							public static void main(String[] argv) {
								System.out.println(X.foo(1));
							}
						}"""
			},
			"0");
	}
	public void testBug544073_062() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public static int foo(int i) {
								boolean v = switch (i) {
									case 1: switch(i) {case 4: break;}; yield true;
									default: yield false;
								};
								return v ? 0 : 1;
							}
							public static void main(String[] argv) {
								System.out.println(X.foo(1));
							}
						}"""
			},
			"0");
	}
	public void testBug544073_063() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public static int foo(int i) {
								boolean v = switch (i) {
									case 1: foo(5); yield true;
									default: yield false;
								};
								return v ? 0 : 1;
							}
							public static void main(String[] argv) {
								System.out.println(X.foo(1));
							}
						}"""
			},
			"0");
	}
	public void testBug544073_064() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public int foo(int i) {
							boolean v = switch (i) {
						        case 1:
						        	switch (i) {
						        		case 1 : i = 10;
						        			break;
						        		default :
						        			i = 2;
						        			break;
						        		}
						        yield true;
						        default: yield false;
						    };
						    return v ? 0 : 1;
						    }
						
						    public static void main(String[] argv) {
						       System.out.println(new X().foo(0));
						    }
						}
						"""
			},
			"1");
	}
	public void testBug544073_065() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(int i) {
							int v =
								switch(switch(i) {
										case 0 -> { yield 2; }
										default -> { yield 3; }
									}) {
								case 0 -> { yield 0; }
								default -> { yield 1; }
							};
						return v == 1 ? v : 0;
						}
						public static void main(String[] argv) {
							System.out.println(new X().foo(0));
						}
					}"""
		},
		"1");
	}
	public void testBug544073_066() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(int i) {
					    	int k = 10;
					    	switch (i) {
					    		case 0 -> { k = 0;}
					    		default -> k = -1;
					    	}
					        return k;
					    }
					    public static void main(String[] argv) {
					        System.out.println(new X().foo(0) == 0 ? "Success" : "Failure");
					    }
					
					}
					"""
		},
		"Success");
	}
	public void testBug544073_067() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void foo(Day day) {
							switch (day) {
							case MONDAY, FRIDAY -> System.out.println(Day.SUNDAY);
							case TUESDAY                -> System.out.println(7);
							case THURSDAY, SATURDAY     -> System.out.println(8);
							case WEDNESDAY              -> System.out.println(9);
							default -> {}
							}    \s
						}
						public static void main(String[] args) {
							X.foo(Day.WEDNESDAY);
						}
					}
					
					enum Day {
						MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
					}
					"""
		},
		"9");
	}
	public void testBug544073_068() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void foo (int i) {
							int v = switch (i) {
								case 60, 600: yield 6;
								case 70: yield 7;
								case 80: yield 8;
								case 90, 900: yield 9;
								default: yield 0;
							};
							System.out.println(v);
						}
						public static void main(String[] args) {
							X.foo(10);
						}
					}
					"""
		},
		"0");
	}
	// see comment 12 in the bug
	public void testBug513766_01() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					public class X {
					
					    public void foo(int i) {
					    	if (switch(i) { default -> magic(); })
					            System.out.println("true");
					        if (magic())
					            System.out.println("true, too");
					    }
					    <T> T magic() { return null; }
					}
					""",
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (switch(i) { default -> magic(); })
					    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from Object to boolean
				----------
				2. ERROR in X.java (at line 6)
					if (magic())
					    ^^^^^^^
				Type mismatch: cannot convert from Object to boolean
				----------
				""";
		runner.javacTestOptions = JavacHasABug.JavacBug8179483_switchExpression;
		runner.runNegativeTest();
	}
	public void testBug544073_070() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static int foo(int i) throws MyException {
					    	int v = switch (i) {
					    		default -> throw new MyException();
					    	};
					        return v;
					    }
					    public static void main(String argv[]) {
					    	try {
								System.out.println(X.foo(1));
							} catch (MyException e) {
								System.out.println("Exception thrown as expected");
							}
						}
					}
					class MyException extends Exception {
						private static final long serialVersionUID = 3461899582505930473L;\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					int v = switch (i) {
				    		default -> throw new MyException();
				    	};
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				A switch expression should have at least one result expression
				----------
				""");
	}
	public void testBug544073_071() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		String message =
				"""
			----------
			1. WARNING in X.java (at line 5)
				case "ABC", (false ? (String) "c" : (String) "d") : break;
				                     ^^^^^^^^^^^^
			Dead code
			----------
			""";

		this.runWarningTest(new String[] {
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
			message,
			getCompilerOptions(),
			"-Xlint:preview");
	}
	public void testBug544073_072() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		String message =
				"""
			----------
			1. WARNING in X.java (at line 5)
				case "ABC", (false ? (String) "c" : (String) "d") : break;
				                     ^^^^^^^^^^^^
			Dead code
			----------
			""";

		this.runWarningTest(new String[] {
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
	public void testBug544073_074() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public enum X {
					    A, B;\s
					    public static void main(String[] args) {
					         X myEnum = X.A;
					         int o;
					         switch(myEnum) {
					             case A -> o = 5;
					             case B -> o = 10;
					             default -> o = 0;
					         }
					         System.out.println(o);
					     }
					}
					"""
		},
		"5");
	}
	public void testBug544073_075() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public enum X {
					    A, B;
					    \s
					    public static void main(String[] args) {
					         X myEnum = X.A;
					         int o;
					         var f = switch(myEnum) {
					             case A -> o = 5;
					             case B -> o = 10;
					         };
					         System.out.println(o);
					     }
					}\s
					"""
		},
		"5");
	}
	public void testBug544073_076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					
					public class X {
					
					
						public static int foo() {
						for (int i = 0; i < 1; ++i) {
								int k = switch (i) {
									case 0:
										yield 1;
									default:
										continue;
								};
								System.out.println(k);
							}
							return 1;
						}
						public static void main(String[] args) {
							X.foo();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					continue;
					^^^^^^^^^
				Continue out of switch expressions not permitted
				----------
				2. ERROR in X.java (at line 11)
					continue;
					^^^^^^^^^
				'continue' or 'return' cannot be the last statement in a Switch expression case body
				----------
				""");
	}
	public void testBug544073_077() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					
					public class X {
					
					
						public static int foo() {
						for (int i = 0; i < 1; ++i) {
								int k = switch (i) {
									case 0:
										yield 1;
									default:
										return 2;
								};
								System.out.println(k);
							}
							return 100;
						}
						public static void main(String[] args) {
							X.foo();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					return 2;
					^^^^^^^^^
				Return within switch expressions not permitted
				----------
				2. ERROR in X.java (at line 11)
					return 2;
					^^^^^^^^^
				'continue' or 'return' cannot be the last statement in a Switch expression case body
				----------
				""");
	}
	public void testBug544073_078() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY, SUNDAY:
								System.out.println("Weekend");
							case MONDAY:
								System.out.println("Weekday");
							default:\s
							}
						}
					}
					
					enum Day {
						MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case SATURDAY, SUNDAY, SUNDAY:
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Duplicate case
				----------
				""");
	}
	public void testBug544073_079() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(Day day) {
							switch (day) {
							case SATURDAY, SUNDAY, MONDAY:
								System.out.println("Weekend");
							case MONDAY, SUNDAY:
								System.out.println("Weekday");
							default:\s
							}
						}
					}
					
					enum Day {
						MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case SATURDAY, SUNDAY, MONDAY:
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Duplicate case
				----------
				2. ERROR in X.java (at line 6)
					case MONDAY, SUNDAY:
					^^^^^^^^^^^^^^^^^^^
				Duplicate case
				----------
				3. ERROR in X.java (at line 6)
					case MONDAY, SUNDAY:
					^^^^^^^^^^^^^^^^^^^
				Duplicate case
				----------
				""");
	}
	public void testBug544073_80() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							
							public class X {
							
								public static int yield() {
									return 1;
								}
								public static int foo(int val) {
									return bar (switch (val) {
									case 1 : { yield val == 1 ? 2 : 3; }
									default : { yield 2; }
									});
								}
								public static int bar(int val) {
									return val;
								}
								public static void main(String[] args) {
									System.out.println(X.foo(1));
								}
							}
							"""
				},
				"2");
	}
	public void testBug544073_81() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						
						
							public static int foo(int val) {
								int k = switch (val) {
								case 1 : { break 1; }
								default : { break 2; }
								};
								return k;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(1));
							}
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case 1 : { break 1; }
						                 ^
					Syntax error on token "1", delete this token
					----------
					2. ERROR in X.java (at line 7)
						default : { break 2; }
						                  ^
					Syntax error on token "2", delete this token
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2323
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException while compiling switch expression
	public void testIssue2323() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void f() {
						int[] array = null;
						(array = new int[1])[0] = 42;
					}
					public static int g() {
						int[] array = null;
						System.out.println(switch(10) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
					});
						return (array = new int[1])[0];
					}
				}
				"""
				},
				"");
	}
	public void testBug547891_01() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void yield() {}
							public static void main(String[] args) {
								yield();
								X.yield();
							}
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						yield();
						^^^^^^^
					restricted identifier yield not allowed here - method calls need to be qualified
					----------
					""");
	}
	public void testBug547891_02() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void yield() {}
						public static void main(String[] args) {
							yield();
						}
						public static void bar() {
							Zork();
						}
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				yield();
				^^^^^^^
			restricted identifier yield not allowed here - method calls need to be qualified
			----------
			2. ERROR in X.java (at line 7)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_03() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield 1;
						}
					}
					class yield {
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						yield 1;
						^^^^^
					Syntax error on token "yield", AssignmentOperator expected after this token
					----------
					2. ERROR in X.java (at line 6)
						class yield {
						      ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					""");
	}
	public void testBug547891_04() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield 1;
							Zork();
						}
					}
					class yield {
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				yield 1;
				^^^^^
			Syntax error on token "yield", AssignmentOperator expected after this token
			----------
			2. ERROR in X.java (at line 7)
				class yield {
				      ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_05() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield y;
						}
					}
					class yield {
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						yield y;
						^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					2. ERROR in X.java (at line 6)
						class yield {
						      ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					""");
	}
	public void testBug547891_06() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield y;
							Zork();
						}
					}
					class yield {
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				yield y;
				^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			2. ERROR in X.java (at line 4)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			3. ERROR in X.java (at line 7)
				class yield {
				      ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_07() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield y = null;
						}
					}
					class yield {
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						yield y = null;
						^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					2. ERROR in X.java (at line 6)
						class yield {
						      ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					""");
	}
	public void testBug547891_08() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield y = null;
							Zork();
						}
					}
					class yield {
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				yield y = null;
				^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			2. ERROR in X.java (at line 4)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			3. ERROR in X.java (at line 7)
				class yield {
				      ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_09() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
						}
					}
					class yield {
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						class yield {
						      ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					""");
	}
	public void testBug547891_10() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Zork();
						}
					}
					class yield {
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			2. ERROR in X.java (at line 6)
				class yield {
				      ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_11() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							new yield();
						}
					}
					class yield {
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						new yield();
						    ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					2. ERROR in X.java (at line 6)
						class yield {
						      ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					""");
	}
	public void testBug547891_12() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							new yield();
							Zork();
						}
					}
					class yield {
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				new yield();
				    ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			2. ERROR in X.java (at line 4)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			3. ERROR in X.java (at line 7)
				class yield {
				      ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_13() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield[] y;
						}
					}
					class yield {
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						yield[] y;
						^^^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					2. ERROR in X.java (at line 6)
						class yield {
						      ^^^^^
					'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
					----------
					""");
	}
	public void testBug547891_14() {
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							yield[] y;
							Zork();
						}
					}
					class yield {
					}
					""",
		};
		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				yield[] y;
				^^^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			2. ERROR in X.java (at line 4)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			3. ERROR in X.java (at line 7)
				class yield {
				      ^^^^^
			'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_15() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		String message =
				"""
			----------
			1. ERROR in X.java (at line 6)
				case 1 -> yield();
				          ^^^^^^^
			restricted identifier yield not allowed here - method calls need to be qualified
			----------
			2. ERROR in X.java (at line 8)
				case 3 -> {yield yield();}
				                 ^^^^^^^
			restricted identifier yield not allowed here - method calls need to be qualified
			----------
			3. ERROR in X.java (at line 10)
				default -> { yield yield();}
				                   ^^^^^^^
			restricted identifier yield not allowed here - method calls need to be qualified
			----------
			""";

		this.runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					
					\t
						public static int foo(int i) {
							int r = switch(i) {
								case 1 -> yield();
								case 2 -> X.yield();
								case 3 -> {yield yield();}
								case 4 -> {yield X.yield();}
								default -> { yield yield();}
							};
							return r;
						}
						public static int yield() {
							return 0;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}
					"""
			},
			message);
	}
	public void testBug547891_16() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		String message =
				"""
			----------
			1. ERROR in X.java (at line 9)
				case 3 -> {yield yield();}
				                 ^^^^^^^
			restricted identifier yield not allowed here - method calls need to be qualified
			----------
			2. ERROR in X.java (at line 11)
				default -> { yield yield();}
				                   ^^^^^^^
			restricted identifier yield not allowed here - method calls need to be qualified
			----------
			""";

		this.runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					
					\t
						public  int foo(int i) {
							X x = new X();
							int r = switch(i) {
								case 1 -> this.yield();
								case 2 -> x.new Y().yield();
								case 3 -> {yield yield();}
								case 4 -> {yield new X().yield() + x.new Y().yield();}
								default -> { yield yield();}
							};
							return r;
						}
						public  int yield() {
							return 0;
						}
						class Y {
							public  int yield() {
								return 0;
							}\t
						}
						public static void main(String[] args) {
							System.out.println(new X().foo(0));
						}
					}
					"""
			},
			message);
	}
	public void testBug547891_17() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						\t
							public  static int foo(int i) {
								int yield = 100;
								int r = switch(i) {
									default -> yield - 1;
								};
								return r;
							}
							public  int yield() {
								return 0;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}"""
			},
			"99");
	}
	public void testBug547891_18() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						\t
							public  static int foo(int i) {
								int yield = 100;
								int r = switch(i) {
									default -> {yield - 1;}
								};
								return r;
							}
							public  int yield() {
								return 0;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}"""
			},
			"-1");
	}
	public void testBug547891_19() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						   static int yield = 100;
						
						\t
							public  static int foo(int i) {
								int r = switch(i) {
									default -> yield - 1;
								};
								return r;
							}
							public  int yield() {
								return 0;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}"""
			},
			"99");
	}
	public void testBug547891_20() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						   static int yield = 100;
						
						\t
							public  static int foo(int i) {
								int r = switch(i) {
									default -> {yield - 1;}
								};
								return r;
							}
							public  int yield() {
								return 0;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}"""
			},
			"-1");
	}
	public void testBug547891_21() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		String message =
				"""
			----------
			1. ERROR in X.java (at line 7)
				default -> yield - 1;
				           ^^^^^
			Cannot make a static reference to the non-static field yield
			----------
			""";

		this.runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					   int yield = 100;
					
					\t
						public  static int foo(int i) {
							int r = switch(i) {
								default -> yield - 1;
							};
							return r;
						}
						public  int yield() {
							return 0;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			message);
	}
	public void testBug547891_22() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
							static int yield = 100;
						\t
							public  static int foo(int i) {
							int r = switch(i) {
									default -> X.yield();
								};
								return r;
							}
							public static  int yield() {
								yield: while (X.yield == 100) {
									yield = 256;
									break yield;
								}
								return yield;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}
						"""
			},
			"256");
	}
	public void testBug547891_23() {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
							static int yield =100 ;
						\t
							public  static int foo(int i) {
							int r = switch(i) {
									default -> X.yield();
								};
								return r;
							}
							public static  int yield() {
								int yield = 500 ;
								yield: while (yield == 500) {
									yield = 1024;
									break yield;
								}
								return yield;
							}
							public static void main(String[] args) {
								System.out.println(X.foo(0));
							}
						}
						"""
			},
			"1024");
	}
	public void testBug547891_24() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
					\t
						public  static int foo(int i) {
							int yield = 100;
							int r = switch(i) {
								default -> {yield yield + 1;}
							};
							return r;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			"101");
	}
	public void testBug547891_25() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
					\t
						public  static int foo(int i) {
							int yield = 100;
							int r = switch(i) {
								default -> {yield yield + yield + yield * yield;}
							};
							return r;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			"10200");
	}
	public void testBug547891_26() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
					\t
						public  static int foo(int i) {
							int yield = 100;
							int r = switch(i) {
								default -> {yield + yield + yield + yield * yield;}
							};
							return r;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			"10200");
	}
	public void testBug547891_27() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
					\t
						public  static int foo(int i) {
							int yield = 100;
							int r = switch(i) {
								default ->0 + yield + 10;
							};
							return r;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			"110");
	}
	public void testBug547891_28() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						public  static int foo(int i) {
							int yield = 100;
							int r = switch(i) {
								 case 0 : yield 100;
								 case 1 : yield yield;
								 default: yield 0;
							};
							return r;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			"100");
	}
	public void testBug547891_29() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						public  static int foo(int i) {
							int yield = 100;
							int r = switch(i) {
								 case 0 : yield 100;
								 case 1 : yield yield;
								 default: yield 0;
							};
							return r > 100 ? yield + 1 : yield + 200;
						}
						public static void main(String[] args) {
							System.out.println(X.foo(0));
						}
					}"""
			},
			"300");
	}
	public void testBug550354_01() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 \s
					  public static int foo(int i) throws Exception {
					    int v = switch (i) {
					        default ->  {if (i > 0) yield 1;
					        else yield 2;}
					    };
					    return v;
					  }
					  public static void main(String argv[]) throws Exception {
					    System.out.println(X.foo(1));
					  }
					}"""
			},
			"1");
	}
	public void testBug548418_01() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  @SuppressWarnings({"unused" })
					  public static void main(String[] args) {
						int day =10;
					    int i = switch (day) {
					      default -> {
					        for(int j = 0; j < 3; j++) {
					        	yield 99;
					        }
					        yield 0;
					      }
					    };
					    System.out.println(i);
					  }
					}
					"""
			},
			"99");
	}
	public void testBug550853_01() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 \s
					  public static int foo(int i) throws Exception {
					    int v = switch (i) {
					        default : {yield switch (i) {
					        		default -> { yield 0; }\s
					        		};
					        	}
					    };
					    return v;
					  }
					  public static void main(String argv[]) throws Exception {
					    System.out.println(X.foo(1));
					  }
					}
					"""
			},
			"0");
	}
	public void testBug550861_01() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 \s
					  public static void foo(int i) throws Exception {
						  System.out.println(switch(0) {
						  default -> {
						    do yield 1; while(false);
						  }
						  });
					  }
					  public static void main(String argv[]) throws Exception {
						  X.foo(1);
					  }
					}
					"""
			},
			"1");
	}
	public void testBug551030a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings("nls")
						static final String MONDAY = "MONDAY";
						public static void main(String[] args) {
							int num = switch (day) {
							case MONDAY:\s
								// Nothing
							default:
								yield ";    \s
							};\s
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					yield ";    \s
					      ^^^^^^^
				String literal is not properly closed by a double-quote
				----------
				""");
	}
	public void testBug551030b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings("nls")
						static final String MONDAY = "MONDAY";
						public static void main(String[] args) {
							int num = switch (day) {
							case MONDAY:\s
								// Nothing
							default:
								yield \""";    \s
							};\s
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					yield \""";    \s
					        ^^^^^^^
				String literal is not properly closed by a double-quote
				----------
				""");
	}
	public void testBug544943() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						public static int foo(int i) throws MyException {
							int v = -1;
							try {
								v = switch (i) {
									case 0 -> switch(i) {
												case 0 -> 1;
												default -> throw new MyException();
											  };
									default -> 1;
								};
							} finally {
								// do nothing
							}
							return v;
						}\s
						public static void main(String argv[]) {
							try {
								System.out.println(X.foo(0));
							} catch (MyException e) {
								e.printStackTrace();
							}
						}
					}
					class MyException extends Exception {
						private static final long serialVersionUID = 3461899582505930473L;\t
					}"""
			},
			"1");
	}
	public void testBug544943_2() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					\t
						public static int foo(int i) throws Exception {
							int v = switch (i) {
								case 0 -> switch (i) {
									case 0 -> 0;
									default-> throw new Exception();
									case 3 -> 3;
									case 2 -> throw new Exception();
									};
								default -> 0;
							};
							return v;
						}
						public static void main(String argv[]) throws Exception {
							System.out.println(X.foo(1));
						}
					}"""
			},
			"0");
	}
	public void testBug552764_001() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						static int twice(int i) {
							switch (i) {
								default -> 3;
							}
							return 0;
						}
						public static void main(String[] args) {
							System.out.print(twice(3));
						}
					}
					""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				default -> 3;
				^^^^^^^
			Arrow in case statement supported from Java 14 onwards only
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug552764_002() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						static int twice(int i) {
							return switch (i) {
								default -> 3;
							};
						}
						public static void main(String[] args) {
							System.out.print(twice(3));
						}
					}
					""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 3)
				return switch (i) {
						default -> 3;
					};
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Switch Expressions are supported from Java 14 onwards only
			----------
			2. ERROR in X.java (at line 4)
				default -> 3;
				^^^^^^^
			Arrow in case statement supported from Java 14 onwards only
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug552764_003() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		String[] testFiles = new String[] {
				"X.java",
				"""
					public class X {
						static int twice(int i) {
							switch (i) {
								case 1, 2 : break;
								default : break;
							}
							return 0;
						}
						public static void main(String[] args) {
							System.out.print(twice(3));
						}
					}
					""",
		};

		String expectedProblemLog =
				"""
			----------
			1. ERROR in X.java (at line 4)
				case 1, 2 : break;
				^^^^^^^^^
			Multi-constant case labels supported from Java 14 onwards only
			----------
			""";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug558067_001() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
					    public int foo(int i, int e) {
					               LABEL: while (i == 0) {
					            i = switch (e) {
					                case 0 : {
					                    for (;;) {
					                        break LABEL; // NO error flagged
					                    }
					                    yield 1;
					                }
					                default : yield 2;
					            };
					        }
					    return i;
					    }
					    public static void main(String argv[]) {
					        new X().foo(0, 1);
					     }
					}
					"""
			},
				"""
					----------
					1. WARNING in X.java (at line 3)
						LABEL: while (i == 0) {
						^^^^^
					The label LABEL is never explicitly referenced
					----------
					2. ERROR in X.java (at line 7)
						break LABEL; // NO error flagged
						^^^^^^^^^^^^
					Breaking out of switch expressions not permitted
					----------
					3. ERROR in X.java (at line 9)
						yield 1;
						^^^^^^^^
					Unreachable code
					----------
					""");
	}
	public void testBug558067_002() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
					    public int foo(int i, int e) {
					   TOP:System.out.println("hello");
					          int x = switch(i) {
					       case 0:
					               LABEL: while (i == 0) {
					            i = switch (e) {
					                case 0 : {
					                    for (;;) {
					                        break LABEL;
					                    }
					                    yield 1;
					                }
					                default : yield 2;
					            };
					        }
					       case 2: for(;;) break TOP;
					       default: yield 0;
					       };
					    return i;
					    }
					    public static void main(String argv[]) {
					        new X().foo(0, 1);
					     }
					}\s
					"""
			},
				"""
					----------
					1. WARNING in X.java (at line 3)
						TOP:System.out.println("hello");
						^^^
					The label TOP is never explicitly referenced
					----------
					2. WARNING in X.java (at line 6)
						LABEL: while (i == 0) {
						^^^^^
					The label LABEL is never explicitly referenced
					----------
					3. ERROR in X.java (at line 10)
						break LABEL;
						^^^^^^^^^^^^
					Breaking out of switch expressions not permitted
					----------
					4. ERROR in X.java (at line 12)
						yield 1;
						^^^^^^^^
					Unreachable code
					----------
					5. ERROR in X.java (at line 17)
						case 2: for(;;) break TOP;
						                ^^^^^^^^^^
					Breaking out of switch expressions not permitted
					----------
					""");
	}
	public void testBug558067_003() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
					    public int foo(int i, int e) {
					               LABEL: while (i == 0) {
					            i = switch (e) {
					                case 0 : {
					                    for (;;) {
					                        continue LABEL;
					                    }
					                    yield 1;
					                }
					                default : yield 2;
					            };
					        }
					    return i;
					    }
					    public static void main(String argv[]) {
					        new X().foo(0, 1);
					     }
					}
					"""
			},
				"""
					----------
					1. WARNING in X.java (at line 3)
						LABEL: while (i == 0) {
						^^^^^
					The label LABEL is never explicitly referenced
					----------
					2. ERROR in X.java (at line 7)
						continue LABEL;
						^^^^^^^^^^^^^^^
					Continue out of switch expressions not permitted
					----------
					3. ERROR in X.java (at line 9)
						yield 1;
						^^^^^^^^
					Unreachable code
					----------
					""");
	}
	public void testBug558067_004() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
					    public int foo(int i, int e) {
					               LABEL: while (i == 0) {
					            i = switch (e) {
					                case 0 : {
					                    switch(e) {
					                      case 0 : {
					                          break LABEL;
					                      }
					                    }
					                    yield 1;
					                }
					                default : yield 2;
					            };
					        }
					    return i;
					    }
					    public static void main(String argv[]) {
					        new X().foo(0, 1);
					     }
					}
					"""
			},
				"""
					----------
					1. WARNING in X.java (at line 3)
						LABEL: while (i == 0) {
						^^^^^
					The label LABEL is never explicitly referenced
					----------
					2. ERROR in X.java (at line 8)
						break LABEL;
						^^^^^^^^^^^^
					Breaking out of switch expressions not permitted
					----------
					""");
	}
	public void testBug558067_005() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
					public class X {
					    public int foo(int i, int e) {
					               LABEL: while (i == 0) {
					            i = switch (e) {
					                case 0 : {
					                    switch(e) {
					                      case 0 : {
					                          continue LABEL;
					                      }
					                    }
					                    yield 1;
					                }
					                default : yield 2;
					            };
					        }
					    return i;
					    }
					    public static void main(String argv[]) {
					        new X().foo(0, 1);
					     }
					}
					"""
			},
				"""
					----------
					1. WARNING in X.java (at line 3)
						LABEL: while (i == 0) {
						^^^^^
					The label LABEL is never explicitly referenced
					----------
					2. ERROR in X.java (at line 8)
						continue LABEL;
						^^^^^^^^^^^^^^^
					Continue out of switch expressions not permitted
					----------
					""");
	}
		public void testConversion1() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								 public static int i = 0;
								 private static String typeName(byte arg){ return "byte"; }
							    private static String typeName(char arg){ return "char"; }
							    private static String typeName(short arg){ return "short"; }
							    private static String typeName(int arg){ return "int"; }
							    private static String typeName(float arg){ return "float"; }
							    private static String typeName(long arg){ return "long"; }
							    private static String typeName(double arg){ return "double"; }
							    private static String typeName(String arg){ return "String"; }
									public static void main(String[] args) {
									 byte v1 = (byte)0;
							        char v2 = ' ';
							        var v = switch(i+1){
							                    case 1 -> v2;
							                    case 5 -> v1;
							                    default -> v2;
							        };
							        System.out.print(typeName(v));
								}
							}
							"""
				},
				"int");
	}
	public void testConversion2() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								 public static int i = 0;
								 private static String typeName(byte arg){ return "byte"; }
							    private static String typeName(char arg){ return "char"; }
							    private static String typeName(short arg){ return "short"; }
							    private static String typeName(int arg){ return "int"; }
							    private static String typeName(float arg){ return "float"; }
							    private static String typeName(long arg){ return "long"; }
							    private static String typeName(double arg){ return "double"; }
							    private static String typeName(String arg){ return "String"; }
									public static void main(String[] args) {
									 long v1 = 0L;
							        double v2 = 0.;
							        var v = switch(i+1){
							                    case 1 -> v2;
							                    case 5 -> v1;
							                    default -> v2;
							        };
							        System.out.print(typeName(v));
								}
							}
							"""
				},
				"double");
	}
	public void testConversion3() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								 public static int i = 0;
								 private static String typeName(byte arg){ return "byte"; }
							    private static String typeName(char arg){ return "char"; }
							    private static String typeName(short arg){ return "short"; }
							    private static String typeName(int arg){ return "int"; }
							    private static String typeName(float arg){ return "float"; }
							    private static String typeName(long arg){ return "long"; }
							    private static String typeName(double arg){ return "double"; }
							    private static String typeName(String arg){ return "String"; }
									public static void main(String[] args) {
									 long v1 = 0L;
							        float v2 = 0.f;
							        var v = switch(i+1){
							                    case 1 -> v2;
							                    case 5 -> v1;
							                    default -> v2;
							        };
							        System.out.print(typeName(v));
								}
							}
							"""
				},
				"float");
	}
	public void testConversion4() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								 public static int i = 0;
								 private static String typeName(byte arg){ return "byte"; }
							    private static String typeName(char arg){ return "char"; }
							    private static String typeName(short arg){ return "short"; }
							    private static String typeName(int arg){ return "int"; }
							    private static String typeName(float arg){ return "float"; }
							    private static String typeName(long arg){ return "long"; }
							    private static String typeName(double arg){ return "double"; }
							    private static String typeName(String arg){ return "String"; }
									public static void main(String[] args) {
									 short v1 = 0;
							        char v2 = ' ';
							        var v = switch(i+1){
							                    case 1 -> v2;
							                    case 5 -> v1;
							                    default -> v2;
							        };
							        System.out.print(typeName(v));
								}
							}
							"""
				},
				"int");
	}
	public void testConversion5() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								 public static int i = 0;
							    private static String typeName(char arg){ return "char"; }
							    private static String typeName(int arg){ return "int"; }
							    private static String typeName(float arg){ return "float"; }
							    private static String typeName(long arg){ return "long"; }
							    private static String typeName(double arg){ return "double"; }
							    private static String typeName(String arg){ return "String"; }
									public static void main(String[] args) {
									 char v1 = 'a';
							        var v = switch(i+1){
							                    case 1 -> 200;
							                    case 5 -> v1;
							                    default -> v1;
							        };
							        System.out.print(typeName(v));
								}
							}
							"""
				},
				"char");
	}
	public void testBug545567_1() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({"finally"})
								public static void main(String[] args) {
							    	int t = switch (0) {
							        default -> {
							            try {
							                yield 1;
							            }
							            finally {
							                yield 3;
							            }
							        }
							     };
							     System.out.println(t);
							    }
							}
							
							"""
				},
				"3");
	}
	public void testBug545567_2() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({ "finally"})
								public static void main(String[] args) {
							    	float t = switch (0) {
							        default -> {
							            try {
							                yield 1;
							            }
							            finally {
							                yield 3;
							            }
							        }
							     };
							     System.out.println(t);
							    }
							}
							
							"""
				},
				"3.0");
	}
	public void testBug545567_3() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({ "finally"})
								public static void main(String[] args) {
							    	String t = switch (0) {
							        default -> {
							            try {
							                yield "one";
							            }
							            finally {
							                yield "three";
							            }
							        }
							     };
							     System.out.println(t);
							    }
							}
							
							"""
				},
				"three");
	}
	public void testBug545567_4() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({"finally" })
								public static void main(String[] args) {
							    	String t = switch (0) {
							        default -> {
							            try {
							                yield "one";
							            }
							            catch (Exception ex) {
							                yield "two";
							            }
							            finally {
							                yield "three";
							            }
							        }
							     };
							     System.out.println(t);
							    }
							}
							
							"""
				},
				"three");
	}
	public void testBug545567_5() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({ "finally" })
								public static void main(String[] args) {
							    	String t = switch (0) {
							        default -> {
							            try {
							                yield "one";
							            }
							            catch (Exception ex) {
							            }
							            yield "zero";
							        }
							     };
							     System.out.print(t);
							    }
							}
							
							"""
				},
				"one");
	}
	public void testBug545567_6() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({ "finally"})
								public static void main(String[] args) {
							    	(new X()).foo(switch (0) {
							        default -> {
							            try {
							                yield "one";
							            }
							            finally {
							            	yield "zero";
							            }
							        }
							     });
							    }
							     public void foo (String str) {
							     	System.out.print(str);
							    }
							}
							
							"""
				},
				"zero");
	}
	public void testBug545567_7() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({ "finally"})
								public static void main(String[] args) {
							    	System.out.print(switch (0) {
							        default -> {
							            try {
							                yield "one";
							            }
							            finally {
							            	yield "zero";
							            }
							        }
							     });
							    }
							}
							
							"""
				},
				"zero");
	}
	public void testBug545567_8() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    @SuppressWarnings({ "finally"})
								public static void main(String[] args) {
							    	System.out.print(switch (0) {
							        default -> {
							            try {
							                yield 1;
							            }
							            catch (Exception ex) {
							                yield 2;
							            }
							            finally {
							                yield 3;
							            }
							        }
							     });
							    }
							}
							
							"""
				},
				"3");
	}
	public void testBug545567_9() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						       public static void main(String[] args) {
						       new X().foo(args);
						    }
						    @SuppressWarnings({ "finally" })
						       public void foo(String[] args) {
						       int t = switch (0) {
						        default -> {
						             try {
						                yield 1;
						            }
						            catch (Exception ex) {
						                yield 2;\s
						            }
						            finally {
						                yield 3;
						            }
						        }      \s
						     };
						       t += switch (0) {
						    default -> {
						         try {
						            yield 1;
						        }
						        catch (Exception ex) {
						            yield 2;\s
						        }
						        finally {
						            yield 3;
						        }
						    }      \s
						 };
						     System.out.println(t);
						    }\s
						}
						"""
				},
				"6");
	}
	public void testBug545567_10() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							       public static void main(String[] args) {
							       new X().foo(args);
							    }
							    @SuppressWarnings({ "finally" })
							       public void foo(String[] args) {
							       int k = 0;
							       int t = switch (0) {
							        default -> {
							             try {
							                k = switch (0) {
							                   default -> {
							                        try {
							                           yield 10;
							                       }
							                       catch (Exception ex) {
							                           yield 20;\s
							                       }
							                       finally {
							                           yield 30;
							                       }
							                   }      \s
							                };
							            }
							            catch (Exception ex) {
							                yield 2;\s
							            }
							            finally {
							                yield 3;
							            }
							        }      \s
							     };
							     System.out.println(t + k);
							    }\s
							}
							"""
				},
				"33");
	}
	public void testBug545567_11() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							       public static void main(String[] args) {
							       new X().foo(args);
							    }
							    @SuppressWarnings({ "finally" })
							       public void foo(String[] args) {
							       int k = 0;
							       int t = switch (0) {
							        default -> {
							             try {
							                k = switch (0) {
							                   default -> {
							                        try {
							                           yield 10;
							                       }
							                       catch (Exception ex) {
							                           yield 20;\s
							                       }
							                   }      \s
							                };
							            }
							            catch (Exception ex) {
							                yield 2;\s
							            }
							            finally {
							                yield 3;
							            }
							        }      \s
							     };
							     System.out.println(t + k);
							    }\s
							}
							"""
				},
				"13");
	}
	public void testBug545567_12() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							       public static void main(String[] args) {
							       new X().foo(args);
							    }
							    @SuppressWarnings({ "finally" })
							       public void foo(String[] args) {
							       int k = 0;
							       int t = switch (0) {
							        default -> {
							             try {
							                k = switch (0) {
							                   default -> {
							                        try {
							                           yield 10;
							                       }
							                       catch (Exception ex) {
							                           yield 20;\s
							                       }
							                       finally {
							                           yield 30;
							                       }
							                   }      \s
							                };
							            }
							            finally {
							                yield 3;
							            }
							        }      \s
							     };
							     System.out.println(t + k);
							    }\s
							}
							"""
				},
				"33");
	}
	public void testBug545567_13() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					        case 0 -> {yield 100;}
					           default -> { \s
					                try {
					                   yield 1;
					               }
					               catch (Exception ex) {
					                   yield 2;
					                }
					               finally {
					                   yield 3;\s
					               }
					           } \s
					        } + switch (10) {
					        case 0 -> {yield 1024;}
					        default -> { \s
					             try {
					                yield 10;
					            }
					            catch (Exception ex) {
					                yield 20;
					             }
					            finally {
					                yield 30;\s
					            }
					        } \s
					     }); \s
					    }
					}
					"""
			},
			"33");
	}
	public void testBug545567_14() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					        case 0 -> {yield 100;}
					           default -> { \s
					                try {
					                   yield 1;
					               }
					               catch (Exception ex) {
					                   yield 2;
					                }
					               finally {
					                 yield switch (10) {
					                   case 0 -> {yield 1024;}
					                   default -> { \s
					                        try {
					                           yield 10;
					                       }
					                       catch (Exception ex) {
					                           yield 20;
					                        }
					                       finally {
					                           yield 30;\s
					                       }
					                   } \s
					                };               }
					           } \s
					        }); \s
					    }
					}
					"""
			},
			"30");
	}
	public void testBug545567_15() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					        case 0 -> {yield 100;}
					           default -> { \s
					                try {
					                       yield 1;
					               }
					               catch (Exception ex) {
					                   yield 2;
					                }
					               finally {
					                   System.out.println(switch (1) {
					                    default -> {yield 100;}});
					                  yield 1;
					                }
					           } \s
					        }); \s
					    }
					}
					"""
			},
			"100\n1");
	}
	public void testBug545567_16() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					        case 0 -> {yield 100;}
					           default -> {  \s
					                try {
					                    yield switch (10) {
					                    case 0 -> {yield 1024;}
					                    default -> {  \s
					                         try {
					                            yield 10;\s
					                        }  \s
					                        catch (Exception ex) {
					                            yield 20;\s
					                         }  \s
					                        finally {
					                            yield 30;\s
					                        }  \s
					                    }  \s
					                 };                \s
					               }  \s
					               catch (Exception ex) {
					                   yield 2;
					                }  \s
					               finally {
					                 yield 3;               }  \s
					           }  \s
					        }); \s
					    }  \s
					}
					"""
			},
			"3");
	}
	public void testBug545567_17() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					        case 0 -> {yield 100;}
					           default -> {  \s
					                try {
					                    System.out.println( switch (10) {
					                    case 0 -> {yield 1024;}
					                    default -> {  \s
					                         try {
					                            yield 10;\s
					                        }  \s
					                        catch (Exception ex) {
					                            yield 20;\s
					                         }   \s
					                        finally {
					                            yield 30;\s
					                        }  \s
					                    }  \s
					                 });\s
					                   yield 1;  \s
					               }  \s
					               catch (Exception ex) {
					                   yield 2;
					                }  \s
					               finally {
					                 yield 3;               }  \s
					           }  \s
					        }); \s
					    }  \s
					}
					"""
			},
			"30\n"+
			"3");
	}
	public void testBug545567_18() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					       public static void main(String[] args) {
					       new X().foo(args);
					    }  \s
					    @SuppressWarnings({ "finally" })
					       public void foo(String[] args) {
					       int t = 0;
					       t += switch (200) {
					       case 0 -> {yield 100;}
					        default -> {
					             try {
					                yield 1;
					            }  \s
					            catch (Exception ex) {
					                yield 2; \s
					            }  \s
					            finally {
					                yield 3;
					            }  \s
					        }
					     };
					     System.out.println(t);
					    }  \s
					}
					"""
			},
			"3");
	}
	public void testBug545567_19() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					           default -> {  \s
					                try { \s
					                    yield switch (10) {
					                    default -> {  \s
					                         try {
					                            yield 10;\s
					                        }  \s
					                        catch (Exception ex) {
					                            yield 20;\s
					                         }  \s
					                        finally {
					                            yield 30;\s
					                         }  \s
					                    }  \s
					                 };                \s
					               }  \s
					               catch (Exception ex) {
					                   yield 2;
					                }  \s
					               finally {
					                 yield 3;               }    \s
					           }  \s
					        });  \s
					    }  \s
					}\s
					
					"""
			},
			"3");
	}
	// test with Autocloseable
	public void testBug545567_20() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					           default -> {  \s
					                try(Y y = new Y();) {\s
					                       yield  1;
					                }
					               catch (Exception ex) {
					                   yield 2;
					                }  \s
					               finally {
					                 yield 3;
					               }
					           }
					        });
					    }
					}\s
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {
					               // do nothing
					       }
					}
					"""
			},
			"3");
	}
	public void testBug545567_21() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					        System.out.println(switch (1) {
					           default -> {  \s
					                try(Y y = new Y();) {\s
					                       yield  10;
					                }
					               catch (Exception ex) {
					                }  \s
					                 yield 3;
					           }
					        });
					    }
					}\s
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {
					               // do nothing
					       }
					}
					"""
			},
			"10");
	}
	public void testBug545567_22() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					       @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					               int argslength = args.length;
					               int t = switch (1) {
					                       case 0 -> {
					                               yield 100;
					                       }
					                       default -> {
					                               try (Y y = new Y();){
					                                               if (argslength < 1)
					                                               yield 10;
					                                               else
					                                                       yield 12;
					                               } catch (Exception ex) {
					                                       yield 2;
					                               } finally {
					                                       yield 3;
					                               }
					                       }
					               };  \s
					               System.out.println(t);
					       }
					}
					     \s
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {
					               // do nothing
					       }\s
					}
					"""
			},
			"3");
	}
	public void testBug545567_23() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					       @SuppressWarnings({ "finally" })
					       public static void main(String[] args) {
					               int t = switch (1) {
					                       case 0 -> {
					                               yield 100;
					                       }
					                       default -> {
					                               try {
					                                       throw new Exception();
					                               } catch (Exception ex) {
					                                       yield 2;
					                               } finally {
					                                       yield 3;
					                               }
					                       }
					               };  \s
					               System.out.println(t);
					       }
					}
					     \s
					class Y implements AutoCloseable {
					       @Override
					       public void close() throws Exception {
					               // do nothing
					       }\s
					}
					"""
			},
			"3");
	}
	public void testBug545567_24() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   new X().foo();
					 }
					 @SuppressWarnings({ "finally" })
					 public  void foo() {
					   int t = switch (1) {
					     case 0 -> {
					       yield bar(100);
					     }
					     default -> {
					       final Y y2 = new Y();
					       try (Y y = new Y(); y2){
					           yield bar(10);
					       } catch (Exception ex) {
					         yield bar(2);
					       } finally {
					         yield bar(3);
					       }
					     }
					   };  \s
					   System.out.println(t);
					 }
					 public int bar(int i) {
					   return i;
					 }
					}
					
					class Y implements AutoCloseable {
					 @Override
					 public void close() throws Exception {
					   // do nothing
					 }
					}"""
			},
			"3");
	}
	public void testBug545567_25() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   new X().foo();
					 }
					 @SuppressWarnings({ "finally" })
					 public  void foo() {
					   int t = switch (1) {
					     case 0 -> {
					       yield bar(100);
					     }
					     default -> {
					       final Y y2 = new Y();
					       try (Y y = new Y(); y2){
					           yield new X().bar(10);
					       } catch (Exception ex) {
					         yield bar(2);
					       } finally {
					         yield new X().bar(3);
					       }
					     }
					   };  \s
					   System.out.println(t);
					 }
					 public int bar(int i) {
					   return i;
					 }
					}
					
					class Y implements AutoCloseable {
					 @Override
					 public void close() throws Exception {
					   // do nothing
					 }
					}"""
			},
			"3");
	}
	public void testBug571929_normal() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   System.out.println(foo("a"));
					 }
					 private static boolean foo(String s) {
					  bar(0L);
					  return switch (s) {
					    case "a" -> {
					      try {
					        yield true;
					      } finally {
					      }
					    }
					    default -> false;
					  };
					 }
					 private static void bar(long l) {}
					}"""
			},
			"true");
	}
	public void testBug571929_lambda() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   System.out.println(foo("a"));
					 }
					 static long m = 0L;
					 private static boolean foo(String s) {
					  long l = m;
					  // capture l
					  Runnable r = () -> bar(l);
					  return switch (s) {
					    case "a" -> {
					      try {
					        yield true;
					      } finally {
					      }
					    }
					    default -> false;
					  };
					 }
					 private static void bar(long l) {}
					}"""
			},
			"true");
	}
	public void testBug561762_001() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						       public static void main(String[] args) {
						               new X().foo(1);
						       }
						       @SuppressWarnings({ "finally" })
						       public  void foo(int i) {
						               int t = switch (1) {\s
						                       case 0 -> {
						                               yield 0;
						                       }
						                       default -> {
						                               I lam2 = (x) ->  {
						                                               yield 2000;
						                               };
						                               yield 1;
						                       }
						               };
						               System.out.println(t);
						       }
						}
						interface I {
						       public int apply(int i);
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						yield 2000;
						^^^^^^^^^^^
					yield outside of switch expression
					----------
					""");

	}
	public void testBug561766_001() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    @SuppressWarnings({ "finally" })
						       public static void main(String[] args) {
						        System.out.println(switch (1) {
						        case 0 -> {yield switch(0) {}
						        }\s
						           default -> {
						                  yield 3;
						           }
						        });
						    }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case 0 -> {yield switch(0) {}
						                            ^
					Syntax error, insert ";" to complete BlockStatements
					----------
					""");

	}
	public void testBug561766_002() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    @SuppressWarnings({ "finally" })
						       public static void main(String[] args) {
						        System.out.println(switch (1) {
						        case 0 -> {yield 100;}
						           default -> { \s
						                try {
						                       yield switch(0) {
						               }
						               catch (Exception ex) {
						                   yield 2;
						                }
						               finally {
						                  yield 3;
						                }
						           } \s
						        }); \s
						    }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						}
						^
					Syntax error, insert ";" to complete YieldStatement
					----------
					2. ERROR in X.java (at line 9)
						}
						^
					Syntax error, insert "}" to complete Block
					----------
					3. ERROR in X.java (at line 18)
						}
						^
					Syntax error on token "}", delete this token
					----------
					4. ERROR in X.java (at line 19)
						}
						^
					Syntax error, insert "}" to complete ClassBody
					----------
					""");

	}

	public void testBug562129() {
		if (this.complianceLevel < ClassFileConstants.JDK14) return;
		runNegativeTest(
			new String[] {
				"SwitchExpressionError.java",
				"""
					class SwitchExpressionError {
					
					    static boolean howMany(int k) {
					        return false || switch (k) {
					            case 1 -> true;
					            case 2 -> Boolean.FALSE;
					            case 3 -> r;
					        };
					    }
					
					}
					"""
			},
			"""
				----------
				1. ERROR in SwitchExpressionError.java (at line 4)
					return false || switch (k) {
					                        ^
				A switch expression should have a default case
				----------
				2. ERROR in SwitchExpressionError.java (at line 7)
					case 3 -> r;
					          ^
				r cannot be resolved to a variable
				----------
				""");
	}
	public void testBug572121() {
		Map<String, String> compilerOptions = getCompilerOptions();
		// must disable this option to trigger compilation restart
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.DISABLED);
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							 private void foo(int i) {
							 }
							
							 private static void bar() {
							 }
							
							 public static void main(String[] args) {
							  if (f) {
							   Object o = switch (j) {
							    default -> {
							     try {
							      bar();
							     } catch (Throwable e) {
							     }
							     yield null;
							    }
							   };
							  }
							  int i = 0;
							  x.foo(i++);
							 }
							
							 private static boolean f = true;
							 private static int j;
							 private static X x = new X();
							}"""
				},
				"",
				compilerOptions
				);
	}
	public void testBug562198_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    int a[] = {1, 2, 3};
					    public int foo() {
					        return switch (0) {
					               case 0 -> {
					                       yield a[0];
					               }
					            default -> {
					                try {
					                    // do nothing
					                } finally {
					                    // do nothing
					                }
					                yield 0;
					            }
					        };
					    }
					    public static void main(String[] args) {
					               System.out.println(new X().foo());
					       }
					}
					"""
			},
			"1");
	}
	public void testBug562728_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					       static public void main (String[] args) {
					               int a = 0x21;
					               int b = 0xff;
					               switch (a) {
					               case 0x21 -> {
					                       switch (b) {
					                       default -> System.out.println("default");
					                       }
					               }
					               case 0x3b -> System.out.println("3b <- WTH?");
					               }
					       }
					}
					"""
			},
			"default");
	}
	public void testBug562728_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static public void main (String[] args) {
					   int a = 0x21;
					   int b = 0xff;
					   switch (a) {
					     case 0x21 -> {
					       switch (b) {
					         default -> System.out.println("default");
					       }
					       return;
					     }
					     case 0x3b -> System.out.println("3b <- WTH?");
					   }
					 }
					}
					"""
			},
			"default");
	}
	public void testBug562728_003() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static public void main (String[] args) throws Exception {
						   int a = 0x21;
						   int b = 0xff;
						   switch (a) {
						     case 0x21 -> {
						       switch (b) {
						         default -> throw new Exception();
						       }
						       return;\s
						     }
						     case 0x3b -> System.out.println("3b <- WTH?");
						   }
						 }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						return;\s
						^^^^^^^
					Unreachable code
					----------
					""");

	}
	public void testBug562728_004() {
		this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				       static public void main (String[] args) throws Exception {
				               int a = 0x21;
				               int b = 0xff;
				               Zork();
				               switch (a) {
				               case 0x21 -> {
				                       switch (b) {
				                       default -> {
				                               for (;;) {
				                                       if (b > 1)
				                                       throw new Exception();
				                               }
				                       }
				                       }
				               }
				               case 0x3b -> System.out.println("3b <- WTH?");
				               }
				       }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			""");
	}
	public void testBug562728_005() {
		this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {                       \s
				        public static int foo(int i) { \s
				                int v;                 \s
				                int t = switch (i) {   \s
				                case 0 : {             \s
				                        yield 0;       \s
				                }                      \s
				                case 2 :v = 2;
				                default :v = 2;
				                };                     \s
				                return t;              \s
				        }                              \s
				                                       \s
				        public boolean bar() {         \s
				                return true;           \s
				        }
				        public static void main(String[] args) {
				                System.out.println(foo(3));
				        }                              \s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				default :v = 2;
				            ^^
			A switch labeled block in a switch expression should not complete normally
			----------
			""");
	}
	public void testBug562728_006() {
		this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {                       \s
				        public static int foo(int i) { \s
				                int v;                 \s
				                int t = switch (i) {   \s
				                case 0 -> {             \s
				                        yield 0;       \s
				                }                      \s
				                case 2 ->{v = 2;}
				                default ->{v = 2;}
				                };                     \s
				                return t;              \s
				        }                              \s
				                                       \s
				        public boolean bar() {         \s
				                return true;           \s
				        }
				        public static void main(String[] args) {
				                System.out.println(foo(3));
				        }                              \s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				case 2 ->{v = 2;}
				               ^^
			A switch labeled block in a switch expression should not complete normally
			----------
			2. ERROR in X.java (at line 9)
				default ->{v = 2;}
				                ^^
			A switch labeled block in a switch expression should not complete normally
			----------
			""");
	}
    public void testBug562728_007() {
        this.runNegativeTest(
        new String[] {
                "X.java",
                """
					public class X {                       \s
					        public static int foo(int i) { \s
					                int v;                 \s
					                int t = switch (i) {   \s
					                case 0 -> {             \s
					                     return 1;
					                }                      \s
					                default ->100;
					                };                     \s
					                return t;              \s
					        }                              \s
					                                       \s
					        public boolean bar() {         \s
					                return true;           \s
					        }
					        public static void main(String[] args) {
					                System.out.println(foo(3));
					        }                              \s
					}
					"""
        },
        """
			----------
			1. ERROR in X.java (at line 6)
				return 1;
				^^^^^^^^^
			Return within switch expressions not permitted
			----------
			""");
}
	public void testBug563023_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					 static public int foo(int a, int b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					            default -> {
					              yield 0;
					            }
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(0, 0));
					 }
					}
					"""
			},
			"0");
	}
    public void testBug563023_002() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"""
					public class X {\s
					 static public int foo(int a, int b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					            case 0 -> {
					              break;
					            }
					            default -> {
					              yield 0;
					            }
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(0, 0));
					 }
					}
					"""
    		},
        """
			----------
			1. ERROR in X.java (at line 13)
				}
				^^
			A switch labeled block in a switch expression should not complete normally
			----------
			""");
}
    public void testBug563023_003() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"""
					public class X {\s
					 static public int foo(int a, int b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					            case 0 -> {
					              yield 0;
					            }
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(0, 0));
					 }
					}
					"""
    		},
        """
			----------
			1. ERROR in X.java (at line 10)
				}
				^^
			A switch labeled block in a switch expression should not complete normally
			----------
			""");
}
    public void testBug563023_004() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"""
					public class X {\s
					 static public int foo(int a, int b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					            case 0 -> {
					              break;
					            }
					            default -> yield 0;
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(0, 0));
					 }
					}
					"""
    		},
        """
			----------
			1. ERROR in X.java (at line 9)
				default -> yield 0;
				                 ^
			Syntax error on token "0", delete this token
			----------
			""");
}
    public void testBug563023_005() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"""
					public class X {\s
					 static public int foo(int a, int b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					            case 0 -> {
					              break;
					            }
					            default ->{ yield 0;}
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(0, 0));
					 }
					}
					"""
    		},
        """
			----------
			1. ERROR in X.java (at line 11)
				}
				^^
			A switch labeled block in a switch expression should not complete normally
			----------
			""");
}
	public void testBug563023_006() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					 static public int foo(MyEnum a, MyEnum b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					       case ONE -> {\s
					              yield 0;
					            }
					       default -> {yield 1;}
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(MyEnum.ONE, MyEnum.TWO));
					 }
					}\s
					enum MyEnum {
					 ONE,
					 TWO
					}
					"""
			},
			"1");
	}
    public void testBug563023_007() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"""
					public class X {\s
					 static public int foo(MyEnum a, MyEnum b){
					   int t = switch (a) {
					     default -> {
					       switch (b) {
					       case ONE -> {\s
					              yield 0;
					            }
					       }     \s
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(MyEnum.ONE, MyEnum.TWO));
					 }
					}\s
					enum MyEnum {
					 ONE,
					 TWO
					}
					"""
    		},
        """
			----------
			1. WARNING in X.java (at line 5)
				switch (b) {
				        ^
			The enum constant TWO needs a corresponding case label in this enum switch on MyEnum
			----------
			2. ERROR in X.java (at line 10)
				}
				^^
			A switch labeled block in a switch expression should not complete normally
			----------
			""");
}
	public void testBug563147_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					 public int apply();
					}
					public class X {\s
					 static public int foo(int a){
					   int t = switch (a) {
					     default -> {
					       I lambda = () -> { return 0;};
					       yield lambda.apply();
					     }
					   };
					   return t;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(1));
					 }
					}\s
					"""
			},
			"0");
	}
	public void testBug563147_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface FI {
					  public int foo();
					}
					public class X {
					  public int field = 0;
					  public int test() {
					   var v = switch (field) {
					     case 0 -> {
					       yield ((FI  ) () -> {
					         int i = 0;
					         while (true) {
					           i++;
					           if (i == 7) {
					             break;
					           }
					         }
					         return i;
					       });  \s
					     }
					     default -> {
					       yield null;
					     }
					   };\s
					   return 0;
					  }
					  public static void main(String[] args) {
					 int t = new X().test();
					 System.out.println(t);
					}
					}
					"""
			},
			"0");
	}
	public void testBug563147_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface FI {
					  public int foo();
					}
					public class X {
					  public int field = 0;
					  public int test() {
					   var v = switch (field) {
					     case 0 -> {
					       yield ((F  ) () -> {
					         int i = 0;
					         while (true) {
					           i++;
					           if (i == 7) {
					             break;
					           }
					         }
					         return i;
					       });  \s
					     }
					     default -> {
					       yield null;
					     }
					   };\s
					   return 0;
					  }
					  public static void main(String[] args) {
					 int t = new X().test();
					 System.out.println(t);
					}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					yield ((F  ) () -> {
					        ^
				F cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 9)
					yield ((F  ) () -> {
					             ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
	}
	public void testBug565156_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public int test() {
					    return switch (0) {
					      default -> {
					        try {
					          yield 0;
					        }
					        catch (RuntimeException e) {
					          throw e;
					        }
					      }
					    };
					  }   \s
					  public static void main(String[] args) {
					       int i = new X().test();
					       System.out.println(i);
					 }
					}
					"""
			},
			"0");
	}
	public void testBug565156_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public int test() {
					    return switch (0) {
					      default -> {
					        try {
					          yield 0;
					        }
					        finally {
					          //do nothing
					        }
					      }
					    };
					  }   \s
					  public static void main(String[] args) {
					       int i = new X().test();
					       System.out.println(i);
					 }
					}
					"""
			},
			"0");
	}
	public void testBug565156_003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public int test() {
					    return switch (0) {
					      default -> {
					        try {
					          yield 0;
					        }
					        finally {
					          int i = 20;\
					          yield 20;\
					        }
					      }
					    };
					  }   \s
					  public static void main(String[] args) {
					       int i = new X().test();
					       System.out.println(i);
					 }
					}
					"""
			},
			"20");
	}
	public void testBug565156_004() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public int test()  {
					    return switch (0) {
					      default -> {
					        try {
					          yield switch (0) {
					          default -> {
					              try {
					                yield 100;
					              }
					              finally {
					                   yield 200;      \s
					               }
					            }
					          };
					        }
					        finally {
					             yield 20;
					         }
					      }
					    };
					  }
					  public static void main(String[] args){
					       int i = new X().test();
					       System.out.println(i);
					  }
					}"""
			},
			"20");
	}
	public void testBug565156_005() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public int test()  {
					    return switch (0) {
					      default -> {
					        try {
					          yield switch (0) {
					          default -> {
					              try {
					                yield 100;
					              }
					              finally {
					                   // do nothing
					               }
					            }
					          };
					        }
					        finally {
					           // do nothing
					         }
					      }
					    };
					  }
					  public static void main(String[] args){
					       int i = new X().test();
					       System.out.println(i);
					  }
					}"""
			},
			"100");
	}
	public void testBug565156_006() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					            new X().foo(args);
					    }
					
					  @SuppressWarnings({ "finally" })
					  public void foo(String[] args) {
					     int t = switch (0) {
					     default -> {
					        try {
					            if (args == null)
					            yield 1;
					            else if (args.length ==2)
					                    yield 2;\s
					            else if (args.length == 4)
					                    yield 4;
					            else yield 5;\s
					        } finally {
					                yield 3;\s
					        }
					     }
					     };\s
					     t = switch (100) {
					     default -> {
					             try {
					                     yield 10;
					             } finally {
					             }
					     } \s
					     };     \s
					     System.out.println(t);
					  }
					}"""
			},
			"10");
	}
	public void testBug565156_007() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					            new X().foo(args);
					    }
					
					  @SuppressWarnings({ "finally" })
					  public void foo(String[] args) {
					     int t = switch (0) {
					     case 101 -> {yield 101;}
					     default -> {
					        try {
					            if (args == null)
					            yield 1;
					            else if (args.length ==2)
					                    yield 2;\s
					            else if (args.length == 4)
					                    yield 4;
					            else yield 5;\s
					        } finally {
					                yield 3;\s
					        }
					     }
					     };\s
					     t = switch (100) {
					     default -> {
					             try {
					                     yield 10;
					             } finally {
					             }
					     } \s
					     };     \s
					     System.out.println(t);
					  }
					}"""
			},
			"10");
	}
	public void testBug547193_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    System.out.println(switch (0) {default -> {
					      try {
					        yield 1;
					      } catch (Exception ex) {
					        yield 2;
					      }
					    }});
					  }
					}"""
			},
			"1");
	}
	public void testBug565844_01() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case j != 1 ? 2 : 3 ->  true;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void testBug565844_02() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 2;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case j != 1 ? 2 : (j == 2 ? 4 : 5) ->  true;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"true");
	}
	public void testBug565844_03() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case j != 1 ? 2 : 3 ->  {
						    						yield true;
						    					}
						    				default -> { yield false;}
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void testBug565844_04() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case j != 1 ? 2 : 3 :  {
						    						yield true;
						    					}
						    				default : { yield false;}
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void testBug565844_05() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case j != 1 ? 2 : 3 ->  {
						    						yield true;
						    					}
						    				default -> { yield false;}
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case j != 1 ? 2 : 3 ->  {
						     ^^^^^^^^^^^^^^
					case expressions must be constant expressions
					----------
					""");
	}
	public void testBug565844_06() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case j != 1 ? ( j != 1 ? 2: 3 ) : 3 -> false;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void testBug565844_07() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						
						       void foo() {
						               Object value2 = switch(1) {
						                       case AAABBB -> 1;
						                               (I)()->();
						                       default -> 0;
						               };
						       }
						}
						interface I {
						       void apply();
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case AAABBB -> 1;
						                ^
					Syntax error on token ";", case expected after this token
					----------
					2. ERROR in X.java (at line 6)
						(I)()->();
						  ^^^^^
					Syntax error on token(s), misplaced construct(s)
					----------
					3. ERROR in X.java (at line 6)
						(I)()->();
						        ^
					Syntax error, insert ")" to complete Expression
					----------
					4. ERROR in X.java (at line 6)
						(I)()->();
						        ^
					Syntax error, insert ":" to complete SwitchLabel
					----------
					""");
	}
	public void _testBug565844SwitchConst_07() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case switch(1) {default -> 2;} -> false;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void _testBug565844SwitchConst_08() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case switch(1) {case 1 -> 2; default -> 0;} -> false;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void _testBug565844SwitchConst_09() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case switch(1) {default -> 2;}, switch(2) {default -> 3;}  -> false;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void _testBug565844SwitchConst_10() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public final static int j = 5;
						    public static void main(String argv[]) {
						    	boolean b =\s
						    			switch (j) {
						    				case switch(1) {case 1 -> 2; default -> 0;},\
						 							switch(2) {case 1 -> 3; default -> 4;}  -> false;
						    				default -> false;
						    			};\s
						    	System.out.println(b);
						    }
						}"""
				},
				"false");
	}
	public void testBug566125_01() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 -> new Short((short)0);
								    	case 2 -> new Double(2.0d);
								    	default -> new Integer((short)6);
							    	});
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							    boolean foo(double data){ return true; }
							}"""
				},
				"true");

	}
	// Same as above, but with explicit yield
	public void testBug566125_02() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 : yield new Short((short)0);
								    	case 2 : yield new Double(2.0d);
								    	default : yield new Integer((short)6);
							    	});
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							    boolean foo(double data){ return true; }
							}"""
				},
				"true");

	}
	public void testBug566125_03() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 -> new Short((short)0);
								    	case 2 -> 2.0d;
								    	default -> new Integer((short)6);
							    	});
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							    boolean foo(double data){ return true; }
							}"""
				},
				"true");

	}
	// Long -> float is accepted
	public void testBug566125_04() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 -> new Integer((short)0);
								    	default -> 2l;
							    	});
							    	System.out.println(b);
							    }
								boolean foo(int data){ return false; }
							    boolean foo(long data){ return true; }
							}"""
				},
				"true");

	}
	public void testBug566125_05() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo(
							    				switch(i%2)  {
							    					case 1 -> switch(i) {
							    								case 1 -> new Byte((byte)1);
							    								case 3 -> new Float(3);
							    								case 5 -> new Long(5);
							    								default -> new Short((short)6);
							    							};\s
							    					default -> switch(i) {
																case 0 -> new Integer((byte)2);
																case 2 -> new Double(4);
																case 4 -> new Long(6);
																default -> new Short((short)8);
							    							};
							    				}
							    			);
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							    boolean foo(double data){ return true; }
							}"""
				},
				"true"
				);

	}
	public void testBug566125_06() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 -> Short.valueOf((short)0);
								    	default -> Double.valueOf(2.0d);
							    	});
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						boolean b = foo( switch(i+1) {
						            ^^^
					The method foo(short) in the type X is not applicable for the arguments (double)
					----------
					2. ERROR in X.java (at line 9)
						default -> Double.valueOf(2.0d);
						           ^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from Double to short
					----------
					"""
				);
	}
	public void testBug566125_07() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 -> Short.valueOf((short)0);
								    	default -> 2.0d;
							    	});
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						boolean b = foo( switch(i+1) {
						            ^^^
					The method foo(short) in the type X is not applicable for the arguments (double)
					----------
					2. ERROR in X.java (at line 9)
						default -> 2.0d;
						           ^^^^
					Type mismatch: cannot convert from double to short
					----------
					"""
				);
	}
	// Same as 07() but with explicit yield
	public void testBug566125_08() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X  {
								public static void main(String[] args) {
									new X().bar(0);
								}
							    @SuppressWarnings("deprecation")
							    public void bar(int i) {
									boolean b = foo( switch(i+1) {
								    	case 0 : yield Short.valueOf((short)0);
								    	default : yield 2.0d;
							    	});
							    	System.out.println(b);
							    }
							    boolean foo(short data){ return false; }
							    boolean foo(byte data){ return false; }
							    boolean foo(int data){ return false; }
							    boolean foo(float data){ return false; }
							    boolean foo(long data){ return false; }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						boolean b = foo( switch(i+1) {
						            ^^^
					The method foo(short) in the type X is not applicable for the arguments (double)
					----------
					2. ERROR in X.java (at line 9)
						default : yield 2.0d;
						                ^^^^
					Type mismatch: cannot convert from double to short
					----------
					"""
				);
	}
	public void testBug567112_001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.util.ArrayList;
							
							public class X {
							    public void foo() {
							        new ArrayList<>().stream().filter(p -> p != null)
							        switch ("") {
							        case "":
							        }
							    }
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						new ArrayList<>().stream().filter(p -> p != null)
						                                            ^^^^^
					Syntax error on tokens, delete these tokens
					----------
					2. ERROR in X.java (at line 8)
						}
						^
					Syntax error, insert ")" to complete Expression
					----------
					3. ERROR in X.java (at line 8)
						}
						^
					Syntax error, insert ";" to complete BlockStatements
					----------
					"""
				);
	}
	public void testBug571833_01() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static int foo(int a) {
					   int b = (int) switch (a) {
					     case 1 -> 1.0;
					     default -> 0;
					   };
					   return b;
					 }
					
					 public static void main(String[] args) {
					   int b = foo(2);
					   System.out.println(b);
					 }
					}"""
			},
			"0"
		);

	}
	public void testBug572382() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							import java.lang.invoke.MethodHandle;
							
							public class X {
							
								Object triggerBug(MethodHandle method) throws Throwable {
									return switch (0) {
									case 0 -> method.invoke("name");
									default -> null;
									};
								}
							}
							"""
				},
				(String)null
				);

	}
	public void testBug576026() {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								enum E { A }
								static class C {
									E e = E.A;
								}
								public static void main(String[] args) {
									C c = new C();
									switch (c.e) {
									case A -> {
										System.out.println("Success");
									}
									default -> System.out.println("Wrong");
									}
								}
							}""",
				},
				"Success");
	}
	public void testBug576861_001() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
					import java.util.Comparator;
					
					public class X {
					 public static void foo(Comparator<? super Long> comparator) {}
					
					 public static void main(String[] args) {
					   int someSwitchCondition = 10;
					   X.foo(switch (someSwitchCondition) {
					   case 10 -> Comparator.comparingLong(Long::longValue);
					   default -> throw new IllegalArgumentException("Unsupported");
					 });
					   System.out.println("hello");
					 }
					}"""
				},
				"hello");
	}
	public void testBug577220_001() {
		this.runNegativeTest(
			new String[] {
				"module-info.java",
				"""
					public class X {
					 void main(Integer i) {
					   Object a = switch (i) {
					   default -> {
					     yield i.toString();
					   }
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in module-info.java (at line 1)
					public class X {
					             ^
				The public type X must be defined in its own file
				----------
				2. ERROR in module-info.java (at line 5)
					yield i.toString();
					       ^
				Syntax error on token ".", ; expected
				----------
				3. ERROR in module-info.java (at line 7)
					}
					^
				Syntax error, insert ";" to complete BlockStatements
				----------
				""");
	}
	public void testIssue966_001() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
					public class X {
					    private static final String SOME_CONSTANT = "PASS";
					    public static void main(String[] args) {
					        switch ("") {
					            case (SOME_CONSTANT) -> {}
					            default -> {}
					        }
					        System.out.println(SOME_CONSTANT);
					    }
					}"""
				},
				"PASS");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/53
	// continue without label is incorrectly handled in a switch expression
	public void testGHIssue53() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						interface I {
							void foo();
						}
						public static String string = "a";
					
						public static void main(String[] args) {
							loop: for (;;) {
								System.out.println("In loop before switch");
							\t
								int result = 123 + switch (string) {
								case "a" -> {
									if (string == null)
										continue; // incorrectly compiles in JDT
									else\s
										continue loop; // correctly flagged as error ("Continue out of switch
									// expressions not permitted")
									// javac (correctly) outputs "error: attempt to continue out of a switch
									// expression" for both continue statements
									yield 789;
								}
								default -> 456;
								};
								System.out.println("After switch. result: " + result);
							}
						}
					}
					"""

				},
				"""
					----------
					1. WARNING in X.java (at line 8)
						loop: for (;;) {
						^^^^
					The label loop is never explicitly referenced
					----------
					2. ERROR in X.java (at line 14)
						continue; // incorrectly compiles in JDT
						^^^^^^^^^
					Continue out of switch expressions not permitted
					----------
					3. ERROR in X.java (at line 16)
						continue loop; // correctly flagged as error ("Continue out of switch
						^^^^^^^^^^^^^^
					Continue out of switch expressions not permitted
					----------
					""");
	}

	public void testGH520() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test(int i) {
							foo(switch (i) {
								case 0 -> m.call();
								default -> null;
							});
						}
						<T> void foo(T t) { }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					foo(switch (i) {
					^^^
				The method foo(T) in the type X is not applicable for the arguments (switch (i) {
				case 0 ->
				    m.call();
				default ->
				    null;
				})
				----------
				2. ERROR in X.java (at line 4)
					case 0 -> m.call();
					          ^
				m cannot be resolved
				----------
				""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1394
	// switch statement with yield and try/catch produces EmptyStackException
	public void testGHI1394() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.ENABLED);
		this.runConformTest(
				new String[] {
				"X.java",
				"""
					public class X {
						protected static int switchWithYield() {
							String myStringNumber = "1";
							return switch (myStringNumber) {
							case "1" -> {
								try {
									yield Integer.parseInt(myStringNumber);
								} catch (NumberFormatException e) {
									throw new RuntimeException("Failed parsing number", e); //$NON-NLS-1$
								}
							}
							default -> throw new IllegalArgumentException("Unexpected value: " + myStringNumber);
							};
						}
						public static void main(String[] args) {
							System.out.println(switchWithYield());
						}
					}\s"""
				},
				"1",
				options);
	}
	public void testGHI1394_2() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.ENABLED);
		this.runConformTest(
				new String[] {
				"X.java",
				"""
					public class X {
						static String myStringNumber = "1";
						protected static int switchWithYield() {
							return switch (myStringNumber) {
							case "10" -> {
								try {
									yield Integer.parseInt(myStringNumber);
								} catch (NumberFormatException e) {
									throw new RuntimeException("Failed parsing number", e); //$NON-NLS-1$
								}
							}
							default -> throw new IllegalArgumentException("Unexpected value: " + myStringNumber);
							};
						}
						public static void main(String[] args) {
					     try {
							    System.out.println(switchWithYield());
					     } catch(IllegalArgumentException iae) {
					         if (!iae.getMessage().equals("Unexpected value: " + myStringNumber))
					             throw iae;
					     }
					     System.out.println("Done");
						}
					}\s"""
				},
				"Done",
				options);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1394
	// switch statement with yield and try/catch produces EmptyStackException
	public void testGHI1394_min() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.ENABLED);
		this.runConformTest(
				new String[] {
				"X.java",
				"""
					public class X {
						static int foo() {
							int x = 1;
							return switch (x) {
							case 1 -> {
								try {
									yield x;
								} finally  {
								\t
								}
							}
							default -> throw new RuntimeException("" + x + " ".toLowerCase());
							};
						}
						public static void main(String[] args) {
							System.out.println(foo());
						}
					}
					"""
				},
				"1",
				options);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1727
	// Internal compiler error: java.util.EmptyStackException
	public void testGHI1727() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
					class X {
						private Object foo(Object value) {
							return switch (value) {
								case String string -> {
									try {
										yield string;
									} catch (IllegalArgumentException exception) {
										yield string;
									}
								}
								default -> throw new IllegalArgumentException("Argument of type " + value.getClass());
							};
						}
					}
					"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1686
	// Switch statement with yield in synchronized and try-catch blocks results in ArrayIndexOutOfBoundsException
	public void testGHI1686() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					public String demo(String input) {
						return switch (input) {
							case "red" -> {
								synchronized (this) {
									yield "apple";
								}
							}
							default -> {
								try {
									yield "banana";
								}
								catch (Exception ex) {
									throw new IllegalStateException(ex);
							    }
						    }
					    };
				    }
				}
				"""
				},
				"");
		}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1686
	// Switch statement with yield in synchronized and try-catch blocks results in ArrayIndexOutOfBoundsException
	public void testGHI1686_works() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					public String demo(String input) {
						return switch (input) {
							case "red" -> {
								synchronized (this) {
									yield "apple";
								}
							}
							default -> {
								yield "banana";
						    }
					    };
				    }
				}
				"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1767
	// NPE in switch with Enum
	public void testGHI1767() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static enum TestEnum {
						E1,
						E2,
						E3
					}

					public static void main(String[] theArgs) {
						System.out.println("Test case 1");
						new X().test1SwitchWithNull(TestEnum.E1);
						new X().test1SwitchWithNull(TestEnum.E2);
						new X().test1SwitchWithNull((TestEnum) null);

						System.out.println("Test case 2");
						new X().test2SwitchWithNull(TestEnum.E1);
						new X().test2SwitchWithNull(TestEnum.E2);
						new X().test2SwitchWithNull((TestEnum) null);

						System.out.println("Test case 3");
						new X().test3SwitchWithNull(TestEnum.E1);
						new X().test3SwitchWithNull(TestEnum.E2);
						new X().test3SwitchWithNull((TestEnum) null);
					}

					private void test1SwitchWithNull(TestEnum theEnum) {
						switch (theEnum) {
							case TestEnum e when e == TestEnum.E1 -> System.out.println(e);
							case null -> System.out.println("Enum: null");
							default -> System.out.println("Enum: default");
						}
					}

					private void test2SwitchWithNull(TestEnum theEnum) {
						switch (theEnum) {
							case TestEnum e -> System.out.println(e);
							case null -> System.out.println("Enum: null");
						}
					}

					private void test3SwitchWithNull(TestEnum theEnum) {
						switch (theEnum) {
							case TestEnum.E1 -> System.out.println(theEnum);
							case null -> System.out.println("Enum: null");
							default -> System.out.println("Enum: default -> " + theEnum);
						}
					}
				}
				"""
				},
				"""
					Test case 1
					E1
					Enum: default
					Enum: null
					Test case 2
					E1
					E2
					Enum: null
					Test case 3
					E1
					Enum: default -> E2
					Enum: null""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1767
	// NPE in switch with Enum
	public void testGHI1767_minimal() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					enum E {
						E
					}

					public static void main(String[] args) {
						E e = null;
						switch(e) {
						case E.E -> {}
						case null -> {}
						}
					}
				}
				"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return element instanceof String data ?
							switch (columnIndex) {
								case 0 -> data;
								case 1 -> data.toUpperCase();
								default -> "Done";
							} : "";
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 0));
						System.out.println(method("Blah", 1));
						System.out.println(method("Blah", 10));
					}
				}
				"""
				},
				"""
					Blah
					BLAH
					Done""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_2() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return element instanceof String data ? "" :
							switch (columnIndex) {
								case 0 -> data;
								case 1 -> data.toUpperCase();
								default -> "";
							};
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 1));
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case 0 -> data;
						          ^^^^
					data cannot be resolved to a variable
					----------
					2. ERROR in X.java (at line 6)
						case 1 -> data.toUpperCase();
						          ^^^^
					data cannot be resolved
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_3() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return element instanceof String data ?
							switch (columnIndex) {
								case 0 -> { yield data; }
								case 1 -> data.toUpperCase();
								default -> "";
							} : "";
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 1));
					}
				}
				"""
				},
				"BLAH");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_4() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return !(element instanceof String data) ? "" :
							switch (columnIndex) {
								case 0 -> data;
								case 1 -> data.toUpperCase();
								default -> "Done";
							};
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 0));
						System.out.println(method("Blah", 1));
						System.out.println(method("Blah", 10));
					}
				}
				"""
				},
				"""
					Blah
					BLAH
					Done""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_5() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						if (element instanceof String string) {
							return element instanceof String data ?
								switch (columnIndex) {
									case 0 -> data;
									case 1 -> string.toUpperCase();
									default -> "Done";
								} : "";
						}
						return null;
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 0));
						System.out.println(method("Blah", 1));
						System.out.println(method("Blah", 10));
					}
				}
				"""
				},
				"""
					Blah
					BLAH
					Done""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/377
	// [switch-expression] Invalid compiler error with switch expression
	public void testGH377() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  enum TestEnum {
				    A, B;
				  }

				  @SuppressWarnings("unused")
				  private int switcher(final TestEnum e) throws Exception {
				    return switch (e) {
				      case A -> 0;
				      case B -> {
				        try {
				          yield 1;
				        } finally {
				          throwingFn();
				        }
				      }
				    };
				  }

				  private void throwingFn() throws Exception {}

				  public static void main(String [] args) throws Exception {
					  System.out.println("Switcher :" + new X().switcher(TestEnum.A));
					  System.out.println("Switcher :" + new X().switcher(TestEnum.B));
				  }
			    }
				"""
				},
				"Switcher :0\n"
				+ "Switcher :1");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2231
	// [Switch-Expression] Assertion failure compiling array access + switch expressions with try blocks
	public void testIssue2231() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void foo(String... ss) {
						System.out.println("Entry = " + switch(ss) {
															case null -> "None";
															case String [] s when s.length == 0 -> "none";
															default ->  {
																try {
																	yield ss[0];
																} finally {

																}
															}
						});
					}

					public static void main(String[] args) {
						foo("Hello");
					}
				}
				"""
				},
				"Entry = Hello");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2231
	// [Switch-Expression] Assertion failure compiling array access + switch expressions with try blocks
	public void testIssue2231_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void foo(String [] ss) {
						System.out.println("Entry = " + switch(ss) {
															case null -> "None";
															case String [] s when s.length == 0 -> "none";
															default ->  {
																try {
																	yield ss[0];
																} finally {

																}
															}
						});
					}

					public static void main(String[] args) {
						foo(new String [] { "Hello" });
					}
				}
				"""
				},
				"Entry = Hello");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					int k;
					void foo() {
						new X() {
							{
								System.out.println ("Switch Expr = " +  switch (X.this.k) {
								default -> {
									try {
										yield throwing();
									} catch (NumberFormatException nfe) {
										yield 10;
									}
									finally {
										System.out.println("Finally");
									}
								}
							});
							}

							private Object throwing() {
								throw new NumberFormatException();
							}
						};
					}

					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					int k;
					void foo() {
						int fooLocal = 10;
						class Local {
							Local() {
								System.out.println("Switch result = " + switch(X.this.k) {
																			default -> {
																				try {
																					System.out.println("Try");
																					yield 10;
																				} catch (Exception e) {
																					System.out.println("Catch");
																					yield 20;
																				} finally {
																					System.out.println("Finally");
																				}
																			}
																		});
							}
						}
						new Local();
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
				},
				"""
					Try
					Finally
					Switch result = 10""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_3() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					int k;

					{
						System.out.println ("Switch Expr = " +  switch (k) {
						default -> {
							try {
								yield throwing();
							} catch (NumberFormatException nfe) {
								yield 10;
							}
							finally {
								System.out.println("Finally");
							}
						}
					});
					}
					private Object throwing() {
						throw new NumberFormatException();
					}
					public static void main(String[] args) {
						new X();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_4() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						args = new String [] { "one" , "two"};
						System.out.println(switch (args) {
							case null ->  0;
							default -> switch(args.length) {
											case 0 -> 0;
											case 1 -> "One";
											default -> new X();
										};
							});
					}
					public String toString() {
						return "some X()";
					}
				}
				"""
				},
				"some X()");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_5() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					int k;
					void foo() {
						String val = "123";
						new X() {
							{
								System.out.println ("Switch Expr = " +  switch (X.this.k) {
								default -> {
									try {
										yield throwing();
									} catch (NumberFormatException nfe) {
										yield val;
									}
									finally {
										System.out.println("Finally");
									}
								}
							});
							}

							private Object throwing() {
								throw new NumberFormatException();
							}
						};
					}

					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_6() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					static int k;

					static {
						System.out.println ("Switch Expr = " +  switch (k) {
						default -> {
							try {
								yield throwing();
							} catch (NumberFormatException nfe) {
								yield 10;
							}
							finally {
								System.out.println("Finally");
							}
						}
					});
					}
					private static Object throwing() {
						throw new NumberFormatException();
					}
					public static void main(String[] args) {
						new X();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2233
	// [Switch-Expression] Assertion failure while compiling enum class that uses switch expression with try block
	public void testIssue2233() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public enum X {
					PARAMETER, FIELD, METHOD;
					X() {
						System.out.println(switch (this) {
												default -> {
													try {
														yield 10;
													} finally {

													}
												}
											});
					}

				    public static void notmain(String [] args) {
				        X x = PARAMETER;
				        System.out.println(x);
				    }
				}
				"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2322
	// [Switch Expression] Internal compiler error: java.util.EmptyStackException at java.base/java.util.Stack.peek
	public void testIssue2322() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String [] args) {
				    int lineCount = 10;
				    long time = 1000;
				    print((int) (lineCount * 10000.0 / time));
				    print((double) (lineCount * 10000.0 / time));
				    System.out.println(switch(lineCount) {
				        default -> {
				    	try {
				    		yield "OK";
				    	} finally {

				    	}
				        }
				    });
				  }
				  static void print(double d) {}
				}
				"""
				},
				"OK");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2335
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException: class org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding cannot be cast to class org.eclipse.jdt.internal.compiler.lookup.ArrayBinding
	public void testIssue2335() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public final class X {

				  public void show() {

				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;

				    short[][][] array = new short[size1][size2][size3];

				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? (short) 1 : (short) 0;
				        }
				      }
				    }
				    System.out.println(switch(42) {
				    	default -> {
				    		try {
				    			yield 42;
				    		} finally {

				    		}
				    	}
				    });

				  }

				  public static void main(String[] args) {
				    new X().show();
				  }
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2335
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException: class org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding cannot be cast to class org.eclipse.jdt.internal.compiler.lookup.ArrayBinding
	public void testIssue2335_min() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public final class X {
				  public static void main(String[] args) {
					   short[] array = new short[10];

					    for (int i = 0; i < 10; i++) {
					        boolean on = false;
					          array[i] = on ? (short) 1 : (short) 0;
					    }
					    System.out.println(switch(42) {
					    	default -> {
					    		try {
					    			yield 42;
					    		} finally {

					    		}
					    	}
					    });
				  }
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2335
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException: class org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding cannot be cast to class org.eclipse.jdt.internal.compiler.lookup.ArrayBinding
	public void testIssue2335_other() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						System.out.println(switch (1) {
						default -> {
							try {
								System.out.println(switch (10) { default -> { try { yield 10; } finally {} } });
							} finally {}
							yield 1;
						}
						});
					}
					X() {}
				}
				"""
				},
				"10\n" +
				"1");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2349
	// [Switch Expression] Verify error at runtime with switch expression and exception handling inside lambda expression
	public void testIssue2349() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					int doit();
				}
				public class X {
					public static void main(String[] args) {
						I i = () -> {
							return 10 + switch (10) {
								default -> { try { yield 32; } catch (NullPointerException npe) { yield -10; } }
						};
						};
						System.out.println(i.doit());
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2360
	// [Switch Expression] Internal compiler error: java.lang.NullPointerException: Cannot read field "binding" because "this.methodDeclaration" is null
	public void testIssue2360() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					void foo(int p, int q);
				}
				public class X {
				   int f;
					void foo(int a) {
				       int loc = 10;
						I i = (int p, int q)  -> {
				           I i2 = new I() { public void foo(int f, int p0) {};};
				           System.out.println(10 + switch (10) {
							default -> { try { yield 32; } catch (NullPointerException npe) { yield -10; } }});
							System.out.println(10 + switch (loc) {
							default -> { try { yield 0; } catch (NullPointerException npe) { yield -10; } }});
							System.out.println(10 + switch (p) {
							default -> { try { yield p; } catch (NullPointerException npe) { yield -10; } }});
							System.out.println(10 + switch (q) {
							default -> { try { yield q; } catch (NullPointerException npe) { yield -10; } }});
						};
						i.foo(10,  20);
					}

					public static void main(String[] args) {
						new X().foo(42);
					}
				}
				"""
				},
				"""
					42
					10
					20
					30""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2360
	// [Switch Expression] Internal compiler error: java.lang.NullPointerException: Cannot read field "binding" because "this.methodDeclaration" is null
	public void testIssue2360_2() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					void foo(int p, int q);
				}
				public class X {
				   int f;
					void foo(int a) {
				       int loc;
						I i = (int p, int q)  -> {
				           I i2 = new I() { public void foo(int f, int p0) {};};
				           System.out.println(10 + switch (10) {
							default -> { try { yield 32; } catch (NullPointerException npe) { yield -10; } }});
						};
						i.foo(10,  20);
					}

					public static void main(String[] args) {
						new X().foo(42);
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2363
	// [Switch Expressions] Compiler crashes with Switch expressions mixed with exception handling
	public void testIssue2363() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String argv[]) {
						System.out.println(void.class == Void.TYPE);
						System.out.println(switch(42) {
				    	default -> {
				    		try {
				    			yield 42;
				    		} finally {

				    		}
				    	}
				    });

					}
				}
				"""
				},
				"true\n42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2366
	// [Switch Expression] Assertion fails when IDE is launched with JVM option -ea
	public void testIssue2366() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				  public X() {
				    super();
				  }
				  public static void foo() {
				    X z;
				    while (((z = getObject()) != null))      {
				        z.bar();
				      }
				    System.out.println(switch(42) {
					  default -> {
						try {
							yield 42;
						} finally {

						}
					  }
				    });
				  }
				  public void bar() {
				  }
				  public static X getObject() {
				    return null;
				  }
				  public static void main(String[] args) {
				    new X().foo();
				  }
				}
				"""
				},
				"42");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    double d = 3;

				    double r = switch (d) {
				      case 1.0 -> 0.2;
				      case 2.0 -> 0.5;
				      case 8.0 -> 2;
				      case 9.0 -> 3;
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						double r = switch (d) {
						                   ^
					Cannot switch on a value of type double. Only convertible int values, strings or enum variables are permitted
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_2() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    long d = 3;

				    double r = switch (d) {
				      case 1.0 -> 0.2;
				      case 2.0 -> 0.5;
				      case 8.0 -> 2;
				      case 9.0 -> 3;
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						double r = switch (d) {
						                   ^
					Cannot switch on a value of type long. Only convertible int values, strings or enum variables are permitted
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_3() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    float d = 3;

				    double r = switch (d) {
				      case 1.0 -> 0.2;
				      case 2.0 -> 0.5;
				      case 8.0 -> 2;
				      case 9.0 -> 3;
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						double r = switch (d) {
						                   ^
					Cannot switch on a value of type float. Only convertible int values, strings or enum variables are permitted
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_4() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    boolean d = true;

				    double r = switch (d) {
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						double r = switch (d) {
						                   ^
					Cannot switch on a value of type boolean. Only convertible int values, strings or enum variables are permitted
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_5() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    double d = 3;

				    switch (d) {
				      case 1.0 -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						switch (d) {
						        ^
					Cannot switch on a value of type double. Only convertible int values, strings or enum variables are permitted
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_6() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (foo()) {
				      case 1.0 -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				this.complianceLevel < ClassFileConstants.JDK21 ?
				"""
					----------
					1. ERROR in X.java (at line 7)
						switch (foo()) {
						        ^^^^^
					Cannot switch on a value of type void. Only convertible int values, strings or enum variables are permitted
					----------
					""" :
						"""
							----------
							1. ERROR in X.java (at line 8)
								case 1.0 -> System.out.println(d);
								     ^^^
							Type mismatch: cannot convert from double to void
							----------
							""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_7() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (null) {
				      case null -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				this.complianceLevel < ClassFileConstants.JDK21 ?
				"""
					----------
					1. ERROR in X.java (at line 7)
						switch (null) {
						        ^^^^
					Cannot switch on a value of type null. Only convertible int values, strings or enum variables are permitted
					----------
					2. ERROR in X.java (at line 8)
						case null -> System.out.println(d);
						     ^^^^
					The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
					----------
					""" :
						"""
							----------
							1. ERROR in X.java (at line 7)
								switch (null) {
								        ^^^^
							An enhanced switch statement should be exhaustive; a default label expected
							----------
							""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_8() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (null) {
				      case null -> System.out.println(d);
				      default -> System.out.println("Default");
				    };

				  }

				  X() {}

				}
				"""
				},
				"3.0");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_9() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (foo()) {
				      case null -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				this.complianceLevel < ClassFileConstants.JDK21 ?
				"""
					----------
					1. ERROR in X.java (at line 7)
						switch (foo()) {
						        ^^^^^
					Cannot switch on a value of type void. Only convertible int values, strings or enum variables are permitted
					----------
					2. ERROR in X.java (at line 8)
						case null -> System.out.println(d);
						     ^^^^
					The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
					----------
					""" :
						"""
							----------
							1. ERROR in X.java (at line 7)
								switch (foo()) {
								        ^^^^^
							An enhanced switch statement should be exhaustive; a default label expected
							----------
							2. ERROR in X.java (at line 8)
								case null -> System.out.println(d);
								     ^^^^
							Type mismatch: cannot convert from null to void
							----------
							""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2387
	// [Switch Expression] Empty Stack exception compiling switch expression with exception handling
	public void testIssue2387() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static class Y {}
					void foo() {
						new X().new Y(){};
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						new X().new Y(){};
						^^^^^^^
					Illegal enclosing instance specification for type X.Y
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2423
	// [Switch-expression] Internal compiler error: java.lang.ClassCastException while compiling switch expression with exception handling
	public void testIssue2423() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String getString(int i) {
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
						return new String[] { "Hello", "World" }[i];
					}
					public static void main(String [] args) {
						System.out.println(getString(0));
					}
				}
				"""
				},
				"42\n"
				+ "Hello");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2447
	// [Switch-expressions] Internal inconsistency warning at compile time and verify error at runtime
	public void testIssue2447() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					static void foo(long l) {

					}
					public static void main(String[] args) {
						long [] larray = { 10 };

						foo(larray[0] = 10);
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2451
	// Internal compiler error: java.lang.AssertionError: Anomalous/Inconsistent operand stack!
	public void testIssue2451() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				import java.util.HashMap;
				import java.util.Map;

				public class X {

					static void foo(long l) {

					}

					private static Map<String, Long> getLevelMapTable() {
						Map<String, Long> t = new HashMap<>();
						t.put(null, 0l);


						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
						return null;
					}

					public static void main(String[] args) {
						getLevelMapTable();
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2453
	// [Switch-expressions] Internal inconsistency warning at compile time and verify error at runtime
	public void testIssue2453() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						System.out.println(double.class);
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"double\n42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2455
	// [Switch-expressions] java.lang.VerifyError: Bad type on operand stack
	public void testIssue2455() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static void foo(String s, int i) {
						System.out.println("String = " + s + " int = " + i);
					}
					public static void main(String[] args) {

						foo("Hello", switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"String = Hello int = 42");
	}
}
