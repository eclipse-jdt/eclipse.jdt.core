/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
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
 *								Bug 423505 - [1.8] Implement "18.5.4 More Specific Method Inference"
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VarargsTest extends AbstractComparableTest {

	public VarargsTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test068" };
//		TESTS_NUMBERS = new int[] { 61 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return VarargsTest.class;
	}
	@Override
	protected String intersection(String... types) {
		if (this.complianceLevel >= ClassFileConstants.JDK1_8)
			return String.join(" & ", types);
		return String.join("&", types);
	}
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y y = new Y();
							y = new Y(null);
							y = new Y(1);
							y = new Y(1, 2, (byte) 3, 4);
							y = new Y(new int[] {1, 2, 3, 4 });
						\t
							Y.count();
							Y.count(null);
							Y.count(1);
							Y.count(1, 2, (byte) 3, 4);
							Y.count(new int[] {1, 2, 3, 4 });
							System.out.print('>');
						}
					}
					""",
				"Y.java",
				"""
					public class Y {
						public Y(int ... values) {
							int result = 0;
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += values[i];
							System.out.print(result);
							System.out.print(' ');
						}
						public static void count(int ... values) {
							int result = 0;
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += values[i];
							System.out.print(result);
							System.out.print(' ');
						}
					}
					""",
			},
			"<0 0 1 10 10 0 0 1 10 10 >");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y y = new Y();
							y = new Y(null);
							y = new Y(1);
							y = new Y(1, 2, (byte) 3, 4);
							y = new Y(new int[] {1, 2, 3, 4 });
						\t
							Y.count();
							Y.count(null);
							Y.count(1);
							Y.count(1, 2, (byte) 3, 4);
							Y.count(new int[] {1, 2, 3, 4 });
							System.out.print('>');
						}
					}
					""",
			},
			"<0 0 1 10 10 0 0 1 10 10 >",
			null,
			false,
			null);
	}

	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y y = new Y();
							y = new Y(null);
							y = new Y(1);
							y = new Y(1, 2, (byte) 3, 4);
							y = new Y(new int[] {1, 2, 3, 4 });
							System.out.print('>');
						}
					}
					""",
				"Y.java",
				"""
					public class Y extends Z {
						public Y(int ... values) { super(values); }
					}
					class Z {
						public Z(int ... values) {
							int result = 0;
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += values[i];
							System.out.print(result);
							System.out.print(' ');
						}
					}
					""",
			},
			"<0 0 1 10 10 >");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y y = new Y();
							y = new Y(null);
							y = new Y(1);
							y = new Y(1, 2, (byte) 3, 4);
							y = new Y(new int[] {1, 2, 3, 4 });
							System.out.print('>');
						}
					}
					""",
			},
			"<0 0 1 10 10 >",
			null,
			false,
			null);
	}

	public void test003() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count();
							Y.count((int[]) null);
							Y.count((int[][]) null);
							Y.count(new int[] {1});
							Y.count(new int[] {1, 2}, new int[] {3, 4});
							Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});
							System.out.print('>');
						}
					}
					""",
				"Y.java",
				"""
					public class Y {
						public static int count(int[] values) {
							int result = 0;
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += values[i];
							System.out.print(' ');
							System.out.print(result);
							return result;
						}
						public static void count(int[] ... values) {
							int result = 0;
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += count(values[i]);
							System.out.print('=');
							System.out.print(result);
						}
					}
					""",
			},
			"<=0 0=0 1 3 7=10 6 4=10>");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count();
							Y.count((int[]) null);
							Y.count((int[][]) null);
							Y.count(new int[] {1});
							Y.count(new int[] {1, 2}, new int[] {3, 4});
							Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});
							System.out.print('>');
						}
					}
					"""
			},
			"<=0 0=0 1 3 7=10 6 4=10>",
			null,
			false,
			null);
	}

	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count(0);
							Y.count(-1, (int[]) null);
							Y.count(-2, (int[][]) null);
							Y.count(1);
							Y.count(2, new int[] {1});
							Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});
							Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});
							System.out.print('>');
						}
					}
					""",
				"Y.java",
				"""
					public class Y {
						public static int count(int j, int[] values) {
							int result = j;
							System.out.print(' ');
							System.out.print('[');
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += values[i];
							System.out.print(result);
							System.out.print(']');
							return result;
						}
						public static void count(int j, int[] ... values) {
							int result = j;
							System.out.print(' ');
							System.out.print(result);
							System.out.print(':');
							for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)
								result += count(j, values[i]);
							System.out.print('=');
							System.out.print(result);
						}
					}
					""",
			},
			"< 0:=0 [-1] -2:=-2 1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26>");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count(0);
							Y.count(-1, (int[]) null);
							Y.count(-2, (int[][]) null);
							Y.count(1);
							Y.count(2, new int[] {1});
							Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});
							Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});
							System.out.print('>');
						}
					}
					"""
			},
			"< 0:=0 [-1] -2:=-2 1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26>",
			null,
			false,
			null);
	}

	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.print();
							Y.print(Integer.valueOf(1));
							Y.print(Integer.valueOf(1), Byte.valueOf((byte) 3), Integer.valueOf(7));
							Y.print(new Integer[] {Integer.valueOf(11) });
							System.out.print('>');
						}
					}
					""",
				"Y.java",
				"""
					public class Y {
						public static void print(Number ... values) {
							for (int i = 0, l = values.length; i < l; i++) {
								System.out.print(' ');
								System.out.print(values[i]);
							}
							System.out.print(',');
						}
					}
					""",
			},
			"<, 1, 1 3 7, 11,>");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.print();
							Y.print(Integer.valueOf(1));
							Y.print(Integer.valueOf(1), Byte.valueOf((byte) 3), Integer.valueOf(7));
							Y.print(new Integer[] {Integer.valueOf(11) });
							System.out.print('>');
						}
					}
					""",
			},
			"<, 1, 1 3 7, 11,>",
			null,
			false,
			null);
	}

	public void test006() { // 70056
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							String[] T_NAMES = new String[] {"foo"};
							String error = "error";
							Y.format("E_UNSUPPORTED_CONV", Integer.valueOf(0));
							Y.format("E_SAVE", T_NAMES[0], error);
						}
					}
					class Y {
						public static String format(String key) { return null; }
						public static String format(String key, Object ... args) { return null; }
					}
					""",
			},
			"");
	}

	public void test007() { // array dimension test compatibility with Object
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.byte2(null);
							Y.byte2((byte) 1);
							Y.byte2(new byte[] {});
							Y.byte2(new byte[][] {});
							Y.byte2(new byte[][][] {});
					
							Y.object(null);
							Y.object((byte) 1);
							Y.object(new byte[] {});
							Y.object(new byte[][] {});
							Y.object(new byte[][][] {});
					
							Y.object(new String());
							Y.object(new String[] {});
							Y.object(new String[][] {});
					
							Y.object2(null);
							Y.object2((byte) 1);
							Y.object2(new byte[] {});
							Y.object2(new byte[][] {});
							Y.object2(new byte[][][] {});
					
							Y.object2(new String());
							Y.object2(new String[] {});
							Y.object2(new String[][] {});
					
							Y.string(null);
							Y.string(new String());
							Y.string(new String[] {});
							Y.string(new String[][] {});
					
							Y.string(new Object());
							Y.string(new Object[] {});
							Y.string(new Object[][] {});
						}
					}
					class Y {
						public static void byte2(byte[] ... values) {}
						public static void object(Object ... values) {}
						public static void object2(Object[] ... values) {}
						public static void string(String ... values) {}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Y.byte2(null);
					^^^^^^^^^^^^^
				Type null of the last argument to method byte2(byte[]...) doesn't exactly match the vararg parameter type. Cast to byte[][] to confirm the non-varargs invocation, or pass individual arguments of type byte[] for a varargs invocation.
				----------
				2. ERROR in X.java (at line 4)
					Y.byte2((byte) 1);
					  ^^^^^
				The method byte2(byte[]...) in the type Y is not applicable for the arguments (byte)
				----------
				3. ERROR in X.java (at line 7)
					Y.byte2(new byte[][][] {});
					  ^^^^^
				The method byte2(byte[]...) in the type Y is not applicable for the arguments (byte[][][])
				----------
				4. WARNING in X.java (at line 9)
					Y.object(null);
					^^^^^^^^^^^^^^
				Type null of the last argument to method object(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				5. WARNING in X.java (at line 12)
					Y.object(new byte[][] {});
					^^^^^^^^^^^^^^^^^^^^^^^^^
				Type byte[][] of the last argument to method object(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				6. WARNING in X.java (at line 13)
					Y.object(new byte[][][] {});
					^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type byte[][][] of the last argument to method object(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				7. WARNING in X.java (at line 16)
					Y.object(new String[] {});
					^^^^^^^^^^^^^^^^^^^^^^^^^
				Type String[] of the last argument to method object(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				8. WARNING in X.java (at line 17)
					Y.object(new String[][] {});
					^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type String[][] of the last argument to method object(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				9. WARNING in X.java (at line 19)
					Y.object2(null);
					^^^^^^^^^^^^^^^
				Type null of the last argument to method object2(Object[]...) doesn't exactly match the vararg parameter type. Cast to Object[][] to confirm the non-varargs invocation, or pass individual arguments of type Object[] for a varargs invocation.
				----------
				10. ERROR in X.java (at line 20)
					Y.object2((byte) 1);
					  ^^^^^^^
				The method object2(Object[]...) in the type Y is not applicable for the arguments (byte)
				----------
				11. ERROR in X.java (at line 21)
					Y.object2(new byte[] {});
					  ^^^^^^^
				The method object2(Object[]...) in the type Y is not applicable for the arguments (byte[])
				----------
				12. WARNING in X.java (at line 23)
					Y.object2(new byte[][][] {});
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type byte[][][] of the last argument to method object2(Object[]...) doesn't exactly match the vararg parameter type. Cast to Object[][] to confirm the non-varargs invocation, or pass individual arguments of type Object[] for a varargs invocation.
				----------
				13. ERROR in X.java (at line 25)
					Y.object2(new String());
					  ^^^^^^^
				The method object2(Object[]...) in the type Y is not applicable for the arguments (String)
				----------
				14. WARNING in X.java (at line 27)
					Y.object2(new String[][] {});
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type String[][] of the last argument to method object2(Object[]...) doesn't exactly match the vararg parameter type. Cast to Object[][] to confirm the non-varargs invocation, or pass individual arguments of type Object[] for a varargs invocation.
				----------
				15. WARNING in X.java (at line 29)
					Y.string(null);
					^^^^^^^^^^^^^^
				Type null of the last argument to method string(String...) doesn't exactly match the vararg parameter type. Cast to String[] to confirm the non-varargs invocation, or pass individual arguments of type String for a varargs invocation.
				----------
				16. ERROR in X.java (at line 32)
					Y.string(new String[][] {});
					  ^^^^^^
				The method string(String...) in the type Y is not applicable for the arguments (String[][])
				----------
				17. ERROR in X.java (at line 34)
					Y.string(new Object());
					  ^^^^^^
				The method string(String...) in the type Y is not applicable for the arguments (Object)
				----------
				18. ERROR in X.java (at line 35)
					Y.string(new Object[] {});
					  ^^^^^^
				The method string(String...) in the type Y is not applicable for the arguments (Object[])
				----------
				19. ERROR in X.java (at line 36)
					Y.string(new Object[][] {});
					  ^^^^^^
				The method string(String...) in the type Y is not applicable for the arguments (Object[][])
				----------
				""");
	}

	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y y = new Y(null);
							y = new Y(true, null);
							y = new Y('i', null);
						}
					}
					class Y {
						public Y(int ... values) {}
						public Y(boolean b, Object ... values) {}
						public Y(char c, int[] ... values) {}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					y = new Y(true, null);
					    ^^^^^^^^^^^^^^^^^
				Type null of the last argument to constructor Y(boolean, Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				2. WARNING in X.java (at line 5)
					y = new Y(\'i\', null);
					    ^^^^^^^^^^^^^^^^
				Type null of the last argument to constructor Y(char, int[]...) doesn't exactly match the vararg parameter type. Cast to int[][] to confirm the non-varargs invocation, or pass individual arguments of type int[] for a varargs invocation.
				----------
				""");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y y = new Y(null);
							y = new Y(true, null);
							y = new Y('i', null);
						}
					}
					class Y extends Z {
						public Y(int ... values) { super(values); }
						public Y(boolean b, Object ... values) { super(b, values); }
						public Y(char c, int[] ... values) {}
					}
					class Z {
						public Z(int ... values) {}
						public Z(boolean b, Object ... values) {}
						public Z(char c, int[] ... values) {}
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					y = new Y(true, null);
					    ^^^^^^^^^^^^^^^^^
				Type null of the last argument to constructor Y(boolean, Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				2. WARNING in X.java (at line 5)
					y = new Y(\'i\', null);
					    ^^^^^^^^^^^^^^^^
				Type null of the last argument to constructor Y(char, int[]...) doesn't exactly match the vararg parameter type. Cast to int[][] to confirm the non-varargs invocation, or pass individual arguments of type int[] for a varargs invocation.
				----------
				""");
	}

	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count(null);
							Y.count(1);
							Y.count(1, 2);
					
							Z.count(1L, 1);
							Z.count(1, 1);
							Z.count(1, null);
							Z.count2(1, null);
							Z.count2(1L, null);
							System.out.print('>');
						}
					}
					class Y {
						public static void count(int values) { System.out.print('1'); }
						public static void count(int ... values) { System.out.print('2'); }
					}
					class Z {
						public static void count(long l, long values) { System.out.print('3'); }
						public static void count(int i, int ... values) { System.out.print('4'); }
						public static void count2(int i, int values) { System.out.print('5'); }
						public static void count2(long l, int ... values) { System.out.print('6'); }
					}
					""",
			},
			"<21233466>");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.test((Object[]) null);
							Y.test(null, null);
							Y.test(null, null, null);
							System.out.print('>');
						}
					}
					class Y {
						public static void test(Object o, Object o2) { System.out.print('1'); }
						public static void test(Object ... values) { System.out.print('2'); }
					}
					""",
			},
			"<212>");
	}

	public void test010() {
		// according to spec this should find count(Object) since it should consider count(Object...) as count(Object[]) until all fixed arity methods are ruled out
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count((Object) Integer.valueOf(1));
							Y.count(Integer.valueOf(1));
					
							Y.count((Object) null);
							Y.count((Object[]) null);
							System.out.print('>');
						}
					}
					class Y {
						public static void count(Object values) { System.out.print('1'); }
						public static void count(Object ... values) { System.out.print('2'); }
					}
					""",
			},
			"<1112>");
		// according to spec this should find count(Object[]) since it should consider count(Object[]...) as count(Object[][]) until all fixed arity methods are ruled out
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							System.out.print('<');
							Y.count(new Object[] {Integer.valueOf(1)});
							Y.count(new Integer[] {Integer.valueOf(1)});
					
							Y.count((Object[]) null);
							Y.count((Object[][]) null);
							System.out.print('>');
						}
					}
					class Y {
						public static void count(Object[] values) { System.out.print('1'); }
						public static void count(Object[] ... values) { System.out.print('2'); }
					}
					""",
			},
			"<1112>");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.string(null);
							Y.string2(null);
							Y.int2(null);
						}
					}
					class Y {
						public static void string(String values) { System.out.print('1'); }
						public static void string(String ... values) { System.out.print('2'); }
						public static void string2(String[] values) { System.out.print('1'); }
						public static void string2(String[] ... values) { System.out.print('2'); }
						public static void int2(int[] values) { System.out.print('1'); }
						public static void int2(int[] ... values) { System.out.print('2'); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Y.string(null);
					  ^^^^^^
				The method string(String) is ambiguous for the type Y
				----------
				2. ERROR in X.java (at line 4)
					Y.string2(null);
					  ^^^^^^^
				The method string2(String[]) is ambiguous for the type Y
				----------
				3. ERROR in X.java (at line 5)
					Y.int2(null);
					  ^^^^
				The method int2(int[]) is ambiguous for the type Y
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83379
	public void test011() {
		runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					public class X { void count(int ... values) {} }
					class Y extends X { void count(int[] values) {} }
					class Z extends Y { void count(int... values) {} }
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					class Y extends X { void count(int[] values) {} }
					                         ^^^^^^^^^^^^^^^^^^^
				Varargs methods should only override or be overridden by other varargs methods unlike Y.count(int[]) and X.count(int...)
				----------
				2. WARNING in X.java (at line 2)
					class Y extends X { void count(int[] values) {} }
					                         ^^^^^^^^^^^^^^^^^^^
				The method count(int[]) of type Y should be tagged with @Override since it actually overrides a superclass method
				----------
				3. WARNING in X.java (at line 3)
					class Z extends Y { void count(int... values) {} }
					                         ^^^^^^^^^^^^^^^^^^^^
				Varargs methods should only override or be overridden by other varargs methods unlike Z.count(int...) and Y.count(int[])
				----------
				4. WARNING in X.java (at line 3)
					class Z extends Y { void count(int... values) {} }
					                         ^^^^^^^^^^^^^^^^^^^^
				The method count(int...) of type Z should be tagged with @Override since it actually overrides a superclass method
				----------
				""",
			null,
			null,
			JavacTestOptions.EclipseHasABug.EclipseBug236379);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77084
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main (String ... args) {
					       for (String a:args) {
					           System.out.println(a);
					       }
					   }
					}
					
					"""
			}
		);
	}

	public void test013() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.count(1, 1);
						}
					}
					class Y {
						public static void count(long i, int j) { System.out.print(1); }
						public static void count(int ... values) { System.out.print(2); }
					}
					""",
			},
			"1");
	}

	public void test014() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.count(new int[0], 1);
							Y.count(new int[0], 1, 1);
						}
					}
					class Y {
						public static void count(int[] array, int ... values) { System.out.print(1); }
						public static void count(Object o, int ... values) { System.out.print(2); }
					}
					""",
			},
			"11"
		);
	}

	public void test015() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.count(new int[0]);
						}
					}
					class Y {
						public static void count(int[] array, int ... values) { System.out.print(1); }
						public static void count(int[] array, int[] ... values) { System.out.print(2); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Y.count(new int[0]);
					  ^^^^^
				The method count(int[], int[]) is ambiguous for the type Y
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test015_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
			this.runNegativeTest(
					new String[] {
							"X.java",
							"""
								public class X {
									public static void main(String[] s) {
										Y.count(new int[0]);
									}
								}
								class Y {
									public static void count(int[] array, int ... values) { System.out.print(1); }
									public static void count(int[] array, int[] ... values) { System.out.print(2); }
								}
								""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						Y.count(new int[0]);
						  ^^^^^
					The method count(int[], int[]) is ambiguous for the type Y
					----------
					""",
				null, true, options);
			} else {
				this.runConformTest(
					new String[] {
							"X.java",
							"""
								public class X {
									public static void main(String[] s) {
										Y.count(new int[0]);
									}
								}
								class Y {
									public static void count(int[] array, int ... values) { System.out.print(1); }
									public static void count(int[] array, int[] ... values) { System.out.print(2); }
								}
								""",
				},
				"1",
				null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}

	public void test016() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest( // but this call is ambiguous
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.count(new int[0]);
							Y.count(new int[0], null);
						}
					}
					class Y {
						public static void count(int[] array, int ... values) { System.out.print(0); }
						public static void count(int[] array, int[][] ... values) { System.out.print(1); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Y.count(new int[0]);
					  ^^^^^
				The method count(int[], int[]) is ambiguous for the type Y
				----------
				2. ERROR in X.java (at line 4)
					Y.count(new int[0], null);
					  ^^^^^
				The method count(int[], int[]) is ambiguous for the type Y
				----------
				"""
		);
	}

	public void test017() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] s) {
							Y.count(new int[0], 1);
							Y.count(new int[0], 1, 1);
							Y.count(new int[0], 1, 1, 1);
						}
					}
					class Y {
						public static void count(int[] array, int ... values) {}
						public static void count(int[] array, int[] ... values) {}
						public static void count(int[] array, int i, int ... values) {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Y.count(new int[0], 1);
					  ^^^^^
				The method count(int[], int[]) is ambiguous for the type Y
				----------
				2. ERROR in X.java (at line 4)
					Y.count(new int[0], 1, 1);
					  ^^^^^
				The method count(int[], int[]) is ambiguous for the type Y
				----------
				3. ERROR in X.java (at line 5)
					Y.count(new int[0], 1, 1, 1);
					  ^^^^^
				The method count(int[], int[]) is ambiguous for the type Y
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590
	public void test018() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
						public static void main(String[] args) {
							String[][] x = {{"X"}, {"Y"}};
							List l = Arrays.asList(x);
							System.out.println(l.size() + " " + l.get(0).getClass().getName());
						}
					}
					""",
			},
			"2 [Ljava.lang.String;");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590 - variation
	public void test019() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
						public static void main(String[] args) {
							String[][] x = {{"X"}, {"Y"}};
							System.out.println(asList(x[0], x[1]).get(1).getClass().getName());
						}
						static <U> List<U> asList(U u1, U... us) {
							List<U> result = new ArrayList<U>();
							result.add(u1);
							result.add(us[0]);
							return result;
						}
					}
					""",
			},
			"java.lang.String");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590 - variation
	public void test020() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
						public static void main(String[] args) {
							String[][] x = {{"X"}, {"Y"}};
							System.out.println(asList(x[0], x).get(1).getClass().getName());
						}
						static <U> List<U> asList(U u1, U... us) {
							List<U> result = new ArrayList<U>();
							result.add(u1);
							result.add(us[0]);
							return result;
						}
					}
					""",
			},
			"[Ljava.lang.String;");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81911
	public void test021() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Arrays;
					
					public class X {
					   public static void main(String[] args) {
					      String[][] arr = new String[][] { args };
					      ArrayList<String[]> al = new ArrayList<String[]>(Arrays.asList(arr));
					   }
					}
					""",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83032
	public void test022() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						String[] args;
						public X(String... args) {
							this.args = args;
						}
						public static X foo() {
							return new X("SU", "C", "CE", "SS"){};
						}
						public String bar() {
							if (this.args != null) {
								StringBuffer buffer = new StringBuffer();
								for (String s : this.args) {
									buffer.append(s);
								}
								return String.valueOf(buffer);
							}
							return null;
						}
						public static void main(String[] args) {
							System.out.print(foo().bar());
						}
					}
					""",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83536
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main (String[] args) {
					        new X().test (new byte[5]);
							 System.out.print("SUCCESS");
					    }
					    private void test (Object... params) {
					    }
					}""",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87042
	public void test024() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static boolean foo(Object... args) {
							return args == null;
						}
					
						public static void main(String[] args) {
							System.out.println(foo(null, null));
						}
					}""",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87042
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						static boolean foo(Object... args) {
							return args == null;
						}
					
						public static void main(String[] args) {
							System.out.println(foo(null));
						}
					}""",
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87318
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
						static void foo(int[] intarray) {
							List<int[]> l = Arrays.asList(intarray);
							System.out.print(l.get(0).length);
						}
						static void foo(String[] strarray) {
							List l = Arrays.asList(strarray);
							System.out.print(l);
						}\t
						public static void main(String[] args) {
							foo(new int[]{0, 1});
							foo(new String[]{"a","b"});
							System.out.println("done");
						}
					}
					""",
			},
			"2[a, b]done");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87900
	public void test027() { // ensure AccVarargs does not collide
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						transient private X() {}
						void test() { X x = new X(); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)\r
					transient private X() {}\r
					                  ^^^
				Illegal modifier for the constructor in type X; only public, protected & private are permitted
				----------
				"""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						transient private X(Object... o) {}
						void test() { X x = new X(1, 2); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					transient private X(Object... o) {}
					                  ^^^^^^^^^^^^^^
				Illegal modifier for the constructor in type X; only public, protected & private are permitted
				----------
				"""
		);
	}
	// check no offending unnecessary varargs cast gets diagnosed
	public void test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					
					public class X {
						void test(Method method){\s
							try {
								method.invoke(this);
								method.invoke(this, new Class[0]);
								method.invoke(this, (Object[])new Class[0]);
							} catch (Exception e) {
							}	\t
						}
					  Zork z;
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 7)
					method.invoke(this, new Class[0]);
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type Class[] of the last argument to method invoke(Object, Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				2. ERROR in X.java (at line 12)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91467
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					/**
					 * Whatever you do, eclipse doesn\'t like it.
					 */
					public class X {
					
						/**
						 * Passing a String vararg to a method needing an Object array makes eclipse
						 * either ask for a cast or complain that it is unnecessary. You cannot do
						 * it right.
						 *\s
						 * @param s
						 */
						public static void q(String... s) {
							 // OK reports: Varargs argument String[] should be cast to Object[] when passed to the method 	printf(String, Object...) from type PrintStream
							System.out.printf("", s);
							// WRONG reports: Unnecessary cast from String[] to Object[]
							System.out.printf("", (Object[]) s);\s
						}
					  Zork z;
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 15)
					System.out.printf("", s);
					^^^^^^^^^^^^^^^^^^^^^^^^
				Type String[] of the last argument to method printf(String, Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
				----------
				2. ERROR in X.java (at line 19)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99260
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					public class X {
						public static void main(String[] args) {
							audit("osvaldo", "localhost", "logged", "X", Integer.valueOf(0));
							audit("osvaldo", "localhost", "logged", "X", "Y");
							audit("osvaldo", "localhost", "logged", new Float(0), new java.awt.Point(0, 0));
						}
						public static <A extends Serializable> void audit(String login,
								String address, String event, A... args) {
							for (A a : args) {
								System.out.println(a.getClass());
							}
						}
					}""",
			},
			"""
				class java.lang.String
				class java.lang.Integer
				class java.lang.String
				class java.lang.String
				class java.lang.Float
				class java.awt.Point""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102181
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static void main(String[] args) {
							Test<String> t = new Tester();
							t.method("SUCCESS");
						}
					
						static abstract class Test<A> {
							abstract void method(A... args);
						}
					
						static class Tester extends Test<String> {
					
							@Override void method(String... args) {
								call(args);
							}
					
							void call(String[] args) {
								for (String str : args)
									System.out.println(str);
							}
						}
					}
					""",
			},
			"SUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102278
	public void test032() {
		this.runConformTest(
			new String[] {
				"Functor.java",
				"""
					public class Functor<T> {
						public void func(T... args) {
							// do noting;
						}
					\t
						public static void main(String... args) {
							Functor<String> functor = new Functor<String>() {
								public void func(String... args) {
									System.out.println(args.length);
								}
							};
							functor.func("Hello!");
						}
					}
					""",
			},
			"1");
	}
 	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102631
	public void test033() {
		this.runNegativeTest(
			false /* skipJavac */,
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
					JavacTestOptions.Excuse.JavacCompilesIncorrectSource : null,
			new String[] {
				"X.java",
				"""
					public class X {
						void a(boolean b, Object... o) {System.out.print(1);}
						void a(Object... o) {System.out.print(2);}
						public static void main(String[] args) {
							X x = new X();
							x.a(true);
							x.a(true, "foobar");
							x.a("foo", "bar");
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					x.a(true);
					  ^
				The method a(boolean, Object[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 7)
					x.a(true, "foobar");
					  ^
				The method a(boolean, Object[]) is ambiguous for the type X
				----------
				""");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void b(boolean b, Object... o) {}
						void b(Boolean... o) {}
						void c(boolean b, boolean b2, Object... o) {}
						void c(Boolean b, Object... o) {}
						public static void main(String[] args) {
							X x = new X();
							x.b(true);
							x.b(true, false);
							x.c(true, true, true);
							x.c(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)\r
					x.b(true);\r
					  ^
				The method b(boolean, Object[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 9)\r
					x.b(true, false);\r
					  ^
				The method b(boolean, Object[]) is ambiguous for the type X
				----------
				3. ERROR in X.java (at line 10)\r
					x.c(true, true, true);\r
					  ^
				The method c(boolean, boolean, Object[]) is ambiguous for the type X
				----------
				4. ERROR in X.java (at line 11)\r
					x.c(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);\r
					  ^
				The method c(boolean, boolean, Object[]) is ambiguous for the type X
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test033_tolerate() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
				false /* skipJavac */,
				this.complianceLevel < ClassFileConstants.JDK1_7 ?
						JavacTestOptions.Excuse.JavacCompilesIncorrectSource : null,
				new String[] {
						"X.java",
						"""
							public class X {
								void a(boolean b, Object... o) {System.out.print(1);}
								void a(Object... o) {System.out.print(2);}
								public static void main(String[] args) {
									X x = new X();
									x.a(true);
									x.a(true, "foobar");
									x.a("foo", "bar");
								}
							}
							""",
					},
					"""
						----------
						1. ERROR in X.java (at line 6)
							x.a(true);
							  ^
						The method a(boolean, Object[]) is ambiguous for the type X
						----------
						2. ERROR in X.java (at line 7)
							x.a(true, "foobar");
							  ^
						The method a(boolean, Object[]) is ambiguous for the type X
						----------
						""",
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
							"X.java",
							"""
								public class X {
									void a(boolean b, Object... o) {System.out.print(1);}
									void a(Object... o) {System.out.print(2);}
									public static void main(String[] args) {
										X x = new X();
										x.a(true);
										x.a(true, "foobar");
										x.a("foo", "bar");
									}
								}
								""",
						},
						"112",
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106106
	public void test034() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;\s
					
					public class X {
					  public static void main(String[] args) {
					    double[][] d = { { 1 } , { 2 } };\s
					    List<double[]> l = Arrays.asList(d); // <T> List<T> asList(T... a)
					    System.out.println("List size: " + l.size());
					  }
					}
					""",
			},
			"List size: 2");
	}
	//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=108095
	public void test035() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static <T> void foo(T ... values) {
					      System.out.print(values.getClass());
					  }
						public static void main(String args[]) {
						   X.<String>foo("monkey", "cat");
					      X.<String>foo(new String[] { "monkey", "cat" });
						}
					}""",
			},
			"class [Ljava.lang.String;class [Ljava.lang.String;");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110563
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
					    public void testBreak() {
					        Collection<Class> classes = new ArrayList<Class>();
					        classes.containsAll(Arrays.asList(String.class, Integer.class, Long.class));
					    }
					}
					""",
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110783
	public void test037() {
		this.runConformTest(
			new String[] {
				"V.java",
				"""
					public class V {
					    public static void main(String[] s) {
					        V v = new V();
					        v.foo("", v, null, "");
					        v.foo("", v, null, "", 1);
					        v.foo2("");
					        v.foo2("", null);
					        v.foo2("", null, null);
					        v.foo3("", v, null, "", null);
					    }
					    void foo(String s, V v, Object... obs) {System.out.print(1);}
					    void foo(String s, V v, String r, Object o, Object... obs) {System.out.print(2);}
					    void foo2(Object... a) {System.out.print(1);}
					    void foo2(String s, Object... a) {System.out.print(2);}
					    void foo2(String s, Object o, Object... a) {System.out.print(3);}
					    void foo3(String s, V v, String... obs) {System.out.print(1);}
					    void foo3(String s, V v, String r, Object o, Object... obs) {System.out.print(2);}
					}
					""",
			},
			"222232");
		this.runNegativeTest(
			new String[] {
				"V.java",
				"""
					public class V {
					    public static void main(String[] s) {
					        V v = new V();
					        v.foo2(null, "");
					        v.foo2(null, "", "");
					        v.foo3("", v, null, "");
					    }
					    void foo2(String s, Object... a) {System.out.print(2);}
					    void foo2(String s, Object o, Object... a) {System.out.print(3);}
					    void foo3(String s, V v, String... obs) {System.out.print(1);}
					    void foo3(String s, V v, String r, Object o, Object... obs) {System.out.print(2);}
					}
					""",
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in V.java (at line 4)\r
					v.foo2(null, "");\r
					  ^^^^
				The method foo2(String, Object[]) is ambiguous for the type V
				----------
				2. ERROR in V.java (at line 5)\r
					v.foo2(null, "", "");\r
					  ^^^^
				The method foo2(String, Object[]) is ambiguous for the type V
				----------
				3. ERROR in V.java (at line 6)\r
					v.foo3("", v, null, "");\r
					  ^^^^
				The method foo3(String, V, String[]) is ambiguous for the type V
				----------
				"""
			: """
				----------
				1. ERROR in V.java (at line 4)
					v.foo2(null, "");
					  ^^^^
				The method foo2(String, Object[]) is ambiguous for the type V
				----------
				2. ERROR in V.java (at line 5)
					v.foo2(null, "", "");
					  ^^^^
				The method foo2(String, Object[]) is ambiguous for the type V
				----------
				""")
			);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void varargs(Serializable... items) {
					        System.out.println(Arrays.deepToString(items) + " (argument wrapped)");
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
						     varargs(new Object[] {1, 2}); //warns "Varargs argument Object[]\s
						     //should be cast to Serializable[] ..", but proposed cast to
						     //Serializable[] fails at runtime (javac does not warn here)
						     varargs((Serializable[])new Object[] {1, 2}); //warns "Varargs argument Object[]\s
						     //should be cast to Serializable[] ..", but proposed cast to
						     //Serializable[] fails at runtime (javac does not warn here)
					        Zork z;
					    }
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 13)
					varargs((Serializable[])new Object[] {1, 2}); //warns "Varargs argument Object[]\s
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from Object[] to Serializable[]
				----------
				2. ERROR in X.java (at line 16)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void varargs(Serializable... items) {
					        System.out.print(Arrays.deepToString(items) + " (argument wrapped)");
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
					    	try {
						        varargs(new Object[] {1, 2}); //warns "Varargs argument Object[]\s
						        	//should be cast to Serializable[] ..", but proposed cast to
						            //Serializable[] fails at runtime (javac does not warn here)
						        varargs((Serializable[])new Object[] {1, 2}); //warns "Varargs argument Object[]\s
						    	//should be cast to Serializable[] ..", but proposed cast to
						        //Serializable[] fails at runtime (javac does not warn here)
					    	} catch(ClassCastException e) {
					    		System.out.println("SUCCESS");
					    	}
					    }
					}
					""",
			},
			"[[1, 2]] (argument wrapped)SUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void array(Serializable... items) {
					        System.out.print(Arrays.deepToString(items));
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
					        array(new Serializable[] {3, 4});
					        array(new Integer[] {5, 6}); //warns (as javac does)
					        array(null); //warns (as javac does)
					    }
					}
					""",
			},
			"[3, 4][5, 6]null");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void array(Serializable... items) {
					        System.out.print(Arrays.deepToString(items));
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
					        array(new Serializable[] {3, 4});
					        array(new Integer[] {5, 6}); //warns (as javac does)
					        array(null); //warns (as javac does)
					        Zork z;
					    }
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 11)
					array(new Integer[] {5, 6}); //warns (as javac does)
					^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type Integer[] of the last argument to method array(Serializable...) doesn't exactly match the vararg parameter type. Cast to Serializable[] to confirm the non-varargs invocation, or pass individual arguments of type Serializable for a varargs invocation.
				----------
				2. WARNING in X.java (at line 12)
					array(null); //warns (as javac does)
					^^^^^^^^^^^
				Type null of the last argument to method array(Serializable...) doesn't exactly match the vararg parameter type. Cast to Serializable[] to confirm the non-varargs invocation, or pass individual arguments of type Serializable for a varargs invocation.
				----------
				3. ERROR in X.java (at line 13)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void varargs(Serializable... items) {
					        System.out.print(Arrays.deepToString(items) + " (argument wrapped)");
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
					        varargs((Serializable) new Object[] {1, 2});
					        varargs((Serializable) new Serializable[] {3, 4}); //warns about
					            //unnecessary cast, although cast is necessary (causes varargs call)
					        varargs((Serializable) new Integer[] {5, 6});
					        varargs((Serializable) null);
					    }
					}
					""",
			},
			"[[1, 2]] (argument wrapped)[[3, 4]] (argument wrapped)[[5, 6]] (argument wrapped)[null] (argument wrapped)");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void varargs(Serializable... items) {
					        System.out.print(Arrays.deepToString(items) + " (argument wrapped)");
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
					        varargs((Serializable) new Object[] {1, 2});
					        varargs((Serializable) new Serializable[] {3, 4}); //warns about
					            //unnecessary cast, although cast is necessary (causes varargs call)
					        varargs((Serializable) new Integer[] {5, 6});
					        varargs((Serializable) null);
					        Zork z;
					    }
					}
					""",
			},
			"""
				----------
				1. WARNING in X.java (at line 10)
					varargs((Serializable) new Object[] {1, 2});
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from Object[] to Serializable
				----------
				2. ERROR in X.java (at line 15)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test044() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Arrays;
					
					public class X {
					    static void array(Serializable... items) {
					        System.out.print(Arrays.deepToString(items));
					    }
					    @SuppressWarnings({"boxing"})
					    public static void main(String[] args) {
					        array((Serializable[]) new Serializable[] {3, 4}); //warns about unnecessary cast
					        array((Serializable[]) new Integer[] {5, 6});
					        array((Serializable[]) null);
					        try {
						        array((Serializable[]) new Object[] {1, 2}); // CCE at run time
					        } catch(ClassCastException e) {
					        	System.out.println("SUCCESS");
					        }
					    }
					}
					""",
			},
			"[3, 4][5, 6]nullSUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test045() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.Serializable;
						import java.util.Arrays;
						
						public class X {
						    static void array(Serializable... items) {
						        System.out.print(Arrays.deepToString(items));
						    }
						    @SuppressWarnings({"boxing"})
						    public static void main(String[] args) {
						        array((Serializable[]) new Serializable[] {3, 4}); //warns about unnecessary cast
						        array((Serializable[]) new Integer[] {5, 6});
						        array((Serializable[]) null);
							     array((Serializable[]) new Object[] {1, 2}); // CCE at run time
						        Zork z;
						    }
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 10)
						array((Serializable[]) new Serializable[] {3, 4}); //warns about unnecessary cast
						      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Serializable[] to Serializable[]
					----------
					2. WARNING in X.java (at line 13)
						array((Serializable[]) new Object[] {1, 2}); // CCE at run time
						      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Object[] to Serializable[]
					----------
					3. ERROR in X.java (at line 14)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133918
	public void test046() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							void foo(Throwable... exceptions) {
							}
							void bar(Exception[] exceptions) {
								foo((Throwable[])exceptions);
							}
							Zork z;
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						foo((Throwable[])exceptions);
						    ^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Exception[] to Throwable[]
					----------
					2. ERROR in X.java (at line 7)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140168
	public void test047() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(Object id, Object value, String... groups) {}
						void foo(Y y, String... groups) {System.out.println(true);}
						public static void main(String[] args) {
							new X().foo(new Y(), "a", "b");
						}
					}
					class Y {}""",
			},
			"true");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(Y y, Object value, String... groups) {}
						void foo(Object id, String... groups) {}
						public static void main(String[] args) {
							new X().foo(new Y(), "a", "b");
						}
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)\r
					new X().foo(new Y(), "a", "b");\r
					        ^^^
				The method foo(Y, Object, String[]) is ambiguous for the type X
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139931
	public void test048() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						        Y<String> [] foo() {
						                return null;
						        }
						        void bar(Y... y) {
						        }
						        void fred() {
						                bar(foo());
						                bar((Y[])foo());
						                Zork z;
						        }
						}
						class Y<E> {
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						void bar(Y... y) {
						         ^
					Y is a raw type. References to generic type Y<E> should be parameterized
					----------
					2. WARNING in X.java (at line 9)
						bar((Y[])foo());
						    ^^^^^^^^^^
					Unnecessary cast from Y<String>[] to Y[]
					----------
					3. ERROR in X.java (at line 10)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141704
	public void test049() {
		this.runConformTest(
				new String[] {
					"Y.java",
					"""
						public class Y extends X {
							public static void main(String[] args) {
								Y y = new Y();
								y.a(null, "");
								y.a(null);
								y.a(y, "");
								y.a(y);
								y.a(y, "", y, y);
								y.a(y, y, y);
							}
							@Override public void a(Object anObject, String aString, Object... args) { super.a(anObject, aString, this, args); }
							@Override public void a(Object anObject, Object... args) { super.a(anObject, this, args); }
						}
						class X implements I {
							public void a(Object anObject, String aString, Object... args) { System.out.print(1); }
							public void a(Object anObject, Object... args) { System.out.print(2); }
						}
						interface I {
							void a(Object anObject, String aString, Object... args);
							void a(Object anObject, Object... args);
						}
						""",
				},
				"121212");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141800
	public void test050() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						 import java.util.Arrays;
						 public class X {
						   public static void main( String args[] ) {
						      Object test = new Object[] { "Hello", "World" };
						      System.out.println(Arrays.asList(test));
						      System.out.println(Arrays.asList((Object[])test)); // Warning here
							   Zork z;
						   }
						}""",
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
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141800 - variation
	public void test051() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						 import java.util.Arrays;
						 public class X {
						   public static void main( String args[] ) {
						      Object test = new Object[] { "Hello", "World" };
						      System.out.print(Arrays.asList(test).size());
						      System.out.println(Arrays.asList((Object[])test).size()); // Warning here
						   }
						}""",
				},
				"12");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159607
	public void test052() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
							void addChildren(Widget w) {
								if (w instanceof Composite) {
									Composite composite = (Composite) w;
									addAll((Widget[]) composite.getChildren());
									addAll(composite.getChildren());
								}
								Zork z;
							}
							void addAll(Widget... widgets) {
							}
						}
						
						class Widget {}
						class Control extends Widget {}
						class Composite extends Control {
							Control[] getChildren() {
								return null;
							}
						}""", // =================,
				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						addAll((Widget[]) composite.getChildren());
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Control[] to Widget[]
					----------
					2. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159607 - variation
	public void test053() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
							void addChildren(Widget w) {
								if (w instanceof Composite) {
									Composite composite = (Composite) w;
									addAll((Control[]) composite.getChildren());
									addAll(composite.getChildren());
								}
								Zork z;
							}
							void addAll(Control... widgets) {
							}
						}
						
						class Widget {}
						class Control extends Widget {}
						class Composite extends Control {
							Control[] getChildren() {
								return null;
							}
						}""", // =================,
				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						addAll((Control[]) composite.getChildren());
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Control[] to Control[]
					----------
					2. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	public void test054() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Zork z;
							public static void varargs(Object... args) {
								if (args == null) {
									System.out.println("args is null");
									return;
								}
								if (args.length == 0) {
									System.out.println("args is of length 0");
									return;
								}
						
								System.out.println(args.length + " " + args[0]);
							}
						
							public static void main(String[] args) {
								@SuppressWarnings("boxing")
								Integer[] i = { 0, 1, 2, 3, 4 };
								varargs(i);
								varargs((Object[]) i);
								varargs((Object) i);
								varargs(i.clone());
							}
						}
						""", // =================
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					2. WARNING in X.java (at line 19)
						varargs(i);
						^^^^^^^^^^
					Type Integer[] of the last argument to method varargs(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
					----------
					3. WARNING in X.java (at line 22)
						varargs(i.clone());
						^^^^^^^^^^^^^^^^^^
					Type Integer[] of the last argument to method varargs(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
					----------
					""");
	}
	public void test055() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							private static int elementCount(Object... elements) {
						     return elements == null ? 0 : elements.length;
						   }
						   public static void main(String... args) {
						     System.out.print("null length array: " + elementCount(null));
						     System.out.print("/[null] length array: " + elementCount((Object)null));
						     System.out.print("/empty length array: " + elementCount());
						     System.out.println("/[a,b,c] length array: " + elementCount("a", "b", "c"));
						   }
						}""", // =================
				},
				"null length array: 0/[null] length array: 1/empty length array: 0/[a,b,c] length array: 3");
	}
	public void test056() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Zork z;
							private static int elementCount(Object... elements) {
						     return elements == null ? 0 : elements.length;
						   }
						   public static void main(String... args) {
						     System.out.print("null length array: " + elementCount(null));
						     System.out.print("/[null] length array: " + elementCount((Object)null));
						     System.out.print("/empty length array: " + elementCount());
						     System.out.println("/[a,b,c] length array: " + elementCount("a", "b", "c"));
						   }
						}""", // =================
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					2. WARNING in X.java (at line 7)
						System.out.print("null length array: " + elementCount(null));
						                                         ^^^^^^^^^^^^^^^^^^
					Type null of the last argument to method elementCount(Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=163889
	public void test057() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.RetentionPolicy;
						
						public class X {
						
						  void a(Enum<?>...enums) {}
						
						  void b () {
						    RetentionPolicy[] t = null;
						    a(t);
						    a((Enum<?>[])t);
						    Zork z;
						  }
						}
						""", // =================
				},
				"""
					----------
					1. WARNING in X.java (at line 10)
						a((Enum<?>[])t);
						  ^^^^^^^^^^^^
					Unnecessary cast from RetentionPolicy[] to Enum<?>[]
					----------
					2. ERROR in X.java (at line 11)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162171
	public void test058() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public void testPassingSubclassArrayAsVararg() {
						        // The argument of type VarargsTest.Subclass[] should explicitly be
						        // cast to VarargsTest.Parent[] for the invocation of the varargs
						        // method processVararg(VarargsTest.Parent...) from type VarargsTest.
						        // It could alternatively be cast to VarargsTest.Parent for a varargs
						        // invocation
						        processVararg(new Subclass[] {});
						    }
						
						    public void testPassingSubclassArrayAsVarargWithCast() {
						        // Unnecessary cast from VarargsTest.Subclass[] to
						        // VarargsTest.Parent[]
						        processVararg((Parent[]) new Subclass[] {});
						        processVararg(new Subclass[] {});
						        Zork z;
						    }
						
						    private void processVararg(Parent... objs) {
						    }
						
						    class Parent {
						    }
						
						    class Subclass extends Parent {
						    }
						}
						""", // =================
				},
				"""
					----------
					1. WARNING in X.java (at line 14)
						processVararg((Parent[]) new Subclass[] {});
						              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from X.Subclass[] to X.Parent[]
					----------
					2. ERROR in X.java (at line 16)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=170765
	public void test059() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public void foo() {
								Integer[] array = null;
								varargs(array);
							}
						
							public void varargs(Number... o) {
							}
						    Zork z;
						}
						""", // =================
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
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186181
	public void test060() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						   public static String varargMethod( Object... objects ) {\r
						      String s = "";
						      for (Object object : objects)
						         s += "," + object.toString();
						      return s;
						   }
						}""",
				},
				"",
				null,
				true,
				null,
				options,
				null);

		// make sure that this file contains the varargs attribute
		IClassFileReader reader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		IMethodInfo[] methodInfos = reader.getMethodInfos();
		assertEquals("Wrong size", 2, methodInfos.length);
		IMethodInfo methodInfo = null;
		if (CharOperation.equals(methodInfos[0].getName(), "varargMethod".toCharArray())) {
			methodInfo = methodInfos[0];
		} else if (CharOperation.equals(methodInfos[1].getName(), "varargMethod".toCharArray())) {
			methodInfo = methodInfos[1];
		}
		assertTrue("ACC_VARARGS is not set", (methodInfo.getAccessFlags() & ClassFileConstants.AccVarargs) == 0);
		assertNotNull("Method varargMethodshould be there", methodInfo);
		assertEquals("2", 2, methodInfo.getAttributeCount());
		IClassFileAttribute[] attributes = methodInfo.getAttributes();
		assertTrue("varargs attribute not found", CharOperation.equals(attributes[0].getAttributeName(), "Varargs".toCharArray())
				|| CharOperation.equals(attributes[1].getAttributeName(), "Varargs".toCharArray()));

		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		this.runConformTest(
				new String[] {
					"UseVararg.java",
					"""
						public class UseVararg {\r
						   public static void main( String[] args ) {
						      String arg = "SUCCESS";
						      String results = X.varargMethod(arg);
						      System.out.println( results );
						   }\r
						}""",
				},
				",SUCCESS",
				null,
				false,
				null,
				options,
				null);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223427
	public void test061() {
		String expectedOutput =
				"""
			----------
			1. WARNING in X.java (at line 5)
				Collections.addAll(constantClassSet, String.class, Object.class);
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: A generic array of Class<? extends Object> is created for a varargs parameter
			----------
			2. ERROR in X.java (at line 6)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""";
		if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
			expectedOutput =
					"""
						----------
						1. ERROR in X.java (at line 6)
							Zork z;
							^^^^
						Zork cannot be resolved to a type
						----------
						""";
		}
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
							public static void main(String[] args) {
								HashSet<Class<?>> constantClassSet = new HashSet<Class<?>>();
								Collections.addAll(constantClassSet, String.class, Object.class);
								Zork z;
							}
						}
						""", // =================
				},
				expectedOutput);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328247
	public void test062() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\r
						private static final String CONST = "";\r
					\r
						public static class A {\r
							A(Integer i, String... tab) {}\r
						}\r
						\r
						Object foo(final Float f) {\r
							return new A(Integer.valueOf(0), CONST) {\r
								public String toString() {\r
									return f.toString();\r
								}\r
							};\r
						}\r
					}""",
			},
			"");
		String expectedOutput =
			"""
			  // Method descriptor #10 (LX;Ljava/lang/Integer;[Ljava/lang/String;Ljava/lang/Float;)V
			  // Stack: 3, Locals: 5
			  X$1(X arg0, java.lang.Integer $anonymous0, java.lang.String... $anonymous1, java.lang.Float arg3);
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$1.class", "X$1", expectedOutput);
	}
	//safe varargs support
	public void test063() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"Y.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					public class Y {\r
						@SafeVarargs
						public static <T> List<T> asList(T... a) {
							return null;
						}
					}""",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					public class X {\r
						public void bar() {
							List<? extends Class<?>> classes = Y.asList(String.class, Boolean.class);
						}
					}""",
			},
			"",
			null,
			false,
			null,
			options,
			null);
	}
	public void test064() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					public class X {\r
						@SafeVarargs
						public static <T> List<T> asList(T... a) {
							return null;
						}
						public void bar() {
							List<? extends Class<?>> classes = X.asList(String.class, Boolean.class);
						}
					}""",
			},
			"",
			null,
			false,
			null,
			options,
			null);
	}
	public void test065() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					public class X {\r
						@SafeVarargs
						public static <T> List<T> asList(T... a) {
							return null;
						}
						public void bar() {
							List<List<String>> classes = X.asList(X.asList("Hello", "World"));
						}
					}""",
			},
			"",
			null,
			false,
			null,
			options,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337093
	public void test066() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.Collection;
						import java.util.Iterator;
						public class X {
						    public static class IteratorChain<T> implements Iterator<T> {
						       public IteratorChain(Collection<? extends T> a, Collection<? extends T> b, Collection<? extends T> ... collections) {
						        }
						        public boolean hasNext() {
						            return false;
						        }
						        public T next() {
						            return null;
						        }
						        public void remove() {
						            throw new UnsupportedOperationException();
						        }
						    }
						    public static void main(String[] args) {
						        new IteratorChain<Number>(null, null);
						    }
						}
						""", // =================
				},
				this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. WARNING in X.java (at line 18)
						new IteratorChain<Number>(null, null);
						^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type safety: A generic array of Collection<? extends Number> is created for a varargs parameter
					----------
					""":
				"""
					----------
					1. WARNING in X.java (at line 5)
						public IteratorChain(Collection<? extends T> a, Collection<? extends T> b, Collection<? extends T> ... collections) {
						                                                                                                       ^^^^^^^^^^^
					Type safety: Potential heap pollution via varargs parameter collections
					----------
					2. WARNING in X.java (at line 18)
						new IteratorChain<Number>(null, null);
						^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type safety: A generic array of Collection<? extends Number> is created for a varargs parameter
					----------
					""",
				null,
				true,
				options);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337799
	public void test067() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
						@SafeVarargs
						public static <T> List<T> asList() {  // Error, not varargs
							return null;
						}
						@SafeVarargs
						public <T> List<T> asList2(T ... a) {    // error not static or final
							return null;
						}
						@SafeVarargs
						public static <T> List<T> asList3(T ... a) {  // OK, varargs & static
							return null;
						}
						@SafeVarargs
						public final <T> List<T> asList4(T ... a) {  // OK varargs & final
							return null;
						}
						@SafeVarargs
						public final static <T> List<T> asList5(T ... a) {  // OK, varargs & static & final
							return null;
						}
						@SafeVarargs
						public int b;
					}
					interface I {
						@SafeVarargs
						public  <T> List<T> asList(T ... t);
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public static <T> List<T> asList() {  // Error, not varargs
					                          ^^^^^^^^
				@SafeVarargs annotation cannot be applied to fixed arity method asList
				----------
				2. ERROR in X.java (at line 8)
					public <T> List<T> asList2(T ... a) {    // error not static or final
					                   ^^^^^^^^^^^^^^^^
				@SafeVarargs annotation cannot be applied to non-final instance method asList2
				----------
				3. ERROR in X.java (at line 23)
					@SafeVarargs
					^^^^^^^^^^^^
				The annotation @SafeVarargs is disallowed for this location
				----------
				4. ERROR in X.java (at line 28)
					public  <T> List<T> asList(T ... t);
					                    ^^^^^^^^^^^^^^^
				@SafeVarargs annotation cannot be applied to non-final instance method asList
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337799
	public void test067b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
						@SafeVarargs
						public X() {  // Error, not varargs
						}
						@SafeVarargs
						public <T> X(T ... a) {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					public X() {  // Error, not varargs
					       ^^^
				@SafeVarargs annotation cannot be applied to fixed arity method X
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795 (make sure there is no warning if vararg parameter is reifiable)
	public void test068() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
						public <T> X(String ... a) {
						}
						public <T> X(int i, String ... a) {
						}
					   public <T> List<T> asList(String ... a) {
					       return null;
					   }
					   public <T> List<T> asList(Zork t, String ... a) {
					       return null;
					   }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					public <T> List<T> asList(Zork t, String ... a) {
					                          ^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795 (make sure there is a warning if vararg parameter is not reifiable)
	public void test068b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
						public <T> X(T ... a) {
						}
						public <T> X(int i, T ... a) {
						}
					   public <T> List<T> asList(T ... a) {
					       return null;
					   }
					   public <T> List<T> asList(T t, T ... a) {
					       return null;
					   }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					public <T> X(T ... a) {
					                   ^
				Type safety: Potential heap pollution via varargs parameter a
				----------
				2. WARNING in X.java (at line 5)
					public <T> X(int i, T ... a) {
					                          ^
				Type safety: Potential heap pollution via varargs parameter a
				----------
				3. WARNING in X.java (at line 7)
					public <T> List<T> asList(T ... a) {
					                                ^
				Type safety: Potential heap pollution via varargs parameter a
				----------
				4. WARNING in X.java (at line 10)
					public <T> List<T> asList(T t, T ... a) {
					                                     ^
				Type safety: Potential heap pollution via varargs parameter a
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795
	public void test068c() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					   @SafeVarargs
						public <T> X(T ... a) {
						}
					   @SafeVarargs
						public <T> X(int i, T ... a) {
						}
					   @SafeVarargs
					   public <T> List<T> asList(T ... a) {
					       return null;
					   }
					   @SafeVarargs
					   public <T> List<T> asList(T t, T ... a) {
					       return null;
					   }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					public <T> List<T> asList(T ... a) {
					                   ^^^^^^^^^^^^^^^
				@SafeVarargs annotation cannot be applied to non-final instance method asList
				----------
				2. ERROR in X.java (at line 14)
					public <T> List<T> asList(T t, T ... a) {
					                   ^^^^^^^^^^^^^^^^^^^^
				@SafeVarargs annotation cannot be applied to non-final instance method asList
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795
	public void test068d() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);

		String[] bounds = new String[] { "Object","Serializable","Comparable<?>"};
		String[] bounds15 = new String[] { "Object","Serializable","Comparable<?>", "Constable"};
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					   @SafeVarargs
					   public static <T> List<T> asList(T ... a) {
					       return null;
					   }
					   public static <T> List<T> asList2(T ... a) {
					       return null;
					   }
						List<? extends Class<?>> classes;\s
					   {
					     classes = X.asList(String.class, Boolean.class);
						  classes = X.asList2(String.class, Boolean.class);
					   }
					}
					"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 7)\n" +
			"	public static <T> List<T> asList2(T ... a) {\n" +
			"	                                        ^\n" +
			"Type safety: Potential heap pollution via varargs parameter a\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 13)\n" +
			"	classes = X.asList2(String.class, Boolean.class);\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: A generic array of Class<? extends "+intersection(isJRE15Plus ? bounds15 : bounds)+"> is created for a varargs parameter\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795 (test effect of SuppressWarnings (should suppress at declaration site, but not at call site)
	public void test068e() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		String[] bounds = new String[] { "Object","Serializable","Comparable<?>"};
		String[] bounds15 = new String[] { "Object","Serializable","Comparable<?>", "Constable"};
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					   @SafeVarargs
					   public static <T> List<T> asList(T ... a) {
					       return null;
					   }
					   @SuppressWarnings("unchecked")
					   public static <T> List<T> asList2(T ... a) {
					       return null;
					   }
						List<? extends Class<?>> classes;\s
					   {
					     classes = X.asList(String.class, Boolean.class);
						  classes = X.asList2(String.class, Boolean.class);
					   }
					}
					"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 14)\n" +
			"	classes = X.asList2(String.class, Boolean.class);\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: A generic array of Class<? extends "+intersection(isJRE15Plus ? bounds15 : bounds)+ "> is created for a varargs parameter\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346042
	public void test069() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runNegativeTest(
			new String[] {
				"p1/B.java",
				"""
					package p1;
					class A {
					}
					public class B extends A {
					 public void foo(A... args) {
					 }
					}
					""",
				"p2/C.java",
				"""
					package p2;
					import p1.B;
					public class C {
					
					 public static final void main(String[] args) {
					   (new B()).foo(new B(), new B());
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in p2\\C.java (at line 6)
					(new B()).foo(new B(), new B());
					          ^^^
				The method foo(A...) of type B is not applicable as the formal varargs element type A is not accessible here
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static void foo(int ...i) {}
					        public static void foo(double...d) {}
					        public static void main(String[] args) {
					            foo(1, 2, 3);
					            System.out.println ("Done");
					        }
					}
					"""
			},
			"Done");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					new String[] {
						"X.java",
						"""
							public class X {
							        public static void foo(int ...i) {}
							        public static void foo(double...d) {}
							        public static void main(String[] args) {
							            foo(1, 2, 3);
							            System.out.println ("Done");
							        }
							}
							"""
					},
					"""
						----------
						1. ERROR in X.java (at line 5)
							foo(1, 2, 3);
							^^^
						The method foo(int[]) is ambiguous for the type X
						----------
						""",
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
							"X.java",
							"""
								public class X {
								        public static void foo(int ...i) {}
								        public static void foo(double...d) {}
								        public static void main(String[] args) {
								            foo(1, 2, 3);
								            System.out.println ("Done");
								        }
								}
								"""
						},
						"Done",
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}

	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070_tolerate2() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					false /* skipJavac */,
					this.complianceLevel == ClassFileConstants.JDK1_7 ?
							JavacTestOptions.Excuse.JavacCompilesIncorrectSource : null,
					new String[] {
						"X.java",
						"""
							import java.util.Arrays;
							public class X {
							        public static void test(int... a) {
										System.out.println(Arrays.toString(a));
							}
							        public static <T> void test(Object... a) {
										System.out.println(Arrays.toString(a));
							}
							        public static void main(String[] args) {
							            test(1);
							        }
							}
							"""
					},
					"""
						----------
						1. ERROR in X.java (at line 10)
							test(1);
							^^^^
						The method test(int[]) is ambiguous for the type X
						----------
						""",
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
								"X.java",
								"""
									import java.util.Arrays;
									public class X {
									        public static void test(int... a) {
												System.out.println(Arrays.toString(a));
									}
									        public static <T> void test(Object... a) {
												System.out.println(Arrays.toString(a));
									}
									        public static void main(String[] args) {
									            test(1);
									        }
									}
									"""
						},
						"[1]",
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}

	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070a() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static <T> void foo(int ...i) {}
					        public static <T> void foo(double...d) {}
					        public static void main(String[] args) {
					            foo(1, 2, 3);
					            System.out.println ("Done");
					        }
					}
					"""
			},
			"Done");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070a_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					new String[] {
						"X.java",
						"""
							public class X {
							        public static <T> void foo(int ...i) {}
							        public static <T> void foo(double...d) {}
							        public static void main(String[] args) {
							            foo(1, 2, 3);
							            System.out.println ("Done");
							        }
							}
							"""
					},
					"""
						----------
						1. ERROR in X.java (at line 5)
							foo(1, 2, 3);
							^^^
						The method foo(int[]) is ambiguous for the type X
						----------
						""",
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
							"X.java",
							"""
								public class X {
								        public static <T> void foo(int ...i) {}
								        public static <T> void foo(double...d) {}
								        public static void main(String[] args) {
								            foo(1, 2, 3);
								            System.out.println ("Done");
								        }
								}
								"""
						},
						"Done",
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        public static void foo(int ...i) {}
					        public static void foo(double d1, double...d) {}
					        public static void main(String[] args) {
					            foo(1, 2, 3);     // foo NOT flagged ambiguous
					        }
					}
					"""
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070b_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src = new String[] {
				"X.java",
				"""
					public class X {
					        public static void foo(int ...i) {}
					        public static void foo(double d1, double...d) {}
					        public static void main(String[] args) {
					            foo(1, 2, 3);     // foo NOT flagged ambiguous
					        }
					}
					"""
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"");
			} else {
				this.runNegativeTest(
						src,
						"""
							----------
							1. ERROR in X.java (at line 5)
								foo(1, 2, 3);     // foo NOT flagged ambiguous
								^^^
							The method foo(int[]) is ambiguous for the type X
							----------
							""");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070c() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] s) {
					        count(1);
					        count(1, 1);
					        count(1, 1, 1);
					    }
					    public static void count(int ... values) {}
					    public static void count(int i, int ... values) {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					count(1);
					^^^^^
				The method count(int[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 4)
					count(1, 1);
					^^^^^
				The method count(int[]) is ambiguous for the type X
				----------
				3. ERROR in X.java (at line 5)
					count(1, 1, 1);
					^^^^^
				The method count(int[]) is ambiguous for the type X
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070d() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void b(boolean b, Object... o) {}
						void b(Boolean... o) {}
						void c(boolean b, boolean b2, Object... o) {}
						void c(Boolean b, Object... o) {}
						public static void main(String[] args) {
							X x = new X();
							x.b(true);
							x.b(true, false);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)\r
					x.b(true);\r
					  ^
				The method b(boolean, Object[]) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 9)\r
					x.b(true, false);\r
					  ^
				The method b(boolean, Object[]) is ambiguous for the type X
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346039
	public void test071() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements IClass{
					    X(IClass c, X t, IType... args) {
						     System.out.println ("1");
					    }
					    X(IClass c, IType... args) {
						    System.out.println ("2");
					    }
					    public static void main(String args[]) {
					        IClass c = null;
					        X t = null;
					        X t2 = new X(c, t);     // incorrectly flagged ambiguous
					    }
					}
					interface IType{}
					interface IClass extends IType{}
					"""
			},
			"1");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test071_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src =
			new String[] {
				"X.java",
				"""
					public class X implements IClass{
					    X(IClass c, X t, IType... args) {
						     System.out.println ("1");
					    }
					    X(IClass c, IType... args) {
						    System.out.println ("2");
					    }
					    public static void main(String args[]) {
					        IClass c = null;
					        X t = null;
					        X t2 = new X(c, t);     // incorrectly flagged ambiguous
					    }
					}
					interface IType{}
					interface IClass extends IType{}
					"""
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"1");
			} else {
				this.runNegativeTest(
						src,
						"""
							----------
							1. ERROR in X.java (at line 11)
								X t2 = new X(c, t);     // incorrectly flagged ambiguous
								       ^^^^^^^^^^^
							The constructor X(IClass, X, IType[]) is ambiguous
							----------
							""");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364672
	public void test072() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						private class Z {}
						public void foo() {
								Z[] zs = null;
								Y.bar(zs, new Z());
						}
						public static void main(String[] args) {}
					}""",
				"Y.java",
				"""
					public class Y {
						public native static <T> void bar(T[] t, T t1, T... t2);
					}"""
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364672
	public void test073() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static final String CONSTANT = "";
						private static class A {
							A(String s, String s2, String s3, A... a) {}
						}
						private static class B extends A {
							B(String s, String s2) {
								super(s, s2, CONSTANT);
							}
						}
						private static void foo(Object o, A ... a) {
						}
						private static B bar() {
							return null;
						}
						public static void main(String[] args) {
							Object o = null;
							foo(o, bar(), bar());
						}
					}"""
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364672
	public void test074() throws Exception {
		this.runNegativeTest(
			new String[] {
				"p1/B.java",
				"""
					package p1;
					class A {}
					public class B extends A {
					 public B(A... args) {}
					 public B() {}
					}
					""",
				"p2/C.java",
				"""
					package p2;
					import p1.B;
					public class C {
						public static final void main(String[] args) {
							new B(new B(), new B());
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in p2\\C.java (at line 5)
					new B(new B(), new B());
					^^^^^^^^^^^^^^^^^^^^^^^
				The constructor B(A...) of type B is not applicable as the formal varargs element type A is not accessible here
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382469
	public void testBug382469() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src =
			new String[] {
				"X.java",
				"""
					public class X {
					    private static void bar(Object... objs) {
						     System.out.println ("1");
					    }
					    private static void bar(int intValue, Object... objs) {
						     System.out.println ("2");
					    }
					    public static void main(String args[]) {
					        bar(5);
					    }
					}
					"""
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"2");
			} else {
				this.runNegativeTest(
						src,
						"""
							----------
							1. WARNING in X.java (at line 5)
								private static void bar(int intValue, Object... objs) {
								                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
							The method bar(int, Object...) from the type X is never used locally
							----------
							2. ERROR in X.java (at line 9)
								bar(5);
								^^^
							The method bar(Object[]) is ambiguous for the type X
							----------
							""");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386361
	public void testBug386361() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src =
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void test(int i, Object... objects) {
						     System.out.println ("1");
					    }
					    public static void test(Object... objects) {
						     System.out.println ("2");
					    }
					    public static void main(String args[]) {
					        test(1,"test");
					    }
					}
					"""
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"1");
			} else {
				this.runNegativeTest(
						src,
						"""
							----------
							1. ERROR in X.java (at line 9)
								test(1,"test");
								^^^^
							The method test(int, Object[]) is ambiguous for the type X
							----------
							""");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426678, [1.8][compiler] Another issue with vararg type element accessibility
	public void test426678() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import p.*;
					public class X  {
					    public static void main(String argv[]) {
					        new B().foo(null, null);
					    }
					}
					""",

				"p/B.java",
				"""
					package p;
					class A {
					}
					public class B extends A {
					    public void foo(A ... o) { System.out.println("MB:A"); }
					    public void foo(Object... o) { System.out.println("MB:O"); }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					new B().foo(null, null);
					        ^^^
				The method foo(A...) of type B is not applicable as the formal varargs element type A is not accessible here
				----------
				"""); // check and adjust,
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436474, [codegen]Problem with varargs and List.toString
	public void test436474() {
		runConformTest(
			new String[] {
				"Scratch.java",
				"""
					import java.util.Arrays;
					import java.util.List;
					public class Scratch {
					    public static void vararg(String... strs) {
					    	System.out.println(strs[0]);
					    }
					   \s
					    public static void main(String[] args) {
					        List<String> l = Arrays.asList("a");
					        vararg(l.toArray(new String[0]));
					    }
					}""",
			},
			"a");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=437973, [1.8][compiler] Missing implementation of JLS 15.12.2.5 Third Bullet - Part 2
	public void test437973() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					class W {}
					class Y extends W {}
					class Z extends Y {}
					class A{}
					public class X {
						void foo(String format, Object ... args) {
							System.out.println("foo Object Varargs");
						}
						void foo(String ... s) {
							System.out.println("foo String Varargs");
						}
						void foo1(Z ... z) {
							System.out.println("foo1 Z varargs");
						}
						void foo1(Z z, Y ... y) {
							System.out.println("foo1 Y varargs");
						}
						void foo2(Z z, W ... w) {
							System.out.println("foo2 W varargs");
						}
						void foo2(Z z, Y ... y) {
							System.out.println("foo2 Y varargs");
						}
						void foo3(A a, W ... w) {
							System.out.println("foo3 W varargs");
						}
						void foo3(A a, Y ... y) {
							System.out.println("foo3 Y varargs");
						}
						void foo4(W w) {
							System.out.println("foo4 W");
						}
						void foo4(W w, A ... a) {
							System.out.println("foo4 A varargs");
						}
						void foo5(W w) {
							System.out.println("foo5 W");
						}
						void foo5(W ... w) {
							System.out.println("foo5 W varargs");
						}
						void foo6(W ... w) {
							System.out.println("foo6 W varargs");
						}
						void foo6(Y ... y) {
							System.out.println("foo6 Y varargs");
						}
					   void foo7(String format, Object ... args) {
						    System.out.println("foo7 Object Varargs");
					   }
					   void foo8(String ... s) {
						    System.out.println("foo8 String Varargs");
					   }
						void bar() {
							foo("f");
							foo("f", 12);
							foo1(new Z());
							foo2(new Z());
							foo3(new A());
							foo4(new W());
							foo5(new W());
							foo6(new W());
							foo6(new Y());
					       foo7("f", 12);
					       foo8("f");
						}
						public static void main(String[] args) {
							X x = new X();
							x.bar();
						}
					}
					""",
			},
			"""
				foo String Varargs
				foo Object Varargs
				foo1 Z varargs
				foo2 Y varargs
				foo3 Y varargs
				foo4 W
				foo5 W
				foo6 W varargs
				foo6 Y varargs
				foo7 Object Varargs
				foo8 String Varargs""");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=437973, [1.8][compiler] Missing implementation of JLS 15.12.2.5 Third Bullet - Part 2
	public void test437973a() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		runConformTest(
		new String[] {
			"X.java",
			"""
				class W {}
				class Y extends W {}
				public class X {
					void foo(W w, Y ... y) {
						System.out.println("foo Y varargs");
				   }
					void foo(W ... w) {
						System.out.println("foo W varargs");
				   }
					void bar() {
						foo(new W(), new W(), new W());
						foo(new Y(), new Y(), new Y());
						foo(new W());
						foo(new Y());
					}
					public static void main(String[] args) {
						X x = new X();
						x.bar();
					}
				}
				"""},
			"""
				foo W varargs
				foo Y varargs
				foo Y varargs
				foo Y varargs""");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=437973, [1.8][compiler] Missing implementation of JLS 15.12.2.5 Third Bullet - Part 2
	//The parameter of one method is not a subtype of the other.
	public void test437973b() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		runNegativeTest(
		new String[] {
			"X.java",
			"""
				class W {}
				class A {}
				public class X {
				    void foo(W ... w) {}
				    void foo(W w, A ... a) {}
				    void bar() {
				        foo(new W()); // 1.8 Error: Ambiguous method error
				    }
				    public static void main(String[] args) {}
				}
				"""},
			"""
				----------
				1. ERROR in X.java (at line 7)
					foo(new W()); // 1.8 Error: Ambiguous method error
					^^^
				The method foo(W[]) is ambiguous for the type X
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437973, [1.8][compiler] Missing implementation of JLS 15.12.2.5 Third Bullet - Part 2
	// Lambda functions
	public void test437973c() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		runNegativeTest(
		new String[] {
			"X.java",
			"""
				class W {}
				class Y extends W {}
				class Z extends Y {}
				class A{}
				
				interface I1 {
					void foo (Y ... y);\s
					default void foo (Y y, W ... w) {}
				}
				
				public class X {
					void bar() {
						I1 i1 = (x) -> {};
						i1.foo(new Y());
					}
				}
				"""},"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437973, [1.8][compiler] Missing implementation of JLS 15.12.2.5 Third Bullet - Part 2
	// Original Test Case - Comment 0
	public void test437973d() {
		runConformTest(
		new String[] {
			"Junk16.java",
			"""
				public class Junk16 {
				    public static String junk(String format, Object... args) {
						 System.out.println("junk 1");
				        return null;
				    }
				    public static String junk(String... s) {
						 System.out.println("junk 2");
				        return null;
				    }
				    public static void main(String[] args) {
				        // COMPILE ERROR IN ECLIPSE (none in JDK) WITHOUT FIX
				        junk("fred");
				        //NO COMPILE ERROR
				        junk("fred", 12);
				    }
				}
				"""},
			"junk 2\n" +
			"junk 1");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443596, [1.8][compiler] Failure for overload resolution in case of Generics and Varags
	public void _test443596() {
		runConformTest(
		new String[] {
			"Collections2.java",
			"""
				public final class Collections2 {
				    static interface Predicate<T> { boolean test(T object); }
				    public static <T> Predicate<T> in(Predicate<? extends T> arg) { return null; }
				    public static <T> Predicate<T> and(Predicate<? super T>... arg) { return null; }
				    public static <T> Predicate<T> and(Predicate<? super T> arg0, Predicate<? super T> arg1) { return null; }
				    static class FilteredCollection<E> {
				        Predicate<? super E> predicate;
				        public void error(Predicate<?> arg) { and(predicate, in(arg)); } // no compile
				    }
				}
				"""});
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=470370, [1.8] Wrong varargs behaviour causes ArrayStoreException
		public void test470370() {
			runConformTest(
			new String[] {
				"TestVarargs.java",
				"""
					import java.util.*;
					public class TestVarargs {
					    public static void main(String[] args) {
					        bar(new Class<?>[]{});
					        foo(new Class<?>[]{});
					    }
					    public static Object foo(Class<?>[] sig) {
					        return Arrays.asList(Arrays.copyOfRange(sig, 0, sig.length));
					    }
					    public static List<Class<?>> bar(Class<?>[] sig) {
					        return Arrays.asList(Arrays.copyOfRange(sig, 0, sig.length));
					    }
					}"""
			},
			"");
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=488658
		public void testBug488658_001() throws Exception {
			if (this.complianceLevel < ClassFileConstants.JDK9) return;
			this.runConformTest(
				new String[] {
					"X.java",
					"""
						class Y<T> {}
						@SuppressWarnings("unused")
						public class X {
							@SafeVarargs
							private <T> Y<T> foo(T ... a) {
								return null;
							}
						}
						""",
				},
			"");
			Map options = getCompilerOptions();
			options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
			this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						class Y<T> {}
						public class X {
						@SuppressWarnings("unused")
							private <T> Y<T> foo(T ... a) {
								return null;
							}
						}
						"""
				},
				"""
					----------
					1. WARNING in X.java (at line 4)
						private <T> Y<T> foo(T ... a) {
						                           ^
					Type safety: Potential heap pollution via varargs parameter a
					----------
					""");

		}

}
