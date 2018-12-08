/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		return buildMinimalComplianceTestSuite(testClass(), F_12);
	}
	public SwitchExpressionTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_12); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_12);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_12);
		return defaultOptions;
	}
	

	public void testSimpleExpressions() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> i * 0;\n" +
				"			case 1 -> 2;\n" +
				"			default -> 3;\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n"
			},
			"3");
	}
	public void testSwitchExpression_531714_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
						"	static int twice(int i) throws Exception {\n"+
						"		int tw = switch (i) {\n"+
						"			case 0 -> 0;\n"+
						"			case 1 -> { \n"+
						"				System.out.println(\"do_not_print\");\n"+
						"				break 1;\n"+
						"			} \n"+
						"			case 3 -> throw new Exception();\n"+
						"			default -> throw new Exception();\n"+
						"		};\n"+
						"		return tw;\n"+
						"	}\n"+
						"	public static void main(String[] args) {\n"+
						"		try {\n"+
						"		    try {\n"+
						"				System.out.print(twice(3));\n"+
						"			} catch (Exception e) {\n"+
						"				// TODO Auto-generated catch block\n"+
						"				e.printStackTrace();\n"+
						"			}\n"+
						"		} catch (Exception e) {\n"+
						"		System.out.print(\"Got Exception\");\n"+
						"		}\n"+
						"	}\n"+
						"}\n"
			},
			"Got Exception");
	}
	public void testBug531714_error_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    twice(1);\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	int tw = switch (i) {\n" + 
			"		};\n" + 
			"	         ^^^^^^^^^^^^^^^^\n" + 
			"A switch expression should have a non-empty switch block\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	int tw = switch (i) {\n" + 
			"	                 ^\n" + 
			"A switch expression should have a default case\n" + 
			"----------\n");
	}
	public void testBug531714_error_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    twice(1);\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"			case \"hello\" -> throw new java.io.IOException(\"hello\");\n" +
				"			default -> throw new java.io.IOException(\"world\");\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	case \"hello\" -> throw new java.io.IOException(\"hello\");\n" + 
			"	     ^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to int\n" + 
			"----------\n");
	}
	public void testBug531714_error_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    twice(1);\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"		    case 2 -> 2;\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	int tw = switch (i) {\n" + 
			"	                 ^\n" + 
			"A switch expression should have a default case\n" + 
			"----------\n");
	}
	/**
	 * Add a test case for enum
	 * If the type of the selector expression is an enum type, 
	 * then the set of all the case constants associated with the switch block
	 *  must contain all the enum constants of that enum type
	 *  Add a missing enum test case
	 */
	public void _testBug531714_error_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = () -> {\n" +
				"      int z = 10;\n" +
				"    };\n" +
				"    i++;\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"		//	case 2 -> 2;\n" +
				"			case \"hello\" -> throw new IOException(\"hello\");\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	int tw = switch (i) {\n" + 
			"	      ^^^^^\n" + 
			" The switch expression should have a default case\n" + 
			"----------\n");
	}
	/*
	 * should compile - test for adding additional nesting in variables
	 * dev note: ref consumeToken().case Switch 
	 */
	public void testBug531714_error_007() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"	static int foo(int i) {\n"+
				"		int tw = \n"+
				"		switch (i) {\n"+
				"			case 1 -> \n"+
				"			 {\n"+
				" 				int z = 100;\n"+
				" 				break z;\n"+
				"			}\n"+
				"			default -> {\n"+
				"				break 12;\n"+
				"			}\n"+
				"		};\n"+
				"		return tw;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.print(foo(1));\n"+
				"	}\n"+
				"}\n"
			},
			"100");
	}

}
	