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
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceSearchTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {

	// Tests counter
	private static int TESTS_COUNT = 0;

	// Search stats
	private static int[] REFERENCES = new int[4];
	private static IJavaSearchScope SEARCH_SCOPE;
	private static int ALL_TYPES_NAMES = 0;
	private static IndexManager INDEX_MANAGER = JavaModelManager.getJavaModelManager().getIndexManager();

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[4];
	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceSearchTests(String name) {
		super(name);
	}

	static {
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
		
		// Create suite
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
	
		// Add test to sjuite and disable indexing if subset of test does include indexing test
		boolean indexing = false;
		for (int i=0, size= TESTS_NAME_LIST.size(); i<size; i++) {
			String testName = (String) TESTS_NAME_LIST.get(i);
			if (testName.equals("testPerfIndexing")) {
				indexing = true;
			}
		}
		if (indexing) {
			INDEX_MANAGER.disable();
		}

		// Init log
		initLogDir();
		createPrintStream(testClass().getName(), LOG_STREAMS, TESTS_COUNT, "Search");
		
		// Return created suite
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceSearchTests.class;
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.resultCollector = new JavaSearchResultCollector();
		if (SEARCH_SCOPE == null) {
			SEARCH_SCOPE = SearchEngine.createJavaSearchScope(ALL_PROJECTS);
		}
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
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
	 */
	class SearchTypeNameRequestor extends TypeNameRequestor {
		int count = 0;
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			this.count++;
		}
	}
	/**
	 * Simple Job which does nothing
	 */
	class	 DoNothing implements IJob {
		/**
		 * Answer true if the job belongs to a given family (tag)
		 */
		public boolean belongsTo(String jobFamily) {
			return true;
		}
		/**
		 * Asks this job to cancel its execution. The cancellation
		 * can take an undertermined amount of time.
		 */
		public void cancel() {
			// nothing to cancel
		}
		/**
		 * Ensures that this job is ready to run.
		 */
		public void ensureReadyToRun() {
			// always ready to do nothing
		}
		/**
		 * Execute the current job, answer whether it was successful.
		 */
		public boolean execute(IProgressMonitor progress) {
			// always succeed to do nothing
			return true;
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
			SEARCH_SCOPE,
			requestor,
			null);
	}

	// Do NOT forget that tests must start with "testPerf"

	public void testPerfIndexing() throws CoreException {
		tagAsSummary("Indexing", Dimension.CPU_TIME);
		INDEX_MANAGER.discardJobs(null); // discard all previous index jobs
		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		INDEX_MANAGER.request(new Measuring(true/*start measuring*/));
		for (int i=0, length=ALL_PROJECTS.length; i<length; i++) {
			INDEX_MANAGER.indexAll(ALL_PROJECTS[i].getProject());
		}
		INDEX_MANAGER.enable();
		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$
		INDEX_MANAGER.request(new Measuring(false /*end measuring*/));
		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$
	}

	/*
	 * Performance tests for search.
	 */
	public void testPerfSearchAllTypeNames() throws CoreException {
		tagAsSummary("Search All Type Names", Dimension.CPU_TIME);
		SearchTypeNameRequestor requestor = new SearchTypeNameRequestor();
		startMeasuring();
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			SEARCH_SCOPE, 
			requestor,
			WAIT_UNTIL_READY_TO_SEARCH,
			null);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		ALL_TYPES_NAMES = requestor.count;
	}
	public void testPerfSearchType() throws CoreException {
		tagAsSummary("Search Type all occurences", Dimension.CPU_TIME);
		startMeasuring();
		search(
//			"String",  > 65000 macthes: needs -Xmx512M
//			"Object", 13497 matches: needs -Xmx128M
//			"IResource", 5886 macthes: fails needs ?
			"JavaCore", // 2145 m	atches
			TYPE,
			ALL_OCCURRENCES, 
			this.resultCollector);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		REFERENCES[0] = this.resultCollector.count;
	}
	public void testPerfSearchField() throws CoreException {
		tagAsSummary("Search Field all occurences", Dimension.CPU_TIME);
		startMeasuring();
		search(
			"FILE", 
			FIELD,
			ALL_OCCURRENCES, 
			this.resultCollector);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		REFERENCES[1] = this.resultCollector.count;
	}
	public void testPerfSearchMethod() throws CoreException {
		tagAsSummary("Search Method all occurences", Dimension.CPU_TIME);
		startMeasuring();
		search(
			"equals", 
			METHOD,
			ALL_OCCURRENCES, 
			this.resultCollector);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		REFERENCES[2] = this.resultCollector.count;
	}
	public void testPerfSearchConstructor() throws CoreException {
		tagAsSummary("Search Constructor all occurences", Dimension.CPU_TIME);
		startMeasuring();
		search(
			"String", 
			CONSTRUCTOR,
			ALL_OCCURRENCES, 
			this.resultCollector);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		REFERENCES[3] = this.resultCollector.count;
	}
}
