/*******************************************************************************
 * Copyright (c) 2013, 2023 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation - additional tests
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsRegressionTest_1_8 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug496574_small" };
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
			"""
				package junk;
				
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				class ZZObject extends Object {
				}
				
				public class Junk3 {
				
				    public static final List EMPTY_LIST = new ArrayList<>();
				    public static final <T> List<T> emptyList() {
				        return (List<T>) EMPTY_LIST;
				    }
				   \s
				    public Junk3(List<ZZObject> list) {
				    }
				   \s
				    //FAILS - if passed as argument
				    public Junk3() {
				        this(emptyList());
				    }
				   \s
				
				    //WORKS - if you assign it (and lose type info?)
				    static List works = emptyList();
				    public Junk3(boolean bogus) {
				        this(works);
				    }
				}""",
		});
}

public void testConditionalExpression1() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				class A {}
				class B extends A {}
				public class X {
					<T> T combine(T x, T y) { return x; }
					A test(A a, B b, boolean flag) {
						return combine(flag ? a : b, a);
					}
				}
				"""
		});
}

public void testConditionalExpression2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				class A{/**/}
				class B extends A {/**/}
				class C extends B {/**/}
				class G<T> {/**/}
				
				public class X {
				G<A> ga=null;
				G<B> gb=null;
				G<C> gc=null;
				G<? super A> gsa=null;
				G<? super B> gsb=null;
				G<? super C> gsc=null;
				
				@SuppressWarnings("unused")
				    public void test(boolean f) {
						G<? super B> l1 = (f) ? gsa : gb;
						G<? super B> l2 = (f) ? gsb : gb;
				       G<? super C> l3 = (f) ? gsc : gb;
				       G<? super B> l4 = (f) ? gsb : gsb;
					}
				}"""
		});
}
public void testBug423839() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				
				public class Test<T> {
				
				    public <T> T randomElement(Collection<T> list) {
				        return randomElement(list instanceof List ? list : new ArrayList<>(list));
				    }
				
				}
				"""
		});
}
public void testBug418807() {
	runConformTest(
		new String[] {
			"Word.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				\s
				public class Word {
					private final String str;
				
					public Word(String s) {
						str = s;
					}
				
					@Override
					public String toString() {
						return str;
					}
				
					public static void main(String[] args) {
						List<String> names = Arrays.asList("Aaron", "Jack", "Ben");
						Stream<Word> ws = names.stream().map(Word::new);
						List<Word> words = ws.collect(Collectors.toList());
						words.forEach(System.out::println);
					}
				}
				"""
		});
}
public void testBug414631() {
	runConformTest(
		new String[] {
			"test/Y.java",
			"""
				package test;
				import java.util.function.Supplier;
				public abstract class Y<E>  {
				  public static <E> Y<E> empty() { return null;}
				  public static <E> Y<E> cons(E head, Supplier<Y<E>> tailFun) {return null;}
				}""",
			"test/X.java",
			"""
				package test;
				import static test.Y.*;
				public class X  {
				  public void foo() {
				    Y<String> generated = cons("a", () -> cons("b", Y::<String>empty));
				  }
				}
				"""
		});
}
public void testBug424038() {
	runNegativeTest(
		new String[] {
			"Foo.java",
			"""
				import java.util.*;
				import java.util.function.*;
				public class Foo<E> {
				
				    public void gather() {
				        StreamLike<E> stream = null;
				        List<Stuff<E>> list1 = stream.gather(() -> new Stuff<>()).toList();
				        List<Consumer<E>> list2 = stream.gather(() -> new Stuff<>()).toList(); // ERROR
				    }
				
				    interface StreamLike<E> {
				        <T extends Consumer<E>> StreamLike<T> gather(Supplier<T> gatherer);
				
				        List<E> toList();
				    }
				
				    static class Stuff<T> implements Consumer<T> {
				        public void accept(T t) {}
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Foo.java (at line 8)
				List<Consumer<E>> list2 = stream.gather(() -> new Stuff<>()).toList(); // ERROR
				                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from List<Foo.Stuff<E>> to List<Consumer<E>>
			----------
			""");
}

// https://bugs.eclipse.org/423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
public void testBug423504() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X  {
				  public static void main(String argv[]) {
				    I<? extends Collection<String>> sorter = (List<String> m) -> { /* sort */ };
				  }
				}\s
				
				interface I<T> {\s
				  public void sort(T col);
				}
				"""
		});
}
// https://bugs.eclipse.org/420525 - [1.8] [compiler] Incorrect error "The type Integer does not define sum(Object, Object) that is applicable here"
public void testBug420525() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.concurrent.CompletableFuture;
				import java.util.concurrent.ExecutionException;
				public class X {
					void test(List<CompletableFuture<Integer>> futures) {
						CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{})).thenApplyAsync( (Void v) -> {
							Integer finalResult = futures.stream().map( (CompletableFuture<Integer> f) -> {
								try {
									return f.get();
								} catch (InterruptedException | ExecutionException e) {
									return 0;
								}
							}).reduce(0, Integer::sum);
						\t
							log("final result is " + finalResult);
							if (finalResult != 50){
								throw new RuntimeException("FAILED");
							} else{
								log("SUCCESS");
							}
						\t
							return null;
						});
				
					}
					void log(String msg) {}
				}
				"""
		});
}
//https://bugs.eclipse.org/420525 - [1.8] [compiler] Incorrect error "The type Integer does not define sum(Object, Object) that is applicable here"
public void testBug420525_mini() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.concurrent.CompletableFuture;
				import java.util.concurrent.ExecutionException;
				public class X {
					void test(List<CompletableFuture<Integer>> futures, boolean b) {
						Integer finalResult = futures.stream().map( (CompletableFuture<Integer> f) -> {
									if (b)\s
										return 1;
									else
										return Integer.valueOf(13);\
								}).reduce(0, Integer::sum);
					}
				}
				"""
		});
}

// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=420525#c7
public void testBug420525a() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"""
				interface I<T> {
				    T bold(T t);
				}
				
				class Main { \s
				    public String foo(String x) { return "<b>" + x + "</b>"; }
				    String bar() {
				        I<? extends String> i = this::foo;
				        return i.bold("1");
				    } \s
				}
				"""
		},
		"""
			----------
			1. ERROR in Main.java (at line 9)
				return i.bold("1");
				         ^^^^
			The method bold(capture#1-of ? extends String) in the type I<capture#1-of ? extends String> is not applicable for the arguments (String)
			----------
			""");
}

public void testBug424415() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				
				import java.util.ArrayList;
				import java.util.Collection;
				
				interface Functional<T> {
				   T apply();
				}
				
				class X {
				    void foo(Object o) { }
				
					<Q extends Collection<?>> Q goo(Functional<Q> s) {
						return null;
					}\s
				
				    void test() {
				        foo(goo(ArrayList<String>::new));
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424415#c6
public void testBug424415b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				
				interface Functional<T> {
				   T apply();
				}
				
				class X {
				    void foo(Object o) { }
				    void foo(String str) {}\s
				
				    <Q extends Collection<?>> Q goo(Functional<Q> s) {
				        return null;
				    }\s
				
				    void test() {
				        foo(goo(ArrayList<String>::new));
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424415#c8
public void testBug424415c() {
	runConformTest(
		new String[] {
			"com/example/MyEmployee.java",
			"""
				package com.example;
				class MyEmployee {
				\t
					public enum Gender { MALE, FEMALE, OTHERS }
				
					private int age = 0;
					private Gender gender = Gender.MALE;
				\t
					public MyEmployee(int age, Gender gender) {
						this.age = age;
						this.gender = gender;
					}\t
				\t
					public int getAge() {
						return age;
					}
				\t
					public Gender getGender() {
						return gender;
					}
				}""",
			"com/example/Test.java",
			"""
				package com.example;
				
				import java.util.List;
				import java.util.concurrent.ConcurrentMap;
				import java.util.stream.Collectors;
				
				public class Test {
				
					ConcurrentMap<MyEmployee.Gender, List<MyEmployee>> test(List<MyEmployee> el) {
						return el.parallelStream()
									.collect(
										Collectors.groupingByConcurrent(MyEmployee::getGender)
										);
					}
				\t
				}"""
		});
}
public void testBug424631() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				
				interface Functional<T> {
				   T apply();
				}
				
				class X {
				    void foo(Collection<String> o) { }
				
					<Q extends Collection<?>> Q goo(Functional<Q> s) {
						return null;
					}\s
				
				    void test() {\s
				        foo(goo(ArrayList<String>::new));
				    }
				}
				"""
		});
}

public void testBug424403() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface Functional { int foo(); }
				
				public class X {
				    static int bar() {
				        return -1;
				    }
				    static <T> T consume(T t) { return null; }
				
				    public static void main(String[] args) {
				    	Functional f = consume(X::bar);
				    } \s
				}
				"""
		});
}
public void testBug401850a() {
	runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		null : JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				public class X<T> {
				   X(T t) {}
				   X(String s) {}
				   int m(X<String> xs) { return 0; }
				   int i = m(new X<>(""));
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 1)
				import java.util.List;
				       ^^^^^^^^^^^^^^
			The import java.util.List is never used
			----------
			2. WARNING in X.java (at line 2)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			""");
}
public void testBug401850b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				public class X<T> {
				   X(T t) {}
				   X(String s) {}
				   int m(X<String> xs) { return 0; }
				   int i = m(new X<String>(""));
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				int i = m(new X<String>(""));
				          ^^^^^^^^^^^^^^^^^
			The constructor X<String>(String) is ambiguous
			----------
			""");
}

public void testBug424710() {
	runConformTest(
		new String[] {
			"MapperTest.java",
			"""
				import java.util.ArrayList;
				import java.util.Arrays;
				import java.util.List;
				import java.util.function.Function;
				import java.util.regex.Matcher;
				import java.util.regex.Pattern;
				import java.util.stream.Stream;
				
				public class MapperTest {
				
				    public static void main( String... argv ){
				        List<String> data = Arrays.asList("abc", "123", "1a", "?!?");
				        List<Pattern> patterns = Arrays.asList(Pattern.compile("[a-z]+"), Pattern.compile("[0-9]+"));
						patterns.stream()
								.flatMap(
										p -> {
											Stream<Matcher> map = data.stream().map(p::matcher);
											Stream<Matcher> filter = map.filter(Matcher::find);
											Function<? super Matcher, ? extends Object> mapper = Matcher::group;
											mapper = matcher -> matcher.group();
											return filter.map(mapper);
										})
								.forEach(System.out::println);
				    }
				}
				"""
		},
		"""
			abc
			a
			123
			1"""
		);
}

public void testBug424075() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import java.util.function.*;
				public class X {
				    public static void main(String[] args) {
				        Consumer<Object> c = null;
				        Arrays.asList(pred(), c);
				    }
				
				    static <T> Predicate<T> pred() {
				        return null;
				    }
				}
				"""
		});
}
public void testBug424205a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void bar(String t);
				}
				class X<T> implements I {
					public void bar(String t) {}
					X(String x) {}
					X(T x) {}
					public void one(X<I> c){}
					public void two() {
						X<I> i = new X<>((String s) -> { });
						one (i);
					}
				}
				"""
		});
}
public void testBug424205b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void bar(String t);
				}
				public class X<T> implements I {
					public void bar(String t) {}
					X(String x) {}
					X(T x) {}
					public void one(X<I> c){}
					public void two() {
						one(new X<>((String s) -> { })); // 1. Three errors
						X<I> i = new X<>((String s) -> { }); // 2. Error - Comment out the previous line to see this error go away.
						one (i);
					}
					public static void main(String[] args) {
						System.out.println("main");
						new X<Integer>("one").two();
					}
				}
				"""
		},
		"main");
}
public void testBug424712a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				import java.util.function.Supplier;
				import java.util.Set;
				
				public class X {
				    public static <T, SOURCE extends Collection<T>, DEST extends Collection<T>>
				        DEST foo(SOURCE sourceCollection, DEST collectionFactory) {
				            return null;
				    } \s
				   \s
				    public static void main(String... args) {
				        Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);
				    ^
			Y cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 12)
				Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);
				                        ^
			Y cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 12)
				Set<Y> rosterSet = (Set<Y>) foo(null, Set::new);
				                                      ^^^
			Cannot instantiate the type Set
			----------
			""");
}
public void testBug424712b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Comparator;
				public class X {
					<T> void test() {
						Comparator<? super T> comparator = (Comparator<? super T>) Comparator.naturalOrder();
						System.out.println("OK");
					}
					public static void main(String[] args) {
						new X().test();
					}
				}
				"""
		},
		"OK");
}
public void testBug425142_minimal() {
	runNegativeTest(
		new String[] {
			"SomethingBreaks.java",
			"""
				import java.io.IOException;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				import java.util.function.Consumer;
				
				@FunctionalInterface interface Use<T, E extends Throwable> {   void accept(T t) throws E; }
				
				@SuppressWarnings("unused") public class SomethingBreaks<T, E extends Throwable> {
				  protected static SomethingBreaks<String, IOException> stream() {     return null;  }
				
				  public void forEach(Consumer<T> use) throws E {}
				
				  public <E2 extends E> void forEach(Use<T, E2> use) throws E, E2 {}
				
				  private static void methodReference(String s) throws IOException {
				    System.out.println(Files.size(Paths.get(s)));
				  }
				 \s
				  public static void useCase9() throws IOException {
				    stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in SomethingBreaks.java (at line 20)
				stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				         ^^^^^^^
			The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>
			----------
			""");
}
public void testBug425142_full() {
	runNegativeTest(
		new String[] {
			"SomethingBreaks.java",
			"""
				import java.io.IOException;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				import java.util.function.Consumer;
				
				@FunctionalInterface interface Use<T, E extends Throwable> {   void accept(T t) throws E; }
				
				@SuppressWarnings("unused") public class SomethingBreaks<T, E extends Throwable> {
				  protected static SomethingBreaks<String, IOException> stream() {     return null;  }
				
				  public void forEach(Consumer<T> use) throws E {}
				
				  public <E2 extends E> void forEach(Use<T, E2> use) throws E, E2 {}
				
				  private static void methodReference(String s) throws IOException {
				    System.out.println(Files.size(Paths.get(s)));
				  }
				 \s
				  public static void useCase1() throws IOException {
				    Use<String, IOException> c =
				      (String s) -> System.out.println(Files.size(Paths.get(s)));
				    stream().forEach(c);
				  }
				 \s
				  public static void useCase2() throws IOException {
				    Use<String, IOException> c = SomethingBreaks::methodReference;
				    stream().forEach(c);
				  }
				 \s
				  public static void useCase3() throws IOException {
				    stream().forEach((Use<String, IOException>) (String s) -> System.out.println(Files.size(Paths.get(s))));
				  }
				 \s
				  public static void useCase4() throws IOException {
				    stream().forEach((Use<String, IOException>) SomethingBreaks::methodReference);
				  }
				 \s
				  public static void useCase5() throws IOException {
				    stream().<IOException> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				  }
				 \s
				  public static void useCase6() throws IOException {
				    stream().<IOException> forEach(SomethingBreaks::methodReference);
				  }
				 \s
				  public static void useCase7() throws IOException {
				    stream().<Use<String, IOException>> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				  }
				 \s
				  public static void useCase8() throws IOException {
				    stream().<Use<String, IOException>> forEach(SomethingBreaks::methodReference);
				  }
				 \s
				  public static void useCase9() throws IOException {
				    stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				  }
				 \s
				  public static void useCase10() throws IOException {
				    stream().forEach(SomethingBreaks::methodReference);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in SomethingBreaks.java (at line 39)
				stream().<IOException> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				                       ^^^^^^^
			The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>
			----------
			2. ERROR in SomethingBreaks.java (at line 43)
				stream().<IOException> forEach(SomethingBreaks::methodReference);
				                       ^^^^^^^
			The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>
			----------
			3. ERROR in SomethingBreaks.java (at line 43)
				stream().<IOException> forEach(SomethingBreaks::methodReference);
				                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			4. ERROR in SomethingBreaks.java (at line 47)
				stream().<Use<String, IOException>> forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				                                                                             ^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			5. ERROR in SomethingBreaks.java (at line 51)
				stream().<Use<String, IOException>> forEach(SomethingBreaks::methodReference);
				                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			6. ERROR in SomethingBreaks.java (at line 55)
				stream().forEach((String s) -> System.out.println(Files.size(Paths.get(s))));
				         ^^^^^^^
			The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>
			----------
			7. ERROR in SomethingBreaks.java (at line 59)
				stream().forEach(SomethingBreaks::methodReference);
				         ^^^^^^^
			The method forEach(Consumer<String>) is ambiguous for the type SomethingBreaks<String,IOException>
			----------
			8. ERROR in SomethingBreaks.java (at line 59)
				stream().forEach(SomethingBreaks::methodReference);
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			""");
}
public void testBug424195a() {
	runNegativeTestMultiResult(
		new String[] {
			"NPEOnCollector.java",
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.function.Function;
				import java.util.function.Predicate;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				
				
				public class NPEOnCollector {
				  static void processJar(Path plugin) throws IOException {
				   \s
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Stream<JarEntry> stream = entries
				          .distinct().collect(Collectors.toCollection(ArrayList::new));
				       \s
				      }
				    }
				  }
				}
				"""
		},
		null,
		new String[] {
			"""
				----------
				1. ERROR in NPEOnCollector.java (at line 17)
					Stream<JarEntry> stream = entries
				          .distinct().collect(Collectors.toCollection(ArrayList::new));
					                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from Collection<JarEntry> to Stream<JarEntry>
				----------
				""",
			"""
				----------
				1. ERROR in NPEOnCollector.java (at line 18)
					.distinct().collect(Collectors.toCollection(ArrayList::new));
					                                            ^^^^^^^^^^^^^^
				The constructed object of type ArrayList is incompatible with the descriptor's return type: Stream<JarEntry>&Collection<T#2>&Collection<JarEntry>
				----------
				"""
		});
}
public void testBug424195b() {
	runConformTest(
		new String[] {
			"NPEOnCollector.java",
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.function.Function;
				import java.util.function.Predicate;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				
				
				public class NPEOnCollector {
				  static void processJar(Path plugin) throws IOException {
				   \s
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Collection<JarEntry> collection = entries
				          .distinct().collect(Collectors.toCollection(ArrayList::new));
				       \s
				      }
				    }
				  }
				}
				"""
		});
}
public void testBug424195_comment2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.PrintStream;
				import java.util.ArrayList;
				import java.util.stream.Collectors;
				import java.util.stream.*;
				public class X  {
				
				    public static void main(String argv[]) {
				        ArrayList<Integer> al = IntStream
				        	     .range(0, 10_000)
				        	     .boxed()
				        	     .collect(Collectors.toCollection(ArrayList::new));
				
				    }
				}
				"""
		});
}
public void testBug425153() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"""
				class C1 {}
				class C2 {}
				
				interface I<P1 extends C1, P2 extends P1> {
				    P2 foo(P1 p1);
				}
				
				public class Main  {
					    public static void main(String argv[]) {
					    	I<?, ?> i = (C1 c1) -> { return new C2(); };
					        Object c2 = i.foo(null);
					    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Main.java (at line 10)
				I<?, ?> i = (C1 c1) -> { return new C2(); };
				            ^^^^^^^^^^
			The target type of this expression is not a well formed parameterized type due to bound(s) mismatch
			----------
			""");
}
public void testBug424845() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				
				public class Test {
				   \s
				
				    interface Function<K, V>{
				        public V apply(K orig);
				    }
				   \s
				   \s
				    static class Ordering<O> {
				
				        public <K> Comparator<K> onResultOf(Function<K, ? extends O> function) {
				            return null;
				        }
				
				       \s
				    }
				   \s
				    public static void main(String[] args) {
				        List<Object> list = new ArrayList<>();
				        Function<Object, String> function = new Function<Object, String>() {
				            public String apply(Object arg0) {
				                return arg0.toString();
				            }
				        };
				        Ordering<Comparable<String>> natural = new Ordering<>();
				        Collections.sort(list, natural.onResultOf(function));
				    }
				   \s
				}
				"""
		});
}
public void testBug425278() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T, S extends X<T>> {\s
				    T foo(S p);
				}
				
				public class X<T>  {
				    public void bar() {
				    I<Object, X<Object>> f = (p) -> p; // Error
				    }
				}
				"""
		});
}
public void testBug425783() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				class MyType<S extends MyType<S>> {
					S myself() { return (S)this; }
				}
				public class Test {
					MyType test() {
						return newInstance().myself();
					}
					MyType test2() {
						return newInstance().myself();
					}
					public <T extends MyType> T newInstance() {
						return (T) new MyType();
					}\
				}
				"""
		});
}
public void testBug425798() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.*;
				import java.util.*;
				import java.util.function.*;
				import java.util.stream.*;
				interface MyCollector<T, A, R> extends Collector<T, A, R> {
				}
				public abstract class X {
					abstract <T, K, U, M extends Map<K, U>>
				    MyCollector<T, ?, M> toMap(Function<? super T, ? extends K> km,
				                                BinaryOperator<U> mf);\
					void test(Stream<Annotation> annotations) {
						annotations
							.collect(toMap(Annotation::annotationType,
								 (first, second) -> first));
					}
				}
				"""
		},
		"");
}
public void testBug425798a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.annotation.*;
				import java.util.*;
				import java.util.function.*;
				import java.util.stream.*;
				interface MyCollector<T, A, R> extends Collector<T, A, R> {
				}
				public abstract class X {
					abstract <T, K, U, M extends Map<K, U>>
				    MyCollector<T, ?, M> toMap(Function<? super T, ? extends K> km,
				                                BinaryOperator<U> mf);\
					void test(Stream<Annotation> annotations) {
						annotations
							.collect(toMap(true ? Annotation::annotationType : Annotation::annotationType,
								 (first, second) -> first));
					}
				}
				"""
		},
		"");
}
// witness for NPE mentioned in https://bugs.eclipse.org/bugs/show_bug.cgi?id=425798#c2
public void testBug425798b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Objects;
				import java.util.PrimitiveIterator;
				import java.util.Spliterator;
				import java.util.Spliterator.OfInt;
				import java.util.function.Consumer;
				import java.util.function.IntConsumer;
				
				class IntIteratorSpliterator implements OfInt {
					public IntIteratorSpliterator(PrimitiveIterator.OfInt arg) { }
					public void forEachRemaining(IntConsumer action) { }
					public boolean tryAdvance(Consumer<? super Integer> action) { return false; }
					public long estimateSize() { return 0; }
					public int characteristics() { return 0; }
					public OfInt trySplit() { return null; }
					public boolean tryAdvance(IntConsumer action) { return false; }
				}
				public class X {
				
					public Spliterator.OfInt spliterator(PrimitiveIterator.OfInt iterator) {
						return new IntIteratorSpliterator(id(iterator));
					}
					<T> T id(T e) { return e; }
				}
				"""
		});
}
public void testBug425460orig() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				public class X {
					final Integer[] boom =
				  		Arrays.asList("1", "22", "333")
				  			.stream()
				  			.map(str -> str.length())
				  			.toArray(i -> new Integer[i]);
				}
				"""
		});
}
public void testBug425460variant() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				public class X {
					final Integer[] boom =
				  		Arrays.asList("1", "22", "333")
				  			.stream()
				  			.map(str -> str.length())
				  			.toArray((int i) -> new Integer[i]);
				}
				"""
		});
}
public void testBug425951() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.List;
				
				public class Test {
				
				    public static void main(String[] args) {
				        index(new A().test());
				    }
				
				    public static <X> void index(Iterable<X> collection)
				    {
				    }
				\t
				    public class A<S extends A<S>>
				    {
				        protected A() {}
					\t
				        public <T> List<T> test()
				       {
				            return null;
				       }
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in Test.java (at line 6)
				index(new A().test());
				^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation index(List) of the generic method index(Iterable<X>) of type Test
			----------
			2. WARNING in Test.java (at line 6)
				index(new A().test());
				      ^^^^^^^^^^^^^^
			Type safety: The expression of type List needs unchecked conversion to conform to Iterable<Object>
			----------
			3. ERROR in Test.java (at line 6)
				index(new A().test());
				      ^^^^^^^
			No enclosing instance of type Test is accessible. Must qualify the allocation with an enclosing instance of type Test (e.g. x.new A() where x is an instance of Test).
			----------
			4. WARNING in Test.java (at line 6)
				index(new A().test());
				          ^
			Test.A is a raw type. References to generic type Test.A<S> should be parameterized
			----------
			""");
}
public void testBug425951a() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.List;
				
				public class Test {
				
				    public void test() {
				        index(new A().test());
				    }
				
				    public static <X> void index(Iterable<X> collection)
				    {
				    }
				\t
				    public class A<S extends A<S>>
				    {
				        protected A() {}
					\t
				        public <T> List<T> test()
				       {
				            return null;
				       }
				    }
				}
				"""
		});
}
public void testBug424906() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				public class Main {
					public <T> void test(Result r) {}
				
					public static void main(String[] args) {
						new Main().test(r -> System.out.println("Hmmm..." + r));
					}
				}
				
				interface Result {
					public void result(Object object);
				}"""
		});
}
public void testBug425156() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T> {
				    void foo(T t);
				}
				public class X {
				    void bar(I<?> i) {
				        i.foo(null);
				    }
				    void run() {
				        bar((X x) -> {}); // Incompatible error reported
				    }
				}
				"""
		});
}
public void testBug425493() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    public void addAttributeBogus(Attribute<?> attribute) {
				        addAttribute(java.util.Objects.requireNonNull(attribute, ""),
				                attribute.getDefault());
				        addAttribute(attribute, attribute.getDefault());
				    }
				    public <T> void addAttributeOK(Attribute<T> attribute) {
				        addAttribute(java.util.Objects.requireNonNull(attribute, ""),
				                attribute.getDefault());
				        addAttribute(attribute, attribute.getDefault());
				    }
				
				    private <T> void addAttribute(Attribute<T> attribute, T defaultValue) {}
				
				    static class Attribute<T> {
				
				        T getDefault() {
				            return null;
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 3)
				addAttribute(java.util.Objects.requireNonNull(attribute, ""),
				^^^^^^^^^^^^
			The method addAttribute(Test.Attribute<T>, T) in the type Test is not applicable for the arguments (Test.Attribute<capture#1-of ?>, capture#2-of ?)
			----------
			2. ERROR in Test.java (at line 5)
				addAttribute(attribute, attribute.getDefault());
				^^^^^^^^^^^^
			The method addAttribute(Test.Attribute<T>, T) in the type Test is not applicable for the arguments (Test.Attribute<capture#3-of ?>, capture#4-of ?)
			----------
			""");
}
public void testBug426366() {
	runConformTest(
		new String[] {
			"a/Test.java",
			"""
				package a;
				
				import java.util.Collections;
				import java.util.List;
				
				/**
				 * @author tomschindl
				 *
				 */
				public class Test {
					public static class A {
						public A(B newSelectedObject, String editorController) {
					    }
				
					    public A(List<B> newSelectedObjects, String editorController) {
					    }
					}
				\t
					public static class B {
					\t
					}
				\t
					public static class C extends A {
						public C() {
							super(Collections.emptyList(), "");
						}
					}
				}
				"""
		});
}
public void testBug426290() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class X {
				    public static void main(String argv[]) {
				       goo(foo());
				    }
				
				    static <T extends Number> List<T> foo() {
				        return new ArrayList<T>();
				    }
				
				    static void goo(Object p1) {
				        System.out.println("goo(Object)");
				    }
				
				    static void goo(List<Integer> p1) {
				        System.out.println("goo(List<Integer>)");
				    }
				}
				"""
		},
		"goo(List<Integer>)");
}
public void testBug425152() {
	runConformTest(
		new String[] {
			"packDown/SorterNew.java",
			"""
				package packDown;
				
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				
				public class SorterNew {
					void sort() {
						Collections.sort(new ArrayList<Person>(),
								Comparator.comparing((Person p) -> p.getName()));
					}
				}
				
				class Person {
					public String getName() {
						return "p";
					}
				}
				"""
		});
}
public void testBug426048() {
	runNegativeTest(
		new String[] {
			"MyFunction.java",
			"""
				import java.lang.annotation.Annotation;
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				
				@Target(ElementType.TYPE_USE)
				@interface Throws {
				  Class<? extends Throwable>[] value() default Throwable.class;
				  Returns method() default @Returns(Annotation.class);
				}
				
				@Target(ElementType.TYPE_USE)
				@interface Returns {
				  Class<? extends Annotation> value() default Annotation.class;
				}
				
				@FunctionalInterface public interface MyFunction<T, @Returns R> {
				  @Returns  R apply(T t);
				
				  default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>
				    compose(MyFunction<? super V, ? extends T> before) {
				
				    return (V v) -> apply(before.apply(v));
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in MyFunction.java (at line 19)
				default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>
				          ^
			Syntax error, insert "Type Identifier (" to complete MethodHeaderName
			----------
			2. ERROR in MyFunction.java (at line 19)
				default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>
				                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from MyFunction<capture#1-of ? super V,capture#2-of ? extends T> to Class<? extends Throwable>[]
			----------
			3. ERROR in MyFunction.java (at line 19)
				default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>
				                                                          ^^^^^^
			before cannot be resolved
			----------
			4. ERROR in MyFunction.java (at line 19)
				default <V> @Throws(((MyFunction<? super V, ? extends T>) before::apply) @Returns MyFunction<V, @Returns R>
				                                                                       ^
			Syntax error, insert ")" to complete Modifiers
			----------
			5. ERROR in MyFunction.java (at line 20)
				compose(MyFunction<? super V, ? extends T> before) {
				       ^
			Syntax error on token "(", , expected
			----------
			""");
}
public void testBug426540() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.stream.Stream;
				import java.util.Collections;
				import static java.util.stream.Collectors.collectingAndThen;
				import static java.util.stream.Collectors.toList;
				public class X {
					Object o = ((Stream<Integer>) null).collect(collectingAndThen(toList(), Collections::unmodifiableList));
				}
				"""
		});
}
public void testBug426671_ok() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.stream.Stream;
				import java.util.*;
				import static java.util.stream.Collectors.collectingAndThen;
				import static java.util.stream.Collectors.toList;
				public class X {
					void test(Stream<List<Integer>> stream) {
						stream.collect(collectingAndThen(toList(), Collections::<List<Integer>>unmodifiableList))
							.remove(0);
					}
				}
				"""
		});
}
public void testBug426671_medium() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.stream.Stream;
				import java.util.*;
				import static java.util.stream.Collectors.collectingAndThen;
				import static java.util.stream.Collectors.toList;
				public class X {
					void test(Stream<List<Integer>> stream) {
						stream.collect(collectingAndThen(toList(), Collections::unmodifiableList))
							.remove(0);
					}
				}
				"""
		});
}
public void testBug426671_full() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.stream.Stream;
				import java.util.*;
				import static java.util.stream.Collectors.collectingAndThen;
				import static java.util.stream.Collectors.toList;
				public class X {
					void test() {
						Arrays.asList((List<Integer>) null).stream().collect(collectingAndThen(toList(), Collections::unmodifiableList))
							.remove(0);
					}
				}
				"""
		});
}
public void testBug426671b() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				interface I<X,Y> {
					Y fun(X y);
				}
				public class Test {
					static <S> S id(S s) { return s; }
					void test() {
				        m1(Test::id, "Hi");
				        m2(Test::id, "Hi").toUpperCase();
				        m3(Test::id, "Hi").toUpperCase();
				   }
				
					<U,V> void m1(I<V,U> i, U u) { }
					<U,V> V m2(I<V,U> i, U u) {
						return null;
					}
					<U,V> V m3(I<U,V> i, U u) {
						return null;
					}
				}"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 8)
				m2(Test::id, "Hi").toUpperCase();
				                   ^^^^^^^^^^^
			The method toUpperCase() is undefined for the type Object
			----------
			""");
}
public void testBug426652() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import static java.util.stream.Collectors.toList;
				public class X {
					Object o = toList();
				}
				"""
		});
}
public void testBug426778() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
					void test(List<CourseProviderEmploymentStatistics> result) {
				          Collections.sort( result,\s
				              Comparator.comparingInt(
				                  (CourseProviderEmploymentStatistics stat) ->  stat.doneTrainingsTotal
								)
				              .reversed()
				              .thenComparing(
				                  (CourseProviderEmploymentStatistics stat) -> stat.courseProviderName ) );
					}
				}
				class CourseProviderEmploymentStatistics {
				   int doneTrainingsTotal;
				   String courseProviderName;
				}
				"""
		});
}
public void testBug426676() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.Arrays;
				import java.util.function.Supplier;
				import java.util.stream.Stream;
				
				
				public class Test {
				    public static void main(String[] args) throws Exception {
				        // Type inference works on map call.
				        Stream<String> s1 =
				        Arrays.stream(new Integer[] { 1, 2 })
				              .map(i -> i.toString());
				       \s
				        // Type inference doesn't work on map call.
				        Stream<String> s2 =
				        Arrays.stream(new Integer[] { 1, 2 })
				              .map(i -> i.toString())
				              .distinct();
				    }
				}
				"""
		});
}
public void testBug424591_comment20() {
	runConformTest(
		new String[] {
			"MyList.java",
			"""
				import java.util.Arrays;
				public class MyList {
				    protected Object[] elements;
				    private int size;
				    @SuppressWarnings("unchecked")
				    public <A> A[] toArray(A[] a) {
				        return (A[]) Arrays.copyOf(elements, size, a.getClass());
				    }
				}
				"""
		});
}
public void testBug424591_comment20_variant() {
	runNegativeTest(
		new String[] {
			"MyList.java",
			"""
				import java.util.Arrays;
				public class MyList {
				    protected Object[] elements;
				    private int size;
				    @SuppressWarnings("unchecked")
				    public <A> A[] toArray(A[] a) {
				        return (A[]) Arrays.copyOf(elements, size, getClass());
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in MyList.java (at line 7)
				return (A[]) Arrays.copyOf(elements, size, getClass());
				                    ^^^^^^
			The method copyOf(U[], int, Class<? extends T[]>) in the type Arrays is not applicable for the arguments (Object[], int, Class<capture#1-of ? extends MyList>)
			----------
			""");
}
public void testBug424591_comment22() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				public class Test {
					public static void main(String[] args) {
				        Test.forObject(new HashSet<>());
					}
				    public static Test forObject(Object o) {
				        return null;
				    }
				}
				"""
		});
}
public void testBug425063() {
    runConformTest(
        new String[] {
            "ComparatorUse.java",
            """
				import java.util.Comparator;
				public class ComparatorUse {
				   Comparator<String> c =
				           Comparator.comparing((String s)->s.toString())
				           .thenComparing(s -> s.length());
				}
				"""
        });
}
public void testBug426764() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {}
				class C1 implements I {}
				class C2 implements I {}
				public class X  {
				    <T > void foo(T p1, I p2) {}
				    <T extends I> void foo(T p1, I p2) {}
				    void bar() {
				        foo(true ? new C1(): new C2(), false ? new C2(): new C1());
				        foo(new C1(), false ? new C2(): new C1());
				    }
				}
				"""
		});
}
// simplest: avoid any grief concerning dequeCapacity:
public void testBug424930a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayDeque;
				import java.util.Deque;
				import java.util.function.Supplier;
				
				public class X<S, T extends Deque<S>> {
				    private final Supplier<T> supplier;
				
				    public X(Supplier<T> supplier) {
				        this.supplier = supplier;
				    }
				   \s
					 int dequeCapacity;
				    public static <S> X<S, Deque<S>> newDefaultMap() {
				        return new X<>(() -> new ArrayDeque<>(13));
				    }
				}
				"""
		});
}
// original test:
public void testBug424930b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayDeque;
				import java.util.Deque;
				import java.util.function.Supplier;
				
				public class X<S, T extends Deque<S>> {
				    private final Supplier<T> supplier;
				
				    public X(Supplier<T> supplier) {
				        this.supplier = supplier;
				    }
				   \s
				    public static <S> X<S, Deque<S>> newDefaultMap(int dequeCapacity) {
				        return new X<>(() -> new ArrayDeque<>(dequeCapacity));
				    }
				}
				"""
		});
}
// witness for an NPE during experiments
public void testBug424930c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayDeque;
				import java.util.Deque;
				import java.util.function.Supplier;
				
				public class X<S, T extends Deque<S>> {
				    private final Supplier<T> supplier;
				
				    public X(Supplier<T> supplier) {
				        this.supplier = supplier;
				    }
				   \s
					 int dequeCapacity;
				    public static <S> X<S, Deque<S>> newDefaultMap() {
				        return new X<>(() -> new ArrayDeque<>(dequeCapacity));
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				return new X<>(() -> new ArrayDeque<>(dequeCapacity));
				                                      ^^^^^^^^^^^^^
			Cannot make a static reference to the non-static field dequeCapacity
			----------
			""");
}
public void testBug426998a() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"""
				public class Snippet {
					static void call(Class type, long init) {
						String string = new String();
						method(type, init == 0 ? new String() : string);
					}
					private static void method(Class type, String s) {}
				}
				"""
		});
}
// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=426764#c5
public void testBug426998b() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"""
				public class Snippet {
				  private static final String PLACEHOLDER_MEMORY = new String();
				
				  static void newInstance(Class type, long init) {
				    method(type, init == 0 ? new String() : PLACEHOLDER_MEMORY);
				  }
				
				  private static void method(Class type, String str) {}
				}
				"""
		});
}
public void testBug427164() {
	runNegativeTest(
		new String[] {
			"NNLambda.java",
			"""
				import java.util.*;
				
				@FunctionalInterface
				interface FInter {
					String allToString(List<String> input);
				}
				
				public abstract class NNLambda {
					abstract <INP> void printem(FInter conv, INP single);
				\t
					void test() {
						printem((i) -> {
								Collections.<String>singletonList("const")
							},\s
							"single");
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in NNLambda.java (at line 12)
				printem((i) -> {
				^^^^^^^
			The method printem(FInter, INP) in the type NNLambda is not applicable for the arguments ((<no type> i) -> {}, String)
			----------
			2. ERROR in NNLambda.java (at line 13)
				Collections.<String>singletonList("const")
				                                         ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			""",
		true); // statement recovery
}
public void testBug427168() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Producer<T> {
					<P> P produce();
				}
				public class X {
					<T> void perform(Producer<T> r) { }
					void test() {
						perform(() -> 13);\s
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				perform(() -> 13);\s
				^^^^^^^
			The method perform(Producer<T>) in the type X is not applicable for the arguments (() -> {})
			----------
			2. ERROR in X.java (at line 7)
				perform(() -> 13);\s
				        ^^^^^^^^
			Illegal lambda expression: Method produce of type Producer<T> is generic\s
			----------
			""");
}
public void testBug427196() {
	runConformTest(
		new String[] {
			"MainTest.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Function;
				
				public class MainTest {
				    public static <T> List<T> copyOf (Collection<T> c) {
				        return new ArrayList<>(c);
				    }
				   \s
				    public static <T> List<T> copyOf (Iterable<T> c) {
				        return new ArrayList<>();
				    }
				   \s
				    public static void main (String[] args) {
				        Function<Collection<String>, List<String>> function1 = c -> MainTest.copyOf(c); //OK
				        Function<Collection<String>, List<String>> function2 = MainTest::copyOf;        //error
				    }
				}
				"""
		});
}
public void testBug427224() {
	runConformTest(
		new String[] {
			"Test2.java",
			"""
				import java.util.*;
				public class Test2 {
				    public static native <T> T applyToSet(java.util.Set<String> s);
				
				    public static void applyToList(java.util.List<String> s) {
				        applyToSet(new java.util.HashSet<>(s));
				    }
				}
				"""
		});
}
// comment 12
public void testBug424637() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					String foo(X x, String s2);
				}
				
				public class X {
					String goo(String ...ts) {
						System.out.println(ts[0]); \s
						return ts[0];
					}
					public static void main(String[] args) {
						I i = X::goo;
						String s = i.foo(new X(), "world");
						System.out.println(s);    \s
					}
				}
				"""
		},
		"world\n" +
		"world",
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=427218, [1.8][compiler] Verify error varargs + inference
public void test427218_reduced() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   public static void main(String[] args) {
				      match(getLast("a"), null);
				   }
				   public static <T> T getLast(T... array) { return null; } // same with T[]
				   public static void match(boolean b, Object foo) { }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				match(getLast("a"), null);
				^^^^^
			The method match(boolean, Object) in the type X is not applicable for the arguments (String, null)
			----------
			2. ERROR in X.java (at line 3)
				match(getLast("a"), null);
				      ^^^^^^^^^^^^
			Type mismatch: cannot convert from String to boolean
			----------
			3. WARNING in X.java (at line 5)
				public static <T> T getLast(T... array) { return null; } // same with T[]
				                                 ^^^^^
			Type safety: Potential heap pollution via varargs parameter array
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=427218, [1.8][compiler] Verify error varargs + inference
public void test427218() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   public static void main(String[] args) {
				      match(getLast("a"), null);
				   }
				   public static <T> T getLast(T... array) { return null; } // same with T[]
				   public static void match(boolean b, Object foo) { }
				   public static <A> void match(Object o, A foo) { }
				}
				""",
		},
		"");
}
public void testBug427223() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				public class Test {
				
					List<Object> toList(Object o) {
						if (o instanceof Optional) {
							return Arrays.asList(((Optional<?>) o).orElse(null));
						} else {
							return null;
						}
					}
				
				}
				"""
		});
}
public void testBug425183_comment8() {
	// similar to what triggered the NPE, but it never did trigger
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String... args) {
				        java.util.Comparator.reverseOrder().thenComparingLong(X::toLong);
				        System.out.println("ok");
				    }
				    static <T> long toLong(T in) { return 0L; }
				}
				"""
		},
		"ok");
}
public void testBug427483() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
					void test() {
						new TreeSet<>((String qn1, String qn2) -> {
				   		boolean b = true;
							System.out.println(b); // ok
				   		if(b) { }
					   		return qn1.compareTo(qn2);
						});
					}
				}
				"""
		});
}
public void testBug427504() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				
					public static <T> Tree<T> model(T o) {
						return Node(Leaf(o), Leaf(o));
					}
				\t
					interface Tree<T> {}
					static <T> Tree<T> Node(Tree<T>... children) { return null; }
					static <T> Tree<T> Leaf(T o) { return null; }
				\t
				}
				"""
		});
}
public void testBug427479() {
	runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.util.*;
				import java.util.function.BinaryOperator;\s
				import java.util.stream.*;
				
				public class Bug {
				\s
					static List<String> names = Arrays.asList(
							"ddd",
							"s",
							"sdfs",
							"sfdf d");\s
				\s
					public static void main(String[] args) {
							 BinaryOperator<List<String>> merge = (List<String> first, List<String> second) -> {
								 first.addAll(second);
								 return first;
								 };
								\s
							Collector<String,?,Map<Integer,List<String>>> collector= Collectors.toMap(
									s -> s.length(),\s
									Arrays::asList,
									merge);\s
							Map<Integer, List<String>> lengthToStrings = names.stream().collect(collector);
						\t
							lengthToStrings.forEach((Integer i, List<String> l)-> {
								System.out.println(i + " : " + Arrays.deepToString(l.toArray()));
							});
				
					}
				
				}
				"""
		});
}
public void testBug427479b() {
	runNegativeTest(
		new String[] {
			"Bug419048.java",
			"""
				import java.util.List;
				import java.util.Map;
				import java.util.stream.Collectors;
				
				
				public class Bug419048 {
					void test1(List<Object> roster) {
				        Map<String, Object> map =\s
				                roster
				                    .stream()
				                    .collect(
				                        Collectors.toMap(
				                            p -> p.getLast(),
				                            p -> p.getLast()
				                        ));
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Bug419048.java (at line 9)
				roster
			                    .stream()
			                    .collect(
			                        Collectors.toMap(
			                            p -> p.getLast(),
			                            p -> p.getLast()
			                        ));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Map<Object,Object> to Map<String,Object>
			----------
			2. ERROR in Bug419048.java (at line 13)
				p -> p.getLast(),
				       ^^^^^^^
			The method getLast() is undefined for the type Object
			----------
			3. ERROR in Bug419048.java (at line 14)
				p -> p.getLast()
				       ^^^^^^^
			The method getLast() is undefined for the type Object
			----------
			""");
}
public void testBug427626() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				
				public class X {
					void m() {
				        List<String> ss = Arrays.asList("1", "2", "3");
				       \s
				        ss.stream().map(s -> {
				          class L1 {};
				          class L2 {
				            void mm(L1 l) {}
				          }
				          return new L2().mm(new L1());
				        }).forEach(e -> System.out.println(e));
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				ss.stream().map(s -> {
			          class L1 {};
			          class L2 {
			            void mm(L1 l) {}
			          }
			          return new L2().mm(new L1());
			        }).forEach(e -> System.out.println(e));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type argument(s) for <R> map(Function<? super T,? extends R>)
			----------
			2. ERROR in X.java (at line 13)
				return new L2().mm(new L1());
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot return a void result
			----------
			""");
}
public void testBug426542() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X {
				\t
					<T extends Comparable & Serializable> void foo(T o1) {
					}
				
					<T extends Serializable> void foo(T o1) {
					}
				
					void bar() {
						foo((Comparable & Serializable)0);
						foo(0);
					}
				}
				"""
		});
}
public void testBug426836() {
	runConformTest(
		new String[] {
			"ReferenceToGetClass.java",
			"""
				import java.util.function.Supplier;
				
				
				public class ReferenceToGetClass {
					<T> T extract(Supplier<T> s) {
						return s.get();
					}
					Class<?> test() {
						Class<? extends ReferenceToGetClass> c = extract(this::getClass);
						return c;
					}
				}
				"""
		} );
}
public void testBug428019() {
    runConformTest(
        new String[] {
            "X.java",
            """
				final public class X {
				  static class Obj {}
				  static class Dial<T> {}
				
				  <T> void put(Class<T> clazz, T data) {
				  }
				
				  static <T> Dial<T> wrap(Dial<T> dl) {
					  return null;
				  }
				
				  static void foo(Dial<? super Obj> dial, X context) {
				    context.put(Dial.class, wrap(dial));
				  }
				 \s
				  public static void main(String[] args) {
					X.foo(new Dial<Obj>(), new X());
				  }
				}
				"""
        });
}
public void testBug428198() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"""
				import java.util.*;
				interface BundleRevision {}
				interface BundleDescription extends BundleRevision {}
				public class Snippet {
				  static Collection<BundleRevision> test(BundleDescription[] triggers) {
				    @SuppressWarnings("unchecked")
				    Collection<BundleRevision> triggerRevisions =
				    //Type mismatch: cannot convert from Collection<Object> to Collection<BundleRevision>
				      Collections
				        .unmodifiableCollection(triggers == null ? Collections.EMPTY_LIST
				        : Arrays.asList((BundleRevision[]) triggers));
				    return triggerRevisions;
				  }
				}
				"""
		});
}
public void testBug428198b() {
	runConformTest(
		new String[] {
			"Snippet.java",
			"""
				import java.util.*;
				interface BundleRevision {}
				interface BundleDescription extends BundleRevision {}
				public class Snippet {
				  static Collection<BundleRevision> test(BundleDescription[] triggers) {
				    @SuppressWarnings("unchecked")
				    Collection<BundleRevision> triggerRevisions =
				      Collections
				        .unmodifiableCollection(triggers == null ? Collections.emptyList()
				        : Arrays.asList((BundleRevision[]) triggers));
				    return triggerRevisions;
				  }
				}
				"""
		});
}
public void testBug428264() {
	runConformTest(
		new String[] {
			"Y.java",
			"""
				import java.util.function.*;
				import java.util.Optional;
				
				interface I<E,F> {}
				class A<G> implements I<G, Optional<G>> {}
				
				public class Y<S,T> {
				    Y(T o, Predicate<T> p, Supplier<I<S,T>> s) {}
				
				    static <Z> Y<Z, Optional<Z>> m() {
				        return new Y<>(Optional.empty(), Optional::isPresent, A::new);
				    }
				}
				"""
		});
}
public void testBug428294() {
	runConformTest(
		new String[] {
			"Junk5.java",
			"""
				import java.util.Collection;
				import java.util.List;
				import java.util.stream.Collectors;
				
				
				public class Junk5 {
				
				    class TestTouchDevice {
				        public Object [] points;
				    }
				   \s
				    public static List<TestTouchDevice> getTouchDevices() {
				        return null;
				    }
				
				    public static Collection<Object[]> getTouchDeviceParameters2(int minPoints) {
				        Collection c = getTouchDevices().stream()
				                .filter(d -> d.points.length >= minPoints)
				                .map(d -> new Object[] { d })
				                .collect(Collectors.toList());
				         return c;
				    }
				   \s
				    public static Collection<Object[]> getTouchDeviceParameters3(int minPoints) {
				        return getTouchDevices().stream()
				                .filter(d -> d.points.length >= minPoints)
				                .map(d -> new Object[] { d })
				                .collect(Collectors.toList());
				    }
				}
				"""
		});
}
public void testBug428291() {
	runConformTest(
		new String[] {
			"AC3.java",
			"""
				import java.util.List;
				
				interface I0<T> { }
				
				interface I1 { }
				interface I1List<E> extends List<E>, I1 {}
				interface I2<T> extends I1 {
					void foo(I0<? super T> arg1);
					void bar(I0<? super T> arg2);
				}
				interface I3<T> extends I2<T> {}
				interface I4<T> extends I2<T> { }
				interface I3List<E> extends I3<I1List<E>>, I1List<E> {}
				abstract class AC1<E> implements I3List<E> { }
				
				abstract class AC2<E>  {
				    public static <E> AC2<E> bork(AC2<E> f1, I3List<E> i3l, I0<? super I1List<E>> i1l) {
				        return null;
				    }
				    public static <E> AC2<E> garp(AC2<E> f2, I0<? super I1List<E>> i1l) {
				        return null;
				    }
				}
				
				public abstract class AC3<E> extends AC1<E> implements I4<I1List<E>> {
				
				    AC2<E> f = null;
				
				    @Override
				    public void foo(I0<? super I1List<E>> arg1) {
				        f = AC2.bork(f, this, arg1);
				    }
				
				    @Override
				    public void bar(I0<? super I1List<E>> arg2) {
				        f = AC2.garp(f, arg2);
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428275,  [1.8][compiler] CCE in InferenceContext18.varArgTypes
public void testBug428275() {
	runConformTest(
		new String[] {
			"p1/C1.java",
			"""
				package p1;
				
				import java.util.List;
				
				public class C1<T1> {
				
					public static class CInner<T2A,T2B> {
						public CInner(T2A a, T2B b) {}
					}
				\t
					public static class CInner2<T3A,T3B> {
						public CInner2(String n, List<CInner<T3A,T3B>> arg) {}
					}
				\t
				    public static <E> List<E> getList1(E... items) {
				    	return null;
				    }
				}
				""",
			"Test.java",
			"""
				import java.util.List;
				
				import p1.C1;
				
				public class Test {
					void test2(List<C1.CInner2> l) {
						l.add(
							new C1.CInner2<>("a",
								C1.getList1(new C1.CInner<>("b", 13))
							)
						);
					}
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428352, [1.8][compiler] NPE in AllocationExpression.analyseCode when trying to pass Consumer as Function
public void test428352() {
	runNegativeTest(
		new String[] {
			"OperationsPile.java",
			"""
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Consumer;
				import java.util.function.Function;
				
				class OperationsPile<B> {
				  OperationsPile(Function<B, ?> handler) {}
				
				  private static <T> void addAll3(Collection<T> c, T t) {}
				
				  static <S> void adaad3(List<OperationsPile<?>> combined, Consumer<S> handler) {
				    addAll3(combined, new OperationsPile<>(null));
				    addAll3(combined, new OperationsPile<>(handler));
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in OperationsPile.java (at line 13)
				addAll3(combined, new OperationsPile<>(handler));
				                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for OperationsPile<>
			----------
			""");
}
public void test428352b() {
	runConformTest(
		new String[] {
			"OperationsPile.java",
			"""
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Consumer;
				import java.util.function.Function;
				
				public class OperationsPile<B> {
				  OperationsPile(Function<B, ?> handler) {}
				
				  private static <T> void addAll3(Collection<T> c, T t) {}
				
				  static <S> void adaad3(List<OperationsPile<?>> combined, Consumer<S> handler) {
				    addAll3(combined, new OperationsPile<>(null));
				  }
					public static void main(String[] args) {
						adaad3(null, null);
						System.out.println(13);
					}
				}
				"""
		},
		"13");
}
public void testBug428307() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import java.util.function.Function;
				import java.util.stream.*;
				
				interface Bar {
					Class<? extends Bar> type();
				}
				public class X {
				\s
				    <T extends Bar> T[] test(Class<T> barClass, Stream<Bar> bars) {
				        return get(bars.
				                    collect(Collectors.toMap(Bar::type,
				                                             Function.identity(),
				                                             ((first,second) -> first),
				                                             HashMap::new)),
				                            barClass);
				    }
				   \s
				    <A extends Bar> A[] get(Map<Class<? extends Bar>,Bar> m, Class<A> c) {
				    	return null;
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428524, [1.8][compiler] NPE when using JSE8 Class Constructor ref "TheClass::new" and "TheClass" is using default no-arg constructor
public void test428524() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Supplier;
				public class X {
					public static void main(String[] args) {
						Supplier<WithNoArgConstructor> works = WithNoArgConstructor::new;
						System.out.println(works.get());
						Supplier<WithoutNoArgConstructor> error = WithoutNoArgConstructor::new;
						System.out.println(error.get());
					\t
					}
					private static class WithNoArgConstructor {
						public WithNoArgConstructor() {
						}
				       public String toString() {
				           return("WithNoArgConstructor");
				       }
					}
					private static class WithoutNoArgConstructor {
				       public String toString() {
				           return("WithOutNoArgConstructor");
				       }
					}
				}
				"""
		},
		"WithNoArgConstructor\n" +
		"WithOutNoArgConstructor");
}
public void testBug428786() {
	runConformTest(
		new String[] {
			"Junk9.java",
			"""
				import java.util.*;
				public class Junk9 {
				    class Node {
				        public double getLayoutY() {return 12;}
				    }
				    class Node2 extends Node {
				    }
				    void junk() {
				        List<Node2> visibleCells = new ArrayList<>(20);
				        Collections.sort(visibleCells, (Node o1, Node o2) -> Double.compare(o1.getLayoutY(), o2.getLayoutY()));
				    }
				}
				"""
		});
}
public void testBug429090_comment1() {
	runNegativeTest(
		new String[] {
			"Junk10.java",
			"""
				
				public class Junk10 {
				    class Observable<T> {}
				    interface InvalidationListener {
				        public void invalidated(Observable<?> observable);
				    }
				    public static abstract class Change<E2> {}
				    interface SetChangeListener<E1> {
				        void onChanged(Change<? extends E1> change);
				    }
				    class SetListenerHelper<T> {}
				    public static <E> SetListenerHelper<E> addListener(
							SetListenerHelper<E> helper, SetChangeListener<? super E> listener) {
				        return helper;
				    }
				    void junk() {
				        addListener(null, (SetChangeListener.Change<?> c) -> {});
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Junk10.java (at line 17)
				addListener(null, (SetChangeListener.Change<?> c) -> {});
				^^^^^^^^^^^
			The method addListener(Junk10.SetListenerHelper<E>, Junk10.SetChangeListener<? super E>) in the type Junk10 is not applicable for the arguments (null, (SetChangeListener.Change<?> c) -> {})
			----------
			2. ERROR in Junk10.java (at line 17)
				addListener(null, (SetChangeListener.Change<?> c) -> {});
				                   ^^^^^^^^^^^^^^^^^^^^^^^^
			SetChangeListener.Change cannot be resolved to a type
			----------
			""");
}
public void testBug429090() {
	runConformTest(
		new String[] {
			"Junk10.java",
			"public class Junk10 {\n" +
			"    class Observable<T> {}\n" +
			"    interface InvalidationListener {\n" +
			"        public void invalidated(Observable observable);\n" +
			"    }\n" +
			"    interface SetChangeListener<E> {\n" +
			"        public static abstract class Change<E> {}\n" +
			"        void onChanged(Change<? extends E> change);\n" +
			"    }\n" +
			"    class SetListenerHelper<T> {}\n" +
			"    public static <E> SetListenerHelper<E> addListener(SetListenerHelper<E> helper, InvalidationListener listener) {\n" +
			"        return helper;\n" +
			"    }\n" +
			"    public static <E> SetListenerHelper<E> addListener(SetListenerHelper<E> helper, SetChangeListener<? super E> listener) {\n" +
			"        return helper;\n" +
			"    }\n" +
			"    void junk() {\n" +
			"        addListener(null, new SetChangeListener () {\n" +
			"            public void onChanged(SetChangeListener.Change change) {}\n" +
			"        });\n" +
			"        addListener(null, (SetChangeListener.Change<? extends Object> c) -> {});\n" + // original was without "extends Object"
			"    }\n" +
			"}\n"
		});
}
public void testBug429490_comment33() {
    runConformTest(
        new String[] {
            "Junk12.java",
            """
				public class Junk12 {
				    class Observable<T> {}
				    class ObservableValue<T> {}
				    interface InvalidationListener {
				        public void invalidated(Observable observable);
				    }
				    public interface ChangeListener<T> {
				        void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);
				    }
				    class ExpressionHelper<T> {}
				    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, InvalidationListener listener) {
				        return helper;
				    }
				    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, ChangeListener<? super T> listener) {
				        return helper;
				    }
				    void junk() {
				        addListener(null, null, new ChangeListener () {
				            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				                throw new RuntimeException();
				            }
				        });
				        addListener(null, null, (value, o1, o2) -> {throw new RuntimeException();});
				    }
				}
				"""
        });
}
public void testBug428811() {
	runConformTest(
		new String[] {
			"MoreCollectors.java",
			"""
				import java.util.AbstractList;
				import java.util.ArrayList;
				import java.util.Arrays;
				import java.util.Collection;
				import java.util.List;
				import java.util.stream.Collector;
				
				public class MoreCollectors {
				    public static void main (String[] args) {
				        ImmutableList<String> list = Arrays.asList("a", "b", "c").stream().collect(toImmutableList());
				       \s
				        System.out.println(list);
				    }
				   \s
				    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList () {
				        return Collector.of(ArrayList<T>::new,
				                List<T>::add,
				                (left, right) -> { left.addAll(right); return left; },
				                ImmutableList::copyOf);
				    }
				   \s
				    private static class ImmutableList<T> extends AbstractList<T> {
				        public static <T> ImmutableList<T> copyOf (Collection<T> c) {
				            return new ImmutableList<>(c.toArray());
				        }
				
				        private Object[] array;
				       \s
				        private ImmutableList (Object[] array) {
				            this.array = array;
				        }
				
				        @Override @SuppressWarnings("unchecked")
				        public T get(int index) {
				            return (T)array[index];
				        }
				
				        @Override
				        public int size() {
				            return array.length;
				        }
				    }
				}
				"""
		},
		"[a, b, c]");
}
// all exceptions can be inferred to match
public void testBug429430() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				import java.io.*;
				public class Main {
				  public static interface Closer<T, X extends Exception> {
				    void closeIt(T it) throws X;
				  }
				
				  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {
				    closer.closeIt(it);
				  }
				
				  public static void main(String[] args) throws IOException {
				    InputStream in = new ByteArrayInputStream("hello".getBytes());
				    close( x -> x.close(), in );
				    close( InputStream::close, in );
				    close( (Closer<InputStream, IOException>)InputStream::close, in );
				  }
				}
				"""
		});
}
// incompatible exceptions prevent suitable inference of exception type
public void testBug429430a() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"""
				import java.io.*;
				@SuppressWarnings("serial") class EmptyStream extends Exception {}
				public class Main {
				  public static interface Closer<T, X extends Exception> {
				    void closeIt(T it) throws X;
				  }
				
				  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {
				    closer.closeIt(it);
				  }
				
				  public static void main(String[] args) throws IOException, EmptyStream {
				    InputStream in = new ByteArrayInputStream("hello".getBytes());
				    close( x ->  { if (in.available() == 0) throw new EmptyStream(); x.close(); }, in );
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in Main.java (at line 14)
				close( x ->  { if (in.available() == 0) throw new EmptyStream(); x.close(); }, in );
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type Exception
			----------
			""");
}
// one of two incompatible exceptions is caught
public void testBug429430b() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				import java.io.*;
				@SuppressWarnings("serial") class EmptyStream extends Exception {}
				public class Main {
				  public static interface Closer<T, X extends Exception> {
				    void closeIt(T it) throws X;
				  }
				
				  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {
				    closer.closeIt(it);
				  }
				
				  public static void main(String[] args) throws EmptyStream {
				    InputStream in = new ByteArrayInputStream("hello".getBytes());
				    close( x ->  {
							try {
								x.close();
							} catch (IOException ioex) { throw new EmptyStream(); }\s
						},\
						in);
				  }
				}
				"""
		});
}
public void testBug429430b2() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				@SuppressWarnings("serial") class EmptyStream extends Exception {}
				public class X {
				  public static interface Closer<T, V extends Exception> {
				    void closeIt(T it) throws V;
				  }
				
				  public static void close( Closer<InputStream, EmptyStream> closer, InputStream it ) throws EmptyStream {
				    closer.closeIt(it);
				  }
				
				  public static void main(String[] args) throws EmptyStream {
				    InputStream in = new ByteArrayInputStream("hello".getBytes());
				    close( x ->  {
							if (x == null)
								throw new IOException();
							else\s
								throw new EmptyStream();\s
						},
						in);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 16)
				throw new IOException();
				^^^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			2. WARNING in X.java (at line 18)
				throw new EmptyStream();\s
				^^^^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""");
}
// ensure type annotation on exception doesn't confuse the inference
public void testBug429430c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
	runConformTest(
		new String[] {
			"Main.java",
			"""
				import java.io.*;
				import java.lang.annotation.*;
				@Target(ElementType.TYPE_USE) @interface Severe {}
				public class Main {
				  public static interface Closer<T, X extends Exception> {
				    void closeIt(T it) throws X;
				  }
				
				  public static <T, X extends Exception> void close( Closer<T, X> closer, T it ) throws X {
				    closer.closeIt(it);
				  }
				
				  static @Severe IOException getException() { return new IOException("severe"); }
				  public static void main(String[] args) throws IOException {
				    InputStream in = new ByteArrayInputStream("hello".getBytes());
				    close( x -> {
							if (in.available() > 0)
								x.close();
							else
								throw getException();
						},
						in);
				  }
				}
				"""
		},
		options);
}
public void testBug429490() {
	runConformTest(
		new String[] {
			"Junk11.java",
			"""
				public class Junk11 {
				    class Observable<T> {}
				    class ObservableValue<T> {}
				    interface InvalidationListener {
				        public void invalidated(Observable observable);
				    }
				    public interface ChangeListener<T> {
				        void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);
				    }
				    class ExpressionHelper<T> {}
				    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, InvalidationListener listener) {
				        return helper;
				    }
				    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, ChangeListener<? super T> listener) {
				        return helper;
				    }
				    void junk() {
				        addListener(null, null, new InvalidationListener () {
				            public void invalidated(Observable o) {throw new RuntimeException();}
				        });
				        addListener(null, null, (o) -> {throw new RuntimeException();});
				    }
				}
				"""
		});
}
public void testBug429424() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
				    public static void main (String[] args) {
				        List<String> list = new ArrayList<>();
				        list.addAll(X.newArrayList());
				        System.out.println(list);
				    }
				   \s
				    public static <T> List<T> newArrayList () {
				        return new ArrayList<T>();
				    }
				}
				
				"""
		});
}
public void testBug426537() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(J[] list, I<J<?>> i) {
						sort(list, i);
					}
				\t
					<T> T[] sort(T[] list, I<? super T> i) {
						return list;
					}
				}
				interface I<T> {}
				interface J<T> {}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				void foo(J[] list, I<J<?>> i) {
				         ^
			J is a raw type. References to generic type J<T> should be parameterized
			----------
			2. ERROR in X.java (at line 3)
				sort(list, i);
				^^^^
			The method sort(T[], I<? super T>) in the type X is not applicable for the arguments (J[], I<J<?>>)
			----------
			""");
}
public void testBug426537b() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				interface C<T, A, R> {}
				
				class MImpl<K, V> {}
				
				interface S<T> { T get(); }
				
				public class Test {
					static <T, K, D> C<T, ?, MImpl<K, D>> m1() {
				        return m2(MImpl::new);
				    }
				   \s
				    static <T, K, D, M extends MImpl<K, D>> C<T, ?, M> m2(S<M> s) {
				    	return null;
				    }
				}
				
				"""
		});
}
public void testBug426537c() {
	// touching MImpl#RAW before type inference we got undesired results from #typeArguments()
	runConformTest(
		new String[] {
			"Ups.java",
			"public class Ups {\n" +
			"    static Object innocent(MImpl o) {\n" +
			"            return o.remove(\"nice\");\n" + // method lookup triggers initialization of the RawTypeBinding.
			"    }\n" +
			"}\n",
			"Test.java",
			"""
				interface S<T> { T get(); }
				interface F<T, R> { R apply(T t); }
				interface C<T, A, R> { }
				interface IM<K,V> {}
				class MImpl<K,V>  implements IM<K,V> {\s
					public V remove(Object key) { return null; }\s
				}
				public final class Test {
				
				    static <T, K, A, D>
				    C<T, ?, IM<K, D>> m1(F<? super T, ? extends K> f, C<? super T, A, D> c) {
				        return m2(f, MImpl::new, c);
				    }
				
				    static <T, K, D, A, M extends IM<K, D>>
				    C<T, ?, M> m2(F<? super T, ? extends K> classifier,
				                                  S<M> mapFactory,
				                                  C<? super T, A, D> downstream) {
				    	return null;
				    }
				}
				"""
		});
}
public void testBug429203() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			"DTest.java",
			"""
				import java.util.function.Function;
				
				
				public class DTest<T> {
					public DTest(Function<T, T> func) { }
				\t
					public DTest(DTest<Integer> dti) {}
					public DTest() {}
				\t
					public static void main(String[] args) {
						DTest<String> t1 = new DTest<String>(new DTest<Integer>());
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in DTest.java (at line 11)
				DTest<String> t1 = new DTest<String>(new DTest<Integer>());
				                       ^^^^^
			Redundant specification of type arguments <String>
			----------
			2. ERROR in DTest.java (at line 11)
				DTest<String> t1 = new DTest<String>(new DTest<Integer>());
				                                         ^^^^^
			Redundant specification of type arguments <Integer>
			----------
			""",
		null, true, customOptions);
}
public void testBug430296() {
	runNegativeTest(
		new String[] {
			"AnnotationCollector.java",
			"""
				import java.lang.annotation.*;
				import java.util.*;
				import java.util.function.*;
				import java.util.stream.*;
				
				public abstract class AnnotationCollector {
				       \s
				        Map<String, Person> test2(Stream<Person> persons) {
				                return persons.collect(Collectors.toMap((Person p) -> p.getLastName(),
				                                                                Function::identity,
				                                                        (p1, p2) -> p1));
				        }
				}
				
				class Person {
				        String getLastName() { return ""; }
				}"""
		},
		"""
			----------
			1. ERROR in AnnotationCollector.java (at line 9)
				return persons.collect(Collectors.toMap((Person p) -> p.getLastName(),
				                                  ^^^^^
			The method toMap(Function<? super T,? extends K>, Function<? super T,? extends U>, BinaryOperator<U>) in the type Collectors is not applicable for the arguments ((Person p) -> {}, Function::identity, (<no type> p1, <no type> p2) -> {})
			----------
			2. ERROR in AnnotationCollector.java (at line 9)
				return persons.collect(Collectors.toMap((Person p) -> p.getLastName(),
				                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Function<Person,K> to Function<? super T,? extends K>
			----------
			3. ERROR in AnnotationCollector.java (at line 10)
				Function::identity,
				^^^^^^^^^^^^^^^^^^
			The type Function does not define identity(T) that is applicable here
			----------
			""");
}
public void testBug430759() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.beans.*;
				import java.util.*;
				import java.util.function.*;
				
				public abstract class Test<T,R> implements Function<T,R> {
				
				  public static final <T,R> Test<T,R> getTest() {
				    return new Test<T,R>() {
				      protected final Map<T,ResultSupplier> results = Collections.synchronizedMap(new HashMap<T,ResultSupplier>());
				      @Override
				      public R apply(final T t) {
				        ResultSupplier result = results.get(t);
				        return result.get();
				      }
				      class ResultSupplier implements Supplier<R> {
				        @Override
				        public synchronized R get() {
				          return null;
				        }
				      }
				    };
				  }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431577 [1.8][bytecode] Bad type on operand stack (different than Bug 429733)
public void testBug431577() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.Function;
				import java.util.function.IntFunction;
				public class Test<R> {
					public static void main(String[] args) {
					new Test<>().test((Integer i) -> null);
					}
					<T> void test(Function<T, R> f) {
					}
					void test(int i, IntFunction<R> f) {
						new State<>(new Val<>(i));
					}
					static class State<R> {
						State(Val<?> o) {
						}
					}
					static class Val<T> {
						Val(T t) {}
					}
				}"""
	});
}
public void testBug432110() {
	runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.util.List;
				import java.util.function.Function;
				import java.util.stream.Stream;
				
				class Bug
				{
				    // fully inline
				    // compiles successfully
				    Stream<? extends Integer> flatten1(
				        final Stream<List<Integer>> input)
				    {
				        return input.flatMap(item -> item.stream().map(value -> value));
				    }
				
				    // lambda using braces
				    // compiles with error in eclipse, successfully with javac
				    Stream<? extends Integer> flatten2(
				        final Stream<List<Integer>> input)
				    {
				        return input.flatMap(item -> {
				            return item.stream().map(value -> value);
				        });
				    }
				
				    // without map step
				    // compiles successfully
				    Stream<? extends Integer> flatten3(
				        final Stream<List<Integer>> input)
				    {
				        return input.flatMap(item -> {
				            return item.stream();
				        });
				    }
				
				    // with map step, but not inline
				    // compiles successfully
				    Stream<? extends Integer> flatten4(
				        final Stream<List<Integer>> input)
				    {
				        return input.flatMap(item -> {
				            final Function<? super Integer, ? extends Integer> mapper = value -> value;
				            return item.stream().map(mapper);
				        });
				    }
				
				    // with map step, but outer lambda is not inline
				    // compiles successfully
				    Stream<? extends Integer> flatten5(
				        final Stream<List<Integer>> input)
				    {
				        final Function<? super List<Integer>, ? extends Stream<? extends Integer>> func = item -> {
				            return item.stream().map(value -> value);
				        };
				        return input.flatMap(func);
				    }
				}
				"""});
}
public void testBug433158() {
	runNegativeTest(
		new String[] {
			"CollectorsMaps.java",
			"""
				
				import java.util.List;
				import java.util.Map;
				import static java.util.stream.Collectors.*;
				
				public class CollectorsMaps {/*Q*/
					private static class Pair<L, R> {
						public final L lhs; public final R rhs;
						public Pair(L lhs, R rhs) { this.lhs = lhs; this.rhs = rhs; }
						public R rhs() { return rhs; }
						public L lhs() { return lhs; }
						public <N> Pair<N, R> keepingRhs(N newLhs) { return new Pair<>(newLhs, rhs); }
						/*E*/}
				
					static Map<String, List<String>> invert(Map<String, List<String>> packages) {
						return packages.entrySet().stream().map(e -> new Pair<>(e.getValue(), e.getKey())).flatMap(
							//The method collect(Collector<? super Object,A,R>) in the type Stream<Object>
							//is not applicable for the arguments\s
							//(Collector<CollectorsMaps.Pair<String,String>,capture#3-of ?,Map<String,List<String>>>)
						  p -> p.lhs.stream().map(p::keepingRhs)).collect(
						  groupingBy(Pair<String, String>::lhs, mapping(Pair<String, String>::rhs, toList())));
					}
				}
				"""
		},
		"");
}
public void testBug432626() {
	runConformTest(
		new String[] {
			"StreamInterface2.java",
			"""
				import java.util.ArrayList;
				import java.util.HashMap;
				import java.util.Map;
				import java.util.function.Function;
				import java.util.function.Supplier;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				
				public interface StreamInterface2 {
				
					static <T, E extends Exception, K> Map<K, ArrayList<T>> terminalAsMapToList(
					  Function<? super T, ? extends K> classifier,
					  Supplier<Stream<T>> supplier,
					  Class<E> classOfE) throws E {
						return terminalAsCollected(classOfE, Collectors.groupingBy(
							  classifier,
							  //This is OK:
							  //Redundant specification of type arguments <K, ArrayList<T>>
							  () -> new HashMap<K, ArrayList<T>>(),
							  Collector.<T, ArrayList<T>> of(
							    () -> new ArrayList<>(),
							    (left, value) -> left.add(value),
							    (left, right) -> combined(left, right))), supplier);
					}
					static <T, E extends Exception, K> Map<K, ArrayList<T>> terminalAsMapToList2(
					  Function<? super T, ? extends K> classifier,
					  Supplier<Stream<T>> supplier,
					  Class<E> classOfE) throws E {
						//After removing type arguments, ECJ shows error, javac doesn't:
						//Type mismatch: cannot convert from HashMap<capture#2-of ? extends K,ArrayList<T>> to Map<K,ArrayList<T>>
						return terminalAsCollected(classOfE, Collectors.groupingBy(
							  classifier,
							  () -> new HashMap<>(),
							  Collector.<T, ArrayList<T>> of(
							    () -> new ArrayList<>(),
							    (left, value) -> left.add(value),
							    (left, right) -> combined(left, right))), supplier);
					}
					static <E extends Exception, T, M> M terminalAsCollected(
					  Class<E> classOfE,
					  Collector<T, ?, M> collector,
					  Supplier<Stream<T>> supplier) throws E {
						try(Stream<T> s = supplier.get()) {
							return s.collect(collector);
						} catch(RuntimeException e) {
							throw unwrapCause(classOfE, e);
						}
					}
					static <E extends Exception> E unwrapCause(Class<E> classOfE, RuntimeException e) throws E {
						Throwable cause = e.getCause();
						if(classOfE.isInstance(cause) == false) {
							throw e;
						}
						throw classOfE.cast(cause);
					}
					static <T> ArrayList<T> combined(ArrayList<T> left, ArrayList<T> right) {
						left.addAll(right);
						return left;
					}
				}
				"""
		});
}
public void testBug432626_reduced() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.HashMap;
				import java.util.Map;
				import java.util.function.Function;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				public interface X {
					static <T, K> Map<K, ArrayList<T>> terminalAsMapToList(Function<? super T, ? extends K> classifier)  {
						return terminalAsCollected(Collectors.groupingBy(
							  classifier,
							  () -> new HashMap<>(),
							  (Collector<T,ArrayList<T>,ArrayList<T>>) null));
					}
					static <T, M> M terminalAsCollected(Collector<T, ?, M> collector) {
						return null;
					}
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433825 [1.8][compiler] Internal compiler error: NullPointerException in AllocationExpression#resolvePart3
public void testBug433825() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				public class X {
				  public static void main(String[] args) {
				  }
				  public void bla() {
				    boolean b = Boolean.TRUE.booleanValue();
				    List<String> c1 = new ArrayList<>();
				    new Bar(b ? c1 : new ArrayList<>()); // this line crashes ecj (4.4 I20140429-0800), but not ecj (eclipse 3.8.2) and javac
				  }
				  private static class Bar {
					  public Bar(Collection<?> col) { }
				  }
				}"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433825 [1.8][compiler] Internal compiler error: NullPointerException in AllocationExpression#resolvePart3
public void testBug433825a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				public class X {
				  public static void main(String[] args) {
				  }
				  public void bla() {
				    boolean b = Boolean.TRUE.booleanValue();
				    new Bar(b ? 0 : new ArrayList<>());
				  }
				  private static class Bar {
					  public Bar(Collection<String> col) { }
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				new Bar(b ? 0 : new ArrayList<>());
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The constructor X.Bar((b ? 0 : new ArrayList<>())) is undefined
			----------
			2. ERROR in X.java (at line 9)
				new Bar(b ? 0 : new ArrayList<>());
				            ^
			Type mismatch: cannot convert from int to Collection<String>
			----------
			3. WARNING in X.java (at line 12)
				public Bar(Collection<String> col) { }
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The constructor X.Bar(Collection<String>) is never used locally
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435462 [1.8] NPE in codegen with nested conditional and allocation expressions
public void testBug435462() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.List;
				public class X {
				  public static void main(String[] args) {
				  }
				  public void bla() {
				    boolean b = Boolean.TRUE.booleanValue();
				    List<String> c1 = new ArrayList<>();
				    new Bar(b ? new ArrayList<>(b ? new ArrayList<>() : c1) : c1);
				  }
				  private static class Bar {
					  public Bar(Collection<?> col) { }
				  }
				}"""
	});
}
public void testBug437007() {
	runConformTest(
		new String[] {
			"ExecutorTests.java",
			"""
				import java.util.*;
				
				public class ExecutorTests {
				    List<Runnable> tasks = Arrays.asList(
				            () -> {
				                System.out.println("task1 start");
				            }
				    );
				
				    public void executeInSync(){
				        tasks.stream().forEach(Runnable::run);
				    }
				}
				"""
		});
}
public void testBug435689() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.*;
				class Foo<T> {
				  <U> void apply(Function<T, Consumer<U>> bar) {}
				}
				
				class Bar {
				  void setBar(String bar){}
				}
				public class Test {
					void test() {
						new Foo<Bar>().apply(bar -> bar::setBar);
					}
				}
				"""
		});
}
public void testBug433845() {
	runNegativeTest(
		new String[] {
			"test/Test.java",
			"""
				package test;
				
				
				public abstract class Test {
					public interface MUIElement {
					\t
					}
				
					private interface Listener<W extends WWidget<?>> {
						public void call(Event<W> event);
					}
				\t
					public static class Event<W extends WWidget<?>> {
					\t
					}
				\t
					public interface WWidget<M extends MUIElement> {
						public void set(Listener<? extends WWidget<M>> handler);
					}
				\t
					public static abstract class A<M extends MUIElement, W extends WWidget<M>> {
					\t
						public final W createWidget(final M element) {
							W w = get();\s
							// works
							w.set((Event<W>e) -> call(e));
							// fails
							w.set(this::call);
							// fails
							w.set((e) -> call(e));
							return w;
						}
					\t
						private W get() {
							return null;
						}
					\t
						private void call(Event<W> event) {
						\t
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in test\\Test.java (at line 28)
				w.set(this::call);
				  ^^^
			The method set(Test.Listener<? extends Test.WWidget<M>>) in the type Test.WWidget<M> is not applicable for the arguments (this::call)
			----------
			2. ERROR in test\\Test.java (at line 28)
				w.set(this::call);
				      ^^^^^^^^^^
			The type Test.A<M,W> does not define call(Test.Event<Test.WWidget<M>>) that is applicable here
			----------
			3. ERROR in test\\Test.java (at line 30)
				w.set((e) -> call(e));
				             ^^^^
			The method call(Test.Event<W>) in the type Test.A<M,W> is not applicable for the arguments (Test.Event<Test.WWidget<M>>)
			----------
			""");
}
public void testBug435187() {
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		new String[] {
			"ExtractLocalLambda.java",
			"""
				
				import java.util.List;
				import java.util.Map;
				import java.util.Map.Entry;
				import java.util.function.Function;
				import java.util.stream.Collector;
				import java.util.stream.Stream;
				
				public class ExtractLocalLambda {
					static Stream<Entry<List<String>, String>> map;
					static Collector<Entry<String, String>, ?, Map<String, List<String>>> groupingBy;
					private static Stream<String> stream(Entry<List<String>, String> p) {		return null;	}
					private static Entry<String, String> keep(Entry<List<String>, String> p, String leftHS2) {		return null;	}
				
					static Map<String, List<String>> beforeRefactoring() {
						// Extract local variable from the parameter to flatMap:
						return map.flatMap(
								p -> stream(p).map(leftHS -> {
									String leftHS2 = leftHS;
									return keep(p, leftHS2);
								})
						).collect(groupingBy);
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in ExtractLocalLambda.java (at line 5)
				import java.util.function.Function;
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The import java.util.function.Function is never used
			----------
			""");
}
public void testBug435767() {
	runConformTest(
		new String[] {
			"DummyClass.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import java.util.stream.*;
				public class DummyClass {
				
					public void method() {
				
						// Cases where there is no error
						final Supplier<Set<String>> suppliers = this.memoize(() -> new HashSet<>());
				
						final Supplier<Map<Object, Object>> noMemoize = () -> suppliers.get().stream()
								.filter(path -> path.startsWith(""))
								.collect(Collectors.toMap(path -> this.getKey(path), path -> this.getValue(path)));
				
						// Case where there is errors.
						final Supplier<Map<Object, Object>> memoize = this.memoize(() -> suppliers.get().stream()
								.filter(path -> path.startsWith(""))
								.collect(Collectors.toMap(path -> this.getKey(path), path -> this.getValue(path))));
				
						// Error message are : Description
						// Resource	Path	Location	Type
						// The method getKey(String) in the type DummyClass is not applicable for the arguments (Object)	DummyClass.java line 23	Java Problem
						// The method getValue(String) in the type DummyClass is not applicable for the arguments (Object)	DummyClass.java line 23	Java Problem
				
					}
				
					private <T> Supplier<T> memoize(final Supplier<T> delegate) {
						return delegate;
					}
				
					private Object getKey(final String path) {
						return path;
					}
				
					private Object getValue(final String path) {
						return path;
					}
				}
				"""
		},
		"");
}
public void testBug434483() {
	runConformTest(
		new String[] {
			"Foo.java",
			"""
				import java.util.*;
				public class Foo {
				\t
				  // Similar to Guava's newLinkedList()
				  public static <E> LinkedList<E> newLinkedList() {
				    return new LinkedList<E>();
				  }
				\t
				  private final ThreadLocal<Queue<String>> brokenQueue = ThreadLocal.withInitial(Foo::newLinkedList);
				\t
				  private final ThreadLocal<Queue<String>> workingQueue1 = ThreadLocal.withInitial(Foo::<String>newLinkedList);
				\t
				  private final ThreadLocal<Queue<String>> workingQueue2 = ThreadLocal.withInitial(() -> Foo.<String>newLinkedList());
				
				}
				"""
		});
}
public void testBug441734() {
	runConformTest(
		new String[] {
			"Example.java",
			"""
				import java.util.*;
				import java.util.function.*;
				class Example {
				    void foo(Iterable<Number> x) { }
				
				    <T> void bar(Consumer<Iterable<T>> f) { }
				
				    void test() {
				        //call 1: lambda w/argument type - OK
				        bar((Iterable<Number> x) -> foo(x));
				
				        //call 2: lambda w/explicit type - OK
				        this.<Number> bar(x -> foo(x));
				
				        //call 3: method ref w/explicit type - OK
				        this.<Number> bar(this::foo);
				
				        //call 4: lambda w/implicit type - correctly(?) fails*
				        //bar(x -> foo(x));
				
				        //call 5: method ref w/implicit type - BUG!
				        bar(this::foo); // errors!
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442245, [1.8][compiler?] These source files lead eclipse to hang (even just on copy/paste)
public void testBug442245() {
	runConformTest(
		new String[] {
			"test/Pattern.java",
			"""
				package test;
				import test.Tuples.Tuple;
				import test.Tuples.Tuple1;
				import test.Tuples.Tuple10;
				import test.Tuples.Tuple11;
				import test.Tuples.Tuple12;
				import test.Tuples.Tuple13;
				import test.Tuples.Tuple2;
				import test.Tuples.Tuple3;
				import test.Tuples.Tuple4;
				import test.Tuples.Tuple5;
				import test.Tuples.Tuple6;
				import test.Tuples.Tuple7;
				import test.Tuples.Tuple8;
				import test.Tuples.Tuple9;
				
				public interface Pattern<R extends Tuple> {
				
					boolean isApplicable(Object o);
				
					R apply(Object o);
				
					static <T, R extends Tuple> Pattern<R> of(Decomposition<T, R> decomposition, R prototype) {
						return null;
					}
				
					static <T, E1> Pattern<Tuple1<E1>> of(Decomposition<T, Tuple1<E1>> decomposition, E1 e1) {
						return Pattern.of(decomposition, Tuples.of(e1));
					}
				
					static <T, E1, E2> Pattern<Tuple2<E1, E2>> of(Decomposition<T, Tuple2<E1, E2>> decomposition, E1 e1, E2 e2) {
						return Pattern.of(decomposition, Tuples.of(e1, e2));
					}
				
					static <T, E1, E2, E3> Pattern<Tuple3<E1, E2, E3>> of(Decomposition<T, Tuple3<E1, E2, E3>> decomposition, E1 e1,
							E2 e2, E3 e3) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3));
					}
				
					static <T, E1, E2, E3, E4> Pattern<Tuple4<E1, E2, E3, E4>> of(
							Decomposition<T, Tuple4<E1, E2, E3, E4>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4));
					}
				
					static <T, E1, E2, E3, E4, E5> Pattern<Tuple5<E1, E2, E3, E4, E5>> of(
							Decomposition<T, Tuple5<E1, E2, E3, E4, E5>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5));
					}
				
					static <T, E1, E2, E3, E4, E5, E6> Pattern<Tuple6<E1, E2, E3, E4, E5, E6>> of(
							Decomposition<T, Tuple6<E1, E2, E3, E4, E5, E6>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5, E6 e6) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7> Pattern<Tuple7<E1, E2, E3, E4, E5, E6, E7>> of(
							Decomposition<T, Tuple7<E1, E2, E3, E4, E5, E6, E7>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5,
							E6 e6, E7 e7) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7, E8> Pattern<Tuple8<E1, E2, E3, E4, E5, E6, E7, E8>> of(
							Decomposition<T, Tuple8<E1, E2, E3, E4, E5, E6, E7, E8>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4, E5 e5,
							E6 e6, E7 e7, E8 e8) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9> Pattern<Tuple9<E1, E2, E3, E4, E5, E6, E7, E8, E9>> of(
							Decomposition<T, Tuple9<E1, E2, E3, E4, E5, E6, E7, E8, E9>> decomposition, E1 e1, E2 e2, E3 e3, E4 e4,
							E5 e5, E6 e6, E7 e7, E8 e8, E9 e9) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10> Pattern<Tuple10<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10>> of(
							Decomposition<T, Tuple10<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10>> decomposition, E1 e1, E2 e2, E3 e3,
							E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> Pattern<Tuple11<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>> of(
							Decomposition<T, Tuple11<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>> decomposition, E1 e1, E2 e2, E3 e3,
							E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10, E11 e11) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> Pattern<Tuple12<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>> of(
							Decomposition<T, Tuple12<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>> decomposition, E1 e1, E2 e2,
							E3 e3, E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10, E11 e11, E12 e12) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));
					}
				
					static <T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> Pattern<Tuple13<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>> of(
							Decomposition<T, Tuple13<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>> decomposition, E1 e1,
							E2 e2, E3 e3, E4 e4, E5 e5, E6 e6, E7 e7, E8 e8, E9 e9, E10 e10, E11 e11, E12 e12, E13 e13) {
						return Pattern.of(decomposition, Tuples.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13));
					}
				
					static interface Decomposition<T, R extends Tuple> {
				
						R apply(T t);
					}
				}
				""",
			"test/Tuples.java",
			"""
				package test;
				
				import java.io.Serializable;
				import java.util.Objects;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				
				public final class Tuples {
				
					private Tuples() {
						throw new AssertionError(Tuples.class.getName() + " is not intended to be instantiated.");
					}
				
					public static Tuple0 of() {
						return Tuple0.instance();
					}
				
					public static <T> Tuple1<T> of(T t) {
						return new Tuple1<>(t);
					}
				
					public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
						return new Tuple2<>(t1, t2);
					}
				
					public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
						return new Tuple3<>(t1, t2, t3);
					}
				
					public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {
						return new Tuple4<>(t1, t2, t3, t4);
					}
				
					public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
						return new Tuple5<>(t1, t2, t3, t4, t5);
					}
				
					public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
						return new Tuple6<>(t1, t2, t3, t4, t5, t6);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5,
							T6 t6, T7 t7) {
						return new Tuple7<>(t1, t2, t3, t4, t5, t6, t7);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> of(T1 t1, T2 t2, T3 t3,
							T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {
						return new Tuple8<>(t1, t2, t3, t4, t5, t6, t7, t8);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 t1, T2 t2,
							T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) {
						return new Tuple9<>(t1, t2, t3, t4, t5, t6, t7, t8, t9);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(T1 t1,
							T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10) {
						return new Tuple10<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(
							T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11) {
						return new Tuple11<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(
							T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) {
						return new Tuple12<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
					}
				
					public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(
							T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13) {
						return new Tuple13<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
					}
				
					private static String stringify(Object... objects) {
						return Stream
								.of(objects)
								.map(o -> (o == null) ? "null" : o.toString())
								.collect(Collectors.joining(", ", "(", ")"));
					}
				
					public static final class Tuple0 implements Tuple, Serializable {
				
						private static final long serialVersionUID = -8715576573413569748L;
				
						private static final Tuple0 INSTANCE = new Tuple0();
				
						private Tuple0() {
						}
				
						public static Tuple0 instance() {
							return INSTANCE;
						}
				
						@Override
						public int arity() {
							return 0;
						}
				
						@Override
						public String toString() {
							return Tuples.stringify();
						}
				
						private Object readResolve() {
							return INSTANCE;
						}
					}
				
					public static final class Tuple1<T> implements Tuple, Serializable {
				
						private static final long serialVersionUID = -8005498887610699234L;
				
						public final T _1;
				
						public Tuple1(T t) {
							this._1 = t;
						}
				
						@Override
						public int arity() {
							return 1;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple1)) {
								return false;
							} else {
								final Tuple1<?> that = (Tuple1<?>) o;
								return Objects.equals(this._1, that._1);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1);
						}
					}
				
					public static final class Tuple2<T1, T2> implements Tuple, Serializable {
				
						private static final long serialVersionUID = -1359843718617881431L;
				
						public final T1 _1;
						public final T2 _2;
				
						public Tuple2(T1 t1, T2 t2) {
							this._1 = t1;
							this._2 = t2;
						}
				
						@Override
						public int arity() {
							return 2;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple2)) {
								return false;
							} else {
								final Tuple2<?, ?> that = (Tuple2<?, ?>) o;
								return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2);
						}
					}
				
					public static final class Tuple3<T1, T2, T3> implements Tuple, Serializable {
				
						private static final long serialVersionUID = 1353320010987934190L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
				
						public Tuple3(T1 t1, T2 t2, T3 t3) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
						}
				
						@Override
						public int arity() {
							return 3;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple3)) {
								return false;
							} else {
								final Tuple3<?, ?, ?> that = (Tuple3<?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3);
						}
					}
				
					public static final class Tuple4<T1, T2, T3, T4> implements Tuple, Serializable {
				
						private static final long serialVersionUID = -835853771811712181L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
				
						public Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
						}
				
						@Override
						public int arity() {
							return 4;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple4)) {
								return false;
							} else {
								final Tuple4<?, ?, ?, ?> that = (Tuple4<?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4);
						}
					}
				
					public static final class Tuple5<T1, T2, T3, T4, T5> implements Tuple, Serializable {
				
						private static final long serialVersionUID = 8365094604388856720L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
				
						public Tuple5(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
						}
				
						@Override
						public int arity() {
							return 5;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple5)) {
								return false;
							} else {
								final Tuple5<?, ?, ?, ?, ?> that = (Tuple5<?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5);
						}
					}
				
					public static final class Tuple6<T1, T2, T3, T4, T5, T6> implements Tuple, Serializable {
				
						private static final long serialVersionUID = -5282391675740552818L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
				
						public Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
						}
				
						@Override
						public int arity() {
							return 6;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple6)) {
								return false;
							} else {
								final Tuple6<?, ?, ?, ?, ?, ?> that = (Tuple6<?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6);
						}
					}
				
					public static final class Tuple7<T1, T2, T3, T4, T5, T6, T7> implements Tuple, Serializable {
				
						private static final long serialVersionUID = 6913366542759921153L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
				
						public Tuple7(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
						}
				
						@Override
						public int arity() {
							return 7;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple7)) {
								return false;
							} else {
								final Tuple7<?, ?, ?, ?, ?, ?, ?> that = (Tuple7<?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7);
						}
					}
				
					public static final class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> implements Tuple, Serializable {
				
						private static final long serialVersionUID = 117641715065938183L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
						public final T8 _8;
				
						public Tuple8(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
							this._8 = t8;
						}
				
						@Override
						public int arity() {
							return 8;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple8)) {
								return false;
							} else {
								final Tuple8<?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple8<?, ?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7)
										&& Objects.equals(this._8, that._8);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8);
						}
					}
				
					public static final class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> implements Tuple, Serializable {
				
						private static final long serialVersionUID = -1578540921124551840L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
						public final T8 _8;
						public final T9 _9;
				
						public Tuple9(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
							this._8 = t8;
							this._9 = t9;
						}
				
						@Override
						public int arity() {
							return 9;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple9)) {
								return false;
							} else {
								final Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7)
										&& Objects.equals(this._8, that._8)
										&& Objects.equals(this._9, that._9);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9);
						}
					}
				
					public static final class Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> implements Tuple, Serializable {
				
						private static final long serialVersionUID = 7991284808329690986L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
						public final T8 _8;
						public final T9 _9;
						public final T10 _10;
				
						public Tuple10(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
							this._8 = t8;
							this._9 = t9;
							this._10 = t10;
						}
				
						@Override
						public int arity() {
							return 10;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple10)) {
								return false;
							} else {
								final Tuple10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7)
										&& Objects.equals(this._8, that._8)
										&& Objects.equals(this._9, that._9)
										&& Objects.equals(this._10, that._10);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);
						}
					}
				
					public static final class Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> implements Tuple, Serializable {
				
						private static final long serialVersionUID = 3493688489700741360L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
						public final T8 _8;
						public final T9 _9;
						public final T10 _10;
						public final T11 _11;
				
						public Tuple11(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
							this._8 = t8;
							this._9 = t9;
							this._10 = t10;
							this._11 = t11;
						}
				
						@Override
						public int arity() {
							return 11;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple11)) {
								return false;
							} else {
								final Tuple11<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple11<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7)
										&& Objects.equals(this._8, that._8)
										&& Objects.equals(this._9, that._9)
										&& Objects.equals(this._10, that._10)
										&& Objects.equals(this._11, that._11);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11);
						}
					}
				
					public static final class Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> implements Tuple, Serializable {
				
						private static final long serialVersionUID = -175212910367376967L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
						public final T8 _8;
						public final T9 _9;
						public final T10 _10;
						public final T11 _11;
						public final T12 _12;
				
						public Tuple12(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
							this._8 = t8;
							this._9 = t9;
							this._10 = t10;
							this._11 = t11;
							this._12 = t12;
						}
				
						@Override
						public int arity() {
							return 12;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple12)) {
								return false;
							} else {
								final Tuple12<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple12<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7)
										&& Objects.equals(this._8, that._8)
										&& Objects.equals(this._9, that._9)
										&& Objects.equals(this._10, that._10)
										&& Objects.equals(this._11, that._11)
										&& Objects.equals(this._12, that._12);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12);
						}
					}
				
					public static final class Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> implements Tuple,
							Serializable {
				
						private static final long serialVersionUID = 2027952127515234777L;
				
						public final T1 _1;
						public final T2 _2;
						public final T3 _3;
						public final T4 _4;
						public final T5 _5;
						public final T6 _6;
						public final T7 _7;
						public final T8 _8;
						public final T9 _9;
						public final T10 _10;
						public final T11 _11;
						public final T12 _12;
						public final T13 _13;
				
						public Tuple13(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13) {
							this._1 = t1;
							this._2 = t2;
							this._3 = t3;
							this._4 = t4;
							this._5 = t5;
							this._6 = t6;
							this._7 = t7;
							this._8 = t8;
							this._9 = t9;
							this._10 = t10;
							this._11 = t11;
							this._12 = t12;
							this._13 = t13;
						}
				
						@Override
						public int arity() {
							return 13;
						}
				
						@Override
						public boolean equals(Object o) {
							if (o == this) {
								return true;
							} else if (o == null || !(o instanceof Tuple13)) {
								return false;
							} else {
								final Tuple13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> that = (Tuple13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
								return Objects.equals(this._1, that._1)
										&& Objects.equals(this._2, that._2)
										&& Objects.equals(this._3, that._3)
										&& Objects.equals(this._4, that._4)
										&& Objects.equals(this._5, that._5)
										&& Objects.equals(this._6, that._6)
										&& Objects.equals(this._7, that._7)
										&& Objects.equals(this._8, that._8)
										&& Objects.equals(this._9, that._9)
										&& Objects.equals(this._10, that._10)
										&& Objects.equals(this._11, that._11)
										&& Objects.equals(this._12, that._12)
										&& Objects.equals(this._13, that._13);
							}
						}
				
						@Override
						public int hashCode() {
							return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);
						}
				
						@Override
						public String toString() {
							return Tuples.stringify(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);
						}
					}
				
					public static interface Tuple {
				
						int arity();
				
						@Override
						boolean equals(Object obj);
				
						@Override
						int hashCode();
				
						@Override
						String toString();
					}
				
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439594  [1.8][compiler] nested lambda type incorrectly inferred vs javac
public void test439594() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.function.Predicate;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				public class X {
					protected static interface IListEntry {
						public <T> T visitRecordsWithResult(Function<Stream<Record>,T> func);	\t
					}
					protected static final class ImmutableRecord {
						public ImmutableRecord(Record r) { }
					}
					protected static final class Record {}
					public List<ImmutableRecord> compilesWithEclipseAndJavac()\s
					{
						return visitEntriesWithResult( stream -> {
							return stream.map( entry -> {
								final List<ImmutableRecord> result1 = entry.visitRecordsWithResult( stream2 -> stream2
										.filter( somePredicate() )
										.map( ImmutableRecord::new )
										.collect( Collectors.toList() )
									);\t
								return result1;
							}).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );
						});	\t
					}	\t
					public List<ImmutableRecord> compilesWithJavacButNotEclipse1()\s
					{
						return visitEntriesWithResult( stream -> {
							return stream.map( entry -> {
								return entry.visitRecordsWithResult( stream2 -> stream2
										.filter( somePredicate() )
										.map( ImmutableRecord::new )
										.collect( Collectors.toList() )
									);\t
							}).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );
						});	\t
					}	\t
					public List<ImmutableRecord> compilesWithJavacButNotEclipse2()\s
					{
						return visitEntriesWithResult( stream -> {
							return stream.map( entry -> entry.visitRecordsWithResult( stream2 -> stream2
										.filter( somePredicate() )
										.map( ImmutableRecord::new )
										.collect( Collectors.toList() ) )
							).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );
						});	\t
					}\t
					public List<ImmutableRecord> compilesWithJavacButNotEclipse3()\s
					{
						return visitEntriesWithResult( stream -> stream.map( entry -> entry.visitRecordsWithResult( stream2 -> stream2
										.filter( somePredicate() )
										.map( ImmutableRecord::new )
										.collect( Collectors.toList() ) )
							)
							.flatMap( List::stream )
							.collect( Collectors.toCollection( ArrayList::new ) )
						);	\t
					}\t
					private static Predicate<Record> somePredicate() {
						return record -> true;
					}	\t
					private <T> T visitEntriesWithResult(Function<Stream<IListEntry>,T> func) {
						return func.apply( new ArrayList<IListEntry>().stream() );
					}
				}
				"""
	},
	"");
}
// reduced version for analysis (no need to run during normal tests)
public void _test439594_small() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.function.Predicate;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				public class X {
					protected static interface IListEntry {
						public <T> T visitRecordsWithResult(Function<Stream<Record>,T> func);	\t
					}
					protected static final class ImmutableRecord {
						public ImmutableRecord(Record r) { }
					}
					protected static final class Record {}
					public List<ImmutableRecord> compilesWithJavacButNotEclipse1()\s
					{
						return visitEntriesWithResult( stream -> {
							return stream.map( entry -> {
								return entry.visitRecordsWithResult( stream2 -> stream2
										.filter( somePredicate() )
										.map( ImmutableRecord::new )
										.collect( Collectors.toList() )
									);\t
							}).flatMap( List::stream ).collect( Collectors.toCollection( ArrayList::new ) );
						});	\t
					}	\t
					private static Predicate<Record> somePredicate() {
						return record -> true;
					}	\t
					private <T> T visitEntriesWithResult(Function<Stream<IListEntry>,T> func) {
						return func.apply( new ArrayList<IListEntry>().stream() );
					}
				}
				"""
	},
	"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433852, [1.8][compiler] Javac rejects type inference results that ECJ accepts
public void test433852() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Optional;
				import java.util.function.Function;
				import java.util.stream.Stream;
				public class X {
					public static void main(String[] args) {
						System.out.println(test(Stream.of(Stream.of("3"))));
						System.out.println(test2(Stream.of(Stream.of("1")).skip(1)));
						System.out.println(test31(Stream.of(Stream.of("2")).skip(1)));
					}
					static Optional<Stream<Object>> test(Stream<Stream<String>> s31) {
						return s31.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny();
					}
					static Object test2(Stream<Stream<String>> s3) {
						return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).flatMap(Function.identity()).findAny().orElse(
						  X.class);
					}
					static Stream<Object> test31(Stream<Stream<String>> s3) {
						return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny().orElse(Stream.of(new Object()));
					}
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 11)
			return s31.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny();
			       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Type mismatch: cannot convert from Optional<Stream<Integer>> to Optional<Stream<Object>>
		----------
		2. ERROR in X.java (at line 14)
			return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).flatMap(Function.identity()).findAny().orElse(
			                                                                                               ^^^^^^
		The method orElse(Integer) in the type Optional<Integer> is not applicable for the arguments (Class<X>)
		----------
		3. ERROR in X.java (at line 18)
			return s3.map(s2 -> s2.map(s1 -> Integer.parseInt(s1))).findAny().orElse(Stream.of(new Object()));
			                                                                  ^^^^^^
		The method orElse(Stream<Integer>) in the type Optional<Stream<Integer>> is not applicable for the arguments (Stream<Object>)
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442916,  [1.8][inference] Type Inference is broken for CompletableFuture then-methods
public void test442916() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import static java.util.concurrent.CompletableFuture.completedFuture;
				import java.util.Arrays;
				import java.util.concurrent.CompletableFuture;
				public class X {
				    public static CompletableFuture<Integer> cf(int value) {
						return completedFuture(value);
				    }
				    public static void main(String[] args) {
						cf(1).thenCompose((xInt) -> cf(2).thenApply((zInt) -> Arrays.asList(xInt, zInt)))
						.thenAccept((ints) -> {
							/* !!!! ints is incorrectly inferred to be an Object, but it is List<Integer> */
							System.out.println(ints.get(0) + ints.get(1)); // should print 3;
						});
					}
				}
				"""
		},
		"3");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442769, [1.8][compiler] Invalid type inference using Stream
public void test442769() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.nio.file.Path;
				import java.nio.file.Paths;
				import java.util.Arrays;
				import java.util.HashMap;
				import java.util.List;
				import java.util.Map;
				import java.util.Map.Entry;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				import java.io.Serializable;
				public class X {
					public static void main(String[] args) {
						Map<Object, Integer> allocated = new HashMap<>();
						   Arrays.asList("a", "b", "c", "d", "e") // List<String>
					          .stream() // Stream<String>
					          .map(Paths::get) // Stream<Path>
					          .flatMap(path -> allocated.keySet() // Set<Object>
					                                    .stream() // Stream<Object>
					                                    .map(group -> Pair.of(group, path) /*Pair<Object,Path>*/) // Stream<Pair<Object, Path>>
					          ) // Stream<Object> [FAIL]
					          .collect(Collectors.toList()) // List<Object>
					          .forEach(item -> System.out.println(item.getKey() + ": " + item.getValue())); // Consumer<? super Object>
					    // with explicit type
					    Arrays.asList("a", "b", "c", "d", "e") // List<String>
					          .stream() // Stream<String>
					          .map(Paths::get) // Stream<Path>
					          .flatMap(path -> allocated.keySet() // Set<Object>
					                                    .stream() // Stream<Object>
					                                    .<Pair<Object,Path>>map(group -> Pair.of(group, path) /*Pair<Object,Path>*/) // Stream<Pair<Object, Path>>
					          ) // Stream<Pair<Object, Path>>
					          .collect(Collectors.toList()) // List<Pair<Object, Path>>
					          .forEach(item -> System.out.println(item.getKey() + ": " + item.getValue())); // Consumer<? super Pair<Object, Path>>
					}
				}
				abstract class Pair<L, R> implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable {
				    public static <L, R> Pair<L, R> of(final L left, final R right) {
				        return null;
				    }
				    public final L getKey() {
				        return null;
				    }
				    public R getValue() {
				        return null;
				    }
				}
				"""
		},
		"");
}
// Test allocation expression boxing compatibility
public void testAllocationBoxingCompatibility() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X  {
				    static <T> int m(T o1, byte o2) {return 1;}      \s
				    static boolean call() {
				        return m(new Long(12l), new Byte((byte)1)) == 1;
				    }
				
				    public static void main(String argv[]) {
				       System.out.println(call());
				    }
				}
				""",
		}, "true");
}
// NPE while building JRE8: https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c113
public void test437444_c113() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					final List<String>[] ls = Util.cast(new List<>[] { null });
				\t
				}
				class Util {
					@SuppressWarnings("unchecked")
					public static <T> T cast(Object x) {
						return (T) x;
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				final List<String>[] ls = Util.cast(new List<>[] { null });
				                                        ^^^^
			Incorrect number of arguments for type List<E>; it cannot be parameterized with arguments <>
			----------
			""");
}
// Error while building JRE8: https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c113
public void test437444_c113a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					final List<String>[] ls = Util.cast(new List<?>[] { null });
				\t
				}
				class Util {
					@SuppressWarnings("unchecked")
					public static <T> T cast(Object x) {
						return (T) x;
					}
				}
				""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434394, [1.8] inference fails in some cases when conditional expression is involved
public void test434394() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				public class X {
				  public void bla() {
				    boolean b = Boolean.TRUE.booleanValue();
				    List<String> c1 = new ArrayList<>();
				    Collections.sort(c1, new Foo(new State<>((b ? new Val<>("AAAA") : new Val<>("BBBB"))))); // Cannot infer type arguments for State
				    Collections.sort(c1,new Foo(b ? new State<>(new Val<>("AAAA")) : new State<>(new Val<>("BBBB")))); // this is fine
				  }
				  static class Foo implements Comparator<String>{
					  public Foo(State<String> st) {
						  //
					  }
					@Override
					public int compare(String o1, String o2) {
						// TODO Auto-generated method stub
						return 0;
					}
				  }
					static class State<R> {
						State(Val<?> o) {
						}
					}
					static class Val<T> {
						Val(T t) {}
					}
				}
				""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=445725,  [1.8][inference] Type inference not occurring with lambda expression and constructor reference
public void test445725() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Arrays;
				import java.util.Collection;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				public class X {
				/**
				   * Takes a collection, applies a mapper to it, and then passes the result into the finishing
				   * function
				   */
				  public static <FROM, TO, RESULT> RESULT mapped(Collection<FROM> collection,
				                                                 Function<? super FROM, ? extends TO> mapper,
				                                                 Function<? super Collection<TO>, RESULT> finisher)
				  {
				    return mapped(collection.stream(), mapper, finisher);
				  }
				  /**
				   * Takes a stream, applies a mapper to it, and then passes the result into the finishing function
				   */
				  public static <FROM, TO, RESULT> RESULT mapped(Stream<FROM> stream,
				                                                 Function<? super FROM, ? extends TO> mapper,
				                                                 Function<? super Collection<TO>, RESULT> finisher)
				  {
				    return finisher.apply(stream.map(mapper).collect(Collectors.toList()));
				  }
				  public static void example()
				  {
				    mapped(Stream.of("1, 2, 3"), Integer::parseInt, ArrayList<Integer>::new);
				    mapped(Arrays.asList("1, 2, 3"), Integer::parseInt, ArrayList<Integer>::new);
				
				    mapped(Stream.of("1, 2, 3"), Integer::parseInt, IntCollection::new);
				    mapped(Arrays.asList("1, 2, 3"), Integer::parseInt, IntCollection::new);
				  }
				  public static class IntCollection extends ArrayList<Integer>
				  {
				    public IntCollection(Collection<Integer> numbers)
				    {
				      super(numbers);
				    }
				  }
				}
				""",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I {
					void bar(String t);
				}
				public class X<T> {
					X(String x) {}
					X(T x) {\s
						System.out.println("Here");
					}
					X(T x, String ...strings) {}
					public void one(X<I> c){}
					public void two() {
						one(new X<>((String s) -> { }));
					}
					public static void main(String[] args) {
						new X("").two();
					}
				}
				""",
		},
		"Here");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					 default <T> void foo (T... p) {}
				}
				abstract class A  {
					public abstract void foo(Object [] p);
				}
				abstract class B extends A implements I {
				}
				public abstract class X extends B implements I {
					public static void main(B b) {
						b.foo("hello", "world");
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				default <T> void foo (T... p) {}
				                           ^
			Type safety: Potential heap pollution via varargs parameter p
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633d() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					 default <T> void foo (T... p) {}
				}
				abstract class A  {
					public void foo(Object [] p) {}
				}
				abstract class B extends A implements I {
				}
				public abstract class X extends B implements I {
					public static void main(B b) {
						b.foo("hello", "world");
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				default <T> void foo (T... p) {}
				                           ^
			Type safety: Potential heap pollution via varargs parameter p
			----------
			2. WARNING in X.java (at line 7)
				abstract class B extends A implements I {
				               ^
			Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
			----------
			3. WARNING in X.java (at line 9)
				public abstract class X extends B implements I {
				                      ^
			Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
			----------
			4. ERROR in X.java (at line 11)
				b.foo("hello", "world");
				  ^^^
			The method foo(T...) of type I cannot be invoked as it is overridden by an inapplicable method
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633e() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					 default <T> void foo (T... p) {}
				}
				abstract class A  {
					public void foo(String [] p) {}
				}
				abstract class B extends A implements I {
				}
				public abstract class X extends B implements I {
					public static void main(B b) {
						b.foo("hello", "world");
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				default <T> void foo (T... p) {}
				                           ^
			Type safety: Potential heap pollution via varargs parameter p
			----------
			""");
}
// original:
public void testBug452788a() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.Function;
				
				interface Test<A> {
				
					<B3> Test<B3> create(B3 b);
				
					<B2> Test<B2> transform(Function<? extends A, Test<B2>> f);
				
					default <B1> Test<B1> wrap(Function<? super A, ? extends B1> f) {
						return transform(a -> create(f.apply(a)));
					}
				}
				"""
		});
}
// variants:
public void testBug452788b() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.function.Function;\n" +
			"\n" +
			"interface Test<A> {\n" +
			"\n" +
			"	<B3> Test<B3> create(B3 b);\n" +
			"\n" +
			"	<B2> Test<B2> transform(Function<? extends A, Test<B2>> f);\n" +
			"\n" +
			"	default <B1> Test<B1> wrap(Function<? super A, ? extends B1> f) {\n" +
			"		return transform((A a) -> create(f.apply(a)));\n" + // explicitly typed lambda
			"	}\n" +
			"	default <B> Function<? extends A, Test<B>> test1(Function<? super A, ? extends B> f) {\n" +
			"		return a -> create(f.apply(a));\n" + // remove outer invocation
			"	}\n" +
			"	default <B> Function<? extends A, Function<? extends A, Test<B>>> test2(Function<? super A, ? extends B> f) {\n" +
			"		return a1 -> a2 -> create(f.apply(a2));\n" + // outer lambda instead of outer invocation
			"	}\n" +
			"}\n"
		});
}
// diamond allocation instead of method (was OK before the patch).
public void testBug452788c() {
	runConformTest(
		new String[] {
			"Test2.java",
			"""
				import java.util.function.Function;
				
				
				public interface Test2<A> {
					<B2> Test2<B2> transform(Function<? extends A, Test2<B2>> f);
				
					default <B1> Test2<B1> wrap(Function<? super A, ? extends B1> f) {
						return transform(a -> new TestImpl<>(f.apply(a)));
					}
				}
				
				class TestImpl<A> implements Test2<A> {
				
					public TestImpl(A a) { }
				
					@Override
					public <B2> Test2<B2> transform(Function<? extends A, Test2<B2>> f) {
						return null;
					}\t
				}
				"""
		});
}
public void testBug457079() {
	runConformTest(
		new String[] {
			"Foo.java",
			"""
				import java.util.Collections;
				import java.util.Map;
				import java.util.Set;
				import java.util.function.Function;
				
				class Foo {
				    static <K, V> Map<K, V> foo(K value, Function<? super K, V> function) {
				        return null;
				    }
				
				    static void bar(Set<String> set) {
				        Map<String, Set<String>> map = foo("", e -> Collections.emptySet());
				    }
				}
				"""
		});
}
public void testBug458396() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"""
				import java.util.List;
				
				interface MyTickContext { }
				abstract class MyEntity {
					abstract void tick(MyTickContext ctx);
				}
				
				public class Main {
				
					protected static final MyTickContext tickContext = new MyTickContext() {
						public void method1(MyEntity e) {
							removeEntity( e );
						}
					};
				
					public static final class Game  {
						public void method2(List<MyEntity> ents) {
							ents.forEach( e -> e.tick(tickContext) );
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Main.java (at line 12)
				removeEntity( e );
				^^^^^^^^^^^^
			The method removeEntity(MyEntity) is undefined for the type new MyTickContext(){}
			----------
			""");
}
public void testBug455945() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.BiFunction;
				import java.util.function.Function;
				import java.util.function.Predicate;
				import java.util.stream.Stream;
				
				public class Test {
				
				    static <T> Tuple2<Seq<T>, Seq<T>> splitAtDoesntCompile(Stream<T> stream, long position) {
				        return seq(stream)
				            .zipWithIndex()
				            .partition(t -> t.v2 < position)
				            .map((v1, v2) -> tuple(
				                v1.map(t -> t.v1),
				                v2.map(t -> t.v1)
				            ));
				    }
				
				    static <T> Tuple2<Seq<T>, Seq<T>> splitAtCompiles(Stream<T> stream, long position) {
				        return seq(stream)
				            .zipWithIndex()
				            .partition(t -> t.v2 < position)
				            .map((v1, v2) -> Test.<Seq<T>, Seq<T>>tuple(
				                v1.map(t -> t.v1),
				                v2.map(t -> t.v1)
				            ));
				    }
				
				    static <T> Seq<T> seq(Stream<T> stream) {
				    	return null;
				    }
				
				    static <T1, T2> Tuple2<T1, T2> tuple(T1 v1, T2 v2) {
				    	return null;
				    }
				}
				
				interface I<T> {
					T get();
					<U> I<U> map(Function<T, U> f);
				}
				
				interface Seq<T> {
					Seq<Tuple2<T, Long>> zipWithIndex();
					Tuple2<Seq<T>, Seq<T>> partition(Predicate<? super T> predicate);
					<R> Seq<R> map(Function<? super T, ? extends R> mapper);
				}
				
				class Tuple2<T1, T2> {
					T1 v1;
					T2 v2;
				\t
					<R> R map(BiFunction<T1, T2, R> function) {
						return null;
					}
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=445231, [compiler] IllegalAccessError running Eclipse-compiled class
// This is a bug in Oracle JREs. Workaround in ECJ: https://bugs.eclipse.org/bugs/show_bug.cgi?id=466675
public void testBug445231() {
	runConformTest(
		true,
		new String[] {
		"com/n/Bug.java",
		"""
			package com.n;
			public class Bug {
			  public static void main(String[] args) {
			    try {
			      new Bug().go();
			      System.err.println("Ok");
			    } catch (IllegalAccessError e) {
			      System.err.println("Error");
			      e.printStackTrace();
			    }
			  }
			  public void go() {
			    Class<?> clazz = Buggered.Foo.class;
			    System.err.println("Here we go");
			    if (clazz.isAnonymousClass()) {
			      System.err.println("is anon");
			    } else {
			      System.err.println("not anon");
			    }
			  }
			}
			""",
		"com/g/Base.java",
		"package com.g;\n" +
		"class Base2{}\n" +
		"class Base {\n" +
		"	class A {}\n" +
		"	static class Builder<B extends Builder<B>> {\n" +
		"		public B setJobName() {\n" +
		"			return null;\n" +
		"		}\n" +
		"		public Base2 setJobName2(B b) {\n" +
		"			return null;\n" +
		"		}\n" +

		//  Wildcard
		"		public void foo(H<? super H<Base3.A>> h) {\n" +
		"			return;\n" +
		"  		}\n" +
		"		private class H<T> {}\n" +
		"	}\n" +
		"   static class Builder2 {\n" +
		"       public <B extends Builder<B>> B setJobName3() {\n" +
		"	        return null;\n" +
		"       }\n" +
		"   }\n" +
		"	static class R {}\n" +
		"	public static class Builder3<B extends R> {\n" +
		"		public B setJobName() {\n" +
		"			return null;\n" +
		"		}\n" +
		"	}\n" +
		"	public static class Builder4<B extends R> {\n" +
		"		public <Q extends R> Builder3<Q> setJobName() {\n" +
		"			return null;\n" +
		"		}\n" +
		"	}\n" +

		// Testing Parameters
		"	static class Builder5 {\n" +
		"		public <B extends Builder<B>> void  foo(B b) {}\n" +
		"	}\n" +

		"}\n" +

		"class Base3 {\n" +
		"	static class A{}\n" +
		"}\n"
		,

		"com/g/Child.java",
		"""
			package com.g;
			import com.g.Base.R;
			public final class Child {
			  public static class Builder<I> extends Base.Builder<Builder<I>> {
				  public void setDummyName(){}
			  }
			  public static class Builder2 extends Base.Builder2 {}
			  public static class Builder3<I> extends  Base.Builder3<R> {}
			  public static class Builder4<I> extends  Base.Builder4<R> {}
			  public static class Builder5 extends Base.Builder5 {}\s
			}
			""",
		"com/n/Buggered.java",
		"package com.n;\n" +
		"import com.g.Child;\n" +
		"class Z{}\n" +
		"public final class Buggered {\n" +
		"  public static final class Foo {}\n" +
		"  void unused() {\n" +
		"    Child.Builder<Void> c = new Child.Builder<Void>();\n" +
		"    c.setJobName();\n" +
		"    c.setJobName2(new Child.Builder<Void>());\n" +
		"    Child.Builder<Z> cb = new Child.Builder<Z>();\n" +
		"    cb.setJobName();\n" +
		"    cb.setJobName2(new Child.Builder<Z>());\n" +
		"    Child.Builder2 cb2 = new Child.Builder2();\n" +
		"    cb2.setJobName3();\n" +
		"    Child.Builder3<Void> cb3 = new Child.Builder3<Void>();\n" +
		"    cb3.setJobName();\n" +
		"    Child.Builder4<Void> cb4 = new Child.Builder4<Void>();\n" +
		"    cb4.setJobName();\n" +

		"    Child.Builder5 cb5 = new Child.Builder5();\n" +
		"    cb5.foo(null);\n" +

		//   Wildcard
		"	c.foo(null);\n" +
		"  }\n" +
		"}\n"
	},
	null, null,
	"""
		Here we go
		not anon
		Ok""", null);
}
public void testBug463728() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				import java.util.function.Function;
				
				
				class Color {
				\t
				}
				
				class TypeMapper<R> {
				
					public TypeMapper() {
					}
					public R orElse(R result) {
						return result;
					}
				}
				
				public class Main {
					Color A;
					Color B;
				
					public static <T, R> TypeMapper<R> mapType(Function<T, R> mapper) {
						return new TypeMapper<R>();
					}
				
					public Color getForeground(Object element) {
						return mapType(library -> {
								return (element != null ? A : B);
							}).orElse(null);
					}
				}
				"""
		});
}
public void testBug470942() {
	runConformTest(
		new String[] {
			"EclipeMarsLamdaIssueWontBuild.java",
			"""
				import java.util.function.Supplier;
				
				public class EclipeMarsLamdaIssueWontBuild {
					class MyClass {
						long getNumber() {
							return 0;
						}
					}
				
					private interface VoidSupplier {
						void perform();
					}
				
					long processTxContent() {
						return withLogging(() -> new MyClass().getNumber());
					}
				
					private static void withLogging(final VoidSupplier supplier) {
						// Do some logging
					}
				
					private static <T> T withLogging(final Supplier<T> supplier) {
						// Do some logging
						return null;
					}
				}
				"""
		});
}
public void testBug468999() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				interface ExceptionAction<T>
				{
				    T run() throws Exception;
				}
				
				interface Action<U> extends ExceptionAction<U>
				{
				    @Override
				    U run();
				}
				
				public class Main
				{
				    public static void main(String[] args) {
				        runAction(() -> {                              // ERROR HERE
				            return "";
				        });
				    }
				
				    static <V> void runAction(ExceptionAction<V> action) {
				        System.out.println("run with exceptions");
				    }
				
				    static <W> void runAction(Action<W> action) {
				        System.out.println("run without exceptions");
				    }
				}
				"""
		});
}
public void testBug470826() {
	runConformTest(
		new String[] {
			"EcjVsCollect.java",
			"""
				import java.util.ArrayList;
				import java.util.stream.Stream;
				
				public class EcjVsCollect {
				
				  public static void main(String[] args) {
				    try (final Stream<Record<String>> stream = getStream()) {
				      stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
				//      ArrayList<Record<String>> foo = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
				    }
				  }
				
				  private static <K> Stream<Record<K>> getStream() {
				    return Stream.empty();
				  }
				
				  private interface Record<K> {
				    K getKey();
				  }
				}
				"""
		});
}
public void testBug466487() {
	runConformTest(
		new String[] {
			"C.java",
			"""
				import java.util.*;
				import java.util.stream.*;
				import static java.util.Arrays.asList;
				
				public class C {
				  static final List<Integer> DIGITS = Collections.unmodifiableList(asList(0,1,2,3,4,5,6,7,8,9));
				   \s
				    Collection<String> flatMapSolutions(final boolean b) {
				      Collection<String> solutions =\s
				          DIGITS.stream().flatMap( s -> {
				               return b ? Stream.empty() : Stream.of("");
				          }) .collect(Collectors.toList());
				      return solutions;
				  }
				}
				"""
		});
}
public void testBug472426() {
	runConformTest(
		new String[] {
			"InferenceBug.java",
			"""
				import java.util.Collections;
				import java.util.List;
				import java.util.function.BiFunction;
				
				public class InferenceBug {
				
				    public static void main(String[] args) {
				
				        // compiles
				        List<String> l = Collections.singletonList("foo");
				        apply(Foo::foo, l);
				
				        // won't compile
				        apply(Foo::foo, Collections.singletonList("foo"));
				    }
				
				    static <T> void apply(BiFunction<Foo, T, Foo> fun, T value) {
				    }
				
				    static class Foo {
				        public Foo foo(List<String> i) {
				            return this;
				        }
				
				        public Foo foo(String... i) {
				            return this;
				        }
				    }
				
				}
				"""
		});
}
public void testBug469753() {
	runConformTest(
		new String[] {
			"LambdaBug.java",
			"""
				import java.util.AbstractMap;
				import java.util.Iterator;
				import java.util.Map.Entry;
				import java.util.function.Function;
				
				public class LambdaBug {
				
				    class Item {
				        String foo;
				    }
				
				    public void bug(String catalogKey, Iterator<Item> items) {
				        go(transform(items, i -> pair(i.foo, i)));
				    }
				
				    public static <K, V> Entry<K, V> pair(K key, V value) {
				        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
				    }
				
				    void go(Iterator<Entry<String, Item>> items) {
				    }
				
				    public static <F, T> Iterator<T> transform(Iterator<F> fromIterator, Function<? super F, ? extends T> function) {
				        return null;
				    }
				
				}
				"""
		});
}
public void testBug470958() {
	runConformTest(
		new String[] {
			"Bug470958.java",
			"""
				import java.time.*;
				import java.util.*;
				import java.util.concurrent.*;
				import static java.util.concurrent.CompletableFuture.*;
				import static java.util.stream.Collectors.*;
				
				class Hotel {}
				
				class Bug470958 {
				  public Map<String, CompletableFuture<List<Hotel>>> asyncLoadMany(List<String> codes, LocalDate begin, LocalDate end) {
				    return loadMany(codes, begin, end)
				    .entrySet()
				    .stream()
				    .collect(toMap(Map.Entry::getKey, entry -> completedFuture(entry.getValue())));
				  }
				
				  public Map<String, List<Hotel>> loadMany(List<String> codes, LocalDate begin, LocalDate end) {
				    return null;
				  }
				}
				"""
		});
}
public void testBug470542() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.Consumer;
				
				public class X {
					void test() {
						process(missing::new);
					}
				\t
					<T> void process(Consumer<T> c) { }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				process(missing::new);
				        ^^^^^^^
			missing cannot be resolved to a type
			----------
			""");
}
public void testBug471280_comment0() {
	runConformTest(
		new String[] {
			"Test0.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import java.util.concurrent.*;
				
				public class Test0 {
				  public CompletableFuture<List<String>> does_not_compile() throws Exception {
				    CompletableFuture<List<String>> firstAsync = new CompletableFuture<>();
				    firstAsync.complete(Collections.singletonList("test"));
				    // The following line gives error "Type mismatch: cannot convert from CompletableFuture<Object> to CompletableFuture<List<String>>"
				    return transform(firstAsync, first -> Collections.singletonList(first.get(0)));
				  }
				
				  public CompletableFuture<List<String>> does_compile() throws Exception {
				    CompletableFuture<List<String>> firstAsync = new CompletableFuture<>();
				    firstAsync.complete(Collections.singletonList("test"));
				    return transform(firstAsync, first -> {
				      return Collections.singletonList(first.get(0));
				    });
				  }
				
				  public <T, R> CompletableFuture<R> transform(CompletableFuture<T> future, Function<T, R> fun) throws Exception {
				    return future.thenApply(fun);
				  }
				}
				"""
		});
}
public void testBug471280_comment3() {
	runConformTest(
		new String[] {
			"Test3.java",
			"""
				import java.util.*;
				import java.util.stream.*;
				
				public class Test3 {
				    public <T> T generic(T value) {
				        return value;
				    }
				
				    public void mapExample(Map<String, String> input) {
				        // does not compile with ejc: Type mismatch: cannot convert from Map<Object,Object> to Map<String,String>
				        Map<String, String> mapped = input.entrySet()
				            .stream()
				            .collect(Collectors.toMap(e -> e.getKey(), e -> generic(e.getValue())));
				    }
				}
				"""
		});
}
public void testBug464496() {
	runConformTest(
		new String[] {
			"Value.java",
			"""
				public class Value<V> {
				    private final V value;
				    public Value(V value) {
				        this.value = value;
				    }
				    public V get() {
				        return value;
				    }
				    public static <V> V getValue(Value<V> value) {
				        return value.get();
				    }
				    public static void main(String[] args) {
				        Value<Integer> intValue = new Value<>(42);
				        long longPrimitive = getValue(intValue); // fails in 1.8 compiler\s
				        System.out.println(longPrimitive);
				    }
				}
				"""
		},
		"42");
}
public void testBug473657() {
	runConformTest(
		new String[] {
			"T2.java",
			"""
				interface I<T> {
				}
				
				@SuppressWarnings({"unchecked", "rawtypes"})
				abstract class T1<T> implements I<T> {
				    public I<T> t(I<? extends Number> l2) {
				        return T2.m((I) this, (I) l2);
				    }
				    public I<T> t(Number l2) {
				        return T2.m((I) this, (I) T2.t(l2));
				    }
				}
				
				public abstract class T2 {
				    public static <T> I<T> t(T t) {
				        return null;
				    }
				    public static <T extends Number> I<T> m(I<T> l1, I<? extends Number> l2) {
				        return null;
				    }
				    public static <T extends Number> I<T> m(T l1, Number l2) {
				        return null;
				    }
				}
				"""
		});
}
public void testBug478848() {
	runConformTest(
		new String[] {
			"InferenceBug.java",
			"""
				import java.util.*;
				public class InferenceBug {
				   \s
				    static class Wrapper<T> {
				        T value;
				        public T getValue() {
				            return null;
				        }
				    }
				   \s
				    static class C1 {
				        //an optional array of String wrappers
				        public Optional<? extends Wrapper<String>[]> optionalArrayOfStringWrappers() {
				            return Optional.empty();
				        }
				    }
				   \s
				    public static void main(String[] args) {
				        C1 c1 = new C1();
				        try {
				            for (Wrapper<String> attribute: c1.optionalArrayOfStringWrappers().get()) {
				                // error in previous line:
				                // Can only iterate over an array or an instance of java.lang.Iterable
				            }
				        } catch (NoSuchElementException nsee) {
				            System.out.print("No such element");
				        }
				    }
				}
				"""
		},
		"No such element");
}
public void testBug479167() {
	runConformTest(
		new String[] {
			"ToArray.java",
			"""
				import java.io.Serializable;
				interface ArrayFunction<E> {
					<S extends E> E[] apply(@SuppressWarnings("unchecked") S... es);
				}
				public class ToArray<E extends Cloneable & Serializable> implements ArrayFunction<E> {
					public final @SafeVarargs @Override <S extends E> E[] apply(S... es) {
						return es;
					}
				
					public static void main(String[] args) {
						ArrayFunction<String[]> toArray = new ToArray<>();
						String[][] array = toArray.apply(args);
						System.out.print(array.getClass().getName());
					}
				}
				"""
		},
		"[[Ljava.lang.String;");
}
public void testBug477751() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.Function;
				class Test {
					public static <T, U> U map(T value, Function<T, U> mapper) {
						if (value != null)
							return mapper.apply(value);
						return null;
					}
				
					String value;
				\t
					void test() {
						map(map(value, nnVal1 -> nnVal1.toLowerCase()),
								nnVal2 -> nnVal2.length());
					}
				
				}
				"""
		});
}
public void testBug482416() {
	runConformTest(
		new String[] {
			"CompilerRegression.java",
			"""
				import java.util.Comparator;
				import java.util.concurrent.Callable;
				
				public class CompilerRegression<T> {
					private ObjectProperty<Comparator<TreeItem<T>>> comparator = new ObjectProperty<Comparator<TreeItem<T>>>();
				
					void sample() {
						//Fails in Mars.1 succeeds in Mars.0
						{
							ObjectBinding<Comparator<TreeItem<T>>> b = Bindings.createObjectBinding(() -> {
								if (this.comparator.get() == null)
									return null;
								return (o1, o2) -> this.comparator.get().compare(o1, o2);
							}, this.comparator);
						}
				
						// Succeeds in both
						{
							ObjectBinding<Comparator<TreeItem<T>>> b = Bindings.createObjectBinding(() -> {
								if (this.comparator.get() == null)
									return null;
								Comparator<TreeItem<T>> cp = (o1, o2) -> this.comparator.get().compare(o1, o2);
								return cp;
							}, this.comparator);
						}
					}
				}
				
				class Bindings {
				    public static <X> ObjectBinding<X> createObjectBinding(final Callable<X> func, final Observable... dependencies) { return null; }
				}
				class ObjectBinding<U> { }
				class TreeItem<V> { }
				class ObjectProperty<W> implements Observable  {
					W get() { return null; }
				}
				interface Observable {}
				"""
		});
}
public void testBug483019() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import sub.B;
				import sub.Marker;
				
				public class Test {
				  public int test(B b) {
				    return (((B & Marker) b).getValue());
				  }
				  public static void main(String[] args) {
				    System.out.println(new Test().test(new B()));
				  }
				}""",
			"sub/A.java",
			"""
				package sub;
				class A {
				  public int getValue() {
				    return 1;
				  }
				}
				""",
			"sub/B.java",
			"package sub;\n" +
			"public class B extends A implements Marker{ }\n",
			"sub/Marker.java",
			"package sub;\n" +
			"public interface Marker{ }\n"
		},
		"1");
}
public void testBug483019a() {
	runConformTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone,
		new String[] {
			"Test.java",
			"""
				import sub.J;
				import sub.Marker;
				
				public class Test {
				  public int test(J j) {
				    return (((Marker & J) j).getValue());
				  }
				  public static void main(String[] args) {
				    System.out.println(new Test().test((J & Marker)() -> 0));
				  }
				}""",
			"sub/I.java",
			"""
				package sub;
				interface I {
				  int getValue();
				}
				""",
			"sub/J.java",
			"package sub;\n" +
			"public interface J extends I{ }\n",
			"sub/Marker.java",
			"package sub;\n" +
			"public interface Marker{ }\n"
		},
		"0");
}

public void testBug484448() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);

	runConformTest(
			new String[] {
				"test/Test.java",
				"""
					package test;
					
					public final class Test {
						/**
						 * @see #g(T, Class)
						 */
						public static <T> T f(T t, Class<T> c1) {
							return g(t, c1);
						}
					
						public static <U> U g(U u, Class<U> c2) {
							return u;
						}
					}
					"""
			},
			options);
}
public void testBug485593() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				
				public class Test {
				  void test() {
				    double[][] d = new double[][]{{1,2},{3,4},{5,6}};
				    double[][] e = Arrays.stream(d).map(double[]::clone).toArray(double[][]::new);
				  }
				}
				"""
		});
}
public void testBug483228a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface UnaryOp<T> { T apply(T arg); }
				interface IntegerToNumber { Number apply(Integer arg); }
				
				public class X {
				
				  <T> void m(UnaryOp<T> f) {}
				  void m(IntegerToNumber f) {}
				
				  void test() {
				    m((Integer i) -> i);
				  }\s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				m((Integer i) -> i);
				^
			The method m(UnaryOp<Integer>) is ambiguous for the type X
			----------
			""");
}
public void testBug449824a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					interface FI1<T> {
						public T get(X x, T n);
					}
					interface FI2 {
						public Integer get(X x, Integer t);
					}
					void m(FI1<Number> fi) { }
					void m(FI2 fi) { }
					Integer id(Number n) {
						return null;
					}
					void test() {
						m(X::id);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				m(X::id);
				^
			The method m(X.FI1<Number>) is ambiguous for the type X
			----------
			""");
}
public void testBug449824b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					interface FI1<T> {
						public T get(T... n);
					}
					interface FI2 {
						public Integer get(Integer... t);
					}
					void m(FI1<Number> fi) { }
					void m(FI2 fi) { }
					Integer id(Number[] n) {
						return null;
					}
					void test() {
						m(this::id);
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				public T get(T... n);
				                  ^
			Type safety: Potential heap pollution via varargs parameter n
			----------
			2. ERROR in X.java (at line 14)
				m(this::id);
				^
			The method m(X.FI1<Number>) is ambiguous for the type X
			----------
			""");
}
public void testBug487746_comment2() {
	runConformTest(
		new String[] {
			"Example.java",
			"""
				
				import java.time.Instant;
				import java.util.Comparator;
				import java.util.stream.Collectors;
				
				public class Example {
				   public void test1() {
				      // Returns Collector<Something,?,Something> - CORRECT
				      Collectors.collectingAndThen(
				            Collectors.<Something>toList(),
				            list -> list.stream().sorted(Comparator.comparing(Something::getTime)).limit(1).findAny().orElse(null)
				      );
				   }
				  \s
				   public void test2() {
				         Collectors.collectingAndThen(
				            Collectors.<Something>toList(),
				            list -> list.stream().collect(Collectors.groupingBy(Something::getSize,
				                     // Returns Collector<Something,?,Object> - INCORRECT!
				                     Collectors.collectingAndThen(
				                        Collectors.<Something>toList(),
				                        list2 -> list2.stream().sorted(Comparator.comparing(Something::getTime)).limit(1).findAny().orElse(null)
				                     )
				                  )));
				   }
				   private interface Something {
				      public int getSize();
				      public Instant getTime();
				  }
				}
				"""
		});
}
public void _testBug487746_comment9() { // FIXME: still reports an unexpected error
	runConformTest(
		new String[] {
			"Example.java",
			"""
				
				import java.time.Instant;
				import java.util.Comparator;
				import java.util.List;
				import java.util.stream.Collectors;
				
				public class Example {
					public void doesntCompile(List<Something> things) {
				   	things.stream()
				       	.filter(thing -> thing.getSize() > 100)
				       	.collect(Collectors.collectingAndThen(
				        	 	Collectors.<Something>toList(),
				         		list -> list.stream().collect(Collectors.groupingBy(Something::getSize,
				           	       	Collectors.collectingAndThen(
				               	      Collectors.<Something>toList(),
				                   	  list2 -> list2.stream().sorted(Comparator.comparing(Something::getTime)).limit(1).findAny().orElse(null)
				                  		)
				               ))))
				   		.forEach((size, thing) -> {
				       		System.out.println(thing.getSize());   // Compile error because Eclipse thinks 'thing' is Object
				   		});
					}
				   private interface Something {
				      public int getSize();
				      public Instant getTime();
				  }
				}
				"""
		});
}
public void testBug480075() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.stream.*;
				public class X {
					void test() {
						IntStream.of(42).mapToObj(i -> i > 42 ? "gt" : i < 42 ? "lt" : "42").findFirst();
				
						Stream.generate(Object::new).map(o -> o != null ? o : o == null ? o : o).findAny();
				
					}
				}
				"""
		});
}
public void testBug488649() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A<T> {}
				public class X {
					static <U> U get(A<U> a) { return null; }
					void test(A a) {
						get(a).missing();
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				void test(A a) {
				          ^
			A is a raw type. References to generic type A<T> should be parameterized
			----------
			2. WARNING in X.java (at line 5)
				get(a).missing();
				^^^^^^
			Type safety: Unchecked invocation get(A) of the generic method get(A<U>) of type X
			----------
			3. WARNING in X.java (at line 5)
				get(a).missing();
				    ^
			Type safety: The expression of type A needs unchecked conversion to conform to A<Object>
			----------
			4. ERROR in X.java (at line 5)
				get(a).missing();
				       ^^^^^^^
			The method missing() is undefined for the type Object
			----------
			""");
}
public void testBug488672() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				
				public class X {
					void foo(Manager manager) {
						HashSet<String> activeBindings = new HashSet<>(manager.getActiveBindingsDisregardingContextFlat());
					}
				}
				
				class Manager {
					Collection getActiveBindingsDisregardingContextFlat() {
						return null;
					}
				}
				"""
		});
}
public void testBug488795() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				interface Parameter {}
				interface Parameters<S extends Parameters<S, T>, T extends Parameter> extends Iterable<T> {
					S get();
				}
				public class X {
					void test(Parameters<?,?> parameters) {
						for(Parameter p : parameters.get())
							System.out.println(p);
					}
				}
				"""
		});
}
public void testBug489976() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import static java.util.stream.Collectors.*;
				import java.util.stream.Collectors;
				
				class Key {}
				class Value {}
				public class Test {
				  void test (List<Map<Key, Value>> maps) {
				    maps.stream().flatMap(s->s.entrySet().stream()).collect(
				        groupingBy(e -> e.getKey(),\s
				            mapping(e -> e.getValue(),  collectingAndThen(toList(),x->x))
				        )
				    );
				  }
				}
				"""
		});
}
public void testBug491934() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
				import java.util.Arrays;
				import java.util.HashSet;
				import java.util.Set;
				
				public class Main {
				
					public static void main(String[] args) {
						// gives compile error in Neon
						// was warning "unchecked" in Mars
						Set<String> genericSet = new HashSet<>(oldApiReturningUntypedSet());
					}
				
					@SuppressWarnings({ "rawtypes", "unchecked" })
					private static Set oldApiReturningUntypedSet() {
						HashSet set = new HashSet();
						set.add("one");
						return set;
					}
				
				}
				"""
		});
}
public void testBug491485() {
	runNegativeTest(
		new String[] {
			"Tester.java",
			"""
				interface SAM<X, Y, Z extends X3> {
					Z bar(X a, Y b);
				}
				interface I<T> {
				\t
				}
				class X3 {
				\t
				}
				public class Tester {
				
					X3 method(SAM<?, ?, ?> s) {
						return s.bar(null, null);
					}
				\t
					Object foo(Object a, Object b) {
						return null;
					}
					X3 junk() {
						return method((SAM<?,?,?> & I <?>) this::foo);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Tester.java (at line 20)
				return method((SAM<?,?,?> & I <?>) this::foo);
				                                   ^^^^^^^^^
			The type of foo(Object, Object) from the type Tester is Object, this is incompatible with the descriptor's return type: X3
			----------
			""");
}

public void testBug485057() {
	runNegativeTest(
		new String[] {
			"Task.java",
			"""
				public class Task {
				
					public static void main(String[] args) {
						foo(rt -> true); // PROBLEM HERE
					}
				
					public static <T extends java.io.Serializable> Task foo(T serialiable) {
						return null;
					}
				
					public static Task foo(java.util.function.Predicate<?> predicate) {
						return null;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Task.java (at line 4)
				foo(rt -> true); // PROBLEM HERE
				^^^
			The method foo(Serializable) is ambiguous for the type Task
			----------
			2. ERROR in Task.java (at line 4)
				foo(rt -> true); // PROBLEM HERE
				    ^^^^^^^^^^
			The target type of this expression must be a functional interface
			----------
			""");
}

public void testBug485373() {
	runNegativeTest(
		new String[] {
			"TestGenericsFunctional.java",
			"""
				import java.util.Collection;
				import java.util.function.Consumer;
				
				public class TestGenericsFunctional {
				
					public static void doStuff(String str, Consumer<String> consumer) {
						consumer.accept(str);
					}
				\t
					public static <C extends Collection<String>> C doStuff(String str, C collection) {
						doStuff(str, st -> collection.add(st));
						return collection;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in TestGenericsFunctional.java (at line 11)
				doStuff(str, st -> collection.add(st));
				^^^^^^^
			The method doStuff(String, Consumer<String>) is ambiguous for the type TestGenericsFunctional
			----------
			""");
}

public void testBug487563() {
	runNegativeTest(
		new String[] {
			"Java8TypeInferenceProblem.java",
			"""
				
				import java.util.Iterator;
				import java.util.List;
				
				public class Java8TypeInferenceProblem {
				
					public ValueObjectImpl myTestMethod() {
						return copyToValueObject(loadBusinessObject(), ValueObjectImpl.class);
					}
				
					public <T extends ValueObject> T copyToValueObject(BusinessObject param, Class<T> voClass) {
						return null;
					}
				
					public <T extends ValueObject> List<T> copyToValueObject(Iterator<BusinessObject> params, Class<T> voClass) {
						return null;
					}
				
					public <T extends BusinessObject> T loadBusinessObject() {
						return null;
					}
				
					private interface BusinessObject { }
				
					private interface ValueObject { }
				
					private class ValueObjectImpl implements ValueObject { }
				
				}
				"""
		},
		"""
			----------
			1. ERROR in Java8TypeInferenceProblem.java (at line 8)
				return copyToValueObject(loadBusinessObject(), ValueObjectImpl.class);
				       ^^^^^^^^^^^^^^^^^
			The method copyToValueObject(Java8TypeInferenceProblem.BusinessObject, Class<Java8TypeInferenceProblem.ValueObjectImpl>) is ambiguous for the type Java8TypeInferenceProblem
			----------
			""");
}
public void testBug492939a() {
	runConformTest(
		new String[] {
			"EclipseInference.java",
			"import java.lang.reflect.Type;\n" +
			"import java.sql.ResultSet;\n" +
			"import java.sql.SQLException;\n" +
			"import java.util.List;\n" +
			"import java.util.Optional;\n" +
			"import java.util.concurrent.ConcurrentHashMap;\n" +
			"import java.util.concurrent.CopyOnWriteArrayList;\n" +
			"import java.util.function.Supplier;\n" +
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class EclipseInference {\n" +
			"\n" +
			"    private final List<RowMapperFactory> rowFactories = new CopyOnWriteArrayList<>();\n" +
			"    private final ConcurrentHashMap<Type, RowMapper<?>> rowCache = new ConcurrentHashMap<>();\n" +
			"\n" +
			"    @SuppressWarnings(\"unchecked\")\n" +
			"    public Optional<RowMapper<?>> findRowMapperFor(Type type) {\n" +
			"        return Optional.ofNullable(rowCache.computeIfAbsent(type, t ->\n" +
			"                findFirstPresent(\n" +
			"                        () -> rowFactories.stream()\n" +
			"                                .flatMap(factory -> toStream(factory.build(t)))\n" +
			"                                .findFirst(),\n" +
			"                        () -> findColumnMapperFor(t)\n" +
			"                                .map(SingleColumnMapper::new))\n" + // HERE: ReferenceExpression had a bug
			"                        .orElse(null)));\n" +
			"    }\n" +
			"\n" +
			"    private Optional<ColumnMapper<?>> findColumnMapperFor(Type t) {\n" +
			"        return Optional.empty();\n" +
			"    }\n" +
			"\n" +
			"    @SafeVarargs\n" +
			"    static <T> Optional<T> findFirstPresent(Supplier<Optional<T>>... suppliers) {\n" +
			"        return Stream.of(suppliers)\n" +
			"                .flatMap(supplier -> toStream(supplier.get()))\n" +
			"                .findFirst();\n" +
			"    }\n" +
			"    static <T> Stream<T> toStream(Optional<T> optional) {\n" +
			"        return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class SingleColumnMapper<T> implements RowMapper<T> {\n" +
			"    SingleColumnMapper(ColumnMapper<T> mapper) {\n" +
			"    }\n" +
			"    @Override\n" +
			"    public T map(ResultSet r) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface RowMapper<T>\n" +
			"{\n" +
			"    T map(ResultSet r);\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface ColumnMapper<T>\n" +
			"{\n" +
			"    T map(ResultSet r, int columnNumber) throws SQLException;\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface RowMapperFactory\n" +
			"{\n" +
			"    Optional<RowMapper<?>> build(Type type);\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface ColumnMapperFactory\n" +
			"{\n" +
			"    Optional<ColumnMapper<?>> build(Type type);\n" +
			"}\n"
		});
}
public void testBug492939b() {
	runConformTest(
		new String[] {
			"EclipseInference.java",
			"import java.lang.reflect.Type;\n" +
			"import java.sql.ResultSet;\n" +
			"import java.sql.SQLException;\n" +
			"import java.util.List;\n" +
			"import java.util.Optional;\n" +
			"import java.util.concurrent.ConcurrentHashMap;\n" +
			"import java.util.concurrent.CopyOnWriteArrayList;\n" +
			"import java.util.function.Supplier;\n" +
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class EclipseInference {\n" +
			"\n" +
			"    private final List<RowMapperFactory> rowFactories = new CopyOnWriteArrayList<>();\n" +
			"    private final ConcurrentHashMap<Type, RowMapper<?>> rowCache = new ConcurrentHashMap<>();\n" +
			"\n" +
			"    @SuppressWarnings(\"unchecked\")\n" +
			"    public Optional<RowMapper<?>> findRowMapperFor(Type type) {\n" +
			"        return Optional.ofNullable(rowCache.computeIfAbsent(type, t ->\n" +
			"                findFirstPresent(\n" +
			"                        () -> rowFactories.stream()\n" +
			"                                .flatMap(factory -> toStream(factory.build(t)))\n" +
			"                                .findFirst(),\n" +
			"                        () -> findColumnMapperFor(t)\n" +
			"                                .map(c -> new SingleColumnMapper<>(c)))\n" + // HERE: LambdaExpression already worked
			"                        .orElse(null)));\n" +
			"    }\n" +
			"\n" +
			"    private Optional<ColumnMapper<?>> findColumnMapperFor(Type t) {\n" +
			"        return Optional.empty();\n" +
			"    }\n" +
			"\n" +
			"    @SafeVarargs\n" +
			"    static <T> Optional<T> findFirstPresent(Supplier<Optional<T>>... suppliers) {\n" +
			"        return Stream.of(suppliers)\n" +
			"                .flatMap(supplier -> toStream(supplier.get()))\n" +
			"                .findFirst();\n" +
			"    }\n" +
			"    static <T> Stream<T> toStream(Optional<T> optional) {\n" +
			"        return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class SingleColumnMapper<T> implements RowMapper<T> {\n" +
			"    SingleColumnMapper(ColumnMapper<T> mapper) {\n" +
			"    }\n" +
			"    @Override\n" +
			"    public T map(ResultSet r) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface RowMapper<T>\n" +
			"{\n" +
			"    T map(ResultSet r);\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface ColumnMapper<T>\n" +
			"{\n" +
			"    T map(ResultSet r, int columnNumber) throws SQLException;\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface RowMapperFactory\n" +
			"{\n" +
			"    Optional<RowMapper<?>> build(Type type);\n" +
			"}\n" +
			"\n" +
			"@FunctionalInterface\n" +
			"interface ColumnMapperFactory\n" +
			"{\n" +
			"    Optional<ColumnMapper<?>> build(Type type);\n" +
			"}\n"
		});
}
public void testBug496942() {
	runConformTest(
		new String[] {
			"ProductManager.java",
			"""
				import java.util.Set;
				import java.util.concurrent.Callable;
				import java.util.function.Function;
				import java.util.stream.Stream;
				
				class Product { }
				class ItineraryDTO { }
				class Result<K, V> {
				    public static <T, U> Function<T, ListenableFuture<Result<T, U>>>\s
				    		asyncCall(Function<T, ListenableFuture<U>> asyncMethod)
				    {
				    	return null;
				    }
				}
				interface ListeningExecutorService {
					<T> ListenableFuture<T> submit(Callable<T> c);
					ListenableFuture<?> submit(Runnable r);
				}
				interface ListenableFuture<T> {}
				
				public class ProductManager {
					public Stream<ListenableFuture<Result<Product, ItineraryDTO>>>\s
							test(ListeningExecutorService executor, Set<Product> productsSet)
					{
				         return productsSet.stream().map(Result.asyncCall(product ->
				                    executor.submit(() -> new ItineraryDTO()
				                )));
					}
				}
				"""
		});
}
public void testBug496574() {
	runConformTest(
		new String[] {
			"EclipseNeonBug.java",
			"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Map;
				import java.util.Optional;
				import java.util.stream.Collectors;
				
				public class EclipseNeonBug {
				
					public static void main(String[] args) {
						List<KeyValueObj> keyValObjs = new ArrayList<>();
						Map<String, String> mses = Optional.ofNullable(keyValObjs)
				                .filter(ms -> !ms.isEmpty())
				                .map(ms -> ms.stream().collect(Collectors.toMap(
				                    metafield -> metafield.getKey(),
				                    metafield -> metafield.getValue())))
				                .orElseGet(() -> Collections.emptyMap());
					}
				
					public static class KeyValueObj {
						private String key;
						private String value;
				
					    public String getKey() {
					        return key;
					    }
				
					    public void setKey(String key) {
					        this.key = key;
					    }
				
					    public String getValue() {
					        return value;
					    }
				
					    public void setValue(String value) {
					        this.value = value;
					    }
					}
				}
				"""
		});
}
public void testBug496574_small() {
	runConformTest(
		new String[] {
			"Small.java",
			"""
				import java.util.*;
				import java.util.stream.*;
				
				interface KeyValueObj {
				    String getKey();
				    String getValue();
				}
				
				public class Small {
				
					public void test(Optional<List<KeyValueObj>> optList) {
						Optional<Map<String, String>> mses = optList
				                .map(ms -> ms.stream().collect(Collectors.toMap(
				                    metafield -> metafield.getKey(),
				                    metafield -> metafield.getValue())));
					}
				}
				"""
		});
}
public void testBug496579() {
	runConformTest(
		new String[] {
			"EclipseNeonBug2.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				import java.util.stream.Collectors;
				
				public class EclipseNeonBug2 {
				
					public static void main(String[] args) {
						Map<String, Map<String, Object>> stuff = new HashMap<>();
						Map<String, Map<String, Integer>> result = stuff.entrySet().stream()
							.collect(Collectors.toMap(
									k -> k.getKey(),\s
									o -> {
										Map<String, Object> child = o.getValue();
										return child.entrySet().stream().collect(Collectors.toMap(
												k -> k.getKey(),\s
												v -> Integer.parseInt(v.getValue().toString())));
									}));
					}
				\t
				}
				"""
		});
}
public void testBug496761() {
	runConformTest(
		new String[] {
			"RepoCase.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				import java.util.Map.Entry;
				import java.util.Optional;
				import java.util.function.Supplier;
				import java.util.stream.Collectors;
				
				public class RepoCase {
					private Map<String, Supplier<?>> dependencyMap = new HashMap<>();
				\t
					void compilerNPE() {
				// Leads to NPE in compiler
						Map<String, Object> map = Optional.ofNullable(this.dependencyMap)
								.map(m -> m.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> (Object) e.getValue().get())))
								.orElse(new HashMap<>());
					\t
				// Compiler error (might be the real cause for the above NPE)	\t
						Optional<Map<String, Object>> o = Optional.ofNullable(this.dependencyMap)
							.map(m -> m.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> (Object) e.getValue().get())));
					}
				}
				"""
		});
}
public void testBug496624() {
	runConformTest(
		new String[] {
			"JDTNPETest.java",
			"""
				import java.util.*;
				import java.util.stream.Collectors;
				
				interface HttpSession{
				  Enumeration<String> getAttributeNames();
				  Object getAttribute(String name);
				}
				public class JDTNPETest {
				   \s
				    public static void main(String[] args){
				        Map<String, Object> sessionAttributes = Optional.<HttpSession>ofNullable(null)
				            .map(s -> Collections.list(s.getAttributeNames()).stream()
				                .collect(Collectors.toMap(name -> name, name -> s.getAttribute(name))))
				            .orElse(null);
				    }
				
				}
				"""
		});
}
public void testBug497193() {
	runConformTest(
		new String[] {
			"EclipseBug.java",
			"""
				import java.util.function.Function;
				
				public class EclipseBug {
				    public static class Class1<K, V > {
				        public Class1( Function<K, V > arg ) {}
				    }
				
				    public static <T, R> R method1( T object, Function<T, R > function ) {
				        return null;
				    }
				
				    public static class Class2 {
				        public static Class2 method1( String arg ) {
				            return null;
				        }
				
				        String method2() {
				            return null;
				        }
				    }
				
				    private final Class1<String, String > member = new Class1<>( arg -> method1( Class2.method1( arg ), class2 -> class2.method2() ) );
				}
				"""
		});
}
public void testBug496578() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.function.BinaryOperator;
				import java.util.function.Function;
				
				public class Test {
					private Long[] v, v_small;
					public Long method(String[] args) {
						ExecPushFactory alg = new ExecPushFactory();
				        Long value = Id.prj(
				                alg.reduce(0L, Long::sum,
				                        alg.flatMap(x ->
				                                        alg.map(y -> x * y,
				                                                alg.source(v_small)),
				                                alg.source(v)))).value;
				        return value;
					}
				}
				class ExecPushFactory {
					public <T, R> App<Id.t, R> flatMap(Function<T, App<Id.t, R>> mapper, App<Id.t, T> app) {
				        return null;
				    }
					public <T> App<Id.t, T> source(T[] array) {
				        return null;
				    }
					public <T, R> App<Id.t, R> map(Function<T, R> mapper, App<Id.t, T> app) {
				        return null;
				    }
				    public <T> App<Id.t, T> reduce(T identity, BinaryOperator<T> accumulator, App<Id.t, T> app) {
				    	return null;
				    }
				}
				class Id<T> implements App<Id.t, T>{
				   public T value;
					public static class t {
					\t
					}
					public static <A> Id<A> prj(App<Id.t, A> app) {
				        return (Id<A>) app;
				    }
				}
				interface App<C, T> {
				\t
				}
				"""
		});
}
public void testBug496675() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    public class B<X, Y> {
				        public class C {}
				    }
				    public class D<X> extends B<String, X> {}
				\t
				    /* This fails with an internal ArrayIndexOutOfBoundsException in\s
				     * ParameterizedTypeBinding.boundCheck. */
				    public class E<X extends D<?>.C> {}
				}
				"""
		});
}
public void testBug496675_comment4() {
	runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
					    public class B<X, Y> {
					        public class C {}
					    }
					    public class D<X extends Number> extends B<String, X> {}
					\t
					    /* This fails with an internal ArrayIndexOutOfBoundsException in\s
					     * ParameterizedTypeBinding.boundCheck. */
					    public class E<X extends D<String>.C> {}
					}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 9)
					public class E<X extends D<String>.C> {}
					                           ^^^^^^
				Bound mismatch: The type String is not a valid substitute for the bounded parameter <X extends Number> of the type Test.D<X>
				----------
				""");
}
public void testBug496675_problem() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    <X extends wrong.D<?>.C> void m() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 2)
				<X extends wrong.D<?>.C> void m() {}
				           ^^^^^
			wrong cannot be resolved to a type
			----------
			""");
}
public void testBug496886() {
	runConformTest(
		new String[] {
			"Outer.java",
			"""
				public interface Outer<E> {
				  public interface Inner<E> {
				  }
				}
				""",
			"SubInterface.java",
			"public interface SubInterface extends Outer<String> {}\n"
		});
	runConformTest(
			new String[] {
				"ProblemClass.java",
				"class ProblemClass implements SubInterface.Inner<String> {}\n"
			},
			"",
			null,
			false, // don't flush
			null);
}
public void testBug497603() {
	runConformTest(
		new String[] {
			"InferBug.java",
			"""
				import java.util.Iterator;
				import java.util.Map.Entry;
				import java.util.function.BiPredicate;
				import java.util.function.Function;
				
				public class InferBug {
				    public static void main(String[] args) {
				        Iterator<Iterator<Entry<String, String>>> x = null;
				        Iterator<Iterator<String>> p = foo(x, i -> foo(i, Entry::getValue));
				    }
				    static <F, T> Iterator<T> foo(Iterator<F> a, Function<F, T> b) {
				        return null;
				    }
				}
				"""
		});
}
public void testBug498113a() {
	runConformTest(
		new String[] {
			"NPETest.java",
			"import java.util.*;\n" +
			"public class NPETest {\n" +
			"\n" +
			"    public void test(\n" +
			"            final Set<String> set,\n" +
			"            final List<Dummy<String>> dummies) {\n" +
			"        set.stream()\n" +
			"            .map(Dummy::new)\n" + // true varargs invocation
			"            .forEach(dummies::add);\n" +
			"    }\n" +
			"    \n" +
			"    class Dummy<T> {\n" +
			"        \n" +
			"        public Dummy(T... args) {\n" +
			"            \n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug498113b() {
	runConformTest(
		new String[] {
			"NPETest.java",
			"import java.util.*;\n" +
			"public class NPETest {\n" +
			"\n" +
			"    public void test(\n" +
			"            final Set<String[]> set,\n" +
			"            final List<Dummy<String>> dummies) {\n" +
			"        set.stream()\n" +
			"            .map(Dummy::new)\n" + // pass String[] for a strict invocation
			"            .forEach(dummies::add);\n" +
			"    }\n" +
			"    \n" +
			"    class Dummy<T> {\n" +
			"        \n" +
			"        public Dummy(T... args) {\n" +
			"            \n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug498362_comment0() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
					static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
					private byte[] stream;
					void test() {
						this.stream = Optional.ofNullable(stream)
				            .map(byte[]::clone)
				            .orElse(EMPTY_BYTE_ARRAY);\
					}
				}
				"""
		});
}
public void testBug498362_comment5() {
	runConformTest(
		new String[] {
			"CloneVerifyError.java",
			"""
				public class CloneVerifyError {
				    public interface PublicCloneable<T> extends Cloneable {
				        public T clone();
				    }
				
				    public static <T> T[] clone0(T[] input) {
				        return input == null ? null : input.clone();
				    }
				
				    public static <T extends PublicCloneable<T>> T clone0(T input) {
				        if (input == null) {
				            return null;
				        } else {
				            return input.clone();
				        }
				    }
				
				    public static void main(String[] args) {
				        Object[] array = null;
				        clone0(array);
				    }
				}
				"""
		});
}
public void testBug470667() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.math.BigInteger;
				import java.util.function.Function;
				public class Test {
						protected <T> T m(Class<T> c, String s, Function<String, T> f) {
							return f.apply(s);
						}
						protected <T> T m(Class<T> c, BigInteger i, Function<BigInteger, T> f) {
							return f.apply(i);
						}
						protected <T> Data<T> createData() {
							return new Data<T>() {
							};
						}
						private <T> Data<T> doA(BigInteger i) {
							String str = "titi ";
							@SuppressWarnings("unchecked")
							Data<T> r = m(Data.class, "toto ",
								(x) -> m(Data.class, str, (y) -> m(Data.class, BigInteger.ZERO, (z) -> createData(i, x, y, z))));
							return r;
						}
				}
				interface Data<T> { }
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 18)
				(x) -> m(Data.class, str, (y) -> m(Data.class, BigInteger.ZERO, (z) -> createData(i, x, y, z))));
				                                                                       ^^^^^^^^^^
			The method createData() in the type Test is not applicable for the arguments (BigInteger, String, String, BigInteger)
			----------
			""");
}
public void testBug497239() {
	runConformTest(
		new String[] {
			"FunctionUtils.java",
			"import java.util.stream.Collector;\n" +
			"import java.util.stream.Collectors;\n" +
			"\n" +
			"public class FunctionUtils<InputElement, ResultElement> {\n" +
			"	public static <T> T[] concat(T[] array1, T[] array2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	public static <T> T[] concat(T[][] arrays) {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	public Collector<ResultElement[], ?, ResultElement[]> on(InputElement[] inputElements) {\n" +
			"		return Collectors.collectingAndThen(Collectors.reducing(FunctionUtils::concat), r -> r.get());\n" +
			"	}\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug472851() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				
				public class Test {
				    public static void main(String... arg) {
				    List<Integer> l1=Arrays.asList(0, 1, 2);
				    List<String>  l2=Arrays.asList("0", "1", "2");
				    a(Arrays.asList(l1, l2));
				}
				static final void a(List<? extends List<?>> type) {
				    test(type);
				}
				static final <Y,L extends List<Y>> void test(List<L> type) {
				    L l1=type.get(0), l2=type.get(1);
				    l2.set(0, l1.get(0));
				}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 10)
				test(type);
				^^^^
			The method test(List<L>) in the type Test is not applicable for the arguments (List<capture#1-of ? extends List<?>>)
			----------
			""");
}
public void testBug502350() {
	runNegativeTest(
		new String[] {
			"makeCompilerFreeze/EclipseJava8Bug.java",
			"package makeCompilerFreeze;\n" +
			"\n" +
			"interface Comparable<E> {} \n" +
			"\n" +
			"interface Comparator<A> {\n" +
			"  public static <B extends Comparable<B>> Comparator<B> naturalOrder() {\n" +
			"    return null;\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"\n" +
			"class Stuff {\n" +
			"  public static <T, S extends T> Object func(Comparator<T> comparator) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"public class EclipseJava8Bug {\n" +
			"  static final Object BORKED =\n" +
			"      Stuff.func(Comparator.naturalOrder());\n" +
			"}\n" +
			"\n" +
			"",
		},
		"""
			----------
			1. ERROR in makeCompilerFreeze\\EclipseJava8Bug.java (at line 20)
				Stuff.func(Comparator.naturalOrder());
				      ^^^^
			The method func(Comparator<T>) in the type Stuff is not applicable for the arguments (Comparator<Comparable<Comparable<B>>>)
			----------
			"""
	);
}
public void testBug499351() {
	runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.util.HashMap;
				import java.util.List;
				import java.util.Map;
				import java.util.Set;
				import java.util.stream.Collectors;
				
				public class Bug {
				    private static final Validator VALIDATOR = new Validator();
				
				    public static void main(String[] args) {
				        Map<String, List<Promotion>> promotions = new HashMap<>();
				
				        Set<ConstraintViolation> cvs = promotions.entrySet().stream()
				            .flatMap(e -> e.getValue().stream()
				                .flatMap(promotion -> VALIDATOR.validate(promotion).stream())
				            )
				            .collect(Collectors.toSet());
				
				        Set<ExtendedConstraintViolation> ecvs = promotions.entrySet().stream()
				                .flatMap(e -> e.getValue().stream()
				                    .map(constraintViolation -> new ExtendedConstraintViolation("", null))
				                )
				                .collect(Collectors.toSet());
				
				        Set<ExtendedConstraintViolation> ecvs2 = promotions.entrySet().stream()
				                .flatMap(e -> e.getValue().stream())
				                .flatMap(promotion -> VALIDATOR.validate(promotion).stream())
				                .map(constraintViolation -> new ExtendedConstraintViolation("promotions/2", constraintViolation))
				                .collect(Collectors.toSet());
				
				        // Below does not compile with 4.7M1, but worked fine in 4.5 (also compiles fine with Oracle/JDK8)
				        //
				        // --> Type mismatch: cannot convert from Set<Object> to Set<Bug.ExtendedConstraintViolation>
				        //
				        Set<ExtendedConstraintViolation> ecvs3 = promotions.entrySet().stream()
				                .flatMap(e -> e.getValue().stream()
				                    .flatMap(promotion -> VALIDATOR.validate(promotion).stream()
				                        .map(constraintViolation -> new ExtendedConstraintViolation("promotions/" + e.getKey(), constraintViolation))
				                    )
				                )
				                .collect(Collectors.toSet());
				    }
				
				    private static class ExtendedConstraintViolation {
				        public ExtendedConstraintViolation(String key, ConstraintViolation cv) {
				        }
				    }
				
				    private static class ConstraintViolation {
				    }
				
				    private static class Promotion {
				    }
				
				    private static class Validator {
				        public Set<ConstraintViolation> validate(Object o) {
				            return null;
				        }
				    }
				}
				"""
		});
}
// reduced version for analysis (no need to run during normal tests)
public void _testBug499351_small() {
	runConformTest(
		new String[] {
			"Small.java",
			"""
				import java.util.*;
				import java.util.stream.Collectors;
				
				public class Small {
				
				    public static void test(Map<String, List<Promotion>> promotions, Validator validator) {
				
				        Set<ExtendedConstraintViolation> ecvs = promotions.entrySet().stream()
				                .flatMap(e -> e.getValue().stream()
				                    .flatMap(promotion -> validator.validate(promotion).stream()
				                        .map(constraintViolation -> new ExtendedConstraintViolation("promotions/" + e.getKey(), constraintViolation))
				                    )
				                )
				                .collect(Collectors.toSet());
				    }
				
				}
				class ExtendedConstraintViolation {
				    public ExtendedConstraintViolation(String key, ConstraintViolation cv) { }
				}
				
				class ConstraintViolation { }
				class Promotion { }
				class Validator {
				    public Set<ConstraintViolation> validate(Object o) { return null; }
				}
				"""
		});
}
public void test499351_extra1() {
	runConformTest(
		new String[] {
			"Example.java",
			"""
				import java.util.function.Function;
				
				public class Example {
				   static <T> T id(T t) { return t; }
				   static <T,X> T f1 (X x) { return null; }
				  \s
				   String test() {
					   return f3(y -> y.f2(Example::f1, id(y)));
				   }
				   <U,V> V f2(Function<U, V> f, U u) {return f.apply(null);}
				   <R> R f3(Function<Example,R> f) { return null; }
				}
				"""
		});
}
public void test499351_extra2() {
	runConformTest(
		new String[] {
			"BadInferenceMars451.java",
			"""
				import java.util.*;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				public class BadInferenceMars451 {
					public static Map<Object, List<X>> BadInferenceMars451Casus1() {
						List<X> stuff = new ArrayList<>();
						return stuff.stream().collect(Collectors.toMap(Function.identity(), t -> Arrays.asList(t), BadInferenceMars451::sum));
					}
					public static <T> List<T> sum(List<T> l1, List<T> l2) {
						return null;
					}
					public static class X {
					}
				}"""
		});
}
public void testBug501949() {
	runConformTest(
		new String[] {
			"DefaultClientRequestsV2.java",
			"""
				import java.io.IOException;
				import java.util.List;
				import java.util.function.Function;
				import java.util.function.Supplier;
				
				
				interface Flux<T> extends Publisher<T> {
					<R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> f);
					<V> Flux<V> map(Function<T,V> mapper);
					Mono<List<T>> collectList();
				}
				abstract class Mono<T> implements Publisher<T> {
					abstract T block();
					abstract <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> f);
				}
				interface Publisher<T> {}
				interface CloudFoundryOperations {
					Flux<SpaceSummary> list();
				}
				class SpaceSummary { }
				class OrganizationSummary {
					String getId() { return ""; }
				}
				interface CFSpace {}
				public class DefaultClientRequestsV2 {
				
					private Flux<OrganizationSummary> _orglist;
				
					private Mono<CloudFoundryOperations> operationsFor(OrganizationSummary org) {
						return null;
					}
				
					public List<CFSpace> getSpaces() {
						return get(
							_orglist
							.flatMap((OrganizationSummary org) -> {
								return operationsFor(org).flatMap((operations) ->
									operations
									.list()
									.map((space) -> wrap(org, space)
									)
								);
							})
							.collectList()
						);
					}
					public static <T> T get(Mono<T> mono)  {
						return mono.block();
					}
					public static CFSpace wrap(OrganizationSummary org, SpaceSummary space) {
						return null;
					}
				}
				"""
		});
}
public void testBug502568() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.Optional;
				import java.util.UUID;
				import java.util.concurrent.CompletableFuture;
				import java.util.function.Function;
				
				public class Test {
					public static void main(String[] args) {
					}
				\t
					public CompletableFuture<UUID> test() {
						UUID id = UUID.randomUUID();
					\t
						return transaction(conn -> {
							return query().thenCompose(rs1 -> {
								return query().thenCompose(rs2 -> {
									return query();
								});
							});
						})
						.thenApply(rs -> id);
					}
				\t
					public <T> CompletableFuture<T> transaction(Function<String,CompletableFuture<T>> param1) {
						return param1.apply("test");
					}
				\t
					public CompletableFuture<Optional<List<String>>> query() {
						return CompletableFuture.completedFuture(Optional.of(new ArrayList<String>()));
					}
				}
				"""
		});
}
public void testBug499725() {
	runConformTest(
		new String[] {
			"Try22.java",
			"""
				import java.rmi.RemoteException;
				import java.util.Arrays;
				import java.util.Collection;
				import java.util.Collections;
				import java.util.List;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				
				
				public class Try22 {
				    public static class RemoteExceptionWrapper {
				        @FunctionalInterface
				        public static interface FunctionRemote<T, R> {
				            R apply(T t) throws RemoteException;
				        }
				       \s
				        public static <T, R> Function<T, R> wrapFunction(FunctionRemote<T, R> f) {
				            return x -> {
				                try {
				                    return f.apply(x);
				                }
				                catch(RemoteException  e) {
				                    throw new RuntimeException(e);
				                }
				            };
				        }
				    }
				
				
				    private static class ThrowingThingy {
				        public Collection<String> listStuff(String in) throws RemoteException {
				            return Collections.emptyList();
				        }
				    }
				
				   \s
				    public static void main(String[] args) {
				        List<String> stagedNodes = Arrays.asList("a", "b", "c");
				        ThrowingThingy remoteThing = new ThrowingThingy();  // simulation of a rmi remote, hence the exceptio
				       \s
				        List<String> resultingStuff = stagedNodes.stream()
				            .flatMap(RemoteExceptionWrapper.wrapFunction(
				                node -> remoteThing.listStuff(node)    // HERE
				                    .stream()
				                    .map(sub -> node + "/" + sub)))
				            .collect(Collectors.toList());
				       \s
				        System.out.println(resultingStuff);
				    }
				}
				"""
		});
}

// Redundant type argument specification error for anonymous types should not occur below source level 9
public void testBug488663() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	String[] testFiles = new String[] {
			"C.java",
			"""
				import java.util.Comparator;
				public class C {
					Comparator<String> comparator = new Comparator<String>() { //
						@Override
						public int compare(String o1, String o2) {
							return 0;
						}
					};
				}"""
		};
	if (this.complianceLevel < ClassFileConstants.JDK9) {
	this.runConformTest(
		testFiles,
		"", options);
	} else {
		this.runNegativeTest(
			testFiles,
			"""
				----------
				1. ERROR in C.java (at line 3)
					Comparator<String> comparator = new Comparator<String>() { //
					                                    ^^^^^^^^^^
				Redundant specification of type arguments <String>
				----------
				""",
			null, true, options);
	}
}

public void testBug499725a() {
	runConformTest(
		new String[] {
			"Try22.java",
			"""
				import java.rmi.RemoteException;
				import java.util.Arrays;
				import java.util.Collection;
				import java.util.Collections;
				import java.util.List;
				import java.util.function.Function;
				import java.util.stream.Collectors;
				
				
				public class Try22 {
				    public static class RemoteExceptionWrapper {
				        @FunctionalInterface
				        public static interface FunctionRemote<T, R> {
				            R apply(T t) throws RemoteException;
				        }
				       \s
				        public static <T, R> Function<T, R> wrapFunction(FunctionRemote<T, R> f) {
				            return x -> {
				                try {
				                    return f.apply(x);
				                }
				                catch(RemoteException  e) {
				                    throw new RuntimeException(e);
				                }
				            };
				        }
				    }
				
				
				    private static class ThrowingThingy {
				        public Collection<String> listStuff(String in) throws RemoteException {
				            return Collections.emptyList();
				        }
				    }
				
				   \s
				    public static void main(String[] args) {
				        List<String> stagedNodes = Arrays.asList("a", "b", "c");
				        ThrowingThingy remoteThing = new ThrowingThingy();  // simulation of a rmi remote, hence the exceptio
				       \s
				        List<String> resultingStuff = stagedNodes.stream()
				            .flatMap(RemoteExceptionWrapper.wrapFunction(
				                (String node) -> remoteThing.listStuff(node)    // HERE
				                    .stream()
				                    .map(sub -> node + "/" + sub)))
				            .collect(Collectors.toList());
				       \s
				        System.out.println(resultingStuff);
				    }
				}
				"""
		});
}
public void testBug508834() {
	runConformTest(
		new String[] {
			"FlatMapper.java",
			"""
				import java.util.stream.Stream;
				public class FlatMapper {
				
					private String[] stuff;
				\t
					public static void main(String[] args) {
					    Stream.of(new FlatMapper[]{})
					        .flatMap(fl -> Stream.of(fl.stuff)) //
					        .filter(st -> !st.isEmpty()); //
					}
				}
				"""
		},
		"");
}
public void testBug508834_comment0() {
	runConformTest(
		new String[] {
			"test/TypeB.java",
			"""
				package test;
				public class TypeB {
				    public String getText() {
				        return "";
				    }
				
				}
				""",
			"test/TypeA.java",
			"""
				package test;
				public class TypeA {
				    public TypeB[] getArrayOfB() {
				        return null;
				    }
				    public TypeB getB() {
				        return null;
				    }
				}
				""",
			"test/Test1.java",
			"""
				package test;
				import java.util.stream.Stream;
				public class Test1 {
				    private TypeA[] arrayOfType() {
				        return null;
				    }
				    private String[] test1() {
				        return Stream
				                .of(arrayOfType())
				                .filter(a -> a.getB() != null)
				                .flatMap(a -> Stream.of(a.getB()))
				                .map(TypeB::getText)
				                .sorted()
				                .toArray(String[]::new);
				    }
				    private String[] test2() {
				        return Stream
				                .of(arrayOfType())
				                .filter(a -> a.getArrayOfB() != null)
				                .flatMap(a -> Stream.of(a.getArrayOfB()))
				                .map(TypeB::getText)
				                .sorted()
				                .toArray(String[]::new);
				    }
				}
				"""
		},
		"");
	}
	public void testBug509694() {
		runConformTest(
			new String[] {
				"NfaUtil.java",
				"""
					/*******************************************************************************
					 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
					 * All rights reserved. This program and the accompanying materials
					 * are made available under the terms of the Eclipse Public License v1.0
					 * which accompanies this distribution, and is available at
					 * http://www.eclipse.org/legal/epl-v10.html
					 *******************************************************************************/
					import java.util.*;
					
					class Lists {
						public static <E> LinkedList<E> newLinkedList() {
							return new LinkedList<E>();
						}
					
						public static <E> LinkedList<E> newLinkedList(Iterable<? extends E> elements) {
							return newLinkedList();
						}
					}
					
					class Maps {
						public static <K, V> HashMap<K, V> newHashMap() {
							return new HashMap<K, V>();
						}
					
						public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
							return new LinkedHashMap<K, V>();
						}
					
						public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
							return new LinkedHashMap<K, V>(map);
						}
					}
					
					class Sets {
						public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
							return new HashSet<E>();
						}
					
						public static <E> HashSet<E> newHashSet(E... elements) {
							HashSet<E> set = new HashSet<>();
							Collections.addAll(set, elements);
							return set;
						}
					}
					
					interface IAcceptor<T> {
						void accept(T t);
					}
					
					interface Nfa<STATE> extends DirectedGraph<STATE> {
						STATE getStop();
						STATE getStart();
					}
					interface DirectedGraph<NODE> {
						Iterable<NODE> getFollowers(NODE state);
					}
					
					/**
					 * @author Moritz Eysholdt - Initial contribution and API
					 */
					public class NfaUtil {
					
						public <S> Map<S, Set<S>> findCycles(Nfa<S> nfa) {
							Map<S, Set<S>> cycles = Maps.newLinkedHashMap();
							findCycles(nfa, nfa.getStart(), (List<S> t) -> {
								Set<S> cycle = Sets.newHashSet(t);
								for (S cycleNode : t) {
									// We have two cycles that are connected via at least
									// one node. Treat them as one cycle.
									Set<S> existingCycle = cycles.get(cycleNode);
									if (existingCycle != null) {
										cycle.addAll(existingCycle);
									}
								}
								for (S n : cycle) {
									cycles.put(n, cycle);
								}
							}, Maps.newHashMap(), Lists.newLinkedList());
							return cycles;
						}
					
						public <S> void findCycles(Nfa<S> nfa, IAcceptor<List<S>> cycleAcceptor) {
							findCycles(nfa, nfa.getStart(), cycleAcceptor, Maps.newHashMap(), Lists.newLinkedList());
						}
					
						private static final int DFS_VISITED = 1;
						private static final int DFS_ON_STACK = 2;
					
						protected <S> void findCycles(Nfa<S> nfa, S node, IAcceptor<List<S>> cycleAcceptor, Map<S, Integer> dfsMark,
								LinkedList<S> dfsStack) {
							dfsStack.push(node);
							dfsMark.put(node, DFS_ON_STACK);
							for (S follower : nfa.getFollowers(node)) {
								Integer followerMark = dfsMark.get(follower);
								if (followerMark == null) {
									findCycles(nfa, follower, cycleAcceptor, dfsMark, dfsStack);
								} else if (followerMark == DFS_ON_STACK) {
									LinkedList<S> cycle = Lists.newLinkedList();
									Iterator<S> stackIter = dfsStack.iterator();
									S cycleNode;
									do {
										cycleNode = stackIter.next();
										cycle.addFirst(cycleNode);
									} while (cycleNode != follower && stackIter.hasNext());
									cycleAcceptor.accept(cycle);
								}
							}
							dfsStack.pop();
							dfsMark.put(node, DFS_VISITED);
						}
					}
					"""
			});
	}
	public void testBug479802() {
		runConformTest(
			new String[] {
				"CompilerBugUncheckedCast.java",
				"""
					public class CompilerBugUncheckedCast {
					    public static void main(String[] args) {
					        Create(true);
					        Create(false);
					    }
					    public interface Base {
					        default String def() { return "Base"; }
					    }
					    public interface Intermediate extends Base {
					        @Override default String def() { return "Intermediate"; }
					    }
					    public interface Derived extends Intermediate { }
					    public static class MyObject implements Base { }
					    public static final class OldObject extends MyObject implements Derived { }
					    public static final class NewObject extends MyObject implements Derived { }
					    public static <OBJECT extends MyObject & Derived> void Make(OBJECT o) { }
					    public static MyObject Create(boolean old) {
					        MyObject f;
					        if (old) {
					            f = new OldObject();
					        } else {
					            f = new NewObject();
					        }
					        Make(uncheckedCast(f));
					        System.out.println(old);
					        return f;
					    }
					    @SuppressWarnings("unchecked")
					    private static <T extends MyObject & Derived> T uncheckedCast(MyObject f) {
					        return (T) f;
					    }
					}"""
			},
			"true\n" +
			"false");
	}
	public void testBug510004_a() {
		runConformTest(
			new String[] {
				"BrokenTypeInference.java",
				"""
					import java.util.Optional;
					import java.util.stream.Stream;
					
					public class BrokenTypeInference {
					    public static void main(String[] args) {
					        Optional.of("42,43").map(s -> Stream.of(s.split(",")));
					    }
					}
					"""
			});
	}
	public void testBug510004_b() {
		runConformTest(
			new String[] {
				"BrokenTypeInference.java",
				"""
					import java.util.List;
					import java.util.Optional;
					
					public class BrokenTypeInference {
					    public static void main(String[] args) {
					        Optional.of("42,43").map(s -> x(s.split(",")));
					    }
					
					    private static <X> List<X> x(X ... xs) {
					        return java.util.Collections.emptyList();
					    }
					
					    private static <X> List<X> x(X x) {
					        return java.util.Collections.emptyList();
					    }
					}
					"""
			});
	}
	public void testBug509324() {
		runConformTest(
			new String[] {
				"testgenerics/TestGenerics.java",
				"""
					package testgenerics;
					
					import java.time.Duration;
					import java.util.function.Function;
					import java.util.function.Supplier;
					
					interface Publisher<T> {}
					
					abstract class Mono<T> implements Publisher<T> {
						public static <T> Mono<T> just(T data) { return null; }
						public static <T> Mono<T> empty() { return null; }
						public final <R> Mono<R> then(Function<? super T, ? extends Mono<? extends R>> transformer) {
							return null;
						}
						public T block() { return null; }
						public final T block(Duration timeout) { return null; }
					}
					class Info {
						public String getApplicationSshEndpoint() { return null; }
					}
					class SshHost {
						public SshHost(String host, int port, String fingerPrint) { }
					}
					
					public class TestGenerics {
					
						private Mono<Info> info = Mono.just(new Info());
					
						public static <T> T ru_get(Mono<T> mono) throws Exception {
							return mono.block();
						}
					
						public SshHost getSshHost() throws Exception {
							return ru_get(
								info.then((i) -> {
									String host = i.getApplicationSshEndpoint();
									if (host!=null) {
										return Mono.just(new SshHost(host, 0, host));
									}
									return Mono.empty();
								})
							);
						}
					}
					"""
			});
	}
	public void testBug469014() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import java.util.stream.Stream;
					
					public class Test {
					    public static <T> Field<T> coalesce(T value, T... values) {
					        return coalesce(field(value), fields(values));
					    }
					    public static <T> Field<T> coalesce(Field<T> field, T value) {
					        return coalesce(field, field(value));
					    }
					
					    public static <T> Field<T> coalesce(Field<T> field, Field<?>... fields) {
					        // irrelevant
					        return null;
					    }
					
					    static <T> Field<T> field(T value) {
					        return new Field<T>(value);
					    }
					
					    static <T> Field<T>[] fields(T... values) {
					        return Stream.of(values).map(Test::field).toArray(Field[]::new);
					    }
					
					    static class Field<T> {
					        public Field(T t) {
					        }
					    }
					}
					"""
			},
			"""
				----------
				1. WARNING in Test.java (at line 4)
					public static <T> Field<T> coalesce(T value, T... values) {
					                                                  ^^^^^^
				Type safety: Potential heap pollution via varargs parameter values
				----------
				2. ERROR in Test.java (at line 5)
					return coalesce(field(value), fields(values));
					       ^^^^^^^^
				The method coalesce(Test.Field<T>, Test.Field<T>[]) is ambiguous for the type Test
				----------
				3. WARNING in Test.java (at line 20)
					static <T> Field<T>[] fields(T... values) {
					                                  ^^^^^^
				Type safety: Potential heap pollution via varargs parameter values
				----------
				4. WARNING in Test.java (at line 21)
					return Stream.of(values).map(Test::field).toArray(Field[]::new);
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The expression of type Test.Field[] needs unchecked conversion to conform to Test.Field<T>[]
				----------
				""");
	}

	public void testBug511876() {
		runConformTest(
			new String[] {
				"util/ClasspathScanner.java",
				"""
					package util;
					
					import java.io.*;
					import java.lang.reflect.Method;
					import java.util.*;
					import java.util.stream.Stream;
					
					class ClassPath {
					    public static ClassPath from(ClassLoader classloader) throws IOException {
					        return new ClassPath();
					    }
					    public Set<ClassInfo> getTopLevelClasses() {
					        return Collections.emptySet();
					    }
					}
					class ClassInfo {
					    public Class<?> load() { return null; }
					    public String getPackageName() { return ""; }
					}
					
					/**
					 * @see https://blog.jooq.org/2016/04/21/the-parameterless-generic-method-antipattern/
					 */
					public class ClasspathScanner {
					    /**
					     * This will produce all the generic, parameterless methods on your class path.
					     */
					    public static void main(String[] args) throws Exception {
					        ClassPath.from(Thread.currentThread().getContextClassLoader())
					                .getTopLevelClasses()
					                .stream()
					                .filter(info -> !info.getPackageName().startsWith("akka") && !info.getPackageName().startsWith("scala") && !info.getPackageName().startsWith("java"))
					                .flatMap(info -> {
					                    try {
					                        return Stream.of(info.load());
					                    }
					                    catch (Throwable ignore) {
					                        return Stream.empty();
					                    }
					                }).flatMap(c -> {
					                    try {
					                        return Stream.of(c.getMethods());
					                    }
					                    catch (Throwable ignore) {
					                        return Stream.<Method> of();
					                    }
					                })
					                .filter(m -> m.getTypeParameters().length > 0 && m.getParameterCount() == 0)
					                .sorted(Comparator.comparing(Method::toString))
					                .map(Method::toGenericString)
					                .forEach(System.out::println);
					    }
					}
					"""
			});
	}

	public void testBug510111() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.util.ArrayList;
					import java.util.Collections;
					import java.util.Comparator;
					import java.util.List;
					
					public class Test {
					
					        public static final class Entity {
					
					                public int getIndex() {
					                        return 1;
					                }
					        }
					
					        public static void main(String[] args) {
					
					                final List<Entity> list = new ArrayList<>();
					                // Eclipse fails to compile the next line with error
					                // Type mismatch: cannot convert from int to Comparable<? super Comparable<? super U>>
					                Collections.sort( list , Comparator.comparing( a -> a.getIndex() ) );\s
					        }
					}"""
			});
	}

	public void testBug511750() {
		runConformTest(
			new String[] {
				"SomeClass.java",
				"""
					import java.util.Collections;
					import java.util.Set;
					import java.util.stream.Collectors;
					
					public class SomeClass {
					
					    public static void main(String[] args) {
					        System.out.println(foo().iterator().next().getBaz());
					    }
					
					    public interface Baz {
					        public String getBaz();
					    }
					
					    public static Set<Baz> foo() {
					        Set<String> stringSet = Collections.singleton("someString");
					        return stringSet.stream().map(s -> new Baz() {
					
					            @Override
					            public String getBaz() {
					                return s;
					            }
					
					        }).collect(Collectors.toSet());
					    }
					}
					"""
			});
	}
	public void testBug511071() {
		runNegativeTest(
			new String[] {
				"test/ATestClass.java",
				"package test;\n" +
				"\n" +
				"interface Functional<T> {\n" +
				"	void test(T t);\n" +
				"}\n" +
				"\n" +
				"public abstract class ATestClass {\n" +
				"	abstract void f(Functional<? super ClassWithMethodWithMissingArgType> predicate);\n" +
				"\n" +
				"	public void m() {\n" +
				"		f(e -> e.matches(\"\"));\n" +
				"	}\n" +
				"}\n" +
				"",
				"test/ClassWithMethodWithMissingArgType.java",
				"package test;\n" +
				"\n" +
				"import java.util.List;\n" +
				"\n" +
				"import missing.Type;\n" +
				"\n" +
				"public class ClassWithMethodWithMissingArgType {\n" +
				"	public void matches(Type arg) {\n" +
				"		arg.hashCode();\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			"""
				----------
				1. ERROR in test\\ATestClass.java (at line 11)
					f(e -> e.matches(""));
					         ^^^^^^^
				The method matches(Type) from the type ClassWithMethodWithMissingArgType refers to the missing type Type
				----------
				----------
				1. ERROR in test\\ClassWithMethodWithMissingArgType.java (at line 5)
					import missing.Type;
					       ^^^^^^^
				The import missing cannot be resolved
				----------
				2. ERROR in test\\ClassWithMethodWithMissingArgType.java (at line 8)
					public void matches(Type arg) {
					                    ^^^^
				Type cannot be resolved to a type
				----------
				"""
		);
	}

	public void testBug511252orig() {
		runConformTest(
			new String[] {
				"ConfigurationServiceLocator.java",
				"""
					import java.util.*;
					import java.util.function.*;
					import java.util.concurrent.*;
					import java.net.URI;
					public class ConfigurationServiceLocator {
					
					  private final Map<String, URI> services = new HashMap<>();
					
					  public <T> CompletionStage<Optional<T>> doWithService(String name, Function<URI, CompletionStage<T>> block) {
					      return Optional.ofNullable(services.get(name))
					              .map(block.andThen(cs -> cs.thenApply(Optional::ofNullable)))
					              .orElse(CompletableFuture.completedFuture(Optional.empty()));
					  }
					
					}
					"""
			});
	}

	public void testBug511252simplified() {
		runConformTest(
			new String[] {
				"ConfigurationServiceLocator.java",
				"""
					import java.util.*;
					import java.util.function.*;
					import java.util.concurrent.*;
					import java.net.URI;
					public class ConfigurationServiceLocator {
					
					  public <T> CompletionStage<Optional<T>> doWithService(Optional<URI> uriopt, Function<URI, CompletionStage<T>> block) {
					      return uriopt
					              .map(block.andThen(cs -> cs.thenApply(Optional::ofNullable)))
					              .orElse(CompletableFuture.completedFuture(Optional.empty()));
					  }
					
					}
					"""
			});
	}

	public void testBug511878() {
		// note: type variables renamed to facilitate debugging
		runConformTest(
			new String[] {
				"SimpleParser.java",
				"""
					import java.util.function.Function;
					
					
					public interface SimpleParser<T> {
					
					    static class Tuple<A,B> {
					    }
					   \s
					    /** the type of the functional interface: Parser<T> :: CharSequence -> Tuple<T, CharSequence>> */
					    abstract Tuple<T, CharSequence> parse(CharSequence cs);
					   \s
					    default <V> SimpleParser<V> andThenBinding(Function<? super T, SimpleParser<V>> f) {
					        return null;
					    }
					
					    default <W> SimpleParser<W> andThen(SimpleParser<W> p) {
					        return null;
					    }
					  \s
					    static <X> SimpleParser<X> output(X v) {
					        return null;
					    }
					   \s
					    static SimpleParser<String> space() {
					        return null;
					    }
					
					    static <Y> SimpleParser<Y> token(SimpleParser<Y> p) {
					        return space().andThen(p.andThenBinding(v -> space().andThen(output(v))));
					    }
					
					}
					"""
			});
	}
	public void testBug506021() {
	runConformTest(
		new String[] {
			"test/__.java",
			"package test;\n" +
			"\n" +
			"interface Result {}\n" +
			"\n" +
			"interface Property<V> {}\n" +
			"\n" +
			"interface GraphTraversal<E> {}\n" +
			"\n" +
			"public class __ {\n" +
			"	public static <E> GraphTraversal<? extends Property<E>> properties2() {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static GraphTraversal<? extends Property<Result>> properties() {\n" +
			"		return properties2();\n" +
			"	}\n" +
			"}\n" +
			"",
		});
	}

	public void testBug506022() {
		// extracted from problem compiling org.apache.tinkerpop.gremlin.giraph.structure.io.GiraphVertexOutputFormat
		// changing the return type of getClass to Class<U> fixes the problem
		runConformTest(
			new String[] {
				"test2/Test2.java",
				"package test2;\n" +
				"\n" +
				"abstract class OutputFormat {\n" +
				"	public abstract int getOutputCommitter();\n" +
				"}\n" +
				"\n" +
				"public abstract class Test2 {\n" +
				"	public static <T> T newInstance(Class<T> theClass) {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	abstract <U> Class<? extends U> getClass(Class<U> xface);\n" +
				"\n" +
				"	int f() {\n" +
				"		return newInstance(getClass(OutputFormat.class)).getOutputCommitter();\n" +
				"	}\n" +
				"}\n" +
				"",
			}
		);
	}

	public void testBug506022b() {
		// extracted from a problem in org.apache.tinkerpop.gremlin.process.computer.util.ComputerGraph
		// replacing this.properties() by this.<I>properties() fixes the problem
		runNegativeTest(
			new String[] {
				"test/Test.java",
				"package test;\n" +
				"\n" +
				"interface Iterator<A> {\n" +
				"}\n" +
				"\n" +
				"interface Function<B, C> {\n" +
				"	C applyTo(B b);\n" +
				"}\n" +
				"\n" +
				"interface Property<D> {\n" +
				"}\n" +
				"\n" +
				"class ComputerProperty<E> implements Property<E> {\n" +
				"	public ComputerProperty(final Property<E> property) {\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"public abstract class Test {\n" +
				"	public abstract <F, G> Iterator<G> map(final Iterator<F> iterator, final Function<F, G> function);\n" +
				"\n" +
				"	public abstract <H> Iterator<? extends Property<H>> properties();\n" +
				"\n" +
				"	public <I> Iterator<Property<I>> test() {\n" +
				"		return map(this.properties(), property -> new ComputerProperty(property));\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			"""
				----------
				1. WARNING in test\\Test.java (at line 24)
					return map(this.properties(), property -> new ComputerProperty(property));
					                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The constructor ComputerProperty(Property) belongs to the raw type ComputerProperty. References to generic type ComputerProperty<E> should be parameterized
				----------
				2. WARNING in test\\Test.java (at line 24)
					return map(this.properties(), property -> new ComputerProperty(property));
					                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The expression of type ComputerProperty needs unchecked conversion to conform to Property<I>
				----------
				3. WARNING in test\\Test.java (at line 24)
					return map(this.properties(), property -> new ComputerProperty(property));
					                                              ^^^^^^^^^^^^^^^^
				ComputerProperty is a raw type. References to generic type ComputerProperty<E> should be parameterized
				----------
				"""
		);
	}
	public void testBug514884() {
		runConformTest(
			new String[] {
				"Minimal.java",
				"""
					import java.io.*;
					public class Minimal {
					    public void iCrash() throws IOException {
					        try (Closeable o = consumes(sneaky()::withVarargs)) {
					        }
					    }
					
					    private Minimal sneaky() { return this; }
					
					    private void withVarargs(String... test) {}
					
					    private Closeable consumes(Runnable r) { return null; }
					}
					"""
			});
	}

	public void testBug494733_comment0() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
						public static void main(String[] args) {
					        List<Integer> integerList = new ArrayList<>();
					        Set<List<Number>> numbetListSet = Collections.singleton(toWildcardGeneric(integerList));
					        numbetListSet.iterator().next().add(Float.valueOf(1.0f));
					        Integer i = integerList.get(0); // Throws ClassCastException
					    }
					   \s
					    static <T> List<? extends T> toWildcardGeneric(List<T> l) {
					        return l;
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					Set<List<Number>> numbetListSet = Collections.singleton(toWildcardGeneric(integerList));
					                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from Set<List<Integer>> to Set<List<Number>>
				----------
				""");
	}

	public void testBug494733_comment1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X {
					public static void main(String[] args) {
					    List<Integer> integerList = new ArrayList<>();
					    List<Object> objectList = id(toWildcardGeneric(integerList));
					    objectList.add("Woo?");
					    Integer i = integerList.get(0);
					}
					
					static <T> T id(T o) {
					    return o;
					}
					
					static <T> List<? extends T> toWildcardGeneric(List<T> l) {
					    return l;
					}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					List<Object> objectList = id(toWildcardGeneric(integerList));
					                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from List<Integer> to List<Object>
				----------
				""");
	}

	public void test483952_bare () {
		runNegativeTest(
			new String[] {
				"test/Test.java",
				"""
					package test;
					import java.util.function.Function;
					public class Test {
						void test1() {
							Function function = x -> x;
							String [] z = test2(function, "");
						}
						<T> T [] test2(Function<T, T> function, T t) {
							return null;
						}
					}"""

			},
			"""
				----------
				1. WARNING in test\\Test.java (at line 5)
					Function function = x -> x;
					^^^^^^^^
				Function is a raw type. References to generic type Function<T,R> should be parameterized
				----------
				2. WARNING in test\\Test.java (at line 6)
					String [] z = test2(function, "");
					              ^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation test2(Function, String) of the generic method test2(Function<T,T>, T) of type Test
				----------
				3. WARNING in test\\Test.java (at line 6)
					String [] z = test2(function, "");
					                    ^^^^^^^^
				Type safety: The expression of type Function needs unchecked conversion to conform to Function<String,String>
				----------
				""");
	}
	public void testBug517710() {
		runConformTest(
			new String[] {
				"test/Test.java",
				"package test;\n" +
				"\n" +
				"public class Test {\n" +
				"	public static class Foo<T> {\n" +
				"	}\n" +
				"\n" +
				"	public static class Bar<U> {\n" +
				"	}\n" +
				"\n" +
				"	public <V> V foo(Foo<V> f) {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	public Foo<Integer> bar(Bar<Integer> b) {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	public Object baz() {\n" +
				"		Bar b = null;\n" +
				"		return foo(bar(b));\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"",
			}
		);
	}
	public void testBug513567() {
		runConformTest(
			new String[] {
				"Foo.java",
				"import java.util.Collection;\n" +
				"import java.util.Optional;\n" +
				"\n" +
				"public class Foo {\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		new Foo().test();\n" +
				"	}\n" +
				"\n" +
				"    private Collection<String> createCollection(Optional<String> foo) {\n" +
				"        return null;\n" +
				"    }\n" +
				"\n" +
				"    private <T> void consumeCollection(Collection<T> bar) {\n" +
				"        // no-op\n" +
				"    }\n" +
				"\n" +
				"    @SuppressWarnings({\"rawtypes\", \"unchecked\"})\n" +
				"    public void test() {\n" +
				"        consumeCollection(createCollection((Optional) null));\n" +
				"    }\n" +
				"\n" +
				"}\n" +
				"",
			}
		);
	}

	public void testBug521159() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.util.*;
					import java.util.stream.*;
					
					interface JarEntry {
						boolean isDirectory();
						String getName();
					}
					class VersionedStream {
						static Stream<JarEntry> stream() { return null; }\
					}
					public class Test {
						static final String SERVICES_PREFIX = "META-INF/services/";
						Map<Boolean, Set<String>> test() {
							return VersionedStream.stream()
								.filter(e -> (! e.isDirectory()))
								.map(JarEntry::getName)
								.filter(e ->\s
					(e.endsWith(".class") ^ e.startsWith(SERVICES_PREFIX)))
								.collect(Collectors.partitioningBy(e -> e.startsWith(SERVICES_PREFIX), Collectors.toSet()));\
						}
					}
					"""
			});
	}

	public void testBug521822() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.util.List;
					interface I<U> {
					    List<U> foo(int i);
					}
					class X<T> {
					    List<T> t;
					    X(I<T> i) {
					        this.t = i.foo(0);
					    }
					}
					class Y<T> extends X<T> {
					    Y(I<T> t) {super(t);}
					}
					class Z {
					    static List<?> method(int ... i) {return null;}
					}
					public class Test  {
					    static X x = new Y<>(Z::method);
					}
					"""
			});
	}

	public void testBug521185() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test<T> {
						private static class Inner {\
							public Inner(){}
						}
						public Inner get() {
							return new Inner();
						}
					}
					""",
				"Z.java",
				"""
					class Z<T> implements I<T> {
						public Z(Runnable r, T... t1) {}
						public String toString (T t) {
							return t.toString();
						}
					}""",
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Test<String> t = new Test<>();
							Z z = new Z<>(()->{System.out.println("asdad");}, t.get());
						}
					}
					interface I<T> {
						String toString();
					}"""
			},
			"""
				----------
				1. WARNING in Z.java (at line 2)
					public Z(Runnable r, T... t1) {}
					                          ^^
				Type safety: Potential heap pollution via varargs parameter t1
				----------
				----------
				1. WARNING in X.java (at line 4)
					Z z = new Z<>(()->{System.out.println("asdad");}, t.get());
					^
				Z is a raw type. References to generic type Z<T> should be parameterized
				----------
				3. ERROR in X.java (at line 4)
					Z z = new Z<>(()->{System.out.println("asdad");}, t.get());
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The type Test.Inner is not visible
				----------
				""");
	}

	public void testBug521185a() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test<T> {
						private static class Inner {\
							public Inner(){}
						}
						public Inner get() {
							return new Inner();
						}
					}
					""",
				"Z.java",
				"""
					class Z<T> implements I<T> {
						public class Q<TT> {
							public Q(Runnable r, TT... ts) {}
						}
						public Z(Runnable r, T... t1) {}
						public String toString (T t) {
							return t.toString();
						}
					}""",
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							Test<String> t = new Test<>();
							Z<Object> zr = new Z<>(null, new Object[0]);
							Z.Q zq = zr.new Q<>(null, t.get());
						}
					}
					interface I<T> {
						String toString();
					}"""
			},
			"""
				----------
				1. WARNING in Z.java (at line 3)
					public Q(Runnable r, TT... ts) {}
					                           ^^
				Type safety: Potential heap pollution via varargs parameter ts
				----------
				2. WARNING in Z.java (at line 5)
					public Z(Runnable r, T... t1) {}
					                          ^^
				Type safety: Potential heap pollution via varargs parameter t1
				----------
				----------
				1. WARNING in X.java (at line 5)
					Z.Q zq = zr.new Q<>(null, t.get());
					^^^
				Z.Q is a raw type. References to generic type Z<T>.Q<TT> should be parameterized
				----------
				2. ERROR in X.java (at line 5)
					Z.Q zq = zr.new Q<>(null, t.get());
					         ^^^^^^^^^^^^^^^^^^^^^^^^^
				The type Test.Inner is not visible
				----------
				""");
	}

	public void testBug521978() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					class Foo<T> {
						public  Foo(T a1, List<String> a2){ }
					}
					class Bar<T> {
						Bar(T item) { }
					}
					public class Test {
						static <T> Bar<T> getBar(T item) {
					        return new Bar<>(item);
					    }
						static <Q> Foo<Q> method(Bar<? extends Foo<Q>> f) {
					    	return null;
					    }
						static void test() {
							method(getBar(
					                new Foo<>("str", new ArrayList())
					            ));
						}
					}"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 17)
					method(getBar(
					^^^^^^
				The method method(Bar<? extends Foo<Q>>) in the type Test is not applicable for the arguments (Bar<Foo>)
				----------
				2. WARNING in Test.java (at line 18)
					new Foo<>("str", new ArrayList())
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The constructor Foo(Object, List) belongs to the raw type Foo. References to generic type Foo<T> should be parameterized
				----------
				3. WARNING in Test.java (at line 18)
					new Foo<>("str", new ArrayList())
					                     ^^^^^^^^^
				ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
				----------
				""");
	}
	public void testBug525576() {
		this.runNegativeTest(
			new String[] {
				"test/Action.java",
				"package test;\n" +
				"\n" +
				"public class Action<S2 extends Service> {\n" +
				"    void setService(S2 s) {\n" +
				"    }\n" +
				"}\n" +
				"",
				"test/Device.java",
				"package test;\n" +
				"\n" +
				"public abstract class Device<DI, S1 extends Service> {\n" +
				"    private DI identity;\n" +
				"\n" +
				"    protected void find(Service service) {\n" +
				"        service.getDevice();\n" +
				"    }\n" +
				"\n" +
				"    public Object equals(Device obj) {\n" +
				"        return obj.identity;\n" +
				"    }\n" +
				"}\n" +
				"",
				"test/Service.java",
				"package test;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"\n" +
				"public abstract class Service<D1 extends Device, S2 extends Service> {\n" +
				"    public Service(Action<S2>[] actionArr) {\n" +
				"        for (Action action : actionArr) {\n" +
				"            action.setService(this);\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    public Action<S2>[] getActions(Collection<Action> actions) {\n" +
				"        return actions.toArray(new Action[actions.size()]);\n" +
				"    }\n" +
				"\n" +
				"    public D1 getDevice() {\n" +
				"    		return null;\n" +
				"    }\n" +
				"}\n" +
				""
			},
			"""
				----------
				1. WARNING in test\\Action.java (at line 3)
					public class Action<S2 extends Service> {
					                               ^^^^^^^
				Service is a raw type. References to generic type Service<D1,S2> should be parameterized
				----------
				----------
				1. WARNING in test\\Device.java (at line 3)
					public abstract class Device<DI, S1 extends Service> {
					                                            ^^^^^^^
				Service is a raw type. References to generic type Service<D1,S2> should be parameterized
				----------
				2. WARNING in test\\Device.java (at line 6)
					protected void find(Service service) {
					                    ^^^^^^^
				Service is a raw type. References to generic type Service<D1,S2> should be parameterized
				----------
				3. WARNING in test\\Device.java (at line 10)
					public Object equals(Device obj) {
					                     ^^^^^^
				Device is a raw type. References to generic type Device<DI,S1> should be parameterized
				----------
				----------
				1. WARNING in test\\Service.java (at line 5)
					public abstract class Service<D1 extends Device, S2 extends Service> {
					                                         ^^^^^^
				Device is a raw type. References to generic type Device<DI,S1> should be parameterized
				----------
				2. WARNING in test\\Service.java (at line 5)
					public abstract class Service<D1 extends Device, S2 extends Service> {
					                                                            ^^^^^^^
				Service is a raw type. References to generic type Service<D1,S2> should be parameterized
				----------
				3. WARNING in test\\Service.java (at line 7)
					for (Action action : actionArr) {
					     ^^^^^^
				Action is a raw type. References to generic type Action<S2> should be parameterized
				----------
				4. WARNING in test\\Service.java (at line 8)
					action.setService(this);
					^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method setService(Service) belongs to the raw type Action. References to generic type Action<S2> should be parameterized
				----------
				5. WARNING in test\\Service.java (at line 12)
					public Action<S2>[] getActions(Collection<Action> actions) {
					                                          ^^^^^^
				Action is a raw type. References to generic type Action<S2> should be parameterized
				----------
				6. WARNING in test\\Service.java (at line 13)
					return actions.toArray(new Action[actions.size()]);
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The expression of type Action[] needs unchecked conversion to conform to Action<S2>[]
				----------
				""");
	}
	public void testBug515600() {
		runConformTest(
			new String[] {
				"test/Test.java",
				"package test;\n" +
				"\n" +
				"interface Publisher<P> {\n" +
				"	void subscribe(Subscriber<? super P> s);\n" +
				"}\n" +
				"\n" +
				"interface Subscriber<S> {\n" +
				"}\n" +
				"\n" +
				"class Flux {\n" +
				"	public static <F> void from(Publisher<? extends F> source) {\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"public abstract class Test {\n" +
				"	abstract void assertThat2(Boolean actual);\n" +
				"\n" +
				"	abstract void assertThat2(String actual);\n" +
				"\n" +
				"	abstract <S> S scan(Class<S> type);\n" +
				"\n" +
				"	public void test() {\n" +
				"		Flux.from(s -> {\n" +
				"			assertThat2(scan(Boolean.class));\n" +
				"		});\n" +
				"	}\n" +
				"}\n" +
				"",
			}
		);
	}
	public void testBug527742() {
		runConformTest(new String[] {
				"test/Test.java",
				"""
					package test;
					import java.util.stream.*;
					import java.util.*;
					
					class Test {
					
					    public void f() {
					
					        Map<Integer, String> map = new HashMap<>();
					        map.put(1, "x");
					        map.put(2, "y");
					        map.put(3, "x");
					        map.put(4, "z");
					
					  //the following line has error
					        Map<String, ArrayList<Integer>> reverseMap = new java.util.HashMap<>(map.entrySet().stream()
					                .collect(Collectors.groupingBy(Map.Entry::getValue)).values().stream()
					                .collect(Collectors.toMap(item -> item.get(0).getValue(),
					                        item -> new ArrayList<>(item.stream().map(Map.Entry::getKey).collect(Collectors.toList())))));\s
					        System.out.println(reverseMap);
					
					    }
					
					}""",

		});
	}
	public void testBug528045() {
		runConformTest(new String[] {
				"test/Test.java",
				"package test;\n" +
				"\n" +
				"interface List<A0> {\n" +
				"	List<A0> append(List<A0> other);\n" +
				"}\n" +
				"\n" +
				"interface Function<A1, B1> {\n" +
				"	B1 f(A1 a);\n" +
				"}\n" +
				"\n" +
				"interface BiFunction<A2, B2, C2> {\n" +
				"	C2 f(A2 a, B2 b);\n" +
				"}\n" +
				"\n" +
				"interface Stream<A3> {\n" +
				"	<B3> B3 foldLeft(Function<B3, Function<A3, B3>> f, B3 b);\n" +
				"\n" +
				"	<C3> C3 foldLeft(BiFunction<C3, A3, C3> f, C3 b);\n" +
				"}\n" +
				"\n" +
				"public class Test {\n" +
				"	<A> List<A> f(Stream<List<A>> map, List<A> nil) {\n" +
				"		return map.foldLeft(List::append, nil);\n" +
				"	}\n" +
				"}\n" +
				""
		});
	}
	public void testBug528046() {
		runConformTest(new String[] {
				"test2/Test.java",
				"package test2;\n" +
				"\n" +
				"interface Supplier<A0> {\n" +
				"	A0 f();\n" +
				"}\n" +
				"\n" +
				"interface Function<A1, B2> {\n" +
				"	B2 f(A1 a);\n" +
				"}\n" +
				"\n" +
				"interface P {\n" +
				"	<A3> void lazy(Supplier<A3> f);\n" +
				"\n" +
				"	<A4> void lazy(Function<Object, A4> f);\n" +
				"}\n" +
				"\n" +
				"class Nil<A5> {\n" +
				"}\n" +
				"\n" +
				"class Test {\n" +
				"	static void test(P p) {\n" +
				"		p.lazy(Nil::new);\n" +
				"	}\n" +
				"}\n" +
				""
		});
	}
	public void testBug519380() {
		runConformTest(
			new String[] {
				"TestLambda.java",
				"""
					import java.util.*;
					import java.util.function.*;
					public class TestLambda {
					
					    protected static <K, V> Map<K, V> newMap(final Function<K, V> loader) {
					        return new HashMap<>();
					    }
					
					    private final Map<Integer, Integer> working = newMap(key -> {
					
					        final List<String> strings = new ArrayList<>();
					
					        final String[] array = strings.toArray(new String[strings.size()]);
					        foo(array);
					
					        return null;
					    });
					
					    private final Map<Void, Void> notWorking = newMap(key -> {
					
					        final List<String> strings = new ArrayList<>();
					
					        // This line seems to be the root of all evils
					        foo(strings.toArray(new String[strings.size()]));
					
					        return null;
					    });
					
					    private void foo(final String[] x) {}
					
					    private void foo(final Integer[] x) {}
					
					}
					"""
			});
	}
	public void testBug519147() {
		runConformTest(
			new String[] {
				"Main.java",
				"""
					import java.util.HashMap;
					import java.util.HashSet;
					import java.util.Set;
					
					public class Main<MyKey, MyValue> {
					
					    static class MyMap<K, V> extends HashMap<K, V> {
					        public MyMap<K, V> putAllReturning(MyMap<K, V> c) { putAll(c); return this; }
					        public MyMap<K, V> putReturning(K key, V value) { put(key, value); return this; }
					    }
					
					    public Main() {
					        Set<MyValue> values = new HashSet<>(); // actually something better
					        final MyMap<MyKey, MyValue> myMap =
					                values.stream()
					                    .reduce(
					                        new MyMap<MyKey, MyValue>(),
					                        (map, value) -> {
					                            Set<MyKey> keys = new HashSet<>(); // actually something better
					
					                            return keys.stream()
					                                .reduce(
					                                    map, // this would work syntactically: new MyMap<MyKey, MyValue>(),
					                                    (map2, key) -> map2.putReturning(key, value),
					                                    MyMap::putAllReturning);
					                        },
					                        MyMap::putAllReturning
					                    );
					    }
					}
					"""
			});
	}
	// no change
	public void testBug521982_comment1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<T> T test(T a, Integer x) { return a; }
						<T> T test(T a, int x) { return a; }
						void doit1(Number nIn) {
							// in assignment context, primitive or boxed target type does not influence overloading
							Number n1 = test(nIn, 3);
							int n2 = test(Integer.valueOf(2), Integer.valueOf(3)); // not ambiguous because one inferences succeeds in strict mode
						}
						void fun(int i) {}
						void doit2() {
							// unboxing allowed if outer invocation is unambiguous
							fun(test(Integer.valueOf(2), 3));
						}
						<T> void fun2(int i, T t) {} // not picked, requires loose mode
						<T> void fun2(Integer i, T t) {}
						void doit3() {
							// primitive arg puts inference to loose mode, then losing during overload resolution
							fun2(test(Integer.valueOf(2), 3), this);
						}
						<T extends Number> void fun3(int i, int j) {} // requires loose mode for param 1
						<T extends Number> void fun3(Integer i, T t) {} // requires loose mode for param 2
						void doit4() {
							// ambiguous because both candidates require loose mode
							fun3(test(Integer.valueOf(2), 3), 4);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 24)
					fun3(test(Integer.valueOf(2), 3), 4);
					^^^^
				The method fun3(int, int) is ambiguous for the type X
				----------
				""");
	}

	public void testBug529518() {
		Runner run = new Runner();
		run.testFiles = new String[] {
			"Try.java",
			"""
				import java.util.function.*;
				public class Try<T> {
				    @FunctionalInterface
				    interface CheckedSupplier<R> {
				        R get() throws Throwable;
				    }
				    static <T> Try<T> of(CheckedSupplier<? extends T> supplier) {
				    	return null;
				    }
					 T getOrElseGet(Function<? super Throwable, ? extends T> other) { return null; }
				}
				""",
			"X.java",
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"        byte[] decode(byte[] base64Bytes) {\n" +
			"                return Try.of(() -> Base64.getDecoder().decode(base64Bytes))\n" +
			"                        .getOrElseGet(t -> null);\n" +
			"        }\n" +
			"}\n" +
			""
		};
		run.runConformTest();
	}
	public void testBug528970() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					import java.util.concurrent.atomic.*;
					public class X {
						public static <T> List<T> returnNull(Class<? extends T> clazz) {
							return null;
						}
					
						public static void main( String[] args )
						{
							List<AtomicReference<?>> l = returnNull(AtomicReference.class);
						}\
					}
					"""
			});
	}
	public void testBug530235() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface MySupplier<V> {
					    V get(Object x) throws Exception;
					}
					
					public class X {
					    public <S> S getSmth()  {
						return exec(s -> {return X.getType(Integer.class);}); /*here the error*/
					    }
					   \s
					    public static <T> T getType(Class<T> class1) {
						    return null;
					    }
					
					    public <U> U exec(MySupplier<U> supplier)  {
						    throw new RuntimeException("Not implemented yet");
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					return exec(s -> {return X.getType(Integer.class);}); /*here the error*/
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from Object to S
				----------
				""");
	}
	public void testBug531681() {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"""
					
					import java.util.Arrays;
					import java.util.function.IntFunction;
					
					public class X {
						public static void main(String[] args) {
							final long[][] someData = new long[0][];
					
							IntFunction<long[][]> function1 = long[][]::new;
							IntFunction<long[][]> function2 = new IntFunction<long[][]>() {
								@Override
								public long[][] apply(int value) { return new long[value][]; }
							};
					
							long[][] array1 = Arrays.stream(someData).toArray(long[][]::new); // works
							long[][] array2 = Arrays.stream(someData).toArray(function2); // compile error in ecj at compliance 1.8
							long[][] array3 = Arrays.stream(someData).toArray(function1); // compile error in ecj at compliance 1.8
						}
					}
					"""
			};
		runner.runConformTest();
	}
	public void testBug488328_001() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
					  static class A<R> {
					    class  I {
					    }
					  }
					
					  public static <R> void m(A<R>.I instance, R generic) {
					    System.out.println("called with A<R>.I");
					  }
					
					  public static void m(long l, Object o) {
					    System.out.println("called with long");
					  }
					
					  public static void main(String... args) {
					    Long l = new Long(3);
					    m(l, l);
					  }
					}"""
			});
	}
	public void testBug488328_002() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					class A1<R> {
					    class  I1<S> {}
					}
					public class Test<R> extends A1<R>{
					  class A2 {
					    class I2 extends A1<R>.I1<R> {}
					  }
					
					  public static <R> void m(A1<R>.I1<R> instance) {
					    System.out.println("called with A1<R>.I1<R>");
					  }
					  public static void main(String... args) {
					    Test<Integer>.A2.I2  l =  new Test<Integer>().new A2().new I2();
					    m(l);
					  }
					}"""
			});
	}
	public void testBug535969() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.util.List;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;
					
					public class Test {
						public static void main(String[] args) {
							List<String> strings = Stream.of(1, 2, 3).map(i -> {
								return new Object() {
									final Integer myInt = i;
								};
							}).map(o -> {
								return o.myInt.toString();
							}).collect(Collectors.toList());
						}
					}
					"""
			});
	}
	public void testBug535969b() {
		runConformTest(
			new String[] {
				"B.java",
				"""
					
					import java.util.Optional;
					import java.util.function.Supplier;
					import java.io.Serializable;
					
					public class B {
					    public static void main(String[] args) {
					
					        // This works fine:
					        System.out.println(new Object() {
					            int j = 5;
					        }.j);
					
					        // This also
					        System.out.println(trace(new Object() {
					            int j = 5;
					        }).j);
					
					        // Also no problem
					        System.out.println(unwrapAndTrace(Optional.of(new Object() {
					            int j = 5;
					        })).j);
					
					        // Lambdas work:
					        System.out.println(((Supplier & Serializable) () -> new Object()).get());\s
					
					        // This doesn't work.
					        System.out.println(invokeAndTrace(() -> new Object() {
					            int j = 5;
					        }).j);
					    }
					
					    public static <T> T trace(T obj) {
					        System.out.println(obj);
					        return obj;
					    }
					
					    public static <T> T invokeAndTrace(Supplier<T> supplier) {
					        T result = supplier.get();
					        System.out.println(result);
					        return result;
					    }
					
					    public static <T> T unwrapAndTrace(Optional<T> optional) {
					        T result = optional.get();
					        System.out.println(result);
					        return result;
					    }
					}
					"""
			});
	}
	public void testBug477894() {
		runConformTest(
			new String[] {
				"Main.java",
				"""
					public class Main {
						static class Foo<T> {
							private final T arg;
							public Foo(T arg) {
								this.arg = arg;
							}
							<R> Foo<R> select(java.util.function.Function<T, R> transformer) {
								return new Foo<>(transformer.apply(this.arg));
							}
							<R> R select2(java.util.function.Function<T, R> transformer) {
								return transformer.apply(this.arg);
							}
						}
						public static void main(String[] args) {
							String out = new Foo<Object>(null)
							.select(x -> new Object() {
								String alias = "anonymous#1";
							})
							.select2(x -> x.alias);
							System.out.println(out);
						}
					}
					"""
			});
	}
	public void testBug427265() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.List;
					
					public class X {
						public static void main(String[] args) {
							List<String> ss = Arrays.asList("1", "2", "3");
							ss.stream().map(s -> new Object() { });
						}
					}
					"""
			});
	}
	public void testBug427265_comment6() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					import java.util.List;
					
					public class X {
						void m() {
					        List<String> ss = Arrays.asList("1", "2", "3");
					       \s
					        ss.stream().map(s -> {
					          class L1 {};
					          class L2 {
					            L1 mm(L1 l) { return l;}
					          }
					          return new L2().mm(new L1());
					        }).forEach(e -> System.out.println(e));
					    }
					}
					"""
			});
	}
	public void testBug525580() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.IGNORE);
		runner.testFiles =
			new String[] {
				"org/a/a/g/d.java",
				"""
					package org.a.a.g;
					
					public class d {
					
					    public <T extends e> T a(Class<T> cls) {
					        T t = (e) cls.newInstance();
					        while (size >= 0) {
					            T a = ((b) this.e.m.get(size)).a();
					            t = a;
					        }
					        return t;
					    }
					
					    public interface b {
					        <T extends e> T a();
					
					        <T extends j> T b();
					    }
					}
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in org\\a\\a\\g\\d.java (at line 5)
					public <T extends e> T a(Class<T> cls) {
					                  ^
				e cannot be resolved to a type
				----------
				2. ERROR in org\\a\\a\\g\\d.java (at line 6)
					T t = (e) cls.newInstance();
					      ^^^^^^^^^^^^^^^^^^^^^
				e cannot be resolved to a type
				----------
				3. ERROR in org\\a\\a\\g\\d.java (at line 6)
					T t = (e) cls.newInstance();
					       ^
				e cannot be resolved to a type
				----------
				4. ERROR in org\\a\\a\\g\\d.java (at line 7)
					while (size >= 0) {
					       ^^^^
				size cannot be resolved to a variable
				----------
				5. ERROR in org\\a\\a\\g\\d.java (at line 8)
					T a = ((b) this.e.m.get(size)).a();
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from e to T
				----------
				6. ERROR in org\\a\\a\\g\\d.java (at line 8)
					T a = ((b) this.e.m.get(size)).a();
					                ^
				e cannot be resolved or is not a field
				----------
				7. ERROR in org\\a\\a\\g\\d.java (at line 8)
					T a = ((b) this.e.m.get(size)).a();
					                        ^^^^
				size cannot be resolved to a variable
				----------
				8. ERROR in org\\a\\a\\g\\d.java (at line 15)
					<T extends e> T a();
					           ^
				e cannot be resolved to a type
				----------
				9. ERROR in org\\a\\a\\g\\d.java (at line 17)
					<T extends j> T b();
					           ^
				j cannot be resolved to a type
				----------
				10. WARNING in org\\a\\a\\g\\d.java (at line 17)
					<T extends j> T b();
					                ^^^
				This method has a constructor name
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug525580_comment28() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.IGNORE);
		runner.testFiles =
			new String[] {
				"xxxxxx/iiibii.java",
				"""
					package xxxxxx;
					
					public class iiibii {
					
					    public <T extends xxxxxx.jajaja> T b041D041D041D041DН041DН(xxxxxx.jjajaa jjajaa) {
					    }
					
					    public xxxxxx.jajaja bН041D041D041DН041DН(byte b, byte b2) {
					        return b041D041D041D041DН041DН(new xxxxxx.jjajaa(b, b2));
					    }
					}
					""",
				"xxxxxx/jjajaa.java",
				"""
					package xxxxxx;
					
					public class jjajaa implements java.io.Serializable, java.lang.Comparable<xxxxxx.jjajaa> {
					    private byte b0445х0445хх04450445;
					    private byte bхх0445хх04450445;
					
					    public jjajaa(byte b, byte b2) {
					        this.bхх0445хх04450445 = b;
					        this.b0445х0445хх04450445 = b2;
					    }
					
					    public int b043704370437з04370437з(xxxxxx.jjajaa jjajaa) {
					        int i = this.bхх0445хх04450445 - jjajaa.bхх0445хх04450445;
					        return i != 0 ? i : this.b0445х0445хх04450445 - jjajaa.b0445х0445хх04450445;
					    }
					
					    public byte[] bззз043704370437з() {
					        return new byte[]{this.bхх0445хх04450445, this.b0445х0445хх04450445};
					    }
					
					    public /* synthetic */ int compareTo(java.lang.Object obj) {
					        return b043704370437з04370437з((xxxxxx.jjajaa) obj);
					    }
					
					    public boolean equals(java.lang.Object obj) {
					        if (obj == null || getClass() != obj.getClass()) {
					            return false;
					        }
					        xxxxxx.jjajaa jjajaa = (xxxxxx.jjajaa) obj;
					        return this.bхх0445хх04450445 == jjajaa.bхх0445хх04450445 && this.b0445х0445хх04450445 == jjajaa.b0445х0445хх04450445;
					    }
					
					    public int hashCode() {
					        return ((this.bхх0445хх04450445 + 427) * 61) + this.b0445х0445хх04450445;
					    }
					
					    public java.lang.String toString() {
					        return xxxxxx.ttotoo.bг04330433г04330433г(this.bхх0445хх04450445) + xxxxxx.ttotoo.bг04330433г04330433г(this.b0445х0445хх04450445);
					    }
					}
					"""
			};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in xxxxxx\\iiibii.java (at line 5)
						public <T extends xxxxxx.jajaja> T b041D041D041D041DН041DН(xxxxxx.jjajaa jjajaa) {
						                  ^^^^^^^^^^^^^
					xxxxxx.jajaja cannot be resolved to a type
					----------
					2. ERROR in xxxxxx\\iiibii.java (at line 8)
						public xxxxxx.jajaja bН041D041D041DН041DН(byte b, byte b2) {
						       ^^^^^^^^^^^^^
					xxxxxx.jajaja cannot be resolved to a type
					----------
					3. ERROR in xxxxxx\\iiibii.java (at line 9)
						return b041D041D041D041DН041DН(new xxxxxx.jjajaa(b, b2));
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from jajaja to jajaja
					----------
					----------
					1. ERROR in xxxxxx\\jjajaa.java (at line 3)
						public class jjajaa implements java.io.Serializable, java.lang.Comparable<xxxxxx.jjajaa> {
						             ^^^^^^
					The type jjajaa must implement the inherited abstract method Comparable<jjajaa>.compareTo(jjajaa)
					----------
					2. WARNING in xxxxxx\\jjajaa.java (at line 3)
						public class jjajaa implements java.io.Serializable, java.lang.Comparable<xxxxxx.jjajaa> {
						             ^^^^^^
					The serializable class jjajaa does not declare a static final serialVersionUID field of type long
					----------
					3. ERROR in xxxxxx\\jjajaa.java (at line 21)
						public /* synthetic */ int compareTo(java.lang.Object obj) {
						                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Name clash: The method compareTo(Object) of type jjajaa has the same erasure as compareTo(T) of type Comparable<T> but does not override it
					----------
					4. ERROR in xxxxxx\\jjajaa.java (at line 38)
						return xxxxxx.ttotoo.bг04330433г04330433г(this.bхх0445хх04450445) + xxxxxx.ttotoo.bг04330433г04330433г(this.b0445х0445хх04450445);
						       ^^^^^^^^^^^^^
					xxxxxx.ttotoo cannot be resolved to a type
					----------
					5. ERROR in xxxxxx\\jjajaa.java (at line 38)
						return xxxxxx.ttotoo.bг04330433г04330433г(this.bхх0445хх04450445) + xxxxxx.ttotoo.bг04330433г04330433г(this.b0445х0445хх04450445);
						                                                                    ^^^^^^^^^^^^^
					xxxxxx.ttotoo cannot be resolved to a type
					----------
					""";
		runner.runNegativeTest();
	}
	public void testBug340506() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
					    public <T> void setValue(Parameter<T> parameter, T value) {
					        System.out.println("Object");
					    }
					
					    public <T> void setValue(Parameter<T> parameter, Field<T> value) {
					        System.out.println("Field");
					    }
					
					    public static void main(String[] args) {
					        new Test().test();
					    }
					
					    private void test() {
					        Parameter<String> p1 = p1();
					        Field<String> f1 = f1();
					        setValue(p1, f1);
							 setValue(p1, null);
					
					        Parameter<Object> p2 = p2();
					        Field<Object> f2 = f2();
					        setValue(p2, f2);
					  		 setValue(p2, null);\
					    }
					
					    private Field<String> f1() {
					        Field<String> f1 = null;
					        return f1;
					    }
					
					    private Parameter<String> p1() {
					        Parameter<String> p1 = null;
					        return p1;
					    }
					
					    private Parameter<Object> p2() {
					        Parameter<Object> p2 = null;
					        return p2;
					    }
					
					    private Field<Object> f2() {
					        Field<Object> f2 = null;
					        return f2;
					    }
					}
					
					interface Field<T> {}
					interface Parameter <T> {}
					"""
			},
			"""
				----------
				1. ERROR in Test.java (at line 18)
					setValue(p1, null);
					^^^^^^^^
				The method setValue(Parameter<String>, String) is ambiguous for the type Test
				----------
				2. ERROR in Test.java (at line 22)
					setValue(p2, f2);
					^^^^^^^^
				The method setValue(Parameter<Object>, Object) is ambiguous for the type Test
				----------
				3. ERROR in Test.java (at line 23)
					setValue(p2, null);    }
					^^^^^^^^
				The method setValue(Parameter<Object>, Object) is ambiguous for the type Test
				----------
				""");
	}
	public void testBug333011() {
		runNegativeTest(
			new String[] {
				"Example.java",
				"""
					import java.util.ArrayList;
					public class Example {
						public static void doSomething() {
							DoJobMr bean = getOnlyElement(new ArrayList());
						}
						public static <T> T getOnlyElement(Iterable<T> iterable) {\s
							return null;
						}
						public static class DoJobMr {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in Example.java (at line 4)
					DoJobMr bean = getOnlyElement(new ArrayList());
					               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from Object to Example.DoJobMr
				----------
				2. WARNING in Example.java (at line 4)
					DoJobMr bean = getOnlyElement(new ArrayList());
					                                  ^^^^^^^^^
				ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
				----------
				""");
	}
	public void testBug537089() {
		runConformTest(
			new String[] {
				"EclipseBug.java",
				"""
					public class EclipseBug {
					    public static <T> void foo(T p1, T p2) {}
					
					    public void shouldCompile() {
					        foo(new int[0], new byte[0]);
					    }
					}
					"""
			});
	}
	public void testBug539329() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Bug539329.java",
				"""
					  import java.util.*;
					 \s
					  public class Bug539329 {
					
					    public static Collection<Class<? extends Interface>> getClasses() {
					      // This yields a compile error in 2018-09, but works in Photon.
					      return Arrays.asList(One.class, Two.class, Three.class);
					    }
					
					    public static Collection<Class<? extends Interface>> getClassesThatWorks() {
					      // This works surprisinly in both versions
					      return Arrays.asList(One.class, Two.class);
					    }
					  }
					
					   class One extends Parent<String> implements Interface { }
					
					  class Two extends Parent<Integer> implements Interface { }
					
					  class Three extends Parent<Object> implements Interface { }
					
					  class Parent<T> { }
					
					  interface Interface { }
					"""
			};
		runner.runConformTest();
	}
	public void testBug543128() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"Bug543128.java",
			"""
				public class Bug543128 {
					static class A {}
					static class B<F, S extends A> extends A {}
					static class C<G extends A> {}
				\t
					public static <H extends A, T> void test(C<? super B<? super T, ? super H>> test)
					{
						test(test); // fails compilation (incorrect)
					}
				}
				"""
		};
		runner.runConformTest();
	}
	public void testBug543820() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"A.java",
			"""
				import java.util.concurrent.atomic.AtomicReference;
				import java.util.Optional;
				public class A {
					private final ThreadLocal<AtomicReference<Optional<Long>>> var =
						ThreadLocal.withInitial(() -> new AtomicReference<>(Optional.empty()));\
				}
				"""
		};
		runner.runConformTest();
	}
	public void testBug540846() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"Test.java",
			"""
				import java.util.*;
				import java.util.stream.*;
				import java.math.*;
				
				public class Test {
				    private List<Object> getRowValues(Map<String, BigDecimal> record, Stream<String> factors) {
				        return Stream.concat(
				            factors.map(f -> {
				                if (f.equals("x")) {
				                    return record.get(f);
				                } else {
				                    return "NM";
				                }
				            }),
				            Stream.of(BigDecimal.ONE)
				        )
				        .map(v -> (v instanceof BigDecimal) ? ((BigDecimal) v).setScale(10, BigDecimal.ROUND_HALF_UP) : v)
				        .collect(Collectors.toList());
				    }
				}
				"""
		};
		runner.runConformTest();
	}
	public void testBug538192() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"Test.java",
			"""
				import java.util.*;
				import java.util.function.Function;
				interface ListFunc<T> extends Function<List<String>, T> {}
				interface MapFunc<T> extends Function<Map<String,String>, T> {}
				class DTT {
					public <T> DTT(Class<T> c, ListFunc<T> f) {}
					public <T> DTT(Class<T> c, MapFunc<T> f) {}\s
				}
				public class Test {
					void test() {
						new DTT(Integer.class, (Map<String, String> row) -> Integer.valueOf(0));
					}
				}
				"""
		};
		runner.runConformTest();
	}
	public void testBug536860() {
		runConformTest(
			new String[] {
				"Snippet.java",
				"""
					import java.io.IOException;
					import java.io.InputStream;
					import java.nio.file.Path;
					import java.util.Map;
					import java.util.concurrent.Callable;
					import java.util.function.Function;
					
					interface EntityReader<T, S> { }
					class ExtraIOUtils {
						public static Callable<InputStream> getInputStreamProvider() {
							return null;
						}
					}
					
					public class Snippet {
						public <T> EntityReader<T, Path> createEntityReader(
						    Function<? super String, ? extends String> colNameMapper,
						    Function<? super String[], ? extends T> instantiator,
						    Map<String, ?> runtimeValues)
						         throws IOException {
						        EntityReader<T, ?> streamReader =
						            createEntityStreamReader(
						            		ExtraIOUtils.getInputStreamProvider(),
						                colNameMapper, instantiator, runtimeValues);
								return null;
						}
						public <T> EntityReader<T, Callable<InputStream>> createEntityStreamReader(
						        Callable<InputStream> streamProvider,
						        Function<? super String, ? extends String> colNameMapper, Function<? super String[], ? extends T> instantiator,
						        Map<String, ?> runtimeValues)
						            throws IOException {
							return null;
						}
					}
					"""
			});
	}
	public void testBug545121() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<T extends V, U extends T, V> void foo(U arg1, T arg2, V arg3) {}
					
						void check() {
							foo((Long) 0l, 0d, "");
						}
					}
					"""
			});
	}
	public void testBug545082a() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"Test.java",
			"""
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				
				public class Test {
					public static void main(String[] args)
					{
					  println(Stream.of(42).collect(Collectors.summingDouble(d -> d)));\s
					}
					public static void println(double x) {}
					public static void println(char[] x) {}
					public static void println(String x) {}
					public static void println(Object x) {}\s
				}
				"""
		};
		runner.runConformTest();
	}
	public void testBug545082b() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"Test.java",
			"""
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				
				public class Test {
					char[] f;\
					public void test() {
					  f = Stream.of(42).collect(Collectors.summingDouble(d -> d));\s
					}
				}
				"""
		};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in Test.java (at line 6)
						f = Stream.of(42).collect(Collectors.summingDouble(d -> d));\s
						    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from Double to char[]
					----------
					""";
		runner.runNegativeTest();
	}
	public void testBug512156_3() {
		runConformTest(
			new String[] {
				"TestFor3TypeParameters.java",
				"""
					import java.util.Objects;
					import java.util.stream.Stream;
					import java.util.stream.StreamSupport;
					
					/**
					 * For comprehension for 3 iterables. Adapted from the http://javaslang.io library to help finding JDT performance bottlenecks.
					 *
					 */
					public class TestFor3TypeParameters {
					
					    public interface Function3<T1, T2, T3, R> {
					        R apply(T1 t1, T2 t2, T3 t3);
					    }   \s
					   \s
					    public static class For3<T1, T2, T3> {
					
					        private final Iterable<T1> ts1;
					        private final Iterable<T2> ts2;
					        private final Iterable<T3> ts3;
					
					        private For3(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3) {
					            this.ts1 = ts1;
					            this.ts2 = ts2;
					            this.ts3 = ts3;
					        }
					
					        /**
					         * Yields a result for elements of the cross product of the underlying Iterables.
					         *
					         * @param f
					         *            a function that maps an element of the cross product to a result
					         * @param <R>
					         *            type of the resulting {@code Iterator} elements
					         * @return an {@code Iterator} of mapped results
					         */
					        public <R> Stream<R> yield(
					                Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
					            Objects.requireNonNull(f, "f is null");
					            return this.stream(ts1)
					                .flatMap(t1 ->
					                    stream(ts2).flatMap(t2 ->\s
					                        stream(ts3).map(t3 ->
					                            f.apply(t1, t2, t3)
					                        )
					                    )
					                );
					        }
					
					        private <T> Stream<T> stream(Iterable<T> iterable) {
					            return StreamSupport.stream(iterable.spliterator(), false);
					        }
					
					    }
					}
					"""
			});
	}
	public void testBug512156_10() {
		runConformTest(
			new String[] {
				"Test10.java",
				"""
					import java.util.Objects;
					import java.util.stream.Stream;
					import java.util.stream.StreamSupport;
					
					/**
					 * For comprehension for 10 iterables. Adapted from the http://javaslang.io library to help finding JDT performance bottlenecks.
					 *
					 */
					public class Test10 {
					
					    public interface Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> {
					        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10);
					    }   \s
					   \s
					    public static class For10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
					
					        private final Iterable<T1> ts1;
					        private final Iterable<T2> ts2;
					        private final Iterable<T3> ts3;
					        private final Iterable<T4> ts4;
					        private final Iterable<T5> ts5;
					        private final Iterable<T6> ts6;
					        private final Iterable<T7> ts7;
					        private final Iterable<T8> ts8;
					        private final Iterable<T9> ts9;
					        private final Iterable<T10> ts10;
					
					        private For10(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6,
					                Iterable<T7> ts7, Iterable<T8> ts8, Iterable<T9> ts9, Iterable<T10> ts10) {
					            this.ts1 = ts1;
					            this.ts2 = ts2;
					            this.ts3 = ts3;
					            this.ts4 = ts4;
					            this.ts5 = ts5;
					            this.ts6 = ts6;
					            this.ts7 = ts7;
					            this.ts8 = ts8;
					            this.ts9 = ts9;
					            this.ts10 = ts10;
					        }
					
					        /**
					         * Yields a result for elements of the cross product of the underlying Iterables.
					         *
					         * @param f
					         *            a function that maps an element of the cross product to a result
					         * @param <R>
					         *            type of the resulting {@code Iterator} elements
					         * @return an {@code Iterator} of mapped results
					         */
					        public <R> Stream<R> yield(
					                Function10<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? extends R> f) {
					            Objects.requireNonNull(f, "f is null");
					            return this.stream(ts1)
					                .flatMap(t1 ->
					                    stream(ts2).flatMap(t2 ->\s
					                        stream(ts3).flatMap(t3 ->\s
					                            stream(ts4).flatMap(t4 ->\s
					                                stream(ts5).flatMap(t5 ->\s
					                                    stream(ts6).flatMap(t6 ->\s
					                                        stream(ts7).flatMap(t7 ->\s
					                                            stream(ts8).flatMap(t8 ->
					                                            	stream(ts9).flatMap(t9 ->
					                                            		stream(ts10).map(t10 -> /**/
					                                                		f.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
					                                                	)
					                                                )
					                                            )
					                                        )
					                                    )
					                                )
					                            )
					                        )
					                    )
					                );
					        }
					
					        private <T> Stream<T> stream(Iterable<T> iterable) {
					            return StreamSupport.stream(iterable.spliterator(), false);
					        }
					
					    }
					}
					"""
			});
	}
	public void testBug547061() {
		runConformTest(
			new String[] {
				"test2/Problematic.java",
				"""
					package test2;
					
					import java.io.IOException;
					import java.util.Collections;
					import java.util.Set;
					import java.util.function.Consumer;
					
					public class Problematic {
					
						@FunctionalInterface
						private interface ThrowingConsumer<T, E extends Throwable> {
							void accept(T t) throws E;
						}
					
						private class FileAsset {
							public FileAsset move(String path) throws IOException {
								System.out.println(path);
								return null;
							}
						}
					
						static <T, E extends Exception> void process(Consumer<Consumer<T>> code, ThrowingConsumer<T, E> throwingConsumer)
								throws E {
							code.accept(t -> {
								try {
									throwingConsumer.accept(t);
								} catch (Exception e) {
									e.printStackTrace();
								}
							});
						}
					
						public void execute(String path) throws IOException {
							Set<FileAsset> set = Collections.singleton(new FileAsset());
							process(set::forEach, (asset) -> {
								process(set::forEach, (asset2) -> {
									asset2.move(path);
								});
							});
					
						}
					}
					"""
			});
	}
	public void testBug545420() {
		runConformTest(
			new String[] {
				"Main.java",
				"""
					public class Main {
						public static void main(String[] args) { \s
							System.out.println(new Main().getDetailCellCssFactory().getReturn());
						}
					
						public FIReturnType getDetailCellCssFactory() {
						\t
							return 	method1(()-> {
										return  () ->{
												return "something";
										};
								});
						}
					\t
						public <X> X method1(FIWithGenerics<X> init) {
							return init.init();	\t
						}
					}
					interface FIReturnType {
						String getReturn();
					}
					interface FIWithGenerics<X> {
						 X init();
					}
					"""
			});
	}
	public void testBug525822() {
		runNegativeTest(
			new String[] {
				"ECJTest.java",
				"""
					import java.util.*;
					import java.util.function.*;
					
					public class ECJTest {
					
						static {
							final List<String> list = new ArrayList<>();
							accept(list::add);
						}
					
						static void accept(Consumer<String> yay) {};
						static void accept(BiConsumer<String, String> nooo) {};
					}
					"""
			},
			"""
				----------
				1. ERROR in ECJTest.java (at line 8)
					accept(list::add);
					^^^^^^
				The method accept(Consumer<String>) is ambiguous for the type ECJTest
				----------
				""");
	}
	public void testBug502327() {
		runConformTest(
			new String[] {
				"Bug.java",
				"""
					public class Bug {
					
					  public void execute() {
					    foo(bar(new ExampleType()));
					  }
					
					  public <VC> void foo(ClassB<VC> a) {}
					
					  public <T> ClassC<T> bar(T t) {
					    return null;
					  }
					
					  public class ClassC<T> extends ClassB<ClassC<T>.NestedClassC> {
					    public class NestedClassC {}
					  }
					
					  public abstract static class ClassB<VC> {}
					
					  public class ExampleType {}
					}
					"""
			});
	}
	public void testBug547807() {
		runConformTest(
			new String[] {
				"DslStep1.java",
				"""
					public interface DslStep1 {
						DslStep2<?> nextStep();
					}
					""",
				"DslStep2.java",
				"""
					public interface DslStep2<S extends DslStep2<? extends S>> {
						S doSomething();
					}
					""",
				"CallBug.java",
				"""
					public class CallBug {
						public void doesNotCompileWithEcj(DslStep1 step1) {
							// Note we need three chained calls for the problem to show up. Two is not enough.
							step1.nextStep().doSomething().doSomething().doSomething();
						}
					}
					"""
			});
	}
	public void testBug548589() {
		runConformTest(
			new String[] {
				"InferenceCheck.java",
				"""
					import java.util.*;
					import java.util.function.*;
					
					public class InferenceCheck {
					
					    public interface P<T> {
					
					        public boolean apply(T value);       \s
					    }
					
					    public static <T> P<T> compilation_failed(P<T> predicate) {
					        List<P<T>> list = Collections.emptyList();
					        list.stream().map(InferenceCheck::compilation_failed);
					        return null;
					    }   \s
					
					    public static <T> P<T> compilation_ok(P<T> predicate) {
					        List<P<T>> list = Collections.emptyList();
					        Function<P<T>, P<T>> f = InferenceCheck::compilation_ok;
					        list.stream().map(f);
					        return null;
					    }   \s
					}
					"""
			});
	}

	public void testBug534466() {
		runNegativeTest(
			new String[] {
				"test/TODO.java",
				"""
					package test;
					public interface TODO {
					    boolean test();
					}
					""",
				"test/FuncN.java",
				"""
					package test;
					
					@FunctionalInterface
					public interface FuncN {
					  State zip(State ...states);
					}
					""",
				"test/Test.java",
				"""
					package test;
					public class Test {
					
					    public static Test define(FuncN zipperFunc,TODO... tasks) {
					        return null;
					    }
					
					    public static Test define(TODO... tasks) {
					        return null;
					    }
					}
					""",
				"test/State.java",
				"""
					package test;
					public class State {
					    public static State mergeStates(State ...states) {
					        return null;
					    }
					}
					""",
				"test/Main.java",
				"""
					package test;
					
					public class Main {
					    public static void main(String[] args) {
					      Test.define(State::mergeStates,()->true);
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in test\\Main.java (at line 5)
					Test.define(State::mergeStates,()->true);
					     ^^^^^^
				The method define(FuncN, TODO[]) is ambiguous for the type Test
				----------
				""");
	}

	public void testBug534223() {
		Runner runner = new Runner();
		String sourceX =
				"""
			package p;
			public class X {
				<S> void m() {
					Runnable r = () -> {
						IFC<S> i = new IFC<S>() {
							public void n(S s) {}
						};
						if (i != null)
							System.out.println(i);
					};
					r.run();
				}
			}
			""";
		runner.testFiles = new String[] {
				"p/IFC.java",
				"""
					package p;
					public interface IFC<T> {
						void n(T t);
					}
					""",
				"p/X.java",
				sourceX
			};
		runner.runConformTest();
		runner.shouldFlushOutputDirectory = false;
		runner.testFiles = new String[] {
				"p/X.java",
				sourceX
			};
		runner.runConformTest(); // don't use pre-compiled p/X$1.class
	}

	public void testBug559449() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						class $$$ {}
						<S> void m() {
							Runnable r = () -> {
								$$$ ddd = new $$$();
								if (ddd != null)
									System.out.println(ddd);
							};
							r.run();
						}
					}
					"""
			};
		runner.runConformTest();
	}
	public void testBug559677() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"MyClass.java",
			"""
				public class MyClass {
					private void myRun() {
					}
					private void myMethod(final Runnable r) {
					}
					public void test() {
						// second opening brace causes endless loop while saving
						myMethod((this::myRun);
					}
				}
				"""
		};
		runner.performStatementsRecovery = true;
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in MyClass.java (at line 8)
					myMethod((this::myRun);
					                     ^
				Syntax error, insert ")" to complete Expression
				----------
				""";
		runner.runNegativeTest();
	}
	public void testBug559951() {
		if (this.complianceLevel < ClassFileConstants.JDK10) return; // uses 'var'
		runConformTest(
			new String[] {
				"no/Demo.java",
				"""
					package no;
					public class Demo {
						static void broken_method_dispatch_on_bounded_type_in_lambda_argument_with_Eclipse_compiler() {
							WithMessageRecipients withRecipients = new Message(new EmailRecipient("Jane", "jane@example.com"), new EmailRecipient("Joe", "joe@example.com"));
					
							withRecipients.getMessageRecipients()
								.stream()
								.forEach(recipient -> System.out.println(recipient.getName() + " <" + recipient.getEmailAddress() + ">"));
						}
						static void works_fine_in_for_loop() {
							WithMessageRecipients withRecipients = new Message(new EmailRecipient("Jane", "jane@example.com"), new EmailRecipient("Joe", "joe@example.com"));
					
							for (var recipient : withRecipients.getMessageRecipients()) {
								System.out.println(recipient.getName() + " <" + recipient.getEmailAddress() + ">");
							}
						}
						public static void main(String... args) {
							works_fine_in_for_loop();
							broken_method_dispatch_on_bounded_type_in_lambda_argument_with_Eclipse_compiler();
						}
					}
					""",
				"no/WithName.java",
				"""
					package no;
					public interface WithName {
						String getName();
					}""",
				"no/WithEmailAddress.java",
				"""
					package no;
					public interface WithEmailAddress {
						String getEmailAddress();
					}
					""",
				"no/WithMessageRecipients.java",
				"""
					package no;
					import java.util.List;
					public interface WithMessageRecipients {
						<CONTACT extends WithName & WithEmailAddress> List<? extends CONTACT> getMessageRecipients();
					}""",
				"no/EmailRecipient.java",
				"""
					package no;
					public class EmailRecipient implements WithName, WithEmailAddress {
						private final String name;
						private final String emailAddress;
						public EmailRecipient(String name, String emailAddress) {
							this.name = name;
							this.emailAddress = emailAddress;
						}
						@Override
						public String getEmailAddress() {
							return emailAddress;
						}
						@Override
						public String getName() {
							return name;
						}
					}""",
				"no/Message.java",
				"""
					package no;
					import java.util.List;
					public class Message implements WithMessageRecipients {
						private final List<EmailRecipient> recipients;
						public Message(EmailRecipient ... recipients) {
							this.recipients = List.of(recipients);
						}
						@Override
						public List<EmailRecipient> getMessageRecipients() {
							return recipients;
						}
					}"""
			},
			"""
				Jane <jane@example.com>
				Joe <joe@example.com>
				Jane <jane@example.com>
				Joe <joe@example.com>""");
	}
	public void testBug560566() {
		runNegativeTest(
			new String[] {
				"Tester.java",
				"""
					import java.util.ArrayList;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;
					
					public class Tester {
					    {
					        Stream.empty().collect(Collectors.toList(ArrayList::new));
					    }
					}"""
			},
			"""
				----------
				1. ERROR in Tester.java (at line 7)
					Stream.empty().collect(Collectors.toList(ArrayList::new));
					                                  ^^^^^^
				The method toList() in the type Collectors is not applicable for the arguments (ArrayList::new)
				----------
				""");
	}
	public void testBug568259() {
		runNegativeTest(
				new String[] {
						"Main.java",
						"""
							public final class Main<T extends Object> {
							    public static <T extends Object> Main<T> invoke(Object o) {
							        return null;
							    }
							    public void test() {
							        invoke(new Main.Inner());
							    }\s
							}"""
					},
				"""
					----------
					1. ERROR in Main.java (at line 6)
						invoke(new Main.Inner());
						           ^^^^^^^^^^
					Main.Inner cannot be resolved to a type
					----------
					""");
	}
	public void testBug562324comment31() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.util.stream.Collectors.*;
					import static java.util.stream.Stream.*;
					
					import java.util.stream.Stream;
					
					public class X {
					
					    public void hello() {
					    	Runnable r = new Runnable () {
					    		@Override
					    		public void run() {
					    		}
					    	};
					    	r.run();
					    }
					
					    static void bug() {
					        Stream<String> stream = of(""); // error here
					    }
					}
					"""
			});
	}
	public void testBug573933() {
		runConformTest(
			new String[] {
				"B.java",
				"import java.util.List;\n" +
				"import java.util.function.Function;\n" +
				"import java.util.stream.Collectors;\n" +
				"public class B {\n" +
				 "   List<M> r;\n" +
				  "  B(final RC c) {\n" +
				  "  	r = m(c.getRI(), i -> t(i, x -> new M(x))); // no error\n" +
				  "  	r = m(c.getRI(), i -> t(i, M::new)); \n" +
				  "  } \n" +
				  "  static <T, U> U m(T t, Function<T, U> f) {\n" +
				  "      return f.apply(t);\n" +
				  "  }\n" +
				  "  static <T, R> List<R> t(final List<T> list, final Function<T, R> function) {\n" +
				  "      return list.stream().map(function).collect(Collectors.toList());\n" +
				  "  }\n" +
				"}\n" +
				"class RC {\n" +
				"    List<Integer> getRI() { return null; }\n" +
				"}\n" +
				"class M {\n" +
				 "   Integer r;\n" +
				 "   public M(final Integer r) {\n" +
				  "  	this.r = r;\n" +
				  "  }\n" +

				    // Removing this constructor makes the problem go away
				  "  public M(final RC i) {\n" +
				  "  	this.r = 3;\n" +
				  "  }\n" +
				  "}"
			});

	}

	public void testBug573378() {
		runNegativeTest(
			new String[] {
				"TypeInferenceError.java",
				"""
					import java.util.*;
					import java.util.function.*;
					import java.util.stream.*;
					
					public class TypeInferenceError {
					  void test() {
					    Optional<Stream<Object>> s = Optional.empty();
					    map(s, Stream::count);
					    assertThat(map(s, Stream::count));
					  }
					  private <T> OptionalInt map(Optional<T> o, ToIntFunction<T> mapper) {
					    return OptionalInt.empty();
					  }
					  private void assertThat(OptionalInt o) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in TypeInferenceError.java (at line 8)
					map(s, Stream::count);
					       ^^^^^^^^^^^^^
				The type of count() from the type Stream<Object> is long, this is incompatible with the descriptor's return type: int
				----------
				2. ERROR in TypeInferenceError.java (at line 9)
					assertThat(map(s, Stream::count));
					                  ^^^^^^^^^^^^^
				The type of count() from the type Stream<Object> is long, this is incompatible with the descriptor's return type: int
				----------
				""");
	}
	public void testBug549446() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return; // uses interface Constable
		runConformTest(
			new String[] {
				"TestFile.java",
				"""
					import java.lang.constant.Constable;
					public class TestFile {
					
					  @SafeVarargs
					  public final <E> E elements(E... args) {
					    return null;
					  }
					
					  public void test1() {
					    var v = elements("a", 1);
					  }
					
					  public void test2() {
					    var v = elements("a", (Comparable<String> & Constable) null);
					  }
					}
					"""
			});
	}
	public void testBug576516() {
		if (this.complianceLevel < ClassFileConstants.JDK11) return; // uses 'var'
		runConformTest(
			new String[] {
				"lib/Base.java",
				"package lib;\n" +
				"public class Base {}\n",

				"lib/ClassA.java",
				"""
					package lib;
					import lib.Holder.Tagging;
					public class ClassA extends Base implements Tagging { }
					""",

				"lib/ClassB.java",
				"""
					package lib;
					import lib.Holder.Tagging;
					public class ClassB extends Base implements Tagging { }
					""",

				"lib/Holder.java",
				"""
					package lib;
					public class Holder  {
					    interface Tagging { }
					}""",

				"Test.java",
				"""
					import java.util.stream.Stream;
					import lib.ClassA;
					import lib.ClassB;
					public class Test {
					
					    public static void main(String[] args) {
					        var builders = Stream.of(new ClassA(), new ClassB());
					    }
					}
					"""
			});
	}
	public void testBug543842() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.math.BigDecimal;
					import java.util.Optional;
					import java.util.function.Function;
					
					public class X {
						void test() {
							BigDecimal b =
							Optional.ofNullable(BigDecimal.ZERO)
								.map((1 == 2) ? BigDecimal::negate : java.util.function.Function.identity())
								.orElse(BigDecimal.ZERO);
						}
					}
					"""
			});
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/506
	// Verify that ECJ can infer the case including nested generic method invocation.
	public void testGH506_a() {
		this.runConformTest(
			new String[] {
				"Convert.java",
				"""
					import java.util.function.Function;
					class Convert<A> {
					    public static <B> void test(final B a) {
					        Convert<B> res1 = convert(arg -> create(arg), a); // ok
					
					        Convert<B> res2 = convert(arg -> wrap(create(arg)), a); // error: Type mismatch
					    }
					
					    public static <C, D> Convert<D> convert(final Function<C, Convert<D>> func, final C value) {
					        return null;
					    }
					
					    public static <E> E wrap(final E a) {
					        return null;
					    }
					
					    public static <F> Convert<F> create(final F initial) {
					        return null;
					    }
					}"""
			}
		);
	}

	// Verify that ECJ still infers well when the nested method invocation is a varargs method.
	public void testGH506_b() {
		this.runConformTest(
			new String[] {
				"Convert.java",
				"""
					import java.util.function.Function;
					class Convert<A> {
					    public static <B> void test(final B a) {
					        Convert<B> res1 = convert(arg -> create(arg), a); // ok
					
					        Convert<B> res2 = convert(arg -> wrap(create(arg)), a); // error: Type mismatch
					    }
					
					    public static <C, D> Convert<D> convert(final Function<C, Convert<D>> func, final C value) {
					        return null;
					    }
					
					    public static <E> E wrap(final E a) {
					        return null;
					    }
					
					    public static <F> Convert<F> create(final F... initial) {
					        return null;
					    }
					}"""
			}
		);
	}
	public void testGH1261() {
		runConformTest(
			new String[] {
				"GH1261.java",
				"""
				import java.io.IOException;
				import java.util.AbstractMap;
				import java.util.function.BiConsumer;
				import java.util.Collections;
				import java.util.Map;

				class Matcher<T extends Object> {}

				public class GH1261 {
					private static final Map<String, String> PARAMETER_VALUE_STRINGS_BY_VALUE = Collections.emptyMap();

					public void testParameterAppendValueTo() throws IOException {
						PARAMETER_VALUE_STRINGS_BY_VALUE.forEach(throwingBiConsumer((value, valueString) -> {
							assertThat(Parameter.appendValueTo(new StringBuilder(), value).toString(), is(valueString));
						}));
					}
					public static <T, R, X extends Throwable> ThrowingBiConsumer<T, R, X> throwingBiConsumer(
							final ThrowingBiConsumer<T, R, X> consumer) {
						return consumer;
					}
					public static <T extends java.lang.Object> Matcher<T> is(Matcher<T> m) { return null; }
					public static <T extends java.lang.Object> Matcher<T> is(T t) { return null; }

					public static <T extends java.lang.Object> void assertThat(T t, Matcher<? super T> m) { }
					// original has overloads of assertThat, which are not relevant here
				}
				interface ThrowingBiConsumer<T, U, X extends Throwable> extends BiConsumer<T, U> {
					void tryAccept(T t, U u) throws X;
					default void accept(final T t, final U u) { }
				}
				@SuppressWarnings("serial")
				final class Parameter extends AbstractMap.SimpleImmutableEntry<String, String> {
					public static <A extends Appendable> A appendValueTo(final A appendable, CharSequence parameterValue) throws IOException {
						return appendable;
					}
					public Parameter(final String name, final String value) {
						super("", "");
					}
				}
				"""
			});
	}
	public void testGH973() {
		runConformTest(
			new String[] {
				"Seq.java",
				"""
				import java.util.List;
				import java.util.function.Consumer;
				import java.util.function.Function;

				public interface Seq<T> {

					void consume(Consumer<T> consumer);

					default <R> Seq<R> map(Function<T, R> mapFunction) {
						return c -> consume(t -> c.accept(mapFunction.apply(t)));
					}

					default <R> Seq<R> flatMap(Function<T, Seq<R>> mapFunction) {
						return c -> consume(t -> mapFunction.apply(t).consume(c));
					}

					static void main(String[] args) {
						Seq<Integer> seq = List.of(1, 2, 3)::forEach;
						seq.map(e -> e * 2).flatMap(e -> List.of(e - 1, e)::forEach).consume(System.out::println);
					}

				}
				"""
			});
	}
	public void testGH1427_class() {
		runConformTest(
			new String[] {
				"Main.java",
				"""
				import java.util.List;
				import java.util.function.Supplier;

				public class Main {
				    private static final List<Pair<Supplier<String>>> ATTRIBUTE = List.of(new Pair<>(String::new));

				    static class Pair<T> { Pair(T first) {} }

				    public static void main(String[] args) {}
				}
				"""
			});
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1794
	// Remove redundant type arguments in lambda expressions leads to type mismatch error
	public void testGH1794() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
		runNegativeTest(
			false /*skipJavac */,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
			new String[] {
				"TypeArgumentsTest.java",
				"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.stream.Collectors;

				public class TypeArgumentsTest {
					public static void main(String[] args) {
						List<String> strings = List.of("string1", "string2");
						ArrayList<ArrayList<String>> collectedStrings = strings.stream()
								.map(s -> new ArrayList<String>())
								.collect(Collectors.toCollection(() -> new ArrayList<>(strings.size())));
						System.out.println(collectedStrings);
					}
				}
				"""
			},
			"",
			null, true, customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576002
	// Mandatory Void Type gets eliminated
	public void testBug576002() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
		runNegativeTest(
			false /*skipJavac */,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
			new String[] {
				"Test.java",
				"""
				import java.util.ArrayList;
				import java.util.List;
				import java.util.concurrent.FutureTask;
				import java.util.stream.Collectors;

				public class Test {
					List<FutureTask<Void>> tasks = new ArrayList<>().stream().map(e -> new FutureTask<Void>(() -> {
						return null;
					})).collect(Collectors.toList());
				}
				"""
			},
			"",
			null, true, customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=550864
	// [1.8][inference] Removing "redundant" type argument results in compile error
	public void testBug550864() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
		runNegativeTest(
			false /*skipJavac */,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
			new String[] {
				"TypeArgBug.java",
				"""
				import java.util.Comparator;
				import java.util.List;

				public class TypeArgBug {

				  public static void main(String[] args) {

				    // Allowed (with type specification in lambda)
				    List<Order<Descriptor>> x = List.of(
				      new Order<>(
				        "best",
				        Comparator.comparing((Item<Descriptor> item) -> ((Basket)item.getData()).getWeaveCount())
				      )
				    );

				    // Allowed, but gives warning (redundant type argument for Order<Descriptor>)
				    List<Order<Descriptor>> y = List.of(
				      new Order<Descriptor>(
				        "best",
				        Comparator.comparing(item -> ((Basket)item.getData()).getWeaveCount())
				      )
				    );

				    // Compiler error after removing redundant argument...
				    List<Order<Descriptor>> z = List.of(
				      new Order<>(
				        "best",
				        Comparator.comparing(item -> ((Basket)item.getData()).getWeaveCount())
				      )
				    );
				  }

				  public interface Descriptor {
				  }

				  public static class Order<T extends Descriptor> {
				    public final String resourceKey;
				    public final Comparator<Item<T>> comparator;

				    public <G extends Comparable<G>> Order(String resourceKey, Comparator<Item<T>> comparator) {
				      this.resourceKey = resourceKey;
				      this.comparator = comparator;
				    }
				  }

				  public static class Item<T extends Descriptor> {
				    private final T data;

				    public Item(T data) {
				      this.data = data;
				    }

				    public T getData() {
				      return data;
				    }
				  }

				  public static class Basket implements Descriptor {
				    public int getWeaveCount() {
				      return 5;
				    }
				  }
				}
				"""
			},
			"""
				----------
				1. ERROR in TypeArgBug.java (at line 18)
					new Order<Descriptor>(
					    ^^^^^
				Redundant specification of type arguments <TypeArgBug.Descriptor>
				----------
				""",
			null, true, customOptions);
	}
	public void testGH1475() {
		runConformTest(
			new String[] {
				"CannotInferTypeArguments.java",
				"""
				public class CannotInferTypeArguments<V extends java.util.concurrent.Semaphore> {
					class Fish {
						public V getFlavour() {
							return null;
						}
					}

					class Shark<E extends Fish> {
					}

					<E extends Fish> Shark<E> fish() {
						// This compiles fine with javac, but will only work in Eclipse with new Shark<E>();
						return new Shark<>();
					}

					<E extends Fish> Shark<E> fish2() {
						Shark<E> s = new Shark<>();
						return s;
					}
				}
				"""
			});
	}

	public void testBug569231() {
		runConformTest(
			new String[] {
				"GenericsBug.java",
				"""
				import java.util.function.Function;
				import java.util.function.Predicate;

				public class GenericsBug<S> {
					public static interface MyInterface<U> {}

					public static class SubClass<U,V> implements MyInterface<V>{
						public SubClass(Function<U,V> g, MyInterface<V>... i) { }
					}

					public static class OptSubClass<U> implements MyInterface<U> {
						public OptSubClass(String s, Predicate<U> p, MyInterface<U>... i) { }

					}

					public static class ParamClass<T> {
						public T    getU()    { return null;}
					}

					GenericsBug(MyInterface<S> in1, MyInterface<S> in2) { }


					public static class MySubClass extends SubClass<ParamClass<Boolean>,Boolean> {
						public MySubClass() {
							super(ParamClass::getU);
						}
					}

					public static void foo() {
						SubClass<ParamClass<Boolean>,Boolean> sc = new SubClass<>(ParamClass::getU);
						new GenericsBug<>(new MySubClass(),
										  new OptSubClass<>("foo", t->t, sc));
					}
				};
				"""
			});
	}

	public void testBug566989() {
		runConformTest(
			new String[] {
				"InferTypeTest.java",
				"""
				import java.util.*;
				public class InferTypeTest<T> {

					@FunctionalInterface
					interface DataLoader<T> {
						List<T> loadData(int offset, int limit);
					}

					class DataList<T> extends ArrayList<T>{
						public DataList(DataLoader<T> dataLoader) {
						}
					}

					void testDataList() {
						List<String> list = new ArrayList<>(new DataList<>((offset, limit) -> Collections.emptyList()));
					}

				}
				"""
			});
	}

	public void testBug509848() {
		runConformTest(
			new String[] {
				"Generics.java",
				"""
				public class Generics {

					public MyGeneric<?> test() {
						boolean maybe = false;

						return lambda((String result) -> {
							if (maybe) {
								return new MyGeneric<>(MyGeneric.of(null));
							}
							else {
								return new MyGeneric<>(MyGeneric.of(""));
							}
						});
					}

					static class MyGeneric <T> {
						T t;
						public MyGeneric(MyGeneric<T> t) {
						}
						public static <R> MyGeneric<R> of(R t) {
							return null;
						}
					}

					public <R> MyGeneric<R> lambda(java.util.function.Function<String, MyGeneric<R>> mapper) {
						return null;
					}
				}
				"""
			});
	}

	public void testGH2386() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"TestClass.java",
			"""
			public class TestClass<E> {
			    class Itr { }
			    class C123172 extends TestClass.Itr<Missing<E>> { }
			}
			"""
		};
		runner.expectedCompilerLog = """
			----------
			1. ERROR in TestClass.java (at line 3)
				class C123172 extends TestClass.Itr<Missing<E>> { }
				                      ^^^^^^^^^^^^^
			The type TestClass.Itr is not generic; it cannot be parameterized with arguments <Missing<E>>
			----------
			2. ERROR in TestClass.java (at line 3)
				class C123172 extends TestClass.Itr<Missing<E>> { }
				                                    ^^^^^^^
			Missing cannot be resolved to a type
			----------
			""";
		runner.runNegativeTest();
	}

	public void testGH2399() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"TestClass.java",
			"""
			public class TestClass implements TestClass.Missing1<TestClass.Missing2<TestClass.Missing3>> {
			}
			"""
		};
		runner.expectedCompilerLog = """
			----------
			1. ERROR in TestClass.java (at line 1)
				public class TestClass implements TestClass.Missing1<TestClass.Missing2<TestClass.Missing3>> {
				                                  ^^^^^^^^^^^^^^^^^^
			Cycle detected: the type TestClass cannot extend/implement itself or one of its own member types
			----------
			""";
		runner.runNegativeTest();
	}
}
