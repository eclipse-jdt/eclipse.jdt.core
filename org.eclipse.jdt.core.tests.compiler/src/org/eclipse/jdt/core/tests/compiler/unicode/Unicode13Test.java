/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.unicode;

import java.util.Map;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.MinimalCompliance;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@MinimalCompliance(value=AbstractCompilerTest.F_15, singleVersion=true)
public class Unicode13Test extends AbstractRegressionTest {
public Unicode13Test(Compliance compliance, TestInfo info) {
	super(compliance, info);
}
@Test
public void test1() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\u08BE; // new unicode character in unicode 13 \n" +
			"}",
		},
		"",
		options);
}
@Test
public void test2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\ud880\\udc00; // new unicode character in unicode 13 using high and low surrogate\n" +
			"}",
		},
		"",
		options);
}
}
