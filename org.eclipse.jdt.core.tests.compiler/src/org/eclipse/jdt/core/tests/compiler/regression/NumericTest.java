package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import junit.framework.TestSuite;

public class NumericTest extends AbstractRegressionTest {
	
public NumericTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new NumericTest("test221"));
		return new RegressionTestSetup(ts);
	}
	return setupSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/B.java",
		"package p;\n" + 
		"public class B {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int offset = -8;\n" + 
		"    int temp = 0 - offset;\n" + 
		"    offset = 0 - offset;  // This is the problem line\n" + 
		"    System.out.println(\"offset: \" + offset);\n" + 
		"    System.out.println(\"temp: \" + temp);\n" + 
		"    if (offset != temp ) {\n" + 
		"      System.err.println(\"offset (\" + offset + \") should be equal to temp (\" + temp + \").\");\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/Y.java",
		"package p;\n" + 
		"public class Y {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int clockend = 0;\n" + 
		"    clockend += 128;\n" + 
		"    if(clockend < 0) {\n" + 
		"      System.out.println(clockend);\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public static Class testClass() {
	return NumericTest.class;
}
}
