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
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExpressionContextTests extends AbstractRegressionTest {

static {
	//	TESTS_NAMES = new String[] { "test380112e"};
	//	TESTS_NUMBERS = new int[] { 50 };
	//	TESTS_RANGE = new int[] { 11, -1 };
}

public ExpressionContextTests(String name) {
	super(name);
}

public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

@Override
protected Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	return defaultOptions;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object o = () -> {};
						Object p = (I) () -> {};
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					Object o = () -> {};
					           ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					interface J {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object p = (I & J) () -> {};
					}
					""" ,
			});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					interface J {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object p = (int & I & J) () -> {};
					}
					""" ,
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Object p = (int & I & J) () -> {};
					            ^^^
				Base types are not allowed in intersection cast operator
				----------
				2. ERROR in X.java (at line 10)
					Object p = (int & I & J) () -> {};
					                         ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					interface J {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object p = (X[] & Serializable & Cloneable) new X[0];
					}
					""" ,
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Object p = (X[] & Serializable & Cloneable) new X[0];
					            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Arrays are not allowed in intersection cast operator
				----------
				2. ERROR in X.java (at line 10)
					Object p = (X[] & Serializable & Cloneable) new X[0];
					                  ^^^^^^^^^^^^
				Serializable cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					interface J {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object p = (I & X) () -> {};
					}
					""" ,
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Object p = (I & X) () -> {};
					                ^
				The type X is not an interface; it cannot be specified as a bounded parameter
				----------
				2. ERROR in X.java (at line 10)
					Object p = (I & X) () -> {};
					                   ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					interface J {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object p = (I & J & I) () -> {};
					}
					""" ,
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Object p = (I & J & I) () -> {};
					                    ^
				Duplicate type in intersection cast operator
				----------
				2. ERROR in X.java (at line 10)
					Object p = (I & J & I) () -> {};
					                       ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test007() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					interface I<T> {
						void doit(List<T> x);
						default void doitalso () {}
						boolean equals(Object o);
					}
					public class X {
						I<String> i = (List<String> p) -> {};
						I<X> i2 = (List<String> p) -> {};
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					I<X> i2 = (List<String> p) -> {};
					           ^^^^
				Lambda expression's parameter p is expected to be of type List<X>
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test008() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
						default void doitalso () {}
					}
					interface J {
						void doit();
						default void doitalso () {}
					}
					public class X {
						Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};
					}
					""" ,
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};
					            ^^^^^^^
				Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
				----------
				2. ERROR in X.java (at line 10)
					Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};
					             ^^^^^^
				Marker cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 10)
					Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};
					                                ^^^^^^^^
				Readonly cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 10)
					Object p = (@Marker java.lang. @Readonly String & I & J) () -> {};
					                                                         ^^^^^
				The target type of this expression must be a functional interface
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test009() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"X.java",
				"""
					import java.util.List;
					import java.util.Map;
					interface I<T> {
						void doit(List<T> x);
						boolean equals(Object o);
					}
					public class X {
						I<String> i = (List<String> p) -> {};
						I<X> i2 = (Map<String, String> & I<X>) null;
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 9)
					I<X> i2 = (Map<String, String> & I<X>) null;
					          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from null to I<X> & Map<String,String>
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test010() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					import java.util.Map;
					interface I<T> {
						void doit(List<T> x);
						boolean equals(Object o);
					}
					public class X {
						I<String> i = (List<String> p) -> {};
						I<X> i2 = (Map<String, String>.Entry & I<X> & Serializable) null;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					I<X> i2 = (Map<String, String>.Entry & I<X> & Serializable) null;
					           ^^^^^^^^^^^^^^^^^^^^^^^^^
				The member type Map.Entry<K,V> cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Map<String,String>
				----------
				2. ERROR in X.java (at line 9)
					I<X> i2 = (Map<String, String>.Entry & I<X> & Serializable) null;
					                                              ^^^^^^^^^^^^
				Serializable cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test011() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					public class X {
						X X = (X & J & K) new Y();
					}
					class Y {
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					X X = (X & J & K) new Y();
					      ^^^^^^^^^^^^^^^^^^^
				Cannot cast from Y to X & J & K
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test012() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					public class X {
						X X = (X & J & K) new Y();
					}
					class Y extends X implements Zork {
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 8)
					X X = (X & J & K) new Y();
					      ^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from Y to X & J & K
				----------
				2. ERROR in X.java (at line 10)
					class Y extends X implements Zork {
					                             ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test013() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					public class X {
						X X = (X & J & K) new Y();
					}
					final class Y extends X {
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					X X = (X & J & K) new Y();
					      ^^^^^^^^^^^^^^^^^^^
				Cannot cast from Y to X & J & K
				----------
				2. WARNING in X.java (at line 8)
					X X = (X & J & K) new Y();
					      ^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from Y to X & J & K
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test014() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					public class X {
					   I i = null;
						X X = (X & J & K) i;
					}
					final class Y extends P {
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					final class Y extends P {
					                      ^
				P cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test015() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					final public class X {
					   I i = null;
						X X = (X & J & K) i;
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					X X = (X & J & K) i;
					      ^^^^^^^^^^^^^
				Cannot cast from I to X & J & K
				----------
				2. ERROR in X.java (at line 10)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test016() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					final public class X implements I {
					   I i = null;
						X X = (X & J & K) i;
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399773, [1.8][compiler] Cast expression should allow for additional bounds to form intersection types
public void test017() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					}
					interface J {
					}
					interface K {
					}
					public class X {
					   I i = null;
						X X = (X & J & K) (X & K & J) i;
					   Zork z;
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 9)
					X X = (X & J & K) (X & K & J) i;
					      ^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from X & K & J to X & J & K
				----------
				2. ERROR in X.java (at line 10)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778, [1.8][compiler] Conditional operator expressions should propagate target types
public void test018() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
					}
					class X {
						Object o = (I) () -> {};
						I k = (()->{});
						I i = 1 == 2 ? () -> {} : () -> {};
						I j = () -> {};
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 7)
					I i = 1 == 2 ? () -> {} : () -> {};
					               ^^^^^^^^
				Dead code
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778, [1.8][compiler] Conditional operator expressions should propagate target types
public void test019() {
	this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
					}
					class X {
						I [] k = {(()->{}), ()->{}, 1 == 2 ? () -> {} : ()->{}};
						I [][] i = {{()->{}}};
						void foo() {
					       I i = () -> {};
					   }
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					I [] k = {(()->{}), ()->{}, 1 == 2 ? () -> {} : ()->{}};
					                                     ^^^^^^^^
				Dead code
				----------
				2. WARNING in X.java (at line 8)
					I i = () -> {};
					  ^
				The local variable i is hiding a field from type X
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778, [1.8][compiler] Conditional operator expressions should propagate target types
public void test020() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						J doit(int x);
					}
					interface J {
						K doit();
					}
					interface K {
					   I doit();
					}\
					class X {
						I foo() {
					       return x -> { return () -> () -> "Hello"; };
					   }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					return x -> { return () -> () -> "Hello"; };
					                                 ^^^^^^^
				Type mismatch: cannot convert from String to I
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778, [1.8][compiler] Conditional operator expressions should propagate target types
public void test021() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void doit();
					}
					class X {
						I foo() {
					       return "Hello" + () -> {};
					   }
						I goo() {
					       return "Hello" + (I)(() -> {});
					   }
						I zoo() {
					       return 10 + (() -> {});
					   }
						I boo() {
					       return 10 + (I) (() -> {});
					   }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					return "Hello" + () -> {};
					                 ^^^^^
				The target type of this expression must be a functional interface
				----------
				2. ERROR in X.java (at line 9)
					return "Hello" + (I)(() -> {});
					       ^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from String to I
				----------
				3. ERROR in X.java (at line 12)
					return 10 + (() -> {});
					            ^^^^^^
				The target type of this expression must be a functional interface
				----------
				4. ERROR in X.java (at line 15)
					return 10 + (I) (() -> {});
					       ^^^^^^^^^^^^^^^^^^^
				The operator + is undefined for the argument type(s) int, I
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401222, [1.8][compiler] Conditional operator expressions results differ from 8b76
public void test022() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.List;
					class X {
						int foo(int x) {
							List<String> l = x == 2 ? (List<String>)(null) : 1;
							List<String> m = x == 2 ? 1 : (List<String>)(null);
							return 1;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					List<String> l = x == 2 ? (List<String>)(null) : 1;
					                                                 ^
				Type mismatch: cannot convert from int to List<String>
				----------
				2. ERROR in X.java (at line 6)
					List<String> m = x == 2 ? 1 : (List<String>)(null);
					                          ^
				Type mismatch: cannot convert from int to List<String>
				----------
				""");
}
public static Class testClass() {
	return ExpressionContextTests.class;
}
}
