/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class AnnotationTest extends AbstractComparisonTest {
	
	String reportMissingJavadocComments = null;

	public AnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 0 };
//		TESTS_RANGE = new int[] { 21, 50 };
//	}
	public static Test suite() {
		return buildTestSuite(testClass());
	}

	public static Class testClass() {  
		return AnnotationTest.class;
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		if (reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		reportMissingJavadocComments = null;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public @interface X { \n" + 
				"	String value(); \n" +
				"}"
			},
			"");
	}
	
	// check invalid annotation
	// TODO (olivier) reenable once addressed
	public void _test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @Foo class X {\n" + 
				"}\n" + 
				"\n" + 
				"@interface Foo {\n" + 
				"	String value();\n" + 
				"}\n"
			},
			"invalid annotation - missing value");
	}
	
	// check annotation method cannot indirectly return annotation type (circular ref)
	// TODO (kent) reenable once addressed
	public void _test003() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	Bar value();\n" +
				"}\n" + 
				"\n" + 
				"@interface Bar {\n" + 
				"	Foo value();\n" +
				"}\n"
			},
			"invalid circular reference to annotation");
	}		

	// check annotation method cannot directly return annotation type
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	Foo value();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 2)\n" + 
			"	Foo value();\n" + 
			"	^^^\n" + 
			"Cycle detected: the annotation type Foo cannot contain members of the same type\n" + 
			"----------\n");
	}		

	// check annotation type cannot have superclass
	// TODO (olivier) unoptimal syntax error -> annotation type cannot extends/implements other types?
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo extends Object {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 1)\n" + 
			"	public @interface Foo extends Object {\n" + 
			"	       ^\n" + 
			"Syntax error on token \"@\", Identifier expected after this token\n" + 
			"----------\n" + 
			"2. ERROR in Foo.java (at line 1)\n" + 
			"	public @interface Foo extends Object {\n" + 
			"	                              ^^^^^^\n" + 
			"The type Object cannot be a superinterface of Foo; a superinterface must be an interface\n" + 
			"----------\n");
	}		

	// check annotation type cannot have superinterfaces
	// TODO (olivier) unoptimal syntax error -> annotation type cannot extends/implements other types?
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo implements Cloneable {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 1)\n" + 
			"	public @interface Foo implements Cloneable {\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Syntax error on tokens, ClassHeaderName expected instead\n" + 
			"----------\n");
	}		

	// check annotation method cannot be specified parameters
	// TODO (olivier) unoptimal syntax error -> no parameter for annotation method?
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	String value(int i);\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 2)\n" + 
			"	String value(int i);\n" + 
			"	            ^\n" + 
			"Syntax error on token \"(\", ; expected\n" + 
			"----------\n" + 
			"2. ERROR in Foo.java (at line 2)\n" + 
			"	String value(int i);\n" + 
			"	                  ^\n" + 
			"Syntax error on token \")\", delete this token\n" + 
			"----------\n");
	}			

	// TODO (olivier) unoptimal syntax error -> annotation method cannot be generic?
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	<T> T value();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 1)\n" + 
			"	public @interface Foo {\n" + 
			"	                      ^\n" + 
			"Syntax error on token \"{\", Type expected after this token\n" + 
			"----------\n" + 
			"2. ERROR in Foo.java (at line 2)\n" + 
			"	<T> T value();\n" + 
			"	      ^^^^^\n" + 
			"Syntax error on token \"value\", delete this token\n" + 
			"----------\n");
	}			

	// check annotation method return type
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	\n" + 
				"	Runnable value();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Runnable value();\n" + 
			"	^^^^^^^^\n" + 
			"Invalid type Runnable for the annotation method X.value(); only primitive, String, Class, enum or annotation types are permitted or an array of those types\n" + 
			"----------\n");
	}
	
	// check annotation method missing return type
	// TODO (olivier) we should get rid of syntax error here (tolerate invalid constructor scenario)
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	\n" + 
				"	value();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	value();\n" + 
			"	^^^^^\n" + 
			"Syntax error on token \"value\", Identifier expected after this token\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	value();\n" + 
			"	^^^^^^^\n" + 
			"Return type for the method is missing\n" + 
			"----------\n");
	}			
}
