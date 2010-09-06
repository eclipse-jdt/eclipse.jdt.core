/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

public class GenericsRegressionTest extends AbstractComparableTest {

	public GenericsRegressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test322531j" };
//		TESTS_NAMES = new String[] { "test1464" };
//		TESTS_NUMBERS = new int[] { 1465 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return GenericsRegressionTest.class;
	}

	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz == clazz || \n" +
				"            X.class == X.class || \n" +
				"            I.class == I.class || \n" +
				"            clazz == X.class || \n" +
				"            X.class == clazz || \n" +
				"            clazz == I.class || \n" +
				"            I.class == clazz || \n" +
				"            I.class == X.class ||\n" +
				"            X.class == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz == clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	clazz == X.class || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	X.class == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<T>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X.class == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz == clazz || \n" +
				"            X.class == X.class || \n" +
				"            I.class == I.class || \n" +
				"            clazz == X.class || \n" +
				"            X.class == clazz || \n" +
				"            clazz == I.class || \n" +
				"            I.class == clazz || \n" +
				"            I.class == X.class ||\n" +
				"            X.class == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz == clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 13)\n" + 
			"	X.class == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public final class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x.getClass() == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#1-of ? extends X> and Class<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	clazz == x.getClass(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<capture#2-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531e() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public final class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x.getClass() == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#1-of ? extends X> and Class<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	clazz == x.getClass(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<capture#2-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface List<E> {}\n" +
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    void main(List<I> li, X t) {\n" +
				"        boolean b = I.class == t.getClass();\n" +
				"	         b = li == t.getList();\n" +
				"    }\n" +
				"    \n" +
				"    List<? extends Object> getList() {\n" +
				"    	return null;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	boolean b = I.class == t.getClass();\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<capture#1-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531h() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz, X t) {\n" +
				"        boolean b = \n" +
				"            clazz == t.getClass() || \n" +
				"            t.getClass() == clazz || \n" +
				"            I.class == t.getClass() ||\n" +
				"            t.getClass() == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I.class == t.getClass() ||\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<capture#3-of ? extends X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	t.getClass() == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#4-of ? extends X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {};\n" +
				"public class X {\n" +
				"    public X() {\n" +
				"    }\n" +
				"    public <T extends I> void test(Class<T> clazz) {\n" +
				"        Class<I> ci = I.class;\n" +
				"        Class<X> ti = X.class;\n" +
				"        boolean b = ci == X.class ||\n" +
				"        	        X.class == ci ||\n" +
				"        			I.class == X.class ||\n" +
				"        			X.class == I.class ||\n" +
				"        			ti == I.class ||\n" +
				"        			I.class == ti ||\n" +
				"        			ti == ci ||\n" +
				"        			ci == ti;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	boolean b = ci == X.class ||\n" + 
			"	            ^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	X.class == ci ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 10)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 11)\n" + 
			"	X.class == I.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 12)\n" + 
			"	ti == I.class ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 13)\n" + 
			"	I.class == ti ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 14)\n" + 
			"	ti == ci ||\n" + 
			"	^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 15)\n" + 
			"	ci == ti;\n" + 
			"	^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531j() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz != clazz || \n" +
				"            X.class != X.class || \n" +
				"            I.class != I.class || \n" +
				"            clazz != X.class || \n" +
				"            X.class != clazz || \n" +
				"            clazz != I.class || \n" +
				"            I.class != clazz || \n" +
				"            I.class != X.class ||\n" +
				"            X.class != I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz != clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	clazz != X.class || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	X.class != clazz || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<T>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	I.class != X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X.class != I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
}