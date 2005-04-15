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

import org.eclipse.jdt.core.tests.dom.RunAllTests;

/**
 * Runs all DOM AST tests.
 */
public class RunDOMTests extends TestCase {

public RunDOMTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite(RunDOMTests.class.getName());
	suite.addTest(RunAllTests.suite());
	return suite;
}
}
