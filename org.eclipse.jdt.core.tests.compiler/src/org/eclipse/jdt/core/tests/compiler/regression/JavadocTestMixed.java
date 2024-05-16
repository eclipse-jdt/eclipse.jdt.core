/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTestMixed extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;

	public JavadocTestMixed(String name) {
		super(name);
	}

	public static Class javadocTestClass() {
		return JavadocTestMixed.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug77602";
//		TESTS_NAMES = new String[] { "testBug80910" };
//		TESTS_NUMBERS = new int[] { 31, 32, 33 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(javadocTestClass());
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocTags != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocComments = null;
	}

	/*
	 * Test missing javadoc
	 */
	public void test001() {
		runConformTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** */
					public class X {
					  /** */
					  public int x;
					  /** */
						 public X() {}
					  /** */
						 public void foo() {}
					}
					""" });
	}

	public void test002() {
		runConformTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** */
					class X {
					  /** */
					  int x;
					  /** */
						 X() {}
					  /** */
					  void foo() {}
					}
					""" });
	}

	public void test003() {
		runConformTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** */
					class X {
					  /** */
					  protected int x;
					  /** */
					  protected X() {}
					  /** */
					  protected void foo() {}
					}
					""" });
	}

	public void test004() {
		runConformTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** */
					class X {
					  /** */
					  private int x;
					  /** */
					  private X() {}
					  /** */
					  private void foo() {}
					}
					""" });
	}

	public void test005() {
		this.reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						public int x;
					
						public X() {}
					
						public void foo() {}
					}
					""" });
	}

	public void test006() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						String s1 = "non-terminated;
						void foo() {}
						String s2 = "terminated";
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					String s1 = "non-terminated;
					            ^^^^^^^^^^^^^^^^
				String literal is not properly closed by a double-quote
				----------
				"""
		);
	}

	public void test010() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					public class X {
						/** Field javadoc comment */
						public int x;
					
						/** Constructor javadoc comment */
						public X() {
						}
						/** Method javadoc comment */
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 2)
					public class X {
					             ^
				Javadoc: Missing comment for public declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test011() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** Class javadoc comment */
					public class X {
						public int x;
					
						/** Constructor javadoc comment */
						public X() {
						}
						/** Method javadoc comment */
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 4)
					public int x;
					           ^
				Javadoc: Missing comment for public declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test012() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** Class javadoc comment */
					public class X {
						/** Field javadoc comment */
						public int x;
					
						public X() {
						}
						/** Method javadoc comment */
						public void foo() {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					public X() {
					       ^^^
				Javadoc: Missing comment for public declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test013() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/** Class javadoc comment */
					public class X {
						/** Field javadoc comment */
						public int x;
					
						/** Constructor javadoc comment */
						public X() {
						}
						public void foo(int a) {
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 10)
					public void foo(int a) {
					            ^^^^^^^^^^
				Javadoc: Missing comment for public declaration
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Test mixing javadoc comments
	 */
	public void test021() {
		runConformTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" });
	}

	public void test022() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Unexpected tag in class javadoc
					 * @author ffr
					 * @see "Test class X"
					 * @param x
					 */
					public class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					* @param x
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test023() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Unexpected tag in field javadoc
					 * @throws InvalidException
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 10)
					* @throws InvalidException
					   ^^^^^^
				Javadoc: Unexpected tag
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test024() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Wrong tags order in constructor javadoc
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @param str Valid param tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 19)
					* @param str Valid param tag
					   ^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in test\\X.java (at line 22)
					public X(String str) {
					                ^^^
				Javadoc: Missing tag for parameter str
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test025() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Wrong param tag in method javadoc
					 * @param vector Invalid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 26)
					* @param vector Invalid param tag
					         ^^^^^^
				Javadoc: Parameter vector is not declared
				----------
				2. ERROR in test\\X.java (at line 33)
					public String foo(java.util.Vector list) {
					                                   ^^^^
				Javadoc: Missing tag for parameter list
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test026() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Invalid see tag in class javadoc
					 * @author ffr
					 * @see "Test class X
					 */
					public class X {
					/**
					 * Invalid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>unexpected text
					 */
						public int x;
					
					/**
					 * Missing throws tag in constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) throws java.io.IOException {
						}
					/**
					 * Missing return tag in method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 5)
					* @see "Test class X
					       ^^^^^^^^^^^^^
				Javadoc: Invalid reference
				----------
				2. ERROR in test\\X.java (at line 10)
					* @see <a href="http://www.ibm.com">Valid URL</a>unexpected text
					                                              ^^^^^^^^^^^^^^^^^^
				Javadoc: Unexpected text
				----------
				3. ERROR in test\\X.java (at line 22)
					public X(String str) throws java.io.IOException {
					                            ^^^^^^^^^^^^^^^^^^^
				Javadoc: Missing tag for declared exception IOException
				----------
				4. ERROR in test\\X.java (at line 32)
					public String foo(java.util.Vector list) {
					       ^^^^^^
				Javadoc: Missing tag for return type
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/*
	 * Javadoc on invalid syntax
	 */
	public void test030() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc on invalid declaration
					 * @author ffr
					 * @see "Test class X"
					 */
					protected class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 7)
					protected class X {
					                ^
				Illegal modifier for the class X; only public, abstract & final are permitted
				----------
				""");
	}

	public void test031() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Valid field javadoc on invalid declaration
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 12)
					public int x
					           ^
				Syntax error, insert ";" to complete ClassBodyDeclarations
				----------
				""");
	}

	public void test032() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc on invalid declaration
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str)\s
						}
					/**
					 * Valid method javadoc
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector list) {
							return "";
						}
					}
					""" },
			"""
				----------
				1. ERROR in test\\X.java (at line 22)
					public X(String str)\s
					                   ^
				Syntax error on token ")", { expected after this token
				----------
				""");
	}

	public void _test033() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					/**
					 * Valid class javadoc
					 * @author ffr
					 * @see "Test class X"
					 */
					public class X {
					/**
					 * Valid field javadoc
					 * @see <a href="http://www.ibm.com">Valid URL</a>
					 */
						public int x;
					
					/**
					 * Valid constructor javadoc
					 * @param str Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public X(String str) {
						}
					/**
					 * Valid method javadoc on invalid declaration
					 * @param list Valid param tag
					 * @throws NullPointerException Valid throws tag
					 * @exception IllegalArgumentException Valid throws tag
					 * @return Valid return tag
					 * @see X Valid see tag
					 * @deprecated
					 */
						public String foo(java.util.Vector ) {
							return "";
						}
					}
					""" },
					this.complianceLevel < ClassFileConstants.JDK1_5
					? """
						----------
						1. ERROR in test\\X.java (at line 23)
							}
							^
						Syntax error, insert "}" to complete ClassBody
						----------
						2. ERROR in test\\X.java (at line 26)
							* @param list Valid param tag
							         ^^^^
						Javadoc: Parameter list is not declared
						----------
						3. ERROR in test\\X.java (at line 33)
							public String foo(java.util.Vector ) {
							                            ^^^^^^
						Syntax error on token "Vector", VariableDeclaratorId expected after this token
						----------
						4. ERROR in test\\X.java (at line 36)
							}
							^
						Syntax error on token "}", delete this token
						----------
						"""
					: """
						----------
						1. ERROR in test\\X.java (at line 23)
							}
							^
						Syntax error, insert "}" to complete ClassBody
						----------
						2. ERROR in test\\X.java (at line 26)
							* @param list Valid param tag
							         ^^^^
						Javadoc: Parameter list is not declared
						----------
						3. ERROR in test\\X.java (at line 33)
							public String foo(java.util.Vector ) {
							                           ^
						Syntax error on token ".", ... expected
						----------
						4. ERROR in test\\X.java (at line 36)
							}
							^
						Syntax error on token "}", delete this token
						----------
						""");
	}

	public void test040() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						/**
						/**
						/**\s
						 * @param str
						 * @param x
						 */
						public void bar(String str, int x) {
						}
						public void foo() {
							bar("toto", 0 /* block comment inline */);
						}
					}
					""" });
	}

	public void test041() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						/**
						 * @see String
						 * @see #
						 * @return String
						 */
						String bar() {return "";}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					* @see #
					       ^
				Javadoc: Invalid reference
				----------
				""",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
			);
	}
}
