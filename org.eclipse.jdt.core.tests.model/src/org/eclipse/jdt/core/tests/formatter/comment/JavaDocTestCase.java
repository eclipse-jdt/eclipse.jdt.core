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

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.text.edits.TextEdit;

public class JavaDocTestCase extends MultiLineTestCase {

	static {
//		TESTS_NAMES = new String[] { "test109636_2" } ;
	}

	public static Test suite() {
		return buildTestSuite(JavaDocTestCase.class);
	}

	public JavaDocTestCase(String name) {
		super(name);
	}

	protected int getCommentKind() {
		return CodeFormatter.K_JAVA_DOC;
	}

	public void testSingleLineComment1() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "\t\t" + DELIMITER + "*\t test*/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSingleLineComment2() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "test" + DELIMITER + "\t" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSingleLineComment3() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + DELIMITER + "* test\t*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineComment4() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "test" + DELIMITER + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace1() {
		assertEquals(PREFIX + " test" + POSTFIX, testFormat(PREFIX + "test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace2() {
		assertEquals(PREFIX + " test" + POSTFIX, testFormat(PREFIX + "test" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace3() {
		assertEquals(PREFIX + " test" + POSTFIX, testFormat(PREFIX + "test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace4() {
		assertEquals(PREFIX + " test test" + POSTFIX, testFormat(PREFIX + " test   test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentTabs1() {
		assertEquals(PREFIX + " test test" + POSTFIX, testFormat(PREFIX + "\ttest\ttest" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentTabs2() {
		assertEquals(PREFIX + " test test" + POSTFIX, testFormat(PREFIX + "\ttest\ttest*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testMultiLineCommentBreak1() {
		String input= PREFIX + " test<br>test" + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "test<br>" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(expected, testFormat(input));
	}

	public void testMultiLineCommentCodeSnippet1() {
		String prefix= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + "while (i != 0) i--;" + postfix; //$NON-NLS-1$
		String expected= prefix + "while (i != 0)" + DELIMITER + INFIX + "\ti--;" + postfix;    //$NON-NLS-1$//$NON-NLS-2$
		String result= testFormat(input);
		assertEquals(expected, result);

		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);

		assertEquals(expected, result);
	}

	/**
	 * [formatting] Error in formatting parts of java code snippets in comment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44035
	 */
	public void testMultiLineCommentCodeSnippet2() {
		String prefix= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + "while (i != 0) { i--; }" + postfix; //$NON-NLS-1$
		String expected= prefix + "while (i != 0) {" + DELIMITER + INFIX + "\ti--;" + DELIMITER + INFIX + "}" + postfix; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String result= testFormat(input);
		assertEquals(expected, result);

		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);

		assertEquals(expected, result);
	}

	public void testMultiLineCommentCodeSnippet3() {
		String input= PREFIX + DELIMITER + "<pre>" + DELIMITER + "while (i != 0)" + DELIMITER + "i--;" + DELIMITER + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String expected= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX + "while (i != 0)" + DELIMITER + INFIX + "\ti--;" + DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String result= testFormat(input);
		assertEquals(expected, result);

		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);

		assertEquals(expected, result);
	}

	public void testMultiLineCommentCodeSnippet4() {
		String input= "/**\n" +
				" * <pre>\n" +
				" * public Object[] getChildren(Object parentElement) {\n" +
				" *     if (parentElement instanceof MovingBox) {\n" +
				" *         MovingBox box = (MovingBox) parentElement;\n" +
				" *         return concat(box.getBoxes().toArray(), box.getBooks().toArray(), box\n" +
				" *                 .getGames().toArray());\n" +
				" *     }\n" +
				" *     return EMPTY_ARRAY;\n" +
				" * }\n" +
				" * </pre>\n" +
				" */";
		String expected= "/**\n" +
				" * <pre>\n" +
				" * public Object[] getChildren(Object parentElement) {\n" +
				" * 	if (parentElement instanceof MovingBox) {\n" +
				" * 		MovingBox box = (MovingBox) parentElement;\n" +
				" * 		return concat(box.getBoxes().toArray(), box.getBooks().toArray(), box\n" +
				" * 				.getGames().toArray());\n" +
				" * 	}\n" +
				" * 	return EMPTY_ARRAY;\n" +
				" * }\n" +
				" * </pre>\n" +
				" */";
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	public void testMultiLineCommentCodeSnippet5() {
		String input= "/**\n" +
				" * <Pre>\n" +
				" * public Object[] getChildren(Object parentElement) {\n" +
				" *     if (parentElement instanceof MovingBox) {\n" +
				" *         MovingBox box = (MovingBox) parentElement;\n" +
				" *         return concat(box.getBoxes().toArray(), box.getBooks().toArray(), box\n" +
				" *                 .getGames().toArray());\n" +
				" *     }\n" +
				" *     return EMPTY_ARRAY;\n" +
				" * }\n" +
				" * </Pre>\n" +
				" */";
		String expected= "/**\n" +
				" * <Pre>\n" +
				" * public Object[] getChildren(Object parentElement) {\n" +
				" * 	if (parentElement instanceof MovingBox) {\n" +
				" * 		MovingBox box = (MovingBox) parentElement;\n" +
				" * 		return concat(box.getBoxes().toArray(), box.getBooks().toArray(), box\n" +
				" * 				.getGames().toArray());\n" +
				" * 	}\n" +
				" * 	return EMPTY_ARRAY;\n" +
				" * }\n" +
				" * </Pre>\n" +
				" */";
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	public void testMultiLineCommentCodeSnippet6() {
		String input= "/**\n" +
				" * <PRE>\n" +
				" * public Object[] getChildren(Object parentElement) {\n" +
				" *     if (parentElement instanceof MovingBox) {\n" +
				" *         MovingBox box = (MovingBox) parentElement;\n" +
				" *         return concat(box.getBoxes().toArray(), box.getBooks().toArray(), box\n" +
				" *                 .getGames().toArray());\n" +
				" *     }\n" +
				" *     return EMPTY_ARRAY;\n" +
				" * }\n" +
				" * </PRE>\n" +
				" */";
		String expected= "/**\n" +
				" * <PRE>\n" +
				" * public Object[] getChildren(Object parentElement) {\n" +
				" * 	if (parentElement instanceof MovingBox) {\n" +
				" * 		MovingBox box = (MovingBox) parentElement;\n" +
				" * 		return concat(box.getBoxes().toArray(), box.getBooks().toArray(), box\n" +
				" * 				.getGames().toArray());\n" +
				" * 	}\n" +
				" * 	return EMPTY_ARRAY;\n" +
				" * }\n" +
				" * </PRE>\n" +
				" */";
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	public void testMultiLineCommentCodeSnippetHtmlEntities1() {
		String prefix= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + "System.out.println(\"test\");" + postfix; //$NON-NLS-1$
		String expected= prefix + "System.out.println(&quot;test&quot;);" + postfix; //$NON-NLS-1$
		String result= testFormat(input);
		assertEquals(expected, result);

		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);

		assertEquals(expected, result);
	}

	public void testMultiLineCommentIndentTabs1() {
		String prefix= "public class Test {" + DELIMITER + "\t\t"; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t\t" + INFIX + "test test" + DELIMITER + "\t\t\t\t" + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "\t\t" + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}

	/**
	 * [formatting] Comments formatter inserts tabs when it should use spaces
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47491
	 */
	public void testMultiLineCommentIndentSpaces1() {
		String prefix= "public class Test {" + DELIMITER + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "   " + INFIX + "test test" + DELIMITER + "   " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}

	public void testMultiLineCommentIndentSpaces2() {
		String prefix= "public class Test {" + DELIMITER + "    "; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "      " + INFIX + "test test" + DELIMITER + "      " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}

	public void testMultiLineCommentIndentSpaces3() {
		String prefix= "public class Test {" + DELIMITER + "  \t  "; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "      " + INFIX + "test test" + DELIMITER + "      " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}

	public void testMultiLineCommentIndentSpaces4() {
		String prefix= "public class Test {" + DELIMITER + "   \t   "; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "         " + INFIX + "test test" + DELIMITER + "         " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}

	/**
	 * [formatting] Repeated insertion of new line when formatting javadoc comment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50212
	 */
	public void testMultiLineCommentBlankLineAfterPre1() {
		String input= PREFIX + DELIMITER + INFIX + "<pre></pre>" + DELIMITER  + INFIX + "test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		String expected= PREFIX + DELIMITER + INFIX + "<pre></pre>" + DELIMITER + INFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		String result= testFormat(input);
		assertEquals(expected, result);
		result= testFormat(result);
		assertEquals(expected, result);
	}

	/**
	 * [formatting][implementation] comment line length not correctly applied
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46341
	 * Do not wrap.
	 */
	public void testMultiLineCommentLineBreakBeforeImmutableRegions1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "22"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "a <code>test</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting][implementation] comment line length not correctly applied
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46341
	 * Do wrap.
	 */
	public void testMultiLineCommentLineBreakBeforeImmutableRegions2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "21"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "a <code>test</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "a" + DELIMITER + INFIX + "<code>test</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting][implementation] comment line length not correctly applied
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46341
	 * Do not wrap. (Consecutive immutable regions on multiple lines.)
	 */
	public void testMultiLineCommentLineBreakBeforeImmutableRegions3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "a <code>" + DELIMITER + INFIX + "testestestestestestestestestest" + DELIMITER + INFIX + "</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do not insert blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.DO_NOT_INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "Description" + DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do insert blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.TRUE); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "Description"; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do not remove blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.TRUE); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "Description" + DELIMITER + INFIX + DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do remove blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags4() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.DO_NOT_INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT,DefaultCodeFormatterConstants.TRUE); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "Description"; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + DELIMITER + INFIX + postfix;
		String expected= prefix + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do not insert blank line before Javadoc tags
	 * @deprecated
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags5() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.DO_NOT_INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.FALSE); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "Description" + DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do insert blank line before Javadoc tags
	 * @deprecated
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags6() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.TRUE); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "Description"; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do not remove blank line before Javadoc tags
	 * @deprecated
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags7() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.TRUE); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "Description" + DELIMITER + INFIX + DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do remove blank line before Javadoc tags
	 * @deprecated
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags8() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.DO_NOT_INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES,DefaultCodeFormatterConstants.TRUE); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "Description"; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + DELIMITER + INFIX + postfix;
		String expected= prefix + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] javadoc formatter removes blank lines between empty javadoc tags (xdoclet fails)
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68577
	 */
	public void testLineBreaksBetweenEmptyJavaDocTags1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "@custom1" + DELIMITER + INFIX + DELIMITER + INFIX + "@custom2" + DELIMITER + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] javadoc formatter removes blank lines between empty javadoc tags (xdoclet fails)
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68577
	 */
	public void testLineBreaksBetweenEmptyJavaDocTags2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "@custom1" + DELIMITER + INFIX + "@custom2" + DELIMITER + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	public void testNoChange1() {
		String content= PREFIX + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}

	public void testNoFormat1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE);
		String content= PREFIX + DELIMITER + INFIX + "test" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}

	/**
	 * @deprecated
	 */
	public void testNoFormat2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT, DefaultCodeFormatterConstants.FALSE);
		String content= PREFIX + DELIMITER + INFIX + "test" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}


	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testInlineTag1() {
		String input= PREFIX + DELIMITER + INFIX + "{@link Object} has many methods." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testInlineTag2() {
		String input= PREFIX + DELIMITER + INFIX + "{@link Object}s are cool." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineInlineTag1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		final String prefix= PREFIX + DELIMITER + INFIX + "{@link Object}";
		final String postfix= "has many methods." + DELIMITER + POSTFIX;
		String input= prefix + " " + postfix;  //$NON-NLS-1$
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
     * [formatting] Javadoc Formatter mishandles spaces in comments
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
     */
    public void testMultilineInlineTag2() {
    	setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
    	final String prefix= PREFIX + DELIMITER + INFIX + "{@link Objecterr}";
    	final String postfix= "s are cool." + DELIMITER + POSTFIX;
    	String input= prefix + postfix;  //$NON-NLS-1$
    	String expected= prefix + DELIMITER + INFIX + postfix;
    	String result= testFormat(input);
    	assertEquals(expected, result);
    }

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testTagWordbreaks1() {
		String input= PREFIX + DELIMITER + INFIX + "<code>Object</code> rocks." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testTagWordbreaks2() {
		String input= PREFIX + DELIMITER + INFIX + "<code>Object</code>s are cool." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineTagWordbreaks1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "<code>Object</code>";
		String postfix=  "rocks." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String input= prefix + " " + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineTagWordbreaks2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		final String prefix= PREFIX + DELIMITER + INFIX + "Foo";
		final String postfix= "<code>Obj</code>s" + DELIMITER + POSTFIX;
		String input= prefix + " " + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	public void testMultiLineComment() {
		String input= PREFIX + DELIMITER + " TOTO " + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "TOTO" + DELIMITER + POSTFIX; //$NON-NLS-1$
		final String result = testFormat(input);
		assertEquals(expected, result);
	}

	public void testMultiLineComment2() {
		String input= PREFIX + DELIMITER + "TOTO" + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "TOTO" + DELIMITER + POSTFIX; //$NON-NLS-1$
		final String result = testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc formatting: extra newline with [pre]
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52921
	 * <p>
	 * This test only formats once.
	 * </p>
	 */
	public void testNoExtraNewlineWithPre1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, DefaultCodeFormatterConstants.TRUE);
		String input= PREFIX + DELIMITER + INFIX + "<pre>wrap here</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX + "wrap here" + DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$; //$NON-NLS-2$; //$NON-NLS-3$;
		String result= testFormat(input);
		assertEquals(expected, result);

		// now re-format several times
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);

		expected= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX + "wrap here" + DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$; //$NON-NLS-2$; //$NON-NLS-3$;
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc formatting: extra newline with [pre]
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52921
	 * <p>
	 * This test only formats once.
	 * </p>
	 */
	public void testNoExtraNewlineWithPre2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, DefaultCodeFormatterConstants.FALSE);
		String input= PREFIX + DELIMITER + INFIX + "<pre>wrap here</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "<pre>wrap here</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String result= testFormat(input);
		assertEquals(expected, result);

		// now re-format several times
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);

		expected= PREFIX + DELIMITER + INFIX + "<pre>wrap here</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=109605
	public void test109605() {
		String input = "/**" + DELIMITER +
				"			 * <pre>" + DELIMITER +
				"			 * " + DELIMITER +
				"			 * </pre>" + DELIMITER +
				"			 * " + DELIMITER +
				"			 * " + DELIMITER +
				"			 * @author Darren Pearce" + DELIMITER +
				"			 * @version 22-Sep-2005" + DELIMITER +
				"			 * " + DELIMITER +
				"			 */";

		String expected = "/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * " +  DELIMITER +
				" * </pre>" + DELIMITER +
				" * " +  DELIMITER +
				" * " + DELIMITER +
				" * @author Darren Pearce" + DELIMITER +
				" * @version 22-Sep-2005" + DELIMITER +
				" * " + DELIMITER +
				" */";
		String result=testFormat(input);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=60453
	public void test60453() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "80");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE);

		String input = "/** Creates a new instance of DynamicEventChannel  sdf sdfs dsdf dsfsd fd fsd fsdf sdf dsfsd (on the same line)" + DELIMITER +
				"* @pre obj != null" + DELIMITER +
				"*/";

		String expected = "/**" + DELIMITER +
				" * Creates a new instance of DynamicEventChannel sdf sdfs dsdf dsfsd fd fsd fsdf" + DELIMITER +
				" * sdf dsfsd (on the same line)" + DELIMITER +
				" * " + DELIMITER +
				" * @pre obj != null" + DELIMITER +
				" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60453
	 * @deprecated
	 */
	public void test60453_2() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "80");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, DefaultCodeFormatterConstants.FALSE);

		String input = "/** Creates a new instance of DynamicEventChannel  sdf sdfs dsdf dsfsd fd fsd fsdf sdf dsfsd (on the same line)" + DELIMITER +
				"* @pre obj != null" + DELIMITER +
				"*/";

		String expected = "/**" + DELIMITER +
				" * Creates a new instance of DynamicEventChannel sdf sdfs dsdf dsfsd fd fsd fsdf" + DELIMITER +
				" * sdf dsfsd (on the same line)" + DELIMITER +
				" * " + DELIMITER +
				" * @pre obj != null" + DELIMITER +
				" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75460
	public void test75460() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, DefaultCodeFormatterConstants.FALSE);

		String input = "/**" + DELIMITER +
				"<pre>"+ DELIMITER +
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
				"</pre>"+ DELIMITER +
				"*/";

		String expected = "/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * Object[] objects = new Object[3];" + DELIMITER +
				" * objects[0] = new String(&quot;Hallo Welt !!!&quot;);" + DELIMITER +
				" * objects[1] = new String(&quot;Test !!!&quot;);" + DELIMITER +
				" * objects[2] = new Integer(&quot;1980&quot;);" + DELIMITER +
				" * ObjectFile.write(pathname, objects);" + DELIMITER +
				" * Object[] objs = ObjectFile.read(pathname);" + DELIMITER +
				" * for (int i = 0; i &lt; objs.length; i++) {" + DELIMITER +
				" * 	System.out.println(objs[i].toString());" + DELIMITER +
				" * }" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=152850
	public void test152850() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input = "/**\n" +
				" * Any text\n" +
				" * \n" +
				" * @param b\n" +
				" */";
		TextEdit edit = ToolFactory.createCodeFormatter(CommentFormatterUtil.createOptions(options)).format(getCommentKind(), input, 0, input.length(), 0, "\n");
		assertNotNull(edit);
		assertEquals("No edit", 0, edit.getChildrenSize());
	}

	public void test198153() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input = "/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * System.out.println(&#34;hello world&#34;);" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";

		String expected = "/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				// No space after "world".
				" * System.out.println(&quot;hello world&quot;);" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	public void test197169() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input = "/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * &#064;Anno1 class Foo {" + DELIMITER +
				" * &#064;Anno1 class Bar {}" + DELIMITER +
				" * }" + DELIMITER +
				" * &#064;Anno2(&#064;Anno1) class Baz {}" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";

		String expected = "/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				// Initial &#064 left alone.
				" * &#064;Anno1" + DELIMITER +
				" * class Foo {" + DELIMITER +
				// Left alone even after whitespace.
				" * 	&#064;Anno1" + DELIMITER +
				" * 	class Bar {" + DELIMITER +
				" * 	}" + DELIMITER +
				" * }" + DELIMITER +
				" * " + DELIMITER +
				// Non-initial &#064; expanded.
				" * &#064;Anno2(@Anno1)" + DELIMITER +
				" * class Baz {" + DELIMITER +
				" * }" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	public void test109636() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input =
				"/**" + DELIMITER +
				" * <code>" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * setLeadingComment(\"/&#42; traditional comment &#42;/\");  // correct" + DELIMITER +
				" * setLeadingComment(\"missing comment delimiters\");  // wrong" + DELIMITER +
				" * setLeadingComment(\"/&#42; unterminated traditional comment \");  // wrong" + DELIMITER +
				" * setLeadingComment(\"/&#42; broken\\n traditional comment &#42;/\");  // correct" + DELIMITER +
				" * setLeadingComment(\"// end-of-line comment\\n\");  // correct" + DELIMITER +
				" * setLeadingComment(\"// end-of-line comment without line terminator\");  // correct" + DELIMITER +
				" * setLeadingComment(\"// broken\\n end-of-line comment\\n\");  // wrong" + DELIMITER +
				" * </pre>" + DELIMITER +
				" * </code>" + DELIMITER +
				" */";

		String result=testFormat(input, options);
		assertEquals(input, result);
	}

	public void test109636_2() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input =
				"/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * /* Comment ending in multiple stars *&#42;/" + DELIMITER +
				" * /* Entity-needing character after a star *&lt; &#42;/" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";

		String expected =
			"/**" + DELIMITER +
			" * <pre>" + DELIMITER +
			" * /* Comment ending in multiple stars *&#42;/" + DELIMITER +
			" * /* Entity-needing character after a star *&lt; &#42;/" + DELIMITER +
			" * </pre>" + DELIMITER +
			" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	public void test109636_3() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input =
				"/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * /* Comment ending in multiple stars ***&#42;/" + DELIMITER +
				" * /* Entity-needing character after a star *&lt; &#42;/" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";

		String expected =
			"/**" + DELIMITER +
			" * <pre>" + DELIMITER +
			" * /* Comment ending in multiple stars ***&#42;/" + DELIMITER +
			" * /* Entity-needing character after a star *&lt; &#42;/" + DELIMITER +
			" * </pre>" + DELIMITER +
			" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	public void test109636_4() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		String input =
				"/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * setLeadingComment(\"/&#42; traditional comment &#42;/\");  // correct" + DELIMITER +
				" * setLeadingComment(\"missing comment delimiters\");  // wrong" + DELIMITER +
				" * setLeadingComment(\"/&#42; unterminated traditional comment \");  // wrong" + DELIMITER +
				" * setLeadingComment(\"/&#42; broken\\n traditional comment &#42;/\");  // correct" + DELIMITER +
				" * setLeadingComment(\"// end-of-line comment\\n\");  // correct" + DELIMITER +
				" * setLeadingComment(\"// end-of-line comment without line terminator\");  // correct" + DELIMITER +
				" * setLeadingComment(\"// broken\\n end-of-line comment\\n\");  // wrong" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";

		String expected =
				"/**" + DELIMITER +
				" * <pre>" + DELIMITER +
				" * setLeadingComment(&quot;/* traditional comment &#42;/&quot;); // correct" + DELIMITER +
				" * setLeadingComment(&quot;missing comment delimiters&quot;); // wrong" + DELIMITER +
				" * setLeadingComment(&quot;/* unterminated traditional comment &quot;); // wrong" + DELIMITER +
				" * setLeadingComment(&quot;/* broken\\n traditional comment &#42;/&quot;); // correct" + DELIMITER +
				" * setLeadingComment(&quot;// end-of-line comment\\n&quot;); // correct" + DELIMITER +
				" * setLeadingComment(&quot;// end-of-line comment without line terminator&quot;); // correct" + DELIMITER +
				" * setLeadingComment(&quot;// broken\\n end-of-line comment\\n&quot;); // wrong" + DELIMITER +
				" * </pre>" + DELIMITER +
				" */";
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}

	/**
	 * @bug 228652: [formatter] New line inserted while formatting a region of a compilation unit.
	 * @test Insure that no new line is inserted before the formatted region
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228652"
	 */
	public void testBug228652() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

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

		String result = testFormat(input, 62, 19, CodeFormatter.K_JAVA_DOC, options);
		assertEquals(expected, result);
	}
}
