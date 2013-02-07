/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
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
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;
public class NegativeLambdaExpressionsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test380112e"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public NegativeLambdaExpressionsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382818, ArrayStoreException while compiling lambda
public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = () -> {\n" +
				"      int z = 10;\n" +
				"    };\n" +
				"    i++;\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I i = () -> {\n" + 
			"      int z = 10;\n" + 
			"    };\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	i++;\n" + 
			"	^^^\n" + 
			"Type mismatch: cannot convert from I to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382841, ClassCastException while compiling lambda
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				" void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = (p, q) -> {\n" +
				"      int r = 10;\n" +
				"    };\n" +
				"    i++;\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	i++;\n" + 
			"	^^^\n" + 
			"Type mismatch: cannot convert from I to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382841, ClassCastException while compiling lambda
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				" void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = null, i2 = (p, q) -> {\n" +
				"      int r = 10;\n" +
				"    }, i3 = null;\n" +
				"    i++;\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	i++;\n" + 
			"	^^^\n" + 
			"Type mismatch: cannot convert from I to int\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, syntax error reported incorrectly on syntactically valid lambda expression
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX {\n" +
				"    public void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"     IX i = () -> 42;\n" +
				"     int x\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	IX i = () -> 42;\n" + 
			"	             ^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	int x\n" + 
			"	    ^\n" + 
			"Syntax error, insert \";\" to complete FieldDeclaration\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383085 super::identifier not accepted.
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX{\n" +
				"	public void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	IX i = super::toString;\n" +
				"   Zork z;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, syntax error reported incorrectly on *syntactically* valid reference expression
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX{\n" +
				"	public void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" +
				"   int x\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	       ^^^^^\n" + 
			"Outer cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	             ^^^\n" + 
			"One cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	                  ^^^\n" + 
			"Two cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	                             ^^^^^\n" + 
			"Three cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	                                    ^^^^\n" + 
			"Four cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	                                                 ^^^^\n" + 
			"Five cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 5)\n" + 
			"	IX i = Outer<One, Two>.Inner<Three, Four>.Deeper<Five, Six<String>>.Leaf::<Blah, Blah>method;\n" + 
			"	                                                       ^^^\n" + 
			"Six cannot be resolved to a type\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 6)\n" + 
			"	int x\n" + 
			"	    ^\n" + 
			"Syntax error, insert \";\" to complete FieldDeclaration\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383096, NullPointerException with a wrong lambda code snippet
public void _test007() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {}\n" +
					"public class X {\n" +
					"    void foo() {\n" +
					"            I t1 = f -> {{};\n" +
					"            I t2 = () -> 42;\n" +
					"        } \n" +
					"        }\n" +
					"}\n",
				},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	int\n" + 
			"	^^^\n" + 
			"Syntax error on token \"int\", delete this token\n" + 
			"----------\n" /* expected compiler log */,
			true /* perform statement recovery */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383949,  Explicit this parameter illegal in lambda expressions
public void test008() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  int foo(X x);\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i = (X this) -> 10;  \n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	I i = (X this) -> 10;  \n" + 
				"	         ^^^^\n" + 
				"Lambda expressions cannot declare a this parameter\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383949,  Explicit this parameter illegal in lambda expressions
public void test009() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.awt.event.ActionListener;\n" +
					"interface I {\n" +
					"    void doit(String s1, String s2);\n" +
					"}\n" +
					"public class X {\n" +
					"  public void test1(int x) {\n" +
					"    ActionListener al = (public xyz) -> System.out.println(xyz); \n" +
					"    I f = (abstract final s, @Nullable t) -> System.out.println(s + t); \n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	ActionListener al = (public xyz) -> System.out.println(xyz); \n" + 
				"	                            ^^^\n" + 
				"Syntax error, modifiers and annotations are not allowed for the lambda parameter xyz as its type is elided\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	I f = (abstract final s, @Nullable t) -> System.out.println(s + t); \n" + 
				"	                      ^\n" + 
				"Syntax error, modifiers and annotations are not allowed for the lambda parameter s as its type is elided\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	I f = (abstract final s, @Nullable t) -> System.out.println(s + t); \n" + 
				"	                                   ^\n" + 
				"Syntax error, modifiers and annotations are not allowed for the lambda parameter t as its type is elided\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381121,  [] should be accepted in reference expressions.
public void test010() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Object foo(int [] ia);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (int [] ia) -> {\n" +
					"		      return ia.clone();\n" +
					"	      };\n" +
					"	I i2 = int[]::clone;\n" +
					"	Zork z;\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
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
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	unknown = 0;\n" + 
				"	^^^^^^^\n" + 
				"unknown cannot be resolved to a variable\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 8)\n" + 
				"	;		int a = 42 + ia;\n" + 
				"	 		        ^^^^^^^\n" + 
				"The operator + is undefined for the argument type(s) int, int[]\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 12)\n" + 
				"	I i = (int [] ia) -> this;\n" + 
				"	                     ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n" +
				"5. ERROR in X.java (at line 16)\n" + 
				"	int b = 42 + array;\n" + 
				"	        ^^^^^^^^^^\n" + 
				"The operator + is undefined for the argument type(s) int, int[]\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 18)\n" + 
				"	return;\n" + 
				"	^^^^^^^\n" + 
				"This method must return a result of type Object\n" +
				"----------\n" + 
				"7. ERROR in X.java (at line 20)\n" + 
				"	Runnable r = () -> { return 42; };\n" + 
				"	                     ^^^^^^^^^^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n"
);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384600, [1.8] 'this' should not be allowed in lambda expressions in contexts that don't allow it
public void test012() {
	// This test checks that common semantic checks are indeed 
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"	static void foo() {\n" +
					"		I i = () -> {\n" +
					"			System.out.println(this);\n" +
					"			I j = () -> {\n" +
					"				System.out.println(this);\n" +
					"				I k = () -> {\n" +
					"					System.out.println(this);\n" +
					"				};\n" +
					"			};\n" +
					"		};\n" +
					"	}\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	System.out.println(this);\n" + 
				"	                   ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	System.out.println(this);\n" + 
				"	                   ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	System.out.println(this);\n" + 
				"	                   ^^^^\n" + 
				"Cannot use this in a static context\n" + 
				"----------\n"
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384600, [1.8] 'this' should not be allowed in lambda expressions in contexts that don't allow it
public void test013() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void doit();\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo(Zork z) {\n" +
					"		I i = () -> {\n" +
					"			System.out.println(this);\n" +
					"			I j = () -> {\n" +
					"				System.out.println(this);\n" +
					"				I k = () -> {\n" +
					"					System.out.println(this);\n" +
					"				};\n" +
					"			};\n" +
					"		};\n" +
					"	}\n" +
					"}\n" ,
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	void foo(Zork z) {\n" + 
				"	         ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n"
				);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384595, Reject illegal modifiers on lambda arguments.
public void test014() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int x, int y, int z);	\n" +
					"}\n" +
					"public class X {\n" +
					"     I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                             ^^^^^^^^^\n" + 
				"Undefined cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                              ^^^^^^\n" + 
				"Lambda expression\'s parameter o is expected to be of type int\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                                     ^\n" + 
				"Illegal modifier for parameter o; only final is permitted\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	I i = (final @Marker int x, @Undefined static strictfp public Object o, static volatile int p) -> x;\n" + 
				"	                                                                                            ^\n" + 
				"Illegal modifier for parameter p; only final is permitted\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 5)\n" + 
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
					"import java.util.Collection;\n" +
					"import java.util.List;\n" +
					"interface I { void run(int x); }\n" +
					"interface J { void run(int x, String s); }\n" +
					"interface K { void run(Collection<String> jobs); }\n" +
					"class X {\n" +
					"    I i1 = (String y) -> {};\n" +
					"    I i2 = (y) -> {};\n" +
					"    I i3 = y -> {};\n" +
					"    I i4 = (int x, String y) -> {};\n" +
					"    I i5 = (int x) -> {};\n" +
					"    J j1 = () -> {};\n" +
					"    J j2 = (x, s) -> {};\n" +
					"    J j3 = (String x, int s) -> {};\n" +
					"    J j4 = (int x, String s) -> {};\n" +
					"    J j5 = x ->  {};\n" +
					"    K k1 = (Collection l) -> {};\n" +
					"    K k2 = (Collection <Integer> l) -> {};\n" +
					"    K k3 = (Collection <String> l) -> {};\n" +
					"    K k4 = (List <String> l) -> {};\n" +
					"    K k5 = (l) -> {};\n" +
					"    K k6 = l -> {};\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	I i1 = (String y) -> {};\n" + 
				"	        ^^^^^^\n" + 
				"Lambda expression\'s parameter y is expected to be of type int\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i4 = (int x, String y) -> {};\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 12)\n" + 
				"	J j1 = () -> {};\n" + 
				"	       ^^^^^^^^\n" + 
				"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 14)\n" + 
				"	J j3 = (String x, int s) -> {};\n" + 
				"	        ^^^^^^\n" + 
				"Lambda expression\'s parameter x is expected to be of type int\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 14)\n" + 
				"	J j3 = (String x, int s) -> {};\n" + 
				"	                  ^^^\n" + 
				"Lambda expression\'s parameter s is expected to be of type String\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 16)\n" + 
				"	J j5 = x ->  {};\n" + 
				"	       ^^^^^^^^\n" + 
				"Lambda expression\'s signature does not match the signature of the functional interface method\n" + 
				"----------\n" + 
				"7. WARNING in X.java (at line 17)\n" + 
				"	K k1 = (Collection l) -> {};\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 17)\n" + 
				"	K k1 = (Collection l) -> {};\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Lambda expression\'s parameter l is expected to be of type Collection<String>\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 18)\n" + 
				"	K k2 = (Collection <Integer> l) -> {};\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Lambda expression\'s parameter l is expected to be of type Collection<String>\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 20)\n" + 
				"	K k4 = (List <String> l) -> {};\n" + 
				"	        ^^^^\n" + 
				"Lambda expression\'s parameter l is expected to be of type Collection<String>\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test016() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  String foo();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i2 = () -> \"Hello\";\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> { return \"Hello\"; };\n" +
					"    I i5 = () -> {};\n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	I i1 = () -> 42;\n" + 
				"	             ^^\n" + 
				"Type mismatch: cannot convert from int to String\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	I i3 = () -> { return 42; };\n" + 
				"	                      ^^\n" + 
				"Type mismatch: cannot convert from int to String\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 10)\n" + 
				"	I i5 = () -> {};\n" + 
				"	       ^^^^^^^^\n" + 
				"This method must return a result of type String\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test017() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  Integer foo();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i2 = () -> \"Hello\";\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> { return \"Hello\"; };\n" +
					"    I i5 = () -> {};\n" +
					"  }\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	I i2 = () -> \"Hello\";\n" + 
				"	             ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to Integer\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i4 = () -> { return \"Hello\"; };\n" + 
				"	                      ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to Integer\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 10)\n" + 
				"	I i5 = () -> {};\n" + 
				"	       ^^^^^^^^\n" + 
				"This method must return a result of type Integer\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test018() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  I foo();\n" +
					"}\n" +
					"class P implements I {\n" +
					"   public I foo() { return null; }\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i2 = () -> \"Hello\";\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> { return \"Hello\"; };\n" +
					"    I i5 = () -> { return new P(); };\n" +
					"  }\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	I i1 = () -> 42;\n" + 
				"	             ^^\n" + 
				"Type mismatch: cannot convert from int to I\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 10)\n" + 
				"	I i2 = () -> \"Hello\";\n" + 
				"	             ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to I\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	I i3 = () -> { return 42; };\n" + 
				"	                      ^^\n" + 
				"Type mismatch: cannot convert from int to I\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 12)\n" + 
				"	I i4 = () -> { return \"Hello\"; };\n" + 
				"	                      ^^^^^^^\n" + 
				"Type mismatch: cannot convert from String to I\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test019() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    I i1 = () -> 42;\n" +
					"    I i3 = () -> { return 42; };\n" +
					"    I i4 = () -> System.out.println();\n" +
					"    I i5 = () -> { System.out.println(); };\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	I i1 = () -> 42;\n" + 
				"	             ^^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	I i3 = () -> { return 42; };\n" + 
				"	               ^^^^^^^^^^\n" + 
				"Void methods cannot return a value\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test020() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  int foo(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"    I i5 = (x) -> { if (x == 0) throw new NullPointerException(); };\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	I i5 = (x) -> { if (x == 0) throw new NullPointerException(); };\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"This method must return a result of type int\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test021() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  int foo(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"    I i5 = (x) -> { if (x == 0) throw new NullPointerException(); throw new NullPointerException(); };\n" +
					"    Zork z;\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test022() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  J foo();\n" +
					"}\n" +
					"interface J {\n" +
					"  int foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    I I = () -> () -> 10;\n" +
					"    Zork z;\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test023() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  J foo();\n" +
					"}\n" +
					"interface J {\n" +
					"  int foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    I i1 = () -> 10;\n" +
					"    I i2 = () -> { return 10; };\n" +
					"    I i3 = () -> () -> 10;\n" +
					"    I i4 = () -> { return () -> 10; };\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	I i1 = () -> 10;\n" + 
				"	             ^^\n" + 
				"Type mismatch: cannot convert from int to J\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 9)\n" + 
				"	I i2 = () -> { return 10; };\n" + 
				"	                      ^^\n" + 
				"Type mismatch: cannot convert from int to J\n" + 
				"----------\n");
}
// Bug 398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test024() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I3 {\n" +
					"  Object foo();\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I3 i = () -> 42; // Warning: Autoboxing, but casting to Object??\n" +
					"  }\n" +
					"  Object foo(Zork z) {\n" +
					"	  return 42;\n" +
					"  }\n" +
					"}\n",
				}, 
				"----------\n" + 
				"1. ERROR in X.java (at line 8)\n" + 
				"	Object foo(Zork z) {\n" + 
				"	           ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test025() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  String foo();\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    I i = () -> 42;\r\n" + 
			"    I i2 = () -> \"Hello, Lambda\";\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i = () -> 42;\n" + 
			"	            ^^\n" + 
			"Type mismatch: cannot convert from int to String\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test026() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  String foo();\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    I i = () -> {\r\n" +
			"      return 42;\r\n" +
			"    };\r\n" + 
			"    I i2 = () -> {\r\n" +
			"      return \"Hello, Lambda as a block!\";\r\n" +
			"    };\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	return 42;\n" + 
			"	       ^^\n" + 
			"Type mismatch: cannot convert from int to String\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test027() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  int baz();\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\n" + 
			"    I i1 = () -> {\n" + 
			"      System.out.println(\"No return\");\n" + 
			"    }; // Error: Lambda block should return value\n" + 
			"    I i2 = () -> {\n" + 
			"      if (Math.random() < 0.5) return 42;\n" + 
			"    }; // Error: Lambda block doesn't always return a value\n" + 
			"    I i3 = () -> {\n" + 
			"      return 42;\n" + 
			"      System.out.println(\"Dead!\");\n" + 
			"    }; // Error: Lambda block has dead code\n" + 
			"  }\n" + 
			"  public static I doesFlowInfoEscape() {\n" + 
			"    I i1 = () -> {\n" + 
			"      return 42;\n" + 
			"    };\n" + 
			"    return i1; // Must not complain about unreachable code!\n" + 
			"  }\n" + 
			"  public static I areExpresionsCheckedForReturns() {\n" + 
			"    I i1 = () -> 42;  // Must not complain about missing return!\n" + 
			"    return i1;\n" + 
			"  }\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i1 = () -> {\n" + 
			"      System.out.println(\"No return\");\n" + 
			"    }; // Error: Lambda block should return value\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This method must return a result of type int\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	I i2 = () -> {\n" + 
			"      if (Math.random() < 0.5) return 42;\n" + 
			"    }; // Error: Lambda block doesn\'t always return a value\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This method must return a result of type int\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 14)\n" + 
			"	System.out.println(\"Dead!\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unreachable code\n" + 
			"----------\n");
}
// Bug 399979 - [1.8][compiler] Statement expressions should be allowed in non-block lambda body when return type is void (edit) 
public void test028() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"  int data;\n" +
			"  public void main(String[] args) {\n" +
			"    I i1 = () -> data++;\n" +
			"    I i2 = () -> data = 10;\n" +
			"    I i3 = () -> data += 10;\n" +
			"    I i4 = () -> --data;\n" +
			"    I i5 = () -> bar();\n" +
			"    I i6 = () -> new X();\n" +
			"    I i7 = () -> 0;\n" +
			"    I i = () -> 1 + data++;\n" +
			"  }\n" +
			"  int bar() {\n" +
			"	  return 0;\n" +
			"  }\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	I i7 = () -> 0;\n" + 
			"	             ^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	I i = () -> 1 + data++;\n" + 
			"	            ^^^^^^^^^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n");
}
// Bug 384600 - [1.8] 'this' should not be allowed in lambda/Reference expressions in contexts that don't allow it
public void test029() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"	void doit();\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	static void foo() {\n" +
			"		I i1 = this::zoo;\n" +
			"		I i2 = super::boo;\n" +
			"		I i3 = () -> super.zoo();\n" +
			"		I i4 = () -> this.boo();\n" +
			"	}\n" +
			"	void boo () {\n" +
			"		I i1 = this::zoo;\n" +
			"		I i2 = super::boo;\n" +
			"		I i3 = () -> super.zoo();\n" +
			"		I i4 = () -> this.boo();\n" +
			"	}\n" +
			"	void zoo() {\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"	void boo() {\n" +
			"	}\n" +
			"	void zoo() {\n" +
			"	}\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	I i1 = this::zoo;\n" + 
			"	       ^^^^\n" + 
			"Cannot use this in a static context\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	I i2 = super::boo;\n" + 
			"	       ^^^^^\n" + 
			"Cannot use super in a static context\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	I i3 = () -> super.zoo();\n" + 
			"	             ^^^^^\n" + 
			"Cannot use super in a static context\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	I i4 = () -> this.boo();\n" + 
			"	             ^^^^\n" + 
			"Cannot use this in a static context\n" + 
			"----------\n");
}
// Bug 382713 - [1.8][compiler] Compiler should reject lambda expressions when target type is not a functional interface
public void test030() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  void foo();\n" +
			"  void goo();\n" +
			"}\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    X x = () -> 10;\n" +
			"    I i = () -> 10;\n" +
			"  }\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	X x = () -> 10;\n" + 
			"	      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	I i = () -> 10;\n" + 
			"	      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// Bug 398267 - [1.8][compiler] Variables in the body of the lambda expression should be valid
public void test031() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"  public void main(String[] args) {\n" +
			"    I i = () -> {\n" +
			"            		p = 10;\n" +
			"            		Zork z = this.blank;\n" +
			"            		super.foo();\n" +
			"            		goo();\n" +
			"           	};\n" +
			"  }\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	p = 10;\n" + 
			"	^\n" + 
			"p cannot be resolved to a variable\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	Zork z = this.blank;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	Zork z = this.blank;\n" + 
			"	              ^^^^^\n" + 
			"blank cannot be resolved or is not a field\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	super.foo();\n" + 
			"	      ^^^\n" + 
			"The method foo() is undefined for the type Object\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 10)\n" + 
			"	goo();\n" + 
			"	^^^\n" + 
			"The method goo() is undefined for the type X\n" + 
			"----------\n");
}
// Bug 399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test032() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface IA {\r\n" + 
			"  void snazz();\r\n" + 
			"}\r\n" + 
			"interface IB {\r\n" + 
			"  void baz() throws java.io.IOException;\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\n" + 
			"    IA i1 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Error: not declared\n" + 
			"    };\n" + 
			"    IB i2 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Fine: IOException is declared\n" + 
			"    }; // No error, it's all good\n" + 
			"  }\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	throw new java.io.EOFException(); // Error: not declared\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type EOFException\n" + 
			"----------\n");
}
// Bug 399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor
public void test033() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface IA {\r\n" + 
			"  void snazz();\r\n" + 
			"}\r\n" + 
			"interface IB {\r\n" + 
			"  void baz() throws java.io.IOException;\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\n" + 
			"    IA i1 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Error: not declared\n" + 
			"    };\n" + 
			"    IB i2 = () -> {\n" + 
			"      throw new java.io.EOFException(); // Fine: IOException is declared\n" + 
			"    }; // No error, it's all good\n" + 
			"  }\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	throw new java.io.EOFException(); // Error: not declared\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type EOFException\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=398734 - [1.8][compiler] Lambda expression type or return type should be checked against the target functional interface method's result type
public void test034() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  int foo(int x, int y);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    int x = 2;\r\n" + 
			"    I i = (a, b) -> {\r\n" + 
			"      return 42.0 + a + args.length; // Type mismatch: cannot convert from double to int\r\n" + 
			"    };\r\n" + 
			"  }\r\n" + 
			"}"},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	return 42.0 + a + args.length; // Type mismatch: cannot convert from double to int\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from double to int\n" + 
			"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381121,  [] should be accepted in reference expressions.
public void test035() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Object foo(int [] ia);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (int [] ia) -> ia.clone();\n" +
					"	I i2 = int[]::clone;\n" +
					"	Zork z;\n" +
					"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382727,  Lambda expression parameters and locals cannot shadow variables from context
public void test036() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\r\n" + 
			"  void foo(int x, int y);\r\n" + 
			"}\r\n" + 
			"public class X {\r\n" + 
			"  public static void main(String[] args) {\r\n" + 
			"    int x, y;\r\n" + 
			"    I i = (x, y) -> { // Error: x,y being redeclared\r\n" + 
			"      int args = 10; //  Error args is being redeclared\r\n" + 
			"    };\r\n" + 
			"  }\r\n" + 
			"}"}, 
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I i = (x, y) -> { // Error: x,y being redeclared\n" + 
			"	       ^\n" + 
			"Duplicate parameter x\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	I i = (x, y) -> { // Error: x,y being redeclared\n" + 
			"	          ^\n" + 
			"Duplicate parameter y\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	int args = 10; //  Error args is being redeclared\n" + 
			"	    ^^^^\n" + 
			"Duplicate local variable args\n" + 
			"----------\n");
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
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	System.out.println(\"Lambda in illegal context: \" + (() -> \"Illegal Lambda\"));\n" + 
			"	                                                   ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(\"Method Reference in illegal context: \" + System::exit);\n" + 
			"	                                                             ^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	System.out.println(\"Constructor Reference in illegal context: \" + String::new);\n" + 
			"	                                                                  ^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	int x = (x) -> 10;\n" + 
			"	        ^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	X x2 = (x) -> 10;\n" + 
			"	       ^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test038() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.EOFException;\n" +
			"import java.io.IOException;\n" +
			"interface I { void m() throws IOException; }\n" +
			"interface J { void m() throws EOFException; }\n" +
			"interface K { void m() throws ClassNotFoundException; }\n" +
			"interface IJ extends I, J {}\n" +
			"interface IJK extends I, J, K {}\n" +
			"public class X {\n" +
			"	int var;\n" +
			"	IJ ij = () -> {\n" +
			"		if (var == 0) {\n" +
			"			throw new IOException();\n" +
			"		} else if (var == 2) {\n" +
			"			throw new EOFException();\n" +
			"		} else {\n" +
			"			throw new ClassNotFoundException(); \n" +
			"		}\n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	throw new IOException();\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 16)\n" + 
			"	throw new ClassNotFoundException(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type ClassNotFoundException\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test039() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.EOFException;\n" +
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"import java.sql.SQLTransientException;\n" +
			"import java.util.List;\n" +
			"import java.util.concurrent.TimeoutException;\n" +
			"interface A {\n" +
			"  List<String> foo(List<String> arg) throws IOException, SQLTransientException;\n" +
			"}\n" +
			"interface B {\n" +
			"  List foo(List<String> arg) throws EOFException, SQLException, TimeoutException;\n" +
			"}\n" +
			"interface C {\n" +
			"  List foo(List arg) throws Exception;\n" +
			"}\n" +
			"interface D extends A, B {}\n" +
			"interface E extends A, B, C {}\n" +
			"public class X {\n" +
			"	int var;\n" +
			"	D d = (x) -> {\n" +
			"		switch (var) {\n" +
			"		case 0 : throw new EOFException();\n" +
			"		case 1: throw new IOException();\n" +
			"		case 2: throw new SQLException();\n" +
			"		case 3: throw new SQLTransientException();\n" +
			"		case 4: throw new TimeoutException();\n" +
			"		default: throw new NullPointerException();\n" +
			"		}\n" +
			"	};\n" +
			"	E e = (x) -> {\n" +
			"		switch (var) {\n" +
			"		case 0 : throw new EOFException();\n" +
			"		case 1: throw new IOException();\n" +
			"		case 2: throw new SQLException();\n" +
			"		case 3: throw new SQLTransientException();\n" +
			"		case 4: throw new TimeoutException();\n" +
			"		default: throw new NullPointerException();\n" +
			"		}\n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 11)\n" + 
			"	List foo(List<String> arg) throws EOFException, SQLException, TimeoutException;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 14)\n" + 
			"	List foo(List arg) throws Exception;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	List foo(List arg) throws Exception;\n" + 
			"	         ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 23)\n" + 
			"	case 1: throw new IOException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 24)\n" + 
			"	case 2: throw new SQLException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type SQLException\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 26)\n" + 
			"	case 4: throw new TimeoutException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type TimeoutException\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 33)\n" + 
			"	case 1: throw new IOException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 34)\n" + 
			"	case 2: throw new SQLException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type SQLException\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 36)\n" + 
			"	case 4: throw new TimeoutException();\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type TimeoutException\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test040() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"interface I {\n" +
			"  <P extends Exception> Object m() throws P;\n" +
			"}\n" +
			"interface J {\n" +
			"  <Q extends Exception> String m() throws Exception;\n" +
			"}\n" +
			"interface G extends I, J {}\n" +
			"public class X {\n" +
			"	int var;\n" +
			"	G g1 = () -> {\n" +
			"	    throw new Exception(); \n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	G g1 = () -> {\n" + 
			"	    throw new Exception(); \n" + 
			"	};\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type J is generic \n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test041() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"interface G1 {\n" +
			"  <E extends Exception> Object m(E p) throws E;\n" +
			"}\n" +
			"interface G2 {\n" +
			"  <F extends Exception> String m(F q) throws Exception;\n" +
			"}\n" +
			"interface G extends G1, G2 {} // G has descriptor <F extends Exception> ()->String throws F\n" +
			"public class X {\n" +
			"	G g = (x) -> { // Elided type is inferred from descriptor to be F\n" +
			"	    throw x;    // ~== throw new F()\n" +
			"	};\n" +
			"}\n" +
			"class Y implements G {\n" +
			"	public <T extends Exception> String m(T t) throws T {\n" +
			"		throw t;\n" +
			"	}\n" +
			"	void foo(G1 g1) {\n" +
			"			g1.m(new IOException());\n" +
			"	}\n" +
			"	void foo(G2 g2) {\n" +
			"			g2.m(new SQLException());\n" +
			"	}\n" +
			"}\n"

			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	G g = (x) -> { // Elided type is inferred from descriptor to be F\n" + 
			"	    throw x;    // ~== throw new F()\n" + 
			"	};\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type G2 is generic \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 20)\n" + 
			"	g1.m(new IOException());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type IOException\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 23)\n" + 
			"	g2.m(new SQLException());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type Exception\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399537 - [1.8][compiler] Exceptions thrown from lambda body must match specification per function descriptor 
public void test042() {
	this.runNegativeTest(
			new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"interface G1 {\n" +
			"  <E extends Exception> Object m(E p) throws E;\n" +
			"}\n" +
			"interface G2 {\n" +
			"  <F extends Exception> String m(F q) throws Exception;\n" +
			"}\n" +
			"interface G extends G1, G2 {} // G has descriptor <F extends Exception> ()->String throws F\n" +
			"public class X {\n" +
			"	G g1 = (F x) -> {\n" +
			"	    throw x;\n" +
			"	};\n" +
			"	G g2 = (IOException x) -> {\n" +
			"	    throw x;\n" +
			"	};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	G g1 = (F x) -> {\n" + 
			"	    throw x;\n" + 
			"	};\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type G2 is generic \n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	G g2 = (IOException x) -> {\n" + 
			"	    throw x;\n" + 
			"	};\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method m of type G2 is generic \n" + 
			"----------\n");
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
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	interface P extends N, O {}\n" + 
			"	          ^\n" + 
			"Name clash: The method foo(List, Class<?>) of type O has the same erasure as foo(List<String>, Class) of type N but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 20)\n" + 
			"	interface S extends Q, R {}\n" + 
			"	          ^\n" + 
			"The return types are incompatible for the inherited methods Q.foo(), R.foo()\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 23)\n" + 
			"	interface V<P, Q> extends T<P>, U<Q> {}\n" + 
			"	          ^\n" + 
			"Name clash: The method foo(P) of type U<P> has the same erasure as foo(P) of type T<P> but does not override it\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 29)\n" + 
			"	B b              =    () -> {};\n" + 
			"	                      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 32)\n" + 
			"	E e              =    () -> {};\n" + 
			"	                      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 40)\n" + 
			"	M m              =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 43)\n" + 
			"	P p              =    (p0, q0) -> {};\n" + 
			"	                      ^^^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 46)\n" + 
			"	S s              =    () -> {};\n" + 
			"	                      ^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 49)\n" + 
			"	V<?,?> v         =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"10. ERROR in X.java (at line 50)\n" + 
			"	W<?,?> w         =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 51)\n" + 
			"	X x              =    (p0) -> {};\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n",
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
			"import java.util.List;\n" +
			"interface A { <T> T foo(List<T> p); }\n" +
			"interface B { <S> S foo(List<S> p); }\n" +
			"interface C { <T, S> S foo(List<T> p); }\n" +
			"interface D extends A, B {}\n" +
			"interface E extends A, C {}\n" +

			"class Z {\n" +
	        "    A a              =    (p) -> { return null;};\n" +
	        "    B b              =    (p) -> { return null;};\n" +
	        "    C c              =    (p) -> { return null;};\n" +
	        "    D d              =    (p) -> { return null;};\n" +
	        "    E e              =    (p) -> { return null;};\n" +
			"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	interface E extends A, C {}\n" + 
			"	          ^\n" + 
			"Name clash: The method foo(List<T>) of type C has the same erasure as foo(List<T>) of type A but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	A a              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type A is generic \n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	B b              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type B is generic \n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	C c              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type C is generic \n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	D d              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Illegal lambda expression: Method foo of type A is generic \n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 12)\n" + 
			"	E e              =    (p) -> { return null;};\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The target type of this expression must be a functional interface\n" + 
			"----------\n",
			null,
			false,
			options);
}
public static Class testClass() {
	return NegativeLambdaExpressionsTest.class;
}
}