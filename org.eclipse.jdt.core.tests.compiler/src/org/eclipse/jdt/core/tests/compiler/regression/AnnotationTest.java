/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann  - Contributions for
 *								bug 295551 - Add option to automatically promote all warnings to error
 *								bug 185682 - Increment/decrement operators mark local variables as read
 *								bug 366003 - CCE in ASTNode.resolveAnnotations(ASTNode.java:639)
 *								bug 384663 - Package Based Annotation Compilation Error in JDT 3.8/4.2 (works in 3.7.2)
 *								bug 386356 - Type mismatch error with annotations and generics
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								bug 376590 - Private fields with @Inject are ignored by unused field validation
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 469584 - ClassCastException in Annotation.detectStandardAnnotation (320)
 *     Jesper S Moller  - Contributions for
 *								bug 384567 - [1.5][compiler] Compiler accepts illegal modifiers on package declaration
 *								bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *     Ulrich Grave <ulrich.grave@gmx.de> - Contributions for
 *                              bug 386692 - Missing "unused" warning on "autowired" fields
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Contributions for
 *                              bug 542520 - [JUnit 5] Warning The method xxx from the type X is never used locally is shown when using MethodSource
 *                              bug 546084 - Using Junit 5s MethodSource leads to ClassCastException
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.junit.Assert;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AnnotationTest extends AbstractComparableTest {

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testBug506888c" };
//		TESTS_NUMBERS = new int[] { 297 };
//		TESTS_RANGE = new int[] { 294, -1 };
	}

	String reportMissingJavadocComments = null;
	private String repeatableIntroText;
	private String repeatableTrailerText;

	public AnnotationTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return AnnotationTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		return options;
	}
	@Override
	protected INameEnvironment getNameEnvironment(String[] testFiles, String[] classPaths, Map<String, String> options) {
		if (this.javaClassLib != null) {
			this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
			return new InMemoryNameEnvironment(testFiles, new INameEnvironment[] {this.javaClassLib });
		}
		return super.getNameEnvironment(testFiles, classPaths, options);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.reportMissingJavadocComments = null;
		this.repeatableIntroText = this.complianceLevel >= ClassFileConstants.JDK1_8 ?
		"Duplicate annotation of non-repeatable type "
		:
		"Duplicate annotation ";
		this.repeatableTrailerText = this.complianceLevel >= ClassFileConstants.JDK1_8 ?
		". Only annotation types marked @Repeatable can be used multiple times at one target.\n"
		:
		". Repeated annotations are allowed only at source level 1.8 or above\n";
		this.javaClassLib = null; // use only in selected tests
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public @interface X {\s
						String value();\s
					}"""
			},
			"");
	}

	// check invalid annotation
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @Foo class X {
					}
					
					@interface Foo {
						String value();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public @Foo class X {
					       ^^^^
				The annotation @Foo must define the attribute value
				----------
				""");
	}

	// check annotation method cannot indirectly return annotation type (circular ref)
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					public @interface Foo {
						Bar value();
					}
					
					@interface Bar {
						Foo value();
					}
					"""
			},
			"""
				----------
				1. ERROR in Foo.java (at line 2)
					Bar value();
					^^^
				Cycle detected: a cycle exists between annotation attributes of Foo and Bar
				----------
				2. ERROR in Foo.java (at line 6)
					Foo value();
					^^^
				Cycle detected: a cycle exists between annotation attributes of Bar and Foo
				----------
				""");
	    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=85538
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Nested {
						String name() default "Hans";
						N2 nest();
					}
					@interface N2 {
						Nested n2() default @Nested(name="Haus", nest= @N2);
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					N2 nest();
					^^
				Cycle detected: a cycle exists between annotation attributes of Nested and N2
				----------
				2. ERROR in X.java (at line 6)
					Nested n2() default @Nested(name="Haus", nest= @N2);
					^^^^^^
				Cycle detected: a cycle exists between annotation attributes of N2 and Nested
				----------
				""");
	}

	// check annotation method cannot directly return annotation type
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					public @interface Foo {
						Foo value();
					}
					"""
			},
			"""
				----------
				1. ERROR in Foo.java (at line 2)
					Foo value();
					^^^
				Cycle detected: the annotation type Foo cannot contain attributes of the annotation type itself
				----------
				""");
	}

	// check annotation type cannot have superclass
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo extends Object {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in Foo.java (at line 1)
					public @interface Foo extends Object {
					                  ^^^
				Annotation type declaration cannot have an explicit superclass
				----------
				""");
	}

	// check annotation type cannot have superinterfaces
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo implements Cloneable {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in Foo.java (at line 1)
					public @interface Foo implements Cloneable {
					                  ^^^
				Annotation type declaration cannot have explicit superinterfaces
				----------
				""");
	}

	// check annotation method cannot be specified parameters
	// TODO (olivier) unoptimal syntax error -> no parameter for annotation method?
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					public @interface Foo {
						String value(int i);
					}
					"""
			},
			"""
				----------
				1. ERROR in Foo.java (at line 2)
					String value(int i);
					       ^^^^^^^^^^^^
				Annotation attributes cannot have parameters
				----------
				""");
	}

	// annotation method cannot be generic?
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"""
					public @interface Foo {
						<T> T value();
					}
					"""
			},
			"""
				----------
				1. ERROR in Foo.java (at line 2)
					<T> T value();
					    ^
				Invalid type T for the annotation attribute Foo.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof
				----------
				2. ERROR in Foo.java (at line 2)
					<T> T value();
					      ^^^^^^^
				Annotation attributes cannot be generic
				----------
				""");
	}

	// check annotation method return type
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
					\t
						Runnable value();
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Runnable value();
				^^^^^^^^
			Invalid type Runnable for the annotation attribute X.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof
			----------
			""");
	}

	// check annotation method missing return type
	// TODO (olivier) we should get rid of syntax error here (tolerate invalid constructor scenario)
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
					\t
						value();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					value();
					^^^^^^^
				Return type for the method is missing
				----------
				""");
	}

	// check annotation denotes annotation type
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@Object
					public class X {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Object
					 ^^^^^^
				Object is not an annotation type
				----------
				""");
	}

	// check for duplicate annotations
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@Foo @Foo
					public class X {
					}
					@interface Foo {}
					"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo\n" +
			"	^^^^\n" +
			this.repeatableIntroText + "@Foo"+ this.repeatableTrailerText +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo\n" +
			"	     ^^^^\n" +
			this.repeatableIntroText + "@Foo"+ this.repeatableTrailerText +
			"----------\n");
	}

	// check single member annotation - no need to specify value if member has default value
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@Foo("hello") public class X {
					}
					
					@interface Foo {
						String id() default "";
						String value() default "";
					}
					"""
			},
			"");
	}

	// check single member annotation -  need to speficy value if member has no default value
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@Foo("hello") public class X {
					}
					
					@interface Foo {
						String id() default "";
						String value() default "";
						String foo();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Foo("hello") public class X {
					^^^^
				The annotation @Foo must define the attribute foo
				----------
				""");
	}

	// check normal annotation -  need to speficy value if member has no default value
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@Foo(
							id = "hello") public class X {
					}
					
					@interface Foo {
						String id() default "";
						String value() default "";
						String foo();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Foo(
					^^^^
				The annotation @Foo must define the attribute foo
				----------
				""");
	}

	// check normal annotation - if single member, no need to be named 'value'
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Name {
						String first();
						String last();
					}
					@interface Author {
						Name name();
					}
					public class X {
					\t
						@Author(name = @Name(first="Bill", last="Yboy"))\s
						void foo() {
						}
					}
					"""
			},
			"");
	}

	// check single member annotation can only refer to 'value' member
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Name {
						String first();
						String last();
					}
					@interface Author {
						Name name();
					}
					@Author(@Name(first="Joe",last="Hacker"))\s
					public class X {
					\t
						@Author(name = @Name(first="Bill", last="Yboy"))\s
						void foo() {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					@Author(@Name(first="Joe",last="Hacker"))\s
					^^^^^^^
				The annotation @Author must define the attribute name
				----------
				2. ERROR in X.java (at line 8)
					@Author(@Name(first="Joe",last="Hacker"))\s
					        ^^^^^
				The attribute value is undefined for the annotation type Author
				----------
				""");
	}

	// check for duplicate member value pairs
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Name {
						String first();
						String last();
					}
					@interface Author {
						Name name();
					}
					public class X {
					\t
						@Author(name = @Name(first="Bill", last="Yboy", last="dup"))\s
						void foo() {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					@Author(name = @Name(first="Bill", last="Yboy", last="dup"))\s
					                                   ^^^^
				Duplicate attribute last in annotation @Name
				----------
				2. ERROR in X.java (at line 10)
					@Author(name = @Name(first="Bill", last="Yboy", last="dup"))\s
					                                                ^^^^
				Duplicate attribute last in annotation @Name
				----------
				""",
			JavacTestOptions.EclipseJustification.EclipseJustification0001);
	}

	// check for duplicate member value pairs - simplified to check javac
	public void test018b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Name {
						String first();
						String last();
					}
					public class X {
						@Name(first="Bill", last="Yboy", last="dup")
						void foo() {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					@Name(first="Bill", last="Yboy", last="dup")
					                    ^^^^
				Duplicate attribute last in annotation @Name
				----------
				2. ERROR in X.java (at line 6)
					@Name(first="Bill", last="Yboy", last="dup")
					                                 ^^^^
				Duplicate attribute last in annotation @Name
				----------
				""");
	}
	// check class annotation member value must be a class literal
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
						Class value() default X.clazz();
					}
					
					public class X {
						@Foo( clazz() )
						void foo() {}
						static Class clazz() { return X.class; }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					Class value() default X.clazz();
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 2)
					Class value() default X.clazz();
					                      ^^^^^^^^^
				The value for annotation attribute Foo.value must be a class literal
				----------
				3. ERROR in X.java (at line 6)
					@Foo( clazz() )
					      ^^^^^^^
				The value for annotation attribute Foo.value must be a class literal
				----------
				4. WARNING in X.java (at line 8)
					static Class clazz() { return X.class; }
					       ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""");
	}

	// check primitive annotation member value must be a constant
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
						int value() default X.val();
					}
					
					public class X {
						@Foo( val() )
						void foo() {}
						static int val() { return 0; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int value() default X.val();
					                    ^^^^^^^
				The value for annotation attribute Foo.value must be a constant expression
				----------
				2. ERROR in X.java (at line 6)
					@Foo( val() )
					      ^^^^^
				The value for annotation attribute Foo.value must be a constant expression
				----------
				""");
	}

	// check String annotation member value must be a constant
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
						String value() default X.val();
					}
					
					public class X {
						@Foo( val() )
						void foo() {}
						static String val() { return ""; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					String value() default X.val();
					                       ^^^^^^^
				The value for annotation attribute Foo.value must be a constant expression
				----------
				2. ERROR in X.java (at line 6)
					@Foo( val() )
					      ^^^^^
				The value for annotation attribute Foo.value must be a constant expression
				----------
				""");
	}
	// check String annotation member value must be a constant
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
						String[] value() default null;
					}
					
					public class X {
						@Foo( null )
						void foo() {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					String[] value() default null;
					                         ^^^^
				The value for annotation attribute Foo.value must be a constant expression
				----------
				2. ERROR in X.java (at line 6)
					@Foo( null )
					      ^^^^
				The value for annotation attribute Foo.value must be a constant expression
				----------
				""");
	}

	// check use of array initializer
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
						String[] value() default {};
					}
					
					public class X {
						@Foo( {} )
						void foo() {}
					}
					"""
			},
			"");
	}

	// check use of binary annotation - check referencing binary annotation
	public void test024() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"""
					public @interface Foo {
						String[] value() default {};
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo({})
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
						String[] value() default {};
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							int value() default 8;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							byte value() default (byte)255;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							boolean value() default true;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							char value() default ' ';
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							short value() default (short)1024;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							double value() default 0.0;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							float value() default -0.0f;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							long value() default 1234567890L;
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					public @interface Foo {
							String value() default "Hello, World";
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					enum E {
						CONST1
					}
					@interface Foo {
						E value() default E.CONST1;
					}"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					@interface Foo {
						Class value() default Object.class;
					}"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					@interface Y {
						int id() default 8;
						Class type();
					}
					public @interface Foo {
						Y value() default @Y(id=10,type=Object.class);
					}"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@Foo()
						void foo() {}
					}
					"""
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
				"""
					@interface Foo {
						int id() default 8;
						Class type();
					}"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Foo(type=String.class) public class X {\n" +
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
				"""
					public @interface X {
						native int id() default 0;
					}"""
			},
		"""
			----------
			1. ERROR in X.java (at line 2)
				native int id() default 0;
				           ^^^^
			Illegal modifier for the annotation attribute X.id; only public & abstract are permitted
			----------
			""");
	}

	// check annotation member modifiers (validity unchanged despite grammar change from JSR 335 - default methods)
	// and https://bugs.eclipse.org/bugs/show_bug.cgi?id=3383968
	public void test039a() {
		String extra = this.complianceLevel < ClassFileConstants.JDK17 ? "" :
				"""
					----------
					1. WARNING in X.java (at line 2)
						strictfp double val() default 0.1;
						^^^^^^^^
					Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
					""";
		int offset = this.complianceLevel < ClassFileConstants.JDK17 ? 0 : 1;
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						strictfp double val() default 0.1;
						synchronized String id() default "zero";
					}"""
			},
			extra +
			"----------\n" +
			(1 + offset) + ". ERROR in X.java (at line 2)\n" +
			"	strictfp double val() default 0.1;\n" +
			"	                ^^^^^\n" +
			"Illegal modifier for the annotation attribute X.val; only public & abstract are permitted\n" +
			"----------\n" +
			(2 + offset) + ". ERROR in X.java (at line 3)\n" +
			"	synchronized String id() default \"zero\";\n" +
			"	                    ^^^^\n" +
			"Illegal modifier for the annotation attribute X.id; only public & abstract are permitted\n" +
			"----------\n");
	}

	// check annotation array field initializer
	public void test040() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						int[] tab;
						int[] value();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int[] tab;
					      ^^^
				The blank final field tab may not have been initialized
				----------
				""");
	}

	// check annotation array field initializer
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						int[] tab = value();
						int[] value();
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int[] tab = value();
				            ^^^^^
			Cannot make a static reference to the non-static method value() from the type X
			----------
			""");
	}

	// check annotation array field initializer
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						int[] tab = { 0 , "aaa".length() };
					}
					"""
			},
		"");
	}

	// check annotation field initializer
	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						int value;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int value;
					    ^^^^^
				The blank final field value may not have been initialized
				----------
				""");
	}

	// check annotation field initializer
	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						protected int value = 0;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					protected int value = 0;
					              ^^^^^
				Illegal modifier for the annotation field X.value; only public, static & final are permitted
				----------
				""");
	}

	// check incompatible default values
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface X {
					    int id () default 10L;\s
					    int[] ids() default { 10L };
					    Class cls() default new Object();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int id () default 10L;\s
					                  ^^^
				Type mismatch: cannot convert from long to int
				----------
				2. ERROR in X.java (at line 3)
					int[] ids() default { 10L };
					                      ^^^
				Type mismatch: cannot convert from long to int
				----------
				3. WARNING in X.java (at line 4)
					Class cls() default new Object();
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				4. ERROR in X.java (at line 4)
					Class cls() default new Object();
					                    ^^^^^^^^^^^^
				Type mismatch: cannot convert from Object to Class
				----------
				""");
	}

	// check need for constant pair value
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    boolean val() default true;
					}
					
					public class X {
					
						boolean bar() {
							return false;
						}
					    @I(val = bar()) void foo() {
					    }
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 10)
				@I(val = bar()) void foo() {
				         ^^^^^
			The value for annotation attribute I.val must be a constant expression
			----------
			""");
	}

	// check array handling of singleton
	public void test047() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    boolean[] val() default {true};
					}
					
					public class X {
					    @I(val = false) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(val={false})
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);

		ClassFileReader fileReader = ClassFileReader.read(new File(OUTPUT_DIR + File.separator  +"I.class"));
		assertEquals("Not an annotation type declaration", TypeDeclaration.ANNOTATION_TYPE_DECL, TypeDeclaration.kind(fileReader.getModifiers()));
	}

	// check invalid constant in array initializer
	public void test048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					     boolean[] value();
					}
					
					public class X {
					     @I(value={false, X.class != null }) void foo() {
					     }
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 6)
				@I(value={false, X.class != null }) void foo() {
				                 ^^^^^^^^^^^^^^^
			The value for annotation attribute I.value must be a constant expression
			----------
			""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79349
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					
					@Documented
					@Retention(RetentionPolicy.RUNTIME)
					@Target(ElementType.TYPE)
					@interface MyAnn {
					  String value() default "Default Message";
					}
					
					@MyAnn
					public class X {
						public @MyAnn void something() { }\t
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 12)
				public @MyAnn void something() { }\t
				       ^^^^^^
			The annotation @MyAnn is disallowed for this location
			----------
			""");
	}

	// check array handling of singleton
	public void test050() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    String[] value();
					}
					
					public class X {
					    @I("Hello") void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value={"Hello"})
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test051() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    String value() default "Hello";
					}
					
					public class X {
					    @I("Hi") void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value="Hi")
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test052() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    int value() default 0;
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=(int) 2)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test053() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    byte value() default 0;
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=(byte) 2)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test054() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    short value() default 0;
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=(short) 2)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test055() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    char value() default ' ';
					}
					
					public class X {
					    @I('@') void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value='@')
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test056() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    long value() default 6;
					}
					
					public class X {
					    @I(Long.MAX_VALUE) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=9223372036854775807L)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test057() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    float value();
					}
					
					public class X {
					    @I(-0.0f) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=-0.0f)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test058() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    double value();
					}
					
					public class X {
					    @I(-0.0) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=-0.0)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test059() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
					    double value() default 0.0;
					    int id();
					}
					@interface I {
					    Foo value();
					}
					
					public class X {
					    @I(@Foo(id=5)) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=@Foo(id=(int) 5))
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test060() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					enum Color {\
						BLUE, RED, GREEN
					}
					@interface I {
					    Color value() default Color.GREEN;
					}
					
					public class X {
					    @I(Color.RED) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value=Color.RED)
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test061() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					enum Color {\
						BLUE, RED, GREEN
					}
					@interface I {
					    Color[] value() default { Color.GREEN };
					}
					
					public class X {
					    @I(Color.RED) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(value={Color.RED})
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test062() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
					    double value() default 0.0;
					    int id() default 0;
					}
					enum Color {\
						BLUE, RED, GREEN
					}
					@interface I {
					    Color[] enums() default { Color.GREEN };
					    Foo[] annotations() default { @Foo() };
					    int[] ints() default { 0, 1, 2, 3 };
					    byte[] bytes() default { 0 };
					    short[] shorts() default { 0 };
					    long[] longs() default { Long.MIN_VALUE, Long.MAX_VALUE };
					    String[] strings() default { "" };
					    boolean[] booleans() default { true, false };
					    float[] floats() default { Float.MAX_VALUE };
					    double[] doubles() default { Double.MAX_VALUE };
					}
					
					public class X {
					    @I(enums=Color.RED,
							annotations=@Foo(),
							ints=2,
							bytes=1,
							shorts=5,
							longs=Long.MIN_VALUE,
							strings="Hi",
							booleans=true,
							floats=0.0f,
							doubles=-0.0) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  @I(enums={Color.RED},
			    annotations={@Foo},
			    ints={(int) 2},
			    bytes={(byte) 1},
			    shorts={(short) 5},
			    longs={-9223372036854775808L},
			    strings={"Hi"},
			    booleans={true},
			    floats={0.0f},
			    doubles={-0.0})
			  void foo();\
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	public void test063() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
					    double value() default 0.0;
					    int id() default 0;
					}
					enum Color {\
						BLUE, RED, GREEN
					}
					@interface I {
					    Color enums() default Color.GREEN;
					    Foo annotations() default @Foo();
					    int ints() default 0;
					    byte bytes() default 0;
					    short shorts() default 0;
					    long longs() default Long.MIN_VALUE;
					    String strings() default "";
					    boolean booleans() default true;
					    float floats() default Float.MAX_VALUE;
					    double doubles() default Double.MAX_VALUE;
					}
					
					public class X {
					    @I(enums=Color.RED,
							annotations=@Foo(),
							ints=2,
							bytes=1,
							shorts=5,
							longs=Long.MIN_VALUE,
							strings="Hi",
							booleans=true,
							floats=0.0f,
							doubles=-0.0) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  @I(enums=Color.RED,
			    annotations=@Foo,
			    ints=(int) 2,
			    bytes=(byte) 1,
			    shorts=(short) 5,
			    longs=-9223372036854775808L,
			    strings="Hi",
			    booleans=true,
			    floats=0.0f,
			    doubles=-0.0)
			  void foo();\
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}

	public void test064() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    String[] names();
					}
					
					public class X {
					    @I(names={"Hello"}) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(names={"Hello"})
			  void foo();\
			""";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79848
	public void test065() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    Class[] classes();
					}
					
					public class X {
					    @I(classes = {X.class, I.class}) public void foo(){
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #6 ()V
			  // Stack: 0, Locals: 1
			  @I(classes={X,I})
			  public void foo();\
			""";

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
				"""
					@interface I {
					    short value() default 0;
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					
					"""
			},
		"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
	public void test067() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    int value() default 0L;
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int value() default 0L;
					                    ^^
				Type mismatch: cannot convert from long to int
				----------
				""");
	}
	// 79844 - variation
	public void test068() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    short[] value() default 2;
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					"""
			},
		"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
	public void test069() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    short[] value() default { 2 };
					}
					
					public class X {
					    @I(2) void foo() {
					    }
					}
					"""
			},
		"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79847
	public void test070() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					    int[][] ids();
					    Object[][] obs();
					}
					
					public class X {
					
					    @I(ids = {{1 , 2}, { 3 }}) public void foo(){
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int[][] ids();
					^^^^^^^
				Invalid type int[][] for the annotation attribute I.ids; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof
				----------
				2. ERROR in X.java (at line 3)
					Object[][] obs();
					^^^^^^^^^^
				Invalid type Object[][] for the annotation attribute I.obs; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof
				----------
				3. ERROR in X.java (at line 8)
					@I(ids = {{1 , 2}, { 3 }}) public void foo(){
					^^
				The annotation @I must define the attribute obs
				----------
				4. ERROR in X.java (at line 8)
					@I(ids = {{1 , 2}, { 3 }}) public void foo(){
					          ^^^^^^^
				The value for annotation attribute I.ids must be a constant expression
				----------
				5. ERROR in X.java (at line 8)
					@I(ids = {{1 , 2}, { 3 }}) public void foo(){
					                   ^^^^^
				The value for annotation attribute I.ids must be a constant expression
				----------
				""");
	}

	public void test071() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
						int hashCode();
						Object clone();
					}
					
					public class X {
					    @I(hashCode = 0) public void foo(){
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int hashCode();
					    ^^^^^^^^^^
				The annotation type I cannot override the method Annotation.hashCode()
				----------
				2. ERROR in X.java (at line 3)
					Object clone();
					^^^^^^
				Invalid type Object for the annotation attribute I.clone; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof
				----------
				3. ERROR in X.java (at line 3)
					Object clone();
					       ^^^^^^^
				The annotation type I cannot override the method Object.clone()
				----------
				4. ERROR in X.java (at line 7)
					@I(hashCode = 0) public void foo(){
					^^
				The annotation @I must define the attribute clone
				----------
				""");
	}

	// check annotation cannot refer to inherited methods as attributes
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
					}
					
					public class X {
					    @I(hashCode = 0) public void foo(){
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					@I(hashCode = 0) public void foo(){
					   ^^^^^^^^
				The attribute hashCode is undefined for the annotation type I
				----------
				""");
	}

	// check code generation of annotation default attribute (autowrapping)
	public void test073() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
					    double value() default 0.0;
					    int id() default 0;
					}
					enum Color {\
						BLUE, RED, GREEN
					}
					@interface I {
					    Color[] enums() default Color.GREEN;
					    Foo[] annotations() default @Foo();
					    int[] ints() default 0;
					    byte[] bytes() default 1;
					    short[] shorts() default 3;
					    long[] longs() default Long.MIN_VALUE;
					    String[] strings() default "";
					    boolean[] booleans() default true;
					    float[] floats() default Float.MAX_VALUE;
					    double[] doubles() default Double.MAX_VALUE;
					    Class[] classes() default I.class;
					}
					
					public class X {
					    @I(enums=Color.RED,
							annotations=@Foo(),
							ints=2,
							bytes=1,
							shorts=5,
							longs=Long.MIN_VALUE,
							strings="Hi",
							booleans=true,
							floats=0.0f,
							doubles=-0.0) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"I.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			abstract @interface I extends java.lang.annotation.Annotation {
			 \s
			  // Method descriptor #8 ()[LColor;
			  public abstract Color[] enums() default {Color.GREEN};
			 \s
			  // Method descriptor #13 ()[LFoo;
			  public abstract Foo[] annotations() default {@Foo};
			 \s
			  // Method descriptor #16 ()[I
			  public abstract int[] ints() default {(int) 0};
			 \s
			  // Method descriptor #19 ()[B
			  public abstract byte[] bytes() default {(byte) 1};
			 \s
			  // Method descriptor #22 ()[S
			  public abstract short[] shorts() default {(short) 3};
			 \s
			  // Method descriptor #25 ()[J
			  public abstract long[] longs() default {-9223372036854775808L};
			 \s
			  // Method descriptor #29 ()[Ljava/lang/String;
			  public abstract java.lang.String[] strings() default {""};
			 \s
			  // Method descriptor #32 ()[Z
			  public abstract boolean[] booleans() default {true};
			 \s
			  // Method descriptor #34 ()[F
			  public abstract float[] floats() default {3.4028235E38f};
			 \s
			  // Method descriptor #37 ()[D
			  public abstract double[] doubles() default {1.7976931348623157E308};
			 \s
			  // Method descriptor #41 ()[Ljava/lang/Class;
			  public abstract java.lang.Class[] classes() default {I};
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	// check code generation of annotation default attribute non array types
	public void test074() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@interface Foo {
					    double value() default 0.0;
					    int id() default 0;
					}
					enum Color {\
						BLUE, RED, GREEN
					}
					@interface I {
					    Color _enum() default Color.GREEN;
					    Foo _annotation() default @Foo;
					    int _int() default 0;
					    byte _byte() default 1;
					    short _short() default 3;
					    long _long() default Long.MIN_VALUE;
					    String _string() default "";
					    boolean _boolean() default true;
					    float _float() default Float.MAX_VALUE;
					    double _double() default Double.MAX_VALUE;
					    Class _class() default I.class;
					}
					public class X {
					    @I(_enum=Color.RED,
							_annotation=@Foo(),
							_int=2,
							_byte=1,
							_short=5,
							_long=Long.MIN_VALUE,
							_string="Hi",
							_boolean=true,
							_float=0.0f,
							_double=-0.0) void foo() {
					    }
					}
					"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"I.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			abstract @interface I extends java.lang.annotation.Annotation {
			 \s
			  // Method descriptor #8 ()LColor;
			  public abstract Color _enum() default Color.GREEN;
			 \s
			  // Method descriptor #13 ()LFoo;
			  public abstract Foo _annotation() default @Foo;
			 \s
			  // Method descriptor #16 ()I
			  public abstract int _int() default (int) 0;
			 \s
			  // Method descriptor #19 ()B
			  public abstract byte _byte() default (byte) 1;
			 \s
			  // Method descriptor #22 ()S
			  public abstract short _short() default (short) 3;
			 \s
			  // Method descriptor #25 ()J
			  public abstract long _long() default -9223372036854775808L;
			 \s
			  // Method descriptor #29 ()Ljava/lang/String;
			  public abstract java.lang.String _string() default "";
			 \s
			  // Method descriptor #32 ()Z
			  public abstract boolean _boolean() default true;
			 \s
			  // Method descriptor #34 ()F
			  public abstract float _float() default 3.4028235E38f;
			 \s
			  // Method descriptor #37 ()D
			  public abstract double _double() default 1.7976931348623157E308;
			 \s
			  // Method descriptor #41 ()Ljava/lang/Class;
			  public abstract java.lang.Class _class() default I;
			""";

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
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					
					@Target ({FIELD, FIELD})
					@interface Tgt {
						E[] foo();
						int[] bar();
					}
					enum E {
						BLEU, BLANC, ROUGE
					}
					
					@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )
					public class X {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					@Target ({FIELD, FIELD})
					                 ^^^^^
				Duplicate element FIELD specified in annotation @Target
				----------
				2. ERROR in X.java (at line 13)
					@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )
					^^^^
				The annotation @Tgt is disallowed for this location
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77463
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"private @interface TestAnnot {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					private @interface TestAnnot {
					                   ^^^^^^^^^
				Illegal modifier for the annotation type TestAnnot; only public & abstract are permitted
				----------
				""");
	}
	// check @Override annotation - strictly for superclasses (overrides) and not interfaces (implements)
	public void test077() {
		String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
			?	"""
				----------
				1. ERROR in X.java (at line 14)
					void foo() {}
					     ^^^^^
				The method foo() of type X must override a superclass method
				----------
				2. ERROR in X.java (at line 18)
					public void baz() {}
					            ^^^^^
				The method baz() of type X must override a superclass method
				----------
				"""
			:	"""
				----------
				1. ERROR in X.java (at line 14)
					void foo() {}
					     ^^^^^
				The method foo() of type X must override or implement a supertype method
				----------
				""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class Further {
						void bar() {}
					}
					
					class Other extends Further {
					}
					
					interface Baz {
						void baz();
					}
					
					public class X extends Other implements Baz {
						@Override
						void foo() {}
						@Override
						void bar() {}
						@Override
						public void baz() {}
					}
					"""
			},
			expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80114
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public @interface X {
						X() {}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					X() {}
					^^^
				Annotation type declaration cannot have a constructor
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test079() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import static java.lang.annotation.RetentionPolicy.*;
					import static java.lang.annotation.ElementType.*;
					
					@Retention(RUNTIME)
					@Target({TYPE})
					@interface Attr {
					  public int tst() default -1;
					}
					
					@Attr\s
					public class X {
					  public static void main(String args[]) {
					  	Object e = X.class.getAnnotation(Attr.class);
					  	System.out.print(e);
					  }
					}"""
			},
			"@Attr(tst=-1)");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import static java.lang.annotation.RetentionPolicy.*;
					import static java.lang.annotation.ElementType.*;
					
					@Retention(CLASS)
					@Target({TYPE})
					@interface Attr {
					  public int tst() default -1;
					}
					
					@Attr\s
					public class X {
					  public static void main(String args[]) {
					  	Object e = X.class.getAnnotation(Attr.class);
					  	System.out.print(e);
					  }
					}"""
			},
			"null");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import static java.lang.annotation.RetentionPolicy.*;
					import static java.lang.annotation.ElementType.*;
					
					@Retention(SOURCE)
					@Target({TYPE})
					@interface Attr {
					  public int tst() default -1;
					}
					
					@Attr\s
					public class X {
					  public static void main(String args[]) {
					  	Object e = X.class.getAnnotation(Attr.class);
					  	System.out.print(e);
					  }
					}"""
			},
			"null");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test082() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import static java.lang.annotation.RetentionPolicy.*;
					import static java.lang.annotation.ElementType.*;
					
					@Retention(SOURCE)
					@Target({TYPE})
					@interface Attr {
					  public int tst() default -1;
					}""",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@Attr\s
					public class X {
					  public static void main(String args[]) {
					  	Object e = X.class.getAnnotation(Attr.class);
					  	System.out.print(e);
					  }
					}"""
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
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import static java.lang.annotation.RetentionPolicy.*;
					import static java.lang.annotation.ElementType.*;
					
					@Retention(CLASS)
					@Target({TYPE})
					@interface Attr {
					  public int tst() default -1;
					}""",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@Attr\s
					public class X {
					  public static void main(String args[]) {
					  	Object e = X.class.getAnnotation(Attr.class);
					  	System.out.print(e);
					  }
					}"""
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
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					import static java.lang.annotation.RetentionPolicy.*;
					import static java.lang.annotation.ElementType.*;
					
					@Retention(RUNTIME)
					@Target({TYPE})
					@interface Attr {
					  public int tst() default -1;
					}""",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@Attr\s
					public class X {
					  public static void main(String args[]) {
					  	Object e = X.class.getAnnotation(Attr.class);
					  	System.out.print(e);
					  }
					}"""
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
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					
					public class X {
					
					  @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface
					TestAnnotation {
					
					    String testAttribute();
					
					  }
					  @TestAnnotation(testAttribute = "test") class A {
					  }
					
					  public static void main(String[] args) {
					    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));
					  }
					
					}"""
			},
			"true");
	}
	// check handling of empty array initializer
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					
					@Target({}) @interface I {}
					@I public class X {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					@I public class X {}
					^^
				The annotation @I is disallowed for this location
				----------
				""");
	}

	// check type targeting annotation also allowed for annotation type
	public void test087() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					
					@Target(TYPE)
					@interface Annot {
					}
					
					@Annot
					public @interface X {
					}
					"""
			},
			"");
	}

	// check parameter/local target for annotation
	public void test088() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					
					@Target(LOCAL_VARIABLE)
					@interface Annot {
					}
					
					public class X {
						void foo(@Annot int i) {
							@Annot int j;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					void foo(@Annot int i) {
					         ^^^^^^
				The annotation @Annot is disallowed for this location
				----------
				""");
	}

	// Add check for parameter
	public void test089() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					
					public class X {
					
					    @Target(ElementType.PARAMETER) @interface I {}
					   \s
					    void m(@I int i){
					    }
					}"""
			},
			"");
	}
	// Add check that type includes annotation type
	public void test090() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					
					public class X {
					
					    @Target(ElementType.TYPE) @interface Annot1 {}
					   \s
					    @Annot1 @interface Annot2 {}
					}"""
			},
			"");
	}
	// Add check that a field cannot have an annotation targetting TYPE
	public void test091() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					
					public class X {
					
					    @Target(ElementType.TYPE) @interface Marker {}
					   \s
					    @Marker static int i = 123;
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					@Marker static int i = 123;
					^^^^^^^
				The annotation @X.Marker is disallowed for this location
				----------
				""");
	}
	// Add check that a field cannot have an annotation targetting FIELD
	public void test092() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					
					public class X {
					
					    @Target(ElementType.FIELD) @interface Marker {}
					   \s
					    @Marker static int i = 123;
					}"""
			},
			"");
	}
	// @Inherited can only be used on annotation types
	public void test093() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Inherited;
					
					@Deprecated
					@Inherited
					class A {
					}
					
					class B extends A {
					}
					
					class C extends B {
					}
					
					public class X {
						C c;
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 4)
				@Inherited
				^^^^^^^^^^
			The annotation @Inherited is disallowed for this location
			----------
			""");
	}
	// check handling of empty array initializer (binary check)
	public void test094() {
		this.runConformTest(
			new String[] {
				"I.java",
				"""
					import java.lang.annotation.Target;
					
					@Target({}) @interface I {}""",
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@I public class X {}"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@I public class X {}
					^^
				The annotation @I is disallowed for this location
				----------
				""",
			null,
			false,
			null);
	}

	// check no interaction between Retention and Target (switch fall-thru)
	public void test095() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					
					@Retention(RetentionPolicy.RUNTIME)
					@interface Ann {}
					
					public class X {
						@Ann
						void foo() {}
					}
					""",
			},
			"");
	}

	// check attributes for parameters
	public void test096() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.annotation.RetentionPolicy.*;
					import java.lang.annotation.Retention;
					import java.lang.annotation.Annotation;
					import java.lang.reflect.Method;
					
					@Retention(CLASS) @interface Attr {
					}
					
					@Retention(RUNTIME) @interface Foo {
						int id() default 0;
					}
					@Foo(id=5) @Attr public class X {
						public void foo(@Foo(id=5) @Attr final int j, @Attr final int k, int n) {
						}
					\t
						public static void main(String[] args) {
							try {
								Class c = X.class;
								Annotation[] annots = c.getAnnotations();
								System.out.print(annots.length);
								Method method = c.getMethod("foo", Integer.TYPE, Integer.TYPE, Integer.TYPE);
								Annotation[][] annotations = method.getParameterAnnotations();
								final int length = annotations.length;
								System.out.print(length);
								if (length == 3) {
									System.out.print(annotations[0].length);
									System.out.print(annotations[1].length);
									System.out.print(annotations[2].length);
								}
							} catch(NoSuchMethodException e) {
							}
						}
					}""",
			},
			"13100");
	}

	public void test097() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
						int id default 0;
					}
					
					@I() public class X {
						public static void main(String[] s) {
							System.out.println(X.class.getAnnotation(I.class));
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int id default 0;
					       ^^^^^^^
				Syntax error on token "default", = expected
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80328
	public void test098() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@interface I {
						int id default 0;
					}
					
					@I() public class X {
						public static void main(String[] s) {
							System.out.println(X.class.getAnnotation(I.class));
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					int id default 0;
					       ^^^^^^^
				Syntax error on token "default", = expected
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80780
	public void test099() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.lang.reflect.Method;
					
					public class X {
					    public static void main(String[] args) {
					        Object o = new X();
					        for (Method m : o.getClass().getMethods()) {
					            if (m.isAnnotationPresent(MyAnon.class)) {
					                System.out.println(m.getAnnotation(MyAnon.class).c());
					            }
					        }
					    }
					    @MyAnon(c = X.class)\s
					    public void foo() {}
					
					    @Retention(RetentionPolicy.RUNTIME)\s
					    public @interface MyAnon {
					        Class c();
					    }
					    public interface I {
					    }
					}"""
			},
			"class X");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		String expectedOutput = null;
		if (options.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput =
				"""
					  Inner classes:
					    [inner class info: #66 X$I, outer class info: #1 X
					     inner name: #68 I, accessflags: 1545 public abstract static],
					    [inner class info: #27 X$MyAnon, outer class info: #1 X
					     inner name: #69 MyAnon, accessflags: 9737 public abstract static]
					""";
		} else if (options.targetJDK == ClassFileConstants.JDK1_6) {
			expectedOutput =
				"""
					  Inner classes:
					    [inner class info: #70 X$I, outer class info: #1 X
					     inner name: #72 I, accessflags: 1545 public abstract static],
					    [inner class info: #27 X$MyAnon, outer class info: #1 X
					     inner name: #73 MyAnon, accessflags: 9737 public abstract static]
					""";
		} else {
			return;
		}

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
				"""
					abstract class Foo {
						abstract protected boolean accept(Object o);
					}
					
					public class X extends Foo {
						@Override\s
						protected boolean accept(Object o) { return false; }
					}
					""",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81148
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					
					@Target(Element)
					public @interface X {
					\t
						boolean UML() default false;
						boolean platformDependent() default true;
						boolean OSDependent() default true;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					@Target(Element)
					        ^^^^^^^
				Element cannot be resolved to a variable
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
	public void test102() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  @TestAnnotation(testAttribute = "test") class A {
					  }
					  public static void main(String[] args) {
					    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));
					  }
					}""",
				"TestAnnotation.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface
					TestAnnotation {
					    String testAttribute();
					}
					"""
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
	public void test103() {
		this.runConformTest(
			new String[] {
				"TestAnnotation.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;
					import java.lang.annotation.Retention;
					@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface
					TestAnnotation {
					    String testAttribute();
					}
					"""
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  @TestAnnotation(testAttribute = "test") class A {
					  }
					  public static void main(String[] args) {
					    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));
					  }
					}""",
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
				"""
					@interface ValuesAnnotation {
						byte[] byteArrayValue();
						char[] charArrayValue();
						boolean[] booleanArrayValue();
						int[] intArrayValue();
						short[] shortArrayValue();
						long[] longArrayValue();
						float[] floatArrayValue();
						double[] doubleArrayValue();
						String[] stringArrayValue();
						ValuesEnum[] enumArrayValue();
						ValueAttrAnnotation[] annotationArrayValue();
						Class[] classArrayValue();
						byte byteValue();
						char charValue();
						boolean booleanValue();
						int intValue();
						short shortValue();
						long longValue();
						float floatValue();
						double doubleValue();
						String stringValue();
						ValuesEnum enumValue();
						ValueAttrAnnotation annotationValue();
						Class classValue();
					}
					enum ValuesEnum {
						ONE, TWO;
					}
					
					@interface ValueAttrAnnotation {
						String value() default "";
					}
					@interface ValueAttrAnnotation1 {
						String value();
					}
					@interface ValueAttrAnnotation2 {
						String value();
					}
					@ValuesAnnotation(
					  byteValue = 1,
					  charValue = 'A',
					  booleanValue = true,
					  intValue = 1,
					  shortValue = 1,
					  longValue = 1L,
					  floatValue = 1.0f,
					  doubleValue = 1.0d,
					  stringValue = "A",
					
					  enumValue = ValuesEnum.ONE,
					  annotationValue = @ValueAttrAnnotation( "annotation"),
					  classValue = X.class,
					
					  byteArrayValue = { 1, -1},
					  charArrayValue = { 'c', 'b', (char)-1},
					  booleanArrayValue = {true, false},
					  intArrayValue = { 1, -1},
					  shortArrayValue = { (short)1, (short)-1},
					  longArrayValue = { 1L, -1L},
					  floatArrayValue = { 1.0f, -1.0f},
					  doubleArrayValue = { 1.0d, -1.0d},
					  stringArrayValue = { "aa", "bb"},
					
					  enumArrayValue = {ValuesEnum.ONE, ValuesEnum.TWO},
					  annotationArrayValue = {@ValueAttrAnnotation( "annotation1"),
					@ValueAttrAnnotation( "annotation2")},
					  classArrayValue = {X.class, X.class}
					)
					@ValueAttrAnnotation1( "classAnnotation1")
					@ValueAttrAnnotation2( "classAnnotation2")
					public class X {
					
					  @ValueAttrAnnotation1( "fieldAnnotation1")
					  @ValueAttrAnnotation2( "fieldAnnotation2")
					  public String testfield = "test";
					
					  @ValueAttrAnnotation1( "methodAnnotation1")
					  @ValueAttrAnnotation2( "methodAnnotation2")
					  @ValueAttrAnnotation()
					  public void testMethod(\s
					      @ValueAttrAnnotation1( "param1Annotation1")\s
					      @ValueAttrAnnotation2( "param1Annotation2") String param1,\s
					      @ValueAttrAnnotation1( "param2Annotation1")\s
					      @ValueAttrAnnotation2( "param2Annotation2") int param2) {
					    // @ValueAttrAnnotation( "codeAnnotation")
					  }
					}
					"""
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82136
	public void test105() {
		this.runConformTest(
			new String[] {
				"Property.java",
				"""
					import java.lang.annotation.Documented;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					
					@Documented
					@Retention(RetentionPolicy.RUNTIME)
					public @interface Property
					{
					  String property();
					  String identifier() default "";
					}""",
				"Properties.java",
				"""
					import java.lang.annotation.Documented;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					
					@Documented
					@Retention(RetentionPolicy.RUNTIME)
					public @interface Properties {
					  Property[] value();
					}""",
				"X.java",
				"""
					@Properties({
					  @Property(property = "prop", identifier = "someIdentifier"),
					  @Property(property = "type")
					})
					public interface X {
					  void setName();
					  String getName();
					}"""
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
                """
					public @interface X {
					    int[] bar() default null;
					}""",
            },
            """
				----------
				1. ERROR in X.java (at line 2)
					int[] bar() default null;
					                    ^^^^
				The value for annotation attribute X.bar must be a constant expression
				----------
				""");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test107() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					@interface Ann {
					    int[] bar();
					}
					@Ann(bar=null) class X {}""",
            },
            """
				----------
				1. ERROR in X.java (at line 4)
					@Ann(bar=null) class X {}
					         ^^^^
				The value for annotation attribute Ann.bar must be a constant expression
				----------
				""");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test108() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Foo {}
					
					@interface Bar {
					    Foo[] foo() default null;
					}
					
					@Bar(foo=null)
					public class X {\s
					}
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 4)
					Foo[] foo() default null;
					                    ^^^^
				The value for annotation attribute Bar.foo must be some @Foo annotation\s
				----------
				2. ERROR in X.java (at line 7)
					@Bar(foo=null)
					         ^^^^
				The value for annotation attribute Bar.foo must be some @Foo annotation\s
				----------
				""");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test109() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Foo {}
					
					@interface Bar {
					    Foo[] foo() default "";
					}
					
					@Bar(foo="")
					public class X {\s
					}
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 4)
					Foo[] foo() default "";
					                    ^^
				The value for annotation attribute Bar.foo must be some @Foo annotation\s
				----------
				2. ERROR in X.java (at line 7)
					@Bar(foo="")
					         ^^
				The value for annotation attribute Bar.foo must be some @Foo annotation\s
				----------
				""");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791
    public void test110() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					import java.lang.annotation.Annotation;
					import java.util.Arrays;
					
					@interface Ann {
					}
					
					interface Iface extends Ann {
					}
					
					abstract class Klass implements Ann {
					}
					
					class SubKlass extends Klass {
						public Class<? extends Annotation> annotationType() {
							return null;
						}
					}
					
					public class X {
						public static void main(String[] args) {
							Class c = SubKlass.class;
							System.out.print("Classes:");
							while (c != Object.class) {
								System.out.print("-> " + c.getName());
								c = c.getSuperclass();
							}
					
							System.out.print(", Interfaces:");
							c = SubKlass.class;
							while (c != Object.class) {
								Class[] i = c.getInterfaces();
								System.out.print("-> " + Arrays.asList(i));
								c = c.getSuperclass();
							}
						}
					}
					""",
            },
			"Classes:-> SubKlass-> Klass, Interfaces:-> []-> [interface Ann]");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791 - variation
    public void test111() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(
    			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
    			CompilerOptions.ERROR);
    	customOptions.put(
    			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
    			CompilerOptions.DISABLED);

    	String expectedOutput =
    		"""
			----------
			1. WARNING in X.java (at line 8)
				interface Iface extends Ann {
				                        ^^^
			The annotation type Ann should not be used as a superinterface for Iface
			----------
			2. WARNING in X.java (at line 11)
				abstract class Klass implements Ann {
				                                ^^^
			The annotation type Ann should not be used as a superinterface for Klass
			----------
			3. ERROR in X.java (at line 14)
				class SubKlass extends Klass {
				      ^^^^^^^^
			The type SubKlass must implement the inherited abstract method Ann.foo()
			----------
			4. WARNING in X.java (at line 20)
				class AnnImpl implements Ann {
				                         ^^^
			The annotation type Ann should not be used as a superinterface for AnnImpl
			----------
			5. ERROR in X.java (at line 21)
				public boolean equals(Object obj) { return false; }
				               ^^^^^^^^^^^^^^^^^^
			The method equals(Object) of type AnnImpl should be tagged with @Override since it actually overrides a superclass method
			----------
			6. ERROR in X.java (at line 22)
				public int hashCode() { return 0; }
				           ^^^^^^^^^^
			The method hashCode() of type AnnImpl should be tagged with @Override since it actually overrides a superclass method
			----------
			7. ERROR in X.java (at line 23)
				public String toString() { return null; }
				              ^^^^^^^^^^
			The method toString() of type AnnImpl should be tagged with @Override since it actually overrides a superclass method
			----------
			8. WARNING in X.java (at line 30)
				Class c = SubKlass.class;
				^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			9. WARNING in X.java (at line 41)
				Class[] i = c.getInterfaces();
				^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			""";

		this.runNegativeTest(
				true,
	    		new String[] {
						"X.java",
						"""
							import java.lang.annotation.Annotation;
							import java.util.Arrays;
							
							@interface Ann {
								int foo();
							}
							
							interface Iface extends Ann {
							}
							
							abstract class Klass implements Ann {
							}
							
							class SubKlass extends Klass {
								public Class<? extends Annotation> annotationType() {
									return null;
								}
							}
							
							class AnnImpl implements Ann {
							    public boolean equals(Object obj) { return false; }
							    public int hashCode() { return 0; }
							    public String toString() { return null; }
							    public Class<? extends Annotation> annotationType() { return null; }
							    public int foo() { return 0; }
							}
							
							public class X {
								public static void main(String[] args) {
									Class c = SubKlass.class;
									System.out.println("Classes:");
									while (c != Object.class) {
										System.out.println("-> " + c.getName());
										c = c.getSuperclass();
									}
							
									System.out.println();
									System.out.println("Interfaces:");
									c = SubKlass.class;
									while (c != Object.class) {
										Class[] i = c.getInterfaces();
										System.out.println("-> " + Arrays.asList(i));
										c = c.getSuperclass();
									}
								}
							}
							""",
		            },
		null, customOptions,
		expectedOutput,
		JavacTestOptions.SKIP);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291
    public void test112() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Annot {
					  String foo1() default "";
					}
					@Annot(foo1=zzz)
					public class X {
					  static final String zzz =  "";
					}
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 4)
					@Annot(foo1=zzz)
					            ^^^
				zzz cannot be resolved to a variable
				----------
				""");
    }
    public void test113() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Annot {
						String foo();
					}
					@Annot( foo = new String(){} )
					public class X {
					\t
					\t
					}
					""",
            },
    		"""
				----------
				1. ERROR in X.java (at line 4)
					@Annot( foo = new String(){} )
					                  ^^^^^^
				An anonymous class cannot subclass the final class String
				----------
				""");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291 - variation
    public void test114() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Annot {
						Class foo();
					}
					@Annot( foo = M.class )
					public class X {
						class M {}
					\t
					}
					""",
            },
            """
				----------
				1. WARNING in X.java (at line 2)
					Class foo();
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 4)
					@Annot( foo = M.class )
					              ^
				M cannot be resolved to a type
				----------
				""");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291 - variation
    public void test115() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Annot {
						Class foo();
						String bar() default "";
					}
					@Annot(foo = M.class, bar = baz()+s)
					public class X {
						class M {
						}
						final static String s = "";
						String baz() { return null; }
						@Annot(foo = T.class, bar = s)
						<T> T foo(T t, String s) {
							return null;
						}
					}
					""",
            },
            """
				----------
				1. WARNING in X.java (at line 2)
					Class foo();
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 5)
					@Annot(foo = M.class, bar = baz()+s)
					             ^
				M cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					@Annot(foo = M.class, bar = baz()+s)
					                            ^^^
				The method baz() is undefined for the type X
				----------
				4. ERROR in X.java (at line 5)
					@Annot(foo = M.class, bar = baz()+s)
					                                  ^
				s cannot be resolved to a variable
				----------
				5. ERROR in X.java (at line 11)
					@Annot(foo = T.class, bar = s)
					             ^^^^^^^
				Illegal class literal for the type parameter T
				----------
				6. WARNING in X.java (at line 12)
					<T> T foo(T t, String s) {
					                      ^
				The parameter s is hiding a field from type X
				----------
				""");
    }
    // check @Deprecated support
    public void test116() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					/** @deprecated */
					@Deprecated
					public class X {
					}
					""",
                "Y.java",
				"""
					public class Y {
						X x;
						Zork z;
					}
					""",
            },
			"""
				----------
				1. WARNING in Y.java (at line 2)
					X x;
					^
				The type X is deprecated
				----------
				2. ERROR in Y.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @Deprecated support
    public void test117() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@Deprecated
					public class X {
					}
					""",
                "Y.java",
				"""
					public class Y {
						X x;
						Zork z;
					}
					""",
            },
			"""
				----------
				1. WARNING in Y.java (at line 2)
					X x;
					^
				The type X is deprecated
				----------
				2. ERROR in Y.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @Deprecated support
    public void test118() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Deprecated {}
					
					@Deprecated // not the real @Deprecated interface
					public class X {
					}
					""",
                "Y.java",
				"""
					public class Y {
						X x;
						Zork z;
					}
					""",
            },
			"""
				----------
				1. ERROR in Y.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @Deprecated support
    public void test119() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@Deprecated
					public class X {
						void foo(){}
					}
					""",
                "Y.java",
				"""
					public class Y extends X {
						void foo(){ super.foo(); }
						Zork z;
					}
					""",
            },
			"""
				----------
				1. WARNING in Y.java (at line 1)
					public class Y extends X {
					                       ^
				The type X is deprecated
				----------
				2. WARNING in Y.java (at line 2)
					void foo(){ super.foo(); }
					     ^^^^^
				The method foo() of type Y should be tagged with @Override since it actually overrides a superclass method
				----------
				3. WARNING in Y.java (at line 2)
					void foo(){ super.foo(); }
					                  ^^^^^
				The method foo() from the type X is deprecated
				----------
				4. ERROR in Y.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @Deprecated support
    public void test120() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@Deprecated
					public class X {
						void foo(){}
					}
					""",
                "Y.java",
				"""
					public class Y extends X {
						void foo(){ super.foo(); }
						Zork z;
					}
					""",
            },
			"""
				----------
				1. WARNING in Y.java (at line 1)
					public class Y extends X {
					                       ^
				The type X is deprecated
				----------
				2. WARNING in Y.java (at line 2)
					void foo(){ super.foo(); }
					     ^^^^^
				The method foo() of type Y should be tagged with @Override since it actually overrides a superclass method
				----------
				3. WARNING in Y.java (at line 2)
					void foo(){ super.foo(); }
					                  ^^^^^
				The method foo() from the type X is deprecated
				----------
				4. ERROR in Y.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check missing @Deprecated detection
    public void test121() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					/** @deprecated */
					public class X {
						/** @deprecated */
						public static class Y {
						}
						/** @deprecated */
						int i;
						/** @deprecated */
						public void flag() {}
						void doNotFlag() {}
					  Zork z;
					}\s
					""",
            },
			"""
				----------
				1. WARNING in X.java (at line 2)
					public class X {
					             ^
				The deprecated type X should be annotated with @Deprecated
				----------
				2. WARNING in X.java (at line 4)
					public static class Y {
					                    ^
				The deprecated type X.Y should be annotated with @Deprecated
				----------
				3. WARNING in X.java (at line 7)
					int i;
					    ^
				The deprecated field X.i should be annotated with @Deprecated
				----------
				4. WARNING in X.java (at line 9)
					public void flag() {}
					            ^^^^^^
				The deprecated method flag() of type X should be annotated with @Deprecated
				----------
				5. ERROR in X.java (at line 11)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88446
    public void test122() {
        this.runConformTest(
            new String[] {
                "X.java",
                """
					import java.lang.annotation.Annotation;
					import java.lang.reflect.Method;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;
					class GenericWithInnerAnnotation<T> {
					    @Retention(RetentionPolicy.RUNTIME)
					    @Target(ElementType.METHOD)
					    public @interface MyAnnotation {
					    }
					}
					public class X extends GenericWithInnerAnnotation<Integer> {
					    @MyAnnotation
					    public void aMethod() {
					    }
					   \s
					    public static void main(String[] args) {
					       try {
					           Method method = X.class.getDeclaredMethod("aMethod", new Class[]{});
					           System.out.print(method.getName());
					           Annotation[] annotations = method.getAnnotations();
					           System.out.println(annotations.length);
					       } catch(NoSuchMethodException e) {
					       }
					    }
					}""",
            },
            "aMethod1");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90110
    public void test123() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					class SuperX {
					
					    static void notOverridden() {
					        return;
					    }
					}
					
					public class X extends SuperX {
					
					    static void notOverridden() {
					        return;
					    }
					  Zork z;
					}\s
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 13)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90110 - variation
    public void test124() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					class SuperX {
					
					    void notOverridden() {
					        return;
					    }
					}
					
					public class X extends SuperX {
					
					    void notOverridden() {
					        return;
					    }
					  Zork z;
					}\s
					""",
            },
			"""
				----------
				1. WARNING in X.java (at line 10)
					void notOverridden() {
					     ^^^^^^^^^^^^^^^
				The method notOverridden() of type X should be tagged with @Override since it actually overrides a superclass method
				----------
				2. ERROR in X.java (at line 13)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    public void test125() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					import java.lang.annotation.*;
					
					public class X implements Ann {
					\t
						Ann ann = new X();
						public Class<? extends Annotation>  annotationType() {
							return null;
						}
					}
					
					@interface Ann {}
					
					""",
            },
			"");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=90484 - check no missing @Override warning
    public void test126() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public interface X {
					   Zork z;
					   X clone();
					}
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    public void test127() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					@Deprecated
					public class X {
					   void foo(){}
					}
					""",
                "Y.java",
                """
					public class Y extends X {
					  @SuppressWarnings("all")
					   void foo(){ super.foo(); }
					   Zork z;
					}
					""",
            },
			"""
				----------
				1. WARNING in Y.java (at line 1)
					public class Y extends X {
					                       ^
				The type X is deprecated
				----------
				2. ERROR in Y.java (at line 4)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    public void test128() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					import java.util.List;
					
					public class X {
					    void foo(List list) {
					        List<String> ls1 = list;
					    }
					    @SuppressWarnings({"unchecked", "rawtypes"})
					    void bar(List list) {
					        List<String> ls2 = list;
					    }
					   Zork z;
					}
					""",
            },
            """
				----------
				1. WARNING in X.java (at line 4)
					void foo(List list) {
					         ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 5)
					List<String> ls1 = list;
					                   ^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				3. ERROR in X.java (at line 11)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    public void test129() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					import java.util.*;
					@SuppressWarnings("unchecked")
					public class X {
						void foo() {
							Map<String, String>[] map = new HashMap[10];
						}
					   Zork z;
					}
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 7)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    public void test130() {
    	Map customOptions = new Hashtable();
		String[] warnings = CompilerOptions.warningOptionNames();
		for (int i = 0, ceil = warnings.length; i < ceil; i++) {
			customOptions.put(warnings[i], CompilerOptions.WARNING);
		}
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					  }
					}
					""",
            },
            null,
            customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 1)
					public class X {
					             ^
				Javadoc: Missing comment for public declaration
				----------
				2. WARNING in X.java (at line 2)
					public static void main(String[] args) {
					                   ^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing comment for public declaration
				----------
				3. WARNING in X.java (at line 2)
					public static void main(String[] args) {
				  }
					                                       ^^^^^
				Empty block should be documented
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // check @SuppressWarning support
    public void test131() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					@SuppressWarnings("all")
					public class X {
					  public static void main(String[] args) {
					    Zork z;
					  }
					}
					""",
            },
            """
				----------
				1. ERROR in X.java (at line 4)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    public void test132() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"""
					import java.io.Serializable;
					import java.util.List;
					import java.util.Vector;
					
					public class X {
						public static void main(String[] args) {
							W.deprecated();
							List<X> l = new Vector();
							l.size();
							try {
								// do nothing
							} finally {
								throw new Error();
							}
							// Zork z;
						}
					
						class S implements Serializable {
							String dummy;
						}
					}""",
    			"W.java",
    			"""
					public class W {
						// @deprecated
						@Deprecated
						static void deprecated() {
							// do nothing
						}
					}
					"""
            },
            """
				----------
				1. WARNING in X.java (at line 7)
					W.deprecated();
					  ^^^^^^^^^^^^
				The method deprecated() from the type W is deprecated
				----------
				2. WARNING in X.java (at line 8)
					List<X> l = new Vector();
					            ^^^^^^^^^^^^
				Type safety: The expression of type Vector needs unchecked conversion to conform to List<X>
				----------
				3. WARNING in X.java (at line 8)
					List<X> l = new Vector();
					                ^^^^^^
				Vector is a raw type. References to generic type Vector<E> should be parameterized
				----------
				4. WARNING in X.java (at line 12)
					} finally {
							throw new Error();
						}
					          ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				finally block does not complete normally
				----------
				5. WARNING in X.java (at line 18)
					class S implements Serializable {
					      ^
				The serializable class S does not declare a static final serialVersionUID field of type long
				----------
				""",
    		null,
    		true,
    		null,
    		"java.lang.Error");
    }
    // check @SuppressWarning support
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=89436
    public void test133() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"""
					import java.io.Serializable;
					import java.util.List;
					import java.util.Vector;
					
					@SuppressWarnings( { "deprecation",//$NON-NLS-1$
							"finally",//$NON-NLS-1$
							"rawtypes",//$NON-NLS-1$
							"serial",//$NON-NLS-1$
							"unchecked"//$NON-NLS-1$
					})
					public class X {
						public static void main(String[] args) {
							W.deprecated();
							List<X> l = new Vector();
							l.size();
							try {
								// do nothing
							} finally {
								throw new Error();
							}
						}
					
						class S implements Serializable {
							Zork dummy;
						}
					}""",
    			"W.java",
    			"""
					public class W {
						// @deprecated
						@Deprecated
						static void deprecated() {
							// do nothing
						}
					}
					"""
            },
    		"""
				----------
				1. ERROR in X.java (at line 24)
					Zork dummy;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    public void test134() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"""
					import java.io.Serializable;
					import java.util.List;
					import java.util.Vector;
					
					public class X {
						@SuppressWarnings( { "deprecation",//$NON-NLS-1$
								"finally",//$NON-NLS-1$
								"rawtypes",//$NON-NLS-1$
								"unchecked"//$NON-NLS-1$
						})
						public static void main(String[] args) {
							W.deprecated();
							List<X> l = new Vector();
							l.size();
							try {
								// do nothing
							} finally {
								throw new Error();
							}
						}
					
						@SuppressWarnings({"unchecked", "rawtypes"}//$NON-NLS-1$//$NON-NLS-2$
						)
						List<X> l = new Vector();
					
						@SuppressWarnings("serial"//$NON-NLS-1$
						)
						class S implements Serializable {
							Zork dummy;
						}
					}""",
    			"W.java",
    			"""
					public class W {
						// @deprecated
						@Deprecated
						static void deprecated() {
							// do nothing
						}
					}
					"""
            },
    		"""
				----------
				1. ERROR in X.java (at line 29)
					Zork dummy;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=69505 -- NOT READY YET: "all" only so far, no file support --
    //                                                        hence no import support
    public void test135() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"""
					@SuppressWarnings("all")//$NON-NLS-1$
					import java.util.List;
					
					public class X {
						public static void main(String[] args) {
							if (false) {
								;
							} else {
							}
							Zork z;
						}
					}"""
            },
    		"""
				----------
				1. ERROR in X.java (at line 2)
					import java.util.List;
					^^^^^^
				Syntax error on token "import", package expected
				----------
				2. ERROR in X.java (at line 10)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    // check @SuppressWarning support
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=71968
    public void test136() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"""
					public class X {
						@SuppressWarnings("unused"//$NON-NLS-1$
						)
						private static final String marker = "never used mark"; //$NON-NLS-1$
					
						public static void main(String[] args) {
							Zork z;
						}
					}"""
            },
			"""
				----------
				1. ERROR in X.java (at line 7)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
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
    			"""
					import java.io.Serializable;
					import java.util.List;
					import java.util.Vector;
					
					@SuppressWarnings("all")
					public class X {
						public static void main(String[] args) {
							W.deprecated();
							List<X> l = new Vector();
							l.size();
							try {
								// do nothing
							} finally {
								throw new Error();
							}
							Zork z;
						}
					
						class S implements Serializable {
							String dummy;
						}
					}""",
    			"W.java",
    			"""
					public class W {
						// @deprecated
						@Deprecated
						static void deprecated() {
							// do nothing
						}
					}
					"""
            },
            """
				----------
				1. WARNING in X.java (at line 6)
					public class X {
					             ^
				Javadoc: Missing comment for public declaration
				----------
				2. WARNING in X.java (at line 7)
					public static void main(String[] args) {
					                   ^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing comment for public declaration
				----------
				3. WARNING in X.java (at line 8)
					W.deprecated();
					  ^^^^^^^^^^^^
				The method deprecated() from the type W is deprecated
				----------
				4. WARNING in X.java (at line 9)
					List<X> l = new Vector();
					            ^^^^^^^^^^^^
				Type safety: The expression of type Vector needs unchecked conversion to conform to List<X>
				----------
				5. WARNING in X.java (at line 9)
					List<X> l = new Vector();
					                ^^^^^^
				Vector is a raw type. References to generic type Vector<E> should be parameterized
				----------
				6. ERROR in X.java (at line 16)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				7. WARNING in X.java (at line 19)
					class S implements Serializable {
					      ^
				The serializable class S does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in W.java (at line 1)
					public class W {
					             ^
				Javadoc: Missing comment for public declaration
				----------
				""",
			null, true, customOptions);
    }
    // check @SuppressWarning support
    public void test138() {
    	Map customOptions = new Hashtable();
    	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
        this.runNegativeTest(

            new String[] {
                "X.java",
    			"""
					@SuppressWarnings("zork")//$NON-NLS-1$
					public class X {
						Zork z;
					}
					"""
            },
    		"""
				----------
				1. WARNING in X.java (at line 1)
					@SuppressWarnings("zork")//$NON-NLS-1$
					                  ^^^^^^
				Unsupported @SuppressWarnings("zork")
				----------
				2. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null, true, customOptions);
    }
    // check @SuppressWarning support
    public void test139() {
    	Map customOptions = new Hashtable();
    	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
        this.runNegativeTest(

            new String[] {
                "X.java",
    			"""
					@SuppressWarnings({"zork", "warningToken"})//$NON-NLS-1$//$NON-NLS-2$
					public class X {
						Zork z;
					}
					"""
            },
    		"""
				----------
				1. WARNING in X.java (at line 1)
					@SuppressWarnings({"zork", "warningToken"})//$NON-NLS-1$//$NON-NLS-2$
					                   ^^^^^^
				Unsupported @SuppressWarnings("zork")
				----------
				2. WARNING in X.java (at line 1)
					@SuppressWarnings({"zork", "warningToken"})//$NON-NLS-1$//$NON-NLS-2$
					                           ^^^^^^^^^^^^^^
				Unsupported @SuppressWarnings("warningToken")
				----------
				3. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90111 - variation
    public void test140() {
    	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
		?	"""
			----------
			1. ERROR in X.java (at line 6)
				static void foo(){}\t
				            ^^^^^
			The method foo() of type Bar must override a superclass method
			----------
			"""
		:	"""
			----------
			1. ERROR in X.java (at line 6)
				static void foo(){}\t
				            ^^^^^
			The method foo() of type Bar must override or implement a supertype method
			----------
			""";
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
					  static void foo(){}
					}
					class Bar extends X {
					  @Override
					  static void foo(){}\t
					}
					
					"""
            },
            expectedOutput,
            JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94867
    public void test141() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface X1 {
						Class<? extends Throwable>[] expected1() default {};
						Class<? super Throwable>[] expected2() default {};
						Class<?>[] expected3() default {};
					}
					
					public class X {
						@X1(expected1=Throwable.class, expected2={})
						public static void main(String[] args) {
						\t
						}
						void foo() {
							Class<? extends Throwable>[] c1 = {};
							Class<? super Throwable>[] c2 = {};
							Class<?>[] c3 = {};
						}
					}
					"""
            },
			"""
				----------
				1. ERROR in X.java (at line 13)
					Class<? extends Throwable>[] c1 = {};
					                                  ^^
				Cannot create a generic array of Class<? extends Throwable>
				----------
				2. ERROR in X.java (at line 14)
					Class<? super Throwable>[] c2 = {};
					                                ^^
				Cannot create a generic array of Class<? super Throwable>
				----------
				""");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94308
    public void test142() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@SuppressWarnings("deprecation")
					public class X extends p.OldStuff {
						/**
						 * @see p.OldStuff#foo()
						 */
						@Override
						public void foo() {
							super.foo();
						}
					}
					""",
                "p/OldStuff.java",
                """
					package p;
					@Deprecated
					public class OldStuff {
						public void foo() {
						}\t
					  Zork z;
					}
					""",
            },
			"""
				----------
				1. ERROR in p\\OldStuff.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true,
			null);
    }
    public void test142b() {
		Map raiseInvalidJavadocSeverity =
			new HashMap(2);
		raiseInvalidJavadocSeverity.put(
				CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		// admittingly, when these are errors, SuppressWarnings is not enough to
		// filter them out *but* the deprecation level being WARNING, we get them
		// out anyway
	    this.runNegativeTest(
	        new String[] {
	            "X.java",
				"""
					@SuppressWarnings("deprecation")
					public class X extends p.OldStuff {
						/**
						 * @see p.OldStuff#foo()
						 */
						@Override
						public void foo() {
							super.foo();
						}
					}
					""",
	            "p/OldStuff.java",
	            """
					package p;
					@Deprecated
					public class OldStuff {
						public void foo() {
						}\t
					  Zork z;
					}
					""",
	        },
			"""
				----------
				1. ERROR in p\\OldStuff.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true,
			raiseInvalidJavadocSeverity);
	}
// check that @SuppressWarning is reported as unused when corresponding warning is moved from
// warning to error
public void test142c() {
	Map raiseDeprecationReduceInvalidJavadocSeverity =
		new HashMap(2);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
    this.runNegativeTest(
    	true,
        new String[] {
            "X.java",
			"""
				@SuppressWarnings("deprecation")
				public class X extends p.OldStuff {
					/**
					 * @see p.OldStuff#foo()
					 */
					@Override
					public void foo() {
						super.foo();
					}
				}
				""",
            "p/OldStuff.java",
            """
				package p;
				@Deprecated
				public class OldStuff {
					public void foo() {
					}\t
				}
				""",
        },
        null,
        raiseDeprecationReduceInvalidJavadocSeverity,
        """
			----------
			1. WARNING in X.java (at line 1)
				@SuppressWarnings("deprecation")
				                  ^^^^^^^^^^^^^
			Unnecessary @SuppressWarnings("deprecation")
			----------
			2. ERROR in X.java (at line 2)
				public class X extends p.OldStuff {
				                         ^^^^^^^^
			The type OldStuff is deprecated
			----------
			3. ERROR in X.java (at line 8)
				super.foo();
				      ^^^^^
			The method foo() from the type OldStuff is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test143() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X extends p.OldStuff {
					@SuppressWarnings("all")
					public void foo() {
						super.foo();
					}
				}
				""",
            "p/OldStuff.java",
            """
				package p;
				@Deprecated
				public class OldStuff {
					public void foo() {
					}\t
				  Zork z;
				}
				""",
        },
		"""
			----------
			1. WARNING in X.java (at line 1)
				public class X extends p.OldStuff {
				                         ^^^^^^^^
			The type OldStuff is deprecated
			----------
			----------
			1. ERROR in p\\OldStuff.java (at line 6)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
    public void test144() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					import java.util.*;
					public class X {
						Zork z;
						@SuppressWarnings("all") \s
						public static class EverythingWrong {
							private EverythingWrong() {}
							@BeforeClass public void notStaticBC() {}
							@BeforeClass static void notPublicBC() {}
							@BeforeClass public static int nonVoidBC() { return 0; }
							@BeforeClass public static void argumentsBC(int i) {}
							@BeforeClass public static void fineBC() {}
							@AfterClass public void notStaticAC() {}
							@AfterClass static void notPublicAC() {}
							@AfterClass public static int nonVoidAC() { return 0; }
							@AfterClass public static void argumentsAC(int i) {}
							@AfterClass public static void fineAC() {}
							@After public static void staticA() {}
							@After void notPublicA() {}
							@After public int nonVoidA() { return 0; }
							@After public void argumentsA(int i) {}
							@After public void fineA() {}
							@Before public static void staticB() {}
							@Before void notPublicB() {}
							@Before public int nonVoidB() { return 0; }
							@Before public void argumentsB(int i) {}
							@Before public void fineB() {}
							@Test public static void staticT() {}
							@Test void notPublicT() {}
							@Test public int nonVoidT() { return 0; }
							@Test public void argumentsT(int i) {}
							@Test public void fineT() {}
						}
						@Test public void testFailures() throws Exception {
							List<Exception> problems= new TestIntrospector(EverythingWrong.class).validateTestMethods();
							int errorCount= 1 + 4 * 5; // missing constructor plus four invalid methods for each annotation */
							assertEquals(errorCount, problems.size());
						}
						public static junit.framework.Test suite() {
							return null; // new JUnit4TestAdapter(TestMethodTest.class);
						}
						void assertEquals(int i, int j) {
						}
					}
					@interface BeforeClass {}
					@interface AfterClass {}
					@interface Test {}
					@interface After {}
					@interface Before {}
					class TestIntrospector {
						TestIntrospector(Class c) {}
						List validateTestMethods() { return null; }
					}
					""",
            },
            """
				----------
				1. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				2. WARNING in X.java (at line 34)
					List<Exception> problems= new TestIntrospector(EverythingWrong.class).validateTestMethods();
					                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<Exception>
				----------
				3. ERROR in X.java (at line 38)
					public static junit.framework.Test suite() {
					              ^^^^^
				junit cannot be resolved to a type
				----------
				4. WARNING in X.java (at line 50)
					TestIntrospector(Class c) {}
					                 ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				5. WARNING in X.java (at line 51)
					List validateTestMethods() { return null; }
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=89937
    public void test145() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@interface Annot {
					  int foo();
					  int bar();
					}
					
					public class X {
					  static final int yyy = 0;
					  @Annot(foo=zzz, bar = yyy)
					  static final int zzz = 0;
					}
					
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 8)
					@Annot(foo=zzz, bar = yyy)
					           ^^^
				Cannot reference a field before it is defined
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96631
    public void test146() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					@SuppressWarnings(value={})
					public class X {
					}
					""",
            },
			"");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96974
    public void test147() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@SuppressWarnings({"nls"})
					public class X<T> {
						 String test= "";
					}""",
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
				"""
					public class X {
						private static void foo() {
							 @interface Bar {
								public String bar = "BUG";
							}
						}
					}""",
            },
            """
				----------
				1. ERROR in X.java (at line 3)
					@interface Bar {
					           ^^^
				The member annotation Bar can only be defined inside a top-level class or interface or in a static context
				----------
				""");
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96991
    public void test149() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
						void bar() {
							@Annot(foo = zzz)
							final int zzz = 0;
					
							@Annot(foo = kkk)
							int kkk = 1;
					
						}
						@Annot(foo = fff)
						final int fff = 0;
					\t
						@Annot(foo = Member.ttt)
						static class Member {
							final static int ttt = 2;
						}
					}
					@interface Annot {
						int foo();
					}
					""",
            },
			"""
				----------
				1. ERROR in X.java (at line 6)
					@Annot(foo = kkk)
					             ^^^
				The value for annotation attribute Annot.foo must be a constant expression
				----------
				2. ERROR in X.java (at line 10)
					@Annot(foo = fff)
					             ^^^
				Cannot reference a field before it is defined
				----------
				""");
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=98091
    public void test150() {
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
				"@SuppressWarnings(\"assertIdentifier\")\n" +
				"class X {}",
            },
    		"""
				----------
				1. WARNING in X.java (at line 1)
					@SuppressWarnings("assertIdentifier")
					                  ^^^^^^^^^^^^^^^^^^
				Unsupported @SuppressWarnings("assertIdentifier")
				----------
				""",
    		null, null,
    		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test151() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@SuppressWarnings({"boxing"})
					public class X {
						 static void foo(int i) {}
						 public static void main(String[] args) {
							foo(Integer.valueOf(0));
						 }
					}""",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test152() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@SuppressWarnings({"boxing"})
					public class X {
						 static void foo(Integer i) {}
						 public static void main(String[] args) {
							foo(0);
						 }
					}""",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test153() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
        this.runConformTest(
            new String[] {
                "X.java",
                """
					enum E { A, B, C }
					public class X {
					    @SuppressWarnings({"incomplete-switch"})
						 public static void main(String[] args) {
							for (E e : E.values()) {
								switch(e) {
									case A :
										System.out.println(e);
									break;
								}
							}
						 }
					}""",
            },
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test154() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
						 static int i;
					    @SuppressWarnings({"hiding"})
						 public static void main(String[] args) {
							for (int i = 0, max = args.length; i < max; i++) {
								System.out.println(args[i]);
							}
						 }
					}""",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test155() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					@SuppressWarnings({"hiding"})
					public class X {\t
						{ int x =\s
							new Object() { \t
								int foo() {\t
									int x = 0;
									return x;\t
								}\t
							}.foo();\t
						}\t
					}
					""",
           },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test156() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
	   			"""
					class T {}
					@SuppressWarnings({"hiding"})
					public class X<T> {
					}
					""",
           },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test157() {
    	Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.WARNING);
        runner.testFiles =
            new String[] {
                "X.java",
    			"""
					public class X {
					   @SuppressWarnings({"hiding"})
						public static void main(String[] args) {
							try {
								throw new BX();
							} catch(BX e) {
							} catch(AX e) {
							}
						}
					}\s
					@SuppressWarnings({"serial"})
					class AX extends Exception {}
					@SuppressWarnings({"serial"})
					class BX extends AX {}
					"""
            };
        runner.javacTestOptions = JavacTestOptions.SKIP; // javac doesn't support @SW("hiding") here, see test157b
        runner.runConformTest();
    }
    public void test157b() {
    	Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.WARNING);
        runner.testFiles =
            new String[] {
                "X.java",
    			"""
					public class X {
						public static void main(String[] args) {
							try {
								throw new BX();
							} catch(BX e) {
							} catch(AX e) {
							}
						}
					}\s
					@SuppressWarnings({"serial"})
					class AX extends Exception {}
					@SuppressWarnings({"serial"})
					class BX extends AX {}
					"""
            };
        runner.expectedCompilerLog =
        		"""
					----------
					1. WARNING in X.java (at line 6)
						} catch(AX e) {
						        ^^
					Unreachable catch block for AX. Only more specific exceptions are thrown and they are handled by previous catch block(s).
					----------
					""";
        runner.runWarningTest();
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test158() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"""
					public class X {
					   @SuppressWarnings({"finally"})
						public static void main(String[] args) {
							try {
								throw new AX();
							} finally {
								return;
							}
						}
					}\s
					@SuppressWarnings({"serial"})
					class AX extends Exception {}"""
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test159() {
    	Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.WARNING);
        runner.testFiles =
            new String[] {
                "X.java",
				"""
					@SuppressWarnings({"static-access"})
					public class X extends XZ {
					\t
						void foo() {
							int j = X.S;
							int k = super.S;
							int l = XZ.S;
							int m = XY.S;
						\t
							bar();
							X.bar();
							XY.bar();
							XZ.bar();
						}
					}
					class XY {
						static int S = 10;
						static void bar(){}
					}
					class XZ extends XY {
					}"""
            };
        runner.javacTestOptions = JavacTestOptions.SKIP; // only testing Eclipse-specific @SW
        runner.runConformTest();
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test160() {
    	Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.WARNING);
        runner.testFiles =
            new String[] {
                "X.java",
				"""
					@SuppressWarnings("static-access")
					public class X {
						void foo() {
							int m = new XY().S;
						}
					}
					class XY {
						static int S = 10;
					}"""
            };
        runner.javacTestOptions = JavacTestOptions.SKIP; // only testing Eclipse-specific @SW
        runner.runConformTest();
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test161() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					@SuppressWarnings("unqualified-field-access")
					public class X {
						int i;
						int foo() {
							return i;
						}
					}"""
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test162() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					@SuppressWarnings({"unchecked", "rawtypes"})
					public class X<T> {
					   \s
					    public static void main(String[] args) {
					        AX ax = new AX();
					        AX ax2 = ax.p;
					        ax.p = new AX<String>();
					        ax.q = new AX<String>();
					        ax.r = new AX<Object>();
					        System.out.println(ax2);
					    }
					}
					
					class AX <P> {
					    AX<P> p;
					    AX<Object> q;
					    AX<String> r;
					    BX<String> s;
					}
					
					class BX<Q> {
					}
					""",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test163() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					import java.io.*;
					@SuppressWarnings("unused")
					public class X<T> {
					   \s
					    public void foo(int i) throws java.io.IOException {
					       int j = 0;
							class C {
								private void bar() {}
							}
					    }
					}""",
				"Y.java", // =================
				"public class Y extends Zork {\n" +
				"}\n", // =================
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					public class Y extends Zork {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test164() {
    	String[] testFiles = new String[] {
                "X.java",
                """
					@SuppressWarnings({"synthetic-access", "unused"})
					public class X {
					    private int i;
						 private void bar() {}
					    public void foo() {
					       class C {
								private void bar() {
									System.out.println(i);
									i = 0;
									bar();
								}
							};
							new C().bar();
					    }
					}"""
        };
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
		if (isMinimumCompliant(ClassFileConstants.JDK11)) { // no synthetic due to nestmate
			this.runConformTest(testFiles);
			return;
		}
		this.runNegativeTest(
            testFiles,
            "",
			null,
			true,
			options
		);
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test165() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDeprecatedAnnotation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
	    this.runConformTest(
	    	true,
            new String[] {
                "X.java",
				"""
					/**
					 * @see Y
					 */
					@SuppressWarnings("deprecation")
					public class X extends Y {
						 /**
						  * @see Y#foo()
						  * @see Y#j
						  */
					    public void foo() {
							super.foo();
					    }
					}""",
				"Y.java",
				"""
					/**
					 * @deprecated
					 */
					public class Y {
						/**
						 * @deprecated
						 */
						public void foo() {}
						/**
						 * @deprecated
						 */
						public int j;
					}"""
            },
            null,
            options,
            "",
			null, null,
			JavacTestOptions.SKIP /* suppressed deprecation related warnings */
		);
    }

    // check array handling of singleton
	public void test166() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Inherited;
					
					@Retention(RetentionPolicy.RUNTIME)
					@Inherited()
					@interface ParameterAnnotation {
						String value() default "Default";
					}
					@interface ClassAnnotation {
						String value() default "Default";
					}
					
					enum EnumClass{
						Value1, Value2, Value3
					}
					
					@Retention(RetentionPolicy.RUNTIME)
					@Inherited()
					@interface ValueAnnotation {
						String value() default "Default";
						boolean booleanValue() default true;
						char charValue() default 'q';
						byte byteValue() default 123;
						short shortValue() default 12345;
						int intValue() default 1234567890;
						float floatValue() default 12345.6789f;
						double doubleValue() default 12345.6789;
						long longValue() default 1234567890123456789l;
						String stringValue() default "stringValue";
						EnumClass enumValue() default EnumClass.Value1;
						Class classValue() default EnumClass.class;
						ClassAnnotation annotationValue() default @ClassAnnotation();
						boolean[] booleanArrayValue() default {true, false};
						char[] charArrayValue() default {'q', 'm'};
						byte[] byteArrayValue() default {123, -123};
						short[] shortArrayValue() default {12345, -12345};
						int[] intArrayValue() default {1234567890, -1234567890};
						float[] floatArrayValue() default {12345.6789f, -12345.6789f};
						double[] doubleArrayValue() default {12345.6789, -12345.6789};
						long[] longArrayValue() default {1234567890123456789l, -1234567890123456789l};
						String[] stringArrayValue() default {"stringValue", "valueString"};
						EnumClass[] enumArrayValue() default {EnumClass.Value1, EnumClass.Value2};
						Class[] classArrayValue() default {X.class, EnumClass.class};
						ClassAnnotation[] annotationArrayValue() default {@ClassAnnotation(), @ClassAnnotation()};
					}
					
					public class X {
						@ValueAnnotation(
							value="ValueAnnotation",
							booleanValue=true,
							charValue='m',
							byteValue=-123,
							shortValue=-12345,
							intValue=-1234567890,
							floatValue=-12345.6789f,
							doubleValue=-12345.6789,
							longValue=-1234567890123456789l,
							stringValue="valueString",
							enumValue=EnumClass.Value3,
							classValue=X.class,
							annotationValue=@ClassAnnotation(value="ClassAnnotation"),
							booleanArrayValue={
								false,
								true
							},
							charArrayValue={
								'm',
								'q'
							},
							byteArrayValue={
								-123,
								123
							},
							shortArrayValue={
								-12345,
								12345
							},
							intArrayValue={
								-1234567890,
								1234567890
							},
							floatArrayValue={
								-12345.6789f,
								12345.6789f
							},
							doubleArrayValue={
								-12345.6789,
								12345.6789
							},
							longArrayValue={
								-1234567890123456789l,
								1234567890123456789l
							},
							stringArrayValue={
								"valueString",
								"stringValue"
							},
							enumArrayValue={
								EnumClass.Value2,
								EnumClass.Value1
							},
							classArrayValue={
								EnumClass.class,
								X.class
							},
							annotationArrayValue={
								@ClassAnnotation(value="ClassAnnotation1"),
								@ClassAnnotation(value="ClassAnnotation2")
							})
						public String field;
						@ValueAnnotation(
							value="ValueAnnotation",
							booleanValue=true,
							charValue='m',
							byteValue=-123,
							shortValue=-12345,
							intValue=-1234567890,
							floatValue=-12345.6789f,
							doubleValue=-12345.6789,
							longValue=-1234567890123456789l,
							stringValue="valueString",
							enumValue=EnumClass.Value3,
							classValue=X.class,
							annotationValue=@ClassAnnotation(value="ClassAnnotation"),
							booleanArrayValue={
								false,
								true
							},
							charArrayValue={
								'm',
								'q'
							},
							byteArrayValue={
								-123,
								123
							},
							shortArrayValue={
								-12345,
								12345
							},
							intArrayValue={
								-1234567890,
								1234567890
							},
							floatArrayValue={
								-12345.6789f,
								12345.6789f
							},
							doubleArrayValue={
								-12345.6789,
								12345.6789
							},
							longArrayValue={
								-1234567890123456789l,
								1234567890123456789l
							},
							stringArrayValue={
								"valueString",
								"stringValue"
							},
							enumArrayValue={
								EnumClass.Value2,
								EnumClass.Value1
							},
							classArrayValue={
								EnumClass.class,
								X.class
							},
							annotationArrayValue={
								@ClassAnnotation(value="ClassAnnotation1"),
								@ClassAnnotation(value="ClassAnnotation2")
							})
						public X(@ParameterAnnotation(value="ParameterAnnotation") @Deprecated() String param1, @ParameterAnnotation(value="ParameterAnnotation") String param2) {
						}
						@ValueAnnotation(
							value="ValueAnnotation",
							booleanValue=true,
							charValue='m',
							byteValue=-123,
							shortValue=-12345,
							intValue=-1234567890,
							floatValue=-12345.6789f,
							doubleValue=-12345.6789,
							longValue=-1234567890123456789l,
							stringValue="valueString",
							enumValue=EnumClass.Value3,
							classValue=X.class,
							annotationValue=@ClassAnnotation(value="ClassAnnotation"),
							booleanArrayValue={
								false,
								true
							},
							charArrayValue={
								'm',
								'q'
							},
							byteArrayValue={
								-123,
								123
							},
							shortArrayValue={
								-12345,
								12345
							},
							intArrayValue={
								-1234567890,
								1234567890
							},
							floatArrayValue={
								-12345.6789f,
								12345.6789f
							},
							doubleArrayValue={
								-12345.6789,
								12345.6789
							},
							longArrayValue={
								-1234567890123456789l,
								1234567890123456789l
							},
							stringArrayValue={
								"valueString",
								"stringValue"
							},
							enumArrayValue={
								EnumClass.Value2,
								EnumClass.Value1
							},
							classArrayValue={
								EnumClass.class,
								X.class
							},
							annotationArrayValue={
								@ClassAnnotation(value="ClassAnnotation1"),
								@ClassAnnotation(value="ClassAnnotation2")
							})
						public void method(@ParameterAnnotation(value="ParameterAnnotation") @Deprecated() String param1, @ParameterAnnotation(value="ParameterAnnotation") String param2){
						}
					}"""
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		final byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		new ClassFileReader(classFileBytes, "X.java".toCharArray(), true);
		disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99469
	public void test167() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public foo(@Deprecated() String s) {
						}
					}
					""",
			},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public foo(@Deprecated() String s) {
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Return type for the method is missing
			----------
			""");
    }
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=94759
    public void test168() {
    	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
			?	"""
				----------
				1. ERROR in X.java (at line 2)
					@Override I clone();
					            ^^^^^^^
				The method clone() of type I must override a superclass method
				----------
				2. ERROR in X.java (at line 7)
					@Override void foo();
					               ^^^^^
				The method foo() of type J must override a superclass method
				----------
				"""
			:	"""
				----------
				1. ERROR in X.java (at line 2)
					@Override I clone();
					            ^^^^^^^
				The method clone() of type I must override or implement a supertype method
				----------
				""";
    	this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					interface I {
						@Override I clone();
						void foo();
					}
					
					interface J extends I {
						@Override void foo();
					}
					""",
           },
           expectedOutput);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test169() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
    			"""
					@SuppressWarnings("serial")
					public class X extends Exception {
						String s = "Hello";\s
					}"""
            },
            null,
            customOptions,
            """
				----------
				1. WARNING in X.java (at line 3)
					String s = "Hello";\s
					           ^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test170() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
            new String[] {
                "X.java",
    			"""
					public class X extends Exception {
					   @SuppressWarnings("nls")
						String s = "Hello";\s
					}"""
            },
    		"",
			null, true, null, customOptions, null);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test171() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" +
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" + 	// no nls-warning here
    			"	String s2 = \"Hello2\"; \n" +		// but an nls-warning here
    			"}"
            },
            null, customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 1)
					public class X extends Exception {
					             ^
				The serializable class X does not declare a static final serialVersionUID field of type long
				----------
				2. WARNING in X.java (at line 4)
					@SuppressWarnings("serial")
					                  ^^^^^^^^
				Unnecessary @SuppressWarnings("serial")
				----------
				3. WARNING in X.java (at line 5)
					String s2 = "Hello2";\s
					            ^^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
    		null, null, JavacTestOptions.SKIP); // nls-warnings are specific to Eclipse - special-casing this special case is irrelevant for javac
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test172() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
        	new String[] {
                "X.java",
    			"""
					@SuppressWarnings("serial")
					public class X extends Exception {
					   @SuppressWarnings("nls")
						String s = "Hello";\s
					   @SuppressWarnings("serial")
						String s2 = "Hello2";\s
					}"""
            },
            null, customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 5)
					@SuppressWarnings("serial")
					                  ^^^^^^^^
				Unnecessary @SuppressWarnings("serial")
				----------
				2. WARNING in X.java (at line 6)
					String s2 = "Hello2";\s
					            ^^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test173() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
    			"""
					@interface Annot {
					    String value() default "NONE";
					}
					@Annot("serial")
					public class X extends Exception {
					   @SuppressWarnings("nls")
						String s = "Hello";\s
					   @SuppressWarnings("serial")
						String s2 = "Hello2";\s
					}"""
            },
            null,
            customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 5)
					public class X extends Exception {
					             ^
				The serializable class X does not declare a static final serialVersionUID field of type long
				----------
				2. WARNING in X.java (at line 8)
					@SuppressWarnings("serial")
					                  ^^^^^^^^
				Unnecessary @SuppressWarnings("serial")
				----------
				3. WARNING in X.java (at line 9)
					String s2 = "Hello2";\s
					            ^^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
			null, null, JavacTestOptions.SKIP); // nls-warnings are specific to Eclipse - special-casing this special case is irrelevant for javac
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test174() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = """
			@interface Annot {
			    int value() default 0;
			}
			@interface Annot2 {
			    String value();
			}
			@Annot(value=5)
			public class X {
			   @Annot2(value="nls")
				String s = null;\s
			   @SuppressWarnings("serial")
				String s2 = "Hello2";\s
			}""";
		this.runConformTest(
			true,
            new String[] {
                "X.java",
    			source
            },
            null, customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 11)
					@SuppressWarnings("serial")
					                  ^^^^^^^^
				Unnecessary @SuppressWarnings("serial")
				----------
				2. WARNING in X.java (at line 12)
					String s2 = "Hello2";\s
					            ^^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test175() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = """
			@interface Annot {
			    int value() default 0;
			}
			@interface Annot2 {
			    String value();
			}
			@Annot(value=5)
			public class X {
			   @Annot2(value="nls") String s = "value";\s
			   @SuppressWarnings("serial")
				String s2 = "Hello2";\s
			}""";
		this.runConformTest(
			true,
            new String[] {
                "X.java",
    			source
            },
            null, customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 9)
					@Annot2(value="nls") String s = "value";\s
					                                ^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				2. WARNING in X.java (at line 10)
					@SuppressWarnings("serial")
					                  ^^^^^^^^
				Unnecessary @SuppressWarnings("serial")
				----------
				3. WARNING in X.java (at line 11)
					String s2 = "Hello2";\s
					            ^^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test176() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = """
			@interface Annot {
			    int value() default 0;
			}
			@interface Annot2 {
			    String value();
			}
			@Annot(value=5)
			public class X {
			   @Annot2(value="nls") String s = "value";\s
			   @SuppressWarnings({"serial", "nls"})
				String s2 = "Hello2";\s
				@Annot(value=5) void foo() {}
			}""";
		this.runConformTest(
			true,
            new String[] {
                "X.java",
    			source
            },
            null, customOptions,
    		"""
				----------
				1. WARNING in X.java (at line 9)
					@Annot2(value="nls") String s = "value";\s
					                                ^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				2. WARNING in X.java (at line 10)
					@SuppressWarnings({"serial", "nls"})
					                   ^^^^^^^^
				Unnecessary @SuppressWarnings("serial")
				----------
				""",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=108263
    public void test177() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					public @interface X {
					  public static final Integer foo = B.zzz;\s
					  public static final int foo3 = B.zzz2;\s
					}
					class B {
					  public static final Integer zzz = new Integer(0);
					  public static final int zzz2 = 0;
					}
					""",
           },
		"");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=111076
    public void test178() {
        runConformTest(
        	true,
            new String[] {
                "X.java",
    			"""
					import java.util.*;
					public class X {
						private void testme(boolean check) {
							ArrayList<Integer> aList = new ArrayList<Integer>();
							for (@SuppressWarnings("unusedLocal")
							Integer i : aList) {
								System.out.println("checking");
							}
						}
					}
					""",
           },
        null,
		"",
		null,
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=112433
    public void test179() {
    	this.runConformTest(
    		true,
    		new String[] {
    			"X.java",
    			"""
					import static java.lang.annotation.ElementType.*;
					import static java.lang.annotation.RetentionPolicy.*;
					import java.lang.annotation.Retention;
					import java.lang.annotation.Target;
					@Target({TYPE, FIELD, METHOD,
					         PARAMETER, CONSTRUCTOR,
					         LOCAL_VARIABLE, PACKAGE,})
					@Retention(CLASS)
					public @interface X {}"""
    		},
    		"",
    		"",
    		null,
    		JavacTestOptions.JavacHasABug.JavacBug6337964);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=116028
    public void test180() {
    	this.runConformTest(
    		new String[] {
    			"X.java",
    			"""
					import java.lang.reflect.Field;
					
					public class X {
					  @Deprecated public static Object x, y, z;
					
					  public static void main(String[] args) {
					    Class c = X.class;
					    int counter = 0;
					    for (Field f : c.getFields()) {
					      counter += f.getDeclaredAnnotations().length;
					    }
					    System.out.print(counter);
					  }
					}"""
    		},
    		"3");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=116028
    public void test181() {
    	this.runConformTest(
    		new String[] {
    			"X.java",
    			"""
					import java.lang.reflect.Field;
					
					public class X {
					  public static Object x, y, z;
					
					  public static void main(String[] args) {
					    Class c = X.class;
					    int counter = 0;
					    for (Field f : c.getFields()) {
					      counter += f.getDeclaredAnnotations().length;
					    }
					    System.out.print(counter);
					  }
					}"""
    		},
    		"0");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=110593
    public void test182() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"""
						public class X {
							void foo(Y y) {
								y.initialize(null, null, null);
							}
						}
						
						
						""", // =================
    				"Y.java", // =================
    				"""
						public class Y {
						
							/**
							 * @deprecated
							 */
							public void initialize(Zork z, String s) {
							}
						
							public void initialize(Zork z, String s, Thread t) {
							}
						}
						
						
						""", // =================
    		},
    		"""
				----------
				1. ERROR in X.java (at line 3)
					y.initialize(null, null, null);
					  ^^^^^^^^^^
				The method initialize(Zork, String, Thread) from the type Y refers to the missing type Zork
				----------
				----------
				1. WARNING in Y.java (at line 6)
					public void initialize(Zork z, String s) {
					            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The deprecated method initialize(Zork, String) of type Y should be annotated with @Deprecated
				----------
				2. ERROR in Y.java (at line 6)
					public void initialize(Zork z, String s) {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in Y.java (at line 6)
					public void initialize(Zork z, String s) {
					                            ^
				Javadoc: Missing tag for parameter z
				----------
				4. ERROR in Y.java (at line 6)
					public void initialize(Zork z, String s) {
					                                      ^
				Javadoc: Missing tag for parameter s
				----------
				5. ERROR in Y.java (at line 9)
					public void initialize(Zork z, String s, Thread t) {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=110593 - variation
    public void test183() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"""
						public class X {
							void foo(Y y) {
								int i = y.initialize;
							}
						}
						
						""", // =================
    				"Y.java", // =================
    				"""
						public class Y {
						
							/**
							 * @deprecated
							 */
							public Zork initialize;
						}
						
						""", // =================
    		},
    		"""
				----------
				1. ERROR in X.java (at line 3)
					int i = y.initialize;
					        ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. WARNING in X.java (at line 3)
					int i = y.initialize;
					          ^^^^^^^^^^
				The field Y.initialize is deprecated
				----------
				----------
				1. ERROR in Y.java (at line 6)
					public Zork initialize;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				2. WARNING in Y.java (at line 6)
					public Zork initialize;
					            ^^^^^^^^^^
				The deprecated field Y.initialize should be annotated with @Deprecated
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=110593 - variation
    public void test184() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"""
						public class X {
							void foo() {
								Y.initialize i;
							}
						}
						
						
						""", // =================
    				"Y.java", // =================
    				"""
						public class Y {
						
							/**
							 * @deprecated
							 */
							public class initialize extends Zork {
							}
						}
						
						
						""", // =================
    		},
    		"""
				----------
				1. WARNING in X.java (at line 3)
					Y.initialize i;
					  ^^^^^^^^^^
				The type Y.initialize is deprecated
				----------
				----------
				1. WARNING in Y.java (at line 6)
					public class initialize extends Zork {
					             ^^^^^^^^^^
				The deprecated type Y.initialize should be annotated with @Deprecated
				----------
				2. ERROR in Y.java (at line 6)
					public class initialize extends Zork {
					                                ^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=123522
    public void test185() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"""
						import p.A;
						@SuppressWarnings("all")
						public class X {
							void foo(A a) {
								Zork z;
							}
						}
						
						class Y {
							A a;
						}
						""", // =================
    				"p/A.java", // =================
    				"""
						package p;
						@Deprecated
						public class A {
						}
						""", // =================
    		},
    		"""
				----------
				1. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				2. WARNING in X.java (at line 10)
					A a;
					^
				The type A is deprecated
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=124346
    public void test186() {
    	this.runNegativeTest(
    		new String[] {
    				"p1/X.java", // =================
    				"""
						package p1;
						public class X {
							@Deprecated
							class Y implements p2.I {
								Zork z;
							}
						}
						""", // =================
    				"p2/I.java", // =================
    				"""
						package p2;
						@Deprecated
						public interface I {
						}
						""", // =================
    		},
    		"""
				----------
				1. ERROR in p1\\X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=124346 - variation
    public void test187() {
    	this.runNegativeTest(
    		new String[] {
    				"p1/X.java", // =================
    				"""
						package p1;
						import p2.I;
						@Deprecated
						public class X {
							Zork z;
						}
						""", // =================
    				"p2/I.java", // =================
    				"""
						package p2;
						@Deprecated
						public interface I {
						}
						""", // =================
    		},
    		"""
				----------
				1. ERROR in p1\\X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=126332
    public void test188() {
    	this.runNegativeTest(
    		new String[] {
				"X.java",
				"""
					@interface A1 {
						int[] values();
					}
					@A1(values = new int[] { 1, 2 })
					public class X {
						public static void main(String[] args) {
						}
					}""",
    		},
    		"""
				----------
				1. ERROR in X.java (at line 4)
					@A1(values = new int[] { 1, 2 })
					             ^^^^^^^^^^^^^^^^^^
				The value for annotation attribute A1.values must be an array initializer
				----------
				""");
    }
    // partial recompile - keep a binary
	public void test189() {
		this.runConformTest(
			true,
			new String[] {
				"A1.java",
				"""
					@A2(@A1(m1 = "u"))
					public @interface A1 {
					  String m1();
					  String m2() default "v";
					}
					""",
				"A2.java",
				"""
					@A2(@A1(m1 = "u", m2 = "w"))
					public @interface A2 {
					  A1[] value();
					}
					""",
			},
			"",
			"",
			null,
			JavacTestOptions.DEFAULT);
		// keep A2 binary, recompile A1 with a name change
		this.runConformTest(
			false, // do not flush A2.class
			new String[] {
				"A1.java",
				"""
					@A2(@A1(m1 = "u"))
					public @interface A1 {
					  String m1();
					  String m3() default "v";
					}
					""",
			},
			null,
			"",
			null,
			JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
	}
// transitive closure on binary types does not need to include annotations
public void test190() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public @interface A {
				  int value();
				}
				"""
			},
		"");
	String binName1 = OUTPUT_DIR + File.separator + "bin1";
	File bin1 = new File(binName1);
	bin1.mkdir();
	String [] javaClassLibs = Util.getJavaClassLibs();
	int javaClassLibsLength;
	String [] xClassLibs = new String[(javaClassLibsLength = javaClassLibs.length) + 2];
	System.arraycopy(javaClassLibs, 0, xClassLibs, 0, javaClassLibsLength);
	xClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	xClassLibs[javaClassLibsLength + 1] = binName1;
	(new File(OUTPUT_DIR + File.separator + "A.class")).renameTo(new File(binName1 + File.separator + "A.class"));
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    @A(0)
				    void foo() {
				        System.out.println("SUCCESS");
				    }
				    public static void main(String args[]) {
				        new X().foo();
				    }
				}""",
		},
		"SUCCESS",
		xClassLibs,
		false, // do not flush
		null);
	String binName2 = OUTPUT_DIR + File.separator + "bin2";
	File bin2 = new File(binName2);
	bin2.mkdir();
	(new File(OUTPUT_DIR + File.separator + "X.class")).renameTo(new File(binName2 + File.separator + "X.class"));
	String [] yClassLibs = new String[javaClassLibsLength + 2];
	System.arraycopy(javaClassLibs, 0, yClassLibs, 0, javaClassLibsLength);
	yClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	yClassLibs[javaClassLibsLength + 1] = binName2;
	// Y compiles despite the fact that A is not on the classpath
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    public static void main(String args[]) {
				        new X().foo();
				    }
				}""",
		},
		"SUCCESS",
		yClassLibs,
		false, // do not flush
		null);
}

// transitive closure on binary types does not need to include annotations - variant
public void test191() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
				public @interface A {
				  int value();
				}
				"""
			},
		"");
	String binName1 = OUTPUT_DIR + File.separator + "bin1";
	File bin1 = new File(binName1);
	bin1.mkdir();
	String [] javaClassLibs = Util.getJavaClassLibs();
	int javaClassLibsLength;
	String [] xClassLibs = new String[(javaClassLibsLength = javaClassLibs.length) + 2];
	System.arraycopy(javaClassLibs, 0, xClassLibs, 0, javaClassLibsLength);
	xClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	xClassLibs[javaClassLibsLength + 1] = binName1;
	(new File(OUTPUT_DIR + File.separator + "A.class")).renameTo(new File(binName1 + File.separator + "A.class"));
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    @A(0)
				    void foo() {
				        System.out.println("SUCCESS");
				    }
				    public static void main(String args[]) {
				        new X().foo();
				    }
				}""",
		},
		"SUCCESS",
		xClassLibs,
		false, // do not flush
		null);
	String binName2 = OUTPUT_DIR + File.separator + "bin2";
	File bin2 = new File(binName2);
	bin2.mkdir();
	(new File(OUTPUT_DIR + File.separator + "X.class")).renameTo(new File(binName2 + File.separator + "X.class"));
	String [] yClassLibs = new String[javaClassLibsLength + 2];
	System.arraycopy(javaClassLibs, 0, yClassLibs, 0, javaClassLibsLength);
	yClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	yClassLibs[javaClassLibsLength + 1] = binName2;
	// Y compiles despite the fact that A is not on the classpath
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    public static void main(String args[]) {
				        new X().foo();
				    }
				}""",
		},
		"SUCCESS",
		yClassLibs,
		false, // do not flush
		null);
}

public void test192() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@ATest(groups={"a","b"})
					void foo(){
					}
					@ATest(groups="c")
					void bar(){
					}
				}
				@interface ATest {
					String[] groups();
				}
				"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102160
public void test193() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public @interface A {
					A circular1();
				}
				@interface B {
					A circular2();
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 2)
				A circular1();
				^
			Cycle detected: the annotation type A cannot contain attributes of the annotation type itself
			----------
			"""
	);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public @interface A {
					B circular2();
					A circular1();
				}
				@interface B {
					A circular();
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 2)
				B circular2();
				^
			Cycle detected: a cycle exists between annotation attributes of A and B
			----------
			2. ERROR in A.java (at line 3)
				A circular1();
				^
			Cycle detected: the annotation type A cannot contain attributes of the annotation type itself
			----------
			3. ERROR in A.java (at line 6)
				A circular();
				^
			Cycle detected: a cycle exists between annotation attributes of B and A
			----------
			"""
	);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public @interface A {
					A circular1();
					B circular2();
				}
				@interface B {
					A circular();
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 2)
				A circular1();
				^
			Cycle detected: the annotation type A cannot contain attributes of the annotation type itself
			----------
			2. ERROR in A.java (at line 3)
				B circular2();
				^
			Cycle detected: a cycle exists between annotation attributes of A and B
			----------
			3. ERROR in A.java (at line 6)
				A circular();
				^
			Cycle detected: a cycle exists between annotation attributes of B and A
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130017
public void test194() {
	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
	?	"""
		----------
		1. ERROR in X.java (at line 5)
			@Override
			^^^^^^^^^
		The annotation @Override is disallowed for this location
		----------
		2. ERROR in X.java (at line 9)
			public static void foo() {}
			                   ^^^^^
		The method foo() of type X must override a superclass method
		----------
		"""
	:	"""
		----------
		1. ERROR in X.java (at line 5)
			@Override
			^^^^^^^^^
		The annotation @Override is disallowed for this location
		----------
		2. ERROR in X.java (at line 9)
			public static void foo() {}
			                   ^^^^^
		The method foo() of type X must override or implement a supertype method
		----------
		""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Base {
				    public static void foo() {}
				}
				public class X extends Base {
					@Override
					X(){}
				\t
				    @Override
				    public static void foo() {}
				}
				"""
		},
		expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130516
public void test195() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("cast")
					void foo() {
						String s = (String) "hello";
					}
					void bar() {
						String s = (String) "hello";
					}
					Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				String s = (String) "hello";
				           ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			2. ERROR in X.java (at line 9)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133440
public void test196() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public @interface X {
				    enum MyEnum {
				        VAL_1, VAL_2
				    }
				    public MyEnum theValue() default null;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public MyEnum theValue() default null;
				                                 ^^^^
			The value for annotation attribute X.theValue must be an enum constant expression
			----------
			""");
}
// no override between package private methods
public void test197() {
	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
	?	"""
		----------
		1. WARNING in p\\X.java (at line 4)
			void foo() {
			     ^^^^^
		The method X.foo() does not override the inherited method from OldStuff since it is private to a different package
		----------
		2. ERROR in p\\X.java (at line 4)
			void foo() {
			     ^^^^^
		The method foo() of type X must override a superclass method
		----------
		"""
	:	"""
		----------
		1. WARNING in p\\X.java (at line 4)
			void foo() {
			     ^^^^^
		The method X.foo() does not override the inherited method from OldStuff since it is private to a different package
		----------
		2. ERROR in p\\X.java (at line 4)
			void foo() {
			     ^^^^^
		The method foo() of type X must override or implement a supertype method
		----------
		""";
    this.runNegativeTest(
        new String[] {
            "p/X.java",
            """
				package p;
				public class X extends q.OldStuff {
					@Override
					void foo() {
					}
				}
				""",
            "q/OldStuff.java",
            """
				package q;
				public class OldStuff {
					void foo() {
					}\t
				}
				""",
        },
        expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134129
public void test198() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				@interface Anno {
				        boolean b() default false;
				        String[] c() default "";
				}
				@Anno(b = {})
				public class X {
					@Anno(c = { 0 })
					void foo(){}
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 5)
				@Anno(b = {})
				          ^^
			Type mismatch: cannot convert from Object[] to boolean
			----------
			2. ERROR in X.java (at line 7)
				@Anno(c = { 0 })
				            ^
			Type mismatch: cannot convert from int to String
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=138443
public void test199() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				@interface AttributeOverrides {
					AttributeOverride[] value();
				}
				@interface AttributeOverride {
					String name();
					Column column();
				}
				@interface Column {
					String name();
				}
				@AttributeOverrides({
				    @AttributeOverride( name="city", column=@Column( name="DIAB99C_TXCTY" )),
				    @AttributeOverride( name="state", column=@Column( name="DIAB99C_TXSTAT" )),
				    @AttributeOverride( name="zipCode", column=@Column( name="DIAB99C_TXZIP")),
				}) public class X {}"""
		},
		"",
		"",
		null,
		JavacTestOptions.JavacHasABug.JavacBug6337964);
}
// JLS 3 - 9.6: cannot override Object's methods
public void test200() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				@interface X {
				  int clone();
				  String finalize();
				  boolean getClass();
				  long notify();
				  double notifyAll();
				  float wait();
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 2)
				int clone();
				    ^^^^^^^
			The annotation type X cannot override the method Object.clone()
			----------
			2. ERROR in X.java (at line 3)
				String finalize();
				       ^^^^^^^^^^
			The annotation type X cannot override the method Object.finalize()
			----------
			3. ERROR in X.java (at line 4)
				boolean getClass();
				        ^^^^^^^^^^
			The annotation type X cannot override the method Object.getClass()
			----------
			4. ERROR in X.java (at line 5)
				long notify();
				     ^^^^^^^^
			The annotation type X cannot override the method Object.notify()
			----------
			5. ERROR in X.java (at line 6)
				double notifyAll();
				       ^^^^^^^^^^^
			The annotation type X cannot override the method Object.notifyAll()
			----------
			6. ERROR in X.java (at line 7)
				float wait();
				      ^^^^^^
			The annotation type X cannot override the method Object.wait()
			----------
			""");
}
//JLS 3 - 9.6: cannot override Annotation's methods
public void test201() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				@interface X {
				  char hashCode();
				  int annotationType();
				  Class toString();
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 2)
				char hashCode();
				     ^^^^^^^^^^
			The annotation type X cannot override the method Annotation.hashCode()
			----------
			2. ERROR in X.java (at line 3)
				int annotationType();
				    ^^^^^^^^^^^^^^^^
			The annotation type X cannot override the method Annotation.annotationType()
			----------
			3. WARNING in X.java (at line 4)
				Class toString();
				^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			4. ERROR in X.java (at line 4)
				Class toString();
				      ^^^^^^^^^^
			The annotation type X cannot override the method Annotation.toString()
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259
public void test202() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				 public class X {
				 @Ann(m=Object)
				 private int foo;
				 private NonExisting bar;
				 }
				 @interface Ann {
				 String m();
				 }
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Ann(m=Object)
				       ^^^^^^
			Object cannot be resolved to a variable
			----------
			2. ERROR in X.java (at line 4)
				private NonExisting bar;
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test203() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@Ann(m=Object())
					private void foo(){}
					private NonExisting bar(){}
				}
				@interface Ann {
				    String m();
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Ann(m=Object())
				       ^^^^^^
			The method Object() is undefined for the type X
			----------
			2. ERROR in X.java (at line 4)
				private NonExisting bar(){}
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test204() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@Ann(m=bar(null))
					private void foo(){}
					private NonExisting bar(NonExisting ne){}
				}
				@interface Ann {
				    String m();
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Ann(m=bar(null))
				       ^^^
			The method bar(NonExisting) from the type X refers to the missing type NonExisting
			----------
			2. ERROR in X.java (at line 4)
				private NonExisting bar(NonExisting ne){}
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				private NonExisting bar(NonExisting ne){}
				                        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test205() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@Ann(m=foo())
					private void foo(){}
					private NonExisting bar(NonExisting ne){}
				}
				@interface Ann {
				    String m();
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Ann(m=foo())
				       ^^^^^
			Type mismatch: cannot convert from void to String
			----------
			2. ERROR in X.java (at line 4)
				private NonExisting bar(NonExisting ne){}
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				private NonExisting bar(NonExisting ne){}
				                        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test206() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@Ann(m=bar())
					private void foo(){}
					private NonExisting bar(){}
				}
				@interface Ann {
				    String m();
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Ann(m=bar())
				       ^^^
			The method bar() from the type X refers to the missing type NonExisting
			----------
			2. ERROR in X.java (at line 4)
				private NonExisting bar(){}
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test207() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				 public class X {
				@Ann(m=foo)
				 private NonExisting foo;
				 private NonExisting bar;
				 }
				 @interface Ann {
				 String m();
				 }
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Ann(m=foo)
				       ^^^
			Cannot reference a field before it is defined
			----------
			2. ERROR in X.java (at line 2)
				@Ann(m=foo)
				       ^^^
			NonExisting cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 3)
				private NonExisting foo;
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 4)
				private NonExisting bar;
				        ^^^^^^^^^^^
			NonExisting cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test208() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				@Retention(RUNTIME) @interface MyAnnotation {
				    public MyEnum value();
				}
				enum MyEnum {
				    ONE, TWO, THREE
				}
				@MyAnnotation(X.FOO) class MyClass {
				}
				public class X {
				    public static final MyEnum FOO = MyEnum.TWO;
				    public static void main(String[] args) {
				        MyAnnotation annotation =
				                MyClass.class.getAnnotation(MyAnnotation.class);
				        System.out.println(annotation.value().toString());
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 9)
				@MyAnnotation(X.FOO) class MyClass {
				              ^^^^^
			The value for annotation attribute MyAnnotation.value must be an enum constant expression
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test209() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				@Retention(RUNTIME) @interface MyAnnotation {
				    public MyEnum value();
				}
				enum MyEnum {
				    ONE, TWO, THREE
				}
				@MyAnnotation(value=X.FOO) class MyClass {
				}
				public class X {
				    public static final MyEnum FOO = MyEnum.TWO;
				    public static void main(String[] args) {
				        MyAnnotation annotation =
				                MyClass.class.getAnnotation(MyAnnotation.class);
				        System.out.println(annotation.value().toString());
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 9)
				@MyAnnotation(value=X.FOO) class MyClass {
				                    ^^^^^
			The value for annotation attribute MyAnnotation.value must be an enum constant expression
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test210() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				@Retention(RUNTIME) @interface MyAnnotation {
				    public MyEnum[] value();
				}
				enum MyEnum {
				    ONE, TWO, THREE
				}
				@MyAnnotation(value= { X.FOO }) class MyClass {
				}
				public class X {
				    public static final MyEnum FOO = MyEnum.TWO;
				    public static void main(String[] args) {
				        MyAnnotation annotation =
				                MyClass.class.getAnnotation(MyAnnotation.class);
				        System.out.println(annotation.value().toString());
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 9)
				@MyAnnotation(value= { X.FOO }) class MyClass {
				                       ^^^^^
			The value for annotation attribute MyAnnotation.value must be an enum constant expression
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test211() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				@Retention(RUNTIME) @interface MyAnnotation {
				    public MyEnum[] value();
				}
				enum MyEnum {
				    ONE, TWO, THREE
				}
				@MyAnnotation(value= { null }) class MyClass {
				}
				public class X {
				    public static final MyEnum FOO = MyEnum.TWO;
				    public static void main(String[] args) {
				        MyAnnotation annotation =
				                MyClass.class.getAnnotation(MyAnnotation.class);
				        System.out.println(annotation.value().toString());
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 9)
				@MyAnnotation(value= { null }) class MyClass {
				                       ^^^^
			The value for annotation attribute MyAnnotation.value must be an enum constant expression
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156891
public void test212() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				@Retention(RUNTIME) @interface MyAnnotation {
				    public MyEnum[] values();
				}
				enum MyEnum {
				    ONE, TWO, THREE
				}
				public class X {
				
						private static final MyEnum[] myValues = { MyEnum.ONE, MyEnum.TWO };
				       @MyAnnotation(values=myValues)\s
				       public void dothetrick(){}\s
				
				        public static void main(String[] args)throws Exception {
				                MyAnnotation sluck = X.class.getMethod("dothetrick", new Class[0]).getAnnotation(MyAnnotation.class);
				                System.out.println(sluck.values().length);
				        }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 12)
				@MyAnnotation(values=myValues)\s
				                     ^^^^^^^^
			The value for annotation attribute MyAnnotation.values must be an array initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156891
public void test213() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				@Retention(RUNTIME) @interface MyAnnotation {
				    public int[] values();
				}
				public class X {
				
						private static final int[] myValues = { 1, 2, 3 };
				       @MyAnnotation(values=myValues)\s
				       public void dothetrick(){}\s
				
				        public static void main(String[] args)throws Exception {
				                MyAnnotation sluck = X.class.getMethod("dothetrick", new Class[0]).getAnnotation(MyAnnotation.class);
				                System.out.println(sluck.values().length);
				        }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 9)
				@MyAnnotation(values=myValues)\s
				                     ^^^^^^^^
			The value for annotation attribute MyAnnotation.values must be an array initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141931
public void test214() {
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.DISABLED);

	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
		?	"""
			----------
			1. ERROR in X.java (at line 3)
				void foo();
				     ^^^^^
			The method foo() of type I must override a superclass method
			----------
			2. ERROR in X.java (at line 8)
				public void foo() {}
				            ^^^^^
			The method foo() of type X must override a superclass method
			----------
			3. ERROR in X.java (at line 13)
				void foo();
				     ^^^^^
			The method foo() of type J must override a superclass method
			----------
			"""
		:	"""
			----------
			1. ERROR in X.java (at line 3)
				void foo();
				     ^^^^^
			The method foo() of type I must override or implement a supertype method
			----------
			""";
    this.runNegativeTest(
    	true,
        new String[] {
            "X.java",
			"""
				interface I {
				  @Override
				  void foo();
				  void bar();
				}
				public class X implements I {
				  @Override
				  public void foo() {}
				  public void bar() {}
				}
				interface J extends I {
					@Override
					void foo();
				}
				""",
        },
        null,
        customOptions,
        expectedOutput,
        JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141931
// variant
public void test215() {
	String sources[] = new String[] {
		"I.java",
		"""
			public interface I {
			  void foo();
			}
			""",
		"X.java",
		"abstract class X implements I {\n" +
		"}\n",
		"Y.java",
		"""
			class Y extends X {
			  @Override
			  public void foo() {}
			}
			"""};
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6) {
		this.runNegativeTest(sources,
			"""
				----------
				1. ERROR in Y.java (at line 3)
					public void foo() {}
					            ^^^^^
				The method foo() of type Y must override a superclass method
				----------
				""");
	} else {
		this.runConformTest(sources,
			"");
	}
}
// extending java.lang.annotation.Annotation
public void test216() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Annotation;
				public class X {
				  void bar(MyConstructor constructor, Class<Ann> ann) {
				    constructor.getAnnotation(ann).message();
				  }
				}
				@interface Ann {
				  String message();
				}
				class MyConstructor<V> {
				  public <T extends Annotation> T getAnnotation(Class<T> c) { return null; }
				}
				""",
        },
		"""
			----------
			1. WARNING in X.java (at line 3)
				void bar(MyConstructor constructor, Class<Ann> ann) {
				         ^^^^^^^^^^^^^
			MyConstructor is a raw type. References to generic type MyConstructor<V> should be parameterized
			----------
			2. WARNING in X.java (at line 4)
				constructor.getAnnotation(ann).message();
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method getAnnotation(Class) belongs to the raw type MyConstructor. References to generic type MyConstructor<V> should be parameterized
			----------
			3. ERROR in X.java (at line 4)
				constructor.getAnnotation(ann).message();
				                               ^^^^^^^
			The method message() is undefined for the type Annotation
			----------
			""");
}
// extending java.lang.annotation.Annotation
public void test217() {
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.DISABLED);
	String expectedOutput =
		"""
		----------
		1. WARNING in X.java (at line 3)
			void bar(MyConstructor constructor, Class<Ann> ann) {
			         ^^^^^^^^^^^^^
		MyConstructor is a raw type. References to generic type MyConstructor<V> should be parameterized
		----------
		2. WARNING in X.java (at line 4)
			constructor.getAnnotation(ann).message();
			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Type safety: The method getAnnotation(Class) belongs to the raw type MyConstructor. References to generic type MyConstructor<V> should be parameterized
		----------
		3. ERROR in X.java (at line 4)
			constructor.getAnnotation(ann).message();
			                               ^^^^^^^
		The method message() is undefined for the type Annotation
		----------
		""";
    this.runNegativeTest(
    	true,
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Annotation;
				public class X {
				  void bar(MyConstructor constructor, Class<Ann> ann) {
				    constructor.getAnnotation(ann).message();
				  }
				}
				@interface Ann {
				  String message();
				}
				interface Z {
				  <T extends Annotation> T getAnnotation(Class<T> c);
				}
				class MyAccessibleObject implements Z {
				  public <T extends Annotation> T getAnnotation(Class<T> c) { return null; }
				}
				class MyConstructor<V> {
				  public <T extends Annotation> T getAnnotation(Class<T> c) { return null; }
				}
				""",
        },
        null,
        customOptions,
        expectedOutput,
        JavacTestOptions.SKIP);
}
// extending java.lang.annotation.Annotation
public void test218() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.lang.annotation.Annotation;
				import java.lang.reflect.Constructor;
				public class X {
				  void bar(Constructor constructor, Class<Ann> ann) {
				    constructor.getAnnotation(ann).message();
				  }
				}
				@interface Ann {
				  String message();
				}
				""",
        },
		"""
			----------
			1. WARNING in X.java (at line 4)
				void bar(Constructor constructor, Class<Ann> ann) {
				         ^^^^^^^^^^^
			Constructor is a raw type. References to generic type Constructor<T> should be parameterized
			----------
			2. WARNING in X.java (at line 5)
				constructor.getAnnotation(ann).message();
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method getAnnotation(Class) belongs to the raw type Constructor. References to generic type Constructor<T> should be parameterized
			----------
			3. ERROR in X.java (at line 5)
				constructor.getAnnotation(ann).message();
				                               ^^^^^^^
			The method message() is undefined for the type Annotation
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBug6400189);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test219() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        @MyB1(MyA2.XX)
				        public void foo(){}
				}""",
			"MyA1.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA1 {
				}""",
			"MyA2.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA2 {
				        public static final MyA1 XX = null;
				}""",
			"MyB1.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE, METHOD
				})
				public @interface MyB1 {
				        MyA1 value();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@MyB1(MyA2.XX)
				      ^^^^^^^
			The value for annotation attribute MyB1.value must be some @MyA1 annotation\s
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test220() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        @MyB1({MyA2.XX})
				        public void foo(){}
				}""",
			"MyA1.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA1 {
				}""",
			"MyA2.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA2 {
				        public static final MyA1 XX = null;
				}""",
			"MyB1.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE, METHOD
				})
				public @interface MyB1 {
				        MyA1[] value();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@MyB1({MyA2.XX})
				       ^^^^^^^
			The value for annotation attribute MyB1.value must be some @MyA1 annotation\s
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test221() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        @MyB1(null)
				        public void foo(){}
				}""",
			"MyA1.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA1 {
				}""",
			"MyA2.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA2 {
				        public static final MyA1 XX = null;
				}""",
			"MyB1.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE, METHOD
				})
				public @interface MyB1 {
				        MyA1 value();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@MyB1(null)
				      ^^^^
			The value for annotation attribute MyB1.value must be some @MyA1 annotation\s
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test222() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        @MyB1({null})
				        public void foo(){}
				}""",
			"MyA1.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA1 {
				}""",
			"MyA2.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA2 {
				        public static final MyA1 XX = null;
				}""",
			"MyB1.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE, METHOD
				})
				public @interface MyB1 {
				        MyA1[] value();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@MyB1({null})
				       ^^^^
			The value for annotation attribute MyB1.value must be some @MyA1 annotation\s
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test223() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        @MyB1(@MyA1())
				        public void foo(){}
				}""",
			"MyA1.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA1 {
				}""",
			"MyA2.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA2 {
				        public static final MyA1 XX = null;
				}""",
			"MyB1.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE, METHOD
				})
				public @interface MyB1 {
				        MyA1 value();
				}"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test224() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        @MyB1({@MyA1(), @MyA1})
				        public void foo(){}
				}""",
			"MyA1.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA1 {
				}""",
			"MyA2.java",
			"""
				import static java.lang.annotation.ElementType.TYPE;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE
				})
				public @interface MyA2 {
				        public static final MyA1 XX = null;
				}""",
			"MyB1.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.Target;
				
				@Target( {
					TYPE, METHOD
				})
				public @interface MyB1 {
				        MyA1[] value();
				}"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=XXXXX
public void test225() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void myMethod() {
				    @MyAnnot1()
				  }
				}
				@interface MyAnnot1 {
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				@MyAnnot1()
				          ^
			Syntax error, insert "enum Identifier" to complete EnumHeader
			----------
			2. ERROR in X.java (at line 3)
				@MyAnnot1()
				          ^
			Syntax error, insert "EnumBody" to complete BlockStatements
			----------
			""",
		null,
		true,
		null /* no custom options */,
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		true  /* do not perform statements recovery */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179477 - variation
public void test226() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public @interface Annot {
				        float[] value();
				        Class<X>[] classe();
				    }
				    @Annot(value={x}, classe={Zork.class,zork})
				    class Inner {
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				@Annot(value={x}, classe={Zork.class,zork})
				              ^
			x cannot be resolved to a variable
			----------
			2. ERROR in X.java (at line 6)
				@Annot(value={x}, classe={Zork.class,zork})
				                          ^^^^
			Zork cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 6)
				@Annot(value={x}, classe={Zork.class,zork})
				                          ^^^^^^^^^^
			Class<Zork> cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 6)
				@Annot(value={x}, classe={Zork.class,zork})
				                                     ^^^^
			zork cannot be resolved to a variable
			----------
			5. ERROR in X.java (at line 6)
				@Annot(value={x}, classe={Zork.class,zork})
				                                     ^^^^
			The value for annotation attribute X.Annot.classe must be a class literal
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test227() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public @interface X<T> {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public @interface X<T> {}
				                    ^
			Syntax error, annotation declaration cannot have type parameters
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533
public void test228() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings("unchecked") //unused
						void doNoEvil(){
						}
					}
					""",
				"Y.java",
				"""
					public class Y {
						Zork z;
					}
					""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				@SuppressWarnings("unchecked") //unused
				                  ^^^^^^^^^^^
			Unnecessary @SuppressWarnings("unchecked")
			----------
			----------
			1. ERROR in Y.java (at line 2)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533
public void test229() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings({"unchecked","all"})
						void doNoEvil(){
						}
						Zork z;
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test230() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	enableAllWarningsForIrritants(options, IrritantSet.UNUSED);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings({"zork", "unused" })
						void foo() {}
					}
					@SuppressWarnings({"all"})
					class X2 {
						@SuppressWarnings({"zork", "unused" })
						void foo() {}
					}
					""",
		},
		null, options,
		"""
			----------
			1. WARNING in X.java (at line 2)
				@SuppressWarnings({"zork", "unused" })
				                   ^^^^^^
			Unsupported @SuppressWarnings("zork")
			----------
			2. ERROR in X.java (at line 2)
				@SuppressWarnings({"zork", "unused" })
				                           ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			3. ERROR in X.java (at line 7)
				@SuppressWarnings({"zork", "unused" })
				                           ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			""",
		null, null, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test231() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	enableAllWarningsForIrritants(options, IrritantSet.UNUSED);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings({"zork", "unused","all"})
						void foo() {}
					}
					
					@SuppressWarnings({"all"})
					class X2 {
						@SuppressWarnings("unused")
						void foo() {}
						Object z;
					}
					""",
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				@SuppressWarnings({"zork", "unused","all"})
				                           ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			2. ERROR in X.java (at line 8)
				@SuppressWarnings("unused")
				                  ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test232() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({"finally","finally"})
					    public int test(int p) {
					    	try {
							return 1;
						} finally {
							return 2;
						}
					    }
					}
					class Y {}""",
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				@SuppressWarnings({"finally","finally"})
				                             ^^^^^^^^^
			Unnecessary @SuppressWarnings("finally")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test233() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    @SuppressWarnings({"finally","finally"})
					    public int test(int p) {
					    	try {
							return Zork;
						} finally {
							return 2;
						}
					    }
					}
					""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				@SuppressWarnings({"finally","finally"})
				                             ^^^^^^^^^
			Unnecessary @SuppressWarnings("finally")
			----------
			2. ERROR in X.java (at line 5)
				return Zork;
				       ^^^^
			Zork cannot be resolved to a variable
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test234() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings(\"finally\")\n" + // unused but no complaint since an error is nested (can't tell for sure)
				"    public int test(int p) {\n" +
				"		return Zork;\n" +
				"    }\n" +
				"}\n",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				return Zork;
				       ^^^^
			Zork cannot be resolved to a variable
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207758
public void test235() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					        void foo() {
					                ArrayList al = null;
					                @SuppressWarnings("unchecked")
					                List<String> ls = al;
					        }
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				ArrayList al = null;
				^^^^^^^^^
			ArrayList cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207758 - variation
public void test236() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
						void foo() {
							@SuppressWarnings("unchecked")
							List<String> ls = bar();
						}
						ArrayList bar() {
							return null;
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				List<String> ls = bar();
				                  ^^^
			The method bar() from the type X refers to the missing type ArrayList
			----------
			2. ERROR in X.java (at line 7)
				ArrayList bar() {
				^^^^^^^^^
			ArrayList cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207758 - variation
public void test237() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X<B extends ArrayList> {
						B get() { return null; }
						void foo() {
							@SuppressWarnings("unchecked")
							List<String> ls = get();
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public class X<B extends ArrayList> {
				                         ^^^^^^^^^
			ArrayList cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 6)
				List<String> ls = get();
				                  ^^^^^
			Type mismatch: cannot convert from B to List<String>
			----------
			""");
}
public void test238() {
	// check that if promoted to ERROR, unhandled warning token shouldn't be suppressed by @SuppressWarnings("all")
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings({"zork","all"})
						void foo() {}
					}
					""",
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				@SuppressWarnings({"zork","all"})
				                   ^^^^^^
			Unsupported @SuppressWarnings("zork")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
public void test239() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSuperinterface, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					class X implements I {}
					@SuppressWarnings("unused")
					class Y extends X implements I {
						Zork z;
					}
					class Z extends X implements I {}
					interface I {}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in X.java (at line 6)
				class Z extends X implements I {}
				                             ^
			Redundant superinterface I for the type Z, already defined by X
			----------
			""",
		null,
		false,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207411
public void test240() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					@Deprecated @Zork
					public class X {
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				@Deprecated @Zork
				             ^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213
public void test241() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					
						@SuppressWarnings("unchecked")
						public static <T> T asClassUnchecked(Object object, T requiredClassObject) {
							return (T) object;
						}
						public static void main(String... args) {
							try {
								X[] xs = X.asClassUnchecked("abc", (X[])null);
								System.out.println(xs.length);
							} catch(ClassCastException e) {
								System.out.println("SUCCESS");
							}
						}
					}
					""",
		},
		"SUCCESS",
		null,
		false,
		null,
		options,
		null);
}
///https://bugs.eclipse.org/bugs/show_bug.cgi?id=210422 - variation
public void test242() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					public final class X implements Serializable {
					    class SMember extends String {} \s
					    @Annot(value = new SMember())
					     void bar() {}
					    @Annot(value =\s
					            new X(){
					                    ZorkAnonymous1 z;
					                    void foo() {
					                            this.bar();
					                            Zork2 z;
					                    }
					            })
						void foo() {}
					}
					@interface Annot {
					        String value();
					}
					""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				public final class X implements Serializable {
				                   ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 3)
				class SMember extends String {} \s
				                      ^^^^^^
			The type SMember cannot subclass the final class String
			----------
			3. ERROR in X.java (at line 4)
				@Annot(value = new SMember())
				               ^^^^^^^^^^^^^
			Type mismatch: cannot convert from X.SMember to String
			----------
			4. ERROR in X.java (at line 7)
				new X(){
				    ^
			An anonymous class cannot subclass the final class X
			----------
			5. ERROR in X.java (at line 8)
				ZorkAnonymous1 z;
				^^^^^^^^^^^^^^
			ZorkAnonymous1 cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test243() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings("unchecked")
						void foo() {
						\t
						}
					}\t
					""",
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test244() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings("unchecked")
						void foo() {
						\t
						}
					}\t
					""",
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				@SuppressWarnings("unchecked")
				                  ^^^^^^^^^^^
			Unnecessary @SuppressWarnings("unchecked")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test245() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	enableAllWarningsForIrritants(options, IrritantSet.UNUSED);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"unchecked","unused"})
						void foo() {
						\t
						}
					}\t
					""",
		},
		null, options,
		"""
			----------
			1. INFO in X.java (at line 3)
				@SuppressWarnings({"unchecked","unused"})
				                   ^^^^^^^^^^^
			At least one of the problems in category 'unchecked' is not analysed due to a compiler option being ignored
			----------
			2. ERROR in X.java (at line 3)
				@SuppressWarnings({"unchecked","unused"})
				                               ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test245_ignored() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportSuppressWarningNotFullyAnalysed, CompilerOptions.IGNORE);
	enableAllWarningsForIrritants(options, IrritantSet.UNUSED);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"unchecked","unused"})
						void foo() {
						\t
						}
					}\t
					""",
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				@SuppressWarnings({"unchecked","unused"})
				                               ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test245_error() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportSuppressWarningNotFullyAnalysed, CompilerOptions.ERROR);
	enableAllWarningsForIrritants(options, IrritantSet.UNUSED);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"unchecked","unused"})
						void foo() {
						\t
						}
					}\t
					""",
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				@SuppressWarnings({"unchecked","unused"})
				                   ^^^^^^^^^^^
			At least one of the problems in category 'unchecked' is not analysed due to a compiler option being ignored
			----------
			2. ERROR in X.java (at line 3)
				@SuppressWarnings({"unchecked","unused"})
				                               ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test246() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings("all")
						void foo() {
						\t
						}
					}\t
					""",
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=211609
public void test247() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	// only enable in 1.6 mode
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
			new String[] {
				"TestAnnotation.java",
				"""
					public @interface TestAnnotation {
						Class targetItem() default void.class;
					}"""
			},
			"");
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
						@TestAnnotation
						private String foo;
					}""",
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=211609
public void test248() {
	this.runNegativeTest(
			new String[] {
				"TestAnnotation.java",
				"""
					public @interface TestAnnotation {
						String targetItem() default void.class;
					}"""
			},
			"""
				----------
				1. ERROR in TestAnnotation.java (at line 2)
					String targetItem() default void.class;
					                            ^^^^^^^^^^
				Type mismatch: cannot convert from Class<Void> to String
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test249() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"@Zork\n" +
			"public class X {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				@Zork
				 ^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput = "public class X {";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test250() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				@Deprecated
				@Zork
				@Annot(1)
				public class X {}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(RUNTIME)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Zork
				 ^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		@java.lang.Deprecated
		@Annot(value=(int) 1)
		public class X {""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test251() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				@Deprecated
				@Zork
				@Annot(1)
				public class X {}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(CLASS)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Zork
				 ^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		@Annot(value=(int) 1)
		@java.lang.Deprecated
		public class X {""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test252() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					public void foo(@Deprecated @Zork @Annot(2) int i) {}
				}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(CLASS)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(@Deprecated @Zork @Annot(2) int i) {}
				                             ^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #22 @java.lang.Deprecated(
		        )
		    RuntimeInvisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 2
		        #17 @Zork(
		        )
		        #18 @Annot(
		          #19 value=(int) 2 (constant type)
		        )
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test253() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					public void foo(@Deprecated @Zork @Annot(2) int i) {}
				}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(RUNTIME)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(@Deprecated @Zork @Annot(2) int i) {}
				                             ^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 2
		        #19 @java.lang.Deprecated(
		        )
		        #20 @Annot(
		          #21 value=(int) 2 (constant type)
		        )
		    RuntimeInvisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #17 @Zork(
		        )
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test254() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					public void foo(@Deprecated int j, @Zork @Annot(3) int i) {}
				}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(RUNTIME)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(@Deprecated int j, @Zork @Annot(3) int i) {}
				                                    ^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #19 @java.lang.Deprecated(
		        )
		      Number of annotations for parameter 1: 1
		        #20 @Annot(
		          #21 value=(int) 3 (constant type)
		        )
		    RuntimeInvisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 0
		      Number of annotations for parameter 1: 1
		        #17 @Zork(
		        )
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test255() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					public void foo(@Deprecated int j, @Annot("") @Deprecated int i) {}
				}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(RUNTIME)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(@Deprecated int j, @Annot("") @Deprecated int i) {}
				                                          ^^
			Type mismatch: cannot convert from String to int
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #17 @java.lang.Deprecated(
		        )
		      Number of annotations for parameter 1: 1
		        #17 @java.lang.Deprecated(
		        )
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test256() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					public void foo(@Deprecated int j, @Annot("") @Deprecated int i) {}
				}""",
			"Annot.java",
			"""
				import java.lang.annotation.Retention;
				import static java.lang.annotation.RetentionPolicy.*;
				@Retention(CLASS)
				@interface Annot {
					int value() default -1;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(@Deprecated int j, @Annot("") @Deprecated int i) {}
				                                          ^^
			Type mismatch: cannot convert from String to int
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"""
		    RuntimeVisibleParameterAnnotations:\s
		      Number of annotations for parameter 0: 1
		        #20 @java.lang.Deprecated(
		        )
		      Number of annotations for parameter 1: 1
		        #20 @java.lang.Deprecated(
		        )\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=216570
public void test257() {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static interface IFoo {
						        public boolean eval(String s);
						    }
						    static class Foo implements IFoo {
						        @Override
						        public boolean eval(String s) {
						            return true;
						        }
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						public boolean eval(String s) {
						               ^^^^^^^^^^^^^^
					The method eval(String) of type X.Foo must override a superclass method
					----------
					""");
		return;
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    static interface IFoo {
					        public boolean eval(String s);
					    }
					    static class Foo implements IFoo {
					        @Override
					        public boolean eval(String s) {
					            return true;
					        }
					    }
					}
					"""
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167262
public void test258() {
	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
	?	"""
		----------
		1. ERROR in X.java (at line 9)
			void bar();//3
			     ^^^^^
		The method bar() of type Bar must override a superclass method
		----------
		2. ERROR in X.java (at line 13)
			public void bar() {}//4
			            ^^^^^
		The method bar() of type BarImpl must override a superclass method
		----------
		"""
	:	"""
		----------
		1. ERROR in X.java (at line 9)
			void bar();//3
			     ^^^^^
		The method bar() of type Bar must override or implement a supertype method
		----------
		""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Foo {
					@Override
					String toString();//1
				}
				interface Bar extends Foo {
					@Override
					String toString();//2
					@Override
					void bar();//3
				}
				class BarImpl implements Bar {
					@Override
					public void bar() {}//4
				}
				"""
		},
		expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273
public void test259() {
	this.runConformTest(
		new String[] {
			"Jpf.java",
			"""
				public class Jpf {
					@interface Action {
						Forward[] forwards();
					}
					@interface Forward {
						String name();
						String path();
						ActionOutput[] actionOutputs();
					}
					@interface ActionOutput {
						String name();
						Class type();
					}
					@Jpf.Action(\s
							forwards = {\s
									@Jpf.Forward(
											name = "success",\s
											path = "results.jsp",\s
											actionOutputs = {\s
													@Jpf.ActionOutput(
															name = "mybeanmethodResult",\s
															type = java.lang.String[].class) }) })
					public Forward mybeanmethod() {
						return null;
					}
				}
				"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245435
public void test260() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@X.StringAnnotation(X.CONSTANT_EXPRESSION)
				public class X {
				  public @interface StringAnnotation {
				    String value();
				  }
				  public final static String CONSTANT = "Constant";
				  public final static String CONSTANT_EXPRESSION = CONSTANT + "Expression";
				}
				"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273
public void test261() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
			"X.java",//=====================
			"""
				public class X {
					public static void main(String[] args) {
						new Other().foo();
					}
				}
				""",
			"Annot.java",//=====================
			"""
				public @interface Annot {
					Class value();
				}
				""",
			"Other.java",//=====================
			"""
				public class Other {
					@Annot(value = Other[].class)
					void foo() {
						System.out.println("SUCCESS");
					}
				}
				"""
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	this.runConformTest(
			new String[] {
				"X.java",//=====================
				"""
					public class X {
						public static void main(String[] args) {
							new Other().foo();
						}
					}
					""",
			},
			"SUCCESS",
			null,
			false,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273 - variation
public void test262() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
			"X.java",//=====================
			"""
				public class X {
					public static void main(String[] args) {
						new Other().foo();
					}
				}
				""",
			"Annot.java",//=====================
			"""
				public @interface Annot {
					String[] values();
				}
				""",
			"Other.java",//=====================
			"""
				public class Other {
					@Annot(values = {"foo","bar"})
					void foo() {
						System.out.println("SUCCESS");
					}
				}
				"""
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	this.runConformTest(
			new String[] {
				"X.java",//=====================
				"""
					public class X {
						public static void main(String[] args) {
							new Other().foo();
						}
					}
					""",
			},
			"SUCCESS",
			null,
			false,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273 - variation
public void test263() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
			"X.java",//=====================
			"""
				public class X {
					public static void main(String[] args) {
						new Other().foo();
					}
				}
				""",
			"Annot.java",//=====================
			"""
				public @interface Annot {
					String[] values();
				}
				""",
			"Other.java",//=====================
			"""
				public class Other {
					@Annot(values = {"foo","bar"})
					void foo() {
						System.out.println("SUCCESS");
					}
				}
				"""
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	this.runConformTest(
			new String[] {
				"X.java",//=====================
				"""
					public class X {
						public static void main(String[] args) {
							new Other().foo();
						}
					}
					""",
			},
			"SUCCESS",
			null,
			false,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=256035
public void test264() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@interface Anno {
					String value();
				}
				
				@Anno(X.B)
				public class X {
					public static final String A = "a";
					public static final String B = A + "b";
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=258906
public void test265() {

	INameEnvironment nameEnvironment = new FileSystem(Util.getJavaClassLibs(), new String[] {}, null);
	IErrorHandlingPolicy errorHandlingPolicy = new IErrorHandlingPolicy() {
		public boolean proceedOnErrors() { return true; }
		public boolean stopOnFirstError() { return false; }
		public boolean ignoreAllErrors() { return false; }
	};
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	CompilerOptions compilerOptions = new CompilerOptions(options);
	compilerOptions.performMethodsFullRecovery = false;
	compilerOptions.performStatementsRecovery = false;
	Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
	requestor.outputPath = "bin/";
	IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());

	Compiler compiler = new Compiler(nameEnvironment, errorHandlingPolicy, compilerOptions, requestor, problemFactory);
	compiler.options.produceReferenceInfo = true;

	String code = "@java.lang.SuppressWarnings(\"test\")\npackage testpack;\n";
	ICompilationUnit source = new CompilationUnit(code.toCharArray(), "testpack/package-info.java", null);

	// don't call compile as would be normally expected since that wipes out the lookup environment
	// before we could query it. Use internal API resolve instead which can run a subset of the
	// compilation steps for us.

	compiler.resolve (source,
		true, // verifyMethods,
		true, // boolean analyzeCode,
		false // generateCode
	);
	char [][] compoundName = new char [][] { "testpack".toCharArray(), "package-info".toCharArray()};
	ReferenceBinding type = compiler.lookupEnvironment.getType(compoundName);
	AnnotationBinding[] annotations = null;
	if (type != null && type.isValidBinding()) {
		annotations = type.getAnnotations();
	}
	assertTrue ("Annotations missing on package-info interface", annotations != null && annotations.length == 1);
	assertEquals("Wrong annotation on package-info interface ", "@SuppressWarnings((String)\"test\")", annotations[0].toString());
	nameEnvironment.cleanup();
	if (requestor.hasErrors) {
		if (!requestor.problemLog.contains("The annotation @SuppressWarnings is disallowed for this location")
			&& !requestor.problemLog.contains("annotations are only available if source level is 1.5 or greater")){
			Assert.assertNull("problem", requestor.problemLog);
		}
	}
	compiler = null;
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220311
public void test266() {
	this.runNegativeTest(
		new String[] {
			"p/package-info.java",
			"""
				@Deprecated
				@Deprecated
				package p;"""
		},
		"----------\n" +
		"1. ERROR in p\\package-info.java (at line 1)\n" +
		"	@Deprecated\n" +
		"	^^^^^^^^^^^\n" +
		this.repeatableIntroText + "@Deprecated"+ this.repeatableTrailerText +
		"----------\n" +
		"2. ERROR in p\\package-info.java (at line 2)\n" +
		"	@Deprecated\n" +
		"	^^^^^^^^^^^\n" +
		this.repeatableIntroText + "@Deprecated"+ this.repeatableTrailerText +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=261323.
// Test to make sure that the use of a static import as an annotation value counts as a use
// (and consequently that there is no unused static import warning)
public void test267() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);

	runNegativeTest(
		true,
		new String[] {
				"com/SomeTest.java",
				"""
					package com;
					import static com.SomeTest.UNCHECKED;
					@SuppressWarnings(UNCHECKED)
					public class SomeTest {
					    public static final String UNCHECKED = "unchecked";
					}
					"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in com\\SomeTest.java (at line 3)
				@SuppressWarnings(UNCHECKED)
				                  ^^^^^^^^^
			Unnecessary @SuppressWarnings("unchecked")
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262304
public void test268() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {
					protected enum E {
						E1, E2
					}
					protected @interface Anno1 { E value(); }
					protected @interface Anno2 { E value(); }
					protected @interface Anno3 { E value(); }
					@Anno1(true ? E.E1 : E.E2)
					@Anno2(bar())
					@Anno3(((E.E1)))
					public void foo() {
					}
					public E bar() { return E.E1; }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				@Anno1(true ? E.E1 : E.E2)
				       ^^^^^^^^^^^^^^^^^^
			The value for annotation attribute X.Anno1.value must be an enum constant expression
			----------
			2. ERROR in X.java (at line 9)
				@Anno2(bar())
				       ^^^^^
			The value for annotation attribute X.Anno2.value must be an enum constant expression
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=274917
public void test269() {
	Map customOptions = new Hashtable();
	String[] warnings = CompilerOptions.warningOptionNames();
	for (int i = 0, ceil = warnings.length; i < ceil; i++) {
		customOptions.put(warnings[i], CompilerOptions.WARNING);
	}
	this.runConformTest(
			true,
			new String[] {
					"X.java",
					"@interface X {}",
			},
			null,
			customOptions,
			"""
				----------
				1. WARNING in X.java (at line 1)
					@interface X {}
					             ^^
				Empty block should be documented
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=287009
public void test270() {
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test<T> {
					@interface Anno {
						Anno value();
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in Test.java (at line 3)
				Anno value();
				^^^^
			Cycle detected: the annotation type Test.Anno cannot contain attributes of the annotation type itself
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289576
public void test271() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@interface A {}
				public class X {
					@SuppressWarnings("unused")
					private void foo(@A Object o) {}
				}"""
		},
	"");

	String expectedOutput =
		"""
		  // Method descriptor #15 (Ljava/lang/Object;)V
		  // Stack: 0, Locals: 2
		  private void foo(@A java.lang.Object o);
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289516
public void test272() throws Exception {
	if (this.complianceLevel != ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@interface A {}
				public class X {
					@SuppressWarnings("unused")
					private void foo(@A Object o) {}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null,
		true);

	String expectedOutput =
		"""
		  // Method descriptor #15 (Ljava/lang/Object;)V
		  // Stack: 0, Locals: 2
		  private void foo(@A java.lang.Object o);
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289576
public void test273() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@interface A {}
				public class X {
					@SuppressWarnings("unused")
					private X(@A Object o) {}
				}"""
		},
		"");

	String expectedOutput =
		"""
		  // Method descriptor #6 (Ljava/lang/Object;)V
		  // Stack: 1, Locals: 2
		  private X(@A java.lang.Object o);
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check Missing override annotation error when a method implements
// and also overrides a method in a superclass
public void test274a() {
	String testString [] = new String[] {
			"T.java",
			"""
				public interface T {
				        void m();
				}
				abstract class A implements T {
				}
				class B extends A {
				        public void m() {}
				}
				"""
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
				"""
			----------
			1. ERROR in T.java (at line 7)
				public void m() {}
				            ^^^
			The method m() of type B should be tagged with @Override since it actually overrides a superinterface method
			----------
			""";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check Missing override annotation error when a method implements but
// doesn't overrides
public void test274b() {
	String testString [] = new String[] {
			"Over.java",
			"""
				interface I {
				        void m();
				}
				public class Over implements I {
				        public void m() {}
				}
				"""
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
			"""
			----------
			1. ERROR in Over.java (at line 5)
				public void m() {}
				            ^^^
			The method m() of type Over should be tagged with @Override since it actually overrides a superinterface method
			----------
			""";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check Missing override annotation error when a method simply overrides
public void test274c() {
	String testString [] = new String[] {
			"B.java",
			"""
				interface A {
				        void m();
				}
				public interface B extends A {
				        void m();
				}
				"""
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
				"""
			----------
			1. ERROR in B.java (at line 5)
				void m();
				     ^^^
			The method m() of type B should be tagged with @Override since it actually overrides a superinterface method
			----------
			""";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check missing override annotation if the method has a signature
// that is override-equivalent to that of any public method declared in Object.
public void test274d() {
	String testString [] = new String[] {
			"A.java",
			"""
				public interface A {
				        String toString();
				}
				"""
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
			"""
			----------
			1. ERROR in A.java (at line 2)
				String toString();
				       ^^^^^^^^^^
			The method toString() of type A should be tagged with @Override since it actually overrides a superinterface method
			----------
			""";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282770.
public void test275() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.ENABLED);

	runConformTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
						public static final boolean DEBUG = false;
					//	@SuppressWarnings("unused")
						public void foo() {
							if (DEBUG)
								System.out.println("true");
							else
								System.out.println("false");
						\t
						}
					}
					"""
		},
		null,
		customOptions,
		"""
			----------
			1. WARNING in X.java (at line 6)
				System.out.println("true");
				^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
		"",
		"",
		JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282770.
public void test276() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.ENABLED);

	runConformTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
						public static final boolean DEBUG = false;
						@SuppressWarnings("unused")
						public void foo() {
							if (DEBUG)
								System.out.println("true");
							else
								System.out.println("false");
						\t
						}
					}
					"""
		},
		null,
		customOptions,
		"",
		"",
		"",
		JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282770.
public void test277() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.DISABLED);

	runConformTest(
		true,
		new String[] {
				"X.java",
				"""
					public class X {
						public static final boolean DEBUG = false;
						@SuppressWarnings("unused")
						public void foo() {
							if (0 < 1)
								System.out.println("true");
							else
								System.out.println("false");
						\t
						}
					}
					"""
		},
		null,
		customOptions,
		"",
		"",
		"",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293777
// To verify that a misleading warning against @Override annotation is not
// issued in case the method signature has not been resolved properly.
public void test278() {
	String testString [] = new String[] {
			"A.java",
			"""
				import javax.swing.JComponent;
				public class A extends JComponent {
				   @Override
					protected void paintComponent(Graphics g) {\
				   }
				}
				"""
			};
	String expectedOutput =
		"""
		----------
		1. WARNING in A.java (at line 2)
			public class A extends JComponent {
			             ^
		The serializable class A does not declare a static final serialVersionUID field of type long
		----------
		2. ERROR in A.java (at line 4)
			protected void paintComponent(Graphics g) {   }
			                              ^^^^^^^^
		Graphics cannot be resolved to a type
		----------
		""";
	this.runNegativeTest(
			testString,
			expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=301683
public void test279() {
	String testString [] = new String[] {
			"A.java",
			"""
				public class A {
				    public @interface Inline {
				        String value();
				    }
				    @Inline("foo")
				    public Zork test;
				    public native void method();
				}"""
			};
	String expectedOutput =
		"""
		----------
		1. ERROR in A.java (at line 6)
			public Zork test;
			       ^^^^
		Zork cannot be resolved to a type
		----------
		""";
	this.runNegativeTest(
			testString,
			expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test280() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"public class A {\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	private int i;\n" + // problem configured as warning but still suppressed
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test281() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED); // this option overrides the next
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"""
				public class A {
					@SuppressWarnings("unused")
					private int i;
				}
				"""
			};
	String expectedErrorString =
			"""
		----------
		1. ERROR in A.java (at line 3)
			private int i;
			            ^
		The value of the field A.i is not used
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test282() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"import java.util.Map;\n" +
			"public class A {\n" +
			"	@SuppressWarnings({\"rawtypes\", \"unused\"})\n" + //suppress a warning and an error
			"	private Map i;\n" +
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test283() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"public class A {\n" +
			"	@SuppressWarnings(\"all\")\n" +
			"	private void i;\n" + // cannot suppress mandatory error
			"}\n"
			};
	String expectedErrorString =
			"""
		----------
		1. ERROR in A.java (at line 3)
			private void i;
			             ^
		void is an invalid type for the variable i
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304031
public void test284() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"X.java",
			"""
				public class X {
				    void m() {
				        @SuppressWarnings("cast")
				        int i= (int) 0;
				        @SuppressWarnings("cast")
				        byte b= (byte) i;
				        System.out.println(b);
				    }
				}"""
	};
	String expectedErrorString =
		"""
		----------
		1. ERROR in X.java (at line 5)
			@SuppressWarnings("cast")
			                  ^^^^^^
		Unnecessary @SuppressWarnings("cast")
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304031
public void test285() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"X.java",
			"""
				public class X {
				    void m() {
				        @SuppressWarnings("cast")
				        int i= (int) 0;
				        @SuppressWarnings("cast")
				        byte b= (byte) i;
				        System.out.println(b);
				    }
				}"""
	};
	String expectedErrorString =
		"""
		----------
		1. ERROR in X.java (at line 5)
			@SuppressWarnings("cast")
			                  ^^^^^^
		Unnecessary @SuppressWarnings("cast")
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304031
public void test286() {
	Map raiseDeprecationReduceInvalidJavadocSeverity =
		new HashMap(2);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					@SuppressWarnings("deprecation")
					public class X extends p.OldStuff {
						/**
						 * @see p.OldStuff#foo()
						 */
						@Override
						public void foo() {
							super.foo();
						}
					}
					""",
				"p/OldStuff.java",
				"""
					package p;
					@Deprecated
					public class OldStuff {
						public void foo() {
						}\t
					}
					""",
		},
		"",
		null,
		true,
		null,
		raiseDeprecationReduceInvalidJavadocSeverity,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=311849
public void test287() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					
					public class X {
						@SuppressWarnings("rawtypes")
						void foo(ArrayList arg) {
							for (
								@SuppressWarnings("unchecked")
								boolean a= arg.add(1), b= arg.add(1);
								Boolean.FALSE;
							) {
								System.out.println(a && b);
							}
						}
					}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=311849
public void test288() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					
					public class X {
						@SuppressWarnings("rawtypes")
						ArrayList arg;
						@SuppressWarnings("unchecked")
						boolean a= arg.add(1), b= arg.add(1);
					}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=311849
public void test289() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					
					public class X {
						void foo(ArrayList arg) {
							for (
								@Deprecated
								@Other
								@SuppressWarnings("unchecked")
								boolean a= arg.add(1), b= arg.add(1);
								Boolean.FALSE;
							) {
								System.out.println(a && b);
							}
						}
					}""",
				"Other.java",
				"@interface Other {}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313109
public void test290() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					class X {
						@SuppressWarnings("rawtypes")
						void foo(ArrayList arg) {
							@SuppressWarnings("unchecked")
							boolean aa = arg.add(1), bb = arg.add(1);
							if (bb)
								System.out.println("hi");
						}
					}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313109
public void test291() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					class X {
						@SuppressWarnings("rawtypes")
						void foo(ArrayList arg) {
							@SuppressWarnings("unchecked")
							boolean aa = arg.add(1), bb = arg.add(1);
							if (aa)
								System.out.println("hi");
						}
					}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313109
public void test292() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					class X {
						@SuppressWarnings("rawtypes")
						void foo(ArrayList arg) {
							@SuppressWarnings("unchecked")
							boolean aa = arg.add(1), bb = arg.add(1);
						}
					}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316456
public void test293() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					@A(name = X.QUERY_NAME, query = X.QUERY)
					public class X {
					    public static final String QUERY_NAME = "client.query.name";
					    private static final String QUERY = "from Client";
					}
					@interface A{
					    String name();
					    String query();
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179566
public void test294() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"""
				/** */
				public class A {
					@SuppressWarnings("javadoc")
					public int foo(int i) { return 0; }
				}
				"""
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179566
public void test295() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"""
				/** */
				public class A {
					/**
					 * @param j the given param/
					 */
					@SuppressWarnings("javadoc")
					public int foo(int i) { return 0; }
				}
				"""
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179566
public void test296() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"""
				/** */
				public class A {
					/**
					 * @param i/
					 */
					@SuppressWarnings("javadoc")
					public int foo(int i) { return 0; }
				}
				"""
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=343621
public void test297() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
	runner.customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	runner.customOptions.put(CompilerOptions.OPTION_ReportComparingIdentical, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);

	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in A.java (at line 15)
				return i == i;
				       ^^^^^^
			Comparing identical expressions
			----------
			""";

	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in A.java (at line 10)
					public final Object build(Class<? super Object>... objects) {
					                                                   ^^^^^^^
				Type safety: Potential heap pollution via varargs parameter objects
				----------
				2. ERROR in A.java (at line 15)
					return i == i;
					       ^^^^^^
				Comparing identical expressions
				----------
				""";
	}
	runner.testFiles = new String[] {
			"A.java",
			"""
				public class A {
					public void one() {
						@SuppressWarnings("unused")
						Object object = new Object();
					}
					public void two() {
						@SuppressWarnings({ "unchecked", "unused" })
						Object object = build();
					}
					public final Object build(Class<? super Object>... objects) {
						return null;
					}
					public boolean bar() {
						int i = 0;
						return i == i;
					}
				}"""
	};
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// Bug 366003 - CCE in ASTNode.resolveAnnotations(ASTNode.java:639)
// many syntax errors fixed, does not trigger CCE
public void testBug366003() {
	runNegativeTest(
		new String[] {
			"snippet/Bug366003.java",
			"package snippet;\n" +
			"public class Bug366003 {\n" +
			"    public void foo(@NonNull Object o1) {\n" +
			"        System.out.println(o1.toString()); // OK: o1 cannot be null\n" +
			"    }         \n" +
			"    @NonNull Object bar(@Nullable String s1) {\n" +
			"        foo(null); // cannot pass null argument\n" +
			"        @NonNull String s= null; // cannot assign null value\n" +
			"        @NonNull String t= s1; // cannot assign potentially null value\n" +
			"        return null; // cannot return null value\n" +
			"    }\n" +
			"}\n" +
			"org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)\n" +
			""
		},
		"""
			----------
			1. ERROR in snippet\\Bug366003.java (at line 3)
				public void foo(@NonNull Object o1) {
				                 ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			2. ERROR in snippet\\Bug366003.java (at line 6)
				@NonNull Object bar(@Nullable String s1) {
				 ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			3. ERROR in snippet\\Bug366003.java (at line 6)
				@NonNull Object bar(@Nullable String s1) {
				                     ^^^^^^^^
			Nullable cannot be resolved to a type
			----------
			4. ERROR in snippet\\Bug366003.java (at line 8)
				@NonNull String s= null; // cannot assign null value
				 ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			5. ERROR in snippet\\Bug366003.java (at line 9)
				@NonNull String t= s1; // cannot assign potentially null value
				 ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			6. ERROR in snippet\\Bug366003.java (at line 12)
				}
				^
			Syntax error on token "}", delete this token
			----------
			7. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			8. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			9. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert ";" to complete MethodDeclaration
			----------
			10. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert "}" to complete ClassBody
			----------
			11. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Return type for the method is missing
			----------
			12. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                       ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			13. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                                                      ^^^^^^^^
			Nullable cannot be resolved to a type
			----------
			14. ERROR in snippet\\Bug366003.java (at line 13)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                                                                           ^
			Syntax error, insert ";" to complete ConstructorDeclaration
			----------
			""");
}
// Bug 366003 - CCE in ASTNode.resolveAnnotations(ASTNode.java:639)
// code is garbage, triggers CCE
public void testBug366003b() {
	runNegativeTest(
		new String[] {
			"snippet/Bug366003.java",
			"package snippet;\n" +
			"public class Bug366003 {\n" +
			"    public void foo(@Blah Object o1) {        \n" +
			"System.out.println(o1.toString()); // OK: o1 cannot be null     }         \n" +
			"@Blah Object bar(@BlahBlah String s1) {         foo(null); // cannot pass\n" +
			"null argument         @Blah String s= null; // cannot assign null value     \n" +
			"    @Blah String t= s1; // cannot assign potentially null value         \n" +
			"return null; // cannot return null value     }\n" +
			"}\n" +
			"\n" +
			"org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)\n" +
			""
		},
		"""
			----------
			1. ERROR in snippet\\Bug366003.java (at line 3)
				public void foo(@Blah Object o1) {       \s
				                 ^^^^
			Blah cannot be resolved to a type
			----------
			2. ERROR in snippet\\Bug366003.java (at line 4)
				System.out.println(o1.toString()); // OK: o1 cannot be null     }        \s
				                                 ^
			Syntax error, insert "}" to complete MethodBody
			----------
			3. ERROR in snippet\\Bug366003.java (at line 5)
				@Blah Object bar(@BlahBlah String s1) {         foo(null); // cannot pass
				 ^^^^
			Blah cannot be resolved to a type
			----------
			4. ERROR in snippet\\Bug366003.java (at line 5)
				@Blah Object bar(@BlahBlah String s1) {         foo(null); // cannot pass
				                  ^^^^^^^^
			BlahBlah cannot be resolved to a type
			----------
			5. ERROR in snippet\\Bug366003.java (at line 6)
				null argument         @Blah String s= null; // cannot assign null value    \s
				^^^^
			Syntax error on token "null", @ expected
			----------
			6. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			7. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			8. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert ";" to complete MethodDeclaration
			----------
			9. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				            ^^^^
			Syntax error, insert "}" to complete ClassBody
			----------
			10. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Return type for the method is missing
			----------
			11. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                       ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			12. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                                                      ^^^^^^^^
			Nullable cannot be resolved to a type
			----------
			13. ERROR in snippet\\Bug366003.java (at line 11)
				org.eclipse.User.User(@NonNull String name, int uid, @Nullable String email)
				                                                                           ^
			Syntax error, insert ";" to complete ConstructorDeclaration
			----------
			""");
}
// Bug 366003 - CCE in ASTNode.resolveAnnotations(ASTNode.java:639)
// minimal syntax error to trigger CCE
public void testBug366003c() {
	runNegativeTest(
		new String[] {
			"snippet/Bug366003.java",
			"""
				package snippet;
				public class Bug366003 {
				    void foo(Object o1) {
				    }
				org.User(@Bla String a)"""
		},
		"""
			----------
			1. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				^^^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			2. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				^^^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			3. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				^^^
			Syntax error, insert ";" to complete MethodDeclaration
			----------
			4. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				^^^
			Syntax error, insert "}" to complete ClassBody
			----------
			5. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				    ^^^^^^^^^^^^^^^^^^^
			Return type for the method is missing
			----------
			6. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				          ^^^
			Bla cannot be resolved to a type
			----------
			7. ERROR in snippet\\Bug366003.java (at line 5)
				org.User(@Bla String a)
				                      ^
			Syntax error, insert ";" to complete ConstructorDeclaration
			----------
			""");
}
// unfinished attempt to trigger the same CCE via catch formal parameters
public void testBug366003d() {
	runNegativeTest(
		new String[] {
			"snippet/Bug366003.java",
			"""
				package snippet;\s
				public class Bug366003 {
					void foo() {
						try {
							System.out.println("");
						} catch (Exeption eFirst) {
							e } catch (@Blah Exception eSecond) {
							e }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in snippet\\Bug366003.java (at line 7)
				e } catch (@Blah Exception eSecond) {
				^
			Syntax error, insert "VariableDeclarators" to complete LocalVariableDeclaration
			----------
			2. ERROR in snippet\\Bug366003.java (at line 7)
				e } catch (@Blah Exception eSecond) {
				^
			Syntax error, insert ";" to complete BlockStatements
			----------
			3. ERROR in snippet\\Bug366003.java (at line 8)
				e }
				^
			Syntax error, insert "VariableDeclarators" to complete LocalVariableDeclaration
			----------
			4. ERROR in snippet\\Bug366003.java (at line 8)
				e }
				^
			Syntax error, insert ";" to complete BlockStatements
			----------
			""");
}
public void testBug366003e() {
	runNegativeTest(
		new String[] {
			"snippet/Bug366003.java",
			"""
				package snippet;
				public class Bug366003 {
				        void foo(Object o1){}
				        @Blah org.User(@Bla String str){}
				}
				"""
		},
		"""
			----------
			1. ERROR in snippet\\Bug366003.java (at line 4)
				@Blah org.User(@Bla String str){}
				 ^^^^
			Blah cannot be resolved to a type
			----------
			2. ERROR in snippet\\Bug366003.java (at line 4)
				@Blah org.User(@Bla String str){}
				          ^^^^
			Syntax error on token "User", Identifier expected after this token
			----------
			3. ERROR in snippet\\Bug366003.java (at line 4)
				@Blah org.User(@Bla String str){}
				          ^^^^^^^^^^^^^^^^^^^^^
			Return type for the method is missing
			----------
			4. ERROR in snippet\\Bug366003.java (at line 4)
				@Blah org.User(@Bla String str){}
				                ^^^
			Bla cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437
public void testBug365437a() {
	Map customOptions = getCompilerOptions();
	enableAllWarningsForIrritants(customOptions, IrritantSet.NULL);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	String testFiles [] = new String[] {
			"p/A.java",
			"""
				package p;
				import p1.*;
				public class A {
					@p1.PreDestroy
					private void foo1(){}
					@PreDestroy
					private void foo2(){}
					@SuppressWarnings("null")
					@PostConstruct
					private void foo1a(){}
					@PostConstruct
					private void foo2a(){}
					@Deprecated\
					private void foo3(){}\
				}
				""",
			"p1/PreDestroy.java",
			"package p1;\n" +
			"public @interface PreDestroy{}",
			"p1/PostConstruct.java",
			"package p1;\n" +
			"public @interface PostConstruct{}"
			};
	String expectedErrorString =
			"""
		----------
		1. WARNING in p\\A.java (at line 8)
			@SuppressWarnings("null")
			                  ^^^^^^
		Unnecessary @SuppressWarnings("null")
		----------
		2. ERROR in p\\A.java (at line 13)
			@Deprecated	private void foo3(){}}
			           	             ^^^^^^
		The method foo3() from the type A is never used locally
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437
public void testBug365437b() {
	if (isJRE11Plus)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "p.NonNull");
	String testFiles [] = new String[] {
			"A.java",
			"""
				import javax.annotation.*;
				public class A {
					@javax.annotation.PreDestroy
					private void foo1(){}
					@PreDestroy
					private void foo2(){}
					@javax.annotation.Resource
					private void foo1a(){}
					@Resource
					@p.NonNull
					private Object foo2a(){ return new Object();}
					@javax.annotation.PostConstruct
					@Deprecated
					private void foo3(){}
					@p.NonNull
					private Object foo3a(){ return new Object();}
				}
				""",
			"p/NonNull.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE})
				public @interface NonNull {
				}"""
			};
	String expectedErrorString =
			"""
		----------
		1. ERROR in A.java (at line 16)
			private Object foo3a(){ return new Object();}
			               ^^^^^^^
		The method foo3a() from the type A is never used locally
		----------
		""";
	INameEnvironment save = this.javaClassLib;
	try {
		if (isJRE9Plus) {
			List<String> limitModules = Arrays.asList("java.se", "java.xml.ws.annotation");
			this.javaClassLib = new CustomFileSystem(limitModules);
		}
		runNegativeTest(
				true,
				testFiles,
				null,
				customOptions,
				expectedErrorString,
				isJRE9Plus ? JavacTestOptions.SKIP : JavacTestOptions.Excuse.EclipseWarningConfiguredAsError); // javac9+ cannot access javax.annotation
	} finally {
		this.javaClassLib = save;
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437
// @SafeVarargs
public void testBug365437c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
	Map customOptions = getCompilerOptions();
	enableAllWarningsForIrritants(customOptions, IrritantSet.NULL);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	String testFiles [] = new String[] {
			"p/A.java",
			"""
				package p;
				import p1.*;
				public class A {
					@p1.PreDestroy
					private void foo1(){}
					@PreDestroy
					private void foo2(){}
					@SuppressWarnings("null")
					@PostConstruct
					private void foo1a(){}
					@PostConstruct
					private void foo2a(){}
					@SafeVarargs\
					private final void foo3(Object... o){}\
				}
				""",
			"p1/PreDestroy.java",
			"package p1;\n" +
			"public @interface PreDestroy{}",
			"p1/PostConstruct.java",
			"package p1;\n" +
			"public @interface PostConstruct{}"
			};
	String expectedErrorString =
			"""
		----------
		1. WARNING in p\\A.java (at line 8)
			@SuppressWarnings("null")
			                  ^^^^^^
		Unnecessary @SuppressWarnings("null")
		----------
		2. ERROR in p\\A.java (at line 13)
			@SafeVarargs	private final void foo3(Object... o){}}
			            	                   ^^^^^^^^^^^^^^^^^
		The method foo3(Object...) from the type A is never used locally
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437
// unused constructor
public void testBug365437d() {
	Map customOptions = getCompilerOptions();
	enableAllWarningsForIrritants(customOptions, IrritantSet.NULL);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_NonNullByDefaultAnnotationName, "p.NonNullByDefault");
	this.runNegativeTest(
		true,
		new String[] {
			"Example.java",
			"""
				class Example {
				  @p.Annot
				  private Example() {
				  }
				  public Example(int i) {
				  }
				}
				class E1 {
					 @Deprecated
				    private E1() {}
				    public E1(long l) {}
				}
				class E2 {
					 @SuppressWarnings("null")
				    private E2() {}
				    public E2(long l) {}
				}
				class E3 {
					 @p.NonNullByDefault
				    private E3() {}
				    public E3(long l) {}
				}
				class E4 {
					 @Deprecated
					 @p.Annot
				    private E4() {}
				    public E4(long l) {}
				}
				""",
			"p/NonNullByDefault.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,CONSTRUCTOR})
				public @interface NonNullByDefault {
				}""",
			"p/Annot.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE, CONSTRUCTOR})
				public @interface Annot {
				}"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 10)
				private E1() {}
				        ^^^^
			The constructor E1() is never used locally
			----------
			2. WARNING in Example.java (at line 14)
				@SuppressWarnings("null")
				                  ^^^^^^
			Unnecessary @SuppressWarnings("null")
			----------
			3. ERROR in Example.java (at line 15)
				private E2() {}
				        ^^^^
			The constructor E2() is never used locally
			----------
			4. ERROR in Example.java (at line 20)
				private E3() {}
				        ^^^^
			The constructor E3() is never used locally
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437
// unused field
public void testBug365437e() {
	Map customOptions = getCompilerOptions();
	enableAllWarningsForIrritants(customOptions, IrritantSet.NULL);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "p.NonNull");
	this.runNegativeTest(
		true,
		new String[] {
			"Example.java",
			"""
				class Example {
				  @p.Annot
				  private int Ex;
				}
				class E1 {
					 @Deprecated
				    private int E1;
				}
				class E2 {
					 @SuppressWarnings("null")
				    private int E2;
				}
				class E3 {
					 @p.NonNull
				    private Object E3 = new Object();
				}
				class E4 {
					 @Deprecated
					 @p.Annot
				    private int E4;
				}
				""",
			"p/NonNull.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE, FIELD})
				public @interface NonNull {
				}""",
			"p/Annot.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE, FIELD})
				public @interface Annot {
				}"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 7)
				private int E1;
				            ^^
			The value of the field E1.E1 is not used
			----------
			2. WARNING in Example.java (at line 10)
				@SuppressWarnings("null")
				                  ^^^^^^
			Unnecessary @SuppressWarnings("null")
			----------
			3. ERROR in Example.java (at line 11)
				private int E2;
				            ^^
			The value of the field E2.E2 is not used
			----------
			4. ERROR in Example.java (at line 15)
				private Object E3 = new Object();
				               ^^
			The value of the field E3.E3 is not used
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437
// unused type
public void testBug365437f() {
	Map customOptions = getCompilerOptions();
	enableAllWarningsForIrritants(customOptions, IrritantSet.NULL);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_NonNullByDefaultAnnotationName, "p.NonNullByDefault");
	this.runNegativeTest(
		true,
		new String[] {
			"Example.java",
			"""
				class Example {
				  @p.Annot
				  private class Ex{}
				}
				class E1 {
					 @Deprecated
				    private class E11{}
				}
				class E2 {
					 @SuppressWarnings("null")
				    private class E22{}
				}
				class E3 {
					 @p.NonNullByDefault
				    private class E33{}
				}
				class E4 {
					 @Deprecated
					 @p.Annot
				    private class E44{}
				}
				""",
			"p/NonNullByDefault.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER})
				public @interface NonNullByDefault {
				}""",
			"p/Annot.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE, CONSTRUCTOR})
				public @interface Annot {
				}"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 7)
				private class E11{}
				              ^^^
			The type E1.E11 is never used locally
			----------
			2. WARNING in Example.java (at line 10)
				@SuppressWarnings("null")
				                  ^^^^^^
			Unnecessary @SuppressWarnings("null")
			----------
			3. ERROR in Example.java (at line 11)
				private class E22{}
				              ^^^
			The type E2.E22 is never used locally
			----------
			4. ERROR in Example.java (at line 15)
				private class E33{}
				              ^^^
			The type E3.E33 is never used locally
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/376590 - Private fields with @Inject are ignored by unused field validation
// using com.google.inject.Inject
public void testBug376590a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			GOOGLE_INJECT_NAME,
			GOOGLE_INJECT_CONTENT,
			"Example.java",
			"import com.google.inject.Inject;\n" +
			"class Example {\n" +
			"  private @Inject Object o;\n" +
			"  private @Inject Example() {}\n" + // no warning on constructor
			"  public Example(Object o) { this.o = o; }\n" +
			"  private @Inject void setO(Object o) { this.o = o;}\n" + // no warning on method
			"}\n"
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 3)
				private @Inject Object o;
				                       ^
			The value of the field Example.o is not used
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/376590 - Private fields with @Inject are ignored by unused field validation
// using jakarta.inject.Inject - slight variation
public void testBug376590b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			JAVAX_INJECT_NAME,
			JAVAX_INJECT_CONTENT,
			"Example.java",
			"""
				class Example {
				  private @jakarta.inject.Inject Object o;
				  private Example() {} // also warn here: no @Inject
				  public Example(Object o) { this.o = o; }
				  private @jakarta.inject.Inject void setO(Object o) { this.o = o;}
				}
				"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 2)
				private @jakarta.inject.Inject Object o;
				                                      ^
			The value of the field Example.o is not used
			----------
			2. ERROR in Example.java (at line 3)
				private Example() {} // also warn here: no @Inject
				        ^^^^^^^^^
			The constructor Example() is never used locally
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/376590 - Private fields with @Inject are ignored by unused field validation
// using jakarta.inject.Inject, combined with standard as well as custom annotations
public void testBug376590c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "p.NonNull");
	this.runNegativeTest(
		true,
		new String[] {
			JAVAX_INJECT_NAME,
			JAVAX_INJECT_CONTENT,
			"Example.java",
			"import jakarta.inject.Inject;\n" +
			"class Example {\n" +
			"  private @Inject @p.NonNull Object o; // do warn, annotations don't signal a read\n" +
			"  private @Deprecated @Inject String old; // do warn, annotations don't signal a read\n" +
			"  private @Inject @p.Annot Object o2;\n" + // don't warn, custom annotation could imply a read access
			"}\n",
			"p/NonNull.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE,FIELD})
				public @interface NonNull {
				}""",
			"p/Annot.java",
			"""
				package p;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Target({TYPE, METHOD,PARAMETER,LOCAL_VARIABLE, CONSTRUCTOR, FIELD})
				public @interface Annot {
				}"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 3)
				private @Inject @p.NonNull Object o; // do warn, annotations don't signal a read
				                                  ^
			The value of the field Example.o is not used
			----------
			2. ERROR in Example.java (at line 4)
				private @Deprecated @Inject String old; // do warn, annotations don't signal a read
				                                   ^^^
			The value of the field Example.old is not used
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void testBug376429a() {
	this.runNegativeTest(
			new String[] {
				"Try.java",
				"""
					public @interface Try {\s
						byte[] value();\s
						@Try t();
						@Try u();
					}"""
			},
			"""
				----------
				1. ERROR in Try.java (at line 3)
					@Try t();
					^^^^
				The annotation @Try must define the attribute value
				----------
				2. ERROR in Try.java (at line 3)
					@Try t();
					     ^^^
				Return type for the method is missing
				----------
				3. ERROR in Try.java (at line 3)
					@Try t();
					     ^^^
				Return type for the method is missing
				----------
				4. ERROR in Try.java (at line 4)
					@Try u();
					^^^^
				The annotation @Try must define the attribute value
				----------
				5. ERROR in Try.java (at line 4)
					@Try u();
					     ^^^
				Return type for the method is missing
				----------
				""");
}
public void testBug376429b() {
	this.runNegativeTest(
			new String[] {
				"Try.java",
				"""
					public @interface Try {\s
						@Try t();
						byte[] value();\s
					}"""
			},
			"""
				----------
				1. ERROR in Try.java (at line 2)
					@Try t();
					^^^^
				The annotation @Try must define the attribute value
				----------
				2. ERROR in Try.java (at line 2)
					@Try t();
					     ^^^
				Return type for the method is missing
				----------
				""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=371832
//Unused imports should be reported even if other errors are suppressed.
public void testBug371832() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.ERROR);
	String testFiles [] = new String[] {
			"A.java",
			"""
				import java.util.List;
				@SuppressWarnings("serial")
				public class A implements java.io.Serializable {
					void foo() {\s
					}
				}
				"""
			};
	String expectedErrorString =
			"""
		----------
		1. ERROR in A.java (at line 1)
			import java.util.List;
			       ^^^^^^^^^^^^^^
		The import java.util.List is never used
		----------
		""";
	runNegativeTest(
			true,
			testFiles,
			null,
			customOptions,
			expectedErrorString,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/384663
// Package Based Annotation Compilation Error in JDT 3.8/4.2 (works in 3.7.2)
public void testBug384663() {
	String[] testFiles = {
		"annotations/test/IExtendsInterface.java",
		"package annotations.test;\n" +
		"public interface IExtendsInterface extends Interface {}\n",

		"annotations/test/Interface.java",
		"package annotations.test;\n" +
		"public interface Interface {}\n",

		"annotations/test/package-info.java",
		"""
			@AnnotationDefinition("Test1")\s
			package annotations.test;
			import annotations.AnnotationDefinition;""",

		"annotations/AnnotationDefinition.java",
		"""
			package annotations;
			import java.lang.annotation.*;
			@Retention(RetentionPolicy.RUNTIME)
			@Target(ElementType.PACKAGE)
			public @interface AnnotationDefinition {
				String value();
			}""",
	};
	runConformTest(testFiles);
}

// Bug 386356 - Type mismatch error with annotations and generics
// test case from comment 9
public void _testBug386356_1() {
	runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				import javax.xml.bind.annotation.adapters.XmlAdapter;
				public abstract class X extends XmlAdapter<String,X> {
				}""",

			"p/package-info.java",
			"""
				@XmlJavaTypeAdapters({ @XmlJavaTypeAdapter(value = X.class, type = X.class) })
				package p;
				import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;  \s
				import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;"""

		});
}

// Bug 386356 - Type mismatch error with annotations and generics
// test case from comment 6
public void testBug386356_2() {
	if (isJRE11Plus)
		return;
	INameEnvironment save = this.javaClassLib;
	try {
		if (isJRE9Plus) {
			List<String> limitModules = Arrays.asList("java.se", "java.xml.bind");
			this.javaClassLib = new CustomFileSystem(limitModules);
		}
		runConformTest(
			new String[] {
				"com/ermahgerd/Ermahgerd.java",
				"""
					package com.ermahgerd;
					
					public class Ermahgerd {
					}""",

				"com/ermahgerd/package-info.java",
				"""
					@XmlJavaTypeAdapters({ @XmlJavaTypeAdapter(value = ErmahgerdXmlAdapter.class, type = Ermahgerd.class) })
					package com.ermahgerd;
					import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
					import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;""",

				"com/ermahgerd/ErmahgerdXmlAdapter.java",
				"""
					package com.ermahgerd;
					
					import javax.xml.bind.annotation.adapters.XmlAdapter;
					
					public class ErmahgerdXmlAdapter extends XmlAdapter<String,Ermahgerd> {
					
						@Override
						public String marshal(Ermahgerd arg0) throws Exception {
							// TODO Auto-generated method stub
							return null;
						}
					
						@Override
						public Ermahgerd unmarshal(String arg0) throws Exception {
							// TODO Auto-generated method stub
							return null;
						}
					}"""
			},
			isJRE9Plus ? JavacTestOptions.SKIP : JavacTestOptions.DEFAULT); // javac9+ cannot access javax.xml.bind
	} finally {
		this.javaClassLib = save;
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=398657
public void test398657() throws Exception {
	if (this.complianceLevel != ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"p/Annot.java",
			"""
				package p;
				public @interface Annot {
				   static public enum E { A }
				   E getEnum();
				}""",
			"X.java",
			"""
				import static p.Annot.E.*;
				import p.Annot;\
				@Annot(getEnum=A)
				public class X {}"""
		},
		"",
		null,
		true,
		null,
		options,
		null,
		true);

	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #22 p/Annot$E, outer class info: #24 p/Annot
		     inner name: #26 E, accessflags: 16409 public static final]
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=398657
public void test398657_2() throws Exception {
	if (this.complianceLevel != ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"p/Y.java",
			"""
				package p;
				public class Y {
					static public @interface Annot {
						int id();
					}
				}""",
			"X.java",
			"""
				import p.Y.Annot;
				@Annot(id=4)
				public class X {}"""
		},
		"",
		null,
		true,
		null,
		options,
		null,
		true);

	String expectedOutput =
			"""
		  Inner classes:
		    [inner class info: #21 p/Y$Annot, outer class info: #23 p/Y
		     inner name: #25 Annot, accessflags: 9737 public abstract static]
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
// check invalid and annotations on package
public void test384567() {
	this.runNegativeTest(
		new String[] {
			"xy/X.java",
			"""
				public final synchronized @Foo private package xy;
				class X {
				}
				
				@interface Foo {
				}
				"""
		},
		"""
			----------
			1. ERROR in xy\\X.java (at line 1)
				public final synchronized @Foo private package xy;
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Syntax error, modifiers are not allowed here
			----------
			2. ERROR in xy\\X.java (at line 1)
				public final synchronized @Foo private package xy;
				                          ^^^^
			Package annotations must be in file package-info.java
			----------
			""");
}
//check invalid modifiers on package
public void test384567_2() {
	this.runNegativeTest(
		new String[] {
			"xy/X.java",
			"""
				public final synchronized private package xy;
				class X {
				}
				
				"""
		},
		"""
			----------
			1. ERROR in xy\\X.java (at line 1)
				public final synchronized private package xy;
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Syntax error, modifiers are not allowed here
			----------
			""");
}
// Bug 416107 - Incomplete error message for member interface and annotation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=416107
public void test416107a() {
	if (this.complianceLevel < ClassFileConstants.JDK16) {
	    this.runNegativeTest(
	            new String[] {
	                "X.java",
	    			"""
						public class X {
							class Y {
								 @interface Bar {
									public String bar = "BUG";
								}
							}
						}""",
	            },
	            """
					----------
					1. ERROR in X.java (at line 3)
						@interface Bar {
						           ^^^
					The member annotation Bar can only be defined inside a top-level class or interface or in a static context
					----------
					""");
	}	else {
	    	    this.runConformTest(
	    	            new String[] {
	    	                "X.java",
	    	    			"""
								public class X {
									class Y {
										 @interface Bar {
											public String bar = "BUG";
										}
									}
								}""",
	    	            },
	    	            "");

	}
}
public void test416107b() {
	if (this.complianceLevel < ClassFileConstants.JDK16) {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							class Y {
								interface Bar {
									public String bar = "BUG";
								}
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						interface Bar {
						          ^^^
					The member interface Bar can only be defined inside a top-level class or interface or in a static context
					----------
					""");
	} else {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							class Y {
								interface Bar {
									public String bar = "BUG";
								}
							}
						}""",
				},
				"");

	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427367
public void test427367() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				@interface Annot1 {
				   Thread.State value() default Thread.State.NEW;
				   int value2() default 1;
				}
				@interface Annot2 {
				   Thread.State value() default Thread.State.NEW;
				}
				@Annot1(value = XXThread.State.BLOCKED, value2 = 42)
				@Annot2(value = XYThread.State.BLOCKED)
				public class X {}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				@Annot1(value = XXThread.State.BLOCKED, value2 = 42)
				                ^^^^^^^^
			XXThread cannot be resolved to a variable
			----------
			2. ERROR in X.java (at line 9)
				@Annot2(value = XYThread.State.BLOCKED)
				                ^^^^^^^^
			XYThread cannot be resolved to a variable
			----------
			""",
		null,
		true,
		null,
		true, // generate output
		false,
		false);

	String expectedOutput = """
		@Annot1@Annot2
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 3, Locals: 1
		  public X();
		     0  new java.lang.Error [8]
		     3  dup
		     4  ldc <String "Unresolved compilation problems: \\n\\tXXThread cannot be resolved to a variable\\n\\tXYThread cannot be resolved to a variable\\n"> [10]
		     6  invokespecial java.lang.Error(java.lang.String) [12]
		     9  athrow
		      Line numbers:
		        [pc: 0, line: 8]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		
		}""";
	try {
		checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
	} catch(org.eclipse.jdt.core.util.ClassFormatException cfe) {
		fail("Error reading classfile");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=376977
public void test376977() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import p.Outer;
				@Outer(nest= {@Nested()})
				public class X {}""",
			"p/Outer.java",
			"""
				package p;
				public @interface Outer {
				   Nested[] nest();\
				}""",
			"p/Nested.java",
			"""
				package p;
				public @interface Nested {
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@Outer(nest= {@Nested()})
				               ^^^^^^
			Nested cannot be resolved to a type
			----------
			""",
		null,
		true,
		null,
		false,
		false,
		false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438437 - [1.8][compiler] Annotations
// on enum constants interpreted only as type annotations if the annotation type
// specifies ElementType.TYPE_USE in @Target along with others.
public void test438437() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"@Target({ElementType.TYPE_USE, ElementType.FIELD})\n" +
			"@interface TUF {} \n" +
			"@Target({ElementType.FIELD})\n" +
			"@interface F {} \n" +
			"@Target({ElementType.TYPE_USE})\n" +
			"@interface TU1 {} \n" +
			"@Target({ElementType.LOCAL_VARIABLE})\n" +
			"@interface LV {} \n" +
			"@Target({ElementType.TYPE_USE})\n" +
			"@interface TU2 {} \n" +
			"class Y {}\n" +
			"public enum X {\n" +
			"	@TUF E1,\n" + // Error without the fix.
			"	@F E2,\n" +
			"	@TU1 E3,\n" + // Error is reported as no type exists for the Enum.
			"	@LV E4,\n" +
			"	@TUF @TU1 @F E5,\n" +
			"	@TUF @TU1 @F @TU2 E6;\n" +
			"	@TUF Y y11;\n" +
			"	@F Y y12;\n" +
			"	@TU1 Y y13;\n" + // No error reported as type exists.
			"	@LV Y y14;\n" +
			"}\n" ,
		},
		"""
			----------
			1. ERROR in X.java (at line 17)
				@TU1 E3,
				^^^^
			Syntax error, type annotations are illegal here
			----------
			2. ERROR in X.java (at line 18)
				@LV E4,
				^^^
			The annotation @LV is disallowed for this location
			----------
			3. ERROR in X.java (at line 19)
				@TUF @TU1 @F E5,
				     ^^^^
			Syntax error, type annotations are illegal here
			----------
			4. ERROR in X.java (at line 20)
				@TUF @TU1 @F @TU2 E6;
				     ^^^^
			Syntax error, type annotations are illegal here
			----------
			5. ERROR in X.java (at line 20)
				@TUF @TU1 @F @TU2 E6;
				             ^^^^
			Syntax error, type annotations are illegal here
			----------
			6. ERROR in X.java (at line 24)
				@LV Y y14;
				^^^
			The annotation @LV is disallowed for this location
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434556,  Broken class file generated for incorrect annotation usage
public void test434556() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				@Retention(RetentionPolicy.RUNTIME)
				@interface C {
					int i();
				}
				public class A {
				  @C(b={},i=42)
				  public void xxx() {}
				  public static void main(String []argv) throws Exception {
					System.out.println(A.class.getDeclaredMethod("xxx").getAnnotations()[0]); \s
				  }
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 8)
				@C(b={},i=42)
				   ^
			The attribute b is undefined for the annotation type C
			----------
			""",
		null,
		true,
		null,
		true, // generate output
		false,
		false);

	String expectedOutput = """
		@C(i=(int) 42)
		  public void xxx();
		     0  new java.lang.Error [20]
		     3  dup
		     4  ldc <String "Unresolved compilation problem: \\n\\tThe attribute b is undefined for the annotation type C\\n"> [22]
		     6  invokespecial java.lang.Error(java.lang.String) [24]
		     9  athrow
		      Line numbers:
		        [pc: 0, line: 8]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: A
		 \s
		""";
	try {
		checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"A.class", "A", expectedOutput, ClassFileBytesDisassembler.DETAILED);
	} catch(org.eclipse.jdt.core.util.ClassFormatException cfe) {
		fail("Error reading classfile");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433747, [compiler] TYPE Annotation allowed in package-info instead of only PACKAGE
public void test433747() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String[] src = new String[] {
			"p/package-info.java",
			"""
				@PackageAnnot("p123456")
				package p;
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				@Target(ElementType.TYPE)
				@interface PackageAnnot {
					String value();
				}
				"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_6) {
		this.runConformTest(src, "");
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p/package-info.class", "", "p123456");
	} else {
	this.runNegativeTest(
			src,
			"""
				----------
				1. ERROR in p\\package-info.java (at line 1)
					@PackageAnnot("p123456")
					^^^^^^^^^^^^^
				The annotation @PackageAnnot is disallowed for this location
				----------
				""",
			null,
			true,
			null,
			true, // generate output
			false,
			false);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=456960 - Broken classfile generated for incorrect annotation usage - case 2
public void test456960() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				@Bar(String)
				public class X {
				}""",
			"Bar.java",
			"""
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				@Retention(RetentionPolicy.RUNTIME)
				@interface Bar {
					Class<?>[] value();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				@Bar(String)
				     ^^^^^^
			String cannot be resolved to a variable
			----------
			""",
		null,
		true,
		null,
		true, // generate output
		false,
		false);

	String expectedOutput =
			"""
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 3, Locals: 1
		  public X();
		     0  new java.lang.Error [8]
		     3  dup
		     4  ldc <String "Unresolved compilation problem: \\n\\tString cannot be resolved to a variable\\n"> [10]
		     6  invokespecial java.lang.Error(java.lang.String) [12]
		     9  athrow
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		}""";
	try {
		checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
	} catch(org.eclipse.jdt.core.util.ClassFormatException cfe) {
		fail("Error reading classfile");
	}

}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=449330 - [1.6]Eclipse compiler doesn't compile annotations in class files
public void test449330() throws Exception {
	String[] testFiles = new String[] {
		"p/X.java",
		"""
			package p;
			@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})
			@interface X { public java.lang.String name(); }
			""",
		"p/package-info.java",
		"@X(name=\"HELLO\")\n" +
		"package p;\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_6) {
		this.runConformTest(testFiles);
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p/package-info.class", "", "HELLO");
	} else {
		this.runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in p\\package-info.java (at line 1)
					@X(name="HELLO")
					^^
				The annotation @X is disallowed for this location
				----------
				""");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=449330 - [1.6]Eclipse compiler doesn't compile annotations in class files
//Retention Policy set to RUNTIME
public void test449330a() throws Exception {
	String[] testFiles = new String[] {
		"p/X.java",
		"""
			package p;
			@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})
			@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
			@interface X { public java.lang.String name(); }
			""",
		"p/package-info.java",
		"@X(name=\"HELLO\")\n" +
		"package p;\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_6) {
		this.runConformTest(testFiles, "");
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p/package-info.class", "", "HELLO");
	} else {
		this.runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in p\\package-info.java (at line 1)
					@X(name="HELLO")
					^^
				The annotation @X is disallowed for this location
				----------
				""");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=449330 - [1.6]Eclipse compiler doesn't compile annotations in class files
//Annotation target not set
public void test449330b() throws Exception {
	String[] testFiles = new String[] {
		"p/X.java",
		"package p;\n" +
		"@interface X { public java.lang.String name(); }\n",
		"p/package-info.java",
		"@X(name=\"HELLO\")\n" +
		"package p;\n"
	};
	this.runConformTest(testFiles, "");
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p/package-info.class", "", "HELLO");
}
//https://bugs.eclipse.org/386692
public void testBug386692() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			SPRINGFRAMEWORK_AUTOWIRED_NAME,
			SPRINGFRAMEWORK_AUTOWIRED_CONTENT,
			"Example.java",
			"""
				class Example {
				  private @org.springframework.beans.factory.annotation.Autowired Object o;
				  private Example() {}
				  public Example(Object o) { this.o = o; }
				  private @org.springframework.beans.factory.annotation.Autowired void setO(Object o) { this.o = o;}
				}
				"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in Example.java (at line 2)
				private @org.springframework.beans.factory.annotation.Autowired Object o;
				                                                                       ^
			The value of the field Example.o is not used
			----------
			2. ERROR in Example.java (at line 3)
				private Example() {}
				        ^^^^^^^^^
			The constructor Example() is never used locally
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=464977
public void testBug464977() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_6 || this.complianceLevel > ClassFileConstants.JDK1_8) {
		return; // Enough to run in 3 levels rather!
	}
	boolean apt = this.enableAPT;
	String source = """
		@Deprecated
		public class DeprecatedClass {
		}""";
	String version = "";
	if  (this.complianceLevel == ClassFileConstants.JDK1_8) {
		version = "1.8 : 52.0";
	} else if  (this.complianceLevel == ClassFileConstants.JDK1_7) {
		version = "1.7 : 51.0";
	} else if  (this.complianceLevel == ClassFileConstants.JDK1_6) {
		version = "1.6 : 50.0";
	}
	String expectedOutput = "// Compiled from DeprecatedClass.java (version " + version + ", super bit, deprecated)\n" +
							"@Deprecated\n" +
							"public class DeprecatedClass {\n" +
							"  \n" +
							"  // Method descriptor #6 ()V\n" +
							"  // Stack: 1, Locals: 1\n" +
							"  public DeprecatedClass();\n" +
							"    0  aload_0 [this]\n" +
							"    1  invokespecial Object() [8]\n" +
							"    4  return\n" +
							"      Line numbers:\n" +
							"        [pc: 0, line: 2]\n" +
							"      Local variable table:\n" +
							"        [pc: 0, pc: 5] local: this index: 0 type: DeprecatedClass\n" +
							"\n" +
							"}";
	try {
		this.enableAPT = true;
		checkClassFile("DeprecatedClass", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	} finally {
		this.enableAPT = apt;
	}
}
public void testBug469584() {
	runNegativeTest(
		new String[] {
			"CCETest.java",
			"""
				import java.lang.annotation.*;
				
				@Retention({RetentionPolicy.CLASS, RetentionPolicy.RUNTIME})
				public @interface CCETest {
				
				}
				"""
		},
		"""
			----------
			1. ERROR in CCETest.java (at line 3)
				@Retention({RetentionPolicy.CLASS, RetentionPolicy.RUNTIME})
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from RetentionPolicy[] to RetentionPolicy
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=472178
public void test472178() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		return; // Enough to run in 3 levels rather!
	}
	String source =
			"""
		import java.lang.annotation.ElementType;
		import java.lang.annotation.Retention;
		import java.lang.annotation.RetentionPolicy;
		import java.lang.annotation.Target;
		import java.util.ArrayList;
		import java.util.Iterator;
		\s
		/**
		 * @author gglab
		 */
		public class Test<X> extends ArrayList<X> {
		    public void iterateRemove()
		    {
		        for (Iterator<X> iter = this.iterator(); iter.hasNext();) {
		            Object key = iter.next();
		            @Flowannotation
		            Foo<@Flowannotation String> f = new Foo<String>();
		            @Flowannotation long l = (@Flowannotation long)f.getI(); // this line causes parse error
		            iter.remove();
		        }
		    }
		\s
		    @Flowannotation
		    class Foo<@Flowannotation T>
		    {
		        @Flowannotation
		        public int getI()
		        {
		            return 3;
		        }
		    }
		\s
		    @Target({ ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE,           ElementType.TYPE, ElementType.FIELD,
		            ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
		    @Retention(RetentionPolicy.RUNTIME)
		    @interface Flowannotation {}
		    public static void main(String[] args) {}
		}""";
	String expectedOutput =
			"""
		  // Method descriptor #6 ()V
		  // Stack: 3, Locals: 6
		  public void iterateRemove();
		     0  aload_0 [this]
		     1  invokevirtual Test.iterator() : Iterator [17]
		     4  astore_1 [iter]
		     5  goto 37
		     8  aload_1 [iter]
		     9  invokeinterface Iterator.next() : Object [21] [nargs: 1]
		    14  astore_2 [key]
		    15  new Test$Foo [27]
		    18  dup
		    19  aload_0 [this]
		    20  invokespecial Test$Foo(Test) [29]
		    23  astore_3 [f]
		    24  aload_3 [f]
		    25  invokevirtual Test$Foo.getI() : int [32]
		    28  i2l
		    29  lstore 4 [l]
		    31  aload_1 [iter]
		    32  invokeinterface Iterator.remove() : void [36] [nargs: 1]
		    37  aload_1 [iter]
		    38  invokeinterface Iterator.hasNext() : boolean [39] [nargs: 1]
		    43  ifne 8
		    46  return
		      Line numbers:
		        [pc: 0, line: 14]
		        [pc: 8, line: 15]
		        [pc: 15, line: 17]
		        [pc: 24, line: 18]
		        [pc: 31, line: 19]
		        [pc: 37, line: 14]
		        [pc: 46, line: 21]
		      Local variable table:
		        [pc: 0, pc: 47] local: this index: 0 type: Test
		        [pc: 5, pc: 46] local: iter index: 1 type: Iterator
		        [pc: 15, pc: 37] local: key index: 2 type: Object
		        [pc: 24, pc: 37] local: f index: 3 type: Foo
		        [pc: 31, pc: 37] local: l index: 4 type: long
		      Local variable type table:
		        [pc: 0, pc: 47] local: this index: 0 type: Test<X>
		        [pc: 5, pc: 46] local: iter index: 1 type: Iterator<X>
		        [pc: 24, pc: 37] local: f index: 3 type: String>
		      Stack map table: number of frames 2
		        [pc: 8, append: {Iterator}]
		        [pc: 37, same]
		    RuntimeVisibleTypeAnnotations:\s
		      #55 @Flowannotation(
		        target type = 0x47 CAST
		        offset = 24
		        type argument index = 0
		      )
		      #55 @Flowannotation(
		        target type = 0x40 LOCAL_VARIABLE
		        local variable entries:
		          [pc: 24, pc: 37] index: 3
		        location = [INNER_TYPE]
		      )
		      #55 @Flowannotation(
		        target type = 0x40 LOCAL_VARIABLE
		        local variable entries:
		          [pc: 24, pc: 37] index: 3
		        location = [INNER_TYPE, TYPE_ARGUMENT(0)]
		      )
		      #55 @Flowannotation(
		        target type = 0x40 LOCAL_VARIABLE
		        local variable entries:
		          [pc: 31, pc: 37] index: 4
		      )
		""";
	checkClassFile("Test", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=470665
public void testBug470665() throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_7) {
		return; // Enough to run in the last two levels!
	}
	boolean apt = this.enableAPT;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in A.java (at line 10)
					};
					^
				Syntax error on token "}", delete this token
				----------
				----------
				"""
			:
			"""
				----------
				1. ERROR in A.java (at line 10)
					};
					^
				Syntax error on token "}", delete this token
				----------
				----------
				1. WARNING in B.java (at line 12)
					X x = new X();
					      ^^^^^^^
				Access to enclosing constructor B.X() is emulated by a synthetic accessor method
				----------
				""";
	String[] sources = new String[] {
			"A.java",
			"""
				public final class A {
					String myString;
					public interface B {
						void test();
					}
					private final B b = new B() {
						@Override
						public void test() {}
					}
				};
				}""",
			"B.java",
			"""
				public class B {
					  private static class X {
					    static final Object instance1;
					    static {
					      try {
					        instance1 = new Object();
					      } catch (Throwable e) {
					        throw new AssertionError(e);
					      }
					    }
					  }
					  X x = new X();
					  Object o = X.instance1;
				}"""
	};
	try {
		this.enableAPT = true;
		runNegativeTest(sources, errMessage);
	} finally {
		this.enableAPT = apt;
	}
}
public void testBug506888a() throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"incomplete-switch"})
						void foo() {
						}
					}\t
					""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. INFO in X.java (at line 3)
				@SuppressWarnings({"incomplete-switch"})
				                   ^^^^^^^^^^^^^^^^^^^
			At least one of the problems in category 'incomplete-switch' is not analysed due to a compiler option being ignored
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug506888b() throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"incomplete-switch"})
						void foo(Color c) {
							switch(c) {
							}
						}
						enum Color { BLUE, RED; }\s
					}\t
					""",
		},
		options);
}
public void testBug506888c() throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.WARNING);
	runner.customOptions.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"incomplete-switch", "unchecked"})
						void foo(Color c) {
							switch(c) {
							}
						}
						enum Color { BLUE, RED; }\s
					}\t
					""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in X.java (at line 3)
				@SuppressWarnings({"incomplete-switch", "unchecked"})
				                                        ^^^^^^^^^^^
			Unnecessary @SuppressWarnings("unchecked")
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug506888d() throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"incomplete-switch"})
						void foo() {
						}
					}\t
					""",
		},
		"",
		null, true, options);
}
public void testBug506888e() throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedLabel, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"unused"})
						void foo() {}
					}\t
					""",
		},
		"",
		null, true, options);
}
public void testBug506888f() throws Exception {

	class MyCompilerRequestor implements ICompilerRequestor {
		String[] problemArguments = null;

		@Override
		public void acceptResult(CompilationResult result) {
			for (CategorizedProblem problem : result.getAllProblems()) {
				String[] arguments = problem.getArguments();
				if (arguments != null && arguments.length > 0) {
					this.problemArguments = arguments;
					return;
				}
			}
		}
	}

	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	MyCompilerRequestor requestor = new MyCompilerRequestor();
	runTest(new String[] {
				"X.java",
				"""
					public class X {
					\t
						@SuppressWarnings({"unused"})
						void foo() {
						}
					}\t
					""",
			},
			false,
			"""
				----------
				1. INFO in X.java (at line 3)
					@SuppressWarnings({"unused"})
					                   ^^^^^^^^
				At least one of the problems in category 'unused' is not analysed due to a compiler option being ignored
				----------
				""",
			"" /*expectedOutputString */,
			"" /* expectedErrorString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			options,
			new Requestor(true, requestor, false, true),
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	assertNotNull(requestor.problemArguments);
	assertEquals(1, requestor.problemArguments.length);
	assertEquals(JavaCore.COMPILER_PB_UNUSED_PARAMETER, requestor.problemArguments[0]);
}
public void testBug537593_001() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	class MyCompilerRequestor implements ICompilerRequestor {
		String[] problemArguments = null;

		@Override
		public void acceptResult(CompilationResult result) {
			for (CategorizedProblem problem : result.getAllProblems()) {
				String[] arguments = problem.getArguments();
				if (arguments != null && arguments.length > 0) {
					this.problemArguments = arguments;
					return;
				}
			}
		}
	}

	if (this.complianceLevel <= ClassFileConstants.JDK1_5) {
		return;
	}
	String[] files = new String[] {
			"X.java",
			"""
				
				public class X {
				
					protected void bar(Z z) {
						System.out.println(z.toString());
					}
				
					public void foo() {
						bar(() -> {
							foo2(new I() {
								@SuppressWarnings({"unused"})
								public void bar2() {}
							});
						});
					}
					public static void main(String[] args) {}
				
					public Z foo2(I i) {
						return i == null ? null : null;
					}
				}
				
				interface Z {
					void apply();
				}
				
				interface I {}
				
				""",
	};

	Map options = getCompilerOptions();
	Object[] opts = {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException,
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable,
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference,
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionWhenOverriding,
			CompilerOptions.OPTION_ReportUnusedExceptionParameter,
			CompilerOptions.OPTION_ReportUnusedImport,
			CompilerOptions.OPTION_ReportUnusedLabel,
			CompilerOptions.OPTION_ReportUnusedLocal,
			CompilerOptions.OPTION_ReportUnusedObjectAllocation,
			CompilerOptions.OPTION_ReportUnusedParameter,
			CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete,
			CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
			CompilerOptions.OPTION_ReportUnusedPrivateMember,
			CompilerOptions.OPTION_ReportUnusedTypeArgumentsForMethodInvocation,
			CompilerOptions.OPTION_ReportUnusedTypeParameter,
			CompilerOptions.OPTION_ReportUnusedWarningToken,
			CompilerOptions.OPTION_ReportRedundantSuperinterface,
			CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments,
	};
	for (Object option : opts)
		options.put(option, CompilerOptions.WARNING);
	MyCompilerRequestor requestor = new MyCompilerRequestor();
	runTest(files,
			false,
			"",
			"" /*expectedOutputString */,
			"" /* expectedErrorString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */,
			null /* vmArguments */,
			options,
			new Requestor(true, requestor, false, true),
			JavacTestOptions.DEFAULT);
	assertNull(requestor.problemArguments);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=542520 - [JUnit 5] Warning The method xxx from the type X
// is never used locally is shown when using MethodSource - common case
public void testBug542520a() throws Exception {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import org.junit.jupiter.params.provider.MethodSource;
				public class ExampleTest {
				
					 @MethodSource("getIntegers")
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> getIntegers() {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		};
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=542520 - [JUnit 5] Warning The method xxx from the type X
// is never used locally is shown when using MethodSource - variation with fully qualified annotation
public void testBug542520b() throws Exception {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				public class ExampleTest {
				
					 @org.junit.jupiter.params.provider.MethodSource("getIntegers")
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> getIntegers() {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		};
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=542520 - [JUnit 5] Warning The method xxx from the type X
// is never used locally is shown when using MethodSource - marker annotation
public void testBug542520c() throws Exception {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import org.junit.jupiter.params.provider.MethodSource;
				public class ExampleTest {
				
					 @MethodSource
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> testIntegers() {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		};
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=542520 - [JUnit 5] Warning The method xxx from the type X
// is never used locally is shown when using MethodSource - missing no-args method source
public void testBug542520d() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import org.junit.jupiter.params.provider.MethodSource;
				public class ExampleTest {
				
					 @MethodSource("getIntegers")
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> getIntegers(int i) {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in ExampleTest.java (at line 9)
				private static List<Integer> getIntegers(int i) {
				                             ^^^^^^^^^^^^^^^^^^
			The method getIntegers(int) from the type ExampleTest is never used locally
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=546084 - Using Junit 5s MethodSource leads to
// ClassCastException - string concatenation, i.e. BinaryExpression in @MethodSource annotation
public void testBug546084a() throws Exception {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import org.junit.jupiter.params.provider.MethodSource;
				public class ExampleTest {
				
					 private final String TEST_METHOD_PREFIX = "get";
					 @MethodSource(TEST_METHOD_PREFIX + "Integers")
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> getIntegers() {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		};
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=546084 - Using Junit 5s MethodSource leads to
// ClassCastException - non string value, e.g. ClassLiteralAccess in @MethodSource annotation
public void testBug546084b() throws Exception {
	this.runNegativeTest(
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import org.junit.jupiter.params.provider.MethodSource;
				public class ExampleTest {
				
					 @MethodSource(Object.class)
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> getIntegers(int i) {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in ExampleTest.java (at line 6)
				@MethodSource(Object.class)
				              ^^^^^^^^^^^^
			Type mismatch: cannot convert from Class<Object> to String[]
			----------
			2. WARNING in ExampleTest.java (at line 9)
				private static List<Integer> getIntegers(int i) {
				                             ^^^^^^^^^^^^^^^^^^
			The method getIntegers(int) from the type ExampleTest is never used locally
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=546084 - Using Junit 5s MethodSource leads to
//ClassCastException - array of string values, e.g. ArrayInitializer in @MethodSource annotation
public void testBug546084c() throws Exception {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			JUNIT_METHODSOURCE_NAME,
			JUNIT_METHODSOURCE_CONTENT,
			"ExampleTest.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import org.junit.jupiter.params.provider.MethodSource;
				public class ExampleTest {
				
					 @MethodSource({ "getIntegers" })
					 void testIntegers(Integer integer) {}
					\s
					 private static List<Integer> getIntegers() {
						return Arrays.asList(0, 5, 1);
					}
				}
				""",
		};
	runner.runConformTest();
}
public void testBug490698_comment16() {
	runConformTest(
		new String[]  {
			"foo/bar/AnnotationError.java",
			"""
				package foo.bar;
				
				import static java.lang.annotation.ElementType.FIELD;
				import static java.lang.annotation.RetentionPolicy.RUNTIME;
				
				import java.lang.annotation.Retention;
				import java.lang.annotation.Target;
				import java.util.function.Predicate;
				
				public class AnnotationError<T> {
				
					public enum P {
						AAA
					}
				
					@Target(FIELD)
					@Retention(RUNTIME)
					public @interface A {
						P value();
					}
				
					@Target(FIELD)
					@Retention(RUNTIME)
					public @interface FF {
					}
				
					public static class Bool extends AnnotationError<Boolean> {
					}
				
					@A(P.AAA)
					@FF
					public static final AnnotationError.Bool FOO = new AnnotationError.Bool();
				}
				"""
		});
}

public void testBugVisibility() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String z() { return Y.MSG; }
					@Deprecated(since = Y.MSG)
					static class Y {
						private final static String MSG = "msg";
					}
				}""",
		},
		"");
}
public void testIssue2400() {
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		return;
	}
	Map customOptions = getCompilerOptions();
	Object bkup = customOptions.get(CompilerOptions.OPTION_AnnotationBasedNullAnalysis);
	customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
	try {
		runNegativeTest(
			new String[] {
				"TestClass.java",
				"""
					package test;
					@com.Missing
					@java.lang.Deprecated
					public class TestClass {
					}""",
				"com.java",
				"""
					package test;
					public class com {
						test.TestClass value;\
					}""",
			},
			"""
				----------
				1. ERROR in TestClass.java (at line 2)
					@com.Missing
					 ^^^^^^^^^^^
				com.Missing cannot be resolved to a type
				----------
				----------
				1. WARNING in com.java (at line 3)
					test.TestClass value;}
					     ^^^^^^^^^
				The type TestClass is deprecated
				----------
				""",
			null,
			true,
			customOptions);
	} finally {
		customOptions.put(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, bkup);
	}
}

}
