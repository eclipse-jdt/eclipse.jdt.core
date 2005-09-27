/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.tests.util.Util;

public class AnnotationTest extends AbstractComparableTest {
	
	String reportMissingJavadocComments = null;

	public AnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 176 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
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
	public void test003() {
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
			"----------\n" + 
			"1. ERROR in Foo.java (at line 2)\n" + 
			"	Bar value();\n" + 
			"	^^^\n" + 
			"Cycle detected: a cycle exists in between annotation attributes of Foo and Bar\n" + 
			"----------\n" + 
			"2. ERROR in Foo.java (at line 6)\n" + 
			"	Foo value();\n" + 
			"	^^^\n" + 
			"Cycle detected: a cycle exists in between annotation attributes of Bar and Foo\n" + 
			"----------\n");
	    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=85538
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Nested {\n" + 
				"	String name() default \"Hans\";\n" +
				"	N2 nest();\n" +
				"}\n" +
				"@interface N2 {\n" + 
				"	Nested n2() default @Nested(name=\"Haus\", nest= @N2);\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	N2 nest();\n" + 
			"	^^\n" + 
			"Cycle detected: a cycle exists in between annotation attributes of Nested and N2\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	Nested n2() default @Nested(name=\"Haus\", nest= @N2);\n" + 
			"	^^^^^^\n" + 
			"Cycle detected: a cycle exists in between annotation attributes of N2 and Nested\n" + 
			"----------\n");
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
			"	                  ^^^\n" + 
			"Annotation type declaration cannot have an explicit superclass\n" + 
			"----------\n");
	}		

	// check annotation type cannot have superinterfaces
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
			"	                  ^^^\n" + 
			"Annotation type declaration cannot have explicit superinterfaces\n" + 
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
			"	       ^^^^^^^^^^^^\n" + 
			"Annotation attributes cannot have parameters\n" + 
			"----------\n");
	}			

	// annotation method cannot be generic?
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" + 
				"	<T> T value();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 2)\n" + 
			"	<T> T value();\n" + 
			"	    ^\n" + 
			"Invalid type T for the annotation attribute Foo.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" + 
			"----------\n" + 
			"2. ERROR in Foo.java (at line 2)\n" + 
			"	<T> T value();\n" + 
			"	      ^^^^^^^\n" + 
			"Annotation attributes cannot be generic\n" + 
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
			"1. WARNING in X.java (at line 2)\n" + 
			"	Class value() default X.clazz();\n" + 
			"	^^^^^\n" + 
			"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	Class value() default X.clazz();\n" + 
			"	                      ^^^^^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a class literal\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	@Foo( clazz() )\n" + 
			"	      ^^^^^^^\n" + 
			"The value for annotation attribute Foo.value must be a class literal\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 8)\n" + 
			"	static Class clazz() { return X.class; }\n" + 
			"	       ^^^^^\n" + 
			"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
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
		"	           ^^^^\n" + 
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
			"The blank final field tab may not have been initialized\n" + 
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
			"1. ERROR in X.java (at line 2)\n" + 
			"	int value;\n" + 
			"	    ^^^^^\n" + 
			"The blank final field value may not have been initialized\n" + 
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
			"3. WARNING in X.java (at line 4)\n" + 
			"	Class cls() default new Object();\n" + 
			"	^^^^^\n" + 
			"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 4)\n" + 
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(val={false})\n" + 
			"  void foo();"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);

		try {
			ClassFileReader fileReader = ClassFileReader.read(new File(OUTPUT_DIR + File.separator  +"I.class"));
			assertEquals("Not an annotation type declaration", TypeDeclaration.ANNOTATION_TYPE_DECL, TypeDeclaration.kind(fileReader.getModifiers()));
		} catch (ClassFormatException e1) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e1) {
			assertTrue("IOException", false);
		}
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
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79349
	public void test049() {
		this.runNegativeTest(
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
				"@MyAnn\n" +
				"public class X {\n" + 
				"	public @MyAnn void something() { }	\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\r\n" + 
		"	public @MyAnn void something() { }	\r\n" + 
		"	       ^^^^^^\n" + 
		"The annotation @MyAnn is disallowed for this location\n" + 
		"----------\n");
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value={\"Hello\"})\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=\"Hi\")\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=(int) 2)\n" + 
			"  void foo();"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test053() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    byte value() default 0;\n" + 
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=(byte) 2)\n" + 
			"  void foo();"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test054() {
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=(short) 2)\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=\'@\')\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=9223372036854775807L)\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=-0.0f)\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=-0.0)\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=@Foo(id=(int) 5))\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value=Color.RED)\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(value={Color.RED})\n" + 
			"  void foo();"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
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
				"    byte[] bytes() default { 0 };\n" + 
				"    short[] shorts() default { 0 };\n" + 
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
				"		bytes=1,\n" + 
				"		shorts=5,\n" + 
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
			"  // Method descriptor #6 ()V\n" + 
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
			"  void foo();"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	
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
				"    byte bytes() default 0;\n" + 
				"    short shorts() default 0;\n" + 
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
				"		bytes=1,\n" + 
				"		shorts=5,\n" + 
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
			"  // Method descriptor #6 ()V\n" + 
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
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" + 
			"  // Stack: 0, Locals: 1\n" + 
			"  @I(names={\"Hello\"})\n" + 
			"  void foo();"; 
			
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
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(classes={X,I})\n" +
			"  public void foo();"; 
			
		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
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

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
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

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79847
	public void test070() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"    int[][] ids();\n" + 
				"    Object[][] obs();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"    @I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int[][] ids();\n" + 
			"	^^^^^^^\n" + 
			"Invalid type int[][] for the annotation attribute I.ids; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Object[][] obs();\n" + 
			"	^^^^^^^^^^\n" + 
			"Invalid type Object[][] for the annotation attribute I.obs; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" + 
			"	^^\n" + 
			"The annotation @I must define the attribute obs\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 8)\n" + 
			"	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" + 
			"	          ^^^^^^^\n" + 
			"The value for annotation attribute I.ids must be a constant expression\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 8)\n" + 
			"	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" + 
			"	                   ^^^^^\n" + 
			"The value for annotation attribute I.ids must be a constant expression\n" + 
			"----------\n");
	}	

	// check annotation type cannot override any supertype method
	public void test071() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"	int hashCode();\n" + 
				"	Object clone();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(hashCode = 0) public void foo(){\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int hashCode();\n" + 
			"	    ^^^^^^^^^^\n" + 
			"The annotation type I cannot override the method Annotation.hashCode()\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Object clone();\n" + 
			"	^^^^^^\n" + 
			"Invalid type Object for the annotation attribute I.clone; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\n" + 
			"	Object clone();\n" + 
			"	       ^^^^^^^\n" + 
			"The annotation type I cannot override the method Object.clone()\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 7)\n" + 
			"	@I(hashCode = 0) public void foo(){\n" + 
			"	^^\n" + 
			"The annotation @I must define the attribute clone\n" + 
			"----------\n");
	}	

	// check annotation cannot refer to inherited methods as attributes
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(hashCode = 0) public void foo(){\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	@I(hashCode = 0) public void foo(){\n" + 
			"	   ^^^^^^^^\n" + 
			"The attribute hashCode is undefined for the annotation type I\n" + 
			"----------\n");
	}
	// check code generation of annotation default attribute (autowrapping)
	public void test073() {
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
				"    Color[] enums() default Color.GREEN;\n" + 
				"    Foo[] annotations() default @Foo();\n" + 
				"    int[] ints() default 0;\n" + 
				"    byte[] bytes() default 1;\n" + 
				"    short[] shorts() default 3;\n" + 
				"    long[] longs() default Long.MIN_VALUE;\n" + 
				"    String[] strings() default \"\";\n" + 
				"    boolean[] booleans() default true;\n" + 
				"    float[] floats() default Float.MAX_VALUE;\n" + 
				"    double[] doubles() default Double.MAX_VALUE;\n" + 
				"    Class[] classes() default I.class;\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"    @I(enums=Color.RED,\n" + 
				"		annotations=@Foo(),\n" + 
				"		ints=2,\n" + 
				"		bytes=1,\n" + 
				"		shorts=5,\n" + 
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
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"I.class"));
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
			"// Compiled from X.java (version 1.5 : 49.0, no super bit)\n" + 
			"abstract @interface I extends java.lang.Object implements java.lang.annotation.Annotation {\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()[LColor;\n" + 
			"  public abstract Color[] enums() default {Color.GREEN};\n" + 
			"  \n" + 
			"  // Method descriptor #13 ()[LFoo;\n" + 
			"  public abstract Foo[] annotations() default {@Foo()};\n" + 
			"  \n" + 
			"  // Method descriptor #16 ()[I\n" + 
			"  public abstract int[] ints() default {(int) 0};\n" + 
			"  \n" + 
			"  // Method descriptor #19 ()[B\n" + 
			"  public abstract byte[] bytes() default {(byte) 1};\n" + 
			"  \n" + 
			"  // Method descriptor #22 ()[S\n" + 
			"  public abstract short[] shorts() default {(short) 3};\n" + 
			"  \n" + 
			"  // Method descriptor #25 ()[J\n" + 
			"  public abstract long[] longs() default {-9223372036854775808L};\n" + 
			"  \n" + 
			"  // Method descriptor #29 ()[Ljava/lang/String;\n" + 
			"  public abstract String[] strings() default {\"\"};\n" + 
			"  \n" + 
			"  // Method descriptor #32 ()[Z\n" + 
			"  public abstract boolean[] booleans() default {true};\n" + 
			"  \n" + 
			"  // Method descriptor #34 ()[F\n" + 
			"  public abstract float[] floats() default {3.4028235E38f};\n" + 
			"  \n" + 
			"  // Method descriptor #37 ()[D\n" + 
			"  public abstract double[] doubles() default {1.7976931348623157E308};\n" + 
			"  \n" + 
			"  // Method descriptor #41 ()[Ljava/lang/Class;\n" + 
			"  public abstract Class[] classes() default {I};\n" + 
			"}"; 
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	// check code generation of annotation default attribute non array types
	public void test074() {
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
				"    Color _enum() default Color.GREEN;\n" + 
				"    Foo _annotation() default @Foo();\n" + 
				"    int _int() default 0;\n" + 
				"    byte _byte() default 1;\n" + 
				"    short _short() default 3;\n" + 
				"    long _long() default Long.MIN_VALUE;\n" + 
				"    String _string() default \"\";\n" + 
				"    boolean _boolean() default true;\n" + 
				"    float _float() default Float.MAX_VALUE;\n" + 
				"    double _double() default Double.MAX_VALUE;\n" + 
				"    Class _class() default I.class;\n" + 
				"}\n" + 
				"public class X {\n" + 
				"    @I(_enum=Color.RED,\n" + 
				"		_annotation=@Foo(),\n" + 
				"		_int=2,\n" + 
				"		_byte=1,\n" + 
				"		_short=5,\n" + 
				"		_long=Long.MIN_VALUE,\n" + 
				"		_string=\"Hi\",\n" + 
				"		_boolean=true,\n" +
				"		_float=0.0f,\n" +
				"		_double=-0.0) void foo() {\n" + 
				"    }\n" + 
				"}\n"
			},
		"");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"I.class"));
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
			"// Compiled from X.java (version 1.5 : 49.0, no super bit)\n" + 
			"abstract @interface I extends java.lang.Object implements java.lang.annotation.Annotation {\n" + 
			"  \n" + 
			"  // Method descriptor #8 ()LColor;\n" + 
			"  public abstract Color _enum() default Color.GREEN;\n" + 
			"  \n" + 
			"  // Method descriptor #13 ()LFoo;\n" + 
			"  public abstract Foo _annotation() default @Foo();\n" + 
			"  \n" + 
			"  // Method descriptor #16 ()I\n" + 
			"  public abstract int _int() default (int) 0;\n" + 
			"  \n" + 
			"  // Method descriptor #19 ()B\n" + 
			"  public abstract byte _byte() default (byte) 1;\n" + 
			"  \n" + 
			"  // Method descriptor #22 ()S\n" + 
			"  public abstract short _short() default (short) 3;\n" + 
			"  \n" + 
			"  // Method descriptor #25 ()J\n" + 
			"  public abstract long _long() default -9223372036854775808L;\n" + 
			"  \n" + 
			"  // Method descriptor #29 ()Ljava/lang/String;\n" + 
			"  public abstract String _string() default \"\";\n" + 
			"  \n" + 
			"  // Method descriptor #32 ()Z\n" + 
			"  public abstract boolean _boolean() default true;\n" + 
			"  \n" + 
			"  // Method descriptor #34 ()F\n" + 
			"  public abstract float _float() default 3.4028235E38f;\n" + 
			"  \n" + 
			"  // Method descriptor #37 ()D\n" + 
			"  public abstract double _double() default 1.7976931348623157E308;\n" + 
			"  \n" + 
			"  // Method descriptor #41 ()Ljava/lang/Class;\n" + 
			"  public abstract Class _class() default I;\n" + 
			"}"; 
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}	
	// check detection of duplicate target element specification
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"\n" + 
				"@Target ({FIELD, FIELD})\n" + 
				"@interface Tgt {\n" + 
				"	E[] foo();\n" + 
				"	int[] bar();\n" + 
				"}\n" + 
				"enum E {\n" + 
				"	BLEU, BLANC, ROUGE\n" + 
				"}\n" + 
				"\n" + 
				"@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )\n" + 
				"public class X {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	@Target ({FIELD, FIELD})\n" + 
			"	                 ^^^^^\n" + 
			"Duplicate element FIELD specified in annotation @Target\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 13)\n" + 
			"	@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )\n" + 
			"	^^^^\n" + 
			"The annotation @Tgt is disallowed for this location\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77463
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"private @interface TestAnnot {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	private @interface TestAnnot {\n" + 
			"	                   ^^^^^^^^^\n" + 
			"Illegal modifier for the annotation type TestAnnot; only public & abstract are permitted\n" + 
			"----------\n");
	}	
	// check @Override annotation - strictly for superclasses (overrides) and not interfaces (implements)
	public void test077() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Further {\n" + 
				"	void bar() {}\n" + 
				"}\n" + 
				"\n" + 
				"class Other extends Further {\n" + 
				"}\n" + 
				"\n" + 
				"interface Baz {\n" + 
				"	void baz();\n" + 
				"}\n" + 
				"\n" + 
				"public class X extends Other implements Baz {\n" + 
				"	@Override\n" + 
				"	void foo() {}\n" + 
				"	@Override\n" + 
				"	void bar() {}\n" + 
				"	@Override\n" + 
				"	public void baz() {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	void foo() {}\n" + 
			"	     ^^^^^\n" + 
			"The method foo() of type X must override a superclass method\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	public void baz() {}\n" + 
			"	            ^^^^^\n" + 
			"The method baz() of type X must override a superclass method\n" + 
			"----------\n");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80114
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" + 
				"	X() {}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	X() {}\n" + 
			"	^^^\n" + 
			"Annotation type declaration cannot have a constructor\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test079() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"@Attr(tst=-1)");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(CLASS)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(SOURCE)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test082() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(SOURCE)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test083() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(CLASS)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test084() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"@Attr(tst=-1)",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76751
	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"  @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface\n" +
				"TestAnnotation {\n" +
				"\n" +
				"    String testAttribute();\n" +
				"\n" +
				"  }\n" +
				"  @TestAnnotation(testAttribute = \"test\") class A {\n" +
				"  }\n" +
				"\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
				"  }\n" +
				"\n" +
				"}"
			},
			"true");
	}
	
	// check handling of empty array initializer
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target({}) @interface I {}\n" +
				"@I public class X {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	@I public class X {}\n" + 
			"	^^\n" + 
			"The annotation @I is disallowed for this location\n" + 
			"----------\n");
	}
	
	// check type targeting annotation also allowed for annotation type
	public void test087() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"\n" + 
				"@Target(TYPE)\n" + 
				"@interface Annot {\n" + 
				"}\n" + 
				"\n" + 
				"@Annot\n" + 
				"public @interface X {\n" + 
				"}\n"
			},
			"");
	}	
	
	// check parameter/local target for annotation
	public void test088() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.ElementType.*;\n" + 
				"\n" + 
				"@Target(LOCAL_VARIABLE)\n" + 
				"@interface Annot {\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo(@Annot int i) {\n" + 
				"		@Annot int j;\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	void foo(@Annot int i) {\n" + 
			"	         ^^^^^^\n" + 
			"The annotation @Annot is disallowed for this location\n" + 
			"----------\n");
	}
	// Add check for parameter
	public void test089() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.PARAMETER) @interface I {}\n" +
				"    \n" +
				"    void m(@I int i){\n" +
				"    }\n" +
				"}"
			},
			"");
	}
	// Add check that type includes annotation type
	public void test090() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.TYPE) @interface Annot1 {}\n" +
				"    \n" +
				"    @Annot1 @interface Annot2 {}\n" +
				"}"
			},
			"");
	}
	// Add check that a field cannot have an annotation targetting TYPE
	public void test091() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.TYPE) @interface Marker {}\n" +
				"    \n" +
				"    @Marker static int i = 123;\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	@Marker static int i = 123;\n" + 
			"	^^^^^^^\n" + 
			"The annotation @X.Marker is disallowed for this location\n" + 
			"----------\n");
	}
	// Add check that a field cannot have an annotation targetting FIELD
	public void test092() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.FIELD) @interface Marker {}\n" +
				"    \n" +
				"    @Marker static int i = 123;\n" +
				"}"
			},
			"");
	}
	// @Inherited can only be used on annotation types
	public void test093() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Inherited;\n" + 
				"\n" + 
				"@Deprecated\n" + 
				"@Inherited\n" + 
				"class A {\n" + 
				"}\n" + 
				"\n" + 
				"class B extends A {\n" + 
				"}\n" + 
				"\n" + 
				"class C extends B {\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	C c;\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\r\n" + 
		"	@Inherited\r\n" + 
		"	^^^^^^^^^^\n" + 
		"The annotation @Inherited is disallowed for this location\n" + 
		"----------\n");
	}
	
	// check handling of empty array initializer (binary check)
	public void test094() {
		this.runConformTest(
			new String[] {
				"I.java",
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target({}) @interface I {}",
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@I public class X {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	@I public class X {}\n" + 
			"	^^\n" + 
			"The annotation @I is disallowed for this location\n" + 
			"----------\n",
			null,
			false,
			null);
	}
	
	// check no interaction between Retention and Target (switch fall-thru)
	public void test095() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" + 
				"\n" + 
				"@Retention(RetentionPolicy.RUNTIME)\n" + 
				"@interface Ann {}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@Ann\n" + 
				"	void foo() {}\n" + 
				"}\n",
			},
			"");
	}

	// check attributes for parameters
	public void test096() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"@Retention(CLASS) @interface Attr {\n" +
				"}\n" +
				"\n" +
				"@Retention(RUNTIME) @interface Foo {\n" +
				"	int id() default 0;\n" +
				"}\n" +
				"@Foo(id=5) @Attr public class X {\n" +
				"	public void foo(@Foo(id=5) @Attr final int j, @Attr final int k, int n) {\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			Class c = X.class;\n" +
				"			Annotation[] annots = c.getAnnotations();\n" +
				"			System.out.print(annots.length);\n" +				"			Method method = c.getMethod(\"foo\", Integer.TYPE, Integer.TYPE, Integer.TYPE);\n" +
				"			Annotation[][] annotations = method.getParameterAnnotations();\n" +
				"			final int length = annotations.length;\n" +
				"			System.out.print(length);\n" +
				"			if (length == 3) {\n" +
				"				System.out.print(annotations[0].length);\n" +
				"				System.out.print(annotations[1].length);\n" +
				"				System.out.print(annotations[2].length);\n" +
				"			}\n" +
				"		} catch(NoSuchMethodException e) {\n" +
				"		}\n" +
				"	}\n" +
				"}",
			},
			"13100");
	}
	
	public void test097() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"	int id default 0;\n" +
				"}\n" +
				"\n" +
				"@I() public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.println(X.class.getAnnotation(I.class));\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int id default 0;\n" + 
			"	       ^^^^^^^\n" + 
			"Syntax error on token \"default\", = expected\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80328
	public void test098() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"	int id default 0;\n" +
				"}\n" +
				"\n" +
				"@I() public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.println(X.class.getAnnotation(I.class));\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int id default 0;\n" + 
			"	       ^^^^^^^\n" + 
			"Syntax error on token \"default\", = expected\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80780
	public void test099() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Object o = new X();\n" +
				"        for (Method m : o.getClass().getMethods()) {\n" +
				"            if (m.isAnnotationPresent(MyAnon.class)) {\n" +
				"                System.out.println(m.getAnnotation(MyAnon.class).c());\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"    @MyAnon(c = X.class) \n" +
				"    public void foo() {}\n" +
				"\n" +
				"    @Retention(RetentionPolicy.RUNTIME) \n" +
				"    public @interface MyAnon {\n" +
				"        Class c();\n" +
				"    }\n" +
				"    public interface I {\n" +
				"    }\n" +
				"}"
			},
			"class X");

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
			"  Inner classes:\n" + 
			"    [inner class info: #29 X$MyAnon, outer class info: #1 X\n" + 
			"     inner name: #68 MyAnon, accessflags: 9737 public abstract static ],\n" + 
			"    [inner class info: #69 X$I, outer class info: #1 X\n" + 
			"     inner name: #71 I, accessflags: 1545 public abstract static ]\n"; 
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80544
	public void test100() {
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class Foo {\n" + 
				"	abstract protected boolean accept(Object o);\n" + 
				"}\n" + 
				"\n" + 
				"public class X extends Foo {\n" + 
				"	@Override \n" + 
				"	protected boolean accept(Object o) { return false; }\n" + 
				"}\n",
			},
			"");
	}		

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81148
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" + 
				"\n" + 
				"@Target(Element)\n" + 
				"public @interface X {\n" + 
				"	\n" + 
				"	boolean UML() default false;\n" + 
				"	boolean platformDependent() default true;\n" + 
				"	boolean OSDependent() default true;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	@Target(Element)\r\n" + 
			"	        ^^^^^^^\n" + 
			"Element cannot be resolved\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
	public void test102() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  @TestAnnotation(testAttribute = \"test\") class A {\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
				"  }\n" +
				"}",
				"TestAnnotation.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface\n" +
				"TestAnnotation {\n" +
				"    String testAttribute();\n" +
				"}\n"
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
	public void test103() {
		this.runConformTest(
			new String[] {
				"TestAnnotation.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface\n" +
				"TestAnnotation {\n" +
				"    String testAttribute();\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  @TestAnnotation(testAttribute = \"test\") class A {\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
				"  }\n" +
				"}",
			},
			"true",
			null,
			false,
			null);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81825
	public void test104() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface ValuesAnnotation {\n" + 
				"	byte[] byteArrayValue();\n" + 
				"	char[] charArrayValue();\n" + 
				"	boolean[] booleanArrayValue();\n" + 
				"	int[] intArrayValue();\n" + 
				"	short[] shortArrayValue();\n" + 
				"	long[] longArrayValue();\n" + 
				"	float[] floatArrayValue();\n" + 
				"	double[] doubleArrayValue();\n" + 
				"	String[] stringArrayValue();\n" + 
				"	ValuesEnum[] enumArrayValue();\n" + 
				"	ValueAttrAnnotation[] annotationArrayValue();\n" + 
				"	Class[] classArrayValue();\n" + 
				"	byte byteValue();\n" + 
				"	char charValue();\n" + 
				"	boolean booleanValue();\n" + 
				"	int intValue();\n" + 
				"	short shortValue();\n" + 
				"	long longValue();\n" + 
				"	float floatValue();\n" + 
				"	double doubleValue();\n" + 
				"	String stringValue();\n" + 
				"	ValuesEnum enumValue();\n" + 
				"	ValueAttrAnnotation annotationValue();\n" + 
				"	Class classValue();\n" + 
				"}\n" + 
				"enum ValuesEnum {\n" + 
				"	ONE, TWO;\n" + 
				"}\n" + 
				"\n" + 
				"@interface ValueAttrAnnotation {\n" + 
				"	String value() default \"\";\n" + 
				"}\n" + 
				"@interface ValueAttrAnnotation1 {\n" + 
				"	String value();\n" + 
				"}\n" + 
				"@interface ValueAttrAnnotation2 {\n" + 
				"	String value();\n" + 
				"}\n" + 
				"@ValuesAnnotation(\n" + 
				"  byteValue = 1,\n" + 
				"  charValue = \'A\',\n" + 
				"  booleanValue = true,\n" + 
				"  intValue = 1,\n" + 
				"  shortValue = 1,\n" + 
				"  longValue = 1L,\n" + 
				"  floatValue = 1.0f,\n" + 
				"  doubleValue = 1.0d,\n" + 
				"  stringValue = \"A\",\n" + 
				"\n" + 
				"  enumValue = ValuesEnum.ONE,\n" + 
				"  annotationValue = @ValueAttrAnnotation( \"annotation\"),\n" + 
				"  classValue = X.class,\n" + 
				"\n" + 
				"  byteArrayValue = { 1, -1},\n" + 
				"  charArrayValue = { \'c\', \'b\', (char)-1},\n" + 
				"  booleanArrayValue = {true, false},\n" + 
				"  intArrayValue = { 1, -1},\n" + 
				"  shortArrayValue = { (short)1, (short)-1},\n" + 
				"  longArrayValue = { 1L, -1L},\n" + 
				"  floatArrayValue = { 1.0f, -1.0f},\n" + 
				"  doubleArrayValue = { 1.0d, -1.0d},\n" + 
				"  stringArrayValue = { \"aa\", \"bb\"},\n" + 
				"\n" + 
				"  enumArrayValue = {ValuesEnum.ONE, ValuesEnum.TWO},\n" + 
				"  annotationArrayValue = {@ValueAttrAnnotation( \"annotation1\"),\n" + 
				"@ValueAttrAnnotation( \"annotation2\")},\n" + 
				"  classArrayValue = {X.class, X.class}\n" + 
				")\n" + 
				"@ValueAttrAnnotation1( \"classAnnotation1\")\n" + 
				"@ValueAttrAnnotation2( \"classAnnotation2\")\n" + 
				"public class X {\n" + 
				"\n" + 
				"  @ValueAttrAnnotation1( \"fieldAnnotation1\")\n" + 
				"  @ValueAttrAnnotation2( \"fieldAnnotation2\")\n" + 
				"  public String testfield = \"test\";\n" + 
				"\n" + 
				"  @ValueAttrAnnotation1( \"methodAnnotation1\")\n" + 
				"  @ValueAttrAnnotation2( \"methodAnnotation2\")\n" + 
				"  @ValueAttrAnnotation()\n" + 
				"  public void testMethod( \n" + 
				"      @ValueAttrAnnotation1( \"param1Annotation1\") \n" + 
				"      @ValueAttrAnnotation2( \"param1Annotation2\") String param1, \n" + 
				"      @ValueAttrAnnotation1( \"param2Annotation1\") \n" + 
				"      @ValueAttrAnnotation2( \"param2Annotation2\") int param2) {\n" + 
				"    // @ValueAttrAnnotation( \"codeAnnotation\")\n" + 
				"  }\n" + 
				"}\n"
			},
			"");	
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82136
	public void test105() {
		this.runConformTest(
			new String[] {
				"Property.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"public @interface Property\n" +
				"{\n" +
				"  String property();\n" +
				"  String identifier() default \"\";\n" +
				"}",
				"Properties.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"public @interface Properties {\n" +
				"  Property[] value();\n" +
				"}",
				"X.java",
				"@Properties({\n" +
				"  @Property(property = \"prop\", identifier = \"someIdentifier\"),\n" +
				"  @Property(property = \"type\")\n" +
				"})\n" +
				"public interface X {\n" +
				"  void setName();\n" +
				"  String getName();\n" +
				"}"
			},
			"");	
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				new ClassFileReader(classFileBytes, "X.java".toCharArray(), true);
			} catch (ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
	}
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test106() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public @interface X {\n" +
                "    int[] bar() default null;\n" +
                "}",
            },
            "----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	int[] bar() default null;\n" + 
			"	                    ^^^^\n" + 
			"The value for annotation attribute X.bar must be a constant expression\n" + 
			"----------\n");
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test107() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@interface Ann {\n" +
                "    int[] bar();\n" +
                "}\n" +
                "@Ann(bar=null) class X {}",
            },
            "----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	@Ann(bar=null) class X {}\n" + 
			"	         ^^^^\n" + 
			"The value for annotation attribute Ann.bar must be a constant expression\n" + 
			"----------\n");
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test108() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Foo {}\n" + 
				"\n" + 
				"@interface Bar {\n" + 
				"    Foo[] foo() default null;\n" + 
				"}\n" + 
				"\n" + 
				"@Bar(foo=null)\n" + 
				"public class X { \n" + 
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	Foo[] foo() default null;\n" + 
			"	                    ^^^^\n" + 
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	@Bar(foo=null)\n" + 
			"	         ^^^^\n" + 
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" + 
			"----------\n");
    }    
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test109() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Foo {}\n" + 
				"\n" + 
				"@interface Bar {\n" + 
				"    Foo[] foo() default \"\";\n" + 
				"}\n" + 
				"\n" + 
				"@Bar(foo=\"\")\n" + 
				"public class X { \n" + 
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	Foo[] foo() default \"\";\n" + 
			"	                    ^^\n" + 
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	@Bar(foo=\"\")\n" + 
			"	         ^^\n" + 
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" + 
			"----------\n");
    }        
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791
    public void test110() {
        this.runConformTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.Annotation;\n" + 
				"import java.util.Arrays;\n" + 
				"\n" + 
				"@interface Ann {\n" + 
				"}\n" + 
				"\n" + 
				"interface Iface extends Ann {\n" + 
				"}\n" + 
				"\n" + 
				"abstract class Klass implements Ann {\n" + 
				"}\n" + 
				"\n" + 
				"class SubKlass extends Klass {\n" + 
				"	public Class<? extends Annotation> annotationType() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Class c = SubKlass.class;\n" + 
				"		System.out.print(\"Classes:\");\n" + 
				"		while (c != Object.class) {\n" + 
				"			System.out.print(\"-> \" + c.getName());\n" + 
				"			c = c.getSuperclass();\n" + 
				"		}\n" + 
				"\n" + 
				"		System.out.print(\", Interfaces:\");\n" + 
				"		c = SubKlass.class;\n" + 
				"		while (c != Object.class) {\n" + 
				"			Class[] i = c.getInterfaces();\n" + 
				"			System.out.print(\"-> \" + Arrays.asList(i));\n" + 
				"			c = c.getSuperclass();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
            },
			"Classes:-> SubKlass-> Klass, Interfaces:-> []-> [interface Ann]");
    }  

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791 - variation
    public void test111() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.Annotation;\n" + 
				"import java.util.Arrays;\n" + 
				"\n" + 
				"@interface Ann {\n" + 
				"	int foo();\n" + 
				"}\n" + 
				"\n" + 
				"interface Iface extends Ann {\n" + 
				"}\n" + 
				"\n" + 
				"abstract class Klass implements Ann {\n" + 
				"}\n" + 
				"\n" + 
				"class SubKlass extends Klass {\n" + 
				"	public Class<? extends Annotation> annotationType() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AnnImpl implements Ann {\n" + 
				"    public boolean equals(Object obj) { return false; }\n" + 
				"    public int hashCode() { return 0; }\n" + 
				"    public String toString() { return null; }\n" + 
				"    public Class<? extends Annotation> annotationType() { return null; }\n" + 
				"    public int foo() { return 0; }\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Class c = SubKlass.class;\n" + 
				"		System.out.println(\"Classes:\");\n" + 
				"		while (c != Object.class) {\n" + 
				"			System.out.println(\"-> \" + c.getName());\n" + 
				"			c = c.getSuperclass();\n" + 
				"		}\n" + 
				"\n" + 
				"		System.out.println();\n" + 
				"		System.out.println(\"Interfaces:\");\n" + 
				"		c = SubKlass.class;\n" + 
				"		while (c != Object.class) {\n" + 
				"			Class[] i = c.getInterfaces();\n" + 
				"			System.out.println(\"-> \" + Arrays.asList(i));\n" + 
				"			c = c.getSuperclass();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 8)\n" + 
    		"	interface Iface extends Ann {\n" + 
    		"	                        ^^^\n" + 
    		"The annotation type Ann should not be used as a superinterface for Iface\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 11)\n" + 
    		"	abstract class Klass implements Ann {\n" + 
    		"	                                ^^^\n" + 
    		"The annotation type Ann should not be used as a superinterface for Klass\n" + 
    		"----------\n" + 
    		"3. ERROR in X.java (at line 14)\n" + 
    		"	class SubKlass extends Klass {\n" + 
    		"	      ^^^^^^^^\n" + 
    		"The type SubKlass must implement the inherited abstract method Ann.foo()\n" + 
    		"----------\n" + 
    		"4. WARNING in X.java (at line 20)\n" + 
    		"	class AnnImpl implements Ann {\n" + 
    		"	                         ^^^\n" + 
    		"The annotation type Ann should not be used as a superinterface for AnnImpl\n" + 
    		"----------\n" + 
    		"5. WARNING in X.java (at line 21)\n" + 
    		"	public boolean equals(Object obj) { return false; }\n" + 
    		"	               ^^^^^^^^^^^^^^^^^^\n" + 
    		"The method equals(Object) of type AnnImpl should be tagged with @Override since it actually overrides a superclass method\n" + 
    		"----------\n" + 
    		"6. WARNING in X.java (at line 22)\n" + 
    		"	public int hashCode() { return 0; }\n" + 
    		"	           ^^^^^^^^^^\n" + 
    		"The method hashCode() of type AnnImpl should be tagged with @Override since it actually overrides a superclass method\n" + 
    		"----------\n" + 
    		"7. WARNING in X.java (at line 23)\n" + 
    		"	public String toString() { return null; }\n" + 
    		"	              ^^^^^^^^^^\n" + 
    		"The method toString() of type AnnImpl should be tagged with @Override since it actually overrides a superclass method\n" + 
    		"----------\n" + 
    		"8. WARNING in X.java (at line 30)\n" + 
    		"	Class c = SubKlass.class;\n" + 
    		"	^^^^^\n" + 
    		"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n");
    }            
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291
    public void test112() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" + 
				"  String foo1() default \"\";\n" + 
				"}\n" + 
				"@Annot(foo1=zzz)\n" + 
				"public class X {\n" + 
				"  static final String zzz =  \"\";\n" + 
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	@Annot(foo1=zzz)\n" + 
			"	            ^^^\n" + 
			"zzz cannot be resolved\n" + 
			"----------\n");
    }          
    public void test113() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" + 
				"	String foo();\n" + 
				"}\n" + 
				"@Annot( foo = new String(){} )\n" + 
				"public class X {\n" + 
				"	\n" + 
				"	\n" + 
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	@Annot( foo = new String(){} )\n" + 
			"	              ^^^^^^^^^^^^^^\n" + 
			"The value for annotation attribute Annot.foo must be a constant expression\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	@Annot( foo = new String(){} )\n" + 
			"	                  ^^^^^^\n" + 
			"An anonymous class cannot subclass the final class String\n" + 
			"----------\n");
    }     	
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291 - variation
    public void test114() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" + 
				"	Class foo();\n" + 
				"}\n" + 
				"@Annot( foo = M.class )\n" + 
				"public class X {\n" + 
				"	class M {}\n" + 
				"	\n" + 
				"}\n",
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 2)\n" + 
    		"	Class foo();\n" + 
    		"	^^^^^\n" + 
    		"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n" + 
    		"2. ERROR in X.java (at line 4)\n" + 
    		"	@Annot( foo = M.class )\n" + 
    		"	              ^\n" + 
    		"M cannot be resolved to a type\n" + 
    		"----------\n");
    }  	
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291 - variation
    public void test115() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" + 
				"	Class foo();\n" + 
				"	String bar() default \"\";\n" + 
				"}\n" + 
				"@Annot(foo = M.class, bar = baz()+s)\n" + 
				"public class X {\n" + 
				"	class M {\n" + 
				"	}\n" + 
				"	final static String s = \"\";\n" + 
				"	String baz() { return null; }\n" + 
				"	@Annot(foo = T.class, bar = s)\n" + 
				"	<T> T foo(T t, String s) {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n",
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 2)\n" + 
    		"	Class foo();\n" + 
    		"	^^^^^\n" + 
    		"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n" + 
    		"2. ERROR in X.java (at line 5)\n" + 
    		"	@Annot(foo = M.class, bar = baz()+s)\n" + 
    		"	             ^\n" + 
    		"M cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"3. ERROR in X.java (at line 5)\n" + 
    		"	@Annot(foo = M.class, bar = baz()+s)\n" + 
    		"	                            ^^^\n" + 
    		"The method baz() is undefined for the type X\n" + 
    		"----------\n" + 
    		"4. ERROR in X.java (at line 5)\n" + 
    		"	@Annot(foo = M.class, bar = baz()+s)\n" + 
    		"	                                  ^\n" + 
    		"s cannot be resolved\n" + 
    		"----------\n" + 
    		"5. ERROR in X.java (at line 11)\n" + 
    		"	@Annot(foo = T.class, bar = s)\n" + 
    		"	             ^^^^^^^\n" + 
    		"Illegal class literal for the type parameter T\n" + 
    		"----------\n" + 
    		"6. WARNING in X.java (at line 12)\n" + 
    		"	<T> T foo(T t, String s) {\n" + 
    		"	                      ^\n" + 
    		"The parameter s is hiding a field from type X\n" + 
    		"----------\n");
    }  	
    // check @Deprecated support
    public void test116() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"/** @deprecated */\n" +
				"@Deprecated\n" + 
				"public class X {\n" + 
				"}\n",
                "Y.java",
				"public class Y {\n" + 
				"	X x;\n" + 
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. WARNING in Y.java (at line 2)\n" + 
			"	X x;\n" + 
			"	^\n" + 
			"The type X is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }  		
    // check @Deprecated support
    public void test117() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@Deprecated\n" + 
				"public class X {\n" + 
				"}\n",
                "Y.java",
				"public class Y {\n" + 
				"	X x;\n" + 
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. WARNING in Y.java (at line 2)\n" + 
			"	X x;\n" + 
			"	^\n" + 
			"The type X is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }  			
    // check @Deprecated support
    public void test118() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Deprecated {}\n" + 
				"\n" + 
				"@Deprecated // not the real @Deprecated interface\n" + 
				"public class X {\n" + 
				"}\n",
                "Y.java",
				"public class Y {\n" + 
				"	X x;\n" + 
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in Y.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }  	
    // check @Deprecated support
    public void test119() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@Deprecated\n" + 
				"public class X {\n" + 
				"	void foo(){}\n" +
				"}\n",
                "Y.java",
				"public class Y extends X {\n" + 
				"	void foo(){ super.foo(); }\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. WARNING in Y.java (at line 1)\n" + 
			"	public class Y extends X {\n" + 
			"	             ^\n" + 
			"The constructor X() is deprecated\n" + 
			"----------\n" + 
			"2. WARNING in Y.java (at line 2)\n" + 
			"	void foo(){ super.foo(); }\n" + 
			"	     ^^^^^\n" + 
			"The method foo() of type Y should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n" + 
			"3. WARNING in Y.java (at line 2)\n" + 
			"	void foo(){ super.foo(); }\n" + 
			"	            ^^^^^^^^^^^\n" + 
			"The method foo() from the type X is deprecated\n" + 
			"----------\n" + 
			"4. ERROR in Y.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }  		
    // check @Deprecated support
    public void test120() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@Deprecated\n" + 
				"public class X {\n" + 
				"	void foo(){}\n" +
				"}\n",
                "Y.java",
				"public class Y extends X {\n" + 
				"	void foo(){ super.foo(); }\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. WARNING in Y.java (at line 1)\n" + 
			"	public class Y extends X {\n" + 
			"	             ^\n" + 
			"The constructor X() is deprecated\n" + 
			"----------\n" + 
			"2. WARNING in Y.java (at line 2)\n" + 
			"	void foo(){ super.foo(); }\n" + 
			"	     ^^^^^\n" + 
			"The method foo() of type Y should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n" + 
			"3. WARNING in Y.java (at line 2)\n" + 
			"	void foo(){ super.foo(); }\n" + 
			"	            ^^^^^^^^^^^\n" + 
			"The method foo() from the type X is deprecated\n" + 
			"----------\n" + 
			"4. ERROR in Y.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }  		
    // check missing @Deprecated detection
    public void test121() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"/** @deprecated */\n" + 
				"public class X {\n" + 
				"	/** @deprecated */\n" + 
				"	public static class Y {\n" + 
				"	}\n" + 
				"	/** @deprecated */\n" + 
				"	int i;\n" + 
				"	/** @deprecated */\n" + 
				"	public void flag() {}\n" + 
				"	void doNotFlag() {}\n" + 
				"  Zork z;\n" +
				"} \n",
            },
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public class X {\n" + 
			"	             ^\n" + 
			"The deprecated type X should be annotated with @Deprecated\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public static class Y {\n" + 
			"	                    ^\n" + 
			"The deprecated type X.Y should be annotated with @Deprecated\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 7)\n" + 
			"	int i;\n" + 
			"	    ^\n" + 
			"The deprecated field X.i should be annotated with @Deprecated\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 9)\n" + 
			"	public void flag() {}\n" + 
			"	            ^^^^^^\n" + 
			"The deprecated method flag() of type X should be annotated with @Deprecated\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88446
    public void test122() {
        this.runConformTest(
            new String[] {
                "X.java",
                "import java.lang.annotation.Annotation;\n" +
                "import java.lang.reflect.Method;\n" +
                "import java.lang.annotation.ElementType;\n" +
                "import java.lang.annotation.Retention;\n" +
                "import java.lang.annotation.RetentionPolicy;\n" +
                "import java.lang.annotation.Target;\n" +
                "class GenericWithInnerAnnotation<T> {\n" +
                "    @Retention(RetentionPolicy.RUNTIME)\n" +
                "    @Target(ElementType.METHOD)\n" +
                "    public @interface MyAnnotation {\n" +
                "    }\n" +
                "}\n" +
                "public class X extends GenericWithInnerAnnotation<Integer> {\n" +
                "    @MyAnnotation\n" +
                "    public void aMethod() {\n" +
                "    }\n" +
                "    \n" +
                "    public static void main(String[] args) {\n" +
                "       try {\n" +
                "           Method method = X.class.getDeclaredMethod(\"aMethod\", new Class[]{});\n" +
                "           System.out.print(method.getName());\n" +
                "           Annotation[] annotations = method.getAnnotations();\n" +
                "           System.out.println(annotations.length);\n" +
                "       } catch(NoSuchMethodException e) {\n" +
                "       }\n" +
                "    }\n" +
                "}",
            },
            "aMethod1");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90110
    public void test123() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"class SuperX {\n" + 
				"\n" + 
				"    static void notOverridden() {\n" + 
				"        return;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"public class X extends SuperX {\n" + 
				"\n" + 
				"    static void notOverridden() {\n" + 
				"        return;\n" + 
				"    }\n" + 
				"  Zork z;\n" +
				"} \n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90110 - variation
    public void test124() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"class SuperX {\n" + 
				"\n" + 
				"    void notOverridden() {\n" + 
				"        return;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"public class X extends SuperX {\n" + 
				"\n" + 
				"    void notOverridden() {\n" + 
				"        return;\n" + 
				"    }\n" + 
				"  Zork z;\n" +
				"} \n",
            },
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	void notOverridden() {\n" + 
			"	     ^^^^^^^^^^^^^^^\n" + 
			"The method notOverridden() of type X should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 13)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }	
    public void test125() {
        this.runConformTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.*;\n" + 
				"\n" + 
				"public class X implements Ann {\n" + 
				"	\n" + 
				"	Ann ann = new X();\n" + 
				"	public Class<? extends Annotation>  annotationType() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"@interface Ann {}\n" + 
				"\n",
            },
			"");
    }		
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=90484 - check no missing @Override warning
    public void test126() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public interface X {\n" + 
				"   Zork z;\n" +
				"   X clone();\n" + 
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }		    
    // check @SuppressWarning support
    public void test127() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@Deprecated\n" + 
                "public class X {\n" + 
                "   void foo(){}\n" +
                "}\n",
                "Y.java",
                "public class Y extends X {\n" + 
                "  @SuppressWarnings(\"all\")\n" +
                "   void foo(){ super.foo(); }\n" +
                "   Zork z;\n" +
                "}\n",
            },
			"----------\n" + 
			"1. WARNING in Y.java (at line 1)\n" + 
			"	public class Y extends X {\n" + 
			"	             ^\n" + 
			"The constructor X() is deprecated\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 4)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }       
    // check @SuppressWarning support
    public void test128() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "import java.util.List;\n" + 
                "\n" + 
                "public class X {\n" + 
                "    void foo(List list) {\n" + 
                "        List<String> ls1 = list;\n" + 
                "    }\n" + 
                "    @SuppressWarnings(\"unchecked\")\n" + 
                "    void bar(List list) {\n" + 
                "        List<String> ls2 = list;\n" + 
                "    }\n" + 
                "   Zork z;\n" +
                "}\n",
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 4)\n" + 
    		"	void foo(List list) {\n" + 
    		"	         ^^^^\n" + 
    		"Type safety: List is a raw type. References to generic type List<E> should be parameterized\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 5)\n" + 
    		"	List<String> ls1 = list;\n" + 
    		"	                   ^^^^\n" + 
    		"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
    		"----------\n" + 
    		"3. ERROR in X.java (at line 11)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n");
    }       
    // check @SuppressWarning support
    public void test129() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.util.*;\n" + 
				"@SuppressWarnings(\"unchecked\")\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		Map<String, String>[] map = new HashMap[10];\n" + 
				"	}\n" + 
                "   Zork z;\n" +				
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }
    // check @SuppressWarning support
    public void test130() {
    	Map customOptions = new Hashtable();
		String[] warnings = CompilerOptions.warningOptionNames();
		for (int i = 0, ceil = warnings.length; i < ceil; i++) {
			customOptions.put(warnings[i], CompilerOptions.WARNING);
		}
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" + 
				"  public static void main(String[] args) {\n" + 
				"  }\n" + 
				"}\n",
            },
    		"----------\n" + 
    		"1. WARNING in X.java (at line 1)\n" + 
    		"	public class X {\n" + 
    		"	             ^\n" + 
    		"Javadoc: Missing comment for public declaration\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 2)\n" + 
    		"	public static void main(String[] args) {\n" + 
    		"	                   ^^^^^^^^^^^^^^^^^^^\n" + 
    		"Javadoc: Missing comment for public declaration\n" + 
    		"----------\n" + 
    		"3. WARNING in X.java (at line 2)\n" + 
    		"	public static void main(String[] args) {\n" + 
    		"  }\n" + 
    		"	                                       ^^^^^\n" + 
    		"Empty block should be documented\n" + 
    		"----------\n",
			null, true, customOptions);
    }       
    // check @SuppressWarning support
    public void test131() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings(\"all\")\n" + 
                "public class X {\n" + 
		        "  public static void main(String[] args) {\n" + 
		        "    Zork z;\n" + 
		        "  }\n" + 
		        "}\n",
            },
            "----------\n" + 
            "1. ERROR in X.java (at line 4)\n" + 
            "	Zork z;\n" + 
            "	^^^^\n" + 
            "Zork cannot be resolved to a type\n" + 
            "----------\n");
    }       
    // check @SuppressWarning support
    public void test132() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" + 
    			"import java.util.List;\n" + 
    			"import java.util.Vector;\n" + 
    			"\n" + 
    			"public class X {\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		W.deprecated();\n" + 
    			"		List<X> l = new Vector();\n" + 
    			"		l.size();\n" + 
    			"		try {\n" + 
    			"			// do nothing\n" + 
    			"		} finally {\n" + 
    			"			throw new Error();\n" + 
    			"		}\n" + 
    			"		// Zork z;\n" + 
    			"	}\n" + 
    			"\n" + 
    			"	class S implements Serializable {\n" + 
    			"		String dummy;\n" + 
    			"	}\n" + 
    			"}",
    			"W.java",
    			"public class W {\n" + 
    			"	// @deprecated\n" + 
    			"	@Deprecated\n" + 
    			"	static void deprecated() {\n" + 
    			"		// do nothing\n" + 
    			"	}\n" + 
    			"}\n"
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 7)\n" + 
    		"	W.deprecated();\n" + 
    		"	^^^^^^^^^^^^^^\n" + 
    		"The method deprecated() from the type W is deprecated\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 8)\n" + 
    		"	List<X> l = new Vector();\n" + 
    		"	            ^^^^^^^^^^^^\n" + 
    		"Type safety: The expression of type Vector needs unchecked conversion to conform to List<X>\n" + 
    		"----------\n" + 
    		"3. WARNING in X.java (at line 8)\n" + 
    		"	List<X> l = new Vector();\n" + 
    		"	                ^^^^^^\n" + 
    		"Type safety: Vector is a raw type. References to generic type Vector<E> should be parameterized\n" + 
    		"----------\n" + 
    		"4. WARNING in X.java (at line 12)\n" + 
    		"	} finally {\n" + 
    		"			throw new Error();\n" + 
    		"		}\n" + 
    		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"finally block does not complete normally\n" + 
    		"----------\n" + 
    		"5. WARNING in X.java (at line 18)\n" + 
    		"	class S implements Serializable {\n" + 
    		"	      ^\n" + 
    		"The serializable class S does not declare a static final serialVersionUID field of type long\n" + 
    		"----------\n");
    }
    // check @SuppressWarning support
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=89436
    public void test133() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" + 
    			"import java.util.List;\n" + 
    			"import java.util.Vector;\n" + 
    			"\n" + 
    			"@SuppressWarnings( { \"deprecation\",//$NON-NLS-1$\n" + 
    			"		\"finally\",//$NON-NLS-1$\n" + 
    			"		\"serial\",//$NON-NLS-1$\n" + 
    			"		\"unchecked\"//$NON-NLS-1$\n" + 
    			"})\n" + 
    			"public class X {\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		W.deprecated();\n" + 
    			"		List<X> l = new Vector();\n" + 
    			"		l.size();\n" + 
    			"		try {\n" + 
    			"			// do nothing\n" + 
    			"		} finally {\n" + 
    			"			throw new Error();\n" + 
    			"		}\n" + 
    			"		Zork z;\n" + 
    			"	}\n" + 
    			"\n" + 
    			"	class S implements Serializable {\n" + 
    			"		String dummy;\n" + 
    			"	}\n" + 
    			"}",
    			"W.java",
    			"public class W {\n" + 
    			"	// @deprecated\n" + 
    			"	@Deprecated\n" + 
    			"	static void deprecated() {\n" + 
    			"		// do nothing\n" + 
    			"	}\n" + 
    			"}\n"
            },
    		"----------\n" + 
    		"1. ERROR in X.java (at line 20)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n");
    }           
    // check @SuppressWarning support
    public void test134() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" + 
    			"import java.util.List;\n" + 
    			"import java.util.Vector;\n" + 
    			"\n" + 
    			"public class X {\n" + 
    			"	@SuppressWarnings( { \"deprecation\",//$NON-NLS-1$\n" + 
    			"			\"finally\",//$NON-NLS-1$\n" + 
    			"			\"unchecked\"//$NON-NLS-1$\n" + 
    			"	})\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		W.deprecated();\n" + 
    			"		List<X> l = new Vector();\n" + 
    			"		l.size();\n" + 
    			"		try {\n" + 
    			"			// do nothing\n" + 
    			"		} finally {\n" + 
    			"			throw new Error();\n" + 
    			"		}\n" + 
    			"		Zork z;\n" + 
    			"	}\n" + 
    			"\n" + 
    			"	@SuppressWarnings(\"unchecked\"//$NON-NLS-1$\n" + 
    			"	)\n" + 
    			"	List<X> l = new Vector();\n" + 
    			"\n" + 
    			"	@SuppressWarnings(\"serial\"//$NON-NLS-1$\n" + 
    			"	)\n" + 
    			"	class S implements Serializable {\n" + 
    			"		String dummy;\n" + 
    			"	}\n" + 
    			"}",
    			"W.java",
    			"public class W {\n" + 
    			"	// @deprecated\n" + 
    			"	@Deprecated\n" + 
    			"	static void deprecated() {\n" + 
    			"		// do nothing\n" + 
    			"	}\n" + 
    			"}\n"
            },
    		"----------\n" + 
    		"1. ERROR in X.java (at line 19)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n");
    }
    // check @SuppressWarning support
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=69505 -- NOT READY YET: "all" only so far, no file support -- 
    //                                                        hence no import support
    public void _test135() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"@SuppressWarnings(\"all\")//$NON-NLS-1$\n" + 
    			"import java.util.List;\n" + 
    			"\n" + 
    			"public class X {\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		if (false) {\n" + 
    			"			;\n" + 
    			"		} else {\n" + 
    			"		}\n" + 
    			"		Zork z;\n" + 
    			"	}\n" + 
    			"}"
            },
    		"----------\n" + 
    		"1. ERROR in X.java (at line 11)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n");
    }  
    // check @SuppressWarning support
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=71968
    public void test136() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X {\n" + 
    			"	@SuppressWarnings(\"unused\"//$NON-NLS-1$\n" + 
    			"	)\n" + 
    			"	private static final String marker = \"never used mark\"; //$NON-NLS-1$\n" + 
    			"\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		Zork z;\n" + 
    			"	}\n" + 
    			"}"
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }  
    // check @SuppressWarning support
    public void test137() {
    	Map customOptions = new Hashtable();
		String[] warnings = CompilerOptions.warningOptionNames();
		for (int i = 0, ceil = warnings.length; i < ceil; i++) {
			customOptions.put(warnings[i], CompilerOptions.WARNING);
		}
		customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" + 
    			"import java.util.List;\n" + 
    			"import java.util.Vector;\n" + 
    			"\n" + 
    			"@SuppressWarnings(\"all\")//$NON-NLS-1$\n" + 
    			"public class X {\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		W.deprecated();\n" + 
    			"		List<X> l = new Vector();\n" + 
    			"		l.size();\n" + 
    			"		try {\n" + 
    			"			// do nothing\n" + 
    			"		} finally {\n" + 
    			"			throw new Error();\n" + 
    			"		}\n" + 
    			"		Zork z;\n" + 
    			"	}\n" + 
    			"\n" + 
    			"	class S implements Serializable {\n" + 
    			"		String dummy;\n" + 
    			"	}\n" + 
    			"}",
    			"W.java",
    			"public class W {\n" + 
    			"	// @deprecated\n" + 
    			"	@Deprecated\n" + 
    			"	static void deprecated() {\n" + 
    			"		// do nothing\n" + 
    			"	}\n" + 
    			"}\n"
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 6)\n" + 
    		"	public class X {\n" + 
    		"	             ^\n" + 
    		"Javadoc: Missing comment for public declaration\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 7)\n" + 
    		"	public static void main(String[] args) {\n" + 
    		"	                   ^^^^^^^^^^^^^^^^^^^\n" + 
    		"Javadoc: Missing comment for public declaration\n" + 
    		"----------\n" + 
    		"3. WARNING in X.java (at line 8)\n" + 
    		"	W.deprecated();\n" + 
    		"	^^^^^^^^^^^^^^\n" + 
    		"The method deprecated() from the type W is deprecated\n" + 
    		"----------\n" + 
    		"4. WARNING in X.java (at line 9)\n" + 
    		"	List<X> l = new Vector();\n" + 
    		"	            ^^^^^^^^^^^^\n" + 
    		"Type safety: The expression of type Vector needs unchecked conversion to conform to List<X>\n" + 
    		"----------\n" + 
    		"5. WARNING in X.java (at line 9)\n" + 
    		"	List<X> l = new Vector();\n" + 
    		"	                ^^^^^^\n" + 
    		"Type safety: Vector is a raw type. References to generic type Vector<E> should be parameterized\n" + 
    		"----------\n" + 
    		"6. ERROR in X.java (at line 16)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"7. WARNING in X.java (at line 19)\n" + 
    		"	class S implements Serializable {\n" + 
    		"	      ^\n" + 
    		"The serializable class S does not declare a static final serialVersionUID field of type long\n" + 
    		"----------\n" + 
    		"----------\n" + 
    		"1. WARNING in W.java (at line 1)\n" + 
    		"	public class W {\n" + 
    		"	             ^\n" + 
    		"Javadoc: Missing comment for public declaration\n" + 
    		"----------\n",
			null, true, customOptions);
    }      
    // check @SuppressWarning support
    public void test138() {
    	Map customOptions = new Hashtable();
    	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
        this.runNegativeTest(

            new String[] {
                "X.java",
    			"@SuppressWarnings(\"zork\")//$NON-NLS-1$\n" + 
    			"public class X {\n" + 
    			"	Zork z;\n" + 
    			"}\n"
            },
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	@SuppressWarnings(\"zork\")//$NON-NLS-1$\n" + 
			"	                  ^^^^^^\n" + 
			"Unhandled warning token zork\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null, true, customOptions);
    }      
    // check @SuppressWarning support
    public void test139() {
    	Map customOptions = new Hashtable();
    	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
        this.runNegativeTest(

            new String[] {
                "X.java",
    			"@SuppressWarnings({\"zork\", \"warningToken\"})//$NON-NLS-1$//$NON-NLS-2$\n" + 
    			"public class X {\n" + 
    			"	Zork z;\n" + 
    			"}\n"
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 1)\n" + 
    		"	@SuppressWarnings({\"zork\", \"warningToken\"})//$NON-NLS-1$//$NON-NLS-2$\n" + 
    		"	                   ^^^^^^\n" + 
    		"Unhandled warning token zork\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 1)\n" + 
    		"	@SuppressWarnings({\"zork\", \"warningToken\"})//$NON-NLS-1$//$NON-NLS-2$\n" + 
    		"	                           ^^^^^^^^^^^^^^\n" + 
    		"Unhandled warning token warningToken\n" + 
    		"----------\n" + 
    		"3. ERROR in X.java (at line 3)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n",
			null, true, customOptions);
    }          
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90111 - variation
    public void test140() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" + 
				"  static void foo(){}\n" + 
				"}\n" + 
				"class Bar extends X {\n" + 
				"  @Override\n" + 
				"  static void foo(){}	\n" + 
				"}\n" + 
				"\n"
            },
			"");
    }              
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94867
    public void test141() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface X1 {\n" + 
				"	Class<? extends Throwable>[] expected1() default {};\n" + 
				"	Class<? super Throwable>[] expected2() default {};\n" + 
				"	Class<?>[] expected3() default {};\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@X1(expected1=Throwable.class, expected2={})\n" + 
				"	public static void main(String[] args) {\n" + 
				"		\n" + 
				"	}\n" + 
				"	void foo() {\n" + 
				"		Class<? extends Throwable>[] c1 = {};\n" + 
				"		Class<? super Throwable>[] c2 = {};\n" + 
				"		Class<?>[] c3 = {};\n" + 
				"	}\n" + 
				"}\n"
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	Class<? extends Throwable>[] c1 = {};\n" + 
			"	                                  ^^\n" + 
			"Cannot create a generic array of Class<? extends Throwable>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	Class<? super Throwable>[] c2 = {};\n" + 
			"	                                ^^\n" + 
			"Cannot create a generic array of Class<? super Throwable>\n" + 
			"----------\n");
    }        
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94308
    public void test142() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(\"deprecation\")\n" + 
				"public class X extends p.OldStuff {\n" + 
				"	/**\n" + 
				"	 * @see p.OldStuff#foo()\n" + 
				"	 */\n" + 
				"	@Override\n" + 
				"	public void foo() {\n" + 
				"		super.foo();\n" + 
				"	}\n" + 
				"}\n",
                "p/OldStuff.java",
                "package p;\n" +
                "@Deprecated\n" +
				"public class OldStuff {\n" + 
				"	public void foo() {\n" + 
				"	}	\n" + 
				"  Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in p\\OldStuff.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }        
    public void _test143() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X extends p.OldStuff {\n" + 
				"	@SuppressWarnings(\"all\")\n" + 
				"	public void foo() {\n" + 
				"		super.foo();\n" + 
				"	}\n" + 
				"}\n",
                "p/OldStuff.java",
                "package p;\n" +
                "@Deprecated\n" +
				"public class OldStuff {\n" + 
				"	public void foo() {\n" + 
				"	}	\n" + 
				"  Zork z;\n" +
				"}\n",
            },
			"----------\n" + 
			"1. ERR OR in p\\OldStuff.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
    }         
    public void test144() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"	Zork z;\n" +
				"	@SuppressWarnings(\"all\")  \n" + 
				"	public static class EverythingWrong {\n" + 
				"		private EverythingWrong() {}\n" + 
				"		@BeforeClass public void notStaticBC() {}\n" + 
				"		@BeforeClass static void notPublicBC() {}\n" + 
				"		@BeforeClass public static int nonVoidBC() { return 0; }\n" + 
				"		@BeforeClass public static void argumentsBC(int i) {}\n" + 
				"		@BeforeClass public static void fineBC() {}\n" + 
				"		@AfterClass public void notStaticAC() {}\n" + 
				"		@AfterClass static void notPublicAC() {}\n" + 
				"		@AfterClass public static int nonVoidAC() { return 0; }\n" + 
				"		@AfterClass public static void argumentsAC(int i) {}\n" + 
				"		@AfterClass public static void fineAC() {}\n" + 
				"		@After public static void staticA() {}\n" + 
				"		@After void notPublicA() {}\n" + 
				"		@After public int nonVoidA() { return 0; }\n" + 
				"		@After public void argumentsA(int i) {}\n" + 
				"		@After public void fineA() {}\n" + 
				"		@Before public static void staticB() {}\n" + 
				"		@Before void notPublicB() {}\n" + 
				"		@Before public int nonVoidB() { return 0; }\n" + 
				"		@Before public void argumentsB(int i) {}\n" + 
				"		@Before public void fineB() {}\n" + 
				"		@Test public static void staticT() {}\n" + 
				"		@Test void notPublicT() {}\n" + 
				"		@Test public int nonVoidT() { return 0; }\n" + 
				"		@Test public void argumentsT(int i) {}\n" + 
				"		@Test public void fineT() {}\n" + 
				"	}\n" + 
				"	@Test public void testFailures() throws Exception {\n" + 
				"		List<Exception> problems= new TestIntrospector(EverythingWrong.class).validateTestMethods();\n" + 
				"		int errorCount= 1 + 4 * 5; // missing constructor plus four invalid methods for each annotation */\n" + 
				"		assertEquals(errorCount, problems.size());\n" + 
				"	}\n" + 
				"	public static junit.framework.Test suite() {\n" + 
				"		return null; // new JUnit4TestAdapter(TestMethodTest.class);\n" + 
				"	}\n" + 
				"	void assertEquals(int i, int j) {\n" + 
				"	}\n" + 
				"}\n" + 
				"@interface BeforeClass {}\n" + 
				"@interface AfterClass {}\n" + 
				"@interface Test {}\n" + 
				"@interface After {}\n" + 
				"@interface Before {}\n" + 
				"class TestIntrospector {\n" + 
				"	TestIntrospector(Class c) {}\n" + 
				"	List validateTestMethods() { return null; }\n" + 
				"}\n",
            },
            "----------\n" + 
    		"1. ERROR in X.java (at line 3)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 34)\n" + 
    		"	List<Exception> problems= new TestIntrospector(EverythingWrong.class).validateTestMethods();\n" + 
    		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Type safety: The expression of type List needs unchecked conversion to conform to List<Exception>\n" + 
    		"----------\n" + 
    		"3. ERROR in X.java (at line 38)\n" + 
    		"	public static junit.framework.Test suite() {\n" + 
    		"	              ^^^^^\n" + 
    		"junit cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"4. WARNING in X.java (at line 50)\n" + 
    		"	TestIntrospector(Class c) {}\n" + 
    		"	                 ^^^^^\n" + 
    		"Type safety: Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n" + 
    		"5. WARNING in X.java (at line 51)\n" + 
    		"	List validateTestMethods() { return null; }\n" + 
    		"	^^^^\n" + 
    		"Type safety: List is a raw type. References to generic type List<E> should be parameterized\n" + 
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=89937
    public void test145() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" + 
				"  int foo();\n" + 
				"  int bar();\n" + 
				"}\n" + 
				"\n" + 
				"public class X {\n" + 
				"  static final int yyy = 0;\n" + 
				"  @Annot(foo=zzz, bar = yyy)\n" + 
				"  static final int zzz = 0;\n" + 
				"}\n" + 
				"\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	@Annot(foo=zzz, bar = yyy)\n" + 
			"	           ^^^\n" + 
			"Cannot reference a field before it is defined\n" + 
			"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96631
    public void test146() {
        this.runConformTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(value={})\n" + 
				"public class X {\n" + 
				"}\n",
            },
			"");
    }
    
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96974
    public void test147() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"nls\"})\n" +
				"public class X<T> {\n" +
				"	 String test= \"\";\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=97466
    public void test148() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	private static void foo() {\n" +
				"		 @interface Bar {\n" +
				"			public String bar = \"BUG\";\n" +
				"		}\n" +
				"	}\n" +
				"}",
            },
            "----------\n" + 
    		"1. ERROR in X.java (at line 3)\n" + 
    		"	@interface Bar {\n" + 
    		"	           ^^^\n" + 
    		"The member annotation Bar can only be defined inside a top-level class or interface\n" + 
    		"----------\n");
    }
    
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96991
    public void test149() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" + 
				"	void bar() {\n" + 
				"		@Annot(foo = zzz)\n" + 
				"		final int zzz = 0;\n" + 
				"\n" + 
				"		@Annot(foo = kkk)\n" + 
				"		int kkk = 1;\n" + 
				"\n" + 
				"	}\n" + 
				"	@Annot(foo = fff)\n" + 
				"	final int fff = 0;\n" + 
				"	\n" + 
				"	@Annot(foo = Member.ttt)\n" + 
				"	static class Member {\n" + 
				"		final static int ttt = 2;\n" + 
				"	}\n" + 
				"}\n" + 
				"@interface Annot {\n" + 
				"	int foo();\n" + 
				"}\n",
            },
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	@Annot(foo = kkk)\n" + 
			"	             ^^^\n" + 
			"The value for annotation attribute Annot.foo must be a constant expression\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	@Annot(foo = fff)\n" + 
			"	             ^^^\n" + 
			"Cannot reference a field before it is defined\n" + 
			"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=98091
    public void test150() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(\"assertIdentifier\")\n" + 
				"class X {}",
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 1)\n" + 
    		"	@SuppressWarnings(\"assertIdentifier\")\n" + 
    		"	                  ^^^^^^^^^^^^^^^^^^\n" + 
    		"Unhandled warning token assertIdentifier\n" + 
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test151() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"boxing\"})\n" +
				"public class X {\n" +
				"	 static void foo(int i) {}\n" +
				"	 public static void main(String[] args) {\n" +
				"		foo(new Integer(0));\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test152() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"boxing\"})\n" +
				"public class X {\n" +
				"	 static void foo(Integer i) {}\n" +
				"	 public static void main(String[] args) {\n" +
				"		foo(0);\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test153() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "enum E { A, B, C }\n" +
				"public class X {\n" +
				"    @SuppressWarnings({\"incomplete-switch\"})\n" +
				"	 public static void main(String[] args) {\n" +
				"		for (E e : E.values()) {\n" +
				"			switch(e) {\n" +
				"				case A :\n" +
				"					System.out.println(e);\n" +
				"				break;\n" +
				"			}\n" +
				"		}\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test154() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	 static int i;\n" +
				"    @SuppressWarnings({\"hiding\"})\n" +
				"	 public static void main(String[] args) {\n" +
				"		for (int i = 0, max = args.length; i < max; i++) {\n" +
				"			System.out.println(args[i]);\n" +
				"		}\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test155() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"hiding\"})\n" +
	   			"public class X {	\n"+
    			"	{ int x = \n"+
    			"		new Object() { 	\n"+
    			"			int foo() {	\n"+
    			"				int x = 0;\n" +
    			"				return x;	\n"+
    			"			}	\n"+
    			"		}.foo();	\n"+
    			"	}	\n"+
    			"}\n",
           },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test156() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
	   			"class T {}\n" +
				"@SuppressWarnings({\"hiding\"})\n" +
	   			"public class X<T> {\n"+
    			"}\n",
           },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test157() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X {\n" + 
				"   @SuppressWarnings({\"hiding\"})\n" +
    			"	public static void main(String[] args) {\n" + 
    			"		try {\n" + 
    			"			throw new BX();\n" + 
    			"		} catch(BX e) {\n" + 
    			"		} catch(AX e) {\n" + 
    			"		}\n" + 
    			"	}\n" + 
    			"} \n" + 
				"@SuppressWarnings({\"serial\"})\n" +
	   			"class AX extends Exception {}\n" + 
				"@SuppressWarnings({\"serial\"})\n" +
    			"class BX extends AX {}\n"		
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test158() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X {\n" + 
				"   @SuppressWarnings({\"finally\"})\n" +
    			"	public static void main(String[] args) {\n" + 
    			"		try {\n" + 
    			"			throw new AX();\n" + 
    			"		} finally {\n" +
    			"			return;\n" +
    			"		}\n" + 
    			"	}\n" + 
    			"} \n" + 
				"@SuppressWarnings({\"serial\"})\n" +
	   			"class AX extends Exception {}" 
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test159() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"static-access\"})\n" +
	   			"public class X extends XZ {\n" + 
    			"	\n" + 
    			"	void foo() {\n" + 
    			"		int j = X.S;\n" + 
    			"		int k = super.S;\n" + 
    			"		int l = XZ.S;\n" + 
    			"		int m = XY.S;\n" + 
    			"		\n" + 
    			"		bar();\n" + 
    			"		X.bar();\n" + 
    			"		XY.bar();\n" + 
    			"		XZ.bar();\n" + 
    			"	}\n" + 
    			"}\n" + 
    			"class XY {\n" + 
    			"	static int S = 10;\n" + 
    			"	static void bar(){}\n" + 
    			"}\n" + 
    			"class XZ extends XY {\n" + 
    			"}"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test160() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(\"static-access\")\n" +
	   			"public class X {\n" + 
    			"	void foo() {\n" + 
    			"		int m = new XY().S;\n" + 
    			"	}\n" + 
    			"}\n" + 
    			"class XY {\n" + 
    			"	static int S = 10;\n" + 
    			"}"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test161() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings(\"unqualified-field-access\")\n" +
	   			"public class X {\n" + 
	   			"	int i;\n" +
    			"	int foo() {\n" + 
    			"		return i;\n" + 
    			"	}\n" + 
    			"}" 
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test162() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings(\"unchecked\")\n" +
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        ax.p = new AX<String>();\n" + 
				"        ax.q = new AX<String>();\n" + 
				"        ax.r = new AX<Object>();\n" + 
				"        System.out.println(ax2);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"    AX<Object> q;\n" + 
				"    AX<String> r;\n" + 
				"    BX<String> s;\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> {\n" + 
				"}\n",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test163() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "import java.io.*;\n" +
                "@SuppressWarnings(\"unused\")\n" +
				"public class X<T> {\n" + 
				"    \n" + 
				"    public void foo(int i) throws java.io.IOException {\n" + 
				"       int j = 0;\n" +
				"		class C {\n" +
				"			private void bar() {}\n" + 			
				"		}\n" +
				"    }\n" + 
				"}" 
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 1)\n" + 
    		"	import java.io.*;\n" + 
    		"	       ^^^^^^^\n" + 
    		"The import java.io is never used\n" + 
    		"----------\n",
			null,
			true,
			options
		);
    }
    
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test164() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings({\"synthetic-access\", \"unused\"})\n" +
				"public class X {\n" + 
				"    private int i;\n" +
				"	 private void bar() {}\n" +
				"    public void foo() {\n" + 
				"       class C {\n" +
				"			private void bar() {\n" +
				"				System.out.println(i);\n" +
				"				i = 0;\n" +
				"				bar();\n" +
				"			}\n" +
				"		};\n" +
				"		new C().bar();\n" +
				"    }\n" + 
				"}" 
            },
            "",
			null,
			true,
			options
		);
    }
    
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test165() {
		Map options = this.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDeprecatedAnnotation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
	    this.runNegativeTest(
            new String[] {
                "X.java",
				"/**\n" +
				" * @see Y\n" +
				" */\n" +
                "@SuppressWarnings(\"deprecation\")\n" +
				"public class X extends Y {\n" + 
				"	 /**\n" +
				"	  * @see Y#foo()\n" +
				"	  * @see Y#j\n" +
				"	  */\n" +
				"    public void foo() {\n" + 
				"		super.foo();\n" +
				"    }\n" + 
				"}",
				"Y.java",
				"/**\n" +
				" * @deprecated\n" +
				" */\n" +
				"public class Y {\n" +
				"	/**\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"	/**\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public int j;\n" +
				"}"
            },
            "",
			null,
			true,
			options
		);
    }

	// check array handling of singleton 
	public void test166() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Inherited;\n" +
				"\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Inherited()\n" +
				"@interface ParameterAnnotation {\n" +
				"	String value() default \"Default\";\n" +
				"}\n"+
				"@interface ClassAnnotation {\n" +
				"	String value() default \"Default\";\n" +
				"}\n" +
				"\n" +
				"enum EnumClass{\n" +
				"	Value1, Value2, Value3\n" +
				"}\n" +
				"\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Inherited()\n" +
				"@interface ValueAnnotation {\n" +
				"	String value() default \"Default\";\n" +
				"	boolean booleanValue() default true;\n" +
				"	char charValue() default \'q\';\n" +
				"	byte byteValue() default 123;\n" +
				"	short shortValue() default 12345;\n" +
				"	int intValue() default 1234567890;\n" +
				"	float floatValue() default 12345.6789f;\n" +
				"	double doubleValue() default 12345.6789;\n" +
				"	long longValue() default 1234567890123456789l;\n" +
				"	String stringValue() default \"stringValue\";\n" +
				"	EnumClass enumValue() default EnumClass.Value1;\n" +
				"	Class classValue() default EnumClass.class;\n" +
				"	ClassAnnotation annotationValue() default @ClassAnnotation();\n" +
				"	boolean[] booleanArrayValue() default {true, false};\n" +
				"	char[] charArrayValue() default {\'q\', \'m\'};\n" +
				"	byte[] byteArrayValue() default {123, -123};\n" +
				"	short[] shortArrayValue() default {12345, -12345};\n" +
				"	int[] intArrayValue() default {1234567890, -1234567890};\n" +
				"	float[] floatArrayValue() default {12345.6789f, -12345.6789f};\n" +
				"	double[] doubleArrayValue() default {12345.6789, -12345.6789};\n" +
				"	long[] longArrayValue() default {1234567890123456789l, -1234567890123456789l};\n" +
				"	String[] stringArrayValue() default {\"stringValue\", \"valueString\"};\n" +
				"	EnumClass[] enumArrayValue() default {EnumClass.Value1, EnumClass.Value2};\n" +
				"	Class[] classArrayValue() default {X.class, EnumClass.class};\n" +
				"	ClassAnnotation[] annotationArrayValue() default {@ClassAnnotation(), @ClassAnnotation()};\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@ValueAnnotation(\n" +
				"		value=\"ValueAnnotation\",\n" +
				"		booleanValue=true,\n" +
				"		charValue=\'m\',\n" +
				"		byteValue=-123,\n" +
				"		shortValue=-12345,\n" +
				"		intValue=-1234567890,\n" +
				"		floatValue=-12345.6789f,\n" +
				"		doubleValue=-12345.6789,\n" +
				"		longValue=-1234567890123456789l,\n" +
				"		stringValue=\"valueString\",\n" +
				"		enumValue=EnumClass.Value3,\n" +
				"		classValue=X.class,\n" +
				"		annotationValue=@ClassAnnotation(value=\"ClassAnnotation\"),\n" +
				"		booleanArrayValue={\n" +
				"			false,\n" +
				"			true\n" +
				"		},\n" +
				"		charArrayValue={\n" +
				"			\'m\',\n" +
				"			\'q\'\n" +
				"		},\n" +
				"		byteArrayValue={\n" +
				"			-123,\n" +
				"			123\n" +
				"		},\n" +
				"		shortArrayValue={\n" +
				"			-12345,\n" +
				"			12345\n" +
				"		},\n" +
				"		intArrayValue={\n" +
				"			-1234567890,\n" +
				"			1234567890\n" +
				"		},\n" +
				"		floatArrayValue={\n" +
				"			-12345.6789f,\n" +
				"			12345.6789f\n" +
				"		},\n" +
				"		doubleArrayValue={\n" +
				"			-12345.6789,\n" +
				"			12345.6789\n" +
				"		},\n" +
				"		longArrayValue={\n" +
				"			-1234567890123456789l,\n" +
				"			1234567890123456789l\n" +
				"		},\n" +
				"		stringArrayValue={\n" +
				"			\"valueString\",\n" +
				"			\"stringValue\"\n" +
				"		},\n" +
				"		enumArrayValue={\n" +
				"			EnumClass.Value2,\n" +
				"			EnumClass.Value1\n" +
				"		},\n" +
				"		classArrayValue={\n" +
				"			EnumClass.class,\n" +
				"			X.class\n" +
				"		},\n" +
				"		annotationArrayValue={\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation1\"),\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation2\")\n" +
				"		})\n" +
				"	public String field;\n" +
				"	@ValueAnnotation(\n" +
				"		value=\"ValueAnnotation\",\n" +
				"		booleanValue=true,\n" +
				"		charValue=\'m\',\n" +
				"		byteValue=-123,\n" +
				"		shortValue=-12345,\n" +
				"		intValue=-1234567890,\n" +
				"		floatValue=-12345.6789f,\n" +
				"		doubleValue=-12345.6789,\n" +
				"		longValue=-1234567890123456789l,\n" +
				"		stringValue=\"valueString\",\n" +
				"		enumValue=EnumClass.Value3,\n" +
				"		classValue=X.class,\n" +
				"		annotationValue=@ClassAnnotation(value=\"ClassAnnotation\"),\n" +
				"		booleanArrayValue={\n" +
				"			false,\n" +
				"			true\n" +
				"		},\n" +
				"		charArrayValue={\n" +
				"			\'m\',\n" +
				"			\'q\'\n" +
				"		},\n" +
				"		byteArrayValue={\n" +
				"			-123,\n" +
				"			123\n" +
				"		},\n" +
				"		shortArrayValue={\n" +
				"			-12345,\n" +
				"			12345\n" +
				"		},\n" +
				"		intArrayValue={\n" +
				"			-1234567890,\n" +
				"			1234567890\n" +
				"		},\n" +
				"		floatArrayValue={\n" +
				"			-12345.6789f,\n" +
				"			12345.6789f\n" +
				"		},\n" +
				"		doubleArrayValue={\n" +
				"			-12345.6789,\n" +
				"			12345.6789\n" +
				"		},\n" +
				"		longArrayValue={\n" +
				"			-1234567890123456789l,\n" +
				"			1234567890123456789l\n" +
				"		},\n" +
				"		stringArrayValue={\n" +
				"			\"valueString\",\n" +
				"			\"stringValue\"\n" +
				"		},\n" +
				"		enumArrayValue={\n" +
				"			EnumClass.Value2,\n" +
				"			EnumClass.Value1\n" +
				"		},\n" +
				"		classArrayValue={\n" +
				"			EnumClass.class,\n" +
				"			X.class\n" +
				"		},\n" +
				"		annotationArrayValue={\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation1\"),\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation2\")\n" +
				"		})\n" +
				"	public X(@ParameterAnnotation(value=\"ParameterAnnotation\") @Deprecated() String param1, @ParameterAnnotation(value=\"ParameterAnnotation\") String param2) {\n" +
				"	}\n" +
				"	@ValueAnnotation(\n" +
				"		value=\"ValueAnnotation\",\n" +
				"		booleanValue=true,\n" +
				"		charValue=\'m\',\n" +
				"		byteValue=-123,\n" +
				"		shortValue=-12345,\n" +
				"		intValue=-1234567890,\n" +
				"		floatValue=-12345.6789f,\n" +
				"		doubleValue=-12345.6789,\n" +
				"		longValue=-1234567890123456789l,\n" +
				"		stringValue=\"valueString\",\n" +
				"		enumValue=EnumClass.Value3,\n" +
				"		classValue=X.class,\n" +
				"		annotationValue=@ClassAnnotation(value=\"ClassAnnotation\"),\n" +
				"		booleanArrayValue={\n" +
				"			false,\n" +
				"			true\n" +
				"		},\n" +
				"		charArrayValue={\n" +
				"			\'m\',\n" +
				"			\'q\'\n" +
				"		},\n" +
				"		byteArrayValue={\n" +
				"			-123,\n" +
				"			123\n" +
				"		},\n" +
				"		shortArrayValue={\n" +
				"			-12345,\n" +
				"			12345\n" +
				"		},\n" +
				"		intArrayValue={\n" +
				"			-1234567890,\n" +
				"			1234567890\n" +
				"		},\n" +
				"		floatArrayValue={\n" +
				"			-12345.6789f,\n" +
				"			12345.6789f\n" +
				"		},\n" +
				"		doubleArrayValue={\n" +
				"			-12345.6789,\n" +
				"			12345.6789\n" +
				"		},\n" +
				"		longArrayValue={\n" +
				"			-1234567890123456789l,\n" +
				"			1234567890123456789l\n" +
				"		},\n" +
				"		stringArrayValue={\n" +
				"			\"valueString\",\n" +
				"			\"stringValue\"\n" +
				"		},\n" +
				"		enumArrayValue={\n" +
				"			EnumClass.Value2,\n" +
				"			EnumClass.Value1\n" +
				"		},\n" +
				"		classArrayValue={\n" +
				"			EnumClass.class,\n" +
				"			X.class\n" +
				"		},\n" +
				"		annotationArrayValue={\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation1\"),\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation2\")\n" +
				"		})\n" +
				"	public void method(@ParameterAnnotation(value=\"ParameterAnnotation\") @Deprecated() String param1, @ParameterAnnotation(value=\"ParameterAnnotation\") String param2){\n" +
				"	}\n" +
				"}"
			},
		"");
		
		try {
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			final byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			new ClassFileReader(classFileBytes, "X.java".toCharArray(), true);
			disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);			
		} catch (ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
	}    
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99469
    public void test167() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" + 
				"	public foo(@Deprecated() String s) {\n" + 
				"	}\n" + 
				"}\n",
           },
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public foo(@Deprecated() String s) {\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Return type for the method is missing\n" + 
		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=94759
    public void test168() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"interface I {\n" + 
				"	@Override I clone();\n" + 
				"	void foo();\n" + 
				"}\n" + 
				"\n" + 
				"interface J extends I {\n" + 
				"	@Override void foo();\n" + 
				"}\n",
           },
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	@Override I clone();\n" + 
		"	            ^^^^^^^\n" + 
		"The method clone() of type I must override a superclass method\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	@Override void foo();\n" + 
		"	               ^^^^^\n" + 
		"The method foo() of type J must override a superclass method\n" + 
		"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test169() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"@SuppressWarnings(\"serial\")\n" + 
    			"public class X extends Exception {\n" +
    			"	String s = \"Hello\"; \n" +
    			"}"
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 3)\n" + 
    		"	String s = \"Hello\"; \n" + 
    		"	           ^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test170() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
            new String[] {
                "X.java",
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" + 
    			"	String s = \"Hello\"; \n" +
    			"}"
            },
    		"",
			null, true, null, customOptions, null);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test171() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" + 
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" + 
    			"	String s2 = \"Hello2\"; \n" +
    			"}"
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 1)\n" + 
    		"	public class X extends Exception {\n" + 
    		"	             ^\n" + 
    		"The serializable class X does not declare a static final serialVersionUID field of type long\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 5)\n" + 
    		"	String s2 = \"Hello2\"; \n" + 
    		"	            ^^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test172() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"@SuppressWarnings(\"serial\")\n" + 
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" + 
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" + 
    			"	String s2 = \"Hello2\"; \n" +
    			"}"
            },
    		"----------\n" + 
    		"1. WARNING in X.java (at line 6)\n" + 
    		"	String s2 = \"Hello2\"; \n" + 
    		"	            ^^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test173() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"@interface Annot {\n" +
    			"    String value() default \"NONE\"; //$NON-NLS-1$\n" +
    			"}\n" +
    			"@Annot(\"serial\")\n" + 
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" + 
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" + 
    			"	String s2 = \"Hello2\"; \n" +
    			"}"
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 5)\n" + 
    		"	public class X extends Exception {\n" + 
    		"	             ^\n" + 
    		"The serializable class X does not declare a static final serialVersionUID field of type long\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 9)\n" + 
    		"	String s2 = \"Hello2\"; \n" + 
    		"	            ^^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test174() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = "@interface Annot {\n" +
    			"    int value() default 0;\n" +
    			"}\n" +
    			"@interface Annot2 {\n" +
    			"    String value();\n" +
    			"}\n" +
    			"@Annot(value=5)\n" + 
    			"public class X {\n" +
    			"   @Annot2(value=\"nls\")\n" + 
    			"	String s = null; \n" +
    			"   @SuppressWarnings(\"serial\")\n" + 
    			"	String s2 = \"Hello2\"; \n" +
    			"}";
		this.runNegativeTest(
            new String[] {
                "X.java",
    			source
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 12)\n" + 
    		"	String s2 = \"Hello2\"; \n" + 
    		"	            ^^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test175() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = "@interface Annot {\n" +
    			"    int value() default 0;\n" +
    			"}\n" +
    			"@interface Annot2 {\n" +
    			"    String value();\n" +
    			"}\n" +
    			"@Annot(value=5)\n" + 
    			"public class X {\n" +
    			"   @Annot2(value=\"nls\") String s = \"value\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" + 
    			"	String s2 = \"Hello2\"; \n" +
    			"}";
		this.runNegativeTest(
            new String[] {
                "X.java",
    			source
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 9)\n" + 
    		"	@Annot2(value=\"nls\") String s = \"value\"; \n" + 
    		"	                                ^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 11)\n" + 
    		"	String s2 = \"Hello2\"; \n" + 
    		"	            ^^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test176() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = "@interface Annot {\n" +
    			"    int value() default 0;\n" +
    			"}\n" +
    			"@interface Annot2 {\n" +
    			"    String value();\n" +
    			"}\n" +
    			"@Annot(value=5)\n" + 
    			"public class X {\n" +
    			"   @Annot2(value=\"nls\") String s = \"value\"; \n" +
    			"   @SuppressWarnings({\"serial\", \"nls\"})\n" + 
    			"	String s2 = \"Hello2\"; \n" +
    			"	@Annot(value=5) void foo() {}\n" + 
    			"}";
		this.runNegativeTest(
            new String[] {
                "X.java",
    			source
            },
            "----------\n" + 
    		"1. WARNING in X.java (at line 9)\n" + 
    		"	@Annot2(value=\"nls\") String s = \"value\"; \n" + 
    		"	                                ^^^^^^^\n" + 
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=108263
    public void test177() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public @interface X {\n" + 
				"  public static final Integer foo = B.zzz; \n" + 
				"  public static final int foo3 = B.zzz2; \n" + 
				"}\n" + 
				"class B {\n" + 
				"  public static final Integer zzz = new Integer(0);\n" + 
				"  public static final int zzz2 = 0;\n" + 
				"}\n",
           },
		"");
    }        
}
