/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class ProblemConstructorTest extends AbstractRegressionTest {

public ProblemConstructorTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return ProblemConstructorTest.class;
}

public void test001() {
	this.runNegativeTest(
		new String[] {
			"prs/Test1.java",
			"""
				package prs;\t
				import java.io.IOException;\t
				public class Test1 {\t
				String s = 3;\t
				Test1() throws IOException {\t
				}\t
				}"""
		},
		"""
			----------
			1. ERROR in prs\\Test1.java (at line 4)
				String s = 3;\t
				           ^
			Type mismatch: cannot convert from int to String
			----------
			""",
		null,
		true,
		null,
		true,
		false,
		false);
	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"prs/Test2.java",
			"""
				package prs;\t
				import java.io.IOException;\t
				public class Test2 {\t
				public void foo() {\t
				try {\t
				Test1 t = new Test1();\t
				System.out.println();\t
				} catch(IOException e)\t
				{\t
				e.printStackTrace();\t
				}\t
				}\t
				}"""
		},
		// compiler results
		"" /* expected compiler log */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		// javac options
		JavacTestOptions.SKIP /* skip javac tests */);
}
// 49843
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public X();
				    public Y();
				   \s
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public X();
				       ^^^
			This method requires a body instead of a semicolon
			----------
			2. ERROR in X.java (at line 3)
				public Y();
				       ^^^
			Return type for the method is missing
			----------
			3. ERROR in X.java (at line 3)
				public Y();
				       ^^^
			This method requires a body instead of a semicolon
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=163443
public void test003() {
	this.runNegativeTest(
		new String[] {
			"Example.java",
			"""
				class Example {
				  private Example() {
				  }
				  public Example(int i) {
				  }
				}
				class E1 {
				    private E1(int i) {}
				    private E1(long l) {}
				}
				class E2 {
				    private E2(int i) {}
				}
				class E3 {
				    public E3(int i) {}
				    Zork z;
				}
				"""
		},
		"""
			----------
			1. WARNING in Example.java (at line 2)
				private Example() {
				        ^^^^^^^^^
			The constructor Example() is never used locally
			----------
			2. ERROR in Example.java (at line 16)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201912, test to make sure that unused public members of
// private class (including constructors, fields, types and methods) get warned about.
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private class M {\s
				       private int state = 0;
				       public int unusedMethod() { return this.state; }
				       public M (int state) { this.state = state;}\s
				       public int unusedField = 0;
				       public class N {}
					}
					private class N {\s
				       private int state = 0;
				       public int usedMethod() { new O(); return new N(this.state + this.usedField).state; }
				       public N (int state) { this.state = state;}\s
				       public int usedField = 0;
				       public class O {}
					}
					public class P {\s
				       private int state = 0;
				       public int unusedMethod() { return this.state; }
				       public P (int state) { this.state = state;}\s
				       public int unusedField = 0;
				       public class N {}
					}
					public M foo(M m, N n) {
				   n.usedMethod(); return m;
					}
				}\s
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				public int unusedMethod() { return this.state; }
				           ^^^^^^^^^^^^^^
			The method unusedMethod() from the type X.M is never used locally
			----------
			2. WARNING in X.java (at line 5)
				public M (int state) { this.state = state;}\s
				       ^^^^^^^^^^^^^
			The constructor X.M(int) is never used locally
			----------
			3. WARNING in X.java (at line 6)
				public int unusedField = 0;
				           ^^^^^^^^^^^
			The value of the field X.M.unusedField is not used
			----------
			4. WARNING in X.java (at line 7)
				public class N {}
				             ^
			The type X.M.N is never used locally
			----------
			"""
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=264991, wrong unused warning reported. Test to ensure that
// we DON'T complain about the constructor of B not being used (as its removal would result in a compile
// error since its base class does not have a no-arg constructor for the synthesized default constructor
// to invoke.
public void test005() {
	String[]  testFiles = new String[] {
			"A.java",
			"""
				public class A {
					public A(String s) {
						B.test();
					}
				
					private static class B extends A {
						public B () { super(""); }
					private static void test() {};
					}
				}
				"""
			};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {
		this.runNegativeTest(testFiles,
				"""
					----------
					1. WARNING in A.java (at line 3)
						B.test();
						^^^^^^^^
					Access to enclosing method test() from the type A.B is emulated by a synthetic accessor method
					----------
					""");
	} else {
		this.runConformTest(testFiles);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we DO complain about the constructor of B not being used when its base class has a no-arg constructor
public void test006() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in A.java (at line 8)
					public B () { super(""); }
					       ^^^^
				The constructor A.B() is never used locally
				----------
				"""
			:
			"""
				----------
				1. WARNING in A.java (at line 3)
					B.test();
					^^^^^^^^
				Access to enclosing method test() from the type A.B is emulated by a synthetic accessor method
				----------
				2. WARNING in A.java (at line 8)
					public B () { super(""); }
					       ^^^^
				The constructor A.B() is never used locally
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A {
					public A(String s) {
						B.test();
					}
					public A() {}
				
					private static class B extends A {
						public B () { super(""); }
						private static void test() {};
				   }
				}
				"""
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we can compile the program successfully after deleting the unused constructor.
public void test007() {
	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
					public A(String s) {
						B.test();
					}
					public A() {}
				
					private static class B extends A {
						private static void test() {};
					}
				}
				"""
		});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we DON'T complain about unused constructor when the super class's default constructor is not visible.
public void test008() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A {
					public A(String s) {this();}
					private A() {}
				}
				class C {
					private static class B extends A {
						public B () { super(""); }
						static void foo() {}
					}
					C() {
						B.foo();
					}
				}
				"""
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we DO complain about unused constructor when the super class's default constructor is visible.
public void test009() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A {
					public A(String s) {}
					protected A() {}
				}
				class C {
					private static class B extends A {
						public B () { super(""); }
						static void foo() {}
					}
					C() {
						B.foo();
					}
				}
				"""
			},
			"""
				----------
				1. WARNING in A.java (at line 7)
					public B () { super(""); }
					       ^^^^
				The constructor C.B() is never used locally
				----------
				""");
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private class Y {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
				   }
				
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				public Y() {}
				       ^^^
			The constructor X.Y() is never used locally
			----------
			""",
		null,
		true,
		null
	);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private static class Y {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
				   }
				
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				public Y() {}
				       ^^^
			The constructor X.Y() is never used locally
			----------
			""",
		null,
		true,
		null
	);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Externalizable;
				import java.io.IOException;
				import java.io.ObjectInput;
				import java.io.ObjectOutput;
				public class X {
					private static class Y implements Externalizable {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
				
						@Override
						public void writeExternal(ObjectOutput out) throws IOException {
						}
				
						@Override\s
						public void readExternal(ObjectInput in) throws IOException,
						ClassNotFoundException {
						}
					}
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}"""
		},
		"",
		null,
		true,
		null
	);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Externalizable;
				import java.io.IOException;
				import java.io.ObjectInput;
				import java.io.ObjectOutput;
				public class X {
					private class Y implements Externalizable {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
				
						@Override
						public void writeExternal(ObjectOutput out) throws IOException {
						}
				
						@Override\s
						public void readExternal(ObjectInput in) throws IOException,
						ClassNotFoundException {
						}
					}
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				public Y() {}
				       ^^^
			The constructor X.Y() is never used locally
			----------
			""",
		null,
		true,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=408038,
//Classes which implement Externalizable should not have an unused constructor warning
//The test case is not directly related to the bug. It was discovered as a result
//of the bug. Please see comment 16 bullet 4 in bugzilla.
public void test408038e() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					int i;
					private X(int x) {i = x;}
					X() {}
					public int foo() {
						X x = new X();
						return x.i;
					}
				}
				"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					private X(int x) {i = x;}
					        ^^^^^^^^
				The constructor X(int) is never used locally
				----------
				""");
}
}
