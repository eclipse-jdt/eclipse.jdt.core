/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class AssertionTest extends AbstractRegressionTest {
//	 Static initializer to specify tests subset using TESTS_* static variables
//	 All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 13, 14 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public AssertionTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_4);
	}

	public static Class testClass() {
		return AssertionTest.class;
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"assert.java",
				"public class assert {}\n",
			},
			"""
				----------
				1. ERROR in assert.java (at line 1)
					public class assert {}
					             ^^^^^^
				Syntax error on token "assert", Identifier expected
				----------
				""");
	}

	public void test002() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				   try {\t
				    int i = 4;
				    assert i != 4;
					   System.out.println(i);
					  } catch(AssertionError e){\t
						System.out.print("SUCCESS");\t
					  }\s
					}\s
				}\s
				""" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}

	public void test003() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				    int i = 4;
				    assert i != 4;
					   System.out.println(i);
					}\s
				}\s
				""" },
		"4",
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-da"});
	}
	public void test004() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				   try {\t
						assert false : "SUC";\t
					  } catch(AssertionError e){\t
						System.out.print(e.getMessage());\t
					  }\t
					  try {\t
						assert false : new Object(){ public String toString(){ return "CESS";}};\t
					  } catch(AssertionError e){\t
						System.out.println(e.getMessage());\t
					  }\t
				  }\t
				}\s
				""" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test005() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				   try {\t
						assert false : 1;\t
					  } catch(AssertionError e){\t
						System.out.print(e.getMessage());\t
					  }\t
					  try {\t
						int i = 2;\t
						assert false : i;\t
					  } catch(AssertionError e){\t
						System.out.println(e.getMessage());\t
					  }\t
				  }\t
				}\s
				""" },
		"12", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test006() {
		this.runNegativeTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
					  try {\t
						assert false : unbound;\t
					  } catch(AssertionError e){\t
						System.out.println(e.getMessage());\t
					  }\t
				  }\t
				}\s
				""" },
		"""
			----------
			1. ERROR in A4.java (at line 4)
				assert false : unbound;\t
				               ^^^^^^^
			unbound cannot be resolved to a variable
			----------
			""");
	}
	public void test007() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				   try {\t
						assert false : 1L;\t
					  } catch(AssertionError e){\t
						System.out.print(e.getMessage());\t
					  }\t
				   try {\t
						assert false : 0L;\t
					  } catch(AssertionError e){\t
						System.out.print(e.getMessage());\t
					  }\t
					  try {\t
						long l = 2L;\t
						assert false : l;\t
					  } catch(AssertionError e){\t
						System.out.println(e.getMessage());\t
					  }\t
				  }\t
				}\s
				""" },
		"102", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test008() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				   try {\t
						assert false : 1.0f;\t
					  } catch(AssertionError e){\t
						System.out.print(e.getMessage());\t
					  }\t
					  try {\t
						float f = 2.0f;\t
						assert false : f;\t
					  } catch(AssertionError e){\t
						System.out.println(e.getMessage());\t
					  }\t
				  }\t
				}\s
				""" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // do not flush previous output dir content
		new String[] {"-ea"});
	}
	public void test009() {
		this.runConformTest(new String[] {
			"A4.java",
			"""
				public class A4 {\s
					public static void main(String[] args) {
				   try {\t
						assert false : 1.0;\t
					  } catch(AssertionError e){\t
						System.out.print(e.getMessage());\t
					  }\t
					  try {\t
						double d = 2.0;\t
						assert false : d;\t
					  } catch(AssertionError e){\t
						System.out.println(e.getMessage());\t
					  }\t
				  }\t
				}\s
				""" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=22334
	public void test010() {
		this.runConformTest(new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {\s
						I.Inner inner = new I.Inner();\s
						try {\s
							inner.test();\s
							System.out.println("FAILED");\s
						} catch(AssertionError e){\s
							System.out.println("SUCCESS");\s
						}\s
					}\s
				}\s
				interface I {\s
				  public static class Inner {\s
				    public void test() {\s
				      assert false;\s
				    }\s
				  }\s
				}\s
				""" },
		"SUCCESS",
		null, // use default classpath
		true, // flush previous output dir content
		new String[] {"-ea"});
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28750
	 */
	public void test011() {
		this.runConformTest(
			new String[] {
				"AssertTest.java",
				"""
					public class AssertTest {
					   public AssertTest() {}
					   public class InnerClass {
					      InnerClass() {
					        assert(false);
					      }
					   }
					  \s
					   public static void main(String[] args) {\t
					        System.out.print("SUCCESS");\t
						}\t
					}"""
			},
			"SUCCESS"); // expected output
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=57743
	 */
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main( String[] args ) {
					        try {
					            throw new Throwable( "This is a test");
					        }
					        catch( Throwable ioe ) {
					            assert false : ioe;
					        }
					        System.out.print("SUCCESS");\t
					    }
					}
					"""
			},
			"SUCCESS"); // expected output
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=157389
	 */
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					        static class Y {
					                public static void test() {
					                        assert false;
					                        System.out.println("SUCCESS");
					                }
					        }
					        public static void main(String[] args) {
					                ClassLoader classLoader = new X().getClass().getClassLoader();
					                // enable assertion for X.Y
					                classLoader.setClassAssertionStatus("X$Y", true);
					                X.Y.test();
					        }
					}"""
			},
			"SUCCESS"); // expected output
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=163600
	 */
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static class Foo {
							public void myMethod(boolean trash) {
								System.out.println("Expecting class Foo");
								Class c = Foo.class;
								System.out.println("Got the class " + c);
							}
						}
						public static class Bar {
							public void myMethod(boolean doAssert) {
								System.out.println("Expecting class Bar");
								Class c = Bar.class;
								System.out.println("Got the class " + c);
								assert c.getName().endsWith("Bar");
							}
						}
						public static void main(String[] args) {
							new Foo().myMethod(false);
							new Bar().myMethod(false);
						}
					}"""
			},
			"""
				Expecting class Foo
				Got the class class X$Foo
				Expecting class Bar
				Got the class class X$Bar"""); // expected output
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=163600
	 */
	public void test015() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static class Foo {
							public void myMethod(boolean trash) {
								System.out.println("Expecting class Foo");
								Class c = Foo.class;
								System.out.println("Got the class " + c);
							}
						}
						public static class Bar {
							public void myMethod(boolean doAssert) {
								System.out.println("Expecting class Bar");
								Class c = Bar.class;
								try {
									assert c.getName().endsWith("Bar2");
								} catch(AssertionError e) {
									System.out.println("SUCCESS");
								}
								System.out.println("Got the class " + c);
							}
						}
						public static void main(String[] args) {
							new Foo().myMethod(false);
							new Bar().myMethod(false);
						}
					}"""
			},
			"""
				Expecting class Foo
				Got the class class X$Foo
				Expecting class Bar
				SUCCESS
				Got the class class X$Bar""",
			null, // use default classpath
			true, // flush previous output dir content
			new String[] {"-ea"});
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=163600
	 */
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						public static class Foo {
							public void myMethod(boolean trash) {
								System.out.println("Expecting class Foo");
								Class c = Foo.class;
								System.out.println("Got the class " + c);
							}
						}
						public static class Bar {
							public void myMethod(boolean doAssert) {
								System.out.println("Expecting class Bar");
								Class c = Bar.class;
								try {
									assert c.getName().endsWith("Bar2");
									System.out.println("SUCCESS");
								} catch(AssertionError e) {
									System.out.println("FAILED");
								}
								System.out.println("Got the class " + c);
							}
						}
						public static void main(String[] args) {
							new Foo().myMethod(false);
							new Bar().myMethod(false);
						}
					}"""
			},
			"""
				Expecting class Foo
				Got the class class X$Foo
				Expecting class Bar
				SUCCESS
				Got the class class X$Bar""",
			null, // use default classpath
			true, // flush previous output dir content
			new String[] {"-da"});
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255008
	public void test017() {
		runNegativeTest(
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
						protected void transform1(boolean srcPts) {
							final float error1;
							assert !(srcPts && (error1 = maxError()) > 0) : error1;
						}
						float foo1(boolean srcPts) {
							final float error2;
							if (!(srcPts && (error2 = maxError()) > 0)) {
							} else {
								return error2;
							}
							return 0;
						}
						float bar1(boolean srcPts) {
							final float error3;
							if ((srcPts && (error3 = maxError()) > 0)) {
								return error3;
							}
							return 0;
						}\t
						protected void transform2(boolean srcPts) {
							final float error4;
							assert (srcPts && (error4 = maxError()) > 0) : error4;
						}
						float foo2(boolean srcPts) {
							final float error5;
							if (srcPts && (error5 = maxError()) > 0) {
							} else {
								return error5;
							}
							return 0;
						}
						float bar2(boolean srcPts) {
							final float error6;
							if (!(srcPts && (error6 = maxError()) > 0)) {
								return error6;
							}
							return 0;
						}
						private float maxError() {
							return 0;
						}
					
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 23)
					assert (srcPts && (error4 = maxError()) > 0) : error4;
					                                               ^^^^^^
				The local variable error4 may not have been initialized
				----------
				2. ERROR in X.java (at line 29)
					return error5;
					       ^^^^^^
				The local variable error5 may not have been initialized
				----------
				3. ERROR in X.java (at line 36)
					return error6;
					       ^^^^^^
				The local variable error6 may not have been initialized
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test018() {
		this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
				    static final int i;
				    static {
				        assert (i = 0) == 0;
				        System.out.println(i);
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				static final int i;
				                 ^
			The blank final field i may not have been initialized
			----------
			2. ERROR in X.java (at line 5)
				System.out.println(i);
				                   ^
			The blank final field i may not have been initialized
			----------
			""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test019() {
		this.runConformTest(new String[] {
			"X.java",
			"""
				public class X {
				    static final int i;
				    static {
				        i = 0;
				        assert i == 0;
				        System.out.println(i);
				    }
				}"""
		},
		"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    void method1() {
								 int i;\
						        assert (i = 0) == 0;
						        System.out.println(i);
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					System.out.println(i);
					                   ^
				The local variable i may not have been initialized
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int bar() {
							return 1;
						}
					    void method1() {
							 int i;\
					        assert (i = this.bar()) == 0;
					        System.out.println(i);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					System.out.println(i);
					                   ^
				The local variable i may not have been initialized
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328361
	public void test022() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int bar() {
							return 1;
						}
					    void method1() {
							 int i;
					        assert i++ == 0;
					        System.out.println(i);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					assert i++ == 0;
					       ^
				The local variable i may not have been initialized
				----------
				2. ERROR in X.java (at line 8)
					System.out.println(i);
					                   ^
				The local variable i may not have been initialized
				----------
				""");
	}
	public void test023() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(new String[] {"X.java",
				"""
					interface Foo {
					  default Object test(Object a) {
					    assert a != null; // triggers creation of bogus synthetic field
					    return a;
					  }
					}
					public class X implements Foo {
						public static void main(String[] args) {
							new X().test("");
							System.out.println("Hello");
						}
					}
					"""}, "Hello");
	}
}
