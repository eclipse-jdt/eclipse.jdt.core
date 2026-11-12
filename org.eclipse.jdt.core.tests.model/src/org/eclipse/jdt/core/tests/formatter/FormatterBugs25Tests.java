/*******************************************************************************
 * Copyright (c) 2026 Kichwa Coders Canada, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import junit.framework.Test;

public class FormatterBugs25Tests extends FormatterRegressionTests {

public static Test suite() {
	return buildModelTestSuite(FormatterBugs25Tests.class);
}

public FormatterBugs25Tests(String name) {
	super(name);
}

/**
 * Create project and set the jar placeholder.
 */
@Override
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterBugs", "25"); //$NON-NLS-1$
	}
	super.setUpSuite();
}

// Test for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5571
public void testIssue5571() throws Exception {
	String source ="""
class Test {
	/// * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
	///// XXXXX
	public void test() {
	}
}
			""";
	String expected ="""
class Test {
	/// * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
	///// XXXXX
	public void test() {
	}
}
			""";

	formatSource(source,expected);
}

}
