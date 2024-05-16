/*******************************************************************************
 * Copyright (c) 2021, 2021 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class InstanceofPrimaryPatternTest extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 17 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test001" };
	}

	public static Class<?> testClass() {
		return InstanceofPrimaryPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public InstanceofPrimaryPatternTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()
				&& preview) {
			defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE17Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
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
	public void test001() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void foo(Object obj) {
							if (obj instanceof String s) {
								System.out.println(s);
							}
					 \
						}
					  public static void main(String[] obj) {
							foo("Hello World!");
						}
					}
					""",
			},
			"Hello World!",
			options);
	}
	public void test002() {
		String expectedDiagnostics = this.complianceLevel < ClassFileConstants.JDK20 ?
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (obj instanceof (String s)) {
						                   ^
					Syntax error on token "(", delete this token
					----------
					2. ERROR in X.java (at line 3)
						if (obj instanceof (String s)) {
						                             ^
					Syntax error on token ")", delete this token
					----------
					""" :
							"""
								----------
								1. ERROR in X.java (at line 3)
									if (obj instanceof (String s)) {
									        ^^^^^^^^^^
								Syntax error on token "instanceof", ReferenceType expected after this token
								----------
								""";
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void foo(Object obj) {
							if (obj instanceof (String s)) {
								System.out.println(s);
							}
					 \
						}
					  public static void main(String[] obj) {
							foo("Hello World!");
						}
					}
					""",
			},
			expectedDiagnostics);
	}
	public void test003() {

		String expectedDiagnostics = this.complianceLevel < ClassFileConstants.JDK20 ?
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (obj instanceof ((String s))) {
						        ^^^^^^^^^^
					Syntax error, insert "Type" to complete InstanceofClassic
					----------
					2. ERROR in X.java (at line 3)
						if (obj instanceof ((String s))) {
						        ^^^^^^^^^^
					Syntax error, insert ") Statement" to complete BlockStatements
					----------
					3. ERROR in X.java (at line 3)
						if (obj instanceof ((String s))) {
						                     ^^^^^^
					Syntax error on token "String", ( expected after this token
					----------
					4. ERROR in X.java (at line 3)
						if (obj instanceof ((String s))) {
						                               ^
					Syntax error, insert "AssignmentOperator Expression" to complete Assignment
					----------
					5. ERROR in X.java (at line 3)
						if (obj instanceof ((String s))) {
						                               ^
					Syntax error, insert ";" to complete Statement
					----------
					""" :
									"""
										----------
										1. ERROR in X.java (at line 3)
											if (obj instanceof ((String s))) {
											                   ^
										Syntax error on token "(", invalid ReferenceType
										----------
										2. ERROR in X.java (at line 3)
											if (obj instanceof ((String s))) {
											                               ^
										Syntax error on token ")", delete this token
										----------
										""";

		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void foo(Object obj) {
							if (obj instanceof ((String s))) {
								System.out.println(s);
							}
					 \
						}
					  public static void main(String[] obj) {
							foo("Hello World!");
						}
					}
					""",
			},
			expectedDiagnostics);
	}
	public void test007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void foo(Object obj) {
							if (obj instanceof var s) {
								System.out.println(s);
							}
					 \
						}
					  public static void main(String[] obj) {
							foo("Hello World!");
							Zork();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					if (obj instanceof var s) {
					                   ^^^
				'var' is not allowed here
				----------
				2. ERROR in X.java (at line 9)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void test009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void foo(String s) {
							if (s instanceof Object o) {
								System.out.println(s1);
							}
					 \
						}
					  public static void main(String[] obj) {
							foo("Hello World!");
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					System.out.println(s1);
					                   ^^
				s1 cannot be resolved to a variable
				----------
				""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1076
	// ECJ accepts invalid Java code instanceof final Type
	public void testGH1076() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class Test {
					    void check() {
					        Number n = Integer.valueOf(1);
					        if (n instanceof final Integer) {}
					        if (n instanceof final Integer x) {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (n instanceof final Integer) {}
					                 ^^^^^^^^^^^^^
				Syntax error, modifiers are not allowed here
				----------
				""");
	}

	public void testGH1621() {
		runConformTest(
			new String[] {
				"ConsumerEndpointSpec.java",
				"""
				import java.util.function.Consumer;

				public class ConsumerEndpointSpec {
					public void setNotPropagatedHeaders(String... headers) {
						for (String s: headers) System.out.print(s);
					}
					void foo(Object h) {
						if (h instanceof ConsumerEndpointSpec producingMessageHandler) {
							acceptIfNotEmpty(new String[] {"OK"}, producingMessageHandler::setNotPropagatedHeaders);
						}
					}
					public <T> void acceptIfNotEmpty(T[] value, Consumer<T[]> consumer) {
						consumer.accept(value);
					}
					public static void main(String... args) {
						ConsumerEndpointSpec obj = new ConsumerEndpointSpec();
						obj.foo(obj);
					}
				}
				"""
			},
			"OK",
			getCompilerOptions());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						Object obj = new Object();
						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) {
						    System.out.println();
						} else {
						    throw new IllegalArgumentException("invalid type"); // works OK without this line
						}

						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
						    System.out.println();
						}
						zork();
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 17)
					zork();
					^^^^
				The method zork() is undefined for the type X
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						Object obj = new Object();
						if (obj instanceof Double c) {
						    System.out.println();
						} else {
						    throw new IllegalArgumentException("invalid type"); // works OK without this line
						}

						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
						    System.out.println();
						}
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						if (args != null) {
							Object obj = new Object();
							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
						zork();
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 19)
					zork();
					^^^^
				The method zork() is undefined for the type X
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_3() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						if (args != null) {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_4() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						try {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						} catch (Exception e) {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						} finally {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				2. ERROR in X.java (at line 26)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				3. ERROR in X.java (at line 39)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_5() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						synchronized(args) {
							Object obj = new Object();
							if (obj instanceof Double c) {
							    System.out.println();
							} else {
							    throw new IllegalArgumentException("invalid type"); // works OK without this line
							}

							if (obj instanceof Integer r) {
							    System.out.println();
							} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
							    System.out.println();
							}
						}
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_6() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
					    try {
					        gain(args);
					    } catch (IllegalArgumentException iae) {
					        if (iae.getMessage().equals("invalid type"))
					            System.out.println("All well");
					    }
					}
					public static void gain(String[] args) {
						Object obj = new Object();
						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) {
						    System.out.println();
						} else {
						    throw new IllegalArgumentException("invalid type"); // works OK without this line
						}

						if (obj instanceof Integer r) {
						    System.out.println();
						} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
						    System.out.println();
						}
					}
				}
				"""
			},
			"All well",
			getCompilerOptions());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_7() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
					    try {
					        gain(new String[] {"Hello"});
					    } catch (IllegalArgumentException iae) {
					        if (iae.getMessage().equals("invalid type"))
					            System.out.println("All well");
					    }
					}
					public static void gain(String[] args) {
						switch(args[0]) {
							case "Hello" :
								Object obj = new Object();
								if (obj instanceof Integer r) {
								    System.out.println();
								} else if (obj instanceof Double c) {
								    System.out.println();
								} else {
								    throw new IllegalArgumentException("invalid type"); // works OK without this line
								}

								if (obj instanceof Integer r) {
								    System.out.println();
								} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
								    System.out.println();
								}
						}
					}
				}
				"""
			},
			"All well",
			getCompilerOptions());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577415
	// Bug in Eclipse Pattern Matching Instanceof Variable Scope
	public void test577415_8() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
					    try {
					        gain(new String[] {"Hello"});
					    } catch (IllegalArgumentException iae) {
					        if (iae.getMessage().equals("invalid type"))
					            System.out.println("All well");
					    }
					}
					public static void gain(String[] args) {
						switch(args[0]) {
							case "Hello" :
								Object obj = new Object();
								if (obj instanceof Double c) {
								    System.out.println();
								} else {
								    throw new IllegalArgumentException("invalid type"); // works OK without this line
								}

								if (obj instanceof Integer r) {
								    System.out.println();
								} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
								    System.out.println();
								}
						}
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 22)
					} else if (obj instanceof Double c) { // Eclipse Compilation Error: Duplicate variable c
					                                 ^
				A pattern variable with the same name is already defined in the statement
				----------
				""");
	}
}