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

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeTest extends AbstractRegressionTest {
public GenericTypeTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
	return options;
}
public static Test suite() {
	return setupSuite(testClass());
}
public void _test001() { // TODO reenable once parameterized supertypes are supported
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
			"\n" + 
			"class XS <Txs> {\n" + 
			"    Txs get(Txs t) {\n" + 
			"        return t;\n" + 
			"    }\n" + 
			"}\n"
		},
		"SUCCESS");
}

public void _test002() { // TODO reenable once parameterized supertypes are supported
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
		"{XS::get}{X::get}SUCCESS");
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
		"Cannot refer to the type variable X as a supertype\n" + 
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
		"Cannot refer to the type variable X as a supertype\n" + 
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
		"Cannot make a static reference to the type variable T\n" + 
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
		"Cannot make a static reference to the type variable T\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	static void foo(T wrong2) {\n" + 
		"	                ^\n" + 
		"Cannot make a static reference to the type variable T\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	T wrong3;\n" + 
		"	^\n" + 
		"Cannot make a static reference to the type variable T\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 10)\n" + 
		"	class MX extends T {\n" + 
		"	                 ^\n" + 
		"Cannot refer to the type variable T as a supertype\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 13)\n" + 
		"	static class SMX extends T {\n" + 
		"	                         ^\n" + 
		"Cannot refer to the type variable T as a supertype\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 14)\n" + 
		"	T wrong4;\n" + 
		"	^\n" + 
		"Cannot make a static reference to the type variable T\n" + 
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
		"Cannot make a static reference to the type variable T\n" + 
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
		"Cannot make a static reference to the type variable T\n" + 
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
		"1. ERROR in Object.java (at line 1)\n" + 
		"	package java.lang;\n" + 
		"	^\n" + 
		"The type java.lang.Object cannot be declared as a generic\n" + 
		"----------\n");
}

// TODO reenable once wildcards are supported
public void _test010() {
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
		"Type mismatch: Cannot convert from Foo to the bounded parameter <T extends Object & Comparable> of the type X\n" + 
		"----------\n");
}

// TODO reenable once wildcards are supported
public void _test011() {
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
			"                } catch (T t) {\n" + 
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
			"        } catch(E ex) {\n" + 
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
			"        } catch(E ex) {\n" + 
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
		// TODO (philippe) should eliminate 1st diagnosis, as foo is still used even if incorrectly
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	private T foo(T t) {\n" + 
		"	          ^^^^^^^^\n" + 
		"The private method foo(T) from the type X is never used locally\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	foo(new XY());\n" + 
		"	^^^\n" + 
		"The method foo(T) in the type X is not applicable for the arguments (XY)\n" + 
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
		"1. ERROR in X.java (at line 5)\n" + 
		"	foo(x);\n" + 
		"	^^^\n" + 
		"The method foo(T) in the type X is not applicable for the arguments (String)\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
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
			"                } catch (T t) {\n" + 
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
			"        } catch(E ex) {\n" + 
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
			"        } catch(E ex) {\n" + 
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
		"The operator += is undefined for the argument type(s) , \n" + 
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
		"Method foo(E) has the same erasure foo(Object) as another method in type X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	void foo(T t){}\n" + 
		"	     ^^^^^^^^\n" + 
		"Method foo(T) has the same erasure foo(Object) as another method in type X\n" + 
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
		"Method foo(E) has the same erasure foo(Exception) as another method in type X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	void foo(T t){}\n" + 
		"	     ^^^^^^^^\n" + 
		"Method foo(T) has the same erasure foo(Exception) as another method in type X\n" + 
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
		"Method foo(E, Thread) has the same erasure foo(Exception, Thread) as another method in type X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	void foo(Exception e, T t){}\n" + 
		"	     ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Method foo(Exception, T) has the same erasure foo(Exception, Thread) as another method in type X\n" + 
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

// TODO (kent) reenable once ambiguity is detected
public void _test038() {
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
		},
		"foo invocation is ambiguous");
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
		"Method foo(L<E>) has the same erasure foo(L) as another method in type X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	void foo(L<T> l2){}\n" + 
		"	     ^^^^^^^^^^^^\n" + 
		"Method foo(L<T>) has the same erasure foo(L) as another method in type X\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	void foo(L l){}\n" + 
		"	     ^^^^^^^^\n" + 
		"Duplicate method foo(L) in type X\n" + 
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
		"	^\n" + 
		"Illegal forward reference to type variable U\n" + 
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
// javac disagrees with us here, but issues a ClassCastException when running it
// TODO (philippe) need to reassess the behavior of this test 
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
			"				        System.out.println(\"SUCCESS:\"+x);\n" + 
			"                    }\n" + 
			"                }.run();\n" + 
			"            }\n" + 
			"        }.run();\n" + 
			"    }\n" + 
			"}\n",
		},
		"SUCCESS");
}

public static Class testClass() {
	return GenericTypeTest.class;
}
}
