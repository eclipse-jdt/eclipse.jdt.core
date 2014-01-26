/*******************************************************************************
 * Copyright (c) 2013, 2014 GK Software AG.
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
//	TESTS_NAMES = new String[] { "testBug426671b" };
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

public void testConditionalExpression2() {
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
// https://bugs.eclipse.org/420525 - [1.8] [compiler] Incorrect error "The type Integer does not define sum(Object, Object) that is applicable here"
public void _testBug420525() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.List;\n" + 
			"import java.util.concurrent.CompletableFuture;\n" + 
			"import java.util.concurrent.ExecutionException;\n" +
			"public class X {\n" +
			"	void test(List<CompletableFuture<Integer>> futures) {\n" + 
			"		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{})).thenApplyAsync( (Void v) -> {\n" + 
			"			Integer finalResult = futures.stream().map( (CompletableFuture<Integer> f) -> {\n" + 
			"				try {\n" + 
			"					return f.get();\n" + 
			"				} catch (InterruptedException | ExecutionException e) {\n" + 
			"					return 0;\n" + 
			"				}\n" + 
			"			}).reduce(0, Integer::sum);\n" + 
			"			\n" + 
			"			log(\"final result is \" + finalResult);\n" + 
			"			if (finalResult != 50){\n" + 
			"				throw new RuntimeException(\"FAILED\");\n" + 
			"			} else{\n" + 
			"				log(\"SUCCESS\");\n" + 
			"			}\n" + 
			"			\n" + 
			"			return null;\n" + 
			"		});\n" + 
			"\n" + 
			"	}\n" +
			"	void log(String msg) {}\n" +
			"}\n"
		});
}

// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=420525#c7
public void testBug420525a() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"interface I<T> {\n" + 
			"    T bold(T t);\n" + 
			"}\n" + 
			"\n" + 
			"class Main {  \n" + 
			"    public String foo(String x) { return \"<b>\" + x + \"</b>\"; }\n" + 
			"    String bar() {\n" + 
			"        I<? extends String> i = this::foo;\n" + 
			"        return i.bold(\"1\");\n" + 
			"    }  \n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 9)\n" + 
		"	return i.bold(\"1\");\n" + 
		"	         ^^^^\n" + 
		"The method bold(capture#1-of ? extends String) in the type I<capture#1-of ? extends String> is not applicable for the arguments (String)\n" + 
		"----------\n");
}

public void testBug424415() {
	runConformTest(
		new String[] {
			"X.java",
			"\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"\n" + 
			"interface Functional<T> {\n" + 
			"   T apply();\n" + 
			"}\n" + 
			"\n" + 
			"class X {\n" + 
			"    void foo(Object o) { }\n" + 
			"\n" + 
			"	<Q extends Collection<?>> Q goo(Functional<Q> s) {\n" + 
			"		return null;\n" + 
			"	} \n" + 
			"\n" + 
			"    void test() {\n" + 
			"        foo(goo(ArrayList<String>::new));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424415#c6
public void testBug424415b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"\n" + 
			"interface Functional<T> {\n" + 
			"   T apply();\n" + 
			"}\n" + 
			"\n" + 
			"class X {\n" + 
			"    void foo(Object o) { }\n" + 
			"    void foo(String str) {} \n" + 
			"\n" + 
			"    <Q extends Collection<?>> Q goo(Functional<Q> s) {\n" + 
			"        return null;\n" + 
			"    } \n" + 
			"\n" + 
			"    void test() {\n" + 
			"        foo(goo(ArrayList<String>::new));\n" + 
			"    }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424415#c8
public void testBug424415c() {
	runConformTest(
		new String[] {
			"com/example/MyEmployee.java",
			"package com.example;\n" + 
			"class MyEmployee {\n" + 
			"	\n" + 
			"	public enum Gender { MALE, FEMALE, OTHERS }\n" + 
			"\n" + 
			"	private int age = 0;\n" + 
			"	private Gender gender = Gender.MALE;\n" + 
			"	\n" + 
			"	public MyEmployee(int age, Gender gender) {\n" + 
			"		this.age = age;\n" + 
			"		this.gender = gender;\n" + 
			"	}	\n" + 
			"	\n" + 
			"	public int getAge() {\n" + 
			"		return age;\n" + 
			"	}\n" + 
			"	\n" + 
			"	public Gender getGender() {\n" + 
			"		return gender;\n" + 
			"	}\n" + 
			"}",
			"com/example/Test.java",
			"package com.example;\n" + 
			"\n" + 
			"import java.util.List;\n" + 
			"import java.util.concurrent.ConcurrentMap;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"	ConcurrentMap<MyEmployee.Gender, List<MyEmployee>> test(List<MyEmployee> el) {\n" + 
			"		return el.parallelStream()\n" + 
			"					.collect(\n" + 
			"						Collectors.groupingByConcurrent(MyEmployee::getGender)\n" + 
			"						);\n" + 
			"	}\n" + 
			"	\n" + 
			"}"
		});
}
public void testBug424631() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"\n" + 
			"interface Functional<T> {\n" + 
			"   T apply();\n" + 
			"}\n" + 
			"\n" + 
			"class X {\n" + 
			"    void foo(Collection<String> o) { }\n" + 
			"\n" + 
			"	<Q extends Collection<?>> Q goo(Functional<Q> s) {\n" + 
			"		return null;\n" + 
			"	} \n" + 
			"\n" + 
			"    void test() { \n" + 
			"        foo(goo(ArrayList<String>::new));\n" + 
			"    }\n" + 
			"}\n"
		});
}

public void testBug424403() {
	runConformTest(
		new String[] {
			"X.java",
			"interface Functional { int foo(); }\n" + 
			"\n" + 
			"public class X {\n" + 
			"    static int bar() {\n" + 
			"        return -1;\n" + 
			"    }\n" + 
			"    static <T> T consume(T t) { return null; }\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"    	Functional f = consume(X::bar);\n" + 
			"    }  \n" + 
			"}\n"
		});
}
public void testBug401850a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"import java.util.ArrayList;\n" + 
			"public class X<T> {\n" + 
			"   X(T t) {}\n" + 
			"   X(String s) {}\n" + 
			"   int m(X<String> xs) { return 0; }\n" + 
			"   int i = m(new X<>(\"\"));\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	int i = m(new X<>(\"\"));\n" + 
		"	          ^^^^^^^^^^^\n" + 
		"The constructor X<String>(String) is ambiguous\n" + 
		"----------\n");
}
public void testBug401850b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"import java.util.ArrayList;\n" + 
			"public class X<T> {\n" + 
			"   X(T t) {}\n" + 
			"   X(String s) {}\n" + 
			"   int m(X<String> xs) { return 0; }\n" + 
			"   int i = m(new X<String>(\"\"));\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	int i = m(new X<String>(\"\"));\n" + 
		"	          ^^^^^^^^^^^^^^^^^\n" + 
		"The constructor X<String>(String) is ambiguous\n" + 
		"----------\n");
}

public void testBug424710() {
	runConformTest(
		new String[] {
			"MapperTest.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Arrays;\n" + 
			"import java.util.List;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.regex.Matcher;\n" + 
			"import java.util.regex.Pattern;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"public class MapperTest {\n" + 
			"\n" + 
			"    public static void main( String... argv ){\n" + 
			"        List<String> data = Arrays.asList(\"abc\", \"123\", \"1a\", \"?!?\");\n" + 
			"        List<Pattern> patterns = Arrays.asList(Pattern.compile(\"[a-z]+\"), Pattern.compile(\"[0-9]+\"));\n" + 
			"		patterns.stream()\n" + 
			"				.flatMap(\n" + 
			"						p -> {\n" + 
			"							Stream<Matcher> map = data.stream().map(p::matcher);\n" + 
			"							Stream<Matcher> filter = map.filter(Matcher::find);\n" + 
			"							Function<? super Matcher, ? extends Object> mapper = Matcher::group;\n" + 
			"							mapper = matcher -> matcher.group();\n" + 
			"							return filter.map(mapper);\n" + 
			"						})\n" + 
			"				.forEach(System.out::println);\n" + 
			"    }\n" + 
			"}\n"
		});
}

public void testBug424075() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"import java.util.function.*;\n" + 
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        Consumer<Object> c = null;\n" + 
			"        Arrays.asList(pred(), c);\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T> Predicate<T> pred() {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug424205a() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"	void bar(String t);\n" + 
			"}\n" + 
			"class X<T> implements I {\n" + 
			"	public void bar(String t) {}\n" + 
			"	X(String x) {}\n" + 
			"	X(T x) {}\n" + 
			"	public void one(X<I> c){}\n" + 
			"	public void two() {\n" + 
			"		X<I> i = new X<>((String s) -> { });\n" + 
			"		one (i);\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug424205b() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"	void bar(String t);\n" + 
			"}\n" + 
			"public class X<T> implements I {\n" + 
			"	public void bar(String t) {}\n" + 
			"	X(String x) {}\n" + 
			"	X(T x) {}\n" + 
			"	public void one(X<I> c){}\n" + 
			"	public void two() {\n" + 
			"		one(new X<>((String s) -> { })); // 1. Three errors\n" + 
			"		X<I> i = new X<>((String s) -> { }); // 2. Error - Comment out the previous line to see this error go away.\n" + 
			"		one (i);\n" + 
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"main\");\n" +
			"		new X<Integer>(\"one\").two();\n" +
			"	}\n" + 
			"}\n"
		},
		"main");
}
public void testBug424712a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" + 
			"import java.util.function.Supplier;\n" + 
			"import java.util.Set;\n" + 
			"\n" + 
			"public class X {\n" + 
			"    public static <T, SOURCE extends Collection<T>, DEST extends Collection<T>>\n" + 
			"        DEST foo(SOURCE sourceCollection, DEST collectionFactory) {\n" + 
			"            return null;\n" + 
			"    }  \n" + 
			"    \n" + 
			"    public static void main(String... args) {\n" + 
			"        Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
		"	    ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 12)\n" + 
		"	Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);\n" + 
		"	                        ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n");
}
public void testBug424712b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Comparator;\n" + 
			"public class X {\n" +
			"	<T> void test() {\n" + 
			"		Comparator<? super T> comparator = (Comparator<? super T>) Comparator.naturalOrder();\n" +
			"		System.out.println(\"OK\");\n" + 
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().test();\n" +
			"	}\n" +
			"}\n"
		},
		"OK");
}
public void testBug425142_minimal() {
	runNegativeTest(
		new String[] {
			"SomethingBreaks.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Files;\n" + 
			"import java.nio.file.Paths;\n" + 
			"import java.util.function.Consumer;\n" + 
			"\n" + 
			"@FunctionalInterface interface Use<T, E extends Throwable> {   void accept(T t) throws E; }\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\") public class SomethingBreaks<T, E extends Throwable> {\n" + 
			"  protected static SomethingBreaks<String, IOException> stream() {     return null;  }\n" + 
			"\n" + 
			"  public void forEach(Consumer<T> use) throws E {}\n" + 
			"\n" + 
			"  public <E2 extends E> void forEach(Use<T, E2> use) throws E, E2 {}\n" + 
			"\n" + 
			"  private static void methodReference(String s) throws IOException {\n" + 
			"    System.out.println(Files.size(Paths.get(s)));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase9() throws IOException {\n" + 
			"    stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in SomethingBreaks.java (at line 20)\n" + 
		"	stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	         ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n");
}
public void testBug425142_full() {
	runNegativeTest(
		new String[] {
			"SomethingBreaks.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Files;\n" + 
			"import java.nio.file.Paths;\n" + 
			"import java.util.function.Consumer;\n" + 
			"\n" + 
			"@FunctionalInterface interface Use<T, E extends Throwable> {   void accept(T t) throws E; }\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\") public class SomethingBreaks<T, E extends Throwable> {\n" + 
			"  protected static SomethingBreaks<String, IOException> stream() {     return null;  }\n" + 
			"\n" + 
			"  public void forEach(Consumer<T> use) throws E {}\n" + 
			"\n" + 
			"  public <E2 extends E> void forEach(Use<T, E2> use) throws E, E2 {}\n" + 
			"\n" + 
			"  private static void methodReference(String s) throws IOException {\n" + 
			"    System.out.println(Files.size(Paths.get(s)));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase1() throws IOException {\n" + 
			"    Use<String, IOException> c =\n" + 
			"      (String s) -> System.out.println(Files.size(Paths.get(s)));\n" + 
			"    stream().forEach(c);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase2() throws IOException {\n" + 
			"    Use<String, IOException> c = SomethingBreaks::methodReference;\n" + 
			"    stream().forEach(c);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase3() throws IOException {\n" + 
			"    stream().forEach((Use<String, IOException>) (String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase4() throws IOException {\n" + 
			"    stream().forEach((Use<String, IOException>) SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase5() throws IOException {\n" + 
			"    stream().<IOException> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase6() throws IOException {\n" + 
			"    stream().<IOException> forEach(SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase7() throws IOException {\n" + 
			"    stream().<Use<String, IOException>> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase8() throws IOException {\n" + 
			"    stream().<Use<String, IOException>> forEach(SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase9() throws IOException {\n" + 
			"    stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
			"  }\n" + 
			"  \n" + 
			"  public static void useCase10() throws IOException {\n" + 
			"    stream().forEach(SomethingBreaks::methodReference);\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in SomethingBreaks.java (at line 39)\n" + 
		"	stream().<IOException> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	                       ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"2. ERROR in SomethingBreaks.java (at line 43)\n" + 
		"	stream().<IOException> forEach(SomethingBreaks::methodReference);\n" + 
		"	                       ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"3. ERROR in SomethingBreaks.java (at line 43)\n" + 
		"	stream().<IOException> forEach(SomethingBreaks::methodReference);\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"4. ERROR in SomethingBreaks.java (at line 47)\n" + 
		"	stream().<Use<String, IOException>> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	                                                                             ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"5. ERROR in SomethingBreaks.java (at line 51)\n" + 
		"	stream().<Use<String, IOException>> forEach(SomethingBreaks::methodReference);\n" + 
		"	                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n" + 
		"6. ERROR in SomethingBreaks.java (at line 55)\n" + 
		"	stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));\n" + 
		"	         ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"7. ERROR in SomethingBreaks.java (at line 59)\n" + 
		"	stream().forEach(SomethingBreaks::methodReference);\n" + 
		"	         ^^^^^^^\n" + 
		"The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>\n" + 
		"----------\n" + 
		"8. ERROR in SomethingBreaks.java (at line 59)\n" + 
		"	stream().forEach(SomethingBreaks::methodReference);\n" + 
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n");
}
public void testBug424195a() {
	runNegativeTest(
		new String[] {
			"NPEOnCollector.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Path;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.function.Predicate;\n" + 
			"import java.util.jar.JarEntry;\n" + 
			"import java.util.jar.JarFile;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"\n" + 
			"public class NPEOnCollector {\n" + 
			"  static void processJar(Path plugin) throws IOException {\n" + 
			"    \n" + 
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" + 
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" + 
			"        Stream<JarEntry> stream = entries\n" + 
			"          .distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
			"        \n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in NPEOnCollector.java (at line 17)\n" + 
		"	Stream<JarEntry> stream = entries\n" + 
		"          .distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Collection<JarEntry> to Stream<JarEntry>\n" + 
		"----------\n");
}
public void testBug424195b() {
	runConformTest(
		new String[] {
			"NPEOnCollector.java",
			"import java.io.IOException;\n" + 
			"import java.nio.file.Path;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collection;\n" + 
			"import java.util.function.Function;\n" + 
			"import java.util.function.Predicate;\n" + 
			"import java.util.jar.JarEntry;\n" + 
			"import java.util.jar.JarFile;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.Stream;\n" + 
			"\n" + 
			"\n" + 
			"public class NPEOnCollector {\n" + 
			"  static void processJar(Path plugin) throws IOException {\n" + 
			"    \n" + 
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" + 
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" + 
			"        Collection<JarEntry> collection = entries\n" + 
			"          .distinct().collect(Collectors.toCollection(ArrayList::new));\n" + 
			"        \n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n"
		});
}
public void testBug424195_comment2() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.PrintStream;\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.stream.Collectors;\n" + 
			"import java.util.stream.*;\n" + 
			"public class X  {\n" + 
			"\n" + 
			"    public static void main(String argv[]) {\n" + 
			"        ArrayList<Integer> al = IntStream\n" + 
			"        	     .range(0, 10_000_000)\n" + 
			"        	     .boxed()\n" + 
			"        	     .collect(Collectors.toCollection(ArrayList::new));\n" + 
			"\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425153() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"class C1 {}\n" + 
			"class C2 {}\n" + 
			"\n" + 
			"interface I<P1 extends C1, P2 extends P1> {\n" + 
			"    P2 foo(P1 p1);\n" + 
			"}\n" + 
			"\n" + 
			"public class Main  {\n" + 
			"	    public static void main(String argv[]) {\n" + 
			"	    	I<?, ?> i = (C1 c1) -> { return new C2(); };\n" + 
			"	        Object c2 = i.foo(null);\n" + 
			"	    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 10)\n" + 
		"	I<?, ?> i = (C1 c1) -> { return new C2(); };\n" + 
		"	            ^^^^^^^^^^\n" + 
		"The target type of this expression is not a well formed parameterized type due to bound(s) mismatch\n" + 
		"----------\n");
}
public void testBug424845() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.Comparator;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"    \n" + 
			"\n" + 
			"    interface Function<K, V>{\n" + 
			"        public V apply(K orig);\n" + 
			"    }\n" + 
			"    \n" + 
			"    \n" + 
			"    static class Ordering<O> {\n" + 
			"\n" + 
			"        public <K> Comparator<K> onResultOf(Function<K, ? extends O> function) {\n" + 
			"            return null;\n" + 
			"        }\n" + 
			"\n" + 
			"        \n" + 
			"    }\n" + 
			"    \n" + 
			"    public static void main(String[] args) {\n" + 
			"        List<Object> list = new ArrayList<>();\n" + 
			"        Function<Object, String> function = new Function<Object, String>() {\n" + 
			"            public String apply(Object arg0) {\n" + 
			"                return arg0.toString();\n" + 
			"            }\n" + 
			"        };\n" + 
			"        Ordering<Comparable<String>> natural = new Ordering<>();\n" + 
			"        Collections.sort(list, natural.onResultOf(function));\n" + 
			"    }\n" + 
			"    \n" + 
			"}\n"
		});
}
public void testBug425278() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<T, S extends X<T>> { \n" + 
			"    T foo(S p);\n" + 
			"}\n" + 
			"\n" + 
			"public class X<T>  {\n" + 
			"    public void bar() {\n" + 
			"    I<Object, X<Object>> f = (p) -> p; // Error\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425783() {
	runConformTest(
		new String[] {
			"Test.java",
			"class MyType<S extends MyType<S>> {\n" +
			"	S myself() { return (S)this; }\n" +
			"}\n" + 
			"public class Test {\n" +
			"	MyType test() {\n" +
			"		return newInstance().myself();\n" +
			"	}\n" +
			"	MyType test2() {\n" +
			"		return newInstance().myself();\n" +
			"	}\n" +
			"	public <T extends MyType> T newInstance() {\n" + 
			"		return (T) new MyType();\n" + 
			"	}" +
			"}\n"
		});
}
public void testBug425798() {
	runNegativeTest( // TODO: for now we just want to prove absence of NPE, should, however, be a conform test, actually.
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n" +
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import java.util.stream.*;\n" +
			"interface MyCollector<T, A, R> extends Collector<T, A, R> {\n" + 
			"}\n" +
			"public abstract class X {\n" +
			"	abstract <T, K, U, M extends Map<K, U>>\n" + 
			"    MyCollector<T, ?, M> toMap(Function<? super T, ? extends K> km,\n" + 
			"                                BinaryOperator<U> mf);" +
			"	void test(Stream<Annotation> annotations) {\n" +
			"		annotations\n" +
			"			.collect(toMap(Annotation::annotationType,\n" +
			"				 (first, second) -> first));\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	.collect(toMap(Annotation::annotationType,\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The type of annotationType() from the type Annotation is Class<? extends Annotation>, this is incompatible with the descriptor's return type: Class<capture#3-of ? extends Annotation>\n" + 
		"----------\n");
}
// witness for NPE mentioned in https://bugs.eclipse.org/bugs/show_bug.cgi?id=425798#c2
public void testBug425798b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Objects;\n" + 
			"import java.util.PrimitiveIterator;\n" + 
			"import java.util.Spliterator;\n" + 
			"import java.util.Spliterator.OfInt;\n" + 
			"import java.util.function.Consumer;\n" + 
			"import java.util.function.IntConsumer;\n" + 
			"\n" + 
			"class IntIteratorSpliterator implements OfInt {\n" + 
			"	public IntIteratorSpliterator(PrimitiveIterator.OfInt arg) { }\n" + 
			"	public void forEachRemaining(IntConsumer action) { }\n" + 
			"	public boolean tryAdvance(Consumer<? super Integer> action) { return false; }\n" + 
			"	public long estimateSize() { return 0; }\n" + 
			"	public int characteristics() { return 0; }\n" + 
			"	public OfInt trySplit() { return null; }\n" + 
			"	public boolean tryAdvance(IntConsumer action) { return false; }\n" + 
			"}\n" + 
			"public class X {\n" + 
			"\n" + 
			"	public Spliterator.OfInt spliterator(PrimitiveIterator.OfInt iterator) {\n" + 
			"		return new IntIteratorSpliterator(id(iterator));\n" + 
			"	}\n" + 
			"	<T> T id(T e) { return e; }\n" + 
			"}\n"
		});
}
public void testBug425460orig() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"public class X {\n" +
			"	final Integer[] boom =\n" + 
			"  		Arrays.asList(\"1\", \"22\", \"333\")\n" + 
			"  			.stream()\n" + 
			"  			.map(str -> str.length())\n" + 
			"  			.toArray(i -> new Integer[i]);\n" +
			"}\n"
		});
}
public void testBug425460variant() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"public class X {\n" +
			"	final Integer[] boom =\n" + 
			"  		Arrays.asList(\"1\", \"22\", \"333\")\n" + 
			"  			.stream()\n" + 
			"  			.map(str -> str.length())\n" + 
			"  			.toArray((int i) -> new Integer[i]);\n" +
			"}\n"
		});
}
public void testBug425951() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"        index(new A().test());\n" + 
			"    }\n" + 
			"\n" + 
			"    public static <X> void index(Iterable<X> collection)\n" + 
			"    {\n" + 
			"    }\n" + 
			"	\n" + 
			"    public class A<S extends A<S>>\n" + 
			"    {\n" + 
			"        protected A() {}\n" + 
			"		\n" + 
			"        public <T> List<T> test()\n" + 
			"       {\n" + 
			"            return null;\n" + 
			"       }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: Unchecked invocation index(List) of the generic method index(Iterable<X>) of type Test\n" + 
		"----------\n" + 
		"2. WARNING in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"Type safety: The expression of type List needs unchecked conversion to conform to Iterable<Object>\n" + 
		"----------\n" + 
		"3. ERROR in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	      ^^^^^^^\n" + 
		"No enclosing instance of type Test is accessible. Must qualify the allocation with an enclosing instance of type Test (e.g. x.new A() where x is an instance of Test).\n" + 
		"----------\n" + 
		"4. WARNING in Test.java (at line 6)\n" + 
		"	index(new A().test());\n" + 
		"	          ^\n" + 
		"Test.A is a raw type. References to generic type Test.A<S> should be parameterized\n" + 
		"----------\n");
}
public void testBug425951a() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"\n" + 
			"    public void test() {\n" + 
			"        index(new A().test());\n" + 
			"    }\n" + 
			"\n" + 
			"    public static <X> void index(Iterable<X> collection)\n" + 
			"    {\n" + 
			"    }\n" + 
			"	\n" + 
			"    public class A<S extends A<S>>\n" + 
			"    {\n" + 
			"        protected A() {}\n" + 
			"		\n" + 
			"        public <T> List<T> test()\n" + 
			"       {\n" + 
			"            return null;\n" + 
			"       }\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug424906() {
	runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" + 
			"	public <T> void test(Result r) {}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Main().test(r -> System.out.println(\"Hmmm...\" + r));\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"interface Result {\n" + 
			"	public void result(Object object);\n" + 
			"}"
		});
}
public void testBug425156() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<T> {\n" + 
			"    void foo(T t);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"    void bar(I<?> i) {\n" + 
			"        i.foo(null);\n" + 
			"    }\n" + 
			"    void run() {\n" + 
			"        bar((X x) -> {}); // Incompatible error reported\n" + 
			"    }\n" + 
			"}\n"
		});
}
public void testBug425493() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test {\n" + 
			"    public void addAttributeBogus(Attribute<?> attribute) {\n" + 
			"        addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
			"                attribute.getDefault());\n" + 
			"        addAttribute(attribute, attribute.getDefault());\n" + 
			"    }\n" + 
			"    public <T> void addAttributeOK(Attribute<T> attribute) {\n" + 
			"        addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
			"                attribute.getDefault());\n" + 
			"        addAttribute(attribute, attribute.getDefault());\n" + 
			"    }\n" + 
			"\n" + 
			"    private <T> void addAttribute(Attribute<T> attribute, T defaultValue) {}\n" + 
			"\n" + 
			"    static class Attribute<T> {\n" + 
			"\n" + 
			"        T getDefault() {\n" + 
			"            return null;\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Test.java (at line 3)\n" + 
		"	addAttribute(java.util.Objects.requireNonNull(attribute, \"\"),\n" + 
		"	^^^^^^^^^^^^\n" + 
		"The method addAttribute(Test.Attribute<T>, T) in the type Test is not applicable for the arguments (Test.Attribute<capture#1-of ?>, capture#2-of ?)\n" + 
		"----------\n" + 
		"2. ERROR in Test.java (at line 5)\n" + 
		"	addAttribute(attribute, attribute.getDefault());\n" + 
		"	^^^^^^^^^^^^\n" + 
		"The method addAttribute(Test.Attribute<T>, T) in the type Test is not applicable for the arguments (Test.Attribute<capture#3-of ?>, capture#4-of ?)\n" + 
		"----------\n");
}
public void testBug426366() {
	runConformTest(
		new String[] {
			"a/Test.java",
			"package a;\n" + 
			"\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"/**\n" + 
			" * @author tomschindl\n" + 
			" *\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	public static class A {\n" + 
			"		public A(B newSelectedObject, String editorController) {\n" + 
			"	    }\n" + 
			"\n" + 
			"	    public A(List<B> newSelectedObjects, String editorController) {\n" + 
			"	    }\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static class B {\n" + 
			"		\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static class C extends A {\n" + 
			"		public C() {\n" + 
			"			super(Collections.emptyList(), \"\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug426290() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class X {\n" + 
			"    public static void main(String argv[]) {\n" + 
			"       goo(foo());\n" + 
			"    }\n" + 
			"\n" + 
			"    static <T extends Number> List<T> foo() {\n" + 
			"        return new ArrayList<T>();\n" + 
			"    }\n" + 
			"\n" + 
			"    static void goo(Object p1) {\n" + 
			"        System.out.println(\"goo(Object)\");\n" + 
			"    }\n" + 
			"\n" + 
			"    static void goo(List<Integer> p1) {\n" + 
			"        System.out.println(\"goo(List<Integer>)\");\n" + 
			"    }\n" + 
			"}\n"
		},
		"goo(List<Integer>)");
}
public void testBug425152() {
	runConformTest(
		new String[] {
			"packDown/SorterNew.java",
			"package packDown;\n" + 
			"\n" + 
			"import java.util.ArrayList;\n" + 
			"import java.util.Collections;\n" + 
			"import java.util.Comparator;\n" + 
			"\n" + 
			"public class SorterNew {\n" + 
			"	void sort() {\n" + 
			"		Collections.sort(new ArrayList<Person>(),\n" + 
			"				Comparator.comparing((Person p) -> p.getName()));\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"class Person {\n" + 
			"	public String getName() {\n" + 
			"		return \"p\";\n" + 
			"	}\n" + 
			"}\n"
		});
}
public void testBug426048() {
	runNegativeTest(
		new String[] {
			"MyFunction.java",
			"import java.lang.annotation.Annotation;\n" + 
			"import java.lang.annotation.ElementType;\n" + 
			"import java.lang.annotation.Target;\n" + 
			"\n" + 
			"@Target(ElementType.TYPE_USE)\n" + 
			"@interface Throws {\n" + 
			"  Class<? extends Throwable>[] value() default Throwable.class;\n" + 
			"  Returns method() default @Returns(Annotation.class);\n" + 
			"}\n" + 
			"\n" + 
			"@Target(ElementType.TYPE_USE)\n" + 
			"@interface Returns {\n" + 
			"  Class<? extends Annotation> value() default Annotation.class;\n" + 
			"}\n" + 
			"\n" + 
			"@FunctionalInterface public interface MyFunction<T, @Returns R> {\n" + 
			"  @Returns  R apply(T t);\n" + 
			"\n" + 
			"  default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
			"    compose(MyFunction<? super V, ? extends T> before) {\n" + 
			"\n" + 
			"    return (V v) -> apply(before.apply(v));\n" + 
			"  }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	          ^\n" + 
		"Syntax error, insert \"Type Identifier (\" to complete MethodHeaderName\n" + 
		"----------\n" + 
		"2. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from MyFunction<capture#1-of ? super V,capture#2-of ? extends T> to Class<? extends Throwable>[]\n" + 
		"----------\n" + 
		"3. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	                                                          ^^^^^^\n" + 
		"before cannot be resolved\n" + 
		"----------\n" + 
		"4. ERROR in MyFunction.java (at line 19)\n" + 
		"	default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>\n" + 
		"	                                                                       ^\n" + 
		"Syntax error, insert \")\" to complete Modifiers\n" + 
		"----------\n" + 
		"5. ERROR in MyFunction.java (at line 20)\n" + 
		"	compose(MyFunction<? super V, ? extends T> before) {\n" + 
		"	       ^\n" + 
		"Syntax error on token \"(\", , expected\n" + 
		"----------\n");
}
public void testBug426540() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.stream.Stream;\n" + 
			"import java.util.Collections;\n" + 
			"import static java.util.stream.Collectors.collectingAndThen;\n" + 
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" + 
			"	Object o = ((Stream<Integer>) null).collect(collectingAndThen(toList(), Collections::unmodifiableList));\n" + 
			"}\n"
		});
}
public void _testBug426671_full() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.stream.Stream;\n" + 
			"import java.util.*;\n" + 
			"import static java.util.stream.Collectors.collectingAndThen;\n" + 
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" +
			"	void test() {\n" +
			"		Arrays.asList((List<Integer>) null).stream().collect(collectingAndThen(toList(), Collections::unmodifiableList))\n" +
			"			.remove(0);\n" +
			"	}\n" + 
			"}\n"
		});
}
public void testBug426671b() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"interface I<X,Y> {\n" + 
			"	Y fun(X y);\n" + 
			"}\n" + 
			"public class Test {\n" + 
			"	static <S> S id(S s) { return s; }\n" + 
			"	void test() {\n" + 
			"        m1(Test::id, \"Hi\");\n" + 
			"        m2(Test::id, \"Hi\").toUpperCase();\n" + 
			"        m3(Test::id, \"Hi\").toUpperCase();\n" + 
			"   }\n" + 
			"\n" + 
			"	<U,V> void m1(I<V,U> i, U u) { }\n" +
			"	<U,V> V m2(I<V,U> i, U u) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	<U,V> V m3(I<U,V> i, U u) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in Test.java (at line 8)\n" + 
		"	m2(Test::id, \"Hi\").toUpperCase();\n" + 
		"	                   ^^^^^^^^^^^\n" + 
		"The method toUpperCase() is undefined for the type Object\n" + 
		"----------\n");
}
public void testBug426652() {
	runConformTest(
		new String[] {
			"X.java",
			"import static java.util.stream.Collectors.toList;\n" + 
			"public class X {\n" + 
			"	Object o = toList();\n" + 
			"}\n"
		});
}
}
