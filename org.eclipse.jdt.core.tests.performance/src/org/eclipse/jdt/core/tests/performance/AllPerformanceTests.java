/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.lang.reflect.*;
import org.eclipse.jdt.core.tests.junit.extension.PerformanceTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

public class AllPerformanceTests extends TestCase {

	public static Class[] getAllTestClasses() {
		return new Class[] {
			FullSourceWorkspaceSearchTests.class, // run this test first to be sure that indexing is finished
			FullSourceWorkspaceBuildTests.class,
			FullSourceWorkspaceASTTests.class,
			FullSourceWorkspaceTypeHierarchyTests.class
		};
	}
	public static Test suite() {
		PerformanceTestSuite perfSuite = new PerformanceTestSuite(AllPerformanceTests.class.getName());
		Class[] testSuites = getAllTestClasses();
		int length = testSuites.length;
		for (int i = 0; i < length; i++) {
			Class testClass = testSuites[i];
			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod(
						"suite", new Class[0]); //$NON-NLS-1$
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
