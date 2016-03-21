/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Unicode19Test extends AbstractRegressionTest {
public Unicode19Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}
public void test481000() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_9);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"		public int a\u037F; // new unicode character in 7.0 \n" + 
			"}",
		},
		"",
		options);
}
public void test481000_2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"		public int a\u037F; // new unicode character in 7.0 \n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public int a" + '\u037F' + "; // new unicode character in 7.0 \n" + 
		"	            ^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" + 
		"----------\n",
		null,
		true,
		options);
}
public void test481000_3() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		public int a" + '\u037F' + "; // new unicode character in 7.0 \n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public int aͿ; // new unicode character in 7.0 \n" + 
			"	            ^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n" + 
			"----------\n",
			null,
			true,
			options);
}
public static Class<Unicode19Test> testClass() {
	return Unicode19Test.class;
}
}
