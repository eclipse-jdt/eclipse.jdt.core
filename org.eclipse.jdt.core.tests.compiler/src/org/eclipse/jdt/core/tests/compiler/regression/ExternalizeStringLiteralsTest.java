/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExternalizeStringLiteralsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 16 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiteralsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"A.java",
			"""
				public class A {
					void foo() {
						System.out.println("a");
					} //$NON-NLS-1$\t
				}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in A.java (at line 3)
				System.out.println("a");
				                   ^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in A.java (at line 4)
				} //$NON-NLS-1$\t
				  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class X {
					String s = null; //$NON-NLS-1$
					String s2 = ""; //$NON-NLS-1$
					String s3 = ""; //$NON-NLS-1$//$NON-NLS-2$
				\t
					void foo() {
						String s4 = null; //$NON-NLS-1$
						String s5 = ""; //$NON-NLS-1$
						String s6 = ""; //$NON-NLS-2$//$NON-NLS-1$
						System.out.println("foo");//$NON-NLS-1$//$NON-NLS-2$
					} //$NON-NLS-1$
					//$NON-NLS-1$
				}//$NON-NLS-3$""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				String s = null; //$NON-NLS-1$
				                 ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 4)
				String s3 = ""; //$NON-NLS-1$//$NON-NLS-2$
				                             ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 7)
				String s4 = null; //$NON-NLS-1$
				                  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			4. ERROR in X.java (at line 9)
				String s6 = ""; //$NON-NLS-2$//$NON-NLS-1$
				                ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			5. ERROR in X.java (at line 10)
				System.out.println("foo");//$NON-NLS-1$//$NON-NLS-2$
				                                       ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			6. ERROR in X.java (at line 11)
				} //$NON-NLS-1$
				  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			7. ERROR in X.java (at line 13)
				}//$NON-NLS-3$
				 ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"p/Foo.java",
			"""
				package p;
				public class Foo {\s
				    public void foo() {
						System.out.println("string1" + "string2" //$NON-NLS-1$
						);
				}""",
		},
		"""
			----------
			1. ERROR in p\\Foo.java (at line 4)
				System.out.println("string1" + "string2" //$NON-NLS-1$
				                               ^^^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in p\\Foo.java (at line 6)
				}
				^
			Syntax error, insert "}" to complete ClassBody
			----------
			""",
		null,
		true,
		customOptions);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"p/Foo.java",
			"""
				package p;
				public class Foo {\s
				    public void foo() {
						//$NON-NLS-1$
					 };
				}""",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {\r
					public static void main(String[] args) {\r
						String s = ""; //$NON-NLS-1$//$NON-NLS-1$\r
				    }\r
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = ""; //$NON-NLS-1$//$NON-NLS-1$
				                            ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\r\n" +
			"	public static void main(String[] args) {\r\n" +
			"		String s = \"\"; //$NON-NLS-1$//$NON-NLS-1$\r\n" +
			"    }\r\n" +
			"",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = ""; //$NON-NLS-1$//$NON-NLS-1$
				                            ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 4)
				}
				^
			Syntax error, insert "}" to complete ClassBody
			----------
			""",
		null,
		true,
		customOptions);
}
public void test007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {\r
					public static void main(String[] args) {\r
						String s = null; //$NON-NLS-1$//$NON-NLS-1$\r
				    }\r
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = null; //$NON-NLS-1$//$NON-NLS-1$
				                 ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 3)
				String s = null; //$NON-NLS-1$//$NON-NLS-1$
				                              ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {\r
					public static void main(String[] args) {\r
						String s = "test"; //$NON-NLS-2$//$NON-NLS-3$\r
				    }\r
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				           ^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in X.java (at line 3)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				                   ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 3)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				                                ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"p/Foo.java",
			"""
				package p;
				public class Foo {\s
				    public void foo(int i) {
						System.out.println("test1" + i + "test2"); //$NON-NLS-2$//$NON-NLS-1$\r
					 };
				}""",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
						int i = s;
						System.out.println(s);
				    }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				           ^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in X.java (at line 3)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				                   ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 3)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				                                ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			4. ERROR in X.java (at line 4)
				int i = s;
				        ^
			Type mismatch: cannot convert from String to int
			----------
			""",
		null,
		true,
		customOptions);
}
public void test011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int i = null;
						String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
						System.out.println(s + i);
				    }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				int i = null;
				        ^^^^
			Type mismatch: cannot convert from null to int
			----------
			2. ERROR in X.java (at line 4)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				           ^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			3. ERROR in X.java (at line 4)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				                   ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			4. ERROR in X.java (at line 4)
				String s = "test"; //$NON-NLS-2$//$NON-NLS-3$
				                                ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		null,
		true,
		customOptions);
}
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int i = null;
						String s = null; //$NON-NLS-2$//$NON-NLS-3$
						System.out.println(s + i);
				    }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				int i = null;
				        ^^^^
			Type mismatch: cannot convert from null to int
			----------
			2. ERROR in X.java (at line 4)
				String s = null; //$NON-NLS-2$//$NON-NLS-3$
				                 ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 4)
				String s = null; //$NON-NLS-2$//$NON-NLS-3$
				                              ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		null,
		true,
		customOptions);
}
public void test013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = "test1";
						System.out.println(s);
				    }
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = "test1";
				           ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=112973
public void test014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = "test1"; //$NON-NLS-?$
						System.out.println(s);
				    }
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s = "test1"; //$NON-NLS-?$
				           ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			2. ERROR in X.java (at line 3)
				String s = "test1"; //$NON-NLS-?$
				                    ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114077
public void test015() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public void foo() {
						String s1= null; //$NON-NLS-1$
						String s2= "";
					}
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s1= null; //$NON-NLS-1$
				                 ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 4)
				String s2= "";
				           ^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114077
public void test016() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					private String s1= null; //$NON-NLS-1$
				\t
					public void foo() {
						String s2= "";
					}
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				private String s1= null; //$NON-NLS-1$
				                         ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 5)
				String s2= "";
				           ^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148352
public void test017() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo(String locationInAST) {
						String enclosingType= ""; //$NON-NLS-1$
						if (locationInAST != null) {
							enclosingType.toString()
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				enclosingType.toString()
				                       ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					#
					String s1= "1"; //$NON-NLS-1$
					public void foo() {
						String s2= "2"; //$NON-NLS-1$
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				#
				^
			Syntax error on token "Invalid Character", delete this token
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test019() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String s1= "1"; //$NON-NLS-1$
					#
					public void foo() {
						String s2= "2"; //$NON-NLS-1$
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				#
				^
			Syntax error on token "Invalid Character", delete this token
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test020() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String s1= "1"; //$NON-NLS-1$
					public void foo() {
						#
						String s2= "2"; //$NON-NLS-1$
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				#
				^
			Syntax error on token "Invalid Character", delete this token
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test021() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String s1= "1"; //$NON-NLS-1$
					public void foo() {
						String s2= "2"; //$NON-NLS-1$
						#
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				#
				^
			Syntax error on token "Invalid Character", delete this token
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test022() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					#
					String s1= "1"; //$NON-NLS-1$
					public void foo() {
						#
						String s2= "2"; //$NON-NLS-1$
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				#
				^
			Syntax error on token "Invalid Character", delete this token
			----------
			2. ERROR in X.java (at line 5)
				#
				^
			Syntax error on token "Invalid Character", delete this token
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public String toString() {
				                StringBuffer output = new StringBuffer(10);
				                output.append(this != null) ? null : "<no type>"); //$NON-NLS-1$
				                output.append(" "); //$NON-NLS-1$
				                return output.toString();
				        }      \s
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				output.append(this != null) ? null : "<no type>"); //$NON-NLS-1$
				                          ^
			Syntax error on token ")", delete this token
			----------
			""",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443456, [1.8][compiler][lambda] $NON-NLS$ in lambda statement used as argument does not work
public void test443456() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.concurrent.Callable;
				public class X {
				    Callable<String> c;
				    void setC(Callable<String> c) {
				        this.c = c;
				    }
				    X() {
				        setC(() -> "ee"); //$NON-NLS-1$
				    }
				}
				""",
		},
		"",
		null,
		true,
		customOptions);
}
public static Class testClass() {
	return ExternalizeStringLiteralsTest.class;
}
}
