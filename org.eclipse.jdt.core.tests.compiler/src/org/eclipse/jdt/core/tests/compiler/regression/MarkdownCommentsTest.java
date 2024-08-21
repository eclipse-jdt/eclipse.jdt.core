/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
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

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MarkdownCommentsTest extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String processAnnotations = null;
	String reportJavadocDeprecation = null;

	public MarkdownCommentsTest(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] {"test018"};
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(MarkdownCommentsTest.class, F_23);
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
		}
		if (this.reportJavadocDeprecation != null) {
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportJavadocDeprecation);
		}
		if (this.reportMissingJavadocComments != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
			if (this.reportMissingJavadocCommentsVisibility != null) {
				options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility,
						this.reportMissingJavadocCommentsVisibility);
			}
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
		}
		if (this.reportMissingJavadocTags != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		}
		if (this.reportMissingJavadocDescription != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription,
					this.reportMissingJavadocDescription);
		}
		if (this.processAnnotations != null) {
			options.put(CompilerOptions.OPTION_Process_Annotations, this.processAnnotations);
		}
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, this.reportDeprecation);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		return options;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
		this.reportDeprecation = CompilerOptions.ERROR;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
	}

	public void test001() {
		this.runNegativeTest(new String[] { "X.java", """
				public class X {
					///
					/// @param parameters array of String
					///
					public int sample(String[] params) {
						return 42;
					}
				}
				""", },

				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	/// @param parameters array of String\n" +
				"	           ^^^^^^^^^^\n" +
				"Javadoc: Parameter parameters is not declared\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test002() {
		this.runNegativeTest(new String[] { "X.java", """
				public class X {
					///
					/// @param params
					///
					public void sample(String[] params) {
						return 42;
					}
				}
				""", },

				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	/// @param params\n" +
				"	           ^^^^^^\n" +
				"Javadoc: Description expected after this reference\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	return 42;\n" +
				"	^^^^^^^^^^\n" +
				"Void methods cannot return a value\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test003() {
		this.runNegativeTest(new String[] { "X.java", """
				public class X {
					///
					/// @param params array of String
					///
					public void sample(String[] params) {
						return 42;
					}
				}
				""", },

				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	return 42;\n" +
				"	^^^^^^^^^^\n" +
				"Void methods cannot return a value\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test004() {
		this.runNegativeTest(new String[] { "X.java", """
				public class X {
					///
					/// @see #method()
					///
					public void sample() {
						return "";
					}
				}
				""", },

				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	/// @see #method()\n" +
				"	          ^^^^^^\n" +
				"Javadoc: The method method() is undefined for the type X\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	return \"\";\n" +
				"	^^^^^^^^^^\n" +
				"Void methods cannot return a value\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test005() {
		this.runNegativeTest(new String[] { "X.java", """
				public class X {
					///
					/// @see #method()
					///
					public void sample() {
						return "";
					}
					protected void method() {}
				}
				""", },

				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	return \"\";\n" +
				"	^^^^^^^^^^\n" +
				"Void methods cannot return a value\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test006() {
		this.runConformTest(new String[] { "X.java",
				"""
				public class X {
					/// @see #method()
					public static void main(String[] arguments) {
						System.out.println("Hello");
					}
					protected static void method() {}
				}
				""", },
				"Hello",
				getCompilerOptions(),
				new String[]{"--enable-preview"}
				);
	}
	public void test007() {
		this.runNegativeTest(new String[] { "X.java",
				"""
				public class X {
					///
					/// @see #method()
					public static void main(String[] arguments) {
						System.out.println("Hello");
					}
				}
				""", },
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	/// @see #method()\n" +
				"	          ^^^^^^\n" +
				"Javadoc: The method method() is undefined for the type X\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test008() {
		this.runNegativeTest(new String[] { "X.java",
				"""
				public class X {
					/// @see #method()
					///
					public static void main(String[] arguments) {
						System.out.println("Hello");
					}
				}
				""", },
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	/// @see #method()\n" +
				"	          ^^^^^^\n" +
				"Javadoc: The method method() is undefined for the type X\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test009() {
		this.runNegativeTest(new String[] { "X.java",
				"""
				public class X {
					//// @see #method()
					////
					public static void main(String[] arguments) {
						System.out.println("Hello");
					}
				}
				""", },
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	//// @see #method()\n" +
				"	           ^^^^^^\n" +
				"Javadoc: The method method() is undefined for the type X\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test010() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						/// Some text here without the necessary tags for main method
						/// @param arguments array of strings
						/// @return java.lang.Str -- should not raise an error

						/// no tags here
						public static void main(String[] arguments) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 7)\n" +
					"	public static void main(String[] arguments) {\n" +
					"	                                 ^^^^^^^^^\n" +
					"Javadoc: Missing tag for parameter arguments\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test011() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						/// Some text here without the necessary tags for main method
						/// @param arguments array of strings
						/// @return java.lang.Str

						// This line will be ignored and the previous block will be considered as the Javadoc
						public static void main(String[] arguments) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	/// @return java.lang.Str\n" +
					"	     ^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test012() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportJavadocDeprecation = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						/// Some text here without the necessary tags for main method
						/// @param arguments

						// This line will be ignored and the previous block will be considered as the Javadoc
						public static void main(String[] arguments) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// @param arguments\n" +
					"	           ^^^^^^^^^\n" +
					"Javadoc: Description expected after this reference\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test013() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						/// Some text here without the necessary tags for main method
						/// @param arguments array of strings

						/**
						 * This will be the effective javadoc and will be reported for missing tags
						 */
						public static void main(String[] arguments) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 8)\n" +
					"	public static void main(String[] arguments) {\n" +
					"	                                 ^^^^^^^^^\n" +
					"Javadoc: Missing tag for parameter arguments\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test014() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						/**
						 * Some text here without the necessary tags for main method
						 * @param arguments
						 */

						///
						/// This will be the effective javadoc and will be reported for missing tags
						///
						public static void main(String[] arguments) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 10)\n" +
					"	public static void main(String[] arguments) {\n" +
					"	                                 ^^^^^^^^^\n" +
					"Javadoc: Missing tag for parameter arguments\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	// Test mark down links inside []
	public void test015() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						///
						/// Reference to an invalid type [Strings]
						///
						public static void main() {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid type [Strings]\n" +
					"	                                  ^^^^^^^\n" +
					"Javadoc: Strings cannot be resolved to a type\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test016() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						///
						/// Reference to an invalid type [java.langs.Strings]
						///
						public static void main() {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid type [java.langs.Strings]\n" +
					"	                                  ^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: java.langs cannot be resolved to a type\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test017() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						///
						/// Reference to an invalid type [Strings] [java.langs.Strings]
						///
						public static void main() {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid type [Strings] [java.langs.Strings]\n" +
					"	                                  ^^^^^^^\n" +
					"Javadoc: Strings cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid type [Strings] [java.langs.Strings]\n" +
					"	                                            ^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: java.langs cannot be resolved to a type\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test018() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.ERROR;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						///
						/// Reference to an invalid type [Strings][java.langs.Strings]
						///
						public static void main() {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid type [Strings][java.langs.Strings]\n" +
					"	                                           ^^^^^^^^^^^^^^^^^^\n" +
					"Javadoc: java.langs cannot be resolved to a type\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test019() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.IGNORE;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						///
						/// Reference to an invalid method in a valid type [charArray()][java.lang.String#toCharArrays()]
						///
						public static void main(String[] args) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid method in a valid type [charArray()][java.lang.String#toCharArrays()]\n" +
					"	                                                                                  ^^^^^^^^^^^^\n" +
					"Javadoc: The method toCharArrays() is undefined for the type String\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test020() {
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.IGNORE;
			this.runNegativeTest(new String[] { "X.java",
					"""
					public class X {
						///
						/// Reference to an invalid method in a valid type [\\[\\]][java.lang.String#toCharArrays()]
						///
						public static void main(String[] args) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	/// Reference to an invalid method in a valid type [\\[\\]][java.lang.String#toCharArrays()]\n" +
					"	                                                                           ^^^^^^^^^^^^\n" +
					"Javadoc: The method toCharArrays() is undefined for the type String\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
	public void test021() {
		// arrays in method reference lack escaping.
		// TODO specific error message?
		String bkup = this.reportMissingJavadocTags;
		try {
			this.reportMissingJavadocTags = CompilerOptions.IGNORE;
			this.runNegativeTest(new String[] { "X.java",
					"""
					///
					/// Reference to method with array parameter: [#main(String[])]
					///
					public class X {
						public static void main(String[] args) {
							System.out.println("Hello");
						}
					}
					""", },
					"----------\n" +
					"1. ERROR in X.java (at line 2)\n" +
					"	/// Reference to method with array parameter: [#main(String[])]\n" +
					"	                                                    ^^^^^^^^\n" +
					"Javadoc: Invalid parameters declaration\n" +
					"----------\n",
					JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
		} finally {
			this.reportMissingJavadocTags = bkup;
		}
	}
}
