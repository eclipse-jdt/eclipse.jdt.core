/*******************************************************************************
 * Copyright (c) 2015 Mateusz Matela and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FormatterOldBugsGistTests extends FormatterRegressionTests {

	private static String lineSeparator = System.getProperty("line.separator");

	public FormatterOldBugsGistTests(String name) {
		super(name);
	}

	/**
	 * @deprecated
	 */
	private void format(String testName, String source, String expectedResult, Map options) {
		org.eclipse.jdt.internal.formatter.old.CodeFormatter formatter = new org.eclipse.jdt.internal.formatter.old.CodeFormatter(options);
		String result = formatter.format(source, 0, null, null);
		assertSourceEquals(" Wrong Format\n" /*nonNLS*/, expectedResult,result, false);
	}

	public static Test suite() {
		return buildTestSuite(FormatterOldBugsGistTests.class);
	}

	public void testComments() {
		String source =
			"/* This is a long comment added just to test whether the formmatting\n" +
			"* for the long comment is handled properly without change in behaviour\n" +
			" */\n" +
			"public void foo() {\n" +
			"//line\tcomment\n" +
			"bar();/*block comment*/\n" +
			"}";
		String expectedResult =
			"/* This is a long comment added just to test whether the formmatting" + lineSeparator +
			"* for the long comment is handled properly without change in behaviour" + lineSeparator +
			" */" + lineSeparator +
			"public void foo() {" + lineSeparator +
			"\t//line\tcomment" + lineSeparator +
			"\tbar();/*block comment*/" + lineSeparator +
			"}";
		format("testComments", source, expectedResult, null);
	}

	/**
	 * @deprecated
	 */
	public void testCode() {
		String source =
		"public class X {" + lineSeparator +
		"public void foo(String s1, String s2, String s3, String s4) throws Exception {"  + lineSeparator +
		"}"  + lineSeparator +
		"static final String[]" + lineSeparator +
		"s11 = new String[]{\"s1\"} ," + lineSeparator +
		"s12 = new String[]{\"s2\"} ," + lineSeparator +
		"s13 = new String[]{\"s3\"} ;" +
		"}";
		String expectedResult =
		"public class X {" + lineSeparator +
		"\tpublic void foo(String s1, String s2, String s3, String s4)" + lineSeparator +
		"\t\tthrows Exception {"  + lineSeparator +
		"\t}"  + lineSeparator +
		"\tstatic final String[] s11 = new String[] { \"s1\" }," + lineSeparator +
		"\t\ts12 = new String[] { \"s2\" }," + lineSeparator +
		"\t\ts13 = new String[] { \"s3\" };"+ lineSeparator +
		"}";
		Map options =JavaCore.getOptions();
		options.put(JavaCore.FORMATTER_LINE_SPLIT, "80");
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		format("testCode", source, expectedResult, options);
	}
}
