/*******************************************************************************
 * Copyright (c) 2005, 2021 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class ConditionalExpressionTest extends AbstractRegressionTest {

	public ConditionalExpressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test003" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ConditionalExpressionTest.class;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100162
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    final boolean isA = true;
					    public static void main(String[] args) {
					        X x = new X();
					        System.out.print(x.isA ? "SUCCESS" : "FAILURE");
					    }
					}""",
			},
			"SUCCESS"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107193
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					class RecipeElement {
					    public static final RecipeElement[] NO_CHILDREN= new RecipeElement[0];\s
					}
					class Ingredient extends RecipeElement { }
					class X extends RecipeElement {
					    private Ingredient[] fIngredients;
					    public RecipeElement[] getChildren() {
					        return fIngredients == null ? NO_CHILDREN : fIngredients;
					    }
					}""",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426078, Bug 426078 - [1.8] VerifyError when conditional expression passed as an argument
	public void test003() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						boolean isOdd(boolean what) {
							return square(what ? new Integer(1) : new Integer(2)) % 2 == 1; // trouble here
						}
						<T> int square(int i) {
							return i * i;
						}
						public static void main(String argv[]) {
							System.out.println(new X().isOdd(true));
						}
					}
					""",
			},
			"true"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423685, - [1.8] poly conditional expression must not use lub
	public void test004() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		if (this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"""
							class A{/**/}
							class B extends A {/**/}
							class G<T> {
								G<B> gb=null;
								G<? super A> gsa=null;
								G<? super B> l = (true)? gsa : gb;
							}
							public class X {
								public static void main(String[] args) {
									System.out.println("OK");
								}
							}
							""",
					},
					"""
						----------
						1. ERROR in X.java (at line 6)
							G<? super B> l = (true)? gsa : gb;
							                 ^^^^^^^^^^^^^^^^
						Type mismatch: cannot convert from G<capture#2-of ? extends Object> to G<? super B>
						----------
						"""
				);
		} else {
			this.runConformTest(
					new String[] {
							"X.java",
							"""
								class A{/**/}
								class B extends A {/**/}
								class G<T> {
									G<B> gb=null;
									G<? super A> gsa=null;
									G<? super B> l = (true)? gsa : gb;
								}
								public class X {
									public static void main(String[] args) {
										System.out.println("OK");
									}
								}
								""",
					},
					"OK"
					);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425181, - Cast expression in ternary operation reported as incompatible
	public void test005() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		if (this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"""
							public class X {
							    public static void main(String args[]) {
							    	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported
							       System.out.println("OK");
							    }
							}
							interface I<T> {}
							interface J<T> extends I<T> {}
							""",
					},
					"""
						----------
						1. WARNING in X.java (at line 3)
							I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported
							          ^
						J is a raw type. References to generic type J<T> should be parameterized
						----------
						2. ERROR in X.java (at line 3)
							I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported
							                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						Type mismatch: cannot convert from I<capture#1-of ? extends I> to I<? super J>
						----------
						3. WARNING in X.java (at line 3)
							I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported
							                           ^
						I is a raw type. References to generic type I<T> should be parameterized
						----------
						4. WARNING in X.java (at line 3)
							I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported
							                                         ^
						J is a raw type. References to generic type J<T> should be parameterized
						----------
						"""
				);
		} else {
			this.runConformTest(
					new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String args[]) {
						    	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported
						       System.out.println("OK");
						    }
						}
						interface I<T> {}
						interface J<T> extends I<T> {}
						""",
					},
					"OK"
					);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426315, - [1.8][compiler] UnsupportedOperationException with conditional expression
	public void test006() {
		this.runConformTest(
				new String[] {
					"X.java",
						"""
							public class X {
								static int foo(Object x) {
									return 0;
								}
								static int foo(int e) {\s
									return 1;\s
								}
							 	public static void main(String args[]) {
							 		Object x = new Object();
									System.out.println(foo(true ? x : new int[0]) != 0);
								}
							}
							""",
				},
				"false"
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426680, - [1.8][compiler] Incorrect handling of poly conditional leads to CCE
	public void test007() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							interface BinaryOperation<T> {
							    T operate(T x, T y);
							}
							class StringCatenation implements BinaryOperation<String> {\s
							    public String operate(String x, String y) { return x + y; }
							}
							public class X {
							    public static void main(String argv[]) {
							    	foo(false ? (a,b)->a+b :new StringCatenation());
							    }
							    static void foo(BinaryOperation<Integer> x) {
							       x.operate(5, 15);
							    }
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						foo(false ? (a,b)->a+b :new StringCatenation());
						                        ^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from StringCatenation to BinaryOperation<Integer>
					----------
					"""
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426680, - [1.8][compiler] Incorrect handling of poly conditional leads to CCE
	public void test008() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							interface BinaryOperation<T> {
							    T operate(T x, T y);
							}
							class StringCatenation implements BinaryOperation<String> {\s
							    public String operate(String x, String y) { return x + y; }
							}
							public class X {
							    public static void main(String argv[]) {
							    	foo(false ? new StringCatenation() : (a,b)->a+b);
							    }
							    static void foo(BinaryOperation<Integer> x) {
							       x.operate(5, 15);
							    }
							}
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						foo(false ? new StringCatenation() : (a,b)->a+b);
						            ^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from StringCatenation to BinaryOperation<Integer>
					----------
					"""
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in assignment context
	public void test009() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.util.function.Function;
							public class X {
								public static void main(String[] args) {
									System.out.println(test(1, X::intToSome));
								}
								static <T> Some test(T value, Function<T, Some> f) {
									return (value == null) ? new Nothing() : f.apply(value);
								}
								static SomeInt intToSome(int i) {
									return new SomeInt();
								}
								static abstract class Some {}
								static class SomeInt extends Some {
								    public String toString() {
										return "SomeInt instance";
							        }
							   }
								static class Nothing extends Some {}
							}
							""",
				},
				"SomeInt instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in poly invocation context
	public void test010() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.util.function.Function;
							public class X {
								public static void main(String[] args) {
									System.out.println(test(1, X::intToSome));
								}
								static <T> Some test(T value, Function<T, Some> f) {
									return id((value == null) ? new Nothing<>() : f.apply(value));
								}
								static <T> T id(T t) {
									return t;
								}
								static SomeInt intToSome(int i) {
									return new SomeInt();
								}
								static abstract class Some {}
								static class SomeInt extends Some {
								    public String toString() {
									return "SomeInt instance";
							            }
							        }
								static class Nothing<T> extends Some {}
							}
							""",
				},
				"SomeInt instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in assignment context, order reversed.
	public void test011() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.util.function.Function;
							public class X {
								public static void main(String[] args) {
									System.out.println(test(1, X::intToSome));
								}
								static <T> Some test(T value, Function<T, Some> f) {
									return (value == null) ? f.apply(value) : new Nothing();
								}
								static SomeInt intToSome(int i) {
									return new SomeInt();
								}
								static abstract class Some {}
								static class SomeInt extends Some {
								    public String toString() {
										return "SomeInt instance";
							        }
							   }
								static class Nothing<T> extends Some {
								    public String toString() {
										return "Nothing instance";
							       }
							   }
							}
							""",
				},
				"Nothing instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in poly invocation context, order reversed.
	public void test012() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.util.function.Function;
							public class X {
								public static void main(String[] args) {
									System.out.println(test(1, X::intToSome));
								}
								static <T> Some test(T value, Function<T, Some> f) {
									return id((value == null) ? f.apply(value) : new Nothing<>());
								}
								static <T> T id(T t) {
									return t;
								}
								static SomeInt intToSome(int i) {
									return new SomeInt();
								}
								static abstract class Some {}
								static class SomeInt extends Some {
								    public String toString() {
									return "SomeInt instance";
							            }
							        }
								static class Nothing<T> extends Some {
								    public String toString() {
										return "Nothing instance";
							       }
							   }
							}
							""",
				},
				"Nothing instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in poly invocation context, interface types
	public void test013() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							import java.util.function.Function;
							public class X {
								public static void main(String[] args) {
									System.out.println(test(1, X::intToSome));
								}
								static <T> Some test(T value, Function<T, Some> f) {
									return id((value == null) ? new Nothing<>() : f.apply(value));
								}
								static <T> T id(T t) {
									return t;
								}
								static SomeInt intToSome(int i) {
									return new SomeInt();
								}
								static interface Some {}
								static class SomeInt implements Some {
									public String toString() {
										return "SomeInt instance";
									}
								}
								static class Nothing<T> implements Some {}
							}""",
				},
				"SomeInt instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427438, - NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
	public void test014() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public X(Class clazz) {
								}
								public void error() {
									boolean test = false;
									int i = 1;
									new X(test ? (i == 2 ? D.class : E.class) : null);
								}
								public class D {
								}
								public class E {
								}
							}
							""",
				},
				this.complianceLevel < ClassFileConstants.JDK1_5 ? "" :
					"""
						----------
						1. WARNING in X.java (at line 2)
							public X(Class clazz) {
							         ^^^^^
						Class is a raw type. References to generic type Class<T> should be parameterized
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427438, - NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
	public void test015() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public X(Class clazz) {
								}
								public void error() {
									boolean test = false;
									int i = 1;
									new X(test ? null : (i == 2 ? D.class : E.class));
								}
								public class D {
								}
								public class E {
								}
							}
							""",
				},
				this.complianceLevel < ClassFileConstants.JDK1_5 ? "" :
					"""
						----------
						1. WARNING in X.java (at line 2)
							public X(Class clazz) {
							         ^^^^^
						Class is a raw type. References to generic type Class<T> should be parameterized
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427625, - NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
	public void test427625() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		Map<String,String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.util.Collection;
							import java.util.List;
							public class X {
								public void error(Collection<Object> c) {
									boolean b  =true;
									c.add(b ? Integer.valueOf(1)
									        : c==null ? null\s
											  : c instanceof List ? Integer.valueOf(1)\s
											                      : o());\s
								}
								public Object o() {
									return null;
								}
							}
							""",
				},
				"",
				null, true, options);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432487,  NullPointerException during compilation using jdk1.8.0
	public void testBug432487() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class Y {
						String f() {
							return "";
						}
					}
					public class X {
					void f(String x) {}
						void bar(Y y) {
							f(y.f2() == 1 ? null : y.f());
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					f(y.f2() == 1 ? null : y.f());
					    ^^
				The method f2() is undefined for the type Y
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c113, - Error building JRE8
	public void test437444_c113() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X extends Y {
						    public X(Z[] n) {
						        super((n == null) ? null : n.clone());
						    }
						}
						class Y  {
						    public Y(Z[] notifications) {
						    }
						}
						interface Z {}
						""",
			},
			"");
	}
	public void test437444_2() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X extends Y {
						    public X(int[] n) {
						        super((n == null) ? null : n.clone());
						    }
						}
						class Y  {
						    public Y(int[] notifications) {
						    }
						}
						interface Z {}
						""",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=484425: [bytecode] Bad type on operand stack - compiler omitted instructions for unboxing null Boolean
	public void test484425() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		this.runConformTest(
				new String[] {
						"Main.java",
						"""
							public class Main {
								public static void main(String[] args) {
									try {
										if ((false) ? true: null);
									} catch(NullPointerException npe) {
										System.out.println("Success");
									}
								}
							}
							"""
				},
				"Success");
	}
}
