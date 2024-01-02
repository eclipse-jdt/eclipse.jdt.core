/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 22");
	static {
		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test001" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return SuperAfterStatementsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_22);
	}
	public SuperAfterStatementsTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_22);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_22);
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
		runConformTest(testFiles, expectedOutput, getCompilerOptions(false));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE21Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {}, JAVAC_OPTIONS);
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
		Map<String, String> options = getCompilerOptions(false);
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
			"100",
			options);
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
	public void test004() {
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
	public void test005() {
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
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	3 errors expected" +
				"	                                           ^^\n" +
				"cannot reference this before supertype constructor has been called\n" +
				"----------\n");
	}
	// any field access, method invocation, or method reference
	// qualified by super is disallowed in a pre-construction context:
	public void test006() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	1 error expected" +
			"	                                           ^^\n" +
			"error: cannot reference i before supertype constructor has been called\n" +
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	1 error expected" +
			"	                                           ^^\n" +
			"error: cannot reference hashCode() before supertype constructor has been called\n" +
			"----------\n"
		);
	}
	// an illegal access does not need to contain a this or super keyword:
	public void test007() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	3 errors expected" +
			"	                   ^^\n" +
			"cannot reference this before supertype constructor has been called\n" +
			"----------\n");
	}
	//an expression involving this does not refer to the current instance but,
	// rather, to the enclosing instance of an inner class:
	public void test008() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	3 errors expected" +
			"	                                           ^^\n" +
			"cannot reference this before supertype constructor has been called\n" +
			"----------\n");
	}
	/* The invocation hello() that appears in the pre-construction context of the
	 * Inner constructor is allowed because it refers to the enclosing instance of
	 * Inner (which, in this case, has the type Outer), not the instance of Inner
	 * that is being constructed
	 */
	public void test009() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Outer {
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
							new Outer().new Inner();
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
	public void test010() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	new Inner();  // Error - 'this' is enclosing instance" +
			"	                                           ^^\n" +
			"cannot reference this before supertype constructor has been called\n" +
			"----------\n");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes cannot have the newly created object as the implicit enclosing
	 * instance
	 */
	public void test011() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	new Inner();  // Error - 'this' is enclosing instance" +
			"	                                           ^^\n" +
			"cannot reference this before supertype constructor has been called\n" +
			"----------\n");
	}
	/* in a pre-construction context, class instance creation expressions that declare
	 * anonymous classes are allowed if the class S is declared static, or if it were
	 * an interface instead of a class, then it would have no enclosing instance and
	 * hence there would be no compile-time error.
	 */
	public void test012() {
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
	public void test013() {
		runConformTest(new String[] {
			"X.java",
				"""
					class O {
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
	public void test014() {
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
	public void test015() {
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
					        return; // Error - return not allowed here
					    }
					    public static void main(String[] args) {
							X x = new X(100);
							System.out.println(x.i);
						}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return; // Error - return not allowed here" +
			"	                                           ^^\n" +
			"cannot reference 'return' with exression \n" +
			"----------\n");
	}
	/* It is a compile-time error if a return statement appears in the prologue of a constructor body.
	 */
	public void test016() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	return; // Error - return not allowed here" +
			"	                                           ^^\n" +
			"cannot reference 'return' before supertype constructor has been called\n" +
			"----------\n");
	}
	/* It is a compile-time error if a return statement appears in the prologue of a constructor body.
	 */
	public void test017() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	return; // Error - return not allowed here" +
			"	                                           ^^\n" +
			"cannot reference 'return' before supertype constructor has been called\n" +
			"----------\n");
	}
	/* Throwing an exception in a prologue of a constructor body is permitted.
	 */
	public void test018() {
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
	public void test019() {
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
	public void test020() {
		runConformTest(new String[] {
			"X.java",
				"""
					class B {
						B(Object o) {}
					 }
					class A<T> extends B {
					    A(Z<?> z) {
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
	/* Unlike in a static context, code in a pre-construction context may refer to the type
	 * of the instance under construction, as long as it does not access the instance itself:
	 */
	public void test021() {
		runNegativeTest(new String[] {
			"X.java",
				"""
					class B {
						B(Object o) {}
					 }
					class A<T> extends B {
				    	A() {
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
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	super(this); // Error - refers to 'this'" +
			"	                            ^^\n" +
			"cannot reference this before supertype constructor has been called\n" +
			"----------\n");
		}
}
