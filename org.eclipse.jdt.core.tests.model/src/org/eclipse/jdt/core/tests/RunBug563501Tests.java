/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests;

import org.eclipse.jdt.core.tests.builder.Bug549646Test;
import org.eclipse.jdt.core.tests.compiler.regression.ModuleCompilationTests;
import org.eclipse.jdt.core.tests.model.ModuleBuilderTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runs minimal suite for reproducing bug 563501.
 * This is not part of the regular test suite.
 */
public class RunBug563501Tests extends TestCase {
	public RunBug563501Tests(String name) {
		super(name);
	}
	public static Test suite() {
		org.eclipse.jdt.core.tests.junit.extension.TestCase.TESTS_NAMES = new String[] {
					"testCompilerRegression", "testReleaseOption10", "testConvertToModule"
				};
		TestSuite suite = new TestSuite(RunBug563501Tests.class.getName());
		suite.addTest(Bug549646Test.suite());
		suite.addTest(ModuleCompilationTests.suite());
		suite.addTest(ModuleBuilderTests.suite());
		return suite;
	}
}

