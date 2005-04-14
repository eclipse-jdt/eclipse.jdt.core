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

import java.io.PrintStream;
import java.text.NumberFormat;
import junit.framework.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;

/**
 */
public class FullSourceWorkspaceTypeHierarchyTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {
	
    // Tests counter
    private static int TESTS_COUNT = 0;
//	private final static int ITERATIONS_COUNT = 10;

    // Log files
    private static PrintStream[] LOG_STREAMS = new PrintStream[LOG_TYPES.length];

	static int[] COUNTERS = new int[1];
	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceTypeHierarchyTests(String name) {
		super(name);
	}

//	static {
//		TESTS_NAMES = new String[] { "testPerfAllTypes" };
//	}
	public static Test suite() {
        Test suite = buildSuite(testClass());
        TESTS_COUNT = suite.countTestCases();
        createPrintStream(testClass().getName(), LOG_STREAMS, TESTS_COUNT, null);
        return suite;
    }

    private static Class testClass() {
        return FullSourceWorkspaceTypeHierarchyTests.class;
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

		// End of execution => one test less
		TESTS_COUNT--;

		// Log perf result
		if (LOG_DIR != null) {
			logPerfResult(LOG_STREAMS, TESTS_COUNT);
		}
		
		// Print statistics
		if (TESTS_COUNT == 0) {
			// Print statistics
			System.out.println("-------------------------------------");
			System.out.println("Type Hierarchy test statistics:");
			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			System.out.println("  - "+intFormat.format(COUNTERS[0])+" all types found.");
			System.out.println("-------------------------------------\n");
		}
		
		// Call super at the end as it close print streams
		super.tearDown();
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

	public void testPerfAllTypes() throws CoreException {
		tagAsSummary("Type Hierarchy>All Types", true); // put in fingerprint
		ICompilationUnit unit = getCompilationUnit("org.eclipse.jdt.core", "org.eclipse.jdt.internal.compiler.ast", "ASTNode.java");
		assertNotNull("ASTNode not found!", unit);

		// Warm up
		IType[] types = unit.getType("ASTNode").newTypeHierarchy(null).getAllClasses();
		int length = types.length;

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
			assertEquals("Unexpected classes number in hierarchy!", length, unit.getType("ASTNode").newTypeHierarchy(null).getAllClasses().length);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();

		// Store counter
		COUNTERS[0] = length;
	}
}
