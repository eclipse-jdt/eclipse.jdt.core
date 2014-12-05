/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
public class LambdaRegressionTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test001"};
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
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"import java.util.function.Function;\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().run();\n" +
				"  }\n" +
				"  public void run() {\n" +
				"    class Inner {\n" +
				"      public Inner() {\n" +
				"        System.out.println(\"miep\");\n" +
				"      }\n" +
				"    }\n" +
				"    Map<String, Inner> map = new HashMap<>();\n" +
				"    Function<String, Inner> function = (name) -> {\n" +
				"      Inner i = map.get(name);\n" +
				"      if (i == null) {\n" +
				"        i = new Inner();\n" +
				"        map.put(name, i);\n" +
				"      }\n" +
				"      return i;\n" +
				"\n" +
				"    };\n" +
				"    function.apply(\"test\");\n" +
				"  }\n" +
				"}\n",
			},
			"miep"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"      super(s -> System.out.println(text));\n" +
				"      // super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	super(s -> System.out.println(text));\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot refer to \'this\' nor \'super\' while explicitly invoking a constructor\n" + 
			"----------\n"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"       super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test004() {
	this.runConformTest(
		new String[] {
			"Y.java", 
			"import java.util.function.Supplier;\n" + 
			"class E {\n" + 
			"	E(Supplier<Object> factory) { }\n" + 
			"}\n" + 
			"public class Y extends E {\n" + 
			"	Y() {\n" + 
			"		super( () -> {\n" + 
			"			class Z extends E {\n" + 
			"				Z() {\n" + 
			"					super(() -> new Object());\n" + 
			"				}\n" + 
			"			}\n" + 
			"			return null;\n" + 
			"			});\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Y();\n" + 
			"	}\n" + 
			"}"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448724, [1.8] [compiler] Wrong resolution of overloaded method when irrelevant type parameter is present and lambda is used as parameter
public void test448724() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"import java.util.concurrent.Callable;\n" +
			"public class X {\n" +
			"	public void mismatchRunnableCallable() throws Exception {\n" +
			"		//Resolves to case1(Runnable) method invocation; lambda with block\n" +
			"		case1(() -> {\"abc\".length();});\n" +
			"		//Resolves to case1(Callable) method invocation, resulting in type mismatch; block removed - lambda with expression\n" +
			"                case1(() -> \"abc\".length());\n" +
			"	}\n" +
			"	public void noSuchMismatch() throws Exception {\n" +
			"		//no difference to case1 \n" +
			"		case2(() -> {\"abc\".length();});\n" +
			"		//the only difference to case 1 is the missing irrelevant <T> type parameter. Properly resolves to case2(Runnable) here\n" +
			"		case2(() -> \"abc\".length());\n" +
			"	}\n" +
			"	public void case1(final Runnable r) {\n" +
			"		System.out.println(\"case1: Runnable\");\n" +
			"	}\n" +
			"	public <T> void case1(Callable<Boolean> c) {\n" +
			"		System.out.println(\"case1: Callable\");\n" +
			"	}\n" +
			"	public void case2(final Runnable supplier) {\n" +
			"		System.out.println(\"case2: Runnable\");\n" +
			"	}\n" +
			"	public void case2(Callable<Boolean> conditionEvaluator) {\n" +
			"		System.out.println(\"case2: Callable\");\n" +
			"	}\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		new X().mismatchRunnableCallable();\n" +
			"		new X().noSuchMismatch();\n" +
			"	}\n" +
			"}\n"
	},
	"case1: Runnable\n" + 
	"case1: Runnable\n" + 
	"case2: Runnable\n" + 
	"case2: Runnable");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference 
public void test447767() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static <T, U, V> T foo(T t, U u, V v) {\n" +
			"       System.out.println(\"Wrong!\");\n" +
			"       return null;\n" +
			"   }\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"       return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", (u, v) -> v));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference 
public void test447767a() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static <T, U, V> T foo(T t, U u, I<T, U, V> i) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", (u, v) -> v));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 9)\n" + 
	"	String s = goo(foo(\"String\", \"String\", (u, v) -> v));\n" + 
	"	                                                 ^\n" + 
	"Type mismatch: cannot convert from Object to String\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference 
public void test447767b() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference 
public void test447767c() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> T foo(T t, U u, V v) {\n" +
			"       System.out.println(\"Wrong!\");\n" +
			"       return null;\n" +
			"   }\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447767, [1.8][compiler] Spurious method not applicable error due to interaction between overload resolution and type inference 
public void test447767d() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> T foo(T t, U u, V v) {\n" +
			"        System.out.println(\"Wrong!\");\n" +
			"        return null;\n" +
			"   }\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"	    return t;	\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449410, [1.8][compiler] Eclipse java compiler does not detect a bad return type in lambda expression  
public void test449410() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"import java.util.Collections;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    Collections.emptyMap()\n" +
			"        .entrySet()\n" +
			"        .forEach(entry -> test() ? bad() : returnType());\n" +
			"  }\n" +
			"  private static boolean test() {\n" +
			"    return (System.currentTimeMillis() & 0x1) == 0;\n" +
			"  }\n" +
			"  private static void returnType() {\n" +
			"  }\n" +
			"  private static void bad() {\n" +
			"  }\n" +
			"}\n"
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 6)\n" + 
	"	.forEach(entry -> test() ? bad() : returnType());\n" + 
	"	 ^^^^^^^\n" + 
	"The method forEach(Consumer<? super Map.Entry<Object,Object>>) in the type Iterable<Map.Entry<Object,Object>> is not applicable for the arguments ((<no type> entry) -> {})\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449824, [1.8] Difference in behaviour with method references and lambdas  
// Captures present behavior - may not be correct.
public void test449824() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    Concrete<Target> fl = new Concrete<Target>();\n" +
			"    fl.call(each -> each.doSomething()); // fails\n" +
			"    fl.call((Target each) -> each.doSomething()); // fails\n" +
			"    fl.call(Target::doSomething); // succeeds in Eclipse 4.5M3 and 4.4.1\n" +
			"    // but fails in Java 8 1.8.0_11\n" +
			"  }\n" +
			"  public static class Target {\n" +
			"    public void doSomething() {\n" +
			"    }\n" +
			"  }\n" +
			"  public static class Concrete<T> implements Left<T>, Right<T> {\n" +
			"    public void call(RightHand<? super T> p) {\n" +
			"    }\n" +
			"  }\n" +
			"  public interface Left<T> {\n" +
			"    default void call(LeftHand<? super T> p) {\n" +
			"    }\n" +
			"  }\n" +
			"  public interface LeftHand<T> {\n" +
			"    public void left(T t);\n" +
			"  }\n" +
			"  public interface Right<T> {\n" +
			"    public void call(RightHand<? super T> p);\n" +
			"  }\n" +
			"  public interface RightHand<T> {\n" +
			"    public void right(T t);\n" +
			"  }\n" +
			"}\n"
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 4)\n" + 
	"	fl.call(each -> each.doSomething()); // fails\n" + 
	"	   ^^^^\n" + 
	"The method call(X.RightHand<? super X.Target>) is ambiguous for the type X.Concrete<X.Target>\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448954, [1.8][compiler] Suspect error: "The method foo(String, String, X::goo) is undefined for the type X"
public void test448954() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"interface I<T, U, V> {\n" +
			"	T goo(U u, V v);\n" +
			"}\n" +
			"interface J {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static String goo(String s, String s2) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> T foo(T t, U u, J j) {\n" +
			"		System.out.println(\"Wrong!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	static <T, U, V> V foo(T t, U u, I<T, U, V> i) {\n" +
			"		System.out.println(\"Right!\");\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = goo(foo(\"String\", \"String\", X::goo));\n" +
			"	}\n" +
			"	static <T> T goo(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"}\n"
	},
	"Right!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450380, [1.8][compiler] NPE in Scope.getExactConstructor(..) for bad constructor reference
public void test450380() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"import java.util.ArrayList;\n" +
			"import java.util.function.IntFunction;\n" +
			"public class X {\n" +
			"    IntFunction<ArrayList<String>> noo() {\n" +
			"        return System::new;\n" +
			"    }\n" +
			"}\n"
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 5)\n" + 
	"	return System::new;\n" + 
	"	       ^^^^^^^^^^^\n" + 
	"The type System does not define System(int) that is applicable here\n" + 
	"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450604, [1.8] CCE at InferenceContext18.getParameter line 1377
public void test450604() {
	this.runNegativeTest(
		new String[] {
			"X.java", 
			"import java.io.IOException;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"public class X<T, E extends Exception> {\n" +
			"	public static <T> List<T> of(T one) { return null; }\n" +
			"	public @SafeVarargs static <T> List<T> of(T... items) { return null; }\n" +
			"	public static void printDependencyLoops() throws IOException {\n" +
			"		Function<? super String, ? extends List<String>> mapping = X::of;\n" +
			"	}\n" +
			"}\n"
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450604, [1.8] CCE at InferenceContext18.getParameter line 1377
public void test450604a() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static <T> List<T> of() { return null; }\n" +
			"	public static @SafeVarargs <T> List<T> of(T... values) { return null; }\n" +
			"	static void walkAll() {\n" +
			"		X.<String> of();\n" +
			"	}\n" +
			"}\n"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=451677, [1.8][compiler] missing type inference 
public void _test451677() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"import java.util.ArrayList;\n" +
			"import java.util.function.Function;\n" +
			"public class X {\n" +
			"	public static void test() {\n" +
			"		operationOnCreated(create(123, size -> new ArrayList<Integer>(size)), l -> l.size()); // works with: (ArrayList<Integer> l) -> l.size()\n" +
			"	}\n" +
			"	public static <R, A> R create(A arg, Function<A, R> factory) {\n" +
			"		return factory.apply(arg);\n" +
			"	}\n" +
			"	public static <R, A> R operationOnCreated(A created, Function<A, R> function) {\n" +
			"		return function.apply(created);\n" +
			"	}\n" +
			"}\n"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=451840
// [1.8] java.lang.BootstrapMethodError when running code with constructor reference
public void testBug451840() {
	runNegativeTest(new String [] {
		"X.java",
		"public class X {\n" +  
		"    public static void main(String[] args) {\n" + 
		"    	X test = new X();\n" + 
		"    	MySupplier<X> s = test::new; // incorrect\n" + 
		"    }\n" + 
		"    public interface MySupplier<T> {\n" + 
		"        T create();\n" + 
		"    }\n" + 
		"}"},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	MySupplier<X> s = test::new; // incorrect\n" + 
		"	                  ^^^^\n" + 
		"test cannot be resolved to a type\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" + 
		"import java.util.Arrays;\n" + 
		"import java.util.List;\n" + 
		"public class X {\n" + 
		"    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);\n" + 
		"    public static void main(String[] args) {\n" + 
		"        for (int i = 0; i < INTEGERS.size(); i++) {\n" + 
		"            MyPredicate<Integer> predicate = INTEGERS.get(i)::equals;\n" + 
		"        }\n" + 
		"    }  \n" + 
		"    public interface MyPredicate<T> extends Serializable {\n" + 
		"        boolean accept(T each);\n" + 
		"    }\n" + 
		"}"
	},
	"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448556
// [1.8][compiler] Invalid compiler error about effectively final variable outside the context of a lambda.
public void testBug4448556a() {
	this.runConformTest(new String [] {
		"X.java",
		"import java.io.Serializable;\n" + 
		"import java.util.Arrays;\n" + 
		"import java.util.List;\n" + 
		"public class X {\n" + 
		"	int value = 0; \n" + 
		"    private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3, 4);\n" + 
		"    public Integer next() {\n" + 
		"    	return new Integer(++value);\n" + 
		"    }\n" + 
		"    public static void main(String[] args) {\n" + 
		"    	X t = new X();\n" + 
		"        MyPredicate<Integer> predicate = t.next()::equals;\n" + 
		"        System.out.println(\"Value \" + t.value + \" accept \" + predicate.accept(t.value));\n" + 
		"    }\n" + 
		"    public interface MyPredicate<T> extends Serializable {\n" + 
		"        boolean accept(T each);\n" + 
		"    }\n" + 
		"}"
	},
	"Value 1 accept true");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453687
// [1.8][compiler]Incorrect errors when compiling code with Method References
public void testBug453687() {
	this.runConformTest(new String [] {
		"X.java",
		"import static java.util.stream.Collectors.groupingBy;\n" + 
		"import static java.util.stream.Collectors.mapping;\n" + 
		"import static java.util.stream.Collectors.toSet;\n" + 
		"import java.util.Locale;\n" + 
		"import java.util.Map;\n" + 
		"import java.util.Set;\n" + 
		"import java.util.stream.Stream;\n" + 
		"public class X {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		Map<String, Set<String>> countryLanguagesMap = Stream.of(Locale.getAvailableLocales()).collect(\n" + 
		"				groupingBy(Locale::getDisplayCountry, mapping(Locale::getDisplayLanguage, toSet())));\n" + 
		"	}\n" + 
		"} "
	},
	"");
}
public static Class testClass() {
	return LambdaRegressionTest.class;
}
}