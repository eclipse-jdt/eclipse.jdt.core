/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression.latest;

import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class TextBlockTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "test003" };
	}
	
	public static Class<?> testClass() {
		return TextBlockTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_13);
	}
	public TextBlockTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_13);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_13);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}
	
	@Override
	protected void runConformTest(String[] testFiles, Map<String, String> customOptions) {
		super.runConformTest(testFiles, "", null, true, new String[] {"--enable-preview"}, customOptions, null);
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		super.runConformTest(testFiles, expectedOutput, null, true, new String[] {"--enable-preview"}, customOptions, null);
	}
	public void test001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\"\"\"\";\n" + 
				"	                               ^^\n" + 
				"Syntax error on token \"\"\"\", invalid AssignmentOperator\n" + 
				"----------\n");
	}
	public void test002() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\" \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\" \"\"\";\n" + 
				"	                               ^^^\n" + 
				"Syntax error on token \"\" \"\", invalid AssignmentOperator\n" + 
				"----------\n");
	}
	public void test003() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\"\n" + 
				"\";\n" + 
				"	                             ^^^^^\n" + 
				"Text block is not properly closed with the delimiter\n" + 
				"----------\n");
	}
	/*
	 * negative - unescaped '\' in a text block
	 */
	public void test004() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"abc\\def" + 
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\"\n" + 
				"abc\\def\"\"\";\n" + 
				"	                             ^^^^^^^^^^^^\n" + 
				"Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ )\n" + 
				"----------\n");
	}
	/* empty text block */
	public void test005() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - escaped '\'
	 */
	public void test006() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"abc\\\\def\"" + 
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"abc\\def\"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n 
	 */
	public void test007() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"    line 1\n" + 
						"    line 2\n" + 
						"  \n" + 
						"  line 3\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"line 1\n" + // test framework trims the leading whitespace
				"  line 2\n" + 
				"\n" + 
				"line 3", 
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n and \r
	 */
	public void test008() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"    line 1\n" + 
						"    line 2\r" + 
						"  \r" + 
						"  line 3\n\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"line 1\n" + 
				"  line 2\n" + 
				"\n" + 
				"line 3", // the trailing whitespace is trimmed by the test framework
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using unescaped '"' in text block
	 */
	public void test009() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\"abc-def\"" + 
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"\"abc-def\"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using escaped '"' in text block
	 */
	public void test010() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\"abc-def\\\"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"\"abc-def\"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using escaped escaped \ and escaped " in text block
	 */
	public void test011() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\"abc\\\"\"\"def\"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"\"abc\"\"\"def\"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using Unicode in text block
	 * and compare with an equal String literal
	 */
	public void test012() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\\u0ba4\\u0bae\\u0bbf\\u0bb4\"\"\";\n" +
						"	public static String str = \"\\u0ba4\\u0bae\\u0bbf\\u0bb4\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(str.equals(textb));\n" +
						"	}\n" +
						"}\n"
				},
				"true",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - bigger piece of code as text block
	 */
	public void test013() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"static String code = \"\"\"\n" + 
						"              public void print(Object o) {\n" + 
						"                  System.out.println(Objects.toString(o));\n" + 
						"              }\n" + 
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(code);\n" +
						"	}\n" +
						"}\n"
				},
				"public void print(Object o) {\n" + 
				"    System.out.println(Objects.toString(o));\n" + 
				"}",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - concatenation of string with text block
	 */
	public void test014() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String code = \"public void print(Object o) {\\n\" +\n" + 
						"              \"\"\"\n" + 
						"                  System.out.println(Objects.toString(o));\n" + 
						"              }\n" + 
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(code);\n" +
						"	}\n" +
						"}\n"
				},
				"public void print(Object o) {\n" + 
				"    System.out.println(Objects.toString(o));\n" + 
				"}",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - freely using quotes
	 */
	public void test015() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String story = \"\"\"\n" + 
						"    \"When I use a word,\" Humpty Dumpty said,\n" + 
						"    in rather a scornful tone, \"it means just what I\n" + 
						"    choose it to mean - neither more nor less.\"\n" + 
						"    \"The question is,\" said Alice, \"whether you\n" + 
						"    can make words mean so many different things.\"\n" + 
						"    \"The question is,\" said Humpty Dumpty,\n" + 
						"    \"which is to be master - that's all.\"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(story);\n" +
						"	}\n" +
						"}\n"
				},
				"\"When I use a word,\" Humpty Dumpty said,\n" + 
				"in rather a scornful tone, \"it means just what I\n" + 
				"choose it to mean - neither more nor less.\"\n" + 
				"\"The question is,\" said Alice, \"whether you\n" + 
				"can make words mean so many different things.\"\n" + 
				"\"The question is,\" said Humpty Dumpty,\n" + 
				"\"which is to be master - that's all.\"",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation
	 */
	public void test016() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" + 
						"              <html>\\r" + 
						"                  <body>\\r" + 
						"                      <p>Hello, world</p>\\r" + 
						"                  </body>\\r" + 
						"              </html>\\r" + 
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" + 
				"    <body>\n" + 
				"        <p>Hello, world</p>\n" + 
				"    </body>\n" + 
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation with empty lines
	 */
	public void test016a() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" + 
						"              <html>\\r\\n" + 
						"                  <body>\\r\\n" + 
						"                      <p>Hello, world</p>\\r\\n" + 
						"                  </body>\\r\\n" + 
						"              </html>\\r\\n" + 
						"              \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n\n" + 
				"    <body>\n\n" + 
				"        <p>Hello, world</p>\n\n" + 
				"    </body>\n\n" + 
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - html code with indentation and trailing whitespace
	 */
	public void test017() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" + 
						"              <html>  \\r" + 
						"                  <body>    \\r" + 
						"                      <p>Hello, world</p>      \\r" + 
						"                  </body>    \\r" + 
						"              </html>  \\r" + 
						"                   \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>\n" + 
				"    <body>\n" + 
				"        <p>Hello, world</p>\n" + 
				"    </body>\n" + 
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using octal escape char for trailing whitespace
	 */
	public void test018() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" + 
						"              <html>\\040\\040\\r" + 
						"                  <body>\\040\\040\\r" + 
						"                      <p>Hello, world</p>\\040\\040\\040\\r" + 
						"                  </body>\\040\\040\\r" + 
						"              </html>\\040\\040\\r" + 
						"                   \"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>  \n" + 
				"    <body>  \n" + 
				"        <p>Hello, world</p>   \n" + 
				"    </body>  \n" + 
				"</html>  ",
				null,
				new String[] {"--enable-preview"});
	}
}
