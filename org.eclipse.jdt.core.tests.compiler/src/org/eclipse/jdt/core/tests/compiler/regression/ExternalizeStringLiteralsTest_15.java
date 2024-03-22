/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExternalizeStringLiteralsTest_15 extends AbstractRegressionTest {

private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 15");

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 6 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiteralsTest_15(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_15);
}

public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);

	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X
				{
				    String x = \"""
				        abcdefg
				        hijklmn
				        \""";
				}"""
		},
		null,
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String x = \"""
			        abcdefg
			        hijklmn
			        \""";
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JAVAC_OPTIONS);
}
public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);

	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X
				{
				    String x = \"""
				        abcdefg
				        hijklmn
				        \"""; //$NON-NLS-1$ //$NON-NLS-2$
				}"""
		},
		null,
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 6)
				\"""; //$NON-NLS-1$ //$NON-NLS-2$
				                   ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		JAVAC_OPTIONS);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);

	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X
				{
				    String x = \"""
				        abcdefg
				        hijklmn
				        \""";
				    @SuppressWarnings("nls")
				    void foo() {
				        String x2 = \"""
				            abcdefg
				            hijklmn
				            \""";
				    }
				}"""
		},
		null,
		customOptions,
		"""
			----------
			1. WARNING in X.java (at line 3)
				String x = \"""
			        abcdefg
			        hijklmn
			        \""";
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JAVAC_OPTIONS);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				class X {
				
					void foo() {
						String s6 = \"""
							SUCCESS
							\""";
						System.out.println(s6);
					}
				}""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in X.java (at line 4)
				String s6 = \"""
						SUCCESS
						\""";
				            ^^^^^^^^^^^^^^^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
						String s6 = \"""
							SUCCESS
							\"""; //$NON-NLS-1$
						System.out.println(s6);
					}
				}""",
		},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=237245
public void test006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@Annot({
						@A(name = \"""
				           name
				           \""", //$NON-NLS-1$
				 		value = \"""
				           Test
				           \""") //$NON-NLS-1$
					})
					@X2(\"""
				   \""") //$NON-NLS-1$
					void foo() {
					}
				}
				@interface Annot {
					A[] value();
				}
				@interface A {
					String name();
					String value();
				}
				@interface X2 {
					String value();
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				\""", //$NON-NLS-1$
				     ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 8)
				\""") //$NON-NLS-1$
				     ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 11)
				\""") //$NON-NLS-1$
				     ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		null,
		true,
		customOptions);
}
public static Class testClass() {
	return ExternalizeStringLiteralsTest_15.class;
}
}
