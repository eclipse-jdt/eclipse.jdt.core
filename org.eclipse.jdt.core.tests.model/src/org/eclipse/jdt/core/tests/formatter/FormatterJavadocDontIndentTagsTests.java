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
 * 	<li>'Indent Javadoc tags' set to <code>false</code></li>
 * </ul>
 */
public class FormatterJavadocDontIndentTagsTests extends FormatterCommentsTests {

	private static final IPath OUTPUT_FOLDER = new Path("out").append("dont_indent_tags");

public static Test suite() {
	// Get all superclass tests
	TestSuite suite = new Suite(FormatterJavadocDontIndentTagsTests.class.getName());
	List tests = buildTestsList(FormatterJavadocDontIndentTagsTests.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;

}

public FormatterJavadocDontIndentTagsTests(String name) {
	super(name);
}

protected void setUp() throws Exception {
    super.setUp();
}

DefaultCodeFormatter codeFormatter() {
	this.formatterPrefs.comment_indent_root_tags = false; // Eclipse default is true
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(this.formatterPrefs);
	return codeFormatter;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterCommentsTests#getOutputFolder()
 */
IPath getOutputFolder() {
	return OUTPUT_FOLDER;
}

}
