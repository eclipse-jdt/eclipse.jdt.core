/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

public class FormatterBugsTests extends FormatterRegressionTests {

public static Test suite() {
	return buildModelTestSuite(FormatterBugsTests.class);
}

public FormatterBugsTests(String name) {
	super(name);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUp()
 */
private void setUpBracesPreferences(String braces) {
	if (braces != null) {
	 	assertTrue("Invalid value for braces preferences: "+braces,
			braces.equals(DefaultCodeFormatterConstants.END_OF_LINE) ||
	 		braces.equals(DefaultCodeFormatterConstants.NEXT_LINE) ||
	 		braces.equals(DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP) ||
	 		braces.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED));
		this.formatterPrefs.brace_position_for_annotation_type_declaration = braces;
		this.formatterPrefs.brace_position_for_anonymous_type_declaration = braces;
		this.formatterPrefs.brace_position_for_array_initializer = braces;
		this.formatterPrefs.brace_position_for_block = braces;
		this.formatterPrefs.brace_position_for_block_in_case = braces;
		this.formatterPrefs.brace_position_for_constructor_declaration = braces;
		this.formatterPrefs.brace_position_for_enum_constant = braces;
		this.formatterPrefs.brace_position_for_enum_declaration = braces;
		this.formatterPrefs.brace_position_for_method_declaration = braces;
		this.formatterPrefs.brace_position_for_switch = braces;
		this.formatterPrefs.brace_position_for_type_declaration = braces;
	}
}

/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterBugs", "1.5"); //$NON-NLS-1$
	}
	super.setUpSuite();
}

/**
 * @bug 198074: [formatter] the code formatter doesn't respect my new lines
 * @test Ensure that the formatter keep line breaks wrapping set by users in the code
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=198074"
 */
public void testBug198074() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"String x = \"select x \"\n" +
		"         + \"from y \"\n" +
		"         + \"where z=a\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		String x = \"select x \"\n" +
		"				+ \"from y \"\n" +
		"				+ \"where z=a\";\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug198074b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"String x = \"select x \"\n" +
		"         + \"from y \"\n" +
		"         + \"where z=a\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    void foo() {\n" +
		"        String x = \"select x \"\n" +
		"                + \"from y \"\n" +
		"                + \"where z=a\";\n" +
		"    }\n" +
		"}\n"
	);
}
// another test case put in bug's comment 1
public void testBug198074_c1() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	String foo(boolean enabled) {\n" +
		"if (enabled)\n" +
		"{\n" +
		"   // we need x\n" +
		"   // we need a select\n" +
		"   return \"select x \"\n" +
		"   + \"from X\";}\n" +
		"	return null;}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	String foo(boolean enabled) {\n" +
		"		if (enabled) {\n" +
		"			// we need x\n" +
		"			// we need a select\n" +
		"			return \"select x \"\n" +
		"					+ \"from X\";\n" +
		"		}\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug198074_c1b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"	String foo(boolean enabled) {\n" +
		"if (enabled)\n" +
		"{\n" +
		"   // we need x\n" +
		"   // we need a select\n" +
		"   return \"select x \"\n" +
		"        + \"from X\";}\n" +
		"	return null;}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    String foo(boolean enabled) {\n" +
		"        if (enabled) {\n" +
		"            // we need x\n" +
		"            // we need a select\n" +
		"            return \"select x \"\n" +
		"                    + \"from X\";\n" +
		"        }\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n"
	);
}
// another test case put in bug's comment 3
public void testBug198074_c3() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"public String toString() {\n" +
		"        return \"YAD01: \"\n" +
		"        + \" nommbr=\'\"+getName()+\"\'\"\n" +
		"        + \" nomgrp=\'\"+getService().getArgtbl()+\"\'\"\n" +
		"        + \" typmbr=\'\"+getMemberType().getArgument()+\"\'\"\n" +
		"        + \" srcpat=\'\"+getPhysicalPath()+\"\'\"\n" +
		"        + \" nommdl=\'\"+getModel()+\"\'\"\n" +
		"        ;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	public String toString() {\n" +
		"		return \"YAD01: \"\n" +
		"				+ \" nommbr=\'\" + getName() + \"\'\"\n" +
		"				+ \" nomgrp=\'\" + getService().getArgtbl() + \"\'\"\n" +
		"				+ \" typmbr=\'\" + getMemberType().getArgument() + \"\'\"\n" +
		"				+ \" srcpat=\'\" + getPhysicalPath() + \"\'\"\n" +
		"				+ \" nommdl=\'\" + getModel() + \"\'\";\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug198074_c3b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"public String toString() {\n" +
		"        return \"YAD01: \"\n" +
		"                + \" nommbr=\'\"+getName()+\"\'\"\n" +
		"                + \" nomgrp=\'\"+getService().getArgtbl()+\"\'\"\n" +
		"                + \" typmbr=\'\"+getMemberType().getArgument()+\"\'\"\n" +
		"                + \" srcpat=\'\"+getPhysicalPath()+\"\'\"\n" +
		"                + \" nommdl=\'\"+getModel()+\"\'\"\n" +
		"        ;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    public String toString() {\n" +
		"        return \"YAD01: \"\n" +
		"                + \" nommbr=\'\" + getName() + \"\'\"\n" +
		"                + \" nomgrp=\'\" + getService().getArgtbl() + \"\'\"\n" +
		"                + \" typmbr=\'\" + getMemberType().getArgument() + \"\'\"\n" +
		"                + \" srcpat=\'\" + getPhysicalPath() + \"\'\"\n" +
		"                + \" nommdl=\'\" + getModel() + \"\'\";\n" +
		"    }\n" +
		"}\n"
	);
}
public void testBug198074_comments() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"String x = \"select x \"\n" +
		"         + \"from y \"\n" +
		"         + \"where z=a\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		String x = \"select x \" + \"from y \" + \"where z=a\";\n" +
		"	}\n" +
		"}\n"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=201022
// see also bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=287462
public void testBug198074_dup201022() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"    String sQuery =\n" +
		"        \"select * \" +\n" +
		"        \"from person p, address a \" +\n" +
		"        \"where p.person_id = a.person_id \" +\n" +
		"        \"and p.person_id = ?\";\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		String sQuery =\n" +
		"				\"select * \" +\n" +
		"						\"from person p, address a \" +\n" +
		"						\"where p.person_id = a.person_id \" +\n" +
		"						\"and p.person_id = ?\";\n" +
		"	}\n" +
		"}\n"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=213700
public void testBug198074_dup213700() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		int a=0, b=0, c=0, d=0, e=0, f=0, g=0, h=0, i=0;\n" +
		"if( (a == b && b == c) &&\n" +
		"    (d == e) &&\n" +
		"    (f == g && h == i) \n" +
		"    ){\n" +
		"}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0;\n" +
		"		if ((a == b && b == c) &&\n" +
		"				(d == e) &&\n" +
		"				(f == g && h == i)) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 199265: [formatter] 3.3 Code Formatter mis-places commented-out import statements
 * @test Ensure that the formatter keep commented import declarations on their lines
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=199265"
 */
public void testBug199265a() throws JavaModelException {
	String source =
		"import java.util.List;\n" + 
		"//import java.util.HashMap;\n" + 
		"import java.util.Set;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265b() throws JavaModelException {
	String source =
		"import java.util.List;\n" + 
		"import java.util.Set;\n" + 
		"//import java.util.HashMap;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265c1() throws JavaModelException {
	String source =
		"import java.util.List;\n" + 
		"//            CU         snippet\n" + 
		"public class X03 {\n" + 
		"	List field;\n" + 
		"}\n";
	formatSource(source,
		"import java.util.List;\n" + 
		"\n" + 
		"//            CU         snippet\n" + 
		"public class X03 {\n" + 
		"	List field;\n" + 
		"}\n"
	);
}
public void testBug199265c2() throws JavaModelException {
	this.formatterPrefs.comment_format_header = true;
	String source =
		"import java.util.List;\n" + 
		"//            CU         snippet\n" + 
		"public class X03 {\n" + 
		"	List field;\n" + 
		"}\n";
	formatSource(source,
		"import java.util.List;\n" + 
		"\n" + 
		"// CU snippet\n" + 
		"public class X03 {\n" + 
		"	List field;\n" + 
		"}\n"
	);
}
public void testBug199265c3() throws JavaModelException {
	String source =
		"import java.util.List;\n" + 
		"\n" + 
		"// line comment\n" + 
		"public class X03 {\n" + 
		"	List field;\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp1a() throws JavaModelException {
	String source =
		"package wksp1;\n" + 
		"\n" + 
		"import java.util.*;\n" + 
		"import java.util.List; // line comment\n" + 
		"\n" + 
		"/**\n" + 
		" * Javadoc comment\n" + 
		" */\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp1b() throws JavaModelException {
	String source =
		"package wksp1;\n" + 
		"\n" + 
		"import java.util.Map;\n" + 
		"\n" + 
		"//==========================\n" + 
		"// Line comment\n" + 
		"//==========================\n" + 
		"\n" + 
		"/**\n" + 
		" * Javadoc comment\n" + 
		" */\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp2a() throws JavaModelException {
	String source =
		"package wksp2;\n" + 
		"\n" + 
		"import java.util.Map;\n" + 
		"\n" + 
		"//#if defined(TEST)\n" + 
		"import java.util.Vector;\n" + 
		"//#else\n" + 
		"//##import java.util.Set;\n" + 
		"//#endif\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265_wksp3a() throws JavaModelException {
	String source =
		"package wksp3;\n" + 
		"\n" + 
		"import java.util.Set;	// comment 1\n" + 
		"import java.util.Map;	// comment 2\n" + 
		"import java.util.List;	// comment 3\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp3;\n" + 
		"\n" + 
		"import java.util.Set; // comment 1\n" + 
		"import java.util.Map; // comment 2\n" + 
		"import java.util.List; // comment 3\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n"
	);
}

/**
 * @bug 208541: [formatter] Formatter does not format whole region/selection
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=208541"
 */
public void testBug208541() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class MyTest {\n" +
		"\n" +
		"    public void testname() throws Exception {\n" +
		"        int i = 5, j = 6, k = 7;\n" +
		"        if (new String().length() != 0 &&\n" +
		"                (i < j && j < k)) {\n" +
		"\n" +
		"        }\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class MyTest {\n" +
		"\n" +
		"	public void testname() throws Exception {\n" +
		"		int i = 5, j = 6, k = 7;\n" +
		"		if (new String().length() != 0 &&\n" +
		"				(i < j && j < k)) {\n" +
		"\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 283467: [formatter] wrong indentation with 'Never join lines' selected
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=283467"
 */
public void testBug283467() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class TestFormatter {\n" +
		"\n" +
		"        public static void main(String[] args) {\n" +
		"                int variable = TestFormatter.doInCallback(new Runnable() {\n" +
		"                        public void run() {\n" +
		"                                // Some comments or code here\n" +
		"                        }\n" +
		"                });\n" +
		"                System.out.println(variable);\n" +
		"        }\n" +
		"\n" +
		"        public static int doInCallback(Runnable r) {\n" +
		"                return 0;\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class TestFormatter {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		int variable = TestFormatter.doInCallback(new Runnable() {\n" +
		"			public void run() {\n" +
		"				// Some comments or code here\n" +
		"			}\n" +
		"		});\n" +
		"		System.out.println(variable);\n" +
		"	}\n" +
		"\n" +
		"	public static int doInCallback(Runnable r) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * @bug 285565: [formatter] wrong indentation with 'Never join lines' selected
 * @test Test to make sure that use either formatter or {@link IndentManipulation}
 * 	API methods an indentation set to zero does not thrown any exception.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=285565"
 */
public void testBug285565a() {
	try {
		assertEquals("Should be 0", 0, IndentManipulation.measureIndentInSpaces("", 0));
		assertEquals("Should be 0", 0, IndentManipulation.measureIndentInSpaces("\t", 0));
		assertEquals("Should be 1", 1, IndentManipulation.measureIndentInSpaces("\t ", 0));
		assertEquals("Should be blank", "\t", IndentManipulation.extractIndentString("\tabc", 0, 0));
	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565b() {
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.tab_size = 0;
	String source = "public class test {\n"
			+ "    public static void main(String[] args) {\n"
			+ "        int B= 12;\n"
			+ "        int C= B - 1;\n"
			+ "        int K= 99;\n"
			+ "        int f1= K - 1 - C;\n"
			+ "        int f2= K - C - C - C;\n"
			+ "    }\n" + "}\n";
	formatSource(source, "public class test {\n"
			+ "public static void main(String[] args) {\n"
			+ "int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;\n"
			+ "}\n"
			+ "}\n");
}
public void testBug285565c() {
	String result = "int B = 12;\n"
		+ " int C = B - 1;\n"
		+ " int K = 99;\n"
		+ " int f1 = K - 1 - C;\n"
		+ " int f2 = K - C - C - C;" ;

	try {
		assertEquals("Should be as shown", result, IndentManipulation.changeIndent("int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;" ,0,0,0, " ","\n"));

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565d() {
	String result = "int B = 12;\n"
		+ "int C = B - 1;\n"
		+ "int K = 99;\n"
		+ "int f1 = K - 1 - C;\n"
		+ "int f2 = K - C - C - C;" ;

	try {
		assertEquals("Should be as shown", result, IndentManipulation.trimIndent("int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;" , 0, 0, 0));

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}
public void testBug285565e() {
	try {
		IndentManipulation.getChangeIndentEdits("int B = 12;\n"
			+ "int C = B - 1;\n"
			+ "int K = 99;\n"
			+ "int f1 = K - 1 - C;\n"
			+ "int f2 = K - C - C - C;", 0, 0, 0, " ");

	} catch (IllegalArgumentException e) {
		assertTrue("Should not happen", false);
	}
}

/**
 * @bug 286601: [formatter] Code formatter formats anonymous inner classes wrongly when 'Never join lines' is on
 * @test Test to make sure that indentation is correct in anonymous inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286601"
 */
public void testBug286601() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void aMethod()\n" +
		"    {\n" +
		"        Object anObject = new Object()\n" +
		"        {\n" +
		"            boolean aVariable;\n" +
		"        };\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void aMethod() {\n" +
		"		Object anObject = new Object()\n" +
		"		{\n" +
		"			boolean aVariable;\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"long x1 = 100000000\n" +
		"        + 200000000\n" +
		"        + 300000000;\n" +
		"long x2 = 100000000\n" +
		"        + 200000000\n" +
		"        + 300000000\n" +
		"        + 400000000;\n" +
		"long x3 = 100000000\n" +
		"        + 200000000\n" +
		"        + 300000000\n" +
		"        + 400000000\n" +
		"        + 500000000;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		long x1 = 100000000\n" +
		"				+ 200000000\n" +
		"				+ 300000000;\n" +
		"		long x2 = 100000000\n" +
		"				+ 200000000\n" +
		"				+ 300000000\n" +
		"				+ 400000000;\n" +
		"		long x3 = 100000000\n" +
		"				+ 200000000\n" +
		"				+ 300000000\n" +
		"				+ 400000000\n" +
		"				+ 500000000;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601c() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.brace_position_for_anonymous_type_declaration= DefaultCodeFormatterConstants.NEXT_LINE;
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void aMethod()\n" +
		"    {\n" +
		"        Object anObject = new Object()\n" +
		"        {\n" +
		"            boolean aVariable;\n" +
		"            void foo()\n" +
		"            {\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void aMethod() {\n" +
		"		Object anObject = new Object()\n" +
		"		{\n" +
		"			boolean aVariable;\n" +
		"\n" +
		"			void foo()\n" +
		"			{\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601d() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.brace_position_for_anonymous_type_declaration= DefaultCodeFormatterConstants.NEXT_LINE;
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void aMethod()\n" +
		"    {\n" +
		"        Object anObject = new Object() /* comment */\n" +
		"        {\n" +
		"            boolean aVariable;\n" +
		"            void foo() /* comment */ \n" +
		"            {\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	public void aMethod() {\n" +
		"		Object anObject = new Object() /* comment */\n" +
		"		{\n" +
		"			boolean aVariable;\n" +
		"\n" +
		"			void foo() /* comment */\n" +
		"			{\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601e() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"public class Test\n" +
		"{\n" +
		"    public void build(String href) {\n" +
		"        // set the href on the related topic\n" +
		"        if (href == null)\n" +
		"            setHref(\"\"); //$NON-NLS-1$\n" +
		"        else {\n" +
		"            if (!href.equals(\"\") // no empty link //$NON-NLS-1$\n" +
		"                    && !href.startsWith(\"/\") // no help url //$NON-NLS-1$\n" +
		"                    && href.indexOf(\':\') == -1) // no other protocols\n" +
		"            {\n" +
		"                setHref(\"/test/\" + href); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"            }\n" +
		"        }\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test\n" +
		"{\n" +
		"	public void build(String href)\n" +
		"	{\n" +
		"		// set the href on the related topic\n" +
		"		if (href == null)\n" +
		"			setHref(\"\"); //$NON-NLS-1$\n" +
		"		else\n" +
		"		{\n" +
		"			if (!href.equals(\"\") // no empty link //$NON-NLS-1$\n" +
		"					&& !href.startsWith(\"/\") // no help url //$NON-NLS-1$\n" +
		"					&& href.indexOf(\':\') == -1) // no other protocols\n" +
		"			{\n" +
		"				setHref(\"/test/\" + href); //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601f() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"public class Test\n" +
		"{\n" +
		"    \n" +
		"    private AntModel getAntModel(final File buildFile) {\n" +
		"        AntModel model= new AntModel(XMLCore.getDefault(), doc, null, new LocationProvider(null) {\n" +
		"            /* (non-Javadoc)\n" +
		"             * @see org.eclipse.ant.internal.ui.editor.outline.ILocationProvider#getLocation()\n" +
		"             */\n" +
		"            public IPath getLocation() {\n" +
		"                return new Path(buildFile.getAbsolutePath());\n" +
		"            }\n" +
		"        });\n" +
		"        model.reconcile(null);\n" +
		"        return model;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"public class Test\n" +
		"{\n" +
		"\n" +
		"	private AntModel getAntModel(final File buildFile)\n" +
		"	{\n" +
		"		AntModel model = new AntModel(XMLCore.getDefault(), doc, null,\n" +
		"				new LocationProvider(null)\n" +
		"				{\n" +
		"					/*\n" +
		"					 * (non-Javadoc)\n" +
		"					 * \n" +
		"					 * @see\n" +
		"					 * org.eclipse.ant.internal.ui.editor.outline.ILocationProvider\n" +
		"					 * #getLocation()\n" +
		"					 */\n" +
		"					public IPath getLocation()\n" +
		"					{\n" +
		"						return new Path(buildFile.getAbsolutePath());\n" +
		"					}\n" +
		"				});\n" +
		"		model.reconcile(null);\n" +
		"		return model;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286601g() {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"package massive;\n" +
		"\n" +
		"public class X05b\n" +
		"{\n" +
		"\n" +
		"    public void foo() throws NullPointerException {\n" +
		"\n" +
		"        Object body = new Object() {\n" +
		"            public void run(StringBuffer monitor) throws IllegalArgumentException {\n" +
		"                IResourceVisitor visitor = new IResourceVisitor() {\n" +
		"                    public boolean visit(String resource) throws IllegalArgumentException {\n" +
		"                        return true;\n" +
		"                    }\n" +
		"                };\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		"interface IResourceVisitor {\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X05b {\n" +
		"\n" +
		"	public void foo() throws NullPointerException {\n" +
		"\n" +
		"		Object body = new Object() {\n" +
		"			public void run(StringBuffer monitor)\n" +
		"					throws IllegalArgumentException {\n" +
		"				IResourceVisitor visitor = new IResourceVisitor() {\n" +
		"					public boolean visit(String resource)\n" +
		"							throws IllegalArgumentException {\n" +
		"						return true;\n" +
		"					}\n" +
		"				};\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"\n" +
		"interface IResourceVisitor {\n" +
		"}\n"
	);
}
public void testBug286601h() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"\n" +
		"public class X05b\n" +
		"{\n" +
		"\n" +
		"    public void foo() throws NullPointerException {\n" +
		"\n" +
		"        Object body = new Object() {\n" +
		"            public void run(StringBuffer monitor) throws IllegalArgumentException {\n" +
		"                IResourceVisitor visitor = new IResourceVisitor() {\n" +
		"                    public boolean visit(String resource) throws IllegalArgumentException {\n" +
		"                        return true;\n" +
		"                    }\n" +
		"                };\n" +
		"            }\n" +
		"        };\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		"interface IResourceVisitor {\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X05b\n" +
		"{\n" +
		"\n" +
		"	public void foo() throws NullPointerException\n" +
		"	{\n" +
		"\n" +
		"		Object body = new Object()\n" +
		"		{\n" +
		"			public void run(StringBuffer monitor)\n" +
		"					throws IllegalArgumentException\n" +
		"			{\n" +
		"				IResourceVisitor visitor = new IResourceVisitor()\n" +
		"				{\n" +
		"					public boolean visit(String resource)\n" +
		"							throws IllegalArgumentException\n" +
		"					{\n" +
		"						return true;\n" +
		"					}\n" +
		"				};\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"\n" +
		"interface IResourceVisitor\n" +
		"{\n" +
		"}\n"
	);
}
public void testBug286601i1() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"\n" +
		"public class X06a {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    /* Comment 1 */\n" +
		"    /* Comment 2 */ { \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"    /* Comment 3 */ { \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X06a\n" +
		"{\n" +
		"\n" +
		"	// Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"	// branch[y]).\n" +
		"	private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"	{\n" +
		"		/* Comment 1 */\n" +
		"		/* Comment 2 */{ \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"		/* Comment 3 */{ \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug286601i2() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"\n" +
		"public class X06a {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    /* Comment 1 */\n" +
		"    /* Comment 2 */ { \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"    /* Comment 3 */ { \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X06a\n" +
		"{\n" +
		"\n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"    // branch[y]).\n" +
		"    private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"    {\n" +
		"     /* Comment 1 */\n" +
		"     /* Comment 2 */{ \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"     /* Comment 3 */{ \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n"
	);
}
public void testBug286601j1() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"\n" +
		"public class X06b {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    { \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"    { \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X06b\n" +
		"{\n" +
		"\n" +
		"	// Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"	// branch[y]).\n" +
		"	private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"	{\n" +
		"		{ \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"		{ \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"	};\n" +
		"\n" +
		"}\n"
	);
}
public void testBug286601j2() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"\n" +
		"public class X06b {\n" +
		"\n" +
		"    \n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] || branch[y]). \n" +
		"    private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE= {\n" +
		"    { \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"    { \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X06b\n" +
		"{\n" +
		"\n" +
		"    // Table to merge access modes for condition statements (e.g branch[x] ||\n" +
		"    // branch[y]).\n" +
		"    private static final String[][] ACCESS_MODE_CONDITIONAL_TABLE =\n" +
		"    {\n" +
		"     { \"1234567890123456789012345678901234567890\", \"1234567890123456789012345678901234567890\" },\n" +
		"     { \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ______________\" },\n" +
		"    };\n" +
		"\n" +
		"}\n"
	);
}
public void testBug286601k() {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.alignment_for_expressions_in_array_initializer = DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source =
		"package massive;\n" +
		"\n" +
		"public class X07 {\n" +
		"    private MinimizedFileSystemElement selectFiles(final Object rootFileSystemObject, final IImportStructureProvider structureProvider) {\n" +
		"\n" +
		"        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {\n" +
		"            public void run() {\n" +
		"                //Create the root element from the supplied file system object\n" +
		"            }\n" +
		"        });\n" +
		"\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"package massive;\n" +
		"\n" +
		"public class X07\n" +
		"{\n" +
		"    private MinimizedFileSystemElement selectFiles(\n" +
		"            final Object rootFileSystemObject,\n" +
		"            final IImportStructureProvider structureProvider)\n" +
		"    {\n" +
		"\n" +
		"        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable()\n" +
		"        {\n" +
		"            public void run()\n" +
		"            {\n" +
		"                // Create the root element from the supplied file system object\n" +
		"            }\n" +
		"        });\n" +
		"\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n"
	);
}

/**
 * @bug 286668: [formatter] 'Never Join Lines' joins lines that are split on method invocation
 * @test Test to make sure that lines are joined when using 'Never Join Lines' preference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286668"
 */
public void testBug286668() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\n" +
		"				\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668c() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"		.append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\n" +
		"				\"def\").append(\"ghi\")\n" +
		"				.append(\"jkl\").append(\n" +
		"						\"mno\")\n" +
		"				.append(\"pqr\").append(\n" +
		"						\"stu\").append(\n" +
		"						\"vwx\").append(\n" +
		"						\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668b_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\n" +
		"				\"def\")\n" +
		"				.append(\"ghi\").append(\n" +
		"						\"jkl\").append(\n" +
		"						\"mno\")\n" +
		"				.append(\"pqr\").append(\n" +
		"						\"stu\").append(\n" +
		"						\"vwx\").append(\n" +
		"						\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668c_40w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 40;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"		.append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\n" +
		"				\"def\")\n" +
		"				.append(\"ghi\").append(\n" +
		"						\"jkl\").append(\n" +
		"						\"mno\")\n" +
		"				.append(\"pqr\").append(\n" +
		"						\"stu\").append(\n" +
		"						\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\").append(\"ghi\")\n" +
		"				.append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668b_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug286668c_60w() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.page_width = 60;
	String source =
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"		StringBuilder builder = new StringBuilder();\n" +
		"		builder.append(\"abc\").append(\"def\")\n" +
		"				.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" +
		"				.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" +
		"				.append(\"yz\");\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}

/**
 * @bug 290905: [formatter] Certain formatter pref constellation cause endless loop ==> OOME
 * @test Verify that there's endless loop when setting tab length to zero.
 * 	As the fix finalize bug 285565 implementation, added tests address only
 * 	missed test cases.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=290905"
 */
public void testBug290905a() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 2;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"void foo() throws Exception { if (true) return; else throw new Exception(); }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"  void foo() throws Exception {\n" +
		"    if (true)\n" +
		"      return;\n" +
		"    else\n" +
		"      throw new Exception();\n" +
		"  }\n" +
		"}\n"
	);
}
public void testBug290905b() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 2;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"void foo() throws Exception { if (true) return; else throw new Exception(); }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 2, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"  void foo() throws Exception {\n" +
		"    if (true)\n" +
		"      return;\n" +
		"    else\n" +
		"      throw new Exception();\n" +
		"  }\n" +
		"}\n"
	);
}
public void testBug290905c() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"       // format line comment preference is activated\n" +
		"}\n",
		false /* do not repeat */
	);
}
public void testBug290905d() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test mixed, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"       // format line comment preference is activated\n" +
		"}\n",
		false /* do not repeat */
	);
}
public void testBug290905e() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use tabs to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"       // format line comment preference is activated\n" +
		"}\n",
		false /* do not repeat */
	);
}
public void testBug290905f() throws JavaModelException {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = false;
	String source =
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the format line comment preference is activated\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test tab char = TAB, tab size = 0, indent size = 0, use spaces to indent\n" +
		" */\n" +
		"public class Test {\n" +
		"int i; // this is a long comment which should be split into two lines as the\n" +
		"// format line comment preference is activated\n" +
		"}\n",
		false /* do not repeat */
	);
}

/**
 * @bug 293496:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * @test Verify that a realigned annotation keep the 'insert_space_before_opening_brace_in_array_initializer'
 * 		preference initial value.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293496"
 */
public void testBug293240() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"  public static <A, B> Function<A, B> forMap(\n" +
		"      Map<? super A, ? extends B> map, @Nullable final B defaultValue) {\n" +
		"    if (defaultValue == null) {\n" +
		"      return forMap(map);\n" +
		"    }\n" +
		"    return new ForMapWithDefault<A, B>(map, defaultValue);\n" +
		"  }\n" +
		"  public Object[] bar() {\n" +
		"	  return new Object[] { null };\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"    public static <A, B> Function<A, B> forMap(Map<? super A, ? extends B> map,\n" +
		"            @Nullable final B defaultValue) {\n" +
		"        if (defaultValue == null) {\n" +
		"            return forMap(map);\n" +
		"        }\n" +
		"        return new ForMapWithDefault<A, B>(map, defaultValue);\n" +
		"    }\n" +
		"\n" +
		"    public Object[] bar() {\n" +
		"        return new Object[] { null };\n" +
		"    }\n" +
		"}\n"
	);
}

/**
 * @bug 293496:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * @test Verify that non ArithmeticException occurs when using tab size = 0
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293496"
 */
public void testBug293496() {
	final Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
	options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
	options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "0");
	options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "0");
	DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
	assertEquals("wrong indentation string", org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING, codeFormatter.createIndentationString(0));
}
}
