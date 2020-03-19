/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.jdtcoretests;


public class AllJdtCoreTests extends org.eclipse.jdt.core.tests.RunJDTCoreTests {

    public AllJdtCoreTests(String testName) { super(testName); }
 }

/*
 * NOTE - sometimes, we have observed failures that were resolved by changing
 * the order in which the jdt-core tests are run.  If you ever get weird errors
 * in the jdt-core tests, then you may want to experiment with changing this class
 * to be something like the following.
 *
 *
public class AllJdtCoreTests extends TestCase {

	public AllJdtCoreTests(String testName)
	{
		super(testName);
	}

		public static Test suite() {
			TestSuite suite = new TestSuite(RunJDTCoreTests.class.getName());
			suite.addTest(RunModelTests.suite());
			suite.addTest(RunBuilderTests.suite());
			suite.addTest(RunCompilerTests.suite());
			suite.addTest(RunDOMTests.suite());
			suite.addTest(RunFormatterTests.suite());

			return suite;
		}
}
	*/

