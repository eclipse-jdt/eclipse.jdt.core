/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
/**
 * Name Lookup within Inner Classes
 * Creation date: (8/2/00 12:04:53 PM)
 * @author: Dennis Conway
 */
public class LookupTest extends AbstractRegressionTest {
public LookupTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
/**
 * Non-static member class
 */
public void test01() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static int value = 23;					\n"+
			"	class B {										\n"+
			"		private int value;							\n"+
			"		B (int val) {								\n"+
			"			value = (A.value * 2) + val;			\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String args[]) {		\n"+
			"		int result = new A().new B(12).value; 		\n"+
			"		int expected = 58; 							\n"+
			"		System.out.println( 						\n"+
			"			result == expected 						\n"+
			"				? \"SUCCESS\"  						\n"+
			"				: \"FAILED : got \"+result+\" instead of \"+ expected); \n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"	
	);									
}
/**
 * Attempt to access non-static field from static inner class (illegal)
 */
public void test02() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private int value;								\n"+
			"	static class B {								\n"+
			"		B () {										\n"+
			"			value = 2;								\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B result = new B();							\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. WARNING in p1\\A.java (at line 3)\n" + 
		"	private int value;								\n" + 
		"	            ^^^^^\n" + 
		"The private field A.value is never used locally\n" + 
		"----------\n" + 
		"2. ERROR in p1\\A.java (at line 6)\n" + 
		"	value = 2;								\n" + 
		"	^^^^^\n" + 
		"Cannot make a static reference to the non-static field value\n" + 
		"----------\n"
	);									
}
/**
 * Access static field from static inner class
 */
public void test03() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static int value;						\n"+
			"	static class B {								\n"+
			"		B () {										\n"+
			"			value = 2;								\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B result = new B();							\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}",
			"SUCCESS"
		}
	);									
}
/**
 * 
 */
public void test04() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private String value;							\n"+
			"	private A (String strIn) {						\n"+
			"		value = new B(strIn, \"E\").str;			\n"+
			"	}												\n"+
			"	class B {										\n"+
			"		String str;									\n"+
			"			private B (String strFromA, String strIn)	{\n"+
			"				str = strFromA + strIn + new C(\"S\").str;\n"+
			"			}										\n"+
			"		class C {									\n"+
			"			String str;								\n"+
			"			private C (String strIn) {				\n"+
			"				str = strIn + new D(\"S\").str;		\n"+
			"			}										\n"+
			"			class D {								\n"+
			"				String str;							\n"+
			"				private D (String strIn) {			\n"+
			"					str = strIn;					\n"+
			"				}									\n"+
			"			}										\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		System.out.println(new A(\"SUCC\").value);	\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 * 
 */
public void test05() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static void doSomething(String showThis) {\n"+
			"		System.out.print(showThis);					\n"+
			"		return;										\n"+
			"	}												\n"+
			"	class B {										\n"+
			"		void aMethod () {							\n"+
			"			p1.A.doSomething(\"SUCC\");				\n"+
			"			A.doSomething(\"ES\");					\n"+
			"			doSomething(\"S\");						\n"+
			"		}											\n"+		
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B foo = new A().new B();					\n"+
			"		foo.aMethod();								\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 * jdk1.2.2 reports: No variable sucess defined in nested class p1.A. B.C.
 * jdk1.3 reports: success has private access in p1.A
 */
public void test06() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private static String success = \"SUCCESS\";	\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(this.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. WARNING in p1\\A.java (at line 3)\n" + 
		"	private static String success = \"SUCCESS\";	\n" + 
		"	                      ^^^^^^^\n" + 
		"The private field A.success is never used locally\n" + 
		"----------\n" + 
		"2. ERROR in p1\\A.java (at line 7)\n" + 
		"	public void aTask() {System.out.println(this.success);}\n" + 
		"	                                        ^^^^^^^^^^^^\n" + 
		"The field success is not visible\n" + 
		"----------\n"

	);
}
/**
 * No errors in jdk1.2.2, jdk1.3
 */
public void test07() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	private static String success = \"SUCCESS\";	\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+	
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(A.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 * jdk1.2.2 reports: Undefined variable: A.this
 * jdk1.3 reports: non-static variable this cannot be referenced from a static context
 */
public void test08() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private static String success = \"SUCCESS\";	\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+	
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(A.this.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\A.java (at line 7)\n" + 
		"	public void aTask() {System.out.println(A.this.success);}\n" + 
		"	                                        ^^^^^^\n" + 
		"No enclosing instance of the type A is accessible in scope\n" + 
		"----------\n" + 
		"2. WARNING in p1\\A.java (at line 7)\n" + 
		"	public void aTask() {System.out.println(A.this.success);}\n" + 
		"	                                        ^^^^^^^^^^^^^^\n" + 
		"The static field A.success should be accessed in a static way\n" + 
		"----------\n"
	);
}
/**
 * jdk1.2.2 reports: No variable success defined in nested class p1.A. B.C
 * jdk1.3 reports: success has private access in p1.A
 */
public void test09() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private String success = \"SUCCESS\";			\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(this.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. WARNING in p1\\A.java (at line 3)\n" + 
		"	private String success = \"SUCCESS\";			\n" + 
		"	               ^^^^^^^\n" + 
		"The private field A.success is never used locally\n" + 
		"----------\n" + 
		"2. ERROR in p1\\A.java (at line 7)\n" + 
		"	public void aTask() {System.out.println(this.success);}\n" + 
		"	                                        ^^^^^^^^^^^^\n" + 
		"The field success is not visible\n" + 
		"----------\n"

	);
}
/**
 * jdk1.2.2 reports: Can't make a static reference to nonstatic variable success in class p1.A
 * jdk1.3 reports: non-static variable success cannot be referenced from a static context
 */
public void test10() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"class A {											\n"+
			"	private String success = \"SUCCESS\";			\n"+
			"	public interface B {							\n"+
			"		public abstract void aTask();				\n"+	
			"		class C extends A implements B {			\n"+
			"			public void aTask() {System.out.println(A.success);}\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String[] argv) {		\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. WARNING in p1\\A.java (at line 3)\n" + 
		"	private String success = \"SUCCESS\";			\n" + 
		"	               ^^^^^^^\n" + 
		"The private field A.success is never used locally\n" + 
		"----------\n" + 
		"2. ERROR in p1\\A.java (at line 7)\n" + 
		"	public void aTask() {System.out.println(A.success);}\n" + 
		"	                                        ^^^^^^^^^\n" + 
		"Cannot make a static reference to the non-static field A.success\n" + 
		"----------\n"
	);
}
/**
 * 
 */
public void test11() {
	this.runNegativeTest(
		new String[] {
			/* p2.Aa */
			"p2/Aa.java",
			"package p2;										\n"+
			"class Aa extends p1.A{								\n"+
			"	class B implements p1.A.C {						\n"+
			"	}												\n"+
			"	public static void main (String args[]) {		\n"+
			"	}												\n"+
			"}",
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"   public A() {									\n"+
			"	}												\n"+
			"	class B implements C {							\n"+
			"		public int sMethod() {						\n"+
			"			return 23;								\n"+
			"		}											\n"+
			"	}												\n"+
			"	public interface C {							\n"+
			"		public abstract int sMethod();				\n"+
			"	}												\n"+
			"}",

		},
		"----------\n" + 
		"1. ERROR in p2\\Aa.java (at line 3)\n" + 
		"	class B implements p1.A.C {						\n" + 
		"	      ^\n" + 
		"Class must implement the inherited abstract method A.C.sMethod()\n" + 
		"----------\n"
	);
}
/**
 * 
 */
public void test12() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	public interface B {							\n"+
			"		public abstract void aMethod (int A);		\n"+
			"		public interface C {						\n"+
			"			public abstract void anotherMethod();	\n"+
			"		}											\n"+
			"	}												\n"+
			"	public class aClass implements B, B.C {			\n"+
			"		public void aMethod (int A) {				\n"+
			"		}											\n"+
			"		public void anotherMethod(){}				\n"+
			"	}												\n"+
			"   	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 * 
 */
public void test13() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A {									\n"+
			"	public interface B {							\n"+
			"		public abstract void aMethod (int A);		\n"+
			"		public interface C {						\n"+
			"			public abstract void anotherMethod(int A);\n"+
			"		}											\n"+
			"	}												\n"+
			"	public class aClass implements B, B.C {			\n"+
			"		public void aMethod (int A) {				\n"+
			"			public void anotherMethod(int A) {};	\n"+
			"		}											\n"+
			"	}												\n"+
			"   	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\A.java (at line 9)\n" + 
		"	public class aClass implements B, B.C {			\n" + 
		"	             ^^^^^^\n" + 
		"Class must implement the inherited abstract method A.B.C.anotherMethod(int)\n" + 
		"----------\n" + 
		"2. ERROR in p1\\A.java (at line 11)\n" + 
		"	public void anotherMethod(int A) {};	\n" + 
		"	                         ^\n" + 
		"Syntax error on token \"(\", \";\" expected\n" + 
		"----------\n"
	);
}
/**
 *
 */
public void test14() {
	this.runNegativeTest(
		new String[] {
			/* pack1.First */
			"pack1/First.java",
			"package pack1;										\n"+
			"public class First {								\n"+
			"	public static void something() {}				\n"+
			"		class Inner {}								\n"+	
			"	public static void main (String argv[]) {		\n"+
			"		First.Inner foo = new First().new Inner();	\n"+
			"		foo.something();							\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. ERROR in pack1\\First.java (at line 7)\n" + 
		"	foo.something();							\n" + 
		"	    ^^^^^^^^^\n" + 
		"The method something() is undefined for the type First.Inner\n" + 
		"----------\n"
	);
}
/**
 *
 */
public void test15() {
	this.runConformTest(
		new String[] {
			/* pack1.First */
			"pack1/First.java",
			"package pack1;										\n"+
			"public class First {								\n"+
			"		class Inner {								\n"+
			"			public void something() {}				\n"+
			"		}											\n"+	
			"	public static void main (String argv[]) {		\n"+
			"		First.Inner foo = new First().new Inner();	\n"+
			"		foo.something();							\n"+
			"		System.out.println(\"SUCCESS\");			\n"+
			"	}												\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test16() {
	this.runConformTest(
		new String[] {
			/* pack1.Outer */
			"pack1/Outer.java",
			"package pack1;										\n"+
			"import pack2.*;									\n"+
			"public class Outer {								\n"+
			"	int time, distance;								\n"+
			"	public Outer() {								\n"+
			"	}												\n"+
			"	public Outer(int d) {							\n"+
			"		distance = d;								\n"+
			"	}												\n"+
			"	public void aMethod() {							\n"+
			"		this.distance *= 2;							\n"+
			"		return;										\n"+
			"	}												\n"+
			"}",
			/* pack2.OuterTwo */
			"pack2/OuterTwo.java",
			"package pack2;										\n"+
			"import pack1.*;									\n"+
			"public class OuterTwo extends Outer {				\n"+
			"	public OuterTwo(int bar) {						\n"+
			"		Outer A = new Outer(3) {					\n"+
			"			public void bMethod(){					\n"+
			"				final class X {						\n"+
			"					int price;						\n"+
			"					public X(int inp) {				\n"+
			"						price = inp + 32;			\n"+
			"					}								\n"+
			"				}									\n"+
			"			}										\n"+
			"		};											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		System.out.println(\"\");					\n"+
			"		OuterTwo foo = new OuterTwo(12);			\n"+
			"		Outer bar = new Outer(8);					\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test17() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	int value;										\n"+
			"	public A(B bVal) {								\n"+
			"		bVal.sval += \"V\";							\n"+
			"	}												\n"+
			"	static class B {								\n"+
			"		public static String sval;					\n"+
			"		public void aMethod() {						\n"+
			"			sval += \"S\";							\n"+
			"			A bar = new A(this);					\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		B foo = new B();							\n"+
			"		foo.sval = \"U\";							\n"+
			"		foo.aMethod();								\n"+
			"		System.out.println(foo.sval);				\n"+
			"	}												\n"+
			"}"
		},
		"USV"
	);
}
/**
 * member class
 */
public void test18() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public class B {								\n"+
			"		String rating;								\n"+
			"		public B (A sth) {							\n"+
			"			sth.rating = \"m\";						\n"+
			"			rating = \"er\";						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating + bar.rating);\n"+
			"	}												\n"+
			"}"
		},
		"mer"
	);
}
/**
 * member class
 */
public void test19() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public void setRating(A sth, String setTo) {	\n"+
			"		sth.rating = setTo;							\n"+
			"		return;										\n"+
			"	}												\n"+
			"	public class B {								\n"+
			"		public B (A sth) {							\n"+
			"			setRating(sth, \"m\");					\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating + bar.other);	\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\A.java (at line 17)\n" + 
		"	System.out.println(foo.rating + bar.other);	\n" + 
		"	                                ^^^^^^^^^\n" + 
		"bar.other cannot be resolved or is not a field\n" + 
		"----------\n"
	);
}
/**
 * member class
 */
public void test20() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public class B {								\n"+
			"		public B (A sth) {							\n"+
			"			sth.rating = \"m\";						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating + bar.other);	\n"+
			"	}												\n"+
			"}"
		},
		"----------\n" + 
		"1. WARNING in p1\\A.java (at line 6)\n" + 
		"	sth.rating = \"m\";						\n" + 
		"	^^^^^^^^^^\n" + 
		"Write access to enclosing field A.rating is emulated by a synthetic accessor method. Increasing its visibility will improve your performance\n" + 
		"----------\n" + 
		"2. ERROR in p1\\A.java (at line 13)\n" + 
		"	System.out.println(foo.rating + bar.other);	\n" + 
		"	                                ^^^^^^^^^\n" + 
		"bar.other cannot be resolved or is not a field\n" + 
		"----------\n"
	);
}
/**
 * member class
 */
public void test21() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"public class A	{									\n"+
			"	private String rating;							\n"+
			"	public class B {								\n"+
			"		public B (A sth) {							\n"+
			"			sth.rating = \"m\";						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		A foo = new A();							\n"+
			"		foo.rating = \"o\";							\n"+
			"		B bar = foo.new B(foo);						\n"+
			"		System.out.println(foo.rating);				\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test22() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;										\n"+
			"import p2.*;										\n"+
			"public class A {									\n"+
			"	public int aValue;								\n"+
			"	public A() {}									\n"+
			"	public static class C extends A {				\n"+
			"		public String aString;						\n"+
			"		public C() {								\n"+
			"		}											\n"+
			"	}												\n"+
			"}",
			/* p2.B */
			"p2/B.java",
			"package p2;										\n"+
			"import p1.*;										\n"+
			"public class B extends A.C {						\n"+
			"	public B() {}									\n"+
			"	public class D extends A {						\n"+
			"		public D() {								\n"+
			"			C val2 = new C();						\n"+
			"			val2.aString = \"s\";					\n"+
			"			A val = new A();						\n"+
			"			val.aValue = 23;						\n"+
			"		}											\n"+
			"	}												\n"+
			"	public static void main (String argv[]) {		\n"+
			"		D foo = new B().new D();					\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test23() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;\n"+
			"public class A implements B {						\n"+
			"}													\n"+
			"interface B {										\n"+
			"	public class A implements B {					\n"+
			"		public static void main (String argv[]) {	\n"+
			"			class Ba {								\n"+
			"				int time;							\n"+
			"			}										\n"+
			"			Ba foo = new Ba();						\n"+
			"			foo.time = 3;							\n"+
			"		}											\n"+
			"		interface C {								\n"+
			"		}											\n"+
			"		interface Bb extends C {					\n"+
			"		}											\n"+
			"	}												\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test24() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;									\n"+
			"public class A {								\n"+
			"	protected static String bleh;				\n"+
			"	interface B {								\n"+
			"		public String bleh();					\n"+
			"		class C{								\n"+
			"			public String bleh() {return \"B\";}\n"+
			"		}										\n"+
			"	}											\n"+
			"	class C implements B {						\n"+
			"		public String bleh() {return \"A\";}	\n"+
			"	}											\n"+
			"	public static void main(String argv[]) {	\n"+
			"		C foo = new A().new C();				\n"+
			"	}											\n"+
			"}"
		}
	);
}
/**
 *
 */
public void test25() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"import p2.*;								\n"+
			"public class A {							\n"+
			"	public static class B {					\n"+
			"		public static int B;				\n"+
			"	}										\n"+
			"	public static void main(String argv[]) {\n"+
			"		B foo = new A.B();					\n"+
			"		B bar = new B();					\n"+
			"		foo.B = 2;							\n"+
			"		p2.B bbar = new p2.B();				\n"+
			"		if (bar.B == 35) {					\n"+
			"			System.out.println(\"SUCCESS\");\n"+
			"		}									\n"+
			"		else {								\n"+
			"			System.out.println(bar.B);		\n"+
			"		}									\n"+
			"	}										\n"+
			"}",
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B() {							\n"+
			"		A.B bleh = new A.B();				\n"+
			"		bleh.B = 35;						\n"+
			"	}										\n"+
			"}"
		},
		"SUCCESS"
	);
}
/**
 *
 */
public void test26() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"public class A {							\n"+
			"	public static class B {					\n"+
			"		protected static int B;				\n"+
			"	}										\n"+
			"	public static void main(String argv[]) {\n"+
			"		B foo = new A.B();					\n"+
			"		B bar = new B();					\n"+
			"		B.B = 2;							\n"+
			"		p2.B bbar = new p2.B();				\n"+
			"		if (B.B == 35) {					\n"+
			"			System.out.println(\"SUCCESS\");\n"+
			"		}									\n"+
			"		else {								\n"+
			"			System.out.println(B.B);		\n"+
			"		}									\n"+
			"	}										\n"+
			"}",
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B() {							\n"+
			"		A.B bleh = new A.B();				\n"+
			"		bleh.B = 35;						\n"+
			"	}										\n"+
			"}"
		},
		"----------\n" + 
		"1. ERROR in p2\\B.java (at line 6)\n" + 
		"	bleh.B = 35;						\n" + 
		"	^^^^^^\n" + 
		"The field bleh.B is not visible\n" + 
		"----------\n"
	);
}
/**
 *
 */
public void test27() {
	this.runNegativeTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;								\n"+
			"public class A {							\n"+
			"	protected static class B {				\n"+
			"		public static int B;				\n"+
			"	}										\n"+
			"	public static void main(String argv[]) {\n"+
			"		B foo = new A.B();					\n"+
			"		B bar = new B();					\n"+
			"		B.B = 2;							\n"+
			"		p2.B bbar = new p2.B();				\n"+
			"		if (B.B == 35) {					\n"+
			"			System.out.println(\"SUCCESS\");\n"+
			"		}									\n"+
			"		else {								\n"+
			"			System.out.println(B.B);		\n"+
			"		}									\n"+
			"	}										\n"+
			"}",
			"p2/B.java",
			"package p2;								\n"+
			"import p1.*;								\n"+
			"public class B extends A {					\n"+
			"	public B() {							\n"+
			"		A.B bleh = new A.B();				\n"+
			"		A.B.B = 35;						\n"+
			"	}										\n"+
			"}"
		},
		"----------\n" + 
		"1. ERROR in p2\\B.java (at line 5)\n" + 
		"	A.B bleh = new A.B();				\n" + 
		"	           ^^^^^^^^^\n" + 
		"The constructor A.B() is not visible\n" + 
		"----------\n"
	);
}
/**
 *
 */
public void test28() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"package p1;									\n"+
			"public class A {								\n"+
			"	static class B {							\n"+
			"		public static class C {					\n"+
			"			private static int a;				\n"+
			"			private int b;						\n"+
			"		}										\n"+
			"	}											\n"+
			"	class D extends B {							\n"+
			"		int j = p1.A.B.C.a;						\n"+
			"	}											\n"+
			"	public static void main (String argv[]) {	\n"+
			"		System.out.println(\"SUCCESS\");		\n"+
			"	}											\n"+
			"}"
		},
		"SUCCESS"
	);
}
public static Class testClass() {
	return LookupTest.class;
}
}
