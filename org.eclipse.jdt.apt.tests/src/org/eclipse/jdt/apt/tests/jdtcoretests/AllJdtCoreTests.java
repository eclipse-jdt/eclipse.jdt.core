/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.jdtcoretests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.RunBuilderTests;
import org.eclipse.jdt.core.tests.RunCompilerTests;
import org.eclipse.jdt.core.tests.RunDOMTests;
import org.eclipse.jdt.core.tests.RunFormatterTests;
import org.eclipse.jdt.core.tests.RunJDTCoreTests;
import org.eclipse.jdt.core.tests.RunModelTests;

//
//  BUGZILLA 101144
// 
// This code should look like this when bug 101144 is fixed:
//
// public class AllJdtCoreTests extends org.eclipse.jdt.core.tests.RunJDTCoreTests {
//
//    public AllJdtCoreTests(String testName) { super(testName); }
// }
//

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
	

