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

import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

/**
 * Javadoc formatter test suite with following options changes from the Eclipse
 * default settings:
 * <ul>
 * 	<li>'Indent description after &#064;param' set to <code>false</code></li>
 * </ul>
 */
public class FormatterJavadocDontIndentTagsDescriptionTests extends FormatterJavadocTests {

public static Test suite() {
	// Get all superclass tests
	TestSuite suite = new Suite(FormatterJavadocDontIndentTagsDescriptionTests.class.getName());
	List tests = buildTestsList(FormatterJavadocDontIndentTagsDescriptionTests.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;

}

public FormatterJavadocDontIndentTagsDescriptionTests(String name) {
	super(name);
}

protected void setUp() throws Exception {
    super.setUp();
}

DefaultCodeFormatter codeFormatter() {
	DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
	preferences.comment_indent_parameter_description = false; // Eclipse default is true
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
	return codeFormatter;
}

String getOutputFolder() {
	return "dont_indent_descr";
}

}
