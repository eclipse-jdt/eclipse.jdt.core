/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class StaticImportTest extends AbstractComparisonTest {

	public StaticImportTest(String name) {
		super(name);
	}

	public static Test suite() {
		return setupSuite(testClass());
	}
	
	public static Class testClass() {
		return StaticImportTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.Math.*;\n" +
				"import static java.lang.Math.PI;\n" +
				"public class X { double pi = abs(PI); }\n",
			},
			"");
	}

	public void test002() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p2.Y.*;\n" +
				"import static p2.Z.Zint;\n" +
				"import static p2.Z.ZMember;\n" +
				"public class X {\n" +
				"	int x = y(1);\n" +
				"	int y = Yint;\n" +
				"	int z = Zint;\n" +
				"	void m1(YMember m) {}\n" +
				"	void m2(ZMember m) {}\n" +
				"}\n",
				"p2/Y.java",
				"package p2;\n" +
				"public class Y {\n" +
				"	public static int Yint = 1;\n" +
				"	public static int y(int y) { return y; }\n" +
				"	public static class YMember {}\n" +
				"}\n",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z {\n" +
				"	public static int Zint = 1;\n" +
				"	public static class ZMember {}\n" +
				"}\n",
			},
			"");
	}

	public void test003() { // test inheritance
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p2.Y.*;\n" +
				"import static p2.Z.Zint;\n" +
				"import static p2.Z.ZMember;\n" +
				"public class X {\n" +
				"	int x = y(1);\n" +
				"	int y = Yint;\n" +
				"	int z = Zint;\n" +
				"	void m1(YMember m) {}\n" +
				"	void m2(ZMember m) {}\n" +
				"}\n",
				"p2/YY.java",
				"package p2;\n" +
				"public class YY {\n" +
				"	public static int Yint = 1;\n" +
				"	public static int y(int y) { return y; }\n" +
				"	public static class YMember {}\n" +
				"}\n",
				"p2/Y.java",
				"package p2;\n" +
				"public class Y extends YY {}\n",
				"p2/ZZ.java",
				"package p2;\n" +
				"public class ZZ {\n" +
				"	public static int Zint = 1;\n" +
				"	public static class ZMember {}\n" +
				"}\n",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z extends ZZ {}\n",
			},
			"");
	}

	public void test004() { // test static vs. instance
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p2.Y.*;\n" +
				"import static p2.Z.Zint;\n" +
				"import static p2.Z.ZMember;\n" +
				"public class X {\n" +
				"	int x = y(1);\n" +
				"	int y = Yint;\n" +
				"	int z = Zint;\n" +
				"	void m1(YMember m) {}\n" +
				"	void m2(ZMember m) {}\n" +
				"}\n",
				"p2/Y.java",
				"package p2;\n" +
				"public class Y {\n" +
				"	public int Yint = 1;\n" +
				"	public int y(int y) { return y; }\n" +
				"	public class YMember {}\n" +
				"}\n",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z {\n" +
				"	public int Zint = 1;\n" +
				"	public class ZMember {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 3)\n" + 
			"	import static p2.Z.Zint;\n" + 
			"	              ^^^^^^^^^\n" + 
			"The import p2.Z.Zint cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in p\\X.java (at line 4)\n" + 
			"	import static p2.Z.ZMember;\n" + 
			"	              ^^^^^^^^^^^^\n" + 
			"The import p2.Z.ZMember cannot be resolved\n" + 
			"----------\n" + 
			"3. ERROR in p\\X.java (at line 6)\n" + 
			"	int x = y(1);\n" + 
			"	        ^\n" + 
			"The method y(int) is undefined for the type X\n" + 
			"----------\n" + 
			"4. ERROR in p\\X.java (at line 7)\n" + 
			"	int y = Yint;\n" + 
			"	        ^^^^\n" + 
			"Yint cannot be resolved\n" + 
			"----------\n" + 
			"5. ERROR in p\\X.java (at line 8)\n" + 
			"	int z = Zint;\n" + 
			"	        ^^^^\n" + 
			"Zint cannot be resolved\n" + 
			"----------\n" + 
			"6. ERROR in p\\X.java (at line 10)\n" + 
			"	void m2(ZMember m) {}\n" + 
			"	        ^^^^^^^\n" + 
			"ZMember cannot be resolved to a type\n" + 
			"----------\n");
	}

	public void test005() { // test visibility
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p2.Y.*;\n" +
				"import static p2.Z.Zint;\n" +
				"import static p2.Z.ZMember;\n" +
				"public class X {\n" +
				"	int x = y(1);\n" +
				"	int y = Yint;\n" +
				"	int z = Zint;\n" +
				"	void m1(YMember m) {}\n" +
				"	void m2(ZMember m) {}\n" +
				"}\n",
				"p2/Y.java",
				"package p2;\n" +
				"public class Y {\n" +
				"	static int Yint = 1;\n" +
				"	static int y(int y) { return y; }\n" +
				"	static class YMember {}\n" +
				"}\n",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z {\n" +
				"	static int Zint = 1;\n" +
				"	static class ZMember {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 2)\n" + 
			"	import static p2.Y.*;\n" + 
			"	              ^^^^\n" + 
			"The import p2.Y is never used\n" + 
			"----------\n" + 
			"2. ERROR in p\\X.java (at line 3)\n" + 
			"	import static p2.Z.Zint;\n" + 
			"	              ^^^^^^^^^\n" + 
			"The import p2.Z.Zint cannot be resolved\n" + 
			"----------\n" + 
			"3. ERROR in p\\X.java (at line 4)\n" + 
			"	import static p2.Z.ZMember;\n" + 
			"	              ^^^^^^^^^^^^\n" + 
			"The type p2.Z.ZMember is not visible\n" + 
			"----------\n" + 
			"4. ERROR in p\\X.java (at line 6)\n" + 
			"	int x = y(1);\n" + 
			"	        ^\n" + 
			"The method y(int) from the type Y is not visible\n" + 
			"----------\n" + 
			"5. ERROR in p\\X.java (at line 7)\n" + 
			"	int y = Yint;\n" + 
			"	        ^^^^\n" + 
			"The field Yint is not visible\n" + 
			"----------\n" + 
			"6. ERROR in p\\X.java (at line 8)\n" + 
			"	int z = Zint;\n" + 
			"	        ^^^^\n" + 
			"Zint cannot be resolved\n" + 
			"----------\n" + 
			"7. ERROR in p\\X.java (at line 9)\n" + 
			"	void m1(YMember m) {}\n" + 
			"	        ^^^^^^^\n" + 
			"The type YMember is not visible\n" + 
			"----------\n" + 
			"8. ERROR in p\\X.java (at line 10)\n" + 
			"	void m2(ZMember m) {}\n" + 
			"	        ^^^^^^^\n" + 
			"ZMember cannot be resolved to a type\n" + 
			"----------\n");
	}

	public void test006() { // test non static member types
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p2.Z.ZStatic;\n" +
				"import static p2.Z.ZNonStatic;\n" +
				"import p2.Z.ZNonStatic;\n" +
				"public class X {\n" +
				"	void m2(ZStatic m) {}\n" +
				"	void m3(ZNonStatic m) {}\n" +
				"}\n",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z {\n" +
				"	public static class ZStatic {}\n" +
				"	public class ZNonStatic {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 3)\n" + 
			"	import static p2.Z.ZNonStatic;\n" + 
			"	              ^^^^^^^^^^^^^^^\n" + 
			"The import p2.Z.ZNonStatic cannot be resolved\n" + 
			"----------\n");
	}

	public void test007() { // test non static member types vs. static field
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p2.Z.ZFieldOverMember;\n" +
				"public class X {\n" +
				"	int z = ZFieldOverMember;\n" +
				"}\n",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z {\n" +
				"	public static int ZFieldOverMember = 1;\n" +
				"	public class ZFieldOverMember {}\n" +
				"}\n",
			},
			"");
	}

	public void test008() { // test static top level types
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static java.lang.System;\n" +
				"public class X {}\n",
			},
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 2)\n" + 
			"	import static java.lang.System;\n" + 
			"	              ^^^^^^^^^^^^^^^^\n" + 
			"The static import java.lang.System must be a field or member type\n" + 
			"----------\n");
	}

	public void test009() { // test static top level types
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static java.lang.reflect.Method.*;\n" +
				"public class X {Method m;}\n",
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 2)\n" + 
			"	import static java.lang.reflect.Method.*;\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import java.lang.reflect.Method is never used\n" + 
			"----------\n" + 
			"2. ERROR in p\\X.java (at line 3)\n" + 
			"	public class X {Method m;}\n" + 
			"	                ^^^^^^\n" + 
			"Method cannot be resolved to a type\n" + 
			"----------\n");
	}

	public void test010() { // 76174
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import static java.lang.System.*;\n" +
				"public class X {\n" +
				"	void foo() { arraycopy(); }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	import static java.lang.System.*;\n" + 
			"	              ^^^^^^^^^^^^^^^^\n" + 
			"The import java.lang.System is never used\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo() { arraycopy(); }\n" + 
			"	             ^^^^^^^^^\n" + 
			"The method arraycopy(Object, int, Object, int, int) in the type System is not applicable for the arguments ()\n" + 
			"----------\n");
	}

	public void test011() { // 76360
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import static p.Y.*;\n" +
				"public class X extends p.Z {}\n" +
				"class XX extends M.N {}\n" +
				"class XXX extends M.Missing {}\n",
				"p/YY.java",
				"package p;\n" +
				"public class YY {\n" +
				"	public static class M {\n" +
				"		public static class N {}\n" +
				"	}\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"public class Y extends YY {}\n",
				"p/Z.java",
				"package p;\n" +
				"public class Z {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	class XXX extends M.Missing {}\n" + 
			"	                  ^^^^^^^^^\n" + 
			"M.Missing cannot be resolved to a type\n" + 
			"----------\n");
	}
}
