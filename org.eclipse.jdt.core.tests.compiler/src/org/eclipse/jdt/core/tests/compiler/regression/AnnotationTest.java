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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AnnotationTest extends AbstractComparisonTest {
	
	String reportMissingJavadocComments = null;

	public AnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 25 };
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
		"Invalid type Runnable for the annotation attribute X.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" + 
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
	
	// check use of array initializer
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"	String[] value() default {};\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@Foo( {} )\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"");
	}
	
	// check use of binary annotation - check referencing binary annotation
	public void test024() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	String[] value() default {};\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo({})\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}			
	
	// check use of binary annotation - check default value presence
	public void test025() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	String[] value() default {};\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test026() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		int value() default 8;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test027() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		byte value() default (byte)255;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test028() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		boolean value() default true;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test029() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		char value() default ' ';\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test030() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		short value() default (short)1024;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test031() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		double value() default 0.0;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test032() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		float value() default -0.0f;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test033() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		long value() default 1234567890L;\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test034() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"		String value() default \"Hello, World\";\n" + 
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test035() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"enum E {\n" +
				"	CONST1\n" +
				"}\n" +
				"@interface Foo {\n" +
				"	E value() default E.CONST1;\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test036() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"@interface Foo {\n" +
				"	Class value() default Object.class;\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test037() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"@interface Y {\n" +
				"	int id() default 8;\n" +
				"	Class type();\n" +
				"}\n" +
				"public @interface Foo {\n" +
				"	Y value() default @Y(id=10,type=Object.class);\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@Foo()\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"",
			null,
			false,
			null);
	}
	
	// check use of binary annotation - check default value presence
	public void test038() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"@interface Foo {\n" +
				"	int id() default 8;\n" +
				"	Class type();\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Foo(type=String.class) public class X {\r\n" + 
				"}"
			},
			"",
			null,
			false,
			null);
	}
	
	// check annotation member modifiers
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	native int id() default 0;\n" + 
				"}"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	native int id() default 0;\n" + 
		"	           ^^\n" + 
		"Illegal modifier for the annotation attribute X.id; only public & abstract are permitted\n" + 
		"----------\n");
	}		
	
	// check annotation array field initializer
	public void test040() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	int[] tab;\n" + 
				"	int[] value();\n" + 
				"}\n" 
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	int[] tab;\n" + 
		"	      ^^^\n" + 
		"The annotation field X.tab must be initialized with a constant expression\n" + 
		"----------\n");
	}		
	
	// check annotation array field initializer
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	int[] tab = value();\n" + 
				"	int[] value();\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	int[] tab = value();\n" + 
		"	            ^^^^^\n" + 
		"Cannot make a static reference to the non-static method value() from the type X\n" + 
		"----------\n");
	}			
	
	// check annotation array field initializer
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	int[] tab = { 0 , \"aaa\".length() };\n" + 
				"}\n"
			},
		"");
	}		
	
	// check annotation field initializer
	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	int value;\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\r\n" + 
		"	int value;\r\n" + 
		"	    ^^^^^\n" + 
		"The annotation field X.value must be initialized with a constant expression\n" + 
		"----------\n");
	}
	
	// check annotation field initializer
	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	protected int value = 0;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	protected int value = 0;\n" + 
			"	              ^^^^^\n" + 
			"Illegal modifier for the annotation field X.value; only public, static & final are permitted\n" + 
			"----------\n");
	}
	
	// check incompatible default values
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface X {\n" + 
				"    int id () default 10L; \n" + 
				"    int[] ids() default { 10L };\n" + 
				"    Class cls() default new Object();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int id () default 10L; \n" + 
			"	                  ^^^\n" + 
			"Type mismatch: cannot convert from long to int\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	int[] ids() default { 10L };\n" + 
			"	                      ^^^\n" + 
			"Type mismatch: cannot convert from long to int\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	Class cls() default new Object();\n" + 
			"	                    ^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to Class\n" + 
			"----------\n");
	}
	
	// check need for constant pair value
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    boolean val() default true;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	boolean bar() {\n" + 
				"		return false;\n" + 
				"	}\n" + 
				"    @I(val = bar()) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	@I(val = bar()) void foo() {\n" + 
		"	         ^^^^^\n" + 
		"The value for annotation attribute I.val must be a constant expression\n" + 
		"----------\n");
	}
	
	// check array handling of singleton 
	public void test047() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    boolean[] val() default {true};\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(val = false) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(val={false})\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);

	}		
	
	// check invalid constant in array initializer
	public void test048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"     boolean[] value();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"     @I(value={false, X.class != null }) void foo() {\n" + 
				"     }\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	@I(value={false, X.class != null }) void foo() {\n" + 
		"	                 ^^^^^^^^^^^^^^^\n" + 
		"The value for annotation attribute I.value must be a constant expression\n" + 
		"----------\n");
	}			
	
	// 79349
	public void test049() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" + 
				"\n" + 
				"@Documented\n" + 
				"@Retention(RetentionPolicy.RUNTIME)\n" + 
				"@Target(ElementType.TYPE)\n" + 
				"@interface MyAnn {\n" + 
				"  String value() default \"Default Message\";\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public @MyAnn void something() { }	\n" + 
				"}\n"
			},
		"");
	}

	// check array handling of singleton 
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    String[] value();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(\"Hello\") void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value={\"Hello\"})\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	
	public void test051() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    String value() default \"Hello\";\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(\"Hi\") void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=\"Hi\")\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test052() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    int value() default 0;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(2) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=(int) 2)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// TODO (olivier) update once https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798 is fixed
	public void test053() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    byte value() default (byte) 0;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I((byte)2) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=(byte) 2)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// TODO (olivier) update once https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798 is fixed
	public void test054() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    short value() default (short) 0;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I((short)2) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=(short) 2)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test055() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    char value() default ' ';\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I('@') void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=\'@\')\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test056() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    long value() default 6;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(Long.MAX_VALUE) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=9223372036854775807L)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test057() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    float value();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(-0.0f) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=-0.0f)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	
	public void test058() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    double value();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(-0.0) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=-0.0)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test059() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"    double value() default 0.0;\n" + 
				"    int id();\n" + 
				"}\n" + 
				"@interface I {\n" + 
				"    Foo value();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(@Foo(id=5)) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=@Foo(id=(int) 5))\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test060() {
		this.runConformTest(
			new String[] {
				"X.java",
				"enum Color {" +
				"	BLUE, RED, GREEN\n" + 
				"}\n" + 
				"@interface I {\n" + 
				"    Color value() default Color.GREEN;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(Color.RED) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=Color.RED)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test061() {
		this.runConformTest(
			new String[] {
				"X.java",
				"enum Color {" +
				"	BLUE, RED, GREEN\n" + 
				"}\n" + 
				"@interface I {\n" + 
				"    Color[] value() default { Color.GREEN };\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(Color.RED) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value={Color.RED})\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 9]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// TODO (olivier) update once https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798 is fixed
	public void test062() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"    double value() default 0.0;\n" + 
				"    int id() default 0;\n" + 
				"}\n" + 
				"enum Color {" +
				"	BLUE, RED, GREEN\n" + 
				"}\n" + 
				"@interface I {\n" + 
				"    Color[] enums() default { Color.GREEN };\n" + 
				"    Foo[] annotations() default { @Foo() };\n" + 
				"    int[] ints() default { 0, 1, 2, 3 };\n" + 
				"    byte[] bytes() default { (byte) 0 };\n" + 
				"    short[] shorts() default { (short) 0 };\n" + 
				"    long[] longs() default { Long.MIN_VALUE, Long.MAX_VALUE };\n" + 
				"    String[] strings() default { \"\" };\n" + 
				"    boolean[] booleans() default { true, false };\n" + 
				"    float[] floats() default { Float.MAX_VALUE };\n" + 
				"    double[] doubles() default { Double.MAX_VALUE };\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(enums=Color.RED,\n" + 
				"		annotations=@Foo(),\n" + 
				"		ints=2,\n" + 
				"		bytes=(byte) 1,\n" + 
				"		shorts=(short) 5,\n" + 
				"		longs=Long.MIN_VALUE,\n" + 
				"		strings=\"Hi\",\n" + 
				"		booleans=true,\n" +
				"		floats=0.0f,\n" +
				"		doubles=-0.0) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(enums={Color.RED},\n" + 
			"      annotations={@Foo()},\n" + 
			"      ints={(int) 2},\n" + 
			"      bytes={(byte) 1},\n" + 
			"      shorts={(short) 5},\n" + 
			"      longs={-9223372036854775808L},\n" + 
			"      strings={\"Hi\"},\n" + 
			"      booleans={true},\n" + 
			"      floats={0.0f},\n" + 
			"      doubles={-0.0})\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 31]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	
	// TODO (olivier) update once https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798 is fixed
	public void test063() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" + 
				"    double value() default 0.0;\n" + 
				"    int id() default 0;\n" + 
				"}\n" + 
				"enum Color {" +
				"	BLUE, RED, GREEN\n" + 
				"}\n" + 
				"@interface I {\n" + 
				"    Color enums() default Color.GREEN;\n" + 
				"    Foo annotations() default @Foo();\n" + 
				"    int ints() default 0;\n" + 
				"    byte bytes() default (byte) 0;\n" + 
				"    short shorts() default (short) 0;\n" + 
				"    long longs() default Long.MIN_VALUE;\n" + 
				"    String strings() default \"\";\n" + 
				"    boolean booleans() default true;\n" + 
				"    float floats() default Float.MAX_VALUE;\n" + 
				"    double doubles() default Double.MAX_VALUE;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(enums=Color.RED,\n" + 
				"		annotations=@Foo(),\n" + 
				"		ints=2,\n" + 
				"		bytes=(byte) 1,\n" + 
				"		shorts=(short) 5,\n" + 
				"		longs=Long.MIN_VALUE,\n" + 
				"		strings=\"Hi\",\n" + 
				"		booleans=true,\n" +
				"		floats=0.0f,\n" +
				"		doubles=-0.0) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(enums=Color.RED,\n" + 
			"      annotations=@Foo(),\n" + 
			"      ints=(int) 2,\n" + 
			"      bytes=(byte) 1,\n" + 
			"      shorts=(short) 5,\n" + 
			"      longs=-9223372036854775808L,\n" + 
			"      strings=\"Hi\",\n" + 
			"      booleans=true,\n" + 
			"      floats=0.0f,\n" + 
			"      doubles=-0.0)\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 31]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	
	public void test064() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    String[] names();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(names={\"Hello\"}) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(names={\"Hello\"})\n" + 
			"  void foo();\n" + 
			"    0  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 7]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" + 
			"}"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79848
	public void test065() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    Class[] classes();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(classes = {X.class, I.class}) public void foo(){\n" +
				"    }\n" +
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
		
		String expectedOutput = 
			"  // Method descriptor  #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(classes={X,I})\n" +
			"  public void foo();\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 7]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// 79844
	public void test066() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    short value() default 0;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(2) void foo() {\n" + 
				"    }\n" + 
				"}\n" + 
				"\n"
			},
		"");
	}
	// 79844 - variation
	public void test067() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    int value() default 0L;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(2) void foo() {\n" + 
				"    }\n" + 
				"}\n" + 
				"\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int value() default 0L;\n" + 
			"	                    ^^\n" + 
			"Type mismatch: cannot convert from long to int\n" + 
			"----------\n");
	}
	
	// 79844 - variation
	public void test068() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    short[] value() default 2;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(2) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
	}	

	// 79844 - variation
	public void test069() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    short[] value() default { 2 };\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(2) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
	}	

}
