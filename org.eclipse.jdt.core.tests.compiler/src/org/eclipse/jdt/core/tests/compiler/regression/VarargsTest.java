/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class VarargsTest extends AbstractComparableTest {

	public VarargsTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 24, 25 };
//		TESTS_RANGE = new int[] { 11, -1 };
//	}
	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
	}
	
	public static Class testClass() {
		return VarargsTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		\n" +
				"		Y.count();\n" +
				"		Y.count(null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2, (byte) 3, 4);\n" +
				"		Y.count(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public Y(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"	public static void count(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"}\n",
			},
			"<0 0 1 10 10 0 0 1 10 10 >");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		\n" +
				"		Y.count();\n" +
				"		Y.count(null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2, (byte) 3, 4);\n" +
				"		Y.count(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n",
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
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y extends Z {\n" +
				"	public Y(int ... values) { super(values); }\n" +
				"}\n" +
				"class Z {\n" +
				"	public Z(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"}\n",
			},
			"<0 0 1 10 10 >");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n",
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
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count();\n" +
				"		Y.count((int[]) null);\n" +
				"		Y.count((int[][]) null);\n" +
				"		Y.count(new int[] {1});\n" +
				"		Y.count(new int[] {1, 2}, new int[] {3, 4});\n" +
				"		Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int count(int[] values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print(result);\n" +
				"		return result;\n" +
				"	}\n" +
				"	public static void count(int[] ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += count(values[i]);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(result);\n" +
				"	}\n" +
				"}\n",
			},
			"<=0 0=0 1 3 7=10 6 4=10>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count();\n" +
				"		Y.count((int[]) null);\n" +
				"		Y.count((int[][]) null);\n" +
				"		Y.count(new int[] {1});\n" +
				"		Y.count(new int[] {1, 2}, new int[] {3, 4});\n" +
				"		Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n"
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
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(0);\n" +
				"		Y.count(-1, (int[]) null);\n" +
				"		Y.count(-2, (int[][]) null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(2, new int[] {1});\n" +
				"		Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});\n" +
				"		Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int count(int j, int[] values) {\n" +
				"		int result = j;\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print('[');\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(']');\n" +
				"		return result;\n" +
				"	}\n" +
				"	public static void count(int j, int[] ... values) {\n" +
				"		int result = j;\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(':');\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += count(j, values[i]);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(result);\n" +
				"	}\n" +
				"}\n",
			},
			"< 0:=0 [-1] -2:=-2 1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(0);\n" +
				"		Y.count(-1, (int[]) null);\n" +
				"		Y.count(-2, (int[][]) null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(2, new int[] {1});\n" +
				"		Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});\n" +
				"		Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n"
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
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.print();\n" +
				"		Y.print(new Integer(1));\n" +
				"		Y.print(new Integer(1), new Byte((byte) 3), new Integer(7));\n" +
				"		Y.print(new Integer[] {new Integer(11) });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void print(Number ... values) {\n" +
				"		for (int i = 0, l = values.length; i < l; i++) {\n" +
				"			System.out.print(' ');\n" +
				"			System.out.print(values[i]);\n" +
				"		}\n" +
				"		System.out.print(',');\n" +
				"	}\n" +
				"}\n",
			},
			"<, 1, 1 3 7, 11,>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.print();\n" +
				"		Y.print(new Integer(1));\n" +
				"		Y.print(new Integer(1), new Byte((byte) 3), new Integer(7));\n" +
				"		Y.print(new Integer[] {new Integer(11) });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n",
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
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		String[] T_NAMES = new String[] {\"foo\"};\n" +
				"		String error = \"error\";\n" +
				"		Y.format(\"E_UNSUPPORTED_CONV\", new Integer(0));\n" +
				"		Y.format(\"E_SAVE\", T_NAMES[0], error);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static String format(String key) { return null; }\n" +
				"	public static String format(String key, Object ... args) { return null; }\n" +
				"}\n",
			},
			"");
	}

	public void test007() { // array dimension test compatibility with Object
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.byte2(null);\n" + // warning: inexact argument type for last parameter
				"		Y.byte2((byte) 1);\n" + // error
				"		Y.byte2(new byte[] {});\n" +
				"		Y.byte2(new byte[][] {});\n" + 
				"		Y.byte2(new byte[][][] {});\n" + // error
				"\n" +
				"		Y.object(null);\n" + // warning
				"		Y.object((byte) 1);\n" +
				"		Y.object(new byte[] {});\n" +
				"		Y.object(new byte[][] {});\n" + // warning
				"		Y.object(new byte[][][] {});\n" + // warning
				"\n" +
				"		Y.object(new String());\n" +
				"		Y.object(new String[] {});\n" + // warning
				"		Y.object(new String[][] {});\n" + // warning
				"\n" +
				"		Y.object2(null);\n" + // warning
				"		Y.object2((byte) 1);\n" + // error
				"		Y.object2(new byte[] {});\n" + // error
				"		Y.object2(new byte[][] {});\n" + 
				"		Y.object2(new byte[][][] {});\n" + // warning
				"\n" +
				"		Y.object2(new String());\n" + // error
				"		Y.object2(new String[] {});\n" + 
				"		Y.object2(new String[][] {});\n" + // warning
				"\n" +
				"		Y.string(null);\n" + // warning
				"		Y.string(new String());\n" +
				"		Y.string(new String[] {});\n" +
				"		Y.string(new String[][] {});\n" + // error
				"\n" +
				"		Y.string(new Object());\n" + // error
				"		Y.string(new Object[] {});\n" + // error
				"		Y.string(new Object[][] {});\n" + // error
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void byte2(byte[] ... values) {}\n" +
				"	public static void object(Object ... values) {}\n" +
				"	public static void object2(Object[] ... values) {}\n" +
				"	public static void string(String ... values) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	Y.byte2(null);\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to byte[][] when passed to the method byte2(byte[]...) from type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Y.byte2((byte) 1);\n" + 
			"	  ^^^^^\n" + 
			"The method byte2(byte[]...) in the type Y is not applicable for the arguments (byte)\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	Y.byte2(new byte[][][] {});\n" + 
			"	  ^^^^^\n" + 
			"The method byte2(byte[]...) in the type Y is not applicable for the arguments (byte[][][])\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 9)\n" + 
			"	Y.object(null);\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to Object[] when passed to the method object(Object...) from type Y\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 12)\n" + 
			"	Y.object(new byte[][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument byte[][] should be cast to Object[] when passed to the method object(Object...) from type Y\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 13)\n" + 
			"	Y.object(new byte[][][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument byte[][][] should be cast to Object[] when passed to the method object(Object...) from type Y\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 16)\n" + 
			"	Y.object(new String[] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument String[] should be cast to Object[] when passed to the method object(Object...) from type Y\n" + 
			"----------\n" + 
			"8. WARNING in X.java (at line 17)\n" + 
			"	Y.object(new String[][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument String[][] should be cast to Object[] when passed to the method object(Object...) from type Y\n" + 
			"----------\n" + 
			"9. WARNING in X.java (at line 19)\n" + 
			"	Y.object2(null);\n" + 
			"	^^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to Object[][] when passed to the method object2(Object[]...) from type Y\n" + 
			"----------\n" + 
			"10. ERROR in X.java (at line 20)\n" + 
			"	Y.object2((byte) 1);\n" + 
			"	  ^^^^^^^\n" + 
			"The method object2(Object[]...) in the type Y is not applicable for the arguments (byte)\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 21)\n" + 
			"	Y.object2(new byte[] {});\n" + 
			"	  ^^^^^^^\n" + 
			"The method object2(Object[]...) in the type Y is not applicable for the arguments (byte[])\n" + 
			"----------\n" + 
			"12. WARNING in X.java (at line 23)\n" + 
			"	Y.object2(new byte[][][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument byte[][][] should be cast to Object[][] when passed to the method object2(Object[]...) from type Y\n" + 
			"----------\n" + 
			"13. ERROR in X.java (at line 25)\n" + 
			"	Y.object2(new String());\n" + 
			"	  ^^^^^^^\n" + 
			"The method object2(Object[]...) in the type Y is not applicable for the arguments (String)\n" + 
			"----------\n" + 
			"14. WARNING in X.java (at line 27)\n" + 
			"	Y.object2(new String[][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument String[][] should be cast to Object[][] when passed to the method object2(Object[]...) from type Y\n" + 
			"----------\n" + 
			"15. WARNING in X.java (at line 29)\n" + 
			"	Y.string(null);\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to String[] when passed to the method string(String...) from type Y\n" + 
			"----------\n" + 
			"16. ERROR in X.java (at line 32)\n" + 
			"	Y.string(new String[][] {});\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String...) in the type Y is not applicable for the arguments (String[][])\n" + 
			"----------\n" + 
			"17. ERROR in X.java (at line 34)\n" + 
			"	Y.string(new Object());\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String...) in the type Y is not applicable for the arguments (Object)\n" + 
			"----------\n" + 
			"18. ERROR in X.java (at line 35)\n" + 
			"	Y.string(new Object[] {});\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String...) in the type Y is not applicable for the arguments (Object[])\n" + 
			"----------\n" + 
			"19. ERROR in X.java (at line 36)\n" + 
			"	Y.string(new Object[][] {});\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String...) in the type Y is not applicable for the arguments (Object[][])\n" + 
			"----------\n");
	}

	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y y = new Y(null);\n" +
				"		y = new Y(true, null);\n" + // null warning
				"		y = new Y('i', null);\n" + // null warning
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public Y(int ... values) {}\n" +
				"	public Y(boolean b, Object ... values) {}\n" +
				"	public Y(char c, int[] ... values) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	y = new Y(true, null);\n" + 
			"	    ^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to Object[] when passed to the constructor Y(boolean, Object...)\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	y = new Y(\'i\', null);\n" + 
			"	    ^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to int[][] when passed to the constructor Y(char, int[]...)\n" + 
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y y = new Y(null);\n" +
				"		y = new Y(true, null);\n" + // null warning
				"		y = new Y('i', null);\n" + // null warning
				"	}\n" +
				"}\n" +
				"class Y extends Z {\n" +
				"	public Y(int ... values) { super(values); }\n" +
				"	public Y(boolean b, Object ... values) { super(b, values); }\n" +
				"	public Y(char c, int[] ... values) {}\n" +
				"}\n" +
				"class Z {\n" +
				"	public Z(int ... values) {}\n" +
				"	public Z(boolean b, Object ... values) {}\n" +
				"	public Z(char c, int[] ... values) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	y = new Y(true, null);\n" + 
			"	    ^^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to Object[] when passed to the constructor Y(boolean, Object...)\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	y = new Y(\'i\', null);\n" + 
			"	    ^^^^^^^^^^^^^^^^\n" + 
			"Varargs argument null should be cast to int[][] when passed to the constructor Y(char, int[]...)\n" + 
			"----------\n");
	}

	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2);\n" +
				"\n" +
				"		Z.count(1L, 1);\n" + // only choice is Z.count(long, int)
				"		Z.count(1, 1);\n" + // chooses Z.count(long, long) over Z.count(int,int...)
				"		Z.count(1, null);\n" + // only choice is Z.count(int,int...)
				"		Z.count2(1, null);\n" + // better choice is Z.count(int,int[])
				"		Z.count2(1L, null);\n" + // better choice is Z.count(long,int...)
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int values) { System.out.print('1'); }\n" +
				"	public static void count(int ... values) { System.out.print('2'); }\n" +
				"}\n" +
				"class Z {\n" +
				"	public static void count(long l, long values) { System.out.print('3'); }\n" +
				"	public static void count(int i, int ... values) { System.out.print('4'); }\n" +
				"	public static void count2(int i, int values) { System.out.print('5'); }\n" +
				"	public static void count2(long l, int ... values) { System.out.print('6'); }\n" +
				"}\n",
			},
			"<21233466>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.test((Object[]) null);\n" + // cast to avoid null warning
				"		Y.test(null, null);\n" +
				"		Y.test(null, null, null);\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(Object o, Object o2) { System.out.print('1'); }\n" +
				"	public static void test(Object ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"<212>");
	}

	public void test010() {
		// according to spec this should find count(Object) since it should consider count(Object...) as count(Object[]) until all fixed arity methods are ruled out
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count((Object) new Integer(1));\n" +
				"		Y.count(new Integer(1));\n" +
				"\n" +
				"		Y.count((Object) null);\n" +
				"		Y.count((Object[]) null);\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(Object values) { System.out.print('1'); }\n" +
				"	public static void count(Object ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"<1112>");
		// according to spec this should find count(Object[]) since it should consider count(Object[]...) as count(Object[][]) until all fixed arity methods are ruled out
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(new Object[] {new Integer(1)});\n" +
				"		Y.count(new Integer[] {new Integer(1)});\n" +
				"\n" +
				"		Y.count((Object[]) null);\n" +
				"		Y.count((Object[][]) null);\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(Object[] values) { System.out.print('1'); }\n" +
				"	public static void count(Object[] ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"<1112>");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.string(null);\n" +
				"		Y.string2(null);\n" +
				"		Y.int2(null);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void string(String values) { System.out.print('1'); }\n" +
				"	public static void string(String ... values) { System.out.print('2'); }\n" +
				"	public static void string2(String[] values) { System.out.print('1'); }\n" +
				"	public static void string2(String[] ... values) { System.out.print('2'); }\n" +
				"	public static void int2(int[] values) { System.out.print('1'); }\n" +
				"	public static void int2(int[] ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Y.string(null);\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Y.string2(null);\n" + 
			"	  ^^^^^^^\n" + 
			"The method string2(String[]) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Y.int2(null);\n" + 
			"	  ^^^^\n" + 
			"The method int2(int[]) is ambiguous for the type Y\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83379
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void count(int ... values) {} }\n" +
				"class Y extends X { void count(int[] values) {} }\n" +
				"class Z extends Y { void count(int... values) {} }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	class Y extends X { void count(int[] values) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^\n" + 
			"The method count(int[]) of type Y should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	class Y extends X { void count(int[] values) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs methods should only override other varargs methods unlike Y.count(int[]) and X.count(int...)\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	class Z extends Y { void count(int... values) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^^\n" + 
			"The method count(int...) of type Z should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 3)\n" + 
			"	class Z extends Y { void count(int... values) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Varargs methods should only override other varargs methods unlike Z.count(int...) and Y.count(int[])\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77084
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
					"   public static void main (String ... args) {\n" + 
					"       for (String a:args) {\n" + 
					"           System.out.println(a);\n" + 
					"       }\n" + 
					"   }\n" + 
					"}\n" + 
					"\n"
			}
		);
	}

	public void test013() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(long i, int j) { System.out.print(1); }\n" +
				"	public static void count(int ... values) { System.out.print(2); }\n" +
				"}\n",
			},
			"1");
	}

	public void test014() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0], 1);\n" +
				"		Y.count(new int[0], 1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) { System.out.print(1); }\n" +
				"	public static void count(Object o, int ... values) { System.out.print(2); }\n" +
				"}\n",
			},
			"11"
		);
	}

	public void test015() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0]);\n" + // for some reason this is not ambiguous
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) { System.out.print(1); }\n" +
				"	public static void count(int[] array, int[] ... values) { System.out.print(2); }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test016() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest( // but this call is ambiguous
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0]);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int[][]...) in Y match
				"		Y.count(new int[0], null);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int[]...) in Y match
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) { System.out.print(0); }\n" +
				"	public static void count(int[] array, int[][] ... values) { System.out.print(1); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Y.count(new int[0]);\n" + 
			"	  ^^^^^\n" + 
			"The method count(int[], int[]) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Y.count(new int[0], null);\n" + 
			"	  ^^^^^\n" + 
			"The method count(int[], int[]) is ambiguous for the type Y\n" + 
			"----------\n"
		);
	}

	public void test017() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0], 1);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int,int...) in Y match
				"		Y.count(new int[0], 1, 1);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int,int...) in Y match
				"		Y.count(new int[0], 1, 1, 1);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int,int...) in Y match
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) {}\n" +
				"	public static void count(int[] array, int[] ... values) {}\n" +
				"	public static void count(int[] array, int i, int ... values) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Y.count(new int[0], 1);\n" + 
			"	  ^^^^^\n" + 
			"The method count(int[], int[]) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Y.count(new int[0], 1, 1);\n" + 
			"	  ^^^^^\n" + 
			"The method count(int[], int[]) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Y.count(new int[0], 1, 1, 1);\n" + 
			"	  ^^^^^\n" + 
			"The method count(int[], int[]) is ambiguous for the type Y\n" + 
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590
	public void test018() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String[][] x = {{\"X\"}, {\"Y\"}};\n" + 
				"		List l = Arrays.asList(x);\n" + 
				"		System.out.println(l.size() + \" \" + l.get(0).getClass().getName());\n" + 
				"	}\n" + 
				"}\n",
			},
			"2 [Ljava.lang.String;");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590 - variation
	public void test019() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String[][] x = {{\"X\"}, {\"Y\"}};\n" + 
				"		System.out.println(asList(x[0], x[1]).get(1).getClass().getName());\n" + 
				"	}\n" + 
				"	static <U> List<U> asList(U u1, U... us) {\n" + 
				"		List<U> result = new ArrayList<U>();\n" + 
				"		result.add(u1);\n" + 
				"		result.add(us[0]);\n" + 
				"		return result;\n" + 
				"	}\n" + 
				"}\n",
			},
			"java.lang.String");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590 - variation
	public void test020() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String[][] x = {{\"X\"}, {\"Y\"}};\n" + 
				"		System.out.println(asList(x[0], x).get(1).getClass().getName());\n" + 
				"	}\n" + 
				"	static <U> List<U> asList(U u1, U... us) {\n" + 
				"		List<U> result = new ArrayList<U>();\n" + 
				"		result.add(u1);\n" + 
				"		result.add(us[0]);\n" + 
				"		return result;\n" + 
				"	}\n" + 
				"}\n",
			},
			"[Ljava.lang.String;");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81911
	public void test021() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.Arrays;\n" + 
				"\n" + 
				"public class X {\n" + 
				"   public static void main(String[] args) {\n" + 
				"      String[][] arr = new String[][] { args };\n" + 
				"      ArrayList<String[]> al = new ArrayList<String[]>(Arrays.asList(arr));\n" + 
				"   }\n" + 
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83032
	public void test022() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	String[] args;\n" +
				"	public X(String... args) {\n" +
				"		this.args = args;\n" +
				"	}\n" +
				"	public static X foo() {\n" +
				"		return new X(\"SU\", \"C\", \"CE\", \"SS\"){};\n" +
				"	}\n" +
				"	public String bar() {\n" +
				"		if (this.args != null) {\n" +
				"			StringBuffer buffer = new StringBuffer();\n" +
				"			for (String s : this.args) {\n" +
				"				buffer.append(s);\n" +
				"			}\n" +
				"			return String.valueOf(buffer);\n" +
				"		}\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(foo().bar());\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83536
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main (String[] args) {\n" + 
				"        new X().test (new byte[5]);\n" + 
				"		 System.out.print(\"SUCCESS\");\n" +
				"    }\n" + 
				"    private void test (Object... params) {\n" + 
				"    }\n" + 
				"}",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87042
	public void test024() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static boolean foo(Object... args) {\n" + 
				"		return args == null;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(foo(null, null));\n" + 
				"	}\n" + 
				"}",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87042
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static boolean foo(Object... args) {\n" + 
				"		return args == null;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(foo(null));\n" + 
				"	}\n" + 
				"}",
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87318
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"	static void foo(int[] intarray) {\n" + 
				"		List<int[]> l = Arrays.asList(intarray);\n" + 
				"		System.out.print(l.get(0).length);\n" + 
				"	}\n" + 
				"	static void foo(String[] strarray) {\n" + 
				"		List l = Arrays.asList(strarray);\n" + 
				"		System.out.print(l);\n" + 
				"	}	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		foo(new int[]{0, 1});\n" + 
				"		foo(new String[]{\"a\",\"b\"});\n" + 
				"		System.out.println(\"done\");\n" + 
				"	}\n" + 
				"}\n",
			},
			"2[a, b]done");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87900
	public void test027() { // ensure AccVarargs does not collide
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	transient private X() {}\n" + 
				"	void test() { X x = new X(); }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	transient private X() {}\r\n" + 
			"	                  ^^^\n" + 
			"Illegal modifier for the method X.X()\n" + 
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	transient private X(Object... o) {}\n" + 
				"	void test() { X x = new X(1, 2); }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	transient private X(Object... o) {}\n" + 
			"	                  ^^^^^^^^^^^^^^\n" + 
			"Illegal modifier for the method X.X()\n" + 
			"----------\n"
		);
	}
}
