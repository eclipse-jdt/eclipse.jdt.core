/*******************************************************************************
 * Copyright (c) 2006, 2020 IBM, BEA Systems, Inc. and others
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
 *    IBM Corporation - Java 8 support
 *
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Helper class to run all the compiler tool tests
 */
public class AllAptTests extends TestCase {
	// run all tests
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(FileManagerTests.class);
		suite.addTestSuite(BatchDispatchTests.class);
		suite.addTestSuite(ModelTests.class);
		suite.addTestSuite(MessagerTests.class);
		suite.addTestSuite(FilerTests.class);
		suite.addTestSuite(ModelUtilTests.class);
		suite.addTestSuite(NegativeTests.class);
		suite.addTestSuite(Java8ElementsTests.class);
		suite.addTestSuite(Java9ElementsTests.class);
		suite.addTestSuite(Java11ElementsTests.class);
		suite.addTestSuite(Java12ElementsTests.class);
		suite.addTestSuite(RecordElementsTests.class);
		suite.addTestSuite(SealedTypeElementsTests.class);
		suite.addTestSuite(AnnotationProcessorTests.class);
		return suite;
	}
}
