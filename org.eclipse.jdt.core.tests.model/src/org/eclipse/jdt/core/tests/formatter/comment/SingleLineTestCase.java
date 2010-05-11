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
package org.eclipse.jdt.core.tests.formatter.comment;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

public class SingleLineTestCase extends CommentTestCase {

	protected static final String PREFIX= "// "; //$NON-NLS-1$

	static {
//		TESTS_NAMES = new String[] { "test109581" } ;
	}

	public static Test suite() {
		return buildTestSuite(SingleLineTestCase.class);
	}

	public SingleLineTestCase(String name) {
		super(name);
	}

	protected int getCommentKind() {
		return CodeFormatter.K_SINGLE_LINE_COMMENT;
	}

	public void testClearBlankLines1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE);
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + "//"+ DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test\ttest" + DELIMITER + "//" + DELIMITER + "//\t\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}

	public void testClearBlankLines2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE);
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + "//" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test\t\ttest" + DELIMITER + PREFIX + DELIMITER + "//\t\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

	public void testClearBlankLines3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE);
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + "//"+ DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test\ttest" + DELIMITER + "//" + DELIMITER + PREFIX + "test\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}

	public void testCommentBegin1() {
		String expected = PREFIX + "test";
		assertEquals(expected, testFormat("//test")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentBegin2() {
		String expected = PREFIX + "test";
		assertEquals(expected, testFormat(PREFIX + "test")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentBegin3() {
		assertEquals(PREFIX + "test" + DELIMITER, testFormat("//\t\ttest " + DELIMITER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentDelimiter1() {
		String expected = PREFIX + "test" + DELIMITER + DELIMITER;
		assertEquals(expected, testFormat("//\t\ttest " + DELIMITER + DELIMITER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentDelimiter2() {
		String expected = PREFIX + "test" + DELIMITER + DELIMITER ;
		assertEquals(expected, testFormat(PREFIX + "test " + DELIMITER + DELIMITER + DELIMITER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentNls1() {
		assertEquals("//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$", testFormat("//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentNls2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "1"); //$NON-NLS-1$
		assertEquals("//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$", testFormat("//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentNls3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		assertEquals("//$NON-NLS-1", testFormat("//$NON-NLS-1")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentNls4() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		assertEquals("//$NON-NLS-4", testFormat("//$NON-NLS-4")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentNls5() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "-2"); //$NON-NLS-1$
		assertEquals("//$NON-NLS-15$", testFormat("//$NON-NLS-15$")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentSpace1() {
		String expected = PREFIX + "test test";
		assertEquals(expected, testFormat("//test\t \t test")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentSpace2() {
		String expected = PREFIX + "test test";
		assertEquals(expected, testFormat("//test test")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentSpace3() {
		String expected = PREFIX + "test test";
		assertEquals(expected, testFormat(PREFIX + "test \t    \t test")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCommentWrapping1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testCommentWrapping2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "1"); //$NON-NLS-1$
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testCommentWrapping3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "32"); //$NON-NLS-1$
		String expected = PREFIX + "test test";
		assertEquals(expected, testFormat("//test\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testCommentWrapping4() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "32"); //$NON-NLS-1$
		assertEquals(PREFIX + "test test" + DELIMITER, testFormat("//test\ttest" + DELIMITER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testCommentWrapping5() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		String prefix= "public class Test {" + DELIMITER + "	int test; // test test test test test test test test test test test test";
		String inputInfix= " ";
		String expectedInfix= DELIMITER + "\t\t\t\t" + PREFIX;
		String suffix= "test" + DELIMITER + "}" + DELIMITER;
		String input= prefix + inputInfix + suffix;
		int offset= input.indexOf("//");
		assertEquals(prefix + expectedInfix + suffix, testFormat(input, offset, input.indexOf(DELIMITER, offset) + DELIMITER.length() - offset));
	}

	public void testHeaderComment1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HEADER, DefaultCodeFormatterConstants.FALSE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "12"); //$NON-NLS-1$
		String expected = PREFIX + "test test" + DELIMITER + PREFIX + "test test" + DELIMITER + PREFIX + "test test";
		assertEquals(expected, testFormat("//test\t\t\t\ttest" + DELIMITER + PREFIX + "test test test test")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public void testHeaderComment2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HEADER, DefaultCodeFormatterConstants.FALSE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "24"); //$NON-NLS-1$
		String expected = "// test" + DELIMITER + PREFIX + "test test test test" + DELIMITER;
		assertEquals(expected, testFormat("//test\t\t\t" + DELIMITER + PREFIX + "test test test test" + DELIMITER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public void testIllegalLineLength1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "1"); //$NON-NLS-1$
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testIllegalLineLength2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "-16"); //$NON-NLS-1$
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat(PREFIX + "\t\t test\ttest")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testMultipleComments1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "5"); //$NON-NLS-1$
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//test test" + DELIMITER + PREFIX + "test test test test")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	}

	public void testMultipleComments2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "12"); //$NON-NLS-1$
		String expected = "// test test" + DELIMITER +
				"// test" + DELIMITER +
				"//" + DELIMITER +
				"// test test" + DELIMITER +
				"// test test";
		assertEquals(expected, testFormat("//test test\ttest" + DELIMITER + PREFIX + DELIMITER + PREFIX + "test test test test")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

	public void testMultipleComments3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "11"); //$NON-NLS-1$
		String expected = PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test" + DELIMITER + PREFIX + "test";
		assertEquals(expected, testFormat("//   test\t\t\ttest\ttest" + DELIMITER + PREFIX + "test test test test")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	}

	public void testIndentedComment1() {
		String prefix= "public class Test {" + DELIMITER + "\t";
		String comment= PREFIX + "test" + DELIMITER;
		String postfix= "}" + DELIMITER;
		String string= prefix + comment + postfix;
		assertEquals(string, testFormat(string, prefix.length(), comment.length()));
	}

	public void testIndentedComment2() {
		String prefix= "public class Test {" + DELIMITER + "\tpublic void test() {" + DELIMITER + "\t\t";
		String comment= PREFIX + "test" + DELIMITER;
		String postfix= "\t}" + DELIMITER + "}" + DELIMITER;
		String string= prefix + comment + postfix;
		assertEquals(string, testFormat(string, prefix.length(), comment.length()));
	}

	public void testIndentedComment3() {
		String prefix= "public class Test {" + DELIMITER + "\tpublic void test() {" + DELIMITER + "\t\tif (true) {" + DELIMITER + "\t\t\t";
		String comment= PREFIX + "test" + DELIMITER;
		String postfix= "\t\t}" + DELIMITER + "\t}" + DELIMITER + "}" + DELIMITER;
		String string= prefix + comment + postfix;
		assertEquals(string, testFormat(string, prefix.length(), comment.length()));
	}

	public void testNoChange1() {
		String content= PREFIX;
		assertEquals(content, testFormat(content));
	}

	public void testNoFormat1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.FALSE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "1");
		String content = PREFIX + "test test";
		assertEquals(content, testFormat(content));
	}
	public void _test109581() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, DefaultCodeFormatterConstants.TRUE);
		String content= "//// some comment ////";
		assertEquals(content + DELIMITER, testFormat(content));
	}
}
