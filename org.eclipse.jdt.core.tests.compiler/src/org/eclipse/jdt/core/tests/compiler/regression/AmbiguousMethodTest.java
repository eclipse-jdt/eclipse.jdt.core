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
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *								bug 399567 - [1.8] Different error message from the reference compiler
 *								bug 401796 - [1.8][compiler] don't treat default methods as overriding an independent inherited abstract method
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
  *								Bug 423505 - [1.8] Implement "18.5.4 More Specific Method Inference"
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AmbiguousMethodTest extends AbstractComparableTest {

	static {
//		TESTS_NAMES = new String [] { "test010a" };
	}
	public AmbiguousMethodTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return AmbiguousMethodTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
	}
	public void test000() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test { public static void main(String[] args) { new B().foo(new C()); } }
					class A { void foo(A a) {} }
					class B extends A { void foo(B b) { System.out.println(1); } }
					class C extends B {}"""
			},
			"1"
		);
	}
	public void test000a() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"""
						public class Test { public static void main(String[] args) { new Subtype<String>().foo(1, "works"); } }
						class Supertype<T1> { <U1> void foo(U1 u, T1 t) {} }
						class Subtype <T2> extends Supertype<T2> { <U3> void foo(U3 u, T2 t) { System.out.println(t); } }"""
				},
				"works"
			);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test001() {
		this.runConformTest(
			new String[] {
				"C.java",
				"""
					public class C { public static void main(String[] args) { new B().m("works"); } }
					class B extends A { @Override <T extends Comparable<T>> void m(T t) { System.out.println(t); } }
					abstract class A { abstract <T extends Comparable<T>> void m(T t); }"""
			},
			"works"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test002() {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static interface I1<E1> { void method(E1 o); }
						static interface I2<E2> { void method(E2 o); }
						static interface I3<E3, E4> extends I1<E3>, I2<E4> {}
						static class Class1 implements I3<String, String> {
							public void method(String o) { System.out.println(o); }
						}
						public static void main(String[] args) {
							I3<String, String> i = new Class1();
							i.method("works");
						}
					}"""
			},
			"works");
		} else {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"""
							public class X {
								static interface I1<E1> { void method(E1 o); }
								static interface I2<E2> { void method(E2 o); }
								static interface I3<E3, E4> extends I1<E3>, I2<E4> {}
								static class Class1 implements I3<String, String> {
									public void method(String o) { System.out.println(o); }
								}
								public static void main(String[] args) {
									I3<String, String> i = new Class1();
									i.method("works");
								}
							}"""
					},
					"""
						----------
						1. ERROR in X.java (at line 4)
							static interface I3<E3, E4> extends I1<E3>, I2<E4> {}
							                 ^^
						Name clash: The method method(E2) of type X.I2<E2> has the same erasure as method(E1) of type X.I1<E1> but does not override it
						----------
						""");
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test002a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static interface I1<E> { void method(E o); }
						static interface I2<E> { void method(E o); }
						static interface I3<E> extends I1<E>, I2<E> {}
						static class Class1 implements I3<String> {
							public void method(String o) { System.out.println(o); }
						}
						public static void main(String[] args) {
							I3<String> i = new Class1();
							i.method("works");
						}
					}"""
			},
			"works"
		);
	}
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X { void foo() { new BB().test(); } }
					class AA<T> { void test() {} }
					class BB extends AA<CC> { <U> void test() {} }
					class CC {}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X { void foo() { new BB().test(); } }
					                                       ^^^^
				The method test() is ambiguous for the type BB
				----------
				2. ERROR in X.java (at line 3)
					class BB extends AA<CC> { <U> void test() {} }
					                                   ^^^^^^
				Name clash: The method test() of type BB has the same erasure as test() of type AA<T> but does not override it
				----------
				"""
		);
	}
	public void test003a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void singleMatch() { System.out.print(new BB().test(new N(), new Integer(1))); }
						void betterMatch() { System.out.print(new CC().test(new N(), new Integer(1))); }
						void worseMatch() { System.out.print(new DD().test(new N(), new Integer(1))); }
						public static void main(String[] s) {
							new X().singleMatch();
							new X().betterMatch();
							new X().worseMatch();
						}
					}
					class AA<T> { int test(T t, Number num) { return 1; } }
					class BB extends AA<N> { @Override int test(N n, Number num) { return 2; } }
					class CC extends AA<M> { <U extends Number> int test(N n, U u) { return 3; } }
					class DD extends AA<N> { <U extends Number> int test(M m, U u) { return 4; } }
					class M {}
					class N extends M {}""",
			},
			"231"
		);
	}
	public void test003b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void ambiguous() { new BB().test(new N()); }
						void exactMatch() { new CC().test(new N()); }
					}
					class AA<T> { void test(T t) {} }
					class BB extends AA<N> { <U> void test(N n) {} }
					class CC extends AA<N> { @Override void test(N n) {} }
					class M {}
					class N extends M {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)\r
					void ambiguous() { new BB().test(new N()); }\r
					                            ^^^^
				The method test(N) is ambiguous for the type BB
				----------
				"""
		);
	}
	public void test003c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void ambiguous() { new BB().test(new N(), Integer.valueOf(1)); }
					}
					class AA<T> { void test(T t, Integer i) {} }
					class BB extends AA<M> { <U extends Number> void test(N n, U u) {} }
					class M {}
					class N extends M {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void ambiguous() { new BB().test(new N(), Integer.valueOf(1)); }
					                            ^^^^
				The method test(N, Integer) is ambiguous for the type BB
				----------
				"""
		);
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test(M<Integer> m) {
							m.id(Integer.valueOf(111));
						}
					}
					class C<T extends Number> { public void id(T t) {} }
					class M<TT> extends C<Integer> { public <ZZ> void id(Integer i) {} }
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					m.id(Integer.valueOf(111));
					  ^^
				The method id(Integer) is ambiguous for the type M<Integer>
				----------
				"""
		);
	}
	public void test004a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void test(M<Integer> m) {
							m.id(new Integer(111));
						}
					}
					class C<T extends Number> { public void id(T t) {} }
					class M<TT> extends C<Integer> { public void id(Integer i) {} }
					"""
			},
			""
		);
	}
	public void test005() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<S extends A> void foo() { }
						                   ^^^^^
					Duplicate method foo() in type X
					----------
					2. WARNING in X.java (at line 3)
						<N extends B> N foo() { return null; }
						                ^^^^^
					Duplicate method foo() in type X
					----------
					3. ERROR in X.java (at line 5)
						new X().foo();
						        ^^^
					The method foo() is ambiguous for the type X
					----------
					""":
					"""
						----------
						1. ERROR in X.java (at line 2)
							<S extends A> void foo() { }
							                   ^^^^^
						Duplicate method foo() in type X
						----------
						2. ERROR in X.java (at line 3)
							<N extends B> N foo() { return null; }
							                ^^^^^
						Duplicate method foo() in type X
						----------
						""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
							 <S extends A> void foo() { }
							 <N extends B> N foo() { return null; }
							 void test () {
							 	new X().foo();
							 }
					}
					class A {}
					class B {}
					"""
			},
			expectedCompilerLog);
/* javac 7
X.java:3: name clash: <N>foo() and <S>foo() have the same erasure
                 <N extends B> N foo() { return null; }
                                 ^
  where N,S are type-variables:
    N extends B declared in method <N>foo()
    S extends A declared in method <S>foo()
1 error
 */
	}
	public void test006() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"""
			----------
			1. ERROR in X.java (at line 3)
				new Y<Object>().foo("X");
				                ^^^
			The method foo(Object) is ambiguous for the type Y<Object>
			----------
			2. ERROR in X.java (at line 4)
				new Y<Object>().foo2("X");
				                ^^^^
			The method foo2(Object) is ambiguous for the type Y<Object>
			----------
			3. WARNING in X.java (at line 10)
				void foo(T2 t) {}
				     ^^^^^^^^^
			Name clash: The method foo(T2) of type Y<T2> has the same erasure as foo(U1) of type X<T> but does not override it
			----------
			4. WARNING in X.java (at line 11)
				<U3> void foo2(T2 t) {}
				          ^^^^^^^^^^
			Name clash: The method foo2(T2) of type Y<T2> has the same erasure as foo2(U2) of type X<T> but does not override it
			----------
			""":
			"""
				----------
				1. ERROR in X.java (at line 3)
					new Y<Object>().foo("X");
					                ^^^
				The method foo(Object) is ambiguous for the type Y<Object>
				----------
				2. ERROR in X.java (at line 4)
					new Y<Object>().foo2("X");
					                ^^^^
				The method foo2(Object) is ambiguous for the type Y<Object>
				----------
				3. ERROR in X.java (at line 10)
					void foo(T2 t) {}
					     ^^^^^^^^^
				Name clash: The method foo(T2) of type Y<T2> has the same erasure as foo(U1) of type X<T> but does not override it
				----------
				4. ERROR in X.java (at line 11)
					<U3> void foo2(T2 t) {}
					          ^^^^^^^^^^
				Name clash: The method foo2(T2) of type Y<T2> has the same erasure as foo2(U2) of type X<T> but does not override it
				----------
				""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					   void test() {
					   	new Y<Object>().foo("X");
					   	new Y<Object>().foo2("X");
					   }
						<U1> U1 foo(U1 t) {return null;}
						<U2> U2 foo2(U2 t) {return null;}
					}
					class Y<T2> extends X<T2> {
						void foo(T2 t) {}
						<U3> void foo2(T2 t) {}
					}
					"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: reference to foo is ambiguous, both method <U1>foo(U1) in X and method
 foo(T2) in Y match
        new Y<Object>().foo("X");
                       ^
  where U1,T2 are type-variables:
    U1 extends Object declared in method <U1>foo(U1)
    T2 extends Object declared in class Y
X.java:4: reference to foo2 is ambiguous, both method <U2>foo2(U2) in X and meth
od <U3>foo2(T2) in Y match
        new Y<Object>().foo2("X");
                       ^
  where U2,U3,T2 are type-variables:
    U2 extends Object declared in method <U2>foo2(U2)
    U3 extends Object declared in method <U3>foo2(T2)
    T2 extends Object declared in class Y
X.java:10: name clash: foo(T2) in Y and <U1>foo(U1) in X have the same erasure,
yet neither overrides the other
        void foo(T2 t) {}
             ^
  where T2,U1 are type-variables:
    T2 extends Object declared in class Y
    U1 extends Object declared in method <U1>foo(U1)
X.java:11: name clash: <U3>foo2(T2) in Y and <U2>foo2(U2) in X have the same era
sure, yet neither overrides the other
        <U3> void foo2(T2 t) {}
                  ^
  where U3,T2,U2 are type-variables:
    U3 extends Object declared in method <U3>foo2(T2)
    T2 extends Object declared in class Y
    U2 extends Object declared in method <U2>foo2(U2)
4 errors
 */
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129056
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"""
					public class B {
					   public static void main(String[] args) {
					   	new M().foo(Integer.valueOf(1), 2);
					   	new N().foo(Integer.valueOf(1), 2);
					   }
					}\
					interface I { void foo(Number arg1, Number arg2); }
					class M {
						public void foo(int arg1, int arg2) {}
						public void foo(Number arg1, Number arg2) {}
					}
					class N implements I {
						public void foo(int arg1, int arg2) {}
						public void foo(Number arg1, Number arg2) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in B.java (at line 3)\r
					new M().foo(Integer.valueOf(1), 2);\r
					        ^^^
				The method foo(int, int) is ambiguous for the type M
				----------
				2. ERROR in B.java (at line 4)\r
					new N().foo(Integer.valueOf(1), 2);\r
					        ^^^
				The method foo(int, int) is ambiguous for the type N
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 1
	public void test008() {
		this.runConformTest(
			new String[] {
				"AA.java",
				"""
					public class AA {
					   public static void main(String[] a) { System.out.print(new C().test(new T())); }
					}\
					class S {}
					class T extends S {}
					class B { <U extends S> int test(U u) {return -1;} }
					class C extends B { @Override int test(S s) {return 1;} }"""
			},
			"1"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 1
	public void test008a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					public class A { void check() { new C().test(new T()); } }
					class S {}
					class T extends S {}
					class B { int test(S s) {return 1;} }
					class C extends B { <U extends S> int test(U u) {return -1;} }"""
			},
			"""
				----------
				1. ERROR in A.java (at line 1)
					public class A { void check() { new C().test(new T()); } }
					                                        ^^^^
				The method test(T) is ambiguous for the type C
				----------
				2. ERROR in A.java (at line 5)
					class C extends B { <U extends S> int test(U u) {return -1;} }
					                                      ^^^^^^^^^
				Name clash: The method test(U) of type C has the same erasure as test(S) of type B but does not override it
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 2
	// see also Bug 399567 - [1.8] Different error message from the reference compiler
	public void test009() {
		String[] testFiles =
				new String[] {
				"T.java",
				"""
					import java.util.*;
					public class T {
					   void test() {
					   	OrderedSet<String> os = null;
					   	os.add("hello");
					   	OrderedSet<Integer> os2 = null;
					   	os2.add(1);
					   }
					}
					interface OrderedSet<E> extends List<E>, Set<E> { boolean add(E o); }
					"""
		};
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			this.runConformTest(testFiles, "");
		else
			this.runNegativeTest(
				testFiles,
				"""
					----------
					1. WARNING in T.java (at line 5)
						os.add("hello");
						^^
					Null pointer access: The variable os can only be null at this location
					----------
					2. WARNING in T.java (at line 7)
						os2.add(1);
						^^^
					Null pointer access: The variable os2 can only be null at this location
					----------
					3. ERROR in T.java (at line 10)
						interface OrderedSet<E> extends List<E>, Set<E> { boolean add(E o); }
						          ^^^^^^^^^^
					Duplicate default methods named spliterator with the parameters () and () are inherited from the types Set<E> and List<E>
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 variant to make it pass on JRE8
	public void test009a() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
			new String[] {
				"T.java",
				"""
					import java.util.*;
					public class T {
					   void test() {
					   	OrderedSet<String> os = null;
					   	os.add("hello");
					   	OrderedSet<Integer> os2 = null;
					   	os2.add(1);
					   }
					}
					interface OrderedSet<E> extends List<E>, Set<E> {
						boolean add(E o);
					   default Spliterator<E> spliterator() { return null; }
					}
					"""
			},
			""
		);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void test010a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  	interface Listener {}
				  	interface ErrorListener {}
				  	static <L1 extends Listener & ErrorListener> Object createParser(L1 l) { return null; }
				  	static <L2 extends ErrorListener & Listener> Object createParser(L2 l) { return null; }
				   public static void main(String[] s) {
				   	class A implements Listener, ErrorListener {}
				   	createParser(new A());
				   }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				createParser(new A());
				^^^^^^^^^^^^
			The method createParser(A) is ambiguous for the type X
			----------
			"""
	);
// javac 7 randomly picks which ever method is second
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void test010b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  	interface Listener {}
				  	interface ErrorListener {}
				  	static <L1 extends Listener> int createParser(L1 l) { return 1; }
				  	static <L2 extends ErrorListener & Listener> int createParser(L2 l) { return 2; }
				   public static void main(String[] s) {
				   	class A implements Listener, ErrorListener {}
				   	System.out.print(createParser(new A()));
				   }
				}"""
		},
		"2"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void test010c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  	interface Listener {}
				  	interface ErrorListener {}
				  	static int createParser(Listener l) { return 1; }
				  	static <L extends ErrorListener & Listener> int createParser(L l) { return 2; }
				   public static void main(String[] s) {
				   	class A implements Listener, ErrorListener {}
				   	System.out.print(createParser(new A()));
				   }
				}"""
		},
		"2"
	);
}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<A extends Number> extends Y<A> {
						<T> void foo(A n, T t) throws ExOne {}
						void test(X<Integer> x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }
						void test2(X x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }
					}
					class Y<C extends Number> {
						void foo(C x, C n) throws ExTwo {}
					}
					class ExOne extends Exception {static final long serialVersionUID = 1;}
					class ExTwo extends Exception {static final long serialVersionUID = 2;}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					void test2(X x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }
					           ^
				X is a raw type. References to generic type X<A> should be parameterized
				----------
				2. WARNING in X.java (at line 4)
					void test2(X x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }
					                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method foo(Number, Number) belongs to the raw type Y. References to generic type Y<C> should be parameterized
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011a() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"""
			----------
			1. WARNING in Combined.java (at line 2)
				<T extends Comparable<T>> void pickOne(T value) throws ExOne {}
				                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Erasure of method pickOne(T) is the same as another method in type Combined<A,B>
			----------
			2. WARNING in Combined.java (at line 3)
				<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
				      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Erasure of method pickOne(Comparable<T>) is the same as another method in type Combined<A,B>
			----------
			""":
			"""
				----------
				1. ERROR in Combined.java (at line 2)
					<T extends Comparable<T>> void pickOne(T value) throws ExOne {}
					                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method pickOne(T) is the same as another method in type Combined<A,B>
				----------
				2. ERROR in Combined.java (at line 3)
					<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method pickOne(Comparable<T>) is the same as another method in type Combined<A,B>
				----------
				""";
		this.runNegativeTest(
			new String[] {
				"Combined.java",
				"""
					public class Combined<A, B> {
						<T extends Comparable<T>> void pickOne(T value) throws ExOne {}
						<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
						void pickOne(Combined<Integer,Integer> c) throws ExOne { c.pickOne("test"); }
						<T extends Number> void pickTwo(Number n, T t) throws ExOne {}
						void pickTwo(A x, Number n) throws ExTwo {}
						void pickTwo(Combined<Integer,Integer> c) throws ExTwo { c.pickTwo(Integer.valueOf(1), 2); }
					}
					class ExOne extends Exception {static final long serialVersionUID = 1;}
					class ExTwo extends Exception {static final long serialVersionUID = 2;}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T#1>pickOne(Comparable<T#1>) and <T#2>pickOne(T#2) have the same erasure
        <T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
              ^
  where T#1,T#2 are type-variables:
    T#1 extends Object declared in method <T#1>pickOne(Comparable<T#1>)
    T#2 extends Comparable<T#2> declared in method <T#2>pickOne(T#2)
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011b() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"""
			----------
			1. WARNING in Test1.java (at line 2)
				<T extends Comparable<T>> void pickOne(T value) throws ExOne {}
				                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Erasure of method pickOne(T) is the same as another method in type Test1<AA,BB>
			----------
			2. WARNING in Test1.java (at line 3)
				<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
				      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Erasure of method pickOne(Comparable<T>) is the same as another method in type Test1<AA,BB>
			----------
			3. WARNING in Test1.java (at line 4)
				void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
				                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation pickOne(Comparable) of the generic method pickOne(T) of type Test1<Integer,Integer>
			----------
			4. WARNING in Test1.java (at line 4)
				void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
				                                                                  ^^^^^^^^^^
			Comparable is a raw type. References to generic type Comparable<T> should be parameterized
			----------
			""":
			"""
				----------
				1. ERROR in Test1.java (at line 2)
					<T extends Comparable<T>> void pickOne(T value) throws ExOne {}
					                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method pickOne(T) is the same as another method in type Test1<AA,BB>
				----------
				2. ERROR in Test1.java (at line 3)
					<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Erasure of method pickOne(Comparable<T>) is the same as another method in type Test1<AA,BB>
				----------
				3. WARNING in Test1.java (at line 4)
					void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
					                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation pickOne(Comparable) of the generic method pickOne(T) of type Test1<Integer,Integer>
				----------
				4. WARNING in Test1.java (at line 4)
					void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
					                                                                  ^^^^^^^^^^
				Comparable is a raw type. References to generic type Comparable<T> should be parameterized
				----------
				""";
		this.runNegativeTest(
			new String[] {
				"Test1.java",
				"""
					public class Test1<AA, BB> {
						<T extends Comparable<T>> void pickOne(T value) throws ExOne {}
						<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
						void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
					}
					class ExOne extends Exception {static final long serialVersionUID = 1;}
					class ExTwo extends Exception {static final long serialVersionUID = 2;}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T#1>pickOne(Comparable<T#1>) and <T#2>pickOne(T#2) have the same erasure
        <T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
              ^
  where T#1,T#2 are type-variables:
    T#1 extends Object declared in method <T#1>pickOne(Comparable<T#1>)
    T#2 extends Comparable<T#2> declared in method <T#2>pickOne(T#2)
X.java:4: warning: [unchecked] unchecked method invocation: method pickOne in class Test1 is applied to given types
        void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
                                                                        ^
  required: T
  found: Comparable
  where T is a type-variable:
    T extends Comparable<T> declared in method <T>pickOne(T)
1 error
1 warning
 */
	}
	public void test012() {
		this.runConformTest(
			new String[] {
				"XX.java",
				"""
					public class XX {
						public static void main(String[] s) { System.out.println(new B().id(new Integer(1))); }
					}
					class A<T extends Number> { public int id(T t) {return 2;} }
					class B extends A<Integer> { public int id(Integer i) {return 1;} }"""
			},
			"1"
		);
	}
	public void test012a() {
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"""
					public class XX {
						public static void main(String[] s) { System.out.println(new B().id(Integer.valueOf(1))); }
					}
					class A<T extends Number> { public int id(T t) {return 2;} }
					class B extends A<Integer> { public <ZZ> int id(Integer i) {return 1;} }"""
			},
			"""
				----------
				1. ERROR in XX.java (at line 2)\r
					public static void main(String[] s) { System.out.println(new B().id(Integer.valueOf(1))); }\r
					                                                                 ^^
				The method id(Integer) is ambiguous for the type B
				----------
				"""
		);
	}
	public void test013() {
			this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<E extends A> void m(E e) { System.out.print(1); }
						<E extends B> void m(E e) { System.out.print(2); }
						public static void main(String[] args) {
							new X().m(new A());
							new X().m(new B());
						}
					}
					class A {}
					class B extends A {}
					"""
			},
			"12"
		);
	}
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void a(G x) { System.out.print(1); }
						void b(F x) { System.out.print(2); }
						public static void main(String[] args) {
							H<C> h = null;
							G<C> g = null;
							new X().a(h);
							new X().a(g);
							new X().b(h);
							new X().b(g);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"1122"
		);
	}
	public void test014a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void a(G<C> x) { System.out.print(1); }
						void b(F<C> x) { System.out.print(2); }
						public static void main(String[] args) {
							H h = null;
							G g = null;
							new X().a(h);
							new X().a(g);
							new X().b(h);
							new X().b(g);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"1122"
		);
	}
	public void test014b() {
		this.runConformTest(
			new String[] {
				"X0.java",
				"""
					public class X0 {
						void two(G x) { System.out.print(1); }
						void two(F<A> x) { System.out.print(2); }
						void three(G x) { System.out.print(3); }
						void three(F<B> x) { System.out.print(4); }
						public static void main(String[] args) {
							H<C> h = null;
							new X0().two(h);
							new X0().three(h);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"13"
		);
	}
	public void test014c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void a(G x) {}
						void a(F<C> x) {}
						void b(G<C> x) {}
						void b(F x) {}
						public static void main(String[] args) {
							H<C> h = null;
							new X().a(h);
							new X().b(h);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)\r
					void a(G x) {}\r
					       ^
				G is a raw type. References to generic type G<T2> should be parameterized
				----------
				2. WARNING in X.java (at line 5)\r
					void b(F x) {}\r
					       ^
				F is a raw type. References to generic type F<T1> should be parameterized
				----------
				3. ERROR in X.java (at line 8)\r
					new X().a(h);\r
					        ^
				The method a(G) is ambiguous for the type X
				----------
				"""
		);
	}
	public void test014d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void one(G<B> x) {}
						void one(F<B> x) {}
						public static void main(String[] args) {
							H<C> h = null;
							new X().one(h);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)\r
					new X().one(h);\r
					        ^^^
				The method one(G<B>) in the type X is not applicable for the arguments (H<C>)
				----------
				"""
		);
	}
	public void test014e() {
		this.runConformTest(
			new String[] {
				"X1.java",
				"""
					public class X1 {
						void two(G<C> x) { System.out.print(1); }
						void two(F<B> x) { System.out.print(2); }
						void three(G<B> x) { System.out.print(3); }
						void three(F<C> x) { System.out.print(4); }
						void four(G<C> x) { System.out.print(5); }
						void four(F<C> x) { System.out.print(6); }
						public static void main(String[] args) {
							H<C> h = null;
							new X1().two(h);
							new X1().three(h);
							new X1().four(h);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"145"
		);
	}
	public void test014f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<E1, E2 extends B> void three(G<E2> x) {}
						<E3 extends C> void three(F<E3> x) {}
						public static void main(String[] args) {
							H<C> h = null;
							new X().three(h);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)\r
					new X().three(h);\r
					        ^^^^^
				The method three(G<C>) is ambiguous for the type X
				----------
				"""
		);
	}
	public void test014g() {
		this.runConformTest(
			new String[] {
				"X3.java",
				"""
					public class X3 {
						<E1, E2 extends B> void one(G<E2> x) { System.out.print(1); }
						<E3 extends B> void one(F<E3> x) { System.out.print(2); }
						<E1, E2 extends C> void two(G<E2> x) { System.out.print(3); }
						<E3 extends B> void two(F<E3> x) { System.out.print(4); }
						<E1, E2 extends C> void four(G<E2> x) { System.out.print(5); }
						<E3 extends C> void four(F<E3> x) { System.out.print(6); }
						public static void main(String[] args) {
							H<C> h = null;
							new X3().one(h);
							new X3().two(h);
							new X3().four(h);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"135"
		);
	}
	public void test014h() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void x(G x) { System.out.print(true); }
						void x(F x) { System.out.print(false); }
						void x2(G<C> x) { System.out.print(true); }
						void x2(F<C> x) { System.out.print(false); }
						void a(G x) {}
						void a(F<C> x) {}
						void a2(G x) {}
						<T extends C> void a2(F<T> x) {}
						void a3(G x) {}
						<T extends F<C>> void a3(T x) {}
						void a4(G x) {}
						<T extends C, S extends F<T>> void a4(S x) {}
						<T extends G> void a5(T x) {}
						void a5(F<C> x) {}
						<T extends G> void a6(T x) {}
						<T extends C, S extends F<T>> void a6(S x) {}
						void b(G<C> x) { System.out.print(true); }
						void b(F x) { System.out.print(false); }
						void b2(G<C> x) { System.out.print(true); }
						<T extends F> void b2(T x) { System.out.print(false); }
						<T extends C> void b3(G<T> x) { System.out.print(true); }
						void b3(F x) { System.out.print(false); }
						<T extends G<C>> void b4(T x) { System.out.print(true); }
						void b4(F x) { System.out.print(false); }
						<T extends C, S extends G<T>> void b5(S x) { System.out.print(true); }
						void b5(F x) { System.out.print(false); }
						void c(G x) { System.out.print(true); }
						<T extends C> void c(F x) { System.out.print(false); }
						public static void main(String[] args) {
							H<C> h = null;
							H hraw = null;
							new X().x(h);
							new X().x(hraw);
							new X().x2(h);
							new X().x2(hraw);
							new X().b(h);
							new X().b(hraw);
							new X().b2(h);
							new X().b2(hraw);
							new X().b3(h);
							new X().b3(hraw);
							new X().b4(h);
							new X().b4(hraw);
							new X().b5(h);
							new X().b5(hraw);
							new X().c(h);
							new X().c(hraw);
						}
					}
					class A {}
					class B extends A {}
					class C extends B {}
					class F<T1> {}\s
					class G<T2> extends F<T2> {}
					class H<T3> extends G<T3> {}"""
			},
			"truetruetruetruetruetruetruetruetruetruetruetruetruetruetruetrue"
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"""
					public class Y extends X {
						public static void ambiguousCases() {
							H<C> h = null;
							H hraw = null;
							new X().a(h);
							new X().a(hraw);
							new X().a2(h);
							new X().a2(hraw);
							new X().a3(h);
							new X().a3(hraw);
							new X().a4(h);
							new X().a4(hraw);
							new X().a5(h);
							new X().a5(hraw);
							new X().a6(h);
							new X().a6(hraw);
						}
					}
					"""
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. WARNING in Y.java (at line 4)
					H hraw = null;
					^
				H is a raw type. References to generic type H<T3> should be parameterized
				----------
				2. ERROR in Y.java (at line 5)
					new X().a(h);
					        ^
				The method a(G) is ambiguous for the type X
				----------
				3. ERROR in Y.java (at line 6)
					new X().a(hraw);
					        ^
				The method a(G) is ambiguous for the type X
				----------
				4. ERROR in Y.java (at line 7)
					new X().a2(h);
					        ^^
				The method a2(G) is ambiguous for the type X
				----------
				5. ERROR in Y.java (at line 8)
					new X().a2(hraw);
					        ^^
				The method a2(G) is ambiguous for the type X
				----------
				6. ERROR in Y.java (at line 9)
					new X().a3(h);
					        ^^
				The method a3(G) is ambiguous for the type X
				----------
				7. ERROR in Y.java (at line 10)
					new X().a3(hraw);
					        ^^
				The method a3(G) is ambiguous for the type X
				----------
				8. ERROR in Y.java (at line 11)
					new X().a4(h);
					        ^^
				The method a4(G) is ambiguous for the type X
				----------
				9. ERROR in Y.java (at line 12)
					new X().a4(hraw);
					        ^^
				The method a4(G) is ambiguous for the type X
				----------
				10. ERROR in Y.java (at line 13)
					new X().a5(h);
					        ^^
				The method a5(H<C>) is ambiguous for the type X
				----------
				11. ERROR in Y.java (at line 14)
					new X().a5(hraw);
					        ^^
				The method a5(H) is ambiguous for the type X
				----------
				12. ERROR in Y.java (at line 15)
					new X().a6(h);
					        ^^
				The method a6(H<C>) is ambiguous for the type X
				----------
				13. ERROR in Y.java (at line 16)
					new X().a6(hraw);
					        ^^
				The method a6(H) is ambiguous for the type X
				----------
				"""
			: """
				----------
				1. WARNING in Y.java (at line 4)
					H hraw = null;
					^
				H is a raw type. References to generic type H<T3> should be parameterized
				----------
				2. ERROR in Y.java (at line 5)
					new X().a(h);
					        ^
				The method a(G) is ambiguous for the type X
				----------
				3. ERROR in Y.java (at line 6)
					new X().a(hraw);
					        ^
				The method a(G) is ambiguous for the type X
				----------
				4. ERROR in Y.java (at line 7)
					new X().a2(h);
					        ^^
				The method a2(G) is ambiguous for the type X
				----------
				5. ERROR in Y.java (at line 8)
					new X().a2(hraw);
					        ^^
				The method a2(G) is ambiguous for the type X
				----------
				6. ERROR in Y.java (at line 13)
					new X().a5(h);
					        ^^
				The method a5(H<C>) is ambiguous for the type X
				----------
				7. ERROR in Y.java (at line 14)
					new X().a5(hraw);
					        ^^
				The method a5(H) is ambiguous for the type X
				----------
				"""),
			null,
			false
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=262209
	public void test014i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I<T> {}
					interface J<T> extends I<T> {}
					
					class X {
						void a(G x) {}
						<T extends C, S extends F<T>> void a(S x) {}
					
						void b(G x) {}
						void b(F x) {}
					
						void c(G x) {}
						void c(F<?> x) {}
					
						void d(G x) {}
						void d(F<C> x) {}
					
						void e(G x) {}
						<T extends C> void e(F<T> x) {}
					
						void f(G x) {}
						<S extends F> void f(S x) {}
					
						void g(G x) {}
						<S extends F & J<S>> void g(S x) {}
					
						<T extends G> void a2(T x) {}
						<T extends C, S extends F<T>> void a2(S x) {}
					
						<T extends G> void b2(T x) {}
						void b2(F x) {}
					
						<T extends G> void c2(T x) {}
						void c2(F<?> x) {}
					
						<T extends G> void d2(T x) {}
						void d2(F<C> x) {}
					
						<T extends G> void e2(T x) {}
						<T extends C> void e2(F<T> x) {}
					
						<T extends G> void f2(T x) {}
						<S extends F & J> void f2(S x) {}
					
						<T extends G> void g2(T x) {}
						<S extends F & J<S>> void g2(S x) {}
					
						void test() {
							X x = new X();
							H<C> h = null;
							H hraw = null;
					
							x.a(h);
							x.a(hraw);
					
							x.b(h);
							x.b(hraw);
					
							x.c(h);
							x.c(hraw);
					
							x.d(h);
							x.d(hraw);
					
							x.e(h);
							x.e(hraw);
					
							x.f(h);
							x.f(hraw);
					
							x.g(h);
							x.g(hraw);
					
							x.a2(h);
							x.a2(hraw);
					
							x.b2(h);\t
							x.b2(hraw);
					
							x.c2(h);
							x.c2(hraw);
					
							x.d2(h);
							x.d2(hraw);
					
							x.e2(h);
							x.e2(hraw);
					
							x.f2(h);
							x.f2(hraw);
					
							x.g2(h);\t
							x.g2(hraw);
						}
					}
					
					class A {}
					class B extends A {}
					class C extends B implements I {}
					class F<T1> {}\s
					class G<T2> extends F<T2> implements J<T2> {}
					class H<T3> extends G<T3> {}
					"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	void a(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	void b(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 9)\n" +
			"	void b(F x) {}\n" +
			"	       ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 11)\n" +
			"	void c(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 14)\n" +
			"	void d(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 17)\n" +
			"	void e(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 20)\n" +
			"	void f(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 21)\n" +
			"	<S extends F> void f(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 23)\n" +
			"	void g(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 24)\n" +
			"	<S extends F & J<S>> void g(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"11. WARNING in X.java (at line 26)\n" +
			"	<T extends G> void a2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"12. WARNING in X.java (at line 29)\n" +
			"	<T extends G> void b2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"13. WARNING in X.java (at line 30)\n" +
			"	void b2(F x) {}\n" +
			"	        ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"14. WARNING in X.java (at line 32)\n" +
			"	<T extends G> void c2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"15. WARNING in X.java (at line 35)\n" +
			"	<T extends G> void d2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"16. WARNING in X.java (at line 38)\n" +
			"	<T extends G> void e2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"17. WARNING in X.java (at line 41)\n" +
			"	<T extends G> void f2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"18. WARNING in X.java (at line 42)\n" +
			"	<S extends F & J> void f2(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"19. WARNING in X.java (at line 42)\n" +
			"	<S extends F & J> void f2(S x) {}\n" +
			"	               ^\n" +
			"J is a raw type. References to generic type J<T> should be parameterized\n" +
			"----------\n" +
			"20. WARNING in X.java (at line 44)\n" +
			"	<T extends G> void g2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"21. WARNING in X.java (at line 45)\n" +
			"	<S extends F & J<S>> void g2(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"22. WARNING in X.java (at line 50)\n" +
			"	H hraw = null;\n" +
			"	^\n" +
			"H is a raw type. References to generic type H<T3> should be parameterized\n" +
			"----------\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"23. ERROR in X.java (at line 52)\n" +
			"	x.a(h);\n" +
			"	  ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n" +
			"24. ERROR in X.java (at line 53)\n" +
			"	x.a(hraw);\n" +
			"	  ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n" +
			"25. ERROR in X.java (at line 58)\n" +
			"	x.c(h);\n" +
			"	  ^\n" +
			"The method c(G) is ambiguous for the type X\n" +
			"----------\n" +
			"26. ERROR in X.java (at line 59)\n" +
			"	x.c(hraw);\n" +
			"	  ^\n" +
			"The method c(G) is ambiguous for the type X\n" +
			"----------\n" +
			"27. ERROR in X.java (at line 61)\n" +
			"	x.d(h);\n" +
			"	  ^\n" +
			"The method d(G) is ambiguous for the type X\n" +
			"----------\n" +
			"28. ERROR in X.java (at line 62)\n" +
			"	x.d(hraw);\n" +
			"	  ^\n" +
			"The method d(G) is ambiguous for the type X\n" +
			"----------\n" +
			"29. ERROR in X.java (at line 64)\n" +
			"	x.e(h);\n" +
			"	  ^\n" +
			"The method e(G) is ambiguous for the type X\n" +
			"----------\n" +
			"30. ERROR in X.java (at line 65)\n" +
			"	x.e(hraw);\n" +
			"	  ^\n" +
			"The method e(G) is ambiguous for the type X\n" +
			"----------\n" +
			"31. ERROR in X.java (at line 71)\n" +
			"	x.g(hraw);\n" +
			"	  ^\n" +
			"The method g(G) is ambiguous for the type X\n" +
			"----------\n" +
			"32. ERROR in X.java (at line 73)\n" +
			"	x.a2(h);\n" +
			"	  ^^\n" +
			"The method a2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"33. ERROR in X.java (at line 74)\n" +
			"	x.a2(hraw);\n" +
			"	  ^^\n" +
			"The method a2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"34. ERROR in X.java (at line 79)\n" +
			"	x.c2(h);\n" +
			"	  ^^\n" +
			"The method c2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"35. ERROR in X.java (at line 80)\n" +
			"	x.c2(hraw);\n" +
			"	  ^^\n" +
			"The method c2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"36. ERROR in X.java (at line 82)\n" +
			"	x.d2(h);\n" +
			"	  ^^\n" +
			"The method d2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"37. ERROR in X.java (at line 83)\n" +
			"	x.d2(hraw);\n" +
			"	  ^^\n" +
			"The method d2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"38. ERROR in X.java (at line 85)\n" +
			"	x.e2(h);\n" +
			"	  ^^\n" +
			"The method e2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"39. ERROR in X.java (at line 86)\n" +
			"	x.e2(hraw);\n" +
			"	  ^^\n" +
			"The method e2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"40. ERROR in X.java (at line 92)\n" +
			"	x.g2(hraw);\n" +
			"	  ^^\n" +
			"The method g2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"41. WARNING in X.java (at line 98)\n"
			: // fewer ambiguities in 1.8
				"23. ERROR in X.java (at line 61)\n" +
				"	x.d(h);\n" +
				"	  ^\n" +
				"The method d(G) is ambiguous for the type X\n" +
				"----------\n" +
				"24. ERROR in X.java (at line 62)\n" +
				"	x.d(hraw);\n" +
				"	  ^\n" +
				"The method d(G) is ambiguous for the type X\n" +
				"----------\n" +
				"25. ERROR in X.java (at line 64)\n" +
				"	x.e(h);\n" +
				"	  ^\n" +
				"The method e(G) is ambiguous for the type X\n" +
				"----------\n" +
				"26. ERROR in X.java (at line 65)\n" +
				"	x.e(hraw);\n" +
				"	  ^\n" +
				"The method e(G) is ambiguous for the type X\n" +
				"----------\n" +
				"27. ERROR in X.java (at line 82)\n" +
				"	x.d2(h);\n" +
				"	  ^^\n" +
				"The method d2(H<C>) is ambiguous for the type X\n" +
				"----------\n" +
				"28. ERROR in X.java (at line 83)\n" +
				"	x.d2(hraw);\n" +
				"	  ^^\n" +
				"The method d2(H) is ambiguous for the type X\n" +
				"----------\n" +
				"29. ERROR in X.java (at line 85)\n" +
				"	x.e2(h);\n" +
				"	  ^^\n" +
				"The method e2(H<C>) is ambiguous for the type X\n" +
				"----------\n" +
				"30. ERROR in X.java (at line 86)\n" +
				"	x.e2(hraw);\n" +
				"	  ^^\n" +
				"The method e2(H) is ambiguous for the type X\n" +
				"----------\n" +
				"31. WARNING in X.java (at line 98)\n"
			) +
			"	class C extends B implements I {}\n" +
			"	                             ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void test015() {
		this.runConformTest(
			new String[] {
				"E.java",
				"""
					public class E {
						public static void main(String[] s) {
							IJ ij = new K();
							try { ij.m(); } catch(E11 e) {}
						}
					}
					interface I { void m() throws E1; }
					interface J { void m() throws E11; }
					interface IJ extends I, J {}
					class K implements IJ { public void m() {} }
					class E1 extends Exception { static final long serialVersionUID = 1; }
					class E11 extends E1 { static final long serialVersionUID = 2; }
					class E2 extends Exception { static final long serialVersionUID = 3; }"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void _test016() {
		this.runConformTest(
			new String[] {
				"E.java",
				"""
					public class E {
						public static void main(String[] s) {
							IJ ij = new K();
							try { ij.m(); } catch(E11 e) {}
						}
					}
					interface I { void m() throws E1; }
					interface J { void m() throws E2, E11; }
					interface IJ extends I, J {}
					class K implements IJ { public void m() {} }
					class E1 extends Exception { static final long serialVersionUID = 1; }
					class E11 extends E1 { static final long serialVersionUID = 2; }
					class E2 extends Exception { static final long serialVersionUID = 3; }"""
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void _test016a() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"""
					public class E {
						public static void main(String[] s) {
							IJ ij = new K();
							ij.m();
							try { ij.m(); } catch(E2 e) {}
						}
					}
					interface I { void m() throws E1; }
					interface J { void m() throws E2, E11; }
					interface IJ extends I, J {}
					class K implements IJ { public void m() {} }
					class E1 extends Exception { static final long serialVersionUID = 1; }
					class E11 extends E1 { static final long serialVersionUID = 2; }
					class E2 extends Exception { static final long serialVersionUID = 3; }"""
			},
			"""
				----------
				1. ERROR in E.java (at line 4)\r
					ij.m();\r
					^^^^^^
				Unhandled exception type E11
				----------
				2. ERROR in E.java (at line 5)\r
					try { ij.m(); } catch(E2 e) {}\r
					      ^^^^^^
				Unhandled exception type E11
				----------
				3. ERROR in E.java (at line 5)\r
					try { ij.m(); } catch(E2 e) {}\r
					                      ^^
				Unreachable catch block for E2. This exception is never thrown from the try statement body
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149893
	public void test017() {
		this.runConformTest(
			new String[] {
				"AbstractFilter.java",
				"""
					import java.util.*;
					public class AbstractFilter<T> implements IFilter<T> {
						public final <E extends T> boolean selekt(E obj) { return true; }
						public final <E extends T> List<E> filter(List<E> elements) {
							if ((elements == null) || (elements.size() == 0)) return elements;
							List<E> okElements = new ArrayList<E>(elements.size());
							for (E obj : elements) {
								if (selekt(obj)) okElements.add(obj);
							}
							return okElements;\
						}
					}
					interface IFilter<T> {
						<E extends T> boolean selekt(E obj);
						<E extends T> List<E> filter(List<E> elements);
					}"""
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	public void test018() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
			return;
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				class X<T extends Object> {
				  public static <U extends Object> X<U> make(Class<U> clazz) {
				    System.out.print(false);
				    return new X<U>();
				  }
				}
				public class Y<V extends String> extends X<V> {
				  public static <W extends String> Y<W> make(Class<W> clazz) {
				    System.out.print(true);
				    return new Y<W>();
				  }
				  public static void main(String[] args) throws Exception {
				    Y.make(String.class);
				  }
				}"""
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// in fact, <W extends String> Y<W> make(Class<W> clazz) is the most
	// specific method according to JLS 15.12.2.5
	public void test019() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;

	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				class X<T extends Object> {
				  public static <U extends Object> X<U> make(Class<U> clazz) {
				    System.out.print(false);
				    return new X<U>();
				  }
				}
				public class Y<V extends String> extends X<V> {
				  public static <W extends String> Y<W> make(Class<W> clazz) {
				    System.out.print(true);
				    return new Y<W>();
				  }
				  public static void main(String[] args) throws Exception {
				    Y.make(getClazz());
				  }
				  public static Class getClazz() {
				    return String.class;
				  }
				}"""
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	public void test020() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				class X<T extends Object> {
				  public static <U extends Object> X<U> make(Class<U> clazz) {
				    System.out.print(true);
				    return new X<U>();
				  }
				}
				public class Y<V extends String> extends X<V> {
				  public static <W extends String> Y<W> make(Class<W> clazz) {
				    System.out.print(false);
				    return new Y<W>();
				  }
				  public static void main(String[] args) throws Exception {
				    Y.make(getClazz().newInstance().getClass());
				  }
				  public static Class getClazz() {
				    return String.class;
				  }
				}"""
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// variant: having both methods in the same class should not change anything
	public void test021() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"""
			----------
			1. WARNING in Y.java (at line 3)
				public class Y<V extends String> extends X<V> {
				                         ^^^^^^
			The type parameter V should not be bounded by the final type String. Final types cannot be further extended
			----------
			2. WARNING in Y.java (at line 4)
				public static <W extends String> Y<W> make(Class<W> clazz) {
				                         ^^^^^^
			The type parameter W should not be bounded by the final type String. Final types cannot be further extended
			----------
			3. WARNING in Y.java (at line 4)
				public static <W extends String> Y<W> make(Class<W> clazz) {
				                                      ^^^^^^^^^^^^^^^^^^^^
			Erasure of method make(Class<W>) is the same as another method in type Y<V>
			----------
			4. WARNING in Y.java (at line 8)
				public static <U extends Object> X<U> make(Class<U> clazz) {
				                                      ^^^^^^^^^^^^^^^^^^^^
			Erasure of method make(Class<U>) is the same as another method in type Y<V>
			----------
			5. WARNING in Y.java (at line 13)
				Y.make(getClazz());
				^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation make(Class) of the generic method make(Class<W>) of type Y
			----------
			6. WARNING in Y.java (at line 13)
				Y.make(getClazz());
				       ^^^^^^^^^^
			Type safety: The expression of type Class needs unchecked conversion to conform to Class<String>
			----------
			7. WARNING in Y.java (at line 15)
				public static Class getClazz() {
				              ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			""":
			"""
				----------
				1. WARNING in Y.java (at line 3)
					public class Y<V extends String> extends X<V> {
					                         ^^^^^^
				The type parameter V should not be bounded by the final type String. Final types cannot be further extended
				----------
				2. WARNING in Y.java (at line 4)
					public static <W extends String> Y<W> make(Class<W> clazz) {
					                         ^^^^^^
				The type parameter W should not be bounded by the final type String. Final types cannot be further extended
				----------
				3. ERROR in Y.java (at line 4)
					public static <W extends String> Y<W> make(Class<W> clazz) {
					                                      ^^^^^^^^^^^^^^^^^^^^
				Erasure of method make(Class<W>) is the same as another method in type Y<V>
				----------
				4. ERROR in Y.java (at line 8)
					public static <U extends Object> X<U> make(Class<U> clazz) {
					                                      ^^^^^^^^^^^^^^^^^^^^
				Erasure of method make(Class<U>) is the same as another method in type Y<V>
				----------
				5. WARNING in Y.java (at line 13)
					Y.make(getClazz());
					^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation make(Class) of the generic method make(Class<W>) of type Y
				----------
				6. WARNING in Y.java (at line 13)
					Y.make(getClazz());
					       ^^^^^^^^^^
				Type safety: The expression of type Class needs unchecked conversion to conform to Class<String>
				----------
				7. WARNING in Y.java (at line 15)
					public static Class getClazz() {
					              ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""";
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"""
					class X<T extends Object> {
					}
					public class Y<V extends String> extends X<V> {
					  public static <W extends String> Y<W> make(Class<W> clazz) {
					    System.out.print(true);
					    return new Y<W>();
					  }
					  public static <U extends Object> X<U> make(Class<U> clazz) {
					    System.out.print(false);
					    return new X<U>();
					  }
					  public static void main(String[] args) throws Exception {
					    Y.make(getClazz());
					  }
					  public static Class getClazz() {
					    return String.class;
					  }
					}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:8: name clash: <U>make(Class<U>) and <W>make(Class<W>) have the same erasure
  public static <U extends Object> X<U> make(Class<U> clazz) {
                                        ^
  where U,W are type-variables:
    U extends Object declared in method <U>make(Class<U>)
    W extends String declared in method <W>make(Class<W>)
X.java:13: warning: [unchecked] unchecked conversion
    Y.make(getClazz());
                   ^
  required: Class<W#1>
  found:    Class
  where W#1,W#2 are type-variables:
    W#1 extends String declared in method <W#2>make(Class<W#2>)
    W#2 extends String declared in method <W#2>make(Class<W#2>)
X.java:13: warning: [unchecked] unchecked method invocation: method make in class Y is applied to given types
    Y.make(getClazz());
          ^
  required: Class<W>
  found: Class
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
1 error
2 warnings
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// variant: using instances triggers raw methods, which are ambiguous
	public void test022() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"""
			----------
			1. WARNING in X.java (at line 3)
				class Y<V extends String> extends X<V> {
				                  ^^^^^^
			The type parameter V should not be bounded by the final type String. Final types cannot be further extended
			----------
			2. WARNING in X.java (at line 4)
				public <W extends String> Y<W> make(Class<W> clazz) {
				                  ^^^^^^
			The type parameter W should not be bounded by the final type String. Final types cannot be further extended
			----------
			3. WARNING in X.java (at line 4)
				public <W extends String> Y<W> make(Class<W> clazz) {
				                               ^^^^^^^^^^^^^^^^^^^^
			Erasure of method make(Class<W>) is the same as another method in type Y<V>
			----------
			4. WARNING in X.java (at line 7)
				public <U extends Object> X<U> make(Class<U> clazz) {
				                               ^^^^^^^^^^^^^^^^^^^^
			Erasure of method make(Class<U>) is the same as another method in type Y<V>
			----------
			5. WARNING in X.java (at line 12)
				Y y = new Y();
				^
			Y is a raw type. References to generic type Y<V> should be parameterized
			----------
			6. WARNING in X.java (at line 12)
				Y y = new Y();
				          ^
			Y is a raw type. References to generic type Y<V> should be parameterized
			----------
			7. ERROR in X.java (at line 13)
				y.make(String.class);
				  ^^^^
			The method make(Class) is ambiguous for the type Y
			----------
			8. ERROR in X.java (at line 14)
				y.make(getClazz());
				  ^^^^
			The method make(Class) is ambiguous for the type Y
			----------
			9. ERROR in X.java (at line 15)
				y.make(getClazz().newInstance().getClass());
				  ^^^^
			The method make(Class) is ambiguous for the type Y
			----------
			10. WARNING in X.java (at line 17)
				public static Class getClazz() {
				              ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			""":
			"""
				----------
				1. WARNING in X.java (at line 3)
					class Y<V extends String> extends X<V> {
					                  ^^^^^^
				The type parameter V should not be bounded by the final type String. Final types cannot be further extended
				----------
				2. WARNING in X.java (at line 4)
					public <W extends String> Y<W> make(Class<W> clazz) {
					                  ^^^^^^
				The type parameter W should not be bounded by the final type String. Final types cannot be further extended
				----------
				3. ERROR in X.java (at line 4)
					public <W extends String> Y<W> make(Class<W> clazz) {
					                               ^^^^^^^^^^^^^^^^^^^^
				Erasure of method make(Class<W>) is the same as another method in type Y<V>
				----------
				4. ERROR in X.java (at line 7)
					public <U extends Object> X<U> make(Class<U> clazz) {
					                               ^^^^^^^^^^^^^^^^^^^^
				Erasure of method make(Class<U>) is the same as another method in type Y<V>
				----------
				5. WARNING in X.java (at line 12)
					Y y = new Y();
					^
				Y is a raw type. References to generic type Y<V> should be parameterized
				----------
				6. WARNING in X.java (at line 12)
					Y y = new Y();
					          ^
				Y is a raw type. References to generic type Y<V> should be parameterized
				----------
				7. WARNING in X.java (at line 13)
					y.make(String.class);
					^^^^^^^^^^^^^^^^^^^^
				Type safety: The method make(Class) belongs to the raw type Y. References to generic type Y<V> should be parameterized
				----------
				8. WARNING in X.java (at line 14)
					y.make(getClazz());
					^^^^^^^^^^^^^^^^^^
				Type safety: The method make(Class) belongs to the raw type Y. References to generic type Y<V> should be parameterized
				----------
				9. WARNING in X.java (at line 15)
					y.make(getClazz().newInstance().getClass());
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method make(Class) belongs to the raw type Y. References to generic type Y<V> should be parameterized
				----------
				10. WARNING in X.java (at line 17)
					public static Class getClazz() {
					              ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T extends Object> {
					}
					class Y<V extends String> extends X<V> {
					  public <W extends String> Y<W> make(Class<W> clazz) {
					    return new Y<W>();
					  }
					  public <U extends Object> X<U> make(Class<U> clazz) {
					    return new X<U>();
					  }
					  @SuppressWarnings({"deprecation"})
					  public static void main(String[] args) throws Exception {
					    Y y = new Y();
					    y.make(String.class);
					    y.make(getClazz());
					    y.make(getClazz().newInstance().getClass());
					  }
					  public static Class getClazz() {
					    return String.class;
					  }
					}"""
			},
			expectedCompilerLog
		);
/* javac 7
X.java:7: name clash: <U>make(Class<U>) and <W>make(Class<W>) have the same erasure
  public <U extends Object> X<U> make(Class<U> clazz) {
                                 ^
  where U,W are type-variables:
    U extends Object declared in method <U>make(Class<U>)
    W extends String declared in method <W>make(Class<W>)
X.java:12: warning: [unchecked] unchecked call to <W>make(Class<W>) as a member of the raw type Y
    y.make(String.class);
          ^
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
X.java:13: warning: [unchecked] unchecked call to <W>make(Class<W>) as a member of the raw type Y
    y.make(getClazz());
          ^
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
X.java:14: warning: [unchecked] unchecked call to <W>make(Class<W>) as a member of the raw type Y
    y.make(getClazz().newInstance().getClass());
          ^
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
1 error
3 warnings
 */
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159711
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
				  public static void staticFoo(Collection<?> p) {
				    System.out.print(1);
				  }
				  public static <T extends List<?>> void staticFoo(T p) {
				    System.out.print(2);
				  }
				  public void foo(Collection<?> p) {
				    System.out.print(1);
				  }
				  public <T extends List<?>> void foo(T p) {
				    System.out.print(2);
				  }
				  public void foo2(Collection<?> p) {
				    System.out.print(1);
				  }
				  public void foo2(List<?> p) {
				    System.out.print(2);
				  }
				  public static void main(String[] args) {
				    staticFoo(new ArrayList<String>(Arrays.asList("")));
				    new X().foo(new ArrayList<String>(Arrays.asList("")));
				    new X().foo2(new ArrayList<String>(Arrays.asList("")));
				  }
				}"""
		},
		"222");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159711
// self contained variant
public void test024() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void foo(L1<?> p) {
				    System.out.println(1);
				  }
				  public static <T extends L2<?>> void foo(T p) {
				    System.out.println(2);
				  }
				  public static void main(String[] args) {
				    foo(new L3<String>());
				  }
				}""",
			"L1.java",
			"public interface L1<T> {\n" +
			"}",
			"L2.java",
			"public interface L2<T> extends L1<T> {\n" +
			"}",
			"L3.java",
			"""
				public class L3<T> implements L2<T> {
				  public L3() {
				  }
				}""",
		},
		"2");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  J m = new Y();
				  void foo() {
				    m.foo(1.0f);
				  }
				}""",
			"I.java",
			"""
				public interface I {
				  <T extends Number> T foo(final Number p);
				}""",
			"J.java",
			"""
				public interface J extends I {
				  Float foo(final Number p);
				}""",
			"Y.java",
			"""
				public class Y implements J {
				  public Float foo(final Number p){
				    return null;
				  }
				}""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
// variant
public void test026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    (new Y()).foo(1.0f);
				  }
				}""",
			"I.java",
			"""
				public interface I {
				  <T extends Number> T foo(final Number p);
				}""",
			"J.java",
			"""
				public interface J extends I {
				  Float foo(final Number p);
				}""",
			"Y.java",
			"""
				public class Y implements J {
				  public Float foo(final Number p){
				    return null;\
				  }
				}""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
// variant
public void test027() {
	this.runNegativeTest(
		new String[] {
			"J.java",
			"""
				public interface J {
				  <T extends Number> T foo(final Number p);
				  Float foo(final Number p);
				}""",
		},
		"""
			----------
			1. ERROR in J.java (at line 2)
				<T extends Number> T foo(final Number p);
				                     ^^^^^^^^^^^^^^^^^^^
			Duplicate method foo(Number) in type J
			----------
			2. ERROR in J.java (at line 3)
				Float foo(final Number p);
				      ^^^^^^^^^^^^^^^^^^^
			Duplicate method foo(Number) in type J
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
public void test028() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"""
				interface Irrelevant {}
				interface I {
				  Object foo(Number n);
				}
				interface J extends Irrelevant, I {
				  String foo(Number n);
				}
				interface K {
				  Object foo(Number n);
				}
				public abstract class X implements J, K {
				  void foo() {
				    foo(0.0f);
				  }
				}"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - simplified
public void test029() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"""
				interface J {
				  String foo(Number n);
				}
				interface K {
				  Object foo(Number n);
				}
				public abstract class X implements J, K {
				  void foo() {
				    foo(0.0f);
				  }
				}"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - same return type
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface J {
				  Object foo(Number n);
				}
				interface K {
				  Object foo(Number n);
				}
				public abstract class X implements J, K {
				  void foo() {
				    foo(0.0f);
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant
public void test031() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"""
				interface Irrelevant {}
				interface I {
				  Object foo(Number n);
				}
				interface J extends Irrelevant, I {
				  String foo(Number n);
				}
				interface K {
				  Object foo(Number n);
				}
				public abstract class X implements Irrelevant, I, J, K {
				  void foo() {
				    foo(0.0f);
				  }
				}"""
		});
}
// tests 32-34 were moved to MethodVerifyTest 134-140

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - the inheriting class implements foo
public void test035() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  Object foo(Number n);
				}
				abstract class J {
				  abstract String foo(Number n);
				}
				public class X extends J implements I {
				  void bar() {
				    foo(0.0f);
				  }
				  public String foo(Number n) {
				    return null;
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - extending instead of implementing
public void test037() {
	this.runConformTest(
 		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				interface I {
				  Object foo(Number n);
				}
				abstract class J {
				  abstract String foo(Number n);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    foo(0.0f);
				  }
				}"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - no promotion of parameter from float to Number
public void test038() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"""
				interface I {
				  Object foo(float f);
				}
				abstract class J {
				  public abstract String foo(float f);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    foo(0.0f);
				  }
				}"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - an explicit cast solves the issue
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  Object foo(float f);
				}
				abstract class J {
				  public abstract String foo(float f);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    String s = ((J) this).foo(0.0f);
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - an explicit cast solves the issue
public void test040() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  Object foo(float f);
				}
				abstract class J {
				  public abstract String foo(float f);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    Object o = ((I) this).foo(0.0f);
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - connecting return types
public void test041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  Object foo(float f);
				}
				abstract class J {
				  public abstract String foo(float f);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    String s = ((I) this).foo(0.0f);
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				String s = ((I) this).foo(0.0f);
				           ^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Object to String
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - a further inheriting class implements String foo
public void test042() {
	this.runConformTest(
 		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				interface I {
				  Object foo(float f);
				}
				abstract class J {
				  public abstract String foo(float f);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    foo(0.0f);
				  }
				}
				class Z extends X {
				  @Override\
				  public String foo(float f) {
				    return null;
				  }
				}"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - a further inheriting class implements Object foo
public void test043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  Object foo(float f);
				}
				abstract class J {
				  public abstract String foo(float f);
				}
				public abstract class X extends J implements I {
				  void bar() {
				    foo(0.0f);
				  }
				}
				class Z extends X {
				  @Override
				  public Object foo(float f) {
				    return null;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				public Object foo(float f) {
				       ^^^^^^
			The return type is incompatible with J.foo(float)
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163370
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<E> {}
				class Y<E> {}
				public class X<E extends Y<E>> implements I<E> {
				  public static <E extends Y<E>> X<E> bar(X<E> s) {
				    return null;
				  }
				  public static <E extends Y<E>> X<E> bar(I<E> c) {
				    return null;
				  }
				  public static <E extends Y<E>> X<E> foo(X<E> s) {
				    X<E> result = bar(s);
				    return result;
				  }
				}"""
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165620
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				abstract class Y<T> implements I<T> {
				}
				interface I<T> {\s
				}
				interface J<T> {
				}
				class X {
				  public static <V extends J<? super V>> V foo(final I<V> a)
				  {
				    return null;
				  }
				  public static <V extends J<? super V>> V foo(final Y<V> a)
				  {
				    return null;
				  }
				  public static <V extends J<? super V>> void test(final Y<V> a)
				  {
				    foo(a);
				  }
				}"""
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163370
// variant
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				abstract class Y<T, U> implements I<T, U> {
				}
				interface I<T, U> {\s
				}
				interface J<T, U> {
				}
				class X {
				  public static <V extends J<V, W>, W extends J<V, W>> V foo(final I<V, W> a)
				  {
				    return null;
				  }
				  public static <V extends J<V, W>, W extends J<V, W>> V foo(final Y<V, W> a)
				  {
				    return null;
				  }
				  public static <V extends J<V, W>, W extends J<V, W>> void test(final Y<V, W> a)
				  {
				    foo(a);
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
public void test047() {
	this.runNegativeTest(
 		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"""
				public class X<T extends I & J> {
				  void foo(T t) {
				  }
				}
				interface I {
				  public int method();
				}
				interface J {
				  public boolean method();
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X<T extends I & J> {
				               ^
			The return types are incompatible for the inherited methods I.method(), J.method()
			----------
			""",
		// javac options
	  	JavacTestOptions.JavacHasABug.JavacBug5061359 /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
// Variant: javac complains as well if we attempt to use method, but noone
// complains upon bar or CONSTANT.
public void test048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T extends I & J> {
				  void foo(T t) {
				    t.method();
				    t.bar();
				    if (t.CONSTANT > 0);
				  }
				}
				interface I {
				  public int method();
				  void bar();
				}
				interface J {
				  public boolean method();
				  static final int CONSTANT = 0;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X<T extends I & J> {
				               ^
			The return types are incompatible for the inherited methods I.method(), J.method()
			----------
			2. ERROR in X.java (at line 3)
				t.method();
				  ^^^^^^
			The method method() is ambiguous for the type T
			----------
			3. WARNING in X.java (at line 5)
				if (t.CONSTANT > 0);
				      ^^^^^^^^
			The static field J.CONSTANT should be accessed in a static way
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
// can't implement both interfaces though
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				  public int method();
				}
				interface J {
				  public boolean method();
				}
				class X implements I, J {
				  public int method() {
				    return 0;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				public int method() {
				       ^^^
			The return type is incompatible with J.method()
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
// variant: secure the legal case
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T extends I & J> {
				  void foo(T t) {
				  }
				}
				interface I {
				  public int method();
				}
				interface J {
				  public int method();
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
public void test051() {
	this.runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
				null : JavacTestOptions.EclipseHasABug.EclipseBug427719,
		new String[] { /* test files */
			"X.java",
			"""
				interface I<T> {
				}
				class Y {
				  void bar(I<?> x) {
				  }
				}
				public class X extends Y {
				  void foo() {
				    bar(new Z());
				  }
				  void bar(Z x) {
				  }
				  private static final class Z implements I {
				  }
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in X.java (at line 9)
				bar(new Z());
				^^^
			The method bar(X.Z) is ambiguous for the type X
			----------
			2. WARNING in X.java (at line 13)
				private static final class Z implements I {
				                                        ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			"""
		: this.complianceLevel < ClassFileConstants.JDK11 ?
			"""
				----------
				1. WARNING in X.java (at line 9)
					bar(new Z());
					    ^^^^^^^
				Access to enclosing constructor X.Z() is emulated by a synthetic accessor method
				----------
				2. WARNING in X.java (at line 13)
					private static final class Z implements I {
					                                        ^
				I is a raw type. References to generic type I<T> should be parameterized
				----------
				"""
			:
				"""
					----------
					1. WARNING in X.java (at line 13)
						private static final class Z implements I {
						                                        ^
					I is a raw type. References to generic type I<T> should be parameterized
					----------
					"""));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void test052() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T> {
				}
				class Y {
				  void bar(I<?> x) {
				  }
				}
				public class X extends Y {
				  void foo() {
				    bar(new Z());
				  }
				  void bar(Z x) {
				  }
				  private static final class Z implements I<String> {
				  }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void test053() {
	this.runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
				null : JavacTestOptions.EclipseHasABug.EclipseBug427719,
		new String[] { /* test files */
			"X.java",
			"""
				interface I<T> {
				}
				class Y {
				  void bar(I<?> x) {
				  }
				}
				public class X extends Y {
				  void foo() {
				    bar(new Z(){});
				  }
				  void bar(Z x) {
				  }
				  private static class Z implements I {
				  }
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in X.java (at line 9)
				bar(new Z(){});
				^^^
			The method bar(X.Z) is ambiguous for the type X
			----------
			2. WARNING in X.java (at line 13)
				private static class Z implements I {
				                                  ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			"""
		:  this.complianceLevel < ClassFileConstants.JDK11 ?
			"""
				----------
				1. WARNING in X.java (at line 9)
					bar(new Z(){});
					        ^^^
				Access to enclosing constructor X.Z() is emulated by a synthetic accessor method
				----------
				2. WARNING in X.java (at line 13)
					private static class Z implements I {
					                                  ^
				I is a raw type. References to generic type I<T> should be parameterized
				----------
				""" :
				"""
					----------
					1. WARNING in X.java (at line 13)
						private static class Z implements I {
						                                  ^
					I is a raw type. References to generic type I<T> should be parameterized
					----------
					"""));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void test054() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void bar(Z x) {
				    System.out.println("bar(Z)");
				  }
				  void bar(I<?> x) {
				    System.out.println("bar(I)");
				  }
				  public static void main(String args[]) {
				    (new X()).bar(new Z());
				  }
				}
				interface I<T> {}
				class Z implements I<Object> {}"""
		},
		"bar(Z)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void _test055() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<T> {}
				class X {
				  void bar(Z x) {
				    System.out.println("bar(Z)");
				  }
				  void bar(I<?> x) {
				    System.out.println("bar(I)");
				  }
				  public static void main(String args[]) {
				    (new X()).bar(new Z());
				  }
				}
				class Z implements I {}"""
		},
		"ERR");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184190
public void test056() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				  void bar(X x) {
				    ZA z = ZA.foo(x);
				    z.toString();
				  }
				}
				class Y<T> {
				  public static <U> Y<U> foo(X<U> x) {
				    return null;
				  }
				}
				class YA<T extends A> extends Y<T> {
				  public static <U extends A> YA<U> foo(X<U> x) {
				    return (YA<U>) Y.foo(x);
				  }
				}
				class ZA<T extends B> extends YA<T> {
				  public static <U extends B> ZA<U> foo(X<U> x) {
				    return (ZA<U>) Y.foo(x);
				  }
				}
				abstract class A  {
				}
				abstract class B extends A {
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186382
public void test057() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
					@Override <T4, G4 extends I<T4>> T4 foo(G4 g) { return super.foo(g); }
				}
				class Y extends Z {
					@Override <T3, G3 extends I<T3>> T3 foo(G3 g) { return super.foo(g); }
				}
				class Z {
					<T2, G2 extends I<T2>> T2 foo(G2 g) { return null; }
				}
				interface I<T1> {}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188741
public void test058() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends A {
					void x(G g) { System.out.print(1); }
					void x(G g, G g2) { System.out.print(1); }
					public static void main(String[] s) {
						H h = new H();
						new X().x(h);
						new X().x(h, h);
					}
				}
				class A<T> {
					void x(T t) { System.out.print(2); }
					<U> void x(T t, U u) { System.out.print(2); }
				}
				class F<T> {}
				class G<T> extends F<T> {}
				class H<T> extends G<T> {}"""
		},
		"11");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188741
public void test058a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends java.util.ArrayList {
					private static final long serialVersionUID = 1L;
					public void add(Comparable o) {}
					public void test() { add("hello"); }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188960
public void test059() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X<T> {
					X() {}
					X(String s) {}
					X(T t) {}
					void foo(String s) {}
					void foo(T t) {}
				}
				class NoErrorSubclass extends X<String> {}
				class StringOnlySubClass extends X<String> {
					StringOnlySubClass(String s) { super(s); }
					@Override void foo(String s) { super.foo(s); }
				}
				class Test {
					Object o = new X<String>("xyz");
					void test(X<String> x) { x.foo("xyz"); }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				class NoErrorSubclass extends X<String> {}
				      ^^^^^^^^^^^^^^^
			Duplicate methods named foo with the parameters (T) and (String) are defined by the type X<String>
			----------
			2. ERROR in X.java (at line 10)
				StringOnlySubClass(String s) { super(s); }
				                               ^^^^^^^^^
			The constructor X<String>(String) is ambiguous
			----------
			3. ERROR in X.java (at line 11)
				@Override void foo(String s) { super.foo(s); }
				                                     ^^^
			The method foo(String) is ambiguous for the type X<String>
			----------
			4. ERROR in X.java (at line 14)
				Object o = new X<String>("xyz");
				           ^^^^^^^^^^^^^^^^^^^^
			The constructor X<String>(String) is ambiguous
			----------
			5. ERROR in X.java (at line 15)
				void test(X<String> x) { x.foo("xyz"); }
				                           ^^^
			The method foo(String) is ambiguous for the type X<String>
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=191029
public void test059a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.TreeMap;
				class X {
					void test(TreeMap<String, Object> tm) {
						TreeMap copy = new TreeMap(tm);
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				TreeMap copy = new TreeMap(tm);
				^^^^^^^
			TreeMap is a raw type. References to generic type TreeMap<K,V> should be parameterized
			----------
			2. WARNING in X.java (at line 4)
				TreeMap copy = new TreeMap(tm);
				               ^^^^^^^^^^^^^^^
			Type safety: The constructor TreeMap(SortedMap) belongs to the raw type TreeMap. References to generic type TreeMap<K,V> should be parameterized
			----------
			3. WARNING in X.java (at line 4)
				TreeMap copy = new TreeMap(tm);
				                   ^^^^^^^
			TreeMap is a raw type. References to generic type TreeMap<K,V> should be parameterized
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=189933
public void test060() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public void bar(K<T, Object> p) {
						new Y(p);
						new Y((J<T, Object>) p);
						new Y((I<T, Object>) p);
					}
				}
				class Y<T, U> {
					Y(I<? extends T, ? extends U> p) {}
					Y(J<T, ? extends U> p) {}
				}
				interface I<T, U> {}
				interface J<T, U> extends I<T, U> {}
				interface K<T, U> extends I<T, U>, J<T, U> {}"""
		},
		""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=189933
// variant
public void test061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public void bar(K<T, Object> p) {
						new Y(p);
						new Y((J<T, Object>) p);
						new Y((I<T, Object>) p);
					}
				}
				class Y<T, U> {
					Y(I<? extends T, ? extends U> p) {}
					Y(J<T, ? extends U> p) {}
				}
				interface I<T, U> {}
				interface J<T, U> {}
				interface K<T, U> extends I<T, U>, J<T, U> {}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				new Y(p);
				^^^^^^^^
			The constructor Y(I) is ambiguous
			----------
			2. WARNING in X.java (at line 3)
				new Y(p);
				    ^
			Y is a raw type. References to generic type Y<T,U> should be parameterized
			----------
			3. WARNING in X.java (at line 4)
				new Y((J<T, Object>) p);
				^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The constructor Y(J) belongs to the raw type Y. References to generic type Y<T,U> should be parameterized
			----------
			4. WARNING in X.java (at line 4)
				new Y((J<T, Object>) p);
				    ^
			Y is a raw type. References to generic type Y<T,U> should be parameterized
			----------
			5. WARNING in X.java (at line 5)
				new Y((I<T, Object>) p);
				^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The constructor Y(I) belongs to the raw type Y. References to generic type Y<T,U> should be parameterized
			----------
			6. WARNING in X.java (at line 5)
				new Y((I<T, Object>) p);
				    ^
			Y is a raw type. References to generic type Y<T,U> should be parameterized
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193265
public void test062() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				enum E implements I {
					F;
				}
				interface I {}
				interface Spec {
					<T1 extends Enum<T1> & I> void method(T1 t);
				}
				abstract class X implements Spec {
					public <T2 extends Enum<T2> & I> void method(T2 t) {}
					void test() { method(E.F); }
				}"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196254
public void test063() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				interface I<R> {}
				class X<T extends I> {
					void method(X<?> that) {}
				}
				class Y<T extends I> extends X<T> {
					@Override void method(X<? extends I> that) { System.out.print(1); }
				}
				public class Test {
					public static void main(String[] args) { new Y().method((X) null); }
				}"""
		},
		"1"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=198120
public void test064() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				interface I<E> {
					void x(I<? extends E> i);
				}
				public abstract class A implements I {
					public void x(I i) {}
				}
				class B extends A {
					void y(A a) { super.x(a); }
				}"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200547
public void test065() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public abstract class A {
					abstract <T extends Number & Comparable<T>> void m(T x);
				}
				class B extends A {
					@Override <T extends Number & Comparable<T>> void m(T x) {}
				}"""
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=214558
public void test066() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				import java.util.*;
				public class A {
					void foo(Collection<Object[]> c) {}
					void foo(Collection<Object[]> c, Object o) {}
					public static void main(String[] args) {
						new B().foo(new ArrayList());
						new B().foo(new ArrayList(), args[0]);
					}
				}
				class B extends A {
					void foo(ArrayList a) {}
					void foo(ArrayList a, Object o) {}
				}"""
		},
		"""
			----------
			1. ERROR in A.java (at line 6)
				new B().foo(new ArrayList());
				        ^^^
			The method foo(ArrayList) is ambiguous for the type B
			----------
			2. WARNING in A.java (at line 6)
				new B().foo(new ArrayList());
				                ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			3. ERROR in A.java (at line 7)
				new B().foo(new ArrayList(), args[0]);
				        ^^^
			The method foo(ArrayList, Object) is ambiguous for the type B
			----------
			4. WARNING in A.java (at line 7)
				new B().foo(new ArrayList(), args[0]);
				                ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			5. WARNING in A.java (at line 11)
				void foo(ArrayList a) {}
				         ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			6. WARNING in A.java (at line 12)
				void foo(ArrayList a, Object o) {}
				         ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=214558 - positive case
public void test067() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				import java.util.*;
				public class A {
					void foo(Collection<Object[]> c) {}
					public static void main(String[] args) {
						new B().foo(new ArrayList<Object>());
					}
				}
				class B extends A {
					void foo(ArrayList<Object> a) {System.out.print(1);}
				}"""
		},
		"1"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279
public void test068() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface A { X<? extends A> foo(); }
				interface B extends A { X<? extends B> foo(); }
				interface C extends B, A {}
				interface D extends A, B {}
				public class X<T> {
					public void bar() {
						C c = null;
						X<? extends B> c_b = c.foo();
						D d = null;
						 X<? extends B> d_b = d.foo();
					}
				}"""
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test069() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface A { X<? extends B> foo(); }
				interface B extends A { X<? extends A> foo(); }
				interface C extends B, A {}
				interface D extends A, B {}
				public class X<T> {
					void test(C c, D d) {
						X<? extends B> c_b = c.foo();
						 X<? extends B> d_b = d.foo();
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface B extends A { X<? extends A> foo(); }
				                        ^^^^^^^^^^^^^^
			The return type is incompatible with A.foo()
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test070() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface A { X<? extends A> foo(); }
				interface B { X<? extends B> foo(); }
				interface C extends B, A {}
				interface D extends A, B {}
				public class X<T> {
					public static void main(String[] args) {
						C c = null;
						X<? extends B> c_b = c.foo();
						D d = null;
						 X<? extends B> d_b = d.foo();
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				interface C extends B, A {}
				          ^
			The return types are incompatible for the inherited methods B.foo(), A.foo()
			----------
			2. ERROR in X.java (at line 4)
				interface D extends A, B {}
				          ^
			The return types are incompatible for the inherited methods A.foo(), B.foo()
			----------
			3. ERROR in X.java (at line 8)
				X<? extends B> c_b = c.foo();
				                       ^^^
			The method foo() is ambiguous for the type C
			----------
			4. ERROR in X.java (at line 10)
				X<? extends B> d_b = d.foo();
				                       ^^^
			The method foo() is ambiguous for the type D
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test071() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				interface I {
					Integer a();
					Float b();
				}
				interface J {
					Integer a();
					Double c();
				}
				abstract class X {
					public abstract Float b();
					public Double c() { return null; }
				}
				abstract class Y extends X implements I, J {
					void test() {
						Integer i = a();
						Float f = b();
						Double d = c();
					}
				}"""
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test072() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				interface I {
					Number a();
					Number b();
				}
				interface J {
					Integer a();
					Number c();
				}
				abstract class X {
					public abstract Float b();
					public Double c() { return null; }
				}
				abstract class Y extends X implements I, J {
					void test() {
						Integer i = a();
						Float f = b();
						Double d = c();
					}
				}
				abstract class Y2 extends X implements J, I {
					void test() {
						Integer i = a();
						Float f = b();
						Double d = c();
					}
				}"""
		},
		"" // javac reports 4 ambiguous errors, 2 each of a() & b() even tho the return types are sustitutable
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test073() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				interface I {
					int a();
					int b();
				}
				interface J {
					byte a();
					int c();
				}
				abstract class X {
					public abstract byte b();
					public byte c() { return 1; }
				}
				abstract class Y extends X implements I, J {
					void test() {
						byte a = a();
						byte b = b();
						byte c = c();
					}
				}
				abstract class Y2 extends X implements J, I {
					void test() {
						byte a = a();
						byte b = b();
						byte c = c();
					}
				}"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 13)
				abstract class Y extends X implements I, J {
				               ^
			The return types are incompatible for the inherited methods J.c(), X.c()
			----------
			2. ERROR in Y.java (at line 13)
				abstract class Y extends X implements I, J {
				               ^
			The return types are incompatible for the inherited methods I.b(), X.b()
			----------
			3. ERROR in Y.java (at line 13)
				abstract class Y extends X implements I, J {
				               ^
			The return types are incompatible for the inherited methods I.a(), J.a()
			----------
			4. ERROR in Y.java (at line 20)
				abstract class Y2 extends X implements J, I {
				               ^^
			The return types are incompatible for the inherited methods J.c(), X.c()
			----------
			5. ERROR in Y.java (at line 20)
				abstract class Y2 extends X implements J, I {
				               ^^
			The return types are incompatible for the inherited methods I.b(), X.b()
			----------
			6. ERROR in Y.java (at line 20)
				abstract class Y2 extends X implements J, I {
				               ^^
			The return types are incompatible for the inherited methods J.a(), I.a()
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=206930
public void test074() {
	this.runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
				null : JavacTestOptions.EclipseHasABug.EclipseBug427719,
		new String[] {
			"Y.java",
			"""
				interface I<T> {}
				class A {
					void a(I x) {}
					void b(I<?> x) {}
					void b(I<?>[] x) {}
					<U> void c(I<?> x) {}
				}
				class B extends A {}
				class C extends B implements I {
					void a(C c) {}
					void b(C c) {}
					void b(C[] c) {}
					void c(C c) {}
				}
				class D extends C {
				    void test() {
				        a(new C());
				        a(new D());
				        b(new C());
				        b(new D());
				        b(new C[0]);
				        b(new D[0]);
				        c(new C());
				        c(new D());
				    }
				}
				class A2<T> {
					void a(I x) {}
					void b(I<?> x) {}
					<U> void c(I<?> x) {}
					void d(I<T> x) {}
				}
				class B2 extends A2 {}
				class C2 extends B2 implements I {
					void a(C2 c) {}
					void b(C2 c) {}
					void c(C2 c) {}
					void d(C2 c) {}
				}
				class D2 extends C2 {
				    void test() {
				        a(new C2());
				        a(new D2());
				        b(new C2());
				        b(new D2());
				        c(new C2());
				        c(new D2());
				        d(new C2());
				        d(new D2());
				    }
				}
				public class Y {}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. WARNING in Y.java (at line 3)
				void a(I x) {}
				       ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			2. WARNING in Y.java (at line 9)
				class C extends B implements I {
				                             ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			3. ERROR in Y.java (at line 19)
				b(new C());
				^
			The method b(C) is ambiguous for the type D
			----------
			4. ERROR in Y.java (at line 20)
				b(new D());
				^
			The method b(C) is ambiguous for the type D
			----------
			5. ERROR in Y.java (at line 21)
				b(new C[0]);
				^
			The method b(C[]) is ambiguous for the type D
			----------
			6. ERROR in Y.java (at line 22)
				b(new D[0]);
				^
			The method b(C[]) is ambiguous for the type D
			----------
			7. ERROR in Y.java (at line 23)
				c(new C());
				^
			The method c(C) is ambiguous for the type D
			----------
			8. ERROR in Y.java (at line 24)
				c(new D());
				^
			The method c(C) is ambiguous for the type D
			----------
			9. WARNING in Y.java (at line 28)
				void a(I x) {}
				       ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			10. WARNING in Y.java (at line 33)
				class B2 extends A2 {}
				                 ^^
			A2 is a raw type. References to generic type A2<T> should be parameterized
			----------
			11. WARNING in Y.java (at line 34)
				class C2 extends B2 implements I {
				                               ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			"""
		: """
			----------
			1. WARNING in Y.java (at line 3)
				void a(I x) {}
				       ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			2. WARNING in Y.java (at line 9)
				class C extends B implements I {
				                             ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			3. WARNING in Y.java (at line 28)
				void a(I x) {}
				       ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			4. WARNING in Y.java (at line 33)
				class B2 extends A2 {}
				                 ^^
			A2 is a raw type. References to generic type A2<T> should be parameterized
			----------
			5. WARNING in Y.java (at line 34)
				class C2 extends B2 implements I {
				                               ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			""")
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=266421
public void test075() {
	this.runNegativeTest(
		new String[] {
			"C.java",
			"""
				abstract class A<T extends Comparable> {
					abstract int x(T val);
				}
				class B<T extends Comparable> extends A<T> {
					@Override int x(T val) { return 0; }
				}
				class C extends B<Double> {
				    int test(Double val) { return x(val); }
				}"""
		},
		"""
			----------
			1. WARNING in C.java (at line 1)
				abstract class A<T extends Comparable> {
				                           ^^^^^^^^^^
			Comparable is a raw type. References to generic type Comparable<T> should be parameterized
			----------
			2. WARNING in C.java (at line 4)
				class B<T extends Comparable> extends A<T> {
				                  ^^^^^^^^^^
			Comparable is a raw type. References to generic type Comparable<T> should be parameterized
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=268837
// See that this test case exhibits the bug 345947
public void test076() {
	String output = (this.complianceLevel == ClassFileConstants.JDK1_6)?
			"""
				----------
				1. WARNING in X.java (at line 8)
					<U> J<String> b();
					              ^^^
				Name clash: The method b() of type J<E> has the same erasure as b() of type I<E> but does not override it
				----------
				2. ERROR in X.java (at line 15)
					J<Integer> b = ints.a();
					               ^^^^^^^^
				Type mismatch: cannot convert from J<String> to J<Integer>
				----------
				3. ERROR in X.java (at line 16)
					J<Object> c = ints.a();
					              ^^^^^^^^
				Type mismatch: cannot convert from J<String> to J<Object>
				----------
				4. WARNING in X.java (at line 17)
					J d = ints.a();
					^
				J is a raw type. References to generic type J<E> should be parameterized
				----------
				5. ERROR in X.java (at line 19)
					I<Integer> f = ints.a();
					               ^^^^^^^^
				Type mismatch: cannot convert from J<String> to I<Integer>
				----------
				6. ERROR in X.java (at line 20)
					I<Object> g = ints.a();
					              ^^^^^^^^
				Type mismatch: cannot convert from J<String> to I<Object>
				----------
				7. WARNING in X.java (at line 21)
					I h = ints.a();
					^
				I is a raw type. References to generic type I<E> should be parameterized
				----------
				8. ERROR in X.java (at line 24)
					ints.b();
					     ^
				The method b() is ambiguous for the type J<Integer>
				----------
				9. ERROR in X.java (at line 25)
					J<String> a = ints.b();
					                   ^
				The method b() is ambiguous for the type J<Integer>
				----------
				10. ERROR in X.java (at line 26)
					J<Integer> b = ints.b();
					                    ^
				The method b() is ambiguous for the type J<Integer>
				----------
				11. ERROR in X.java (at line 27)
					J<Object> c = ints.b();
					                   ^
				The method b() is ambiguous for the type J<Integer>
				----------
				12. WARNING in X.java (at line 28)
					J d = ints.b();
					^
				J is a raw type. References to generic type J<E> should be parameterized
				----------
				13. ERROR in X.java (at line 28)
					J d = ints.b();
					           ^
				The method b() is ambiguous for the type J<Integer>
				----------
				14. ERROR in X.java (at line 29)
					I<String> e = ints.b();
					                   ^
				The method b() is ambiguous for the type J<Integer>
				----------
				15. ERROR in X.java (at line 30)
					I<Integer> f = ints.b();
					                    ^
				The method b() is ambiguous for the type J<Integer>
				----------
				16. ERROR in X.java (at line 31)
					I<Object> g = ints.b();
					                   ^
				The method b() is ambiguous for the type J<Integer>
				----------
				17. WARNING in X.java (at line 32)
					I h = ints.b();
					^
				I is a raw type. References to generic type I<E> should be parameterized
				----------
				18. ERROR in X.java (at line 32)
					I h = ints.b();
					           ^
				The method b() is ambiguous for the type J<Integer>
				----------
				19. WARNING in X.java (at line 39)
					J d = ints.c();
					^
				J is a raw type. References to generic type J<E> should be parameterized
				----------
				20. WARNING in X.java (at line 43)
					I h = ints.c();
					^
				I is a raw type. References to generic type I<E> should be parameterized
				----------
				""":
				"""
					----------
					1. ERROR in X.java (at line 8)
						<U> J<String> b();
						              ^^^
					Name clash: The method b() of type J<E> has the same erasure as b() of type I<E> but does not override it
					----------
					2. ERROR in X.java (at line 15)
						J<Integer> b = ints.a();
						               ^^^^^^^^
					Type mismatch: cannot convert from J<String> to J<Integer>
					----------
					3. ERROR in X.java (at line 16)
						J<Object> c = ints.a();
						              ^^^^^^^^
					Type mismatch: cannot convert from J<String> to J<Object>
					----------
					4. WARNING in X.java (at line 17)
						J d = ints.a();
						^
					J is a raw type. References to generic type J<E> should be parameterized
					----------
					5. ERROR in X.java (at line 19)
						I<Integer> f = ints.a();
						               ^^^^^^^^
					Type mismatch: cannot convert from J<String> to I<Integer>
					----------
					6. ERROR in X.java (at line 20)
						I<Object> g = ints.a();
						              ^^^^^^^^
					Type mismatch: cannot convert from J<String> to I<Object>
					----------
					7. WARNING in X.java (at line 21)
						I h = ints.a();
						^
					I is a raw type. References to generic type I<E> should be parameterized
					----------
					8. ERROR in X.java (at line 24)
						ints.b();
						     ^
					The method b() is ambiguous for the type J<Integer>
					----------
					9. ERROR in X.java (at line 25)
						J<String> a = ints.b();
						                   ^
					The method b() is ambiguous for the type J<Integer>
					----------
					10. ERROR in X.java (at line 26)
						J<Integer> b = ints.b();
						                    ^
					The method b() is ambiguous for the type J<Integer>
					----------
					11. ERROR in X.java (at line 27)
						J<Object> c = ints.b();
						                   ^
					The method b() is ambiguous for the type J<Integer>
					----------
					12. WARNING in X.java (at line 28)
						J d = ints.b();
						^
					J is a raw type. References to generic type J<E> should be parameterized
					----------
					13. ERROR in X.java (at line 28)
						J d = ints.b();
						           ^
					The method b() is ambiguous for the type J<Integer>
					----------
					14. ERROR in X.java (at line 29)
						I<String> e = ints.b();
						                   ^
					The method b() is ambiguous for the type J<Integer>
					----------
					15. ERROR in X.java (at line 30)
						I<Integer> f = ints.b();
						                    ^
					The method b() is ambiguous for the type J<Integer>
					----------
					16. ERROR in X.java (at line 31)
						I<Object> g = ints.b();
						                   ^
					The method b() is ambiguous for the type J<Integer>
					----------
					17. WARNING in X.java (at line 32)
						I h = ints.b();
						^
					I is a raw type. References to generic type I<E> should be parameterized
					----------
					18. ERROR in X.java (at line 32)
						I h = ints.b();
						           ^
					The method b() is ambiguous for the type J<Integer>
					----------
					19. WARNING in X.java (at line 39)
						J d = ints.c();
						^
					J is a raw type. References to generic type J<E> should be parameterized
					----------
					20. WARNING in X.java (at line 43)
						I h = ints.c();
						^
					I is a raw type. References to generic type I<E> should be parameterized
					----------
					""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<E> {
					I<String> a();
					I<String> b();
					<T1> I<T1> c();
				}
				interface J<E> extends I<E> {
					J<String> a();
					<U> J<String> b();
					<T2> J<T2> c();
				}
				class X {
					void a(J<Integer> ints) {
						ints.a();
						J<String> a = ints.a();
						J<Integer> b = ints.a();
						J<Object> c = ints.a();
						J d = ints.a();
						I<String> e = ints.a();
						I<Integer> f = ints.a();
						I<Object> g = ints.a();
						I h = ints.a();
					}
					void b(J<Integer> ints) {
						ints.b();
						J<String> a = ints.b();
						J<Integer> b = ints.b();
						J<Object> c = ints.b();
						J d = ints.b();
						I<String> e = ints.b();
						I<Integer> f = ints.b();
						I<Object> g = ints.b();
						I h = ints.b();
					}
					void c(J<Integer> ints) {
						ints.c();
						J<String> a = ints.c();
						J<Integer> b = ints.c();
						J<Object> c = ints.c();
						J d = ints.c();
						I<String> e = ints.c();
						I<Integer> f = ints.c();
						I<Object> g = ints.c();
						I h = ints.c();
					}
				}"""
		},
		output
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=270194
public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				abstract class X implements I {
					public <A extends J<A, D>, D extends J<A, D>> A method(A arg) { return null; }
					void test(Y<String> c) { method(c); }
				}
				interface I {
					<A extends J<A,D>, D extends J<A,D>> A method(A arg);
				}
				interface J<A extends J<A,D>, D extends J<A,D>> {}
				class Y<E> implements J<Y<E>, Y<E>> {}"""
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=287592
public void test078() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					class Field<T> { T value; }
					<T> void a(T value) {}
					<T> void a(Field<T> field) {}
					<T extends Number> void b(T value) {}
					<T> void b(Field<T> field) {}
					void c(String value) {}
					void c(Field<String> field) {}
					void test(X x) {
						x.a(null);
						x.<String>a(null);
						x.b(null);
						x.<Integer>b(null);
						x.c(null);
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				x.b(null);
				  ^
			The method b(Number) is ambiguous for the type X
			----------
			2. ERROR in X.java (at line 13)
				x.<Integer>b(null);
				           ^
			The method b(Integer) is ambiguous for the type X
			----------
			3. ERROR in X.java (at line 14)
				x.c(null);
				  ^
			The method c(String) is ambiguous for the type X
			----------
			"""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=292350
// See that this test case exhibits the bug 345947
public void test079() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<T> {}
				class A {}
				class B extends A {}
				interface One {
				    I<B> x() throws IllegalAccessError;
				    <T extends A> I<T> y() throws IllegalAccessError;
				}
				interface Two extends One {
				    <T extends A> I<T> x() throws IllegalAccessError;
				    I<B> y() throws IllegalAccessError;
				}
				class X {
				    void x(Two t) { t.x(); }
				    void y(Two t) { t.y(); }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				<T extends A> I<T> x() throws IllegalAccessError;
				                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Name clash: The method x() of type Two has the same erasure as x() of type One but does not override it
			----------
			2. WARNING in X.java (at line 10)
				I<B> y() throws IllegalAccessError;
				^
			Type safety: The return type I<B> for y() from the type Two needs unchecked conversion to conform to I<A> from the type One
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293384
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<Tout extends Object> {
				   static public abstract class BaseA {};
					static public abstract class BaseB extends BaseA {};
					static public class Real extends BaseB {};
					static BaseA ask(String prompt) {
					    Real impl = new Real();
					    return (BaseA) ask(prompt, impl);
					}
					static BaseA ask(String prompt, Real impl) {
					    return null;
					}
					static <T extends BaseA> T ask(String prompt, T impl) {
					    return null;
					}
					static public void main(String[] args) {
				        System.out.println("SUCCESS");
					}
				}
				"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test081() {
	this.runConformTest(
		new String[] {
			"C.java",
			"""
				class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {
				    public void doSet(ModelType valueGetter) {
				        this.set((ValueType) valueGetter.getObject());
				    }
				    public void set(Object object) {
				        System.out.println("In A.set(Object)");
				    }
				}
				class B extends A<E, CharSequence> {
					public void set(CharSequence string) {
				        System.out.println("In B.set(CharSequence)");
				    }
				}
				public class C extends B {
				    static public void main(String[] args) {
				        C c = new C();
				        c.run();
				    }
				    public void run() {
				        E e = new E<String>(String.class);
				        this.doSet(e);
				    }
				}
				class D {
				    public Object getObject() {
				        return null;
				    }
				}
				class E<Type extends CharSequence> extends D {
				    private Class<Type> typeClass;
				    public E(Class<Type> typeClass) {
				        this.typeClass = typeClass;
				    }
				    public Type getObject() {
				        try {
				            return (Type) typeClass.newInstance();
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    }
				}
				interface I<ModelType, ValueType> {
				    public void doSet(ModelType model);
				    public void set(ValueType value);
				}
				"""

		},
		"In B.set(CharSequence)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test082() {
	this.runConformTest(
		new String[] {
			"C.java",
			"""
				class A<ModelType extends D, ValueType> extends I<ModelType, ValueType> {
				    public void doSet(ModelType valueGetter) {
				        this.set((ValueType) valueGetter.getObject());
				    }
				    public void set(Object object) {
				        System.out.println("In A.set(Object)");
				    }
				}
				class B extends A<E, CharSequence> {
					public void set(CharSequence string) {
				        System.out.println("In B.set(CharSequence)");
				    }
				}
				public class C extends B {
				    static public void main(String[] args) {
				        C c = new C();
				        c.run();
				    }
				    public void run() {
				        E e = new E<String>(String.class);
				        this.doSet(e);
				    }
				}
				class D {
				    public Object getObject() {
				        return null;
				    }
				}
				class E<Type extends CharSequence> extends D {
				    private Class<Type> typeClass;
				    public E(Class<Type> typeClass) {
				        this.typeClass = typeClass;
				    }
				    public Type getObject() {
				        try {
				            return (Type) typeClass.newInstance();
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    }
				}
				abstract class I<ModelType, ValueType> {
				    public abstract void doSet(ModelType model);
				    public abstract void set(ValueType value);
				}
				"""

		},
		"In B.set(CharSequence)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test083() {
	this.runConformTest(
		new String[] {
			"C.java",
			"""
				class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {
				    public void doSet(ModelType valueGetter) {
				        this.set((ValueType) valueGetter.getObject());
				    }
				    public void set(Object object) {
				        System.out.println("In A.set(Object)");
				    }
				}
				class B extends A<E, CharSequence> implements I<E, CharSequence> {
					public void set(CharSequence string) {
				        System.out.println("In B.set(CharSequence)");
				    }
				}
				public class C extends B {
				    static public void main(String[] args) {
				        C c = new C();
				        c.run();
				    }
				    public void run() {
				        E e = new E<String>(String.class);
				        this.doSet(e);
				    }
				}
				class D {
				    public Object getObject() {
				        return null;
				    }
				}
				class E<Type extends CharSequence> extends D {
				    private Class<Type> typeClass;
				    public E(Class<Type> typeClass) {
				        this.typeClass = typeClass;
				    }
				    public Type getObject() {
				        try {
				            return (Type) typeClass.newInstance();
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    }
				}
				interface I<ModelType, ValueType> {
				    public void doSet(ModelType model);
				    public void set(ValueType value);
				}
				"""

		},
		"In B.set(CharSequence)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test084() {
	this.runConformTest(
		new String[] {
			"C.java",
			"""
				abstract class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {
				    public void doSet(ModelType valueGetter) {
				        this.set((ValueType) valueGetter.getObject());
				    }
				    public void set(Object object) {
				        System.out.println("In A.set(Object)");
				    }
				}
				class B extends A<E, CharSequence> {
				}
				public class C extends B {
				    static public void main(String[] args) {
				        C c = new C();
				        c.run();
				    }
				    public void run() {
				        E e = new E<String>(String.class);
				        this.doSet(e);
				    }
				}
				class D {
				    public Object getObject() {
				        return null;
				    }
				}
				class E<Type extends CharSequence> extends D {
				    private Class<Type> typeClass;
				    public E(Class<Type> typeClass) {
				        this.typeClass = typeClass;
				    }
				    public Type getObject() {
				        try {
				            return (Type) typeClass.newInstance();
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    }
				}
				interface I<ModelType, ValueType> {
				    public void doSet(ModelType model);
				    public void set(ValueType value);
				}
				"""

		},
		"In A.set(Object)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test085() {
	this.runConformTest(
		new String[] {
			"C.java",
			"""
				class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {
				    public void doSet(ModelType valueGetter) {
				        this.set((ValueType) valueGetter.getObject());
				    }
				    public void set(Object object) {
				        System.out.println("In A.set(Object)");
				    }
				}
				class B extends A<E, CharSequence> {
				}
				public class C extends B {
				    static public void main(String[] args) {
				        C c = new C();
				        c.run();
				    }
				    public void run() {
				        E e = new E<String>(String.class);
				        this.doSet(e);
				    }
				}
				class D {
				    public Object getObject() {
				        return null;
				    }
				}
				class E<Type extends CharSequence> extends D {
				    private Class<Type> typeClass;
				    public E(Class<Type> typeClass) {
				        this.typeClass = typeClass;
				    }
				    public Type getObject() {
				        try {
				            return (Type) typeClass.newInstance();
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    }
				}
				interface I<ModelType, ValueType> {
				    public void doSet(ModelType model);
				    public void set(ValueType value);
				}
				"""

		},
		"In A.set(Object)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test086() {
	this.runConformTest(
		new String[] {
			"C.java",
			"""
				class A<ModelType extends D, ValueType> {
				    public void doSet(ModelType valueGetter) {
				        this.set((ValueType) valueGetter.getObject());
				    }
				    public void set(Object object) {
				        System.out.println("In A.set(Object)");
				    }
				}
				class B extends A<E, CharSequence> {
					public void set(CharSequence string) {
				        System.out.println("In B.set(CharSequence)");
				    }
				}
				public class C extends B {
				    static public void main(String[] args) {
				        C c = new C();
				        c.run();
				    }
				    public void run() {
				        E e = new E<String>(String.class);
				        this.doSet(e);
				    }
				}
				class D {
				    public Object getObject() {
				        return null;
				    }
				}
				class E<Type extends CharSequence> extends D {
				    private Class<Type> typeClass;
				    public E(Class<Type> typeClass) {
				        this.typeClass = typeClass;
				    }
				    public Type getObject() {
				        try {
				            return (Type) typeClass.newInstance();
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    }
				}
				interface I<ModelType, ValueType> {
				    public void doSet(ModelType model);
				    public void set(ValueType value);
				}
				"""

		},
		"In A.set(Object)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321485
public void test087() {
	String source =
		"""
		import java.util.Collection;
		import java.util.List;
		public class X {
		    public static <T> List<T> with(List<? extends T> p) { return null; }\s
		    public static <T> Collection<T> with(Collection<T> p) { return null; }
		    static { with(null); }
		}\s
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest( // FIXME: Eclipse has a bug
			new String[] { "X.java", source },
			"""
				----------
				1. ERROR in X.java (at line 6)
					static { with(null); }
					         ^^^^
				The method with(List<? extends Object>) is ambiguous for the type X
				----------
				"""
		);
	} else {
		this.runConformTest(
			new String[] { "X.java", source }
		);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=354579
public void test088a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    int foo () { return 0; }\s
				    double foo() { return 0.0; }
				}\s
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int foo () { return 0; }\s
				    ^^^^^^
			Duplicate method foo() in type X
			----------
			2. ERROR in X.java (at line 3)
				double foo() { return 0.0; }
				       ^^^^^
			Duplicate method foo() in type X
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=354579
public void test088b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public interface X {
				    int foo ();\s
				    double foo();
				}\s
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int foo ();\s
				    ^^^^^^
			Duplicate method foo() in type X
			----------
			2. ERROR in X.java (at line 3)
				double foo();
				       ^^^^^
			Duplicate method foo() in type X
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354579
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
				    int m2(List<Integer> a) {return 0;}\s
				    double m2(List<Integer> b) {return 0.0;}
				}\s
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				int m2(List<Integer> a) {return 0;}\s
				    ^^^^^^^^^^^^^^^^^^^
			Duplicate method m2(List<Integer>) in type X
			----------
			2. ERROR in X.java (at line 4)
				double m2(List<Integer> b) {return 0.0;}
				       ^^^^^^^^^^^^^^^^^^^
			Duplicate method m2(List<Integer>) in type X
			----------
			"""
	);
}
public void testBug426521() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.List;
				
				class Test {
				    <U> void m(List<U> l, U v) { }
				
				    <V> void m(List<V> l1, List<V> l2) { }
				
				    void test(List<Object> l) {
				        m(l, l); //JDK 6/7 give ambiguity here - EJC compiles ok
				    }
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ? "" :
		"""
			----------
			1. ERROR in Test.java (at line 9)
				m(l, l); //JDK 6/7 give ambiguity here - EJC compiles ok
				^
			The method m(List<Object>, Object) is ambiguous for the type Test
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428845
public void testBug428845() {
	runNegativeTest(
			new String[] {
				"AmbiguousTest.java",
				"""
					import java.io.File;
					public class AmbiguousTest {
					  static interface IInterface {
					    public void method(File file);
					  }
					  static abstract class AbstractClass implements IInterface {
					    public void method(File file) {
					      System.err.println("file");
					    }
					    public void method(String string) {
					      System.err.println("string");
					    }
					  }
					  private static AbstractClass newAbstractClass() {
					    return new AbstractClass() {};
					  }
					  public static void main(String[] args) {
					    newAbstractClass().method(null);
					  }
					}"""
			},
			"""
				----------
				1. ERROR in AmbiguousTest.java (at line 18)
					newAbstractClass().method(null);
					                   ^^^^^^
				The method method(File) is ambiguous for the type AmbiguousTest.AbstractClass
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=458563 - invalid ambiguous method error on Java 8 that isn't seen on Java 7 (or with javac)
public void testBug458563() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface IStoredNode<T> extends INodeHandle<DocumentImpl>, NodeHandle { }
				interface NodeHandle extends INodeHandle<DocumentImpl> { }
				class DocumentImpl implements INodeHandle<DocumentImpl> {
					public Object getNodeId() {return null;}
				}
				interface INodeHandle<D> {
				    public Object  getNodeId();
				}
				public class X {
					public void foo(IStoredNode bar) {
						bar.getNodeId();
					}
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=458563 - invalid ambiguous method error on Java 8 that isn't seen on Java 7 (or with javac)
public void testBug458563a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface IStoredNode<T> extends INodeHandle<DocumentImpl>, NodeHandle { }
				interface NodeHandle extends INodeHandle<DocumentImpl> { }
				class DocumentImpl implements INodeHandle<DocumentImpl> {
					public Object getNodeId() {return null;}
				}
				interface INodeHandle<D> {
				    public Object  getNodeId();
				}
				public class X {
					public void foo(IStoredNode<?> bar) {
						bar.getNodeId();
					}
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=466730 - Java 8: single method with generics is ambiguous when using import static ...* and inheritance
public void testBug466730() {
	runConformTest(
		new String[] {
			"bug/Base.java",
			"""
				package bug;
				public class Base {
					public static Object works() {
				        throw new IllegalStateException();
					}
				    public static <T> T fails() {
				        throw new IllegalStateException();
				    }
				}
				""",
			"bug/Derived.java",
			"package bug;\n" +
			"public class Derived extends Base {}\n",
			"bug/StaticImportBug.java",
			"""
				package bug;
				import static bug.Base.*;
				import static bug.Derived.*;
				public class StaticImportBug {
					void m() {
						java.util.Objects.requireNonNull(works());
						java.util.Objects.requireNonNull(fails());
					}
				}
				"""
	});
}
}
