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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class ExternalizeStringLiteralsTest extends AbstractRegressionTest {

static {
//		TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 2 };
//		TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiteralsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildTestSuite(testClass());
}

public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" + 
			"	void foo() {\n" + 
			"		System.out.println(\"a\");\n" + 
			"	} //$NON-NLS-1$	\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in A.java (at line 3)\n" + 
		"	System.out.println(\"a\");\n" + 
		"	                   ^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n" + 
		"2. ERROR in A.java (at line 4)\n" + 
		"	} //$NON-NLS-1$	\n" + 
		"	  ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}

public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	String s = null; //$NON-NLS-1$\n" +
			"	String s2 = \"\"; //$NON-NLS-1$\n" +
			"	String s3 = \"\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
			"	\n" +
			"	void foo() {\n" +
			"		String s4 = null; //$NON-NLS-1$\n" +
			"		String s5 = \"\"; //$NON-NLS-1$\n" +
			"		String s6 = \"\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
			"		System.out.println(\"foo\");//$NON-NLS-1$//$NON-NLS-2$\n" +
			"	} //$NON-NLS-1$\n" +
			"	//$NON-NLS-1$\n" +
			"}//$NON-NLS-3$",
		}, 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	String s = null; //$NON-NLS-1$\n" + 
		"	                 ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	String s3 = \"\"; //$NON-NLS-1$//$NON-NLS-2$\n" + 
		"	                             ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	String s4 = null; //$NON-NLS-1$\n" + 
		"	                  ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	String s6 = \"\"; //$NON-NLS-1$//$NON-NLS-2$\n" + 
		"	                             ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 10)\n" + 
		"	System.out.println(\"foo\");//$NON-NLS-1$//$NON-NLS-2$\n" + 
		"	                                       ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 11)\n" + 
		"	} //$NON-NLS-1$\n" + 
		"	  ^^^^^^^^^^^^^\n" + 
		"Unnecessary $NON-NLS$ tag\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 13)\n" + 
		"	}//$NON-NLS-3$\n" + 
		"	 ^^^^^^^^^^^^^\n" + 
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
			"p/Foo.java",
			"package p;\n" + 
			"public class Foo { \n" + 
			"    public void foo() {\n" + 
			"		System.out.println(\"string1\" + \"string2\" //$NON-NLS-1$\n" + 
			"		);\n" + 
			"}",
		}, 
		"----------\n" + 
		"1. ERROR in p\\Foo.java (at line 4)\n" + 
		"	System.out.println(\"string1\" + \"string2\" //$NON-NLS-1$\n" + 
		"	                               ^^^^^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n" + 
		"2. ERROR in p\\Foo.java (at line 6)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n",
		null,
		true,
		customOptions);	
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"p/Foo.java",
			"package p;\n" + 
			"public class Foo { \n" + 
			"    public void foo() {\n" + 
			"		//$NON-NLS-1$\n" + 
			"	 };\n" + 
			"}",
		}, 
		"",
		null,
		true,
		null,
		customOptions,
		null);	
}
public static Class testClass() {
	return ExternalizeStringLiteralsTest.class;
}
}
