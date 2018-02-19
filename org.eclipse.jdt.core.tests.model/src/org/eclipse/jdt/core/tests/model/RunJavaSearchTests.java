/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.*;
import java.util.*;
import java.util.ArrayList;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunJavaSearchTests extends junit.framework.TestCase {

	public final static List TEST_CLASSES = new ArrayList();
	static {
		// All test suites put in this list should use the same tests projects
		// (eg. JavaSearch and JavaSearch15)
		TEST_CLASSES.add(JavaSearchTests.class);
		TEST_CLASSES.add(JavaSearchGenericTypeTests.class);
		TEST_CLASSES.add(JavaSearchGenericTypeEquivalentTests.class);
		TEST_CLASSES.add(JavaSearchGenericTypeExactTests.class);
		TEST_CLASSES.add(JavaSearchGenericFieldTests.class);
		TEST_CLASSES.add(JavaSearchGenericMethodTests.class);
		TEST_CLASSES.add(JavaSearchGenericMethodExactTests.class);
		TEST_CLASSES.add(JavaSearchGenericMethodEquivalentTests.class);
		TEST_CLASSES.add(JavaSearchGenericConstructorTests.class);
		TEST_CLASSES.add(JavaSearchGenericConstructorExactTests.class);
		TEST_CLASSES.add(JavaSearchGenericConstructorEquivalentTests.class);
		TEST_CLASSES.add(WorkingCopySearchTests.class);
		TEST_CLASSES.add(JavaSearchJavadocTests.class);
		TEST_CLASSES.add(JavaSearchFineGrainTests.class);
	}

	public static Class[] getTestClasses() {
		return (Class[]) TEST_CLASSES.toArray();
	}

	public RunJavaSearchTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunJavaSearchTests.class.getName());

		// Store test classes with same "JavaSearch"project
		AbstractJavaSearchTests.JAVA_SEARCH_SUITES = new ArrayList(TEST_CLASSES);

		// Get all classes
		List allClasses = new ArrayList(TEST_CLASSES);
		allClasses.add(JavaSearchBugsTests.class);
		allClasses.add(JavaSearchBugsTests2.class);
		allClasses.add(JavaSearchBugs8Tests.class);
		allClasses.add(JavaSearchBugs9Tests.class);
		allClasses.add(JavaSearchBugs10Tests.class);
		allClasses.add(JavaSearchMultipleProjectsTests.class);
		allClasses.add(SearchTests.class);
		allClasses.add(JavaSearchScopeTests.class);
		allClasses.add(MatchingRegionsTest.class);
		allClasses.add(JavaIndexTests.class);

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
				Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
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
