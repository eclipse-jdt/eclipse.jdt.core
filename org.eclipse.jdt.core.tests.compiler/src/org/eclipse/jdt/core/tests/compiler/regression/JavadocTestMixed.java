/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
	String reportMissingJavadoc = CompilerOptions.ENABLED;

	public JavadocTestMixed(String name) {
		super(name);
	}

	public static Class testClass() {
		return JavadocTestMixed.class;
	}

	public static Test suite() {
		if (false) {
			TestSuite ts;
			//some of the tests depend on the order of this suite.
			ts = new TestSuite();
			for (int i = 2; i <= 2; i++) {
				String meth = "test";
				if (i < 10) {
					meth += "0";
				}
				if (i < 100) {
					meth += "0";
				}
				meth += i;
				ts.addTest(new JavadocTestMixed(meth));
			}
			return new RegressionTestSetup(ts, COMPLIANCE_1_4);
		}
		if (false) {
			TestSuite ts = new TestSuite();
			ts.addTest(new JavadocTestMixed("testBug45958"));
			ts.addTest(new JavadocTestMixed("testBug45958a"));
			ts.addTest(new JavadocTestMixed("testBug45958b"));
			ts.addTest(new JavadocTestMixed("testBug45958c"));
			return new RegressionTestSetup(ts, COMPLIANCE_1_4);
		}
		return setupSuite(testClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportMissingJavadoc, reportMissingJavadoc);
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
		reportMissingJavadoc = CompilerOptions.ENABLED;
	}
	
	/*
	 * Test missing javadoc
	 */
	public void test001() {
		this.runConformTest(
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
					+ "	/** Method javadoc comment */\n"
					+ "	public void foo() {\n"
					+ "	}\n"
					+ "}\n" });
	}
	
	public void test002() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "class X {\n"
					+ "	int x;\n"
					+ "\n"
					+ "	X() {}\n"
					+ "\n"
					+ "	void foo() {}\n"
					+ "}\n" });
	}
	
	public void test003() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "class X {\n"
					+ "	protected int x;\n"
					+ "\n"
					+ "	protected X() {}\n"
					+ "\n"
					+ "	protected void foo() {}\n"
					+ "}\n" });
	}
	
	public void test004() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n"
					+ "class X {\n"
					+ "	private int x;\n"
					+ "\n"
					+ "	private X() {}\n"
					+ "\n"
					+ "	private void foo() {}\n"
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

	/**
	 * Test fix for bug 45596.
	 * When this bug happened, compiler wrongly complained on missing parameter javadoc
	 * entries for method declaration in anonymous class.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45596">45596</a>
	 */
	public void testBug45596() {
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
	 */
	public void testBug45596a() {
		reportMissingJavadoc = CompilerOptions.DISABLED;
		this.runNegativeTest(
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
		"}\n" },
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
			);
	}

	/**
	 * Additional test for bug 45596.
	 * Verify no complain about missing parameter javadoc entries.
	 */
	public void testBug45596b() {
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
				"	int foo(String str, Double dbl) throws InterruptedException {\n" + 
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
		"	int foo(String str, Double dbl) throws InterruptedException {\n" + 
		"	                           ^^^\n" + 
		"Javadoc: Missing tag for parameter dbl\n" + 
		"----------\n"
		);
	}

	/**
	 * Test fix for bug 45958.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45958">45958</a>
	 */
	public void testBug45958() {
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
		   		"	void foo() {\n" + 
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
	reportMissingJavadoc = CompilerOptions.DISABLED;
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
	   		"	void foo() {\n" + 
	   		"	}\n" + 
	   		"}\n",
	   		"XX.java",
	   		"public class XX extends X {\n" + 
	   		"	/**\n" + 
	   		"	 * @param i\n" + 
	   		"	 * @see #X(int)\n" + 
	   		"	 */\n" + 
	   		"	XX(int i) {\n" + 
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
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
		reportMissingJavadoc = CompilerOptions.DISABLED;
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
}
