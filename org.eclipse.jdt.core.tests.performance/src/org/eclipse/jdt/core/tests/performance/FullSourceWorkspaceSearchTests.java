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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceSearchTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {

	// Tests counters
	private static int TESTS_COUNT = 0;
	private final static int ITERATIONS_COUNT = 10;

	// Search stats
	private static int[] REFERENCES = new int[4];
	private static int ALL_TYPES_NAMES = 0;

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[4];
	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceSearchTests(String name) {
		super(name);
	}

	static {
//		org.eclipse.jdt.internal.core.search.processing.JobManager.VERBOSE = true;
//		TESTS_NAMES = new String[] { "testPerfIndexing", "testPerfSearchAllTypeNames" };
	}
	/*
	 * Specific way to build test suite.
	 * We need to know whether test perf indexing is in list to allow
	 * index manager disabling.
	 * CAUTION: If test perf indexing is not included in test suite,
	 * then time for other tests may include time spent to index files!
	 */
	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		createPrintStream(testClass().getName(), LOG_STREAMS, TESTS_COUNT, null);
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceSearchTests.class;
	}

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
			System.out.println("-------------------------------------");
			System.out.println("Search performance test statistics:");
			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			System.out.println("  - "+intFormat.format(REFERENCES[0])+" type references found.");
			System.out.println("  - "+intFormat.format(REFERENCES[1])+" field references found.");
			System.out.println("  - "+intFormat.format(REFERENCES[2])+" method references found.");
			System.out.println("  - "+intFormat.format(REFERENCES[3])+" constructor references found.");
			System.out.println("  - "+intFormat.format(ALL_TYPES_NAMES)+" all types names.");
			System.out.println("-------------------------------------\n");
		}
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
	/**
	 * Simple type name requestor: only count classes and interfaces.
	 * @deprecated
	 */
	class SearchTypeNameRequestor implements ITypeNameRequestor {
		int count = 0;
		public void acceptClass(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			this.count++;
		}
		public void acceptInterface(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			this.count++;
		}
	}
	/**
	 * Job to measure times in same thread than index manager.
	 */
	class	 Measuring implements IJob {
		boolean start;
		Measuring(boolean start) {
			this.start = start;
		}
		public boolean belongsTo(String jobFamily) {
			return true;
		}
		public void cancel() {
			// nothing to cancel
		}
		public void ensureReadyToRun() {
			// always ready to do nothing
		}
		/**
		 * Execute the current job, answer whether it was successful.
		 */
		public boolean execute(IProgressMonitor progress) {
			if (start) {
				startMeasuring();
			} else {
				stopMeasuring();
				commitMeasurements();
				assertPerformance();
			}
			return true;
		}
	}
	
	protected JavaSearchResultCollector resultCollector;

	protected void search(String patternString, int searchFor, int limitTo, SearchRequestor requestor) throws CoreException {
		int matchMode = patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1
			? SearchPattern.R_PATTERN_MATCH
			: SearchPattern.R_EXACT_MATCH;
		SearchPattern pattern = SearchPattern.createPattern(
			patternString, 
			searchFor,
			limitTo, 
			matchMode | SearchPattern.R_CASE_SENSITIVE);
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			SearchEngine.createWorkspaceScope(),
			requestor,
			null);
	}

	// Do NOT forget that tests must start with "testPerf"

	/**
	 * Performance tests for search: Indexing.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Consider this initial indexing jobs as warm-up for this test.
	 */
	public void testPerfIndexing() throws CoreException {
		tagAsSummary("Search>Indexing", Dimension.CPU_TIME, true/*put in fingerprint*/);
//		INDEX_MANAGER.discardJobs(null); // discard all previous index jobs
		// Wait for indexing end (we use initial indexing as warm-up)
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$
		
		// Remove all previous indexing
		INDEX_MANAGER.removeIndexFamily(new Path(""));
		INDEX_MANAGER.reset();
		
		// Restart brand new indexing
		INDEX_MANAGER.request(new Measuring(true/*start measuring*/));
		for (int i=0, length=ALL_PROJECTS.length; i<length; i++) {
			INDEX_MANAGER.indexAll(ALL_PROJECTS[i].getProject());
		}
//		INDEX_MANAGER.enable();
		
		// Wait for indexing end
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$
		
		// Commit measures
		INDEX_MANAGER.request(new Measuring(false /*end measuring*/));
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$
	}
	
	/**
	 * Old version no longer used as scope is now a Workspace scope
	 * instead of all projects scope.
	 * 
	 * CAUTION: This test result is no longer put in performance fingerprint as its scope
	 * did not match common usage of search all type names functionality.
	 * @deprecated
	 */
	public void testPerfSearchAllTypeNames() throws CoreException {
		// Do no longer print results for this test
		tagAsSummary("Search>Names>All Projects", Dimension.CPU_TIME, false/*do NOT put in fingerprint*/);
		SearchTypeNameRequestor requestor = new SearchTypeNameRequestor();
		startMeasuring();
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			SearchEngine.createJavaSearchScope(ALL_PROJECTS), 
			requestor,
			WAIT_UNTIL_READY_TO_SEARCH,
			null);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		ALL_TYPES_NAMES = requestor.count;
	}

	/**
	 * Performance tests for search: Declarations Types Names.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 * 
	 * @deprecated
	 */
	public void testPerfSearchWkspAllTypeNames() throws CoreException {
		tagAsSummary("Search>Names>Workspace", Dimension.CPU_TIME, true/*put in fingerprint*/);
		SearchTypeNameRequestor requestor = new SearchTypeNameRequestor();

		// Wait for indexing end
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$

		// warmup
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			SearchEngine.createWorkspaceScope(), 
			requestor,
			WAIT_UNTIL_READY_TO_SEARCH,
			null);

		// Loop of measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
			for (int j=0; j<ITERATIONS_COUNT; j++) {
				new SearchEngine().searchAllTypeNames(
					null,
					null,
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
					IJavaSearchConstants.TYPE,
					SearchEngine.createWorkspaceScope(), 
					requestor,
					WAIT_UNTIL_READY_TO_SEARCH,
					null);
			}
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();

		// Store counter
		ALL_TYPES_NAMES = requestor.count;
	}

	/**
	 * Performance tests for search: Occurence Types.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 * 
	 * Note that following search have been tested:
	 *		- "String":				> 65000 macthes (CAUTION: needs -Xmx512M)
	 *		- "Object":			13497 matches
	 *		- ""IResource":	5886 macthes
	 *		- "JavaCore":		2145 matches
	 */
	public void testPerfSearchType() throws CoreException {
		tagAsSummary("Search>Occurences>Types", Dimension.CPU_TIME, true/*put in fingerprint*/);

		// Wait for indexing end
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$

		// warm-up
		search("JavaCore", TYPE, ALL_OCCURRENCES, this.resultCollector);

		// Loop of measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
//			for (int j=0; j<ITERATIONS_COUNT; j++) {
				search("JavaCore", TYPE, ALL_OCCURRENCES, this.resultCollector);
//			}
			stopMeasuring();
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store counter
		REFERENCES[0] = this.resultCollector.count;
	}

	/**
	 * Performance tests for search: Declarations Types Names.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 */
	public void testPerfSearchField() throws CoreException {
		tagAsSummary("Search>Occurences>Fields", Dimension.CPU_TIME, true/*put in fingerprint*/);

		// Wait for indexing end
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$

		// warm-up
		search("FILE", FIELD, ALL_OCCURRENCES, this.resultCollector);

		// Loop of measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
//			for (int j=0; j<ITERATIONS_COUNT; j++) {
				search("FILE", FIELD, ALL_OCCURRENCES, this.resultCollector);
//			}
			stopMeasuring();
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store counter
		REFERENCES[1] = this.resultCollector.count;
	}

	/**
	 * Performance tests for search: Declarations Types Names.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 */
	public void testPerfSearchMethod() throws CoreException {
		tagAsSummary("Search>Occurences>Methods", Dimension.CPU_TIME, true/*put in fingerprint*/);

		// Wait for indexing end
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$

		// warm-up
		search("equals", METHOD, ALL_OCCURRENCES, this.resultCollector);

		// Loop of measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
//			for (int j=0; j<ITERATIONS_COUNT; j++) {
				search("equals", METHOD, ALL_OCCURRENCES, this.resultCollector);
//			}
			stopMeasuring();
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store counter
		REFERENCES[2] = this.resultCollector.count;
	}

	/**
	 * Performance tests for search: Declarations Types Names.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 */
	public void testPerfSearchConstructor() throws CoreException {
		tagAsSummary("Search>Occurences>Constructors", Dimension.CPU_TIME, true/*put in fingerprint*/);

		// Wait for indexing end
		waitUntilIndexesReady();
//		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
//		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$

		// warm-up
		search("String", CONSTRUCTOR, ALL_OCCURRENCES, this.resultCollector);

		// Loop of measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			startMeasuring();
//			for (int j=0; j<ITERATIONS_COUNT; j++) {
				search("String", CONSTRUCTOR, ALL_OCCURRENCES, this.resultCollector);
//			}
			stopMeasuring();
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store counter
		REFERENCES[3] = this.resultCollector.count;
	}
}
