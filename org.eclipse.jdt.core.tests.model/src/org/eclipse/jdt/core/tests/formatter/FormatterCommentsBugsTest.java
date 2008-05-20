/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;

import junit.framework.Test;

public class FormatterCommentsBugsTest extends FormatterCommentsTests {

	private static final IPath OUTPUT_FOLDER = new Path("out");

public static Test suite() {
	return buildModelTestSuite(FormatterCommentsBugsTest.class);
}

public FormatterCommentsBugsTest(String name) {
    super(name);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterCommentsTests#getOutputFolder()
 */
IPath getOutputFolder() {
	return OUTPUT_FOLDER;
}

/**
 * @bug 228652: [formatter] New line inserted while formatting a region of a compilation unit.
 * @test Insure that no new line is inserted before the formatted region
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228652"
 */
// TODO (frederic) See https://bugs.eclipse.org/bugs/show_bug.cgi?id=49187
public void _testBug228652() {
	String input =
			"package a;\r\n" + 
			"\r\n" + 
			"public class Test {\r\n" + 
			"\r\n" + 
			"	private int field;\r\n" + 
			"	\r\n" + 
			"	/**\r\n" + 
			"	 * fds \r\n" + 
			"	 */\r\n" + 
			"	public void foo() {\r\n" + 
			"	}\r\n" + 
			"}";

	String expected =
			"package a;\r\n" + 
			"\r\n" + 
			"public class Test {\r\n" + 
			"\r\n" + 
			"	private int field;\r\n" + 
			"	\r\n" + 
			"	/**\r\n" + 
			"	 * fds\r\n" + 
			"	 */\r\n" + 
			"	public void foo() {\r\n" + 
			"	}\r\n" + 
			"}";
	
	formatSource(input, expected, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, false, 62, 19, null);
}

/**
 * @bug 231263: [formatter] New JavaDoc formatter wrongly indent tags description
 * @test Insure that new formatter indent tags description as the old one
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231263"
 */
public void testBug231263() throws JavaModelException {
	formatUnit("bugs.b231263", "BadFormattingSample.java");
}
public void testBug231263a() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b231263", "X.java");
}

/**
 * @bug 231297: [formatter] New JavaDoc formatter wrongly split inline tags before reference
 * @test Insure that new formatter do not split reference in inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297"
 */
public void testBug231297() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b231297", "X.java");
}
public void testBug231297a() throws JavaModelException {
	this.preferences.comment_line_length = 30;
	formatUnit("bugs.b231297", "X01.java");
}
public void testBug231297b() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b231297", "X02.java");
}
public void testBug231297c() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b231297", "X03.java");
}
public void testBug231297d() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b231297", "X03b.java");
}

/**
 * @bug 232285: [formatter] New comment formatter wrongly formats javadoc header/footer with several contiguous stars
 * @test Insure that new formatter do not add/remove stars in header and footer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232285"
 */
public void testBug232285a() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01.java");
}
public void testBug232285b() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01b.java");
}
public void testBug232285c() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01c.java");
}
public void testBug232285d() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01d.java");
}
public void testBug232285e() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01e.java");
}
public void testBug232285f() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01f.java");
}
public void testBug232285g() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X02.java");
}
public void testBug232285h() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X03.java");
}
public void testBug232285i() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X04.java");
}
public void testBug232285j() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232285", "X04b.java");
}

/**
 * @bug 232488: [formatter] Code formatter scrambles JavaDoc of Generics
 * @test Insure that comment formatter format properly generic param tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232488"
 */
public void testBug232488() throws JavaModelException {
	this.preferences.comment_line_length = 40;
	formatUnit("bugs.b232488", "X01.java");
}
}
