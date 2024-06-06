/*******************************************************************************
 * Copyright (c) 2016, 2020 Stephan Herrmann and others.
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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NullChecksTests extends AbstractNullAnnotationTest {

	public NullChecksTests(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testAssertNonNull1" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class<NullChecksTests> testClass() {
		return NullChecksTests.class;
	}

	public void testAssertNonNull1() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						@SuppressWarnings("null")
						static @NonNull String hide(String some) {
							return some;
						}
						public static void main(String... args) {
							@NonNull String myHiddenNull = hide(null);
							try {
								assertNonNull("foo", myHiddenNull);
							} catch (NullPointerException npe) {
								System.out.println(npe.getMessage());
							}
							try {
								assertNonNullWithMessage("Shouldn't!", "foo", myHiddenNull);
							} catch (NullPointerException npe) {
								System.out.println(npe.getMessage());
							}
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"Value in position 1 must not be null\n" +
			"Shouldn\'t!");
	}

	public void testAssertNonNullElements() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					import java.util.*;
					public class X {
						@SuppressWarnings("null")
						static @NonNull String hide(String some) {
							return some;
						}
						public static void main(String... args) {
							@NonNull List<String> myList = new ArrayList<>();
							myList.add("foo");
							myList.add(null);
							try {
								assertNonNullElements(myList);
							} catch (NullPointerException npe) {
								System.out.println(npe.getMessage());
							}
							@NonNull List<@NonNull String> myList2 = new ArrayList<>();
							myList2.add("foo");
							myList2.add(hide(null));
							try {
								assertNonNullElements(myList2, "Shouldn't!");
							} catch (NullPointerException npe) {
								System.out.println(npe.getMessage());
							}
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"Value in position 1 must not be null\n" +
			"Shouldn\'t!");
	}

	public void testRequireNonNull() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						@SuppressWarnings("null")
						static @NonNull String hide(String some) {
							return some;
						}
						static void test(@Nullable String str, @Nullable X x) {
							@NonNull String nnStr;
							@NonNull X nnX;
							try {
								nnStr = requireNonNull(str);
								nnX = requireNonNull(null, "Shouldn't!");
							} catch (NullPointerException npe) {
								System.out.println(npe.getMessage());
							}
						}
						public static void main(String... args) {
							test("foo", null);
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"Shouldn\'t!");
	}

	public void testRequireNonEmptyString() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						@SuppressWarnings("null")
						static @NonNull String hide(String some) {
							return some;
						}
						static void test(@Nullable String str1, @Nullable String str2) {
							@NonNull String nnStr;
							try {
								nnStr = requireNonEmpty(str1);
							} catch (NullPointerException npe) {
								System.out.println("npe:"+npe.getMessage());
							}
							try {
								nnStr = requireNonEmpty(str2, "Shouldn't!");
							} catch (NullPointerException npe) {
								System.out.println("npe"+npe.getMessage());
							} catch (IllegalArgumentException iae) {
								System.out.println(iae.getMessage());
							}
						}
						public static void main(String... args) {
							test(null, "");
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"npe:null\n" +
			"Shouldn\'t!");
	}

	public void testRequireNonEmptyCollection() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						static void test(@Nullable Collection<String> strs, @Nullable Collection<String> strs1, Collection<String> strs2) {
							@NonNull Collection<String> nnStrs;
							try {
								nnStrs = requireNonEmpty(strs);
							} catch (NullPointerException npe) {
								System.out.println("NPE:"+npe.getMessage());
							}
							try {
								nnStrs = requireNonEmpty(strs1);
							} catch (NullPointerException npe) {
								System.out.println("npe:"+npe.getMessage());
							}
							try {
								nnStrs = requireNonEmpty(strs2, "Shouldn't!");
							} catch (NullPointerException npe) {
								System.out.println("npe"+npe.getMessage());
							} catch (IllegalArgumentException iae) {
								System.out.println(iae.getMessage());
							}
						}
						public static void main(String... args) {
							test(Collections.singletonList("good"), null, Collections.emptyList());
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"npe:null\n" +
			"Shouldn\'t!");
	}

	public void testIsNull() {
		Map<String, String> compilerOptions = getCompilerOptions();
		compilerOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						@SuppressWarnings("null")
						static <T> @NonNull T hide(T some) {
							return some;
						}
						static void test(@NonNull X x1, @NonNull X x2, @NonNull X x3) {
							if (isNull(x1))
								System.out.println("IS NULL");
							if (isAnyNull(x2, x1))
								System.out.println("IS ANY NULL 1");
							if (isAnyNull(x2, x3))
								System.out.println("IS ANY NULL 2");
						}
						public static void main(String... args) {
							test(hide(null), new X(), new X());
						}
					}
					"""
			},
			compilerOptions,
			"",
			"IS NULL\n" +
			"IS ANY NULL 1");
	}

	public void testAsNullable() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						static void test(Optional<X> xopt) {
							if (xopt != null) {
								X x = asNullable(xopt);
								if (x == null)
									System.out.println("NULL");
							}
						}
						public static void main(String... args) {
							test(Optional.ofNullable(null));
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"NULL");
	}

	public void testNonNullElse() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.function.*;
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						static void test(String str, String noStr, @NonNull Supplier<@NonNull String> prov) {
							System.out.println(nonNullElse(str, "ELSE1"));
							System.out.println(nonNullElse(noStr, "ELSE2"));
							System.out.println(nonNullElseGet(str, () -> "ELSE3"));
							System.out.println(nonNullElseGet(noStr, prov));
						}
						public static void main(String... args) {
							test("good", null, () -> "ELSE4");
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"""
				good
				ELSE2
				good
				ELSE4""");
	}

	public void _testIfNonNull() { // FIXME: see https://bugs.eclipse.org/489609 - [1.8][null] null annotation on wildcard is dropped during inference
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import static org.eclipse.jdt.annotation.Checks.*;
					public class X {
						static void test(@Nullable String str) {
							ifNonNull(str, s -> print(s));
						}
						static void print(@NonNull String s) {
							System.out.print(s);
						}
						public static void main(String... args) {
							test("good");
						}
					}
					"""
			},
			getCompilerOptions(),
			"",
			"good");
	}

	public void testBooleanNullAssertions() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.Objects;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"	public void demonstrateNotWorkingNullCheck() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        if (Object.class.isInstance(mayBeNull)) {\n" +
				"            mayBeNull.toString();\n" +
				"        }\n" +
				"    }\n" +
				"	public void negatedNullCheck() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        if (!Objects.nonNull(mayBeNull)) {\n" +
				"            System.out.println(\"not\");\n" +
				"        } else {\n" +
				"            mayBeNull.toString();\n" +
				"        }\n" +
				"        if (!(Integer.class.isInstance(mayBeNull) || Long.class.isInstance(mayBeNull))) {\n" +
				"            mayBeNull.toString(); // still only a potential problem\n" +
				"        }\n" +
				"    }\n" +
				"	public void nullCheckAlgegra() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        if (Math.random() > 0.5 && Object.class.isInstance(mayBeNull)) {\n" +
				"            mayBeNull.toString();\n" + // both operands are true
				"        }\n" +
				"        if (!Object.class.isInstance(mayBeNull) || Math.random() > 0.5) {\n" +
				"            System.out.println(\"not\");\n" +
				"        } else {\n" +
				"            mayBeNull.toString();\n" + // both operands are false
				"        }\n" +
				"        if (Object.class.isInstance(mayBeNull) && mayBeNull.equals(\"hi\"))\n" + // second evaluated only when first is true
				"            System.out.println(\"equal\");\n" +
				"        if (Objects.isNull(mayBeNull) || mayBeNull.equals(\"hi\"))\n" + // second evaluated only when first is false
				"            System.out.println(\"equal or null\");\n" +
				"    }\n" +
				"	public void objectsUtils() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        String s = Objects.nonNull(mayBeNull) ? mayBeNull.toString(): null;\n" +
				"        if (Objects.isNull(mayBeNull) || Math.random() > 0.5) {\n" +
				"            System.out.println(\"not\");\n" +
				"        } else {\n" +
				"            mayBeNull.toString();\n" +
				"        }\n" +
				"    }\n" +
				"	public void loops() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        for (; Objects.nonNull(mayBeNull); mayBeNull=next(mayBeNull)) {\n" +
				"            mayBeNull.toString();\n" + // guarded by the condition
				"        }\n" +
				"        mayBeNull.toString(); // can only be null after the loop\n" +
				"        Object initiallyNN = new Object();\n" +
				"        while (Objects.nonNull(initiallyNN)) {\n" +
				"            initiallyNN.toString();\n" + // guarded by the condition
				"            initiallyNN = next(initiallyNN);\n" +
				"        }\n" +
				"        initiallyNN.toString(); // can only be null after the loop\n" +
				"    }\n" +
				"    @Nullable Object next(Object o) { return o; }\n" +
				"}\n"
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 24)
					mayBeNull.toString(); // still only a potential problem
					^^^^^^^^^
				Potential null pointer access: The variable mayBeNull may be null at this location
				----------
				2. ERROR in X.java (at line 65)
					mayBeNull.toString(); // can only be null after the loop
					^^^^^^^^^
				Null pointer access: The variable mayBeNull can only be null at this location
				----------
				3. ERROR in X.java (at line 71)
					initiallyNN.toString(); // can only be null after the loop
					^^^^^^^^^^^
				Null pointer access: The variable initiallyNN can only be null at this location
				----------
				""");
	}
	public void testBug465085_comment12() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
		runConformTest(
			new String[] {
				"Snippet.java",
				"""
					import java.util.Collection;
					
					public class Snippet {
						int instanceCount(Collection<?> elements, Class<?> clazz) {
							int count = 0;
							for (Object o : elements) {  // warning here: "The value of the local variable o is not used"
								if (clazz.isInstance(o)) {
									count++;
								}
							}
							return count;
						}
					}
					"""
			},
			options);
	}
}
