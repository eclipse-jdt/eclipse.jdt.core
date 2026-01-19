/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

public class GetDerivedSourceTests extends ModifyingResourceTests {

	private IJavaProject javaProject;

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "TypeParameterBug73884" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 13 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
	}

	public GetDerivedSourceTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(GetDerivedSourceTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.javaProject = setUpJavaProject("kotlin-example");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("kotlin-example");
		super.tearDownSuite();
	}

	/**
	 * Ensure kotlin source library with folder structure not exact as class jar returns type source
	 * https://github.com/eclipse-jdt/eclipse.jdt.core/pull/4769
	 */
	public void testKotlinType() throws Exception {
		IType type = this.javaProject.findType("org.example.Library");
		String typeSource = type.getSource();

		String expectedSource = """
			class Library {
			    fun someLibraryMethod(): Boolean {
			        return true
			    }
			}
			""";
		assertSourceEquals("Unexpected source", expectedSource, typeSource);
	}
}
