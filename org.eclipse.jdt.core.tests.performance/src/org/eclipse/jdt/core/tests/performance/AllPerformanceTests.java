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
import org.eclipse.jdt.core.tests.model.NameLookupTests2;
import junit.framework.Test;

/**
 * Class to run all JDT/Core performance tests.
 */
public class AllPerformanceTests extends junit.framework.TestCase {

	final static String LENGTH = System.getProperty("length", "0");
	final static boolean ADD = System.getProperty("add", "false").equals("true");

	/**
	 * Define performance tests classes to be run.
	 */
	public static Class[] getAllTestClasses() {
		return new Class[] {
			FullSourceWorkspaceSearchTests.class, // run this test first to be sure that indexing is finished
			FullSourceWorkspaceBuildTests.class,
			FullSourceWorkspaceASTTests.class,
			FullSourceWorkspaceTypeHierarchyTests.class,
			NameLookupTests2.class
		};
	}

	/**
	 * Additional test class(es).
	 * 
	 * Classes put in this list will be run only if "additional" VM parameter is added
	 * while running JUnit test suite.
	 * 
	 * This parameter is an integer to specify position where this additional classes
	 * list has to be added in main list {@link #getAllTestClasses()}.
	 * 
	 * For example, set VM parameter -Dadditional=2 will result to run following list of classes:
	 *		- FullSourceWorkspaceSearchTests
	 *		- FullSourceWorkspaceBuildTests
	 *		- FullSourceWorkspaceCompletionTests <-- additional class inserted at position 2
	 *		- FullSourceWorkspaceASTTests
	 *		- FullSourceWorkspaceTypeHierarchyTests
	 *		- NameLookupTests2
	 *
	 * @see #ADD
	 */
	public static Class[] getAdditionalTestClasses() {
		return new Class[] {
			FullSourceWorkspaceCompletionTests.class
		};
	}
	
	/**
	 * Build test suite.
	 * All classes suite method are called and bundle to main test suite.
	 * 
	 * @see FullSourceWorkspaceSearchTests
	 * @see FullSourceWorkspaceBuildTests
	 * @see FullSourceWorkspaceCompletionTests <-- additional class inserted at position 2
	 * @see FullSourceWorkspaceASTTests
	 * @see FullSourceWorkspaceTypeHierarchyTests
	 * @see NameLookupTests2
	 */
	public static Test suite() {
		PerformanceTestSuite perfSuite = new PerformanceTestSuite(AllPerformanceTests.class.getName());
		Class[] testSuites = getAllTestClasses();

		// Reset subsets of tests (after having test classes loaded
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;

		// Get test suites subset
		int length = testSuites.length;
		if (ADD) {
			try {
				Class[] complete = getAdditionalTestClasses();
				int completeLength = complete.length;
				Class[] newSuites = new Class[length+completeLength];
				System.arraycopy(testSuites, 0, newSuites, 0, length);
				System.arraycopy(complete, 0, newSuites, length, completeLength);
				testSuites = newSuites;
				length = testSuites.length;
			} catch (NumberFormatException e1) {
				// do nothing
			}
		}

		// Get suite acronym
		String suitesAcronym = "";
		for (int i = 0; i < length; i++) {
			String name = FullSourceWorkspaceTests.suiteTypeShortName(testSuites[i]);
			if (name != null) {
				suitesAcronym += name.substring(0, 1);
			}
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
