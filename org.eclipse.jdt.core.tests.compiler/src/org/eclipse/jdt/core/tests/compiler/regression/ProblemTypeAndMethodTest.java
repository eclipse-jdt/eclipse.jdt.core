/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

import junit.framework.Test;

public class ProblemTypeAndMethodTest extends AbstractRegressionTest {
public ProblemTypeAndMethodTest(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 169, 180 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {  
	return ProblemTypeAndMethodTest.class;
}

public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"        interface Moosh { void foo(); }\n" + 
			"\n" + 
			"        static abstract class A implements Moosh {}\n" + 
			"\n" + 
			"        static class W extends A {}\n" + 
			"        static class Y extends A {}\n" + 
			"        static class Z extends A {}\n" + 
			"        public static void main(String[] args) {\n" + 
			"                new W();  // throws ClassFormatError\n" + 
			"        }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	static class W extends A {}\n" + 
		"	             ^\n" + 
		"The type X.W must implement the inherited abstract method X.Moosh.foo()\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	static class Y extends A {}\n" + 
		"	             ^\n" + 
		"The type X.Y must implement the inherited abstract method X.Moosh.foo()\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	static class Z extends A {}\n" + 
		"	             ^\n" + 
		"The type X.Z must implement the inherited abstract method X.Moosh.foo()\n" + 
		"----------\n",
		null /* no extra class libraries */, 
		true /* flush output directory */, 
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */, 
		false /* do not show warning token */, 
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	ClassFileReader reader = this.getClassFileReader(OUTPUT_DIR + File.separator  +"X$W.class", "X$W");
	IBinaryMethod[] methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	int counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
	
	reader = this.getClassFileReader(OUTPUT_DIR + File.separator  +"X$Y.class", "X$Y");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
	
	reader = this.getClassFileReader(OUTPUT_DIR + File.separator  +"X$Z.class", "X$Z");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
}
}
