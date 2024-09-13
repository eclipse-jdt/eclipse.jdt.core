/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;

import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class RecursivelyFilterableTestSuite extends TestSuite implements Filterable {
	public SuiteOfTestCases currentTestCase;
	private Vector<Test> filteredTests = null;

	/*
	 * Creates a new suite on the given class. This class must be a subclass of
	 * SetupableTestSuite.
	 */
	public RecursivelyFilterableTestSuite(Class theClass) {
		super(theClass);
	}

	public RecursivelyFilterableTestSuite(String name) {
		super(name);
	}

	public void addTest(Test test) {
		super.addTest(test);
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		Vector v1 = new Vector(10);
		Enumeration<Test> en = super.tests();
		while (en.hasMoreElements()) {
			Test t = en.nextElement();
			if (filter.shouldRun(makeDescription(t))) {
				Test recursed = filterRecurse(filter, t);
				v1.add(recursed);
			}
		}
		this.filteredTests = v1;
	}

    public Test filterRecurse(Filter filter, Test toTest) throws NoTestsRemainException {
        if (toTest instanceof Filterable) {
            Filterable adapter = (Filterable) toTest;
            adapter.filter(filter);
        } else if (toTest instanceof TestSuite) {
            TestSuite suite = (TestSuite) toTest;
            TestSuite filtered = new TestSuite(suite.getName());
            int n = suite.testCount();
            for (int i = 0; i < n; i++) {
                Test test = suite.testAt(i);
                if (filter.shouldRun(makeDescription(test))) {
                    filtered.addTest(test);
                }
            }
            if (filtered.testCount() == 0) {
                throw new NoTestsRemainException();
            }
            return filtered;
        }
        return toTest;
    }

	public int countTestCases() {
		if (this.filteredTests == null) {
			return super.countTestCases();
		}
		int count = 0;
		for (Test each : this.filteredTests) {
			count += each.countTestCases();
		}
		return count;
	}

	/**
	 * Returns the test at the given index.
	 */
	public Test testAt(int index) {
		return this.filteredTests == null ? super.testAt(index) : this.filteredTests.get(index);
	}

	/**
	 * Returns the number of tests in this suite.
	 */
	public int testCount() {
		return this.filteredTests == null ? super.testCount() : this.filteredTests.size();
	}

	/**
	 * Returns the tests as an enumeration.
	 */
	public Enumeration<Test> tests() {
		return this.filteredTests == null ? super.tests() : this.filteredTests.elements();
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

	private static Annotation[] getAnnotations(TestCase test) {
		String methName = test.getName();
		if (test instanceof org.eclipse.jdt.core.tests.junit.extension.TestCase) {
			methName = ((org.eclipse.jdt.core.tests.junit.extension.TestCase) test).methodName;
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

	private static Description makeDescription(Test test) {
		if (test instanceof TestCase) {
			TestCase tc = (TestCase) test;
			return Description.createTestDescription(tc.getClass(), tc.getName(), getAnnotations(tc));
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

	private static String createSuiteDescription(TestSuite ts) {
		int count = ts.countTestCases();
		String example = count == 0 ? "" : String.format(" [example: %s]", ts.testAt(0));
		return String.format("TestSuite with %s tests%s", count, example);
	}

}
