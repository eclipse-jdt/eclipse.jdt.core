/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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

/**
 * Runs all formatter tests.
 */
public class RunFormatterTests extends junit.framework.TestCase {

	public final static List TEST_SUITES = new ArrayList();
	static {
		TEST_SUITES.add(FormatterCommentsBugsTest.class);
		TEST_SUITES.add(FormatterCommentsTests.class);
		TEST_SUITES.add(FormatterCommentsClearBlankLinesTests.class);
		TEST_SUITES.add(FormatterJavadocDontIndentTagsTests.class);
		TEST_SUITES.add(FormatterJavadocDontIndentTagsDescriptionTests.class);
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

