/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import org.eclipse.jdt.core.tests.junit.extension.PerformanceTestSuite;
import org.eclipse.jdt.core.tests.model.NameLookupTests2;

import junit.framework.Test;
import junit.framework.TestCase;

public class AllPerformanceTests extends TestCase {

	public static Test suite() {
		PerformanceTestSuite suite = new PerformanceTestSuite(AllPerformanceTests.class.getName());
		suite.addTestSuite(FullSourceWorkspaceTests.class);
		suite.addTestSuite(NameLookupTests2.class);
		return suite;
	}
}
