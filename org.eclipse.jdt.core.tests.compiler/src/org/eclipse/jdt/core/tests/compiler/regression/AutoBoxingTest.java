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

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AutoBoxingTest extends AbstractComparableTest {

	public AutoBoxingTest(String name) {
		super(name);
	}

	@Override
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
		return defaultOptions;
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 78 };
//		TESTS_RANGE = new int[] { 151, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return AutoBoxingTest.class;
	}

	public void test001() { // constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(1);
						}
						public static void test(Integer i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test((byte)127);
						}
						public static void test(Byte b) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test('b');
						}
						public static void test(Character c) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(-0.0f);
						}
						public static void test(Float f) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(0.0);
						}
						public static void test(Double d) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(Long.MAX_VALUE);
						}
						public static void test(Long l) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(Short.MAX_VALUE);
						}
						public static void test(Short s) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(false);
						}
						public static void test(Boolean b) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test002() { // non constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static int bar() {return 1;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Integer i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static byte bar() {return 1;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Byte b) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static char bar() {return 'c';}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Character c) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static float bar() {return 0.0f;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Float f) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static double bar() {return 0.0;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Double d) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static long bar() {return 0;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Long l) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static short bar() {return 0;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Short s) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static boolean bar() {return true;}
						public static void main(String[] s) {
							test(bar());
						}
						public static void test(Boolean b) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test003() { // Number -> base type
		// Integer -> int
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Integer(1));
						}
						public static void test(int i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Byte -> byte
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Byte((byte) 1));
						}
						public static void test(byte b) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Byte -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Byte((byte) 1));
						}
						public static void test(long l) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Character -> char
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Character('c'));
						}
						public static void test(char c) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Float -> float
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Float(0.0f));
						}
						public static void test(float f) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Double -> double
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Double(0.0));
						}
						public static void test(double d) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Long -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Long(0L));
						}
						public static void test(long l) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Short -> short
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(new Short((short) 0));
						}
						public static void test(short s) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		// Boolean -> boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							test(Boolean.TRUE);
						}
						public static void test(boolean b) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test004() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.test(1);
						}
					}
					class Y {
						private static void test(int i) { System.out.print('n'); }
						static void test(int... i) { System.out.print('n'); }
						public static void test(Integer i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							new Y().test(1);
						}
					}
					class Y {
						private void test(int i) { System.out.print('n'); }
						void test(int... i) { System.out.print('n'); }
						public void test(Integer i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test005() { // this is NOT an ambiguous case as 'long' is matched before autoboxing kicks in
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							new Y().test(1);
						}
					}
					class Y {
						void test(Integer i) { System.out.print('n'); }
						void test(long i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test006() {
		this.runNegativeTest( // Integers are not compatible with Longs, even though ints are compatible with longs
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							new Y().test(1, 1);
						}
					}
					class Y {
						void test(Long i, int j) { System.out.print('n'); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new Y().test(1, 1);
					        ^^^^
				The method test(Long, int) in the type Y is not applicable for the arguments (int, int)
				----------
				"""
		);
		this.runNegativeTest( // likewise with Byte and Integer
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							new Y().test((byte) 1, 1);
						}
					}
					class Y {
						void test(Integer i, int j) { System.out.print('n'); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new Y().test((byte) 1, 1);
					        ^^^^
				The method test(Integer, int) in the type Y is not applicable for the arguments (byte, int)
				----------
				"""
		);
	}

	public void test007() {
		this.runConformTest( // this is NOT an ambiguous case as Long is not a match for int
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							new Y().test(1, 1);
						}
					}
					class Y {
						void test(Long i, int j) { System.out.print('n'); }
						void test(long i, Integer j) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test008() { // test autoboxing AND varargs method match
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.test(1, new Integer(2), -3);
						}
					}
					class Y {
						public static void test(int ... i) { System.out.print('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test009() {
		this.runNegativeTest( // 2 of these sends are ambiguous
			new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings("deprecation")
						public static void main(String[] s) {
							new Y().test(1, 1);
							new Y().test(Integer.valueOf(1), Integer.valueOf(1));
						}
					}
					class Y {
						void test(Integer i, int j) {}
						void test(int i, Integer j) {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					new Y().test(1, 1);
					        ^^^^
				The method test(Integer, int) is ambiguous for the type Y
				----------
				2. ERROR in X.java (at line 5)
					new Y().test(Integer.valueOf(1), Integer.valueOf(1));
					        ^^^^
				The method test(Integer, int) is ambiguous for the type Y
				----------
				"""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							new Y().test(new Integer(1), 1);
							new Y().test(1, new Integer(1));
						}
					}
					class Y {
						void test(Integer i, int j) { System.out.print(1); }
						void test(int i, Integer j) { System.out.print(2); }
					}
					""",
			},
			"12"
		);
	}

	public void test010() { // local declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							int i = Y.test();
							System.out.print(i);
						}
					}
					class Y {
						public static Byte test() { return new Byte((byte) 1); }
					}
					""",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Object o = Y.test();
							System.out.print(o);
						}
					}
					class Y {
						public static int test() { return 1; }
					}
					""",
			},
			"1"
		);
	}

	public void test011() { // field declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static int i = Y.test();
						public static void main(String[] s) {
							System.out.print(i);
						}
					}
					class Y {
						public static Byte test() { return new Byte((byte) 1); }
					}
					""",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static Object o = Y.test();
						public static void main(String[] s) {
							System.out.print(o);
						}
					}
					class Y {
						public static int test() { return 1; }
					}
					""",
			},
			"1"
		);
	}

	public void test012() { // varargs and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer x = new Integer(15);\s
							int y = 32;
							System.out.printf("%x + %x", x, y);
						}
					}""",
			},
			"f + 20"
		);
	}

	public void test013() { // foreach and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
							for (final Integer e : tab) {
								System.out.print(e);
							}
						}
					}
					""",
			},
			"123456789"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer[] tab = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
							for (final int e : tab) {
								System.out.print(e);
							}
						}
					}
					""",
			},
			"123456789"
		);
	}

	public void test014() { // switch
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Integer i = new Integer(1);
							switch(i) {
								case 1 : System.out.print('y');
							}
						}
					}
					""",
			},
			"y"
		);
	}

	public void test015() { // return statement
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static Integer foo1() {
							return 0;
						}
						static int foo2() {
							return new Integer(0);
						}
						public static void main(String[] args) {
							System.out.print(foo1());
							System.out.println(foo2());
						}
					}
					""",
			},
			"00"
		);
	}

	public void test016() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer i = args.length == 0 ? 0 : new Integer(1);
							System.out.println(i);
						}
					}
					""",
			},
			"0"
		);
	}

	public void test017() { // cast expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer i = new Integer(1);
							System.out.println((int)i);
						}
					}
					""",
			},
			"1"
		);
	}

	public void test018() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings("deprecation")
						public static void main(String[] args) {
							Float f = args.length == 0 ? Float.valueOf(0) : 0;
							System.out.println((int)f);
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					Float f = args.length == 0 ? Float.valueOf(0) : 0;
					          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The expression of type float is boxed into Float
				----------
				2. WARNING in X.java (at line 4)
					Float f = args.length == 0 ? Float.valueOf(0) : 0;
					                             ^^^^^^^^^^^^^^^^
				The expression of type Float is unboxed into float
				----------
				3. ERROR in X.java (at line 5)
					System.out.println((int)f);
					                   ^^^^^^
				Cannot cast from Float to int
				----------
				""");
	}

	public void test019() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println((Integer) 0);
							System.out.println((Float) 0);
						\t
						}
					}
					""",
			},
		"""
			----------
			1. WARNING in X.java (at line 3)
				System.out.println((Integer) 0);
				                             ^
			The expression of type int is boxed into Integer
			----------
			2. ERROR in X.java (at line 4)
				System.out.println((Float) 0);
				                   ^^^^^^^^^
			Cannot cast from int to Float
			----------
			""");
	}

	public void test020() { // binary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
					      System.out.println(2 + b);
					    }
					}
					""",
			},
			"3"
		);
	}

	public void test021() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    Integer i = +b + (-b);
							System.out.println(i);
					    }
					}
					""",
			},
			"0"
		);
	}

	public void test022() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    Integer i = 0;
						    int n = b + i;
							System.out.println(n);
					    }
					}
					""",
			},
			"1"
		);
	}

	public void test023() { // 78849
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Character cValue = new Character('c');
							if ('c' == cValue) System.out.println('y');
						}
					}
					""",
			},
			"y"
		);
	}

	public void test024() { // 79254
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) { test(2); }
						static void test(Object o) { System.out.println('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test025() { // 79641
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) { test(true); }
						static void test(Object ... o) { System.out.println('y'); }
					}
					""",
			},
			"y"
		);
	}

	public void test026() { // compound assignment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    Integer i = 0;
						    i += b;
							System.out.println(i);
					    }
					}
					""",
			},
			"1"
		);
	}

	public void test027() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							if (0 == new X()) {
								System.out.println();
							}
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					if (0 == new X()) {
					    ^^^^^^^^^^^^
				Incompatible operand types int and X
				----------
				"""
		);
	}

	public void test028() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    int i = +b;
							System.out.println(i);
					    }
					}
					""",
			},
			"1"
		);
	}

	public void test029() { // generic type case
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
						    int sum = 0;
						    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {
						    	sum += iterator.next();
						    }
					        System.out.print(sum);
					    }
					}""",
			},
			"10"
		);
	}

	public void test030() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
							Boolean b = Boolean.TRUE;
						\t
							if (b && !b) {
								System.out.print("THEN");
							} else {
								System.out.print("ELSE");
							}
					    }
					}""",
			},
			"ELSE"
		);
	}

	public void test031() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static Boolean foo() { return Boolean.FALSE; }
						public static void main(String[] args) {
							Boolean b = foo();
						\t
							if (!b) {
								System.out.print("THEN");
							} else {
								System.out.print("ELSE");
							}
					    }
					}""",
			},
			"THEN"
		);
	}

	public void test032() throws Exception { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] args) {
					      if (new Integer(1) == new Integer(0)) {
					         System.out.println();
					      }
					      System.out.print("SUCCESS");
					   }
					}""",
			},
			"SUCCESS"
		);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 4, Locals: 1
			  public static void main(java.lang.String[] args);
			     0  new java.lang.Integer [16]
			     3  dup
			     4  iconst_1
			     5  invokespecial java.lang.Integer(int) [18]
			     8  new java.lang.Integer [16]
			    11  dup
			    12  iconst_0
			    13  invokespecial java.lang.Integer(int) [18]
			    16  if_acmpne 25
			    19  getstatic java.lang.System.out : java.io.PrintStream [21]
			    22  invokevirtual java.io.PrintStream.println() : void [27]
			    25  getstatic java.lang.System.out : java.io.PrintStream [21]
			    28  ldc <String "SUCCESS"> [32]
			    30  invokevirtual java.io.PrintStream.print(java.lang.String) : void [34]
			    33  return
			""";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test033() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] s) {
					      System.out.print(Boolean.TRUE || Boolean.FALSE);
					   }
					}""",
			},
			"true"
		);
	}

	public void test034() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    int i = b++;
							System.out.print(i);
							System.out.print(b);
					    }
					}
					""",
			},
			"12"
		);
	}

	public void test035() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    int i = b--;
							System.out.print(i);
							System.out.print(b);
					    }
					}
					""",
			},
			"10"
		);
	}

	public void test036() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    int i = ++b;
							System.out.print(i);
							System.out.print(b);
					    }
					}
					""",
			},
			"22"
		);
	}

	public void test037() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
						    Byte b = new Byte((byte)1);
						    int i = --b;
							System.out.print(i);
							System.out.print(b);
					    }
					}
					""",
			},
			"00"
		);
	}

	public void test038() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static boolean foo() { return false; }
					   public static void main(String[] s) {
							boolean b = foo();
					      System.out.print(b || Boolean.FALSE);
					   }
					}""",
			},
			"false"
		);
	}

	public void test039() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							int i = 0;
							if (i != null) {
							}
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (i != null) {
					    ^^^^^^^^^
				The operator != is undefined for the argument type(s) int, null
				----------
				"""
		);
	}

	public void test040() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
							Integer i = new Integer(1);
							if (i == null)
								i++;
							System.out.print(i);
						}
					}""",
			},
			"1"
		);
	}

	public void test041() { // equal expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer i = 0;
							if (i != null) {
								System.out.println("SUCCESS");
							}
						}
					}
					""",
			},
			"SUCCESS"
		);
	}

	public void test042() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static Boolean bar() { return Boolean.TRUE; }\s
						public static void main(String[] args) {
							Integer i = bar() ? new Integer(1) : null;
							int j = i;
							System.out.print(j);
						}
					}""",
			},
			"1"
		);
	}

	public void test043() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer i = 0;
							i += "aaa";
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Integer i = 0;
					            ^
				The expression of type int is boxed into Integer
				----------
				2. ERROR in X.java (at line 4)
					i += "aaa";
					^^^^^^^^^^
				The operator += is undefined for the argument type(s) Integer, String
				----------
				""");
	}

	public void test044() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer i = 0;
							i += null;
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Integer i = 0;
					            ^
				The expression of type int is boxed into Integer
				----------
				2. ERROR in X.java (at line 4)
					i += null;
					^^^^^^^^^
				The operator += is undefined for the argument type(s) Integer, null
				----------
				""");
	}

	public void test045() { // binary expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer i = 0;
							i = i + null;
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Integer i = 0;
					            ^
				The expression of type int is boxed into Integer
				----------
				2. ERROR in X.java (at line 4)
					i = i + null;
					    ^^^^^^^^
				The operator + is undefined for the argument type(s) Integer, null
				----------
				""");
	}

	public void test046() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Byte b = new Byte((byte)1);
							b++;
							System.out.println((Byte)b);
						}
					}
					""",
			},
			"2");
	}

	public void test047() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Byte b = new Byte((byte)1);
							b++;
							if (b instanceof Byte) {
								System.out.println("SUCCESS" + b);
							}
						}
					}
					""",
			},
			"SUCCESS2");
	}

	public void test048() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static Byte b = new Byte((byte)1);
						public static void main(String[] s) {
							b++;
							if (b instanceof Byte) {
								System.out.print("SUCCESS" + b);
							}
						}
					}
					""",
			},
			"SUCCESS2");
	}

	public void test049() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static class Y {
							public static Byte b = new Byte((byte)1);
						}
						public static void main(String[] s) {
							X.Y.b++;
							if (X.Y.b instanceof Byte) {
								System.out.print("SUCCESS" + X.Y.b);
							}
						}
					}
					""",
			},
			"SUCCESS2");
	}

	public void test050() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static Byte b = new Byte((byte)1);
						public static void main(String[] s) {
							++b;
							if (b instanceof Byte) {
								System.out.print("SUCCESS" + b);
							}
						}
					}
					""",
			},
			"SUCCESS2");
	}

	public void test051() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static class Y {
							public static Byte b = new Byte((byte)1);
						}
						public static void main(String[] s) {
							++X.Y.b;
							if (X.Y.b instanceof Byte) {
								System.out.print("SUCCESS" + X.Y.b);
							}
						}
					}
					""",
			},
			"SUCCESS2");
	}

	public void test052() { // boxing in var decl
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Byte b = 0;
							++b;
							foo(0);
						}
						static void foo(Byte b) {
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Byte b = 0;
					         ^
				The expression of type int is boxed into Byte
				----------
				2. WARNING in X.java (at line 4)
					++b;
					^^^
				The expression of type byte is boxed into Byte
				----------
				3. WARNING in X.java (at line 4)
					++b;
					  ^
				The expression of type Byte is unboxed into int
				----------
				4. ERROR in X.java (at line 5)
					foo(0);
					^^^
				The method foo(Byte) in the type X is not applicable for the arguments (int)
				----------
				""");
	}

	public void test053() { // boxing in var decl
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Byte b = 1;
							++b;
							if (b instanceof Byte) {
								System.out.println("SUCCESS");
							}
						}
					}
					""",
			},
			"SUCCESS");
	}

	public void test054() { // boxing in field decl
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static Byte b = 1;
						public static void main(String[] s) {
							++b;
							if (b instanceof Byte) {
								System.out.println("SUCCESS");
							}
						}
					}
					""",
			},
			"SUCCESS");
	}

	public void test055() { // boxing in foreach
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							byte[] bytes = {0, 1, 2};
							for(Integer i : bytes) {
								System.out.print(i);
							}
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					for(Integer i : bytes) {
					                ^^^^^
				Type mismatch: cannot convert from element type byte to Integer
				----------
				2. WARNING in X.java (at line 4)
					for(Integer i : bytes) {
					                ^^^^^
				The expression of type byte is boxed into Integer
				----------
				""");
	}

	public void test056() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							int[] ints = {0, 1, 2};
							for(Integer i : ints) {
								System.out.print(i);
							}
						}
					}
					""",
			},
			"012");
	}

	public void test057() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							byte[] bytes = {0, 1, 2};
							for(Byte b : bytes) {
								System.out.print(b);
							}
						}
					}
					""",
			},
			"012");
	}

	public void test058() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
						    int sum = 0;
						    for (Integer i : list) {
						    	sum += i;
						    }	   \s
					        System.out.print(sum);
					    }
					}
					""",
			},
			"10");
	}

	public void test059() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
						    int sum = 0;
						    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {
						    	if (1 == iterator.next()) {
						    		System.out.println("SUCCESS");
						    		break;
						    	}
						    }
					    }
					}
					""",
			},
			"SUCCESS");
	}

	public void test060() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Boolean> list = new ArrayList<Boolean>();
							for (int i = 0; i < 5; i++) {
								list.add(i % 2 == 0);
						    }
						    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {
						    	if (iterator.next()) {
						    		System.out.println("SUCCESS");
						    		break;
						    	}
						    }
					    }
					}
					""",
			},
			"SUCCESS");
	}

	public void test061() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Boolean> list = new ArrayList<Boolean>();
							boolean b = true;
							for (int i = 0; i < 5; i++) {
								list.add((i % 2 == 0) && b);
						    }
						    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {
						    	if (iterator.next()) {
						    		System.out.println("SUCCESS");
						    		break;
						    	}
						    }
					    }
					}
					""",
			},
			"SUCCESS");
	}

	public void test062() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							boolean b = true;
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
							int sum = 0;
						    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {
						    	sum = sum + iterator.next();
						    }
						    System.out.println(sum);
					    }
					}
					""",
			},
			"10");
	}

	public void test063() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							boolean b = true;
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
							int val = 0;
						    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {
						    	val = ~ iterator.next();
						    }
						    System.out.println(val);
					    }
					}
					""",
			},
			"-5");
	}

	public void test064() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					import java.util.Iterator;
					
					public class X {
					
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							boolean b = true;
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
							int val = 0;
						    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {
						    	val += (int) iterator.next();
						    }
						    System.out.println(val);
					    }
					}
					""",
			},
			"10");
	}

	public void test065() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					
					public class X {
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
						    int sum = 0;
						    for (int i : list) {
						    	sum += i;
						    }
					        System.out.print(sum);
					    }
					}""",
			},
			"10"
		);
	}

	public void test066() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					
					public class X {
						public static void main(String[] args) {
							Integer[] tab = new Integer[] {0, 1, 2, 3, 4};
						    int sum = 0;
						    for (int i : tab) {
						    	sum += i;
						    }
					        System.out.print(sum);
					    }
					}""",
			},
			"10"
		);
	}

	public void test067() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					
					public class X {
						public static void main(String[] args) {
							int[] tab = new int[] {0, 1, 2, 3, 4};
						    int sum = 0;
						    for (Integer i : tab) {
						    	sum += i;
						    }
					        System.out.print(sum);
					    }
					}""",
			},
			"10"
		);
	}

	public void test068() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					
					public class X {
						public static void main(String[] args) {
							List<Integer> list = new ArrayList<Integer>();
							for (int i = 0; i < 5; i++) {
								list.add(i);
						    }
						    int sum = 0;
						    for (Integer i : list) {
						    	sum += i;
						    }
					        System.out.print(sum);
					    }
					}""",
			},
			"10"
		);
	}

	public void test069() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Boolean bool = true;
							assert bool : "failed";
						    System.out.println("SUCCESS");
						}
					}
					""",
			},
			"SUCCESS");
	}

	public void test070() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					
					public class X {
						public static void main(String[] args) {
							List<Boolean> lb = new ArrayList<Boolean>();
							lb.add(true);
							Iterator<Boolean> iterator = lb.iterator();
							assert iterator.next() : "failed";
						    System.out.println("SUCCESS");
						}
					}
					""",
			},
			"SUCCESS");
	}

	public void test071() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					
					public class X {
						public static void main(String[] args) {
							List<Boolean> lb = new ArrayList<Boolean>();
							lb.add(true);
							Iterator<Boolean> iterator = lb.iterator();
							assert args != null : iterator.next();
						    System.out.println("SUCCESS");
						}
					}
					""",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81971
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
					        doFoo(getVoid());
					    }
					
					    private static void doFoo(Object o) { }
					
					    private static void getVoid() { }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					doFoo(getVoid());
					^^^^^
				The method doFoo(Object) in the type X is not applicable for the arguments (void)
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81571
	public void test073() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@SuppressWarnings("deprecation")
					    public static void main(String[] args) {
					        a(Integer.valueOf(1), 2);
					    }
					    public static void a(int a, int b) { System.out.println("SUCCESS"); }
					    public static void a(Object a, Object b) {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					a(Integer.valueOf(1), 2);
					^
				The method a(int, int) is ambiguous for the type X
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432
	public void test074() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 Object e() {
					  return "".compareTo("") > 0;
					 }
					 public static void main(String[] args) {
					  System.out.print(new X().e());
					 }
					}""",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 Object e() {
					  return "".compareTo("") > 0;
					 }
					 public static void main(String[] args) {
					  System.out.print(new X().e());
					 }
					 Zork z;
					}""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					return "".compareTo("") > 0;
					       ^^^^^^^^^^^^^^^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				2. ERROR in X.java (at line 8)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 Object e() {
					 int i = 12;\s
					  boolean b = false;
					  switch(i) {
					    case 0: return i > 0;
					    case 1: return i >= 0;
					    case 2: return i < 0;
					    case 3: return i <= 0;
					    case 4: return i == 0;
					    case 5: return i != 0;
					    case 6: return i & 0;
					    case 7: return i ^ 0;
					    case 8: return i | 0;
					    case 9: return b && b;
					    default: return b || b;
					  }
					 }
					 public static void main(String[] args) {
					  System.out.print(new X().e());
					 }
					 Zork z;
					}""",
			},
			"""
				----------
				1. WARNING in X.java (at line 6)
					case 0: return i > 0;
					               ^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				2. WARNING in X.java (at line 7)
					case 1: return i >= 0;
					               ^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				3. WARNING in X.java (at line 8)
					case 2: return i < 0;
					               ^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				4. WARNING in X.java (at line 9)
					case 3: return i <= 0;
					               ^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				5. WARNING in X.java (at line 10)
					case 4: return i == 0;
					               ^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				6. WARNING in X.java (at line 11)
					case 5: return i != 0;
					               ^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				7. WARNING in X.java (at line 12)
					case 6: return i & 0;
					               ^^^^^
				The expression of type int is boxed into Integer
				----------
				8. WARNING in X.java (at line 13)
					case 7: return i ^ 0;
					               ^^^^^
				The expression of type int is boxed into Integer
				----------
				9. WARNING in X.java (at line 14)
					case 8: return i | 0;
					               ^^^^^
				The expression of type int is boxed into Integer
				----------
				10. WARNING in X.java (at line 15)
					case 9: return b && b;
					               ^^^^^^
				Comparing identical expressions
				----------
				11. WARNING in X.java (at line 15)
					case 9: return b && b;
					               ^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				12. WARNING in X.java (at line 16)
					default: return b || b;
					                ^^^^^^
				Comparing identical expressions
				----------
				13. WARNING in X.java (at line 16)
					default: return b || b;
					                ^^^^^^
				The expression of type boolean is boxed into Boolean
				----------
				14. ERROR in X.java (at line 22)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test077() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 Object e() {
					 int i = 12;\s
					  boolean b = false;
					  switch(i) {
					    case 0: return i > 0;
					    case 1: return i >= 0;
					    case 2: return i < 0;
					    case 3: return i <= 0;
					    case 4: return i == 0;
					    case 5: return i != 0;
					    case 6: return i & 0;
					    case 7: return i ^ 0;
					    case 8: return i | 0;
					    case 9: return b && b;
					    default: return b || b;
					  }
					 }
					 public static void main(String[] args) {
					  System.out.print(new X().e());
					 }
					}""",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81923
	public void test078() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
						public <A extends T> X(A... t) {}
						<T> void foo(T... t) {}
						<T> void zip(T t) {}
						void test() {
							new X<Integer>(10, 20);
							foo(10);
							foo(10, 20);
							zip(10);
						}
					}
					"""
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407
	public void _test079() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.HashMap;
					public class X {
						static HashMap<Character, Character> substitutionList(String s1, String s2) {
							HashMap<Character, Character> subst = new HashMap<Character, Character>();
							for (int i = 0; i < s1.length(); i++) {
								char key = s1.charAt(i);
								char value = s2.charAt(i);
								if (subst.containsKey(key)) {
									if (value != subst.get(key)) {
										return null;
									}
								} else if (subst.containsValue(value)) {
									return null;
								} else {
									subst.put(key, value);
								}
							}
							return subst;
						}
						public static void main(String[] args) {
							System.out.println("Bogon");
						}
					}
					"""
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407 - variation
	public void test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.HashMap;
					
					public class X {
					
						public static void main(String[] args) {
							HashMap<Character, Character> subst = new HashMap<Character, Character>();
							subst.put(\'a\', \'a\');
							if (\'a\' == subst.get(\'a\')) {
								System.out.println("SUCCESS");
							}
						}
					}
					"""
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407 - variation
	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.HashMap;
					
					public class X {
					
						public static void main(String[] args) {
							HashMap<Byte, Byte> subst = new HashMap<Byte, Byte>();
							subst.put((byte)1, (byte)1);
							if (1 + subst.get((byte)1) > 0.f) {
								System.out.println("SUCCESS");
							}	\t
						}
					}
					"""
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82859
	public void test082() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String argv[]) {
							System.out.println(void.class == Void.TYPE);
						}
					}"""
			},
			"true"
		);
	}

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82647
	public void test083() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int counter = 0;
					
						public boolean wasNull() {
							return ++counter % 2 == 0;
						}
					
						private Byte getByte() {
							return (byte) 0;
						}
					
						private Short getShort() {
							return (short) 0;
						}
					
						private Long getLong() {
							return 0L;
						}
					
						private Integer getInt() {
							return 0; // autoboxed okay
						}
					
						// This should be the same as the second one.
						private Byte getBytey() {
							byte value = getByte();
							return wasNull() ? null : value;
						}
					
						private Byte getByteyNoBoxing() {
							byte value = getByte();
							return wasNull() ? null : (Byte) value;
						}
					
						// This should be the same as the second one.
						private Short getShorty() {
							short value = getShort();
							return wasNull() ? null : value;
						}
					
						private Short getShortyNoBoxing() {
							short value = getShort();
							return wasNull() ? null : (Short) value;
						}
					
						// This should be the same as the second one.
						private Long getLongy() {
							long value = getLong();
							return wasNull() ? null : value;
						}
					
						private Long getLongyNoBoxing() {
							long value = getLong();
							return wasNull() ? null : (Long) value;
						}
					
						// This should be the same as the second one.
						private Integer getIntegery() {
							int value = getInt();
							return wasNull() ? null : value;
						}
					
						private Integer getIntegeryNoBoxing() {
							int value = getInt();
							return wasNull() ? null : (Integer) value;
						}
					}
					"""
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82647 - variation
	public void test084() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						Short foo() {
							short value = 0;
							return this == null ? null : value;
						}
						boolean bar() {
							short value = 0;
							return null == value;
						}
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					return this == null ? null : value;
					                             ^^^^^
				The expression of type short is boxed into Short
				----------
				2. ERROR in X.java (at line 8)
					return null == value;
					       ^^^^^^^^^^^^^
				The operator == is undefined for the argument type(s) null, short
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83965
	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						private static void checkByteConversions(Byte _byte) {
							short s = (short) _byte;
							short s2 = _byte;
							int i = (int) _byte;
							long l = (long) _byte;
							float f = (float) _byte;
							double d = (double) _byte;
							if ( _byte.byteValue() != s ) {
					            System.err.println("Must be equal 0");
					        }
							if ( _byte.byteValue() != i ) {
					            System.err.println("Must be equal 1");
					        }
							if ( _byte.byteValue() != l ) {
					            System.err.println("Must be equal 2");
					        }
							if ( _byte.byteValue() != f ) {
					            System.err.println("Must be equal 3");
					        }
							if ( _byte.byteValue() != d ) {
					            System.err.println("Must be equal 4");
					        }
						}\s
					
						private static void checkCharacterConversions(Character _character) {
							int i = (int) _character;
							long l = (long) _character;
							float f = (float) _character;
							double d = (double) _character;
							if ( _character.charValue() != i ) {
					            System.err.println("Must be equal 9");
					        }
							if ( _character.charValue() != l ) {
					            System.err.println("Must be equal 10");
					        }
							if ( _character.charValue() != f ) {
					            System.err.println("Must be equal 11");
					        }
							if ( _character.charValue() != d ) {
					            System.err.println("Must be equal 12");
					        }
						}
					
						private static void checkFloatConversions(Float _float) {
							double d = (double) _float;
							if ( _float.floatValue() != d ) {
					            System.err.println("Must be equal 18");
					        }
						}
					
						private static void checkIntegerConversions(Integer _integer) {
							long l = (long) _integer;
							float f = (float) _integer;
							double d = (double) _integer;
							if ( _integer.intValue() != l ) {
					            System.err.println("Must be equal 13");
					        }
							if ( _integer.intValue() != f ) {
					            System.err.println("Must be equal 14");
					        }
							if ( _integer.intValue() != d ) {
					            System.err.println("Must be equal 15");
					        }
						}
					
						private static void checkIntegerConversions(Short _short) {
							int i = (int) _short;
							long l = (long) _short;
							float f = (float) _short;
							double d = (double) _short;
							if ( _short.shortValue() != i ) {
					            System.err.println("Must be equal 5");
					        }
							if ( _short.shortValue() != l ) {
					            System.err.println("Must be equal 6");
					        }
							if ( _short.shortValue() != f ) {
					            System.err.println("Must be equal 7");
					        }
							if ( _short.shortValue() != d ) {
					            System.err.println("Must be equal 8");
					        }
						}
					
						private static void checkLongConversions(Long _long) {
							float f = (float) _long;
							double d = (double) _long;
							if ( _long.longValue() != f ) {
					            System.err.println("Must be equal 16");
					        }
							if ( _long.longValue() != d ) {
					            System.err.println("Must be equal 17");
					        }
						}
					
					    public static void main(String args[]) {
					        Byte _byte = new Byte((byte)2);
					        Character _character = new Character(\'@\');
					        Short _short = new Short((short)255);
					        Integer _integer = new Integer(12345678);
					        Long _long = new Long(1234567890);
					        Float _float = new Float(-0.0);
					
					        checkByteConversions(_byte);
					        checkIntegerConversions(_short);
					        checkCharacterConversions(_character);
					        checkIntegerConversions(_integer);
					        checkLongConversions(_long);
					        checkFloatConversions(_float);
					
					        System.out.println("OK");
					      }
					}
					"""
			},
			"OK"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84055
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  private static void checkConversions(byte _byte) {
					    Short s = (short) _byte; // cast is necessary
					    Short s2 = _byte; // ko
					  }\s
					  public static void main(String args[]) {
					    byte _byte = 2;
					    checkConversions(_byte);
					    System.out.println("OK");
					  }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Short s = (short) _byte; // cast is necessary
					          ^^^^^^^^^^^^^
				The expression of type short is boxed into Short
				----------
				2. ERROR in X.java (at line 4)
					Short s2 = _byte; // ko
					           ^^^^^
				Type mismatch: cannot convert from byte to Short
				----------
				"""
        );
	}
    // autoboxing and type argument inference
    public void test087() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                """
					public class X {
					    <T> T foo(T t) { return t; }
					   \s
					    public static void main(String[] args) {
					        int i = new X().foo(12);
					        System.out.println(i);
					    }
					    Zork z;
					}
					"""
            },
			"""
				----------
				1. WARNING in X.java (at line 5)
					int i = new X().foo(12);
					        ^^^^^^^^^^^^^^^
				The expression of type Integer is unboxed into int
				----------
				2. WARNING in X.java (at line 5)
					int i = new X().foo(12);
					                    ^^
				The expression of type int is boxed into Integer
				----------
				3. ERROR in X.java (at line 8)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				"""
        );
    }
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=84480 - variation with autoboxing diagnosis on
	 */
	public void test088() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int f;
						void foo(int i) {
							i = i++;
							i = ++i;
							f = f++;
							f = ++f;
							Zork z;
						}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					i = ++i;
					^^^^^^^
				The assignment to variable i has no effect
				----------
				2. WARNING in X.java (at line 7)
					f = ++f;
					^^^^^^^
				The assignment to variable f has no effect
				----------
				3. ERROR in X.java (at line 8)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true,
			customOptions);
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84345
    public void test089() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
					  public Object foo() {
					  	byte b = 0;
						Number n = (Number) b;
					
					    java.io.Serializable o = null;
					    if (o == 0) return o;
					    return this;
					  }
					}
					"""
            },
			"""
				----------
				1. WARNING in X.java (at line 4)
					Number n = (Number) b;
					           ^^^^^^^^^^
				Unnecessary cast from byte to Number
				----------
				2. WARNING in X.java (at line 4)
					Number n = (Number) b;
					                    ^
				The expression of type byte is boxed into Byte
				----------
				3. ERROR in X.java (at line 7)
					if (o == 0) return o;
					    ^^^^^^
				Incompatible operand types Serializable and int
				----------
				"""
        );
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84345 - variation
    public void test090() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
					  public Object foo() {
					 \s
					  	Boolean b = null;
					     if (b == true) return b;
					     Object o = null;
					    if (o == true) return o;
					    return this;
					  }
					}
					"""
            },
			"""
				----------
				1. WARNING in X.java (at line 5)
					if (b == true) return b;
					    ^
				The expression of type Boolean is unboxed into boolean
				----------
				2. ERROR in X.java (at line 7)
					if (o == true) return o;
					    ^^^^^^^^^
				Incompatible operand types Object and boolean
				----------
				"""
        );
    }

    // type argument inference and autoboxing
    public void test091() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					public class X {
					
					    public static void main(String[] args) {
					        Comparable<?> c1 = foo("", new Integer(5));
					        Object o = foo("", 5);
					    }
					    public static <T> T foo(T t1, T t2) {\s
					    	System.out.print("foo("+t1.getClass().getSimpleName()+","+t2.getClass().getSimpleName()+")");
					    	return null;\s
					    }
					}
					"""
            },
			"foo(String,Integer)foo(String,Integer)"
        );
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84669
    public void test092() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					public class X
					{
						public X()
						{
							super();
						}
					
						public Object convert(Object value)
						{
							Double d = (Double)value;
							d = (d/100);
							return d;
						}
					
						public static void main(String[] args)
						{
							X test = new X();
							Object value = test.convert(new Double(50));
							System.out.println(value);
						}
					}
					"""
            },
			"0.5"
        );
    }

    public void test093() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer someInteger = 12;
							System.out.println((args == null ? someInteger : \'A\') == \'A\');
						}
					}
					"""
            },
			"true"
        );
    }

    public void test094() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer someInteger = 12;
							System.out.println((args == null ? someInteger : \'A\') == \'A\');
							Zork z;
						}
					}
					"""
            },
			"""
				----------
				1. WARNING in X.java (at line 3)
					Integer someInteger = 12;
					                      ^^
				The expression of type int is boxed into Integer
				----------
				2. WARNING in X.java (at line 4)
					System.out.println((args == null ? someInteger : \'A\') == \'A\');
					                                   ^^^^^^^^^^^
				The expression of type Integer is unboxed into int
				----------
				3. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				"""
        );
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80630
    public void test095() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					public class X {
						public static void main(String[] args) {
							boolean b = true;
							Character _Character = new Character(\' \');
							char c = \' \';
							Integer _Integer = new Integer(2);
							if ((b ? _Character : _Integer) == c) {
								System.out.println("SUCCESS");
							} else {
								System.out.println("FAILURE");
							}
						}
					}
					"""
            },
			"SUCCESS"
        );
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80630 - variation
    public void test096() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
						@SuppressWarnings("deprecation")
						public static void main(String[] args) {
							boolean b = true;
							Character _Character = Character.valueOf(\' \');
							char c = \' \';
							Integer _Integer = Integer.valueOf(2);
							if ((b ? _Character : _Integer) == c) {
								System.out.println(zork);
							} else {
								System.out.println("FAILURE");
							}
						}
					}
					"""
            },
			"""
				----------
				1. WARNING in X.java (at line 8)
					if ((b ? _Character : _Integer) == c) {
					         ^^^^^^^^^^
				The expression of type Character is unboxed into int
				----------
				2. WARNING in X.java (at line 8)
					if ((b ? _Character : _Integer) == c) {
					                      ^^^^^^^^
				The expression of type Integer is unboxed into int
				----------
				3. ERROR in X.java (at line 9)
					System.out.println(zork);
					                   ^^^^
				zork cannot be resolved to a variable
				----------
				"""
        );
    }
    // conditional operator: bool ? Integer : Integer --> Integer (identical operand types)
    // but   bool ? Integer : Short --> unboxed int
    public void test097() {
        this.runConformTest(
            new String[] {
                "X.java",
				"""
					public class X {
					    public static void main(String args[]) {
					        Integer i = 1;
					        Integer j = 2;
					        Short s = 3;
					        foo(args != null ? i : j);
					        foo(args != null ? i : s);
					    }
					    static void foo(int i) {
					        System.out.print("[int:"+i+"]");
					    }
					    static void foo(Integer i) {
					        System.out.print("[Integer:"+i+"]");
					    }
					}
					"""
            },
			"[Integer:1][int:1]"
        );
    }
    // conditional operator: bool ? Integer : Integer --> Integer (identical operand types)
    // but   bool ? Integer : Short --> unboxed int
    // check autoboxing warnings
    public void test098() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"""
					public class X {
					    public static void main(String args[]) {
					        Integer i = 1;
					        Integer j = 2;
					        Short s = 3;
					        foo(args != null ? i : j);
					        foo(args != null ? i : s);
							 Zork z;
					    }
					    static void foo(int i) {
					        System.out.print("[int:"+i+"]");
					    }
					    static void foo(Integer i) {
					        System.out.print("[Integer:"+i+"]");
					    }
					}
					"""
            },
			"""
				----------
				1. WARNING in X.java (at line 3)
					Integer i = 1;
					            ^
				The expression of type int is boxed into Integer
				----------
				2. WARNING in X.java (at line 4)
					Integer j = 2;
					            ^
				The expression of type int is boxed into Integer
				----------
				3. WARNING in X.java (at line 5)
					Short s = 3;
					          ^
				The expression of type int is boxed into Short
				----------
				4. WARNING in X.java (at line 7)
					foo(args != null ? i : s);
					                   ^
				The expression of type Integer is unboxed into int
				----------
				5. WARNING in X.java (at line 7)
					foo(args != null ? i : s);
					                       ^
				The expression of type Short is unboxed into int
				----------
				6. ERROR in X.java (at line 8)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				"""
        );
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801
	public void test099() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends A {
					    public void m(Object o) { System.out.println("SUCCESS"); }
					    public static void main(String[] args) { ((A) new X()).m(1); }
					}
					interface I { void m(Object o); }
					abstract class A implements I {
						public final void m(int i) {
							System.out.print("SUCCESS + ");
							m(new Integer(i));
						}
						public final void m(double d) {
							System.out.print("FAILED");
						}
					}
					"""
			},
			"SUCCESS + SUCCESS"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87267
	public void test100() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Integer[] integers = {};
							int[] ints = (int[]) integers;
							float[] floats = {};
							Float[] fs = (Float[]) floats;
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					int[] ints = (int[]) integers;
					             ^^^^^^^^^^^^^^^^
				Cannot cast from Integer[] to int[]
				----------
				2. ERROR in X.java (at line 6)
					Float[] fs = (Float[]) floats;
					             ^^^^^^^^^^^^^^^^
				Cannot cast from float[] to Float[]
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85491
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(Object... i) { System.out.print(1); }
						void foo(int... i) { System.out.print(2); }
						@SuppressWarnings("deprecation")
						public static void main(String[] args) {
							new X().foo(1);
							new X().foo(Integer.valueOf(1));
							new X().foo(1, Integer.valueOf(1));
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					new X().foo(1);
					        ^^^
				The method foo(Object[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 7)
					new X().foo(Integer.valueOf(1));
					        ^^^
				The method foo(Object[]) is ambiguous for the type X
				----------
				3. ERROR in X.java (at line 8)
					new X().foo(1, Integer.valueOf(1));
					        ^^^
				The method foo(Object[]) is ambiguous for the type X
				----------
				""");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(Number... i) { System.out.print(1); }
						void foo(int... i) { System.out.print(2); }
						@SuppressWarnings("deprecation")
						public static void main(String[] args) {
							new X().foo(1);
							new X().foo(Integer.valueOf(1));
							new X().foo(1, Integer.valueOf(1));
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					new X().foo(1);
					        ^^^
				The method foo(Number[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 7)
					new X().foo(Integer.valueOf(1));
					        ^^^
				The method foo(Number[]) is ambiguous for the type X
				----------
				3. ERROR in X.java (at line 8)
					new X().foo(1, Integer.valueOf(1));
					        ^^^
				The method foo(Number[]) is ambiguous for the type X
				----------
				"""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(int i, Object... o) { System.out.print(1); }
						void foo(Integer o, int... i) { System.out.print(2); }
						@SuppressWarnings("deprecation")
						public static void main(String[] args) {
							new X().foo(1);
							new X().foo(Integer.valueOf(1));
							new X().foo(1, Integer.valueOf(1));
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					new X().foo(1);
					        ^^^
				The method foo(int, Object[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 7)
					new X().foo(Integer.valueOf(1));
					        ^^^
				The method foo(int, Object[]) is ambiguous for the type X
				----------
				3. ERROR in X.java (at line 8)
					new X().foo(1, Integer.valueOf(1));
					        ^^^
				The method foo(int, Object[]) is ambiguous for the type X
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801
	public void test102() {
		runConformTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					class Cla<A> {
						A val;
						public Cla(A x) { val = x; }
						A getVal() { return val; }
					}
					
					public class X {
					\t
						void proc0(Cla<Long> b0) {
							final Long t1 = b0.getVal();
							System.out.print(t1);
							final long t2 = b0.getVal();
							System.out.print(t2);
						}
					
						void proc1(Cla<? extends Long> obj) {
							final Long t3 = obj.getVal();
							System.out.print(t3);
							final long t4 = obj.getVal();
							System.out.print(t4);
						}
					\t
						<U extends Long> void proc2(Cla<U> obj) {
							final Long t5 = obj.getVal();
							System.out.print(t5);
							final long t6 = obj.getVal();
							System.out.println(t6);
						}
					\t
						public static void main(String[] args) {
							X x = new X();
							x.proc0(new Cla<Long>(0l));
							x.proc1(new Cla<Long>(1l));
							x.proc2(new Cla<Long>(2l));
						}
					}
					"""
			},
			// compiler results
			null /* do not check compiler log */,
			// runtime results
			"001122" /* expected output string */,
			"" /* expected error string */,
			// javac options
			JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801 - variation (check warnings)
	public void test103() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class Cla<A> {
						Zork z;
						A val;
						public Cla(A x) { val = x; }
						A getVal() { return val; }
					}
					
					public class X {
					\t
						void proc0(Cla<Long> b0) {
							final Long t1 = b0.getVal();
							System.out.print(t1);
							final long t2 = b0.getVal();
							System.out.print(t2);
						}
					
						void proc1(Cla<? extends Long> obj) {
							final Long t3 = obj.getVal();
							System.out.print(t3);
							final long t4 = obj.getVal();
							System.out.print(t4);
						}
					\t
						<U extends Long> void proc2(Cla<U> obj) {
							final Long t5 = obj.getVal();
							System.out.print(t5);
							final long t6 = obj.getVal();
							System.out.printltn(t6);
						}
					\t
						public static void main(String[] args) {
							X x = new X();
							x.proc0(new Cla<Long>(0l));
							x.proc1(new Cla<Long>(1l));
							x.proc2(new Cla<Long>(2l));
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				2. WARNING in X.java (at line 13)
					final long t2 = b0.getVal();
					                ^^^^^^^^^^^
				The expression of type Long is unboxed into long
				----------
				3. WARNING in X.java (at line 20)
					final long t4 = obj.getVal();
					                ^^^^^^^^^^^^
				The expression of type capture#2-of ? extends Long is unboxed into long
				----------
				4. WARNING in X.java (at line 24)
					<U extends Long> void proc2(Cla<U> obj) {
					           ^^^^
				The type parameter U should not be bounded by the final type Long. Final types cannot be further extended
				----------
				5. WARNING in X.java (at line 27)
					final long t6 = obj.getVal();
					                ^^^^^^^^^^^^
				The expression of type U is unboxed into long
				----------
				6. ERROR in X.java (at line 28)
					System.out.printltn(t6);
					           ^^^^^^^^
				The method printltn(long) is undefined for the type PrintStream
				----------
				7. WARNING in X.java (at line 33)
					x.proc0(new Cla<Long>(0l));
					                      ^^
				The expression of type long is boxed into Long
				----------
				8. WARNING in X.java (at line 34)
					x.proc1(new Cla<Long>(1l));
					                      ^^
				The expression of type long is boxed into Long
				----------
				9. WARNING in X.java (at line 35)
					x.proc2(new Cla<Long>(2l));
					                      ^^
				The expression of type long is boxed into Long
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95868
	public void test104() {
		this.runConformTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacGeneratesIncorrectCode,
			new String[] {
				"X.java",
				"""
					import java.util.HashMap;
					
					public class X {
						public static void main(String[] args) {
							try {
								String x = "";
								HashMap<String, Integer> y = new HashMap<String, Integer>();
								Integer w = (x.equals("X") ? 0 : y.get("yKey"));
							} catch(NullPointerException e) {
								System.out.println("SUCCESS");
							}
						}
					}
					"""
			},
			"SUCCESS");
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779
public void test105() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				final class Pair<F, S> {
					public F first;
					public S second;
				
					public static <F, S> Pair<F, S> create(F f, S s) {
						return new Pair<F, S>(f, s);
					}
				
					public Pair(final F f, final S s) {
						first = f;
						second = s;
					}
				}
				
				public class X {
					public void a() {
						Pair<Integer, Integer> p = Pair.create(1, 3);
						// p.first -= 1; // should be rejected ?
						p.first--;
						--p.first;
						p.first = p.first - 1;
						System.out.println(p.first);
					}
				
					public static void main(final String[] args) {
						new X().a();
					}
				}
				""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"-2" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779 - variation
public void test106() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class XSuper<T> {
					T value;
				}
				public class X extends XSuper<Integer>{
					public void a() {
						value--;
						--value;
						value -= 1;
						value = value - 1;
						System.out.println(value);
					}
				
					public static void main(final String[] args) {
						X x = new X();
						x.value = 5;
						x.a();
					}
				}
				""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779 - variation
public void test107() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class XSuper<T> {
					T value;
				}
				public class X extends XSuper<Integer>{
					public void a() {
						this.value--;
						--this.value;
						this.value -= 1;
						this.value = this.value - 1;
						System.out.println(this.value);
					}
				
					public static void main(final String[] args) {
						X x = new X();
						x.value = 5;
						x.a();
					}
				}
				""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779 - variation
public void test108() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class XSuper<T> {
					T value;
				}
				public class X extends XSuper<Integer>{
					public static void a(X x) {
						x.value--;
						--x.value;
						x.value -= 1;
						x.value = x.value - 1;
						System.out.println(x.value);
					}
				
					public static void main(final String[] args) {
						X x = new X();
						x.value = 5;
						a(x);
					}
				}
				""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100043
public void test109() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int foo = 0;
						String bar = "zero";
						System.out.println((foo != 0) ? foo : bar);
					}
				}
				""",
		},
		"zero");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100043 - variation
public void test110() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String args[]) {
				    	if (new Boolean(true) ? true : new Boolean(false)) {
				    		System.out.print("SUCCESS");
				    	} else {
				    		System.out.print("FAILED");
				    	}
				    }
				}
				""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105524
public void test111() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				class Wrapper< T >
				{
				    public T value;
				}
				
				public class X
				{
				    public static void main( final String[ ] args )
				    {
				        final Wrapper< Integer > wrap = new Wrapper< Integer >( );
				        wrap.value = 0;
				        wrap.value = wrap.value + 1; // works
				        wrap.value++; // throws VerifyError
				        wrap.value += 1; // throws VerifyError
				    }
				}
				""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105284
public void test112() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Short s;
						s = 5;  // Type mismatch: cannot convert from int to Short
						Short[] shorts = { 0, 1, 2, 3 };
						System.out.println(s+shorts[2]);
					}
				}
				""",
		},
		"7");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105284 - variation
public void test113() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Short s;
						s = 5;  // Type mismatch: cannot convert from int to Short
				
						int i = 0;
						s = i; // not a constant
					\t
						bar(4);
						Short[] shorts = { 0, 1, 2, 3 };
					}
					void bar(Short s) {}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				s = 5;  // Type mismatch: cannot convert from int to Short
				    ^
			The expression of type int is boxed into Short
			----------
			2. ERROR in X.java (at line 7)
				s = i; // not a constant
				    ^
			Type mismatch: cannot convert from int to Short
			----------
			3. ERROR in X.java (at line 9)
				bar(4);
				^^^
			The method bar(Short) in the type X is not applicable for the arguments (int)
			----------
			4. WARNING in X.java (at line 10)
				Short[] shorts = { 0, 1, 2, 3 };
				                   ^
			The expression of type int is boxed into Short
			----------
			5. WARNING in X.java (at line 10)
				Short[] shorts = { 0, 1, 2, 3 };
				                      ^
			The expression of type int is boxed into Short
			----------
			6. WARNING in X.java (at line 10)
				Short[] shorts = { 0, 1, 2, 3 };
				                         ^
			The expression of type int is boxed into Short
			----------
			7. WARNING in X.java (at line 10)
				Short[] shorts = { 0, 1, 2, 3 };
				                            ^
			The expression of type int is boxed into Short
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100182
public void test114() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] s) {
						char c = \'a\';
						System.out.printf("%c",c);	\t
						System.out.printf("%d\\n",(int)c);	\t
					}
					Zork z;
				}
				""" ,
		},
		"""
			----------
			1. WARNING in X.java (at line 4)\r
				System.out.printf("%c",c);		\r
				                       ^
			The expression of type char is boxed into Character
			----------
			2. WARNING in X.java (at line 5)\r
				System.out.printf("%d\\n",(int)c);		\r
				                         ^^^^^^
			The expression of type int is boxed into Integer
			----------
			3. ERROR in X.java (at line 7)\r
				Zork z;\r
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100182 - variation
public void test115() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] s) {
						char c = \'a\';
						System.out.printf("%c",c);	\t
						System.out.printf("%d\\n",(int)c);	\t
					}
				}
				""",
		},
		"a97");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106870
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    boolean foo(Long l, Float f) {
				    	return f == l;
				    }
				    float bar(Long l, Float f) {
				    	return this == null ? f : l;
				    }
				    double baz(Long l, Float f) {
				    	return this == null ? f : l;
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)\r
				return f == l;\r
				       ^^^^^^
			Incompatible operand types Float and Long
			----------
			2. WARNING in X.java (at line 6)\r
				return this == null ? f : l;\r
				                      ^
			The expression of type Float is unboxed into float
			----------
			3. WARNING in X.java (at line 6)\r
				return this == null ? f : l;\r
				                          ^
			The expression of type Long is unboxed into float
			----------
			4. WARNING in X.java (at line 9)\r
				return this == null ? f : l;\r
				                      ^
			The expression of type Float is unboxed into float
			----------
			5. WARNING in X.java (at line 9)\r
				return this == null ? f : l;\r
				                          ^
			The expression of type Long is unboxed into float
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122987
public void test117() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args)
				    {
				        Object obj = true ? true : 17.3;
						 Zork z;
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				Object obj = true ? true : 17.3;
				                    ^^^^
			The expression of type boolean is boxed into Boolean
			----------
			2. WARNING in X.java (at line 4)
				Object obj = true ? true : 17.3;
				                           ^^^^
			The expression of type double is boxed into Double
			----------
			3. ERROR in X.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}

// Integer array and method with T extends Integer bound
public void test118() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				    public static <T extends Integer> void foo(final T[] p) {
				        System.out.println(p[0] / 4);
				    }
				    public static void main(final String[] args) {
				        X.foo(new Integer[] { 4, 8, 16 });
				    }
				}""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBug6575821 /* javac test options */);
}

// Integer as member of a parametrized class
public void test119() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X<T> {
				    T m;
				    X(T p) {
				        this.m = p;
				    }
				    public static void main(String[] args) {
				        X<Integer> l = new X<Integer>(0);
				        l.m++;
				        System.out.println(l.m);
				    }
				}""",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=137918
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int a = 100;
						boolean c = a instanceof Integer;
						Integer i = (Integer) a;
						System.out.println(c);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				boolean c = a instanceof Integer;
				            ^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types int and Integer
			----------
			2. WARNING in X.java (at line 5)
				Integer i = (Integer) a;
				            ^^^^^^^^^^^
			Unnecessary cast from int to Integer
			----------
			3. WARNING in X.java (at line 5)
				Integer i = (Integer) a;
				                      ^
			The expression of type int is boxed into Integer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156108
public void test121() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						final int i = -128;
						Byte b = i;
					}
					public static void main(String[] args) {
						Byte no = 127; // warning: int boxed to Byte > fine
						switch (no) { // warning: Byte is unboxed into int > why in int??? output
							case -128: // error: cannot convert int to Byte > needs a explicit (byte)cast.
								break;
							case (byte) 127: // works
								break;
						}
						no = new Byte(127);
					}
				}""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				Byte b = i;
				         ^
			The expression of type int is boxed into Byte
			----------
			2. WARNING in X.java (at line 7)
				Byte no = 127; // warning: int boxed to Byte > fine
				          ^^^
			The expression of type int is boxed into Byte
			----------
			3. WARNING in X.java (at line 8)
				switch (no) { // warning: Byte is unboxed into int > why in int??? output
				        ^^
			The expression of type Byte is unboxed into int
			----------
			4. ERROR in X.java (at line 14)
				no = new Byte(127);
				     ^^^^^^^^^^^^^
			The constructor Byte(int) is undefined
			----------
			"""
);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156108 - variation
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					Byte foo() {
						final int i = -128;
						return i;
					}
					Byte bar() {
						final int i = 1000;
						return i;
					}\t
				}""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				return i;
				       ^
			The expression of type int is boxed into Byte
			----------
			2. ERROR in X.java (at line 8)
				return i;
				       ^
			Type mismatch: cannot convert from int to Byte
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155255
public void test123() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						foo1();
						foo2();
						foo3();
						foo4();
						System.out.println("[done]");
					}
					static void foo1() {
						Object x = true ? true : "";
						System.out.print("[1:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
					static void foo2() {
						Object x = Boolean.TRUE != null ? true : "";
						System.out.print("[2:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
					static void foo3() {
						Object x = false ? "" : false;
						System.out.print("[3:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
					static void foo4() {
						Object x = Boolean.TRUE == null ? "" : false;
						System.out.print("[4:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
				}""", // =================
		},
		"[1:true,java.lang.Boolean][2:true,java.lang.Boolean][3:false,java.lang.Boolean][4:false,java.lang.Boolean][done]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155255 - variation
public void test124() {
	String specVersion = System.getProperty("java.specification.version");
	isJRE15Plus =  Integer.valueOf(specVersion) >= Integer.valueOf(CompilerOptions.VERSION_15);
	String bounds = isJRE15Plus ? "Object&Serializable&Comparable<?>&Constable" : "Object&Serializable&Comparable<?>";

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static void foo5() {
						boolean x = false ? "" : false;
						System.out.print("[4:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}\t
				}""", // =================
		},
		this.complianceLevel >= ClassFileConstants.JDK1_8 ?
				"""
					----------
					1. ERROR in X.java (at line 3)
						boolean x = false ? "" : false;
						                    ^^
					Type mismatch: cannot convert from String to boolean
					----------
					2. ERROR in X.java (at line 4)
						System.out.print("[4:"+ x + "," + x.getClass().getCanonicalName() + "]");
						                                  ^^^^^^^^^^^^
					Cannot invoke getClass() on the primitive type boolean
					----------
					""" :
						"----------\n" +
						"1. ERROR in X.java (at line 3)\n" +
						"	boolean x = false ? \"\" : false;\n" +
						"	            ^^^^^^^^^^^^^^^^^^\n" +
						"Type mismatch: cannot convert from "+ bounds +" to boolean\n" +
						"----------\n" +
						"2. WARNING in X.java (at line 3)\n" +
						"	boolean x = false ? \"\" : false;\n" +
						"	                         ^^^^^\n" +
						"The expression of type boolean is boxed into Boolean\n" +
						"----------\n" +
						"3. ERROR in X.java (at line 4)\n" +
						"	System.out.print(\"[4:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
						"	                                  ^^^^^^^^^^^^\n" +
						"Cannot invoke getClass() on the primitive type boolean\n" +
						"----------\n");
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155255 - variation
public void test125() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						foo1();
						foo2();
						foo3();
						System.out.println("[done]");
					}
					static void foo1() {
						Object x = true ? 3.0f : false;
						System.out.print("[1:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
					static void foo2() {
						Object x = true ? 2 : false;
						System.out.print("[2:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
					static void foo3() {
						Object x = false ? 2 : false;
						System.out.print("[3:"+ x + "," + x.getClass().getCanonicalName() + "]");
					}
				}
				""", // =================
		},
		"[1:3.0,java.lang.Float][2:2,java.lang.Integer][3:false,java.lang.Boolean][done]");
	}
public void test126() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(boolean b) {
						int i = 12;
						Integer r1 = b ? null : i;
						int r2 = b ? null : i;
					}
					Zork z;
				}
				""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				Integer r1 = b ? null : i;
				                        ^
			The expression of type int is boxed into Integer
			----------
			2. WARNING in X.java (at line 5)
				int r2 = b ? null : i;
				         ^^^^^^^^^^^^
			The expression of type Integer is unboxed into int
			----------
			3. WARNING in X.java (at line 5)
				int r2 = b ? null : i;
				                    ^
			The expression of type int is boxed into Integer
			----------
			4. ERROR in X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
public void test127() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] s) {
				                Object[] os1 = new Object[] {(long)1234567};
				                Object[] os2 = new Object[] {1234567};
				                Object o1 = os1[0], o2 = os2[0];
				                if (o1.getClass().equals(o2.getClass())) {
				                    System.out.println("FAILED:o1["+o1.getClass().getName()+"],o2:["+o2.getClass()+"]");
				                } else {
				                    System.out.println("SUCCESS:o1["+o1.getClass().getName()+"],o2:["+o2.getClass()+"]");
				                }
				        }
				}
				""", // =================
		},
		"SUCCESS:o1[java.lang.Long],o2:[class java.lang.Integer]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159987
public void test128() {
	// check there is no unncessary cast warning when autoboxing, even in array initializer
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] s) {
						Object o1 = (long) 1234567;
						Object[] os1 = new Object[] { (long) 1234567 };
						Object[] os2 = { (long) 1234567 };
						foo((long) 1234567);
					}
					static void foo(Object o) {
					}
					Zork z;
				}
				""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Object o1 = (long) 1234567;
				            ^^^^^^^^^^^^^^
			The expression of type long is boxed into Long
			----------
			2. WARNING in X.java (at line 4)
				Object[] os1 = new Object[] { (long) 1234567 };
				                              ^^^^^^^^^^^^^^
			The expression of type long is boxed into Long
			----------
			3. WARNING in X.java (at line 5)
				Object[] os2 = { (long) 1234567 };
				                 ^^^^^^^^^^^^^^
			The expression of type long is boxed into Long
			----------
			4. WARNING in X.java (at line 6)
				foo((long) 1234567);
				    ^^^^^^^^^^^^^^
			The expression of type long is boxed into Long
			----------
			5. ERROR in X.java (at line 10)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155104
public void test129() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X{
				   java.io.Serializable field=this==null?8:"".getBytes();
					Zork z;
				}
				""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 2)\r
				java.io.Serializable field=this==null?8:"".getBytes();\r
				                                      ^
			The expression of type int is boxed into Integer
			----------
			2. ERROR in X.java (at line 3)\r
				Zork z;\r
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test130() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					Boolean[] myBool = new Boolean[1];
					void foo() {
						if (this.myBool[0]) {}
					}
					public static void main(String[] args) {
						try {
							new X().foo();
							System.out.println("FAILURE");
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test131() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					Boolean myBool = null;
					void foo() {
						if (myBool) {}
					}
					public static void main(String[] args) {
						try {
							new X().foo();
							System.out.println("FAILURE");
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test132() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static Boolean myBool = null;
					static void foo() {
						if (myBool) {}
					}
					public static void main(String[] args) {
						try {
							foo();
							System.out.println("FAILURE");
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test133() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					Boolean myBool = null;
					void foo() {
						if (this.myBool) {}
					}
					public static void main(String[] args) {
						try {
							new X().foo();
							System.out.println("FAILURE");
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test134() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static Boolean MyBool = null;
					static void foo() {
						if (X.MyBool) {}
					}
					public static void main(String[] args) {
						try {
							foo();
							System.out.println("FAILURE");
						} catch(NullPointerException e) {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372
public void test135() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T foo() { return null; }
				}
				
				public class X {
				        public static void main(String[] args) {
				                A<Long> a = new A<Long>();
								 A ua = a;
				                try {
					                long s = a.foo();
				                } catch(NullPointerException e) {
				                	System.out.println("SUCCESS");
				                	return;
				                }
				            	System.out.println("FAILED");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test136() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T foo(Object o) {
				                return (T) o; // should get unchecked warning
				        }
				}
				
				public class X {
				        public static void main(String[] args) {
				                A<Long> a = new A<Long>();
				                try {
					                long s = a.foo(new Object());
				                } catch(ClassCastException e) {
				                	System.out.println("SUCCESS");
				                	return;
				                }
				            	System.out.println("FAILED");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test137() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T foo;
				}
				
				public class X {
				        public static void main(String[] args) {
				                A<Long> a = new A<Long>();
								 A ua = a;
								 ua.foo = new Object();
				                try {
					                long s = a.foo;
				                } catch(ClassCastException e) {
				                	System.out.println("SUCCESS");
				                	return;
				                }
				            	System.out.println("FAILED");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test138() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T foo;
				}
				
				public class X extends A<Long>{
				        public static void main(String[] args) {
							new X().foo();
						 }
				 		 public void foo() {
								 A ua = this;
								 ua.foo = new Object();
				                try {
					                long s = foo;
				                } catch(ClassCastException e) {
				                	System.out.println("SUCCESS");
				                	return;
				                }
				            	System.out.println("FAILED");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test139() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T foo;
				}
				
				public class X extends A<Long>{
				        public static void main(String[] args) {
							new X().foo();
						 }
				 		 public void foo() {
								 A ua = this;
								 ua.foo = new Object();
				                try {
					                long s = this.foo;
				                } catch(ClassCastException e) {
				                	System.out.println("SUCCESS");
				                	return;
				                }
				            	System.out.println("FAILED");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test140() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A {
				        long foo() {
				                return 0L;
				        }
				}
				
				public class X {
				        public static void main(String[] args) {
				                A a = new A();
					             Long s = a.foo();
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test141() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A {
				        long foo = 0L;
				}
				
				public class X {
				        public static void main(String[] args) {
				                A a = new A();
					             Long s = a.foo;
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test142() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A {
				        long foo = 0L;
				}
				
				public class X extends A {
				        public static void main(String[] args) {
							new X().bar();
				        }
						void bar() {
					             Long s = foo;
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test143() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A {
				        long foo = 0L;
				}
				
				public class X extends A {
				        public static void main(String[] args) {
							new X().bar();
				        }
						void bar() {
					             Long s = this.foo;
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test144() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T[] foo;
				}
				
				public class X extends A<Long>{
				        public static void main(String[] args) {
							new X().foo();
						 }
				 		 public void foo() {
								 A ua = this;
								 ua.foo = new Object[1];
				                try {
					                long s = this.foo[0];
				                } catch(ClassCastException e) {
				                	System.out.println("SUCCESS");
				                	return;
				                }
				            	System.out.println("FAILED");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test145() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A {
				        long[] foo = { 0L };
				}
				
				public class X extends A {
				        public static void main(String[] args) {
							new X().bar();
				        }
						void bar() {
					             Long s = this.foo[0];
				                System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test146() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
				        public T foo;
				}
				
				public class X {
				        public static void main(String[] args) {
				            A<Long> a = new A<Long>();
					         long s = a.foo.MAX_VALUE;
				            System.out.println("SUCCESS");
				        }
				}
				""", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test147() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if(new Integer(2) == 0) {}
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test148() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Z test = new Z(1, 1);
						System.out.println("SUCCESS" + test.foo());
					}
				}""",
			"Z.java",
			"""
				class Z {
					public <A, B extends A> Z(A a, B b) {
					}
					public int foo() {
						return 0;
					}
				}"""
		},
		"SUCCESS0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test149() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Z test = new Z(1, 1);
						System.out.println("SUCCESS");
					}
				}""",
			"Z.java",
			"""
				class Z {
					public <A, B extends A> Z(A a, B b) {
					}
				}"""
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test150() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if(new Integer(2) == 0) {
							System.out.println("FAILED");
						} else {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test151() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if(new Double(2.0) == 0.0) {
							System.out.println("FAILED");
						} else {
							System.out.println("SUCCESS");
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test152() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if(new Double(2.0) == 0.0) {}
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223685
public void test153() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						Integer a = 0;
						char b = (char)((int)a);
						char c = (char)(a + 1);
						char d = (char)(a);
						int e = (int) a;
						Integer f = (Integer) e;
					}
					void bar() {
						X x = (X) null;
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Integer a = 0;
				            ^
			The expression of type int is boxed into Integer
			----------
			2. WARNING in X.java (at line 4)
				char b = (char)((int)a);
				                     ^
			The expression of type Integer is unboxed into int
			----------
			3. WARNING in X.java (at line 5)
				char c = (char)(a + 1);
				                ^
			The expression of type Integer is unboxed into int
			----------
			4. ERROR in X.java (at line 6)
				char d = (char)(a);
				         ^^^^^^^^^
			Cannot cast from Integer to char
			----------
			5. WARNING in X.java (at line 7)
				int e = (int) a;
				        ^^^^^^^
			Unnecessary cast from Integer to int
			----------
			6. WARNING in X.java (at line 7)
				int e = (int) a;
				              ^
			The expression of type Integer is unboxed into int
			----------
			7. WARNING in X.java (at line 8)
				Integer f = (Integer) e;
				            ^^^^^^^^^^^
			Unnecessary cast from int to Integer
			----------
			8. WARNING in X.java (at line 8)
				Integer f = (Integer) e;
				                      ^
			The expression of type int is boxed into Integer
			----------
			9. WARNING in X.java (at line 11)
				X x = (X) null;
				      ^^^^^^^^
			Unnecessary cast from null to X
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565
public void test154() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				        T counter;
				        public static void main(String[] args) {
				        	 bar(new X<Integer>());
				        	 new Y().foo();
				        	 new Y().baz();
				        }
				        static void bar(X<Integer> x) {
				        	x.counter = 0;
				            System.out.print(Integer.toString(x.counter++));
				        }
				}
				
				class Y extends X<Integer> {
					Y() {
						this.counter = 0;
					}
				    void foo() {
				        System.out.print(Integer.toString(counter++));
				    }
				    void baz() {
				        System.out.println(Integer.toString(this.counter++));
				    }
				}
				""",
		},
		"000");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test155() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				        T[] counter;
				        public static void main(String[] args) {
				        	 bar(new X<Integer>());
				        	 new Y().foo();
				        	 new Y().baz();
				        }
				        static void bar(X<Integer> x) {
				        	x.counter = new Integer[]{ 0 };
				            System.out.print(Integer.toString(x.counter[0]++));
				        }
				}
				
				class Y extends X<Integer> {
					Y() {
						this.counter =  new Integer[]{ 0 };
					}
				    void foo() {
				        System.out.print(Integer.toString(counter[0]++));
				    }
				    void baz() {
				        System.out.println(Integer.toString(this.counter[0]++));
				    }
				}
				""",
		},
		"000");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test156() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static void print(Character c) {
						System.out.print((char) c);
					}
					public static void main(String[] args) {
						char c = \'H\';
						print(c++);
						print(c++);
						System.out.println("done");
				    }
				}
				""",
		},
		"HIdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test157() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					char c = \'H\';
					static void print(Character c) {
						System.out.print((char) c);
					}
					public static void main(String[] args) {
						X x = new X();
						print(x.c++);
						print(x.c++);
						System.out.println("done");
				    }
				}
				""",
		},
		"HIdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test158() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static X singleton = new X();
					static X singleton() { return singleton; }
					char c = \'H\';
				\t
					static void print(Character c) {
						System.out.print((char) c);
					}
					public static void main(String[] args) {
						print(singleton().c++);
						print(singleton().c++);
						System.out.println("done");
				    }
				}
				""",
		},
		"HIdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236019
public void test159() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X {
				    ArrayList params;
				    public int getSqlParamCount() {
				        return params == null ? null:params.size();
				    }
				    public int getSqlParamCount2() {
				        return null;
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				ArrayList params;
				^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			2. WARNING in X.java (at line 5)
				return params == null ? null:params.size();
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The expression of type Integer is unboxed into int
			----------
			3. WARNING in X.java (at line 5)
				return params == null ? null:params.size();
				                             ^^^^^^^^^^^^^
			The expression of type int is boxed into Integer
			----------
			4. ERROR in X.java (at line 8)
				return null;
				       ^^^^
			Type mismatch: cannot convert from null to int
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test160() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				        T counter;
				        public static void main(String[] args) {
				        	 bar(new X<Integer>());
				        	 new Y().foo();
				        	 new Y().baz();
				        }
				        static void bar(X<Integer> x) {
				        	x.counter = 0;
				            System.out.print(Integer.toString(++x.counter));
				        }
				}
				
				class Y extends X<Integer> {
					Y() {
						this.counter = 0;
					}
				    void foo() {
				        System.out.print(Integer.toString(++counter));
				    }
				    void baz() {
				        System.out.println(Integer.toString(++this.counter));
				    }
				}
				""",
		},
		"111");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test161() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				        T[] counter;
				        public static void main(String[] args) {
				        	 bar(new X<Integer>());
				        	 new Y().foo();
				        	 new Y().baz();
				        }
				        static void bar(X<Integer> x) {
				        	x.counter = new Integer[]{ 0 };
				            System.out.print(Integer.toString(++x.counter[0]));
				        }
				}
				
				class Y extends X<Integer> {
					Y() {
						this.counter =  new Integer[]{ 0 };
					}
				    void foo() {
				        System.out.print(Integer.toString(++counter[0]));
				    }
				    void baz() {
				        System.out.println(Integer.toString(++this.counter[0]));
				    }
				}
				""",
		},
		"111");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test162() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static void print(Character c) {
						System.out.print((char) c);
					}
					public static void main(String[] args) {
						char c = \'H\';
						print(++c);
						print(++c);
						System.out.println("done");
				    }
				}
				""",
		},
		"IJdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test163() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					char c = \'H\';
					static void print(Character c) {
						System.out.print((char) c);
					}
					public static void main(String[] args) {
						X x = new X();
						print(++x.c);
						print(++x.c);
						System.out.println("done");
				    }
				}
				""",
		},
		"IJdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test164() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static X singleton = new X();
					static X singleton() { return singleton; }
					char c = \'H\';
				\t
					static void print(Character c) {
						System.out.print((char) c);
					}
					public static void main(String[] args) {
						print(++singleton().c);
						print(++singleton().c);
						System.out.println("done");
				    }
				}
				""",
		},
		"IJdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=231709
public void test165() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void foo() {
				        Integer i1 = 10 ;
				        final short s = 100;
				        i1 = s;
				        switch (i1)
				        {
				            case s:
				        }
				    }
				    public void bar() {
				        Integer i2 = 10 ;
				        final byte b = 100;
				        i2 = b;
				        switch (i2)
				        {
				            case b:
				        }
				    }  \s
				    public void baz() {
				        Integer i3 = 10 ;
				        final char c = 100;
				        i3 = c;
				        switch (i3)
				        {
				            case c:
				        }
				    }    \s
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Integer i1 = 10 ;
				             ^^
			The expression of type int is boxed into Integer
			----------
			2. ERROR in X.java (at line 5)
				i1 = s;
				     ^
			Type mismatch: cannot convert from short to Integer
			----------
			3. WARNING in X.java (at line 6)
				switch (i1)
				        ^^
			The expression of type Integer is unboxed into int
			----------
			4. ERROR in X.java (at line 8)
				case s:
				     ^
			Type mismatch: cannot convert from short to Integer
			----------
			5. WARNING in X.java (at line 12)
				Integer i2 = 10 ;
				             ^^
			The expression of type int is boxed into Integer
			----------
			6. ERROR in X.java (at line 14)
				i2 = b;
				     ^
			Type mismatch: cannot convert from byte to Integer
			----------
			7. WARNING in X.java (at line 15)
				switch (i2)
				        ^^
			The expression of type Integer is unboxed into int
			----------
			8. ERROR in X.java (at line 17)
				case b:
				     ^
			Type mismatch: cannot convert from byte to Integer
			----------
			9. WARNING in X.java (at line 21)
				Integer i3 = 10 ;
				             ^^
			The expression of type int is boxed into Integer
			----------
			10. ERROR in X.java (at line 23)
				i3 = c;
				     ^
			Type mismatch: cannot convert from char to Integer
			----------
			11. WARNING in X.java (at line 24)
				switch (i3)
				        ^^
			The expression of type Integer is unboxed into int
			----------
			12. ERROR in X.java (at line 26)
				case c:
				     ^
			Type mismatch: cannot convert from char to Integer
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=231709 - variation
public void test166() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(short s, byte b, char c) {
						Integer is = s;
						Integer ib = b;
						Integer ic = c;\t
					}
					void foo() {
						final short s = 0;
						final byte b = 0;
						final char c = 0;
						Integer is = s;
						Integer ib = b;
						Integer ic = c;\t
					}
					void foo2() {
						Integer is = (short)0;
						Integer ib = (byte)0;
						Integer ic = (char)0;\t
					}
					void foo3() {
						Short si = 0;
						Byte bi = 0;
						Character ci = 0;
					}
					void foo4() {
						Short si = (byte) 0;
						Byte bi = (short) 0;
						Character ci = (short) 0;
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Integer is = s;
				             ^
			Type mismatch: cannot convert from short to Integer
			----------
			2. ERROR in X.java (at line 4)
				Integer ib = b;
				             ^
			Type mismatch: cannot convert from byte to Integer
			----------
			3. ERROR in X.java (at line 5)
				Integer ic = c;\t
				             ^
			Type mismatch: cannot convert from char to Integer
			----------
			4. ERROR in X.java (at line 11)
				Integer is = s;
				             ^
			Type mismatch: cannot convert from short to Integer
			----------
			5. ERROR in X.java (at line 12)
				Integer ib = b;
				             ^
			Type mismatch: cannot convert from byte to Integer
			----------
			6. ERROR in X.java (at line 13)
				Integer ic = c;\t
				             ^
			Type mismatch: cannot convert from char to Integer
			----------
			7. ERROR in X.java (at line 16)
				Integer is = (short)0;
				             ^^^^^^^^
			Type mismatch: cannot convert from short to Integer
			----------
			8. ERROR in X.java (at line 17)
				Integer ib = (byte)0;
				             ^^^^^^^
			Type mismatch: cannot convert from byte to Integer
			----------
			9. ERROR in X.java (at line 18)
				Integer ic = (char)0;\t
				             ^^^^^^^
			Type mismatch: cannot convert from char to Integer
			----------
			10. WARNING in X.java (at line 21)
				Short si = 0;
				           ^
			The expression of type int is boxed into Short
			----------
			11. WARNING in X.java (at line 22)
				Byte bi = 0;
				          ^
			The expression of type int is boxed into Byte
			----------
			12. WARNING in X.java (at line 23)
				Character ci = 0;
				               ^
			The expression of type int is boxed into Character
			----------
			13. WARNING in X.java (at line 26)
				Short si = (byte) 0;
				           ^^^^^^^^
			The expression of type byte is boxed into Short
			----------
			14. WARNING in X.java (at line 27)
				Byte bi = (short) 0;
				          ^^^^^^^^^
			The expression of type short is boxed into Byte
			----------
			15. WARNING in X.java (at line 28)
				Character ci = (short) 0;
				               ^^^^^^^^^
			The expression of type short is boxed into Character
			----------
			""");
}
public void test167() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				     String foo(Comparable<String> x) {
				       System.out.println( "one" );\
						return null;
				     }
				     void foo(int x) {
				       System.out.println( "two" );
				     }
					void bar() {
				       Integer i = 1;
				       String s = foo(i);\s
				     }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				Integer i = 1;
				            ^
			The expression of type int is boxed into Integer
			----------
			2. ERROR in X.java (at line 10)
				String s = foo(i);\s
				           ^^^^^^
			Type mismatch: cannot convert from void to String
			----------
			3. WARNING in X.java (at line 10)
				String s = foo(i);\s
				               ^
			The expression of type Integer is unboxed into int
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264843
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				@SuppressWarnings("deprecation")
				public class X {
				    <T extends Integer> T a() { return 35; }
				    <T extends Integer> T[] b() { return new int[]{35}; }
				    <T extends Integer> T c() { return Integer.valueOf(35); }
				    <T extends Integer> T[] d() { return new Integer[]{35}; }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				<T extends Integer> T a() { return 35; }
				           ^^^^^^^
			The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
			----------
			2. ERROR in X.java (at line 3)
				<T extends Integer> T a() { return 35; }
				                                   ^^
			Type mismatch: cannot convert from int to T
			----------
			3. WARNING in X.java (at line 4)
				<T extends Integer> T[] b() { return new int[]{35}; }
				           ^^^^^^^
			The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
			----------
			4. ERROR in X.java (at line 4)
				<T extends Integer> T[] b() { return new int[]{35}; }
				                                     ^^^^^^^^^^^^^
			Type mismatch: cannot convert from int[] to T[]
			----------
			5. WARNING in X.java (at line 5)
				<T extends Integer> T c() { return Integer.valueOf(35); }
				           ^^^^^^^
			The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
			----------
			6. ERROR in X.java (at line 5)
				<T extends Integer> T c() { return Integer.valueOf(35); }
				                                   ^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Integer to T
			----------
			7. WARNING in X.java (at line 6)
				<T extends Integer> T[] d() { return new Integer[]{35}; }
				           ^^^^^^^
			The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
			----------
			8. ERROR in X.java (at line 6)
				<T extends Integer> T[] d() { return new Integer[]{35}; }
				                                     ^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Integer[] to T[]
			----------
			9. WARNING in X.java (at line 6)
				<T extends Integer> T[] d() { return new Integer[]{35}; }
				                                                   ^^
			The expression of type int is boxed into Integer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264843
public void test169() {
	String expectedCompilerLog = this.complianceLevel >= ClassFileConstants.JDK21 ?
			"""
				----------
				1. WARNING in X.java (at line 1)
					public class X<T extends Integer> {
					                         ^^^^^^^
				The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
				----------
				2. ERROR in X.java (at line 2)
					T x = 12;
					      ^^
				Type mismatch: cannot convert from int to T
				----------
				3. WARNING in X.java (at line 3)
					Byte y = 12;
					         ^^
				The expression of type int is boxed into Byte
				----------
				4. ERROR in X.java (at line 5)
					t = 5;
					    ^
				Type mismatch: cannot convert from int to T
				----------
				5. WARNING in X.java (at line 6)
					switch (t) {
					        ^
				The expression of type T is unboxed into int
				----------
				6. ERROR in X.java (at line 6)
					switch (t) {
					        ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				7. ERROR in X.java (at line 7)
					case 1:
					     ^
				Type mismatch: cannot convert from int to T
				----------
				8. WARNING in X.java (at line 12)
					t = 5;
					    ^
				The expression of type int is boxed into Byte
				----------
				9. WARNING in X.java (at line 13)
					switch (t) {
					        ^
				The expression of type Byte is unboxed into int
				----------
				"""
	  :
			"""
				----------
				1. WARNING in X.java (at line 1)
					public class X<T extends Integer> {
					                         ^^^^^^^
				The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended
				----------
				2. ERROR in X.java (at line 2)
					T x = 12;
					      ^^
				Type mismatch: cannot convert from int to T
				----------
				3. WARNING in X.java (at line 3)
					Byte y = 12;
					         ^^
				The expression of type int is boxed into Byte
				----------
				4. ERROR in X.java (at line 5)
					t = 5;
					    ^
				Type mismatch: cannot convert from int to T
				----------
				5. WARNING in X.java (at line 6)
					switch (t) {
					        ^
				The expression of type T is unboxed into int
				----------
				6. ERROR in X.java (at line 7)
					case 1:
					     ^
				Type mismatch: cannot convert from int to T
				----------
				7. WARNING in X.java (at line 12)
					t = 5;
					    ^
				The expression of type int is boxed into Byte
				----------
				8. WARNING in X.java (at line 13)
					switch (t) {
					        ^
				The expression of type Byte is unboxed into int
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T extends Integer> {
				    T x = 12;
				    Byte y = 12;
				    	void x(T t) {
				    		t = 5;
				    		switch (t) {
				    		case 1:
				    			break;
				    		}
				    	}
				    	void y(Byte t) {
				    		t = 5;
				    		switch (t) {
				    		case 1:
				    			break;
				    		}
				    	}
				}
				""",
		},
		expectedCompilerLog);
}
}
