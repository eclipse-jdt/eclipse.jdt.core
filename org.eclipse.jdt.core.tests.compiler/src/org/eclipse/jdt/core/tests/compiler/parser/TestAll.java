/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.junit.extension.ExcludedTestSuite;

/**
 * Run all parser regression tests
 */
public class TestAll extends TestCase {
	public static boolean EXPERT; // an expert doesn't exclude any tests
	public static String[] EXCLUDED_TESTS = new String[] {
		"CompletionParserTest", "testVB_2", // completion on field access on anonymous inner class with syntax error
		"CompletionParserTest", "testVB_4", // completion on field access on anonymous inner class with syntax error
		"CompletionParserTest", "testVB_5", // completion on field access on anonymous inner class with syntax error
		"LabelStatementCompletionTest", "testInLabeledInnerClassWithErrorBefore", // cannot get labels in recovery mode yet
	};
/**
 * TestAll constructor comment.
 * @param testName java.lang.String
 */
public TestAll(String testName) {
	super(testName);
}
/**
 * Adds all the tests in the given class to the suite except
 * the ones that are excluded.
 */
public static void addTest(TestSuite suite, Class testClass) {
	TestSuite innerSuite = new TestSuite(testClass);
	suite.addTest(innerSuite);
}
public static Test suite() {
	TestSuite suite = new TestSuite(TestAll.class.getName());
	
	/* completion tests */
	addTest(suite, AllocationExpressionCompletionTest.class);
	addTest(suite, ClassLiteralAccessCompletionTest.class);
	addTest(suite, CompletionParserTest.class);
	addTest(suite, CompletionRecoveryTest.class);
	addTest(suite, DietCompletionTest.class);
	addTest(suite, ExplicitConstructorInvocationCompletionTest.class);
	addTest(suite, FieldAccessCompletionTest.class);
	addTest(suite, InnerTypeCompletionTest.class);
	addTest(suite, LabelStatementCompletionTest.class);
	addTest(suite, MethodInvocationCompletionTest.class);
	addTest(suite, NameReferenceCompletionTest.class);
	addTest(suite, ReferenceTypeCompletionTest.class);
	addTest(suite, CompletionParserTest2.class);

	/* selection tests */
	addTest(suite, ExplicitConstructorInvocationSelectionTest.class);
	addTest(suite, SelectionTest.class);

	/* recovery tests */
	addTest(suite, DietRecoveryTest.class);

	/* source element parser tests */
	addTest(suite, SourceElementParserTest.class);

	/* syntax error diagnosis tests */
	addTest(suite, SyntaxErrorTest.class);

	if (EXPERT) {
		return suite;
	} else {
		return new ExcludedTestSuite(suite, EXCLUDED_TESTS);
	}
}
}
