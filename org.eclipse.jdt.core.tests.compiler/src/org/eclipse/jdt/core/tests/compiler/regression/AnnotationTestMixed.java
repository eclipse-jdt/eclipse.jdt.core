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

import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AnnotationTestMixed extends AnnotationTest {

	public AnnotationTestMixed(String name) {
		super(name);
	}

	public static Class testClass() {
		return AnnotationTestMixed.class;
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
				ts.addTest(new AnnotationTestMixed(meth));
			}
			return new RegressionTestSetup(ts, COMPLIANCE_1_4);
		}
		return setupSuite(testClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidAnnotation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingAnnotation, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		return options;
	}
	
	/*
	 * Test missing annotation
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
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportInvalidAnnotation, CompilerOptions.IGNORE);
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
					+ "}\n" },
			null,
			null,
			true,
			null,
			options);
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
				+ "Annotation: Missing javadoc comment for public declaration\n"
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
				+ "Annotation: Missing javadoc comment for public declaration\n"
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
				+ "Annotation: Missing javadoc comment for public declaration\n"
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
				+ "Annotation: Missing javadoc comment for public declaration\n"
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
				+ "Annotation: Unexpected javadoc entry\n"
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
				+ "Annotation: Unexpected javadoc entry\n"
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
				+ "Annotation: Unexpected javadoc entry\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) {\n"
				+ "	                ^^^\n"
				+ "Annotation: Missing javadoc entry for parameter str\n"
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
				+ "Annotation: Parameter vector is not declared\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 33)\n"
				+ "	public String foo(java.util.Vector list) {\n"
				+ "	                                   ^^^^\n"
				+ "Annotation: Missing javadoc entry for parameter list\n"
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
				+ "Annotation: Invalid reference\n"
				+ "----------\n"
				+ "2. ERROR in test\\X.java (at line 10)\n"
				+ "	* @see <a href=\"http://www.ibm.com\">Valid URL</a>unexpected text\n"
				+ "	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Annotation: Invalid reference\n"
				+ "----------\n"
				+ "3. ERROR in test\\X.java (at line 22)\n"
				+ "	public X(String str) throws java.io.IOException {\n"
				+ "	                            ^^^^^^^^^^^^^^^^^^^\n"
				+ "Annotation: Missing javadoc entry for declared exception IOException\n"
				+ "----------\n"
				+ "4. ERROR in test\\X.java (at line 32)\n"
				+ "	public String foo(java.util.Vector list) {\n"
				+ "	       ^^^^^^\n"
				+ "Annotation: Missing javadoc entry for return type\n"
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
				+ "Annotation: Parameter list is not declared\n"
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
	 * Test fix for bug 45198.
	 * When this bug happened a NPE was thrown in factory.createMethod(...)
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45198">45198</a>
	 */
	public void _testBug45198() { // TODO (frederic) should move this test to model side (where plugin is started)
		Hashtable savedOptions = JavaCore.getOptions();
		Map options = getCompilerOptions();
		Hashtable newOptions = new Hashtable(options.size());
		newOptions.putAll(options);
		JavaCore.setOptions(newOptions);
		try {
			IDOMFactory factory= new DOMFactory();
			IDOMMethod m= factory.createMethod(
					"/**" + LINE_SEPARATOR +
					" * @return Returns the content." + LINE_SEPARATOR +
					" */" + LINE_SEPARATOR +
					"public String getContent() {" + LINE_SEPARATOR +
					"	return fContent;" + LINE_SEPARATOR +
					"}" + LINE_SEPARATOR);
			assertTrue("initial contents wrong", equals(m.getContents(), 
							"/**" + LINE_SEPARATOR +
							" * @return Returns the content." + LINE_SEPARATOR +
							" */" + LINE_SEPARATOR +
							"public String getContent() {" + LINE_SEPARATOR +
							"	return fContent;" + LINE_SEPARATOR +
							"}" + LINE_SEPARATOR));
		} finally {
			JavaCore.setOptions(savedOptions);
		}
	}

	/**
	 * Test fix for bug 45596.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45596">45596</a>
	 */
	public void testBug45596() {
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
}
