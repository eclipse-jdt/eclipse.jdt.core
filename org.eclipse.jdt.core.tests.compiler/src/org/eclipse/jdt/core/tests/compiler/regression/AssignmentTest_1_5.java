/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AssignmentTest_1_5 extends AbstractRegressionTest {

public AssignmentTest_1_5(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 15 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test1() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						double test = 47d;
						value += test;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				value += test;
				^^^^^^^^^^^^^
			The operator += is undefined for the argument type(s) Integer, double
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						float test = 47f;
						value += test;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				value += test;
				^^^^^^^^^^^^^
			The operator += is undefined for the argument type(s) Integer, float
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						byte test = 47;
						value += test;
						System.out.println(value);
					}
				}""",
		},
		"4758");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						char test = 'a';
						value += test;
						System.out.println(value);
					}
				}""",
		},
		"4808");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test5() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						long test = 100L;
						value += test;
						System.out.println(value);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				value += test;
				^^^^^^^^^^^^^
			The operator += is undefined for the argument type(s) Integer, long
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test6() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						boolean test = true;
						value += test;
						System.out.println(value);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				value += test;
				^^^^^^^^^^^^^
			The operator += is undefined for the argument type(s) int, boolean
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test7() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						short test = 32767;
						value += test;
						System.out.println(value);
					}
				}""",
		},
		"37478");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test8() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int x = -8;
						x += 7.8f;
						System.out.println(x == 0 ? "SUCCESS" : "FAILED");
					}
				}""",
		},
		"SUCCESS");
}
public void test9() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class XSuper<T> {
					T value;
				}
				public class X extends XSuper<String>{
					public void a() {
						this.value += 1;
						this.value = this.value + 1;
						System.out.println(this.value);
					}
				
					public static void main(final String[] args) {
						X x = new X();
						x.value = "[";
						x.a();
					}
				}
				""",
		},
		"[11");
}
public void test10() {
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
public void test11() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String args[]) {\s
						Long _long = new Long(44);
						byte b = (byte) 1;
						char c = (char) 2;
						short s = (short) 32767;
						int i = 10;
						long l = 80L;
						_long >>>= b;
						_long <<= c;
						_long >>= s;
						_long >>>= i;
						_long = 77l;
						_long <<= l;
						System.out.println(_long);
					}
				}""",
		},
		"5046272"
	);
}
public void test12() {
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test13() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						long test = 47L;
						value &= test;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				value &= test;
				^^^^^^^^^^^^^
			The operator &= is undefined for the argument type(s) Integer, long
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test14() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer value = 4711;
						long test = 47L;
						value |= test;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				value |= test;
				^^^^^^^^^^^^^
			The operator |= is undefined for the argument type(s) Integer, long
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277450
public void test15() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Byte value = (byte) 1;
						value++;
						System.out.println(value);
					}
				}""",
		},
		"2");
}
public static Class testClass() {
	return AssignmentTest_1_5.class;
}
}
