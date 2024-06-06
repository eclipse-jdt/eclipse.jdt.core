/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class NumericTest extends AbstractRegressionTest {

public NumericTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/B.java",
		"""
			package p;
			public class B {
			  public static void main(String[] args) {
			    int offset = -8;
			    int temp = 0 - offset;
			    offset = 0 - offset;  // This is the problem line
			    System.out.println("offset: " + offset);
			    System.out.println("temp: " + temp);
			    if (offset != temp ) {
			      System.err.println("offset (" + offset + ") should be equal to temp (" + temp + ").");
			      System.exit(-1);
			    }
			  }
			}
			""",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/Y.java",
		"""
			package p;
			public class Y {
			  public static void main(String[] args) {
			    int clockend = 0;
			    clockend += 128;
			    if(clockend < 0) {
			      System.out.println(clockend);
			      System.exit(-1);
			    }
			  }
			}
			""",
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133738
public void test003() {
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
			  int i1 = -2147483648;
			  int i2 = -(2147483648);
			}""",
	},
	"""
		----------
		1. ERROR in X.java (at line 3)
			int i2 = -(2147483648);
			          ^^^^^^^^^^^^
		The literal 2147483648 of type int is out of range\s
		----------
		""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133738
public void test004() {
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			public class X {
			  long l1 = -9223372036854775808L;
			  long l2 = -(9223372036854775808L);
			}""",
	},
	"""
		----------
		1. ERROR in X.java (at line 3)
			long l2 = -(9223372036854775808L);
			           ^^^^^^^^^^^^^^^^^^^^^^
		The literal 9223372036854775808L of type long is out of range\s
		----------
		""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232814
public void test005() {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					int result;
					if (130 != (result = testShort1())) System.out.println("failed-testShort1():" + result);
					if (130 != (result = testShort2())) System.out.println("failed-testShort2():" + result);
					if (130 != (result = testInt1())) System.out.println("failed-testInt1():" + result);
					if (130 != (result = testInt2())) System.out.println("failed-testInt2():" + result);
					if (30 != (result = testByte1())) System.out.println("failed-testByte1():" + result);
					if (30 != (result = testByte2())) System.out.println("failed-testByte2():" + result);
					System.out.println("done");
				}
				static int testShort1() {
					short min = Short.MIN_VALUE;
					int num = -32638;
					return num = num - min;
				}
				static int testShort2() {
					final short min = Short.MIN_VALUE;
					int num = -32638;
					return num = num - min;
				}
				static int testInt1() {
					short min = Short.MIN_VALUE;
					int num = -32638;
					return num = num - min;
				}
				static int testInt2() {
					final short min = Short.MIN_VALUE;
					int num = -32638;
					return num = num - min;
				}\t
				static int testByte1() {
					byte min = Byte.MIN_VALUE;
					int num = -98;
					return num = num - min;
				}
				static int testByte2() {
					final byte min = Byte.MIN_VALUE;
					int num = -98;
					return num = num - min;
				}	\t
			}
			""",
	},
	"done");
}
public static Class testClass() {
	return NumericTest.class;
}
}
