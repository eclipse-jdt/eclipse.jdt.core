/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.tests.RunAllJdtModelTestsTracing;
import org.eclipse.jdt.core.tests.dom.RunAllTestsTracing;
import org.eclipse.test.TracingSuite.TracingOptions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TracingSuiteConfigurationTests extends TestCase {
	public TracingSuiteConfigurationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(TracingSuiteConfigurationTests.class);
	}

	public void testCombinedTracingSuite() {
		assertHeadlessTracing(RunAllJdtModelTestsTracing.class);
	}

	public void testDomTracingSuite() {
		assertHeadlessTracing(RunAllTestsTracing.class);
	}

	public void testModelTracingSuite() {
		assertHeadlessTracing(AllJavaModelTestsTracing.class);
	}

	private void assertHeadlessTracing(Class<?> suite) {
		TracingOptions options = suite.getAnnotation(TracingOptions.class);
		assertNotNull("Explicit tracing options are required for " + suite.getName(), options);
		assertEquals("Headless Core tests must not depend on a display server", 0, options.maxScreenshotCount());
		assertEquals("Keep the existing timeout diagnostics", 60L, options.stackDumpTimeoutSeconds());
		assertTrue("Keep reporting test starts", options.logTestStart());
	}
}
