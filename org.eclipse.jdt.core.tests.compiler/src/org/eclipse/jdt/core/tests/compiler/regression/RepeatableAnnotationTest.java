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
}
