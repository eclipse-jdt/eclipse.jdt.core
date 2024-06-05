/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation.
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
 *								Bug 424205 - [1.8] Cannot infer type for diamond type with lambda on method invocation
 *								Bug 429203 - [1.8][compiler] NPE in AllocationExpression.binding
 *								Bug 456508 - Unexpected RHS PolyTypeBinding for: <code-snippet>
 *								Bug 462083 - [1.8][inference] Java 8 generic return type mismatch with interface involving type parameter.
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsRegressionTest_1_7 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug456508" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public GenericsRegressionTest_1_7(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<String> x = new X<>();
						x.testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"SUCCESS");
}
public void test001a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<String> x = new X<>();
						x.testFunction(1);
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				x.testFunction(1);
				  ^^^^^^^^^^^^
			The method testFunction(String) in the type X<String> is not applicable for the arguments (int)
			----------
			""");
}
public void test001b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						java.util.ArrayList<String> x = new java.util.ArrayList<>();
						x.add("");
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
// fields
public void test001b_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					static java.util.ArrayList<String> x = new java.util.ArrayList<>();
					public static void main(String[] args) {
						X.x.add("");
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
public void test001c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						java.util.ArrayList<String> x = new java.util.ArrayList<>();
						x.add(1);
						System.out.println("SUCCESS");
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				x.add(1);
				  ^^^
			The method add(int, String) in the type ArrayList<String> is not applicable for the arguments (int)
			----------
			""");
}
// fields
public void test001c_1() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					static java.util.ArrayList<String> x = new java.util.ArrayList<>();
					public static void main(String[] args) {
						X.x.add(1);
						System.out.println("SUCCESS");
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				X.x.add(1);
				    ^^^
			The method add(int, String) in the type ArrayList<String> is not applicable for the arguments (int)
			----------
			""");
}
public void test001d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					public class X<T> {\
						public void ab(ArrayList<String> al){
							System.out.println("SUCCESS");
						}
						public static void main(String[] args) {
							X<String> x = new X<>();
							x.ab(new ArrayList<>());
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					x.ab(new ArrayList<>());
					  ^^
				The method ab(ArrayList<String>) in the type X<String> is not applicable for the arguments (ArrayList<Object>)
				----------
				""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						public class X<T> {\
							public void ab(ArrayList<String> al){
								System.out.println("SUCCESS");
							}
							public static void main(String[] args) {
								X<String> x = new X<>();
								x.ab(new ArrayList<>());
							}
						}""",
				},
				"SUCCESS");
	}
}
public void test001e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					public class X<T> {\
						public void ab(ArrayList<T> al){
							System.out.println("SUCCESS");
						}
						public static void main(String[] args) {
							X<String> x = new X<>();
							x.ab(new ArrayList<>());
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					x.ab(new ArrayList<>());
					  ^^
				The method ab(ArrayList<String>) in the type X<String> is not applicable for the arguments (ArrayList<Object>)
				----------
				""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						public class X<T> {\
							public void ab(ArrayList<T> al){
								System.out.println("SUCCESS");
							}
							public static void main(String[] args) {
								X<String> x = new X<>();
								x.ab(new ArrayList<>());
							}
						}""",
				},
				"SUCCESS");
	}
}
public void test001f() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<T>{
						void methodx(T param){
							System.out.println(param);
						}
					}
					public static void main(String[] args) {
						X<String>.X2<String> x = new X<>().new X2<>();
						x.methodx("SUCCESS");
					}
				}""",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	class X2<T>{\n" +
		"	         ^\n" +
		"The type parameter T is hiding the type T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	X<String>.X2<String> x = new X<>().new X2<>();\n" +
		"	                         ^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<String> to X<String>.X2<String>\n"
		:
		"Cannot infer type arguments for X2<>\n"
		) +
		"----------\n");
}
// fields
public void test001f_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<T>{
						void methodx(T param){
							System.out.println(param);
						}
					}
					X<String>.X2<String> x;
					public static void main(String[] args) {
						X test = new X();
						test.x = new X<>().new X2<>();
						test.x.methodx("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
public void test001g() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<K>{
						void methodx(T param, K param2){
							System.out.println(param);
							System.out.println(param2);
						}
					}
					public static void main(String[] args) {
						X<String>.X2<Integer> x = new X<>().new X2<>();
						x.methodx("SUCCESS",1);
					}
				}""",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	X<String>.X2<Integer> x = new X<>().new X2<>();\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<Integer> to X<String>.X2<Integer>\n"
		:
		"Cannot infer type arguments for X2<>\n"
		) +
		"----------\n");
}
public void test001g_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<K>{
						void methodx(T param, K param2){
							System.out.println(param);
							System.out.println(param2);
						}
					}
					X<String>.X2<Integer> x;
					public static void main(String[] args) {
						X test = new X();\
						test.x = new X<>().new X2<>();
						test.x.methodx("SUCCESS",1);
					}
				}""",
		},
		"SUCCESS\n1");
}
public void test001h() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<T>{
						void methodx(T param){
							System.out.println(param);
						}
					}
					public static void main(String[] args) {
						X<String>.X2<String> x = new X<>().new X2<>();
						x.methodx(1);
					}
				}""",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	class X2<T>{\n" +
		"	         ^\n" +
		"The type parameter T is hiding the type T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	X<String>.X2<String> x = new X<>().new X2<>();\n" +
		"	                         ^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<String> to X<String>.X2<String>\n"
		:
		"Cannot infer type arguments for X2<>\n"
		) +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	x.methodx(1);\n" +
		"	  ^^^^^^^\n" +
		"The method methodx(String) in the type X<String>.X2<String> is not applicable for the arguments (int)\n" +
		"----------\n");
}
public void test001h_1() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<T>{
						void methodx(T param){
							System.out.println(param);
						}
					}
					X<String>.X2<String> x;
					public static void main(String[] args) {
						X test = new X();
						test.x = new X<>().new X2<>();
						test.x.methodx(1);
					}
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				class X2<T>{
				         ^
			The type parameter T is hiding the type T
			----------
			2. WARNING in X.java (at line 9)
				X test = new X();
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			3. WARNING in X.java (at line 9)
				X test = new X();
				             ^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			4. WARNING in X.java (at line 10)
				test.x = new X<>().new X2<>();
				     ^
			Type safety: The field x from the raw type X is assigned a value of type X<Object>.X2<Object>. References to generic type X<T> should be parameterized
			----------
			5. WARNING in X.java (at line 11)
				test.x.methodx(1);
				^^^^^^^^^^^^^^^^^
			Type safety: The method methodx(Object) belongs to the raw type X.X2. References to generic type X<T>.X2<T> should be parameterized
			----------
			""");
}
public void test001h_2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<T>{
						void methodx(T param){
							System.out.println(param);
						}
					}
					X<String>.X2<String> x;
					public static void main(String[] args) {
						X test = new X();
						test.x = new X<>().new X2<>();
						test.x.methodx(1);
					}
				}""",
		},
		"1");
}
public void test001i() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<K>{
						class X22<I>{
							void methodx(T param, K param2, I param3){
								System.out.println(param);
							}
						}
					}
					public static void main(String[] args) {
						X<String> test = new X<>();\
						X<String>.X2<Integer>.X22<X<String>> x = new X<>().new X2<>().new X22<>();
						x.methodx("SUCCESS", 1, test);
					}
				}""",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	X<String> test = new X<>();		X<String>.X2<Integer>.X22<X<String>> x = new X<>().new X2<>().new X22<>();\n" +
		"	                           		                                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<Object>.X22<X<String>> to X<String>.X2<Integer>.X22<X<String>>\n"
		:
		"Cannot infer type arguments for X22<>\n"
		) +
		"----------\n");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X x = new X<>();
						x.testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				X x = new X<>();
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			2. WARNING in X.java (at line 4)
				x.testFunction("SUCCESS");
				^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method testFunction(Object) belongs to the raw type X. References to generic type X<T> should be parameterized
			----------
			""");
}
public void test003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						new X<>().testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"SUCCESS");
}

public void test004b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<U> {
					}
					public static void main(String[] args) {
						new X<>().new X2<>(){
							void newMethod(){
							}
						};
					}
				}""",
		},
		this.complianceLevel < ClassFileConstants.JDK9 ?
		"""
			----------
			1. ERROR in X.java (at line 5)
				new X<>().new X2<>(){
				              ^^
			'<>' cannot be used with anonymous classes
			----------
			""":
			"""
				----------
				1. ERROR in X.java (at line 6)
					void newMethod(){
					     ^^^^^^^^^^^
				The method newMethod() of type new X<Object>.X2<Object>(){} must override or implement a supertype method
				----------
				""");
}
public void test004c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<U> {
						U f1;\
						public void setF(U a){
							this.f1 = a;\
							System.out.println(this.f1);
						}
					}
					public static void main(String[] args) {
						new X<>().new X2<Integer>(){
							void newMethod(){
							}
						}.setF(1);
					}
				}""",
		},
		"1");
}

public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X1<T> {
					int abc = 1;
					public void testFunction(T param){
						System.out.println(param + "X1");
					}
				}
				public class X<T> extends X1<T> {
					public static void main(String[] args) {
						X1<String> x = new X<>();
						x.testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"SUCCESS");
}
// shows the difference between using <> and the raw type - different semantics
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	T field1;" +
			"	public X(T param){\n" +
			"		field1 = param;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X.testFunction(new X<>(\"hello\").getField());\n" + // prints 1
			"		X.testFunction(new X(\"hello\").getField());\n" + //prints 2
			"	}\n" +
			"	public static void testFunction(String param){\n" +
			"		System.out.println(1);\n" +
			"	}\n" +
			"	public static void testFunction(Object param){\n" +
			"		System.out.println(2);\n" +
			"	}\n" +
			"	public T getField(){\n" +
			"		return field1;" +
			"	}\n" +
			"}",
		},
		"1\n" +
		"2");
}
public void test007a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						public X(){
						}
						public X(T param){
							System.out.println(param);
						}
						public static void testFunction(X<String> param){
							System.out.println("SUCCESS");
						}
						public static void main(String[] args) {
							X.testFunction(new X<>());
							X.testFunction(new X("hello"));
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					X.testFunction(new X<>());
					  ^^^^^^^^^^^^
				The method testFunction(X<String>) in the type X is not applicable for the arguments (X<Object>)
				----------
				2. WARNING in X.java (at line 12)
					X.testFunction(new X("hello"));
					               ^^^^^^^^^^^^^^
				Type safety: The constructor X(Object) belongs to the raw type X. References to generic type X<T> should be parameterized
				----------
				3. WARNING in X.java (at line 12)
					X.testFunction(new X("hello"));
					               ^^^^^^^^^^^^^^
				Type safety: The expression of type X needs unchecked conversion to conform to X<String>
				----------
				4. WARNING in X.java (at line 12)
					X.testFunction(new X("hello"));
					                   ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X<T> {
							public X(){
							}
							public X(T param){
								System.out.println(param);
							}
							public static void testFunction(X<String> param){
								System.out.println("SUCCESS");
							}
							public static void main(String[] args) {
								X.testFunction(new X<>());
								X.testFunction(new X("hello"));
							}
						}""",
				},
				"""
					SUCCESS
					hello
					SUCCESS""");
	}
}
//shows the difference between using <> and the raw type - different semantics
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	T field1;\n" +
			"	public X(T param){\n" +
			"		field1 = param;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<?> x1 = new X(1).get(\"\");\n" + // ok - passing String where Object is expected
			"		X<?> x2 = new X<>(1).get(\"\");\n" + // bad - passing String where Integer is expected
			"	}\n" +
			"	public X<T> get(T t){\n" +
			"		return this;" +
			"	}\n" +
			"}",
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				X<?> x1 = new X(1).get("");
				          ^^^^^^^^
			Type safety: The constructor X(Object) belongs to the raw type X. References to generic type X<T> should be parameterized
			----------
			2. WARNING in X.java (at line 7)
				X<?> x1 = new X(1).get("");
				          ^^^^^^^^^^^^^^^^
			Type safety: The method get(Object) belongs to the raw type X. References to generic type X<T> should be parameterized
			----------
			3. WARNING in X.java (at line 7)
				X<?> x1 = new X(1).get("");
				              ^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			4. ERROR in X.java (at line 8)
				X<?> x2 = new X<>(1).get("");
				                     ^^^
			The method get(Integer) in the type X<Integer> is not applicable for the arguments (String)
			----------
			""");
}

public void test0014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<J,K> {
					public static void main(String[] args) {
						X<String,Integer> x = new X<>();
						x.testFunction("SUCCESS", 123);
					}
					public void testFunction(J param, K param2){
						System.out.println(param);
						System.out.println(param2);
					}
				}""",
		},
		"SUCCESS\n" +
		"123");
}
public void test0014a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<J,K> {
					public static void main(String[] args) {
						X<String,Integer> x = new X<>();
						x.testFunction(123, "SUCCESS");
					}
					public void testFunction(J param, K param2){
						System.out.println(param);
						System.out.println(param2);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				x.testFunction(123, "SUCCESS");
				  ^^^^^^^^^^^^
			The method testFunction(String, Integer) in the type X<String,Integer> is not applicable for the arguments (int, String)
			----------
			""");
}
public void test0015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X(){
						System.out.println("const.1");
					}
					X (T t) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<String> x = new X<>();
						X<String> x2 = new X<>("");
					}
				}""",
		},
		"const.1\nconst.2");
}
// To verify that <> cannot be used with explicit type arguments to generic constructor.
public void test0016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					<E> X(){
						System.out.println("const.1");
					}
					<K,J> X (Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<String> x = new <String>X<>();
						X<String> x2 = new <String, Integer>X<>(1);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				X<String> x = new <String>X<>();
				                   ^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			2. ERROR in X.java (at line 10)
				X<String> x2 = new <String, Integer>X<>(1);
				                    ^^^^^^^^^^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			""");
}
public void test0016a() {
	this.runConformTest(  // javac fails to compile this, looks buggy
		new String[] {
			"X.java",
			"""
				public class X<T> {
					<E> X(){
						System.out.println("const.1");
					}
					<K,J> X (Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<String> x = new X<>();
						X<String> x2 = new X<>(1);
					}
				}""",
		},
		"const.1\nconst.2");
}
// To verify that <> cannot be used with explicit type arguments to a generic constructor.
public void test0016b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X<String> x;
					X<String> x2;
					<E> X(){
						System.out.println("const.1");
					}
					<K,J> X (Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<Integer> test = new <String>X<>();
						test.x = new <String>X<>();
						test.x2 = new <String, Integer>X<>(1);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				X<Integer> test = new <String>X<>();
				                       ^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			2. ERROR in X.java (at line 12)
				test.x = new <String>X<>();
				              ^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			3. ERROR in X.java (at line 13)
				test.x2 = new <String, Integer>X<>(1);
				               ^^^^^^^^^^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			""");
}
//To verify that a parameterized invocation of a generic constructor works even if <> is used
//to elide class type parameters. This test handles fields
public void test0016c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X<String> x;
					X<String> x2;
					<E> X(){
						System.out.println("const.1");
					}
					<K,J> X (Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<Integer> test = new X<>();
						test.x = new X<>();
						test.x2 = new X<>(1);
					}
				}""",
		},
		"const.1\nconst.1\nconst.2");
}
// To verify that <> cannot be used with explicit type arguments to generic constructor.
public void test0017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X(int i){
						System.out.println("const.1");
					}
					<K,J> X (Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<String> x = new X<>(1);
						X<String> x2 = new <String, Integer>X<>(1);
						Integer i = 1;
						X<String> x3 = new <String, Integer>X<>(i);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				X<String> x2 = new <String, Integer>X<>(1);
				                    ^^^^^^^^^^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			2. ERROR in X.java (at line 12)
				X<String> x3 = new <String, Integer>X<>(i);
				                    ^^^^^^^^^^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			"""
);
}
// To verify that a parameterized invocation of a non-generic constructor works even if <> is used
// to elide class type parameters.
public void test0017a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X(int i){
						System.out.println("const.1");
					}
					<K,J> X (Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X<String> x = new X<>(1);
						X<String> x2 = new X<>(1);
						Integer i = 1;
						X<String> x3 = new X<>(i);
					}
				}""",
		},
		"const.1\nconst.1\nconst.2");
}
// To verify that the correct constructor is found by parameter substitution in the diamond case
public void test0018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X(T t){
						System.out.println("const.1");
					}
					X (T t, Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X x = new X<>("");
						X x2 = new X<>("",1);
					}
				}""",
		},
		"const.1\nconst.2");
}
// To verify that the correct constructor is found by parameter substitution
// in the diamond case -- fields
public void test0018b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X f1;
					X f2;
					X(T t){
						System.out.println("const.1");
					}
					X (T t, Integer i) {
						System.out.println("const.2");
					}
					public static void main(String[] args) {
						X x = new X<>("");
						x.f1 = new X<>("");
						x.f2 = new X<>("",1);
					}
				}""",
		},
		"const.1\nconst.1\nconst.2");
}
public void test0019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   String s = new String<>("junk");
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				String s = new String<>("junk");
				               ^^^^^^
			The type String is not generic; it cannot be parameterized with arguments <>
			----------
			""");
}
// check inference at method argument position.
public void test0020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				    Zork z;
				    public X(T t) {}
					 int f(X<String> p) {return 0;}
					 int x = f(new X<>(""));
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//check inference at method argument position.
public void test0021() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					import java.util.ArrayList;
					class X<T> {
					  public X(T t) {}
					  int f(List<String> p) {return 0;}
					  int x = f(new ArrayList<>());
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					int x = f(new ArrayList<>());
					        ^
				The method f(List<String>) in the type X<T> is not applicable for the arguments (ArrayList<Object>)
				----------
				""");
	} else {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.List;
						import java.util.ArrayList;
						class X<T> {
						  public X(T t) {}
						  int f(List<String> p) {return 0;}
						  int x = f(new ArrayList<>());
						}
						""",
				},
				"");
	}
}
public void test0022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				
				class StringKeyHashMap<V> extends HashMap<String, V>  { \s
				}
				
				class IntegerValueHashMap<K> extends HashMap<K, Integer>  { \s
				}
				
				public class X {
				    Map<String, Integer> m1 = new StringKeyHashMap<>();
				    Map<String, Integer> m2 = new IntegerValueHashMap<>();
				}
				"""
		},
		"");
}
public void test0023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				
				class StringKeyHashMap<V> extends HashMap<String, V>  { \s
				}
				
				class IntegerValueHashMap<K> extends HashMap<K, Integer>  { \s
				}
				
				public class X {
				    Map<String, Integer> m1 = new StringKeyHashMap<>(10);
				    Map<String, Integer> m2 = new IntegerValueHashMap<>();
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				class StringKeyHashMap<V> extends HashMap<String, V>  { \s
				      ^^^^^^^^^^^^^^^^
			The serializable class StringKeyHashMap does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 7)
				class IntegerValueHashMap<K> extends HashMap<K, Integer>  { \s
				      ^^^^^^^^^^^^^^^^^^^
			The serializable class IntegerValueHashMap does not declare a static final serialVersionUID field of type long
			----------
			3. ERROR in X.java (at line 11)
				Map<String, Integer> m1 = new StringKeyHashMap<>(10);
				                          ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for StringKeyHashMap<>
			----------
			""");
}
// check inference at return expression.
public void test0024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				class X<T> {
				  public X() {}
				  X<String> f(List<String> p) {return new X<>();}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			""");
}
// check inference at cast expression.
public void test0025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				class X<T> {
				  public X() {}
				  void f(List<String> p) { Object o = (X<String>) new X<>();}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				void f(List<String> p) { Object o = (X<String>) new X<>();}
				                                    ^^^^^^^^^^^^^^^^^^^^^
			Cannot cast from X<Object> to X<String>
			----------
			""");
}
// Test various scenarios.
public void test0026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				public class X<T> {
					X(T t) {}
				   X(String s) {}
				   X(List<?> l) {}
				   X<T> idem() { return this; }
				   X<Number> x = new X<>(1);
				   X<Integer> x2 = new X<>(1);
				   List<?> list = new ArrayList<>();
				   X<?> x3 = new X<>(1);
				   X<Object> x4 = new X<>(1).idem();
				   X<Object> x5 = new X<>(1);
				   int m(X<String> xs) { return 0; }
				   int i = m(new X<>(""));
				   X<?> x6 = new X<>(list);
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in X.java (at line 8)
				X<Number> x = new X<>(1);
				              ^^^^^^^^^^
			Type mismatch: cannot convert from X<Integer> to X<Number>
			----------
			2. ERROR in X.java (at line 12)
				X<Object> x4 = new X<>(1).idem();
				               ^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from X<Integer> to X<Object>
			----------
			3. ERROR in X.java (at line 13)
				X<Object> x5 = new X<>(1);
				               ^^^^^^^^^^
			Type mismatch: cannot convert from X<Integer> to X<Object>
			----------
			4. ERROR in X.java (at line 15)
				int i = m(new X<>(""));
				        ^
			The method m(X<String>) in the type X<T> is not applicable for the arguments (X<Object>)
			----------
			""" :
			"""
				----------
				1. ERROR in X.java (at line 12)
					X<Object> x4 = new X<>(1).idem();
					               ^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from X<Integer> to X<Object>
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=344655
public void test0027() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				       class Y<U> {
					    <K,J> Y (Integer i) {
					    }
					}
				
					<K,J> X (Integer i) {
					}
				
					public static void main(String[] args) {
						X<String> x = new <String, Integer> X<>(1);
						X<String> x2 = x.new <String, Integer> Y<>(1);
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				X<String> x = new <String, Integer> X<>(1);
				                   ^^^^^^^^^^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			2. ERROR in X.java (at line 12)
				X<String> x2 = x.new <String, Integer> Y<>(1);
				                      ^^^^^^^^^^^^^^^
			Explicit type arguments cannot be used with '<>' in an allocation expression
			----------
			"""
);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345239
public void test0028() {
	String[] testFiles = new String[] {
			"X.java",
			"""
				public class X<T> {
				     X<String> x = new X<> () {}
				;\
				     class Y<U> {
					  }
				     X<String>.Y<String> y = x.new Y<>() {};
				}
				"""
		};
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		this.runNegativeTest(
			testFiles,
			"""
				----------
				1. ERROR in X.java (at line 2)
					X<String> x = new X<> () {}
					                  ^
				'<>' cannot be used with anonymous classes
				----------
				2. ERROR in X.java (at line 5)
					X<String>.Y<String> y = x.new Y<>() {};
					                              ^
				'<>' cannot be used with anonymous classes
				----------
				""");
	} else {
		this.runConformTest(testFiles);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345359
public void test0029() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X<String> f2 = new X<>(new Y());\s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<String> f2 = new X<>(new Y());\s
				                           ^
			Y cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345359
public void test0029a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    class I<T> {
				        I(T t) {}
				    }
				    X.I<String> f = new X().new I<>(new Y());\s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				X.I<String> f = new X().new I<>(new Y());\s
				                                    ^
			Y cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class C {}
				interface I {}
				public class X<T extends C & I> {
				    X() {}
				    X f = new X<>();
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				X f = new X<>();
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class C {}
				interface I {}
				public class X<T extends C & I> {
				    X() {}
				    X<?> f = new X<>();
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class C {}
				interface I {}
				public class X<T extends C & I> {
				    static <U extends C & I> X<U> getX() {
				        return null;
				    }
				    X<?> f2 = getX();
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class C {}
				interface I {}
				public class X<T extends C & I> {
				    static <U extends C & I> X<U> getX() {
				        return null;
				    }
				    X f2 = getX();
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				X f2 = getX();
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
				    void foo(T a) {
					 System.out.println(a);
					 }
					 class Y<K>{
						Y(T t,K k) {}
					 }
				    public static void main(String[] args) {
						X<Integer> x1 = new X<>(1,1);
						X<Integer> x2 = new X<>(1);
						X<Integer> x3 = new X<>();
						X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();
						X<Integer>.Y<String> y2 = new X<>(1,1).new Y<>(1);
						X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);
						X<Integer>.Y<String> y4 = new X<>(1).new Y<>("","");
						X<Integer>.Y<String> y5 = new X<>(1).new Y<>(1,"");
						X<Integer>.Y<String> y6 = new X<>().new Y<>(1,"");
						X<Integer>.Y<String> y7 = new X<>().new Y<>(1,1);
					 }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(1,1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 16)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 17)\n" +
		"	X<Integer>.Y<String> y4 = new X<>(1).new Y<>(\"\",\"\");\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 19)\n" +
		"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\");\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<String> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n" +
		"7. ERROR in X.java (at line 20)\n" +
		"	X<Integer>.Y<String> y7 = new X<>().new Y<>(1,1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0034b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
				    void foo(T a) {
					 System.out.println(a);
					 }
					 class Y<K>{
						Y(T t,K k) {}
						Y(K k) {}
					 }
				    public static void main(String[] args) {
						X<String>.Y<String> y42 = new X<>("").new Y<>("");
						X<String>.Y<String> y41 = new X<>("").new Y<>("","");
						X<Integer>.Y<String> y4 = new X<>(1).new Y<>("","");
					 }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				X<Integer>.Y<String> y4 = new X<>(1).new Y<>("","");
				                          ^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for Y<>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
					 @SafeVarargs
				    X(String abc, String abc2, T... t) {}
				    void foo(T a) {
					 System.out.println(a);
					 }
					 class Y<K>{
						@SafeVarargs
						Y(T t,String abc, K... k) {}
					 }
				    public static void main(String[] args) {
						X<Integer> x1 = new X<>(1,1);
						X<Integer> x2 = new X<>(1);
						X<Integer> x3 = new X<>();
						X<Integer> x4 = new X<>("","");
						X<Integer> x5 = new X<>("","","");
						X<Integer> x6 = new X<>("","",1);
						X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();
						X<Integer>.Y<String> y2 = new X<>("",1).new Y<>("");
						X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);
						X<Integer>.Y<String> y4 = new X<>(1).new Y<>(1,"");
						X<Integer>.Y<String> y5 = new X<>(1).new Y<>(1,"","");
						X<Integer>.Y<String> y6 = new X<>().new Y<>(1,"",1);
						X<Integer>.Y<String> y7 = new X<>().new Y<>("","",1);
					 }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n" +
		"3. ERROR in X.java (at line 20)\n" +
		"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 21)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(\"\",1).new Y<>(\"\");\n" +
		"	                          ^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 22)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 25)\n" +
		"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n" +
		"7. ERROR in X.java (at line 26)\n" +
		"	X<Integer>.Y<String> y7 = new X<>().new Y<>(\"\",\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
					 @SafeVarargs
				    X(String abc, String abc2, T... t) {}
				    void foo(T a) {
					 System.out.println(a);
					 }
					 class Y<K>{
						@SafeVarargs
						Y(T t,String abc, K... k) {}
					 }
					X<Integer> x1 = new X<>(1,1);
					X<Integer> x2 = new X<>(1);
					X<Integer> x3 = new X<>();
					X<Integer> x4 = new X<>("","");
					X<Integer> x5 = new X<>("","","");
					X<Integer> x6 = new X<>("","",1);
					X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();
					X<Integer>.Y<String> y2 = new X<>("",1).new Y<>("");
					X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);
					X<Integer>.Y<String> y4 = new X<>(1).new Y<>(1,"");
					X<Integer>.Y<String> y5 = new X<>(1).new Y<>(1,"","");
					X<Integer>.Y<String> y6 = new X<>().new Y<>(1,"",1);
					X<Integer>.Y<String> y7 = new X<>().new Y<>("","",1);
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n" +
		"3. ERROR in X.java (at line 19)\n" +
		"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 20)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(\"\",1).new Y<>(\"\");\n" +
		"	                          ^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 21)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 24)\n" +
		"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n" +
		"7. ERROR in X.java (at line 25)\n" +
		"	X<Integer>.Y<String> y7 = new X<>().new Y<>(\"\",\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0034a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
				    void foo(T a) {
					 System.out.println(a);
					 }
				    public static void main(String[] args) {
						X<Integer> x1 = new X<>(1,1);
						X<Integer> x2 = new X<>(1);
						X<Integer> x3 = new X<>();
					 }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				X<Integer> x1 = new X<>(1,1);
				                ^^^^^^^^^^^^
			Cannot infer type arguments for X<>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0035a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
					 @SafeVarargs
				    X(String abc, String abc2, T... t) {}
				    void foo(T a) {
					 	System.out.println(a);
					 }
				    public static void main(String[] args) {
						X<Integer> x1 = new X<>(1,1);
						X<Integer> x2 = new X<>(1);
						X<Integer> x3 = new X<>();
						X<Integer> x4 = new X<>("","");
						X<Integer> x5 = new X<>("","","");
						X<Integer> x6 = new X<>("","",1);
					 }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0036a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
					 @SafeVarargs
				    X(String abc, String abc2, T... t) {}
				    void foo(T a) {
					 System.out.println(a);
					 }
					X<Integer> x1 = new X<>(1,1);
					X<Integer> x2 = new X<>(1);
					X<Integer> x3 = new X<>();
					X<Integer> x4 = new X<>("","");
					X<Integer> x5 = new X<>("","","");
					X<Integer> x6 = new X<>("","",1);
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    X(T t) {}
				    X() {}
					 @SafeVarargs
				    X(String abc, String abc2, T... t) {}
				    void foo(T a) {
					 System.out.println(a);
					 }
					 class Y<K>{
						@SafeVarargs
						Y(T t,String abc, K... k) {}
					 }
				    public static void main(String[] args) {
						X<Integer>.Y<String> y1 = new X<>().new Y<>(1);
						X<Integer>.Y<String> y2 = new X<>(1).new Y<>(1);
						X<Integer>.Y<String> y3 = new X<>("","",1).new Y<>(1);
						X<Integer>.Y<String> y4 = new X<>(1,"").new Y<>(1,"");
					 }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				X<Integer>.Y<String> y1 = new X<>().new Y<>(1);
				                          ^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for Y<>
			----------
			2. ERROR in X.java (at line 15)
				X<Integer>.Y<String> y2 = new X<>(1).new Y<>(1);
				                          ^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for Y<>
			----------
			3. ERROR in X.java (at line 16)
				X<Integer>.Y<String> y3 = new X<>("","",1).new Y<>(1);
				                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for Y<>
			----------
			4. ERROR in X.java (at line 17)
				X<Integer>.Y<String> y4 = new X<>(1,"").new Y<>(1,"");
				                          ^^^^^^^^^^^^^
			Cannot infer type arguments for X<>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=341795
public void test0038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface A {
				    <T extends B & C> T getaMethod();
				    <T extends B & C> void setaMethod(T param);
				}
				class B {
				}
				interface C {
				}
				public class X {
				    public void someMethod(A aInstance) {
				        aInstance.getaMethod();
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319436
public void test0039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X {
				  public static void main(String[] args) {
				    createObject();
				  }
				  private static <T extends Comparable<?> & Serializable> T createObject() {
				    return null;
				  }
				}
				"""
		},
		"");
}
public void test0042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<T> {}
				public class X {
				    <T extends I<T>> void m() { }
				    { m(); }\s
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    class Y<Z>  {
				        Y(T a, Z b) {
				        }
				    }
				    public static void main(String[] args) {
				        X<String>.Y<String>  x1 = new X<String>().new Y<String>("","");
				        X<String>.Y<String>  x2 = new X<String>().new Y<>("","");
				        System.out.println("SUCCESS");
				    }
				}
				"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				    class Y<Z> {
				         Y(T a, Z b) {
				         }
				    }
				    public static void main(String[] args) {
				        X<String>.Y<String> x = new X<>().new Y<>("","");
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	X<String>.Y<String> x = new X<>().new Y<>(\"\",\"\");\n" +
		"	                        ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<String> to X<String>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    class Y<T, Z> {
				         Y(T a, Z b) {
				         }
				    }
				    public static void main(String[] args) {
				        X.Y<String, String> x = new X().new Y<>("","");
				        System.out.println("SUCCESS");
				    }
				}
				"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				    class Y<Z> {
				         Y(T a, Z b) {\s
				         }
				    }
				    public static void main(String[] args) {
				        X<String>.Y<String> x = new X<String>().new Y<>("","");
				        System.out.println("SUCCESS");
				    }
				}
				"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				    class Y<Z> {
				         Y(T a, Z b) {
				         }
				    }
				    public static void main(String[] args) {
				        X<String>.Y<String> x1 = new X<String>().new Y<String>("","");\s
				        X<String>.Y<String> x2 = new X<String>().new Y<>("",""); // javac wrong error\s
				        System.out.println("SUCCESS");
				    }
				}
				"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				    <T> X(T t) {
				    }
				    X<String> x = new X<>("");\s
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				<T> X(T t) {
				 ^
			The type parameter T is hiding the type T
			----------
			2. ERROR in X.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0049() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				     class Y<Z> {
				          Y(T a, Z b) {
				          }
				     }
				   public static void main(String[] args) {
				       X<Object>.Y<String> x1 = new X<Object>().new Y<String>(new Object(),"");
				       X<Object>.Y<String> x2 = new X<>().new Y<String>(new Object(),"");
				       X<Object>.Y<String> x3 = new X<Object>().new Y<>(new Object(),"");
				       X<Object>.Y<String> x4 = new X<>().new Y<>(new Object(),"");
				     }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T extends Comparable<T>> {
				     class Y<Z> {
				          Y(T a, Z b) {
				          }
				     }
				   public static void main(String[] args) {
				       X<String>.Y<String> x1 = new X<String>().new Y<>("","");
				     }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T extends Comparable<T>> {
				     class Y<Z> {
				          Y(Integer a, Z b) {
				          }
				          Y(T a, Z b) {
				          }
				     }
				   public static void main(String[] args) {
				       X<String>.Y<String> x1 = new X<String>().new Y<>("","");
				     }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<E> {
				    X(E e) {}
				    X() {}
				    public static void main(String[] args) {
				        X<Number> x = new X<Number>(1);
				        X<String> x2 = new X<String>("SUCCESS");
				        X<Integer> x3 = new X<Integer>(1);
				        X<AX> x4 = new X<AX>(new AX());
						 X<? extends AX> x5 = new X<AX<String>>(new AX<String>());
						 X<?> x6 = new X<AX<String>>(new AX<String>());
						 X<Class<? extends Object>> x7 = new X<Class<? extends Object>>();
					}
				}
				class AX<T>{}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in X.java (at line 6)
					X<String> x2 = new X<String>("SUCCESS");
					                   ^
				Redundant specification of type arguments <String>
				----------
				2. ERROR in X.java (at line 7)
					X<Integer> x3 = new X<Integer>(1);
					                    ^
				Redundant specification of type arguments <Integer>
				----------
				3. ERROR in X.java (at line 8)
					X<AX> x4 = new X<AX>(new AX());
					               ^
				Redundant specification of type arguments <AX>
				----------
				4. ERROR in X.java (at line 9)
					X<? extends AX> x5 = new X<AX<String>>(new AX<String>());
					                         ^
				Redundant specification of type arguments <AX<String>>
				----------
				5. ERROR in X.java (at line 9)
					X<? extends AX> x5 = new X<AX<String>>(new AX<String>());
					                                           ^^
				Redundant specification of type arguments <String>
				----------
				6. ERROR in X.java (at line 10)
					X<?> x6 = new X<AX<String>>(new AX<String>());
					              ^
				Redundant specification of type arguments <AX<String>>
				----------
				7. ERROR in X.java (at line 10)
					X<?> x6 = new X<AX<String>>(new AX<String>());
					                                ^^
				Redundant specification of type arguments <String>
				----------
				8. ERROR in X.java (at line 11)
					X<Class<? extends Object>> x7 = new X<Class<? extends Object>>();
					                                    ^
				Redundant specification of type arguments <Class<? extends Object>>
				----------
				"""
		: """
			----------
			1. ERROR in X.java (at line 5)
				X<Number> x = new X<Number>(1);
				                  ^
			Redundant specification of type arguments <Number>
			----------
			2. ERROR in X.java (at line 6)
				X<String> x2 = new X<String>("SUCCESS");
				                   ^
			Redundant specification of type arguments <String>
			----------
			3. ERROR in X.java (at line 7)
				X<Integer> x3 = new X<Integer>(1);
				                    ^
			Redundant specification of type arguments <Integer>
			----------
			4. ERROR in X.java (at line 8)
				X<AX> x4 = new X<AX>(new AX());
				               ^
			Redundant specification of type arguments <AX>
			----------
			5. ERROR in X.java (at line 9)
				X<? extends AX> x5 = new X<AX<String>>(new AX<String>());
				                         ^
			Redundant specification of type arguments <AX<String>>
			----------
			6. ERROR in X.java (at line 9)
				X<? extends AX> x5 = new X<AX<String>>(new AX<String>());
				                                           ^^
			Redundant specification of type arguments <String>
			----------
			7. ERROR in X.java (at line 10)
				X<?> x6 = new X<AX<String>>(new AX<String>());
				              ^
			Redundant specification of type arguments <AX<String>>
			----------
			8. ERROR in X.java (at line 10)
				X<?> x6 = new X<AX<String>>(new AX<String>());
				                                ^^
			Redundant specification of type arguments <String>
			----------
			9. ERROR in X.java (at line 11)
				X<Class<? extends Object>> x7 = new X<Class<? extends Object>>();
				                                    ^
			Redundant specification of type arguments <Class<? extends Object>>
			----------
			"""
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<E> {
					 E eField;
					 E get() { return this.eField; }
				    X(E e) {}
				    X(int e, String e2) {}
				    public static void main(String[] args) {
				        X<Number> x = new X<Number>(1);
				        X<String> x2 = new X<String>("SUCCESS");
				        X<String> x22 = new X<String>(1,"SUCCESS");
				        X<Integer> x3 = new X<Integer>(1);
				        String s = foo(new X<String>("aaa"));
				        String s2 = foo(new X<String>(1,"aaa"));
					}
				    static String foo(X<String> x) {
						return x.get();
				    }
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in X.java (at line 8)
					X<String> x2 = new X<String>("SUCCESS");
					                   ^
				Redundant specification of type arguments <String>
				----------
				2. ERROR in X.java (at line 9)
					X<String> x22 = new X<String>(1,"SUCCESS");
					                    ^
				Redundant specification of type arguments <String>
				----------
				3. ERROR in X.java (at line 10)
					X<Integer> x3 = new X<Integer>(1);
					                    ^
				Redundant specification of type arguments <Integer>
				----------
				4. ERROR in X.java (at line 11)
					String s = foo(new X<String>("aaa"));
					                   ^
				Redundant specification of type arguments <String>
				----------
				5. ERROR in X.java (at line 12)
					String s2 = foo(new X<String>(1,"aaa"));
					                    ^
				Redundant specification of type arguments <String>
				----------
				"""
		: """
			----------
			1. ERROR in X.java (at line 7)
				X<Number> x = new X<Number>(1);
				                  ^
			Redundant specification of type arguments <Number>
			----------
			2. ERROR in X.java (at line 8)
				X<String> x2 = new X<String>("SUCCESS");
				                   ^
			Redundant specification of type arguments <String>
			----------
			3. ERROR in X.java (at line 9)
				X<String> x22 = new X<String>(1,"SUCCESS");
				                    ^
			Redundant specification of type arguments <String>
			----------
			4. ERROR in X.java (at line 10)
				X<Integer> x3 = new X<Integer>(1);
				                    ^
			Redundant specification of type arguments <Integer>
			----------
			5. ERROR in X.java (at line 11)
				String s = foo(new X<String>("aaa"));
				                   ^
			Redundant specification of type arguments <String>
			----------
			6. ERROR in X.java (at line 12)
				String s2 = foo(new X<String>(1,"aaa"));
				                    ^
			Redundant specification of type arguments <String>
			----------
			"""
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<E> {
					X(String abc, String def) {}
					void foo() {
						X<Integer> x = new X<Integer>("","");
						foo3(new X<Integer>("",""));
					}
					X<Integer> foo2() {
						return new X<Integer>("","");
					}
					void foo3(X<Integer> x) {}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				X<Integer> x = new X<Integer>("","");
				                   ^
			Redundant specification of type arguments <Integer>
			----------
			2. ERROR in X.java (at line 5)
				foo3(new X<Integer>("",""));
				         ^
			Redundant specification of type arguments <Integer>
			----------
			3. ERROR in X.java (at line 8)
				return new X<Integer>("","");
				           ^
			Redundant specification of type arguments <Integer>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<E> {
				    X(E e) {}
				    X() {}
				    public static void main(String[] args) {
				        X<Number> x = new X<Number>(1);
					}
				}
				class AX<T>{}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"" :
		"""
			----------
			1. ERROR in X.java (at line 5)
				X<Number> x = new X<Number>(1);
				                  ^
			Redundant specification of type arguments <Number>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0053() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Z.java",
			"""
				public class Z <T extends ZB> {\s
				    public static void main(String[] args) {
				        foo(new Z<ZB>());
				    }
				    static void foo(Z<ZB> z) {
				    }
				}
				class ZB {
				}"""
		},
		"""
			----------
			1. ERROR in Z.java (at line 3)
				foo(new Z<ZB>());
				        ^
			Redundant specification of type arguments <ZB>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0054() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y<V> {
				  public static <W extends ABC> Y<W> make(Class<W> clazz) {
				    return new Y<W>();
				  }
				}
				class ABC{}
				"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 3)
				return new Y<W>();
				           ^
			Redundant specification of type arguments <W>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0055() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<A> {\n" +
			"  class Inner<B> { }\n" +
			"  static class Inner2<C> { }\n" +
			"\n" +
			"  void method() {\n" +
			"    X<String>.Inner<Integer> a= new X<String>().new Inner<Integer>();\n" +
			"    X<String>.Inner<Integer> a1= new X<String>().new Inner<>();\n" +	// do not warn. Removing String from X<String> not possible
			"    Inner<Integer> b= new X<A>().new Inner<Integer>();\n" +
			"    Inner<Integer> c= new Inner<Integer>();\n" +
			"    X<A>.Inner<Integer> e= new X<A>().new Inner<Integer>();\n" +
			"    X<A>.Inner<Integer> f= new Inner<Integer>();\n" +
			"    X.Inner2<Integer> d3 = new X.Inner2<Integer>();\n" +
			"  }\n" +
			"}\n",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				X<String>.Inner<Integer> a= new X<String>().new Inner<Integer>();
				                                                ^^^^^
			Redundant specification of type arguments <Integer>
			----------
			2. ERROR in X.java (at line 8)
				Inner<Integer> b= new X<A>().new Inner<Integer>();
				                                 ^^^^^
			Redundant specification of type arguments <Integer>
			----------
			3. ERROR in X.java (at line 9)
				Inner<Integer> c= new Inner<Integer>();
				                      ^^^^^
			Redundant specification of type arguments <Integer>
			----------
			4. ERROR in X.java (at line 10)
				X<A>.Inner<Integer> e= new X<A>().new Inner<Integer>();
				                                      ^^^^^
			Redundant specification of type arguments <Integer>
			----------
			5. ERROR in X.java (at line 11)
				X<A>.Inner<Integer> f= new Inner<Integer>();
				                           ^^^^^
			Redundant specification of type arguments <Integer>
			----------
			6. ERROR in X.java (at line 12)
				X.Inner2<Integer> d3 = new X.Inner2<Integer>();
				                             ^^^^^^
			Redundant specification of type arguments <Integer>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
// qualified allocation
public void test0056() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X <T> {
					void foo1() {
						X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();
					}
					void foo2() {
						X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();
					}
					class Item <E> {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();
				                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from X<Exception>.Item<Thread> to X<String>.Item<Thread>
			----------
			2. ERROR in X.java (at line 3)
				X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();
				                                                  ^^^^
			Redundant specification of type arguments <Thread>
			----------
			3. ERROR in X.java (at line 6)
				X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();
				                                  ^^^^^^^^^^^^^^^^^
			Cannot allocate the member type X<Exception>.Item<Thread> using a parameterized compound name; use its simple name and an enclosing instance of type X<Exception>
			----------
			4. ERROR in X.java (at line 6)
				X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();
				                                               ^^^^
			Redundant specification of type arguments <Thread>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
// qualified allocation
public void test0056b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X <T> {
					static class X1<Z> {
						X1(Z z){}
					}
					X1<Integer> x1 = new X.X1<Integer>(1);
					X1<Number> x2 = new X.X1<Number>(1);
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in X.java (at line 5)
					X1<Integer> x1 = new X.X1<Integer>(1);
					                       ^^
				Redundant specification of type arguments <Integer>
				----------
				"""
		: """
			----------
			1. ERROR in X.java (at line 5)
				X1<Integer> x1 = new X.X1<Integer>(1);
				                       ^^
			Redundant specification of type arguments <Integer>
			----------
			2. ERROR in X.java (at line 6)
				X1<Number> x2 = new X.X1<Number>(1);
				                      ^^
			Redundant specification of type arguments <Number>
			----------
			"""
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
// qualified allocation
public void test0056c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X <T> {
					X(T t){}
					class X1<Z> {
						X1(Z z){}
					}
					X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in X.java (at line 6)
					X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);
					                               ^
				Redundant specification of type arguments <Integer>
				----------
				"""
		: """
			----------
			1. ERROR in X.java (at line 6)
				X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);
				                               ^
			Redundant specification of type arguments <Integer>
			----------
			2. ERROR in X.java (at line 6)
				X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);
				                                                 ^^
			Redundant specification of type arguments <Number>
			----------
			"""
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0057() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void test() {
						Pair<Double, Integer> p = new InvertedPair<Integer, Double>();
					}
				}
				class Pair<A, B> {
				}
				class InvertedPair<A, B> extends Pair<B, A> {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Pair<Double, Integer> p = new InvertedPair<Integer, Double>();
				                              ^^^^^^^^^^^^
			Redundant specification of type arguments <Integer, Double>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0058() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
				    public void test(boolean param) {
				        ArrayList<?> ls = (param)\s
				        		? new ArrayList<String>()
				        		: new ArrayList<Object>();
				        	\t
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				: new ArrayList<Object>();
				      ^^^^^^^^^
			Redundant specification of type arguments <Object>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0059() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class X<T> {
					 X(List<? extends T> p) {}
				    Object x = new X<CharSequence>((ArrayList<String>) null);
				}
				"""
		},
		"",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=351965
public void test0060() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class X {
					 public void foo() {
				    	new ArrayList<>();
					 }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				new ArrayList<>();
				    ^^^^^^^^^
			'<>' operator is not allowed for source level below 1.7
			----------
			2. ERROR in X.java (at line 6)
				new ArrayList<>();
				    ^^^^^^^^^
			Incorrect number of arguments for type ArrayList<E>; it cannot be parameterized with arguments <>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=351965
public void test0060a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				
				public class X {
					 public void foo() {
				    	new java.util.ArrayList<>();
					 }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				new java.util.ArrayList<>();
				    ^^^^^^^^^^^^^^^^^^^
			'<>' operator is not allowed for source level below 1.7
			----------
			2. ERROR in X.java (at line 4)
				new java.util.ArrayList<>();
				    ^^^^^^^^^^^^^^^^^^^
			Incorrect number of arguments for type ArrayList<E>; it cannot be parameterized with arguments <>
			----------
			""",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361441
public void test0061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.net.URI;\
				import java.nio.file.FileSystems;\
				import java.util.Collections;
				public class X {
					 public static void foo() {
				    	URI uri = URI.create("http://www.eclipse.org");
						FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());
					 }
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in X.java (at line 5)
					FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());
					                            ^^^^^^^^^^^^^
				The method newFileSystem(URI, Map<String,?>) in the type FileSystems is not applicable for the arguments (URI, Map<Object,Object>)
				----------
				"""
		: """
			----------
			1. ERROR in X.java (at line 5)
				FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			2. WARNING in X.java (at line 5)
				FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());
				             ^^^^^^^^^^^^^^
			Unused type arguments for the non generic method newFileSystem(URI, Map<String,?>) of type FileSystems; it should not be parameterized with arguments <String, Object>
			----------
			"""
		));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428220, [1.8][compiler] Javadoc processing interferes with type inference.
public void test428220() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class HashMap<K, V> {
					static class Node<K, V> {
						Node(int hash, K key, V value, Node<K, V> next) {}
					}
					/** @see #put(Object, Object) */
					public V put(K key, V value) {	return null; }
				
					Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
						return new Node<>(hash, key, value, next); // Error
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class HashMap<K, V> {
				             ^^^^^^^
			The public type HashMap must be defined in its own file
			----------
			""", null, true, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428220, [1.8][compiler] Javadoc processing interferes with type inference.
public void test428220a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class HashMap<K, V> {
					static class Node<K, V> {
						Node(int hash, K key, V value, Node<K, V> next) {}
					}
					/** @see #put(Object, Object) */
					public V put(K key, V value) {	return null; }
				
					Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
						return new Node<>(hash, key, value, next); // Error
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class HashMap<K, V> {
				             ^^^^^^^
			The public type HashMap must be defined in its own file
			----------
			""", null, true, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442929, [1.8][compiler] ClassCastException during runtime where is no cast
public void test442929() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						shouldNotThrow();
					}
					static void shouldNotThrow() {
						final String[] array = { "" };
						final String[] expected = { "" };
						// throws
				       try {
						    assertThat(op(array, "")).isEqualTo(expected);
				       } catch (ClassCastException c) {
				           System.out.println("Expected CCE");
				       }
					}
					static <T> T[] op(T[] array, T element) {
						return asArray(element);
					}
					@SafeVarargs
					static <T> T[] asArray(T... elements) {
						return elements;
					}
					static <T> ObjectArrayAssert<T> assertThat(T actual) {
						return new ObjectArrayAssert<>(actual);
					}
					static class ObjectArrayAssert<T> {
						ObjectArrayAssert(T actual) {
						}
						void isEqualTo(T expected) {
						}
					}
				}
				""",
		},
		"Expected CCE");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448028, [1.8] 1.8 cannot infer type arguments where 1.7 does
public void test448028() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		   new String[] {
			   "X.java",
			   """
				public class X {
				
				  public static interface I {/*empty*/}
				
				  public static class C
				    implements I {/*empty*/}
				
				  public static class W<T extends I>
				    implements I {
				
				    // --- problem is triggered only, when there is a vararg-parameter
				    public W(final T t, final Object... o) {
				      super();
				    }
				  }
				
				  // --- needed to trigger problem
				  public static final <T> T inspect(final T t) {
				    return t;
				  }
				
				  // --- this compiles ok when having JDK Compilance set to 1.7 !
				  public static final W<C> err1() {
				    final C c = new C();
				    final Object o = new Object();
				    return inspect(new W<>(c, o)); // - ERROR: Cannot infer type arguments for W<> F.java
				  }
				
				  public static final W<C> wrn1() {
				    final C c = new C();
				    final Object o = new Object();
				    // --- giving the type-parameter yields a warning
				    // --- comparing that to the error of method err1() it does not make much sense
				    return inspect(new W<C>(c, o)); // - WARNING: Redundant specification of type arguments <F.C> F.java
				  }
				
				  public static final W<C> ok1() {
				    final C c = new C();
				    // --- no extra vararg-paramaeter
				    return inspect(new W<>(c)); // - OK
				  }
				
				  public static final W<C> ok2() {
				    final C c = new C();
				    final Object o = new Object();
				    // --- no check-method
				    return new W<>(c, o); // - OK
				  }
				
				  public static final W<C> ok3() {
				    final C c = new C();
				    // --- no check-method
				    return new W<>(c); // - OK
				  }
				
				  public static final W<C> ok4() {
				    final C c = new C();
				    final Object o = new Object();
				    // --- this also compiles (my solution for now)
				    final W<C> w = new W<>(c, o);
				    return inspect(w);
				  }
				}
				""",
		   },
		   """
			----------
			1. ERROR in X.java (at line 34)
				return inspect(new W<C>(c, o)); // - WARNING: Redundant specification of type arguments <F.C> F.java
				                   ^
			Redundant specification of type arguments <X.C>
			----------
			""",
			null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449619,  [1.8][compiler] Qualified <> allocation fails to compile.
public void test449619() {
	String source = """
		public class X {
			public class Y<T> {
			}
			static void foo(Y<String> ys) {
			}
			public static void main(String[] args) {
				foo(new X().new Y<>());
			}
		}
		""";
	if (this.complianceLevel >= ClassFileConstants.JDK1_8)
		this.runConformTest(
		   new String[] {
			   "X.java",
			   source,
		   },
		   "");
	else
		this.runNegativeTest(
		   new String[] {
			   "X.java",
			   source,
		   },
		   """
			----------
			1. ERROR in X.java (at line 7)
				foo(new X().new Y<>());
				^^^
			The method foo(X.Y<String>) in the type X is not applicable for the arguments (X.Y<Object>)
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429733, [1.8][bytecode] Bad type on operand stack
public void test429733() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						test(new Some<>(1.1d));
					}
					static <S> void test(Option<S> value) {
					}
					static interface Option<T> {
					}
					static class Some<T> implements Option<T> {
						Some(T value) {
				         System.out.println(value);
						}
					}
				}
				"""
		},
		"1.1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429733, [1.8][bytecode] Bad type on operand stack
public void test429733a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						test(new Some<Double>(1.1d));
					}
					static <S> void test(Option<S> value) {
					}
					static interface Option<T> {
					}
					static class Some<T> implements Option<T> {
						Some(T value) {
				         System.out.println(value);
						}
					}
				}
				"""
		},
		"1.1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375394
public void test375394a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    B<C, ? extends C<C>, ? extends C<C>> b = new B<>();
				}
				class B <T, U extends C<T>, V extends U>{}
				class C<T> {}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				B<C, ? extends C<C>, ? extends C<C>> b = new B<>();
				  ^
			C is a raw type. References to generic type C<T> should be parameterized
			----------
			2. WARNING in X.java (at line 2)
				B<C, ? extends C<C>, ? extends C<C>> b = new B<>();
				                 ^
			C is a raw type. References to generic type C<T> should be parameterized
			----------
			3. WARNING in X.java (at line 2)
				B<C, ? extends C<C>, ? extends C<C>> b = new B<>();
				                                 ^
			C is a raw type. References to generic type C<T> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427728, [1.8] Type Inference rejects calls requiring boxing/unboxing
public void test427728b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collections;
				import java.util.LinkedHashMap;
				import java.util.Map;
				public class X {
					public static void main(String[] args) {
						   Map<X, Integer> map = new LinkedHashMap<>();
						   map.put(null, X.getInt());
						   map.put(null, X.getint());
						}
						private static <T> int getInt() {
						   return 0;
						}
						private static int getint() {
							   return 0;
						}
				}
				"""
		},
		"");
}
public void testBug456508() {
	runNegativeTest(
		new String[] {
			"QueryAtom.java",
			"""
				public class QueryAtom<T, P> {
					public QueryAtom(SingularAttribute<? super T, P> path) {
					}
				}
				""",
			"SubqueryIn.java",
			"""
				public class SubqueryIn<S, P>  {
					public SubqueryIn(QueryAtom<S, P>... subqueryAtoms) {
					}
				}
				""",
			"Test.java",
			"""
				class PAccount {}
				class PGroepAccount {}
				interface SingularAttribute<X, T> {}
				
				public class Test {
				    public static volatile SingularAttribute<PGroepAccount, PAccount> account;
				
					public void nietInGroep() {
						recordFilter(new SubqueryIn<>(new QueryAtom<>(account)));
					}
				
					protected <P> void recordFilter(SubqueryIn<?, P> atom) {
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in SubqueryIn.java (at line 2)
				public SubqueryIn(QueryAtom<S, P>... subqueryAtoms) {
				                                     ^^^^^^^^^^^^^
			Type safety: Potential heap pollution via varargs parameter subqueryAtoms
			----------
			----------
			1. WARNING in Test.java (at line 9)
				recordFilter(new SubqueryIn<>(new QueryAtom<>(account)));
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: A generic array of QueryAtom<PGroepAccount,PAccount> is created for a varargs parameter
			----------
			""");
}
public void testBug462083() {
	runConformTest(
		new String[] {
			"Java8InterfaceTest.java",
			"""
				public abstract class Java8InterfaceTest
				{
					public static interface Entity {}
				
					public static interface Service<T1 extends Entity> {}
				
				    public static interface ServiceLocator<T2 extends Entity> {}
				
				    public static class ConcreteClass<T3 extends Entity, S extends Service<T3>> implements ServiceLocator<T3> {}
				
				    protected abstract <T4 extends Entity> ConcreteClass<T4, ?> getParameterized(T4 entity);
				
				    protected <T5 extends Entity> ServiceLocator<T5> getInterface(T5 entity)
				    {
				    	return getParameterized(entity);
				    }
				}
				"""
		});
}
public void testBug469653() {
	String codeContent =
		"""
		import java.util.*;
		
		class ImmutableList<E> {
			static <F> ImmutableList<F> copyOf(Iterable<? extends F> in) { return null; }
			ImmutableList<E> reverse() { return this; }
			Iterator<E> iterator() { return null; }
		}
		public class Code {
		  public static void test() {
		      Iterable<? extends String> services = null;
		      Iterator<String> reverseServices = ImmutableList.copyOf(services).reverse().iterator();
		  }
		}""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTest(
			new String[] { "Code.java", codeContent });
	} else {
		runNegativeTest(
			new String[] { "Code.java", codeContent },
			"""
				----------
				1. ERROR in Code.java (at line 11)
					Iterator<String> reverseServices = ImmutableList.copyOf(services).reverse().iterator();
					                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from Iterator<capture#1-of ? extends String> to Iterator<String>
				----------
				""");
	}
}
public void testBug488649_JDK6791481_ex1() {
	int count = 1;
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				class Test<X> {
					X m(Class<X> c) {return null;}
					X x = m((Class)String.class);
				}
				"""
		},
		"----------\n" +
		(this.complianceLevel >= ClassFileConstants.JDK1_8
			?
		(count++)+". ERROR in Test.java (at line 3)\n" +
		"	X x = m((Class)String.class);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Object to X\n" + // <- want to see this error, but at 1.7- we keep javac compatibility
		"----------\n"
			:
		""
		)+
		(count++)+". WARNING in Test.java (at line 3)\n" +
		"	X x = m((Class)String.class);\n" +
		"	        ^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<X>\n" +
		"----------\n" +
		(count++)+". WARNING in Test.java (at line 3)\n" +
		"	X x = m((Class)String.class);\n" +
		"	         ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n");
}
public void testGH1326() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		class Inner<T> {}
		class Outer<T> {
			Outer(T t) {}
			Outer<T> self() { return this; }
		}
		public class Foo {
			Outer<Inner<String>> x = new Outer<>(new Inner<String>()).self();
		}
		"""
	};
	runner.runConformTest();
}
public void testGH1326_alt() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		class Inner<T> {}
		class Outer<T> {
			Outer(Inner<String> t1, T t2) {}
			Outer<T> self() { return this; }
		}
		public class Foo {
			Inner<String> inner = new Inner<>();
			Outer<Inner<String>> x = new Outer<>(new Inner<String>(), inner).self();
			Outer<Inner<String>> xok = new Outer<>(new Inner<>(), inner).self();
		}
		"""
	};
	runner.expectedCompilerLog =
			this.complianceLevel >= ClassFileConstants.JDK1_8
			?
			"""
			----------
			1. ERROR in Foo.java (at line 8)
				Outer<Inner<String>> x = new Outer<>(new Inner<String>(), inner).self();
				                                         ^^^^^
			Redundant specification of type arguments <String>
			----------
			"""
			: // 1.7 inference is less capable:
			"""
			----------
			1. ERROR in Foo.java (at line 8)
				Outer<Inner<String>> x = new Outer<>(new Inner<String>(), inner).self();
				                                         ^^^^^
			Redundant specification of type arguments <String>
			----------
			2. ERROR in Foo.java (at line 9)
				Outer<Inner<String>> xok = new Outer<>(new Inner<>(), inner).self();
				                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for Outer<>
			----------
			""";
	runner.runNegativeTest();
}
public static Class testClass() {
	return GenericsRegressionTest_1_7.class;
}
}