/*******************************************************************************
 * Copyright (c) 2017 GK Software AG and others.
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

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJrt;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NullAnnotationBatchCompilerTest extends AbstractBatchCompilerTest {

	protected static final String NONNULL_BY_DEFAULT_ANNOTATION_CONTENT = """
		package org.eclipse.jdt.annotation;
		import static java.lang.annotation.ElementType.*;
		import java.lang.annotation.*;
		@Documented
		@Retention(RetentionPolicy.CLASS)
		@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
		public @interface NonNullByDefault{
		}""";
	protected static final String NULLABLE_ANNOTATION_CONTENT = """
		package org.eclipse.jdt.annotation;
		import static java.lang.annotation.ElementType.*;
		import java.lang.annotation.*;
		@Documented
		@Retention(RetentionPolicy.CLASS)
		@Target({ METHOD, PARAMETER, FIELD })
		public @interface Nullable{
		}
		""";
	protected static final String NONNULL_ANNOTATION_CONTENT = """
		package org.eclipse.jdt.annotation;
		import static java.lang.annotation.ElementType.*;
		import java.lang.annotation.*;
		@Documented
		@Retention(RetentionPolicy.CLASS)
		@Target({ METHOD, PARAMETER, FIELD })
		public @interface NonNull{
		}
		""";
	protected static final String ELEMENT_TYPE_18_CONTENT = "package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n" +
				"";
	protected static final String NONNULL_ANNOTATION_18_CONTENT = """
		package org.eclipse.jdt.annotation;
		import java.lang.annotation.ElementType;
		import java.lang.annotation.*;
		@Documented
		@Retention(RetentionPolicy.CLASS)
		@Target({ ElementType.TYPE_USE })
		public @interface NonNull{
		}
		""";
	protected static final String NULLABLE_ANNOTATION_18_CONTENT = """
		package org.eclipse.jdt.annotation;
		import java.lang.annotation.ElementType;
		import java.lang.annotation.*;
		@Documented
		@Retention(RetentionPolicy.CLASS)
		@Target({ ElementType.TYPE_USE })
		public @interface Nullable{
		}
		""";
	protected static final String NONNULL_BY_DEFAULT_ANNOTATION_18_CONTENT = """
		package org.eclipse.jdt.annotation;
		import java.lang.annotation.ElementType;
		import static org.eclipse.jdt.annotation.DefaultLocation.*;
		
		import java.lang.annotation.Retention;
		import java.lang.annotation.RetentionPolicy;
		import java.lang.annotation.Target;
		@Retention(RetentionPolicy.CLASS)
		@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE })
		public @interface NonNullByDefault {
			DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };
		}
		""";
	protected static final String DEFAULT_LOCATION_CONTENT = """
		package org.eclipse.jdt.annotation;
		public enum DefaultLocation {
			PARAMETER,
			RETURN_TYPE,
			FIELD,
			TYPE_PARAMETER,
			TYPE_BOUND,
			TYPE_ARGUMENT,
			ARRAY_CONTENTS
		}
		""";

	static {
//		TESTS_NAMES = new String[] { "test490010NoEeaFile" };
//		TESTS_NUMBERS = new int[] { 306 };
//		TESTS_RANGE = new int[] { 298, -1 };
	}

	/**
	 * This test suite only needs to be run on one compliance.
	 * As it includes some specific 1.5 tests, it must be used with a least a 1.5 VM
	 * and not be duplicated in general test suite.
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class<?> testClass() {
		return NullAnnotationBatchCompilerTest.class;
	}

	public NullAnnotationBatchCompilerTest(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Util.delete(OUTPUT_DIR);
	}

	/** Call this to discard eea-superimposed JRE classes from the JRT cache. */
	public void clearJrtCache(String releaseVersion) {
		String[] javaClassLibs = Util.getJavaClassLibs();
		if (javaClassLibs.length == 1 && javaClassLibs[0].endsWith("/lib/jrt-fs.jar")) {
			ClasspathJrt.clearCache(javaClassLibs[0], releaseVersion);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
	// -err option - regression tests to check option nullAnnot
	// Null warnings because of annotations, null spec violations plus one specific problem configured as errors
	public void test314_warn_options() {
		this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X {
						  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
							 return this;
						  }
						}
						class Y extends X {
						    @Nullable Object foo(Object o, Object o2) { return null; }
						}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -err:+nullAnnot -warn:-null -err:+nonnullNotRepeated -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
					@Nullable Object foo(Object o, Object o2) { return null; }
					^^^^^^^^^^^^^^^^
				The return type is incompatible with \'@NonNull Object\' returned from X.foo(Object, Object) (mismatching null constraints)
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
					@Nullable Object foo(Object o, Object o2) { return null; }
					                     ^^^^^^
				Missing nullable annotation: inherited method from X specifies this parameter as @Nullable
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
					@Nullable Object foo(Object o, Object o2) { return null; }
					                               ^^^^^^
				Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
				----------
				3 problems (3 errors)
				""",
			true);
	}

	// -warn option - regression tests to check option nullAnnot and missingNullDefault
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
	public void test315_warn_options() {
		this.runConformTest(
			new String[] {
					"p/package-info.java",
					"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
					"package p;\n",
					"p/X.java",
					"""
						package p;
						public class X {
						}
						""",
					"p1/X1.java",
					"""
						package p1;
						public class X1 {
						}
						""",
					"p1/X1a.java",
					"""
						package p1;
						public class X1a {
						}
						""",
					"Default1.java",
					"public class Default1 {\n" +
					"}\n",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -warn:+nullAnnot -warn:+null -missingNullDefault -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
	}

	// -warn option - regression tests to check option nullAnnot and missingNullDefault
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
	public void test315_warn_options_a() {
		this.runConformTest(
			new String[] {
					"p1/X1.java",
					"""
						package p1;
						public class X1 {
						   class Inner{};
						}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p1" + File.separator + "X1.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -warn:+nullAnnot -warn:+null -missingNullDefault -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p1/X1.java (at line 1)
					package p1;
					        ^^
				A default nullness annotation has not been specified for the package p1
				----------
				1 problem (1 warning)
				""",
			true);
	}

	// -warn option - regression tests to check option nullAnnot and missingNullDefault
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
	public void test315_warn_options_b() {
		this.runNegativeTest(
			new String[] {
					"X1.java",
					"""
						public class X1 {
							Zork z;
						}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "X1.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -warn:+nullAnnot -warn:+null -missingNullDefault -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X1.java (at line 1)
					public class X1 {
					             ^^
				A default nullness annotation has not been specified for the type X1
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X1.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				2 problems (1 error, 1 warning)
				""",
			true);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
	// -warn option - regression tests to check option nullAnnot
	// option syntax error
	public void test316_warn_options() {
		this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("unused")
						public class X {}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -warn:+nullAnnot(foo|bar) -warn:+null -nonNullByDefault -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"Token nullAnnot(foo|bar) is not in the expected format \"nullAnnot(<nullable annotation name> | <non null annotation name> | <non-null by default annotation name>)\"\n",
			true);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408815
	// -warn option - regression tests to check option syntacticAnalysis
	// Null warnings because of annotations, null spec violations, suppressed by null-check
	public void test316b_warn_options() {
		this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X {
						  @Nullable Object f;
						  @NonNull Object foo() {
						    if (this.f != null)
						      return this.f;
							 return this;
						  }
						}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -warn:+nullAnnot -warn:+null,syntacticAnalysis -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
	// -warn option - regression tests to check option nullAnnot (no args)
	// Null warnings because of annotations, null spec violations
	public void test313_warn_options() {
		this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X {
						  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
							 return this;
						  }
						}
						class Y extends X {
						    @Nullable Object foo(Object o, Object o2) { return null; }
						}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -warn:+nullAnnot -warn:-null -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
					@Nullable Object foo(Object o, Object o2) { return null; }
					^^^^^^^^^^^^^^^^
				The return type is incompatible with \'@NonNull Object\' returned from X.foo(Object, Object) (mismatching null constraints)
				----------
				2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
					@Nullable Object foo(Object o, Object o2) { return null; }
					                     ^^^^^^
				Missing nullable annotation: inherited method from X specifies this parameter as @Nullable
				----------
				3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
					@Nullable Object foo(Object o, Object o2) { return null; }
					                               ^^^^^^
				Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
				----------
				3 problems (3 warnings)
				""",
			true);
	}

	// Bug 388281 - [compiler][null] inheritance of null annotations as an option
	// -warn option - regression tests to check option inheritNullAnnot
	public void test320_warn_options() {
		this.runNegativeTest(
			new String[] {
					"p/Super.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class Super {
						    void foo(@NonNull String s) {}
						}
						""",
					"p/Sub.java",
					"""
						package p;
						public class Sub extends Super {
						    void foo(String s) {
						        s= null;
						        super.foo(s);
						    }
						}
						""",
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Sub.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -err:+nullAnnot,+null,+inheritNullAnnot -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/Sub.java (at line 4)
					s= null;
					   ^^^^
				Null type mismatch: required '@NonNull String' but the provided value is null
				----------
				1 problem (1 error)
				""",
			true);
	}

	// -warn option - test multiple sets of null annotations
	public void testBug466291() {
		this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						import static java.lang.annotation.ElementType.*;
						import java.lang.annotation.*;
						@NonNullByDefault
						public class X {
						  public Object foo(@Nullable Object o, Object o2) {
							 return new Object();
						  }
						  public Object bar() {
							 return this;
						  }
						}
						@Documented
						@Retention(RetentionPolicy.CLASS)
						@Target({ METHOD, PARAMETER })
						@interface NonNull{
						}
						@Documented
						@Retention(RetentionPolicy.CLASS)
						@Target({ METHOD, PARAMETER })
						@interface Nullable{
						}
						@Documented
						@Retention(RetentionPolicy.CLASS)
						@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
						@interface NonNullByDefault{
						}"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -1.5"
			+ " -warn:+nullAnnot(p.Nullable|p.NonNull|p.NonNullByDefault) -warn:+null -warn:-nullUncheckedConversion "
			+ "-proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);

		// test twice: 1. against SourceTypeBinding(p.X), 2. against BinaryTypeBinding(p.X):
		for (int i=0; i<2; i++) {
			this.runNegativeTest(
					new String[] {
							"p2/X2.java",
							"""
								package p2;
								import org.eclipse.jdt.annotation.*;
								public class X2 {
								  @NonNull Object test(@NonNull p.X nonnullX, @Nullable p.X nullableX) {
								    nonnullX.foo(nullableX, nullableX);
									 return nonnullX.bar();
								  }
								}
								""",
							"org/eclipse/jdt/annotation/NonNull.java",
							NONNULL_ANNOTATION_CONTENT,
							"org/eclipse/jdt/annotation/Nullable.java",
							NULLABLE_ANNOTATION_CONTENT,
							"org/eclipse/jdt/annotation/NonNullByDefault.java",
							NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
					},
					"\"" + OUTPUT_DIR +  File.separator + "p2" + File.separator + "X2.java\""
					+ " -sourcepath \"" + OUTPUT_DIR + "\""
					+ " -classpath \"" + OUTPUT_DIR + "\""
					+ " -1.5"
					+ " -warn:+nullAnnot(org.eclipse.jdt.annotation.Nullable|org.eclipse.jdt.annotation.NonNull|org.eclipse.jdt.annotation.NonNullByDefault)"
					+ " -warn:+nullAnnot(p.Nullable||p.NonNullByDefault) -warn+null -proc:none -d \"" + OUTPUT_DIR + "\"", // nonnull remains unset for secondaries
					"",
					"""
						----------
						1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p2/X2.java (at line 5)
							nonnullX.foo(nullableX, nullableX);
							                        ^^^^^^^^^
						Null type mismatch: required '@NonNull Object' but the provided value is specified as @Nullable
						----------
						1 problem (1 error)
						""",
					false);
			// force reading of BinaryTypeBinding(p.X):
			String xPath = OUTPUT_DIR + File.separator + "p" + File.separator + "X.java";
			new File(xPath).delete();
		}
	}

	//-warn option - test multiple sets of null annotations, three (partial) sets of secondary annotations
	public void testBug466291b() {
		this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						import static java.lang.annotation.ElementType.*;
						import java.lang.annotation.*;
						@NonNullByDefault
						public class X {
						  public Object foo(@Nullable Object o, Object o2) {
							 return new Object();
						  }
						  public Object bar() {
							 return this;
						  }
						}
						@Documented
						@Retention(RetentionPolicy.CLASS)
						@Target({ METHOD, PARAMETER })
						@interface NonNull{
						}
						@Documented
						@Retention(RetentionPolicy.CLASS)
						@Target({ METHOD, PARAMETER })
						@interface Nullable{
						}
						@Documented
						@Retention(RetentionPolicy.CLASS)
						@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
						@interface NonNullByDefault{
						}"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -1.5"
			+ " -warn:+nullAnnot(p.Nullable|p.NonNull|p.NonNullByDefault) -warn:+null -warn:-nullUncheckedConversion "
			+ "-proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);

		// force reading of BinaryTypeBinding(p.X):
		String xPath = OUTPUT_DIR + File.separator + "p" + File.separator + "X.java";
		new File(xPath).delete();

		this.runNegativeTest(
				new String[] {
						"p2/X2.java",
						"""
							package p2;
							import org.eclipse.jdt.annotation.*;
							public class X2 {
							  @NonNull Object test(@NonNull p.X nonnullX, @Nullable p.X nullableX) {
							    nonnullX.foo(nullableX, nullableX);
								 return nonnullX.bar();
							  }
							}
							""",
						"org/eclipse/jdt/annotation/NonNull.java",
						NONNULL_ANNOTATION_CONTENT,
						"org/eclipse/jdt/annotation/Nullable.java",
						NULLABLE_ANNOTATION_CONTENT,
						"org/eclipse/jdt/annotation/NonNullByDefault.java",
						NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
				},
				"\"" + OUTPUT_DIR +  File.separator + "p2" + File.separator + "X2.java\""
				+ " -sourcepath \"" + OUTPUT_DIR + "\""
				+ " -classpath \"" + OUTPUT_DIR + "\""
				+ " -1.5"
				+ " -warn:+nullAnnot(org.eclipse.jdt.annotation.Nullable|org.eclipse.jdt.annotation.NonNull|org.eclipse.jdt.annotation.NonNullByDefault)"
				+ " -warn:+nullAnnot(|x.AbsentNonNull|) "
				+ " -warn:+nullAnnot(p.Nullable||p.NonNullByDefault) "
				+ " -warn:+nullAnnot(yet.AnotherNullable|yet.AnotherNonnull|yet.anotherNNBD) "
				+ " -warn+null -proc:none -d \"" + OUTPUT_DIR + "\"",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p2/X2.java (at line 5)
						nonnullX.foo(nullableX, nullableX);
						                        ^^^^^^^^^
					Null type mismatch: required '@NonNull Object' but the provided value is specified as @Nullable
					----------
					1 problem (1 error)
					""",
				false);
	}

	// == Variants of org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest.testBug375366a(): ==

	// Bug 375366 - ECJ ignores unusedParameterIncludeDocCommentReference unless enableJavadoc option is set
	// property file enables null annotation support
	public void testBug375366c() throws IOException {
		createOutputTestDirectory("regression/.settings");
		Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
				"eclipse.preferences.version=1\n" +
				"org.eclipse.jdt.core.compiler.annotation.nullanalysis=enabled\n");
		this.runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import org.eclipse.jdt.annotation.*;
							public class X {
							  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
								 return this;
							  }
							}
							class Y extends X {
							    @Nullable Object foo(Object o, Object o2) { return null; }
							}
							""",
						"org/eclipse/jdt/annotation/NonNull.java",
						NONNULL_ANNOTATION_CONTENT,
						"org/eclipse/jdt/annotation/Nullable.java",
						NULLABLE_ANNOTATION_CONTENT,
						"org/eclipse/jdt/annotation/NonNullByDefault.java",
						NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
				},
				"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
				+ " -sourcepath \"" + OUTPUT_DIR + "\""
				+ " -1.5"
				+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
				+ " -d \"" + OUTPUT_DIR + "\"",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
						@Nullable Object foo(Object o, Object o2) { return null; }
						^^^^^^^^^^^^^^^^
					The return type is incompatible with \'@NonNull Object\' returned from X.foo(Object, Object) (mismatching null constraints)
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
						@Nullable Object foo(Object o, Object o2) { return null; }
						                     ^^^^^^
					Missing nullable annotation: inherited method from X specifies this parameter as @Nullable
					----------
					3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
						@Nullable Object foo(Object o, Object o2) { return null; }
						                               ^^^^^^
					Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
					----------
					3 problems (2 errors, 1 warning)
					""",
				false/*don't flush*/);
	}

	// Bug 375366 - ECJ ignores unusedParameterIncludeDocCommentReference unless enableJavadoc option is set
	// property file enables null annotation support, one optional warning disabled
	public void testBug375366d() throws IOException {
		createOutputTestDirectory("regression/.settings");
		Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
				"""
					eclipse.preferences.version=1
					org.eclipse.jdt.core.compiler.annotation.nullanalysis=enabled
					org.eclipse.jdt.core.compiler.problem.nonnullParameterAnnotationDropped=ignore
					""");
		this.runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import org.eclipse.jdt.annotation.*;
							public class X {
							  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
								 return this;
							  }
							}
							class Y extends X {
							    @Nullable Object foo(Object o, Object o2) { return null; }
							}
							""",
						"org/eclipse/jdt/annotation/NonNull.java",
						NONNULL_ANNOTATION_CONTENT,
						"org/eclipse/jdt/annotation/Nullable.java",
						NULLABLE_ANNOTATION_CONTENT,
						"org/eclipse/jdt/annotation/NonNullByDefault.java",
						NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
				},
				"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
				+ " -sourcepath \"" + OUTPUT_DIR + "\""
				+ " -1.5"
				+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
				+ " -d \"" + OUTPUT_DIR + "\"",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
						@Nullable Object foo(Object o, Object o2) { return null; }
						^^^^^^^^^^^^^^^^
					The return type is incompatible with \'@NonNull Object\' returned from X.foo(Object, Object) (mismatching null constraints)
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
						@Nullable Object foo(Object o, Object o2) { return null; }
						                     ^^^^^^
					Missing nullable annotation: inherited method from X specifies this parameter as @Nullable
					----------
					2 problems (2 errors)
					""",
				false/*don't flush*/);
	}

	// Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
	// - single external annotation directory
	public void test440477() throws IOException {
		String annots_dir = Util.getOutputDirectory() + File.separator + "annots";
		String annots_java_util = annots_dir + File.separator + "java/util";
		new File(annots_java_util).mkdirs();
		Util.createFile(
				annots_java_util + File.separator + "Map.eea",
				TEST_440687_MAP_EEA_CONTENT);

		String o_e_j_annotation_dir = OUTPUT_DIR + File.separator +
				"org" + File.separator + "eclipse" + File.separator + "jdt" + File.separator + "annotation";
		String j_l_annotation_dir = OUTPUT_DIR +  File.separator +
				"java" + File.separator + "lang" + File.separator + "annotation";
		this.runConformTest(
			new String[] {
				"java/lang/annotation/ElementType.java",
				ELEMENT_TYPE_18_CONTENT,
				"org/eclipse/jdt/annotation/NonNull.java",
				NONNULL_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/Nullable.java",
				NULLABLE_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/DefaultLocation.java",
				DEFAULT_LOCATION_CONTENT,
				"org/eclipse/jdt/annotation/NonNullByDefault.java",
				NONNULL_BY_DEFAULT_ANNOTATION_18_CONTENT,
				"test1/Test1.java",
				"""
					package test1;
					
					import java.util.Map;
					import org.eclipse.jdt.annotation.*;
					
					@NonNullByDefault
					public class Test1 {
						void test(Map<String,Test1> map, String key) {
							Test1 v = map.get(key);
							if (v == null)
								throw new RuntimeException(); // should not be reported as dead code, although V is a '@NonNull Test1'
						}
					}
					"""
				},
				" -1.8 -proc:none -d none -warn:+nullAnnot -annotationpath " + annots_dir +
				" -sourcepath \"" + OUTPUT_DIR + "\" " +
				// explicitly mention all files to ensure a good order, cannot pull in source of NNBD on demand
				"\"" + j_l_annotation_dir   +  File.separator + "ElementType.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNull.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "DefaultLocation.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNullByDefault.java\" " +
				"\"" + OUTPUT_DIR +  File.separator + "test1" + File.separator + "Test1.java\"",
				"",
				"",
				true);
	}
	// file content for tests below:
	private static final String TEST_440687_MAP_EEA_CONTENT =
				"""
		class java/util/Map
		 <K:V:>
		
		get
		 (Ljava/lang/Object;)TV;
		 (Ljava/lang/Object;)T0V;
		put
		 (TK;TV;)TV;
		 (TK;TV;)T0V;
		remove
		 (Ljava/lang/Object;)TV;
		 (Ljava/lang/Object;)T0V;
		""";
	private static final String TEST_440687_OBJECT_EEA_CONTENT =
				"""
		class java/lang/Object
		
		equals
		 (Ljava/lang/Object;)Z
		 (L0java/lang/Object;)Z
		""";
	// Bug 440687 - [compiler][batch][null] improve command line option for external annotations
	// work horse for tests below
	void runTest440687(String compilerPathArgs, String extraSourcePaths, String expectedCompilerMessage, boolean isSuccess) {

		String[] testFiles = new String[] {
					"java/lang/annotation/ElementType.java",
					ELEMENT_TYPE_18_CONTENT,
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_18_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_18_CONTENT,
					"org/eclipse/jdt/annotation/DefaultLocation.java",
					DEFAULT_LOCATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_18_CONTENT,
					"test1/Test1.java",
					"""
						package test1;
						
						import java.util.Map;
						import org.eclipse.jdt.annotation.*;
						
						@NonNullByDefault
						public class Test1 {
							void test(Map<String,Test1> map, String key) {
								Test1 v = map.get(key);
								if (v == null)
									throw new RuntimeException(); // should not be reported as dead code, although V is a '@NonNull Test1'
							}
							public boolean equals(@NonNull Object other) { return false; }
						}
						"""
				};

		String o_e_j_annotation_dir = OUTPUT_DIR + File.separator +
				"org" + File.separator + "eclipse" + File.separator + "jdt" + File.separator + "annotation";
		String j_l_annotation_dir = OUTPUT_DIR +  File.separator +
				"java" + File.separator + "lang" + File.separator + "annotation";

		String commandLine = " -1.8 -proc:none -d none -warn:+nullAnnot " + compilerPathArgs +
				" -sourcepath \"" + OUTPUT_DIR + extraSourcePaths + "\" " +
				// explicitly mention all files to ensure a good order, cannot pull in source of NNBD on demand
				"\"" + j_l_annotation_dir   +  File.separator + "ElementType.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNull.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "DefaultLocation.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNullByDefault.java\" " +
				"\"" + OUTPUT_DIR +  File.separator + "test1" + File.separator + "Test1.java\"";

		if (expectedCompilerMessage == null)
			expectedCompilerMessage =
					"""
						----------
						1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
							public boolean equals(@NonNull Object other) { return false; }
							                      ^^^^^^^^^^^^^^^
						The nullness annotation is redundant with a default that applies to this location
						----------
						2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
							public boolean equals(@NonNull Object other) { return false; }
							                      ^^^^^^^^^^^^^^^
						Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable
						----------
						2 problems (2 warnings)
						""";
		try {
			if (isSuccess)
				this.runConformTest(testFiles, commandLine, "", expectedCompilerMessage, true);
			else
				this.runNegativeTest(testFiles, commandLine, "", expectedCompilerMessage, true);
		} finally {
			Util.delete(Util.getOutputDirectory());
		}
	}
	// Bug 440687 - [compiler][batch][null] improve command line option for external annotations
	// - two external annotation directories as part of the sourcepath/classpath
	public void test440687a() throws IOException {

		String annots_dir1 = Util.getOutputDirectory() + File.separator + "annots1";
		String annots_java_util = annots_dir1 + File.separator + "java/util";
		new File(annots_java_util).mkdirs();
		Util.createFile(annots_java_util + File.separator + "Map.eea",
				TEST_440687_MAP_EEA_CONTENT);

		String annots_dir2 = Util.getOutputDirectory() + File.separator + "annots2";
		String annots_java_lang = annots_dir2 + File.separator + "java/lang";
		new File(annots_java_lang).mkdirs();
		Util.createFile(annots_java_lang + File.separator + "Object.eea",
				TEST_440687_OBJECT_EEA_CONTENT);

		runTest440687("-annotationpath CLASSPATH -classpath \"" + annots_dir2 + "\"",
				File.pathSeparator + annots_dir1, // extra source path
				null, // expect normal error
				true);
	}
	// Bug 440687 - [compiler][batch][null] improve command line option for external annotations
	// - two external annotation directories specifically configured.
	public void test440687b() throws IOException {

		String annots_dir = Util.getOutputDirectory() + File.separator + "annots1";
		String annots_java_util = annots_dir + File.separator + "java/util";
		new File(annots_java_util).mkdirs();
		Util.createFile(
				annots_java_util + File.separator + "Map.eea",
				TEST_440687_MAP_EEA_CONTENT);

		String annots_dir2 = Util.getOutputDirectory() + File.separator + "annots2";
		String annots_java_lang = annots_dir2 + File.separator + "java/lang";
		new File(annots_java_lang).mkdirs();
		Util.createFile(
				annots_java_lang + File.separator + "Object.eea",
				TEST_440687_OBJECT_EEA_CONTENT);

		runTest440687("-annotationpath \"" + annots_dir + File.pathSeparator + annots_dir2 + "\" ",
					"", // no extra source path
					null, // expect normal error
					true);
	}
	// Bug 440687 - [compiler][batch][null] improve command line option for external annotations
	// - single external annotation zip with 2 entries
	public void test440687c() throws IOException {

		String annots_dir = Util.getOutputDirectory() + File.separator + "annots";
		new File(annots_dir).mkdirs();
		String annotsZipFile = annots_dir+ File.separator + "jre-annots.zip";
		Util.createSourceZip(
			new String[] {
				"java/util/Map.eea",
				TEST_440687_MAP_EEA_CONTENT,
				"java/lang/Object.eea",
				TEST_440687_OBJECT_EEA_CONTENT
			},
			annotsZipFile);

		runTest440687("-annotationpath CLASSPATH -classpath \"" + annotsZipFile + "\"",
						"", // no extra source path
						null, // expect normal error
						true);
	}
	// Bug 440687 - [compiler][batch][null] improve command line option for external annotations
	// - missing argument after -annotationpath
	public void test440687d() throws IOException {
		runTest440687("-annotationpath", // missing argument
						"",
						"Missing argument to -annotationpath at \'-sourcepath\'\n",
						false);
	}

	// project is configured for eea (directory on classpath), but no specific file for Map found
	public void test490010NoEeaFile1() throws IOException {

		String annots_dir1 = Util.getOutputDirectory() + File.separator + "annots1";
		new File(annots_dir1).mkdirs();

		String annots_dir2 = Util.getOutputDirectory() + File.separator + "annots2";
		String annots_java_lang = annots_dir2 + File.separator + "java/lang";
		new File(annots_java_lang).mkdirs();
		Util.createFile(annots_java_lang + File.separator + "Object.eea",
				TEST_440687_OBJECT_EEA_CONTENT);

		runTest440687("-annotationpath CLASSPATH -classpath \"" + annots_dir2 + "\"",
				File.pathSeparator + annots_dir1, // extra source path
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
						Test1 v = map.get(key);
						          ^^^^^^^^^^^^
					Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind
					----------
					2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 11)
						throw new RuntimeException(); // should not be reported as dead code, although V is a \'@NonNull Test1\'
						^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Dead code
					----------
					3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
						public boolean equals(@NonNull Object other) { return false; }
						                      ^^^^^^^^^^^^^^^
					The nullness annotation is redundant with a default that applies to this location
					----------
					4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
						public boolean equals(@NonNull Object other) { return false; }
						                      ^^^^^^^^^^^^^^^
					Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable
					----------
					4 problems (4 warnings)
					""",
				true);
	}

	// project is configured for eea (jar on classpath), but no specific file for Map found
	public void test490010NoEeaFile2() throws IOException {

		String annots_dir1 = Util.getOutputDirectory() + File.separator + "annots1";
		new File(annots_dir1).mkdirs();

		String annots_dir2 = Util.getOutputDirectory() + File.separator + "annots2";
		String annots_java_lang = annots_dir2 + File.separator + "java/lang";
		new File(annots_java_lang).mkdirs();
		Util.createFile(annots_java_lang + File.separator + "Object.eea",
				TEST_440687_OBJECT_EEA_CONTENT);
		String zipName = Util.getOutputDirectory() + File.separator + "annots2.zip";
		Util.zip(new File(annots_dir2), zipName);
		Util.delete(annots_dir2);

		runTest440687("-annotationpath CLASSPATH -classpath \"" + zipName + "\"",
				File.pathSeparator + annots_dir1, // extra source path
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
						Test1 v = map.get(key);
						          ^^^^^^^^^^^^
					Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind
					----------
					2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 11)
						throw new RuntimeException(); // should not be reported as dead code, although V is a \'@NonNull Test1\'
						^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Dead code
					----------
					3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
						public boolean equals(@NonNull Object other) { return false; }
						                      ^^^^^^^^^^^^^^^
					The nullness annotation is redundant with a default that applies to this location
					----------
					4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
						public boolean equals(@NonNull Object other) { return false; }
						                      ^^^^^^^^^^^^^^^
					Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable
					----------
					4 problems (4 warnings)
					""",
				true);
	}

	// project is configured for eea (dedicated annotation zip), but no specific file for Map found
	public void test490010NoEeaFile3() throws IOException {

		String annots_dir1 = Util.getOutputDirectory() + File.separator + "annots1";
		new File(annots_dir1).mkdirs();

		String annots_dir2 = Util.getOutputDirectory() + File.separator + "annots2";
		String annots_java_lang = annots_dir2 + File.separator + "java/lang";
		new File(annots_java_lang).mkdirs();
		Util.createFile(annots_java_lang + File.separator + "Object.eea",
				TEST_440687_OBJECT_EEA_CONTENT);
		String zipName = Util.getOutputDirectory() + File.separator + "annots2.zip";
		Util.zip(new File(annots_dir2), zipName);
		Util.delete(annots_dir2);

		runTest440687("-annotationpath \"" + zipName + "\"",
				File.pathSeparator + annots_dir1, // extra source path
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
						Test1 v = map.get(key);
						          ^^^^^^^^^^^^
					Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Map<@NonNull String,@NonNull Test1>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind
					----------
					2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 11)
						throw new RuntimeException(); // should not be reported as dead code, although V is a \'@NonNull Test1\'
						^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Dead code
					----------
					3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
						public boolean equals(@NonNull Object other) { return false; }
						                      ^^^^^^^^^^^^^^^
					The nullness annotation is redundant with a default that applies to this location
					----------
					4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 13)
						public boolean equals(@NonNull Object other) { return false; }
						                      ^^^^^^^^^^^^^^^
					Illegal redefinition of parameter other, inherited method from Object declares this parameter as @Nullable
					----------
					4 problems (4 warnings)
					""",
				true);
	}
	public void testBug571055_explicit() throws IOException {
		runTestBug571055(false, false);
	}
	public void testBug571055_inherit() throws IOException {
		runTestBug571055(true, false);
	}
	public void testBug571055_dedicatedAnnotationPath() throws IOException {
		runTestBug571055(false, true);
	}
	private void runTestBug571055(boolean inheritAnnotations, boolean dedicatedAnnotationPath) throws IOException {
		String annots_dir = Util.getOutputDirectory() + File.separator + "annots";
		String annots_api = annots_dir + File.separator + "api";
		new File(annots_api).mkdirs();
		Util.createFile(
				annots_api + File.separator + "Foo.eea",
				"""
					class api/Foo
					m
					 (Ljava/lang/String;)Ljava/lang/String;
					 (L1java/lang/String;)L0java/lang/String;
					""");
		if (!inheritAnnotations) {
			// 'manually' establish consistency:
			String annots_impl = annots_dir + File.separator + "impl";
			new File(annots_impl).mkdirs();
			Util.createFile(
					annots_impl + File.separator + "FooImpl.eea",
					"""
						class impl/FooImpl
						m
						 (Ljava/lang/String;)Ljava/lang/String;
						 (L1java/lang/String;)L0java/lang/String;
						""");
		}

		String[] testFiles = new String[] {
				"java/lang/annotation/ElementType.java",
				ELEMENT_TYPE_18_CONTENT,
				"org/eclipse/jdt/annotation/NonNull.java",
				NONNULL_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/Nullable.java",
				NULLABLE_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/DefaultLocation.java",
				DEFAULT_LOCATION_CONTENT,
				"api/Foo.java",
				"""
					package api;
					public interface Foo {
						String m(String a);
					}
					""",
				"impl/FooImpl.java",
				"""
					package impl;
					import api.Foo;
					public class FooImpl implements Foo {
						public String m(String a) { return null; }
					}
					""",
				"test1/Test1.java",
				"""
					package test1;
					
					import api.Foo;
					
					public class Test1 {
						void test(Foo api) {
							String result = api.m(null);
							System.out.println(result.toUpperCase());
						}
					}
					"""
			};

		String commandLine;
		if (dedicatedAnnotationPath) {
			commandLine = "-annotationpath \"" + annots_dir + "\" " +
				" -1.8 -proc:none  -err:+nullAnnot -warn:+null " +
				(inheritAnnotations ? " -warn:+inheritNullAnnot ": "") +
				" \"" + OUTPUT_DIR + "\"";
		} else {
			commandLine = "-annotationpath CLASSPATH " +
				" -1.8 -proc:none  -err:+nullAnnot -warn:+null " +
				(inheritAnnotations ? " -warn:+inheritNullAnnot ": "") +
				" -classpath \"" + annots_dir + "\" " +
				" \"" + OUTPUT_DIR + "\"";
		}

		// expect eea-motivated problems in Test1 but no in FooImpl:
		String expectedCompilerMessage =
				"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 7)
				String result = api.m(null);
				                      ^^^^
			Null type mismatch: required '@NonNull String' but the provided value is null
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 8)
				System.out.println(result.toUpperCase());
				                   ^^^^^^
			Potential null pointer access: The variable result may be null at this location
			----------
			2 problems (1 error, 1 warning)
			""";
		this.runNegativeTest(testFiles, commandLine, "", expectedCompilerMessage, false);
	}

	public void testGHTycho1641() throws IOException {
		try {
			// tests external annotations with --release option
			String annotDir = OUTPUT_DIR + File.separator + "eea";
			String annotJavaUtilDir = annotDir + "/java/util".replace('/', File.separatorChar);
			new File(annotJavaUtilDir).mkdirs();
			Util.createFile(annotJavaUtilDir + File.separatorChar + "Objects.eea",
					"""
						class java/util/Objects
						requireNonNull
						 <T:Ljava/lang/Object;>(TT;)TT;
						 <T:Ljava/lang/Object;>(TT;)T1T;
						""");
			runConformTest(
				new String[] {
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_18_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_18_CONTENT,
					"collectiontest/TestClass.java",
					"""
						package collectiontest;
						import java.util.Objects;
						import org.eclipse.jdt.annotation.Nullable;
						public class TestClass {
						
						    @Nullable String test;
						
						    public void concat(String suffix) {
						        test = Objects.requireNonNull(test).concat(suffix);
						    }
						}
						"""
				},
				"--release 11 "+
				" -sourcepath \"" + OUTPUT_DIR + "\"" +
				" -annotationpath \""+annotDir+ "\"" +
				" -err:+nullAnnot -err:+null -proc:none -d \"" + OUTPUT_DIR + "\"" +
				" \"" + OUTPUT_DIR +  File.separator + "collectiontest" + File.separator + "TestClass.java\"",
				"",
				"",
				false);
		} finally {
			clearJrtCache("11");
		}
	}

	public void testGH703() {
		// replicates NullTypeAnnotationTest.testBug456584() but with --release option
		runConformTest(
			new String[] {
				"p/Test.java",
				"""
					package p;
					import java.util.*;
					import java.util.function.*;
					import org.eclipse.jdt.annotation.*;
					
					@NonNullByDefault
					public class Test {
					
					  public static final <T,R> @NonNull R applyRequired(final T input, final Function<? super T,? extends R> function) { // Warning on '@NonNull R': "The nullness annotation is redundant with a default that applies to this location"
					    return Objects.requireNonNull(function.apply(input));
					  }
					
					}
					""",
				"org/eclipse/jdt/annotation/NonNull.java",
				NONNULL_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/Nullable.java",
				NULLABLE_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/NonNullByDefault.java",
				NONNULL_BY_DEFAULT_ANNOTATION_CONTENT
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Test.java\"" +
					" --release " + CompilerOptions.VERSION_9 + " " +
					" -sourcepath \"" + OUTPUT_DIR + "\"" +
					" -warn:+nullAnnot -warn:+null ",
					"",
					"",
					true);
	}
	public void testGH1452_src() throws IOException {
		String annotationPath = "/annotations";
		new File(OUTPUT_DIR+annotationPath+"/some/sillyPackage").mkdirs();
		Util.createFile(OUTPUT_DIR+annotationPath+"/some/sillyPackage/Foo.eea",
				"""
					class some/sillyPackage/Foo
					get
					 (Ljava/lang/String;)Ljava/lang/String;
					 (L1java/lang/String;)L0java/lang/String;
					""");

		String[] testFiles = new String[] {
				"java/lang/annotation/ElementType.java",
				ELEMENT_TYPE_18_CONTENT,
				"org/eclipse/jdt/annotation/NonNull.java",
				NONNULL_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/Nullable.java",
				NULLABLE_ANNOTATION_18_CONTENT,
				"org/eclipse/jdt/annotation/DefaultLocation.java",
				DEFAULT_LOCATION_CONTENT,
				"org/eclipse/jdt/annotation/NonNullByDefault.java",
				NONNULL_BY_DEFAULT_ANNOTATION_18_CONTENT,
				"sillyPackage/Foo.java",
				"""
					package some.sillyPackage;
					public class Foo {
						public String get(String s) { return null; }
					}
					""",
				"test1/Test1.java",
				"""
					package test1;
					
					import some.sillyPackage.Foo;
					import org.eclipse.jdt.annotation.*;
					
					@NonNullByDefault
					public class Test1 {
						void test(Foo f) {
							System.out.print(f.get(null).toUpperCase());
						}
					}
					"""
			};

		String o_e_j_annotation_dir = OUTPUT_DIR + File.separator +
				"org" + File.separator + "eclipse" + File.separator + "jdt" + File.separator + "annotation";
		String j_l_annotation_dir = OUTPUT_DIR +  File.separator +
				"java" + File.separator + "lang" + File.separator + "annotation";

		String commandLine = " -1.8 -proc:none -d none -err:+nullAnnot,null -annotationpath CLASSPATH " +
				" -classpath \"" + OUTPUT_DIR + annotationPath +"\" " +
				// explicitly mention all files to ensure a good order, cannot pull in source of NNBD on demand
				"\"" + j_l_annotation_dir   +  File.separator + "ElementType.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNull.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "Nullable.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "DefaultLocation.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNullByDefault.java\" " +
				"\"" + OUTPUT_DIR +  File.separator + "sillyPackage" + File.separator + "Foo.java\" " +
				"\"" + OUTPUT_DIR +  File.separator + "test1" + File.separator + "Test1.java\"";

		String expectedCompilerMessage =
				"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
				System.out.print(f.get(null).toUpperCase());
				                 ^^^^^^^^^^^
			Potential null pointer access: The method get(String) may return null
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
				System.out.print(f.get(null).toUpperCase());
				                       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			2 problems (2 errors)
			""";
		try {
			this.runNegativeTest(testFiles, commandLine, "", expectedCompilerMessage, false);
		} finally {
			Util.delete(Util.getOutputDirectory());
		}
	}
	public void testGH1452_bin() throws IOException {
		String jarPath = OUTPUT_DIR+"/lib.jar";
		String annotationZip = OUTPUT_DIR+"/annotations.zip";
		Util.createJar(new String[] {
				"some/sillyPackage/Foo.java",
				"""
					package some.sillyPackage;
					public class Foo {
						public String get(String s) { return null; }
					}
					"""
			},
			jarPath,
			"1.8");
		Util.createSourceZip(
				new String[] {
					"some/sillyPackage/Foo.eea",
					"""
						class some/sillyPackage/Foo
						get
						 (Ljava/lang/String;)Ljava/lang/String;
						 (L1java/lang/String;)L0java/lang/String;
						"""
				},
				annotationZip);

		String[] testFiles = new String[] {
					"java/lang/annotation/ElementType.java",
					ELEMENT_TYPE_18_CONTENT,
					"org/eclipse/jdt/annotation/NonNull.java",
					NONNULL_ANNOTATION_18_CONTENT,
					"org/eclipse/jdt/annotation/Nullable.java",
					NULLABLE_ANNOTATION_18_CONTENT,
					"org/eclipse/jdt/annotation/DefaultLocation.java",
					DEFAULT_LOCATION_CONTENT,
					"org/eclipse/jdt/annotation/NonNullByDefault.java",
					NONNULL_BY_DEFAULT_ANNOTATION_18_CONTENT,
					"test1/Test1.java",
					"""
						package test1;
						
						import some.sillyPackage.Foo;
						import org.eclipse.jdt.annotation.*;
						
						@NonNullByDefault
						public class Test1 {
							void test(Foo f) {
								System.out.print(f.get(null).toUpperCase());
							}
						}
						"""
				};

		String o_e_j_annotation_dir = OUTPUT_DIR + File.separator +
				"org" + File.separator + "eclipse" + File.separator + "jdt" + File.separator + "annotation";
		String j_l_annotation_dir = OUTPUT_DIR +  File.separator +
				"java" + File.separator + "lang" + File.separator + "annotation";

		String commandLine = " -1.8 -proc:none -d none -err:+nullAnnot,null  -annotationpath CLASSPATH " +
				" -classpath \"" + annotationZip +"\""+ File.pathSeparator + "\"" + jarPath + "\" " +
				// explicitly mention all files to ensure a good order, cannot pull in source of NNBD on demand
				"\"" + j_l_annotation_dir   +  File.separator + "ElementType.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNull.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "Nullable.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "DefaultLocation.java\" " +
				"\"" + o_e_j_annotation_dir +  File.separator + "NonNullByDefault.java\" " +
				"\"" + OUTPUT_DIR +  File.separator + "test1" + File.separator + "Test1.java\"";

		String expectedCompilerMessage =
				"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
				System.out.print(f.get(null).toUpperCase());
				                 ^^^^^^^^^^^
			Potential null pointer access: The method get(String) may return null
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/test1/Test1.java (at line 9)
				System.out.print(f.get(null).toUpperCase());
				                       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			2 problems (2 errors)
			""";
		try {
			this.runNegativeTest(testFiles, commandLine, "", expectedCompilerMessage, false);
		} finally {
			Util.delete(Util.getOutputDirectory());
		}
	}
}
