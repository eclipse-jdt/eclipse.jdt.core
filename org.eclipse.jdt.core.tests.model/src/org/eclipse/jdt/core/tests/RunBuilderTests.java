/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import org.eclipse.jdt.core.tests.builder.BuilderTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runs all Java builder tests.
 */
public class RunBuilderTests extends TestCase {
public RunBuilderTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite(RunBuilderTests.class.getName());
	suite.addTest(BuilderTests.suite());
	return suite;
}
}

