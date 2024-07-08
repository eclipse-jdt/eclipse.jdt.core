/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SuperAfterStatementsTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 23");
	private static final String[] VMARGS = new String[] {"--enable-preview"};
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test007b" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return SuperAfterStatementsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}
	public SuperAfterStatementsTest(String testName) {
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
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
	class Runner extends AbstractRegressionTest.Runner {
		public Runner(boolean reportPreview) {
			this();
			this.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		}
		public Runner() {
			super();
			this.vmArguments = VMARGS;
			this.javacTestOptions = JAVAC_OPTIONS;
			this.customOptions = getCompilerOptions();
			this.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
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
			"----------\n" +
			"1. WARNING in X.java (at line 12)\n" +
			"	super(value);\n" +
			"	^^^^^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
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
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	this.i++;                   // Error\n" +
				"	^^^^\n" +
				"Cannot use this in an early construction context\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	this.hashCode();            // Error\n" +
				"	^^^^\n" +
				"Cannot use this in an early construction context\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	System.out.print(this);     // Error\n" +
				"	                 ^^^^\n" +
				"Cannot use this in an early construction context\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 7)\n" +
				"	super();\n" +
				"	^^^^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	super.i++;                  // Error\n" +
			"	^^^^^\n" +
			"Cannot use super in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n"
		);
	}
	public void test007b() {
		// not a problem in outer early construction context
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					class D {
						int i;
					}
					class X {
						X() {
							class E extends D {
								E() {
									super.i++;
								}
							}
							System.out.print(new E().i);
							super();
						}
						public static void main(String... args) {
							new X();
						}
					}
				"""
			};
		runner.expectedOutputString = "1";
		runner.runConformTest();
	}
	public void test007c() {
		// but no access to outer this from local class
		Runner runner = new Runner(false);
		runner.testFiles = new String[] {
				"X.java",
				"""
					class X {
						X() {
							class E {
							    void m() {
							        System.out.print(X.this);
							    }
							}
							super();
							new E();
						}
					}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in X.java (at line 5)
					System.out.print(X.this);
					                 ^^^^^^
				Cannot use X.this in an early construction context
				----------
				""";
		runner.runNegativeTest();
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	i++;                        // Error\n" +
			"	^\n" +
			"Cannot use i in an early construction context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	hashCode();                 // Error\n" +
			"	^^^^^^^^^^\n" +
			"Cannot use hashCode() in an early construction context\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 6)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
	}
	//an expression involving this does not refer to the current instance but,
	// rather, to the enclosing instance of an inner class:
	public void test009_NOK() {
		runNegativeTest(new String[] {
			"B.java",
				"""
					class B {
					    class C {
					        int c;
					        C() {
					            C.this.c++;             // Error - same instance
					            super();
					        }
					    }
					}
				"""
			},
			"----------\n" +
			"1. ERROR in B.java (at line 5)\n" +
			"	C.this.c++;             // Error - same instance\n" +
			"	^^^^^^\n" +
			"Cannot use C.this in an early construction context\n" +
			"----------\n" +
			"2. WARNING in B.java (at line 6)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
	}
	public void test009_OK() {
		runConformTest(new String[] {
			"B.java",
				"""
					class B {
					    int b;
					    class C {
					        C() {
					            B.this.b++;             // Allowed - enclosing instance
					            super();
					        }
					    }
					    public static void main(String... args) {
					    	B b = new B();
					    	C c = b.new C();
					    	System.out.print(b.b);
					    }
					}
				"""
			},
			"1");
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	new Inner(); // Error - \'this\' is enclosing instance\n" +
			"	^^^^^^^^^^^\n" +
			"Cannot use new Inner() in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	var tmp = new S() { };      // Error\n" +
			"	          ^^^^^^^^^^^\n" +
			"Cannot use new S() {\n" +
			"  x() {\n" +
			"    super();\n" +
			"  }\n" +
			"} in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return 0; // Error - return not allowed here\n" +
			"	^^^^^^^^^\n" +
			"Void methods cannot return a value\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	return; // Error - return not allowed here\n" +
			"	^^^^^^^\n" +
			"return ; statement not allowed in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 10)\n" +
			"	super(i);\n" +
			"	^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	return; // Error - return not allowed here\n" +
			"	^^^^^^^\n" +
			"return ; statement not allowed in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	this(i, 0);\n" +
			"	^^^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	int j = a.i;\n" +
			"	        ^^^\n" +
			"Cannot use a.i in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 14)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	super(this); // Error - refers to \'this\'\n" +
			"	      ^^^^\n" +
			"Cannot refer to \'this\' nor \'super\' while explicitly invoking a constructor\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	I tos = super::toString;\n" +
			"	        ^^^^^\n" +
			"Cannot use super in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	this(i, 0);\n" +
			"	^^^^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	int j = a.getI();\n" +
			"	        ^\n" +
			"Cannot use a in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 15)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	int j = J.super.getI();\n" +
			"	        ^^^^^^^\n" +
			"Cannot use J.super in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	int j = J.super.getI();\n" +
			"	        ^^^^^^^\n" +
			"Cannot use J.super in an early construction context\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 9)\n" +
			"	this(j);\n" +
			"	^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n");
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
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	this(0);\n" +
		"	^^^^^^^^\n" +
		"Constructor call must be the first statement in a constructor\n" +
		"----------\n"
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
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	public X(int i){}\n" +
		"	       ^^^^^^^^\n" +
		"Implicit super constructor Y() is undefined. Must explicitly invoke another constructor\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"The constructor Y() is undefined\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	this(0);\n" +
		"	^^^^^^^^\n" +
		"Constructor call must be the first statement in a constructor\n" +
		"----------\n"
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
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"The constructor Y() is undefined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	this(0);\n" +
		"	^^^^^^^^\n" +
		"Constructor call must be the first statement in a constructor\n" +
		"----------\n"
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
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	this();\n" +
		"	^^^^^^^\n" +
		"Constructor call must be the first statement in a constructor\n" +
		"----------\n"
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
	public void test040() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(new String[] {
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
						}
					"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	super(value);\n" +
			"	^^^^^^^^^^^^^\n" +
			"Flexible Constructor Bodies is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testGH2467() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runner.testFiles = new String[] {
				"Test3.java",
				"""
				class Super {}
				public class Test3 extends Super {
					Test3(Test3 other) {
						other.foo(); // bogus error:
						foo(); // error is correct
						super();
					}
					void foo() {}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test3.java (at line 5)
					foo(); // error is correct
					^^^^^
				Cannot use foo() in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}
	public void testOuterConstruction_1() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runner.testFiles = new String[] {
				"Test.java",
				"""
				public class Test {
					Test() {
						class Local {
							Local() {
								foo(this);
							}
						};
						super();
						new Local();
					}
					void foo(Object r) { System.out.print(r.getClass().getName()); }
					public static void main(String... args) {
						new Test();
					}
				}
				"""
			};
		runner.expectedCompilerLog = """
				----------
				1. ERROR in Test.java (at line 5)
					foo(this);
					^^^^^^^^^
				Cannot use foo(this) in an early construction context
				----------
				""";
		runner.runNegativeTest();
	}

	public void testOuterConstruction_2() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		runner.testFiles = new String[] {
				"Test.java",
				"""
				public class Test {
					static class Inner {
						Inner() {
							foo(this);
						}
					};
					Test() {
						new Inner();
						super();
					}
					static void foo(Object r) { System.out.print(r.getClass().getName()); }
					public static void main(String... args) {
						new Test();
					}
				}
				"""
			};
		runner.expectedOutputString = "Test$Inner";
		runner.runConformTest();
	}
}
