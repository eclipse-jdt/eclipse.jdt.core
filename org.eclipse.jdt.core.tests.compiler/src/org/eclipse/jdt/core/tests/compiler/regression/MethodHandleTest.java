/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

/**
 * Regression test for MethodHandle.invokeExact(..)/invokeGeneric(..) invocation
 */
public class MethodHandleTest extends AbstractRegressionTest {
	public MethodHandleTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}

	public static Class testClass() {
		return MethodHandleTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" + 
				"import java.lang.invoke.MethodHandles;\n" + 
				"import java.lang.invoke.MethodType;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) throws Throwable {\n" + 
				"		MethodHandles.Lookup lookup = MethodHandles.lookup();\n" + 
				"\n" + 
				"		MethodType mt = MethodType.methodType(String.class, String.class, char.class);\n" + 
				"		MethodHandle mh = lookup.findStatic(X.class, \"append\", mt);\n" + 
				"		String s = (String) mh.invokeExact(\"follo\",'w');\n" + 
				"		System.out.println(s);\n" + 
				"\n" + 
				"		mt = MethodType.methodType(int.class, Object[].class);\n" + 
				"		mh = lookup.findVirtual(X.class, \"arrayLength\", mt);\n" + 
				"		int i = (int) mh.invokeExact(new X(), new Object[] {1, 'A', \"foo\"});\n" + 
				"		System.out.println(i);\n" + 
				"\n" + 
				"		mt = MethodType.methodType(void.class, String.class);\n" + 
				"		mh = lookup.findStatic(X.class, \"hello\", mt);\n" + 
				"		mh.invokeExact(\"world\");\n" + 
				"\n" + 
				"		mt = MethodType.methodType(Object.class, String.class, int.class);\n" + 
				"		mh = lookup.findVirtual(X.class, \"foo\", mt);\n" + 
				"		Object o = mh.invokeGeneric(new X(), (Object)\"foo:\", i);\n" +
				"\n" +
				"		mt = MethodType.methodType(void.class);\n" + 
				"		mh = lookup.findStatic(X.class, \"bar\", mt);\n" + 
				"		mh.invokeExact();\n" + 
				"	}\n" +
				"	public static void bar() {\n" + 
				"		System.out.println(\"bar\");\n" + 
				"	}\n" +
				"	public Object foo(String s, int i) {\n" + 
				"		System.out.println(s + i);\n" + 
				"		return s + i;\n" + 
				"	}\n" +
				"	public static String append(String s, char c) {\n" + 
				"		return s + c;\n" + 
				"	}\n" +
				"	public int arrayLength(Object[] array) {\n" + 
				"		return array.length;\n" + 
				"	}\n" + 
				"	public static void hello(String name) {\n" + 
				"		System.out.println(\"Hello, \"+ name);\n" + 
				"	}\n" + 
				"}"
			},
			"follow\n" + 
			"3\n" + 
			"Hello, world\n" + 
			"foo:3\n" +
			"bar");
	}
}
