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
		return new RegressionTestSetup(ts);
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

public static Class testClass() {
	return BooleanTest.class;
}
}
