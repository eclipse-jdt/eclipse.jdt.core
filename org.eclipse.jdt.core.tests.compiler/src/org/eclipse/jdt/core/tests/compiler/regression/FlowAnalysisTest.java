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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

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

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        while ((char) (c1 = 0) == 1) {}\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        while ((char) (c1 = 0) == 1) ;\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        for (;(char) (c1 = 0) == 1;) ;\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127255
// Compiler incorrectly reports "variable may not have been initialized"
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public void test() {\n" + 
			"        int c1, c2;\n" + 
			"        do ; while ((char) (c1 = 0) == 1);\n" + 
			"        if (c1 == 0) {} // silent\n" + 
			"        if (c2 == 0) {} // complain\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	if (c2 == 0) {} // complain\n" + 
		"	    ^^\n" + 
		"The local variable c2 may not have been initialized\n" + 
		"----------\n",
		null, true, options);
}

public static Class testClass() {
	return FlowAnalysisTest.class;
}
}

