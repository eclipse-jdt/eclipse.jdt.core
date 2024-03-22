/*******************************************************************************
 * Copyright (c) 2021, 2023 GK Software SE, and others.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NullAnnotationTests21 extends AbstractNullAnnotationTest {

	public NullAnnotationTests21(String name) {
		super(name);
	}

	static {
//			TESTS_NAMES = new String[] { "test_totalTypePatternNonNullExpression" };
//			TESTS_NUMBERS = new int[] { 001 };
//			TESTS_RANGE = new int[] { 1, 12 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}

	public static Class<?> testClass() {
		return NullAnnotationTests21.class;
	}

	@Deprecated // super method is deprecated
	@Override
	protected void setUpAnnotationLib() throws IOException {
		if (this.LIBS == null) {
			String[] defaultLibs = getDefaultClassPaths();
			int len = defaultLibs.length;
			this.LIBS = new String[len+1];
			System.arraycopy(defaultLibs, 0, this.LIBS, 0, len);
			this.LIBS[len] = NullAnnotationTests9.createAnnotation_2_2_jar(Util.getOutputDirectory() + File.separator, null);
		}
	}

	// -------- helper ------------

	private Runner getDefaultRunner() {
		Runner runner = new Runner();
		runner.classLibraries = this.LIBS;
		Map<String,String> opts = getCompilerOptions();
		opts.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		runner.customOptions = opts;
		runner.javacTestOptions =
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		return runner;
	}

	// --------- tests start -----------

	public void test_typePatternIsNN() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(Object o) {
							switch (o) {
								case Integer i -> consumeInt(i);
								default -> System.out.println("default");
							}
						}
						void consumeInt(@NonNull Integer i) {
							System.out.print(i);
						}
						public static void main(String... args) {
							new X().foo(3);
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "3";
		runner.runConformTest();
	}

	public void test_totalTypePatternDoesNotAdmitNull() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(Number n) {
							try {
								switch (n) {
									case Integer i -> consumeInt(i);
									case Number n0 -> consumeNumber(n0);
								}
							} catch (NullPointerException npe) {
								// ignoring the unchecked warning, and expecting the NPE:
								System.out.print(npe.getMessage());
							}
						}
						void consumeInt(@NonNull Integer i) {
							System.out.print(i);
						}
						void consumeNumber(@NonNull Number n) {
							System.out.print(n.toString());
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 7)
						case Number n0 -> consumeNumber(n0);
						                                ^^
					Null type safety (type annotations): The expression of type \'Number\' needs unchecked conversion to conform to \'@NonNull Number\'
					----------
					""";
//		runner.expectedOutputString = "Cannot invoke \"Object.toString()\" because \"n\" is null";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_totalTypePatternNonNullExpression() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(Number n) {
							if (n == null) return;
							switch (n) {
								case Integer i -> System.out.print(i);
								case Number n0 -> consumeNumber(n0);
							}
						}
						void consumeNumber(@NonNull Number n) {
							System.out.print(n.toString());
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "";
		runner.runConformTest();
	}

	public void test_totalTypePatternNonNullExpression_swExpr() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						int foo(Number n) {
							if (n == null) return -1;
							return switch (n) {
								case Integer i -> i;
								case Number n0 -> consumeNumber(n0);
							};
						}
						int consumeNumber(@NonNull Number n) {
							return Integer.valueOf(n.toString());
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "";
		runner.runConformTest();
	}

	public void test_totalTypePatternPlusNullPattern() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(Number n) {
							switch (n) {
								case null -> System.out.print("null");
								case Integer i -> System.out.print(i);
								case Number n0 -> consumeNumber(n0);
							}
						}
						void consumeNumber(@NonNull Number n) {
							System.out.print(n.toString());
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_totalTypePatternNullableExpression() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(@Nullable Number n) {
							switch (n) {
								case Integer i -> System.out.print(i);
								case Number n0 -> consumeNumber(n0);
							}
						}
						void consumeNumber(@NonNull Number n) {
							System.out.print(n.toString());
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 6)
						case Number n0 -> consumeNumber(n0);
						                                ^^
					Null type mismatch: required \'@NonNull Number\' but the provided value is inferred as @Nullable
					----------
					""";
		runner.runNegativeTest();
	}

	public void test_switchOverNNValueWithNullCase() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(@NonNull Object o) {
							switch (o) {
								case Integer i -> consumeInt(i);
								case null -> System.out.print("null");
								default -> System.out.println("default");
							}
						}
						void consumeInt(@NonNull Integer i) {
							System.out.print(i);
						}
						public static void main(String... args) {
							new X().foo(3);
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 6)
						case null -> System.out.print("null");
						^^^^^^^^^
					Unnecessary \'null\' pattern, the switch selector expression cannot be null
					----------
					""";
		runner.expectedOutputString = "3";
		runner.runConformTest();
	}

	// null cannot be in the same case with pattern as per the 432+433 jep
	public void _test_switchNullInSameCase() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(Object o) {
							switch (o) {
								case null, Integer i -> consumeInt(i);
								default -> System.out.println("default");
							}
						}
						void consumeInt(@NonNull Integer i) {
							System.out.print(i);
						}
						public static void main(String... args) {
							new X().foo(3);
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 5)
						case null, Integer i -> consumeInt(i);
						                                   ^
					Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable
					----------
					""";
		runner.runNegativeTest();
	}

	public void test_switchOverNNValueWithNullCase_swExpr() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						int foo(@NonNull Object o) {
							return switch (o) {
								case Integer i -> consumeInt(i);
								case null -> 0;
								default -> -1;
							};
						}
						int consumeInt(@NonNull Integer i) {
							return i;
						}
						public static void main(String... args) {
							System.out.print(new X().foo(3));
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 6)
						case null -> 0;
						^^^^^^^^^
					Unnecessary \'null\' pattern, the switch selector expression cannot be null
					----------
					""";
		runner.expectedOutputString = "3";
		runner.runConformTest();
	}

	public void test_nullHostileSwitch() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(@Nullable Object o) {
							switch (o) {
								case Integer i -> consumeInt(i);
								default -> System.out.println(o);
							};
						}
						void consumeInt(@NonNull Integer i) {
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 4)
						switch (o) {
						        ^
					Potential null pointer access: this expression has a \'@Nullable\' type
					----------
					""";
		runner.runNegativeTest();
	}

	public void test_defaultDoesNotApplyToNull() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						void foo(@Nullable Object o) {
							switch (o) {
								case Integer i -> consumeInt(i);
								case null -> System.out.print("null");
								default -> System.out.println(o.toString());
							};
						}
						void consumeInt(@NonNull Integer i) {
						}
						public static void main(String... args) {
							new X().foo(null);
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_defaultDoesNotApplyToNull_field() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						@Nullable Object o;
						void foo() {
							switch (this.o) {
								case Integer i -> consumeInt(i);
								case null -> System.out.print("null");
								default -> System.out.println(this.o.toString());
							};
						}
						void consumeInt(@NonNull Integer i) {
						}
						public static void main(String... args) {
							new X().foo();
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void test_defaultDoesNotApplyToNull_field2() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class X {
						@Nullable Object o;
						void foo(X x) {
							switch (x.o) {
								case Integer i -> consumeInt(i);
								case null -> System.out.print("null");
								default -> System.out.println(x.o.toString());
							};
						}
						void consumeInt(@NonNull Integer i) {
						}
						public static void main(String... args) {
							new X().foo(new X());
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "null";
		runner.runConformTest();
	}

	public void testBug576329() {
		Runner runner = getDefaultRunner();
		runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
				"Main.java",
				"""
					public class Main {
					    int length;
					    public String switchOnArray(Object argv[]) {
					        return switch(argv.length) {
					        case 0 -> "0";
					        default -> "x";
					        };
					    }
						public static void main(String... args) {
							System.out.print(new Main().switchOnArray(args));
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "0";
		runner.runConformTest();
	}

	public void testInstanceOfPatternIsNonNull() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						public static void consumeNonNull(@NonNull String s) {
							System.out.println("nonnull");
						}
						public static void main(String... args) {
							Object o = Math.random() < 0 ? new Object() : "blah";
							if (o instanceof String message) {
								consumeNonNull(message);
							}
						}
					}
					"""
			};
		runner.expectedCompilerLog = "";
		runner.expectedOutputString = "nonnull";
		runner.runConformTest();
	}

	public void testInstanceOfPatternIsLaterAssignedNull() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						public static void consumeNonNull(@NonNull String s) {
							System.out.println("nonnull");
						}
						public static void main(String... args) {
							Object o = Math.random() >= 0 ? new Object() : "blah";
							if (o instanceof String message) {
								consumeNonNull(message);
								message = null;
								consumeNonNull(message);
							}
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 11)
						consumeNonNull(message);
						               ^^^^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					""";
		runner.runNegativeTest();
	}

	// since 11: uses 'var'
	public void testNullableVar() {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"Test.java",
				"""
					
					import org.eclipse.jdt.annotation.NonNull;
					
					public class Test {
						public @NonNull Test getSomeValue() { return this; }
					\t
						void test(boolean rainyDay) {
							var a = rainyDay ? getSomeValue() : null;
							a.getSomeValue(); // problem not detected
						}
						void test2(boolean rainyDay) {
							Test a = rainyDay ? getSomeValue() : null;
							a.getSomeValue(); // Potential null pointer access: The variable a may be null at this location
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in Test.java (at line 9)
						a.getSomeValue(); // problem not detected
						^
					Potential null pointer access: The variable a may be null at this location
					----------
					2. ERROR in Test.java (at line 13)
						a.getSomeValue(); // Potential null pointer access: The variable a may be null at this location
						^
					Potential null pointer access: The variable a may be null at this location
					----------
					""";
		runner.runNegativeTest();
	}
	public void _testGH629_01() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_18);
		options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "test.NonNull");
		options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "test.Nullable");
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

		runNegativeTestWithLibs(
				new String[] {
						"Configuration.java",
						"public interface Configuration {\n" +
								"}\n",
						"Init.java",
						"public interface Init<C extends Configuration> {\n" +
								"}\n",
						"Annot.java",
						"""
							public @interface Annot {
							    Class<? extends Init<? extends Configuration>>[] inits();\s
							}
							""",
						"App.java",
						"""
							interface I<T> {}
							@Annot(inits = {App.MyInit.class})
							public class App {
							    static class MyInit implements I<String>, Init<Configuration> {}
							}
							"""
				},
				options,
				"");
	}
	public void _testGH629_02() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_18);
		options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "test.NonNull");
		options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "test.Nullable");
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

		runNegativeTestWithLibs(
				new String[] {
						"Annot.java",
						"""
							public @interface Annot {
							    Class<? extends Init<? extends Configuration>>[] inits();\s
							}
							""",
						"App.java",
						"""
							@Annot(inits = {App.MyInit.class})
							public class App {
							    static class MyInit implements Init<Configuration> {}
							}
							""",
						"Configuration.java",
						"public interface Configuration {\n" +
								"}\n",
						"Init.java",
						"public interface Init<C extends Configuration> {\n" +
								"}\n"
				},
				options,
				"");
	}
	public void testBug572361() {
		runConformTestWithLibs(
			new String[] {
				"NonNullByDefaultAndRecords.java",
				"""
					import org.eclipse.jdt.annotation.NonNullByDefault;
					
					@NonNullByDefault
					public record NonNullByDefaultAndRecords () { }
					"""
			},
			getCompilerOptions(),
			"");
	}

	public void testIssue233_ok() throws Exception {
		Runner runner = getDefaultRunner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
		runner.testFiles = new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					record A1(@NonNull String ca1, String ca2) {}
					record B1(@Nullable String cb1, String cb2) {}
					@NonNullByDefault
					public class X {
						record A2(@NonNull String ca1, String ca2) {}
						record B2(@Nullable String cb1, String cb2) {}
					
						public static @NonNull String workWithA(A1 a, boolean f) {
							return f ? a.ca1() : a.ca2();
						}
						public static @NonNull String workWithA(A2 a, boolean f) {
							return f ? a.ca1() : a.ca2();
						}
						public static String workWithB(B1 b, boolean f) {
							if (f) {
								String c = b.cb1();
								return c != null ? c : "default ";
							}
							return b.cb2();
						}
						public static String workWithB(B2 b, boolean f) {
							if (f) {
								String c = b.cb1();
								return c != null ? c : "default ";
							}
							return b.cb2();
						}
						public static void main(String... args) {
							@NonNull String sa11 = workWithA(new A1("hello ", "A11 "), true);
							@NonNull String sa12 = workWithA(new A1("hello ", "A12 "), false);
							@NonNull String sb11 = workWithB(new B1(null, "B11 "), true);
							@NonNull String sb12 = workWithB(new B1(null, "B12 "), false);
							@NonNull String sa21 = workWithA(new A2("hello ", "A21 "), true);
							@NonNull String sa22 = workWithA(new A2("hello ", "A22 "), false);
							@NonNull String sb21 = workWithB(new B2(null, "B21"), true);
							@NonNull String sb22 = workWithB(new B2(null, "B22"), false);
							System.out.println(sa11+sa12+sb11+sb12+sa21+sa22+sb21+sb22);
						}
					}
					"""
			};
		runner.expectedOutputString = "hello A12 default B12 hello A22 default B22";
		runner.runConformTest();
	}
	public void testIssue233_nok() throws Exception {
		// like testIssue233_ok - but annotations on record components ca1 / cb1 swapped (twice)
		Runner runner = getDefaultRunner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
		runner.testFiles = new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					record A1(@Nullable String ca1, String ca2) {}
					record B1(@NonNull String cb1, String cb2) {}
					@NonNullByDefault
					public class X {
						record A2(@Nullable String ca1, String ca2) {}
						record B2(@NonNull String cb1, String cb2) {}
					
						public static @NonNull String workWithA(A1 a, boolean f) {
							return f ? a.ca1() : a.ca2();
						}
						public static @NonNull String workWithA(A2 a, boolean f) {
							return f ? a.ca1() : a.ca2();
						}
						public static String workWithB(B1 b, boolean f) {
							if (f) {
								String c = b.cb1();
								return c != null ? c : "default ";
							}
							return b.cb2();
						}
						public static String workWithB(B2 b, boolean f) {
							if (f) {
								String c = b.cb1();
								return c != null ? c : "default ";
							}
							return b.cb2();
						}
						public static void main(String... args) {
							@NonNull String sa11 = workWithA(new A1("hello ", "A11 "), true);
							@NonNull String sa12 = workWithA(new A1("hello ", "A12 "), false);
							@NonNull String sb11 = workWithB(new B1(null, "B11 "), true);
							@NonNull String sb12 = workWithB(new B1(null, "B12 "), false);
							@NonNull String sa21 = workWithA(new A2("hello ", "A21 "), true);
							@NonNull String sa22 = workWithA(new A2("hello ", "A22 "), false);
							@NonNull String sb21 = workWithB(new B2(null, "B21"), true);
							@NonNull String sb22 = workWithB(new B2(null, "B22"), false);
							System.out.println(sa11+sa12+sb11+sb12+sa21+sa22+sb21+sb22);
						}
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 10)
						return f ? a.ca1() : a.ca2();
						           ^^^^^^^
					Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
					----------
					2. WARNING in X.java (at line 10)
						return f ? a.ca1() : a.ca2();
						                     ^^^^^^^
					Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'
					----------
					3. ERROR in X.java (at line 13)
						return f ? a.ca1() : a.ca2();
						           ^^^^^^^
					Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
					----------
					4. ERROR in X.java (at line 18)
						return c != null ? c : "default ";
						       ^
					Redundant null check: The variable c cannot be null at this location
					----------
					5. WARNING in X.java (at line 20)
						return b.cb2();
						       ^^^^^^^
					Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'
					----------
					6. ERROR in X.java (at line 25)
						return c != null ? c : "default ";
						       ^
					Redundant null check: The variable c cannot be null at this location
					----------
					7. ERROR in X.java (at line 32)
						@NonNull String sb11 = workWithB(new B1(null, "B11 "), true);
						                                        ^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					8. ERROR in X.java (at line 33)
						@NonNull String sb12 = workWithB(new B1(null, "B12 "), false);
						                                        ^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					9. ERROR in X.java (at line 36)
						@NonNull String sb21 = workWithB(new B2(null, "B21"), true);
						                                        ^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					10. ERROR in X.java (at line 37)
						@NonNull String sb22 = workWithB(new B2(null, "B22"), false);
						                                        ^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					""";
		runner.expectedOutputString = "hellodefault";
		runner.runNegativeTest();
	}
	public void testIssue233_npeWitness() throws Exception {
		Runner runner = getDefaultRunner();
		runner.testFiles = new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public record X(@NonNull String ca1, String ca2, @Nullable String ca2) {}\n"
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 2)
						public record X(@NonNull String ca1, String ca2, @Nullable String ca2) {}
						                                            ^^^
					Duplicate component ca2 in record
					----------
					2. ERROR in X.java (at line 2)
						public record X(@NonNull String ca1, String ca2, @Nullable String ca2) {}
						                                                                  ^^^
					Duplicate component ca2 in record
					----------
					3. ERROR in X.java (at line 2)
						public record X(@NonNull String ca1, String ca2, @Nullable String ca2) {}
						                                                                  ^^^
					Duplicate parameter ca2
					----------
					""";
		runner.runNegativeTest();
	}

	public void testGH1399() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"C.java",
				"""
				@interface Ann { Class<? extends A> value(); }
				class A {}
				@Ann(C.B.class) // <- ERROR: Type mismatch: cannot convert from Class<C.B> to Class<? extends A>
				class C<T extends Number> {
				    class B extends A {}
				}
				"""};
		runner.runConformTest();
	}

	public void testGH1399_2() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"C.java",
				"""
				@interface Ann { Class<? extends A> value(); }
				class A {}
				@Ann(C.B.class)
				class C<T extends java.util.List<Number>> {
				    class B extends A {}
				}
				"""};
		runner.runConformTest();
	}
	public void testGH1302() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p/package-info.java",
			"""
			@org.eclipse.jdt.annotation.NonNullByDefault
			package p;
			""",
			"p/Parent.java",
			"""
			package p;
			import java.util.Map;
			public interface Parent {
			  Map<String, String> model();
			}
			""",
			"p/Child.java",
			"""
			package p;
			import java.util.Map;
			public record Child(Map<String, String> model) implements Parent {
			}
			"""
		};
		runner.customOptions = getCompilerOptions();
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}

	public void testGH1691_a() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"bug/package-info.java",
			"""
			@org.eclipse.jdt.annotation.NonNullByDefault
			package bug;
			""",
			"bug/BlahSuper.java",
			"""
			package bug;

			import java.io.IOException;
			import java.io.OutputStream;

			public sealed interface BlahSuper<T, E extends Exception> permits Blah, BlahOther { }
			abstract non-sealed class BlahOther implements BlahSuper<OutputStream, IOException> { }
			""",
			"bug/Blah.java",
			"""
			package bug;

			import java.io.IOException;
			import java.io.OutputStream;

			public abstract non-sealed class Blah<T, E extends Exception> implements BlahSuper<T, E> {
				public abstract static class InnerBlah extends Blah<OutputStream, IOException> { }
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}


	public void testGH1691_b() {
		// @NonNull on secondary bound is sufficient
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"bug/Marker.java",
			"""
			package bug;
			public interface Marker {}
			""",
			"bug/MyException.java",
			"""
			package bug;
			import java.io.IOException;
			public class MyException extends IOException implements Marker {}
			""",
			"bug/BlahSuper.java",
			"""
			package bug;

			import java.io.OutputStream;
			import org.eclipse.jdt.annotation.NonNullByDefault;

			@NonNullByDefault
			public sealed interface BlahSuper<T, E extends Exception & Marker> permits Blah, BlahOther { }
			@NonNullByDefault
			abstract non-sealed class BlahOther implements BlahSuper<OutputStream, MyException> { }
			""",
			"bug/Blah.java",
			"""
			package bug;

			import java.io.OutputStream;
			import org.eclipse.jdt.annotation.NonNull;

			public abstract non-sealed class Blah<T, E extends Exception & @NonNull Marker> implements BlahSuper<T, E> {
				public abstract static class InnerBlah extends Blah<OutputStream, @NonNull MyException> { }
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}
	public void testGH1009() {
		Runner runner = new Runner();
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAnnotatedTypeArgumentToUnannotated, CompilerOptions.ERROR);
		runner.customOptions = options;
		runner.testFiles = new String[] {
			"UnsafeNullTypeConversionFalsePositive.java",
			"""
			import java.util.ArrayList;
			import java.util.List;
			import java.util.stream.Collectors;

			import org.eclipse.jdt.annotation.NonNull;

			public class UnsafeNullTypeConversionFalsePositive {

				public static void main(final String[] args) {
					final List<@NonNull StringBuffer> someList = new ArrayList<>();
					List<@NonNull String> results;
					// was buggy:
					results = someList.stream().map(String::new).collect(Collectors.toList());
					// was OK:
					results = someList.stream().map(buff -> new String(buff)).collect(Collectors.toList());
					results = someList.stream().<@NonNull String>map(String::new).collect(Collectors.toList());
				}
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}

	public void testGH1760() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"X.java",
			"""
			import org.eclipse.jdt.annotation.*;
			import java.util.*;
			import java.util.stream.*;
			public class X {
				List<@NonNull String> filter(List<@Nullable String> input) {
					return input.stream()
								.filter(Objects::nonNull)
								.collect(Collectors.toList());
				}
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}

	public void testGH1964_since_22() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runner.customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_22);
		runner.customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_22);
		runner.customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_22);
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.testFiles = new String[] {
			"JDK21TestingMain.java",
			"""
			import static java.util.FormatProcessor.FMT;
			import static java.lang.StringTemplate.RAW;

			public final class JDK21TestingMain
			{
			  public static void main(final String[] args)
			  {
			    final int fourtyTwo = 42;
			    final String str = FMT."\\{fourtyTwo}";

			    final int x=1;
			    final int y=2;
			    final StringTemplate st = RAW."\\{x} + \\{y} = \\{x + y}";

			    final var x1 = STR."Hello World";
			    final var x2 = FMT."Hello World";
			    final var x3 = RAW."Hello World";

			    System.out.println(STR."Hello World");

			    System.out.println();
			  }
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}
	public void testGH1771() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportNullSpecViolation, CompilerOptions.ERROR);
		runner.customOptions.put(CompilerOptions.OPTION_InheritNullAnnotations, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
			"p/Foo.java",
			"""
			package p;

			public interface Foo
			{
				@org.eclipse.jdt.annotation.NonNullByDefault
				record Bar(Object foo) implements
				          Foo
				{
				}

				@org.eclipse.jdt.annotation.Nullable
				Object foo();
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.expectedCompilerLog = """
			----------
			1. ERROR in p\\Foo.java (at line 6)
				record Bar(Object foo) implements
				           ^^^^^^
			The default '@NonNull' conflicts with the inherited '@Nullable' annotation in the overridden method from Foo
			----------
			""";
		runner.runNegativeTest();
	}
	public void testGH1771_corrected() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportNullSpecViolation, CompilerOptions.ERROR);
		runner.customOptions.put(CompilerOptions.OPTION_InheritNullAnnotations, CompilerOptions.ENABLED);
		runner.testFiles = new String[] {
			"p/Foo.java",
			"""
			package p;

			public interface Foo
			{
				@org.eclipse.jdt.annotation.NonNullByDefault
				record Bar(@org.eclipse.jdt.annotation.Nullable Object foo) implements
				          Foo
				{
				}

				@org.eclipse.jdt.annotation.Nullable
				Object foo();
			}
			"""
		};
		runner.classLibraries = this.LIBS;
		runner.runConformTest();
	}
}
