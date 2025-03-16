/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Test class for completion in Javadoc markdown comments
 */
public class JavadocMarkdownCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocMarkdownCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_NUMBERS = new int[] { 58 };
//	TESTS_RANGE = new int[] { 58, 69 };
	TESTS_NAMES = new String[] { "testArrayReference" };
}
public static Test suite() {
	return buildModelTestSuite(JavadocMarkdownCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions("23");
}

public void testArrayReference() throws JavaModelException {
	String source =
		"package javadoc.types;\n" +
		"/// \n" +
		"/// see [#fo]\n" +
		"/// \n" +
		"public class Link {\n" +
		"  	public void foo(String[] args) { }\n" +
		"}\n";
	completeInJavadoc(
			"/Completion/src/javadoc/types/Link.java",
			source,
			true,
			"fo",
			1);
	assertResults(
			"foo[METHOD_REF]{foo(String\\[\\]), Ljavadoc.types.Link;, ([Ljava.lang.String;)V, foo, (args), "+this.positions+R_DRICNRNS+"}"
	);
}
}
