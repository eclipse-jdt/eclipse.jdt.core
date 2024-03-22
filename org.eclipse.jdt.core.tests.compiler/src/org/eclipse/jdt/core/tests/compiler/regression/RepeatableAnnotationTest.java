/*******************************************************************************
 * Copyright (c) 2013, 2020 Jesper S Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper S Moller - initial API and implementation
 *     					Bug 412151 - [1.8][compiler] Check repeating annotation's collection type
 *     					Bug 412149 - [1.8][compiler] Emit repeated annotations into the designated container
 *     					Bug 419209 - [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
 *		Stephan Herrmann - Contribution for
 *						Bug 419209 - [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

@SuppressWarnings({ "rawtypes" })
public class RepeatableAnnotationTest extends AbstractComparableTest {

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test006" };
//		TESTS_NUMBERS = new int[] { 297 };
//		TESTS_RANGE = new int[] { 294, -1 };
	}
	boolean isJRE14 = false;
	public RepeatableAnnotationTest(String name) {
		super(name);
		String javaVersion = System.getProperty("java.version");
		int index = javaVersion.indexOf('.');
		if (index != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			index = javaVersion.indexOf('-');
			if (index != -1)
				javaVersion = javaVersion.substring(0, index);
		}
		this.isJRE14 = Integer.parseInt(javaVersion) >= 14;
	}
	private String normalizeAnnotationString(String s) {
		if (!this.isJRE14) return s;
		if (s.indexOf("value=") != -1) {
			s = s.replace("value=[", "{");
			s = s.replace("value=", "");
		}
		return s;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class testClass() {
		return RepeatableAnnotationTest.class;
	}

	// check repeated occurrence of non-repeatable annotation
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @Foo @Foo class X {
					}
					
					""",
				"Foo.java",
				"public @interface Foo {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public @Foo @Foo class X {
					       ^^^^
				Duplicate annotation of non-repeatable type @Foo. Only annotation types marked @Repeatable can be used multiple times at one target.
				----------
				2. ERROR in X.java (at line 1)
					public @Foo @Foo class X {
					            ^^^^
				Duplicate annotation of non-repeatable type @Foo. Only annotation types marked @Repeatable can be used multiple times at one target.
				----------
				""");
	}

	public void test002() {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							@Foo @Foo public class X {
							}
							
							""",
								"Foo.java",
								"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
										"}\n",
										"FooContainer.java",
										"""
											public @interface FooContainer {
												Foo[] value();
											}
											"""
				},
				"");
	}

	// check repeated occurrence of annotation where annotation container is not valid for the target
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					@Target({ElementType.METHOD, ElementType.FIELD}) public @interface FooContainer {
						Foo[] value();
					}
					""",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
				"}\n",
				"X.java",
				"""
					@Foo @Foo public class X { /* Problem */
					  @Foo @Foo void okHere() { /* No problem */
					    @Foo @Foo int local = 0; /* Problem! */
					  }
					  @Foo @Foo int alsoFoo = 0; /* No problem */
					  @Foo class Y {} /* No problem since not repeated */
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Foo @Foo public class X { /* Problem */
					^^^^
				The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location
				----------
				2. ERROR in X.java (at line 3)
					@Foo @Foo int local = 0; /* Problem! */
					^^^^
				The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location
				----------
				""");
	}

	// This is the same test as test003, only where the annotation info for Foo is from a class file, not from the compiler
	public void test004() {
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						@Target({ElementType.METHOD, ElementType.FIELD}) public @interface FooContainer {
							Foo[] value();
						}
						""",
					"Foo.java",
					"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
					"}\n"
				},
				"");
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"@Foo @Foo public class X { /* Problem */\n" +
				"}\n"
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Foo @Foo public class X { /* Problem */
					^^^^
				The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location
				----------
				""";
		runner.shouldFlushOutputDirectory = false;
		runner.javacTestOptions = JavacTestOptions.JavacHasABug.JavacBug8044196;
		runner.runNegativeTest();
	}

	// Test that a single, repeatable annotation can exist just fine an occurrence of its container annotation
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}
					@interface FooContainer { Foo[] value(); }
					@Foo @FooContainer({@Foo, @Foo}) public class X { /* Not a problem */ }
					"""
			},
			"");
	}

	// Test that an repeated annotation can't occur together with its container annotation
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}
					@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* A problem */ }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* A problem */ }
					^^^^
				The repeatable annotation @Foo may not be repeated where its container annotation type @FooContainer is also used directly
				----------
				""");
	}

	// Test that an repeated annotation can't occur together with its container annotation, even if it itself is repeatable.
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface FooContainerContainer { FooContainer[] value(); }
					@java.lang.annotation.Repeatable(FooContainerContainer.class) @interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}
					@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }
					^^^^
				The repeatable annotation @Foo may not be repeated where its container annotation type @FooContainer is also used directly
				----------
				""");
	}

	// Test that an repeated annotation can't occur together with its container annotation, even if it itself is repeatable.
	public void test007a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface FooContainerContainer { FooContainer[] value(); }
					@java.lang.annotation.Repeatable(FooContainerContainer.class) @interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}
					@interface Bar {}
					@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }
					^^^^
				The repeatable annotation @Foo may not be repeated where its container annotation type @FooContainer is also used directly
				----------
				2. ERROR in X.java (at line 5)
					@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }
					          ^^^^
				Duplicate annotation of non-repeatable type @Bar. Only annotation types marked @Repeatable can be used multiple times at one target.
				----------
				3. ERROR in X.java (at line 5)
					@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }
					               ^^^^
				Duplicate annotation of non-repeatable type @Bar. Only annotation types marked @Repeatable can be used multiple times at one target.
				----------
				""");
	}

	// Test that repeated annotations should be contiguous (raises a warning if not) -- not yet in BETA_JAVA8
	public void _test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Bar {}
					@interface Baz {}
					@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}
					@interface FooContainer { Foo[] value(); }
					@Foo @Bar @Foo /* just lexical */ @Foo public class X { /* Gives a warning */ }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					@Foo @Bar @Foo /* just lexical */ @Foo public class X { /* Gives a warning */ }
					          ^^^^
				Repeated @Foo annotations are not grouped together
				----------
				""");
	}
	// Test that deprecation of container annotation is reflected in the repeated annotation (disabled until specification clarification is available)
	public void _test009() {
		this.runConformTest(
			new String[] {
				"Y.java",
				"""
					@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo { int value(); }
					@Deprecated @interface FooContainer { Foo[] value(); }
					@Foo(0) class X { /* Gives a warning */ }
					@Foo(1) @Foo(2) public class Y { /* Gives a warning */ }
					"""
			},
			new ASTVisitor() {
				public boolean visit(
						TypeDeclaration typeDeclaration,
						CompilationUnitScope scope) {
						if (new String(typeDeclaration.name).equals("X")) {
							assertFalse("Foo on X should NOT be deprecated!", typeDeclaration.annotations[0].getCompilerAnnotation().getAnnotationType().isDeprecated());
						}
						if (new String(typeDeclaration.name).equals("Y")) {
							assertEquals("Find Foo(1) on Y",  IntConstant.fromValue(1), typeDeclaration.annotations[0].getCompilerAnnotation().getElementValuePairs()[0].value);
							assertTrue("1st Foo on Y should be deprecated!", typeDeclaration.annotations[0].getCompilerAnnotation().getAnnotationType().isDeprecated());
							assertEquals("Find Foo(2) on Y",  IntConstant.fromValue(2), typeDeclaration.annotations[1].getCompilerAnnotation().getElementValuePairs()[0].value);
							assertTrue("2nd Foo on Y should be deprecated!", typeDeclaration.annotations[1].getCompilerAnnotation().getAnnotationType().isDeprecated());
						}
						return true; // do nothing by default, keep traversing
					}
			});
	}
	// Bug 412151: [1.8][compiler] Check repeating annotation's collection type
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test010() {
		this.runNegativeTest(
			new String[] {
			"Foo.java",
			"""
				@interface FooContainer {
				}
				@java.lang.annotation.Repeatable(FooContainer.class)
				@interface Foo {}
				"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 3)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			The container annotation type @FooContainer must declare a member value()
			----------
			""");
	}
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test011() {
		this.runNegativeTest(
			new String[] {
			"Foo.java",
			"""
				@interface FooContainer {
				    int[] value();
				}
				@java.lang.annotation.Repeatable(FooContainer.class)
				@interface Foo {}
				"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 4)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			The value method in the container annotation type @FooContainer must be of type Foo[] but is int[]
			----------
			""");
	}
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					@interface FooContainer {
					    Foo[][] value();
					}
					@java.lang.annotation.Repeatable(FooContainer.class)
					@interface Foo {}
					"""
			},
			"""
				----------
				1. ERROR in Foo.java (at line 2)
					Foo[][] value();
					^^^^^^^
				Invalid type Foo[][] for the annotation attribute FooContainer.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof
				----------
				2. ERROR in Foo.java (at line 4)
					@java.lang.annotation.Repeatable(FooContainer.class)
					                                 ^^^^^^^^^^^^^^^^^^
				The value method in the container annotation type @FooContainer must be of type Foo[] but is Foo[][]
				----------
				"""
		);
	}
	// 412151: Any methods declared by TC other than value() have a default value (JLS 9.6.2).
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					@interface FooContainer {
					    Foo[] value();
					    int hasDefaultValue() default 1337;
					    int doesntHaveDefaultValue();
					}
					@java.lang.annotation.Repeatable(FooContainer.class)
					@interface Foo {}
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 6)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			The container annotation type @FooContainer must declare a default value for the annotation attribute \'doesntHaveDefaultValue\'
			----------
			""");
	}
	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	public void test014() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@Retention(RetentionPolicy.CLASS)
					@interface FooContainer {
					    Foo[] value();
					}
					@java.lang.annotation.Repeatable(FooContainer.class)
					@Retention(RetentionPolicy.CLASS)
					@interface Foo {
					}
					"""
			},
		"");
	}

	//
	public void test015() {
		// These are fine:
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"""
						public @interface FooContainer {
							Foo[] value();
						}
						""",
					"Foo.java",
					"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
					"}\n"
				},
				"");
		// This changes FooContainer without re-checking Foo
		this.runConformTest(
				new String[] {
						"FooContainer.java",
						"""
							public @interface FooContainer {
								int[] value();
							}
							"""
					},
					"",
					null,
					false,
					null);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo @Foo public class X { /* Problem since Foo now uses FooContainer which doesn't work anymore*/\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Foo @Foo public class X { /* Problem since Foo now uses FooContainer which doesn\'t work anymore*/
					^^^^
				The value method in the container annotation type @FooContainer must be of type Foo[] but is int[]
				----------
				""",
			null, false /* don't flush*/);
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Base example, both targets are specified
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@Retention(RetentionPolicy.SOURCE)
					@interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 5)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			Retention \'RUNTIME\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'SOURCE\'
			----------
			""");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on FooContainer
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@Retention(RetentionPolicy.SOURCE)
					@interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class)
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 5)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			Retention \'CLASS\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'SOURCE\'
			----------
			""");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 4)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			Retention \'RUNTIME\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'CLASS\'
			----------
			""");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo - but positive
	public void test019() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@interface FooContainer { Foo[] value(); }
					@java.lang.annotation.Repeatable(FooContainer.class)
					@Retention(RetentionPolicy.SOURCE)
					@interface Foo { }
					"""
			});
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T
	// Only specified on FooContainer, separate compilation
	public void test020() {
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"""
						import java.lang.annotation.Retention;
						import java.lang.annotation.RetentionPolicy;
						@Retention(RetentionPolicy.SOURCE)
						public @interface FooContainer { Foo[] value(); }
						""",
					"Foo.java",
					"""
						import java.lang.annotation.Retention;
						import java.lang.annotation.RetentionPolicy;
						@Retention(RetentionPolicy.SOURCE)
						@java.lang.annotation.Repeatable(FooContainer.class)
						public @interface Foo { }
						"""
				});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"public @interface Foo { } // If omitted, retention is class\n"
			},
		"""
			----------
			1. ERROR in Foo.java (at line 1)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			Retention \'CLASS\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'SOURCE\'
			----------
			""",
		null, false /* don't flush*/);
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo, separate compilation
	public void test021() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					public @interface FooContainer { Foo[] value(); }
					""",
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@java.lang.annotation.Repeatable(FooContainer.class)
					public @interface Foo { }
					"""
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					@java.lang.annotation.Repeatable(FooContainer.class)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 3)
				@java.lang.annotation.Repeatable(FooContainer.class)
				                                 ^^^^^^^^^^^^^^^^^^
			Retention \'RUNTIME\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'CLASS\'
			----------
			""",
		null, false /* don't flush*/);
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's @Targets, if specified, must be a subset or the same as T's @Targets. Simple test
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
					@interface FooContainer { Foo[] value(); }
					""",
				"Foo.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @java.lang.annotation.Repeatable(FooContainer.class)
					@Target({ElementType.FIELD})
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 3)
				public @java.lang.annotation.Repeatable(FooContainer.class)
				                                        ^^^^^^^^^^^^^^^^^^
			The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: TYPE, METHOD
			----------
			""");
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's @Targets, if specified, must be a subset or the same as T's @Targets. Test this as a separate pass, so that
	// FooContainer is loaded from binary.
	public void test023() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @Target({ElementType.METHOD})
					@interface FooContainer { Foo[] value(); }
					""",
				"Foo.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @Target({ElementType.METHOD})
					@interface Foo { }
					"""
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @java.lang.annotation.Repeatable(FooContainer.class)
					@java.lang.annotation.Target({ElementType.FIELD})
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 3)
				public @java.lang.annotation.Repeatable(FooContainer.class)
				                                        ^^^^^^^^^^^^^^^^^^
			The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: METHOD
			----------
			""",
		null, false /* don't flush*/);
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's may target ANNOTATION_TYPE but that should match TYPE for T, since it's a superset
	public void test024() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"""
					import java.lang.annotation.ElementType;
					@java.lang.annotation.Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
					@interface FooContainer { Foo[] value(); }
					""",
				"Foo.java",
				"""
					import java.lang.annotation.ElementType;
					@java.lang.annotation.Repeatable(FooContainer.class)
					@java.lang.annotation.Target({ElementType.METHOD, ElementType.TYPE})
					@interface Foo { }
					"""
			});
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// Test that all ElementTypes can be reported
	public void test025() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
					@interface FooContainer { Foo[] value(); }
					""",
				"Foo.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					public @java.lang.annotation.Repeatable(FooContainer.class)
					@Target({})
					@interface Foo { }
					"""
			},
		"""
			----------
			1. ERROR in Foo.java (at line 3)
				public @java.lang.annotation.Repeatable(FooContainer.class)
				                                        ^^^^^^^^^^^^^^^^^^
			The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE
			----------
			""");
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's has no @Targets (=every declaration location), but @Foo has, then complain.
	public void test026() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@interface Foo { }\n"
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					@java.lang.annotation.Repeatable(FooContainer.class)
					@java.lang.annotation.Target({ElementType.FIELD})
					@interface Foo { }
					"""
			},
			"""
				----------
				1. ERROR in Foo.java (at line 3)
					@java.lang.annotation.Repeatable(FooContainer.class)
					                                 ^^^^^^^^^^^^^^^^^^
				The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: TYPE, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, MODULE, RECORD_COMPONENT
				----------
				""",
		null, false /* don't flush*/);
	}

	// 412151: If T is @Documented, then TC should also be Documented
	public void test027() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Documented @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Documented @interface Foo { }\n"});
	}

	// 412151: If T is @Documented, then TC should also be Documented, OK for TC to be documented while T is not
	public void test028() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Documented @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@interface Foo { }\n"});
	}

	// 412151: If T is @Documented, then TC should also be Documented
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Documented\n" +
				"@interface Foo { }\n"
			},
			"""
				----------
				1. ERROR in Foo.java (at line 1)
					@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Documented
					                                 ^^^^^^^^^^^^^^^^^^
				The repeatable annotation type @Foo is marked @Documented, but its container annotation type @FooContainer is not
				----------
				""");
	}

	// 412151: If T is @Documented, then TC should also be Documented - check from previous compilation
	public void test030() {
		this.runConformTest(
				new String[] {
					"FooContainer.java",
					"@java.lang.annotation.Documented @interface FooContainer { Foo[] value(); }\n",
					"Foo.java",
					"@java.lang.annotation.Documented @interface Foo { }\n"
				});
			this.runConformTest(
				new String[] {
					"Foo.java",
					"public @java.lang.annotation.Documented @java.lang.annotation.Repeatable(FooContainer.class)\n" +
					"@interface Foo { }\n"
				},
				"",
				null,
				false,
				null);
	}

	// 412151: If T is @Inherited, then TC should also be Inherited
	public void test031() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Inherited @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Inherited @interface Foo { }\n"});
	}

	// 412151: If T is @Inherited, then TC should also be Inherited, OK for TC to be inherited while T is not.
	public void test032() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Inherited @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@interface Foo { }\n"});
	}
	// 412151: If T is @Inherited, then TC should also be Inherited
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Inherited\n" +
				"@interface Foo { }\n"
			},
			"""
				----------
				1. ERROR in Foo.java (at line 1)
					@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Inherited
					                                 ^^^^^^^^^^^^^^^^^^
				The repeatable annotation type @Foo is marked @Inherited, but its container annotation type @FooContainer is not
				----------
				""");
	}

	// 412151: If T is @Inherited, then TC should also be Inherited - check from previous compilation
	public void test034() {
		this.runConformTest(
				new String[] {
					"FooContainer.java",
					"@java.lang.annotation.Inherited @interface FooContainer { Foo[] value(); }\n",
					"Foo.java",
					"@java.lang.annotation.Inherited @interface Foo { }\n"
				});
			this.runConformTest(
				new String[] {
					"Foo.java",
					"public @java.lang.annotation.Inherited @java.lang.annotation.Repeatable(FooContainer.class)\n" +
					"@interface Foo { }\n"
				},
				"",
				null,
				false,
				null);
	}
	// 412151: Ensure no double reporting for bad target.
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Target;
					@Target(ElementType.FIELD)
					@interface TC {
						T [] value();
					}
					@Target(ElementType.TYPE)
					@Repeatable(TC.class)
					@interface T {
					}
					@T @T // we used to double report here.
					public class X {\s
						X f;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					@Repeatable(TC.class)
					            ^^^^^^^^
				The container annotation type @TC is allowed at targets where the repeatable annotation type @T is not: FIELD
				----------
				2. ERROR in X.java (at line 12)
					@T @T // we used to double report here.
					^^
				The annotation @T cannot be repeated at this location since its container annotation type @TC is disallowed at this location
				----------
				""");
	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					
					@Retention(RUNTIME)
					@interface AttrContainer {
					  public Attr[] value();
					}
					@Retention(RUNTIME)
					@Repeatable(AttrContainer.class)
					@interface Attr {
					  public int value() default -1;
					}
					
					@Attr(1) @Attr(2)
					public class X {
					  public static void main(String args[]) {
					  	Object e[] = X.class.getAnnotationsByType(Attr.class);
					  	for (int i=0; i<e.length;++i) System.out.print(e[i] + " ");
					  }
					}"""
			},
			normalizeAnnotationString("@Attr(value=1) @Attr(value=2)"));

	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that only repetitions go into the container
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Annotation;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					
					@Retention(RUNTIME)
					@interface AttrContainer {
					  public Attr[] value();
					}
					@Retention(RUNTIME)
					@Repeatable(AttrContainer.class)
					@interface Attr {
					  public int value() default -1;
					}
					
					public class X {
					  @Attr(1) class Y1 {}
					  @Attr(1) @Attr(2) class Y2 {}\s
					  public static void main(String args[]) {
					  	System.out.print("Y1: " + normalizeAnnotation(Y1.class.getAnnotation(Attr.class)) + "\\n");
					  	System.out.print("Y2: " + normalizeAnnotation(Y2.class.getAnnotation(Attr.class)) + "\\n");
					  	System.out.print("Y1: " + normalizeAnnotation(Y1.class.getAnnotation(AttrContainer.class)) + "\\n");
					  	System.out.print("Y2: " + normalizeAnnotation(Y2.class.getAnnotation(AttrContainer.class)) + "\\n");
					  }
					  static String normalizeAnnotation(Annotation a) {
					 		if (a == null) return null;
						  String str = a.toString();
						  str = str.replace("value={@", "value=[@");
						  str = str.replace(")}", ")]");
						  return str;
					  }
					}"""
			},
			normalizeAnnotationString("""
				Y1: @Attr(value=1)
				Y2: null
				Y1: null
				Y2: @AttrContainer(value=[@Attr(value=1), @Attr(value=2)])"""));

	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that the retention from the containing annotation is used
	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					
					@Retention(RUNTIME)
					@interface AttrContainer {
					  public Attr[] value();
					}
					@Retention(SOURCE)
					@Repeatable(AttrContainer.class)
					@interface Attr {
					  public int value() default -1;
					}
					
					public class X {
					  @Attr(1) class Y1 {}
					  @Attr(1) @Attr(2) class Y2 {}\s
					  public static void main(String args[]) {
					  	System.out.println("Y1 has " + Y1.class.getAnnotationsByType(Attr.class).length);
					  	System.out.println("Y2 has " + Y2.class.getAnnotationsByType(Attr.class).length);
					  }
					}"""
			},
			"Y1 has 0\n" +
			"Y2 has 2");

	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that repeated annotations can appear at package targets
	public void test039() throws Exception {
		String[] testFiles = {
				"repeatable/Main.java",
				"""
					package repeatable;
					public class Main {
					    public static void main (String[] argv) {
					    };
					}""",

			"repeatable/FooContainer.java",
			"""
				package repeatable;
				@java.lang.annotation.Target(java.lang.annotation.ElementType.PACKAGE)
				@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
				public @interface FooContainer {
					Foo[] value();
				}
				""",

			"repeatable/Foo.java",
			"""
				package repeatable;
				@java.lang.annotation.Repeatable(FooContainer.class)
				public @interface Foo {}
				""",

			"repeatable/package-info.java",
			"""
				@Foo @Foo
				package repeatable;
				import repeatable.Foo;""",
		};
		runConformTest(testFiles, "");
		String expectedOutout =
				"""
			  RuntimeVisibleAnnotations:\s
			    #8 @repeatable.FooContainer(
			      #9 value=[
			        annotation value =
			            #10 @repeatable.Foo(
			            )
			        annotation value =
			            #10 @repeatable.Foo(
			            )
			        ]
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "repeatable" + File.separator + "package-info.class", "package-info", expectedOutout, ClassFileBytesDisassembler.SYSTEM);
	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that repeated annotations show up on fields, methods, and parameters
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Field;
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					
					@Retention(RUNTIME)
					@interface AttrContainer {
					  public Attr[] value();
					}
					@Retention(RUNTIME)
					@Repeatable(AttrContainer.class)
					@interface Attr {
					  public int value() default -1;
					}
					
					public class X {
					   @Attr(1) @Attr(2) public int field;
					
					   @Attr(3) @Attr(4)
					   public static void main(@Attr(5) @Attr(6) String args[]) throws Exception {
					    Field fieldField = X.class.getField("field");
					    dump(fieldField.getAnnotationsByType(Attr.class));
					    Method mainMethod = X.class.getMethod("main", (new String[0]).getClass());
					    dump(mainMethod.getAnnotationsByType(Attr.class));
					    Parameter argvParameter = mainMethod.getParameters()[0];
					    dump(argvParameter.getAnnotationsByType(Attr.class));
					   }
					   static void dump(Attr[] attrs) {
					    for (int i=0; i<attrs.length;++i) System.out.print(attrs[i] + " ");
					   }
					}"""
			},
			normalizeAnnotationString(
					"@Attr(value=1) @Attr(value=2) @Attr(value=3) @Attr(value=4) @Attr(value=5) @Attr(value=6)"));
	}
	// Test that repeated annotations show up type parameters properly.
	public void testTypeParameters() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Annotation;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import java.lang.reflect.AnnotatedElement;
					import java.lang.reflect.AnnotatedType;
					import java.lang.reflect.Field;
					import java.lang.reflect.Method;
					import java.lang.reflect.Type;
					import java.lang.reflect.TypeVariable;
					
					import static java.lang.annotation.RetentionPolicy.*;
					
					@Retention(RUNTIME)
					@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER,})
					@interface TC {
					  public T[] value();
					}
					@Retention(RUNTIME)
					@Repeatable(TC.class)
					@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD})
					@interface T {
					  public int value() default -1;
					}
					
					interface I<@T(1) @T(2) K extends @T(3) @T(4) Object & java.lang.@T(5) @T(6) Comparable<?>> {
					}
					
					
					public class X {
					  public static void main(String args[]) {
						Class<I> ci = I.class; \s
					  	printAnnotations("I.class", ci);
					  	TypeVariable<Class<I>>[] typeParameters = ci.getTypeParameters();
					  	for (TypeVariable<?> t: typeParameters) {
					  		printAnnotations(t.getName(), t);
					  		AnnotatedType[] bounds = t.getAnnotatedBounds();
					  		for (AnnotatedType bound : bounds) {
					  			printAnnotations(bound.getType().getTypeName(), bound);
					  		}
					  	}
					  }
					 \s
					  static void printAnnotations(String name, AnnotatedElement element) {
						  int [] iterations = { 0, 1 };
						  for (int i : iterations) {
							  Class<? extends Annotation> annotation = i == 0 ? T.class : TC.class;
							  for (int j: iterations) {
								  Annotation [] annotations = j == 0 ? new Annotation [] { element.getAnnotation(annotation) } : element.getAnnotationsByType(annotation);
								  if (annotations.length == 0 || (annotations.length == 1 && annotations[0] == null)) continue;
								  System.out.print(name + (j == 0 ? ".getAnnotation(" : ".getAnnotationByType(") + annotation.getName() + ".class): ");
								  for (Annotation a : annotations) {
									  System.out.print(normalizeAnnotation(a) + " ");
								  }
								  System.out.print("\\n");
							  }
						  }
					  }
					  static String normalizeAnnotation(Annotation a) {
					 		if (a == null) return null;
						  String str = a.toString();
						  str = str.replace("value={@", "value=[@");
						  str = str.replace(")}", ")]");
						  return str;
					  }
					}
					"""

			},
			normalizeAnnotationString("""
				K.getAnnotationByType(T.class): @T(value=1) @T(value=2)\s
				K.getAnnotation(TC.class): @TC(value=[@T(value=1), @T(value=2)])\s
				K.getAnnotationByType(TC.class): @TC(value=[@T(value=1), @T(value=2)])\s
				java.lang.Object.getAnnotationByType(T.class): @T(value=3) @T(value=4)\s
				java.lang.Object.getAnnotation(TC.class): @TC(value=[@T(value=3), @T(value=4)])\s
				java.lang.Object.getAnnotationByType(TC.class): @TC(value=[@T(value=3), @T(value=4)])\s
				java.lang.Comparable<?>.getAnnotationByType(T.class): @T(value=5) @T(value=6)\s
				java.lang.Comparable<?>.getAnnotation(TC.class): @TC(value=[@T(value=5), @T(value=6)])\s
				java.lang.Comparable<?>.getAnnotationByType(TC.class): @TC(value=[@T(value=5), @T(value=6)])"""),
			null,
			true,
			new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	// Test that repeated annotations show up at various sites, both type use and declaration.
	public void testVariousSites() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Annotation;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import java.lang.reflect.AnnotatedArrayType;
					import java.lang.reflect.AnnotatedElement;
					import java.lang.reflect.AnnotatedParameterizedType;
					import java.lang.reflect.AnnotatedType;
					import java.lang.reflect.Constructor;
					import java.lang.reflect.Field;
					import java.lang.reflect.Method;
					import java.lang.reflect.TypeVariable;
					
					import static java.lang.annotation.RetentionPolicy.*;
					
					@Retention(RUNTIME)
					@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD})
					@interface TC {
					  public T[] value();
					}
					@Retention(RUNTIME)
					@Repeatable(TC.class)
					@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD})
					@interface T {
					  public int value() default -1;
					}
					
					interface I {
					}
					
					@T(1) @T(2)
					public class X<@T(3) @T(4) K extends @T(5) @T(6) Object & java.lang.@T(7) @T(8) Comparable<?>, @T(9) @T(10) V> extends @T(11) @T(12) Object implements @T(13) @T(14) I {
					  public @T(15) @T(16) X<@T(17) @T(18) String, @T(19) @T(20) Integer> field;
					  @T(21) @T(22)
					  public <@T(23) @T(24) Q> X @T(25) @T(26) [] method(@T(27) @T(28) X<K, V> this,\s
							                                             @T(29) @T(30) X<@T(31) @T(32) String, String> that) throws @T(33) @T(34) NullPointerException {
						  return null;
					  }
					  @T(35) @T(36)
					  public X() {
						 \s
					  }
					  @T(37) @T(48)
					  public class MemberType {
						 \s
					  }
					 \s
					  public static void main(String args[]) {
						Class<X> xc = X.class; \s
					  	printAnnotations("Class: " + "X.class", xc);
					  	TypeVariable<Class<X>>[] typeParameters = xc.getTypeParameters();
					  	for (TypeVariable<?> t: typeParameters) {
					  		printAnnotations("Type Parameter: " + t.getName(), t);
					  		AnnotatedType[] bounds = t.getAnnotatedBounds();
					  		for (AnnotatedType bound : bounds) {
					  			printAnnotations("Type parameter bound: " + bound.getType().getTypeName(), bound);
					  		}
					  	}
					  	AnnotatedType annotatedSuperclass = xc.getAnnotatedSuperclass();
					  	printAnnotations("Superclass: " + annotatedSuperclass.getType().getTypeName(), annotatedSuperclass);
					  \t
					  	AnnotatedType [] annotatedSuperInterfaces = xc.getAnnotatedInterfaces();
					  	printAnnotations("Superinterface: " + annotatedSuperInterfaces[0].getType().getTypeName(), annotatedSuperInterfaces[0]);
					  \t
					  	for (Field field: xc.getFields()) {
					  		printAnnotations("Field: " + field.getName(), field);
					  		AnnotatedParameterizedType fType = (AnnotatedParameterizedType) field.getAnnotatedType();
					  		for (AnnotatedType typeArgumentType : fType.getAnnotatedActualTypeArguments())
					  			printAnnotations("Field Type argument: " + typeArgumentType.getType().getTypeName(), typeArgumentType);
					  		\t
					  	}
					  	for (Method method: xc.getMethods()) {
					  		switch (method.getName()) {
					  		case "method"  :
					  			printAnnotations(method.getName(), method);
					  			AnnotatedArrayType mType = (AnnotatedArrayType) method.getAnnotatedReturnType();
					  			printAnnotations("Method return type: " + mType.getType().getTypeName(), mType);
					  			AnnotatedType mTypeEtype = mType.getAnnotatedGenericComponentType();
					  			printAnnotations("Method return type, element type: " + mTypeEtype.getType().getTypeName(), mTypeEtype);
					  			TypeVariable<Method>[] typeParameters2 = method.getTypeParameters();
					  		  	for (TypeVariable<?> t: typeParameters2) {
					  		  		printAnnotations("Method Type Parameter: " + t.getName(), t);
					  		  	}
					  		  	AnnotatedType annotatedReceiverType = method.getAnnotatedReceiverType();
					  		  	printAnnotations("Receiver: ", annotatedReceiverType);
					  		  	AnnotatedType[] annotatedParameterTypes = method.getAnnotatedParameterTypes();
					  		  	for (AnnotatedType annotatedParameterType : annotatedParameterTypes) {
					  		  		printAnnotations("Parameter: ", annotatedParameterType);
					  		  	}
					  		  	AnnotatedType[] annotatedExceptionTypes = method.getAnnotatedExceptionTypes();
					  		  	for (AnnotatedType annotatedType : annotatedExceptionTypes) {
									printAnnotations("Exception type: ", annotatedType);
								}
					  			break;
					  		}
					  	}
					  	for (Constructor<?> constructor : xc.getConstructors()) {
					  		printAnnotations("Constructor: ", constructor);
					  	}
					  	// don't know how to get member classes.
					  }
					 \s
					  static void printAnnotations(String name, AnnotatedElement element) {
						  int [] iterations = { 0, 1 };
						  for (int i : iterations) {
							  Class<? extends Annotation> annotation = i == 0 ? T.class : TC.class;
							  for (int j: iterations) {
								  Annotation [] annotations = j == 0 ? new Annotation [] { element.getAnnotation(annotation) } : element.getAnnotationsByType(annotation);
								  if (annotations.length == 0 || (annotations.length == 1 && annotations[0] == null)) continue;
								  System.out.print(name + (j == 0 ? ".getAnnotation(" : ".getAnnotationByType(") + annotation.getName() + ".class): ");
								  for (Annotation a : annotations) {
									  System.out.print(normalizeAnnotation(a) + " ");
								  }
								  System.out.print("\\n");
							  }
						  }
					  }
					  static String normalizeAnnotation(Annotation a) {
					 		if (a == null) return null;
						  String str = a.toString();
						  str = str.replace("value={@", "value=[@");
						  str = str.replace(")}", ")]");
						  return str;
					  }
					}
					"""

			},
			normalizeAnnotationString("""
				Class: X.class.getAnnotationByType(T.class): @T(value=1) @T(value=2)\s
				Class: X.class.getAnnotation(TC.class): @TC(value=[@T(value=1), @T(value=2)])\s
				Class: X.class.getAnnotationByType(TC.class): @TC(value=[@T(value=1), @T(value=2)])\s
				Type Parameter: K.getAnnotationByType(T.class): @T(value=3) @T(value=4)\s
				Type Parameter: K.getAnnotation(TC.class): @TC(value=[@T(value=3), @T(value=4)])\s
				Type Parameter: K.getAnnotationByType(TC.class): @TC(value=[@T(value=3), @T(value=4)])\s
				Type parameter bound: java.lang.Object.getAnnotationByType(T.class): @T(value=5) @T(value=6)\s
				Type parameter bound: java.lang.Object.getAnnotation(TC.class): @TC(value=[@T(value=5), @T(value=6)])\s
				Type parameter bound: java.lang.Object.getAnnotationByType(TC.class): @TC(value=[@T(value=5), @T(value=6)])\s
				Type parameter bound: java.lang.Comparable<?>.getAnnotationByType(T.class): @T(value=7) @T(value=8)\s
				Type parameter bound: java.lang.Comparable<?>.getAnnotation(TC.class): @TC(value=[@T(value=7), @T(value=8)])\s
				Type parameter bound: java.lang.Comparable<?>.getAnnotationByType(TC.class): @TC(value=[@T(value=7), @T(value=8)])\s
				Type Parameter: V.getAnnotationByType(T.class): @T(value=9) @T(value=10)\s
				Type Parameter: V.getAnnotation(TC.class): @TC(value=[@T(value=9), @T(value=10)])\s
				Type Parameter: V.getAnnotationByType(TC.class): @TC(value=[@T(value=9), @T(value=10)])\s
				Superclass: java.lang.Object.getAnnotationByType(T.class): @T(value=11) @T(value=12)\s
				Superclass: java.lang.Object.getAnnotation(TC.class): @TC(value=[@T(value=11), @T(value=12)])\s
				Superclass: java.lang.Object.getAnnotationByType(TC.class): @TC(value=[@T(value=11), @T(value=12)])\s
				Superinterface: I.getAnnotationByType(T.class): @T(value=13) @T(value=14)\s
				Superinterface: I.getAnnotation(TC.class): @TC(value=[@T(value=13), @T(value=14)])\s
				Superinterface: I.getAnnotationByType(TC.class): @TC(value=[@T(value=13), @T(value=14)])\s
				Field: field.getAnnotationByType(T.class): @T(value=15) @T(value=16)\s
				Field: field.getAnnotation(TC.class): @TC(value=[@T(value=15), @T(value=16)])\s
				Field: field.getAnnotationByType(TC.class): @TC(value=[@T(value=15), @T(value=16)])\s
				Field Type argument: java.lang.String.getAnnotationByType(T.class): @T(value=17) @T(value=18)\s
				Field Type argument: java.lang.String.getAnnotation(TC.class): @TC(value=[@T(value=17), @T(value=18)])\s
				Field Type argument: java.lang.String.getAnnotationByType(TC.class): @TC(value=[@T(value=17), @T(value=18)])\s
				Field Type argument: java.lang.Integer.getAnnotationByType(T.class): @T(value=19) @T(value=20)\s
				Field Type argument: java.lang.Integer.getAnnotation(TC.class): @TC(value=[@T(value=19), @T(value=20)])\s
				Field Type argument: java.lang.Integer.getAnnotationByType(TC.class): @TC(value=[@T(value=19), @T(value=20)])\s
				method.getAnnotationByType(T.class): @T(value=21) @T(value=22)\s
				method.getAnnotation(TC.class): @TC(value=[@T(value=21), @T(value=22)])\s
				method.getAnnotationByType(TC.class): @TC(value=[@T(value=21), @T(value=22)])\s
				Method return type: X[].getAnnotationByType(T.class): @T(value=25) @T(value=26)\s
				Method return type: X[].getAnnotation(TC.class): @TC(value=[@T(value=25), @T(value=26)])\s
				Method return type: X[].getAnnotationByType(TC.class): @TC(value=[@T(value=25), @T(value=26)])\s
				Method return type, element type: X.getAnnotationByType(T.class): @T(value=21) @T(value=22)\s
				Method return type, element type: X.getAnnotation(TC.class): @TC(value=[@T(value=21), @T(value=22)])\s
				Method return type, element type: X.getAnnotationByType(TC.class): @TC(value=[@T(value=21), @T(value=22)])\s
				Method Type Parameter: Q.getAnnotationByType(T.class): @T(value=23) @T(value=24)\s
				Method Type Parameter: Q.getAnnotation(TC.class): @TC(value=[@T(value=23), @T(value=24)])\s
				Method Type Parameter: Q.getAnnotationByType(TC.class): @TC(value=[@T(value=23), @T(value=24)])\s
				Receiver: .getAnnotationByType(T.class): @T(value=27) @T(value=28)\s
				Receiver: .getAnnotation(TC.class): @TC(value=[@T(value=27), @T(value=28)])\s
				Receiver: .getAnnotationByType(TC.class): @TC(value=[@T(value=27), @T(value=28)])\s
				Parameter: .getAnnotationByType(T.class): @T(value=29) @T(value=30)\s
				Parameter: .getAnnotation(TC.class): @TC(value=[@T(value=29), @T(value=30)])\s
				Parameter: .getAnnotationByType(TC.class): @TC(value=[@T(value=29), @T(value=30)])\s
				Exception type: .getAnnotationByType(T.class): @T(value=33) @T(value=34)\s
				Exception type: .getAnnotation(TC.class): @TC(value=[@T(value=33), @T(value=34)])\s
				Exception type: .getAnnotationByType(TC.class): @TC(value=[@T(value=33), @T(value=34)])\s
				Constructor: .getAnnotationByType(T.class): @T(value=35) @T(value=36)\s
				Constructor: .getAnnotation(TC.class): @TC(value=[@T(value=35), @T(value=36)])\s
				Constructor: .getAnnotationByType(TC.class): @TC(value=[@T(value=35), @T(value=36)])"""),
			null,
			true,
			new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	// Test that bad container specifications are handled properly.
	public void testBadContainerType() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Repeatable;
					@Repeatable(X.class)
					@interface T {
					  public int value() default -1;
					}
					public class X {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					@Repeatable(X.class)
					            ^^^^^^^
				Type mismatch: cannot convert from Class<X> to Class<? extends Annotation>
				----------
				""");
	}
	// Test unspecified target.
	public void testUnspecifiedTarget() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Target;
					
					@Target(ElementType.TYPE_USE)
					@interface TC {
						T [] value();
					}
					
					@Repeatable(TC.class)
					@interface T {
					}
					
					@T @T
					public class X {\s
						X f;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					@Repeatable(TC.class)
					            ^^^^^^^^
				The container annotation type @TC is allowed at targets where the repeatable annotation type @T is not: TYPE_USE
				----------
				""");
	}
	// Test unspecified target.
	public void testUnspecifiedTarget2() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Repeatable;
					import java.lang.annotation.Target;
					
					@Target(ElementType.TYPE_PARAMETER)
					@interface TC {
						T [] value();
					}
					
					@Repeatable(TC.class)
					@interface T {
					}
					
					@T @T
					public class X {\s
						X f;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 14)
					@T @T
					^^
				The annotation @T cannot be repeated at this location since its container annotation type @TC is disallowed at this location
				----------
				""");
	}
	public void testDeprecation() {
		this.runNegativeTest(
			new String[] {
				"TC.java",
				"""
					@Deprecated
					public @interface TC {
					  public T[] value();
					}
					""",
				"T.java",
				"""
					@java.lang.annotation.Repeatable(TC.class)
					@interface T {
					  public int value() default -1;
					}
					interface I<@T(1) @T(2) K> {
					}
					"""
			},
			"""
				----------
				1. WARNING in T.java (at line 1)
					@java.lang.annotation.Repeatable(TC.class)
					                                 ^^
				The type TC is deprecated
				----------
				2. WARNING in T.java (at line 5)
					interface I<@T(1) @T(2) K> {
					            ^^
				The type TC is deprecated
				""");
	}
	public void testDeprecation2() { // verify that deprecation warning does not show up when the deprecated element is used in the same file defining it.
		this.runNegativeTest(
			new String[] {
				"T.java",
				"""
					@Deprecated
					@interface TC {
					  public T[] value();
					}
					@java.lang.annotation.Repeatable(TC.class)
					@interface T {
					  public int value() default -1;
					}
					interface I extends @T(1) Runnable {
					}
					"""
			},
			"""
				----------
				1. ERROR in T.java (at line 9)
					interface I extends @T(1) Runnable {
					                    ^^
				Annotation types that do not specify explicit target element types cannot be applied here
				----------
				""");
	}

	// 419209: [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
	public void testRepeatableWithContaining1() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"A.java",
				"""
					@interface FooContainerContainer {
					  public FooContainer[] value();
					}
					@java.lang.annotation.Repeatable(FooContainerContainer.class)
					@interface FooContainer {
					  public Foo[] value();
					}
					@java.lang.annotation.Repeatable(FooContainer.class)
					@interface Foo {
					  public int value() default -1;
					}
					@FooContainer({@Foo(1)}) @FooContainer({@Foo(2)}) @Foo(3) class A {}
					"""
			},
			"""
				----------
				1. WARNING in A.java (at line 12)
					@FooContainer({@Foo(1)}) @FooContainer({@Foo(2)}) @Foo(3) class A {}
					                                                  ^^^^
				The repeatable annotation @Foo may not be present where its container annotation type @FooContainer is repeated
				----------
				""");
	}
	// 419209: [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
	public void testRepeatableWithContaining2() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"A.java",
				"""
					@interface FooContainerContainer {
					  public FooContainer[] value();
					}
					@java.lang.annotation.Repeatable(FooContainerContainer.class)
					@interface FooContainer {
					  public Foo[] value();
					}
					@java.lang.annotation.Repeatable(FooContainer.class)
					@interface Foo {
					  public int value() default -1;
					}
					@Foo(1) @FooContainer({@Foo(2)}) @FooContainer({@Foo(3)}) class A {}
					"""
			},
			"""
				----------
				1. WARNING in A.java (at line 12)
					@Foo(1) @FooContainer({@Foo(2)}) @FooContainer({@Foo(3)}) class A {}
					^^^^
				The repeatable annotation @Foo may not be present where its container annotation type @FooContainer is repeated
				----------
				""");
	}
	// 419209: [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
	public void testRepeatableWithContaining3() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"A.java",
				"""
					@interface FooContainerContainer {
					  public FooContainer[] value();
					}
					@java.lang.annotation.Repeatable(FooContainerContainer.class)
					@interface FooContainer {
					  public Foo[] value();
					}
					@java.lang.annotation.Repeatable(FooContainer.class)
					@interface Foo {
					  public int value() default -1;
					}
					@FooContainer({@Foo(2)}) @Foo(1) @FooContainer({@Foo(3)}) class A {}
					"""
			},
			"""
				----------
				1. WARNING in A.java (at line 12)
					@FooContainer({@Foo(2)}) @Foo(1) @FooContainer({@Foo(3)}) class A {}
					                         ^^^^
				The repeatable annotation @Foo may not be present where its container annotation type @FooContainer is repeated
				----------
				""");
	}
	// check repeated occurrence of annotation where annotation container is not valid for the target
	public void testRepeatingAnnotationsWithoutTarget() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"""
					public @interface FooContainer {
						Foo[] value();
					}
					""",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
				"}\n",
				"X.java",
				"public class X<@Foo @Foo T> extends @Foo @Foo Object {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<@Foo @Foo T> extends @Foo @Foo Object {
					                                    ^^^^
				Annotation types that do not specify explicit target element types cannot be applied here
				----------
				2. ERROR in X.java (at line 1)
					public class X<@Foo @Foo T> extends @Foo @Foo Object {
					                                    ^^^^
				The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location
				----------
				3. ERROR in X.java (at line 1)
					public class X<@Foo @Foo T> extends @Foo @Foo Object {
					                                         ^^^^
				Annotation types that do not specify explicit target element types cannot be applied here
				----------
				""");
	}
}
