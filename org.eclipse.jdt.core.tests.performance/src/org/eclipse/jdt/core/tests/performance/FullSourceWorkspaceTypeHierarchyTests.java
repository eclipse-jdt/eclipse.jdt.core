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

import java.text.NumberFormat;
import junit.framework.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceTypeHierarchyTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {
	
	static int[] COUNTERS = new int[4];
	private static int COUNT = 0;
	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceTypeHierarchyTests(String name) {
		super(name);
	}

//	static {
//		TESTS_NAMES = new String[] { "testPerfSearchType" };
//	}
	public static Test suite() {
		Test suite = buildSuite(FullSourceWorkspaceTypeHierarchyTests.class);
		COUNT = suite.countTestCases();
		return suite;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.resultCollector = new JavaSearchResultCollector();
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		COUNT--;
		if (COUNT == 0) {
			// Print statistics
			System.out.println("-------------------------------------");
			System.out.println("Type Hierarchy test statistics:");
			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			System.out.println("  - "+intFormat.format(COUNTERS[0])+" all types found.");
			System.out.println("-------------------------------------\n");
		}
	}

	/**
	 * Simple search result collector: only count matches.
	 */
	class JavaSearchResultCollector extends SearchRequestor {
		int count = 0;
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			this.count++;
		}
	}
	
	protected JavaSearchResultCollector resultCollector;

	// Do NOT forget that tests must start with "testPerf"
	public void testPerfAllTypes() throws CoreException {
		tagAsSummary("Type Hierarchy all types", Dimension.CPU_TIME);
		ICompilationUnit unit = getCompilationUnit("org.eclipse.jdt.core", "org.eclipse.jdt.internal.compiler.ast", "ASTNode.java");
		assertNotNull("ASTNode not found!", unit);
		startMeasuring();
		ITypeHierarchy hierarchy = unit.getType("ASTNode").newTypeHierarchy(null);
		IType[] types = hierarchy.getAllClasses();
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		COUNTERS[0] = types.length;
	}
}
