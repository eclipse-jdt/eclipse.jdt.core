/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.compiler.regression.RegressionTestSetup;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractCompilerTest extends TestCase {

	public static final String COMPLIANCE_1_3 = "1.3";
	public static final String COMPLIANCE_1_4 = "1.4";
	public static final String COMPLIANCE_1_5 = "1.5";

	public static final int F_1_3 = 0x1;
	public static final int F_1_4 = 0x2;
	public static final int F_1_5 = 0x4;

	private static int possibleComplianceLevels = -1;

	protected String complianceLevel;

	/*
	 * Returns the highest compliance level this VM instance can run.
	 */
	public static String highestComplianceLevels() {
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			return COMPLIANCE_1_5;
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			return COMPLIANCE_1_3;
		}
		// default
		return COMPLIANCE_1_4;
	}

	/*
	 * Returns the possible compliance levels this VM instance can run.
	 */
	public static int getPossibleComplianceLevels() {
		if (possibleComplianceLevels == -1) {
			String compliance = System.getProperty("compliance");
			if (compliance != null) {
				if (COMPLIANCE_1_3.equals(compliance)) {
					possibleComplianceLevels = F_1_3;
				} else if (COMPLIANCE_1_4.equals(compliance)) {
					possibleComplianceLevels = F_1_4;
				} else if (COMPLIANCE_1_5.equals(compliance)) {
					possibleComplianceLevels = F_1_5;
				} else {
					System.out.println("Invalid compliance specified (" + compliance + ")");
					System.out.println("Use one of " + COMPLIANCE_1_3 + ", " + COMPLIANCE_1_4 + ", " + COMPLIANCE_1_5);
					System.out.println("Defaulting to all possible compliances");
				}
			}
			if (possibleComplianceLevels == -1) {
				possibleComplianceLevels = F_1_3;
				String specVersion = System.getProperty("java.specification.version");
				boolean canRun1_4 = !"1.0".equals(specVersion) && !"1.1".equals(specVersion) && !"1.2".equals(specVersion) && !"1.3".equals(specVersion);
				if (canRun1_4) {
					possibleComplianceLevels |= F_1_4;
				}
				boolean canRun1_5 = canRun1_4 && !"1.4".equals(specVersion);
				if (canRun1_5) {
					possibleComplianceLevels |= F_1_5;
				}
			}
		}
		return possibleComplianceLevels;
	}

	/*
	 * Returns a test suite including the tests defined by the given classes for all possible complianceLevels
	 * and using the given setup class (CompilerTestSetup or a subclass)
	 */
	public static Test suite(String suiteName, Class setupClass, ArrayList testClasses) {
		TestSuite all = new TestSuite(suiteName);
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			all.addTest(suiteForComplianceLevel(COMPLIANCE_1_3, setupClass, testClasses));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			all.addTest(suiteForComplianceLevel(COMPLIANCE_1_4, setupClass, testClasses));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			all.addTest(suiteForComplianceLevel(COMPLIANCE_1_5, setupClass, testClasses));
		}
		return all;
	}

	/*
	 * Returns a test suite including the tests defined by the given classes for the given complianceLevel 
	 * (see AbstractCompilerTest for valid values) and using the given setup class (CompilerTestSetup or a subclass)
	 */
	public static Test suiteForComplianceLevel(String complianceLevel, Class setupClass, ArrayList testClasses) {
		TestSuite suite;
		if (testClasses.size() == 1) {
			suite = new TestSuite((Class)testClasses.get(0), complianceLevel);
		} else {
			suite = new TestSuite(complianceLevel);
			for (int i = 0, length = testClasses.size(); i < length; i++) {
				Class testClass = (Class)testClasses.get(i);
				TestSuite innerSuite = new TestSuite(testClass);
				suite.addTest(innerSuite);
			}
		}

		// call the setup constructor with the suite and compliance level
		try {
			Constructor constructor = setupClass.getConstructor(new Class[]{Test.class, String.class});
			Test setUp = (Test)constructor.newInstance(new Object[]{suite, complianceLevel});
			return setUp;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Test setupSuite(Class clazz) {
		ArrayList testClasses = new ArrayList();
		testClasses.add(clazz);
		return suite(clazz.getName(), RegressionTestSetup.class, testClasses);
	}

	public static Test suite(Class evaluationTestClass) {
		TestSuite suite = new TestSuite(evaluationTestClass);
		return suite;
	}

	public static Test buildTestSuite(Class evaluationTestClass) {
		if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE !=null) {
			return buildTestSuite(evaluationTestClass, highestComplianceLevels());
		}
		return setupSuite(evaluationTestClass);
	}

	public static Test buildTestSuite(Class evaluationTestClass, String complianceLevel) {
		TestSuite suite = new TestSuite(complianceLevel);
		List tests = buildTestsList(evaluationTestClass);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		TestSuite test = new TestSuite(evaluationTestClass.getName());
		test.addTest(new RegressionTestSetup(suite, complianceLevel));
		return test;
	}

	public AbstractCompilerTest(String name) {
		super(name);
	}

	protected Map getCompilerOptions() {
		Map options = new CompilerOptions().getMap();
		if (COMPLIANCE_1_3.equals(this.complianceLevel)) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
		} else if (COMPLIANCE_1_4.equals(this.complianceLevel)) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
		} else if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		}
		return options;
	}

	public String getName() {
		String name = super.getName();
		if (this.complianceLevel != null) {
			name = name + " - " + this.complianceLevel;
		}
		return name;
	}

	public void initialize(CompilerTestSetup setUp) {
		this.complianceLevel = setUp.complianceLevel;
	}

	protected String testName() {
		return super.getName();
	}
}
