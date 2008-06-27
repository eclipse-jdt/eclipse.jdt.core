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
package org.eclipse.jdt.core.tests.formatter.comment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.tests.model.SuiteOfTestCases;

public abstract class CommentTestCase extends SuiteOfTestCases {
	public static Test buildTestSuite(Class evaluationTestClass) {
		return buildTestSuite(evaluationTestClass, null); //$NON-NLS-1$
	}

	public static Test buildTestSuite(Class evaluationTestClass, String suiteName) {
		TestSuite suite = new Suite(suiteName==null?evaluationTestClass.getName():suiteName);
		List tests = buildTestsList(evaluationTestClass);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	public static final String DELIMITER= TextUtilities.getDefaultLineDelimiter(new Document());

	private Map userOptions;

	protected CommentTestCase(String name) {
		super(name);
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		this.userOptions= null;
	}

	protected abstract int getCommentKind();

	protected Map getUserOptions() {
		return this.userOptions;
	}

	protected void setUserOption(String name, String value) {
		if (this.userOptions == null)
			this.userOptions= new HashMap();

		this.userOptions.put(name, value);
	}

	protected void setUserOption(Map options) {
		if (this.userOptions == null) {
			this.userOptions= options;
		} else {
			this.userOptions.putAll(options);
		}
	}

	protected final String testFormat(String text) {
		return testFormat(text, 0, text.length());
	}

	protected final String testFormat(String text, Map options) {
		return testFormat(text, 0, text.length(), getCommentKind(), options);
	}
	protected String testFormat(String text, int offset, int length) {
		return testFormat(text, offset, length, getCommentKind());
	}

	protected String testFormat(String text, int offset, int length, int kind) {
		return testFormat(text, offset, length, kind, getUserOptions());
	}

	protected String testFormat(String text, int offset, int length, int kind, Map options) {
		assertNotNull(text);
		assertTrue(offset >= 0);
		assertTrue(offset < text.length());
		assertTrue(length >= 0);
		assertTrue(offset + length <= text.length());

		assertTrue(kind == CodeFormatter.K_JAVA_DOC || kind == CodeFormatter.K_MULTI_LINE_COMMENT || kind == CodeFormatter.K_SINGLE_LINE_COMMENT);

		return CommentFormatterUtil.format(kind, text, offset, length, CommentFormatterUtil.createOptions(options));
	}
	protected String testFormat(String text, int offset, int length, int kind, int indentationLevel) {
		assertNotNull(text);
		assertTrue(offset >= 0);
		assertTrue(offset < text.length());
		assertTrue(length >= 0);
		assertTrue(offset + length <= text.length());

		assertTrue(kind == CodeFormatter.K_JAVA_DOC || kind == CodeFormatter.K_MULTI_LINE_COMMENT || kind == CodeFormatter.K_SINGLE_LINE_COMMENT);

		return CommentFormatterUtil.format(kind, text, offset, length, indentationLevel, CommentFormatterUtil.createOptions(getUserOptions()));
	}

}
