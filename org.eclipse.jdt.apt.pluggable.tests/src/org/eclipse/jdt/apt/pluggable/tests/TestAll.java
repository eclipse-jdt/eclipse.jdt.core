/*******************************************************************************
 * Copyright (c) 2008, 2009 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.pluggable.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run all annotation processor tests.
 * Annotation processors may be registered by using this test plugin to extend
 * <code>org.eclipse.jdt.apt.core.annotationProcessorFactory</code>, providing
 * the name of an annotation processor factory class implemented in this plugin.
 */
public class TestAll extends TestCase {

	public TestAll(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		suite.addTest(InfrastructureTests.suite());
		suite.addTest(FilerTests.suite());
		suite.addTest(ModelTests.suite());
		suite.addTest(BuilderTests.suite());
		return suite;
	}
}
