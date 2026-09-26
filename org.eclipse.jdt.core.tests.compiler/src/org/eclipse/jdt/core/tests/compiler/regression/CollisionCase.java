/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class CollisionCase extends AbstractRegressionTest {

public CollisionCase(Compliance compliance, TestInfo info) {
	super(compliance, info);
}

@Test
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

@Test
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
		"foo.bar cannot be resolved to a type\n" +
		"----------\n");
}
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=84886
@Test
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	class MyFoo {\n" +
			"		class Bar {}\n" +
			"	}\n" +
			"	static class MyFoo$Bar {}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	static class MyFoo$Bar {}\n" +
		"	             ^^^^^^^^^\n" +
		"Duplicate nested type MyFoo$Bar\n" +
		"----------\n");
}
}
