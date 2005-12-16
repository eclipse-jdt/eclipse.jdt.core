/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.jdt.core.*;

/**
 */
public class FullSourceWorkspaceCompletionTests extends FullSourceWorkspaceTests {
	
	// Counters
	private static final int WARMUP_COUNT = 10;
	private static final int ITERATION_COUNT = 40;
	static int[] PROPOSAL_COUNTS;
	static int TESTS_COUNT = 0;
	static int TESTS_LENGTH = 0;
	static int COMPLETIONS_COUNT = 0;

	// Log files
	private static PrintStream[] LOG_STREAMS = new PrintStream[LOG_TYPES.length];

	public FullSourceWorkspaceCompletionTests(String name) {
		super(name);
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		PROPOSAL_COUNTS = new int[TESTS_COUNT];
		createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, "Complete");
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceCompletionTests.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.performance.FullSourceWorkspaceTests#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		COMPLETIONS_COUNT = 0;
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
			System.out.println("-------------------------------------");
			System.out.println("Completion performance test statistics:");
			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			System.out.println("  - "+intFormat.format(COMPLETIONS_COUNT)+" completions have been performed");
			System.out.println("  - following proposals have been done:");
			for (int i=0; i<TESTS_LENGTH; i++) {
				System.out.println("  	+ test "+i+": "+intFormat.format(PROPOSAL_COUNTS[i])+" proposals");
			}
			System.out.println("-------------------------------------\n");
		}
		
		// Call super at the end as it close print streams
		super.tearDown();
	}
	
	private class TestCompletionRequestor extends CompletionRequestor {
		public void accept(CompletionProposal proposal) {
			PROPOSAL_COUNTS[TESTS_LENGTH-TESTS_COUNT]++;
		}
	}

	private void complete(
			String testName,
			String projectName,
			String packageName,
			String unitName,
			String completeAt,
			String completeBehind,
			int warmupCount,
			int iterationCount) throws JavaModelException {
		complete(false, // do NOT put result in fingerprint
				testName,
				projectName,
				packageName,
				unitName,
				completeAt,
				completeBehind,
				null,
				warmupCount,
				iterationCount);
	}
	private void complete(
			boolean fingerprint,
			String testName,
			String projectName,
			String packageName,
			String unitName,
			String completeAt,
			String completeBehind,
			int[] ignoredKinds,
			int warmupCount,
			int iterationCount) throws JavaModelException {
		
		tagAsSummary(testName, fingerprint);
		
		waitUntilIndexesReady();
		
		TestCompletionRequestor requestor = new TestCompletionRequestor();
		if(ignoredKinds != null) {
			for (int i = 0; i < ignoredKinds.length; i++) {
				requestor.setIgnored(ignoredKinds[i], true);
			}
		}
		
		ICompilationUnit unit =
			getCompilationUnit(projectName, packageName, unitName);
		
		String str = unit.getSource();
		int completionIndex = str.indexOf(completeAt) + completeBehind.length();
		
		if (DEBUG) System.out.print("Perform code assist inside " + unitName + "...");
		
		// Warm up
		if(warmupCount > 0) {
			unit.codeComplete(completionIndex, requestor);
			for (int i = 1; i < warmupCount; i++) {
				unit.codeComplete(completionIndex, requestor);
			}
		}

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
			for (int j = 0; j < iterationCount; j++) {
				unit.codeComplete(completionIndex, requestor);
				COMPLETIONS_COUNT++;
			}
			stopMeasuring();
		}
		if (DEBUG) System.out.println("done!");
		
		// Commit
		commitMeasurements();
		assertPerformance();
	}
	
	public void testCompleteMethodDeclaration() throws JavaModelException {
		complete(
				"Completion>Method>Declaration",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"IType {",
				"IType {",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteMemberAccess() throws JavaModelException {
		complete(true, // put in fingerprint
				"Completion>Member>Access",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"this.",
				"this.",
				null,
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteTypeReference() throws JavaModelException {
		complete(
				"Completion>Type>Reference",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"ArrayList list",
				"A",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteEmptyName() throws JavaModelException {
		complete(
				"Completion>Name>Empty",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteName() throws JavaModelException {
		complete(true, // put result in fingerprint
				"Completion>Name",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				null,
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteEmptyNameWithoutTypes() throws JavaModelException {
		complete(false, // do NOT put result in fingerprint
				"Completion>Name>Empty>No Type",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				new int[]{CompletionProposal.TYPE_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteNameWithoutTypes() throws JavaModelException {
		complete(false, // do NOT put result in fingerprint
				"Completion>Name>No Type",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				new int[]{CompletionProposal.TYPE_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteEmptyNameWithoutMethods() throws JavaModelException {
		complete(false, // do NOT put result in fingerprint
				"Completion>Name>Empty>No Method",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				new int[]{CompletionProposal.METHOD_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testCompleteNameWithoutMethods() throws JavaModelException {
		complete(false, // do NOT put result in fingerprint
				"Completion>Name>No Method",
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				new int[]{CompletionProposal.METHOD_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
}
