package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class CollisionCase extends AbstractRegressionTest {
	
public CollisionCase(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public static Class testClass() {
	return CollisionCase.class;
}

public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import foo.bar;\n" +
			"public class X {	\n" +
			"    foo	afoo; \n" +
			"    bar	abar; \n" +
			"    public static void main(String[] args) {	\n" +
			"		System.out.print(\"SUCCESS\");	\n" +
			"    }	\n" +
			"}	\n",
			"foo.java",
			"public class foo {}\n",
			"foo/bar.java",
			"package foo;\n" +
			"public class bar {}\n",
		},
		"SUCCESS");
}

public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    foo	afoo; \n" +
			"    foo.bar	abar; \n" +
			"}	\n",
			"foo.java",
			"public class foo {}\n",
			"foo/bar.java",
			"package foo;\n" +
			"public class bar {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	foo.bar	abar; \n" + 
		"	^^^^^^^\n" + 
		"foo.bar cannot be resolved (or is not a valid type) for the field X.abar\n" + 
		"----------\n");
}
}
