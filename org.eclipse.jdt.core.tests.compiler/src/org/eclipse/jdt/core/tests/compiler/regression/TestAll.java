/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all compiler regression tests
 */
public class TestAll extends junit.framework.TestCase {

public TestAll(String testName) {
	super(testName);
}
public static Test suite() {
	ArrayList standardTests = new ArrayList();
//	standardTests.addAll(JavadocTest.allTestClasses);
	standardTests.add(ArrayTest.class);
	standardTests.add(AssignmentTest.class);
	standardTests.add(BooleanTest.class);
	standardTests.add(CastTest.class);
	standardTests.add(ClassFileComparatorTest.class);
	standardTests.add(CollisionCase.class);
	standardTests.add(ConstantTest.class);
	standardTests.add(DeprecatedTest.class);
	standardTests.add(LocalVariableTest.class);
	standardTests.add(LookupTest.class);
	standardTests.add(NumericTest.class);
	standardTests.add(ProblemConstructorTest.class);
	standardTests.add(ScannerTest.class);
	standardTests.add(SwitchTest.class);
	standardTests.add(TryStatementTest.class);
	standardTests.add(UtilTest.class);
	standardTests.add(XLargeTest.class);
	standardTests.add(InternalScannerTest.class);
	standardTests.add(ConditionalExpressionTest.class);
	standardTests.add(ExternalizeStringLiteralsTest.class);
	standardTests.add(NonFatalErrorTest.class);
	standardTests.add(FlowAnalysisTest.class);
	standardTests.add(CharOperationTest.class);
	standardTests.add(RuntimeTests.class);
	standardTests.add(DebugAttributeTest.class);
	standardTests.add(NullReferenceTest.class);
	standardTests.add(CompilerInvocationTests.class);
	
	// add all javadoc tests
	for (int i=0, l=JavadocTest.ALL_CLASSES.size(); i<l; i++) {
		standardTests.add(JavadocTest.ALL_CLASSES.get(i));
	}

	TestSuite all = new TestSuite(TestAll.class.getName());
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_3) != 0) {
		ArrayList tests_1_3 = (ArrayList)standardTests.clone();
		tests_1_3.add(Compliance_1_3.class);
		tests_1_3.add(JavadocTest_1_3.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_3, RegressionTestSetup.class, tests_1_3));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_4) != 0) {
		ArrayList tests_1_4 = (ArrayList)standardTests.clone();
		tests_1_4.add(AssertionTest.class);
		tests_1_4.add(Compliance_1_4.class);
		tests_1_4.add(ClassFileReaderTest_1_4.class);
		tests_1_4.add(JavadocTest_1_4.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_4, RegressionTestSetup.class, tests_1_4));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_5) != 0) {
		ArrayList tests_1_5 = (ArrayList)standardTests.clone();
		tests_1_5.addAll(RunComparableTests.ALL_CLASSES);
		tests_1_5.add(AssertionTest.class);
		tests_1_5.add(ClassFileReaderTest_1_5.class);
		tests_1_5.add(GenericTypeSignatureTest.class);
		tests_1_5.add(InternalHexFloatTest.class);
		tests_1_5.add(JavadocTest_1_5.class);
		tests_1_5.add(BatchCompilerTest.class);
		tests_1_5.add(ExternalizeStringLiterals15Test.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_5, RegressionTestSetup.class, tests_1_5));
	}
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_6) != 0) {
		ArrayList tests_1_6 = (ArrayList)standardTests.clone();
		tests_1_6.addAll(RunComparableTests.ALL_CLASSES);
		tests_1_6.add(AssertionTest.class);
		tests_1_6.add(ClassFileReaderTest_1_5.class);
		tests_1_6.add(GenericTypeSignatureTest.class);
		tests_1_6.add(InternalHexFloatTest.class);
		tests_1_6.add(JavadocTest_1_5.class);
		tests_1_6.add(BatchCompilerTest.class);
		tests_1_6.add(ExternalizeStringLiterals15Test.class);
		tests_1_6.add(StackMapAttributeTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
		all.addTest(AbstractCompilerTest.suiteForComplianceLevel(AbstractCompilerTest.COMPLIANCE_1_6, RegressionTestSetup.class, tests_1_6));
	}
	return all;
}
}
