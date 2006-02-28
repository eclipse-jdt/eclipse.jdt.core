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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RunCompletionParserTests extends junit.framework.TestCase {

	public final static List TEST_CLASSES = new ArrayList();
	static {
		TEST_CLASSES.add(AllocationExpressionCompletionTest.class);
		TEST_CLASSES.add(AnnotationCompletionParserTest.class);
		TEST_CLASSES.add(ClassLiteralAccessCompletionTest.class);
		TEST_CLASSES.add(CompletionParserTest.class);
		TEST_CLASSES.add(CompletionParserTest2.class);
		TEST_CLASSES.add(CompletionParserTestKeyword.class);
		TEST_CLASSES.add(CompletionRecoveryTest.class);
		TEST_CLASSES.add(DietCompletionTest.class);
		TEST_CLASSES.add(EnumCompletionParserTest.class);
		TEST_CLASSES.add(ExplicitConstructorInvocationCompletionTest.class);
		TEST_CLASSES.add(FieldAccessCompletionTest.class);
		TEST_CLASSES.add(GenericsCompletionParserTest.class);
		TEST_CLASSES.add(InnerTypeCompletionTest.class);
		TEST_CLASSES.add(JavadocCompletionParserTest.class);
		TEST_CLASSES.add(LabelStatementCompletionTest.class);
		TEST_CLASSES.add(MethodInvocationCompletionTest.class);
		TEST_CLASSES.add(NameReferenceCompletionTest.class);
		TEST_CLASSES.add(ReferenceTypeCompletionTest.class);
	}

	public static Class[] getTestClasses() {
		int size = TEST_CLASSES.size();
		Class[] testClasses = new Class[size];
		TEST_CLASSES.toArray(testClasses);
		return testClasses;
	}

	public RunCompletionParserTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunCompletionParserTests.class.getName());

		// Get all classes
		Class[] allClasses = getTestClasses();

		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
		TestCase.ONLY_SUFFIX = null;

		// Add all tests suite of tests
		for (int i = 0, length = allClasses.length; i < length; i++) {
			Class testClass = allClasses[i];

			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
				ts.addTest(suite);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return ts;
	}
}
