/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Hashtable;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ParserTest extends AbstractRegressionTest {
public ParserTest(String name) {
	super(name);
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", delete this token\n" + 
		"----------\n"
	);
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws new\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws new\n" + 
		"	^^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n"
	);
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws new X\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws new X\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", throw expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	throws new X\n" + 
		"	           ^\n" + 
		"Syntax error, unexpected end of method\n" + 
		"----------\n"
	);
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		throws\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", delete this token\n" + 
		"----------\n"
	);
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		throws new\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws new\n" + 
		"	^^^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n"
	);
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		throws new X\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws new X\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", throw expected\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	throws new X\n" + 
		"	           ^\n" + 
		"Syntax error, unexpected end of initializer\n" + 
		"----------\n"
	);
}
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo()throw {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo()throw {\n" + 
		"	          ^^^^^\n" + 
		"Syntax error on token \"throw\", { expected\n" + 
		"----------\n"
	);
}
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo()throw E {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \"}\" to complete ClassBody\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo()throw E {\n" + 
		"	          ^^^^^\n" + 
		"Syntax error on token \"throw\", throws expected\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	}\n" + 
		"	^\n" + 
		"Syntax error on token \"}\", delete this token\n" + 
		"----------\n"
	);
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws e\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws e\n" + 
		"	^^^^^^^^\n" + 
		"Syntax error on tokens, delete these tokens\n" + 
		"----------\n"
	);
}
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(){\n" +
			"		throws e;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	throws e;\n" + 
		"	^^^^^^\n" + 
		"Syntax error on token \"throws\", throw expected\n" + 
		"----------\n"
	);
}
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(X, Object o, String s) {\n" +
			"	}\n" +
			"   public void bar(){}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public void foo(X, Object o, String s) {\n" + 
		"	                 ^\n" + 
		"Syntax error on token \",\", . expected\n" + 
		"----------\n"
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test012() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo() {\n" +
			"		\"foo\".equals(\"bar\");\n" +
			"		;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	             ^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		nls // custom options
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40681
 */
public void test013() {
	Hashtable nls = new Hashtable();
	nls.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo() {\n" +
			"		\"foo\".equals(\"bar\");\n" +
			"		//;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	\"foo\".equals(\"bar\");\n" + 
		"	             ^^^^^\n" + 
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
		"----------\n",
		null, // custom classpath
		true, // flush previous output dir content
		nls // custom options
	);
}

}
