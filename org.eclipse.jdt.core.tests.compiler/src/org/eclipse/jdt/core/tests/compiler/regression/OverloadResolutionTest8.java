/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
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
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 425156 - [1.8] Lambda as an argument is flagged with incompatible error
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class OverloadResolutionTest8 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test007"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public OverloadResolutionTest8(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public static Class testClass() {
	return OverloadResolutionTest8.class;
}

public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo(int [] a);
					}
					interface J  {
						int foo(int a);
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo((a)->a.length));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					System.out.println(foo((a)->a.length));
					                   ^^^
				The method foo(I) is ambiguous for the type X
				----------
				"""
			);
}
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						int foo();
					}
					public class X {
						static void goo(I i) {
							System.out.println("goo(I)");
						}
						static void goo(J j) {
							System.out.println("goo(J)");
						}
						public static void main(String[] args) {
							final boolean x = true;
							goo(()-> goo((I)null));
						}
						int f() {
							final boolean x = true;
							while (x);
						}
					}
					""",
			},
			"goo(I)");
}
public void test003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface J {
						int foo();
					}
					public class X {
					   static final boolean f = true;
						static void goo(J j) {
							System.out.println("goo(J)");
						}
						public static void main(String[] args) {
							final boolean x = true;
							goo(()-> {\s
								final boolean y = true;
								while (y);\s
								});
							goo(()-> {\s
								while (x);\s
								});
							goo(()-> {\s
								while (f);\s
								});
						}
					}
					""",
			},
			"""
				goo(J)
				goo(J)
				goo(J)""");
}

public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface J {
						int foo();
					}
					public class X {
					   static boolean f = true;
						static void goo(J j) {
							System.out.println("goo(J)");
						}
						public static void main(String[] args) {
							boolean x = true;
							goo(()-> {\s
								boolean y = true;
								while (y);\s
								});
							goo(()-> {\s
								while (x);\s
								});
							goo(()-> {\s
								while (f);\s
								});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					goo(()-> {\s
					^^^
				The method goo(J) in the type X is not applicable for the arguments (() -> {})
				----------
				2. ERROR in X.java (at line 15)
					goo(()-> {\s
					^^^
				The method goo(J) in the type X is not applicable for the arguments (() -> {})
				----------
				3. ERROR in X.java (at line 18)
					goo(()-> {\s
					^^^
				The method goo(J) in the type X is not applicable for the arguments (() -> {})
				----------
				""");
}
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface J {
						int foo();
					}
					public class X {
					   final boolean f = true;
						static void goo(J j) {
							System.out.println("goo(J)");
						}
						public static void main(String[] args) {
							final boolean x = true;
							goo(()-> {\s
								final boolean y = true;
								while (y);\s
								});
							goo(()-> {\s
								while (x);\s
								});
							goo(()-> {\s
								while (f);\s
								});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					goo(()-> {\s
					^^^
				The method goo(J) in the type X is not applicable for the arguments (() -> {})
				----------
				2. ERROR in X.java (at line 19)
					while (f);\s
					       ^
				Cannot make a static reference to the non-static field f
				----------
				""");
}
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static interface StringToInt {
					  	int stoi(String s);
					 }
					 public static interface ReduceInt {
					     int reduce(int a, int b);
					 }
					 void foo(StringToInt s) { }
					 void bar(ReduceInt r) { }
					 void bar() {
					     bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK
					     foo(s -> s.length());
					     foo((s) -> s.length());
					     foo((String s) -> s.length()); //SingleVariableDeclaration is OK
					     bar((x, y) -> x+y);
					 }
					}
					""",
			},
			"");
}
public void test007() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface J {
						void foo();
					}
					public class X {
						static void goo(J j) {
							System.out.println("goo(J)");
						}
						public static void main(String[] args) {
							goo(()-> 10);\s
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					goo(()-> 10);\s
					^^^
				The method goo(J) in the type X is not applicable for the arguments (() -> {})
				----------
				2. ERROR in X.java (at line 9)
					goo(()-> 10);\s
					         ^^
				Void methods cannot return a value
				----------
				""");
}
public void test008() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Object foo();
					}
					interface J  {
						String foo();
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo(()->null));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"foo(J)");
}
public void test009() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Object foo();
					}
					interface J  {
						void foo();
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo(()-> {}));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"foo(J)");
}
public void test010() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Object foo();
					}
					interface J  {
						void foo();
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo(()-> foo(()->null)));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"foo(I)");
}
public void test011() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J  {
						String foo();
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo(()-> "Hello" ));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"foo(J)");
}
public void test012() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J  {
						String foo();
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo(()-> 1234 ));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"foo(I)");
}
public void test013() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J  {
						Integer foo();
					}
					public class X {
						public static void main(String[] args) {
							System.out.println(foo(()-> 1234 ));
						}
						static String foo(I i) {
							return("foo(I)");
						}
						static String foo(J j) {
							return("foo(J)");
						}
					}
					""",
			},
			"foo(I)");
}
public void test014() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo();
					}
					interface J {
						int foo();
					}
					public class X {
					\s
						static void foo(I i) {
							System.out.println("foo(I)");
						}
					\t
						static void foo(J j) {
							System.out.println("foo(J)");
						}
					\t
						public static void main(String[] args) {
							foo(()-> new Integer(10));
						}
					}
					""",
			},
			"foo(I)");
}
public void test015() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface J {
						int foo();
					}
					interface I {
						Integer foo();
					}
					public class X {
					\s
						static void foo(I i) {
							System.out.println("foo(I)");
						}
					\t
						static void foo(J j) {
							System.out.println("foo(J)");
						}
					\t
						public static void main(String[] args) {
							foo(()-> new Integer(10));
						}
					}
					""",
			},
			"foo(I)");
}
public void test016() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface O {
						Object foo();
					}
					interface S {
						String foo();
					}
					interface I {
						O foo();
					}
					interface J {
						S foo();
					}
					public class X {
						static void foo(I i) {
							System.out.println("foo(I)");
						}
						static void foo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(()-> ()-> "String");
						}
					}
					""",
			},
			"foo(J)");
}
public void test017() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface J {
						int foo();
					}
					public class X {
						static void foo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(()-> new Integer(10));
						}
					}
					""",
			},
			"foo(J)");
}
public void test018() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X [] foo(int x);
					}
					public class X {
						static void foo(I x) {
					            System.out.println("foo(I)");
						}
						I i = X[]::new;
						public static void main(String[] args) {
							foo(X[]::new);
						}
					}
					""",
			},
			"foo(I)");
}
public void test019() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					public class X {
						static void foo(I x) {
					            System.out.println("foo(I)");
						}
						I i = X[]::new;
						public static void main(String[] args) {
							foo(X[]::new);
						}
					}
					""",
			},
			"foo(I)");
}

public void test020() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						Y foo();
					}
					class Y {
						Y() {
						}
					\t
						Y(int x) {
						}
					}
					public class X {
						static void foo(I i) {
						}
						static void foo(J j) {
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 20)
					foo(Y::new);
					^^^
				The method foo(I) is ambiguous for the type X
				----------
				""");
}
public void test021() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						Y foo();
					}
					class Y {
						private Y() {
						}
					\t
						Y(int x) {
						}
					}
					public class X {
						static void foo(I i) {
					       System.out.println("foo(I)");
						}
						static void foo(J j) {
					       System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"foo(I)");
}
public void test022() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						Y foo();
					}
					class Y {
						Y(float f) {
					       System.out.println("Y(float)");
						}
					\t
						Y(int x) {
					       System.out.println("Y(int)");
						}
					}
					public class X {
						static void foo(I i) {
					       i.foo(10);
						}
						static void foo(J j) {
					       j.foo();
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"Y(int)");
}
public void test023() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						Y foo();
					}
					class Y {
						Y(int ... x) {
						}
					\t
					}
					public class X {
						static void foo(I i) {
						}
						static void foo(J j) {
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					foo(Y::new);
					^^^
				The method foo(I) is ambiguous for the type X
				----------
				""");
}
public void test024() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						Y foo(int x);
					}
					class Y {
						Y(int x) {
						}
					\t
					}
					public class X {
						static void foo(I i) {
						}
						static void foo(J j) {
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					foo(Y::new);
					^^^
				The method foo(I) is ambiguous for the type X
				----------
				""");
}
public void test025() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						X foo(int x);
					}
					class Y extends X {
					    Y(int x) {
					    }
					}
					public class X {
						static void foo(I i) {
					            System.out.println("foo(I)");
						}
						static void foo(J j) {
					            System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"foo(I)");
}
public void test026() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						X foo(int x);
					}
					class Y extends X {
					    <T> Y(int x) {
					    }
					}
					public class X {
						static void foo(I i) {
					            System.out.println("foo(I)");
						}
						static void foo(J j) {
					            System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(Y::new);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 19)
					foo(Y::new);
					^^^
				The method foo(I) is ambiguous for the type X
				----------
				""");
}
public void test027() { // javac bug: 8b115 complains of ambiguity here.
	this.runConformTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacDoesNotCompileCorrectSource,
			new String[] {
				"X.java",
				"""
					interface I {
						Y foo(int x);
					}
					interface J {
						X foo(int x);
					}
					class Y extends X {
					    <T> Y(int x) {
					    }
					}
					public class X {
						static void foo(I i) {
					            System.out.println("foo(I)");
						}
						static void foo(J j) {
					            System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(Y::<String>new);
						}
					}
					""",
			},
			"foo(I)");
}
public void test028() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y [] foo(int x);
					}
					interface J {
						X [] foo();
					}
					class Y extends X {
					}
					public class X {
						static void foo(I i) {
							System.out.println("foo(I)");
						}
						static void foo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(Y []::new);
						}
					}
					""",
			},
			"foo(I)");
}
public void test029() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y [] foo(int x);
					}
					interface J {
						X [] foo();
					}
					class Y extends X {
					}
					public class X {
						static void foo(I i) {
							System.out.println("foo(I)");
						}
						static void foo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(X []::new);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 17)
					foo(X []::new);
					^^^
				The method foo(I) in the type X is not applicable for the arguments (X[]::new)
				----------
				2. ERROR in X.java (at line 17)
					foo(X []::new);
					    ^^^^^^^^^
				Constructed array X[] cannot be assigned to Y[] as required in the interface descriptor \s
				----------
				""");
}
public void test030() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Y [] foo(int x);
					}
					interface J {
						X [] foo(int x);
					}
					class Y extends X {
					}
					public class X {
						static void foo(I i) {
							System.out.println("foo(I)");
						}
						static void foo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							foo(X []::new);
						}
					}
					""",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850, [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
public void test031() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						void foo(X<String> s) {
					       System.out.println("foo(X<String>)");
					   }
						public static void main(String[] args) {
							new X<String>().foo(new X<>());
						}
					}
					""",
			},
			"foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850, [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
// FAIL: we no longer see that both methods are applicable...
// inference starts with X#RAW, finds the second method, then infers the diamond to Object and sees that foo is not ambiguous
public void _test032() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					    void foo(X<String> s, Object o) {
					        System.out.println("foo(X<String>)");
					    }
					    void foo(X xs, String s) {
					        System.out.println("foo(X<String>)");
					    }
					    public static void main(String[] args) {
					        new X<String>().foo(new X<>(), "Hello");
					    }
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					void foo(X xs, String s) {
					         ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				2. ERROR in X.java (at line 9)
					new X<String>().foo(new X<>(), "Hello");
					                ^^^
				The method foo(X<String>, Object) is ambiguous for the type X<String>
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850, [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
public void test033() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					class Y<T> {}
					public class X<T> extends Y<T> {
					    void foo(X<String> s) {
					        System.out.println("foo(X<String>)");
					    }
					    void foo(Y<String> y) {
					        System.out.println("foo(Y<String>)");
					    }
					    public static void main(String[] args) {
					        new X<String>().foo(new X<>());
					    }
					}
					""",
			},
			"foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422050, [1.8][compiler] Overloaded method call with poly-conditional expression rejected by the compiler
public void test422050() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {\s
						int foo();\s
					}
					interface J {\s
						double foo();\s
					}
					public class X {
						static int foo(I i) {
							return 0;
						}
						static int foo(J j) {
							return 1;
						}
						public static void main(String argv[]) {
							System.out.println(foo (() -> true ? 0 : 1));
						}
					}
					""",
			},
			"0");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						int foo();
					}
					public class X {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						static int foo() {
							 return 0;
						}
						public static void main(String[] args) {\s
							goo(X::foo);
						}
					}
					""",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						int foo(int y) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						int foo(int x) {
							 return 0;
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						<T> int foo(int y) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						int foo(int x) {
							 return 0;
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						<T> int foo(String y) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						int foo(int x) {
							 return 0;
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 23)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						int foo(String y) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						<T> int foo(int x) {
							 return 0;
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 23)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test4008712() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						int foo(String y) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						<T> int foo(int x) {
							 return 0;
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 23)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				""");
}
public void test4008712e() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						int foo(int y) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712f() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						int foo(int ... x) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 20)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				""");
}
public void test4008712g() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						private int foo(int x) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 8)
					private int foo(int x) {
					            ^^^^^^^^^^
				The method foo(int) from the type Y is never used locally
				----------
				2. ERROR in X.java (at line 20)
					goo(new X()::foo);
					^^^
				The method goo(I) in the type X is not applicable for the arguments (new X()::foo)
				----------
				3. ERROR in X.java (at line 20)
					goo(new X()::foo);
					    ^^^^^^^^^^^^
				The type X does not define foo(int) that is applicable here
				----------
				""");
}
public void test4008712h() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						public <T> int foo(int x) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 20)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				""");
}
public void test4008712i() { // javac bug: 8b115 complains of ambiguity here.
	this.runConformTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacDoesNotCompileCorrectSource,
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {
						int foo(int x);
					}
					class Y {
						public <T> int foo(int x) {
							 return 0;
						}
					}
					public class X extends Y {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::<String>foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712j() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						int foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<T> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"foo(I)");
}
public void test4008712k() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<T> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712l() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712m() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
					   public void foo() {}
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 21)
					goo(new X<String>()::foo);
					^^^
				The method goo(I) is ambiguous for the type X<T>
				----------
				""");
}
public void test4008712n() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
					   public String foo(String s) { return null; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712o() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					interface K<T> {
						public T foo(T x);
					}
					class Y<T> implements K {
						public Object foo(Object x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
					   public Object foo(Object s) { return null; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"foo(I)");
}
public void test4008712p() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
					   public String foo(String s) { return null; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 21)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X<T>
				----------
				2. WARNING in X.java (at line 21)
					goo(new X()::foo);
					        ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""");
}
public void test4008712q_raw() {
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 20)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X<T>
				----------
				2. WARNING in X.java (at line 20)
					goo(new X()::foo);
					    ^^^^^^^^^^^^
				Type safety: The method foo(Object) belongs to the raw type Y. References to generic type Y<T> should be parameterized
				----------
				3. WARNING in X.java (at line 20)
					goo(new X()::foo);
					        ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.JavacCompilesIncorrectSource;
	runner.runNegativeTest();
}
public void test4008712q_diamond() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<>()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712r() {
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						String foo();
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X[0]::clone);
						}
					}
					""",
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 20)
					goo(new X[0]::clone);
					^^^
				The method goo(I) is ambiguous for the type X<T>
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.JavacCompilesIncorrectSource;
	runner.runNegativeTest();
}
public void test4008712s() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						String foo();
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X[0]::toString);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712t() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Class foo();
					}
					interface J {
						Object foo();
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X[0]::getClass);
						}
					}
					""",
			},
			"foo(I)");
}
public void test4008712u() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						int foo();
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(I::clone);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 20)
					goo(I::clone);
					^^^
				The method goo(I) in the type X<T> is not applicable for the arguments (I::clone)
				----------
				2. ERROR in X.java (at line 20)
					goo(I::clone);
					    ^^^^^^^^
				The type I does not define clone() that is applicable here
				----------
				""");
}
public void test4008712v() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						int foo();
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
					       I i = () -> {};
							goo(i::hashCode);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712w() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						int foo();
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					}
					public class X<T> extends Y<String> {
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
					       I i = () -> {};
							goo(i::clone);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 21)
					goo(i::clone);
					^^^
				The method goo(I) in the type X<T> is not applicable for the arguments (i::clone)
				----------
				2. ERROR in X.java (at line 21)
					goo(i::clone);
					    ^^^^^^^^
				The type I does not define clone() that is applicable here
				----------
				""");
}
public void test4008712x() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String x);
					}
					interface J {
						String foo(String x);
					}
					class Y<T> {
						public T foo(T x) {
							 return null;
						}
					   private void foo() {}
					}
					public class X<T> extends Y<String> {
					   public String foo(String s) { return null; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X<String>()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712y() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						int foo();
					}
					public class X {
					   public int foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712z() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						long foo();
					}
					interface J {
						int foo();
					}
					public class X {
					   public int foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712za() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						long foo();
					}
					interface J {
						int foo();
					}
					public class X {
					   public long foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(I)");
}
public void test4008712zb() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {
						void foo();
					}
					public class X {
					   public long foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 16)
					goo(new X()::foo);
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				""");
}
public void test4008712zc() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J {
						Integer foo();
					}
					public class X {
					   public long foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 16)
					goo(new X()::foo);
					^^^
				The method goo(I) in the type X is not applicable for the arguments (new X()::foo)
				----------
				2. ERROR in X.java (at line 16)
					goo(new X()::foo);
					    ^^^^^^^^^^^^
				The type of foo() from the type X is long, this is incompatible with the descriptor\'s return type: int
				----------
				""");
}
public void test4008712zd() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J {
						Long foo();
					}
					public class X {
					   public long foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712ze() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J {
						Integer foo();
					}
					public class X {
					   public int foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(I)");
}
public void test4008712zf() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					interface J {
						Integer foo();
					}
					public class X {
					   public Integer foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void test4008712zg() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo();
					}
					interface J {
						Long foo();
					}
					public class X {
					   public Integer foo() { return 0; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(I)");
}
public void test4008712zh() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo();
					}
					interface J {
						Long foo();
					}
					public class X {
					   public Long foo() { return 0L; }
						static void goo(I i) {
							System.out.println("foo(I)");
						}
						static void goo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {\s
							goo(new X()::foo);
						}
					}
					""",
			},
			"foo(J)");
}
public void testVarargs() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					public class X {
						static void goo(I ... i) {
							i[0].foo();
						}
						public static void main(String[] args) {
							goo(()->{ System.out.println("Lambda");});
						}
					}
					""",
			},
			"Lambda");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850,  [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
public void test401850() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						void foo(X<String> s) {
							System.out.println("foo(X<String>)");
						}
						void foo(int x) {
							System.out.println("foo(int)");
						}
						public static void main(String[] args) {
							new X<String>().foo(new X<>());
						}
					}
					""",
			},
			"foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    Object m(X t);
					}
					interface J extends I {
					}
					public class X {
					    int foo()  { return 0; }
					    int test() {
					        return foo(X::foo);
					    }
					    int foo(I i) {return 0;}
					    int foo(J j) { return 1;}
					    public static void main(String args[]) {
					        X x = new X();
					        int i = x.test();
					        System.out.println(i);
					    }
					}
					""",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    Object m(X t);
					}
					interface J extends I {
					}
					public class X {
					    int foo()  { return 0; }
					    int test() {
					        return foo((x) -> x);
					    }
					    int foo(I i) {return 0;}
					    int foo(J j) { return 1;}
					    public static void main(String args[]) {
					        X x = new X();
					        int i = x.test();
					        System.out.println(i);
					    }
					}
					""",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    Object m(X t);
					}
					interface J extends I {
					}
					public class X {
					    int foo()  { return 0; }
					    int test() {
					        return foo(true ? (x) -> x : X::foo);
					    }
					    int foo(I i) {return 0;}
					    int foo(J j) { return 1;}
					    public static void main(String args[]) {
					        X x = new X();
					        int i = x.test();
					        System.out.println(i);
					    }
					}
					""",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    Object m(X t);
					}
					interface J extends I {
					}
					public class X {
					    int foo1()  { return 0; }
					    int foo2()  { return 0; }
					    int test() {
					        return foo(true ? X::foo1 : X::foo2);
					    }
					    int foo(I i) {return 0;}
					    int foo(J j) { return 1;}
					    public static void main(String args[]) {
					        X x = new X();
					        int i = x.test();
					        System.out.println(i);
					    }
					}
					""",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427628,  regression : The method * is ambiguous for the type *
public void test427628() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String [] args) {
					       new X().error(null);
					   }
						public void error(I i) {
							test(i!=null?i.getJ():null);
						}
						public void test(I i) {
					       System.out.println("I");
						}
						public void test(J j) {
					       System.out.println("J" + j);
						}
						public class I{
							public J getJ() {
								return null;
							}
						}
						public class J{}
					}
					""",
			},
			"Jnull");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427628,  regression : The method * is ambiguous for the type *
public void test427628a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String [] args) {
					       new X().error(null);
					   }
						public void error(I i) {
							test(i!=null?i.getJ():null);
						}
						public void test(I i) {
					       System.out.println("I");
						}
						public void test(K k) {
					       System.out.println("K" + j);
						}
						public class I{
							public J getJ() {
								return null;
							}
						}
						public class J{}
						public class K{}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					test(i!=null?i.getJ():null);
					^^^^
				The method test(X.I) in the type X is not applicable for the arguments (((i != null) ? i.getJ() : null))
				----------
				2. ERROR in X.java (at line 6)
					test(i!=null?i.getJ():null);
					             ^^^^^^^^
				Type mismatch: cannot convert from X.J to X.I
				----------
				3. ERROR in X.java (at line 12)
					System.out.println("K" + j);
					                         ^
				j cannot be resolved to a variable
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427628,  regression : The method * is ambiguous for the type *
public void test427628b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void setSetting(String key, String value) {
						}
						public void setSetting(String key, Integer value) {
						    setSetting(key, value == null ? null : Integer.toString(value));
						}
					}
					""",
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421922, [1.8][compiler] Varargs & Overload - Align to JLS8
public void _test421922() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					public class X {
					    public static void main(String[] args) {
					        test(1);
					    }
					    public static void test(int... a) {
					        System.out.print("int ... = ");
					        System.out.println(Arrays.toString(a));
					    }
					    public static <T> void test(Object... a) {
					        System.out.print("Object ... = ");
					        System.out.println(Arrays.toString(a));
					    }
					}
					""",
			},
			"int ... = [1]");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427748, [1.8][compiler] Cannot convert from Boolean to boolean on generic return type
public void test427748() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String [] args) {
					    getLog(doit(baction));
					  }
					  private static interface Action<T> {T run();}
					  private static Action<Boolean> baction = () -> true;
					  static void getLog(int override) {}
					  static void getLog(boolean override) {
					      System.out.println("OK");
					  }
					  private static <T> T doit(Action<T> action) { return action.run(); }
					}
					""",
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427808, [1.8] Correct super() invocation is not inferred when argument is a conditional expression
public void test427808() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends Foo {
						public X(I i) {
							super(i != null ?  i.toString() : null);
					    }
					   public static void main(String [] args) {
					       new X(null);
					   }
					}
					class Foo implements I {
						Foo(I i) {}
						Foo(String string){ System.out.println("OK"); }
					}
					interface I {}
					""",
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429985,  [1.8][compiler] Resolution of right method signature
public void test429985() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Supplier;
					public class X {
						public static void main(String[] args) {
							// This does not compile with ECJ
							test(() -> "hi");
						}
						// Note: when removing this code the main function compiles with ECJ
						static void test(String message) {
						}
						static void test(Supplier<String> messageSupplier) {
					       System.out.println(messageSupplier.get());
						}
					}
					""",
			},
			"hi");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429985,  [1.8][compiler] Resolution of right method signature
public void test429985a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Supplier;
					public class X {
						public static void main(String[] args) {
							// This does not compile with ECJ
							test(() -> "hi");
						}
						static void test(Supplier<String> messageSupplier) {
					       System.out.println(messageSupplier.get());
						}
						// Note: when removing this code the main function compiles with ECJ
						static void test(String message) {
						}
					}
					""",
			},
			"hi");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448801, [1.8][compiler] Scope.mSMB & 15.12.3 Compile-Time Step 3
public void test448801() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						private class Y {
						}
						public X(Y ...ys) {
						}
						public void foo(Y ...ys) {
						}
						public void goo() {
						}
					}
					""",
				"Z.java",
				"""
					interface I {
						static void ifoo() {
						}
					}
					abstract class ZSuper {
						void zSuperFoo() {
						}
						abstract void goo();
					}
					public class Z extends ZSuper implements I {
						void goo() {
							super.zSuperFoo();
							super.goo();
						}
						public static void main(String[] args) {
							X x = new X();
							x.foo();
							System.out.println(x.goo());
							goo();
							Z.goo();
							zoo();
							new Z().ifoo();
							super.zSuperFoo();
						}
						class ZZ {
							class ZZZ {
								void zoo() {
								}
							}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in Z.java (at line 13)
					super.goo();
					^^^^^^^^^^^
				Cannot directly invoke the abstract method goo() for the type ZSuper
				----------
				2. ERROR in Z.java (at line 16)
					X x = new X();
					      ^^^^^^^
				The constructor X(X.Y...) of type X is not applicable as the formal varargs element type X.Y is not accessible here
				----------
				3. ERROR in Z.java (at line 17)
					x.foo();
					  ^^^
				The method foo(X.Y...) of type X is not applicable as the formal varargs element type X.Y is not accessible here
				----------
				4. ERROR in Z.java (at line 18)
					System.out.println(x.goo());
					           ^^^^^^^
				The method println(boolean) in the type PrintStream is not applicable for the arguments (void)
				----------
				5. ERROR in Z.java (at line 19)
					goo();
					^^^
				Cannot make a static reference to the non-static method goo() from the type Z
				----------
				6. ERROR in Z.java (at line 20)
					Z.goo();
					^^^^^^^
				Cannot make a static reference to the non-static method goo() from the type Z
				----------
				7. ERROR in Z.java (at line 21)
					zoo();
					^^^
				The method zoo() is undefined for the type Z
				----------
				8. ERROR in Z.java (at line 22)
					new Z().ifoo();
					        ^^^^
				The method ifoo() is undefined for the type Z
				----------
				9. ERROR in Z.java (at line 23)
					super.zSuperFoo();
					^^^^^
				Cannot use super in a static context
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450415, [1.8][compiler] Failure to resolve overloaded call.
public void test450415() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					interface I {
						String foo();
					}
					interface J {
						List<String> foo();
					}
					public class X {
					    static void goo(I i) {
					    	System.out.println("goo(I)");
					    }
					    static void goo(J j) {
					    	System.out.println("goo(J)");
					    }
					    static <T> List<T> loo() {
					    	return null;
					    }
					    public static void main(String[] args) {
							goo(()->loo());
						}
					}
					"""
			},
			"goo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450415, [1.8][compiler] Failure to resolve overloaded call.
public void test450415a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					public class X {
						static <T> void foo() {
							class Y {
								void goo(T t) {
									System.out.println("T");
								}
								void goo(I i) {
									System.out.println("I");
								}
							}
							new Y().goo(()->{});
						}
						public static void main(String[] args) {
							foo();
						}
					}
					"""
			},
			"I");
}
public void test482440a() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				class Test {
				
				    // generic method
				    interface ConsumerA {
				        <T> void accept(int i);
				    }
				
				    // non-generic
				    interface ConsumerB {
				        void accept(int i);
				    }
				
				    // A before B
				    void execute1(ConsumerA c) {}
				    void execute1(ConsumerB c) {}
				
				    // B before A
				    void execute2(ConsumerB c) {}
				    void execute2(ConsumerA c) {}
				
				    void test() {
				        execute1(x -> {});  // compiles in Eclipse
				        execute2(x -> {});  // doesn't compile
				    }
				
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 22)
				execute1(x -> {});  // compiles in Eclipse
				^^^^^^^^
			The method execute1(Test.ConsumerA) is ambiguous for the type Test
			----------
			2. ERROR in Test.java (at line 23)
				execute2(x -> {});  // doesn\'t compile
				^^^^^^^^
			The method execute2(Test.ConsumerB) is ambiguous for the type Test
			----------
			""");
}
public void test482440b() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				class Test {
				
				    // generic method
				    interface ConsumerA {
				        <T> void accept(int i);
				    }
				
				    // non-generic
				    interface ConsumerB {
				        void accept(int i);
				    }
				
				    // A before B
				    void execute1(ConsumerA c) {}
				    void execute1(ConsumerB c) {}
				
				    // B before A
				    void execute2(ConsumerB c) {}
				    void execute2(ConsumerA c) {}
				
				    void test() {
				        execute1((int x) -> {});  // compiles in Eclipse
				        execute2((int x) -> {});  // doesn't compile
				    }
				
				}
				"""
		});
}
}
