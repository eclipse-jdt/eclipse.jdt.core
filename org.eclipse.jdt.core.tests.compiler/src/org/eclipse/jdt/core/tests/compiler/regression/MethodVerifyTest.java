/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 388800 - [1.8] adjust tests to 1.8 JRE
 *								bug 388795 - [compiler] detection of name clash depends on order of super interfaces
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *								bug 402237 - [1.8][compiler] investigate differences between compilers re MethodVerifyTest
 *								bug 395681 - [compiler] Improve simulation of javac6 behavior from bug 317719 after fixing bug 388795
 *								bug 409473 - [compiler] JDT cannot compile against JRE 1.8
 *								Bug 410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
 *	   Andy Clement - Contribution for
 *								bug 406928 - computation of inherited methods seems damaged (affecting @Overrides)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodVerifyTest extends AbstractComparableTest {
	static {
//		TESTS_NAMES = new String[] { "test124", "test124b" };
//		TESTS_NUMBERS = new int[] { 124 };
//		TESTS_RANGE = new int[] { 190, -1};
	}

	public MethodVerifyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return MethodVerifyTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
	}

	String mustOverrideMessage(String method, String type) {
		return "The method " + method + " of type " + type +
			(new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
				? " must override a superclass method\n"
				: " must override or implement a supertype method\n");
	}
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"""
					public class Y<T> extends X<A> { public void foo(T t) {} }
					class X<U> { public void foo(U u) {} }
					class A {}
					"""
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					public class Y<T> extends X<A> { public void foo(T t) {} }
					                                             ^^^^^^^^
				Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it
				----------
				"""
		);
	}

	public void test001a() {
		this.runNegativeTest(
				new String[] {
					"J.java",
					"""
						public class J<T> implements I<A> { public void foo(T t) {} }
						interface I<U> { public void foo(U u); }
						class A {}
						"""
				},
				"""
					----------
					1. ERROR in J.java (at line 1)
						public class J<T> implements I<A> { public void foo(T t) {} }
						             ^
					The type J<T> must implement the inherited abstract method I<A>.foo(A)
					----------
					2. ERROR in J.java (at line 1)
						public class J<T> implements I<A> { public void foo(T t) {} }
						                                                ^^^^^^^^
					Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it
					----------
					"""
			);
	}
	public void test001b() {
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n"
			},
			"""
				----------
				1. WARNING in YY.java (at line 1)
					public class YY<T> extends X { public void foo(T t) {} }
					                           ^
				X is a raw type. References to generic type X<U> should be parameterized
				----------
				2. ERROR in YY.java (at line 1)
					public class YY<T> extends X { public void foo(T t) {} }
					                                           ^^^^^^^^
				Name clash: The method foo(T) of type YY<T> has the same erasure as foo(Object) of type X but does not override it
				----------
				"""
		);
	}
	public void test001c() {
		this.runNegativeTest(
				new String[] {
						"JJ.java",
						"public class JJ<T> implements I { public void foo(T t) {} }\n" +
						"interface I<U> { public void foo(U u); }\n"
				},
				"""
					----------
					1. ERROR in JJ.java (at line 1)
						public class JJ<T> implements I { public void foo(T t) {} }
						             ^^
					The type JJ<T> must implement the inherited abstract method I.foo(Object)
					----------
					2. WARNING in JJ.java (at line 1)
						public class JJ<T> implements I { public void foo(T t) {} }
						                              ^
					I is a raw type. References to generic type I<U> should be parameterized
					----------
					3. ERROR in JJ.java (at line 1)
						public class JJ<T> implements I { public void foo(T t) {} }
						                                              ^^^^^^^^
					Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(Object) of type I but does not override it
					----------
					"""
		);
	}
	public void test001d() {
		this.runConformTest(
				new String[] {
						"YYY.java",
						"public class YYY<T> extends X<T> { public void foo(T t) {} }\n" +
						"class X<U> { public void foo(U u) {} }\n"
				},
				""
		);
	}
	public void test001e() {
		this.runConformTest(
				new String[] {
						"JJJ.java",
						"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n" +
						"interface I<U> { public void foo(U u); }\n"
				},
				""
		);
	}

	public void test002() { // separate files
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					public class Y<T> extends X<A> { public void foo(T t) {} }
					                                             ^^^^^^^^
				Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it
				----------
				"""
		);
	}
	public void test002a() { // separate files
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"""
				----------
				1. ERROR in J.java (at line 1)
					public class J<T> implements I<A> { public void foo(T t) {} }
					             ^
				The type J<T> must implement the inherited abstract method I<A>.foo(A)
				----------
				2. ERROR in J.java (at line 1)
					public class J<T> implements I<A> { public void foo(T t) {} }
					                                                ^^^^^^^^
				Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it
				----------
				"""
		);
	}
	public void test002b() { // separate files
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"""
				----------
				1. WARNING in YY.java (at line 1)
					public class YY<T> extends X { public void foo(T t) {} }
					                           ^
				X is a raw type. References to generic type X<U> should be parameterized
				----------
				2. ERROR in YY.java (at line 1)
					public class YY<T> extends X { public void foo(T t) {} }
					                                           ^^^^^^^^
				Name clash: The method foo(T) of type YY<T> has the same erasure as foo(Object) of type X but does not override it
				----------
				"""
		);
	}
	public void test002c() { // separate files
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"""
				----------
				1. ERROR in JJ.java (at line 1)
					public class JJ<T> implements I { public void foo(T t) {} }
					             ^^
				The type JJ<T> must implement the inherited abstract method I.foo(Object)
				----------
				2. WARNING in JJ.java (at line 1)
					public class JJ<T> implements I { public void foo(T t) {} }
					                              ^
				I is a raw type. References to generic type I<U> should be parameterized
				----------
				3. ERROR in JJ.java (at line 1)
					public class JJ<T> implements I { public void foo(T t) {} }
					                                              ^^^^^^^^
				Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(Object) of type I but does not override it
				----------
				"""
		);
	}
	public void test002d() { // separate files
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			""
		);
	}
	public void test002e() { // separate files
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			""
		);
	}

	public void test003() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n"
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					public class Y<T> extends X<A> { public void foo(T t) {} }
					                                             ^^^^^^^^
				Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it
				----------
				""",
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
	}
	public void test003a() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n"
			},
			"""
				----------
				1. ERROR in J.java (at line 1)
					public class J<T> implements I<A> { public void foo(T t) {} }
					             ^
				The type J<T> must implement the inherited abstract method I<A>.foo(A)
				----------
				2. ERROR in J.java (at line 1)
					public class J<T> implements I<A> { public void foo(T t) {} }
					                                                ^^^^^^^^
				Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it
				----------
				""",
			// J is not abstract and does not override abstract method foo(A) in I
			null,
			false,
			null
		);
	}
	public void test003b() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n"
			},
			"""
				----------
				1. WARNING in YY.java (at line 1)
					public class YY<T> extends X { public void foo(T t) {} }
					                           ^
				X is a raw type. References to generic type X<U> should be parameterized
				----------
				2. ERROR in YY.java (at line 1)
					public class YY<T> extends X { public void foo(T t) {} }
					                                           ^^^^^^^^
				Name clash: The method foo(T) of type YY<T> has the same erasure as foo(Object) of type X but does not override it
				----------
				""",
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
	}
	public void test003c() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n"
			},
			"""
				----------
				1. ERROR in JJ.java (at line 1)
					public class JJ<T> implements I { public void foo(T t) {} }
					             ^^
				The type JJ<T> must implement the inherited abstract method I.foo(Object)
				----------
				2. WARNING in JJ.java (at line 1)
					public class JJ<T> implements I { public void foo(T t) {} }
					                              ^
				I is a raw type. References to generic type I<U> should be parameterized
				----------
				3. ERROR in JJ.java (at line 1)
					public class JJ<T> implements I { public void foo(T t) {} }
					                                              ^^^^^^^^
				Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(Object) of type I but does not override it
				----------
				""",
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
			null,
			false,
			null
		);
	}
	public void test003d() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n"
			},
			"",
			null,
			false,
			null
		);
	}
	public void test003e() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n"
			},
			"",
			null,
			false,
			null
		);
	}

	public void test004() { // all together
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"""
					class A {}
					class B {}
					class X<U> { public U foo() {return null;} }
					interface I<U> { public U foo(); }
					class J<T> implements I<B> { public T foo() {return null;} }
					class K<T> implements I<T> { public T foo() {return null;} }
					class L<T> implements I { public T foo() {return null;} }
					class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
					class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }
					class W<T> extends X { @Override public T foo() { return super.foo(); } }
					""",
			},
			"""
				----------
				1. ERROR in ALL.java (at line 5)
					class J<T> implements I<B> { public T foo() {return null;} }
					                                    ^
				The return type is incompatible with I<B>.foo()
				----------
				2. WARNING in ALL.java (at line 7)
					class L<T> implements I { public T foo() {return null;} }
					                      ^
				I is a raw type. References to generic type I<U> should be parameterized
				----------
				3. ERROR in ALL.java (at line 8)
					class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
					                                           ^
				The return type is incompatible with X<A>.foo()
				----------
				4. ERROR in ALL.java (at line 8)
					class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
					                                                            ^^^^^^^^^^^
				Type mismatch: cannot convert from A to T
				----------
				5. WARNING in ALL.java (at line 10)
					class W<T> extends X { @Override public T foo() { return super.foo(); } }
					                   ^
				X is a raw type. References to generic type X<U> should be parameterized
				----------
				6. ERROR in ALL.java (at line 10)
					class W<T> extends X { @Override public T foo() { return super.foo(); } }
					                                                         ^^^^^^^^^^^
				Type mismatch: cannot convert from Object to T
				----------
				"""
		);
	}

	public void test005() { // separate files
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public U foo() {return null;} }\n",
				"I.java",
				"interface I<U> { public U foo(); }\n",

				"J.java",
				"class J<T> implements I<B> { public T foo() {return null;} }\n",
				"K.java",
				"class K<T> implements I<T> { public T foo() {return null;} }\n",
				"L.java",
				"class L<T> implements I { public T foo() {return null;} }\n",

				"Y.java",
				"class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
			},
			"""
				----------
				1. ERROR in J.java (at line 1)
					class J<T> implements I<B> { public T foo() {return null;} }
					                                    ^
				The return type is incompatible with I<B>.foo()
				----------
				----------
				1. WARNING in L.java (at line 1)
					class L<T> implements I { public T foo() {return null;} }
					                      ^
				I is a raw type. References to generic type I<U> should be parameterized
				----------
				----------
				1. ERROR in Y.java (at line 1)
					class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
					                                           ^
				The return type is incompatible with X<A>.foo()
				----------
				2. ERROR in Y.java (at line 1)
					class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
					                                                            ^^^^^^^^^^^
				Type mismatch: cannot convert from A to T
				----------
				----------
				1. WARNING in W.java (at line 1)
					class W<T> extends X { @Override public T foo() { return super.foo(); } }
					                   ^
				X is a raw type. References to generic type X<U> should be parameterized
				----------
				2. ERROR in W.java (at line 1)
					class W<T> extends X { @Override public T foo() { return super.foo(); } }
					                                                         ^^^^^^^^^^^
				Type mismatch: cannot convert from Object to T
				----------
				"""
		);
	}

	public void test006() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public U foo() {return null;} }\n",
				"I.java",
				"interface I<U> { public U foo(); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"class J<T> implements I<B> { public T foo() {return null;} }\n",
				"K.java",
				"class K<T> implements I<T> { public T foo() {return null;} }\n",
				"L.java",
				"class L<T> implements I { public T foo() {return null;} }\n",

				"Y.java",
				"class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
				},
				"""
					----------
					1. ERROR in J.java (at line 1)
						class J<T> implements I<B> { public T foo() {return null;} }
						                                    ^
					The return type is incompatible with I<B>.foo()
					----------
					----------
					1. WARNING in L.java (at line 1)
						class L<T> implements I { public T foo() {return null;} }
						                      ^
					I is a raw type. References to generic type I<U> should be parameterized
					----------
					----------
					1. ERROR in Y.java (at line 1)
						class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
						                                           ^
					The return type is incompatible with X<A>.foo()
					----------
					2. ERROR in Y.java (at line 1)
						class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }
						                                                            ^^^^^^^^^^^
					Type mismatch: cannot convert from A to T
					----------
					----------
					1. WARNING in W.java (at line 1)
						class W<T> extends X { @Override public T foo() { return super.foo(); } }
						                   ^
					X is a raw type. References to generic type X<U> should be parameterized
					----------
					2. ERROR in W.java (at line 1)
						class W<T> extends X { @Override public T foo() { return super.foo(); } }
						                                                         ^^^^^^^^^^^
					Type mismatch: cannot convert from Object to T
					----------
					""",
			/*
			J.java:1: J is not abstract and does not override abstract method foo() in I
			J.java:1: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			W.java:1: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			Y.java:1: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			Y.java:1: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			 */
			null,
			false,
			null
		);
	}

	public void test007() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					abstract class A implements I {}
					interface I extends J { String foo(); }
					interface J { Object foo(); }
					""",
				"X.java",
				"abstract class X1 extends A implements J {}\n"
			},
			""
		);
	}
	public void test007a() { // simple covariance cases
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					abstract class A implements I {}
					interface I extends J { Object foo(); }
					interface J { String foo(); }
					""",
				"X.java",
				"abstract class X2 extends A implements J {}\n"
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					interface I extends J { Object foo(); }
					                        ^^^^^^
				The return type is incompatible with J.foo()
				----------
				"""
		);
	}
	public void test007b() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X3 extends A implements J {}\n" +
				"interface J { Object foo(); }\n"
			},
			""
		);
	}
	public void test007c() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I { Object foo(); }\n",
				"X.java",
				"abstract class X4 extends A implements J {}\n" +
				"interface J { String foo(); }\n"
			},
			""
		);
	}
	public void test007d() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"class A { public String foo() { return null; } }\n" +
				"interface I { Object foo(); }\n",
				"X.java",
				"abstract class X5 extends A implements I {}\n"
			},
			""
		);
	}
	public void test007e() { // simple covariance cases
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public Object foo() { return null; } }\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X6 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X6 extends A implements I {}
					               ^^
				The type X6 must implement the inherited abstract method I.foo() to override A.foo()
				----------
				"""
		);
	}
	public void test007f() { // simple covariance cases
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { int get(short i, short s) { return i; } }\n" +
				"class B extends A { @Override short get(short i, short s) {return i; } }\n"
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					class B extends A { @Override short get(short i, short s) {return i; } }
					                              ^^^^^
				The return type is incompatible with A.get(short, short)
				----------
				"""
		);
	}

	public void test008() { // covariance test
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"""
					interface I { I foo(); }
					class A implements I { public A foo() { return null; } }
					class B extends A { @Override public B foo() { return null; } }
					class C extends B { @Override public A foo() { return null; } }
					class D extends B implements I {}
					""",
			},
			"""
				----------
				1. ERROR in ALL.java (at line 4)
					class C extends B { @Override public A foo() { return null; } }
					                                     ^
				The return type is incompatible with B.foo()
				----------
				"""
		);
	}

	public void test009() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class G<T> {}
					interface I { void foo(G<I> x); }
					abstract class A implements I { void foo(G<A> x) {} }
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 3)
					abstract class A implements I { void foo(G<A> x) {} }
					                                     ^^^^^^^^^^^
				Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it
				----------
				"""
		);
	}
	public void test009a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class G<T> {}
					interface I { I foo(G<I> x); }
					abstract class A implements I { I foo(G<A> x) { return null; } }
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 3)
					abstract class A implements I { I foo(G<A> x) { return null; } }
					                                  ^^^^^^^^^^^
				Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it
				----------
				"""
		);
	}

	public void test010() { // executable bridge method case
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public X foo() {
					        System.out.println("Did NOT add bridge method");
					        return this;
					    }
					    public static void main(String[] args) throws Exception {
					        X x = new A();
					        x.foo();
					        System.out.print(" + ");
					        I i = new A();
					        i.foo();
					    }
					}
					interface I {
					    public I foo();
					}
					class A extends X implements I {
					    public A foo() {
					        System.out.print("Added bridge method");
					        return this;
					    }
					}
					"""
			},
			"Added bridge method + Added bridge method"
		);
	}

	public void test011() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X1 extends A implements I {}
					               ^^
				The inherited method A.foo(T) cannot hide the public abstract method in I
				----------
				"""
		);
	}
	public void test011a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X2 extends A implements I {}
					               ^^
				Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it
				----------
				"""
		);
	}
	public void test011b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T, S> void foo(T t); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X3 extends A implements I {}
					               ^^
				Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it
				----------
				"""
		);
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T s) {} }\n" +
				"class Y1 extends A { @Override void foo(Object s) {} }\n"
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					class Y1 extends A { @Override void foo(Object s) {} }
					                                    ^^^^^^^^^^^^^
				Cannot reduce the visibility of the inherited method from A
				----------
				"""
		);
	}
	public void test012a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T[] s) {} }\n" +
				"class Y2 extends A { @Override void foo(Object[] s) {} }\n"
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					class Y2 extends A { @Override void foo(Object[] s) {} }
					                                    ^^^^^^^^^^^^^^^
				Cannot reduce the visibility of the inherited method from A
				----------
				"""
		);
	}
	public void test012b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public void foo(Class<Object> s) {} }\n" +
				"class Y3 extends A { @Override void foo(Class<Object> s) {} }\n"
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					class Y3 extends A { @Override void foo(Class<Object> s) {} }
					                                    ^^^^^^^^^^^^^^^^^^^^
				Cannot reduce the visibility of the inherited method from A
				----------
				"""
		);
	}

	public void test013() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runConformTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X0 extends A implements I {}\n"
			},
			""
		);
	}
	public void test013a() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X1 extends A implements I {}
					               ^^
				Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it
				----------
				"""
		);
	}
	public void test013b() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T, S> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X2 extends A implements I {}
					               ^^
				Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it
				----------
				"""
		);
	}
	public void test013c() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> S foo(Class<T> s) { return null; } }\n" +
				"interface I { <T> Object foo(Class<T> s); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X3 extends A implements I {}
					               ^^
				Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it
				----------
				"""
		);
	}
	public void test013d() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> Object foo(Class<T> s) { return null; } }\n" +
				"interface I { <T, S> S foo(Class<T> s); }\n",
				"X.java",
				"abstract class X4 extends A implements I {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X4 extends A implements I {}
					               ^^
				Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it
				----------
				"""
		);
	}
	public void test013e() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",

				"X.java",
				"class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					class X5 extends A implements I { public <T> void foo(Class<T> s) {} }
					                                                  ^^^^^^^^^^^^^^^
				Name clash: The method foo(Class<T>) of type X5 has the same erasure as foo(Class<T>) of type A but does not override it
				----------
				"""
		);
	}

	public void test014() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X { void foo(A a) {} }
					class Y extends X { void foo(A a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test014a() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X { void foo(A[] a) {} }
					class Y extends X { void foo(A[] a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test014b() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X { void foo(A<String>[] a) {} }
					class Y extends X { void foo(A[] a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test014c() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X { void foo(A<String> a) {} }
					class Y extends X { void foo(A a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test014d() { // name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X { void foo(A a) {} }
					class Y extends X { void foo(A<String> a) {} }
					class A<T> {}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 1)
					class X { void foo(A a) {} }
					                   ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				2. ERROR in X.java (at line 2)
					class Y extends X { void foo(A<String> a) {} }
					                         ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type X but does not override it
				----------
				"""
		);
	}
	public void test014e() { // name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X { void foo(A[] a) {} }
					class Y extends X { void foo(A<String>[] a) {} }
					class A<T> {}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 1)
					class X { void foo(A[] a) {} }
					                   ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				2. ERROR in X.java (at line 2)
					class Y extends X { void foo(A<String>[] a) {} }
					                         ^^^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type X but does not override it
				----------
				"""
		);
	}

	public void test015() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					abstract class X extends Y implements I { }
					interface I { void foo(A a); }
					class Y { public void foo(A a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test015a() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					abstract class X extends Y implements I { }
					interface I { void foo(A[] a); }
					class Y { public void foo(A[] a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test015b() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					abstract class X extends Y implements I { }
					interface I { void foo(A<String>[] a); }
					class Y { public void foo(A[] a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test015c() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					abstract class X extends Y implements I { }
					interface I { void foo(A<String> a); }
					class Y { public void foo(A a) {} }
					class A<T> {}
					"""
			},
			""
		);
	}
	public void test015d() { // more name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class X extends Y implements I { }
					interface I { void foo(A a); }
					class Y { public void foo(A<String> a) {} }
					class A<T> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X extends Y implements I { }
					               ^
				Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it
				----------
				2. WARNING in X.java (at line 2)
					interface I { void foo(A a); }
					                       ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				"""
		);
	}
	public void test015e() { // more name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class X extends Y implements I { }
					interface I { void foo(A[] a); }
					class Y { public void foo(A<String>[] a) {} }
					class A<T> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					abstract class X extends Y implements I { }
					               ^
				Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type I but does not override it
				----------
				2. WARNING in X.java (at line 2)
					interface I { void foo(A[] a); }
					                       ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				"""
		);
	}

	public void test016() { // 73971
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<E extends A> void m(E e) { System.out.print("A="+e.getClass()); }
						<E extends B> void m(E e) { System.out.print("B="+e.getClass()); }
						public static void main(String[] args) {
							new X().m(new A());
							new X().m(new B());
						}
					}
					class A {}
					class B extends A {}
					"""
			},
			"A=class AB=class B"
		);
	}
	public void test016b() { // 73971
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static <E extends A> void m(E e) { System.out.print("A="+e.getClass()); }
						static <E extends B> void m(E e) { System.out.print("B="+e.getClass()); }
						public static void main(String[] args) {
							m(new A());
							m(new B());
						}
					}
					class A {}
					class B extends A {}
					"""
			},
			"A=class AB=class B"
		);
	}

	public void test017() { // 77785
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<T> {}
					class Y { void test(X<? extends Number> a) {} }
					class Z extends Y { void test(X<Number> a) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					class Z extends Y { void test(X<Number> a) {} }
					                         ^^^^^^^^^^^^^^^^^
				Name clash: The method test(X<Number>) of type Z has the same erasure as test(X<? extends Number>) of type Y but does not override it
				----------
				"""
		);
	}
	public void test017a() { // 77785
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<T> {}
					class Y { void test(X<Number> a) {} }
					class Z extends Y { void test(X<? extends Number> a) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					class Z extends Y { void test(X<? extends Number> a) {} }
					                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Name clash: The method test(X<? extends Number>) of type Z has the same erasure as test(X<Number>) of type Y but does not override it
				----------
				"""
		);
	}

	public void test018() { // 77861
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X implements Comparable<X> {
						public int compareTo(Object o) { return 0; }
						public int compareTo(X o) { return 1; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public int compareTo(Object o) { return 0; }
					           ^^^^^^^^^^^^^^^^^^^
				Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it
				----------
				"""
		);
	}

	public void test019() { // 78140
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public class A {
						<T> T get() { return null; }\s
					}
					class B extends A {
						<T> T get() { return null; }\s
					}
					"""
			},
			""
		);
	}

	public void test020() { // 78232
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						public static void main(String[] args) {
							AbstractBase ab = new AbstractBase();
							Derived d = new Derived();
							AbstractBase ab2 = new Derived();
							Visitor<String, String> v = new MyVisitor();
							System.out.print(ab.accept(v, ab.getClass().getName()));
							System.out.print('+');
							System.out.print(d.accept(v, d.getClass().getName()));
							System.out.print('+');
							System.out.print(ab2.accept(v, ab2.getClass().getName()));
						}
						static class MyVisitor implements Visitor<String, String> {
							public String visitBase(AbstractBase ab, String obj) { return "Visited base: " + obj; }
							public String visitDerived(Derived d, String obj) { return "Visited derived: " + obj; }
						}
					}
					interface Visitor<R, T> {
						R visitBase(AbstractBase ab, T obj);
						R visitDerived(Derived d, T obj);
					}
					interface Visitable {
						<R, T> R accept(Visitor<R, T> v, T obj);
					}
					class AbstractBase implements Visitable {
						public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitBase(this, obj); }
					}
					class Derived extends AbstractBase implements Visitable {
						public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitDerived(this, obj); }
					}
					"""
			},
			"Visited base: AbstractBase+Visited derived: Derived+Visited derived: Derived"
		);
	}

	public void test021() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					public class A {
						public void foo(java.util.Map<String, Class<?>> m) { }\s
					}
					""",
				"B.java",
				"""
					class B extends A {
						@Override void foo(java.util.Map<String, Class<?>> m) { }\s
					}
					"""
			},
			"""
				----------
				1. ERROR in B.java (at line 2)
					@Override void foo(java.util.Map<String, Class<?>> m) { }\s
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot reduce the visibility of the inherited method from A
				----------
				"""
		);
		// now save A & pick it up as a binary type
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public class A {
						public void foo(java.util.Map<String, Class<?>> m) { }\s
					}
					"""
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"""
					class B extends A {
						@Override void foo(java.util.Map<String, Class<?>> m) { }\s
					}
					"""
			},
			"""
				----------
				1. ERROR in B.java (at line 2)
					@Override void foo(java.util.Map<String, Class<?>> m) { }\s
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot reduce the visibility of the inherited method from A
				----------
				""",
			null,
			false,
			null
		);
	}

	public void test022() { // 77562
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.util.*;
					class A { List getList() { return null; } }
					class B extends A { @Override List<String> getList() { return null; } }
					"""
			},
			""
		);
	}
	public void test022a() { // 77562
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					import java.util.*;
					class A { List<String> getList() { return null; } }
					class B extends A { @Override List getList() { return null; } }
					"""
			},
			"""
				----------
				1. WARNING in A.java (at line 3)
					class B extends A { @Override List getList() { return null; } }
					                              ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in A.java (at line 3)
					class B extends A { @Override List getList() { return null; } }
					                              ^^^^
				Type safety: The return type List for getList() from the type B needs unchecked conversion to conform to List<String> from the type A
				----------
				"""
		);
	}

	public void test023() { // 80739
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A<T> {
						void foo(T t) {}
						void foo(String i) {}
					}
					class B extends A<String> {}
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 5)
					class B extends A<String> {}
					      ^
				Duplicate methods named foo with the parameters (String) and (T) are defined by the type A<String>
				----------
				"""
		);
	}

	public void test024() { // 80626
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					class A {
						public <E extends Object> void m(E e) {}
					}
					class B extends A {
						public void m(Object e) {}
					}
					"""
			},
			""
			// no complaint
		);
	}
	public void test024a() { // 80626
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A {
						public void m(Object e) {}
					}
					class B extends A {
						public <E extends Object> void m(E e) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 5)
					public <E extends Object> void m(E e) {}
					                               ^^^^^^
				Name clash: The method m(E) of type B has the same erasure as m(Object) of type A but does not override it
				----------
				"""
		);
	}
	public void test024b() { // 80626
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A {
						public <E extends Object> void m(E e) {}
					}
					class B extends A {
						@Override public void m(Object e) {}
					}
					class C extends B {
						public <E extends Object> void m(E e) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 8)
					public <E extends Object> void m(E e) {}
					                               ^^^^^^
				Name clash: The method m(E) of type C has the same erasure as m(Object) of type B but does not override it
				----------
				"""
		);
	}

	public void test025() { // 81618
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							new B().test();
						}
					}
					class A {
						<T extends Number> T test() { return null; }
					}
					class B extends A {
						@Override Integer test() { return 1; }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 10)
					@Override Integer test() { return 1; }
					          ^^^^^^^
				Type safety: The return type Integer for test() from the type B needs unchecked conversion to conform to T from the type A
				----------
				"""
		);
	}
	public void test025a() { // 81618
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							new B().test();
						}
					}
					class A {
						<T extends Number> T[] test() { return null; }
					}
					class B extends A {
						@Override Integer[] test() { return new Integer[] {2}; }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 10)
					@Override Integer[] test() { return new Integer[] {2}; }
					          ^^^^^^^^^
				Type safety: The return type Integer[] for test() from the type B needs unchecked conversion to conform to T[] from the type A
				----------
				"""
		);
	}
	public void test025b() { // 81618
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(new B().<Integer>test(new Integer(1)));
						}
					}
					class A {
						<T> T test(T t) { return null; }
					}
					class B extends A {
						@Override <T> T test(T t) { return t; }
					}
					"""
			},
			"1"
		);
	}
	public void test025c() { // 81618
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(new B().<Number>test(1));
						}
					}
					class A<T> {
						<U> T test(U u) { return null; }
					}
					class B extends A<Integer> {
						@Override <U> Integer test(U u) { return 1; }
					}
					"""
			},
			"1"
		);
	}
	public void test025d() { // 81618
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					import java.util.concurrent.Callable;
					public class A {
						public static void main(String[] args) throws Exception {
							Callable<Integer> integerCallable = new Callable<Integer>() {
								public Integer call() { return new Integer(1); }
							};
							System.out.println(integerCallable.call());
						}
					}
					"""
			},
			"1"
		);
	}
	public void test025e() { // 81618
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					interface X<T extends X> { T x(); }
					abstract class Y<S extends X> implements X<S> { public abstract S x(); }
					abstract class Z implements X { public abstract X x(); }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 1)
					interface X<T extends X> { T x(); }
					                      ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				2. WARNING in X.java (at line 2)
					abstract class Y<S extends X> implements X<S> { public abstract S x(); }
					                           ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				3. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X x(); }
					                            ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				4. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X x(); }
					                                                ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}
	public void test025f() { // 81618
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					interface X<T extends X> { T[] x(); }
					abstract class Y<S extends X> implements X<S> { public abstract S[] x(); }
					abstract class Z implements X { public abstract X[] x(); }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 1)
					interface X<T extends X> { T[] x(); }
					                      ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				2. WARNING in X.java (at line 2)
					abstract class Y<S extends X> implements X<S> { public abstract S[] x(); }
					                           ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				3. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X[] x(); }
					                            ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				4. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X[] x(); }
					                                                ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}

	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.print(
								new B().test().getClass() + " & "
								+ new C().test().getClass() + " & "
								+ new D().test().getClass());
						}
					}
					class A<T extends Number> {
						A<T> test() { return this; }
					}
					class B extends A {
						A test() { return super.test(); }
					}
					class C extends A<Integer> {
						A<Integer> test() { return super.test(); }
					}
					class D<U, V extends Number> extends A<V> {
						A<V> test() { return super.test(); }
					}
					"""
			},
			"class B & class C & class D"
		);
	}
	public void test026a() {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public abstract class A<E> {
						public abstract A<E> test();
					}
					class H<K,V> {
						class M extends A<K> {
							public A<K> test() { return null; }
						}
					}
					"""
			},
			""
		);
	}
	public void test026b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X extends java.util.AbstractMap {
						public java.util.Set entrySet() { return null; }
					}
					"""
			},
			""
		);
	}
	public void test026c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.print(new C().test().getClass());
						}
					}
					class A<T extends Number> {
						A<T> test() { return this; }
					}
					class C extends A<Integer> {
						@Override A test() { return super.test(); }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 10)
					@Override A test() { return super.test(); }
					          ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				2. WARNING in X.java (at line 10)
					@Override A test() { return super.test(); }
					          ^
				Type safety: The return type A for test() from the type C needs unchecked conversion to conform to A<T> from the type A<T>
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X { <T> void test() {} }\n" +
				"class Y extends X { void test() {} }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test() {} }\n" +
				"class Y extends X { <T> void test() {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class Y extends X { <T> void test() {} }
					                             ^^^^^^
				Name clash: The method test() of type Y has the same erasure as test() of type X but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o) {} }\n" +
				"class Y<T> extends X<T> { void test(Object o) {} }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o, T t) {} }\n" +
				"class Y<T> extends X<T> { void test(Object o, T t) {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class Y<T> extends X<T> { void test(Object o, T t) {} }
					                               ^^^^^^^^^^^^^^^^^^^
				Name clash: The method test(Object, T) of type Y<T> has the same erasure as test(T, T) of type X<T> but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test() {
							Pair<Double, Integer> p = new InvertedPair<Integer, Double>();
							p.setA(Double.valueOf(1.1));
						}
					}
					class Pair<A, B> {
						public void setA(A a) {}
					}
					class InvertedPair<A, B> extends Pair<B, A> {
						public void setA(A a) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					public void setA(A a) {}
					            ^^^^^^^^^
				Name clash: The method setA(A) of type InvertedPair<A,B> has the same erasure as setA(A) of type Pair<A,B> but does not override it
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81727
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements I<X>{
						public X foo() { return null; }
					}
					interface I<T extends I> { T foo(); }
					"""
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81568
	public void test029() {
		this.runConformTest(
			new String[] {
				"I.java",
				"""
					public interface I {
						public I clone();
					}
					interface J extends I {}
					"""
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81535
	public void test030() {
		java.util.Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);

		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.OutputStreamWriter;
					import java.io.PrintWriter;
					public class X extends PrintWriter implements Runnable {
						public X(OutputStreamWriter out, boolean flag) { super(out, flag); }
						public void run() {}
					}
					"""
			},
			"",
			null, // use default class-path
			false, // do not flush previous output dir content
			null, // no special vm args
			options,
			null
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80743
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X { long hashCode(); }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface X { long hashCode(); }
					              ^^^^
				The return type is incompatible with Object.hashCode()
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=113273
	public void test032() {
		// NOTE: javac only reports these errors when the problem type follows the bounds
		// if the type X is defined first, then no errors are reported
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I { Integer foo(); }
					interface J { Integer foo(); }
					public class X<T extends I&J> implements I {
						public Integer foo() { return null; }
					}"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=113273
	public void test032a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I { Float foo(); }
					interface J { Integer foo(); }
					public class X<T extends I&J> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public class X<T extends I&J> {}
					               ^
				The return types are incompatible for the inherited methods I.foo(), J.foo()
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=113273
	public void test032b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I { String foo(); }
					class A { public Object foo() { return null; } }
					public class X<T extends A&I> {}
					interface J extends I { Object foo(); }
					class Y<T extends I&J> {}
					class Z<T extends J&I> {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public class X<T extends A&I> {}
					               ^
				The return types are incompatible for the inherited methods I.foo(), A.foo()
				----------
				2. ERROR in X.java (at line 4)
					interface J extends I { Object foo(); }
					                        ^^^^^^
				The return type is incompatible with I.foo()
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80745
	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I { Number foo(); }
					interface J { Integer foo(); }
					public class X implements I, J {
						public Integer foo() {return 1;}
						public static void main(String argv[]) {
							I i = null;
							J j = null;
							System.out.print(i instanceof J);
							System.out.print('=');
							System.out.print(j instanceof I);
						}
					}
					"""
			},
			"false=false"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80745
	public void test033a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I { Number foo(A a); }
					interface J<T> { Integer foo(A<T> a); }
					class A<T>{}
					public class X implements I, J {
						public Integer foo(A a) {return 1;}
						public static void main(String argv[]) {
							I i = null;
							J j = null;
							System.out.print(i instanceof J);
							System.out.print('=');
							System.out.print(j instanceof I);
						}
					}
					"""
			},
			"false=false"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034() {
		this.runConformTest(
			new String[] {
				"B.java",
				"""
					interface I<E extends Comparable<E>> { void test(E element); }
					class A implements I<Integer> { public void test(Integer i) {} }
					public class B extends A { public void test(String i) {} }
					"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034a() {
		this.runConformTest(
			new String[] {
				"B.java",
				"""
					interface I<E extends Comparable> { void test(E element); }
					class A { public void test(Integer i) {} }
					public class B extends A implements I<Integer> {}
					class C extends B { public void test(Object i) {} }
					"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034b() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"""
					interface I<E extends Comparable> { void test(E element); }
					class A { public void test(Integer i) {} }
					public class B extends A implements I<Integer> { public void test(Comparable i) {} }
					"""
			},
			"""
				----------
				1. WARNING in B.java (at line 1)
					interface I<E extends Comparable> { void test(E element); }
					                      ^^^^^^^^^^
				Comparable is a raw type. References to generic type Comparable<T> should be parameterized
				----------
				2. ERROR in B.java (at line 3)
					public class B extends A implements I<Integer> { public void test(Comparable i) {} }
					                                                             ^^^^^^^^^^^^^^^^^^
				Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it
				----------
				3. WARNING in B.java (at line 3)
					public class B extends A implements I<Integer> { public void test(Comparable i) {} }
					                                                                  ^^^^^^^^^^
				Comparable is a raw type. References to generic type Comparable<T> should be parameterized
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034c() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"""
					interface I<E extends Comparable<E>> { void test(E element); }
					class A implements I<Integer> { public void test(Integer i) {} }
					public class B extends A { public void test(Comparable i) {} }
					"""
			},
			"""
				----------
				1. ERROR in B.java (at line 3)
					public class B extends A { public void test(Comparable i) {} }
					                                       ^^^^^^^^^^^^^^^^^^
				Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it
				----------
				2. WARNING in B.java (at line 3)
					public class B extends A { public void test(Comparable i) {} }
					                                            ^^^^^^^^^^
				Comparable is a raw type. References to generic type Comparable<T> should be parameterized
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034d() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"""
					abstract class AA<E extends Comparable> { abstract void test(E element); }
					class A extends AA<Integer> { @Override public void test(Integer i) {} }
					public class B extends A { public void test(Comparable i) {} }
					"""
			},
			"""
				----------
				1. WARNING in B.java (at line 1)
					abstract class AA<E extends Comparable> { abstract void test(E element); }
					                            ^^^^^^^^^^
				Comparable is a raw type. References to generic type Comparable<T> should be parameterized
				----------
				2. ERROR in B.java (at line 3)
					public class B extends A { public void test(Comparable i) {} }
					                                       ^^^^^^^^^^^^^^^^^^
				Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type AA<E> but does not override it
				----------
				3. WARNING in B.java (at line 3)
					public class B extends A { public void test(Comparable i) {} }
					                                            ^^^^^^^^^^
				Comparable is a raw type. References to generic type Comparable<T> should be parameterized
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80626
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"""
					interface I<U>{ int compareTo(U o); }
					abstract class F<T extends F<T>> implements I<T>{ public final int compareTo(T o) { return 0; } }
					public class E extends F<E> { public int compareTo(Object o) { return 0; } }
					"""
			},
			"""
				----------
				1. ERROR in E.java (at line 3)
					public class E extends F<E> { public int compareTo(Object o) { return 0; } }
					                                         ^^^^^^^^^^^^^^^^^^^
				Name clash: The method compareTo(Object) of type E has the same erasure as compareTo(U) of type I<U> but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80626
	public void test035a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public enum X {
						;
						public int compareTo(Object o) { return 0; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public int compareTo(Object o) { return 0; }
					           ^^^^^^^^^^^^^^^^^^^
				Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036() { // 2 interface cases
		// no bridge methods are created in these conform cases so no name clashes can occur
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X implements Equivalent, EqualityComparable {
						public boolean equalTo(Object other) { return true; }
					}
					abstract class Y implements Equivalent, EqualityComparable {}
					class Z extends Y {
						public boolean equalTo(Object other) { return true; }
					}
					interface Equivalent<T> { boolean equalTo(T other); }
					interface EqualityComparable<T> { boolean equalTo(T other); }
					"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036a() { // 2 interface cases
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X implements Equivalent, EqualityComparable {
						public boolean equalTo(Comparable other) { return true; }
						public boolean equalTo(Number other) { return true; }
					}
					abstract class Y implements Equivalent, EqualityComparable {}
					class Z extends Y {
						public boolean equalTo(Comparable other) { return true; }
						public boolean equalTo(Number other) { return true; }
					}
					interface Equivalent<T extends Comparable> { boolean equalTo(T other); }
					interface EqualityComparable<T extends Number> { boolean equalTo(T other); }
					"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036b() { // 2 interface cases
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X<S> implements Equivalent<S>, EqualityComparable<S> {
						public boolean equalTo(S other) { return true; }
					}
					abstract class Y<S> implements Equivalent<S>, EqualityComparable<S> {}
					class Z<U> extends Y<U> {
						public boolean equalTo(U other) { return true; }
					}
					interface Equivalent<T> { boolean equalTo(T other); }
					interface EqualityComparable<T> { boolean equalTo(T other); }
					"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036c() { // 2 interface cases
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {
						public boolean equalTo(T other) { return true; }
						public boolean equalTo(S other) { return true; }
					}
					abstract class Y<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {}
					class Z<U extends Comparable, V extends Number> extends Y<U, V> {
						public boolean equalTo(U other) { return true; }
						public boolean equalTo(V other) { return true; }
					}
					interface Equivalent<T extends Comparable> { boolean equalTo(T other); }
					interface EqualityComparable<S extends Number> { boolean equalTo(S other); }
					"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036d() { // 2 interface cases
		// in these cases, bridge methods are needed once abstract/concrete methods are defiined (either in the abstract class or a concrete subclass)
		if (this.complianceLevel < ClassFileConstants.JDK1_7) {
			this.runConformTest(
					new String[] {
							"Y.java",
							"""
								abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
									public abstract boolean equalTo(Number other);
								}
								interface Equivalent<T> { boolean equalTo(T other); }
								interface EqualityComparable<T> { boolean equalTo(T other); }
								"""
					},
					""
					// no bridge methods are created here since Y does not define an equalTo(?) method which equals an inherited equalTo method
					);
		} else {
			this.runNegativeTest(
					new String[] {
							"Y.java",
							"""
								abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
									public abstract boolean equalTo(Number other);
								}
								interface Equivalent<T> { boolean equalTo(T other); }
								interface EqualityComparable<T> { boolean equalTo(T other); }
								"""
					},
					"""
						----------
						1. ERROR in Y.java (at line 1)
							abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
							               ^
						Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it
						----------
						""");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036e() { // 2 interface cases
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"""
					abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
						public abstract boolean equalTo(Object other);
					}
					interface Equivalent<T> { boolean equalTo(T other); }
					interface EqualityComparable<T> { boolean equalTo(T other); }
					"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"""
				----------
				1. ERROR in Y.java (at line 2)
					public abstract boolean equalTo(Object other);
					                        ^^^^^^^^^^^^^^^^^^^^^
				Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it
				----------
				2. ERROR in Y.java (at line 2)
					public abstract boolean equalTo(Object other);
					                        ^^^^^^^^^^^^^^^^^^^^^
				Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it
				----------
				""" :
			"""
				----------
				1. ERROR in Y.java (at line 1)
					abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
					               ^
				Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it
				----------
				2. ERROR in Y.java (at line 2)
					public abstract boolean equalTo(Object other);
					                        ^^^^^^^^^^^^^^^^^^^^^
				Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it
				----------
				3. ERROR in Y.java (at line 2)
					public abstract boolean equalTo(Object other);
					                        ^^^^^^^^^^^^^^^^^^^^^
				Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036f() { // 2 interface cases
		// NOTE: javac has a bug, reverse the implemented interfaces & the name clash goes away
		// but eventually when a concrete subclass must define the remaining method, the error shows up
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"""
					abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
						public abstract boolean equalTo(String other);
					}
					interface Equivalent<T> { boolean equalTo(T other); }
					interface EqualityComparable<T> { boolean equalTo(T other); }
					"""
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {
					               ^
				Name clash: The method equalTo(T) of type Equivalent<T> has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036g() { // 2 interface cases
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"""
					abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {
						public boolean equalTo(Integer other) { return true; }
					}
					interface Equivalent<T> { boolean equalTo(T other); }
					interface EqualityComparable<T> { boolean equalTo(T other); }
					"""
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {
					               ^
				Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it
				----------
				"""
		);
	}

	public void test037() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X implements I, J { }
					abstract class Y implements J, I { }
					abstract class Z implements K { }
					class YYY implements J, I { public void foo(A a) {} }
					class XXX implements I, J { public void foo(A a) {} }
					class ZZZ implements K { public void foo(A a) {} }
					interface I { void foo(A a); }
					interface J { void foo(A<String> a); }
					interface K extends I { void foo(A<String> a); }
					class A<T> {}"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"""
				----------
				1. WARNING in X.java (at line 4)
					class YYY implements J, I { public void foo(A a) {} }
					                                            ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				2. WARNING in X.java (at line 5)
					class XXX implements I, J { public void foo(A a) {} }
					                                            ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				3. WARNING in X.java (at line 6)
					class ZZZ implements K { public void foo(A a) {} }
					                                         ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				4. WARNING in X.java (at line 7)
					interface I { void foo(A a); }
					                       ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				5. ERROR in X.java (at line 9)
					interface K extends I { void foo(A<String> a); }
					                             ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type K has the same erasure as foo(A) of type I but does not override it
				----------
				""" :
				"""
					----------
					1. WARNING in X.java (at line 4)
						class YYY implements J, I { public void foo(A a) {} }
						                                            ^
					A is a raw type. References to generic type A<T> should be parameterized
					----------
					2. WARNING in X.java (at line 5)
						class XXX implements I, J { public void foo(A a) {} }
						                                            ^
					A is a raw type. References to generic type A<T> should be parameterized
					----------
					3. WARNING in X.java (at line 6)
						class ZZZ implements K { public void foo(A a) {} }
						                                         ^
					A is a raw type. References to generic type A<T> should be parameterized
					----------
					4. WARNING in X.java (at line 7)
						interface I { void foo(A a); }
						                       ^
					A is a raw type. References to generic type A<T> should be parameterized
					----------
					5. ERROR in X.java (at line 9)
						interface K extends I { void foo(A<String> a); }
						                             ^^^^^^^^^^^^^^^^
					Name clash: The method foo(A<String>) of type K has the same erasure as foo(A) of type I but does not override it
					----------
					""");
	}
	public void test037a() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"""
					public abstract class XX implements I, J { public abstract void foo(A<String> a); }
					interface I { void foo(A a); }
					interface J { void foo(A<String> a); }
					class A<T> {}"""
			},
			"""
				----------
				1. ERROR in XX.java (at line 1)
					public abstract class XX implements I, J { public abstract void foo(A<String> a); }
					                                                                ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it
				----------
				2. WARNING in XX.java (at line 2)
					interface I { void foo(A a); }
					                       ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				"""
		);
	}
	public void test037b() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"""
					public class XX implements I, J { public void foo(A<String> a) {} }
					class YY implements J, I { public void foo(A<String> a) {} }
					class ZZ implements K { public void foo(A<String> a) {} }
					interface I { void foo(A a); }
					interface J { void foo(A<String> a); }
					interface K extends I { void foo(A<String> a); }
					class A<T> {}"""
			},
			"""
				----------
				1. ERROR in XX.java (at line 1)
					public class XX implements I, J { public void foo(A<String> a) {} }
					             ^^
				The type XX must implement the inherited abstract method I.foo(A)
				----------
				2. ERROR in XX.java (at line 1)
					public class XX implements I, J { public void foo(A<String> a) {} }
					                                              ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it
				----------
				3. ERROR in XX.java (at line 2)
					class YY implements J, I { public void foo(A<String> a) {} }
					      ^^
				The type YY must implement the inherited abstract method I.foo(A)
				----------
				4. ERROR in XX.java (at line 2)
					class YY implements J, I { public void foo(A<String> a) {} }
					                                       ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type YY has the same erasure as foo(A) of type I but does not override it
				----------
				5. ERROR in XX.java (at line 3)
					class ZZ implements K { public void foo(A<String> a) {} }
					      ^^
				The type ZZ must implement the inherited abstract method I.foo(A)
				----------
				6. ERROR in XX.java (at line 3)
					class ZZ implements K { public void foo(A<String> a) {} }
					                                    ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type ZZ has the same erasure as foo(A) of type I but does not override it
				----------
				7. WARNING in XX.java (at line 4)
					interface I { void foo(A a); }
					                       ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				8. ERROR in XX.java (at line 6)
					interface K extends I { void foo(A<String> a); }
					                             ^^^^^^^^^^^^^^^^
				Name clash: The method foo(A<String>) of type K has the same erasure as foo(A) of type I but does not override it
				----------
				"""
		);
	}
	public void test037c() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X extends Y implements I { }
					interface I { void foo(A a); }
					class Y { void foo(A<String> a) {} }
					class A<T> {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public abstract class X extends Y implements I { }
					                      ^
				Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it
				----------
				2. WARNING in X.java (at line 2)
					interface I { void foo(A a); }
					                       ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				"""
		);
	}
	public void test037d() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X extends Y implements I { }
					interface I { void foo(A<String> a); }
					class Y { void foo(A a) {} }
					class A<T> {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public abstract class X extends Y implements I { }
					                      ^
				The inherited method Y.foo(A) cannot hide the public abstract method in I
				----------
				2. WARNING in X.java (at line 3)
					class Y { void foo(A a) {} }
					                   ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				"""
		);
	}
	public void test037e() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X extends Y implements I { }
					interface I { <T, S> void foo(T t); }
					class Y { <T> void foo(T t) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public abstract class X extends Y implements I { }
					                      ^
				Name clash: The method foo(T) of type Y has the same erasure as foo(T) of type I but does not override it
				----------
				"""
		);
	}

	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends H<Object> { void foo(A<?> a) { super.foo(a); } }
					class H<T extends Object> { void foo(A<? extends T> a) {} }
					class A<T> {}"""
			},
			""
		);
	}
	public void test038a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X extends H<Number> { void foo(A<?> a) {} }
					class H<T extends Number> { void foo(A<? extends T> a) {} }
					class A<T> {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X extends H<Number> { void foo(A<?> a) {} }
					                                        ^^^^^^^^^^^
				Name clash: The method foo(A<?>) of type X has the same erasure as foo(A<? extends T>) of type H<T> but does not override it
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83573
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] args) {
					      Test test = new Test();
					      This test2 = new Test();
					      System.out.println(test.get());
					   }
					   interface This {
					      public Object get();
					   }
					\s
					   interface That extends This {
					      public String get();
					\s
					   }
					\s
					   static class Test implements That {
					\s
					      public String get() {
					         return "That";
					\s
					      }
					   }
					}
					"""
			},
			"That"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040() {
		this.runNegativeTest(
			new String[] {
				"Base.java",
				"interface Base<E> { Base<E> proc(); }\n" +
				"abstract class Derived<D> implements Base<D> { public abstract Derived<D> proc(); }\n"
			},
			"" // no warnings
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T extends Number> T test() { return null; } }\n" +
				"class B extends A { @Override Integer test() { return 1; } }\n"
			},
			"""
				----------
				1. WARNING in A.java (at line 2)
					class B extends A { @Override Integer test() { return 1; } }
					                              ^^^^^^^
				Type safety: The return type Integer for test() from the type B needs unchecked conversion to conform to T from the type A
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					import java.util.*;
					class A { List<String> getList() { return null; } }
					class B extends A { @Override List getList() { return null; } }
					"""
			},
			"""
				----------
				1. WARNING in A.java (at line 3)
					class B extends A { @Override List getList() { return null; } }
					                              ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in A.java (at line 3)
					class B extends A { @Override List getList() { return null; } }
					                              ^^^^
				Type safety: The return type List for getList() from the type B needs unchecked conversion to conform to List<String> from the type A
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X<T> { X<T> x(); }
					abstract class Y<S> implements X<S> { public abstract X x(); }
					abstract class Z implements X { public abstract X x(); }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					abstract class Y<S> implements X<S> { public abstract X x(); }
					                                                      ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				2. WARNING in X.java (at line 2)
					abstract class Y<S> implements X<S> { public abstract X x(); }
					                                                      ^
				Type safety: The return type X for x() from the type Y<S> needs unchecked conversion to conform to X<T> from the type X<T>
				----------
				3. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X x(); }
					                            ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				4. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X x(); }
					                                                ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X<T> { X<T>[] x(); }
					abstract class Y<S> implements X<S> { public abstract X[] x(); }
					abstract class Z implements X { public abstract X[] x(); }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					abstract class Y<S> implements X<S> { public abstract X[] x(); }
					                                                      ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				2. WARNING in X.java (at line 2)
					abstract class Y<S> implements X<S> { public abstract X[] x(); }
					                                                      ^^^
				Type safety: The return type X[] for x() from the type Y<S> needs unchecked conversion to conform to X<T>[] from the type X<T>
				----------
				3. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X[] x(); }
					                            ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				4. WARNING in X.java (at line 3)
					abstract class Z implements X { public abstract X[] x(); }
					                                                ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X { public void foo(String... n) {} }
					interface I { void foo(String[] n); }
					class Y extends X implements I { }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					class Y extends X implements I { }
					      ^
				Varargs methods should only override or be overridden by other varargs methods unlike X.foo(String...) and I.foo(String[])
				----------
				""",
			null,
			null,
			JavacTestOptions.EclipseJustification.EclipseBug83902
			// warning: foo(java.lang.String...) in X cannot implement foo(java.lang.String[]) in I; overridden method has no '...'
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041a() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X { public void foo(String[] n) {} }
					interface I { void foo(String... n); }
					class Y extends X implements I { }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					class Y extends X implements I { }
					      ^
				Varargs methods should only override or be overridden by other varargs methods unlike X.foo(String[]) and I.foo(String...)
				----------
				""",
			null,
			null,
			JavacTestOptions.EclipseJustification.EclipseBug83902
			// warning: foo(java.lang.String[]) in X cannot implement foo(java.lang.String...) in I; overriding method is missing '...'
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041b() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public Y foo() {
							System.out.println("SUCCESS");
							return null;
						}
						public static void main(String[] args) {
							((I) new Y()).foo();
						}
					}
					interface I { X foo(); }
					class Y extends X implements I { }
					"""
			},
			"SUCCESS"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041c() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X { public A foo() { return null; } }
					interface I { A<String> foo(); }
					class Y extends X implements I { }
					class A<T> { }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 1)
					public class X { public A foo() { return null; } }
					                        ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				2. WARNING in X.java (at line 3)
					class Y extends X implements I { }
					      ^
				Type safety: The return type A for foo() from the type X needs unchecked conversion to conform to A<String> from the type I
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041d() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X { public Object foo() { return null; } }
					interface I { <T> T foo(); }
					class Y extends X implements I { }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					class Y extends X implements I { }
					      ^
				Type safety: The return type Object for foo() from the type X needs unchecked conversion to conform to T from the type I
				----------
				""",
			null, null,
			JavacTestOptions.EclipseJustification.EclipseBug83902b
			// NOTE: javac issues an error & a warning which contradict each other
			// if the method Object foo() is implemented in Y then only the warning is issued, so X should be allowed to implement the method
			// Y is not abstract and does not override abstract method <T>foo() in I
			// warning: foo() in X implements <T>foo() in I; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85930
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface Callable<T>
					{
					    public enum Result { GOOD, BAD };
					    public Result call(T arg);
					}
					
					public class X implements Callable<String>
					{
					    public Result call(String arg) { return Result.GOOD; } // Warning line
					}
					"""
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X { void test(E<Integer,Integer> e) { e.id(Integer.valueOf(1)); } }
					abstract class C<A> { public abstract void id(A x); }
					interface I<B> { void id(B x); }
					abstract class E<A, B> extends C<A> implements I<B> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					abstract class E<A, B> extends C<A> implements I<B> {}
					               ^
				Name clash: The method id(A) of type C<A> has the same erasure as id(B) of type I<B> but does not override it
				----------
				""",
			JavacTestOptions.EclipseJustification.EclipseBug72704
			// javac won't report it until C.id() is made concrete or implemented in E
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	// variant where C and C.id are concrete
	public void test043_1() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X { void test(E<Integer,Integer> e) { e.id(Integer.valueOf(1)); } }
					class C<A> { public void id(A x) {} }
					interface I<B> { void id(B x); }
					abstract class E<A, B> extends C<A> implements I<B> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					abstract class E<A, B> extends C<A> implements I<B> {}
					               ^
				Name clash: The method id(A) of type C<A> has the same erasure as id(B) of type I<B> but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043a() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X { void test(E<Integer,Integer> e) { e.id(Integer.valueOf(2)); } }
					abstract class C<A extends Number> { public abstract void id(A x); }
					interface I<B> { void id(B x); }
					abstract class E<A extends Number, B> extends C<A> implements I<B> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X { void test(E<Integer,Integer> e) { e.id(Integer.valueOf(2)); } }
					                                                     ^^
				The method id(Integer) is ambiguous for the type E<Integer,Integer>
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043b() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X { void test(E<Integer,Integer> e) { e.id(Integer.valueOf(111)); } }
					abstract class C<A extends Number> { public void id(A x) {} }
					interface I<B> { void id(B x); }
					class E<A extends Number, B> extends C<A> implements I<B> { public void id(B b) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X { void test(E<Integer,Integer> e) { e.id(Integer.valueOf(111)); } }
					                                                     ^^
				The method id(Integer) is ambiguous for the type E<Integer,Integer>
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043c() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test(E<Integer,Integer> e) { e.id(Integer.valueOf(111)); }
						void test(M<Integer,Integer> m) {
							m.id(Integer.valueOf(111));
							((E<Integer, Integer>) m).id(Integer.valueOf(111));
						}
						void test(N<Integer> n) { n.id(Integer.valueOf(111)); }
					}
					abstract class C<A extends Number> { public void id(A x) {} }
					interface I<B> { void id(B x); }
					abstract class E<A extends Number, B> extends C<A> implements I<B> {}
					class M<A extends Number, B> extends E<A, B> { public void id(B b) {} }
					abstract class N<T extends Number> extends E<T, Number> { @Override public void id(T n) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					m.id(Integer.valueOf(111));
					  ^^
				The method id(Integer) is ambiguous for the type M<Integer,Integer>
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97161
	public void test043d() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p.Y.*;
					import static p.Z.*;
					public class X {
						Y data = null;
						public X() { foo(data.l); }
					}
					""",
				"p/Y.java",
				"""
					package p;
					import java.util.List;
					public class Y {
						List l = null;
						public static <T> void foo(T... e) {}
					}
					""",
				"p/Z.java",
				"""
					package p;
					import java.util.List;
					public class Z {
						public static <T> void foo(List<T>... e) {}
					}
					"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"""
				----------
				1. WARNING in p\\X.java (at line 6)
					public X() { foo(data.l); }
					             ^^^^^^^^^^^
				Type safety: A generic array of List<Object> is created for a varargs parameter
				----------
				2. WARNING in p\\X.java (at line 6)
					public X() { foo(data.l); }
					             ^^^^^^^^^^^
				Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Z
				----------
				3. WARNING in p\\X.java (at line 6)
					public X() { foo(data.l); }
					                 ^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<Object>
				----------
				----------
				1. WARNING in p\\Y.java (at line 4)
					List l = null;
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				""" :
				"""
					----------
					1. WARNING in p\\X.java (at line 6)
						public X() { foo(data.l); }
						             ^^^^^^^^^^^
					Type safety: A generic array of List<Object> is created for a varargs parameter
					----------
					2. WARNING in p\\X.java (at line 6)
						public X() { foo(data.l); }
						             ^^^^^^^^^^^
					Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Z
					----------
					3. WARNING in p\\X.java (at line 6)
						public X() { foo(data.l); }
						                 ^^^^^^
					Type safety: The expression of type List needs unchecked conversion to conform to List<Object>
					----------
					----------
					1. WARNING in p\\Y.java (at line 4)
						List l = null;
						^^^^
					List is a raw type. References to generic type List<E> should be parameterized
					----------
					2. WARNING in p\\Y.java (at line 5)
						public static <T> void foo(T... e) {}
						                                ^
					Type safety: Potential heap pollution via varargs parameter e
					----------
					----------
					1. WARNING in p\\Z.java (at line 4)
						public static <T> void foo(List<T>... e) {}
						                                      ^
					Type safety: Potential heap pollution via varargs parameter e
					----------
					"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97161
	public void test043e() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p.Y.*;
					public class X {
						Y data = null;
						public X() { foo(data.l); }
					}
					""",
				"p/Y.java",
				"""
					package p;
					import java.util.List;
					public class Y {
						List l = null;
						public static <T> void foo(T... e) {}
						public static <T> void foo(List<T>... e) {}
					}
					"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"""
				----------
				1. WARNING in p\\X.java (at line 5)
					public X() { foo(data.l); }
					             ^^^^^^^^^^^
				Type safety: A generic array of List<Object> is created for a varargs parameter
				----------
				2. WARNING in p\\X.java (at line 5)
					public X() { foo(data.l); }
					             ^^^^^^^^^^^
				Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Y
				----------
				3. WARNING in p\\X.java (at line 5)
					public X() { foo(data.l); }
					                 ^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<Object>
				----------
				----------
				1. WARNING in p\\Y.java (at line 4)
					List l = null;
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				""" :
				"""
					----------
					1. WARNING in p\\X.java (at line 5)
						public X() { foo(data.l); }
						             ^^^^^^^^^^^
					Type safety: A generic array of List<Object> is created for a varargs parameter
					----------
					2. WARNING in p\\X.java (at line 5)
						public X() { foo(data.l); }
						             ^^^^^^^^^^^
					Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Y
					----------
					3. WARNING in p\\X.java (at line 5)
						public X() { foo(data.l); }
						                 ^^^^^^
					Type safety: The expression of type List needs unchecked conversion to conform to List<Object>
					----------
					----------
					1. WARNING in p\\Y.java (at line 4)
						List l = null;
						^^^^
					List is a raw type. References to generic type List<E> should be parameterized
					----------
					2. WARNING in p\\Y.java (at line 5)
						public static <T> void foo(T... e) {}
						                                ^
					Type safety: Potential heap pollution via varargs parameter e
					----------
					3. WARNING in p\\Y.java (at line 6)
						public static <T> void foo(List<T>... e) {}
						                                      ^
					Type safety: Potential heap pollution via varargs parameter e
					----------
					"""
		);
	}

	public void test043f() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test(M<Integer,Integer> m) {
							m.id(new Integer(111), new Integer(112));
						}
					}
					abstract class C<T1 extends Number> { public <U1 extends Number> void id(T1 x, U1 u) {} }
					interface I<T2> { }
					abstract class E<T3 extends Number, T4> extends C<T3> implements I<T4> {}
					class M<T5 extends Number, T6> extends E<T5, T6> { public <U2 extends Number> void id(T5 b, U2 u) {} }
					"""
			},
			""
		);
	}
	public void test043g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test(M<Integer,Integer> m) {
							m.id(Integer.valueOf(111));
						}
					}
					abstract class C<T1 extends Number> { public void id(T1 x) {} }
					interface I<T2> { void id(T2 x); }
					abstract class E<T3 extends Number, T4> extends C<T3> implements I<T4> {}
					class M<T5 extends Number, T6> extends E<T5, T6> { public void id(T6 b) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					m.id(Integer.valueOf(111));
					  ^^
				The method id(Integer) is ambiguous for the type M<Integer,Integer>
				----------
				"""
		);
	}

	// ensure AccOverriding remains when attempting to override final method
	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { final void foo() {} }\n" +
				"class XS extends X { @Override void foo() {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class XS extends X { @Override void foo() {} }
					                                    ^^^^^
				Cannot override the final method from X
				----------
				"""
		);
	}
	// ensure AccOverriding remains when attempting to override final method
	public void test044a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public void foo() {} }\n" +
				"class XS extends X { @Override void foo() {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class XS extends X { @Override void foo() {} }
					                                    ^^^^^
				Cannot reduce the visibility of the inherited method from X
				----------
				"""
		);
	}
	// ensure AccOverriding remains when attempting to override final method
	public void test044b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() {} }\n" +
				"class XS extends X { @Override void foo() throws ClassNotFoundException {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class XS extends X { @Override void foo() throws ClassNotFoundException {} }
					                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Exception ClassNotFoundException is not compatible with throws clause in X.foo()
				----------
				"""
		);
	}
	// ensure AccOverriding remains when attempting to override final method
	public void test044c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() {} }\n" +
				"class XS extends X { @Override int foo() {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					class XS extends X { @Override int foo() {} }
					                               ^^^
				The return type is incompatible with X.foo()
				----------
				"""
		);
	}

	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class Foo {}
					
					interface Bar {
					  Foo get(Class<?> c);
					}
					public class X implements Bar {
					  public Foo get(Class c) { return null; }
					}
					"""
			},
			""
		);
	}

	// ensure no unchecked warning
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface IX <T> {
						public T doSomething();
					}
					public class X implements IX<Integer> {
					   Zork z;
						public Integer doSomething() {
							return null;
						}
					}
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87157
	public void test047() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface Interface {
					    Number getValue();
					}
					class C1 {
					    public Double getValue() {
					        return 0.0;
					    }
					}
					public class X extends C1 implements Interface{
					    public static void main(String[] args) {
					        Interface i=new X();
					        System.out.println(i.getValue());
					    }
					}
					"""
			},
		"0.0");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X1.java (at line 2)
						public class X1 extends LinkedHashMap<String, String> {
						             ^^
					The serializable class X1 does not declare a static final serialVersionUID field of type long
					----------
					2. WARNING in X1.java (at line 3)
						public Object putAll(Map<String,String> a) { return null; }
						              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Name clash: The method putAll(Map<String,String>) of type X1 has the same erasure as putAll(Map<? extends K,? extends V>) of type HashMap<K,V> but does not override it
					----------
					""":
					"""
						----------
						1. WARNING in X1.java (at line 2)
							public class X1 extends LinkedHashMap<String, String> {
							             ^^
						The serializable class X1 does not declare a static final serialVersionUID field of type long
						----------
						2. ERROR in X1.java (at line 3)
							public Object putAll(Map<String,String> a) { return null; }
							              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						Name clash: The method putAll(Map<String,String>) of type X1 has the same erasure as putAll(Map<? extends K,? extends V>) of type HashMap<K,V> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X1.java",
				"""
					import java.util.*;
					public class X1 extends LinkedHashMap<String, String> {
					    public Object putAll(Map<String,String> a) { return null; }
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: name clash: putAll(Map<String,String>) in X1 and putAll(Map<? extends K,? extends V>) in HashMap have the same erasure, yet neither overrides the other
        public Object putAll(Map<String,String> a) { return null; }
                      ^
  where K,V are type-variables:
    K extends Object declared in class HashMap
    V extends Object declared in class HashMap
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X2.java (at line 2)
						public Object foo(I<String> z) { return null; }
						              ^^^^^^^^^^^^^^^^
					Name clash: The method foo(I<String>) of type X2 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
					----------
					""":
					"""
						----------
						1. ERROR in X2.java (at line 2)
							public Object foo(I<String> z) { return null; }
							              ^^^^^^^^^^^^^^^^
						Name clash: The method foo(I<String>) of type X2 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X2.java",
				"""
					public class X2 extends Y<String> {
					    public Object foo(I<String> z) { return null; }
					}
					class Y<T> implements I<T> {
					    public void foo(I<? extends T> a) {}
					}
					interface I<T> {
					    public void foo(I<? extends T> a);
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X2 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public Object foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048b() {
		this.runNegativeTest(
			new String[] {
				"X3.java",
				"""
					public class X3 extends Y<String> {
					    public void foo(I<String> z) {}
					}
					class Y<T> implements I<T> {
					    public void foo(I<? extends T> a) {}
					}
					interface I<T> {
					    public void foo(I<? extends T> a);
					}
					"""
			},
			"""
				----------
				1. ERROR in X3.java (at line 2)
					public void foo(I<String> z) {}
					            ^^^^^^^^^^^^^^^^
				Name clash: The method foo(I<String>) of type X3 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
				----------
				"""
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X3 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public void foo(I<String> z) {}
                ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048c() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X4.java (at line 2)
						public String foo(I<String> z) { return null; }
						              ^^^^^^^^^^^^^^^^
					Name clash: The method foo(I<String>) of type X4 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
					----------
					""":
					"""
						----------
						1. ERROR in X4.java (at line 2)
							public String foo(I<String> z) { return null; }
							              ^^^^^^^^^^^^^^^^
						Name clash: The method foo(I<String>) of type X4 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X4.java",
				"""
					public class X4 extends Y<String> {
					    public String foo(I<String> z) { return null; }
					}
					class Y<T> implements I<T> {
					    public Object foo(I<? extends T> a) { return null; }
					}
					interface I<T> {
					    public Object foo(I<? extends T> a);
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X4 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public String foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048d() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X5.java (at line 2)
						public Object foo(I<String> z) { return null; }
						              ^^^^^^^^^^^^^^^^
					Name clash: The method foo(I<String>) of type X5 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
					----------
					""":
					"""
						----------
						1. ERROR in X5.java (at line 2)
							public Object foo(I<String> z) { return null; }
							              ^^^^^^^^^^^^^^^^
						Name clash: The method foo(I<String>) of type X5 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X5.java",
				"""
					public class X5 extends Y<String> {
					    public Object foo(I<String> z) { return null; }
					}
					class Y<T> implements I<T> {
					    public String foo(I<? extends T> a) { return null; }
					}
					interface I<T> {
					    public String foo(I<? extends T> a);
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X5 and foo(I<? extends T>) in Y have the
 same erasure, yet neither overrides the other
    public Object foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048e() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X6.java (at line 2)
						public void foo(I<String> z) {}
						            ^^^^^^^^^^^^^^^^
					Name clash: The method foo(I<String>) of type X6 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
					----------
					""":
					"""
						----------
						1. ERROR in X6.java (at line 2)
							public void foo(I<String> z) {}
							            ^^^^^^^^^^^^^^^^
						Name clash: The method foo(I<String>) of type X6 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X6.java",
				"""
					public class X6 extends Y<String> {
					    public void foo(I<String> z) {}
					}
					class Y<T> implements I<T> {
					    public Object foo(I<? extends T> a) { return null; }
					}
					interface I<T> {
					    public Object foo(I<? extends T> a);
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X6 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public void foo(I<String> z) {}
                ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048f() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X7.java (at line 2)
						public String foo(I<String> z) { return null; }
						              ^^^^^^^^^^^^^^^^
					Name clash: The method foo(I<String>) of type X7 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
					----------
					""":
					"""
						----------
						1. ERROR in X7.java (at line 2)
							public String foo(I<String> z) { return null; }
							              ^^^^^^^^^^^^^^^^
						Name clash: The method foo(I<String>) of type X7 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X7.java",
				"""
					public class X7 extends Y<String> {
					    public String foo(I<String> z) { return null; }
					}
					class Y<T> implements I<T> {
					    public T foo(I<? extends T> a) { return null; }
					}
					interface I<T> {
					    public T foo(I<? extends T> a);
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X7 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public String foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048g() {
		this.runNegativeTest(
			new String[] {
				"X8.java",
				"""
					public class X8 extends Y<String> {
					    public Object foo(I<String> z) { return null; }
					}
					class Y<T> implements I<T> {
					    public T foo(I<? extends T> a) { return null; }
					}
					interface I<T> {
					    public T foo(I<? extends T> a);
					}
					"""
			},
			"""
				----------
				1. ERROR in X8.java (at line 2)
					public Object foo(I<String> z) { return null; }
					              ^^^^^^^^^^^^^^^^
				Name clash: The method foo(I<String>) of type X8 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it
				----------
				"""
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X8 and foo(I<? extends T>) in Y have the  same erasure, yet neither overrides the other
    public Object foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88094
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						T id(T x) { return x; }
						A id(A x) { return x; }
					}
					class Y<T extends A> extends X<T> {
						@Override T id(T x) { return x; }
						@Override A id(A x) { return x; }
					}
					class A {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					@Override T id(T x) { return x; }
					            ^^^^^^^
				Erasure of method id(T) is the same as another method in type Y<T>
				----------
				2. ERROR in X.java (at line 6)
					@Override T id(T x) { return x; }
					            ^^^^^^^
				Name clash: The method id(T) of type Y<T> has the same erasure as id(A) of type X<T> but does not override it
				----------
				3. ERROR in X.java (at line 7)
					@Override A id(A x) { return x; }
					            ^^^^^^^
				Erasure of method id(A) is the same as another method in type Y<T>
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88094
	public void test049a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						T id(T x) { return x; }
						A id(A x) { return x; }
					}
					class Y<T extends A> extends X<T> {}
					class A {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					class Y<T extends A> extends X<T> {}
					      ^
				Duplicate methods named id with the parameters (A) and (T) are defined by the type X<T>
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94754
	public void test050() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						public static <S extends A> S foo() { System.out.print("A"); return null; }
						                              ^^^^^
					Duplicate method foo() in type X
					----------
					2. WARNING in X.java (at line 3)
						public static <N extends B> N foo() { System.out.print("B"); return null; }
						                              ^^^^^
					Duplicate method foo() in type X
					----------
					3. WARNING in X.java (at line 7)
						new X().<B>foo();
						^^^^^^^^^^^^^^^^
					The static method foo() from the type X should be accessed in a static way
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							public static <S extends A> S foo() { System.out.print("A"); return null; }
							                              ^^^^^
						Duplicate method foo() in type X
						----------
						2. ERROR in X.java (at line 3)
							public static <N extends B> N foo() { System.out.print("B"); return null; }
							                              ^^^^^
						Duplicate method foo() in type X
						----------
						3. ERROR in X.java (at line 6)
							X.<B>foo();
							     ^^^
						Bound mismatch: The generic method foo() of type X is not applicable for the arguments (). The inferred type B is not a valid substitute for the bounded parameter <S extends A>
						----------
						4. ERROR in X.java (at line 7)
							new X().<B>foo();
							           ^^^
						Bound mismatch: The generic method foo() of type X is not applicable for the arguments (). The inferred type B is not a valid substitute for the bounded parameter <S extends A>
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static <S extends A> S foo() { System.out.print("A"); return null; }
						public static <N extends B> N foo() { System.out.print("B"); return null; }
						public static void main(String[] args) {
							X.<A>foo();
							X.<B>foo();
							new X().<B>foo();
						}
					}
					class A {}
					class B {}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N>foo() and <S>foo() have the same erasure
        public static <N extends B> N foo() { System.out.print("B"); return null; }
                                      ^
  where N,S are type-variables:
    N extends B declared in method <N>foo()
    S extends A declared in method <S>foo()
X.java:6: method foo in class X cannot be applied to given types
                X.<B>foo();
                 ^
  required: no arguments
  found: no arguments
X.java:7: method foo in class X cannot be applied to given types
                new X().<B>foo();
                       ^
  required: no arguments
  found: no arguments
3 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94754
	public void test050a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						public static <S extends A> void foo() { System.out.print("A"); }
						                                 ^^^^^
					Duplicate method foo() in type X
					----------
					2. WARNING in X.java (at line 3)
						public static <N extends B> N foo() { System.out.print("B"); return null; }
						                              ^^^^^
					Duplicate method foo() in type X
					----------
					3. ERROR in X.java (at line 5)
						X.foo();
						  ^^^
					The method foo() is ambiguous for the type X
					----------
					4. ERROR in X.java (at line 6)
						foo();
						^^^
					The method foo() is ambiguous for the type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							public static <S extends A> void foo() { System.out.print("A"); }
							                                 ^^^^^
						Duplicate method foo() in type X
						----------
						2. ERROR in X.java (at line 3)
							public static <N extends B> N foo() { System.out.print("B"); return null; }
							                              ^^^^^
						Duplicate method foo() in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static <S extends A> void foo() { System.out.print("A"); }
						public static <N extends B> N foo() { System.out.print("B"); return null; }
						static void test () {
							X.foo();
							foo();
						}
					}
					class A {}
					class B {}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N>foo() and <S>foo() have the same erasure
        public static <N extends B> N foo() { System.out.print("B"); return null; }
                                      ^
  where N,S are type-variables:
    N extends B declared in method <N>foo()
    S extends A declared in method <S>foo()
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. ERROR in X.java (at line 3)
						Y foo(Object o) {  return null; } // duplicate
						  ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C1
					----------
					2. ERROR in X.java (at line 4)
						Z foo(Object o) {  return null; } // duplicate
						  ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C1
					----------
					3. WARNING in X.java (at line 7)
						<T extends Y> T foo(Object o) {  return null; } // duplicate
						                ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C2
					----------
					4. WARNING in X.java (at line 8)
						<T extends Z> T foo(Object o) {  return null; } // duplicate
						                ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C2
					----------
					5. ERROR in X.java (at line 11)
						A<Y> foo(Object o) {  return null; } // duplicate
						     ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C3
					----------
					6. ERROR in X.java (at line 12)
						A<Z> foo(Object o) {  return null; } // duplicate
						     ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C3
					----------
					7. ERROR in X.java (at line 15)
						Y foo(Object o) {  return null; } // duplicate
						  ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C4
					----------
					8. ERROR in X.java (at line 16)
						<T extends Z> T foo(Object o) {  return null; } // duplicate
						                ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C4
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 3)
							Y foo(Object o) {  return null; } // duplicate
							  ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C1
						----------
						2. ERROR in X.java (at line 4)
							Z foo(Object o) {  return null; } // duplicate
							  ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C1
						----------
						3. ERROR in X.java (at line 7)
							<T extends Y> T foo(Object o) {  return null; } // duplicate
							                ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C2
						----------
						4. ERROR in X.java (at line 8)
							<T extends Z> T foo(Object o) {  return null; } // duplicate
							                ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C2
						----------
						5. ERROR in X.java (at line 11)
							A<Y> foo(Object o) {  return null; } // duplicate
							     ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C3
						----------
						6. ERROR in X.java (at line 12)
							A<Z> foo(Object o) {  return null; } // duplicate
							     ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C3
						----------
						7. ERROR in X.java (at line 15)
							Y foo(Object o) {  return null; } // duplicate
							  ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C4
						----------
						8. ERROR in X.java (at line 16)
							<T extends Z> T foo(Object o) {  return null; } // duplicate
							                ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C4
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						class C1 {
							Y foo(Object o) {  return null; } // duplicate
							Z foo(Object o) {  return null; } // duplicate
						}
						class C2 {
							<T extends Y> T foo(Object o) {  return null; } // duplicate
							<T extends Z> T foo(Object o) {  return null; } // duplicate
						}
						class C3 {
							A<Y> foo(Object o) {  return null; } // duplicate
							A<Z> foo(Object o) {  return null; } // duplicate
						}
						class C4 {
							Y foo(Object o) {  return null; } // duplicate
							<T extends Z> T foo(Object o) {  return null; } // duplicate
						}
					}
					class A<T> {}
					class Y {}
					class Z {}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: foo(Object) is already defined in X.C1
                Z foo(Object o) {  return null; } // duplicate
                  ^
X.java:8: name clash: <T#1>foo(Object) and <T#2>foo(Object) have the same erasure
                <T extends Z> T foo(Object o) {  return null; } // duplicate
                                ^
  where T#1,T#2 are type-variables:
    T#1 extends Z declared in method <T#1>foo(Object)
    T#2 extends Y declared in method <T#2>foo(Object)
X.java:12: foo(Object) is already defined in X.C3
                A<Z> foo(Object o) {  return null; } // duplicate
                     ^
X.java:16: foo(Object) is already defined in X.C4
                <T extends Z> T foo(Object o) {  return null; } // duplicate
                                ^
4 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050c() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. ERROR in X.java (at line 3)
						A<Y> foo(A<Y> o) {  return null; } // duplicate
						     ^^^^^^^^^^^
					Erasure of method foo(A<Y>) is the same as another method in type X.C5
					----------
					2. ERROR in X.java (at line 4)
						A<Z> foo(A<Z> o) {  return null; } // duplicate
						     ^^^^^^^^^^^
					Erasure of method foo(A<Z>) is the same as another method in type X.C5
					----------
					3. WARNING in X.java (at line 7)
						<T extends Y> T foo(A<Y> o) {  return null; } // ok
						                ^^^^^^^^^^^
					Erasure of method foo(A<Y>) is the same as another method in type X.C6
					----------
					4. WARNING in X.java (at line 8)
						<T extends Z> T foo(A<Z> o) {  return null; } // ok
						                ^^^^^^^^^^^
					Erasure of method foo(A<Z>) is the same as another method in type X.C6
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 3)
							A<Y> foo(A<Y> o) {  return null; } // duplicate
							     ^^^^^^^^^^^
						Erasure of method foo(A<Y>) is the same as another method in type X.C5
						----------
						2. ERROR in X.java (at line 4)
							A<Z> foo(A<Z> o) {  return null; } // duplicate
							     ^^^^^^^^^^^
						Erasure of method foo(A<Z>) is the same as another method in type X.C5
						----------
						3. ERROR in X.java (at line 7)
							<T extends Y> T foo(A<Y> o) {  return null; } // ok
							                ^^^^^^^^^^^
						Erasure of method foo(A<Y>) is the same as another method in type X.C6
						----------
						4. ERROR in X.java (at line 8)
							<T extends Z> T foo(A<Z> o) {  return null; } // ok
							                ^^^^^^^^^^^
						Erasure of method foo(A<Z>) is the same as another method in type X.C6
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						class C5 {
							A<Y> foo(A<Y> o) {  return null; } // duplicate
							A<Z> foo(A<Z> o) {  return null; } // duplicate
						}
						class C6 {
							<T extends Y> T foo(A<Y> o) {  return null; } // ok
							<T extends Z> T foo(A<Z> o) {  return null; } // ok
						}
					}
					class A<T> {}
					class Y {}
					class Z {}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: name clash: foo(A<Z>) and foo(A<Y>) have the same erasure
                A<Z> foo(A<Z> o) {  return null; } // duplicate
                     ^
X.java:8: name clash: <T#1>foo(A<Z>) and <T#2>foo(A<Y>) have the same erasure
                <T extends Z> T foo(A<Z> o) {  return null; } // ok
                                ^
  where T#1,T#2 are type-variables:
    T#1 extends Z declared in method <T#1>foo(A<Z>)
    T#2 extends Y declared in method <T#2>foo(A<Y>)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050d() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 3)
						<T extends Y, U> T foo(Object o) {  return null; } // ok
						                   ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C7
					----------
					2. WARNING in X.java (at line 4)
						<T extends Z> T foo(Object o) {  return null; } // ok
						                ^^^^^^^^^^^^^
					Duplicate method foo(Object) in type X.C7
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 3)
							<T extends Y, U> T foo(Object o) {  return null; } // ok
							                   ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C7
						----------
						2. ERROR in X.java (at line 4)
							<T extends Z> T foo(Object o) {  return null; } // ok
							                ^^^^^^^^^^^^^
						Duplicate method foo(Object) in type X.C7
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						class C7 {
							<T extends Y, U> T foo(Object o) {  return null; } // ok
							<T extends Z> T foo(Object o) {  return null; } // ok
						}
					}
					class A<T> {}
					class Y {}
					class Z {}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: name clash: <T#1>foo(Object) and <T#2,U>foo(Object) have the same erasure
                <T extends Z> T foo(Object o) {  return null; } // ok
                                ^
  where T#1,T#2,U are type-variables:
    T#1 extends Z declared in method <T#1>foo(Object)
    T#2 extends Y declared in method <T#2,U>foo(Object)
    U extends Object declared in method <T#2,U>foo(Object)
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050e() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<N extends B> N a(A<String> s) { return null; }
						                ^^^^^^^^^^^^^^
					Erasure of method a(A<String>) is the same as another method in type X
					----------
					2. WARNING in X.java (at line 3)
						<N> Object a(A<Number> n) { return null; }
						           ^^^^^^^^^^^^^^
					Erasure of method a(A<Number>) is the same as another method in type X
					----------
					3. WARNING in X.java (at line 4)
						<N extends B> void b(A<String> s) {}
						                   ^^^^^^^^^^^^^^
					Erasure of method b(A<String>) is the same as another method in type X
					----------
					4. WARNING in X.java (at line 5)
						<N extends B> B b(A<Number> n) { return null; }
						                ^^^^^^^^^^^^^^
					Erasure of method b(A<Number>) is the same as another method in type X
					----------
					5. WARNING in X.java (at line 6)
						void c(A<String> s) {}
						     ^^^^^^^^^^^^^^
					Erasure of method c(A<String>) is the same as another method in type X
					----------
					6. WARNING in X.java (at line 7)
						B c(A<Number> n) { return null; }
						  ^^^^^^^^^^^^^^
					Erasure of method c(A<Number>) is the same as another method in type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							<N extends B> N a(A<String> s) { return null; }
							                ^^^^^^^^^^^^^^
						Erasure of method a(A<String>) is the same as another method in type X
						----------
						2. ERROR in X.java (at line 3)
							<N> Object a(A<Number> n) { return null; }
							           ^^^^^^^^^^^^^^
						Erasure of method a(A<Number>) is the same as another method in type X
						----------
						3. ERROR in X.java (at line 4)
							<N extends B> void b(A<String> s) {}
							                   ^^^^^^^^^^^^^^
						Erasure of method b(A<String>) is the same as another method in type X
						----------
						4. ERROR in X.java (at line 5)
							<N extends B> B b(A<Number> n) { return null; }
							                ^^^^^^^^^^^^^^
						Erasure of method b(A<Number>) is the same as another method in type X
						----------
						5. ERROR in X.java (at line 6)
							void c(A<String> s) {}
							     ^^^^^^^^^^^^^^
						Erasure of method c(A<String>) is the same as another method in type X
						----------
						6. ERROR in X.java (at line 7)
							B c(A<Number> n) { return null; }
							  ^^^^^^^^^^^^^^
						Erasure of method c(A<Number>) is the same as another method in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <N extends B> N a(A<String> s) { return null; }
							 <N> Object a(A<Number> n) { return null; }
							 <N extends B> void b(A<String> s) {}
							 <N extends B> B b(A<Number> n) { return null; }
							 void c(A<String> s) {}
							 B c(A<Number> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<String>) have the same erasure
        <N> Object a(A<Number> n) { return null; }
                   ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<String>)
X.java:5: name clash: <N#1>b(A<Number>) and <N#2>b(A<String>) have the same erasure
        <N extends B> B b(A<Number> n) { return null; }
                        ^
  where N#1,N#2 are type-variables:
    N#1 extends B declared in method <N#1>b(A<Number>)
    N#2 extends B declared in method <N#2>b(A<String>)
X.java:7: name clash: c(A<Number>) and c(A<String>) have the same erasure
        B c(A<Number> n) { return null; }
          ^
3 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <N extends B> N a(A<String> s) { return null; }
							 <N> B a(A<Number> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					<N extends B> N a(A<String> s) { return null; }
					                ^^^^^^^^^^^^^^
				Erasure of method a(A<String>) is the same as another method in type X
				----------
				2. ERROR in X.java (at line 3)
					<N> B a(A<Number> n) { return null; }
					      ^^^^^^^^^^^^^^
				Erasure of method a(A<Number>) is the same as another method in type X
				----------
				"""
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<String>) have the same erasure
        <N> B a(A<Number> n) { return null; }
              ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<String>)
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <N extends B> N b(A<String> s) { return null; }
							 <N extends B> B b(A<Number> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					<N extends B> N b(A<String> s) { return null; }
					                ^^^^^^^^^^^^^^
				Erasure of method b(A<String>) is the same as another method in type X
				----------
				2. ERROR in X.java (at line 3)
					<N extends B> B b(A<Number> n) { return null; }
					                ^^^^^^^^^^^^^^
				Erasure of method b(A<Number>) is the same as another method in type X
				----------
				"""
		);
/* javac 7
X.java:3: name clash: <N#1>b(A<Number>) and <N#2>b(A<String>) have the same erasure
        <N extends B> B b(A<Number> n) { return null; }
                        ^
  where N#1,N#2 are type-variables:
    N#1 extends B declared in method <N#1>b(A<Number>)
    N#2 extends B declared in method <N#2>b(A<String>)
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050h() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 B c(A<String> s) { return null; }
							 B c(A<Number> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					B c(A<String> s) { return null; }
					  ^^^^^^^^^^^^^^
				Erasure of method c(A<String>) is the same as another method in type X
				----------
				2. ERROR in X.java (at line 3)
					B c(A<Number> n) { return null; }
					  ^^^^^^^^^^^^^^
				Erasure of method c(A<Number>) is the same as another method in type X
				----------
				"""
		);
/* javac 7
X.java:3: name clash: c(A<Number>) and c(A<String>) have the same erasure
        B c(A<Number> n) { return null; }
          ^
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050i() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<N extends B> N a(A<Number> s) { return null; }
						                ^^^^^^^^^^^^^^
					Duplicate method a(A<Number>) in type X
					----------
					2. WARNING in X.java (at line 3)
						<N> Object a(A<Number> n) { return null; }
						           ^^^^^^^^^^^^^^
					Duplicate method a(A<Number>) in type X
					----------
					3. WARNING in X.java (at line 4)
						<N extends B> N b(A<Number> s) { return null; }
						                ^^^^^^^^^^^^^^
					Erasure of method b(A<Number>) is the same as another method in type X
					----------
					4. WARNING in X.java (at line 5)
						<N> Object b(A<String> n) { return null; }
						           ^^^^^^^^^^^^^^
					Erasure of method b(A<String>) is the same as another method in type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							<N extends B> N a(A<Number> s) { return null; }
							                ^^^^^^^^^^^^^^
						Duplicate method a(A<Number>) in type X
						----------
						2. ERROR in X.java (at line 3)
							<N> Object a(A<Number> n) { return null; }
							           ^^^^^^^^^^^^^^
						Duplicate method a(A<Number>) in type X
						----------
						3. ERROR in X.java (at line 4)
							<N extends B> N b(A<Number> s) { return null; }
							                ^^^^^^^^^^^^^^
						Erasure of method b(A<Number>) is the same as another method in type X
						----------
						4. ERROR in X.java (at line 5)
							<N> Object b(A<String> n) { return null; }
							           ^^^^^^^^^^^^^^
						Erasure of method b(A<String>) is the same as another method in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <N extends B> N a(A<Number> s) { return null; }
							 <N> Object a(A<Number> n) { return null; }
							 <N extends B> N b(A<Number> s) { return null; }
							 <N> Object b(A<String> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<Number>) have the same erasure
        <N> Object a(A<Number> n) { return null; }
                   ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<Number>)
X.java:5: name clash: <N#1>b(A<String>) and <N#2>b(A<Number>) have the same erasure
        <N> Object b(A<String> n) { return null; }
                   ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>b(A<String>)
    N#2 extends B declared in method <N#2>b(A<Number>)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050j() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <N extends B> N a(A<Number> s) { return null; }
							 <N> B a(A<Number> n) { return null; }
							 <N extends B> N b(A<Number> s) { return null; }
							 <N> B b(A<String> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					<N extends B> N a(A<Number> s) { return null; }
					                ^^^^^^^^^^^^^^
				Duplicate method a(A<Number>) in type X
				----------
				2. ERROR in X.java (at line 3)
					<N> B a(A<Number> n) { return null; }
					      ^^^^^^^^^^^^^^
				Duplicate method a(A<Number>) in type X
				----------
				3. ERROR in X.java (at line 4)
					<N extends B> N b(A<Number> s) { return null; }
					                ^^^^^^^^^^^^^^
				Erasure of method b(A<Number>) is the same as another method in type X
				----------
				4. ERROR in X.java (at line 5)
					<N> B b(A<String> n) { return null; }
					      ^^^^^^^^^^^^^^
				Erasure of method b(A<String>) is the same as another method in type X
				----------
				"""
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<Number>) have the same erasure
        <N> B a(A<Number> n) { return null; }
              ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<Number>)
X.java:5: name clash: <N#1>b(A<String>) and <N#2>b(A<Number>) have the same erasure
        <N> B b(A<String> n) { return null; }
              ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>b(A<String>)
    N#2 extends B declared in method <N#2>b(A<Number>)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050k() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <N extends B> void a(A<Number> s) {}
							 <N extends B> B a(A<Number> n) { return null; }
							 <N extends B> Object b(A<Number> s) { return null; }
							 <N extends B> B b(A<Number> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					<N extends B> void a(A<Number> s) {}
					                   ^^^^^^^^^^^^^^
				Duplicate method a(A<Number>) in type X
				----------
				2. ERROR in X.java (at line 3)
					<N extends B> B a(A<Number> n) { return null; }
					                ^^^^^^^^^^^^^^
				Duplicate method a(A<Number>) in type X
				----------
				3. ERROR in X.java (at line 4)
					<N extends B> Object b(A<Number> s) { return null; }
					                     ^^^^^^^^^^^^^^
				Duplicate method b(A<Number>) in type X
				----------
				4. ERROR in X.java (at line 5)
					<N extends B> B b(A<Number> n) { return null; }
					                ^^^^^^^^^^^^^^
				Duplicate method b(A<Number>) in type X
				----------
				"""
		);
/* javac 7
X.java:3: <N>a(A<Number>) is already defined in X
                <N extends B> B a(A<Number> n) { return null; }
                                ^
  where N is a type-variable:
    N extends B declared in method <N>a(A<Number>)
X.java:5: <N>b(A<Number>) is already defined in X
                <N extends B> B b(A<Number> n) { return null; }
                                ^
  where N is a type-variable:
    N extends B declared in method <N>b(A<Number>)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050l() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 void a(A<Number> s) {}
							 B a(A<Number> n) { return null; }
							 Object b(A<Number> s) {}
							 B b(A<Number> n) { return null; }
					}
					class A<T> {}
					class B {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void a(A<Number> s) {}
					     ^^^^^^^^^^^^^^
				Duplicate method a(A<Number>) in type X
				----------
				2. ERROR in X.java (at line 3)
					B a(A<Number> n) { return null; }
					  ^^^^^^^^^^^^^^
				Duplicate method a(A<Number>) in type X
				----------
				3. ERROR in X.java (at line 4)
					Object b(A<Number> s) {}
					       ^^^^^^^^^^^^^^
				Duplicate method b(A<Number>) in type X
				----------
				4. ERROR in X.java (at line 4)
					Object b(A<Number> s) {}
					       ^^^^^^^^^^^^^^
				This method must return a result of type Object
				----------
				5. ERROR in X.java (at line 5)
					B b(A<Number> n) { return null; }
					  ^^^^^^^^^^^^^^
				Duplicate method b(A<Number>) in type X
				----------
				"""
		);
/* javac 7
X.java:3: a(A<Number>) is already defined in X
                 B a(A<Number> n) { return null; }
                   ^
X.java:5: b(A<Number>) is already defined in X
                 B b(A<Number> n) { return null; }
                   ^
2 errors
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X implements I {
							 public <T extends I> void foo(T t) {}
					}
					interface I {
							 <T> void foo(T t);
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X implements I {
					             ^
				The type X must implement the inherited abstract method I.foo(T)
				----------
				"""
		);
/* javac 7
X.java:1: X is not abstract and does not override abstract method <T>foo(T) in I
class X implements I {
^
  where T is a type-variable:
    T extends Object declared in method <T>foo(T)
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(A<String> a) {}
						void foo(A<Integer> a) {}
					}
					class A<T> {}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(A<String> a) {}
					     ^^^^^^^^^^^^^^^^
				Erasure of method foo(A<String>) is the same as another method in type X
				----------
				2. ERROR in X.java (at line 3)
					void foo(A<Integer> a) {}
					     ^^^^^^^^^^^^^^^^^
				Erasure of method foo(A<Integer>) is the same as another method in type X
				----------
				"""
		);
/* javac 7
X.java:3: name clash: foo(A<Integer>) and foo(A<String>) have the same erasure
        void foo(A<Integer> a) {}
             ^
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						void foo(A<String> a) {}
						     ^^^^^^^^^^^^^^^^
					Erasure of method foo(A<String>) is the same as another method in type X
					----------
					2. WARNING in X.java (at line 3)
						Object foo(A<Integer> a) { return null; }
						       ^^^^^^^^^^^^^^^^^
					Erasure of method foo(A<Integer>) is the same as another method in type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							void foo(A<String> a) {}
							     ^^^^^^^^^^^^^^^^
						Erasure of method foo(A<String>) is the same as another method in type X
						----------
						2. ERROR in X.java (at line 3)
							Object foo(A<Integer> a) { return null; }
							       ^^^^^^^^^^^^^^^^^
						Erasure of method foo(A<Integer>) is the same as another method in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(A<String> a) {}
						Object foo(A<Integer> a) { return null; }
					}
					class A<T> {}
					""",
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: foo(A<Integer>) and foo(A<String>) have the same erasure
        Object foo(A<Integer> a) { return null; }
               ^
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void test052() {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public class A<T> {
						public A test() { return null; }
						public A<T> test2() { return null; }
						public A<X> test3() { return null; }
						public <U> A<U> test4() { return null; }
					}
					class B extends A<X> {
						@Override public B test() { return null; }
						@Override public B test2() { return null; }
						@Override public B test3() { return null; }
						@Override public <U> A<U> test4() { return null; }
					}
					class X{}
					"""
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void test052a() {
		this.runNegativeTest(
				new String[] {
					"A.java",
					"""
						public class A<T> {
							public <U> A<U> test() { return null; }
							public <U> A<U> test2() { return null; }
							public <U> A<U> test3() { return null; }
						}
						class B extends A<X> {
							@Override public B test() { return null; }
							@Override public A test2() { return null; }
							@Override public A<X> test3() { return null; }
						}
						class X{}
						"""
				},
				"""
					----------
					1. WARNING in A.java (at line 7)
						@Override public B test() { return null; }
						                 ^
					Type safety: The return type B for test() from the type B needs unchecked conversion to conform to A<Object> from the type A<X>
					----------
					2. WARNING in A.java (at line 8)
						@Override public A test2() { return null; }
						                 ^
					A is a raw type. References to generic type A<T> should be parameterized
					----------
					3. WARNING in A.java (at line 8)
						@Override public A test2() { return null; }
						                 ^
					Type safety: The return type A for test2() from the type B needs unchecked conversion to conform to A<U> from the type A<T>
					----------
					4. WARNING in A.java (at line 9)
						@Override public A<X> test3() { return null; }
						                 ^
					Type safety: The return type A<X> for test3() from the type B needs unchecked conversion to conform to A<Object> from the type A<X>
					----------
					"""
			);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void test053() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
						void test(A a) { B b = a.foo(); }
						void test2(A<X> a) { B b = a.foo(); }
						void test3(B b) { B bb = b.foo(); }
					}
					class A<T> {
						<U> A<U> foo() { return null; }
					}
					class B extends A<X> {
						@Override B foo() { return null; }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					void test(A a) { B b = a.foo(); }
					          ^
				A is a raw type. References to generic type A<T> should be parameterized
				----------
				2. ERROR in X.java (at line 2)
					void test(A a) { B b = a.foo(); }
					                       ^^^^^^^
				Type mismatch: cannot convert from A to B
				----------
				3. ERROR in X.java (at line 3)
					void test2(A<X> a) { B b = a.foo(); }
					                           ^^^^^^^
				Type mismatch: cannot convert from A<Object> to B
				----------
				4. WARNING in X.java (at line 10)
					@Override B foo() { return null; }
					          ^
				Type safety: The return type B for foo() from the type B needs unchecked conversion to conform to A<Object> from the type A<X>
				----------
				"""
		);
	}

	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void a(Object x) {}
						<T> T a(T x) {  return null; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void a(Object x) {}
					     ^^^^^^^^^^^
				Erasure of method a(Object) is the same as another method in type X
				----------
				2. ERROR in X.java (at line 3)
					<T> T a(T x) {  return null; }
					      ^^^^^^
				Erasure of method a(T) is the same as another method in type X
				----------
				"""
		);
/* javac 7
X.java:3: a(Object) is already defined in X
        <T> T a(T x) {  return null; }
              ^
1 error
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<T1, T2> String aaa(X x) {  return null; }
						                ^^^^^^^^
					Erasure of method aaa(X) is the same as another method in type X
					----------
					2. WARNING in X.java (at line 3)
						<T extends X> T aaa(T x) {  return null; }
						                ^^^^^^^^
					Erasure of method aaa(T) is the same as another method in type X
					----------
					3. WARNING in X.java (at line 4)
						<T> String aa(X x) {  return null; }
						           ^^^^^^^
					Erasure of method aa(X) is the same as another method in type X
					----------
					4. WARNING in X.java (at line 5)
						<T extends X> T aa(T x) {  return null; }
						                ^^^^^^^
					Erasure of method aa(T) is the same as another method in type X
					----------
					5. ERROR in X.java (at line 6)
						String a(X x) {  return null; }
						       ^^^^^^
					Erasure of method a(X) is the same as another method in type X
					----------
					6. ERROR in X.java (at line 7)
						<T extends X> T a(T x) {  return null; }
						                ^^^^^^
					Erasure of method a(T) is the same as another method in type X
					----------
					7. WARNING in X.java (at line 8)
						<T> String z(X x) { return null; }
						           ^^^^^^
					Duplicate method z(X) in type X
					----------
					8. WARNING in X.java (at line 9)
						<T, S> Object z(X x) { return null; }
						              ^^^^^^
					Duplicate method z(X) in type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							<T1, T2> String aaa(X x) {  return null; }
							                ^^^^^^^^
						Erasure of method aaa(X) is the same as another method in type X
						----------
						2. ERROR in X.java (at line 3)
							<T extends X> T aaa(T x) {  return null; }
							                ^^^^^^^^
						Erasure of method aaa(T) is the same as another method in type X
						----------
						3. ERROR in X.java (at line 4)
							<T> String aa(X x) {  return null; }
							           ^^^^^^^
						Erasure of method aa(X) is the same as another method in type X
						----------
						4. ERROR in X.java (at line 5)
							<T extends X> T aa(T x) {  return null; }
							                ^^^^^^^
						Erasure of method aa(T) is the same as another method in type X
						----------
						5. ERROR in X.java (at line 6)
							String a(X x) {  return null; }
							       ^^^^^^
						Erasure of method a(X) is the same as another method in type X
						----------
						6. ERROR in X.java (at line 7)
							<T extends X> T a(T x) {  return null; }
							                ^^^^^^
						Erasure of method a(T) is the same as another method in type X
						----------
						7. ERROR in X.java (at line 8)
							<T> String z(X x) { return null; }
							           ^^^^^^
						Duplicate method z(X) in type X
						----------
						8. ERROR in X.java (at line 9)
							<T, S> Object z(X x) { return null; }
							              ^^^^^^
						Duplicate method z(X) in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<T1, T2> String aaa(X x) {  return null; }
						<T extends X> T aaa(T x) {  return null; }
						<T> String aa(X x) {  return null; }
						<T extends X> T aa(T x) {  return null; }
						String a(X x) {  return null; }
						<T extends X> T a(T x) {  return null; }
						<T> String z(X x) { return null; }
						<T, S> Object z(X x) { return null; }
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T>aaa(T) and <T1,T2>aaa(X) have the same erasure
        <T extends X> T aaa(T x) {  return null; }
                        ^
  where T,T1,T2 are type-variables:
    T extends X declared in method <T>aaa(T)
    T1 extends Object declared in method <T1,T2>aaa(X)
    T2 extends Object declared in method <T1,T2>aaa(X)
X.java:5: name clash: <T#1>aa(T#1) and <T#2>aa(X) have the same erasure
        <T extends X> T aa(T x) {  return null; }
                        ^
  where T#1,T#2 are type-variables:
    T#1 extends X declared in method <T#1>aa(T#1)
    T#2 extends Object declared in method <T#2>aa(X)
X.java:7: a(X) is already defined in X
        <T extends X> T a(T x) {  return null; }
                        ^
X.java:9: name clash: <T#1,S>z(X) and <T#3>z(X) have the same erasure
        <T, S> Object z(X x) { return null; }
                      ^
  where T#1,S,T#3 are type-variables:
    T#1 extends Object declared in method <T#1,S>z(X)
    S extends Object declared in method <T#1,S>z(X)
    T#3 extends Object declared in method <T#3>z(X)
4 errors
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						Object foo(X<T> t) { return null; }
						       ^^^^^^^^^^^
					Duplicate method foo(X<T>) in type X<T>
					----------
					2. WARNING in X.java (at line 3)
						<S> String foo(X<T> s) { return null; }
						           ^^^^^^^^^^^
					Duplicate method foo(X<T>) in type X<T>
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							Object foo(X<T> t) { return null; }
							       ^^^^^^^^^^^
						Duplicate method foo(X<T>) in type X<T>
						----------
						2. ERROR in X.java (at line 3)
							<S> String foo(X<T> s) { return null; }
							           ^^^^^^^^^^^
						Duplicate method foo(X<T>) in type X<T>
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
							 Object foo(X<T> t) { return null; }
							 <S> String foo(X<T> s) { return null; }
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <S>foo(X<T>) and foo(X<T>) have the same erasure
        <S> String foo(X<T> s) { return null; }
                   ^
  where S,T are type-variables:
    S extends Object declared in method <S>foo(X<T>)
    T extends Object declared in class X
1 error
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
							<T1 extends X<T1>> void dupT() {}
							<T2 extends X<T2>> Object dupT() {return null;}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					<T1 extends X<T1>> void dupT() {}
					                        ^^^^^^
				Duplicate method dupT() in type X<T>
				----------
				2. ERROR in X.java (at line 3)
					<T2 extends X<T2>> Object dupT() {return null;}
					                          ^^^^^^
				Duplicate method dupT() in type X<T>
				----------
				"""
		);
/* javac 7
X.java:3: <T1>dupT() is already defined in X
        <T2 extends X<T2>> Object dupT() {return null;}
                                  ^
  where T1 is a type-variable:
    T1 extends X<T1> declared in method <T1>dupT()
1 error
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054d() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<T> T a(A<T> t) {return null;}
						      ^^^^^^^^^
					Erasure of method a(A<T>) is the same as another method in type X
					----------
					2. WARNING in X.java (at line 3)
						<T> String a(A<Object> o) {return null;}
						           ^^^^^^^^^^^^^^
					Erasure of method a(A<Object>) is the same as another method in type X
					----------
					3. WARNING in X.java (at line 4)
						<T> T aa(A<T> t) {return null;}
						      ^^^^^^^^^^
					Erasure of method aa(A<T>) is the same as another method in type X
					----------
					4. WARNING in X.java (at line 5)
						String aa(A<Object> o) {return null;}
						       ^^^^^^^^^^^^^^^
					Erasure of method aa(A<Object>) is the same as another method in type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							<T> T a(A<T> t) {return null;}
							      ^^^^^^^^^
						Erasure of method a(A<T>) is the same as another method in type X
						----------
						2. ERROR in X.java (at line 3)
							<T> String a(A<Object> o) {return null;}
							           ^^^^^^^^^^^^^^
						Erasure of method a(A<Object>) is the same as another method in type X
						----------
						3. ERROR in X.java (at line 4)
							<T> T aa(A<T> t) {return null;}
							      ^^^^^^^^^^
						Erasure of method aa(A<T>) is the same as another method in type X
						----------
						4. ERROR in X.java (at line 5)
							String aa(A<Object> o) {return null;}
							       ^^^^^^^^^^^^^^^
						Erasure of method aa(A<Object>) is the same as another method in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<T> T a(A<T> t) {return null;}
						<T> String a(A<Object> o) {return null;}
						<T> T aa(A<T> t) {return null;}
						String aa(A<Object> o) {return null;}
					}
					class A<T> {}
					""",
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T#1>a(A<Object>) and <T#2>a(A<T#2>) have the same erasure

        <T> String a(A<Object> o) {return null;}
                   ^
  where T#1,T#2 are type-variables:
    T#1 extends Object declared in method <T#1>a(A<Object>)
    T#2 extends Object declared in method <T#2>a(A<T#2>)
X.java:5: name clash: aa(A<Object>) and <T>aa(A<T>) have the same erasure
        String aa(A<Object> o) {return null;}
               ^
  where T is a type-variable:
    T extends Object declared in method <T>aa(A<T>)
2 errors
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95933
	public void test055() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							A a = new C();
							try { a.f(new Object()); } catch (ClassCastException e) {
								System.out.println(1);
							}
						}
					}
					interface A<T> { void f(T x); }
					interface B extends A<String> { void f(String x); }
					class C implements B { public void f(String x) {} }
					"""
			},
			"1"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test056() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static String bind(String message, Object binding) { return null; }
					   public static String bind(String message, Object[] bindings) { return null; }
					}
					class Y extends X {
					   public static String bind(String message, Object binding) { return null; }
					   public static String bind(String message, Object[] bindings) { return null; }
					}
					class Z {
					   void bar() { Y.bind("", new String[] {""}); }
					}
					"""
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84035
	public void test057() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] args) {
					   	A<Integer> x = new A<Integer>();
					   	B<Integer> y = new B<Integer>();
					   	new X().print(x);
					   	new X().print(y);
						}
						public <T extends IA<?>> void print(T a) { System.out.print(1); }
						public <T extends IB<?>> void print(T a) { System.out.print(2); }
					}
					interface IA<E> {}
					interface IB<E> extends IA<E> {}
					class A<E> implements IA<E> {}
					class B<E> implements IB<E> {}
					"""
			},
			"12");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84035
	public void test057a() {
		this.runConformTest(
			new String[] {
				"XX.java",
				"""
					public class XX {
					   public static void main(String[] args) {
					   	A<Integer> x = new A<Integer>();
					   	B<Integer> y = new B<Integer>();
					   	print(x);
					   	print(y);
						}
						public static <T extends IA<?>> void print(T a) { System.out.print(3); }
						public static <T extends IB<?>> void print(T a) { System.out.print(4); }
					}
					interface IA<E> {}
					interface IB<E> extends IA<E> {}
					class A<E> implements IA<E> {}
					class B<E> implements IB<E> {}
					"""
			},
			"34");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X <B extends Number> {
					   public static void main(String[] args) {
					   	X<Integer> x = new X<Integer>();
					   	x.aaa(null);
					   	x.aaa(15);
						}
						<T> T aaa(T t) { System.out.print('T'); return null; }
						void aaa(B b) { System.out.print('B'); }
					}
					"""
			},
			"BB");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. ERROR in X.java (at line 3)
						new X<Object>().foo("X");
						                ^^^
					The method foo(String) is ambiguous for the type X<Object>
					----------
					2. ERROR in X.java (at line 4)
						new X<Object>().foo2("X");
						                ^^^^
					The method foo2(String) is ambiguous for the type X<Object>
					----------
					3. WARNING in X.java (at line 6)
						<T> T foo(T t) {return null;}
						      ^^^^^^^^
					Erasure of method foo(T) is the same as another method in type X<A>
					----------
					4. WARNING in X.java (at line 7)
						void foo(A a) {}
						     ^^^^^^^^
					Erasure of method foo(A) is the same as another method in type X<A>
					----------
					5. WARNING in X.java (at line 8)
						<T> T foo2(T t) {return null;}
						      ^^^^^^^^^
					Erasure of method foo2(T) is the same as another method in type X<A>
					----------
					6. WARNING in X.java (at line 9)
						<T> void foo2(A a) {}
						         ^^^^^^^^^
					Erasure of method foo2(A) is the same as another method in type X<A>
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 6)
							<T> T foo(T t) {return null;}
							      ^^^^^^^^
						Erasure of method foo(T) is the same as another method in type X<A>
						----------
						2. ERROR in X.java (at line 7)
							void foo(A a) {}
							     ^^^^^^^^
						Erasure of method foo(A) is the same as another method in type X<A>
						----------
						3. ERROR in X.java (at line 8)
							<T> T foo2(T t) {return null;}
							      ^^^^^^^^^
						Erasure of method foo2(T) is the same as another method in type X<A>
						----------
						4. ERROR in X.java (at line 9)
							<T> void foo2(A a) {}
							         ^^^^^^^^^
						Erasure of method foo2(A) is the same as another method in type X<A>
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<A> {
						void test() {
							new X<Object>().foo("X");
							new X<Object>().foo2("X");
						}
						<T> T foo(T t) {return null;}
						void foo(A a) {}
						<T> T foo2(T t) {return null;}
						<T> void foo2(A a) {}
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:7: name clash: foo(A) and <T>foo(T) have the same erasure
        void foo(A a) {}
             ^
  where A,T are type-variables:
    A extends Object declared in class X
    T extends Object declared in method <T>foo(T)
X.java:9: name clash: <T#1>foo2(A) and <T#3>foo2(T#3) have the same erasure
        <T> void foo2(A a) {}
                 ^
  where T#1,A,T#3 are type-variables:
    T#1 extends Object declared in method <T#1>foo2(A)
    A extends Object declared in class X
    T#3 extends Object declared in method <T#3>foo2(T#3)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. ERROR in X.java (at line 3)
						new X<Object>().foo("X");
						                ^^^
					The method foo(String) is ambiguous for the type X<Object>
					----------
					2. ERROR in X.java (at line 4)
						new X<Object>().foo2("X");
						                ^^^^
					The method foo2(String) is ambiguous for the type X<Object>
					----------
					3. WARNING in X.java (at line 6)
						<T> T foo(T t) {return null;}
						      ^^^^^^^^
					Name clash: The method foo(T) of type X<A> has the same erasure as foo(A) of type Y<A> but does not override it
					----------
					4. WARNING in X.java (at line 7)
						<T> T foo2(T t) {return null;}
						      ^^^^^^^^^
					Name clash: The method foo2(T) of type X<A> has the same erasure as foo2(A) of type Y<A> but does not override it
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 3)
							new X<Object>().foo("X");
							                ^^^
						The method foo(String) is ambiguous for the type X<Object>
						----------
						2. ERROR in X.java (at line 4)
							new X<Object>().foo2("X");
							                ^^^^
						The method foo2(String) is ambiguous for the type X<Object>
						----------
						3. ERROR in X.java (at line 6)
							<T> T foo(T t) {return null;}
							      ^^^^^^^^
						Name clash: The method foo(T) of type X<A> has the same erasure as foo(A) of type Y<A> but does not override it
						----------
						4. ERROR in X.java (at line 7)
							<T> T foo2(T t) {return null;}
							      ^^^^^^^^^
						Name clash: The method foo2(T) of type X<A> has the same erasure as foo2(A) of type Y<A> but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<A> extends Y<A> {
						void test() {
							new X<Object>().foo("X");
							new X<Object>().foo2("X");
						}
						<T> T foo(T t) {return null;}
						<T> T foo2(T t) {return null;}
					}
					class Y<A> {
						void foo(A a) {}
						<T> void foo2(A a) {}
					}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: reference to foo is ambiguous, both method foo(A) in Y and method <T>foo(T) in X match
                new X<Object>().foo("X");
                               ^
  where A,T are type-variables:
    A extends Object declared in class Y
    T extends Object declared in method <T>foo(T)
X.java:4: reference to foo2 is ambiguous, both method <T#1>foo2(A) in Y and method <T#3>foo2(T#3) in X match
                new X<Object>().foo2("X");
                               ^
  where T#1,A,T#3 are type-variables:
    T#1 extends Object declared in method <T#1>foo2(A)
    A extends Object declared in class Y
    T#3 extends Object declared in method <T#3>foo2(T#3)
X.java:6: name clash: <T>foo(T) in X and foo(A) in Y have the same erasure, yet neither overrides the other
        <T> T foo(T t) {return null;}
              ^
  where T,A are type-variables:
    T extends Object declared in method <T>foo(T)
    A extends Object declared in class Y
X.java:7: name clash: <T#1>foo2(T#1) in X and <T#2>foo2(A) in Y have the same erasure, yet neither overrides the other
        <T> T foo2(T t) {return null;}
              ^
  where T#1,T#2,A are type-variables:
    T#1 extends Object declared in method <T#1>foo2(T#1)
    T#2 extends Object declared in method <T#2>foo2(A)
    A extends Object declared in class Y
4 errors
 */
	}

	public void test059() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {new B().foo("aa");}
					}
					class A { <U> void foo(U u) {System.out.print(false);} }
					class B extends A { <V> void foo(String s) {System.out.print(true);} }
					"""
			},
			"true");
	}
	public void test059a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {new B().foo("aa");}
					}
					class A { <U> void foo(String s) {System.out.print(true);} }
					class B extends A { <V> void foo(V v) {System.out.print(false);} }
					"""
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060() {
		this.runConformTest(
			new String[] {
				"I.java",
				"""
					import java.util.Iterator;
					public interface I {
						void method(Iterator<Object> iter);
						public static class TestClass implements I {
							public void method(Iterator iter) {}
						}
					}"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060b() {
		this.runConformTest(
			new String[] {
				"I2.java",
				"""
					import java.util.Iterator;
					public interface I2 {
						void method(Iterator<Object>[] iter);
						public static class TestClass implements I2 {
							public void method(Iterator[] iter) {}
						}
					}"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060c() {
		this.runNegativeTest(
			new String[] {
				"I3.java",
				"""
					import java.util.Iterator;
					public interface I3 {
						void method(Iterator<Object>[] iter);
						public static class TestClass implements I3 {
							public void method(Iterator[][] iter) {}
						}
					}"""
			},
			"""
				----------
				1. ERROR in I3.java (at line 4)
					public static class TestClass implements I3 {
					                    ^^^^^^^^^
				The type I3.TestClass must implement the inherited abstract method I3.method(Iterator<Object>[])
				----------
				2. WARNING in I3.java (at line 5)
					public void method(Iterator[][] iter) {}
					                   ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99106
	public void test061() {
		this.runNegativeTest(
			new String[] {
				"Try.java",
				"""
					public class Try {
						public static void main(String[] args) {
							Ex<String> ex = new Ex<String>();
							ex.one("eclipse", Integer.valueOf(1));
							ex.two(Integer.valueOf(1));
							ex.three("eclipse");
							ex.four("eclipse");
							System.out.print(',');
							Ex ex2 = ex;
							ex2.one("eclipse", Integer.valueOf(1));
							ex2.two(Integer.valueOf(1));
							ex2.three("eclipse");
							ex2.four("eclipse");
						}
					}
					class Top<TC> {
						<TM> void one(TC cTop, TM mTop) { System.out.print(-1); }
						<TM> void two(TM mTop) { System.out.print(-2); }
						void three(TC cTop) { System.out.print(-3); }
						<TM> void four(TC cTop) { System.out.print(-4); }
					}
					class Ex<C> extends Top<C> {
						@Override <M> void one(C cEx, M mEx) { System.out.print(1); }
						@Override <M> void two(M mEx) { System.out.print(2); }
						@Override void three(C cEx) { System.out.print(3); }
						@Override <M> void four(C cEx) { System.out.print(4); }
					}"""
			},
			"""
				----------
				1. WARNING in Try.java (at line 9)
					Ex ex2 = ex;
					^^
				Ex is a raw type. References to generic type Ex<C> should be parameterized
				----------
				2. WARNING in Try.java (at line 10)
					ex2.one("eclipse", Integer.valueOf(1));
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method one(Object, Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized
				----------
				3. WARNING in Try.java (at line 11)
					ex2.two(Integer.valueOf(1));
					^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method two(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized
				----------
				4. WARNING in Try.java (at line 12)
					ex2.three("eclipse");
					^^^^^^^^^^^^^^^^^^^^
				Type safety: The method three(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized
				----------
				5. WARNING in Try.java (at line 13)
					ex2.four("eclipse");
					^^^^^^^^^^^^^^^^^^^
				Type safety: The method four(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99106
	public void test062() {
		this.runNegativeTest(
			new String[] {
				"Errors.java",
				"""
					public class Errors {
						void foo() {
							Ex<String> ex = new Ex<String>();
							ex.proof("eclipse");
							ex.five("eclipse");
							ex.six("eclipse");
							Ex ex2 = ex;
							ex2.proof("eclipse");
							ex2.five("eclipse");
							ex2.six("eclipse");
						}
					}
					class Top<TC> {
						<TM> void proof(Object cTop) {}
						<TM> void five(TC cTop) {}
						void six(TC cTop) {}
					}
					class Ex<C> extends Top<C> {
						@Override void proof(Object cTop) {}
						@Override void five(C cEx) {}
						@Override <M> void six(C cEx) {}
					}"""
			},
			"----------\n" +
			"1. ERROR in Errors.java (at line 5)\n" +
			"	ex.five(\"eclipse\");\n" +
			"	   ^^^^\n" +
			"The method five(String) is ambiguous for the type Ex<String>\n" +
			"----------\n" +
			"2. ERROR in Errors.java (at line 6)\n" +
			"	ex.six(\"eclipse\");\n" +
			"	   ^^^\n" +
			"The method six(String) is ambiguous for the type Ex<String>\n" +
			"----------\n" +
			"3. WARNING in Errors.java (at line 7)\n" +
			"	Ex ex2 = ex;\n" +
			"	^^\n" +
			"Ex is a raw type. References to generic type Ex<C> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in Errors.java (at line 9)\n" +
			"	ex2.five(\"eclipse\");\n" +
			"	    ^^^^\n" +
			"The method five(Object) is ambiguous for the type Ex\n" +
			"----------\n" +
			"5. ERROR in Errors.java (at line 10)\n" +
			"	ex2.six(\"eclipse\");\n" +
			"	    ^^^\n" +
			"The method six(Object) is ambiguous for the type Ex\n" +
			"----------\n" +
			"6. ERROR in Errors.java (at line 20)\n" +
			"	@Override void five(C cEx) {}\n" +
			"	               ^^^^^^^^^^^\n" +
			"Name clash: The method five(C) of type Ex<C> has the same erasure as five(TC) of type Top<TC> but does not override it\n" +
			"----------\n" +
			"7. ERROR in Errors.java (at line 20)\n" +
			"	@Override void five(C cEx) {}\n" +
			"	               ^^^^^^^^^^^\n" +
			mustOverrideMessage("five(C)", "Ex<C>") +
			"----------\n" +
			"8. ERROR in Errors.java (at line 21)\n" +
			"	@Override <M> void six(C cEx) {}\n" +
			"	                   ^^^^^^^^^^\n" +
			"Name clash: The method six(C) of type Ex<C> has the same erasure as six(TC) of type Top<TC> but does not override it\n" +
			"----------\n" +
			"9. ERROR in Errors.java (at line 21)\n" +
			"	@Override <M> void six(C cEx) {}\n" +
			"	                   ^^^^^^^^^^\n" +
			mustOverrideMessage("six(C)", "Ex<C>") +
			"----------\n"
			// 5: reference to five is ambiguous, both method <TM>five(TC) in Top<java.lang.String> and method five(C) in Ex<java.lang.String> match
			// 6: reference to six is ambiguous, both method six(TC) in Top<java.lang.String> and method <M>six(C) in Ex<java.lang.String> match
			// 9: reference to five is ambiguous, both method <TM>five(TC) in Top and method five(C) in Ex match
			// **** 9: warning: [unchecked] unchecked call to <TM>five(TC) as a member of the raw type Top
			// 10: reference to six is ambiguous, both method six(TC) in Top and method <M>six(C) in Ex match
			// **** 10: warning: [unchecked] unchecked call to six(TC) as a member of the raw type Top
			// 20: method does not override a method from its superclass
			// 21: method does not override a method from its superclass
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551
	public void test063() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface IStructuredContentProvider<I, E extends I> {
					    public E[] getElements(I inputElement);
					    public E[] getChildren(E parent);
					}
					
					public class X implements IStructuredContentProvider {
					// eclipse error: The type X must implement the inherited
					// abstract method IStructuredContentProvider.getChildren(I)
					
					    public Object[] getElements(Object inputElement) {
					        // eclipse error: The return type is incompatible with
					        // IStructuredContentProvider.getElements(Object)
					        return null;
					    }
					
					    public Object[] getChildren(Object parent) {
					        // eclipse error: Name clash: The method getChildren(Object) of type
					        // X has the same erasure as getChildren(E) of type
					        // IStructuredContentProvider<I,E> but does not override it
					        return null;
					    }
					}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551 - variation
	public void test064() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface IStructuredContentProvider<I, E extends I> {
					    public E[] getElements(I inputElement);
					    public E[] getChildren(E parent);
					}
					
					public class X implements IStructuredContentProvider<Object,Object> {
					// eclipse error: The type X must implement the inherited
					// abstract method IStructuredContentProvider.getChildren(I)
					
					    public Object[] getElements(Object inputElement) {
					        // eclipse error: The return type is incompatible with
					        // IStructuredContentProvider.getElements(Object)
					        return null;
					    }
					
					    public Object[] getChildren(Object parent) {
					        // eclipse error: Name clash: The method getChildren(Object) of type
					        // X has the same erasure as getChildren(E) of type
					        // IStructuredContentProvider<I,E> but does not override it
					        return null;
					    }
					}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551 - variation
	public void test065() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					
					interface IStructuredContentProvider<I, E extends List<String>> {
					    public E[] getElements(I inputElement);
					    public E[] getChildren(E parent);
					}
					
					public class X implements IStructuredContentProvider {
					// eclipse error: The type X must implement the inherited
					// abstract method IStructuredContentProvider.getChildren(I)
					
					    public List[] getElements(Object inputElement) {
					        // eclipse error: The return type is incompatible with
					        // IStructuredContentProvider.getElements(Object)
					        return null;
					    }
					
					    public List[] getChildren(List parent) {
					        // eclipse error: Name clash: The method getChildren(Object) of type
					        // X has the same erasure as getChildren(E) of type
					        // IStructuredContentProvider<I,E> but does not override it
					        return null;
					    }
					}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=103849
	public void test066() {
		this.runConformTest(
			new String[] {
				"JukeboxImpl.java",
				"""
					public class JukeboxImpl implements Jukebox {
					    public <M extends Music,A extends Artist<M>> A getArtist (M music){return null;}
					    void test () { getArtist(new Rock()); }
					}
					interface Jukebox {
						<M extends Music, A extends Artist<M>> A getArtist (M music);
					}
					interface Music {}
					class Rock implements Music {}
					interface Artist<M extends Music> {}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107098
	public void test067() {
		this.runConformTest(
			new String[] {
				"NoErrors.java",
				"""
					public class NoErrors {
					    public static void main(String[] args) { new B().foo2(1, 10); }
					}
					class A<T> {
						<S1 extends T> void foo2(Number t, S1 s) { System.out.print(false); }
					}
					class B extends A<Number> {
						<S2 extends Number> void foo2(Number t, S2 s) { System.out.print(true); }
					}
					"""
			},
			"true");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107681
	public void test068() {
		this.runConformTest(
			new String[] {
				"ReflectionNavigator.java",
				"""
					import java.lang.reflect.Type;
					public class ReflectionNavigator implements Navigator<Type> {
					    public <T> Class<T> erasure(Type t) { return null; }
					}
					interface Navigator<TypeT> {
						<T> TypeT erasure(TypeT x);
					}
					class Usage {
						public void foo(ReflectionNavigator r, Type t) { r.erasure(t); }
					}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108203
	public void test069() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Type;
					public class X implements I<A> {
					    public <N extends A> void x1() {}
					    public <N extends Number> void x2() {}
					    public <N extends Number> void x3() {}
					}
					interface I<V> {
						<N extends V> void x1();
						<N extends String> void x2();
						<N extends Object> void x3();
					}
					class A {}
					class B<T> {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public class X implements I<A> {
					             ^
				The type X must implement the inherited abstract method I<A>.x3()
				----------
				2. ERROR in X.java (at line 2)
					public class X implements I<A> {
					             ^
				The type X must implement the inherited abstract method I<A>.x2()
				----------
				3. ERROR in X.java (at line 4)
					public <N extends Number> void x2() {}
					                               ^^^^
				Name clash: The method x2() of type X has the same erasure as x2() of type I<V> but does not override it
				----------
				4. ERROR in X.java (at line 5)
					public <N extends Number> void x3() {}
					                               ^^^^
				Name clash: The method x3() of type X has the same erasure as x3() of type I<V> but does not override it
				----------
				5. WARNING in X.java (at line 9)
					<N extends String> void x2();
					           ^^^^^^
				The type parameter N should not be bounded by the final type String. Final types cannot be further extended
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101049
	public void test070() {
		Map<String,String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
		this.runConformTest(
			true,
			new String[] {
				"BooleanFactory.java",
				"""
					interface Factory<T> {
						<U extends T> U create(Class<U> c);
					}
					public class BooleanFactory implements Factory<Boolean> {
						public <U extends Boolean> U create(Class<U> c) {
							try { return c.newInstance(); } catch(Exception e) { return null; }
						}
					}
					"""
			}, null, options,
			"""
				----------
				1. WARNING in BooleanFactory.java (at line 5)
					public <U extends Boolean> U create(Class<U> c) {
					                  ^^^^^^^
				The type parameter U should not be bounded by the final type Boolean. Final types cannot be further extended
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107045
	public void test071() {
		this.runNegativeTest(
			new String[] {
				"D.java",
				"""
					class D extends B<Integer> {
						@Override void m(Number t) {}
						@Override void m(Integer t) {}
					}
					class A<T extends Number> { void m(T t) {} }
					class B<S extends Integer> extends A<S> { @Override void m(S t) {} }"""
			},
			"----------\n" +
			"1. ERROR in D.java (at line 2)\n" +
			"	@Override void m(Number t) {}\n" +
			"	               ^^^^^^^^^^^\n" +
			"Name clash: The method m(Number) of type D has the same erasure as m(T) of type A<T> but does not override it\n" +
			"----------\n" +
			"2. ERROR in D.java (at line 2)\n" +
			"	@Override void m(Number t) {}\n" +
			"	               ^^^^^^^^^^^\n" +
			mustOverrideMessage("m(Number)", "D") +
			"----------\n" +
			"3. WARNING in D.java (at line 6)\n" +
			"	class B<S extends Integer> extends A<S> { @Override void m(S t) {} }\n" +
			"	                  ^^^^^^^\n" +
			"The type parameter S should not be bounded by the final type Integer. Final types cannot be further extended\n" +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108780
	public void test072() {
		this.runConformTest(
			new String[] {
				"B.java",
				"""
					class A<E> { E foo(E e) { return null; } }
					class B<T> extends A<T> {
						@Override T foo(Object arg0) { return null; }
					}"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073() {
		this.runConformTest(
			new String[] {
				"NumericArray.java",
				"""
					class Array<T> {
						public void add(T t) { System.out.println(false); }
					}
					public class NumericArray<T extends Number> extends Array<T> {
						public static void main(String[] s) { new NumericArray<Integer>().add(1); }
						@Override public void add(Number n) { System.out.println(true); }
					}"""
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073a() {
		this.runConformTest(
			new String[] {
				"NumericArray2.java",
				"""
					class Array<T> {
						public T add(T t) { System.out.println(false); return null; }
					}
					public class NumericArray2<T extends Number> extends Array<T> {
						public static void main(String[] s) { new NumericArray2<Integer>().add(1); }
						@Override public T add(Number n) { System.out.println(true); return null; }
					}"""
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073b() {
		this.runConformTest(
			new String[] {
				"NumericArray3.java",
				"""
					class Array<T> {
						public <U extends Number> void add(U u) {}
					}
					public class NumericArray3<T extends Number> extends Array<T> {
						public static void main(String[] s) { new NumericArray3<Integer>().add(1); }
						@Override public void add(Number n) { System.out.println(true); }
					}"""
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073c() {
		this.runNegativeTest(
			new String[] {
				"NumericArray4.java",
				"""
					class Array<T> {
						public <U> void add(T t) {}
					}
					public class NumericArray4<T extends Number> extends Array<T> {
						@Override public <U> void add(Number n) {}
					}"""
			},
			"----------\n" +
			"1. ERROR in NumericArray4.java (at line 5)\n" +
			"	@Override public <U> void add(Number n) {}\n" +
			"	                          ^^^^^^^^^^^^^\n" +
			mustOverrideMessage("add(Number)", "NumericArray4<T>") +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073d() {
		this.runNegativeTest(
			new String[] {
				"NumericArray5.java",
				"""
					class Array<T> {
						public <U> void add(T t, U u) {}
					}
					public class NumericArray5<T extends Number> extends Array<T> {
						@Override public void add(Number n, Integer i) {}
					}"""
			},
			"----------\n" +
			"1. ERROR in NumericArray5.java (at line 5)\n" +
			"	@Override public void add(Number n, Integer i) {}\n" +
			"	                      ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			mustOverrideMessage("add(Number, Integer)", "NumericArray5<T>") +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100970
	public void test074() {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I {}\n" +
				"interface J extends I { @Override void clone(); }"
			},
			"----------\n" +
			"1. WARNING in I.java (at line 2)\n" +
			"	interface J extends I { @Override void clone(); }\n" +
			"	                                  ^^^^\n" +
			"The return type is incompatible with Object.clone(), thus this interface cannot be implemented\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 2)\n" +
			"	interface J extends I { @Override void clone(); }\n" +
			"	                                       ^^^^^^^\n" +
			mustOverrideMessage("clone()", "J") +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100970
	public void test074a() {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I { @Override void clone(); }\n" +
				"interface J extends I {}"
			},
			"----------\n" +
			"1. WARNING in I.java (at line 1)\n" +
			"	interface I { @Override void clone(); }\n" +
			"	                        ^^^^\n" +
			"The return type is incompatible with Object.clone(), thus this interface cannot be implemented\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 1)\n" +
			"	interface I { @Override void clone(); }\n" +
			"	                             ^^^^^^^\n" +
			mustOverrideMessage("clone()", "I") +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100970
	public void test074b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					interface I {
						int finalize();
						float hashCode();
					}
					interface J extends I {}
					abstract class A implements J {}"""
			},
			"""
				----------
				1. WARNING in A.java (at line 2)
					int finalize();
					^^^
				The return type is incompatible with Object.finalize(), thus this interface cannot be implemented
				----------
				2. ERROR in A.java (at line 3)
					float hashCode();
					^^^^^
				The return type is incompatible with Object.hashCode()
				----------
				3. ERROR in A.java (at line 6)
					abstract class A implements J {}
					               ^
				The return types are incompatible for the inherited methods I.finalize(), Object.finalize()
				----------
				4. ERROR in A.java (at line 6)
					abstract class A implements J {}
					               ^
				The return types are incompatible for the inherited methods I.hashCode(), Object.hashCode()
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107105
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A { <T, S extends J & I<T>> void foo() { } }
					class B extends A { @Override <T1, S1 extends J & I<S1>> void foo() { } }
					class C extends A { @Override <T2, S2 extends J & I> void foo() { } }
					class D extends A { @Override <T3, S3 extends J & I<T3>> void foo() { } }
					class E extends A { @Override <T4, S4 extends I<T4> & J> void foo() { } }
					interface I<TT> {}
					interface J {}"""
			},
			"----------\n" +
			"1. ERROR in A.java (at line 2)\n" +
			"	class B extends A { @Override <T1, S1 extends J & I<S1>> void foo() { } }\n" +
			"	                                                              ^^^^^\n" +
			"Name clash: The method foo() of type B has the same erasure as foo() of type A but does not override it\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 2)\n" +
			"	class B extends A { @Override <T1, S1 extends J & I<S1>> void foo() { } }\n" +
			"	                                                              ^^^^^\n" +
			mustOverrideMessage("foo()", "B") +
			"----------\n" +
			"3. WARNING in A.java (at line 3)\n" +
			"	class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" +
			"	                                                  ^\n" +
			"I is a raw type. References to generic type I<TT> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in A.java (at line 3)\n" +
			"	class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" +
			"	                                                          ^^^^^\n" +
			"Name clash: The method foo() of type C has the same erasure as foo() of type A but does not override it\n" +
			"----------\n" +
			"5. ERROR in A.java (at line 3)\n" +
			"	class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" +
			"	                                                          ^^^^^\n" +
			mustOverrideMessage("foo()", "C") +
			"----------\n"
			// A.java:2: method does not override a method from its superclass
			// A.java:3: method does not override a method from its superclass
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107105
	public void test075a() {
		this.runConformTest(
			// there is no name clash in this case AND no override error - there would be if the annotation was present
			new String[] {
				"A.java",
				"""
					class A<U> { <S extends J> void foo(U u, S s) { } }
					class B<V> extends A<V> { <S1 extends K> void foo(V v, S1 s) { } }
					interface J {}
					interface K extends J {}"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107105
	public void test075b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A<U> { <T, S extends J & I<T>> void foo(U u, T t, S s) { } }
					class B<V> extends A<V> { @Override <T1, S1 extends K & I<T1>> void foo(V v, T1 t, S1 s) { } }
					interface I<TT> {}
					interface J {}
					interface K extends J {}"""
			},
			"----------\n" +
			"1. ERROR in A.java (at line 2)\n" +
			"	class B<V> extends A<V> { @Override <T1, S1 extends K & I<T1>> void foo(V v, T1 t, S1 s) { } }\n" +
			"	                                                                    ^^^^^^^^^^^^^^^^^^^^\n" +
			mustOverrideMessage("foo(V, T1, S1)", "B<V>") +
			"----------\n"
			// A.java:2: method does not override a method from its superclass
		);
	}
	public void test076() {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					class A {
						<T, S extends J & I<S>> void foo(S s) { }
						<T, S extends I<T> & J > void foo(S s) { }
					}
					interface I<TT> {}
					interface J {}
					"""
			},
			""
		);
	}
	public void test076a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A {
						<T, S extends J & I<T>> void foo() { }
						<T, S extends I<T> & J> void foo() { }
					}
					interface I<TT> {}
					interface J {}
					"""
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					<T, S extends J & I<T>> void foo() { }
					                             ^^^^^
				Duplicate method foo() in type A
				----------
				2. ERROR in A.java (at line 3)
					<T, S extends I<T> & J> void foo() { }
					                             ^^^^^
				Duplicate method foo() in type A
				----------
				"""
		);
	}
	public void test076b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A {
						<T, S extends J & I<T>> void foo() { }
						<T, S extends I<T> & K> void foo() { }
					}
					interface I<TT> {}
					interface J {}
					interface K extends J {}"""
			},
			"""
				----------
				1. ERROR in A.java (at line 2)
					<T, S extends J & I<T>> void foo() { }
					                             ^^^^^
				Duplicate method foo() in type A
				----------
				2. ERROR in A.java (at line 3)
					<T, S extends I<T> & K> void foo() { }
					                             ^^^^^
				Duplicate method foo() in type A
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test077() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						Object o = new A<Integer>().foo(new Integer(1));
					}
					interface I<T1> { I<T1> foo(T1 t); }
					interface J<T2> { J<T2> foo(T2 t); }
					class B<T> { public A<T> foo(T t) { return new A<T>(); } }
					class A<S> extends B<S> implements I<S>, J<S> {}"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test077a() {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"""
					public interface I { I foo(); }
					interface J { J foo(); }
					interface K extends I, J { K foo(); }
					interface L { K getI(); }
					interface M { I getI(); }
					interface N { J getI(); }
					interface O extends L, M, N { K getI(); }
					interface P extends L, M, N {}
					class X implements L, M, N { public K getI() { return null; } }
					abstract class Y implements L, M, N {}
					abstract class Z implements L, M, N { public K getI() { return null; } }
					"""
			},
			""
// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=241821
// Now if 1 of 3 methods is acceptable to the other 2 then no error is reported
/* See addtional comments in https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
			"----------\n" +
			"1. ERROR in I.java (at line 3)\n" +
			"	interface K extends I, J { K foo(); }\n" +
			"	          ^\n" +
			"The return type is incompatible with J.foo(), I.foo()\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 7)\n" +
			"	interface O extends L, M, N { K getI(); }\n" +
			"	          ^\n" +
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 8)\n" +
			"	interface P extends L, M, N {}\n" +
			"	          ^\n" +
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 10)\n" +
			"	abstract class Y implements L, M, N {}\n" +
			"	               ^\n" +
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" +
			"----------\n"
*/
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128560
	public void test078() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
		customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"""
					public abstract class X implements IAppendable {
					    public X append(char c) {
					        return null;
					    }
					}
					
					interface IAppendable {
						IAppendable append(char c);
					}
					""",
			},
			null,
			customOptions,
			"""
				----------
				1. ERROR in X.java (at line 2)
					public X append(char c) {
					       ^
				The return type is incompatible with IAppendable.append(char)
				----------
				""",
			JavacTestOptions.SKIP /* we are altering the compatibility settings */);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=81222
	public void test079() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					class A<E> { void x(A<String> s) {} }
					class B extends A { void x(A<String> s) {} }
					class C extends A { @Override void x(A s) {} }
					class D extends A { void x(A<Object> s) {} }"""
			},
			"""
				----------
				1. WARNING in A.java (at line 2)
					class B extends A { void x(A<String> s) {} }
					                ^
				A is a raw type. References to generic type A<E> should be parameterized
				----------
				2. ERROR in A.java (at line 2)
					class B extends A { void x(A<String> s) {} }
					                         ^^^^^^^^^^^^^^
				Name clash: The method x(A<String>) of type B has the same erasure as x(A) of type A but does not override it
				----------
				3. WARNING in A.java (at line 3)
					class C extends A { @Override void x(A s) {} }
					                ^
				A is a raw type. References to generic type A<E> should be parameterized
				----------
				4. WARNING in A.java (at line 3)
					class C extends A { @Override void x(A s) {} }
					                                     ^
				A is a raw type. References to generic type A<E> should be parameterized
				----------
				5. WARNING in A.java (at line 4)
					class D extends A { void x(A<Object> s) {} }
					                ^
				A is a raw type. References to generic type A<E> should be parameterized
				----------
				6. ERROR in A.java (at line 4)
					class D extends A { void x(A<Object> s) {} }
					                         ^^^^^^^^^^^^^^
				Name clash: The method x(A<Object>) of type D has the same erasure as x(A) of type A but does not override it
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106880
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"HashOrder.java",
				"""
					public class HashOrder extends DoubleHash<String> {
						public static HashOrder create() { return null; }
					}
					class DoubleHash<T> {
						public static <U> DoubleHash<U> create() { return null; }
					}"""
			},
			"""
				----------
				1. WARNING in HashOrder.java (at line 2)
					public static HashOrder create() { return null; }
					              ^^^^^^^^^
				Type safety: The return type HashOrder for create() from the type HashOrder needs unchecked conversion to conform to DoubleHash<Object> from the type DoubleHash<String>
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=125956
	public void test081() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X<U> implements I {
						public A<String> foo() { return null; }
						public <S> A<U> bar() { return null; }
					}
					interface I {
						<T> A<T> foo();
						<S> A<S> bar();
					}
					class A<V> {}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					public A<String> foo() { return null; }
					       ^
				Type safety: The return type A<String> for foo() from the type X<U> needs unchecked conversion to conform to A<Object> from the type I
				----------
				2. ERROR in X.java (at line 3)
					public <S> A<U> bar() { return null; }
					           ^^^^
				The return type is incompatible with I.bar()
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105339
	public void test082() {
		this.runNegativeTest(
			new String[] {
				"V.java",
				"""
					public class V extends U { @Override public C<B> foo() { return null; } }
					class U { public <T extends A> C<T> foo() { return null; } }
					class A {}
					class B extends A {}
					class C<T> {}"""
			},
			"""
				----------
				1. WARNING in V.java (at line 1)
					public class V extends U { @Override public C<B> foo() { return null; } }
					                                            ^
				Type safety: The return type C<B> for foo() from the type V needs unchecked conversion to conform to C<A> from the type U
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132831
	public void test083() {
		this.runConformTest(
			new String[] {
				"C.java",
				"""
					public class C extends p.B {
						public static void main(String[] args) {
							System.out.println(((p.I) new C()).m() == null);
						}
					}""",
				"p/B.java",
				"""
					package p;
					public abstract class B extends A {}
					abstract class A implements I {
						public A m() { return null; }
					}""",
				"p/I.java",
				"package p;\n" +
				"public interface I { I m(); }\n"
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132841
	public void test084() {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public class A<T1 extends A.M> implements I<T1> {
						public java.util.List<T1> f(M n) { return null; }
						static class M {}
					}
					interface I<T2> {
						java.util.List<T2> f(T2 t);
					}"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132841
	public void test084a() {
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public class A<T1 extends A.M> implements I<T1> {
						public void foo(Number n, M m) {}
						public void foo2(Number n, M m) {}
						public void foo3(Number n, M m) {}
						static class M {}
					}
					interface I<T2> {
						<U extends Number> void foo(U u, T2 t);
						void foo2(Number n, T2 t);
						<U extends Number> void foo3(U u, A.M m);
					}"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132841
	public void test084b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					public class A<T1 extends A.M> implements I<T1> {
						public void foo4(Number n, T1 m) {}
						static class M {}
					}
					interface I<T2> {
						<U extends Number> void foo4(U u, A.M m);
					}"""
			},
			"""
				----------
				1. ERROR in A.java (at line 1)
					public class A<T1 extends A.M> implements I<T1> {
					             ^
				The type A<T1> must implement the inherited abstract method I<T1>.foo4(U, A.M)
				----------
				2. ERROR in A.java (at line 2)
					public void foo4(Number n, T1 m) {}
					            ^^^^^^^^^^^^^^^^^^^^
				Name clash: The method foo4(Number, T1) of type A<T1> has the same erasure as foo4(U, A.M) of type I<T2> but does not override it
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=136543
	public void test085() {
		this.runNegativeTest(
			new String[] {
				"Parent.java",
				"""
					import java.util.Collection;
					public class Parent {
						static void staticCase1(Collection c) {}
						static void staticCase2(Collection<String> c) {}
						void instanceCase1(Collection c) {}
						void instanceCase2(Collection<String> c) {}
					}
					class Child extends Parent {
						static void staticCase1(Collection<String> c) {}
						static void staticCase2(Collection c) {}
						void instanceCase1(Collection<String> c) {}
						@Override void instanceCase2(Collection c) {}
					}"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"""
				----------
				1. WARNING in Parent.java (at line 3)
					static void staticCase1(Collection c) {}
					                        ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				2. WARNING in Parent.java (at line 5)
					void instanceCase1(Collection c) {}
					                   ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				3. WARNING in Parent.java (at line 10)
					static void staticCase2(Collection c) {}
					                        ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				4. ERROR in Parent.java (at line 11)
					void instanceCase1(Collection<String> c) {}
					     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Name clash: The method instanceCase1(Collection<String>) of type Child has the same erasure as instanceCase1(Collection) of type Parent but does not override it
				----------
				5. WARNING in Parent.java (at line 12)
					@Override void instanceCase2(Collection c) {}
					                             ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				""":
				"""
					----------
					1. WARNING in Parent.java (at line 3)
						static void staticCase1(Collection c) {}
						                        ^^^^^^^^^^
					Collection is a raw type. References to generic type Collection<E> should be parameterized
					----------
					2. WARNING in Parent.java (at line 5)
						void instanceCase1(Collection c) {}
						                   ^^^^^^^^^^
					Collection is a raw type. References to generic type Collection<E> should be parameterized
					----------
					3. ERROR in Parent.java (at line 9)
						static void staticCase1(Collection<String> c) {}
						            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Name clash: The method staticCase1(Collection<String>) of type Child has the same erasure as staticCase1(Collection) of type Parent but does not hide it
					----------
					4. WARNING in Parent.java (at line 10)
						static void staticCase2(Collection c) {}
						                        ^^^^^^^^^^
					Collection is a raw type. References to generic type Collection<E> should be parameterized
					----------
					5. ERROR in Parent.java (at line 11)
						void instanceCase1(Collection<String> c) {}
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Name clash: The method instanceCase1(Collection<String>) of type Child has the same erasure as instanceCase1(Collection) of type Parent but does not override it
					----------
					6. WARNING in Parent.java (at line 12)
						@Override void instanceCase2(Collection c) {}
						                             ^^^^^^^^^^
					Collection is a raw type. References to generic type Collection<E> should be parameterized
					----------
					"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=136543 - case 2
	public void test085b() {
		this.runNegativeTest(
			new String[] {
				"Parent.java",
				"""
					import java.util.Collection;
					public class Parent {
						static void staticMismatchCase1(Collection c) {}
						static void staticMismatchCase2(Collection<String> c) {}
						void mismatchCase1(Collection c) {}
						void mismatchCase2(Collection<String> c) {}
					}
					class Child extends Parent {
						void staticMismatchCase1(Collection c) {}
						void staticMismatchCase2(Collection<String> c) {}
						static void mismatchCase1(Collection c) {}
						static void mismatchCase2(Collection<String> c) {}
					}"""
			},
			"""
				----------
				1. WARNING in Parent.java (at line 3)
					static void staticMismatchCase1(Collection c) {}
					                                ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				2. WARNING in Parent.java (at line 5)
					void mismatchCase1(Collection c) {}
					                   ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				3. ERROR in Parent.java (at line 9)
					void staticMismatchCase1(Collection c) {}
					     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				This instance method cannot override the static method from Parent
				----------
				4. WARNING in Parent.java (at line 9)
					void staticMismatchCase1(Collection c) {}
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				5. ERROR in Parent.java (at line 10)
					void staticMismatchCase2(Collection<String> c) {}
					     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				This instance method cannot override the static method from Parent
				----------
				6. ERROR in Parent.java (at line 11)
					static void mismatchCase1(Collection c) {}
					            ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				This static method cannot hide the instance method from Parent
				----------
				7. WARNING in Parent.java (at line 11)
					static void mismatchCase1(Collection c) {}
					                          ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				8. ERROR in Parent.java (at line 12)
					static void mismatchCase2(Collection<String> c) {}
					            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				This static method cannot hide the instance method from Parent
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=136543 - case 3
	public void test085c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X<V> extends CX<V> implements IX<V> {}
					class CX<T> { public static void foo(Object o) {} }
					abstract class X2 extends CX implements IX {}
					interface IX<U> { void foo(U u); }"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public abstract class X<V> extends CX<V> implements IX<V> {}
					                      ^
				The static method foo(Object) conflicts with the abstract method in IX<V>
				----------
				2. ERROR in X.java (at line 3)
					abstract class X2 extends CX implements IX {}
					               ^^
				The static method foo(Object) conflicts with the abstract method in IX
				----------
				3. WARNING in X.java (at line 3)
					abstract class X2 extends CX implements IX {}
					                          ^^
				CX is a raw type. References to generic type CX<T> should be parameterized
				----------
				4. WARNING in X.java (at line 3)
					abstract class X2 extends CX implements IX {}
					                                        ^^
				IX is a raw type. References to generic type IX<U> should be parameterized
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90438
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X implements I { public <T extends Object & Data> void copyData(T data) {} }
					interface I { <A extends Data> void copyData(A data); }
					interface Data {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X implements I { public <T extends Object & Data> void copyData(T data) {} }
					             ^
				The type X must implement the inherited abstract method I.copyData(A)
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90438 - case 2
	public void test086b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements I { public <T> G<T> foo(Class<T> stuffClass) { return null; } }
					interface I { <T extends Object> G<T> foo(Class<T> stuffClass); }
					class G<T> {}"""
			},
			""
		);
	}
	public void test087() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Collection;
					
					interface Interface1 {
					}
					interface Interface2 extends Interface1 {
					}
					interface Interface3 {
					    <P extends Interface1> Collection<P> doStuff();
					}
					interface Interface4 extends Interface3 {
					    Collection<Interface2> doStuff();
					}
					public class X {
					    Zork z;
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 11)
					Collection<Interface2> doStuff();
					^^^^^^^^^^
				Type safety: The return type Collection<Interface2> for doStuff() from the type Interface4 needs unchecked conversion to conform to Collection<Interface1> from the type Interface3
				----------
				2. ERROR in X.java (at line 14)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				"""
		);
	}
	//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=142653 - variation
	public void test088() {
		this.runNegativeTest(
			new String[] {
				"X.java",//===================
				"""
					import java.util.*;
					public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {
					\t
						void foo() {
							this.add(new Object());
							this.add(null);
						}
					}
					interface I<T1> extends Collection<String> {
					}
					""" , // =================, // =================
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {
					             ^
				The interface Collection cannot be implemented more than once with different arguments: Collection<T0> and Collection<String>
				----------
				2. ERROR in X.java (at line 2)
					public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {
					             ^
				The type X<T0> must implement the inherited abstract method Runnable.run()
				----------
				3. WARNING in X.java (at line 2)
					public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {
					             ^
				The serializable class X does not declare a static final serialVersionUID field of type long
				----------
				4. ERROR in X.java (at line 5)
					this.add(new Object());
					     ^^^
				The method add(T0) in the type ArrayList<T0> is not applicable for the arguments (Object)
				----------
				"""
		);
	}
	//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=142653 - variation
	public void test089() {
		this.runNegativeTest(
			new String[] {
				"X.java",//===================
				"""
					import java.util.*;
					public class X extends X2 {}
					abstract class X2 extends X3 implements List<String> {}
					abstract class X3 implements List<Thread> {}""", // =================
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					abstract class X2 extends X3 implements List<String> {}
					               ^^
				The interface List cannot be implemented more than once with different arguments: List<Thread> and List<String>
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147690
	public void test090() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class XSuper {
						Object foo() throws Exception { return null; }
						protected Object bar() throws Exception { return null; }
					}
					public class X extends XSuper {
						protected String foo() { return null; }
						public String bar() { return null; }
					}""", // =================
			},
			"");
		// 	ensure bridge methods have target method modifiers, and inherited thrown exceptions
		String expectedOutput =
			"""
			  // Method descriptor #17 ()Ljava/lang/Object;
			  // Stack: 1, Locals: 1
			  public bridge synthetic java.lang.Object bar() throws java.lang.Exception;
			    0  aload_0 [this]
			    1  invokevirtual X.bar() : java.lang.String [21]
			    4  areturn
			      Line numbers:
			        [pc: 0, line: 1]
			 \s
			  // Method descriptor #17 ()Ljava/lang/Object;
			  // Stack: 1, Locals: 1
			  protected bridge synthetic java.lang.Object foo() throws java.lang.Exception;
			    0  aload_0 [this]
			    1  invokevirtual X.foo() : java.lang.String [23]
			    4  areturn
			      Line numbers:
			        [pc: 0, line: 1]
			""";

		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148783
	public void test091() {
		this.runNegativeTest(
			new String[] {
				"DataSet.java",//===================
				"""
					import java.io.Serializable;
					import java.util.*;
					
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					\t
						public <S> S[] toArray(S[] s) {
							return s;
						}
					
						public boolean add(Object o) { return false; }
						public void add(int index, Object element) {}
						public boolean addAll(Collection c) {	return false; }
						public boolean addAll(int index, Collection c) {	return false; }
						public void clear() {}
						public boolean contains(Object o) {	return false; }
						public boolean containsAll(Collection c) { return false; }
						public Object get(int index) { return null; }
						public int indexOf(Object o) { return 0; }
						public boolean isEmpty() {	return false; }
						public Iterator iterator() {	return null; }
						public int lastIndexOf(Object o) {	return 0; }
						public ListIterator listIterator() {	return null; }
						public ListIterator listIterator(int index) {	return null; }
						public boolean remove(Object o) {	return false; }
						public Object remove(int index) {	return null; }
						public boolean removeAll(Collection c) {	return false; }
						public boolean retainAll(Collection c) {	return false; }
						public Object set(int index, Object element) {	return false; }
						public int size() {	return 0; }
						public List subList(int fromIndex, int toIndex) {	return null; }
						public Object[] toArray() {	return null; }
						public boolean hasNext() {	return false; }
						public Object next() {	return null; }
						public void remove() {}
					}
					""", // =================
			},
			"""
				----------
				1. ERROR in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					      ^^^^^^^
				The type DataSet<T> must implement the inherited abstract method List.toArray(Object[])
				----------
				2. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					      ^^^^^^^
				The serializable class DataSet does not declare a static final serialVersionUID field of type long
				----------
				3. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					                                           ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				4. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					                                                 ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				5. ERROR in DataSet.java (at line 6)
					public <S> S[] toArray(S[] s) {
					               ^^^^^^^^^^^^^^
				Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type List but does not override it
				----------
				6. ERROR in DataSet.java (at line 6)
					public <S> S[] toArray(S[] s) {
					               ^^^^^^^^^^^^^^
				Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type Collection but does not override it
				----------
				7. WARNING in DataSet.java (at line 12)
					public boolean addAll(Collection c) {	return false; }
					                      ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				8. WARNING in DataSet.java (at line 13)
					public boolean addAll(int index, Collection c) {	return false; }
					                                 ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				9. WARNING in DataSet.java (at line 16)
					public boolean containsAll(Collection c) { return false; }
					                           ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				10. WARNING in DataSet.java (at line 20)
					public Iterator iterator() {	return null; }
					       ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				11. WARNING in DataSet.java (at line 22)
					public ListIterator listIterator() {	return null; }
					       ^^^^^^^^^^^^
				ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized
				----------
				12. WARNING in DataSet.java (at line 23)
					public ListIterator listIterator(int index) {	return null; }
					       ^^^^^^^^^^^^
				ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized
				----------
				13. WARNING in DataSet.java (at line 26)
					public boolean removeAll(Collection c) {	return false; }
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				14. WARNING in DataSet.java (at line 27)
					public boolean retainAll(Collection c) {	return false; }
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				15. WARNING in DataSet.java (at line 30)
					public List subList(int fromIndex, int toIndex) {	return null; }
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				"""
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148783 - variation
	public void test092() {
		this.runNegativeTest(
			new String[] {
				"DataSet.java",//===================
				"""
					import java.io.Serializable;
					import java.util.*;
					
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					\t
						public <S extends T> S[] toArray(S[] s) {
							return s;
						}
					
						public boolean add(Object o) { return false; }
						public void add(int index, Object element) {}
						public boolean addAll(Collection c) {	return false; }
						public boolean addAll(int index, Collection c) {	return false; }
						public void clear() {}
						public boolean contains(Object o) {	return false; }
						public boolean containsAll(Collection c) { return false; }
						public Object get(int index) { return null; }
						public int indexOf(Object o) { return 0; }
						public boolean isEmpty() {	return false; }
						public Iterator iterator() {	return null; }
						public int lastIndexOf(Object o) {	return 0; }
						public ListIterator listIterator() {	return null; }
						public ListIterator listIterator(int index) {	return null; }
						public boolean remove(Object o) {	return false; }
						public Object remove(int index) {	return null; }
						public boolean removeAll(Collection c) {	return false; }
						public boolean retainAll(Collection c) {	return false; }
						public Object set(int index, Object element) {	return false; }
						public int size() {	return 0; }
						public List subList(int fromIndex, int toIndex) {	return null; }
						public Object[] toArray() {	return null; }
						public boolean hasNext() {	return false; }
						public Object next() {	return null; }
						public void remove() {}
					}
					""", // =================
			},
			"""
				----------
				1. ERROR in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					      ^^^^^^^
				The type DataSet<T> must implement the inherited abstract method List.toArray(Object[])
				----------
				2. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					      ^^^^^^^
				The serializable class DataSet does not declare a static final serialVersionUID field of type long
				----------
				3. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					                                           ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				4. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					                                                 ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				5. WARNING in DataSet.java (at line 12)
					public boolean addAll(Collection c) {	return false; }
					                      ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				6. WARNING in DataSet.java (at line 13)
					public boolean addAll(int index, Collection c) {	return false; }
					                                 ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				7. WARNING in DataSet.java (at line 16)
					public boolean containsAll(Collection c) { return false; }
					                           ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				8. WARNING in DataSet.java (at line 20)
					public Iterator iterator() {	return null; }
					       ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				9. WARNING in DataSet.java (at line 22)
					public ListIterator listIterator() {	return null; }
					       ^^^^^^^^^^^^
				ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized
				----------
				10. WARNING in DataSet.java (at line 23)
					public ListIterator listIterator(int index) {	return null; }
					       ^^^^^^^^^^^^
				ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized
				----------
				11. WARNING in DataSet.java (at line 26)
					public boolean removeAll(Collection c) {	return false; }
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				12. WARNING in DataSet.java (at line 27)
					public boolean retainAll(Collection c) {	return false; }
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				13. WARNING in DataSet.java (at line 30)
					public List subList(int fromIndex, int toIndex) {	return null; }
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148783 - variation
	public void test093() {
		this.runNegativeTest(
			new String[] {
				"DataSet.java",//===================
				"""
					import java.io.Serializable;
					import java.util.*;
					
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					\t
						public <S> S[] toArray(S[] s) {
							return s;
						}
						public Object[] toArray(Object[] o) {
							return o;
						}
						public boolean add(Object o) { return false; }
						public void add(int index, Object element) {}
						public boolean addAll(Collection c) {	return false; }
						public boolean addAll(int index, Collection c) {	return false; }
						public void clear() {}
						public boolean contains(Object o) {	return false; }
						public boolean containsAll(Collection c) { return false; }
						public Object get(int index) { return null; }
						public int indexOf(Object o) { return 0; }
						public boolean isEmpty() {	return false; }
						public Iterator iterator() {	return null; }
						public int lastIndexOf(Object o) {	return 0; }
						public ListIterator listIterator() {	return null; }
						public ListIterator listIterator(int index) {	return null; }
						public boolean remove(Object o) {	return false; }
						public Object remove(int index) {	return null; }
						public boolean removeAll(Collection c) {	return false; }
						public boolean retainAll(Collection c) {	return false; }
						public Object set(int index, Object element) {	return false; }
						public int size() {	return 0; }
						public List subList(int fromIndex, int toIndex) {	return null; }
						public Object[] toArray() {	return null; }
						public boolean hasNext() {	return false; }
						public Object next() {	return null; }
						public void remove() {}
					}
					""", // =================
			},
			"""
				----------
				1. ERROR in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					      ^^^^^^^
				The type DataSet<T> must implement the inherited abstract method List.toArray(Object[])
				----------
				2. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					      ^^^^^^^
				The serializable class DataSet does not declare a static final serialVersionUID field of type long
				----------
				3. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					                                           ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				4. WARNING in DataSet.java (at line 4)
					class DataSet<T extends Number> implements List, Iterator, Serializable {
					                                                 ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				5. ERROR in DataSet.java (at line 6)
					public <S> S[] toArray(S[] s) {
					               ^^^^^^^^^^^^^^
				Erasure of method toArray(S[]) is the same as another method in type DataSet<T>
				----------
				6. ERROR in DataSet.java (at line 6)
					public <S> S[] toArray(S[] s) {
					               ^^^^^^^^^^^^^^
				Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type List but does not override it
				----------
				7. ERROR in DataSet.java (at line 6)
					public <S> S[] toArray(S[] s) {
					               ^^^^^^^^^^^^^^
				Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type Collection but does not override it
				----------
				8. ERROR in DataSet.java (at line 9)
					public Object[] toArray(Object[] o) {
					                ^^^^^^^^^^^^^^^^^^^
				Erasure of method toArray(Object[]) is the same as another method in type DataSet<T>
				----------
				9. WARNING in DataSet.java (at line 14)
					public boolean addAll(Collection c) {	return false; }
					                      ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				10. WARNING in DataSet.java (at line 15)
					public boolean addAll(int index, Collection c) {	return false; }
					                                 ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				11. WARNING in DataSet.java (at line 18)
					public boolean containsAll(Collection c) { return false; }
					                           ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				12. WARNING in DataSet.java (at line 22)
					public Iterator iterator() {	return null; }
					       ^^^^^^^^
				Iterator is a raw type. References to generic type Iterator<E> should be parameterized
				----------
				13. WARNING in DataSet.java (at line 24)
					public ListIterator listIterator() {	return null; }
					       ^^^^^^^^^^^^
				ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized
				----------
				14. WARNING in DataSet.java (at line 25)
					public ListIterator listIterator(int index) {	return null; }
					       ^^^^^^^^^^^^
				ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized
				----------
				15. WARNING in DataSet.java (at line 28)
					public boolean removeAll(Collection c) {	return false; }
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				16. WARNING in DataSet.java (at line 29)
					public boolean retainAll(Collection c) {	return false; }
					                         ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				17. WARNING in DataSet.java (at line 32)
					public List subList(int fromIndex, int toIndex) {	return null; }
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				"""
		);
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146383
public void test094() {
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.JavacCompilesIncorrectSource,
		new String[] {
			"X.java",//===================
			"""
				import java.util.ArrayList;
				import java.util.Arrays;
				class Y<T> {}
				public class X
				{
				  private static ArrayList<Y<X>> y = new ArrayList<Y<X>>();
				  void foo(Y[] array)
				  {
				    y.addAll(Arrays.asList(array));
				  }
				}
				""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				void foo(Y[] array)
				         ^
			Y is a raw type. References to generic type Y<T> should be parameterized
			----------
			2. ERROR in X.java (at line 9)
				y.addAll(Arrays.asList(array));
				  ^^^^^^
			The method addAll(Collection<? extends Y<X>>) in the type ArrayList<Y<X>> is not applicable for the arguments (List<Y>)
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148957
public void test096() {
	this.runNegativeTest(
		new String[] {
			"ProblemClass.java",//===================
			"""
				import java.util.Collection;
				import javax.swing.JLabel;
				interface SuperInterface {
				   public <A extends JLabel> void doIt(Collection<A> as);
				}
				
				public class ProblemClass implements SuperInterface {
				   public void doIt(Collection<? extends JLabel> as) {
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in ProblemClass.java (at line 7)
				public class ProblemClass implements SuperInterface {
				             ^^^^^^^^^^^^
			The type ProblemClass must implement the inherited abstract method SuperInterface.doIt(Collection<A>)
			----------
			2. ERROR in ProblemClass.java (at line 8)
				public void doIt(Collection<? extends JLabel> as) {
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method doIt(Collection<? extends JLabel>) of type ProblemClass has the same erasure as doIt(Collection<A>) of type SuperInterface but does not override it
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148957 - variation
public void test097() {
	this.runConformTest(
		new String[] {
			"ProblemClass.java",//===================
			"""
				import java.util.Collection;
				import javax.swing.JLabel;
				interface SuperInterface {
				   public <A extends JLabel> void doIt(Collection<A> as);
				}
				
				public class ProblemClass implements SuperInterface {
				   public <B extends JLabel> void doIt(Collection<B> as) {
				   }
				}
				"""
		},
		""
	);
}

// autoboxing mixed with type parameters substitution
public void test098() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X<A, B> {
				    public X(List<A> toAdd) {
				    }
				    public <L extends List<? super A>, LF extends Factory<L>> L\s
				            foo(B b, L l, LF lf) {
				        return l;
				    }
				    public static class ListFactory<T> implements Factory<List<T>> {
				        public List<T> create() {
				            return null;
				        }
				    }
				    public static interface Factory<T> {
				        public T create();
				    }
				    public static void bar() {
				        (new X<Long, Number>(new ArrayList<Long>())).
				            foo(1, (List<Number>) null, new ListFactory<Number>());
				    }
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=153874
public void test099() {
	Map customOptions= getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"TestCharset.java",
			"""
				import java.nio.charset.*;
				public class TestCharset extends Charset {
					protected TestCharset(String n, String[] a) { super(n, a); }
					public boolean contains(Charset cs) { return false; }
					public CharsetDecoder newDecoder() { return null; }
					public CharsetEncoder newEncoder() { return null; }
				}
				""" ,
		},
		"",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);
}

// name conflict
public void test100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				public class X<E> {
				  boolean removeAll(Collection<? extends E> c) {
				    return false;
				  }
				}
				""",
			"Y.java",
			"""
				import java.util.Collection;
				public class Y<E> extends X<E>
				{
				  <T extends E> boolean removeAll(Collection<T> c) {
				    return false;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 4)
				<T extends E> boolean removeAll(Collection<T> c) {
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method removeAll(Collection<T>) of type Y<E> has the same erasure as removeAll(Collection<? extends E>) of type X<E> but does not override it
			----------
			"""
	);
}

// name conflict
public void test101() {
	String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
			"""
				----------
				1. WARNING in X.java (at line 3)
					Integer getX(List<Integer> l) {
					        ^^^^^^^^^^^^^^^^^^^^^
				Erasure of method getX(List<Integer>) is the same as another method in type X
				----------
				2. WARNING in X.java (at line 6)
					String getX(List<String> l) {
					       ^^^^^^^^^^^^^^^^^^^^
				Erasure of method getX(List<String>) is the same as another method in type X
				----------
				3. ERROR in X.java (at line 11)
					Integer getX(List<Integer> l) {
					        ^^^^^^^^^^^^^^^^^^^^^
				Duplicate method getX(List<Integer>) in type Y
				----------
				4. ERROR in X.java (at line 14)
					String getX(List<Integer> l) {
					       ^^^^^^^^^^^^^^^^^^^^^
				Duplicate method getX(List<Integer>) in type Y
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 3)
						Integer getX(List<Integer> l) {
						        ^^^^^^^^^^^^^^^^^^^^^
					Erasure of method getX(List<Integer>) is the same as another method in type X
					----------
					2. ERROR in X.java (at line 6)
						String getX(List<String> l) {
						       ^^^^^^^^^^^^^^^^^^^^
					Erasure of method getX(List<String>) is the same as another method in type X
					----------
					3. ERROR in X.java (at line 11)
						Integer getX(List<Integer> l) {
						        ^^^^^^^^^^^^^^^^^^^^^
					Duplicate method getX(List<Integer>) in type Y
					----------
					4. ERROR in X.java (at line 14)
						String getX(List<Integer> l) {
						       ^^^^^^^^^^^^^^^^^^^^^
					Duplicate method getX(List<Integer>) in type Y
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
				    Integer getX(List<Integer> l) {
				        return null;
				    }
				    String getX(List<String> l) {
				        return null;
				    }
				}
				class Y {
				    Integer getX(List<Integer> l) {
				        return null;
				    }
				    String getX(List<Integer> l) {
				        return null;
				    }
				}"""
		},
		expectedCompilerLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test102() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface ReturnLeaf extends ReturnDerived {
					}
				
					private interface Interface {
						ReturnBase bar();
					}
				
					private static class Implementation {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child extends Implementation implements Interface {
					}
				
					private static class Grandchild extends Child implements Interface {
						@Override
						public ReturnLeaf bar() {
							return null;
						}
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 26)
				public ReturnLeaf bar() {
				                  ^^^^^
			Cannot override the final method from X.Implementation
			----------
			""",
		null,
		true,
		options
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test103() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface Interface {
						ReturnBase bar();
					}
				
					private static class Implementation {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Grandchild extends Child implements Interface {
					}
				
					private static class Child extends Implementation implements Interface {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test104() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface Interface {
						ReturnBase bar();
					}
				
					private static class Implementation {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child extends Implementation implements Interface {
					}
				
					private static class Grandchild extends Child implements Interface {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test105() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private static class Super {
						ReturnBase bar() {
							return null;
						}
					}
				
					private static class Implementation extends Super {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child extends Implementation {
					}
				
					private static class Grandchild extends Child {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("Should have two method bar", 2, count);
	assertTrue("should have one bridge method", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test106() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private static class Super {
						ReturnBase bar() {
							return null;
						}
					}
				
					private static abstract class Implementation extends Super {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child extends Implementation {
					}
				
					private static class Grandchild extends Child {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count ++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("should have two methods bar", 2, count);
	assertTrue("should have one bridge method", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test107() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface Interface<E> {
						ReturnBase bar();
					}
				
					private static class Implementation<T> {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child<U> extends Implementation<U> implements Interface<U> {
					}
				
					private static class Grandchild<V> extends Child<V> implements Interface<V> {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test108() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface ReturnLeaf extends ReturnDerived {
					}
				
					private interface Interface<E> {
						ReturnBase bar();
					}
				
					private static class Implementation<T> {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child<U> extends Implementation<U> implements Interface<U> {
					}
				
					private static class Grandchild<V> extends Child<V> implements Interface<V> {
						@Override
						public ReturnLeaf bar() {
							return null;
						}
					}
				
					public static void main(String[] args) {
						new Grandchild<String>();
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 26)
				public ReturnLeaf bar() {
				                  ^^^^^
			Cannot override the final method from X.Implementation<V>
			----------
			""",
		null,
		true,
		options
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test109() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface Interface<E> {
						ReturnBase bar();
					}
				
					private static class Implementation<T> {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Grandchild<V> extends Child<V> implements Interface<V> {
					}
				
					private static class Child<U> extends Implementation<U> implements Interface<U> {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test110() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private interface Interface<E> {
						ReturnBase bar();
					}
				
					private static class Implementation<T> {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child<U> extends Implementation<U> implements Interface<U> {
					}
				
					private static class Grandchild<V> extends Child<V> implements Interface<V> {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test111() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private static class Super<E> {
						ReturnBase bar() {
							return null;
						}
					}
				
					private static class Implementation<T> extends Super<T> {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child<U> extends Implementation<U> {
					}
				
					private static class Grandchild<V> extends Child<V> {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("should have two methods bar", 2, count);
	assertTrue("should have one bridge method", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test112() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private interface ReturnBase {
					}
				
					private interface ReturnDerived extends ReturnBase {
					}
				
					private static class Super<E> {
						ReturnBase bar() {
							return null;
						}
					}
				
					private static abstract class Implementation<T> extends Super<T> {
						public final ReturnDerived bar() {
							return null;
						}
					}
				
					private static class Child<U> extends Implementation<U> {
					}
				
					private static class Grandchild<V> extends Child<V> {
					}
				
					public static void main(String[] args) {
						new Grandchild();
					}
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);

	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("should have two methods bar", 2, count);
	assertTrue("should have one bridge method", found);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test113() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				abstract class Y {
				  abstract void foo();
				}
				public class X extends Y {
				  void foo() {
				    // should not complain for missing super call, since overriding\s
				    // abstract method
				  }
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test114() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class Y {
				  void foo() {}
				}
				public class X extends Y {
				  @Override
				  void foo() {
				  }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				void foo() {
				     ^^^^^
			The method X.foo() is overriding a method without making a super invocation
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test115() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y {
				  void foo() {}
				}
				public class X extends Y {
				  @Override
				  void foo() {
				    super.foo();
				  }
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test116() {
   	Map options = getCompilerOptions();
   	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y {
				  Zork foo() {}
				}
				public class X extends Y {
				  @Override
				  Object foo() {
				     return new Y() {
				         Object foo() {
				            return null;
				         }
				     };\
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork foo() {}
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 6)
				Object foo() {
				^^^^^^
			The return type is incompatible with Y.foo()
			----------
			3. ERROR in X.java (at line 6)
				Object foo() {
				       ^^^^^
			The method X.foo() is overriding a method without making a super invocation
			----------
			4. ERROR in X.java (at line 8)
				Object foo() {
				^^^^^^
			The return type is incompatible with Y.foo()
			----------
			5. WARNING in X.java (at line 8)
				Object foo() {
				       ^^^^^
			The method foo() of type new Y(){} should be tagged with @Override since it actually overrides a superclass method
			----------
			6. ERROR in X.java (at line 8)
				Object foo() {
				       ^^^^^
			The method new Y(){}.foo() is overriding a method without making a super invocation
			----------
			""",
		null,
		true,
		options	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test117() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class Y {
				  Object foo() {
				     return null;
				  }
				}
				public class X extends Y {
				  @Override
				  Object foo() {
				     return new Y() {
				         @Override
				         Object foo() {
				            return null;
				         }
				     };\
				  }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 8)
				Object foo() {
				       ^^^^^
			The method X.foo() is overriding a method without making a super invocation
			----------
			2. ERROR in X.java (at line 11)
				Object foo() {
				       ^^^^^
			The method new Y(){}.foo() is overriding a method without making a super invocation
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test118() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class Y<E> {
					<U extends E> U foo() {
						return null;
					}
				}
				
				public class X<T> extends Y<T> {
					@Override
					<V extends T> V foo() {
						return null;
					}
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 9)
				<V extends T> V foo() {
				                ^^^^^
			The method X<T>.foo() is overriding a method without making a super invocation
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test119() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class Y<E> {
					E foo() {
						return null;
					}
				}
				
				public class X<T> extends Y<T> {
					@Override
					T foo() {
						return null;
					}
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 9)
				T foo() {
				  ^^^^^
			The method X<T>.foo() is overriding a method without making a super invocation
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161541
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					abstract class M<T extends CharSequence, S> {
						void e(T t) {}
						void e(S s) {}
					}
					class N extends M<String, String> {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				class N extends M<String, String> {}
				      ^
			Duplicate methods named e with the parameters (S) and (T) are defined by the type X.M<String,String>
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=202830
public void test120a() {
	String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
			"""
				----------
				1. WARNING in Bar.java (at line 2)
					int getThing(V v) { return 1; }
					    ^^^^^^^^^^^^^
				Erasure of method getThing(V) is the same as another method in type Foo<V,E>
				----------
				2. WARNING in Bar.java (at line 3)
					boolean getThing(E e) { return true; }
					        ^^^^^^^^^^^^^
				Erasure of method getThing(E) is the same as another method in type Foo<V,E>
				----------
				""":
				"""
					----------
					1. ERROR in Bar.java (at line 2)
						int getThing(V v) { return 1; }
						    ^^^^^^^^^^^^^
					Erasure of method getThing(V) is the same as another method in type Foo<V,E>
					----------
					2. ERROR in Bar.java (at line 3)
						boolean getThing(E e) { return true; }
						        ^^^^^^^^^^^^^
					Erasure of method getThing(E) is the same as another method in type Foo<V,E>
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"Bar.java",
			"""
				class Foo<V, E> {
					int getThing(V v) { return 1; }
					boolean getThing(E e) { return true; }
				}
				public class Bar<V,E> extends Foo<V,E> {}"""
		},
		expectedCompilerLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173477
public void test121() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Root {
					public Root someMethod();
				}
				
				interface Intermediary extends Root {
					public Leaf someMethod();
				}
				
				class Leaf implements Intermediary {
					public Leaf someMethod() {
						System.out.print("SUCCESS");
						return null;
					}
				}
				
				public class X {
					public static void main(String[] args) {
						Leaf leafReference = new Leaf();
						leafReference.someMethod();
						Root rootReference = leafReference;
						rootReference.someMethod(); /* throws error */
					}
				}"""
		},
		"SUCCESSSUCCESS"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=175987
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  public void foo(Integer i, Y<String> l1, Y<String> l2);
				}
				public class X implements I {
				  public void foo(Integer i, Y<String> l1, Y l2) {
				  }
				}
				class Y<T> {
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				public class X implements I {
				             ^
			The type X must implement the inherited abstract method I.foo(Integer, Y<String>, Y<String>)
			----------
			2. ERROR in X.java (at line 5)
				public void foo(Integer i, Y<String> l1, Y l2) {
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method foo(Integer, Y<String>, Y) of type X has the same erasure as foo(Integer, Y<String>, Y<String>) of type I but does not override it
			----------
			3. WARNING in X.java (at line 5)
				public void foo(Integer i, Y<String> l1, Y l2) {
				                                         ^
			Y is a raw type. References to generic type Y<T> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=175987
// variant that must pass because X#foo's signature is a subsignature of
// I#foo's.
public void test123() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  public void foo(Integer i, Y<String> l1, Y<String> l2);
				}
				public class X implements I {
				  public void foo(Integer i, Y l1, Y l2) {
				  }
				}
				class Y<T> {
				}"""},
		""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// **
public void test124() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static String choose(String one, String two) {
				    return one + X.<String>choose(one, two);
				  }
				  public static <T> T choose(T one, T two) {
				    return two;
				  }
				  public static void main(String args[]) {
				    try {
				        System.out.println(choose("a", "b"));
				    } catch (StackOverflowError e) {
				        System.out.println("Stack Overflow");
				    }
				  }
				}"""},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ? "ab" : "Stack Overflow");
}
// Bug 460993: [compiler] Incremental build not always reports the same errors (type cannot be resolved - indirectly referenced)
public void test124b() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
				  public Object o = "";
				  public static void main(String args[]) {
				    X.main(args);
				  }
				}
				""",
			"X.java",
			"""
				public class X {
				  public static String choose(String one, String two) {
				    return one + X.<String>choose(one, two);
				  }
				  public static <T> T choose(T one, T two) {
				    return two;
				  }
				  public static void main(String args[]) {
				    try {
				        System.out.println(choose("a", "b"));
				    } catch (StackOverflowError e) {
				        System.out.println("Stack Overflow");
				    }
				  }
				}"""},
		this.complianceLevel <= ClassFileConstants.JDK1_6 ? "ab" : "Stack Overflow");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// variant
public void test125() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static <T> String choose(String one, String two) {
				    return one;
				  }
				  public static <T> T choose(T one, T two) {
				    return two;
				  }
				  public static void main(String args[]) {
				    System.out.println(choose("a", "b") + X.<String>choose("a", "b"));
				  }
				}"""
		},
		"aa"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// variant
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176171
// deprecated by GenericTypeTest#test1203.
//public void _test126() {
//	this.runNegativeTest(
//		new String[] {
//			"X.java",
//			"public class X extends Y {\n" +
//			"  public static String foo(String one, String two) {\n" + // complain
//			"    return X.<String>foo(one, two);\n" +
//			"  }\n" +
//			"  public String bar(String one, String two) {\n" + // complain
//			"    return this.<String>bar(one, two);\n" +
//			"  }\n" +
//			"  @Override\n" +
//			"  public String foobar(String one, String two) {\n" + // OK
//			"    return this.<String>foobar(one, two);\n" +
//			"  }\n" +
//			"}\n" +
//			"class Y {\n" +
//			"  public <T> String foobar(String one, String two) {\n" +
//			"    return null;\n" +
//			"  }\n" +
//			"}\n"},
//		"----------\n" +
//		"1. ERROR in X.java (at line 3)\n" +
//		"	return X.<String>foo(one, two);\n" +
//		"	                 ^^^\n" +
//		"The method foo(String, String) of type X is not generic; it cannot be parameterized with arguments <String>\n" +
//		"----------\n" +
//		"2. ERROR in X.java (at line 6)\n" +
//		"	return this.<String>bar(one, two);\n" +
//		"	                    ^^^\n" +
//		"The method bar(String, String) of type X is not generic; it cannot be parameterized with arguments <String>\n" +
//		"----------\n");
//}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174445
public void test127() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  enum Enum1 {
				    value;
				  }
				  enum Enum2 {
				    value;
				  }
				  static abstract class A<T> {
				    abstract <U extends T> U foo();
				  }
				  static class B extends A<Enum<?>> {
				    @Override
				    Enum<?> foo() {
				      return Enum1.value;
				    } \s
				  }
				  public static void main(String[] args) {
				    A<Enum<?>> a = new B();
				    Enum2 value = a.foo();
				  }
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 13)
				Enum<?> foo() {
				^^^^
			Type safety: The return type Enum<?> for foo() from the type X.B needs unchecked conversion to conform to U from the type X.A<T>
			----------
			""",
		null,
		true,
		null,
		"java.lang.ClassCastException");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
public void test128() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<U, V> {
				  U foo(Object o, V v);
				}
				public class X<U, V> implements I<U, V> {
				  public Object foo(Object o, Object v) { return null; }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				public Object foo(Object o, Object v) { return null; }
				       ^^^^^^
			Type safety: The return type Object for foo(Object, Object) from the type X<U,V> needs unchecked conversion to conform to U from the type I<U,V>
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - Object is not a subtype of Z
public void test129() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<U, V> {
				  U foo(Object o, V v);
				}
				public class X<U extends Z, V> implements I<U, V> {
				  public Object foo(Object o, Object v) { return null; }
				}
				class Z {}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public Object foo(Object o, Object v) { return null; }
				       ^^^^^^
			The return type is incompatible with I<U,V>.foo(Object, V)
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - Z<Object> is not a subtype of Z<U>, and |Z<U>| = Z, not Z<Object>
public void test130() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<U, V> {
				  Z<U> foo(Object o, V v);
				}
				public class X<U, V> implements I<U, V> {
				  public Z<Object> foo(Object o, Object v) { return null; }
				}
				class Z<T> {}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public Z<Object> foo(Object o, Object v) { return null; }
				       ^^^^^^^^^
			The return type is incompatible with I<U,V>.foo(Object, V)
			----------
			""",
		JavacTestOptions.EclipseJustification.EclipseBug180789
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - two interfaces
public void test131() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<U, V> {
				  U foo();
				  U foo(Object o, V v);
				}
				interface X<U, V> extends I<U, V> {
				  Object foo();
				  Object foo(Object o, Object v);
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				Object foo();
				^^^^^^
			The return type is incompatible with I<U,V>.foo()
			----------
			2. WARNING in X.java (at line 7)
				Object foo(Object o, Object v);
				^^^^^^
			Type safety: The return type Object for foo(Object, Object) from the type X<U,V> needs unchecked conversion to conform to U from the type I<U,V>
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - type identity vs type equivalence
public void test132() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<U> {
				  U foo(I<?> p);
				  U foo2(I<? extends Object> p);
				}
				public class X<U> implements I<U> {
				  public Object foo(I<? extends Object> p) { return null; }
				  public Object foo2(I<?> p) { return null; }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				public Object foo(I<? extends Object> p) { return null; }
				       ^^^^^^
			The return type is incompatible with I<U>.foo(I<?>)
			----------
			2. ERROR in X.java (at line 7)
				public Object foo2(I<?> p) { return null; }
				       ^^^^^^
			The return type is incompatible with I<U>.foo2(I<? extends Object>)
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - if we detect a return type incompatibility, then skip any @Override errors
public void test133() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				class A<U> {
				  U foo() { return null; }
				  U foo(U one) { return null; }
				  U foo(U one, U two) { return null; }
				}
				class B<U> extends A<U> {
				  @Override // does not override error
				  Object foo() { return null; } // cannot override foo(), incompatible return type error
				  @Override // does not override error
				  Object foo(Object one) { return null; } // unchecked conversion warning
				  @Override // does not override error
				  Object foo(Object one, U two) { return null; }
				}
				class C<U> extends A<U> {
				  @Override // does not override error
				  Object foo(U one) { return null; } // cannot override foo(U), incompatible return type error
				  @Override // does not override error
				  Object foo(U one, U two) { return null; } // cannot override foo(U), incompatible return type error
				}"""
		},
		"----------\n" +
		"1. ERROR in A.java (at line 8)\n" +
		"	Object foo() { return null; } // cannot override foo(), incompatible return type error\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with A<U>.foo()\n" +
		"----------\n" +
		"2. WARNING in A.java (at line 10)\n" +
		"	Object foo(Object one) { return null; } // unchecked conversion warning\n" +
		"	^^^^^^\n" +
		"Type safety: The return type Object for foo(Object) from the type B<U> needs unchecked conversion to conform to U from the type A<U>\n" +
		"----------\n" +
		"3. ERROR in A.java (at line 12)\n" +
		"	Object foo(Object one, U two) { return null; }\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Name clash: The method foo(Object, U) of type B<U> has the same erasure as foo(U, U) of type A<U> but does not override it\n" +
		"----------\n" +
		"4. ERROR in A.java (at line 12)\n" +
		"	Object foo(Object one, U two) { return null; }\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^\n" +
		mustOverrideMessage("foo(Object, U)", "B<U>") +
		"----------\n" +
		"5. ERROR in A.java (at line 16)\n" +
		"	Object foo(U one) { return null; } // cannot override foo(U), incompatible return type error\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with A<U>.foo(U)\n" +
		"----------\n" +
		"6. ERROR in A.java (at line 18)\n" +
		"	Object foo(U one, U two) { return null; } // cannot override foo(U), incompatible return type error\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with A<U>.foo(U, U)\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test134() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				interface I {
				  <T extends Exception & Cloneable> T foo(Number n);
				}
				interface J extends I {
				  A foo(Number n);
				}
				abstract class A extends Exception implements Cloneable {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. WARNING in A.java (at line 5)
				A foo(Number n);
				^
			Type safety: The return type A for foo(Number) from the type J needs unchecked conversion to conform to T from the type I
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test135() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				abstract class X implements J {}
				class X2 implements J {
				  public A foo(Number n) { return null; }
				}
				abstract class Y extends X {}
				interface I {
				  <T extends Exception & Cloneable> T foo(Number n);
				}
				interface J extends I {
				  A foo(Number n);
				}
				abstract class A extends Exception implements Cloneable {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				public A foo(Number n) { return null; }
				       ^
			Type safety: The return type A for foo(Number) from the type X2 needs unchecked conversion to conform to T from the type I
			----------
			2. WARNING in X.java (at line 10)
				A foo(Number n);
				^
			Type safety: The return type A for foo(Number) from the type J needs unchecked conversion to conform to T from the type I
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test136() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X extends E {}
				class X2 extends E {
				  @Override public A foo(Number n) { return null; }
				}
				abstract class Y extends X {}
				abstract class D {
				  abstract <T extends Exception & Cloneable> T foo(Number n);
				}
				abstract class E extends D {
				  @Override abstract A foo(Number n);
				}
				abstract class A extends Exception implements Cloneable {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 10)
				@Override abstract A foo(Number n);
				                   ^
			Type safety: The return type A for foo(Number) from the type E needs unchecked conversion to conform to T from the type D
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test137() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X implements J {}
				interface I {
				  <T extends Y<T> & Cloneable> T foo(Number n);
				}
				interface J extends I {
				  XX foo(Number n);
				}
				class Z { }
				class Y <U> extends Z { }
				abstract class XX extends Y<XX> implements Cloneable {}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				XX foo(Number n);
				^^
			Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test138() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X implements J {}
				interface I {
				  <T extends Exception & Cloneable> A<T> foo(Number n);
				}
				interface J extends I {
				  A<XX> foo(Number n);
				}
				class A<T> { }\
				abstract class XX extends Exception implements Cloneable {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				A<XX> foo(Number n);
				^
			Type safety: The return type A<XX> for foo(Number) from the type J needs unchecked conversion to conform to A<Exception&Cloneable> from the type I
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test139() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X implements J {
				  void foo() {}
				  public XX foo(Number n) { return null; }
				}
				interface I {
				  <T extends Exception & Cloneable> T foo(Number n);
				}
				interface J extends I {
				  XX foo(Number n);
				}
				abstract class XX extends Exception implements Cloneable {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				public XX foo(Number n) { return null; }
				       ^^
			Type safety: The return type XX for foo(Number) from the type X needs unchecked conversion to conform to T from the type I
			----------
			2. WARNING in X.java (at line 9)
				XX foo(Number n);
				^^
			Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test140() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X implements J, K {}
				interface I {
				  <T extends Exception & Cloneable> T foo(Number n);
				}
				interface J extends I {
				  XX foo(Number n);
				}
				interface K {
				  NullPointerException foo(Number n);
				}
				abstract class XX extends Exception implements Cloneable {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public abstract class X implements J, K {}
				                      ^
			The return types are incompatible for the inherited methods J.foo(Number), K.foo(Number)
			----------
			2. WARNING in X.java (at line 6)
				XX foo(Number n);
				^^
			Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186457
public void test141() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.nio.charset.Charset;
				import java.nio.charset.CharsetDecoder;
				import java.nio.charset.CharsetEncoder;
				public class X extends Charset {
				  public X(String name, String[] aliases) { super(name, aliases); }
				  @Override public CharsetEncoder newEncoder() { return null;  }
				  @Override public CharsetDecoder newDecoder() { return null;  }
				  @Override public boolean contains(Charset x) { return false; }
				  public int compareTo(Object obj) {
				    return compareTo((Charset) obj);
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				public int compareTo(Object obj) {
				           ^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186457
public void test142() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.nio.charset.Charset;
				import java.nio.charset.CharsetDecoder;
				import java.nio.charset.CharsetEncoder;
				public class X extends Charset {
				  public X(String name, String[] aliases) { super(name, aliases); }
				  public CharsetEncoder newEncoder() { return null;  }
				  public CharsetDecoder newDecoder() { return null;  }
				  public boolean contains(Charset x) { return false; }
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190748
public void test143() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] s) { ((IBase) new Impl()).get(); }
				}
				class Impl extends AImpl implements IBase, IEnhanced {}
				interface IBase {
					IBaseReturn get();
				}
				interface IEnhanced extends IBase {
					IEnhancedReturn get();
				}
				abstract class AImpl {
					public IEnhancedReturn get() { return null; }
				}
				interface IBaseReturn {}
				interface IEnhancedReturn extends IBaseReturn {}"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=194034
// See that this test case exhibits the bug 345947
public void test144() {
	this.runNegativeTest(
		new String[] {
			"PurebredCatShopImpl.java",
			"""
				import java.util.List;
				interface Pet {}
				interface Cat extends Pet {}
				interface PetShop { List<Pet> getPets(); }
				interface CatShop extends PetShop {
					<V extends Pet> List<? extends Cat> getPets();
				}
				interface PurebredCatShop extends CatShop {}
				class CatShopImpl implements CatShop {
					public List<Pet> getPets() { return null; }
				}
				class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {}"""
		},
		"""
			----------
			1. ERROR in PurebredCatShopImpl.java (at line 6)
				<V extends Pet> List<? extends Cat> getPets();
				                                    ^^^^^^^^^
			Name clash: The method getPets() of type CatShop has the same erasure as getPets() of type PetShop but does not override it
			----------
			2. WARNING in PurebredCatShopImpl.java (at line 10)
				public List<Pet> getPets() { return null; }
				       ^^^^
			Type safety: The return type List<Pet> for getPets() from the type CatShopImpl needs unchecked conversion to conform to List<? extends Cat> from the type CatShop
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195468
public void test145() {
	this.runConformTest(
		new String[] {
			"BaseImpl.java",
			"""
				abstract class Base<Tvalue> implements BaseInterface<Tvalue>{ public void setValue(Object object) {} }
				interface BaseInterface<Tvalue> { void setValue(Tvalue object); }
				class BaseImpl extends Base<String> { public void setValue(String object) {} }"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195802
public void test146() {
	this.runConformTest(
		new String[] {
			"BugB.java",
			"""
				abstract class A<K> { void get(K key) {} }
				abstract class B extends A<C> { <S> void get(C<S> type) {} }
				class B2 extends A<C> { <S> void get(C<S> type) {} }
				class BugB extends B {}
				class NonBugB extends B2 {}
				class C<T> {}"""
		},
		""
	);
}
public void test147() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface J<T> { <U1, U2> void foo(T t); }
				class Y<T> { public <U3> void foo(T t) {} }
				abstract class X<T> extends Y<T> implements J<T> {
					@Override public void foo(Object o) {}
				}
				abstract class X1<T> extends Y<T> implements J<T> {
					public <Ignored> void foo(Object o) {}
				}
				abstract class X2<T> extends Y<T> implements J<T> {}
				abstract class X3 extends Y<Number> implements J<String> {}
				abstract class X4 extends Y<Number> implements J<String> {
					@Override public void foo(Number o) {}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				abstract class X1<T> extends Y<T> implements J<T> {
				               ^^
			Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it
			----------
			2. ERROR in X.java (at line 7)
				public <Ignored> void foo(Object o) {}
				                      ^^^^^^^^^^^^^
			Name clash: The method foo(Object) of type X1<T> has the same erasure as foo(T) of type Y<T> but does not override it
			----------
			3. ERROR in X.java (at line 7)
				public <Ignored> void foo(Object o) {}
				                      ^^^^^^^^^^^^^
			Name clash: The method foo(Object) of type X1<T> has the same erasure as foo(T) of type J<T> but does not override it
			----------
			4. ERROR in X.java (at line 9)
				abstract class X2<T> extends Y<T> implements J<T> {}
				               ^^
			Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it
			----------
			5. ERROR in X.java (at line 10)
				abstract class X3 extends Y<Number> implements J<String> {}
				               ^^
			Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it
			----------
			6. ERROR in X.java (at line 11)
				abstract class X4 extends Y<Number> implements J<String> {
				               ^^
			Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204624
public void test148() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				abstract class X { abstract <T extends Object> T go(A<T> a); }
				class Y extends X {
					@Override <T extends Object> T go(A a) { return null; }
				}
				class A<T> {}"""
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 2)\n" +
		"	class Y extends X {\n" +
		"	      ^\n" +
		"The type Y must implement the inherited abstract method X.go(A<T>)\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 3)\n" +
		"	@Override <T extends Object> T go(A a) { return null; }\n" +
		"	                               ^^^^^^^\n" +
		"Name clash: The method go(A) of type Y has the same erasure as go(A<T>) of type X but does not override it\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 3)\n" +
		"	@Override <T extends Object> T go(A a) { return null; }\n" +
		"	                               ^^^^^^^\n" +
		mustOverrideMessage("go(A)", "Y") +
		"----------\n" +
		"4. WARNING in Y.java (at line 3)\n" +
		"	@Override <T extends Object> T go(A a) { return null; }\n" +
		"	                                  ^\n" +
		"A is a raw type. References to generic type A<T> should be parameterized\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=208995
public void test149() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				class A {
					void a(X x) {}
					void b(Y<Integer> y) {}
					static void c(X x) {}
					static void d(Y<Integer> y) {}
				}
				""",
			"B.java",
			"""
				class B extends A {
					static void a(X x) {}
					static void b(Y<String> y) {}
					static void c(X x) {}
					static void d(Y<String> y) {}
				}
				""",
			"B2.java",
			"""
				class B2 extends A {
					static void b(Y<Integer> y) {}
					static void d(Y<Integer> y) {}
				}
				""",
			"C.java",
			"""
				class C extends A {
					@Override void a(X x) {}
					void b(Y<String> y) {}
					void c(X x) {}
					void d(Y<String> y) {}
				}
				""",
			"C2.java",
			"""
				class C2 extends A {
					@Override void b(Y<Integer> y) {}
					void d(Y<Integer> y) {}
				}
				class X {}
				class Y<T> {}"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"""
			----------
			1. ERROR in B.java (at line 2)
				static void a(X x) {}
				            ^^^^^^
			This static method cannot hide the instance method from A
			----------
			----------
			1. ERROR in B2.java (at line 2)
				static void b(Y<Integer> y) {}
				            ^^^^^^^^^^^^^^^
			This static method cannot hide the instance method from A
			----------
			----------
			1. ERROR in C.java (at line 3)
				void b(Y<String> y) {}
				     ^^^^^^^^^^^^^^
			Name clash: The method b(Y<String>) of type C has the same erasure as b(Y<Integer>) of type A but does not override it
			----------
			2. ERROR in C.java (at line 4)
				void c(X x) {}
				     ^^^^^^
			This instance method cannot override the static method from A
			----------
			----------
			1. ERROR in C2.java (at line 3)
				void d(Y<Integer> y) {}
				     ^^^^^^^^^^^^^^^
			This instance method cannot override the static method from A
			----------
			""" :
			"""
				----------
				1. ERROR in B.java (at line 2)
					static void a(X x) {}
					            ^^^^^^
				This static method cannot hide the instance method from A
				----------
				2. ERROR in B.java (at line 3)
					static void b(Y<String> y) {}
					            ^^^^^^^^^^^^^^
				Name clash: The method b(Y<String>) of type B has the same erasure as b(Y<Integer>) of type A but does not hide it
				----------
				3. ERROR in B.java (at line 5)
					static void d(Y<String> y) {}
					            ^^^^^^^^^^^^^^
				Name clash: The method d(Y<String>) of type B has the same erasure as d(Y<Integer>) of type A but does not hide it
				----------
				----------
				1. ERROR in B2.java (at line 2)
					static void b(Y<Integer> y) {}
					            ^^^^^^^^^^^^^^^
				This static method cannot hide the instance method from A
				----------
				----------
				1. ERROR in C.java (at line 3)
					void b(Y<String> y) {}
					     ^^^^^^^^^^^^^^
				Name clash: The method b(Y<String>) of type C has the same erasure as b(Y<Integer>) of type A but does not override it
				----------
				2. ERROR in C.java (at line 4)
					void c(X x) {}
					     ^^^^^^
				This instance method cannot override the static method from A
				----------
				3. ERROR in C.java (at line 5)
					void d(Y<String> y) {}
					     ^^^^^^^^^^^^^^
				Name clash: The method d(Y<String>) of type C has the same erasure as d(Y<Integer>) of type A but does not hide it
				----------
				----------
				1. ERROR in C2.java (at line 3)
					void d(Y<Integer> y) {}
					     ^^^^^^^^^^^^^^^
				This instance method cannot override the static method from A
				----------
				"""
	);
}
public void test150() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					// DOESN\'T Compile
					public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date){
						return null;
					}
					// Doesn\'t compile
					public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date){
						return null;
					}
					// Using vararg trick compiles ok use vararg to differentiate method signature
					public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date, D1 ... notUsed){
						return null;
					}
					// Using vararg trick compiles ok use vararg to differentiate method signature
					public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date, D2 ... notUsed){
						return null;
					}
					class MyT<T>{}
					class D1<T>{}
					class D2<T>{}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date){
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Erasure of method method3(X.D1<String>, X.D1<String>, X.D1<Date>) is the same as another method in type X
			----------
			2. ERROR in X.java (at line 7)
				public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date){
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Erasure of method method3(X.D1<String>, X.D1<String>, X.D1<String>) is the same as another method in type X
			----------
			3. WARNING in X.java (at line 11)
				public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date, D1 ... notUsed){
				                                                                                    ^^
			X.D1 is a raw type. References to generic type X.D1<T> should be parameterized
			----------
			4. WARNING in X.java (at line 15)
				public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date, D2 ... notUsed){
				                                                                            ^^
			X.D2 is a raw type. References to generic type X.D2<T> should be parameterized
			----------
			""");
}
public void test151() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Date;
				
				public class X {
					// Using vararg trick compiles ok use vararg to differentiate method signature
					public static MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date, D1... notUsed) {
						System.out.print("#method3(D1<String>, D1<String>, D1<java.util.Date>, D1[])");
						return null;
					}
				
					// Using vararg trick compiles ok use vararg to differentiate method signature
					public static MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date, D2... notUsed) {
						System.out.print("#method3(D1<String>, D1<String>, D1<java.util.Date>, D2[])");
						return null;
					}
				
					/**
					 * this java main demonstrates that compiler can differentiate between to
					 * the 2 different methods.
					 * @param args
					 */
					public static void main(String[] args) {
						X x = new X();
						D1<String> dString = x.new D1<String>();
						D1<Date> dDate = x.new D1<Date>();
						// calling first defined method
						X.method3(dString, dString, dDate);
						// calling second defined method
						X.method3(dString, dString, dString);
						// / will write out
						// method3 called with this signature: D1<String> harg, D1<String> oarg,
						// D1<java.util.Date> date
						// method3 called with this signature: D1<String> harg, D1<String> oarg,
						// D1<String> date
					}
					class MyT<T> {}
					public class D1<T> {}
					public class D2<T> {}
				}
				"""
		},
		"#method3(D1<String>, D1<String>, D1<java.util.Date>, D1[])#method3(D1<String>, D1<String>, D1<java.util.Date>, D2[])");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=219625
public void test152() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static <T> void feedFoosValueIntoFoo(Foo<T> foo) {
						foo.doSomething(foo.getValue());
					}
					static void testTypedString() {
						ConcreteFoo foo = new ConcreteFoo();
						foo.doSomething(foo.getValue());
					}
					static void testGenericString() {
						feedFoosValueIntoFoo(new ConcreteFoo());
					}
					public static void main(String[] args) {
						testTypedString();
						testGenericString();
						System.out.print(1);
					}
				}
				interface Foo<T> {
					T getValue();
					void doSomething(T o);
				}
				abstract class AbstractFoo<T> implements Foo<T> {
					public void doSomething(String o) {}
				}
				class ConcreteFoo extends AbstractFoo<String> {
					public String getValue() { return null; }
				}"""
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986
public void test153() {
	this.runConformTest(
		new String[] {
			"test/impl/SubOneImpl.java", //--------------------------------------------
			"""
				package test.impl;
				import test.intf.SubTwo;
				public abstract class SubOneImpl extends SuperTypeExtendImpl implements test.intf.SubOne
				{
				    public SubOneImpl plus(test.intf.SubOne attribute)
				    {
				        throw new RuntimeException("foo");
				    }
				    public SubTwoImpl plus(SubTwo attribute)
				    {
				        throw new RuntimeException("foo");
				    }
				}
				""",
			"test/impl/SubSubOneImpl.java", //--------------------------------------------
			"""
				package test.impl;
				public abstract class SubSubOneImpl extends SubOneImpl
				{
				}
				""",
			"test/impl/SubTwoImpl.java", //--------------------------------------------
			"""
				package test.impl;
				import test.intf.SubOne;
				public abstract class SubTwoImpl extends SuperTypeExtendImpl implements
				test.intf.SubTwo
				{
				    public SubTwoImpl plus(SubOne attribute)
				    {
				        throw new RuntimeException("foo");
				    }
				    public SubTwoImpl plus(test.intf.SubTwo attribute)
				    {
				        throw new RuntimeException("foo");
				    }
				}
				""",
			"test/impl/SuperTypeExtend.java", //--------------------------------------------
			"""
				package test.impl;
				import test.intf.SubOne;
				import test.intf.SubTwo;
				public interface SuperTypeExtend extends test.intf.SuperType
				{
				    public SuperTypeExtend plus(SubOne addend);
				    public SuperTypeExtend plus(SubTwo addend);
				}
				""",
			"test/impl/SuperTypeExtendImpl.java", //--------------------------------------------
			"""
				package test.impl;
				public abstract class SuperTypeExtendImpl implements SuperTypeExtend
				{
				}
				""",
			"test/intf/SubOne.java", //--------------------------------------------
			"""
				package test.intf;
				public interface SubOne<Owner> extends SuperType<Owner>
				{
				    public SubOne<Owner> plus(SubOne addend);
				    public SubTwo<Owner> plus(SubTwo addend);
				}
				""",
			"test/intf/SubTwo.java", //--------------------------------------------
			"""
				package test.intf;
				public interface SubTwo<Owner> extends SuperType<Owner>
				{
				    public SubTwo<Owner> plus(SubOne addend);
				    public SubTwo<Owner> plus(SubTwo addend);
				}
				""",
			"test/intf/SuperType.java", //--------------------------------------------
			"""
				package test.intf;
				public interface SuperType<Owner>
				{
				    public SuperType<Owner> plus(SubOne addend);
				    public SuperType<Owner> plus(SubTwo addend);
				}
				""",
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test154() {
	this.runConformTest(
		new String[] {
			"test/impl/SubOneImpl.java", //--------------------------------------------
			"""
				package test.impl;
				public abstract class SubOneImpl extends SuperTypeExtendImpl implements test.impl.SubOne {
					public SubOneImpl plus(test.impl.SubOne attribute) {
						throw new RuntimeException("foo");
					}
					public SubTwoImpl plus(SubTwo attribute) {
						throw new RuntimeException("foo");
					}
				}
				
				abstract class SubSubOneImpl extends SubOneImpl {
				}
				
				abstract class SubTwoImpl extends SuperTypeExtendImpl implements test.impl.SubTwo {
					public SubTwoImpl plus(SubOne attribute) {
						throw new RuntimeException("foo");
					}
					public SubTwoImpl plus(test.impl.SubTwo attribute) {
						throw new RuntimeException("foo");
					}
				}
				
				interface SuperTypeExtend extends test.impl.SuperType {
					public SuperTypeExtend plus(SubOne addend);
					public SuperTypeExtend plus(SubTwo addend);
				}
				
				abstract class SuperTypeExtendImpl implements SuperTypeExtend {
				}
				
				interface SubOne<Owner> extends SuperType<Owner> {
					public SubOne<Owner> plus(SubOne addend);
					public SubTwo<Owner> plus(SubTwo addend);
				}
				
				interface SubTwo<Owner> extends SuperType<Owner> {
					public SubTwo<Owner> plus(SubOne addend);
					public SubTwo<Owner> plus(SubTwo addend);
				}
				
				interface SuperType<Owner> {
					public SuperType<Owner> plus(SubOne addend);
					public SuperType<Owner> plus(SubTwo addend);
				}
				""",
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test155() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"""
				class A {}
				class B {}
				interface I {
					A foo();
				}
				interface J {
					B foo();
				}
				public abstract class X implements I, J {
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				public abstract class X implements I, J {
				                      ^
			The return types are incompatible for the inherited methods I.foo(), J.foo()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test156() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"""
				class Common {}
				class A extends Common {}
				class B extends Common {}
				interface I {
					A foo();
				}
				interface J {
					B foo();
				}
				public abstract class X implements I, J {
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				public abstract class X implements I, J {
				                      ^
			The return types are incompatible for the inherited methods I.foo(), J.foo()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test157() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"""
				interface A {
					A foo();
				}
				interface B {
					B foo();
				}
				interface C extends A, B {}
				
				class Root {
					public C foo() { return null; }
				}
				public abstract class X extends Root implements A, B {
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				interface C extends A, B {}
				          ^
			The return types are incompatible for the inherited methods A.foo(), B.foo()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test158() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"""
				import java.io.Serializable;
				
				interface AFoo {\s
					Serializable foo();
					Serializable bar();
				}
				interface BFoo {\s
					Cloneable foo();\s
					Cloneable bar();\s
				}
				
				interface C extends Serializable, Cloneable {}
				
				class Root {
					public C foo() { return null; }
				}
				public abstract class X extends Root implements AFoo, BFoo {
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 17)
				public abstract class X extends Root implements AFoo, BFoo {
				                      ^
			The return types are incompatible for the inherited methods AFoo.bar(), BFoo.bar()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test159() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"""
				import java.io.Serializable;
				
				interface AFoo {\s
					Serializable foo();
					Serializable bar();
				}
				interface BFoo {\s
					Cloneable foo();\s
					Cloneable bar();\s
				}
				interface C extends Serializable, Cloneable {}
				class Root {
					public C foo() { return null; }
				}
				public abstract class X extends Root implements AFoo, BFoo {}
				abstract class Y extends X {}
				class Z extends X {}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				public abstract class X extends Root implements AFoo, BFoo {}
				                      ^
			The return types are incompatible for the inherited methods AFoo.bar(), BFoo.bar()
			----------
			2. ERROR in X.java (at line 16)
				abstract class Y extends X {}
				               ^
			The return types are incompatible for the inherited methods AFoo.bar(), BFoo.bar()
			----------
			3. ERROR in X.java (at line 17)
				class Z extends X {}
				      ^
			The type Z must implement the inherited abstract method BFoo.bar()
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=208010
public void test160() {
	this.runConformTest(
		new String[] {
			"bar/X.java", //--------------------------------------------
			"""
				package bar;\
				public class X {
					static void foo() {}
				}""",
			"foo/Y.java", //--------------------------------------------
			"""
				package foo;\
				public class Y extends bar.X {
					static void foo() {}
				}""",
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185
public void test161() {
	this.runConformTest(
		new String[] {
			"Concrete.java",
			"""
				abstract class SuperAbstract<Owner, Type> {
					abstract Object foo(Type other);
				}
				abstract class HalfGenericSuper<Owner> extends SuperAbstract<Owner, String> {
					@Override abstract Object foo(String other);
				}
				abstract class AbstractImpl<Owner> extends HalfGenericSuper<Owner> {
					@Override Object foo(String other) { return null; }
				}
				class Concrete extends AbstractImpl{}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant
public void test162() {
	this.runConformTest(
		new String[] {
			"Concrete.java",
			"""
				abstract class SuperAbstract<Owner, Type> {
					abstract Object foo(Type other);
				}
				class HalfGenericSuper<Owner> extends SuperAbstract<Owner, String> {
					@Override Object foo(String other) { return null; }
				}
				abstract class AbstractImpl<Owner> extends HalfGenericSuper<Owner> {}
				class HalfConcrete extends HalfGenericSuper {}
				class Concrete extends AbstractImpl{}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant return types
public void test163() {
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"""
				abstract class SuperAbstract<Owner, Type> {
					abstract Type foo(Type other);
				}
				class HalfGenericSuper<Owner> extends SuperAbstract<Owner, String> {
					@Override Object foo(String other) { return null; }
				}
				class Concrete extends HalfGenericSuper{}"""
		},
		"""
			----------
			1. ERROR in Concrete.java (at line 5)
				@Override Object foo(String other) { return null; }
				          ^^^^^^
			The return type is incompatible with SuperAbstract<Owner,String>.foo(String)
			----------
			2. WARNING in Concrete.java (at line 7)
				class Concrete extends HalfGenericSuper{}
				                       ^^^^^^^^^^^^^^^^
			HalfGenericSuper is a raw type. References to generic type HalfGenericSuper<Owner> should be parameterized
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant return types
public void test164() {
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"""
				interface I<Owner, Type> {
					Type foo(Type other);
					Owner foo2(Type other);
					Object foo3(Type other);
				}
				class HalfGenericSuper {
					public Object foo(String other) { return null; }
					public Integer foo2(String other) { return null; }
					public String foo3(String other) { return null; }
				}
				class HalfConcrete extends HalfGenericSuper {}
				class Concrete extends HalfConcrete implements I<Object, String> {}"""
		},
		"""
			----------
			1. ERROR in Concrete.java (at line 12)
				class Concrete extends HalfConcrete implements I<Object, String> {}
				      ^^^^^^^^
			The type Concrete must implement the inherited abstract method I<Object,String>.foo(String) to override HalfGenericSuper.foo(String)
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant return types
public void test165() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { void foo() {} }\n" +
			"class Y extends X { @Override int foo() { return 1; } }"
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends X { @Override int foo() { return 1; } }
				                              ^^^
			The return type is incompatible with X.foo()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=238014
public void test166() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X extends A implements I<String> {}
				interface I<T> { void foo(T item); }
				class A {
					public void foo(Object item) {}
					public void foo(String item) {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X extends A implements I<String> {}
				      ^
			Name clash: The method foo(Object) of type A has the same erasure as foo(T) of type I<T> but does not override it
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=238817
public void test167() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X implements I<String>, J<String> {
					public <T3> void foo(T3 t, String s) {}
				}
				interface I<U1> { <T1> void foo(T1 t, U1 u); }
				interface J<U2> { <T2> void foo(T2 t, U2 u); }
				"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236096
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> extends Y {
					@Override <V> void foo(M m) { }
					@Override <V> M bar() { return null; }
				}
				class Y<T> {
					class M<V> {}
					<V> void foo(M<V> m) {}
					<V> M<V> bar() { return null; }
				}"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	class X<T> extends Y {\n" +
		"	                   ^\n" +
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	@Override <V> void foo(M m) { }\n" +
		"	                   ^^^^^^^^\n" +
		"Name clash: The method foo(Y.M) of type X<T> has the same erasure as foo(Y.M) of type Y but does not override it\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	@Override <V> void foo(M m) { }\n" +
		"	                   ^^^^^^^^\n" +
		mustOverrideMessage("foo(Y.M)", "X<T>") +
		"----------\n" +
		"4. WARNING in X.java (at line 2)\n" +
		"	@Override <V> void foo(M m) { }\n" +
		"	                       ^\n" +
		"Y.M is a raw type. References to generic type Y<T>.M<V> should be parameterized\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 3)\n" +
		"	@Override <V> M bar() { return null; }\n" +
		"	              ^\n" +
		"Y.M is a raw type. References to generic type Y<T>.M<V> should be parameterized\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 3)\n" +
		"	@Override <V> M bar() { return null; }\n" +
		"	                ^^^^^\n" +
		"Name clash: The method bar() of type X<T> has the same erasure as bar() of type Y but does not override it\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 3)\n" +
		"	@Override <V> M bar() { return null; }\n" +
		"	                ^^^^^\n" +
		mustOverrideMessage("bar()", "X<T>") +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=243820
public void test169() {
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				class X<T> {
					interface I<S> {}
					interface J { A foo(A a, I<String> i); }
					static class A {}
					static class B implements J {
						public R foo(A a, I i) { return null; }
					}
				}
				class R<T> extends X.A {}"""
		},
		null,
		null,
		"""
			----------
			1. WARNING in X.java (at line 6)
				public R foo(A a, I i) { return null; }
				       ^
			R is a raw type. References to generic type R<T> should be parameterized
			----------
			2. WARNING in X.java (at line 6)
				public R foo(A a, I i) { return null; }
				                  ^
			X.I is a raw type. References to generic type X.I<S> should be parameterized
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=243820
public void test169a() {
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				class X<T> {
					abstract class B implements J {
						public R foo(X<String>.B b, I i) { return null; }
					}
				}
				interface I<S> {}
				interface J { A foo(A a, I<String> i); }
				class A {}
				class R<T> extends A {}"""
		},
		null,
		null,
		"""
			----------
			1. WARNING in X.java (at line 3)
				public R foo(X<String>.B b, I i) { return null; }
				       ^
			R is a raw type. References to generic type R<T> should be parameterized
			----------
			2. WARNING in X.java (at line 3)
				public R foo(X<String>.B b, I i) { return null; }
				                            ^
			I is a raw type. References to generic type I<S> should be parameterized
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066
public void test170() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"class X { synchronized void foo() {} }\n" +
			"class Y extends X { @Override void foo() { } }"
		},
		null, // libs
		options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				class Y extends X { @Override void foo() { } }
				                                   ^^^^^
			The method Y.foo() is overriding a synchronized method without being synchronized
			----------
			""",
		Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066 - variation
public void test171() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				public enum X {
				  FOO { @Override void foo() { super.foo(); } };
				  synchronized void foo() { }
				}"""
		},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				FOO { @Override void foo() { super.foo(); } };
				                     ^^^^^
			The method new X(){}.foo() is overriding a synchronized method without being synchronized
			----------
			""",
		Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066 - variation
public void test172() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				public class X {
				  void bar() { new X() { @Override void foo() {} }; }
				  synchronized void foo() { }
				}"""
		},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 2)
				void bar() { new X() { @Override void foo() {} }; }
				                                      ^^^^^
			The method new X(){}.foo() is overriding a synchronized method without being synchronized
			----------
			""",
		Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066 - variation
public void test173() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				public class X { synchronized void foo() {} }
				class Y extends X {}
				class Z extends Y { @Override void foo() {} }
				"""
		},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				class Z extends Y { @Override void foo() {} }
				                                   ^^^^^
			The method Z.foo() is overriding a synchronized method without being synchronized
			----------
			""",
		Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249140
public void test174() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X extends Y implements I { }
				abstract class Y { public abstract Object m(); }
				abstract class A implements I, J { }
				abstract class B implements J, I { }
				interface I { String m(); }
				interface J { Object m(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				class X extends Y implements I { }
				      ^
			The type X must implement the inherited abstract method I.m() to override Y.m()
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=38751
public void test175() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingHashCodeMethod, CompilerOptions.WARNING);
	this.runNegativeTest(
		false,
		new String[] {
			"A.java",
			"""
				class A {
					@Override public boolean equals(Object o) { return true; }
				}"""
		},
		null,
		options,
		"""
			----------
			1. WARNING in A.java (at line 1)
				class A {
				      ^
			The type A should also implement hashCode() since it overrides Object.equals()
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=38751
public void test176() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingHashCodeMethod, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				class A {
					@Override public boolean equals(Object o) { return true; }
					@Override public int hashCode() { return 1; }
				}
				class B extends A {
					@Override public boolean equals(Object o) { return false; }
				}"""
		},
		"",
	null,
	false,
	options);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251091
// Srikanth, Aug 10th 2010. This test does not elicit any name clash error from javac 5 or javac6
// javac7 reports "X.java:7: name clash: foo(Collection<?>) in X and foo(Collection) in A have the
// same erasure, yet neither overrides the other"
// After the fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=322001, we match
// JDK7 (7b100) behavior. (earlier we would issue an extra name clash)
public void test177() {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel >= ClassFileConstants.JDK1_6) { // see test187()
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 3)
						class A extends LinkedHashMap {
						      ^
					The serializable class A does not declare a static final serialVersionUID field of type long
					----------
					2. WARNING in X.java (at line 3)
						class A extends LinkedHashMap {
						                ^^^^^^^^^^^^^
					LinkedHashMap is a raw type. References to generic type LinkedHashMap<K,V> should be parameterized
					----------
					3. WARNING in X.java (at line 4)
						public A foo(Collection c) { return this; }
						             ^^^^^^^^^^
					Collection is a raw type. References to generic type Collection<E> should be parameterized
					----------
					4. WARNING in X.java (at line 6)
						class X extends A implements I {
						      ^
					The serializable class X does not declare a static final serialVersionUID field of type long
					----------
					5. WARNING in X.java (at line 7)
						@Override public X foo(Collection<?> c) { return this; }
						                   ^^^^^^^^^^^^^^^^^^^^
					Name clash: The method foo(Collection<?>) of type X has the same erasure as foo(Collection) of type A but does not override it
					----------
					""":
					"""
						----------
						1. WARNING in X.java (at line 3)
							class A extends LinkedHashMap {
							      ^
						The serializable class A does not declare a static final serialVersionUID field of type long
						----------
						2. WARNING in X.java (at line 3)
							class A extends LinkedHashMap {
							                ^^^^^^^^^^^^^
						LinkedHashMap is a raw type. References to generic type LinkedHashMap<K,V> should be parameterized
						----------
						3. WARNING in X.java (at line 4)
							public A foo(Collection c) { return this; }
							             ^^^^^^^^^^
						Collection is a raw type. References to generic type Collection<E> should be parameterized
						----------
						4. WARNING in X.java (at line 6)
							class X extends A implements I {
							      ^
						The serializable class X does not declare a static final serialVersionUID field of type long
						----------
						5. ERROR in X.java (at line 7)
							@Override public X foo(Collection<?> c) { return this; }
							                   ^^^^^^^^^^^^^^^^^^^^
						Name clash: The method foo(Collection<?>) of type X has the same erasure as foo(Collection) of type A but does not override it
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					interface I { I foo(Collection<?> c); }
					class A extends LinkedHashMap {
						public A foo(Collection c) { return this; }
					}
					class X extends A implements I {
						@Override public X foo(Collection<?> c) { return this; }
					}"""
			},
			expectedCompilerLog
		);
	} else {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					interface I { I foo(Collection<?> c); }
					class A extends LinkedHashMap {
						public A foo(Collection c) { return this; }
					}
					class X extends A implements I {
						@Override public X foo(Collection<?> c) { return this; }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					class A extends LinkedHashMap {
					      ^
				The serializable class A does not declare a static final serialVersionUID field of type long
				----------
				2. WARNING in X.java (at line 3)
					class A extends LinkedHashMap {
					                ^^^^^^^^^^^^^
				LinkedHashMap is a raw type. References to generic type LinkedHashMap<K,V> should be parameterized
				----------
				3. WARNING in X.java (at line 4)
					public A foo(Collection c) { return this; }
					             ^^^^^^^^^^
				Collection is a raw type. References to generic type Collection<E> should be parameterized
				----------
				4. WARNING in X.java (at line 6)
					class X extends A implements I {
					      ^
				The serializable class X does not declare a static final serialVersionUID field of type long
				----------
				5. ERROR in X.java (at line 7)
					@Override public X foo(Collection<?> c) { return this; }
					                   ^^^^^^^^^^^^^^^^^^^^
				Name clash: The method foo(Collection<?>) of type X has the same erasure as foo(Collection) of type A but does not override it
				----------
				6. ERROR in X.java (at line 7)
					@Override public X foo(Collection<?> c) { return this; }
					                   ^^^^^^^^^^^^^^^^^^^^
				The method foo(Collection<?>) of type X must override a superclass method
				----------
				"""
		);
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=241821
public void test178() {
	this.runConformTest(
		new String[] {
			"I.java",
			"""
				import java.util.*;
				interface I<E> extends I1<E>, I2<E>, I3<E> {}
				interface I1<E> { List<E> m(); }
				interface I2<E> { Queue<E> m(); }
				interface I3<E> { LinkedList<E> m(); }"""
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=163093
public void test179() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Adaptable {
					public Object getAdapter(Class clazz);\t
				}
				
				public class X implements Adaptable {
					@Override
					public Object getAdapter(Class<?> clazz) {
						return null;
					}
				}
				"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public Object getAdapter(Class clazz);	\n" +
		"	                         ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	public class X implements Adaptable {\n" +
		"	             ^\n" +
		"The type X must implement the inherited abstract method Adaptable.getAdapter(Class)\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	public Object getAdapter(Class<?> clazz) {\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Name clash: The method getAdapter(Class<?>) of type X has the same erasure as getAdapter(Class) of type Adaptable but does not override it\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	public Object getAdapter(Class<?> clazz) {\n" +
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		mustOverrideMessage("getAdapter(Class<?>)", "X") +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255035
public void test180() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class S {
					String foo() { return null; }
				}
				class X extends S {
					foo() { return null; }
					@Override String foo() { return null; }
					Number foo() { return null; }
					void test() { foo(); }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				foo() { return null; }
				^^^^^
			Return type for the method is missing
			----------
			2. ERROR in X.java (at line 5)
				foo() { return null; }
				^^^^^
			Duplicate method foo() in type X
			----------
			3. ERROR in X.java (at line 6)
				@Override String foo() { return null; }
				                 ^^^^^
			Duplicate method foo() in type X
			----------
			4. ERROR in X.java (at line 7)
				Number foo() { return null; }
				       ^^^^^
			Duplicate method foo() in type X
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249134
public void test181() {
	this.runNegativeTest(
		new String[] {
			"I.java",
			"""
				interface I {
					String m();
					Object n();
					String o();
					Object p();
				}
				abstract class A {
					public abstract Object m();
					public abstract String n();
					abstract Object o();
					abstract String p();
				}
				class A2 {
					public abstract Object m();
					public abstract String n();
					abstract Object o();
					abstract String p();
				}
				""",
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"""
			----------
			1. ERROR in I.java (at line 13)
				class A2 {
				      ^^
			The type A2 must be an abstract class to define abstract methods
			----------
			2. ERROR in I.java (at line 14)
				public abstract Object m();
				                       ^^^
			The abstract method m in type A2 can only be defined by an abstract class
			----------
			3. ERROR in I.java (at line 15)
				public abstract String n();
				                       ^^^
			The abstract method n in type A2 can only be defined by an abstract class
			----------
			4. ERROR in I.java (at line 16)
				abstract Object o();
				                ^^^
			The abstract method o in type A2 can only be defined by an abstract class
			----------
			5. ERROR in I.java (at line 17)
				abstract String p();
				                ^^^
			The abstract method p in type A2 can only be defined by an abstract class
			----------
			----------
			1. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method A.p()
			----------
			2. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method I.o() to override A.o()
			----------
			3. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method A.n()
			----------
			4. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method I.m() to override A.m()
			----------
			----------
			1. ERROR in B2.java (at line 1)
				class B2 extends A2 implements I {}
				      ^^
			The type B2 must implement the inherited abstract method I.o() to override A2.o()
			----------
			2. ERROR in B2.java (at line 1)
				class B2 extends A2 implements I {}
				      ^^
			The type B2 must implement the inherited abstract method I.m() to override A2.m()
			----------
			""",
		null,
		true,
		null,
		true,
		false,
		false
	);
	this.runNegativeTest(
		new String[] {
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"""
			----------
			1. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method A.p()
			----------
			2. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method I.o() to override A.o()
			----------
			3. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method A.n()
			----------
			4. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method I.m() to override A.m()
			----------
			----------
			1. ERROR in B2.java (at line 1)
				class B2 extends A2 implements I {}
				      ^^
			The type B2 must implement the inherited abstract method I.o() to override A2.o()
			----------
			2. ERROR in B2.java (at line 1)
				class B2 extends A2 implements I {}
				      ^^
			The type B2 must implement the inherited abstract method I.m() to override A2.m()
			----------
			""",
		null,
		false
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249134
public void test182() {
	this.runNegativeTest(
		new String[] {
			"I.java",
			"""
				interface I {
					String m();
					Object n();
				}
				class A {
					public Object m() { return null; }
					public String n() { return null; }
				}
				abstract class A2 {
					public Object m() { return null; }
					public String n() { return null; }
				}
				""",
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"""
			----------
			1. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method I.m() to override A.m()
			----------
			----------
			1. ERROR in B2.java (at line 1)
				class B2 extends A2 implements I {}
				      ^^
			The type B2 must implement the inherited abstract method I.m() to override A2.m()
			----------
			"""
	);
	this.runNegativeTest(
		new String[] {
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"""
			----------
			1. ERROR in B.java (at line 1)
				class B extends A implements I {}
				      ^
			The type B must implement the inherited abstract method I.m() to override A.m()
			----------
			----------
			1. ERROR in B2.java (at line 1)
				class B2 extends A2 implements I {}
				      ^^
			The type B2 must implement the inherited abstract method I.m() to override A2.m()
			----------
			""",
		null,
		false
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262208
public void test183() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class XX {
					<T extends C, S extends G<T>> void a(S gC) {}
					<T extends C, S extends G<T>> void b(T c) {}
					<T extends C> void c(G<T> gC) {}
					<T extends C, S extends G<T>> void d(S gC) {}
				}
				class X extends XX {
					@Override void a(G g) {}
					@Override void b(C c) {}
					@Override void c(G g) {}
					@Override <T extends C, S extends G<C>> void d(S gc) {}
				}
				class C {}
				class G<T2> {}"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	@Override void a(G g) {}\n" +
		"	                 ^\n" +
		"G is a raw type. References to generic type G<T2> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	@Override void c(G g) {}\n" +
		"	                 ^\n" +
		"G is a raw type. References to generic type G<T2> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	@Override <T extends C, S extends G<C>> void d(S gc) {}\n" +
		"	                                             ^^^^^^^\n" +
		"Name clash: The method d(S) of type X has the same erasure as d(S) of type XX but does not override it\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	@Override <T extends C, S extends G<C>> void d(S gc) {}\n" +
		"	                                             ^^^^^^^\n" +
		mustOverrideMessage("d(S)", "X") +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264881
public void test184() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				class A<U extends Number> {
					<T extends A<Number>> T a() { return null; }
					<T extends Number> U num() { return null; }
					<T> T x() { return null; }
					<T extends Number> T y() { return null; }
					<T extends Integer> T z() { return null; }
				}
				class B extends A<Double> {
					@Override A a() { return null; }
					@Override Double num() { return 1.0; }
					@Override Integer x() { return 1; }
					@Override Integer y() { return 1; }
					@Override Integer z() { return 1; }
				}
				class C extends A {
					@Override A a() { return null; }
					@Override Double num() { return 1.0; }
					@Override Integer x() { return 1; }
					@Override Integer y() { return 1; }
					@Override Integer z() { return 1; }
				}
				class M {
					<T extends M> Object m(Class<T> c) { return null; }
					<T extends M> Object n(Class<T> c) { return null; }
				}
				class N<V> extends M {
					@Override <T extends M> T m(Class<T> c) { return null; }
					@Override <T extends M> V n(Class<T> c) { return null; }
				}"""
		},
		"""
			----------
			1. WARNING in A.java (at line 6)
				<T extends Integer> T z() { return null; }
				           ^^^^^^^
			The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
			----------
			2. WARNING in A.java (at line 9)
				@Override A a() { return null; }
				          ^
			A is a raw type. References to generic type A<U> should be parameterized
			----------
			3. WARNING in A.java (at line 9)
				@Override A a() { return null; }
				          ^
			Type safety: The return type A for a() from the type B needs unchecked conversion to conform to T from the type A<U>
			----------
			4. WARNING in A.java (at line 11)
				@Override Integer x() { return 1; }
				          ^^^^^^^
			Type safety: The return type Integer for x() from the type B needs unchecked conversion to conform to T from the type A<U>
			----------
			5. WARNING in A.java (at line 12)
				@Override Integer y() { return 1; }
				          ^^^^^^^
			Type safety: The return type Integer for y() from the type B needs unchecked conversion to conform to T from the type A<U>
			----------
			6. WARNING in A.java (at line 13)
				@Override Integer z() { return 1; }
				          ^^^^^^^
			Type safety: The return type Integer for z() from the type B needs unchecked conversion to conform to T from the type A<U>
			----------
			7. WARNING in A.java (at line 15)
				class C extends A {
				                ^
			A is a raw type. References to generic type A<U> should be parameterized
			----------
			8. WARNING in A.java (at line 16)
				@Override A a() { return null; }
				          ^
			A is a raw type. References to generic type A<U> should be parameterized
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=267088
public void test185() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				interface I { I hello(); }
				interface J { J hello(); }
				class A implements I, J {}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 3)
				class A implements I, J {}
				      ^
			The type A must implement the inherited abstract method J.hello()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=271303
public void test186() {
	this.runNegativeTest(
		false,
		new String[] {
			"p1/A.java",
			"package p1;\n" +
			"public class A { void m() {} }\n",
			"p2/B.java",
			"package p2;\n" +
			"public class B extends p1.A { void m() {} }\n",
			"p1/C.java",
			"package p1;\n" +
			"public class C extends p2.B { @Override void m() {} }"
		},
		null,
		null,
		"""
			----------
			1. WARNING in p2\\B.java (at line 2)
				public class B extends p1.A { void m() {} }
				                                   ^^^
			The method B.m() does not override the inherited method from A since it is private to a different package
			----------
			----------
			1. WARNING in p1\\C.java (at line 2)
				public class C extends p2.B { @Override void m() {} }
				                                             ^^^
			The method C.m() does not override the inherited method from B since it is private to a different package
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings
	);
}
// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=?
public void test187() {
	String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6 )?
			"""
				----------
				1. WARNING in X.java (at line 6)
					double f(List<Integer> l) {return 0;}
					       ^^^^^^^^^^^^^^^^^^
				Name clash: The method f(List<Integer>) of type Y has the same erasure as f(List<String>) of type X but does not override it
				----------
				2. WARNING in X.java (at line 13)
					int f(List<String> l) {return 0;}
					    ^^^^^^^^^^^^^^^^^
				Erasure of method f(List<String>) is the same as another method in type XX
				----------
				3. WARNING in X.java (at line 14)
					double f(List<Integer> l) {return 0;}
					       ^^^^^^^^^^^^^^^^^^
				Erasure of method f(List<Integer>) is the same as another method in type XX
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 6)
						double f(List<Integer> l) {return 0;}
						       ^^^^^^^^^^^^^^^^^^
					Name clash: The method f(List<Integer>) of type Y has the same erasure as f(List<String>) of type X but does not override it
					----------
					2. ERROR in X.java (at line 13)
						int f(List<String> l) {return 0;}
						    ^^^^^^^^^^^^^^^^^
					Erasure of method f(List<String>) is the same as another method in type XX
					----------
					3. ERROR in X.java (at line 14)
						double f(List<Integer> l) {return 0;}
						       ^^^^^^^^^^^^^^^^^^
					Erasure of method f(List<Integer>) is the same as another method in type XX
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				class X {
				    int f(List<String> l) {return 0;}
				}
				class Y extends X {
				    double f(List<Integer> l) {return 0;}
				}
				interface I {
					double f(List<Integer> l);
				}
				abstract class Z extends X implements I {}
				class XX {
				    int f(List<String> l) {return 0;}
				double f(List<Integer> l) {return 0;}
				}"""
		},
		expectedCompilerLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=279836
public void test188() {
	this.runNegativeTest(
		false,
		new String[] {
			"Y.java",
			"""
				abstract class Y<T extends Number> implements I<T> {
					public T get(T element) { return null; }
				}
				interface I<T> { T get(T element); }
				class Z extends Y {}"""
		},
		null,
		null,
		"""
			----------
			1. WARNING in Y.java (at line 5)
				class Z extends Y {}
				                ^
			Y is a raw type. References to generic type Y<T> should be parameterized
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284431
public void test189() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Interface {
				    void foo() throws CloneNotSupportedException, InterruptedException;
				}
				abstract class AbstractClass1 {
				    public abstract void foo() throws ClassNotFoundException, CloneNotSupportedException;
				}
				abstract class AbstractClass2 extends AbstractClass1  implements Interface {
					void bar() {
				        try {
				        	foo();
				        } catch (CloneNotSupportedException e) {
				        }
				    }
				}
				
				class X extends AbstractClass2 {
					@Override
					public void foo() throws CloneNotSupportedException {
					}
				}
				class Y extends AbstractClass2 {
					@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException {}
				}
				class Z extends AbstractClass2 {
					@Override public void foo() throws CloneNotSupportedException, InterruptedException {}
				}
				class All extends AbstractClass2 {
					@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException, InterruptedException {}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 22)
				@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException {}
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Exception ClassNotFoundException is not compatible with throws clause in Interface.foo()
			----------
			2. ERROR in X.java (at line 25)
				@Override public void foo() throws CloneNotSupportedException, InterruptedException {}
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Exception InterruptedException is not compatible with throws clause in AbstractClass1.foo()
			----------
			3. ERROR in X.java (at line 28)
				@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException, InterruptedException {}
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Exception ClassNotFoundException is not compatible with throws clause in Interface.foo()
			----------
			4. ERROR in X.java (at line 28)
				@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException, InterruptedException {}
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Exception InterruptedException is not compatible with throws clause in AbstractClass1.foo()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test190() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					@Override public int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				@Override public int foo() { return 0; }
				                 ^^^
			The return type is incompatible with A.foo()
			----------
			----------
			1. WARNING in p2\\C.java (at line 3)
				public int foo() { return 1; }
				           ^^^^^
			The method C.foo() does not override the inherited method from A since it is private to a different package
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test191() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					static void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					public static int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public static int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				public static int foo() { return 0; }
				              ^^^
			The return type is incompatible with A.foo()
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test192() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					public static int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public static int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				public static int foo() { return 0; }
				                  ^^^^^
			This static method cannot hide the instance method from A
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test193() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					@Override public int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public static int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				@Override public int foo() { return 0; }
				                 ^^^
			The return type is incompatible with A.foo()
			----------
			2. ERROR in p1\\B.java (at line 3)
				@Override public int foo() { return 0; }
				                     ^^^^^
			This instance method cannot override the static method from C
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test194() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					static void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					public int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public static int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				public int foo() { return 0; }
				           ^^^^^
			This instance method cannot override the static method from A
			----------
			2. ERROR in p1\\B.java (at line 3)
				public int foo() { return 0; }
				           ^^^^^
			This instance method cannot override the static method from C
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test195() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					static void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					@Override public int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				@Override public int foo() { return 0; }
				                     ^^^^^
			This instance method cannot override the static method from A
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test196() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					static void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					public static int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				public static int foo() { return 0; }
				              ^^^
			The return type is incompatible with A.foo()
			----------
			2. ERROR in p1\\B.java (at line 3)
				public static int foo() { return 0; }
				                  ^^^^^
			This static method cannot hide the instance method from C
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test197() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					void foo() {}
				}""",
			"p1/B.java",
			"""
				package p1;
				public class B extends p2.C {
					public static int foo() { return 0; }
				}""",
			"p2/C.java",
			"""
				package p2;
				public class C extends p1.A {
					public int foo() { return 1; }
				}"""
		},
		"""
			----------
			1. ERROR in p1\\B.java (at line 3)
				public static int foo() { return 0; }
				                  ^^^^^
			This static method cannot hide the instance method from A
			----------
			2. ERROR in p1\\B.java (at line 3)
				public static int foo() { return 0; }
				                  ^^^^^
			This static method cannot hide the instance method from C
			----------
			----------
			1. WARNING in p2\\C.java (at line 3)
				public int foo() { return 1; }
				           ^^^^^
			The method C.foo() does not override the inherited method from A since it is private to a different package
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284948
public void test198() {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6) return;

	this.runConformTest(
		new String[] {
			"MyAnnotation.java",
			"""
				@interface MyAnnotation {
				    MyEnum value();
				}""",
			"MyClass.java",
			"""
				public class MyClass implements MyInterface {
				    @Override public void foo() {}
				}""",
			"MyEnum.java",
			"""
				enum MyEnum implements Runnable {
					G {
						@Override public void methodA() {
							new Runnable() {
								@Override public void run() {}
							};
						}
					},
					D {
						@Override public void methodA() {}
					},
					A {
						@Override public void methodA() {}
						@Override public void methodB() {}
					},
					B {
						@Override public void methodA() {}
					},
					C {
						@Override public void methodA() {}
						@Override public void methodB() {}
					},
					E {
						@Override public void methodA() {}
					},
					F {
						@Override public void methodA() {}
					};
					private MyEnum() {}
					public void methodA() {}
					public void methodB() {}
					@Override public void run() {}
				}""",
			"MyInterface.java",
			"""
				interface MyInterface {
				    @MyAnnotation(MyEnum.D) public void foo();
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284785
public void test199() {
	this.runConformTest(
		new String[] {
			"Bar.java",
			"""
				public interface Bar {
					void addError(String message, Object... arguments);
					void addError(Throwable t);
				}""",
		},
		""
	);
	this.runConformTest(
		false,
		new String[] {
			"Foo.java",
			"""
				public class Foo {
					void bar(Bar bar) {
						bar.addError("g");
					}
				}"""
		},
		"",
		"",
		"",
		JavacTestOptions.SKIP
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285088
public void test200() {
	String errorMessage =
				"""
		----------
		1. ERROR in X.java (at line 3)
			int foo(Collection bar) { return 0; }
			    ^^^^^^^^^^^^^^^^^^^
		Erasure of method foo(Collection) is the same as another method in type X
		----------
		2. WARNING in X.java (at line 3)
			int foo(Collection bar) { return 0; }
			        ^^^^^^^^^^
		Collection is a raw type. References to generic type Collection<E> should be parameterized
		----------
		3. ERROR in X.java (at line 4)
			double foo(Collection<String> bar) {return 0; }
			       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Erasure of method foo(Collection<String>) is the same as another method in type X
		----------
		""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				class X {
					int foo(Collection bar) { return 0; }
					double foo(Collection<String> bar) {return 0; }
				}"""
		},
		errorMessage
	);
/* javac 7
X.java:4: foo(Collection) is already defined in X
        double foo(Collection<String> bar) {return 0; }
               ^
1 error
 */
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286228
public void test201() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				interface I {}
				interface J<T1> { J<T1> get(); }
				interface K<T2 extends J<? extends I>> { T2 get(); }
				interface A<T3 extends K<T3> & J<? extends I>> extends J<I> {}
				interface B<T4 extends J<? extends I> & K<T4>> extends J<I> {}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284280
public void test202() {
	this.runConformTest(
		new String[] {
			"SubClass.java",
			"""
				interface MyInterface <T0 extends Object> {
					String testMe(T0 t);
				}
				abstract class AbstractSuperClass<T1 extends AbstractSuperClass> implements MyInterface<T1> {
					public String testMe(T1 o) { return null; }
				}
				class SubClass extends AbstractSuperClass<SubClass> {
				   @Override public String testMe(SubClass o) {
				      return super.testMe(o);
				   }
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=292240
public void test203() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {}
				interface Y<T extends I> extends java.util.Comparator<T> {
					public int compare(T o1, T o2);
				}
				class X implements Y {
					public int compare(Object o1, Object o2) {
						return compare((I) o1, (I) o2);
					}
					public int compare(I o1, I o2) { return 0; }
				}"""
		},
		""
	);
}
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=293615 (bad name clash error)
// No user vs user clash or user vs synthetic clash in this test
public void test204() {
	this.runConformTest(
		new String[] {
			"OverrideBug.java",
			"""
				import java.util.List;
				interface Map<K, V> {
					public V put(K key, V value);
				}
				public class OverrideBug<K, V> implements Map<K, List<V>> {
				public List<V> put(final K arg0, final List<V> arg1) {
				    return null;
				}
				public List<V> put(final K arg0, final V arg1) {
				    return null;
				}
				}"""
		},
		"");
}
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=293615 (bad name clash error)
// verify that we report user vs bridge clash properly.
public void test204a() {
	this.runNegativeTest(
		new String[] {
			"OverrideBug.java",
			"""
				import java.util.List;
				interface Map<K, V> {
					public V put(K key, V value);
				}
				public class OverrideBug<K, V> implements Map<K, List<V>> {
				public List<V> put(final K arg0, final List<V> arg1) {
				    return null;
				}
				public V put(final K arg0, final V arg1) {
				    return null;
				}
				}"""
		},
		"""
			----------
			1. ERROR in OverrideBug.java (at line 9)
				public V put(final K arg0, final V arg1) {
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method put(K, V) of type OverrideBug<K,V> has the same erasure as put(K, V) of type Map<K,V> but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362
public void test205() {
	this.runConformTest(
		new String[] {
			"Tester.java",
			"""
				import java.lang.reflect.Method;
				
				public class Tester {
				
				 public static interface Converter<T> {
				   T convert(String input);
				 }
				
				 public static abstract class EnumConverter<T extends Enum<T>> implements Converter<Enum<T>> {
				   public final T convert(String input) {
				     return null;
				   }
				 }
				
				 public static class SomeEnumConverter extends EnumConverter<Thread.State> {
				 }
				
				 public static void main(String[] args) throws Exception {
				   Method m = SomeEnumConverter.class.getMethod("convert", String.class);
				   System.out.println(m.getGenericReturnType());
				 }
				
				}
				"""

		},
		"T");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362 (variation)
public void test206() {
	this.runConformTest(
		new String[] {
			"Tester.java",
			"""
				import java.lang.reflect.Method;
				
				public class Tester {
				
				 public static interface Converter<T> {
				   T convert(String input);
				 }
				
				 public static abstract class EnumConverter<T extends Enum<T>> implements Converter<T> {
				   public final T convert(String input) {
				     return null;
				   }
				 }
				
				 public static class SomeEnumConverter extends EnumConverter<Thread.State> {
				 }
				
				 public static void main(String[] args) throws Exception {
				   Method m = SomeEnumConverter.class.getMethod("convert", String.class);
				   System.out.println(m.getGenericReturnType());
				 }
				
				}
				"""

		},
		"T");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362 (variation)
// Note that this test prints "T" with javac5 and "class java.lang.Object with javac 6,7
public void test207() {
	this.runConformTest(
		new String[] {
			"Tester.java",
			"""
				import java.lang.reflect.Method;
				
				public class Tester {
				
				 public static interface Converter<T> {
				   T convert(String input);
				 }
				
				 public static abstract class EnumConverter<T extends Enum<T>, K> implements Converter<T> {
				   public final T convert(K input) {
				     return null;
				   }
				 }
				
				 public static class SomeEnumConverter extends EnumConverter<Thread.State, String> {
				 }
				
				 public static void main(String[] args) throws Exception {
				   Method m = SomeEnumConverter.class.getMethod("convert", String.class);
				   System.out.println(m.getGenericReturnType());
				 }
				
				}
				"""

		},
		"class java.lang.Object");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class.
public void test208() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.lang.annotation.Annotation;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.reflect.Method;
				
				public class Test extends Super {
				    public static void main(String[] args) {
				        try {
				            Method m = Test.class.getMethod("setFoo", String.class);
				            Annotation a = m.getAnnotation(Anno.class);
				            System.out.println("Annotation was " + (a == null ? "not " : "") +
				"found");
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				}
				
				class Super {
				    @Anno
				    public void setFoo(String foo) {}
				}
				
				@Retention(RetentionPolicy.RUNTIME)
				@interface Anno {
				
				}
				"""
		},
		"Annotation was found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class.
public void test208a() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.lang.annotation.Annotation;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.reflect.Method;
				
				public class Test extends Super {
				    public void setFoo() {}
				    public static void main(String[] args) {
				        try {
				            Method m = Test.class.getMethod("setFoo", String.class);
				            Annotation a = m.getAnnotation(Anno.class);
				            System.out.println("Annotation was " + (a == null ? "not " : "") +
				"found");
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				}
				
				class Super {
				    @Anno
				    public void setFoo(String foo) {}
				}
				
				@Retention(RetentionPolicy.RUNTIME)
				@interface Anno {
				
				}
				"""
		},
		"Annotation was found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322001
public void test209() {
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"""
				class Bar extends Zork {}
				class Foo {}
				
				interface Function<F, T> {
				    T apply(F f);
				}
				interface Predicate<T> {
				    boolean apply(T t);
				}
				
				public class Concrete implements Predicate<Foo>, Function<Bar, Boolean> {
				    public Boolean apply(Bar two) {
				        return null;
				    }
				    public boolean apply(Foo foo) {
				        return false;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Concrete.java (at line 1)
				class Bar extends Zork {}
				                  ^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321548
public void test210() {
	this.runNegativeTest(
		new String[] {
			"ErasureTest.java",
			"""
				interface Interface1<T> {
				    public void hello(T greeting);
				}
				interface Interface2<T> {
				    public int hello(T greeting);
				}
				public class ErasureTest extends Zork implements Interface1<String>, Interface2<Double> {
				    public void hello(String greeting) { }
				    public int hello(Double greeting) {
				        return 0;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in ErasureTest.java (at line 7)
				public class ErasureTest extends Zork implements Interface1<String>, Interface2<Double> {
				                                 ^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
public void test211() {
	this.runNegativeTest(
		new String[] {
			"SomeClass.java",
			"""
				interface Equivalent<T> {
					boolean equalTo(T other);
				}
				
				interface EqualityComparable<T> {
					boolean equalTo(T other);
				}
				
				public class SomeClass implements Equivalent<String>, EqualityComparable<Integer> {
					public boolean equalTo(String other) {
						return true;
					}
					public boolean equalTo(Integer other) {
						return true;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in SomeClass.java (at line 9)
				public class SomeClass implements Equivalent<String>, EqualityComparable<Integer> {
				             ^^^^^^^^^
			Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323693
public void test212() {
	this.runNegativeTest(
		new String[] {
			"Derived.java",
			"""
				class Base<T> {
				    T foo(T x) {
				        return x;
				    }
				}
				interface Interface<T>{
				    T foo(T x);
				}
				public class Derived extends Base<String> implements Interface<Integer> {
				    public Integer foo(Integer x) {
				        return x;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Derived.java (at line 9)
				public class Derived extends Base<String> implements Interface<Integer> {
				             ^^^^^^^
			Name clash: The method foo(T) of type Interface<T> has the same erasure as foo(T) of type Base<T> but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void test213() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public abstract class Y implements I<Y> {
						public final Y foo(Object o, J<Y> j) {
							return null;
						}
					public final void bar(Object o, J<Y> j, Y y) {
					}
				}""",
			"I.java",
			"""
				public interface I<S> {
					public S foo(Object o, J<S> j);
					public void bar(Object o, J<S> j, S s);
				}""",
			"J.java",
			"public interface J<S> {}"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public Object foo() {
						return new Y() {};
					}
				}"""
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void test213a() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public abstract class Y implements I<Y> {
						public final Y foo(Object o, J<Y, I<Y>> j) {
							return null;
						}
					public final void bar(Object o, J<Y, String> j, Y y) {
					}
				}""",
			"I.java",
			"""
				public interface I<S> {
					public S foo(Object o, J<S, I<S>> j);
					public void bar(Object o, J<S, String> j, S s);
				}""",
			"J.java",
			"public interface J<S, T> {}"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public Object foo() {
						return new Y() {};
					}
				}"""
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void test213b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"ConsoleSession.java",
			"""
				public abstract class ConsoleSession implements ServiceFactory<Object> {
					public final void ungetService(Bundle bundle, ServiceRegistration<Object> registration, Object service) {
					}
				\t
					public final Object getService(Bundle bundle, ServiceRegistration<Object> registration) {
						return this;
					}
				}
				
				class Bundle {}
				
				interface ServiceFactory<S> {
					public void ungetService(Bundle b, ServiceRegistration<S> registration, S service);
					public S getService(Bundle bundle, ServiceRegistration<S> registration);
				}
				
				interface ServiceRegistration<T> {
				
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
					"OSGiConsole.java",
					"""
						public class OSGiConsole {
							OSGiConsole() {
								new ConsoleSession() {
								};
							}
						}
						""",
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void test213c() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"ConsoleSession.java",
			"""
				public abstract class ConsoleSession implements ServiceFactory<ConsoleSession> {
					public final void ungetService(Bundle bundle, ServiceRegistration<ConsoleSession> registration, ConsoleSession service) {
					}
				\t
					public final ConsoleSession getService(Bundle bundle, ServiceRegistration<ConsoleSession> registration) {
						return this;
					}
				}
				
				class Bundle {}
				
				interface ServiceFactory<S> {
					public void ungetService(Bundle b, ServiceRegistration<S> registration, S service);
					public S getService(Bundle bundle, ServiceRegistration<S> registration);
				}
				
				interface ServiceRegistration<T> {
				
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
					"OSGiConsole.java",
					"""
						public class OSGiConsole {
							OSGiConsole() {
								new ConsoleSession() {
								};
							}
						}
						""",
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
public void test326354() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						public class X extends Y<I>  implements I {
						    public static void main(String[] args) {
						        ((I) new X()).foo(null);
						    }
						}
						
						interface I {
						    public void foo(I i);
						}
						\s
						abstract class Y<T> {
							   public void foo(T t) {}
						}
						"""
			},
			""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328827
public void test328827() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Map.java",
			"public interface Map<K,V> {}\n",

			"EventProperties.java",
			"public class EventProperties implements Map<String, Object> {}\n",

			"Event.java",
			"""
				public class Event {
				    public Event(Map<String, ?> properties) {}
				}"""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
				"Map.java",
				"public interface Map {}\n",

				"X.java",
				"""
					public class X {
					    public void start() {
					        Event event = new Event(new EventProperties());
						}
					}"""
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=329584
public void test329584() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"I.java",
			"""
				public interface I {
					void foo(Object o[], Dictionary<Object, Object> dict);
				}""",
			"Dictionary.java",
			"public class Dictionary<U, V> {}\n",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements I {
					public void foo(Object o[], Dictionary dict) {}
				}""",
			"Dictionary.java",
			"public class Dictionary {}\n",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=329588
public void test329588() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
					public O<?> foo() {
						return null;
					}
				}""",
			"O.java",
			"public class O<V> {}\n",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo(A a) {
						O o = (O) a.foo();
						System.out.println(o);
					}
				}""",
			"O.java",
			"public class O {}\n",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330445
public void test330445() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				import java.util.Map;
				public class Y {
					static void foo(Map<String, String> map) {
					}
				}""",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Properties;
				public class X {
				    static void bar(Object[] args) {
				        Y.foo(new Properties());
				    }
				}""",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330435
public void test330435() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
					public static <T> B<T> asList(T... tab) {
						return null;
					}
				}""",
			"B.java",
			"""
				public interface B<V> {
					<T> T[] toArray(T[] tab);
				}
				""",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String[] foo(Object[] args) {
						String[] a = A.asList(args).toArray(new String[0]);
						return a;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				String[] a = A.asList(args).toArray(new String[0]);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Object[] to String[]
			----------
			""",
		null,
		false,
		compilerOptions14);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330264
public void test330264() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"BundleContext.java",
			"""
				public interface BundleContext {
				    <S> S getService(ServiceReference<S> reference);
				}
				""",
			"ServiceReference.java",
			"public interface ServiceReference<S> extends Comparable<Object> {}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Activator.java",
			"""
				public class Activator  {
				    public void start(BundleContext context, ServiceReference ref) {
				        Runnable r = context.getService(ref);
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in Activator.java (at line 3)
				Runnable r = context.getService(ref);
				             ^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Object to Runnable
			----------
			""",
		null,
		false,
		compilerOptions14);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test331446() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Comparator;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class Test {\n" +
			"	public static <T> void assertEquals(String message,\n" +
			"			Comparator<T> comparator, List<T> expected, List<T> actual) {\n" +
			"		if (expected.size() != actual.size()) {\n" +
			"			//failNotEquals(message, expected, actual);\n" +
			"		}\n" +
			"		for (int i = 0, l = expected.size(); i < l; i++) {\n" +
			"			assertEquals(message, comparator, expected.get(i), actual.get(i));\n" +
			"		}\n" +
			"	}\n" +
			"	public static <T> void assertEquals(String message,\n" +
			"			Comparator<T> comparator, T expected, T actual) {\n" +
			"		if (comparator.compare(expected, actual) == 0) {\n" +
			"			return;\n" +
			"		}\n" +
			"		//failNotEquals(message, expected, actual);\n" +
			"	}\n" +
			"}\n" +
			""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Comparator;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void testAmbiguity() {\n" +
			"		Comparator comparator = new Comparator() {\n" +
			"			\n" +
			"			public int compare(Object o1, Object o2) {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		};\n" +
			"		Test.assertEquals(\"Test\", comparator, new ArrayList(), new ArrayList());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test331446a() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_4);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Comparator;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class Test {\n" +
			"	public static  void assertEquals(String message,\n" +
			"			Comparator comparator, List expected, List actual) {\n" +
			"		if (expected.size() != actual.size()) {\n" +
			"			//failNotEquals(message, expected, actual);\n" +
			"		}\n" +
			"		for (int i = 0, l = expected.size(); i < l; i++) {\n" +
			"			assertEquals(message, comparator, expected.get(i), actual.get(i));\n" +
			"		}\n" +
			"	}\n" +
			"	public static void assertEquals(String message,\n" +
			"			Comparator comparator, Object expected, Object actual) {\n" +
			"		if (comparator.compare(expected, actual) == 0) {\n" +
			"			return;\n" +
			"		}\n" +
			"		//failNotEquals(message, expected, actual);\n" +
			"	}\n" +
			"}\n" +
			""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Comparator;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void testAmbiguity() {\n" +
			"		Comparator comparator = new Comparator() {\n" +
			"			\n" +
			"			public int compare(Object o1, Object o2) {\n" +
			"				return 0;\n" +
			"			}\n" +
			"		};\n" +
			"		Test.assertEquals(\"Test\", comparator, new ArrayList(), new ArrayList());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446 (all 1.4)
public void test331446b() {
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"Project.java",
			"""
				class List{}
				public class Project {
				    static  void foo(List expected) {}
				    public static void foo(Object expected) {}
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions14,
		null);

	this.runConformTest(
		new String[] {
			"Client.java",
			"""
				public class Client {
				    Client(List l) {
				        Project.foo(l);
				    }
				}
				"""
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446 (1.4/1.5 mix)
public void test331446c() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Project.java",
			"""
				class List<T> {}
				public class Project {
				    static <T> void foo(List<T> expected) {}
				    public static <T> void foo(T expected) {}
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"Client.java",
			"""
				public class Client {
				    Client(List l) {
				        Project.foo(l);
				    }
				}
				"""
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446 (all 1.5)
public void test331446d() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Project.java",
			"""
				class List<T> {}
				public class Project {
				    static <T> void foo(List<T> expected) {}
				    public static <T> void foo(T expected) {}
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	this.runConformTest(
		new String[] {
			"Client.java",
			"""
				public class Client {
				    Client(List l) {
				        Project.foo(l);
				    }
				}
				"""
			},
		"",
		null,
		false,
		null,
		compilerOptions15,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test1415Mix() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Abstract.java",
			"""
				abstract class Generic<T> {
					abstract void foo(T t);
				}
				public abstract class Abstract extends Generic<String> {
				}"""
			},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"public class Concrete extends Abstract {\n" +
			"}",
		},
		"""
			----------
			1. ERROR in Concrete.java (at line 1)
				public class Concrete extends Abstract {
				             ^^^^^^^^
			The type Concrete must implement the inherited abstract method Generic<String>.foo(String)
			----------
			""",
		null,
		false,
		compilerOptions14);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test1415Mix2() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Abstract.java",
			"""
				abstract class Generic<T> {
					abstract void foo(T t);
				}
				public abstract class Abstract extends Generic<String> {
				}"""
			},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
				"Concrete.java",
				"""
					public class Concrete extends Abstract {
					    void foo(String s) {}
					}""",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332744 (all 1.5+)
public void test332744() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"EList.java",
			"""
				import java.util.List;
				public interface EList<E> extends List<E> {
				}
				""",
			"FeatureMap.java",
			"""
				public interface FeatureMap extends EList<FeatureMap.Entry> {
				    interface Entry {
				    }
				}
				""",
			"InternalEList.java",
			"public interface InternalEList<E> extends EList<E> {\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	this.runConformTest(
		new String[] {
			"Client.java",
			"""
				public class Client {
				    Client(FeatureMap fm) {
						InternalEList e = (InternalEList) fm;
					}
				}
				"""
			},
		"",
		null,
		false,
		null,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332744 (1.4/1.5 mix)
public void _test332744b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"EList.java",
			"""
				import java.util.List;
				public interface EList<E> extends List<E> {
				}
				""",
			"FeatureMap.java",
			"""
				public interface FeatureMap extends EList<FeatureMap.Entry> {
				    interface Entry {
				    }
				}
				""",
			"InternalEList.java",
			"public interface InternalEList<E> extends EList<E> {\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"Client.java",
			"""
				public class Client {
				    Client(FeatureMap fm) {
						InternalEList e = (InternalEList) fm;
					}
				}
				"""
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=339447
public void test339447() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements Cloneable {
					public synchronized X clone() {
						return this;
					}
				}""", // =================
		},
		"");
	// 	ensure bridge methods have target method modifiers, and inherited thrown exceptions
	String expectedOutput =
			"  public bridge synthetic java.lang.Object clone() throws java.lang.CloneNotSupportedException;";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322740
public void test322740() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Base  {
				    boolean equalTo(Object other) {return false;}
				}
				interface EqualityComparable<T> {
				    boolean equalTo(T other);
				}
				public class X extends Base implements EqualityComparable<String> {
				    public boolean equalTo(String other) {
				        return true;
				    }
				    public static void main(String args[]) {
				        new X().equalTo(args);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				public class X extends Base implements EqualityComparable<String> {
				             ^
			Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(Object) of type Base but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334306
public void test334306() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {}
				interface I {
				    void foo(X<Number> p);
				}
				interface J extends I {
				    void foo(X<Integer> p);
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				void foo(X<Integer> p);
				     ^^^^^^^^^^^^^^^^^
			Name clash: The method foo(X<Integer>) of type J has the same erasure as foo(X<Number>) of type I but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342819
public void test342819() throws Exception {
	this.runNegativeTest(
		new String[] {
			"TwoWayDTOAdapter.java",
			"""
				public interface TwoWayDTOAdapter<A, B> extends DTOAdapter <A, B>{
				    public A convert(B b);
				}
				""",
			"DTOAdapter.java",
			"""
				public interface DTOAdapter<A, B> {
				    public B convert(A a);
				}
				""",
			"TestAdapter.java",
			"""
				public class TestAdapter implements TwoWayDTOAdapter<Long, Integer> {
				    public Long convert(Integer b) {
				        return null;
				    }
				    public Integer convert(Long a) {
				        return null;
				    }
				}"""
		},
		"""
			----------
			1. ERROR in TwoWayDTOAdapter.java (at line 2)
				public A convert(B b);
				         ^^^^^^^^^^^^
			Name clash: The method convert(B) of type TwoWayDTOAdapter<A,B> has the same erasure as convert(A) of type DTOAdapter<A,B> but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				    void f(String s) {}
				}
				class B<T> extends A<T> {
				    void f(T t) {}
				}
				public class X extends B<String> {
				    void foo(X x) {
				        x.f("");
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				public class X extends B<String> {
				             ^
			Duplicate methods named f with the parameters (T) and (String) are inherited from the types B<String> and A<String>
			----------
			2. ERROR in X.java (at line 9)
				x.f("");
				  ^
			The method f(String) is ambiguous for the type X
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029b() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface A<T> {
				    void f(String s);
				}
				interface B<T> extends A<T> {
				    void f(T t);
				}
				public class X implements B<String> {
				    public void f(String t) {
				        Zork z;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029c() throws Exception {
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				class A<T> {
				    void f(String s) {}
				}
				interface B<T> {
				    void f(T t);
				}
				public class X extends A<String> implements B<String> {
				    public void f(String t) {
				    }
				}
				"""
		},
		null,
		null,
		"""
			----------
			1. WARNING in X.java (at line 8)
				public void f(String t) {
				            ^^^^^^^^^^^
			The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029d() throws Exception {
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				class A<T> {
				    void f(T s) {}
				}
				interface B<T> {
				    void f(String t);
				}
				public class X extends A<String> implements B<String> {
				    public void f(String t) {
				    }
				}
				"""
		},
		null,
		null,
		"""
			----------
			1. WARNING in X.java (at line 8)
				public void f(String t) {
				            ^^^^^^^^^^^
			The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029e() throws Exception {
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				class A<T> {
				    void f(String s) {}
				}
				class B<T> extends A<T> {
				    void f(T t) {}
				}
				public class X extends B<String> {
				    	void f(String s) {
				       }
				}
				"""
		},
		null,
		null,
		"""
			----------
			1. WARNING in X.java (at line 8)
				void f(String s) {
				     ^^^^^^^^^^^
			The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method
			----------
			""",
		Excuse.EclipseHasSomeMoreWarnings);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029f() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				    void f(String s) {}
				}
				class B<T> extends A<T> {
				    void f(T t) {}
				}
				public class X extends B<String> {
					void f(String s) {
						super.f(s);
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				void f(String s) {
				     ^^^^^^^^^^^
			The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 9)
				super.f(s);
				      ^
			The method f(String) is ambiguous for the type B<String>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353089
public void test353089() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				interface A {
				int get(List<String> l);
				}
				interface B  {
				int get(List<Integer> l);
				}
				interface C  extends A, B {\s
				int get(List l);      // name clash error here
				}
				public class X {
				    public static void main(String [] args) {
				        System.out.println("Built OK");
				    }
				}"""
		},
		"Built OK");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=353089
public void test353089b() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				interface I {
				    void a(List<String> i, List<String> j);
				    void b(List<String> i, List<String> j);
				    void c(List i, List<String> j);
				}
				interface X extends I {
				    public void a(List<String> i, List j);
				    public void b(List i, List j);
				    public void c(List i, List j);
				    public void d(Zork z);
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				void c(List i, List<String> j);
				       ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			2. ERROR in X.java (at line 8)
				public void a(List<String> i, List j);
				            ^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method a(List<String>, List) of type X has the same erasure as a(List<String>, List<String>) of type I but does not override it
			----------
			3. WARNING in X.java (at line 8)
				public void a(List<String> i, List j);
				                              ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			4. WARNING in X.java (at line 9)
				public void b(List i, List j);
				              ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			5. WARNING in X.java (at line 9)
				public void b(List i, List j);
				                      ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			6. WARNING in X.java (at line 10)
				public void c(List i, List j);
				              ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			7. WARNING in X.java (at line 10)
				public void c(List i, List j);
				                      ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			8. ERROR in X.java (at line 11)
				public void d(Zork z);
				              ^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=353089
public void test353089c() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				interface IFtest {
				    public void doTest(Integer i, List<String> pList, List<String> pList2);
				}
				interface Impl extends IFtest {
				    public void doTest(Integer i, List<String> iList, List iList2);
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				public void doTest(Integer i, List<String> iList, List iList2);
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method doTest(Integer, List<String>, List) of type Impl has the same erasure as doTest(Integer, List<String>, List<String>) of type IFtest but does not override it
			----------
			2. WARNING in X.java (at line 6)
				public void doTest(Integer i, List<String> iList, List iList2);
				                                                  ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 4)
					public <T extends List> T foo() { return null; }
					                  ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 4)
					public <T extends List> T foo() { return null; }
					                          ^^^^^
				Duplicate method foo() in type X
				----------
				3. WARNING in X.java (at line 5)
					public <T extends Set> T foo() { return null; }
					                  ^^^
				Set is a raw type. References to generic type Set<E> should be parameterized
				----------
				4. WARNING in X.java (at line 5)
					public <T extends Set> T foo() { return null; }
					                         ^^^^^
				Duplicate method foo() in type X
				----------
				5. ERROR in X.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. WARNING in X.java (at line 4)
						public <T extends List> T foo() { return null; }
						                  ^^^^
					List is a raw type. References to generic type List<E> should be parameterized
					----------
					2. ERROR in X.java (at line 4)
						public <T extends List> T foo() { return null; }
						                          ^^^^^
					Duplicate method foo() in type X
					----------
					3. WARNING in X.java (at line 5)
						public <T extends Set> T foo() { return null; }
						                  ^^^
					Set is a raw type. References to generic type Set<E> should be parameterized
					----------
					4. ERROR in X.java (at line 5)
						public <T extends Set> T foo() { return null; }
						                         ^^^^^
					Duplicate method foo() in type X
					----------
					5. ERROR in X.java (at line 6)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.Set;
				class X {
				    public <T extends List> T foo() { return null; }
					 public <T extends Set> T foo() { return null; }
					 Zork z;
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719a() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 4)
					public Integer same(List<Integer> a) { return null; }
					               ^^^^^^^^^^^^^^^^^^^^^
				Erasure of method same(List<Integer>) is the same as another method in type X
				----------
				2. WARNING in X.java (at line 5)
					public String same(List<String> b) { return null; }
					              ^^^^^^^^^^^^^^^^^^^^
				Erasure of method same(List<String>) is the same as another method in type X
				----------
				3. ERROR in X.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 4)
						public Integer same(List<Integer> a) { return null; }
						               ^^^^^^^^^^^^^^^^^^^^^
					Erasure of method same(List<Integer>) is the same as another method in type X
					----------
					2. ERROR in X.java (at line 5)
						public String same(List<String> b) { return null; }
						              ^^^^^^^^^^^^^^^^^^^^
					Erasure of method same(List<String>) is the same as another method in type X
					----------
					3. ERROR in X.java (at line 6)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.Set;
				class X {
				    public Integer same(List<Integer> a) { return null; }
					 public String same(List<String> b) { return null; }
					 Zork z;
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719b() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 3)
					public static String doIt(final List<String> arg) { return null; }
					                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method doIt(List<String>) is the same as another method in type X
				----------
				2. WARNING in X.java (at line 4)
					public static CharSequence doIt(final List<CharSequence> arg) { return null; }
					                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method doIt(List<CharSequence>) is the same as another method in type X
				----------
				3. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 3)
						public static String doIt(final List<String> arg) { return null; }
						                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method doIt(List<String>) is the same as another method in type X
					----------
					2. ERROR in X.java (at line 4)
						public static CharSequence doIt(final List<CharSequence> arg) { return null; }
						                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method doIt(List<CharSequence>) is the same as another method in type X
					----------
					3. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				class X {
				    public static String doIt(final List<String> arg) { return null; }
					 public static CharSequence doIt(final List<CharSequence> arg) { return null; }
					 Zork z;
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719c() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 3)
					protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }
					                            ^^^^^^
				The type parameter T should not be bounded by the final type String. Final types cannot be further extended
				----------
				2. WARNING in X.java (at line 3)
					protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }
					                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method same(Collection<? extends T>) is the same as another method in type X
				----------
				3. WARNING in X.java (at line 4)
					protected static <T extends Number> T same(Collection<? extends T> p_col) { return null; }
					                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method same(Collection<? extends T>) is the same as another method in type X
				----------
				4. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. WARNING in X.java (at line 3)
						protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }
						                            ^^^^^^
					The type parameter T should not be bounded by the final type String. Final types cannot be further extended
					----------
					2. ERROR in X.java (at line 3)
						protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }
						                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method same(Collection<? extends T>) is the same as another method in type X
					----------
					3. ERROR in X.java (at line 4)
						protected static <T extends Number> T same(Collection<? extends T> p_col) { return null; }
						                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method same(Collection<? extends T>) is the same as another method in type X
					----------
					4. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				class X {
				    protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }
					 protected static <T extends Number> T same(Collection<? extends T> p_col) { return null; }
					 Zork z;
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719d() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 3)
					public static boolean foo(List<String> x) { return true; }
					                      ^^^^^^^^^^^^^^^^^^^
				Erasure of method foo(List<String>) is the same as another method in type X
				----------
				2. WARNING in X.java (at line 4)
					public static int foo(List<Integer> x) { return 2; }
					                  ^^^^^^^^^^^^^^^^^^^^
				Erasure of method foo(List<Integer>) is the same as another method in type X
				----------
				3. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 3)
						public static boolean foo(List<String> x) { return true; }
						                      ^^^^^^^^^^^^^^^^^^^
					Erasure of method foo(List<String>) is the same as another method in type X
					----------
					2. ERROR in X.java (at line 4)
						public static int foo(List<Integer> x) { return 2; }
						                  ^^^^^^^^^^^^^^^^^^^^
					Erasure of method foo(List<Integer>) is the same as another method in type X
					----------
					3. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				class X {
				    public static boolean foo(List<String> x) { return true; }
					 public static int foo(List<Integer> x) { return 2; }
					 Zork z;
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719e() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 3)
					public String getFirst (ArrayList<String> ss) { return ss.get(0); }
					              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method getFirst(ArrayList<String>) is the same as another method in type X
				----------
				2. WARNING in X.java (at line 4)
					public Integer getFirst (ArrayList<Integer> ss) { return ss.get(0); }
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method getFirst(ArrayList<Integer>) is the same as another method in type X
				----------
				3. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 3)
						public String getFirst (ArrayList<String> ss) { return ss.get(0); }
						              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method getFirst(ArrayList<String>) is the same as another method in type X
					----------
					2. ERROR in X.java (at line 4)
						public Integer getFirst (ArrayList<Integer> ss) { return ss.get(0); }
						               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method getFirst(ArrayList<Integer>) is the same as another method in type X
					----------
					3. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				class X {
				    public String getFirst (ArrayList<String> ss) { return ss.get(0); }
					 public Integer getFirst (ArrayList<Integer> ss) { return ss.get(0); }
					 Zork z;
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719f() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 3)
					public static <R extends Object> X<R> forAccountSet(List list) { return null; }
					                                      ^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method forAccountSet(List) is the same as another method in type X<Z>
				----------
				2. WARNING in X.java (at line 3)
					public static <R extends Object> X<R> forAccountSet(List list) { return null; }
					                                                    ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 4)
					public static <R extends Object> ChildX<R> forAccountSet(List<R> list) { return null; }
					                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method forAccountSet(List<R>) is the same as another method in type X<Z>
				----------
				4. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 3)
						public static <R extends Object> X<R> forAccountSet(List list) { return null; }
						                                      ^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method forAccountSet(List) is the same as another method in type X<Z>
					----------
					2. WARNING in X.java (at line 3)
						public static <R extends Object> X<R> forAccountSet(List list) { return null; }
						                                                    ^^^^
					List is a raw type. References to generic type List<E> should be parameterized
					----------
					3. ERROR in X.java (at line 4)
						public static <R extends Object> ChildX<R> forAccountSet(List<R> list) { return null; }
						                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method forAccountSet(List<R>) is the same as another method in type X<Z>
					----------
					4. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				class X<Z> {
				    public static <R extends Object> X<R> forAccountSet(List list) { return null; }
					 public static <R extends Object> ChildX<R> forAccountSet(List<R> list) { return null; }
					 Zork z;
				}
				class ChildX<Z> extends X<Z>{}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719g() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in X.java (at line 3)
					public static int[] doIt(Collection<int[]> col) { return new int[1]; }
					                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method doIt(Collection<int[]>) is the same as another method in type X<Z>
				----------
				2. WARNING in X.java (at line 4)
					public static int[][] doIt(Collection<int[][]> col) { return new int[0][0]; }
					                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method doIt(Collection<int[][]>) is the same as another method in type X<Z>
				----------
				3. WARNING in X.java (at line 5)
					public int[] doIt2(Collection<int[]> col) { return new int[0]; }
					             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method doIt2(Collection<int[]>) is the same as another method in type X<Z>
				----------
				4. WARNING in X.java (at line 6)
					public int[][] doIt2(Collection<int[][]> col) { return new int[0][0]; }
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method doIt2(Collection<int[][]>) is the same as another method in type X<Z>
				----------
				5. ERROR in X.java (at line 7)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 3)
						public static int[] doIt(Collection<int[]> col) { return new int[1]; }
						                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method doIt(Collection<int[]>) is the same as another method in type X<Z>
					----------
					2. ERROR in X.java (at line 4)
						public static int[][] doIt(Collection<int[][]> col) { return new int[0][0]; }
						                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method doIt(Collection<int[][]>) is the same as another method in type X<Z>
					----------
					3. ERROR in X.java (at line 5)
						public int[] doIt2(Collection<int[]> col) { return new int[0]; }
						             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method doIt2(Collection<int[]>) is the same as another method in type X<Z>
					----------
					4. ERROR in X.java (at line 6)
						public int[][] doIt2(Collection<int[][]> col) { return new int[0][0]; }
						               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Erasure of method doIt2(Collection<int[][]>) is the same as another method in type X<Z>
					----------
					5. ERROR in X.java (at line 7)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				class X<Z> {
				    public static int[] doIt(Collection<int[]> col) { return new int[1]; }
					 public static int[][] doIt(Collection<int[][]> col) { return new int[0][0]; }
					 public int[] doIt2(Collection<int[]> col) { return new int[0]; }
					 public int[][] doIt2(Collection<int[][]> col) { return new int[0][0]; }
					 Zork z;
				}
				class ChildX<Z> extends X<Z>{}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719h() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. WARNING in Test.java (at line 3)
					public class Test<Key, Value> extends HashMap<Key, Collection<Value>> {
					             ^^^^
				The serializable class Test does not declare a static final serialVersionUID field of type long
				----------
				2. WARNING in Test.java (at line 4)
					public Collection<Value> put(Key k, Value v) { return null; }
					                         ^^^^^^^^^^^^^^^^^^^
				Name clash: The method put(Key, Value) of type Test<Key,Value> has the same erasure as put(K, V) of type HashMap<K,V> but does not override it
				----------
				3. WARNING in Test.java (at line 5)
					public Collection<Value> get(Key k) { return null; }
					                         ^^^^^^^^^^
				Name clash: The method get(Key) of type Test<Key,Value> has the same erasure as get(Object) of type HashMap<K,V> but does not override it
				----------
				4. ERROR in Test.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""":
				"""
					----------
					1. WARNING in Test.java (at line 3)
						public class Test<Key, Value> extends HashMap<Key, Collection<Value>> {
						             ^^^^
					The serializable class Test does not declare a static final serialVersionUID field of type long
					----------
					2. ERROR in Test.java (at line 4)
						public Collection<Value> put(Key k, Value v) { return null; }
						                         ^^^^^^^^^^^^^^^^^^^
					Name clash: The method put(Key, Value) of type Test<Key,Value> has the same erasure as put(K, V) of type HashMap<K,V> but does not override it
					----------
					3. ERROR in Test.java (at line 5)
						public Collection<Value> get(Key k) { return null; }
						                         ^^^^^^^^^^
					Name clash: The method get(Key) of type Test<Key,Value> has the same erasure as get(Object) of type HashMap<K,V> but does not override it
					----------
					4. ERROR in Test.java (at line 6)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.Collection;
				import java.util.HashMap;
				public class Test<Key, Value> extends HashMap<Key, Collection<Value>> {
				    public Collection<Value> put(Key k, Value v) { return null; }
					 public Collection<Value> get(Key k) { return null; }
					 Zork z;
				}
				"""
		},
		output);
}
public void test345949a() throws Exception {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_7) return;
	this.runNegativeTest(
		new String[] {
			"Sub.java",
			"""
				class A<T> {}
				class Super {
				    public static void foo(A<Number> p) {}
				}
				public class Sub extends Super {
					 public static void foo(A<Integer> p) {}
				}
				"""
		},
		"""
			----------
			1. ERROR in Sub.java (at line 6)
				public static void foo(A<Integer> p) {}
				                   ^^^^^^^^^^^^^^^^^
			Name clash: The method foo(A<Integer>) of type Sub has the same erasure as foo(A<Number>) of type Super but does not hide it
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=355838
public void testBug355838() throws Exception {
	String output =
			"""
		----------
		1. ERROR in ErasureBug.java (at line 4)
			public String output(List<String> integers) {
			              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Erasure of method output(List<String>) is the same as another method in type ErasureBug
		----------
		2. ERROR in ErasureBug.java (at line 7)
			public String output(List doubles) {
			              ^^^^^^^^^^^^^^^^^^^^
		Erasure of method output(List) is the same as another method in type ErasureBug
		----------
		3. WARNING in ErasureBug.java (at line 7)
			public String output(List doubles) {
			                     ^^^^
		List is a raw type. References to generic type List<E> should be parameterized
		----------
		4. WARNING in ErasureBug.java (at line 10)
			public static void main(String[] args) { new ErasureBug().output(new ArrayList()); }
			                                                                 ^^^^^^^^^^^^^^^
		Type safety: The expression of type ArrayList needs unchecked conversion to conform to List<String>
		----------
		5. WARNING in ErasureBug.java (at line 10)
			public static void main(String[] args) { new ErasureBug().output(new ArrayList()); }
			                                                                     ^^^^^^^^^
		ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
		----------
		""";
	this.runNegativeTest(
		new String[] {
			"ErasureBug.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class ErasureBug {
				    public String output(List<String> integers) {
						return "1";
					 }
				    public String output(List doubles) {
						return "2";
					 }
					 public static void main(String[] args) { new ErasureBug().output(new ArrayList()); }
				}
				"""
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class if the non public class happens to be defined in a named package.
public void test288658() {
	this.runConformTest(
		new String[] {
			"pkg/Test.java",
			"""
				package pkg;
				import java.lang.annotation.Annotation;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.reflect.Method;
				
				public class Test extends Super {
				    public static void main(String[] args) {
				        try {
				            Method m = Test.class.getMethod("setFoo", String.class);
				            Annotation a = m.getAnnotation(Anno.class);
				            System.out.println("Annotation was " + (a == null ? "not " : "") +
				"found");
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				}
				
				class Super {
				    @Anno
				    public void setFoo(String foo) {}
				}
				
				@Retention(RetentionPolicy.RUNTIME)
				@interface Anno {
				
				}
				"""
		},
		"Annotation was found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class if the non public class happens to be defined in a named package.
public void test288658a() {
	this.runConformTest(
		new String[] {
			"pkg/Test.java",
			"""
				package pkg;
				import java.lang.annotation.Annotation;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.reflect.Method;
				
				public class Test extends Super {
				    public void setFoo() {}
				    public static void main(String[] args) {
				        try {
				            Method m = Test.class.getMethod("setFoo", String.class);
				            Annotation a = m.getAnnotation(Anno.class);
				            System.out.println("Annotation was " + (a == null ? "not " : "") +
				"found");
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				}
				
				class Super {
				    @Anno
				    public void setFoo(String foo) {}
				}
				
				@Retention(RetentionPolicy.RUNTIME)
				@interface Anno {
				
				}
				"""
		},
		"Annotation was found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				interface A {
				int get(List<String> l);
				}
				interface B  {
				int get(List<Integer> l);
				}
				interface C  extends A, B {\s
				//int get(List l);      // name clash error here
				    Zork z;
				}
				"""
		},
		this.complianceLevel <= ClassFileConstants.JDK1_6 ?
				"""
					----------
					1. ERROR in X.java (at line 10)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""" :
					"""
						----------
						1. ERROR in X.java (at line 8)
							interface C  extends A, B {\s
							          ^
						Name clash: The method get(List<Integer>) of type B has the same erasure as get(List<String>) of type A but does not override it
						----------
						2. ERROR in X.java (at line 10)
							Zork z;
							^^^^
						Zork cannot be resolved to a type
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				interface A {
				int get(List<String> l);
				}
				interface B  {
				int get(List<Integer> l);
				}
				interface C  extends A, B {\s
				    int get(List l);      // name clash error here
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				int get(List l);      // name clash error here
				        ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			2. ERROR in X.java (at line 10)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				    <T> T e(Action<T> p);
				}
				interface Y {
				    <S, T> S e(Action<S> t);
				}
				interface E extends X, Y {
				}
				class Action<T> {
				    Zork z;
				}
				"""

		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. ERROR in X.java (at line 10)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""" :
					"""
						----------
						1. ERROR in X.java (at line 7)
							interface E extends X, Y {
							          ^
						Name clash: The method e(Action<S>) of type Y has the same erasure as e(Action<T>) of type X but does not override it
						----------
						2. ERROR in X.java (at line 10)
							Zork z;
							^^^^
						Zork cannot be resolved to a type
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface X {
				    <T> T e(Action<T> p);
				    <S, T> S e(Action<S> t);
				}
				class Action<T> {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				<T> T e(Action<T> p);
				      ^^^^^^^^^^^^^^
			Erasure of method e(Action<T>) is the same as another method in type X
			----------
			2. ERROR in X.java (at line 3)
				<S, T> S e(Action<S> t);
				         ^^^^^^^^^^^^^^
			Erasure of method e(Action<S>) is the same as another method in type X
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384580, Apply changes in JLS 8.4.5 to calculation of duplicate method return types
public void testBug384580() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				interface X { <T> List<T> m(); }
				interface Y<K> { List<K> m(); }
				interface Z extends X, Y {} \s
				class Foo implements Z {
					public <T> List<T> m() {
						return null;
					}
				\t
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				interface Z extends X, Y {} \s
				                       ^
			Y is a raw type. References to generic type Y<K> should be parameterized
			----------
			2. ERROR in X.java (at line 5)
				class Foo implements Z {
				      ^^^
			The type Foo must implement the inherited abstract method Y.m()
			----------
			3. ERROR in X.java (at line 6)
				public <T> List<T> m() {
				                   ^^^
			Name clash: The method m() of type Foo has the same erasure as m() of type Y but does not override it
			----------
			""");
}
// https://bugs.eclipse.org/406928 - computation of inherited methods seems damaged (affecting @Overrides)
public void testBug406928() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6) return;
	this.runConformTest(
		new String[] {
			"TestPointcut.java",
			"""
				interface MethodMatcher {
					boolean matches();
				}
				abstract class StaticMethodMatcher implements MethodMatcher { }
				abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher { }
				
				class TestPointcut extends StaticMethodMatcherPointcut {
					@Override
					public boolean matches() { return false; }\s
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/409473 - [compiler] JDT cannot compile against JRE 1.8
// Test failed when running on a JRE 1.8 b90
public void testBug409473() {
    this.runConformTest(
        new String[] {
            "Foo.java",
            "public abstract class Foo<E> implements java.util.List<E> { } "
        });
}
// https://bugs.eclipse.org/410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
public void testBug410325() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				public class Main {
					public static void main(String[] args) {
						F3 f3 = new F3();
						SubSub sub = new SubSub();
						sub.foo(f3);
				
						Sub<F3> sub2 = sub;
						Base<F3> base = sub;
						sub2.foo(f3);
						base.foo(f3);
				
						F2 f2 = new F2();
						sub2.foo(f2);
					}
				
					public static class F1 {
					}
				
					public static class F2 extends F1 {
					}
				
					public static class F3 extends F2 {
						public void bar() {
							System.out.println("bar in F3");
						}
					}
				
					public static abstract class Base<T extends F1> {
						public abstract void foo(T bar);
					}
				
					public static abstract class Sub<T extends F2> extends Base<T> {
						@Override
						public void foo(F2 bar) {
							System.out.println(getClass().getSimpleName() + ": F2 + "
									+ bar.getClass().getSimpleName());
						}
					}
				
					public static class SubSub extends Sub<F3> {
					}
				}"""
		});
}
// https://bugs.eclipse.org/410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
// test from duplicate bug 411811
public void testBug411811() {
	runConformTest(
		new String[] {
			"FaultyType.java",
			"""
				    class ParamType {}
				
				    abstract class AbstractType<T extends ParamType> {
				        public abstract void foo(T t);
				    }
				
				    abstract class SubAbstractType<T extends ParamType> extends AbstractType<T> {
				        @Override public void foo(ParamType t) {}
				    }
				
				    class SubParamType extends ParamType {}
				   \s
				public class FaultyType extends SubAbstractType<SubParamType> {}"""
		});
}
// https://bugs.eclipse.org/410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
// test from duplicate bug 415600
public void testBug415600() {
	runConformTest(
		new String[] {
			"A.java",
			"""
				import java.io.Reader;
				import java.io.StringReader;
				
				public abstract class A<E extends Reader> {
					protected abstract void create(E element);
				}
				
				abstract class B<T extends Reader> extends A<T> {
					public void create(Reader element) { }
				}
				
				class C extends B<StringReader> { }
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423849,  [1.8][compiler] cannot implement java.nio.file.Path because of compiler name clash
public void test423849() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.nio.file.WatchEvent.Kind;
				import java.nio.file.WatchEvent.Modifier;
				import java.nio.file.WatchKey;
				import java.nio.file.WatchService;
				abstract class Y implements Path {
				    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
				        return null;
				    }
				}
				public class X {
				    public static void main(String [] args) {
				        System.out.println("OK");
				    }
				}
				"""
		});
}
// assure that an inherited bridge method need not be repeated
public void testBug426546() {
	runConformTest(
		new String[] {
			"C.java",
			"""
				class A {
				   A getMe() { return null; }
				}
				class B extends A {
				   B getMe() { return null; }
				}
				public class C extends B {
				   C getMe() { return this; }
				   public String toString() { return "C"; }
				   public static void main(String[] args) {
				      C c = new C();
				      System.out.print(c.getMe());
				      B b = c;
				      System.out.print(b.getMe());
				      A a = c;
				      System.out.print(a.getMe());\s
				   }
				}
				"""
		},
		"CCC");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438812, Missing bridge methods in indirect child classes with ECJ 3.10.0
public void testBug438812() throws Exception {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.Collection;
				import java.util.List;
				
				public interface A {
				    Iterable getIterable();
				}
				
				class B implements A {
				    public Collection getIterable() { return null; }
				}
				
				class C extends B {
				    public List getIterable() { return null; }
				}""",
		},
		"");
	String expectedOutput = "  public bridge synthetic java.lang.Iterable getIterable();";

	File f = new File(OUTPUT_DIR + File.separator + "C.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=469454, The compiler generates wrong code during inheritance
public void testBug469454() throws Exception {
	this.runConformTest(
		new String[] {
			"TestClass.java",
			"""
				public class TestClass {
				    public static class A {
				        public static A method() {
				            return null;
				        }
				    }
				    public static class B extends A {
				        public static B method() {
				            return null;
				        }
				    }
				    public static void main(String[] args) {
				        System.out.println(B.class.getDeclaredMethods().length);
				    }
				}
				""",
		},
		"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=469454, The compiler generates wrong code during inheritance
public void testBug469454a() throws Exception {
	this.runNegativeTest(
		new String[] {
			"TestClass.java",
			"""
				public class TestClass {
				    public static class A {
				        public final A method() {
				            return null;
				        }
				    }
				    public static class B extends A {
				        @Override
				        public B method() {
				            return null;
				        }
				    }
				    public static void main(String[] args) {
				        System.out.println(B.class.getDeclaredMethods().length);
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in TestClass.java (at line 9)
				public B method() {
				         ^^^^^^^^
			Cannot override the final method from TestClass.A
			----------
			""",
		null,
		true,
		null,
		true,
		false,
		false);
	String expectedOutput = "  public bridge synthetic TestClass.A method();";

	File f = new File(OUTPUT_DIR + File.separator + "TestClass$B.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index != -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index != -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438812, Missing bridge methods in indirect child classes with ECJ 3.10.0
public void testBug438812a() throws Exception {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.Collection;
				import java.util.List;
				
				public abstract class A {
				    abstract Iterable getIterable();
				}
				
				class B extends A {
				    public Collection getIterable() { return null; }
				}
				
				class C extends B {
				    public List getIterable() { return null; }
				}""",
		},
		"");
	String expectedOutput = "  public bridge synthetic java.lang.Iterable getIterable();";

	File f = new File(OUTPUT_DIR + File.separator + "C.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=461529, Abstract class extending interface with default impl won't compile, but does compile from cmd line
public void testBug461529() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return;
	this.runConformTest(
		new String[] {
			"foo/IBarable.java",
			"""
				package foo;
				public interface IBarable<T extends IBarable<T>> {
				    default IBar<T> createBar() {
				        throw new UnsupportedOperationException();
				    }
				}
				""",
			"foo/IBar.java",
			"""
				package foo;
				public interface IBar<T extends IBarable<T>> {
				    T bar();
				}
				""",
			"foo/Foo.java",
			"""
				package foo;
				public abstract class Foo implements IBarable<Foo> {
				    public abstract static class Builder implements IBar<Foo> {}
				    @Override
				    public abstract Builder createBar();
				}
				""",
			"foo/ChildFoo.java",
			"package foo;\n" +
			"public abstract class ChildFoo extends Foo {}\n"
		});
}
public void testBug467776_regression() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6) return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"ITeam.java",
			"""
				public interface ITeam {
				        <T> T getRole(Object o, Class<T> clazz);
				}
				""",
			"Team.java",
			"""
				public class Team implements ITeam {
				
				        @Override
				        public <T> T getRole(Object o, Class<T> clazz) {
				                return null;
				        }
				}
				""",
			"MyTeam.java",
			"""
				public class MyTeam extends Team {
				        @Override
				        public <T> T getRole(Object o, Class<T> clazz) {
				                return super.getRole(o, clazz);
				        }
				}
				"""

		},
		compilerOptions);
}
public void testBug500673() {
	runNegativeTest(
		new String[] {
			"mfi.java",
			"""
				interface mfi {
				    public transient void a(Throwable throwable);
				}
				""",
			"mfa.java",
			"final class mfa implements mfi {\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in mfi.java (at line 2)\n" +
		"	public transient void a(Throwable throwable);\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Illegal modifier for the interface method a; only public & abstract are permitted\n"
		: this.complianceLevel < ClassFileConstants.JDK9 ?
				"Illegal modifier for the interface method a; only public, abstract, default, static and strictfp are permitted\n" :
				"Illegal modifier for the interface method a; only public, private, abstract, default, static and strictfp are permitted\n"
		) +
		"----------\n" +
		"----------\n" +
		"1. ERROR in mfa.java (at line 1)\n" +
		"	final class mfa implements mfi {\n" +
		"	            ^^^\n" +
		"The type mfa must implement the inherited abstract method mfi.a(Throwable)\n" +
		"----------\n");
}
public void testBug506653() {
	runConformTest(
		false, // flushOutputDirectory
		new String[] {
			"A.java",
			"""
				   public class A {
				    public class B {
				        <E extends Object, F extends E> E bar(F x) {
				            return null;
				        }
				    }
				    public class C extends B {
						 @Override
				        public String bar(Object x) {
				            return "Oops";
				        }
				    }
				    public static void main(String... args) {
				        new A().test();
				    }
				    void test() {
				        B b = new C();
						 try {
				        	Integer i = b.bar(1);
						 } catch (ClassCastException cce) {
							System.out.print("cce");
						 }
				    }
				   }
				"""
		},
		"""
			----------
			1. WARNING in A.java (at line 9)
				public String bar(Object x) {
				       ^^^^^^
			Type safety: The return type String for bar(Object) from the type A.C needs unchecked conversion to conform to E from the type A.B
			----------
			""",
		"cce",
		"",
		JavacTestOptions.DEFAULT);
}
public void testBug536593() {
	runConformTest(
		new String[] {
			"AbstractComp.java",
			"""
				public abstract class AbstractComp {
					protected abstract boolean isReadOnly();
				}
				""",
			"HasValue.java",
			"""
				public interface HasValue<T> {
					boolean isReadOnly();
				}
				""",
			"Factory.java",
			"public class Factory<T, F extends AbstractComp & HasValue<T>> {\n" +
			"}\n"
		});
}
public void testBug536978_comment2() {
	runNegativeTest(
			new String[] {
				"SimpleDemo.java",
				"""
					abstract interface AbstractResult {
						public abstract int test();
					}
					
					abstract class AbstractDemo<Request extends AbstractResult, Response extends AbstractResult> {
						protected abstract Response test(Request request);
					}
					
					interface SimpleResult extends AbstractResult {};
					
					class Result1 implements SimpleResult {
					    public int test() { return 1; }
					}
					class OtherResult implements AbstractResult {
					    public int test() { return 2; }
					}
					
					public class SimpleDemo<Request extends AbstractResult, Response extends AbstractResult>\s
					extends AbstractDemo<Request, Response> {
					
					    @Override
					    protected SimpleResult test(AbstractResult request) {
					        return new Result1();
					    }
					   \s
					    public static void main(String... args) {
					        AbstractDemo<OtherResult,OtherResult> demo = new SimpleDemo<OtherResult,OtherResult>();
					        OtherResult result = demo.test(new OtherResult());
					    }
					
					}
					"""
			},
			"""
				----------
				1. ERROR in SimpleDemo.java (at line 22)
					protected SimpleResult test(AbstractResult request) {
					          ^^^^^^^^^^^^
				The return type is incompatible with AbstractDemo<Request,Response>.test(Request)
				----------
				""");
}
public void testBug536978_comment5() {
	String errMsg = isJRE11Plus
			? "class Result1 cannot be cast to class OtherResult (Result1 and OtherResult are in unnamed module of loader"
			: "Result1 cannot be cast to OtherResult";
	runConformTest(
		new String[] {
			"SimpleDemo.java",
			"""
				
				abstract interface AbstractResult {
					public abstract int test();
				}
				
				abstract class AbstractDemo<Request extends AbstractResult, Response extends AbstractResult> {
					protected abstract Response test(Request request);
				
				}
				
				class Result1 implements AbstractResult {
					public int test() {
						return 1;
					}
				}
				
				class OtherResult implements AbstractResult {
					public int test() {
						return 2;
					}
				}
				
				public class SimpleDemo<Request extends AbstractResult, Response extends AbstractResult> extends AbstractDemo<Request, Response> {
					@Override @SuppressWarnings("unchecked")
					protected AbstractResult test(AbstractResult request) {
						return new Result1();
					}
				
					public static void main(String... args) {
						AbstractDemo<OtherResult, OtherResult> demo = new SimpleDemo<OtherResult, OtherResult>();
						try {
							OtherResult result = demo.test(new OtherResult());
						} catch (ClassCastException e) {
							System.out.println(e.getMessage().replaceFirst("(unnamed module of loader).*", "$1"));
						}
					}
				}
				"""
		},
		errMsg);
}
}
