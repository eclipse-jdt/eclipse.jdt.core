/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

public class InitializationTests extends AbstractRegressionTest {

public InitializationTests(String name) {
		super(name);
}

public static Test suite() {
	Test suite = buildAllCompliancesTestSuite(testClass());
	return suite;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public void foo() throws Exception{\n" + 
			"		String temp;\n" +
			"		Object temp2= new String(\"test\");\n" +
			"		if(temp2 instanceof String) {\n" +
			"			temp = (String) temp2;\n" +
			"		} else {\n" +
			"			if (true) {\n" +
			"				throw new Exception(\"not a string\");\n" +
			"			}\n" +
			"		}\n" +
			"		temp.trim();\n" +
			"	}\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	{\n" +
			"		if (true)\n" +
			"			throw new NullPointerException();\n" + 
			"	}\n" +
			"	public X(){}\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;" +
				"		if (true || (abc = new X(2)).returnA() == 2) {\n" +
				"			System.out.println(\"Hello\");\n" +
				"       } else { \n" +
				"			abc = new X(1);\n" +
				"		}\n" +
				"	}\n" +
				"}\n"

			}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	abc = new X(1);\n" + 
			"	^^^\n" + 
			"The final local variable abc may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public static boolean comparison (X x, int val) {\n" +
				"		return (x.returnA() == val);\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		boolean comp = X.comparison((abc = new X(2)), (abc = new X(1)).returnA());\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	boolean comp = X.comparison((abc = new X(2)), (abc = new X(1)).returnA());\n" + 
			"	                                               ^^^\n" + 
			"The final local variable abc may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		boolean comp = ((abc = new X(2)).returnA() == 1 || (abc = new X(1)).returnA() == 1);\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	boolean comp = ((abc = new X(2)).returnA() == 1 || (abc = new X(1)).returnA() == 1);\n" + 
			"	                                                    ^^^\n" + 
			"The final local variable abc may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020f() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		int val;\n" +
				"		if (true || (abc = new X(1)).returnA() == 1)\n" +
				"			val = (abc = new X(2)).returnA();\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	val = (abc = new X(2)).returnA();\n" + 
			"	       ^^^\n" + 
			"The final local variable abc may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020g() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final X abc;\n" +
				"		int val;\n" +
				"		if (true) {\n" +
				"			val = 0;\n" +
				"		} else {\n" +
				"			val = (abc = new X(1)).returnA();\n" +
				"		}\n" +
				"		abc = new X(2);\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 16)\n" + 
			"	abc = new X(2);\n" + 
			"	^^^\n" + 
			"The final local variable abc may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020h() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	final static X[] abc;\n" +
				"	static {\n" +
				"		for (Object[] j = new Object[1]; !(((abc = new X[10]).length) == 10); ){\n" +
				"			break;\n" +
				"		}\n" +
				"	}\n" +
				"	//Zork z;\n" +
				"}\n"

			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020i() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	class Inner {\n" +
				"		public int aInner;\n" +
				"		public Inner(int a){\n" +
				"			this.aInner = a;\n" +
				"		}\n" +
				"	}\n" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		int val;" +
				"		final int int1;\n" +
				"		final  int int2;\n" +
				"		val = new X(int1 = 1).new Inner(int2 = int1).aInner;\n" +
				"		System.out.println(int1 + int2);\n" +
				"	}\n" +
				"}\n"

			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"	public void foo() {\n" +
				"		final int abc;\n" +
				"		abc = new X(abc = 2).returnA();\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	abc = new X(abc = 2).returnA();\n" + 
			"	^^^\n" + 
			"The final local variable abc may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020k() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   private int a;\n" +
				"	final int x;\n" +
				"	{\n" +
				"		x = new X(x = 2).returnA();" +
				"	}\n" +
				"	public X (int a) {\n" +
				"		this.a = a;\n" +
				"	}\n" +
				"	public int returnA () {\n" +
				"		return a;\n" +
				"	}\n" +
				"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x = new X(x = 2).returnA();	}\n" + 
			"	^\n" + 
			"The final field x may already have been assigned\n" + 
			"----------\n",
			null, false, options);
}

public static Class testClass() {
	return InitializationTests.class;
}
}
