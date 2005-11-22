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
package org.eclipse.jdt.core.tests.performance;

import java.lang.reflect.*;
import org.eclipse.jdt.core.tests.junit.extension.PerformanceTestSuite;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import junit.framework.Test;

/**
 * Class to run all JDT/Core performance tests.
 */
public class AllPerformanceTests extends junit.framework.TestCase {

	final static boolean ADD = System.getProperty("add", "false").equals("true");
	final static String RUN_ID = System.getProperty("runID");

	/**
	 * Define performance tests classes to be run.
	 */
	public static Class[] getAllTestClasses() {
		return new Class[] {
			FullSourceWorkspaceSearchTests.class, // run this test first to be sure that indexing is finished
			FullSourceWorkspaceBuildTests.class,
			FullSourceWorkspaceASTTests.class,
			FullSourceWorkspaceTypeHierarchyTests.class,
			FullSourceWorkspaceModelTests.class,
			FullSourceWorkspaceCompletionTests.class,
		};
	}

	/**
	 * Additional test class(es).
	 * 
	 * Classes put in this list will be run only if "add" VM parameter is added
	 * while running JUnit test suite.
	 * 
	 * @see #ADD
	 */
	public static Class[] getAdditionalTestClasses() {
		return new Class[] {
		};
	}
	
	/**
	 * Build test suite.
	 * All classes suite method are called and bundle to main test suite.
	 */
	public static Test suite() {
		PerformanceTestSuite perfSuite = new PerformanceTestSuite(AllPerformanceTests.class.getName());
		Class[] testSuites = getAllTestClasses();

		// Cannot run performance tests if one of subset static fields is not null
		// (this may modify tests run order and make stored results invalid)
		if (TestCase.TESTS_NAMES != null ||
			TestCase.TESTS_PREFIX != null ||
			TestCase.TESTS_NUMBERS != null ||
			TestCase.TESTS_RANGE != null) {
			System.err.println("Cannot run performance tests as there are defined subsets which may alter tests order!");
			return perfSuite;
		}

		// Get test suites subset
		int length = testSuites.length;
		if (RUN_ID != null) {
			Class[] subSetSuites = new Class[length];
			int count = 0;
			for (int i = 0; i < length; i++) {
				String name = FullSourceWorkspaceTests.suiteTypeShortName(testSuites[i]);
				if (RUN_ID.indexOf(name.charAt(0)) >= 0) {
					subSetSuites[count++] = testSuites[i];
				}
			}
			System.arraycopy(subSetSuites, 0, testSuites = new Class[count], 0, count);
			length = count;
		}

		// Get test suites subset
		if (ADD) {
			Class[] complete = getAdditionalTestClasses();
			int completeLength = complete.length;
			Class[] newSuites = new Class[length+completeLength];
			System.arraycopy(testSuites, 0, newSuites, 0, length);
			System.arraycopy(complete, 0, newSuites, length, completeLength);
			testSuites = newSuites;
			length = testSuites.length;
		}

		// Get suite acronym
		if (length == 0) {
			System.err.println("There's no performances suites to run!!!");
			return perfSuite;
		}
		String suitesAcronym = "";
		if (RUN_ID == null) {
			for (int i = 0; i < length; i++) {
				String name = FullSourceWorkspaceTests.suiteTypeShortName(testSuites[i]);
				if (name != null) {
					char firstChar = name.charAt(0);
					if (suitesAcronym.indexOf(firstChar) >= 0) {
						System.out.println("WARNING: Duplicate letter in RUN_ID for test suite: "+name);
					}
					suitesAcronym += firstChar;
				}
			}
		} else {
			suitesAcronym = RUN_ID;
		}
		FullSourceWorkspaceTests.RUN_ID = suitesAcronym; //.toLowerCase();
		
		// Get tests of suites
		for (int i = 0; i < length; i++) {
			Class testClass = testSuites[i];
			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
				perfSuite.addTest(suite);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return perfSuite;
	}
}
