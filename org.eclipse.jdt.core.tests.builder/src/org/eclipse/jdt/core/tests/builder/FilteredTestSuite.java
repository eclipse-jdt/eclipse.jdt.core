package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
public class FilteredTestSuite extends TestSuite {
	public static boolean EXPERT = false;
	String[] excludedTests = null;
/**
 * Creates a new test suite by excluding the given tests
 * from the given test suite if Tests.EXPERT is false.
 */
public FilteredTestSuite(String[] excludedTests) {
	this.excludedTests = excludedTests;
	if(!EXPERT) {
		for (int i = 0; i < excludedTests.length; i+=2) {
			System.out.println("excluded : "+excludedTests[i]+"#"+excludedTests[i+1]);
		}
	}
}
/**
 * Adds a test to the suite.
 */
public void addTest(Test test) {
	if(EXPERT){
		super.addTest(test);
	} else {
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
