/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BooleanTest extends AbstractRegressionTest {
	
public BooleanTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new BooleanTest("test221"));
		return new RegressionTestSetup(ts, COMPLIANCE_1_4);
	}
	return setupSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public Object getAccessibleSelection(int i) {\n" + 
		"    int c, d;\n" + 
		"    if ((this == null) || ((d = 4) > 0)) {\n" + 
		"      c = 2;\n" + 
		"    }\n" + 
		"    else {\n" + 
		"      if (this == null) {\n" + 
		"        c = 3;\n" + 
		"        i++;\n" + 
		"      }\n" + 
		"      i++;\n" + 
		"    }\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public String getAccessibleSelection2(int i) {\n" + 
		"    int c, d;\n" + 
		"    return ((this == null) || ((d = 4) > 0))\n" + 
		"      ? String.valueOf(c = 2)\n" + 
		"      : String.valueOf(i++); \n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/H.java",
		"package p;\n" + 
		"public class H {\n" + 
		"  Thread fPeriodicSaveThread;\n" + 
		"  public void bar() {\n" + 
		"    int a = 0, b = 0;\n" + 
		"    if (a == 0 || (b = 2) == 2) {\n" + 
		"      //a = 1;\n" + 
		"    }\n" + 
		"    System.out.println(b);\n" + 
		"    if (b != 0) {\n" + 
		"      System.err.println(\"<bar>b should be equal to 0.\");\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public void bar2() {\n" + 
		"    int a = 0, b = 0;\n" + 
		"    if (a == 1 && (b = 2) == 2) {\n" + 
		"      //a = 1;\n" + 
		"    }\n" + 
		"    System.out.println(b);\n" + 
		"    if (b != 0) {\n" + 
		"      System.err.println(\"<bar2>b should be equal to 0.\");\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    new H().bar();\n" + 
		"    new H().bar2();\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test003() {
	this.runConformTest(new String[] {
		"p/I.java",
		"package p;\n" + 
		"/**\n" + 
		" * This test0 should run without producing a java.lang.ClassFormatError\n" + 
		" */\n" + 
		"public class I {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int i = 1, j;\n" + 
		"    if (((i > 0) || ((j = 10) > j--)) && (i < 12)) {\n" + 
		"      System.out.println(i);\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public static void main1(String[] args) {\n" + 
		"    int i = 1, j;\n" + 
		"    if (((i < 12) && ((j = 10) > j--)) || (i > 0)) {\n" + 
		"      System.out.println(i);\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public static void main2(String[] args) {\n" + 
		"    int i = 1, j;\n" + 
		"    if (((i < 12) && ((j = 10) > j--)) && (i > 0)) {\n" + 
		"      System.out.println(i);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test004() {
	this.runConformTest(new String[] {
		"p/J.java",
		"package p;\n" + 
		"/**\n" + 
		" * This test0 should run without producing a java.lang.ClassFormatError\n" + 
		" */\n" + 
		"public class J {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int i = 1, j;\n" + 
		"    if (((i > 0) || ((j = 10) > j--)) && (i < 12)) {\n" + 
		"      System.out.println(i);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/M.java",
		"package p;\n" + 
		"public class M {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int a = 0, b = 0;\n" + 
		"    if (a == 0 || (b = 2) == 2) {\n" + 
		"    }\n" + 
		"    if (b != 0) {\n" + 
		"      System.out.println(\"b should be equal to zero\");\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/Q.java",
		"package p;\n" + 
		"/**\n" + 
		" * This test0 should run without producing a java.lang.VerifyError\n" + 
		" */\n" + 
		"public class Q {\n" + 
		"  boolean bar() {\n" + 
		"    if (false && foo()) {\n" + 
		"      return true;\n" + 
		"    }\n" + 
		"    return false;\n" + 
		"  }\n" + 
		"  boolean foo() {\n" + 
		"    return true;\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    new Q().bar();\n" + 
		"  }\n" + 
		"}\n",
	});
}

// Bug 6596
public void test007() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (!(a&&b)){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");			
}
// Bug 6596
public void test008() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (!(a||b)){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");			
}
// Bug 6596
public void test009() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		final boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (!(a&&b)){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");			
}

// Bug 6596
public void test010() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (a == b){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");			
}

// Bug 46675
public void test011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		String s = null;\n" + 
			"		boolean b = s != null && (s.length() == 0 ? TestConst.c1 : TestConst.c2);\n" + 
			"		if (!b) System.out.println(\"SUCCESS\");\n" +
			"	}\n" + 
			"\n" + 
			"	public static class TestConst {\n" + 
			"		public static final boolean c1 = true;\n" + 
			"		public static final boolean c2 = true;\n" + 
			"	}\n" + 
			"}",
		},
		"SUCCESS");
}

// Bug 46675 - variation
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		String s = \"aaa\";\n" + 
			"		boolean b = s != null && (s.length() == 0 ? TestConst.c1 : TestConst.c2);\n" + 
			"		if (b) System.out.println(\"SUCCESS\");\n" +
			"	}\n" + 
			"\n" + 
			"	public static class TestConst {\n" + 
			"		public static final boolean c1 = true;\n" + 
			"		public static final boolean c2 = true;\n" + 
			"	}\n" + 
			"}",
		},
		"SUCCESS");
}

// Bug 46675 - variation
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		String s = \"aaa\";\n" + 
			"		boolean b = s == null || (s.length() == 0 ? TestConst.c1 : TestConst.c2);\n" + 
			"		if (!b) System.out.println(\"SUCCESS\");\n" +
			"	}\n" + 
			"\n" + 
			"	public static class TestConst {\n" + 
			"		public static final boolean c1 = false;\n" + 
			"		public static final boolean c2 = false;\n" + 
			"	}\n" + 
			"}",
		},
		"SUCCESS");
}

// Bug 47881
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"		boolean b = true;\n" + 
			"		b = b && false;                 \n" + 
			"		if (b) {\n" + 
			"			System.out.println(\"FAILED\");\n" + 
			"		} else {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"		}\n" + 
			"    }\n" + 
			"}\n" + 
			"\n",
		},
		"SUCCESS");
}

// Bug 47881 - variation
public void test015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"		boolean b = true;\n" + 
			"		b = b || true;                 \n" + 
			"		if (b) {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"		} else {\n" + 
			"			System.out.println(\"FAILED\");\n" + 
			"		}\n" + 
			"    }\n" + 
			"}\n" + 
			"\n",
		},
		"SUCCESS");
}
// Bug 47881 - variation
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"		boolean b = false;\n" + 
			"		b = b && true;                 \n" + 
			"		if (b) {\n" + 
			"			System.out.println(\"FAILED\");\n" + 
			"		} else {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"		}\n" + 
			"    }\n" + 
			"}\n" + 
			"\n",
		},
		"SUCCESS");
}

// Bug 47881 - variation
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" + 
			"\n" + 
			"    public static void main(String args[]) {\n" + 
			"		boolean b = true;\n" + 
			"		b = b || false;                 \n" + 
			"		if (b) {\n" + 
			"			System.out.println(\"SUCCESS\");\n" + 
			"		} else {\n" + 
			"			System.out.println(\"FAILED\");\n" + 
			"		}\n" + 
			"    }\n" + 
			"}\n" + 
			"\n",
		},
		"SUCCESS");
}
public static Class testClass() {
	return BooleanTest.class;
}
}
