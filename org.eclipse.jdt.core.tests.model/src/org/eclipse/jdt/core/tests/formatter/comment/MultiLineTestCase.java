/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter.comment;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MultiLineTestCase extends CommentTestCase {
	static {
//		TESTS_NAMES = new String[] { "test170580" } ;
	}

	protected static final String INFIX= " * "; //$NON-NLS-1$
	protected static final String POSTFIX= " */"; //$NON-NLS-1$
	private static final String PREFIX= "/* "; //$NON-NLS-1$

	public static Test suite() {
		return buildTestSuite(MultiLineTestCase.class);
	}

	public MultiLineTestCase(String name) {
		super(name);
	}

	@Override
	protected int getCommentKind() {
		return CodeFormatter.K_MULTI_LINE_COMMENT;
	}

	public void testSingleLineComment1() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat("/*\t\t" + DELIMITER + "*\t test*/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public void testSingleLineComment2() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "test" + DELIMITER + "\t" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public void testSingleLineComment3() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + DELIMITER + "* test\t*/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSingleLineComment4() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat("/*test" + DELIMITER + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSingleLineCommentSpace1() {
		assertEquals(PREFIX + "test" + POSTFIX, testFormat("/*test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace2() {
		assertEquals(PREFIX + "test" + POSTFIX, testFormat("/*test" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace3() {
		assertEquals(PREFIX + "test" + POSTFIX, testFormat(PREFIX + "test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace4() {
		assertEquals(PREFIX + "test test" + POSTFIX, testFormat("/* test   test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentTabs1() {
		assertEquals(PREFIX + "test test" + POSTFIX, testFormat("/*\ttest\ttest" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentTabs2() {
		assertEquals(PREFIX + "test test" + POSTFIX, testFormat("/*\ttest\ttest*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * [formatting] formatter removes last line with block comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51654
	 */
	public void testMultiLineCommentAsterisk1() {
		// test3 (currently) forces the comment formatter to actually do something, it wouldn't do anything otherwise.
		String input= PREFIX + INFIX + "test1" + DELIMITER + "test2" + INFIX + DELIMITER + "test3" + DELIMITER + "test4" + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String result= testFormat(input);
		assertTrue(result.indexOf("test1") != -1); //$NON-NLS-1$
		assertTrue(result.indexOf("test2") != -1); //$NON-NLS-1$
		assertTrue(result.indexOf("test3") != -1); //$NON-NLS-1$
		assertTrue(result.indexOf("test4") != -1); //$NON-NLS-1$
	}

	public void testNoChange1() {
		String content= "/*" + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}

	public void testNoFormat1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		String content= PREFIX + DELIMITER + INFIX + "test" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=145544
	 */
	public void testMultiLineCommentFor145544() {
		setUserOption(DefaultCodeFormatterConstants.getJavaConventionsSettings());
		String input= "/**\n" +  //$NON-NLS-1$
				" * Member comment\n" +//$NON-NLS-1$
				" */";//$NON-NLS-1$
		String result= testFormat(input, 0, input.length(), CodeFormatter.K_MULTI_LINE_COMMENT , 2);
		String expectedOutput = "/**\n" +
			" * Member comment\n" +
			" */";
		assertEquals("Different output", expectedOutput, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75460
	public void test75460() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);

		String input = "/*" + DELIMITER +
				"            Object[] objects = new Object[3];" + DELIMITER +
				"            objects[0] = new String(\"Hallo Welt !!!\");" + DELIMITER +
				"            objects[1] = new String(\"Test !!!\");" + DELIMITER +
				"            objects[2] = new Integer(\"1980\");" + DELIMITER +
				"            ObjectFile.write(pathname, objects);" + DELIMITER +
				"            Object[] objs = ObjectFile.read(pathname);" + DELIMITER +
				"            for(int i = 0; i < objs.length; i++)" + DELIMITER +
				"            {" + DELIMITER +
				"              System.out.println(objs[i].toString());" + DELIMITER +
				"            }" + DELIMITER +
				"*/";

		String expected = input;
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=49412
	// check backward compatibility
	/**
	 * @deprecated
	 */
	public void test49412() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.TRUE);

		String input = "/*" + DELIMITER + DELIMITER +
				" test block comment with a blank line" + DELIMITER +
				"*/";

		String expected= "/*" + DELIMITER +
		" * test block comment with a blank line" + DELIMITER +
		" */";

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=49412
	// check backward compatibility
	/**
	 * @deprecated
	 */
	public void test49412_2() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT, DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.TRUE);

		String input = "/*" + DELIMITER + DELIMITER +
				" test block comment with a blank line" + DELIMITER +
				"*/";

		String expected= input;

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=49412
	// check backward compatibility
	/**
	 * @deprecated
	 */
	public void test49412_3() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.FALSE);

		String input = "/*" + DELIMITER + DELIMITER +
				" test block comment with a blank line" + DELIMITER +
				"*/";

		String expected= "/*" + DELIMITER +
		" * "+ DELIMITER +
		" * test block comment with a blank line" + DELIMITER +
		" */";

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170580
	public void _test170580() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input = "/*\n" +
				"<pre>\n" +
				"&lt;test&gt;\n" +
				"</pre>\n" +
				"         */";

		String expected= "/*\n" +
				" * <pre>&lt;test&gt;</pre>\n" +
				" */";

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}
}
