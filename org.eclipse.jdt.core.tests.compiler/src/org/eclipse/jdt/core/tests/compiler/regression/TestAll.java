/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.junit.extension.ExcludedTestSuite;

/**
 * Run all compiler regression tests
 */
public class TestAll extends TestCase {
	public static boolean EXPERT; // an expert doesn't exclude any tests
	static String[] EXCLUDED_TESTS = new String[] {};
public TestAll(String testName) {
	super(testName);
}
/**
 * Adds all the tests in the given class to the suite except
 * the ones that are excluded.
 */
public static void addTest(TestSuite suite, Class testClass) {
	TestSuite innerSuite = new TestSuite(testClass);
	suite.addTest(innerSuite);
}
public static Test suite() {
	TestSuite suite = new TestSuite(TestAll.class.getName());

	String specVersion = System.getProperty("java.specification.version");
	if (!"1.0".equals(specVersion) && !"1.1".equals(specVersion) && !"1.2".equals(specVersion) && !"1.3".equals(specVersion)) {
		addTest(suite, AssertionTest.class);
	}

	addTest(suite, BatchCompilerTest.class);
	addTest(suite, ClassFileComparatorTest.class);
	addTest(suite, ClassFileReaderTest.class);
	addTest(suite, Compliance_1_3.class);	
	addTest(suite, Compliance_1_4.class);	
	addTest(suite, DeprecatedTest.class);
	addTest(suite, LookupTest.class);
	addTest(suite, ProblemConstructorTest.class);
	addTest(suite, UtilTest.class);
	addTest(suite, ScannerTest.class);
		
	if (EXPERT) {
		return new RegressionTestSetup(suite);
	} else {
		return new RegressionTestSetup(new ExcludedTestSuite(suite, EXCLUDED_TESTS));
	}
}
}
