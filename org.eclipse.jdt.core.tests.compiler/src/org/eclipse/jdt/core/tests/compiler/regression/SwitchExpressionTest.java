/*******************************************************************************
 * Copyright (c) 2018 GK Software SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SwitchExpressionTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testSimpleExpressions" };
	}
	
	public static Class<?> testClass() {
		return SwitchExpressionTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9); // FIXME
	}
	public SwitchExpressionTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_9); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_9);
		return defaultOptions;
	}

	public void testSimpleExpressions() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> 2;\n" +
				"			default -> i * 2;\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n"
			},
			"6");
	}
	public void testSwitchExpression_531714_002() {
		runConformTest(
			new String[] {
				"X.java",
				"package se1;\n" +
				"\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"	static int twice(int i) throws IOException {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"		//	case 2 -> 2;\n" +
				"			case 3 -> throw new IOException(\"hello\");\n" +
				"			default -> throw new IOException(\"world\");\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		try {\n" +
				"		    System.out.print(twice(3));\n" +
				"		} catch (IOException e) {\n" +
				"		  //\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"6");
	}
}
	