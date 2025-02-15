/*******************************************************************************
 * Copyright (c) 2025 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchDelegate;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchDelegateDiscovery;

public class JavaSearchExtensionTest extends AbstractJavaSearchTests {
	private static final String DELEGATE_SYSPROP = "IJavaSearchDelegate";
	public JavaSearchExtensionTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(JavaSearchExtensionTest.class, ALPHABETICAL_SORT);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
	}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public void testDelegateFound() throws JavaModelException {
		String oldSystemProperty  = System.getProperty(DELEGATE_SYSPROP);

		try {
			System.setProperty(DELEGATE_SYSPROP, "org.eclipse.jdt.core.tests.model.TestJavaSearchDelegate");
			IJavaSearchDelegate del = JavaSearchDelegateDiscovery.getInstance();
			assertNotNull(del);
			assertTrue(del instanceof TestJavaSearchDelegate);

			System.clearProperty(DELEGATE_SYSPROP);
			del = JavaSearchDelegateDiscovery.getInstance();
			assertNull(del);

			System.setProperty(DELEGATE_SYSPROP, "unknownVal");
			del = JavaSearchDelegateDiscovery.getInstance();
			assertNull(del);
		} finally {
			if( oldSystemProperty != null )
				System.setProperty(DELEGATE_SYSPROP, oldSystemProperty);
			else
				System.clearProperty(DELEGATE_SYSPROP);
		}
	}
}
