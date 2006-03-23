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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.test.performance.Performance;

/**
 */
public class FullSourceWorkspaceSearchTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {

	// Tests counters
	private static int TESTS_COUNT = 0;
	private final static int ITERATIONS_COUNT = 10;

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[LOG_TYPES.length];

	/**
	 * @param name
	 */
	public FullSourceWorkspaceSearchTests(String name) {
		super(name);
	}

	static {
//		org.eclipse.jdt.internal.core.search.processing.JobManager.VERBOSE = true;
//		TESTS_NAMES = new String[] { "testSearchField" };
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
		createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceSearchTests.class;
	}

	protected void setUp() throws Exception {
		super.setUp();
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
	
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope, JavaSearchResultCollector resultCollector) throws CoreException {
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
			scope,
			resultCollector,
			null);
	}

	/**
	 * Clean last category table cache
	 * @param type Tells whether previous search was a type search or not
	 * @param scope TODO
	 */
	protected void cleanCategoryTableCache(boolean type, IJavaSearchScope scope, JavaSearchResultCollector resultCollector) throws CoreException {
		long time = System.currentTimeMillis();
		if (type) {
			search("foo", FIELD, DECLARATIONS, scope, resultCollector);
		} else {
			search("Foo", TYPE, DECLARATIONS, scope, resultCollector);
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
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			scope, 
			requestor,
			WAIT_UNTIL_READY_TO_SEARCH,
			null);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	All type names = "+intFormat.format(requestor.count));

		// Measures
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(true, scope, resultCollector);
			runGc();
			startMeasuring();
			for (int j=0; j<ITERATIONS_COUNT; j++) {
				new SearchEngine().searchAllTypeNames(
					null,
					null,
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
					IJavaSearchConstants.TYPE,
					scope, 
					requestor,
					WAIT_UNTIL_READY_TO_SEARCH,
					null);
			}
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
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
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT }, IJavaSearchScope.SOURCES);
		String name = "JavaCore";
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(name, TYPE, ALL_OCCURRENCES, scope, resultCollector);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	- "+intFormat.format(resultCollector.count)+" occurences for type '"+name+"' in project "+JDT_CORE_PROJECT.getElementName());

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(true, scope, resultCollector);
			runGc();
			startMeasuring();
			search(name, TYPE, ALL_OCCURRENCES, scope, resultCollector);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
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
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT }, IJavaSearchScope.SOURCES);
		String name = "TYPE";
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(name, FIELD, ALL_OCCURRENCES, scope, resultCollector);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	- "+intFormat.format(resultCollector.count)+" occurences for field '"+name+"' in project "+JDT_CORE_PROJECT.getElementName());

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(false, scope, resultCollector);
			runGc();
			startMeasuring();
			search(name, FIELD, ALL_OCCURRENCES, scope, resultCollector);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Performance tests for search: All occurences of a method.
	 * This search do NOT use binding resolution.
	 * 
	 * TODO (frederic) remove from fingerprint as soon as results on
	 * 	{@link #testSearchBinaryMethod()} will have been verified in releng output.
	 */
	public void testSearchMethod() throws CoreException {
		tagAsSummary("Search>Occurences>Methods", true); // put in fingerprint
		setComment(Performance.EXPLAINS_DEGRADATION_COMMENT, "Test is not enough stable and will be replaced by another one...");
	
		// Wait for indexing end
		waitUntilIndexesReady();
	
		// Warm up
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT }, IJavaSearchScope.SOURCES);
		String name = "equals";
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(name, METHOD, ALL_OCCURRENCES, scope, resultCollector);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	- "+intFormat.format(resultCollector.count)+" occurences for method '"+name+"' in project "+JDT_CORE_PROJECT.getElementName());
	
		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			// clean before test
			cleanCategoryTableCache(false, scope, resultCollector);
			runGc();
	
			// test
			startMeasuring();
			search(name, METHOD, ALL_OCCURRENCES, scope, resultCollector);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Performance tests for search: All occurences of a method.
	 * This search use binding resolution.
	 * 
	 * @since 3.2 M6
	 * 
	 * TODO (frederic) put in fingerprint as soon as results will have been verified in releng output.
	 */
	public void testSearchBinaryMethod() throws CoreException {
		tagAsSummary("Search>Occurences>Methods>Resolved", false); // put in fingerprint

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT }, IJavaSearchScope.SOURCES);
		String name = "java.lang.Object.hashCode()";
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(name, METHOD, ALL_OCCURRENCES, scope, resultCollector);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	- "+intFormat.format(resultCollector.count)+" occurences for method '"+name+"' in project "+JDT_CORE_PROJECT.getElementName());

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			// clean before test
			cleanCategoryTableCache(false, scope, resultCollector);
			runGc();

			// test
			startMeasuring();
			search(name, METHOD, ALL_OCCURRENCES, scope, resultCollector);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
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
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT }, IJavaSearchScope.SOURCES);
		String name = "String";
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(name, CONSTRUCTOR, ALL_OCCURRENCES, scope, resultCollector);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	- "+intFormat.format(resultCollector.count)+" occurences for constructor '"+name+"' in project "+JDT_CORE_PROJECT.getElementName());

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(false, scope, resultCollector);
			runGc();
			startMeasuring();
			search(name, CONSTRUCTOR, ALL_OCCURRENCES, scope, resultCollector);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Performance tests for search: Package Declarations.
	 * 
	 * First wait that already started indexing jobs end before perform test.
	 * Perform one search before measure performance for warm-up.
	 */
	public void testSearchPackageDeclarations() throws CoreException {
		tagAsSummary("Search>Declarations>Packages", false); // do NOT put in fingerprint

		// Wait for indexing end
		waitUntilIndexesReady();

		// Warm up
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT }, IJavaSearchScope.SOURCES);
		String name = "*";
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(name, PACKAGE, DECLARATIONS, scope, resultCollector);
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("	- "+intFormat.format(resultCollector.count)+" package declarations in project "+JDT_CORE_PROJECT.getElementName());

		// Measures
		for (int i=0; i<MEASURES_COUNT; i++) {
			cleanCategoryTableCache(false, scope, resultCollector);
			runGc();
			startMeasuring();
			search(name, PACKAGE, DECLARATIONS, scope, resultCollector);
			stopMeasuring();
		}
		
		// Commit
		commitMeasurements();
		assertPerformance();
	}
}
