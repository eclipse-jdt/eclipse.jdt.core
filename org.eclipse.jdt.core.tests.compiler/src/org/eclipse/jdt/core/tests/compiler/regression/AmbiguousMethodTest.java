/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.*;

public class AmbiguousMethodTest extends AbstractComparableTest {

	public AmbiguousMethodTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}
	
	public static Class testClass() {
		return AmbiguousMethodTest.class;
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test001() {
		this.runConformTest(
			new String[] {
				"C.java",
				"public class C { public static void main(String[] args) { new B().m(\"works\"); } }\n" +
				"class B extends A { <T extends Comparable<T>> void m(T t) { System.out.println(t); } }\n" +
				"abstract class A { abstract <T extends Comparable<T>> void m(T t); }"
			},
			"works"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static interface I1<E> { void method(E o); }\n" +
				"	static interface I2<E> { void method(E o); }\n" +
				"	static interface I3<E> extends I1<E>, I2<E> {}\n" +
				"	static class Class1 implements I3<String> {\n" +
				"		public void method(String o) { System.out.println(o); }\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		I3<String> i = new Class1();\n" +
				"		i.method(\"works\");\n" +
				"	}\n" +
				"}"
			},
			"works"
		);
	}
}