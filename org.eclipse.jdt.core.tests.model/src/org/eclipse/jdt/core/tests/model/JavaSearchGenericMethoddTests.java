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

import junit.framework.Test;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;

/**
 * Test search for generic fields.
 */
public class JavaSearchGenericMethoddTests extends JavaSearchTests {

	public JavaSearchGenericMethoddTests(String name) {
		super(name);
	}
	public static Test suite() {
//		return buildTestSuite(JavaSearchGenericMethoddTests.class, "testGenericFieldReference", null);
		return buildTestSuite(JavaSearchGenericMethodTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "testGenericFieldReferenceAC04" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 8 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { -1, -1 };
	}

	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector.showAccuracy = true;
	}

	/**
	 * Bug 73277: [1.5][Search] Fields search does not work with generics
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73277)
	 */
	/*
	 * Following functionalities are tested:
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic type field
	 * 		b) multiple parameters generic type field
	 * 		c) single parameterized type field
	 * 		d) mutliple parameterized type field
	 * 	B) Search using a string pattern
	 * 		a) single name
	 * 			GenericFieldReferenceBA* tests
	 * 		b) any char characters
	 * 			GenericFieldReferenceBB* tests
	 * 		b) any string characters
	 * 			GenericFieldReferenceBB* tests
	 */
	// Search reference to a field of generic type
	public void testElementPatternSingleTypeArgument01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g4/m/def/GS.java").getType("GS");
		IMethod method = type.getMethod("standard", new String[] { "Q;" });
		IJavaSearchScope scope = getJavaSearchCUScope("JavaSearch15", "g4/m/ref", "R1.java");
		search(method, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"",
			resultCollector);
	}
}
