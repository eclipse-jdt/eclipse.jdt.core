package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AnnotationTest extends AbstractRegressionTest {
	
public AnnotationTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new AnnotationTest("test221"));
		return new RegressionTestSetup(ts, COMPLIANCE_1_4);
	}
	return setupSuite(testClass());
}

public void test001() { 
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	{\n" + 
			"		new Z().foo();\n" + 
			"	}\n" + 
			"}\n",
			"Z.java",
			"public class Z {\n" + 
			"  /** \n"+
			"   * \n"+
			"   * **   ** ** ** @deprecated */\n" +
			"	public void foo() { \n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	new Z().foo();\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The method foo() from the type Z is deprecated\n" + 
		"----------\n");
}

public static Class testClass() {
	return AnnotationTest.class;
}
}
