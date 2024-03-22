/*******************************************************************************
 * Copyright (c) 2015, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 446691 - [1.8][null][compiler] NullPointerException in SingleNameReference.analyseCode
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class LambdaRegressionTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test572873a", "test572873b"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaRegressionTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.HashMap;
					import java.util.Map;
					import java.util.function.Function;
					public class X {
					  public static void main(String[] args) {
					    new X().run();
					  }
					  public void run() {
					    class Inner {
					      public Inner() {
					        System.out.println("miep");
					      }
					    }
					    Map<String, Inner> map = new HashMap<>();
					    Function<String, Inner> function = (name) -> {
					      Inner i = map.get(name);
					      if (i == null) {
					        i = new Inner();
					        map.put(name, i);
					      }
					      return i;
					
					    };
					    function.apply("test");
					  }
					}
					""",
			},
			"miep"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Consumer;
					@SuppressWarnings("all")
					public class X {
					  private final String text = "Bug?";
					  public static void main(String[] args) {
					    new X().doIt();
					  }
					  private void doIt() {
					    new Sub();
					  }
					  private class Super<T> {
					    public Super(Consumer<T> consumer) {
					    }
					  }
					  private class Sub extends Super<String> {
					    public Sub() {
					      super(s -> System.out.println(text));
					      // super(s -> System.out.println("miep"));
					    }
					  }
					}
					""",
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Consumer;
					@SuppressWarnings("all")
					public class X {
					  private final String text = "Bug?";
					  public static void main(String[] args) {
					    new X().doIt();
					  }
					  private void doIt() {
					    new Sub();
					  }
					  private class Super<T> {
					    public Super(Consumer<T> consumer) {
					    }
					  }
					  private class Sub extends Super<String> {
					    public Sub() {
					       super(s -> System.out.println("miep"));
					    }
					  }
					}
					""",
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test004() {
	this.runConformTest(
		false,
		JavacHasABug.JavacThrowsAnException,
		new String[] {
			"Y.java",
			"""
				import java.util.function.Supplier;
				class E {
					E(Supplier<Object> factory) { }
				}
				public class Y extends E {
					Y() {
						super( () -> {
							class Z extends E {
								Z() {
									super(() -> new Object());
								}
							}
							return null;
							});
					}
					public static void main(String[] args) {
						new Y();
					}
				}"""
	},
	null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448724, [1.8] [compiler] Wrong resolution of overloaded method when irrelevant type parameter is present and lambda is used as parameter
public void test448724() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.concurrent.Callable;
				public class X {
					public void mismatchRunnableCallable() throws Exception {
						//Resolves to case1(Runnable) method invocation; lambda with block
						case1(() -> {"abc".length();});
						//Resolves to case1(Callable) method invocation, resulting in type mismatch; block removed - lambda with expression
				                case1(() -> "abc".length());
					}
					public void noSuchMismatch() throws Exception {
						//no difference to case1\s
						case2(() -> {"abc".length();});
						//the only difference to case 1 is the missing irrelevant <T> type parameter. Properly resolves to case2(Runnable) here
						case2(() -> "abc".length());
					}
					public void case1(final Runnable r) {
						System.out.println("case1: Runnable");
					}
					public <T> void case1(Callable<Boolean> c) {
						System.out.println("case1: Callable");
					}
					public void case2(final Runnable supplier) {
						System.out.println("case2: Runnable");
					}
					public void case2(Callable<Boolean> conditionEvaluator) {
						System.out.println("case2: Callable");
					}
					public static void main(String[] args) throws Exception {
						new X().mismatchRunnableCallable();
						new X().noSuchMismatch();
					}
				}
				"""
	},
	"""
		case1: Runnable
		case1: Runnable
		case2: Runnable
		case2: Runnable""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T, U, V> {
					T goo(U u, V v);
				}
				public class X {
					static <T, U, V> T foo(T t, U u, V v) {
				       System.out.println("Wrong!");
				       return null;
				   }
					static <T, U, V> V foo(T t, U u, I<T, U, V> i) {
						System.out.println("Right!");
				       return null;
					}
					public static void main(String[] args) {
						String s = goo(foo("String", "String", (u, v) -> v));
					}
					static <T> T goo(T t) {
					    return t;\t
					}
				}
				"""
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I<T, U, V> {
					T goo(U u, V v);
				}
				public class X {
					static <T, U, V> T foo(T t, U u, I<T, U, V> i) {
						return null;
					}
					public static void main(String[] args) {
						String s = goo(foo("String", "String", (u, v) -> v));
					}
					static <T> T goo(T t) {
					    return t;\t
					}
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 9)
			String s = goo(foo("String", "String", (u, v) -> v));
			                                                 ^
		Type mismatch: cannot convert from Object to String
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T, U, V> {
					T goo(U u, V v);
				}
				public class X {
					static String goo(String s, String s2) {
						return null;
					}
					static <T, U, V> V foo(T t, U u, I<T, U, V> i) {
						System.out.println("Right!");
						return null;
					}
					public static void main(String[] args) {
						String s = goo(foo("String", "String", X::goo));
					}
					static <T> T goo(T t) {
					    return t;\t
					}
				}
				"""
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T, U, V> {
					T goo(U u, V v);
				}
				public class X {
					static String goo(String s, String s2) {
						return null;
					}
					static <T, U, V> T foo(T t, U u, V v) {
				       System.out.println("Wrong!");
				       return null;
				   }
					static <T, U, V> V foo(T t, U u, I<T, U, V> i) {
						System.out.println("Right!");
						return null;
					}
					public static void main(String[] args) {
						String s = goo(foo("String", "String", X::goo));
					}
					static <T> T goo(T t) {
					    return t;\t
					}
				}
				"""
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference
public void test447767d() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T, U, V> {
					T goo(U u, V v);
				}
				public class X {
					static String goo(String s, String s2) {
						return null;
					}
					static <T, U, V> T foo(T t, U u, V v) {
				        System.out.println("Wrong!");
				        return null;
				   }
					static <T, U, V> V foo(T t, U u, I<T, U, V> i) {
						System.out.println("Right!");
						return null;
					}
					public static void main(String[] args) {
						String s = goo(foo("String", "String", X::goo));
					}
					static <T> T goo(T t) {
					    return t;\t
					}
				}
				"""
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449410, [1.8][compiler] Eclipse java compiler does not detect a bad return type in lambda expression
public void test449410() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collections;
				public class X {
				  public static void main(String[] args) {
				    Collections.emptyMap()
				        .entrySet()
				        .forEach(entry -> test() ? bad() : returnType());
				  }
				  private static boolean test() {
				    return (System.currentTimeMillis() & 0x1) == 0;
				  }
				  private static void returnType() {
				  }
				  private static void bad() {
				  }
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 6)
			.forEach(entry -> test() ? bad() : returnType());
			 ^^^^^^^
		The method forEach(Consumer<? super Map.Entry<Object,Object>>) in the type Iterable<Map.Entry<Object,Object>> is not applicable for the arguments ((<no type> entry) -> {})
		----------
		2. ERROR in X.java (at line 6)
			.forEach(entry -> test() ? bad() : returnType());
			                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Invalid expression as statement
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449824, [1.8] Difference in behaviour with method references and lambdas
// Captures present behavior - may not be correct.
public void test449824() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    Concrete<Target> fl = new Concrete<Target>();
				    fl.call(each -> each.doSomething()); // fails
				    fl.call((Target each) -> each.doSomething()); // fails
				    fl.call(Target::doSomething); // succeeds in Eclipse 4.5M3 and 4.4.1
				    // but fails in Java 8 1.8.0_11
				  }
				  public static class Target {
				    public void doSomething() {
				    }
				  }
				  public static class Concrete<T> implements Left<T>, Right<T> {
				    public void call(RightHand<? super T> p) {
				    }
				  }
				  public interface Left<T> {
				    default void call(LeftHand<? super T> p) {
				    }
				  }
				  public interface LeftHand<T> {
				    public void left(T t);
				  }
				  public interface Right<T> {
				    public void call(RightHand<? super T> p);
				  }
				  public interface RightHand<T> {
				    public void right(T t);
				  }
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 4)
			fl.call(each -> each.doSomething()); // fails
			   ^^^^
		The method call(X.RightHand<? super X.Target>) is ambiguous for the type X.Concrete<X.Target>
		----------
		2. ERROR in X.java (at line 6)
			fl.call(Target::doSomething); // succeeds in Eclipse 4.5M3 and 4.4.1
			   ^^^^
		The method call(X.RightHand<? super X.Target>) is ambiguous for the type X.Concrete<X.Target>
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448954, [1.8][compiler] Suspect error: "The method foo(String, String, X::goo) is undefined for the type X"
public void test448954() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface I<T, U, V> {
					T goo(U u, V v);
				}
				interface J {
					void foo();
				}
				public class X {
					static String goo(String s, String s2) {
						return null;
					}
					static <T, U, V> T foo(T t, U u, J j) {
						System.out.println("Wrong!");
						return null;
					}
					static <T, U, V> V foo(T t, U u, I<T, U, V> i) {
						System.out.println("Right!");
						return null;
					}
					public static void main(String[] args) {
						String s = goo(foo("String", "String", X::goo));
					}
					static <T> T goo(T t) {
						return t;
					}
				}
				"""
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450380, [1.8][compiler] NPE in Scope.getExactConstructor(..) for bad constructor reference
public void test450380() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.function.IntFunction;
				public class X {
				    IntFunction<ArrayList<String>> noo() {
				        return System::new;
				    }
				}
				"""
	},
	"""
		----------
		1. ERROR in X.java (at line 5)
			return System::new;
			       ^^^^^^^^^^^
		The type System does not define System(int) that is applicable here
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450604, [1.8] CCE at InferenceContext18.getParameter line 1377
public void test450604() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.util.List;
				import java.util.function.Function;
				public class X<T, E extends Exception> {
					public static <T> List<T> of(T one) { return null; }
					public @SafeVarargs static <T> List<T> of(T... items) { return null; }
					public static void printDependencyLoops() throws IOException {
						Function<? super String, ? extends List<String>> mapping = X::of;
					}
				}
				"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450604, [1.8] CCE at InferenceContext18.getParameter line 1377
public void test450604a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					public static <T> List<T> of() { return null; }
					public static @SafeVarargs <T> List<T> of(T... values) { return null; }
					static void walkAll() {
						X.<String> of();
					}
				}
				"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=451677, [1.8][compiler] missing type inference
public void _test451677() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.function.Function;
				public class X {
					public static void test() {
						operationOnCreated(create(123, size -> new ArrayList<Integer>(size)), l -> l.size()); // works with: (ArrayList<Integer> l) -> l.size()
					}
					public static <R, A> R create(A arg, Function<A, R> factory) {
						return factory.apply(arg);
					}
					public static <R, A> R operationOnCreated(A created, Function<A, R> function) {
						return function.apply(created);
					}
				}
				"""
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=451840
// [1.8] java.lang.BootstrapMethodError when running code with constructor reference
public void testBug451840() {
	runNegativeTest(new String [] {
		"X.java",
		"""
			public class X {
			    public static void main(String[] args) {
			    	X test = new X();
			    	MySupplier<X> s = test::new; // incorrect
			    }
			    public interface MySupplier<T> {
			        T create();
			    }
			}"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				MySupplier<X> s = test::new; // incorrect
				                  ^^^^
			test cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556() {
	this.runConformTest(new String [] {
		"X.java",
		"""
			import java.io.Serializable;
			import java.util.Arrays;
			import java.util.List;
			public class X {
			    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);
			    public static void main(String[] args) {
			        for (int i = 0; i < INTEGERS.size(); i++) {
			            MyPredicate<Integer> predicate = INTEGERS.get(i)::equals;
			        }
			    } \s
			    public interface MyPredicate<T> extends Serializable {
			        boolean accept(T each);
			    }
			}"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556a() {
	this.runConformTest(new String [] {
		"X.java",
		"""
			import java.io.Serializable;
			import java.util.Arrays;
			import java.util.List;
			public class X {
				int value = 0;\s
			    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);
			    public Integer next() {
			    	return new Integer(++value);
			    }
			    public static void main(String[] args) {
			    	X t = new X();
			        MyPredicate<Integer> predicate = t.next()::equals;
			        System.out.println("Value " + t.value + " accept " + predicate.accept(t.value));
			    }
			    public interface MyPredicate<T> extends Serializable {
			        boolean accept(T each);
			    }
			}"""
	},
	"Value 1 accept true");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453687
// [1.8][compiler]Incorrect errors when compiling code with Method References
public void testBug453687() {
	this.runConformTest(new String [] {
		"X.java",
		"""
			import static java.util.stream.Collectors.groupingBy;
			import static java.util.stream.Collectors.mapping;
			import static java.util.stream.Collectors.toSet;
			import java.util.Locale;
			import java.util.Map;
			import java.util.Set;
			import java.util.stream.Stream;
			public class X {
				public static void main(String[] args) {
					Map<String, Set<String>> countryLanguagesMap = Stream.of(Locale.getAvailableLocales()).collect(
							groupingBy(Locale::getDisplayCountry, mapping(Locale::getDisplayLanguage, toSet())));
				}
			} """
	},
	"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=456481 - [1.8] VerifyError on constructor reference inside lambda
public void testBug456481() {
	this.runConformTest(new String [] {
		"Test.java",
		"""
			public class Test  {
			    interface Constructor {
			        MyTest execute();
			    }
			    interface ArrayConstructor {
			    	MyTest[] execute(int no);
			    }
			    interface ParameterizedConstructor {
			    	MyParameterizedTest<String> execute();
			    }
			    class MyTest {
			        MyTest() { System.out.println("Constructor executed"); }
			    }
			    class MyParameterizedTest<T> {
			    	MyParameterizedTest() {
			    		System.out.println("Parameterized Constructor executed");
			    	}
			    }
			    public Constructor getConstructor() {
			        return getConstructor(() -> { return MyTest::new; });
			    }
			    public MyTest[] getArray(int no) {
			    	return new MyTest[no];
			    }
			    ArrayConstructor getArrayConstructor() {
			    	return getArrayConstructor(() -> {return MyTest[]::new;});
			    }
			    ParameterizedConstructor getParameterizedConstructor() {
			    	return getParameterizedConstructor(() -> {return MyParameterizedTest<String>::new;});
			    }
			    ArrayConstructor getArrayConstructor(ArrayWrapper w) {
			    	return w.unwrap();
			    }
			    public static void main(String argv[]) {
			        Test t = new Test();
			        MyTest mytest = t.getConstructor().execute();
			        MyTest[] array = t.getArrayConstructor().execute(2);
			        MyParameterizedTest<String> pt = t.getParameterizedConstructor().execute();
			    }
			    ParameterizedConstructor getParameterizedConstructor(PTWrapper ptw) {
			    	return ptw.unwrap();
			    }
			    Constructor getConstructor(Wrapper arg) {
			        return arg.unwrap();
			    }
			    interface PTWrapper {
			    	ParameterizedConstructor unwrap();
			    }
			    interface ArrayWrapper {
			    	ArrayConstructor unwrap();
			    }
			    interface Wrapper {
			        Constructor unwrap();
			    }
			}"""
	},
	"Constructor executed\n" +
	"Parameterized Constructor executed");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=457007, VerifyError
public void testBug457007() {
	this.runConformTest(new String [] {
		"Test.java",
		"""
			public class Test {
				void method() {
			  		class Bar {}
			  		java.util.function.Function<String, Bar> f = str -> new Bar();
				}
				public static void main(String[] args) {
					System.out.println("done");
				}
			}"""
	},
	"done");
}
public void testBug446691_comment5() {
	runConformTest(new String [] {
		"Test.java",
		"""
			import java.util.*;
			
			public class Test {
			  protected final Integer myInt;
			
			  public Test() {
			    myInt = Integer.valueOf(0);
			    try {
			      Optional.empty().orElseThrow(() -> new IllegalArgumentException(myInt.toString()));
			    } catch (IllegalArgumentException e) {
			      throw new RuntimeException();
			    }
			    return;
			  }
			}
			"""
	});
}
public void testBug446691_comment8() {
	runConformTest(new String [] {
		"Boom.java",
		"""
			public class Boom {
			  private final String field;
			  public Boom(String arg) {
			    this.field = arg;
			    try {
			      java.util.function.Supplier<String> supplier = () -> field;
			    } catch (Exception e) {
			     \s
			    }
			  }
			}
			"""
	});
}
public void testBug446691_comment14() {
	runNegativeTest(new String [] {
		"test/Main.java",
		"""
			package test;
			
			import java.util.logging.Logger;
			
			public class Main {
			
				private static final Logger LOG = Logger.getLogger("test");
				private final static String value;
			
				static {
					try {
						LOG.info(() -> String.format("Value is: %s", value));
					} catch (final Exception ex) {
						throw new ExceptionInInitializerError(ex);
					}
				}
			}"""
	},
	"""
		----------
		1. ERROR in test\\Main.java (at line 8)
			private final static String value;
			                            ^^^^^
		The blank final field value may not have been initialized
		----------
		2. ERROR in test\\Main.java (at line 12)
			LOG.info(() -> String.format("Value is: %s", value));
			                                             ^^^^^
		The blank final field value may not have been initialized
		----------
		""");
}
// error in lambda even if field is assigned later
public void testBug446691_comment14b() {
	runNegativeTest(new String [] {
		"test/Main.java",
		"""
			package test;
			
			import java.util.logging.Logger;
			
			public class Main {
			
				private static final Logger LOG = Logger.getLogger("test");
				private final static String value;
			
				static {
					try {
						LOG.info(() -> String.format("Value is: %s", value));
					} catch (final Exception ex) {
						throw new ExceptionInInitializerError(ex);
					}
					value = "";\
				}
			}"""
	},
	"""
		----------
		1. ERROR in test\\Main.java (at line 12)
			LOG.info(() -> String.format("Value is: %s", value));
			                                             ^^^^^
		The blank final field value may not have been initialized
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=463526
// Parenthesis are incorrectly allowed in lambda when LambdaBody is an expression statement
public void testBug463526() {
	runNegativeTest(new String [] {
		"Test.java",
		"""
			public class Test {
			    public static void main(String[] args) {
			        Receiver r = new Receiver();
			        r.accept((l) -> (doItOnTheClass(new Object())));
			    }
			    public static void doItOnTheClass(Object o) {
			        System.out.println("done it");
			    }
			    public static class Receiver {
			        public void accept(Listener l) {
			            l.doIt(new Object());
			        }
			    }
			    public static interface Listener {
			        public void doIt(Object o);
			    }
			}"""
	},
	"""
		----------
		1. ERROR in Test.java (at line 4)
			r.accept((l) -> (doItOnTheClass(new Object())));
			  ^^^^^^
		The method accept(Test.Listener) in the type Test.Receiver is not applicable for the arguments ((<no type> l) -> {})
		----------
		2. ERROR in Test.java (at line 4)
			r.accept((l) -> (doItOnTheClass(new Object())));
			                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Invalid expression as statement
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=463526
// Parenthesis are incorrectly allowed in lambda when LambdaBody is an expression statement
public void testBug463526b() {
	runNegativeTest(new String [] {
		"Test.java",
		"""
			import java.util.function.Consumer;
			public class Test {
			    public static void main(String[] args) {
			        Receiver r = new Receiver();
			        r.process((o) -> (new Object()));
			    }
			    public static class Receiver {
			        public void process(Consumer<Object> p) {
			        }
			    }
			}"""
	},
	"""
		----------
		1. ERROR in Test.java (at line 5)
			r.process((o) -> (new Object()));
			  ^^^^^^^
		The method process(Consumer<Object>) in the type Test.Receiver is not applicable for the arguments ((<no type> o) -> {})
		----------
		2. ERROR in Test.java (at line 5)
			r.process((o) -> (new Object()));
			                 ^^^^^^^^^^^^^^
		Void methods cannot return a value
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=463526
// Parenthesis are incorrectly allowed in lambda when LambdaBody is an expression statement
public void testBug463526c() {
	runNegativeTest(new String [] {
		"Test.java",
		"""
			import java.util.function.Consumer;
			public class Test {
			    public static void main(String[] args) {
			        Receiver r = new Receiver();
			        r.assign((o) -> (o = new Object()));
			    }
			    public static class Receiver {
			        public void assign(Consumer<Object> a) {
			        }
			    }
			}"""
	},
	"""
		----------
		1. ERROR in Test.java (at line 5)
			r.assign((o) -> (o = new Object()));
			  ^^^^^^
		The method assign(Consumer<Object>) in the type Test.Receiver is not applicable for the arguments ((<no type> o) -> {})
		----------
		2. ERROR in Test.java (at line 5)
			r.assign((o) -> (o = new Object()));
			                ^^^^^^^^^^^^^^^^^^
		Void methods cannot return a value
		----------
		""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=464408
public void testBug464408() {
	runNegativeTest(new String[]{
		"test/X.java",
		"""
			import java.util.ArrayList;
			import java.util.List;
			public class X {
			   void x() {
			       List<List<String>> list = new ArrayList<>();
			       list.stream().toArray(List<String>[]::new);
			   }\
			}"""
	}, """
		----------
		1. ERROR in test\\X.java (at line 6)
			list.stream().toArray(List<String>[]::new);
			                      ^^^^^^^^^^^^^^^^^^^
		Cannot create a generic array of List<String>
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=465900
// Internal compiler error: java.lang.IllegalArgumentException: info cannot be null at org.eclipse.jdt.internal.compiler.codegen.StackMapFrame.addStackItem(StackMapFrame.java:81)
public void testBug465900() {
	this.runConformTest(new String [] {
		"X.java",
		"""
			import java.io.Serializable;
			import java.util.ArrayList;
			import java.util.List;
			import java.util.function.Supplier;
			public class X {
				private static final long serialVersionUID = 1L;
				protected void x() {
					String str = "groep.koppeling." + ("".isEmpty() ? "toevoegen" : "bewerken");
					List<String> bean = new ArrayList<>();
					test(bean.get(0)::isEmpty);
				}
				private void test(SerializableSupplier<Boolean> test) {}
			}
			@FunctionalInterface
			interface SerializableSupplier<T> extends Supplier<T>, Serializable {}
			"""
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477888
// [1.8][compiler] Compiler silently produces garbage but editor shows no errors
public void testBug477888() {
	runNegativeTest(new String [] {
		"Test.java",
		"""
			import java.io.IOException;
			import java.nio.file.Files;
			import java.nio.file.Paths;
			import java.util.function.Consumer;
			public class Test {
				public static void main(String[] args) throws IOException {
					Files.lines(Paths.get(args[0])).filter(x -> {return !x.startsWith(".");}).forEach(printMe());
				}
				private static Consumer<String> printMe() {
					return x -> x.isEmpty() ? System.out.println() : System.out.println(getIndex() + " " + x); // error must be reported here!
				}
				static int idx;
			
				private static int getIndex() {
					return ++idx;
				}
			}
			"""
	},
	"""
		----------
		1. ERROR in Test.java (at line 10)
			return x -> x.isEmpty() ? System.out.println() : System.out.println(getIndex() + " " + x); // error must be reported here!
			            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Invalid expression as statement
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=472648
// [compiler][1.8] Lambda expression referencing method with generic type has incorrect compile errors
public void testBug472648() {
	runNegativeTest(
		false,
		JavacHasABug.JavacBugFixed_901,
		new String [] {
		"Test.java",
		"""
			import java.util.ArrayList;
			import java.util.List;
			import java.util.function.Consumer;
			public class Test {
				public static void main(String argv[]) {
					new Test();
				}
				public Test() {
					List<Number> numList = new ArrayList<>();
					numList.add(1);
					numList.add(1.5);
					numList.add(2);
					numList.add(2.5);
					forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));
				}
				private <T> void forEachValueOfType(List<?> list, Class<T> type, Consumer<T> action) {
				\t
					for (Object o : list) {
						if (type.isAssignableFrom(o.getClass())) {
							@SuppressWarnings("unchecked")
							T convertedObject = (T) o;
							action.accept(convertedObject);
						}
					}
				}
			}"""
	},
	"""
		----------
		1. ERROR in Test.java (at line 14)
			forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));
			^^^^^^^^^^^^^^^^^^
		The method forEachValueOfType(List<?>, Class<T>, Consumer<T>) in the type Test is not applicable for the arguments (List<Number>, Class<Integer>, (Integer i) -> {})
		----------
		2. ERROR in Test.java (at line 14)
			forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));
			                                            ^^^^^^^
		Incompatible type specified for lambda expression's parameter i
		----------
		3. ERROR in Test.java (at line 14)
			forEachValueOfType(numList, Integer.class, (Integer i) -> (System.out.println(Integer.toString(i))));
			                                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Invalid expression as statement
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=473432
// Internal compiler error: java.lang.IllegalArgumentException: info cannot be null at org.eclipse.jdt.internal.compiler.codegen.StackMapFrame.addStackItem(StackMapFrame.java:81)
public void testBug473432() {
	this.runConformTest(new String [] {
		"Tester.java",
		"""
			import java.util.function.Function;
			public class Tester {
				private static class ValueWrapper<O> {
					private O val_;
					public ValueWrapper(O val) {
						val_ = val;
					}
					public <R> R mapOrElse(Function<O, R> func, R defaultValue) {
						if(val_ != null) {
							return func.apply(val_);
						}
						return defaultValue;
					}
				}
				private static void handleObject(Object object) {
					System.out.println("Handled: " + object);
				}
				public static void main(String[] args) {
					ValueWrapper<String> wrapper = new ValueWrapper<>("value");
					boolean skipMethod = false;
					// works on both JDT 3.9.2 and 3.11.0
					Boolean result = skipMethod ? true : wrapper.mapOrElse(v -> false, null);
					System.out.println(result);
					wrapper = new ValueWrapper<>(null);
					// works on JDT 3.9.2
					handleObject(skipMethod ?
							true :
							wrapper.mapOrElse(v -> false, null));
					wrapper = new ValueWrapper<>(null);
					// works on neither version
					result = skipMethod ?
							true :
							wrapper.mapOrElse(v -> false, null);
					System.out.println(result);
				}
			}
			"""
	},
	"""
		false
		Handled: null
		null""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=511676 [1.8] Lambda with inner class defs causes java.lang.VerifyError: Bad type on operand stack
public void testBug511676() {
	this.runConformTest(new String [] {
			"A.java",
			"""
				import java.util.function.Function;
				public class A {
				    interface C<T> { }
				    interface O<T> {
				        Object r(C<T> s);
				    }
				    static <T, R> O<R> m(O<T> source, Function<T, O<R>> mapper) {
				        return o -> {
				            class D {
				            	class E {
				                }
				                E e = new E();
				            }
				            D d = new D();
				            return d.e;
				        };
				    }
				    public static void main(String[] args) {
				        m(null, null);
				        System.out.println(" Done");
				    }
				}
				"""
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=511676 [1.8] Lambda with inner class defs causes java.lang.VerifyError: Bad type on operand stack
public void testBug511676a() {
	this.runConformTest(new String [] {
			"A.java",
			"""
				public class A {
				    interface C<T> { }
				    interface O<T> {
				        Object r(C<T> s);
				    }
				    static O<Object> def = o -> {
				        class D {
				        	class E {
				            }
				            E e = new E();
				        }
				        D d = new D();
				        return d.e;
				    };
				    public static void main(String[] args) {
				        O<Object> o = A.def;
				        System.out.println(" Done");
				    }
				}
				"""
		},
		"Done");
}
public void testBug543778() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Sandbox.java",
			"""
				import java.util.function.Supplier;
				
				public class Sandbox {
				
				    <R extends Object> R get(Supplier<@NonNull R> impl) {
				        return null;
				    }
				
				    Object getter() {
				        return get(() -> new Object() {
				
				            @Override
				            public String toString() {
				                return super.toString();
				            }
				
				        });
				    }
				
				}
				""",
			"NonNull.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				
				@Retention(RetentionPolicy.CLASS)
				@Target({ ElementType.TYPE_USE })
				public @interface NonNull {
				    // marker annotation with no members
				}
				""",
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED); // bug happens due to type annotation handling
	runner.runConformTest();
}
public void test572873a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Iterator;
					
					public class X {
					    static <T> Iterable<T> iterable() {
					        return () -> new Iterator<T>() {
					            @Override
					            public boolean hasNext() {
					                return false;
					            }
					
					            @Override
					            public T next() {
					                return null;
					            }
					        };
					    }
						public static void main(String[] args) {
							System.out.println("test T");
						}
					}""",
			},
			"test T"
			);
}
public void test572873b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.function.Consumer;
					
					public class X {
						public static <T> void build(T element) {
					        new Thread(() -> {
					            new Consumer<T>() {
					
					                @Override
					                public void accept(T t) {\
					
					                }
					            };
					        });
						}\
						public static void main(String[] args) {
							System.out.println("test T");
						}
					}""",
			},
			"test T"
			);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1507
// Errors when referencing a var inside lambda
public void testIssue1507() {
	if (this.complianceLevel < ClassFileConstants.JDK10)
		return;
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {

				    private void makeBug() {
				        String nonBuggyLambda = getString(() -> {
				            assert "Goodbye World".equals(nonBuggyLambda);
				            System.out.println("No popup errors, but there is an error marker as expected");
				        });

				        var buggyLambda = getString(() -> {
				            assert "Goodbye World".equals(buggyLambda);
				            System.out.println("Now using var, this entire project can no longer build due to errors.");
				            System.out.println("There will be error popups and countless error log entries.");
				        });

				    }

				    private String getString(Runnable r) {
				        return "Goodbye World";
				    }
				}
				""",
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					private void makeBug() {
					             ^^^^^^^^^
				The method makeBug() from the type X is never used locally
				----------
				2. ERROR in X.java (at line 5)
					assert "Goodbye World".equals(nonBuggyLambda);
					                              ^^^^^^^^^^^^^^
				The local variable nonBuggyLambda may not have been initialized
				----------
				3. ERROR in X.java (at line 10)
					assert "Goodbye World".equals(buggyLambda);
					                              ^^^^^^^^^^^
				The local variable buggyLambda may not have been initialized
				----------
				""");
}
public static Class testClass() {
	return LambdaRegressionTest.class;
}
}
