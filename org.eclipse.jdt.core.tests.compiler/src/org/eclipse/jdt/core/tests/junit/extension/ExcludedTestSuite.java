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
package org.eclipse.jdt.core.tests.junit.extension;

import junit.framework.*;
public class ExcludedTestSuite extends TestSuite {
	String[] excludedTests = null;
/**
 * Creates a new test suite by excluding the given tests
 * from the given test suite.
 */
public ExcludedTestSuite(Test suite, String[] excludedTests) {
	this.excludedTests = excludedTests;
	this.addTest(suite);
}
/**
 * Adds a test to the suite.
 */
public void addTest(Test test) {
	if (test instanceof TestCase) {
		if (!isExcluded((TestCase)test)) {
			super.addTest(test);
		}
	} else if (test instanceof TestSuite) {
		java.util.Enumeration enum = ((TestSuite)test).tests();
		while (enum.hasMoreElements()) {
			this.addTest((Test)enum.nextElement());
		}
	}
}
/**
 * Returns whether the test is excluded.
 */
public boolean isExcluded(TestCase test) {
	if (this.excludedTests == null) {
		return false;
	}
	for (int i = 0; i < this.excludedTests.length; i += 2) {
		String className = this.excludedTests[i];
		String methodName = this.excludedTests[i+1];
		if (test.getName().equals(methodName) &&
			test.getClass().getName().endsWith(className)) {
				return true;
		}
	}
	return false;
}
}
