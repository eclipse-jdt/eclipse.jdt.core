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
				"class B extends A { @Override <T extends Comparable<T>> void m(T t) { System.out.println(t); } }\n" +
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
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() { new BB().test(); } }\n" + 
				"class AA<T> { void test() {} }\n" + 
				"class BB extends AA<CC> { <U> void test() {} }\n" + 
				"class CC {}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X { void foo() { new BB().test(); } }\n" + 
			"	                                       ^^^^\n" + 
			"The method test() is ambiguous for the type BB\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	class BB extends AA<CC> { <U> void test() {} }\n" + 
			"	                                   ^^^^^^\n" + 
			"Name clash: The method test() of type BB has the same erasure as test() of type AA<T> but does not override it\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void singleMatch() { System.out.print(new BB().test(new N(), new Integer(1))); }\n" +
				"	void betterMatch() { System.out.print(new CC().test(new N(), new Integer(1))); }\n" +
				"	void worseMatch() { System.out.print(new DD().test(new N(), new Integer(1))); }\n" +
				"	public static void main(String[] s) {\n" +
				"		new X().singleMatch();\n" +
				"		new X().betterMatch();\n" +
				"		new X().worseMatch();\n" +
				"	}\n" +
				"}\n" + 
				"class AA<T> { int test(T t, Number num) { return 1; } }\n" + 
				"class BB extends AA<N> { @Override int test(N n, Number num) { return 2; } }\n" + 
				"class CC extends AA<M> { <U extends Number> int test(N n, U u) { return 3; } }\n" + 
				"class DD extends AA<N> { <U extends Number> int test(M m, U u) { return 4; } }\n" + 
				"class M {}\n" +
				"class N extends M {}",
			},
			"231"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void ambiguous() { new BB().test(new N()); }\n" +
				"	void exactMatch() { new CC().test(new N()); }\n" +
				"}\n" + 
				"class AA<T> { void test(T t) {} }\n" + 
				"class BB extends AA<N> { <U> void test(N n) {} }\n" + 
				"class CC extends AA<N> { @Override void test(N n) {} }\n" + 
				"class M {}\n" +
				"class N extends M {}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	void ambiguous() { new BB().test(new N()); }\r\n" + 
			"	                            ^^^^\n" + 
			"The method test(N) is ambiguous for the type BB\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void ambiguous() { new BB().test(new N(), new Integer(1)); }\n" +
				"}\n" + 
				"class AA<T> { void test(T t, Integer i) {} }\n" + 
				"class BB extends AA<M> { <U extends Number> void test(N n, U u) {} }\n" + 
				"class M {}\n" +
				"class N extends M {}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void ambiguous() { new BB().test(new N(), new Integer(1)); }\n" + 
			"	                            ^^^^\n" + 
			"The method test(N, Integer) is ambiguous for the type BB\n" + 
			"----------\n"
		);
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"	}\n" +
				"}\n" +
				"class C<T extends Number> { public void id(T t) {} }\n" +
				"class M<TT> extends C<Integer> { public <ZZ> void id(Integer i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	m.id(new Integer(111));\n" + 
			"	  ^^\n" + 
			"The method id(Integer) is ambiguous for the type M<Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in M<java.lang.Integer,java.lang.Integer> match
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"	}\n" +
				"}\n" +
				"class C<T extends Number> { public void id(T t) {} }\n" +
				"class M<TT> extends C<Integer> { public void id(Integer i) {} }\n"
			},
			""
		);
	}
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <S extends A> void foo() { }\n" + 
				"		 <N extends B> N foo() { return null; }\n" + 
				"		 void test () {\n" + 
				"		 	new X().foo();\n" + 
				"		 }\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	new X().foo();\n" + 
			"	        ^^^\n" + 
			"The method foo() is ambiguous for the type X\n" + 
			"----------\n"
		);
	}
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"   void test() {\n" + 
				"   	new Y<Object>().foo(\"X\");\n" + 
				"   	new Y<Object>().foo2(\"X\");\n" + 
				"   }\n" + 
				"	<U1> U1 foo(U1 t) {return null;}\n" +
				"	<U2> U2 foo2(U2 t) {return null;}\n" +
				"}\n" +
				"class Y<T2> extends X<T2> {\n" + 
				"	void foo(T2 t) {}\n" +
				"	<U3> void foo2(T2 t) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	new Y<Object>().foo(\"X\");\n" + 
			"	                ^^^\n" + 
			"The method foo(Object) is ambiguous for the type Y<Object>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	new Y<Object>().foo2(\"X\");\n" + 
			"	                ^^^^\n" + 
			"The method foo2(Object) is ambiguous for the type Y<Object>\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129056
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"public class B {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	new M().foo(new Integer(1), 2);\n" + 
				"   	new N().foo(new Integer(1), 2);\n" + 
				"   }\n" +
				"}" + 
				"interface I { void foo(Number arg1, Number arg2); }\n" +
				"class M {\n" +
				"	public void foo(int arg1, int arg2) {}\n" +
				"	public void foo(Number arg1, Number arg2) {}\n" +
				"}\n" +
				"class N implements I {\n" +
				"	public void foo(int arg1, int arg2) {}\n" +
				"	public void foo(Number arg1, Number arg2) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\r\n" + 
			"	new M().foo(new Integer(1), 2);\r\n" + 
			"	        ^^^\n" + 
			"The method foo(int, int) is ambiguous for the type M\n" + 
			"----------\n" +
			"2. ERROR in B.java (at line 4)\r\n" + 
			"	new N().foo(new Integer(1), 2);\r\n" + 
			"	        ^^^\n" + 
			"The method foo(int, int) is ambiguous for the type N\n" + 
			"----------\n"
			// reference to foo is ambiguous, both method foo(int,int) in M and method foo(java.lang.Number,java.lang.Number) in M match
			// reference to foo is ambiguous, both method foo(int,int) in N and method foo(java.lang.Number,java.lang.Number) in N match
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 1
	public void test008() {
		this.runConformTest(
			new String[] {
				"AA.java",
				"public class AA {\n" + 
				"   public static void main(String[] a) { System.out.print(new C().test(new T())); }\n" + 
				"}" + 
				"class S {}\n" +
				"class T extends S {}\n" +
				"class B { <U extends S> int test(U u) {return -1;} }\n" +
				"class C extends B { @Override int test(S s) {return 1;} }"
			},
			"1"
		);
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A { void check() { new C().test(new T()); } }\n" + 
				"class S {}\n" +
				"class T extends S {}\n" +
				"class B { int test(S s) {return 1;} }\n" +
				"class C extends B { <U extends S> int test(U u) {return -1;} }"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 1)\n" + 
			"	public class A { void check() { new C().test(new T()); } }\n" + 
			"	                                        ^^^^\n" + 
			"The method test(T) is ambiguous for the type C\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 5)\n" + 
			"	class C extends B { <U extends S> int test(U u) {return -1;} }\n" + 
			"	                                      ^^^^^^^^^\n" + 
			"Name clash: The method test(U) of type C has the same erasure as test(S) of type B but does not override it\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 2
	public void test009() {
		this.runConformTest(
			new String[] {
				"T.java",
				"import java.util.*;\n" + 
				"public class T {\n" + 
				"   void test() {\n" + 
				"   	OrderedSet<String> os = null;\n" + 
				"   	os.add(\"hello\");\n" + 
				"   	OrderedSet<Integer> os2 = null;\n" + 
				"   	os2.add(1);\n" + 
				"   }\n" +
				"}" + 
				"interface OrderedSet<E> extends List<E>, Set<E> { boolean add(E o); }\n"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
	public void _test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"  	interface Listener {}\n" + 
				"  	interface ErrorListener {}\n" + 
				"  	static <L1 extends Listener & ErrorListener> Object createParser(L1 l) { return null; }\n" + 
				"  	static <L2 extends ErrorListener & Listener> Object createParser(L2 l) { return null; }\n" + 
				"   public static void main(String[] s) {\n" + 
				"   	class A implements Listener, ErrorListener {}\n" + 
				"   	createParser(new A());\n" + 
				"   }\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\r\n" + 
			"	static <L1 extends Listener & ErrorListener> Object createParser(L1 l) { return null; }\r\n" + 
			"	                                                    ^^^^^^^^^^^^^^^^^^\n" + 
			"Method createParser(L1) has the same erasure createParser(X.Listener) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\r\n" + 
			"	static <L2 extends ErrorListener & Listener> Object createParser(L2 l) { return null; }\r\n" + 
			"	                                                    ^^^^^^^^^^^^^^^^^^\n" + 
			"Method createParser(L2) has the same erasure createParser(X.ErrorListener) as another method in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\r\n" + 
			"	createParser(new A());\r\n" + 
			"	^^^^^^^^^^^^\n" + 
			"The method createParser(A) is undefined for the type X\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"  	interface Listener {}\n" + 
				"  	interface ErrorListener {}\n" + 
				"  	static <L1 extends Listener> int createParser(L1 l) { return 1; }\n" + 
				"  	static <L2 extends ErrorListener & Listener> int createParser(L2 l) { return 2; }\n" + 
				"   public static void main(String[] s) {\n" + 
				"   	class A implements Listener, ErrorListener {}\n" + 
				"   	System.out.print(createParser(new A()));\n" + 
				"   }\n" +
				"}"
			},
			"2"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"  	interface Listener {}\n" + 
				"  	interface ErrorListener {}\n" + 
				"  	static int createParser(Listener l) { return 1; }\n" + 
				"  	static <L extends ErrorListener & Listener> int createParser(L l) { return 2; }\n" + 
				"   public static void main(String[] s) {\n" + 
				"   	class A implements Listener, ErrorListener {}\n" + 
				"   	System.out.print(createParser(new A()));\n" + 
				"   }\n" +
				"}"
			},
			"2"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<A, B> extends Y<A> {\n" + 
				"  	<T extends Number> void foo(Number n, T t) throws ExOne {}\n" + 
				"   void test(X<Integer,Integer> c) throws ExTwo { c.foo(new Integer(1), new Integer(2)); }\n" +
				"}\n" +
				"class Y<C> {\n" + 
				"   void foo(Number x, C n) throws ExTwo {}\n" +
				"}\n" + 
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			""
		);
//		this.runConformTest(
//			new String[] {
//				"Combined.java",
//				"public class Combined<A, B> {\n" + 
//				"  	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" + 
//				"  	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" + 
//				"  	void pickOne(Combined<Integer,Integer> c) throws ExOne { c.pickOne(\"test\"); }\n" + 
//				"  	<T extends Number> void pickTwo(Number n, T t) throws ExOne {}\n" + 
//				"   void pickTwo(A x, Number n) throws ExTwo {}\n" + 
//				"   void pickTwo(Combined<Integer,Integer> c) throws ExTwo { c.pickTwo(new Integer(1), 2); }\n" +
//				"}\n" +
//				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
//				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
//			},
//			""
//		);
	}
	public void test012() {
		this.runConformTest(
			new String[] {
				"XX.java",
				"public class XX {\n" +
				"	public static void main(String[] s) { System.out.println(new B().id(new Integer(1))); }\n" + 
				"}\n" + 
				"class A<T extends Number> { public int id(T t) {return 2;} }\n" + 
				"class B extends A<Integer> { public int id(Integer i) {return 1;} }"
			},
			"1"
		);
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public class XX {\n" +
				"	public static void main(String[] s) { System.out.println(new B().id(new Integer(1))); }\n" + 
				"}\n" + 
				"class A<T extends Number> { public int id(T t) {return 2;} }\n" + 
				"class B extends A<Integer> { public <ZZ> int id(Integer i) {return 1;} }"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 2)\r\n" + 
			"	public static void main(String[] s) { System.out.println(new B().id(new Integer(1))); }\r\n" + 
			"	                                                                 ^^\n" + 
			"The method id(Integer) is ambiguous for the type B\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(T) in A<java.lang.Integer> and method <ZZ>id(java.lang.Integer) in B match
		);
	}
	public void test013() {
			this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<E extends A> void m(E e) { System.out.print(1); }\n" + 
				"	<E extends B> void m(E e) { System.out.print(2); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().m(new A());\n" +
				"		new X().m(new B());\n" + 
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n"
			},
			"12"
		);
	}
	public void test014() {
		this.runConformTest(
			new String[] {
				"X0.java",
				"public class X0 {\n" + 
				"	void two(G x) { System.out.print(1); }\n" + 
				"	void two(F<A> x) { System.out.print(2); }\n" + 
				"	void three(G x) { System.out.print(3); }\n" + 
				"	void three(F<B> x) { System.out.print(4); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		new X0().two(h);\n" +
				"		new X0().three(h);\n" +
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"13"
		);
//		this.runConformTest(
//			new String[] {
//				"X.java",
//				"public class X {\n" + 
//				"	void four(G x) { System.out.print(5); }\n" + 
//				"	void four(F<C> x) { System.out.print(6); }\n" + 
//				"	public static void main(String[] args) {\n" + 
//				"		H<C> h = null;\n" +
//				"		new X().four(h);\n" +
//				"	}\n" + 
//				"}\n" +
//				"class A {}\n" + 
//				"class B extends A {}\n" +
//				"class C extends B {}\n" +
//				"class F<T1> {} \n" + 
//				"class G<T2> extends F<T2> {}\n" +
//				"class H<T3> extends G<T3> {}"
//			},
//			"ambiguous?"
//			// reference to four is ambiguous, both method four(G) in X and method four(F<C>) in X match
//		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void one(G<B> x) {}\n" + 
				"	void one(F<B> x) {}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		new X().one(h);\n" + // no match
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\r\n" + 
			"	new X().one(h);\r\n" + 
			"	        ^^^\n" + 
			"The method one(G<B>) in the type X is not applicable for the arguments (H<C>)\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X1.java",
				"public class X1 {\n" + 
				"	void two(G<C> x) { System.out.print(1); }\n" + 
				"	void two(F<B> x) { System.out.print(2); }\n" + 
				"	void three(G<B> x) { System.out.print(3); }\n" + 
				"	void three(F<C> x) { System.out.print(4); }\n" + 
				"	void four(G<C> x) { System.out.print(5); }\n" + 
				"	void four(F<C> x) { System.out.print(6); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		new X1().two(h);\n" +
				"		new X1().three(h);\n" +
				"		new X1().four(h);\n" +
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"145"
		);
//		this.runNegativeTest(
//			new String[] {
//				"X.java",
//				"public class X {\n" + 
//				"	<E1, E2 extends B> void three(G<E2> x) {}\n" + 
//				"	<E3 extends C> void three(F<E3> x) {}\n" + 
//				"	public static void main(String[] args) {\n" + 
//				"		H<C> h = null;\n" +
//				"		new X().three(h);\n" +
//				"	}\n" + 
//				"}\n" +
//				"class A {}\n" + 
//				"class B extends A {}\n" +
//				"class C extends B {}\n" +
//				"class F<T1> {} \n" + 
//				"class G<T2> extends F<T2> {}\n" +
//				"class H<T3> extends G<T3> {}"
//			},
//			"----------\n" + 
//			"1. ERROR in X.java (at line 6)\r\n" + 
//			"	new X().three(h);\r\n" + 
//			"	        ^^^^^\n" + 
//			"The method three(G<C>) is ambiguous for the type X\n" + 
//			"----------\n"
//		);
		this.runConformTest(
			new String[] {
				"X3.java",
				"public class X3 {\n" + 
				"	<E1, E2 extends B> void one(G<E2> x) { System.out.print(1); }\n" + 
				"	<E3 extends B> void one(F<E3> x) { System.out.print(2); }\n" + 
				"	<E1, E2 extends C> void two(G<E2> x) { System.out.print(3); }\n" + 
				"	<E3 extends B> void two(F<E3> x) { System.out.print(4); }\n" + 
				"	<E1, E2 extends C> void four(G<E2> x) { System.out.print(5); }\n" + 
				"	<E3 extends C> void four(F<E3> x) { System.out.print(6); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		new X3().one(h);\n" +
				"		new X3().two(h);\n" +
				"		new X3().four(h);\n" +
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"135"
		);
	}
}