/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@PreviewTest
public class ValueClassesAndObjectsTest extends AbstractRegressionTestCommon {
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssue3536" };
	}
	public static Class<?> testClass() {
		return ValueClassesAndObjectsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_28);
	}
	public ValueClassesAndObjectsTest(String testName) {
		super(testName);
	}

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 28");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}

	@Override
	protected JavacTestOptions getJavacTestOptions() {
		return JAVAC_OPTIONS;
	}

	// =================================================
	// https://cr.openjdk.org/~dlsmith/jep401/latest/
    public void testValueTypes_001() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				 public class X {
				    public static void main(String[] args) {
						System.out.println("");
					}
				}
				class value {}
			"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	class value {}\n" +
			"	      ^^^^^\n" +
			"\'value\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 28\n" +
			"----------\n");
	}

    public void testValueTypes_002() {
		runConformTest(new String[] {
			"X.java",
				"""
				 public value class X {
				    public static void main(String[] args) {
				    	System.out.println("Ok!");
					}
				}
			"""
			},
			"Ok!");
	}

    // Snippet from https://openjdk.org/jeps/401 illustrating or lack/presence of identity - with preview turned on for compiler and runtime
    public void testValueness() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
			import java.time.LocalDate;
			import java.util.Objects;

			public class X {
			  public static void main(String[] args){
				  Integer x = 1996, y = 1996;
				  System.out.println(x == y);
				  System.out.println(Objects.hasIdentity(x));

				  LocalDate d1 = LocalDate.of(1996, 1, 23);
				  System.out.println(d1);
				  LocalDate d2 = d1.plusYears(30);
				  System.out.println(d2);
				  LocalDate d3 = d2.minusYears(30);
				  System.out.println(d3);
				  System.out.println(d1 == d3);
				  System.out.println(Objects.hasIdentity(d1));


				  String s = "abcd";
				  System.out.println(Objects.hasIdentity(s));
				  String t = "aabcd".substring(1);
				  System.out.println(s == t);
				  System.out.println(s.equals(t));
			  }
			}
 			"""
 			},
			"true\n" +
			"false\n" +
			"1996-01-23\n" +
			"2026-01-23\n" +
			"1996-01-23\n" +
			"true\n" +
			"false\n" +
			"true\n" +
			"false\n" +
			"true");
 	}

    // Same snippet as above but with preview turned off for both compiler and runtime
    public void testIdentityfulness() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
			import java.time.LocalDate;
			import java.util.Objects;

			public class X {
			  public static void main(String[] args){
				  Integer x = 1996, y = 1996;
				  System.out.println(x == y);
				  System.out.println(Objects.hasIdentity(x));

				  LocalDate d1 = LocalDate.of(1996, 1, 23);
				  System.out.println(d1);
				  LocalDate d2 = d1.plusYears(30);
				  System.out.println(d2);
				  LocalDate d3 = d2.minusYears(30);
				  System.out.println(d3);
				  System.out.println(d1 == d3);
				  System.out.println(Objects.hasIdentity(d1));


				  String s = "abcd";
				  System.out.println(Objects.hasIdentity(s));
				  String t = "aabcd".substring(1);
				  System.out.println(s == t);
				  System.out.println(s.equals(t));
			  }
			}
 			"""
 			},
			"false\n" +
			"true\n" +
			"1996-01-23\n" +
			"2026-01-23\n" +
			"1996-01-23\n" +
			"false\n" +
			"true\n" +
			"true\n" +
			"false\n" +
			"true",
 			getCompilerOptions(false), new String[] {}, new JavacTestOptions("-source 28"));
 	}

    // Same snippet as above but with preview turned off for compiler and turned on for runtime
    public void testValueness_without_compiler_preview_with_runtime_preview() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
			import java.time.LocalDate;
			import java.util.Objects;

			public class X {
			  public static void main(String[] args){
				  Integer x = 1996, y = 1996;
				  System.out.println(x == y);
				  System.out.println(Objects.hasIdentity(x));

				  LocalDate d1 = LocalDate.of(1996, 1, 23);
				  System.out.println(d1);
				  LocalDate d2 = d1.plusYears(30);
				  System.out.println(d2);
				  LocalDate d3 = d2.minusYears(30);
				  System.out.println(d3);
				  System.out.println(d1 == d3);
				  System.out.println(Objects.hasIdentity(d1));


				  String s = "abcd";
				  System.out.println(Objects.hasIdentity(s));
				  String t = "aabcd".substring(1);
				  System.out.println(s == t);
				  System.out.println(s.equals(t));
			  }
			}
 			"""
 			},
			"true\n" +
			"false\n" +
			"1996-01-23\n" +
			"2026-01-23\n" +
			"1996-01-23\n" +
			"true\n" +
			"false\n" +
			"true\n" +
			"false\n" +
			"true",
			getCompilerOptions(false), VMARGS, new JavacTestOptions("-source 28"));
 	}
    // Same snippet as above but with preview turned on for compiler and turned off for runtime
    public void testValueness_with_compiler_preview_without_runtime_preview() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions(true); // preview enabled
		runner.testFiles = new String[] {
				"X.java",
				"""
				import java.time.LocalDate;
				import java.util.Objects;

				public class X {
				  public static void main(String[] args){
					  Integer x = 1996, y = 1996;
					  System.out.println(x == y);
					  System.out.println(Objects.hasIdentity(x));

					  LocalDate d1 = LocalDate.of(1996, 1, 23);
					  System.out.println(d1);
					  LocalDate d2 = d1.plusYears(30);
					  System.out.println(d2);
					  LocalDate d3 = d2.minusYears(30);
					  System.out.println(d3);
					  System.out.println(d1 == d3);
					  System.out.println(Objects.hasIdentity(d1));


					  String s = "abcd";
					  System.out.println(Objects.hasIdentity(s));
					  String t = "aabcd".substring(1);
					  System.out.println(s == t);
					  System.out.println(s.equals(t));
				  }
				}
				"""
			};
		runner.javacTestOptions = JAVAC_OPTIONS;
//		runner.vmArguments = VMARGS; not passing --enable-preview to java
		runner.expectedErrorString =
				"""
				java.lang.UnsupportedClassVersionError: Preview features are not enabled for X (class file version 72.65535). Try running with '--enable-preview'
				""";
		runner.runConformTest();
	}

    // Snippet from https://openjdk.org/jeps/401 - test synchronization - compile time
    public void testSynchronizationCompileTime() {
    	runNegativeTest(new String [] {
 				"X.java",
 				"""
 				import java.time.LocalDate;

 				public class X {
 				  public static void main(String[] args){
 					  LocalDate d1 = LocalDate.of(1996, 1, 23);
 					  synchronized (d1) { d1.notify(); }
 				  }
 				}
 				"""},
    			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	synchronized (d1) { d1.notify(); }\n" +
				"	              ^^\n" +
				"Illegal attempt to synchronize on an instance of a value class\n" +
				"----------\n");

 	}
    // Snippet from https://openjdk.org/jeps/401 - test synchronization - run time
    public void testSynchronizationRunTime() {
    	runConformTest(new String [] {
 				"X.java",
 				"""
 				import java.time.LocalDate;

 				public class X {
 				  public static void main(String[] args){
 					  LocalDate d1 = LocalDate.of(1996, 1, 23);
 					  Object o = d1;
 					  try {
 					      synchronized (o) { d1.notify(); }
				      } catch (IdentityException e) {
				          System.out.println("All well!");
				      }
 				  }
 				}
 				"""},
    			"All well!");
 	}
    // Test subclassing - legal and illegal scenarios
    // Snippet from https://openjdk.org/jeps/401 - test synchronization - compile time
    public void testSubclassing() {
    	runNegativeTest(new String [] {
 				"X.java",
 				"""
				import java.io.IOException;

				// Legal subclassing scenarios
				value class V1 {} // implicit extension of jlO
				abstract value class V2 extends Object {} // explicit extension of jlO
				value class V3 extends java.lang.Object {} // explicit extension of explicitly spelled out jlO
				value class V4 extends java.lang.Number { // concrete extension of abstract value class

					private static final long serialVersionUID = 1L;

					@Override
					public int intValue() {
						return 0;
					}

					@Override
					public long longValue() {
						return 0;
					}

					@Override
					public float floatValue() {
						return 0;
					}

					@Override
					public double doubleValue() {
						return 0;
					}
				}
				abstract value class V5 extends Number { // abstract extension of abstract value class
					private static final long serialVersionUID = 1L;
				}

				value class V6 implements java.io.Serializable { // A value class may implement interfaces
					private static final long serialVersionUID = 1L;
				}
				class I1 extends V2 {} // identity class may subclass an abstract value class
				value record VPoint(int x, int y) {} // Legal value record

				// negative tests below
				value class V7 extends String {} // cannot subclass concrete identity class
				value class V8 extends java.util.Map<String, String> {} // a super class must be a class
				value class V9 extends java.io.InputStream { // cannot subclass abstract identity class

				    @Override
				    public int read() throws IOException {
				        return 0;
				    }}
				value class V10 extends V3 {} // cannot subclass a concrete value class
				class I2 extends V1 {} // identity class cannot subclass a concrete value class.
				abstract value class V11 extends java.util.ArrayList<String> { // abstract value class may not subclass concrete identity class
					private static final long serialVersionUID = 1L;
				}
 				"""},
    			"----------\n" +
				"1. ERROR in X.java (at line 42)\n" +
				"	value class V7 extends String {} // cannot subclass concrete identity class\n" +
				"	                       ^^^^^^\n" +
				"The type V7 cannot subclass the final class String\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 43)\n" +
				"	value class V8 extends java.util.Map<String, String> {} // a super class must be a class\n" +
				"	                       ^^^^^^^^^^^^^\n" +
				"The type Map<String,String> cannot be the superclass of V8; a superclass must be a class\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 44)\n" +
				"	value class V9 extends java.io.InputStream { // cannot subclass abstract identity class\n" +
				"	                       ^^^^^^^^^^^^^^^^^^^\n" +
				"A value class may extend either java.lang.Object or an abstract value class, but not an identity class.\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 50)\n" +
				"	value class V10 extends V3 {} // cannot subclass a concrete value class\n" +
				"	                        ^^\n" +
				"The type V10 cannot subclass the final class V3\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 51)\n" +
				"	class I2 extends V1 {} // identity class cannot subclass a concrete value class.\n" +
				"	                 ^^\n" +
				"The type I2 cannot subclass the final class V1\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 52)\n" +
				"	abstract value class V11 extends java.util.ArrayList<String> { // abstract value class may not subclass concrete identity class\n" +
				"	                                 ^^^^^^^^^^^^^^^^^^^\n" +
				"A value class may extend either java.lang.Object or an abstract value class, but not an identity class.\n" +
				"----------\n");

 	}
 }