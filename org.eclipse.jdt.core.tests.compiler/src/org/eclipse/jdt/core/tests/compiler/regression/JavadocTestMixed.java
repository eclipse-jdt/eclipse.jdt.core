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
import junit.framework.TestSuite;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTestMixed extends JavadocTest {

	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;

	public JavadocTestMixed(String name) {
		super(name);
	}

	public static Class testClass() {
		return JavadocTestMixed.class;
	}

	public static Test suite() {
		if (false) {
			TestSuite ts;
			int[] numbers = { 41 };
			//some of the tests depend on the order of this suite.
			ts = new TestSuite();
			for (int i = 0; i < numbers.length; i++) {
				String meth = "test";
				int num = numbers[i];
				if (num < 10) {
					meth += "0";
				}
				if (num < 100) {
					meth += "0";
				}
				meth += num;
				ts.addTest(new JavadocTestMixed(meth));
			}
			return new RegressionTestSetup(ts, COMPLIANCE_1_4);
		}
		if (false) {
			TestSuite ts = new TestSuite();
			ts.addTest(new JavadocTestMixed("testBug47339b"));
			ts.addTest(new JavadocTestMixed("testBug48064a"));
			return new RegressionTestSetup(ts, COMPLIANCE_1_4);
		}
		return setupSuite(testClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, reportInvalidJavadoc);
		if (reportMissingJavadocComments != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		reportInvalidJavadoc = CompilerOptions.ERROR;
		reportMissingJavadocComments = null;
	}
	
	/*
	 * Test missing javadoc
	 */
	public void test001() {
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
			"	        ^^^^^^^^\n" + 
			"Javadoc: Invalid reference\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	* @see X   (double)\n" + 
			"	           ^^^^^^^^\n" + 
			"Javadoc: Invalid reference\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	* @see foo()\n" + 
			"	          ^^\n" + 
			"Javadoc: Invalid reference\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	* @see foo  ()\n" + 
			"	            ^^\n" + 
			"Javadoc: Invalid reference\n" + 
			"----------\n"
		);
	}

	/**
	 * Test fix for bug 47341.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47341">47341</a>
	 */
	public void testBug47341() {
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runNegativeTest(
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
					"	 * @return\n" + 
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
		this.runNegativeTest(
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
				"	      ^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Invalid tag\n" + 
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
		this.runConformTest(
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
		this.runNegativeTest(
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
		this.runConformTest(
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
		this.runNegativeTest(
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
}
