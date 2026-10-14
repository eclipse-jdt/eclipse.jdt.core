/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Replicas of tests from {@link SuperAfterStatementsTest} whose results differ when run
 * at 28 with preview enabled.
 */
@RunJavac
@PreviewTest
public class EarlyConstructionContextTests28 extends AbstractRegressionTestCommon {

	static {
//		TESTS_NAMES = new String[] { "testStaticFieldAccessThroughThis" };
	}

	public static Class<?> testClass() {
		return EarlyConstructionContextTests28.class;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_28);
	}

	public EarlyConstructionContextTests28(String testName) {
		super(testName);
	}

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 28");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	class Runner extends AbstractRegressionTest.Runner {
		public Runner() {
			super();
			this.customOptions = getCompilerOptions();
		}
	}

	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = getCompilerOptions(true);
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}

	@Override
	protected JavacTestOptions getJavacTestOptions() {
		return JAVAC_OPTIONS;
	}

	// i++ on a field without initializer is a legal early read under preview
	public void test008() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			class A {
			    int i;
			    A() {
			        i++;
			        hashCode();                 // Error
			        super();
			    }
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	hashCode();                 // Error\n" +
		"	^^^^^^^^^^\n" +
		"Cannot invoke method hashCode() in an early construction context\n" +
		"----------\n");
	}

	public void testGH3748a() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				final int fin1, fin2;
				{
					fin1 = 0;
					fin2 = 1;
				}
				X() {
					int abc = 0; // Commenting out this line brings out the error
					this(fin1 = 10);
					fin2 = 11;
				}
				X(int x) {}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	this(fin1 = 10);\n" +
		"	     ^^^^\n" +
		"Cannot refer to field fin1 in an early construction context\n" +
		"----------\n");
	}

	public void testGH3748b() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				final int fin1;
				final int fin2;
				{
					fin1 = 0;
					fin2 = 1;
				}
				X() {
					this(fin1 = 10);
					fin2 = 11;
				}
				X(int x) {}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	this(fin1 = 10);\n" +
		"	     ^^^^\n" +
		"Cannot refer to field fin1 in an early construction context\n" +
		"----------\n");
	}

	public void testGH4449a() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;

			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	this.i = 1;\n" +
		"	^^^^^^\n" +
		"Cannot refer to field i in an early construction context\n" +
		"----------\n");
	}

	public void testGH4449b() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;
			  public X(Integer o) { this(o.intValue()); }
			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	this.i = 1;\n" +
		"	^^^^^^\n" +
		"Cannot refer to field i in an early construction context\n" +
		"----------\n");
	}

	public void testGH4449c() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;
			  public X(Integer o) { this.i = o.intValue(); }
			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	this.i = 1;\n" +
		"	^^^^^^\n" +
		"Cannot refer to field i in an early construction context\n" +
		"----------\n");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5514
	// [28] ECJ accepts assignment of static field via 'this' before super()
	public void testStaticFieldAccessThroughThis() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				class Parent1 {}
				public class X {
				    static int x = -1;
				}
				class Child1 extends Parent1 {
				    static int f;
				    Child1(int i) {
				        this.f = i; // Javac rejects this
				        f = i; // Ok.
				        super();
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	this.f = i; // Javac rejects this\n" +
			"	^^^^^^\n" +
			"Cannot refer to field f in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	this.f = i; // Javac rejects this\n" +
			"	     ^\n" +
			"The static field Child1.f should be accessed in a static way\n" +
			"----------\n");
	}

	public void testStaticFieldAccessThroughThis_without_preview() {
		Runner runner = new Runner();
		runner.customOptions.put(CompilerOptions.OPTION_Source, "28");
		runner.customOptions.put(CompilerOptions.OPTION_Compliance, "28");
		runner.customOptions.put(CompilerOptions.OPTION_TargetPlatform, "28");
		runner.javacTestOptions = JavacTestOptions.forRelease(JavaCore.VERSION_28);
		runner.testFiles = new String[] {
				"X.java",
				"""
				class Parent1 {}
				public class X {
				    static int x = -1;
				}
				class Child1 extends Parent1 {
				    static int f;
				    Child1(int i) {
				        this.f = i; // Javac rejects this
				        f = i; // Ok.
				        super();
				    }
				}
				"""
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	this.f = i; // Javac rejects this\n" +
				"	^^^^^^\n" +
				"Cannot refer to field f in an early construction context\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 8)\n" +
				"	this.f = i; // Javac rejects this\n" +
				"	     ^\n" +
				"The static field Child1.f should be accessed in a static way\n" +
				"----------\n";
		runner.runNegativeTest();
	}

	// ===== Moved from ValueClassesAndObjectsTest: instance field reads in early construction contexts =====

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
	// Fields that are initialized at declaration site cannot be read in early construction context! (since the initialization happens at super() boundary)
	public void testFieldReadsInEarlyConstruction() { // check illegal access via FieldReference, SingleNameReference & QualifiedNameReference
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    X xo = new X();
				    int x;
				    int y;
				    int z = 10;
				    int k = 0;
				    X() {
				        x = 42;
				        y = x + xo.x + z + this.k;
				        super();
				        System.out.println("x = " + x + ", y = " + y);
				    }

				    public static void main() {
				        new X();
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	y = x + xo.x + z + this.k;\n" +
			"	        ^\n" +
			"Cannot refer to field xo in an early construction context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	y = x + xo.x + z + this.k;\n" +
			"	               ^\n" +
			"Cannot refer to field z in an early construction context\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	y = x + xo.x + z + this.k;\n" +
			"	                   ^^^^^^\n" +
			"Cannot refer to field k in an early construction context\n" +
			"----------\n");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
	public void testFieldReadsInEarlyConstruction_2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    final X xo;
				    int x;
				    int y;
				    X() {
				        x = 42;
				        y = x + xo.x;
				        super();
				        System.out.println("x = " + x + ", y = " + y);
				    }

				    public static void main() {
				        new X();
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	y = x + xo.x;\n" +
			"	        ^^\n" +
			"The blank final field xo may not have been initialized\n" +
			"----------\n");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
	public void testFieldReadsInEarlyConstruction_3() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    final String xo;
				    int x;
				    int y;
				    X() {
				        x = 42;
				        xo = "Hello";
				        y = x + xo.length();
				        super();
				        System.out.println("x = " + x + ", y = " + y);
				    }

				    public static void main() {
				        new X();
				    }
				}
				"""
			},
			"x = 42, y = 47");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
	// Exercise reads found in the prologue, including receivers and operands
	// of assignments, plus the argument of the explicit constructor call.
	public void testFieldReadsInEarlyConstructor() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X extends SuperType {
			    static int staticField = 17;

			    int directRead;
			    int assigned;
			    int rhsRead;
			    int[] array;
			    int index;
			    Box object;
			    int compound;
			    int increment;
			    int constructorArgument;
			    Box qualifiedRoot;
			    int afterConstructorCall;
			    final int blankFinal;

			    static class Box {
			        int field;
			    }

			    X(int rhsRead) {
			        directRead = 1;
			        assigned = rhsRead;             // parameter shadows the field rhsRead
			        this.rhsRead = 3;
			        array = new int[2];
			        index = 1;
			        array[index] = directRead + assigned;
			        object = new Box();
			        object.field = array[index];   // object is read on the LHS
			        compound = 4;
			        compound += object.field;       // reads old compound and object
			        increment = 2;
			        increment++;               // reads old increment
			        constructorArgument = 5;
			        qualifiedRoot = new Box();
			        qualifiedRoot.field = 7;
			        blankFinal = 11;
			        int localOnly = 6;

			        System.out.println("pre=" + directRead + "," + assigned
			            + "," + rhsRead + "," + array[index] + ","
			            + object.field + "," + compound + "," + increment
			            + "," + blankFinal + "," + staticField + "," + localOnly);
			        super(constructorArgument + qualifiedRoot.field + blankFinal);
			        afterConstructorCall = directRead;
			        System.out.println("after=" + afterConstructorCall + ","
			            + compound + "," + blankFinal);
			    }

			    public static void main(String[] args) {
			        new X(9);
			    }
			}

			class SuperType {
			    SuperType(int value) {
			        System.out.println("super=" + value);
			    }
			}
			"""
		},
		"pre=1,9,9,10,10,14,3,11,17,6\n" +
		"super=23\n" +
		"after=1,14,11");
	}

	public void testTooEagerReadOfFinalField() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    final int i = 42;
						    final double d;
						    X() {
						        System.out.println("i = " + i);
						        System.out.println("d = " + d); // flow analysis not done due to resolve error in previous line
						        super();
						    }
						    void main() {
						    }
						}
			    		""" },
						"----------\n" +
						"1. ERROR in X.java (at line 5)\n" +
						"	System.out.println(\"i = \" + i);\n" +
						"	                            ^\n" +
						"Cannot refer to field i in an early construction context\n" +
						"----------\n");
	}

	public void testTooEagerReadOfBlankFinalField() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    final double d;
						    X() {
						        System.out.println("d = " + d);
						        super();
						    }
						    void main() {
						    }
						}
			    		""" },
						"----------\n" +
						"1. ERROR in X.java (at line 4)\n" +
						"	System.out.println(\"d = \" + d);\n" +
						"	                            ^\n" +
						"The blank final field d may not have been initialized\n" +
						"----------\n");
	}

	public void testDefaultInitializationsInEarlyReads() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    boolean b;
						    char c;
						    byte by;
						    short s;
						    int i;
						    long l;
						    float f;
						    double d;
						    String string;
						    X() {
						        System.out.println("b = " + b);
						        System.out.println("c = " + c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + l);
						        System.out.println("f = " + f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						        super();
						    }
						    void main() {
						    }
						}
			    		""" },
						"b = false\n" +
						"c = \u0000\n" +
						"by = 0\n" +
						"s = 0\n" +
						"i = 0\n" +
						"l = 0\n" +
						"f = 0.0\n" +
						"d = 0.0\n" +
						"string = null");
	}

	public void testDefaultInitializationsInEarlyReadsThroughThis() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    boolean b;
						    char c;
						    byte by;
						    short s;
						    int i;
						    long l;
						    float f;
						    double d;
						    String string;
						    X() {
						        c = 'A';
						        this.i = 42;
						        s = 99;
						        this.f = 134.456f;
						        d = 456.789;
						        this.l = 9876543;
						        string = "Hello world";
						        System.out.println("b = " + b);
						        System.out.println("c = " + c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + l);
						        System.out.println("f = " + f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						        super();
						        System.out.println("b = " + b);
						        System.out.println("c = " + this.c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + this.l);
						        System.out.println("f = " + this.f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						    }
						    void main() {
						    }
						}
			    		""" },
						"b = false\n" +
						"c = A\n" +
						"by = 0\n" +
						"s = 99\n" +
						"i = 42\n" +
						"l = 9876543\n" +
						"f = 134.456\n" +
						"d = 456.789\n" +
						"string = Hello world\n" +
						"b = false\n" +
						"c = A\n" +
						"by = 0\n" +
						"s = 99\n" +
						"i = 42\n" +
						"l = 9876543\n" +
						"f = 134.456\n" +
						"d = 456.789\n" +
						"string = Hello world");
	}

	// See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5475
	public void testDefaultInitializationsInEarlyReadsThroughThis_negative() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    boolean b;
						    char c;
						    byte by;
						    short s;
						    int i;
						    long l;
						    float f;
						    double d;
						    String string;
						    X() {
						        c = 'A';
						        this.i = 42;
						        s = 99;
						        this.f = 134.456f;
						        d = 456.789;
						        this.l = 9876543;
						        string = "Hello world";
						        System.out.println("b = " + b);
						        System.out.println("c = " + this.c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + this.l);
						        System.out.println("f = " + this.f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						        super();
						        System.out.println("b = " + b);
						        System.out.println("c = " + this.c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + this.l);
						        System.out.println("f = " + this.f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						    }
						    void main() {
						    }
						}
			    		""" },
				"----------\n" +
				"1. ERROR in X.java (at line 20)\n" +
				"	System.out.println(\"c = \" + this.c);\n" +
				"	                            ^^^^^^\n" +
				"Cannot refer to field c in an early construction context\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 24)\n" +
				"	System.out.println(\"l = \" + this.l);\n" +
				"	                            ^^^^^^\n" +
				"Cannot refer to field l in an early construction context\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 25)\n" +
				"	System.out.println(\"f = \" + this.f);\n" +
				"	                            ^^^^^^\n" +
				"Cannot refer to field f in an early construction context\n" +
				"----------\n");
	}

	public void testFieldAssignmentInSuperCallIsPreserved() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				class S {
				    S(int i, int j) {}
				}
				public class X extends S {
				    int x;
				    int y;
				    X() {
				        super(x = 42, y += 99);
				        System.out.println(x + " " + y);
				    }
				    public static void main(String[] args) {
				        new X();
				    }
				}
				"""
			},
			"42 99");
	}

	public void testTooEagerReadOfSuperField() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						class Super {
						    int i;
						    Super(int k) {}
						}

						 class Test extends Super {
						    Test() {
						        super(i);
						    }
						}
			    		""" },
						"----------\n" +
						"1. ERROR in X.java (at line 8)\n" +
						"	super(i);\n" +
						"	      ^\n" +
						"Cannot refer to field i in an early construction context\n" +
						"----------\n");
	}

	public void testProxyFlush() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				class S {
				    S() {
				    }
				}

				public class X extends S {
				    int x;

				    {
				        x = 2;
				    }

				    X() {
				        x = 1;
				        int ignored = x; // causes x to receive a proxy
				        super();
				    }

				    public static void main(String[] args) {
				        System.out.print(new X().x);
				    }
				}
				"""
			},
			"2");
	}

	public void testFieldAccessBeforeAlternateConstructorCall() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				class S {
				    S() {
				    }
				}

				public class X extends S {
				    int x;

				    {
				        x = 2;
				    }

				    X(int x) {}
				    X() {
				        x = 1;
				        int ignored = x;
				        this(x);
				        System.out.println(x);
				    }

				    public static void main(String[] args) {
				        System.out.print(new X().x);
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 15)\n" +
			"	x = 1;\n" +
			"	^\n" +
			"Cannot refer to field x in an early construction context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	int ignored = x;\n" +
			"	              ^\n" +
			"Cannot refer to field x in an early construction context\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 17)\n" +
			"	this(x);\n" +
			"	     ^\n" +
			"Cannot refer to field x in an early construction context\n" +
			"----------\n");
	}

	public void testPrematureProxy() throws Exception {
		runConformTest(new String[] {
			"X.java",
			"""
			class S {
			    S() {
			        init();
			    }
			    void init() {}
			}

			public class X extends S {
			    int x;

			    X() {
			        int x = 1;      // local shadows the field
			        int y = x;      // reads the LOCAL, not the field
			        if (y != 1)
			            throw new AssertionError(y);
			        super();
			    }

			    @Override
			    void init() {
			        this.x = 42;    // field written during super()
			    }

			    public static void main(String[] args) {
			        System.out.println(new X().x);
			    }
			}
			"""
		},
		"42");
	}

	public void testProxyDoesntLeakIntoLambdaOrLocalClass() throws Exception {
		runNegativeTest(new String[] {
			"X.java",
			"""
			import java.util.function.Supplier;

			public class X {
				int f;

				X() {
					int early = f; // force proxy

					Supplier<Integer> s = () -> f;
					class L {
						L() {
							int e = f;
						}

						int get() {
							return f;
						}
					}

					int a = s.get();
					int b = new L().get();
					if (a != 0 || b != 0) {
						throw new AssertionError(a + ":" + b);
					}
					super();
				}

				public static void main(String[] args) {
					new X();
				}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	Supplier<Integer> s = () -> f;\n" +
		"	                            ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	int e = f;\n" +
		"	        ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	return f;\n" +
		"	       ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	public void testInheritedFieldReadInEarlyConstructionContext() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				class S {
				    int inherited;
				}

				public class X extends S {
				    int own;

				    X() {
				        int a = inherited; // inherited field: should be rejected
				        int b = own;       // current-class field
				        super();
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	int a = inherited; // inherited field: should be rejected\n" +
			"	        ^^^^^^^^^\n" +
			"Cannot refer to field inherited in an early construction context\n" +
			"----------\n"
		);
	}

	public void testProxyFlushDoesNotClobberWritesDuringSuper() {
		runConformTest(new String[] {
			"X.java",
			"""
			class S {
			    S() {
			        init();
			    }
			    void init() {}
			}

			public class X extends S {
			    int x;
			    int y = 99;
			    X() {
			        x = 1;
			        int y = x;      // reads the LOCAL, not the field
			        if (y != 1)
			            throw new AssertionError(y);
			        super();
			    }

			    @Override
			    void init() {
			        this.x = 42;    // field written during super()
			        this.y = 24;    // field written during super()
			    }

			    public static void main(String[] args) {
			        X x = new X();
			        System.out.println(x.x);
			        System.out.println(x.y);
			    }
			}
			"""
		},
		"42\n99");
	}

	public void testNoVerifyErrorOnQualifiedThis() {
		runConformTest(new String[] {
			"X.java",
			"""
			class ProblemReporter {
				public void record() {
				}
			}

			interface ISourceElementRequestor {
				void acceptProblem(String problem);
			}

			public class X {
				ISourceElementRequestor requestor;
				ProblemReporter problemReporter;

				public X() {
					this.requestor = p -> System.out.println(p);
					this.problemReporter = new ProblemReporter() {
						@Override
						public void record() {
							X.this.requestor.acceptProblem("No Problem : 1");
						}
					};
					X.this.requestor.acceptProblem("ReferenceOfFieldOfThis"); // only for this we should build a ReferenceOfFieldOfThis
				}

				public String toString() {
					return "Some X";
				}

				void foo() {
					X.this.requestor.acceptProblem("No Problem : 3");
				}

				void X() {
					X.this.requestor.acceptProblem("No Problem : 2");
				}

				public static void main(String[] args) {
					new X().problemReporter.record();
					new X().X();
					new X().foo();
				}
			}
			"""
		},
		"ReferenceOfFieldOfThis\n" +
		"No Problem : 1\n" +
		"ReferenceOfFieldOfThis\n" +
		"No Problem : 2\n" +
		"ReferenceOfFieldOfThis\n" +
		"No Problem : 3");
	}

	public void testECC_legal_read_of_declared_field() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;

				X() {
					x = 10;
					int y = x;
					super();
					System.out.println(y);
				}

				public static void main(String[] args) {
					new X();
				}
			}
			""",
		}, "10");
	}

	public void testECC_illegal_read_of_this_field() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;

				X() {
					x = 42;
					int y = this.x;
					super();
					System.out.println(y);
				}

				public static void main(String[] args) {
					new X();
				}
			}
			""",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	int y = this.x;\n" +
		"	        ^^^^^^\n" +
		"Cannot refer to field x in an early construction context\n" +
		"----------\n");
	}

	public void testECC_legal_read_in_expression_and_assignment() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;
				int y;

				X() {
					x = 5;
					y = x + 2;
					super();
					System.out.println(y);
				}

				public static void main(String[] args) {
					new X();
				}
			}
			""",
		}, "7");
	}

	public void testProxyCreationResumesAfterLambdaDetour() {
		runConformTest(new String[] {
			"X.java",
			"""
			interface I { int get(); }

			public class X {
			    int a;
			    int b;

			    X() {
			        a = 1;
			        I i = () -> 0;   // detour out and back
			        int x = b;       // first read of b after return: must still get proxy
			        super();
			        System.out.println(x);
			    }

			    public static void main(String[] args) {
			        new X();
			    }
			}
			"""
		}, "0");
	}

	public void testProxyCreationResumesAfterLocalClassDetour() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
			    int a;
			    int b;

			    X() {
			        a = 1;
			        class L {
			            int get() { return 0; }
			        }
			        int x = b;   // first read of b after detour: must still get proxy
			        super();
			        System.out.println(x);
			    }

			    public static void main(String[] args) {
			        new X();
			    }
			}
			"""
		}, "0");
	}
}