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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;

/**
 * Javadoc formatter test suite with following options changes from the Eclipse
 * default settings:
 * <ul>
 * 	<li>'Remove blank lines' set to <code>true</code></li>
 * 	<li>'Enable header comment formatting' set to <code>true</code></li>
 * </ul>
 * options activated.
 */
@SuppressWarnings("rawtypes")
public class FormatterCommentsClearBlankLinesTests extends FormatterCommentsTests {

private static final IPath OUTPUT_FOLDER = new Path("out").append("clear_blank_lines");

public static Test suite() {
	// Get all superclass tests
	TestSuite suite = new Suite(FormatterCommentsClearBlankLinesTests.class.getName());
	List tests = buildTestsList(FormatterCommentsClearBlankLinesTests.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;
}

public FormatterCommentsClearBlankLinesTests(String name) {
	super(name);
}

@Override
protected void setUp() throws Exception {
    super.setUp();
}

@Override
DefaultCodeFormatter codeFormatter() {
	this.formatterPrefs.comment_clear_blank_lines_in_block_comment = true;
	this.formatterPrefs.comment_clear_blank_lines_in_javadoc_comment = true;
	this.formatterPrefs.comment_format_header = true;
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(this.formatterPrefs);
	return codeFormatter;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterCommentsTests#getOutputFolder()
 */
@Override
IPath getOutputFolder() {
	return OUTPUT_FOLDER;
}
}
