/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for
 *	 							bug 185682 - Increment/decrement operators mark local variables as read
 *								bug 388800 - [1.8] adjust tests to 1.8 JRE
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import junit.framework.Test;
/**
 * Name Lookup within Inner Classes
 * Creation date: (8/2/00 12:04:53 PM)
 * @author Dennis Conway
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class LookupTest extends AbstractRegressionTest {
public LookupTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

static {
//	TESTS_NAMES = new String [] { "test096" };
}
/**
 * Non-static member class
 */
public void test001() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					private static int value = 23;				\t
					class B {									\t
						private int value;						\t
						B (int val) {							\t
							value = (A.value * 2) + val;		\t
						}										\t
					}											\t
					public static void main (String args[]) {	\t
						int result = new A().new B(12).value; 	\t
						int expected = 58; 						\t
						System.out.println( 					\t
							result == expected 					\t
								? "SUCCESS"  					\t
								: "FAILED : got "+result+" instead of "+ expected);\s
					}											\t
				}"""
		},
		"SUCCESS"
	);
}
/**
 * Attempt to access non-static field from static inner class (illegal)
 */
public void test002() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				class A {										\t
					private int value;							\t
					static class B {							\t
						B () {									\t
							value = 2;							\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						B result = new B();						\t
					}											\t
				}"""
		},
		"""
			----------
			1. WARNING in p1\\A.java (at line 3)
				private int value;							\t
				            ^^^^^
			The value of the field A.value is not used
			----------
			2. ERROR in p1\\A.java (at line 6)
				value = 2;							\t
				^^^^^
			Cannot make a static reference to the non-static field value
			----------
			""");
}
/**
 * Access static field from static inner class
 */
public void test003() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					private static int value;					\t
					static class B {							\t
						B () {									\t
							value = 2;							\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						B result = new B();						\t
						System.out.println("SUCCESS");		\t
					}											\t
				}""",
		},
		"SUCCESS"
	);
}
public void test004() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					private String value;						\t
					private A (String strIn) {					\t
						value = new B(strIn, "E").str;		\t
					}											\t
					class B {									\t
						String str;								\t
							private B (String strFromA, String strIn)	{
								str = strFromA + strIn + new C("S").str;
							}									\t
						class C {								\t
							String str;							\t
							private C (String strIn) {			\t
								str = strIn + new D("S").str;	\t
							}									\t
							class D {							\t
								String str;						\t
								private D (String strIn) {		\t
									str = strIn;				\t
								}								\t
							}									\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						System.out.println(new A("SUCC").value);\t
					}											\t
				}"""
		},
		"SUCCESS"
	);
}
public void test005() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					private static void doSomething(String showThis) {
						System.out.print(showThis);				\t
						return;									\t
					}											\t
					class B {									\t
						void aMethod () {						\t
							p1.A.doSomething("SUCC");			\t
							A.doSomething("ES");				\t
							doSomething("S");					\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						B foo = new A().new B();				\t
						foo.aMethod();							\t
					}											\t
				}"""
		},
		"SUCCESS"
	);
}
/**
 * jdk1.2.2 reports: No variable sucess defined in nested class p1.A. B.C.
 * jdk1.3 reports: success has private access in p1.A
 */
public void test006() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				class A {										\t
					private static String success = "SUCCESS";\t
					public interface B {						\t
						public abstract void aTask();			\t
						class C extends A implements B {		\t
							public void aTask() {System.out.println(this.success);}
						}										\t
					}											\t
					public static void main (String[] argv) {	\t
					}											\t
				}"""
		},
		"""
			----------
			1. ERROR in p1\\A.java (at line 7)
				public void aTask() {System.out.println(this.success);}
				                                             ^^^^^^^
			The field A.success is not visible
			----------
			2. WARNING in p1\\A.java (at line 7)
				public void aTask() {System.out.println(this.success);}
				                                             ^^^^^^^
			The static field A.success should be accessed in a static way
			----------
			""");
}
/**
 * No errors in jdk1.2.2, jdk1.3
 */
public void test007() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					private static String success = "SUCCESS";\t
					public interface B {						\t
						public abstract void aTask();			\t
						class C extends A implements B {		\t
							public void aTask() {System.out.println(A.success);}
						}										\t
					}											\t
					public static void main (String[] argv) {	\t
					}											\t
				}"""
		}
	);
}
/**
 * jdk1.2.2 reports: Undefined variable: A.this
 * jdk1.3 reports: non-static variable this cannot be referenced from a static context
 */
public void test008() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				class A {										\t
					private static String success = "SUCCESS";\t
					public interface B {						\t
						public abstract void aTask();			\t
						class C extends A implements B {		\t
							public void aTask() {System.out.println(A.this.success);}
						}										\t
					}											\t
					public static void main (String[] argv) {	\t
					}											\t
				}"""
		},
		"""
			----------
			1. ERROR in p1\\A.java (at line 7)
				public void aTask() {System.out.println(A.this.success);}
				                                        ^^^^^^
			No enclosing instance of the type A is accessible in scope
			----------
			2. WARNING in p1\\A.java (at line 7)
				public void aTask() {System.out.println(A.this.success);}
				                                               ^^^^^^^
			The static field A.success should be accessed in a static way
			----------
			"""
	);
}
/**
 * jdk1.2.2 reports: No variable success defined in nested class p1.A. B.C
 * jdk1.3 reports: success has private access in p1.A
 */
public void test009() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				class A {										\t
					private String success = "SUCCESS";		\t
					public interface B {						\t
						public abstract void aTask();			\t
						class C extends A implements B {		\t
							public void aTask() {System.out.println(this.success);}
						}										\t
					}											\t
					public static void main (String[] argv) {	\t
					}											\t
				}"""
		},
		"""
			----------
			1. ERROR in p1\\A.java (at line 7)
				public void aTask() {System.out.println(this.success);}
				                                             ^^^^^^^
			The field A.success is not visible
			----------
			""");
}
/**
 * jdk1.2.2 reports: Can't make a static reference to nonstatic variable success in class p1.A
 * jdk1.3 reports: non-static variable success cannot be referenced from a static context
 */
public void test010() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				class A {										\t
					private String success = "SUCCESS";		\t
					public interface B {						\t
						public abstract void aTask();			\t
						class C extends A implements B {		\t
							public void aTask() {System.out.println(A.success);}
						}										\t
					}											\t
					public static void main (String[] argv) {	\t
					}											\t
				}"""
		},
		"""
			----------
			1. WARNING in p1\\A.java (at line 3)
				private String success = "SUCCESS";		\t
				               ^^^^^^^
			The value of the field A.success is not used
			----------
			2. ERROR in p1\\A.java (at line 7)
				public void aTask() {System.out.println(A.success);}
				                                        ^^^^^^^^^
			Cannot make a static reference to the non-static field A.success
			----------
			""");
}
public void test011() {
	this.runNegativeTest(
		new String[] {
			/* p2.Aa */
			"p2/Aa.java",
			"""
				package p2;									\t
				class Aa extends p1.A{							\t
					class B implements p1.A.C {					\t
					}											\t
					public static void main (String args[]) {	\t
					}											\t
				}""",
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
				   public A() {								\t
					}											\t
					class B implements C {						\t
						public int sMethod() {					\t
							return 23;							\t
						}										\t
					}											\t
					public interface C {						\t
						public abstract int sMethod();			\t
					}											\t
				}""",

		},
		"""
			----------
			1. ERROR in p2\\Aa.java (at line 3)
				class B implements p1.A.C {					\t
				      ^
			The type Aa.B must implement the inherited abstract method A.C.sMethod()
			----------
			"""
	);
}
public void test012() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					public interface B {						\t
						public abstract void aMethod (int A);	\t
						public interface C {					\t
							public abstract void anotherMethod();\t
						}										\t
					}											\t
					public class aClass implements B, B.C {		\t
						public void aMethod (int A) {			\t
						}										\t
						public void anotherMethod(){}			\t
					}											\t
				   	public static void main (String argv[]) {\t
						System.out.println("SUCCESS");		\t
					}											\t
				}"""
		},
		"SUCCESS"
	);
}
public void test013() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A {								\t
					public interface B {						\t
						public abstract void aMethod (int A);	\t
						public interface C {					\t
							public abstract void anotherMethod(int A);
						}										\t
					}											\t
					public class aClass implements B, B.C {		\t
						public void aMethod (int A) {			\t
							public void anotherMethod(int A) {};\t
						}										\t
					}											\t
				   	public static void main (String argv[]) {\t
						System.out.println("SUCCESS");		\t
					}											\t
				}"""
		},
		"----------\n" +
		"1. ERROR in p1\\A.java (at line 9)\n" +
		"	public class aClass implements B, B.C {			\n" +
		"	             ^^^^^^\n" +
		"The type A.aClass must implement the inherited abstract method A.B.C.anotherMethod(int)\n" +
		"----------\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"2. ERROR in p1\\A.java (at line 11)\n" +
		"	public void anotherMethod(int A) {};	\n" +
		"	                         ^\n" +
		"Syntax error on token \"(\", ; expected\n" +
		"----------\n" +
		"3. ERROR in p1\\A.java (at line 11)\n" +
		"	public void anotherMethod(int A) {};	\n" +
		"	                               ^\n" +
		"Syntax error on token \")\", ; expected\n"
		:
		"1. ERROR in p1\\A.java (at line 9)\n" +
		"	public class aClass implements B, B.C {			\n" +
		"	             ^^^^^^\n" +
		"The type A.aClass must implement the inherited abstract method A.B.C.anotherMethod(int)\n" +
		"----------\n" +
		"2. ERROR in p1\\A.java (at line 11)\n" +
		"	public void anotherMethod(int A) {};	\n" +
		"	       ^^^^\n" +
		"Syntax error on token \"void\", record expected\n"
		) +
		"----------\n"
	);
}
public void test014() {
	this.runNegativeTest(
		new String[] {
			/* pack1.First */
			"pack1/First.java",
			"""
				package pack1;									\t
				public class First {							\t
					public static void something() {}			\t
						class Inner {}							\t
					public static void main (String argv[]) {	\t
						First.Inner foo = new First().new Inner();\t
						foo.something();						\t
						System.out.println("SUCCESS");		\t
					}											\t
				}"""
		},
		"""
			----------
			1. ERROR in pack1\\First.java (at line 7)
				foo.something();						\t
				    ^^^^^^^^^
			The method something() is undefined for the type First.Inner
			----------
			"""
	);
}
public void test015() {
	this.runConformTest(
		new String[] {
			/* pack1.First */
			"pack1/First.java",
			"""
				package pack1;									\t
				public class First {							\t
						class Inner {							\t
							public void something() {}			\t
						}										\t
					public static void main (String argv[]) {	\t
						First.Inner foo = new First().new Inner();\t
						foo.something();						\t
						System.out.println("SUCCESS");		\t
					}											\t
				}"""
		},
		"SUCCESS"
	);
}
public void test016() {
	this.runConformTest(
		new String[] {
			/* pack1.Outer */
			"pack1/Outer.java",
			"""
				package pack1;									\t
				import pack2.*;								\t
				public class Outer {							\t
					int time, distance;							\t
					public Outer() {							\t
					}											\t
					public Outer(int d) {						\t
						distance = d;							\t
					}											\t
					public void aMethod() {						\t
						this.distance *= 2;						\t
						return;									\t
					}											\t
				}""",
			/* pack2.OuterTwo */
			"pack2/OuterTwo.java",
			"""
				package pack2;									\t
				import pack1.*;								\t
				public class OuterTwo extends Outer {			\t
					public OuterTwo(int bar) {					\t
						Outer A = new Outer(3) {				\t
							public void bMethod(){				\t
								final class X {					\t
									int price;					\t
									public X(int inp) {			\t
										price = inp + 32;		\t
									}							\t
								}								\t
							}									\t
						};										\t
					}											\t
					public static void main (String argv[]) {	\t
						System.out.println("");				\t
						OuterTwo foo = new OuterTwo(12);		\t
						Outer bar = new Outer(8);				\t
					}											\t
				}"""
		}
	);
}
public void test017() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A	{								\t
					int value;									\t
					public A(B bVal) {							\t
						bVal.sval += "V";						\t
					}											\t
					static class B {							\t
						public static String sval;				\t
						public void aMethod() {					\t
							sval += "S";						\t
							A bar = new A(this);				\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						B foo = new B();						\t
						foo.sval = "U";						\t
						foo.aMethod();							\t
						System.out.println(foo.sval);			\t
					}											\t
				}"""
		},
		"USV"
	);
}
/**
 * member class
 */
public void test018() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A	{								\t
					private String rating;						\t
					public class B {							\t
						String rating;							\t
						public B (A sth) {						\t
							sth.rating = "m";					\t
							rating = "er";					\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						A foo = new A();						\t
						foo.rating = "o";						\t
						B bar = foo.new B(foo);					\t
						System.out.println(foo.rating + bar.rating);
					}											\t
				}"""
		},
		"mer"
	);
}
/**
 * member class
 */
public void test019() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A	{								\t
					private String rating;						\t
					public void setRating(A sth, String setTo) {\t
						sth.rating = setTo;						\t
						return;									\t
					}											\t
					public class B {							\t
						public B (A sth) {						\t
							setRating(sth, "m");				\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						A foo = new A();						\t
						foo.rating = "o";						\t
						B bar = foo.new B(foo);					\t
						System.out.println(foo.rating + bar.other);\t
					}											\t
				}"""
		},
		"""
			----------
			1. ERROR in p1\\A.java (at line 17)
				System.out.println(foo.rating + bar.other);\t
				                                    ^^^^^
			other cannot be resolved or is not a field
			----------
			"""
	);
}
/**
 * member class
 */
public void test020() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in p1\\A.java (at line 13)
					System.out.println(foo.rating + bar.other);\t
					                                    ^^^^^
				other cannot be resolved or is not a field
				----------
				"""
			:
			"""
				----------
				1. WARNING in p1\\A.java (at line 6)
					sth.rating = "m";					\t
					    ^^^^^^
				Write access to enclosing field A.rating is emulated by a synthetic accessor method
				----------
				2. ERROR in p1\\A.java (at line 13)
					System.out.println(foo.rating + bar.other);\t
					                                    ^^^^^
				other cannot be resolved or is not a field
				----------
				""";
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A	{								\t
					private String rating;						\t
					public class B {							\t
						public B (A sth) {						\t
							sth.rating = "m";					\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						A foo = new A();						\t
						foo.rating = "o";						\t
						B bar = foo.new B(foo);					\t
						System.out.println(foo.rating + bar.other);\t
					}											\t
				}"""
		},
		errMessage);
}
/**
 * member class
 */
public void test021() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				public class A	{								\t
					private String rating;						\t
					public class B {							\t
						public B (A sth) {						\t
							sth.rating = "m";					\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						A foo = new A();						\t
						foo.rating = "o";						\t
						B bar = foo.new B(foo);					\t
						System.out.println(foo.rating);			\t
					}											\t
				}"""
		}
	);
}
public void test022() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;									\t
				import p2.*;									\t
				public class A {								\t
					public int aValue;							\t
					public A() {}								\t
					public static class C extends A {			\t
						public String aString;					\t
						public C() {							\t
						}										\t
					}											\t
				}""",
			/* p2.B */
			"p2/B.java",
			"""
				package p2;									\t
				import p1.*;									\t
				public class B extends A.C {					\t
					public B() {}								\t
					public class D extends A {					\t
						public D() {							\t
							C val2 = new C();					\t
							val2.aString = "s";				\t
							A val = new A();					\t
							val.aValue = 23;					\t
						}										\t
					}											\t
					public static void main (String argv[]) {	\t
						D foo = new B().new D();				\t
					}											\t
				}"""
		}
	);
}
public void test023() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;
				public class A implements B {					\t
				}												\t
				interface B {									\t
					public class A implements B {				\t
						public static void main (String argv[]) {\t
							class Ba {							\t
								int time;						\t
							}									\t
							Ba foo = new Ba();					\t
							foo.time = 3;						\t
						}										\t
						interface C {							\t
						}										\t
						interface Bb extends C {				\t
						}										\t
					}											\t
				}"""
		}
	);
}
public void test024() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;								\t
				public class A {							\t
					protected static String bleh;			\t
					interface B {							\t
						public String bleh();				\t
						class C{							\t
							public String bleh() {return "B";}
						}									\t
					}										\t
					class C implements B {					\t
						public String bleh() {return "A";}\t
					}										\t
					public static void main(String argv[]) {\t
						C foo = new A().new C();			\t
					}										\t
				}"""
		}
	);
}
public void test025() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;							\t
				import p2.*;							\t
				public class A {						\t
					public static class B {				\t
						public static int B;			\t
					}									\t
					public static void main(String argv[]) {
						B foo = new A.B();				\t
						B bar = new B();				\t
						foo.B = 2;						\t
						p2.B bbar = new p2.B();			\t
						if (bar.B == 35) {				\t
							System.out.println("SUCCESS");
						}								\t
						else {							\t
							System.out.println(bar.B);	\t
						}								\t
					}									\t
				}""",
			"p2/B.java",
			"""
				package p2;							\t
				import p1.*;							\t
				public class B extends A {				\t
					public B() {						\t
						A.B bleh = new A.B();			\t
						bleh.B = 35;					\t
					}									\t
				}"""
		},
		"SUCCESS"
	);
}
public void test026() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;							\t
				public class A {						\t
					public static class B {				\t
						protected static int B;			\t
					}									\t
					public static void main(String argv[]) {
						B foo = new A.B();				\t
						B bar = new B();				\t
						B.B = 2;						\t
						p2.B bbar = new p2.B();			\t
						if (B.B == 35) {				\t
							System.out.println("SUCCESS");
						}								\t
						else {							\t
							System.out.println(B.B);	\t
						}								\t
					}									\t
				}""",
			"p2/B.java",
			"""
				package p2;							\t
				import p1.*;							\t
				public class B extends A {				\t
					public B() {						\t
						A.B bleh = new A.B();			\t
						bleh.B = 35;					\t
					}									\t
				}"""
		},
		"""
			----------
			1. ERROR in p2\\B.java (at line 6)
				bleh.B = 35;					\t
				     ^
			The field A.B.B is not visible
			----------
			""");
}
public void test027() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;							\t
				public class A {						\t
					protected static class B {			\t
						public static int B;			\t
					}									\t
					public static void main(String argv[]) {
						B foo = new A.B();				\t
						B bar = new B();				\t
						B.B = 2;						\t
						p2.B bbar = new p2.B();			\t
						if (B.B == 35) {				\t
							System.out.println("SUCCESS");
						}								\t
						else {							\t
							System.out.println(B.B);	\t
						}								\t
					}									\t
				}""",
			"p2/B.java",
			"""
				package p2;							\t
				import p1.*;							\t
				public class B extends A {				\t
					public B() {						\t
						A.B bleh = new A.B();			\t
						A.B.B = 35;					\t
					}									\t
				}"""
		},
		"""
			----------
			1. ERROR in p2\\B.java (at line 5)
				A.B bleh = new A.B();			\t
				           ^^^^^^^^^
			The constructor A.B() is not visible
			----------
			"""
	);
}
public void test028() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;								\t
				public class A {							\t
					static class B {						\t
						public static class C {				\t
							private static int a;			\t
							private int b;					\t
						}									\t
					}										\t
					class D extends B {						\t
						int j = p1.A.B.C.a;					\t
					}										\t
					public static void main (String argv[]) {\t
						System.out.println("SUCCESS");	\t
					}										\t
				}"""
		},
		"SUCCESS"
	);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=10634
 */
public void test029() {
	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				import p2.Top;\t
				public class X extends Top {\t
					Member field;\t
				}\t
				""",
			"p2/Top.java",
			"""
				package p2;\t
				public class Top {\t
					class Member {\t
						void foo(){}\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 4)
				Member field;\t
				^^^^^^
			The type Member is not visible
			----------
			""");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11435
 * 1.3 compiler must accept classfiles without abstract method (target >=1.2)
 */
public void test030() {

	Hashtable target1_2 = new Hashtable();
	target1_2.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);

	this.runConformTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;\s
				public abstract class A implements I {\t
				  public static void main(String[] args) {\t
				    System.out.println("SUCCESS");\t
				  }\t
				}\s
				interface I {\t
					void foo();\t
				}\t
				""",
		},
		"SUCCESS", // expected output
		null, // custom classpath
		true, // flush previous output dir content
		null, // special vm args
		target1_2,  // custom options
		null/*no custom requestor*/);

	this.runConformTest(
		new String[] {
			"p1/C.java",
			"""
				package p1;\s
				public class C {\t
					void bar(A a){\s
						a.foo();\t
					}\t
				  public static void main(String[] args) {\t
				    System.out.println("SUCCESS");\t
				  }\t
				}\s
				"""
		},
		"SUCCESS", // expected output
		null, // custom classpath
		false, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null/*no custom requestor*/);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - must filter abstract methods when searching concrete methods
 */
public void test031() {

	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X extends AbstractY {\t
					public void init() {\t
						super.init();\t
					}\t
					public static void main(String[] arguments) {\t
						new X().init();\t
					}\t
				}\t
				abstract class AbstractY extends AbstractZ implements I {\t
					public void init(int i) {\t
					}\t
				}\t
				abstract class AbstractZ implements I {\t
					public void init() {\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				interface I {\t
					void init();\t
					void init(int i);\t
				}\t
				"""
		},
		"SUCCESS"); // expected output
}

/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=29211
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=29213
 */
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------
			"""
				public class X {
					public static void main(String[] arguments) {
						System.out.println(p.Bar.array[0].length);
						System.out.println(p.Bar.array.length);
						System.out.println(p.Bar.array[0].foo());
					}
				}
				""",
			"p/Bar.java", //----------------------------
			"""
				package p;
				public class Bar {
					public static Z[] array;
				}
				class Z {
					public String foo(){\s
						return "";
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				System.out.println(p.Bar.array[0].length);
				                   ^^^^^^^^^^^^^^
			The type Z is not visible
			----------
			2. ERROR in X.java (at line 4)
				System.out.println(p.Bar.array.length);
				                   ^^^^^^^^^^^
			The type Z is not visible
			----------
			3. ERROR in X.java (at line 5)
				System.out.println(p.Bar.array[0].foo());
				                   ^^^^^^^^^^^^^^
			The type Z is not visible
			----------
			""");
}

// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test033() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"""
				package p;\t
				public abstract class X {\t
					abstract void foo();\t
				}\t
				""",
			"q/Y.java", //==================================
			"""
				package q;\t
				public class Y extends p.X {\t
					void foo(){}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in q\\Y.java (at line 2)
				public class Y extends p.X {\t
				             ^
			This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Y. Either make the type abstract or make the inherited method visible
			----------
			2. WARNING in q\\Y.java (at line 3)
				void foo(){}\t
				     ^^^^^
			The method Y.foo() does not override the inherited method from X since it is private to a different package
			----------
			""");
}

// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test034() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"""
				package p;\t
				public abstract class X {\t
					abstract void foo();\t
				}\t
				""",
			"q/Y.java", //==================================
			"""
				package q;\t
				public abstract class Y extends p.X {\t
					void foo(){}\t
				}\t
				class Z extends Y {\t
				}\t
				""",
		},
		"""
			----------
			1. WARNING in q\\Y.java (at line 3)
				void foo(){}\t
				     ^^^^^
			The method Y.foo() does not override the inherited method from X since it is private to a different package
			----------
			2. ERROR in q\\Y.java (at line 5)
				class Z extends Y {\t
				      ^
			This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible
			----------
			"""
);
}

// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test035() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"""
				package p;\t
				public abstract class X {\t
					abstract void foo();\t
					abstract void bar();\t
				}\t
				""",
			"p/Y.java", //==================================
			"""
				package p;\t
				public abstract class Y extends X {\t
					void foo(){};\t
				}\t
				""",
			"q/Z.java", //==================================
			"""
				package q;\t
				class Z extends p.Y {\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in q\\Z.java (at line 2)
				class Z extends p.Y {\t
				      ^
			This class must implement the inherited abstract method X.bar(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible
			----------
			""");
}
// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test036() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"""
				package p;\t
				public abstract class X {\t
					abstract void foo();\t
					public interface I {\t
						void foo();\t
					}\t
				}\t
				""",
			"q/Y.java", //==================================
			"""
				package q;\t
				public abstract class Y extends p.X {\t
					void foo(){}\t
				}\t
				class Z extends Y implements p.X.I {\t
				}\t
				""",
		},
		"----------\n" +
		"1. WARNING in q\\Y.java (at line 3)\n" +
		"	void foo(){}	\n" +
		"	     ^^^^^\n" +
		"The method Y.foo() does not override the inherited method from X since it is private to a different package\n" +
		"----------\n" +
		"2. ERROR in q\\Y.java (at line 5)\n" +
		"	class Z extends Y implements p.X.I {	\n" +
		"	      ^\n" +
		"This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible\n" +
		"----------\n" + // TODO (philippe) should not have following error due to default abstract?
		"3. ERROR in q\\Y.java (at line 5)\n" +
		"	class Z extends Y implements p.X.I {	\n" +
		"	      ^\n" +
		"The inherited method Y.foo() cannot hide the public abstract method in X.I\n" +
		"----------\n");
}
// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test037() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"""
				package p;\t
				public abstract class X {\t
					abstract void foo();\t
					void bar(){}\t
				}\t
				""",
			"q/Y.java", //==================================
			"""
				package q;\t
				public abstract class Y extends p.X {\t
					void foo(){}	//warn\s
					void bar(){}	//warn\s
				}\t
				class Z extends Y {\t
					void bar(){}	//nowarn\s
				}\t
				""",
		},
		"""
			----------
			1. WARNING in q\\Y.java (at line 3)
				void foo(){}	//warn\s
				     ^^^^^
			The method Y.foo() does not override the inherited method from X since it is private to a different package
			----------
			2. WARNING in q\\Y.java (at line 4)
				void bar(){}	//warn\s
				     ^^^^^
			The method Y.bar() does not override the inherited method from X since it is private to a different package
			----------
			3. ERROR in q\\Y.java (at line 6)
				class Z extends Y {\t
				      ^
			This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible
			----------
			""");
}
// 30805 Abstract non-visible method diagnosis fooled by intermediate declarations
public void test038() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"""
				package p;\t
				public abstract class X {\t
					abstract void foo();\t
				}\t
				""",
			"q/Y.java", //==================================
			"""
				package q;\t
				public abstract class Y extends p.X {\t
					void foo(){}	//warn\s
				}\t
				class Z extends Y {\t
					void foo(){}	//error\s
				}\t
				""",
		},
		"""
			----------
			1. WARNING in q\\Y.java (at line 3)
				void foo(){}	//warn\s
				     ^^^^^
			The method Y.foo() does not override the inherited method from X since it is private to a different package
			----------
			2. ERROR in q\\Y.java (at line 5)
				class Z extends Y {\t
				      ^
			This class must implement the inherited abstract method X.foo(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible
			----------
			""");
}

// 31198 - regression after 30805 - Abstract non-visible method diagnosis fooled by intermediate declarations
public void test039() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //==================================
			"package p;	\n" +
			"public abstract class X {	\n" +
			"	abstract void foo();	\n" + // should not complain about this one in Z, since it has a visible implementation
			"	abstract void bar();	\n" +
			"}	\n",
			"p/Y.java", //==================================
			"""
				package p;\t
				public abstract class Y extends X {\t
					public void foo(){};\t
				}\t
				""",
			"q/Z.java", //==================================
			"""
				package q;\t
				class Z extends p.Y {\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in q\\Z.java (at line 2)
				class Z extends p.Y {\t
				      ^
			This class must implement the inherited abstract method X.bar(), but cannot override it since it is not visible from Z. Either make the type abstract or make the inherited method visible
			----------
			""");
}

/*
 * 31398 - non-visible abstract method fooling method verification - should not complain about foo() or bar()
 */
public void test040() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //================================
			"package p;	\n" +
			"public class X extends q.Y.Member {	\n" +
			"		void baz(){}	\n" + // doesn't hide Y.baz()
			"}	\n",
			"q/Y.java", //================================
			"""
				package q;\t
				public abstract class Y {\t
					abstract void foo();\t
					abstract void bar();\t
					abstract void baz();\t
					public static abstract class Member extends Y {\t
						public void foo() {}\t
						void bar(){}\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 2)
				public class X extends q.Y.Member {\t
				             ^
			This class must implement the inherited abstract method Y.baz(), but cannot override it since it is not visible from X. Either make the type abstract or make the inherited method visible
			----------
			2. WARNING in p\\X.java (at line 3)
				void baz(){}\t
				     ^^^^^
			The method X.baz() does not override the inherited method from Y since it is private to a different package
			----------
			""");
}

/*
 * 31450 - non-visible abstract method fooling method verification - should not complain about foo()
 */
public void test041() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //================================
			"""
				package p;\t
				public class X extends q.Y.Member {\t
					public void foo() {}\t
					public static class M extends X {}\t
				}\t
				""",
			"q/Y.java", //================================
			"package q;	\n" +
			"public abstract class Y {	\n" +
			"	abstract void foo();	\n" +
			"	abstract void bar();	\n" +
			"	public static abstract class Member extends Y {	\n" +
			"		protected abstract void foo();	\n" + // takes precedence over inherited abstract Y.foo()
			"	}	\n" +
			"}	\n",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 2)
				public class X extends q.Y.Member {\t
				             ^
			This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from X. Either make the type abstract or make the inherited method visible
			----------
			2. ERROR in p\\X.java (at line 4)
				public static class M extends X {}\t
				                    ^
			This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from M. Either make the type abstract or make the inherited method visible
			----------
			""");
}

/*
 * 31450 - non-visible abstract method fooling method verification - should not complain about foo()
 */
public void test042() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //================================
			"""
				package p;\t
				public class X extends q.Y.Member {\t
					public void foo() {}\t
					public static class M extends X {}\t
				}\t
				""",
			"q/Y.java", //================================
			"""
				package q;\t
				public abstract class Y {\t
					abstract void foo();\t
					abstract void bar();\t
					public static abstract class Member extends Y {\t
						void foo(){}\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 2)
				public class X extends q.Y.Member {\t
				             ^
			This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from X. Either make the type abstract or make the inherited method visible
			----------
			2. WARNING in p\\X.java (at line 3)
				public void foo() {}\t
				            ^^^^^
			The method X.foo() does not override the inherited method from Y.Member since it is private to a different package
			----------
			3. ERROR in p\\X.java (at line 4)
				public static class M extends X {}\t
				                    ^
			This class must implement the inherited abstract method Y.bar(), but cannot override it since it is not visible from M. Either make the type abstract or make the inherited method visible
			----------
			""");
}

public void test043() {
	this.runConformTest(
		new String[] {
			"X.java", //================================
			"""
				public class X {
					public interface Copyable extends Cloneable {
						public Object clone() throws CloneNotSupportedException;
					}
				
					public interface TestIf extends Copyable {
					}
				
					public static class ClassA implements Copyable {
						public Object clone() throws CloneNotSupportedException {
							return super.clone();
						}
					}
				
					public static class ClassB implements TestIf {
						public Object clone() throws CloneNotSupportedException {
							return super.clone();
						}
					}
				
					public static void main(String[] args) throws Exception {
						Copyable o1 = new ClassA();
						ClassB o2 = new ClassB();
						TestIf o3 = o2;
						Object clonedObject;
						clonedObject = o1.clone();
						clonedObject = o2.clone();
						clonedObject = o3.clone();
						System.out.println("SUCCESS");
					}
				}"""
		},
		"SUCCESS");
}
/*
 * 62639 - check that missing member type is not noticed if no direct connection with compiled type
 */
public void test044() {
	this.runConformTest(
		new String[] {
			"p/Dumbo.java",
			"""
				package p;
				public class Dumbo {
				  public class Clyde { }
					public static void main(String[] args) {
						  System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");
	// delete binary file Dumbo$Clyde (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p" + File.separator + "Dumbo$Clyde.class"));

	this.runConformTest(
		new String[] {
			"q/Main.java",
			"""
				package q;
				public class Main extends p.Dumbo {
					public static void main(String[] args) {
						  p.Dumbo d;
						  System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS",
		null,
		false,
		null);
}
/*
 * ensure that can still found binary member types at depth >=2 (enclosing name Dumbo$Clyde $ Fred)
 */
public void test045() {
	this.runConformTest(
		new String[] {
			"p/Dumbo.java",
			"""
				package p;
				public class Dumbo {
				  public class Clyde {
				  	  public class Fred {
					  }
					}
					public static void main(String[] args) {
						  System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");

	this.runConformTest(
		new String[] {
			"q/Main.java",
			"""
				package q;
				public class Main extends p.Dumbo {
					public static void main(String[] args) {
						  p.Dumbo.Clyde.Fred f;
						  System.out.println("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS",
		null,
		false,
		null);
}
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java", //================================
			"""
				public class X {
				     private XY foo(XY t) {
				        System.out.println(t);
				        return t;
				    }
				    public static void main(String[] args) {
				        new X() {
				            void run() {
				                foo(new XY());
				            }
				        }.run();
				    }
				}
				class XY {
				    public String toString() {
				        return "SUCCESS";
				    }
				}
				"""
		},
			"""
				----------
				1. ERROR in X.java (at line 9)
					foo(new XY());
					^^^
				Cannot make a static reference to the non-static method foo(XY) from the type X
				----------
				""");
}
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java", //================================
			"""
				public class X extends SuperTest
				{
				    public X()
				    {
				        super();
				    }
				 \s
				    static void print(Object obj)
				    {
				        System.out.println("Object:" + obj.toString());
				    }
				   \s
				    public static void main(String[] args)
				    {
				        print("Hello world");
				    }
				}
				class SuperTest
				{
				    SuperTest(){};
				    static void print(String s)
				    {
				        System.out.println("String: " + s);
				    }
				}
				"""	},
		"String: Hello world");
}
// 73740 - missing serialVersionUID diagnosis shouldn't trigger load of Serializable
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"""
				public class X {
				   public static void main(String[] args) {
						System.out.println("SUCCESS");
				   }
				}
				""",
		},
		"SUCCESS",
		Util.concatWithClassLibs(OUTPUT_DIR, true/*output in front*/),
		false, // do not flush output
		null,  // vm args
		null, // options
		new ICompilerRequestor() {
			public void acceptResult(CompilationResult result) {
				assertNotNull("missing reference information",result.simpleNameReferences);
				char[] serializable = TypeConstants.JAVA_IO_SERIALIZABLE[2];
				for (int i = 0, length = result.simpleNameReferences.length; i < length; i++) {
					char[] name = result.simpleNameReferences[i];
					if (CharOperation.equals(name, serializable))
						assertTrue("should not contain reference to Serializable", false);
				}
			}
		});
}
// 76682 - ClassCastException in qualified name computeConversion
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java", //---------------------------
			"""
				public class X
				{
				    private String foo() {
				        return "Started " + java.text.DateFormat.format(new java.util.Date());
				    }
				}
				""" ,
		},
		"""
			----------
			1. ERROR in X.java (at line 4)\r
				return "Started " + java.text.DateFormat.format(new java.util.Date());\r
				                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot make a static reference to the non-static method format(Date) from the type DateFormat
			----------
			""");
}
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"""
				public class X {
				
				    public static void main(String argv[]) {
				    	X.Y.Z.foo();
				    }
				    static class Y {
				    	static class Z {
				    		static void foo() {
				    			System.out.println("SUCCESS");
				    		}
				    	}
				    }
				}
				""",
		},
		"SUCCESS");
}

public void test051() {
	this.runNegativeTest(
		new String[] {
			"X.java", //---------------------------
			"""
				public class X {
				
				    public static void main(String[] args) {
				        args.finalize();
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				args.finalize();
				     ^^^^^^^^
			The method finalize() from the type Object is not visible
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87463
public void test052() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"""
				public class X {
					public void test() {
						class C {
							public C() {
							}
							public void foo() {
								System.out.println("hello");
							}
						}
						int n = 0;
						switch (n) {
							case 0 :
								if (true) {
									C c2 = new C();
								}
						}
					}
				}
				""",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87463 - variation
public void test053() {
	this.runConformTest(
		new String[] {
			"X.java", //---------------------------
			"""
				public class X {
					public void test() {
						int l = 1;
						switch(l) {
							case 1:\s
								class C {
									public C() {
									}
									public void foo() {
										System.out.println("hello");
									}
								}
								int n = 0;
								switch (n) {
									case 0 :
										if (true) {
											C c2 = new C();
										}
								}
						}
					}
				}
				""",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93486
public void test054() {
    this.runConformTest(
        new String[] {
            "X.java", //---------------------------
            """
				import java.util.LinkedHashMap;
				import java.util.Map.Entry;
				
				public class X {
				   \s
				    private LinkedHashMap fCache;
				   \s
				    public X(final int cacheSize) {
				        // start with 100 elements but be able to grow until cacheSize
				        fCache= new LinkedHashMap(100, 0.75f, true) {
				            /** This class is not intended to be serialized. */
				            private static final long serialVersionUID= 1L;
				            protected boolean removeEldestEntry(Entry eldest) {
				                return size() > cacheSize;
				            }
				        };
				    }
				}
				""",
        },
        "");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106140
public void test055() {
    this.runNegativeTest(
        new String[] {
            "A.java",
            """
				import p.*;
				public class A {
				    public void errors() {
				    B b = new B();
				        String s1 = b.str;
				        String s2 = B.str;
				    }
				}
				""",
            "p/B.java",
            """
				package p;
				class B {
				    public static String str;
				}
				""",
        },
		"""
			----------
			1. ERROR in A.java (at line 4)
				B b = new B();
				^
			The type B is not visible
			----------
			2. ERROR in A.java (at line 4)
				B b = new B();
				          ^
			The type B is not visible
			----------
			3. ERROR in A.java (at line 5)
				String s1 = b.str;
				            ^
			The type B is not visible
			----------
			4. ERROR in A.java (at line 6)
				String s2 = B.str;
				            ^
			The type B is not visible
			----------
			""");
}
// final method in static inner class still found in extending classes
public void test056() {
    this.runConformTest(
        new String[] {
            "X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    I x = new Z();
				    x.foo();
				  }
				  static interface I {
				    Y foo();
				  }
				  static class Y {
				    public final Y foo() {\s
				        System.out.println("SUCCESS");
				        return null;\s
				    }
				  }
				  static class Z extends Y implements I {
				      // empty
				  }
				}""",
        },
        "SUCCESS");
}
// unresolved type does not fool methods signature comparison
public void test057() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.awt.*;
				public class X {
				    public void foo(Window w) {
				        // empty
				    }
				    public void foo(Applet a) {
				        // empty
				    }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				public void foo(Applet a) {
				                ^^^^^^
			Applet cannot be resolved to a type
			----------
			"""
		);
}
public void test058() {
    this.runConformTest(
        new String[] {
        		"p/X.java", // =================
        		"""
					package p;
					
					import p.q.Z;
					public class X {\s
					  public static void main(String argv[]) {
					     System.out.println(Z.z);
					  }
					}""", // =================
        		"p/q/Z.java", // =================
        		"""
					package p.q;
					
					public class Z extends Y implements I {\s
					}
					class Y {
					    protected static int z = 1;
					}
					interface I {
					    int z = 0;
					}""", // =================
		},
		"0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132813
public void test059() {
    this.runNegativeTest(
        new String[] {
        		"X.java", // =================
    			"""
					public class X {
					\t
						void aa(int i) {
						}
						void aa(long l) {
						}
						Zork bb() {
						}
						void cc() {
							this.bb();
						}
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}
					""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Zork bb() {
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 10)
				this.bb();
				     ^^
			The method bb() from the type X refers to the missing type Zork
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132813 - variation
public void test060() {
    this.runNegativeTest(
        new String[] {
        		"X.java", // =================
    			"""
					public class X {
					\t
						void aa(int i) {
						}
						Zork aa(long l) {
						}
						Zork bb() {
						}
						void cc() {
							this.bb();
						}
						public static void main(String[] args) {
							System.out.println("SUCCESS");
						}
					}
					""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Zork aa(long l) {
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 7)
				Zork bb() {
				^^^^
			Zork cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 10)
				this.bb();
				     ^^
			The method bb() from the type X refers to the missing type Zork
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134839
public void test061() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		// ensure target is 1.1 for having default abstract methods involved
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	}
    this.runConformTest(
        new String[] {
        		"X.java", // =================
    			"""
					interface MyInterface {
					        public void writeToStream();
					        public void readFromStream();
					}
					
					public abstract class X implements MyInterface {
					        public void b() {
					        }
					        public void a() {
					                writeTypeToStream();
					        }
					        private void writeTypeToStream() {
					        }
					}
					""", // =================
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134839
public void test062() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		// ensure target is 1.1 for having default abstract methods involved
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	}
    this.runConformTest(
        new String[] {
        		"X.java", // =================
    			"""
					interface MyInterface {
					        public void writeToStream();
					        public void readFromStream();
					}
					
					public abstract class X implements MyInterface {
					        public void b() {
					        }
					        public void a() {
					                writeTypeToStream();
					        }
					        private void writeTypeToStream() {
					        }
					}
					""", // =================
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135292
public void test063() {
    this.runNegativeTest(
        new String[] {
    		"X.java", // =================
			"""
				class 56 {
				
				        private static class B {
				                public static final String F = "";
				        }
				
				        private static class C {
				        }
				
				        public void foo() {
				                System.out.println(B.F);
				        }
				}
				""", // =================
	},
	"""
		----------
		1. ERROR in X.java (at line 1)
			class 56 {
			      ^^
		Syntax error on token "56", Identifier expected
		----------
		2. ERROR in X.java (at line 3)
			private static class B {
			                     ^
		Illegal modifier for the class B; only public, abstract & final are permitted
		----------
		3. ERROR in X.java (at line 7)
			private static class C {
			                     ^
		Illegal modifier for the class C; only public, abstract & final are permitted
		----------
		4. ERROR in X.java (at line 8)
			}
			^
		Syntax error on token "}", delete this token
		----------
		5. ERROR in X.java (at line 11)
			System.out.println(B.F);
			                   ^^^
		The type B is not visible
		----------
		6. ERROR in X.java (at line 13)
			}
			^
		Syntax error, insert "}" to complete ClassBody
		----------
		""");
}
//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=137744
public void test064() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		// ensure target is 1.1 for having default abstract methods involved
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.println("SUCCESS");
							B a = new C();
						\t
							a.hasKursAt(1);
						}
					
					}""",
				"A.java",
				"""
					abstract public class A implements IA0 {
						int t;
						public A() {
						}
					}""",
				"B.java",
				"""
					abstract public class B extends A implements IA3, IA1 {
						int a;
						public B() {
						}
						public void test() {\t
						}
					}""",
				"C.java",
				"""
					public class C extends B implements IA4, IA2{
						int c;
						public C() {
						}
						public boolean hasKursAt(int zeitpunkt) {
							return false;
						}
					}""",
				"IA0.java",
				"""
					public interface IA0 {
						public void test();
					}""",
				"IA1.java",
				"""
					public interface IA1 extends IA0 {
						public boolean hasKursAt(int zeitpunkt);
					}""",
				"IA2.java",
				"""
					public interface IA2 extends IA0 {
						public boolean hasKursAt(int zeitpunkt);
					}""",
				"IA3.java",
				"public interface IA3 extends IA2 {\n" +
				"}",
				"IA4.java",
				"public interface IA4 extends IA3 {\n" +
				"}"
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135323
public void test065() {
	this.runConformTest(
			new String[] {
				"com/internap/other/ScopeExample.java",//===================
				"""
					package com.internap.other;
					import com.internap.*;
					public class ScopeExample {
						private static final String LOGGER = "SUCCESS";
						public static void main(String[] args) {
							PublicAccessSubclass sub = new PublicAccessSubclass() {
								public void implementMe() {
									System.out.println(LOGGER);
								}
							};
							sub.implementMe();
						}
					}""",
				"com/internap/PublicAccessSubclass.java",//===================
				"""
					package com.internap;
					public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {
						public abstract void implementMe();			\t
					}""",
				"com/internap/DefaultAccessSuperclass.java",//===================
				"""
					package com.internap;
					class DefaultAccessSuperclass {
						private static final String LOGGER = "FAILED";
					}""",
			},
			"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135323 - variation
public void test066() {
	this.runConformTest(
			new String[] {
				"com/internap/other/ScopeExample.java",//===================
				"""
					package com.internap.other;
					import com.internap.*;
					public class ScopeExample {
						private static final String LOGGER() { return "SUCCESS"; }
						public static void main(String[] args) {
							PublicAccessSubclass sub = new PublicAccessSubclass() {
								public void implementMe() {
									System.out.println(LOGGER());
								}
							};
							sub.implementMe();
						}
					}""",
				"com/internap/PublicAccessSubclass.java",//===================
				"""
					package com.internap;
					public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {
						public abstract void implementMe();			\t
					}""",
				"com/internap/DefaultAccessSuperclass.java",//===================
				"""
					package com.internap;
					class DefaultAccessSuperclass {
						private static final String LOGGER() { return "FAILED"; }
					}""",
			},
			"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=135323 - variation
public void test067() {
	Map options = getCompilerOptions();
	if (CompilerOptions.VERSION_1_3.equals(options.get(CompilerOptions.OPTION_Compliance))) {
		this.runNegativeTest(
				new String[] {
					"com/internap/other/ScopeExample.java",//===================
					"""
						package com.internap.other;
						import com.internap.*;
						public class ScopeExample {
							private static final String LOGGER = "FAILED";
							public static void main(String[] args) {
								PublicAccessSubclass sub = new PublicAccessSubclass() {
									public void implementMe() {
										System.out.println(LOGGER);
									}
								};
								sub.implementMe();
							}
						}""",
					"com/internap/PublicAccessSubclass.java",//===================
					"""
						package com.internap;
						public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {
							public abstract void implementMe();			\t
						}""",
					"com/internap/DefaultAccessSuperclass.java",//===================
					"""
						package com.internap;
						class DefaultAccessSuperclass {
							public static final String LOGGER = "SUCCESS";
						}""",
				},
				"""
					----------
					1. WARNING in com\\internap\\other\\ScopeExample.java (at line 4)\r
						private static final String LOGGER = "FAILED";\r
						                            ^^^^^^
					The value of the field ScopeExample.LOGGER is not used
					----------
					2. ERROR in com\\internap\\other\\ScopeExample.java (at line 8)\r
						System.out.println(LOGGER);\r
						                   ^^^^^^
					The field LOGGER is defined in an inherited type and an enclosing scope\s
					----------
					""");
		return;
	}
	this.runConformTest(
			new String[] {
				"com/internap/other/ScopeExample.java",//===================
				"""
					package com.internap.other;
					import com.internap.*;
					public class ScopeExample {
						private static final String LOGGER = "FAILED";
						public static void main(String[] args) {
							PublicAccessSubclass sub = new PublicAccessSubclass() {
								public void implementMe() {
									System.out.println(LOGGER);
								}
							};
							sub.implementMe();
						}
					}""",
				"com/internap/PublicAccessSubclass.java",//===================
				"""
					package com.internap;
					public abstract class PublicAccessSubclass extends DefaultAccessSuperclass {
						public abstract void implementMe();			\t
					}""",
				"com/internap/DefaultAccessSuperclass.java",//===================
				"""
					package com.internap;
					class DefaultAccessSuperclass {
						public static final String LOGGER = "SUCCESS";
					}""",
			},
			"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139099
public void test068() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_5) return;
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runConformTest(
			new String[] {
				"X.java",//===================
				"""
					public class X {
					    public X() {
					    }
					    public static void main(String[] args) {
					        X l = new X();
					        StringBuffer sb = new StringBuffer();
					        sb.append(l);
					    }
					}""", // =================,
			},
			"",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139099
public void test068a() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_5) return;

	this.runConformTest(
		new String[] {
			"X1.java",
			"""
				public class X1 { X1 foo() { return null; } }
				class X2 extends X1 { X2 foo() { return null; } }
				class Y { public X2 foo() { return null; } }
				interface I { X1 foo(); }
				class Z extends Y implements I {}""",
		},
		"");
	this.runConformTest(
		new String[] {
			"Test.java",//===================
			"""
				public class Test {
				    public static void main(String[] args) {
				        X1 x = new X2().foo();
				        X2 xx = new X2().foo();
				        X1 z = new Z().foo();
				        X2 zz = new Z().foo();
				    }
				}""", // =================,
		},
		"",
		null,
		false,
		null);

	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"Test14.java",//===================
			"""
				public class Test14 {
				    public static void main(String[] args) {
				        X1 x = new X2().foo();
				        X2 xx = new X2().foo();
				        X1 z = new Z().foo();
				        X2 zz = new Z().foo();
				    }
				}""", // =================,
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139099 - variation
public void test069() {
	this.runConformTest(
			new String[] {
				"X.java",//===================
				"""
					public class X {
					    public X() {
					    }
					    public static void main(String[] args) {
					        X l = new X();
					        StringBuffer sb = new StringBuffer();
					        sb.append(l);
					    }
					}""", // =================,
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140643
public void test070() {
	this.runConformTest(
			new String[] {
				"X.java",//===================
				"""
					public class X {
						interface I {
						}
					
						void test() {
							new I() {
								void foo() {
								}
							}.foo(); // compiles OK.
							new I() {
								void $foo() {
								}
							}.$foo(); // The method $foo() is undefined for the type new T.I(){}
						}
					}""", // =================
			},
			"");
}
// using $ in the name of a class defined within another package
public void test071() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
				}""",
			"p/X$X.java",
			"""
				package p;
				public class X$X {
				}""",
		},
		"");
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				import p.*;
				public class Y {
				  X$X f = new X$X();
				}""",
		},
		"",
		null /* no extra class libraries */,
		false /* do not flush output directory */,
		null /* no vm arguments */,
		null /* no custom options*/,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}
public void test072() {
	this.runNegativeTest(
			new String[] {
				"X.java",//===================
				"""
					public class X {
						void bar(AX ax) {
							ax.foo(null);
						}
					\t
					}
					interface IX {
						void foo(String s);
					}
					interface JX {
						void foo(Thread t);
					}
					abstract class AX implements IX, JX {
						public void foo(String s) {}
					}
					""", // =================
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					ax.foo(null);
					   ^^^
				The method foo(String) is ambiguous for the type AX
				----------
				""");
}
public void test073() {
	this.runNegativeTest(
		new String[] {
			"E.java",//===================
			"""
				public class E {
					void run(int i) {}
					static class Inner {
						void run() { run(1); }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in E.java (at line 4)
				void run() { run(1); }
				             ^^^
			The method run() in the type E.Inner is not applicable for the arguments (int)
			----------
			""");
}

// was Compliance_1_x#test008
public void test074() {
	String[] sources = new String[] {
		"p1/Test.java",
		"""
			package p1;\s
			import Test2;\t
			import Test2.Member;\t
			public class Test {\s
				public static void main(String[] arguments) {\s
					System.out.println("SUCCESS");\t
				}\s
			}\s
			""",
		"Test2.java",
		"""
			public class Test2 {\s
				public class Member {\t
				}\s
			}\s
			"""
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runConformTest(
			sources,
			"SUCCESS");
	} else {
		runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in p1\\Test.java (at line 2)
					import Test2;\t
					       ^^^^^
				The import Test2 cannot be resolved
				----------
				2. ERROR in p1\\Test.java (at line 3)
					import Test2.Member;\t
					       ^^^^^
				The import Test2 cannot be resolved
				----------
				""");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150758
public void test075() {
	this.runConformTest(
			new String[] {
				"package1/Test.java",//===================
				"""
					package package1;
					import package2.MyList;
					public class Test {
					        public void reproduce(String sortKey, boolean isAscending) {
					                MyList recList = new MyList();
					                recList.add(null);
					        }
					}
					""",//===================
				"package2/MyList.java",//===================
				"""
					package package2;
					import java.util.AbstractList;
					import java.util.List;
					public class MyList extends AbstractList implements List {
					        void add(Integer i) {
					        }
					        public Object get(int index) {
					                return null;
					        }
					        public int size() {
					                return 0;
					        }
					}""", // =================
			},
			"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159543
public void test076() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"p/Y.java",	//===================
				"""
					package p;
					public class Y {
					  public static void foo(String s) {
					  }
					}
					""",		//===================
				"q/X.java",	//===================
				"""
					package q;
					import static p.Y.foo;
					public class X {
					        void foo() {
					        }
					        void bar() {
					          foo("");
					        }
					}""", 		// =================
			},
			"""
				----------
				1. ERROR in q\\X.java (at line 7)
					foo("");
					^^^
				The method foo() in the type X is not applicable for the arguments (String)
				----------
				""");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159893
public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"""
				abstract  class B {
				  public String getValue(){
				    return "pippo";
				  }
				}
				class D {
				  private String value;
				  public D(String p_Value){
				    value = p_Value;
				  }
				  private  String getValue(){
				    return "pippoD";
				  }
				}
				public class X extends B {
				  class C extends D{
				    public C() {
				      super(getValue());
				      String s = getValue();
				    }
				  }
				}
				""", 		// =================
		},
		"");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159893 - variation
public void test078() {
	this.runNegativeTest(
		new String[] {
			"X.java",	//===================
			"""
				class D {
				  private String value;
				  public D(String p_Value){
				    value = p_Value;
				  }
				  private  String getValue(){
				    return "pippoD";
				  }
				}
				public class X {
				  class C extends D{
				    public C() {
				      super(getValue());
				      String s = getValue();
				    }
				  }
				}
				""", 		// =================
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				private String value;
				               ^^^^^
			The value of the field D.value is not used
			----------
			2. ERROR in X.java (at line 13)
				super(getValue());
				      ^^^^^^^^
			The method getValue() from the type D is not visible
			----------
			3. ERROR in X.java (at line 14)
				String s = getValue();
				           ^^^^^^^^
			The method getValue() from the type D is not visible
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166354
// **
public void test079() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"""
				abstract class Y {
				  private void foo(boolean b) {
				    System.out.println("Y");
				    return;
				  }
				}
				public class X {
				  private void foo(String s) {
				    System.out.println("X");
				    return;
				  }
				  private class Z extends Y {
				    public void bar(boolean b) {
				      foo("Flag " + b);
				      X.this.foo("Flag " + b);
				    }
				  }
				  Z m = new Z();
				  public static void main(String args[]) {
				    new X().m.bar(true);
				  }
				}""", 		// =================
		},
		"X\nX");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166354
// variant
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"""
				abstract class Y {
				  private void foo(String s) {
				    System.out.println("Y");
				    return;
				  }
				}
				public class X {
				  private void foo(String s) {
				    System.out.println("X");
				    return;
				  }
				  private class Z extends Y {
				    public void bar(boolean b) {
				      foo("Flag " + b);
				      X.this.foo("Flag " + b);
				    }
				  }
				  Z m = new Z();
				  public static void main(String args[]) {
				    new X().m.bar(true);
				  }
				}""", 		// =================
		},
		"X\nX");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
public void test081() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"""
				public class X extends Y {
				  public void set(int value) {
				      System.out.println("set(" + value + ")");
				  }
				  public static void main(String[] args) {
				    X x = new X();
				    x.set(1L);
				  }
				}
				abstract class Y implements I {
				  public void set(long value) {
				    set((int)value);
				  }
				  public void set(double value) {
				    set((int)value);
				  }
				}
				interface I {
				  void set(int value);
				  void set(long value);
				}
				""", 		// =================
		},
		"set(1)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
// variant
public void test082() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"""
				public class X extends Y {
				  public void set(int value) {
				      System.out.println("set(" + value + ")");
				  }
				  public static void main(String[] args) {
				    X x = new X();
				    x.set(1L);
				  }
				}
				abstract class Y implements I {
				  public abstract void set(int value);
				  public void set(long value) {
				    set((int)value);
				  }
				  public void set(double value) {
				    set((int)value);
				  }
				}
				interface I {
				  void set(int value);
				  void set(long value);
				}
				""", 		// =================
		},
		"set(1)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
// variant
public void test083() {
	String src[] =
		new String[] {
			"X.java",
			"""
				public class X extends Z {
				  public void set(int value) {
				      System.out.println("set(" + value + ")");
				  }
				  public static void main(String[] args) {
				    X x = new X();
				    x.set(1L);
				  }
				}
				abstract class Z extends Y {
				  public void set(long value) {
				    set((int)value);
				  }
				  public void set(double value) {
				    set((int)value);
				  }
				}
				abstract class Y implements I {
				}
				interface I {
				  void set(int value);
				  void set(long value);
				}
				""",
		};
	if (this.complianceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
			src,
			"""
				----------
				1. ERROR in X.java (at line 12)\r
					set((int)value);\r
					^^^
				The method set(long) is ambiguous for the type Z
				----------
				2. ERROR in X.java (at line 15)\r
					set((int)value);\r
					^^^
				The method set(long) is ambiguous for the type Z
				----------
				""");
	} else {
		this.runConformTest(
			src,
			"set(1)");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588
// variant
public void test084() {
	this.runConformTest(
		new String[] {
			"X.java",	//===================
			"""
				public class X extends Y {
				  public void set(int value, int i) {
				      System.out.println("set(" + value + ")");
				  }
				  public static void main(String[] args) {
				    X x = new X();
				    x.set(1L, 1);
				  }
				}
				abstract class Y implements I {
				  public void set(long value, int i) {
				    set((int)value, i);
				  }
				  public void set(int i, double value) {
				    set(i, (int)value);
				  }
				}
				interface I {
				  void set(int value, int i);
				}
				""", 		// =================
		},
		"set(1)");
}

public void test086() {
	this.runNegativeTest(
		new String[] {
			"X.java",	//===================
			"""
				public class X {
					public static void main(String[] arguments) {
						Y y = new Y();
						System.out.println(y.array[0]);
						System.out.println(y.length);
					}
				}
				class Y {
					private class Invisible {}
					Invisible[] array;
				}
				""", 		// =================
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				System.out.println(y.length);
				                     ^^^^^^
			length cannot be resolved or is not a field
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185422 - variation
public void _test087() {
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"""
					import java.util.*;
					/**
					 * @see Private - Private is not visible here
					 */
					public abstract class X implements X.Private, Secondary.SecondaryPrivate {
						/**
					 * @see Private - Private is visible here
						 */
						private static interface Private {}
						Private field;
					}
					class Secondary {
						private static interface SecondaryPrivate {}
					}
					""", // =================
			},
			"done");
}
public void test088() {
	this.runNegativeTest(
		new String[] {
			"java/lang/Object.java",	//===================
			"""
				package java.lang;
				public class Object {
					public Object() {
						super();
					}
				}
				""", 		// =================
		},
		"""
			----------
			1. ERROR in java\\lang\\Object.java (at line 4)
				super();
				^^^^^^^^
			super cannot be used in java.lang.Object
			----------
			""");
}

public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",	//===================
			"""
				public class X {
					static class Member implements X {
						Member () {
							super();
						}
					}
				}
				""", 		// =================
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				static class Member implements X {
				                               ^
			The type X cannot be a superinterface of Member; a superinterface must be an interface
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239833
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public synchronized int f;
					public synchronized X() {}
					public volatile void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public synchronized int f;
				                        ^
			Illegal modifier for the field f; only public, protected, private, static, final, transient & volatile are permitted
			----------
			2. ERROR in X.java (at line 3)
				public synchronized X() {}
				                    ^^^
			Illegal modifier for the constructor in type X; only public, protected & private are permitted
			----------
			3. ERROR in X.java (at line 4)
				public volatile void foo() {}
				                     ^^^^^
			Illegal modifier for the method foo; only public, protected, private, abstract, static, final, synchronized, native & strictfp are permitted
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test091() {
	this.runNegativeTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"""
				package foo;
				public class Test {
				        public class M1 {
				              public class M2 {}
				        }
				}
				""",
			"bar/Test2.java",//------------------------------
			"""
				package bar;
				import foo.Test;
				import Test.M1.M2;
				public class Test2 {
				}
				""",
		},
		"""
			----------
			1. ERROR in bar\\Test2.java (at line 3)
				import Test.M1.M2;
				       ^^^^
			The import Test cannot be resolved
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test092() {
	this.runNegativeTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"""
				package foo;
				public class Test {
				        public class M1 {
				              public class M2 {}
				        }
				}
				""",
			"bar/Test2.java",//------------------------------
			"""
				package bar;
				import foo.*;
				import Test.M1.M2;
				public class Test2 {
				}
				""",
		},
		"""
			----------
			1. ERROR in bar\\Test2.java (at line 3)
				import Test.M1.M2;
				       ^^^^
			The import Test cannot be resolved
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test093() {
	this.runNegativeTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"""
				package foo;
				public class Test {
				        public class M1 {
				              public class foo {}
				        }
				}
				""",
			"bar/Test2.java",//------------------------------
			"""
				package bar;
				import foo.Test;
				import Test.M1.foo;
				public class Test2 {
				}
				""",
		},
		"""
			----------
			1. ERROR in bar\\Test2.java (at line 3)
				import Test.M1.foo;
				       ^^^^
			The import Test cannot be resolved
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250211 - variation
public void test094() {
	this.runConformTest(
		new String[] {
			"foo/Test.java",//------------------------------
			"""
				package foo;
				public class Test {
				        public class M1 {
				              public class foo {}
				        }
				}
				""",
			"bar/Test2.java",//------------------------------
			"""
				package bar;
				import foo.Test.M1.foo;
				public class Test2 {
				}
				""",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=277965
public void test095() {
	this.runNegativeTest(
		new String[] {
			"p1/B.java",
			"package p1;\n" +
			"protected class B1 {}",
			"X.java", // =================
			"public class X extends p1.B1 {}",
	},
	"""
		----------
		1. ERROR in p1\\B.java (at line 2)
			protected class B1 {}
			                ^^
		Illegal modifier for the class B1; only public, abstract & final are permitted
		----------
		----------
		1. ERROR in X.java (at line 1)
			public class X extends p1.B1 {}
			                       ^^^^^
		The type p1.B1 is not visible
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id= 317212
public void test096() {
	this.runNegativeTest(
		new String[] {
			"p0/B.java",//------------------------------
			"""
				package p0;
				public class B {
				    public static A m() {
				        return new A();
				    }
				}
				class A {
				        public class M {
				            public M() {}
				        }
				}
				""",
			"p1/C.java",//------------------------------
			"""
				package p1;
				import p0.B;
				public class C {
				    public static void main(String[] args) {
				        B.m().new M();
				    }
				}""",
		},
		"""
			----------
			1. ERROR in p1\\C.java (at line 5)
				B.m().new M();
				^^^^^
			The type p0.A is not visible
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id= 317212
public void test097() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in B.java (at line 6)
					public class M {
					             ^
				The type B.A.M is never used locally
				----------
				2. WARNING in B.java (at line 7)
					public M() {}
					       ^^^
				The constructor B.A.M() is never used locally
				----------
				3. ERROR in B.java (at line 13)
					B.m().new M();
					^^^^^
				The type B$A is not visible
				----------
				"""
			:
			"""
				----------
				1. WARNING in B.java (at line 3)
					return new B().new A();
					       ^^^^^^^^^^^^^^^
				Access to enclosing constructor B.A() is emulated by a synthetic accessor method
				----------
				2. WARNING in B.java (at line 6)
					public class M {
					             ^
				The type B.A.M is never used locally
				----------
				3. WARNING in B.java (at line 7)
					public M() {}
					       ^^^
				The constructor B.A.M() is never used locally
				----------
				4. ERROR in B.java (at line 13)
					B.m().new M();
					^^^^^
				The type B$A is not visible
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"""
				public class B {
				    public static A m() {
				        return new B().new A();
				    }
				    private class A {
				        public class M {
				            public M() {}
				        }
				    }
				}
				class C {
				    public static void main(String[] args) {
				        B.m().new M();
				    }
				}
				""",
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test098() {
	this.runConformTest(
		new String[] {
			"B.java",//------------------------------
			"""
				class A {
				    public final static class B {
				        public final static String length = "very long";
				    }
				    private  int [] B = new int[5];
				}
				public class B {
				    public static void main(String[] args) {
				        System.out.println(A.B.length);
				    }
				}
				""",
		},
		"very long");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test099() {
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"""
				class A {
				    public final static class B {
				        public final static String length = "very long";
				    }
				    public int [] B = new int[5];
				}
				public class B {
				    public static void main(String[] args) {
				        System.out.println(A.B.length);
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in B.java (at line 9)
				System.out.println(A.B.length);
				                   ^^^^^^^^^^
			Cannot make a static reference to the non-static field A.B
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test100() {
	this.runConformTest(
		new String[] {
			"B.java",//------------------------------
			"""
				class A {
				    public final class B {
				        public final String length = "very long";
				    }
				    public static int [] B = new int[5];
				}
				public class B {
				    public static void main(String[] args) {
				        System.out.println(A.B.length);
				    }
				}
				""",
		},
		"5");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test101() {
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"""
				class A {
				    private final class B {
				        public final String length = "very long";
				    }
				    private int [] B = new int[5];
				}
				public class B {
				    public static void main(String[] args) {
				        System.out.println(A.B.length);
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in B.java (at line 2)
				private final class B {
				                    ^
			The type A.B is never used locally
			----------
			2. WARNING in B.java (at line 3)
				public final String length = "very long";
				                    ^^^^^^
			The value of the field A.B.length is not used
			----------
			3. WARNING in B.java (at line 5)
				private int [] B = new int[5];
				               ^
			The value of the field A.B is not used
			----------
			4. ERROR in B.java (at line 9)
				System.out.println(A.B.length);
				                     ^
			The field A.B is not visible
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317858
public void test102() {
	this.runNegativeTest(
		new String[] {
			"B.java",//------------------------------
			"""
				class A {
				    public final class B {
				        private final String length = "very long";
				    }
				    private int [] B = new int[5];
				}
				public class B {
				    public static void main(String[] args) {
				        System.out.println(A.B.length);
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in B.java (at line 3)
				private final String length = "very long";
				                     ^^^^^^
			The value of the field A.B.length is not used
			----------
			2. WARNING in B.java (at line 5)
				private int [] B = new int[5];
				               ^
			The value of the field A.B is not used
			----------
			3. ERROR in B.java (at line 9)
				System.out.println(A.B.length);
				                       ^^^^^^
			The field A.B.length is not visible
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
public void test103() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in A.java (at line 2)
					private int x;
					            ^
				The value of the field A.x is not used
				----------
				2. WARNING in A.java (at line 4)
					private int x;
					            ^
				The value of the field A.B.x is not used
				----------
				3. WARNING in A.java (at line 5)
					private C c = new C() {
					          ^
				The value of the field A.B.c is not used
				----------
				4. WARNING in A.java (at line 6)
					void foo() {
					     ^^^^^
				The method foo() from the type new A.C(){} is never used locally
				----------
				5. WARNING in A.java (at line 12)
					private int x;
					            ^
				The value of the field A.C.x is not used
				----------
				"""
			:
			"""
				----------
				1. WARNING in A.java (at line 2)
					private int x;
					            ^
				The value of the field A.x is not used
				----------
				2. WARNING in A.java (at line 4)
					private int x;
					            ^
				The value of the field A.B.x is not used
				----------
				3. WARNING in A.java (at line 5)
					private C c = new C() {
					          ^
				The value of the field A.B.c is not used
				----------
				4. WARNING in A.java (at line 6)
					void foo() {
					     ^^^^^
				The method foo() from the type new A.C(){} is never used locally
				----------
				5. WARNING in A.java (at line 7)
					x = 3;
					^
				Write access to enclosing field A.B.x is emulated by a synthetic accessor method
				----------
				6. WARNING in A.java (at line 12)
					private int x;
					            ^
				The value of the field A.C.x is not used
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"A.java",//------------------------------
			"""
				public class A {
					  private int x;
					  static class B {
					    private int x;
					    private C c = new C() {
					      void foo() {
					        x = 3;
					      }
					    };
					  }
					  static class C {
					    private int x;
					  }
					}
				""",
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
public void test104() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"A.java",//------------------------------
			"""
				public class A {
					  private int x;
					  static class B {
					    private int x;
					    private C c = new C() {
					      void foo() {
					        x = 3;
					      }
					    };
					  }
					  static class C {
					    public int x;
					  }
					}
				""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in A.java (at line 2)
				private int x;
				            ^
			The value of the field A.x is not used
			----------
			2. WARNING in A.java (at line 4)
				private int x;
				            ^
			The value of the field A.B.x is not used
			----------
			3. WARNING in A.java (at line 5)
				private C c = new C() {
				          ^
			The value of the field A.B.c is not used
			----------
			4. WARNING in A.java (at line 6)
				void foo() {
				     ^^^^^
			The method foo() from the type new A.C(){} is never used locally
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
public void test105() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	String errMessage =	isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in A.java (at line 2)
					private int x;
					            ^
				The value of the field A.x is not used
				----------
				2. WARNING in A.java (at line 3)
					private C c = new C() {
					          ^
				The value of the field A.c is not used
				----------
				3. WARNING in A.java (at line 4)
					void foo() {
					     ^^^^^
				The method foo() from the type new A.C(){} is never used locally
				----------
				4. WARNING in A.java (at line 9)
					private int x;
					            ^
				The value of the field A.C.x is not used
				----------
				"""
			:
			"""
				----------
				1. WARNING in A.java (at line 2)
					private int x;
					            ^
				The value of the field A.x is not used
				----------
				2. WARNING in A.java (at line 3)
					private C c = new C() {
					          ^
				The value of the field A.c is not used
				----------
				3. WARNING in A.java (at line 4)
					void foo() {
					     ^^^^^
				The method foo() from the type new A.C(){} is never used locally
				----------
				4. WARNING in A.java (at line 5)
					x = 3;
					^
				Write access to enclosing field A.x is emulated by a synthetic accessor method
				----------
				5. WARNING in A.java (at line 9)
					private int x;
					            ^
				The value of the field A.C.x is not used
				----------
				""";

	this.runNegativeTest(
		new String[] {
			"A.java",//------------------------------
			"""
				public class A {
					  private int x;
					  private C c = new C() {
					    void foo() {
					      x = 3;
					    }
					  };
					  static class C {
					    private int x;
					  }
					 }
				""",
		},
		errMessage);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=350738
public void test106() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",//------------------------------
			"""
				import java.util.List;
				import java.util.Set;
				public class X {
					private static List<Object> foo1(Set<Object> set) {
					    return foo1(set);
					}
					private static <T> List<T> foo3(Set<T> set) {
					    return foo3(set);
					}
				}
				""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in X.java (at line 4)
				private static List<Object> foo1(Set<Object> set) {
				                            ^^^^^^^^^^^^^^^^^^^^^
			The method foo1(Set<Object>) from the type X is never used locally
			----------
			2. WARNING in X.java (at line 7)
				private static <T> List<T> foo3(Set<T> set) {
				                           ^^^^^^^^^^^^^^^^
			The method foo3(Set<T>) from the type X is never used locally
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}

public void testBug537828() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"FieldBug.java",//------------------------------
			"""
				class A {
					Object obj = "A.obj";
				}
				
				class B {
					private Object obj = "B.obj";
					public Object getObj() {return obj;}
				}
				
				public class FieldBug {
					Object obj = "FieldBug.obj";
				
					static class AA extends A {
						class BB extends B {
							Object n = obj;
						}
					}
				\t
					public static void main(String[] args) {
						System.out.println(new AA().new BB().n);
					}
				}""",
		},
		"A.obj");
}
public void testBug577350_001() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK10) return;
	this.runConformTest(
		new String[] {
			"X.java",//------------------------------
			"""
				import java.lang.invoke.MethodHandles;
				import java.lang.invoke.VarHandle;
				
				public class X {
				 private static final VarHandle VH;
				 static {
				   var lookup = MethodHandles.lookup();
				   try {
				     VH = lookup.findVarHandle(X.class, "value", int.class);
				   } catch (NoSuchFieldException | IllegalAccessException e) {
				     throw new AssertionError(e);
				   }
				 }
				
				 private volatile int value;
				
				 public void test() {
				   VH.compareAndSet(this, 2, 3); // <--- HERE
				 }
				
				 public static void main(String[] args) {
				   new X().test();
				 }
				}""",
		},
		"");
}
public void testBug577350_002() {
	Map options = getCompilerOptions();
	CompilerOptions compOptions = new CompilerOptions(options);
	if (compOptions.complianceLevel < ClassFileConstants.JDK9) return;
	this.runConformTest(
		new String[] {
			"X.java",//------------------------------
			"""
				import java.lang.invoke.MethodHandles;
				import java.lang.invoke.MethodHandles.Lookup;
				import java.lang.invoke.VarHandle;
				
				public class X {
				 private static final VarHandle VH;
				 static {
				   Lookup lookup = MethodHandles.lookup();
				   try {
				     VH = lookup.findVarHandle(X.class, "value", int.class);
				   } catch (NoSuchFieldException | IllegalAccessException e) {
				     throw new AssertionError(e);
				   }
				 }
				
				 private volatile int value;
				
				 public void test() {
				   VH.compareAndSet(this, 2, 3); // <--- HERE
				 }
				
				 public static void main(String[] args) {
				   new X().test();
				 }
				}""",
		},
		"");
}
public static Class testClass() {	return LookupTest.class;
}
}
