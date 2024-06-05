/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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
public class UnderscoresInLiteralsTest extends AbstractRegressionTest {
	static {
//		TESTS_NUMBERS = new int[] { 24 };
	}
	public UnderscoresInLiteralsTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}

	public static Class testClass() {
		return UnderscoresInLiteralsTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0b_001);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0b_001);
					                   ^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0_b001);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0_b001);
					                   ^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0b001_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0b001_);
					                   ^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x_11.0p33f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x_11.0p33f);
					                   ^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x11_.0p33f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x11_.0p33f);
					                   ^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x11._0p33f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x11._0p33f);
					                   ^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x11.0_p33f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x11.0_p33f);
					                   ^^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x11.0p_33f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x11.0p_33f);
					                   ^^^^^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x11.0p33_f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x11.0p33_f);
					                   ^^^^^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x_0001AEFBBA);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x_0001AEFBBA);
					                   ^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0_x0001AEFBBA);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0_x0001AEFBBA);
					                   ^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x0001AEFBBA_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0x0001AEFBBA_);
					                   ^^^^^^^^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(_01234567);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(_01234567);
					                   ^^^^^^^^^
				_01234567 cannot be resolved to a variable
				----------
				""");
	}
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(01234567_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(01234567_);
					                   ^^^^^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(1_.236589954f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(1_.236589954f);
					                   ^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(1._236589954f);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(1._236589954f);
					                   ^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(1_e2);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(1_e2);
					                   ^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(1e_2);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(1e_2);
					                   ^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(1e2_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(1e2_);
					                   ^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(01e2_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(01e2_);
					                   ^^^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(01_e2_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(01_e2_);
					                   ^^^
				Underscores have to be located within digits
				----------
				""");
	}
	public void test022() {
		Map customedOptions = getCompilerOptions();
		customedOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
		customedOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0b1110000_);
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(0b1110000_);
					                   ^^^^^^^^^^
				Underscores can only be used with source level 1.7 or greater
				----------
				""",
			null,
			true,
			customedOptions);
	}
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(0x1234____5678____90L);
						}
					}"""
			},
			"78187493520");
	}
	public void test024() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(90_00__00_0);
						}
					}"""
			},
			"9000000");
	}
}
