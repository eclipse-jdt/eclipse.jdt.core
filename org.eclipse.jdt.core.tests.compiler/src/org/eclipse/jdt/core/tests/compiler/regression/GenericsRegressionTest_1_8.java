/*******************************************************************************
 * Copyright (c) 2013 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class GenericsRegressionTest_1_8 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug424038" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public GenericsRegressionTest_1_8(String name) {
	super(name);
}
public static Class testClass() {
	return GenericsRegressionTest_1_8.class;
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public void testBug423070() {
	this.runConformTest(
		new String[] {
			"junk/Junk3.java",
			"package junk;\n" + 
			"\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"class ZZObject extends Object {\n" + 
			"}\n" + 
			"\n" + 
			"public class Junk3 {\n" + 
			"\n" + 
			"    public static final List EMPTY_LIST = new ArrayList<>();\n" + 
			"    public static final <T> List<T> emptyList() {\n" + 
			"        return (List<T>) EMPTY_LIST;\n" + 
			"    }\n" + 
			"    \n" + 
			"    public Junk3(List<ZZObject> list) {\n" + 
			"    }\n" + 
			"    \n" + 
			"    //FAILS - if passed as argument\n" + 
			"    public Junk3() {\n" + 
			"        this(emptyList());\n" + 
			"    }\n" + 
			"    \n" + 
			"\n" + 
			"    //WORKS - if you assign it (and lose type info?)\n" + 
			"    static List works = emptyList();\n" + 
			"    public Junk3(boolean bogus) {\n" + 
			"        this(works);\n" + 
			"    }\n" + 
			"}",
		});
}

public void testConditionalExpression1() {
	runConformTest(
		new String[] {
			"X.java",
			"class A {}\n" +
			"class B extends A {}\n" +
			"public class X {\n" +
			"	<T> T combine(T x, T y) { return x; }\n" +
			"	A test(A a, B b, boolean flag) {\n" +
			"		return combine(flag ? a : b, a);\n" +
			"	}\n" +
			"}\n"
		});
}

public void _testConditionalExpression2() {
	runConformTest(
		new String[] {
			"X.java",
			"class A{/**/}\n" + 
			"class B extends A {/**/}\n" + 
			"class C extends B {/**/}\n" + 
			"class G<T> {/**/}\n" + 
			"\n" + 
			"public class X {\n" + 
			"G<A> ga=null;\n" + 
			"G<B> gb=null;\n" + 
			"G<C> gc=null;\n" + 
			"G<? super A> gsa=null;\n" + 
			"G<? super B> gsb=null;\n" + 
			"G<? super C> gsc=null;\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\")\n" + 
			"    public void test(boolean f) {\n" + 
			"		G<? super B> l1 = (f) ? gsa : gb;\n" +
			"		G<? super B> l2 = (f) ? gsb : gb;\n" +
			"       G<? super C> l3 = (f) ? gsc : gb;\n" +
			"       G<? super B> l4 = (f) ? gsb : gsb;\n" +
			"	}\n" +
			"}"
		});
}
public void testBug423839() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test<T> {\n" + 
			"\n" + 
			"    public <T> T randomElement(Collection<T> list) {\n" + 
			"        return randomElement(list instanceof List ? list : new ArrayList<>(list));\n" + 
			"    }\n" + 
			"\n" + 
			"}\n"
		});
}
public void testBug418807() {
	runConformTest(
		new String[] {
			"Word.java",
			"import java.util.Arrays;\n" + 
			"import java.util.List;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			" \n" + 
			"public class Word {\n" + 
			"	private final String str;\n" + 
			"\n" + 
			"	public Word(String s) {\n" + 
			"		str = s;\n" + 
			"	}\n" + 
			"\n" + 
			"	@Override\n" + 
			"	public String toString() {\n" + 
			"		return str;\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		List<String> names = Arrays.asList(\"Aaron\", \"Jack\", \"Ben\");\n" + 
			"		Stream<Word> ws = names.stream().map(Word::new);\n" + 
			"		List<Word> words = ws.collect(Collectors.toList());\n" + 
			"		words.forEach(System.out::println);\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug414631() {
	runConformTest(
		new String[] {
			"test/Y.java",
			"package test;\n" + 
			"import java.util.function.Supplier;\n" + 
			"public abstract class Y<E>  {\n" + 
			"  public static <E> Y<E> empty() { return null;}\n" + 
			"  public static <E> Y<E> cons(E head, Supplier<Y<E>> tailFun) {return null;}\n" + 
			"}",
			"test/X.java",
			"package test;\n" + 
			"import static test.Y.*;\n" + 
			"public class X  {\n" + 
			"  public void foo() {\n" + 
			"    Y<String> generated = cons(\"a\", () -> cons(\"b\", Y::<String>empty));\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug424038() {
	runNegativeTest(
		new String[] {
			"Foo.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"public class Foo<E> {\n" + 
			"\n" + 
			"    public void gather() {\n" + 
			"        StreamLike<E> stream = null;\n" + 
			"        List<Stuff<E>> list1 = stream.gather(() -> new Stuff<>()).toList();\n" + 
			"        List<Consumer<E>> list2 = stream.gather(() -> new Stuff<>()).toList(); // ERROR\n" + 
			"    }\n" + 
			"\n" + 
			"    interface StreamLike<E> {\n" + 
			"        <T extends Consumer<E>> StreamLike<T> gather(Supplier<T> gatherer);\n" + 
			"\n" + 
			"        List<E> toList();\n" + 
			"    }\n" + 
			"\n" + 
			"    static class Stuff<T> implements Consumer<T> {\n" + 
			"        public void accept(T t) {}\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Foo.java (at line 8)\n" + 
		"	List<Consumer<E>> list2 = stream.gather(() -> new Stuff<>()).toList(); // ERROR\n" + 
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from List<Foo<E>.Stuff<E>> to List<Consumer<E>>\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference" 
public void testBug423504() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X  {\n" + 
			"  public static void main(String argv[]) {\n" + 
			"    I<? extends Collection<String>> sorter = (List<String> m) -> { /* sort */ };\n" + 
			"  }\n" + 
			"} \n" + 
			"\n" + 
			"interface I<T> { \n" + 
			"  public void sort(T col);\n" + 
			"}\n"
		});
}
}
