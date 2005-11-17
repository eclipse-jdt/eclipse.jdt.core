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

public class FlowAnalysisTest extends AbstractRegressionTest {
	
public FlowAnalysisTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new FlowAnalysisTest("test221"));
		return new RegressionTestSetup(ts, COMPLIANCE_1_4);
	}
	return setupSuite(testClass());
}

public void test001() {
	this.runNegativeTest(new String[] {
		"X.java", // =================
		"public class X {\n" + 
		"	public String foo(int i) {\n" + 
		"		if (true) {\n" + 
		"			return null;\n" + 
		"		}\n" + 
		"		if (i > 0) {\n" + 
		"			return null;\n" + 
		"		}\n" + 
		"	}	\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 2)\n" + 
	"	public String foo(int i) {\n" + 
	"	              ^^^^^^^^^^\n" + 
	"This method must return a result of type String\n" + 
	"----------\n");
}

public static Class testClass() {
	return FlowAnalysisTest.class;
}
}

