/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
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

import java.io.IOException;
import org.junit.jupiter.api.TestInfo;

/** Run tests from the super class with (legacy) declaration annotations. */
public class NullDeclarationAnnotationTest extends NullAnnotationTest {

	public NullDeclarationAnnotationTest(Compliance compliance, TestInfo info) {
		super(compliance, info);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug545715" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public boolean useDeclarationAnnotations() {
		return true;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.TEST_JAR_SUFFIX = ".jar";
	}

	@Override
	protected String getAnnotationLibPath() throws IOException {
		return getAnnotationV1LibPath();
	}

}
