/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

public class SuperTypeTest extends AbstractRegressionTest {

	public SuperTypeTest(String name) {
		super(name);
	}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 42, 43, 44 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return SuperTypeTest.class;
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=136106
	 */
	public void test001() {
		this.runConformTest(
			new String[] {
				/* org.eclipse.curiosity.A */
				"org/eclipse/curiosity/A.java",
				"package org.eclipse.curiosity;\n" + 
				"public abstract class A implements InterfaceA {\n" + 
				"	private void e() {\n" + 
				"	}\n" + 
				"	public void f() {\n" + 
				"		this.e();\n" + 
				"	}\n" + 
				"}",
				/* org.eclipse.curiosity.InterfaceA */
				"org/eclipse/curiosity/InterfaceA.java",
				"package org.eclipse.curiosity;\n" + 
				"public interface InterfaceA extends InterfaceBase {}\n",
				"org/eclipse/curiosity/InterfaceBase.java",
				/* org.eclipse.curiosity.InterfaceBase */
				"package org.eclipse.curiosity;\n" + 
				"public interface InterfaceBase {\n" + 
				"    public void a();\n" + 
				"    public void b();\n" + 
				"    public void c();\n" + 
				"    public void d();\n" + 
				"}"
			}
		);
	}
// was Compliance_1_x#test001
public void test002() {
	String[] sources = new String[] {
		"p1/Test.java",
		"package p1; \n"+
		"public class Test { \n"+
		"	public static void main(String[] arguments) { \n"+
		"		new Test().foo(); \n"+
		"	} \n"+
		"	class M { \n"+
		"	} \n"+
		"	void foo(){ \n"+
		"		class Y extends Secondary { \n"+
		"			M m; \n"+
		"		}; \n"+
		"		System.out.println(\"SUCCESS\");	\n" +
		"	} \n"+
		"} \n" +
		"class Secondary { \n" +
		"	class M {} \n" +
		"} \n"
	};
	if (this.complianceLevel.equals(COMPLIANCE_1_3)) {
		runNegativeTest(
			sources,
			"----------\n" + 
			"1. ERROR in p1\\Test.java (at line 10)\n" + 
			"	M m; \n" + 
			"	^\n" + 
			"The type M is defined in an inherited type and an enclosing scope\n" + 
			"----------\n");
	} else {
		runConformTest(
			sources,
			"SUCCESS");
	}
}	
}
