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

@SuppressWarnings("preview")
public class TextBlockTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "test018" };
	}
	
	public static Class<?> testClass() {
		return TextBlockTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public TextBlockTest(String testName){
		super(testName);
	}
	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean previewFlag) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, previewFlag ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
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
	public void test003a() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						"\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\"\n" + 
				"\n" + 
				"	                             ^^^^\n" + 
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
				"	                             ^^^^^^^^^\n" + 
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
						"  line 3\"\"\";\n" +
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
	 * Positive - Multi line text block with varying indentation
	 * and \n and \r
	 */
	public void test008a() {
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
						"		System.out.print(\"<\");\n" +
						"		System.out.print(textb);\n" +
						"		System.out.print(\">\");\n" +
						"	}\n" +
						"}\n"
				},
				"<    line 1\n" + 
				"    line 2\n" + 
				"  \n" + 
				"  line 3\n" +
				">", // the trailing whitespace is trimmed by the test framework
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
	 * output compared with String API
	 */
	@SuppressWarnings("removal")
	public void test016b() {
		String text = "<html>\n" + 
					"    <body>\n" + 
					"      <p>Hello, world</p>\n" + 
					"    </body>\n" + 
					"  </html>";
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static String html = \"\"\"\n" + 
						text + "\\n" +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(html);\n" +
						"	}\n" +
						"}\n"
				},
				text.stripIndent().translateEscapes(),
				null,
				new String[] {"--enable-preview"});
			
	}
	/*
	 * positive - html code with indentation with \r as terminator
	 */
	public void test016c() {
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
						"              </html>\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(html);\n" +
						"	}\n" +
						"}\n"
				},
				"<html>  \n" + 
				"    <body>  \n" + 
				"        <p>Hello, world</p>   \n" + 
				"    </body>  \n" + 
				"</html>",
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - using text block as a method argument
	 */
	public void test019() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(\"\"\"\n" + 
						"              <html>\\n" + 
						"                  <body>\\n" + 
						"                      <p>Hello, world</p>\\n" + 
						"                  </body>\\n" + 
						"              </html>\\n" + 
						"              \"\"\");\n" +
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
	 * positive - using variable assigned with text block as a method argument
	 */
	public void test020() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		String html = \"\"\"\n" + 
						"              <html>\n" + 
						"                  <body>\n" + 
						"                      <p>Hello, world</p>\n" + 
						"                  </body>\n" + 
						"              </html>\n" + 
						"                  \"\"\";\n" +	
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
	 * positive - assigning strings and text blocks interchangeably.
	 */
	public void test021() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		String html = \"\"\"\n" + 
						"              <html>\n" + 
						"                  <body>\n" + 
						"                      <p>Hello, world</p>\n" + 
						"                  </body>\n" + 
						"              </html>\n" + 
						"                  \"\"\";\n" +	
						"       String s = html;\n" +	
						"		System.out.println(s);\n" +
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
	 * positive - escaped '\', compare with String::translateEscapes
	 */
	@SuppressWarnings("removal")
	public void test022() {
		String text = "abc\\\\def\"";
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						text + 
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				text.translateEscapes(),
				null,
				new String[] {"--enable-preview"});
	}
	/*
	 * positive - escaped """, compare output with 
	 * 							String::translateEscapes
	 * 							String::stripIndent
	 */
	@SuppressWarnings("removal")
	public void test023() {
		String text = "abc\\\"\"\"def\"  ";
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" + 
						text + 
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(textb);\n" +
						"	}\n" +
						"}\n"
				},
				text.translateEscapes().stripIndent(),
				null,
				new String[] {"--enable-preview"});
	}
	public void test024() {
		runConformTest(
				new String[] {
						"Main.java",
						"@SuppressWarnings(\"preview\")\n" + 
						"public class Main {\n" + 
						"    public static void main(String[] args) {\n" + 
						"		runConformTest(\n" + 
						"				new String[] {\n" + 
						"						\"XYZ.java\",\n" + 
						"						\"\"\"\n" + 
						"								public class XYZ {\n" + 
						"									public static String textb = \\\"\"\"\n" + 
						"											abc\\\\\\\"\"\"def\"  \n" + 
						"												\\\"\"\";\n" + 
						"									public static void main(String[] args) {\n" + 
						"										System.out.println(textb);\n" + 
						"									}\n" + 
						"								}\"\"\"" + 
						"				}, \n" + 
						"				\"\",\n" + 
						"				null,\n" + 
						"				new String[] {\"--enable-preview\"});\n" + 
						"    }\n" + 
						"	private static void runConformTest(String[] strings, String text, Object object, String[] strings2) {\n" + 
						"		System.out.println(strings[1]);\n" + 
						"	}\n" + 
						"}"
				}, 
				"public class XYZ {\n" + 
				"	public static String textb = \"\"\"\n" + 
				"			abc\\\"\"\"def\"\n" + 
				"				\"\"\";\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(textb);\n" + 
				"	}\n" + 
				"}",
				null,
				new String[] {"--enable-preview"});
	}
	public void test025() {
		runNegativeTest(
				new String[] {
						"""
						X.java""",
						"""
						public class X {
							public static String textb = \"""
									abc\\def\""";
							public static void main(String[] args) {
								System.out.println(textb);
							}
						}
						"""
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\"\n" + 
				"			abc\\def\"\"\";\n" + 
				"	                             ^^^^^^^^^^^^\n" + 
				"Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ )\n" + 
				"----------\n",
				null,
				true,
				getCompilerOptions());
	}
	@SuppressWarnings("removal")
	public void test026() {
		String text = "abc\\\\def";
		runConformTest(
				new String[] {
						"""
						X.java""",
						"""
						public class X {
							public static String textb = \"""
									abc\\\\def\""";
							public static void main(String[] args) {
								System.out.println(textb);
							}
						}
						"""
				}, 
				text.translateEscapes(),
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void test027() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"  public static void main (String[] args) {\n" +
						"     String xyz = \n" + 
						"       \"\"\"\n" + 
						"         public class Switch {\n" + 
						"           public static void bar(int arg0) {\n" + 
						"             int arg1 = 0;\n" + 
						"             pointer: foo(\n" + 
						"               switch (0 + arg0) {\n" + 
						"                 case 1 -> 1;\n" + 
						"                 default -> {break p;}\\n\"\n" + 
						"               }\n" + 
						"             });\n" + 
						"             public static void foo(int arg0) {\n" + 
						"               bar(MyDay.SUNDAY);\n" + 
						"               }\n" + 
						"             }\\n\"\"\";  \n" + 
						"    System.out.println(xyz);\n" +
						"  }\n" +
						"}"
				}, 
				"public class Switch {\n" + 
				"           public static void bar(int arg0) {\n" + 
				"             int arg1 = 0;\n" + 
				"             pointer: foo(\n" + 
				"               switch (0 + arg0) {\n" + 
				"                 case 1 -> 1;\n" + 
				"                 default -> {break p;}\n" + 
				"\"\n" + 
				"               }\n" + 
				"             });\n" + 
				"             public static void foo(int arg0) {\n" + 
				"               bar(MyDay.SUNDAY);\n" + 
				"               }\n" + 
				"             }",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// An empty text block
	public void test028() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"  public static void main (String[] args) {\n" +
						"     String xyz = \n" + 
						"       \"\"\"\n" + 
						"         \\n\"\"\";  \n" + 
						"    System.out.println(xyz);\n" +
						"  }\n" +
						"}"
				}, 
				"",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	// An empty text block
	public void test029() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"	public static String str = \"\"\"\n" + 
						"			   Hello Guru	\n" + 
						"				\n" + 
						"			\"\"\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.println(str);\n" +
						"  }\n" +
						"}"
				}, 
				"Hello Guru", // output comparison tool strips off all trailing whitespace
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug550356() {
		Map<String, String> options = getCompilerOptions(false);
		runNegativeTest(
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
				"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	public static String textb = \"\"\"\n" + 
				"\"\"\";\n" + 
				"	                             ^^^^^^^\n" + 
				"Text Blocks is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n",
				null,
				true,
				options);
	}
	public void testBug551948_1() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String text = \"\"\"\n" + 
						"            Lorem ipsum dolor sit amet, consectetur adipiscing \\\n" + 
						"            elit, sed do eiusmod tempor incididunt ut labore \\\n" + 
						"            et dolore magna aliqua.\\\n" + 
						"            \"\"\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(text);\n" +
						"  }\n" +
						"}"
				}, 
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", // output comparison tool strips off all trailing whitespace
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_2() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String noLastLF = \"\"\"\n" + 
						"    abc\n" + 
						"        def\\\n" + 
						"    ghi\"\"\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(noLastLF);\n" +
						"  }\n" +
						"}"
				}, 
				"abc\n    defghi",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_3() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String python = \"\"\"\n" + 
						"    if x == True and \\\\\n" + 
						"        y == False\n" + 
						"    \"\"\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(python);\n" +
						"  }\n" +
						"}"
				}, 
				"if x == True and \\\n" + 
				"    y == False",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_4() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String colors = \"\"\"\n" + 
						"    red   \\\n" + 
						"    green \\\n" + 
						"    blue  \\\n" + 
						"    orange\"\"\"; \n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(colors);\n" +
						"  }\n" +
						"}"
				}, 
				"red   green blue  orange",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_5() {
		runNegativeTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String colors = \"\"\"\n" + 
						"    \\red   \n" + 
						"    \\green \n" + 
						"    \\blue  \n" + 
						"    \\orange\"\"\"; \n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(colors);\n" +
						"  }\n" +
						"}"
				},
				"----------\n" + 
				"1. ERROR in Cls2.java (at line 2)\n" + 
				"	static String colors = \"\"\"\n" + 
				"    \\red   \n" + 
				"    \\green \n" + 
				"	                       ^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ )\n" + 
				"----------\n",
				null,
				true,
				getCompilerOptions(true));
	}
	public void testBug551948_6() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String str = \"A\\sline\\swith\\sspaces\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(str);\n" +
						"  }\n" +
						"}"
				}, 
				"A line with spaces",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_7() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String colors = \"\"\"\n" + 
						"    red  \\s\n" + 
						"    green\\s\n" + 
						"    blue \\s\n" + 
						"    \"\"\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(colors);\n" +
						"  }\n" +
						"}"
				}, 
				"red   \ngreen \nblue", // trailing whitespaces are trimmed
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug551948_8() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"public class Cls2 {\n" + 
						"static String s = \"\"\"\n" + 
						"aaa\n" + 
						"\n" + 
						"bbb\n" + 
						"\n" + 
						"\n" + 
						"ccc" + 
						"\"\"\";\n" + 
						"  public static void main (String[] args) {\n" +
						"    System.out.print(s);\n" +
						"  }\n" +
						"}"
				}, 
				"aaa\n\n" + 
				"bbb\n\n\n" + 
				"ccc", // trailing whitespaces are trimmed
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
}
