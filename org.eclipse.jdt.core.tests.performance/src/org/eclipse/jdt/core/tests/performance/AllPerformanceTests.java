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

public class AllPerformanceTests extends junit.framework.TestCase {

	static String LENGTH = System.getProperty("length", "0");
	static String COMPLETE = System.getProperty("complete");

	public static Class[] getAllTestClasses() {
		return new Class[] {
			FullSourceWorkspaceSearchTests.class, // run this test first to be sure that indexing is finished
			FullSourceWorkspaceBuildTests.class,
			FullSourceWorkspaceASTTests.class,
			FullSourceWorkspaceTypeHierarchyTests.class,
			NameLookupTests2.class
		};
	}
	public static Class[] getCompleteClasses() {
		return new Class[] {
			CompleteFullSourceWorkspaceBuildTests.class,
			FullSourceWorkspaceCompletionTests.class
		};
	}
	public static Test suite() {
		PerformanceTestSuite perfSuite = new PerformanceTestSuite(AllPerformanceTests.class.getName());
		Class[] testSuites = getAllTestClasses();

		// Reset subsets of tests (after having test classes loaded
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;

		// Get test suites subset
		int length = 0;
		try {
			length = Integer.parseInt(LENGTH);
			if (length<=0 || length>testSuites.length)
				length = testSuites.length;
		} catch (NumberFormatException e1) {
			length = testSuites.length;
		}
		if (COMPLETE != null) {
			int pos = -1;
			try {
				pos = Integer.parseInt(COMPLETE);
				Class[] complete = getCompleteClasses();
				int cl = complete.length;
				Class[] newSuites = new Class[length+cl];
				if (pos <= 0) {
					System.arraycopy(complete, 0, newSuites, 0, cl);
					System.arraycopy(testSuites, 0, newSuites, cl, length);
				} else if (pos >= length) {
					System.arraycopy(testSuites, 0, newSuites, 0, length);
					System.arraycopy(complete, 0, newSuites, length, cl);
				} else {
					for (int i=0; i<pos; i++)
						newSuites[i] = testSuites[i];
					for (int i=pos; i<pos+cl; i++)
						newSuites[i] = complete[i-pos];
					for (int i=pos+cl; i<length+cl; i++)
						newSuites[i] = testSuites[i-cl];
				}
				testSuites = newSuites;
				length = testSuites.length;
			} catch (NumberFormatException e1) {
				// do nothing
			}
		}
		
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
