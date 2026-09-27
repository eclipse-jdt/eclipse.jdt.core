/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

@MinimalCompliance(value=AbstractCompilerTest.F_13, singleVersion=true)
public class Unicode12_1Test extends AbstractRegressionTest {
public Unicode12_1Test(Compliance compliance, TestInfo info) {
	super(compliance, info);
}
@Test
public void test1() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_13);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\uA7BA; // new unicode character in unicode 12.0 \n" +
			"}",
		},
		"",
		options);
}
@Test
public void test2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_12);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		public int a\\uA7BA; // new unicode character in unicode 12.0 \n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public int a\\uA7BA; // new unicode character in unicode 12.0 \n" +
		"	            ^^^^^^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		options);
}
}
