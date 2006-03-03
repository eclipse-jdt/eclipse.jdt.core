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
	public static final String COMPLIANCE_1_6 = "1.6";

	public static final int F_1_3 = 0x1;
	public static final int F_1_4 = 0x2;
	public static final int F_1_5 = 0x4;
	public static final int F_1_6 = 0x8;

	private static int possibleComplianceLevels = -1;

	protected String complianceLevel;

	/*
	 * Returns the highest compliance level this VM instance can run.
	 */
	public static String highestComplianceLevels() {
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			return COMPLIANCE_1_6;
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			return COMPLIANCE_1_5;
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			return COMPLIANCE_1_4;
		}
		return COMPLIANCE_1_3;
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
				} else if (COMPLIANCE_1_6.equals(compliance)) {
					possibleComplianceLevels = F_1_6;
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
				boolean canRun1_6 = "1.6".equals(specVersion);
				if (canRun1_6) {
					possibleComplianceLevels |= F_1_6;
				}
			}
		}
		return possibleComplianceLevels;
	}

	/**
	 * Build a test suite made of test suites for all possible running VM compliances .
	 * 
	 * @see #buildComplianceSetupTestSuite(List, Class, String) for test suite children content.
	 * 
	 * @param testSuiteClass The main test suite to build.
	 * @param setupClass The compiler setup to class to use to bundle given tets suites tests.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildAllCompliancesSetupSuite(Class testSuiteClass, Class setupClass, List testClasses) {
		TestSuite suite = new TestSuite(testSuiteClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(buildComplianceSetupTestSuite(testClasses, setupClass, COMPLIANCE_1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildComplianceSetupTestSuite(testClasses, setupClass, COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildComplianceSetupTestSuite(testClasses, setupClass, COMPLIANCE_1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildComplianceSetupTestSuite(testClasses, setupClass, COMPLIANCE_1_6));
		}
		return suite;
	}

	/**
	 * Build a test suite for a compliance and a list of test suites.
	 * Returned test suite has only one child: {@link CompilerTestSetup} test suite.
	 * Name of returned suite is the given compliance level.
	 * 
	 * @see #buildComplianceSetupTestSuite(List, Class, String) for child test suite content.
	 * 
	 * @param complianceLevel The compliance level used for this test suite.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildComplianceCompilerTestSetupSuite(String complianceLevel, List testClasses) {
		return buildComplianceSetupTestSuite(testClasses, CompilerTestSetup.class, complianceLevel);
	}

	/**
	 * Build a test suite for a compliance and a list of test suites.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the given compliance level.
	 * 
	 * @see #buildComplianceSetupTestSuite(List, Class, String) for child test suite content.
	 * 
	 * @param complianceLevel The compliance level used for this test suite.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildComplianceRegressionTestSetupSuite(String complianceLevel, List testClasses) {
		return buildComplianceSetupTestSuite(testClasses, RegressionTestSetup.class, complianceLevel);
	}

	/**
	 * Build a test suite for a compliance and a list of test suites.
	 * Children of returned test suite are setup test suites (see {@link CompilerTestSetup}).
	 * Name of returned suite is the given compliance level.
	 * 
	 * @param complianceLevel The compliance level used for this test suite.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	private static Test buildComplianceSetupTestSuite(List testClasses, Class setupClass, String complianceLevel) {
		TestSuite complianceSuite = new TestSuite(complianceLevel);
		for (int i=0, m=testClasses.size(); i<m ; i++) {
			Class testClass = (Class)testClasses.get(i);
			TestSuite suite = new TestSuite(testClass.getName());
			List tests = buildTestsList(testClass);
			for (int index=0, size=tests.size(); index<size; index++) {
				suite.addTest((Test)tests.get(index));
			}
			complianceSuite.addTest(suite);
		}
	
		// call the setup constructor with the suite and compliance level
		try {
			Constructor constructor = setupClass.getConstructor(new Class[]{Test.class, String.class});
			Test setUp = (Test)constructor.newInstance(new Object[]{complianceSuite, complianceLevel});
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

	/**
	 * Build a test suite made of test suites for all possible running VM compliances .
	 * 
	 * @see #buildComplianceRegressionSetupSuite(Class, String) for test suite children content.
	 * 
	 * @param evaluationTestClass The main test suite to build.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildAllCompliancesRegressionTestSetupSuite(Class evaluationTestClass) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_6));
		}
		return suite;
	}

	/**
	 * Build a regression test setup suite for a compliance and a test suite to run.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the name of given test suite class.
	 * 
	 * @param uniqueCompliance The unqie compliance level used for this test suite.
	 * @param evaluationTestClass The test suite to run.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildComplianceRegressionSetupSuite(Class evaluationTestClass, String uniqueCompliance) {
		String highestLevel = highestComplianceLevels();
		if (highestLevel.compareTo(uniqueCompliance) < 0) {
			System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+highestLevel+"!");
			return new TestSuite();
		}
		TestSuite complianceSuite = new TestSuite(uniqueCompliance);
		List tests = buildTestsList(evaluationTestClass);
		for (int index=0, size=tests.size(); index<size; index++) {
			complianceSuite.addTest((Test)tests.get(index));
		}
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		suite.addTest(new RegressionTestSetup(complianceSuite, uniqueCompliance));
		return suite;
	}

	/**
	 * Build a regression test setup suite for a minimal compliance and a test suite to run.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the name of given test suite class.
	 * The test suite will be run iff the compliance is at least the specified one.
	 * 
	 * @param minimalCompliance The unqie compliance level used for this test suite.
	 * @param evaluationTestClass The test suite to run.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildComplianceRegressionTestSetupSuite(Class evaluationTestClass, int minimalCompliance) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		int level13 = complianceLevels & AbstractCompilerTest.F_1_3;
		if (level13 != 0) {
			if (level13 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_3+"!");
			} else {
				suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_3));
			}
		}
		int level14 = complianceLevels & AbstractCompilerTest.F_1_4;
		if (level14 != 0) {
			if (level14 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_4+"!");
			} else {
				suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_4));
			}
		}
		int level15 = complianceLevels & AbstractCompilerTest.F_1_5;
		if (level15 != 0) {
			if (level15 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_5+"!");
			} else {
				suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_5));
			}
		}
		int level16 = complianceLevels & AbstractCompilerTest.F_1_6;
		if (level16 != 0) {
			if (level16 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_6+"!");
			} else {
				suite.addTest(buildComplianceRegressionSetupSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_6));
			}
		}
		return suite;
	}

	public AbstractCompilerTest(String name) {
		super(name);
	}

	protected Map getCompilerOptions() {
		Map options = new CompilerOptions().getMap();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
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
		} else if (COMPLIANCE_1_6.equals(this.complianceLevel)) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
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
