package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A test case class that can be set up (using the setUpSuite() method) and tore down (using the teardDownSuite() method)
 * once for all test cases of this class.
 */
public class SuiteOfTestCases extends TestCase {
		
	/*
	 * A test suite that initialize the test case's fields once, then that copies the values
	 * of these fields intto each subsequent test case.
	 */
	protected static class Suite extends TestSuite {
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
					
					// skip static fields
					if (Modifier.isStatic(field.getModifiers())) continue;
					
					// make the field accessible
					field.setAccessible(true);
					
					try {
						Object value = field.get(this.currentTestCase);
						field.set(test, value);
					} catch (IllegalAccessException e) {
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
						Suite.super.run(result);
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
				this.initialize(current);
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
	}
	
	/**
	 * Tear down the test suite once after all test cases have run.
	 */
	public void tearDownSuite() throws Exception {
	}
}
