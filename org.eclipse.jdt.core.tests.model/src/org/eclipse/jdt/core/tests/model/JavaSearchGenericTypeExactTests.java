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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Test for search of generic types using R_EXACT_MATCH rule.
 */
public class JavaSearchGenericTypeExactTests extends JavaSearchGenericTypeTests {
	
	/**
	 * @param name
	 */
	public JavaSearchGenericTypeExactTests(String name) {
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
		TestSuite suite = new Suite(JavaSearchGenericTypeExactTests.class.getName());
		List tests = buildTestsList(JavaSearchGenericTypeExactTests.class, 1);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	/*
	 * Do not add line if this is not an exact match rule.
	 */
	void addResultLine(StringBuffer buffer, char[] line) {
		if (CharOperation.match(RESULT_EXACT_MATCH, line, true)) {
			super.addResultLine(buffer, line);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.JavaSearchGenericTypeTests#removeLastTypeArgument(char[])
	 */
	int[] removeLastTypeArgument(char[] line) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(org.eclipse.jdt.core.IJavaElement, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(IJavaElement element, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		search(element, limitTo, EXACT_RULE, scope, requestor);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#search(java.lang.String, int, int, org.eclipse.jdt.core.search.IJavaSearchScope, org.eclipse.jdt.core.search.SearchRequestor)
	 */
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		search(patternString, searchFor, limitTo, EXACT_RULE, scope, requestor);
	}
}
