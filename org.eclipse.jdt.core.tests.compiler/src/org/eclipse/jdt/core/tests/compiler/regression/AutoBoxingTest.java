package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class AutoBoxingTest extends AbstractComparisonTest {

	public AutoBoxingTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 0 };
//		TESTS_RANGE = new int[] { 11, -1 };
//	}
	public static Test suite() {
		return buildTestSuite(testClass());
	}
	
	public static Class testClass() {
		return AutoBoxingTest.class;
	}
// (TODO) kent - needs to be added to TestAll when support is released
	public void test001() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(int i) { System.out.print('n'); }\n" +
				"	static void test(int... i) { System.out.print('n'); }\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private void test(int i) { System.out.print('n'); }\n" +
				"	void test(int... i) { System.out.print('n'); }\n" +
				"	public void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test002() { // convert Byte to long?
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(new Byte((byte) 1));\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(long i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test003() { // this is NOT an ambiguous case as 'long' is matched before autoboxing kicks in
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i) { System.out.print('n'); }\n" +
				"	void test(long i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test004() {
		this.runNegativeTest( // Integers are not compatible with Longs, even though ints are compatible with longs
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new Y().test(1, 1);\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Long, int) in the type Y is not applicable for the arguments (int, int)\n" + 
			"----------\n"
			// test(java.lang.Long,int) in Y cannot be applied to (int,int)
		);
		this.runNegativeTest( // likewise with Byte and Integer
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test((byte) 1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new Y().test((byte) 1, 1);\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Integer, int) in the type Y is not applicable for the arguments (byte, int)\n" + 
			"----------\n"
			// test(java.lang.Integer,int) in Y cannot be applied to (byte,int)
		);
	}

	public void test005() {
		this.runConformTest( // this is NOT an ambiguous case as Long is not a match for int
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"	void test(long i, Integer j) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test006() { // test autoboxing AND varargs method match
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1, new Integer(2), -3);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(int ... i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test007() {
		this.runNegativeTest( // 2 of these sends are ambiguous
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" + // reference to test is ambiguous, both method test(java.lang.Integer,int) in Y and method test(int,java.lang.Integer) in Y match
				"		new Y().test(new Integer(1), new Integer(1));\n" + // reference to test is ambiguous
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) {}\n" +
				"	void test(int i, Integer j) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new Y().test(1, 1);\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Integer, int) is ambiguous for the type Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	new Y().test(new Integer(1), new Integer(1));\r\n" + 
			"	        ^^^^\n" + 
			"The method test(Integer, int) is ambiguous for the type Y\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(new Integer(1), 1);\n" +
				"		new Y().test(1, new Integer(1));\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print(1); }\n" +
				"	void test(int i, Integer j) { System.out.print(2); }\n" +
				"}\n",
			},
			"12"
		);
	}
	public void test008() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(int i) { System.out.print('n'); }\n" +
				"	static void test(int... i) { System.out.print('n'); }\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private void test(int i) { System.out.print('n'); }\n" +
				"	void test(int... i) { System.out.print('n'); }\n" +
				"	public void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static byte bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(byte i) { System.out.print('n'); }\n" +
				"	static void test(byte... i) { System.out.print('n'); }\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static char bar() {return 'c';}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(char i) { System.out.print('n'); }\n" +
				"	static void test(char... i) { System.out.print('n'); }\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static float bar() {return 0.0f;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(float f) { System.out.print('n'); }\n" +
				"	static void test(float... f) { System.out.print('n'); }\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static double bar() {return 0.0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(double d) { System.out.print('n'); }\n" +
				"	static void test(double... d) { System.out.print('n'); }\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static long bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(long l) { System.out.print('n'); }\n" +
				"	static void test(long... l) { System.out.print('n'); }\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static short bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(short s) { System.out.print('n'); }\n" +
				"	static void test(short... s) { System.out.print('n'); }\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean bar() {return true;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(boolean b) { System.out.print('n'); }\n" +
				"	static void test(boolean... b) { System.out.print('n'); }\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}
	public void test009() { // local declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int i = Y.test();\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Object o = Y.test();\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test010() { // field declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int i = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Object o = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}
	public void test011() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(int i) { System.out.print('n'); }\n" +
				"	static void test(int... i) { System.out.print('n'); }\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private void test(int i) { System.out.print('n'); }\n" +
				"	void test(int... i) { System.out.print('n'); }\n" +
				"	public void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static byte bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(byte i) { System.out.print('n'); }\n" +
				"	static void test(byte... i) { System.out.print('n'); }\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static char bar() {return 'c';}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(char i) { System.out.print('n'); }\n" +
				"	static void test(char... i) { System.out.print('n'); }\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static float bar() {return 0.0f;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(float f) { System.out.print('n'); }\n" +
				"	static void test(float... f) { System.out.print('n'); }\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static double bar() {return 0.0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(double d) { System.out.print('n'); }\n" +
				"	static void test(double... d) { System.out.print('n'); }\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static long bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(long l) { System.out.print('n'); }\n" +
				"	static void test(long... l) { System.out.print('n'); }\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static short bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(short s) { System.out.print('n'); }\n" +
				"	static void test(short... s) { System.out.print('n'); }\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean bar() {return true;}\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(bar());\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(boolean b) { System.out.print('n'); }\n" +
				"	static void test(boolean... b) { System.out.print('n'); }\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}
}