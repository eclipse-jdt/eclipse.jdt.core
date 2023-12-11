/*******************************************************************************
 * Copyright (c) 2023 Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/** can be manually executed */
public class JavaCoreStandaloneTest {
	@Before
	public void assume() {
		boolean usesOSGi = org.osgi.framework.FrameworkUtil.getBundle(JavaCoreStandaloneTest.class) != null;
		Assume.assumeFalse("OSGI - Needs to be executed as JUnit-test (NOT as plugin - test)", usesOSGi);
		boolean pluginInitialized = ResourcesPlugin.getPlugin() != null;
		Assume.assumeFalse("Resources - Needs to be executed as JUnit-test (NOT as plugin - test)", pluginInitialized);
	}

	/**
	 * <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1720">Issue 1720</a>
	 **/
	@Test
	public void testGetOptionsSmoke() {
		JavaCore.getOptions();
	}
}
