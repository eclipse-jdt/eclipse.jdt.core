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
			"1. ERROR in X.java (at line 10)\n" + 
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
			"1. ERROR in X.java (at line 6)\n" + 
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
			"1. ERROR in X.java (at line 6)\n" + 
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
	// This test checks that common semantic checks are indeed 
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
public static Class testClass() {
	return NegativeLambdaExpressionsTest.class;
}
}