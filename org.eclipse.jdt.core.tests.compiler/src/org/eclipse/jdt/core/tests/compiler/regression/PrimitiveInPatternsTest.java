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
	public void test211() {
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
	public void test212() {
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
	public void test213() {
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
	public void test214() {
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
	public void test215() {
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
	public void test216() {
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
	public void test217() {
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
	public void test218() {
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
	public void test219() {
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
	public void test220() {
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
	public void test221() {
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
	public void test222() {
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
	public void test223() {
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
	public void test224() {
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
	public void test225() {
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
	public void test226() {
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
	public void test227() {
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
	public void test228() {
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
	public void test229() {
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
	public void test230() {
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
	public void test231() {
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
	public void test232() {
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
	public void test233() {
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
	public void test234() {
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
	public void test235() {
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
	public void test236() {
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
	public void test237() {
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
	public void test238() {
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
	public void test239() {
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
	public void test240() {
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
	public void test241() {
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
	public void test242() {
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
	public void test243() {
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
	public void test244() {
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
	public void test245() {
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
	public void test246() {
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
	public void test247() {
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
	public void test248() {
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
	public void test249() {
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
	public void test250() {
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
	public void test251() {
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
	public void test252() {
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
	public void testSpec001() {
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
	public void testSpec002() {
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
	public void testSpec003() {
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
	public void testSpec004() {
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
	public void testSpec005() {
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
	public void testSpec006() {
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
	public void testSpec00X() {
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
