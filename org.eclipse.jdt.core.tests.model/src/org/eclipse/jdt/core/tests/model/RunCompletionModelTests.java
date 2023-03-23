/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gábor Kövesdán - Contribution for Bug 350000 - [content assist] Include non-prefix matches in auto-complete suggestions
 *     Jesper Steen Møller - Contributions for bug 529552 - [18.3] Add 'var' in completions
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunCompletionModelTests extends junit.framework.TestCase {

	protected static final boolean ONLY_JAVADOC = "true".equals(System.getProperty("onlyJavadoc", "false"));

	public final static List COMPLETION_SUITES = new ArrayList();
	static {
		if (!ONLY_JAVADOC) {
			COMPLETION_SUITES.add(CompletionTests.class);
			COMPLETION_SUITES.add(CompletionTests2.class);
			COMPLETION_SUITES.add(CompletionTests3.class);
			COMPLETION_SUITES.add(CompletionTests_1_5.class);
			COMPLETION_SUITES.add(CompletionTests18.class);
			COMPLETION_SUITES.add(CompletionTests9.class);
			COMPLETION_SUITES.add(CompletionTests10.class);
			COMPLETION_SUITES.add(CompletionTests11.class);
			COMPLETION_SUITES.add(CompletionTests14.class);
			COMPLETION_SUITES.add(CompletionTests16_1.class);
			COMPLETION_SUITES.add(CompletionTests16_2.class);
			COMPLETION_SUITES.add(CompletionTests17.class);
			COMPLETION_SUITES.add(CompletionTestsForRecordPattern.class);
			COMPLETION_SUITES.add(CompletionContextTests.class);
			COMPLETION_SUITES.add(CompletionContextTests_1_5.class);
			COMPLETION_SUITES.add(CompletionWithMissingTypesTests.class);
			COMPLETION_SUITES.add(CompletionWithMissingTypesTests2.class);
			COMPLETION_SUITES.add(CompletionWithMissingTypesTests_1_5.class);
			COMPLETION_SUITES.add(CompletionWithMissingTypesTests_1_8.class);
			COMPLETION_SUITES.add(SnippetCompletionContextTests.class);
			COMPLETION_SUITES.add(SubstringCompletionTests.class);
			COMPLETION_SUITES.add(SubwordCompletionTests.class);
		}
		COMPLETION_SUITES.add(JavadocTypeCompletionModelTest.class);
		COMPLETION_SUITES.add(JavadocFieldCompletionModelTest.class);
		COMPLETION_SUITES.add(JavadocMethodCompletionModelTest.class);
		COMPLETION_SUITES.add(JavadocPackageCompletionModelTest.class);
		COMPLETION_SUITES.add(JavadocTextCompletionModelTest.class);
		COMPLETION_SUITES.add(JavadocBugsCompletionModelTest.class);
		COMPLETION_SUITES.add(JavadocCompletionContextTests.class);
		COMPLETION_SUITES.add(JavadocModuleCompletionTests15.class);
		COMPLETION_SUITES.add(JavadocCompletionContextTests_1_5.class);
	}

	public static Class[] getTestClasses() {
		int size = COMPLETION_SUITES.size();
		if (!ONLY_JAVADOC) {
			Class[] testClasses = new Class[size+2];
			COMPLETION_SUITES.toArray(testClasses);
			testClasses[size] = SnippetCompletionTests.class;
			testClasses[size+1] = SnippetCompletionTests_1_5.class;
			return testClasses;
		}
		Class[] testClasses = new Class[size];
		COMPLETION_SUITES.toArray(testClasses);
		return testClasses;
	}

	public RunCompletionModelTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunCompletionModelTests.class.getName());

		// Store test classes with same "Completion"project
		AbstractJavaModelCompletionTests.COMPLETION_SUITES = new ArrayList(COMPLETION_SUITES);

		// Get all classes
		Class[] allClasses = getTestClasses();

		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;

		// Add all tests suite of tests
		for (int i = 0, length = allClasses.length; i < length; i++) {
			Class testClass = allClasses[i];

			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
				ts.addTest(suite);
				if (suite.countTestCases()== 0) {
					AbstractJavaModelCompletionTests.COMPLETION_SUITES.remove(testClass);
				}
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
