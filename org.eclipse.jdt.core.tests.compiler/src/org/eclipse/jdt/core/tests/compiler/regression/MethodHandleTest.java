/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

/**
 * Regression test for MethodHandle.invokeExact(..)/invokeGeneric(..) invocation
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
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

	static {
//		TESTS_NAMES = new String [] { "test009" };
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					
					public class X {
						public static void main(String[] args) throws Throwable {
							MethodHandles.Lookup lookup = MethodHandles.lookup();
					
							MethodType mt = MethodType.methodType(String.class, String.class, char.class);
							MethodHandle mh = lookup.findStatic(X.class, "append", mt);
							String s = (String) mh.invokeExact("follo",'w');
							System.out.println(s);
					
							mt = MethodType.methodType(int.class, Object[].class);
							mh = lookup.findVirtual(X.class, "arrayLength", mt);
							int i = (int) mh.invokeExact(new X(), new Object[] {1, 'A', "foo"});
							System.out.println(i);
					
							mt = MethodType.methodType(void.class, String.class);
							mh = lookup.findStatic(X.class, "hello", mt);
							mh.invokeExact("world");
					
							mt = MethodType.methodType(Object.class, String.class, int.class);
							mh = lookup.findVirtual(X.class, "foo", mt);
							Object o = mh.invoke(new X(), (Object)"foo:", i);
					
							mt = MethodType.methodType(void.class);
							mh = lookup.findStatic(X.class, "bar", mt);
							mh.invokeExact();
						}
						public static void bar() {
							System.out.println("bar");
						}
						public Object foo(String s, int i) {
							System.out.println(s + i);
							return s + i;
						}
						public static String append(String s, char c) {
							return s + c;
						}
						public int arrayLength(Object[] array) {
							return array.length;
						}
						public static void hello(String name) {
							System.out.println("Hello, "+ name);
						}
					}"""
			},
			"""
				follow
				3
				Hello, world
				foo:3
				bar""");
	}
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					import java.lang.invoke.WrongMethodTypeException;
					
					public class X {
						public static void foo() {
						}
						public static void main(String[] args) {
							try {
								MethodHandle handle = MethodHandles.lookup().findStatic(X.class, "foo", MethodType.methodType(void.class));
								try {
									handle.invoke(null);
								} catch (WrongMethodTypeException ok) {
									System.out.println("This is ok");
								} catch (Throwable e) {
									e.printStackTrace();
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}"""
			},
			"This is ok");
	}
	public void test003() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					import java.lang.invoke.WrongMethodTypeException;
					
					public class X {
						public static <T> T foo(T param){
							return null;
						}
						public static void main(String[] args) {
							try {
								MethodHandle handle = MethodHandles.lookup().findStatic(X.class, "foo", MethodType.methodType(Object.class, Object.class));
								try {
									handle.invoke(null);
								} catch (Throwable e) {
									e.printStackTrace();
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}"""
			},
			"");
	}
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					import java.lang.invoke.WrongMethodTypeException;
					
					public class X {
						public static <T> T foo(T param){
							return null;
						}
						public static void main(String[] args) {
							try {
								MethodHandle handle = MethodHandles.lookup().findStatic(X.class, "foo", MethodType.methodType(Object.class, Object.class));
								try {
									handle.invoke(new Object());
								} catch (Throwable e) {
									e.printStackTrace();
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}"""
			},
			"");
	}
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					import java.lang.invoke.WrongMethodTypeException;
					
					public class X {
						public static <T> T foo(T param){
							return null;
						}
						public static void main(String[] args) {
							try {
								MethodHandle handle = MethodHandles.lookup().findStatic(X.class, "foo", MethodType.methodType(Object.class, Object.class));
								try {
									Object o = handle.invoke(new Object());
								} catch (Throwable e) {
									e.printStackTrace();
								}
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}"""
			},
			"");
	}
	public void test006() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					import java.lang.invoke.WrongMethodTypeException;
					
					public class X {
						public static void main(String[] args) throws Throwable {
							MethodHandles.Lookup lookup = MethodHandles.lookup();
					
							MethodType mt = MethodType.methodType(String.class, String.class, char.class);
							MethodHandle mh = lookup.findStatic(X.class, "append", mt);
							String s = (String) mh.invokeExact("follo",'w');
							System.out.println(s);
							MethodType mt2 = MethodType.methodType(String.class, String.class, char.class);
							MethodHandle mh2 = lookup.findStatic(X.class, "append", mt2);
							try {
								mh2.invokeExact("follo",'w');
							} catch(WrongMethodTypeException e) {
								System.out.println("Expected exception");
							}
						}
						public static String append(String s, char c) {
							return s + c;
						}
					}"""
			},
			"follow\n" +
			"Expected exception");
	}
	public void test007() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.invoke.MethodHandles.lookup;
					import static java.lang.invoke.MethodType.methodType;
					import java.lang.invoke.MethodHandle;
					
					public class X {
						public static void main(String[] args) throws Throwable {
							MethodHandle fooMH = lookup().findStatic(X.class, "foo", methodType(String.class));
							String s = (String) fooMH.invokeExact();
							System.out.println(s);
							fooMH.asType(methodType(void.class)).invokeExact();
						}
						public static String foo() {
							System.out.println("Inside foo");
							return "foo";
						}
					}"""
			},
			"""
				Inside foo
				foo
				Inside foo""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386259, wrong unnecessary cast warning.
	public void test009() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		runNegativeTest(
				// test directory preparation
				true /* flush output directory */,
				new String[] { /* test files */
						"X.java",
						"""
							import java.lang.invoke.MethodHandle;
							import java.lang.invoke.MethodHandles;
							import java.lang.invoke.MethodType;
							public class X {
							  public static void main(String[] args) throws Throwable {
							    String str = "test";
							    MethodHandle mh = MethodHandles.lookup().findVirtual(String.class, "toString",\s
							        MethodType.methodType(String.class));
							    String actual = (String) mh.invoke(str);
							    assert "test".equals(actual);
							    Zork z;
							  }
							}
							"""
				},
				// compiler options
				null /* no class libraries */,
				customOptions /* custom options */,
				"""
					----------
					1. ERROR in X.java (at line 11)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""",
				// javac options
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386259 variation.
	public void test010() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		runNegativeTest(
				// test directory preparation
				true /* flush output directory */,
				new String[] { /* test files */
						"X.java",
						"""
							import java.lang.invoke.MethodHandle;
							import java.lang.invoke.MethodHandles;
							import java.lang.invoke.MethodType;
							public class X {
							  public static void main(String[] args) throws Throwable {
							    String str = "test";
							    MethodHandle mh = MethodHandles.lookup().findVirtual(String.class, "toString",\s
							        MethodType.methodType(String.class));
							    Object actual = (Object) mh.invoke(str);
							    assert "test".equals(actual);
							    Zork z;
							  }
							}
							"""
				},
				// compiler options
				null /* no class libraries */,
				customOptions /* custom options */,
				"""
					----------
					1. ERROR in X.java (at line 9)
						Object actual = (Object) mh.invoke(str);
						                ^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Object to Object
					----------
					2. ERROR in X.java (at line 11)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""",
				// javac options
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=466748
	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.reflect.Method;
					
					public class X {
						public static void test1(Integer i){
							System.out.println("test1:" + i);
						}
						public static void test2(int i){
							System.out.println("test2:" + i);
						}
					
						public static void main(String[] args) throws Throwable{
							Method m1 = X.class.getMethod("test1", Integer.class);
							Method m2 = X.class.getMethod("test2", int.class);
					
							MethodHandle test1Handle = MethodHandles.lookup().unreflect(m1);
							MethodHandle test2Handle = MethodHandles.lookup().unreflect(m2);
						\t
							Integer arg_Integer = 1;
							int arg_int = 1;
						\t
							// results in a java.lang.VerifyError - but should work without error
							test1Handle.invokeExact(Integer.class.cast(arg_int));
						\t
							// The following line also results in a java.lang.VerifyError, but should actually throw a ClassCastException
							try {
								test2Handle.invokeExact(int.class.cast(arg_Integer));\s
							} catch(ClassCastException e) {
								System.out.println("SUCCESS");
							}
						}
					}"""
			},
			"test1:1\n" +
			"SUCCESS");
	}
}
