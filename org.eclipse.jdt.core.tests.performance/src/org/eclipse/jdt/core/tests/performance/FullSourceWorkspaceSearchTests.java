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

import java.io.IOException;
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
	
	static int[] REFERENCES = new int[4];
	private static int COUNT = 0;
	private static IJavaSearchScope SEARCH_SCOPE;

	/**
	 * @param name
	 */
	public FullSourceWorkspaceSearchTests(String name) {
		super(name);
	}

//	static {
//		TESTS_NAMES = new String[] { "testPerfSearchType" };
//	}
	public static Test suite() {
		Test suite = buildSuite(FullSourceWorkspaceSearchTests.class);
		COUNT = suite.countTestCases();
		return suite;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.resultCollector = new JavaSearchResultCollector();
		if (SEARCH_SCOPE == null) {
			SEARCH_SCOPE = SearchEngine.createJavaSearchScope(ALL_PROJECTS);
			waitUntilReadyToSearch();
		}
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
			System.out.println("Search performance test statistics:");
			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			System.out.println("  - "+intFormat.format(REFERENCES[0])+" type references found.");
			System.out.println("  - "+intFormat.format(REFERENCES[1])+" field references found.");
			System.out.println("  - "+intFormat.format(REFERENCES[2])+" method references found.");
			System.out.println("  - "+intFormat.format(REFERENCES[3])+" constructor references found.");
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.performance.FullSourceWorkspaceTests#setFullSourceWorkspace()
	 */
	private void waitUntilReadyToSearch() throws IOException, CoreException {
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		IJob doNothing = new IJob() {
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
		};
		if (DEBUG) System.out.print("Wait until ready to search..."); //$NON-NLS-1$
		indexManager.performConcurrentJob(doNothing, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		if (DEBUG) {
			if (indexManager.awaitingJobsCount() == 0)
				System.out.println("done"); //$NON-NLS-1$
			else
				System.err.println(" KO: remaining jobs="+indexManager.awaitingJobsCount()); //$NON-NLS-1$
		}
	}

	// Do NOT forget that tests must start with "testPerf"
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
