/*******************************************************************************
 * Copyright (c) 2011, 2024 IBM Corporation and others.
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
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *							bug 382721 - [1.8][compiler] Effectively final variables needs special treatment
 *                          Bug 384687 - [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
 *                          Bug 404657 - [1.8][compiler] Analysis for effectively final variables fails to consider loops
 *     Stephan Herrmann - Contribution for
 *							bug 404649 - [1.8][compiler] detect illegal reference to indirect or redundant super via I.super.m() syntax
 *							bug 404728 - [1.8]NPE on QualifiedSuperReference error
 *							Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *							Bug 423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
 *							Bug 425156 - [1.8] Lambda as an argument is flagged with incompatible error
 *							Bug 426563 - [1.8] AIOOBE when method with error invoked with lambda expression as argument
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NegativeLambdaExpressionsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test401610i"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public NegativeLambdaExpressionsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public static Test setUpTest(Test test) throws Exception {
	TestCase.setUpTest(test);
	RegressionTestSetup suite = new RegressionTestSetup(ClassFileConstants.JDK1_8);
	suite.addTest(test);
	return suite;
}

@Override
protected Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	return defaultOptions;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382818, ArrayStoreException while compiling lambda
public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					  void foo(int x, int y);
					}
					public class X {
					  public static void main(String[] args) {
					    int x, y;
					    I i = () -> {
					      int z = 10;
					    };
					    i++;
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					I i = () -> {
					      ^^^^^
				Lambda expression's signature does not match the signature of the functional interface method foo(int, int)
				----------
				2. ERROR in X.java (at line 10)
					i++;
					^^^
				Type mismatch: cannot convert from I to int
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382841, ClassCastException while compiling lambda
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					 void foo(int x, int y);
					}
					public class X {
					  public static void main(String[] args) {
					    int x, y;
					    I i = (p, q) -> {
					      int r = 10;
					    };
					    i++;
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					i++;
					^^^
				Type mismatch: cannot convert from I to int
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382841, ClassCastException while compiling lambda
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					 void foo(int x, int y);
					}
					public class X {
					  public static void main(String[] args) {
					    int x, y;
					    I i = null, i2 = (p, q) -> {
					      int r = 10;
					    }, i3 = null;
					    i++;
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					i++;
					^^^
				Type mismatch: cannot convert from I to int
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, syntax error reported incorrectly on syntactically valid lambda expression
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface IX {
					    public void foo();
					}
					public class X {
					     IX i = () -> 42;
					     int x
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					IX i = () -> 42;
					             ^^
				Void methods cannot return a value
				----------
				2. ERROR in X.java (at line 6)
					int x
					    ^
				Syntax error, insert ";" to complete FieldDeclaration
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383085 super::identifier not accepted.
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface IX{
						public void foo();
					}
					public class X {
						IX i = super::toString;
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, syntax error reported incorrectly on *syntactically* valid reference expression
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface One{}
					interface Two{}
					interface Three{}
					interface Four{}
					interface Five{}
					interface Blah{}
					interface Outer<T1,T2>{interface Inner<T3,T4>{interface Leaf{ <T> void method(); } } }
					interface IX{
						public void foo();
					}
					public class X {
						IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;
					   int x
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;
					       ^^^^^^^^^^^^^^^^^^^^^
				The member type Outer.Inner<T3,T4> cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Outer<One,Two>
				----------
				2. ERROR in X.java (at line 12)
					IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Outer.Inner.Deeper cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 12)
					IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;
					                                                       ^^^
				Six cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 13)
					int x
					    ^
				Syntax error, insert ";" to complete FieldDeclaration
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383096, NullPointerException with a wrong lambda code snippet
public void _test007() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {}
						public class X {
						    void foo() {
						            I t1 = f -> {{};
						            I t2 = () -> 42;
						        }\s
						        }
						}
						""",
				},
			"""
				----------
				1. ERROR in X.java (at line 6)
					int
					^^^
				Syntax error on token "int", delete this token
				----------
				""",
			true /* perform statement recovery */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383949,  Explicit this parameter illegal in lambda expressions
public void test008() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  int foo(X x);
						}
						public class X {
						  public static void main(String[] args) {
						    I i = (X this) -> 10; \s
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						I i = (X this) -> 10; \s
						         ^^^^
					Lambda expressions cannot declare a this parameter
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383949,  Explicit this parameter illegal in lambda expressions
public void test009() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.awt.event.ActionListener;
						interface I {
						    void doit(String s1, String s2);
						}
						public class X {
						  public void test1(int x) {
						    ActionListener al = (public xyz) -> System.out.println(xyz);\s
						    I f = (abstract final s, @Nullable t) -> System.out.println(s + t);\s
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						ActionListener al = (public xyz) -> System.out.println(xyz);\s
						                            ^^^
					Syntax error, modifiers and annotations are not allowed for the lambda parameter xyz as its type is elided
					----------
					2. ERROR in X.java (at line 8)
						I f = (abstract final s, @Nullable t) -> System.out.println(s + t);\s
						                      ^
					Syntax error, modifiers and annotations are not allowed for the lambda parameter s as its type is elided
					----------
					3. ERROR in X.java (at line 8)
						I f = (abstract final s, @Nullable t) -> System.out.println(s + t);\s
						                                   ^
					Syntax error, modifiers and annotations are not allowed for the lambda parameter t as its type is elided
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381121,  [] should be accepted in reference expressions.
public void test010() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							Object foo(int [] ia);
						}
						public class X {
							I i = (int [] ia) -> {
								      return ia.clone();
							      };
							I i2 = int[]::clone;
							Zork z;
						}
						""",
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382701, [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expressions.
public void test011() {
	// This test checks that common semantic checks are indeed
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Object foo(int [] ia);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (int [] ia) -> {\n" +
					"		Zork z;\n" +  // Error: No such type
					"		unknown = 0;\n;" + // Error: No such variable
					"		int a = 42 + ia;\n" + // Error: int + int[] is wrong
					"		return ia.clone();\n" +
					"	};\n" +
					"	static void staticLambda() {\n" +
					"		I i = (int [] ia) -> this;\n" + // 'this' is static
					"	}\n" +
					"	I j = array -> {\n" +
					"		int a = array[2] + 3;\n" + // No error, ia must be correctly identifies as int[]
					"		int b = 42 + array;\n" + // Error: int + int[] is wrong - yes it is!
					"		System.out.println(\"i(array) = \" + i.foo(array));\n" + // fields are accessible!
					"		return;\n" + // Error here, expecting Object, not void
					"	};\n" +
					"	Runnable r = () -> { return 42; };\n" + // Runnable.run not expecting return value
					"	void anotherLambda() {\n" +
					"		final int beef = 0;\n" +
					"		I k = (int [] a) -> a.length + beef;\n" + // No error, beef is in scope
					"	}\n" +
					"}\n",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 7)
						unknown = 0;
						^^^^^^^
					unknown cannot be resolved to a variable
					----------
					3. ERROR in X.java (at line 8)
						;		int a = 42 + ia;
						 		        ^^^^^^^
					The operator + is undefined for the argument type(s) int, int[]
					----------
					4. ERROR in X.java (at line 12)
						I i = (int [] ia) -> this;
						                     ^^^^
					Cannot use this in a static context
					----------
					5. ERROR in X.java (at line 16)
						int b = 42 + array;
						        ^^^^^^^^^^
					The operator + is undefined for the argument type(s) int, int[]
					----------
					6. ERROR in X.java (at line 18)
						return;
						^^^^^^^
					This method must return a result of type Object
					----------
					7. ERROR in X.java (at line 20)
						Runnable r = () -> { return 42; };
						                     ^^^^^^^^^^
					Void methods cannot return a value
					----------
					"""
);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384600, [1.8] 'this' should not be allowed in lambda expressions in contexts that don't allow it
public void test012() {
	// This test checks that common semantic checks are indeed
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
							static void foo() {
								I i = () -> {
									System.out.println(this);
									I j = () -> {
										System.out.println(this);
										I k = () -> {
											System.out.println(this);
										};
									};
								};
							}
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						System.out.println(this);
						                   ^^^^
					Cannot use this in a static context
					----------
					2. ERROR in X.java (at line 9)
						System.out.println(this);
						                   ^^^^
					Cannot use this in a static context
					----------
					3. ERROR in X.java (at line 11)
						System.out.println(this);
						                   ^^^^
					Cannot use this in a static context
					----------
					"""
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384600, [1.8] 'this' should not be allowed in lambda expressions in contexts that don't allow it
public void test013() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
							void foo(Zork z) {
								I i = () -> {
									System.out.println(this);
									I j = () -> {
										System.out.println(this);
										I k = () -> {
											System.out.println(this);
										};
									};
								};
							}
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						void foo(Zork z) {
						         ^^^^
					Zork cannot be resolved to a type
					----------
					"""
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384595, Reject illegal modifiers on lambda arguments.
public void test014() {
	String extra = this.complianceLevel < ClassFileConstants.JDK17 ? "" :
		"""
			----------
			2. WARNING in X.java (at line 5)
				I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;
				                                              ^^^^^^^^
			Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
			""";
	int offset = this.complianceLevel < ClassFileConstants.JDK17 ? 0 : 1;
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int x, int y, int z);\t
						}
						public class X {
						     I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;
						}
						@interface Marker {
						}
						""",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
				"	                             ^^^^^^^^^\n" +
				"Undefined cannot be resolved to a type\n" +
				extra +
				"----------\n" +
				(2 + offset) + ". ERROR in X.java (at line 5)\n" +
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
				"	                                                              ^^^^^^\n" +
				"Lambda expression\'s parameter o is expected to be of type int\n" +
				"----------\n" +
				(3 + offset) + ". ERROR in X.java (at line 5)\n" +
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
				"	                                                                     ^\n" +
				"Illegal modifier for parameter o; only final is permitted\n" +
				"----------\n" +
				(4 + offset) + ". ERROR in X.java (at line 5)\n" +
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
				"	                                                                                            ^\n" +
				"Illegal modifier for parameter p; only final is permitted\n" +
				"----------\n" +
				(5 + offset) + ". ERROR in X.java (at line 5)\n" +
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
				"	                                                                                                  ^\n" +
				"Void methods cannot return a value\n" +
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399534, [1.8][compiler] Lambda parameters must be checked for compatibility with the single abstract method of the functional interface.
public void test015() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.Collection;
						import java.util.List;
						interface I { void run(int x); }
						interface J { void run(int x, String s); }
						interface K { void run(Collection<String> jobs); }
						class X {
						    I i1 = (String y) -> {};
						    I i2 = (y) -> {};
						    I i3 = y -> {};
						    I i4 = (int x, String y) -> {};
						    I i5 = (int x) -> {};
						    J j1 = () -> {};
						    J j2 = (x, s) -> {};
						    J j3 = (String x, int s) -> {};
						    J j4 = (int x, String s) -> {};
						    J j5 = x ->  {};
						    K k1 = (Collection l) -> {};
						    K k2 = (Collection <Integer> l) -> {};
						    K k3 = (Collection <String> l) -> {};
						    K k4 = (List <String> l) -> {};
						    K k5 = (l) -> {};
						    K k6 = l -> {};
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						I i1 = (String y) -> {};
						        ^^^^^^
					Lambda expression's parameter y is expected to be of type int
					----------
					2. ERROR in X.java (at line 10)
						I i4 = (int x, String y) -> {};
						       ^^^^^^^^^^^^^^^^^^^^
					Lambda expression's signature does not match the signature of the functional interface method run(int)
					----------
					3. ERROR in X.java (at line 12)
						J j1 = () -> {};
						       ^^^^^
					Lambda expression's signature does not match the signature of the functional interface method run(int, String)
					----------
					4. ERROR in X.java (at line 14)
						J j3 = (String x, int s) -> {};
						        ^^^^^^
					Lambda expression's parameter x is expected to be of type int
					----------
					5. ERROR in X.java (at line 14)
						J j3 = (String x, int s) -> {};
						                  ^^^
					Lambda expression's parameter s is expected to be of type String
					----------
					6. ERROR in X.java (at line 16)
						J j5 = x ->  {};
						       ^^^^
					Lambda expression's signature does not match the signature of the functional interface method run(int, String)
					----------
					7. WARNING in X.java (at line 17)
						K k1 = (Collection l) -> {};
						        ^^^^^^^^^^
					Collection is a raw type. References to generic type Collection<E> should be parameterized
					----------
					8. ERROR in X.java (at line 17)
						K k1 = (Collection l) -> {};
						        ^^^^^^^^^^
					Lambda expression's parameter l is expected to be of type Collection<String>
					----------
					9. ERROR in X.java (at line 18)
						K k2 = (Collection <Integer> l) -> {};
						        ^^^^^^^^^^
					Lambda expression's parameter l is expected to be of type Collection<String>
					----------
					10. ERROR in X.java (at line 20)
						K k4 = (List <String> l) -> {};
						        ^^^^
					Lambda expression's parameter l is expected to be of type Collection<String>
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test016() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  String foo();
						}
						public class X {
						  public static void main(String[] args) {
						    I i1 = () -> 42;
						    I i2 = () -> "Hello";
						    I i3 = () -> { return 42; };
						    I i4 = () -> { return "Hello"; };
						    I i5 = () -> {};
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						I i1 = () -> 42;
						             ^^
					Type mismatch: cannot convert from int to String
					----------
					2. ERROR in X.java (at line 8)
						I i3 = () -> { return 42; };
						                      ^^
					Type mismatch: cannot convert from int to String
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test017() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  Integer foo();
						}
						public class X {
						  public static void main(String[] args) {
						    I i1 = () -> 42;
						    I i2 = () -> "Hello";
						    I i3 = () -> { return 42; };
						    I i4 = () -> { return "Hello"; };
						    I i5 = () -> {};
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						I i2 = () -> "Hello";
						             ^^^^^^^
					Type mismatch: cannot convert from String to Integer
					----------
					2. ERROR in X.java (at line 9)
						I i4 = () -> { return "Hello"; };
						                      ^^^^^^^
					Type mismatch: cannot convert from String to Integer
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test018() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  I foo();
						}
						class P implements I {
						   public I foo() { return null; }
						}
						public class X {
						  public static void main(String[] args) {
						    I i1 = () -> 42;
						    I i2 = () -> "Hello";
						    I i3 = () -> { return 42; };
						    I i4 = () -> { return "Hello"; };
						    I i5 = () -> { return new P(); };
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						I i1 = () -> 42;
						             ^^
					Type mismatch: cannot convert from int to I
					----------
					2. ERROR in X.java (at line 10)
						I i2 = () -> "Hello";
						             ^^^^^^^
					Type mismatch: cannot convert from String to I
					----------
					3. ERROR in X.java (at line 11)
						I i3 = () -> { return 42; };
						                      ^^
					Type mismatch: cannot convert from int to I
					----------
					4. ERROR in X.java (at line 12)
						I i4 = () -> { return "Hello"; };
						                      ^^^^^^^
					Type mismatch: cannot convert from String to I
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test019() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void foo();
						}
						public class X {
						    I i1 = () -> 42;
						    I i3 = () -> { return 42; };
						    I i4 = () -> System.out.println();
						    I i5 = () -> { System.out.println(); };
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						I i1 = () -> 42;
						             ^^
					Void methods cannot return a value
					----------
					2. ERROR in X.java (at line 6)
						I i3 = () -> { return 42; };
						               ^^^^^^^^^^
					Void methods cannot return a value
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test020() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  int foo(int x);
						}
						public class X {
						    I i5 = (x) -> { if (x == 0) throw new NullPointerException(); };
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						I i5 = (x) -> { if (x == 0) throw new NullPointerException(); };
						       ^^^^^^
					This method must return a result of type int
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test021() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  int foo(int x);
						}
						public class X {
						    I i5 = (x) -> { if (x == 0) throw new NullPointerException(); throw new NullPointerException(); };
						    Zork z;
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test022() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  J foo();
						}
						interface J {
						  int foo();
						}
						public class X {
						    I I = () -> () -> 10;
						    Zork z;
						}
						""",
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
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test023() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  J foo();
						}
						interface J {
						  int foo();
						}
						public class X {
						    I i1 = () -> 10;
						    I i2 = () -> { return 10; };
						    I i3 = () -> () -> 10;
						    I i4 = () -> { return () -> 10; };
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						I i1 = () -> 10;
						             ^^
					Type mismatch: cannot convert from int to J
					----------
					2. ERROR in X.java (at line 9)
						I i2 = () -> { return 10; };
						                      ^^
					Type mismatch: cannot convert from int to J
					----------
					""");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test024() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I3 {
						  Object foo();
						}
						public class X {
						  public static void main(String[] args) {
						    I3 i = () -> 42; // Warning: Autoboxing, but casting to Object??
						  }
						  Object foo(Zork z) {
							  return 42;
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						Object foo(Zork z) {
						           ^^^^
					Zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test025() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {\r
				  String foo();\r
				}\r
				public class X {\r
				  public static void main(String[] args) {\r
				    I i = () -> 42;\r
				    I i2 = () -> "Hello, Lambda";\r
				  }\r
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = () -> 42;
					            ^^
				Type mismatch: cannot convert from int to String
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test026() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {\r
				  String foo();\r
				}\r
				public class X {\r
				  public static void main(String[] args) {\r
				    I i = () -> {\r
				      return 42;\r
				    };\r
				    I i2 = () -> {\r
				      return "Hello, Lambda as a block!";\r
				    };\r
				  }\r
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 7)
					return 42;
					       ^^
				Type mismatch: cannot convert from int to String
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test027() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {\r
				  int baz();\r
				}\r
				public class X {\r
				  public static void main(String[] args) {
				    I i1 = () -> {
				      System.out.println("No return");
				    }; // Error: Lambda block should return value
				    I i2 = () -> {
				      if (Math.random() < 0.5) return 42;
				    }; // Error: Lambda block doesn't always return a value
				    I i3 = () -> {
				      return 42;
				      System.out.println("Dead!");
				    }; // Error: Lambda block has dead code
				  }
				  public static I doesFlowInfoEscape() {
				    I i1 = () -> {
				      return 42;
				    };
				    return i1; // Must not complain about unreachable code!
				  }
				  public static I areExpresionsCheckedForReturns() {
				    I i1 = () -> 42;  // Must not complain about missing return!
				    return i1;
				  }
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i1 = () -> {
					       ^^^^^
				This method must return a result of type int
				----------
				2. ERROR in X.java (at line 9)
					I i2 = () -> {
					       ^^^^^
				This method must return a result of type int
				----------
				3. ERROR in X.java (at line 14)
					System.out.println("Dead!");
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unreachable code
				----------
				""");
}
// Bug 399979 - [1.8][compiler] Statement expressions should be allowed in non-block lambda body when return type is void (edit)
public void test028() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {
				  void foo();
				}
				public class X {
				  int data;
				  public void main(String[] args) {
				    I i1 = () -> data++;
				    I i2 = () -> data = 10;
				    I i3 = () -> data += 10;
				    I i4 = () -> --data;
				    I i5 = () -> bar();
				    I i6 = () -> new X();
				    I i7 = () -> 0;
				    I i = () -> 1 + data++;
				  }
				  int bar() {
					  return 0;
				  }
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					I i7 = () -> 0;
					             ^
				Void methods cannot return a value
				----------
				2. ERROR in X.java (at line 14)
					I i = () -> 1 + data++;
					            ^^^^^^^^^^
				Void methods cannot return a value
				----------
				""");
}
// Bug 384600 - [1.8] 'this' should not be allowed in lambda/Reference expressions in contexts that don't allow it
public void test029() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {
					void doit();
				}
				public class X extends Y {
					static void foo() {
						I i1 = this::zoo;
						I i2 = super::boo;
						I i3 = () -> super.zoo();
						I i4 = () -> this.boo();
					}
					void boo () {
						I i1 = this::zoo;
						I i2 = super::boo;
						I i3 = () -> super.zoo();
						I i4 = () -> this.boo();
					}
					void zoo() {
					}
				}
				class Y {
					void boo() {
					}
					void zoo() {
					}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i1 = this::zoo;
					       ^^^^
				Cannot use this in a static context
				----------
				2. ERROR in X.java (at line 7)
					I i2 = super::boo;
					       ^^^^^
				Cannot use super in a static context
				----------
				3. ERROR in X.java (at line 8)
					I i3 = () -> super.zoo();
					             ^^^^^
				Cannot use super in a static context
				----------
				4. ERROR in X.java (at line 9)
					I i4 = () -> this.boo();
					             ^^^^
				Cannot use this in a static context
				----------
				""");
}
// Bug 382713 - [1.8][compiler] Compiler should reject lambda expressions when target type is not a functional interface
public void test030() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {
				  void foo();
				  void goo();
				}
				public class X {
				  public static void main(String[] args) {
				    X x = () -> 10;
				    I i = () -> 10;
				  }
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					X x = () -> 10;
					      ^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				2. ERROR in X.java (at line 8)
					I i = () -> 10;
					      ^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// Bug 398267 - [1.8][compiler] Variables in the body of the lambda expression should be valid
public void test031() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {
				  void foo();
				}
				public class X {
				  public void main(String[] args) {
				    I i = () -> {
				            		p = 10;
				            		Zork z = this.blank;
				            		super.foo();
				            		goo();
				           	};
				  }
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					p = 10;
					^
				p cannot be resolved to a variable
				----------
				2. ERROR in X.java (at line 8)
					Zork z = this.blank;
					^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 8)
					Zork z = this.blank;
					              ^^^^^
				blank cannot be resolved or is not a field
				----------
				4. ERROR in X.java (at line 9)
					super.foo();
					      ^^^
				The method foo() is undefined for the type Object
				----------
				5. ERROR in X.java (at line 10)
					goo();
					^^^
				The method goo() is undefined for the type X
				----------
				""");
}
// Bug 399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test032() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface IA {\r
				  void snazz();\r
				}\r
				interface IB {\r
				  void baz() throws java.io.IOException;\r
				}\r
				public class X {\r
				  public static void main(String[] args) {
				    IA i1 = () -> {
				      throw new java.io.EOFException(); // Error: not declared
				    };
				    IB i2 = () -> {
				      throw new java.io.EOFException(); // Fine: IOException is declared
				    }; // No error, it's all good
				  }
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 10)
					throw new java.io.EOFException(); // Error: not declared
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type EOFException
				----------
				""");
}
// Bug 399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test033() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface IA {\r
				  void snazz();\r
				}\r
				interface IB {\r
				  void baz() throws java.io.IOException;\r
				}\r
				public class X {\r
				  public static void main(String[] args) {
				    IA i1 = () -> {
				      throw new java.io.EOFException(); // Error: not declared
				    };
				    IB i2 = () -> {
				      throw new java.io.EOFException(); // Fine: IOException is declared
				    }; // No error, it's all good
				  }
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 10)
					throw new java.io.EOFException(); // Error: not declared
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type EOFException
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test034() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {\r
				  int foo(int x, int y);\r
				}\r
				public class X {\r
				  public static void main(String[] args) {\r
				    int x = 2;\r
				    I i = (a, b) -> {\r
				      return 42.0 + a + args.length; // Type mismatch: cannot convert from double to int\r
				    };\r
				  }\r
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 8)
					return 42.0 + a + args.length; // Type mismatch: cannot convert from double to int
					       ^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from double to int
				----------
				""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381121,  [] should be accepted in reference expressions.
public void test035() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							Object foo(int [] ia);
						}
						public class X {
							I i = (int [] ia) -> ia.clone();
							I i2 = int[]::clone;
							Zork z;
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727,  Lambda expression parameters and locals cannot shadow variables from context
public void test036() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {\r
				  void foo(int x, int y);\r
				}\r
				public class X {\r
				  public static void main(String[] args) {\r
				    int x, y;\r
				    I i = (x, y) -> { // Error: x,y being redeclared\r
				      int args = 10; //  Error args is being redeclared\r
				    };\r
				  }\r
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 7)
					I i = (x, y) -> { // Error: x,y being redeclared
					       ^
				Lambda expression's parameter x cannot redeclare another local variable defined in an enclosing scope.\s
				----------
				2. ERROR in X.java (at line 7)
					I i = (x, y) -> { // Error: x,y being redeclared
					          ^
				Lambda expression's parameter y cannot redeclare another local variable defined in an enclosing scope.\s
				----------
				3. ERROR in X.java (at line 8)
					int args = 10; //  Error args is being redeclared
					    ^^^^
				Lambda expression's local variable args cannot redeclare another local variable defined in an enclosing scope.\s
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382702 - [1.8][compiler] Lambda expressions should be rejected in disallowed contexts
public void test037() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" +
			"  int foo1(String x);\r\n" +
			"}\r\n" +
			"public class X {\r\n" +
			"  public static void main(String[] args) {\r\n" +
			"    System.out.println(\"Lambda in illegal context: \" + (() -> \"Illegal Lambda\"));\r\n" +
			"    System.out.println(\"Method Reference in illegal context: \" + System::exit);\r\n" +
			"    System.out.println(\"Constructor Reference in illegal context: \" + String::new);\r\n" +
			"    I sam1 = (x) -> x.length(); // OK\r\n" +
//			"    I sam2 = ((String::length)); // OK\r\n" +
//			"    I sam3 = (Math.random() > 0.5) ? String::length : String::hashCode; // OK\r\n" +
//			"    I sam4 = (I)(String::length); // OK\r\n" +
            "    int x = (x) -> 10;\n" +
            "    X x2 = (x) -> 10;\n" +
			"  }\r\n" +
			"}"},
			"""
				----------
				1. ERROR in X.java (at line 6)
					System.out.println("Lambda in illegal context: " + (() -> "Illegal Lambda"));
					                                                   ^^^^^^^^^^^^^^^^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				2. ERROR in X.java (at line 7)
					System.out.println("Method Reference in illegal context: " + System::exit);
					                                                             ^^^^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				3. ERROR in X.java (at line 8)
					System.out.println("Constructor Reference in illegal context: " + String::new);
					                                                                  ^^^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				4. ERROR in X.java (at line 10)
					int x = (x) -> 10;
					        ^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				5. ERROR in X.java (at line 11)
					X x2 = (x) -> 10;
					       ^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test038() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				import java.io.EOFException;
				import java.io.IOException;
				interface I { void m() throws IOException; }
				interface J { void m() throws EOFException; }
				interface K { void m() throws ClassNotFoundException; }
				interface IJ extends I, J {}
				interface IJK extends I, J, K {}
				public class X {
					int var;
					IJ ij = () -> {
						if (var == 0) {
							throw new IOException();
						} else if (var == 2) {
							throw new EOFException();
						} else {
							throw new ClassNotFoundException();\s
						}
					};
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					throw new IOException();
					^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type IOException
				----------
				2. ERROR in X.java (at line 16)
					throw new ClassNotFoundException();\s
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type ClassNotFoundException
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test039() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				import java.io.EOFException;
				import java.io.IOException;
				import java.sql.SQLException;
				import java.sql.SQLTransientException;
				import java.util.List;
				import java.util.concurrent.TimeoutException;
				interface A {
				  List<String> foo(List<String> arg) throws IOException, SQLTransientException;
				}
				interface B {
				  List foo(List<String> arg) throws EOFException, SQLException, TimeoutException;
				}
				interface C {
				  List foo(List arg) throws Exception;
				}
				interface D extends A, B {}
				interface E extends A, B, C {}
				public class X {
					int var;
					D d = (x) -> {
						switch (var) {
						case 0 : throw new EOFException();
						case 1: throw new IOException();
						case 2: throw new SQLException();
						case 3: throw new SQLTransientException();
						case 4: throw new TimeoutException();
						default: throw new NullPointerException();
						}
					};
					E e = (x) -> {
						switch (var) {
						case 0 : throw new EOFException();
						case 1: throw new IOException();
						case 2: throw new SQLException();
						case 3: throw new SQLTransientException();
						case 4: throw new TimeoutException();
						default: throw new NullPointerException();
						}
					};
				}
				"""
			},
			"""
				----------
				1. WARNING in X.java (at line 11)
					List foo(List<String> arg) throws EOFException, SQLException, TimeoutException;
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 14)
					List foo(List arg) throws Exception;
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 14)
					List foo(List arg) throws Exception;
					         ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				4. ERROR in X.java (at line 23)
					case 1: throw new IOException();
					        ^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type IOException
				----------
				5. ERROR in X.java (at line 24)
					case 2: throw new SQLException();
					        ^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type SQLException
				----------
				6. ERROR in X.java (at line 26)
					case 4: throw new TimeoutException();
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type TimeoutException
				----------
				7. ERROR in X.java (at line 33)
					case 1: throw new IOException();
					        ^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type IOException
				----------
				8. ERROR in X.java (at line 34)
					case 2: throw new SQLException();
					        ^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type SQLException
				----------
				9. ERROR in X.java (at line 36)
					case 4: throw new TimeoutException();
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type TimeoutException
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test040() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I {
				  <P extends Exception> Object m() throws P;
				}
				interface J {
				  <Q extends Exception> String m() throws Exception;
				}
				interface G extends I, J {}
				public class X {
					int var;
					G g1 = () -> {
					    throw new Exception();\s
					};
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					G g1 = () -> {
					       ^^^^^
				Illegal lambda expression: Method m of type J is generic\s
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test041() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.sql.SQLException;
				interface G1 {
				  <E extends Exception> Object m(E p) throws E;
				}
				interface G2 {
				  <F extends Exception> String m(F q) throws Exception;
				}
				interface G extends G1, G2 {} // G has descriptor <F extends Exception> ()->String throws F
				public class X {
					G g = (x) -> { // Elided type is inferred from descriptor to be F
					    throw x;    // ~== throw new F()
					};
				}
				class Y implements G {
					public <T extends Exception> String m(T t) throws T {
						throw t;
					}
					void foo(G1 g1) {
							g1.m(new IOException());
					}
					void foo(G2 g2) {
							g2.m(new SQLException());
					}
				}
				"""

			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					G g = (x) -> { // Elided type is inferred from descriptor to be F
					      ^^^^^^
				Illegal lambda expression: Method m of type G2 is generic\s
				----------
				2. ERROR in X.java (at line 20)
					g1.m(new IOException());
					^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type IOException
				----------
				3. ERROR in X.java (at line 23)
					g2.m(new SQLException());
					^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type Exception
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test042() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.sql.SQLException;
				interface G1 {
				  <E extends Exception> Object m(E p) throws E;
				}
				interface G2 {
				  <F extends Exception> String m(F q) throws Exception;
				}
				interface G extends G1, G2 {} // G has descriptor <F extends Exception> ()->String throws F
				public class X {
					G g1 = (F x) -> {
					    throw x;
					};
					G g2 = (IOException x) -> {
					    throw x;
					};
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					G g1 = (F x) -> {
					       ^^^^^^^^
				Illegal lambda expression: Method m of type G2 is generic\s
				----------
				2. ERROR in X.java (at line 11)
					G g1 = (F x) -> {
					        ^
				F cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 14)
					G g2 = (IOException x) -> {
					       ^^^^^^^^^^^^^^^^^^
				Illegal lambda expression: Method m of type G2 is generic\s
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399224 - [1.8][compiler][internal] Implement TypeBinding.getSingleAbstractMethod
public void test043() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.IGNORE);

	this.runNegativeTest(
			new String[] {
			"X.java",
            "import java.util.List;\n" +
			"interface A { void foo(); }\n" +  // yes
			"interface B { boolean equals(Object obj); }\n" + // no
			"interface C extends B { void foo(); }\n" + // yes
			"interface D<T> { boolean equals(Object obj); void foo(); }\n" + // yes
			"interface E { void foo(); Object clone(); }\n" + // no
			"interface F { void foo(List<String> p); }\n" + // yes
            "interface G { void foo(List<String> p); }\n" + // yes
            "interface H extends F, G {}\n" + // yes
            "interface I { List foo(List<String> p); }\n" + // yes
            "interface J { List<String> foo(List arg); }\n" + // yes
            "interface K extends I, J {}\n" + // yes
            "interface L { void foo(List<Integer> p); }\n" +  // yes
            "interface M extends I, L {}\n" + // no
            "interface N { void foo(List<String> p, Class q); }\n" + // yes
            "interface O { void foo(List p, Class<?> q); }\n" + // yes
            "interface P extends N, O {}\n" + // no
            "interface Q { long foo(); }\n" + // yes
            "interface R { int foo(); }\n" + // yes
            "interface S extends Q, R {}\n" + // no
            "interface T<P> { void foo(P p); }\n" + // yes
            "interface U<P> { void foo(P p); }\n" + // yes
            "interface V<P, Q> extends T<P>, U<Q> {}\n" + // no
            "interface W<T, N extends Number> { void m(T arg); void m(N arg); }\n" + // no
            "interface X extends W<String, Integer> {}\n" + // no
            "interface Y extends W<Integer, Integer> {}\n" + // yes

            "class Z {\n" +
            "    A a              =    () -> {};\n" +
            "    B b              =    () -> {};\n" +
            "    C c              =    () -> {};\n" +
            "    D<?> d           =    () -> {};\n" +
            "    E e              =    () -> {};\n" +
            "    F f              =    (p0) -> {};\n" +
            "    G g              =    (p0) -> {};\n" +
            "    H h              =    (p0) -> {};\n" +
            "    I i              =    (p0) -> { return null; };\n" +
            "    J j              =    (p0) -> { return null; };\n" +
            "    K k              =    (p0) -> { return null; };\n" +
            "    L l              =    (p0) -> {};\n" +
            "    M m              =    (p0) -> {};\n" +
            "    N n              =    (p0, q0) -> {};\n" +
            "    O o              =    (p0, q0) -> {};\n" +
            "    P p              =    (p0, q0) -> {};\n" +
            "    Q q              =    () -> { return 0;};\n" +
            "    R r              =    () -> { return 0;};\n" +
            "    S s              =    () -> {};\n" +
            "    T<?> t           =    (p0) -> {};\n" +
            "    U<?> u           =    (p0) -> {};\n" +
            "    V<?,?> v         =    (p0) -> {};\n" +
            "    W<?,?> w         =    (p0) -> {};\n" +
            "    X x              =    (p0) -> {};\n" +
            "    Y y              =    (p0) -> {};\n" +
			"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 17)
					interface P extends N, O {}
					          ^
				Name clash: The method foo(List, Class<?>) of type O has the same erasure as foo(List<String>, Class) of type N but does not override it
				----------
				2. ERROR in X.java (at line 20)
					interface S extends Q, R {}
					          ^
				The return types are incompatible for the inherited methods Q.foo(), R.foo()
				----------
				3. ERROR in X.java (at line 23)
					interface V<P, Q> extends T<P>, U<Q> {}
					          ^
				Name clash: The method foo(P) of type U<P> has the same erasure as foo(P) of type T<P> but does not override it
				----------
				4. ERROR in X.java (at line 29)
					B b              =    () -> {};
					                      ^^^^^
				The target type of this expression must be a functional interface
				----------
				5. ERROR in X.java (at line 32)
					E e              =    () -> {};
					                      ^^^^^
				The target type of this expression must be a functional interface
				----------
				6. ERROR in X.java (at line 40)
					M m              =    (p0) -> {};
					                      ^^^^^^^
				The target type of this expression must be a functional interface
				----------
				7. ERROR in X.java (at line 43)
					P p              =    (p0, q0) -> {};
					                      ^^^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				8. ERROR in X.java (at line 46)
					S s              =    () -> {};
					                      ^^^^^
				The target type of this expression must be a functional interface
				----------
				9. ERROR in X.java (at line 49)
					V<?,?> v         =    (p0) -> {};
					                      ^^^^^^^
				The target type of this expression must be a functional interface
				----------
				10. ERROR in X.java (at line 50)
					W<?,?> w         =    (p0) -> {};
					                      ^^^^^^^
				The target type of this expression must be a functional interface
				----------
				11. ERROR in X.java (at line 51)
					X x              =    (p0) -> {};
					                      ^^^^^^^
				The target type of this expression must be a functional interface
				----------
				""",
			null,
			false,
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399224 - [1.8][compiler][internal] Implement TypeBinding.getSingleAbstractMethod
public void test044() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.IGNORE);

	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				import java.util.List;
				interface A { <T> T foo(List<T> p); }
				interface B { <S> S foo(List<S> p); }
				interface C { <T, S> S foo(List<T> p); }
				interface D extends A, B {}
				interface E extends A, C {}
				class Z {
				    A a              =    (p) -> { return null;};
				    B b              =    (p) -> { return null;};
				    C c              =    (p) -> { return null;};
				    D d              =    (p) -> { return null;};
				    E e              =    (p) -> { return null;};
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					interface E extends A, C {}
					          ^
				Name clash: The method foo(List<T>) of type C has the same erasure as foo(List<T>) of type A but does not override it
				----------
				2. ERROR in X.java (at line 8)
					A a              =    (p) -> { return null;};
					                      ^^^^^^
				Illegal lambda expression: Method foo of type A is generic\s
				----------
				3. ERROR in X.java (at line 9)
					B b              =    (p) -> { return null;};
					                      ^^^^^^
				Illegal lambda expression: Method foo of type B is generic\s
				----------
				4. ERROR in X.java (at line 10)
					C c              =    (p) -> { return null;};
					                      ^^^^^^
				Illegal lambda expression: Method foo of type C is generic\s
				----------
				5. ERROR in X.java (at line 11)
					D d              =    (p) -> { return null;};
					                      ^^^^^^
				Illegal lambda expression: Method foo of type B is generic\s
				----------
				6. ERROR in X.java (at line 12)
					E e              =    (p) -> { return null;};
					                      ^^^^^^
				The target type of this expression must be a functional interface
				----------
				""",
			null,
			false,
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400386 - [1.8][spec] Broken example in 9.8, discussion box - bullet 2 ?
public void test045() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				interface I { Object m(); }
				interface J<S> { S m(); }
				interface K<T> { T m(); }
				interface Functional<S,T> extends I, J<S>, K<T> {}
				class X {
				    Functional<String,Integer> f = () -> { };
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					interface Functional<S,T> extends I, J<S>, K<T> {}
					          ^^^^^^^^^^
				The return types are incompatible for the inherited methods I.m(), J<S>.m(), K<T>.m()
				----------
				2. ERROR in X.java (at line 6)
					Functional<String,Integer> f = () -> { };
					                               ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
public void test046() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				import java.util.List;
				interface A { void f(List<String> ls); }
				interface B { void f(List<Integer> li); }
				interface C extends A,B {}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					interface C extends A,B {}
					          ^
				Name clash: The method f(List<Integer>) of type B has the same erasure as f(List<String>) of type A but does not override it
				----------
				""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test047() {
	// This test checks that the simple cases are OK
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    int var = 2;
						    I x = new I() {
						      public void doit() {
						        System.out.println(args); // OK: args is not re-assignment since declaration/first assignment
						        System.out.println(var); // Error: var is not effectively final
						      }
						    };
						    var=2;
						  }
						}""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						System.out.println(var); // Error: var is not effectively final
						                   ^^^
					Local variable var defined in an enclosing scope must be final or effectively final
					----------
					"""
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test048() {
	// This test checks that the simple cases are OK
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    int var = 2;
						    I x2 = () -> {
						      System.out.println(var); // Error: var is not effectively final
						    };
						    var=2;
						  }
						}""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						System.out.println(var); // Error: var is not effectively final
						                   ^^^
					Local variable var defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test049() {
	// This test checks that the simple cases are OK
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    int var = 2;
						    I x2 = () -> {
						      System.out.println(args); // OK: args is not re-assignment since declaration/first assignment
						    };
						    var=2;
						  }
						}""" ,
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test050() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    try {
						      new java.io.File("dweep").getCanonicalPath();
						    } catch (java.io.IOException ioe) {
						      I x2 = () -> {
						        System.out.println(ioe.getMessage()); // OK: args is not re-assignment since declaration/first assignment
						      };
						    };
						  }
						}
						"""
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test051() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    java.util.List<String> list = new java.util.ArrayList<>();
						    for (String s : list) {
						      I x2 = () -> {
						        System.out.println(s); // OK: args is not re-assignment since declaration/first assignment
						      };
						    };
						  }
						
						}
						""" ,
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test052() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    java.util.List<String> list = new java.util.ArrayList<>();
						    for (String s2 : list) {
						      s2 = "Nice!";
						      I x2 = () -> {
						        System.out.println(s2); // Error: var is not effectively final
						      };
						    };
						  }
						
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						System.out.println(s2); // Error: var is not effectively final
						                   ^^
					Local variable s2 defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test053() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  void foo() {
						    try {
						       System.out.println("try");
						  } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
						    I i = () -> {
						      System.out.println(e);
						     };
						    }
						  }
						}
						""" ,
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test054() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"  void foo2(String[] args) {\n" +
					"   int var;\n" +
					"   if (args != null)\n" +
					"      var = args.length;\n" +
					"   else\n" +
					"      var = 2;\n" +
					"   I x = new I() {\n" +
					"     public void doit() {\n" +
					"       System.out.println(var);\n" +  // no error here.
					"       args = null;\n" + // error here.
					"     }\n" +
					"   };\n" +
					"  }\n" +
					"}\n" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 14)
						args = null;
						^^^^
					Local variable args defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test055() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  void foo(final int x) {
						    I i = () -> {
						      x = 10;
						     };
						  }
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						x = 10;
						^
					The final local variable x cannot be assigned. It must be blank and not using a compound assignment
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test056() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  void foo(final int x) {
						    X i = new X() {
						      { x = 10; }
						     };
						  }
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						{ x = 10; }
						  ^
					The final local variable x cannot be assigned, since it is defined in an enclosing type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test057() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  void foo(int x) {
						    I i = () -> {
						      x = 10;
						     };
						  }
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						x = 10;
						^
					Local variable x defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test058() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  void foo(int x) {
						    X i = new X() {
						      { x = 10; }
						     };
						  }
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						{ x = 10; }
						  ^
					Local variable x defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test059() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo();
						}
						class X {
							void foo(int [] p) {
								for (int is : p) {
									I j = new I () {
										public void foo() {
											System.out.println(is);
										};
									};
								}
							}
						}
						""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test060() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo();
						}
						class X {
							void foo(int [] p) {
								for (int is : p) {
									I j = () -> {
											System.out.println(is);
									};
								}
							}
						}
						""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test061() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"  void foo2(String[] args) {\n" +
					"   int var;\n" +
					"   if (args != null)\n" +
					"      var = args.length;\n" +
					"   else\n" +
					"      var = 2;\n" +
					"   I x = () ->  {\n" +
					"       System.out.println(var);\n" +  // no error here.
					"       args = null;\n" + // error here.
					"   };\n" +
					"  }\n" +
					"}\n" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						args = null;
						^^^^
					Local variable args defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test062() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    int var;
						    if (args != null) {
						       var = args.length;
						       I x = new I() {
						         public void doit() {
						           System.out.println(var);
						         }
						       };
						    } else {
						       var = 2; // HERE
						    }
						  }
						}
						""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test063() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.io.IOException;
						interface I {
						    void doit();
						}
						public class X {
						  public static void main(String[] args) throws IOException {
						
							try {
								throw new IOException();
							} catch (Exception e) {
								if (args == null) {
									throw e;
								}\s
						                else {
									e = null;
								}
							}
						  }
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 12)
						throw e;
						^^^^^^^^
					Unhandled exception type Exception
					----------
					2. WARNING in X.java (at line 14)
						else {
								e = null;
							}
						     ^^^^^^^^^^^^^^^^^^
					Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test064() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.io.IOException;
						interface I {
						    void doit();
						}
						public class X {
						  public static void main(String[] args) throws IOException {
						
							try {
								throw new IOException();
							} catch (Exception e) {
								if (args == null) {
									throw e;
								}\s
							}
						  }
						}
						""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test065() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo();
						}
						class X {
							void foo() {
								int x = 10;
								I i = () -> {
									System.out.println(x++);
								};
							}
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						System.out.println(x++);
						                   ^
					Local variable x defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test066() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.io.IOException;
						class X {
							void foo(int x) throws IOException {
								try {
									throw new IOException();
								} catch (Exception e) {
									if (x == 0) {
										throw e;
									} else {
										e = null;
									}
								}
							}
						}
						""" ,
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						throw e;
						^^^^^^^^
					Unhandled exception type Exception
					----------
					2. WARNING in X.java (at line 9)
						} else {
									e = null;
								}
						       ^^^^^^^^^^^^^^^^^^^^
					Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test067() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit ();
						}
						class X {
							int p;
							void foo(int p) {
								int i = 10;
								X x = new X();
								x = new X();
								I l = () -> {
									x.p = i++;
								};
							}
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 6)
						void foo(int p) {
						             ^
					The parameter p is hiding a field from type X
					----------
					2. ERROR in X.java (at line 11)
						x.p = i++;
						      ^
					Local variable i defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382721, [1.8][compiler] Effectively final variables needs special treatment
public void test068() {
	// This test checks that common semantic checks are indeed run
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit ();
						}
						class X {
							int p;
							void foo(int p) {
								int i = 10;
								X x = new X();
								x = new X();
								I l = () -> {
									x.p = i;
								};
							}
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 6)
						void foo(int p) {
						             ^
					The parameter p is hiding a field from type X
					----------
					2. ERROR in X.java (at line 11)
						x.p = i;
						^
					Local variable x defined in an enclosing scope must be final or effectively final
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test069() {
	// Lambda argument hides a field.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p);
						}
						public class X {
							int f1;
							int f2;
						
							void foo() {
								I i = (int f1)  -> {
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i = (int f1)  -> {
						           ^^
					The parameter f1 is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test070() {
	// Lambda argument redeclares outer method argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p);
						}
						public class X {
							void foo(int x) {
								I i = (int x)  -> {
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						I i = (int x)  -> {
						           ^
					Lambda expression's parameter x cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test071() {
	// Lambda argument redeclares outer method local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p);
						}
						public class X {
							void foo(int x) {
						       int l;
								I i = (int l)  -> {
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						I i = (int l)  -> {
						           ^
					Lambda expression's parameter l cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test072() {
	// Lambda redeclares its own argument
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
							void foo(int x) {
						       int l;
								I i = (int p, int p)  -> {
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						I i = (int p, int p)  -> {
						                  ^
					Duplicate parameter p
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test073() {
	// Lambda local hides a field
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo() {
								I i = (int p, int q)  -> {
						           int f;
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 8)
						int f;
						    ^
					The local variable f is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test074() {
	// Lambda local redeclares the enclosing method's argument
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int a) {
								I i = (int p, int q)  -> {
						           int a;
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						int a;
						    ^
					Lambda expression's local variable a cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test075() {
	// Lambda local redeclares the enclosing method's local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int a) {
						       int loc;
								I i = (int p, int q)  -> {
						           int loc;
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						int loc;
						    ^^^
					Lambda expression's local variable loc cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test076() {
	// Lambda local redeclares its own parameter
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int a) {
						       int loc;
								I i = (int p, int q)  -> {
						           int p;
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						int p;
						    ^
					Duplicate local variable p
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test077() {
	// Lambda local redeclares its own self
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int a) {
						       int loc;
								I i = (int p, int q)  -> {
						           int self, self;
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						int self, self;
						          ^^^^
					Duplicate local variable self
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test078() {
	// Nested Lambda argument redeclares a field.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int a) {
						       int loc;
								I i = (int p, int q)  -> {
						           I i2 = (int f, int p0) -> {};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i2 = (int f, int p0) -> {};
						            ^
					The parameter f is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test079() {
	// Nested Lambda argument redeclares outer method's argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int loc;
								I i = (int p, int q)  -> {
						           I i2 = (int f, int outerp) -> {};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i2 = (int f, int outerp) -> {};
						            ^
					The parameter f is hiding a field from type X
					----------
					2. ERROR in X.java (at line 9)
						I i2 = (int f, int outerp) -> {};
						                   ^^^^^^
					Lambda expression's parameter outerp cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test080() {
	// Nested Lambda argument redeclares outer method's local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						           I i2 = (int locouter, int outerp) -> {};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						I i2 = (int locouter, int outerp) -> {};
						            ^^^^^^^^
					Lambda expression's parameter locouter cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					2. ERROR in X.java (at line 9)
						I i2 = (int locouter, int outerp) -> {};
						                          ^^^^^^
					Lambda expression's parameter outerp cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test081() {
	// Nested Lambda argument redeclares outer lambda's argument.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						           I i2 = (int p, int q) -> {};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						I i2 = (int p, int q) -> {};
						            ^
					Lambda expression's parameter p cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					2. ERROR in X.java (at line 9)
						I i2 = (int p, int q) -> {};
						                   ^
					Lambda expression's parameter q cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test082() {
	// Nested Lambda argument redeclares outer lambda's local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = (int lamlocal, int q) -> {};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {};
						            ^^^^^^^^
					Lambda expression's parameter lamlocal cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					2. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {};
						                          ^
					Lambda expression's parameter q cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test083() {
	// Nested Lambda local redeclares a field.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = (int lamlocal, int q) -> {int f;};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {int f;};
						            ^^^^^^^^
					Lambda expression's parameter lamlocal cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					2. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {int f;};
						                          ^
					Lambda expression's parameter q cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					3. WARNING in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {int f;};
						                                     ^
					The local variable f is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test084() {
	// Nested Lambda local redeclares outer methods local.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = (int lamlocal, int q) -> {int locouter;};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {int locouter;};
						            ^^^^^^^^
					Lambda expression's parameter lamlocal cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					2. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {int locouter;};
						                          ^
					Lambda expression's parameter q cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					3. ERROR in X.java (at line 10)
						I i2 = (int lamlocal, int q) -> {int locouter;};
						                                     ^^^^^^^^
					Lambda expression's local variable locouter cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test085() {
	// Nested Lambda local redeclares outer lambda's argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = (int j, int q) -> {int p, lamlocal;};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						I i2 = (int j, int q) -> {int p, lamlocal;};
						                   ^
					Lambda expression's parameter q cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					2. ERROR in X.java (at line 10)
						I i2 = (int j, int q) -> {int p, lamlocal;};
						                              ^
					Lambda expression's local variable p cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					3. ERROR in X.java (at line 10)
						I i2 = (int j, int q) -> {int p, lamlocal;};
						                                 ^^^^^^^^
					Lambda expression's local variable lamlocal cannot redeclare another local variable defined in an enclosing scope.\s
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test086() {
	// Nested Lambda local redeclares its own argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = (int x1, int x2) -> {int x1, x2;};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						I i2 = (int x1, int x2) -> {int x1, x2;};
						                                ^^
					Duplicate local variable x1
					----------
					2. ERROR in X.java (at line 10)
						I i2 = (int x1, int x2) -> {int x1, x2;};
						                                    ^^
					Duplicate local variable x2
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test087() {
	// Inner class (!) inside Lambda hides field
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           class X { void foo(int f) {} }
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						class X { void foo(int f) {} }
						      ^
					The nested type X cannot hide an enclosing type
					----------
					2. WARNING in X.java (at line 10)
						class X { void foo(int f) {} }
						                       ^
					The parameter f is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test088() {
	// class inside lambda (!) redeclares a field.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int a) {
						       int loc;
								I i = (int p, int q)  -> {
						           I i2 = new I() { public void foo(int f, int p0) {};
								};};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo(int f, int p0) {};
						                                     ^
					The parameter f is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test089() {
	// class inside lambda redeclares outer method's argument.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int loc;
								I i = (int p, int q)  -> {
						           I i2 = new I() { public void foo(int f, int outerp) {}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo(int f, int outerp) {}};
						                                     ^
					The parameter f is hiding a field from type X
					----------
					2. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo(int f, int outerp) {}};
						                                            ^^^^^^
					The parameter outerp is hiding another local variable defined in an enclosing scope
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test090() {
	// class inside lambda redeclares outer method's local.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						           I i2 = new I() { public void foo(int locouter, int outerp)  {}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo(int locouter, int outerp)  {}};
						                                     ^^^^^^^^
					The parameter locouter is hiding another local variable defined in an enclosing scope
					----------
					2. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo(int locouter, int outerp)  {}};
						                                                   ^^^^^^
					The parameter outerp is hiding another local variable defined in an enclosing scope
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test091() {
	// class inside lambda redeclares outer lambda's argument.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						           I i2 = new I() { public void foo (int p, int q) {}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo (int p, int q) {}};
						                                      ^
					The parameter p is hiding another local variable defined in an enclosing scope
					----------
					2. WARNING in X.java (at line 9)
						I i2 = new I() { public void foo (int p, int q) {}};
						                                             ^
					The parameter q is hiding another local variable defined in an enclosing scope
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test092() {
	// class inside lambda redeclares outer lambda's local.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = new I() { public void foo (int lamlocal, int q)  {} };
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo (int lamlocal, int q)  {} };
						                                      ^^^^^^^^
					The parameter lamlocal is hiding another local variable defined in an enclosing scope
					----------
					2. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo (int lamlocal, int q)  {} };
						                                                    ^
					The parameter q is hiding another local variable defined in an enclosing scope
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test093() {
	// local of class inside lambda redeclares a field.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};
						                                      ^^^^^^^^
					The parameter lamlocal is hiding another local variable defined in an enclosing scope
					----------
					2. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};
						                                                    ^
					The parameter q is hiding another local variable defined in an enclosing scope
					----------
					3. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo (int lamlocal, int q) {int f;}};
						                                                            ^
					The local variable f is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test094() {
	// local of class under lambda redeclares outer methods local.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};
						                                     ^^^^^^^^
					The parameter lamlocal is hiding another local variable defined in an enclosing scope
					----------
					2. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};
						                                                   ^
					The parameter q is hiding another local variable defined in an enclosing scope
					----------
					3. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo(int lamlocal, int q) {int locouter;}};
						                                                           ^^^^^^^^
					The local variable locouter is hiding another local variable defined in an enclosing scope
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test095() {
	// local of class under lambda redeclares outer lambda's argument & local
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};
						                                            ^
					The parameter q is hiding another local variable defined in an enclosing scope
					----------
					2. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};
						                                                    ^
					The local variable p is hiding another local variable defined in an enclosing scope
					----------
					3. WARNING in X.java (at line 10)
						I i2 = new I() { public void foo(int j, int q) {int p, lamlocal;}};
						                                                       ^^^^^^^^
					The local variable lamlocal is hiding another local variable defined in an enclosing scope
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727, [1.8][compiler] Lambda expression parameters and locals cannot shadow variables from context
public void test096() {
	// local of class under lambda redeclares its own argument & local
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
						       int lamlocal;
						           I i2 = new I() { public void foo(int x1, int x2) {int x1, x2;}};
								};
							}\t
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						I i2 = new I() { public void foo(int x1, int x2) {int x1, x2;}};
						                                                      ^^
					Duplicate local variable x1
					----------
					2. ERROR in X.java (at line 10)
						I i2 = new I() { public void foo(int x1, int x2) {int x1, x2;}};
						                                                          ^^
					Duplicate local variable x2
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384687 [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
public void test097() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"class Action<K> {\r\n" +
			"  static <T1> int fooMethod(Object x) { return 0; }\r\n" +
			"}\r\n" +
			"interface I {\r\n" +
			"  int foo(Object x);\r\n" +
			"}\r\n" +
			"public class X {\r\n" +
			"  public static void main(String[] args) {\r\n" +
			"    I functional = Action::<?>fooMethod;\r\n" + // no raw type warning here, Action:: is really Action<>::
			"  }\r\n" +
			"}"},
			"""
				----------
				1. ERROR in X.java (at line 9)
					I functional = Action::<?>fooMethod;
					                        ^
				Wildcard is not allowed at this location
				----------
				""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=384687 [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
public void test098() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"""
				class Action<K> {\r
				  int foo(Object x, Object y, Object z) { return 0; }\r
				}\r
				interface I {\r
				  void foo(Object x);\r
				}\r
				public class X {\r
				  public static void main(String[] args) {\r
				    Action<Object> exp = new Action<Object>();\r
				    int x,y,z;\r
				    I len6 = foo->exp.<?>method(x, y, z);\r
				  }\r
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 11)
					I len6 = foo->exp.<?>method(x, y, z);
					                   ^
				Wildcard is not allowed at this location
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399770: [1.8][compiler] Implement support for @FunctionalInterface
public void test_bug399770_1() {
	this.runConformTest(
			new String[] {
					"YYY.java",
					"""
						interface BASE { void run(); }
						@FunctionalInterface
						interface DERIVED extends BASE {void run();}\
						public class YYY {
							public static void main(String[] args) {
								System.out.println("Hello");\
							}
						}""",
			},
			"Hello"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399770: [1.8][compiler] Implement support for @FunctionalInterface
public void test_bug399770_2() {
	this.runNegativeTest(
			new String[] {
					"YYY.java",
					"""
						interface BASE { void run(); }
						@FunctionalInterface
						interface DERIVED extends BASE {void run1();}\
						@FunctionalInterface public class YYY {
						   @FunctionalInterface int x;\
							@FunctionalInterface public static void main(String[] args) {
						       @FunctionalInterface int y;\
								System.out.println("Hello");\
							}
						}""",
			},
			"""
				----------
				1. ERROR in YYY.java (at line 3)
					interface DERIVED extends BASE {void run1();}@FunctionalInterface public class YYY {
					          ^^^^^^^
				Invalid '@FunctionalInterface' annotation; DERIVED is not a functional interface
				----------
				2. ERROR in YYY.java (at line 3)
					interface DERIVED extends BASE {void run1();}@FunctionalInterface public class YYY {
					                                                                               ^^^
				Invalid '@FunctionalInterface' annotation; YYY is not a functional interface
				----------
				3. ERROR in YYY.java (at line 4)
					@FunctionalInterface int x;	@FunctionalInterface public static void main(String[] args) {
					^^^^^^^^^^^^^^^^^^^^
				The annotation @FunctionalInterface is disallowed for this location
				----------
				4. ERROR in YYY.java (at line 4)
					@FunctionalInterface int x;	@FunctionalInterface public static void main(String[] args) {
					                           	^^^^^^^^^^^^^^^^^^^^
				The annotation @FunctionalInterface is disallowed for this location
				----------
				5. ERROR in YYY.java (at line 5)
					@FunctionalInterface int y;		System.out.println("Hello");	}
					^^^^^^^^^^^^^^^^^^^^
				The annotation @FunctionalInterface is disallowed for this location
				----------
				"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400745, [1.8][compiler] Compiler incorrectly allows shadowing of local class names.
public void test400745() {
	// Lambda redeclares a local class from its outer scope.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo();
						}
						public class X {
							public void foo() {
								class Y {};
								I i = ()  -> {
									class Y{} ;
								};
							}
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					class Y{} ;
					      ^
				Duplicate nested type Y
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400745, [1.8][compiler] Compiler incorrectly allows shadowing of local class names.
public void test400745a() {
	// local type hiding scenario
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void foo();
						}
						public class X {
							private void foo() {
								class Y {}
								X x = new X() {
									private void foo() {
										class Y {};
									}
								};
								I i = () -> {
									class LX {
										void foo() {
											class Y {};
										}
									};
								};
							}
						}
						""",
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					private void foo() {
					             ^^^^^
				The method foo() from the type X is never used locally
				----------
				2. WARNING in X.java (at line 6)
					class Y {}
					      ^
				The type Y is never used locally
				----------
				3. WARNING in X.java (at line 8)
					private void foo() {
					             ^^^^^
				The method foo() from the type new X(){} is never used locally
				----------
				4. WARNING in X.java (at line 9)
					class Y {};
					      ^
				The type Y is hiding the type Y
				----------
				5. WARNING in X.java (at line 9)
					class Y {};
					      ^
				The type Y is never used locally
				----------
				6. WARNING in X.java (at line 13)
					class LX {
					      ^^
				The type LX is never used locally
				----------
				7. WARNING in X.java (at line 14)
					void foo() {
					     ^^^^^
				The method foo() from the type LX is never used locally
				----------
				8. WARNING in X.java (at line 15)
					class Y {};
					      ^
				The type Y is hiding the type Y
				----------
				9. WARNING in X.java (at line 15)
					class Y {};
					      ^
				The type Y is never used locally
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						public interface I<P extends ParameterType> {
							<T extends ExceptionType , R extends ReturnType> R doit(P p) throws T;
						}
						
						class ReturnType {
						}
						
						class ParameterType {
						}
						
						class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I i = (p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\I.java (at line 12)
					class ExceptionType extends Exception {
					      ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					I i = (p) -> { return null; };
					^
				I is a raw type. References to generic type I<P> should be parameterized
				----------
				2. ERROR in X.java (at line 3)
					I i = (p) -> { return null; };
					      ^^^^^^
				The type ReturnType from the descriptor computed for the target context is not visible here. \s
				----------
				3. ERROR in X.java (at line 3)
					I i = (p) -> { return null; };
					      ^^^^^^
				The type ParameterType from the descriptor computed for the target context is not visible here. \s
				----------
				4. ERROR in X.java (at line 3)
					I i = (p) -> { return null; };
					      ^^^^^^
				The type ExceptionType from the descriptor computed for the target context is not visible here. \s
				----------
				""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556a() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						public interface I<P extends ParameterType> {
							<T extends ExceptionType , R extends ReturnType> R doit(P p) throws T;
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						
						class ParameterType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I i = (p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					I i = (p) -> { return null; };
					^
				I is a raw type. References to generic type I<P> should be parameterized
				----------
				2. ERROR in X.java (at line 3)
					I i = (p) -> { return null; };
					      ^^^^^^
				The type ParameterType from the descriptor computed for the target context is not visible here. \s
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556b() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType> {
							<T extends ExceptionType , R extends ReturnType> R doit(List<? extends List<P>>[] p) throws T;
						}
						
						class ReturnType {
						}
						
						class ParameterType {
						}
						
						class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I i = (p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\I.java (at line 13)
					class ExceptionType extends Exception {
					      ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					I i = (p) -> { return null; };
					^
				I is a raw type. References to generic type I<P> should be parameterized
				----------
				2. ERROR in X.java (at line 3)
					I i = (p) -> { return null; };
					      ^^^^^^
				The type ReturnType from the descriptor computed for the target context is not visible here. \s
				----------
				3. ERROR in X.java (at line 3)
					I i = (p) -> { return null; };
					      ^^^^^^
				The type ExceptionType from the descriptor computed for the target context is not visible here. \s
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556c() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						class ParameterType {
						}
						class ReturnType {
						}
						class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I<?, ?, ?> i = (p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\I.java (at line 10)
					class ExceptionType extends Exception {
					      ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. ERROR in X.java (at line 3)
					I<?, ?, ?> i = (p) -> { return null; };
					               ^^^^^^
				The type ReturnType from the descriptor computed for the target context is not visible here. \s
				----------
				2. ERROR in X.java (at line 3)
					I<?, ?, ?> i = (p) -> { return null; };
					               ^^^^^^
				The type ParameterType from the descriptor computed for the target context is not visible here. \s
				----------
				3. ERROR in X.java (at line 3)
					I<?, ?, ?> i = (p) -> { return null; };
					               ^^^^^^
				The type ExceptionType from the descriptor computed for the target context is not visible here. \s
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556d() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I<?, ?, ?> i = (p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556e() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I<?, ?, ?> i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. ERROR in X.java (at line 3)
					I<?, ?, ?> i = (String p) -> { return null; };
					                ^^^^^^
				Lambda expression's parameter p is expected to be of type List<? extends List<ParameterType>>[]
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556f() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I<? extends p.ParameterType, ? extends p.ExceptionType, ? extends p.ReturnType> i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. ERROR in X.java (at line 3)
					I<? extends p.ParameterType, ? extends p.ExceptionType, ? extends p.ReturnType> i = (String p) -> { return null; };
					                                                                                     ^^^^^^
				Lambda expression's parameter p is expected to be of type List<? extends List<ParameterType>>[]
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556g() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						class P extends p.ParameterType {}
						class T extends p.ExceptionType {}
						class R extends p.ReturnType {}
						public class X {
							I<P, T, R> i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					class T extends p.ExceptionType {}
					      ^
				The serializable class T does not declare a static final serialVersionUID field of type long
				----------
				2. ERROR in X.java (at line 6)
					I<P, T, R> i = (String p) -> { return null; };
					                ^^^^^^
				Lambda expression's parameter p is expected to be of type List<? extends List<P>>[]
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556h() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						class P extends p.ParameterType {}
						class T extends p.ExceptionType {}
						class R extends p.ReturnType {}
						public class X {
							I<T, R, P> i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					class T extends p.ExceptionType {}
					      ^
				The serializable class T does not declare a static final serialVersionUID field of type long
				----------
				2. ERROR in X.java (at line 6)
					I<T, R, P> i = (String p) -> { return null; };
					  ^
				Bound mismatch: The type T is not a valid substitute for the bounded parameter <P extends ParameterType> of the type I<P,T,R>
				----------
				3. ERROR in X.java (at line 6)
					I<T, R, P> i = (String p) -> { return null; };
					     ^
				Bound mismatch: The type R is not a valid substitute for the bounded parameter <T extends ExceptionType> of the type I<P,T,R>
				----------
				4. ERROR in X.java (at line 6)
					I<T, R, P> i = (String p) -> { return null; };
					        ^
				Bound mismatch: The type P is not a valid substitute for the bounded parameter <R extends ReturnType> of the type I<P,T,R>
				----------
				5. ERROR in X.java (at line 6)
					I<T, R, P> i = (String p) -> { return null; };
					               ^^^^^^^^^^^^^
				The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556i() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						class P extends p.ParameterType {}
						class T extends p.ExceptionType {}
						class R extends p.ReturnType {}
						public class X {
							I<? super P, ? super T, ? super R> i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					class T extends p.ExceptionType {}
					      ^
				The serializable class T does not declare a static final serialVersionUID field of type long
				----------
				2. ERROR in X.java (at line 6)
					I<? super P, ? super T, ? super R> i = (String p) -> { return null; };
					                                        ^^^^^^
				Lambda expression's parameter p is expected to be of type List<? extends List<P>>[]
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556j() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends P , R extends T> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						class P extends p.ParameterType {}
						class T extends p.ExceptionType {}
						class R extends p.ReturnType {}
						public class X {
							I<?, ?, ?> i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. ERROR in p\\I.java (at line 4)
					R doit(List<? extends List<P>>[] p) throws T;
					                                           ^
				No exception of type T can be thrown; an exception type must be a subclass of Throwable
				----------
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					class T extends p.ExceptionType {}
					      ^
				The serializable class T does not declare a static final serialVersionUID field of type long
				----------
				2. ERROR in X.java (at line 6)
					I<?, ?, ?> i = (String p) -> { return null; };
					               ^^^^^^^^^^^^^
				The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400556, [1.8][compiler] Visibility checks are missing for lambda/reference expressions
public void test400556k() {
	this.runNegativeTest(
			new String[] {
					"p/I.java",
					"""
						package p;
						import java.util.List;
						public interface I<P extends ParameterType, T extends ExceptionType , R extends ReturnType> {
							R doit(List<? extends List<P>>[] p) throws T;
						}
						""",
					"p/ParameterType.java",
					"""
						package p;
						public class ParameterType {
						}
						""",
					"p/ReturnType.java",
					"""
						package p;
						public class ReturnType {
						}
						""",
					"p/ExceptionType.java",
					"""
						package p;
						public class ExceptionType extends Exception {
						}
						""",
					"X.java",
					"""
						import p.I;
						public class X {
							I i = (String p) -> { return null; };
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\ExceptionType.java (at line 2)
					public class ExceptionType extends Exception {
					             ^^^^^^^^^^^^^
				The serializable class ExceptionType does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in X.java (at line 3)
					I i = (String p) -> { return null; };
					^
				I is a raw type. References to generic type I<P,T,R> should be parameterized
				----------
				2. ERROR in X.java (at line 3)
					I i = (String p) -> { return null; };
					       ^^^^^^
				Lambda expression's parameter p is expected to be of type List[]
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void zoo(int x, String p);
						}
						public class X {
							int x = 0;
							I i = x::zoo;
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = x::zoo;
					      ^
				Cannot invoke zoo(int, String) on the primitive type int
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750a() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void zoo(int x, String p);
						}
						public class X {
							int x = 0;
							I i = I::new;
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = I::new;
					      ^
				Cannot instantiate the type I
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750b() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void zoo(int x, String p);
						}
						abstract public class X {
							int x = 0;
							I i = X::new;
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = X::new;
					      ^
				Cannot instantiate the type X
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750c() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void zoo(int x, String p);
						}
						abstract public class X {
							int x = 0;
							I i = E::new;
						}
						enum E {}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = E::new;
					      ^
				Cannot instantiate the type E
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750d() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> {
							X(int x, String p) {}
							I i = X<? extends String>::new;
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = X<? extends String>::new;
					      ^^^^^^^^^^^^^^^^^^^
				Cannot instantiate the type X<? extends String>
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750e() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> {
							X(int x, String p) {}
							I i = T::new;
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = T::new;
					      ^
				Cannot instantiate the type T
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750f() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> {
							X(int x, String p) {}
							I i = Annot::new;
						}
						@interface Annot {}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I i = Annot::new;
					      ^^^^^
				Cannot instantiate the type Annot
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750g() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> {
							X(int x, String p) {}
						   static {
							    I i = this::foo;
						   }
						   X<?> foo(int x, String p) { return null; }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					I i = this::foo;
					      ^^^^
				Cannot use this in a static context
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750h() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> {
							X(int x, String p) {}
							I i = this::foo;
						   X<?> foo(int x, String p) { return null; }
						}
						"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750i() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> extends Y {
						   static {
							    I i = super::foo;
						   }
						}
						class Y {
						    X<?> foo(int x, String p) { return null; }
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 6)
							I i = super::foo;
							      ^^^^^
						Cannot use super in a static context
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750j() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
						  X<?> zoo(int x, String p);
						}
						public class X<T> extends Y {
							I i = super::foo;
						}
						class Y {
						    X<?> foo(int x, String p) { return null; }
						}
						"""
					},
					"""
						----------
						1. WARNING in X.java (at line 5)
							I i = super::foo;
							      ^^^^^^^^^^
						Access to enclosing method foo(int, String) from the type Y is emulated by a synthetic accessor method
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750k() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void zoo();
						}
						class Z {
							void zoo() {}
						}
						class X extends Z {
						    static class N {
						    	I i = X.super::zoo;
						    }
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i = X.super::zoo;
							      ^^^^^^^
						No enclosing instance of the type X is accessible in scope
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750l() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
						  void zoo();
						}
						class Z {
							void zoo() {}
						}
						class X extends Z {
						    class N {
						    	I i = X.super::zoo;
						    }
						}
						"""
					},
					"""
						----------
						1. WARNING in X.java (at line 9)
							I i = X.super::zoo;
							      ^^^^^^^^^^^^
						Access to enclosing method zoo() from the type Z is emulated by a synthetic accessor method
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750m() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						import java.util.List;
						interface I {
							List<String> doit();
						}
						interface J {
							int size(ArrayList<String> als);
						}
						class X {
						   I i1 = ArrayList::new;
						   I i2 = ArrayList<String>::new;
						   I i3 = ArrayList<Integer>::new;
						   I i4 = List<String>::new;
						   J j1 = String::length;
						   J j2 = List::size;
						   J j3 = List<String>::size;
						   J j4 = List<Integer>::size;
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 12)
							I i3 = ArrayList<Integer>::new;
							       ^^^^^^^^^^^^^^^^^^^^^^^
						The constructed object of type ArrayList<Integer> is incompatible with the descriptor's return type: List<String>
						----------
						2. ERROR in X.java (at line 13)
							I i4 = List<String>::new;
							       ^^^^^^^^^^^^
						Cannot instantiate the type List<String>
						----------
						3. ERROR in X.java (at line 14)
							J j1 = String::length;
							       ^^^^^^^^^^^^^^
						The type String does not define length(ArrayList<String>) that is applicable here
						----------
						4. ERROR in X.java (at line 17)
							J j4 = List<Integer>::size;
							       ^^^^^^^^^^^^^^^^^^^
						The type List<Integer> does not define size(ArrayList<String>) that is applicable here
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750n() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						import java.util.List;
						interface I {
							List<String> doit();
						}
						public class X {
						   I i1 = ArrayList<String>[]::new;
						   I i2 = List<String>[]::new;
						   I i3 = ArrayList<String>::new;
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 7)
							I i1 = ArrayList<String>[]::new;
							       ^^^^^^^^^^^^^^^^^^^^^^^^
						Cannot create a generic array of ArrayList<String>
						----------
						2. ERROR in X.java (at line 8)
							I i2 = List<String>[]::new;
							       ^^^^^^^^^^^^^^^^^^^
						Cannot create a generic array of List<String>
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750o() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						import java.util.List;
						interface I {
							List<String> [] doit();
						}
						interface J {
							List<String> [] doit(long l);
						}
						interface K {
							List<String> [] doit(String s, long l);
						}
						interface L {
							List<String> [] doit(short s);
						}
						interface M {
							List<String> [] doit(byte b);
						}
						interface N {
							List<String> [] doit(int i);
						}
						interface O {
							List<String> [] doit(Integer i);
						}
						interface P {
							List<String> [] doit(Short i);
						}
						interface Q {
							List<String> [] doit(Float i);
						}
						interface R {
							List<String> [] doit(int i);
						}
						public class X {
						   I i = List[]::new;
						   J j = ArrayList[]::new;
						   K k = ArrayList[]::new;
						   L l = ArrayList[]::new;
						   M m = ArrayList[]::new;
						   N n = ArrayList[]::new;
						   O o = ArrayList[]::new;
						   P p = ArrayList[]::new;
						   Q q = ArrayList[]::new;
						   R r = ArrayList[][][]::new;
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 34)
							I i = List[]::new;
							      ^^^^^^^^^^^
						Incompatible parameter list for array constructor. Expected (int), but found ()
						----------
						2. ERROR in X.java (at line 35)
							J j = ArrayList[]::new;
							      ^^^^^^^^^^^^^^^^
						Incompatible parameter list for array constructor. Expected (int), but found (long)
						----------
						3. ERROR in X.java (at line 36)
							K k = ArrayList[]::new;
							      ^^^^^^^^^^^^^^^^
						Incompatible parameter list for array constructor. Expected (int), but found (String, long)
						----------
						4. ERROR in X.java (at line 42)
							Q q = ArrayList[]::new;
							      ^^^^^^^^^^^^^^^^
						Incompatible parameter list for array constructor. Expected (int), but found (Float)
						----------
						5. ERROR in X.java (at line 43)
							R r = ArrayList[][][]::new;
							      ^^^^^^^^^^^^^^^^^^^^
						Constructed array ArrayList[][][] cannot be assigned to List<String>[] as required in the interface descriptor \s
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750p() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit();
						}
						class X<T> {
							static void foo() {}
							{
								X<String> x = new X<String>();
								I i1 = x::foo;
								I i2 = X<String>::foo;
								I i3 = X::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i1 = x::foo;
							       ^^^^^^
						The method foo() from the type X<String> should be accessed in a static way\s
						----------
						2. ERROR in X.java (at line 10)
							I i2 = X<String>::foo;
							       ^^^^^^^^^^^^^^
						The method foo() from the type X<String> should be accessed in a static way\s
						----------
						3. ERROR in X.java (at line 11)
							I i3 = X::foo;
							       ^^^^^^
						The type of foo() from the type X is void, this is incompatible with the descriptor's return type: List<String>
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750q() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit();
						}
						class X<T> {
							void foo() {}
							{
								X<String> x = new X<String>();
								I i1 = x::foo;
								I i2 = X<String>::foo;
								I i3 = X::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i1 = x::foo;
							       ^^^^^^
						The type of foo() from the type X<String> is void, this is incompatible with the descriptor's return type: List<String>
						----------
						2. ERROR in X.java (at line 10)
							I i2 = X<String>::foo;
							       ^^^^^^^^^^^^^^
						Cannot make a static reference to the non-static method foo() from the type X<String>
						----------
						3. ERROR in X.java (at line 11)
							I i3 = X::foo;
							       ^^^^^^
						Cannot make a static reference to the non-static method foo() from the type X
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750r() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit(X<String> xs);
						}
						class X<T> {
							void foo() {}
							{
								X<String> x = new X<String>();
								I i1 = X::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i1 = X::foo;
							       ^^^^^^
						The type of foo() from the type X<String> is void, this is incompatible with the descriptor's return type: List<String>
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750s() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit(X<String> xs);
						}
						class X<T> {
							void foo() {}
							{
								X<String> x = new X<String>();
								I i1 = X<String>::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i1 = X<String>::foo;
							       ^^^^^^^^^^^^^^
						The type of foo() from the type X<String> is void, this is incompatible with the descriptor's return type: List<String>
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750t() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit(X<String> xs);
						}
						class X<T> {
							static List<String> foo() {}
							{
								X<String> x = new X<String>();
								I i1 = X<String>::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i1 = X<String>::foo;
							       ^^^^^^^^^^^^^^
						The method foo() from the type X<String> should be accessed in a static way\s
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750u() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit(X<String> xs, int x);
						}
						class X<T> {
							static List<String> foo() {}
							{
								X<String> x = new X<String>();
								I i1 = X::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 9)
							I i1 = X::foo;
							       ^^^^^^
						The type X does not define foo(X<String>, int) that is applicable here
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750v() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit(X<String> xs, int x);
						}
						class X<T> {
							static List<String> foo(X<String> xs, int x) {}
							List<String> foo(int x) {}
							{
								X<String> x = new X<String>();
								I i1 = X::foo;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 10)
							I i1 = X::foo;
							       ^^^^^^
						Ambiguous method reference: both foo(int) and foo(X<String>, int) from the type X<String> are eligible
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750w() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							X<String> doit(int x);
						}
						interface J {
							X<String> doit(int x, int p);
						}
						interface K {
							X<String> doit(int x);
						   void goo();
						}
						interface L {
							X<String> doit(short x);
						}
						interface M {
							X<String> doit(String s);
						}
						class X<T> {
							X(int x, int y) {}
							X(int x) {}
							{
								I i = X::new;
						       J j = X<Integer>::new;
						       K k = X::new;
						       L l = X<String>::new;
						       M m = X<String>::new;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 23)
							J j = X<Integer>::new;
							      ^^^^^^^^^^^^^^^
						The constructed object of type X<Integer> is incompatible with the descriptor's return type: X<String>
						----------
						2. ERROR in X.java (at line 24)
							K k = X::new;
							      ^^^^^^
						The target type of this expression must be a functional interface
						----------
						3. ERROR in X.java (at line 26)
							M m = X<String>::new;
							      ^^^^^^^^^^^^^^
						The type X<String> does not define X(String) that is applicable here
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750x() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						import java.io.IOException;
						import java.io.FileNotFoundException;
						interface I {
							X<String> doit(int x);
						}
						interface J {
							X<String> doit(int x) throws IOException;
						}
						interface K {
							X<String> doit(int x) throws FileNotFoundException;
						}
						interface L {
							X<String> doit(short x) throws Exception;
						}
						class X<T> {
							X(int x) throws IOException, FileNotFoundException {}
							{
								I i = X::new;
						       J j = X<Integer>::new;
						       K k = X::new;
						       L l = X<String>::new;
							}
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 19)
							I i = X::new;
							      ^^^^^^
						Unhandled exception type IOException
						----------
						2. ERROR in X.java (at line 19)
							I i = X::new;
							      ^^^^^^
						Unhandled exception type FileNotFoundException
						----------
						3. ERROR in X.java (at line 20)
							J j = X<Integer>::new;
							      ^^^^^^^^^^^^^^^
						The constructed object of type X<Integer> is incompatible with the descriptor's return type: X<String>
						----------
						4. ERROR in X.java (at line 21)
							K k = X::new;
							      ^^^^^^
						Unhandled exception type IOException
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750y() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							void doit();
						}
						abstract class Y {
						    abstract void foo();
						}
						class X extends Y {
							void foo() {}
						   I i = super::foo;
						}
						"""
					},
					"""
						----------
						1. ERROR in X.java (at line 10)
							I i = super::foo;
							      ^^^^^^^^^^
						Cannot directly invoke the abstract method foo() for the type Y
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.WARNING);
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						class Y {
						    static void foo() {}
						}
						class X extends Y {
						   I i = X::foo;
						}
						"""
					},
					"""
						----------
						1. WARNING in X.java (at line 8)
							I i = X::foo;
							      ^^^^^^
						The static method foo() from the type Y should be accessed directly\s
						----------
						""",
					null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z1() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						class X extends Y {
						   I i = X::foo;
						}
						""",
					"Y.java",
					"""
						@Deprecated class Y {
						    @Deprecated static void foo() {}
						}
						"""
					},
					"""
						----------
						1. WARNING in X.java (at line 4)
							class X extends Y {
							                ^
						The type Y is deprecated
						----------
						2. WARNING in X.java (at line 5)
							I i = X::foo;
							      ^^^^^^
						The method foo() from the type Y is deprecated
						----------
						""",
					null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z2() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeArgumentsForMethodInvocation, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						class X extends Y {
						   I i = X::<String>foo;
						}
						""",
					"Y.java",
					"""
						@Deprecated class Y {
						    @Deprecated static void foo() {}
						}
						"""
					},
					"""
						----------
						1. WARNING in X.java (at line 4)
							class X extends Y {
							                ^
						The type Y is deprecated
						----------
						2. WARNING in X.java (at line 5)
							I i = X::<String>foo;
							      ^^^^^^^^^^^^^^
						The method foo() from the type Y is deprecated
						----------
						3. WARNING in X.java (at line 5)
							I i = X::<String>foo;
							          ^^^^^^
						Unused type arguments for the non generic method foo() of type Y; it should not be parameterized with arguments <String>
						----------
						""",
					null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z3() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						class X {
						   String foo() { return null; }
						   I i = new X()::foo;
						}
						""",
					},
					"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z4() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						interface I {
							List<String> doit();
						}
						class X {
						   void foo() { return; }
						   I i = new X()::foo;
						}
						""",
					},
					"""
						----------
						1. ERROR in X.java (at line 7)
							I i = new X()::foo;
							      ^^^^^^^^^^^^
						The type of foo() from the type X is void, this is incompatible with the descriptor's return type: List<String>
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z5() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						import java.util.ArrayList;
						interface I {
							List<String> doit();
						}
						class X {
						   ArrayList<Integer> foo() { return null; }
						   I i = new X()::foo;
						}
						""",
					},
					"""
						----------
						1. ERROR in X.java (at line 8)
							I i = new X()::foo;
							      ^^^^^^^^^^^^
						The type of foo() from the type X is ArrayList<Integer>, this is incompatible with the descriptor's return type: List<String>
						----------
						""");
}
//  https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z6() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.DEFAULT,
			new String[] {
					"X.java",
					"""
						import java.util.List;
						import java.util.ArrayList;
						interface I {
							List<String> doit(X<String> x, int y);
						}
						class X<T> {
						   ArrayList<String> foo(int x) { return null; }
						   I i = X::foo;
						}
						""",
					},
					"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z7() {
this.runNegativeTest(
		false /* skipJavac */,
		new JavacTestOptions("-Xlint:rawtypes"),
		new String[] {
				"X.java",
				"""
					import java.util.List;
					import java.util.ArrayList;
					interface I {
						List<String> doit(X x, int y);
					}
					class X<T> {
					   ArrayList<Integer> foo(int x) { return null; }
					   I i = X::foo;
					}
					""",
				},
				"""
					----------
					1. WARNING in X.java (at line 4)
						List<String> doit(X x, int y);
						                  ^
					X is a raw type. References to generic type X<T> should be parameterized
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z8() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						int [] doit(int [] ia);
					}
					class X<T> {
					   I i = int []::clone;
					}
					""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384750, [1.8] Compiler should reject invalid method reference expressions
public void test384750z9() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						int [] doit(X x);
					}
					public class X {
						Zork foo() {
						}
					   I i = X::foo;
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						Zork foo() {
						^^^^
					Zork cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 7)
						I i = X::foo;
						      ^^^^^^
					The method foo() from the type X refers to the missing type Zork
					----------
					3. ERROR in X.java (at line 7)
						I i = X::foo;
						      ^^^^^^
					The type of foo() from the type X is Zork, this is incompatible with the descriptor's return type: int[]
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610() {
this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface I {
					    void foo();
					}
					public class X {
						void foo(I i) {
							System.out.println("foo");
						}
						public static void main(String[] args) {
							new X().foo(()->{});
						}
					}
					""",
				},
				"foo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610a() {
this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface I {
					    void foo();
					}
					interface J {
					    void foo(int x, int y);
					}
					interface K {
					    void foo(String s);
					}
					public class X {
						void foo(I i) {
							System.out.println("foo(I)");
						}
						void foo(J j) {
							System.out.println("foo(J)");
						}
						void foo(K k) {
							System.out.println("foo(K)");
						}
						public static void main(String[] args) {
							new X().foo(()->{});
							new X().foo((x, y)->{});
							new X().foo((s)->{});
						}
					}
					""",
				},
				"""
					foo(I)
					foo(J)
					foo(K)""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610b() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
					    void foo();
					}
					interface J {
					    void foo(int x, int y);
					}
					interface K {
					    void foo(String s);
					}
					public class X {
						void foo(I i) {
							System.out.println("foo(I)");
						}
						void foo(J j) {
							System.out.println("foo(J)");
						}
						public static void main(String[] args) {
							new X().foo(()->{});
							new X().foo((x, y)->{});
							new X().foo((s)->{});
						}
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 20)
						new X().foo((s)->{});
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments ((<no type> s) -> {})
					----------
					2. ERROR in X.java (at line 20)
						new X().foo((s)->{});
						            ^^^^^
					Lambda expression's signature does not match the signature of the functional interface method foo()
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610c() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
					    void foo();
					}
					interface K {
					    String foo(String s);
					}
					public class X {
						void foo(I i) {
							System.out.println("foo(I)");
						}
						void foo(K k) {
							System.out.println("foo(K)");
						}
						public static void main(String[] args) {
							new X().foo(()->{ return "";});
							new X().foo(()-> 10);
							new X().foo((s)->{});
							new X().foo((s)->{ return;});
							new X().foo((s)->{ return "";});
							new X().foo((s)-> "hello");
							new X().foo(()->{ return;});
							new X().foo(()-> System.out.println());
						}
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 15)
						new X().foo(()->{ return "";});
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments (() -> {})
					----------
					2. ERROR in X.java (at line 15)
						new X().foo(()->{ return "";});
						                  ^^^^^^^^^^
					Void methods cannot return a value
					----------
					3. ERROR in X.java (at line 16)
						new X().foo(()-> 10);
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments (() -> {})
					----------
					4. ERROR in X.java (at line 16)
						new X().foo(()-> 10);
						                 ^^
					Void methods cannot return a value
					----------
					5. ERROR in X.java (at line 17)
						new X().foo((s)->{});
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments ((<no type> s) -> {})
					----------
					6. ERROR in X.java (at line 17)
						new X().foo((s)->{});
						            ^^^^^
					Lambda expression's signature does not match the signature of the functional interface method foo()
					----------
					7. ERROR in X.java (at line 18)
						new X().foo((s)->{ return;});
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments ((<no type> s) -> {})
					----------
					8. ERROR in X.java (at line 18)
						new X().foo((s)->{ return;});
						            ^^^^^
					Lambda expression's signature does not match the signature of the functional interface method foo()
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610d() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    <T> T id(T arg) { return arg; }
					    Runnable r = id(() -> { System.out.println(); });
					}
					""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610e() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I<T extends String> {
						void foo();
					}
					public class X {
						<T> T foo(I<T> it) { return null; }
						public static void main(String[] args) {
							new X().foo(()->{});
						}
					}
					""",
				},
				"""
					----------
					1. WARNING in X.java (at line 1)
						interface I<T extends String> {
						                      ^^^^^^
					The type parameter T should not be bounded by the final type String. Final types cannot be further extended
					----------
					2. ERROR in X.java (at line 5)
						<T> T foo(I<T> it) { return null; }
						            ^
					Bound mismatch: The type T is not a valid substitute for the bounded parameter <T extends String> of the type I<T>
					----------
					3. ERROR in X.java (at line 7)
						new X().foo(()->{});
						        ^^^
					The method foo(I<Object>) in the type X is not applicable for the arguments (() -> {})
					----------
					4. ERROR in X.java (at line 7)
						new X().foo(()->{});
						            ^^^^
					The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
					----------
					""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
// demonstrate that the bound problem is the only real issue in test401610e()
public void test401610ee() {
this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		new String[] {
				"X.java",
				"""
					interface I<T extends String> {
						void foo();
					}
					public class X {
						<T extends String> T foo(I<T> it) { return null; }
						public static void main(String[] args) {
							new X().foo(()->{});
						}
					}
					""",
				},
				"""
					----------
					1. WARNING in X.java (at line 1)
						interface I<T extends String> {
						                      ^^^^^^
					The type parameter T should not be bounded by the final type String. Final types cannot be further extended
					----------
					2. WARNING in X.java (at line 5)
						<T extends String> T foo(I<T> it) { return null; }
						           ^^^^^^
					The type parameter T should not be bounded by the final type String. Final types cannot be further extended
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610f() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I<T> {
						void foo();
					}
					public class X {
						<T> T foo(I<T> it) { return null; }
						public static void main(String[] args) {
							new X().foo(()->{});
						}
					}
					""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610g() {
this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {\s
					    String foo();
					}
					public class X {
						void foo(I it) { System.out.println("foo(I)");}
						void foo(J it) { System.out.println("foo(J)");}
						public static void main(String[] args) {
							new X().foo(()->{});
						}
					}
					""",
				},
				"foo(I)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610h() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						void foo();
					}
					interface J {\s
					    String foo();
					}
					public class X {
						void foo(I it) { System.out.println("foo(I)");}
						void foo(J it) { System.out.println("foo(J)");}
						public static void main(String[] args) {
							new X().foo(()->{ return 10; });
						}
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						new X().foo(()->{ return 10; });
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments (() -> {})
					----------
					2. ERROR in X.java (at line 11)
						new X().foo(()->{ return 10; });
						                  ^^^^^^^^^^
					Void methods cannot return a value
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610i() {
this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {\s
					    void foo(String s);
					}
					public class X {
						void foo(I it) { System.out.println("foo(I)");}
						void foo(J it) { System.out.println("foo(J)");}
						public static void main(String[] args) {
							new X().foo((String s)->{});
						}
					}
					""",
				},
				"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401610, [1.8][compiler] Allow lambda/reference expressions in non-overloaded method invocation contexts
public void test401610j() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					interface J {\s
					    void foo(String s);
					}
					public class X {
						void foo(I it) { System.out.println("foo(I)");}
						void foo(J it) { System.out.println("foo(J)");}
						public static void main(String[] args) {
							new X().foo((Object o)->{});
						}
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						new X().foo((Object o)->{});
						        ^^^
					The method foo(I) in the type X is not applicable for the arguments ((Object o) -> {})
					----------
					2. ERROR in X.java (at line 11)
						new X().foo((Object o)->{});
						             ^^^^^^
					Lambda expression's parameter o is expected to be of type int
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401789, [1.8][compiler] Enable support for method/constructor references in non-overloaded method calls.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401790, Follow up of bug 401610, explicit constructor calls and allocation expressions needs updates too.
public void test401789_401790() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X {
						int foo(I i) { return 10;}
						class Y {
							Y (I i) {}
							Y() {
								this(X::goo);
							}
						}
						X(I i) {}
						X() {
							this((x) -> { return 10;});
						}
						int goo() { return 0;}
						{
							foo(X::goo);
							new X((x)->{ return 10;});
							new X((x)->{ return 10;}).new Y((x) -> { return 0;});
							new X((x)->{ return 10;}) {};
						}
					}
					class Z extends X {
						Z() {
							super(X::goo);
						}
						Z(int i) {
							super (x -> 10);
						}
					   Zork z;
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 31)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401789, [1.8][compiler] Enable support for method/constructor references in non-overloaded method calls.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401790, Follow up of bug 401610, explicit constructor calls and allocation expressions needs updates too.
public void test401789_401790a() {
this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						String foo(X x);
					}
					public class X {
						int foo(I i) { return 10;}
						class Y {
							Y (I i) {}
							Y() {
								this(X::goo);
							}
						}
						X(I i) {}
						X() {
							this((x) -> { return 10;});
						}
						int goo() { return 0;}
						{
							foo(X::goo);
							new X((x)->{ return 10;});
							new X((x)->{ return 10;}).new Y((x) -> { return 0;});
							new X((x)->{ return 10;}) {};
						}
					}
					class Z extends X {
						Z() {
							super(X::goo);
						}
						Z(int i) {
							super (x -> 10);
						}
					   Zork z;
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						this(X::goo);
						^^^^^^^^^^^^^
					The constructor X.Y(X::goo) is undefined
					----------
					2. ERROR in X.java (at line 9)
						this(X::goo);
						     ^^^^^^
					The type of goo() from the type X is int, this is incompatible with the descriptor's return type: String
					----------
					3. ERROR in X.java (at line 14)
						this((x) -> { return 10;});
						                     ^^
					Type mismatch: cannot convert from int to String
					----------
					4. ERROR in X.java (at line 18)
						foo(X::goo);
						^^^
					The method foo(I) in the type X is not applicable for the arguments (X::goo)
					----------
					5. ERROR in X.java (at line 18)
						foo(X::goo);
						    ^^^^^^
					The type of goo() from the type X is int, this is incompatible with the descriptor's return type: String
					----------
					6. ERROR in X.java (at line 19)
						new X((x)->{ return 10;});
						                    ^^
					Type mismatch: cannot convert from int to String
					----------
					7. ERROR in X.java (at line 20)
						new X((x)->{ return 10;}).new Y((x) -> { return 0;});
						                    ^^
					Type mismatch: cannot convert from int to String
					----------
					8. ERROR in X.java (at line 20)
						new X((x)->{ return 10;}).new Y((x) -> { return 0;});
						                                                ^
					Type mismatch: cannot convert from int to String
					----------
					9. ERROR in X.java (at line 21)
						new X((x)->{ return 10;}) {};
						                    ^^
					Type mismatch: cannot convert from int to String
					----------
					10. ERROR in X.java (at line 26)
						super(X::goo);
						^^^^^^^^^^^^^^
					The constructor X(X::goo) is undefined
					----------
					11. ERROR in X.java (at line 26)
						super(X::goo);
						      ^^^^^^
					The type of goo() from the type X is int, this is incompatible with the descriptor's return type: String
					----------
					12. ERROR in X.java (at line 29)
						super (x -> 10);
						            ^^
					Type mismatch: cannot convert from int to String
					----------
					13. ERROR in X.java (at line 31)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X extends Zork {
						int foo(I ...i) { return 10;}
						int goo() { return 0;}
						{
							foo(X::goo);
							foo((x)-> {return 10;});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public class X extends Zork {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X {
						int foo(I [] ...i) { return 10;}
						int goo() { return 0;}
						{
							foo(X::goo);
							foo((x)-> {return 10;});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					foo(X::goo);
					^^^
				The method foo(I[]...) in the type X is not applicable for the arguments (X::goo)
				----------
				2. ERROR in X.java (at line 8)
					foo(X::goo);
					    ^^^^^^
				The target type of this expression must be a functional interface
				----------
				3. ERROR in X.java (at line 9)
					foo((x)-> {return 10;});
					^^^
				The method foo(I[]...) in the type X is not applicable for the arguments ((<no type> x) -> {})
				----------
				4. ERROR in X.java (at line 9)
					foo((x)-> {return 10;});
					    ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X extends Zork {
						X(I ...i) {}
						int goo() { return 0;}
						{
							new X(X::goo);
							new X((x)-> {return 10;});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public class X extends Zork {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X extends Zork {
						X(I ...i) {}
					   X() {
					       this((x)-> {return 10;});
					}
						int goo() { return 0;}
						{
							new X(X::goo);
							new X((x)-> {return 10;});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public class X extends Zork {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X extends Zork {
					    class Y {
					        Y(I ... i) {}
					    }
						int goo() { return 0;}
						{
							new X().new Y(X::goo);
							new X().new Y((x)-> {return 10;});
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public class X extends Zork {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401845, [1.8][compiler] Bad interaction between varargs and lambas/references
public void test401845e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X extends Zork {
						X(I ...i) {}
					   X() {
					       this((x)-> {return 10;});
					}
						int goo() { return 0;}
						{
							new X(X::goo) {};
							new X((x)-> {return 10;}){};
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public class X extends Zork {
					                       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401847, [1.8][compiler] Polyconditionals not accepted in method invocation contexts.
public void test401847() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"X.java",
				"""
					interface I {
						Integer foo(X x);
					}
					public class X {
						int foo(I ...i) { return 10;}
						int goo() { return 0;}
						{
							foo(true ? X::goo : X::goo);
							foo(true ? x-> 1 : x->0);
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 8)
					foo(true ? X::goo : X::goo);
					                    ^^^^^^
				Dead code
				----------
				2. WARNING in X.java (at line 9)
					foo(true ? x-> 1 : x->0);
					                   ^^^^
				Dead code
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401847, [1.8][compiler] Polyconditionals not accepted in method invocation contexts.
public void test401847a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String foo(X x);
					}
					public class X {
						int foo(I ...i) { return 10;}
						int goo() { return 0;}
						{
							foo(true ? X::goo : X::goo);
							foo(true ? x-> 1 : x->0);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					foo(true ? X::goo : X::goo);
					^^^
				The method foo(I...) in the type X is not applicable for the arguments ((true ? X::goo : X::goo))
				----------
				2. ERROR in X.java (at line 8)
					foo(true ? X::goo : X::goo);
					           ^^^^^^
				The type of goo() from the type X is int, this is incompatible with the descriptor's return type: String
				----------
				3. ERROR in X.java (at line 8)
					foo(true ? X::goo : X::goo);
					                    ^^^^^^
				The type of goo() from the type X is int, this is incompatible with the descriptor's return type: String
				----------
				4. ERROR in X.java (at line 9)
					foo(true ? x-> 1 : x->0);
					               ^
				Type mismatch: cannot convert from int to String
				----------
				5. ERROR in X.java (at line 9)
					foo(true ? x-> 1 : x->0);
					                      ^
				Type mismatch: cannot convert from int to String
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					class X {
						void foo(I i) {}
						I i = ()->{ throw new RuntimeException(); }; // OK
						{
							foo(()->{ throw new RuntimeException(); });
						}
					}
					""",			},
				"""
					----------
					1. WARNING in X.java (at line 5)
						void foo(I i) {}
						           ^
					The parameter i is hiding a field from type X
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo();
					}
					class X {
						void foo(I i) {}
						I i = ()->{ throw new RuntimeException(); }; // OK
						{
							foo(()->{ if (1 == 2) throw new RuntimeException(); });
						}
					}
					""",			},
				"""
					----------
					1. WARNING in X.java (at line 5)
						void foo(I i) {}
						           ^
					The parameter i is hiding a field from type X
					----------
					2. ERROR in X.java (at line 8)
						foo(()->{ if (1 == 2) throw new RuntimeException(); });
						^^^
					The method foo(I) in the type X is not applicable for the arguments (() -> {})
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    String foo(String x) throws Exception;
					}
					public class X {
						static final boolean FALSE = false;
						static final boolean TRUE = true;
						void goo(I i) {
						}
						void zoo() {
							final boolean NIJAM = true;
							final boolean POI = false;
					       final boolean BLANK;
					       BLANK = true;
							goo((x) -> { while (FALSE) throw new Exception(); });
							goo((x) -> { while (TRUE) throw new Exception(); });
							goo((x) -> { while (NIJAM) throw new Exception(); });
							goo((x) -> { while (POI) throw new Exception(); });
							goo((x) -> { if (TRUE) throw new Exception(); else throw new Exception(); });
							goo((x) -> { if (TRUE) throw new Exception(); });
							goo((x) -> { if (true) throw new Exception(); else throw new Exception(); });
							goo((x) -> { if (false) throw new Exception(); else throw new Exception(); });
							goo((x) -> { while (BLANK) throw new Exception(); });
						}
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 14)
						goo((x) -> { while (FALSE) throw new Exception(); });
						    ^^^^^^
					This lambda expression must return a result of type String
					----------
					2. ERROR in X.java (at line 17)
						goo((x) -> { while (POI) throw new Exception(); });
						    ^^^^^^
					This lambda expression must return a result of type String
					----------
					3. ERROR in X.java (at line 19)
						goo((x) -> { if (TRUE) throw new Exception(); });
						^^^
					The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {})
					----------
					4. ERROR in X.java (at line 22)
						goo((x) -> { while (BLANK) throw new Exception(); });
						    ^^^^^^
					This lambda expression must return a result of type String
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    String foo(String x) throws Exception;
					}
					public class X {
						void goo(I i) {
						}
						void zoo() {
							goo((x) -> { if (x) return null; });
							goo((x) -> {});
						}
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 8)
						goo((x) -> { if (x) return null; });
						                 ^
					Type mismatch: cannot convert from String to boolean
					----------
					2. ERROR in X.java (at line 9)
						goo((x) -> {});
						^^^
					The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {})
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939ca() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    String foo(String x) throws Exception;
					}
					public class X {
						void goo(I i) {
						}
						void zoo() {
							goo((x) -> {});
						}
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 8)
						goo((x) -> {});
						^^^
					The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {})
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    String foo(boolean x) throws Exception;
					}
					public class X {
						void goo(I i) {
						}
						void zoo() {
							goo((x) -> { if (x) return null; });
						}
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 8)
						goo((x) -> { if (x) return null; });
						    ^^^^^^
					This method must return a result of type String
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    void foo(boolean x) throws Exception;
					}
					public class X {
						void goo(I i) {
						}
						void zoo() {
							goo((x) -> { return null; });
						}
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 8)
						goo((x) -> { return null; });
						^^^
					The method goo(I) in the type X is not applicable for the arguments ((<no type> x) -> {})
					----------
					2. ERROR in X.java (at line 8)
						goo((x) -> { return null; });
						             ^^^^^^^^^^^^
					Void methods cannot return a value
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401939, [1.8][compiler] Incorrect shape analysis leads to method resolution failure .
public void test401939f() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    void foo(boolean x) throws Exception;
					}
					public class X {
						void goo(I i) {
						}
						void zoo() {
							goo((x) -> { throw new Exception(); });
						}
					}
					""",			},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402219, [1.8][compiler] Compile time errors in lambda during hypothetical type check should render candidate method inapplicable.
public void test402219() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String foo(String s1, String s2);
					}
					interface J {
						X foo(X x1, X x2);
					}
					public class X {\s
						void goo(I i) {}
						void goo(J j) {}
					    public static void main(String [] args) {
							new X().goo((p1, p2) -> p1 = p1 + p2);
					    }
					    Zork z;
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 11)
						new X().goo((p1, p2) -> p1 = p1 + p2);
						        ^^^
					The method goo(I) is ambiguous for the type X
					----------
					2. ERROR in X.java (at line 13)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402219, [1.8][compiler] Compile time errors in lambda during hypothetical type check should render candidate method inapplicable.
public void test402219a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	this.runNegativeTest(
			false /* skipJavac */,
			new JavacTestOptions("Xlint:empty"),
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(String s1, String s2);
					}
					interface J {
						X foo(X x1, X x2);
					}
					public class X {\s
						void goo(I i) {/* */}
						void goo(J j) {/* */}
					    public static void main(String [] args) {
							new X().goo((p1, p2) -> {});
					    }
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 11)
						new X().goo((p1, p2) -> {});
						                        ^^
					Empty block should be documented
					----------
					""",
			null,
			false,
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402219, [1.8][compiler] Compile time errors in lambda during hypothetical type check should render candidate method inapplicable.
public void test402219b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String foo(String s1, String s2);
					}
					interface J {
						X foo(X x1, X x2);
					}
					public class X {\s
						void goo(I i) {}
						void goo(J j) {}
					    public static void main(String [] args) {
							new X().goo((p1, p2) -> p1 + p2);
					    }
					    Zork z;
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 11)
						new X().goo((p1, p2) -> p1 + p2);
						        ^^^
					The method goo(I) is ambiguous for the type X
					----------
					2. ERROR in X.java (at line 13)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402259, [1.8][compiler] NPE during overload resolution when there are syntax errors.
public void test402259() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						J foo();
					}
					interface J {
						void foo();
					}
					public class X {
						void foo(I i) {};
						public static void main(String[] args) {
							new X().foo(() -> { return () -> { return}; });
						}
					}
					""",			},
				"""
					----------
					1. ERROR in X.java (at line 10)
						new X().foo(() -> { return () -> { return}; });
						                                   ^^^^^^
					Syntax error, insert ";" to complete BlockStatements
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes..
public void test402261() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						J foo();
					}
					interface J {
						void foo();
					}
					public class X {
						void foo(I i) {};
						public static void main(String[] args) {
							new X().foo(() -> { class local { void foo() { return; }} return () -> { return;}; });
						}
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes..
public void test402261a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						J foo();
					}
					interface J {
						void foo();
					}
					public class X {
						void foo(I i) {};
						public static void main(String[] args) {
							new X().foo(() -> { J j = () -> { return; }; return () -> { return;}; });
						}
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes..
public void test402261b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						J foo();
					}
					interface J {
						void foo();
					}
					public class X {
						void foo(I i) {};
						public static void main(String[] args) {
							new X().foo(() -> { J j = new J() { public void foo() { return; } }; return () -> { return;}; });
						}
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402261, [1.8][compiler] Shape analysis confused by returns from inner classes..
public void test402261c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						J foo();
					}
					interface J {
						void foo();
					}
					public class X {
						void foo(I i) {};
						public static void main(String[] args) {
							new X().foo(() -> { return new J() { public void foo() { return; } }; });
						}
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401769, [1.8][compiler] Explore solutions with better performance characteristics than LambdaExpression#copy()
public void test401769() {
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
					class X {
						void g(I i) {}
						void g(J j) {}
						int f;
						{
							g(() -> f++);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					g(() -> f++);
					^
				The method g(I) is ambiguous for the type X
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609() {
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
					abstract class Y {
						abstract void foo();
					}
					public class X extends Y {
						void f(I i) {}
						void f(J j) {}
					\t
						void foo() {
						}
					\t
						public static void main(String[] args) {
							f(super::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					f(super::foo);
					  ^^^^^
				Cannot use super in a static context
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609a() {
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
					abstract class Y {
						abstract void foo();
					}
					public class X extends Y {
						void f(I i) {}
					\t
						void foo() {
						}
					\t
						public void main(String[] args) {
							f(super::foo);
					       I i = super::foo;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 17)
					f(super::foo);
					  ^^^^^^^^^^
				Cannot directly invoke the abstract method foo() for the type Y
				----------
				2. ERROR in X.java (at line 18)
					I i = super::foo;
					      ^^^^^^^^^^
				Cannot directly invoke the abstract method foo() for the type Y
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609b() {
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
					abstract class Y {
						abstract void foo();
					}
					public class X extends Y {
						void f(I i) {}
						void f(J j) {}
					\t
						void foo() {
						}
					\t
						public void zoo(String[] args) {
							f(super::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					f(super::foo);
					^
				The method f(I) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 18)
					f(super::foo);
					  ^^^^^^^^^^
				Cannot directly invoke the abstract method foo() for the type Y
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402609, [1.8][compiler] AIOOB exception with a program using method references.
public void test402609c() {
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
					abstract class Y {
						void foo() {}
					}
					public class X extends Y {
						void f(I i) {}
						void f(J j) {}
					\t
						void foo() {
						}
					\t
						public void main(String[] args) {
							f(super::foo);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					f(super::foo);
					^
				The method f(I) is ambiguous for the type X
				----------
				""");
}

// 15.28:
// https://bugs.eclipse.org/382350 - [1.8][compiler] Unable to invoke inherited default method via I.super.m() syntax
public void testSuperReference01() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements I2, I1 {
					@Override
					public void print() {
						System.out.print("!");\
					}
				   void test() {
						doOutput(I1.super::print); // illegal attempt to skip I2.print()
					}
					public static void main(String... args) {
						new X().test();
					}
				   void doOutput(CanPrint printer) {
				      printer.print();\
				   }
				}
				interface CanPrint {
					void print();
				}
				interface I1 {
					default void print() {
						System.out.print("O");
					}
				}
				interface I2 extends I1 {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				doOutput(I1.super::print); // illegal attempt to skip I2.print()
				         ^^^^^^^^
			Illegal reference to super type I1, cannot bypass the more specific direct super type I2
			----------
			"""
	);
}

// 15.28.1:
// https://bugs.eclipse.org/382350 - [1.8][compiler] Unable to invoke inherited default method via I.super.m() syntax
public void testSuperReference02() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I0 {
					default void print() { System.out.println("I0"); }
				}
				
				interface IA extends I0 {}
				
				interface IB extends I0 {
					@Override default void print() {
						System.out.println("IB");
					}
				}
				public class X implements IA, IB {
					@Override
					public void print() {
						System.out.print("!");\
					}
				   void test() {
						doOutput(IA.super::print); // illegal attempt to skip IB.print()
					}
					public static void main(String... args) {
						new X().test();
					}
				   void doOutput(CanPrint printer) {
				      printer.print();\
				   }
				}
				interface CanPrint {
					void print();
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 17)
				doOutput(IA.super::print); // illegal attempt to skip IB.print()
				         ^^^^^^^^^^^^^^^
			Illegal reference to super method print() from type I0, cannot bypass the more specific override from type IB
			----------
			"""
	);
}

public void testSuperReference03() {
	this.runNegativeTest(
			new String[] {
				"XY.java",
				"""
					interface J {
						void foo(int x);
					}
					class XX {
						public  void foo(int x) {}
					}
					class Y extends XX {
						static class Z {
							public static void foo(int x) {
								System.out.print(x);
							}
						}
							public void foo(int x) {
								System.out.print(x);
							}
					}
					
					public class XY extends XX {
						@SuppressWarnings("unused")
						public  void bar(String [] args) {
							 Y y = new Y();
							 J jj = y :: foo;
							 J jx = y.super ::  foo;
						}
						public static void main (String [] args) {}
					}"""
			},
			"""
				----------
				1. ERROR in XY.java (at line 23)
					J jx = y.super ::  foo;
					       ^
				y cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406614, [1.8][compiler] Missing and incorrect errors for lambda in explicit constructor call.
public void test406614() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int doit();
					}
					public class X {
						int f;
						X(I i) {
						}
						X() {
							this(() -> this.f);
						}
						X(short s) {
							this(() -> this.g());
						}
						X (int x) {
						    this(() -> f);
						}
						X (long x) {
						    this(() -> g());
						}
						int g() {
							return 0;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					this(() -> this.f);
					^^^^^^^^^^^^^^^^^^^
				The constructor X(() -> {}) is undefined
				----------
				2. ERROR in X.java (at line 9)
					this(() -> this.f);
					           ^^^^
				Cannot refer to 'this' nor 'super' while explicitly invoking a constructor
				----------
				3. ERROR in X.java (at line 12)
					this(() -> this.g());
					^^^^^^^^^^^^^^^^^^^^^
				The constructor X(() -> {}) is undefined
				----------
				4. ERROR in X.java (at line 12)
					this(() -> this.g());
					           ^^^^
				Cannot refer to 'this' nor 'super' while explicitly invoking a constructor
				----------
				5. ERROR in X.java (at line 15)
					this(() -> f);
					^^^^^^^^^^^^^^
				The constructor X(() -> {}) is undefined
				----------
				6. ERROR in X.java (at line 15)
					this(() -> f);
					           ^
				Cannot refer to an instance field f while explicitly invoking a constructor
				----------
				7. ERROR in X.java (at line 18)
					this(() -> g());
					           ^
				Cannot refer to an instance method while explicitly invoking a constructor
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial
public void test406588() {
	this.runNegativeTest(
			false /* skipJavac */,
			null,
			new String[] {
				"X.java",
				"""
					interface I {
						X.Y.Z makeY(int x);
					}
					public class X {
						class Y {
							Y(I i) {
							\t
							}
							Y() {
								this(Z::new);
							}
							class Z {
								Z(int x) {
					
								}
							}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface I {
					^
				No enclosing instance of type X.Y is available due to some intermediate constructor invocation
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406586, [1.8][compiler] Missing error about unavailable enclosing instance
public void test406586() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X.Y makeY();
					}
					public class X {
						public class Y {
						}
						static void foo() {
							I i = Y::new;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					interface I {
					^
				No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401989, [1.8][compiler] hook lambda expressions into "can be static" analysis
public void test401989() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
			new String[] {
				"X.java",
				"""
					interface I {
						void make();
					}
					public class X {
						int val;
						private I test() {
							return () -> System.out.println(val);
						}
						private I testCanBeStatic() {
							return () -> System.out.println();
						}
						public void call() { test().make(); testCanBeStatic().make();}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					private I testCanBeStatic() {
					          ^^^^^^^^^^^^^^^^^
				The method testCanBeStatic() from the type X can be declared as static
				----------
				""",
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test406773() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
				"""
					----------
					1. ERROR in X.java (at line 5)
						void foo() {
						     ^^^^^
					The method foo() from the type X can potentially be declared as static
					----------
					"""
				:
				"""
					----------
					1. ERROR in X.java (at line 5)
						void foo() {
						     ^^^^^
					The method foo() from the type X can potentially be declared as static
					----------
					2. WARNING in X.java (at line 10)
						I i = X::new;
						      ^^^^^^
					Access to enclosing constructor X(int) is emulated by a synthetic accessor method
					----------
					""";
		this.runNegativeTest(
			false,
			JavacTestOptions.SKIP, /* skip, because we are using custom error settings here */
			new String[] {
					"X.java",
					"""
						interface I {
							X makeX(int x);
						}
						public class X {
							void foo() {
								int local = 10;
								class Y extends X {
									class Z extends X {
										void f() {
											I i = X::new;
											i.makeX(123456);
											i = Y::new;
											i.makeX(987654);
											i = Z::new;
											i.makeX(456789);
										}
										private Z(int z) {
										}
										Z() {}
									}
									private Y(int y) {
										System.out.println(local);
									}
									private Y() {
									}
								}
								new Y().new Z().f();
							}
							private X(int x) {
							}
							X() {
							}
							public static void main(String[] args) {
								new X().foo();
							}
						}
						"""
			},
			errMessage,
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406859,  [1.8][compiler] Bad hint that method could be declared static
public void test406859a() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							int foo(int i);
						}
						public class X {
							public static void main(String[] args) {
								X x = new X();
								I i = x::foo;
								i.foo(3);
							}
							int foo(int x) {
								return x;
							}  \s
						}
						"""
			},
			"",
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406859,  [1.8][compiler] Bad hint that method could be declared static
public void test406859b() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit (Y y);
						}
						
						class Y {
							void foo() {
								return;
							}
						}
						
						public class X {
							public static void main(String[] args) {
								I i = Y::foo;\s
								Y y = new Y();
								i.doit(y);
							}
						}
						"""
			},
			"",
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406859,  [1.8][compiler] Bad hint that method could be declared static
public void test406859c() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit ();
						}
						
						class Y {
							void foo() { \s
								return;
							}
						}
						
						public class X {
							public static void main(String[] args) {
								I i = new Y()::foo;
								i.doit();
							}
						}
						"""
			},
			"",
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406859,  [1.8][compiler] Bad hint that method could be declared static
// A case where we can't help but report the wrong hint due to separate compilation.
public void test406859d() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.WARNING);
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		new String[] {
				"Y.java",
				"""
					public class Y {
						void foo() {
							return;
						}
					}""",
				"X.java",
				"""
					interface I {
						void doit ();
					}
					
					public class X {
						public static void main(String[] args) {
							I i = new Y()::foo;
							i.doit();
						}
					}
					"""
		},
		"""
			----------
			1. WARNING in Y.java (at line 2)
				void foo() {
				     ^^^^^
			The method foo() from the type Y can potentially be declared as static
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=410114, [1.8] CCE when trying to parse method reference expression with inappropriate type arguments
public void test410114() throws IOException {
	String source = """
		interface I {
		    void foo(Y<String> y);
		}
		public class Y<T> {
		    class Z<K> {
		        Z(Y<String> y) {
		            System.out.println("Y<T>.Z<K>:: new");
		        }
		        void bar() {
		            I i = Y<String>.Z<Integer>::<String> new;
		            i.foo(new Y<String>());
		            i = Y<String>.Z<Integer>:: new;
		            i.foo(new Y<String>());
		            i = Y.Z:: new;
		            i.foo(new Y<String>());
		        }
		    }
		}
		""";
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[]{"Y.java",
						source},
						"""
							----------
							1. WARNING in Y.java (at line 10)
								I i = Y<String>.Z<Integer>::<String> new;
								                             ^^^^^^
							Unused type arguments for the non generic constructor Y<String>.Z<Integer>(Y<String>) of type Y<String>.Z<Integer>; it should not be parameterized with arguments <String>
							----------
							2. WARNING in Y.java (at line 14)
								i = Y.Z:: new;
								    ^^^^^^^^^
							Type safety: The constructor Y.Z(Y) belongs to the raw type Y.Z. References to generic type Y<T>.Z<K> should be parameterized
							----------
							""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412453,
//[1.8][compiler] Stackoverflow when compiling LazySeq
public void test412453() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.util.AbstractList;
					import java.util.Comparator;
					import java.util.Optional;
					
					import java.util.function.*;
					
					abstract class Y<E> extends AbstractList<E> {
						public <C extends Comparable<? super C>> Optional<E> minBy(Function<E, C> propertyFun) { return null;}\s
					}
					
					public class X {
						public void foo(Y<Integer> empty) throws Exception {
							final Optional<Integer> min = empty.minBy((a, b) -> a - b);
						}
					}
					
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				final Optional<Integer> min = empty.minBy((a, b) -> a - b);
				                                    ^^^^^
			The method minBy(Function<Integer,C>) in the type Y<Integer> is not applicable for the arguments ((<no type> a, <no type> b) -> {})
			----------
			2. ERROR in X.java (at line 13)
				final Optional<Integer> min = empty.minBy((a, b) -> a - b);
				                                          ^^^^^^^^^^^^^^^
			Lambda expression's signature does not match the signature of the functional interface method apply(Integer)
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412284,
//[1.8][compiler] [1.8][compiler] Inspect all casts to/instanceof AbstractMethodDeclaration to eliminate potential CCEs
public void test412284a() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.IOException;
					interface I { void foo() throws IOException; }
					public class X {
					 void bar() {
						 I i = () -> {
							 try {
								 throw new IOException();
							 } catch (IOException e) {			\s
							 } finally {
								 i.foo();
							 }
						 };
					 }
					}
					"""
		},

		"""
			----------
			1. ERROR in X.java (at line 10)
				i.foo();
				^
			The local variable i may not have been initialized
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412284,
//[1.8][compiler] [1.8][compiler] Inspect all casts to/instanceof AbstractMethodDeclaration to eliminate potential CCEs
public void test412284b() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I { void foo();}
					class X {\s
					   final int t;
					   X(){
					       I x = () ->  {
					    	 try {
					           t = 3;
					         } catch (Exception e) {
					           t = 4;
					         }
					      };
					  }
					}
					"""
		},

		"""
			----------
			1. ERROR in X.java (at line 4)
				X(){
				^^^
			The blank final field t may not have been initialized
			----------
			2. ERROR in X.java (at line 7)
				t = 3;
				^
			The final field X.t cannot be assigned
			----------
			3. ERROR in X.java (at line 9)
				t = 4;
				^
			The final field X.t cannot be assigned
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412284,
//[1.8][compiler] [1.8][compiler] Inspect all casts to/instanceof AbstractMethodDeclaration to eliminate potential CCEs
public void test412284c() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I { void foo();}
					class X {\s
					   final int t;
					   X(){
					       I x = () ->  {
					    	 try {
					           t += 3;
					         } catch (Exception e) {
					           t += 4;
					         }
					      };
					  }
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				X(){
				^^^
			The blank final field t may not have been initialized
			----------
			2. ERROR in X.java (at line 7)
				t += 3;
				^
			The blank final field t may not have been initialized
			----------
			3. ERROR in X.java (at line 7)
				t += 3;
				^
			The final field X.t cannot be assigned
			----------
			4. ERROR in X.java (at line 9)
				t += 4;
				^
			The blank final field t may not have been initialized
			----------
			5. ERROR in X.java (at line 9)
				t += 4;
				^
			The final field X.t cannot be assigned
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=412650
// [1.8][compiler]Incongruent Lambda Exception thrown
public void test412650() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						String sam();
					}
					public class X {
						static String foo(I i) { return ""; }
						public static void main(String[] args) {
							foo(() -> foo(X::getInt));
						}
						static Integer getInt() { return 0; }
					}
					"""
		},
		"----------\n" +
		// this is reported because the lambda has errors and thus is not marked as valueCompatible:
		"1. ERROR in X.java (at line 7)\n" +
		"	foo(() -> foo(X::getInt));\n" +
		"	^^^\n" +
		"The method foo(I) in the type X is not applicable for the arguments (() -> {})\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	foo(() -> foo(X::getInt));\n" +
		"	          ^^^\n" +
		"The method foo(I) in the type X is not applicable for the arguments (X::getInt)\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	foo(() -> foo(X::getInt));\n" +
		"	              ^^^^^^^^^\n" +
		"The type of getInt() from the type X is Integer, this is incompatible with the descriptor\'s return type: String\n" +
		"----------\n",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409544
// Bug 409544 - [1.8][compiler] Any local variable used but not declared in a lambda body must be definitely assigned before the lambda body.
public void test409544() {
	this.runNegativeTest(
		new String[] {
				"Sample.java",
				"""
					public class Sample{
						interface Int { void setInt(int[] i); }
						public static void main(String[] args) {
							int j;
							Int int1 = (int... i) -> {
															j=10;
													};
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Sample.java (at line 6)
				j=10;
				^
			Local variable j defined in an enclosing scope must be final or effectively final
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=409544
// Bug 409544 - [1.8][compiler] Any local variable used but not declared in a lambda body must be definitely assigned before the lambda body.
public void test409544b() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    interface Int {
						void setInt(int[] i);
					    }
					    public static void main(String[] args) {
					
					    int j = 0;
					    Int i = new Int() {
							@Override
							public void setInt(int[] i) {
								j = 10;
							}
						};
					    }
					}
					"""
		},
		"""
			----------
			1. WARNING in X.java (at line 10)
				public void setInt(int[] i) {
				                         ^
			The parameter i is hiding another local variable defined in an enclosing scope
			----------
			2. ERROR in X.java (at line 11)
				j = 10;
				^
			Local variable j defined in an enclosing scope must be final or effectively final
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=415844
// Bug 415844 - [1.8][compiler] Blank final initialized in a lambda expression should not pass
public void test415844a() {
	this.runNegativeTest(
		new String[] {
				"Sample.java",
				"""
					public class Sample{
						interface Int { void setInt(int i); }
						public static void main(String[] args) {
							final int j;
							Int int1 = (int i) -> {
													j=10;
							};
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Sample.java (at line 6)
				j=10;
				^
			The final local variable j cannot be assigned, since it is defined in an enclosing type
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=415844
// Bug 415844 - [1.8][compiler] Blank final initialized in a lambda expression should not pass
public void test415844b() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    interface Int {
							void setInt(int[] i);
					    }
					    public static void main(String[] args) {
					    	final int j;
					    	Int i = new Int() {
								@Override
								public void setInt(int[] i) {
									j = 10;
								}
							};
					    }
					}
					"""
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				public void setInt(int[] i) {
				                         ^
			The parameter i is hiding another local variable defined in an enclosing scope
			----------
			2. ERROR in X.java (at line 10)
				j = 10;
				^
			The final local variable j cannot be assigned, since it is defined in an enclosing type
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* custom options */
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=404657 [1.8][compiler] Analysis for effectively final variables fails to consider loops
public void test404657_final() {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					" void executeLater(Runnable r) { /* ... */\n" +
					" }\n" +
					" public int testFinally() {\n" +
					"  int n;\n" +
					"  try {\n" +
					"   n = 42;\n" +
					"    executeLater(() -> System.out.println(n)); // Error: n is not effectively final\n" +
					"  } finally {\n" +
					"   n = 23;\n" +
					"  }\n" +
					"  return n;\n" +
					" }\n" +
					"\n" +
					"}\n" +
					""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					executeLater(() -> System.out.println(n)); // Error: n is not effectively final
					                                      ^
				Local variable n defined in an enclosing scope must be final or effectively final
				----------
				"""
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=404657 [1.8][compiler] Analysis for effectively final variables fails to consider loops
public void test404657_loop() {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					" void executeLater(Runnable r) { /* ... */\n" +
					" }\n" +
					" public void testLoop() {\n" +
					"  int n;\n" +
					"  for (int i = 0; i < 3; i++) {\n" +
					"   n = i;\n" +
					"   executeLater(() -> System.out.println(n)); // Error: n is not effectively final\n" +
					"  }\n" +
					" }\n" +
					"\n" +
					"}\n" +
					""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					executeLater(() -> System.out.println(n)); // Error: n is not effectively final
					                                      ^
				Local variable n defined in an enclosing scope must be final or effectively final
				----------
				"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420580, [1.8][compiler] ReferenceExpression drops explicit type arguments
public void testExplicitTypeArgument() {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void sam(X t, Integer s);\n" +
					"}\n" +
					"public class X {\n" +
					"	<T> void function(T t) {}\n" +
					"	public static void main(String [] args) {\n" +
					"		I i = X::<String>function;\n" +
					"		i = X::function;\n" +
					"		i = X::<Integer>function;\n" +
					"	}\n" +
					"}\n" +
					""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					I i = X::<String>function;
					      ^^^^^^^^^^^^^^^^^^^
				The type X does not define function(X, Integer) that is applicable here
				----------
				"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420582,  [1.8][compiler] Compiler should allow creation of generic array creation with unbounded wildcard type arguments
public void testGenericArrayCreation() {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X<?, ?, ?>[] makeArray(int i);\n" +
					"}\n" +
					"public class X<T, U, V> {\n" +
					"	public static void main(String [] args) {\n" +
					"		I i = X<?, ?, ?>[]::new; // OK.\n" +
					"		i = X<String, Integer, ?>[]::new; // ! OK\n" +
					"		X<?, ?, ?> [] a = new X<?, ?, ?>[10]; // OK\n" +
					"		a = new X<String, Integer, ?>[10]; // ! OK\n" +
					"		System.out.println(i.makeArray(1024).length);\n" +
					"	}\n" +
					"}\n" +
					""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					i = X<String, Integer, ?>[]::new; // ! OK
					    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot create a generic array of X<String,Integer,?>
				----------
				2. ERROR in X.java (at line 9)
					a = new X<String, Integer, ?>[10]; // ! OK
					    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot create a generic array of X<String,Integer,?>
				----------
				"""
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420598, [1.8][compiler] Incorrect error about intersection cast type not being a functional interface.
public void testIntersectionCast() {
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						import java.io.Serializable;
						interface I {
							void foo();
						}
						interface J extends I {
							void foo();
						}
						interface K {
						}
						interface L {
							void foo();
						}
						interface All extends J, I, K, L {}
						public class X {
							public static void main(String[] args) {
								I i = (I & Serializable) () -> {};
								i = (I & J & K) () -> {};
								i = (J & I & K) () -> {}; \s
								i = (J & I & K & L) () -> {}; \s
								i = (All) () -> {};
							}
						}
						"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421711, [1.8][compiler] '_' as identifier for a lambda parameter should be rejected.
public void testUnderScoreParameter() {
		if (this.complianceLevel >= ClassFileConstants.JDK22)
			return;
		String level = this.complianceLevel >= ClassFileConstants.JDK9 ? "ERROR" : "WARNING";
		String errorMessage = this.complianceLevel >= ClassFileConstants.JDK9 ? "\'_\' is a keyword from source level 9 onwards, cannot be used as identifier\n" : "\'_\' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on\n";
		if (this.complianceLevel >= ClassFileConstants.JDK22) {
			errorMessage = "Unnamed Patterns and Variables is a preview feature and disabled by default. Use --enable-preview to enable\n";
		}
		String otherErrorMessage = this.complianceLevel >= ClassFileConstants.JDK22 ? errorMessage : "\'_\' is a keyword from source level 9 onwards, cannot be used as identifier\n";
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface F {
							void foo(int x);
						}
						interface I {
							default void foo() {
								F f = (int _) -> {
								};
								F f2 = _ -> {};
							}
						}
						"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	F f = (int _) -> {\n" +
			"	           ^\n" +
			otherErrorMessage +
			"----------\n" +
			"2. "+ level +" in X.java (at line 8)\n" +
			"	F f2 = _ -> {};\n" +
			"	       ^\n" +
			errorMessage +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	F f2 = _ -> {};\n" +
			"	       ^\n" +
			otherErrorMessage +
			"----------\n"
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383096, [1.8][compiler]NullPointerException with a wrong lambda code snippet.
public void test383096() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {}
						class XI {
							void foo() {
						        	I t1 = f -> {{};
						        	I t2 = () -> 42;
						        }\s
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					I t2 = () -> 42;
					       ^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				2. ERROR in X.java (at line 6)
					}\s
					^
				Syntax error, insert ";" to complete BlockStatements
				----------
				3. ERROR in X.java (at line 7)
					}
					^
				Syntax error, insert "}" to complete ClassBody
				----------
				""",
			true // statement recovery.
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422516,  [1.8][compiler] NPE in ArrayReference.analyseAssignment.
public void test422516() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String[] args) throws InterruptedException {
						        final int[] result= { 0 };
						        Thread t = new Thread(() -> {
						            sysoresult[0]= 42;
						        });
						        t.start();
						        t.join();
						        System.out.println(result[0]);
						    }
						}
						"""

			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					sysoresult[0]= 42;
					^^^^^^^^^^
				sysoresult cannot be resolved to a variable
				----------
				""",
			true // statement recovery.
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422516,  [1.8][compiler] NPE in ArrayReference.analyseAssignment.
public void test422516a() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String[] args) throws InterruptedException {
						        final int[] result= { 0 };
						        Thread t = new Thread(() -> {
						            System.out.printlnresult[0]= 42;
						        });
						        t.start();
						        t.join();
						        System.out.println(result[0]);
						    }
						}
						"""

			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					System.out.printlnresult[0]= 42;
					           ^^^^^^^^^^^^^
				printlnresult cannot be resolved or is not a field
				----------
				"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422489, [1.8][compiler] NPE in CompoundAssignment.analyseCode when creating AST for java.util.stream.Collectors
public void test422489() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(String [] x, String y);
						}
						interface J {
							void foo(int x, int y);
						}
						public class X {
						    static void goo(I i) {
						    }
						    static void goo(J j) {
						    }
						    public static void main(String[] args) throws InterruptedException {
								goo((x, y) -> { x[0] += 1; });
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					goo((x, y) -> { x[0] += 1; });
					^^^
				The method goo(I) is ambiguous for the type X
				----------
				"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422489, [1.8][compiler] NPE in CompoundAssignment.analyseCode when creating AST for java.util.stream.Collectors
public void test422489a() { // interfaces and methods order changed, triggers NPE.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface J {
							void foo(int x, int y);
						}
						interface I {
							void foo(String [] x, String y);
						}
						public class X {
						    static void goo(J j) {
						    }
						    static void goo(I i) {
						    }
						    public static void main(String[] args) throws InterruptedException {
								goo((x, y) -> { x[0] += 1; });
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					goo((x, y) -> { x[0] += 1; });
					^^^
				The method goo(J) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 13)
					goo((x, y) -> { x[0] += 1; });
					                ^^^^
				The type of the expression must be an array type but it resolved to int
				----------
				"""
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422489, [1.8][compiler] NPE in CompoundAssignment.analyseCode when creating AST for java.util.stream.Collectors
public void test422489b() { // interfaces and methods order changed, triggers NPE.
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured,
			new String[] {
					"X.java",
					"""
						interface I {
							String foo(String [] x, String y);
						}
						interface J {
							void foo(int x, int y);
						}
						public class X {
						    static void goo(J j) {
						    }
						    static void goo(I i) {
						    }
						    public static void main(String[] args) throws InterruptedException {
								goo((x, y) -> { return x[0] += 1; });
						    }
						}
						"""
			},
			""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422489, [1.8][compiler] NPE in CompoundAssignment.analyseCode when creating AST for java.util.stream.Collectors
public void test422489c() { // interfaces and methods order changed, triggers NPE.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							String foo(String [] x, String y);
						}
						interface J {
							void foo(int x, int y);
						}
						public class X {
						    static void goo(J j) {
						    }
						    static void goo(I i) {
						    }
						    public static void main(String[] args) throws InterruptedException {
								goo((x, y) -> x[0] += 1);
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					goo((x, y) -> x[0] += 1);
					^^^
				The method goo(J) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 13)
					goo((x, y) -> x[0] += 1);
					              ^^^^
				The type of the expression must be an array type but it resolved to int
				----------
				"""
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422489, [1.8][compiler] NPE in CompoundAssignment.analyseCode when creating AST for java.util.stream.Collectors
public void test422489d() { // interfaces and methods order changed, triggers NPE.
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							String foo(String x, String y);
						}
						interface J {
							void foo(int x, int y);
						}
						public class X {
						    static void goo(J j) {
						    }
						    static void goo(I i) {
						    }
						    public static void main(String[] args) throws InterruptedException {
								goo((x, y) -> x[0] += 1);
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					goo((x, y) -> x[0] += 1);
					^^^
				The method goo(J) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 13)
					goo((x, y) -> x[0] += 1);
					              ^^^^
				The type of the expression must be an array type but it resolved to int
				----------
				"""
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422801, [1.8][compiler] NPE in MessageSend.analyseCode in lambda body with missing import
public void test422801() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public void foo(Random arg) {
						        new Thread(() -> {
						            arg.intValue();
						        });
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo(Random arg) {
					                ^^^^^^
				Random cannot be resolved to a type
				----------
				"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422801, [1.8][compiler] NPE in MessageSend.analyseCode in lambda body with missing import
public void test422801a() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    Random arg;
						    public void foo() {
						        new Thread(() -> {
						            arg.intValue();
						        });
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					Random arg;
					^^^^^^
				Random cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					arg.intValue();
					^^^
				Random cannot be resolved to a type
				----------
				"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405134, [1.8][code assist + compiler] compiler and code assist problem in multilevel lambda with curly bracketed body
public void test405134a() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Foo { \n" +
					"	int run1(int s1, int s2);\n" +
					"	static int x2 = 0;\n" +
					"}\n" +
					"interface Foo1 {\n" +
					"	Foo run2(int argFoo1);\n" +
					"}\n" +
					"interface X extends Foo{\n" +
					"    static int x1 = 2;\n" +
					"    static Foo f = (x5, x6) -> x5;\n" +
					"    static Foo1 f1 = af1 -> (a1,b1) -> {int uniqueName = 4; return uniqueName};\n" + // missing semicolon triggers an NPE
					"}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					static Foo1 f1 = af1 -> (a1,b1) -> {int uniqueName = 4; return uniqueName};
					                                                                         ^
				Syntax error on token "}", delete this token
				----------
				2. ERROR in X.java (at line 12)
					}
					^
				Syntax error, insert ";" to complete FieldDeclaration
				----------
				3. ERROR in X.java (at line 12)
					}
					^
				Syntax error, insert "}" to complete InterfaceBody
				----------
				""",
			true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421927, [1.8][compiler] Bad diagnostic: Unnecessary cast from I to I for lambdas.
public void test421927() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {\s
							int foo();
						}
						public class X {
						    I i  = (I & java.io.Serializable) () -> 42;
						}
						"""
			},
			"",
			true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421927, [1.8][compiler] Bad diagnostic: Unnecessary cast from I to I for lambdas.
public void test421927a() {
	this.runNegativeTest(
			false,
			Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
					"X.java",
					"""
						interface I {\s
							int foo();
						}
						public class X {
						    I i;
						    { i = (I & java.io.Serializable) () -> 42;
						       I j = (I & java.io.Serializable) () -> 42;
						       j = (I & java.io.Serializable) j == null ? () -> 42 : () -> 42;
						       j = goo((I & java.io.Serializable) () -> 42);
						    }
						    I goo(I i) {
						        return (I & java.io.Serializable) () -> 42;
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 11)
					I goo(I i) {
					        ^
				The parameter i is hiding a field from type X
				----------
				""",
			true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423429, [1.8][compiler] NPE in LambdaExpression.analyzeCode
public void test423429() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							J foo(String x, String y);
						}
						interface J {
							K foo(String x, String y);
						}
						interface K {
							int foo(String x, int y);
						}
						public class X {
							static void goo(K i) {}
							public static void main(String[] args) {
								goo ((first, second) -> {
									return (xyz, pqr) -> first.length();
								});
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 14)
					return (xyz, pqr) -> first.length();
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423129,  [1.8][compiler] Hook up lambda expressions into statement recovery
public void test423129() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							String foo(Integer x);
						}
						public class X {
							static void goo(String s) {
							}
							static void goo(I i) {
							}
							public static void main(String[] args) {
								goo((xyz) -> {
									System.out.println(xyz);
									return xyz.
								});
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					return xyz.
					       ^^^
				Type mismatch: cannot convert from Integer to String
				----------
				2. ERROR in X.java (at line 12)
					return xyz.
					          ^
				Syntax error on token ".", ; expected
				----------
				""",
			true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423129,  [1.8][compiler] Hook up lambda expressions into statement recovery
public void test423129b() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						import java.util.Arrays;
						import java.util.Collections;
						import java.util.Comparator;
						public class X {
						   int compareTo(X x) { return 0; }
							void foo() {
								Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X())),
										(X o1, X o2) -> o1.compareTo(o2)); //[2]
							}
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""",
			true);
}
// modified the previous example to craft a result requiring constant narrowing (13 -> byte)
public void test423129c() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						import java.util.List;
						import java.util.Arrays;
						class MySorter { static <T> void sort(List<T> l, MyComparator<T> comp) { } }
						interface MyComparator<T> { byte compare(T t1, T t2); }
						public class X {
						   int compareTo(X x) { return 0; }
							void foo() {
								MySorter.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X())),
										(X o1, X o2) -> 13);
							}
						}
						"""
			});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424400, [1.8] Interfaces in the same hierarchy are allowed in an intersection cast with different type argument
public void test424400() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X<T> implements MyComparable<T>{
						    public static void main(String argv[]) {
						    	int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
						    }
						    public int compareTo(T o) {
								return 0;
							}
						}
						interface MyComparable<T> extends Comparable<T> {}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The interface Comparable cannot be implemented more than once with different arguments: Comparable<Integer> and Comparable
				----------
				2. WARNING in X.java (at line 3)
					int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
					                                     ^^^^^^^^^^^^
				MyComparable is a raw type. References to generic type MyComparable<T> should be parameterized
				----------
				3. WARNING in X.java (at line 3)
					int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
					                                                       ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424400, [1.8] Interfaces in the same hierarchy are allowed in an intersection cast with different type argument
public void _test424400() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X<T> implements MyComparable<T> {
						    public static void main(String argv[]) {
						    	int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
						    }
						    public int compareTo(T o) {
							return 0;
						    }
						}
						interface MyComparable<T> {
						     public int compareTo(T value);
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The interface Comparable cannot be implemented more than once with different arguments: Comparable and Comparable<Integer>
				----------
				2. WARNING in X.java (at line 3)
					int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
					                                     ^^^^^^^^^^^^
				MyComparable is a raw type. References to generic type MyComparable<T> should be parameterized
				----------
				3. WARNING in X.java (at line 3)
					int result = ((Comparable<Integer> & MyComparable) new X()).compareTo(1);
					                                                       ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424444, [1.8] VerifyError when constructor reference used with array
public void test424444() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface Functional<T> {
					    T foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	int [] a = goo(10);
					    	Functional<Integer []> contr = int[]::new;
					        System.out.println("Done");
					    }
					    static int [] goo(int x) {
					    	return new int [x];
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Functional<Integer []> contr = int[]::new;
				                               ^^^^^^^^^^
			Constructed array int[] cannot be assigned to Integer[] as required in the interface descriptor \s
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface IJK {
					    void foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	int [] a = (int [] & IJK) null;
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				int [] a = (int [] & IJK) null;
				            ^^^^^^
			Arrays are not allowed in intersection cast operator
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512a() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface IJK {
					    void foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	int [] a = (int [] & Serializable & IJK) null;
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				int [] a = (int [] & Serializable & IJK) null;
				            ^^^^^^
			Arrays are not allowed in intersection cast operator
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512b() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface IJK {
					    void foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	int [] a = (int [] & IJK & Serializable) null;
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				int [] a = (int [] & IJK & Serializable) null;
				            ^^^^^^
			Arrays are not allowed in intersection cast operator
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512c() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface IJK {
					    void foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	int [] a = (IJK & Serializable & int []) null;
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				int [] a = (IJK & Serializable & int []) null;
				                                 ^^^^^^
			The type int[] is not an interface; it cannot be specified as a bounded parameter
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512cd() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface I {
					    void foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	I i = (int [] & I) (i) -> {};
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				I i = (int [] & I) (i) -> {};
				       ^^^^^^
			Arrays are not allowed in intersection cast operator
			----------
			2. ERROR in X.java (at line 7)
				I i = (int [] & I) (i) -> {};
				                   ^^^^^^
			The target type of this expression must be a functional interface
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512ce() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface I {
					    void foo(int size);
					}
					public class X  {
					    public static void main(String argv[]) {
					    	I i = (int [] & Serializable) (i) -> {};
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				I i = (int [] & Serializable) (i) -> {};
				       ^^^^^^
			Arrays are not allowed in intersection cast operator
			----------
			2. ERROR in X.java (at line 7)
				I i = (int [] & Serializable) (i) -> {};
				                              ^^^^^^
			The target type of this expression must be a functional interface
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425621, [1.8][compiler] Missing error for raw type in constructor reference with explicit type arguments
public void test425621() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					class Y<T> {
					    Y() {}
					}   \s
					interface I {
					    Y<Y> foo();
					}
					public class X  {
					    I i = Y::<X>new;
					}
					""",
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				Y<Y> foo();
				  ^
			Y is a raw type. References to generic type Y<T> should be parameterized
			----------
			2. ERROR in X.java (at line 8)
				I i = Y::<X>new;
				          ^
			Explicit type arguments cannot be specified in raw constructor reference expression
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423803, [1.8][compiler] No error shown for ambiguous reference to the method
public void test423803() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					class C2 implements C2_Sup {
					    public static final FI fi = x -> x++;
					    public static final FL fl = x -> x++;
					    {
					        bar(x -> x++); // [1]
					        bar(fl);\s
					    }
					    void bar(FI fi) { }
					}
					interface C2_Sup {\t
						default void bar(FL fl) { }
					}
					@FunctionalInterface
					interface FI {
						int foo(int x);
					}
					@FunctionalInterface
					interface FL {
					    long foo(long x);
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				bar(x -> x++); // [1]
				^^^
			The method bar(FI) is ambiguous for the type C2
			----------
			2. WARNING in X.java (at line 8)
				void bar(FI fi) { }
				            ^^
			The parameter fi is hiding a field from type C2
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423803, [1.8][compiler] No error shown for ambiguous reference to the method
public void test423803b() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X implements K {
					    {
					        bar(x -> x++); // [1]
					    }
					    void bar(I fi) { }
					}
					interface K {\t
						default void bar(J fl) { }
					}
					interface I {
						int foo(int x);
					}
					interface J {
					    long foo(long x);
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				bar(x -> x++); // [1]
				^^^
			The method bar(I) is ambiguous for the type X
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425712, [1.8][compiler] Valid program rejected by the compiler.
public void test425712() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					class C2 {
					    {
					        bar( () -> (char) 0); // [1]
					    }
					    void bar(FC fc) { }
					    void bar(FB fb) { }
					}
					interface FB {
						byte foo();
					}
					interface FC {
					    char foo();
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				bar( () -> (char) 0); // [1]
				^^^
			The method bar(FC) is ambiguous for the type C2
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421926, [1.8][compiler] Compiler tolerates illegal forward reference from lambda in initializer
public void test421926() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {
						I run(int s1);
					}
					class X {\t
					   public static final int f = f;
						public static final I fi = x -> fi;
						public static final I fj = x -> fk;
						public static final I fk = x -> fj;
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public static final int f = f;
				                            ^
			Cannot reference a field before it is defined
			----------
			2. ERROR in X.java (at line 6)
				public static final I fi = x -> fi;
				                                ^^
			Cannot reference a field before it is defined
			----------
			3. ERROR in X.java (at line 7)
				public static final I fj = x -> fk;
				                                ^^
			Cannot reference a field before it is defined
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421926, [1.8][compiler] Compiler tolerates illegal forward reference from lambda in initializer
public void test421926b() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {\s
						int run(int s1, int s2);\s
					}
					public class X {
					    static int f = ((I) (int x5, int x2) -> x1).run(10,  20);
					    static int x1 = 2;
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				static int f = ((I) (int x5, int x2) -> x1).run(10,  20);
				                                        ^^
			Cannot reference a field before it is defined
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421926, [1.8][compiler] Compiler tolerates illegal forward reference from lambda in initializer
public void test421926c() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					interface I {\s
						int run(int s1, int s2);\s
					}
					public class X {
					    int f = ((I) (int x5, int x2) -> x1).run(10,  20);
					    static int x1 = 2;
					}
					""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426206, [1.8][compiler] Compiler tolerates illegal code.
public void test426206() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.util.Comparator;
					public class X  {
					    public static void main(String argv[]) {
					        Comparator<? extends String> c = true ? (Integer i, Integer j) -> { return 0; } : (Long i, Long j) -> { return 1; };
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				Comparator<? extends String> c = true ? (Integer i, Integer j) -> { return 0; } : (Long i, Long j) -> { return 1; };
				                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Comparator<Integer> to Comparator<? extends String>
			----------
			2. ERROR in X.java (at line 4)
				Comparator<? extends String> c = true ? (Integer i, Integer j) -> { return 0; } : (Long i, Long j) -> { return 1; };
				                                                                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Comparator<Long> to Comparator<? extends String>
			----------
			""");
}
public void testBug426563() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<U, V extends J<U>> {\s
				    void foo(U u, V v);\s
				}
				
				interface J<T> {}
				
				public class X  {
				
				    public void bar(FI<?, ?> fi) {}
				
				    public static void main(String args[]) {
				      new X().bar((p, q) -> {});\s
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				public void bar(FI<?, ?> fi) {}
				                ^^
			FI cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 12)
				new X().bar((p, q) -> {});\s
				        ^^^
			The method bar(FI<?,?>) from the type X refers to the missing type FI
			----------
			3. ERROR in X.java (at line 12)
				new X().bar((p, q) -> {});\s
				            ^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426965,  [1.8] Eclipse rejects valid type conversion in lambda
public void test426965() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					interface I<U extends List<X>, V> {
						V foo(U p);
					}
					public void main() {
						I<List<X>, Object> fi = p -> p.toArray(new X[] {});
					}
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
public void test427207() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						I i = (I) ((args == null) ? ()->{} : ()-> {});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				I i = (I) ((args == null) ? ()->{} : ()-> {});
				                            ^^^^
			The target type of this expression must be a functional interface
			----------
			2. ERROR in X.java (at line 6)
				I i = (I) ((args == null) ? ()->{} : ()-> {});
				                                     ^^^^
			The target type of this expression must be a functional interface
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425278, [1.8][compiler] Suspect error: The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
// NOTE: javac 8b127 incorrectly accepts this program due to https://bugs.openjdk.java.net/browse/JDK-8033810
public void test425278() {
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.JavacHasABug.JavacBug8033810,
		new String[] {
			"X.java",
			"""
				interface I<T, S extends X<T>> {\s
				    T foo(S p);
				}
				public class X<T>  {
				    public void bar() {
				    	I<Object, ? extends X<Object>> f = (p) -> p;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				I<Object, ? extends X<Object>> f = (p) -> p;
				                                   ^^^^^^^^
			The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427265, - [1.8][compiler] Type inference with anonymous classes
public void test427265() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
				    public static void main(String[] args) {
					     List<String> ss = Arrays.asList("1", "2", "3");
					     ss.stream().map(s -> new Object() {});
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427749, - [1.8][compiler]NullPointerException in ReferenceExpression.resolveType
public void test427749() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				    void foo(X<String> y);
				}
				public class X<T> {
				    class Z<K> {
				        Z(X<String> y) {
				            System.out.println("Y<T>.Z<K>::new");
				        }
				        public void bar() {
				            I i = Y<String>.Z<Integer>::<String> new;
				            i.foo(new Y<String>());
				        }
				    }
					public void foo() {
						Z<String> z = new Z<String>(null);
						z.bar();
					}
					public static void main(String[] args) {
						Y<String> y = new Y<String>();
						y.foo();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				I i = Y<String>.Z<Integer>::<String> new;
				      ^
			Y cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 11)
				i.foo(new Y<String>());
				          ^
			Y cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 19)
				Y<String> y = new Y<String>();
				^
			Y cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 19)
				Y<String> y = new Y<String>();
				                  ^
			Y cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428300, - [1.8] Map.computeIfAbsent fails with array value types
public void test428300() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.concurrent.ConcurrentHashMap;
				public class X {
					public static void main(String[] args) {
						ConcurrentHashMap<String, String[]> map = new ConcurrentHashMap<>();
						map.computeIfAbsent("doo", e -> new String[] {});
					}
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428300, - [1.8] Map.computeIfAbsent fails with array value types
public void test428300a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.concurrent.ConcurrentHashMap;
				import java.util.function.Function;
				public class X {
					public static void main(String[] args) {
						ConcurrentHashMap<String, String[]> map = new ConcurrentHashMap<>();
						Function<String, String[]> f = e -> new String[] {};
						map.computeIfAbsent("doo", f);
					}
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428177, - [1.8][compiler] Insistent capture issues
public void test428177() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				class InsistentCapture {
				  static void processJar(Path plugin) throws IOException {
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Function<? super JarEntry, ? extends String> toName =
				          entry -> entry.getName();
				        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok
				        withWildcard(entries.map(toName).distinct()); // Ok
				        withWildcard(stream); // Ok
				        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR
				        withoutWildcard(entries.map(toName).distinct()); // ERROR
				        withoutWildcard(stream); // ERROR
				        withoutWildcard(stream2); // Ok
				        withoutWildcard(coerce(stream)); // Ok
				        withoutWildcard(stream.map((String v1) -> { // ERROR
				          String r = "" + v1; // Hover on v: Ok
				          return r;
				        }));
				        withoutWildcard(stream.map((v2) -> { // Ok
				          String r = "" + v2; // Hover on v: NOT OK
				          return r;
				        }));
				      }
				    }
				  }
				  private static Stream<String> coerce(Stream<? extends String> stream) {
				    if("1" == "") { return stream.collect(Collectors.toList()).stream(); // ERROR
				    }
				    return stream.collect(Collectors.toList()); // NO ERROR
				  }
				  private static void withWildcard(Stream<? extends String> distinct) {
				    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK
				  }
				  private static void withoutWildcard(Stream<String> distinct) {
				    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 19)
				Stream<String> stream2 = entries.map(toName).distinct(); // ERROR
				                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Stream<capture#7-of ? extends String> to Stream<String>
			----------
			2. ERROR in X.java (at line 20)
				withoutWildcard(entries.map(toName).distinct()); // ERROR
				^^^^^^^^^^^^^^^
			The method withoutWildcard(Stream<String>) in the type InsistentCapture is not applicable for the arguments (Stream<capture#9-of ? extends String>)
			----------
			3. ERROR in X.java (at line 21)
				withoutWildcard(stream); // ERROR
				^^^^^^^^^^^^^^^
			The method withoutWildcard(Stream<String>) in the type InsistentCapture is not applicable for the arguments (Stream<capture#10-of ? extends String>)
			----------
			4. ERROR in X.java (at line 36)
				if("1" == "") { return stream.collect(Collectors.toList()).stream(); // ERROR
				                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Stream<capture#14-of ? extends String> to Stream<String>
			----------
			5. ERROR in X.java (at line 38)
				return stream.collect(Collectors.toList()); // NO ERROR
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from List<capture#17-of ? extends String> to Stream<String>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428795, - [1.8]Internal compiler error: java.lang.NullPointerException at org.eclipse.jdt.internal.compiler.ast.MessageSend.analyseCode
public void test428795() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.net.NetworkInterface;
				import java.util.Optional;
				public class X {
				  public static void main( String[] args ) {
				    Optional.ofNullable( NetworkInterface.getByIndex( 2 ) ).ifPresent( ni -> {
				      Optional.ofNullable( ni.getDisplayName() ).ifPresent( name ->
				        System.out.println( name.get().toUpperCase() )
				      );
				    });
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				System.out.println( name.get().toUpperCase() )
				                         ^^^
			The method get() is undefined for the type String
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				public class X {
				    public static void main (String[] args) {
				        Function<List<String>, String> func = ArrayList::toString;
				        System.out.println(func.apply(Arrays.asList("a", "b")));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Function<List<String>, String> func = ArrayList::toString;
				                                      ^^^^^^^^^^^^^^^^^^^
			The type ArrayList does not define toString(List<String>) that is applicable here
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				public class X {
				    public static void main (String[] args) {
				        Function<ArrayList<String>, String> func = ArrayList::toString;
				        System.out.println(func.apply(Arrays.asList("a", "b")));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				System.out.println(func.apply(Arrays.asList("a", "b")));
				                        ^^^^^
			The method apply(ArrayList<String>) in the type Function<ArrayList<String>,String> is not applicable for the arguments (List<String>)
			----------
			2. ERROR in X.java (at line 8)
				System.out.println(func.apply(Arrays.asList("a", "b")));
				                              ^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from List<String> to ArrayList<String>
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				public class X {
				    public static void main (String[] args) {
				        Function<ArrayList<String>, String> func = List::toString;
				        System.out.println(func.apply(Arrays.asList("a", "b")));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				System.out.println(func.apply(Arrays.asList("a", "b")));
				                        ^^^^^
			The method apply(ArrayList<String>) in the type Function<ArrayList<String>,String> is not applicable for the arguments (List<String>)
			----------
			2. ERROR in X.java (at line 8)
				System.out.println(func.apply(Arrays.asList("a", "b")));
				                              ^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from List<String> to ArrayList<String>
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				class Vector<E> extends ArrayList<E> {}
				interface I {
				    ArrayList<String> get();
				}
				public class X {
				    public static void main (String[] args) {
				        I i = ArrayList::new;
				        System.out.println(i.get());
				    }
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				class Vector<E> extends ArrayList<E> {}
				      ^^^^^^
			The serializable class Vector does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 14)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				class Vector<E> extends ArrayList<E> {}
				interface I {
				    List<String> get();
				}
				public class X {
				    public static void main (String[] args) {
				        I i = ArrayList::new;
				        System.out.println(i.get());
				    }
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				class Vector<E> extends ArrayList<E> {}
				      ^^^^^^
			The serializable class Vector does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 14)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857e() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				class Vector<E> extends ArrayList<E> {}
				interface I {
				    Vector<String> get();
				}
				public class X {
				    public static void main (String[] args) {
				        I i = ArrayList::new;
				        System.out.println(i.get());
				    }
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				class Vector<E> extends ArrayList<E> {}
				      ^^^^^^
			The serializable class Vector does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 11)
				I i = ArrayList::new;
				      ^^^^^^^^^^^^^^
			The constructed object of type ArrayList is incompatible with the descriptor's return type: Vector<String>
			----------
			3. ERROR in X.java (at line 14)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857f() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				class Vector<E> extends ArrayList<E> {}
				interface I {
				    ArrayList<String> get();
				}
				public class X {
				    public static void main (String[] args) {
				        I i = Vector::new;
				        System.out.println(i.get());
				    }
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				class Vector<E> extends ArrayList<E> {}
				      ^^^^^^
			The serializable class Vector does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 14)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857g() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				public class X {
				    public static void main (String[] args) {
				        Function<? extends ArrayList<String>, String> func = ArrayList::toString;
				        System.out.println(func.apply(Arrays.asList("a", "b")));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				System.out.println(func.apply(Arrays.asList("a", "b")));
				                        ^^^^^
			The method apply(capture#1-of ? extends ArrayList<String>) in the type Function<capture#1-of ? extends ArrayList<String>,String> is not applicable for the arguments (List<String>)
			----------
			2. ERROR in X.java (at line 8)
				System.out.println(func.apply(Arrays.asList("a", "b")));
				                              ^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from List<String> to capture#1-of ? extends ArrayList<String>
			----------
			""", null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429833, - [1.8][compiler] Missing types cause NPE in lambda analysis.
public void test429833() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I1 { int foo(Strin i); }
				class Y {
					I1 i = (a) -> {\s
						a.charAt(0);
					};
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				interface I1 { int foo(Strin i); }
				                       ^^^^^
			Strin cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				I1 i = (a) -> {\s
				       ^^^^^^
			This lambda expression refers to the missing type Strin
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429934, - [1.8][search] for references to type of lambda with 'this' parameter throws AIIOBE/NPE
public void test429934() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Function<String, String> f1= (String s, Function this) -> s;
						Function<String, String> f2= (Function this, String s) -> s;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Function<String, String> f1= (String s, Function this) -> s;
				^^^^^^^^
			Function cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				Function<String, String> f1= (String s, Function this) -> s;
				                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			3. ERROR in X.java (at line 3)
				Function<String, String> f1= (String s, Function this) -> s;
				                                        ^^^^^^^^
			Function cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 3)
				Function<String, String> f1= (String s, Function this) -> s;
				                                                 ^^^^
			Lambda expressions cannot declare a this parameter
			----------
			5. ERROR in X.java (at line 4)
				Function<String, String> f2= (Function this, String s) -> s;
				^^^^^^^^
			Function cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				Function<String, String> f2= (Function this, String s) -> s;
				                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			7. ERROR in X.java (at line 4)
				Function<String, String> f2= (Function this, String s) -> s;
				                              ^^^^^^^^
			Function cannot be resolved to a type
			----------
			8. ERROR in X.java (at line 4)
				Function<String, String> f2= (Function this, String s) -> s;
				                                       ^^^^
			Lambda expressions cannot declare a this parameter
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429969, [1.8][compiler] Possible RuntimeException in Lambda tangles ECJ
public void test429969() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.Optional;
					public class X {
					    public static void main(String[] args) {
					        final String s = Arrays.asList("done").stream().reduce(null, (s1,s2) -> {
					                // THE FOLLOWING LINE CAUSES THE PROBLEM
					                require(s1 != null || s2 != null, "both strings are null");
					                    return (s1 != null) ? s1 : s2;
					            }, (s1,s2) -> (s1 != null) ? s1 : s2);
					\t
					        System.out.println(s);
					    }
					    static void require(boolean condition, String msg) throws java.io.IOException {
					        if (!condition) {
					            throw new java.io.IOException(msg);
					        }
					    }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					import java.util.Optional;
					       ^^^^^^^^^^^^^^^^^^
				The import java.util.Optional is never used
				----------
				2. ERROR in X.java (at line 7)
					require(s1 != null || s2 != null, "both strings are null");
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unhandled exception type IOException
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429969, [1.8][compiler] Possible RuntimeException in Lambda tangles ECJ
public void test429969a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    void foo() throws RuntimeException;
					}
					public class X {
						static void goo() throws Exception {
							throw new Exception();
						}
						public static void main(String[] args) {
							I i = X::goo;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					I i = X::goo;
					      ^^^^^^
				Unhandled exception type Exception
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface incorrectly rejected as not being.
public void test430310() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface Func1<T1, R> {
					        R apply(T1 v1);
					        void other();
					}
					@FunctionalInterface // spurious error: F1<T, R> is not a functional interface
					interface F1<T1, R> extends Func1<T1, R> {
						default void other() {}
					}
					@FunctionalInterface
					interface F2<T1, R> extends Func1<T1, R> {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					interface F2<T1, R> extends Func1<T1, R> {
					          ^^
				Invalid '@FunctionalInterface' annotation; F2<T1,R> is not a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424154, [1.8][compiler] PolyTypeBinding must not render the full lambda body in error messages
//Example copied from bug report.
public void test424154a() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;\
				public class X {
					void foo(List<Process> list) {
						list.removeIf((int x) -> "");
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				list.removeIf((int x) -> "");
				     ^^^^^^^^
			The method removeIf(Predicate<? super Process>) in the type Collection<Process> is not applicable for the arguments ((int x) -> {})
			----------
			2. ERROR in X.java (at line 3)
				list.removeIf((int x) -> "");
				               ^^^
			Lambda expression's parameter x is expected to be of type Process
			----------
			3. ERROR in X.java (at line 3)
				list.removeIf((int x) -> "");
				                         ^^
			Type mismatch: cannot convert from String to boolean
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424154,  [1.8][compiler] PolyTypeBinding must not render the full lambda body in error messages
//Variations where return types or arguments mismatch or both.
public void test424154b() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					interface I {int foo(int x);}
					void foo2(I i) {}
					void foo() {}
					void bar() {
						foo(0, (int x, int y) -> {return 2;}, 0);
						foo2((int x) -> "");
						foo2((float x) -> 0);
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				foo(0, (int x, int y) -> {return 2;}, 0);
				^^^
			The method foo() in the type X is not applicable for the arguments (int, (int x, int y) -> {}, int)
			----------
			2. ERROR in X.java (at line 7)
				foo2((int x) -> "");
				^^^^
			The method foo2(X.I) in the type X is not applicable for the arguments ((int x) -> {})
			----------
			3. ERROR in X.java (at line 7)
				foo2((int x) -> "");
				                ^^
			Type mismatch: cannot convert from String to int
			----------
			4. ERROR in X.java (at line 8)
				foo2((float x) -> 0);
				^^^^
			The method foo2(X.I) in the type X is not applicable for the arguments ((float x) -> {})
			----------
			5. ERROR in X.java (at line 8)
				foo2((float x) -> 0);
				      ^^^^^
			Lambda expression's parameter x is expected to be of type int
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431514 [1.8] Incorrect compilation error in lambda expression
public void test431514() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Supplier;
				public class X {
					public void foo() {
						class Z {
							public Supplier<Object> get() {
								return () -> {
									class Z { }
									return new Z();
								};
							}
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				class Z { }
				      ^
			The nested type Z cannot hide an enclosing type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439707 [1.8][compiler] Lambda can be passed illegally to invisible method argument
public void test439707() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        T2.run(() -> {});
				    }
				}
				""",
			"T2.java",
			"""
				public class T2 {
				    public static void run(InvisibleInterface i) {
				    }
				    private interface InvisibleInterface {
				        void run();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				T2.run(() -> {});
				       ^^^^^
			The type T2.InvisibleInterface from the descriptor computed for the target context is not visible here. \s
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442983, [1.8] NPE in Scope.findDefaultAbstractMethod
public void test442983() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Function;
				class CL<T> {
					<F> String method1(CL<T> ie) {
						return "b";
					}
					public void bar() {	\t
						Function<CL<Integer>, String> v5 = CL::method1;
						v5 = t -> t.method1();\t
					}\t
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Function<CL<Integer>, String> v5 = CL::method1;
				                                   ^^^^^^^^^^^
			Cannot make a static reference to the non-static method method1(CL) from the type CL
			----------
			2. ERROR in X.java (at line 8)
				v5 = t -> t.method1();\t
				            ^^^^^^^
			The method method1(CL<Integer>) in the type CL<Integer> is not applicable for the arguments ()
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438945, [1.8] NullPointerException InferenceContext18.checkExpression in java 8 with generics, primitives, and overloading
public void test438945() {
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured,
		new String[] {
			"X.java",
			"""
				import java.util.function.ToIntFunction;
				import java.util.function.ToLongFunction;
				public class X {
				    public static void error() {
				        test(X::works);
				        test(X::broken);
				    }
				    private static <T> void test(ToLongFunction<T> func) {}
				    private static <T> void test(ToIntFunction<T> func) {}
				    private static int broken(Object o) { return 0; }
				    private static long works(Object o) { return 0; }\s
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method references with overloaded varargs method
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515, [1.8] ECJ reports error at method reference to overloaded instance method
public void test440643() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);

	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			"X.java",
			"""
				@FunctionalInterface
				interface Accumalator<E> {
				  void acum(Container<E> container, E data);
				}
				interface Container<E> {
				  public void add(E data);
				  @SuppressWarnings("unchecked")
				  public void add(E...data);
				}
				class Binding<E> {
				  private final Accumalator<E> function;
				 \s
				  public Binding() {
				    function = Container::add;
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				private final Accumalator<E> function;
				                             ^^^^^^^^
			The value of the field Binding<E>.function is not used
			----------
			""",
		null,
		false,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method references with overloaded varargs method
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515, [1.8] ECJ reports error at method reference to overloaded instance method
public void test440643a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Fun<T, R> {
					R apply(T arg);
				}
				public class X {
					static int size() {
						return -1;
					}
					static int size(Object arg) {
						return 0;
					}
					int size(X arg) {
						return 1;
					}
					public static void main(String args[]) {
						Fun<X, Integer> f1 = X::size;
						System.out.println(f1.apply(new X()));
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				Fun<X, Integer> f1 = X::size;
				                     ^^^^^^^
			Cannot make a static reference to the non-static method size(X) from the type X
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method references with overloaded varargs method
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515, [1.8] ECJ reports error at method reference to overloaded instance method
public void test440643b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Fun<T, R> {
					R apply(T arg);
				}
				public class X {
					int size() {
						return -1;
					}
					static int size(Object arg) {
						return 0;
					}
					public static void main(String args[]) {
						Fun<X, Integer> f1 = X::size;
						System.out.println(f1.apply(new X()));
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				Fun<X, Integer> f1 = X::size;
				                     ^^^^^^^
			Ambiguous method reference: both size() and size(Object) from the type X are eligible
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435397, [1.8][compiler] Ambiguous method while using Lambdas
public void test435397() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Function;
				interface B {}
				interface Config {}
				interface M {
				  void configure(B binder);
				}
				class M2 implements M {
				  public M2(final Config conf) {
				  }
				  public M2() {
				  }
				@Override
				  public void configure(final B binder) {
				  }
				}
				// BootModule
				class BaseModule implements M {
				  // eager module creation
				  public BaseModule module(final M m) {
				    return this;
				  }
				  // lazy module creation
				  public BaseModule module(final Function<Config, M> cons) {
				    return this;
				  }
				  @Override
				  public void configure(final B binder) {
				  }
				}
				// Client with error
				class M1 extends BaseModule {
				  public static void main(final String[] args) {
				    new M1().module((c) -> new M2());
				       // The method module(M) is ambiguous for the type M1
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 33)
				new M1().module((c) -> new M2());
				         ^^^^^^
			The method module(M) is ambiguous for the type M1
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433458, [1.8][compiler] Eclipse accepts lambda expression with potentially uninitialized arguments
public void test433458() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Comparator;\n" +
			"public class X {\n" +
			"    final Comparator mComparator1;\n" +
			//"    Comparator mComparator2 = mComparator1;\n" +
			"    Comparator mComparator2 = (pObj1, pObj2) -> mComparator1.compare(pObj1, pObj2);\n" +
			"    X() {mComparator1 = Comparator.naturalOrder();}\n" +
			"}\n"
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				final Comparator mComparator1;
				      ^^^^^^^^^^
			Comparator is a raw type. References to generic type Comparator<T> should be parameterized
			----------
			2. WARNING in X.java (at line 4)
				Comparator mComparator2 = (pObj1, pObj2) -> mComparator1.compare(pObj1, pObj2);
				^^^^^^^^^^
			Comparator is a raw type. References to generic type Comparator<T> should be parameterized
			----------
			3. WARNING in X.java (at line 4)
				Comparator mComparator2 = (pObj1, pObj2) -> mComparator1.compare(pObj1, pObj2);
				                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method compare(Object, Object) belongs to the raw type Comparator. References to generic type Comparator<T> should be parameterized
			----------
			4. ERROR in X.java (at line 4)
				Comparator mComparator2 = (pObj1, pObj2) -> mComparator1.compare(pObj1, pObj2);
				                                            ^^^^^^^^^^^^
			The blank final field mComparator1 may not have been initialized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433458, [1.8][compiler] Eclipse accepts lambda expression with potentially uninitialized arguments
public void test433458a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void foo();
				}
				class X {
					final int x;
					X() {
						I i = () -> {
							x = 20;
						};
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				X() {
				^^^
			The blank final field x may not have been initialized
			----------
			2. ERROR in X.java (at line 8)
				x = 20;
				^
			The final field X.x cannot be assigned
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433588, [1.8][compiler] ECJ compiles an ambiguous call in the presence of an unrelated unused method.
public void test433588() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in X.java (at line 15)
					public final @SafeVarargs void forEachOrdered(Consumer<? super T> action, Consumer<? super T>... actions) throws E {}
					                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The method forEachOrdered(Consumer<? super T>, Consumer<? super T>...) from the type X.AbstractStream<T,E,STREAM,SELF,CONSUMER> is never used locally
				----------
				2. ERROR in X.java (at line 29)
					lines1.forEachOrdered(s -> Files.isHidden(Paths.get(s)));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				3. ERROR in X.java (at line 30)
					lines1.forEachOrdered(s -> System.out.println(s));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				"""
			:
			"""
				----------
				1. WARNING in X.java (at line 15)
					public final @SafeVarargs void forEachOrdered(Consumer<? super T> action, Consumer<? super T>... actions) throws E {}
					                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The method forEachOrdered(Consumer<? super T>, Consumer<? super T>...) from the type X.AbstractStream<T,E,STREAM,SELF,CONSUMER> is never used locally
				----------
				2. WARNING in X.java (at line 17)
					private static class UnStream<T> extends AbstractStream<T, RuntimeException, Stream<T>, UnStream<T>, Consumer<? super T>> {}
					                     ^^^^^^^^
				Access to enclosing constructor X.AbstractStream<T,E,STREAM,SELF,CONSUMER>() is emulated by a synthetic accessor method
				----------
				3. WARNING in X.java (at line 18)
					private static class IOStream<T> extends AbstractStream<T, IOException, Stream<T>, IOStream<T>, IOConsumer<? super T>> {}
					                     ^^^^^^^^
				Access to enclosing constructor X.AbstractStream<T,E,STREAM,SELF,CONSUMER>() is emulated by a synthetic accessor method
				----------
				4. ERROR in X.java (at line 29)
					lines1.forEachOrdered(s -> Files.isHidden(Paths.get(s)));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				5. ERROR in X.java (at line 30)
					lines1.forEachOrdered(s -> System.out.println(s));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				import java.util.function.Consumer;
				import java.util.stream.Stream;
				public class X {
					private interface StreamyBase<T, E extends Exception> {
						@SuppressWarnings("unused")
						default void forEachOrdered(Consumer<? super T> action) throws E {}
					}
					abstract private static class AbstractStream<T, E extends Exception, STREAM, SELF extends AbstractStream<T, E, STREAM, SELF, CONSUMER>, CONSUMER> implements StreamyBase<T, E> {
						@SuppressWarnings("unused")
						public void forEachOrdered(CONSUMER action) throws E {}
						// remove this method with a warning about it being unused:
						public final @SafeVarargs void forEachOrdered(Consumer<? super T> action, Consumer<? super T>... actions) throws E {}
					}
					private static class UnStream<T> extends AbstractStream<T, RuntimeException, Stream<T>, UnStream<T>, Consumer<? super T>> {}
					private static class IOStream<T> extends AbstractStream<T, IOException, Stream<T>, IOStream<T>, IOConsumer<? super T>> {}
					@FunctionalInterface
					private interface ExConsumer<T, E extends Exception> {
						void accept(T t1) throws E;
					}
					@FunctionalInterface
					private interface IOConsumer<T> extends ExConsumer<T, IOException> {}
					public static void tests1(IOStream<String> lines1, UnStream<String> lines2) throws IOException {
						IOConsumer<? super String> action = s -> Files.isHidden(Paths.get(s));
						Consumer<? super String> action2 = s -> System.out.println(s);
						// After removal these two become ambiguous:
						lines1.forEachOrdered(s -> Files.isHidden(Paths.get(s)));
						lines1.forEachOrdered(s -> System.out.println(s));
						lines1.forEachOrdered(action);
						lines1.forEachOrdered(action2);
						lines2.forEachOrdered(action2);
					}
				}
				"""
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433588, [1.8][compiler] ECJ compiles an ambiguous call in the presence of an unrelated unused method.
public void test433588a() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in X.java (at line 29)
					lines1.forEachOrdered(s -> Files.isHidden(Paths.get(s)));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				2. ERROR in X.java (at line 30)
					lines1.forEachOrdered(s -> System.out.println(s));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				"""
			:
			"""
				----------
				1. WARNING in X.java (at line 17)
					private static class UnStream<T> extends AbstractStream<T, RuntimeException, Stream<T>, UnStream<T>, Consumer<? super T>> {}
					                     ^^^^^^^^
				Access to enclosing constructor X.AbstractStream<T,E,STREAM,SELF,CONSUMER>() is emulated by a synthetic accessor method
				----------
				2. WARNING in X.java (at line 18)
					private static class IOStream<T> extends AbstractStream<T, IOException, Stream<T>, IOStream<T>, IOConsumer<? super T>> {}
					                     ^^^^^^^^
				Access to enclosing constructor X.AbstractStream<T,E,STREAM,SELF,CONSUMER>() is emulated by a synthetic accessor method
				----------
				3. ERROR in X.java (at line 29)
					lines1.forEachOrdered(s -> Files.isHidden(Paths.get(s)));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				4. ERROR in X.java (at line 30)
					lines1.forEachOrdered(s -> System.out.println(s));
					       ^^^^^^^^^^^^^^
				The method forEachOrdered(X.IOConsumer<? super String>) is ambiguous for the type X.IOStream<String>
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				import java.util.function.Consumer;
				import java.util.stream.Stream;
				public class X {
					private interface StreamyBase<T, E extends Exception> {
						@SuppressWarnings("unused")
						default void forEachOrdered(Consumer<? super T> action) throws E {}
					}
					abstract private static class AbstractStream<T, E extends Exception, STREAM, SELF extends AbstractStream<T, E, STREAM, SELF, CONSUMER>, CONSUMER> implements StreamyBase<T, E> {
						@SuppressWarnings("unused")
						public void forEachOrdered(CONSUMER action) throws E {}
						// remove this method with a warning about it being unused:
						// public final @SafeVarargs void forEachOrdered(Consumer<? super T> action, Consumer<? super T>... actions) throws E {}
					}
					private static class UnStream<T> extends AbstractStream<T, RuntimeException, Stream<T>, UnStream<T>, Consumer<? super T>> {}
					private static class IOStream<T> extends AbstractStream<T, IOException, Stream<T>, IOStream<T>, IOConsumer<? super T>> {}
					@FunctionalInterface
					private interface ExConsumer<T, E extends Exception> {
						void accept(T t1) throws E;
					}
					@FunctionalInterface
					private interface IOConsumer<T> extends ExConsumer<T, IOException> {}
					public static void tests1(IOStream<String> lines1, UnStream<String> lines2) throws IOException {
						IOConsumer<? super String> action = s -> Files.isHidden(Paths.get(s));
						Consumer<? super String> action2 = s -> System.out.println(s);
						// After removal these two become ambiguous:
						lines1.forEachOrdered(s -> Files.isHidden(Paths.get(s)));
						lines1.forEachOrdered(s -> System.out.println(s));
						lines1.forEachOrdered(action);
						lines1.forEachOrdered(action2);
						lines2.forEachOrdered(action2);
					}
				}
				"""
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433735, [1.8] Discrepancy with javac when dealing with local classes in lambda expressions
public void test433735() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Supplier;
				class E {
					E(Supplier<Object> factory) { }
				}
				public class X extends E {
					X() {
						super( () -> {
							class Z extends E {
								Z() {
									super(new Supplier<Object>() {
										@Override
										public Object get() {
											return new Object();
										}
									});
								}
							}\s
							return new Z();
							});
					}
					public static void main(String[] args) {
						new X();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				super( () -> {
				       ^^^^^
			Cannot refer to 'this' nor 'super' while explicitly invoking a constructor
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432531 [1.8] VerifyError with anonymous subclass inside of lambda expression in the superclass constructor call
public void test432531a() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				import java.util.function.Supplier;
				class E {
					E(Supplier<Object> factory) { }
				}
				public class Y extends E {
					Y() {
						super( () -> {
							class Z extends E {
								Z() {
									super(() -> new Object());
								}
							}
							return new Z();
							});
					}
					public static void main(String[] args) {
						new Y();
					}
				}"""
	},
	"""
		----------
		1. ERROR in Y.java (at line 7)
			super( () -> {
			       ^^^^^
		Cannot refer to 'this' nor 'super' while explicitly invoking a constructor
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432605, [1.8] Incorrect error "The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here"
public void _test432605() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.HashMap;
				import java.util.function.Function;
				import java.util.function.Supplier;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				public class X {
				static <T, E extends Exception, K, L, M> M terminalAsMapToList(
				    Function<? super T, ? extends K> classifier,
				    Function<HashMap<K, L>, M> intoMap,
				    Function<ArrayList<T>, L> intoList,
				    Supplier<Stream<T>> supplier,
				    Class<E> classOfE) throws E {
				  	return terminalAsCollected(
				  	  classOfE,
				  	  Collectors.collectingAndThen(
				  	    Collectors.groupingBy(
				  	      classifier,
				  	      HashMap<K, L>::new,
				  	      Collectors.collectingAndThen(
				  	      	// The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here
				  	      	// from ArrayList<T>::add:
				  	        Collector.of(ArrayList<T>::new, ArrayList<T>::add, (ArrayList<T> left, ArrayList<T> right) -> {\s
				  		        left.addAll(right);
				  		        return left;
				  	        }),
				  	        intoList)),
				  	    intoMap),
				  	  supplier);
				  }
					static <E extends Exception, T, M> M terminalAsCollected(
				    Class<E> class1,
				    Collector<T, ?, M> collector,
				    Supplier<Stream<T>> supplier) throws E {
				  	try(Stream<T> s = supplier.get()) {
				  		return s.collect(collector);
				  	} catch(RuntimeException e) {
				  		throw unwrapCause(class1, e);
				  	}
				  }
					static <E extends Exception> E unwrapCause(Class<E> classOfE, RuntimeException e) throws E {
						Throwable cause = e.getCause();
						if(classOfE.isInstance(cause) == false) {
							throw e;
						}
						throw classOfE.cast(cause);
				}
				}
				"""
	},
	"""
		----------
		1. ERROR in Y.java (at line 7)
			super( () -> {
			       ^^^^^
		No enclosing instance of type Y is available due to some intermediate constructor invocation
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444665, Internal compiler error: java.lang.NullPointerException at org.eclipse.jdt.internal.compiler.problem.ProblemReporter.invalidMethod
public void test444665() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static void foo(java.util.Map<Long, Long> map) {
				        java.util.function.Consumer<int[]> c = array -> map.compute(array.get(0), (k, v) -> null);
				    }
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 3)
			java.util.function.Consumer<int[]> c = array -> map.compute(array.get(0), (k, v) -> null);
			                                                            ^^^^^^^^^^^^
		Cannot invoke get(int) on the array type int[]
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442446, [1.8][compiler] compiler unable to infer lambda's generic argument types
public void test442446() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				import java.util.Map;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				public class X {
				  X(Collection<Object> pCollection) {
				    this(
				      pCollection.stream().collect(
				        Collectors.toMap(
				          Function.identity(), pElement -> 1, (pInt1, pInt2) -> pInt1 + pInt2
				        )
				      )
				    );
				  }
				  X(Map<Object,Integer> pMap) {}
				}
				"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432759,  [1.8][compiler] Some differences between Javac and ECJ regarding wildcards and static methods
public void test432759() {
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.JavacDoesNotCompileCorrectSource,
		new String[] {
			"X.java",
			"""
				import java.util.function.BinaryOperator;
				import java.util.function.Consumer;
				
				/*Q*/
				@FunctionalInterface interface Subsumer<T> {	void accept(T t);
				  default                                 Subsumer<T> andThe1(                  Subsumer<? super T> afterT) { return (T t) -> {      accept(t); afterT.accept(t); }; }
				  default                                 Subsumer<T> andThe2(Subsumer<T> this, Subsumer<? super T> afterT) { return (T t) -> { this.accept(t); afterT.accept(t); }; }
				  static <U>                              Subsumer<U> andThe3(Subsumer<U> tihs, Subsumer<? super U> afterU) { return (U u) -> { tihs.accept(u); afterU.accept(u); }; }
				  static <S extends ISSUPER_S, ISSUPER_S> Subsumer<S> andThe4(Subsumer<S> tihs, Subsumer<ISSUPER_S> afterS) { return (S s) -> { tihs.accept(s); afterS.accept(s); }; }
				}
				public class X {
					static <T extends ISSUPER_T, ISSUPER_T> void method() {
						BinaryOperator<Consumer<? super T>> attempt_X_0 = Consumer::andThen;
						BinaryOperator<Subsumer<? super T>> attempt_X_1 = Subsumer::andThe1;
						BinaryOperator<Subsumer<? super T>> attempt_X_2 = Subsumer::andThe2;
						BinaryOperator<Subsumer<? super T>> attempt_X_3 = Subsumer::andThe3;
						BinaryOperator<Subsumer<? super T>> attempt_X_4 = Subsumer::andThe4;
						BinaryOperator<Consumer<ISSUPER_T>> attempt_n_0 = Consumer::andThen;
						BinaryOperator<Subsumer<ISSUPER_T>> attempt_n_1 = Subsumer::andThe1;
						BinaryOperator<Subsumer<ISSUPER_T>> attempt_n_2 = Subsumer::andThe2;
						BinaryOperator<Subsumer<ISSUPER_T>> attempt_n_3 = Subsumer::andThe3;
						BinaryOperator<Subsumer<ISSUPER_T>> attempt_n_4 = Subsumer::andThe4;
						// Summary:
						// ECJ error #1, javac no error
						// ECJ error #2, javac no error
						// ECJ error #3, javac no error
						// ECJ error #4, javac error #1
						// ECJ error #5, javac error #2
						// ECJ no error, javac no error
						// ECJ no error, javac no error
						// ECJ no error, javac no error
						// ECJ no error, javac no error
						// ECJ no error, javac no error
					}
				}
				"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c36,  NPE in broken code
public void test437444() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.stream.Collectors;
				public class X {
					public static void main(String[] args) {
						List<Person> roster = new ArrayList<>();
				        Map<String, Person> map =\s
				            roster
				                .stream()
				                .collect(
				                    Collectors.toMap(
				                        Person::getLast,
				                        Function.identity()\s
				                    ));
					}
				}
				class Person {
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 7)
			Map<String, Person> map =\s
			^^^
		Map cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 13)
			Function.identity()\s
			^^^^^^^^
		Function cannot be resolved
		----------
		""");
}
// test ground target type with wildcards left in non parameter positions.
public void testGroundTargetTypeWithWithWildcards() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A {}
				class B {}
				class C {}
				class Y extends C {}
				interface I<R, S, T> {
				    T m(R r, S s);
				}
				public class X extends A {
				    Object m(I<? extends A, ? extends B, ? extends C> i) {
				    	return m((X x1, X x2) -> { return new Y(); });
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				return m((X x1, X x2) -> { return new Y(); });
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from I<X,X,C> to I<? extends A,? extends B,? extends C>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474522, [1.8][compiler] ecj doesn't handle captured final fields correctly in lambdas
public void test474522() {
	this.runNegativeTest(
		new String[] {
			"Bug.java",
			"""
				import java.awt.event.ActionEvent;
				import java.awt.event.ActionListener;
				public class Bug {
				    final String s;
				    public Bug() {
				        this.s = "";
				    }
				    private final ActionListener listener1 = new ActionListener() {
				        @Override public void actionPerformed(ActionEvent e) {
				            System.out.println(s);
				        }
				    };
				    private final ActionListener listener2 = e -> System.out.println(s);
				}
				"""
		},
		"""
			----------
			1. WARNING in Bug.java (at line 8)
				private final ActionListener listener1 = new ActionListener() {
				                             ^^^^^^^^^
			The value of the field Bug.listener1 is not used
			----------
			2. WARNING in Bug.java (at line 13)
				private final ActionListener listener2 = e -> System.out.println(s);
				                             ^^^^^^^^^
			The value of the field Bug.listener2 is not used
			----------
			3. ERROR in Bug.java (at line 13)
				private final ActionListener listener2 = e -> System.out.println(s);
				                                                                 ^
			The blank final field s may not have been initialized
			----------
			""");
}
public void testBug487390() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface ConsumeN {
					void consume(String.. strings); // syntax error here
				}
				public class X {
				
					void consu(ConsumeN c) { }
					void test() {
						consu((String... s) -> System.out.print(s.length));
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				void consume(String.. strings); // syntax error here
				                    ^
			Syntax error on token ".", Identifier expected
			----------
			2. ERROR in X.java (at line 8)
				consu((String... s) -> System.out.print(s.length));
				^^^^^
			The method consu(ConsumeN) in the type X is not applicable for the arguments ((String... s) -> {})
			----------
			3. ERROR in X.java (at line 8)
				consu((String... s) -> System.out.print(s.length));
				      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Lambda expression's signature does not match the signature of the functional interface method consume()
			----------
			""");
}
public void testBug487390b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface ConsumeN {
					void consume();
				}
				public class X {
				
					void consu(ConsumeN c) { }
					void test() {
						consu((String... s) -> System.out.print(s.length));
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				consu((String... s) -> System.out.print(s.length));
				^^^^^
			The method consu(ConsumeN) in the type X is not applicable for the arguments ((String... s) -> {})
			----------
			2. ERROR in X.java (at line 8)
				consu((String... s) -> System.out.print(s.length));
				      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Lambda expression's signature does not match the signature of the functional interface method consume()
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=458332, [1.8][compiler] only 409 method references/lambda expressions per class possible
public void testBug458332() {
	runConformTest(
		false,
		null,
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.util.function.Consumer;
				public class Test {
					public static void main(String[] args) {
						System.out.println(Data.part1().length);
						System.out.println(Data.part2().length);
					}
					@FunctionalInterface
					private static interface MethodRef extends Consumer<String[]>, Serializable {}
					private static class Data {
						static MethodRef[] part1() {
							return new MethodRef[] {
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
								};
						}
						static MethodRef[] part2() {
							return new MethodRef[] {
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
									Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main, Test::main,
								};
						}
					}
				}
				"""
		},
		"450\n" +
		"250");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/125
// Java parser/compiler accepts invalid source code with lambdas
public void testGH125() {
	this.runNegativeTest(
		new String[] {
			"ParserBug.java",
			"""
				import java.util.function.Supplier;
				
				public class ParserBug {
					Supplier<? extends Integer> how;\s
					public ParserBug() {
						//This is not executed, meaning the whole problematic statement does not begin to execute.
						System.out.println("Constrctor");
					}
					public ParserBug recompute(Supplier<? extends Integer> how) {
						this.how = how;
						return this;
					}
					public static void main(String[] args) {
						System.out.println("Parser is wonky:");
						if(false) { //condition is ignored
							System.out.println("The following statement does not even begin execution");
				
				
							Math impossibleObject = new ParserBug()
									.recompute(()->true?0:false?1:2)
									)) //Umantched parentheses
				.nonexistentMethod();
				//Wrong autoindent, no syntax highligting
				System.out.println("Statements after are not executed");
				System.out.println(impossibleObject);
				
				Void impossibleObject //no complaint about redeclared variable
				= 42 ; //no typechecking here
				
						}else {
							System.out.println("Else branch (is never executed)");
						}
					}
					{
						//Variable not declared? No problem!
						doItAgain = new ParserBug()
								.recompute(()->true?0:false?1:2)
								))))))) // Not even for this many
				.nonexistentMethod();
					}
				
				}
				""",
		},
		"""
			----------
			1. WARNING in ParserBug.java (at line 9)
				public ParserBug recompute(Supplier<? extends Integer> how) {
				                                                       ^^^
			The parameter how is hiding a field from type ParserBug
			----------
			2. ERROR in ParserBug.java (at line 20)
				.recompute(()->true?0:false?1:2)
				                              ^
			Syntax error on token(s), misplaced construct(s)
			----------
			""");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/367
// ECJ accepts invalid syntax without error
public void testGH367() {
	this.runNegativeTest(
		new String[] {
			"EclipseParserBug.java",
			"""
				public class EclipseParserBug  {
				    public static void copy(boolean[] src, boolean[] dst) {
				        long start=System.nanoTime();
				        IntStream.range(0, src.length).parallel().forEach(i -> dst[i] = src[i]))); 	 \s
				        System.out.println("Copy took: " + (System.nanoTime() - start));
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in EclipseParserBug.java (at line 4)
				IntStream.range(0, src.length).parallel().forEach(i -> dst[i] = src[i]))); 	 \s
				                                                                     ^
			Syntax error on token(s), misplaced construct(s)
			----------
			""");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/859
// Invalid code not rejected by compiler
public void testGH859() {
	this.runNegativeTest(
		new String[] {
			"CFSXXX.java",
			"""
				import java.util.function.Consumer;
				import java.util.function.Supplier;
				
				/**
				 * CFSXXX
				 *\s
				 * @author connors
				 */
				public class CFSXXX {
				
				    public static void main(String[] args) {
				        boolean mybool_XXXWhoopsGotTheWrongNameHereXXX = true;
				        if (mybool) {
				            System.out.println("within if - true part");
				            setSupplier(() -> x -> System.out.println("x" + x); );
				        } else {
				            System.out.println("within if - false part");
				        }
				    }
				   \s
				    public static void setSupplier(Supplier<Consumer<String>> supplier) {
				        System.out.println("setSupplier called: " + supplier);
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in CFSXXX.java (at line 15)
				setSupplier(() -> x -> System.out.println("x" + x); );
				                                                 ^
			Syntax error on token(s), misplaced construct(s)
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=553601
// Compile without errors invalid source code
public void test553601() {
	this.runNegativeTest(
		new String[] {
			"TestOptional3.java",
			"""
				import java.util.function.Supplier;
				public class TestOptional3 {
				    public static void main(String[] args) throws Exception {
				        supplier.get();  // !!!!!!!!! NullPointerException
				    }
				   \s
				    public static Supplier<Object> supplier = () -> {
				        try {
				            Optional.ofNullable(null)
				                    .orElseThrow(() -> new Exception()))))))))))))))))))))))))))))))))))))))))))))));
				                    //                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				        } catch (Exception ex) { }
				       \s
				        return "test";
				    };
				}
				""",
		},
		"""
			----------
			1. ERROR in TestOptional3.java (at line 10)
				.orElseThrow(() -> new Exception()))))))))))))))))))))))))))))))))))))))))))))));
				                                                           ^
			Syntax error on token(s), misplaced construct(s)
			----------
			""");
}

public void testIssue810() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"class Foo {\n"+
				"static void foo() { \n"+
				"run(Bar::privateMethod); // <-- warning: Access to enclosing method ... emulated by a synthetic accessor\n"+// method
				"}\n" +
				"public static void main(String[] args) {\n"+
				"Zork z; \n"+
				"}\n" +
				"static class Bar { \n"+
				"private static void privateMethod() { \n" +
				/**/" }\n" +
				"}\n" +
				"static void run(Runnable r) { \n"+
				"r.run(); \n"+
				"} \n"+
			    "}\n"},
				"""
					----------
					1. WARNING in X.java (at line 3)
						run(Bar::privateMethod); // <-- warning: Access to enclosing method ... emulated by a synthetic accessor
						    ^^^^^^^^^^^^^^^^^^
					Access to privateMethod() from the type Foo.Bar is emulated by a synthetic accessor method
					----------
					2. ERROR in X.java (at line 6)
						Zork z;\s
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
}

public static Class testClass() {
	return NegativeLambdaExpressionsTest.class;
}
}
