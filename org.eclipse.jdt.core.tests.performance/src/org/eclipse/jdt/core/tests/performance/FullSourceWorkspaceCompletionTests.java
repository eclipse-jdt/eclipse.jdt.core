/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.PrintStream;
import java.text.NumberFormat;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

@SuppressWarnings("rawtypes")
public class FullSourceWorkspaceCompletionTests extends FullSourceWorkspaceTests {

	// Counters
	private static final int WARMUP_COUNT = 10;
	private static final int ITERATION_COUNT = 40;
	static int[] PROPOSAL_COUNTS;
	static int TESTS_COUNT = 0;
	static int TESTS_LENGTH;
	static int COMPLETIONS_COUNT = 0;

	// Log files
	private static PrintStream[] LOG_STREAMS = new PrintStream[DIM_NAMES.length];

	public FullSourceWorkspaceCompletionTests(String name) {
		super(name);
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_LENGTH = TESTS_COUNT = suite.countTestCases();
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
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		COMPLETIONS_COUNT = 0;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
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

	class TestCompletionRequestor extends CompletionRequestor {
		public void accept(CompletionProposal proposal) {
			PROPOSAL_COUNTS[TESTS_LENGTH-TESTS_COUNT]++;
		}
	}

	private void complete(
			String projectName,
			String packageName,
			String unitName,
			String completeAt,
			String completeBehind,
			int warmupCount,
			int iterationCount) throws JavaModelException {
		complete(projectName,
				packageName,
				unitName,
				completeAt,
				completeBehind,
				null,
				warmupCount,
				iterationCount);
	}
	private void complete(
			String projectName,
			String packageName,
			String unitName,
			String completeAt,
			String completeBehind,
			int[] ignoredKinds,
			int warmupCount,
			int iterationCount) throws JavaModelException {

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

	public void testPerfCompleteMethodDeclaration() throws JavaModelException {
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"IType {",
				"IType {",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteMemberAccess() throws JavaModelException {
		tagAsGlobalSummary("Codeassist in expression", true); // put in global fingerprint
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"this.",
				"this.",
				null,
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteTypeReference() throws JavaModelException {
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"ArrayList list",
				"A",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteEmptyName() throws JavaModelException {
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteName() throws JavaModelException {
		tagAsSummary("Codeassist in name", true); // put in fingerprint
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				null,
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteEmptyNameWithoutTypes() throws JavaModelException {
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				new int[]{CompletionProposal.TYPE_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteNameWithoutTypes() throws JavaModelException {
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"p",
				new int[]{CompletionProposal.TYPE_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteEmptyNameWithoutMethods() throws JavaModelException {
		complete(
				"org.eclipse.jdt.core",
				"org.eclipse.jdt.internal.core",
				"SourceType.java",
				"params.add",
				"",
				new int[]{CompletionProposal.METHOD_REF},
				WARMUP_COUNT,
				ITERATION_COUNT);
	}
	public void testPerfCompleteNameWithoutMethods() throws JavaModelException {
		complete(
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
