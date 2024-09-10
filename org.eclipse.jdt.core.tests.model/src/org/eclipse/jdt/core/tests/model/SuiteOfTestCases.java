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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import junit.extensions.TestDecorator;
import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.test.internal.performance.PerformanceMeterFactory;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;

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
	public static class Suite extends TestSuite implements Filterable {
		public SuiteOfTestCases currentTestCase;
	    private Vector<Test> filteredTests = null;


		/*
		 * Creates a new suite on the given class. This class must be a subclass of SetupableTestSuite.
		 */
		public Suite(Class theClass) {
			super(theClass);
		}
		public Suite(String name) {
			super(name);
		}


	    public void addTest(Test test) {
	    	super.addTest(test);
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
						e.printStackTrace(System.out);
						throw new IllegalStateException(e);
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
						superOrFilteredRun(result);
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

		public void superOrFilteredRun(TestResult result) {
			if( filteredTests != null ) {
		        for (Test each : filteredTests) {
		            if (result.shouldStop()) {
		                break;
		            }
		            runTest(each, result);
		        }
			} else {
				superRun(result);
			}
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
					e.printStackTrace(System.out);
					throw new IllegalStateException(e);
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
		@Override
		public void filter(Filter filter) throws NoTestsRemainException {
			Vector v1 = new Vector(10);
			Enumeration<Test> en = super.tests();
			while(en.hasMoreElements()) {
				Test t = en.nextElement();
                if (filter.shouldRun(makeDescription(t))) {
                	v1.add(t);
                }
			}
			filteredTests = v1;
		}

	    public int countTestCases() {
	    	if( filteredTests == null ) {
	    		return super.countTestCases();
	    	}
	        int count = 0;
	        for (Test each : filteredTests) {
	            count += each.countTestCases();
	        }
	        return count;
	    }

	    /**
	     * Returns the test at the given index.
	     */
	    public Test testAt(int index) {
	        return filteredTests == null ? super.testAt(index) : filteredTests.get(index);
	    }

	    /**
	     * Returns the number of tests in this suite.
	     */
	    public int testCount() {
	        return filteredTests == null ? super.testCount() : filteredTests.size();
	    }

	    /**
	     * Returns the tests as an enumeration.
	     */
	    public Enumeration<Test> tests() {
	        return filteredTests == null ? super.tests() : filteredTests.elements();
	    }

	    private static Description makeDescription(Test test) {
	        if (test instanceof TestCase) {
	            TestCase tc = (TestCase) test;
	            return Description.createTestDescription(tc.getClass(), tc.getName(),
	                    getAnnotations(tc));
	        } else if (test instanceof TestSuite) {
	            TestSuite ts = (TestSuite) test;
	            String name = ts.getName() == null ? createSuiteDescription(ts) : ts.getName();
	            Description description = Description.createSuiteDescription(name);
	            int n = ts.testCount();
	            for (int i = 0; i < n; i++) {
	                Description made = makeDescription(ts.testAt(i));
	                description.addChild(made);
	            }
	            return description;
	        } else if (test instanceof Describable) {
	            Describable adapter = (Describable) test;
	            return adapter.getDescription();
	        } else if (test instanceof TestDecorator) {
	            TestDecorator decorator = (TestDecorator) test;
	            return makeDescription(decorator.getTest());
	        } else {
	            // This is the best we can do in this case
	            return Description.createSuiteDescription(test.getClass());
	        }
	    }
	    private static Annotation[] getAnnotations(TestCase test) {
	    	String methName = test.getName();
	    	if( test instanceof org.eclipse.jdt.core.tests.junit.extension.TestCase ) {
	    		methName = ((org.eclipse.jdt.core.tests.junit.extension.TestCase)test).methodName;
	    	}
	        try {
	            Method m = test.getClass().getMethod(methName);
	            Annotation[] ret = m.getDeclaredAnnotations();
	            return ret;
	        } catch (SecurityException e) {
	        } catch (NoSuchMethodException e) {
	        }
	        return new Annotation[0];
	    }
	}

    private static String createSuiteDescription(TestSuite ts) {
        int count = ts.countTestCases();
        String example = count == 0 ? "" : String.format(" [example: %s]", ts.testAt(0));
        return String.format("TestSuite with %s tests%s", count, example);
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
