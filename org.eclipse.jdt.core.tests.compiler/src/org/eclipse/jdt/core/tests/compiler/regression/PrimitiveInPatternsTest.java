/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class PrimitiveInPatternsTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 23");
	private static final String[] VMARGS = new String[] {"--enable-preview"};
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test037" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return PrimitiveInPatternsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}
	public PrimitiveInPatternsTest(String testName) {
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
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
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE23Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
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

	// https://cr.openjdk.org/~abimpoudis/instanceof/jep455-20240424/specs/instanceof-jls.html#jls-5.1.2
	// 5.7 Testing Contexts
	// Identity Conversion
	public void test001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte foo(byte b) {
						if (b instanceof byte) {
							return b;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte foo(byte b) {
						if (b instanceof byte bb) {
							return bb;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(int i) {
						if (i instanceof int) {
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1");
	}
	public void test004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(int i) {
						if (i instanceof int) {
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1");
	}
	public void test005() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(long l) {
						if (l instanceof long) {
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						long l = 1L;
						System.out.println(X.foo(l));
					}
				}
				"""
			},
			"1");
	}
	public void test006() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(long l) {
						if (l instanceof long ll) {
							return ll;
						}
						return -1;
					}
					public static void main(String[] args) {
						long l = 1L;
						System.out.println(X.foo(l));
					}
				}
				"""
			},
			"1");
	}
	public void test007() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(float f) {
						if (f instanceof float) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						float f = 1.0f;
						System.out.println(X.foo(f));
					}
				}
				"""
			},
			"1.0");
	}
	public void test008() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(float f) {
						if (f instanceof float ff) {
							return ff;
						}
						return -1;
					}
					public static void main(String[] args) {
						float f = 1.0f;
						System.out.println(X.foo(f));
					}
				}
				"""
			},
			"1.0");
	}
	public void test009() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(double d) {
						if (d instanceof double) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						double d = 1.0;
						System.out.println(X.foo(d));
					}
				}
				"""
			},
			"1.0");
	}
	public void test010() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(double d) {
						if (d instanceof double dd) {
							return dd;
						}
						return -1;
					}
					public static void main(String[] args) {
						double d = 1.0;
						System.out.println(X.foo(d));
					}
				}
				"""
			},
			"1.0");
	}

	public void test011() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte foo() {
						if (bar() instanceof byte) {
							byte b = (byte) bar();
							return b;
						}
						return -1;
					}
					public static byte bar() {
						byte b = 1;
						return b;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test012() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte foo() {
						if (bar() instanceof byte b) {
							return b;
						}
						return -1;
					}
					public static byte bar() {
						byte b = 1;
						return b;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test013() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo() {
						if (bar() instanceof int) {
							int i = (int) bar();
							return i;
						}
						return -1;
					}
					public static int bar() {
						int i = 1;
						return i;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test014() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo() {
						if (bar() instanceof int i) {
							return i;
						}
						return -1;
					}
					public static int bar() {
						int i = 1;
						return i;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test015() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo() {
						if (bar() instanceof long) {
							long l = (long) bar();
							return l;
						}
						return -1;
					}
					public static long bar() {
						return 1L;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test016() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo() {
						if (bar() instanceof long l) {
							return l;
						}
						return -1;
					}
					public static long bar() {
						return 1L;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test017() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo() {
						if (bar() instanceof float) {
							float f = (float) bar();
							return f;
						}
						return -1;
					}
					public static float bar() {
						float f = 1.0f;
						return f;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test018() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo() {
						if (bar() instanceof float f) {
							return f;
						}
						return -1;
					}
					public static float bar() {
						float f = 1.0f;
						return f;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test019() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo() {
						if (bar() instanceof double) {
							double d = (double) bar();
							return d;
						}
						return -1;
					}
					public static double bar() {
						double d = 1.0d;
						return d;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test020() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo() {
						if (bar() instanceof double) {
							double d = (double) bar();
							return d;
						}
						return -1;
					}
					public static double bar() {
						double d = 1.0d;
						return d;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	// Widening primitive conversions
	public void test021() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static short foo(byte b) {
						if (b instanceof short) {
							short s = (short)b;
							return s;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test022() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static short foo(byte b) {
						if (b instanceof short s) {
							return s;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test023() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(byte b) {
						if (b instanceof int) {
							int i = (int)b;
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test024() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(byte b) {
						if (b instanceof int i) {
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test025() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(byte b) {
						if (b instanceof long) {
							long l = (long)b;
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test026() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(byte b) {
						if (b instanceof long l) {
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1");
	}
	public void test027() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(byte b) {
						if (b instanceof float) {
							float f = (float)b;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1.0");
	}
	public void test028() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(byte b) {
						if (b instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1.0");
	}
	public void test029() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(byte b) {
						if (b instanceof double) {
							double d = (double)b;
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1.0");
	}
	public void test030() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(byte b) {
						if (b instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b = 1;
						System.out.println(X.foo(b));
					}
				}
				"""
			},
			"1.0");
	}
	public void test031() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(short s) {
						if (s instanceof int) {
							int i = (int) s;
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1");
	}
	public void test032() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(short s) {
						if (s instanceof int i) {
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1");
	}
	public void test033() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(short s) {
						if (s instanceof long) {
							long l = (long)s;
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1");
	}
	public void test034() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(short s) {
						if (s instanceof long l) {
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1");
	}
	public void test035() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(short s) {
						if (s instanceof float) {
							float f = (float)s;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1.0");
	}
	public void test036() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(short s) {
						if (s instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1.0");
	}
	public void test037() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(short s) {
						if (s instanceof double) {
							double d = (double)s;
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1.0");
	}
	public void test038() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(short s) {
						if (s instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo(s));
					}
				}
				"""
			},
			"1.0");
	}
	public void test039() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(char c) {
						if (c instanceof int) {
							int i = (int) c;
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1");
	}
	public void test040() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo(char c) {
						if (c instanceof int i) {
							return i;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1");
	}
	public void test041() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(char c) {
						if (c instanceof long) {
							long l = (long)c;
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1");
	}
	public void test042() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(char c) {
						if (c instanceof long l) {
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1");
	}
	public void test043() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(char c) {
						if (c instanceof float) {
							float f = (float)c;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1.0");
	}
	public void test044() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(char c) {
						if (c instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1.0");
	}
	public void test045() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(char c) {
						if (c instanceof double) {
							double d = (double)c;
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1.0");
	}
	public void test046() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(char c) {
						if (c instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						char c = 1;
						System.out.println(X.foo(c));
					}
				}
				"""
			},
			"1.0");
	}
	public void test047() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(int i) {
						if (i instanceof long) {
							long l = (long)i;
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1");
	}
	public void test048() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo(int i) {
						if (i instanceof long l) {
							return l;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1");
	}
	public void test049() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float) {
							float f = (float)i;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1.0");
	}
	public void test050() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1.0");
	}
	public void test051() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(int i) {
						if (i instanceof double) {
							double d = (double)i;
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1.0");
	}
	public void test052() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(int i) {
						if (i instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"1.0");
	}
	public void test053() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(long l) {
						if (l instanceof float) {
							float f = (float)l;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						long l = 1;
						System.out.println(X.foo(l));
					}
				}
				"""
			},
			"1.0");
	}
	public void test054() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(long l) {
						if (l instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						long l = 1;
						System.out.println(X.foo(l));
					}
				}
				"""
			},
			"1.0");
	}
	public void test055() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(long l) {
						if (l instanceof double) {
							double d = (double)l;
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						long l = 1;
						System.out.println(X.foo(l));
					}
				}
				"""
			},
			"1.0");
	}
	public void test056() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(long l) {
						if (l instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						long l = 1;
						System.out.println(X.foo(l));
					}
				}
				"""
			},
			"1.0");
	}
	public void test057() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(float f) {
						if (f instanceof double) {
							double d = (double)f;
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						float f = 1.0f;
						System.out.println(X.foo(f));
					}
				}
				"""
			},
			"1.0");
	}
	public void test058() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo(float f) {
						if (f instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						float f = 1.0f;
						System.out.println(X.foo(f));
					}
				}
				"""
			},
			"1.0");
	}
	public void test059() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float) {
							float f = (float)i;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1234567890;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	public void test060() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1234567890;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	public void test061() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float) {
							float f = (float)i;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = Integer.MAX_VALUE;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	public void test062() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = Integer.MAX_VALUE;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}

	// Widening with functions
	public void test063() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static short foo() {
						if (getByte() instanceof short) {
							short s = (short) getByte();
							return s;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test064() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static short foo() {
						if (getByte() instanceof short s) {
							return s;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test065() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo() {
						if (getByte()  instanceof int) {
							int i = (int)getByte() ;
							return i;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test066() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int foo() {
						if (getByte()  instanceof int i) {
							return i;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test067() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo() {
						if (getByte()  instanceof long) {
							long l = (long)getByte() ;
							return l;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test068() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long foo() {
						if (getByte()  instanceof long l) {
							return l;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}
	public void test069() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo() {
						if (getByte() instanceof float) {
							float f = (float)getByte() ;
							return f;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test070() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo() {
						if (getByte()  instanceof float f) {
							return f;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test071() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo() {
						if (getByte()  instanceof double) {
							double d = (double)getByte() ;
							return d;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test072() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo() {
						if (getByte()  instanceof double d) {
							return d;
						}
						return -1;
					}
					public static byte getByte() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1.0");
	}
	public void test073() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int fooInt() {
						if (getShort() instanceof int) {
							int i = (int) getShort();
							return i;
						}
						return -1;
					}
					public static long fooLong() {
						if (getShort() instanceof long) {
							long l = (long)getShort();
							return l;
						}
						return -1;
					}
					public static float fooFloat() {
						if (getShort() instanceof float) {
							float f = (float)getShort();
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getShort() instanceof double) {
							double d = (double)getShort();
							return d;
						}
						return -1;
					}
					public static short getShort() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.fooInt());
						System.out.println(X.fooLong());
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1\n"
			+ "1\n"
			+ "1.0\n"
			+ "1.0");
	}
	public void test074() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int fooInt() {
						if (getShort() instanceof int i) {
							return i;
						}
						return -1;
					}
					public static long fooLong() {
						if (getShort() instanceof long l) {
							return l;
						}
						return -1;
					}
					public static float fooFloat() {
						if (getShort() instanceof float f) {
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getShort() instanceof double d) {
							return d;
						}
						return -1;
					}
					public static short getShort() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.fooInt());
						System.out.println(X.fooLong());
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1\n"
			+ "1\n"
			+ "1.0\n"
			+ "1.0");
	}
	public void test075() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int fooInt() {
						if (getChar() instanceof int) {
							int i = (int) getChar();
							return i;
						}
						return -1;
					}
					public static long fooLong() {
						if (getChar() instanceof long) {
							long l = (long)getChar();
							return l;
						}
						return -1;
					}
					public static float fooFloat() {
						if (getChar() instanceof float) {
							float f = (float)getChar();
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getChar() instanceof double) {
							double d = (double)getChar();
							return d;
						}
						return -1;
					}
					public static char getChar() {
						return (char)1;
					}
					public static void main(String[] args) {
						System.out.println(X.fooInt());
						System.out.println(X.fooLong());
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1\n"
			+ "1\n"
			+ "1.0\n"
			+ "1.0");
	}
	public void test076() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static int fooInt() {
						if (getChar() instanceof int i) {
							return i;
						}
						return -1;
					}
					public static long fooLong() {
						if (getChar() instanceof long l) {
							return l;
						}
						return -1;
					}
					public static float fooFloat() {
						if (getChar() instanceof float f) {
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getChar() instanceof double d) {
							return d;
						}
						return -1;
					}
					public static char getChar() {
						return (char)1;
					}
					public static void main(String[] args) {
						System.out.println(X.fooInt());
						System.out.println(X.fooLong());
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1\n"
			+ "1\n"
			+ "1.0\n"
			+ "1.0");
	}
	public void test077() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long fooLong() {
						if (getInt() instanceof long) {
							long l = (long)getInt();
							return l;
						}
						return -1;
					}
					public static float fooFloat() {
						if (getInt() instanceof float) {
							float f = (float)getInt();
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getInt() instanceof double) {
							double d = (double)getInt();
							return d;
						}
						return -1;
					}
					public static int getInt() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.fooLong());
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1\n"
			+ "1.0\n"
			+ "1.0");
	}
	public void test078() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static long fooLong() {
						if (getInt() instanceof long l) {
							return l;
						}
						return -1;
					}
					public static float fooFloat() {
						if (getInt() instanceof float f) {
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getInt() instanceof double d) {
							return d;
						}
						return -1;
					}
					public static int getInt() {
						return 1;
					}
					public static void main(String[] args) {
						System.out.println(X.fooLong());
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1\n"
			+ "1.0\n"
			+ "1.0");
	}
	public void test079() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float fooFloat() {
						if (getLong() instanceof float) {
							float f = (float)getLong();
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getLong() instanceof double) {
							double d = (double) getLong();
							return d;
						}
						return -1;
					}
					public static long getLong() {
						return 1L;
					}
					public static void main(String[] args) {
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1.0\n"
			+ "1.0");
	}

	public void test080() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float fooFloat() {
						if (getLong() instanceof float f) {
							return f;
						}
						return -1;
					}
					public static double fooDouble() {
						if (getLong() instanceof double d) {
							return d;
						}
						return -1;
					}
					public static long getLong() {
						return 1L;
					}
					public static void main(String[] args) {
						System.out.println(X.fooFloat());
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1.0\n"
			+ "1.0");
	}
	public void test081() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double fooDouble() {
						if (getFloat() instanceof double) {
							double d = (double) getFloat();
							return d;
						}
						return -1;
					}
					public static float getFloat() {
						return 1.0f;
					}
					public static void main(String[] args) {
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1.0");
	}
	public void test082() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double fooDouble() {
						if (getFloat() instanceof double d) {
							return d;
						}
						return -1;
					}
					public static float getFloat() {
						return 1.0f;
					}
					public static void main(String[] args) {
						System.out.println(X.fooDouble());
					}
				}
				"""
			},
			"1.0");
	}
	public void test083() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float) {
							float f = (float)i;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1234567890;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	public void test084() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = 1234567890;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	public void test085() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float) {
							float f = (float)i;
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = Integer.MAX_VALUE;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	public void test086() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static float foo(int i) {
						if (i instanceof float f) {
							return f;
						}
						return -1;
					}
					public static void main(String[] args) {
						int i = Integer.MAX_VALUE;
						System.out.println(X.foo(i));
					}
				}
				"""
			},
			"-1.0");
	}
	// Narrowing Primitive Double

	public void testX001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte d2b(double d) {
						if (d instanceof byte) {
							byte r = (byte) d;
							return r;
						}
						return 0;
					}
					public static short d2s(double d) {
						if (d instanceof short) {
							short r = (short) d;
							return r;
						}
						return 0;
					}
					public static char d2c(double d) {
						if (d instanceof char) {
							char r = (char) d;
							return r;
						}
						return 0;
					}
					public static int d2i(double d) {
						if (d instanceof int) {
							int r = (int) d;
							return r;
						}
						return 0;
					}
					public static long d2l(double d) {
						if (d instanceof long) {
							long r = (long) d;
							return r;
						}
						return 0;
					}
					public static float d2f(double d) {
						if (d instanceof float) {
							float r = (float) d;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						double d = 49;
						System.out.println(X.d2b(d));
						System.out.println(X.d2s(d));
						System.out.println(X.d2c(d));
						System.out.println(X.d2i(d));
						System.out.println(X.d2l(d));
						System.out.println(X.d2f(d));
					}
				}
				"""
			},
			"49\n" +
			"49\n" +
			"1\n" +
			"49\n" +
			"49\n" +
			"49.0");
	}
	public void testX002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte d2b(double d) {
						if (d instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short d2s(double d) {
						if (d instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char d2c(double d) {
						if (d instanceof char r) {
							return r;
						}
						return 0;
					}
					public static int d2i(double d) {
						if (d instanceof int r) {
							return r;
						}
						return 0;
					}
					public static long d2l(double d) {
						if (d instanceof long r) {
							return r;
						}
						return 0;
					}
					public static float d2f(double d) {
						if (d instanceof float r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						double d = 49;
						System.out.println(X.d2b(d));
						System.out.println(X.d2s(d));
						System.out.println(X.d2c(d));
						System.out.println(X.d2i(d));
						System.out.println(X.d2l(d));
						System.out.println(X.d2f(d));
					}
				}
				"""
			},
			"49\n" +
			"49\n" +
			"1\n" +
			"49\n" +
			"49\n" +
			"49.0");
	}

	public void testX003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte d2b() {
						if (getDouble() instanceof byte) {
							byte r = (byte) getDouble();
							return r;
						}
						return 0;
					}
					public static short d2s() {
						if (getDouble() instanceof short) {
							short r = (short) getDouble();
							return r;
						}
						return 0;
					}
					public static char d2c() {
						if (getDouble() instanceof char) {
							char r = (char) getDouble();
							return r;
						}
						return 0;
					}
					public static int d2i() {
						if (getDouble() instanceof int) {
							int r = (int) getDouble();
							return r;
						}
						return 0;
					}
					public static long d2l() {
						if (getDouble() instanceof long) {
							long r = (long) getDouble();
							return r;
						}
						return 0;
					}
					public static float d2f() {
						if (getDouble() instanceof float) {
							float r = (float) getDouble();
							return r;
						}
						return 0;
					}
					private static double getDouble() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.d2b());
						System.out.println(X.d2s());
						System.out.println(X.d2c());
						System.out.println(X.d2i());
						System.out.println(X.d2l());
						System.out.println(X.d2f());
					}
				}
				"""
			},
			"49\n" +
			"49\n" +
			"1\n" +
			"49\n" +
			"49\n" +
			"49.0");
	}
	public void testX004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte d2b() {
						if (getDouble() instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short d2s() {
						if (getDouble() instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char d2c() {
						if (getDouble() instanceof char r) {
							return r;
						}
						return 0;
					}
					public static int d2i() {
						if (getDouble() instanceof int r) {
							return r;
						}
						return 0;
					}
					public static long d2l() {
						if (getDouble() instanceof long r) {
							return r;
						}
						return 0;
					}
					public static float d2f() {
						if (getDouble() instanceof float r) {
							return r;
						}
						return 0;
					}
					private static double getDouble() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.d2b());
						System.out.println(X.d2s());
						System.out.println(X.d2c());
						System.out.println(X.d2i());
						System.out.println(X.d2l());
						System.out.println(X.d2f());
					}
				}
				"""
			},
			"49\n" +
			"49\n" +
			"1\n" +
			"49\n" +
			"49\n" +
			"49.0");
	}


	//Narrowing float
	public void testF001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte f2b(float f) {
						if (f instanceof byte) {
							byte r = (byte) f;
							return r;
						}
						return 0;
					}
					public static short f2s(float f) {
						if (f instanceof short) {
							short r = (short) f;
							return r;
						}
						return 0;
					}
					public static char f2c(float f) {
						if (f instanceof char) {
							char r = (char) f;
							return r;
						}
						return 0;
					}
					public static int f2i(float f) {
						if (f instanceof int) {
							int r = (int) f;
							return r;
						}
						return 0;
					}
					public static long f2l(float f) {
						if (f instanceof long) {
							long r = (long) f;
							return r;
						}
						return 0;
					}
					public static float f2f(float f) {
						if (f instanceof float) {
							float r = (float) f;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						float f = 49;
						System.out.println(X.f2b(f));
						System.out.println(X.f2s(f));
						System.out.println(X.f2c(f));
						System.out.println(X.f2i(f));
						System.out.println(X.f2l(f));
						System.out.println(X.f2f(f));

					}
				}
				"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49\n" +
				"49\n" +
				"49.0");
	}
	public void testF002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte f2b(float f) {
						if (f instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short f2s(float f) {
						if (f instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char f2c(float f) {
						if (f instanceof char r) {
							return r;
						}
						return 0;
					}
					public static int f2i(float f) {
						if (f instanceof int r) {
							return r;
						}
						return 0;
					}
					public static long f2l(float f) {
						if (f instanceof long r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						float f = 49;
						System.out.println(X.f2b(f));
						System.out.println(X.f2s(f));
						System.out.println(X.f2c(f));
						System.out.println(X.f2i(f));
						System.out.println(X.f2l(f));
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49\n" +
				"49");
	}
	public void testF003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte f2b() {
						if (getFloat() instanceof byte) {
							byte r = (byte) getFloat();
							return r;
						}
						return 0;
					}
					public static short f2s() {
						if (getFloat() instanceof short) {
							short r = (short) getFloat();
							return r;
						}
						return 0;
					}
					public static char f2c() {
						if (getFloat() instanceof char) {
							char r = (char) getFloat();
							return r;
						}
						return 0;
					}
					public static int f2i() {
						if (getFloat() instanceof int) {
							int r = (int) getFloat();
							return r;
						}
						return 0;
					}
					public static long f2l() {
						if (getFloat() instanceof long) {
							long r = (long) getFloat();
							return r;
						}
						return 0;
					}
					private static float getFloat() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.f2b());
						System.out.println(X.f2s());
						System.out.println(X.f2c());
						System.out.println(X.f2i());
						System.out.println(X.f2l());
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49\n" +
				"49");
	}
	public void testF004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte f2b() {
						if (getFloat() instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short f2s() {
						if (getFloat() instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char f2c() {
						if (getFloat() instanceof char r) {
							return r;
						}
						return 0;
					}
					public static int f2i() {
						if (getFloat() instanceof int r) {
							return r;
						}
						return 0;
					}
					public static long f2l() {
						if (getFloat() instanceof long r) {
							return r;
						}
						return 0;
					}
					private static float getFloat() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.f2b());
						System.out.println(X.f2s());
						System.out.println(X.f2c());
						System.out.println(X.f2i());
						System.out.println(X.f2l());
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49\n" +
				"49");
	}

	// Narrowing Long
	public void testL001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte l2b(long l) {
						if (l instanceof byte) {
							byte r = (byte) l;
							return r;
						}
						return 0;
					}
					public static short l2s(long l) {
						if (l instanceof short) {
							short r = (short) l;
							return r;
						}
						return 0;
					}
					public static char l2c(long l) {
						if (l instanceof char) {
							char r = (char) l;
							return r;
						}
						return 0;
					}
					public static int l2i(long l) {
						if (l instanceof int) {
							int r = (int) l;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						long l = 49;
						System.out.println(X.l2b(l));
						System.out.println(X.l2s(l));
						System.out.println(X.l2c(l));
						System.out.println(X.l2i(l));

					}
				}
				"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49");
	}
	public void testL002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte l2b(long l) {
						if (l instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short l2s(long l) {
						if (l instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char l2c(long l) {
						if (l instanceof char r) {
							return r;
						}
						return 0;
					}
					public static int l2i(long l) {
						if (l instanceof int r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						long l = 49;
						System.out.println(X.l2b(l));
						System.out.println(X.l2s(l));
						System.out.println(X.l2c(l));
						System.out.println(X.l2i(l));
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49");
	}
	public void testL003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte l2b() {
						if (getLong() instanceof byte) {
							byte r = (byte) getLong();
							return r;
						}
						return 0;
					}
					public static short l2s() {
						if (getLong() instanceof short) {
							short r = (short) getLong();
							return r;
						}
						return 0;
					}
					public static char l2c() {
						if (getLong() instanceof char) {
							char r = (char) getLong();
							return r;
						}
						return 0;
					}
					public static int l2i() {
						if (getLong() instanceof int) {
							int r = (int) getLong();
							return r;
						}
						return 0;
					}
					private static long getLong() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.l2b());
						System.out.println(X.l2s());
						System.out.println(X.l2c());
						System.out.println(X.l2i());
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49");
	}
	public void testL004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte l2b() {
						if (getLong() instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short l2s() {
						if (getLong() instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char l2c() {
						if (getLong() instanceof char r) {
							return r;
						}
						return 0;
					}
					public static int l2i() {
						if (getLong() instanceof int r) {
							return r;
						}
						return 0;
					}
					private static long getLong() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.l2b());
						System.out.println(X.l2s());
						System.out.println(X.l2c());
						System.out.println(X.l2i());
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1\n" +
				"49");
	}
	// Narrowing int
	public void testI001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte i2b(int i) {
						if (i instanceof byte) {
							byte r = (byte) i;
							return r;
						}
						return 0;
					}
					public static short i2s(int i) {
						if (i instanceof short) {
							short r = (short) i;
							return r;
						}
						return 0;
					}
					public static char i2c(int i) {
						if (i instanceof char) {
							char r = (char) i;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						int i = 49;
						System.out.println(X.i2b(i));
						System.out.println(X.i2s(i));
						System.out.println(X.i2c(i));

					}
				}
				"""
			},
				"49\n" +
				"49\n" +
				"1");
	}
	public void testI002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte i2b(int i) {
						if (i instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short i2s(int i) {
						if (i instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char i2c(int i) {
						if (i instanceof char r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						int i = 49;
						System.out.println(X.i2b(i));
						System.out.println(X.i2s(i));
						System.out.println(X.i2c(i));
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1");
	}
	public void testI003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte i2b() {
						if (getInt() instanceof byte) {
							byte r = (byte) getInt();
							return r;
						}
						return 0;
					}
					public static short i2s() {
						if (getInt() instanceof short) {
							short r = (short) getInt();
							return r;
						}
						return 0;
					}
					public static char i2c() {
						if (getInt() instanceof char) {
							char r = (char) getInt();
							return r;
						}
						return 0;
					}
					private static int getInt() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.i2b());
						System.out.println(X.i2s());
						System.out.println(X.i2c());
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1");
	}
	public void testI004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte i2b() {
						if (getInt() instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short i2s() {
						if (getInt() instanceof short r) {
							return r;
						}
						return 0;
					}
					public static char i2c() {
						if (getInt() instanceof char r) {
							return r;
						}
						return 0;
					}
					private static int getInt() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.i2b());
						System.out.println(X.i2s());
						System.out.println(X.i2c());
					}
				}
     			"""
			},
				"49\n" +
				"49\n" +
				"1");
	}
	// Narrowing char
	public void testC001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte c2b(char c) {
						if (c instanceof byte) {
							byte r = (byte) c;
							return r;
						}
						return 0;
					}
					public static short c2s(char c) {
						if (c instanceof short) {
							short r = (short) c;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						char c = 49;
						System.out.println(X.c2b(c));
						System.out.println(X.c2s(c));

					}
				}
				"""
			},
				"49\n" +
				"49");
	}
	public void testC002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte c2b(char c) {
						if (c instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short c2s(char c) {
						if (c instanceof short r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						char c = 49;
						System.out.println(X.c2b(c));
						System.out.println(X.c2s(c));
					}
				}
     			"""
			},
				"49\n" +
				"49");
	}
	public void testC003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte c2b() {
						if (getChar() instanceof byte) {
							byte r = (byte) getChar();
							return r;
						}
						return 0;
					}
					public static short c2s() {
						if (getChar() instanceof short) {
							short r = (short) getChar();
							return r;
						}
						return 0;
					}
					private static char getChar() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.c2b());
						System.out.println(X.c2s());
					}
				}
     			"""
			},
				"49\n" +
				"49");
	}
	public void testC004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte c2b() {
						if (getChar() instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static short c2s() {
						if (getChar() instanceof short r) {
							return r;
						}
						return 0;
					}
					private static char getChar() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.c2b());
						System.out.println(X.c2s());
					}
				}
     			"""
			},
				"49\n" +
				"49");
	}

	// Narrowing short
	public void testS001() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte s2b(short s) {
						if (s instanceof byte) {
							byte r = (byte) s;
							return r;
						}
						return 0;
					}
					public static char s2c(short s) {
						if (s instanceof char) {
							char r = (char) s;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						short s = 49;
						System.out.println(X.s2b(s));
						System.out.println(X.s2c(s));

					}
				}
				"""
			},
				"49\n" +
				"1");
	}
	public void testS002() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte s2b(short s) {
						if (s instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static char s2c(short s) {
						if (s instanceof char r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						short s = 49;
						System.out.println(X.s2b(s));
						System.out.println(X.s2c(s));
					}
				}
     			"""
			},
				"49\n" +
				"1");
	}
	public void testS003() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte s2b() {
						if (getShort() instanceof byte) {
							byte r = (byte) getShort();
							return r;
						}
						return 0;
					}
					public static char s2c() {
						if (getShort() instanceof char) {
							char r = (char) getShort();
							return r;
						}
						return 0;
					}
					private static short getShort() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.s2b());
						System.out.println(X.s2c());
					}
				}
     			"""
			},
				"49\n" +
				"1");
	}
	public void testS004() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte s2b() {
						if (getShort() instanceof byte r) {
							return r;
						}
						return 0;
					}
					public static char s2c() {
						if (getShort() instanceof char r) {
							return r;
						}
						return 0;
					}
					private static short getShort() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.s2b());
						System.out.println(X.s2c());
					}
				}
     			"""
			},
				"49\n" +
				"1");
	}

	// test from spec
	public void _testSpec001() {
		runConformTest(new String[] {
			"X.java",
				"""
					public class X {
						public int getStatus() {
							return 100;
						}
						public static int foo(X x) {
							return switch (x.getStatus()) {
						    case int i -> i;
							default -> -1;
						};
						}
						public static void main(String[] args) {
							X x = new X();
							System.out.println(X.foo(x));
						}
					}
				"""
			},
			"100");
	}
	public void _testSpec002() {
		runConformTest(new String[] {
			"X.java",
				"""
					public class X {
						public int getStatus() {
							return 100;
						}
						public static int foo(X x) {
							return switch (x.getStatus()) {
						    case int i when i > 10 -> i * i;
						    case int i -> i;
							default -> -1;
						};
						}
						public static void main(String[] args) {
							X x = new X();
							System.out.println(X.foo(x));
						}
					}
				"""
			},
			"100");
	}
	public void _testSpec003() {
		runConformTest(new String[] {
			"X.java",
				"""
					import java.util.Map;

					sealed interface JsonValue {}
					record JsonString(String s) implements JsonValue { }
					record JsonNumber(double d) implements JsonValue { }
					record JsonObject(Map<String, JsonValue> map) implements JsonValue { }


					public class X {

						public static void foo() {
							var json = new JsonObject(Map.of("name", new JsonString("John"),
					                "age",  new JsonNumber(30)));
					        JsonValue v = json.map().get("age");
							System.out.println(v);
						}
						public static void main(String[] args) {
							X.foo();
						}
					}
				"""
			},
			"JsonNumber[d=30.0]");
	}
	public void _testSpec004() {
		runConformTest(new String[] {
			"X.java",
				"""
					import java.util.Map;

					sealed interface JsonValue {}
					record JsonString(String s) implements JsonValue { }
					record JsonNumber(double d) implements JsonValue { }
					record JsonObject(Map<String, JsonValue> map) implements JsonValue { }


					public class X {

						public static JsonObject foo() {
							var json = new JsonObject(Map.of("name", new JsonString("John"),
					                "age",  new JsonNumber(30)));
							return json;
						}
						public static void bar(Object json) {
							if (json instanceof JsonObject(var map)
								    && map.get("name") instanceof JsonString(String n)
								    && map.get("age")  instanceof JsonNumber(double a)) {
								    int age = (int)a;  // unavoidable (and potentially lossy!) cast
								    System.out.println(age);
								}
						}
						public static void main(String[] args) {
							X.bar(X.foo());
						}
					}
				"""
			},
			"30");
	}
	public void _testSpec005() {
		runConformTest(new String[] {
			"X.java",
				"""
					import java.util.HashMap;
					import java.util.Map;

					sealed interface I {}
					record ZNumber(double d) implements I { }
					record ZObject(Map<String, I> map) implements I { }


					public class X {

						public static ZObject foo() {
							Map<String, I> myMap = new HashMap<>();
							myMap.put("age",  new ZNumber(30));
							return new ZObject(myMap);
						}
						public static void bar(Object json) {
							if (json instanceof ZObject(var map)) {
								if (map.get("age")  instanceof ZNumber(double d)) {
									System.out.println("double:"+d);
								}
							}
						}
						public static void main(String[] args) {
							X.bar(X.foo());
						}
					}
				"""
			},
			"double:30.0");
	}
	public void _testSpec006() {
		runConformTest(new String[] {
			"X.java",
				"""
					import java.util.HashMap;
					import java.util.Map;

					sealed interface I {}
					record ZNumber(double d) implements I { }
					record ZObject(Map<String, I> map) implements I { }


					public class X {

						public static ZObject foo() {
							Map<String, I> myMap = new HashMap<>();
							myMap.put("age",  new ZNumber(30));
							return new ZObject(myMap);
						}
						public static void bar(Object json) {
							if (json instanceof ZObject(var map)) {
								if (map.get("age")  instanceof ZNumber(int i)) {
									System.out.println("int:"+i);
								} else if (map.get("age")  instanceof ZNumber(double d)) {
									System.out.println("double:"+d);
								}
							}
						}
						public static void main(String[] args) {
							X.bar(X.foo());
						}
					}
				"""
			},
			"int:30");
	}
	public void _testSpec00X() {
		runNegativeTest(new String[] {
			"X.java",
				"""
      			"""
			},
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}

}
