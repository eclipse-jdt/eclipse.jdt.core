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
import org.eclipse.jdt.core.tests.model.NameLookupTests2;
import junit.framework.Test;
import junit.framework.TestCase;

public class AllPerformanceTests extends TestCase {

	static String LENGTH = System.getProperty("length", "0");

	public static Class[] getAllTestClasses() {
		return new Class[] {
			FullSourceWorkspaceSearchTests.class, // run this test first to be sure that indexing is finished
			FullSourceWorkspaceBuildTests.class,
//			FullSourceWorkspaceCompletionTests.class,
			FullSourceWorkspaceASTTests.class,
			FullSourceWorkspaceTypeHierarchyTests.class,
			NameLookupTests2.class
		};
	}
	public static Test suite() {
		PerformanceTestSuite perfSuite = new PerformanceTestSuite(AllPerformanceTests.class.getName());
		Class[] testSuites = getAllTestClasses();
		int length = 0;
		try {
			length = Integer.parseInt(LENGTH);
			if (length<=0 || length>testSuites.length)
				length = testSuites.length;
		} catch (NumberFormatException e1) {
			length = testSuites.length;
		}
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
