package org.eclipse.jdt.core.tests.compiler.regression.latest;

import java.util.ArrayList;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.model.CompletionTests13;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestAll extends junit.framework.TestCase {

public TestAll(String testName) {
	super(testName);
}
public static Test suite() {

	// Common test suites
	ArrayList standardTests = new ArrayList();
	standardTests.add(TextBlockTest.class);
	standardTests.add(CompletionTests13.class);
	TestSuite all = new TestSuite(TestAll.class.getName());
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_13) != 0) {
		ArrayList tests_13 = (ArrayList)standardTests.clone();
		TestCase.resetForgottenFilters(tests_13);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.getComplianceLevelForJavaVersion(ClassFileConstants.MAJOR_VERSION_13), tests_13));
	}
	return all;
}
}
