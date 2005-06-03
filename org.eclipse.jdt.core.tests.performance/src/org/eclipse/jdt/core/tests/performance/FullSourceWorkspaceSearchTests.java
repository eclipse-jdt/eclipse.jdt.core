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
	private static PrintStream[] LOG_STREAMS = new PrintStream[LOG_TYPES.length];

	// Scopes
	IJavaSearchScope workspaceScope;

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
		this.workspaceScope = SearchEngine.createWorkspaceScope();
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

	protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
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
			this.workspaceScope,
			this.resultCollector,
			null);
	}

	/**
	 * Clean last category table cache
	 * @param type Tells whether previous search was a type search or not
	 */
	protected void cleanCategoryTableCache(boolean type) throws CoreException {
		long time = System.currentTimeMillis();
		if (type) {
			search("foo", FIELD, DECLARATIONS);
		} else {
			search("Foo", TYPE, DECLARATIONS);
		}
		if (DEBUG) System.out.println("Time to clean category table cache: "+(System.currentTimeMillis()-time));
	}

	/**
	 * Performance tests for search: Indexing.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Consider this initial indexing jobs as warm-up for this test.
	 */
	public void testIndexing() throws CoreException {
		tagAsSummary("Search>Indexing", true); // put in fingerprint
	
		// Wait for indexing end (we use initial indexing as warm-up)
		waitUntilIndexesReady();
		
		// Remove all previous indexing
		INDEX_MANAGER.removeIndexFamily(new Path(""));
		INDEX_MANAGER.reset();
		
		// Clean memory
		runGc();
	
		// Restart brand new indexing
		INDEX_MANAGER.request(new Measuring(true/*start measuring*/));
		for (int i=0, length=ALL_PROJECTS.length; i<length; i++) {
			INDEX_MANAGER.indexAll(ALL_PROJECTS[i].getProject());
		}
		
		// Wait for indexing end
		waitUntilIndexesReady();
		
		// Commit
		INDEX_MANAGER.request(new Measuring(false /*end measuring*/));
		waitUntilIndexesReady();
	}

	/**
	 * Performance tests for search: Declarations Types Names.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 * 
	 * @deprecated As we use deprecated API
	 */
	public void testSearchAllTypeNames() throws CoreException {
		tagAsSummary("Search>Names>Workspace", true); // put in fingerprint
		SearchTypeNameRequestor requestor = new SearchTypeNameRequestor();

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			this.workspaceScope, 
			requestor,
			WAIT_UNTIL_READY_TO_SEARCH,
			null);

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(true);
			startMeasuring();
			for (int j=0; j<ITERATIONS_COUNT; j++) {
				new SearchEngine().searchAllTypeNames(
					null,
					null,
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
					IJavaSearchConstants.TYPE,
					this.workspaceScope, 
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
	public void testSearchType() throws CoreException {
		tagAsSummary("Search>Occurences>Types", true); // put in fingerprint

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		search("JavaCore", TYPE, ALL_OCCURRENCES);

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(true);
			startMeasuring();
			search("JavaCore", TYPE, ALL_OCCURRENCES);
			stopMeasuring();
		}
		
		// Commit
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
	public void testSearchField() throws CoreException {
		tagAsSummary("Search>Occurences>Fields", true); // put in fingerprint

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		search("FILE", FIELD, ALL_OCCURRENCES);

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(false);
			startMeasuring();
			search("FILE", FIELD, ALL_OCCURRENCES);
			stopMeasuring();
		}
		
		// Commit
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
	public void testSearchMethod() throws CoreException {
		tagAsSummary("Search>Occurences>Methods", true); // put in fingerprint

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		search("equals", METHOD, ALL_OCCURRENCES);

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(false);
			startMeasuring();
			search("equals", METHOD, ALL_OCCURRENCES);
			stopMeasuring();
		}
		
		// Commit
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
	public void testSearchConstructor() throws CoreException {
		tagAsSummary("Search>Occurences>Constructors", true); // put in fingerprint

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		search("String", CONSTRUCTOR, ALL_OCCURRENCES);

		// Clean memory
		runGc();

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(false);
			startMeasuring();
			search("String", CONSTRUCTOR, ALL_OCCURRENCES);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();

		// Store counter
		REFERENCES[3] = this.resultCollector.count;
	}
}
