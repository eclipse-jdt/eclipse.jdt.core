package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class VarargTest extends AbstractComparisonTest {

	public VarargTest(String name) {
		super(name);
	}

	public static Test suite() {
		return setupSuite(testClass());
	}
	
	public static Class testClass() {
		return VarargTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		\n" +
				"		Y.count();\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2, (byte) 3, 4);\n" +
				"		Y.count(new int[] {1, 2, 3, 4 });\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public Y(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"	public static void count(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"}\n",
			},
			"0 1 10 10 0 1 10 10");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		\n" +
				"		Y.count();\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2, (byte) 3, 4);\n" +
				"		Y.count(new int[] {1, 2, 3, 4 });\n" +
				"	}\n" +
				"}\n",
			},
			"0 1 10 10 0 1 10 10",
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
				"		Y.count();\n" +
				"		Y.count(new int[] {1});\n" +
				"		Y.count(new int[] {1, 2}, new int[] {3, 4});\n" +
				"		Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int count(int[] values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print(result);\n" +
				"		return result;\n" +
				"	}\n" +
				"	public static void count(int[] ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values.length; i < l; i++)\n" +
				"			result += count(values[i]);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(result);\n" +
				"	}\n" +
				"}\n",
			},
			"=0 1 3 7=10 6 4=10");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count();\n" +
				"		Y.count(new int[] {1});\n" +
				"		Y.count(new int[] {1, 2}, new int[] {3, 4});\n" +
				"		Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});\n" +
				"	}\n" +
				"}\n"
			},
			"=0 1 3 7=10 6 4=10",
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
				"		Y.count(1);\n" +
				"		Y.count(2, new int[] {1});\n" +
				"		Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});\n" +
				"		Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int count(int j, int[] values) {\n" +
				"		int result = j;\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print('[');\n" +
				"		for (int i = 0, l = values.length; i < l; i++)\n" +
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
				"		for (int i = 0, l = values.length; i < l; i++)\n" +
				"			result += count(j, values[i]);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(result);\n" +
				"	}\n" +
				"}\n",
			},
			"1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(1);\n" +
				"		Y.count(2, new int[] {1});\n" +
				"		Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});\n" +
				"		Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});\n" +
				"	}\n" +
				"}\n"
			},
			"1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26",
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
				"		Y.print();\n" +
				"		Y.print(new Integer(1));\n" +
				"		Y.print(new Integer(1), new Byte((byte) 3), new Integer(7));\n" +
				"		Y.print(new Integer[] {new Integer(11) });\n" +
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
			", 1, 1 3 7, 11");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.print();\n" +
				"		Y.print(new Integer(1));\n" +
				"		Y.print(new Integer(1), new Byte((byte) 3), new Integer(7));\n" +
				"		Y.print(new Integer[] {new Integer(11) });\n" +
				"	}\n" +
				"}\n",
			},
			", 1, 1 3 7, 11",
			null,
			false,
			null);
	}

	public void test005() { // 70056
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
	// TODO (kent) split in smaller test cases
	public void test006() { // array dimension test compatibility with Object
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
// TODO (kent) autoboxing				"		Y.object((byte) 1);\n" +
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
			"Inexact argument for the varargs method byte2(byte[][]) from the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Y.byte2((byte) 1);\n" + 
			"	  ^^^^^\n" + 
			"The method byte2(byte[][]) in the type Y is not applicable for the arguments (byte)\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	Y.byte2(new byte[][][] {});\n" + 
			"	  ^^^^^\n" + 
			"The method byte2(byte[][]) in the type Y is not applicable for the arguments (byte[][][])\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 9)\n" + 
			"	Y.object(null);\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object(Object[]) from the type Y\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 11)\n" + 
			"	Y.object(new byte[][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object(Object[]) from the type Y\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 12)\n" + 
			"	Y.object(new byte[][][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object(Object[]) from the type Y\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 15)\n" + 
			"	Y.object(new String[] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object(Object[]) from the type Y\n" + 
			"----------\n" + 
			"8. WARNING in X.java (at line 16)\n" + 
			"	Y.object(new String[][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object(Object[]) from the type Y\n" + 
			"----------\n" + 
			"9. WARNING in X.java (at line 18)\n" + 
			"	Y.object2(null);\n" + 
			"	^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object2(Object[][]) from the type Y\n" + 
			"----------\n" + 
			"10. ERROR in X.java (at line 19)\n" + 
			"	Y.object2((byte) 1);\n" + 
			"	  ^^^^^^^\n" + 
			"The method object2(Object[][]) in the type Y is not applicable for the arguments (byte)\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 20)\n" + 
			"	Y.object2(new byte[] {});\n" + 
			"	  ^^^^^^^\n" + 
			"The method object2(Object[][]) in the type Y is not applicable for the arguments (byte[])\n" + 
			"----------\n" + 
			"12. WARNING in X.java (at line 22)\n" + 
			"	Y.object2(new byte[][][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object2(Object[][]) from the type Y\n" + 
			"----------\n" + 
			"13. ERROR in X.java (at line 24)\n" + 
			"	Y.object2(new String());\n" + 
			"	  ^^^^^^^\n" + 
			"The method object2(Object[][]) in the type Y is not applicable for the arguments (String)\n" + 
			"----------\n" + 
			"14. WARNING in X.java (at line 26)\n" + 
			"	Y.object2(new String[][] {});\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method object2(Object[][]) from the type Y\n" + 
			"----------\n" + 
			"15. WARNING in X.java (at line 28)\n" + 
			"	Y.string(null);\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the varargs method string(String[]) from the type Y\n" + 
			"----------\n" + 
			"16. ERROR in X.java (at line 31)\n" + 
			"	Y.string(new String[][] {});\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String[]) in the type Y is not applicable for the arguments (String[][])\n" + 
			"----------\n" + 
			"17. ERROR in X.java (at line 33)\n" + 
			"	Y.string(new Object());\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String[]) in the type Y is not applicable for the arguments (Object)\n" + 
			"----------\n" + 
			"18. ERROR in X.java (at line 34)\n" + 
			"	Y.string(new Object[] {});\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String[]) in the type Y is not applicable for the arguments (Object[])\n" + 
			"----------\n" + 
			"19. ERROR in X.java (at line 35)\n" + 
			"	Y.string(new Object[][] {});\n" + 
			"	  ^^^^^^\n" + 
			"The method string(String[]) in the type Y is not applicable for the arguments (Object[][])\n" + 
			"----------\n");
	}

	public void test007() {
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
			"Inexact argument for the vararg constructor Y(boolean, Object[])\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	y = new Y(\'i\', null);\n" + 
			"	    ^^^^^^^^^^^^^^^^\n" + 
			"Inexact argument for the vararg constructor Y(char, int[][])\n" + 
			"----------\n");
	}
	// check overloading varargs method with non varargs one
	// check inexact argument for explicit constructor call scenario
}
