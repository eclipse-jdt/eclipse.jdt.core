/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

	public void test000() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test { public static void main(String[] args) { new B().foo(new C()); } }\n" +
				"class A { void foo(A a) {} }\n" +
				"class B extends A { void foo(B b) { System.out.println(1); } }\n" +
				"class C extends B {}"
			},
			"1"
		);
	}
	public void test000a() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test { public static void main(String[] args) { new Subtype<String>().foo(1, \"works\"); } }\n" +
					"class Supertype<T1> { <U1> void foo(U1 u, T1 t) {} }\n" +
					"class Subtype <T2> extends Supertype<T2> { <U3> void foo(U3 u, T2 t) { System.out.println(t); } }"
				},
				"works"
			);		
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
				"	static interface I1<E1> { void method(E1 o); }\n" +
				"	static interface I2<E2> { void method(E2 o); }\n" +
				"	static interface I3<E3, E4> extends I1<E3>, I2<E4> {}\n" +
				"	static class Class1 implements I3<String, String> {\n" +
				"		public void method(String o) { System.out.println(o); }\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		I3<String, String> i = new Class1();\n" +
				"		i.method(\"works\");\n" +
				"	}\n" +
				"}"
			},
			"works"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test002a() {
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
	}
	public void test003a() {
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
	}
	public void test003b() {
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
	}
	public void test003c() {
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
	}
	public void test004a() {
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
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 1
	public void test008a() {
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void _test010a() {
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
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void _test010b() {
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
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void _test010c() {
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A extends Number> extends Y<A> {\n" + 
				"	<T> void foo(A n, T t) throws ExOne {}\n" + 
				"	void test(X<Integer> x) throws ExTwo { x.foo(new Integer(1), new Integer(2)); }\n" +
				"	void test2(X x) throws ExTwo { x.foo(new Integer(1), new Integer(2)); }\n" +
				"}\n" +
				"class Y<C extends Number> {\n" + 
				"	void foo(C x, C n) throws ExTwo {}\n" +
				"}\n" + 
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	void test2(X x) throws ExTwo { x.foo(new Integer(1), new Integer(2)); }\n" + 
			"	           ^\n" + 
			"X is a raw type. References to generic type X<A> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	void test2(X x) throws ExTwo { x.foo(new Integer(1), new Integer(2)); }\n" + 
			"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method foo(Number, Number) belongs to the raw type Y. References to generic type Y<C> should be parameterized\n" + 
			"----------\n"
			// test2 - warning: [unchecked] unchecked call to foo(C,C) as a member of the raw type Y
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011a() {
		this.runConformTest(
			new String[] {
				"Combined.java",
				"public class Combined<A, B> {\n" + 
				"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" + 
				"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" + 
				"	void pickOne(Combined<Integer,Integer> c) throws ExOne { c.pickOne(\"test\"); }\n" + 
				"	<T extends Number> void pickTwo(Number n, T t) throws ExOne {}\n" + 
				"	void pickTwo(A x, Number n) throws ExTwo {}\n" + 
				"	void pickTwo(Combined<Integer,Integer> c) throws ExTwo { c.pickTwo(new Integer(1), 2); }\n" +
				"}\n" +
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011b() {
		this.runNegativeTest(
			new String[] {
				"Test1.java",
				"public class Test1<AA, BB> {\n" +
				"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
				"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
				"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" +
				"}\n" +
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			"----------\n" + 
			"1. WARNING in Test1.java (at line 4)\n" + 
			"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" + 
			"	                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation pickOne(Comparable) of the generic method pickOne(T) of type Test1<Integer,Integer>\n" + 
			"----------\n" + 
			"2. WARNING in Test1.java (at line 4)\n" + 
			"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" + 
			"	                                                                  ^^^^^^^^^^\n" + 
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
			"----------\n"
		);
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
	}
	public void test012a() {
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
				"X.java",
				"public class X {\n" + 
				"	void a(G x) { System.out.print(1); }\n" + 
				"	void b(F x) { System.out.print(2); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		G<C> g = null;\n" +
				"		new X().a(h);\n" +
				"		new X().a(g);\n" +
				"		new X().b(h);\n" +
				"		new X().b(g);\n" +
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"1122"
		);
	}
	public void test014a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void a(G<C> x) { System.out.print(1); }\n" + 
				"	void b(F<C> x) { System.out.print(2); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H h = null;\n" +
				"		G g = null;\n" +
				"		new X().a(h);\n" +
				"		new X().a(g);\n" +
				"		new X().b(h);\n" +
				"		new X().b(g);\n" +
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"1122"
		);
	}
	public void test014b() {
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
	}
	public void test014c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void a(G x) {}\n" + 
				"	void a(F<C> x) {}\n" + 
				"	void b(G<C> x) {}\n" + 
				"	void b(F x) {}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		new X().a(h);\n" +
				"		new X().b(h);\n" +
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
			"1. WARNING in X.java (at line 2)\r\n" + 
			"	void a(G x) {}\r\n" + 
			"	       ^\n" + 
			"G is a raw type. References to generic type G<T2> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\r\n" + 
			"	void b(F x) {}\r\n" + 
			"	       ^\n" + 
			"F is a raw type. References to generic type F<T1> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\r\n" + 
			"	new X().a(h);\r\n" + 
			"	        ^\n" + 
			"The method a(G) is ambiguous for the type X\n" + 
			"----------\n"
			// reference to a is ambiguous, both method a(G) in X and method a(F<C>) in X match
		);
	}
	public void test014d() {
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
	}
	public void test014e() {
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
	}
	public void test014f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<E1, E2 extends B> void three(G<E2> x) {}\n" + 
				"	<E3 extends C> void three(F<E3> x) {}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		new X().three(h);\n" +
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
			"	new X().three(h);\r\n" + 
			"	        ^^^^^\n" + 
			"The method three(G<C>) is ambiguous for the type X\n" + 
			"----------\n"
		);
	}
	public void test014g() {
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
	public void test014h() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void x(G x) { System.out.print(true); }\n" + 
				"	void x(F x) { System.out.print(false); }\n" +  
				"	void x2(G<C> x) { System.out.print(true); }\n" + 
				"	void x2(F<C> x) { System.out.print(false); }\n" +  
				"	void a(G x) {}\n" + 
				"	void a(F<C> x) {}\n" +  
				"	void a2(G x) {}\n" + 
				"	<T extends C> void a2(F<T> x) {}\n" +  
				"	void a3(G x) {}\n" + 
				"	<T extends F<C>> void a3(T x) {}\n" + 
				"	void a4(G x) {}\n" + 
				"	<T extends C, S extends F<T>> void a4(S x) {}\n" + 
				"	<T extends G> void a5(T x) {}\n" + 
				"	void a5(F<C> x) {}\n" +  
				"	void b(G<C> x) { System.out.print(true); }\n" + 
				"	void b(F x) { System.out.print(false); }\n" +  
				"	void b2(G<C> x) { System.out.print(true); }\n" + 
				"	<T extends F> void b2(T x) { System.out.print(false); }\n" +  
				"	<T extends C> void b3(G<T> x) { System.out.print(true); }\n" + 
				"	void b3(F x) { System.out.print(false); }\n" +  
				"	<T extends G<C>> void b4(T x) { System.out.print(true); }\n" + 
				"	void b4(F x) { System.out.print(false); }\n" +  
				"	<T extends C, S extends G<T>> void b5(S x) { System.out.print(true); }\n" + 
				"	void b5(F x) { System.out.print(false); }\n" + 
				"	void c(G x) { System.out.print(true); }\n" + 
				"	<T extends C> void c(F x) { System.out.print(false); }\n" +  
				"	public static void main(String[] args) {\n" + 
				"		H<C> h = null;\n" +
				"		H hraw = null;\n" +
				"		new X().x(h);\n" +
				"		new X().x(hraw);\n" +
				"		new X().x2(h);\n" +
				"		new X().x2(hraw);\n" +
				"		new X().b(h);\n" +
				"		new X().b(hraw);\n" +
				"		new X().b2(h);\n" +
				"		new X().b2(hraw);\n" +
				"		new X().b3(h);\n" +
				"		new X().b3(hraw);\n" +
				"		new X().b4(h);\n" +
				"		new X().b4(hraw);\n" +
				"		new X().b5(h);\n" +
				"		new X().b5(hraw);\n" +
				"		new X().c(h);\n" +
				"		new X().c(hraw);\n" +
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" + 
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"truetruetruetruetruetruetruetruetruetruetruetruetruetruetruetrue"
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y extends X {\n" + 
				"	public static void ambiguousCases() {\n" + 
				"		H<C> h = null;\n" +
				"		H hraw = null;\n" +
				"		new X().a(h);\n" +
				"		new X().a(hraw);\n" +
				"		new X().a2(h);\n" +
				"		new X().a2(hraw);\n" +
				"		new X().a3(h);\n" +
				"		new X().a3(hraw);\n" +
				"		new X().a4(h);\n" +
				"		new X().a4(hraw);\n" +
				"		new X().a5(h);\n" +
				"		new X().a5(hraw);\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in Y.java (at line 4)\n" + 
			"	H hraw = null;\n" + 
			"	^\n" + 
			"H is a raw type. References to generic type H<T3> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 5)\n" + 
			"	new X().a(h);\n" + 
			"	        ^\n" + 
			"The method a(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"3. ERROR in Y.java (at line 6)\n" + 
			"	new X().a(hraw);\n" + 
			"	        ^\n" + 
			"The method a(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"4. ERROR in Y.java (at line 7)\n" + 
			"	new X().a2(h);\n" + 
			"	        ^^\n" + 
			"The method a2(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"5. ERROR in Y.java (at line 8)\n" + 
			"	new X().a2(hraw);\n" + 
			"	        ^^\n" + 
			"The method a2(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"6. ERROR in Y.java (at line 9)\n" + 
			"	new X().a3(h);\n" + 
			"	        ^^\n" + 
			"The method a3(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"7. ERROR in Y.java (at line 10)\n" + 
			"	new X().a3(hraw);\n" + 
			"	        ^^\n" + 
			"The method a3(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"8. ERROR in Y.java (at line 11)\n" + 
			"	new X().a4(h);\n" + 
			"	        ^^\n" + 
			"The method a4(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"9. ERROR in Y.java (at line 12)\n" + 
			"	new X().a4(hraw);\n" + 
			"	        ^^\n" + 
			"The method a4(G) is ambiguous for the type X\n" + 
			"----------\n" + 
			"10. ERROR in Y.java (at line 13)\n" + 
			"	new X().a5(h);\n" + 
			"	        ^^\n" + 
			"The method a5(H<C>) is ambiguous for the type X\n" + 
			"----------\n" + 
			"11. ERROR in Y.java (at line 14)\n" + 
			"	new X().a5(hraw);\n" + 
			"	        ^^\n" + 
			"The method a5(H) is ambiguous for the type X\n" + 
			"----------\n",
			null,
			false
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void test015() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public class E {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		IJ ij = new K();\n" + 
				"		try { ij.m(); } catch(E11 e) {}\n" +
				"	}\n" +
				"}\n" + 
				"interface I { void m() throws E1; }\n" + 
				"interface J { void m() throws E11; }\n" +
				"interface IJ extends I, J {}\n" + 
				"class K implements IJ { public void m() {} }\n" + 
				"class E1 extends Exception { static final long serialVersionUID = 1; }\n" +
				"class E11 extends E1 { static final long serialVersionUID = 2; }\n" + 
				"class E2 extends Exception { static final long serialVersionUID = 3; }"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void _test016() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public class E {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		IJ ij = new K();\n" + 
				"		try { ij.m(); } catch(E11 e) {}\n" +
				"	}\n" +
				"}\n" + 
				"interface I { void m() throws E1; }\n" + 
				"interface J { void m() throws E2, E11; }\n" +
				"interface IJ extends I, J {}\n" + 
				"class K implements IJ { public void m() {} }\n" + 
				"class E1 extends Exception { static final long serialVersionUID = 1; }\n" +
				"class E11 extends E1 { static final long serialVersionUID = 2; }\n" + 
				"class E2 extends Exception { static final long serialVersionUID = 3; }"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void _test016a() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public class E {\n" + 
				"	public static void main(String[] s) {\n" + 
				"		IJ ij = new K();\n" + 
				"		ij.m();\n" + 
				"		try { ij.m(); } catch(E2 e) {}\n" +
				"	}\n" +
				"}\n" + 
				"interface I { void m() throws E1; }\n" + 
				"interface J { void m() throws E2, E11; }\n" +
				"interface IJ extends I, J {}\n" + 
				"class K implements IJ { public void m() {} }\n" + 
				"class E1 extends Exception { static final long serialVersionUID = 1; }\n" +
				"class E11 extends E1 { static final long serialVersionUID = 2; }\n" + 
				"class E2 extends Exception { static final long serialVersionUID = 3; }"
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 4)\r\n" + 
			"	ij.m();\r\n" + 
			"	^^^^^^\n" + 
			"Unhandled exception type E11\n" + 
			"----------\n" + 
			"2. ERROR in E.java (at line 5)\r\n" + 
			"	try { ij.m(); } catch(E2 e) {}\r\n" + 
			"	      ^^^^^^\n" + 
			"Unhandled exception type E11\n" + 
			"----------\n" + 
			"3. ERROR in E.java (at line 5)\r\n" + 
			"	try { ij.m(); } catch(E2 e) {}\r\n" + 
			"	                      ^^\n" + 
			"Unreachable catch block for E2. This exception is never thrown from the try statement body\n" + 
			"----------\n"
			// 4: unreported exception E11; must be caught or declared to be thrown
			// 5: exception E2 is never thrown in body of corresponding try statement
			// 5: unreported exception E11; must be caught or declared to be thrown
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149893
	public void test017() {
		this.runConformTest(
			new String[] {
				"AbstractFilter.java",
				"import java.util.*;\n" + 
				"public class AbstractFilter<T> implements IFilter<T> {\n" + 
				"	public final <E extends T> boolean selekt(E obj) { return true; }\n" + 
				"	public final <E extends T> List<E> filter(List<E> elements) {\n" + 
				"		if ((elements == null) || (elements.size() == 0)) return elements;\n" +
				"		List<E> okElements = new ArrayList<E>(elements.size());\n" +
				"		for (E obj : elements) {\n" +
				"			if (selekt(obj)) okElements.add(obj);\n" +
				"		}\n" +
				"		return okElements;" +
				"	}\n" +
				"}\n" + 
				"interface IFilter<T> {\n" + 
				"	<E extends T> boolean selekt(E obj);\n" +
				"	<E extends T> List<E> filter(List<E> elements);\n" + 
				"}"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	public void test018() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" + 
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" + 
			"    System.out.print(false);\n" + 
			"    return new X<U>();\n" + 
			"  }\n" + 
			"}\n" + 
			"public class Y<V extends String> extends X<V> {\n" + 
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" + 
			"    System.out.print(true);\n" + 
			"    return new Y<W>();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) throws Exception {\n" + 
			"    Y.make(String.class);\n" + 
			"  }\n" + 
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// in fact, <W extends String> Y<W> make(Class<W> clazz) is the most
	// specific method according to JLS 15.12.2.5
	public void test019() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" + 
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" + 
			"    System.out.print(false);\n" + 
			"    return new X<U>();\n" + 
			"  }\n" + 
			"}\n" + 
			"public class Y<V extends String> extends X<V> {\n" + 
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" + 
			"    System.out.print(true);\n" + 
			"    return new Y<W>();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) throws Exception {\n" + 
			"    Y.make(getClazz());\n" + 
			"  }\n" + 
			"  public static Class getClazz() {\n" + 
			"    return String.class;\n" + 
			"  }\n" + 
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	public void test020() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" + 
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" + 
			"    System.out.print(true);\n" + 
			"    return new X<U>();\n" + 
			"  }\n" + 
			"}\n" + 
			"public class Y<V extends String> extends X<V> {\n" + 
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" + 
			"    System.out.print(false);\n" + 
			"    return new Y<W>();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) throws Exception {\n" + 
			"    Y.make(getClazz().newInstance().getClass());\n" + 
			"  }\n" + 
			"  public static Class getClazz() {\n" + 
			"    return String.class;\n" + 
			"  }\n" + 
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// variant: having both methods in the same class should not change anything
	public void test021() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" + 
			"}\n" + 
			"public class Y<V extends String> extends X<V> {\n" + 
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" + 
			"    System.out.print(true);\n" + 
			"    return new Y<W>();\n" + 
			"  }\n" + 
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" + 
			"    System.out.print(false);\n" + 
			"    return new X<U>();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) throws Exception {\n" + 
			"    Y.make(getClazz());\n" + 
			"  }\n" + 
			"  public static Class getClazz() {\n" + 
			"    return String.class;\n" + 
			"  }\n" + 
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// variant: using instances triggers raw methods, which are ambiguous
	public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T extends Object> {\n" + 
			"}\n" + 
			"class Y<V extends String> extends X<V> {\n" + 
			"  public <W extends String> Y<W> make(Class<W> clazz) {\n" + 
			"    return new Y<W>();\n" + 
			"  }\n" + 
			"  public <U extends Object> X<U> make(Class<U> clazz) {\n" + 
			"    return new X<U>();\n" + 
			"  }\n" + 
			"  public static void main(String[] args) throws Exception {\n" + 
			"    Y y = new Y();\n" + 
			"    y.make(String.class);\n" + 
			"    y.make(getClazz());\n" + 
			"    y.make(getClazz().newInstance().getClass());\n" + 
			"  }\n" + 
			"  public static Class getClazz() {\n" + 
			"    return String.class;\n" + 
			"  }\n" + 
			"}"			
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	class Y<V extends String> extends X<V> {\n" + 
		"	                  ^^^^^^\n" + 
		"The type parameter V should not be bounded by the final type String. Final types cannot be further extended\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 4)\n" + 
		"	public <W extends String> Y<W> make(Class<W> clazz) {\n" + 
		"	                  ^^^^^^\n" + 
		"The type parameter W should not be bounded by the final type String. Final types cannot be further extended\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 11)\n" + 
		"	Y y = new Y();\n" + 
		"	^\n" + 
		"Y is a raw type. References to generic type Y<V> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 11)\n" + 
		"	Y y = new Y();\n" + 
		"	          ^\n" + 
		"Y is a raw type. References to generic type Y<V> should be parameterized\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 12)\n" + 
		"	y.make(String.class);\n" + 
		"	  ^^^^\n" + 
		"The method make(Class) is ambiguous for the type Y\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 13)\n" + 
		"	y.make(getClazz());\n" + 
		"	  ^^^^\n" + 
		"The method make(Class) is ambiguous for the type Y\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 14)\n" + 
		"	y.make(getClazz().newInstance().getClass());\n" + 
		"	  ^^^^\n" + 
		"The method make(Class) is ambiguous for the type Y\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 16)\n" + 
		"	public static Class getClazz() {\n" + 
		"	              ^^^^^\n" + 
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
		"----------\n");
	}	
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159711
public void test023() {
this.runConformTest(
	new String[] {
		"X.java",
		"import java.util.*;\n" + 
		"public class X {\n" +
		"  public static void staticFoo(Collection<?> p) {\n" +
		"    System.out.print(1);\n" +
		"  }\n" +
		"  public static <T extends List<?>> void staticFoo(T p) {\n" + 
		"    System.out.print(2);\n" +
		"  }\n" +
		"  public void foo(Collection<?> p) {\n" +
		"    System.out.print(1);\n" +
		"  }\n" +
		"  public <T extends List<?>> void foo(T p) {\n" + 
		"    System.out.print(2);\n" +
		"  }\n" +
		"  public void foo2(Collection<?> p) {\n" +
		"    System.out.print(1);\n" +
		"  }\n" +
		"  public void foo2(List<?> p) {\n" + 
		"    System.out.print(2);\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" + 
		"    staticFoo(new ArrayList<String>(Arrays.asList(\"\")));\n" + 
		"    new X().foo(new ArrayList<String>(Arrays.asList(\"\")));\n" + 
		"    new X().foo2(new ArrayList<String>(Arrays.asList(\"\")));\n" + 
		"  }\n" +
		"}"
	},
	"222");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159711
// self contained variant
public void test024() {
this.runConformTest(
	new String[] {
		"X.java",
		"public class X {\n" +
		"  public static void foo(L1<?> p) {\n" +
		"    System.out.println(1);\n" +
		"  }\n" +
		"  public static <T extends L2<?>> void foo(T p) {\n" + 
		"    System.out.println(2);\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" + 
		"    foo(new L3<String>());\n" + 
		"  }\n" +
		"}",
		"L1.java",
		"public interface L1<T> {\n" +
		"}",
		"L2.java",
		"public interface L2<T> extends L1<T> {\n" +
		"}",
		"L3.java",
		"public class L3<T> implements L2<T> {\n" +
		"  public L3() {\n" +
		"  }\n" +
		"}",
	},
	"2");
}	
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  J m = new Y();" +
			"  void foo() {\n" +
			"    m.foo(1.0f);\n" +
			"  }\n" +
			"}",
			"I.java",
			"public interface I {\n" +
			"  <T extends Number> T foo(final Number p);\n" +
			"}",
			"J.java",
			"public interface J extends I {\n" +
			"  Float foo(final Number p);\n" +
			"}",
			"Y.java",
			"public class Y implements J {\n" +
			"  public Float foo(final Number p){\n" +
			"    return null;" +
			"  }\n" +
			"}",
		},
		"");
}	
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
// variant
public void test026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    (new Y()).foo(1.0f);\n" +
			"  }\n" +
			"}",
			"I.java",
			"public interface I {\n" +
			"  <T extends Number> T foo(final Number p);\n" +
			"}",
			"J.java",
			"public interface J extends I {\n" +
			"  Float foo(final Number p);\n" +
			"}",
			"Y.java",
			"public class Y implements J {\n" +
			"  public Float foo(final Number p){\n" +
			"    return null;" +
			"  }\n" +
			"}",
		},
		"");
}	
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
// variant
public void test027() {
	this.runNegativeTest(
		new String[] {
			"J.java",
			"public interface J {\n" +
			"  <T extends Number> T foo(final Number p);\n" +
			"  Float foo(final Number p);\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in J.java (at line 2)\n" + 
		"	<T extends Number> T foo(final Number p);\n" + 
		"	                     ^^^^^^^^^^^^^^^^^^^\n" + 
		"Duplicate method foo(Number) in type J\n" + 
		"----------\n" + 
		"2. ERROR in J.java (at line 3)\n" + 
		"	Float foo(final Number p);\n" + 
		"	      ^^^^^^^^^^^^^^^^^^^\n" + 
		"Duplicate method foo(Number) in type J\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// **
public void test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Irrelevant {}\n" + 
			"interface I {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"interface J extends Irrelevant, I {\n" + 
			"  String foo(Number n);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J, K {\n" + 
			"  void foo() {\n" + 
			"    foo(0.0f);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - simplified
// **
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface J {\n" + 
			"  String foo(Number n);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J, K {\n" + 
			"  void foo() {\n" + 
			"    foo(0.0f);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - same return type
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface J {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J, K {\n" + 
			"  void foo() {\n" + 
			"    foo(0.0f);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant
// **
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Irrelevant {}\n" + 
			"interface I {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"interface J extends Irrelevant, I {\n" + 
			"  String foo(Number n);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements Irrelevant, I, J, K {\n" + 
			"  void foo() {\n" + 
			"    foo(0.0f);\n" +
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
public void _test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J {\n" + 
			"}\n" + 
			"abstract class XX extends Exception implements Cloneable {}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// variant that shows that the use of a substitution is needed during the bounds
// check
public void _test032a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class Z { }\n" +
			"class Y <U> extends Z { }" +
			"interface I {\n" + 
			"  <T extends Y<T> & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J {\n" + 
			"}\n" + 
			"abstract class XX extends Y<XX> implements Cloneable {}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// variant
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J {\n" + 
			"  void foo() {\n" + 
			"  }\n" + 
			"  public XX foo(Number n) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}\n" + 
			"abstract class XX extends Exception implements Cloneable {}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// variant that rightly complains
public void test034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  NullPointerException foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X implements J, K {\n" + 
			"}\n" + 
			"abstract class XX extends Exception implements Cloneable {}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	XX foo(Number n);\n" + 
		"	^^\n" + 
		"Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	public abstract class X implements J, K {\n" + 
		"	                      ^\n" + 
		"The return type is incompatible with I.foo(Number), K.foo(Number), J.foo(Number)\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - the inheriting class implements foo
public void test035() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  abstract String foo(Number n);\n" + 
			"}\n" + 
			"public class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    foo(0.0f);\n" + // calls X#foo(Number)
			"  }\n" + 
			"  public String foo(Number n) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - extending instead of implementing
// **
public void test037() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(Number n);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  abstract String foo(Number n);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    foo(0.0f);\n" +
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - no promotion of parameter from float to Number
// **
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(float f);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  public abstract String foo(float f);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    foo(0.0f);\n" +
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - an explicit cast solves the issue
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(float f);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  public abstract String foo(float f);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    String s = ((J) this).foo(0.0f);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - an explicit cast solves the issue
public void test040() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(float f);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  public abstract String foo(float f);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    Object o = ((I) this).foo(0.0f);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - connecting return types
public void test041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(float f);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  public abstract String foo(float f);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    String s = ((I) this).foo(0.0f);\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	String s = ((I) this).foo(0.0f);\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Object to String\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - a further inheriting class implements String foo
// **
public void test042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(float f);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  public abstract String foo(float f);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    foo(0.0f);\n" +
			"  }\n" + 
			"}\n" + 
			"class Z extends X {\n" +
			"  @Override" + 
			"  public String foo(float f) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - a further inheriting class implements Object foo
public void test043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  Object foo(float f);\n" + 
			"}\n" + 
			"abstract class J {\n" + 
			"  public abstract String foo(float f);\n" + 
			"}\n" + 
			"public abstract class X extends J implements I {\n" + 
			"  void bar() {\n" + 
			"    foo(0.0f);\n" +
			"  }\n" + 
			"}\n" + 
			"class Z extends X {\n" +
			"  @Override\n" + 
			"  public Object foo(float f) {\n" +  // cannot override String foo
			"    return null;\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 14)\n" + 
		"	public Object foo(float f) {\n" + 
		"	       ^^^^^^\n" + 
		"The return type is incompatible with J.foo(float)\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163370
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<E> {}\n" + 
			"class Y<E> {}\n" + 
			"public class X<E extends Y<E>> implements I<E> {\n" + 
			"  public static <E extends Y<E>> X<E> bar(X<E> s) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  public static <E extends Y<E>> X<E> bar(I<E> c) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  public static <E extends Y<E>> X<E> foo(X<E> s) {\n" + 
			"    X<E> result = bar(s);\n" + 
			"    return result;\n" + 
			"  }\n" + 
			"}"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165620
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"abstract class Y<T> implements I<T> {\n" + 
			"}\n" + 
			"interface I<T> { \n" + 
			"}\n" + 
			"interface J<T> {\n" + 
			"}\n" + 
			"class X {\n" + 
			"  public static <V extends J<? super V>> V foo(final I<V> a)\n" + 
			"  {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  public static <V extends J<? super V>> V foo(final Y<V> a)\n" + 
			"  {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  public static <V extends J<? super V>> void test(final Y<V> a)\n" + 
			"  {\n" + 
			"    foo(a);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163370
// variant
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"abstract class Y<T, U> implements I<T, U> {\n" + 
			"}\n" + 
			"interface I<T, U> { \n" + 
			"}\n" + 
			"interface J<T, U> {\n" + 
			"}\n" + 
			"class X {\n" + 
			"  public static <V extends J<V, W>, W extends J<V, W>> V foo(final I<V, W> a)\n" + 
			"  {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  public static <V extends J<V, W>, W extends J<V, W>> V foo(final Y<V, W> a)\n" + 
			"  {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"  public static <V extends J<V, W>, W extends J<V, W>> void test(final Y<V, W> a)\n" + 
			"  {\n" + 
			"    foo(a);\n" + 
			"  }\n" + 
			"}"
		},
		"");
}
}
