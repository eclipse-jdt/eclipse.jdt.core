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
package org.eclipse.jdt.core.tests.model;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchPattern;

/**
 * Test for search of generic types using raw match selection.
 */
public class JavaSearchGenericTypeErasureTests extends JavaSearchGenericTypeTests {
	
	/**
	 * @param name
	 */
	public JavaSearchGenericTypeErasureTests(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testArray";
//		TESTS_NAMES = new String[] { "ParameterizedElement" };
//		TESTS_NUMBERS = new int[] { 1 };
//		TESTS_RANGE = new int[] { 6, -1 };
	}
	public static Test suite() {
		TestSuite suite = new Suite(JavaSearchGenericTypeErasureTests.class.getName());
		List tests = buildTestsList(JavaSearchGenericTypeErasureTests.class, 1);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	/*
	 * Remove last type arguments from a line of an expected result.
	 * This line contains one search match print out.
	 */
	int[] removeLastTypeArgument(char[] line) {
		int idx=line.length-1;
		while (line[idx] != ']') idx--;
		if (line[--idx] != '>') return null;
		int n = 1;
		int end = idx+1;
		while(idx>=0 && n!= 0) {
			switch (line[--idx]) {
				case '<': n--; break;
				case '>': n++; break;
			}
		}
		if (n!= 0) {
			// something wrong here...
			return null;
		}
		int start = idx;
		while (idx>=0 && line[idx] != '[') idx--;
		if (idx > 0)
			return new int[] { start, end };
		// We should have opened a bracket!
		return null;
	}

	void cleanLine(StringBuffer buffer, char[] line) {
		int[] positions = removeLastTypeArgument(line);
		if (buffer.length() > 0) buffer.append('\n');
		if (positions == null) {
			buffer.append(line);
		} else if (positions != null) {
			int stop = positions[0];
			int restart = positions[1];
			buffer.append(line, 0, stop);
			buffer.append(line, restart, line.length-restart);
		} else {
			buffer.append(line);
		}
	}

	protected void search(IJavaElement element, int limitTo, boolean rawMatch, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		int matchRule = SearchPattern.R_EXACT_MATCH|SearchPattern.R_CASE_SENSITIVE;
		matchRule |= SearchPattern.R_ERASURE_MATCH; // force rule to match erasure
		this.searchPattern = (JavaSearchPattern) SearchPattern.createPattern(element, limitTo, matchRule);
		new SearchEngine().search(
			this.searchPattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null
		);
	}

	protected void search(String patternString, int searchFor, int limitTo, boolean rawMatch, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		int matchMode = patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1
			? SearchPattern.R_PATTERN_MATCH
			: SearchPattern.R_EXACT_MATCH;
		int matchRule = matchMode | SearchPattern.R_CASE_SENSITIVE;
		matchRule |= SearchPattern.R_ERASURE_MATCH; // force rule to match erasure
		this.searchPattern = (JavaSearchPattern) SearchPattern.createPattern(
			patternString, 
			searchFor,
			limitTo, 
			matchRule);
		new SearchEngine().search(
			this.searchPattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
	}
}
