/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeTest extends AbstractComparisonTest {
	
	public GenericTypeTest(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
//	static {
//		Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "Bug51529a", "Bug51529b" };
//		Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 308, 309 };
//		Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { 21, 50 };
//		testsRange = new int[] { -1, 50 }; // run all tests with a number less or equals to 50
//		testsRange = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
//	}
	public static Test suite() {
		if (testsNames != null || testsNumbers!=null || testsRange!=null) {
			return new RegressionTestSetup(buildTestSuite(testClass()), highestComplianceLevels());
		}
		return setupSuite(testClass());
	}

	public static Class testClass() {  
		return GenericTypeTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Tx1 extends String, Tx2 extends Comparable>  extends XS<Tx2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Integer w = new X<String,Integer>().get(new Integer(12));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class XS <Txs> {\n" + 
				"    Txs get(Txs t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Xp1 extends String, Xp2 extends Comparable>  extends XS<Xp2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Integer w = new X<String,Integer>().get(new Integer(12));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    Xp2 get(Xp2 t){\n" + 
				"        System.out.print(\"{X::get}\");\n" + 
				"        return super.get(t);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class XS <XSp1> {\n" + 
				"    XSp1 get(XSp1 t) {\n" + 
				"        System.out.print(\"{XS::get}\");\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"{X::get}{XS::get}SUCCESS");
	}
	
	// check cannot bind superclass to type variable
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <X> extends X {\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <X> extends X {\n" + 
			"	                           ^\n" + 
			"Cannot refer to the type parameter X as a supertype\n" + 
			"----------\n");
	}
	
	// check cannot bind superinterface to type variable
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <X> implements X {\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <X> implements X {\n" + 
			"	                              ^\n" + 
			"Cannot refer to the type parameter X as a supertype\n" + 
			"----------\n");
	}
	
	// check cannot bind type variable in static context
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"    T t;\n" + 
				"    static {\n" + 
				"        T s;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	T s;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
				
	// check static references to type variables
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"    T ok1;\n" + 
				"    static {\n" + 
				"        T wrong1;\n" + 
				"    }\n" + 
				"    static void foo(T wrong2) {\n" + 
				"		T wrong3;\n" + 
				"    }\n" + 
				"    class MX extends T {\n" + 
				"        T ok2;\n" + 
				"    }\n" + 
				"    static class SMX extends T {\n" + 
				"        T wrong4;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	T wrong1;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	static void foo(T wrong2) {\n" + 
			"	                ^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	T wrong3;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	class MX extends T {\n" + 
			"	                 ^\n" + 
			"Cannot refer to the type parameter T as a supertype\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	static class SMX extends T {\n" + 
			"	                         ^\n" + 
			"Cannot refer to the type parameter T as a supertype\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 14)\n" + 
			"	T wrong4;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
	
	// check static references to type variables
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"    T ok1;\n" + 
				"    static class SMX {\n" + 
				"        T wrong4;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	T wrong4;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
	
	// check static references to type variables
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"     T ok;\n" + 
				"    static T wrong;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	static T wrong;\n" + 
			"	       ^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
	
	// Object cannot be generic
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"Object.java",
				"package java.lang;\n" +
				"public class Object <T> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Object.java (at line 2)\n" + 
			"	public class Object <T> {\n" + 
			"	                     ^\n" + 
			"The type java.lang.Object cannot be declared as a generic\n" + 
			"----------\n");
	}
	
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Foo {} \n" + 
				"public class X<T extends Object & Comparable<? super T>> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Foo>();\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	new X<Foo>();\n" + 
			"	      ^^^\n" + 
			"Bound mismatch: The type Foo is not a valid substitute for the bounded parameter <T extends Object & Comparable<? super T>> of the type X<T>\n" + 
			"----------\n");
	}
	
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Object & Comparable<? super T>> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Foo>();\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	new X<Foo>();\n" + 
			"	      ^^^\n" + 
			"Foo cannot be resolved to a type\n" + 
			"----------\n");
	}
	
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    T foo(T t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = new X<String>().foo(\"SUCCESS\");\n" + 
				"        System.out.println(s);\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    T foo(T t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>().baz(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(foo(t));\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    T foo(T t) throws T {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>().baz(new EX());\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(foo(t));\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	System.out.println(foo(t));\n" + 
			"	                   ^^^^^^\n" + 
			"Unhandled exception type T\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 16)\n" + 
			"	class EX extends Exception {\n" + 
			"	      ^^\n" + 
			"The serializable class EX does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n");
	}
	public void test015() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    String foo() throws T {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>().baz(new EX());\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                try {\n" + 
				"	                System.out.println(foo());\n" + 
				"                } catch (Exception t) {\n" + 
				"                }\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    void bar(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(Exception ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Exception>().bar(new Exception());\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    void bar(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(Exception ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<IOException>().bar(new Exception());\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	new X<IOException>().bar(new Exception());\n" + 
			"	                     ^^^\n" + 
			"The method bar(IOException) in the type X<IOException> is not applicable for the arguments (Exception)\n" + 
			"----------\n");
	}
	public void test018() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T foo(T t) {\n" + 
				"        System.out.println(t);\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<XY>() {\n" + 
				"            void run() {\n" + 
				"                foo(new XY());\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" + 
				"class XY {\n" + 
				"    public String toString() {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"     private T foo(T t) {\n" + 
				"        System.out.println(t);\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<XY>() {\n" + 
				"            void run() {\n" + 
				"                foo(new XY());\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" + 
				"class XY {\n" + 
				"    public String toString() {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	foo(new XY());\n" + 
			"	^^^\n" + 
			"The method foo(T) in the type X<T> is not applicable for the arguments (XY)\n" + 
			"----------\n");
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"     void foo(Y<T> y) {\n" + 
				"		System.out.print(\"SUCC\");\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>().bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new Y<T>() {\n" + 
				"            public void pre() {\n" + 
				"                foo(this);\n" + 
				"            }\n" + 
				"        }.print(\"ESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class Y <P> {\n" + 
				"	public void print(P p) {\n" + 
				"		pre();\n" + 
				"		System.out.println(p);\n" + 
				"	}\n" + 
				"	public void pre() {\n" + 
				"	}\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	}.print(\"ESS\");\n" + 
			"	  ^^^^^\n" + 
			"The method print(T) in the type Y<T> is not applicable for the arguments (String)\n" + 
			"----------\n");
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    void foo(T t) {\n" + 
				"    }\n" + 
				"    void bar(String x) {\n" + 
				"        foo(x);\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>().foo(new Object());\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X <T extends String> {\n" + 
			"	                          ^^^^^^\n" + 
			"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	foo(x);\n" + 
			"	^^^\n" + 
			"The method foo(T) in the type X<T> is not applicable for the arguments (String)\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	new X<String>().foo(new Object());\n" + 
			"	                ^^^\n" + 
			"The method foo(String) in the type X<String> is not applicable for the arguments (Object)\n" + 
			"----------\n");
	}
	
	public void test022() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    X(T t) {\n" + 
				"        System.out.println(t);\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"       new X<String>(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    X(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(t);\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    X(final T t) throws T {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(t);\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>(new EX());\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	new X<EX>(new EX());\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type EX\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	class EX extends Exception {\n" + 
			"	      ^^\n" + 
			"The serializable class EX does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n");
	}
	
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    String foo() throws T {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>(new EX());\n" + 
				"    }\n" + 
				"    X(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                try {\n" + 
				"	                System.out.println(foo());\n" + 
				"                } catch (Exception t) {\n" + 
				"                }\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    X(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(Exception ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Exception>(new Exception());\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test027() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    X(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(Exception ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<IOException>(new Exception());\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	new X<IOException>(new Exception());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X<IOException>(Exception) is undefined\n" + 
			"----------\n");
	}
	
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = new X<String>(\"SU\").t;\n" + 
				"        System.out.print(s);\n" + 
				"        s = new X<String>(\"failed\").t = \"CC\";\n" + 
				"        System.out.print(s);\n" + 
				"        s = new X<String>(\"\").t += \"ESS\";\n" + 
				"        System.out.println(s);\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X() {\n" + 
				"    }\n" + 
				"    T foo(T a, T b) {\n" + 
				"        T s;\n" + 
				"        s = t = a;\n" + 
				"		s = t += b;\n" + 
				"		return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(new X<String>().foo(\"SUC\", \"CESS\"));\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	s = t += b;\n" + 
			"	    ^^^^^^\n" + 
			"The operator += is undefined for the argument type(s) T, T\n" + 
			"----------\n");
	}
	
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X() {\n" + 
				"    }\n" + 
				"    T foo(T a) {\n" + 
				"        T s;\n" + 
				"        s = t = a;\n" + 
				"		return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(new X<String>().foo(\"SUCCESS\"));\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}
	
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<String>(\"INNER\") {\n" + 
				"            void run() {\n" + 
				"                \n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"		                String s = t = \"SUC\";\n" + 
				"		                s = t+= \"CESS\";\n" + 
				"				        System.out.println(t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}
	
	public void test032() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<String>(\"INNER\") {\n" + 
				"            void run() {\n" + 
				"                String s = t = \"SUC\";\n" + 
				"                s = t+= \"CESS\";\n" + 
				"		        System.out.println(t);\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}
	
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E, T> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(E e){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(E) has the same erasure foo(Object) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(T t){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(T) has the same erasure foo(Object) as another method in type X<E,T>\n" + 
			"----------\n");
	}		
	
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception, T extends Exception> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(E e){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(E) has the same erasure foo(Exception) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(T t){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(T) has the same erasure foo(Exception) as another method in type X<E,T>\n" + 
			"----------\n");
	}	
	
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception, T extends Thread> {\n" + 
				"	void foo(E e, Thread t){}\n" + 
				"	void foo(Exception e, T t){}\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(E e, Thread t){}\n" + 
			"	     ^^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(E, Thread) has the same erasure foo(Exception, Thread) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(Exception e, T t){}\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(Exception, T) has the same erasure foo(Exception, Thread) as another method in type X<E,T>\n" + 
			"----------\n");
	}	
	
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception, T extends Thread> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"    public static void main(String[] args) {\n" + 
				"		 System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}			
				
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Cloneable, T extends Thread & Cloneable> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"    public static void main(String[] args) {\n" + 
				"		 System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}	
	
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Cloneable, T extends Thread & Cloneable> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X<XY,XY> x = new X<XY, XY>();\n" + 
				"		x.foo(new XY());\n" + 
				"	}\n" + 
				"}\n" + 
				"class XY extends Thread implements Cloneable {\n" + 
				"}\n" ,
			},		"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	x.foo(new XY());\n" + 
			"	  ^^^\n" + 
			"The method foo(XY) is ambiguous for the type X<XY,XY>\n" + 
			"----------\n");
	}

	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Cloneable, T extends Thread> {\n" + 
				"	void foo(L<E> l1){}\n" + 
				"	void foo(L<T> l2){}\n" + 
				"	void foo(L l){}\n" + 
				"}\n" + 
				"\n" + 
				"class L<E> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(L<E> l1){}\n" + 
			"	     ^^^^^^^^^^^^\n" + 
			"Method foo(L<E>) has the same erasure foo(L<E>) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(L<T> l2){}\n" + 
			"	     ^^^^^^^^^^^^\n" + 
			"Method foo(L<T>) has the same erasure foo(L<E>) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	void foo(L l){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Duplicate method foo(L) in type X<E,T>\n" + 
			"----------\n");
	}
	
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends X> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test041() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T, U extends T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test042() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends U, U> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <T extends U, U> {\n" + 
			"	                ^\n" + 
			"Illegal forward reference to type parameter U\n" + 
			"----------\n");
	}	
	
	public void test043() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends L<T> , U extends T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" +
				"class L<E>{}\n",
			},
			"SUCCESS");
	}	
	
	public void test044() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends L<X> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n" + 
				"class L<E> {}\n",
			},
			"SUCCESS");
	}	
	
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public Z<T> var;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public Z<T> var;\n" + 
			"	       ^\n" + 
			"Z cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public Object<T> var;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public Object<T> var;\n" + 
			"	              ^\n" + 
			"T cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test047() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    private T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new MX<String>(\"INNER\") {\n" + 
				"            void run() {\n" + 
				"                \n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"		                String s = t = \"SUC\";\n" + 
				"		                s = t+= \"CESS\";\n" + 
				"				        System.out.println(t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" + 
				"class MX<U> {\n" + 
				"    MX(U u){}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 15)\n" + 
			"	String s = t = \"SUC\";\n" + 
			"	       ^\n" + 
			"Type mismatch: cannot convert from T to String\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 15)\n" + 
			"	String s = t = \"SUC\";\n" + 
			"	               ^^^^^\n" + 
			"Type mismatch: cannot convert from String to T\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 16)\n" + 
			"	s = t+= \"CESS\";\n" + 
			"	    ^^^^^^^^^^\n" + 
			"The operator += is undefined for the argument type(s) T, String\n" + 
			"----------\n");
	}
	// Access to enclosing 't' of type 'T' (not substituted from X<X> as private thus non inherited)
	// javac finds no error/warning on this test but it should
	public void test048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    private T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        X x = t;\n" + 
				"				        System.out.println(x);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	X x = t;\n" + 
			"	  ^\n" + 
			"Type mismatch: cannot convert from T to X\n" + 
			"----------\n");
	}
	public void test049() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    public T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        X x = t;\n" + 
				"				        System.out.println(\"SUCCESS\");\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test050() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Super {class M {}}\n" + 
				"public class X <T extends M> extends Super {\n" +
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public class X <T extends M> extends Super {\n" + 
			"	                          ^\n" + 
			"M cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test051() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Super {class M {}}\n" + 
				"public class X extends Super {\n" + 
				"	class N <T extends M> {}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test052() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test053() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        print(t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test054() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        print(X.this.t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	print(X.this.t);\n" + 
			"	^^^^^\n" + 
			"The method print(X) in the type A<X> is not applicable for the arguments (T)\n" + 
			"----------\n");
	}

	public void test055() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"	  X<String> xs = new X<String>(\"SUCCESS\");\n" + 
				"	  System.out.println(xs.t);\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	
	public void test056() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"	  X<String> xs = new X<String>(\"SUCCESS\");\n" + 
				"	  System.out.println((X)xs.t);\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	System.out.println((X)xs.t);\n" + 
			"	                   ^^^^^^^\n" + 
			"Cannot cast from String to X\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\A.java (at line 7)\n" + 
			"	protected void print(P p) {\n" + 
			"	                       ^\n" + 
			"The parameter p is hiding a field from type A<P>\n" + 
			"----------\n");
	}
	
	public void test057() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"	  X<X<String>> xs = new X<X<String>>(new X<String>(\"SUCCESS\"));\n" + 
				"	  System.out.println(xs.t.t);\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n", 
			},
			"SUCCESS");
	}

	// JSR14-v10[§2.1,§2.2]: Valid multiple parameter types
	public void test058() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3> {\n" + 
					"}\n" + 
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X<String, Number, Integer> x;\n" + 
					"}\n"
			}
		);
	}
	// JSR14-v10[§2.1,§2.2]: Invalid multiple parameter types: more declared than referenced
	public void test059() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3, A4> {\n" + 
					"}\n" + 
					"// Invalid Valid Type Syntax (not enough parameters)\n" + 
					"class Y {\n" + 
					"	X<String, Number, Integer> x;\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 7)\n" + 
				"	X<String, Number, Integer> x;\n" + 
				"	^\n" + 
				"Incorrect number of arguments for type X<A1,A2,A3,A4>; it cannot be parameterized with arguments <String, Number, Integer>\n" + 
				"----------\n"
		);
	}
	// JSR14-v10[§2.1,§2.2]: Invalid multiple parameter types: more referenced than declared
	public void test060() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2> {\n" + 
					"}\n" + 
					"// Invalid Valid Type Syntax (too many parameters)\n" + 
					"class Y {\n" + 
					"	X<String, Number, Integer> x;\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 7)\n" + 
				"	X<String, Number, Integer> x;\n" + 
				"	^\n" + 
				"Incorrect number of arguments for type X<A1,A2>; it cannot be parameterized with arguments <String, Number, Integer>\n" + 
				"----------\n"
		);
	}
	// JSR14-v10[§2.1,§2.2]: Invalid multiple parameter types: primitive types
	public void test061() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3, A4, A5, A6, A7> {\n" + 
					"}\n" + 
					"// Invalid Valid Type Syntax (primitive cannot be parameters)\n" + 
					"class Y {\n" + 
					"	X<int, short, long, float, double, boolean, char> x;\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	  ^^^\n" + 
				"Syntax error on token \"int\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"2. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	       ^^^^^\n" + 
				"Syntax error on token \"short\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"3. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	              ^^^^\n" + 
				"Syntax error on token \"long\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"4. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                    ^^^^^\n" + 
				"Syntax error on token \"float\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"5. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                           ^^^^^^\n" + 
				"Syntax error on token \"double\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"6. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                                   ^^^^^^^\n" + 
				"Syntax error on token \"boolean\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"7. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                                            ^^^^\n" + 
				"Syntax error on token \"char\", Dimensions expected after this token\n" + 
				"----------\n"
		);
	}
	// JSR14-v10[§2.1,§2.2]: Valid multiple parameter types: primitive type arrays
	public void test062() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" + 
					"}\n" + 
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X<int[], short[][], long[][][], float[][][][], double[][][][][], boolean[][][][][][], char[][][][][][][], Object[][][][][][][][][]> x;\n" + 
					"}\n"
			},
			""
		);
	}
	public void test063() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends p.A<T> {\n" + 
				"    \n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X(args);\n" + 
				"        X<String> xs = new X<String>(args);\n" + 
				"	}\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	X x = new X(args);\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the constructor X(T) of raw type X. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	X<String> xs = new X<String>(args);\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor X<String>(String[]) is undefined\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p\\A.java (at line 7)\n" + 
		"	protected void print(P p) {\n" + 
		"	                       ^\n" + 
		"The parameter p is hiding a field from type A<P>\n" + 
		"----------\n");
	}
	// raw type: variable map to its strict erasure 
	public void test064() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Exception & IX> {\n" + 
				"    T t;\n" + 
				"    void bar(T t) {\n" + 
				"        t.getMessage();\n" + 
				"        t.foo();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X x = new X();\n" + // raw type
				"		x.t.getMessage();\n" + // T is strictly exception !
				"		x.t.foo();\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"interface IX {\n" + 
				"    void foo();\n" + 
				"}\n", 
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	void bar(T t) {\n" + 
		"	           ^\n" + 
		"The parameter t is hiding a field from type X<T>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	x.t.foo();\n" + 
		"	    ^^^\n" + 
		"The method foo() is undefined for the type Exception\n" + 
		"----------\n");
	}
	// raw type: assignments 
	public void test065() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"\n" + 
				"public class X<T extends Exception> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X x = new X();\n" + 
				"		X<IOException> xioe = new X<IOException>(); // ok\n" + 
				"		\n" + 
				"		X x2 = xioe;\n" + 
				"		X<IOException> xioe2 = x; // unsafe\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	X<IOException> xioe2 = x; // unsafe\n" + 
			"	                       ^\n" + 
			"Unsafe type operation: Should not convert expression of raw type X to type X<IOException>. References to generic type X<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}

	// JSR14-v10[§2.1,§2.2]: Invalid PT declaration (mix with reference)
	public void test066() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Valid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A2>> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Valid Parameterized Type Declaration\n" + 
				"class X2<A2>{\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2>> {\n" + 
			"	                              ^^\n" + 
			"A2 cannot be resolved to a type\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Invalid PT declaration (mix with reference)
	public void test067() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Valid Consecutive Parameterized Type Declaration\n" + 
				"public class X1< A1 extends X2	<	A2	>     			> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Valid Parameterized Type Declaration\n" + 
				"class X2<A2>{\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1< A1 extends X2	<	A2	>     			> {\n" + 
			"	                              	 	^^\n" + 
			"A2 cannot be resolved to a type\n" + 
			"----------\n"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	// TODO (david) diagnosis message on error 3 sounds strange, doesn't it?
	public void test068() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A2> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Invalid Parameterized Type Declaration\n" + 
				"class X2<A2 {\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2> {\n" + 
			"	                              ^^\n" + 
			"A2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2> {\n" + 
			"	                                ^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"3. ERROR in test\\X1.java (at line 7)\n" + 
			"	class X2<A2 {\n" + 
			"	         ^^\n" + 
			"Syntax error on token \"A2\", > expected after this token\n" + 
			"----------\n"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	public void test069() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A2 {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Invalid Parameterized Type Declaration\n" + 
				"class X2<A2> {\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2 {\n" + 
			"	                              ^^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test070() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1>> {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1>> {\n" + 
			"	                  ^^\n" + 
			"Syntax error on token \">>\", > expected\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Unexpected consecutive PT declaration (with spaces)
	public void test071() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1 < A1 > > {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1 < A1 > > {\n" + 
			"	                       ^\n" + 
			"Syntax error on token \">\", delete this token\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (unary right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1>>> {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1>>> {\n" + 
			"	                  ^^^\n" + 
			"Syntax error on token \">>>\", > expected\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test073() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A1>>> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Valid Parameterized Type Declaration\n" + 
				"class X2<A2> {\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A1>>> {\n" + 
			"	                                ^^^\n" + 
			"Syntax error on token \">>>\", >> expected\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Unexpected consecutive PT declaration (with spaces)
	public void test074() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1 < A1 > > > {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1 < A1 > > > {\n" + 
			"	                       ^^^\n" + 
			"Syntax error on tokens, delete these tokens\n" + 
			"----------\n"
		);
	}
	
	// A is not an interface
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Object & p.A<? super T>> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"}",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"}"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T extends Object & p.A<? super T>> extends p.A<T> {\n" + 
		"	                                   ^^^\n" + 
		"The type A<? super T> is not an interface; it cannot be specified as a bounded parameter\n" + 
		"----------\n"
		);
	}

	// A is not an interface
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Object & p.A> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"}",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <T extends Object & p.A> extends p.A<T> {\n" + 
			"	                                   ^^^\n" + 
			"The type A is not an interface; it cannot be specified as a bounded parameter\n" + 
			"----------\n"
		);
	}
	// unsafe type operation: only for constructors with signature change
	public void test077() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends p.A<T> {\n" + 
				"	 X() {\n" +
				"		super(null);\n" +
				"	}\n"+
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"    }\n" + 
				"    X(X<T> xt) {\n" + 
				"        super(xt.t);\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X();\n" + 
				"        X x1 = new X(args);\n" + 
				"        X x2 = new X(x);\n" + 
				"        X<String> xs = new X<String>(args);\n" + 
				"	}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	super(xt.t);\n" + 
			"	      ^^^^\n" + 
			"xt.t cannot be resolved or is not a field\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	X x1 = new X(args);\n" + 
			"	       ^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor X(T) of raw type X. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	X x2 = new X(x);\n" + 
			"	       ^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor X(X<T>) of raw type X. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 15)\n" + 
			"	X<String> xs = new X<String>(args);\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X<String>(String[]) is undefined\n" + 
			"----------\n");
	}	
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import p.A;\n" + 
				"public class X {\n" + 
				"    X(A<String> a, A<String> b) {\n" + 
				"    }\n" + 
				"    void foo(A<String> a) {\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X((A)null, (A)null);\n" + 
				"        A a = new A((A)null);\n" + 
				"		x.foo(a);\n" + 
				"		a.print(x);\n" + 
				"		A<String> as = new A<String>(null);\n" + 
				"		as.print(\"hello\");\n" + 
				"	}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" +
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"    protected void print(A<P> a) {\n" + 
				"        print(a.p);\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	X x = new X((A)null, (A)null);\n" + 
			"	            ^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type A to type A<String>. References to generic type A<P> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 8)\n" + 
			"	X x = new X((A)null, (A)null);\n" + 
			"	                     ^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type A to type A<String>. References to generic type A<P> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	A a = new A((A)null);\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"The constructor A(P) is not visible\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 10)\n" + 
			"	x.foo(a);\n" + 
			"	      ^\n" + 
			"Unsafe type operation: Should not convert expression of raw type A to type A<String>. References to generic type A<P> should be parameterized\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	a.print(x);\n" + 
			"	  ^^^^^\n" + 
			"The method print(P) from the type A is not visible\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 12)\n" + 
			"	A<String> as = new A<String>(null);\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor A<String>(P) is not visible\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 13)\n" + 
			"	as.print(\"hello\");\n" + 
			"	   ^^^^^\n" + 
			"The method print(P) from the type A<String> is not visible\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\A.java (at line 7)\n" + 
			"	protected void print(P p) {\n" + 
			"	                       ^\n" + 
			"The parameter p is hiding a field from type A<P>\n" + 
			"----------\n");
	}	

	// JSR14-v10[§2.4]: Valid consecutive Type Parameters Brackets
	public void test079() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String>>>> {\n" + 
					"	A a;\n" +
					"	public static void main(String[] args) {\n" + 
					"		X<X1<X2<X3<String>>>> x = new X<X1<X2<X3<String>>>>();\n" + 
					"		x.a = new X1<X2<X3<String>>>();\n" + 
					"		x.a.a1 = new X2<X3<String>>();\n" + 
					"		x.a.a1.a2 = new X3<String>();\n" + 
					"		x.a.a1.a2.a3 = \"SUCCESS\";\n" + 
					"		System.out.println(x.a.a1.a2.a3);\n" + 
					"	}\n" + 
					"}\n" + 
					"class X1<A extends X2<X3<String>>> {\n" + 
					"	A a1;\n" +
					"}\n" + 
					"class X2<A extends X3<String>> {\n" + 
					"	A a2;\n" +
					"}\n" + 
					"class X3<A> {\n" + 
					"	A a3;\n" +
					"}\n"
			},
			"SUCCESS" 
		);
	}
	// TODO (david) remove errors: insert dimension to complete array type
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String>>> {}\n" + 
					"class X1<A extends X2<X3<String>> {}\n" + 
					"class X2<A extends X3<String> {}\n" + 
					"class X3<A {}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String>>> {}\n" + 
			"	                                        ^^^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"2. ERROR in test\\X.java (at line 3)\n" + 
			"	class X1<A extends X2<X3<String>> {}\n" + 
			"	                               ^^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"3. ERROR in test\\X.java (at line 4)\n" + 
			"	class X2<A extends X3<String> {}\n" + 
			"	                            ^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"4. ERROR in test\\X.java (at line 5)\n" + 
			"	class X3<A {}\n" + 
			"	         ^\n" + 
			"Syntax error on token \"A\", > expected after this token\n" + 
			"----------\n"
		);
	}
	// TODO (david) remove errors: insert dimension to complete array type
	public void test081() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String>> {}\n" + 
					"class X1<A extends X2<X3<String> {}\n" + 
					"class X2<A extends X3<String {}\n" + 
					"class X3<A> {}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String>> {}\n" + 
			"	                                        ^^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n" + 
			"2. ERROR in test\\X.java (at line 3)\n" + 
			"	class X1<A extends X2<X3<String> {}\n" + 
			"	                               ^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n" + 
			"3. ERROR in test\\X.java (at line 4)\n" + 
			"	class X2<A extends X3<String {}\n" + 
			"	                      ^^^^^^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n"
		);
	}
	// TODO (david) remove error: insert dimension to complete array type
	public void test082() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String> {}\n" + 
					"class X1<A extends X2<X3<String {}\n" + 
					"class X2<A extends X3<String>> {}\n" + 
					"class X3<A> {}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String> {}\n" + 
			"	                                        ^\n" + 
			"Syntax error, insert \">>>\" to complete ReferenceType3\n" + 
			"----------\n" + 
			"2. ERROR in test\\X.java (at line 3)\n" + 
			"	class X1<A extends X2<X3<String {}\n" + 
			"	                         ^^^^^^\n" + 
			"Syntax error, insert \">>>\" to complete ReferenceType3\n" + 
			"----------\n"
		);
	}
	// TODO (david) remove error: insert dimension to complete array type
	public void test083() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String {}\n" + 
					"class X1<A extends X2<X3<String>>> {}\n" + 
					"class X2<A extends X3<String>> {}\n" + 
					"class X3<A> {}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String {}\n" + 
			"	                                  ^^^^^^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"2. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String {}\n" + 
			"	                                  ^^^^^^\n" + 
			"Syntax error, insert \">>>\" to complete ReferenceType3\n" + 
			"----------\n"
		);
	}
	public void test084() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    X(AX<String> a, AX<String> b) {\n" + 
				"    }\n" + 
				"    void foo(AX<String> a) {\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X((AX)null, (AX)null);\n" + 
				"        AX a = new AX((AX)null);\n" + 
				"        AX a2 = new AX(null);\n" + 
				"		x.foo(a);\n" + 
				"		a.foo(a);\n" + 
				"		a.bar(a);\n" + 
				"		AX<String> as = new AX<String>(null);\n" + 
				"		as.print(a);\n" + 
				"		as.bar(a);\n" + 
				"	}\n" + 
				"}\n" + 
				"class AX <P> {\n" + 
				"    AX(AX<P> ax){}\n" + 
				"    AX(P p){}\n" + 
				"    void print(P p){}\n" + 
				"    void foo(AX rawAx){}\n" + 
				"    void bar(AX<P> ax){}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	X x = new X((AX)null, (AX)null);\n" + 
			"	            ^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 7)\n" + 
			"	X x = new X((AX)null, (AX)null);\n" + 
			"	                      ^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 8)\n" + 
			"	AX a = new AX((AX)null);\n" + 
			"	       ^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor AX(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 9)\n" + 
			"	AX a2 = new AX(null);\n" + 
			"	        ^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor AX(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 10)\n" + 
			"	x.foo(a);\n" + 
			"	      ^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 12)\n" + 
			"	a.bar(a);\n" + 
			"	^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the method bar(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 13)\n" + 
			"	AX<String> as = new AX<String>(null);\n" + 
			"	                ^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor AX<String>(AX<String>) is ambiguous\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 14)\n" + 
			"	as.print(a);\n" + 
			"	   ^^^^^\n" + 
			"The method print(String) in the type AX<String> is not applicable for the arguments (AX)\n" + 
			"----------\n" + 
			"9. WARNING in X.java (at line 15)\n" + 
			"	as.bar(a);\n" + 
			"	       ^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<String>. References to generic type AX<P> should be parameterized\n" + 
			"----------\n");
	}		

	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        X x = (X)ax.p;\n" + 
				"        System.out.println(x);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    \n" + 
				"    P p;\n" + 
				"}\n",
			},
		"null");
	}		

	public void test086() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        ax.p = new AX<String>();\n" + 
				"        System.out.println(ax2);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	ax.p = new AX<String>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<String> to the field p of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n",
		null,
		true,
		customOptions);		
	}		
	
	public void test087() {
		Map customOptions = getCompilerOptions();
		// check no unsafe type operation problem is issued
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        AX ax3 = new AX<String>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);		
	}			
	
	public void test088() {
		Map customOptions = getCompilerOptions();
		// check no unsafe type operation problem is issued
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"     AX ax = new AX();\n" + 
				"     AX ax2 = ax.p;\n" + 
				"     AX ax3 = new AX<String>();\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);		
	}				

	public void test089() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    T q;\n" + 
				"     public static void main(String[] args) {\n" + 
				"         X<String[]> xss = new X<String[]>();\n" + 
				"         X<X<String[]>> xxs = new X<X<String[]>>();\n" + 
				"         xxs.q = xss;\n" + 
				"         System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"}\n",
			},
		"SUCCESS");
	}				

	public void test090() {
		Map customOptions = getCompilerOptions();
		// check no unsafe type operation problem is issued
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    T q;\n" + 
				"    \n" + 
				"     public static void main(String[] args) {\n" + 
				"         X<String[]> xss = new X<String[]>();\n" + 
				"         X<X<String[]>> xxs = new X<X<String[]>>();\n" + 
				"         xxs.q = xss;\n" + 
				"         System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"      void foo(X[] xs) {\n" + 
				"          xs[0] = new X<String>();\n" + 
				"     }\n" +
				"}\n",
			},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);		
	}				

	public void test091() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"      void foo(X<String>[] xs) {\n" + 
				"     }\n" +
				"}\n",
			},
			"");		
	}				

	public void test092() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"     void foo() {\n" + 
				"         X<String> xs = new X<String>(\"\");\n" + 
				"         X<String> xs2 = (X<String>) xs;\n" + 
				"         \n" + 
				"         ((X)xs).t = this;\n" + 
				"         \n" + 
				"         System.out.prinln((T) this.t);\n" + 
				"     }\n" + 
				"     public static void main(String[] args) {\n" + 
				"		new X<String>(\"SUCCESS\").foo();\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	X<String> xs2 = (X<String>) xs;\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type X<String> for expression of type X<String>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 10)\n" + 
			"	((X)xs).t = this;\n" + 
			"	        ^\n" + 
			"Unsafe type operation: Should not assign expression of type X<T> to the field t of raw type X. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 12)\n" + 
			"	System.out.prinln((T) this.t);\n" + 
			"	           ^^^^^^\n" + 
			"The method prinln(T) is undefined for the type PrintStream\n" + 
			"----------\n");
	}		

	public void test093() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = new AX();\n" + 
				"        ax.p = ax2.p;\n" + // javac reports unchecked warning, which seems a bug as no difference in between lhs and rhs types
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"SUCCESS");		
	}		
	
	// same as test001, but every type is now a SourceTypeBinding
	public void test094() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Tx1 extends S, Tx2 extends C>  extends XS<Tx2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        I w = new X<S,I>().get(new I());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class S {}\n" +
				"class I implements C<I> {}\n" + 
				"interface C<Tc> {}\n" +
				"class XS <Txs> {\n" + 
				"    Txs get(Txs t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	public void test095() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Tx1 extends S, Tx2 extends C>  extends XS<Tx2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        I w = new X<S,I>().get(new I());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class S {}\n" +
				"class I implements C {}\n" + 
				"interface C<Tc> {}\n" +
				"class XS <Txs> {\n" + 
				"    Txs get(Txs t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	public void test096() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends X {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<T> extends X {}\n" + 
			"	                          ^\n" + 
			"Cycle detected: the type X cannot extend/implement itself or one of its own member types\n" + 
			"----------\n");
	}
	public void test097() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends X<String> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<T> extends X<String> {}\n" + 
			"	                          ^\n" + 
			"Cycle detected: the type X cannot extend/implement itself or one of its own member types\n" + 
			"----------\n");
	}	
	public void test098() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        ax.p = new AX<String>();\n" + 
				"        ax.q = new AX<String>();\n" + 
				"        ax.r = new AX<Object>();\n" + 
				"        ax.s = new AX<String>();\n" + 
				"        System.out.println(ax2);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"    AX<Object> q;\n" + 
				"    AX<String> r;\n" + 
				"    BX<String> s;\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> {\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	ax.p = new AX<String>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<String> to the field p of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	ax.q = new AX<String>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<String> to the field q of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	ax.r = new AX<Object>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<Object> to the field r of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	ax.s = new AX<String>();\n" + 
		"	       ^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from AX<String> to BX\n" + 
		"----------\n",
		null,
		true,
		customOptions);		
	}				
	// wildcard bound cannot be base type
	// TODO (david) only syntax error should be related to wilcard bound being a base type. Ripple effect is severe here.
	public void test099() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X  <T extends AX<? super int>> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		AX<String> ax;\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"	void foo(X<?> x) {\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X  <T extends AX<? super int>> {\n" + 
		"	                                      ^^^\n" + 
		"Syntax error on token \"int\", Dimensions expected after this token\n" + 
		"----------\n");		
	}		

	// type parameterized with wildcard cannot appear in allocation
	public void test100() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends AX> x = new X<? extends AX>(new AX<String>());\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"    P foo() { return null; }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	X<? extends AX> x = new X<? extends AX>(new AX<String>());\n" + 
			"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Bound mismatch: The constructor X(? extends AX) of type X<? extends AX> is not applicable for the arguments (AX<String>). The wildcard parameter ? extends AX has no lower bound, and may actually be more restrictive than argument AX<String>\n" + 
			"----------\n");		
	}		


	// wilcard may not pass parameter bound check
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
				"		x.t.foo(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.println(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X <T extends String> {\n" + 
			"	                          ^^^^^^\n" + 
			"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
			"	  ^^^^^^^^^^^^\n" + 
			"Bound mismatch: The type ? extends AX is not a valid substitute for the bounded parameter <T extends String> of the type X<T>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
			"	                          ^^\n" + 
			"Bound mismatch: The type AX<String> is not a valid substitute for the bounded parameter <T extends String> of the type X<T>\n" + 
			"----------\n");		
	}		
	// unbound wildcard implicitly bound by matching parameter bounds
	public void test102() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<?> x = new X<BX<String>>(new BX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	x.t.bar(\"ESS\");\n" + 
		"	    ^^^\n" + 
		"The method bar(String) is undefined for the type ?\n" + 
		"----------\n");		
	}		
	
	public void test103() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends BX> x = new X<BX<String>>(new BX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			

	// wildcard bound check
	public void test104() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends BX> x = new X<AX<String>>(new AX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	X<? extends BX> x = new X<AX<String>>(new AX<String>());\n" + 
		"	                ^\n" + 
		"Type mismatch: cannot convert from X<AX<String>> to X<? extends BX>\n" + 
		"----------\n");		
	}			
	public void test105() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
				"		x.t.foo(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.println(p);\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	public void test106() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X<BX<String>> x = new X<BX<String>>(new BX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	// unsafe assignment thru binaries
	public void test107() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X  {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        \n" + 
				"        Iterable<String> is = new ArrayList();\n" + 
				"		is.iterator();\n" + 
				"    }\n" + 
				"}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	Iterable<String> is = new ArrayList();\n" + 
			"	                      ^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type ArrayList to type Iterable<String>. References to generic type Iterable<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);		
	}			
	// class literal: Integer.class of type Class<Integer>
	public void test108() {
	    // also ensure no unsafe type operation problem is issued (assignment to variable of type raw)
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
			"public class X {\n" + 
			"    Class k;\n" + 
			"    public static void main(String args[]) {\n" + 
			"        new X().foo();\n" + 
			"    }\n" + 
			"    void foo() {\n" + 
			"        Class c = this.getClass();\n" + 
			"        this.k = this.getClass();\n" + 
			"        this.k = Integer.class;\n" + 
			"        try {\n" + 
			"            Integer i = Integer.class.newInstance();\n" + 
			"        } catch (Exception e) {\n" + 
			"        }\n" + 
			"        System.out.println(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}\n",
			},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);		
	}	
	// parameterized interface cannot be implemented simultaneously with distinct arguments
	public void test109() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements AX<String> {}\n" + 
				"class Y extends X implements AX<Thread> {}\n" + 
				"interface AX<P> {}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X implements AX<Thread> {}\n" + 
			"	      ^\n" + 
			"The interface AX cannot be implemented simultaneously with different arguments: AX<Thread> and AX<String>\n" + 
			"----------\n");
	}		
	public void test110() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements AX {}\n" + 
				"class Y extends X implements AX<Thread> {}\n" + 
				"interface AX<P> {}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X implements AX<Thread> {}\n" + 
			"	      ^\n" + 
			"The interface AX cannot be implemented simultaneously with different arguments: AX<Thread> and AX\n" + 
			"----------\n");		
	}		
	public void test111() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements AX<Object> {}\n" + 
				"class Y extends X implements AX {}\n" + 
				"interface AX<P> {}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X implements AX {}\n" + 
			"	      ^\n" + 
			"The interface AX cannot be implemented simultaneously with different arguments: AX and AX<Object>\n" + 
			"----------\n");		
	}		
	// test member types
	public void test112() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    void foo(X<Thread>.MX<String>.MMX<X> mx) {}\n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT> {}\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(X<Thread>.MX<String>.MMX<X> mx) {}\n" + 
			"	           ^^^^^^\n" + 
			"Bound mismatch: The type Thread is not a valid substitute for the bounded parameter <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>> of the type X<T>\n" + 
			"----------\n");		
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    class MX <MT extends Comparable> {\n" + 
				"        class MMX <MMT> {}\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
			"	                                         ^^^^^^^^\n" + 
			"Bound mismatch: The type Runnable is not a valid substitute for the bounded parameter <MT extends Comparable> of the type X<T>.MX<MT>\n" + 
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT extends Comparable> {}\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
			"	                                                       ^^^^^^^^\n" + 
			"Bound mismatch: The type Iterable<String> is not a valid substitute for the bounded parameter <MMT extends Comparable> of the type X<T>.MX<MT>.MMX<MMT>\n" + 
			"----------\n");
	}			
	public void test113() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo();\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"  void foo() {\n" + 
				"		new X<String>().new MX<T>();\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}		
	public void test114() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" + 
				"  }\n" + 
				"  void foo(X<String>.MX<Thread> mx) {\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	public void test115() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" + 
				"  }\n" + 
				"  void foo(X.MX mx) {\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}		
	public void test116() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" + 
				"  }\n" + 
				"  void foo(X<?>.MX<?> mx) {\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	// test member types
	public void test117() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    public static void main(String [] args) {\n" + 
				"        \n" + 
				"        new X<X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void foo(X<X.MX.MMX>.MX<X>.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT> {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}				
	// test generic method with recursive parameter bound <T extends Comparable<? super T>>
	public void test118() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        java.util.Collections.sort(new java.util.LinkedList<String>());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	// test binary member types
	// TODO (kent) reenable once NPE is addressed (need more deferring in binary bound checks)
	public void _test119() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    public static void main(String [] args) {\n" + 
				"        \n" + 
				"        new X<X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void foo(X<X.MX.MMX>.MX<Object>.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    void foo2(X<X.MX.MMX>.MX<Iterable>.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    void foo3(X<X<X.MX.MMX>.MX<Runnable>.MMX<Iterable<String>>> mx) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT> {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");

		this.runConformTest(
			new String[] {
				"Y.java",
				"public class Y extends X {\n" + 
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false, // do not flush output
			null);		
	}			
	// test generic method
	public void test120() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T>{\n" + 
				"    public static void main(String[] args) {\n" + 
				"        \n" + 
				"        String s = new X<String>().foo(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    <U extends String> T foo (U u) {\n" + 
				"        System.out.println(u);\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// substitute array types
	public void test121() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X<String>().foo(args);\n" + 
				"	}\n" + 
				"    \n" + 
				"    void foo(T[] ts) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// generic method with most specific common supertype: U --> String
	public void test122() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X<String>().foo(args, new X<X<String>>());\n" + 
				"	}\n" + 
				"    <U> void foo(U[] us, X<X<U>> xxu) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// invalid parameterized type
	public void test123() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T<String> ts;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	T<String> ts;\n" + 
			"	^\n" + 
			"The type T is not generic; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}		
	// generic method with indirect type inference: BX<String, Thread> --> AX<W>
	public void test124() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    <W> void foo(AX<W> aw) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(new BX<String,Thread>());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<T> {\n" + 
				"}\n" + 
				"class BX<U, V> extends AX<V> {\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	// generic method with indirect type inference: CX  --> AX<W>
	public void test125() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    <W> void foo(AX<W> aw) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(new CX());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<T> {\n" + 
				"}\n" + 
				"class BX<U, V> extends AX<V> {\n" + 
				"}\n" + 
				"class CX extends BX<String, Thread> {\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// variation on test125 with typo: CX extends B instead of BX.
	public void test126() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    <W> void foo(AX<W> aw) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(new CX());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<T> {\n" + 
				"}\n" + 
				"class BX<U, V> extends AX<V> {\n" + 
				"}\n" + 
				"class CX extends B<String, Thread> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X().foo(new CX());\n" + 
			"	        ^^^\n" + 
			"The method foo(AX<W>) in the type X is not applicable for the arguments (CX)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 16)\n" + 
			"	class CX extends B<String, Thread> {\n" + 
			"	                 ^\n" + 
			"B cannot be resolved to a type\n" + 
			"----------\n");
	}			
	// 57784: test generic method
	public void test127() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        java.util.Arrays.asList(new Object[] {\"1\"});\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	// 58666: special treatment for Object#getClass declared of type: Class<? extends Object>
	// but implicitly converted to Class<? extends X> for free.
	public void test128() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"    public static void main(String[] args) {\n" + 
				"		X x = new X();\n" + 
				"		Class c1 = x.getClass();\n" + 
				"		Class<? extends X> c2 = x.getClass();\n" + 
				"		String s = \"hello\";\n" + 
				"		Class<? extends X> c3 = s.getClass();\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	Class<? extends X> c3 = s.getClass();\n" + 
			"	                   ^^\n" + 
			"Type mismatch: cannot convert from Class<? extends String> to Class<? extends X>\n" + 
			"----------\n");
	}		
	// variation on test128
	public void test129() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"		XY xy = new XY();\n" + 
				"		Class c1 = xy.getClass();\n" + 
				"		Class<? extends XY> c2 = xy.getClass();\n" + 
				"		String s = \"hello\";\n" + 
				"		Class<? extends XY> c3 = s.getClass();\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class XY extends X {\n" + 
				"    public Class <? extends Object> getClass() {\n" + 
				"        return super.getClass();\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	Class<? extends XY> c3 = s.getClass();\n" + 
			"	                    ^^\n" + 
			"Type mismatch: cannot convert from Class<? extends String> to Class<? extends XY>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	public Class <? extends Object> getClass() {\n" + 
			"	                                ^^^^^^^^^^\n" + 
			"Cannot override the final method from Object\n" + 
			"----------\n");
	}			
	// getClass on array type
	public void test130() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X[] x = new X[0];\n" + 
				"		Class<? extends X[]> c = x.getClass();\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}			
	// 58979
	public void test131() {
		this.runNegativeTest(
			new String[] {
				"ArrayList.java",
				" interface List<T> {\n" + 
				"	 List<T> foo();\n" + 
				"}\n" + 
				"\n" + 
				" class ArrayList<T> implements List<T> {\n" + 
				"	public List<T> foo() {\n" + 
				"		List<T> lt = this;\n" + 
				"		lt.bar();\n" + 
				"		return this;\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in ArrayList.java (at line 8)\n" + 
			"	lt.bar();\n" + 
			"	   ^^^\n" + 
			"The method bar() is undefined for the type List<T>\n" + 
			"----------\n");
	}
	public void test132() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  <T extends X<W>.Z> foo() {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<T extends X<W>.Z> foo() {}\n" + 
			"	             ^\n" + 
			"W cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	<T extends X<W>.Z> foo() {}\n" + 
			"	                   ^^^^^\n" + 
			"Return type for the method is missing\n" + 
			"----------\n");
	}
	// bridge method
	public void test133() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new Y();\n" + 
				"        System.out.println(x.foo());\n" + 
				"    }\n" + 
				"   T foo() {return null;}\n" + 
				"   void foo(T t) {}\n" + 
				"}\n" + 
				"class Y extends X<Object> {\n" + 
				"    String foo() {return \"SUCCESS\";}\n" + 
				"    void foo(String s) {}\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	public void test134() {
		this.runConformTest(
			new String[] {
				"Z.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public class Z <T extends List> { \n" + 
				"    T t;\n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ArrayList>().set(new ArrayList<String>()));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    Z<T> set(T t) {\n" + 
				"        this.t = t;\n" + 
				"        return this;\n" + 
				"    }\n" + 
				"    T get() { \n" + 
				"        return this.t; \n" + 
				"    }\n" + 
				"    \n" + 
				"    static void foo(Z<? super ArrayList> za) {\n" + 
				"        za.get().isEmpty();\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	public void test135() {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z <T extends ZA> { \n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ZA>());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    static void foo(Z<? super String> zs) {\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class ZA {\n" + 
				"    void foo() {}\n" + 
				"}\n" + 
				"\n" + 
				"class ZB extends ZA {\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in Z.java (at line 3)\n" + 
			"	foo(new Z<ZA>());\n" + 
			"	^^^\n" + 
			"The method foo(Z<? super String>) in the type Z<T> is not applicable for the arguments (Z<ZA>)\n" + 
			"----------\n" + 
			"2. ERROR in Z.java (at line 6)\n" + 
			"	static void foo(Z<? super String> zs) {\n" + 
			"	                  ^^^^^^^^^^^^^^\n" + 
			"Bound mismatch: The type ? super String is not a valid substitute for the bounded parameter <T extends ZA> of the type Z<T>\n" + 
			"----------\n");
	}
	public void test136() {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z <T extends ZB> { \n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ZB>());\n" + 
				"    }\n" + 
				"    static void foo(Z<? super ZA> zs) {\n" + 
				"        zs.foo();\n" + 
				"    }\n" + 
				"}\n" + 
				"class ZA {\n" + 
				"}\n" + 
				"class ZB extends ZA {\n" + 
				"    void foo() {}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in Z.java (at line 3)\n" + 
			"	foo(new Z<ZB>());\n" + 
			"	^^^\n" + 
			"The method foo(Z<? super ZA>) in the type Z<T> is not applicable for the arguments (Z<ZB>)\n" + 
			"----------\n" + 
			"2. ERROR in Z.java (at line 5)\n" + 
			"	static void foo(Z<? super ZA> zs) {\n" + 
			"	                  ^^^^^^^^^^\n" + 
			"Bound mismatch: The type ? super ZA is not a valid substitute for the bounded parameter <T extends ZB> of the type Z<T>\n" + 
			"----------\n" + 
			"3. ERROR in Z.java (at line 6)\n" + 
			"	zs.foo();\n" + 
			"	   ^^^\n" + 
			"The method foo(Z<? super ZA>) in the type Z<? super ZA> is not applicable for the arguments ()\n" + 
			"----------\n");
	}
	public void test137() {
		this.runConformTest(
			new String[] {
				"Z.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public class Z <T extends List> { \n" + 
				"    T t;\n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ArrayList>().set(new ArrayList<String>()));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    Z<T> set(T t) {\n" + 
				"        this.t = t;\n" + 
				"        return this;\n" + 
				"    }\n" + 
				"    T get() { \n" + 
				"        return this.t; \n" + 
				"    }\n" + 
				"    \n" + 
				"    static void foo(Z<? extends ArrayList> za) {\n" + 
				"        za.get().isEmpty();\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	// unbound wildcard still remembers its variable bound: Z<?> behaves like Z<AX>
	public void test138() {
		this.runConformTest(
			new String[] {
				"Z.java",
				"public class Z <T extends AX> {\n" + 
				"    T t;\n" + 
				"    Z(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		 Z<AX<String>> zax = new Z<AX<String>>(new AX<String>());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    void baz(Z<?> zu){\n" + 
				"        zu.t.foo(null);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	// extending wildcard considers its bound prior to its corresponding variable
	public void test139() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    T get() {\n" + 
				"        return this.t;\n" + 
				"    }\n" + 
				"    void bar(X<? extends BX> x) {\n" + 
				"        x.get().afoo();\n" + 
				"        x.get().bfoo();\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX {\n" + 
				"    void afoo() {}\n" + 
				"}\n" + 
				"class BX {\n" + 
				"    void bfoo() {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	void bar(X<? extends BX> x) {\n" + 
			"	           ^^^^^^^^^^^^\n" + 
			"Bound mismatch: The type ? extends BX is not a valid substitute for the bounded parameter <T extends AX> of the type X<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	x.get().afoo();\n" + 
			"	        ^^^^\n" + 
			"The method afoo() is undefined for the type ? extends BX\n" + 
			"----------\n");
	}		
	// extending wildcard considers its bound prior to its corresponding variable
	public void test140() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    T get() {\n" + 
				"        return this.t;\n" + 
				"    }\n" + 
				"    void bar(X<? extends BX> x) {\n" + 
				"        x.get().afoo();\n" + 
				"        x.get().bfoo();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"class AX {\n" + 
				"    void afoo() {}\n" + 
				"}\n" + 
				"class BX extends AX {\n" + 
				"    void bfoo() {}\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	// super wildcard considers its variable for lookups
	public void test141() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    T get() {\n" + 
				"        return this.t;\n" + 
				"    }\n" + 
				"    void bar(X<? super BX> x) {\n" + 
				"        x.get().afoo();\n" + 
				"        x.get().bfoo();\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX {\n" + 
				"    void afoo() {}\n" + 
				"}\n" + 
				"class BX extends AX {\n" + 
				"    void bfoo() {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	x.get().bfoo();\n" + 
			"	        ^^^^\n" + 
			"The method bfoo() is undefined for the type ? super BX\n" + 
			"----------\n");
	}		
	public void test142() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    T get() {\n" + 
				"        return this.t;\n" + 
				"    }\n" + 
				"    void bar(X<? extends X> x) {\n" + 
				"        x = identity(x);\n" + 
				"    }\n" + 
				"    <P extends AX> X<P> identity(X<P> x) {\n" + 
				"        return x;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX {\n" + 
				"    void afoo() {}\n" + 
				"}\n" + 
				"class BX extends AX {\n" + 
				"    void bfoo() {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	void bar(X<? extends X> x) {\n" + 
			"	           ^^^^^^^^^^^\n" + 
			"Bound mismatch: The type ? extends X is not a valid substitute for the bounded parameter <T extends AX> of the type X<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	x = identity(x);\n" + 
			"	    ^^^^^^^^\n" + 
			"Bound mismatch: The generic method identity(X<P>) of type X<T> is not applicable for the arguments (X<? extends X>) since the type ? extends X is not a valid substitute for the bounded parameter <P extends AX>\n" + 
			"----------\n");
	}			
	public void test143() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Class<? extends X> xx = null;\n" + 
				"        Class<? extends Object> xo = xx;\n" + 
				"        Class<Object> xo2 = xx;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	Class<Object> xo2 = xx;\n" + 
			"	              ^^^\n" + 
			"Type mismatch: cannot convert from Class<? extends X> to Class<Object>\n" + 
			"----------\n");
	}			
	public void test144() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Class<? extends X> xx = null;\n" + 
				"        Class<? extends Object> xo = xx;\n" + 
				"        X x = get(xx);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    static <P> P get(Class<P> cp) {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	// 59641: check assign/invoke with wildcards
	public void test145() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		XList<?> lx = new XList<X>();\n" + 
				"		X x = lx.get();\n" + 
				"		lx.add(null);\n" + 
				"		lx.add(x);\n" + 
				"		lx.slot = x;\n" + 
				"		lx.addAll(lx);\n" + 
				"    }    	\n" + 
				"}\n" + 
				"class XList<E extends X> {\n" + 
				"    E slot;\n" + 
				"    void add(E e) {}\n" + 
				"    E get() { return null; \n" + 
				"    }\n" + 
				"    void addAll(XList<E> le) {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	lx.add(x);\n" + 
			"	^^^^^^^^^\n" + 
			"Bound mismatch: The method add(?) of type XList<?> is not applicable for the arguments (X). The wildcard parameter ? has no lower bound, and may actually be more restrictive than argument X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	lx.slot = x;\n" + 
			"	          ^\n" + 
			"Bound mismatch: Cannot assign expression of type X to wildcard type ?. The wildcard type has no lower bound, and may actually be more restrictive than expression type\n" + 
			"----------\n");
	}
	// 59628
	public void test146() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.AbstractList;\n" + 
				"public class X extends AbstractList {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    public int size() { return 0; }\n" + 
				"    public Object get(int index) { return null; }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	// 59723
	public void test147() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    char[][] tokens = new char[0][];\n" + 
				"	    ArrayList list = new ArrayList();\n" + 
				"		list.toArray(tokens);\n" + 
				"      System.out.println(\"SUCCESS\");\n" + 
				"    }    	\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	// bridge method
	public void test148() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends AX<String>{\n" + 
				"    \n" + 
				"    String foo(String s) {\n" + 
				"        System.out.println(s);\n" + 
				"        return s;\n" + 
				"    }\n" + 
				"	public static void main(String[] args) {\n" + 
				"	   new X().bar(\"SUCCESS\");\n" + 
				"    }    	\n" + 
				"}\n" + 
				"class AX<T> {\n" + 
				"    T foo(T t) {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"    void bar(T t) {\n" + 
				"        foo(t);\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	// method compatibility
	public void test149() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public abstract class X implements java.util.Collection {\n" + 
				"	public Object[] toArray(Object[] a) {\n" + 
				"		return a;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	   System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X implements java.util.Collection<Object> {\n" + 
				"	public Object[] toArray(Object[] a) {\n" + 
				"		return a;\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public Object[] toArray(Object[] a) {\n" + 
			"	       ^^^^^^^^\n" + 
			"Unsafe type operation: The return type Object[] of the method toArray(Object[]) of type X needs unchecked conversion to conform to the return type T[] of inherited method\n" + 
			"----------\n");
	}			
	public void test150() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"    \n" + 
				"    <T extends X> void foo(T[] ta, List<T> lt) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(args, new ArrayList<String>());\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X().foo(args, new ArrayList<String>());\n" + 
			"	        ^^^\n" + 
			"Bound mismatch: The generic method foo(T[], List<T>) of type X is not applicable for the arguments (String[], List<String>) since the type String is not a valid substitute for the bounded parameter <T extends X>\n" + 
			"----------\n");
	}			
	public void test151() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X <E>{\n" + 
				"    \n" + 
				"    <T extends X> X(T[] ta, List<T> lt) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X<Object>(args, new ArrayList<String>());\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X<Object>(args, new ArrayList<String>());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Bound mismatch: The generic constructor X(T[], List<T>) of type X<E> is not applicable for the arguments (String[], List<String>) since the type String is not a valid substitute for the bounded parameter <T extends X>\n" + 
			"----------\n");
	}
	// 60556
	public void test152() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    List<X> x(List<X> list) {\n" + 
				"        return Collections.unmodifiableList(list);\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	public void test153() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<X> a = bar(ax);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(AX<? extends T> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	public void test154() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<X> a = bar(ax);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(AX<? super T> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	public void test155() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<X> a = bar(ax);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(AX<?> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	public void test156() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<X> a = bar(ax);\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(AX<?> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E extends X> {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	public static <T> AX<T> bar(AX<?> a) {\n" + 
			"	                     ^\n" + 
			"Bound mismatch: The type T is not a valid substitute for the bounded parameter <E extends X> of the type AX<E>\n" + 
			"----------\n");
	}			
	public void test157() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<String> as = new AX<String>();\n" + 
				"        AX<X> a = bar(ax, as);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T,U> AX<T> bar(AX<? extends U> a, AX<? super U> b) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	AX<X> a = bar(ax, as);\n" + 
			"	          ^^^\n" + 
			"The method bar(AX<? extends U>, AX<? super U>) in the type X is not applicable for the arguments (AX<X>, AX<String>)\n" + 
			"----------\n");
	}
	public void test158() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<String> as = new AX<String>();\n" + 
				"        AX<X> a = bar(ax, as);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T,U> AX<T> bar(AX<?> a, AX<? super U> b) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	public void test159() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>(new X());\n" + 
				"        AX<String> as = new AX<String>(\"SUCCESS\");\n" + 
				"        AX<X> a = bar(ax, as);\n" + 
				"	}\n" + 
				"    public static <T,U> T bar(AX<?> a, AX<? super U> b) {\n" + 
				"		return a.get();\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"	 E e;\n" +
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from ? to T\n" + 
			"----------\n");
	}		
	public void test160() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = foo(new AX<String>(\"aaa\"));\n" + 
				"	}\n" + 
				"    static <V> V foo(AX<String> a) {\n" + 
				"        return a.get();\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to V\n" + 
			"----------\n");
	}		
	public void test161() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        boolean b = foo(new AX<String>(\"aaa\")).equals(args);\n" + 
				"	}\n" + 
				"    static <V> V foo(AX<String> a) {\n" + 
				"        return a.get();\n" + 
				"    }\n" + 
				"    String bar() {\n" + 
				"        return \"bbb\";\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to V\n" + 
			"----------\n");
	}		
	public void test162() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = foo(new AX<String>(\"aaa\")).bar();\n" + 
				"	}\n" + 
				"    static <V> V foo(AX<String> a) {\n" + 
				"        return a.get();\n" + 
				"    }\n" + 
				"    String bar() {\n" + 
				"        return \"bbb\";\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	String s = foo(new AX<String>(\"aaa\")).bar();\n" + 
			"	                                      ^^^\n" + 
			"The method bar() is undefined for the type V\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to V\n" + 
			"----------\n");
	}		
	public void test163() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = foo(new AX<String>(\"aaa\")).bar();\n" + 
				"	}\n" + 
				"    static <V> V foo(AX<String> a) {\n" + 
				"        return a.get();\n" + 
				"    }\n" + 
				"    String bar() {\n" + 
				"        return \"bbb\";\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	String s = foo(new AX<String>(\"aaa\")).bar();\n" + 
			"	                                      ^^^\n" + 
			"The method bar() is undefined for the type V\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to V\n" + 
			"----------\n");
	}		
	public void test164() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" + 
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new AX<String>(\"SUCCESS\"));\n" + 
				"	}\n" + 
				"    static <V> List<V> foo(AX<String> a) {\n" + 
				"        System.out.println(a.get());\n" + 
				"        List<V> v = null;\n" + 
				"        if (a == null) v = foo(a); \n" + 
				"        return v;\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	public void test165() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>();\n" + 
				"        AX<String> a = bar(ax);\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(AX<?> a) {\n" + 
				"		 if (a == null) {\n" +
				"        	AX<String> as = bar(a);\n" + 
				"        	String s = as.get();\n" + 
				"		}\n" +
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E get() { return null; }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	public void test166() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>(new X());\n" + 
				"        AX<String> a = bar(ax, true);\n" + 
				"        String s = a.get();\n" + 
				"        System.out.println(s);\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(AX<?> a, boolean recurse) {\n" + 
				"        if (recurse) {\n" + 
				"	        AX<String> as = bar(a, false);\n" + 
				"			String s = as.get();\n" + 
				"        }\n" + 
				"		return new AX(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	public void test167() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<String, Thread> a = bar();\n" + 
				"        String s = a.get();\n" + 
				"        System.out.println(s);\n" + 
				"	}\n" + 
				"    public static <T, U> AX<T, U> bar() {\n" + 
				"		return new AX(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E, F> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"SUCCESS");
	}		
	public void test168() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<String, Thread> a = bar();\n" + 
				"        String s = a.get();\n" + 
				"        System.out.println(s);\n" + 
				"	}\n" + 
				"    public static <T, U> AX<AX<T, T>, U> bar() {\n" + 
				"		return new AX(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E, F> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	AX<String, Thread> a = bar();\n" + 
			"	                   ^\n" + 
			"Type mismatch: cannot convert from AX<AX<T,T>,Thread> to AX<String,Thread>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	return new AX(\"SUCCESS\");\n" + 
			"	       ^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor AX(E) of raw type AX. References to generic type AX<E,F> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 9)\n" + 
			"	return new AX(\"SUCCESS\");\n" + 
			"	       ^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<AX<T,T>,U>. References to generic type AX<E,F> should be parameterized\n" + 
			"----------\n");
	}		
	public void test169() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<String> a = bar(new X());\n" + 
				"        String s = a.get();\n" + 
				"        System.out.println(s);\n" + 
				"	}\n" + 
				"    public static <T> AX<T> bar(T t) {\n" + 
				"		return new AX(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"    E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	AX<String> a = bar(new X());\n" + 
			"	           ^\n" + 
			"Type mismatch: cannot convert from AX<X> to AX<String>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	return new AX(\"SUCCESS\");\n" + 
			"	       ^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor AX(E) of raw type AX. References to generic type AX<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 9)\n" + 
			"	return new AX(\"SUCCESS\");\n" + 
			"	       ^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<T>. References to generic type AX<E> should be parameterized\n" + 
			"----------\n");
	}
	// Expected type inference for cast operation
	public void test170() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>(new X());\n" + 
				"        AX<String> as = new AX<String>(\"\");\n" + 
				"        ax = (AX)bar(ax);\n" + // shouldn't complain about unnecessary cast
				"	}\n" + 
				"    public static <T> T bar(AX<?> a) {\n" + 
				"		return a.get();\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"	 E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	ax = (AX)bar(ax);\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type AX to type AX<X>. References to generic type AX<E> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from ? to T\n" + 
			"----------\n");
	}
	// Expected type inference for cast operation
	public void test171() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>(new X());\n" + 
				"        AX<String> as = new AX<String>(\"\");\n" + 
				"        ax = (AX<X>)bar(ax);\n" + // should still complain about unnecessary cast as return type inference would have
				"	}\n" +                                         // worked the same without the cast due to assignment
				"    public static <T> T bar(AX<?> a) {\n" + 
				"		return a.get();\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"	 E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	ax = (AX<X>)bar(ax);\n" + 
			"	     ^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type AX<X> for expression of type AX<X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	return a.get();\n" + 
			"	       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from ? to T\n" + 
			"----------\n");
	}
	// Expected type inference for cast operation
	public void test172() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX<X> ax = new AX<X>(new X());\n" + 
				"        AX<String> as = new AX<String>(\"SUCCESS\");\n" + 
				"        ax = (AX<X>)bar(ax);\n" + // no warn for unsafe cast, since forbidden cast
				"	}\n" + 
				"    public static <T> String bar(AX<?> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"	 E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	ax = (AX<X>)bar(ax);\n" + 
			"	     ^^^^^^^^^^^^^^\n" + 
			"Cannot cast from String to AX<X>\n" + 
			"----------\n");
	}
	// Expected type inference for return statement
	public void test173() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"    	foo();\n" + 
				"    	System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T> T bar(AX<?> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"    public static AX<X> foo() {\n" + 
				"        AX<X> ax = new AX<X>(new X());\n" + 
				"       return bar(ax);\n" + // use return type of enclosing method for type inference
				"    }\n" + 
				"}\n" + 
				"class AX<E> {\n" + 
				"	 E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n" + 
				"\n"
			},
			"SUCCESS");
	}	
	// Expected type inference for field declaration
	public void test174() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"    	Object o = foo;\n" + 
				"    	System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    public static <T> T bar(AX<?> a) {\n" + 
				"		return null;\n" + 
				"    }    \n" + 
				"    static AX<X> foo = bar(new AX<X>(new X()));\n" + // use field type for type inference
				"}\n" + 
				"class AX<E> {\n" + 
				"	 E e;\n" + 
				"    AX(E e) { this.e = e; }\n" + 
				"    E get() { return this.e; }\n" + 
				"}\n" + 
				"\n"
			},
			"SUCCESS");
	}		
	// 60563
	public void test175() {
		this.runConformTest(
			new String[] {
				"X.java",
				"    interface A<T> {\n" + 
				"        T[] m1(T x);                          \n" + 
				"    }\n" + 
				"    public class X { \n" + 
				"    	public static void main(String[] args) {\n" + 
				"			new X().m2(new A<X>(){ \n" + 
				"				public X[] m1(X x) { \n" + 
				"					System.out.println(\"SUCCESS\");\n" + 
				"					return null;\n" + 
				"				}\n" + 
				"			});\n" + 
				"		}\n" + 
				"        void m2(A<X> x) { \n" + 
				"            m3(x.m1(new X())); \n" + 
				"        }\n" + 
				"        void m3(X[] x) {\n" + 
				"        }                    \n" + 
				"    }\n"
			},
			"SUCCESS");
	}
	// unsafe raw return value
	public void test176() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"    <T> Vector<T> valuesOf(Hashtable<?, T> h) {\n" + 
				"        return new Vector();\n" + 
				"    }\n" + 
				"    Vector<Object> data;\n" + 
				"    \n" + 
				"    public void t() {\n" + 
				"        Vector<Object> v = (Vector<Object>) data.elementAt(0);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	return new Vector();\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type Vector to type Vector<T>. References to generic type Vector<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 10)\n" + 
			"	Vector<Object> v = (Vector<Object>) data.elementAt(0);\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from Object to parameterized type Vector<Object> will not check conformance of type arguments at runtime\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// cast to type variable allowed, can be diagnosed as unnecessary
	public void test177() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"	\n" + 
				"	T foo(T t) {\n" + 
				"		return (T) t;\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	return (T) t;\n" + 
			"	       ^^^^^\n" + 
			"Unnecessary cast to type T for expression of type T\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// reject instanceof type variable or parameterized type
	public void test178() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"	\n" + 
				"	T foo(T t) {\n" + 
				"		if (t instanceof X<T>) {\n" + 
				"			return t;\n" + 
				"		} else if (t instanceof X<String>) {\n" + 
				"			return t;\n" + 
				"		} else 	if (t instanceof T) {\n" + 
				"			return t;\n" + 
				"		} else if (t instanceof X) {\n" + 
				"			return t;\n" + 
				"		}\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	if (t instanceof X<T>) {\n" + 
			"	    ^^^^^^^^^^^^^^\n" + 
			"Cannot perform instanceof check against parameterized type X<T>. Use instead its raw form X since generic type information will be erased at runtime\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	} else if (t instanceof X<String>) {\n" + 
			"	           ^^^^^^^^^^^^^^\n" + 
			"Cannot perform instanceof check against parameterized type X<String>. Use instead its raw form X since generic type information will be erased at runtime\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	} else 	if (t instanceof T) {\n" + 
			"	       	    ^^^^^^^^^^^^^^\n" + 
			"Cannot perform instanceof check against type parameter T. Use instead its erasure Object since generic type information will be erased at runtime\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// 61507
	public void test179() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class U {\n" + 
				" static <T> T notNull(T t) { return t; }\n" + 
				"}\n" + 
				"public class X {\n" + 
				" void t() {\n" + 
				"  String s = U.notNull(null);\n" + 
				" }\n" + 
				" public static void main(String[] args) {\n" + 
				"	new X().t();\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}	
	public void test180() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class U {\n" + 
				" static <T> T notNull(T t) { return t; }\n" + 
				"}\n" + 
				"public class X {\n" + 
				" void t() {\n" + 
				"  String s = U.notNull(\"\");\n" + 
				" }\n" + 
				" public static void main(String[] args) {\n" + 
				"	new X().t();\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}	
	// 61507 - variation computing most specific type with 'null'
	public void test181() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class U {\n" + 
				" static <T> T notNull(T t, V<T> vt) { return t; }\n" + 
				"}\n" + 
				"class V<T> {}\n" + 
				"\n" + 
				"public class X {\n" + 
				" void t() {\n" + 
				"  String s = U.notNull(null, new V<String>());\n" + 
				" }\n" + 
				" public static void main(String[] args) {\n" + 
				"	new X().t();\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test182() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<E> {\n" + 
				"    X<E> foo() {\n" + 
				"    	return (X<E>) this;\n" + 
				"    }\n" + 
				"    X<String> bar() {\n" + 
				"    	return (AX<String>) new X<String>();\n" + 
				"    }\n" + 
				"    X<String> bar(Object o) {\n" + 
				"    	return (AX<String>) o;\n" + 
				"    }\n" + 
				"    X<E> foo(Object o) {\n" + 
				"    	return (AX<E>) o;\n" + 
				"    }    \n" + 
				"    X<E> baz(Object o) {\n" + 
				"    	return (AX<E>) null;\n" + 
				"    }\n" + 
				"    X<String> baz2(BX bx) {\n" + 
				"    	return (X<String>) bx;\n" + 
				"    }    \n" + 
				"}\n" + 
				"class AX<F> extends X<F> {}\n" + 
				"class BX extends AX<String> {}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	return (X<E>) this;\n" + 
			"	       ^^^^^^^^^^^\n" + 
			"Unnecessary cast to type X<E> for expression of type X<E>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 9)\n" + 
			"	return (AX<String>) o;\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from Object to parameterized type AX<String> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 12)\n" + 
			"	return (AX<E>) o;\n" + 
			"	       ^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from Object to parameterized type AX<E> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 15)\n" + 
			"	return (AX<E>) null;\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type AX<E> for expression of type null\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 18)\n" + 
			"	return (X<String>) bx;\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type X<String> for expression of type BX\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	public void test183() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	\n" + 
				"	{\n" + 
				"		Dictionary<String, Integer> d;\n" + 
				"		Object o;\n" + 
				"		\n" + 
				"		Object a1 = (Hashtable<String,Integer>) d;\n" + 
				"		Object a2 = (Hashtable) o;\n" + 
				"\n" + 
				"		Object a3 = (Hashtable<Float, Double>) d;\n" + 
				"		Object a4 = (Hashtable<String,Integer>) o;\n" + 
				"		\n" + 
				"		abstract class Z1 extends Hashtable<String,Integer> {\n" + 
				"		}\n" + 
				"		Z1 z1;\n" + 
				"		Object a5 = (Hashtable<String,Integer>) z1;\n" + 
				"\n" + 
				"		abstract class Z2 extends Z1 {\n" + 
				"		}\n" + 
				"		Object a6 = (Z2) z1;\n" + 
				"\n" + 
				"		abstract class Z3 extends Hashtable {\n" + 
				"		}\n" + 
				"		Z3 z3;\n" + 
				"		Object a7 = (Hashtable<String,Integer>) z3;\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	Object a3 = (Hashtable<Float, Double>) d;\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot cast from Dictionary<String,Integer> to Hashtable<Float,Double>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	Object a4 = (Hashtable<String,Integer>) o;\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from Object to parameterized type Hashtable<String,Integer> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 18)\n" + 
			"	Object a5 = (Hashtable<String,Integer>) z1;\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type Hashtable<String,Integer> for expression of type Z1\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 27)\n" + 
			"	Object a7 = (Hashtable<String,Integer>) z3;\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from Z3 to parameterized type Hashtable<String,Integer> will not check conformance of type arguments at runtime\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// 62292 - parameterized message send
	public void test184() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	static <T, U> T foo(T t, U u) {\n" + 
				"		return t;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(X.<String,X>foo(\"SUCCESS\", null));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	// parameterized message send - variation on 184 with non-static generic method
	public void test185() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	<T, U> T foo(T t, U u) {\n" + 
				"		return t;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new X().<String,X>foo(\"SUCCESS\", null));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}			
	// message send parameterized with type not matching parameter bounds
	public void test186() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	<T, U extends String> T foo(T t, U u) {\n" + 
				"		return t;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new X().<String, X>foo(\"SUCCESS\", null));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	<T, U extends String> T foo(T t, U u) {\n" + 
			"	              ^^^^^^\n" + 
			"The type parameter U should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(new X().<String, X>foo(\"SUCCESS\", null));\n" + 
			"	                                      ^^^\n" + 
			"Bound mismatch: The generic method foo(T, U) of type X is not applicable for the arguments (String, X) since the type X is not a valid substitute for the bounded parameter <U extends String>\n" + 
			"----------\n");
	}			
	// invalid type argument arity for parameterized message send
	public void test187() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	<T, U extends String> T foo(T t, U u) {\n" + 
				"		return t;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	<T, U extends String> T foo(T t, U u) {\n" + 
			"	              ^^^^^^\n" + 
			"The type parameter U should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" + 
			"	                                   ^^^\n" + 
			"Incorrect number of type arguments for generic method <T, U>foo(T, U) of type X; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}				
	// parameterized invocation of non generic method with incorrect argument count
	public void test188() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	void foo() {\n" + 
				"		return;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(new X().<String>foo(\"SUCCESS\", null));\n" + 
			"	                                   ^^^\n" + 
			"The method foo() in the type X is not applicable for the arguments (String, null)\n" + 
			"----------\n");
	}
	// parameterized invocation of non generic method
	public void test189() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	void foo() {\n" + 
				"		return;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new X().<String>foo());\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(new X().<String>foo());\n" + 
			"	                                   ^^^\n" + 
			"The method foo() of type X is not generic; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}		
	// parameterized allocation
	public void test190() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public <T> X(T t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new <String>X(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}		
	// parameterized allocation - wrong arity
	public void test191() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public <T> X(T t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new <String, String>X(\"FAILED\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	new <String, String>X(\"FAILED\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incorrect number of type arguments for generic constructor <T>X(T) of type X; it cannot be parameterized with arguments <String, String>\n" + 
			"----------\n");
	}			
	// parameterized allocation - non generic target constructor
	public void test192() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public X(String t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new <String>X(\"FAILED\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	new <String>X(\"FAILED\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X(String) of type X is not generic; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}			
	// parameterized allocation - argument type mismatch
	public void test193() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public <T> X(T t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new <String>X(new X(null));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	new <String>X(new X(null));\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The parameterized constructor <String>X(String) of type X is not applicable for the arguments (X)\n" + 
			"----------\n");
	}			
	// parameterized invocation - argument type mismatch
	public void test194() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	<T> void foo(T t) {\n" + 
				"		return;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new X().<String>foo(new X()));\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	System.out.println(new X().<String>foo(new X()));\n" + 
			"	                                   ^^^\n" + 
			"The parameterized method <String>foo(String) of type X is not applicable for the arguments (X)\n" + 
			"----------\n");
	}
	// parameterized qualified allocation
	public void test195() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public class MX {\n" +
				"		public <T> MX(T t){\n" + 
				"			System.out.println(t);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().new <String>MX(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}		
	// parameterized qualified allocation - wrong arity
	public void test196() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public class MX {\n" +
				"		public <T> MX(T t){\n" + 
				"			System.out.println(t);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().new <String,String>MX(\"FAILED\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X().new <String,String>MX(\"FAILED\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incorrect number of type arguments for generic constructor <T>MX(T) of type X.MX; it cannot be parameterized with arguments <String, String>\n" + 
			"----------\n");
	}			
	// parameterized qualified allocation - non generic target constructor
	public void test197() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public class MX {\n" +
				"		public MX(String t){\n" + 
				"			System.out.println(t);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().new <String>MX(\"FAILED\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X().new <String>MX(\"FAILED\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor MX(String) of type X.MX is not generic; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}			
	// parameterized qualified allocation - argument type mismatch
	public void test198() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public class MX {\n" +
				"		public <T>MX(T t){\n" + 
				"			System.out.println(t);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().new <String>MX(new X());\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X().new <String>MX(new X());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The parameterized constructor <String>MX(String) of type X.MX is not applicable for the arguments (X)\n" + 
			"----------\n");
	}			
	// parameterized explicit constructor call
	public void test199() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public <T> X(T t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		class Local extends X {\n" + 
				"			Local() {\n" +
				"				<String>super(\"SUCCESS\");\n" + 
				"			}\n" + 
				"		};\n" + 
				"		new Local();\n" +				
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}		
	// parameterized explicit constructor call - wrong arity
	public void test200() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public <T> X(T t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		class Local extends X {\n" + 
				"			Local() {\n" +
				"				<String,String>super(\"FAILED\");\n" + 
				"			}\n" + 
				"		};\n" + 
				"		new Local();\n" +				
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	<String,String>super(\"FAILED\");\n" + 
			"	               ^^^^^^^^^^^^^^^\n" + 
			"Incorrect number of type arguments for generic constructor <T>X(T) of type X; it cannot be parameterized with arguments <String, String>\n" + 
			"----------\n");
	}			
	// parameterized explicit constructor call - non generic target constructor
	public void test201() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public X(String t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		class Local extends X {\n" + 
				"			Local() {\n" +
				"				<String>super(\"FAILED\");\n" + 
				"			}\n" + 
				"		};\n" + 
				"		new Local();\n" +				
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	<String>super(\"FAILED\");\n" + 
			"	        ^^^^^^^^^^^^^^^\n" + 
			"The constructor X(String) of type X is not generic; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}			
	// parameterized explicit constructor call - argument type mismatch
	public void test202() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public <T> X(T t){\n" + 
				"		System.out.println(t);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		class Local extends X {\n" + 
				"			Local() {\n" +
				"				<String>super(new X(null));\n" + 
				"			}\n" + 
				"		};\n" + 
				"		new Local();\n" +				
				"	}\n" + 
				"}\n", 			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	<String>super(new X(null));\n" + 
			"	        ^^^^^^^^^^^^^^^^^^\n" + 
			"The parameterized constructor <String>X(String) of type X is not applicable for the arguments (X)\n" + 
			"----------\n");
	}			
	// 62822 - supertypes partially resolved during bound check
	public void test203() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		demo.AD ad;\n" +
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 			
				"demo/AD.java",
				"package demo;\n" +
				"public interface AD extends LIST<ADXP> {}\n",
				"demo/ADXP.java",
				"package demo;\n" +
				"public interface ADXP extends BIN {}\n",
				"demo/ANY.java",
				"package demo;\n" +
				"public interface ANY {}\n",
				"demo/BL.java",
				"package demo;\n" +
				"public interface BL extends ANY {}\n",
				"demo/LIST.java",
				"package demo;\n" +
				"public interface LIST<T extends ANY> extends ANY {}\n",
				"demo/BIN.java",
				"package demo;\n" +
				"public interface BIN extends LIST<BL> {}\n",
			},
			"SUCCESS");
	}
	// 62806
	public void test204() {
		this.runConformTest(
			new String[] {
				"Function.java",
				"public abstract class Function<Y,X> {\n" + 
				"    public abstract Y eval(X x);\n" + 
				"\n" + 
				"}\n",
				"FunctionMappedComparator.java",
				"import java.util.*;\n" + 
				"public class FunctionMappedComparator<Y,X> implements Comparator<X> {\n" + 
				"	/*\n" + 
				"	 * \'Function\' is highlighted as an error here - the message is:\n" + 
				"	 * The type Function is not generic; it cannot be parameterized with arguments <Y, X>\n" + 
				"	 */\n" + 
				"    protected Function<Y,X> function;\n" + 
				"    protected Comparator<Y> comparator;\n" + 
				"    public FunctionMappedComparator(Function<Y,X> function,Comparator<Y> comparator ) {\n" + 
				"        this.function=function;\n" + 
				"        this.comparator=comparator;\n" + 
				"    }\n" + 
				"\n" + 
				"    public int compare(X x1, X x2) {\n" + 
				"        return comparator.compare(function.eval(x1),function.eval(x2));\n" + 
				"    }\n" + 
				"}\n", 			
			},
			"");
	}
	// 63555 - reference to static type parameter allowed inside type itself
	public void test205() {
		this.runConformTest(
			new String[] {
				"Alpha.java",
				"public class Alpha {\n" + 
				"	static class Beta<T> {\n" + 
				"		T obj;\n" + 
				"	}\n" + 
				"}\n", 			
			},
			"");
	}
	// 63555 - variation on static method type parameter
	public void test206() {
		this.runConformTest(
			new String[] {
				"Alpha.java",
				"public class Alpha {\n" + 
				"	static <T> void Beta(T t) {\n" + 
				"	}\n" + 
				"}\n", 			
			},
			"");
	}			
	// 63590 - disallow parameterized type in catch/throws clause
	public void test207() {
		this.runNegativeTest(
			new String[] {
				"Alpha.java",
				"public class Alpha<T> extends RuntimeException {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Object() {\n" + 
				"			public void m() throws Alpha<String> {\n" + 
				"				System.out.println(\"SUCCESS\");\n" + 
				"			}\n" + 
				"		}.m();\n" + 
				"	}\n" + 
				"}\n", 			
			},
			"----------\n" + 
			"1. WARNING in Alpha.java (at line 1)\n" + 
			"	public class Alpha<T> extends RuntimeException {\n" + 
			"	             ^^^^^\n" + 
			"The serializable class Alpha does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in Alpha.java (at line 1)\n" + 
			"	public class Alpha<T> extends RuntimeException {\n" + 
			"	                              ^^^^^^^^^^^^^^^^\n" + 
			"The generic class Alpha<T> may not subclass java.lang.Throwable\n" + 
			"----------\n" + 
			"3. ERROR in Alpha.java (at line 4)\n" + 
			"	public void m() throws Alpha<String> {\n" + 
			"	                       ^^^^^\n" + 
			"Cannot use the parameterized type Alpha<String> either in catch block or throws clause\n" + 
			"----------\n");
	}			
	// 63590 - disallow parameterized type in catch/throws clause
	public void test208() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends RuntimeException {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			throw new X<String>();\n" + 
				"		} catch(X<String> e) {\n" + 
				"			System.out.println(\"X<String>\");\n" + 
				"		} catch(X<X<String>> e) {\n" + 
				"			System.out.println(\"X<X<String>>\");\n" + 
				"		} catch(RuntimeException e) {\n" + 
				"			System.out.println(\"RuntimeException\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n", 			
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X<T> extends RuntimeException {\n" + 
			"	             ^\n" + 
			"The serializable class X does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	public class X<T> extends RuntimeException {\n" + 
			"	                          ^^^^^^^^^^^^^^^^\n" + 
			"The generic class X<T> may not subclass java.lang.Throwable\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	} catch(X<String> e) {\n" + 
			"	                  ^\n" + 
			"Cannot use the parameterized type X<String> either in catch block or throws clause\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 7)\n" + 
			"	} catch(X<X<String>> e) {\n" + 
			"	                     ^\n" + 
			"Cannot use the parameterized type X<X<String>> either in catch block or throws clause\n" + 
			"----------\n");
	}
	// 63556 - should resolve all occurrences of A to type variable
	public void test209() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<A,B,C extends java.util.List<A>> {}\n" + 
				"class X2<A,C extends java.util.List<A>, B> {}\n" + 
				"class X3<B, A,C extends java.util.List<A>> {}\n", 			
			},
			"");
	}	
	// 68006 - Invalid modifier after parse
	public void test210() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(Map<? super Object, ? extends String> m){\n" + 
				"	}\n" + 
				"}\n", 			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(Map<? super Object, ? extends String> m){\n" + 
			"	         ^^^\n" + 
			"Map cannot be resolved to a type\n" + 
			"----------\n");
	}
	// test compilation against binaries
	public void test211() {
		this.runConformTest(
			new String[] {
				"p/Top.java",
				"package p;\n" +
				"public interface Top<T> {}\n",
			},
			"");

		this.runConformTest(
			new String[] {
				"p/Super.java",
				"package p;\n" +
				"public class Super<T> implements Top<T>{\n" +
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false, // do not flush output
			null);		
	}			
	// check type variable equivalence
	public void test212() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"\n" + 
				"public class X<T>{\n" + 
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"	X<T> _recurse; \n" + 
				"	public List<T> toList(){\n" + 
				"		List<T> result = new ArrayList<T>();\n" + 
				"		result.addAll(_recurse.toList()); // should be applicable\n" + 
				"		return result;\n" + 
				"	}\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	public void test213() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<R,T extends Comparable<T>>{\n" + 
				"	T test;\n" + 
				"	public Comparable<? extends T> getThis(){\n" + 
				"		return test;\n" + 
				"	}\n" + 
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	// 68133 - verify error
	public void test214() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        ArrayList<Object> l;\n" + 
				"        switch (args.length) {\n" + 
				"        case 1:\n" + 
				"            l = new ArrayList<Object>();\n" + 
				"            System.out.println(l);\n" + 
				"            break;\n" + 
				"        default:\n" + 
				"            System.out.println(\"SUCCESS\");\n" + 
				"            return;\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	// 68133 variation
	public void test215() { 
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		java.util.ArrayList<Object> i;	\n" + 
				"		outer: {\n" + 
				"			if (args == null) {\n" + 
				"				i = null;\n" + 
				"				break outer;\n" + 
				"			}\n" + 
				"			return;\n" + 
				"		}\n" + 
				"		System.out.println(i);	\n" + 
				"		System.out.println(\"SUCCESS\");	\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
			
		String expectedOutput =
			"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(String[] args);\n" + 
			"     0  aload_0\n" + 
			"     1  ifnonnull 9\n" + 
			"     4  aconst_null\n" + 
			"     5  astore_1\n" + 
			"     6  goto 10\n" + 
			"     9  return\n" + 
			"    10  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
			"    13  aload_1\n" + 
			"    14  invokevirtual #27 <Method java/io/PrintStream.println(Ljava/lang/Object;)V>\n" + 
			"    17  getstatic #21 <Field java/lang/System.out Ljava/io/PrintStream;>\n" + 
			"    20  ldc #29 <String \"SUCCESS\">\n" + 
			"    22  invokevirtual #32 <Method java/io/PrintStream.println(Ljava/lang/String;)V>\n" + 
			"    25  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"        [pc: 4, line: 6]\n" + 
			"        [pc: 6, line: 7]\n" + 
			"        [pc: 9, line: 9]\n" + 
			"        [pc: 10, line: 11]\n" + 
			"        [pc: 17, line: 12]\n" + 
			"        [pc: 25, line: 13]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 26] local: args index: 0 type: [Ljava/lang/String;\n" + 
			"        [pc: 6, pc: 9] local: i index: 1 type: Ljava/util/ArrayList;\n" + 
			"        [pc: 10, pc: 26] local: i index: 1 type: Ljava/util/ArrayList;\n" + 
			"      Local variable type table:\n" + 
			"        [pc: 6, pc: 9] local: i index: 1 type: Ljava/util/ArrayList<Ljava/lang/Object;>;\n" + 
			"        [pc: 10, pc: 26] local: i index: 1 type: Ljava/util/ArrayList<Ljava/lang/Object;>;\n";
		
		try {
			File f = new File(OUTPUT_DIR + File.separator + "X.class");
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	// 68998 parameterized field constants
	public void test216() { 
		this.runConformTest(
			new String[] {
				"test/cheetah/NG.java",
				"package test.cheetah;\n" +
				"public class NG extends G {\n" +
				"		public static void main(String[] args) {\n" + 
				"			System.out.println(\"SUCCESS\");	\n" + 
				"		}\n" + 
				"    public boolean test() {\n" +
				"        return o == null;\n" +
				"    }\n" +
				"}\n",
				"test/cheetah/G.java",
				"package test.cheetah;\n" +
				"public class G<E> {\n" +
				"    protected Object o;\n" +
				"}\n",
			},
			"SUCCESS");
	}
	// 69135 - unnecessary cast operation
	public void test217() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"public class X {\n" + 
				"    public static void main(String [] args) {\n" + 
				"		ArrayList<String> l= new ArrayList<String>();\n" + 
				"		String string = (String) l.get(0);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	String string = (String) l.get(0);\n" + 
			"	                ^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type String for expression of type String\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// 64154 visibility issue due to invalid use of parameterized binding
	public void test218() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T>{\n" + 
				"	private final T _data;\n" + 
				"	private X(T data){\n" + 
				"		_data = data;\n" + 
				"	}\n" + 
				"	public T getData(){\n" + 
				"		return _data;\n" + 
				"	}\n" + 
				"	public static <E> X<E> create(E data) {\n" + 
				"		return new X<E>(data);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		create(new Object());\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}	
	// 64154 variation
	public void test219() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T>{\n" + 
				"	private final T _data;\n" + 
				"	private X(T data){\n" + 
				"		_data = data;\n" + 
				"	}\n" + 
				"	public T getData(){\n" + 
				"		return _data;\n" + 
				"	}\n" + 
				"	public static <E> E create(E data) {\n" + 
				"		return new X<E>(data)._data;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		create(new Object());\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}	
	// 69141 unsafe wildcard operation tolerates wildcard with lower bounds
	public void test220() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		ArrayList<? super Integer> al = new ArrayList<Object>();\n" + 
				"		al.add(new Integer(1)); // (1)\n" + 
				"		Integer i = al.get(0);  // (2)\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	Integer i = al.get(0);  // (2)\n" + 
			"	        ^\n" + 
			"Type mismatch: cannot convert from ? super Integer to Integer\n" + 
			"----------\n");
	}		
	// 69141 variation
	public void test221() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		ArrayList<? extends Integer> al = new ArrayList<Integer>();\n" + 
				"		al.add(new Integer(1)); // (1)\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	al.add(new Integer(1)); // (1)\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Bound mismatch: The method add(? extends Integer) of type ArrayList<? extends Integer> is not applicable for the arguments (Integer). The wildcard parameter ? extends Integer has no lower bound, and may actually be more restrictive than argument Integer\n" + 
			"----------\n");
	}
	// 69141: variation
	public void test222() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		XList<? super Integer> lx = new XList<Integer>();\n" + 
				"		lx.slot = new Integer(1);\n" + 
				"		Integer i = lx.slot;\n" +
				"    }    	\n" + 
				"}\n" + 
				"class XList<E> {\n" + 
				"    E slot;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	Integer i = lx.slot;\n" + 
			"	        ^\n" + 
			"Type mismatch: cannot convert from ? super Integer to Integer\n" + 
			"----------\n");
	}	
	
	// 69251- instantiating wildcards
	public void test223() {
		Map customOptions = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" + 
				"import java.util.Map;\n" + 
				"public class X {\n" + 
				"    static final Map<String, Class<? extends Object>> classes \n" + 
				"            = new HashMap<String, Class<? extends Object>>();\n" + 
				"    \n" + 
				"    static final Map<String, Class<? extends Object>> classes2 \n" + 
				"            = new HashMap<String, Class>();\n" + 
				"    \n" + 
				"    class MX<E> {\n" + 
				"    	E get() { return null; }\n" + 
				"    	void foo(E e) {}\n" + 
				"    }\n" + 
				"    \n" + 
				"    void foo() {\n" + 
				"    	MX<Class<? extends Object>> mx1 = new MX<Class<? extends Object>>();\n" + 
				"    	MX<Class> mx2 = new MX<Class>();\n" + 
				"    	mx1.foo(mx2.get());\n" + 
				"    }\n" + 
				"}\n"	, 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	static final Map<String, Class<? extends Object>> classes2 \n" + 
			"	                                                  ^^^^^^^^\n" + 
			"Type mismatch: cannot convert from HashMap<String,Class> to Map<String,Class<? extends Object>>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 18)\n" + 
			"	mx1.foo(mx2.get());\n" + 
			"	        ^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type Class to type Class<? extends Object>. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// 68998 variation
	public void test224() { 
		this.runNegativeTest(
			new String[] {
				"test/cheetah/NG.java",
				"package test.cheetah;\n" +
				"public class NG extends G {\n" +
				"		public static void main(String[] args) {\n" + 
				"			System.out.println(\"SUCCESS\");	\n" + 
				"		}\n" + 
				"    public boolean test() {\n" +
				"        return o == null;\n" +
				"    }\n" +
				"}\n",
				"test/cheetah/G.java",
				"package test.cheetah;\n" +
				"public class G<E> {\n" +
				"    protected final Object o;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in test\\cheetah\\G.java (at line 2)\r\n" + 
			"	public class G<E> {\r\n" + 
			"	             ^\n" + 
			"The blank final field o may not have been initialized\n" + 
			"----------\n");
	}	
	// 69353 - prevent using type parameter in catch block
	public void test225() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    String foo() throws T {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>().baz(new EX());\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                try {\n" + 
				"	                System.out.println(foo());\n" + 
				"                } catch (T t) {\n" + 
				"                }\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	} catch (T t) {\n" + 
			"	           ^\n" + 
			"Cannot use the type parameter T in a catch block\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 19)\n" + 
			"	class EX extends Exception {\n" + 
			"	      ^^\n" + 
			"The serializable class EX does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n");
	}
	// 69170 - invalid generic array creation
	public void test226() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T>{\n" + 
				"	 Object x1= new T[0];\n" + 
				"	 Object x2= new X<String>[0];	 \n" + 
				"	 Object x3= new X<T>[0];	 \n" + 
				"	 Object x4= new X[0];	 \n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Object x1= new T[0];\n" + 
			"	           ^^^^^^^^\n" + 
			"Cannot create a generic array of T\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Object x2= new X<String>[0];	 \n" + 
			"	           ^^^^^^^^^^^^^^^^\n" + 
			"Cannot create a generic array of X<String>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	Object x3= new X<T>[0];	 \n" + 
			"	           ^^^^^^^^^^^\n" + 
			"Cannot create a generic array of X<T>\n" + 
			"----------\n");
	}
	// 69359 - unsafe cast diagnosis
	public void test227() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" import java.util.*;\n" + 
				" public class X {\n" + 
				"  List list() { return null; }\n" + 
				"  void m() { List<X> l = (List<X>)list(); } // unsafe cast\n" + 
				"  void m0() { List<X> l = list(); } // unsafe conversion\n" + 
				"  void m1() { for (X a : list()); } // type mismatch\n" + 
				"  void m2() { for (Iterator<X> i = list().iterator(); i.hasNext();); }  // unsafe conversion\n" + 
				"  void m3() { Collection c = null; List l = (List<X>)c; } // unsafe cast\n" + 
				"  void m4() { Collection c = null; List l = (List<?>)c; } // ok\n" + 
				"  void m5() { List c = null; List l = (Collection<X>)c; } // type mismatch\n" + 
				"  void m6() { List c = null; List l = (Collection<?>)c; } // type mismatch\n" + 
				"}\n"	,
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	void m() { List<X> l = (List<X>)list(); } // unsafe cast\n" + 
			"	                       ^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from List to parameterized type List<X> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	void m0() { List<X> l = list(); } // unsafe conversion\n" + 
			"	                        ^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type List to type List<X>. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	void m1() { for (X a : list()); } // type mismatch\n" + 
			"	                       ^^^^^^\n" + 
			"Type mismatch: cannot convert from element type Object to X\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 7)\n" + 
			"	void m2() { for (Iterator<X> i = list().iterator(); i.hasNext();); }  // unsafe conversion\n" + 
			"	                                 ^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type Iterator to type Iterator<X>. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 8)\n" + 
			"	void m3() { Collection c = null; List l = (List<X>)c; } // unsafe cast\n" + 
			"	                                          ^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from Collection to parameterized type List<X> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 10)\n" + 
			"	void m5() { List c = null; List l = (Collection<X>)c; } // type mismatch\n" + 
			"	                                ^\n" + 
			"Type mismatch: cannot convert from Collection<X> to List\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 10)\n" + 
			"	void m5() { List c = null; List l = (Collection<X>)c; } // type mismatch\n" + 
			"	                                    ^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from List to parameterized type Collection<X> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 11)\n" + 
			"	void m6() { List c = null; List l = (Collection<?>)c; } // type mismatch\n" + 
			"	                                ^\n" + 
			"Type mismatch: cannot convert from Collection<?> to List\n" + 
			"----------\n" + 
			"9. WARNING in X.java (at line 11)\n" + 
			"	void m6() { List c = null; List l = (Collection<?>)c; } // type mismatch\n" + 
			"	                                    ^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type Collection<?> for expression of type List\n" + 
			"----------\n");
	}	
	// conversion from raw to X<?> is safe (no unsafe warning)
	public void test228() {
		this.runConformTest(
			new String[] {
				"X.java",
				" import java.util.*;\n" + 
				" public class X {\n" + 
				" 	List<?> list = new ArrayList();\n" + 
				" }\n",
			},
			"");
	}
	// can resolve member through type variable
	public void test229() {
		this.runConformTest(
			new String[] {
				"X.java",
				" public class X <T extends XC> {\n" + 
				" 	T.MXC f;\n" + 
				" 	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				" }\n" + 
				"\n" + 
				" class XC {\n" + 
				" 	class MXC {}\n" + 
				" }\n",
			},
			"SUCCESS");
	}			
	// 69375 - equivalence of wildcards
	public void test230() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		List<? extends Integer> li= null;\n" + 
				"		List<? extends Number> ln= null;\n" + 
				"		ln = li;\n" + 
				"		li= ln;\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	li= ln;\n" + 
			"	    ^^\n" + 
			"Type mismatch: cannot convert from List<? extends Number> to List<? extends Integer>\n" + 
			"----------\n");
	}
	// 69170 - variation
	public void test231() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T>{\n" + 
				"	 Object x1= new X<?>[0];	 \n" + 
				"	 Object x2= new X<? super String>[0];	 \n" + 
				"	 Object x3= new X<? extends Thread>[0];	 \n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object x2= new X<? super String>[0];	 \n" + 
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot create a generic array of X<? super String>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Object x3= new X<? extends Thread>[0];	 \n" + 
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot create a generic array of X<? extends Thread>\n" + 
			"----------\n");
	}	
	// 69542 - generic cast should be less strict
	public void test232() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    private T val;\n" + 
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(\"BAD\");\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont=new Container<Integer>();\n" + 
				"	    cont.setVal(new Integer(0));\n" + 
				"	    badMethod(cont);\n" + 
				"	    Object someVal = cont.getVal(); // no cast \n" + 
				"	    System.out.println(cont.getVal()); // no cast \n" + 
				"	}\n" + 
				"}\n",
			},
			"BAD");
	}	
	// 69542 - variation
	public void test233() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    private T val;\n" + 
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(new Long(0));\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont=new Container<Integer>();\n" + 
				"	    cont.setVal(new Integer(0));\n" + 
				"	    badMethod(cont);\n" + 
				"	    Number someVal = cont.getVal();// only cast to Number \n" + 
				"	    System.out.println(\"SUCCESS\"); \n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	// 69542 - variation
	public void test234() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    public T val;\n" + 
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(\"BAD\");\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont=new Container<Integer>();\n" + 
				"	    cont.setVal(new Integer(0));\n" + 
				"	    badMethod(cont);\n" + 
				"	    Object someVal = cont.val; // no cast \n" + 
				"	    System.out.println(cont.val); // no cast \n" + 
				"	}\n" + 
				"}\n",
			},
			"BAD");
	}		
	// 69542 - variation
	public void test235() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    public T val;\n" + 
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(new Long(0));\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont=new Container<Integer>();\n" + 
				"	    cont.setVal(new Integer(0));\n" + 
				"	    badMethod(cont);\n" + 
				"	    Number someVal = cont.val;// only cast to Number \n" + 
				"	    System.out.println(\"SUCCESS\"); \n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	// 69542 - variation
	public void test236() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    public T val;\n" + 
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(\"BAD\");\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont=new Container<Integer>();\n" + 
				"	    cont.setVal(new Integer(0));\n" + 
				"	    badMethod(cont);\n" + 
				"	    Object someVal = (cont).val; // no cast \n" + 
				"	    System.out.println((cont).val); // no cast \n" + 
				"	}\n" + 
				"}\n",
			},
			"BAD");
	}		
	// 69542 - variation
	public void test237() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    public T val;\n" + 
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(new Long(0));\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont=new Container<Integer>();\n" + 
				"	    cont.setVal(new Integer(0));\n" + 
				"	    badMethod(cont);\n" + 
				"	    Number someVal = (cont).val;// only cast to Number \n" + 
				"	    System.out.println(\"SUCCESS\"); \n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// 69542 - variation
	public void test238() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    public T val;\n" + 
				"		Container<T> next;\n" +
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(\"BAD\");\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont = new Container<Integer>();\n" + 
				"		cont.next = new Container<Integer>();\n" + 
				"	    cont.next.setVal(new Integer(0));\n" + 
				"	    badMethod(cont.next);\n" + 
				"	    Object someVal = cont.next.val; // no cast \n" + 
				"	    System.out.println(cont.next.val); // no cast \n" + 
				"	}\n" + 
				"}\n",
			},
			"BAD");
	}		
	// 69542 - variation
	public void test239() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				" 	static class Container<T>{\n" + 
				"	    public T val;\n" + 
				"		Container<T> next;\n" +
				"	    public T getVal() {\n" + 
				"	        return val;\n" + 
				"	    }\n" + 
				"	    public void setVal(T val) {\n" + 
				"	        this.val = val;\n" + 
				"	    }\n" + 
				"	}\n" + 
				"	public static void badMethod(Container<?> param){\n" + 
				"	    Container x=param;\n" + 
				"	    x.setVal(new Long(0));\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	    Container<Integer> cont = new Container<Integer>();\n" + 
				"		cont.next = new Container<Integer>();\n" + 
				"	    cont.next.setVal(new Integer(0));\n" + 
				"	    badMethod(cont.next);\n" + 
				"	    Number someVal = cont.next.val;// only cast to Number \n" + 
				"	    System.out.println(\"SUCCESS\"); \n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	// 69713 NPE due to length pseudo field
	public void test240() {
		this.runNegativeTest(
			new String[] {
				"X.java",
			"public class X {\n" + 
			"    String[] elements = null;\n" + 
			"	\n" + 
			"    public X() {\n" + 
			"        String s = \"a, b, c, d\";\n" + 
			"        elements = s.split(\",\");\n" + 
			"        if(elements.length = 3) {\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"	
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	if(elements.length = 3) {\n" + 
			"	   ^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from int to boolean\n" + 
			"----------\n");
	}	
	// 69776 - missing checkcast on cast operation
	public void test241() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" + 
				"import java.util.Map;\n" + 
				"public class X {\n" + 
				"    private static final Map<String, Class> classes = new HashMap<String, Class>();\n" + 
				"    public static void main(String[] args) throws Exception {\n" + 
				"    	classes.put(\"test\", X.class);\n" + 
				"        final Class<? extends Object> clazz = (Class<? extends Object>) classes.get(\"test\");\n" + 
				"        Object o = clazz.newInstance();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	// 69776 - variation
	public void test242() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" + 
				"import java.util.Map;\n" + 
				"public class X {\n" + 
				"    private static final Map<String, Class> classes = new HashMap<String, Class>();\n" + 
				"    public static void main(String[] args) throws Exception {\n" + 
				"    	classes.put(\"test\", X.class);\n" + 
				"        final Class<? extends Object> clazz = (Class<? extends Object>) classes.get(\"test\");\n" + 
				"        Object o = clazz.newInstance();\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	final Class<? extends Object> clazz = (Class<? extends Object>) classes.get(\"test\");\n" + 
			"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type Class<? extends Object> for expression of type Class\n" + 
			"----------\n");
	}		
	public void test243() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public X foo() {\n" +
				"        System.out.println(\"Did NOT add bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"    public static void main(String[] args) throws Exception {\n" +
				"        X x = new A();\n" +
				"        x.foo();\n" +
				"        System.out.print(\" + \");\n" +
				"        I i = new A();\n" +
				"        i.foo();\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public I foo();\n" +
				"}\n" +
				"class A extends X implements I {\n" +
				"    public A foo() {\n" +
				"        System.out.print(\"Added bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"}\n"
			},
			"Added bridge method + Added bridge method");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public X foo() { return this; }\n" +
				"    public static void main(String[] args) throws Exception {\n" +
				"        System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n",
				"SubTypes.java",
				"class A extends X {\n" +
				"    public A foo() { return this; }\n" +
				"}\n" +
				"class B extends X {\n" +
				"    public X foo() { return new X(); }\n" +
				"    public B foo() { return this; }\n" +
				"}\n" +
				"class C extends A {\n" +
				"    public X foo() { return new X(); }\n" +
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in SubTypes.java (at line 5)\n" + 
		"	public X foo() { return new X(); }\n" + 
		"	         ^^^^^\n" + 
		"Duplicate method foo() in type B\n" + 
		"----------\n" + 
		"2. ERROR in SubTypes.java (at line 6)\n" + 
		"	public B foo() { return this; }\n" + 
		"	         ^^^^^\n" + 
		"Duplicate method foo() in type B\n" + 
		"----------\n" + 
		"3. ERROR in SubTypes.java (at line 9)\n" + 
		"	public X foo() { return new X(); }\n" + 
		"	         ^^^^^\n" + 
		"The return type is incompatible with A.foo()\n" + 
		"----------\n");
	}
	// generic method of raw type
	public void test244() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> { \n" + 
				"	<G> T foo(G g) {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X rx = new X();\n" + 
				"		rx.foo(\"hello\");\n" + 
				"	}\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	rx.foo(\"hello\");\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the method foo(G) of raw type X. References to generic type X<T> should be parameterized\n" + 
		"----------\n");
	}
	// generic method of raw type
	public void test245() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> { \n" + 
				"	<G> T foo(G g) {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X rx = new X();\n" + 
				"		rx.<String>foo(\"hello\");\n" + 
				"	}\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	rx.<String>foo(\"hello\");\n" + 
		"	           ^^^\n" + 
		"The method foo(Object) of raw type X is no more generic; it cannot be parameterized with arguments <String>\n" + 
		"----------\n");
	}		
	// 69320 parameterized type compatibility
	public void test246() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"    class MX<E> {\n" + 
				"    }\n" + 
				"    void foo() {\n" + 
				"      MX<Class<? extends Object>> mx2 = new MX<Class>();\n" + 
				"    }\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	MX<Class<? extends Object>> mx2 = new MX<Class>();\n" + 
		"	                            ^^^\n" + 
		"Type mismatch: cannot convert from X.MX<Class> to X.MX<Class<? extends Object>>\n" + 
		"----------\n");
	}		
	// 69320 variation
	public void test247() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"    void foo() {\n" + 
				"      MX<Class<? extends Object>> mx2 = new MX<Class>(); // wrong\n" + 
				"      MX<Class<? extends Object>> mx3 = new MX<Class<? extends String>>(); // wrong\n" + 
				"      MX<Class<? extends Object>> mx4 = new MX<Class<String>>(); // wrong\n" + 
				"      MX<? extends Class> mx5 = new MX<Class>(); // ok\n" + 
				"      MX<? super Class> mx6 = new MX<Class>(); // ok\n" + 
				"      MX<Class<? extends Class>> mx7 = new MX<Class<Class>>(); // wrong\n" + 
				"      MX<MX<? extends Class>> mx8 = new MX<MX<Class>>(); // wrong\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class MX<E> {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	MX<Class<? extends Object>> mx2 = new MX<Class>(); // wrong\n" + 
			"	                            ^^^\n" + 
			"Type mismatch: cannot convert from MX<Class> to MX<Class<? extends Object>>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	MX<Class<? extends Object>> mx3 = new MX<Class<? extends String>>(); // wrong\n" + 
			"	                            ^^^\n" + 
			"Type mismatch: cannot convert from MX<Class<? extends String>> to MX<Class<? extends Object>>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	MX<Class<? extends Object>> mx4 = new MX<Class<String>>(); // wrong\n" + 
			"	                            ^^^\n" + 
			"Type mismatch: cannot convert from MX<Class<String>> to MX<Class<? extends Object>>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 8)\n" + 
			"	MX<Class<? extends Class>> mx7 = new MX<Class<Class>>(); // wrong\n" + 
			"	                           ^^^\n" + 
			"Type mismatch: cannot convert from MX<Class<Class>> to MX<Class<? extends Class>>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 9)\n" + 
			"	MX<MX<? extends Class>> mx8 = new MX<MX<Class>>(); // wrong\n" + 
			"	                        ^^^\n" + 
			"Type mismatch: cannot convert from MX<MX<Class>> to MX<MX<? extends Class>>\n" + 
			"----------\n");
	}			
	// 70247 check type variable is bound during super type resolution
	public void test248() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X<T> extends Vector<? super X<int[]>>{}\n"			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public class X<T> extends Vector<? super X<int[]>>{}\n" + 
			"	                          ^^^^^^\n" + 
			"The type X cannot extend or implement Vector<? super X<int[]>>. A supertype may not specify any wildcard\n" + 
			"----------\n");
	}			
	// 70247 variation
	public void test249() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X<T> implements List<? super X<int[]>>{}\n"			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public class X<T> implements List<? super X<int[]>>{}\n" + 
			"	                             ^^^^\n" + 
			"The type X cannot extend or implement List<? super X<int[]>>. A supertype may not specify any wildcard\n" + 
			"----------\n");
	}			
	// 70295 Class<? extends Object> is compatible with Class<?>
	public void test250() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    void test(Object o) {\n" + 
				"        X.class.isAssignableFrom(o.getClass());\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}			
	// 69800 '? extends Object' is not compatible with A
	public void test251() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"    static class A {\n" + 
				"    }\n" + 
				"    A test() throws Exception {\n" + 
				"        Class<? extends Object> clazz = null;\n" + 
				"        return clazz.newInstance(); // ? extends Object\n" + 
				"    }\n" + 
				"}\n"	
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	return clazz.newInstance(); // ? extends Object\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from ? extends Object to X.A\n" + 
			"----------\n");
	}
	// 69799 NPE in foreach checkcast
	public void test252() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Set<X> channel = channels.get(0);\n" + 
				"	    for (Iterator<X> iter = channel.iterator(); iter.hasNext();) {\n" + 
				"	        Set<X> element;\n" + 
				"	        element = (Set<X>) iter.next();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	Set<X> channel = channels.get(0);\n" + 
			"	                 ^^^^^^^^\n" + 
			"channels cannot be resolved\n" + 
			"----------\n");
	}			
	// 70243 unsafe cast when wildcards
	public void test253() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        List<Integer> li= new ArrayList<Integer>();\n" + 
				"        List<? extends Number> ls= li;       \n" + 
				"        List<Number> x2= (List<Number>)ls;//unsafe\n" + 
				"        x2.add(new Float(1.0));\n" + 
				"        \n" + 
				"        Integer i= li.get(0);//ClassCastException!\n" + 
				"        \n" + 
				"        List<Number> ls2 = (List<? extends Number>)ls;\n" + 
				"        List<? extends Number> ls3 = (List<? extends Number>) li;\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	List<Number> x2= (List<Number>)ls;//unsafe\n" + 
			"	                 ^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from List<? extends Number> to parameterized type List<Number> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 11)\n" + 
			"	List<Number> ls2 = (List<? extends Number>)ls;\n" + 
			"	             ^^^\n" + 
			"Type mismatch: cannot convert from List<? extends Number> to List<Number>\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 11)\n" + 
			"	List<Number> ls2 = (List<? extends Number>)ls;\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type List<? extends Number> for expression of type List<? extends Number>\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 12)\n" + 
			"	List<? extends Number> ls3 = (List<? extends Number>) li;\n" + 
			"	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast to type List<? extends Number> for expression of type List<Integer>\n" + 
			"----------\n");
	}
	// 70053 missing checkcast in string concatenation
	public void test254() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" + 
				"public class X {\n" + 
				" public static void main(String[] args) {\n" + 
				"  X x = new X();\n" + 
				"  System.out.print(\"S\" + x.a() + \"U\" + x.b().get(0) + \"C\" + x.a() + \"C\");\n" + 
				"  System.out.println(new StringBuilder(\"E\").append(x.a()).append(\"S\").append(x.b().get(0)).append(\"S\").append(x.a()).append(\"!\"));  \n" + 
				" }\n" + 
				" String a() { return \"\"; }\n" + 
				" List<String> b() { \n" + 
				"  ArrayList<String> als = new ArrayList<String>(1);\n" + 
				"  als.add(a());\n" + 
				"  return als;\n" + 
				" }\n" + 
				"}\n"
			},
			"SUCCESS!");		
	}
	// 69351 generic type cannot extend Throwable
	public void test255() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T, U> extends Throwable {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X<T, U> extends Throwable {\n" + 
			"	             ^\n" + 
			"The serializable class X does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 1)\n" + 
			"	public class X<T, U> extends Throwable {\n" + 
			"	                             ^^^^^^^^^\n" + 
			"The generic class X<T,U> may not subclass java.lang.Throwable\n" + 
			"----------\n");		
	}
	// 70616 - reference to binary Enum
	public void test256() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"\n" + 
				"		Enum<X> ex = null;\n" + 
				"		String s = ex.name();\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	Enum<X> ex = null;\n" + 
			"	     ^\n" + 
			"Bound mismatch: The type X is not a valid substitute for the bounded parameter <E extends Enum<E>> of the type Enum<E>\n" + 
			"----------\n");		
	}
	// 70618 - reference to variable allowed in parameterized super type
	public void test257() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    public abstract class M extends java.util.AbstractList<T> {}\n" + 
				"}\n" +
				"class Y<T> extends T {}\n" + 
				"class Z<T> {\n" + 
				"    class M extends T {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	class Y<T> extends T {}\n" + 
			"	                   ^\n" + 
			"Cannot refer to the type parameter T as a supertype\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	class M extends T {}\n" + 
			"	                ^\n" + 
			"Cannot refer to the type parameter T as a supertype\n" + 
			"----------\n");
	}
	public void test258() {
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X<K,V> implements java.util.Map<K,V> {\n" + 
				"    static abstract class M<K,V> implements Entry<K,V> {}\n" + 
				"}\n"
			},
			"");
	}
	// 70767 - NPE compiling code with explicit constructor invocation
	public void test259() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<E> {\n" + 
				"	\n" + 
				"	<E> X(E e) {\n" + 
				"		<E> this();\n" + 
				"	}\n" + 
				"	\n" + 
				"	<E> X() {\n" + 
				"	}\n" + 
				"}\n"
			},
			"");
	}
	public void test260() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" + 
				"	class MX <F> {\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class XC<G> extends X<G> {\n" + 
				"	class MXC<H> extends MX<H> {\n" + 
				"	}\n" + 
				"}\n"
			},
			"");
	}	
	public void test261() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" + 
				"	void foo(){\n" + 
				"		X<Integer> xi = (X<Integer>) new X<String>();\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	X<Integer> xi = (X<Integer>) new X<String>();\n" + 
			"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot cast from X<String> to X<Integer>\n" + 
			"----------\n");
	}		
	public void test262() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E,F> {\n" + 
				"	void foo(){\n" + 
				"		X<E,String> xe = (X<E,String>) new X<String,String>();\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	X<E,String> xe = (X<E,String>) new X<String,String>();\n" + 
			"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from X<String,String> to parameterized type X<E,String> will not check conformance of type arguments at runtime\n" + 
			"----------\n");
	}			
	public void test263() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E,F> {\n" + 
				"	void foo(){\n" + 
				"		XC<E,String> xe = (XC<E,String>) new X<String,String>();\n" + 
				"	}\n" + 
				"}\n" + 
				"class XC<G,H> extends X<G,H> {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	XC<E,String> xe = (XC<E,String>) new X<String,String>();\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from X<String,String> to parameterized type XC<E,String> will not check conformance of type arguments at runtime\n" + 
			"----------\n");
	}
	public void test264() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E,F> {\n" + 
				"	void foo(){\n" + 
				"		XC<E,String> xe = (XC<E,String>) new X<String,Integer>();\n" + 
				"	}\n" + 
				"}\n" + 
				"class XC<G,H> extends X<G,H> {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	XC<E,String> xe = (XC<E,String>) new X<String,Integer>();\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot cast from X<String,Integer> to XC<E,String>\n" + 
			"----------\n");
	}		
	public void test265() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" + 
				"	<U> void foo(){\n" + 
				"			XC<U> xcu = (XC<U>) new X<E>();\n" + 
				"			XC<U> xcu1 = (XC<?>) new X<E>();			\n" + 
				"			XC<?> xcu2 = (XC<? extends X>) new X<E>();						\n" + 
				"	}\n" + 
				"}\n" + 
				"class XC<G> extends X<G> {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	XC<U> xcu = (XC<U>) new X<E>();\n" + 
			"	            ^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from X<E> to parameterized type XC<U> will not check conformance of type arguments at runtime\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	XC<U> xcu1 = (XC<?>) new X<E>();			\n" + 
			"	      ^^^^\n" + 
			"Type mismatch: cannot convert from XC<?> to XC<U>\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 5)\n" + 
			"	XC<?> xcu2 = (XC<? extends X>) new X<E>();						\n" + 
			"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: The cast from X<E> to parameterized type XC<? extends X> will not check conformance of type arguments at runtime\n" + 
			"----------\n");
	}		
	public void test266() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E> {\n" + 
				"	void bar() {\n" + 
				"		X<? extends E> xe = new X<E>();\n" + 
				"	}\n" + 
				"}\n"
			},
			"");
	}		
	public void test267() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"	static void foo(X<?> xany) { \n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		foo(new X<Object[]>());\n" + 
				"	}\n" + 
				"}\n"	
			},
			"SUCCESS");
	}		
	public void test268() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"public class X <T> {\n" + 
				"	X[] foo() {\n" + 
				"		ArrayList<X> list = new ArrayList();\n" + 
				"		return list.toArray(new X[list.size()]);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	ArrayList<X> list = new ArrayList();\n" + 
			"	                    ^^^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not convert expression of raw type ArrayList to type ArrayList<X>. References to generic type ArrayList<E> should be parameterized\n" + 
			"----------\n");
	}
	// 70975 - test compilation against binary generic method
	public void test269() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"\n" + 
				"	<U> U[] bar(U[] u)  { \n" +
				"		System.out.println(\"SUCCESS\");\n" + 
				"		return null; }\n" + 
				"\n" + 
				"	static String[] foo() {\n" + 
				"		X<String> xs = new X<String>();\n" + 
				"		return xs.bar(new String[0]);\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		foo();\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS");

		this.runConformTest(
			new String[] {
				"Y.java",
				"public class Y {\n" +
				"    public static void main(String [] args) {\n" + 
				"		X<String> xs = new X<String>();\n" + 
				"		String[] s = xs.bar(new String[0]);\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false, // do not flush output
			null);		
	}
	// 70969 - lub(List<String>, List<Object>) --> List<? extends Object>
	public void test270() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"    public void test(boolean param) {\n" + 
				"        ArrayList<?> ls = (param) \n" + 
				"        		? new ArrayList<String>()\n" + 
				"        		: new ArrayList<Object>();\n" + 
				"        		\n" + 
				"        X x = param ? new XY() : new XZ();\n" + 
				"        XY y = (XY) new XZ();\n" + 
				"    }\n" + 
				"}\n" + 
				"class XY extends X {}\n" + 
				"class XZ extends X {}\n"
			},

			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	XY y = (XY) new XZ();\n" + 
			"	       ^^^^^^^^^^^^^\n" + 
			"Cannot cast from XZ to XY\n" + 
			"----------\n");
	}
	// 71080 - parameter bound <T extends Enum<T>> should be allowed
	public void test271() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends Enum<T>> {\n" + 
				"}\n" 
			},
			"");
	}	
	// 71080 - variation
	public void test272() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends XY<T>> {\n" + 
				"}\n" + 
				"\n" + 
				"class XY<U extends Cloneable> implements Cloneable {\n" + 
				"}\n"
			},
			"");
	}		
	// 71080 - variation
	public void test273() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T extends XY<T> & Cloneable> {\n" + 
				"}\n" + 
				"\n" + 
				"class XY<U extends Cloneable> {\n" + 
				"}\n"
			},
			"");
	}
	// 71241
	public void test274() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" + 
				"    public List useList(List l) {\n" + 
				"        l.add(\"asdf\");\n" + 
				"        return l;\n" + 
				"    }\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"    public List<String> useList(List<String> l) {\n" + 
				"        l.add(\"asdf\");\n" + 
				"        return l;\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	l.add(\"asdf\");\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the method add(E) of raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	public List<String> useList(List<String> l) {\n" + 
			"	                    ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash : The method useList(List<String>) of type Y has the same erasure as useList(List) of type X but does not override it\n" + 
			"----------\n");
	}
	// 71241 - variation
	public void test275() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" + 
				"    public List<String> useList(List<String> l) {\n" + 
				"        l.add(\"asdf\");\n" + 
				"        return l;\n" + 
				"    }\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"    public List useList(List l) {\n" + 
				"        l.add(\"asdf\");\n" + 
				"        return l;\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 9)\n" + 
			"	public List useList(List l) {\n" + 
			"	       ^^^^\n" + 
			"Unsafe type operation: The return type List of the method useList(List) of type Y needs unchecked conversion to conform to the return type List<String> of inherited method\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 10)\n" + 
			"	l.add(\"asdf\");\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the method add(E) of raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}
	// 71241 - variation
	public void test276() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" + 
				"    public void useList(List l) {}\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"    public void useList(List<String> l) {\n" + 
				"		super.useList(l);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	public void useList(List<String> l) {\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash : The method useList(List<String>) of type Y has the same erasure as useList(List) of type X but does not override it\n" + 
			"----------\n");
	}
	// 71241 - variation
	public void test277() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" + 
				"    public void useList(List<String> l) {}\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"    public void useList(List l) {\n" + 
				"		super.useList(l);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	super.useList(l);\n" + 
			"	              ^\n" +
			"Unsafe type operation: Should not convert expression of raw type List to type List<String>. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}
	// 71241 - variation
	public void test278() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> implements I {\n" + 
				"    public Class<T> getDeclaringClass() { return null; }\n" + 
				"}\n" +
				"class Y implements I {\n" + 
				"    public Class<?> getDeclaringClass() { return null; }\n" + 
				"}\n" +
				"interface I {\n" + 
				"	public Class getDeclaringClass();\n" + 
				"}\n"
			},
			"");
	}
	// 69901
	public void test279() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements ISomething {\n" + 
				"    public Class getSomething() { return null; }\n" + 
				"}\n" + 
				"class Y {}\n" + 
				"interface ISomething {\n" + 
				"    public Class<? extends Y> getSomething();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public Class getSomething() { return null; }\n" + 
			"	       ^^^^^\n" + 
			"Unsafe type operation: The return type Class of the method getSomething() of type X needs unchecked conversion to conform to the return type Class<? extends Y> of inherited method\n" + 
			"----------\n");
	}
	// 62822
	public void test280() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface X<T1 extends Y<T2>, T2 extends Z> {}\n" +
				"interface Y<T3 extends Z> {}\n" +
				"interface Z {}\n"
			},
			"");
	}	
	public void test281() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T1 extends Y<T2>, T2 extends Z> {}\n" +
				"interface Y<T3 extends Comparable> {}\n" +
				"interface Z {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	interface X<T1 extends Y<T2>, T2 extends Z> {}\n" + 
			"	                         ^^\n" +
			"Bound mismatch: The type T2 is not a valid substitute for the bounded parameter <T3 extends Comparable> of the type Y<T3>\n" + 
			"----------\n");
	}	
	public void test282() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Y.Member<String> {}\n" +
				"class Y { static class Member<T> {} }\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" +
				"public class X extends p1.Y.Member<String> {}\n" +
				"class Y { static class Member<T> {} }\n"
			},
			"");
	}	
	public void test283() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Y.Missing<String> {}\n" +
				"class Y { static class Member<T> {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X extends Y.Missing<String> {}\n" + 
			"	                       ^^^^^^^^^\n" + 
			"Y.Missing cannot be resolved to a type\n" + 
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n" +
				"public class X extends Y.Missing<String> {}\n" +
				"class Y { static class Member<T> {} }\n"
			},
			"----------\n" + 
			"1. ERROR in p1\\X.java (at line 2)\n" + 
			"	public class X extends Y.Missing<String> {}\n" + 
			"	                       ^^^^^^^^^\n" + 
			"Y.Missing cannot be resolved to a type\n" + 
			"----------\n");
	}
	// 72083
	public void test284() {
		this.runConformTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A <T1 extends A<T1, T2>, T2 extends B<T1, T2>> {\n" +
				"    public static void main(String [] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
				"p1/B.java",
				"package p1;\n" +
				"public class B <T3 extends A<T3, T4>, T4 extends B<T3, T4>> {}\n"
			},
			"SUCCESS");
		this.runConformTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A <T1 extends B<T1, T2>, T2 extends A<T1, T2>> {\n" +
				"    public static void main(String [] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
				"p1/B.java",
				"package p1;\n" +
				"public class B <T3 extends B<T3, T4>, T4 extends A<T3, T4>> {}\n"
			},
			"SUCCESS");
	}
	// 73530
	public void test285() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Vector;\n" + 
				"public class X {\n" + 
				"  public static void main(String[] args){\n" + 
				"    Vector<Integer[]> v = new Vector<Integer[]>();\n" + 
				"    Integer[] array1 = new Integer[5];\n" + 
				"    array1[0] = new Integer(17);\n" + 
				"    array1[1] = new Integer(42);\n" + 
				"    v.add(array1);\n" + 
				"    Integer twentyfour = v.get(0)[1];  // responsible for the crash\n" + 
				"    System.out.println(twentyfour);\n" + 
				"  }\n" + 
				"}"
			},
			"42");
	}
	// 72644
	// TODO (philippe) we need a way to test these 2 methods & find them 'equivalent'... right isEquivalentTo return false
	public void test286() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T extends Object> T foo(Class<T> c) {return null;}\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"	<T extends Object> T foo(Class<T> c) {return null;}\n" + 
				"}"
			},
			"");
	}	
	public void test287() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"\n" + 
				"	public class A <U> {\n" + 
				"		\n" + 
				"		public class B <V> {\n" + 
				"			\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		\n" + 
				"		X.A.B<String> bs;\n" + 
				"	}\n" + 
				"}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	X.A.B<String> bs;\n" + 
			"	^^^^^\n" + 
			"The member type X.A.B<String> must be qualified with a parameterized type, since it is not static\n" + 
			"----------\n");
	}	
	public void test288() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"\n" + 
				"	public static class A <U> {\n" + 
				"		\n" + 
				"		public static class B <V> {\n" + 
				"			\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		\n" + 
				"		X.A.B<String> bs;\n" + 
				"	}\n" + 
				"}\n" 
			},
			"");
	}		
	public void test289() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"\n" + 
				"	public class A <U> {\n" + 
				"		\n" + 
				"		public class B <V> {\n" + 
				"			\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		\n" + 
				"		X<String>.A.B<String> bs;\n" + 
				"	}\n" + 
				"}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	X<String>.A.B<String> bs;\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"The member type X<String>.A must be parameterized, since it is qualified with a parameterized type\n" + 
			"----------\n");
	}	
	public void test290() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"\n" + 
				"	public static class A <U> {\n" + 
				"		\n" + 
				"		public class B <V> {\n" + 
				"			\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		\n" + 
				"		X<String>.A.B<String> bs;\n" + 
				"	}\n" + 
				"}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	X<String>.A.B<String> bs;\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"The member type X<String>.A cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type X<String>\n" + 
			"----------\n");
	}		
	// ensure bound check deals with supertype (and their enclosing type)
	public void test291() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Iterable>{\n" + 
				"	class MX<U extends Iterable> {\n" + 
				"	}\n" + 
				"}\n" + 
				"class SX extends X<Thread>.MX<Object> {\n" + 
				"	SX(X x){\n" + 
				"		x.super();\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class SX extends X<Thread>.MX<Object> {\n" + 
			"	                   ^^^^^^\n" + 
			"Bound mismatch: The type Thread is not a valid substitute for the bounded parameter <T extends Iterable> of the type X<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	class SX extends X<Thread>.MX<Object> {\n" + 
			"	                              ^^^^^^\n" + 
			"Bound mismatch: The type Object is not a valid substitute for the bounded parameter <U extends Iterable> of the type X<T>.MX<U>\n" + 
			"----------\n");
	}		
	public void test292() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"	class Y {\n" + 
				"		class Z<U> {\n" + 
				"		}\n" + 
				"	}\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<Object>.Y.Z<Object> zo;\n" + 
				"	}\n" + 
				"}\n"	
			},
			"");
	}
	// 73837
	public void test293() {
		this.runConformTest(
			new String[] {
				"B.java", //---------------------------
				"public class B<X>{\n"+
				"    public B(X str,D dValue){}\n"+
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"	,
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
			"SUCCESS");

		this.runConformTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<Z,Y> {\n" + 
				"    public B<Z> test(Z zValue,D<Y> yValue){ return new B<Z>(zValue,yValue); }\n" + 
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false, // do not flush output
			null);		
	}
	// 73837 variation
	public void test294() {
		this.runConformTest(
			new String[] {
				"B.java", //---------------------------
				"public class B<X>{\n"+
				"    public B(X str, B<D> dValue){}\n"+
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"	,
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
			"SUCCESS");

		this.runNegativeTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<Z,Y> {\n" + 
				"    public B<Z> test(Z zValue,B<D<Y>> yValue){ return new B<Z>(zValue,yValue); }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 2)\r\n" + 
			"	public B<Z> test(Z zValue,B<D<Y>> yValue){ return new B<Z>(zValue,yValue); }\r\n" + 
			"	                                                  ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor B<Z>(Z, B<D<Y>>) is undefined\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null);		
	}
	// non-static method #start() gets its type substituted when accessed through raw type
	public void test295() {
		this.runNegativeTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<U> {\n" + 
				"\n" + 
				"	void bar() {\n" + 
				"		new B().start().get(new B().start()).get(new B().start());\n" + 
				"	}\n" + 
				"}\n",
				"B.java", //---------------------------
				"public class B<X>{\n" + 
				"	X get(B<X> bx) { return null; }\n" + 
				"	B<B<D>> start() { return null; }\n" + 
				"}",
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
		"----------\n" + 
		"1. WARNING in C.java (at line 4)\n" + 
		"	new B().start().get(new B().start()).get(new B().start());\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the method get(B<X>) of raw type B. References to generic type B<X> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in C.java (at line 4)\n" + 
		"	new B().start().get(new B().start()).get(new B().start());\n" + 
		"	                                     ^^^\n" + 
		"The method get(B) is undefined for the type Object\n" + 
		"----------\n");
	}
	// static method #start() gets its type does not get substituted when accessed through raw type
	public void test296() {
		this.runNegativeTest(
			new String[] {
				"C.java", //---------------------------
				"public class C<U> {\n" + 
				"\n" + 
				"	void bar() {\n" + 
				"		new B().start().get(new B().start()).get(new B().start());\n" + 
				"	}\n" + 
				"}\n",
				"B.java", //---------------------------
				"public class B<X>{\n" + 
				"	X get(B<X> bx) { return null; }\n" + 
				"	static B<B<D>> start() { return null; }\n" + 
				"}",
				"D.java", //---------------------------
				"public class D<Y>{}\n",
			},
		"----------\n" + 
		"1. WARNING in C.java (at line 4)\n" + 
		"	new B().start().get(new B().start()).get(new B().start());\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"The static method start() from the type B should be accessed in a static way\n" + 
		"----------\n" + 
		"2. WARNING in C.java (at line 4)\n" + 
		"	new B().start().get(new B().start()).get(new B().start());\n" + 
		"	                    ^^^^^^^^^^^^^^^\n" + 
		"The static method start() from the type B should be accessed in a static way\n" + 
		"----------\n" + 
		"3. ERROR in C.java (at line 4)\n" + 
		"	new B().start().get(new B().start()).get(new B().start());\n" + 
		"	                                     ^^^\n" + 
		"The method get(B<D>) in the type B<D> is not applicable for the arguments (B<B<D>>)\n" + 
		"----------\n" + 
		"4. WARNING in C.java (at line 4)\n" + 
		"	new B().start().get(new B().start()).get(new B().start());\n" + 
		"	                                         ^^^^^^^^^^^^^^^\n" + 
		"The static method start() from the type B should be accessed in a static way\n" + 
		"----------\n");
	}
	public void test297() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.HashMap;\n" + 
				"import java.util.Iterator;\n" + 
				"import java.util.Map;\n" + 
				"\n" + 
				"public class X {\n" + 
				"		 public static void main(String[] args) {\n" + 
				"		 		 Map<String, String> map = new HashMap<String, String>();\n" + 
				"		 		 \n" + 
				"		 		 map.put(\"foo\", \"bar\");\n" + 
				"		 		 \n" + 
				"		 		 // Error reported on the following line\n" + 
				"		 		 Iterator<Map.Entry<String,String>> i = map.entrySet().iterator();\n" + 
				"		 		 while (i.hasNext()) {\n" + 
				"		 		 		 Map.Entry<String, String> entry = i.next();\n" + 
				"		 		 		 System.out.println(entry.getKey() + \", \" + entry.getValue());\n" + 
				"		 		 }\n" + 
				"		 }\n" + 
				"}\n",
			},
			"foo, bar");	
	}
	// 72644
	// TODO (kent) this should generate:
	// X is not abstract and does not override abstract method putAll(java.util.Map<? extends java.lang.String,? extends V>) in java.util.Map
	public void test298() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.Collection;\n" + 
				"import java.util.Map;\n" + 
				"import java.util.Set;\n" + 
				"\n" + 
				"public class X<V> implements Map<String, V> {\n" + 
				"   private Map<String, V> backingMap;\n" + 
				"   \n" + 
				"   public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"   }\n" + 
				"   public int size() { return 0; }\n" + 
				"   public boolean isEmpty() { return false; }\n" + 
				"   public boolean containsKey(Object key) { return false; }\n" + 
				"   public boolean containsValue(Object value) { return false; }\n" + 
				"   public V get(Object key) { return null; }\n" + 
				"   public V put(String key, V value) { return null; }\n" + 
				"   public V remove(Object key) { return null; }\n" + 
				"   public void clear() { }\n" + 
				"   public Set<String> keySet() { return null; }\n" + 
				"   public Collection<V> values() { return null; }\n" + 
				"   public void putAll(Map<String, ? extends V> t) { }\n" + 
				"   public Set<Map.Entry<String, V>> entrySet() {\n" + 
				"      return this.backingMap.entrySet();\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");
/* Also
name clash: putAll(java.util.Map<? extends java.lang.String,? extends V>) in GenericMap<S,V> and putAll(java.util.Map<? extends K,? extends V>)
in java.util.Map<S,V> have the same erasure, yet neither overrides the other

abstract class GenericMap<S, V> implements java.util.Map<S, V> {
   public void putAll(java.util.Map<? extends String, ? extends V> t) { }
}
 */
	}
	// 74244
	public void test299() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X {\n" + 
				" public static void main(String argv[]) {\n" + 
				" 	System.out.println(Boolean.class == boolean.class ? \"FAILED\" : \"SUCCESS\");\n" + 
				" }\n" + 
				"}\n",
			},
			"SUCCESS");	
	}	
	// 74119
	public void _test300() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X {\n" + 
				"    static interface I extends Visitible<I> {\n" + 
				"    }\n" + 
				"    static interface Visitible<T> {\n" + 
				"        void acceptVisitor(Visitor<? super T> visitor);\n" + 
				"    }\n" + 
				"    static interface Visitor<T> {\n" + 
				"        void visit(T t);\n" + 
				"    }\n" + 
				"    static class C implements I {\n" + 
				"        public void acceptVisitor(Visitor<? super I> visitor) {\n" + 
				"            visitor.visit(this); // should be ok\n" + 
				"            visitor.visit((I) this); // (2) This is a workaround\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");	
	}
	// 74320: check no complaint for unused private method
	public void test301() {
		this.runNegativeTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.List;\n" + 
				"public class X {\n" + 
				"	public static void reverse(List<?> list) { \n" + 
				"		rev(list);\n" + 
				"	}\n" + 
				"	private static <T> void rev(List<T> list) {\n" + 
				"	}\n" + 
				"	Zork foo() {\n" + 
				"	}\n" + 				
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	Zork foo() {\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");	
	}		
	// 74514
	public void test302() {
		this.runNegativeTest(
			new String[] {
				"X.java", //---------------------------
				"import java.util.ArrayList;\n" + 
				"import java.util.Enumeration;\n" + 
				"import java.util.Iterator;\n" + 
				"import java.util.List;\n" + 
				"public class X {\n" + 
				"	public void test2() {\n" + 
				"		List<String> l= new ArrayList<String>();\n" + 
				"		for (Iterator<String> i= l.iterator(); i.next(); ) {\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	import java.util.Enumeration;\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import java.util.Enumeration is never used\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	for (Iterator<String> i= l.iterator(); i.next(); ) {\n" + 
			"	                                       ^^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to boolean\n" + 
			"----------\n");	
	}		
	// 74544
	public void test303() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public  class X {\n" + 
				"  	public static void main(String[] args) {\n" + 
				"  		Y<String> ys = new Y<String>();\n" + 
				"	    Y<String>.Member m = ys.new Member();\n" + 
				"	    m.foo();\n" + 
				"  	}    \n" + 
				"  }\n" + 
				"  class Y<T> {\n" + 
				"    class Member {\n" + 
				"    	void foo(){\n" + 
				"    		System.out.println(\"SUCCESS\");\n" + 
				"    	}\n" + 
				"    }\n" + 
				"  }\n" + 
				"\n",
			},
			"SUCCESS");	
	}
	// 74592
	public void test304() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X<T extends Y> {}\n" + 
				"class Y extends X {}"
			},
			"");	
	}
	// 74420
	public void test305() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X<T> {\n" + 
				"  	T x;\n" + 
				"  	<U extends T> T foo(U u) { return u; }\n" + 
				"}\n"
			},
			"");
	}
	// 74096
	public void test306() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X<T extends X<T>> {\n" + 
				"  	private int i = 1;\n" + 
				"  	private int i() {return i;}\n" + 
				"  	private static class M { private static int j = 2; }\n" + 
				"  	public int foo(T t) { return t.i + t.i() + T.M.j; }\n" + 
				"}\n"
			},
			"");	
	}
	// 72583
	public void test307() {
		this.runConformTest(
			new String[] {
				"X.java", //---------------------------
				"public class X {\n" + 
				"	static <T> T foo(T t1, T t2){ return t1; }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		IX s = null;\n" + 
				"		 foo(new Object(), s);\n" + 
				"	}\n" + 
				"}\n" +
				"interface IX {}\n"
			},
			"");	
	}	
	// 73696
	public void test308() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" + 
					"public class X<T> {\n" + 
					"	class Member {}\n" + 
					"}\n",
				"p/Y.java",
				"package p;\n" + 
					"public class Y {\n" + 
					"	p.X.Member m;\n" + 
					"	p.X<String>.Member ms = m;\n" + 
					"}\n"
			});	
	}	
	public void test309() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" + 
					"public class X<T> {\n" + 
					"	class Member {\n" + 
					"		class Sub {}\n" + 
					"	}\n" + 
					"}\n",
				"p/Y.java",
				"package p;\n" + 
					"public class Y {\n" + 
					"	p.X.Member.Sub s;\n" + 
					"	p.X<Exception>.Member.Sub es = s;\n" + 
					"}\n"
			});	
	}	
	// 75156 - should report name clash
	// TODO (kent) reenable once addressed
	  public void _test310() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"public class X extends X2 {\n" + 
				"	void foo(List<X> lx) { }\n" + 
				"}\n" + 
				"\n" + 
				"abstract class X2 {\n" + 
				"	void foo(List<Object> lo) { }\n" + 
				"}"
			},
			"report name clash");	
	}
	// 75156 variation - should report name clash and ambiguity
	// TODO (kent) reenable once addressed
	public void _test311() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"public class X extends X2 {\n" + 
				"	void foo(List<X> lx) { }\n" + 
				"	void bar(){\n" + 
				"		this.foo((List)null);\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"abstract class X2 {\n" + 
				"	void foo(List<Object> lo) { }\n" + 
				"}"
			},
			"should report name clash and ambiguity");	
	}		
	// 75156 variation - should report name clash instead of final method override
	// TODO (kent) reenable once addressed
	public void _test312() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"public class X extends X2 {\n" + 
				"	void foo(List<X> lx) { }\n" + 
				"}\n" + 
				"\n" + 
				"abstract class X2 {\n" + 
				"	final void foo(List<Object> lo) { }\n" + 
				"}"	
			},
			"should report name clash");	
	}
	// 73963 
	public void test313() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.net.Inet6Address;\n" + 
				"import java.net.InetAddress;\n" + 
				"import java.util.AbstractList;\n" + 
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public class X {\n" + 
				"void takeAbstract(AbstractList<? extends InetAddress> arg) { }\n" + 
				"\n" + 
				"void takeList(List<? extends InetAddress> arg) { }\n" + 
				"\n" + 
				"void construct() {\n" + 
				"	AbstractList<InetAddress> a= new ArrayList<InetAddress>();\n" + 
				"	takeAbstract(a);\n" + 
				"	takeAbstract(new ArrayList<InetAddress>()); // a inlined: error 1:\n" + 
				"//The method takeAbstract(AbstractList<? extends InetAddress>) in the type A\n" + 
				"// is not applicable for the arguments (ArrayList<InetAddress>)\n" + 
				"	\n" + 
				"	List<InetAddress> l= new ArrayList<InetAddress>();\n" + 
				"	takeList(l);\n" + 
				"	takeList(new ArrayList<InetAddress>()); // l inlined: ok\n" + 
				"	\n" + 
				"	ArrayList<? extends InetAddress> aw= new ArrayList<InetAddress>();\n" + 
				"	takeAbstract(aw);\n" + 
				"	takeAbstract(new ArrayList<Inet6Address>()); // aw inlined: error 2:\n" + 
				"//The method takeAbstract(AbstractList<? extends InetAddress>) in the type A\n" + 
				"// is not applicable for the arguments (ArrayList<Inet6Address>)\n" + 
				"\n" + 
				"	takeList(aw);\n" + 
				"	takeList(new ArrayList<Inet6Address>()); //aw inlined: ok\n" + 
				"}\n" + 
				"}"
			},
			"");	
	}				
}
