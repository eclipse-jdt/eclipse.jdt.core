/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.*;

public class MethodVerifyTest extends AbstractComparisonTest {

	public MethodVerifyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return setupSuite(testClass());
	}
	
	public static Class testClass() {
		return MethodVerifyTest.class;
	}

	public void test001() { // all together
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"class A {}\n" +
				"class B {}\n" +
				"class X<U> { public void foo(U u) {} }\n" +
				"interface I<U> { public void foo(U u); }\n" +

				"class J<T> implements I<B> { public void foo(T t) {} }\n" +
				"class K<T> implements I<T> { public void foo(T t) {} }\n" +
				"class L<T> implements I { public void foo(T t) {} }\n" +

				"class Y<T> extends X<A> { public void foo(T t) { super.foo(t); } }\n" +
				"class Z<T> extends X<T> { public void foo(T t) { super.foo(t); } }\n" +
				"class W<T> extends X { public void foo(T t) { super.foo(t); } }\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 5)\n" + 
			"	class J<T> implements I<B> { public void foo(T t) {} }\n" + 
			"	      ^\n" + 
			"Class must implement the inherited abstract method I<B>.foo(B)\n" + 
			"----------\n" + 
			"2. ERROR in ALL.java (at line 7)\n" + 
			"	class L<T> implements I { public void foo(T t) {} }\n" + 
			"	      ^\n" + 
			"Class must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"3. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { public void foo(T t) { super.foo(t); } }\n" + 
			"	                                                       ^^^\n" + 
			"The method foo(A) in the type X<A> is not applicable for the arguments (T)\n" + 
			"----------\n" + 
			"4. WARNING in ALL.java (at line 10)\n" + 
			"	class W<T> extends X { public void foo(T t) { super.foo(t); } }\n" + 
			"	                                              ^^^^^^^^^^^^\n" + 
			"Type safety: The method foo(Object) belongs to the raw type X. References to generic type X<U> should be parameterized\n" + 
			"----------\n"
			/*
			ALL.java:5: J is not abstract and does not override abstract method foo(B) in I
			ALL.java:7: L is not abstract and does not override abstract method foo(java.lang.Object) in I
			ALL.java:8: foo(A) in X<A> cannot be applied to (T)
			ALL.java:10: warning: [unchecked] unchecked call to foo(U) as a member of the raw type X
			 */
		);
	}

	public void test002() { // separate files
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",

				"J.java",
				"class J<T> implements I<B> { public void foo(T t) {} }\n",
				"K.java",
				"class K<T> implements I<T> { public void foo(T t) {} }\n",
				"L.java",
				"class L<T> implements I { public void foo(T t) {} }\n",

				"Y.java",
				"class Y<T> extends X<A> { public void foo(T t) { super.foo(t); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { public void foo(T t) { super.foo(t); } }\n",
				"W.java",
				"class W<T> extends X { public void foo(T t) { super.foo(t); } }\n",
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public void foo(T t) {} }\n" + 
			"	      ^\n" + 
			"Class must implement the inherited abstract method I<B>.foo(B)\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in L.java (at line 1)\n" + 
			"	class L<T> implements I { public void foo(T t) {} }\n" + 
			"	      ^\n" + 
			"Class must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public void foo(T t) { super.foo(t); } }\n" + 
			"	                                                       ^^^\n" + 
			"The method foo(A) in the type X<A> is not applicable for the arguments (T)\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in W.java (at line 1)\n" + 
			"	class W<T> extends X { public void foo(T t) { super.foo(t); } }\n" + 
			"	                                              ^^^^^^^^^^^^\n" + 
			"Type safety: The method foo(Object) belongs to the raw type X. References to generic type X<U> should be parameterized\n" + 
			"----------\n"
			/*
			J.java:1: J is not abstract and does not override abstract method foo(B) in I
			L.java:1: L is not abstract and does not override abstract method foo(java.lang.Object) in I
			W.java:1: warning: [unchecked] unchecked call to foo(U) as a member of the raw type X
			Y.java:1: foo(A) in X<A> cannot be applied to (T)
			 */
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
				"J.java",
				"class J<T> implements I<B> { public void foo(T t) {} }\n",
				"K.java",
				"class K<T> implements I<T> { public void foo(T t) {} }\n",
				"L.java",
				"class L<T> implements I { public void foo(T t) {} }\n",

				"Y.java",
				"class Y<T> extends X<A> { public void foo(T t) { super.foo(t); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { public void foo(T t) { super.foo(t); } }\n",
				"W.java",
				"class W<T> extends X { public void foo(T t) { super.foo(t); } }\n",
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public void foo(T t) {} }\n" + 
			"	      ^\n" + 
			"Class must implement the inherited abstract method I<B>.foo(B)\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in L.java (at line 1)\n" + 
			"	class L<T> implements I { public void foo(T t) {} }\n" + 
			"	      ^\n" + 
			"Class must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { public void foo(T t) { super.foo(t); } }\n" + 
			"	                                                       ^^^\n" + 
			"The method foo(A) in the type X<A> is not applicable for the arguments (T)\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in W.java (at line 1)\n" + 
			"	class W<T> extends X { public void foo(T t) { super.foo(t); } }\n" + 
			"	                                              ^^^^^^^^^^^^\n" + 
			"Type safety: The method foo(Object) belongs to the raw type X. References to generic type X<U> should be parameterized\n" + 
			"----------\n",
			/*
			J.java:1: J is not abstract and does not override abstract method foo(B) in I
			L.java:1: L is not abstract and does not override abstract method foo(java.lang.Object) in I
			W.java:1: warning: [unchecked] unchecked call to foo(U) as a member of the raw type X
			Y.java:1: foo(A) in X<A> cannot be applied to (T)
			 */
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
				"class Z<T> extends X<T> { public T foo() { return super.foo(); } }\n" +
				"class W<T> extends X { public T foo() { return super.foo(); } }\n",
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
			"	class W<T> extends X { public T foo() { return super.foo(); } }\n" + 
			"	                                               ^^^^^^^^^^^\n" + 
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
				"class Z<T> extends X<T> { public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { public T foo() { return super.foo(); } }\n",
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
			"	class W<T> extends X { public T foo() { return super.foo(); } }\n" + 
			"	                                               ^^^^^^^^^^^\n" + 
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
				"class Z<T> extends X<T> { public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { public T foo() { return super.foo(); } }\n",
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
			"	class W<T> extends X { public T foo() { return super.foo(); } }\n" + 
			"	                                               ^^^^^^^^^^^\n" + 
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
	}

	public void test008() { // covariance test
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"interface I { I foo(); }\n" +
				"class A implements I { public A foo() { return null; } }\n" +
				"class B extends A { public B foo() { return null; } }\n" +
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
			"Name clash : The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
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
			"Name clash : The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
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
			"Name clash : The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
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
			"Name clash : The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in A and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T s) {} }\n" +
				"class Y1 extends A { void foo(Object s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class Y1 extends A { void foo(Object s) {} }\r\n" + 
			"	                          ^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object) in Y1 cannot override <T>foo(T) in A; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T[] s) {} }\n" +
				"class Y2 extends A { void foo(Object[] s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class Y2 extends A { void foo(Object[] s) {} }\n" + 
			"	                          ^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object[]) in Y2 cannot override <T>foo(T[]) in A; attempting to assign weaker access privileges; was public
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public void foo(Class<Object> s) {} }\n" +
				"class Y3 extends A { void foo(Class<Object> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class Y3 extends A { void foo(Class<Object> s) {} }\r\n" + 
			"	                          ^^^^^^^^^^^^^^^^^^^^\n" + 
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
			"Name clash : The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
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
			"Name clash : The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
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
			"Name clash : The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
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
			"Name clash : The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
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
			"Name clash : The method foo(Class<T>) of type X5 has the same erasure as foo(Class<T>) of type A but does not override it\n" + 
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
			"Name clash : The method foo(A<String>) of type Y has the same erasure as foo(A) of type X but does not override it\n" + 
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
			"Name clash : The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type X but does not override it\n" + 
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
			"Name clash : The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
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
			"Name clash : The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in I have the same erasure, yet neither overrides the other
		);
	}

	public void _test016() { // 73971 and 77228
		this.runConformTest(
			new String[] {
				"X.java",
				"class X {\n" + 
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
			"A=AB=B"
		);
		this.runConformTest(	// cannot have 2 methods with compatible return types like Object & String so how is this legal?
			new String[] {
				"X.java",
				"class X implements I, J {\n" + 
				"	public <T extends I> T getValue(String value) { return null; }\n" + 
				"	public <T extends J> T getValue(String value) { return null; }\n" + 
				"}\n" +
				"interface I { <T extends I> T getValue(String value); }\n" + 
				"interface J { <T extends J> T getValue(String value); }\n"
			},
			""
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
			"Name clash : The method test(X<Number>) of type Z has the same erasure as test(X<? extends Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<java.lang.Number>) in Z and test(X<? extends java.lang.Number>) in Y have the same erasure, yet neither overrides the other
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
			"Name clash : The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
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
				"	void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
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
				"	void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n",
			null,
			false,
			null
		);
	}
}