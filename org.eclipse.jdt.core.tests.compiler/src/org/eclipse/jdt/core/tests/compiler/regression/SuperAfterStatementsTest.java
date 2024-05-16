/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SuperAfterStatementsTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 22");
	private static final String[] VMARGS = new String[] {"--enable-preview"};
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test038" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return SuperAfterStatementsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_22);
	}
	public SuperAfterStatementsTest(String testName) {
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(false);
	}
	protected String[] getDefaultClassPaths() {
		String[] libs = DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
		if (this.extraLibPath != null) {
			String[] l = new String[libs.length + 1];
			System.arraycopy(libs, 0, l, 0, libs.length);
			l[libs.length] = this.extraLibPath;
			return l;
		}
		return libs;
	}
	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(false, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE22Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
	}
	protected void runConformTest(
			String[] testFiles,
			String expectedOutputString,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				null /* no custom options */,
				false /* do not perform statements recovery */,
				null /* no custom requestor */,
				// compiler results
				false /* expecting no compiler errors */,
				null /* do not check compiler log */,
				// runtime options
				false /* do not force execution */,
				vmArguments /* vm arguments */,
				// runtime results
				expectedOutputString /* expected output string */,
				null /* do not check error string */,
				// javac options
				JavacTestOptions.DEFAULT /* default javac test options */);
		}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test001() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				class Y {
					public int v;
					Y(int v) {
						this.v = v;
					}
				}
				public class X extends Y {

				    public X(int value) {
				        if (value <= 0)
				            throw new IllegalArgumentException("non-positive value");
				        super(value);
				    }
				    public static void main(String[] args) {
						System.out.println(new X(100).v);
						Zork();
					}
				}
      			"""
			},
			"""
				----------
				1. WARNING in X.java (at line 12)
					super(value);
					^^^^^^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				2. ERROR in X.java (at line 16)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void test002() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int v;
						Y(int v) {
							this.v = v;
						}
					}
					@SuppressWarnings("preview")
					public class X extends Y {

					    public X(int value) {
					        if (value <= 0)
					            throw new IllegalArgumentException("non-positive value");
					        super(value);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.v);
						}
					}
				"""
			},
			"100");
	}
	public void test003() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int v;
						Y(int v) {
							this.v = v;
						}
					}
					@SuppressWarnings("preview")
					public class X extends Y {

					    public X(int value) {
					        if (value <= 0)
					            throw new IllegalArgumentException("non-positive value");
					        super(value);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.v);
						}
					}
				"""
			},
			"100");
	}
	public void test004() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int[] vArr;
						Y(int[] vArr) {
							this.vArr = new int[vArr.length];
							for (int i = 0, l = vArr.length; i < l; ++i) {
								this.vArr[i] = vArr[i];
							}
						}
					}
					public class X extends Y {

						public X(int v) {
					        var iVal = Integer.valueOf(v);
					        if (iVal == 0)
					            throw new IllegalArgumentException("0 not allowed");
					        final int[] vArray = switch (iVal) {
					            case 1 -> new int[] { 1, 2, 3, 4};
					            case 2 -> new int[] { 2,3,4};
					            default -> new int[] {100, 200};
					        };
					        super(vArray);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.vArr[0]);
							X x2 = new X(1);
							System.out.println(x2.vArr[0]);
						}
					}
				"""
			},
			"100\n" +
			"1");
	}
	public void test005() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y {
						public int[] vArr;
						private F f1;
						private F f2;

						Y(F f1, F f2) {
							this.f1 = f1;
							this.f2 = f2;
						}
					}
					class F {}
					public class X extends Y {
						public int i;
						public X(int i) {
					        var f = new F();
					        super(f, f);
					        this.i = i;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
							X x2 = new X(1);
							System.out.println(x2.i);
						}
					}
				"""
			},
			"100\n" +
			"1");
	}
	// any unqualified this expression is disallowed in a pre-construction context:
	public void test006() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					class A {
					    int i;
					    A() {
					        this.i++;                   // Error
					        this.hashCode();            // Error
					        System.out.print(this);     // Error
					        super();
					    }
					}
				"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						this.i++;                   // Error
						^^^^
					Cannot use this in a pre-construction context
					----------
					2. ERROR in X.java (at line 5)
						this.hashCode();            // Error
						^^^^
					Cannot use this in a pre-construction context
					----------
					3. ERROR in X.java (at line 6)
						System.out.print(this);     // Error
						                 ^^^^
					Cannot use this in a pre-construction context
					----------
					4. WARNING in X.java (at line 7)
						super();
						^^^^^^^^
					You are using a preview language feature that may or may not be supported in a future release
					----------
					""");
	}
	// any field access, method invocation, or method reference
	// qualified by super is disallowed in a pre-construction context:
	public void test007() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class D {
					    int i;
					}
					class E extends D {
					    E() {
					        super.i++;                  // Error
					        super();
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					super.i++;                  // Error
					^^^^^
				Cannot use super in a pre-construction context
				----------
				2. WARNING in X.java (at line 7)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				"""
		);
	}
	// an illegal access does not need to contain a this or super keyword:
	public void test008() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class A {
					    int i;
					    A() {
					        i++;                        // Error
					        hashCode();                 // Error
					        super();
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					i++;                        // Error
					^
				Cannot use i in a pre-construction context
				----------
				2. ERROR in X.java (at line 5)
					hashCode();                 // Error
					^^^^^^^^^^
				Cannot use hashCode() in a pre-construction context
				----------
				3. WARNING in X.java (at line 6)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	//an expression involving this does not refer to the current instance but,
	// rather, to the enclosing instance of an inner class:
	public void test009() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class B {
					    int b;
					    class C {
					        int c;
					        C() {
					            B.this.b++;             // Allowed - enclosing instance
					            C.this.c++;             // Error - same instance
					            super();
					        }
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					C.this.c++;             // Error - same instance
					^^^^^^
				Cannot use C.this in a pre-construction context
				----------
				2. WARNING in X.java (at line 8)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	/* The invocation hello() that appears in the pre-construction context of the
	 * Inner constructor is allowed because it refers to the enclosing instance of
	 * Inner (which, in this case, has the type Outer), not the instance of Inner
	 * that is being constructed
	 */
	public void test010() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    void hello() {
					        System.out.println("Hello");
					    }
					    class Inner {
					        Inner() {
					            hello();                // Allowed - enclosing instance method
					            super();
					        }
					    }
					    public static void main(String[] args) {
							new X().new Inner();
						}
					}
				"""
			},
			"Hello");
	}
	/* The expression new Inner() is illegal because it requires providing the Inner constructor
	 * with an enclosing instance of Outer, but the instance of Outer that would be provided is
	 * still under construction and therefore inaccessible.
	 */
	public void test011() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Outer {
					    class Inner {}
					    Outer() {
					        new Inner(); // Error - 'this' is enclosing instance
					        super();
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					new Inner(); // Error - 'this' is enclosing instance
					^^^^^^^^^^^
				Cannot use new Inner() in a pre-construction context
				----------
				2. WARNING in X.java (at line 5)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes cannot have the newly created object as the implicit enclosing
	 * instance
	 */
	public void test012() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class X {
					    class S {}
					    X() {
					        var tmp = new S() { };      // Error
					        super();
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					var tmp = new S() { };      // Error
					          ^^^^^^^^^^^
				Cannot use new S() {
				  x() {
				    super();
				  }
				} in a pre-construction context
				----------
				2. WARNING in X.java (at line 5)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test013() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    X() {
					        S tmp = new S(){};    // OK
					        super();
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
					class S {}
				"""
			},
			"hello");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test014() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    static class S {}
					    X() {
					        S tmp = new S() { };    // OK
					        super();
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
				"""
			},
			"hello");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test015() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    static class S {}
					    X() {
					        var tmp = new S() { };    // OK
					        super();
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
				"""
			},
			"hello");
	}
	/* Here the enclosing instance of the class instance creation expression is not
	 * the newly created U object but, rather, the lexically enclosing O instance.
	 */
	public void test016() {
		runConformTest(new String[] {
			"X.java",
				"""
					class X {
					    class S {}
					    class U {
					        U() {
					            var tmp = new S() { };  // Allowed
					            super();
					        }
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
				"""
			},
			"hello");
	}
	/* A return statement may be used in the epilogue of a constructor body
	 * if it does not include an expression (i.e. return; is allowed,
	 * but return e; is not).
	 */
	public void test017() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							super(i);
					        return;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"100");
	}
	/* It is a compile-time error if a return statement that includes an expression
	 *  appears in the epilogue of a constructor body.
	 */
	public void test018() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							super(i);
					        return 0; // Error - return not allowed here
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					return 0; // Error - return not allowed here
					^^^^^^^^^
				Void methods cannot return a value
				----------
				""");
	}
	/* It is a compile-time error if a return statement appears in the prologue of a constructor body.
	 */
	public void test019() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
					        return; // Error - return not allowed here
							super(i);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					return; // Error - return not allowed here
					^^^^^^^
				return ; statement not allowed in prologue
				----------
				2. WARNING in X.java (at line 10)
					super(i);
					^^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	/* It is a compile-time error if a return statement appears in the prologue of a constructor body.
	 */
	public void test020() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					public class X {
						public int i;
						public X(int i) {
					        return; // Error - return not allowed here
							this(i, 0);
					    }
						public X(int i, int j) {
							this.i = i + j;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					return; // Error - return not allowed here
					^^^^^^^
				return ; statement not allowed in prologue
				----------
				2. WARNING in X.java (at line 5)
					this(i, 0);
					^^^^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	/* Throwing an exception in a prologue of a constructor body is permitted.
	 */
	public void test021() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							if (i < 0)
								throw new IllegalArgumentException();
							super(i);
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"100");
	}
	/* Throwing an exception in a prologue of a constructor body is permitted.
	 */
	public void test022() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y{
						public int i;
						Y(int i){
							this.i = i;
						}
					}
					public class X extends Y{
						public X(int i) {
							if (i < 0)
								throw new IllegalArgumentException();
							super(i);
					    }
					    public static void main(String[] args) {
					    	try {
					    		X x = new X(-1);
					    	} catch (IllegalArgumentException e) {
					    		System.out.println("hello");
					    	}
						}
					}
				"""
			},
			"hello");
	}
	/* Unlike in a static context, code in a pre-construction context may refer to the type
	 * of the instance under construction, as long as it does not access the instance itself:
	 */
	public void test023() {
		runConformTest(new String[] {
			"X.java",
				"""
					class B {
						B(Object o) {}
					 }
					class X<T> extends B {
					    X(Z<?> z) {
					        super((T)z.get(0));      // Allowed - refers to 'T' but not 'this'
					    }
					    public static void main(String[] args) {
							System.out.println("hello");
						}
					}
					class Z<T> {
						T get(int i) {
							return null;
						}
					}
				"""
			},
			"hello");
	}
	public void test024() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class A{
					    public int i;
					    A(int i) {
					        this.i = i;
					    }
					}

					public class X{
					    A a = new A(0);
					    public boolean b;
					    X(int i) {
					    	int j = a.i;
					    	this.b = j == 0;
					        super();
					    }
					    public static void main(String[] argv) {
					    	System.out.println(new X(0).b);
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					int j = a.i;
					        ^^^
				Cannot use a.i in a pre-construction context
				----------
				2. ERROR in X.java (at line 13)
					this.b = j == 0;
					^^^^
				Cannot use this in a pre-construction context
				----------
				3. WARNING in X.java (at line 14)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	/* Its an error of this is used in super(this) - no change for this error
	 */
	public void test025() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class B {
						B(Object o) {}
					 }
					public class X<T> extends B {
				    	X() {
			        		super(this); // Error - refers to 'this'
			   			}
					}
					class Z<T> {
						T get(int i) {
							return null;
						}
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					super(this); // Error - refers to 'this'
					      ^^^^
				Cannot refer to 'this' nor 'super' while explicitly invoking a constructor
				----------
				""");
		}
	/**
	 *
	 */
	public void test026() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {}
					interface I {
					  String tos();
					 }
					public class X {
						int i;
						public X(int i) {
							I tos = super::toString;
							this(i, 0);
					    }
						public X(int i, int j) {
							this.i = i + j;
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					I tos = super::toString;
					        ^^^^^
				Cannot use super in a pre-construction context
				----------
				2. WARNING in X.java (at line 9)
					this(i, 0);
					^^^^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	public void test027() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class A{
					    public int i;
					    A(int i) {
					        this.i = i;
					    }
						public int getI() { return this.i; }
					}

					public class X{
					    A a = new A(0);
					    public boolean b;
					    X(int i) {
					    	int j = a.getI();
					    	this.b = j == 0;
					        super();
					    }
					    public static void main(String[] argv) {
					    	System.out.println(new X(0).b);
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					int j = a.getI();
					        ^
				Cannot use a in a pre-construction context
				----------
				2. ERROR in X.java (at line 14)
					this.b = j == 0;
					^^^^
				Cannot use this in a pre-construction context
				----------
				3. WARNING in X.java (at line 15)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	public void test028() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					interface I {
					    default int getI() { return 0; }
					}
					interface J extends I {}
					public class X implements J {
						int i;
					    X() {
					        int j = J.super.getI();
					        super();
					    }
					    X(int i) {
					    	this.i = i;
					    }
					    public static void main(String argv[]) {
					    	System.out.println(new X(0).getI() == 0);
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					int j = J.super.getI();
					        ^^^^^^^
				Cannot use J.super in a pre-construction context
				----------
				2. WARNING in X.java (at line 9)
					super();
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	public void test029() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					interface I {
					    default int getI() { return 0; }
					}
					interface J extends I {}
					public class X implements J {
						int i;
					    X() {
					        int j = J.super.getI();
					        this(j);
					    }
					    X(int i) {
					    	this.i = i;
					    }
					    public static void main(String argv[]) {
					    	System.out.println(new X(0).getI() == 0);
					    }
					}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					int j = J.super.getI();
					        ^^^^^^^
				Cannot use J.super in a pre-construction context
				----------
				2. WARNING in X.java (at line 9)
					this(j);
					^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				""");
	}
	public void test030() {
		runConformTest(new String[] {
			"X.java",
				"""
					public class X {
					    int v = 0;
					    public boolean foo() {
					        class Inner {
					            int getV() {
					                return v;
					            }
					        }
					        return new Inner(){}.getV() == v;
					    }
					    public static void main(String args[]) {
					    	System.out.println(new X().foo());
					    }
					}
				"""
			},
			"true");
	}
	public void test031() {
		runNegativeTest(new String[] {
			"X.java",
			"""
				       class X {
				       int j = 0;
				       X() {}
				       X(int i) {
				           if (i == 0) {
				               String s = STR."\\{j}";
				               i += s.length();
				           }
				           this();
				       }
				       public static void main(String[] args) {
				         Zork();
				         System.out.println(0);
				       }
				   }
				"""
			},
				"""
					----------
					1. WARNING in X.java (at line 6)
						String s = STR."\\{j}";
						           ^^^^^^^^^
					You are using a preview language feature that may or may not be supported in a future release
					----------
					2. ERROR in X.java (at line 6)
						String s = STR."\\{j}";
						                  ^
					Cannot use j in a pre-construction context
					----------
					3. WARNING in X.java (at line 9)
						this();
						^^^^^^^
					You are using a preview language feature that may or may not be supported in a future release
					----------
					4. ERROR in X.java (at line 12)
						Zork();
						^^^^
					The method Zork() is undefined for the type X
					----------
					"""
			);
	}
	public void test032() {
		runConformTest(new String[] {
			"X.java",
				"""
				abstract class Y {
					public abstract int getI();
				}
				public class X {
					public int i;
				    X() {
				         new Y() {
				            public int getI() {
				                return 0;
				            }
				        }.getI();
				        super();
				    }
				   public static void main(String argv[]) {
					   System.out.println(new X().i);
				    }
				}
			"""
			},
			"0"
		);
	}
	public void test033() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {
						public Y() {}
						public Y(int i){}
					}
					class X extends Y {
						public boolean b;;
						public X(int i){}
					    	public X (boolean b) {
					          super();
					          this(0);
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"""
			----------
			1. ERROR in X.java (at line 10)
				this(0);
				^^^^^^^^
			Constructor call must be the first statement in a constructor
			----------
			"""
			);
	}
	public void test034() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {
						public Y(int i){}
					}
					class X extends Y {
						public boolean b;;
						public X(int i){}
					    	public X (boolean b) {
					          super();
					          this(0);
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"""
			----------
			1. ERROR in X.java (at line 6)
				public X(int i){}
				       ^^^^^^^^
			Implicit super constructor Y() is undefined. Must explicitly invoke another constructor
			----------
			2. ERROR in X.java (at line 8)
				super();
				^^^^^^^^
			The constructor Y() is undefined
			----------
			3. ERROR in X.java (at line 9)
				this(0);
				^^^^^^^^
			Constructor call must be the first statement in a constructor
			----------
			"""
			);
	}
	public void test035() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y {
						public Y(int i){}
					}
					class X extends Y {
						public boolean b;;
						public X(int i){ super(i);}
					    	public X (boolean b) {
					          super();
					          this(0);
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"""
			----------
			1. ERROR in X.java (at line 8)
				super();
				^^^^^^^^
			The constructor Y() is undefined
			----------
			2. ERROR in X.java (at line 9)
				this(0);
				^^^^^^^^
			Constructor call must be the first statement in a constructor
			----------
			"""
			);
	}
	public void test036() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class Y { }
					class X extends Y {
					        public boolean b;;
					        public X(){}
					        public X (boolean b) {
					          super();
					          this();
					          this.b = b;
					    }
					    public static void main(String argv[]) {
					        System.out.println(new X(true).b);
					    }
					}
				"""
			},
		"""
			----------
			1. ERROR in X.java (at line 7)
				this();
				^^^^^^^
			Constructor call must be the first statement in a constructor
			----------
			"""
			);
	}
	// Disabled till https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2373 is fixed
	public void _test037() {
		runConformTest(new String[] {
			"X.java",
				"""
					interface I {
					    void foo();
					}
					public class X {
					    public static boolean b;
					    static class Y { boolean b = true;}
					    X() {}
					    X(boolean b) {
					        I l = () -> {
					            X.b = new Y() {
					                public boolean b() {
					                    return this.b;
					                }
					            }.b();
					        };
					        l.foo();
					        super();
					    }

					    public static void main(String argv[]) {
					    	new X(true);
					        System.out.println(X.b);
					    }
					}
      			"""
			},
			"true"
		);
	}
	public void test038() {
		runConformTest(new String[] {
			"X.java",
				"""
					interface I {
						void foo();
					}

					public class X {
						public static boolean b;

						static class Y {
							boolean b = true;
						}
						X() {}
						X(boolean b) {
							I l = () -> {
								X.b = new Y() {
									public boolean b() {
										return this.b;
									}
								}.b();
								System.out.println(switch (42) {
								default -> {
									try {
										yield 42;
									} finally {

									}
								}
								});
							};
							l.foo();
							super();
						}

						public static void main(String argv[]) {
							new X(true);
							System.out.println(X.b);
						}
					}
				"""
			},
			"42\n" +
			"true"
		);
	}
	public void test039() {
		runConformTest(new String[] {
			"X.java",
				"""
					interface I {
						void foo();
					}

					public class X {
						public static boolean b;

						static class Y {
							boolean b = true;
						}
						X() {}
						X(boolean b) {
							I l = () -> {
								X.b = new Y() {
									public boolean b() {
										return this.b;
									}
								}.b();
								System.out.println(switch (42) {
								default -> {
									try {
										yield 42;
									} finally {

									}
								}
								});
							};
							l.foo();
							super();
						}

						public static void main(String argv[]) {
							new X(true);
							System.out.println(X.b);
						}
					}
				"""
			},
			"42\n" +
			"true"
		);
	}
}
