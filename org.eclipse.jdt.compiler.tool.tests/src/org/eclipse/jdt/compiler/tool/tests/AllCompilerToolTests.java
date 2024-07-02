/*******************************************************************************
 * Copyright (c) 2006, 2021 IBM Corporation and others.
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
package org.eclipse.jdt.compiler.tool.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Helper class to run all the compiler tool tests
 */
public class AllCompilerToolTests extends TestCase {
	// run all tests
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(CompilerToolTests.class);
		suite.addTestSuite(CompilerToolJava9Tests.class);
		suite.addTest(CompilerInvocationTests.suite());
		suite.addTestSuite(InMemoryCompilationTest.class);
		return suite;
	}
}
