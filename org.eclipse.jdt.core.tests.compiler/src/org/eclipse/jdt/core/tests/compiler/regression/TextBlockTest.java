/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class TextBlockTest extends AbstractRegressionTest {

	public static Class<?> testClass() {
		return TextBlockTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public TextBlockTest(String testName){
		super(testName);
	}
	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}

	static {
	//	TESTS_NAMES = new String [] { "testBug565639_2" };
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean previewFlag) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions, String[] vmArguments) {
		runConformTest(testFiles, expectedOutput, customOptions, vmArguments, new JavacTestOptions("-source 15 "));
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(true, testFiles, null, expectedOutput, null, new JavacTestOptions("-source 15"));
	}
	protected void runConformTest(
			// test directory preparation
			boolean shouldFlushOutputDirectory,
			String[] testFiles,
			//compiler options
			String[] classLibraries /* class libraries */,
			Map<String, String> customOptions /* custom options */,
			// compiler results
			String expectedCompilerLog,
			// runtime results
			String expectedOutputString,
			String expectedErrorString,
			String[] vmarguments,
			// javac options
			JavacTestOptions javacTestOptions) {
		runTest(
			// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			expectedCompilerLog /* expected compiler log */,
			// runtime options
			false /* do not force execution */,
			vmarguments /* no vm arguments */,
			// runtime results
			expectedOutputString /* expected output string */,
			expectedErrorString /* expected error string */,
			// javac options
			javacTestOptions /* javac test options */);
	}
	public void test001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \"""\""";
						                               ^^
					Syntax error on token \"""\", invalid AssignmentOperator
					----------
					""");
	}
	public void test002() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \""" \""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \""" \""";
						                               ^^^
					Syntax error on token "" "", invalid AssignmentOperator
					----------
					""");
	}
	public void test003() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \"""
					";
						                             ^^^^^
					Text block is not properly closed with the delimiter
					----------
					""");
	}
	public void test003a() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""

								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \"""

						                             ^^^^
					Text block is not properly closed with the delimiter
					----------
					""");
	}
	/*
	 * negative - unescaped '\' in a text block
	 */
	public void test004() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							abc\\def\
							\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \"""
					abc\\def\""";
						                             ^^^^^^^^^
					Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\"  \\\'  \\ )
					----------
					""");
	}
	/* empty text block */
	public void test005() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"",
				null);
	}
	/*
	 * positive - escaped '\'
	 */
	public void test006() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							abc\\\\def\
							\""";
								public static void main(String[] args) {
									System.out.print(textb);
								}
							}
							"""
				},
				"abc\\def",
				null);
	}
	/*
	 * positive - escaped '\'
	 */
	public void test006a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							\\u007Babc\\\\def\
							\""";
								public static void main(String[] args) {
									System.out.print(textb);
								}
							}
							"""
				},
				"{abc\\def",
				null);
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n
	 */
	public void test007() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							    line 1
							    line 2
							 \s
							  line 3\""";
								public static void main(String[] args) {
									System.out.print(textb);
								}
							}
							"""
				},
				"""
					line 1
					  line 2

					line 3""",
				null);
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n and \r
	 */
	public void test008() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							    line 1
							    line 2\r\
							  \r\
							  line 3\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					line 1
					  line 2

					line 3""", // the trailing whitespace is trimmed by the test framework
				null);
	}
	/*
	 * Positive - Multi line text block with varying indentation
	 * and \n and \r
	 */
	public void test008a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							    line 1
							    line 2\r\
							  \r\
							  line 3
							\""";
								public static void main(String[] args) {
									System.out.print("<");
									System.out.print(textb);
									System.out.print(">");
								}
							}
							"""
				},
				"""
					<    line 1
					    line 2

					  line 3
					>""", // the trailing whitespace is trimmed by the test framework
				null);
	}
	/*
	 * positive - using unescaped '"' in text block
	 */
	public void test009() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							"abc-def\
							\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"\"abc-def",
				null);
	}
	/*
	 * positive - using escaped '"' in text block
	 */
	public void test010() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							"abc-def\\\"""\";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"\"abc-def\"",
				null);
	}
	/*
	 * positive - using escaped \ and escaped " in text block
	 */
	public void test011() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							"abc\\\"""def\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"\"abc\"\"\"def",
				null);
	}
	/*
	 * positive - using Unicode in text block
	 * and compare with an equal String literal
	 */
	public void test012() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							\\u0ba4\\u0bae\\u0bbf\\u0bb4\""";
								public static String str = "\\u0ba4\\u0bae\\u0bbf\\u0bb4";
								public static void main(String[] args) {
									System.out.println(str.equals(textb));
								}
							}
							"""
				},
				"true",
				null);
	}
	/*
	 * positive - bigger piece of code as text block
	 */
	public void test013() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							static String code = \"""
							              public void print(Object o) {
							                  System.out.println(Objects.toString(o));
							              }
							              \""";
								public static void main(String[] args) {
									System.out.print(code);
								}
							}
							"""
				},
				"""
					public void print(Object o) {
					    System.out.println(Objects.toString(o));
					}""",
				null);
	}
	/*
	 * positive - concatenation of string with text block
	 */
	public void test014() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String code = "public void print(Object o) {\\n" +
							              \"""
							                  System.out.println(Objects.toString(o));
							              }
							              \""";
								public static void main(String[] args) {
									System.out.print(code);
								}
							}
							"""
				},
				"""
					public void print(Object o) {
					    System.out.println(Objects.toString(o));
					}""",
				null);
	}
	/*
	 * positive - freely using quotes
	 */
	public void test015() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String story = \"""
							    "When I use a word," Humpty Dumpty said,
							    in rather a scornful tone, "it means just what I
							    choose it to mean - neither more nor less."
							    "The question is," said Alice, "whether you
							    can make words mean so many different things."
							    "The question is," said Humpty Dumpty,
							    "which is to be master - that's all.\""";
								public static void main(String[] args) {
									System.out.print(story);
								}
							}
							"""
				},
				"""
					"When I use a word," Humpty Dumpty said,
					in rather a scornful tone, "it means just what I
					choose it to mean - neither more nor less."
					"The question is," said Alice, "whether you
					can make words mean so many different things."
					"The question is," said Humpty Dumpty,
					"which is to be master - that's all.""",
				null);
	}
	/*
	 * positive - html code with indentation
	 */
	public void test016() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String html = \"""
							              <html>
							                  <body>
							                      <p>Hello, world</p>
							                  </body>
							              </html>\""";
								public static void main(String[] args) {
									System.out.print(html);
								}
							}
							"""
				},
				"""
					<html>
					    <body>
					        <p>Hello, world</p>
					    </body>
					</html>""",
				null);
	}
	/*
	 * positive - html code with indentation with empty lines
	 */
	public void test016a() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String html = \"""
							              <html>\\r\\n\
							                  <body>\\r\\n\
							                      <p>Hello, world</p>\\r\\n\
							                  </body>\\r\\n\
							              </html>\\r\\n\
							              \""";
								public static void main(String[] args) {
									System.out.println(html);
								}
							}
							"""
				},
				"""
					<html>
					                  <body>
					                      <p>Hello, world</p>
					                  </body>
					              </html>""",
				null);
	}
	/*
	 * positive - html code with indentation with \r as terminator
	 */
	public void test016c() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String html = \"""
							              <html>
							                  <body>
							                      <p>Hello, world</p>
							                  </body>
							              </html>
							              \""";
								public static void main(String[] args) {
									System.out.println(html);
								}
							}
							"""
				},
				"""
					<html>
					    <body>
					        <p>Hello, world</p>
					    </body>
					</html>""",
				null);
	}
	/*
	 * positive - html code with indentation and trailing whitespace
	 */
	public void test017() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String html = \"""
							              <html>\s\s
							                  <body>\s\s\s\s
							                      <p>Hello, world</p>\s\s\s\s\s\s
							                  </body>\s\s\s\s
							              </html>\s\s
							                   \""";
								public static void main(String[] args) {
									System.out.println(html);
								}
							}
							"""
				},
				"""
					<html>
					    <body>
					        <p>Hello, world</p>
					    </body>
					</html>""",
				null);
	}
	/*
	 * positive - using octal escape char for trailing whitespace
	 */
	public void test018() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static String html = \"""
							              <html>\\040\\040
							                  <body>\\040\\040
							                      <p>Hello, world</p>\\040\\040\\040
							                  </body>\\040\\040
							              </html>\""";
								public static void main(String[] args) {
									System.out.print(html);
								}
							}
							"""
				},
				"""
					<html>\s\s
					    <body>\s\s
					        <p>Hello, world</p>\s\s\s
					    </body>\s\s
					</html>""",
				null);
	}
	/*
	 * positive - using text block as a method argument
	 */
	public void test019() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void main(String[] args) {
									System.out.println(\"""
							              <html>\\n\
							                  <body>\\n\
							                      <p>Hello, world</p>\\n\
							                  </body>\\n\
							              </html>\\n\
							              \""");
								}
							}
							"""
				},
				"""
					<html>
					                  <body>
					                      <p>Hello, world</p>
					                  </body>
					              </html>""",
				null);
	}
	/*
	 * positive - using variable assigned with text block as a method argument
	 */
	public void test020() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void main(String[] args) {
									String html = \"""
							              <html>
							                  <body>
							                      <p>Hello, world</p>
							                  </body>
							              </html>
							                  \""";
									System.out.println(html);
								}
							}
							"""
				},
				"""
					<html>
					    <body>
					        <p>Hello, world</p>
					    </body>
					</html>""",
				null);
	}
	/*
	 * positive - assigning strings and text blocks interchangeably.
	 */
	public void test021() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void main(String[] args) {
									String html = \"""
							              <html>
							                  <body>
							                      <p>Hello, world</p>
							                  </body>
							              </html>
							                  \""";
							       String s = html;
									System.out.println(s);
								}
							}
							"""
				},
				"""
					<html>
					    <body>
					        <p>Hello, world</p>
					    </body>
					</html>""",
				null);
	}
	public void test024() {
		runConformTest(
				true,
				new String[] {
						"Main.java",
						"""
							@SuppressWarnings("preview")
							public class Main {
							    public static void main(String[] args) {
									runConformTest(
											new String[] {
													"XYZ.java",
													\"""
															public class XYZ {
																public static String textb = \\\"""
																		abc\\\\\\\"""def"\s\s
																			\\\""";
																public static void main(String[] args) {
																	System.out.println(textb);
																}
															}\"""\
											},\s
											"",
											null,
											new String[] {"--enable-preview"});
							    }
								private static void runConformTest(String[] strings, String text, Object object, String[] strings2) {
									System.out.println(strings[1]);
								}
							}"""
				},
				null,
				"""
					public class XYZ {
						public static String textb = \"""
								abc\\\"""def"
									\""";
						public static void main(String[] args) {
							System.out.println(textb);
						}
					}""",
				null,
				JavacTestOptions.DEFAULT);
	}
	public void test025() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
									public static String textb = \"""
										abc\\def\""";
									public static void main(String[] args) {
										System.out.println(textb);
									}
								}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \"""
								abc\\def\""";
						                             ^^^^^^^^^^^^
					Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\"  \\\'  \\\\ )
					----------
					""",
				null,
				true,
				getCompilerOptions());
	}

	public void test027() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main (String[] args) {
							     String xyz =\s
							       \"""
							         public class Switch {
							           public static void bar(int arg0) {
							             int arg1 = 0;
							             pointer: foo(
							               switch (0 + arg0) {
							                 case 1 -> 1;
							                 default -> {break p;}\\n"
							               }
							             });
							             public static void foo(int arg0) {
							               bar(MyDay.SUNDAY);
							               }
							             }\\n\""";\s\s
							    System.out.println(xyz);
							  }
							}"""
				},
				"""
					public class Switch {
					  public static void bar(int arg0) {
					    int arg1 = 0;
					    pointer: foo(
					      switch (0 + arg0) {
					        case 1 -> 1;
					        default -> {break p;}
					"
					      }
					    });
					    public static void foo(int arg0) {
					      bar(MyDay.SUNDAY);
					      }
					    }""",
				getCompilerOptions());
	}
	// An empty text block
	public void test028() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							  public static void main (String[] args) {
							     String xyz =\s
							       \"""
							         \\n\""";\s\s
							    System.out.println(xyz);
							  }
							}"""
				},
				"",
				getCompilerOptions());
	}
	// An empty text block
	public void test029() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
								public static String str = \"""
										   Hello Guru\t
								\t\t\t
										\""";
							  public static void main (String[] args) {
							    System.out.println(str);
							  }
							}"""
				},
				"Hello Guru", // output comparison tool strips off all trailing whitespace
				getCompilerOptions());
	}
	public void testBug551948_1() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String text = \"""
							            Lorem ipsum dolor sit amet, consectetur adipiscing \\
							            elit, sed do eiusmod tempor incididunt ut labore \\
							            et dolore magna aliqua.\\
							            \""";
							  public static void main (String[] args) {
							    System.out.print(text);
							  }
							}"""
				},
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", // output comparison tool strips off all trailing whitespace
				getCompilerOptions());
	}
	public void testBug551948_2() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String noLastLF = \"""
							    abc
							        def\\
							    ghi\""";
							  public static void main (String[] args) {
							    System.out.print(noLastLF);
							  }
							}"""
				},
				"abc\n    defghi",
				getCompilerOptions());
	}
	public void testBug551948_3() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String python = \"""
							    if x == True and \\\\
							        y == False
							    \""";
							  public static void main (String[] args) {
							    System.out.print(python);
							  }
							}"""
				},
				"if x == True and \\\n" +
				"    y == False",
				getCompilerOptions());
	}
	public void testBug551948_4() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String colors = \"""
							    red   \\
							    green \\
							    blue  \\
							    orange\""";\s
							  public static void main (String[] args) {
							    System.out.print(colors);
							  }
							}"""
				},
				"red   green blue  orange",
				getCompilerOptions());
	}
	public void testBug551948_5() {
		runNegativeTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String colors = \"""
							    \\red\s\s\s
							    \\green\s
							    \\blue\s\s
							    \\orange\""";\s
							  public static void main (String[] args) {
							    System.out.print(colors);
							  }
							}"""
				},
				"""
					----------
					1. ERROR in Cls2.java (at line 3)
						static String colors = \"""
					    \\red\s\s\s
					    \\green\s
						                       ^^^^^^^^^^^^^^^^^^^^^^
					Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\"  \\\'  \\\\ )
					----------
					""",
				null,
				true,
				getCompilerOptions(true));
	}
	public void testBug551948_6() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String str = "A\\sline\\swith\\sspaces";
							  public static void main (String[] args) {
							    System.out.print(str);
							  }
							}"""
				},
				"A line with spaces",
				getCompilerOptions());
	}
	public void testBug551948_7() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String colors = \"""
							    red  \\s
							    green\\s
							    blue \\s
							    \""";
							  public static void main (String[] args) {
							    System.out.print(colors);
							  }
							}"""
				},
				"red   \ngreen \nblue", // trailing whitespaces are trimmed
				getCompilerOptions());
	}
	public void testBug551948_8() {
		runConformTest(
				new String[] {
						"Cls2.java",
						"""
							@SuppressWarnings("preview")
							public class Cls2 {
							static String s = \"""
							aaa

							bbb


							ccc\
							\""";
							  public static void main (String[] args) {
							    System.out.print(s);
							  }
							}"""
				},
				"""
					aaa

					bbb


					ccc""",
				getCompilerOptions());
	}
	public void testCompliances_1() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	String textBlock = \"""

							    			aa\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	char LF  = (char) 0x000A;
							        String str = "" + LF + "aa";
							        return textBlock.equals(str);
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_2() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	String textBlock = \"""
							\\n\
							\\n\
							\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	 char LF  = (char) 0x000A;
							        String str = "" + LF + LF + "";
							        return textBlock.equals(str);
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_3() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							
							\""";
							    	 System.out.print(textBlock);
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	 char LF  = (char) 0x000A;
							        String str = "" + '\\u0015' + LF + "";
							        return textBlock.equals(str.stripIndent());
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_4() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							v\r\
							\r\
							vaa\""";
								char[] cs = textBlock.toCharArray();
							    for (char c : cs) {
									//System.out.print((int)c);
									//System.out.print(',');
								}
							    //System.out.println();
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	 char LF  = (char) 0x000A;
							        String str = "v" + LF + LF + '\\u0076' + "aa";
							        return textBlock.equals(str.stripIndent());
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_5() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							aa\f\
							\f\
							\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	 char LF  = (char) 0x000A;
							        String str = "aa" + LF + LF + "";
							        return textBlock.equals(str);
							    }
							}"""
				},
				"false",
				getCompilerOptions());
	}
	public void testCompliances_6() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							
							\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	 char LF  = (char) 0x000A;
							        String str = "" + '\\u0015' + LF + "";
							        return textBlock.equals(str);
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_7() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							aav
							\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	 char LF  = (char) 0x000A;
							        String str = "aa" + '\\u0076' + LF + "";
							        return textBlock.equals(str.stripIndent());
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_8() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							\\"some\\"\\n \\"string\\" \\n \\"here\\"\\n\""";
							    	 System.out.print(textBlock.length());
							    }
							}"""
				},
				"26",
				getCompilerOptions());
	}
	// Escaped """ with escaping at the first '"'
	public void testCompliances_9() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							some string ends with \\\"""\\n\""";
							    	 System.out.print(textBlock.length());
							    }
							}"""
				},
				"26",
				getCompilerOptions());
	}
	// Escaped """ with escaping at the second '"'
	public void testCompliances_10() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							some string ends with "\\""\\n\""";
							    	 System.out.print(textBlock.length());
							    }
							}"""
				},
				"26",
				getCompilerOptions());
	}
	// Escaped """ with escaping at the third '"'
	public void testCompliances_11() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	 String textBlock = \"""
							some string ends with ""\\"\\n\""";
							    	 System.out.print(textBlock.length());
							    }
							}"""
				},
				"26",
				getCompilerOptions());
	}
	public void testCompliances_12() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	String textBlock = \"""
							\r
							    			aa\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							    	char LF  = (char) 0x000A;
							        String str = "" + LF + "aa";
							        return textBlock.equals(str);
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_13() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	String textb = \"""
							\\0\\1\\2\\3\\4\\5\\6\\7\\10\\11\\12\\13\\14\\15\\16\\17\\20\\21\\22\\23\\24\\25\\26\\27\\30\\31\\32\\33\\34\\35\\36\\37\\40\\41\\42\\43\\44\\45\\46\\47\\50\\51\\52\\53\\54\\55\\56\\57\\60\\61\\62\\63\\64\\65\\66\\67\\70\\71\\72\\73\\74\\75\\76\\77\\100\\101\\102\\103\\104\\105\\106\\107\\110\\111\\112\\113\\114\\115\\116\\117\\120\\121\\122\\123\\124\\125\\126\\127\\130\\131\\132\\133\\134\\135\\136\\137\\140\\141\\142\\143\\144\\145\\146\\147\\150\\151\\152\\153\\154\\155\\156\\157\\160\\161\\162\\163\\164\\165\\166\\167\\170\\171\\172\\173\\174\\175\\176\\177\\200\\201\\202\\203\\204\\205\\206\\207\\210\\211\\212\\213\\214\\215\\216\\217\\220\\221\\222\\223\\224\\225\\226\\227\\230\\231\\232\\233\\234\\235\\236\\237\\240\\241\\242\\243\\244\\245\\246\\247\\250\\251\\252\\253\\254\\255\\256\\257\\260\\261\\262\\263\\264\\265\\266\\267\\270\\271\\272\\273\\274\\275\\276\\277\\300\\301\\302\\303\\304\\305\\306\\307\\310\\311\\312\\313\\314\\315\\316\\317\\320\\321\\322\\323\\324\\325\\326\\327\\330\\331\\332\\333\\334\\335\\336\\337\\340\\341\\342\\343\\344\\345\\346\\347\\350\\351\\352\\353\\354\\355\\356\\357\\360\\361\\362\\363\\364\\365\\366\\367\\370\\371\\372\\373\\374\\375\\376\\377\""";
									System.out.println(textb.length());
									for (int i=0; i<=0xFF; i++) {
							            if (i != (int)textb.charAt(i)) {
							                System.out.println("Error in octal escape :" + i);
							            }
							        }
							    }
							}"""
				},
				"256",
				getCompilerOptions());
	}
	public void testCompliances_14() {
		runConformTest(
				new String[] {
						"C.java",
						"""
							@SuppressWarnings("preview")
							public class C {
							    public static void main(String argv[]) {
							    	String textBlock = \"""\r
							          This is a multi-line
							          message that is super-
							          exciting!\""";
							    	 System.out.print(compare(textBlock));
							    }
							    private static boolean compare(String textBlock) {
							        String str = "This is a multi-line\\n" +\s
											"message that is super-\\n" +\s
											"exciting!";
							        return textBlock.equals(str);
							    }
							}"""
				},
				"true",
				getCompilerOptions());
	}
	public void testBug553252() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		Map<String, String> copy = new HashMap<>(defaultOptions);
		copy.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
		copy.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		copy.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
		copy.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							\""";
								public static void main(String[] args) {
									System.out.println(textb);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public static String textb = \"""
					\""";
						                             ^^^^^^^
					The Java feature \'Text Blocks\' is only available with source level 15 and above
					----------
					""",
				null,
				true,
				new String[] {"-source 14 "},
				copy);
	}
	public void testBug562460() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							a\\sb\\sc\""";
								public static void main(String[] args) {
									System.out.println(textb.equals("a b c"));
								}
							}
							"""
				},
				"true",
				getCompilerOptions());
	}
	public void testCompliances_15() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
								\\baa\""";
								public static void main(String[] args) {
									print(textb.toCharArray());
								}
							   private static void print(char[] val) {
							        for (char c : val) {
							            System.out.print((int)c + ",");
							        }
							    }
							}
							"""
				},
				"8,97,97,",
				getCompilerOptions());
	}
	public void testCompliances_16() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
								\\baa\""";
								public static void main(String[] args) {
									print(textb.toCharArray());
								}
							   private static void print(char[] val) {
							        for (char c : val) {
							            System.out.print((int)c + ",");
							        }
							    }
							}
							"""
				},
				"8,97,97,",
				getCompilerOptions());
	}
	public void testCompliances_17() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							\\t\\baa\""";
								public static void main(String[] args) {
									print(textb.toCharArray());
								}
							   private static void print(char[] val) {
							        for (char c : val) {
							            System.out.print((int)c + ",");
							        }
							    }
							}
							"""
				},
				"9,8,97,97,",
				getCompilerOptions());
	}
	public void testCompliances_18() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static String textb = \"""
							\\013\\baa\""";
								public static void main(String[] args) {
									print(textb.toCharArray());
								}
							   private static void print(char[] val) {
							        for (char c : val) {
							            System.out.print((int)c + ",");
							        }
							    }
							}
							"""
				},
				"11,8,97,97,",
				getCompilerOptions());
	}
	public void testBug565639_1() {
		runConformTest(true,
					new String[]{
						"X.java",
						"""
							public class X {
							    static final String TEXT_BLOCK = \"""
							              1
							              2
							              3
							              4
							              5
							            \""";
							    public static void main(String[] args)  {
							        throw new RuntimeException("This is line 10.");
							    }
							}
							"""
				},
				null,
				getCompilerOptions(),
				"",
				"",
				"Exception in thread \"main\" java.lang.RuntimeException: This is line 10.\n" +
						"	at X.main(X.java:10)",
				new String[] {"--enable-preview"},
				new JavacTestOptions("-source 14 --enable-preview"));
	}
	public void testBug565639_2() {
		runConformTest(true,
				new String[]{
					"X.java",
					"""
						public class X {
						    public static void main(String[] args)  {
						    	String TEXT_BLOCK = \"""
						              1
						              2
						              3
						              4
						              5
						            \""";
						        throw new RuntimeException("This is line 10.");
						    }
						}
						"""
			},
			null,
			getCompilerOptions(),
			"",
			"",
			"Exception in thread \"main\" java.lang.RuntimeException: This is line 10.\n" +
					"	at X.main(X.java:10)",
			new String[] {"--enable-preview"},
			new JavacTestOptions("-source 14 --enable-preview"));
	}
	public void testBug565639_3() {
		runNegativeTest(new String[]{
					"X.java",
					"""
						public class X {
						    public static void main(String[] args)  {
						    	String TEXT_BLOCK = \"""
						              1
						              2
						              3
						              4
						              5
						            \"""\";
						        throw new RuntimeException("This is line 10.");
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						\"""\";
						   ^^
					String literal is not properly closed by a double-quote
					----------
					""");
	}
	public void testBug565639_4() {
		runNegativeTest(new String[]{
					"X.java",
					"""
						public class X {
						    public static void main(String[] args)  {
						    	String TEXT_BLOCK = \"""
						              1
						              2
						              3
						              4
						              5
						            \"""\"";
						        throw new RuntimeException("This is line 10.");
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						\"""\"";
						   ^^
					Syntax error on token \"""\", delete this token
					----------
					""");
	}
	public void testBug565639_5() {
		runNegativeTest(new String[]{
					"X.java",
					"""
						public class X {
						    public static void main(String[] args)  {
						    	String TEXT_BLOCK = \"""
						              1
						              2
						              3
						              4
						              5
						            \\\"""\""";
						        throw new RuntimeException("This is line 10.");
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						\\\"""\""";
						     ^^
					Syntax error on token \"""\", delete this token
					----------
					""");
	}
	public void testBug565639_6() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String[] args)  {
							    	String TEXT_BLOCK = \"""
							              1
							              2
							              3
							              4
							              \\\"""
							              \""";
							        System.out.println(TEXT_BLOCK);
							    }
							}
							"""
				},
				"1\n" +
				"2\n" +
				"3\n" +
				"4\n" +
				"\"\"\"",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug575953() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String[] args)  {
							    	String TEXT_BLOCK = \"""
							           public class A {
							               public void foo() {\\s
							                   String k = \\\"""
							                       abcdefg
							                       \\\"""
							                   System.out.pri\\
							           ntln("abc");\\s
							               }
							           }\\
							           \""";
							        System.out.println(TEXT_BLOCK);
							    }
							}
							"""
				},
				"""
					public class A {
					    public void foo() {\s
					        String k = \"""
					            abcdefg
					            \"""
					        System.out.println("abc");\s
					    }
					}""",
				getCompilerOptions(),
				new String[] {"--enable-preview"});
	}
	public void testBug578649_1() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String[] args) {
							        System.out.println(\"""
							        <record>
							          <value field=\\"NAME2\\">b\\tc</value>
							          <value field=\\"NAME1\\">a\\tb</value>
							        </record>
							        \""");
							    }
							}
							"""
				},
				"""
					<record>
					  <value field="NAME2">b	c</value>
					  <value field="NAME1">a	b</value>
					</record>""",
				getCompilerOptions());
	}
	public void testBug578649_2() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String[] args) {
							        System.out.println(\"""
							        123\\b45
							        \""");
							    }
							}
							"""
				},
				"123\b45",
				getCompilerOptions());
	}


	/*
	 * positive - html code with indentation with empty lines
	 * output compared with String API
	 */
	public void test016b() {
		String text = """
			<html>
			    <body>
			      <p>Hello, world</p>
			    </body>
			  </html>""";
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
	 * positive - escaped '\', compare with String::translateEscapes
	 */
	public void test022() {
		String text = "abc\\\\def";
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public static String textb = \"\"\"\n" +
						text +
						"\"\"\";\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.print(textb);\n" +
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
	public void testIssue544_1() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String argv[]) {
							      String outer = \"""
							                String inner = \\\"""
							                       \\\""";\""";
							      System.out.println(outer.length());
							    }
							}
							"""
				},
				"30",
				getCompilerOptions());
	}
	public void testIssue544_2() {
		runConformTest(
			new String[] {
					"X.java",
					"""
					public class X {
					  public static void main(String argv[]) {
					  String outer = \"""
String text = \\\"""
          String text = \\\"""
                  String text = \\\"""
                          A text block inside a text block at level 3
                      \\\""";
              \\\""";
      \\\""";\""";
  System.out.println(outer.equals(
              "String text = \\"\\"\\"\\n" +
              "          String text = \\"\\"\\"\\n" +
              "                  String text = \\"\\"\\"\\n" +
              "                          A text block inside a text block at level 3\\n" +
              "                      \\"\\"\\";\\n" +
              "              \\"\\"\\";\\n" +
              "      \\"\\"\\";"
              ));
					}
					}"""
			},
			"true",
			getCompilerOptions());
	}
	public void testIssue544_3() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    public static void main(String argv[]) {
        String s = \"""
\
\""";
        System.out.println(compare(s));
    }
    private static boolean compare(String s) {
        return s.equals(\"\");
    }
}
						"""
				},
				"true",
				getCompilerOptions());
	}
	public void testIssue544_4() {
		runConformTest(
				new String[] {
						"X.java",
						"""
public class X {
    public static void main(String argv[]) {
        String s = \"""
\
some \
newline
string\
.\""";
        System.out.println(compare(s));
    }
    private static boolean compare(String s) {
        return s.equals(\"""
some newline
string.\""");
    }
}
						"""
				},
				"true",
				getCompilerOptions());
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4129
	// Text block with unicode escape before multiple backslashes has wrong value
	public void testIssue4129() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    public static void main(String[] args)
						    {
						        System.out.println(\"\"\"
						            A \\\\\\\"-\\\\\\\" B\"\"\");
						        System.out.println(\"\"\"
						            \\u0041 \\\\\\"-\\\\\\" B\"\"\");
						    }
						}
						"""
				},
				"A \\\"-\\\" B\n" +
				"A \\\"-\\\" B");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4153
	// [Text blocks] Delimiters in unicode not handled properly
	public void testIssue4153() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	String text = \\u0022\"\\u0022\n" +
						"        Hello\"\"\";\n" +
						"       \n" +
						"        System.out.println(text);\n" +
						"    }\n" +
						"}\n"
				},
				"Hello");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=561978
	// Text block with the \<line-terminator> distorts line numbers for Unix-style line delimiters
	public void testBug561978() {
		runConformTest(true,
				new String[] {
						"X.java",
						"""
						/*  1 */ public class X {
						/*  2 */
						/*  3 */     public static void main(String[] args) {
						/*  4 */
						/*  5 */         String text = \"\"\"
						                     Lorem ipsum dolor sit amet, consectetur adipiscing \\
						                     elit, sed do eiusmod tempor incididunt ut labore \\
						                     et dolore magna aliqua.\\
						                     \"\"\";
						/* 10 */
						/* 11 */         	throw new RuntimeException(\"This is line 11.\");
						/* 12 */    }
						/* 13 */ }
						"""
				},
				null,
				getCompilerOptions(),
				"",
				"",
				"Exception in thread \"main\" java.lang.RuntimeException: This is line 11.\n" +
						"	at X.main(X.java:11)",
						new String[] {"--enable-preview"},
						new JavacTestOptions("-source 14 --enable-preview"));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=570719
	// Text blocks: '\<line-terminator>' after '\s' compiled to "\\\n" instead of to ""
	public void testBug570719() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
							public static void main(String[] args) {

								System.out.println(\"\"\"
										text \\s\\
										block
										\"\"\");

								System.out.println(\"\"\"
										text \\s
										block\\
										!!!
										\"\"\");

								System.out.println(\"\"\"
										text \\
										block
										\"\"\");

								System.out.println(\"\"\"
										text \\
										block \\s
										!!!
										\"\"\");
								System.out.println("Done");
							}
						}

						"""
				},
				"text  block\n" +
				"\n" +
				"text  \n" +
				"block!!!\n" +
				"\n" +
				"text block\n" +
				"\n" +
				"text block  \n" +
				"!!!\n" +
				"\n" +
				"Done");
	}
}
