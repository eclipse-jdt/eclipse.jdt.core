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
package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.CleanupAfterSuiteTests;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunAllTests extends junit.framework.TestCase {
public RunAllTests(String name) {
	super(name);
}
public static Class[] getAllTestClasses() {
	return new Class[] {
		RunConverterTests.class,
		ASTTest.class,
		ASTVisitorTest.class,
		ASTMatcherTest.class,
		ASTStructuralPropertyTest.class,
		ASTParserTest.class,
		ASTModelBridgeTests.class,
		BatchASTCreationTests.class,
		CompatibilityRulesTests.class,
		org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTest.class,
		org.eclipse.jdt.core.tests.rewrite.modifying.ASTRewritingModifyingTest.class,
		ASTPositionsTest.class,
		ASTNodeFinderTest.class,
		org.eclipse.jdt.core.tests.dom.APIDocumentationTests.class,

		// should always be the last one, to cleanup environment after messy tests
		CleanupAfterSuiteTests.class
	};
}
public static Test suite() {
	TestSuite ts = new TestSuite(RunAllTests.class.getName());

	Class[] testClasses = getAllTestClasses();
	// Reset forgotten subsets of tests
	TestCase.TESTS_PREFIX = null;
	TestCase.TESTS_NAMES = null;
	TestCase.TESTS_NUMBERS = null;
	TestCase.TESTS_RANGE = null;
	TestCase.RUN_ONLY_ID = null;

	for (int i = 0; i < testClasses.length; i++) {
		Class testClass = testClasses[i];

		// call the suite() method and add the resulting suite to the suite
		try {
			Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
			Test suite = (Test)suiteMethod.invoke(null, new Object[0]);
			ts.addTest(suite);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	return ts;
}
}
