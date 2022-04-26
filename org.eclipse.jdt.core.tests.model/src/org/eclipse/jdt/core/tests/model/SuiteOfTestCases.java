/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.eclipse.test.internal.performance.PerformanceMeterFactory;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A test case class that can be set up (using the setUpSuite() method) and torn down (using the tearDownSuite() method)
 * once for all test cases of this class.
 */
@SuppressWarnings("rawtypes")
public class SuiteOfTestCases extends org.eclipse.jdt.core.tests.junit.extension.TestCase {

	/**
	 * Number of milliseconds that a test case can run for before we consider it to be potentially
	 * deadlocked and dump out a stack trace. Currently set to 5 minutes.
	 */
	private static final long FROZEN_TEST_TIMEOUT_MS = 1000 * 60 * 5;

	/*
	 * A test suite that initialize the test case's fields once, then that copies the values
	 * of these fields into each subsequent test case.
	 */
	public static class Suite extends TestSuite {
		public SuiteOfTestCases currentTestCase;

		/*
		 * Creates a new suite on the given class. This class must be a subclass of SetupableTestSuite.
		 */
		public Suite(Class theClass) {
			super(theClass);
		}
		public Suite(String name) {
			super(name);
		}
		private void initialize(SuiteOfTestCases test) {
			Class currentClass = test.getClass();
			while (currentClass != null && !currentClass.equals(SuiteOfTestCases.class)) {
				Field[] fields = currentClass.getDeclaredFields();
				for (int i = 0, length = fields.length; i < length; i++) {
					Field field = fields[i];

					// skip static and final fields
					int modifiers = field.getModifiers();
					if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) continue;

					// make the field accessible
					field.setAccessible(true);

					try {
						Object value = field.get(this.currentTestCase);
						field.set(test, value);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				currentClass = currentClass.getSuperclass();
			}
		}
		public void run(final TestResult result) {
			Protectable p= new Protectable() {
				public void protect() throws Exception {
					try {
						// run suite (first test run will setup the suite)
						superRun(result);
					} finally {
						// tear down the suite
						if (Suite.this.currentTestCase != null) { // protect against empty test suite
							Suite.this.currentTestCase.tearDownSuite();
						}
					}
				}
			};
			result.runProtected(this, p);
		}
		public void superRun(TestResult result) {
			super.run(result);
		}
		public void runTest(Test test, TestResult result) {
			SuiteOfTestCases current = (SuiteOfTestCases)test;
			if (this.currentTestCase == null) {
				// setup suite
				try {
					current.setUpSuite();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// copy the values of the previous current test case's fields into the current one
				initialize(current);
			}
			try {
				super.runTest(test, result);
			} finally {
				// make current
				this.currentTestCase = current;
			}
		}
	}

	public SuiteOfTestCases(String name) {
		super(name);
	}

	/**
	 * Setup the test suite once before all test cases run.
	 */
	public void setUpSuite() throws Exception {
		// Indexer is disabled for tests by defailt, see
		// org.eclipse.jdt.core.tests.junit.extension.TestCase.isIndexDisabledForTest()

		// The first individual setup() call will also call done()
		FreezeMonitor.expectCompletionIn(FROZEN_TEST_TIMEOUT_MS);
	}

	/**
	 * Tear down the test suite once after all test cases have run.
	 */
	public void tearDownSuite() throws Exception {
		// Indexer is disabled for tests by defailt, see
		// org.eclipse.jdt.core.tests.junit.extension.TestCase.isIndexDisabledForTest()

		// Just to be symmetrical to setup(), actually this shouldn't be needed
		// if we have at least one test
		FreezeMonitor.done();
	}

	@Override
	protected void setUp() throws Exception {
		FreezeMonitor.expectCompletionIn(FROZEN_TEST_TIMEOUT_MS);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		FreezeMonitor.done();
		super.tearDown();
	}

	/**
	 * Decorate an individual test with setUpSuite/tearDownSuite, so that the test can be run standalone.
	 * This method is called by the Eclipse JUnit test runner when a test is re-run from the JUnit view's context menu.
	 */
	public static Test setUpTest(Test test) {
		if (!(test instanceof SuiteOfTestCases))
			return test;

		final SuiteOfTestCases suiteOfTestCases = (SuiteOfTestCases) test;
		return new TestSetup(test) {
			protected void setUp() throws Exception {
				// reset the PerformanceMeterFactory, so that the same scenario can be run again:
				Field field = PerformanceMeterFactory.class.getDeclaredField("fScenarios");
				field.setAccessible(true);
				Set set = (Set) field.get(null);
				set.clear();

				suiteOfTestCases.setUpSuite();
			}

			protected void tearDown() throws Exception {
				suiteOfTestCases.tearDownSuite();
			}
		};
	}

}
