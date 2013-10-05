/*******************************************************************************
 * Copyright (c) 2013 Jesper S Moller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Jesper S Moller - initial API and implementation
 *     					Bug 412151 - [1.8][compiler] Check repeating annotation's collection type
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

public class RepeatableAnnotationTest extends AbstractComparableTest {

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test006" };
//		TESTS_NUMBERS = new int[] { 297 };
//		TESTS_RANGE = new int[] { 294, -1 };
	}

	public RepeatableAnnotationTest(String name) {
		super(name);
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
				"public @Foo @Foo class X {\n" +
				"}\n" +
				"\n",
				"Foo.java",
				"public @interface Foo {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public @Foo @Foo class X {\n" + 
			"	       ^^^^\n" + 
			"Duplicate annotation of non-repeatable type @Foo. Only annotation types marked @Repeatable can be used multiple times at one target.\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	public @Foo @Foo class X {\n" + 
			"	            ^^^^\n" + 
			"Duplicate annotation of non-repeatable type @Foo. Only annotation types marked @Repeatable can be used multiple times at one target.\n" + 
			"----------\n");
	}

	public void test002() {
		this.runConformTest(
				new String[] {
						"X.java",
						"@Foo @Foo public class X {\n" +
								"}\n" +
								"\n",
								"Foo.java",
								"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
										"}\n",
										"FooContainer.java",
										"public @interface FooContainer {\n" +
												"	Foo[] value();\n" +
												"}\n"
				},
				"");
	}

	// check repeated occurrence of annotation where annotation container is not valid for the target 
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target({ElementType.METHOD, ElementType.FIELD}) public @interface FooContainer {\n" +
				
				"	Foo[] value();\n" +
				"}\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
				"}\n",
				"X.java",
				"@Foo @Foo public class X { /* Problem */\n" +
				"  @Foo @Foo void okHere() { /* No problem */\n" +
				"    @Foo @Foo int local = 0; /* Problem! */\n" +
				"  }\n" +
				"  @Foo @Foo int alsoFoo = 0; /* No problem */\n" +
				"  @Foo class Y {} /* No problem since not repeated */\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Foo @Foo public class X { /* Problem */\n" + 
			"	^^^^\n" + 
			"The repeatable annotation @Foo is disallowed for this location since its container annotation @FooContainer is disallowed at this location\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	@Foo @Foo int local = 0; /* Problem! */\n" + 
			"	^^^^\n" + 
			"The repeatable annotation @Foo is disallowed for this location since its container annotation @FooContainer is disallowed at this location\n" + 
			"----------\n");
	}

	// This is the same test as test003, only where the annotation info for Foo is from a class file, not from the compiler
	public void test004() {
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target({ElementType.METHOD, ElementType.FIELD}) public @interface FooContainer {\n" +
					"	Foo[] value();\n" +
					"}\n",
					"Foo.java",
					"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
					"}\n"
				}, 
				"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo @Foo public class X { /* Problem */\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Foo @Foo public class X { /* Problem */\n" + 
			"	^^^^\n" + 
			"The repeatable annotation @Foo is disallowed for this location since its container annotation @FooContainer is disallowed at this location\n" + 
			"----------\n",
			null, false /* don't flush*/);
	}

	// Test that a single, repeatable annotation can exist just fine an occurrence of its container annotation
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@Foo @FooContainer({@Foo, @Foo}) public class X { /* Not a problem */ }\n"
			}, 
			"");
	}

	// Test that an repeated annotation can't occur together with its container annotation
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* A problem */ }\n"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* A problem */ }\n" + 
			"	^^^^\n" + 
			"The repeatable annotation @Foo may not be repeated where its container annotation @FooContainer is also used directly\n" + 
			"----------\n");
	}

	// Test that an repeated annotation can't occur together with its container annotation, even if it itself is repeatable.
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface FooContainerContainer { FooContainer[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class) @interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" + 
			"	^^^^\n" + 
			"The repeatable annotation @Foo may not be repeated where its container annotation @FooContainer is also used directly\n" + 
			"----------\n");
	}
	
	// Test that an repeated annotation can't occur together with its container annotation, even if it itself is repeatable.
	public void test007a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface FooContainerContainer { FooContainer[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class) @interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@interface Bar {}\n" +
				"@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" + 
			"	^^^^\n" + 
			"The repeatable annotation @Foo may not be repeated where its container annotation @FooContainer is also used directly\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" + 
			"	          ^^^^\n" + 
			"Duplicate annotation of non-repeatable type @Bar. Only annotation types marked @Repeatable can be used multiple times at one target.\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" + 
			"	               ^^^^\n" + 
			"Duplicate annotation of non-repeatable type @Bar. Only annotation types marked @Repeatable can be used multiple times at one target.\n" + 
			"----------\n");
	}

	// Test that repeated annotations should be contiguous (raises a warning if not) -- not yet in BETA_JAVA8
	public void _test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Bar {}\n" +
				"@interface Baz {}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@Foo @Bar @Foo /* just lexical */ @Foo public class X { /* Gives a warning */ }\n"
			}, 
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	@Foo @Bar @Foo /* just lexical */ @Foo public class X { /* Gives a warning */ }\n" + 
			"	          ^^^^\n" + 
			"Repeated @Foo annotations are not grouped together\n" + 
			"----------\n");
	}
	// Test that deprecation of container annotation is reflected in the repeated annotation (disabled until specification clarification is available)
	public void _test009() {
		this.runConformTest(
			new String[] {
				"Y.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo { int value(); }\n" +
				"@Deprecated @interface FooContainer { Foo[] value(); }\n" +
				"@Foo(0) class X { /* Gives a warning */ }\n" + 
				"@Foo(1) @Foo(2) public class Y { /* Gives a warning */ }\n"
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
			"@interface FooContainer {\n" +
			"}\n" +
			"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"@interface Foo {}\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 3)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"The containing annotation @FooContainer must declare a member value()\n" + 
		"----------\n");
	}
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test011() {
		this.runNegativeTest(
			new String[] {
			"Foo.java",
			"@interface FooContainer {\n" +
			"    int[] value();\n" +
			"}\n" +
			"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"@interface Foo {}\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 4)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"The value method in the containing annotation @FooContainer must be of type Foo[] but is int[]\n" + 
		"----------\n");
	}
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@interface FooContainer {\n" +
				"    Foo[][] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 2)\n" + 
			"	Foo[][] value();\n" + 
			"	^^^^^^^\n" + 
			"Invalid type Foo[][] for the annotation attribute FooContainer.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" + 
			"----------\n" + 
			"2. ERROR in Foo.java (at line 4)\n" + 
			"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
			"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
			"The value method in the containing annotation @FooContainer must be of type Foo[] but is Foo[][]\n" + 
			"----------\n"
		);
	}
	// 412151: Any methods declared by TC other than value() have a default value (§9.6.2).
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@interface FooContainer {\n" +
				"    Foo[] value();\n" +
				"    int hasDefaultValue() default 1337;\n" +
				"    int doesntHaveDefaultValue();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {}\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 6)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"The containing annotation @FooContainer must declare a default value for the annotation attribute \'doesntHaveDefaultValue\'\n" + 
		"----------\n");
	}
	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	public void test014() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface FooContainer {\n" +
				"    Foo[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface Foo {\n" +
				"}\n"
			}, 
		"");
	}

	// 
	public void test015() {
		// These are fine:
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"public @interface FooContainer {\n" +
					"	Foo[] value();\n" +
					"}\n",
					"Foo.java",
					"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
					"}\n"
				}, 
				"");
		// This changes FooContainer without re-checking Foo
		this.runConformTest(
				new String[] {
						"FooContainer.java",
						"public @interface FooContainer {\n" +
						"	int[] value();\n" +
						"}\n"
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
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Foo @Foo public class X { /* Problem since Foo now uses FooContainer which doesn\'t work anymore*/\n" + 
			"	^^^^\n" + 
			"The value method in the containing annotation @FooContainer must be of type Foo[] but is int[]\n" + 
			"----------\n",
			null, false /* don't flush*/);
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Base example, both targets are specified
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@Retention(RetentionPolicy.SOURCE)\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Foo { }\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 5)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"Retention \'RUNTIME\' of @Foo is longer than the retention of the containing annotation @FooContainer, which is \'SOURCE\'\n" + 
		"----------\n");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on FooContainer
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@Retention(RetentionPolicy.SOURCE)\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo { }\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 5)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"Retention \'CLASS\' of @Foo is longer than the retention of the containing annotation @FooContainer, which is \'SOURCE\'\n" + 
		"----------\n");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Foo { }\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 4)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"Retention \'RUNTIME\' of @Foo is longer than the retention of the containing annotation @FooContainer, which is \'CLASS\'\n" + 
		"----------\n");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo - but positive
	public void test019() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.SOURCE)\n" +
				"@interface Foo { }\n"
			});
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T
	// Only specified on FooContainer, separate compilation
	public void test020() {
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"import java.lang.annotation.Retention;\n" + 
					"import java.lang.annotation.RetentionPolicy;\n" + 
					"@Retention(RetentionPolicy.SOURCE)\n" +
					"public @interface FooContainer { Foo[] value(); }\n",
					"Foo.java",
					"import java.lang.annotation.Retention;\n" + 
					"import java.lang.annotation.RetentionPolicy;\n" + 
					"@Retention(RetentionPolicy.SOURCE)\n" +
					"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
					"public @interface Foo { }\n"
				});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"public @interface Foo { } // If omitted, retention is class\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 1)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"Retention \'CLASS\' of @Foo is longer than the retention of the containing annotation @FooContainer, which is \'SOURCE\'\n" + 
		"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo, separate compilation
	public void test021() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"public @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"public @interface Foo { }\n"
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.RetentionPolicy;\n" + 
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Foo { }\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 3)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"Retention \'RUNTIME\' of @Foo is longer than the retention of the containing annotation @FooContainer, which is \'CLASS\'\n" + 
		"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's @Targets, if specified, must be a subset or the same as T's @Targets. Simple test
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Target({ElementType.FIELD})\n" +
				"@interface Foo { }\n"
			},
		"----------\n" + 
		"1. ERROR in Foo.java (at line 3)\n" + 
		"	public @java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                        ^^^^^^^^^^^^^^^^^^\n" + 
		"The containing annotation @FooContainer is allowed at targets where the repeatable annotation @Foo is not: TYPE, METHOD\n" + 
		"----------\n");
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's @Targets, if specified, must be a subset or the same as T's @Targets. Test this as a separate pass, so that
	// FooContainer is loaded from binary.
	public void test023() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @Target({ElementType.METHOD})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @Target({ElementType.METHOD})\n" +
				"@interface Foo { }\n"
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@java.lang.annotation.Target({ElementType.FIELD})\n" +
				"@interface Foo { }\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 3)\n" + 
		"	public @java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                        ^^^^^^^^^^^^^^^^^^\n" + 
		"The containing annotation @FooContainer is allowed at targets where the repeatable annotation @Foo is not: METHOD\n" + 
		"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's may target ANNOTATION_TYPE but that should match TYPE for T, since it's a superset
	public void test024() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.ElementType;\n" + 
				"@java.lang.annotation.Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.ElementType;\n" + 
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@java.lang.annotation.Target({ElementType.METHOD, ElementType.TYPE})\n" +
				"@interface Foo { }\n"
			});
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// Test that all ElementTypes can be reported
	public void test025() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Target({})\n" +
				"@interface Foo { }\n"
			},
		"----------\n" + 
		"1. ERROR in Foo.java (at line 3)\n" + 
		"	public @java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                        ^^^^^^^^^^^^^^^^^^\n" + 
		"The containing annotation @FooContainer is allowed at targets where the repeatable annotation @Foo is not: TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE\n" + 
		"----------\n");
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's has no @Targets (=everywhere), but @Foo has, then complain.
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
				"import java.lang.annotation.Target;\n" + 
				"import java.lang.annotation.ElementType;\n" + 
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@java.lang.annotation.Target({ElementType.FIELD})\n" +
				"@interface Foo { }\n"
			}, 
		"----------\n" + 
		"1. ERROR in Foo.java (at line 3)\n" + 
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" + 
		"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"The repeatable annotation @Foo has a @Target annotation, @FooContainer does not\n" + 
		"----------\n",
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
			"----------\n" + 
			"1. ERROR in Foo.java (at line 1)\n" + 
			"	@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Documented\n" + 
			"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
			"The repeatable annotation @Foo is marked @Documented, but the containing annotation @FooContainer is not\n" + 
			"----------\n");
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
			"----------\n" + 
			"1. ERROR in Foo.java (at line 1)\n" + 
			"	@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Inherited\n" + 
			"	                                 ^^^^^^^^^^^^^^^^^^\n" + 
			"The repeatable annotation @Foo is marked @Inherited, but the containing annotation @FooContainer is not\n" + 
			"----------\n");
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
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.FIELD)\n" +
				"@interface TC {\n" +
				"	T [] value();\n" +
				"}\n" +
				"@Target(ElementType.TYPE)\n" +
				"@Repeatable(TC.class)\n" +
				"@interface T {\n" +
				"}\n" +
				"@T @T // we used to double report here.\n" +
				"public class X { \n" +
				"	X f;\n" +
				"}\n"
			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	@Repeatable(TC.class)\n" + 
			"	            ^^^^^^^^\n" + 
			"The containing annotation @TC is allowed at targets where the repeatable annotation @T is not: FIELD\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	@T @T // we used to double report here.\n" + 
			"	^^\n" + 
			"The repeatable annotation @T is disallowed for this location since its container annotation @TC is disallowed at this location\n" + 
			"----------\n");
	}	
}
