/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

public class FieldAccessTest extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 5, 6 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}

public FieldAccessTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.ERROR);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test001() {
	this.runConformTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" + 
			"class BaseFoo {\n" + 
			" public static final int VAL = 0;\n" + 
			"}",
			"foo/NextFoo.java",
			"package foo;\n" + 
			"public class NextFoo extends BaseFoo {\n" + 
			"}",
			"bar/Bar.java",
			"package bar;\n" + 
			"public class Bar {\n" + 
			" int v = foo.NextFoo.VAL;\n" + 
			"}"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test002() {
	this.runNegativeTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" + 
			"public class BaseFoo {\n" + 
			" public static final int VAL = 0;\n" + 
			"}",
			"foo/NextFoo.java",
			"package foo;\n" + 
			"public class NextFoo extends BaseFoo {\n" + 
			"}",
			"bar/Bar.java",
			"package bar;\n" + 
			"public class Bar {\n" + 
			" int v = foo.NextFoo.VAL;\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in bar\\Bar.java (at line 3)\n" + 
		"	int v = foo.NextFoo.VAL;\n" + 
		"	                    ^^^\n" + 
		"The static field BaseFoo.VAL should be accessed directly\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" + 
			"class BaseFoo {\n" + 
			" public static final int VAL = 0;\n" + 
			"}",
			"foo/NextFoo.java",
			"package foo;\n" + 
			"public class NextFoo extends BaseFoo {\n" + 
			"}",
			"bar/Bar.java",
			"package bar;\n" + 
			"import foo.NextFoo;\n" +
			"public class Bar {\n" +
			"	NextFoo[] tab = new NextFoo[] { new NextFoo() };\n" +
			"	int v = tab[0].VAL;\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"foo/BaseFoo.java",
			"package foo;\n" + 
			"public class BaseFoo {\n" + 
			" public static final int VAL = 0;\n" + 
			"}",
			"foo/NextFoo.java",
			"package foo;\n" + 
			"public class NextFoo extends BaseFoo {\n" + 
			"}",
			"bar/Bar.java",
			"package bar;\n" + 
			"import foo.NextFoo;\n" +
			"public class Bar {\n" +
			"	NextFoo[] tab = new NextFoo[] { new NextFoo() };\n" +
			"	int v = tab[0].VAL;\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in bar\\Bar.java (at line 5)\n" + 
		"	int v = tab[0].VAL;\n" + 
		"	               ^^^\n" + 
		"The static field BaseFoo.VAL should be accessed directly\n" + 
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142234
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private String memberVariable;\n" + 
			"	public String getMemberVariable() {\n" + 
			"		return (memberVariable);\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	return (memberVariable);\n" + 
		"	        ^^^^^^^^^^^^^^\n" + 
		"Unqualified access to the field X.memberVariable \n" + 
		"----------\n",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142234
public void test006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private String memberVariable;\n" + 
			"	public String getMemberVariable() {\n" + 
			"		return \\u0028memberVariable\\u0029;\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	return \\u0028memberVariable\\u0029;\n" + 
		"	             ^^^^^^^^^^^^^^\n" + 
		"Unqualified access to the field X.memberVariable \n" + 
		"----------\n",
		null,
		true,
		options);
}
public static Class testClass() {
	return FieldAccessTest.class;
}
}

