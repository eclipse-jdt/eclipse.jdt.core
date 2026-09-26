/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

/**
 * Instances of this class and subclasses encapsulate state and behavior that is
 * shared among all tests of one test class at exactly one compliance level.
 * <p>
 * Instances are exclusively created via AbstractRegressionTest.newTestSetup(String, long)
 * and overrides.
 * </p>
 */
public class CompilerTestSetup {

	private String testName;
	public final long complianceLevel;

	public CompilerTestSetup(String testName, long complianceLevel) {
		this.testName = testName;
		this.complianceLevel = complianceLevel;
	}

	public String getName() {
		return this.testName;
	}

	public void setUp() {
		// hook for subclasses
	}

	public void tearDown() {
		// hook for subclasses
	}
}
