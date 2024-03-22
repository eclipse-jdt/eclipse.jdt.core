/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - contributions for
 *								bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
 *								bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class InitializationTests extends AbstractRegressionTest {

public InitializationTests(String name) {
		super(name);
}

public static Test suite() {
	Test suite = buildAllCompliancesTestSuite(testClass());
	return suite;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public void foo() throws Exception{
						String temp;
						Object temp2= new String("test");
						if(temp2 instanceof String) {
							temp = (String) temp2;
						} else {
							if (true) {
								throw new Exception("not a string");
							}
						}
						temp.trim();
					}
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					{
						if (true)
							throw new NullPointerException();
					}
					public X(){}
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public int a;\
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public void foo() {
							final X abc;\
							if (true || (abc = new X(2)).returnA() == 2) {
								System.out.println("Hello");
					       } else {\s
								abc = new X(1);
							}
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					abc = new X(1);
					^^^
				The final local variable abc may already have been assigned
				----------
				""",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;\
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public static boolean comparison (X x, int val) {
							return (x.returnA() == val);
						}
						public void foo() {
							final X abc;
							boolean comp = X.comparison((abc = new X(2)), (abc = new X(1)).returnA());
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					boolean comp = X.comparison((abc = new X(2)), (abc = new X(1)).returnA());
					                                               ^^^
				The final local variable abc may already have been assigned
				----------
				""",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;\
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public void foo() {
							final X abc;
							boolean comp = ((abc = new X(2)).returnA() == 1 || (abc = new X(1)).returnA() == 1);
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					boolean comp = ((abc = new X(2)).returnA() == 1 || (abc = new X(1)).returnA() == 1);
					                                                    ^^^
				The final local variable abc may already have been assigned
				----------
				""",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020f() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;\
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public void foo() {
							final X abc;
							int val;
							if (true || (abc = new X(1)).returnA() == 1)
								val = (abc = new X(2)).returnA();
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					val = (abc = new X(2)).returnA();
					       ^^^
				The final local variable abc may already have been assigned
				----------
				""",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020g() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;\
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public void foo() {
							final X abc;
							int val;
							if (true) {
								val = 0;
							} else {
								val = (abc = new X(1)).returnA();
							}
							abc = new X(2);
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 16)
					abc = new X(2);
					^^^
				The final local variable abc may already have been assigned
				----------
				""",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020h() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						final static X[] abc;
						static {
							for (Object[] j = new Object[1]; !(((abc = new X[10]).length) == 10); ){
								break;
							}
						}
						//Zork z;
					}
					"""

			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020i() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;\
						class Inner {
							public int aInner;
							public Inner(int a){
								this.aInner = a;
							}
						}
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public void foo() {
							int val;\
							final int int1;
							final  int int2;
							val = new X(int1 = 1).new Inner(int2 = int1).aInner;
							System.out.println(int1 + int2);
						}
					}
					"""

			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;\
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
						public void foo() {
							final int abc;
							abc = new X(abc = 2).returnA();
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					abc = new X(abc = 2).returnA();
					^^^
				The final local variable abc may already have been assigned
				----------
				""",
			null, false, options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318020
public void test318020k() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   private int a;
						final int x;
						{
							x = new X(x = 2).returnA();\
						}
						public X (int a) {
							this.a = a;
						}
						public int returnA () {
							return a;
						}
					}
					"""

			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					x = new X(x = 2).returnA();	}
					^
				The final field x may already have been assigned
				----------
				""",
			null, false, options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=325567
public void test325567() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					
					public class X {
						public static void main(String[] args) {
							bar(3);
						}
						public static void bar(int i) {
							final String before;
							try {
								before = foo();
							} catch (IOException e) {
								// ignore
							}
							B b = new B(new I() {
								public String bar() {
									return new String(before);
								}
							});
							try {
								b.i.bar();
							} catch(Exception e) {
								// ignore
							}
						}
					
						private static String foo() throws IOException {
							return null;
						}
					\t
						static class B {
							I i;
							B(I i) {
								this.i = i;
							}
						}
						static interface I {
							String bar();
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 16)
					return new String(before);
					                  ^^^^^^
				The local variable before may not have been initialized
				----------
				""",
			null, false, options);
}

// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
// definite assignment along all true-yielding paths is sufficient
public void testBug324178b() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"""
				public class Bug324178 {
					 boolean foo(boolean b) {
				        boolean v;
				        if (b ? false : (true && (v = true)))
				            return v;
				        return false;
				    }
				    public static void main(String[] args) {
				        System.out.print(new Bug324178().foo(false));
				    }
				}
				"""
		},
		"true");
}

// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
// definite assignment along all true-yielding paths is sufficient
public void testBug324178c() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"""
				public class Bug324178 {
					 boolean foo() {
				        boolean r=false;\
				        boolean v;
				        if ((true && (v = true)) ? true : true && (v = false)) r = v;
				        return r;
				    }
				    public static void main(String[] args) {
				        System.out.print(new Bug324178().foo());
				    }
				}
				"""
		},
		"true");
}
// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
// must detect that b2 may be uninitialized, no special semantics for Boolean
public void testBug324178d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runNegativeTest(
		new String[] {
			"Bug324178.java",
			"""
				public class Bug324178 {
					 boolean foo(boolean b1) {
				  		 Boolean b2;
				        if (b1 ? (b2 = Boolean.TRUE) : null)
				          return b2;
				        return false;
				    }
				    public static void main(String[] args) {
				        System.out.print(new Bug324178().foo(true));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Bug324178.java (at line 5)
				return b2;
				       ^^
			The local variable b2 may not have been initialized
			----------
			""");
}
// Bug 383690 - [compiler] location of error re uninitialized final field should be aligned
public void testBug383690() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					 final Object o; // report here!
					 final static Object oStatic; // report here!
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				final Object o; // report here!
				             ^
			The blank final field o may not have been initialized
			----------
			2. ERROR in X.java (at line 3)
				final static Object oStatic; // report here!
				                    ^^^^^^^
			The blank final field oStatic may not have been initialized
			----------
			""");
}
public static Class testClass() {
	return InitializationTests.class;
}
}
