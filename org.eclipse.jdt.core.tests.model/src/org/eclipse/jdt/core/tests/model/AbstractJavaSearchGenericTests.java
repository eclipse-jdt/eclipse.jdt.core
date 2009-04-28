/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;

/**
 * Abstract class for generic search tests.
 */
public class AbstractJavaSearchGenericTests extends JavaSearchTests {

	static char[] RESULT_POTENTIAL_MATCH = "*] POTENTIAL_*".toCharArray();
	static char[] RESULT_EXACT_MATCH = "*] EXACT_*".toCharArray();
	static char[] RESULT_ERASURE_MATCH = "*] ERASURE_*".toCharArray();
	int matchRule;

	public AbstractJavaSearchGenericTests(String name, int matchRule) {
		super(name);
		this.matchRule = matchRule;
	}

	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector.showAccuracy(true);
		this.resultCollector.showRule();
	}

	/*
	 * Add given line to given buffer.
	 */
	void addResultLine(StringBuffer buffer, char[] line) {
		if (buffer.length() > 0) buffer.append('\n');
		buffer.append(line);
	}

	/*
	 * Clean results from all lines which have not expected match rule.
	 */
	final String cleanResults(String expected) {
		char[][] lines = CharOperation.splitOn('\n', expected.toCharArray());
		StringBuffer buffer = new StringBuffer(expected.length());
		for (int i=0, n=lines.length; i<n; i++) {
			addResultLine(buffer, lines[i]);
		}
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * Overridden to remove all last type arguments from expected string.
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertSearchResults(java.lang.String, java.lang.String, java.lang.Object)
	 */
	protected void assertSearchResults(String message, String expected, JavaSearchResultCollector collector) {
		String actual = collector.toString();
		String trimmed = cleanResults(expected);
		if (!trimmed.equals(actual)) {
			System.out.println(getName()+" expected result is:");
			System.out.print(displayString(actual, this.tabs));
			System.out.println("");
		}
		assertEquals(
			message,
			trimmed,
			actual
		);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(org.eclipse.jdt.core.IJavaElement, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(IJavaElement element, int limitTo) throws CoreException {
		search(element, limitTo, this.matchRule, getJavaSearchScope15(), this.resultCollector);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(java.lang.String, int, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
		search(patternString, searchFor, limitTo, this.matchRule, getJavaSearchScope15(), this.resultCollector);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(org.eclipse.jdt.core.IJavaElement, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(IJavaElement element, int limitTo, IJavaSearchScope scope) throws CoreException {
		search(element, limitTo, this.matchRule, scope, this.resultCollector);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(java.lang.String, int, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope) throws CoreException {
		search(patternString, searchFor, limitTo, this.matchRule, scope, this.resultCollector);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(org.eclipse.jdt.core.IJavaElement, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(IJavaElement element, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		search(element, limitTo, this.matchRule, scope, requestor);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(java.lang.String, int, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		search(patternString, searchFor, limitTo, this.matchRule, scope, requestor);
	}
}
