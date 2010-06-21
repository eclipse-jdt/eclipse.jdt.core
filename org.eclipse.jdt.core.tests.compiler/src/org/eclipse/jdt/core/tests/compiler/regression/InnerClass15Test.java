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

public class InnerClass15Test extends AbstractRegressionTest {
public InnerClass15Test(String name) {
	super(name);
}
static {
//	TESTS_NUMBERS = new int[] { 2 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test001() {
	this.runNegativeTest(new String[] {
		"X.java",
		"class X {\n" + 
		"	<X> void foo() {\n" + 
		"		class X {}\n" + 
		"	}\n" + 
		"}",
	},
	"----------\n" + 
	"1. WARNING in X.java (at line 2)\n" + 
	"	<X> void foo() {\n" + 
	"	 ^\n" + 
	"The type parameter X is hiding the type X\n" + 
	"----------\n" + 
	"2. WARNING in X.java (at line 3)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The nested type X is hiding the type parameter X of the generic method foo() of type X\n" + 
	"----------\n" + 
	"3. ERROR in X.java (at line 3)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The nested type X cannot hide an enclosing type\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test002() {
	this.runNegativeTest(new String[] {
		"X.java",
		"class X<X> {\n" + 
		"	void foo() {\n" + 
		"		class X {}\n" + 
		"	}\n" + 
		"}",
	},
	"----------\n" + 
	"1. WARNING in X.java (at line 1)\n" + 
	"	class X<X> {\n" + 
	"	        ^\n" + 
	"The type parameter X is hiding the type X<X>\n" + 
	"----------\n" + 
	"2. WARNING in X.java (at line 3)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The nested type X is hiding the type parameter X of type X<X>\n" + 
	"----------\n" + 
	"3. ERROR in X.java (at line 3)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The nested type X cannot hide an enclosing type\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
// note javac reports an error for this test, but that is
// incorrect, compare and contrast javac behavior with
// test004.
public void test003() {
	this.runNegativeTest(new String[] {
		"Y.java",
		"class Y {\n" +
		"class X {}\n" + 
		"	<X> void foo() {\n" + 
		"		class X {}\n" + 
		"	}\n" + 
		"}",
	},
	"----------\n" + 
	"1. WARNING in Y.java (at line 3)\n" + 
	"	<X> void foo() {\n" + 
	"	 ^\n" + 
	"The type parameter X is hiding the type Y.X\n" + 
	"----------\n" + 
	"2. WARNING in Y.java (at line 4)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The nested type X is hiding the type parameter X of the generic method foo() of type Y\n" + 
	"----------\n" + 
	"3. WARNING in Y.java (at line 4)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The type X is never used locally\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test004() {
	this.runNegativeTest(new String[] {
		"Y.java",
		"class Y {\n" +
		"class X {}\n" + 
		"   void foo() {\n" + 
		"		class X {}\n" + 
		"	}\n" + 
		"}",
	},
	"----------\n" + 
	"1. WARNING in Y.java (at line 4)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The type X is hiding the type Y.X\n" + 
	"----------\n" + 
	"2. WARNING in Y.java (at line 4)\n" + 
	"	class X {}\n" + 
	"	      ^\n" + 
	"The type X is never used locally\n" + 
	"----------\n");
}
public static Class testClass() {
	return InnerClass15Test.class;
}
}
