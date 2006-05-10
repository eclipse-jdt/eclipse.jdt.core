/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Dummy test so that the 'suite' target in test.xml has at least one test.
 * (an empty target caused the tests to DNF on 2004/09/19)
 */
public class OneTest extends TestCase {

		public static Test suite() {
			return new TestSuite(OneTest.class);	
		}
		
		public void test1() {
			assertTrue(true);
		}
}
