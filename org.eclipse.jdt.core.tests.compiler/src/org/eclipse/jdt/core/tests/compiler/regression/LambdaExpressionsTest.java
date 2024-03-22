/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
 *								Bug 424742 - [1.8] NPE in LambdaExpression.isCompatibleWith
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.EclipseHasABug;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.EclipseJustification;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.BootstrapMethodsAttribute;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LambdaExpressionsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test056"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaExpressionsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					  int add(int x, int y);
					}
					public class X {
					  public static void main(String[] args) {
					    I i = (x, y) -> {
					      return x + y;
					    };
					    System.out.println(i.add(1234, 5678));
					  }
					}
					""",
			},
			"6912"
			);
}
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface Greetings {
					  void greet(String head, String tail);
					}
					public class X {
					  public static void main(String[] args) {
					    Greetings g = (x, y) -> {
					      System.out.println(x + y);
					    };
					    g.greet("Hello, ", "World!");
					  }
					}
					""",
			},
			"Hello, World!"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178,  [1.8][compiler] Some functional interfaces are wrongly rejected
public void test003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					  void foo(int x, int y);
					}
					public class X {
					  public static void main(String[] args) {
					    BinaryOperator<String> binOp = (x,y) -> { return x+y; };
					    System.out.println("SUCCESS");
					    // System.out.println(binOp.apply("SUCC", "ESS")); // when lambdas run
					  }
					}
					""",
				"BiFunction.java",
				"""
					@FunctionalInterface
					public interface BiFunction<T, U, R> {
					    R apply(T t, U u);
					}""",
				"BinaryOperator.java",
				"""
					@FunctionalInterface
					public interface BinaryOperator<T> extends BiFunction<T,T,T> {
					}"""
			},
			"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178,  [1.8][compiler] Some functional interfaces are wrongly rejected
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {
					  void foo(int x, int y);
					}
					public class X {
					  public static void main(String[] args) {
					    BinaryOperator binOp = (x,y) -> { return x+y; };
					    System.out.println("SUCCESS");
					    // System.out.println(binOp.apply("SUCC", "ESS")); // when lambdas run
					  }
					}
					""",
				"BiFunction.java",
				"""
					@FunctionalInterface
					public interface BiFunction<T, U, R> {
					    R apply(T t, U u);
					}""",
				"BinaryOperator.java",
				"""
					@FunctionalInterface
					public interface BinaryOperator<T> extends BiFunction<T,T,T> {
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 6)
					BinaryOperator binOp = (x,y) -> { return x+y; };
					^^^^^^^^^^^^^^
				BinaryOperator is a raw type. References to generic type BinaryOperator<T> should be parameterized
				----------
				2. ERROR in X.java (at line 6)
					BinaryOperator binOp = (x,y) -> { return x+y; };
					                                         ^^^
				The operator + is undefined for the argument type(s) java.lang.Object, java.lang.Object
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test005() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String id(String s);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> s;
							System.out.println(i.id("Hello"));
						}
					}
					"""
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test006() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String id(String s);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> s + s;
							System.out.println(i.id("Hello"));
						}
					}
					"""
			},
			"HelloHello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test007() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void print(String s);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> System.out.println(s);
							i.print("Hello");
						}
					}
					"""
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test008() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String print(String s);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> new String(s).toUpperCase();
							System.out.println(i.print("Hello"));
						}
					}
					"""
			},
			"HELLO");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test009() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String print(String s);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> new String(s);
							System.out.println(i.print("Hello"));
						}
					}
					"""
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test010() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int unbox(Integer i);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> s;
							System.out.println(i.unbox(new Integer(1234)));
						}
					}
					"""
			},
			"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test011() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						Integer box(int i);
					}
					public class X {
						public static void main(String[] args) {
							I i = (s) -> s;
							System.out.println(i.box(1234));
						}
					}
					"""
			},
			"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test012() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X subType();
					}
					public class X {
						public static void main(String[] args) {
							I i = () -> new Y();
							System.out.println(i.subType());
						}
					}
					class Y extends X {
					    public String toString() {
					        return "Some Y";
					    }
					}"""
			},
			"Some Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test013() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
					    void foo(String s);
					}
					public class X {
					    public static void main(String [] args) {
					        int in = 12345678;
					        I i = (s) -> {
					            I j = (s2) -> {
					                System.out.println(s + s2 + in); \s
					            };
					            j.foo("Number=");
					        };
					        i.foo("The ");
					    }
					}
					"""
			},
			"The Number=12345678");
}
public void test014() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void nonmain(String[] args) {
						    int var = 2;
						    I x2 = () -> {
						      System.out.println("Argc = " + args.length);
						      for (int i = 0; i < args.length; i++) {
						          System.out.println("Argv[" + i + "] = " + args[i]);
						      }
						    };
						    x2.doit();
						    var=2;
						  }
						  public static void main(String[] args) {
						      nonmain(new String[] {"Hello! ", "World!" });
						  }
						}""" ,
				},
				"""
					Argc = 2
					Argv[0] = Hello!\s
					Argv[1] = World!""");
}
public void test015() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    try {
						      new java.io.File((String) null).getCanonicalPath();
						    } catch (NullPointerException | java.io.IOException ioe) {
						      I x2 = () -> {
						        System.out.println(ioe.getMessage()); // OK: args is not re-assignment since declaration/first assignment
						      };
						      x2.doit();
						    };
						  }
						}
						"""
				},
				"null");
}
public void test016() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void doit();
						}
						public class X {
						  public static void main(String[] args) {
						    java.util.List<String> list = new java.util.ArrayList<>();
						    list.add("SomeString");
						    for (String s : list) {
						      I x2 = () -> {
						        System.out.println(s); // OK: args is not re-assignment since declaration/first assignment
						      };
						      x2.doit();
						    };
						  }
						
						}
						""" ,
				},
				"SomeString");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406181, [1.8][compiler][codegen] IncompatibleClassChangeError when running code with lambda method
public void test017() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  void foo(int x, int y);
						}
						public class X {
						  public static void main(String[] args) {
						    BinaryOperator<String> binOp = (x,y) -> { return x+y; };\s
						    System.out.println(binOp.apply("SUCC", "ESS")); // when lambdas run
						  }
						}
						@FunctionalInterface
						interface BiFunction<T, U, R> {\s
						    R apply(T t, U u);
						}
						@FunctionalInterface\s
						interface BinaryOperator<T> extends BiFunction<T,T,T> {\s
						}
						""",
				},
				"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test018() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X [][][] copy (short x);
						}
						public class X  {
							public static void main(String[] args) {
								I i = X[][][]::new;
						       I j = X[][][]::new;
								X[][][] x = i.copy((short) 631);
								System.out.println(x.length);
						       x = j.copy((short) 136);
								System.out.println(x.length);
							}
						}
						""",
				},
				"631\n" +
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test019() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X [][][] copy (int x);
						}
						public class X  {
							public static void main(String[] args) {
								I i = X[][][]::new;
						       I j = X[][][]::new;
								X[][][] x = i.copy(631);
								System.out.println(x.length);
						       x = j.copy(136);
								System.out.println(x.length);
							}
						}
						""",
				},
				"631\n" +
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test020() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X [][][] copy (Integer x);
						}
						public class X  {
							public static void main(String[] args) {
								I i = X[][][]::new;
						       I j = X[][][]::new;
								X[][][] x = i.copy(631);
								System.out.println(x.length);
						       x = j.copy(136);
								System.out.println(x.length);
							}
						}
						""",
				},
				"631\n" +
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test021() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X [][][] copy (Integer x);
						}
						public class X  {
							public static void main(String[] args) {
								I i = X[][][]::new;
						       I j = X[][][]::new;
								X[][][] x = i.copy(new Integer(631));
								System.out.println(x.length);
						       x = j.copy(new Integer((short)136));
								System.out.println(x.length);
							}
						}
						""",
				},
				"631\n" +
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test022() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    Object copy(int [] ia);
						}
						interface J {
							int [] copy(int [] ia);
						}
						public class X  {
						    public static void main(String [] args) {
						        I i = int[]::<String>clone;
						        int [] x = new int [] { 10, 20, 30 };
						        int [] y = (int []) i.copy(x);
						        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {
						        	System.out.println("Broken");
						        } else {
						        	System.out.println("OK");
						        }
						        J j = int []::clone;
						        y = null;
						        y = j.copy(x);
						        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {
						        	System.out.println("Broken");
						        } else {
						        	System.out.println("OK");
						        }
						    }
						}
						""" ,
				},
				"OK\n" +
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test023() {
this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    Object copy(int [] ia);
						}
						
						public class X  {
						    public static void main(String [] args) {
						        I i = int[]::<String>clone;
						        int [] ia = (int []) i.copy(new int[10]);
						        System.out.println(ia.length);
						    }
						}
						""",
				},
				"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test024() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    YBase copy(Y ia);
						}
						public class X  {
						    public static void main(String [] args) {
						        I i = Y::<String>copy;
						        YBase yb = i.copy(new Y());
						        System.out.println(yb.getClass());
						    }
						}
						class YBase {
							public YBase copy() {
								return this;
							}
						}
						class Y extends YBase {
						}
						""",
				},
				"class Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test025() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    int foo(int [] ia);
						}
						public class X  {
						    public static void main(String [] args) {
						        I i = int[]::<String>hashCode;
						        i.foo(new int[10]);
						    }
						}
						""",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call misdispatched
public void test026() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							Integer foo(int x, int y);
						}
						class Y {
							int foo(int x, int y) {
								System.out.println("Y.foo(" + x + "," + y + ")");
								return foo(x, y);
							}
						}
						public class X extends Y {
							int foo(int x, int y) {
								System.out.println("X.foo(" + x + "," + y + ")");
								return x + y;
							}
							void goo() {
								I i = super::foo;
								System.out.println(i.foo(1234, 4321));
							}
							public static void main(String[] args) {
								new X().goo();
							}
						}
						""",
				},
				"""
					Y.foo(1234,4321)
					X.foo(1234,4321)
					5555""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call misdispatched
public void test027() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							int foo(int x, int y);
						}
						interface J {
							default int foo(int x, int y) {
								System.out.println("I.foo(" + x + "," + y + ")");
								return x + y;
							}
						}
						public class X implements J {
							public static void main(String[] args) {
								I i = new X().f();
								System.out.println(i.foo(1234, 4321));
								i = new X().g();
								try {
									System.out.println(i.foo(1234, 4321));
								} catch (Throwable e) {
									System.out.println(e.getMessage());
								}
							}
							I f() {
								return J.super::foo;
							}
							I g() {
								return new X()::foo;
							}
							public int foo(int x, int y) {
								throw new RuntimeException("Exception");
							}
						}
						""",
				},
				"""
					I.foo(1234,4321)
					5555
					Exception""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406584, Bug 406584 - [1.8][compiler][codegen] ClassFormatError: Invalid method signature
public void test028() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    Object copy();
						}
						public class X  {
						    public static void main(String [] args) {
						    	int [] x = new int[] { 0xdeadbeef, 0xfeedface };
						    	I i = x::<String>clone;
						       System.out.println(Integer.toHexString(((int []) i.copy())[0]));
						       System.out.println(Integer.toHexString(((int []) i.copy())[1]));
						    }
						}
						""",
				},
				"deadbeef\n" +
				"feedface");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial
public void test029() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X.Y.Z makexyz(int val);
						}
						public class X {
							public static void main(String args []) {
								new X().new Y().new Z().new P().goo();
							}
							class Y {
								class Z {
									Z(int val) {
										System.out.println(Integer.toHexString(val));
									}\t
									Z() {
									}
									class P {
										void goo() {
											I i = Z::new;
											i.makexyz(0xdeadbeef);
										}
										I i = Z::new;
										{ i.makexyz(0xfeedface); }
									}
								}
								I i = Z::new;
								{ i.makexyz(0xbeeffeed); }
							}
						}
						""",
				},
				"""
					beeffeed
					feedface
					deadbeef""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial
public void test030() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X.Y makeY();
						}
						public class X {
							public class Y {
						       public String toString() {
						           return "class Y";
						   }
							}
							void foo() {
								I i = Y::new;
								System.out.println(i.makeY());
							}
							public static void main(String[] args) {
								new X().foo();
							}
						}
						""",
				},
				"class Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial
public void test031() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X.Y makeY(int x);
						}
						public class X {
							class Y {
								String state;\s
								Y(int x) {
									state = Integer.toHexString(x);
								}
								public String toString() {
									return state;
								}
							}
							class Z extends Y {
								Z(int x) {
									super(x);
								}
							}
							public static void main(String[] args) {
								new X().f();
							}
							void f() {
								I i = Y::new;
								System.out.println(i.makeY(0xdeadbeef));
							}
						}
						""",
				},
				"deadbeef");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial
public void test032() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X.Y makeY(int x);
						}
						public class X {
							class Y {
								String state;\s
								Y(int x) {
									state = Integer.toHexString(x);
								}
								public String toString() {
									return state;
								}
							}
							class Z extends Y {
								Z(int x) {
									super(x);
								}
							}
							public static void main(String[] args) {
								new X().f();
							}
							void f() {
								I i = Z::new;
								System.out.println(i.makeY(0xdeadbeef));
							}
						}
						""",
				},
				"deadbeef");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial
public void test033() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X.Y.Z makeY(int x);
						}
						public class X {
							class Y {
								Y() {
								}
								class Z {
									String state;
									Z(int x) {
										state = Integer.toHexString(x);
									}
									public String toString() {
										return state;
									}
								}
							}
							class YS extends Y {
								YS() {
								}
								void f() {
									I i = Z::new;
									System.out.println(i.makeY(0xbeefface));
								}
							}
							public static void main(String[] args) {
								new X().new YS().f();
							}
						}
						""",
				},
				"beefface");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods.
public void test034() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    int foo();
						}
						public class X {
						    int f = 1234;
						    void foo() {
						        int x = 4321;
						        I i = () -> x + f;
						        System.out.println(i.foo());
						    }
						    public static void main(String[] args) {
								new X().foo();
							}
						}
						""",
				},
				"5555");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods.
public void test035() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int p, int q);
						}
						public class X {
						   int f;
							void foo(int outerp) {
						       int locouter;
								I i = (int p, int q)  -> {
									class Local {
										void foo() {
						               }
									};
									new Local();
								};
						   }
							public static void main(String[] args) {
								System.out.println("OK");
							}
						}
						""",
				},
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods.
public void test036() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    String foo(String x, String y);
						}
						public class X {
						    String xf = "Lambda ";
						    String x() {
						    	String xl = "code ";
						    	class Y {
									String yf = "generation ";
									String y () {
										String yl = "with ";
										class Z {
											String zf = "instance ";
											String z () {
												String zl = "and ";
												class P {
													String pf = "local ";
													String p () {
														String pl = "capture ";
														I i = (x1, y1) -> {
															return (((I) ((x2, y2) -> {
																return ( ((I) ((x3, y3) -> {
																	return xf + xl + yf + yl + zf + zl + pf + pl + x3 + y3;
																})).foo("works ", "fine ") + x2 + y2);
															})).foo("in ", "the ") + x1 + y1);
														};
														return i.foo("eclipse ", "compiler ");
													}
												}
												return new P().p();
											}
										}
										return new Z().z();
									}
						    	}
						    	return new Y().y();
						    }
						    public static void main(String[] args) {
							System.out.println(new X().x());
						    }
						}
						""",
				},
				"Lambda code generation with instance and local capture works fine in the eclipse compiler");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods.
public void test037() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    String foo(String x, String y);
						}
						public class X {
						    String xf = "Lambda ";
						    String x() {
						    	String xl = "code ";
						    	class Y {
									String yf = "generation ";
									String y () {
										String yl = "with ";
										class Z {
											String zf = "instance ";
											String z () {
												String zl = "and ";
												class P {
													String pf = "local ";
													String p () {
														String pl = "capture ";
														I i = (x1, y1) -> {
															return (((I) ((x2, y2) -> {
																return ( ((I) ((x3, y3) -> {
						                                           String exclaim = "!";
																	return xf + xl + yf + yl + zf + zl + pf + pl + x3 + y3 + x2 + y2 + x1 + y1 + exclaim;
																})).foo("works ", "fine "));
															})).foo("in ", "the "));
														};
														return i.foo("eclipse ", "compiler ");
													}
												}
												return new P().p();
											}
										}
										return new Z().z();
									}
						    	}
						    	return new Y().y();
						    }
						    public static void main(String[] args) {
							System.out.println(new X().x());
						    }
						}
						""",
				},
				"Lambda code generation with instance and local capture works fine in the eclipse compiler !");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation for intersection cast.
public void test038() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						}
						interface J {
						}
						public class X implements I, J {
							public static void main( String [] args) {\s
								f(new X());
							}
							static void f(Object o) {
								X x = (X & I & J) o;
						       System.out.println("OK");
							}
						}
						""",
				},
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation for intersection cast.
public void test039() {
	String errMsg = isJRE11Plus
		? "class X cannot be cast to class I (X and I are in unnamed module of loader"
		: "X cannot be cast to I";
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						}
						interface J {
						}
						public class X implements J {
							public static void main( String [] args) {\s
								f(new X());
							}
							static void f(Object o) {
						       try {
								    X x = (X & I & J) o;
						       } catch (ClassCastException e) {
						           System.out.println(e.getMessage().replaceFirst("(unnamed module of loader).*", "$1"));
						       }
							}
						}
						""",
				},
				errMsg);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test041() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X makeX(int x);
						}
						public class X {
							class Z {
								void f() {
									I i = X::new;
									i.makeX(123456);
								}
							}
							private X(int x) {
								System.out.println(x);
							}
							X() {
							}
							public static void main(String[] args) {
								new X().new Z().f();
							}
						}
						""",
				},
				"123456");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test042() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X makeX(int x);
						}
						public class X {
							class Y extends X {
								class Z {
									void f() {
										I i = X::new;
										i.makeX(123456);
										i = Y::new;
										i.makeX(987654);
									}
								}
								private Y(int y) {
									System.out.println("Y(" + y + ")");
								}
								private Y() {
								\t
								}
							}
							private X(int x) {
								System.out.println("X(" + x + ")");
							}
						
							X() {
							}
							public static void main(String[] args) {
								new X().new Y().new Z().f();
							}
						
						}
						""",
				},
				"X(123456)\n" +
				"Y(987654)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test043() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X makeX(int x);
						}
						public class X {
							class Y extends X {
								class Z extends X {
									void f() {
										I i = X::new;
										i.makeX(123456);
										i = Y::new;
										i.makeX(987654);
						               i = Z::new;
						               i.makeX(456789);
									}
						       	private Z(int z) {
										System.out.println("Z(" + z + ")");
									}
						           Z() {
						           }
						       }
								private Y(int y) {
									System.out.println("Y(" + y + ")");
								}
								private Y() {
								\t
								}
							}
							private X(int x) {
								System.out.println("X(" + x + ")");
							}
						
							X() {
							}
							public static void main(String[] args) {
								new X().new Y().new Z().f();
							}
						
						}
						""",
				},
				"""
					X(123456)
					Y(987654)
					Z(456789)""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test044() {
	this.runConformTest(
			false,
			JavacHasABug.JavacBugFixed_901,
			new String[] {
					"X.java",
					"""
						interface I {
							X makeX(int x);
						}
						public class X {
							void foo() {
								int local;
								class Y extends X {
									class Z extends X {
										void f() {
											I i = X::new;
											i.makeX(123456);
											i = Y::new;
											i.makeX(987654);
											i = Z::new;
											i.makeX(456789);
										}
										private Z(int z) {
											System.out.println("Z(" + z + ")");
										}
										Z() {}
									}
									private Y(int y) {
										System.out.println("Y(" + y + ")");
									}
									private Y() {
									}
								}
								new Y().new Z().f();
							}
							private X(int x) {
								System.out.println("X(" + x + ")");
							}
						
							X() {
							}
							public static void main(String[] args) {
								new X().foo();
							}
						}
						""",
				},
				"""
					X(123456)
					Y(987654)
					Z(456789)""");
}
public void test045() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
							X makeX(int x);
						}
						public class X {
							I i = (x) -> {
								class Y extends X {
									private Y (int y) {
										System.out.println(y);
									}
									Y() {
									}
									void f() {
										I i = X::new;
										i.makeX(123456);
										i = X.Y::new;
										i.makeX(987654);
									}
								}
								return null;\s
							};
							private X(int x) {
								System.out.println(x);
							}
							X() {
							}
							public static void main(String[] args) {
								new X().new Y().f();
							}
						}
						""",
				},
				"""
					----------
					1. WARNING in X.java (at line 6)
						class Y extends X {
						      ^
					The type Y is never used locally
					----------
					2. WARNING in X.java (at line 7)
						private Y (int y) {
						        ^^^^^^^^^
					The constructor Y(int) is never used locally
					----------
					3. WARNING in X.java (at line 10)
						Y() {
						^^^
					The constructor Y() is never used locally
					----------
					4. WARNING in X.java (at line 13)
						I i = X::new;
						  ^
					The local variable i is hiding a field from type X
					----------
					5. ERROR in X.java (at line 15)
						i = X.Y::new;
						    ^^^
					X.Y cannot be resolved to a type
					----------
					6. ERROR in X.java (at line 27)
						new X().new Y().f();
						            ^
					X.Y cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406760, [1.8][compiler][codegen] "VerifyError: Bad type on operand stack" with qualified super method references
public void test046() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							String doit();
						}
						public class X extends B {
							class Y {
								class Z {
									void f() {
									\t
										 I i = X.super::toString; // Verify error
										 System.out.println(i.doit());
										 i = X.this::toString; // This call gets dispatched OK.
										 System.out.println(i.doit());
									}
								}
							}
						\t
							public static void main(String[] args) {
								new X().new Y().new Z().f();\s
							}
						\t
							public String toString() {
								return "X's toString";
							}
						}
						class B {
							public String toString() {
								return "B's toString";
							}
						}
						""",
				},
				"B\'s toString\n" +
				"X\'s toString");
}
public void test047() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int x, int y);
						}
						public class X {
							public static void main(String[] args) {
								long lng = 1234;
								double d = 1234.5678;
								I i = (x, y) -> {
									System.out.println("long = " + lng);
									System.out.println("args length = " + args.length);
									System.out.println("double = " + d);
									System.out.println("x = " + x);
									System.out.println("y = " + y);
								};
								i.foo(9876, 4321);
							}
						}
						""",
				},
				"""
					long = 1234
					args length = 0
					double = 1234.5678
					x = 9876
					y = 4321""");
}
public void test048() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I<T, J> {
							void foo(T x, J y);
						}
						public class X {
							public static void main(String[] args) {
								long lng = 1234;
								double d = 1234.5678;
								I<Object, Object> i = (x, y) -> {
									System.out.println("long = " + lng);
									System.out.println("args length = " + args.length);
									System.out.println("double = " + d);
									System.out.println("x = " + x);
									System.out.println("y = " + y);
								};
								i.foo(9876, 4321);
							\t
								I<String, String> i2 = (x, y) -> {
									System.out.println(x);
									System.out.println(y);
								};
								i2.foo("Hello !",  "World");
							}
						}
						""",
				},
				"""
					long = 1234
					args length = 0
					double = 1234.5678
					x = 9876
					y = 4321
					Hello !
					World""");
}
public void test049() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I<T, J> {
							void foo(X x, T t, J j);
						}
						public class X {
							public static void main(String[] args) {
								I<String, String> i = X::foo;
								i.foo(new X(), "Hello", "World!");
							}
							void foo(String s, String t) {
								System.out.println(s);
								System.out.println(t);
							}
						}
						""",
				},
				"Hello\n" +
				"World!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test050() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int x, int y);
						}
						public class X {
							static private void add(int x, int y) {
								System.out.println(x + y);
							}
							private void multiply(int x, int y) {
								System.out.println(x * y);
							}
							static class Y {
								static private void subtract(int x, int y) {
									System.out.println(x - y);
								}
								private void divide (int x, int y) {
									System.out.println(x / y);
								}
								static void doy() {
									I i = X::add;
									i.foo(1234, 12);
									i = new X()::multiply;
									i.foo(12, 20);
									i = Y::subtract;
									i.foo(123,  13);
									i = new Y()::divide;
									i.foo(99, 9);
								}
							}
							public static void main(String[] args) {
								I i = X::add;
								i.foo(1234, 12);
								i = new X()::multiply;
								i.foo(12, 20);
								i = Y::subtract;
								i.foo(123,  13);
								i = new Y()::divide;
								i.foo(99, 9);
								Y.subtract(10,  7);
								Y.doy();
							}
						}
						""",
				},
				"""
					1246
					240
					110
					11
					3
					1246
					240
					110
					11""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test051() {
	this.runConformTest(
			false /* skipJavac*/,
			JavacHasABug.JavacBugFixed_901,
			new String[] {
					"p2/B.java",
					"""
						package p2;
						import p1.*;
						interface I {
							void foo();
						}
						interface J {
							void foo();
						}
						public class B extends A {
							class Y {
								void g() {
									I i = B::foo;
									i.foo();
									J j = new B()::goo;
									j.foo();
								}
							}
							public static void main(String[] args) {
								new B().new Y().g();
							}
						}
						""",
					"p1/A.java",
					"""
						package p1;
						import p2.*;
						public class A {
							protected static void foo() {
							    System.out.println("A's static foo");
							}
							protected void goo() {
							    System.out.println("A's instance goo");
							}
						}"""
				},
				"A\'s static foo\n" +
				"A\'s instance goo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test052() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
							void foo(int x);
						}
						public class X {
							void foo() {
								int local = 10;
								class Y {
									void foo(int x) {
										System.out.println(local);
									}
									void goo() {
										I i = this::foo;
										i.foo(10);
									}
								}
								new Y().goo();
							}
							public static void main(String[] args) {
								new X().foo();
							}
						}
						"""
				},
				"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406847, [1.8] lambda code compiles but then produces IncompatibleClassChangeError when run
public void test053() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      """
			import java.util.*;
			public class X {
			  public static <E> void printItem(E value, int index) {
			    String output = String.format("%d -> %s", index, value);
			    System.out.println(output);
			  }
			  public static void main(String[] argv) {
			    List<String> list = Arrays.asList("A","B","C");
			    eachWithIndex(list,X::printItem);
			  }
			  interface ItemWithIndexVisitor<E> {
			    public void visit(E item, int index);
			  }
			  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {
			    for (int i = 0; i < list.size(); i++) {
			         visitor.visit(list.get(i), i);
			    }
			  }
			}
			"""
	    },
	    """
			0 -> A
			1 -> B
			2 -> C""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406847, [1.8] lambda code compiles but then produces IncompatibleClassChangeError when run
public void test054() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      """
			import java.util.*;
			public class X {
			  public static <E> void printItem(E value) {}
			  public static void main(String[] argv) {
			    List<String> list = null;
			    eachWithIndex(list, X::printItem);
			  }
			  interface ItemWithIndexVisitor<E> {
			    public void visit(E item);
			  }
			  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {}
			}
			"""
	    },
	    "");
}
public void test055() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      """
			interface I {
				void foo(int i);
			}
			public class X {
				public static void main(String[] args) {
					X x = new X();
					I i = x::foo;
				}
				int foo(int x) {
					return x;
				}
			}
			"""
	    },
	    "");
}
public void test056() {
	  String expected = isJRE15Plus ? "Cannot invoke \"Object.getClass()\" because \"x\" is null" : "null";
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      """
			interface I {
				void foo(int i);
			}
			public class X {
				public static void main(String[] args) {
					X x = null;
					try {
						I i = x::foo;
						i.foo(10);
					} catch (NullPointerException npe) {
						System.out.println(npe.getMessage());
					}
				}
				int foo(int x) {
					return x;
				}
			}
			"""
	    },
	    expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=410114, [1.8] CCE when trying to parse method reference expression with inappropriate type arguments
public void test057() {
	String source = """
		interface I {
		    void foo(Y<String> y);
		}
		public class Y<T> {
		    class Z<K> {
		        Z(Y<String> y) {
		            System.out.println("Y<T>.Z<K>:: new");
		        }
		        public void bar() {
		            I i = Y<String>.Z<Integer>::<String> new;
		            i.foo(new Y<String>());
		            i = Y<String>.Z<Integer>:: new;
		            i.foo(new Y<String>());
		            i = Y.Z:: new;
		            i.foo(new Y<String>());
		        }
		    }
			public void foo() {
				Z<String> z = new Z<String>(null);
				z.bar();
			}
			public static void main(String[] args) {
				Y<String> y = new Y<String>();
				y.foo();
			}
		}
		""";
this.runConformTest(
	new String[]{"Y.java",
				source},
				"""
					Y<T>.Z<K>:: new
					Y<T>.Z<K>:: new
					Y<T>.Z<K>:: new
					Y<T>.Z<K>:: new""");
}
// Bug 411273 - [1.8][compiler] Bogus error about unhandled exceptions for unchecked exceptions thrown by method reference.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=411273
public void test058() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							F1 f = X::foo;
							System.out.println("Hello, World");
						}
					    static int foo (int x) throws NumberFormatException { return 0; }
					}
					interface F1 { int X(int x);}
					"""
			},
			"Hello, World"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420582,  [1.8][compiler] Compiler should allow creation of generic array creation with unbounded wildcard type arguments
public void testGenericArrayCreation() {
		this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X<?, ?, ?>[] makeArray(int i);\n" +
					"}\n" +
					"public class X<T, U, V> {\n" +
					"	public static void main(String [] args) {\n" +
					"		I i = X<?, ?, ?>[]::new; // OK.\n" +
					"		System.out.println(i.makeArray(1024).length);\n" +
					"	}\n" +
					"}\n" +
					""
			},
			"1024"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421536, [1.8][compiler] Verify error with small program when preserved unused variables is off.
public void test421536() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					I foo();
				}
				public class X {
					public static void main(String[] args) {
						try {
							I i = () -> null;
						} catch (NullPointerException npe) {}
				       System.out.println("OK");
					}
				}
				"""
		},
		"OK",
		customOptions);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421536, [1.8][compiler] Verify error with small program when preserved unused variables is off.
public void test421536a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
				   public static void foo() {}
					public static void main(String[] args) {
						try {
							I i = X::foo;
						} catch (NullPointerException npe) {}
				       System.out.println("OK");
					}
				}
				"""
		},
		"OK",
		customOptions);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421607, [1.8][compiler] Verify Error with intersection casts
public void test421607() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					public void foo();
				}
				class C implements I {
					public void foo() {
						System.out.println("You will get here");
					}
				}
				public class X {
					public static void main(String[] args) {
						((C & I) (I) new C()).foo();
					}
				}
				"""
		},
		"You will get here");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421712, [1.8][compiler] java.lang.NoSuchMethodError with lambda expression in interface default method.
public void test421712() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface F {
					void foo();
				}
				interface I {
					default void foo() {
						F f = () -> {
						};
				   System.out.println("Lambda instantiated");
					}
				}
				public class X implements I {
					public static void main(String argv[]) {
						X x = new X();
						x.foo();
					}
				}
				"""
		},
		"Lambda instantiated");
}


//https://bugs.eclipse.org/bugs/show_bug.cgi?id=422515, [1.8][compiler] "Missing code implementation in the compiler" when lambda body accesses array variable
public void test422515() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String[] args) throws InterruptedException {
						        final int[] result = { 0 };
						        Thread t = new Thread(() -> result[0] = 42);
						        t.start();
						        t.join();
						        System.out.println(result[0]);
						    }
						}
						"""
			},
			"42"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422515, [1.8][compiler] "Missing code implementation in the compiler" when lambda body accesses array variable
public void test422515a() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String[] args) throws InterruptedException {
						        final int[] result= { 0 };
						        final int x = args.length + 42;
						        Thread t = new Thread(() -> {
						            result[0]= x;
						        });
						        t.start();
						        t.join();
						        System.out.println(result[0]);
						    }
						}
						"""
			},
			"42"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422800, [1.8][compiler] "Missing code implementation in the compiler" 2
public void test422800() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    private String fField; // must be here; can be used or unused
						    public void foo(Integer arg) {
						        new Thread(() -> {
						            arg.intValue();
						        });
						    }
						    public static void main(String [] args) {
							     System.out.println("OK");
						    }
						}
						"""
			},
			"OK"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421927, [1.8][compiler] Bad diagnostic: Unnecessary cast from I to I for lambdas.
public void test421927() {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {\s
							int foo();
						}
						public class X {
						    static I i  = (I & java.io.Serializable) () -> 42;
						    public static void main(String args[]) {
						        System.out.println(i.foo());
						    }
						}
						"""
			},
			"42");
}

public void testReferenceExpressionInference1() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<E> {
					E foo(E e);
				}
				public class X {
					<T> T print(I<T> i) { return null; }
					void test() {
						String s = print(this::bar);\
					}
					<S> S bar(S s) { return s; }
				}
				"""
		});
}

public void testReferenceExpressionInference2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<E,F> {
					F foo(E e);
				}
				public class X {
					<S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }
					void test() {
						I<X,String> x2s = compose(this::bar, this::i2s);\
					}
					String i2s (Integer i) { return i.toString(); }
					<V,W extends Number> W bar(V v) { return null; }
				}
				"""
		});
}

public void testReferenceExpressionInference3a() {
	runConformTest(
		false /* skipJavac*/,
		JavacTestOptions.Excuse.JavacDoesNotCompileCorrectSource,
		new String[] {
			"X.java",
			"""
				interface I<E,F> {
					F foo(E e);
				}
				public class X {
					<S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }
					void test() {
						I<X,String> x2s = compose(this::bar, this::<String>i2s);\
					}
					<Z> Z i2s (Integer i) { return null; }
					<V,W extends Number> W bar(V v) { return null; }
				}
				"""
		}, null);
}

// previous test demonstrates that a solution exists, just inference doesn't find it.
public void testReferenceExpressionInference3b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<E,F> {
					F foo(E e);
				}
				public class X {
					<S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }
					void test() {
						I<X,String> x2s = compose(this::bar, this::i2s);
					}
					<Z> Z i2s (Integer i) { return null; }
					<V,W extends Number> W bar(V v) { return null; }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				I<X,String> x2s = compose(this::bar, this::i2s);
				                                     ^^^^^^^^^
			The type X does not define i2s(Object) that is applicable here
			----------
			""");
}
public void testLambdaInference1() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      """
			import java.util.*;
			public class X {
			  public static void main(String[] argv) {
			    List<String> list = null;
			    eachWithIndex(list, s -> print(s));
			  }
			  static void print(String s) {}
			  interface ItemWithIndexVisitor<E> {
			    public void visit(E item);
			  }
			  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {}
			}
			"""
	    },
	    "");
}

public void testLambdaInference2() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      """
			import java.util.*;
			class A {}
			class B extends A {
				void bar() {}
			}
			public class X {
			  public static void main(String[] argv) {
			    someWithIndex(getList(), (B b) -> b.bar());
			  }
			  interface ItemWithIndexVisitor<E> {
			    public void visit(E item);
			  }
			  public static <G> void someWithIndex(List<G> list, ItemWithIndexVisitor<G> visitor) {}
			  static <I extends A> List<I> getList() { return null; }
			}
			"""
	    },
	    "");
}

public void testBug419048_1() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import java.util.stream.*;
				public class X {
					public void test() {
						 List<Person> roster = new ArrayList<>();
				       \s
				        Map<String, Person> map =\s
				            roster
				                .stream()
				                .collect(
				                    Collectors.toMap(
				                        p -> p.getLast(),
				                        p -> p
				                    ));
					}
				}
				class Person {
				  public String getLast() { return null; }
				}
				"""
		});
}

public void testBug419048_2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import java.util.stream.*;
				public class X {
					public void test() {
						 List<Person> roster = new ArrayList<>();
				       \s
				        Map<String, Person> map =\s
				            roster
				                .stream()
				                .collect(
				                    Collectors.toMap(
				                        Person::getLast,
				                        Function.identity()
				                    ));
					}
				}
				class Person {
				  public String getLast() { return null; }
				}
				"""
		});
}

public void testBug419048_3() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import java.util.stream.*;
				public class X {
					public void test() {
						 List<Person> roster = new ArrayList<>();
				       \s
				        Map<String, Person> map =\s
				            roster
				                .stream()
				                .collect(
				                    Collectors.toMap(
				                        new Function<Person, String>() {
				                            public String apply(Person p) {\s
				                                return p.getLast();\s
				                            }\s
				                        },
				                        Function.identity()
				                    ));
					}
				}
				class Person {
				  public String getLast() { return null; }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424226,  [1.8] Cannot use static method from an interface in static method reference
public void test424226() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void fun1() {
						FI fi = I::staticMethod;\s
					}
				   public static void main(String [] args) {
				       System.out.println("OK");
				   }
				}
				@FunctionalInterface
				interface FI {
					void foo();\t
				}
				interface I {
					static FI staticMethod() {
						return null;
					}
				}
				"""
		}, "OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423684, [1.8][compiler] IllegalAccessError using functional consumer calling inherited method
public void test423684() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import mypackage.MyPublicClass;
				public class Test {
				    public static void main(String[] args) {
				        doesWork();
				        doesNotWork();
				    }
				    public static void doesNotWork() {
				        MyPublicClass victim = new MyPublicClass();
				        List<String> items = Arrays.asList("first", "second", "third");
				        items.forEach(victim::doSomething); //illegal access error here
				    }
				    public static void doesWork() {
				        MyPublicClass victim = new MyPublicClass();
				        List<String> items = Arrays.asList("first", "second", "third");
				        for (String item : items) {
				            victim.doSomething(item);
				        }
				    }
				}
				""",
			"mypackage/MyPublicClass.java",
			"""
				package mypackage;
				class MyPackagePrivateBaseClass {
				    public void doSomething(String input) {
				        System.out.println(input);
				    }
				}
				public class MyPublicClass extends MyPackagePrivateBaseClass {
				}
				"""
		},
		"""
			first
			second
			third
			first
			second
			third""");
}
public void testBug424742() {
	runNegativeTest(
		new String[] {
			"TestInlineLambdaArray.java",
			"""
				package two.test;
				
				class TestInlineLambdaArray {
					TestInlineLambdaArray h = new TestInlineLambdaArray(x -> x++);	// [9]
					public TestInlineLambda(FI fi) {}
				}
				
				interface FI {
						void foo();
				}
				"""
		},
		"""
			----------
			1. ERROR in TestInlineLambdaArray.java (at line 4)
				TestInlineLambdaArray h = new TestInlineLambdaArray(x -> x++);	// [9]
				                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The constructor TestInlineLambdaArray((<no type> x) -> {}) is undefined
			----------
			2. ERROR in TestInlineLambdaArray.java (at line 5)
				public TestInlineLambda(FI fi) {}
				       ^^^^^^^^^^^^^^^^^^^^^^^
			Return type for the method is missing
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424589, [1.8][compiler] NPE in TypeSystem.getUnannotatedType
public void test424589() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.Collection;
				import java.util.function.Supplier;
				import java.util.Set;
				public class X {
				    public static <T, Y extends Collection<T>>
				        Y foo(Supplier<Y> y) {
				            return null;
				    } \s
				    public static void main(String[] args) {
				        Set<Z> x = foo(Set::new);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				Set<Z> x = foo(Set::new);
				    ^
			Z cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 11)
				Set<Z> x = foo(Set::new);
				               ^^^
			Cannot instantiate the type Set
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425152, [1.8] [compiler] NPE in LambdaExpression.analyzeCode
public void test425152() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				interface Base {\s
					Base get(int x);
				}
				class Main {
				    <T> Base foo(Base b) {\s
				        return null;\s
				     }
				    void bar(Base b) { }
				    void testCase() {
				        bar(foo((int p)->null));
				     }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425512, [1.8][compiler] Arrays should be allowed in intersection casts
public void test425512() throws Exception {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					public class X  {
					    public static void main(String argv[]) {
					    	int [] a = (int [] & Cloneable & Serializable) new int[5];
					       System.out.println(a.length);
					    }
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				int [] a = (int [] & Cloneable & Serializable) new int[5];
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Arrays are not allowed in intersection cast operator
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424628, [1.8][compiler] Multiple method references to inherited method throws LambdaConversionException
public void test424628() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    public static interface Consumer<T> {
					        void accept(T t);
					    }
					   \s
					    public static class Base {
					        public void method () { System.out.println(123); }
					    }
					    public static class Foo extends Base {}
					    public static class Bar extends Base {}
					
					    public static void main (String[] args) {
					        Consumer<Foo> foo = Foo::method;
					        Consumer<Bar> bar = Bar::method;
					        foo.accept(new Foo());
					        bar.accept(new Bar());
					    }
					}
					""",
		},
		"123\n123");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425712, [1.8][compiler] Valid program rejected by the compiler.
public void test425712() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    {
					        bar( () -> (char) 0); // [1]
					    }
					    void bar(FB fb) { }
					    public static void main(String[] args) {
							System.out.println("OK");
						}
					}
					interface FB {
						byte foo();
					}
					""",
		},
		"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426074, [1.8][compiler] 18.5.2 Functional interface parameterization inference problem with intersection types.
public void test426074() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface Functional<T> {
					    void foo(T t);
					}
					interface I { }
					public class X {
						public static void main(String[] args) {
					    	Functional<? extends X> f = (Functional<? extends X> & I) (X c) -> {
					    		System.out.println("main");
					    	};
					    	f.foo(null);
					    }
					}
					""",
		},
		"main");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at runtime due to emission order of casts in intersection casts
public void test426411() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					public class X {
						public static void main(String argv[]) throws Exception {
							((Serializable & AutoCloseable) (() -> {})).close();
						}
					}
					""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at runtime due to emission order of casts in intersection casts
public void test426411b() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface AnotherAutoCloseable extends AutoCloseable {}
					public class X {
						public static void main(String argv[]) throws Exception {
							((Serializable & AnotherAutoCloseable) (() -> {})).close();
						}
					}
					""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at runtime due to emission order of casts in intersection casts
public void test426411c() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					public class X {
						public static void main(String argv[]) throws Exception {
							((AutoCloseable & Serializable) (() -> {})).close();
						}
					}
					""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at runtime due to emission order of casts in intersection casts
public void test426411d() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface AnotherAutoCloseable extends AutoCloseable {}
					public class X {
						public static void main(String argv[]) throws Exception {
							((AnotherAutoCloseable & Serializable) (() -> {})).close();
						}
					}
					""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at runtime due to emission order of casts in intersection casts
public void test426411e() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface I {}
					interface J extends I {
					   static final int xyz = 99;
					}
					public class X {
						public static void main(String argv[]) throws Exception {
							J j = new J() {};
							System.out.println(((I & J) j).xyz);
						}
					}
					""",
		},
		"99");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at runtime due to emission order of casts in intersection casts
public void test426411f() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					interface I {}
					interface J extends I {
					   final int xyz = 99;
					}
					public class X {
						public static void main(String argv[]) throws Exception {
							J j = new J() {};
							System.out.println(((I & J) j).xyz);
						}
					}
					""",
		},
		"99");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426086, [1.8] LambdaConversionException when method reference to an inherited method is invoked from sub class
public void test426086() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface Functional {
					    Long square(Integer a);
					}
					public class X {
					    static class Base {
					    	 private Long square(Integer a) {
					             return Long.valueOf(a*a);
					         }\s
					    }
					    static class SubClass extends Base {
					        public Long callSquare(Integer i) {
					            Functional fi = SubClass.super::square;
					            return fi.square(i);
					        }
					    }
					    public static void main(String argv[]) throws Exception {
					    	System.out.println(new SubClass().callSquare(-3));
					    }
					}
					""",
		},
		"9");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426086, [1.8] LambdaConversionException when method reference to an inherited method is invoked from sub class
public void test426086a() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					interface Functional {
					    Long square(Integer a);
					}
					public class X {
					    static class Base {
					    	 private Long square(Integer a) {
					             return Long.valueOf(a*a);
					         }\s
					    }
					    static class SubClass extends Base {
					        public Long callSquare(Integer i) {
					            Functional fi = super::square;
					            return fi.square(i);
					        }
					    }
					    public static void main(String argv[]) throws Exception {
					    	System.out.println(new SubClass().callSquare(-3));
					    }
					}
					""",
		},
		"9");
}
// Bug 406744 - [1.8][compiler][codegen] LambdaConversionException seen when method reference targets a varargs method.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406744
public void test406744a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(Integer a1, Integer a2, String a3);
					}
					class Y {
						static void m(Number a1, Object... rest) {
							System.out.println(a1);
							print(rest);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X {
						public static void main(String [] args) {
							I i = Y::m;
							i.foo(10, 20, "10, 20");
						}
					}
					""",
			},
			"""
				10
				20
				10, 20"""
			);
}
public void test406744b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int foo(Integer a1, Integer a2, String a3);
					}
					class Y {
						static int m(Number a1, Object... rest) {
							System.out.println(a1);
							print(rest);
							return 1;
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X {
						public static void main(String [] args) {
							I i = Y::m;
							i.foo(10, 20, "10, 20");
						}
					}
					""",
			},
			"""
				10
				20
				10, 20"""
			);
}
public void test406744c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(Integer a1, Integer a2, String a3);
					}
					class Y {
						 Y(Number a1, Object... rest) {
							System.out.println(a1);
							print(rest);
						}
						static void m(Number a1, Object... rest) {
							System.out.println(a1);
							print(rest);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X {
						public static void main(String [] args) {
							I i = Y::new;
							i.foo(10, 20, "10, 20");
						}
					}
					""",
			},
			"""
				10
				20
				10, 20"""
			);
}
public void test406744d() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int a1, Integer a2, String a3);
					}
					interface Y {
						static void m(float a1, Object... rest) {
							System.out.println(a1);
							print(rest);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public interface X extends Y{
						public static void main(String [] args) {
							I i = Y::m;
							i.foo(10, 20, "10, 20");
						}
					}
					""",
			},
			"""
				10.0
				20
				10, 20"""
			);
}
public void test406744e() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						String method(int a);
					}
					class C {
						static String foo(Integer... i) {
							return "foo";
						}
						static String goo(Integer bi, Integer... i) {
							return "bar";
						}
						public void foo() {
							I i;
							i = C::foo;
							System.out.println(i.method(0));
							i = C::goo;
							System.out.println(i.method(0));
						}
					}
					public class X {
						public static void main(String argv[])   {
							new C().foo();
						}
					}
					""",
			},
			"foo\n" +
			"bar"
			);
}
public void test406744f() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(Integer a1, Integer a2, String a3);
					}
					class Y {
						void m(Number a1, Object... rest) {
							System.out.println(a1);
							print(rest);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X extends Y {
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
						public static void main(String [] args) {
							new X().foo();
						}
						void foo() {
							I i = super::m;
							i.foo(10, 20, "10, 20");
						}
					}
					""",
			},
			"""
				10
				20
				10, 20"""
			);
}
public void test406744g() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(Integer a1, Integer a2, String a3);
					}
					class Y {
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X extends Y {
						private void m(Number a1, Object... rest) {
							System.out.println(a1);
							print(rest);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
						public static void main(String [] args) {
							new X().foo();
						}
						void foo() {
							I i = this::m;
							i.foo(10, 20, "10, 20");
						}
					}
					""",
			},
			"""
				10
				20
				10, 20"""
			);
}
public void test406744h() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int [] ia);
					}
					class Y {
						void m(Object... rest) {
							System.out.println("Hello " + rest.length);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X extends Y {
						public static void main(String [] args) {
							new X().foo();
						}
						void foo() {
							I i = super::m;
							i.foo(new int [0]);
						}
					}
					""",
			},
			"Hello 1"
			);
}
public void test406744i() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int [] ia);
					}
					interface I1 {
						void foo(int [] ia);
					}
					class Y {
						void m(Object... rest) {
							System.out.println("Hello " + rest.length);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X extends Y {
						public static void main(String [] args) {
							new X().foo();
						}
						void foo() {
							I i = super::m;
							i.foo(new int [0]);
							I1 i1 = super::m;
							i1.foo(new int [0]);
						}
					}
					""",
			},
			"Hello 1\n" +
			"Hello 1"
			);
}
public void test406744j() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int [] ia);
					}
					class Y {
						void m(Object... rest) {
							I i = this::n;
							i.foo(new int [0]);
						}
						void n(Object... rest) {
							System.out.println("Hello " + rest.length);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					public class X extends Y {
						public static void main(String [] args) {
							new X().foo();
						}
						void foo() {
							I i = super::m;
							i.foo(new int [0]);
						}
					}
					""",
			},
			"Hello 1"
			);
}
public void test406744k() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int [] ia);
					}
					class Y {
						static void m(Object... rest) {
							System.out.println("Hello " + rest.length);
						}
						static void print (Object [] o) {
							for (int i = 0; i < o.length; i++)
								System.out.println(o[i]);
						}
					}
					class Y1 extends Y { }
					public class X {
						public static void main(String [] args) {
							new X().foo();
						}
						void foo() {
							I i = Y::m;
							i.foo(new int [0]);
							i = Y1::m;
							i.foo(new int [0]);
						}
					}
					""",
			},
			"Hello 1\n" +
			"Hello 1"
			);
}
public void test406744l() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(Integer i);
					}
					public class X {
						static void foo(int ... x) {
						}
						public static void main(String[] args) {
							I i = X::foo;
							i.foo(1);
							System.out.println("Hello");
					}
					}
					""",
			},
			"Hello"
			);
}
public void test406744m() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int i);
					}
					public class X {
						static void foo(int ... x) {
						}
						public static void main(String[] args) {
							I i = X::foo;
							i.foo(1);
							System.out.println("Hello");
						}
					}
					""",
			},
			"Hello"
			);
}
public void test406744n() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(Integer i);
					}
					class Base {
						void foo(Object ...objects) {
							System.out.println("Ok");
						}
					}
					public class X extends Base {
						void foo(Object... objects) {
							throw new RuntimeException();
						}
						public static void main(String[] args) {
							new X().goo();
						}
						void goo() {
							I i = super::foo;
							i.foo(10);
						}
					}
					""",
			},
			"Ok"
			);
}
public void test406744o() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					class Base {
						public void foo(int ...is) {
							System.out.println("foo");
						}
					}
					public class X extends Base {
						public static void main( String[] args ) {
							I i = new X()::foo;
							i.foo(10);
						}
					}
					""",
			},
			"foo"
			);
}
public void test406744p() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					public class X {
						private void foo(int ...is) {
							System.out.println("foo");
						}
						public static void main(String[] args ) {
							new X().new Y().foo();
						}
						class Y extends X {
							void foo() {
								I i = new X()::foo;
								i.foo(10);
							}
						}
					}
					""",
			},
			"foo"
			);
}
public void test406744q() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					class Y {
						public static void foo(int ...is) {
							System.out.println("Y.foo");
						}
					}
					public class X {
						public static void foo(int ...is) {
							System.out.println("X.foo");
						}
						public static void main(String[] args) {
							I i = X::foo;
							i.foo(10);
							i = Y::foo;
							i.foo(20);
						}
					}
					""",
			},
			"X.foo\n" +
			"Y.foo"
			);
}
public void test406744r() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int t, int [] ia);
					}
					public class X {
						public static void foo(Integer i, int ...is) {
							System.out.println("Y.foo");
						}
						public static void main(String[] args) {
							I i = X::foo;
							i.foo(10, null);
						}
					}
					""",
			},
			"Y.foo"
			);
}
public void test406744s() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X foo(int x);
					}
					public class X {
						class Y extends X {
							Y(int ... x) {
								System.out.println("Y::Y");
							}
						}
						public static void main(String[] args ) {
							new X().goo();
						}
						void goo() {
							I i = Y::new;
							i.foo(10);
						}
					}
					""",
			},
			"Y::Y"
			);
}
public void test406744t() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X foo(int x);
					}
					public class X<T> {
						class Y extends X {
						    Y(int ... x) {
							    System.out.println("Y::Y");
						    }
						}
						public static void main(String[] args ) {
							System.out.println("Hello");
							new X().goo();
						}
						void goo() {
							I i = Y::new;
							i.foo(10);
						}
					}
					""",
			},
			"Hello\n" +
			"Y::Y"
			);
}
public void test406744u() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X<String> foo(int x);
					}
					public class X<T> { \s
						class Y extends X<String> {
						    Y(int ... x) {
							    System.out.println("Y::Y");\s
						    }
						}
						public static void main(String[] args ) {
							System.out.println("Hello");
							new X<String>().goo(); \s
						}
						void goo() {
							I i = Y::new;
							i.foo(10);\s
						}
					}
					""",
			},
			"Hello\n" +
			"Y::Y"
			);
}
public void test406744v() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X foo();
					}
					public class X {
						private X(int ... is) {
							System.out.println("X::X");
						}
					\t
						public static void main(String[] args) {
							new X().new Y().goo();
						}
						public class Y {
							public void goo() {
								I i = X::new;\s
								i.foo();
							}\s
						}
					}
					""",
			},
			"X::X\n" +
			"X::X"
			);
}
public void test406744w() {
	this.runConformTest(
			new String[] {
				"p2/B.java",
				"""
					package p2;
					import p1.*;
					interface I {
						void foo(int x);
					}
					interface J {
						void foo(int x);
					}
					public class B extends A {
						class Y {
							void g() {
								I i = B::foo;
								i.foo(10);
								J j = new B()::goo;
								j.foo(10);
							}
						}
						public static void main(String[] args) {
							new B().new Y().g();
						}
					}
					""",
				"p1/A.java",
				"""
					package p1;
					import p2.*;
					public class A {
						protected static void foo(int ... is) {
						    System.out.println("A's static foo");
						}
						protected void goo(int ... is) {
						    System.out.println("A's instance goo");
						}
					}
					"""
			},
			"A\'s static foo\n" +
			"A\'s instance goo"
			);
}
public void test406744x() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						void foo(int x);
					}
					public class X {
						class Y {
							void goo() {
								I i = X::goo;
								i.foo(10);
							}
						}
						private static void goo(Integer i) {
							System.out.println(i);
						}
						public static void main(String[] args) {
							 new X().new Y().goo();\s
						}
					}
					"""
			},
			"10"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427483, [Java 8] Variables in lambdas sometimes can't be resolved
public void test427483() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.TreeSet;
					public class X {
						public static void main(String[] args) {
							new TreeSet<>((String qn1, String qn2) -> {
								boolean b = true;
								System.out.println(b); // ok
								if (b) {
								} // Eclipse says: b cannot be resolved or is not a field
								return qn1.compareTo(qn2);
							});
						}
					}
					"""
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427627, [1.8] List.toArray not compiled correctly (NoSuchMethodError) within Lambda
public void test427627() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					public class X {
					  public static void main(String[] args) {
					    Runnable r = () -> {
					      List<SourceKey> keys = new ArrayList<>();
					
					      associate("Test", keys.toArray(new SourceKey[keys.size()]));
					    };
					    r.run();
					  }
					  private static void associate(String o, SourceKey... keys) {
						  System.out.println(o);
						  System.out.println(keys.length);
					  }
					  public class SourceKey {
					    public SourceKey(Object source, Object key) {
					    }
					  }
					}
					"""
			},
			"Test\n0"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427744, [1.8][compiler][regression] Issue with boxing compatibility in poly conditional
public void test427744() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {  \s
					    public static void main(String argv[]) {
					        int i = ((I) (x) -> { return 999; }).foo(true ? 0 : (Comparable) null);
					        System.out.println(i);
					    }
					    interface I {
					        int foo (Comparable arg);\s
					        default int foo (Object arg) {\s
					            return 0;
					        }
					    }
					}
					"""
			},
			"999"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427962, [1.8][compiler] Stream#toArray(String[]::new) not inferred without help
public void test427962() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Function;
					import java.util.function.IntFunction;
					import java.util.stream.Stream;
					import java.util.stream.IntStream;
					public class X {
					  static <A, B> Stream<B> objmap(Function<A, B> p1, A[] p2) {return Stream.of(p2).map(p1);}
					  static <B> Stream<B> intmap(IntFunction<B> p1, int[] p2) {return IntStream.of(p2).mapToObj(p1);}
					  public static void main(String[] args) {
					    Integer[] p12 = {1, 2, 3};
					    int[] p22 = {1, 2, 3};
					    //works
					    String[] a11 = objmap(String::valueOf, p12).<String> toArray(String[]::new);
					    String[] a21 = intmap(String::valueOf, p22).<String> toArray(String[]::new);
					    //does not work
					    String[] a12 = objmap(String::valueOf, p12).toArray(String[]::new);
					    String[] a22 = intmap(String::valueOf, p22).toArray(String[]::new);
					  }
					}
					"""
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428112, [1.8][compiler] ClassCastException in ReferenceExpression.generateCode
public void test428112() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.Locale;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;
					public class X {
						public static void main(String[] args) {
							System.out.println(Locale.lookup(Stream.of( "de", "*-CH" ).map(Locale.LanguageRange::new).collect(Collectors.toList()),\s
					                                   Arrays.asList(Locale.getAvailableLocales())));
						}
					}
					"""
			},
			"de"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428003, [1.8][compiler] Incorrect error on lambda expression when preceded by another explicit lambda expression
public void test428003() { // extracted small test
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					public class X {
					    public static void main(String[] args) {
					        Arrays.sort(args, (String x, String y) -> x.length() - y.length());
					        Arrays.sort(args, (x, y) -> Integer.compare(x.length(), y.length()));
					    }
					}
					"""
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428003, [1.8][compiler] Incorrect error on lambda expression when preceded by another explicit lambda expression
public void test428003a() { // full test case
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					public class X {
					    public static void main(String[] args) {
					        String[] words = {"java", "interface", "lambda", "expression" };
					        Arrays.sort(words, (String word1, String word2) -> {
					                    if (word1.length() < word2.length())
					                        return -1;
					                    else if (word1.length() > word2.length())
					                        return 1;
					                    else
					                        return 0;
					                  });
					        for (String word : words)
					            System.out.println(word);
					        words = new String [] {"java", "interface", "lambda", "expression" };
					        Arrays.sort(words, (word1, word2) -> Integer.compare(word1.length(), word2.length()));
					        for (String word : words)
					            System.out.println(word);
					        words = new String [] {"java", "interface", "lambda", "expression" };
					        Arrays.sort(words, (String word1, String word2) -> Integer.compare(word1.length(), word2.length()));
					        for (String word : words)
					            System.out.println(word);
					      }
					  }
					"""
			},
			"""
				java
				lambda
				interface
				expression
				java
				lambda
				interface
				expression
				java
				lambda
				interface
				expression"""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428261, [1.8][compiler] Incorrect error: No enclosing instance of the type X is accessible in scope
public void test428261() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						X foo(int a);
					}
					public class X {
						public static void main(String[] args) {
							String s = "Blah";
							class Local extends X {
								Local(int a) {
									System.out.println(a);
									System.out.println(s);
								}
							}
							I i = Local::new; // Incorrect error here.
					       i.foo(10);
						}
					}
					"""
			},
			"10\n" +
			"Blah"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428261, [1.8][compiler] Incorrect error: No enclosing instance of the type X is accessible in scope
public void test428261a() {
	this.runConformTest(
			false,
			JavacHasABug.JavacBugFixed_901,
			new String[] {
				"X.java",
				"""
					interface I {
						X foo(int a);
					}
					public class X {
						void goo() {
							class Local extends X {
								Local(int a) {
									System.out.println(a);
								}
							}
							I i = Local::new;
					       i.foo(10);
						}
					   public static void main(String [] args) {
					        new X().goo();
					   }
					}
					"""
			},
			"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428552,  [1.8][compiler][codegen] Serialization does not work for method references
public void test428552() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.*;
					public class X {
						interface Example extends Serializable {
							String convert(Object o);
						}
						public static void main(String[] args) throws IOException {
							Example e=Object::toString;
							try(ObjectOutputStream os=new ObjectOutputStream(new ByteArrayOutputStream())) {
								os.writeObject(e);
							}
					       System.out.println("No exception !");
						}
					}
					"""
			},
			"No exception !",
			null,
			true,
			new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428642, [1.8][compiler] java.lang.IllegalArgumentException: Invalid lambda deserialization exception
public void test428642() {
	this.runConformTest(
			new String[] {
				"QuickSerializedLambdaTest.java",
				"""
					import java.io.*;
					import java.util.function.IntConsumer;
					public class QuickSerializedLambdaTest {
						interface X extends IntConsumer,Serializable{}
						public static void main(String[] args) throws IOException, ClassNotFoundException {
							X x1 = i -> System.out.println(i);// lambda expression
							X x2 = System::exit; // method reference
							ByteArrayOutputStream debug=new ByteArrayOutputStream();
							try(ObjectOutputStream oo=new ObjectOutputStream(debug))
							{
								oo.writeObject(x1);
								oo.writeObject(x2);
							}
							try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))
							{
								X x=(X)oi.readObject();
								x.accept(42);// shall print 42
								x=(X)oi.readObject();
								x.accept(0);// shall exit
							}
							throw new AssertionError("should not reach this point");
						}
					}
					"""
			},
			"42",
			null,
			true,
			new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112,  [1.8][compiler] Exception when compiling Serializable array constructor reference
public void test429112() {
	this.runConformTest(
			new String[] {
				"ArrayConstructorReference.java",
				"""
					import java.io.Serializable;
					import java.util.function.IntFunction;
					public class ArrayConstructorReference {
					  interface IF extends IntFunction<Object>, Serializable {}
					  public static void main(String[] args) {
					    IF factory=String[][][]::new;
					    Object o = factory.apply(10);
					    System.out.println(o.getClass());
					    String [][][] sa = (String [][][]) o;
					    System.out.println(sa.length);
					  }
					}
					"""
			},
			"class [[[Ljava.lang.String;\n" +
			"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112,  [1.8][compiler] Exception when compiling Serializable array constructor reference
public void test429112a() {
	this.runConformTest(
			new String[] {
				"ArrayConstructorReference.java",
				"""
					import java.io.Serializable;
					import java.util.function.IntFunction;
					public class ArrayConstructorReference {
					  interface IF extends IntFunction<Object>, Serializable {}
					  public static void main(String[] args) {
					    IF factory=java.util.function.IntFunction[][][]::new;
					    Object o = factory.apply(10);
					    System.out.println(o.getClass());
					    java.util.function.IntFunction[][][] sa = (java.util.function.IntFunction[][][]) o;
					    System.out.println(sa.length);
					  }
					}
					"""
			},
			"class [[[Ljava.util.function.IntFunction;\n" +
			"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112,  [1.8][compiler] Exception when compiling Serializable array constructor reference
public void test429112b() {
	this.runConformTest(
			new String[] {
				"ArrayConstructorReference.java",
				"""
					import java.io.Serializable;
					import java.util.function.IntFunction;
					public class ArrayConstructorReference {
					  interface IF extends IntFunction<Object>, Serializable {}
					  public static void main(String[] args) {
					    IF factory=java.util.function.IntFunction[]::new;
					    Object o = factory.apply(10);
					    System.out.println(o.getClass());
					    java.util.function.IntFunction[] sa = (java.util.function.IntFunction[]) o;
					    System.out.println(sa.length);
					  }
					}
					"""
			},
			"class [Ljava.util.function.IntFunction;\n" +
			"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112,  [1.8][compiler] Exception when compiling Serializable array constructor reference
public void test429112c() {
	this.runConformTest(
			new String[] {
				"ArrayConstructorReference.java",
				"""
					import java.io.Serializable;
					import java.util.function.IntFunction;
					public class ArrayConstructorReference {
					  interface IF extends IntFunction<Object>, Serializable {}
					  public static void main(String[] args) {
					    IF factory=String[]::new;
					    Object o = factory.apply(10);
					    System.out.println(o.getClass());
					    String [] sa = (String []) o;
					    System.out.println(sa.length);
					  }
					}
					"""
			},
			"class [Ljava.lang.String;\n" +
			"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.List;
					import java.util.function.Function;
					public class X {
					    public static void main (String[] args) {
					        Function<List<String>, String> func = List::toString;
					        System.out.println(func.apply(Arrays.asList("a", "b")));
					    }
					}
					"""
			},
			"[a, b]",
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				interface I {
				    List<String> getList();
				}
				public class X {
				    public static void main (String[] args) {
				        I i = ArrayList::new;
				        System.out.println(i.getList());
				    }
				}
				"""
		},
		"[]",
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, - [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.ArrayList;
				import java.util.function.Function;
				interface I {
				    ArrayList<String> getList();
				}
				public class X {
				    public static void main (String[] args) {
				        I i = ArrayList::new;
				        System.out.println(i.getList());
				    }
				}
				"""
		},
		"[]",
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428857, [1.8] Method reference to instance method of generic class incorrectly gives raw type warning
public void test428857c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.List;
					import java.util.function.Function;
					import java.util.ArrayList;
					public class X {
					    public static void main (String[] args) {
					        Function<ArrayList<String>, String> func = List::toString;
					        System.out.println(func.apply(new ArrayList<>(Arrays.asList("a", "b"))));
					    }
					}
					"""
			},
			"[a, b]",
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429763,  [1.8][compiler] Incompatible type specified for lambda expression's parameter
public void test429763() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Function;
					public class X {
						public static void main(String[] args) {
					       try {
							    final int i = new Test<Integer>().test((Byte b) -> (int) b);
					       } catch (NullPointerException e) {
					            System.out.println("NPE");
					       }
						}
						static class Test<R> {
							<T> R test(Function<T,R> f) {
								return null;
							}
						}
					}
					"""
			},
			"NPE");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429763,  [1.8][compiler] Incompatible type specified for lambda expression's parameter
public void test429763a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Function;
					public class X {
						public static void main(String[] args) {
							// does not compile
							new Test<Integer>().test((Byte b) -> (int) b);
						}
						static class Test<R> {
							<T> void test(Function<T,R> f) {
							}
						}
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429759, [1.8][compiler] Lambda expression's signature matching error
public void test429759() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Function;
					import java.util.function.Supplier;
					public class X {
						public static void main(String[] args) {
							final int i = new Test<Integer>().test("", (String s) -> 1);
						}
						static class Test<R> {
							<T> R test(T t, Supplier<R> s) {
								return s.get();
							}
							<T> R test(T t, Function<T, R> f) {
								return f.apply(t);
							}
						}
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429948, Unhandled event loop exception is thrown when a lambda expression is nested
public void test429948() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Supplier;
					public class X {
						public static void main(String[] args) {
							execute(() -> {
								executeInner(() -> {
								});
								return null;
							});
							System.out.println("done");
						}
						static <R> R execute(Supplier<R> supplier) {
							return null;
						}
						static void executeInner(Runnable callback) {
						}
					}
					"""
			},
			"done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429969, [1.8][compiler] Possible RuntimeException in Lambda tangles ECJ
public void test429969() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.Optional;
					public class X {
					    public static void main(String[] args) {
					        final String s = Arrays.asList("done").stream().reduce(null, (s1,s2) -> {
					                // THE FOLLOWING LINE CAUSES THE PROBLEM
					                require(s1 != null || s2 != null, "both strings are null");
					                    return (s1 != null) ? s1 : s2;
					            }, (s1,s2) -> (s1 != null) ? s1 : s2);
					\t
					        System.out.println(s);
					    }
					    static void require(boolean condition, String msg) throws RuntimeException {
					        if (!condition) {
					            throw new RuntimeException(msg);
					        }
					    }
					}
					"""
			},
			"done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430015, [1.8] NPE trying to disassemble classfile with lambda method and MethodParameters
public void test430015() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.util.Arrays;
					import java.util.function.IntConsumer;
					public class X {
					    IntConsumer xx(int a) {
					        return i -> { };
					    }
					    public static void main(String[] args) {
					        Method[] methods = X.class.getDeclaredMethods();
					        for (Method method : methods) {
					        	if (method.getName().contains("lambda")) {
					         		Parameter[] parameters = method.getParameters();
					        		System.out.println(Arrays.asList(parameters));
					        	}
					        }
					    }
					}
					"""
			},
			"[int arg0]");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430040, [1.8] [compiler] Type Type mismatch: cannot convert from Junk13.ExpressionHelper<Object> to Junk13.ExpressionHelper<Object>
public void test430040() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        System.out.println("OK");
					    }
					    class Observable<T> {}
					    class ObservableValue<T> {}
					    interface InvalidationListener {
					        public void invalidated(Observable observable);
					    }
					    public interface ChangeListener<T> {
					        void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);
					    }
					    static class ExpressionHelper<T> {}
					    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, InvalidationListener listener) {
					        return helper;
					    }
					    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, ChangeListener<? super T> listener) {
					        return helper;
					    }
					    private ExpressionHelper<Object> helper;
					    public void junk() {
					        helper = (ExpressionHelper<Object>) addListener(helper, null, (Observable o) -> {throw new RuntimeException();});
					        helper = addListener(helper, null, (Observable o) -> {throw new RuntimeException();});
					    }
					}
					"""
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430043, [1.8][compiler] Cannot infer type arguments for Junk14<>
public void test430043() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.File;
					import java.io.IOException;
					import java.io.StringReader;
					import java.nio.file.Files;
					import java.text.MessageFormat;
					import java.util.*;
					import java.util.function.Function;
					import java.util.jar.Attributes;
					import java.util.jar.JarFile;
					import java.util.jar.Manifest;
					public class X<T>  {
					    public X(String name, String description, String id, Class<T> valueType, String[] fallbackIDs, Function<Map<String, ? super Object>, T> defaultValueFunction, boolean requiresUserSetting, Function<String, T> stringConverter) {
					    }
					    public static final X<String> NAME  =
					            new X<>(
					                    null,
					                    null,
					                    null,
					                    String.class,
					                    null,
					                    params -> {throw new IllegalArgumentException("junk14");},
					                    true,
					                    s -> s
					            );
					     public static void main(String [] args) {
					         System.out.println("OK");
					     }
					}
					"""
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Consumer;
					public class X {
					    interface StringConsumer extends Consumer<String> {
					        void accept(String t);
					    }
					    public static void main(String... x) {
					      StringConsumer c = s->System.out.println("m("+s+')');
					      c.accept("direct call");
					      Consumer<String> c4b=c;
					      c4b.accept("bridge method");
					    }
					}
					"""
			},
			"m(direct call)\n" +
			"m(bridge method)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035a() { // test reference expressions requiring bridges.
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Consumer;
					public class X {
					    interface StringConsumer extends Consumer<String> {
					        void accept(String t);
					    }
					    static void m(String s) { System.out.println("m("+s+")"); }\s
					    public static void main(String... x) {
					      StringConsumer c = X::m;
					      c.accept("direct call");
					      Consumer<String> c4b=c;
					      c4b.accept("bridge method");
					    }
					}
					"""
			},
			"m(direct call)\n" +
			"m(bridge method)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I<T> {
						void foo(T t);
					}
					interface J<T> {
						void foo(T t);
					}
					interface K extends I<String>, J<String> {
					}
					public class X {
					    public static void main(String... x) {
					      K k = s -> System.out.println("m("+s+')');
					      k.foo("direct call");
					      J<String> j = k;
					      j.foo("bridge method");
					      I<String> i = k;
					      i.foo("bridge method");
					    }
					}
					"""
			},
			"""
				m(direct call)
				m(bridge method)
				m(bridge method)""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I<T> {
						void foo(String t, T u);
					}
					interface J<T> {
						void foo(T t, String u);
					}
					interface K extends I<String>, J<String> {
						void foo(String t, String u);
					}
					public class X {
					    public static void main(String... x) {
					      K k = (s, u) -> System.out.println("m("+ s + u + ')');
					      k.foo("direct", " call");
					      J<String> j = k;
					      j.foo("bridge",  " method(j)");
					      I<String> i = k;
					      i.foo("bridge",  " method(i)");
					    }
					}
					"""
			},
			"""
				m(direct call)
				m(bridge method(j))
				m(bridge method(i))""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035d() { // 8b131 complains of ambiguity.
	this.runConformTest(
			false,
			EclipseHasABug.EclipseBug510528,
			new String[] {
				"X.java",
				"""
					interface I<T> {
						void foo(String t, T u);
					}
					interface J<T> {
						void foo(T t, String u);
					}
					interface K extends I<String>, J<String> {
					}
					public class X {
					    public static void main(String... x) {
					      K k = (s, u) -> System.out.println("m("+ s + u + ')');
					      k.foo("direct", " call");
					      J<String> j = k;
					      j.foo("bridge",  " method(j)");
					      I<String> i = k;
					      i.foo("bridge",  " method(i)");
					    }
					}
					"""
			},
			"""
				m(direct call)
				m(bridge method(j))
				m(bridge method(i))""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035e() { // 8b131 complains of ambiguity in call.
	this.runConformTest(
			false,
			EclipseHasABug.EclipseBug510528,
			new String[] {
				"X.java",
				"""
					interface I<T> {
						Object foo(String t, T u);
					}
					interface J<T> {
						String foo(T t, String u);
					}
					interface K extends I<String>, J<String> {
					}
					public class X {
					    public static void main(String... x) {
					      K k = (s, u) -> s + u;
					      System.out.println(k.foo("direct", " call"));
					      J<String> j = k;
					      System.out.println(j.foo("bridge",  " method(j)"));
					      I<String> i = k;
					      System.out.println(i.foo("bridge",  " method(i)"));
					    }
					}
					"""
			},
			"""
				direct call
				bridge method(j)
				bridge method(i)""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035f() { // ensure co-variant return emits a bridge request.
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I<T> {
						Object foo(String t, String u);
					}
					interface J<T> {
						String foo(String t, String u);
					}
					interface K extends I<String>, J<String> {
					}
					public class X {
					    public static void main(String... x) {
					      K k = (s, u) -> s + u;
					      System.out.println(k.foo("direct", " call"));
					      J<String> j = k;
					      System.out.println(j.foo("bridge",  " method(j)"));
					      I<String> i = k;
					      System.out.println(i.foo("bridge",  " method(i)"));
					    }
					}
					"""
			},
			"""
				direct call
				bridge method(j)
				bridge method(i)""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430241,  [1.8][compiler] Raw return type results in incorrect covariant return bridge request to LambdaMetaFactory
public void test430241() { // ensure raw return type variant does not emit a bridge request.
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface K extends I, J {
					}
					interface I {
					    Comparable<Integer> foo();
					}
					interface J {
					    Comparable foo();
					}
					public class X {
						public static void main(String[] args) {
							K k = () -> null;
							System.out.println(k.foo());
						}
					}
					"""
			},
			"null");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface incorrectly rejected as not being.
public void test430310() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface Func1<T1, R> {
					        R apply(T1 v1);
					        void other();
					}
					@FunctionalInterface // spurious error: F1<T, R> is not a functional interface
					public interface X<T1, R> extends Func1<T1, R> {
						default void other() {}
					   public static void main(String [] args) {
					       System.out.println("OK");
					   }
					}
					"""
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface incorrectly rejected as not being.
public void test430310a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					@FunctionalInterface
					public interface X<T1, T2, R> {
					    R apply(T1 v1, T2 v2);
					    default void other() {}
					    public static void main(String[] args) {
					        System.out.println("OK");
					    }
					}
					"""
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface incorrectly rejected as not being.
public void test430310b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I1 {
						int foo(String s);
					}
					@FunctionalInterface
					interface A1 extends I1 {
						@Override
						default int foo(String s) {
							return -1;
						}
						int foo(java.io.Serializable s);
					}
					public class X {
						public static void main(String[] args) {
							System.out.println("OK");
						}
					}
					"""
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface incorrectly rejected as not being.
public void test430310c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					interface I2 {
						int foo(String s);
					}
					@FunctionalInterface
					interface A2 extends I2 {
						@Override
						default int foo(String s) {
							return -1;
						}
						int bar(java.io.Serializable s);
					}
					public class X {
						public static void main(String[] args) {
							System.out.println("OK");
						}
					}
					"""
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432619, [1.8] Bogus error from method reference: "should be accessed in a static way"
public void test432619() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.BiConsumer;
				public interface X<E extends Exception> {
					static void foo() {
					    BiConsumer<double[][], Double> biConsumer2 = Re2::accumulate;
					}
					static class Re2 {
					    static void accumulate(double[][] container, Double value) {}
					}
				   public static void main(String [] args) {
				       System.out.println("OK");
				   }
				}
				"""
		},
		"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432619, [1.8] Bogus error from method reference: "should be accessed in a static way"
public void test432619a() throws Exception {
	this.runConformTest(
		new String[] {
			"StreamInterface.java",
			"""
				import java.util.Map;
				import java.util.stream.Collector;
				public interface StreamInterface<E extends Exception> {
					static class DoubleCo {
						private static class Re2 {
							static <K, E extends Exception> Map<K, double[]> internalToMapToList2() {
								Collector<Double, double[][], double[][]> toContainer1 = Collector.of(
								//The method supply() from the type StreamInterface.DoubleCo.Re2 should be accessed in a static way
								  StreamInterface.DoubleCo.Re2::supply,
								  //The method accumulate(double[][], Double) from the type StreamInterface.DoubleCo.Re2 should be accessed in a static way
								  StreamInterface.DoubleCo.Re2::accumulate,
								  //The method combine(double[][], double[][]) from the type StreamInterface.DoubleCo.Re2 should be accessed in a static way
								  StreamInterface.DoubleCo.Re2::combine);
								Collector<Double, double[][], double[][]> toContainer2 =
								//All 3 from above:
								  Collector.of(DoubleCo.Re2::supply, DoubleCo.Re2::accumulate, DoubleCo.Re2::combine);
								return null;
							}
							private static double[][] supply() {
								return new double[64][];
							}
							private static void accumulate(double[][] container, Double value) {}
							private static double[][] combine(double[][] container, double[][] containerRight) {
								return new double[container.length + containerRight.length][];
							}
						}
					}
				     public static void main(String [] args) {
				         System.out.println("OK");
				     }
				}
				"""
		},
		"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432682, [1.8][compiler] Type mismatch error with lambda expression
public void test432682() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Optional;
				public class X {
					public static void main(String[] args) {
						Optional<String> userName = Optional.of("sa");
						Optional<String> password = Optional.of("sa");
						boolean isValid = userName.flatMap(u -> {
							return password.map(p -> {
								return u.equals("sa") && p.equals("sa");
							});
						}).orElse(false);
						System.out.println(isValid);
					}
				}
				"""
		},
		"true");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432520, compiler "duplicate method" bug with lamdas and generic interfaces
public void test432520() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void withProvider(Provider<String> provider) { }
					public static void main(String [] args) {
						withProvider(() -> "user");
					}
				}
				interface ParentProvider<T> {
					T get();
				}
				// if you remove the extends clause everything works fine
				interface Provider<T> extends ParentProvider<T> {
					T get();
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432625, [1.8] VerifyError with lambdas and wildcards
public void test432625() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.stream.Stream;
				public class X {
				    public static void main(String[] args) {
				        Stream<?> stream = Stream.of("A");
				        stream.map(x -> (String) x);
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430766, [1.8] Internal compiler error.
public void test430766() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Comparator;
					import java.util.List;
					public class X {
						static class Person {
							private String email;
							public Person(String email) {
								this.email = email;
							}
							public String getEmail() {
								return email;\
							}
						}
						public static void main(String[] args) {
							List<Person> persons = new ArrayList<Person>();
							persons.add(new Person("joe.smith@gmail.com"));
							persons.add(new Person("alice.smith@gmail.com"));
							persons.sort(Comparator.comparing(Comparator.nullsLast(Person::getEmail)));
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 17)
					persons.sort(Comparator.comparing(Comparator.nullsLast(Person::getEmail)));
					                                             ^^^^^^^^^
				The method nullsLast(Comparator<? super T>) in the type Comparator is not applicable for the arguments (Person::getEmail)
				----------
				2. ERROR in X.java (at line 17)
					persons.sort(Comparator.comparing(Comparator.nullsLast(Person::getEmail)));
					                                                       ^^^^^^^^^^^^^^^^
				The type X.Person does not define getEmail(T, T) that is applicable here
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430766, [1.8] Internal compiler error.
public void test430766a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Comparator;
					import java.util.List;
					import java.io.Serializable;
					public class X {
						static class Person {
							private String email;
							public Person(String email) {
								this.email = email;
							}
							public String getEmail() {
								return email;\
							}
						public static <T extends Runnable,V extends Serializable> int isRunnable(T first, V second) {
							return (second instanceof Runnable) ? 1 : 0;
						}
						}
						public static void main(String[] args) {
							List<Person> persons = new ArrayList<Person>();
							persons.add(new Person("joe.smith@gmail.com"));
							persons.add(new Person("alice.smith@gmail.com"));
							persons.sort(Comparator.comparing(Comparator.nullsLast(Person::<Runnable>isRunnable)));
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 21)
					persons.sort(Comparator.comparing(Comparator.nullsLast(Person::<Runnable>isRunnable)));
					                                             ^^^^^^^^^
				The method nullsLast(Comparator<? super T>) in the type Comparator is not applicable for the arguments (Person::<Runnable>isRunnable)
				----------
				2. ERROR in X.java (at line 21)
					persons.sort(Comparator.comparing(Comparator.nullsLast(Person::<Runnable>isRunnable)));
					                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The type X.Person does not define isRunnable(T, T) that is applicable here
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431190, [1.8] VerifyError when using a method reference
public void test431190() throws Exception {
	this.runConformTest(
		new String[] {
			"Java8VerifyError.java",
			"""
				public class Java8VerifyError {
				    public static class Foo {
				        public Object get() {
				            return new Object();
				        }
				    }
				    @FunctionalInterface
				    public static interface Provider<T> {
				        public T get();
				    }
				    public static void main(String[] args) {
				        Provider<Foo> f = () -> new Foo();
				        Provider<Provider<Object>> meta = () -> f.get()::get;
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431514 [1.8] Incorrect compilation error in lambda expression
public void test431514() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
						void fun1(int x) {
							class Local {
								FI test= () -> {
									try {
									} catch (Exception e) {
										int x;
									};
								};
							}
						}
					}
					interface FI {
						void foo();
					}"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431514 [1.8] Incorrect compilation error in lambda expression
public void test431514a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					void fun1(int x) {
						class Local {
							class L1 { }
							int y;
							FI test= () -> {
								class L1 { }\s
								int y;\s
							};
						}
					}
				\t
				}
				interface FI {
					void foo();
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432531 [1.8] VerifyError with anonymous subclass inside of lambda expression in the superclass constructor call
public void test432531() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				import java.util.function.Supplier;
				class E {
					E(Supplier<Object> factory) { }
				}
				public class Y extends E {
					Y() {
						super(() -> new Object() {
						});
					}
					public static void main(String[] args) {
						new Y();
					}
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434297 [1.8] NPE in LamdaExpression.analyseCode with lamda expression nested in a conditional expression
public void test434297() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				public class X {
				  public static void main(String[] args) {
				  }
				  public void bla() {
				    boolean b = Boolean.TRUE.booleanValue();
				    List<String> c1 = new ArrayList<>();
				    Collections.sort(c1, b ? null : new Bar(new ArrayList<>(),Comparator.nullsLast((a,e) -> {return 0;})));
				  }
				  private static class Bar implements Comparator<String>{
					  public <T> Bar(Collection<T> col, Comparator<T> comp) { }
					@Override
					public int compare(String o1, String o2) {
						return 0;
					}
				  }
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436542 : Eclipse 4.4 compiler generates "bad class file" according to javac
public void test436542() throws Exception {
	this.runConformTest(
		new String[] {
			"Utility.java",
			"""
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				public class Utility {
				    public static void main(String[] args) {
				        System.out.println("Success");
				    }
				    public static <T, R> List<R> mapList(Collection<T> original, Function<T, R> func) {
				        return original.stream().map(func).collect(Collectors.toList());
				    }
				    public static <S, T> void bindMap(List<T> dest, ObservableList<S> src, Function<S, T> func) {
				        dest.addAll(mapList(src, func));
				        src.addListener((ListChangeListener<S>) changes -> {
				            for (int i = changes.getFrom(); i < changes.getTo(); i++)
				                dest.set(i, func.apply(src.get(i)));
				        });
				    }
				    public interface ObservableList<E> extends List<E> {
				        public void addListener(ListChangeListener<? super E> listener);
				    }
				    @FunctionalInterface
				    public interface ListChangeListener<E> {
				        public abstract static class Change<E> {
				            public abstract int getFrom();
				            public abstract int getTo();
				        }
				        public void onChanged(Change<? extends E> c);
				    }
				}""",
		},
		"Success",
		Util.concatWithClassLibs(new String[]{OUTPUT_DIR}, false),
		true,
		null);
	IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "Utility.class", IClassFileReader.ALL);
	IMethodInfo lambdaMethod = null;
	IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
	int length = methodInfos.length;
	for (int i = 0; i < length; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if ("lambda$0".equals(new String(methodInfo.getName()))) {
			lambdaMethod = methodInfo;
			break;
		}
	}
	assertNotNull("Could not find lambda method",lambdaMethod);
	IClassFileAttribute signature = org.eclipse.jdt.internal.core.util.Util.getAttribute(lambdaMethod, IAttributeNamesConstants.SIGNATURE);
	assertNull("Found generic signature for lambda method", signature);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515 [1.8] ECJ reports error at method reference to overloaded instance method
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method references with overloaded varargs method
public void test439515() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Fun<T, R> {
					R apply(T arg);
				}
				public class X {
					int size() {
						return -1;
					}
					int size(Object arg) {
						return 0;
					}
					int size(X arg) {
						return 1;
					}
					public static void main(String args[]) {
						Fun<X, Integer> f1 = X::size;
						System.out.println(f1.apply(new X()));
					}
				}
				"""
	    },
	    "-1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515 [1.8] ECJ reports error at method reference to overloaded instance method
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method references with overloaded varargs method
public void test439515a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface Fun<T, R> {
					R apply(T arg);
				}
				public class X {
					static int size() {
						return -1;
					}
					static int size(Object arg) {
						return 0;
					}
					static int size(X arg) {
						return 1;
					}
					public static void main(String args[]) {
						Fun<X, Integer> f1 = X::size;
						System.out.println(f1.apply(new X()));
					}
				}
				"""
	    },
	    "1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438534 Java8 java.lang.Method.getGeneric* methods fail with java.lang.reflect.GenericSignatureFormatError: Signature Parse error: Expected Field Type Signature
public void test438534() {
	this.runConformTest(
		new String[] {
			"ByteCodeTest.java",
			"""
				import java.lang.reflect.Method;
				import java.security.AccessController;
				import java.security.PrivilegedAction;
				import java.util.Collections;
				import java.util.Comparator;
				public class ByteCodeTest {
				  public static class BrokenByteCode {
				    public void hello() {
				      Collections.sort(Collections.<String> emptyList(), Comparator.comparing((String data) -> data.length()));
				    }
				  }
				  public static void main(String[] args) {
				    for (Method method : AccessController.doPrivileged((PrivilegedAction<Method[]>) () -> BrokenByteCode.class.getDeclaredMethods())) {
				      method.getGenericExceptionTypes();
				      method.getGenericParameterTypes();
				      method.getGenericReturnType();
				    }
				    System.out.println("SUCCESS");
				  }
				}
				"""
	    },
	    "SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440152 [codegen]"Missing code implementation in the compiler" on cascaded inner class references
public void test440152() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				import java.util.function.Function;
				interface Foo {void alpha(Bar pBar);}
				class Bar {Object bravo() {return null;}}
				class Test {
				  Test(Function pFunction) {
				    class Baz {public Baz(Object pObj) {pFunction.apply(pObj);}}
				    delta(pBar -> charlie(new Baz(pBar.bravo())));
				  }
				  void charlie(Object pRemovals) {}
				  void delta(Foo pListener) {}
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440152 [codegen]"Missing code implementation in the compiler" on cascaded inner class references
public void test440152a() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				import java.util.function.Function;
				interface Foo {void alpha(Bar pBar);}
				class Bar {Object bravo() {return null;}}
				class Test {
					Test(Function pFunction) {
					    class Baz {
					    	public Baz(Object pObj) {
					    	}
					    	class NestedBaz extends Baz {
					    		NestedBaz(Object pObj) {
					    			super(pObj);
					    			pFunction.apply(pObj);
					    		}
					    	}
					    	}
					    delta(pBar -> charlie(new Baz(pBar).new NestedBaz(pBar.bravo())));
					  }
					  void charlie(Object pRemovals) {}
					  void delta(Foo pListener) {}
				}
				"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432110,  [1.8][compiler] nested lambda type incorrectly inferred vs javac
public void test432110() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Function;
				public interface X {
				    default void test() {
				        testee().flatMap(_warning_ -> {
				            return result().map(s -> 0);
				        });
				    }
				    Either<Integer, Integer> testee();
				    Either<Integer, String> result();
				    static interface Either<L, R> {
				        <U> Either<L, U> flatMap(Function<? super R, Either<L, U>> mapper);
				        <U> Either<L, U> map(Function<? super R, U> mapper);
				    }
				    public static void main(String [] args) {
				        System.out.println("OK");
				    }
				}
				""",
		},
		"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=441929, [1.8][compiler] @SuppressWarnings("unchecked") not accepted on local variable
public void test441929() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    @FunctionalInterface
				    interface X {
				        public void x();
				    }
				    public void z(X x) {
				    }
				    public <T> void test() {
				        z(() -> {
				            try {
				                @SuppressWarnings("unchecked")   // (1)
				                Class<? extends T> klass = (Class<? extends T>) Class.forName("java.lang.Object");   // (2)
				                System.out.println(klass.getName());
				            } catch (Exception e) {
				                e.printStackTrace();
				            }
				        });
				    }
				}
				""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437781, [1.8][compiler] Eclipse accepts code rejected by javac because of ambiguous method reference
public void test437781() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Consumer;
				import java.util.function.Function;
				public class X {
					public static void main(String[] args) {
						new X().visit( System.out::println );
					}
					public boolean visit(Function<Integer, Boolean> func) {
						System.out.println("Function");
						return true;
					}
					public void visit(Consumer<Integer> func) {
						System.out.println("Consumer");
					}\t
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				new X().visit( System.out::println );
				        ^^^^^
			The method visit(Function<Integer,Boolean>) is ambiguous for the type X
			----------
			2. ERROR in X.java (at line 5)
				new X().visit( System.out::println );
				               ^^^^^^^^^^^^^^^^^^^
			The type of println(Object) from the type PrintStream is void, this is incompatible with the descriptor\'s return type: Boolean
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443889, [1.8][compiler] Lambdas get compiled to duplicate methods
public void test443889() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.BiConsumer;
				import java.util.function.Consumer;
				public class X {
				    public interface CurryBiConsumer<T, U> extends BiConsumer<T, U> {
				        default public CurryConsumer<U> curryFirst(T t) {
				            return (u) -> accept(t, u);
				        }
				        default public CurryConsumer<T> currySecond(U u) {
				            return (t) -> accept(t, u);
				        }
				    }
				    public interface CurryConsumer<T> extends Consumer<T> {
				        default public Runnable curry(T t) {
				            return () -> accept(t);
				        }
				    }
				    static void execute(Runnable r) {
				        System.out.println("BEFORE");
				        r.run();
				        System.out.println("AFTER");
				    }
				    static void display(String str, int count) {
				        System.out.println("DISP: " + str + " " + count);
				    }
				    public static void main(String[] args) {
				        CurryBiConsumer<String, Integer> bc = X::display;
				        execute(bc.curryFirst("Salomon").curry(42));
				    }
				}
				"""
		},
		"""
			BEFORE
			DISP: Salomon 42
			AFTER""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=441907, [1.8][compiler] Eclipse 4.4.x compiler generics bugs with streams and lambdas
public void test441907() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import java.util.function.Predicate;
				import java.util.stream.Stream;
				public class X {
				  public static class FooBar<V> {
				  }
				  public interface FooBarred {
				    public <V> boolean hasFooBar(final FooBar<V> fooBar);
				  }
				  public interface Widget extends FooBarred {
				  }
				  public static void test() {
				    Set<FooBar<?>> foobars = new HashSet<>();
				    Set<Widget> widgets = new HashSet<>();
				    Stream<X.FooBar<?>> s = null;
				    FooBarred fb = null;
				    fb.hasFooBar((FooBar<?>) null);
				    boolean anyWidgetHasFooBar = widgets.stream().anyMatch(
				        widget -> foobars.stream().anyMatch(widget::hasFooBar)
				        );
				  }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444773, [1.8][compiler] NullPointerException in LambdaExpression.analyseCode
public void test444773() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.Optional;
				\s
				public class X {
				  static class Container {
				    final private String s;
				    public Container(String s) { this.s = s; }
				  }
				\s
				  public static void main(String[] args) {
				    final List<Container> list = new ArrayList<>();
				    final Optional<String> optStr = Optional.of("foo");
				    list.add(new Container(optStr.orElseThrow(() -> new IllegalStateException()))); // Error here
				\s
				    // This will work:
				    final String s = optStr.orElseThrow(IllegalStateException::new);
				    list.add(new Container(s));\t
				  }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444772, [1.8][compiler] NullPointerException in ReferenceExpression.shouldGenerateImplicitLambda
public void test444772() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.Optional;
				\s
				public class X {
				  static class Container {
				    final private String s;
				    public Container(String s) { this.s = s; }
				  }
				\s
				  public static void main(String[] args) {
				    final List<Container> list = new ArrayList<>();
				    final Optional<String> optStr = Optional.of("foo");
				    list.add(new Container(optStr.orElseThrow(IllegalStateException::new))); // Error here
				\s
				    // This will work:
				    final String s = optStr.orElseThrow(IllegalStateException::new);
				    list.add(new Container(s));\t
				  }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444803, [1.8][compiler] Exception in thread "main" java.lang.VerifyError: Bad local variable type
public void test444803() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				public class X {
				    X abc = null;
				    public static void main(String[] args) {
				        new X();
				    }
				    private void doSth() {
				        final List<String> l = new ArrayList<>();
				        try {
				            System.out.println("ok");
				        } finally {
				            Runnable r = () -> abc.terminateInstances(abc.withInstanceIds(l));
				        }
				    }
				    public void terminateInstances(X abc) {
				    }
				    public X withInstanceIds(Collection<String> arg0) {
				    	return null;
				    }
				}
				interface FI {
					public void foo(Collection<String> arg0);
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444785, [1.8] Error in JDT Core during reconcile
public void test444785() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				import java.util.function.Function;
				public interface X {
					@FunctionalInterface
					static interface Function1<T1, R> extends Function<T1, R>, Serializable {
						@Override
						R apply(T1 t1);
					}
					@FunctionalInterface
					static interface Function6<T1, T2, T3, T4, T5, T6, R> extends Serializable {
						R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
						default Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>>> curried() {
							return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> apply(t1, t2, t3, t4, t5, t6);
						}
						default Function1<Tuple6<T1, T2, T3, T4, T5, T6>, R> tupled() {
							return t -> apply(t._1, t._2, t._3, t._4, t._5, t._6);
						}
					}
					static final class Tuple6<T1, T2, T3, T4, T5, T6> {
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
						}
					}
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost generic type information (4.4 -> 4.4.1 regression)
public void test447119() {
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.util.Arrays;
					import java.util.function.Function;
					import java.util.List;
					public class X {
					    private static List<String> foo(List<String> x){return x;}
					    public static void main(String[] args) {
					        Function<List<String>,List<String>> f = i -> { return i; };
					        Method[] methods = X.class.getDeclaredMethods();
					        for (Method m : methods) {
					        	if (m.getName().contains("lambda")) {
					        		System.out.println("- " + m.getGenericReturnType() + " " + m.getName() + "(" + Arrays.asList(m.getGenericParameterTypes()) + ")");
					        	}
					        }
					    }
					}
					"""
			};
	runner.expectedOutputString =
			"- interface java.util.List lambda$0([interface java.util.List])";
	runner.expectedJavacOutputString =
			"- interface java.util.List lambda$main$0([interface java.util.List])";
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost generic type information (4.4 -> 4.4.1 regression)
public void test447119a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.util.Arrays;
					import java.util.function.Function;
					import java.util.List;
					public class X {
					    private static List<String> foo(List<String> x){return x;}
					    public static void main(String[] args) {
					        Function<List<String>,List<String>> f = X::foo;
					        Method[] methods = X.class.getDeclaredMethods();
					        for (Method m : methods) {
					        	if (m.getName().contains("lambda")) {
					        		System.out.println("- " + m.getGenericReturnType() + " " + m.getName() + "(" + Arrays.asList(m.getGenericParameterTypes()) + ")");
					        	}
					        }
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost generic type information (4.4 -> 4.4.1 regression)
public void test447119b() {
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.util.Arrays;
					import java.util.function.Function;
					import java.util.List;
					import java.io.Serializable;\
					public class X {
					    private static interface SerializableFunction<A, R> extends Function<A, R>, Serializable { }\
					    private static List<String> foo(List<String> x){return x;}
					    public static void main(String[] args) {
					        SerializableFunction<List<String>, List<String>> f = i -> { return i; };
					        Method[] methods = X.class.getDeclaredMethods();
					        for (Method m : methods) {
					        	if (m.getName().contains("lambda")) {
					        		System.out.println("- " + m.getGenericReturnType() + " " + m.getName() + "(" + Arrays.asList(m.getGenericParameterTypes()) + ")");
					        	}
					        }
					    }
					}
					"""
			};
	runner.expectedOutputString =
			"- interface java.util.List lambda$0([interface java.util.List])";
	runner.expectedJavacOutputString =
			"- interface java.util.List lambda$main$7796d039$1([interface java.util.List])";
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost generic type information (4.4 -> 4.4.1 regression)
public void test447119c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.util.Arrays;
					import java.util.function.Function;
					import java.util.List;
					import java.io.Serializable;\
					public class X {
					    private static interface SerializableFunction<A, R> extends Function<A, R>, Serializable { }\
					    private static List<String> foo(List<String> x){return x;}
					    public static void main(String[] args) {
					        SerializableFunction<List<String>, List<String>> f = X::foo;
					        Method[] methods = X.class.getDeclaredMethods();
					        for (Method m : methods) {
					        	if (m.getName().contains("foo")) {
					        		System.out.println("- " + m.getGenericReturnType() + " " + m.getName() + "(" + Arrays.asList(m.getGenericParameterTypes()) + ")");
					        	}
					        }
					    }
					}
					"""
			},
			"- java.util.List<java.lang.String> foo([java.util.List<java.lang.String>])");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost generic type information (4.4 -> 4.4.1 regression)
public void test447119d() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.ObjectStreamClass;
					import java.io.Serializable;
					import java.lang.invoke.SerializedLambda;
					import java.lang.reflect.Method;
					import java.util.List;
					import java.util.function.Function;
					import java.util.ArrayList;
					import java.util.Collections;
					import java.util.Comparator;
					public class X {
						private static interface SerializableFunction<A, R> extends Function<A, R>, Serializable { }
						private static List<String> noop(List<String> l) { return l; }
						public static void main(String[] args) throws Exception {
							SerializableFunction<List<String>, List<String>> f = X::noop;
							Method invokeWriteReplaceMethod = ObjectStreamClass.class.getDeclaredMethod("invokeWriteReplace", Object.class);
							invokeWriteReplaceMethod.setAccessible(true);
							SerializedLambda l = (SerializedLambda)invokeWriteReplaceMethod.invoke(ObjectStreamClass.lookupAny(f.getClass()), f);
							System.out.println("Lambda binds to: " + l.getImplClass() + "." + l.getImplMethodName());
							System.out.println("Methods (with generics):");
							List<String> list = new ArrayList<String>();
							for(Method m : X.class.getDeclaredMethods()) {
								if(m.getName().equals("main")) continue;
								if(m.getName().contains("deserializeLambda")) continue;
								list.add("- " + m.getGenericReturnType() + " " + m.getName() + "(" + m.getGenericParameterTypes()[0] + ")");
							}
							Collections.sort(list, new Comparator<String>() {
								public int compare(String s1, String s2) {
									return s1.compareTo(s2);
								}
							});
							System.out.println(list.toString());
						}
					}
					"""
			},
			"""
				Lambda binds to: X.noop
				Methods (with generics):
				[- java.util.List<java.lang.String> noop(java.util.List<java.lang.String>)]""",
			null,
			true,
			(isJRE9Plus
			? new String[] { "--add-opens", "java.base/java.io=ALL-UNNAMED" }
			: new String [] { "-Ddummy" }) // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost generic type information (4.4 -> 4.4.1 regression)
public void test447119e() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.Method;
					import java.lang.reflect.Parameter;
					import java.util.Arrays;
					import java.util.function.Function;
					import java.util.List;
					public class X implements java.io.Serializable {
					    private static List<String> foo(List<String> x){return x;}
					    public static void main(String[] args) {
					        Function<List<String>,List<String>> f = X::foo;
					        Method[] methods = X.class.getDeclaredMethods();
					        for (Method m : methods) {
					        	if (m.getName().contains("lambda")) {
					        		System.out.println("- " + m.getGenericReturnType() + " " + m.getName() + "(" + Arrays.asList(m.getGenericParameterTypes()) + ")");
					        	}
					        }
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432605, [1.8] Incorrect error "The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here"
public void test432605() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.HashMap;
				import java.util.function.Function;
				import java.util.function.Supplier;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				public class X {
				static <T, E extends Exception, K, L, M> M terminalAsMapToList(
				    Function<? super T, ? extends K> classifier,
				    Function<HashMap<K, L>, M> intoMap,
				    Function<ArrayList<T>, L> intoList,
				    Supplier<Stream<T>> supplier,
				    Class<E> classOfE) throws E {
				  	return terminalAsCollected(
				  	  classOfE,
				  	  Collectors.collectingAndThen(
				  	    Collectors.groupingBy(
				  	      classifier,
				  	      HashMap<K, L>::new,
				  	      Collectors.collectingAndThen(
				  	      	// The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here
				  	      	// from ArrayList<T>::add:
				  	        Collector.of(ArrayList<T>::new, ArrayList<T>::add, (ArrayList<T> left, ArrayList<T> right) -> {\s
				  		        left.addAll(right);
				  		        return left;
				  	        }),
				  	        intoList)),
				  	    intoMap),
				  	  supplier);
				  }
					static <E extends Exception, T, M> M terminalAsCollected(
				    Class<E> class1,
				    Collector<T, ?, M> collector,
				    Supplier<Stream<T>> supplier) throws E {
				  	try(Stream<T> s = supplier.get()) {
				  		return s.collect(collector);
				  	} catch(RuntimeException e) {
				  		throw unwrapCause(class1, e);
				  	}
				  }
					static <E extends Exception> E unwrapCause(Class<E> classOfE, RuntimeException e) throws E {
						Throwable cause = e.getCause();
						if(classOfE.isInstance(cause) == false) {
							throw e;
						}
						throw classOfE.cast(cause);
				}
				}
				"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432605, [1.8] Incorrect error "The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here"
public void testreduced432605() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.HashMap;
				import java.util.function.Function;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				public class X {
				    static <T, K, L, M> void foo() {
					Collector<T, ?, M> cat =\s
				            Collectors.collectingAndThen(
						Collectors.groupingBy((Function<? super T, ? extends K>) null,\s
								HashMap<K, L>::new,\s
								(Collector<T, ArrayList<T>, L>) null),\s
								(Function<HashMap<K, L>, M>) null);
					}
				}
				"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448802, [1.8][compiler] Poly invocations interleaved by a impertinent lambda may need some more changes,
public void test448802() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Optional;
				public class X {
					public static void main(String[] args) {
						Optional<String> userName = Optional.of("sa");
						Optional<String> password = Optional.of("sa");
						boolean isValid = userName.flatMap((String u) -> {
							return password.map((String p) -> {
								return u.equals("sa") && p.equals("sa");
							});
						}).orElse(false);
						System.out.println(isValid);
					}
				}
				"""
		},
		"true");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.GENERATE);
	this.runConformTest(
		false,
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.invoke.SerializedLambda;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test {
				    public static interface Map<IN, OUT> {
				        public OUT map(IN in);
				    }
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = (in) -> new Tuple<>();
				        for(Method m : Test.class.getDeclaredMethods()) {
				        // Use the type information stored in signature
				            if (m.getName().contains("lambda")) {
				              System.out.println(m.getGenericReturnType());
				              for (Type t : m.getGenericParameterTypes()) {
				                  System.out.println(t);
				              }
				            }
				        }
				    }
				}"""
			},
		null,
		customOptions,
		null,
		(reflectNestedClassUseDollar
		? "Test$Tuple<java.lang.Integer, java.lang.String>\n" +
		  "Test$Tuple<java.lang.String, java.lang.Double>"
		: "Test.Test$Tuple<java.lang.Integer, java.lang.String>\n" +
		  "Test.Test$Tuple<java.lang.String, java.lang.Double>"),
		null,
		EclipseJustification.EclipseBug449063);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.GENERATE);
	this.runConformTest(
		false,
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.invoke.SerializedLambda;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test {
				    public static interface Map<IN, OUT> extends Serializable {
				        public OUT map(IN in);
				    }
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = (in) -> new Tuple<>();
				        SerializedLambda sl = getSerializedLambda(map);     \s
				        Method m = getLambdaMethod(sl);
				        // Use the type information stored in signature
				        System.out.println(m.getGenericReturnType());
				        for (Type t : m.getGenericParameterTypes()) {
				            System.out.println(t);
				        }
				    }
				    public static Method getLambdaMethod(SerializedLambda lambda) throws Exception {
				        String implClassName = lambda.getImplClass().replace(\'/\', \'.\');
				        Class<?> implClass = Class.forName(implClassName);
				        String lambdaName = lambda.getImplMethodName();
				        for (Method m : implClass.getDeclaredMethods()) {
				            if (m.getName().equals(lambdaName)) {
				                return m;
				            }
				        }
				        throw new Exception("Lambda Method not found");
				    }
				    public static SerializedLambda getSerializedLambda(Object function) throws Exception {
				        if (function == null || !(function instanceof java.io.Serializable)) {
				            throw new IllegalArgumentException();
				        }
				        for (Class<?> clazz = function.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
				            try {
				                Method replaceMethod = clazz.getDeclaredMethod("writeReplace");
				                replaceMethod.setAccessible(true);
				                Object serializedForm = replaceMethod.invoke(function);
				                if (serializedForm instanceof SerializedLambda) {
				                    return (SerializedLambda) serializedForm;
				                }
				            }
				            catch (NoSuchMethodError e) {
				                // fall through the loop and try the next class
				            }
				            catch (Throwable t) {
				                throw new RuntimeException("Error while extracting serialized lambda", t);
				            }
				        }
				        throw new Exception("writeReplace method not found");
				    }
				}"""
			},
		null,
		customOptions,
		null,
		(reflectNestedClassUseDollar
		? "Test$Tuple<java.lang.Integer, java.lang.String>\n" +
		  "Test$Tuple<java.lang.String, java.lang.Double>"
		: "Test.Test$Tuple<java.lang.Integer, java.lang.String>\n" +
		  "Test.Test$Tuple<java.lang.String, java.lang.Double>"),
		null,
		EclipseJustification.EclipseBug449063);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.DO_NOT_GENERATE);
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.invoke.SerializedLambda;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test {
				    public static interface Map<IN, OUT> {
				        public OUT map(IN in);
				    }
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = (in) -> new Tuple<>();
				        for(Method m : Test.class.getDeclaredMethods()) {
				        // Use the type information stored in signature
				            if (m.getName().contains("lambda")) {
				              System.out.println(m.getGenericReturnType());
				              for (Type t : m.getGenericParameterTypes()) {
				                  System.out.println(t);
				              }
				            }
				        }
				    }
				}"""
			},
			"class Test$Tuple\n" +
			"class Test$Tuple",
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.DO_NOT_GENERATE);
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.invoke.SerializedLambda;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test {
				    public static interface Map<IN, OUT> extends Serializable {
				        public OUT map(IN in);
				    }
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = (in) -> new Tuple<>();
				        SerializedLambda sl = getSerializedLambda(map);     \s
				        Method m = getLambdaMethod(sl);
				        // Use the type information stored in signature
				        System.out.println(m.getGenericReturnType());
				        for (Type t : m.getGenericParameterTypes()) {
				            System.out.println(t);
				        }
				    }
				    public static Method getLambdaMethod(SerializedLambda lambda) throws Exception {
				        String implClassName = lambda.getImplClass().replace(\'/\', \'.\');
				        Class<?> implClass = Class.forName(implClassName);
				        String lambdaName = lambda.getImplMethodName();
				        for (Method m : implClass.getDeclaredMethods()) {
				            if (m.getName().equals(lambdaName)) {
				                return m;
				            }
				        }
				        throw new Exception("Lambda Method not found");
				    }
				    public static SerializedLambda getSerializedLambda(Object function) throws Exception {
				        if (function == null || !(function instanceof java.io.Serializable)) {
				            throw new IllegalArgumentException();
				        }
				        for (Class<?> clazz = function.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
				            try {
				                Method replaceMethod = clazz.getDeclaredMethod("writeReplace");
				                replaceMethod.setAccessible(true);
				                Object serializedForm = replaceMethod.invoke(function);
				                if (serializedForm instanceof SerializedLambda) {
				                    return (SerializedLambda) serializedForm;
				                }
				            }
				            catch (NoSuchMethodError e) {
				                // fall through the loop and try the next class
				            }
				            catch (Throwable t) {
				                throw new RuntimeException("Error while extracting serialized lambda", t);
				            }
				        }
				        throw new Exception("writeReplace method not found");
				    }
				}"""
			},
			"class Test$Tuple\n" +
			"class Test$Tuple",
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.GENERATE);
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.invoke.SerializedLambda;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test {
				    public static interface Map<IN, OUT> {
				        public OUT map(IN in);
				    }
				    public static Tuple<Integer, String> noop(Tuple<String, Double> t){return null;}
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = Test::noop;
				        for(Method m : Test.class.getDeclaredMethods()) {
				        // Use the type information stored in signature
				            if (m.getName().contains("lambda")) {
				              System.out.println(m.getGenericReturnType());
				              for (Type t : m.getGenericParameterTypes()) {
				                  System.out.println(t);
				              }
				            }
				        }
				    }
				}"""
			},
			"",
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063e() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.DO_NOT_GENERATE);
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.invoke.SerializedLambda;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test {
				    public static interface Map<IN, OUT> extends Serializable {
				        public OUT map(IN in);
				    }
				    public static Tuple<Integer, String> noop(Tuple<String, Double> t){return null;}
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = Test::noop;
				        SerializedLambda sl = getSerializedLambda(map);     \s
				        Method m = getLambdaMethod(sl);
				        // Use the type information stored in signature
				        System.out.println(m.getGenericReturnType());
				        for (Type t : m.getGenericParameterTypes()) {
				            System.out.println(t);
				        }
				    }
				    public static Method getLambdaMethod(SerializedLambda lambda) throws Exception {
				        String implClassName = lambda.getImplClass().replace(\'/\', \'.\');
				        Class<?> implClass = Class.forName(implClassName);
				        String lambdaName = lambda.getImplMethodName();
				        for (Method m : implClass.getDeclaredMethods()) {
				            if (m.getName().equals(lambdaName)) {
				                return m;
				            }
				        }
				        throw new Exception("Lambda Method not found");
				    }
				    public static SerializedLambda getSerializedLambda(Object function) throws Exception {
				        if (function == null || !(function instanceof java.io.Serializable)) {
				            throw new IllegalArgumentException();
				        }
				        for (Class<?> clazz = function.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
				            try {
				                Method replaceMethod = clazz.getDeclaredMethod("writeReplace");
				                replaceMethod.setAccessible(true);
				                Object serializedForm = replaceMethod.invoke(function);
				                if (serializedForm instanceof SerializedLambda) {
				                    return (SerializedLambda) serializedForm;
				                }
				            }
				            catch (NoSuchMethodError e) {
				                // fall through the loop and try the next class
				            }
				            catch (Throwable t) {
				                throw new RuntimeException("Error while extracting serialized lambda", t);
				            }
				        }
				        throw new Exception("writeReplace method not found");
				    }
				}"""
			},
			(reflectNestedClassUseDollar
			? "Test$Tuple<java.lang.Integer, java.lang.String>\n" +
			  "Test$Tuple<java.lang.String, java.lang.Double>"
			: "Test.Test$Tuple<java.lang.Integer, java.lang.String>\n" +
			  "Test.Test$Tuple<java.lang.String, java.lang.Double>"),
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063, [1.8][compiler] Bring back generic signatures for Lambda Expressions
public void test449063f() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_LambdaGenericSignature, CompilerOptions.GENERATE);
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.io.Serializable;
				import java.lang.reflect.Method;
				import java.lang.reflect.Type;
				public class Test implements Serializable{
				    public static interface Map<IN, OUT> {
				        public OUT map(IN in);
				    }
				    public static Tuple<Integer, String> noop(Tuple<String, Double> t){return null;}
				    public static class Tuple<T1, T2> {
				        private T1 field1;
				        private T2 field2;
				    }
				    public static void main(String[] strings) throws Exception {
				        Map<Tuple<String, Double>, Tuple<Integer, String>> map = Test::noop;
				        for(Method m : Test.class.getDeclaredMethods()) {
				        // Use the type information stored in signature
				            if (m.getName().contains("lambda")) {
				              System.out.println(m.getGenericReturnType());
				              for (Type t : m.getGenericParameterTypes()) {
				                  System.out.println(t);
				              }
				            }
				        }
				    }
				}"""
			},
			"",
			customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=445949, Lambda parameter not shadowing in nested scope producing non-existent compilation error
public void test445949() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Consumer;
				public class X {
					void methodInFirstLevel(int y) {
						class Second {
							int t = y;
							Consumer<Integer> myConsumer1 = (z) -> {
								System.out.println("z = " + z);
								System.out.println("y = " + y);
								System.out.println("t = " + t);
							};
							Consumer<Integer> myConsumer2 = (y) -> {
								System.out.println("y = " + y);
								System.out.println("t = " + t);
							};
							void foo( int y) {
								System.out.println("y = " + y);
							}
							class Third {
								Consumer<Integer> myConsumer3 = (y) -> {
									System.out.println("y = " + y);
								};
							}
							void bar(int y) {
								new Third().myConsumer3.accept(y);
							}
				 		}
						new Second().myConsumer1.accept(10);
						new Second().myConsumer2.accept(20);
						new Second().foo(30);
						new Second().bar(40);
					\t
					}
					void foo() {
				  		Consumer<Integer> myConsumer2 = (y) -> {
						class Inner {
					  	Consumer<Integer> myConsumer4 = (y) -> {\s
						class InnerMost {
						Consumer<Integer> myConsumer3 = (y /*error without fix*/) -> {};
						}
					  	};
						}
						new Inner().myConsumer4.accept(10);
					};
					}
					public static void main(String[] args) {
						new X().methodInFirstLevel(5);
						new X().foo();
					}
				}
				"""
	},
	"z = 10\ny = 5\nt = 5\ny = 20\nt = 5\ny = 30\ny = 40");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=445949, Lambda parameter not shadowing in nested scope producing non-existent compilation error
public void test445949a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Consumer;
				class X {
					void foo(int y) {
						Consumer<Integer> c1 = (y)-> {};
					}
					void foo2() {
						int y;
						Consumer<Integer> c1 = (y)-> {};
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				Consumer<Integer> c1 = (y)-> {};
				                        ^
			Lambda expression's parameter y cannot redeclare another local variable defined in an enclosing scope.\s
			----------
			2. ERROR in X.java (at line 8)
				Consumer<Integer> c1 = (y)-> {};
				                        ^
			Lambda expression's parameter y cannot redeclare another local variable defined in an enclosing scope.\s
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=456395, can't compile the Java8 code
public void test456395() {
	this.runConformTest(
			new String[] {
			"Test.java",
			"""
				import java.io.*;
				import java.util.*;
				import java.util.stream.*;
				import static java.util.stream.Collectors.*;
				public class Test {
				   public static void main(String[] args) throws IOException {
				      Stream<Locale> locales = Stream.of(Locale.getAvailableLocales());
				      locales = Stream.of(Locale.getAvailableLocales());
				      Map<String, Set<String>> countryToLanguages = locales.collect(
				         groupingBy(Locale::getDisplayCountry,\s
				            mapping(Locale::getDisplayLanguage,
				               toSet())));
				   }
				}
				"""});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=459305
public void test459305() {
	this.runConformTest(
			new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				import java.util.function.BiConsumer;
				import java.util.function.Consumer;
				public class X {
				   public static void main(String[] args) {
				      foo(arg1 -> bar(X::baz));
				   }
				   private static <A1> void foo(Consumer<A1> c) { c.accept(null); }
				   private static void baz(String s1, String s2) { System.out.println(s1 + "::" + s2); }
				   private static void bar(VoidMethodRef2<String, String> mr2) { mr2.accept("one", "two"); }
				   private static interface VoidMethodRef2<A1, A2> extends BiConsumer<A1, A2>, Serializable {}
				}
				"""},
			"one::two");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=467825 Missing code implementation in the compiler
public void test467825() {
	this.runConformTest(
		new String[] {
			"Main.java",
			"""
				import java.util.function.Function;
				public class Main {
				    public Function<String, String> f(int x) {
				    	class A {
				    		void g() {
				    	        System.out.println(x);
				    		}
				    	}
				        return s -> {
				        	A a = new A();
				            return s;
				        };
				    }
				}
				"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=467825 Missing code implementation in the compiler
public void test467825a() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.Function;
				interface Foo {void alpha(Bar pBar);}
				class Bar {Object bravo() {return null;}}
				class Test {
					Foo foo(Function pFunction) {
					    class Baz {
					    	public Baz(Object pObj) {
					    	}
					    	class NestedBaz extends Baz {
					    		NestedBaz(Object pObj) {
					    			super(pObj);
					    			pFunction.apply(pObj);
					    		}
					    	}
					    }
					    return pBar -> {
					    		Object o = new Baz(pBar).new NestedBaz(pBar.bravo());
					    	};
					  }
					  void charlie(Object pRemovals) {}
					  void delta(Foo pListener) {}
				}
				"""
	});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=461004 Multiple spurious errors compiling FunctionalJava project
public void test461004() {
	this.runConformTest(
		false /* skipJavac */,
		JavacHasABug.JavacBugFixed_901,
		new String[] {
			"Ice.java",
			"""
				import java.util.function.BiPredicate;
				import java.util.function.Function;
				class Ice {
				  static <T> BiPredicate<T, T> create(BiPredicate<? super T, ? super T> fn) {
				    return null;
				  }
				  static <T, K> BiPredicate<T, T> create(Function<? super T, ? super K> map) {
				    return null;
				  }
				  void someMethod(BiPredicate<String, String> b) {}
				  void method() {
				    BiPredicate<String, String> eq = String::equalsIgnoreCase;
				    // these all compile:
				    BiPredicate<String, String> ok1 = create( eq );
				    BiPredicate<String, String> ok2 = create( (a, b) -> true );
				    BiPredicate<String, String> ok3 = create( String::valueOf );
				    // this causes an internal compiler error, ArrayIndexOutOfBoundsException: 1
				    someMethod(create( String::equalsIgnoreCase ));
				  }
				}
				"""
	}, null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=478533 [compiler][1.8][lambda] check visibility of target context is broken
public void test478533() {
	this.runConformTest(
		new String[] {
			"test/BugDemonstrator.java",
			"""
				package test;
				import test.subpackage.B;
				public class BugDemonstrator {
					public static void main(String[] args) {
						// OK
						invoke(new B() {
							public String invoke(Integer input) {
								return null;
							}
						});
						// ERROR
						invoke((Integer i) -> { // Error is here: The type A<Object,Integer> from the descriptor computed for the target context is not visible here.
							return null;
						});
					}
					private static String invoke(B b) {
						return b.invoke(1);
					}
				}
				""",
			"test/subpackage/A.java",
			"""
				package test.subpackage;
				interface A<I> {
					String invoke(I input);
				}
				""",
			"test/subpackage/B.java",
			"package test.subpackage;\n" +
			"public interface B extends A<Integer> {}\n"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=478533 [compiler][1.8][lambda] check visibility of target context is broken
public void test478533a() {
	this.runNegativeTest(
		new String[] {
			"test/BugDemonstrator.java",
			"""
				package test;
				import test.subpackage.C;
				public class BugDemonstrator {
					public static void main(String[] args) {
						C c = new C();
						c.invoke((Integer i) -> {\s
							return null;
						}, 2);
					}
				}
				""",
			"test/subpackage/A.java",
			"""
				package test.subpackage;
				public interface A<I> {
					String invoke(I input);
				}
				""",
			"test/subpackage/B.java",
			"package test.subpackage;\n" +
			"interface B extends A<Integer> {}\n" ,
			"test/subpackage/C.java",
			"""
				package test.subpackage;
				public class C {
					public String invoke(B b, Integer input) {
						return b.invoke(input);
					}
				}
				"""
	},
	"""
		----------
		1. ERROR in test\\BugDemonstrator.java (at line 6)
			c.invoke((Integer i) -> {\s
			         ^^^^^^^^^^^^^^
		The type B from the descriptor computed for the target context is not visible here. \s
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477263 [1.8][compiler] No enclosing instance of the type Outer is accessible in scope for method reference
public void test477263() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.Function;
				public interface Test<T> {
				    static <K> void test(Function<?, ? extends K> function) {
				        class Outer {
				        	Outer(K k) {}
				            class Inner {
				                public Inner(K k) {}
				                private void method(K k) {
				                    System.out.println(function.apply(null));
				                    Function<K, Inner> f = Inner::new;
				                    Function<K, Outer> f2 = Outer::new;
				                }
				            }
				        }
				        new Outer(null).new Inner(null).method(null);
				    }
				    public static void main(String[] args) {
						Test.test((k) -> "Success");
					}
				}"""
	},
	"Success");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477263 [1.8][compiler] No enclosing instance of the type Outer is accessible in scope for method reference
public void test477263a() {
	this.runConformTest(
		false,
		JavacHasABug.JavacBug8144673,
		new String[] {
			"X.java",
			"""
				interface I {
					X makeX(int x);
				}
				public class X {
					void foo() {
						int local = 10;
						class Y extends X {
							class Z extends X  {
								private Z(int z) {
								}
								private Z() {}
							}
							private Y(int y) {
								System.out.println(y);
							}
							 Y() {
							}
						}
						I i = Y :: new;
						i.makeX(local);
					}
					private X(int x) {
					}
					X() {
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}"""
	},
	"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477263 [1.8][compiler] No enclosing instance of the type Outer is accessible in scope for method reference
public void test477263b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					X makeX(int x);
				}
				public class X {
					void foo() {
						int local = 10;
						class Y extends X {
							class Z extends X  {
								private Z(int z) {
									System.out.println(local);
								}
								void f(int in) {
									I i2 = Z::new;
									i2.makeX(in);
								}
								private Z() {}
							}
							private Y(int y) {
								System.out.println("Y");
							}
							 Y() {
							}
						}
						new Y().new Z().f(0);
					}
					private X(int x) {
						System.out.println(x);
					}
					X() {
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}"""
	},
	"10");
}
public void testBug487586() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				
				interface Calculator {
				    public int calculate(int a, int b);
				}
				
				interface Sumator {
				    public int test();
				
				    public int test3(int a, int b);
				}
				
				// intersection of both types
				interface Both extends Sumator, Calculator {
				
				}
				public class X {
				  public static void main(String[] args) {
				    Calculator test = (Calculator & Sumator) (a, b) -> a + b;
				    System.out.println(test.calculate(2, 3));
				
				    Sumator sumator = (Calculator & Sumator) (a, b) -> a + b; // does compile, but throws an Exception
				    sumator.test();
				
				    Both both = (Both) (a, b) -> a + b; // does not compile
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 18)
				Calculator test = (Calculator & Sumator) (a, b) -> a + b;
				                                         ^^^^^^^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			2. ERROR in X.java (at line 21)
				Sumator sumator = (Calculator & Sumator) (a, b) -> a + b; // does compile, but throws an Exception
				                                         ^^^^^^^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			3. ERROR in X.java (at line 24)
				Both both = (Both) (a, b) -> a + b; // does not compile
				                   ^^^^^^^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=452587 Java 8: Method references to the same method do not share BootstrapMethod
public void testBug452587() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				 public class Test {
				    public static void main(String[] args) {
				      Runnable m = Test::m;
				      Runnable n = Test::m;
				      Runnable o = Test::m;
				      Runnable p = Test::m;
				      Runnable q = Test::m;
				    }
				    public static void m() {}
				  }
				"""
	});
	IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "Test.class", IClassFileReader.ALL);
	BootstrapMethodsAttribute bootstrapMethodsAttribute = null;
	IClassFileAttribute[] attrs = classFileReader.getAttributes();
	for (int i=0,max=attrs.length;i<max;i++) {
		if (new String(attrs[i].getAttributeName()).equals("BootstrapMethods")) {
			bootstrapMethodsAttribute = (BootstrapMethodsAttribute)attrs[i];
			break;
		}
	}
	assertNotNull("BootstrapMethods attribute not found", bootstrapMethodsAttribute);
	int bmaLength = bootstrapMethodsAttribute.getBootstrapMethodsLength();
	assertEquals("Incorrect number of bootstrap methods found", 1, bmaLength);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=485529 [1.8][compiler] Verify error with constructor reference to nested class constructor
public void testBug485529() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					X makeX(int x);
				}
				public class X {
						class Y extends X {
							class Z extends X  {
								private Z(int z) {
								}
								private Z() {}
							}
							private Y(int y) {
							}
							 Y() {
							}
						}
						I i = Y :: new;
					private X(int x) {
					}
				\t
					X() {
					}
					public static void main(String[] args) {
						new X();
					\t
					}
				}"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=479284 [1.8][inference] fail to resolve matching types for lambda and method reference + NPE at build
public void testBug479284() {
	runNegativeTest(
		new String[] {
			"BadInferenceMars451.java",
			"""
				package bug.report;
				import java.util.ArrayList;
				import java.util.Arrays;
				import java.util.List;
				import java.util.Map;
				import java.util.function.BinaryOperator;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				/**
				 * Problem is valid on Version: Mars.1 Release (4.5.1) Build id: 20150924-1200
				 */
				public class BadInferenceMars451 {
					public static Map<Object, List<X>> BadInferenceMars451Casus1() {
						List<X> stuff = new ArrayList<>();
						return stuff.stream().collect(Collectors.toMap(Function.identity(), t -> Arrays.asList(t), BadInferenceMars451::sum));
					}
					public static Map<Object, List<X>> BadInferenceMars451Casus1Fixed1() {
						List<X> stuff = new ArrayList<>();
						return stuff.stream().collect(Collectors.toMap(Function.identity(), t -> Arrays.asList(t), (BinaryOperator<List<X>>) BadInferenceMars451::sum));
					}
					public static Map<Object, List<X>> BadInferenceMars451Casus1Fixed2() {
						List<X> stuff = new ArrayList<>();
						return stuff.stream().collect(Collectors.toMap(Function.identity(), t -> Arrays.<X> asList(t), BadInferenceMars451::sum));
					}
					/*\s
					 * Uncomment this to see eclipse crash at build
					 * this doesnt work but it should not crash the ide
					 */\s
					public static Map<Object, List<X>> BadInferenceMars451Casus1Crash() {
						List<X> stuff = new ArrayList<>();
						return stuff.stream().collect(Collectors.toMap(Function.identity(), t -> Arrays.asList(t), BadInferenceMars451<X>::sum));
					}
					public static <T> List<T> sum(List<T> l1, List<T> l2) {
						return null;
					}
					public static class X {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in BadInferenceMars451.java (at line 31)
				return stuff.stream().collect(Collectors.toMap(Function.identity(), t -> Arrays.asList(t), BadInferenceMars451<X>::sum));
				                                                                                           ^^^^^^^^^^^^^^^^^^^
			The type BadInferenceMars451 is not generic; it cannot be parameterized with arguments <BadInferenceMars451.X>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=491139 Lambda that redefines a default method with generics
public void testBug491139() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					interface Foo<T> {
							default String bar(String s) {
							return ("default : " + s);
						}
						String bar(T t);
					}
					public String testLambdaRedefiningADefault() {
						Foo<String> foo =
						    (t) -> {
						      return "lambda : " + t;
						  };
						return (((Foo)foo).bar("String"));
					}
					public static void main(String[] args) {
						System.out.println(new Test().testLambdaRedefiningADefault());
					}
				}
				"""
	},
	"lambda : String");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=489631 [1.8] "java.lang.VerifyError: Bad type on operand stack" with lamba and type defined in method
public void test489631() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				interface I { int getLength(); }
				public class Test {
					public void test() {
						class A<T> {
							List<T> l;
							public A(List<T> l) {
								this.l = l;
							}
						}
						List<Integer> list = new ArrayList<>();
						I i = () -> new A<>(list).l.size();
						System.out.println(i.getLength());
					}
					public static void main(String[] args) {
						new Test().test();
					}
				}"""
	},
	"0");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=489631 [1.8] "java.lang.VerifyError: Bad type on operand stack" with lamba and type defined in method
public void test489631a() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				interface I { int getLength(); }
				public class Test {
					public void test() {
						class A<T> {
							List<T> l;
							public A(List<T> l) {
								this.l = l;
							}
							class B {
								List<Integer> iL;
								public B(List<Integer> l) {
									// super(l);
									this.iL = l;
								}
							}
						}
						List<Integer> list = new ArrayList<>();
						I i = () -> new A<>(list).new B(list).iL.size();
						System.out.println(i.getLength());
					}
					public static void main(String[] args) {
						new Test().test();
					}
				}"""
	},
	"0");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=476859 enclosing method not found error when EJC compiled, works fine with oracle jdk compiler
public void test476859() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"""
				import java.lang.reflect.Method;
				import java.util.function.Function;
				public class Test {
				  public static void main(String[] args) {
				    final Function<Void,Method> f = __ -> {
				    	class Dummy{}
				      return new Dummy(){}.getClass().getEnclosingMethod();
				    };
				    System.out.println(f.apply(null));
				  }
				}"""
		};
	runner.expectedOutputString =
		"public static void Test.main(java.lang.String[])";
	runner.expectedJavacOutputString =
		"public static void Test.main(java.lang.String[])";
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=476859 enclosing method not found error when EJC compiled, works fine with oracle jdk compiler
public void test476859a() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"""
				import java.lang.reflect.Method;
				import java.util.function.Function;
				public class Test {
				  public static void main(String[] args) {
					 \s
				    final Function<Void,Method> f = __ -> {
				    	class Dummy{}
				      return new Dummy(){}.getClass().getEnclosingMethod();
				    };
				    System.out.println(f.apply(null));
				    new AnotherClass().foo();
				  }
				}
				class AnotherClass {
					void foo() {
						final Function<Void,Method> f = __ -> {
					    	class Dummy{}
					      return new Dummy(){}.getClass().getEnclosingMethod();
					    };
					    System.out.println(f.apply(null));
					}
				}
				"""
		};
	runner.expectedOutputString =
		"public static void Test.main(java.lang.String[])\n" +
		"void AnotherClass.foo()";
	runner.expectedJavacOutputString =
		"public static void Test.main(java.lang.String[])\n" +
		"void AnotherClass.foo()";
	runner.runConformTest();
}
public void testBug499258() {
	runConformTest(
		new String[] {
			"bug499258/ShellTab.java",
			"package bug499258;\n" +
			"class Controller {\n" +
			"	public void newTerminal(Object... path) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"interface EventHandler {\n" +
			"	void handle();\n" +
			"}\n" +
			"\n" +
			"public class ShellTab {\n" +
			"	private final Controller controller;\n" +
			"\n" +
			"	public ShellTab(Controller controller) {\n" +
			"		this.controller = controller;\n" +
			"		EventHandler h = this.controller::newTerminal;\n" +
			"	}\n" +
			"}\n" +
			"",
		}
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=500374 Using a method reference to a generic method in a Base class gives me NoSuchMethodError
public void test500374() {
	this.runConformTest(
		new String[] {
			"client/Client.java",
			"""
				package client;
				import lib.Sub;
				public class Client {
				    public static void main(String[] args) throws Throwable {
				        Sub s1 = new Sub();
				        doSomething(() -> s1.m());
				        doSomething(s1::m);
				    }
				    interface Aaa {
				        Object f() throws Throwable;
				    }
				    public static void doSomething(Aaa a) throws Throwable {
				        System.out.println("Done");
				    }
				}
				""",
			"lib/Sub.java",
			"package lib;\n" +
			"public class Sub extends Base<Sub> {}",
			"lib/Base.java",
			"""
				package lib;
				class Base<T> {
				    public T m() {
				        System.out.println("m");
				        return thisInstance();
				    }
				    @SuppressWarnings("unchecked")
				    T thisInstance() {
				        return (T) this;
				    }
				}"""
	},
	"Done\n" +
	"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=500374 Using a method reference to a generic method in a Base class gives me NoSuchMethodError
public void test500374a() {
	this.runConformTest(
		new String[] {
			"client/Client.java",
			"""
				package client;
				import java.lang.invoke.MethodHandle;
				import java.lang.invoke.MethodHandles;
				import java.lang.invoke.MethodType;
				import lib.Sub;
				public class Client {
				    public static void main(String[] args) throws Throwable {
				        MethodHandle mh = MethodHandles.lookup().findVirtual(Sub.class, "m", MethodType.methodType(Object.class));
				        doSomething(mh::invoke);
				    }
				    interface Aaa {
				        Object f() throws Throwable;
				    }
				    public static void doSomething(Aaa a) throws Throwable {
				        System.out.println("Done");
				    }
				}
				""",
			"lib/Sub.java",
			"package lib;\n" +
			"public class Sub extends Base<Sub> {}",
			"lib/Base.java",
			"""
				package lib;
				class Base<T> {
				    public T m() {
				        System.out.println("m");
				        return thisInstance();
				    }
				    @SuppressWarnings("unchecked")
				    T thisInstance() {
				        return (T) this;
				    }
				}"""
	},
	"Done");
}
public void testBug502871() {
	runNegativeTest(
		new String[] {
			"test/GryoMapper.java",
			"package test;\n" +
			"\n" +
			"interface Generic<A> {\n" +
			"}\n" +
			"\n" +
			"interface SAM<B> {\n" +
			"	B m();\n" +
			"}\n" +
			"\n" +
			"public final class GryoMapper {\n" +
			"	public void addCustom(Generic c) {\n" +
			"		addOrOverrideRegistration(() -> GryoTypeReg.of(c));\n" +
			"	}\n" +
			"\n" +
			"	private <C> void addOrOverrideRegistration(SAM<GryoTypeReg<C>> newRegistrationBuilder) {\n" +
			"	}\n" +
			"\n" +
			"	static class GryoTypeReg<D> {\n" +
			"		static <E> GryoTypeReg<E> of(Generic<E> clazz) {\n" +
			"			return null;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. WARNING in test\\GryoMapper.java (at line 11)
				public void addCustom(Generic c) {
				                      ^^^^^^^
			Generic is a raw type. References to generic type Generic<A> should be parameterized
			----------
			2. WARNING in test\\GryoMapper.java (at line 12)
				addOrOverrideRegistration(() -> GryoTypeReg.of(c));
				                                ^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation of(Generic) of the generic method of(Generic<E>) of type GryoMapper.GryoTypeReg
			----------
			3. WARNING in test\\GryoMapper.java (at line 12)
				addOrOverrideRegistration(() -> GryoTypeReg.of(c));
				                                ^^^^^^^^^^^^^^^^^
			Type safety: The expression of type GryoMapper.GryoTypeReg needs unchecked conversion to conform to GryoMapper.GryoTypeReg<Object>
			----------
			4. WARNING in test\\GryoMapper.java (at line 12)
				addOrOverrideRegistration(() -> GryoTypeReg.of(c));
				                                               ^
			Type safety: The expression of type Generic needs unchecked conversion to conform to Generic<Object>
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=490469 Internal compiler error: java.lang.NullPointerException at org.eclipse.jdt.internal.compiler.ast.LambdaExpression.analyseCode(LambdaExpression.java:512)
public void testBUg490469() {
	this.runConformTest(
		new String[] {
			"AbstractClientProxy.java",
			"""
				import java.util.function.Supplier;
				public abstract class AbstractClientProxy {
					protected <T> T getRemoteObject(String url, Class<T> responseType, Object... urlVariables) {
						return handleException(this.bindGet(REST::getForObject, url, responseType, urlVariables));
					}
					private <T> T handleException(Supplier<T> s){
						T t = null;
						try{
							t= s.get();
						}catch(Exception e){
						}
						return t;
					}
					private  <T> Supplier<T> bindGet(GetFunc fn, String url, Class<T> responseType, Object... uriVariables) {
						return () -> fn.invoke(url, responseType, uriVariables);
					}
				}
				class REST {
					static <T> T getForObject(String url, Class<T> respType, Object... vars) {
						return null;
					}
				}
				interface GetFunc {
					<T> T invoke(String url, Class<T> responseType, Object... uriVariables);
				}
				"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=509804 Incorrect Enclosing Method Attribute generated for anonymous class in lambda after method reference
public void test509804() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"""
				import java.lang.reflect.Method;
				import java.util.function.Supplier;
				public enum Test {
					A(Object::new),
					B(() -> new Object(){}),
					;
					private final Supplier<Object> s;
					Test(Supplier<Object> e){
						this.s = e;
					}
					public static void main(String[] args) throws NoSuchMethodException, SecurityException {
						System.out.println(B.s.get().getClass().getEnclosingMethod());
					}
				}
				"""
		};
	runner.expectedOutputString =
			"null";
	runner.expectedJavacOutputString =
			"null";
	runner.runConformTest();
}
public void testBug514105() {
	runConformTest(
		new String[] {
			"FunctionalInterfaceBug.java",
			"""
				import java.util.function.Function;
				import java.util.function.UnaryOperator;
				
				@FunctionalInterface
				interface BaseFunction<S, T> extends Function<S, T> {
				
				    T foo(final S s);
				
				    default T apply(final S s) {
				        return null;
				    }
				}
				
				@FunctionalInterface
				interface SubFunction<T> extends UnaryOperator<T>, BaseFunction<T, T> {
				}
				public class FunctionalInterfaceBug {}
				"""
		});
}
public void testBug515473() {
	runConformTest(
		new String[] {
			"test/LambdaResourceLeak.java",
			"package test;\n" +
			"\n" +
			"class X implements AutoCloseable {\n" +
			"	@Override\n" +
			"	public void close() {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"interface SAM {\n" +
			"	Object m();\n" +
			"}\n" +
			"\n" +
			"public class LambdaResourceLeak {\n" +
			"	void f() {\n" +
			"		X x1 = new X();\n" +
			"		SAM sam = () -> {\n" +
			"			return \"\";\n" +
			"		};\n" +
			"		sam.m();\n" +
			"		x1.close();\n" +
			"	}\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug517299() {
	runConformTest(
		new String[] {
			"example/FooTest.java",
			"""
				package example;
				import java.util.function.Consumer;
				import foo.Foo;
				public class FooTest {
					public void test() {
						Foo.Bar foo = new Foo.Bar();
						foo.print("direct");
						invoke(foo::print, "methodReference");
					}
					private static void invoke(Consumer<String> method, String name) {
						method.accept(name);
					}
					public static void main(String[] args) {
						new FooTest().test();
					}
				}""",
			"foo/Foo.java",
			"""
				package foo;
				public abstract class Foo {
					public static class Bar extends Baz {}
					static class Baz {
						public final void print(String name) {
							System.out.println("Baz.print called - "+name);
						}
					}
				}"""
		},
		"Baz.print called - direct\n" +
		"Baz.print called - methodReference"
	);
}
public void testBug521808() {
	runConformTest(
		new String[] {
			"Z.java",
			"""
				interface FI1 {
					Object m(Integer... s);
				}
				interface FI2<T> {
					Object m(T... arg);
				}
				public class Z {
					static Object m(FI1 fi, Integer v1, Integer v2) {
						return fi.m(v1, v2);
					}
					static <V extends Integer> Object m(FI2<V> fi, V v1, V v2) {
						return null;
					}
					public static void main(String argv[]) {
						Object obj = m((FI1) (Integer... is) -> is[0] + is[1], 3, 4);
						obj = m((Integer... is) -> is[0] + is[1], 3, 4); // Javac compiles, ECJ won't
					}
				}""",
		}
	);
}
public void testBug522469() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<R> {
				
					public static void main(String[] args) {
						I<?> i = (X<?> x) -> "";
					}
				}
				interface I<T> {
					String m(X<? extends T> x);
				}
				"""
		}
	);
}
public void testBug517951() {
	runConformTest(
		new String[] {
			"Minimal.java",
			"""
				public class Minimal {
				    public void iCrash() {
				        try {
				            System.out.println("Body can't be empty");
				        } finally {
				            consumes(sneaky()::withVarargs);
				        }
				    }
				    public static void main(String[] args) {
						new Minimal().iCrash();
					}
				    private Minimal sneaky() { return this; }
				    private void withVarargs(String... test) {}
				    private void consumes(Runnable r) {}
				}"""
		},
		"Body can't be empty"
	);
}
public void testBug517951a() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"""
				import java.nio.file.Files;
				import java.nio.file.Paths;
				import java.util.function.Consumer;
				public class Snippet {
					void postError( String fmt, Object ... args ) {
					}
					public boolean test(Consumer<String> postError ) {
						return false;
					}
					void func() {
						try( java.io.InputStream istr = Files.newInputStream( Paths.get( "" ) )){
						} catch( Exception e ) {
						} finally {
							test( this::postError);
						}
					}
				}"""
		}
	);
}
public void testBug517951b() {
	runConformTest(
		new String[] {
			"Element.java",
			"""
				public class Element
				{
				    Operation operation = new Operation(Element::new);
				    public Element(Integer matrix)
				    {
				    		//...
				    }
				    public Element(Operation... operation)
				    {
				    		//...
				    }
				}
				class Operation
				{
				    public Operation(Factory factory)
				    {
				        //...
				    }
				}
				interface Factory
				{
				    Element create(Operation... operations);
				}"""
		}
	);
}
public void testBug517951c() {
	runConformTest(
		new String[] {
			"npetest/NpeTest.java",
			"""
				package npetest;
				public class NpeTest {
				    public NpeTestScheduler scheduler;
				    private void doIt(Object... params) {
				        try {
				            System.out.println("Done");
				        }
				        finally {
				            scheduler.schedule(this::doIt);
				        }
				    }
				}""",
			"npetest/NpeTestIf.java",
			"""
				package npetest;
				@FunctionalInterface
				public interface NpeTestIf {
				    void doSomething(Object... params);
				}""",
			"npetest/NpeTestScheduler.java",
			"""
				package npetest;
				public class NpeTestScheduler {
				    public void schedule(NpeTestIf what) {
				        what.doSomething();
				    }
				}"""
		}
	);
}
public void testBug521818() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"test/Main.java",
			"""
				package test;
				class C {}
				class D {
					<T extends C & Runnable> D(int i, T t) {\
						System.out.println("D");
				}
				}
				interface Goo {
				    <T extends C & Runnable> String m(T p);
				}
				class A {
				    public static <K extends Runnable> String bar(K a) {
						System.out.println("Bar");
				       return null;
				    }
				    public static <K extends Runnable> D baz(int i, K a) {
						System.out.println("Baz");
				       return null;
				    }
				}
				interface Foo<Z extends C & Runnable> {
					D get(int i, Z z);
				}
				public class Main  {
				    public static void main(String[] args) {
				    	Foo<? extends C> h = A::baz;
				    	h.get(0,  null);
				    	Foo<? extends C> h2 = D::new;
				    	h2.get(0,  null);
				    	Goo g = A::bar;
				    	g.m(null);
				    }\s
				}"""
		};
	runner.expectedOutputString =
		"""
			Baz
			D
			Bar""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacGeneratesIncorrectCode; // similar to fixed https://bugs.openjdk.java.net/browse/JDK-8058112
	runner.runConformTest();
}
public void testBug522469a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X<R> {
					public static void main(String[] args) {
						I<?> i = (X<?> x) -> "";
						J<?, Y> j = (C<?, Y> y) -> "";
						J<?, Y> j2 = (C<? extends Object, Y> y) -> "";
						J<? extends Object, Y> j3 = (C<?, Y> y) -> "";
						J<? extends Object, Y> j4 = (C<? extends Object, Y> y) -> "";
						J<?, Y> j5 = (C<?, Z> y) -> "";
						K<? extends List<?>> k = (D<? extends List<?>> d) -> "";
					}
				}
				class C<T, U> {}
				class D<T extends List<T>> {}
				class Z {}
				class Y extends Z {}
				interface I<T> {
					String m(X<? extends T> x);
				}
				interface J<R, S extends Z> {
					String m(C<? extends R, S> ya);
				}
				interface K<R extends List<R>> {
					String m(D<? extends R> d);
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				J<?, Y> j5 = (C<?, Z> y) -> "";
				             ^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from J<Object,Z> to J<?,Y>
			----------
			2. ERROR in X.java (at line 10)
				K<? extends List<?>> k = (D<? extends List<?>> d) -> "";
				                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
			----------
			""");
}
public void testBug522469b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class C<T> {}
				public class X  {
				    interface I<T> {
				        void foo(C<? super Long> l);
				    }
				    public static void run() {
				        I<String> i = (C<? super Number> l) -> {};
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				I<String> i = (C<? super Number> l) -> {};
				               ^
			Lambda expression's parameter l is expected to be of type C<? super Long>
			----------
			""");
}
public void testBug529199() {
	runConformTest(
		new String[] {
			"p2/Test.java",
			"""
				package p2;
				public class Test {
				   public static void main(String... args) {
				       p1.B.m(); // ok
				       Runnable r = p1.B::m; r.run(); // runtime error
				   }
				}""",
			"p1/A.java",
			"""
				package p1;
				class A {
				   public static void m() { System.out.println("A.m"); }
				}
				""",
			"p1/B.java",
			"""
				package p1;
				public class B extends A {
				}
				"""
		},
		"A.m\n" +
		"A.m"
	);
}
public void testBug553885a() {
	runConformTest(
			new String[] {
					"p2/Test.java",
						"""
							package p2;
							import java.util.Optional;
							import p1.B;
							import p1.BImpl;
							
							public class Test {
							    public static void main(String[] args) {
							        Optional<Integer> map = Optional.of(new BImpl()).map(B::amount);
							        System.out.print(map);
							    }
							}""",
					"p1/A.java",
							"""
								package p1;
								interface A {
								    default int amount() {
								        return 0;
								    }
								}
								""",
					"p1/B.java",
							"""
								package p1;
								public interface B extends A {}
								
								""",
					"p1/BImpl.java",
							"package p1;\n" +
							"public class BImpl implements B {}\n",
			},
			"Optional[0]"
			);
}
public void testBug553885b() {
	runConformTest(
			new String[] {
					"p2/Test.java",
						"""
							package p2;
							import java.util.Optional;
							import p1.B;
							import p1.BImpl;
							
							public class Test {
							    public static void main(String[] args) {
							        B b = new BImpl();
							        Optional<Integer> map = Optional.of(b).map(B::amount);
							        System.out.print(map);
							    }
							}""",
					"p1/A.java",
							"""
								package p1;
								interface A {
								    default int amount() {
								        return 0;
								    }
								}
								""",
					"p1/B.java",
							"""
								package p1;
								public interface B extends A {}
								
								""",
					"p1/BImpl.java",
							"package p1;\n" +
							"public class BImpl implements B {}\n",
			},
			"Optional[0]"
			);
}
public void testBug553885c() {
	// classes instead of interface with default method
	runConformTest(
			new String[] {
					"p2/Test.java",
						"""
							package p2;
							import java.util.Optional;
							import p1.B;
							import p1.BImpl;
							
							public class Test {
							    public static void main(String[] args) {
							        B b = new BImpl();
							        Optional<Integer> map = Optional.of(b).map(B::amount);
							        System.out.print(map);
							    }
							}""",
					"p1/A.java",
							"""
								package p1;
								class A {
								    public int amount() {
								        return 0;
								    }
								}
								""",
					"p1/B.java",
							"""
								package p1;
								public class B extends A {}
								
								""",
					"p1/BImpl.java",
							"package p1;\n" +
							"public class BImpl extends B {}\n",
			},
			"Optional[0]"
			);
}

public void testBug521182() {
	runConformTest(
		new String[] {
			"MethodRef.java",
			"""
				import java.util.function.Supplier;
				public class MethodRef {
				  public static void m(Supplier<?> s) {
				  }
				  public static void main(String[] args) {
				    Object ref = null;
					 try {
				    	m(ref::toString);
					    System.out.println("A NPE should have been thrown !!!!!");
					 } catch (NullPointerException e) {
						System.out.println("Success");
					 }
				  }
				}"""
		},
		"Success");
}
public void testBug521182a() {
	runConformTest(
		new String[] {
			"MethodRef.java",
			"""
				import java.util.function.Supplier;
				public class MethodRef {
					Object field = null;
				  public static void m(Supplier<?> s) {
				  }
				  public static void main(String[] args) {
					 try {
						MethodRef ref = new MethodRef();
				    	m(ref.field::toString);
					    System.out.println("A NPE should have been thrown !!!!!");
					 } catch (NullPointerException e) {
						System.out.println("Success");
					 }
				  }
				}"""
		},
		"Success");
}
public void testBug521182b() {
	runConformTest(
		new String[] {
			"MethodRef.java",
			"""
				import java.util.function.Supplier;
				public class MethodRef {
				  public static void m(Supplier<?> s) {
				  }
				  public static Object get() {
					 return null;
				  }
				  public static void main(String[] args) {
					 try {
				    	m(get()::toString);
					    System.out.println("A NPE should have been thrown !!!!!");
					 } catch (NullPointerException e) {
						System.out.println("Success");
					 }
				  }
				}"""
		},
		"Success");
}
public void testBug516833() {
	Map options = new HashMap<>(2);
	options.put(CompilerOptions.OPTION_MethodParametersAttribute, "generate");
	this.runConformTest(
		new String[] {
			"ParameterTest.java",
			"""
				import java.lang.reflect.Method;
				import java.lang.reflect.Parameter;
				import java.util.Arrays;
				import java.util.List;
				public class ParameterTest {
					void foo(String s, List<String> s1) {
						s1.stream().filter(p -> p.equals(s));
					}
					public static void main(String[] args) {
						for (Method m : ParameterTest.class.getDeclaredMethods()) {
							if (m.getName().contains("lambda")) {
								Parameter[] params = m.getParameters();
								System.out.println(Arrays.asList(params));
							}
						\t
						}
					}
				}
				"""
		},
		"[java.lang.String arg0, java.lang.String arg1]", options);
}
public void testBug531093comment1() {
	runConformTest(
		new String[] {
			"bug/Bug.java",
			"package bug;\n" +
			"\n" +
			"import java.lang.reflect.Method;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Optional;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Bug {\n" +
			"	public static <E extends Number> Function<E, Optional<String>> useMethodRef() {\n" +
			"		return Bug::getMapper;\n" +
			"	}\n" +
			"\n" +
			"	private static Optional<String> getMapper(Number event) {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Method[] methods = Bug.class.getDeclaredMethods();\n" +
			"		for (Method method : methods) {\n" +
		"				Arrays.asList(method.getParameters()).toString();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		""
	);
}
public void testBug531093() {
	runConformTest(
		new String[] {
			"bug/Bug.java",
			"package bug;\n" +
			"import java.io.Serializable;\n" +
			"import java.lang.reflect.Method;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Optional;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Bug {\n" +
			"    public <E extends Number & Serializable> Function<E, Optional<String>> useMethodRef() {\n" +
			"        return Bug::getMapper;\n" +
			"    }\n" +
			"\n" +
			"    private static Optional<String> getMapper(Number event) {\n" +
			"        return null;\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        Method[] methods = Bug.class.getDeclaredMethods();\n" +
			"        for (Method method : methods) {\n" +
			"            String.valueOf(Arrays.asList(method.getParameters()));\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		""
	);
}
public void testBug540520() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"Run.java",
			"""
				import java.util.ArrayList;
				import java.util.HashMap;
				import java.util.List;
				import java.util.Map;
				import java.util.stream.Collectors;
				
				public class Run {
				
					public static void main(String[] args) {
					\t
						List<TypeDeptCount> list = new ArrayList<>();
						for(int i=0;i<10;i++) {
							TypeDeptCount ty = new TypeDeptCount();
							ty.setCykbbm("10"+i);
							ty.setCount(i);
						}
				        List<Map<String, Object>> datas = list.stream().collect(Collectors.groupingBy(TypeDeptCount::getType))
				                .entrySet().stream().map(item -> item.getValue().stream().reduce(new HashMap<String, Object>() {
				                    private static final long serialVersionUID = 1L;
				                    {
				                        put("count", 0);
				                        put("type", item.getKey());
				                    }
				                }, (data1, val) -> {
				                    data1.put(val.getCykbbm(), val.getCount());
				                    data1.put("count", (Integer) data1.get("count") + val.getCount());
				                    return data1;
				                }, (data1, data2) -> {
				                    data2.put("count", (Integer) data1.get("count") + (Integer) data2.get("count"));
				                    data1.putAll(data2);
				                    return data1;
				                })).sorted((item1, item2) -> (Integer) item2.get("count") - (Integer) item1.get("count"))
				                .collect(Collectors.toList());
				        System.out.println(datas);
					}
				}
				""",
			"TypeDeptCount.java",
			"""
				public class TypeDeptCount {
				
				    private String type;
				    private String cykbbm;
				    private Integer count;
				   \s
					public String getType() {
						return type;
					}
					public void setType(String type) {
						this.type = type;
					}
					public String getCykbbm() {
						return cykbbm;
					}
					public void setCykbbm(String cykbbm) {
						this.cykbbm = cykbbm;
					}
					public Integer getCount() {
						return count;
					}
					public void setCount(Integer count) {
						this.count = count;
					}
				}
				"""
		};
	runner.runConformTest();
}
public void testBug540631() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"EclipseCompileBug.java",
			"""
				public enum EclipseCompileBug {
					/*
					 * Next line fails with these errors in Eclipse, works with javac:
					 * <li>Test cannot be resolved to a type
					 * <li>Cannot reference a field before it is defined
					 */
					Test(Test::new);
				
					@FunctionalInterface
				    public interface IConstructor<T extends Object> {
				        T apply();
				    }
				
				    private final IConstructor<?> constructor;
					private EclipseCompileBug (IConstructor<?> newObj) {
						constructor = newObj;
					}
				\t
					public static class Test {
					\t
					}
				}
				"""
		};
	runner.runConformTest();
}
public void testBug562324() {
	if (this.complianceLevel < ClassFileConstants.JDK11)
		return; // uses 'var'
	runConformTest(
		new String[] {
			"X.java",
			"""
				
				import java.util.Arrays;
				import java.util.List;
				import java.util.Set;
				import java.util.function.Function;
				import java.util.stream.Collector;
				
				public class X  {
				
					public static void main(String[] args) {
						try {
							List<String> taskNames = Arrays.asList("First", "Second", "Third");
						\t
							// To avoid the ClassFormatError at run-time, declare this variable with type 'Set<Y>'
							var services = taskNames.stream().collect(X.toSet(name -> new Y(){}));
				
							String[] names = services.stream().map(e -> e).toArray(String[] :: new);
						} catch (RuntimeException re) {
							System.out.print(re.getMessage());
						}
					}
				    public static <T, U>
				    Collector<T, ?, Set<U>> toSet(Function<? super T, ? extends U> valueMapper) {
				    	throw new RuntimeException("it runs");
				    }
				
				}
				
				abstract class Y{}
				"""
		},
		"it runs");
}
public void testBug562324b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				
				import java.util.Arrays;
				import java.util.List;
				import java.util.Set;
				import java.util.function.Function;
				import java.util.stream.Collector;
				
				public class X  {
				
					public static void main(String[] args) {
						try {
							List<String> taskNames = Arrays.asList("First", "Second", "Third");
						\t
							// To avoid the ClassFormatError at run-time, declare this variable with type 'Set<Y>'
							;
				
							String[] names = taskNames.stream().collect(X.toSet(name -> new Y(){}))
												.stream().map(e -> e).toArray(String[] :: new);
						} catch (RuntimeException re) {
							System.out.print(re.getMessage());
						}
					}
				    public static <T, U>
				    Collector<T, ?, Set<U>> toSet(Function<? super T, ? extends U> valueMapper) {
				    	throw new RuntimeException("it runs");
				    }
				
				}
				
				abstract class Y{}
				"""
		},
		"it runs");
}
public void testBug576152() {
	runConformTest(
			new String[] {
					"Example.java",
					"""
						
						import java.util.function.Supplier;
						
						public class Example {
						    Example() {
						        inspect(this::singleMethod);
						        inspect(this::overloadedMethod);
						    }
						    void inspect(Inspector... inspectors) { }
						    void singleMethod(byte[] input, int offset, int len) { }
						    void overloadedMethod() { }
						    void overloadedMethod(byte[] input, int offset, int len) { }
						
						    public static void main(String[] args) {
						    	String s1 = hoge1(String::new);
						    	String s2 = hoge2(String::new);	// Error. See the attached file.
						    }
						    static <T> T hoge1(Supplier<T> sup) {
						    	return null;
						    }
						    static <T> T hoge2(Supplier<T>... sup) {
						    	return null;
						    }
						}
						
						@FunctionalInterface
						interface Inspector {
						    void update(byte[] input, int offset, int len);
						}"""
			}
			);
}
public void testBug529197_001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    int var = 0;
					
					    public static void main(String[] args) {
					       X x = new X();
					       x.new Inner();
					    }
					
					    public class Inner {
					        public Inner(Runnable r) {
					        	System.out.println("SUCCESS");\s
					        }
					        public Inner() {
					            this(() -> {
					                var = 1;
					            });
					        }
					    }
					}
					""",
			},
			"SUCCESS"
			);
}
public void testBug529197_002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    int var = 0;
					
					    public static void main(String[] args) {
					       X x = new X();
					       x.new Inner();
					    }
					
					    public class Inner {
					        public Inner(Runnable r) {
					        	System.out.println("SUCCESS");\s
					        }
					        public Inner() {
					            this(() -> {
					                var = 1;
					            });
					        }
					    }
					}
					""",
			},
			"SUCCESS"
			);
}
public void testBug529197_003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					class Struct { String component; }
					public class X {
					    Struct struct = new Struct();
					
					    public static void main(String[] args) {
					       X x = new X();
					       x.new Inner();
					       System.out.println(x.struct.component);
					    }
					
					    public class Inner {
					        public Inner(Runnable r) {
					        	r.run();
					        	System.out.print("SUCCESS");\s
					        }
					        public Inner() {
					            this(() -> {
					                struct.component = ".run";
					            });
					        }
					    }
					}
					""",
			},
			"SUCCESS.run"
			);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/756
public void testIssue756() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.reflect.GenericSignatureFormatError;
					import java.lang.reflect.Method;
					import java.util.function.Supplier;
					
					public class X<T> {
					    public X(T... elements) {
					    	System.out.println();
					    }
					
					    public static Supplier<? extends X<Object>> supplier() {
					        return X<Object>::new;
					    }
					
					    public static void main(String[] args) {
							boolean OK = true;
					    	for (Method m : X.class.getDeclaredMethods()) {
								try {
									m.getGenericReturnType();
								} catch (GenericSignatureFormatError e) {
									System.out.println("Oops, bad signature in class file");
									OK = false;
								}
							}
					    	if (OK)
					    		System.out.println("All clear!");
						}
					}
					"""
			},
			"All clear!"
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577466
public void test577466() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.util.function.Function.identity;
					import java.util.Map;
					import java.util.concurrent.ConcurrentHashMap;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;
					
					public class X {
					
					    private static final Map<Class<?>, Map<String, I>> LOOKUP = new ConcurrentHashMap<>();
					
					    @SuppressWarnings("unchecked")
					    static <E extends Enum<E> & I> E lookupLiteral(Class<E> enumType, String literal) {
					        return (E) LOOKUP.computeIfAbsent(enumType, t ->
					            Stream.of(enumType.getEnumConstants()).collect(Collectors.<E, String, I>toMap(E::getLiteral, identity()))
					        ).get(literal);
					    }
					\s
					    public static void main(String[] args) {
					        System.out.println(X.lookupLiteral(EN.class, "a"));
					    }
					
					    interface I {
					        String getLiteral();
					    }
					
					    enum EN implements I {\s
					        A("a");
					
					        final String literal;
					
					        EN(String literal) {
					            this.literal = literal;
					        }
					
					        @Override
					        public String getLiteral() {
					            return literal;
					        }
					    }
					}
					"""

			},
			"A"
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=570511#c2
public void test570511_comment2() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Supplier;
					
					  public class X {
					
					    public static void main(String[] args) {
					        System.out.println(getValue(Size.S));
					    }
					
					    enum Size implements Supplier<String> {
					        S, M, L;
					
					        @Override
					        public String get() {
					            return "Size: " + toString();
					        }
					    }
					
					    public static <V extends Enum<?> & Supplier<String>> String getValue(V t) {
					        return valueOf(t::get);
					    }
					
					    public static String valueOf(Supplier<String> id) {
					        return id.get();
					    }
					  }
					"""
			},
			"Size: S"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=483219
public void test483219_comment_0() {
	this.runConformTest(
			new String[] {
				"Intersection.java",
				"""
					public class Intersection {
					  interface I {
					  }
					  interface J {
					    void foo();
					  }
					
					  static <T extends I & J> void bar(T t) {
					      Runnable r = t::foo;
					  }\s
					 \s
					  public static void main(String[] args) {
					    class A implements I, J { public void foo() {} }
					    bar(new A());
					  }
					}
					"""

			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=483219
public void test483219_comment_1() {
	this.runConformTest(
			new String[] {
				"Main.java",
				"""
					import java.util.Arrays;
					import java.util.Comparator;
					import java.util.List;
					
					import static java.util.stream.Collectors.toList;
					
					public class Main {
					
					
					    public static void main(String[] args) {
					        Main main = new Main();
					        main.toInfoListError(Arrays.asList(new Base()));
					    }
					
					    public <H extends B & A> List<Info> toInfoListError(List<H> list) {
					        Comparator<B> byNameComparator = (B b1, B b2) -> b1.getB().compareToIgnoreCase(b2.getB());
					        return list.stream().sorted(byNameComparator).map(Info::new).collect(toList());
					    }
					
					    public <H extends B & A> List<Info> toInfoListWorks(List<H> list) {
					        Comparator<B> byNameComparator = (B b1, B b2) -> b1.getB().compareToIgnoreCase(b2.getB());
					        return list.stream().sorted(byNameComparator).map(s -> new Info(s)).collect(toList());
					    }
					}
					
					interface B {
					    public String getB();
					}
					
					interface A {
					    public long getA();
					}
					
					class Info {
					
					    private final long a;
					    private final String b;
					
					    <H extends A & B> Info(H h) {
					        a = h.getA();
					        b = h.getB();
					    }
					}
					
					class Base implements A, B {
					
					    @Override
					    public long getA() {
					        return 7L;
					    }
					
					    @Override
					    public String getB() {
					        return "hello";
					    }
					}
					"""
			},
			""
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=483219
public void test483219_comment_2a() {
	this.runConformTest(
			new String[] {
				"MethodHandleTest.java",
				"""
					import java.awt.Component;
					import java.util.Collection;
					import java.util.Collections;
					
					public class MethodHandleTest {
					    public static void main(String... args) {
					        new MethodHandleTest().run();
					    }
					
					    private void run() {
					        ComponentWithSomeMethod myComp = new ComponentWithSomeMethod();
					        new Caller<ComponentWithSomeMethod>().callSomeMethod(Collections.singletonList(myComp));
					    }
					
					    private interface HasSomeMethod {
					        void someMethod();
					    }
					
					    static class ComponentWithSomeMethod extends Component implements HasSomeMethod {
					        @Override
					        public void someMethod() {
					            System.out.println("Some method");
					        }
					    }
					
					    class Caller<T extends Component & HasSomeMethod> {
					        public void callSomeMethod(Collection<T> components) {
					            components.forEach(HasSomeMethod::someMethod); //  <-- crashes
					//          components.forEach(comp -> comp.someMethod());     <-- works fine
					
					        }
					    }
					}
					"""

			},
			"Some method"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=483219
public void test483219_comment_2b() {
	this.runConformTest(
			new String[] {
				"Foo.java",
				"""
					import java.util.stream.Stream;
					public final class Foo
					{
					    private interface X
					    {
					        default void x()
					        {
					        }
					    }
					
					    private enum E1
					        implements X
					    {
					        INSTANCE,
					        ;
					    }
					
					    private enum E2
					        implements X
					    {
					        INSTANCE,
					        ;
					    }
					
					    public static void main(final String... args)
					    {
					        Stream.of(E1.INSTANCE, E2.INSTANCE).forEach(X::x);
					    }
					}
					"""
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=483219
public void test483219_comment_3() {
	this.runConformTest(
			new String[] {
				"SortedInterfacesTest.java",
				"""
					import java.util.function.Consumer;
					public class SortedInterfacesTest {
					   <T> void forAll(Consumer<T> consumer, T... values) { }
					
					   public void secondTest() {
					       forAll(Picture::draw, new MyPicture(), new Universal());
					   }
					
					   interface Shape { void draw(); }
					   interface Marker { }
					   interface Picture { void draw(); }
					
					   class MyShape implements Marker, Shape { public void draw() { } }
					   class MyPicture implements Marker, Picture { public void draw() { } }
					   class Universal implements Marker, Picture, Shape { public void draw() { } }
					
					   public static void main(String[] args) {
					       new SortedInterfacesTest().secondTest();
					   }
					}
					"""
			},
			""
			);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1048
// Reflection APIs fail with NPE processing class file produced by JDT compiler
public void testGHIssue1048() {
	this.runConformTest(
			new String[] {
				"Reproducer.java",
				"""
					import java.io.IOException;
					import java.io.Serializable;
					import java.lang.reflect.Method;
					import java.util.Optional;
					import java.util.stream.Stream;
					public class Reproducer {
					    public static void main(String[] args) {
					        try {
					            new Reproducer().testClassWithStreamAndOptional2();
					        } catch (IOException e) {
					            e.printStackTrace();
					        }
					    }
					    private void testClassWithStreamAndOptional2() throws IOException {
					        Class<?> c = this.getClass();
					        for (Method m : c.getDeclaredMethods()) {
					            if (m.isSynthetic())
					                System.out.println(m.getGenericReturnType());
					        }
					    }
					    private Stream<Serializable> doMyStuff() {
					        Stream<Serializable> s = Stream.empty();\s
					        return Optional.ofNullable(s).orElseGet(Stream::of);
					    }
					}
					"""
			},
			"interface java.util.stream.Stream"
			);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1054
// AIOOBE when checking implicit lambda generation requirement
public void testGHIssue1054() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					interface FI {
					    Object invokeMethodReference(String s, char c1, char c2) throws Throwable;
					}
					public class X {
					    private static MethodHandle createMethodHandle() {
					    	return null;
					    }
					    public static void run() throws Throwable {
					        MethodHandle ms = createMethodHandle();\s
					        FI fi = ms::invoke;
					        fi.invokeMethodReference("", (char)0, (char)0);
					    }
					    public static void main(String [] args) throws Throwable {
					        try {\s
					            run();
					        } catch(NullPointerException npe) {
					            System.out.println("NPE as expected");
					        }
					    }
					}
					"""},
			"NPE as expected"
			);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1054
// AIOOBE when checking implicit lambda generation requirement
public void testGHIssue1054_2() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.MethodHandle;
					interface FI {
					    <T extends Object & Runnable> Object invokeMethodReference(Object o, T t2) throws Throwable;
					}
					public class X {
					    private static MethodHandle createMethodHandle() {
					    	return null;
					    }
					    public static void run() throws Throwable {
					        MethodHandle ms = createMethodHandle();\s
					        FI fi = ms::invoke;
					        fi.invokeMethodReference(null, ()-> {});\s
					    }
					    public static void main(String[] args) throws Throwable {
					    	try {
					    		run();
					    	} catch (NullPointerException npe) {
					    		System.out.println("NPE as expected");
					    	}
						}
					}
					"""},
			"NPE as expected"
			);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/975
// [Compiler] Reflection returns null for lambda capturing local method type variable
public void testGHIssue975() {
	this.runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.lang.reflect.*;
					
					public class Test {
					    static class Capturing<T> {
					        protected Capturing() {
					            ParameterizedType paramT = (ParameterizedType) getClass().getGenericSuperclass();
					            Type t = paramT.getActualTypeArguments()[0];
					
					            if (t instanceof TypeVariable) {
					                System.out.println("Found expected type");
					            } else {
					                throw new AssertionError("Unexpected type: " + t);
					            }
					        }
					    }
					
					    static void run(Runnable r) {
					        r.run();
					    }
					
					    public static <T> void main(String... args) {
					        class Local {
					            <M> void runTest() {
					                run(() -> new Capturing<M>() {});
					            }
					        }
					
					        new Local().runTest();
					    }
					}
					"""},
			"Found expected type"
			);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/625
public void testGHIssue625() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.Optional;
					
					public class X {
						 public static void main(String[] argv) {
						        System.out.println(dummy("foo"));
						    }
					\t
						    static <T extends Serializable & CharSequence> int dummy(T value) {
						        return Optional.ofNullable(value).map(CharSequence::length).orElse(0);
						    }
					}
					"""},
			"3"
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=551882
// Invalid receiver type class X; not a subtype of implementation type interface Y
public void testBug551882() {
	this.runConformTest(
			new String[] {
				"Foo.java",
				"""
					class X {
					}
					
					interface Y {
						void foo();
					}
					
					class Z extends X implements Y {
					
						@Override
						public void foo() {
						}
					\t
					}
					public class Foo {
					
						<T extends X & Y> void run(T t) {
							Runnable r = t::foo;
							r.run();
						}
					\t
						public static void main(String[] args) {
							new Foo().run(new Z() {});
						}
					
					}
					"""},
			""
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=511958
// [1.8][compiler] Discrepancy with javac behavior when handling inner classes and lambdas
public void testBug511958() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Consumer;
					@SuppressWarnings("all")
					public class X {
					  private final String text = "Bug?";
					  public static void main(String[] args) {
					    new X().doIt();
					  }
					 \s
					  private void doIt() {
					    new Sub();
					  }
					  private class Super<T> {
						  public Super(String s) {}
					    public Super(Consumer<T> consumer) {
					    }
					  }
					  private class Sub extends Super<String> {
					    public Sub() {
					      super(s -> System.out.println(text)); \s
					    }
					   \s
					  }
					}
					"""},
			""
			);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1060
// NPE when inspecting scrapbook expression that uses Java 8 features
public void testGH1060() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println(new X().foo());
						}
					\t
						String foo() {
							return java.time.format.DateTimeFormatter
									.ofPattern("yyyyMMddHHmmss.SSS000")
									.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HHmmss").parse("30.03.2021 112430", java.time.LocalDateTime::from));
						}
					}
					"""},
			"20210330112430.000000"
			);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=546161
//LambdaConversionException due to invalid instantiated method type argument to LambdaMetafactory::metafactory
public void testBug546161() {
	this.runConformTest(
			new String[] {
				"Main.java",
				"""
					public class Main {
					    public static void main(String[] args) {
					    	((Widget<?>) new Widget<>()).addListener(evt -> {});
					    }
					}
					
					class Widget<E extends CharSequence> {
					    void addListener(Listener<? super E> listener) {}
					}
					
					interface Listener<E extends CharSequence> {
					    void m(E event);
					}
					"""},
			""
			);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=546161
//LambdaConversionException due to invalid instantiated method type argument to LambdaMetafactory::metafactory
public void testBug546161_2() {
	this.runConformTest(
			new String[] {
				"Main.java",
				"""
					public class Main {
					    public static void main(String[] args) {
					    	new Widget<>().addListener(evt -> {});
					    }
					}
					class Widget<E extends CharSequence> {
					    void addListener(Listener<? super E> listener) {}
					}
					interface Listener<E extends CharSequence> {
					    void m(E event);
					}
					"""},
			""
			);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=574269
//java.lang.invoke.LambdaConversionException: Invalid receiver type class java.lang.Object
public void testBug574269() {
	this.runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.util.List;
					
					public class Test {
					
						public static void main(String[] args) {
							System.out.println("Testing with explicit type");
							MyGroup group = new MyGroup();
					
							System.out.println("  Testing lambda");
							if (group.getPersons().stream().anyMatch(p -> p.isFoo())) {}
							System.out.println("  Lambda is good");
						\t
							System.out.println("  Testing method reference");
							if (group.getPersons().stream().anyMatch(Test.Person::isFoo)) {}
							System.out.println("  Method reference is good");
					
							System.out.println("Testing with wildcard");
							Group<?> group2 = new MyGroup();
					
							System.out.println("  Testing lambda");
							if (group2.getPersons().stream().anyMatch(p -> p.isFoo())) {}
							System.out.println("  Lambda is good");
						\t
							System.out.println("  Testing method reference");
							if (group2.getPersons().stream().anyMatch(Test.Person::isFoo)) {}
							System.out.println("  Method reference is good");
						}
					
						interface Group<T extends Person> {
							List<T> getPersons();
						}
					
						interface Person {
							boolean isFoo();
						}
					
						static class MyGroup implements Group<MyPerson> {
					
							@Override
							public List<MyPerson> getPersons() {
								return List.of();
							}
						}
					
						static class MyPerson implements Person {
					
							@Override
							public boolean isFoo() {
								return true;
							}
						}
					}
					"""},
			"""
				Testing with explicit type
				  Testing lambda
				  Lambda is good
				  Testing method reference
				  Method reference is good
				Testing with wildcard
				  Testing lambda
				  Lambda is good
				  Testing method reference
				  Method reference is good"""
			);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=574269
//java.lang.invoke.LambdaConversionException: Invalid receiver type class java.lang.Object
public void testBug574269_2() {
	this.runConformTest(
			new String[] {
				"TestX.java",
				"""
					import java.util.List;
					
					public class TestX {
					
						public static void main(String[] args) {
							Group<MyPerson> group1 = new MyGroup();
							group1.getPersons().stream().anyMatch(TestX.Person::isFoo);
							System.out.println("Method reference is good");
						\t
							Group<?> group2 = new MyGroup();
							group2.getPersons().stream().anyMatch(TestX.Person::isFoo);
							System.out.println("Method reference is not bad");
						}
					
						interface Group<T extends Person> {
							List<T> getPersons();
						}
					
						interface Person {
							boolean isFoo();
						}
					
						static class MyGroup implements Group<MyPerson> {
							@Override
							public List<MyPerson> getPersons() {
								return List.of();
							}
						}
					
						static class MyPerson implements Person {
							@Override
							public boolean isFoo() {
								return true;
							}
						}
					}
					"""},
			"Method reference is good\n" +
			"Method reference is not bad"
			);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=570511
//java.lang.BootstrapMethodError in Eclipse compiled code that doesn't happen with javac
public void testBug570511() {
	this.runConformTest(
			new String[] {
				"T.java",
				"""
					// ---------------------------------------------------------------
					import java.util.function.Consumer;
					
					public class T {
					    public static void main(String[] args) {
					        new T().test(new X());
					    }
					
					    void test(I<?> i) {
					        i.m("a", "b", this::m);
					    }
					
					    void m(I<?> i) {
					        System.out.println("T.m");
					    }
					}
					
					interface I<C extends I<C>> {
					    C m(Object key, Object value, Consumer<? super C> consumer);
					}
					
					class X implements I<X> {
					    @Override
					    public X m(Object key, Object value, Consumer<? super X> consumer) {
					        consumer.accept(this);
					        System.out.println("X.m");
					        return null;
					    }
					}
					"""},
			"T.m\n" +
			"X.m"
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577719
// BootstrapMethodError: call site initialization exception
public void testBug577719() {
	this.runConformTest(
			new String[] {
				"WeirdCleanupAndCallSiteInitializationException.java",
				"""
					import java.util.stream.Stream;
					
					public class WeirdCleanupAndCallSiteInitializationException {
						public static void main(String[] args) {
							B b = () -> new A() {
							};
							A a = get();
							System.out.println("done");
						}
					
						public static A get(B<?>... sources) {
							return Stream.of(sources) //
									.map(B::getT) //
									.filter(A::exists_testOpen) //
									.findFirst() //
									.orElse(null);
						}
					
						public interface B<T extends A> extends A {
							T getT();
						}
					
						public interface A {
							default boolean exists_testOpen() {
								return true;
							}
						}
					}
					"""},
			"done"
			);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577719
// BootstrapMethodError: call site initialization exception
public void testBug577719_2() {
	this.runConformTest(
			new String[] {
				"WeirdCleanupAndCallSiteInitializationException.java",
				"""
					import java.util.stream.Stream;
					
					public class WeirdCleanupAndCallSiteInitializationException {
						public static void main(String[] args) {
							B b = () -> new A() {
							};
							A a = get();
							System.out.println("done");
						}
					
						public static A get(B<?>... sources) {
							return Stream.of(sources) //
									.map(B::getT) //
									.filter(x -> x.exists_testOpen()) //
									.findFirst() //
									.orElse(null);
						}
					
						public interface B<T extends A> extends A {
							T getT();
						}
					
						public interface A {
							default boolean exists_testOpen() {
								return true;
							}
						}
					}
					"""},
			"done"
			);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1162
// Eclipse incorrectly requires catch for nested sneaky throws; OpenJDK compiles with no problem
public void testGH1162() {
	this.runNegativeTest(
			new String[] {
				"X11.java",
				"""
					import java.io.IOException;
					import java.nio.file.Path;
					import java.util.function.Consumer;
					import java.util.stream.Stream;
					
					public class X11 {
					
						public X11 parse() throws IOException {
							return null;
						}
					\t
						Stream<Path> list() throws IOException {
							return null;
						}
					
						public void foo() throws IOException {
					
							throwingConsumer((Path barDir) -> {
								try (final Stream<Path> files = list()) {
									throwingConsumer((Path file) -> {
										final X11 document;// = parse();
										document = parse();
									});
								}
							});
						}
						public void goo() throws IOException {
					
							throwingConsumer((Path barDir) -> {
								try (final Stream<Path> files = list()) {
									throwingConsumer((Path file) -> {
										final X11 document = parse();
									});
								}
							});
						}
					
						public static <T, X extends Throwable> ThrowingConsumer<T, X> throwingConsumer(
								final ThrowingConsumer<T, X> consumer) {
							return consumer;
						}
					}
					
					
					interface ThrowingConsumer<T, X extends Throwable> extends Consumer<T> {
					
						void tryAccept(T t) throws X;
					
						default void accept(final T t) {
							tryAccept(t);
						}
					}
					"""},
			"""
				----------
				1. ERROR in X11.java (at line 50)
					tryAccept(t);
					^^^^^^^^^^^^
				Unhandled exception type X
				----------
				""");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1162
// Eclipse incorrectly requires catch for nested sneaky throws; OpenJDK compiles with no problem
public void testGH1162_2() {
	this.runNegativeTest(
			new String[] {
				"X11.java",
				"""
					import java.io.IOException;
					import java.nio.file.Path;
					import java.util.function.Consumer;
					import java.util.stream.Stream;
					
					public class X11 {
					
						public X11 parse() throws IOException {
							return null;
						}
					\t
						Stream<Path> list() throws IOException {
							return null;
						}
					
						public void foo() throws IOException {
					
							throwingConsumer(() -> {
								try (final Stream<Path> files = list()) {
									throwingConsumer(() -> {
										final X11 document;// = parse();
										document = parse();
									});
								}
							});
						}
						public static <T, X extends Throwable> ThrowingConsumer<T, X> throwingConsumer(
								final ThrowingConsumer<T, X> consumer) {
							return consumer;
						}
					}
					
					
					interface ThrowingConsumer<T, X extends Throwable> extends Consumer<T> {
					
						void tryAccept() throws X;
					
						default void accept(final T t) {
							tryAccept();
						}
					}
					"""},
			"""
				----------
				1. ERROR in X11.java (at line 39)
					tryAccept();
					^^^^^^^^^^^
				Unhandled exception type X
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576252
// Open declaration / Javadoc popup is confused by overloaded method with method reference
public void testBug576252() {
	this.runConformTest(
			new String[] {
				"LambdaTest.java",
				"""
					public class LambdaTest {
						public static void method(String value) {
							System.out.print("para");
						}
					
						public static void method(java.util.function.Supplier<String> supplier) {
							System.out.print(supplier.get());
						}
					
						public static void main(String[] args) {
							LambdaTest.method(LambdaTest.class::toString);
						}
					}
					"""},
			"class LambdaTest");
}
public static Class testClass() {
	return LambdaExpressionsTest.class;
}
}
