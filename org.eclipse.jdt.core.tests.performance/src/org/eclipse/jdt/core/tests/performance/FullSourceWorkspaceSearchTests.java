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
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.SearchBasicEngine;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceSearchTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {
	
	static int[] REFERENCES = new int[4];
	private static int COUNT = 0;

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

	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
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
			requestor,
			null);
	}

	// Do NOT forget that tests must start with "testPerf"
	public void testPerfSearchType() throws CoreException {
		tagAsSummary("Search Type all occurences", Dimension.CPU_TIME);
		startMeasuring();
		search(
//			"String",  > 65000 macthes: needs -Xmx512M
//			"Object", 13497 matches: needs -Xmx128M
//			"IResource", 5886 macthes: fails needs ?
			"JavaCore", // 2597 m	atches
			TYPE,
			ALL_OCCURRENCES, 
			SearchBasicEngine.createJavaSearchScope(ALL_PROJECTS),
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
			SearchBasicEngine.createJavaSearchScope(ALL_PROJECTS),
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
			SearchBasicEngine.createJavaSearchScope(ALL_PROJECTS),
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
			SearchBasicEngine.createJavaSearchScope(ALL_PROJECTS),
			this.resultCollector);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		// store counter
		REFERENCES[3] = this.resultCollector.count;
	}
}
