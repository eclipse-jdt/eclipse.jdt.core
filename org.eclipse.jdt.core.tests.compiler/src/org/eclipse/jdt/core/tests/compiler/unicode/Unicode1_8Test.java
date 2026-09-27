/*******************************************************************************
 * Copyright (c) 2014, 2016 IBM Corporation and others.
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

@SuppressWarnings({ "unchecked", "rawtypes" })
@MinimalCompliance(value=AbstractCompilerTest.F_1_8, singleVersion=true)
public class Unicode1_8Test extends AbstractRegressionTest {
public Unicode1_8Test(Compliance compliance, TestInfo info) {
	super(compliance, info);
}
@Test
public void test426214() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		int a\\u058f = 0; // new unicode character in 6.2.0 \n" +
			"		String a41\\u08fc; // new unicode character in 6.2.0\n" +
			"		float a\\u057f = 1;\n" +
			"}",
		},
		"",
		options);
}
@Test
public void test426214_4() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"		int a\\u061C = 0; // new unicode character in 6.3.0 \n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int a\\u061C = 0; // new unicode character in 6.3.0 \n" +
		"	     ^^^^^^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		options);
}
}
