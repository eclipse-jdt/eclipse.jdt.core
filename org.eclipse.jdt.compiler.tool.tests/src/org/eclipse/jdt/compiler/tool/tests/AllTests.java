/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class AllTests extends TestCase {
	// run all tests
	@SuppressWarnings("unchecked")
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(CompilerToolTests.suite());
		return suite;
	}
}
