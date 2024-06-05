/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
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

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

// Bug 488662 - [1.9] Allow private methods in interfaces
@SuppressWarnings({ "rawtypes" })
public class InterfaceMethodsTest_9 extends AbstractComparableTest {

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug488662_001" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Test setUpTest(Test test) throws Exception {
		TestCase.setUpTest(test);
		RegressionTestSetup suite = new RegressionTestSetup(ClassFileConstants.JDK9);
		suite.addTest(test);
		return suite;
	}

	public static Class testClass() {
		return InterfaceMethodsTest_9.class;
	}

	public InterfaceMethodsTest_9(String name) {
		super(name);
	}

	// private method - positive test
	public void testBug488662_001() {
		runConformTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				@SuppressWarnings("unused")
				    private void foo()  {}
				}
				""",
		},
		"");
	}
	// private method legal combination of modifiers - positive test
	public void testBug488662_002() {
		runConformTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				@SuppressWarnings("unused")
				    private static void foo()  {}
				}
				""",
		},
		"");
	}
	// private method legal combination of modifiers - positive test
	public void testBug488662_003() {
		runConformTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				@SuppressWarnings("unused")
				    private strictfp void foo()  {}
				}
				""",
		},
		"");
	}

	// missing method body - negative test
	public void testBug488662_004() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				@SuppressWarnings("unused")
				    private void foo();
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 3)
					private void foo();
					             ^^^^^
				This method requires a body instead of a semicolon
				----------
				""");
	}

	// illegal modifier combination - negative test
	public void testBug488662_005() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				@SuppressWarnings("unused")
				    private default void foo();
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 3)
					private default void foo();
					                     ^^^^^
				Illegal combination of modifiers for the private interface method foo; additionally only one of static and strictfp is permitted
				----------
				2. ERROR in I.java (at line 3)
					private default void foo();
					                     ^^^^^
				This method requires a body instead of a semicolon
				----------
				""");
	}
	// illegal modifier combination - negative test
	public void testBug488662_006() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
					private abstract void foo();
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					private abstract void foo();
					                      ^^^^^
				Illegal combination of modifiers for the private interface method foo; additionally only one of static and strictfp is permitted
				----------
				""");
	}

	// illegal modifier combination - negative test
	public void testBug488662_007() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				    private synchronized void foo();
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					private synchronized void foo();
					                          ^^^^^
				Illegal modifier for the interface method foo; only public, private, abstract, default, static and strictfp are permitted
				----------
				2. ERROR in I.java (at line 2)
					private synchronized void foo();
					                          ^^^^^
				This method requires a body instead of a semicolon
				----------
				""");
	}

	// reduced visibility modifier - negative test
	public void testBug488662_008() {
		runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					public default void foo() {}
				}
				public class X implements I{
				@SuppressWarnings("unused")
				@Override
					private void foo() {}
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					private void foo() {}
					             ^^^^^
				Cannot reduce the visibility of the inherited method from I
				----------
				""");
	}


	// No unimplemented method error - positive test
	public void testBug488662_009() {
		runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					private  void foo() {
					}
					public default void bar() {
						foo();
					}
				}
				public class X implements I{
					public static void main(String[] args) {
						new X().bar();
					}
				}
				"""
		},
		"");
	}
	// illegal modifier combination - multiple errors - negative test
	public void testBug488662_010() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				    private public void foo(){}
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					private public void foo(){}
					                    ^^^^^
				Illegal combination of modifiers for the private interface method foo; additionally only one of static and strictfp is permitted
				----------
				""");
	}
	// illegal modifier combination - negative test
	public void testBug488662_011() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				    private protected void foo();
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					private protected void foo();
					                       ^^^^^
				Illegal modifier for the interface method foo; only public, private, abstract, default, static and strictfp are permitted
				----------
				2. ERROR in I.java (at line 2)
					private protected void foo();
					                       ^^^^^
				This method requires a body instead of a semicolon
				----------
				""");
	}
	// illegal modifier combination - multiple errors - negative test
	public void testBug488662_012() {
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				    private private public default protected void foo();
				}
				"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					private private public default protected void foo();
					                                              ^^^^^
				Duplicate modifier for the method foo in type I
				----------
				2. ERROR in I.java (at line 2)
					private private public default protected void foo();
					                                              ^^^^^
				Illegal modifier for the interface method foo; only public, private, abstract, default, static and strictfp are permitted
				----------
				3. ERROR in I.java (at line 2)
					private private public default protected void foo();
					                                              ^^^^^
				This method requires a body instead of a semicolon
				----------
				""");
	}
	public void testBug517926() {
		runNegativeTest(
			new String[] {
				"I.java",
				"""
					public interface I<T> {
					   private String name(T t){return null;}
						default String getName() { return name(null);}
					}
					""",
				"A.java",
				"""
					public class A implements I<String> {
						@Override
						public String name(String s) {
							return null;
						}
					}"""
			},
			"""
				----------
				1. ERROR in A.java (at line 3)
					public String name(String s) {
					              ^^^^^^^^^^^^^^
				The method name(String) of type A must override or implement a supertype method
				----------
				""");
	}
	public void testBug521743() {
		runConformTest(
			new String[] {
				"FI.java",
				"""
					interface FI {
					    private <T> void foo(Class c){}
					}
					interface FI2 extends FI {
					    default <T> void foo(Class<T> c) {}
					}"""
			},
			"");
	}
	public void testBug520795() {
		runNegativeTest(
			new String[] {
				"I.java",
				"""
					public interface I {
					    private static void foo(){};
						default void bar() {
							foo();
						}\
					}
					""",
				"X.java",
				"""
					public class X {
					public static void main(String[] args) {
						I.foo();
					}\
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					I.foo();
					  ^^^
				The method foo() from the type I is not visible
				----------
				""" );
	}
	public void testBug520795a() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					interface I {
					   private static void foo(){};
						default void bar() {
							foo();
						}\
					}
					public static void main(String[] args) {
						I.foo();
					}\
					}
					"""
		});
	}
	public void testBug520795b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					public interface I {
					   private static void foo(){};
						void bar();\
					}
					public static void main(String[] args) {
						I i = () -> {};
						i.foo();
					}\
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					i.foo();
					  ^^^
				This static method of interface X.I can only be accessed as X.I.foo
				----------
				""" );
	}
	public void testBug520795c() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					public interface I {
					   private static void foo(){};
					}
					public interface J extends I {
					   default void goo(){I.super.foo();};
						void baz();\
					}
					public static void main(String[] args) {
						J j = () -> {};
						j.goo();
					}\
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					default void goo(){I.super.foo();};
					                           ^^^
				This static method of interface X.I can only be accessed as X.I.foo
				----------
				""" );
	}
	public void testBug518272() {
		runConformTest(
			new String[] {
				"GeneratedAccessorBug.java",
				"""
					public interface GeneratedAccessorBug {
					  void hello();
					  private static void foo() {}
					  public static void bar() {
					    new GeneratedAccessorBug() {
					      public void hello() {
					        foo();
					      }
					    }.hello();
					  }
					  public static void main(String[] args) {
					    GeneratedAccessorBug.bar();
					  }
					}"""
		});
	}
}