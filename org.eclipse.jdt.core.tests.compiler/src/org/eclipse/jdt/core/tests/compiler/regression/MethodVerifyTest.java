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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MethodVerifyTest extends AbstractComparableTest {

	public MethodVerifyTest(String name) {
		super(name);
	}

	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
	}
	
	public static Class testClass() {
		return MethodVerifyTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n" +
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\r\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\r\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"interface I<U> { public void foo(U u); }\n" +
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// J is not abstract and does not override abstract method foo(A) in I
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in YY.java (at line 1)\r\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\r\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n" +
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n"
			},
			""
		);
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
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\r\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\r\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// J is not abstract and does not override abstract method foo(A) in I
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in YY.java (at line 1)\r\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\r\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			""
		);
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
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\r\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\r\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n",
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n",
			// J is not abstract and does not override abstract method foo(A) in I
			null,
			false,
			null
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in YY.java (at line 1)\r\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\r\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n",
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. ERROR in JJ.java (at line 1)\r\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\r\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n",
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
			null,
			false,
			null
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
				"class A {}\n" +
				"class B {}\n" +
				"class X<U> { public U foo() {return null;} }\n" +
				"interface I<U> { public U foo(); }\n" +

				"class J<T> implements I<B> { public T foo() {return null;} }\n" +
				"class K<T> implements I<T> { public T foo() {return null;} }\n" +
				"class L<T> implements I { public T foo() {return null;} }\n" +

				"class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" +
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n" +
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 5)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                      ^^^^^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"3. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                                  ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"4. ERROR in ALL.java (at line 10)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                         ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n"
			/*
			ALL.java:5: J is not abstract and does not override abstract method foo() in I
			ALL.java:5: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			ALL.java:8: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			ALL.java:8: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			                                                           ^
			ALL.java:10: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			 */
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
				"class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                      ^^^^^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                                  ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in W.java (at line 1)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                         ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n"
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
				"class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
				},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                      ^^^^^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public T foo() { return super.foo(); } }\n" + 
			"	                                                  ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in W.java (at line 1)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                         ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n",
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
				"abstract class A implements I {}\n" +
				"interface I extends J { String foo(); }\n" +
				"interface J { Object foo(); }\n",
				"X.java",
				"abstract class X1 extends A implements J {}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I extends J { Object foo(); }\n" +
				"interface J { String foo(); }\n",
				"X.java",
				"abstract class X2 extends A implements J {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 1)\r\n" + 
			"	abstract class A implements I {}\r\n" + 
			"	               ^\n" + 
			"The return type is incompatible with J.foo(), I.foo()\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 2)\r\n" + 
			"	interface I extends J { Object foo(); }\r\n" + 
			"	                               ^^^^^\n" + 
			"The return type is incompatible with J.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X2 extends A implements J {}\r\n" + 
			"	               ^^\n" + 
			"The return type is incompatible with I.foo(), J.foo()\n" + 
			"----------\n"
		);
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
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public Object foo() { return null; } }\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X6 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X6 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"The return type is incompatible with I.foo(), A.foo()\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { int get(short i, short s) { return i; } }\n" +
				"class B extends A { short get(short i, short s) {return i; } }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class B extends A { short get(short i, short s) {return i; } }\r\n" + 
			"	                          ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The return type is incompatible with A.get(short, short)\n" + 
			"----------\n"
		);
	}

	public void test008() { // covariance test
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"interface I { I foo(); }\n" +
				"class A implements I { public A foo() { return null; } }\n" +
				"class B extends A { @Override public B foo() { return null; } }\n" +
				"class C extends B { public A foo() { return null; } }\n" +
				"class D extends B implements I {}\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 4)\r\n" + 
			"	class C extends B { public A foo() { return null; } }\r\n" + 
			"	                             ^^^^^\n" + 
			"The return type is incompatible with B.foo()\n" + 
			"----------\n"
			// foo() in C cannot override foo() in B; attempting to use incompatible return type
		);
	}

	public void test009() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class G<T> {}\n" +
				"interface I { void foo(G<I> x); }\n" +
				"abstract class A implements I { void foo(G<A> x) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\r\n" + 
			"	abstract class A implements I { void foo(G<A> x) {} }\r\n" + 
			"	                                     ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(G<A>) in A and foo(G<I>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class G<T> {}\n" +
				"interface I { I foo(G<I> x); }\n" +
				"abstract class A implements I { I foo(G<A> x) { return null; } }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\r\n" + 
			"	abstract class A implements I { I foo(G<A> x) { return null; } }\r\n" + 
			"	                                  ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(G<A>) in A and foo(G<I>) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test010() { // executable bridge method case
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public X foo() {\n" +
				"        System.out.println(\"Did NOT add bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"    public static void main(String[] args) throws Exception {\n" +
				"        X x = new A();\n" +
				"        x.foo();\n" +
				"        System.out.print(\" + \");\n" +
				"        I i = new A();\n" +
				"        i.foo();\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public I foo();\n" +
				"}\n" +
				"class A extends X implements I {\n" +
				"    public A foo() {\n" +
				"        System.out.print(\"Added bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"}\n"
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
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X1 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"The inherited method A.foo(T) cannot hide the public abstract method in I\n" + 
			"----------\n"
			// <T>foo(T) in A cannot implement <T>foo(T) in I; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X2 extends A implements I {}\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(T) in A and <T>foo(T) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T, S> void foo(T t); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X3 extends A implements I {}\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in A and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T s) {} }\n" +
				"class Y1 extends A { @Override void foo(Object s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class Y1 extends A { @Override void foo(Object s) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object) in Y1 cannot override <T>foo(T) in A; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T[] s) {} }\n" +
				"class Y2 extends A { @Override void foo(Object[] s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class Y2 extends A { @Override void foo(Object[] s) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object[]) in Y2 cannot override <T>foo(T[]) in A; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public void foo(Class<Object> s) {} }\n" +
				"class Y3 extends A { @Override void foo(Class<Object> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class Y3 extends A { @Override void foo(Class<Object> s) {} }\r\n" + 
			"	                                    ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Class<java.lang.Object>) in Y3 cannot override foo(java.lang.Class<java.lang.Object>) in A; attempting to assign weaker access privileges; was public
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
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X1 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(java.lang.Class<T>) in A and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T, S> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X2 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in A and <T,S>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> S foo(Class<T> s) { return null; } }\n" +
				"interface I { <T> Object foo(Class<T> s); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X3 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(java.lang.Class<T>) in A and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> Object foo(Class<T> s) { return null; } }\n" +
				"interface I { <T, S> S foo(Class<T> s); }\n",
				"X.java",
				"abstract class X4 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X4 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in A and <T,S>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",

				"X.java",
				"class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\r\n" + 
			"	                                                  ^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(Class<T>) of type X5 has the same erasure as foo(Class<T>) of type A but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in X5 and <T,S>foo(java.lang.Class<T>) in A have the same erasure, yet neither overrides the other
		);
	}

	public void test014() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A a) {} }\n" + 
				"class Y extends X { void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A[] a) {} }\n" + 
				"class Y extends X { void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A<String>[] a) {} }\n" + 
				"class Y extends X { void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A<String> a) {} }\n" + 
				"class Y extends X { void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X { void foo(A a) {} }\n" + 
				"class Y extends X { void foo(A<String> a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { void foo(A<String> a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X { void foo(A[] a) {} }\n" + 
				"class Y extends X { void foo(A<String>[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { void foo(A<String>[] a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in X have the same erasure, yet neither overrides the other
		);
	}

	public void test015() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A a); }\n" + 
				"class Y { public void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A[] a); }\n" + 
				"class Y { public void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A<String>[] a); }\n" + 
				"class Y { public void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A<String> a); }\n" + 
				"class Y { public void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A a); }\n" + 
				"class Y { public void foo(A<String> a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X extends Y implements I { }\r\n" + 
			"	               ^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A[] a); }\n" + 
				"class Y { public void foo(A<String>[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X extends Y implements I { }\r\n" + 
			"	               ^\n" + 
			"Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test016() { // 73971
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<E extends A> void m(E e) { System.out.print(\"A=\"+e.getClass()); }\n" + 
				"	<E extends B> void m(E e) { System.out.print(\"B=\"+e.getClass()); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().m(new A());\n" +
				"		new X().m(new B());\n" + 
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n"
			},
			"A=class AB=class B"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static <E extends A> void m(E e) { System.out.print(\"A=\"+e.getClass()); }\n" + 
				"	static <E extends B> void m(E e) { System.out.print(\"B=\"+e.getClass()); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		m(new A());\n" +
				"		m(new B());\n" + 
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n"
			},
			"A=class AB=class B"
		);
	}

	public void test017() { // 77785
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {}\n" + 
				"class Y { void test(X<? extends Number> a) {} }\n" + 
				"class Z extends Y { void test(X<Number> a) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	class Z extends Y { void test(X<Number> a) {} }\r\n" + 
			"	                         ^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(X<Number>) of type Z has the same erasure as test(X<? extends Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<java.lang.Number>) in Z and test(X<? extends java.lang.Number>) in Y have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {}\n" + 
				"class Y { void test(X<Number> a) {} }\n" + 
				"class Z extends Y { void test(X<? extends Number> a) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	class Z extends Y { void test(X<? extends Number> a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(X<? extends Number>) of type Z has the same erasure as test(X<Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<? extends java.lang.Number>) in Z and test(X<java.lang.Number>) in Y have the same erasure, yet neither overrides the other
		);
	}

	public void test018() { // 77861
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X implements Comparable<X> {\n" + 
				"	public int compareTo(Object o) { return 0; }\n" + 
				"	public int compareTo(X o) { return 1; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public int compareTo(Object o) { return 0; }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in X and compareTo(T) in java.lang.Comparable<X> have the same erasure, yet neither overrides the other
		);
	}

	public void test019() { // 78140
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	<T> T get() { return null; } \n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	<T> T get() { return null; } \n" + 
				"}\n"
			},
			""
		);
	}

	public void test020() { // 78232
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		AbstractBase ab = new AbstractBase();\n" + 
				"		Derived d = new Derived();\n" + 
				"		AbstractBase ab2 = new Derived();\n" + 
				"		Visitor<String, String> v = new MyVisitor();\n" + 
				"		System.out.print(ab.accept(v, ab.getClass().getName()));\n" + 
				"		System.out.print('+');\n" + 
				"		System.out.print(d.accept(v, d.getClass().getName()));\n" + 
				"		System.out.print('+');\n" + 
				"		System.out.print(ab2.accept(v, ab2.getClass().getName()));\n" + 
				"	}\n" + 
				"	static class MyVisitor implements Visitor<String, String> {\n" + 
				"		public String visitBase(AbstractBase ab, String obj) { return \"Visited base: \" + obj; }\n" + 
				"		public String visitDerived(Derived d, String obj) { return \"Visited derived: \" + obj; }\n" + 
				"	}\n" + 
				"}\n" + 
				"interface Visitor<R, T> {\n" + 
				"	R visitBase(AbstractBase ab, T obj);\n" + 
				"	R visitDerived(Derived d, T obj);\n" + 
				"}\n" + 
				"interface Visitable {\n" + 
				"	<R, T> R accept(Visitor<R, T> v, T obj);\n" + 
				"}\n" + 
				"class AbstractBase implements Visitable {\n" + 
				"	public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitBase(this, obj); }\n" + 
				"}\n" + 
				"class Derived extends AbstractBase implements Visitable {\n" + 
				"	public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitDerived(this, obj); }\n" + 
				"}\n"
			},
			"Visited base: AbstractBase+Visited derived: Derived+Visited derived: Derived"
		);
	}

	public void test021() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	public void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n",
				"B.java",
				"class B extends A {\n" + 
				"	@Override void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	@Override void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
		);
		// now save A & pick it up as a binary type
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	public void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"class B extends A {\n" + 
				"	@Override void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	@Override void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n",
			null,
			false,
			null
		);
	}

	public void test022() { // 77562
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List getList() { return null; } }\n" + 
				"class B extends A { @Override List<String> getList() { return null; } }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List<String> getList() { return null; } }\n" + 
				"class B extends A { @Override List getList() { return null; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { @Override List getList() { return null; } }\n" + 
			"	                              ^^^^\n" + 
			"Type safety: The return type List for getList() from the type B needs unchecked conversion to conform to List<String> from the type A\n" + 
			"----------\n"
			// unchecked warning on B.getList()
		);
	}

	public void test023() { // 80739
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A<T> {\n" + 
				"	void foo(T t) {}\n" + 
				"	void foo(String i) {}\n" + 
				"}\n" + 
				"class B extends A<String> {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 5)\r\n" + 
			"	class B extends A<String> {}\r\n" + 
			"	      ^\n" + 
			"Duplicate methods named foo with the parameters (String) and (T) are defined by the type A<String>\n" + 
			"----------\n"
			// methods foo(T) from A<java.lang.String> and foo(java.lang.String) from A<java.lang.String> are inherited with the same signature
		);
	}

	public void test024() { // 80626
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n"
			},
			""
			// no complaint
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 5)\r\n" + 
			"	public <E extends Object> void m(E e) {}\r\n" + 
			"	                               ^^^^^^\n" + 
			"Name clash: The method m(E) of type B has the same erasure as m(Object) of type A but does not override it\n" + 
			"----------\n"
			// name clash: <E>m(E) in B and m(java.lang.Object) in A have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	@Override public void m(Object e) {}\n" + 
				"}\n" + 
				"class C extends B {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 8)\r\n" + 
			"	public <E extends Object> void m(E e) {}\r\n" + 
			"	                               ^^^^^^\n" + 
			"Name clash: The method m(E) of type C has the same erasure as m(Object) of type B but does not override it\n" + 
			"----------\n"
			// name clash: <E>m(E) in C and m(java.lang.Object) in B have the same erasure, yet neither overrides the other
		);
	}

	public void test025() { // 81618
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new B().test();\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T extends Number> T test() { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	@Override Integer test() { return 1; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	@Override Integer test() { return 1; }\n" + 
			"	          ^^^^^^^\n" + 
			"Type safety: The return type Integer for test() from the type B needs unchecked conversion to conform to T from the type A\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new B().test();\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T extends Number> T[] test() { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	@Override Integer[] test() { return new Integer[] {2}; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	@Override Integer[] test() { return new Integer[] {2}; }\n" + 
			"	          ^^^^^^^^^\n" + 
			"Type safety: The return type Integer[] for test() from the type B needs unchecked conversion to conform to T[] from the type A\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new B().<Integer>test(new Integer(1)));\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T> T test(T t) { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	@Override <T> T test(T t) { return t; }\n" + 
				"}\n"
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new B().<Number>test(1));\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {\n" + 
				"	<U> T test(U u) { return null; }\n" + 
				"}\n" +
				"class B extends A<Integer> {\n" + 
				"	@Override <U> Integer test(U u) { return 1; }\n" + 
				"}\n"
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.util.concurrent.Callable;\n" + 
				"public class A {\n" + 
				"	public static void main(String[] args) throws Exception {\n" + 
				"		Callable<Integer> integerCallable = new Callable<Integer>() {\n" + 
				"			public Integer call() { return new Integer(1); }\n" + 
				"		};\n" + 
				"		System.out.println(integerCallable.call());\n" + 
				"	}\n" + 
				"}\n"
			},
			"1"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T extends X> { T x(); }\n" +
				"abstract class Y<S extends X> implements X<S> { public abstract S x(); }\n" +
				"abstract class Z implements X { public abstract X x(); }\n"
			},
			"" // no warnings
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T extends X> { T[] x(); }\n" +
				"abstract class Y<S extends X> implements X<S> { public abstract S[] x(); }\n" +
				"abstract class Z implements X { public abstract X[] x(); }\n"
			},
			"" // no warnings
		);
	}

	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(\n" + 
				"			new B().test().getClass() + \" & \"\n" + 
				"			+ new C().test().getClass() + \" & \"\n" + 
				"			+ new D().test().getClass());\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T extends Number> {\n" + 
				"	A<T> test() { return this; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	A test() { return super.test(); }\n" + 
				"}\n" +
				"class C extends A<Integer> {\n" + 
				"	A<Integer> test() { return super.test(); }\n" + 
				"}\n" +
				"class D<U, V extends Number> extends A<V> {\n" + 
				"	A<V> test() { return super.test(); }\n" + 
				"}\n"
			},
			"class B & class C & class D"
		);
		this.runConformTest(
			new String[] {
				"A.java",
				"public abstract class A<E> {\n" + 
				"	public abstract A<E> test();\n" + 
				"}\n" +
				"class H<K,V> {\n" + 
				"	class M extends A<K> {\n" + 
				"		public A<K> test() { return null; }\n" + 
				"	}\n" +
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends java.util.AbstractMap {\n" + 
				"	public java.util.Set entrySet() { return null; }\n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(new C().test().getClass());\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T extends Number> {\n" + 
				"	A<T> test() { return this; }\n" + 
				"}\n" +
				"class C extends A<Integer> {\n" + 
				"	@Override A test() { return super.test(); }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	@Override A test() { return super.test(); }\n" + 
			"	          ^\n" + 
			"Type safety: The return type A for test() from the type C needs unchecked conversion to conform to A<T> from the type A<T>\n" + 
			"----------\n"
			// warning: test() in C overrides test() in A; return type requires unchecked conversion
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
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test() {} }\n" + 
				"class Y extends X { <T> void test() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { <T> void test() {} }\n" + 
			"	                             ^^^^^^\n" + 
			"Name clash: The method test() of type Y has the same erasure as test() of type X but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo() in Y and foo() in X have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o) {} }\n" + 
				"class Y<T> extends X<T> { void test(Object o) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class Y<T> extends X<T> { void test(Object o) {} }\r\n" + 
			"	                               ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Object) of type Y<T> has the same erasure as test(T) of type X<T> but does not override it\n" + 
			"----------\n"
			// no error unless you try to do a super send which then fails
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o, T t) {} }\n" + 
				"class Y<T> extends X<T> { void test(Object o, T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class Y<T> extends X<T> { void test(Object o, T t) {} }\r\n" + 
			"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Object, T) of type Y<T> has the same erasure as test(T, T) of type X<T> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Object,T) in Y<T> and test(T,T) in X<T> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void test() {\n" + 
				"		Pair<Double, Integer> p = new InvertedPair<Integer, Double>();\n" + 
				"		p.setA(new Double(1.1));\n" + 
				"	}\n" + 
				"}\n" +
				"class Pair<A, B> {\n" + 
				"	public void setA(A a) {}\n" + 
				"}\n" +
				"class InvertedPair<A, B> extends Pair<B, A> {\n" + 
				"	public void setA(A a) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	public void setA(A a) {}\n" + 
			"	            ^^^^^^^^^\n" + 
			"Name clash: The method setA(A) of type InvertedPair<A,B> has the same erasure as setA(A) of type Pair<A,B> but does not override it\n" + 
			"----------\n"
			// name clash: setA(A) in InvertedPair<A,B> and setA(A) in Pair<B,A> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81727
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I<X>{\n" + 
				"	public X foo() { return null; }\n" + 
				"}\n" +
				"interface I<T extends I> { T foo(); }\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81568
	public void test029() {
		this.runConformTest(
			new String[] {
				"I.java",
				"public interface I {\n" + 
				"	public I clone();\n" + 
				"}\n" +
				"interface J extends I {}\n"
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
				"import java.io.OutputStreamWriter;\n" + 
				"import java.io.PrintWriter;\n" + 
				"public class X extends PrintWriter implements Runnable {\n" + 
				"	public X(OutputStreamWriter out, boolean flag) { super(out, flag); }\n" +
				"	public void run() {}\n" +
				"}\n"
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
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	interface X { long hashCode(); }\r\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"The return type is incompatible with Object.hashCode()\n" + 
			"----------\n"
			// hashCode() in X cannot override hashCode() in java.lang.Object; attempting to use incompatible return type
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736
	public void test032() {
		// NOTE: javac only reports these errors when the problem type follows the bounds
		// if the type X is defined first, then no errors are reported
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I { Float foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X<T extends I&J> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	public class X<T extends I&J> {}\r\n" + 
			"	             ^\n" + 
			"The return type is incompatible with J.foo(), I.foo()\n" + 
			"----------\n"
			// types J and I are incompatible; both define foo(), but with unrelated return types
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I { String foo(); }\n" +
				"class A { public Object foo() { return null; } }" +
				"public class X<T extends A&I> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class A { public Object foo() { return null; } }public class X<T extends A&I> {}\r\n" + 
			"	                                                             ^\n" + 
			"The return type is incompatible with I.foo(), A.foo()\n" + 
			"----------\n"
			// foo() in A cannot implement foo() in I; attempting to use incompatible return type
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80745
	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Number foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X implements I, J {\n" +
				"	public Integer foo() {return 1;}\n" +
				"	public static void main(String argv[]) {\n" +
				"		I i = null;\n" +
				"		J j = null;\n" +
				"		System.out.print(i instanceof J);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(j instanceof I);\n" +
				"	}\n" +
				"}\n"
			},
			"false=false"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Number foo(A a); }\n" +
				"interface J<T> { Integer foo(A<T> a); }\n" +
				"class A<T>{}\n" +
				"public class X implements I, J {\n" +
				"	public Integer foo(A a) {return 1;}\n" +
				"	public static void main(String argv[]) {\n" +
				"		I i = null;\n" +
				"		J j = null;\n" +
				"		System.out.print(i instanceof J);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(j instanceof I);\n" +
				"	}\n" +
				"}\n"
			},
			"false=false"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034() {
		this.runConformTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable<E>> { void test(E element); }\n" +
				"class A implements I<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(String i) {} }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable> { void test(E element); }\n" +
				"class A { public void test(Integer i) {} }\n" +
				"public class B extends A implements I<Integer> {}\n" +
				"class C extends B { public void test(Object i) {} }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable> { void test(E element); }\n" +
				"class A { public void test(Integer i) {} }\n" +
				"public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n" + 
			"	                                                             ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in I<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable<E>> { void test(E element); }\n" +
				"class A implements I<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in I<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"abstract class AA<E extends Comparable> { abstract void test(E element); }\n" +
				"class A extends AA<Integer> { @Override public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type AA<E> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in AA<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80626
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"interface I<U>{ int compareTo(U o); }\n" +
				"abstract class F<T extends F<T>> implements I<T>{ public final int compareTo(T o) { return 0; } }\n" +
				"public class E extends F<E> { public int compareTo(Object o) { return 0; } }\n"
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 3)\n" + 
			"	public class E extends F<E> { public int compareTo(Object o) { return 0; } }\n" + 
			"	                                         ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type E has the same erasure as compareTo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in E and compareTo(U) in I<E> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	;\n" +
				"	public int compareTo(Object o) { return 0; }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	public int compareTo(Object o) { return 0; }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in X and compareTo(T) in java.lang.Comparable<X> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036() { // 2 interface cases
		// no bridge methods are created in these conform cases so no name clashes can occur
		this.runConformTest(
			new String[] {
				"X.java",
				"class X implements Equivalent, EqualityComparable {\n" +
				"	public boolean equalTo(Object other) { return true; }\n" +
				"}\n" +
				"abstract class Y implements Equivalent, EqualityComparable {}\n" +
				"class Z extends Y {\n" +
				"	public boolean equalTo(Object other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X implements Equivalent, EqualityComparable {\n" +
				"	public boolean equalTo(Comparable other) { return true; }\n" +
				"	public boolean equalTo(Number other) { return true; }\n" +
				"}\n" +
				"abstract class Y implements Equivalent, EqualityComparable {}\n" +
				"class Z extends Y {\n" +
				"	public boolean equalTo(Comparable other) { return true; }\n" +
				"	public boolean equalTo(Number other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T extends Comparable> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T extends Number> { boolean equalTo(T other); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X<S> implements Equivalent<S>, EqualityComparable<S> {\n" +
				"	public boolean equalTo(S other) { return true; }\n" +
				"}\n" +
				"abstract class Y<S> implements Equivalent<S>, EqualityComparable<S> {}\n" +
				"class Z<U> extends Y<U> {\n" +
				"	public boolean equalTo(U other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"class X<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {\n" +
				"	public boolean equalTo(T other) { return true; }\n" +
				"	public boolean equalTo(S other) { return true; }\n" +
				"}\n" +
				"abstract class Y<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {}\n" +
				"class Z<U extends Comparable, V extends Number> extends Y<U, V> {\n" +
				"	public boolean equalTo(U other) { return true; }\n" +
				"	public boolean equalTo(V other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T extends Comparable> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<S extends Number> { boolean equalTo(S other); }\n"
			},
			""
		);

		// in these cases, bridge methods are needed once abstract/concrete methods are defiined (either in the abstract class or a concrete subclass)
		this.runConformTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(Number other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
			// no bridge methods are created here since Y does not define an equalTo(?) method which equals an inherited equalTo method
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(Object other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(java.lang.Object) in Y and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
		);
		// NOTE: javac has a bug, reverse the implemented interfaces & the name clash goes away
		// but eventually when a concrete subclass must define the remaining method, the error shows up
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(String other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type Equivalent<T> has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(T) in Equivalent<java.lang.String> and equalTo(T) in EqualityComparable<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {\n" +
				"	public boolean equalTo(Integer other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(T) in EqualityComparable<java.lang.Integer> and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}

	public void test037() { // test inheritance scenarios
		this.runConformTest(
			new String[] {
				"X.java",
				"public abstract class X implements I, J { }\n" +
				"abstract class Y implements J, I { }\n" +
				"abstract class Z implements K { }\n" +

				"class YYY implements J, I { public void foo(A a) {} }\n" +
				"class XXX implements I, J { public void foo(A a) {} }\n" +
				"class ZZZ implements K { public void foo(A a) {} }\n" +

				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"interface K extends I { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public abstract class XX implements I, J { public abstract void foo(A<String> a); }\n" +
				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 1)\r\n" + 
			"	public abstract class XX implements I, J { public abstract void foo(A<String> a); }\r\n" + 
			"	                                                                ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in XX and foo(A) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public class XX implements I, J { public void foo(A<String> a) {} }\n" +
				"class YY implements J, I { public void foo(A<String> a) {} }\n" +
				"class ZZ implements K { public void foo(A<String> a) {} }\n" +

				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"interface K extends I { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 1)\n" + 
			"	public class XX implements I, J { public void foo(A<String> a) {} }\n" + 
			"	             ^^\n" + 
			"The type XX must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in XX.java (at line 1)\n" + 
			"	public class XX implements I, J { public void foo(A<String> a) {} }\n" + 
			"	                                              ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"3. ERROR in XX.java (at line 2)\n" + 
			"	class YY implements J, I { public void foo(A<String> a) {} }\n" + 
			"	      ^^\n" + 
			"The type YY must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"4. ERROR in XX.java (at line 2)\n" + 
			"	class YY implements J, I { public void foo(A<String> a) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type YY has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"5. ERROR in XX.java (at line 3)\n" + 
			"	class ZZ implements K { public void foo(A<String> a) {} }\n" + 
			"	      ^^\n" + 
			"The type ZZ must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"6. ERROR in XX.java (at line 3)\n" + 
			"	class ZZ implements K { public void foo(A<String> a) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type ZZ has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// XX/YY/ZZ is not abstract and does not override abstract method foo(A) in I
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { void foo(A a); }\n" +
				"class Y { void foo(A<String> a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in I have the same erasure, yet neither overrides the other
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { void foo(A<String> a); }\n" +
				"class Y { void foo(A a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"The inherited method Y.foo(A) cannot hide the public abstract method in I\n" + 
			"----------\n"
			// foo(A) in Y cannot implement foo(A<java.lang.String>) in I; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { <T, S> void foo(T t); }\n" +
				"class Y { <T> void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"Name clash: The method foo(T) of type Y has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in Y and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends H<Object> { void foo(A<?> a) { super.foo(a); } }\n" +
				"class H<T extends Object> { void foo(A<? extends T> a) {} }\n" +
				"class A<T> {}"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends H<Number> { void foo(A<?> a) {} }\n" +
				"class H<T extends Number> { void foo(A<? extends T> a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X extends H<Number> { void foo(A<?> a) {} }\r\n" + 
			"	                                        ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<?>) of type X has the same erasure as foo(A<? extends T>) of type H<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<?>) in X and foo(A<? extends T>) in H<java.lang.Number> have the same erasure, yet neither overrides the other
			// with    public class X extends H<Number> { void foo(A<?> a) { super.foo(a); } }
			// foo(A<? extends java.lang.Number>) in H<java.lang.Number> cannot be applied to (A<capture of ?>)
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83573
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static void main(String[] args) {\n" + 
				"      Test test = new Test();\n" + 
				"      This test2 = new Test();\n" + 
				"      System.out.println(test.get());\n" + 
				"   }\n" + 
				"   interface This {\n" + 
				"      public Object get();\n" + 
				"   }\n" + 
				" \n" + 
				"   interface That extends This {\n" + 
				"      public String get();\n" + 
				" \n" + 
				"   }\n" + 
				" \n" + 
				"   static class Test implements That {\n" + 
				" \n" + 
				"      public String get() {\n" + 
				"         return \"That\";\n" + 
				" \n" + 
				"      }\n" + 
				"   }\n" + 
				"}\n"
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
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T extends Number> T test() { return null; } }\n" +
				"class B extends A { @Override Integer test() { return 1; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 2)\n" + 
			"	class B extends A { @Override Integer test() { return 1; } }\n" + 
			"	                              ^^^^^^^\n" + 
			"Type safety: The return type Integer for test() from the type B needs unchecked conversion to conform to T from the type A\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List<String> getList() { return null; } }\n" + 
				"class B extends A { @Override List getList() { return null; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { @Override List getList() { return null; } }\n" + 
			"	                              ^^^^\n" + 
			"Type safety: The return type List for getList() from the type B needs unchecked conversion to conform to List<String> from the type A\n" + 
			"----------\n"
			// unchecked warning on B.getList()
		);

		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T> { X<T> x(); }\n" +
				"abstract class Y<S> implements X<S> { public abstract X x(); }\n" + // warning: x() in Y implements x() in X; return type requires unchecked conversion
				"abstract class Z implements X { public abstract X x(); }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S> implements X<S> { public abstract X x(); }\n" + 
			"	                                                      ^\n" + 
			"Type safety: The return type X for x() from the type Y<S> needs unchecked conversion to conform to X<T> from the type X<T>\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T> { X<T>[] x(); }\n" +
				"abstract class Y<S> implements X<S> { public abstract X[] x(); }\n" + // warning: x() in Y implements x() in X; return type requires unchecked conversion
				"abstract class Z implements X { public abstract X[] x(); }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S> implements X<S> { public abstract X[] x(); }\n" + 
			"	                                                      ^^^\n" + 
			"Type safety: The return type X[] for x() from the type Y<S> needs unchecked conversion to conform to X<T>[] from the type X<T>\n" + 
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public void foo(String... n) {} }\n" +
				"interface I { void foo(String[] n); }\n" +
				"class Y extends X implements I { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Varargs methods should only override other varargs methods unlike X.foo(String...) and I.foo(String[])\n" + 
			"----------\n"
			// warning: foo(java.lang.String...) in X cannot implement foo(java.lang.String[]) in I; overridden method has no '...'
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public void foo(String[] n) {} }\n" +
				"interface I { void foo(String... n); }\n" +
				"class Y extends X implements I { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Varargs methods should only override other varargs methods unlike X.foo(String[]) and I.foo(String...)\n" + 
			"----------\n"
			// warning: foo(java.lang.String[]) in X cannot implement foo(java.lang.String...) in I; overriding method is missing '...'
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public Y foo() {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		((I) new Y()).foo();\n" +
				"	}\n" +
				"}\n" +
				"interface I { X foo(); }\n" +
				"class Y extends X implements I { }\n"
			},
			"SUCCESS"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public A foo() { return null; } }\n" +
				"interface I { A<String> foo(); }\n" +
				"class Y extends X implements I { }\n" +
				"class A<T> { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Type safety: The return type A for foo() from the type X needs unchecked conversion to conform to A<String> from the type I\n" + 
			"----------\n"
			// warning: foo() in X implements foo() in I; return type requires unchecked conversion
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public Object foo() { return null; } }\n" +
				"interface I { <T> T foo(); }\n" +
				"class Y extends X implements I { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Type safety: The return type Object for foo() from the type X needs unchecked conversion to conform to T from the type I\n" + 
			"----------\n"
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
				"interface Callable<T>\n" + 
				"{\n" + 
				"    public enum Result { GOOD, BAD };\n" + 
				"    public Result call(T arg);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements Callable<String>\n" + 
				"{\n" + 
				"    public Result call(String arg) { return Result.GOOD; } // Warning line\n" + 
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test(E<Integer,Integer> e) { e.id(new Integer(1)); } }\n" +
				"abstract class C<A> { public abstract void id(A x); }\n" +
				"interface I<B> { void id(B x); }\n" +
				"abstract class E<A, B> extends C<A> implements I<B> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(1)); } }\n" + 
			"	                                                     ^^\n" + 
			"The method id(Integer) is ambiguous for the type E<Integer,Integer>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	abstract class E<A, B> extends C<A> implements I<B> {}\n" + 
			"	               ^\n" + 
			"Name clash: The method id(A) of type C<A> has the same erasure as id(B) of type I<B> but does not override it\n" + 
			"----------\n"
			// javac won't report it until C.id() is made concrete or implemented in E
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test(E<Integer,Integer> e) { e.id(new Integer(2)); } }\n" +
				"abstract class C<A extends Number> { public abstract void id(A x); }\n" +
				"interface I<B> { void id(B x); }\n" +
				"abstract class E<A extends Number, B> extends C<A> implements I<B> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(2)); } }\r\n" + 
			"	                                                     ^^\n" + 
			"The method id(Integer) is ambiguous for the type E<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in I<java.lang.Integer> match
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test(E<Integer,Integer> e) { e.id(new Integer(111)); } }\n" +
				"abstract class C<A extends Number> { public void id(A x) {} }\n" +
				"interface I<B> { void id(B x); }\n" +
				"class E<A extends Number, B> extends C<A> implements I<B> { public void id(B b) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(111)); } }\r\n" + 
			"	                                                     ^^\n" + 
			"The method id(Integer) is ambiguous for the type E<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in E<java.lang.Integer,java.lang.Integer> match
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(E<Integer,Integer> e) { e.id(new Integer(111)); }\n" +
				"	void test(M<Integer,Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"		((E<Integer, Integer>) m).id(new Integer(111));\n" +
				"	}\n" +
				"	void test(N<Integer> n) { n.id(new Integer(111)); }\n" +
				"}\n" +
				"abstract class C<A extends Number> { public void id(A x) {} }\n" +
				"interface I<B> { void id(B x); }\n" +
				"abstract class E<A extends Number, B> extends C<A> implements I<B> {}\n" +
				"class M<A extends Number, B> extends E<A, B> { public void id(B b) {} }\n" +
				"abstract class N<T extends Number> extends E<T, Number> { @Override public void id(T n) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\r\n" + 
			"	m.id(new Integer(111));\r\n" + 
			"	  ^^\n" + 
			"The method id(Integer) is ambiguous for the type M<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in M<java.lang.Integer,java.lang.Integer> match
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97161
	public void test043a() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p.Y.*;\n" +
				"import static p.Z.*;\n" +
				"public class X {\n" +
				"	Y data = null;\n" +
				"	public X() { foo(data.l); }\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"import java.util.List;\n" +
				"public class Y {\n" +
				"	List l = null;\n" +
				"	public static <T> void foo(T... e) {}\n" +
				"}\n",
				"p/Z.java",
				"package p;\n" +
				"import java.util.List;\n" +
				"public class Z {\n" +
				"	public static <T> void foo(List<T>... e) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation foo(List...) of the generic method foo(List<T>...) of type Z\n" + 
			"----------\n" + 
			"2. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	                 ^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<T>\n" + 
			"----------\n"
			// unchecked conversion warnings
		);
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p.Y.*;\n" +
				"public class X {\n" +
				"	Y data = null;\n" +
				"	public X() { foo(data.l); }\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"import java.util.List;\n" +
				"public class Y {\n" +
				"	List l = null;\n" +
				"	public static <T> void foo(T... e) {}\n" +
				"	public static <T> void foo(List<T>... e) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation foo(List...) of the generic method foo(List<T>...) of type Y\n" + 
			"----------\n" + 
			"2. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	                 ^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<T>\n" + 
			"----------\n"
			// unchecked conversion warnings
		);
	}

	public void test043b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer,Integer> m) {\n" +
				"		m.id(new Integer(111), new Integer(112));\n" +
				"	}\n" +
				"}\n" +
				"abstract class C<T1 extends Number> { public <U1 extends Number> void id(T1 x, U1 u) {} }\n" +
				"interface I<T2> { }\n" +
				"abstract class E<T3 extends Number, T4> extends C<T3> implements I<T4> {}\n" +
				"class M<T5 extends Number, T6> extends E<T5, T6> { public <U2 extends Number> void id(T5 b, U2 u) {} }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer,Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"	}\n" +
				"}\n" +
				"abstract class C<T1 extends Number> { public void id(T1 x) {} }\n" +
				"interface I<T2> { void id(T2 x); }\n" +
				"abstract class E<T3 extends Number, T4> extends C<T3> implements I<T4> {}\n" +
				"class M<T5 extends Number, T6> extends E<T5, T6> { public void id(T6 b) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	m.id(new Integer(111));\n" + 
			"	  ^^\n" + 
			"The method id(Integer) is ambiguous for the type M<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in M<java.lang.Integer,java.lang.Integer> match
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
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override void foo() {} }\n" + 
			"	                                    ^^^^^\n" + 
			"Cannot override the final method from X\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public void foo() {} }\n" + 
				"class XS extends X { @Override void foo() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override void foo() {} }\n" + 
			"	                                    ^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from X\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() {} }\n" + 
				"class XS extends X { @Override void foo() throws ClassNotFoundException {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override void foo() throws ClassNotFoundException {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Exception ClassNotFoundException is not compatible with throws clause in X.foo()\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() {} }\n" + 
				"class XS extends X { @Override int foo() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override int foo() {} }\n" + 
			"	                                   ^^^^^\n" + 
			"The return type is incompatible with X.foo()\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override int foo() {} }\n" + 
			"	                                   ^^^^^\n" + 
			"The method foo() of type XS must override a superclass method\n" + 
			"----------\n"
		);
	}

	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Foo {}\n" + 
				"\n" + 
				"interface Bar {\n" + 
				"  Foo get(Class<?> c);\n" + 
				"}\n" + 
				"public class X implements Bar {\n" + 
				"  public Foo get(Class c) { return null; }\n" + 
				"}\n"
			},
			""
		);
	}

	// ensure no unchecked warning
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX <T> {\n" + 
				"	public T doSomething();\n" + 
				"}\n" + 
				"public class X implements IX<Integer> {\n" + 
				"   Zork z;\n" +
				"	public Integer doSomething() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87157
	public void test047() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface Interface {\n" + 
				"    Number getValue();\n" + 
				"}\n" + 
				"class C1 {\n" + 
				"    public Double getValue() {\n" + 
				"        return 0.0;\n" + 
				"    }\n" + 
				"}\n" + 
				"public class X extends C1 implements Interface{\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Interface i=new X();\n" + 
				"        System.out.println(i.getValue());\n" + 
				"    }\n" + 
				"}\n"
			},
		"0.0");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048() {
		this.runConformTest(
			new String[] {
				"X1.java",
				"import java.util.*;\n" + 
				"public class X1 extends LinkedHashMap<String, String> {\n" + 
				"    public Object putAll(Map<String,String> a) { return null; }\n" + 
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X2.java",
				"public class X2 extends Y<String> {\n" + 
				"    public Object foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public void foo(I<? extends T> a) {}\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public void foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X3.java",
				"public class X3 extends Y<String> {\n" + 
				"    public void foo(I<String> z) {}\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public void foo(I<? extends T> a) {}\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public void foo(I<? extends T> a);\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X3.java (at line 2)\r\n" + 
			"	public void foo(I<String> z) {}\r\n" + 
			"	            ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(I<String>) of type X3 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(I<java.lang.String>) in X and foo(I<? extends T>) in Y<java.lang.String> have the same erasure, yet neither overrides the other
		);
		this.runConformTest(
			new String[] {
				"X4.java",
				"public class X4 extends Y<String> {\n" + 
				"    public String foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public Object foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public Object foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X5.java",
				"public class X5 extends Y<String> {\n" + 
				"    public Object foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public String foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public String foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X6.java",
				"public class X6 extends Y<String> {\n" + 
				"    public void foo(I<String> z) {}\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public Object foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public Object foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X7.java",
				"public class X7 extends Y<String> {\n" + 
				"    public String foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public T foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public T foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X8.java",
				"public class X8 extends Y<String> {\n" + 
				"    public Object foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public T foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public T foo(I<? extends T> a);\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X8.java (at line 2)\r\n" + 
			"	public Object foo(I<String> z) { return null; }\r\n" + 
			"	              ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(I<String>) of type X8 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(I<java.lang.String>) in X7 and foo(I<? extends T>) in Y<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88094
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"	T id(T x) { return x; }\n" + 
				"	A id(A x) { return x; }\n" + 
				"}\n" +
				"class Y<T extends A> extends X<T> {\n" + 
				"	@Override T id(T x) { return x; }\n" + 
				"	@Override A id(A x) { return x; }\n" + 
				"}\n" + 
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class Y<T extends A> extends X<T> {\n" + 
			"	      ^\n" + 
			"Name clash: The method id(A) of type X<T> has the same erasure as id(T) of type X<T> but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	@Override T id(T x) { return x; }\n" + 
			"	            ^^^^^^^\n" + 
			"Method id(T) has the same erasure id(A) as another method in type Y<T>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	@Override A id(A x) { return x; }\n" + 
			"	            ^^^^^^^\n" + 
			"Duplicate method id(A) in type Y<T>\n" + 
			"----------\n"
			// id(T) is already defined in Y
			// id(java.lang.String) in Y overrides id(T) in X; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94754
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 public static <S extends A> S foo() { System.out.print(\"A\"); return null; }\n" + 
				"		 public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
				"		 public static void main(String[] args) {\n" + 
				"		 	X.<A>foo();\n" + 
				"		 	X.<B>foo();\n" + 
				"		 	new X().<B>foo();\n" + 
				"		 }\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B {}\n"
			},
			"ABB"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 public static <S extends A> void foo() { System.out.print(\"A\"); }\n" + 
				"		 public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
				"		 static void test () {\n" + 
				"		 	X.foo();\n" + 
				"		 	foo();\n" + 
				"		 }\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\r\n" + 
			"	X.foo();\r\n" + 
			"	  ^^^\n" + 
			"The method foo() is ambiguous for the type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\r\n" + 
			"	foo();\r\n" + 
			"	^^^\n" + 
			"The method foo() is ambiguous for the type X\n" + 
			"----------\n"
			// both references are ambiguous
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class C1 {\n" + 
				"		Y foo(Object o) {  return null; } // duplicate\n" + 
				"		Z foo(Object o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"	class C2 {\n" + 
				"		<T extends Y> T foo(Object o) {  return null; } // ok\n" + 
				"		<T extends Z> T foo(Object o) {  return null; } // ok\n" + 
				"	}\n" + 
				"	class C3 {\n" + 
				"		A<Y> foo(Object o) {  return null; } // duplicate\n" + 
				"		A<Z> foo(Object o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"	class C4 {\n" + 
				"		Y foo(Object o) {  return null; } // duplicate\n" + 
				"		<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {}" +
				"class Y {}" +
				"class Z {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Y foo(Object o) {  return null; } // duplicate\n" + 
			"	  ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C1\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Z foo(Object o) {  return null; } // duplicate\n" + 
			"	  ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C1\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 11)\n" + 
			"	A<Y> foo(Object o) {  return null; } // duplicate\n" + 
			"	     ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C3\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	A<Z> foo(Object o) {  return null; } // duplicate\n" + 
			"	     ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C3\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 15)\n" + 
			"	Y foo(Object o) {  return null; } // duplicate\n" + 
			"	  ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C4\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 16)\n" + 
			"	<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
			"	                ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C4\n" + 
			"----------\n"
			// foo(java.lang.Object) is already defined in X.C1
			// foo(java.lang.Object) is already defined in X.C3
			// foo(java.lang.Object) is already defined in X.C4
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class C5 {\n" + 
				"		A<Y> foo(A<Y> o) {  return null; } // duplicate\n" + 
				"		A<Z> foo(A<Z> o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"	class C6 {\n" + 
				"		<T extends Y> T foo(A<Y> o) {  return null; } // ok\n" + 
				"		<T extends Z> T foo(A<Z> o) {  return null; } // ok\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {}" +
				"class Y {}" +
				"class Z {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	A<Y> foo(A<Y> o) {  return null; } // duplicate\r\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Duplicate method foo(A<Y>) in type X.C5\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	A<Z> foo(A<Z> o) {  return null; } // duplicate\r\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Duplicate method foo(A<Z>) in type X.C5\n" + 
			"----------\n"
			// name clash: foo(A<Y>) and foo(A<Z>) have the same erasure
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class C7 {\n" + 
				"		<T extends Y, U> T foo(Object o) {  return null; } // ok\n" + 
				"		<T extends Z> T foo(Object o) {  return null; } // ok\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {}" +
				"class Y {}" +
				"class Z {}"
			},
			""
		);
	}	

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<String> s) { return null; }\n" + 
				"		 <N> Object a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> void b(A<String> s) {}\n" + 
				"		 <N extends B> B b(A<Number> n) { return null; }\n" + 
				"		 void c(A<String> s) {}\n" + 
				"		 B c(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<String> s) { return null; }\n" + 
				"		 <N> B a(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<N extends B> N a(A<String> s) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<String>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<N> B a(A<Number> n) { return null; }\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n"
			// name clash: <N>a(A<java.lang.String>) and <N>a(A<java.lang.Number>) have the same erasure
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N b(A<String> s) { return null; }\n" + 
				"		 <N extends B> B b(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	<N extends B> N b(A<String> s) { return null; }\r\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<String>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	<N extends B> B b(A<Number> n) { return null; }\r\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n"
			// name clash: <N>b(A<java.lang.String>) and <N>b(A<java.lang.Number>) have the same erasure
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 B c(A<String> s) { return null; }\n" + 
				"		 B c(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	B c(A<String> s) { return null; }\r\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method c(A<String>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	B c(A<Number> n) { return null; }\r\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method c(A<Number>) in type X\n" + 
			"----------\n"
			// name clash: c(A<java.lang.String>) and c(A<java.lang.Number>) have the same erasure
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050c() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<Number> s) { return null; }\n" + 
				"		 <N> Object a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> N b(A<Number> s) { return null; }\n" + 
				"		 <N> Object b(A<String> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<Number> s) { return null; }\n" + 
				"		 <N> B a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> N b(A<Number> s) { return null; }\n" + 
				"		 <N> B b(A<String> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	<N extends B> N a(A<Number> s) { return null; }\r\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	<N> B a(A<Number> n) { return null; }\r\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\r\n" + 
			"	<N extends B> N b(A<Number> s) { return null; }\r\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\r\n" + 
			"	<N> B b(A<String> n) { return null; }\r\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<String>) in type X\n" + 
			"----------\n"
			// name clash: <N>a(A<java.lang.Number>) and <N>a(A<java.lang.Number>) have the same erasure
			// name clash: <N>b(A<java.lang.Number>) and <N>b(A<java.lang.String>) have the same erasure
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> void a(A<Number> s) {}\n" + 
				"		 <N extends B> B a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> Object b(A<Number> s) { return null; }\n" + 
				"		 <N extends B> B b(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<N extends B> void a(A<Number> s) {}\n" + 
			"	                   ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<N extends B> B a(A<Number> n) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	<N extends B> Object b(A<Number> s) { return null; }\n" + 
			"	                     ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	<N extends B> B b(A<Number> n) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n"
			// <N>a(A<java.lang.Number>) is already defined in X
			// <N>b(A<java.lang.Number>) is already defined in X
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 void a(A<Number> s) {}\n" + 
				"		 B a(A<Number> n) { return null; }\n" + 
				"		 Object b(A<Number> s) {}\n" + 
				"		 B b(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	void a(A<Number> s) {}\r\n" + 
			"	     ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	B a(A<Number> n) { return null; }\r\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\r\n" + 
			"	Object b(A<Number> s) {}\r\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\r\n" + 
			"	B b(A<Number> n) { return null; }\r\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n"
			// a(A<java.lang.Number>) is already defined in X
			// b(A<java.lang.Number>) is already defined in X
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements I {\n" + 
				"		 public <T extends I> void foo(T t) {}\n" + 
				"}\n" +
				"interface I {\n" + 
				"		 <T> void foo(T t);\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X implements I {\r\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I.foo(T)\n" + 
			"----------\n"
			// X is not abstract and does not override abstract method <T>foo(T) in I
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(A<String> a) {}\n" + 
				"	void foo(A<Integer> a) {}\n" +
				"}\n" + 
				"class A<T> {}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	void foo(A<String> a) {}\r\n" + 
			"	     ^^^^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(A<String>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	void foo(A<Integer> a) {}\r\n" + 
			"	     ^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(A<Integer>) in type X\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) and foo(A<java.lang.Integer>) have the same erasure
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(A<String> a) {}\n" + 
				"	Object foo(A<Integer> a) { return null; }\n" +
				"}\n" + 
				"class A<T> {}\n",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void _test052() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A<T> {\n" + 
				"	public A test() { return null; }\n" + 
				"	public A<T> test2() { return null; }\n" + 
				"	public A<X> test3() { return null; }\n" + 
				"	public <U> A<U> test4() { return null; }\n" + 
				"}\n" +
				"class B extends A<X> {\n" + 
				"	@Override public B test() { return null; }\n" + 
				"	@Override public B test2() { return null; }\n" + 
				"	@Override public B test3() { return null; }\n" + 
				"	@Override public <U> A<U> test4() { return null; }\n" + 
				"}\n" +
				"class X{}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A<T> {\n" + 
				"	public <U> A<U> test() { return null; }\n" + 
				"	public <U> A<U> test2() { return null; }\n" + 
				"	public <U> A<U> test3() { return null; }\n" + 
				"}\n" +
				"class B extends A<X> {\n" + 
				"	@Override public B test() { return null; }\n" + 
				"	@Override public A test2() { return null; }\n" + 
				"	@Override public A<X> test3() { return null; }\n" + 
				"}\n" +
				"class X{}\n"
			},
			"1. WARNING in A.java (at line 7)\r\n" + 
			"	@Override public B test() { return null; }\r\n" + 
			"	                 ^\n" + 
			"Type safety: The return type B for test() from the type B needs unchecked conversion to conform to A<U> from the type A<T>\n" + 
			"----------\n" + 
			"2. WARNING in A.java (at line 8)\n" + 
			"	@Override public A test2() { return null; }\n" + 
			"	                 ^\n" + 
			"Type safety: The return type A for test2() from the type B needs unchecked conversion to conform to A<U> from the type A<T>\n" + 
			"----------\n" + 
			"3. WARNING in A.java (at line 9)\r\n" + 
			"	@Override public A<X> test3() { return null; }\r\n" + 
			"	                 ^\n" + 
			"Type safety: The return type A<X> for test3() from the type B needs unchecked conversion to conform to A<U> from the type A<T>\n" + 
			"----------\n"
			// warning: test() in B overrides <U>test() in A; return type requires unchecked conversion
			// warning: test2() in B overrides <U>test2() in A; return type requires unchecked conversion
			// warning: test3() in B overrides <U>test3() in A; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void _test053() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"	void test(A a) { B b = a.foo(); }\n" + 
				"	void test2(A<X> a) { B b = a.foo(); }\n" + 
				"	void test3(B b) { B bb = b.foo(); }\n" + 
				"}\n" +
				"class A<T> {\n" + 
				"	<U> A<U> foo() { return null; }\n" + 
				"}\n" +
				"class B extends A<X> {\n" + 
				"	@Override B foo() { return null; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	void test(A a) { B b = a.foo(); }\r\n" + 
			"	                   ^\n" + 
			"Type mismatch: cannot convert from A to B\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\r\n" + 
			"	void test(A a) { B b = a.foo(); }\r\n" + 
			"	                       ^^^^^^^\n" + 
			"Type safety: The method foo() belongs to the raw type A. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\r\n" + 
			"	void test2(A<X> a) { B b = a.foo(); }\r\n" + 
			"	                       ^\n" + 
			"Type mismatch: cannot convert from A<Object> to B\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 10)\r\n" + 
			"	@Override B foo() { return null; }\r\n" + 
			"	                 ^\n" + 
			"Type safety: The return type B for foo() from the type B needs unchecked conversion to conform to A<U> from the type A<T>\n" + 
			"----------\n"
			// 2: incompatible types
			// 3: incompatible types; no instance(s) of type variable(s) U exist so that A<U> conforms to B
			// 10 warning: foo() in B overrides <U>foo() in A; return type requires unchecked conversion
		);
	}

	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void a(Object x) {}\n" +
				"	<T> T a(T x) {  return null; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void a(Object x) {}\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Duplicate method a(Object) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<T> T a(T x) {  return null; }\n" + 
			"	      ^^^^^^\n" + 
			"Method a(T) has the same erasure a(Object) as another method in type X\n" + 
			"----------\n"
			// a(X) is already defined in X
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T1, T2> String aaa(X x) {  return null; }\n" + 
				"	<T extends X> T aaa(T x) {  return null; }\n" + 
				"	<T> String aa(X x) {  return null; }\n" + 
				"	<T extends X> T aa(T x) {  return null; }\n" + 
				"	String a(X x) {  return null; }\n" + // dup
				"	<T extends X> T a(T x) {  return null; }\n" + 

				"	<T> String z(X x) { return null; }\n" + 
				"	<T, S> Object z(X x) { return null; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\r\n" + 
			"	String a(X x) {  return null; }\r\n" + 
			"	       ^^^^^^\n" + 
			"Duplicate method a(X) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\r\n" + 
			"	<T extends X> T a(T x) {  return null; }\r\n" + 
			"	                ^^^^^^\n" + 
			"Method a(T) has the same erasure a(X) as another method in type X\n" + 
			"----------\n"
			// a(X) is already defined in X
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"		 Object foo(X<T> t) { return null; }\n" + 
				"		 <S> String foo(X<T> s) { return null; }\n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"		<T1 extends X<T1>> void dupT() {}\n" + 
				"		<T2 extends X<T2>> Object dupT() {return null;}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	<T1 extends X<T1>> void dupT() {}\r\n" + 
			"	                        ^^^^^^\n" + 
			"Duplicate method dupT() in type X<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	<T2 extends X<T2>> Object dupT() {return null;}\r\n" + 
			"	                          ^^^^^^\n" + 
			"Duplicate method dupT() in type X<T>\n" + 
			"----------\n"
			// <T1>dupT() is already defined in X
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T> T a(A<T> t) {return null;}\n" + 
				"	<T> String a(A<Object> o) {return null;}\n" +
				"	<T> T aa(A<T> t) {return null;}\n" + 
				"	String aa(A<Object> o) {return null;}\n" +
				"}\n" + 
				"class A<T> {}\n",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95933
	public void test055() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		A a = new C();\n" + 
				"		try { a.f(new Object()); } catch (ClassCastException e) {\n" +
				"			System.out.println(1);\n" +
				"		}\n" +
				"	}\n" + 
				"}\n" +
				"interface A<T> { void f(T x); }\n" + 
				"interface B extends A<String> { void f(String x); }\n" + 
				"class C implements B { public void f(String x) {} }\n"
			},
			"1"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test056() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static String bind(String message, Object binding) { return null; }\n" + 
				"   public static String bind(String message, Object[] bindings) { return null; }\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"   public static String bind(String message, Object binding) { return null; }\n" + 
				"   public static String bind(String message, Object[] bindings) { return null; }\n" + 
				"}\n" + 
				"class Z {\n" + 
				"   void bar() { Y.bind(\"\", new String[] {\"\"}); }\n" + 
				"}\n"
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84035
	public void test057() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	A<Integer> x = new A<Integer>();\n" + 
				"   	B<Integer> y = new B<Integer>();\n" + 
				"   	new X().print(x);\n" + 
				"   	new X().print(y);\n" + 
				"	}\n" +
				"	public <T extends IA<?>> void print(T a) { System.out.print(1); }\n" +
				"	public <T extends IB<?>> void print(T a) { System.out.print(2); }\n" +
				"}\n" +
				"interface IA<E> {}\n" + 
				"interface IB<E> extends IA<E> {}\n" + 
				"class A<E> implements IA<E> {}\n" + 
				"class B<E> implements IB<E> {}\n"
			},
			"12");
		this.runConformTest(
			new String[] {
				"XX.java",
				"public class XX {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	A<Integer> x = new A<Integer>();\n" + 
				"   	B<Integer> y = new B<Integer>();\n" + 
				"   	print(x);\n" + 
				"   	print(y);\n" + 
				"	}\n" +
				"	public static <T extends IA<?>> void print(T a) { System.out.print(3); }\n" +
				"	public static <T extends IB<?>> void print(T a) { System.out.print(4); }\n" +
				"}\n" +
				"interface IA<E> {}\n" + 
				"interface IB<E> extends IA<E> {}\n" + 
				"class A<E> implements IA<E> {}\n" + 
				"class B<E> implements IB<E> {}\n"
			},
			"34");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <B extends Number> {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	X<Integer> x = new X<Integer>();\n" + 
				"   	x.aaa(null);\n" + 
				"   	x.aaa(15);\n" + 
				"	}\n" +
				"	<T> T aaa(T t) { System.out.print('T'); return null; }\n" +
				"	void aaa(B b) { System.out.print('B'); }\n" +
				"}\n"
			},
			"BB");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" + 
				"   void test() {\n" + 
				"   	new X<Object>().foo(\"X\");\n" + 
				"   	new X<Object>().foo2(\"X\");\n" + 
				"   }\n" + 
				"	<T> T foo(T t) {return null;}\n" +
				"	void foo(A a) {}\n" +
				"	<T> T foo2(T t) {return null;}\n" +
				"	<T> void foo2(A a) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new X<Object>().foo(\"X\");\r\n" + 
			"	                ^^^\n" + 
			"The method foo(String) is ambiguous for the type X<Object>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	new X<Object>().foo2(\"X\");\r\n" + 
			"	                ^^^^\n" + 
			"The method foo2(String) is ambiguous for the type X<Object>\n" + 
			"----------\n"
			// both references are ambiguous
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> extends Y<A> {\n" + 
				"   void test() {\n" + 
				"   	new X<Object>().foo(\"X\");\n" + 
				"   	new X<Object>().foo2(\"X\");\n" + 
				"   }\n" + 
				"	<T> T foo(T t) {return null;}\n" +
				"	<T> T foo2(T t) {return null;}\n" +
				"}\n" +
				"class Y<A> {\n" +
				"	void foo(A a) {}\n" +
				"	<T> void foo2(A a) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new X<Object>().foo(\"X\");\r\n" + 
			"	                ^^^\n" + 
			"The method foo(String) is ambiguous for the type X<Object>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	new X<Object>().foo2(\"X\");\r\n" + 
			"	                ^^^^\n" + 
			"The method foo2(String) is ambiguous for the type X<Object>\n" + 
			"----------\n"
			// both references are ambiguous
		);
	}

	public void test059() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {new B().foo(\"aa\");}\n" + 
				"}\n" +
				"class A { <U> void foo(U u) {System.out.print(false);} }\n" + 
				"class B extends A { <V> void foo(String s) {System.out.print(true);} }\n"
			},
			"true");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {new B().foo(\"aa\");}\n" + 
				"}\n" +
				"class A { <U> void foo(String s) {System.out.print(true);} }\n" + 
				"class B extends A { <V> void foo(V v) {System.out.print(false);} }\n"
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060() {
		this.runConformTest(
			new String[] {
				"I.java",
				"import java.util.Iterator;\n" +
				"public interface I {\n" +
				"	void method(Iterator<Object> iter);\n" +
				"	public static class TestClass implements I {\n" +
				"		public void method(Iterator iter) {}\n" +
				"	}\n" +
				"}"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"I2.java",
				"import java.util.Iterator;\n" +
				"public interface I2 {\n" +
				"	void method(Iterator<Object>[] iter);\n" +
				"	public static class TestClass implements I2 {\n" +
				"		public void method(Iterator[] iter) {}\n" +
				"	}\n" +
				"}"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"I3.java",
				"import java.util.Iterator;\n" +
				"public interface I3 {\n" +
				"	void method(Iterator<Object>[] iter);\n" +
				"	public static class TestClass implements I3 {\n" +
				"		public void method(Iterator[][] iter) {}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 4)\r\n" + 
			"	public static class TestClass implements I3 {\r\n" + 
			"	                    ^^^^^^^^^\n" + 
			"The type I3.TestClass must implement the inherited abstract method I3.method(Iterator<Object>[])\n" + 
			"----------\n"
			// does not override abstract method method(java.util.Iterator<java.lang.Object>[]) in I3
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99106
	public void test061() {
		this.runNegativeTest(
			new String[] {
				"Try.java",
				"public class Try {\n" +
				"	public static void main(String[] args) {\n" +
				"		Ex<String> ex = new Ex<String>();\n" +
				"		ex.one(\"eclipse\", new Integer(1));\n" +
				"		ex.two(new Integer(1));\n" +
				"		ex.three(\"eclipse\");\n" +
				"		ex.four(\"eclipse\");\n" +
				"		System.out.print(',');\n" +
				"		Ex ex2 = ex;\n" +
				"		ex2.one(\"eclipse\", new Integer(1));\n" + // unchecked warning
				"		ex2.two(new Integer(1));\n" + // unchecked warning
				"		ex2.three(\"eclipse\");\n" + // unchecked warning
				"		ex2.four(\"eclipse\");\n" + // unchecked warning
				"	}\n" +
				"}\n" +
				"class Top<TC> {\n" +
				"	<TM> void one(TC cTop, TM mTop) { System.out.print(-1); }\n" +
				"	<TM> void two(TM mTop) { System.out.print(-2); }\n" +
				"	void three(TC cTop) { System.out.print(-3); }\n" +
				"	<TM> void four(TC cTop) { System.out.print(-4); }\n" +
				"}\n" +
				"class Ex<C> extends Top<C> {\n" +
				"	@Override <M> void one(C cEx, M mEx) { System.out.print(1); }\n" +
				"	@Override <M> void two(M mEx) { System.out.print(2); }\n" +
				"	@Override void three(C cEx) { System.out.print(3); }\n" +
				"	@Override <M> void four(C cEx) { System.out.print(4); }\n" +
				"}"				
			},
			"----------\n" + 
			"1. WARNING in Try.java (at line 10)\n" + 
			"	ex2.one(\"eclipse\", new Integer(1));\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method one(Object, Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Try.java (at line 11)\n" + 
			"	ex2.two(new Integer(1));\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method two(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Try.java (at line 12)\n" + 
			"	ex2.three(\"eclipse\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method three(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in Try.java (at line 13)\n" + 
			"	ex2.four(\"eclipse\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method four(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99106
	public void test062() {
		this.runNegativeTest(
			new String[] {
				"Errors.java",
				"public class Errors {\n" +
				"	void foo() {\n" +
				"		Ex<String> ex = new Ex<String>();\n" +
				"		ex.proof(\"eclipse\");\n" +
				"		ex.five(\"eclipse\");\n" +
				"		ex.six(\"eclipse\");\n" +
				"		Ex ex2 = ex;\n" +
				"		ex2.proof(\"eclipse\");\n" +
				"		ex2.five(\"eclipse\");\n" +
				"		ex2.six(\"eclipse\");\n" +
				"	}\n" +
				"}\n" +
				"class Top<TC> {\n" +
				"	<TM> void proof(Object cTop) {}\n" +
				"	<TM> void five(TC cTop) {}\n" +
				"	void six(TC cTop) {}\n" +
				"}\n" +
				"class Ex<C> extends Top<C> {\n" +
				"	@Override void proof(Object cTop) {}\n" +
				"	@Override void five(C cEx) {}\n" +
				"	@Override <M> void six(C cEx) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in Errors.java (at line 6)\n" + 
			"	ex.six(\"eclipse\");\n" + 
			"	   ^^^\n" + 
			"The method six(String) is ambiguous for the type Ex<String>\n" + 
			"----------\n" + 
			"2. WARNING in Errors.java (at line 9)\n" + 
			"	ex2.five(\"eclipse\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method five(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in Errors.java (at line 10)\n" + 
			"	ex2.six(\"eclipse\");\n" + 
			"	    ^^^\n" + 
			"The method six(Object) is ambiguous for the type Ex\n" + 
			"----------\n" + 
			"4. ERROR in Errors.java (at line 21)\n" + 
			"	@Override <M> void six(C cEx) {}\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"The method six(C) of type Ex<C> must override a superclass method\n" + 
			"----------\n" + 
			"5. ERROR in Errors.java (at line 21)\n" + 
			"	@Override <M> void six(C cEx) {}\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"Name clash: The method six(C) of type Ex<C> has the same erasure as six(TC) of type Top<TC> but does not override it\n" + 
			"----------\n"
			// we disagree about the ambiguous errors on lines 5, 9 & 20, see the message sends to proof()
			// 5: reference to five is ambiguous, both method <TM>five(TC) in Top<java.lang.String> and method five(C) in Ex<java.lang.String> match
			// 6: reference to six is ambiguous, both method six(TC) in Top<java.lang.String> and method <M>six(C) in Ex<java.lang.String> match
			// 9: reference to five is ambiguous, both method <TM>five(TC) in Top and method five(C) in Ex match
			// 9: warning: [unchecked] unchecked call to <TM>five(TC) as a member of the raw type Top
			// 10: reference to six is ambiguous, both method six(TC) in Top and method <M>six(C) in Ex match
			// 10: warning: [unchecked] unchecked call to six(TC) as a member of the raw type Top
			// 20: method does not override a method from its superclass
			// 21: method does not override a method from its superclass
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551
	public void test063() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface IStructuredContentProvider<I, E extends I> {\n" + 
				"    public E[] getElements(I inputElement);\n" + 
				"    public E[] getChildren(E parent);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements IStructuredContentProvider {\n" + 
				"// eclipse error: The type X must implement the inherited\n" + 
				"// abstract method IStructuredContentProvider.getChildren(I)\n" + 
				"\n" + 
				"    public Object[] getElements(Object inputElement) {\n" + 
				"        // eclipse error: The return type is incompatible with\n" + 
				"        // IStructuredContentProvider.getElements(Object)\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"\n" + 
				"    public Object[] getChildren(Object parent) {\n" + 
				"        // eclipse error: Name clash: The method getChildren(Object) of type\n" + 
				"        // X has the same erasure as getChildren(E) of type\n" + 
				"        // IStructuredContentProvider<I,E> but does not override it\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551 - variation
	public void test064() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface IStructuredContentProvider<I, E extends I> {\n" + 
				"    public E[] getElements(I inputElement);\n" + 
				"    public E[] getChildren(E parent);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements IStructuredContentProvider<Object,Object> {\n" + 
				"// eclipse error: The type X must implement the inherited\n" + 
				"// abstract method IStructuredContentProvider.getChildren(I)\n" + 
				"\n" + 
				"    public Object[] getElements(Object inputElement) {\n" + 
				"        // eclipse error: The return type is incompatible with\n" + 
				"        // IStructuredContentProvider.getElements(Object)\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"\n" + 
				"    public Object[] getChildren(Object parent) {\n" + 
				"        // eclipse error: Name clash: The method getChildren(Object) of type\n" + 
				"        // X has the same erasure as getChildren(E) of type\n" + 
				"        // IStructuredContentProvider<I,E> but does not override it\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551 - variation
	public void test065() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"interface IStructuredContentProvider<I, E extends List<String>> {\n" + 
				"    public E[] getElements(I inputElement);\n" + 
				"    public E[] getChildren(E parent);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements IStructuredContentProvider {\n" + 
				"// eclipse error: The type X must implement the inherited\n" + 
				"// abstract method IStructuredContentProvider.getChildren(I)\n" + 
				"\n" + 
				"    public List[] getElements(Object inputElement) {\n" + 
				"        // eclipse error: The return type is incompatible with\n" + 
				"        // IStructuredContentProvider.getElements(Object)\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"\n" + 
				"    public List[] getChildren(List parent) {\n" + 
				"        // eclipse error: Name clash: The method getChildren(Object) of type\n" + 
				"        // X has the same erasure as getChildren(E) of type\n" + 
				"        // IStructuredContentProvider<I,E> but does not override it\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=103849
	public void test066() {
		this.runConformTest(
			new String[] {
				"JukeboxImpl.java",
				"public class JukeboxImpl implements Jukebox {\n" + 
				"    public <M extends Music,A extends Artist<M>> A getArtist (M music){return null;}\n" + 
				"    void test () { getArtist(new Rock()); }\n" + 
				"}\n" + 
				"interface Jukebox {\n" + 
				"	<M extends Music, A extends Artist<M>> A getArtist (M music);\n" + 
				"}\n" + 
				"interface Music {}\n" + 
				"class Rock implements Music {}\n" + 
				"interface Artist<M extends Music> {}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107098
	public void test067() {
		this.runConformTest(
			new String[] {
				"NoErrors.java",
				"public class NoErrors {\n" + 
				"    public static void main(String[] args) { new B().foo2(1, 10); }\n" + 
				"}\n" + 
				"class A<T> {\n" + 
				"	<S1 extends T> void foo2(Number t, S1 s) { System.out.print(false); }\n" + 
				"}\n" + 
				"class B extends A<Number> {\n" + 
				"	<S2 extends Number> void foo2(Number t, S2 s) { System.out.print(true); }\n" + 
				"}\n"
			},
			"true");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107681
	public void test068() {
		this.runConformTest(
			new String[] {
				"ReflectionNavigator.java",
				"import java.lang.reflect.Type;\n" +
				"public class ReflectionNavigator implements Navigator<Type> {\n" + 
				"    public <T> Class<T> erasure(Type t) { return null; }\n" + 
				"}\n" + 
				"interface Navigator<TypeT> {\n" + 
				"	<T> TypeT erasure(TypeT x);\n" + 
				"}\n" + 
				"class Usage {\n" + 
				"	public void foo(ReflectionNavigator r, Type t) { r.erasure(t); }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108203
	public void test069() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Type;\n" +
				"public class X implements I<A> {\n" + 
				"    public <N extends A> void x1() {}\n" + 
				"    public <N extends Number> void x2() {}\n" + 
				"    public <N extends Number> void x3() {}\n" + 
				"}\n" + 
				"interface I<V> {\n" + 
				"	<N extends V> void x1();\n" + 
				"	<N extends String> void x2();\n" + 
				"	<N extends Object> void x3();\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B<T> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	public class X implements I<A> {\r\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I<A>.x3()\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\r\n" + 
			"	public class X implements I<A> {\r\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I<A>.x2()\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\r\n" + 
			"	public <N extends Number> void x2() {}\r\n" + 
			"	                               ^^^^\n" + 
			"Name clash: The method x2() of type X has the same erasure as x2() of type I<V> but does not override it\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\r\n" + 
			"	public <N extends Number> void x3() {}\r\n" + 
			"	                               ^^^^\n" + 
			"Name clash: The method x3() of type X has the same erasure as x3() of type I<V> but does not override it\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 9)\r\n" + 
			"	<N extends String> void x2();\r\n" + 
			"	           ^^^^^^\n" + 
			"The type parameter N should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101049
	public void test070() {
		this.runNegativeTest(
			new String[] {
				"BooleanFactory.java",
				"interface Factory<T> {\n" +
				"	<U extends T> U create(Class<U> c);\n" + 
				"}\n" + 
				"public class BooleanFactory implements Factory<Boolean> {\n" + 
				"	public <U extends Boolean> U create(Class<U> c) {\n" + 
				"		try { return c.newInstance(); } catch(Exception e) { return null; }\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in BooleanFactory.java (at line 5)\n" + 
			"	public <U extends Boolean> U create(Class<U> c) {\n" + 
			"	                  ^^^^^^^\n" + 
			"The type parameter U should not be bounded by the final type Boolean. Final types cannot be further extended\n" + 
			"----------\n"
		);
	}
}