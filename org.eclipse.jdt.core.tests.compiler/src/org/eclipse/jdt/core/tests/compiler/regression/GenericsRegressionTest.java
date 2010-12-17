/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 282152 - [1.5][compiler] Generics code rejected by Eclipse but accepted by javac
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericsRegressionTest extends AbstractComparableTest {

	public GenericsRegressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test322531j" };
//		TESTS_NAMES = new String[] { "test1464" };
//		TESTS_NUMBERS = new int[] { 1465 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return GenericsRegressionTest.class;
	}

	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz == clazz || \n" +
				"            X.class == X.class || \n" +
				"            I.class == I.class || \n" +
				"            clazz == X.class || \n" +
				"            X.class == clazz || \n" +
				"            clazz == I.class || \n" +
				"            I.class == clazz || \n" +
				"            I.class == X.class ||\n" +
				"            X.class == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz == clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	clazz == X.class || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	X.class == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<T>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X.class == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz == clazz || \n" +
				"            X.class == X.class || \n" +
				"            I.class == I.class || \n" +
				"            clazz == X.class || \n" +
				"            X.class == clazz || \n" +
				"            clazz == I.class || \n" +
				"            I.class == clazz || \n" +
				"            I.class == X.class ||\n" +
				"            X.class == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz == clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 13)\n" + 
			"	X.class == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public final class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x.getClass() == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#1-of ? extends X> and Class<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	clazz == x.getClass(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<capture#2-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531e() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public final class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz, X x) {\n" +
				"        boolean b = \n" +
				"            x.getClass() == clazz || \n" +
				"            clazz == x.getClass(); \n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	x.getClass() == clazz || \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#1-of ? extends X> and Class<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	clazz == x.getClass(); \n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<capture#2-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface List<E> {}\n" +
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    void main(List<I> li, X t) {\n" +
				"        boolean b = I.class == t.getClass();\n" +
				"	         b = li == t.getList();\n" +
				"    }\n" +
				"    \n" +
				"    List<? extends Object> getList() {\n" +
				"    	return null;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	boolean b = I.class == t.getClass();\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<capture#1-of ? extends X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531h() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X implements I {\n" +
				"    <T extends I> void main(Class<T> clazz, X t) {\n" +
				"        boolean b = \n" +
				"            clazz == t.getClass() || \n" +
				"            t.getClass() == clazz || \n" +
				"            I.class == t.getClass() ||\n" +
				"            t.getClass() == I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	I.class == t.getClass() ||\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<capture#3-of ? extends X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	t.getClass() == I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<capture#4-of ? extends X> and Class<I>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {};\n" +
				"public class X {\n" +
				"    public X() {\n" +
				"    }\n" +
				"    public <T extends I> void test(Class<T> clazz) {\n" +
				"        Class<I> ci = I.class;\n" +
				"        Class<X> ti = X.class;\n" +
				"        boolean b = ci == X.class ||\n" +
				"        	        X.class == ci ||\n" +
				"        			I.class == X.class ||\n" +
				"        			X.class == I.class ||\n" +
				"        			ti == I.class ||\n" +
				"        			I.class == ti ||\n" +
				"        			ti == ci ||\n" +
				"        			ci == ti;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	boolean b = ci == X.class ||\n" + 
			"	            ^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	X.class == ci ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 10)\n" + 
			"	I.class == X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 11)\n" + 
			"	X.class == I.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 12)\n" + 
			"	ti == I.class ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 13)\n" + 
			"	I.class == ti ||\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 14)\n" + 
			"	ti == ci ||\n" + 
			"	^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 15)\n" + 
			"	ci == ti;\n" + 
			"	^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void _test322531j() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"public class X {\n" +
				"    <T extends I> void main(Class<T> clazz) {\n" +
				"        boolean b = \n" +
				"            clazz != clazz || \n" +
				"            X.class != X.class || \n" +
				"            I.class != I.class || \n" +
				"            clazz != X.class || \n" +
				"            X.class != clazz || \n" +
				"            clazz != I.class || \n" +
				"            I.class != clazz || \n" +
				"            I.class != X.class ||\n" +
				"            X.class != I.class;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	clazz != clazz || \n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	clazz != X.class || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<T> and Class<X>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	X.class != clazz || \n" + 
			"	^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<T>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	I.class != X.class ||\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<I> and Class<X>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X.class != I.class;\n" + 
			"	^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types Class<X> and Class<I>\n" + 
			"----------\n");
	}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
public void test282152() {
    this.runConformTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}",
            "Impl.java",
            "public class Impl<T extends Number> implements Test<T>{\n" +
            "    T val;" +
            "    public <U> void test(Test<? super U> t, U value) {\n" +
            "        t.setValue(value);\n" +
            "    }\n" +
            "    public void setValue(T v) {\n" +
            "        this.val = v;\n" +
            "    }\n" +
            "}",
            "Client.java",
            "public class Client {\n" +
            "    void test() {\n" +
            "        Impl<Integer> t1 = new Impl<Integer>();\n" +
            "        Double n = Double.valueOf(3.14);\n" +
            "        t1.test(new Impl<Number>(), n);\n" +
            "    }\n" +
            "}\n"
        },
        ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// violating lower bound
public void test282152b() {
    this.runNegativeTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}",
            "Impl.java",
            "public class Impl<T extends Number> implements Test<T>{\n" +
            "    T val;" +
            "    public <U> void test(Test<? super U> t, U value) {\n" +
            "        t.setValue(value);\n" +
            "    }\n" +
            "    public void setValue(T v) {\n" +
            "        this.val = v;\n" +
            "    }\n" +
            "}",
            "Client.java",
            "public class Client {\n" +
            "    void test() {\n" +
            "        Impl<Integer> t1 = new Impl<Integer>();\n" +
            "        Number n = Double.valueOf(3.14);\n" +
            "        t1.test(new Impl<Double>(), n);\n" +
            "    }\n" +
            "}\n"
        },
        "----------\n" + 
		"1. ERROR in Client.java (at line 5)\n" + 
		"	t1.test(new Impl<Double>(), n);\n" + 
		"	   ^^^^\n" + 
		"The method test(Test<? super U>, U) in the type Impl<Integer> is not applicable for the arguments (Impl<Double>, Number)\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// contradictory bounds
public void test282152c() {
    this.runNegativeTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U extends Exception> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}"
        },
        "----------\n" + 
		"1. ERROR in Test.java (at line 2)\n" + 
		"	public <U extends Exception> void test(Test<? super U> t, U value);\n" + 
		"	                                            ^^^^^^^^^\n" + 
		"Bound mismatch: The type ? super U is not a valid substitute for the bounded parameter <T extends Number> of the type Test<T>\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// compatible constraints
public void test282152d() {
    this.runConformTest(
        new String[] {
            "Test.java",
            "public interface Test<T extends Number> {\n" +
            "    public <U extends Integer> void test(Test<? super U> t, U value);\n" +
            "    public void setValue(T v);" +
            "}",
            "Impl.java",
            "public class Impl<T extends Number> implements Test<T>{\n" +
            "    T val;" +
            "    public <U extends Integer> void test(Test<? super U> t, U value) {\n" +
            "        t.setValue(value);\n" +
            "    }\n" +
            "    public void setValue(T v) {\n" +
            "        this.val = v;\n" +
            "    }\n" +
            "}",
            "Client.java",
            "public class Client {\n" +
            "    void test() {\n" +
            "        Impl<Integer> t1 = new Impl<Integer>();\n" +
            "        Integer i = Integer.valueOf(3);\n" +
            "        t1.test(new Impl<Integer>(), i);\n" +
            "    }\n" +
            "}\n"
        },
        ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// direct use of type variable does not involve capture, thus no merging of constraints happens
public void test282152e() {
	this.runNegativeTest(
	    new String[] {
	        "Test.java",
	        "public interface Test<T extends Number> {\n" +
	        "    public <U> void test(Test<U> t, U value);\n" +
	        "    public void setValue(T v);" +
	        "}"
	    },
	    "----------\n" + 
		"1. ERROR in Test.java (at line 2)\n" + 
		"	public <U> void test(Test<U> t, U value);\n" + 
		"	                          ^\n" + 
		"Bound mismatch: The type U is not a valid substitute for the bounded parameter <T extends Number> of the type Test<T>\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330869
public void test330869() {
    this.runConformTest(
            new String[] {
                    "X.java",
                    "public class X {\n" +
                    "    public <T> T getAdapter(Class<? extends T> adapterType) {\n" +
                    "        T result = null;\n" +
                    "        if (adapterType == Foo.class) {\n" +
                    "        }\n" +
                    "        else if (adapterType == Bar.class) {\n" +
                    "        }\n" +
                    "        return  result;\n" +
                    "     }\n" +
                    "     public class Foo {\n" +
                    "     }\n" +
                    "     public interface Bar {\n" +
                    "     }\n" +
                    "}\n"
            },
            ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public Object getAdapter(Class clazz);    \n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	public Object getAdapter(Class clazz);    \n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	public Object getAdapter(Class clazz) {\n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class<String> clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	public Object getAdapter(Class clazz) {\n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface Adaptable {\n" +
					"    public Object getAdapter(Class<String> clazz);    \n" +
					"}\n" +
					"public class X implements Adaptable {\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n" +
					"class Y extends X {\n" +
					"    @Override\n" +
					"    public Object getAdapter(Class clazz) {\n" +
					"        return null;\n" +
					"    }\n" +
					"}\n"

			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	public Object getAdapter(Class clazz) {\n" + 
			"	                         ^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817e() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List get() { return null; } // OK to warn in 1.5 code\n" +
					"}\n" +
					"class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should not warn (overrides)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public List get() { return null; } // OK to warn in 1.5 code\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
public void test322817f() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List<String> get() { return null; }\n" +
					"}\n" +
					"class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should warn (super's return type is not raw)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 11)\n" + 
			"	public List get() { // should warn (super\'s return type is not raw)\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 11)\n" + 
			"	public List get() { // should warn (super\'s return type is not raw)\n" + 
			"	       ^^^^\n" + 
			"Type safety: The return type List for get() from the type Sub needs unchecked conversion to conform to List<String> from the type Top\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (Disable reporting of unavoidable problems)
public void test322817g() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"Top.java",
					"import java.util.List;\n" +
					"public class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List get() { return null; } // OK to warn in 1.5 code\n" +
					"    List list; // OK to warn in 1.5 code\n" +
					"}\n",
					"Sub.java",
					"import java.util.List;\n" +
					"public class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\"); // should not warn ('arg' is forced raw)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should not warn (overrides)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n",
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().list.add(\"arg\"); // should not warn (uses raw field declared elsewhere)\n" +
					"        new Top().get().add(\"arg\"); // should not warn (uses raw API)\n" +
					"        List raw= new Top().get(); // OK to warn ('raw' declared here)\n" +
					"        raw.add(\"arg\"); // OK to warn ('raw' declared here)\n" +
					"        // When Top#get() is generified, both of the following will fail\n" +
					"        // with a compile error if type arguments don't match:\n" +
					"        List<String> unchecked= new Top().get(); // should not warn (forced)\n" +
					"        unchecked.add(\"x\");\n" +
					"        // Should not warn about unchecked cast, but should warn about\n" +
					"        // unnecessary cast:\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in Top.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Top.java (at line 4)\n" + 
			"	public List get() { return null; } // OK to warn in 1.5 code\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Top.java (at line 5)\n" + 
			"	List list; // OK to warn in 1.5 code\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	List raw= new Top().get(); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 7)\n" + 
			"	raw.add(\"arg\"); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (Enable reporting of unavoidable problems)
public void test322817h() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
			new String[] {
					"Top.java",
					"import java.util.List;\n" +
					"public class Top {\n" +
					"    public void set(List arg) { }\n" +
					"    public List get() { return null; }\n" +
					"    List list;\n" +
					"}\n",
					"Sub.java",
					"import java.util.List;\n" +
					"public class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) {\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\");\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() {\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n",
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().list.add(\"arg\");\n" +
					"        new Top().get().add(\"arg\");\n" +
					"        List raw= new Top().get();\n" +
					"        raw.add(\"arg\");\n" +
					"        List<String> unchecked= new Top().get();\n" +
					"        unchecked.add(\"x\");\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in Top.java (at line 3)\n" + 
			"	public void set(List arg) { }\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Top.java (at line 4)\n" + 
			"	public List get() { return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Top.java (at line 5)\n" + 
			"	List list;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in Sub.java (at line 4)\n" + 
			"	public void set(List arg) {\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Sub.java (at line 6)\n" + 
			"	arg.set(0, \"A\");\n" + 
			"	^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method set(int, Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Sub.java (at line 9)\n" + 
			"	public List get() {\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	new Top().list.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	new Top().get().add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	List raw= new Top().get();\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 7)\n" + 
			"	raw.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 8)\n" + 
			"	List<String> unchecked= new Top().get();\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked cast from List to List<String>\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (Default options)
public void test322817i() {
	Map customOptions = getCompilerOptions();
	this.runNegativeTest(
			new String[] {
					"Top.java",
					"import java.util.List;\n" +
					"public class Top {\n" +
					"    public void set(List arg) { }\n" +
					"    public List get() { return null; }\n" +
					"    List list;\n" +
					"}\n",
					"Sub.java",
					"import java.util.List;\n" +
					"public class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) {\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\");\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() {\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n",
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().list.add(\"arg\");\n" +
					"        new Top().get().add(\"arg\");\n" +
					"        List raw= new Top().get();\n" +
					"        raw.add(\"arg\");\n" +
					"        List<String> unchecked= new Top().get();\n" +
					"        unchecked.add(\"x\");\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in Top.java (at line 3)\n" + 
			"	public void set(List arg) { }\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Top.java (at line 4)\n" + 
			"	public List get() { return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Top.java (at line 5)\n" + 
			"	List list;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in Sub.java (at line 4)\n" + 
			"	public void set(List arg) {\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Sub.java (at line 6)\n" + 
			"	arg.set(0, \"A\");\n" + 
			"	^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method set(int, Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Sub.java (at line 9)\n" + 
			"	public List get() {\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	new Top().list.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	new Top().get().add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	List raw= new Top().get();\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 7)\n" + 
			"	raw.add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 8)\n" + 
			"	List<String> unchecked= new Top().get();\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked cast from List to List<String>\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 10)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (all in same file)
public void test322817j() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.List;\n" +
					"class Top {\n" +
					"    public void set(List arg) { } // OK to warn in 1.5 code\n" +
					"    public List get() { return null; } // OK to warn in 1.5 code\n" +
					"}\n" +
					"class Sub extends Top {\n" +
					"    @Override\n" +
					"    public void set(List arg) { // should not warn (overrides)\n" +
					"        super.set(arg);\n" +
					"        arg.set(0, \"A\"); // should not warn ('arg' is forced raw)\n" +
					"    }\n" +
					"    @Override\n" +
					"    public List get() { // should not warn (overrides)\n" +
					"        return super.get();\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"    void run() {\n" +
					"        new Top().get().add(\"arg\");\n" +
					"        List raw= new Top().get(); // OK to warn ('raw' declared here)\n" +
					"        raw.add(\"arg\"); // OK to warn ('raw' declared here)\n" +
					"        List<String> unchecked= new Top().get();\n" +
					"        unchecked.add(\"x\");\n" +
					"        List<String> cast= (List<String>) new Top().get();\n" +
					"        cast.add(\"x\");\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public void set(List arg) { } // OK to warn in 1.5 code\n" + 
			"	                ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public List get() { return null; } // OK to warn in 1.5 code\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 19)\n" + 
			"	new Top().get().add(\"arg\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 20)\n" + 
			"	List raw= new Top().get(); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 21)\n" + 
			"	raw.add(\"arg\"); // OK to warn (\'raw\' declared here)\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 22)\n" + 
			"	List<String> unchecked= new Top().get();\n" + 
			"	                        ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 24)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: Unchecked cast from List to List<String>\n" + 
			"----------\n" + 
			"8. WARNING in X.java (at line 24)\n" + 
			"	List<String> cast= (List<String>) new Top().get();\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unnecessary cast from List to List<String>\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 (make sure there is no NPE when receiver is null)
public void test322817k() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.util.Arrays;\n" +
					"import java.util.Set;\n" +
					"import java.util.HashSet;\n" +
					"public class X {\n" +
					"    public void foo(String[] elements) {\n" +
					"	     Set set= new HashSet(Arrays.asList(elements));\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	Set set= new HashSet(Arrays.asList(elements));\n" + 
			"	^^^\n" + 
			"Set is a raw type. References to generic type Set<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 6)\n" + 
			"	Set set= new HashSet(Arrays.asList(elements));\n" + 
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The constructor HashSet(Collection) belongs to the raw type HashSet. References to generic type HashSet<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	Set set= new HashSet(Arrays.asList(elements));\n" + 
			"	             ^^^^^^^\n" + 
			"HashSet is a raw type. References to generic type HashSet<E> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
}
}