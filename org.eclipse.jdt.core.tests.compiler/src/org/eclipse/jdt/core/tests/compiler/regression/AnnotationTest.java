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
	public void test002() {
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
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public @Foo class X {\n" + 
			"	       ^^^^\n" + 
			"The annotation @Foo must define the attribute value\n" + 
			"----------\n");
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
			"Cycle detected: the annotation type Foo cannot contain attributes of the annotation type itself\n" + 
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
			"Invalid type Runnable for the annotation attribute X.value; only primitive, String, Class, enum or annotation types are permitted or an array of those types\n" + 
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
	
	// check annotation denotes annotation type
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Object\n" + 
				"public class X {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Object\n" + 
			"	 ^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to Annotation\n" + 
			"----------\n");
	}			
	
	// check for duplicate annotations
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo @Foo\n" + 
				"public class X {\n" + 
				"}\n" + 
				"@interface Foo {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Foo @Foo\n" + 
			"	^^^^\n" + 
			"Duplicate annotation @Foo\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	@Foo @Foo\n" + 
			"	     ^^^^\n" + 
			"Duplicate annotation @Foo\n" + 
			"----------\n");
	}
	
	// check single member annotation - no need to specify value if member has default value
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@Foo(\"hello\") public class X {\n" + 
				"}\n" + 
				"\n" + 
				"@interface Foo {\n" + 
				"	String id() default \"\";\n" + 
				"	String value() default \"\";\n" + 
				"}\n"
			},
			"");
	}	
	
	// check single member annotation -  need to speficy value if member has no default value
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo(\"hello\") public class X {\n" + 
				"}\n" + 
				"\n" + 
				"@interface Foo {\n" + 
				"	String id() default \"\";\n" + 
				"	String value() default \"\";\n" + 
				"	String foo();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Foo(\"hello\") public class X {\n" + 
			"	^^^^\n" + 
			"The annotation @Foo must define the attribute foo\n" + 
			"----------\n");
	}
	
	// check normal annotation -  need to speficy value if member has no default value
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo(\n" + 
				"		id = \"hello\") public class X {\n" + 
				"}\n" + 
				"\n" + 
				"@interface Foo {\n" + 
				"	String id() default \"\";\n" + 
				"	String value() default \"\";\n" + 
				"	String foo();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@Foo(\n" + 
			"	^^^^\n" + 
			"The annotation @Foo must define the attribute foo\n" + 
			"----------\n");
	}
	
	// check normal annotation - if single member, no need to be named 'value'
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Name {\n" + 
				"	String first();\n" + 
				"	String last();\n" + 
				"}\n" + 
				"@interface Author {\n" + 
				"	Name name();\n" + 
				"}\n" + 
				"public class X {\n" + 
				"	\n" + 
				"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\")) \n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"}\n"
			},
			"");
	}		

	// check single member annotation can only refer to 'value' member
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Name {\n" + 
				"	String first();\n" + 
				"	String last();\n" + 
				"}\n" + 
				"@interface Author {\n" + 
				"	Name name();\n" + 
				"}\n" + 
				"@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" + 
				"public class X {\n" + 
				"	\n" + 
				"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\")) \n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" + 
			"	^^^^^^^\n" + 
			"The annotation @Author must define the attribute name\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" + 
			"	        ^^^^^\n" + 
			"The attribute value is undefined for the annotation type Author\n" + 
			"----------\n");
	}		

	// check for duplicate member value pairs
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Name {\n" + 
				"	String first();\n" + 
				"	String last();\n" + 
				"}\n" + 
				"@interface Author {\n" + 
				"	Name name();\n" + 
				"}\n" + 
				"public class X {\n" + 
				"	\n" + 
				"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" + 
			"	                                   ^^^^\n" + 
			"Duplicate attribute last in annotation @Name\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" + 
			"	                                                ^^^^\n" + 
			"Duplicate attribute last in annotation @Name\n" + 
			"----------\n");
	}
	
	// check class annotation member value must be a class literal
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"	Class value() default X.clazz();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@Foo( clazz() )\n" + 
				"	void foo() {}\n" + 
				"	static Class clazz() { return X.class; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Class value() default X.clazz();\n" + 
			"	                      ^^^^^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a class literal\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	@Foo( clazz() )\n" + 
			"	      ^^^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a class literal\n" + 
			"----------\n");
	}			
	
	// check primitive annotation member value must be a constant
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"	int value() default X.val();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@Foo( val() )\n" + 
				"	void foo() {}\n" + 
				"	static int val() { return 0; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int value() default X.val();\n" + 
			"	                    ^^^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a constant expression\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	@Foo( val() )\n" + 
			"	      ^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a constant expression\n" + 
			"----------\n");
	}		
	// check String annotation member value must be a constant
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"	String value() default X.val();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@Foo( val() )\n" + 
				"	void foo() {}\n" + 
				"	static String val() { return \"\"; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	String value() default X.val();\n" + 
			"	                       ^^^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a constant expression\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	@Foo( val() )\n" + 
			"	      ^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a constant expression\n" + 
			"----------\n");
	}		
	
	// check String annotation member value must be a constant
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"	String[] value() default null;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@Foo( null )\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	String[] value() default null;\n" + 
			"	                         ^^^^\n" + 
			"The value for annotation attribute Foo.value must be a constant expression\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	@Foo( null )\n" + 
			"	      ^^^^\n" + 
			"The value for annotation attribute Foo.value must be a constant expression\n" + 
			"----------\n");
	}			
}
