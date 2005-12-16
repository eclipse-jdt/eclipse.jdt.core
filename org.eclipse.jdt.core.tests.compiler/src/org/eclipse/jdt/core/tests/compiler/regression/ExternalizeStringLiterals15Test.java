/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ExternalizeStringLiterals15Test extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 3 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiterals15Test(String name) {
	super(name);
}
public static Test suite() {
	return buildTestSuiteUniqueCompliance(testClass(), COMPLIANCE_1_5);
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}
public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.Target;\n" +
			"@Target({TYPE, FIELD, METHOD,\r\n" + 
			"         PARAMETER, CONSTRUCTOR,\r\n" + 
			"         LOCAL_VARIABLE, PACKAGE})\r\n" + 
			"@Retention(CLASS)\r\n" + 
			"public @interface X\r\n" + 
			"{\r\n" + 
			"    String[] value() default {};\r\n" + 
			"    String justification() default \"\";\r\n" + 
			"}"
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
		new String[] {
			"X.java",
			"class X {\n" +
			"	String s2 = \"test1\"; //$NON-NLS-1$\n" +
			"	String s3 = \"test2\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
			"	\n" +
			"	@SuppressWarnings(\"nls\")\n" +
			"	void foo() {\n" +
			"		String s4 = null;\n" +
			"		String s5 = \"test3\";\n" +
			"		String s6 = \"test4\";\n" +
			"		System.out.println(\"test5\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	String s3 = \"test2\"; //$NON-NLS-1$//$NON-NLS-2$\n" + 
		"	                                  ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	String s2 = \"test1\"; //$NON-NLS-1$\n" +
			"	String s3 = \"test2\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
			"	\n" +
			"	@SuppressWarnings(\"nls\")\n" +
			"	void foo() {\n" +
			"		String s4 = null;\n" +
			"		String s5 = null;//$NON-NLS-1$\n" +
			"		String s6 = \"test4\";\n" +
			"		System.out.println(\"test5\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	String s3 = \"test2\"; //$NON-NLS-1$//$NON-NLS-2$\n" + 
		"	                                  ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	String s2 = \"test1\"; //$NON-NLS-1$\n" +
			"	String s3 = \"test2\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
			"	\n" +
			"	@SuppressWarnings(\"nls\")\n" +
			"	void foo() {\n" +
			"		String s4 = null;\n" +
			"		String s5 = null;//$NON-NLS-1$\n" +
			"		String s6 = \"test4\";\n" +
			"		System.out.println(\"test5\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	String s3 = \"test2\"; //$NON-NLS-1$//$NON-NLS-2$\n" + 
		"	                                  ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
public static Class testClass() {
	return ExternalizeStringLiterals15Test.class;
}
}
