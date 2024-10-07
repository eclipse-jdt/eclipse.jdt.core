/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Jesper S Moller - Contribution for bug 402173
 *                       Contribution for bug 402892
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] follow up bug for comments - https://bugs.eclipse.org/458208
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.formatter.*;
import org.eclipse.jdt.core.tests.formatter.comment.CommentsTestSuite;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.CleanupAfterSuiteTests;

/**
 * Runs all formatter tests.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RunFormatterTests extends junit.framework.TestCase {

	public final static List TEST_SUITES = new ArrayList();
	static {
		TEST_SUITES.add(FormatterCommentsBugsTest.class);
		TEST_SUITES.add(FormatterCommentsTests.class);
		TEST_SUITES.add(FormatterCommentsClearBlankLinesTests.class);
		TEST_SUITES.add(FormatterJavadocDontIndentTagsTests.class);
		TEST_SUITES.add(FormatterJavadocDontIndentTagsDescriptionTests.class);
		TEST_SUITES.add(FormatterOldBugsGistTests.class);

		// should always be the last one, to cleanup environment after messy tests
		TEST_SUITES.add(CleanupAfterSuiteTests.class);
	}

	public static Class[] getTestClasses() {
		return (Class[]) TEST_SUITES.toArray();
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunFormatterTests.class.getName());

		// Store test classes with same "JavaSearch"project
		FormatterCommentsTests.ALL_TEST_SUITES = new ArrayList(TEST_SUITES);

		// Get all classes
		List allClasses = new ArrayList();
		String type = System.getProperty("type");
		if (type == null || !type.equals("javadoc")) {
			allClasses.add(FormatterRegressionTests.class);
			allClasses.add(FormatterBugsTests.class);
		}
		allClasses.add(CommentsTestSuite.class);
		allClasses.add(FormatterJSR335Tests.class);
		allClasses.add(FormatterJSR308Tests.class);
		allClasses.add(FormatterBugs18Tests.class);
		allClasses.addAll(TEST_SUITES);

		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;

		// Add all tests suite of tests
		for (int i = 0, size = allClasses.size(); i < size; i++) {
			Class testClass = (Class) allClasses.get(i);

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

public RunFormatterTests(String name) {
	super(name);
}
}

