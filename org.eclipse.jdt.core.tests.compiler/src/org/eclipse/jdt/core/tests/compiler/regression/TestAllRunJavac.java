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

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.junit.platform.suite.api.BeforeSuite;

public class TestAllRunJavac extends TestAll {
	@BeforeSuite
	public static void runJavacFilter() {
		AbstractCompilerTest.ONLY_RUN_JAVA_OPT_IN = true;
	}
}
