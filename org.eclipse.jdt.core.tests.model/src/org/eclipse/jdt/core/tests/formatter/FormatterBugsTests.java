/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
	formatSource(source,
		"import java.util.List;\n" + 
		"import java.util.Set;\n" + 
		"\n" + 
		"//import java.util.HashMap;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"}\n"
	);
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
public void testBug199265d1() throws JavaModelException {
	String source =
		"import java.util.Set; // trailing comment\n" + 
		"// line comment\n" + 
		"import java.util.Map; // trailing comment\n" + 
		"// line comment\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"import java.util.Set; // trailing comment\n" + 
		"// line comment\n" + 
		"import java.util.Map; // trailing comment\n" + 
		"// line comment\n" + 
		"\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug199265d2() throws JavaModelException {
	String source =
		"import java.util.Set; // trailing comment\n" + 
		"// line comment\n" + 
		"import java.util.Map; // trailing comment\n" + 
		"// line comment\n" + 
		"\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source);
}
public void testBug199265d3() throws JavaModelException {
	String source =
		"import java.util.Set; // trailing comment\n" + 
		"	// line comment\n" + 
		"import java.util.Map; // trailing comment\n" + 
		"	// line comment\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"import java.util.Set; // trailing comment\n" + 
		"// line comment\n" + 
		"import java.util.Map; // trailing comment\n" + 
		"// line comment\n" + 
		"\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"}\n"
	);
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
 * @bug 252556: [formatter] Spaces removed before formatted region of a compilation unit.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=252556"
 */
// TODO Fix the bug... this test currently verifies that the problem still occurs!
public void testBug252556() {
	String source =
		"package a;\n" + 
		"\n" + 
		"public class Test {\n" + 
		"\n" + 
		"	private int field;\n" + 
		"	\n" + 
		"	[#/**\n" + 
		"	 * fds \n" + 
		"	 */#]\n" + 
		"	public void foo() {\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package a;\n" + 
		"\n" + 
		"public class Test {\n" + 
		"\n" + 
		"	private int field;\n" + 
//		"	\n" + // this is the expected untouched line
		"\n" + // instead the tab is removed although it is outside the selection...
		"	/**\n" + 
		"	 * fds\n" + 
		"	 */\n" + 
		"	public void foo() {\n" + 
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
		"public class X05\n" +
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
		"public class X05 {\n" +
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
		"public class X05\n" +
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
		"public class X05\n" +
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
 * @bug 293300: [formatter] The formatter is still unstable in certain circumstances
 * @test Verify that formatting twice a compilation unit does not produce different output
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293300"
 */
public void testBug293300_wksp1_01() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	boolean foo(int test, int value) {\n" + 
		"		// This comment may also be impacted after having been split in several lines. Furthermore, it\'s also important to verify that the algorithm works when the comment is split into several lines. It\'s a common use case that it may works for 1, 2 but not for 3 iterations...\n" + 
		"		if (test == 0) {\n" + 
		"			// skip\n" + 
		"		} else if (Math.sqrt(Math.pow(test, 2)) > 10) // This is the offending comment after having been split into several lines\n" + 
		"			return false;\n" + 
		"		return true;\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	boolean foo(int test, int value) {\n" + 
		"		// This comment may also be impacted after having been split in several\n" + 
		"		// lines. Furthermore, it\'s also important to verify that the algorithm\n" + 
		"		// works when the comment is split into several lines. It\'s a common use\n" + 
		"		// case that it may works for 1, 2 but not for 3 iterations...\n" + 
		"		if (test == 0) {\n" + 
		"			// skip\n" + 
		"		} else if (Math.sqrt(Math.pow(test, 2)) > 10) // This is the offending\n" + 
		"														// comment after having\n" + 
		"														// been split into\n" + 
		"														// several lines\n" + 
		"			return false;\n" + 
		"		return true;\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wkps1_02() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"	String field;\n" + 
		"	 public X02(String test) {\n" + 
		"		field= test.toLowerCase();\n" + 
		"		try {\n" + 
		"			testWhetherItWorksOrNot(test); // This comment will be split and should not involve instability\n" + 
		"		} catch (Exception e) {\n" + 
		"			return;\n" + 
		"		}\n" + 
		"	 }\n" + 
		"	private void testWhetherItWorksOrNot(String test) {\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"	String field;\n" + 
		"\n" + 
		"	public X02(String test) {\n" + 
		"		field = test.toLowerCase();\n" + 
		"		try {\n" + 
		"			testWhetherItWorksOrNot(test); // This comment will be split and\n" + 
		"											// should not involve instability\n" + 
		"		} catch (Exception e) {\n" + 
		"			return;\n" + 
		"		}\n" + 
		"	}\n" + 
		"\n" + 
		"	private void testWhetherItWorksOrNot(String test) {\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wkps1_03() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X03 {\n" + 
		"public static final native int foo(\n" + 
		"	int firstParameter,\n" + 
		"	int secondParameter,\n" + 
		"	int[] param3);        //When a long comment is placed here with at least one line to follow,\n" + 
		"						  //    the second line may be difficult to be formatted correctly\n" + 
		"public static final native int bar();\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X03 {\n" + 
		"	public static final native int foo(int firstParameter, int secondParameter,\n" + 
		"			int[] param3); // When a long comment is placed here with at least\n" + 
		"							// one line to follow,\n" + 
		"							// the second line may be difficult to be formatted\n" + 
		"							// correctly\n" + 
		"\n" + 
		"	public static final native int bar();\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug293300_wkps1_04() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"interface Y04_____________________________ {\n" + 
		"}\n" + 
		"\n" + 
		"public interface X04 extends Y04_____________________________ { // modifier constant\n" + 
		"	// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)\n" + 
		"	final int AccDefault = 0;\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"interface Y04_____________________________ {\n" + 
		"}\n" + 
		"\n" + 
		"public interface X04 extends Y04_____________________________ { // modifier\n" + 
		"																// constant\n" + 
		"	// those constants are depending upon ClassFileConstants (relying that\n" + 
		"	// classfiles only use the 16 lower bits)\n" + 
		"	final int AccDefault = 0;\n" + 
		"}\n"
	);
}
public void testBug293300_wkps1_05() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	private final static String[] TEST_BUG = {\"a\", //$NON-NLS-1$\n" + 
		"			\"b\", //$NON-NLS-1$\n" + 
		"			\"c\", //$NON-NLS-1$\n" + 
		"	};\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	private final static String[] TEST_BUG = { \"a\", //$NON-NLS-1$\n" + 
		"			\"b\", //$NON-NLS-1$\n" + 
		"			\"c\", //$NON-NLS-1$\n" + 
		"	};\n" + 
		"}\n"
	);
}
public void testBug293300_wkps1_05_JoinLinesComments_BracesNextLine() {
	this.formatterPrefs.join_wrapped_lines = false;
	setUpBracesPreferences(DefaultCodeFormatterConstants.NEXT_LINE);
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	private final static String[] TEST_BUG = {\"a\", //$NON-NLS-1$\n" + 
		"			\"b\", //$NON-NLS-1$\n" + 
		"			\"c\", //$NON-NLS-1$\n" + 
		"	};\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X05\n" + 
		"{\n" + 
		"	private final static String[] TEST_BUG =\n" + 
		"	{ \"a\", //$NON-NLS-1$\n" + 
		"			\"b\", //$NON-NLS-1$\n" + 
		"			\"c\", //$NON-NLS-1$\n" + 
		"	};\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_01() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	protected String foo(String[] tests) {\n" + 
		"		String result = null;\n" + 
		"		for (int i = 0; i < tests.length; i++) {\n" + 
		"			String test = tests[i];\n" + 
		"			if (test.startsWith(\"test\")) { //$NON-NLS-1$\n" + 
		"				//we got the malformed tree exception here\n" + 
		"				result = test;\n" + 
		"			}\n" + 
		"		}\n" + 
		"		return result;\n" + 
		"	}\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	protected String foo(String[] tests) {\n" + 
		"		String result = null;\n" + 
		"		for (int i = 0; i < tests.length; i++) {\n" + 
		"			String test = tests[i];\n" + 
		"			if (test.startsWith(\"test\")) { //$NON-NLS-1$\n" + 
		"				// we got the malformed tree exception here\n" + 
		"				result = test;\n" + 
		"			}\n" + 
		"		}\n" + 
		"		return result;\n" + 
		"	}\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_02() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"\n" + 
		"	public void foo(int kind) {\n" + 
		"		switch (kind) {\n" + 
		"			case 0 :\n" + 
		"				break;\n" + 
		"			case 1 :\n" + 
		"				//the first formatting looks strange on this already splitted\n" + 
		"				// comment\n" + 
		"				if (true)\n" + 
		"					return;\n" + 
		"			//fall through\n" + 
		"			default:\n" + 
		"				if (kind < 0)\n" + 
		"					return;\n" + 
		"				break;\n" + 
		"		}\n" + 
		"	}\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	public void foo(int kind) {\n" + 
		"		switch (kind) {\n" + 
		"		case 0:\n" + 
		"			break;\n" + 
		"		case 1:\n" + 
		"			// the first formatting looks strange on this already splitted\n" + 
		"			// comment\n" + 
		"			if (true)\n" + 
		"				return;\n" + 
		"			// fall through\n" + 
		"		default:\n" + 
		"			if (kind < 0)\n" + 
		"				return;\n" + 
		"			break;\n" + 
		"		}\n" + 
		"	}\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_03() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X03 {\n" + 
		"	public byte[] foo(byte value) {\n" + 
		"		byte[] result = new byte[10];\n" + 
		"		int valTest = 0;\n" + 
		"		switch (value) {\n" + 
		"			case 1 :\n" + 
		"				for (int j = 10; j >= 0; j--) {\n" + 
		"					result[j] = (byte) (valTest & 0xff); // Bottom 8\n" + 
		"					// bits\n" + 
		"					valTest = valTest >>> 2;\n" + 
		"				}\n" + 
		"				break;\n" + 
		"		}\n" + 
		"		return result;\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X03 {\n" + 
		"	public byte[] foo(byte value) {\n" + 
		"		byte[] result = new byte[10];\n" + 
		"		int valTest = 0;\n" + 
		"		switch (value) {\n" + 
		"		case 1:\n" + 
		"			for (int j = 10; j >= 0; j--) {\n" + 
		"				result[j] = (byte) (valTest & 0xff); // Bottom 8\n" + 
		"				// bits\n" + 
		"				valTest = valTest >>> 2;\n" + 
		"			}\n" + 
		"			break;\n" + 
		"		}\n" + 
		"		return result;\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_04() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"		int lastDiagonal[]= new int[1000000 + 1]; // this line comments configuration\n" + 
		"		// may screw up the formatter to know which one\n" + 
		"		int origin= 1000000 / 2; // needs to stay at its current indentation or not\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X04 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"		int lastDiagonal[] = new int[1000000 + 1]; // this line comments\n" + 
		"													// configuration\n" + 
		"		// may screw up the formatter to know which one\n" + 
		"		int origin = 1000000 / 2; // needs to stay at its current indentation or\n" + 
		"									// not\n" + 
		"	}\n" + 
		"}\n"
	);
}
private static final String EXPECTED_OUTPUT_WKSP2E1 =
	"package wksp2;\n" + 
	"\n" + 
	"public class X05 {\n" + 
	"	void foo(int val) {\n" + 
	"		try {\n" + 
	"			loop: for (int i = 0; i < 10; i++) {\n" + 
	"				switch (val) {\n" + 
	"				case 1:\n" + 
	"					if (i == 0) {\n" + 
	"						if (true) {\n" + 
	"							val++;\n" + 
	"						} // these comments\n" + 
	"							// may be wrongly\n" + 
	"							// realigned\n" + 
	"							// by the formatter\n" + 
	"\n" + 
	"						// other comment\n" + 
	"						val--;\n" + 
	"						continue loop;\n" + 
	"					}\n" + 
	"				default:\n" + 
	"					throw new IllegalArgumentException();\n" + 
	"				}\n" + 
	"			}\n" + 
	"		} finally {\n" + 
	"		}\n" + 
	"	}\n" + 
	"}\n";
public void testBug293300_wksp2_05() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							// may be wrongly\n" + 
		"							// realigned\n" + 
		"							// by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1);
}
public void testBug293300_wksp2_05b() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							 // may be wrongly\n" + 
		"							 // realigned\n" + 
		"							 // by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1);
}
private static final String EXPECTED_OUTPUT_WKSP2E3 =
	"package wksp2;\n" + 
	"\n" + 
	"public class X05 {\n" + 
	"	void foo(int val) {\n" + 
	"		try {\n" + 
	"			loop: for (int i = 0; i < 10; i++) {\n" + 
	"				switch (val) {\n" + 
	"				case 1:\n" + 
	"					if (i == 0) {\n" + 
	"						if (true) {\n" + 
	"							val++;\n" + 
	"						} // these comments\n" + 
	"							// may be wrongly\n" + 
	"							// realigned\n" + 
	"							// by the formatter\n" + 
	"\n" + 
	"						// other comment\n" + 
	"						val--;\n" + 
	"						continue loop;\n" + 
	"					}\n" + 
	"				default:\n" + 
	"					throw new IllegalArgumentException();\n" + 
	"				}\n" + 
	"			}\n" + 
	"		} finally {\n" + 
	"		}\n" + 
	"	}\n" + 
	"}\n";
public void testBug293300_wksp2_05c() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							  // may be wrongly\n" + 
		"							  // realigned\n" + 
		"							  // by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
public void testBug293300_wksp2_05d() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							   // may be wrongly\n" + 
		"							   // realigned\n" + 
		"							   // by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
public void testBug293300_wksp2_05e() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"								// may be wrongly\n" + 
		"								// realigned\n" + 
		"								// by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3);
}
private static final String EXPECTED_OUTPUT_WKSP2E1_SPACES =
	"package wksp2;\n" + 
	"\n" + 
	"public class X05 {\n" + 
	"    void foo(int val) {\n" + 
	"        try {\n" + 
	"            loop: for (int i = 0; i < 10; i++) {\n" + 
	"                switch (val) {\n" + 
	"                case 1:\n" + 
	"                    if (i == 0) {\n" + 
	"                        if (true) {\n" + 
	"                            val++;\n" + 
	"                        } // these comments\n" + 
	"                          // may be wrongly\n" + 
	"                          // realigned\n" + 
	"                          // by the formatter\n" + 
	"\n" + 
	"                        // other comment\n" + 
	"                        val--;\n" + 
	"                        continue loop;\n" + 
	"                    }\n" + 
	"                default:\n" + 
	"                    throw new IllegalArgumentException();\n" + 
	"                }\n" + 
	"            }\n" + 
	"        } finally {\n" + 
	"        }\n" + 
	"    }\n" + 
	"}\n";
public void testBug293300_wksp2_05_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							// may be wrongly\n" + 
		"							// realigned\n" + 
		"							// by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1_SPACES);
}
public void testBug293300_wksp2_05b_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							 // may be wrongly\n" + 
		"							 // realigned\n" + 
		"							 // by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E1_SPACES);
}
private static final String EXPECTED_OUTPUT_WKSP2E3_SPACES =
	"package wksp2;\n" + 
	"\n" + 
	"public class X05 {\n" + 
	"    void foo(int val) {\n" + 
	"        try {\n" + 
	"            loop: for (int i = 0; i < 10; i++) {\n" + 
	"                switch (val) {\n" + 
	"                case 1:\n" + 
	"                    if (i == 0) {\n" + 
	"                        if (true) {\n" + 
	"                            val++;\n" + 
	"                        } // these comments\n" + 
	"                          // may be wrongly\n" + 
	"                          // realigned\n" + 
	"                          // by the formatter\n" + 
	"\n" + 
	"                        // other comment\n" + 
	"                        val--;\n" + 
	"                        continue loop;\n" + 
	"                    }\n" + 
	"                default:\n" + 
	"                    throw new IllegalArgumentException();\n" + 
	"                }\n" + 
	"            }\n" + 
	"        } finally {\n" + 
	"        }\n" + 
	"    }\n" + 
	"}\n";
public void testBug293300_wksp2_05c_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							  // may be wrongly\n" + 
		"							  // realigned\n" + 
		"							  // by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp2_05d_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"							   // may be wrongly\n" + 
		"							   // realigned\n" + 
		"							   // by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp2_05e_spaces() {
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X05 {\n" + 
		"	void foo(int val) {\n" + 
		"		try {\n" + 
		"			loop: for (int i=0; i<10; i++) {\n" + 
		"				switch (val) {\n" + 
		"					case 1 :\n" + 
		"						if (i==0) {\n" + 
		"							if (true) {\n" + 
		"								val++;\n" + 
		"							} //these comments\n" + 
		"								// may be wrongly\n" + 
		"								// realigned\n" + 
		"								// by the formatter\n" + 
		"\n" + 
		"							// other comment\n" + 
		"							val--;\n" + 
		"							continue loop;\n" + 
		"						}\n" + 
		"					default :\n" + 
		"						throw new IllegalArgumentException();\n" + 
		"				}\n" + 
		"			}\n" + 
		"		}\n" + 
		"		finally {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source, EXPECTED_OUTPUT_WKSP2E3_SPACES);
}
public void testBug293300_wksp_06() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X06 {\n" + 
		"public static final native int foo(\n" + 
		"	String field,        //First field\n" + 
		"	int[] array);        //This comment may cause trouble for the formatter, especially if there\'s another\n" + 
		"						  //    line below  \n" + 
		"public static final native int bar();\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X06 {\n" + 
		"	public static final native int foo(String field, // First field\n" + 
		"			int[] array); // This comment may cause trouble for the formatter,\n" + 
		"							// especially if there\'s another\n" + 
		"							// line below\n" + 
		"\n" + 
		"	public static final native int bar();\n" + 
		"}\n"
	);
}
public void testBug293300_wksp_07() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X07 {\n" + 
		"	void foo(boolean test) {\n" + 
		"		if (test) {\n" + 
		"			while (true) {\n" + 
		"				try {\n" + 
		"					try {\n" + 
		"					} finally {\n" + 
		"						if (true) {\n" + 
		"							try {\n" + 
		"								toString();\n" + 
		"							} catch (Exception e) {\n" + 
		"							} // nothing\n" + 
		"						}\n" + 
		"					} // first comment which does not move\n" + 
		"\n" + 
		"					// second comment which should not move\n" + 
		"					toString();\n" + 
		"				} catch (Exception e) {\n" + 
		"				}\n" + 
		"\n" + 
		"			} // last comment\n" + 
		"\n" + 
		"		}\n" + 
		"\n" + 
		"		return;\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source);
}
public void testBug293300_wksp2_08() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X08 {\n" + 
		"int foo(int x) {\n" + 
		"    while (x < 0) {\n" + 
		"        switch (x) {\n" + 
		"        \n" + 
		"        }\n" + 
		"    } // end while\n" + 
		"\n" + 
		"        // fill in output parameter\n" + 
		"    if(x > 10)\n" + 
		"        x = 1;\n" + 
		"\n" + 
		"        // return the value\n" + 
		"    return x;\n" + 
		"    }\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X08 {\n" + 
		"	int foo(int x) {\n" + 
		"		while (x < 0) {\n" + 
		"			switch (x) {\n" + 
		"\n" + 
		"			}\n" + 
		"		} // end while\n" + 
		"\n" + 
		"		// fill in output parameter\n" + 
		"		if (x > 10)\n" + 
		"			x = 1;\n" + 
		"\n" + 
		"		// return the value\n" + 
		"		return x;\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_08b() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X08 {\n" + 
		"int foo(int x) {\n" + 
		"    while (x < 0) {\n" + 
		"        switch (x) {\n" + 
		"        \n" + 
		"        }\n" + 
		"    } /* end while */\n" + 
		"\n" + 
		"        // fill in output parameter\n" + 
		"    if(x > 10)\n" + 
		"        x = 1;\n" + 
		"\n" + 
		"        // return the value\n" + 
		"    return x;\n" + 
		"    }\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X08 {\n" + 
		"	int foo(int x) {\n" + 
		"		while (x < 0) {\n" + 
		"			switch (x) {\n" + 
		"\n" + 
		"			}\n" + 
		"		} /* end while */\n" + 
		"\n" + 
		"		// fill in output parameter\n" + 
		"		if (x > 10)\n" + 
		"			x = 1;\n" + 
		"\n" + 
		"		// return the value\n" + 
		"		return x;\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_08c() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X08 {\n" + 
		"int foo(int x) {\n" + 
		"    while (x < 0) {\n" + 
		"        switch (x) {\n" + 
		"        \n" + 
		"        }\n" + 
		"    } /** end while */\n" + 
		"\n" + 
		"        // fill in output parameter\n" + 
		"    if(x > 10)\n" + 
		"        x = 1;\n" + 
		"\n" + 
		"        // return the value\n" + 
		"    return x;\n" + 
		"    }\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X08 {\n" + 
		"	int foo(int x) {\n" + 
		"		while (x < 0) {\n" + 
		"			switch (x) {\n" + 
		"\n" + 
		"			}\n" + 
		"		}\n" + 
		"		/** end while */\n" + 
		"\n" + 
		"		// fill in output parameter\n" + 
		"		if (x > 10)\n" + 
		"			x = 1;\n" + 
		"\n" + 
		"		// return the value\n" + 
		"		return x;\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_09() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X09 {\n" + 
		"void foo(int param) {\n" + 
		"        int local = param - 10000; // first comment\n" + 
		"                                    // on several lines\n" + 
		"        // following unrelated comment\n" + 
		"        // also on several lines\n" + 
		"        int value = param + 10000;\n" + 
		"}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X09 {\n" + 
		"	void foo(int param) {\n" + 
		"		int local = param - 10000; // first comment\n" + 
		"									// on several lines\n" + 
		"		// following unrelated comment\n" + 
		"		// also on several lines\n" + 
		"		int value = param + 10000;\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_10() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X10 {\n" + 
		"\n" + 
		"    private  String           field;          //  Trailing comment of the field\n" + 
		"                                               //  This comment was not well formatted\n" + 
		"                                               //  as an unexpected line was inserted after the first one\n" + 
		"\n" + 
		"    // -------------------------------\n" + 
		"    X10()  {}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X10 {\n" + 
		"\n" + 
		"	private String field; // Trailing comment of the field\n" + 
		"							// This comment was not well formatted\n" + 
		"							// as an unexpected line was inserted after the\n" + 
		"							// first one\n" + 
		"\n" + 
		"	// -------------------------------\n" + 
		"	X10() {\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_11() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public abstract class X11 {\n" + 
		"\n" + 
		"    // [NEW] \n" + 
		"    /**\n" + 
		"     * Comment foo\n" + 
		"     */\n" + 
		"    public abstract StringBuffer foo();\n" + 
		"//#if defined(TEST)\n" + 
		"//#else\n" + 
		"//#endif\n" + 
		"\n" + 
		"    // [NEW]\n" + 
		"    /**\n" + 
		"     * Comment foo2\n" + 
		"     */\n" + 
		"    public abstract StringBuffer foo2();\n" + 
		"    // [NEW]\n" + 
		"    /**\n" + 
		"     * Comment foo3\n" + 
		"     */\n" + 
		"    public abstract StringBuffer foo3();\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public abstract class X11 {\n" + 
		"\n" + 
		"	// [NEW]\n" + 
		"	/**\n" + 
		"	 * Comment foo\n" + 
		"	 */\n" + 
		"	public abstract StringBuffer foo();\n" + 
		"\n" + 
		"	// #if defined(TEST)\n" + 
		"	// #else\n" + 
		"	// #endif\n" + 
		"\n" + 
		"	// [NEW]\n" + 
		"	/**\n" + 
		"	 * Comment foo2\n" + 
		"	 */\n" + 
		"	public abstract StringBuffer foo2();\n" + 
		"\n" + 
		"	// [NEW]\n" + 
		"	/**\n" + 
		"	 * Comment foo3\n" + 
		"	 */\n" + 
		"	public abstract StringBuffer foo3();\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_12a() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X12 {\n" + 
		"\n" + 
		"\n" + 
		"	private boolean sampleField = false;   //trailing comment of the field which\n" + 
		" 	                                      //was wrongly formatted in previous\n" + 
		"	                                      //version as an unexpected empty lines was\n" + 
		"	                                      //inserted after the second comment line...\n" + 
		"\n" + 
		"\n" + 
		"	/**\n" + 
		"	    Javadoc comment\n" + 
		"	*/\n" + 
		"	public X12() {}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X12 {\n" + 
		"\n" + 
		"	private boolean sampleField = false; // trailing comment of the field which\n" + 
		"											// was wrongly formatted in previous\n" + 
		"											// version as an unexpected empty\n" + 
		"											// lines was\n" + 
		"											// inserted after the second comment\n" + 
		"											// line...\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Javadoc comment\n" + 
		"	 */\n" + 
		"	public X12() {\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_12b() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X12 {\n" + 
		"\n" + 
		"\n" + 
		"	private boolean sampleField = false;   //trailing comment of the field which\n" + 
		" 	                                       //was wrongly formatted in previous\n" + 
		"	                                       //version as an unexpected empty lines was\n" + 
		"	                                       //inserted after the second comment line...\n" + 
		"\n" + 
		"\n" + 
		"	/**\n" + 
		"	    Javadoc comment\n" + 
		"	*/\n" + 
		"	public X12() {}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X12 {\n" + 
		"\n" + 
		"	private boolean sampleField = false; // trailing comment of the field which\n" + 
		"											// was wrongly formatted in previous\n" + 
		"											// version as an unexpected empty\n" + 
		"											// lines was\n" + 
		"											// inserted after the second comment\n" + 
		"											// line...\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Javadoc comment\n" + 
		"	 */\n" + 
		"	public X12() {\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_13() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X13 {\n" + 
		"void foo(int x) {\n" + 
		"	switch (x) {\n" + 
		"		default : // regular object ref\n" + 
		"//				if (compileTimeType.isRawType() && runtimeTimeType.isBoundParameterizedType()) {\n" + 
		"//				    scope.problemReporter().unsafeRawExpression(this, compileTimeType, runtimeTimeType);\n" + 
		"//				}\n" + 
		"	}\n" + 
		"}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X13 {\n" + 
		"	void foo(int x) {\n" + 
		"		switch (x) {\n" + 
		"		default: // regular object ref\n" + 
		"			// if (compileTimeType.isRawType() &&\n" + 
		"			// runtimeTimeType.isBoundParameterizedType()) {\n" + 
		"			// scope.problemReporter().unsafeRawExpression(this,\n" + 
		"			// compileTimeType, runtimeTimeType);\n" + 
		"			// }\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_14() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public interface X14 {\n" + 
		"void foo();\n" + 
		"// line 1\n" + 
		"// line 2\n" + 
		"void bar();\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public interface X14 {\n" + 
		"	void foo();\n" + 
		"\n" + 
		"	// line 1\n" + 
		"	// line 2\n" + 
		"	void bar();\n" + 
		"}\n"
	);
}
// TODO (frederic) try to fix the formatter instability in the following test case
public void _testBug293300_wksp2_15a() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X15 {\n" + 
		"	void foo(int[] params) {\n" + 
		"		if (params.length > 0) { // trailing comment formatted in several lines...\n" + 
		"//			int length = params == null ? : 0 params.length; // this commented lined causes troubles for the formatter but only if the comment starts at column 1...\n" + 
		"			for (int i=0; i<params.length; i++) {\n" + 
		"			}\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"\n" + 
		"public class X15 {\n" + 
		"	void foo(int[] params) {\n" + 
		"		if (params.length > 0) { // trailing comment formatted in several\n" + 
		"									// lines...\n" + 
		"			// int length = params == null ? : 0 params.length; // this\n" + 
		"			// commented\n" + 
		"			// lined causes troubles for the formatter but only if the comment\n" + 
		"			// starts at column 1...\n" + 
		"			for (int i = 0; i < params.length; i++) {\n" + 
		"			}\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp2_15b() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X15 {\n" + 
		"	void foo(int[] params) {\n" + 
		"		if (params.length > 0) { // trailing comment formatted in several lines...\n" + 
		"			// int length = params == null ? : 0 params.length; // this commented lined does not cause troubles for the formatter when the comments is not on column 1...\n" + 
		"			for (int i=0; i<params.length; i++) {\n" + 
		"			}\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X15 {\n" + 
		"	void foo(int[] params) {\n" + 
		"		if (params.length > 0) { // trailing comment formatted in several\n" + 
		"									// lines...\n" + 
		"			// int length = params == null ? : 0 params.length; // this\n" + 
		"			// commented lined does not cause troubles for the formatter when\n" + 
		"			// the comments is not on column 1...\n" + 
		"			for (int i = 0; i < params.length; i++) {\n" + 
		"			}\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug293300_wksp3_01() {
	String source = 
		"package wksp3;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"static String[] constant = {\n" + 
		"// comment\n" + 
		"\"first\",\n" + 
		"// comment\n" + 
		"\"second\",\n" + 
		"};\n" + 
		"}\n";
	formatSource(source,
		"package wksp3;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"	static String[] constant = {\n" + 
		"			// comment\n" + 
		"			\"first\",\n" + 
		"			// comment\n" + 
		"			\"second\", };\n" + 
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

/**
 * @bug 294500: [formatter] MalformedTreeException when formatting an invalid sequence of <code> tags in a javadoc comment
 * @test Verify that no MalformedTreeException occurs while formatting bug test cases
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294500"
 */
public void testBug294500a() {
	String source =
		"package wkps3;\n" + 
		"/**\n" + 
		" * This sample produce an MalformedTreeException\n" + 
		" * when formatted.\n" + 
		" *\n" + 
		" * <p> First paragraph\n" + 
		" * {@link java.lang.String </code>a simple\n" + 
		" * string<code>}.\n" + 
		" *\n" + 
		" * <p> Second paragraph.\n" + 
		" *\n" + 
		" * <p> Third paragraph. </p>\n" + 
		" *\n" + 
		" */\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wkps3;\n" + 
		"\n" + 
		"/**\n" + 
		" * This sample produce an MalformedTreeException when formatted.\n" + 
		" * \n" + 
		" * <p>\n" + 
		" * First paragraph {@link java.lang.String </code>a simple string<code>}.\n" + 
		" * \n" + 
		" * <p>\n" + 
		" * Second paragraph.\n" + 
		" * \n" + 
		" * <p>\n" + 
		" * Third paragraph.\n" + 
		" * </p>\n" + 
		" * \n" + 
		" */\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug294500b() {
	String source =
		"package wkps3;\n" + 
		"/**\n" + 
		" * This sample produce an AIIOBE when formatting.\n" + 
		" *\n" + 
		" * <p> First paragraph\n" + 
		" * {@link java.lang.String </code>a simple\n" + 
		" * string<code>}.\n" + 
		" */\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wkps3;\n" + 
		"\n" + 
		"/**\n" + 
		" * This sample produce an AIIOBE when formatting.\n" + 
		" * \n" + 
		" * <p>\n" + 
		" * First paragraph {@link java.lang.String </code>a simple string<code>}.\n" + 
		" */\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"}\n"
	);
}

/**
 * @bug 294618: [formatter] The formatter takes two passes to format a common sequence of html tags
 * @test Verify that the specific sequence of html tags is well formatted in one pass
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294618"
 */
public void testBug294618a() {
	String source =
		"package wkps3;\n" + 
		"\n" + 
		"/**\n" + 
		" * The formatter was not able to format the current comment:\n" + 
		" * \n" + 
		" * <ol>\n" + 
		" *   <li><p> First item\n" + 
		" *\n" + 
		" *   <li><p> Second item\n" + 
		" *\n" + 
		" *   <li><p> First paragraph of third item\n" + 
		" *\n" + 
		" *   <p> Second paragraph of third item\n" + 
		" *\n" + 
		" *   <blockquote><table cellpadding=0 cellspacing=0 summary=\"layout\">\n" + 
		" *   <tr><td><tt>::255.255.0.d</tt><td></tr>\n" + 
		" *   </table></blockquote>\n" + 
		" *   </li>\n" + 
		" * </ol>\n" + 
		" */\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wkps3;\n" + 
		"\n" + 
		"/**\n" + 
		" * The formatter was not able to format the current comment:\n" + 
		" * \n" + 
		" * <ol>\n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * First item\n" + 
		" * \n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * Second item\n" + 
		" * \n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * First paragraph of third item\n" + 
		" * \n" + 
		" * <p>\n" + 
		" * Second paragraph of third item\n" + 
		" * \n" + 
		" * <blockquote>\n" + 
		" * <table cellpadding=0 cellspacing=0 summary=\"layout\">\n" + 
		" * <tr>\n" + 
		" * <td><tt>::255.255.0.d</tt>\n" + 
		" * <td>\n" + 
		" * </tr>\n" + 
		" * </table>\n" + 
		" * </blockquote></li>\n" + 
		" * </ol>\n" + 
		" */\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug294618b() {
	String source =
		"/**\n" + 
		" * Verify deep html tag nesting:\n" + 
		" * \n" + 
		" * <ol>\n" + 
		" *   <li><p> First item\n" + 
		" *   <li><p> Second item\n" + 
		" *   <ul>\n" + 
		" *     <li><p> First item of second item\n" + 
		" *       <blockquote><table cellpadding=0 cellspacing=0 summary=\"layout\">\n" + 
		" *       <tr><td><tt><i><b>::255.255.0.d</b></i></tt></td></tr>\n" + 
		" *       </table></blockquote>\n" + 
		" *     </li>\n" + 
		" *   </ul>\n" + 
		" *   </li>\n" + 
		" * </ol>\n" + 
		" */\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"/**\n" + 
		" * Verify deep html tag nesting:\n" + 
		" * \n" + 
		" * <ol>\n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * First item\n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * Second item\n" + 
		" * <ul>\n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * First item of second item <blockquote>\n" + 
		" * <table cellpadding=0 cellspacing=0 summary=\"layout\">\n" + 
		" * <tr>\n" + 
		" * <td><tt><i><b>::255.255.0.d</b></i></tt></td>\n" + 
		" * </tr>\n" + 
		" * </table>\n" + 
		" * </blockquote></li>\n" + 
		" * </ul>\n" + 
		" * </li>\n" + 
		" * </ol>\n" + 
		" */\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"}\n"
	);
}

/**
 * @bug 294631: [formatter] The formatter takes two passes to format a common sequence of html tags
 * @test Verify that the specific sequence of html tags is well formatted in one pass
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294631"
 */
public void testBug294631() {
	String source =
		"package wkps3;\n" + 
		"\n" + 
		"/**\n" + 
		" * This comment makes the formatter unstable:\n" + 
		" * \n" + 
		" * <ol>\n" + 
		" *   <li><p> first line\n" + 
		" *   second line</li>\n" + 
		" * </ol>\n" + 
		" */\n" + 
		"public class X {\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wkps3;\n" + 
		"\n" + 
		"/**\n" + 
		" * This comment makes the formatter unstable:\n" + 
		" * \n" + 
		" * <ol>\n" + 
		" * <li>\n" + 
		" * <p>\n" + 
		" * first line second line</li>\n" + 
		" * </ol>\n" + 
		" */\n" + 
		"public class X {\n" + 
		"\n" + 
		"}\n"
	);
}

/**
 * @bug 295175: [formatter] Missing space before a string at the beginning of a javadoc comment
 * @test Verify that space is well inserted before the leading string
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295175"
 */
public void testBug295175a() {
	String source = 
		"public class X {\n" + 
		"/**\n" + 
		" * <p>\n" + 
		" * \"String\", this string may be not well formatted in certain circumstances,\n" + 
		" * typically after bug 294529 has been fixed...\n" + 
		" */\n" + 
		"void foo() {}\n" + 
		"}\n";
	formatSource(source,
		"public class X {\n" + 
		"	/**\n" + 
		"	 * <p>\n" + 
		"	 * \"String\", this string may be not well formatted in certain circumstances,\n" + 
		"	 * typically after bug 294529 has been fixed...\n" + 
		"	 */\n" + 
		"	void foo() {\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug295175b() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public interface X {\n" + 
		"\n" + 
		"    /**\n" + 
		"     * <P>\n" + 
		"     * <BR>\n" + 
		"	 *<B>NOTE</B><BR>\n" + 
		"	 * Formatter can miss a space before the previous B tag...\n" + 
		"     **/\n" + 
		"	void foo();\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public interface X {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * <P>\n" + 
		"	 * <BR>\n" + 
		"	 * <B>NOTE</B><BR>\n" + 
		"	 * Formatter can miss a space before the previous B tag...\n" + 
		"	 **/\n" + 
		"	void foo();\n" + 
		"}\n"
	);
}
public void testBug295175c() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public interface X {\n" + 
		"\n" + 
		"    /**\n" + 
		"     * <P>Following p tag can miss a space before after formatting\n" + 
		"     *<p>\n" + 
		"     * end of comment.\n" + 
		"     **/\n" + 
		"	void foo();\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public interface X {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * <P>\n" + 
		"	 * Following p tag can miss a space before after formatting\n" + 
		"	 * <p>\n" + 
		"	 * end of comment.\n" + 
		"	 **/\n" + 
		"	void foo();\n" + 
		"}\n"
	);
}
public void testBug295175d() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public interface X {\n" + 
		"\n" + 
		"    /**\n" + 
		"     * <p>Following p tag can miss a space before after formatting\n" + 
		"     *\n" + 
		"     *<p>\n" + 
		"     * <BR>\n" + 
		"	 *<B>NOTE</B><BR>\n" + 
		"	 * Formatter can miss a space before the previous B tag...\n" + 
		"     **/\n" + 
		"	void foo();\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public interface X {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * <p>\n" + 
		"	 * Following p tag can miss a space before after formatting\n" + 
		"	 * \n" + 
		"	 * <p>\n" + 
		"	 * <BR>\n" + 
		"	 * <B>NOTE</B><BR>\n" + 
		"	 * Formatter can miss a space before the previous B tag...\n" + 
		"	 **/\n" + 
		"	void foo();\n" + 
		"}\n"
	);
}
public void testBug295175e() {
	String source = 
		"package wksp3;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"    /** \n" + 
		"     * In this peculiar config <code>true</code>, the comment is not___ \n" + 
		"     * really well formatted. The problem is that the first_ code tag\n" + 
		"     * here_______ <code>/*</code> and <code>*&#47;</code> go at the end of the previous line\n" + 
		"     * instead of staying on the 3rd one... \n" + 
		"     */\n" + 
		"    void foo() {}\n" + 
		"}\n";
	formatSource(source,
		"package wksp3;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"	/**\n" + 
		"	 * In this peculiar config <code>true</code>, the comment is not___ really\n" + 
		"	 * well formatted. The problem is that the first_ code tag here_______\n" + 
		"	 * <code>/*</code> and <code>*&#47;</code> go at the end of the previous\n" + 
		"	 * line instead of staying on the 3rd one...\n" + 
		"	 */\n" + 
		"	void foo() {\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug295175f() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Finds the deepest <code>IJavaElement</code> in the hierarchy of\n" + 
		"	 * <code>elt</elt>'s children (including <code>elt</code> itself)\n" + 
		"	 * which has a source range that encloses <code>position</code>\n" + 
		"	 * according to <code>mapper</code>.\n" + 
		"	 */\n" + 
		"	void foo() {}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * Finds the deepest <code>IJavaElement</code> in the hierarchy of\n" + 
		"	 * <code>elt</elt>\'s children (including <code>elt</code> itself) which has\n" + 
		"	 * a source range that encloses <code>position</code> according to\n" + 
		"	 * <code>mapper</code>.\n" + 
		"	 */\n" + 
		"	void foo() {\n" + 
		"	}\n" + 
		"}\n"
	);
}

/**
 * @bug 295238: [formatter] The comment formatter add an unexpected new line in block comment
 * @test Verify that formatting a block comment with a tag does not add an unexpected new line
 * 		when the 'Never join lines' option is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295238"
 */
public void testBug295238() {
	this.formatterPrefs.join_lines_in_comments = false;
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public interface X03 {\n" + 
		"	\n" + 
		"	class Inner {\n" + 
		"		\n" + 
		"		/* (non-Javadoc)\n" + 
		"		 * @see org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.eclipse.jface.text.DocumentCommand)\n" + 
		"		 */\n" + 
		"		protected void foo() {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public interface X03 {\n" + 
		"\n" + 
		"	class Inner {\n" + 
		"\n" + 
		"		/*\n" + 
		"		 * (non-Javadoc)\n" + 
		"		 * \n" + 
		"		 * @see\n" + 
		"		 * org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.eclipse\n" + 
		"		 * .jface.text.DocumentCommand)\n" + 
		"		 */\n" + 
		"		protected void foo() {\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
}
// the following test already passed with v_A21, but failed with first version of the patch
public void testBug295238b1() {
	this.formatterPrefs.join_lines_in_comments = false;
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"/*		if ((operatorSignature & CompareMASK) == (alternateOperatorSignature & CompareMASK)) { // same promotions and result\n" + 
		"			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); \n" + 
		"		}\n" + 
		"*/		\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"		/*\n" + 
		"		 * if ((operatorSignature & CompareMASK) == (alternateOperatorSignature\n" + 
		"		 * & CompareMASK)) { // same promotions and result\n" + 
		"		 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)\n" + 
		"		 * expression, TypeBinding.wellKnownType(scope,\n" + 
		"		 * expression.implicitConversion >> 4));\n" + 
		"		 * }\n" + 
		"		 */\n" + 
		"	}\n" + 
		"}\n"
	);
}
// the following test failed with v_A21 and with the version v00 of the patch
public void testBug295238b2() {
	this.formatterPrefs.join_lines_in_comments = false;
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"/*			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); \n" + 
		"*/		\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"		/*\n" + 
		"		 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)\n" + 
		"		 * expression, TypeBinding.wellKnownType(scope,\n" + 
		"		 * expression.implicitConversion >> 4));\n" + 
		"		 */\n" + 
		"	}\n" + 
		"}\n"
	);
}
// the following test failed with v_A21 and with the version v00 of the patch
public void testBug295238b3() {
	this.formatterPrefs.join_lines_in_comments = false;
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"/*\n" + 
		"			scope.problemReporter().unnecessaryCastForArgument((CastExpression)expression,  TypeBinding.wellKnownType(scope, expression.implicitConversion >> 4)); \n" + 
		"*/		\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void foo() {\n" + 
		"		/*\n" + 
		"		 * scope.problemReporter().unnecessaryCastForArgument((CastExpression)\n" + 
		"		 * expression, TypeBinding.wellKnownType(scope,\n" + 
		"		 * expression.implicitConversion >> 4));\n" + 
		"		 */\n" + 
		"	}\n" + 
		"}\n"
	);
}

/**
 * @bug 264112: [Formatter] Wrap when necessary too aggressive on short qualifiers
 * @test 
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=264112"
 */
// Max line width = 24
public void testBug264112_w24_S1() {
	this.formatterPrefs.page_width = 24;
	String source = 
		"class Sample1 {void foo() {Other.bar( 100,\n" + 
		"200,\n" + 
		"300,\n" + 
		"400,\n" + 
		"500,\n" + 
		"600,\n" + 
		"700,\n" + 
		"800,\n" + 
		"900 );}}\n";
	formatSource(source,
		"class Sample1 {\n" + 
		"	void foo() {\n" + 
		"		Other.bar(100,\n" + 
		"				200,\n" + 
		"				300,\n" + 
		"				400,\n" + 
		"				500,\n" + 
		"				600,\n" + 
		"				700,\n" + 
		"				800,\n" + 
		"				900);\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug264112_w24_S2() {
	this.formatterPrefs.page_width = 24;
	String source = 
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" + 
		"	int foo(Some a) {\n" + 
		"		return a.getFirst();\n" + 
		"	}\n" + 
		"}\n"
	);
}
// Max line width = 25
public void testBug264112_w25_S1() {
	this.formatterPrefs.page_width = 25;
	String source = 
		"class Sample1 {void foo() {Other.bar( 100,\n" + 
		"200,\n" + 
		"300,\n" + 
		"400,\n" + 
		"500,\n" + 
		"600,\n" + 
		"700,\n" + 
		"800,\n" + 
		"900 );}}\n";
	formatSource(source,
		"class Sample1 {\n" + 
		"	void foo() {\n" + 
		"		Other.bar(100,\n" + 
		"				200,\n" + 
		"				300,\n" + 
		"				400,\n" + 
		"				500,\n" + 
		"				600,\n" + 
		"				700,\n" + 
		"				800, 900);\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug264112_w25_S2() {
	this.formatterPrefs.page_width = 25;
	String source = 
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" + 
		"	int foo(Some a) {\n" + 
		"		return a.getFirst();\n" + 
		"	}\n" + 
		"}\n"
	);
}
// Max line width = 26
public void testBug264112_w26_S1() {
	this.formatterPrefs.page_width = 26;
	String source = 
		"class Sample1 {void foo() {Other.bar( 100,\n" + 
		"200,\n" + 
		"300,\n" + 
		"400,\n" + 
		"500,\n" + 
		"600,\n" + 
		"700,\n" + 
		"800,\n" + 
		"900 );}}\n";
	formatSource(source,
		"class Sample1 {\n" + 
		"	void foo() {\n" + 
		"		Other.bar(100,\n" + 
		"				200, 300,\n" + 
		"				400, 500,\n" + 
		"				600, 700,\n" + 
		"				800, 900);\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug264112_w26_S2() {
	this.formatterPrefs.page_width = 26;
	String source = 
		"class Sample2 {int foo(Some a) {return a.getFirst();}}\n";
	formatSource(source,
		"class Sample2 {\n" + 
		"	int foo(Some a) {\n" + 
		"		return a.getFirst();\n" + 
		"	}\n" + 
		"}\n"
	);
}
public void testBug264112_wksp1_01() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	public Object foo(Object scope) {\n" + 
		"		if (scope != null) {\n" + 
		"			if (true) {\n" + 
		"				for (int i = 0; i < 10; i++) {\n" + 
		"					if (i == 0) {\n" + 
		"					} else if (i < 5) {\n" + 
		"					} else {\n" + 
		"						scope.problemReporter().typeMismatchErrorActualTypeExpectedType(expression, expressionTb, expectedElementsTb);\n" + 
		"						return null;\n" + 
		"					}\n" + 
		"				}\n" + 
		"			}\n" + 
		"			return null;\n" + 
		"		}\n" + 
		"	}\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	public Object foo(Object scope) {\n" + 
		"		if (scope != null) {\n" + 
		"			if (true) {\n" + 
		"				for (int i = 0; i < 10; i++) {\n" + 
		"					if (i == 0) {\n" + 
		"					} else if (i < 5) {\n" + 
		"					} else {\n" + 
		"						scope.problemReporter()\n" + 
		"								.typeMismatchErrorActualTypeExpectedType(\n" + 
		"										expression, expressionTb,\n" + 
		"										expectedElementsTb);\n" + 
		"						return null;\n" + 
		"					}\n" + 
		"				}\n" + 
		"			}\n" + 
		"			return null;\n" + 
		"		}\n" + 
		"	}\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug264112_wksp1_02() {
	String source = 
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	public String toString() {\n" + 
		"		StringBuffer buffer = new StringBuffer();\n" + 
		"		if (true) {\n" + 
		"			buffer.append(\"- possible values:	[\"); //$NON-NLS-1$ \n" + 
		"			buffer.append(\"]\\n\"); //$NON-NLS-1$ \n" + 
		"			buffer.append(\"- curr. val. index:	\").append(currentValueIndex).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" + 
		"		}\n" + 
		"		buffer.append(\"- description:		\").append(description).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" + 
		"		return buffer.toString();\n" + 
		"	}\n" + 
		"\n" + 
		"}\n";
	formatSource(source,
		"package wksp1;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	public String toString() {\n" + 
		"		StringBuffer buffer = new StringBuffer();\n" + 
		"		if (true) {\n" + 
		"			buffer.append(\"- possible values:	[\"); //$NON-NLS-1$ \n" + 
		"			buffer.append(\"]\\n\"); //$NON-NLS-1$ \n" + 
		"			buffer.append(\"- curr. val. index:	\").append(currentValueIndex).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" + 
		"		}\n" + 
		"		buffer.append(\"- description:		\").append(description).append(\"\\n\"); //$NON-NLS-1$ //$NON-NLS-2$\n" + 
		"		return buffer.toString();\n" + 
		"	}\n" + 
		"\n" + 
		"}\n"
	);
}
public void testBug264112_wksp2_01() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"    private static final String PATH_SMOOTH_QUAD_TO = \"SMOOTH\";\n" + 
		"    private static final String XML_SPACE = \" \";\n" + 
		"    private static final String PATH_CLOSE = \"CLOSE\";\n" + 
		"\n" + 
		"	String foo(Point point, Point point_plus1) {\n" + 
		"        StringBuffer sb = new StringBuffer();\n" + 
		"        while (true) {\n" + 
		"            if (point != null) {\n" + 
		"                // Following message send was unnecessarily split\n" + 
		"                sb.append(PATH_SMOOTH_QUAD_TO)\n" + 
		"                .append(String.valueOf(midValue(point.x, point_plus1.x)))\n" + 
		"                .append(XML_SPACE)\n" + 
		"                .append(String.valueOf(midValue(point.y, point_plus1.y)));\n" + 
		"            } else {\n" + 
		"                break;\n" + 
		"            }\n" + 
		"        }\n" + 
		"        sb.append(PATH_CLOSE);\n" + 
		"\n" + 
		"        return sb.toString();\n" + 
		"    }\n" + 
		"\n" + 
		"    private int midValue(int x1, int x2) {\n" + 
		"        return (x1 + x2) / 2;\n" + 
		"    }\n" + 
		"\n" + 
		"}\n" + 
		"class Point {\n" + 
		"	int x,y;\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X01 {\n" + 
		"\n" + 
		"	private static final String PATH_SMOOTH_QUAD_TO = \"SMOOTH\";\n" + 
		"	private static final String XML_SPACE = \" \";\n" + 
		"	private static final String PATH_CLOSE = \"CLOSE\";\n" + 
		"\n" + 
		"	String foo(Point point, Point point_plus1) {\n" + 
		"		StringBuffer sb = new StringBuffer();\n" + 
		"		while (true) {\n" + 
		"			if (point != null) {\n" + 
		"				// Following message send was unnecessarily split\n" + 
		"				sb.append(PATH_SMOOTH_QUAD_TO)\n" + 
		"						.append(\n" + 
		"								String.valueOf(midValue(point.x, point_plus1.x)))\n" + 
		"						.append(XML_SPACE)\n" + 
		"						.append(\n" + 
		"								String.valueOf(midValue(point.y, point_plus1.y)));\n" + 
		"			} else {\n" + 
		"				break;\n" + 
		"			}\n" + 
		"		}\n" + 
		"		sb.append(PATH_CLOSE);\n" + 
		"\n" + 
		"		return sb.toString();\n" + 
		"	}\n" + 
		"\n" + 
		"	private int midValue(int x1, int x2) {\n" + 
		"		return (x1 + x2) / 2;\n" + 
		"	}\n" + 
		"\n" + 
		"}\n" + 
		"\n" + 
		"class Point {\n" + 
		"	int x, y;\n" + 
		"}\n"
	);
}
public void testBug264112_wksp2_02() {
	String source = 
		"package wksp2;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"	\n" + 
		"	void test(X02 indexsc) {\n" + 
		"		if (indexsc == null) {\n" + 
		"		} else {\n" + 
		"\n" + 
		"			indexsc.reopenScan(\n" + 
		"						searchRow,                      	// startKeyValue\n" + 
		"						ScanController.GE,            		// startSearchOp\n" + 
		"						null,                         		// qualifier\n" + 
		"						null, 		                        // stopKeyValue\n" + 
		"						ScanController.GT             		// stopSearchOp \n" + 
		"						);\n" + 
		"		}\n" + 
		"		\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source,
		"package wksp2;\n" + 
		"\n" + 
		"public class X02 {\n" + 
		"\n" + 
		"	void test(X02 indexsc) {\n" + 
		"		if (indexsc == null) {\n" + 
		"		} else {\n" + 
		"\n" + 
		"			indexsc.reopenScan(searchRow, // startKeyValue\n" + 
		"					ScanController.GE, // startSearchOp\n" + 
		"					null, // qualifier\n" + 
		"					null, // stopKeyValue\n" + 
		"					ScanController.GT // stopSearchOp\n" + 
		"					);\n" + 
		"		}\n" + 
		"\n" + 
		"	}\n" + 
		"}\n"
	);
}

/**
 * @bug 297225: [formatter] Indentation may be still wrong in certain circumstances after formatting
 * @test Verify that comment indentation is correct when there's a mix of tab and spaces in
 * 		existing indentation and all comments formatting is off.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297225"
 */
public void testBug297225() {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_javadoc_comment = false;
	String source = 
		"public class X01 {\n" + 
		"   	\n" + 
		"   	/**\n" + 
		"   	 * The foo method\n" + 
		"   	 */\n" + 
		"	void foo() {}\n" + 
		"}\n";
	formatSource(source,
		"public class X01 {\n" + 
		"\n" + 
		"	/**\n" + 
		"	 * The foo method\n" + 
		"	 */\n" + 
		"	void foo() {\n" + 
		"	}\n" + 
		"}\n"
	);
}

/**
 * @bug 297546: [formatter] Formatter removes blank after @see if reference is wrapped
 * @test Verify that space after the @see tag is not removed while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297546"
 */
public void testBug297546() {
	String source = 
		"package org.eclipse.jdt.core;\n" + 
		"public class TestClass implements TestInterface {\n" + 
		"\n" + 
		"	/* (non-Javadoc)\n" + 
		"	 * @see org.eclipse.jdt.core.TestInterface#testMethod(org.eclipse.jdt.core.TestInterface)\n" + 
		"	 */\n" + 
		"	public void testMethod(TestInterface aLongNameForAParam) {\n" + 
		"		// do nothing\n" + 
		"	}\n" + 
		"\n" + 
		"	\n" + 
		"}\n" + 
		"interface TestInterface {\n" + 
		"	void testMethod(TestInterface aLongNameForAParam);\n" + 
		"}\n";
	formatSource(source,
		"package org.eclipse.jdt.core;\n" + 
		"\n" + 
		"public class TestClass implements TestInterface {\n" + 
		"\n" + 
		"	/*\n" + 
		"	 * (non-Javadoc)\n" + 
		"	 * \n" + 
		"	 * @see org.eclipse.jdt.core.TestInterface#testMethod(org.eclipse.jdt.core.\n" + 
		"	 * TestInterface)\n" + 
		"	 */\n" + 
		"	public void testMethod(TestInterface aLongNameForAParam) {\n" + 
		"		// do nothing\n" + 
		"	}\n" + 
		"\n" + 
		"}\n" + 
		"\n" + 
		"interface TestInterface {\n" + 
		"	void testMethod(TestInterface aLongNameForAParam);\n" + 
		"}\n"
	);
}

/**
 * @bug 298243: [formatter] Removing empty lines between import groups
 * @test Verify that space after the @see tag is not removed while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=298243"
 */
public void testBug298243() {
	this.formatterPrefs.number_of_empty_lines_to_preserve = 0;
	String source = 
		"package test;\n" + 
		"\n" + 
		"import java.util.concurrent.atomic.AtomicInteger;\n" + 
		"\n" + 
		"import org.xml.sax.SAXException;\n" + 
		"\n" + 
		"public class Test {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		SAXException e;\n" + 
		"		AtomicInteger w;\n" + 
		"	}\n" + 
		"}\n";
	formatSource(source);
}
}
