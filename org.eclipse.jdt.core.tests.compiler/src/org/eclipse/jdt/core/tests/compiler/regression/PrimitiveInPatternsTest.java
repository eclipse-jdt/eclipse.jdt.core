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
	private static final String[] PRIMITIVES = { "boolean", "byte", "char", "short", "int", "long", "float", "double" };
	@SuppressWarnings("unused")
	private static final String[] BOXES = { "Boolean", "Byte", "Char", "Short", "Int", "Long", "Float", "Double" };
	private static final String[] GOODVALUES = { "true", "1", "'a'", "1", "1", "1L", "1.0f", "1.0d" };
	private static final String[] NONVALUES = { "false", "-1", "'-'", "-1", "-1", "-1L", "-1.0f", "-1.0d" };
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIdentity" };
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
	public void testIdentity() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(PRIM v) {
						if (v instanceof PRIM) {
							return v;
						}
						return NONVAL;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.fooPRIM(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			String prim = PRIMITIVES[i];
			methods.append(methodTmpl.replaceAll("PRIM", prim).replace("NONVAL", NONVALUES[i]));
			calls.append(callTmpl.replaceAll("PRIM", prim).replace("VAL", GOODVALUES[i]));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] {
				"X.java",
				classX.toString()
			},
			"true|1|a|1|1|1|1.0|1.0|");
	}
	public void testIdentityPattern() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(PRIM v) {
						if (v instanceof PRIM vv) {
							return vv;
						}
						return NONVAL;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.fooPRIM(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			String prim = PRIMITIVES[i];
			methods.append(methodTmpl.replaceAll("PRIM", prim).replace("NONVAL", NONVALUES[i]));
			calls.append(callTmpl.replaceAll("PRIM", prim).replace("VAL", GOODVALUES[i]));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] {
				"X.java",
				classX.toString()
			},
			"true|1|a|1|1|1|1.0|1.0|");
	}

	public void testIdentity_otherLhs() {
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

	public void testIdentityPattern_otherLhs() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (barPRIM() instanceof PRIM vv) {
							return vv;
						}
						return NONVAL;
					}
					public static PRIM barPRIM() {
						return VAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.fooPRIM());
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			String prim = PRIMITIVES[i];
			methods.append(methodTmpl.replaceAll("PRIM", prim).replace("NONVAL", NONVALUES[i]).replace("VAL", GOODVALUES[i]));
			calls.append(callTmpl.replaceAll("PRIM", prim));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] {
				"X.java",
				classX.toString()
			},
			"true|1|a|1|1|1|1.0|1.0|");
	}

	// Widening primitive conversions
	private void testWideningFrom(String from, int idx, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(FROM v) {
						if (v instanceof PRIM) {
							PRIM vv = (PRIM) v;
							return vv;
						}
						return NONVAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.fooPRIM(v));
						System.out.print('|');
				""";
		// for all primitive types "greater" than 'from':
		for (int i = idx+1; i < PRIMITIVES.length; i++) {
			String prim = PRIMITIVES[i];
			methods.append(methodTmpl.replaceAll("PRIM", prim).replace("FROM", from).replace("NONVAL", NONVALUES[i]));
			calls.append(callTmpl.replace("PRIM", prim));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(from).append(" v = 97;");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] {
				"X.java",
				classX.toString()
			},
			expectedOut);
	}
	public void testWideningByte() {
		testWideningFrom("byte", 1, "a|97|97|97|97.0|97.0|");
	}
	public void testWideningChar() {
		testWideningFrom("char", 2, "97|97|97|97.0|97.0|");
	}
	public void testWideningShort() {
		testWideningFrom("short", 3, "97|97|97.0|97.0|");
	}
	public void testWideningInt() {
		testWideningFrom("int", 4, "97|97.0|97.0|");
	}
	public void testWideningLong() {
		testWideningFrom("long", 5, "97.0|97.0|");
	}
	private void testWideningFrom_pattern(String from, int idx, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(FROM v) {
						if (v instanceof PRIM vv) {
							return vv;
						}
						return NONVAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.fooPRIM(v));
						System.out.print('|');
				""";
		// for all primitive types "greater" than 'from':
		for (int i = idx+1; i < PRIMITIVES.length; i++) {
			String prim = PRIMITIVES[i];
			methods.append(methodTmpl.replaceAll("PRIM", prim).replace("FROM", from).replace("NONVAL", NONVALUES[i]));
			calls.append(callTmpl.replace("PRIM", prim));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(from).append(" v = 97;");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] {
				"X.java",
				classX.toString()
			},
			expectedOut);
	}
	public void testWideningByte_pattern() {
		testWideningFrom_pattern("byte", 1, "a|97|97|97|97.0|97.0|");
	}
	public void testWideningChar_pattern() {
		testWideningFrom_pattern("char", 2, "97|97|97|97.0|97.0|");
	}
	public void testWideningShort_pattern() {
		testWideningFrom_pattern("short", 3, "97|97|97.0|97.0|");
	}
	public void testWideningInt_pattern() {
		testWideningFrom_pattern("int", 4, "97|97.0|97.0|");
	}
	public void testWideningLong_pattern() {
		testWideningFrom_pattern("long", 5, "97.0|97.0|");
	}
	public void testWideningFloat2Double() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static double foo1(float f) {
						if (f instanceof double) {
							double d = (double)f;
							return d;
						}
						return -1;
					}
					public static double foo2(float f) {
						if (f instanceof double d) {
							return d;
						}
						return -1;
					}
					public static void main(String[] args) {
						float f = 1.0f;
						System.out.print(X.foo1(f));
						System.out.print(X.foo2(f));
					}
				}
				"""
			},
			"1.01.0");
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

	public void test087() {
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
	public void test088() {
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

	public void test089() {
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
	public void test090() {
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
	public void test091() {
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
	public void test092() {
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
	public void test093() {
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
	public void test094() {
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
	public void test095() {
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
	public void test096() {
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
	public void test097() {
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
	public void test098() {
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
	public void test099() {
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
	public void test100() {
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
	public void test101() {
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
	public void test102() {
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
	public void test103() {
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
	public void test104() {
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
	public void test105() {
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
	public void test106() {
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
	public void test107() {
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
	public void test108() {
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
	public void test109() {
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
	public void test110() {
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

	// 5.1.4 Widening and Narrowing Primitive Conversion
	public void test111() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static char b2c(byte b) {
						if (b instanceof char) {
							char r = (char) b;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						byte b = 49;
						System.out.println(X.b2c(b));

					}
				}
				"""
			},
				"1");
	}
	public void test112() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static char b2c(byte b) {
						if (b instanceof char r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						byte b = 49;
						System.out.println(X.b2c(b));
					}
				}
     			"""
			},
				"1");
	}
	public void test113() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static char b2c() {
						if (getByte() instanceof char) {
							char r = (char) getByte();
							return r;
						}
						return 0;
					}
					private static byte getByte() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.b2c());
					}
				}
     			"""
			},
			"1");
	}
	public void test114() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static char b2c() {
						if (getByte() instanceof char r) {
							return r;
						}
						return 0;
					}
					private static byte getByte() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.b2c());
					}
				}
     			"""
			},
			"1");
	}
	public void test115() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Boolean boolean2Boolean(boolean b) {
						if (b instanceof Boolean) {
							Boolean r = (Boolean) b;
							return r;
						}
						return Boolean.FALSE;
					}
					public static void main(String[] args) {
						boolean b = true;
						System.out.println(X.boolean2Boolean(b));

					}
				}
				"""
			},
				"true");
	}
	public void test116() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Boolean boolean2Boolean(boolean b) {
						if (b instanceof Boolean r) {
							return r;
						}
						return Boolean.FALSE;
					}
					public static void main(String[] args) {
						boolean b = true;
						System.out.println(X.boolean2Boolean(b));
					}
				}
     			"""
			},
				"true");
	}
	public void test117() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Boolean boolean2Boolean() {
						if (getboolean() instanceof Boolean) {
							Boolean r = (Boolean) getboolean();
							return r;
						}
						return Boolean.FALSE;
					}
					private static boolean getboolean() {
						return true;
					}
					public static void main(String[] args) {
						System.out.println(X.boolean2Boolean());
					}
				}
     			"""
			},
			"true");
	}
	public void test118() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Boolean boolean2Boolean() {
						if (getboolean() instanceof Boolean r) {
							return r;
						}
						return Boolean.FALSE;
					}
					private static boolean getboolean() {
						return true;
					}
					public static void main(String[] args) {
						System.out.println(X.boolean2Boolean());
					}
				}
     			"""
			},
			"true");
	}

	public void test119() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Byte byte2Byte(byte b) {
						if (b instanceof Byte) {
							Byte r = (Byte) b;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						byte b = 49;
						System.out.println(X.byte2Byte(b));

					}
				}
				"""
			},
			"49");
	}
	public void test120() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Byte byte2Byte(byte b) {
						if (b instanceof Byte r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						byte b = 49;
						System.out.println(X.byte2Byte(b));
					}
				}
     			"""
			},
			"49");
	}
	public void test121() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Byte byte2Byte() {
						if (getbyte() instanceof Byte) {
							Byte r = (Byte) getbyte();
							return r;
						}
						return 0;
					}
					private static byte getbyte() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.byte2Byte());
					}
				}
			    """
			},
			"49");
	}
	public void test122() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Byte byte2Byte() {
						if (getbyte() instanceof Byte r) {
							return r;
						}
						return 0;
					}
					private static byte getbyte() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.byte2Byte());
					}
				}
     			"""
			},
			"49");
	}
	public void test123() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Short short2Short(short s) {
						if (s instanceof Short) {
							Short r = (Short) s;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						short s = 49;
						System.out.println(X.short2Short(s));

					}
				}
				"""
			},
			"49");
	}
	public void test124() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Short short2Short(short s) {
						if (s instanceof Short r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						short s = 49;
						System.out.println(X.short2Short(s));
					}
				}
     			"""
			},
			"49");
	}
	public void test125() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Short short2Short() {
						if (getshort() instanceof Short) {
							Short r = (Short) getshort();
							return r;
						}
						return 0;
					}
					private static short getshort() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.short2Short());
					}
				}
			    """
			},
			"49");
	}
	public void test126() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Short short2Short() {
						if (getshort() instanceof Short r) {
							return r;
						}
						return 0;
					}
					private static short getshort() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.short2Short());
					}
				}
     			"""
			},
			"49");
	}
	public void test127() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Character char2Character(char c) {
						if (c instanceof Character) {
							Character r = (Character) c;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						char c = 49;
						System.out.println(X.char2Character(c));

					}
				}
				"""
			},
			"1");
	}
	public void test128() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Character char2Character(char c) {
						if (c instanceof Character r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						char c = 49;
						System.out.println(X.char2Character(c));
					}
				}
     			"""
			},
			"1");
	}
	public void test129() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Character char2Character() {
						if (getChar() instanceof Character) {
							Character r = (Character) getChar();
							return r;
						}
						return 0;
					}
					private static char getChar() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.char2Character());
					}
				}
			    """
			},
			"1");
	}
	public void test130() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Character char2Character() {
						if (getChar() instanceof Character r) {
							return r;
						}
						return 0;
					}
					private static char getChar() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.char2Character());
					}
				}
     			"""
			},
			"1");
	}
	public void test131() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Integer int2Integer(int i) {
						if (i instanceof Integer) {
							Integer r = (Integer) i;
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						int i = 49;
						System.out.println(X.int2Integer(i));

					}
				}
				"""
			},
			"49");
	}
	public void test132() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Integer int2Integer(int i) {
						if (i instanceof Integer r) {
							return r;
						}
						return 0;
					}
					public static void main(String[] args) {
						int i = 49;
						System.out.println(X.int2Integer(i));
					}
				}
     			"""
			},
			"49");
	}
	public void test133() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Integer int2Integer() {
						if (getInt() instanceof Integer) {
							Integer r = (Integer) getInt();
							return r;
						}
						return 0;
					}
					private static int getInt() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.int2Integer());
					}
				}
			    """
			},
			"49");
	}
	public void test134() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Integer int2Integer() {
						if (getInteger() instanceof Integer r) {
							return r;
						}
						return 0;
					}
					private static int getInteger() {
						return 49;
					}
					public static void main(String[] args) {
						System.out.println(X.int2Integer());
					}
				}
     			"""
			},
			"49");
	}
	public void test135() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Long long2Long(long l) {
						if (l instanceof Long) {
							Long r = (Long) l;
							return r;
						}
						return 0L;
					}
					public static void main(String[] args) {
						long l = 49;
						System.out.println(X.long2Long(l));

					}
				}
				"""
			},
			"49");
	}
	public void test136() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Long long2Long(long l) {
						if (l instanceof Long r) {
							return r;
						}
						return 0L;
					}
					public static void main(String[] args) {
						long l = 49;
						System.out.println(X.long2Long(l));
					}
				}
     			"""
			},
			"49");
	}
	public void test137() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Long long2Long() {
						if (getLong() instanceof Long) {
							Long r = (Long) getLong();
							return r;
						}
						return 0L;
					}
					private static long getLong() {
						return 49L;
					}
					public static void main(String[] args) {
						System.out.println(X.long2Long());
					}
				}
			    """
			},
			"49");
	}
	public void test138() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Long long2Long() {
						if (getLong() instanceof Long r) {
							return r;
						}
						return 0L;
					}
					private static long getLong() {
						return 49L;
					}
					public static void main(String[] args) {
						System.out.println(X.long2Long());
					}
				}
			    """
			},
			"49");
	}
	public void test139() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Float float2Float(float f) {
						if (f instanceof Float) {
							Float r = (Float) f;
							return r;
						}
						return 0.0f;
					}
					public static void main(String[] args) {
						float f = 49.0f;
						System.out.println(X.float2Float(f));

					}
				}
     			"""
			},
			"49.0");
	}
	public void test140() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Float float2Float(float f) {
						if (f instanceof Float r) {
							return r;
						}
						return 0.0f;
					}
					public static void main(String[] args) {
						float f = 49.0f;
						System.out.println(X.float2Float(f));
					}
				}
				"""
			},
			"49.0");
	}
	public void test141() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Float float2Float() {
						if (getFloat() instanceof Float) {
							Float r = (Float) getFloat();
							return r;
						}
						return 0.0f;
					}
					private static float getFloat() {
						return 49.0f;
					}
					public static void main(String[] args) {
						System.out.println(X.float2Float());
					}
				}
     			"""
			},
			"49.0");
	}
	public void test142() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Float float2Float() {
						if (getFloat() instanceof Float r) {
							return r;
						}
						return 0.0f;
					}
					private static float getFloat() {
						return 49.0f;
					}
					public static void main(String[] args) {
						System.out.println(X.float2Float());
					}
				}
			    """
			},
			"49.0");
	}
	public void test143() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Double double2Double(double d) {
						if (d instanceof Double) {
							Double r = (Double) d;
							return r;
						}
						return 0.0d;
					}
					public static void main(String[] args) {
						double d = 49.0d;
						System.out.println(X.double2Double(d));

					}
				}
     			"""
			},
			"49.0");
	}
	public void test144() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Double double2Double(double d) {
						if (d instanceof Double r) {
							return r;
						}
						return 0.0d;
					}
					public static void main(String[] args) {
						double d = 49.0d;
						System.out.println(X.double2Double(d));
					}
				}
				"""
			},
			"49.0");
	}
	public void test145() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Double double2Double() {
						if (getDouble() instanceof Double) {
							Double r = (Double) getDouble();
							return r;
						}
						return 0.0d;
					}
					private static double getDouble() {
						return 49.0d;
					}
					public static void main(String[] args) {
						System.out.println(X.double2Double());
					}
				}
     			"""
			},
			"49.0");
	}
	public void test146() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Double double2Double() {
						if (getDouble() instanceof Double r) {
							return r;
						}
						return 0.0d;
					}
					private static double getDouble() {
						return 49.0d;
					}
					public static void main(String[] args) {
						System.out.println(X.double2Double());
					}
				}
			    """
			},
			"49.0");
	}
	// boxing and widening reference conversion
	public void test147() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo(byte b) {
						if (b instanceof Comparable r) {
							return r;
						}
						return null;
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

	public void test148() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable<Byte> foo(byte b) {
						if (b instanceof Comparable r) {
							return r;
						}
						return null;
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

	public void test149() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable<Byte> foo(byte b) {
						if (b instanceof Comparable<Byte> r) {
							return r;
						}
						return null;
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

	public void test150() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Number foo(byte b) {
						if (b instanceof Number r) {
							return r;
						}
						return null;
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

	public void test151() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(boolean b) {
						if (b instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Boolean> foo2(boolean b) {
						if (b instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Boolean> foo3(boolean b) {
						if (b instanceof Comparable<Boolean> r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						boolean b = true;
						System.out.println(X.foo1(b));
						System.out.println(X.foo2(b));
						System.out.println(X.foo3(b));
					}
				}
			    """
			},
			"true\n" +
			"true\n" +
			"true");
	}
	public void test152() {
		runNegativeTest(new String[] {
			"X.java",
				"""

				public class X {
					public static Number foo(boolean b) {
						if (b instanceof Number r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						boolean b = true;
						System.out.println(X.foo(b));
					}
				}
  			    """
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (b instanceof Number r) {\n" +
			"	    ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Incompatible conditional operand types boolean and Number\n" +
			"----------\n");
	}
	public void test153() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(short s) {
						if (s instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Short> foo2(short s) {
						if (s instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Short> foo3(short s) {
						if (s instanceof Comparable<Short> r) {
							return r;
						}
						return null;
					}
					public static Number foo4(short s) {
						if (s instanceof Number r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						short s = 1;
						System.out.println(X.foo1(s));
						System.out.println(X.foo2(s));
						System.out.println(X.foo3(s));
						System.out.println(X.foo4(s));
					}
				}
			    """
			},
			"1\n" +
			"1\n" +
			"1\n" +
			"1");
	}
	public void test154() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(char c) {
						if (c instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Character> foo2(char c) {
						if (c instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Character> foo3(char c) {
						if (c instanceof Comparable<Character> r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						char c = 49;
						System.out.println(X.foo1(c));
						System.out.println(X.foo2(c));
						System.out.println(X.foo3(c));
					}
				}
			    """
			},
			"1\n" +
			"1\n" +
			"1");
	}
	public void test155() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(int i) {
						if (i instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Integer> foo2(int i) {
						if (i instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Integer> foo3(int i) {
						if (i instanceof Comparable<Integer> r) {
							return r;
						}
						return null;
					}
					public static Number foo4(int i) {
						if (i instanceof Number r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						int i = 49;
						System.out.println(X.foo1(i));
						System.out.println(X.foo2(i));
						System.out.println(X.foo3(i));
						System.out.println(X.foo4(i));
					}
				}
			    """
			},
			"49\n" +
			"49\n" +
			"49\n" +
			"49");
	}
	public void test156() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(long l) {
						if (l instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Long> foo2(long l) {
						if (l instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Long> foo3(long l) {
						if (l instanceof Comparable<Long> r) {
							return r;
						}
						return null;
					}
					public static Number foo4(long l) {
						if (l instanceof Number r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						long l = 49;
						System.out.println(X.foo1(l));
						System.out.println(X.foo2(l));
						System.out.println(X.foo3(l));
						System.out.println(X.foo4(l));
					}
				}
			    """
			},
			"49\n" +
			"49\n" +
			"49\n" +
			"49");
	}
	public void test157() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(float f) {
						if (f instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Float> foo2(float f) {
						if (f instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Float> foo3(float f) {
						if (f instanceof Comparable<Float> r) {
							return r;
						}
						return null;
					}
					public static Number foo4(float f) {
						if (f instanceof Number r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						float f = 49.0f;
						System.out.println(X.foo1(f));
						System.out.println(X.foo2(f));
						System.out.println(X.foo3(f));
						System.out.println(X.foo4(f));
					}
				}
			    """
			},
			"49.0\n" +
			"49.0\n" +
			"49.0\n" +
			"49.0");
	}
	public void test158() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Comparable foo1(double d) {
						if (d instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Double> foo2(double d) {
						if (d instanceof Comparable r) {
							return r;
						}
						return null;
					}
					public static Comparable<Double> foo3(double d) {
						if (d instanceof Comparable<Double> r) {
							return r;
						}
						return null;
					}
					public static Number foo4(double d) {
						if (d instanceof Number r) {
							return r;
						}
						return null;
					}
					public static void main(String[] args) {
						double d = 49.0d;
						System.out.println(X.foo1(d));
						System.out.println(X.foo2(d));
						System.out.println(X.foo3(d));
						System.out.println(X.foo4(d));
					}
				}
			    """
			},
			"49.0\n" +
			"49.0\n" +
			"49.0\n" +
			"49.0");
	}

	// reference - unboxing
	public void _test159() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static boolean Boolean2boolean(Boolean b) {
						if (b instanceof boolean) {
							boolean r = (boolean) b;
							return r;
						}
						return false;
					}
					public static void main(String[] args) {
						boolean b = true;
						System.out.println(X.Boolean2boolean(b));

					}
				}
			    """
			},
			"true");
	}

	public void _test160() {
		runConformTest(new String[] {
			"X.java",
				"""
					public class X {
						public static boolean Boolean2boolean(Boolean b) {
							if (b instanceof boolean r) {
								return r;
							}
							return false;
						}
						public static void main(String[] args) {
							boolean b = true;
							System.out.println(X.Boolean2boolean(b));

						}
					}
			    """
			},
			"true");
	}

	public void _test161() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static boolean Boolean2boolean() {
						if (getBoolean() instanceof boolean) {
							boolean r = (boolean) getBoolean();
							return r;
						}
						return false;
					}
					private static Boolean getBoolean() {
						return Boolean.TRUE;
					}
					public static void main(String[] args) {
						System.out.println(X.Boolean2boolean());
					}
				}
			    """
			},
			"true");
	}

	public void _test162() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static boolean Boolean2boolean() {
						if (getBoolean() instanceof boolean r) {
							return r;
						}
						return false;
					}
					private static Boolean getBoolean() {
						return Boolean.TRUE;
					}
					public static void main(String[] args) {
						System.out.println(X.Boolean2boolean());
					}
				}
			    """
			},
			"true");
	}

	public void testNonPrim001() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y<T> {
					    public boolean foo(T t) {
					        if (t instanceof T) {
					            return false;
					        }
					        return true;
					    }
					}

					public class X  {
					    public static void main(String argv[]) {
					    	System.out.println(new Y<X>().foo(null));
					    }
					}
 			    """
			},
			"true");
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
