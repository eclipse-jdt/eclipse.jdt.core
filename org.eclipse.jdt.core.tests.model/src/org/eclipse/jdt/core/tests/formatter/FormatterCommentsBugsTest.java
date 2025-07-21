/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Robin Stocker - Bug 49619 - [formatting] comment formatter leaves whitespace in comments
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Bad line breaking in Eclipse javadoc comments - https://bugs.eclipse.org/348338
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.Hashtable;
import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

public class FormatterCommentsBugsTest extends FormatterCommentsTests {

	private static final IPath OUTPUT_FOLDER = new Path("out");

public static Test suite() {
	return buildModelTestSuite(FormatterCommentsBugsTest.class);
}
static {
	//TESTS_NAMES = new String[] { "testBug287833b" } ;
}
public FormatterCommentsBugsTest(String name) {
    super(name);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterCommentsTests#getOutputFolder()
 */
@Override
IPath getOutputFolder() {
	return OUTPUT_FOLDER;
}

/**
 * bug 196308: [formatter] Don't escape entity when formatting in {@code <pre>} tags within javadoc comments
 * test Ensure that entity are not escaped when formatting in {@code <pre>} tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=196308"
 */
public void testBug196308() throws JavaModelException {
	String source =
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * &at;MyAnnotation\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * &at;MyAnnotation\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=204257
public void testBug196308b() throws JavaModelException {
	String source =
		"public class A\n" +
		"{\n" +
		"  /**\n" +
		"   * <pre>\n" +
		"   *   &#92;u\n" +
		"   * </pre>\n" +
		"   */\n" +
		"  public void a()\n" +
		"  {\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"public class A {\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 *   &#92;u\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public void a() {\n" +
		"	}\n" +
		"}\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=238547
public void testBug196308c() throws JavaModelException {
	String source =
		"/**\n" +
		" * &#x01;&#x20;&#x21;&#x40;&#x41;&#233;\n" +
		" * <pre>&#x01;&#x20;&#x21;&#x40;&#x41;&#233;</pre>\n" +
		" */\n" +
		"public class TestClass {}\n";
	formatSource(source,
		"/**\n" +
		" * &#x01;&#x20;&#x21;&#x40;&#x41;&#233;\n" +
		" * \n" +
		" * <pre>\n" +
		" * &#x01;&#x20;&#x21;&#x40;&#x41;&#233;\n" +
		" * </pre>\n" +
		" */\n" +
		"public class TestClass {\n" +
		"}\n"
	);
}

/**
 * bug 198963: [formatter] 3.3 Code Formatter repeatedly indents block comment
 * test Ensure that no the formatter indents the block comment only once
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=198963"
 */
public void testBug198963_Tabs01() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 0; /*\n" +
		"				* XXXX\n" +
		"				*/\n" +
		"}"
	);
}
public void testBug198963_Tabs02() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 10; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 10; /*\n" +
		"				* XXXX\n" +
		"				*/\n" +
		"}"
	);
}
public void testBug198963_Tabs03() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 100; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 100; /*\n" +
		"					* XXXX\n" +
		"					*/\n" +
		"}"
	);
}
public void testBug198963_Tabs04() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"                      * XXXX\n" +
		"                        */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 0; /*\n" +
		"				       * XXXX\n" +
		"				         */\n" +
		"}"
	);
}
public void testBug198963_Tabs05() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"public class Test {\n" +
		"\n" +
		"        /*\n" +
		"             * XXXX\n" +
		"               */\n" +
		"    int x = 0;\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	     * XXXX\n" +
		"	       */\n" +
		"	int x = 0;\n" +
		"}"
	);
}
public void testBug198963_Tabs06() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.TAB;
	String source =
		"public class Test {\n" +
		"\n" +
		"            /*\n" +
		"         * XXXX\n" +
		"       */\n" +
		"    int x = 0;\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	* XXXX\n" +
		"	*/\n" +
		"	int x = 0;\n" +
		"}"
	);
}
public void testBug198963_Spaces01() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"               * XXXX\n" +
		"               */\n" +
		"}"
	);
}
public void testBug198963_Spaces02() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 10; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    int x = 10; /*\n" +
		"                * XXXX\n" +
		"                */\n" +
		"}"
	);
}
public void testBug198963_Spaces03() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 100; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    int x = 100; /*\n" +
		"                 * XXXX\n" +
		"                 */\n" +
		"}"
	);
}
public void testBug198963_Spaces04() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"                      * XXXX\n" +
		"                        */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"                      * XXXX\n" +
		"                        */\n" +
		"}"
	);
}
public void testBug198963_Spaces05() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"        /*\n" +
		"             * XXXX\n" +
		"               */\n" +
		"    int x = 0;\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    /*\n" +
		"         * XXXX\n" +
		"           */\n" +
		"    int x = 0;\n" +
		"}"
	);
}
public void testBug198963_Spaces06() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	String source =
		"public class Test {\n" +
		"\n" +
		"            /*\n" +
		"         * XXXX\n" +
		"       */\n" +
		"    int x = 0;\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"    /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"    int x = 0;\n" +
		"}"
	);
}
public void testBug198963_Mixed01() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 0; /*\n" +
		"			   * XXXX\n" +
		"			   */\n" +
		"}"
	);
}
public void testBug198963_Mixed02() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 10; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 10; /*\n" +
		"				* XXXX\n" +
		"				*/\n" +
		"}"
	);
}
public void testBug198963_Mixed03() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 100; /*\n" +
		"    * XXXX\n" +
		"    */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 100; /*\n" +
		"				 * XXXX\n" +
		"				 */\n" +
		"}"
	);
}
public void testBug198963_Mixed04() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"public class Test {\n" +
		"\n" +
		"    int x = 0; /*\n" +
		"                      * XXXX\n" +
		"                        */\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	int x = 0; /*\n" +
		"			          * XXXX\n" +
		"			            */\n" +
		"}"
	);
}
public void testBug198963_Mixed05() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"public class Test {\n" +
		"\n" +
		"        /*\n" +
		"             * XXXX\n" +
		"               */\n" +
		"    int x = 0;\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	     * XXXX\n" +
		"	       */\n" +
		"	int x = 0;\n" +
		"}"
	);
}
public void testBug198963_Mixed06() {
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	String source =
		"public class Test {\n" +
		"\n" +
		"            /*\n" +
		"         * XXXX\n" +
		"       */\n" +
		"    int x = 0;\n" +
		"}";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	* XXXX\n" +
		"	*/\n" +
		"	int x = 0;\n" +
		"}"
	);
}

/**
 * bug 204091: [formatter] format region in comment introduces comment start/end tokens
 * test Ensure that a region inside a javadoc comment is well formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=204091"
 */
// NOT_FIXED_YET
public void _testBug204091() {
	String source =
		"public class Test {\r\n" +
		"	/**\r\n" +
		"	 * Don't format this:\r\n" +
		"	 *    it has been formatted by the user!\r\n" +
		"	 * \r\n" +
		"	 * [#@param    param   format   this comment    #]\r\n" +
		"	 */\r\n" +
		"	public void foo() {\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
		"public class Test {\r\n" +
		"	/**\r\n" +
		"	 * Don't format this:\r\n" +
		"	 *    it has been formatted by the user!\r\n" +
		"	 * \r\n" +
		"	 * @param param\n" +
		"	 *            format this comment\n" +
		"	 */\r\n" +
		"	public void foo() {\r\n" +
		"	}\r\n" +
		"}"
	);
}

/**
 * bug 217108: [formatter] deletes blank lines between comments
 * test Ensure that blank lines are preserved
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=217108"
 */
public void testBug217108a() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    /* a */\n" +
		"    // b\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/* a */\n" +
		"	// b\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108b() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    /* a */\n" +
		"\n" +
		"    // b\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/* a */\n" +
		"\n" +
		"	// b\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108c() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    // b\n" +
		"    /* a */\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	// b\n" +
		"	/* a */\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108d() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    // b\n" +
		"\n" +
		"    /* a */\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	// b\n" +
		"\n" +
		"	/* a */\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108e() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    // a\n" +
		"\n" +
		"    // b\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	// a\n" +
		"\n" +
		"	// b\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108f() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    // a\n" +
		"    // b\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	// a\n" +
		"	// b\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108g() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    /** a */\n" +
		"    // b\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/** a */\n" +
		"	// b\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}
public void testBug217108h() {
	String source =
		"public class Test {\n" +
		"\n" +
		"    /** a */\n" +
		"\n" +
		"    // b\n" +
		"\n" +
		"    int i;\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/** a */\n" +
		"\n" +
		"	// b\n" +
		"\n" +
		"	int i;\n" +
		"\n" +
		"}\n"
	);
}

/**
 * bug 228652: [formatter] New line inserted while formatting a region of a compilation unit.
 * test Ensure that no new line is inserted before the formatted region
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228652"
 */
public void testBug228652() {
	String source =
		"package a;\r\n" +
		"\r\n" +
		"public class Test {\r\n" +
		"\r\n" +
		"	private int field;\r\n" +
		"	\r\n" +
		"	[#/**\r\n" +
		"	 * fds \r\n" +
		"	 */#]\r\n" +
		"	public void foo() {\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source,
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
		"}"
	);
}

/**
 * bug 230944: [formatter] Formatter does not respect /*-
 * test Ensure that new formatter does not format block comment starting with '/*-'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=230944"
 */
public void testBug230944a() throws JavaModelException {
	formatUnit("bugs.b230944", "X01.java");
}
public void testBug230944b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b230944", "X02.java");
}

/**
 * bug 231263: [formatter] New JavaDoc formatter wrongly indent tags description
 * test Ensure that new formatter indent tags description as the old one
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231263"
 */
public void testBug231263() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("bugs.b231263", "BadFormattingSample.java");
}
public void testBug231263a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	useOldJavadocTagsFormatting();
	formatUnit("bugs.b231263", "X.java");
}

/**
 * bug 231297: [formatter] New JavaDoc formatter wrongly split inline tags before reference
 * test Ensure that new formatter do not split reference in inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297"
 */
public void testBug231297() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X.java");
}
public void testBug231297a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 30;
	formatUnit("bugs.b231297", "X01.java");
}
public void testBug231297b() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X02.java");
}
public void testBug231297c() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X03.java");
}
public void testBug231297d() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed non formatted inline tag description
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b231297", "X03b.java");
}

/**
 * bug 232285: [formatter] New comment formatter wrongly formats javadoc header/footer with several contiguous stars
 * test Ensure that new formatter do not add/remove stars in header and footer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232285"
 */
public void testBug232285a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01.java");
}
public void testBug232285b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01b.java");
}
public void testBug232285c() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01c.java");
}
public void testBug232285d() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01d.java");
}
public void testBug232285e() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01e.java");
}
public void testBug232285f() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X01f.java");
}
public void testBug232285g() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X02.java");
}
public void testBug232285h() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X03.java");
}
public void testBug232285i() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X04.java");
}
public void testBug232285j() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232285", "X04b.java");
}

/**
 * bug 232488: [formatter] Code formatter scrambles JavaDoc of Generics
 * test Ensure that comment formatter format properly generic param tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232488"
 */
public void testBug232488() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	useOldJavadocTagsFormatting();
	formatUnit("bugs.b232488", "X01.java");
}

/**
 * bug 232466: [formatter] References of inlined tags are still split in certain circumstances
 * test Ensure that new formatter do not add/remove stars in header and footer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232466"
 */
public void testBug232466a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b232466", "X01.java");
}
public void testBug232466b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	setPageWidth80();
	formatUnit("bugs.b232466", "X02.java");
}

/**
 * bug 232768: [formatter] does not format block and single line comment if too much selected
 * test Ensure that the new comment formatter formats comments touched by the selection
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232768"
 */
public void testBug232768a() throws JavaModelException {
	String source = "public class A {\r\n" +
			"[#\r\n" +
			"        /*\r\n" +
			"         * A block comment \r\n" +
			"         * on two lines\r\n" +
			"         */\r\n" +
			"\r\n#]" +
			"}\r\n" +
			"";
	formatSource(source,
		"public class A {\n" +
		"\n" +
		"	/*\n" +
		"	 * A block comment on two lines\n" +
		"	 */\n" +
		"\n" +
		"}\n"
	);
}
public void testBug232768b() throws JavaModelException {
	String source = "public class B {\r\n" +
			"[#\r\n" +
			"        public void \r\n" +
			"        foo() {}\r\n" +
			"#]\r\n" +
			"        /*\r\n" +
			"         * A block comment \r\n" +
			"         * on two lines\r\n" +
			"         */\r\n" +
			"\r\n" +
			"}\r\n";
	formatSource(source,
		"public class B {\n" +
		"\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"\n" +
		"        /*\r\n" +
		"         * A block comment \n" +
		"         * on two lines\n" +
		"         */\n" +
		"\n" +
		"}\n"
	);
}
public void testBug232768_Javadoc01() throws JavaModelException {
	// Selection starts before and ends after the javadoc comment
	String source = "public class C {\n" +
		"	\n" +
		"[#        /**\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"#]\n" +
		"\n" +
		"}";
	formatSource(source,
		"public class C {\n" +
		"	\n" +
		"	/**\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"	void m1() {\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Javadoc02() throws JavaModelException {
	// Selection starts at javadoc comment begin and ends after it
	String source = "public class C {\n" +
		"	\n" +
		"        [#/**\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m1  (   )   {\n" +
		"	#]\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"public class C {\n" +
		"	\n" +
		"        /**\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"	void m1() {\n" +
		"\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Javadoc03() throws JavaModelException {
	// Selection starts inside the javadoc comment and ends after it
	String source = "public class C {\n" +
		"	\n" +
		"        /**\n" +
		"         * a\n" +
		"         * b\n" +
		"         * [#c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		#]m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"public class C {\n" +
		"	\n" +
		"        /**\n" +
		"		 * a b c d .\n" +
		"		 */\n" +
		"		void m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Javadoc04() throws JavaModelException {
	// Selection starts before the javadoc comment and ends at its end
	String source = "public class C {\n" +
		"[#	\n" +
		"        /**\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */#]\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	formatSource(source,
		"public class C {\n" +
		"\n" +
		"	/**\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Javadoc05() throws JavaModelException {
	// Selection starts before the javadoc comment and ends inside it
	String source = "[#   public     class			C{\n" +
		"	\n" +
		"        /**\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c#]\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"public class C {\n" +
		"\n" +
		"	/**\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Javadoc06() throws JavaModelException {
	// Selection starts and ends inside the javadoc comment
	String source = "   public     class			C{    \n" +
		"	\n" +
		"        /**\n" +
		"         * a\n" +
		"         * b\n" +
		"         * [#c#]\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"   public     class			C{    \n" +
		"	\n" +
		"        /**\n" +
		"		 * a b c d .\n" +
		"		 */\n" +
		"        void		m1  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Block01() throws JavaModelException {
	// Selection starts before and ends after the block comment
	String source = "public class D {\n" +
		"	\n" +
		"[#        /*\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"#]\n" +
		"\n" +
		"}";
	formatSource(source,
		"public class D {\n" +
		"	\n" +
		"	/*\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"	void m2() {\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Block02() throws JavaModelException {
	// Selection starts at block comment begin and ends after it
	String source = "public class D {\n" +
		"	\n" +
		"        [#/*\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m2  (   )   {\n" +
		"	#]\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"public class D {\n" +
		"	\n" +
		"        /*\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"	void m2() {\n" +
		"\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Block03() throws JavaModelException {
	// Selection starts inside the block comment and ends after it
	String source = "public class D {\n" +
		"	\n" +
		"        /*\n" +
		"         * a\n" +
		"         * b\n" +
		"         * [#c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		#]m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"public class D {\n" +
		"	\n" +
		"        /*\n" +
		"		 * a b c d .\n" +
		"		 */\n" +
		"		void m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Block04() throws JavaModelException {
	// Selection starts before the block comment and ends at its end
	String source = "public class D {\n" +
		"[#	\n" +
		"        /*\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c\n" +
		"         * d\n" +
		"         * .\n" +
		"         */#]\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	formatSource(source,
		"public class D {\n" +
		"\n" +
		"	/*\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Block05() throws JavaModelException {
	// Selection starts before the block comment and ends inside it
	String source = "[#   public     class			D{\n" +
		"	\n" +
		"        /*\n" +
		"         * a\n" +
		"         * b\n" +
		"         * c#]\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"public class D {\n" +
		"\n" +
		"	/*\n" +
		"	 * a b c d .\n" +
		"	 */\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Block06() throws JavaModelException {
	// Selection starts and ends inside the block comment
	String source = "   public     class			D{    \n" +
		"	\n" +
		"        /*\n" +
		"         * a\n" +
		"         * b\n" +
		"         * [#c#]\n" +
		"         * d\n" +
		"         * .\n" +
		"         */\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}";
	// Note that the incorrect indentation before the javadoc is fixed in this test case...
	// This is due to the fact that the region is adapted to include the edit just before the comment
	formatSource(source,
		"   public     class			D{    \n" +
		"	\n" +
		"        /*\n" +
		"		 * a b c d .\n" +
		"		 */\n" +
		"        void		m2  (   )   {\n" +
		"	\n" +
		"        }     \n" +
		"\n" +
		"\n" +
		"}"
	);
}
public void testBug232768_Line01() throws JavaModelException {
	// Selection starts before and ends after the line comment
	String source = "public class E {\n" +
		"	\n" +
		"\n" +
		"[#        void            m3()         { // this        is        a    bug\n" +
		"\n" +
		"        }\n" +
		"#]   \n" +
		"}";
	formatSource(source,
		"public class E {\n" +
		"	\n" +
		"\n" +
		"	void m3() { // this is a bug\n" +
		"\n" +
		"	}\n" +
		"   \n" +
		"}"
	);
}
public void testBug232768_Line02() throws JavaModelException {
	// Selection starts at line comment begin and ends after it
	String source = "public class E {\n" +
		"	\n" +
		"\n" +
		"        void            m3()         { [#// this        is        a    bug\n" +
		"\n#]" +
		"        }\n" +
		"   \n" +
		"}";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class E {\n" +
		"	\n" +
		"\n" +
		"        void            m3()         { // this is a bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"}"
	);
}
public void testBug232768_Line03() throws JavaModelException {
	// Selection starts inside line comment and ends after it
	String source = "public class E {\n" +
		"	\n" +
		"\n" +
		"        void            m3()         { // this        [#is        a    bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"              }#]";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class E {\n" +
		"	\n" +
		"\n" +
		"        void            m3()         { // this        is a bug\n" +
		"\n" +
		"		}\n" +
		"\n" +
		"	}"
	);
}
public void testBug232768_Line04() throws JavaModelException {
	// Selection starts before the line comment and ends at its end
	String source = "public class E {[#       \n" +
		"	\n" +
		"\n" +
		"        void            m3()         { // this        is        a    bug#]\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"		}";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class E {\n" +
		"\n" +
		"	void m3() { // this is a bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"		}"
	);
}
public void testBug232768_Line05() throws JavaModelException {
	// Selection starts before the line comment and ends inside it
	String source = "public class E {       \n" +
		"	\n" +
		"\n[#" +
		"        void            m3()         { // this   #]     is        a    bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"		}";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class E {       \n" +
		"	\n" +
		"\n" +
		"	void m3() { // this     is        a    bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"		}"
	);
}
public void testBug232768_Line06() throws JavaModelException {
	// Selection starts and ends inside the line comment
	String source = "public class E {       \n" +
		"	\n" +
		"\n" +
		"        void            m3()         { // this        is        [#a#]    bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"              }";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class E {       \n" +
		"	\n" +
		"\n" +
		"        void            m3()         { // this        is        a    bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"              }"
	);
}
public void testBug232768_Line07() throws JavaModelException {
	// Selection starts and ends inside the line comment
	String source = "public class F {\n" +
		"	\n" +
		"\n" +
		"        void            m3()         {     \n" +
		"[#        	// this        is        a    bug\n" +
		"#]\n" +
		"        }\n" +
		"   \n" +
		"}";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class F {\n" +
		"	\n" +
		"\n" +
		"        void            m3()         {     \n" +
		"			// this is a bug\n" +
		"\n" +
		"        }\n" +
		"   \n" +
		"}"
	);
}
public void testBug232768_Line08() throws JavaModelException {
	// Selection starts and ends inside the line comment
	String source = "public class G {\n" +
		"	void foo() {\n" +
		"	\n" +
		"        // Now we parse one of 'CustomActionTagDependent',\n" +
		"        // 'CustomActionJSPContent', or 'CustomActionScriptlessContent'.\n" +
		"        // depending on body-content in TLD.\n" +
		"	}\n" +
		"}";
	// Note that the line comment wasn't formatted using 3.3 and 3.4 M6
	formatSource(source,
		"public class G {\n" +
		"	void foo() {\n" +
		"\n" +
		"		// Now we parse one of 'CustomActionTagDependent',\n" +
		"		// 'CustomActionJSPContent', or 'CustomActionScriptlessContent'.\n" +
		"		// depending on body-content in TLD.\n" +
		"	}\n" +
		"}"
	);
}

/**
 * bug 232788: [formatter] Formatter misaligns stars when formatting block comments
 * test Ensure that block comment formatting is correct even with indentation size=1
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=232788"
 */
public void testBug232788_Tabs01() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X01_tabs.java");
}
public void testBug232788_Spaces01() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X01_spaces.java");
}
public void testBug232788_Mixed01() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X01_mixed.java");
}
public void testBug232788_Tabs02() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	formatUnit("bugs.b232788", "X02_tabs.java");
}
public void testBug232788_Spaces02() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	formatUnit("bugs.b232788", "X02_spaces.java");
}
public void testBug232788_Mixed02() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 0;
	this.formatterPrefs.indentation_size = 0;
	formatUnit("bugs.b232788", "X02_mixed.java");
}
public void testBug232788_Tabs03() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X03_tabs.java");
}
public void testBug232788_Spaces03() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.SPACE;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X03_spaces.java");
}
public void testBug232788_Mixed03() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	this.formatterPrefs.tab_char = DefaultCodeFormatterOptions.MIXED;
	this.formatterPrefs.tab_size = 1;
	this.formatterPrefs.indentation_size = 1;
	formatUnit("bugs.b232788", "X03_mixed.java");
}

/**
 * bug 233011: [formatter] Formatting edited lines has problems (esp. with comments)
 * test Ensure that new comment formatter format all comments concerned by selections
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233011"
 */
public void testBug233011() throws JavaModelException {
	String source = "\n" +
		"public class E01 {\n" +
		"        /** \n" +
		"         * Javadoc      [# #]            \n" +
		"         * comment\n" +
		"         */\n" +
		"        /*\n" +
		"         * block           [# #]            \n" +
		"         * comment\n" +
		"         */\n" +
		"        // [#single                       line#] comment\n" +
		"}";
	formatSource(source,
		"\n" +
		"public class E01 {\n" +
		"        /**\n" +
		"		 * Javadoc comment\n" +
		"		 */\n" +
		"        /*\n" +
		"		 * block comment\n" +
		"		 */\n" +
		"        // single line comment\n" +
		"}"
	);
}

/**
 * bug 233228: [formatter] line comments which contains \\u are not correctly formatted
 * test Ensure that the new formatter is not screwed up by invalid unicode value inside comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233228"
 */
public void testBug233228a() throws JavaModelException {
	formatUnit("bugs.b233228", "X01.java");
}
public void testBug233228b() throws JavaModelException {
	formatUnit("bugs.b233228", "X01b.java");
}
public void testBug233228c() throws JavaModelException {
	formatUnit("bugs.b233228", "X01c.java");
}
public void testBug233228d() throws JavaModelException {
	formatUnit("bugs.b233228", "X02.java");
}
public void testBug233228e() throws JavaModelException {
	formatUnit("bugs.b233228", "X03.java");
}

/**
 * bug 233224: [formatter] Xdoclet tags looses @ on format
 * test Ensure that doclet tags are preserved while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233224"
 */
public void testBug233224() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	formatUnit("bugs.b233224", "X01.java");
}

/**
 * bug 233259: [formatter] html tag should not be split by formatter
 * test Ensure that html tag is not split by the new comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233259"
 */
public void testBug233259a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see <a href=\"http://0\">Test</a>\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\"http://0\">Test</a>\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug233259b() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// split html reference as this allow not to go over the max line width
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see <a href=\"http://0123\">Test</a>\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\n" +
		"	 *      \"http://0123\">Test</a>\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug233259c() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see <a href=\"http://012346789\">Test</a>\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\n" +
		"	 *      \"http://012346789\">Test</a>\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug233259d() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see <a href=\"http://012346789012346789012346789\">Test</a>\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\n" +
		"	 *      \"http://012346789012346789012346789\">Test</a>\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 237942: [formatter] String references are put on next line when over the max line length
 * test Ensure that string reference is not put on next line when over the max line width
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237942"
 */
public void testBug237942a() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see \"string reference: 01234567\"\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see \"string reference: 01234567\"\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug237942b() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// do not split string reference as this can lead to javadoc syntax error
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see \"string reference: 012345678\"\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see \"string reference: 012345678\"\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug237942c() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// do not split string reference as this can lead to javadoc syntax error
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see \"string reference: 01234567 90\"\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see \"string reference: 01234567 90\"\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug237942d() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	// difference with 3.3 formatter:
	// do not split string reference as this can lead to javadoc syntax error
	String source =
		"public class X {\n" +
		"        /**\n" +
		"         * @see \"string reference: 01234567890123\"\n" +
		"         */\n" +
		"        void foo() {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * @see \"string reference: 01234567890123\"\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 234336: [formatter] JavaDocTestCase.testMultiLineCommentIndent* tests fail in I20080527-2000 build
 * test Ensure that new comment formatter format all comments concerned by selections
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=234336"
 */
public void testBug234336() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"	[#/**\n" +
		"			 * test test\n" +
		"				 */#]\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	/**\n" +
		"	 * test test\n" +
		"	 */\n" +
		"}\n",
		CodeFormatter.K_JAVA_DOC,
		1, /* indentation level */
		true /* formatting twice */
	);
}

//static { TESTS_PREFIX = "testBug234583"; }
/**
 * bug 234583: [formatter] Code formatter should adapt edits instead of regions
 * test Ensure that selected region(s) are correctly formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=234583"
 */
public void testBug234583a() throws JavaModelException {
	String source =
		"public class X {\n" +
		"[#                        int i= 1;               #]\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	int i = 1;\n" +
		"}\n"
	);
}
public void testBug234583b() throws JavaModelException {
	String source =
		"public class X {      \n" +
		"\n" +
		"\n" +
		"\n" +
		"[#                        int i= 1;               #]\n" +
		"\n" +
		"\n" +
		"\n" +
		"\n" +
		"     }\n";
	formatSource(source,
		"public class X {      \n" +
		"\n" +
		"\n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"\n" +
		"\n" +
		"\n" +
		"     }\n"
	);
}
public void testBug234583c() throws JavaModelException {
	String source =
		"public class X {      \n" +
		"\n" +
		"\n" +
		"\n" +
		"[#                        int i= 1;               \n" +
		"#]\n" +
		"\n" +
		"\n" +
		"\n" +
		"     }\n";
	formatSource(source,
		"public class X {      \n" +
		"\n" +
		"\n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"\n" +
		"\n" +
		"     }\n"
	);
}
public void testBug234583d() throws JavaModelException {
	String source =
		"public class X {      \n" +
		"\n" +
		"\n" +
		"[#\n" +
		"                        int i= 1;               \n" +
		"\n" +
		"#]\n" +
		"\n" +
		"\n" +
		"     }\n";
	formatSource(source,
		"public class X {      \n" +
		"\n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"\n" +
		"     }\n"
	);
}
public void testBug234583e() throws JavaModelException {
	String source =
		"public class X {      \n" +
		"\n" +
		"[#\n" +
		"\n" +
		"                        int i= 1;               \n" +
		"\n" +
		"\n" +
		"#]\n" +
		"\n" +
		"     }\n";
	formatSource(source,
		"public class X {      \n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"     }\n"
	);
}
public void testBug234583f() throws JavaModelException {
	String source =
		"public class X {      \n" +
		"[#\n" +
		"\n" +
		"\n" +
		"                        int i= 1;               \n" +
		"\n" +
		"\n" +
		"\n" +
		"#]\n" +
		"     }\n";
	formatSource(source,
		"public class X {      \n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"     }\n"
	);
}
public void testBug234583g() throws JavaModelException {
	String source =
		"public class X {      [#\n" +
		"\n" +
		"\n" +
		"\n" +
		"                        int i= 1;               \n" +
		"\n" +
		"\n" +
		"\n" +
		"\n" +
		"#]     }\n";
	formatSource(source,
		"public class X {      \n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"     }\n"
	);
}
public void testBug234583h() throws JavaModelException {
	String source =
		"public class X {   [#   \n" +
		"\n" +
		"\n" +
		"\n" +
		"                        int i= 1;               \n" +
		"\n" +
		"\n" +
		"\n" +
		"\n" +
		"   #]  }\n";
	formatSource(source,
		"public class X {   \n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"  }\n"
	);
}
public void testBug234583i() throws JavaModelException {
	String source =
		"public class X {[#      \n" +
		"\n" +
		"\n" +
		"\n" +
		"                        int i= 1;               \n" +
		"\n" +
		"\n" +
		"\n" +
		"\n" +
		"     #]}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	int i = 1;\n" +
		"\n" +
		"}\n"
	);
}
// duplicate https://bugs.eclipse.org/bugs/show_bug.cgi?id=239447
public void testBug234583_Bug239447() throws JavaModelException {
	setPageWidth80();
	String source =
		"public class Bug239447 {\n" +
		"	private static final String CONTENT = \"test.ObjectB {\\n\"\n" +
		"[#			     + \"     multiEle = { name=\\\"Foo\\\" }\\n\"#]\n" +
		"			+ \"     multiEle = :x { name=\\\"Bar\\\" }\\n\" + \"   singleEle = x;\\n\"\n" +
		"			+ \"}\";\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Bug239447 {\n" +
		"	private static final String CONTENT = \"test.ObjectB {\\n\"\n" +
		"			+ \"     multiEle = { name=\\\"Foo\\\" }\\n\"\n" +
		"			+ \"     multiEle = :x { name=\\\"Bar\\\" }\\n\" + \"   singleEle = x;\\n\"\n" +
		"			+ \"}\";\n" +
		"\n" +
		"}\n"
	);
}
// duplicate https://bugs.eclipse.org/bugs/show_bug.cgi?id=237592
public void testBug234583_Bug237592() throws JavaModelException {
	String source =
		"package test;\n" +
		"\n" +
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"	}\n" +
		"\n" +
		"[#	  #]\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"[#	 #]\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package test;\n" +
		"\n" +
		"public class Test {\n" +
		"\n" +
		"	void foo() {\n" +
		"	}\n" +
		"\n" +
		"\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}

/**
 * bug 236230: [formatter] SIOOBE while formatting a compilation unit.
 * test Ensure that no exception occurs while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=236230"
 */
public void testBug236230() throws JavaModelException {
	String source =
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"  /**\n" +
		"   * <p>If there is an authority, it is:\n" +
		"   * <pre>\n" +
		"   *   //authority/device/pathSegment1/pathSegment2...</pre>\n" +
		"   */\n" +
		"  public String devicePath() {\n" +
		"	  return null;\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * If there is an authority, it is:\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * // authority/device/pathSegment1/pathSegment2...\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public String devicePath() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug236230b() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	String source =
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"  /**\n" +
		"   * <p>If there is an authority, it is:\n" +
		"   * <pre>//authority/device/pathSegment1/pathSegment2...</pre>\n" +
		"   */\n" +
		"  public String devicePath() {\n" +
		"	  return null;\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * If there is an authority, it is:\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * // authority/device/pathSegment1/pathSegment2...\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public String devicePath() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug236230c() throws JavaModelException {
	this.formatterPrefs.comment_format_header = true;
	String source =
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"  /**\n" +
		"   * <p>If there is an authority, it is:\n" +
		"   * <pre>\n" +
		"			import java.util.List;\n" +
		"			//            CU         snippet\n" +
		"			public class X implements List {}\n" +
		"		</pre>\n" +
		"   */\n" +
		"  public String devicePath() {\n" +
		"	  return null;\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * If there is an authority, it is:\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * import java.util.List;\n" +
		"	 * \n" +
		"	 * // CU snippet\n" +
		"	 * public class X implements List {\n" +
		"	 * }\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public String devicePath() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug236230d() throws JavaModelException {
	String source =
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"  /**\n" +
		"   * <p>If there is an authority, it is:\n" +
		"   * <pre>\n" +
		"			//class	body		snippet\n" +
		"			public class X {}\n" +
		"		</pre>\n" +
		"   */\n" +
		"  public String devicePath() {\n" +
		"	  return null;\n" +
		"  }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Need a javadoc comment before to get the exception.\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * If there is an authority, it is:\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * // class body snippet\n" +
		"	 * public class X {\n" +
		"	 * }\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public String devicePath() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
}
// Following tests showed possible regressions while implementing the fix...
public void testBug236230e() throws JavaModelException {
	String source =
		"public class X02 {\n" +
		"\n" +
		"\n" +
		"	/**\n" +
		"	/**\n" +
		"	 * Removes the Java nature from the project.\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"\n" +
		"	/**\n" +
		"	 * /** Removes the Java nature from the project.\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug236230f() throws JavaModelException {
	String source =
		"public class X03 {\n" +
		"  /** The value of <tt>System.getProperty(\"java.version\")<tt>. **/\n" +
		"  static final String JAVA_VERSION = System.getProperty(\"java.version\");\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	/** The value of <tt>System.getProperty(\"java.version\")<tt>. **/\n" +
		"	static final String JAVA_VERSION = System.getProperty(\"java.version\");\n" +
		"\n" +
		"}\n"
	);
}

/**
 * bug 236406: [formatter] Formatting qualified invocations can be broken when the Line Wrapping policy forces element to be on a new line
 * test Verify that wrapping policies forcing the first element to be on a new line are working again...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=236406"
 */
public void testBug236406_CDB1() {
	String source =
		"/**        Javadoc		comment    	    */void foo1() {System.out.println();}\n" +
		"//        Line		comment    	    \n" +
		"void foo2() {System.out.println();}\n" +
		"/*        Block		comment    	    */\n" +
		"void foo3() {\n" +
		"/*        statement Block		comment    	    */\n" +
		"System.out.println();}\n";
	formatSource(source,
		"/**        Javadoc		comment    	    */\n" +
		"void foo1() {\n" +
		"	System.out.println();\n" +
		"}\n" +
		"\n" +
		"//        Line		comment    	    \n" +
		"void foo2() {\n" +
		"	System.out.println();\n" +
		"}\n" +
		"\n" +
		"/*        Block		comment    	    */\n" +
		"void foo3() {\n" +
		"	/*        statement Block		comment    	    */\n" +
		"	System.out.println();\n" +
		"}\n",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS
	);
}
public void testBug236406_CDB2() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"/**        Javadoc		comment    	    */void foo1() {System.out.println();}\n" +
		"//        Line		comment    	    \n" +
		"void foo2() {System.out.println();}\n" +
		"/*        Block		comment    	    */\n" +
		"void foo3() {\n" +
		"/*        statement Block		comment    	    */\n" +
		"System.out.println();}\n";
	formatSource(source,
		"/** Javadoc comment */\n" +
		"void foo1() {\n" +
		"	System.out.println();\n" +
		"}\n" +
		"\n" +
		"// Line comment\n" +
		"void foo2() {\n" +
		"	System.out.println();\n" +
		"}\n" +
		"\n" +
		"/* Block comment */\n" +
		"void foo3() {\n" +
		"	/* statement Block comment */\n" +
		"	System.out.println();\n" +
		"}\n",
		CodeFormatter.K_CLASS_BODY_DECLARATIONS | CodeFormatter.F_INCLUDE_COMMENTS
	);
}
public void testBug236406_EX1() {
	String source =
		"//        Line		comment    	    \n" +
		"i = \n" +
		"/**        Javadoc		comment    	    */\n" +
		"1     +     (/*      Block		comment*/++a)\n";
	formatSource(source,
		"//        Line		comment    	    \n" +
		"i =\n" +
		"		/**        Javadoc		comment    	    */\n" +
		"		1 + (/*      Block		comment*/++a)\n",
		CodeFormatter.K_EXPRESSION
	);
}
public void testBug236406_EX2() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"//        Line		comment    	    \n" +
		"i = \n" +
		"/**        Javadoc		comment    	    */\n" +
		"1     +     (/*      Block		comment*/++a)\n";
	formatSource(source,
		"// Line comment\n" +
		"i =\n" +
		"		/** Javadoc comment */\n" +
		"		1 + (/* Block comment */++a)\n",
		CodeFormatter.K_EXPRESSION | CodeFormatter.F_INCLUDE_COMMENTS
	);
}
public void testBug236406_ST1() {
	String source =
		"/**        Javadoc		comment    	    */foo1();\n" +
		"//        Line		comment    	    \n" +
		"foo2();\n" +
		"/*        Block		comment    	    */\n" +
		"foo3(); {\n" +
		"/*        indented Block		comment    	    */\n" +
		"System.out.println();}\n";
	formatSource(source,
		"/**        Javadoc		comment    	    */\n" +
		"foo1();\n" +
		"//        Line		comment    	    \n" +
		"foo2();\n" +
		"/*        Block		comment    	    */\n" +
		"foo3();\n" +
		"{\n" +
		"	/*        indented Block		comment    	    */\n" +
		"	System.out.println();\n" +
		"}\n",
		CodeFormatter.K_STATEMENTS
	);
}
public void testBug236406_ST2() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"/**        Javadoc		comment    	    */foo1();\n" +
		"//        Line		comment    	    \n" +
		"foo2();\n" +
		"/*        Block		comment    	    */\n" +
		"foo3(); {\n" +
		"/*        indented Block		comment    	    */\n" +
		"System.out.println();}\n";
	formatSource(source,
		"/** Javadoc comment */\n" +
		"foo1();\n" +
		"// Line comment\n" +
		"foo2();\n" +
		"/* Block comment */\n" +
		"foo3();\n" +
		"{\n" +
		"	/* indented Block comment */\n" +
		"	System.out.println();\n" +
		"}\n",
		CodeFormatter.K_STATEMENTS | CodeFormatter.F_INCLUDE_COMMENTS
	);
}

/**
 * bug 237051: [formatter] Formatter insert blank lines after javadoc if javadoc contains Commons Attributes @@ annotations
 * test Ensure that Commons Attributes @@ annotations do not screw up the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237051"
 */
public void testBug237051() throws JavaModelException {
	String source =
		"public interface Test {\n" +
		"/**\n" +
		" * foo\n" +
		" * \n" +
		" * @@Foo(\"foo\")\n" +
		" */\n" +
		"Object doSomething(Object object) throws Exception;\n" +
		"}\n" +
		"\n";
	formatSource(source,
		"public interface Test {\n" +
		"	/**\n" +
		"	 * foo\n" +
		"	 * \n" +
		"	 * @@Foo(\"foo\")\n" +
		"	 */\n" +
		"	Object doSomething(Object object) throws Exception;\n" +
		"}\n"
	);
}
public void testBug237051b() throws JavaModelException {
	String source =
		"public interface Test {\n" +
		"/**\n" +
		" * foo\n" +
		" * @@Foo(\"foo\")\n" +
		" */\n" +
		"Object doSomething(Object object) throws Exception;\n" +
		"}\n" +
		"\n";
	formatSource(source,
		"public interface Test {\n" +
		"	/**\n" +
		"	 * foo\n" +
		"	 * \n" +
		"	 * @@Foo(\"foo\")\n" +
		"	 */\n" +
		"	Object doSomething(Object object) throws Exception;\n" +
		"}\n"
	);
}
public void testBug237051c() throws JavaModelException {
	String source =
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the download rate in bytes per second.  If the rate is unknown,\n" +
		"	 * @{link {@link #UNKNOWN_RATE}} is returned.\n" +
		"	 * @return the download rate in bytes per second\n" +
		"	 */\n" +
		"	public long getTransferRate() {\n" +
		"		return -1;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the download rate in bytes per second. If the rate is unknown,\n" +
		"	 * \n" +
		"	 * @{link {@link #UNKNOWN_RATE}} is returned.\n" +
		"	 * @return the download rate in bytes per second\n" +
		"	 */\n" +
		"	public long getTransferRate() {\n" +
		"		return -1;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug237051d() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"public class X {\n" +
		"\n" +
		"	\n" +
		"	/**\n" +
		"	 * Copies specified input stream to the output stream. Neither stream\n" +
		"	 * is closed as part of this operation.\n" +
		"	 * \n" +
		"	 * @param is input stream\n" +
		"	 * @param os output stream\n" +
		"	 * @param monitor progress monitor\n" +
		"     * @param expectedLength - if > 0, the number of bytes from InputStream will be verified\n" +
		"	 * @@return the offset in the input stream where copying stopped. Returns -1 if end of input stream is reached.\n" +
		"	 * @since 2.0\n" +
		"	 */\n" +
		"	public static long foo() {\n" +
		"		return -1;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * Copies specified input stream to the output stream. Neither stream is\n" +
		"	 * closed as part of this operation.\n" +
		"	 * \n" +
		"	 * @param is\n" +
		"	 *            input stream\n" +
		"	 * @param os\n" +
		"	 *            output stream\n" +
		"	 * @param monitor\n" +
		"	 *            progress monitor\n" +
		"	 * @param expectedLength\n" +
		"	 *            - if > 0, the number of bytes from InputStream will be\n" +
		"	 *            verified\n" +
		"	 * @@return the offset in the input stream where copying stopped. Returns -1\n" +
		"	 *          if end of input stream is reached.\n" +
		"	 * @since 2.0\n" +
		"	 */\n" +
		"	public static long foo() {\n" +
		"		return -1;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 237453: [formatter] Save actions fails to remove excess new lines when set to "format edited lines"
 * test Ensure that empty lines/spaces selection is well formatted
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237453"
 */
public void testBug237453a() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	[#\n" +
		"	#]\n" +
		" 	void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		" 	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453b() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"[#	#]\n" +
		" 	void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"\n" +
		" 	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453c() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"[#	\n" +
		"#] 	void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		" 	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453d() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"[#	\n" +
		" #]	void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453e() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"[#	\n" +
		" 	#]void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453f() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"[# #]	void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453g() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"[# #] void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		" void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453h() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"[# 	#]void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}"
	);
}
public void testBug237453i() throws JavaModelException {
	String source =
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"[#  #]void bar() {\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"package test1;\n" +
		"\n" +
		"public class E1 {\n" +
		" 	void foo() {\n" +
		"	}\n" +
		" 	\n" +
		"	\n" +
		"	void bar() {\n" +
		"	}\n" +
		"}"
	);
}

/**
 * bug 238090: [formatter] Formatter insert blank lines after javadoc if javadoc contains Commons Attributes @@ annotations
 * test Ensure that no unexpected empty new lines are added while formatting a reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238090"
 */
public void testBug238090() throws JavaModelException {
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"package test.bugs;\n" +
		"public class LongNameClass {\n" +
		"/**\n" +
		" * @see test.bugs.\n" +
		" * LongNameClass#longNameMethod(java.lang.String)\n" +
		" */\n" +
		"public void foo() {\n" +
		"}\n" +
		"\n" +
		"void longNameMethod(String str) {\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"package test.bugs;\n" +
		"\n" +
		"public class LongNameClass {\n" +
		"	/**\n" +
		"	 * @see test.bugs.\n" +
		"	 *      LongNameClass#longNameMethod(java.lang.String)\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"\n" +
		"	void longNameMethod(String str) {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 238210: [formatter] CodeFormatter wraps line comments without whitespaces
 * test Ensure that line without spaces are not wrapped by the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238210"
 */
public void testBug238210() throws JavaModelException {
	String source =
		"/**\n" +
		" * LineCommentTestCase\n" +
		" * \n" +
		" * Formatting this compilation unit with line comment enabled and comment line width set to 100 or\n" +
		" * lower will result in both protected region comments to be wrapped although they do not contain\n" +
		" * any whitespace (excluding leading whitespace which should be / is being ignored altogether)\n" +
		" * \n" +
		" * @author Axel Faust, PRODYNA AG\n" +
		" */\n" +
		"public class LineCommentTestCase {\n" +
		"\n" +
		"    public void someGeneratedMethod() {\n" +
		"        //protected-region-start_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]\n" +
		"        // some manually written code\n" +
		"        // protected-region-end_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * LineCommentTestCase\n" +
		" * \n" +
		" * Formatting this compilation unit with line comment enabled and comment line\n" +
		" * width set to 100 or lower will result in both protected region comments to be\n" +
		" * wrapped although they do not contain any whitespace (excluding leading\n" +
		" * whitespace which should be / is being ignored altogether)\n" +
		" * \n" +
		" * @author Axel Faust, PRODYNA AG\n" +
		" */\n" +
		"public class LineCommentTestCase {\n" +
		"\n" +
		"	public void someGeneratedMethod() {\n" +
		"		// protected-region-start_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]\n" +
		"		// some manually written code\n" +
		"		// protected-region-end_[id=_14_0_1_3dd20592_1202209856234_914658_24183_someGeneratedMethod]\n" +
		"	}\n" +
		"}\n"
	);
}
// possible side effects detected while running massive tests
public void testBug238210_X01() throws JavaModelException {
	setPageWidth80();
	String source =
		"package eclipse30;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	void foo() {\n" +
		"		\n" +
		"		binding = new LocalVariableBinding(this, tb, modifiers, false); // argument decl, but local var  (where isArgument = false)\n" +
		"	}\n" +
		"\n" +
		"	public class LocalVariableBinding {\n" +
		"\n" +
		"		public LocalVariableBinding(X01 x01, Object tb, Object modifiers,\n" +
		"				boolean b) {\n" +
		"		}\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"	Object modifiers;\n" +
		"	Object tb;\n" +
		"	LocalVariableBinding binding;\n" +
		"}\n";
	formatSource(source,
		"package eclipse30;\n" +
		"\n" +
		"public class X01 {\n" +
		"\n" +
		"	void foo() {\n" +
		"\n" +
		"		binding = new LocalVariableBinding(this, tb, modifiers, false); // argument\n" +
		"																		// decl,\n" +
		"																		// but\n" +
		"																		// local\n" +
		"																		// var\n" +
		"																		// (where\n" +
		"																		// isArgument\n" +
		"																		// =\n" +
		"																		// false)\n" +
		"	}\n" +
		"\n" +
		"	public class LocalVariableBinding {\n" +
		"\n" +
		"		public LocalVariableBinding(X01 x01, Object tb, Object modifiers,\n" +
		"				boolean b) {\n" +
		"		}\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"	Object modifiers;\n" +
		"	Object tb;\n" +
		"	LocalVariableBinding binding;\n" +
		"}\n",
		false /*do not formatting twice*/
	);
}
public void testBug238210_X02() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"package eclipse30;\n" +
		"\n" +
		"public class X02 {\n" +
		"	//private static short[] randomArray = {213, 231, 37, 85, 211, 29, 161, 175, 187, 3, 147, 246, 170, 30, 202, 183, 242, 47, 254, 189, 25, 248, 193, 2};\n" +
		"}\n";
	formatSource(source,
		"package eclipse30;\n" +
		"\n" +
		"public class X02 {\n" +
		"	// private static short[] randomArray = {213, 231, 37, 85, 211, 29, 161,\n" +
		"	// 175, 187, 3, 147, 246, 170, 30, 202, 183, 242, 47, 254, 189, 25, 248,\n" +
		"	// 193, 2};\n" +
		"}\n"
	);
}
public void testBug238210_X03() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"package eclipse30;\n" +
		"\n" +
		"public class X03 {\n" +
		"\n" +
		"	\n" +
		"	/**\n" +
		"	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#handleBreakpointEvent(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, org.eclipse.jdt.internal.debug.core.model.JDIThread)\n" +
		"	 * \n" +
		"	 * (From referenced JavaDoc:\n" +
		"	 * 	Returns whethers the thread should be resumed\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"package eclipse30;\n" +
		"\n" +
		"public class X03 {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#handleBreakpointEvent(com.sun.jdi.event.Event,\n" +
		"	 *      org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget,\n" +
		"	 *      org.eclipse.jdt.internal.debug.core.model.JDIThread)\n" +
		"	 * \n" +
		"	 *      (From referenced JavaDoc: Returns whethers the thread should be\n" +
		"	 *      resumed\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 238853: [formatter] Code Formatter does not properly format valid xhtml in javadoc.
 * test Ensure that xhtml valid tags are taken into account by the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238853"
 */
public void testBug238853() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"/**\n" +
		" * This is a test comment. \n" +
		" * <p /> \n" +
		" * Another comment. <br /> \n" +
		" * Another comment.\n" +
		" */\n" +
		"public void testMethod1()\n" +
		"{\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * This is a test comment.\n" +
		"	 * <p />\n" +
		"	 * Another comment. <br />\n" +
		"	 * Another comment.\n" +
		"	 */\n" +
		"	public void testMethod1() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 238920: [formatter] Code Formatter removes javadoc status if @category present
 * test Ensure that line without spaces are not wrapped by the comment formatter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=238920"
 */
public void testBug238920() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"/**\n" +
		" * @category test\n" +
		" */\n" +
		"void foo() {\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	/**\n" +
		"	 * @category test\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug238920b() throws JavaModelException {
	String source =
		"public class X02 {\n" +
		"/**\n" +
		" * Test for bug 238920\n" +
		" * @category test\n" +
		" */\n" +
		"void foo() {\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	/**\n" +
		"	 * Test for bug 238920\n" +
		"	 * \n" +
		"	 * @category test\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug238920c() throws JavaModelException {
	String source =
		"public class X03 {\n" +
		"/**\n" +
		" * @category test\n" +
		" * @return zero\n" +
		" */\n" +
		"int foo() {\n" +
		"	return 0;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	/**\n" +
		"	 * @category test\n" +
		"	 * @return zero\n" +
		"	 */\n" +
		"	int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 239130: [formatter] problem formatting block comments
 * test Ensure that the comment formatter preserve line breaks when specified
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=239130"
 */
public void testBug239130_default() throws JavaModelException {
	String source =
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 * \n" +
		"	 * Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 * \n" +
		"	 *      Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_clearBlankLines() throws JavaModelException {
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 * \n" +
		"	 * Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 * \n" +
		"	 * Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 * \n" +
		"	 *      Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_clearBlankLines_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 * \n" +
		"	 * Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * @see java.lang.String\n" +
		"	 *      Formatter should keep empty line above\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=196124
public void testBug239130_196124_default() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"public class X {\n" +
		"\n" +
		"        /**\n" +
		"         * The foo method.\n" +
		"         * foo is a substitute for bar.\n" +
		"         * \n" +
		"         * @param param1 The first parameter\n" +
		"         * @param param2\n" +
		"         *            The second parameter.\n" +
		"         *            If <b>null</b>the first parameter is used\n" +
		"         */\n" +
		"        public void foo(Object param1, Object param2) {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * The foo method. foo is a substitute for bar.\n" +
		"	 * \n" +
		"	 * @param param1\n" +
		"	 *            The first parameter\n" +
		"	 * @param param2\n" +
		"	 *            The second parameter. If <b>null</b>the first parameter is\n" +
		"	 *            used\n" +
		"	 */\n" +
		"	public void foo(Object param1, Object param2) {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_196124() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	useOldJavadocTagsFormatting();
	String source =
		"public class X {\n" +
		"\n" +
		"        /**\n" +
		"         * The foo method.\n" +
		"         * foo is a substitute for bar.\n" +
		"         * \n" +
		"         * @param param1 The first parameter\n" +
		"         * @param param2\n" +
		"         *            The second parameter.\n" +
		"         *            If <b>null</b>the first parameter is used\n" +
		"         */\n" +
		"        public void foo(Object param1, Object param2) {\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"\n" +
		"	/**\n" +
		"	 * The foo method.\n" +
		"	 * foo is a substitute for bar.\n" +
		"	 * \n" +
		"	 * @param param1\n" +
		"	 *            The first parameter\n" +
		"	 * @param param2\n" +
		"	 *            The second parameter.\n" +
		"	 *            If <b>null</b>the first parameter is used\n" +
		"	 */\n" +
		"	public void foo(Object param1, Object param2) {\n" +
		"	}\n" +
		"}\n"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=96696
public void testBug239130_96696_block_default() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * \n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * \n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source, source);
}
public void testBug239130_96696_block_clearBlankLines() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.join_wrapped_lines = false;
	this.formatterPrefs.comment_clear_blank_lines_in_block_comment = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * \n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * \n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	 * Conceptually, all viewers perform two primary tasks: - They help adapt\n" +
		"	 * your domain objects into viewable entities - They provide notifications\n" +
		"	 * when the viewable entities are selected or changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_96696_block_clearBlankLines_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_clear_blank_lines_in_block_comment = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * \n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * \n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/*\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_96696_javadoc_default() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * \n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * \n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source, source);
}
public void testBug239130_96696_javadoc_clearBlankLines() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * \n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * \n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Conceptually, all viewers perform two primary tasks: - They help adapt\n" +
		"	 * your domain objects into viewable entities - They provide notifications\n" +
		"	 * when the viewable entities are selected or changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug239130_96696_javadoc_clearBlankLines_preserveLineBreaks() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * \n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * \n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Conceptually, all viewers perform two primary tasks:\n" +
		"	 * - They help adapt your domain objects into viewable entities\n" +
		"	 * - They provide notifications when the viewable entities are selected or\n" +
		"	 * changed through the UI\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 239719: [formatter] Code formatter destroys pre formatted javadoc comments
 * test Ensure that annotations inside <pre>...</pre> tags are not considered as javadoc tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=239719"
 */
public void testBug239719() throws JavaModelException {
	String source =
		"/**\n" +
		" * <pre>\n" +
		" *  public class Test implements Runnable\n" +
		" *  {\n" +
		" *    @Override\n" +
		" *    public void run()\n" +
		" *    { \n" +
		" *      // Hello really bad Ganymede formatter !!!\n" +
		" *      // Shit happens when somebody tries to change a running system\n" +
		" *      System.out.println(\"Press Shift+Ctrl+F to format\");\n" +
		" *    }\n" +
		" *  }</pre>\n" +
		" */\n" +
		" public class Test \n" +
		" {\n" +
		" }\n";
	formatSource(source,
		"/**\n" +
		" * <pre>\n" +
		" * public class Test implements Runnable {\n" +
		" * 	@Override\n" +
		" * 	public void run() {\n" +
		" * 		// Hello really bad Ganymede formatter !!!\n" +
		" * 		// Shit happens when somebody tries to change a running system\n" +
		" * 		System.out.println(\"Press Shift+Ctrl+F to format\");\n" +
		" * 	}\n" +
		" * }\n" +
		" * </pre>\n" +
		" */\n" +
		"public class Test {\n" +
		"}\n"
	);
}
public void testBug239719b() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"public class X01 {\n" +
		"	\n" +
		"	private int fLength;\n" +
		"	private int fOffset;\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the inclusive end position of this edit. The inclusive end\n" +
		"	 * position denotes the last character of the region manipulated by\n" +
		"	 * this edit. The returned value is the result of the following\n" +
		"	 * calculation:\n" +
		"	 * <pre>\n" +
		"	 *   getOffset() + getLength() - 1;\n" +
		"	 * <pre>\n" +
		"	 * \n" +
		"	 * @return the inclusive end position\n" +
		"	 */\n" +
		"	public final int getInclusiveEnd() {\n" +
		"		return fOffset + fLength - 1;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	private int fLength;\n" +
		"	private int fOffset;\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the inclusive end position of this edit. The inclusive end\n" +
		"	 * position denotes the last character of the region manipulated by this\n" +
		"	 * edit. The returned value is the result of the following calculation:\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * getOffset() + getLength() - 1;\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * \n" +
		"	 * @return the inclusive end position\n" +
		"	 */\n" +
		"	public final int getInclusiveEnd() {\n" +
		"		return fOffset + fLength - 1;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 239941: [formatter] Unclosed html tags make the formatter to produce incorrect outputs
 * test Ensure that unclosed html tags do not screw up the formatter in following javadoc comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=239941"
 */
public void testBug239941() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * Unclosed pre tag\n" +
		"	 */\n" +
		"	int foo;\n" +
		"\n" +
		"    /**\n" +
		"     * Gets the signers of this class.\n" +
		"     *\n" +
		"     * @return  the signers of this class, or null if there are no signers.  In\n" +
		"     * 		particular, this method returns null if this object represents\n" +
		"     * 		a primitive type or void.\n" +
		"     * @since 	JDK1.1\n" +
		"     */\n" +
		"    public native Object[] getSigners();\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * Unclosed pre tag\n" +
		"	 */\n" +
		"	int foo;\n" +
		"\n" +
		"	/**\n" +
		"	 * Gets the signers of this class.\n" +
		"	 *\n" +
		"	 * @return the signers of this class, or null if there are no signers. In\n" +
		"	 *         particular, this method returns null if this object represents a\n" +
		"	 *         primitive type or void.\n" +
		"	 * @since JDK1.1\n" +
		"	 */\n" +
		"	public native Object[] getSigners();\n" +
		"}\n"
	);
}

/**
 * bug 240686: [formatter] Formatter do unexpected things
 * test Ensure that open brace are well taken into account just after the HTML tag opening
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=240686"
 */
public void testBug240686() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"/** \n" +
		" * <pre>{ }</pre>\n" +
		" * \n" +
		" * <table>\n" +
		" * <tr>{ \"1\",\n" +
		" * \"2\"}\n" +
		" * </tr>\n" +
		" * </table>\n" +
		" */\n" +
		"void foo() {}\n" +
		"}\n";
	// output is different than 3.3 one: <tr> is considered as new line tag
	// hence the text inside the tag is put on a new line
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * {}\n" +
		"	 * </pre>\n" +
		"	 * \n" +
		"	 * <table>\n" +
		"	 * <tr>\n" +
		"	 * { \"1\", \"2\"}\n" +
		"	 * </tr>\n" +
		"	 * </table>\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 241345: [formatter] Didn't Format HTML tags is unavailable
 * test Ensure that unset 'Format HTML tags' preference format HTML tags like simple text
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=241345"
 */
public void testBug241345() throws JavaModelException {
	this.formatterPrefs.comment_format_html = false;
	String source =
		"/**\n" +
		" * <p>Should not format HTML paragraph</p>\n" +
		" */\n" +
		"public interface Test {\n" +
		"	/**\n" +
		"	 * \n" +
		"	 * These possibilities include: <ul><li>Formatting of header\n" +
		"	 * comments.</li><li>Formatting of Javadoc tags</li></ul>\n" +
		"	 */\n" +
		"	int bar();\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * <p>Should not format HTML paragraph</p>\n" +
		" */\n" +
		"public interface Test {\n" +
		"	/**\n" +
		"	 * \n" +
		"	 * These possibilities include: <ul><li>Formatting of header\n" +
		"	 * comments.</li><li>Formatting of Javadoc tags</li></ul>\n" +
		"	 */\n" +
		"	int bar();\n" +
		"\n" +
		"}\n"
	);
}

/**
 * bug 241687: [formatter] problem formatting block comments
 * test Ensure that the comment formatter always honors the tacit contract of not modifying block comments starting with '/*-'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=241687"
 */
public void testBug241687() throws JavaModelException {
	String source =
		"public interface Test {\n" +
		"\n" +
		"/*---------------------\n" +
		" * END OF SETS AND GETS\n" +
		" * test test test test test test test\n" +
		"test test test test test test \n" +
		" * \n" +
		"*\n" +
		" *---------------------*/\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface Test {\n" +
		"\n" +
		"	/*---------------------\n" +
		"	 * END OF SETS AND GETS\n" +
		"	 * test test test test test test test\n" +
		"	test test test test test test \n" +
		"	 * \n" +
		"	*\n" +
		"	 *---------------------*/\n" +
		"	void foo();\n" +
		"}\n"
	);
}

/**
 * bug 251133: [formatter] Automatic formatting single line comments is incoherent among tools
 * test Test the new formatter capability to completely ignore line comments starting at first column
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=251133"
 */
public void testBug251133() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	String source =
		"public class X01 {\n" +
		"//		int		a    =  	  1;\n" +
		"//    int     b	=	  	2;\n" +
		"}";
	formatSource(source,
		"public class X01 {\n" +
		"	// int a = 1;\n" +
		"	// int b = 2;\n" +
		"}"
	);
}
public void testBug251133a() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source,
		"public class X {\n" +
		"	// first column comment\n" +
		"}"
	);
}
public void testBug251133b() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source,
		"public class X {\n" +
		"// first column comment\n" +
		"}"
	);
}
public void testBug251133c() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source);
}
public void testBug251133d() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = true;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source);
}
public void testBug251133e() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source,
		"public class X {\n" +
		"	//		first	  	column  	  comment		\n" +
		"}"
	);
}
public void testBug251133f() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = true;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source,
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}"
	);
}
public void testBug251133g() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = false;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source,
		"public class X {\n" +
		"	//		first	  	column  	  comment		\n" +
		"}"
	);
}
public void testBug251133h() throws JavaModelException {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_line_comment_starting_on_first_column = false;
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"public class X {\n" +
		"//		first	  	column  	  comment		\n" +
		"}";
	formatSource(source);
}

/**
 * bug 256799: [formatter] Formatter wrongly adds space to //$FALL-THROUGH$
 * test Ensure that the comment formatter preserve $FALL-THROUGH$ tag leading spaces
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=256799"
 */
public void testBug256799_Line01() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			//$FALL-THROUGH$\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			//    	   $FALL-THROUGH$\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			//		$FALL-THROUGH$                  \n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			//$FALL-THROUGH$\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug256799_Line02() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			//$FALL-THROUGH$     with	text   	   after        \n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$		with	text   	   after        		\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			//    	   $FALL-THROUGH$  		   with	text   	   after	        \n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			//		$FALL-THROUGH$		             		with	text   	   after			\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			//$FALL-THROUGH$ with text after\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$ with text after\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$ with text after\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			// $FALL-THROUGH$ with text after\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug256799_Block01() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			/*$FALL-THROUGH$*/\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$*/\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			/*$FALL-THROUGH$ */\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 5:\n" +
		"			test = value;\n" +
		"			/*    	   $FALL-THROUGH$*/\n" +
		"		case 6:\n" +
		"			test = value;\n" +
		"			/*		$FALL-THROUGH$                  */\n" +
		"		case 7:\n" +
		"			test = value;\n" +
		"			/*$FALL-THROUGH$			*/\n" +
		"		case 8:\n" +
		"			test = value;\n" +
		"			/*		     		     $FALL-THROUGH$	    	    	*/\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 5:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 6:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 7:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		case 8:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ */\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug256799_Block02() throws JavaModelException {
	String source =
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			/*$FALL-THROUGH$with    text    after*/\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$with  		  text	after*/\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			/*$FALL-THROUGH$    with	   	text   	after	    */\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$     with	   	text   	after	    */\n" +
		"		case 5:\n" +
		"			test = value;\n" +
		"			/*    	   $FALL-THROUGH$	with  		  text	after*/\n" +
		"		case 6:\n" +
		"			test = value;\n" +
		"			/*		$FALL-THROUGH$         	with  		  text	after        */\n" +
		"		case 7:\n" +
		"			test = value;\n" +
		"			/*$FALL-THROUGH$			with  		  text	after	*/\n" +
		"		case 8:\n" +
		"			test = value;\n" +
		"			/*		     		     $FALL-THROUGH$	    		with  		  text	after    	*/\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	int foo(int value) {\n" +
		"		int test = 0;\n" +
		"		switch (value) {\n" +
		"		case 1:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$with text after */\n" +
		"		case 2:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$with text after */\n" +
		"		case 3:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ with text after */\n" +
		"		case 4:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ with text after */\n" +
		"		case 5:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ with text after */\n" +
		"		case 6:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ with text after */\n" +
		"		case 7:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ with text after */\n" +
		"		case 8:\n" +
		"			test = value;\n" +
		"			/* $FALL-THROUGH$ with text after */\n" +
		"		default:\n" +
		"			test = -1;\n" +
		"			break;\n" +
		"		}\n" +
		"		return test;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 254998: [formatter] wrong type comment format during code generation
 * test Ensure that the comment formatter works well on the given test case
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=254998"
 */
public void testBug254998() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_header = true;
	String source =
		"/**\n" +
		" * Test for\n" +
		" * bug 254998\n" +
		" */\n" +
		"package javadoc;\n" +
		"\n" +
		"/**\n" +
		" * Test for\n" +
		" * bug 254998\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test for bug 254998\n" +
		" */\n" +
		"package javadoc;\n" +
		"\n" +
		"/**\n" +
		" * Test for\n" +
		" * bug 254998\n" +
		" */\n" +
		"public class Test {\n" +
		"\n" +
		"}\n"
	);
}
public void testBug254998b() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_header = true;
	String source =
		"/*\n" +
		" * Test for\n" +
		" * bug 254998\n" +
		" */\n" +
		"package block;\n" +
		"\n" +
		"/*\n" +
		" * Test for\n" +
		" * bug 254998\n" +
		" */\n" +
		"public class Test {\n" +
		"/*\n" +
		" * Test for\n" +
		" * bug 254998\n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"/*\n" +
		" * Test for bug 254998\n" +
		" */\n" +
		"package block;\n" +
		"\n" +
		"/*\n" +
		" * Test for bug 254998\n" +
		" */\n" +
		"public class Test {\n" +
		"	/*\n" +
		"	 * Test for\n" +
		"	 * bug 254998\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug254998c() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = false;
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_format_header = true;
	String source =
		"//		Test		for		bug		254998\n" +
		"package line;\n" +
		"\n" +
		"//		Test		for		bug		254998\n" +
		"public class Test {\n" +
		"//		Test		for		bug		254998\n" +
		"}\n";
	formatSource(source,
		"// Test for bug 254998\n" +
		"package line;\n" +
		"\n" +
		"// Test for bug 254998\n" +
		"public class Test {\n" +
		"	//		Test		for		bug		254998\n" +
		"}\n"
	);
}

/**
 * bug 260011: [formatter] Formatting of html in javadoc comments doesn't work with style attributes
 * test Ensure that the comment formatter understand <p> html tag with attributes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260011"
 */
public void testBug260011() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"    /**\n" +
		"     * some comment text here\n" +
		"     * <p style=\"font-variant:small-caps;\">\n" +
		"     * some text to be styled a certain way\n" +
		"     * </p>\n" +
		"     */\n" +
		"    void foo() {}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	/**\n" +
		"	 * some comment text here\n" +
		"	 * <p style=\"font-variant:small-caps;\">\n" +
		"	 * some text to be styled a certain way\n" +
		"	 * </p>\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug260011_01() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"    /**\n" +
		"     * some comment text here\n" +
		"     * <ul style=\"font-variant:small-caps;\"><li style=\"font-variant:small-caps;\">\n" +
		"     * some text to be styled a certain way</li></ul>\n" +
		"     * end of comment\n" +
		"     */\n" +
		"    void foo() {}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	/**\n" +
		"	 * some comment text here\n" +
		"	 * <ul style=\"font-variant:small-caps;\">\n" +
		"	 * <li style=\"font-variant:small-caps;\">some text to be styled a certain\n" +
		"	 * way</li>\n" +
		"	 * </ul>\n" +
		"	 * end of comment\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug260011_02() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"    /**\n" +
		"     * some comment text here\n" +
		"     * <pre style=\"font-variant:small-caps;\">\n" +
		"     *      some text\n" +
		"     *           to be styled\n" +
		"     *                 a certain way\n" +
		"     *      \n" +
		"     * </pre>\n" +
		"     * end of comment\n" +
		"     */\n" +
		"    void foo() {}\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	/**\n" +
		"	 * some comment text here\n" +
		"	 * \n" +
		"	 * <pre style=\"font-variant:small-caps;\">\n" +
		"	 *      some text\n" +
		"	 *           to be styled\n" +
		"	 *                 a certain way\n" +
		"	 * \n" +
		"	 * </pre>\n" +
		"	 * \n" +
		"	 * end of comment\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}
public void testBug260011_03() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Indent char is a space char but not a line delimiters.\n" +
		"	 * <code>== Character.isWhitespace(ch) && ch != \'\\n\' && ch != \'\\r\'</code>\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Indent char is a space char but not a line delimiters.\n" +
		"	 * <code>== Character.isWhitespace(ch) && ch != \'\\n\' && ch != \'\\r\'</code>\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug260011_04() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * The list of variable declaration fragments (element type: \n" +
		"	 * <code VariableDeclarationFragment</code>).  Defaults to an empty list.\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * The list of variable declaration fragments (element type: <code\n" +
		"	 * VariableDeclarationFragment</code>). Defaults to an empty list.\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n"
	);
}
public void testBug260011_05() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Compares version strings.\n" +
		"	 * \n" +
		"	 * @return result of comparison, as integer;\n" +
		"	 * <code><0 if left is less than right </code>\n" +
		"	 * <code>0 if left is equals to right</code>\n" +
		"	 * <code>>0 if left is greater than right</code>\n" +
		"	 */\n" +
		"	int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Compares version strings.\n" +
		"	 * \n" +
		"	 * @return result of comparison, as integer;\n" +
		"	 *         <code><0 if left is less than right </code>\n" +
		"	 *         <code>0 if left is equals to right</code>\n" +
		"	 *         <code>>0 if left is greater than right</code>\n" +
		"	 */\n" +
		"	int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug260011_06() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the length of this array.\n" +
		"	 * \n" +
		"	 * @return the length of this array\n" +
		"	 * @exception DebugException if this method fails. Reasons include:<ul>\n" +
		"	 * <li>Failure communicating with the VM.  The DebugException\'s\n" +
		"	 * status code contains the underlying exception responsible for\n" +
		"	 * the failure.</li>\n" +
		"	 * </ul\n" +
		"	 */\n" +
		"	public int getLength();\n" +
		"}\n";
	formatSource(source,
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the length of this array.\n" +
		"	 * \n" +
		"	 * @return the length of this array\n" +
		"	 * @exception DebugException\n" +
		"	 *                if this method fails. Reasons include:\n" +
		"	 *                <ul>\n" +
		"	 *                <li>Failure communicating with the VM. The\n" +
		"	 *                DebugException\'s status code contains the underlying\n" +
		"	 *                exception responsible for the failure.</li> </ul\n" +
		"	 */\n" +
		"	public int getLength();\n" +
		"}\n"
	);
}
public void testBug260011_07() throws JavaModelException {
	String source =
		"public interface Test {\n" +
		"\n" +
		"	\n" +
		"	/**\n" +
		"	 * Returns the change directly associated with this change element or <code\n" +
		"	 * null</code> if the element isn\'t associated with a change.\n" +
		"	 * \n" +
		"	 * @return the change or <code>null</code>\n" +
		"	 */\n" +
		"	public String getChange();\n" +
		"}\n";
	formatSource(source,
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the change directly associated with this change element or <code\n" +
		"	 * null</code> if the element isn\'t associated with a change.\n" +
		"	 * \n" +
		"	 * @return the change or <code>null</code>\n" +
		"	 */\n" +
		"	public String getChange();\n" +
		"}\n"
	);
}
public void testBug260011_08() throws JavaModelException {
	String source =
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Answer the element factory for an id, or <code>null</code. if not found.\n" +
		"	 * @param targetID\n" +
		"	 * @return\n" +
		"	 */\n" +
		"	public int foo(String targetID);\n" +
		"}\n";
	formatSource(source,
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Answer the element factory for an id, or <code>null</code. if not found.\n" +
		"	 * \n" +
		"	 * @param targetID\n" +
		"	 * @return\n" +
		"	 */\n" +
		"	public int foo(String targetID);\n" +
		"}\n"
	);
}
public void testBug260011_09() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"     * o   Example: baseCE < a << b <<< q << c < d < e * nextCE(X,1) \n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * o Example: baseCE < a << b <<< q << c < d < e * nextCE(X,1)\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n"
	);
}
public void testBug260011_09b() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"     * o   Example: baseCE < a < b < q < c < p < e * nextCE(X,1) \n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * o Example: baseCE < a < b < q < c < p < e * nextCE(X,1)\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n"
	);
}
public void testBug260011_10() throws JavaModelException {
	String source =
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Creates and opens a dialog to edit the given template.\n" +
		"	 * <p\n" +
		"	 * Subclasses may override this method to provide a custom dialog.</p>\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Creates and opens a dialog to edit the given template. <p Subclasses may\n" +
		"	 * override this method to provide a custom dialog.\n" +
		"	 * </p>\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260011_11() throws JavaModelException {
	String source =
		"public class Test {\n" +
		"\n" +
		"    /** \n" +
		"     * <p>Binary property IDS_Trinary_Operator (new).</p> \n" +
		"     * <p?For programmatic determination of Ideographic Description \n" +
		"     * Sequences.</p> \n" +
		"     * @stable ICU 2.6\n" +
		"     */ \n" +
		"    public static final int IDS_TRINARY_OPERATOR = 19; \n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * <p>\n" +
		"	 * Binary property IDS_Trinary_Operator (new).\n" +
		"	 * </p>\n" +
		"	 * <p?For programmatic determination of Ideographic Description Sequences.\n" +
		"	 * </p>\n" +
		"	 * \n" +
		"	 * @stable ICU 2.6\n" +
		"	 */\n" +
		"	public static final int IDS_TRINARY_OPERATOR = 19;\n" +
		"}\n"
	);
}

/**
 * bug 260274: [formatter] * character is removed while formatting block comments
 * test Ensure that the comment formatter keep '*' characters while formatting block comments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260274"
 */
public void testBug260274() throws JavaModelException {
	String source =
		"class X {\n" +
		"/*\n" +
		" * The formatter should NOT remove * character\n" +
		" * in block comments!\n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * The formatter should NOT remove * character in block comments!\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260274b() throws JavaModelException {
	String source =
		"class X {\n" +
		"/*\n" +
		" * The formatter should keep \'*\' characters\n" +
		" * in block comments!\n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * The formatter should keep \'*\' characters in block comments!\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260274c() throws JavaModelException {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"class X {\n" +
		"/* *********************************************\n" +
		" * Test \n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * *********************************************\n" +
		"	 * Test\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260274d() throws JavaModelException {
	String source =
		"class X {\n" +
		"/* *********************************************\n" +
		" * Test \n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * ********************************************* Test\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260274e() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"class X {\n" +
		"/*\n" +
		" * **************************************************\n" +
		" * **********  Test  **********  Test  **************\n" +
		" * **************************************************\n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * ************************************************** ********** Test\n" +
		"	 * ********** Test **************\n" +
		"	 * **************************************************\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260274f() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"class X {\n" +
		"/* *****************************************************************************\n" +
		" * Action that allows changing the model providers sort order.\n" +
		" */\n" +
		"void foo() {\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * *************************************************************************\n" +
		"	 * **** Action that allows changing the model providers sort order.\n" +
		"	 */\n" +
		"	void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug260274g() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"class X {\n" +
		"/*\n" +
		" * **********************************************************************************\n" +
		" * **********************************************************************************\n" +
		" * **********************************************************************************\n" +
		" * The code below was added to track the view with focus\n" +
		" * in order to support save actions from a view. Remove this\n" +
		" * experimental code if the decision is to not allow views to \n" +
		" * participate in save actions (see bug 10234) \n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * *************************************************************************\n" +
		"	 * *********\n" +
		"	 * *************************************************************************\n" +
		"	 * *********\n" +
		"	 * *************************************************************************\n" +
		"	 * ********* The code below was added to track the view with focus in order\n" +
		"	 * to support save actions from a view. Remove this experimental code if the\n" +
		"	 * decision is to not allow views to participate in save actions (see bug\n" +
		"	 * 10234)\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260274h() throws JavaModelException {
	String source =
		"class X {\n" +
		"    /**\n" +
		"	 * @see #spacing(Point)\n" +
		"	 * * @see #spacing(int, int)\n" +
		"	 */\n" +
		"    public void foo() {\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/**\n" +
		"	 * @see #spacing(Point) * @see #spacing(int, int)\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug260274i() throws JavaModelException {
	String source =
		"class X {\n" +
		"/***********************************************\n" +
		" * Test \n" +
		" */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/***********************************************\n" +
		"	 * Test\n" +
		"	 */\n" +
		"}\n"
	);
}

/**
 * bug 260276: [formatter] Inconsistent formatting of one-line block comment
 * test Ensure that the comment formatter has a consistent behavior while formatting one-line block comment
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260276"
 */
public void testBug260276() throws JavaModelException {
	String source =
		"class X {\n" +
		"/* a\n" +
		"comment */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * a comment\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260276b() throws JavaModelException {
	String source =
		"class X {\n" +
		"/* a\n" +
		" comment */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * a comment\n" +
		"	 */\n" +
		"}\n"
	);
}
public void testBug260276c() throws JavaModelException {
	String source =
		"class X {\n" +
		"/* a\n" +
		" * comment */\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	/*\n" +
		"	 * a comment\n" +
		"	 */\n" +
		"}\n"
	);
}

/**
 * bug 260381: [formatter] Javadoc formatter breaks {@code ...} tags.
 * test Ensure that the @code tag is similar to {@code <code>} HTML tag
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260381"
 */
public void testBug260381() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version {@code            The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X01 {\n" +
		"}\n";
	formatSource(source);
}
public void testBug260381a() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version {@code          " +
		" *            The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X01a {\n" +
		"}\n";
	formatSource(source);
}
public void testBug260381b() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" *  @version {@code\n" +
		" * The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X01b {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version {@code\n" +
		" * The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X01b {\n" +
		"}\n"
	);
}
public void testBug260381c() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" *  @version {@code     \n" +
		" *          \n" +
		"            \n" +
		" *          The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X01c {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version {@code     \n" +
		" *          \n" +
		"            \n" +
		" *          The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X01c {\n" +
		"}\n"
	);
}
public void testBug260381d() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>\n" +
		" */\n" +
		"public class X02 {\n" +
		"}\n";
	formatSource(source);
}
public void testBug260381e() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version\n" +
		" *          <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>\n" +
		" */\n" +
		"public class X02b {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @version <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>\n" +
		" */\n" +
		"public class X02b {\n" +
		"}\n"
	);
}
public void testBug260381f() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" *  @see Object <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>\n" +
		" */\n" +
		"public class X02c {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * @author Myself\n" +
		" * @see Object\n" +
		" *      <code>            The  			  text		here     should     not			be   		    			formatted....   	   </code>\n" +
		" */\n" +
		"public class X02c {\n" +
		"}\n"
	);
}
public void testBug260381g() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * {@code            The  			  text		here     should     not			be   		    			formatted....   	   }\n" +
		" */\n" +
		"public class X03 {\n" +
		"}\n";
	formatSource(source);
}
public void testBug260381h() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * <code>            The  			  text		here     should     \n" +
		" * not			be   		    			formatted....   	   </code>\n" +
		" */\n" +
		"public class X03b {\n" +
		"}\n";
	formatSource(source);
}
public void testBug260381i() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * {@code            The  			  text		here     should\n" +
		" * not			be   		    			formatted....   	   }\n" +
		" */\n" +
		"public class X03c {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * {@code            The  			  text		here     should\n" +
		" * not			be   		    			formatted....   	   }\n" +
		" */\n" +
		"public class X03c {\n" +
		"}\n"
	);
}
public void testBug260381j() throws JavaModelException {
	String source =
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * {@code      \n" +
		" *       The  			  text		here     should\n" +
		" *       not			\n" +
		" *       be   		    			formatted....   	   }\n" +
		" */\n" +
		"public class X03d {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Comments that can be formated in several lines...\n" +
		" * \n" +
		" * {@code      \n" +
		" *       The  			  text		here     should\n" +
		" *       not			\n" +
		" *       be   		    			formatted....   	   }\n" +
		" */\n" +
		"public class X03d {\n" +
		"}\n"
	);
}
public void testBug260381k() throws JavaModelException {
	String source =
		"/**\n" +
		" * Literal inline tag should also be untouched by the formatter\n" +
		" * \n" +
		" * @version {@literal            The  			  text		here     should     not			be   		    			     formatted....   	   }\n" +
		" */\n" +
		"public class X04 {\n" +
		"\n" +
		"}\n";
	formatSource(source);
}
public void testBug260381_wksp1_01() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"package wksp1;\n" +
		"\n" +
		"public interface I01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns all configured content types for the given source viewer. This list\n" +
		"	 * tells the caller which content types must be configured for the given source \n" +
		"	 * viewer, i.e. for which content types the given source viewer\'s functionalities\n" +
		"	 * must be specified. This implementation always returns <code>\n" +
		"	 * new String[] { IDocument.DEFAULT_CONTENT_TYPE }</code>.\n" +
		"	 *\n" +
		"	 * @param source the source viewer to be configured by this configuration\n" +
		"	 * @return the configured content types for the given viewer\n" +
		"	 */\n" +
		"	public String[] getConfiguredContentTypes(String source);\n" +
		"}\n";
	formatSource(source,
		"package wksp1;\n" +
		"\n" +
		"public interface I01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns all configured content types for the given source viewer. This\n" +
		"	 * list tells the caller which content types must be configured for the\n" +
		"	 * given source viewer, i.e. for which content types the given source\n" +
		"	 * viewer\'s functionalities must be specified. This implementation always\n" +
		"	 * returns <code>\n" +
		"	 * new String[] { IDocument.DEFAULT_CONTENT_TYPE }</code>.\n" +
		"	 *\n" +
		"	 * @param source\n" +
		"	 *            the source viewer to be configured by this configuration\n" +
		"	 * @return the configured content types for the given viewer\n" +
		"	 */\n" +
		"	public String[] getConfiguredContentTypes(String source);\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_01() throws JavaModelException {
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"public interface I01 {\n" +
		"	/**\n" +
		"	 * Returns the composition of two functions. For {@code f: A->B} and\n" +
		"	 * {@code g: B->C}, composition is defined as the function h such that\n" +
		"	 * {@code h(a) == g(f(a))} for each {@code a}.\n" +
		"	 *\n" +
		"	 * @see <a href=\"//en.wikipedia.org/wiki/Function_composition\">\n" +
		"	 * function composition</a>\n" +
		"	 *\n" +
		"	 * @param g the second function to apply\n" +
		"	 * @param f the first function to apply\n" +
		"	 * @return the composition of {@code f} and {@code g}\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I01 {\n" +
		"	/**\n" +
		"	 * Returns the composition of two functions. For {@code f: A->B} and\n" +
		"	 * {@code g: B->C}, composition is defined as the function h such that\n" +
		"	 * {@code h(a) == g(f(a))} for each {@code a}.\n" +
		"	 *\n" +
		"	 * @see <a href=\"//en.wikipedia.org/wiki/Function_composition\"> function\n" +
		"	 *      composition</a>\n" +
		"	 *\n" +
		"	 * @param g\n" +
		"	 *            the second function to apply\n" +
		"	 * @param f\n" +
		"	 *            the first function to apply\n" +
		"	 * @return the composition of {@code f} and {@code g}\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_01b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"public interface I01b {\n" +
		"  /**\n" +
		"   * Returns the composition of two functions. For <code> f: A->B</code> and\n" +
		"   * <code> g: B->C</code>, composition is defined as the function h such that\n" +
		"   * <code> h(a) == g(f(a))</code> for each <code> a</code>.\n" +
		"   *\n" +
		"   * @see <a href=\"//en.wikipedia.org/wiki/Function_composition\">\n" +
		"   * function composition</a>\n" +
		"   *\n" +
		"   * @param g the second function to apply\n" +
		"   * @param f the first function to apply\n" +
		"   * @return the composition of <code> f</code> and <code> g</code>\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I01b {\n" +
		"	/**\n" +
		"	 * Returns the composition of two functions. For <code> f: A->B</code> and\n" +
		"	 * <code> g: B->C</code>, composition is defined as the function h such that\n" +
		"	 * <code> h(a) == g(f(a))</code> for each <code> a</code>.\n" +
		"	 *\n" +
		"	 * @see <a href=\"//en.wikipedia.org/wiki/Function_composition\"> function\n" +
		"	 *      composition</a>\n" +
		"	 *\n" +
		"	 * @param g\n" +
		"	 *            the second function to apply\n" +
		"	 * @param f\n" +
		"	 *            the first function to apply\n" +
		"	 * @return the composition of <code> f</code> and <code> g</code>\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_01c() throws JavaModelException {
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"public interface I01c {\n" +
		"  /**\n" +
		"   * Returns the composition of two functions. For <code> f: A->B</code> and\n" +
		"   * <code>\n" +
		"   * g: B->C\n" +
		"   * </code>,\n" +
		"   * composition is defined as the function h such that\n" +
		"   * <code>\n" +
		"   *  h(a) == g(f(a))\n" +
		"   *  </code>\n" +
		"   *  for each\n" +
		"   *  <code>\n" +
		"   *  a\n" +
		"   *  </code>.\n" +
		"   *\n" +
		"   * @see <a href=\"//en.wikipedia.org/wiki/Function_composition\">\n" +
		"   * function composition</a>\n" +
		"   *\n" +
		"   * @param g the second function to apply\n" +
		"   * @param f the first function to apply\n" +
		"   * @return the composition of <code> f</code> and <code> g</code>\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I01c {\n" +
		"	/**\n" +
		"	 * Returns the composition of two functions. For <code> f: A->B</code> and\n" +
		"	 * <code>\n" +
		"	 * g: B->C\n" +
		"	 * </code>, composition is defined as the function h such that <code>\n" +
		"	 *  h(a) == g(f(a))\n" +
		"	 *  </code> for each <code>\n" +
		"	 *  a\n" +
		"	 *  </code>.\n" +
		"	 *\n" +
		"	 * @see <a href=\"//en.wikipedia.org/wiki/Function_composition\"> function\n" +
		"	 *      composition</a>\n" +
		"	 *\n" +
		"	 * @param g\n" +
		"	 *            the second function to apply\n" +
		"	 * @param f\n" +
		"	 *            the first function to apply\n" +
		"	 * @return the composition of <code> f</code> and <code> g</code>\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_02() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I02 {\n" +
		"\n" +
		"  /**\n" +
		"   * Implementations of {@code computeNext} <b>must</b> invoke this method when\n" +
		"   * there are no elements left in the iteration.\n" +
		"   *\n" +
		"   * @return {@code null}; a convenience so your {@link #computeNext}\n" +
		"   *     implementation can use the simple statement {@code return endOfData();}\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I02 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Implementations of {@code computeNext} <b>must</b> invoke this method\n" +
		"	 * when there are no elements left in the iteration.\n" +
		"	 *\n" +
		"	 * @return {@code null}; a convenience so your {@link #computeNext}\n" +
		"	 *         implementation can use the simple statement\n" +
		"	 *         {@code return endOfData();}\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_03() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I03 {\n" +
		"  /**\n" +
		"   * A builder for creating immutable bimap instances, especially {@code public\n" +
		"   * static final} bimaps (\"constant bimaps\"). Example: <pre>   {@code\n" +
		"   *\n" +
		"   *   static final ImmutableBiMap<String, Integer> WORD_TO_INT =\n" +
		"   *       new ImmutableBiMap.Builder<String, Integer>()\n" +
		"   *           .put(\"one\", 1)\n" +
		"   *           .put(\"two\", 2)\n" +
		"   *           .put(\"three\", 3)\n" +
		"   *           .build();}</pre>\n" +
		"   *\n" +
		"   * For <i>small</i> immutable bimaps, the {@code ImmutableBiMap.of()} methods\n" +
		"   * are even more convenient.\n" +
		"   *\n" +
		"   * <p>Builder instances can be reused - it is safe to call {@link #build}\n" +
		"   * multiple times to build multiple bimaps in series. Each bimap is a superset\n" +
		"   * of the bimaps created before it.\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I03 {\n" +
		"	/**\n" +
		"	 * A builder for creating immutable bimap instances, especially\n" +
		"	 * {@code public\n" +
		"	 * static final} bimaps (\"constant bimaps\"). Example:\n" +
		"	 * \n" +
		"	 * <pre>   {@code\n" +
		"	 *\n" +
		"	 * static final ImmutableBiMap<String, Integer> WORD_TO_INT = new ImmutableBiMap.Builder<String, Integer>()\n" +
		"	 * 		.put(\"one\", 1).put(\"two\", 2).put(\"three\", 3).build();\n" +
		"	 * }</pre>\n" +
		"	 *\n" +
		"	 * For <i>small</i> immutable bimaps, the {@code ImmutableBiMap.of()}\n" +
		"	 * methods are even more convenient.\n" +
		"	 *\n" +
		"	 * <p>\n" +
		"	 * Builder instances can be reused - it is safe to call {@link #build}\n" +
		"	 * multiple times to build multiple bimaps in series. Each bimap is a\n" +
		"	 * superset of the bimaps created before it.\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_03b() throws JavaModelException {
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I03b {\n" +
		"	/**\n" +
		"	 * A builder for creating immutable bimap instances, xxxxxxxx {@code public\n" +
		"	 * static final} bimaps (\"constant bimaps\").\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I03b {\n" +
		"	/**\n" +
		"	 * A builder for creating immutable bimap instances, xxxxxxxx {@code public\n" +
		"	 * static final} bimaps (\"constant bimaps\").\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_04() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I04 {\n" +
		"\n" +
		"  /**\n" +
		"   * Returns an immutable multiset containing the given elements.\n" +
		"   * \n" +
		"   * <p>The multiset is ordered by the first occurrence of each element. For\n" +
		"   * example, {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3))} yields\n" +
		"   * a multiset with elements in the order {@code 2, 3, 3, 1}.\n" +
		"   *\n" +
		"   * <p>Note that if {@code c} is a {@code Collection<String>}, then {@code\n" +
		"   * ImmutableMultiset.copyOf(c)} returns an {@code ImmutableMultiset<String>}\n" +
		"   * containing each of the strings in {@code c}, while\n" +
		"   * {@code ImmutableMultiset.of(c)} returns an\n" +
		"   * {@code ImmutableMultiset<Collection<String>>} containing one element\n" +
		"   * (the given collection itself).\n" +
		"   *\n" +
		"   * <p><b>Note:</b> Despite what the method name suggests, if {@code elements}\n" +
		"   * is an {@code ImmutableMultiset}, no copy will actually be performed, and\n" +
		"   * the given multiset itself will be returned.\n" +
		"   *\n" +
		"   * @throws NullPointerException if any of {@code elements} is null\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I04 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns an immutable multiset containing the given elements.\n" +
		"	 * \n" +
		"	 * <p>\n" +
		"	 * The multiset is ordered by the first occurrence of each element. For\n" +
		"	 * example, {@code ImmutableMultiset.copyOf(Arrays.asList(2, 3, 1, 3))}\n" +
		"	 * yields a multiset with elements in the order {@code 2, 3, 3, 1}.\n" +
		"	 *\n" +
		"	 * <p>\n" +
		"	 * Note that if {@code c} is a {@code Collection<String>}, then {@code\n" +
		"	 * ImmutableMultiset.copyOf(c)} returns an {@code ImmutableMultiset<String>}\n" +
		"	 * containing each of the strings in {@code c}, while\n" +
		"	 * {@code ImmutableMultiset.of(c)} returns an\n" +
		"	 * {@code ImmutableMultiset<Collection<String>>} containing one element (the\n" +
		"	 * given collection itself).\n" +
		"	 *\n" +
		"	 * <p>\n" +
		"	 * <b>Note:</b> Despite what the method name suggests, if {@code elements}\n" +
		"	 * is an {@code ImmutableMultiset}, no copy will actually be performed, and\n" +
		"	 * the given multiset itself will be returned.\n" +
		"	 *\n" +
		"	 * @throws NullPointerException\n" +
		"	 *             if any of {@code elements} is null\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_05() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I05 {\n" +
		"\n" +
		"  /**\n" +
		"   * Indexes the specified values into a {@code Multimap} by applying a\n" +
		"   * specified function to each item in an {@code Iterable} of values. Each\n" +
		"   * value will be stored as a value in the specified multimap. The key used to\n" +
		"   * store that value in the multimap will be the result of calling the function\n" +
		"   * on that value. Depending on the multimap implementation, duplicate entries\n" +
		"   * (equal keys and equal values) may be collapsed.\n" +
		"   *\n" +
		"   * <p>For example,\n" +
		"   *\n" +
		"   * <pre class=\"code\">\n" +
		"   * List&lt;String> badGuys =\n" +
		"   *   Arrays.asList(\"Inky\", \"Blinky\", \"Pinky\", \"Pinky\", \"Clyde\");\n" +
		"   * Function&lt;String, Integer> stringLengthFunction = ...;\n" +
		"   * Multimap&lt;Integer, String> index = Multimaps.newHashMultimap();\n" +
		"   * Multimaps.index(badGuys, stringLengthFunction, index);\n" +
		"   * System.out.println(index); </pre>\n" +
		"   *\n" +
		"   * prints\n" +
		"   *\n" +
		"   * <pre class=\"code\">\n" +
		"   * {4=[Inky], 5=[Pinky, Clyde], 6=[Blinky]} </pre>\n" +
		"   *\n" +
		"   * The {@link HashMultimap} collapses the duplicate occurrence of\n" +
		"   * {@code (5, \"Pinky\")}.\n" +
		"   *\n" +
		"   * @param values the values to add to the multimap\n" +
		"   * @param keyFunction the function used to produce the key for each value\n" +
		"   * @param multimap the multimap to store the key value pairs\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I05 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Indexes the specified values into a {@code Multimap} by applying a\n" +
		"	 * specified function to each item in an {@code Iterable} of values. Each\n" +
		"	 * value will be stored as a value in the specified multimap. The key used\n" +
		"	 * to store that value in the multimap will be the result of calling the\n" +
		"	 * function on that value. Depending on the multimap implementation,\n" +
		"	 * duplicate entries (equal keys and equal values) may be collapsed.\n" +
		"	 *\n" +
		"	 * <p>\n" +
		"	 * For example,\n" +
		"	 *\n" +
		"	 * <pre class=\"code\">\n" +
		"	 * List&lt;String> badGuys =\n" +
		"	 *   Arrays.asList(\"Inky\", \"Blinky\", \"Pinky\", \"Pinky\", \"Clyde\");\n" +
		"	 * Function&lt;String, Integer> stringLengthFunction = ...;\n" +
		"	 * Multimap&lt;Integer, String> index = Multimaps.newHashMultimap();\n" +
		"	 * Multimaps.index(badGuys, stringLengthFunction, index);\n" +
		"	 * System.out.println(index);\n" +
		"	 * </pre>\n" +
		"	 *\n" +
		"	 * prints\n" +
		"	 *\n" +
		"	 * <pre class=\"code\">\n" +
		"	 * {4=[Inky], 5=[Pinky, Clyde], 6=[Blinky]}\n" +
		"	 * </pre>\n" +
		"	 *\n" +
		"	 * The {@link HashMultimap} collapses the duplicate occurrence of\n" +
		"	 * {@code (5, \"Pinky\")}.\n" +
		"	 *\n" +
		"	 * @param values\n" +
		"	 *            the values to add to the multimap\n" +
		"	 * @param keyFunction\n" +
		"	 *            the function used to produce the key for each value\n" +
		"	 * @param multimap\n" +
		"	 *            the multimap to store the key value pairs\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_06() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I06 {\n" +
		"\n" +
		"  /**\n" +
		"   * Adds a number of occurrences of an element to this multiset. Note that if\n" +
		"   * {@code occurrences == 1}, this method has the identical effect to {@link\n" +
		"   * #add(Object)}. This method is functionally equivalent (except in the case\n" +
		"   * of overflow) to the call {@code addAll(Collections.nCopies(element,\n" +
		"   * occurrences))}, which would presumably perform much more poorly.\n" +
		"   *\n" +
		"   * @param element the element to add occurrences of; may be {@code null} only\n" +
		"   *     if explicitly allowed by the implementation\n" +
		"   * @param occurrences the number of occurrences of this element to add. May\n" +
		"   *     be zero, in which case no change will be made.\n" +
		"   * @return the previous count of this element before the operation; possibly\n" +
		"   *     zero - TODO: make this the actual behavior!\n" +
		"   * @throws IllegalArgumentException if {@code occurrences} is negative, or if\n" +
		"   *     this operation would result in more than {@link Integer#MAX_VALUE}\n" +
		"   *     occurrences of the element \n" +
		"   * @throws NullPointerException if {@code element} is null and this\n" +
		"   *     implementation does not permit null elements. Note that if {@code\n" +
		"   *     occurrences} is zero, the implementation may opt to return normally.\n" +
		"   */\n" +
		"  boolean /*int*/ add(E element, int occurrences);\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I06 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Adds a number of occurrences of an element to this multiset. Note that if\n" +
		"	 * {@code occurrences == 1}, this method has the identical effect to\n" +
		"	 * {@link #add(Object)}. This method is functionally equivalent (except in\n" +
		"	 * the case of overflow) to the call\n" +
		"	 * {@code addAll(Collections.nCopies(element,\n" +
		"	 * occurrences))}, which would presumably perform much more poorly.\n" +
		"	 *\n" +
		"	 * @param element\n" +
		"	 *            the element to add occurrences of; may be {@code null} only if\n" +
		"	 *            explicitly allowed by the implementation\n" +
		"	 * @param occurrences\n" +
		"	 *            the number of occurrences of this element to add. May be zero,\n" +
		"	 *            in which case no change will be made.\n" +
		"	 * @return the previous count of this element before the operation; possibly\n" +
		"	 *         zero - TODO: make this the actual behavior!\n" +
		"	 * @throws IllegalArgumentException\n" +
		"	 *             if {@code occurrences} is negative, or if this operation\n" +
		"	 *             would result in more than {@link Integer#MAX_VALUE}\n" +
		"	 *             occurrences of the element\n" +
		"	 * @throws NullPointerException\n" +
		"	 *             if {@code element} is null and this implementation does not\n" +
		"	 *             permit null elements. Note that if {@code\n" +
		"	 *     occurrences} is zero, the implementation may opt to return normally.\n" +
		"	 */\n" +
		"	boolean /* int */ add(E element, int occurrences);\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_07() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I07 {\n" +
		"\n" +
		"  /**\n" +
		"   * Constructs a new, empty multiset, sorted according to the specified\n" +
		"   * comparator. All elements inserted into the multiset must be <i>mutually\n" +
		"   * comparable</i> by the specified comparator: {@code comparator.compare(e1,\n" +
		"   * e2)} must not throw a {@code ClassCastException} for any elements {@code\n" +
		"   * e1} and {@code e2} in the multiset. If the user attempts to add an element\n" +
		"   * to the multiset that violates this constraint, the {@code add(Object)} call\n" +
		"   * will throw a {@code ClassCastException}.\n" +
		"   *\n" +
		"   * @param comparator the comparator that will be used to sort this multiset. A\n" +
		"   *     null value indicates that the elements\' <i>natural ordering</i> should\n" +
		"   *     be used.\n" +
		"   */\n" +
		"  void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I07 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Constructs a new, empty multiset, sorted according to the specified\n" +
		"	 * comparator. All elements inserted into the multiset must be <i>mutually\n" +
		"	 * comparable</i> by the specified comparator: {@code comparator.compare(e1,\n" +
		"	 * e2)} must not throw a {@code ClassCastException} for any elements {@code\n" +
		"	 * e1} and {@code e2} in the multiset. If the user attempts to add an\n" +
		"	 * element to the multiset that violates this constraint, the\n" +
		"	 * {@code add(Object)} call will throw a {@code ClassCastException}.\n" +
		"	 *\n" +
		"	 * @param comparator\n" +
		"	 *            the comparator that will be used to sort this multiset. A null\n" +
		"	 *            value indicates that the elements\' <i>natural ordering</i>\n" +
		"	 *            should be used.\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_08() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"package wksp2;\n" +
		"\n" +
		"public interface I08 {\n" +
		"\n" +
		"	  /**\n" +
		"	   * Returns the composition of a function and a predicate. For every {@code x},\n" +
		"	   * the generated predicate returns {@code predicate(function(x))}.\n" +
		"	   *\n" +
		"	   * @return the composition of the provided function and predicate\n" +
		"	   */\n" +
		"	void foo();\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"public interface I08 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Returns the composition of a function and a predicate. For every\n" +
		"	 * {@code x}, the generated predicate returns\n" +
		"	 * {@code predicate(function(x))}.\n" +
		"	 *\n" +
		"	 * @return the composition of the provided function and predicate\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug260381_wksp2_09() throws JavaModelException {
	String source =
		"package wksp2;\n" +
		"\n" +
		"/**\n" +
		"	A Conditional represents an if/then/else block.\n" +
		"	When this is created the code  will already have\n" +
		"	the conditional check code. The code is optimized for branch\n" +
		"	offsets that fit in 2 bytes, though will handle 4 byte offsets.\n" +
		"<code>\n" +
		"     if condition\n" +
		"	 then code\n" +
		"	 else code\n" +
		"</code>\n" +
		"     what actually gets built is\n" +
		"<code>\n" +
		"     if !condition branch to eb:\n" +
		"	  then code\n" +
		"	  goto end:  // skip else\n" +
		"	 eb:\n" +
		"	  else code\n" +
		"	 end:\n" +
		"</code>\n" +
		"*/\n" +
		"public class X09 {\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"package wksp2;\n" +
		"\n" +
		"/**\n" +
		" * A Conditional represents an if/then/else block. When this is created the code\n" +
		" * will already have the conditional check code. The code is optimized for\n" +
		" * branch offsets that fit in 2 bytes, though will handle 4 byte offsets. <code>\n" +
		"     if condition\n" +
		"	 then code\n" +
		"	 else code\n" +
		"</code> what actually gets built is <code>\n" +
		"     if !condition branch to eb:\n" +
		"	  then code\n" +
		"	  goto end:  // skip else\n" +
		"	 eb:\n" +
		"	  else code\n" +
		"	 end:\n" +
		"</code>\n" +
		" */\n" +
		"public class X09 {\n" +
		"\n" +
		"}\n"
	);
}

/**
 * bug 260798: [formatter] Strange behavior of never join lines
 * test Ensure that the formatter indents lines correctly when never join lines pref is activated
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=260798"
 */
public void testBug260798() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"class X {\n" +
		"    @Override\n" +
		"    public void addSelectionListener(SelectionListener listener) {\n" +
		"        super.addSelectionListener(new SelectionListener() {\n" +
		"            @Override\n" +
		"            public void widgetSelected(SelectionEvent e) {\n" +
		"            }\n" +
		"\n" +
		"            @Override\n" +
		"            public void widgetDefaultSelected(SelectionEvent e) {\n" +
		"            };\n" +
		"        });\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"	@Override\n" +
		"	public void addSelectionListener(SelectionListener listener) {\n" +
		"		super.addSelectionListener(new SelectionListener() {\n" +
		"			@Override\n" +
		"			public void widgetSelected(SelectionEvent e) {\n" +
		"			}\n" +
		"\n" +
		"			@Override\n" +
		"			public void widgetDefaultSelected(SelectionEvent e) {\n" +
		"			};\n" +
		"		});\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug260798b() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"class X {\n" +
		"\n" +
		"    void foo() {\n" +
		"        this.bar(new Object() {\n" +
		"            @Override\n" +
		"            public String toString() {\n" +
		"                return \"\";\n" +
		"            }\n" +
		"        });\n" +
		"    }\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"\n" +
		"	void foo() {\n" +
		"		this.bar(new Object() {\n" +
		"			@Override\n" +
		"			public String toString() {\n" +
		"				return \"\";\n" +
		"			}\n" +
		"		});\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug260798c() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"class X {\n" +
		"\n" +
		"{\n" +
		"    this.bar(new Object() {\n" +
		"        @Override\n" +
		"        public String toString() {\n" +
		"            return \"\";\n" +
		"        }\n" +
		"    });\n" +
		"}\n" +
		"    void bar(Object object) {\n" +
		"        \n" +
		"    }\n" +
		"\n" +
		"}\n";
	formatSource(source,
		"class X {\n" +
		"\n" +
		"	{\n" +
		"		this.bar(new Object() {\n" +
		"			@Override\n" +
		"			public String toString() {\n" +
		"				return \"\";\n" +
		"			}\n" +
		"		});\n" +
		"	}\n" +
		"\n" +
		"	void bar(Object object) {\n" +
		"\n" +
		"	}\n" +
		"\n" +
		"}\n"
	);
}

/**
 * bug 267551: [formatter] Wrong spacing in default array parameter for annotation type
 * test Ensure that no space is inserted before the array initializer when used inside annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=267551"
 */
public void testBug267551() throws JavaModelException {
	String source =
		"import java.lang.annotation.*;\n" +
		"\n" +
		"@Target({ ElementType.ANNOTATION_TYPE })\n" +
		"@Retention(RetentionPolicy.RUNTIME)\n" +
		"public @interface Foo { }\n";
	formatSource(source,
		"import java.lang.annotation.*;\n" +
		"\n" +
		"@Target({ ElementType.ANNOTATION_TYPE })\n" +
		"@Retention(RetentionPolicy.RUNTIME)\n" +
		"public @interface Foo {\n" +
		"}\n"
	);
}

/**
 * bug 267658: [formatter] Javadoc comments may be still formatted as block comments
 * test Ensure that javadoc comment are formatted as block comment with certain
 * 	options configuration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=267658"
 */
public void testBug267658() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = true;
	this.formatterPrefs.comment_format_line_comment = false;
	String source =
		"/**\n" +
		" * Test for\n" +
		" * bug 267658\n" +
		" */\n" +
		"package javadoc;\n" +
		"\n" +
		"/**\n" +
		" * Test for\n" +
		" * bug 267658\n" +
		" */\n" +
		"public class Test {\n" +
		"/**\n" +
		" * Test for\n" +
		" * bug 267658\n" +
		" */\n" +
		"int field;\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * Test for\n" +
		" * bug 267658\n" +
		" */\n" +
		"package javadoc;\n" +
		"\n" +
		"/**\n" +
		" * Test for\n" +
		" * bug 267658\n" +
		" */\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Test for\n" +
		"	 * bug 267658\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n"
	);
}
public void testBug267658b() throws JavaModelException {
	this.formatterPrefs.comment_format_javadoc_comment = false;
	this.formatterPrefs.comment_format_block_comment = true;
	this.formatterPrefs.comment_format_line_comment = false;
	String source =
		"public class Test {\n" +
		"/**\n" +
		" * test bug\n" +
		" */\n" +
		"int field;\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"	/**\n" +
		"	 * test bug\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n"
	);
}

/**
 * bug 270209: [format] Condensed block comment formatting
 * test Verify that block and javadoc comments are formatted in condensed
 * 		mode when the corresponding preferences is set
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=270209"
 */
public void testBug270209_Block01() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_block_boundaries = false;
	String source =
		"public interface X01 {\n" +
		"\n" +
		"/* Instead of like this.  I use these a lot and\n" +
		" * this can take up a lot of space. */\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X01 {\n" +
		"\n" +
		"	/* Instead of like this. I use these a lot and this can take up a lot of\n" +
		"	 * space. */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug270209_Block02() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_block_boundaries = false;
	String source =
		"public interface X02 {\n" +
		"\n" +
		"/*\n" +
		" * Instead of like this.  I use these a lot and\n" +
		" * this can take up a lot of space.\n" +
		" */\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X02 {\n" +
		"\n" +
		"	/* Instead of like this. I use these a lot and this can take up a lot of\n" +
		"	 * space. */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug270209_Block03() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_block_boundaries = false;
	String source =
		"public interface X03 {\n" +
		"\n" +
		"/*\n" +
		" * \n" +
		" * Instead of like this.  I use these a lot and\n" +
		" * this can take up a lot of space.\n" +
		" * \n" +
		" */\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X03 {\n" +
		"\n" +
		"	/* Instead of like this. I use these a lot and this can take up a lot of\n" +
		"	 * space. */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug270209_Javadoc01() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_javadoc_boundaries = false;
	String source =
		"public interface X01 {\n" +
		"\n" +
		"/** Instead of like this.  I use these a lot and\n" +
		" * this can take up a lot of space. */\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X01 {\n" +
		"\n" +
		"	/** Instead of like this. I use these a lot and this can take up a lot of\n" +
		"	 * space. */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug270209_Javadoc02() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_javadoc_boundaries = false;
	String source =
		"public interface X02 {\n" +
		"\n" +
		"/**\n" +
		" * Instead of like this.  I use these a lot and\n" +
		" * this can take up a lot of space.\n" +
		" */\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X02 {\n" +
		"\n" +
		"	/** Instead of like this. I use these a lot and this can take up a lot of\n" +
		"	 * space. */\n" +
		"	void foo();\n" +
		"}\n"
	);
}
public void testBug270209_Javadoc03() throws JavaModelException {
	this.formatterPrefs.comment_new_lines_at_javadoc_boundaries = false;
	String source =
		"public interface X03 {\n" +
		"\n" +
		"/**\n" +
		" * \n" +
		" * Instead of like this.  I use these a lot and\n" +
		" * this can take up a lot of space.\n" +
		" * \n" +
		" */\n" +
		"void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X03 {\n" +
		"\n" +
		"	/** Instead of like this. I use these a lot and this can take up a lot of\n" +
		"	 * space. */\n" +
		"	void foo();\n" +
		"}\n"
	);
}

/**
 * bug 273619: [formatter] Formatting repeats *} in javadoc
 * test Ensure that *} is not repeated while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=273619"
 */
public void testBug273619a() throws JavaModelException {
	String source =
		"/**\n" +
		" * <ul>\n" +
		" * <li>{@code *}</li>\n" +
		" * </ul>\n" +
		" */\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * <ul>\n" +
		" * <li>{@code *}</li>\n" +
		" * </ul>\n" +
		" */\n" +
		"public class X {\n" +
		"}\n"
	);
}
public void testBug273619b() throws JavaModelException {
	String source =
		"/**\n" +
		" * <p>\n" +
		" * {@link *}\n" +
		" * </p>\n" +
		" */\n" +
		"public class X {\n" +
		"}\n";
	formatSource(source,
		"/**\n" +
		" * <p>\n" +
		" * {@link *}\n" +
		" * </p>\n" +
		" */\n" +
		"public class X {\n" +
		"}\n"
	);
}

/**
 * bug 279359: [formatter] Formatter with 'never join lines' produces extra level of indent
 * test Ensure that no extra indentation is produced at the end of a body when
 * 	'never join lines' preference is set.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=279359"
 */
public void testBug279359() throws JavaModelException {
	this.formatterPrefs.join_wrapped_lines = false;
	String source =
		"public class Formatter {\n" +
		"\n" +
		"        public static void main(String[] args) {\n" +
		"\n" +
		"                Executors.newCachedThreadPool().execute(new Runnable() {\n" +
		"\n" +
		"                        public void run() {\n" +
		"                                throw new UnsupportedOperationException(\"stub\");\n" +
		"                        }\n" +
		"\n" +
		"                });\n" +
		"\n" +
		"        }\n" +
		"}\n";
	formatSource(source,
		"public class Formatter {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"\n" +
		"		Executors.newCachedThreadPool().execute(new Runnable() {\n" +
		"\n" +
		"			public void run() {\n" +
		"				throw new UnsupportedOperationException(\"stub\");\n" +
		"			}\n" +
		"\n" +
		"		});\n" +
		"\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 280061: [formatter] AIOOBE while formatting javadoc comment
 * test Ensure that no exception occurs while formatting 1.5 snippet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280061"
 */
public void testBug280061() throws JavaModelException {
	this.formatterOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
	String source =
		"public interface X {\n" +
		"/**\n" +
		" * <pre>\n" +
		" *   void solve(Executor e,\n" +
		" *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)\n" +
		" *     throws InterruptedException, ExecutionException {\n" +
		" *       CompletionService&lt;Result&gt; ecs\n" +
		" *           = new ExecutorCompletionService&lt;Result&gt;(e);\n" +
		" *       for (Callable&lt;Result&gt; s : solvers)\n" +
		" *           ecs.submit(s);\n" +
		" *       int n = solvers.size();\n" +
		" *       for (int i = 0; i &lt; n; ++i) {\n" +
		" *           Result r = ecs.take().get();\n" +
		" *           if (r != null)\n" +
		" *               use(r);\n" +
		" *       }\n" +
		" *   }\n" +
		" * </pre>\n" +
		" */\n" +
		" void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X {\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 *   void solve(Executor e,\n" +
		"	 *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)\n" +
		"	 *     throws InterruptedException, ExecutionException {\n" +
		"	 *       CompletionService&lt;Result&gt; ecs\n" +
		"	 *           = new ExecutorCompletionService&lt;Result&gt;(e);\n" +
		"	 *       for (Callable&lt;Result&gt; s : solvers)\n" +
		"	 *           ecs.submit(s);\n" +
		"	 *       int n = solvers.size();\n" +
		"	 *       for (int i = 0; i &lt; n; ++i) {\n" +
		"	 *           Result r = ecs.take().get();\n" +
		"	 *           if (r != null)\n" +
		"	 *               use(r);\n" +
		"	 *       }\n" +
		"	 *   }\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}

/**
 * bug 280255: [formatter] Format edited lines adds two new lines on each save
 * test Ensure that no new line is added while formatting edited lines
 * 	options configuration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280255"
 */
public void testBug280255() throws JavaModelException {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"public class X {\n" +
		"	private void foo(int val) {\n" +
		"		switch (val) {\n" +
		"			case 0:\n" +
		"			{\n" +
		"\n" +
		"\n" +
		"[#				return ;#]\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source,
		"public class X {\n" +
		"	private void foo(int val) {\n" +
		"		switch (val) {\n" +
		"			case 0:\n" +
		"			{\n" +
		"\n" +
		"\n" +
		"				return;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug280255b() throws JavaModelException {
	this.formatterPrefs.indent_empty_lines = true;
	String source =
		"public class X {\r\n" +
		"	private void foo(int val) {\r\n" +
		"		switch (val) {\r\n" +
		"			case 0:\r\n" +
		"			{\r\n" +
		"\r\n" +
		"\r\n" +
		"[#				return ;#]\r\n" +
		"			}\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"}\r\n";
	formatSource(source,
		"public class X {\r\n" +
		"	private void foo(int val) {\r\n" +
		"		switch (val) {\r\n" +
		"			case 0:\r\n" +
		"			{\r\n" +
		"\r\n" +
		"\r\n" +
		"				return;\r\n" +
		"			}\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"}\r\n"
	);
}

/**
 * bug 280616: [formatter] Valid 1.5 code is not formatted inside {@code <pre>} tag
 * test Ensure that 1.5 snippet is formatted when source level is 1.5
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280616"
 */
public void testBug280616() throws JavaModelException {
	setPageWidth80();
	String source =
		"public interface X {\n" +
		"/**\n" +
		" * <pre>\n" +
		" *   void solve(Executor e,\n" +
		" *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)\n" +
		" *     throws InterruptedException, ExecutionException {\n" +
		" *       CompletionService&lt;Result&gt; ecs\n" +
		" *           = new ExecutorCompletionService&lt;Result&gt;(e);\n" +
		" *       for (Callable&lt;Result&gt; s : solvers)\n" +
		" *           ecs.submit(s);\n" +
		" *       int n = solvers.size();\n" +
		" *       for (int i = 0; i &lt; n; ++i) {\n" +
		" *           Result r = ecs.take().get();\n" +
		" *           if (r != null)\n" +
		" *               use(r);\n" +
		" *       }\n" +
		" *   }\n" +
		" * </pre>\n" +
		" */\n" +
		" void foo();\n" +
		"}\n";
	formatSource(source,
		"public interface X {\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 * void solve(Executor e, Collection&lt;Callable&lt;Result&gt;&gt; solvers)\n" +
		"	 * 		throws InterruptedException, ExecutionException {\n" +
		"	 * 	CompletionService&lt;Result&gt; ecs = new ExecutorCompletionService&lt;Result&gt;(\n" +
		"	 * 			e);\n" +
		"	 * 	for (Callable&lt;Result&gt; s : solvers)\n" +
		"	 * 		ecs.submit(s);\n" +
		"	 * 	int n = solvers.size();\n" +
		"	 * 	for (int i = 0; i &lt; n; ++i) {\n" +
		"	 * 		Result r = ecs.take().get();\n" +
		"	 * 		if (r != null)\n" +
		"	 * 			use(r);\n" +
		"	 * 	}\n" +
		"	 * }\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	void foo();\n" +
		"}\n"
	);
}

/**
 * bug 287833: [formatter] Formatter removes the first character after the * in the {@code <pre>} tag
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=287833"
 */
public void testBug287833a() {
	String source =
		"public class test1 {\n" +
	    "/**\n"+
	    "* <pre>\n"+
	    "*void foo() {\n"+
	    "*}\n"+
	    "* </pre>\n"+
	    "*/\n"+
	    "void foo() {\n"+
	    "}\n"+
	    "}\n";

	formatSource(source,
		"public class test1 {\n"+
	    "	/**\n"+
	    "	 * <pre>\n"+
	    "	 * void foo() {\n"+
	    "	 * }\n"+
	    "	 * </pre>\n"+
	    "	 */\n"+
	    "	void foo() {\n"+
	    "	}\n" +
	    "}\n");
}
public void testBug287833b() {
	String source =
		"public class test1 {\n" +
	    "/**\n"+
	    "* <pre>\n"+
	    "* void foo() {\n"+
	    "*\r\n"+
	    "* }\n"+
	    "* </pre>\n"+
	    "*/ \n"+
	    "void foo() {\n"+
	    "}\n"+
	    "}\n";

	formatSource(source,
		"public class test1 {\n"+
	    "	/**\n"+
	    "	 * <pre>\n"+
	    "	 * void foo() {\n"+
	    "	 *\r\n" +
	    "	 * }\n"+
	    "	 * </pre>\n"+
	    "	 */\n"+
	    "	void foo() {\n"+
	    "	}\n" +
	    "}\n");
}
public void testBug287833c() {
	String source =
		"public class test1 {\n" +
	    "/**\n"+
	    "* <pre>\n"+
	    "* void foo() {\n"+
	    "*\n"+
	    "* }\n"+
	    "* </pre>\n"+
	    "*/ \n"+
	    "void foo() {\n"+
	    "}\n"+
	    "}\n";

	formatSource(source,
		"public class test1 {\n"+
	    "	/**\n"+
	    "	 * <pre>\n"+
	    "	 * void foo() {\n"+
	    "	 *\n" +
	    "	 * }\n"+
	    "	 * </pre>\n"+
	    "	 */\n"+
	    "	void foo() {\n"+
	    "	}\n" +
	    "}\n");
}

/**
 * bug 295825: [formatter] Commentaries are running away after formatting are used
 * test Verify that block comment stay unchanged when text starts with a star
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295825"
 */
public void testBug295825() {
	String source =
		"public class A {\n" +
		"	/* * command */\n" +
		"	void method() {\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}

/**
 * bug 300379: [formatter] Fup of bug 287833
 * test Verify that the leading '{' is not deleted while formatting
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=300379"
 */
public void testBug300379() {
	String source =
		"public class X {\n" +
		"    /**\n" +
		"     * <pre>   {@code\n" +
		"     * \n" +
		"     *   public class X {\n" +
		"     *   }}</pre>\n" +
		"     */\n" +
		"    public void foo() {}\n" +
	    "}\n";

	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * <pre>   {@code\n" +
		"	 * \n" +
		"	 * public class X {\n" +
		"	 * }\n" +
		"	 * }</pre>\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n");
}
public void testBug300379b() {
	String source =
		"public class X {\n" +
		"    /**\n" +
		"     * <pre>   {@code\n" +
		"     * \n" +
		"     *   public class X {}}</pre>\n" +
		"     */\n" +
		"    public void foo() {}\n" +
	    "}\n";

	formatSource(source,
		"public class X {\n" +
		"	/**\n" +
		"	 * <pre>   {@code\n" +
		"	 * \n" +
		"	 * public class X {\n" +
		"	 * }\n" +
		"	 * }</pre>\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n");
}

/**
 * bug 304705: [formatter] Unexpected indentation of wrapped line comments when 'Never indent line comment on first column' preference is checked
 * test Verify that wrapped line comments are also put at first column
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304705"
 */
public void testBug304705() {
	setFormatLineCommentOnFirstColumn();
	this.formatterPrefs.never_indent_line_comments_on_first_column = true;
	String source =
		"public interface Example {\n" +
		"// This is a long comment    with	whitespace     that should be split in multiple line comments in case the line comment formatting is enabled\n" +
		"	int foo();\n" +
	    "}\n";
	formatSource(source,
		"public interface Example {\n" +
		"// This is a long comment with whitespace that should be split in multiple line\n" +
		"// comments in case the line comment formatting is enabled\n" +
		"	int foo();\n" +
	    "}\n");
}
public void testBug304705b() {
	this.formatterPrefs.never_indent_block_comments_on_first_column = true;
	String source =
		"public interface Example {\n" +
		"/* This is a long comment    with	whitespace     that should be split in multiple line comments in case the line comment formatting is enabled */\n" +
		"	int foo();\n" +
	    "}\n";
	formatSource(source,
		"public interface Example {\n" +
		"/*\n" +
		" * This is a long comment with whitespace that should be split in multiple line\n" +
		" * comments in case the line comment formatting is enabled\n" +
		" */\n" +
		"	int foo();\n" +
	    "}\n");
}

/**
 * bug 305281: [formatter] Turning off formatting changes comment's formatting
 * test Verify that turning off formatting in a javadoc does not screw up its indentation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305281"
 */
public void testBug305281() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_DISABLING_TAG, "format: OFF");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_ENABLING_TAG, "format: ON");
	String source =
		"public class test {\n" +
		"\n" +
		"    /**\n" +
		"     * @param args\n" +
		"     * format: OFF\n" +
		"     */\n" +
		"    public static void main(String[] args) {\n" +
		"        do {\n" +
		"            } while (false);\n" +
		"        for (;;) {\n" +
		"        }\n" +
		"        // format: ON\n" +
		"    }\n" +
	    "}\n";
	formatSource(source,
		"public class test {\n" +
		"\n" +
		"	/**\n" +
		"     * @param args\n" +
		"     * format: OFF\n" +
		"     */\n" +
		"    public static void main(String[] args) {\n" +
		"        do {\n" +
		"            } while (false);\n" +
		"        for (;;) {\n" +
		"        }\n" +
		"        // format: ON\n" +
		"	}\n" +
	    "}\n");
}

/**
 * bug 305371: [formatter] Unexpected indentation of line comment
 * test Verify that comments with too different indentation are not considered as contiguous
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305371"
 */
public void testBug305371() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	String source =
		"class X01 {\n" +
		"//  unformatted    comment    !\n" +
		"        //  formatted    comment    !\n" +
	    "}\n";
	formatSource(source,
		"class X01 {\n" +
		"//  unformatted    comment    !\n" +
		"	// formatted comment !\n" +
	    "}\n");
}
public void testBug305371b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	String source =
		"class X02 {\n" +
		"        //  formatted    comment    !\n" +
		"//  unformatted    comment    !\n" +
	    "}\n";
	formatSource(source,
		"class X02 {\n" +
		"	// formatted comment !\n" +
		"//  unformatted    comment    !\n" +
	    "}\n");
}
public void testBug305371c() {
	String source =
		"class X03 {\n" +
		"        //  formatted    comment    1\n" +
		"    //  formatted    comment    2\n" +
	    "}\n";
	formatSource(source,
		"class X03 {\n" +
		"	// formatted comment 1\n" +
		"	// formatted comment 2\n" +
	    "}\n");
}
public void testBug305371d() {
	String source =
		"class X04 {\n" +
		"    //  formatted    comment    1\n" +
		"        //  formatted    comment    2\n" +
	    "}\n";
	formatSource(source,
		"class X04 {\n" +
		"	// formatted comment 1\n" +
		"	// formatted comment 2\n" +
	    "}\n");
}

/**
 * bug 305518: [formatter] Line inside &lt;pre&gt; tag is wrongly indented by one space when starting just after the star
 * test Verify formatting of a &lt;pre&gt; tag section keep lines right indented
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305518"
 */
public void testBug305518() {
	String source =
		"public interface Test {\n" +
		"/**\n" +
		" * <pre>\n" +
		" *    A\n" +
		" *   / \\\n" +
		" *  B   C\n" +
		" * / \\ / \\\n" +
		" *D  E F  G\n" +
		" * </pre>\n" +
		" */\n" +
		"public void foo();\n" +
	    "}\n";
	formatSource(source,
		"public interface Test {\n" +
		"	/**\n" +
		"	 * <pre>\n" +
		"	 *    A\n" +
		"	 *   / \\\n" +
		"	 *  B   C\n" +
		"	 * / \\ / \\\n" +
		"	 *D  E F  G\n" +
		"	 * </pre>\n" +
		"	 */\n" +
		"	public void foo();\n" +
	    "}\n");
}
public void testBug305518_wksp2_01() {
	useOldCommentWidthCounting();
	String source =
		"public class X01 {\n" +
		"/**\n" +
		"	<P> This is an example of starting and shutting down the Network Server in the example\n" +
		"	above with the API.\n" +
		"	<PRE>\n" +
		"	\n" +
		"	NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName(\"myhost\"),1621)\n" +
		"\n" +
		"	serverControl.shutdown();\n" +
		"	</PRE>\n" +
		"\n" +
		"	\n" +
		"*/\n" +
		"public void foo() {}\n" +
	    "}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	/**\n" +
		"	 * <P>\n" +
		"	 * This is an example of starting and shutting down the Network Server in\n" +
		"	 * the example above with the API.\n" +
		"	 * \n" +
		"	 * <PRE>\n" +
		"	\n" +
		"	NetworkServerControl serverControl = new NetworkServerControl(InetAddress.getByName(\"myhost\"),1621)\n" +
		"	\n" +
		"	serverControl.shutdown();\n" +
		"	 * </PRE>\n" +
		"	 * \n" +
		"	 * \n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
	    "}\n");
}
public void testBug305518_wksp2_02() {
	String source =
		"public class X02 {\n" +
		"/**\n" +
		" * Represents namespace name:\n" +
		" * <pre>e.g.<pre>MyNamespace;\n" +
		" *MyProject\\Sub\\Level;\n" +
		" *namespace\\MyProject\\Sub\\Level;\n" +
		" */\n" +
		"public void foo() {}\n" +
	    "}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	/**\n" +
		"	 * Represents namespace name:\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * e.g.\n" +
		"	 * \n" +
		"	 * <pre>\n" +
		"	 * MyNamespace; MyProject\\Sub\\Level; namespace\\MyProject\\Sub\\Level;\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
	    "}\n");
}
public void testBug305518_wksp2_03() {
	String source =
		"public class X03 {\n" +
		"/**\n" +
		"* <PRE>\n" +
		"*  String s = ... ; // get string from somewhere\n" +
		"*  byte [] compressed = UnicodeCompressor.compress(s);\n" +
		"* </PRE>\n" +
		" */\n" +
		"public void foo() {}\n" +
	    "}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	/**\n" +
		"	 * <PRE>\n" +
		"	*  String s = ... ; // get string from somewhere\n" +
		"	*  byte [] compressed = UnicodeCompressor.compress(s);\n" +
		"	 * </PRE>\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
	    "}\n");
}

/**
 * bug 305830: [formatter] Turning off formatting changes comment's formatting
 * test Verify that turning off formatting in a javadoc does not screw up its indentation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305830"
 */
public void testBug305830() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"public class X01 {\n" +
		"void foo() {\n" +
		"bar(\"a non-nls string\", 0 /*a    comment*/); //$NON-NLS-1$\n" +
		"}\n" +
		"void bar(String string, int i) {\n" +
		"}\n" +
	    "}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	void foo() {\n" +
		"		bar(\"a non-nls string\", //$NON-NLS-1$\n" +
		"				0 /* a comment */);\n" +
		"	}\n" +
		"\n" +
		"	void bar(String string, int i) {\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug305830b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"public class X02 {\n" +
		"void foo() {\n" +
		"bar(\"str\", 0 /*a    comment*/); //$NON-NLS-1$\n" +
		"}\n" +
		"void bar(String string, int i) {\n" +
		"}\n" +
	    "}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	void foo() {\n" +
		"		bar(\"str\", 0 /* a comment */); //$NON-NLS-1$\n" +
		"	}\n" +
		"\n" +
		"	void bar(String string, int i) {\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug305830c() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"public class X03 {\n" +
		"void foo() {\n" +
		"bar(\"str\", 0 /*              a						comment                            */); //$NON-NLS-1$\n" +
		"}\n" +
		"void bar(String string, int i) {\n" +
		"}\n" +
	    "}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	void foo() {\n" +
		"		bar(\"str\", 0 /* a comment */); //$NON-NLS-1$\n" +
		"	}\n" +
		"\n" +
		"	void bar(String string, int i) {\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug305830d() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "40");
	String source =
		"public class X01 {\n" +
		"void foo() {\n" +
		"bar(\"a non-nls string\" /*a    comment*/, 0); //$NON-NLS-1$\n" +
		"}\n" +
		"void bar(String string, int i) {\n" +
		"}\n" +
	    "}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	void foo() {\n" +
		"		bar(\"a non-nls string\" /* a comment */, //$NON-NLS-1$\n" +
		"				0);\n" +
		"	}\n" +
		"\n" +
		"	void bar(String string, int i) {\n" +
		"	}\n" +
	    "}\n"
	);
}

/**
 * bug 309835: [formatter] adds blank lines on each run
 * test Test that no line is added when the word exceeds the line width
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=309835"
 */
public void testBug309835() {
	this.formatterPrefs.join_lines_in_comments = false;
	this.formatterPrefs.comment_line_length = 120;
	String source =
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,\n" +
		" *         a blank line is inserted on each formating.\n" +
		" *         Check project preferences to see the format settings\n" +
		" *         (max. line length 80 chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n";
	formatSource(source);
}
public void testBug309835b() {
	this.formatterPrefs.comment_line_length = 120;
	String source =
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,\n" +
		" *         a blank line is inserted on each formating.\n" +
		" *         Check project preferences to see the format settings\n" +
		" *         (max. line length 80 chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n";
	formatSource(source,
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long, a blank\n" +
		" *         line is inserted on each formating. Check project preferences to see the format settings (max. line length 80\n" +
		" *         chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n"
	);
}
public void testBug309835c() {
	this.formatterPrefs.join_lines_in_comments = false;
	String source =
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,\n" +
		" *         a blank line is inserted on each formating.\n" +
		" *         Check project preferences to see the format settings\n" +
		" *         (max. line length 80 chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n";
	formatSource(source,
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line\n" +
		" *         is exactly 121 characters long,\n" +
		" *         a blank line is inserted on each formating.\n" +
		" *         Check project preferences to see the format settings\n" +
		" *         (max. line length 80 chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n"
	);
}
public void testBug309835d() {
	String source =
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line is exactly 121 characters long,\n" +
		" *         a blank line is inserted on each formating.\n" +
		" *         Check project preferences to see the format settings\n" +
		" *         (max. line length 80 chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n";
	formatSource(source,
		"package org.eclipse.bug.test;\n" +
		"\n" +
		"/**\n" +
		" * @author Bug Reporter\n" +
		" *         ThisIsAVeryLongCommentWithoutSpaces_TheFormatterTriesToBreakTheLine_ButTheResultOfItIsThatANewLineIsAdded_____\n" +
		" * \n" +
		" *         Try to press Ctrl+Shift+F to format the code. If the unbreakable line\n" +
		" *         is exactly 121 characters long, a blank line is inserted on each\n" +
		" *         formating. Check project preferences to see the format settings (max.\n" +
		" *         line length 80 chars, max. comment line length 120 chars)\n" +
		" */\n" +
		"public class FormatterBug {\n" +
	    "}\n"
	);
}
public void testBug309835_wksp1_01() {
	useOldJavadocTagsFormatting();
	String source =
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * @param severity the severity to search for. Must be one of <code>FATAL\n" +
		"	 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>\n" +
		"	 */\n" +
		"	public void foo(int severity) {\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * @param severity\n" +
		"	 *            the severity to search for. Must be one of <code>FATAL\n" +
		"	 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>\n" +
		"	 */\n" +
		"	public void foo(int severity) {\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug309835_wksp1_02() {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	String source =
		"public class X02 {\n" +
		"\n" +
		"	/**\n" +
		"	 * INTERNAL USE-ONLY\n" +
		"	 * Generate the byte for a problem method info that correspond to a boggus method.\n" +
		"	 *\n" +
		"	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration\n" +
		"	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding\n" +
		"	 */\n" +
		"	public void foo(int severity) {\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X02 {\n" +
		"\n" +
		"	/**\n" +
		"	 * INTERNAL USE-ONLY Generate the byte for a problem method info that\n" +
		"	 * correspond to a boggus method.\n" +
		"	 *\n" +
		"	 * @param method\n" +
		"	 *            org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration\n" +
		"	 * @param methodBinding\n" +
		"	 *            org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding\n" +
		"	 */\n" +
		"	public void foo(int severity) {\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug309835_wksp2_01() {
	String source =
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"     * Given a jar file, get the names of any AnnotationProcessorFactory\n" +
		"     * implementations it offers.  The information is based on the Sun\n" +
		"     * <a href=\"http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider\">\n" +
		"     * Jar Service Provider spec</a>: the jar file contains a META-INF/services\n" +
		"     */\n" +
		"	public void foo() {\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X01 {\n" +
		"\n" +
		"	/**\n" +
		"	 * Given a jar file, get the names of any AnnotationProcessorFactory\n" +
		"	 * implementations it offers. The information is based on the Sun <a href=\n" +
		"	 * \"http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider\">\n" +
		"	 * Jar Service Provider spec</a>: the jar file contains a META-INF/services\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
	    "}\n"
	);
}

/**
 * bug 311864: [formatter] NPE with empty {@code }
 * test Ensure that no NPE occurs while formatting an empty code inline tag.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=311864"
 */
public void testBug311864() throws JavaModelException {
	useOldCommentWidthCounting();
	useOldJavadocTagsFormatting();
	this.formatterPrefs.use_tags = true;
	String source =
		"public class Test {\n" +
		"\n" +
		"/**\n" +
		"* Compares two property values. For font or color the <i>description</i> of\n" +
		"* the resource, {@link FontData} or {@link RGB}, is used for comparison.\n" +
		"*\n" +
		"* @param value1\n" +
		"* first property value\n" +
		"* @param value2\n" +
		"* second property value\n" +
		"* @return {@code true} if the values are equals; otherwise {@code}\n" +
		"*/\n" +
		"boolean foo(int value1, int value2) {\n" +
		"	return value1 > value2;\n" +
		"}\n" +
		"}\n";
	formatSource(source,
		"public class Test {\n" +
		"\n" +
		"	/**\n" +
		"	 * Compares two property values. For font or color the <i>description</i> of\n" +
		"	 * the resource, {@link FontData} or {@link RGB}, is used for comparison.\n" +
		"	 *\n" +
		"	 * @param value1\n" +
		"	 *            first property value\n" +
		"	 * @param value2\n" +
		"	 *            second property value\n" +
		"	 * @return {@code true} if the values are equals; otherwise {@code}\n" +
		"	 */\n" +
		"	boolean foo(int value1, int value2) {\n" +
		"		return value1 > value2;\n" +
		"	}\n" +
		"}\n"
	);
}

/**
 * bug 315577: [formatter] NullPointerException (always) on inserting a custom template proposal into java code when "Use code formatter" is on
 * test Ensure that no NPE occurs when inserting the custom template
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=315577"
 */
public void testBug315577() throws JavaModelException {
	String source =
		"public class C {\n" +
		"\n" +
		"	/**\n" +
		"	 * aaaa aaa aaa.<br>\n" +
		"	 * {@link C}: aaaa.<br>\n" +
		"	 * {@link C}: aaaa.<br>\n" +
		"	 * aaa {@link C}: aaaa.<br>\n" +
		"	 * {@link C}: aaaa<br>\n" +
		"	 * {@link C}: aaaa.<br>\n" +
		"	 */\n" +
		"	public C() {\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}

/**
 * bug 315732: [formatter] NullPointerException (always) on inserting a custom template proposal into java code when "Use code formatter" is on
 * test Ensure that no NPE occurs when inserting the custom template
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=315732"
 */
public void testBug315732() throws JavaModelException {
	this.formatterPrefs.use_tags = true;
	String source =
		"// ============================================================================\r\n" +
		"// /*-*/\r\n" +
		"// ============================================================================\r\n";
	formatSource(source,
		"	// ============================================================================\n" +
		"	// /*-*/\n" +
		"	// ============================================================================\n",
		CodeFormatter.K_UNKNOWN,
		1,
		true
	);
}

/**
 * bug 313651: [formatter] Unexpected indentation of line comment
 * test Verify that comments with too different indentation are not considered as contiguous
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313651"
 */
public void testBug313651_01() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"public class X01 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"//		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}
public void testBug313651_01b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"public class X01 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"//		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}
public void testBug313651_01c() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"public class X01 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"//		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X01 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"		// System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug313651_02() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"public class X02 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"//		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}
public void testBug313651_02b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"public class X02 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"//		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}
public void testBug313651_02c() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"public class X02 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"//		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X02 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"		// System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug313651_03() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"public class X03 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"//		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"//		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}
public void testBug313651_03b() {
	this.formatterPrefs = null;
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
	this.formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
	String source =
		"public class X03 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"//		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"//		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source);
}
public void testBug313651_03c() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"public class X03 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"//		System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"//		System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	public void testMethod() {\n" +
		"		// Comment 1\n" +
		"		System.out.println(\"start\");\n" +
		"		// System.out.println(\"next\");\n" +
		"		// Comment 1\n" +
		"		// System.out.println(\"end\");\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug313651_wksp3_01() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"package wksp3;\n" +
		"public class X01 implements\n" +
		"// start of comment\n" +
		"// MyFirstInterface {\n" +
		"	MySecondInterface {\n" +
		"// end of comment\n" +
	    "}\n";
	formatSource(source,
		"package wksp3;\n" +
		"\n" +
		"public class X01 implements\n" +
		"		// start of comment\n" +
		"		// MyFirstInterface {\n" +
		"		MySecondInterface {\n" +
		"	// end of comment\n" +
	    "}\n"
	);
}
public void testBug313651_wksp3_02() {
	setFormatLineCommentOnFirstColumn();
	String source =
		"package wksp3;\n" +
		"public class X02 implements MyOtherInterface, \n" +
		"// start of comment\n" +
		"// MyFirstInterface {\n" +
		"	MySecondInterface {\n" +
		"// end of comment\n" +
	    "}\n";
	formatSource(source,
		"package wksp3;\n" +
		"\n" +
		"public class X02 implements MyOtherInterface,\n" +
		"		// start of comment\n" +
		"		// MyFirstInterface {\n" +
		"		MySecondInterface {\n" +
		"	// end of comment\n" +
	    "}\n"
	);
}
public void testBug348338() {
	useOldCommentWidthCounting();
	String source =
		"public class X03 {\n" +
		"	/**\n" +
		"	 * Check wrapping of javadoc tags surrounded wit punctuation [{@code marks}].\n" +
		"	 * <p>\n" +
		"	 * Check wrapping of string literals surrounded with punctuation marks (\"e.g. in parenthesis\" wraps).\n" +
		"	 * <p>\n" +
		"	 * {@code Sometimes wrapping on punctuation is necessary because line is too}. long otherwise.\n" +
		"	 */\n" +
		"	public void test() {\n" +
		"\n" +
		"		/*\n" +
		"		 * Check wrapping of string literals surrounded with punctuation marks (\"e.g. in parenthesis\" wraps).\n" +
		"		 * \n" +
		"		 * The dot at the end of this sentence is beyond the line \"length limit\".\n" +
		"		 * \n" +
		"		 * But this sentence should fit in the line length limit \"with the dot\".\n" +
		"		 */\n" +
		"	}\n" +
	    "}\n";
	formatSource(source,
		"public class X03 {\n" +
		"	/**\n" +
		"	 * Check wrapping of javadoc tags surrounded wit punctuation\n" +
		"	 * [{@code marks}].\n" +
		"	 * <p>\n" +
		"	 * Check wrapping of string literals surrounded with punctuation marks\n" +
		"	 * (\"e.g. in parenthesis\" wraps).\n" +
		"	 * <p>\n" +
		"	 * {@code Sometimes wrapping on punctuation is necessary because line is too}.\n" +
		"	 * long otherwise.\n" +
		"	 */\n" +
		"	public void test() {\n" +
		"\n" +
		"		/*\n" +
		"		 * Check wrapping of string literals surrounded with punctuation marks\n" +
		"		 * (\"e.g. in parenthesis\" wraps).\n" +
		"		 * \n" +
		"		 * The dot at the end of this sentence is beyond the line\n" +
		"		 * \"length limit\".\n" +
		"		 * \n" +
		"		 * But this sentence should fit in the line length limit \"with the dot\".\n" +
		"		 */\n" +
		"	}\n" +
	    "}\n"
	);
}
public void testBug470986() {
	this.formatterPrefs.comment_format_line_comment = false;
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"class Example {  	 // test\n" +
		"\n" +
		"	void method1() {   	  // test\n" +
		"		int a = 1; // test\n" +
		"	}// test\n" +
		"\n" +
		"}";
	formatSource(source);
}
public void testBug471062() {
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"class C {\r\n" +
		"	void method() {\r\n" +
		"		Arrays.asList(1, 2,   // test\r\n" +
		"				3, 4);\r\n" +
		"		if (condition)        // test\r\n" +
		"			operation();\r\n" +
		"	}\r\n" +
		"}";
	formatSource(source);
}
public void testBug471918() {
	String source =
		"class C {\n" +
		"\n" +
		"	/** Returns a new foo instance. */\n" +
		"	public Foo createFoo1() {\n" +
		"	}\n" +
		"\n" +
		"	/** @return a new foo instance. */\n" +
		"	public Foo createFoo2() {\n" +
		"	}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/474011 - [formatter] non-nls strings are duplicated by formatter
 */
public void testBug474011() {
	useOldCommentWidthCounting();
	String source =
		"class A {\n" +
		"	String aaaaaaaaaaaaaaaa = \"11111111111111111111111111111111111111\"; //$NON-NLS-1$ aaa bbb ccc\n" +
		"	String bbbbbbbbbbbbbbbb = \"22222222222222222222222222222222222222\"; //$NON-NLS-1$ //$NON-NLS-1$\n" +
		"	String cccccccccccccccc = \"33333333333333333333333333333333333333\"; //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"	String dddddddddddddddd = \"44444444444444444444444444444444444444\"; //$NON-NLS-1$ // $NON-NLS-2$\n" +
		"	String eeeeeeeeeeeeeeee = \"55555555555555555555555555555555555555\"; //$NON-NLS-1$ // aaa // bbb\n" +
		"}";
	formatSource(source,
		"class A {\n" +
		"	String aaaaaaaaaaaaaaaa = \"11111111111111111111111111111111111111\"; //$NON-NLS-1$ aaa\n" +
		"																		// bbb\n" +
		"																		// ccc\n" +
		"	String bbbbbbbbbbbbbbbb = \"22222222222222222222222222222222222222\"; //$NON-NLS-1$\n" +
		"	String cccccccccccccccc = \"33333333333333333333333333333333333333\"; //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"	String dddddddddddddddd = \"44444444444444444444444444444444444444\"; //$NON-NLS-1$ //\n" +
		"																		// $NON-NLS-2$\n" +
		"	String eeeeeeeeeeeeeeee = \"55555555555555555555555555555555555555\"; //$NON-NLS-1$ //\n" +
		"																		// aaa\n" +
		"																		// //\n" +
		"																		// bbb\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/475294 - [formatter] "Preserve whitespace..." problems with wrapped line comments
 */
public void testBug475294() {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	String source =
		"public class A {\n" +
		"	void a() {\n" +
		"		System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"public class A {\n" +
		"	void a() {\n" +
		"		System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								 // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								  // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								   // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"									// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								 	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								  	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								   	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								    	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"								  	  // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"										// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/475294 - [formatter] "Preserve whitespace..." problems with wrapped line comments
 */
public void testBug475294b() {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_preserve_white_space_between_code_and_line_comments = true;
	this.formatterPrefs.use_tabs_only_for_leading_indentations = true;
	String source =
		"public class A {\n" +
		"	void a() {\n" +
		"		System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"public class A {\n" +
		"	void a() {\n" +
		"		System.out.println();// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                     // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                      // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                       // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                        // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                     	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println(); 	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                      	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                       	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();   	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                        	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();    	// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                         	// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();  	  // aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                       	  // ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"		System.out.println();		// aaaaaaa bbbbbbbbbbbbbbb ccccccccccccc\n" +
		"		                     		// ddddddddddddddd eeeeeeeeeeeeeee\n" +
		"	}\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/479292 - [formatter] Header comment formatting for package-info.java occurs even when "Format header comment" is unchecked
 */
public void testBug479292() {
	String source =
		"/** This   is   a   header   comment */\n" +
		"\n" +
		"/** This   is   a   package   javadoc */\n" +
		"package test;";
	formatSource(source,
		"/** This   is   a   header   comment */\n" +
		"\n" +
		"/** This is a package javadoc */\n" +
		"package test;"
	);
}
/**
 * https://bugs.eclipse.org/479292 - [formatter] Header comment formatting for package-info.java occurs even when "Format header comment" is unchecked
 */
public void testBug479292b() {
	this.formatterPrefs.comment_format_header = true;
	String source =
		"/** This   is   a   header   comment */\n" +
		"\n" +
		"/** This   is   a   package   javadoc */\n" +
		"package test;";
	formatSource(source,
		"/** This is a header comment */\n" +
		"\n" +
		"/** This is a package javadoc */\n" +
		"package test;"
	);
}
/**
 * https://bugs.eclipse.org/121728 - [formatter] Code formatter thinks <P> generic class parameter is a HTML <p> tag
 */
public void testBug121728() {
	String source =
			"/**\n" +
			" * Test Class\n" +
			" *\n" +
			" * @param <P> Some generic class parameter\n" +
			" */\n" +
			"public class Test<P> {\n" +
			"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/479469 - [formatter] Line wrap for long @see references
 */
public void testBug479469() {
	String source =
		"/**\n" +
		" * Test Class\n" +
		" * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
		" *\n" +
		" * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa Label can be wrapped\n" +
		" *\n" +
		" * @see <a href=\"a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\">Label can be wrapped</a>\n" +
		" */\n" +
		"public class Test {\n" +
		"}";
	formatSource(source,
		"/**\n" +
		" * Test Class\n" +
		" * \n" +
		" * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
		" *\n" +
		" * @see a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
		" *      Label can be wrapped\n" +
		" *\n" +
		" * @see <a href=\n" +
		" *      \"a.very.loong.reference.with.a.fully.qualified.paackage.that.should.not.be.wrapped.Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\">Label\n" +
		" *      can be wrapped</a>\n" +
		" */\n" +
		"public class Test {\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/480029 - [formatter] Comments indentation error in javadoc @return statements
 */
public void testBug480029() {
	String source =
		"public class JavadocCommentIssue\n" +
		"{\n" +
		"	/** @return <ul><li>Case 1</b></li></ul> */\n" +
		"	public int foo() {return 0;}\n" +
		"}\n";
	formatSource(source,
		"public class JavadocCommentIssue {\n" +
		"	/**\n" +
		"	 * @return\n" +
		"	 *         <ul>\n" +
		"	 *         <li>Case 1</b></li>\n" +
		"	 *         </ul>\n" +
		"	 */\n" +
		"	public int foo() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n"
	);
}
/**
 * https://bugs.eclipse.org/480030 - [formatter] Comments indentation error in switch statements
 */
public void testBug480030() {
	String source =
		"public class SwitchCommentIssue {\n" +
		"	public void switchIssue(int a) {\n" +
		"		while (a > 0) {\n" +
		"			switch (a) {\n" +
		"			// Test\n" +
		"			case 1:\n" +
		"				break;\n" +
		"			// Test\n" +
		"			case 2:\n" +
		"				continue;\n" +
		"			// Test\n" +
		"			case 3:\n" +
		"				return;\n" +
		"			// Test\n" +
		"			case 4: {\n" +
		"				return;\n" +
		"			}\n" +
		"			// test\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/479474 - [formatter] Problems when doc.comment.support=disabled
 */
public void testBug479474() {
	Hashtable<String, String> parserOptions = JavaCore.getOptions();
	try {
		Hashtable<String, String> newParserOptions = JavaCore.getOptions();
		newParserOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.DISABLED);
		JavaCore.setOptions(newParserOptions);
		String source =
			"/**\n" +
			" * Test\n" +
			" * @author mr.awesome\n" +
			" */\n" +
			"public class Test {\n" +
			"}";
		formatSource(source,
			"/**\n" +
			" * Test\n" +
			" * \n" +
			" * @author mr.awesome\n" +
			" */\n" +
			"public class Test {\n" +
			"}"
		);
	} finally {
		JavaCore.setOptions(parserOptions);
	}
}
/**
 * https://bugs.eclipse.org/484957 - [formatter] Extra blank lines between consecutive javadoc comments
 */
public void testBug484957() {
	String source =
		"import java.io.Serializable;\n" +
		"\n" +
		"/**********/\n" +
		"/*** A ****/\n" +
		"/**********/\n" +
		"\n" +
		"public class MyClass implements Serializable {\n" +
		"\tprivate int field1;\n" +
		"\n" +
		"\t/**********/\n" +
		"\t/*** B ****/\n" +
		"\t/**********/\n" +
		"\tpublic void foo() {\n" +
		"\t}\n" +
		"\n" +
		"\t/**********/\n" +
		"\t/*** C ****/\n" +
		"\t/**********/\n" +
		"\tprivate int field2;\n" +
		"\n" +
		"\t/**********/\n" +
		"\t/*** D ****/\n" +
		"\t/**********/\n" +
		"\tprivate class NestedType {\n" +
		"\t}\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/93459 - [formatter] Javadoc formatter does not break URLs
 */
public void testBug93459() {
	this.formatterPrefs.comment_line_length = 120;
	String source =
		"class Example {\n" +
		"	/**\n" +
		"	 * This is similar to <a href=\"http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html#isSupported(java.lang.String)\">java.nio.charset.Charset.isSupported(String)</a>\n" +
		"	 */\n" +
		"	int a;\n" +
		"}";
	formatSource(source,
		"class Example {\n" +
		"	/**\n" +
		"	 * This is similar to <a href=\n" +
		"	 * \"http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html#isSupported(java.lang.String)\">java.nio.charset.Charset.isSupported(String)</a>\n" +
		"	 */\n" +
		"	int a;\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/510995 - NPE at CommentsPreparator.translateFormattedTokens when using $NON-NLS-1$ in Javadoc
 */
public void testBug510995() {
	String source =
		"/**\n" +
		" * <pre>\n" +
		" * NAME = &quot;org.test....&quot; //$NON-NLS-1$\n" +
		" * </pre>\n" +
		" */\n" +
		"class Test {\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/510995 - NPE at CommentsPreparator.translateFormattedTokens when using $NON-NLS-1$ in Javadoc
 */
public void testBug510995b() {
	String source =
		"/**\n" +
		" * <pre>\n" +
		" * NAME = \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" + \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"; //$NON-NLS-1$ //$NON-NLS-2$\n" +
		" * </pre>\n" +
		" */\n" +
		"class Test {\n" +
		"}";
	formatSource(source,
		"/**\n" +
		" * <pre>\n" +
		" * NAME = \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" //$NON-NLS-1$\n" +
		" * 		+ \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"; //$NON-NLS-1$\n" +
		" * </pre>\n" +
		" */\n" +
		"class Test {\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/512095 - [formatter] Unstable wrap on a line with wrapped code and wrapped block comment
 */
public void testBug512095() {
	useOldCommentWidthCounting();
	String source =
		"class Test1 {\n" +
		"	void f() {\n" +
		"		String c = \"aaaaaaaaaaaaaaaa\" + \"aaaaaaaaaaaaaaa\"; /* 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 */\n" +
		"	}\n" +
		"}";
	formatSource(source,
		"class Test1 {\n" +
		"	void f() {\n" +
		"		String c = \"aaaaaaaaaaaaaaaa\"\n" +
		"				+ \"aaaaaaaaaaaaaaa\"; /*\n" +
		"										 * 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15\n" +
		"										 * 16 17\n" +
		"										 */\n" +
		"	}\n" +
		"}"
	);
}
/**
 * https://bugs.eclipse.org/545898 - [formatter] IOOBE with empty javadoc @param
 */
public void testBug545898() {
	useOldCommentWidthCounting();
	String source =
		"/**\n" +
		" * @param\n" +
		" */\n" +
		"void foo() {\n" +
		"}";
	formatSource(source);
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012a() {
	setComplianceLevel(CompilerOptions.VERSION_17);
	String source =
		"/**\n" +
		" * Test1\n" +
		" * <pre>{@code\n" +
		" * public class X {@Deprecated int    a    ;\n" +
		" * }\n" +
		" * }</pre>\n" +
		" * Test2\n" +
		" * <pre>{@code int    a = 1   ;}</pre>\n" +
		" * Test3\n" +
		" * <pre>\n" +
		" * code sample: {@code public   void foo( ){}  }\n" +
		" * literal sample: {@literal public   void foo( ){}  }\n" +
		" * the end\n" +
		" * </pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}";
	formatSource(source,
		"/**\n" +
		" * Test1\n" +
		" * \n" +
		" * <pre>{@code\n" +
		" * public class X {\n" +
		" * 	@Deprecated\n" +
		" * 	int a;\n" +
		" * }\n" +
		" * }</pre>\n" +
		" * \n" +
		" * Test2\n" +
		" * \n" +
		" * <pre>{@code\n" +
		" * int a = 1;\n" +
		" * }</pre>\n" +
		" * \n" +
		" * Test3\n" +
		" * \n" +
		" * <pre>\n" +
		" * code sample: {@code\n" +
		" * public void foo() {\n" +
		" * }\n" +
		" * }\n" +
		" * literal sample: {@literal public   void foo( ){}  }\n" +
		" * the end\n" +
		" * </pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012b() {
	String source =
		"/**\n" +
		" * <pre>{@code\n" +
		" * public class X {@Deprecated int    a    ;\n" +
		" * }\n" +
		" * }</pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}";
	formatSource(source,
		"/**\n" +
		" * <pre>{@code\n" +
		" * public class X { @Deprecated\n" +
		" * 	int a;\n" +
		" * }\n" +
		" * }</pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012c() {
	String source =
		"/**\n" +
		" * <pre>@something something</pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}";
	formatSource(source,
		"/**\n" +
		" * <pre>\n" +
		" * &#64;something something\n" +
		" * </pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}");
}
/**
 * https://bugs.eclipse.org/552012 - Javadoc formatting breaks {@code} code block.
 */
public void testBug552012d() {
	String source =
		"/**\n" +
		" * <pre>\n" +
		" * @something something\n" +
		" * </pre>\n" +
		" */\n" +
		"public class MyTest {\n" +
		"}";
	formatSource(source);
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2127
 */
public void testIssue2127a() {
	setComplianceLevel(CompilerOptions.VERSION_9);
	String source = """
		/** This   is   a   header   comment */

		/** This   is   a   module   javadoc */
		module test{}
		""";
	formatSource(source, """
		/** This   is   a   header   comment */

		/** This is a module javadoc */
		module test {
		}
		""",
		CodeFormatter.K_MODULE_INFO | CodeFormatter.F_INCLUDE_COMMENTS);
}
/**
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2127
 */
public void testIssue2127b() {
	setComplianceLevel(CompilerOptions.VERSION_9);
	this.formatterPrefs.comment_format_header = true;
	String source = """
		/** This   is   a   header   comment */

		/** This   is   a   module   javadoc */
		module test{}
		""";
	formatSource(source, """
		/** This is a header comment */

		/** This is a module javadoc */
		module test {
		}
		""",
		CodeFormatter.K_MODULE_INFO | CodeFormatter.F_INCLUDE_COMMENTS);
}
}
