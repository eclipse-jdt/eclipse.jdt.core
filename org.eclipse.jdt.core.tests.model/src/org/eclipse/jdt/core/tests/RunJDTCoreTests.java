/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runs all JDT Core tests.
 */
public class RunJDTCoreTests extends TestCase {
public RunJDTCoreTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite(RunJDTCoreTests.class.getName());
	suite.addTest(RunBuilderTests.suite());
	suite.addTest(RunCompilerTests.suite());
	suite.addTest(RunDOMTests.suite());
	suite.addTest(RunFormatterTests.suite());
	suite.addTest(RunModelTests.suite());
	return suite;
}
}

