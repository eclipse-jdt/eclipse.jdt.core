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

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTestMixed extends JavadocTest {

	String localDocCommentSupport = CompilerOptions.ENABLED;
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
		// 	Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] {
//			"Bug73995"
//		};
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 3, 7, 10, 21 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildSuite(javadocTestClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.localDocCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, reportInvalidJavadoc);
		if (reportMissingJavadocComments != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportInvalidJavadoc);
		if (reportMissingJavadocTags != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.localDocCommentSupport = this.docCommentSupport;
		reportInvalidJavadoc = CompilerOptions.ERROR;
		reportMissingJavadocTags = CompilerOptions.ERROR;
		reportMissingJavadocComments = null;
	}
	
	/*
	 * Test missing javadoc
	 */
	public void test001() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "public class X {\n"
					+ "  /** */\n"
					+ "  public int x;\n"
					+ "  /** */\n"
					+ "	 public X() {}\n"
					+ "  /** */\n"
					+ "	 public void foo() {}\n"
					+ "}\n" });
	}
	
	public void test002() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "class X {\n"
					+ "  /** */\n"
					+ "  int x;\n"
					+ "  /** */\n"
					+ "	 X() {}\n"
					+ "  /** */\n"
					+ "  void foo() {}\n"
					+ "}\n" });
	}
	
	public void test003() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "class X {\n"
					+ "  /** */\n"
					+ "  protected int x;\n"
					+ "  /** */\n"
					+ "  protected X() {}\n"
					+ "  /** */\n"
					+ "  protected void foo() {}\n"
					+ "}\n" });
	}
	
	public void test004() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** */\n"
					+ "class X {\n"
					+ "  /** */\n"
					+ "  private int x;\n"
					+ "  /** */\n"
					+ "  private X() {}\n"
					+ "  /** */\n"
					+ "  private void foo() {}\n"
					+ "}\n" });
	}
	
	public void test005() {
		reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	public X() {}\n"
					+ "\n"
					+ "	public void foo() {}\n"
					+ "}\n" });
	}
	
	public void test006() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	String s1 = \"non-terminated;\n" + 
				"	void foo() {}\n" + 
				"	String s2 = \"terminated\";\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	String s1 = \"non-terminated;\n" + 
				"	            ^^^^^^^^^^^^^^^^\n" + 
				"String literal is not properly closed by a double-quote\n" + 
				"----------\n"
		);
	}
	
	public void test010() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "public class X {\n"
					+ "	/** Field javadoc comment */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	/** Constructor javadoc comment */\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 2)\n"
				+ "	public class X {\n"
				+ "	             ^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n");
	}
	
	public void test011() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** Class javadoc comment */\n"
					+ "public class X {\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	/** Constructor javadoc comment */\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 4)\n"
				+ "	public int x;\n"
				+ "	           ^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n");
	}
	
	public void test012() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** Class javadoc comment */\n"
					+ "public class X {\n"
					+ "	/** Field javadoc comment */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	public X() {\n"
				+ "	       ^^^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n");
	}
	
	public void test013() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/** Class javadoc comment */\n"
					+ "public class X {\n"
					+ "	/** Field javadoc comment */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "	/** Constructor javadoc comment */\n"
					+ "	public X() {\n"
					+ "	}\n"
					+ "	public void foo(int a) {\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 10)\n"
				+ "	public void foo(int a) {\n"
				+ "	            ^^^^^^^^^^\n"
				+ "Javadoc: Missing comment for public declaration\n"
				+ "----------\n");
	}
	
	/*
	 * Test mixing javadoc comments
	 */
	public void test021() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" });
	}
	
	public void test022() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Unexpected tag in class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " * @param x\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 6)\n"
				+ "	* @param x\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n");
	}
	
	public void test023() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Unexpected tag in field javadoc\n"
					+ " * @throws InvalidException\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 10)\n"
				+ "	* @throws InvalidException\n"
				+ "	   ^^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n");
	}
	
	public void test024() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Wrong tags order in constructor javadoc\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @param str Valid param tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 19)\n"
				+ "	* @param str Valid param tag\n"
				+ "	   ^^^^^\n"
				+ "Javadoc: Unexpected tag\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) {\n"
				+ "	                ^^^\n"
				+ "Javadoc: Missing tag for parameter str\n"
				+ "----------\n");
	}
	
	public void test025() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Wrong param tag in method javadoc\n"
					+ " * @param vector Invalid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 26)\n"
				+ "	* @param vector Invalid param tag\n"
				+ "	         ^^^^^^\n"
				+ "Javadoc: Parameter vector is not declared\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 33)\n"
				+ "	public String foo(java.util.Vector list) {\n"
				+ "	                                   ^^^^\n"
				+ "Javadoc: Missing tag for parameter list\n"
				+ "----------\n");
	}
	
	public void test026() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Invalid see tag in class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Invalid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>unexpected text\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Missing throws tag in constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) throws java.io.IOException {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Missing return tag in method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 5)\n"
				+ "	* @see \"Test class X\n"
				+ "	      ^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see <a href=\"http://www.ibm.com\">Valid URL</a>unexpected text\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Invalid reference\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) throws java.io.IOException {\n"
				+ "	                            ^^^^^^^^^^^^^^^^^^^\n"
				+ "Javadoc: Missing tag for declared exception IOException\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 32)\n"
				+ "	public String foo(java.util.Vector list) {\n"
				+ "	       ^^^^^^\n"
				+ "Javadoc: Missing tag for return type\n"
				+ "----------\n");
	}
	
	/*
	 * Javadoc on invalid syntax
	 */
	public void test030() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc on invalid declaration\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "protected class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 7)\n"
				+ "	protected class X {\n"
				+ "	                ^\n"
				+ "Illegal modifier for the class X; only public, abstract & final are permitted\n"
				+ "----------\n");
	}
	
	public void test031() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc on invalid declaration\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 12)\n"
				+ "	public int x\n"
				+ "	           ^\n"
				+ "Syntax error, insert \";\" to complete ClassBodyDeclarations\n"
				+ "----------\n");
	}
	
	public void test032() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc on invalid declaration\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) \n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector list) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) \n"
				+ "	                   ^\n"
				+ "Syntax error on token \")\", { expected after this token\n"
				+ "----------\n");
	}
	
	public void test033() {
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "/**\n"
					+ " * Valid class javadoc\n"
					+ " * @author ffr\n"
					+ " * @see \"Test class X\"\n"
					+ " */\n"
					+ "public class X {\n"
					+ "/**\n"
					+ " * Valid field javadoc\n"
					+ " * @see <a href=\"http://www.ibm.com\">Valid URL</a>\n"
					+ " */\n"
					+ "	public int x;\n"
					+ "\n"
					+ "/**\n"
					+ " * Valid constructor javadoc\n"
					+ " * @param str Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public X(String str) {\n"
					+ "	}\n"
					+ "/**\n"
					+ " * Valid method javadoc on invalid declaration\n"
					+ " * @param list Valid param tag\n"
					+ " * @throws NullPointerException Valid throws tag\n"
					+ " * @exception IllegalArgumentException Valid throws tag\n"
					+ " * @return Valid return tag\n"
					+ " * @see X Valid see tag\n"
					+ " * @deprecated\n"
					+ " */\n"
					+ "	public String foo(java.util.Vector ) {\n"
					+ "		return \"\";\n"
					+ "	}\n"
					+ "}\n" },
			"----------\n"
				+ "1. ERROR in test\\X.java (at line 23)\n"
				+ "	}\n"
				+ "	^\n"
				+ "Syntax error, insert \"}\" to complete ClassBody\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 26)\n"
				+ "	* @param list Valid param tag\n"
				+ "	         ^^^^\n"
				+ "Javadoc: Parameter list is not declared\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 33)\n"
				+ "	public String foo(java.util.Vector ) {\n"
				+ "	                            ^^^^^^\n"
				+ "Syntax error on token \"Vector\", VariableDeclaratorId expected after this token\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 36)\n"
				+ "	}\n"
				+ "	^\n"
				+ "Syntax error on token \"}\", delete this token\n"
				+ "----------\n");
	}
	
	public void test040() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	/**\n" + 
					"	/**\n" + 
					"	/** \n" + 
					"	 * @param str\n" + 
					"	 * @param x\n" + 
					"	 */\n" + 
					"	public void bar(String str, int x) {\n" + 
					"	}\n" + 
					"	public void foo() {\n" + 
					"		bar(\"toto\", 0 /* block comment inline */);\n" + 
					"	}\n" + 
					"}\n" });
	}
	
	public void test041() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * @see String\n" + 
					"	 * @see #\n" + 
					"	 * @return String\n" + 
					"	 */\n" + 
					"	String bar() {return \"\";}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @see #\n" + 
				"	       ^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n"
			);
	}

	/**
	 * Test fix for bug 45596.
	 * When this bug happened, compiler wrongly complained on missing parameter javadoc
	 * entries for method declaration in anonymous class.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45596">45596</a>
	 */
	public void testBug45596() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
			 	+ "class X {\n"
					+ "	void foo(int x, String str) {}\n"
			  		+ "}\n",
				"test/Y.java",
				"package test;\n"
			   		+ "class Y {\n"
			   		+ "  /** */\n"
			   		+ "  protected X field = new X() {\n"
			   		+ "    void foo(int x, String str) {}\n"
			   		+ "  };\n"
			   		+ "}\n"});
	}

	/**
	 * Additional test for bug 45596.
	 * Verify correct complain about missing parameter javadoc entries in anonymous class.
	 * Since bug 47132, @param, @return and @throws tags are not resolved in javadoc of anonymous
	 * class...
	 */
	public void testBug45596a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(int x, String str) {}\n" + 
				"}\n",
				"Y1.java",
				"public class Y1 {\n" + 
				"	/** */\n" +
				"	protected X field = new X() {\n" + 
				"		/** Invalid javadoc comment in anonymous class */\n" + 
				"		void foo(String str) {}\n" + 
				"	};\n" + 
				"}\n",
				"Y2.java",
				"public class Y2 {\n" + 
				"	/** */\n" + 
				"	void foo() {\n" + 
				"		X x = new X() {\n" + 
				"			/** Invalid javadoc comment in anonymous class */\n" + 
				"			void foo(String str) {}\n" + 
				"		};\n" + 
				"		x.foo(0, \"\");\n" + 
				"	}\n" + 
				"}\n",
				"Y3.java",
				"public class Y3 {\n" + 
				"	static X x;\n" + 
				"	static {\n" + 
				"		x = new X() {\n" + 
				"			/** Invalid javadoc comment in anonymous class */\n" + 
				"			void foo(String str) {}\n" + 
				"		};\n" + 
				"	}\n" + 
				"}\n" } /*,
				"----------\n" + 
				"1. ERROR in Y1.java (at line 5)\n" + 
				"	void foo(String str) {}\n" + 
				"	                ^^^\n" + 
				"Javadoc: Missing tag for parameter str\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. ERROR in Y2.java (at line 6)\n" + 
				"	void foo(String str) {}\n" + 
				"	                ^^^\n" + 
				"Javadoc: Missing tag for parameter str\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. ERROR in Y3.java (at line 6)\n" + 
				"	void foo(String str) {}\n" + 
				"	                ^^^\n" + 
				"Javadoc: Missing tag for parameter str\n" + 
				"----------\n"
				*/
			);
	}

	/**
	 * Additional test for bug 45596.
	 * Verify no complain about missing parameter javadoc entries.
	 */
	public void testBug45596b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
		"X.java",
		"public class X {\n" + 
		"	void foo(int x, String str) {}\n" + 
		"}\n",
		"Y1.java",
		"public class Y1 {\n" + 
		"	/** */\n" + 
		"	protected X field = new X() {\n" + 
		"		/**\n" + 
		"		 * Valid javadoc comment in anonymous class.\n" + 
		"		 * @param str String\n" + 
		"		 * @return int\n" + 
		"		 */\n" + 
		"		int bar(String str) {\n" + 
		"			return 10;\n" + 
		"		}\n" + 
		"	};\n" + 
		"}\n",
		"Y2.java",
		"public class Y2 {\n" + 
		"	/** */\n" + 
		"	void foo() {\n" + 
		"		X x = new X() {\n" + 
		"			/**\n" + 
		"			 * Valid javadoc comment in anonymous class.\n" + 
		"			 * @param str String\n" + 
		"			 * @return int\n" + 
		"			 */\n" + 
		"			int bar(String str) {\n" + 
		"				return 10;\n" + 
		"			}\n" + 
		"		};\n" + 
		"		x.foo(0, \"\");\n" + 
		"	}\n" + 
		"}\n",
		"Y3.java",
		"public class Y3 {\n" + 
		"	static X x;\n" + 
		"	static {\n" + 
		"		x = new X() {\n" + 
		"			/**\n" + 
		"			 * Valid javadoc comment in anonymous class.\n" + 
		"			 * @param str String\n" + 
		"			 * @return int\n" + 
		"			 */\n" + 
		"			int bar(String str) {\n" + 
		"				return 10;\n" + 
		"			}\n" + 
		"		};\n" + 
		"	}\n" + 
		"}\n"}
			);
	}

	/**
	 * Test fix for bug 45592.
	 * When this bug happened, a NullPointerException occured during the compilation.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45592">45592</a>
	 */
	public void testBug45592() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
		"a/Y.java",
		"package a;\n" + 
		"\n" + 
		"/** */\n" + 
		"public class Y {\n" + 
		"	protected boolean bar(Object obj) {\n" + 
		"		return obj == null;\n" + 
		"	}\n" + 
		"}\n",
		"test/X.java",
		"package test;\n" + 
		"public class X {\n" + 
		"	public static Boolean valueOf(boolean bool) {\n" + 
		"		if (bool) {\n" + 
		"			return Boolean.TRUE;\n" + 
		"		} else {\n" + 
		"			return Boolean.FALSE;\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
		"test/YY.java",
		"package test;\n" + 
		"\n" + 
		"import a.Y;\n" + 
		"\n" + 
		"/** */\n" + 
		"public class YY extends Y {\n" + 
		"	/**\n" + 
		"	 * Returns a Boolean.\n" + 
		"	 * @param key\n" + 
		"	 * @return A Boolean telling whether the key is null or not.\n" + 
		"	 * @see #bar(Object)\n" + 
		"	 */\n" + 
		"	protected Boolean foo(Object key) {\n" + 
		"		return X.valueOf(bar(key));\n" + 
		"	}\n" + 
		"}\n"
		}
			);
	}

	/**
	 * Test fix for bug 45737.
	 * When this bug happened, compiler complains on return type and argument of method bar.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45737">45737</a>
	 */
	public void testBug45737() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Y.java",
				"class Y {\n" + 
				"	void foo() {\n" + 
				"		X x = new X() {\n" + 
				"			/**\n" + 
				"			 * Valid javadoc comment in anonymous class.\n" + 
				"			 * @param str String\n" + 
				"			 * @return int\n" + 
				"			 */\n" + 
				"			int bar(String str) {\n" + 
				"				return 10;\n" + 
				"			}\n" + 
				"		};\n" + 
				"		x.foo();\n" + 
				"	}\n" + 
				"}\n",
				"X.java",
				"class X {\n" + 
				"	void foo() {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 45669.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45669">45669</a>
	 */
	public void testBug45669() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * Valid javadoc comment with tags mixed order\n" + 
				"	 * @param str first param\n" + 
				"	 * 		@see String\n" + 
				"	 * @param dbl second param\n" + 
				"	 * 		@see Double\n" + 
				"	 * 		also\n" + 
				"	 * 		@see \"String ref\"\n" + 
				"	 * @return int\n" + 
				"	 * @throws InterruptedException\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	int foo(String str, Double dbl) throws InterruptedException {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	/*
	 * Additional test for bug 45669.
	 * Verify that compiler complains when @throws tag is between @param tags.
	 */
	public void testBug45669a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	/**\n" + 
				"	 * Javadoc comment with tags invalid mixed order\n" + 
				"	 * @param str first param\n" + 
				"	 * 		@see String\n" + 
				"	 * @throws InterruptedException\n" + 
				"	 * @param dbl second param\n" + 
				"	 * 		@see Double\n" + 
				"	 * 		also\n" + 
				"	 * 		@see \"String ref\"\n" + 
				"	 * @return int\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	public int foo(String str, Double dbl) throws InterruptedException {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	* @param dbl second param\n" + 
		"	   ^^^^^\n" + 
		"Javadoc: Unexpected tag\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 14)\n" + 
		"	public int foo(String str, Double dbl) throws InterruptedException {\n" + 
		"	                                  ^^^\n" + 
		"Javadoc: Missing tag for parameter dbl\n" + 
		"----------\n"
		);
	}

	/**
	 * Test fix for bug 45958.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45958">45958</a>
	 */
	public void testBug45958() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int x;\n" + 
				"	public X(int i) {\n" + 
				"		x = i;\n" + 
				"	}\n" + 
				"	/**\n" + 
				"	 * @see #X(int)\n" + 
				"	 */\n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	public void testBug45958a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
			   "X.java",
		   		"public class X {\n" + 
		   		"	int x;\n" + 
		   		"	public X(int i) {\n" + 
		   		"		x = i;\n" + 
		   		"	}\n" + 
		   		"	/**\n" + 
		   		"	 * @see #X(String)\n" + 
		   		"	 */\n" + 
		   		"	public void foo() {\n" + 
		   		"	}\n" + 
		   		"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	* @see #X(String)\n" + 
				"	        ^\n" + 
				"Javadoc: The constructor X(String) is undefined\n" + 
				"----------\n"
		);
	}
	public void testBug45958b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
			   "X.java",
		   		"public class X {\n" + 
		   		"	int x;\n" + 
		   		"	public X(int i) {\n" + 
		   		"		x = i;\n" + 
		   		"	}\n" + 
		   		"	/**\n" + 
		   		"	 * @see #X(int)\n" + 
		   		"	 */\n" + 
		   		"	public void foo() {\n" + 
		   		"	}\n" + 
		   		"}\n",
		   		"XX.java",
		   		"public class XX extends X {\n" + 
		   		"	/**\n" + 
		   		"	 * @param i\n" + 
		   		"	 * @see #X(int)\n" + 
		   		"	 */\n" + 
		   		"	public XX(int i) {\n" + 
		   		"		super(i);\n" + 
		   		"		x++;\n" + 
		   		"	}\n" + 
		   		"}\n"
			},
			"----------\n" + 
				"1. ERROR in XX.java (at line 4)\n" + 
				"	* @see #X(int)\n" + 
				"	        ^\n" + 
				"Javadoc: The method X(int) is undefined for the type XX\n" + 
				"----------\n"
			);
	}
	public void testBug45958c() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int x;\n" + 
				"	public X(int i) {\n" + 
				"		x = i;\n" + 
				"	}\n" + 
				"	/**\n" + 
				"	 * @see #X(String)\n" + 
				"	 */\n" + 
				"	void foo() {\n" + 
				"	}\n" + 
				"	void X(String str) {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 46901.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=46901">46901</a>
	 */
	public void testBug46901() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"A.java",
				"public abstract class A {\n" + 
				"	public A() { super(); }\n" + 
				"}\n",
				"X.java",
				"/**\n" + 
				" * @see A#A()\n" + 
				" */\n" + 
				"public class X extends A {\n" + 
				"	public X() { super(); }\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 47215.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47215">47215</a>
	 */
	public void testBug47215() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"	/**\n" + 
				"	 * @see X\n" + 
				"	 * @see X#X(int)\n" + 
				"	 * @see X(double)\n" + 
				"	 * @see X   (double)\n" + 
				"	 * @see X[double]\n" + 
				"	 * @see X!=}}\n" + 
				"	 * @see foo()\n" + 
				"	 * @see foo  ()\n" + 
				"	 */\n" + 
				"	public class X {\n" + 
				"		public X(int i){}\n" + 
				"		public void foo() {}\n" + 
				"	}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @see X(double)\n" + 
				"	       ^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @see X[double]\n" + 
				"	       ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @see X!=}}\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see foo()\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	* @see foo  ()\n" + 
				"	       ^^^\n" + 
				"Javadoc: foo cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 47341.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47341">47341</a>
	 */
	public void testBug47341() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" + 
				"public class X {\n" + 
				"	void foo_package() {}\n" + 
				"	protected void foo_protected() {}\n" + 
				"}\n",
				"p1/Y.java",
				"package p1;\n" + 
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see #foo_package()\n" + 
				"	 */\n" + 
				"	protected void bar() {\n" + 
				"		foo_package();\n" + 
				"	}\n" + 
				"}\n",
				"p2/Y.java",
				"package p2;\n" + 
				"import p1.X;\n" + 
				"\n" + 
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see X#foo_protected()\n" + 
				"	 */\n" + 
				"	protected void bar() {\n" + 
				"		foo_protected();\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 47132.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47132">47132</a>
	 */
	public void testBug47132() {
		runConformTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X {\n" + 
				"  /** */\n" + 
				"  public void foo(){\n" + 
				"    new Object(){\n" + 
				"		public int x;\n" + 
				"       public void bar(){}\n" + 
				"    };\n" + 
				"  }\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 47339.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47339">47339</a>
	 */
	public void testBug47339() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X implements Comparable {\n" + 
				"	/**\n" + 
				"	 * @see java.lang.Comparable#compareTo(java.lang.Object)\n" + 
				"	 */\n" + 
				"	public int compareTo(Object o) {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	/** @see Object#toString() */\n" + 
				"	public String toString(){\n" + 
				"		return \"\";\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	public void testBug47339a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X extends RuntimeException {\n" + 
				"	\n" + 
				"	/**\n" + 
				"	 * @see RuntimeException#RuntimeException(java.lang.String)\n" + 
				"	 */\n" + 
				"	public X(String message) {\n" + 
				"		super(message);\n" + 
				"	}\n" + 
				"}\n"
			}
		);
	}
	public void testBug47339b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X implements Comparable {\n" + 
				"	/** */\n" + 
				"	public int compareTo(Object o) {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	/** */\n" + 
				"	public String toString(){\n" + 
				"		return \"\";\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	public int compareTo(Object o) {\n" + 
				"	       ^^^\n" + 
				"Javadoc: Missing tag for return type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public int compareTo(Object o) {\n" + 
				"	                            ^\n" + 
				"Javadoc: Missing tag for parameter o\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	public String toString(){\n" + 
				"	       ^^^^^^\n" + 
				"Javadoc: Missing tag for return type\n" + 
				"----------\n"
		);
	}
	public void testBug47339c() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X extends RuntimeException {\n" + 
				"	\n" + 
				"	/** */\n" + 
				"	public X(String message) {\n" + 
				"		super(message);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	public X(String message) {\n" + 
				"	                ^^^^^^^\n" + 
				"Javadoc: Missing tag for parameter message\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 48064.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48064">48064</a>
	 */
	public void testBug48064() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public X(String str) {}\n" + 
				"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see X#X(STRING)\n" + 
				"	 */\n" + 
				"	public Y(String str) {super(str);}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 3)\n" + 
			"	* @see X#X(STRING)\n" + 
			"	           ^^^^^^\n" + 
			"Javadoc: STRING cannot be resolved or is not a type\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 5)\n" + 
			"	public Y(String str) {super(str);}\n" + 
			"	                ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n"
		);
	}
	public void testBug48064a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void foo(String str) {}\n" + 
				"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
				"	/**\n" + 
				"	 * @see X#foo(STRING)\n" + 
				"	 */\n" + 
				"	public void foo(String str) {super.foo(str);}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 3)\n" + 
			"	* @see X#foo(STRING)\n" + 
			"	             ^^^^^^\n" + 
			"Javadoc: STRING cannot be resolved or is not a type\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 5)\n" + 
			"	public void foo(String str) {super.foo(str);}\n" + 
			"	                       ^^^\n" + 
			"Javadoc: Missing tag for parameter str\n" + 
			"----------\n"
		);
	}

	/**
	 * Test fix for bug 48523.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48523">48523</a>
	 */
	public void testBug48523() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
					"public class X {\n" + 
					"	public void foo() throws IOException {}\n" + 
					"}\n",
				"Y.java",
				"import java.io.IOException;\n" + 
					"public class Y extends X {\n" + 
					"	/**\n" + 
					"	 * @throws IOException\n" + 
					"	 * @see X#foo()\n" + 
					"	 */\n" + 
					"	public void foo() throws IOException {}\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 48711.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48711">48711</a>
	 */
	public void testBug48711() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"import java.io.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @throws IOException\n" + 
				"	 * @throws EOFException\n" + 
				"	 * @throws FileNotFoundException\n" + 
				"	 */\n" + 
				"	public void foo() throws IOException {}\n" + 
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 45782.
	 * When this bug happened, compiler wrongly complained on missing parameters declaration
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45782">45782</a>
	 */
	public void testBug45782() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X implements Comparable {\n" + 
					"\n" + 
					"	/**\n" + 
					"	 * Overridden method with return value and parameters.\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public boolean equals(Object obj) {\n" + 
					"		return super.equals(obj);\n" + 
					"	}\n" + 
					"\n" + 
					"	/**\n" + 
					"	 * Overridden method with return value and thrown exception.\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public Object clone() throws CloneNotSupportedException {\n" + 
					"		return super.clone();\n" + 
					"	}\n" + 
					"\n" + 
					"	/**\n" + 
					"	 * Implemented method (Comparable)  with return value and parameters.\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public int compareTo(Object o) { return 0; }\n" + 
					"}\n"
			});
	}
	public void testBug45782a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unefficient inheritDoc tag on a method which is neither overridden nor implemented...\n" + 
					"	 * {@inheritDoc}\n" + 
					"	 */\n" + 
					"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
				"	       ^^^\n" + 
				"Javadoc: Missing tag for return type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
				"	                      ^^^\n" + 
				"Javadoc: Missing tag for parameter str\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" + 
				"	                                  ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing tag for declared exception IllegalArgumentException\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 49260.
	 * When this bug happened, compiler wrongly complained on Invalid parameters declaration
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49260">49260</a>
	 */
	public void testBug49260() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
					"public final class X {\n" + 
					"	int bar(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" + 
					"	/**\n" + 
					"	 * Valid method reference on several lines\n" + 
					"	 * @see #bar(String str,\n" + 
					"	 * 		int var,\n" + 
					"	 * 		Vector list,\n" + 
					"	 * 		char[] array)\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n" });
	}

	/**
	 * Test fix for bug 48385.
	 * When this bug happened, compiler does not complain on CharOperation references in @link tags
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48385">48385</a>
	 */
	public void testBug48385() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * Method outside javaDoc Comment\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 * @param str\n" + 
					"	 * @param var tag description not empty\n" + 
					"	 * @param list third param with embedded tag: {@link Vector}\n" + 
					"	 * @param array fourth param with several embedded tags on several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@linkplain CharOperation Label not empty} tag description not empty\n" + 
					"	 * @throws IllegalAccessException\n" + 
					"	 * @throws NullPointerException tag description not empty\n" + 
					"	 * @return an integer\n" + 
					"	 * @see String\n" + 
					"	 * @see Vector tag description not empty\n" + 
					"	 * @see Object tag description includes embedded tags and several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 */\n" + 
					"	int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" + 
					"}\n"},
			"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 12)\n" + 
				"	*  2) {@linkplain CharOperation Label not empty} tag description not empty\n" + 
				"	                  ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 20)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	public void testBug48385And49620() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * Method outside javaDoc Comment\n" + 
					"	 *  1) {@link\n" + 
					"	 * 				String} tag description not empty\n" + 
					"	 *  2) {@link\n" + 
					"	 * 				CharOperation Label not empty} tag description not empty\n" + 
					"	 * @param\n" + 
					"	 * 				str\n" + 
					"	 * @param\n" + 
					"	 * 				var tag description not empty\n" + 
					"	 * @param list third param with embedded tag: {@link\n" + 
					"	 * 				Vector} but also on several lines: {@link\n" + 
					"	 * 				CharOperation}\n" + 
					"	 * @param array fourth param with several embedded tags on several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 * @throws\n" + 
					"	 * 					IllegalAccessException\n" + 
					"	 * @throws\n" + 
					"	 * 					NullPointerException tag description not empty\n" + 
					"	 * @return int\n" + 
					"	 * 					an integer\n" + 
					"	 * @see\n" + 
					"	 * 			String\n" + 
					"	 * @see\n" + 
					"	 * 		Vector\n" + 
					"	 * 		tag description not empty\n" + 
					"	 * @see Object tag description includes embedded tags and several lines:\n" + 
					"	 *  1) {@link String} tag description not empty\n" + 
					"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" + 
					"	 */\n" + 
					"	int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" + 
					"}\n"},
			"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	* 				CharOperation Label not empty} tag description not empty\n" + 
				"	  				^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 15)\n" + 
				"	* 				CharOperation}\n" + 
				"	  				^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 18)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 32)\n" + 
				"	*  2) {@link CharOperation Label not empty} tag description not empty\n" + 
				"	             ^^^^^^^^^^^^^\n" + 
				"Javadoc: CharOperation cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}
	public void testBug48385a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Method outside javaDoc Comment\n" + 
					"	 *  1) {@link } Missing reference\n" + 
					"	 *  2) {@link Unknown} Cannot be resolved\n" + 
					"	 *  3) {@link *} Missing reference\n" + 
					"	 *  4) {@link #} Invalid reference\n" + 
					"	 *  5) {@link String } } Valid reference\n" + 
					"	 *  6) {@link String {} Invalid tag\n" + 
					"	 * @return int\n" + 
					"	 */\n" + 
					"	int foo() {return 0;}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  1) {@link } Missing reference\n" + 
				"	        ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	*  2) {@link Unknown} Cannot be resolved\n" + 
				"	             ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	*  3) {@link *} Missing reference\n" + 
				"	        ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	*  4) {@link #} Invalid reference\n" + 
				"	             ^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	*  6) {@link String {} Invalid tag\n" + 
				"	      ^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 49491.
	 * When this bug happened, compiler complained on duplicated throws tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49491">49491</a>
	 */
	public void testBug49491() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public final class X {\n" + 
					"	/**\n" + 
					"	 * Now valid duplicated throws tag\n" + 
					"	 * @throws IllegalArgumentException First comment\n" + 
					"	 * @throws IllegalArgumentException Second comment\n" + 
					"	 * @throws IllegalArgumentException Last comment\n" + 
					"	 */\n" + 
					"	void foo() throws IllegalArgumentException {}\n" + 
					"}\n" });
	}
	public void testBug49491a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public final class X {\n" + 
					"	/**\n" + 
					"	 * Duplicated param tags should be still flagged\n" + 
					"	 * @param str First comment\n" + 
					"	 * @param str Second comment\n" + 
					"	 * @param str Last comment\n" + 
					"	 */\n" + 
					"	void foo(String str) {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @param str Second comment\n" + 
				"	         ^^^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @param str Last comment\n" + 
				"	         ^^^\n" + 
				"Javadoc: Duplicate tag for parameter\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 48376.
	 * When this bug happened, compiler complained on duplicated throws tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48376">48376</a>
	 */
	public void testBug48376() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* 			</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			</a>\n" + 
					"	* @see Object\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"
		 });
	}
	public void testBug48376a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*          IBM Home Page<\n" + 
					"	* 			/a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			\n" + 
					"	* @see Unknown\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">\n" + 
				"	*          IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">\n" + 
				"	*          IBM Home Page<\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\">\n" + 
				"	*\n" + 
				"	*          IBM\n" + 
				"	*\n" + 
				"	*          Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 16)\n" + 
				"	* @see Unknown\n" + 
				"	       ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 50644.
	 * When this bug happened, compiler complained on duplicated throws tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50644">50644</a>
	 */
	public void testBug50644() {
		reportInvalidJavadoc = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * Should not be @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"p2/Y.java",
				"package p2;\n" + 
					"import p1.X;\n" + 
					"public class Y {\n" + 
					"	public void foo() {\n" + 
					"		X x = new X();\n" + 
					"		x.foo();\n" + 
					"	}\n" + 
					"}\n"
		 });
	}

	/**
	 * Test fix for bug 50695.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50695">50695</a>
	 */
	public void testBug50695() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * @see java\n" + 
					"	 * @see java.util\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
		 });
	}
	public void testBug50695b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * @see java.unknown\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			 },
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	* @see java.unknown\n" + 
				"	       ^^^^^^^^^^^^\n" + 
				"Javadoc: java.unknown cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 51626.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51626">51626</a>
	 */
	public void testBug51626() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" + 
					"public class X {\n" + 
					"	/**\n" + 
					"	 * @see String\n" + 
					"	 * toto @deprecated\n" + 
					"	 */\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"p2/Y.java",
				"package p2;\n" + 
					"import p1.*;\n" + 
					"public class Y {\n" + 
					"	void foo() {\n" + 
					"		X x = new X(); \n" + 
					"		x.foo();\n" + 
					"	}\n" + 
					"}\n"
		 });
	}

	/**
	 * Test fix for bug 52216.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=52216">52216</a>
	 */
	public void testBug52216() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * Valid ref with white spaces at the end\n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>		   \n" + 
					"*/\n" + 
					"public class X {\n" + 
					"}\n"
		 });
	}
	public void testBug52216a() {
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"* @see \"Valid ref with white spaces at the end\"	   \n" + 
					"*/\n" + 
					"public class X {\n" + 
					"}\n"
		 });
	}
	public void testBug52216b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>		   \n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>\n" + 
					"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>			,\n" + 
					"* @see \"Valid ref with white spaces at the end\"\n" + 
					"* @see \"Valid ref with white spaces at the end\"	   \n" + 
					"* @see \"Invalid ref\"	   .\n" + 
					"*/\n" + 
					"public class X {\n" + 
					"}\n"
			 },
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>			,\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	* @see \"Invalid ref\"	   .\n" + 
				"	       ^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 51529.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51529">51529</a>
	 */
	public void testBug51529() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Vector\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n"
		 });
	}
	public void testBug51529a() {
		reportInvalidJavadoc = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Vector\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n"
			}
		);
	}
	public void testBug51529b() {
		this.localDocCommentSupport = CompilerOptions.DISABLED;
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Vector\n" + 
				"	 */\n" + 
				"	void foo() {}\n" + 
				"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 1)\n" + 
				"	import java.util.Vector;\n" + 
				"	       ^^^^^^^^^^^^^^^^\n" + 
				"The import java.util.Vector is never used\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 51911.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51911">51911</a>
	 */
	public void testBug51911() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		// Warn an ambiguous method reference
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public void foo(int i, float f) {}\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* @see #foo\n" + 
				"	        ^^^\n" + 
				"Javadoc: foo is an ambiguous method reference or is not a field\n" + 
				"----------\n"
		);
	}
	public void testBug51911a() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		// Accept unambiguous method reference
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	}
		);
	}
	public void testBug51911b() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		// Accept field reference with method name
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public int foo;\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	}
		);
	}
	public void testBug51911c() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		// Accept field reference with ambiguous method name
		runConformTest(
			new String[] {
				"X.java",
					"/**\n" +
					" * @see #foo\n" +
					" */\n" +
					"public class X {\n" +
					"	public int foo;\n" +
					"	public void foo() {}\n" +
					"	public void foo(String str) {}\n" +
					"}\n"
		 	}
		);
	}

	/**
	 * Test fix for bug 53279: [Javadoc] Compiler should complain when inline tag is not terminated
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53279">53279</a>
	 */
	public void testBug53279() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link Object\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link Object\n" + 
				"	   ^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n"
		);
	}
	public void testBug53279a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link Object\n" + 
					"	 * @return int\n" + 
					"	 */\n" + 
					"	int foo() {return 0;}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link Object\n" + 
				"	   ^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n"
		);
	}
	public void testBug53279b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link        \n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link        \n" + 
				"	   ^^^^^^^^^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	*  {@link        \n" + 
				"	     ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n"
		);
	}
	public void testBug53279c() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Unterminated inline tags\n" + 
					"	 *  {@link\n" + 
					"	 * @return int\n" + 
					"	 */\n" + 
					"	int foo() {return 0;}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@link\n" + 
				"	   ^^^^^^\n" + 
				"Javadoc: Missing closing brace for inline tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	*  {@link\n" + 
				"	     ^^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 53290: [Javadoc] Compiler should complain when tag name is not correct
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53290">53290</a>
	 */
	public void testBug53290() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * See as inline tag\n" + 
					"	 *  {@see Object}\n" + 
					"	 *  @see Object\n" + 
					"	 *  @link Object\n" + 
					"	 *  {@link Object}\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	*  {@see Object}\n" + 
				"	     ^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	*  @link Object\n" + 
				"	    ^^^^\n" + 
				"Javadoc: Unexpected tag\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 62812: Some malformed javadoc tags are not reported as malformed
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=62812">62812</a>
	 */
	public void testBug62812() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * @see Object#clone())\n" + 
					" * @see Object#equals(Object)}\n" + 
					" * @see Object#equals(Object))\n" + 
					" * @see Object#equals(Object)xx\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 2)\n" + 
				"	* @see Object#clone())\n" + 
				"	                   ^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in Test.java (at line 3)\n" + 
				"	* @see Object#equals(Object)}\n" + 
				"	                    ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in Test.java (at line 4)\n" + 
				"	* @see Object#equals(Object))\n" + 
				"	                    ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"4. ERROR in Test.java (at line 5)\n" + 
				"	* @see Object#equals(Object)xx\n" + 
				"	                    ^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}
	public void testBug62812a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * {@link Object#clone())}\n" + 
					" * {@link Object#equals(Object)}\n" + 
					" * {@link Object#equals(Object))}\n" + 
					" * {@link Object#equals(Object)xx}\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 2)\n" + 
				"	* {@link Object#clone())}\n" + 
				"	                     ^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in Test.java (at line 4)\n" + 
				"	* {@link Object#equals(Object))}\n" + 
				"	                      ^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in Test.java (at line 5)\n" + 
				"	* {@link Object#equals(Object)xx}\n" + 
				"	                      ^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 51606: [Javadoc] Compiler should complain when tag name is not correct
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51606">51606</a>
	 */
	public void testBug51606() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"  *  @param a {@inheritDoc}\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Y.java (at line 5)\n" + 
				"	public void foo(int a, int b) {\n" + 
				"	                           ^\n" + 
				"Javadoc: Missing tag for parameter b\n" + 
				"----------\n"
		);
	}
	public void testBug51606a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"   * {@inheritDoc}\n" + 
					"  *  @param a aaaaa\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			},
			""
		);
	}
	public void testBug51606b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"   * Text before inherit tag\n" + 
					"   * {@inheritDoc}\n" + 
					"  *  @param a aaaaa\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			}
		);
	}
	public void testBug51606c() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"  /**\n" + 
					"   * @param a aaa\n" + 
					"   * @param b bbb\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n",
				"Y.java",
				"public class Y extends X {\n" + 
					"  /**\n" + 
					"   * Text before inherit tag {@inheritDoc}\n" + 
					"  *  @param a aaaaa\n" + 
					"   */\n" + 
					"  public void foo(int a, int b) {\n" + 
					"  }\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 65174: Spurious "Javadoc: Missing reference" error
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65174">65174</a>
	 */
	public void testBug65174() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link\n" + 
					" * Object valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with error: {@link\n" + 
					"	 * Object valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link    		\n" + 
					" * Object valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with error: {@link   		\n" + 
					"	 * Object valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link java.lang.\n" + 
					" * Object valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with error: {@link java.lang.\n" + 
					"	 * Object valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174c() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment with no error: {@link Object\n" + 
					" * valid} because it\'s not on first line\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/** Comment previously with no error: {@link Object\n" + 
					"	 * valid} because tag is on comment very first line\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug65174d() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	/** Comment previously with no error: {@link Object valid} comment on one line */\n" + 
					"	void foo1() {}\n" + 
					"	/** Comment previously with no error: {@link Object valid}       */\n" + 
					"	void foo2() {}\n" + 
					"	/** Comment previously with no error: {@link Object valid}*/\n" + 
					"	void foo3() {}\n" + 
					"	/**                    {@link Object valid} comment on one line */\n" + 
					"	void foo4() {}\n" + 
					"	/**{@link Object valid} comment on one line */\n" + 
					"	void foo5() {}\n" + 
					"	/**       {@link Object valid} 				*/\n" + 
					"	void foo6() {}\n" + 
					"	/**{@link Object valid} 				*/\n" + 
					"	void foo7() {}\n" + 
					"	/**				{@link Object valid}*/\n" + 
					"	void foo8() {}\n" + 
					"	/**{@link Object valid}*/\n" + 
					"	void foo9() {}\n" + 
					"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 65180: Spurious "Javadoc: xxx cannot be resolved or is not a field" error with inner classes
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65180">65180</a>
	 */
	public void testBug65180() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testFunc\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"}\n" + 
					"\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 6)\r\n" + 
				"	* @see #testFunc\r\n" + 
				"	        ^^^^^^^^\n" + 
				"Javadoc: testFunc cannot be resolved or is not a field\n" + 
				"----------\n"
		);
	}
	public void testBug65180a() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testFunc()\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"}\n" + 
					"\n"
			}
		);
	}
	public void testBug65180b() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see Test#testFunc\n" + 
					"		 * @see Test#testFunc()\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"}\n" + 
					"\n"
			}
		);
	}
	public void testBug65180c() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testFunc\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc() {}\n" + 
					"	public void testFunc(String str) {}\n" + 
					"}\n" + 
					"\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 6)\n" + 
				"	* @see #testFunc\n" + 
				"	        ^^^^^^^^\n" + 
				"Javadoc: testFunc cannot be resolved or is not a field\n" + 
				"----------\n"
		);
	}
	public void testBug65180d() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	int testField;\n" + 
					"	public class Inner {\n" + 
					"		/**\n" + 
					"		 * Does something.\n" + 
					"		 * \n" + 
					"		 * @see #testField\n" + 
					"		 * @see #testFunc(int)\n" + 
					"		 */\n" + 
					"		public void innerFunc() {\n" + 
					"			testFunc(testField);\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void testFunc(int test) {\n" + 
					"		testField = test; \n" + 
					"	}\n" + 
					"}\n" + 
					"\n"
			}
		);
	}
	public void testBug65180e() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"ITest.java",
				"public interface ITest {\n" + 
					"	/**\n" + 
					"	 * @see #foo() \n" + 
					"	 */\n" + 
					"	public static int field = 0;\n" + 
					"	/**\n" + 
					"	 * @see #field\n" + 
					"	 */\n" + 
					"	public void foo();\n" + 
					"}\n"
			}
		);
	}
	public void testBug65180f() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    static class SuperInner {\n" + 
					"    	public int field;\n" + 
					"        public void foo() {}\n" + 
					"     }\n" + 
					"    \n" + 
					"	public static class Inner extends SuperInner {\n" + 
					"		/**\n" + 
					"		 * @see #field\n" + 
					"		 */\n" + 
					"		public static int f;\n" + 
					"		/**\n" + 
					"		 * @see #foo()\n" + 
					"		 */\n" + 
					"		public static void bar() {}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public void foo() {}\n" + 
					"}"
			}
		);
	}

	/**
	 * Test fix for bug 65253: [Javadoc] @@tag is wrongly parsed as @tag
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65253">65253</a>
	 */
	public void testBug65253() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/**\n" + 
					" * Comment \n" + 
					" * @@@@see Unknown Should not complain on ref\n" + 
					" */\n" + 
					"public class Test {\n" + 
					"	/**\n" + 
					"	 * Comment\n" + 
					"	 * @@@param xxx Should not complain on param\n" + 
					"	 * @@return int\n" + 
					"	 */\n" + 
					"	int foo() { // should warn on missing tag for return type\n" + 
					"		return 0;\n" + 
					"	}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 11)\n" + 
				"	int foo() { // should warn on missing tag for return type\n" + 
				"	^^^\n" + 
				"Javadoc: Missing tag for return type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 66551: Error in org.eclipse.swt project on class PrinterData
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66551">66551</a>
	 */
	public void testBug66551() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    int field;\n" + 
					"    /**\n" + 
					"     *  @see #field\n" + 
					"     */\n" + 
					"    void foo(int field) {\n" + 
					"    }\n" + 
					"\n" + 
					"}\n"
			}
		);
	}
	public void testBug66551a() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    static int field;\n" + 
					"    /**\n" + 
					"     *  @see #field\n" + 
					"     */\n" + 
					"    static void foo(int field) {\n" + 
					"    }\n" + 
					"\n" + 
					"}\n"
			}
		);
	}
	public void testBug66551b() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	int field;\n" + 
					"	/**\n" + 
					"	 * {@link #field}\n" + 
					"	 */\n" + 
					"	void foo(int field) {\n" + 
					"	}\n" + 
					"\n" + 
					"}\n"
			}
		);
	}
	public void testBug66551c() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"	static int field;\n" + 
					"	/**\n" + 
					"	 * {@link #field}\n" + 
					"	 */\n" + 
					"	static void foo(int field) {\n" + 
					"	}\n" + 
					"\n" + 
					"}\n"
			}
		);
	}	

	/**
	 * Test fix for bug 66573: Shouldn't bind to local constructs
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66573">66573</a>
	 */
	public void testBug66573() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
					"    /**\n" + 
					"     * @see Local\n" + 
					"     */\n" + 
					"    void foo() {\n" + 
					"        class Local { \n" + 
					"            // shouldn\'t be seen from javadoc\n" + 
					"         }\n" + 
					"    }\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in Test.java (at line 3)\n" + 
				"	* @see Local\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Local cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 68017: Javadoc processing does not detect missing argument to @return
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68017">68017</a>
	 */
	public void testBug68017conform() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@return valid integer*/\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return #\n" + 
					"	 */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			}
		);
	}
	public void testBug68017negative() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@return*/\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**@return        */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"	/**@return****/\n" + 
					"	public int foo3() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return\n" + 
					"	 */\n" + 
					"	public int foo4() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@return*/\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@return        */\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	/**@return****/\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 9)\n" + 
				"	*	@return\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n"
		);
	}
	// Javadoc issue a warning on following tests
	public void testBug68017javadocWarning1() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return* */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**@return** **/\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	*	@return* */\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	/**@return** **/\n" + 
				"	    ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n"
		);
	}
	public void testBug68017javadocWarning2() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return#\n" + 
					"	 */\n" + 
					"	public int foo() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	*	@return#\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 68025: Javadoc processing does not detect some wrong links
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68025">68025</a>
	 */
	public void testBug68025conform() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Y.java",
				"public class Y {\n" + 
					"	public int field;\n" + 
					"	public void foo() {}\n" + 
					"}\n",
				"Z.java",
				"public class Z {\n" + 
					"	/**\n" + 
					"	 *	@see Y#field #valid\n" + 
					"	 *	@see Y#foo #valid\n" + 
					"	 */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Y#field     # valid*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Y#foo		# valid*/\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Y#foo()\n" + 
					"	 *# valid*/\n" + 
					"	public void foo4() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug68025negative() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	public int field;\n" + 
					"	public void foo() {}\n" + 
					"	/**\n" + 
					"	 *	@see #field#invalid\n" + 
					"	 *	@see #foo#invalid\n" + 
					"	 */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Y#field# invalid*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Y#foo#	invalid*/\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Y#foo()#\n" + 
					"	 *valid*/\n" + 
					"	public void foo4() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	*	@see #field#invalid\n" + 
				"	 	     ^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	*	@see #foo#invalid\n" + 
				"	 	     ^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 9)\n" + 
				"	/**@see Y#field# invalid*/\n" + 
				"	         ^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 11)\n" + 
				"	/**@see Y#foo#	invalid*/\n" + 
				"	         ^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 13)\n" + 
				"	/**@see Y#foo()#\n" + 
				"	             ^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 69272: [Javadoc] Invalid malformed reference (missing separator)
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69272">69272</a>
	 */
	public void testBug69272classValid() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object*/\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object\n" + 
					"	*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object    */\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Object****/\n" + 
					"	public void foo4() {}\n" + 
					"	/**@see Object		****/\n" + 
					"	public void foo5() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug69272classInvalid() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object* */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object*** ***/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object***\n" + 
					"	 */\n" + 
					"	public void foo3() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see Object* */\n" + 
				"	        ^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see Object*** ***/\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	/**@see Object***\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}
	public void testBug69272fieldValid() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	int field;\n" + 
					"	/**@see #field*/\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see #field\n" + 
					"	*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see #field    */\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see #field****/\n" + 
					"	public void foo4() {}\n" + 
					"	/**@see #field		********/\n" + 
					"	public void foo5() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug69272fieldInvalid() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	int field;\n" + 
					"	/**@see #field* */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see #field*** ***/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see #field***\n" + 
					"	 */\n" + 
					"	public void foo3() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	/**@see #field* */\n" + 
				"	        ^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	/**@see #field*** ***/\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	/**@see #field***\n" + 
				"	        ^^^^^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}
	public void testBug69272methodValid() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object#wait()*/\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object#wait()\n" + 
					"	*/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object#wait()    */\n" + 
					"	public void foo3() {}\n" + 
					"	/**@see Object#wait()****/\n" + 
					"	public void foo4() {}\n" + 
					"	/**@see Object#wait()		****/\n" + 
					"	public void foo5() {}\n" + 
					"}\n"
			}
		);
	}
	public void testBug69272methodInvalid() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Object#wait()* */\n" + 
					"	public void foo1() {}\n" + 
					"	/**@see Object#wait()*** ***/\n" + 
					"	public void foo2() {}\n" + 
					"	/**@see Object#wait()***\n" + 
					"	 */\n" + 
					"	public void foo3() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see Object#wait()* */\n" + 
				"	                   ^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see Object#wait()*** ***/\n" + 
				"	                   ^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	/**@see Object#wait()***\n" + 
				"	                   ^^^^^\n" + 
				"Javadoc: Malformed reference (missing end space separator)\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 69275: [Javadoc] Invalid warning on @see link
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69275">69275</a>
	 */
	public void testBug69275conform() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>*/\n" + 
					"	void foo1() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>\n" + 
					"	*/\n" + 
					"	void foo2() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>		*/\n" + 
					"	void foo3() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>**/\n" + 
					"	void foo4() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>     *****/\n" + 
					"	void foo5() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug69275negative() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>* */\n" + 
					"	void foo1() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>	** **/\n" + 
					"	void foo2() {}\n" + 
					"	/**@see <a href=\"http://www.eclipse.org\">text</a>**\n" + 
					"	*/\n" + 
					"	void foo3() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>* */\n" + 
				"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>	** **/\n" + 
				"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 69302: [Javadoc] Invalid reference warning inconsistent with javadoc tool
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69302">69302</a>
	 */
	public void testBug69302conform1() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see Object <a href=\"http://www.eclipse.org\">Eclipse</a>\n" + 
					"	 */\n" + 
					"	void foo1() {}\n" + 
					"	/**\n" + 
					"	 *	@see Object \"Valid string reference\"\n" + 
					"	 */\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug69302conform2() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see Unknown <a href=\"http://www.eclipse.org\">Eclipse</a>\n" + 
					"	 */\n" + 
					"	void foo1() {}\n" + 
					"	/**\n" + 
					"	 *	@see Unknown \"Valid string reference\"\n" + 
					"	 */\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug69302negative() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**@see Unknown blabla <a href=\"http://www.eclipse.org\">text</a>*/\n" + 
					"	void foo1() {}\n" + 
					"	/**@see Unknown blabla \"Valid string reference\"*/\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	/**@see Unknown blabla <a href=\"http://www.eclipse.org\">text</a>*/\n" + 
				"	        ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved or is not a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	/**@see Unknown blabla \"Valid string reference\"*/\n" + 
				"	        ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 68726: [Javadoc] Target attribute in @see link triggers warning
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68726">68726</a>
	 */
	public void testBug68726conform1() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@see Object <a href=\"http://www.eclipse.org\" target=\"_top\">Eclipse</a>\n" + 
					"	 */\n" + 
					"	void foo1() {}\n" + 
					"	/**@see Object <a href=\"http://www.eclipse.org\" target=\"_top\" target1=\"_top1\" target2=\"_top2\">Eclipse</a>*/\n" + 
					"	void foo2() {}\n" + 
					"}\n"	
			}
		);
	}
	public void testBug68726conform2() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* 			</a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			</a>\n" + 
					"	* @see Object\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"	
			}
		);
	}
	public void testBug68726negative1() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 * Invalid URL link references\n" + 
					"	 *\n" + 
					"	 * @see <a href=\"invalid\" target\n" + 
					"	 * @see <a href=\"invalid\" target=\n" + 
					"	 * @see <a href=\"invalid\" target=\"\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\"\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid<\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid</\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid</a\n" + 
					"	 * @see <a href=\"invalid\" target=\"_top\">invalid</a> no text allowed after the href\n" + 
					"	 */\n" + 
					"	void foo() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	* @see <a href=\"invalid\" target\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	* @see <a href=\"invalid\" target=\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	* @see <a href=\"invalid\" target=\"\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\"\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 11)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 12)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 13)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid<\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 14)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid</\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"11. ERROR in X.java (at line 15)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid</a\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"12. ERROR in X.java (at line 16)\n" + 
				"	* @see <a href=\"invalid\" target=\"_top\">invalid</a> no text allowed after the href\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n"
		);
	}
	public void testBug68726negative2() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*          IBM Home Page<\n" + 
					"	* 			/a>\n" + 
					"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
					"	*\n" + 
					"	*          IBM\n" + 
					"	*\n" + 
					"	*          Home Page\n" + 
					"	*\n" + 
					"	*\n" + 
					"	* 			\n" + 
					"	* @see Unknown\n" + 
					"	*/\n" + 
					"public class X {\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
				"	*          IBM Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
				"	*          IBM Home Page<\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 8)\n" + 
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" + 
				"	*\n" + 
				"	*          IBM\n" + 
				"	*\n" + 
				"	*          Home Page\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid URL link format\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 16)\n" + 
				"	* @see Unknown\n" + 
				"	       ^^^^^^^\n" + 
				"Javadoc: Unknown cannot be resolved or is not a type\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 73348: [Javadoc] Missing description for return tag is not always warned
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73348">73348</a>
	 */
	public void testBug73348conform() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return      \n" + 
					"	 *	int\n" + 
					"	 */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return      \n" + 
					"	 *	int\n" + 
					"	 *	@see Object\n" + 
					"	 */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			}
		);
	}
	public void testBug73348negative() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return\n" + 
					"	 *	@see Object\n" + 
					"	 */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return      \n" + 
					"	 *	@see Object\n" + 
					"	 */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"}\n",
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	*	@return\n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	*	@return      \n" + 
				"	 	 ^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
				"----------\n"
		);
	}

	/**
	 * Test fix for bug 73995: [Javadoc] Wrong warning for missing return type description for @return {@inheritDoc}
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73995">73995</a>
	 */
	public void testBug73995() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"	/**\n" + 
					"	 *	@return {@link Object}     \n" + 
					"	 */\n" + 
					"	public int foo1() {return 0; }\n" + 
					"	/** @return {@inheritedDoc} */\n" + 
					"	public int foo2() {return 0; }\n" + 
					"	/**\n" + 
					"	 *	@return\n" + 
					"	 *		{@unknown_tag}\n" + 
					"	 */\n" + 
					"	public int foo3() {return 0; }\n" + 
					"}\n",
			}
 		);
 	}
}
