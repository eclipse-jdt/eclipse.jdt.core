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
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
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
public void _test001() { // TODO reenable once generics are supported
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

public void _test002() { // TODO reenable once generics are supported
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
		"Cannot refer to the type variable X\n" + 
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
		"Cannot refer to the type variable X\n" + 
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
		"Cannot refer to the type variable T\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 13)\n" + 
		"	static class SMX extends T {\n" + 
		"	                         ^\n" + 
		"Cannot refer to the type variable T\n" + 
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
		"The type java.lang.Object cannot be declared as generic\n" + 
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
		"The type argument Foo does not satisfy the parameter T bound: \'Object & Comparable\' in type X<T>\n" + 
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

public static Class testClass() {
	return GenericTypeTest.class;
}
}
