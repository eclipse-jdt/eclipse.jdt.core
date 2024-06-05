/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ParserTest extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 18 };
//		TESTS_RANGE = new int[] { 11, -1 };
}
public ParserTest(String name) {
	super(name);
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(){
						throws
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws
				^^^^^^
			Syntax error on token "throws", delete this token
			----------
			"""
	);
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(){
						throws new
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws new
				^^^^^^^^^^
			Syntax error on tokens, delete these tokens
			----------
			"""
	);
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(){
						throws new X
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws new X
				^^^^^^
			Syntax error on token "throws", throw expected
			----------
			2. ERROR in X.java (at line 3)
				throws new X
				           ^
			Syntax error, insert "( )" to complete Expression
			----------
			3. ERROR in X.java (at line 3)
				throws new X
				           ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			""");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					{
						throws
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws
				^^^^^^
			Syntax error on token "throws", delete this token
			----------
			"""
	);
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					{
						throws new
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws new
				^^^^^^^^^^
			Syntax error on tokens, delete these tokens
			----------
			"""
	);
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					{
						throws new X
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws new X
				^^^^^^
			Syntax error on token "throws", throw expected
			----------
			2. ERROR in X.java (at line 3)
				throws new X
				           ^
			Syntax error, insert "( )" to complete Expression
			----------
			3. ERROR in X.java (at line 3)
				throws new X
				           ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			""");
}
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo()throw {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X {
				               ^
			Syntax error, insert "}" to complete ClassBody
			----------
			2. ERROR in X.java (at line 2)
				void foo()throw {
				          ^^^^^
			Syntax error on token "throw", { expected
			----------
			"""
	);
}
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo()throw E {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X {
				               ^
			Syntax error, insert "}" to complete ClassBody
			----------
			2. ERROR in X.java (at line 2)
				void foo()throw E {
				          ^^^^^
			Syntax error on token "throw", throws expected
			----------
			3. ERROR in X.java (at line 4)
				}
				^
			Syntax error on token "}", delete this token
			----------
			"""
	);
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(){
						throws e
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws e
				^^^^^^^^
			Syntax error on tokens, delete these tokens
			----------
			"""
	);
}
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(){
						throws e;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				throws e;
				^^^^^^
			Syntax error on token "throws", throw expected
			----------
			"""
	);
}
public void _test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo(X, Object o, String s) {
					}
				   public void bar(){}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(X, Object o, String s) {
				                 ^
			Syntax error on token ",", . expected
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test012() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo() {
						"foo".equals("bar");
						;
					}
				}
				"""
		},
		null, nls,
		"""
			----------
			1. ERROR in X.java (at line 3)
				"foo".equals("bar");
				^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in X.java (at line 3)
				"foo".equals("bar");
				             ^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test013() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo() {
						"foo".equals("bar");
						//;
					}
				}
				"""
		},
		null, nls,
		"""
			----------
			1. ERROR in X.java (at line 3)
				"foo".equals("bar");
				^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in X.java (at line 3)
				"foo".equals("bar");
				             ^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47227
 */
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo() {\s
						import java.lang.*;
					}\s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				import java.lang.*;
				^^^^^^
			Syntax error on token "import", delete this token
			----------
			2. ERROR in X.java (at line 3)
				import java.lang.*;
				^^^^^^^^^^^^^^^^^
			Syntax error on token(s), misplaced construct(s)
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				// some code
				}
				/*
				// some comments
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				/*
			// some comments
			
				^^^^^^^^^^^^^^^^^^^^
			Unexpected end of comment
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String s = \""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				String s = "
				           ^
			String literal is not properly closed by a double-quote
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	char c = '"
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				char c = '
				         ^
			Invalid character constant
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
 */
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	char c = '\\u0"
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				char c = '\\u0
				          ^^^
			Invalid unicode
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=12287
 */
public void test019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void doit() {
						int[] foo = null;
						foo[0] =\s
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				foo[0] =\s
				     ^
			Syntax error, insert "AssignmentOperator Expression" to complete Assignment
			----------
			2. ERROR in X.java (at line 4)
				foo[0] =\s
				     ^
			Syntax error, insert ";" to complete Statement
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=38895
 */
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					}
					public static int newLibraryEntry() {
						if (sourceAttachmentPath != null) {
							if (sourceAttachmentPath.isEmpty()) { && !
				sourceAttachmentPath.isAbsolute()) {
							foo();
						}
						return null;
					}
					}
					public void foo() {
					}
					public void bar() {
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (sourceAttachmentPath.isEmpty()) { && !
				                                      ^^
			Syntax error on token "&&", invalid (
			----------
			2. ERROR in X.java (at line 7)
				sourceAttachmentPath.isAbsolute()) {
				                                   ^
			Syntax error on token "{", invalid AssignmentOperator
			----------
			"""
	);
}
public void test021() {
	StringBuilder buffer = new StringBuilder();
	buffer.append("public class X {\n");
	for (int i = 0; i < 1000; i++) {
		buffer.append("\tint field_" + i + " = 0; \n");
	}
	for (int i = 0; i < 1000; i++) {
		if (i == 0)
			buffer.append("\tvoid method_" + i + "() { /* default */ } \n");
		else
			buffer.append("\tvoid method_" + i + "() { method_" + (i - 1) + "() \n");
	}
	buffer.append("}\n");

	Hashtable options = new Hashtable();
	options.put(CompilerOptions.OPTION_MaxProblemPerUnit, "10");
	this.runNegativeTest(
		new String[] {
			"X.java",
			buffer.toString()
		},
		"""
			----------
			1. ERROR in X.java (at line 1003)
				void method_1() { method_0()\s
				                           ^
			Syntax error, insert "}" to complete MethodBody
			----------
			2. ERROR in X.java (at line 1003)
				void method_1() { method_0()\s
				                           ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			3. ERROR in X.java (at line 1004)
				void method_2() { method_1()\s
				                           ^
			Syntax error, insert "}" to complete MethodBody
			----------
			4. ERROR in X.java (at line 1004)
				void method_2() { method_1()\s
				                           ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			5. ERROR in X.java (at line 1005)
				void method_3() { method_2()\s
				                           ^
			Syntax error, insert "}" to complete MethodBody
			----------
			6. ERROR in X.java (at line 1005)
				void method_3() { method_2()\s
				                           ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			7. ERROR in X.java (at line 1006)
				void method_4() { method_3()\s
				                           ^
			Syntax error, insert "}" to complete MethodBody
			----------
			8. ERROR in X.java (at line 1006)
				void method_4() { method_3()\s
				                           ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			9. ERROR in X.java (at line 1007)
				void method_5() { method_4()\s
				                           ^
			Syntax error, insert "}" to complete MethodBody
			----------
			10. ERROR in X.java (at line 2002)
				}
				^
			Syntax error, insert "}" to complete ClassBody
			----------
			""",
		null, // custom classpath
		true, // flush previous output dir content
		options // custom options
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				interface X {
				    int f= 1;;
				}"""
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				int f= 1;;
				         ^
			Unnecessary semicolon
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test023() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				class X {
				    int f= 1;;
				}"""
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				int f= 1;;
				         ^
			Unnecessary semicolon
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156119
 */
public void test024() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				interface X {
				    int f= 1;\\u003B
				}"""
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				int f= 1;\\u003B
				         ^^^^^^
			Unnecessary semicolon
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=160337
 */
public void test025() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
				        static class Y {
				                public void foo(int i) {}
				        }
				        static Y FakeInvocationSite = new Y(){
				                public void foo(int i) {}
				        };
				}"""
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(int i) {}
				                       ^^
			Empty block should be documented
			----------
			2. ERROR in X.java (at line 6)
				public void foo(int i) {}
				                       ^^
			Empty block should be documented
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=160337
 */
public void test026() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
				        static class Y {
				                public void foo(int i) {}
				        }
				        static Y FakeInvocationSite = new Y(){
				                public void foo(int i) {
									class A {
										A() {}
										public void bar() {}
									}
									new A().bar();
								 }
				        };
				}"""
		},
		null, options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(int i) {}
				                       ^^
			Empty block should be documented
			----------
			2. ERROR in X.java (at line 9)
				public void bar() {}
				                  ^^
			Empty block should be documented
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173992
 */
public void test027() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.EOFException;
				import java.io.FileNotFoundException;
				import java.io.IOException;
				import org.xml.sax.SAXException;
				public class X {
				public void doSomething() throws FileNotFoundException, EOFException, SAXException{
				
				}
				public void doSomethingElse() {
				try {
					doSomething();
				}
				 catch ( SAXException exception) {
				
				} \s
				catch ( FileNotFoundException exception ) {
				
				}   \s
				catch (
					// working before the slashes
				) {
				
				}\s
				}\s
				}
				"""
        	},
		"""
			----------
			1. ERROR in X.java (at line 19)
				catch (
				      ^
			Syntax error on token "(", FormalParameter expected after this token
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=239198
 */
public void _test028() {
	String error = (this.complianceLevel == ClassFileConstants.JDK14) ?
			"""
				----------
				1. ERROR in X.java (at line 4)
					Srtring bar = \"""
				    }
					              ^^^^
				Text block is not properly closed with the delimiter
				----------
				""" :
			"""
				----------
				1. ERROR in X.java (at line 4)
					Srtring bar = \"""
					              ^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				2. ERROR in X.java (at line 4)
					Srtring bar = \"""
					                ^
				String literal is not properly closed by a double-quote
				----------
				""";
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				    public static void foo(String param) {
				    	String foo= param;
				    	Srtring bar = \"""
				    }
				}"""
		},
		error,
		null,
		true,
		options);
}
public void testBug485477() {
	runNegativeTest(
		new String[] {
			"T.java",
			"public class T {{\n" +
			"  Object o = T.super; // error: '.' expected\n" + // instance initializer
			"  System.out.println(o.toString());\n" +
			"}}\n" +
			"class U {\n" +
			"  Object o1;\n" +
			"  Object o2 = T.super;\n" + // field initializer
			"  U() {\n" +
			"    o1 = U.super;\n" +  // constructor
			"    System.out.println(o1.toString());\n" +
			"  }\n" +
			"}"
		},
		"""
			----------
			1. ERROR in T.java (at line 2)
				Object o = T.super; // error: '.' expected
				             ^^^^^
			Syntax error, insert ". Identifier" to complete Expression
			----------
			2. ERROR in T.java (at line 7)
				Object o2 = T.super;
				              ^^^^^
			Syntax error, insert ". Identifier" to complete Expression
			----------
			3. ERROR in T.java (at line 9)
				o1 = U.super;
				       ^^^^^
			Syntax error, insert ". Identifier" to complete Expression
			----------
			""");
}
}
