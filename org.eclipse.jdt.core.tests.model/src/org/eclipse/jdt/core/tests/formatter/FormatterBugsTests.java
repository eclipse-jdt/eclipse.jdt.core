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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

import junit.framework.Test;

public class FormatterBugsTests extends FormatterRegressionTests {

public static Test suite() {
	return buildModelTestSuite(FormatterBugsTests.class);
}

public FormatterBugsTests(String name) {
	super(name);
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
		"					+ \"from y \"\n" + 
		"					+ \"where z=a\";\n" + 
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
		"                 + \"from y \"\n" + 
		"                 + \"where z=a\";\n" + 
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
		"			+ \"from X\";\n" + 
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
		"                 + \"from X\";\n" + 
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
		"		+ \" nommbr=\'\" + getName() + \"\'\"\n" + 
		"		+ \" nomgrp=\'\" + getService().getArgtbl() + \"\'\"\n" + 
		"		+ \" typmbr=\'\" + getMemberType().getArgument() + \"\'\"\n" + 
		"		+ \" srcpat=\'\" + getPhysicalPath() + \"\'\"\n" + 
		"		+ \" nommdl=\'\" + getModel() + \"\'\";\n" + 
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
		"             + \" nommbr=\'\"+getName()+\"\'\"\n" + 
		"             + \" nomgrp=\'\"+getService().getArgtbl()+\"\'\"\n" + 
		"             + \" typmbr=\'\"+getMemberType().getArgument()+\"\'\"\n" + 
		"             + \" srcpat=\'\"+getPhysicalPath()+\"\'\"\n" + 
		"             + \" nommdl=\'\"+getModel()+\"\'\"\n" + 
		"        ;\n" + 
		"}\n" + 
		"}\n";
	formatSource(source,
		"public class Test {\n" + 
		"\n" + 
		"    public String toString() {\n" + 
		"        return \"YAD01: \"\n" + 
		"             + \" nommbr=\'\" + getName() + \"\'\"\n" + 
		"             + \" nomgrp=\'\" + getService().getArgtbl() + \"\'\"\n" + 
		"             + \" typmbr=\'\" + getMemberType().getArgument() + \"\'\"\n" + 
		"             + \" srcpat=\'\" + getPhysicalPath() + \"\'\"\n" + 
		"             + \" nommdl=\'\" + getModel() + \"\'\";\n" + 
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
		"			\"select * \" +\n" + 
		"			\"from person p, address a \" +\n" + 
		"			\"where p.person_id = a.person_id \" +\n" + 
		"			\"and p.person_id = ?\";\n" + 
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
		"			(d == e) &&\n" + 
		"			(f == g && h == i)) {\n" + 
		"		}\n" + 
		"	}\n" + 
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
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\"yz\");\n" + 
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
	formatSource(source);
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
	formatSource(source);
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
		"		.append(\"pqr\").append(\"stu\")\n" + 
		"				.append(\"vwx\").append(\n" + 
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
		"		.append(\"ghi\").append(\"jkl\")\n" + 
		"				.append(\"mno\")\n" + 
		"		.append(\"pqr\").append(\"stu\")\n" + 
		"				.append(\"vwx\").append(\n" + 
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
		"		.append(\"ghi\").append(\"jkl\")\n" + 
		"				.append(\"mno\")\n" + 
		"		.append(\"pqr\").append(\"stu\")\n" + 
		"				.append(\"vwx\")\n" + 
		"		.append(\"yz\");\n" + 
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
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\n" + 
		"				\"yz\");\n" + 
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
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" + 
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\").append(\n" + 
		"				\"yz\");\n" + 
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
		"		.append(\"ghi\").append(\"jkl\").append(\"mno\")\n" + 
		"		.append(\"pqr\").append(\"stu\").append(\"vwx\")\n" + 
		"		.append(\"yz\");\n" + 
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
 * @bug 293240:  [formatter] 'insert_space_before_opening_brace_in_array_initializer' preference may be reset in certain circumstances
 * @test Verify that a realigned annotation keep the 'insert_space_before_opening_brace_in_array_initializer'
 * 		preference initial value.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293240"
 */
public void testBug293240() throws JavaModelException {
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

}
