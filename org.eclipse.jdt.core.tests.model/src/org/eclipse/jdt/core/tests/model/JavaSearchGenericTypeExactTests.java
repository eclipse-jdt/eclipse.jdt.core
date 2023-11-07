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
package org.eclipse.jdt.core.tests.model;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test for search of generic types using R_EXACT_MATCH rule.
 */
@SuppressWarnings("rawtypes")
public class JavaSearchGenericTypeExactTests extends JavaSearchGenericTypeTests {

	public JavaSearchGenericTypeExactTests(String name) {
		super(name, EXACT_RULE);
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
		List tests = buildTestsList(JavaSearchGenericTypeExactTests.class, 1, 0/* do not sort*/);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	/*
	 * Do not add line if this is not an exact match rule.
	 */
	@Override
	void addResultLine(StringBuffer buffer, char[] line) {
		if (CharOperation.match(RESULT_EXACT_MATCH, line, true)) {
			super.addResultLine(buffer, line);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.JavaSearchGenericTypeTests#removeLastTypeArgument(char[])
	 */
	@Override
	int[] removeLastTypeArgument(char[] line) {
		return null;
	}
}
