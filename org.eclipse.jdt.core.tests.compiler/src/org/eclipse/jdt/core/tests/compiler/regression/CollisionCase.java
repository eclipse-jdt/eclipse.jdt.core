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

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class CollisionCase extends AbstractRegressionTest {

public CollisionCase(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return CollisionCase.class;
}

public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import foo.bar;
				public class X {\t
				    foo	afoo;\s
				    bar	abar;\s
				    public static void main(String[] args) {\t
						System.out.print("SUCCESS");\t
				    }\t
				}\t
				""",
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
			"""
				public class X {\t
				    foo	afoo;\s
				    foo.bar	abar;\s
				}\t
				""",
			"foo.java",
			"public class foo {}\n",
			"foo/bar.java",
			"package foo;\n" +
			"public class bar {}\n",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				foo.bar	abar;\s
				^^^^^^^
			foo.bar cannot be resolved to a type
			----------
			""");
}
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=84886
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					class MyFoo {
						class Bar {}
					}
					static class MyFoo$Bar {}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				static class MyFoo$Bar {}
				             ^^^^^^^^^
			Duplicate nested type MyFoo$Bar
			----------
			""");
}
}
