package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.core.tests.junit.extension.*;

import junit.framework.*;

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
		
	if (EXPERT) {
		return new RegressionTestSetup(suite);
	} else {
		return new RegressionTestSetup(new ExcludedTestSuite(suite, EXCLUDED_TESTS));
	}
}
}
