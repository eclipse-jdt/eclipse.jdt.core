/*******************************************************************************
 * Copyright (c) 2026 GK Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * This test class exists for the sole purpose of printing statistics about tests run as comparison of ecj vs javac.
 * It is be integrated in {@link TestAll} as the very last test in the suite.
 */
@RunJavac
public class PrintRunJavacStats extends AbstractRegressionTest {
	public PrintRunJavacStats(Compliance compliance, TestInfo info) {
		super(compliance, info);
	}
	@Test
	public void testPrint() {
		Runtime.getRuntime().addShutdownHook(new Thread(AbstractRegressionTest::printRunJavacStats));
	}
}