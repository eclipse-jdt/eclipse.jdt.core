/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *								bug 185682 - Increment/decrement operators mark local variables as read
 *								bug 401271 - StackOverflowError when searching for a methods references
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class StaticImportTest extends AbstractComparableTest {

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test075" };
//		TESTS_NAMES = new String[] { "test085c" };
//		TESTS_NUMBERS = new int[] { 80 };
//		TESTS_RANGE = new int[] { 75, -1 };
	}

	public StaticImportTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return StaticImportTest.class;
	}


	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.Math.*;
					import static java.lang.Math.PI;
					public class X { double pi = abs(PI); }
					""",
			},
			"");
	}

	public void test002() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p2.Y.*;
					import static p2.Z.Zint;
					import static p2.Z.ZMember;
					public class X {
						int x = y(1);
						int y = Yint;
						int z = Zint;
						void m1(YMember m) {}
						void m2(ZMember m) {}
					}
					""",
				"p2/Y.java",
				"""
					package p2;
					public class Y {
						public static int Yint = 1;
						public static int y(int y) { return y; }
						public static class YMember {}
					}
					""",
				"p2/Z.java",
				"""
					package p2;
					public class Z {
						public static int Zint = 1;
						public static class ZMember {}
					}
					""",
			},
			"");
	}

	public void test003() { // test inheritance
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p2.Y.*;
					import static p2.Z.Zint;
					import static p2.Z.ZMember;
					public class X {
						int x = y(1);
						int y = Yint;
						int z = Zint;
						void m1(YMember m) {}
						void m2(ZMember m) {}
					}
					""",
				"p2/YY.java",
				"""
					package p2;
					public class YY {
						public static int Yint = 1;
						public static int y(int y) { return y; }
						public static class YMember {}
					}
					""",
				"p2/Y.java",
				"package p2;\n" +
				"public class Y extends YY {}\n",
				"p2/ZZ.java",
				"""
					package p2;
					public class ZZ {
						public static int Zint = 1;
						public static class ZMember {}
					}
					""",
				"p2/Z.java",
				"package p2;\n" +
				"public class Z extends ZZ {}\n",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"import static p.A.C;\n" +
				"public class X { int i = C; }\n",
				"p/A.java",
				"""
					package p;
					public class A extends B implements I {}
					class B implements I {}
					""",
				"p/I.java",
				"package p;\n" +
				"public interface I { public static int C = 1; }\n"
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.C;
					public class X {\s
						int i = C;\s
						int j = p.A.C;\s
					}
					""",
				"p/A.java",
				"""
					package p;
					public class A implements I {}
					interface I { public static int C = 1; }
					"""
			},
			JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
	}

	public void test004() { // test static vs. instance
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p2.Y.*;
					import static p2.Z.Zint;
					import static p2.Z.ZMember;
					public class X {
						int x = y(1);
						int y = Yint;
						int z = Zint;
						void m1(YMember m) {}
						void m2(ZMember m) {}
					}
					""",
				"p2/Y.java",
				"""
					package p2;
					public class Y {
						public int Yint = 1;
						public int y(int y) { return y; }
						public class YMember {}
					}
					""",
				"p2/Z.java",
				"""
					package p2;
					public class Z {
						public int Zint = 1;
						public class ZMember {}
					}
					""",
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					import static p2.Z.Zint;
					              ^^^^^^^^^
				The import p2.Z.Zint cannot be resolved
				----------
				2. ERROR in p\\X.java (at line 4)
					import static p2.Z.ZMember;
					              ^^^^^^^^^^^^
				The import p2.Z.ZMember cannot be resolved
				----------
				3. ERROR in p\\X.java (at line 6)
					int x = y(1);
					        ^
				The method y(int) is undefined for the type X
				----------
				4. ERROR in p\\X.java (at line 7)
					int y = Yint;
					        ^^^^
				Yint cannot be resolved to a variable
				----------
				5. ERROR in p\\X.java (at line 8)
					int z = Zint;
					        ^^^^
				Zint cannot be resolved to a variable
				----------
				6. ERROR in p\\X.java (at line 9)
					void m1(YMember m) {}
					        ^^^^^^^
				YMember cannot be resolved to a type
				----------
				7. ERROR in p\\X.java (at line 10)
					void m2(ZMember m) {}
					        ^^^^^^^
				ZMember cannot be resolved to a type
				----------
				""");
	}

	public void test005() { // test visibility
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p2.Y.*;
					import static p2.Z.Zint;
					import static p2.Z.ZMember;
					public class X {
						int x = y(1);
						int y = Yint;
						int z = Zint;
						void m1(YMember m) {}
						void m2(ZMember m) {}
					}
					""",
				"p2/Y.java",
				"""
					package p2;
					public class Y {
						static int Yint = 1;
						static int y(int y) { return y; }
						static class YMember {}
					}
					""",
				"p2/Z.java",
				"""
					package p2;
					public class Z {
						static int Zint = 1;
						static class ZMember {}
					}
					""",
			},
		"""
			----------
			1. ERROR in p\\X.java (at line 3)
				import static p2.Z.Zint;
				              ^^^^^^^^^
			The field Z.p2.Z.Zint is not visible
			----------
			2. ERROR in p\\X.java (at line 4)
				import static p2.Z.ZMember;
				              ^^^^^^^^^^^^
			The type p2.Z.ZMember is not visible
			----------
			3. ERROR in p\\X.java (at line 6)
				int x = y(1);
				        ^
			The method y(int) from the type Y is not visible
			----------
			4. ERROR in p\\X.java (at line 7)
				int y = Yint;
				        ^^^^
			The field Y.Yint is not visible
			----------
			5. ERROR in p\\X.java (at line 8)
				int z = Zint;
				        ^^^^
			Zint cannot be resolved to a variable
			----------
			6. ERROR in p\\X.java (at line 9)
				void m1(YMember m) {}
				        ^^^^^^^
			The type YMember is not visible
			----------
			7. ERROR in p\\X.java (at line 10)
				void m2(ZMember m) {}
				        ^^^^^^^
			ZMember cannot be resolved to a type
			----------
			""");
	}

	public void test006() { // test non static member types
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p2.Z.ZStatic;
					import static p2.Z.ZNonStatic;
					import p2.Z.ZNonStatic;
					public class X {
						void m2(ZStatic m) {}
						void m3(ZNonStatic m) {}
					}
					""",
				"p2/Z.java",
				"""
					package p2;
					public class Z {
						public static class ZStatic {}
						public class ZNonStatic {}
					}
					""",
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					import static p2.Z.ZNonStatic;
					              ^^^^^^^^^^^^^^^
				The import p2.Z.ZNonStatic cannot be resolved
				----------
				""");
	}

	public void test007() { // test non static member types vs. static field
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p2.Z.ZFieldOverMember;
					public class X {
						int z = ZFieldOverMember;
					}
					""",
				"p2/Z.java",
				"""
					package p2;
					public class Z {
						public static int ZFieldOverMember = 1;
						public class ZFieldOverMember {}
					}
					""",
			},
			"");
	}

	public void test008() { // test static top level types
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static java.lang.System;
					public class X {}
					""",
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 2)
					import static java.lang.System;
					              ^^^^^^^^^^^^^^^^
				The static import java.lang.System must be a field or member type
				----------
				""");
	}

	public void test009() { // test static top level types
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static java.lang.reflect.Method.*;
					public class X {Method m;}
					""",
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					public class X {Method m;}
					                ^^^^^^
				Method cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76174
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.System.*;
					public class X {
						void foo() { arraycopy(); }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					void foo() { arraycopy(); }
					             ^^^^^^^^^
				The method arraycopy(Object, int, Object, int, int) in the type System is not applicable for the arguments ()
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76360
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.Y.*;
					public class X extends p.Z {}
					class XX extends M.N {}
					class XXX extends M.Missing {}
					""",
				"p/YY.java",
				"""
					package p;
					public class YY {
						public static class M {
							public static class N {}
						}
					}
					""",
				"p/Y.java",
				"package p;\n" +
				"public class Y extends YY {}\n",
				"p/Z.java",
				"package p;\n" +
				"public class Z {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					class XXX extends M.Missing {}
					                  ^^^^^^^^^
				M.Missing cannot be resolved to a type
				----------
				""");
	}

	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.Math.*;
					public class X {
						public static void main(String[] s) {
							System.out.println(max(1, 2));
						}
					}
					""",
			},
			"2");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.Math.max;
					public class X {
						public static void main(String[] s) {
							System.out.println(max(1, 3));
						}
					}
					""",
			},
			"3");
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p1.C.F;
					import p2.*;
					public class X implements F {\
						int i = F();\
					}
					""",
				"p1/C.java",
				"""
					package p1;
					public class C {
						public static int F() { return 0; }
					}
					""",
				"p2/F.java",
				"package p2;\n" +
				"public interface F {}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77955
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import static p.Y.ZZ;\n" + // found if ZZ is static
				"import static p.Z.ZZ.WW;\n" + // found if WW is static
				"import static p.Z.Zz.WW;\n" + // found if WW is static
				"import static p.Z.Zz.*;\n" + // legal
				"import static p.Z.Zz.Zzz;\n" + // legal

				"import static p.Y.Zz;\n" + // Zz is not static
				"import static p.Z.Zz.WW.*;\n" + // import requires canonical name for p.W.WW

				"import p.Y.ZZ;\n" + // import requires canonical name for p.Z.ZZ
				"import static p.Y.ZZ.*;\n" + // import requires canonical name for p.Z.ZZ
				"import static p.Y.ZZ.WW;\n" + // import requires canonical name for p.Z.ZZ
				"import static p.Y.ZZ.WW.*;\n" + // import requires canonical name for p.W.WW
				"import static p.Y.ZZ.ZZZ;\n" + // import requires canonical name for p.Z.ZZ
				"import static p.Y.ZZ.WW.WWW;\n" + // import requires canonical name for p.W.WW
				"public class X {\n" +
				"	int i = Zzz + Zzzz;\n" +
				"	ZZ z;\n" +
				"	WW w;\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"public class Y extends Z {}\n",
				"p/Z.java",
				"""
					package p;
					public class Z {
						public class Zz extends W { public static final int Zzz = 0; public static final int Zzzz = 1; }
						public static class ZZ extends W { public static final int ZZZ = 0; }
					}
					""",
				"p/W.java",
				"""
					package p;
					public class W {
						public static class WW { public static final int WWW = 0; }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					import static p.Y.Zz;
					              ^^^^^^
				The import p.Y.Zz cannot be resolved
				----------
				2. ERROR in X.java (at line 7)
					import static p.Z.Zz.WW.*;
					              ^^^^^^^^^
				The import p.Z.Zz.WW cannot be resolved
				----------
				3. ERROR in X.java (at line 8)
					import p.Y.ZZ;
					       ^^^^^^
				The import p.Y.ZZ cannot be resolved
				----------
				4. ERROR in X.java (at line 9)
					import static p.Y.ZZ.*;
					              ^^^^^^
				The import p.Y.ZZ cannot be resolved
				----------
				5. ERROR in X.java (at line 10)
					import static p.Y.ZZ.WW;
					              ^^^^^^
				The import p.Y.ZZ cannot be resolved
				----------
				6. ERROR in X.java (at line 11)
					import static p.Y.ZZ.WW.*;
					              ^^^^^^
				The import p.Y.ZZ cannot be resolved
				----------
				7. ERROR in X.java (at line 12)
					import static p.Y.ZZ.ZZZ;
					              ^^^^^^
				The import p.Y.ZZ cannot be resolved
				----------
				8. ERROR in X.java (at line 13)
					import static p.Y.ZZ.WW.WWW;
					              ^^^^^^
				The import p.Y.ZZ cannot be resolved
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78056
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static p.Z.ZZ.ZZZ;\n" +
				"public class X {}\n",
				"p/Z.java",
				"""
					package p;
					public class Z {
						public class ZZ { public static final  int ZZZ = 0; }
					}
					""",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=78075
	public void test015() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import p.Z.*;
					import static p.Z.*;
					public class X { int i = COUNT; }
					""",
				"p/Z.java",
				"""
					package p;
					public class Z {
						public static final  int COUNT = 0;
					}
					""",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.Z.*;
					import p.Z.*;
					public class X { int i = COUNT; }
					""",
				"p/Z.java",
				"""
					package p;
					public class Z {
						public static final  int COUNT = 0;
					}
					""",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77630
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import static java.lang.*;\n" +
				"public class X {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import static java.lang.*;
					              ^^^^^^^^^
				Only a type can be imported. java.lang resolves to a package
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81724
	public void test017() {
		this.runConformTest(
			new String[] {
				"bug/A.java",
				"""
					package bug;
					import static bug.C.*;
					public class A {
					   private B b;
					}
					""",
				"bug/B.java",
				"""
					package bug;
					import static bug.C.*;
					public class B {
					}
					""",
				"bug/C.java",
				"""
					package bug;
					public class C {
					   private B b;
					}
					""",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81724 - variation
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"bug/A.java",
				"""
					package bug;
					import static bug.C.*;
					public class A {
					   private B b2 = b;
					}
					""",
				"bug/B.java",
				"""
					package bug;
					import static bug.C.*;
					public class B {
					}
					""",
				"bug/C.java",
				"""
					package bug;
					public class C {
					   private static B b;
					}
					""",
			},
			"""
				----------
				1. ERROR in bug\\A.java (at line 4)
					private B b2 = b;
					               ^
				The field C.b is not visible
				----------
				----------
				1. WARNING in bug\\B.java (at line 2)
					import static bug.C.*;
					              ^^^^^
				The import bug.C is never used
				----------
				----------
				1. WARNING in bug\\C.java (at line 3)
					private static B b;
					                 ^
				The value of the field C.b is not used
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81718
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.Math.PI;
					
					public class X {
					  boolean PI;
					  Zork z;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82754
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.Math.round;
					public class X {
					  void foo() { cos(0); }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					void foo() { cos(0); }
					             ^^^
				The method cos(int) is undefined for the type X
				----------
				"""	);
	}

	public void test021() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.B.foo;
					public class X {
					  void test() { foo(); }
					}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public static void foo() {} }\n",
				"p/B.java",
				"package p;\n" +
				"public class B extends A { }\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.B.foo;
					public class X {
					  void test() { foo(); }
					}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public void foo() {} }\n",
				"p/B.java",
				"package p;\n" +
				"public class B extends A { static void foo(int i) {} }\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import static p.B.foo;
					              ^^^^^^^
				The import p.B.foo cannot be resolved
				----------
				2. ERROR in X.java (at line 3)
					void test() { foo(); }
					              ^^^
				The method foo() is undefined for the type X
				----------
				"""
		);
	}

	public void test022() { // test field/method collisions
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.F;
					import static p.B.F;
					public class X {
						int i = F;
					}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public static class F {} }\n",
				"p/B.java",
				"package p;\n" +
				"public class B { public static int F = 2; }\n",
			},
			""
			// no collision between field and member type
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.F;
					import static p.B.F;
					public class X {
						int i = F + F();
					}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public static int F() { return 1; } }\n",
				"p/B.java",
				"package p;\n" +
				"public class B { public static int F = 2; }\n",
			},
			""
			// no collision between field and method
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.F;
					import static p.B.F;
					public class X {
						int i = F;
					}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public static int F = 1; }\n",
				"p/B.java",
				"package p;\n" +
				"public class B extends A {}\n",
			},
			""
			// no collision between 2 fields that are the same
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.A.F;
					import static p.B.F;
					public class X {
						int i = F;
					}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public static int F = 1; }\n",
				"p/B.java",
				"package p;\n" +
				"public class B { public static int F = 2; }\n",
			},
			this.complianceLevel < ClassFileConstants.JDK1_8 ?
					"""
						----------
						1. ERROR in X.java (at line 2)
							import static p.B.F;
							              ^^^^^
						The import p.B.F collides with another import statement
						----------
						""" :
						"""
							----------
							1. ERROR in X.java (at line 4)
								int i = F;
								        ^
							The field F is ambiguous
							----------
							"""
		);
	}

	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.C;
					public class X {
						public static void main(String[] args) {
							System.out.print(C);
							System.out.print(C());
						}
					}
					""",
				"p/A.java",
				"""
					package p;
					public class A {
						public static int C = 1;
						public static int C() { return C + 3; }
					}
					"""
			},
			"14"
		);
		this.runConformTest( // extra inheritance hiccup for method lookup
			new String[] {
				"X.java",
				"""
					import static p.A.C;
					public class X {
						public static void main(String[] args) {
							System.out.print(C);
							System.out.print(C());
						}
					}
					""",
				"p/A.java",
				"""
					package p;
					public class A extends B {
						public static int C() { return C + 3; }
					}
					""",
				"p/B.java",
				"""
					package p;
					public class B {
						public static int C = 1;
					}
					"""
			},
			"14"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83376
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"p/B.java",
				"""
					package p;
					import static p.A.m;
					import static p2.C.m;
					class A { static void m() {} }
					public class B { public static void main(String[] args) { m(); } }
					""",
				"p2/C.java",
				"package p2;\n" +
				"public class C { public static void m() {} }\n"
			},
			"""
				----------
				1. ERROR in p\\B.java (at line 5)
					public class B { public static void main(String[] args) { m(); } }
					                                                          ^
				The method m() is ambiguous for the type B
				----------
				"""
		);
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p.A.m;
					import static p.B.m;
					public class X { void test() { m(); } }
					class B extends A {}
					""",
				"p/A.java",
				"package p;\n" +
				"public class A { public static int m() { return 0; } }\n"
			},
			""
		);
	}

	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.Math.*;
					public class X {
						public static void main(String[] s) {
							System.out.print(max(PI, 4));
							new Runnable() {
								public void run() {
									System.out.println(max(PI, 5));
								}
							}.run();
						}
					}
					"""
			},
			"4.05.0"
		);
	}

	public void test026() { // ensure inherited problem fields do not stop package resolution
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Y { static void test() { java.lang.String.valueOf(0); } }\n" +
				"class Y { private String java; }\n"
			},
			""
		);
	}

	public void test027() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.ST.foo;
					public class X {
					\t
						foo bar;
					}
					""",
				"p/ST.java",
				"""
					package p;\s
					public class ST {
						public static int foo;
					}
					"""	,
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					foo bar;
					^^^
				foo cannot be resolved to a type
				----------
				""");
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87490
	public void test028() {
		this.runConformTest(
			new String[] {
				"p1/Z.java",//====================
				"""
					package p1;
					public class Z {
						public interface I {
						}
					}
					""",
				"q/Y.java",//====================
				"""
					package q;
					import static p.X.I;
					import static p1.Z.I;
					public class Y implements I {
					}
					""",
				"p/X.java",//====================
				"""
					package p;
					public enum X {
						I, J, K
					}
					"""	,
			},
			"");
		// recompile Y against binaries
		this.runConformTest(
			new String[] {
				"q/Y.java",//====================
				"""
					package q;
					import static p.X.I;
					import static p1.Z.I;
					public class Y implements I {
					}
					""",
			},
			"",
			null,
			false,
			null);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93913
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					import static p2.C.B;
					public class A extends B {
						void test() {\
							int i = B();
							B b = null;
							b.fooB();
							b.fooC();
							fooC();
						}
					}
					""",
				"p1/B.java",
				"""
					package p1;
					public class B {
						public void fooB() {}
					}
					""",
				"p2/C.java",
				"""
					package p2;
					public class C {
						public static class B { public void fooC() {} }
						public static int B() { return 0; }
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\A.java (at line 6)
					b.fooB();
					  ^^^^
				The method fooB() is undefined for the type C.B
				----------
				"""
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=94262
	public void test030() {
		this.runNegativeTest(
			new String[] {
				"p2/Test.java",
				"""
					package p2;
					import static p1.A.*;
					public class Test {
						Inner1 i; // not found
						Inner2 j;
					}
					""",
				"p1/A.java",
				"""
					package p1;
					public class A {
						public class Inner1 {}
						public static class Inner2 {}
					}
					""",
			},
			"""
				----------
				1. ERROR in p2\\Test.java (at line 4)
					Inner1 i; // not found
					^^^^^^
				Inner1 cannot be resolved to a type
				----------
				"""
		);
		this.runConformTest(
			new String[] {
				"p2/Test.java",
				"""
					package p2;
					import p1.A.*;
					import static p1.A.*;
					import static p1.A.*;
					public class Test {
						Inner1 i;
						Inner2 j;
					}
					""",
				"p1/A.java",
				"""
					package p1;
					public class A {
						public class Inner1 {}
						public static class Inner2 {}
					}
					""",
			},
			""
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=95909
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"PointRadius.java",
				"""
					import static java.lang.Math.sqrt;
					
					public class PointRadius {
					
						public static void main(String[] args) {
							double radius = 0;
							radius = sqrt(pondArea / Math.PI);
					
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in PointRadius.java (at line 7)
					radius = sqrt(pondArea / Math.PI);
					              ^^^^^^^^
				pondArea cannot be resolved to a variable
				----------
				""");
	}

	//http://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test032() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.*;
					import static p.B.*;
					public class X {
						public static void main(String[] args) {foo();}
					}
					""",
				"p/A.java",
				"""
					package p;\
					public class A {
						public static void foo() {System.out.print(false);}
					}
					""",
				"p/B.java",
				"""
					package p;\
					public class B extends A {
						public static void foo() {System.out.print(true);}
					}
					"""
			},
			"true");
	}

	//http://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test032b() {
		this.runNegativeTest(
			new String[] {
				"X2.java",
				"""
					import static p2.A.*;
					import static p2.B.*;
					public class X2 { void test() {foo();} }
					""",
				"p2/A.java",
				"""
					package p2;\
					public class A {
						public static void foo() {}
					}
					""",
				"p2/B.java",
				"""
					package p2;\
					public class B {
						public static void foo() {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X2.java (at line 3)
					public class X2 { void test() {foo();} }
					                               ^^^
				The method foo() is ambiguous for the type X2
				----------
				"""
		);
	}

	//http://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test032c() {
		this.runConformTest(
			new String[] {
				"X3.java",
				"""
					import static p3.A.*;
					import static p3.B.foo;
					public class X3 {
						public static void main(String[] args) {foo();}
					}
					""",
				"p3/A.java",
				"""
					package p3;\
					public class A {
						public static void foo() {System.out.print(false);}
					}
					""",
				"p3/B.java",
				"""
					package p3;\
					public class B {
						public static void foo() {System.out.print(true);}
					}
					"""
			},
			"true");
	}

	//http://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test032d() {
		this.runConformTest(
			new String[] {
				"X4.java",
				"""
					import static p4.A.foo;
					import static p4.B.*;
					public class X4 {
						public static void main(String[] args) {foo();}
					}
					""",
				"p4/A.java",
				"""
					package p4;\
					public class A {
						public static void foo() {System.out.print(true);}
					}
					""",
				"p4/B.java",
				"""
					package p4;\
					public class B extends A {
						public static void foo() {System.out.print(false);}
					}
					"""
			},
			"true");
	}

	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.*;
					import static p.B.*;
					public class X {
						public static void main(String[] args) {foo("aa");}
					}
					""",
				"p/A.java",
				"""
					package p;\
					public class A {
						public static <U> void foo(U u) {System.out.print(false);}
					}
					""",
				"p/B.java",
				"""
					package p;\
					public class B extends A {
						public static <V> void foo(String s) {System.out.print(true);}
					}
					"""
			},
			"true");
	}

	public void test033b() {
		this.runConformTest(
			new String[] {
				"X2.java",
				"""
					import static p2.A.*;
					import static p2.B.*;
					public class X2 {
						public static void main(String[] args) {foo("aa");}
					}
					""",
				"p2/A.java",
				"""
					package p2;\
					public class A {
						public static <U> void foo(String s) {System.out.print(true);}
					}
					""",
				"p2/B.java",
				"""
					package p2;\
					public class B extends A {
						public static <V> void foo(V v) {System.out.print(false);}
					}
					"""
			},
			"true");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104198
	public void test034() {
		this.runConformTest(
			new String[] {
				"test/AbstractTest.java",
				"""
					package test;
					public abstract class AbstractTest<Z> {
					 \s
					  public abstract MyEnum m(Z z);
					 \s
					  public enum MyEnum {
					    A,B
					  }
					}
					""",
				"test/X.java",
				"""
					package test;
					import static test.AbstractTest.MyEnum.*;
					public class X extends AbstractTest<String> {
					  @Override public MyEnum m(String s) {
					    return A;
					  }
					}
					"""
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117861
	public void test035() {
		this.runConformTest(
			new String[] {
				"Bug.java",
				"""
					import static java.lang.String.format;
					public class Bug extends p.TestCase {
						public static void main(String[] args) {
							String msg = "test";
							System.out.print(format(msg));
							System.out.print(format(msg, 1, 2));
						}
					}
					""",
				"p/TestCase.java",
				"""
					package p;
					public class TestCase {
						static String format(String message, Object expected, Object actual) {return null;}
					}
					"""
			},
			"testtest");
		this.runNegativeTest(
			new String[] {
				"C.java",
				"""
					class A {
						static class B { void foo(Object o, String s) {} }
						void foo(int i) {}
					}
					class C extends A.B {
						void test() { foo(1); }
					}
					"""
			},
			"""
				----------
				1. ERROR in C.java (at line 6)
					void test() { foo(1); }
					              ^^^
				The method foo(Object, String) in the type A.B is not applicable for the arguments (int)
				----------
				""");
		this.runNegativeTest(
			new String[] {
				"A.java",
				"""
					public class A {
					  void foo(int i, long j) {}
					  class B {
					    void foo() { foo(1, 1); }
					  }
					}""",
			},
			"""
				----------
				1. ERROR in A.java (at line 4)
					void foo() { foo(1, 1); }
					             ^^^
				The method foo() in the type A.B is not applicable for the arguments (int, int)
				----------
				"""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126564
	public void test036() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.A.CONSTANT_I;
					import static p.A.CONSTANT_B;
					public class X {
					  static int i = p.A.CONSTANT_I;
					  static int j = p.A.CONSTANT_B;
					  static int m = CONSTANT_I;
					  static int n = CONSTANT_B;
					}""",
				"p/A.java",
				"""
					package p;
					public class A extends B implements I {}
					interface I { int CONSTANT_I = 1; }
					class B { int CONSTANT_B = 1; }""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					import static p.A.CONSTANT_B;
					              ^^^^^^^^^^^^^^
				The field B.p.A.CONSTANT_B is not visible
				----------
				2. ERROR in X.java (at line 5)
					static int j = p.A.CONSTANT_B;
					                   ^^^^^^^^^^
				The field B.CONSTANT_B is not visible
				----------
				3. ERROR in X.java (at line 7)
					static int n = CONSTANT_B;
					               ^^^^^^^^^^
				CONSTANT_B cannot be resolved to a variable
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126564 - variation
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.A.CONSTANT_I;
					import static p.A.CONSTANT_B;
					public class X {
					  static int i = p.A.CONSTANT_I;
					  static int j = p.A.CONSTANT_B;
					  static int m = CONSTANT_I;
					  static int n = CONSTANT_B;
					}""",
				"p/A.java",
				"""
					package p;
					public class A extends B implements I {}
					interface I { int CONSTANT_I = 1; }
					class B { public static int CONSTANT_B = 1; }""",
			},
			JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126564 - variation
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.A.foo_I;
					import static p.A.foo_B;
					public class X {
					  static int i = p.A.foo_I();
					  static int j = p.A.foo_B();
					  static int m = foo_I();
					  static int n = foo_B();
					}""",
				"p/A.java",
				"""
					package p;
					public abstract class A extends B implements I {}
					interface I { int foo_I(); }
					class B { int foo_B() { return 2;} }""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import static p.A.foo_I;
					              ^^^^^^^^^
				The import p.A.foo_I cannot be resolved
				----------
				2. ERROR in X.java (at line 2)
					import static p.A.foo_B;
					              ^^^^^^^^^
				The import p.A.foo_B cannot be resolved
				----------
				3. ERROR in X.java (at line 4)
					static int i = p.A.foo_I();
					               ^^^^^^^^^^^
				Cannot make a static reference to the non-static method foo_I() from the type I
				----------
				4. ERROR in X.java (at line 5)
					static int j = p.A.foo_B();
					                   ^^^^^
				The method foo_B() from the type B is not visible
				----------
				5. ERROR in X.java (at line 6)
					static int m = foo_I();
					               ^^^^^
				The method foo_I() is undefined for the type X
				----------
				6. ERROR in X.java (at line 7)
					static int n = foo_B();
					               ^^^^^
				The method foo_B() is undefined for the type X
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126564 - variation
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.A.foo_I;
					import static p.A.foo_B;
					public class X {
					  static int i = p.A.foo_I();
					  static int j = p.A.foo_B();
					  static int m = foo_I();
					  static int n = foo_B();
					}""",
				"p/A.java",
				"""
					package p;
					public abstract class A extends B implements I {}
					interface I { int foo_I(); }
					class B { public static int foo_B() { return 2;} }""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import static p.A.foo_I;
					              ^^^^^^^^^
				The import p.A.foo_I cannot be resolved
				----------
				2. ERROR in X.java (at line 4)
					static int i = p.A.foo_I();
					               ^^^^^^^^^^^
				Cannot make a static reference to the non-static method foo_I() from the type I
				----------
				3. ERROR in X.java (at line 6)
					static int m = foo_I();
					               ^^^^^
				The method foo_I() is undefined for the type X
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87490 - variation
	public void test040() {
		this.runConformTest(
			new String[] {
				"p1/Z.java",//====================
				"""
					package p1;
					public class Z {
						public interface I {
						}
					}
					""",
				"q/Y.java",//====================
				"""
					package q;
					import static p.X.foo;
					import static p1.Z.I;
					public class Y implements I {
					}
					""",
				"p/X.java",//====================
				"""
					package p;
					public class X {
						public static void foo() {}
					}
					"""	,
			},
			"");
		// recompile Y against binaries
		this.runConformTest(
			new String[] {
				"q/Y.java",//====================
				"""
					package q;
					import static p.X.foo;
					import static p1.Z.I;
					public class Y implements I {
					}
					""",
			},
			"",
			null,
			false,
			null);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134118
	public void test041() {
		this.runConformTest(
			true,
			new String[] {
				"Test.java",
				"""
					import static p.I.*;
					import static p.J.*;
					public class Test {
						int i = Constant;
					}
					""",
				"p/I.java",
				"package p;\n" +
				"public interface I { static int Constant = 1; }\n",
				"p/J.java",
				"package p;\n" +
				"public interface J extends I {}\n"	,
			},
			"""
				----------
				1. WARNING in Test.java (at line 2)
					import static p.J.*;
					              ^^^
				The import p.J is never used
				----------
				""",
			null,
			null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133737
	public void test042() {
		this.runNegativeTest(
			new String[] {
				"ImportTest.java",
				"""
					import static p.ArrayTest.toString2;
					public class ImportTest extends SuperTest {
						public static void main(String[] args) { printArgs(1, 2, 3, 4, 5); }
						static void printArgs(Object... args) { toString2(args); }
					}
					class SuperTest {
						static void toString2() {}
					}
					""",
				"p/ArrayTest.java",
				"""
					package p;
					public class ArrayTest {
						public static void toString2(String[] args) {}
					}
					""",
			},
			"""
				----------
				1. ERROR in ImportTest.java (at line 4)
					static void printArgs(Object... args) { toString2(args); }
					                                        ^^^^^^^^^
				The method toString2() in the type SuperTest is not applicable for the arguments (Object[])
				----------
				"""
		);
		this.runNegativeTest(
			new String[] {
				"ImportTest.java",
				"""
					import static java.util.Arrays.toString;
					public class ImportTest {
						public static void main(String[] args) { printArgs(1, 2, 3, 4, 5); }
						static void printArgs(Object... args) { toString(args); }
					}
					"""
			},
			"""
				----------
				1. ERROR in ImportTest.java (at line 4)
					static void printArgs(Object... args) { toString(args); }
					                                        ^^^^^^^^
				The method toString() in the type Object is not applicable for the arguments (Object[])
				----------
				"""
		);
	}
	public void test042b() {
		this.runConformTest(
			new String[] {
				"ImportTest.java",
				"""
					import static p.DefinesFoo.foo;
					public class ImportTest extends SuperImportTest {
						void test() { foo("fails?"); }
					}
					class SuperImportTest {
						private void foo() {}
					}
					""",
				"p/DefinesFoo.java",
				"""
					package p;
					public class DefinesFoo {
						public static void foo(String s) {}
					}
					""",
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129388
	public void test043() {
		this.runConformTest(
			new String[] {
				"B.java",
				"""
					import static java.lang.String.format;
					public class B extends p.A {
						void test() { format("fails?"); }
						void test2() { format("fails?", null); }
						void test3() { format("fails?", null, null); }
						void test4() { format("fails?", null, null, null); }
					}
					""",
				"p/A.java",
				"""
					package p;
					public class A {
						static String format(String message, Object expected, Object actual) { return null; }
					}
					""",
			},
			""
		);
	}
	// names potential confusion
	public void test044() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p.X.B.E;
					import static p.X.B.*;
					
					public class X {
					  public static class Y {
					    public enum E { FOO; }
					    public static Object E() { return null; }
					    public enum F { FOO; }
					    public static Object F() { return null; }
					  }
					  public static class B extends Y {}
					  Object f1 = E.FOO;
					  Object f2 = E();
					  Object f3 = F.FOO;
					  Object f4 = F();
					}
					""",
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142772
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import static test.Y.arrayList;\n" +
				"public class X { static void arrayList(int x) { arrayList(); } }\n",
				"test/Y.java",
				"package test;\n" +
				"public class Y { public static void arrayList() {} }\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public class X { static void arrayList(int x) { arrayList(); } }
					                                                ^^^^^^^^^
				The method arrayList(int) in the type X is not applicable for the arguments ()
				----------
				"""
		);
	}
	public void test045b() {
		this.runNegativeTest(
			new String[] {
				"test/One.java",
				"package test;\n" +
				"public class One { public static void arrayList(String s) {} }\n",
				"test/Two.java",
				"package test;\n" +
				"public class Two { public void arrayList(int i) {} }\n",
				"test/Three.java",
				"""
					package test;
					import static test.One.arrayList;
					public class Three extends Two { public static void test(String s) { arrayList(s); } }
					""",
			},
			"""
				----------
				1. ERROR in test\\Three.java (at line 3)
					public class Three extends Two { public static void test(String s) { arrayList(s); } }
					                                                                     ^^^^^^^^^
				The method arrayList(int) in the type Two is not applicable for the arguments (String)
				----------
				"""
		);
		this.runNegativeTest(
			new String[] {
				"test/One.java",
				"package test;\n" +
				"public class One { public static void arrayList(String s) {} }\n",
				"test/Two.java",
				"package test;\n" +
				"public class Two { public static void arrayList(int i) {} }\n",
				"test/Three.java",
				"""
					package test;
					import static test.One.arrayList;
					public class Three extends Two { public static void test(String s) { arrayList(s); } }
					""",
			},
			"""
				----------
				1. ERROR in test\\Three.java (at line 3)
					public class Three extends Two { public static void test(String s) { arrayList(s); } }
					                                                                     ^^^^^^^^^
				The method arrayList(int) in the type Two is not applicable for the arguments (String)
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133737
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"error/Exporter.java",
				"""
					package error;
					public class Exporter {
					  public static String getName(Class<?> c) {
					    return null;
					  }
					}""",
				"error/Importer.java",
				"""
					package error;
					import static error.Exporter.getName;
					public class Importer extends Base {
					  public void testSomething() {
					    getName();
					    getName(Importer.class);
					  }
					}""",
				"error/Base.java",
				"""
					package error;
					public class Base {
					  public String getName() {
					    return "name";
					  }
					}"""
			},
			"""
				----------
				1. ERROR in error\\Importer.java (at line 6)
					getName(Importer.class);
					^^^^^^^
				The method getName() in the type Base is not applicable for the arguments (Class<Importer>)
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165069
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165081
	public void test047() {
		this.runNegativeTest(
			new String[] {
				"sample/X.java",
				"""
					package sample;
					import static sample.X.TestEnum.V1;
					import static sample.X.TestEnum.V2;
					
					public class X<T> {
					        public static enum TestEnum {
					                V1,
					                V2
					        }
					
					        public void test(final TestEnum value) {
					                switch (value) {
					                        case V1:
					                        case V2:
					                }
					        }
					
					        public void ref() {
					               final TestEnum v1 = TestEnum.V1;
					               final TestEnum v2 = TestEnum.V2;
									int i;
									i++;
					        }
					}""", // =================
			},
			"""
				----------
				1. WARNING in sample\\X.java (at line 2)
					import static sample.X.TestEnum.V1;
					              ^^^^^^^^^^^^^^^^^^^^
				The import sample.X.TestEnum.V1 is never used
				----------
				2. WARNING in sample\\X.java (at line 3)
					import static sample.X.TestEnum.V2;
					              ^^^^^^^^^^^^^^^^^^^^
				The import sample.X.TestEnum.V2 is never used
				----------
				3. ERROR in sample\\X.java (at line 22)
					i++;
					^
				The local variable i may not have been initialized
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165069 - variation
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165081 - variation
	public void test048() {
		this.runNegativeTest(
			new String[] {
				"sample/X.java",
				"""
					package sample;
					import static sample.X.TestEnum.*;
					
					public class X<T> {
					        public static enum TestEnum {
					                V1,
					                V2
					        }
					
					        public void test(final TestEnum value) {
					                switch (value) {
					                        case V1:
					                        case V2:
					                }
					        }
					
					        public void ref() {
					               final TestEnum v1 = TestEnum.V1;
					               final TestEnum v2 = TestEnum.V2;
									int i;
									i++;
					        }
					}""", // =================
			},
			"""
				----------
				1. WARNING in sample\\X.java (at line 2)
					import static sample.X.TestEnum.*;
					              ^^^^^^^^^^^^^^^^^
				The import sample.X.TestEnum is never used
				----------
				2. ERROR in sample\\X.java (at line 21)
					i++;
					^
				The local variable i may not have been initialized
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165081 - variation
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"sample/X.java",
				"""
					package sample;
					import static sample.X.*;
					public class X {
						public class Member {}
						public void ref() {
							int i;
							i++;
						}
					}""", // =================
			},
			"""
				----------
				1. WARNING in sample\\X.java (at line 2)
					import static sample.X.*;
					              ^^^^^^^^
				The import sample.X is never used
				----------
				2. ERROR in sample\\X.java (at line 7)
					i++;
					^
				The local variable i may not have been initialized
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=187329
	public void test050() {
		this.runConformTest(
			new String[] {
				"p/A.java",
				"""
					package p;
					import static p.B.bar3;
					public class A { int a = bar3; }""" ,
				"p/B.java",
				"""
					package p;
					import static p.Util.someStaticMethod;
					public class B {
						static final int bar = someStaticMethod();
						static final int bar2 = someStaticMethod();
						static final int bar3 = someStaticMethod();
					}""" ,
				"p/C.java",
				"""
					package p;
					import static p.B.bar;
					public class C { int c = bar; }""" ,
				"p/Util.java",
				"package p;\n" +
				"class Util { static int someStaticMethod() { return 0; } }"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=207433
	public void test051() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static r.Y.Z;
					import q.*;
					public class X<T> extends Z<T> {
					   Z<T> getZ() { return null; }\s
						void bar() {
							System.out.println(getZ().value);
						}
					}
					""",
				"q/Z.java",
				"""
					package q;
					import r.Y;
					public class Z<T> extends Y<T> {
					}
					""",
				"r/Y.java",
				"""
					package r;
					public class Y<T> {
						public static String foo;
						public String value;
						public static String Z;
					}
					""" ,
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=207433 - variation
	public void test052() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static r.Y.*;
					import q.*;
					public class X<T> extends Z<T> {
					   Z<T> getZ() { return null; }\s
						void bar() {
							System.out.println(getZ().value);
						}
					}
					""",
				"q/Z.java",
				"""
					package q;
					import r.Y;
					public class Z<T> extends Y<T> {
					}
					""",
				"r/Y.java",
				"""
					package r;
					public class Y<T> {
						public static String foo;
						public String value;
						public static String Z;
					}
					""" ,
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=207433 - variation
	public void test053() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static q.Y.foo;
					public class X extends Z {
					   Z getZ() { return null; }\s
						void bar() {
							System.out.println(getZ().value);
						}
					}
					""",
				"p/Z.java",
				"""
					package p;
					import q.Y;
					public class Z extends Y {
					}
					""",
				"q/Y.java",
				"""
					package q;
					public class Y {
						public static int foo;
						public int value;
					}
					""" ,
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193210
	public void test055() {
		this.runConformTest(
				new String[] {
					"p/X.java",
					"""
						package p;
						import static r.Y.Z;
						import q.*;
						import r.*;
						public class X<T> extends Z<T> {
						   V<T> getV() { return null; }\s
							void bar() {
								System.out.println(getV().value);
							}
						}
						""",
					"q/Z.java",
					"""
						package q;
						import r.Y;
						public class Z<T> extends Y<T> {
						}
						""",
					"r/Y.java",
					"""
						package r;
						public class Y<T> extends V<T>{
							public static class Z<U> {}
						}
						""" ,
					"r/V.java",
					"""
						package r;
						public class V<T> {
							public Runnable value;
						}
						""" ,
				},
				"");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193210 - variation
	public void test056() {
		this.runNegativeTest(
				new String[] {
					"p/X.java",
					"""
						package p;
						import static r.Y.Z;
						import q.*;
						public class X extends Z {
						   Z getZ() { return null; }\s
							void bar() {
								System.out.println(getZ().value);
							}
						}
						""",
					"q/Z.java",
					"""
						package q;
						import r.Y;
						public class Z extends Y {
						}
						""",
					"r/Y.java",
					"""
						package r;
						public class Y extends V{
							public static class Z {}
						}
						""" ,
					"r/V.java",
					"""
						package r;
						public class V {
							public Runnable value;
						}
						""" ,
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 7)
						System.out.println(getZ().value);
						                          ^^^^^
					value cannot be resolved or is not a field
					----------
					""");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193210 - variation
	public void test057() {
		this.runNegativeTest(
				new String[] {
					"p/X.java",
					"""
						package p;
						import static r.Y.Z;
						import q.*;
						public class X<T> extends Z<T> {
						   Z<T> getZ() { return null; }\s
							void bar() {
								System.out.println(getZ().value);
							}
						}
						""",
					"q/Z.java",
					"""
						package q;
						import r.Y;
						public class Z<T> extends Y<T> {
						}
						""",
					"r/Y.java",
					"""
						package r;
						public class Y<T> extends V<T>{
							public static class Z {}
						}
						""" ,
					"r/V.java",
					"""
						package r;
						public class V<T> {
							public Runnable value;
						}
						""" ,
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 4)
						public class X<T> extends Z<T> {
						                          ^
					The type Y.Z is not generic; it cannot be parameterized with arguments <T>
					----------
					2. ERROR in p\\X.java (at line 5)
						Z<T> getZ() { return null; }\s
						^
					The type Y.Z is not generic; it cannot be parameterized with arguments <T>
					----------
					3. ERROR in p\\X.java (at line 7)
						System.out.println(getZ().value);
						                   ^^^^
					The method getZ() is undefined for the type X<T>
					----------
					""");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216930
	public void test058() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static p.A.a;
					public class X {
					   void foo(W w) { a(w).a(w); }
					}
					""",
				"p/A.java",
				"""
					package p;
					public class A {
					   public static A a(W... w) { return null; }
					   public A a(W w) { return null; }
					}
					""",
				"p/W.java",
				"package p;\n" +
				"public class W {}\n"
			},
			"");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183211
	public void test059() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static q.A.a;
					public class X {
					}
					""",
				"q/A.java",
				"""
					package q;
					interface I {
						String a = "";
					}
					class B {
						public static String a;
					}
					public class A extends B implements I{
					}
					""",
			},
			"");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183211 - variation
	public void test060() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static q.A.a;
					public class X {
					}
					""",
				"q/A.java",
				"""
					package q;
					interface I {
						String a(Object o);
					}
					class B {
						public static void a(){}
					}
					public abstract class A extends B implements I{
					}
					""",
			},
			"");
		}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183211 - variation
	public void test061() {
		runConformTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"p/X.java",
				"""
					package p;
					import static q.A.a;
					public class X {
					}
					""",
				"q/A.java",
				"""
					package q;
					interface I {
						String a = "";
					}
					interface B {
						String a = "2";
					}
					public class A implements B, I {
					}
					""",
			},
			// compiler results
			null /* do not check compiler log */,
			// runtime results
			"" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183211 - variation
	public void test062() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static q.A.a;
					public class X {
					}
					""",
				"q/A.java",
				"""
					package q;
					interface I {
						String a(Object o);
					}
					interface B {
						void a();
					}
					public abstract class A implements B, I{
					}
					""",
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 2)
					import static q.A.a;
					              ^^^^^
				The import q.A.a cannot be resolved
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183211 - variation
	public void test063() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import static q.A.a;
					import static q.A.b;
					public class X {
						void test() {
							System.out.println(a);
							System.out.println(b);
							System.out.println(b(1));
						}
					}
					""",
				"q/A.java",
				"""
					package q;
					interface I {
						String a = "1";
						String b = "2";
					}
					interface J {
						String a = "3";
					}
					class B {
						public static String a = "4";
						public static String b = "5";
						public static String b(int i) { return "6"; }
					}
					public class A extends B implements J, I {}
					""",
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 6)
					System.out.println(a);
					                   ^
				The field a is ambiguous
				----------
				2. ERROR in p\\X.java (at line 7)
					System.out.println(b);
					                   ^
				The field b is ambiguous
				----------
				""",
			JavacTestOptions.DEFAULT
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183211 - variation
	public void test064() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					import static p2.A.M;
					public class X {
						M m;
					}
					""",
				"p2/A.java",
				"""
					package p2;
					interface I { class M {} }
					class B { public static class M {} }
					public class A extends B implements I {}
					""",
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 4)
					M m;
					^
				The type M is ambiguous
				----------
				""",
			JavacTestOptions.DEFAULT
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230026
	public void test065() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.I.E.C;
					
					class C {}
					class B<T> {}
					public class X extends B<C>{
					}""",
				"p/I.java",
				"""
					package p;
					
					public interface I <T extends Object> {
						enum E { C }
					}""",
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230026 - variation
	public void test066() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.I.E.C;
					
					class C {}
					class B<T> {}
					public class X extends B<C>{
					}""",
				"p/I.java",
				"""
					package p;
					
					public interface I <T extends Object> {
						enum E { ; public static void C(){} }
					}""",
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230026 - variation
	public void test067() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import static p.I.E.C;
					
					class C {}
					class B<T> {}
					public class X extends B<C>{
					}""",
				"p/I.java",
				"""
					package p;
					
					public interface I <T extends Object> {
						enum E { ; static void C(){} }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import static p.I.E.C;
					              ^^^^^^^
				The import p.I.E.C cannot be resolved
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230026 - variation
	public void test068() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.I.E.C;
					class C {}
					class B<T> {}
					public class X extends B<C>{
						static void test() { int i = C; }
					}""",
				"p/I.java",
				"""
					package p;
					public interface I<T extends Object> {
						public static class E extends F {}
						public static class F { public static int C; }
					}""",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230026 - variation
	public void test069() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static p.I.E.C;
					class C {}
					class B<T> {}
					public class X extends B<C>{
						static void test() { C(); }
					}""",
				"p/I.java",
				"""
					package p;
					public interface I<T extends Object> {
						public static class E extends F {}
						public static class F { public static void C() {} }
					}""",
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211
	public void test070() {
		this.runConformTest(
			new String[] {
				"node/Test.java",//------------------------------
				"""
					package node;
					public class Test {
					        public static void node() {}
					}
					""",
				"node2/Test2.java",//------------------------------
				"""
					package node2;
					import static node.Test.node;
					public class Test2 {
					}
					""",
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
	public void test071() {
		this.runNegativeTest(
			new String[] {
				"node/Test/node.java",//------------------------------
				"""
					package node.Test;
					public class node {
					}
					""",
				"node/Test.java",//------------------------------
				"""
					package node;
					public class Test {
					        public static void node() {}
					}
					""",
				"node2/Test2.java",//------------------------------
				"""
					package node2;
					import node.Test;
					import static Test.node;
					public class Test2 {
					}
					""",
			},
			"""
				----------
				1. ERROR in node\\Test.java (at line 2)
					public class Test {
					             ^^^^
				The type Test collides with a package
				----------
				----------
				1. ERROR in node2\\Test2.java (at line 3)
					import static Test.node;
					              ^^^^
				The import Test cannot be resolved
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93913 - variation
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"p1/A.java",
				"""
					package p1;
					import static p2.C.B;
					public class A extends B {
						void test() {\
							int i = B;
							B b = null;
							int v1 = b.fooB;
							int v2 = b.fooC;
							int v3 = fooC;
						}
					}
					""",
				"p1/B.java",
				"""
					package p1;
					public class B {
						public int fooB;
					}
					""",
				"p2/C.java",
				"""
					package p2;
					public class C {
						public static class B { public int fooC; }
						public static int B;
					}
					""",
			},
			"""
				----------
				1. ERROR in p1\\A.java (at line 6)
					int v1 = b.fooB;
					           ^^^^
				fooB cannot be resolved or is not a field
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=256375
	public void test073() {
		this.runNegativeTest(
			new String[] {
				"test/Outer.java",
				"""
					package test;
					import static test.Outer.Inner.VALUE;
					public class Outer {
					    int i = VALUE;
					    int i2 = Inner.VALUE;
					    static class Inner {
					        private static final int VALUE = 0;
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in test\\Outer.java (at line 2)
					import static test.Outer.Inner.VALUE;
					              ^^^^^^^^^^^^^^^^^^^^^^
				The field Outer.Inner.test.Outer.Inner.VALUE is not visible
				----------
				2. ERROR in test\\Outer.java (at line 4)
					int i = VALUE;
					        ^^^^^
				VALUE cannot be resolved to a variable
				----------
				""");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=256375 - variation
	public void test074() {
		this.runConformTest(
			new String[] {
				"test/Outer.java",
				"""
					package test;
					import static test.Outer.Inner.*;
					public class Outer {
					    int i = VALUE;
					    int i2 = Inner.VALUE;
					    static class Inner {
					        private static final int VALUE = 0;
					    }
					}
					""",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
	// To verify that a static import importing a type which has already been
	// imported by a single type import is reported as duplicate
	// while the other static members imported by it are not shadowed.
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"A/A.java",
				"""
					package A;
					import B.B.C1;
					import static B.B.C1;
					public abstract class A {
						protected void A1(Object task) {
							C1 c = C1(task);
						}
					}
					""",
				"B/B.java",
				"""
					package B;
					final public class B {
						private B() {}
						public static class C1 {}
						public static C1 C1(Object o) {
							return new C1();
						}
					}
					""",
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
	// To verify that a static import importing a static method doesn't collide
	// with a single type import importing a non-static type with the same name as the method
	public void test076() {
		this.runConformTest(
			new String[] {
				"A/A.java",
				"""
					package A;
					import B.B.C1;
					import static B.B.C1;
					public class A {
						protected void A1(Object task) {
							C1 c1;
							int c = C1(task);
						}
					}
					""",
				"B/B.java",
				"""
					package B;
					final public class B {
						private B() {}
						public class C1 {}
						public static int C1(Object o) {
							return 1;
						}
					}
					""",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
	// To verify that two static imports importing the same type don't collide
	public void test077() {
		this.runConformTest(
			new String[] {
				"p1/X.java",
				"""
					package p1;
					import p2.A;
					import static p2.A.C;
					import static p1.B.C;
					public class X {
						public static void main(String[] args) {
							foo();
						}
						public static void foo() {
							if (C.CONST == 1) {
								System.out.println("SUCCESS");
								return;
							}
							System.out.println("FAILED");
						}
					}
					class B extends A {}""",
				"p2/A.java",
				"""
					package p2;
					public class A {
						public static class C {
							public static int CONST = 1;
						}
					}"""
			},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
	// To verify that a static import importing a type which has already been
	// imported by a single type import is reported as duplicate
	// while the other static members imported by it are not shadowed.
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"A/A.java",
				"""
					package A;
					import static B.B.C1;
					import B.B.C1;
					public abstract class A {
						protected void A1(Object task) {
							C1 c = C1(task);
						}
					}
					""",
				"B/B.java",
				"""
					package B;
					final public class B {
						private B() {}
						public static class C1 {}
						public static C1 C1(Object o) {
							return new C1();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in A\\A.java (at line 3)
					import B.B.C1;
					       ^^^^^^
				The import B.B.C1 collides with another import statement
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
	// To verify that a static import importing a type which has already been
	// imported by a single type import is not reported as duplicate
	// if they are just the same type
	public void test079() {
		this.runNegativeTest(
			new String[] {
				"A/A.java",
				"""
					package A;
					import static B.B.C1;
					import B.B.C1;
					public abstract class A {
						protected void A1(C1 c) {
						}
					}
					""",
				"B/B.java",
				"""
					package B;
					final public class B {
						public static class C1 {}
					}
					""",
			},
			"""
				----------
				1. WARNING in A\\A.java (at line 2)
					import static B.B.C1;
					              ^^^^^^
				The import B.B.C1 is never used
				----------
				"""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336934
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"a/B.java",
				"package a;\n" +
				"public class B {}",
				"external/Lib.java",
				"""
					package external;
					public class Lib {
						public static void m() {}
					}""",
				"a/B/C.java",
				"""
					package a.B;
					import static external.Lib.m;
					public class C {
						public void main() {
							m();
						}
					}"""
			},
			"""
				----------
				1. ERROR in a\\B\\C.java (at line 1)
					package a.B;
					        ^^^
				The package a.B collides with a type
				----------
				"""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318401
	public void test081() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import static p1.Bar.B;\n" +
				"import p3.Foo.*;\n" +
				"public class Test {\n" +
				"	public static void main(String [] args){\n" +
				"		new Test().beginTest();" +
				"	}\n" +
				"	public void beginTest(){\n" +
				"		System.out.print(\"1 + 1 =  \");\n" +
				"		if(alwaysTrue()) System.out.println(\"2\");\n" +
				"		else System.out.println(\"3\"); " +
				"	}\n" +
				"	public boolean alwaysTrue(){\n" +
				"		String myB   =        B.class.getCanonicalName();;\n" +		// refers to p1.Bar.B (class)
				"		String realB = p1.Bar.B.class.getCanonicalName();;\n" +     // refers to p1.Bar.B (class)
				"		B();\n" +				// refers to p1.Bar.B() (method)
				"		return myB.equals(realB);\n" +
				"	}\n" +
				"}\n",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static class B{}
						final public static String B = new String("random");
						public static void B(){}
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public class B{
							public int a;
						}
					}
					"""
			},
			"1 + 1 =  2");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318401
	public void test082() {
		this.runNegativeTest(
			new String[] {
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static class B{}
						final public static String B = new String("random");
						public static void B(){}
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public class B{
							public int a;
						}
					}
					""",
				"p2/Test.java",
				"package p2;\n" +
				"import static p1.Bar.B;\n" +
				"import p3.Foo.*;\n" +
				"public class Test {\n" +
				"	public static void main(String [] args){\n" +
				"		new Test().beginTest();" +
				"	}\n" +
				"	public void beginTest(){\n" +
				"		System.out.print(\"1 + 1 =  \");\n" +
				"		if(alwaysTrue()) System.out.println(\"2\");\n" +
				"		else System.out.println(\"3\"); " +
				"	}\n" +
				"	public boolean alwaysTrue(){\n" +
				"		B b = null;\n" +		// refers to p1.Bar.B (class)
				"		String realB = B;\n" +  // refers to p1.Bar.B (field)
				"		B();\n" +				// refers to p1.Bar.B() (method)
				"		int abc = b.a;\n;" +	// static import for Bar.B overshadows on demand import Foo.B
				"	}\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in p2\\Test.java (at line 15)
					int abc = b.a;
					            ^
				a cannot be resolved or is not a field
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318401
	public void test083() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import static p1.Bar.B;\n" +
				"import p3.Foo.*;\n" +
				"public class Test {\n" +
				"	public static void main(String [] args){\n" +
				"		new Test().test2();" +
				"	}\n" +
				"	public void test2(){\n" +
				"		System.out.println(B.toString());\n" +		// Field obscures class B
				"		System.out.println(p1.Bar.B.toString());\n" +  // Field obscures the class B
				"		System.out.println(B.class.getCanonicalName().toString());\n" +	// the class B
				"		System.out.println(p1.Bar.B.class.getCanonicalName().toString());" +	// class B
				"	}\n" +
				"}\n",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static class B{}
						final public static String B = new String("random");
						public static void B(){}
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public class B{
							public int a;
						}
					}
					"""
			},
			"""
				random
				random
				p1.Bar.B
				p1.Bar.B""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318401
	// Check if we're able to find the correct static member type being imported,
	// even though the import originally resolved to the static field of the same name,
	// coming from the supertype
	public void test084() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import static p1.Bar.B;\n" +
				"import p3.Foo.*;\n" +
				"public class Test {\n" +
				"	public static void main(String [] args){\n" +
				"		new Test().test2();" +
				"	}\n" +
				"	public void test2(){\n" +
				"		System.out.println(B.class.getCanonicalName().toString());\n" +	// the class B
				"		System.out.println(p1.Bar.B.class.getCanonicalName().toString());" +	// class B
				"	}\n" +
				"}\n",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar extends SuperBar{
						public static class B{}
						public static void B(){}
					}
					""",
				"p1/SuperBar.java",
				"""
					package p1;
					public class SuperBar {
						final public static String B = new String("random");
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public class B{
							public int a;
						}
					}
					"""
			},
			"p1.Bar.B\n" +
			"p1.Bar.B");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361327
	// To verify that all static members are imported with a single static import statement
	public void test085() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import static p1.Bar.B;
					import static p3.Foo.B;
					public class Test {
						public static void main(String [] args){
							new Test().test2();\
						}
						public void test2(){
							System.out.println(B.class.getCanonicalName().toString());
							System.out.println(p1.Bar.B.class.getCanonicalName().toString());\
						}
					}
					""",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static class B{}
						public static String B = new String("random");
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public static class B{
						}
					}
					"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_8 ?
					"""
						----------
						1. ERROR in Test.java (at line 2)
							import static p3.Foo.B;
							              ^^^^^^^^
						The import p3.Foo.B collides with another import statement
						----------
						""" :
						"""
							----------
							1. ERROR in Test.java (at line 7)
								System.out.println(B.class.getCanonicalName().toString());
								                   ^
							The type B is ambiguous
							----------
							"""

				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361327
	// To verify that all static members are imported with a single static import statement,
	// even from a supertype
	public void test085a() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import static p1.Bar.B;
					import static p3.Foo.B;
					public class Test {
						public static void main(String [] args){
							new Test().test2();\
						}
						public void test2(){
							System.out.println(B.class.getCanonicalName().toString());
							System.out.println(p1.Bar.B.class.getCanonicalName().toString());\
						}
					}
					""",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar extends SuperBar{
						public static void B(){}
					}
					""",
				"p1/SuperBar.java",
				"""
					package p1;
					public class SuperBar {
						public static class B{}
						final public static String B = new String("random");
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public static class B{
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 2)
					import static p3.Foo.B;
					              ^^^^^^^^
				The import p3.Foo.B collides with another import statement
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361327
	// To verify that all static members are imported with a single static import statement
	// this tests checks collision with single type import
	public void test085b() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import static p1.Bar.B;
					import p3.Foo.B;
					public class Test {
						public static void main(String [] args){
							new Test().test2();\
						}
						public void test2(){
							System.out.println(B.class.getCanonicalName().toString());
							System.out.println(p1.Bar.B.class.getCanonicalName().toString());\
						}
					}
					""",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static class B{}
						public static String B = new String("random");
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public class B{
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 2)
					import p3.Foo.B;
					       ^^^^^^^^
				The import p3.Foo.B collides with another import statement
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361327
	// To verify that all static members are imported with a single static import statement
	// this tests checks collision with top level type
	public void test085c() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import static p1.Bar.B;
					public class Test {
						public static void main(String [] args){
							new Test().test2();\
						}
						public void test2(){
							System.out.println(B.class.getCanonicalName().toString());
							System.out.println(p1.Bar.B.class.getCanonicalName().toString());\
						}
					}
					class B{
					}
					""",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static class B{}
						public static String B = new String("random");
					}
					""",
			},
			"""
				----------
				1. ERROR in Test.java (at line 1)
					import static p1.Bar.B;
					              ^^^^^^^^
				The import p1.Bar.B conflicts with a type defined in the same file
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361327
	// Test obscuring rules defined in JLS 7.5.3
	public void test086() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"import static p1.Bar.B;\n" +
				"import static p3.Foo.*;\n" +
				"public class Test {\n" +
				"	public static void main(String [] args){\n" +
				"		new Test().test2();" +
				"	}\n" +
				"	public void test2(){\n" +
				"       B();\n" + // should be p1.Bar.B() and not p3.Foo.B()
				"		System.out.println(B.toString());\n" + // should be p1.Bar.B
				"	}\n" +
				"}\n",
				"p1/Bar.java",
				"""
					package p1;
					public class Bar{
						public static void B(){ System.out.println("Bar's method B");}
						public static String B = new String("Bar's field B");
					}
					""",
				"p3/Foo.java",
				"""
					package p3;
					public class Foo {
						public static void B(){ System.out.println("Foo's method B");}
						public static String B = new String("Foo's field B");
					}
					"""
			},
			"Bar\'s method B\n" +
			"Bar\'s field B");
	}

	// https://bugs.eclipse.org/401271 - StackOverflowError when searching for a methods references
	public void testBug401271() {
		runNegativeTest(
			new String[] {
				"a/b/c/a.java",
				"package a.b.c;\n" +
				"public class a {}\n",
				"a/b/c/C.java",
				"""
					package a.b.c;
					public class C {
						public static final int a = 3;
					}
					""",
				"x/y/R.java",
				"""
					package x.y;
					import static a.b.c.C.a;
					//import a.b.c.a;
					
					public class R {\s
						a b;\s
						char h = a;\s
					}"""
			},
			"""
				----------
				1. ERROR in x\\y\\R.java (at line 6)
					a b;\s
					^
				a cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426544 - [1.8][compiler] Compiler over-eagerly detects collision of single static imports
	public void test426544() {
		runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					    public static int f;
					    public static class C {}
					    public static class I {}
					}
					""",
				"q/X.java",
				"""
					package q;
					public class X {
					    public static int f;
					    public static class C {}
					    public static class I {}
					}
					""",
				"X.java",
				"""
					import static p.X.f;
					import static q.X.f;
					import static p.X.C;
					import static p.X.I;
					import static q.X.C;
					import static q.X.I;
					public class X {\s
					    { f = 0; }
					    { C c = null; }
					    { I i = null; }
					}
					"""
			},
			this.complianceLevel < ClassFileConstants.JDK1_8 ?
					"""
						----------
						1. ERROR in X.java (at line 2)
							import static q.X.f;
							              ^^^^^
						The import q.X.f collides with another import statement
						----------
						2. ERROR in X.java (at line 5)
							import static q.X.C;
							              ^^^^^
						The import q.X.C collides with another import statement
						----------
						3. ERROR in X.java (at line 6)
							import static q.X.I;
							              ^^^^^
						The import q.X.I collides with another import statement
						----------
						""" :
						"""
							----------
							1. ERROR in X.java (at line 8)
								{ f = 0; }
								  ^
							The field f is ambiguous
							----------
							2. ERROR in X.java (at line 9)
								{ C c = null; }
								  ^
							The type C is ambiguous
							----------
							3. ERROR in X.java (at line 10)
								{ I i = null; }
								  ^
							The type I is ambiguous
							----------
							""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=479287
	// erroneous compile error using static imports and generics
	public void testBug479287() {
		this.runConformTest(
			new String[] {
				"joetest/GenericsIssue.java",
				"""
					package joetest;
					import static joetest.GenericsIssueCollaborator.takesAnything;
					import java.util.Collection;
					import java.util.Collections;
					public class GenericsIssue {
						private void oddCompileError() {
							takesAnything(returnThings("works without wildcard in return value"));
							GenericsIssueCollaborator.takesAnything(returnThingsWildcard("works without static import"));
							takesAnything(returnThingsWildcard("doesn't work with static import"));
						}
						private <T> Collection<T> returnThings(T thing) {
							return Collections.singleton(thing);
						}
						\t
						private <T> Collection<? extends T> returnThingsWildcard(T toReturn) {
							return Collections.singleton(toReturn);
						}
					}""",
				"joetest/GenericsIssueCollaborator.java",
				"""
					package joetest;
					public class GenericsIssueCollaborator {
						public static <T> void takesAnything(T thing) {
							System.out.println("TOOK IT: " + thing);
						}
					}"""
			});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442580
	// Explicit static import after two wildcard imports is ambiguous (works in javac)
	public void testBug442580() {
		this.runConformTest(new String [] {
				"a/A.java",
				"""
					package a;
					
					public class A {
						public static void foo() {
							System.out.println("A.foo");
						}
					}""",
				"b/B.java",
				"""
					package b;
					
					public class B {
						public static void foo() {
							System.out.println("B.foo");
						}
					}""",
				"Test.java",
				"""
					import static a.A.*;
					import static b.B.*;
					import static b.B.foo;
					
					public class Test {
						public static void main(String[] args) {
							foo();
						}
					}"""
		});
	}
	public void testBug520874a() {
		if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
			return;
		}
		runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import static p.A1.Outer.*;
							import static p.A1.AnotherOuter.Inner;
							public class X {}
							class A1 {
								static class Outer<T extends Inner> {
									private static interface Inner {}
							    }
								static class AnotherOuter {
									private static class Inner {}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 3)
						import static p.A1.AnotherOuter.Inner;
						              ^^^^^^^^^^^^^^^^^^^^^^^
					The type p.A1.AnotherOuter.Inner is not visible
					----------
					2. ERROR in p\\X.java (at line 6)
						static class Outer<T extends Inner> {
						                             ^^^^^
					The type Inner is not visible
					----------
					""");
	}
	public void testBug520874b() {
		if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
			return;
		}
		runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import p.A1.Outer.*;
							public class X {}
							class A1 {
								static class Outer<T extends Inner> {
									private static interface Inner {}
							    }
							}
							"""
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 5)
						static class Outer<T extends Inner> {
						                             ^^^^^
					The type Inner is not visible
					----------
					""");
	}
	public void testBug520874c() {
		if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
			return;
		}
		runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import static p.A1.Outer.Inner;
							import static p.A1.AnotherOuter.Inner;
							public class X {}
							class A1 {
								static class Outer<T extends Inner> {
									private static interface Inner {}
							    }
								static class AnotherOuter<T extends Inner> {
									private static class Inner {}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 2)
						import static p.A1.Outer.Inner;
						              ^^^^^^^^^^^^^^^^
					The type p.A1.Outer.Inner is not visible
					----------
					2. ERROR in p\\X.java (at line 3)
						import static p.A1.AnotherOuter.Inner;
						              ^^^^^^^^^^^^^^^^^^^^^^^
					The type p.A1.AnotherOuter.Inner is not visible
					----------
					3. ERROR in p\\X.java (at line 6)
						static class Outer<T extends Inner> {
						                             ^^^^^
					Inner cannot be resolved to a type
					----------
					4. ERROR in p\\X.java (at line 9)
						static class AnotherOuter<T extends Inner> {
						                                    ^^^^^
					Inner cannot be resolved to a type
					----------
					""");
	}
	public void testBug520874d() {
		if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
			return;
		}
		runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import static p.A.B.Inner;
							import p.Bar.Inner;
							public class X {}
							class A {
							    static class B extends Bar {}
							}
							""",
						"p/Bar.java",
						"""
							package p;
							public class Bar {;
								public static class Inner {}
							}
							"""
				},
				"""
					----------
					1. WARNING in p\\X.java (at line 2)
						import static p.A.B.Inner;
						              ^^^^^^^^^^^
					The import p.A.B.Inner is never used
					----------
					2. WARNING in p\\X.java (at line 3)
						import p.Bar.Inner;
						       ^^^^^^^^^^^
					The import p.Bar.Inner is never used
					----------
					""");
	}
	public void testBug520874e() {
		if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
			return;
		}
		runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							package p;
							import static p.A.B.Inner;
							import p.Bar.*;
							public class X {}
							class A {
							    static class B extends Bar {}
							}
							""",
						"p/Bar.java",
						"""
							package p;
							public class Bar {;
								public static class Inner {}
							}
							"""
				},
				"""
					----------
					1. WARNING in p\\X.java (at line 2)
						import static p.A.B.Inner;
						              ^^^^^^^^^^^
					The import p.A.B.Inner is never used
					----------
					2. WARNING in p\\X.java (at line 3)
						import p.Bar.*;
						       ^^^^^
					The import p.Bar is never used
					----------
					""");
	}
	public void testGH809_field_a() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p2/Client.java",
			"""
			package p2;
			import static p1.Sub.*;
			public class Client {
				public static void main(String... args) {
					System.out.println(ONE);
				}
			}
			""",
			"p1/Super.java",
			"""
			package p1;
			class Super {
				public static int ONE = 1;
			}
			""",
			"p1/Sub.java",
			"""
			package p1;
			public class Sub extends Super {}
			"""
		};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void testGH809_field_b() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p2/Client.java",
			"""
			package p2;
			import static p1.Sub.ONE;
			public class Client {
				public static void main(String... args) {
					System.out.println(ONE);
				}
			}
			""",
			"p1/Super.java",
			"""
			package p1;
			class Super {
				public static int ONE = 1;
			}
			""",
			"p1/Sub.java",
			"""
			package p1;
			public class Sub extends Super {}
			"""
		};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void testGH809_field_c() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p2/Client.java",
			"""
			package p2;
			import p1.Sub;
			public class Client {
				public static void main(String... args) {
					System.out.println(Sub.ONE);
				}
			}
			""",
			"p1/Super.java",
			"""
			package p1;
			class Super {
				public static int ONE = 1;
			}
			""",
			"p1/Sub.java",
			"""
			package p1;
			public class Sub extends Super {}
			"""
		};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void testGH809_method_a() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p2/Client.java",
			"""
			package p2;
			import static p1.Sub.*;
			public class Client {
				public static void main(String... args) {
					System.out.println(ONE());
				}
			}
			""",
			"p1/Super.java",
			"""
			package p1;
			class Super {
				public static int ONE() { return 1; }
			}
			""",
			"p1/Sub.java",
			"""
			package p1;
			public class Sub extends Super {}
			"""
		};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void testGH809_method_b() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p2/Client.java",
			"""
			package p2;
			import static p1.Sub.ONE;
			public class Client {
				public static void main(String... args) {
					System.out.println(ONE());
				}
			}
			""",
			"p1/Super.java",
			"""
			package p1;
			class Super {
				public static int ONE() { return 1; }
			}
			""",
			"p1/Sub.java",
			"""
			package p1;
			public class Sub extends Super {}
			"""
		};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void testGH809_method_c() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"p2/Client.java",
			"""
			package p2;
			import p1.Sub;
			public class Client {
				public static void main(String... args) {
					System.out.println(Sub.ONE());
				}
			}
			""",
			"p1/Super.java",
			"""
			package p1;
			class Super {
				public static int ONE() { return 1; }
			}
			""",
			"p1/Sub.java",
			"""
			package p1;
			public class Sub extends Super {}
			"""
		};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
}