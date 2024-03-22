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

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExternalizeStringLiteralsTest_1_5 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 7 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiteralsTest_1_5(String name) {
	super(name);
}
public static Test suite() {
	return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_5);
}

public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import static java.lang.annotation.ElementType.*;
				import static java.lang.annotation.RetentionPolicy.*;
				import java.lang.annotation.Retention;
				import java.lang.annotation.Target;
				@Target({TYPE, FIELD, METHOD,
				         PARAMETER, CONSTRUCTOR,
				         LOCAL_VARIABLE, PACKAGE})
				@Retention(CLASS)
				public @interface X
				{
				    String[] value() default {};
				    String justification() default "";
				}"""
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				class X {
					String s2 = "test1"; //$NON-NLS-1$
					String s3 = "test2"; //$NON-NLS-1$//$NON-NLS-2$
				
				
					void foo() {
						String s4 = null;
						String s5 = "test3";
						String s6 = "test4";
						System.out.println("test5");
					}
				}""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s3 = "test2"; //$NON-NLS-1$//$NON-NLS-2$
				                                  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 8)
				String s5 = "test3";
				            ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			3. ERROR in X.java (at line 9)
				String s6 = "test4";
				            ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			4. ERROR in X.java (at line 10)
				System.out.println("test5");
				                   ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				class X {
					String s2 = "test1"; //$NON-NLS-1$
					String s3 = "test2"; //$NON-NLS-1$//$NON-NLS-2$
				
				
					void foo() {
						String s4 = null;
						String s5 = null;//$NON-NLS-1$
						String s6 = "test4";
						System.out.println("test5");
					}
				}""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in X.java (at line 3)
				String s3 = "test2"; //$NON-NLS-1$//$NON-NLS-2$
				                                  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 8)
				String s5 = null;//$NON-NLS-1$
				                 ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 9)
				String s6 = "test4";
				            ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			4. ERROR in X.java (at line 10)
				System.out.println("test5");
				                   ^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				class X {
					String s2 = "test1"; //$NON-NLS-1$
					String s3 = "test2"; //$NON-NLS-1$//$NON-NLS-2$
				\t
					@SuppressWarnings("nls")
					void foo() {
						String s4 = null;
						String s5 = null;//$NON-NLS-1$
						String s6 = "test4";
						System.out.println("test5");
					}
				}""",
		},
		null, customOptions,
		"""
			----------
			1. WARNING in X.java (at line 3)
				String s3 = "test2"; //$NON-NLS-1$//$NON-NLS-2$
				                                  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162903
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				class X {
				
					void foo() {
						String s6 = "SUCCESS";
						System.out.println(s6);
					}
				}""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in X.java (at line 4)
				String s6 = "SUCCESS";
				            ^^^^^^^^^
			Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162903
public void test006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("nls")
					public static void main(String[] args) {
						String s6 = "SUCCESS";
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
public void test007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@Annot({
						@A(name = "name", //$NON-NLS-1$
				 		value = "Test") //$NON-NLS-1$
					})
					@X2("") //$NON-NLS-1$
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
			1. ERROR in X.java (at line 3)
				@A(name = "name", //$NON-NLS-1$
				                  ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			2. ERROR in X.java (at line 4)
				value = "Test") //$NON-NLS-1$
				                ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			3. ERROR in X.java (at line 6)
				@X2("") //$NON-NLS-1$
				        ^^^^^^^^^^^^^
			Unnecessary $NON-NLS$ tag
			----------
			""",
		null,
		true,
		customOptions);
}
public static Class testClass() {
	return ExternalizeStringLiteralsTest_1_5.class;
}
}
