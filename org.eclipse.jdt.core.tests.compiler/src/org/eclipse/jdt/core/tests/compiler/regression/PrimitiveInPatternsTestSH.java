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

public class PrimitiveInPatternsTestSH extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 23 -Xlint:-preview");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	private static final String[] PRIMITIVES = { "boolean", "byte", "char", "short", "int", "long", "float", "double" };
	private static final String[] BOXES = { "Boolean", "Byte", "Character", "Short", "Integer", "Long", "Float", "Double" };
	// note: Character.MAX_VALUE doesn't play well with stream handling around TestVerifier, so we avoid non-ascii chars during print():
	private static final String[] MAXVALUES = { "true", "Byte.MAX_VALUE", "'z'", "Short.MAX_VALUE", "Integer.MAX_VALUE", "Long.MAX_VALUE", "Float.MAX_VALUE", "Double.MAX_VALUE" };
	private static final String[] GOODVALUES = { "true", "49", "'1'", "49", "49", "49L", "49.0f", "49.0d" }; // 49 ~ '1'
	private static final String[] NEGVALUES = { "false", "-1", "'-'", "-1", "-1", "-1L", "-1.0f", "-1.0d" };
	private static final boolean[] IS_NUMERICAL = { false, true, false, true, true, true, true, true };
	private static String MAX_VALUES_STRING = "true|127|z|32767|2147483647|9223372036854775807|3.4028235E38|1.7976931348623157E308|";
	/**
	 * Test programs may use the following placeholders, which are filled in by this method:
	 * <ul>
	 * <li>PRIM a primitive type
	 * <li>BOX the corresponding boxing type
	 * <li>NEGVAL a value of that type signaling failure
	 * <li>VAL a regular value
	 * </ul>
	 * @param template the template with placeholders
	 * @param idx index into {@link #PRIMITIVES} etc.
	 * @return the program snippet with placeholders filled in.
	 */
	private static String fillIn(String template, int idx) {
		return template.replaceAll("PRIM", PRIMITIVES[idx]).replaceAll("BOX", BOXES[idx])
						.replace("NEGVAL", NEGVALUES[idx]).replace("VAL", GOODVALUES[idx]).replace("MAX", MAXVALUES[idx]);
	}
	/** like {@link #fillIn(String, int)}, but may use {@link #MAXVALUES} if 'maxValue' is true. */
	private static String fillInMax(String template, int idx, boolean useMax) {
		return template.replaceAll("PRIM", PRIMITIVES[idx]).replaceAll("BOX", BOXES[idx])
						.replace("NEGVAL", NEGVALUES[idx]).replace("VAL", useMax ? MAXVALUES[idx] : GOODVALUES[idx]);
	}

	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testPrimitivePatternInSwitch" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return PrimitiveInPatternsTestSH.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}
	public PrimitiveInPatternsTestSH(String testName) {
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
						return NEGVAL;
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
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillInMax(callTmpl, i, true));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, MAX_VALUES_STRING);
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
						return NEGVAL;
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
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillInMax(callTmpl, i, true));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, MAX_VALUES_STRING);
	}

	public void testIdentity_functionLhs() {
		// one sample should suffice here:
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

	public void testIdentityPattern_functionLhs() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (barPRIM() instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
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
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
			"true|49|1|49|49|49|49.0|49.0|");
	}

	// Widening primitive conversions
	// 5.1.2: [...] exact widening primitive conversion [...]. Such a conversion can be one of the following:
	// * from an integral type to another integral type
	// * from byte, short, or char to a floating-point type
	// * from int to double
	// * from float to double
	// inexact widening conversions:
	// * from int to float, or from long to float, or from long to double
	private void testWideningFrom(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="long", idx=5, useMax=false, ...):
		//	public class X {
		//		public static float foofloat(long v) {
		//			if (v instanceof float) {
		//				float vv = (float) v;
		//				return vv;
		//			}
		//			return -1.0f;
		//		}
		//		public static double foodouble(long v) {
		//			if (v instanceof double) {
		//				double vv = (double) v;
		//				return vv;
		//			}
		//			return -1.0d;
		//		}
		//		public static void main(String[] args) {
		//			long v = 49L;
		//			System.out.print(X.foofloat(v));
		//			System.out.print('|');
		//			System.out.print(X.foodouble(v));
		//			System.out.print('|');
		//	}}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(FROM v) {
						if (v instanceof PRIM) {
							PRIM vv = (PRIM) v;
							return vv;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.fooPRIM(v));
						System.out.print('|');
				""";
		// for all numerical primitive types "greater" than 'from':
		for (int i = idx+1; i < PRIMITIVES.length; i++) {
			if (!IS_NUMERICAL[i]) continue;
			methods.append(fillIn(methodTmpl.replace("FROM", from), i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(fillInMax("PRIM v = VAL;\n", idx, useMax));
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testWideningFrom_pattern(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="long", idx=5, useMax=false, ..):
		//	public class X {
		//		public static float foofloat() {
		//			if (bar() instanceof float vv) {
		//				return vv;
		//			}
		//			return -1.0f;
		//		}
		//		public static double foodouble() {
		//			if (bar() instanceof double vv) {
		//				return vv;
		//			}
		//			return -1.0d;
		//		}
		//		static long bar() {
		//			return 49L;
		//		}
		//		public static void main(String[] args) {
		//			System.out.print(X.foofloat());
		//			System.out.print('|');
		//			System.out.print(X.foodouble());
		//			System.out.print('|');
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (bar() instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
					}
				""";
		String methodBar = fillInMax("""
					static PRIM bar() {
						return VAL;
					}
					""",
					idx, useMax);
		String callTmpl =
				"""
						System.out.print(X.fooPRIM());
						System.out.print('|');
				""";
		// for all numerical primitive types "greater" than 'from':
		for (int i = idx+1; i < PRIMITIVES.length; i++) {
			if (!IS_NUMERICAL[i]) continue;
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append(methodBar);
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testWideningFrom_both(String prim, int idx, boolean useMax, String expectedOut) {
		testWideningFrom(prim, idx, useMax, expectedOut);
		testWideningFrom_pattern(prim, idx, useMax, expectedOut);
	}
	public void testWideningByte() {
		testWideningFrom_both("byte", 1, false, "49|49|49|49.0|49.0|");
		testWideningFrom_both("byte", 1, true, "127|127|127|127.0|127.0|");
	}
	public void testWideningChar() {
		testWideningFrom_both("char", 2, false, "49|49|49|49.0|49.0|"); // '1'
		testWideningFrom_both("char", 2, true, "122|122|122|122.0|122.0|"); // 'z'
	}
	public void testWideningShort() {
		testWideningFrom_both("short", 3, false, "49|49|49.0|49.0|");
		testWideningFrom_both("short", 3, true, "32767|32767|32767.0|32767.0|");
	}
	public void testWideningInt() {
		testWideningFrom_both("int", 4, false, "49|49.0|49.0|");
		// max-int -> float is not exact
		testWideningFrom_both("int", 4, true, "2147483647|-1.0|"+String.valueOf((double) Integer.MAX_VALUE)+'|');
	}
	public void testWideningLong() {
		testWideningFrom_both("long", 5, false, "49.0|49.0|");
		// max-long -> float/double is not exact
		testWideningFrom_both("long", 5, true, "-1.0|-1.0|");
	}
	public void testWideningFloat() {
		testWideningFrom_both("float", 6, false, "49.0|");
		testWideningFrom_both("float", 6, true, String.valueOf((double) Float.MAX_VALUE)+"|");
	}

	// Narrowing Primitive Double
	private void testNarrowingFrom(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="short", idx=3, useMax=false):
		//	public class X {
		//		public static byte foobyte(short v) {
		//			if (v instanceof byte) {
		//				byte vv = (byte) v;
		//				return vv;
		//			}
		//			return -1;
		//		}
		//		public static char foochar(short v) {
		//			if (v instanceof char) {
		//				char vv = (char) v;
		//				return vv;
		//			}
		//			return '-';
		//		}
		//		static void print(Object o) {
		//			if (o instanceof Character && ((int)((char) o) > 127))
		//				System.out.print((int)((char) o)); // avoid char encoding issues
		//			else
		//				System.out.print(o);
		//			System.out.print('|');
		//		}
		//		public static void main(String[] args) {
		//			short v = 49;
		//			print(X.foobyte(v));
		//			print(X.foochar(v));
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(FROM v) {
						if (v instanceof PRIM) {
							PRIM vv = (PRIM) v;
							return vv;
						}
						return NEGVAL;
					}
				""";
		String methodPrint = """
				static void print(Object o) {
					if (o instanceof Character && ((int)((char) o) > 127))
						System.out.print((int)((char) o)); // avoid char encoding issues
					else
						System.out.print(o);
					System.out.print('|');
				}
				""";
		String callTmpl =
				"""
						print(X.fooPRIM(v));
				""";
		// for all primitive types "smaller" than 'from' (except for boolean):
		for (int i = 1; i < idx; i++) {
			methods.append(fillIn(methodTmpl.replace("FROM", from), i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append(methodPrint);
		classX.append("public static void main(String[] args) {\n");
		classX.append(fillInMax("PRIM v = VAL;\n", idx, useMax));
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testNarrowingFrom_pattern(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="short", idx=3, useMax=false, ...):
		//	public class X {
		//		public static byte foobyte() {
		//			if (bar() instanceof byte vv) {
		//				return vv;
		//			}
		//			return -1;
		//		}
		//		public static char foochar() {
		//			if (bar() instanceof char vv) {
		//				return vv;
		//			}
		//			return '-';
		//		}
		//		static short bar() {
		//			return 49;
		//		}
		//		static void print(Object o) {
		//			if (o instanceof Character && (int)((char) o) > 127)
		//				System.out.print((int)((char) o)); // avoid char encoding issues
		//			else
		//				System.out.print(o);
		//			System.out.print('|');
		//		}
		//		public static void main(String[] args) {
		//			print(X.foobyte());
		//			print(X.foochar());
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (bar() instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
					}
				""";
		String methodBar = fillInMax("""
				static PRIM bar() {
					return VAL;
				}
				""",
				idx, useMax);
		String methodPrint = """
				static void print(Object o) {
					if (o instanceof Character && ((int)((char) o) > 127))
						System.out.print((int)((char) o)); // avoid char encoding issues
					else
						System.out.print(o);
					System.out.print('|');
				}
				""";
		String callTmpl =
				"""
						print(X.fooPRIM());
				""";
		// for all primitive types "smaller" than 'from' (except for boolean):
		for (int i = 1; i < idx; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append(methodBar);
		classX.append(methodPrint);
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testNarrowingFrom_both(String prim, int idx, boolean useMax, String expectedOut) {
		testNarrowingFrom(prim, idx, useMax, expectedOut);
		testNarrowingFrom_pattern(prim, idx, useMax, expectedOut);
	}
	public void testNarrowingDouble() {
		testNarrowingFrom_both("double", 7, false, "49|1|49|49|49|49.0|");
		testNarrowingFrom_both("double", 7, true, "-1|-|-1|-1|-1|-1.0|");
	}
	public void testNarrowingFloat() {
		testNarrowingFrom_both("float", 6, false, "49|1|49|49|49|");
		testNarrowingFrom_both("float", 6, true, "-1|-|-1|-1|-1|");
	}
	public void testNarrowingLong() {
		testNarrowingFrom_both("long", 5, false, "49|1|49|49|");
		testNarrowingFrom_both("long", 5, true, "-1|-|-1|-1|");
	}
	public void testNarrowingInt() {
		testNarrowingFrom_both("int", 4, false, "49|1|49|");
		testNarrowingFrom_both("int", 4, true, "-1|-|-1|");
	}
	public void testNarrowingShort() {
		testNarrowingFrom_both("short", 3, false, "49|1|");
		testNarrowingFrom_both("short", 3, true, "-1|32767|");
	}
	public void testNarrowingChar() {
		testNarrowingFrom_both("char", 2, false, "49|"); // '1'
	}

	public void testNarrowingChar_various() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static char b2c(byte b) {
						if (b instanceof char) {
							return (char) b;
						}
						return '-';
					}
					public static char s2c(short s) {
						if (s instanceof char) {
							return (char) s;
						}
						return '-';
					}
					public static short c2s(char c) {
						if (c instanceof short) {
							return (short) c;
						}
						return -1;
					}
					public static char b2c_pat(byte b) {
						if (b instanceof char v) {
							return v;
						}
						return '-';
					}
					public static char s2c_pat(short s) {
						if (s instanceof char v) {
							return v;
						}
						return '-';
					}
					public static short c2s_pat(char c) {
						if (c instanceof short v) {
							return v;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b=49, bmax=Byte.MAX_VALUE;
						short s=49, smax=Short.MAX_VALUE;
						char c='1', cmax=Character.MAX_VALUE;
						print(X.b2c(b));
						print(X.s2c(s));
						print(X.c2s(c));
						print(X.b2c(bmax));
						print(X.s2c(smax));
						print(X.c2s(cmax));
						System.out.println();
						print(X.b2c_pat(b));
						print(X.s2c_pat(s));
						print(X.c2s_pat(c));
						print(X.b2c_pat(bmax));
						print(X.s2c_pat(smax));
						print(X.c2s_pat(cmax));
					}
					static void print(Object s) {
						if (s instanceof Character)
							System.out.print((int)((char) s)); // avoid char encoding issues
						else
							System.out.print(s);
						System.out.print('|');
					}
				}
				"""
			},
			"49|49|49|127|32767|-1|\n" +
			"49|49|49|127|32767|-1|");
	}

	public void testBoxing() {
		//	public class X {
		//		public static Boolean boolean2Boolean(boolean v) {
		//			if (v instanceof Boolean) {
		//				return (Boolean) v;
		//			}
		//			return false;
		//		}
		//		public static Byte byte2Byte(byte v) {
		//			if (v instanceof Byte) {
		//				return (Byte) v;
		//			}
		//			return -1;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			boolean vboolean = true;
		//			System.out.print(X.boolean2Boolean(vboolean));
		//			System.out.print('|');
		//			byte vbyte = 49;
		//			System.out.print(X.byte2Byte(vbyte));
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static BOX PRIM2BOX(PRIM v) {
						if (v instanceof BOX) {
							return (BOX) v;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.PRIM2BOX(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}
	public void testBoxing_pattern() {
		//	public class X {
		//		public static Boolean boolean2Boolean() {
		//			if (barboolean() instanceof Boolean v) {
		//				return v;
		//			}
		//			return false;
		//		}
		//		static boolean barboolean() {
		//			return true;
		//		}
		//		public static Byte byte2Byte() {
		//			if (barbyte() instanceof Byte v) {
		//				return v;
		//			}
		//			return -1;
		//		}
		//		static byte barbyte() {
		//			return 49;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			System.out.print(X.boolean2Boolean());
		//			System.out.print('|');
		//			System.out.print(X.byte2Byte());
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static BOX PRIM2BOX() {
						if (barPRIM() instanceof BOX v) {
							return v;
						}
						return NEGVAL;
					}
					static PRIM barPRIM() {
						return VAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.PRIM2BOX());
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}

	public void testUnboxing() {
		//	public class X {
		//		public static boolean Boolean2boolean(Boolean v) {
		//			if (v instanceof boolean) {
		//				return (boolean) v;
		//			}
		//			return false;
		//		}
		//		public static byte Byte2byte(Byte v) {
		//			if (v instanceof byte) {
		//				return (byte) v;
		//			}
		//			return -1;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			Boolean vBoolean = true;
		//			System.out.print(X.Boolean2boolean(vBoolean));
		//			System.out.print('|');
		//			Byte vByte = 49;
		//			System.out.print(X.Byte2byte(vByte));
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM BOX2PRIM(BOX v) {
						if (v instanceof PRIM) {
							return (PRIM) v;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						BOX vBOX = VAL;
						System.out.print(X.BOX2PRIM(vBOX));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}
	public void testUnboxing_pattern() {
		//	public class X {
		//		public static Boolean boolean2Boolean() {
		//			if (barboolean() instanceof Boolean v) {
		//				return v;
		//			}
		//			return false;
		//		}
		//		static boolean barboolean() {
		//			return true;
		//		}
		//		public static Byte byte2Byte() {
		//			if (barbyte() instanceof Byte v) {
		//				return v;
		//			}
		//			return -1;
		//		}
		//		static byte barbyte() {
		//			return 49;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			System.out.print(X.boolean2Boolean());
		//			System.out.print('|');
		//			System.out.print(X.byte2Byte());
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM BOX2PRIM() {
						if (barBOX() instanceof PRIM v) {
							return v;
						}
						return NEGVAL;
					}
					static BOX barBOX() {
						return VAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.BOX2PRIM());
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}

	// boxing and widening reference conversion

	private void primitive2Comparable(String prim, int idx, String expectedOut) {
		String methodTmpl =
				"""
					@SuppressWarnings("rawtypes")
					public static Comparable foo1(PRIM v) {
						if (v instanceof Comparable r) {
							return r;
						}
						return null;
					}
					@SuppressWarnings({"rawtypes", "unchecked" })
					public static Comparable<BOX> foo2(PRIM v) {
						if (v instanceof Comparable r) {
							return r;
						}
						return null;
					}
					@SuppressWarnings("unchecked")
					public static Comparable<BOX> foo3(PRIM v) {
						if (v instanceof Comparable<BOX> r) {
							return r;
						}
						return null;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.foo1(vPRIM));
						System.out.print('|');
						System.out.print(X.foo2(vPRIM));
						System.out.print('|');
						System.out.print(X.foo3(vPRIM));
						System.out.print('|');
				""";
		if (IS_NUMERICAL[idx]) {
			methodTmpl +=
				"""
					public static Number foo4(PRIM v) {
						if (v instanceof Number r) {
							return r;
						}
						return null;
					}
				""";
			callTmpl +=
				"""
					System.out.print(X.foo4(vPRIM));
					System.out.print('|');
				""";
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(fillIn(methodTmpl, idx));
		classX.append("public static void main(String[] args) {\n");
		classX.append(fillIn(callTmpl, idx));
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}

	public void test2Comparable_boolean() {
		primitive2Comparable("boolean", 0, "true|true|true|");
	}
	public void test2Comparable_byte() {
		primitive2Comparable("byte", 1, "49|49|49|49|");
	}
	public void test2Comparable_char() {
		primitive2Comparable("char", 2, "1|1|1|"); // '1'
	}
	public void test2Comparable_short() {
		primitive2Comparable("short", 3, "49|49|49|49|");
	}
	public void test2Comparable_int() {
		primitive2Comparable("int", 4, "49|49|49|49|");
	}
	public void test2Comparable_long() {
		primitive2Comparable("long", 5, "49|49|49|49|");
	}
	public void test2Comparable_float() {
		primitive2Comparable("float", 6, "49.0|49.0|49.0|49.0|");
	}
	public void test2Comparable_double() {
		primitive2Comparable("double", 7, "49.0|49.0|49.0|49.0|");
	}
	public void test2Number_NOK() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Number foo1(boolean b) {
						if (b instanceof Number r) {
							return r;
						}
						return null;
					}
					public static Number foo2(char c) {
						if (c instanceof Number r) {
							return r;
						}
						return null;
					}
				}
  			    """
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				if (b instanceof Number r) {
				    ^^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types boolean and Number
			----------
			2. ERROR in X.java (at line 9)
				if (c instanceof Number r) {
				    ^^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types char and Number
			----------
			""");
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

	public void testPrimitivePatternInSwitch() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM switchPRIM(PRIM in) {
						return switch (in) {
							case MAX -> NEGVAL;
							case PRIM v -> v;
						};
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.switchPRIM(vPRIM));
						System.out.print('|');
						vPRIM = MAX;
						System.out.print(X.switchPRIM(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"false|false|49|-1|1|-|49|-1|49|-1|49|-1|49.0|-1.0|49.0|-1.0|");
	}

	public void testPrimitivePatternInSwitch_more() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					public static String switchbool(boolean in) {
						// generic test couldn't differentiate cases by output
						return switch (in) {
							case true -> "true";
							case boolean v -> "v="+String.valueOf(v);
						};
					}
					public static String switchfloatMoreCases(float f) {
						return switch (f) {
							case 1.0f -> "1.0";
							case 1.5f -> "1.5";
							case float v -> "v="+String.valueOf(v);
						};
					}
					public static void main(String... args) {
						System.out.print(switchbool(true));
						System.out.print("|");
						System.out.print(switchbool(false));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.0f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.5f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.6f));
						System.out.print("|");
					}
				}
				"""},
				"true|v=false|1.0|1.5|v=1.6|");
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
