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
package org.eclipse.jdt.core.tests.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.compiler.regression.RegressionTestSetup;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractCompilerTest extends TestCase {

	public static final String COMPLIANCE_1_3 = CompilerOptions.VERSION_1_3;
	public static final String COMPLIANCE_1_4 = CompilerOptions.VERSION_1_4;
	public static final String COMPLIANCE_1_5 = CompilerOptions.VERSION_1_5;
	public static final String COMPLIANCE_1_6 = CompilerOptions.VERSION_1_6;
	public static final String COMPLIANCE_1_7 = CompilerOptions.VERSION_1_7;

	public static final int F_1_3 = 0x01;
	public static final int F_1_4 = 0x02;
	public static final int F_1_5 = 0x04;
	public static final int F_1_6 = 0x08;
	public static final int F_1_7 = 0x10;

	protected static boolean RUN_JAVAC = CompilerOptions.ENABLED.equals(System.getProperty("run.javac"));
	private static int possibleComplianceLevels = 
		RUN_JAVAC ? F_1_5 : -1;
	  // javac tests imply 1.5 compliance

	protected String complianceLevel;

	/**
	 * Build a test suite made of test suites for all possible running VM compliances .
	 * 
	 * @see #buildUniqueComplianceTestSuite(Class, String) for test suite children content.
	 * 
	 * @param evaluationTestClass The main test suite to build.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildAllCompliancesTestSuite(Class evaluationTestClass) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_6));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_7));
		}
		return suite;
	}

	/**
	 * Build a test suite made of test suites for all possible running VM compliances .
	 * 
	 * @see #buildComplianceTestSuite(List, Class, String) for test suite children content.
	 * 
	 * @param testSuiteClass The main test suite to build.
	 * @param setupClass The compiler setup to class to use to bundle given tets suites tests.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildAllCompliancesTestSuite(Class testSuiteClass, Class setupClass, List testClasses) {
		TestSuite suite = new TestSuite(testSuiteClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, COMPLIANCE_1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, COMPLIANCE_1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_6) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, COMPLIANCE_1_6));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			suite.addTest(buildComplianceTestSuite(testClasses, setupClass, COMPLIANCE_1_7));
		}
		return suite;
	}

	/**
	 * Build a test suite for a compliance and a list of test suites.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the given compliance level.
	 * 
	 * @see #buildComplianceTestSuite(List, Class, String) for child test suite content.
	 * 
	 * @param complianceLevel The compliance level used for this test suite.
	 * @param testClasses The list of test suites to include in main test suite.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildComplianceTestSuite(String complianceLevel, List testClasses) {
		return buildComplianceTestSuite(testClasses, RegressionTestSetup.class, complianceLevel);
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
	private static Test buildComplianceTestSuite(List testClasses, Class setupClass, String complianceLevel) {
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
	 * Build a regression test setup suite for a minimal compliance and a test suite to run.
	 * Returned test suite has only one child: {@link RegressionTestSetup} test suite.
	 * Name of returned suite is the name of given test suite class.
	 * The test suite will be run iff the compliance is at least the specified one.
	 * 
	 * @param minimalCompliance The unqie compliance level used for this test suite.
	 * @param evaluationTestClass The test suite to run.
	 * @return built test suite (see {@link TestSuite}
	 */
	public static Test buildMinimalComplianceTestSuite(Class evaluationTestClass, int minimalCompliance) {
		TestSuite suite = new TestSuite(evaluationTestClass.getName());
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		int level13 = complianceLevels & AbstractCompilerTest.F_1_3;
		if (level13 != 0) {
			if (level13 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_3+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_3));
			}
		}
		int level14 = complianceLevels & AbstractCompilerTest.F_1_4;
		if (level14 != 0) {
			if (level14 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_4+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_4));
			}
		}
		int level15 = complianceLevels & AbstractCompilerTest.F_1_5;
		if (level15 != 0) {
			if (level15 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_5+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_5));
			}
		}
		int level16 = complianceLevels & AbstractCompilerTest.F_1_6;
		if (level16 != 0) {
			if (level16 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_6+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_6));
			}
		}
		int level17 = complianceLevels & AbstractCompilerTest.F_1_7;
		if (level17 != 0) {
			if (level17 < minimalCompliance) {
				System.err.println("Cannot run "+evaluationTestClass.getName()+" at compliance "+COMPLIANCE_1_7+"!");
			} else {
				suite.addTest(buildUniqueComplianceTestSuite(evaluationTestClass, AbstractCompilerTest.COMPLIANCE_1_7));
			}
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
	public static Test buildUniqueComplianceTestSuite(Class evaluationTestClass, String uniqueCompliance) {
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

	/*
	 * Returns the highest compliance level this VM instance can run.
	 */
	public static String highestComplianceLevels() {
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_7) != 0) {
			return COMPLIANCE_1_7;
		}
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
				} else if (COMPLIANCE_1_7.equals(compliance)) {
					possibleComplianceLevels = F_1_7;
				} else {
					System.out.println("Invalid compliance specified (" + compliance + ")");
					System.out.print("Use one of ");
					System.out.print(COMPLIANCE_1_3 + ", ");
					System.out.print(COMPLIANCE_1_4 + ", ");
					System.out.print(COMPLIANCE_1_5 + ", ");
					System.out.print(COMPLIANCE_1_6 + ", ");
					System.out.println(COMPLIANCE_1_7);
					System.out.println("Defaulting to all possible compliances");
				}
			}
			if (possibleComplianceLevels == -1) {
				possibleComplianceLevels = F_1_3;
				String specVersion = System.getProperty("java.specification.version");
				boolean canRun1_4 = !"1.0".equals(specVersion)
					&& !CompilerOptions.VERSION_1_1.equals(specVersion)
					&& !CompilerOptions.VERSION_1_2.equals(specVersion)
					&& !CompilerOptions.VERSION_1_3.equals(specVersion);
				if (canRun1_4) {
					possibleComplianceLevels |= F_1_4;
				}
				boolean canRun1_5 = canRun1_4 && !CompilerOptions.VERSION_1_4.equals(specVersion);
				if (canRun1_5) {
					possibleComplianceLevels |= F_1_5;
				}
				boolean canRun1_6 = canRun1_5 && !CompilerOptions.VERSION_1_5.equals(specVersion);
				if (canRun1_6) {
					possibleComplianceLevels |= F_1_6;
				}
				boolean canRun1_7 = canRun1_6 && !CompilerOptions.VERSION_1_6.equals(specVersion);
				if (canRun1_7) {
					possibleComplianceLevels |= F_1_7;
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
		Class testClass;
		if (testClasses.size() == 1) {
			suite = new TestSuite(testClass = (Class)testClasses.get(0), complianceLevel);
			TESTS_COUNTERS.put(testClass.getName(), new Integer(suite.countTestCases()));
		} else {
			suite = new TestSuite(complianceLevel);
			for (int i = 0, length = testClasses.size(); i < length; i++) {
				TestSuite innerSuite = new TestSuite(testClass = (Class)testClasses.get(i));
				TESTS_COUNTERS.put(testClass.getName(), new Integer(innerSuite.countTestCases()));
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
		String className = evaluationTestClass.getName();
		Integer testsNb;
		int newTestsNb = test.countTestCases();
		if ((testsNb = (Integer) TESTS_COUNTERS.get(className)) != null)
			newTestsNb += testsNb.intValue();
		TESTS_COUNTERS.put(className, new Integer(newTestsNb));
		return test;
	}

	
	public static boolean isJRELevel(int compliance) {
		return (AbstractCompilerTest.getPossibleComplianceLevels() & compliance) != 0;
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
		} else if (COMPLIANCE_1_7.equals(this.complianceLevel)) {
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
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
	
	// Output files management
	protected IPath 
		outputRootDirectoryPath = new Path(Util.getOutputDirectory()),
		outputTestDirectoryPath;

	/**
	 * Create a test specific output directory as a subdirectory of 
	 * outputRootDirectory, given a subdirectory path. The whole 
	 * subtree is created as needed. outputTestDirectoryPath is 
	 * modified according to the latest call to this method.
	 * @param suffixPath a valid relative path for the subdirectory
	 */
	protected void createOutputTestDirectory(String suffixPath) {
		this.outputTestDirectoryPath = 
			((IPath) this.outputRootDirectoryPath.clone()).append(suffixPath);
		File dir = this.outputTestDirectoryPath.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void writeFiles(String[] testFiles) {
		createOutputTestDirectory(testName());

		// Write each given test file
		for (int i = 0, length = testFiles.length; i < length; ) {
			String fileName = testFiles[i++];
			String contents = testFiles[i++];
			IPath filePath = 
				((IPath) this.outputTestDirectoryPath.clone()).append(fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				File dir = filePath.removeLastSegments(1).toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			Util.writeToFile(contents, filePath.toString());
		}
	}
	
	// Summary display		
	// Used by AbstractRegressionTest for javac comparison tests
	protected static Map TESTS_COUNTERS = new HashMap();
}
