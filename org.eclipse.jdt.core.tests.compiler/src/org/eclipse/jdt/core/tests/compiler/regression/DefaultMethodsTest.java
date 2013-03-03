/*******************************************************************************
 * Copyright (c) 2013 GK Software AG, IBM Corporation and others.
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
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;

import junit.framework.Test;

// See https://bugs.eclipse.org/380501
// Bug 380501 - [1.8][compiler] Add support for default methods (JSR 335)
public class DefaultMethodsTest extends AbstractComparableTest {

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testInheritedDefaultOverrides" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class testClass() {
		return DefaultMethodsTest.class;
	}

	public DefaultMethodsTest(String name) {
		super(name);
	}

	// default methods with various modifiers, positive cases
	public void testModifiers1() {
// Inject an unrelated compile error to prevent class file verification. TODO revert
// (even lambda-enabled JRE doesn't accept now-legal modifier combinations)
//		runConformTest(
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1()  {}\n" +
			"    public default synchronized void foo2() { System.exit(0); }\n" +
			"    strictfp default void foo3() {}\n" +
			"    public default strictfp synchronized void foo4() {}\n" +
			"    public default strictfp synchronized @Annot void foo5() {}\n" +
			"}\n" +
			"public class Wrong{}\n"}, // TODO remove me
		// TODO remove me:
		"----------\n" +
		"1. ERROR in I.java (at line 10)\n" +
		"	public class Wrong{}\n" +
		"	             ^^^^^\n" +
		"The public type Wrong must be defined in its own file\n" +
		"----------\n");
	}

	// default methods with various modifiers, simple syntax error blows the parser
	public void testModifiers1a() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1() { System.out.println(3); }\n" +
			"    public default synchronized void foo2() {}\n" +
			"    stritfp default void foo3() {}\n" + // typo in strictfp
			"    default public strictfp synchronized void foo4() {}\n" +
			"    public strictfp  default synchronized @Annot void foo5() {}\n" +
			"    public default <T> T foo6(T t) { return t; }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 6)\n" +
			"	stritfp default void foo3() {}\n" +
			"	^^^^^^^\n" +
			"Syntax error, insert \"Identifier (\" to complete MethodHeaderName\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 6)\n" +
			"	stritfp default void foo3() {}\n" +
			"	^^^^^^^\n" +
			"Syntax error, insert \")\" to complete MethodDeclaration\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 6)\n" +
			"	stritfp default void foo3() {}\n" +
			"	^^^^^^^\n" +
			"Syntax error, insert \";\" to complete MethodDeclaration\n" +
			"----------\n");
	}

	// regular interface with illegal modifiers
	public void testModifiers2() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    void foo1();\n" +
			"    public synchronized void foo2();\n" +
			"    strictfp void foo3();\n" +
			"    public strictfp synchronized void foo4();\n" +
			"    public strictfp synchronized @Annot void foo5();\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 5)\n" +
			"	public synchronized void foo2();\n" +
			"	                         ^^^^^^\n" +
			"Illegal modifier for the interface method foo2; only public & abstract are permitted\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 6)\n" +
			"	strictfp void foo3();\n" +
			"	              ^^^^^^\n" +
			"Illegal modifier for the interface method foo3; only public & abstract are permitted\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 7)\n" +
			"	public strictfp synchronized void foo4();\n" +
			"	                                  ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public & abstract are permitted\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 8)\n" +
			"	public strictfp synchronized @Annot void foo5();\n" +
			"	                                         ^^^^^^\n" +
			"Illegal modifier for the interface method foo5; only public & abstract are permitted\n" +
			"----------\n");
	}

	// default & regular methods with modifiers that are illegal even for default methods
	public void testModifiers3() {
		runNegativeTest(
		new String[] {
			"I.java",
			"public interface I {\n" +
			"    native void foo1();\n" +
			"    static void foo2();\n" +
			"    native default void foo3() {}\n" +
			"    default native void foo4() {}\n" +
			"    static default void foo5() {}\n" +
			"    default static void foo6() {}\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	native void foo1();\n" +
			"	            ^^^^^^\n" +
			"Illegal modifier for the interface method foo1; only public & abstract are permitted\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 3)\n" +
			"	static void foo2();\n" +
			"	            ^^^^^^\n" +
			"Illegal modifier for the interface method foo2; only public & abstract are permitted\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 4)\n" +
			"	native default void foo3() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo3; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 5)\n" +
			"	default native void foo4() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n" +
			"5. ERROR in I.java (at line 6)\n" +
			"	static default void foo5() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo5; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n" +
			"6. ERROR in I.java (at line 7)\n" +
			"	default static void foo6() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo6; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n");
	}

	// if an interface methods is explicitly "abstract" it cannot have a (default) body
	public void testModifiers4() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"public interface I {\n" +
			"    abstract void foo1();\n" + // OK
			"    public abstract default void foo2() {}\n" +
			"    default abstract void foo3() {}\n" +
			"    void foo4() { }\n" + // implicit "abstract" without "default" doesn't allow a body, either
			"    abstract static default void foo5() {}\n" + // double fault
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 4)\n" +
			"	public abstract default void foo2() {}\n" +
			"	                             ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 5)\n" +
			"	default abstract void foo3() {}\n" +
			"	                      ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 6)\n" +
			"	void foo4() { }\n" +
			"	     ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 7)\n" +
			"	abstract static default void foo5() {}\n" +
			"	                             ^^^^^^\n" +
			"Illegal modifier for the interface method foo5; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n" +
			"5. ERROR in I.java (at line 7)\n" +
			"	abstract static default void foo5() {}\n" +
			"	                             ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n");
	}

	// class implements interface with default method. 
	// - no need to implement this interface method as it is not abstract
	public void testModifiers5() {
		runConformTest(
			new String[] {
				"C.java",
				"public class C implements I {\n" +
				"    public static void main(String[] args) {\n" +
				"        new C().foo();\n" +
				"    }\n" +
				"}\n",
				"I.java",
				"public interface I {\n" +
				"    default void foo() {\n" +
				"        System.out.println(\"default\");\n" +
				"    }\n" +
				"}\n"
			},
			"default"
			);
	}

	// class implements interface with default method. 
	// - witness for NoSuchMethodError in synthetic method (SuperMethodAccess)
	public void testModifiers5a() {
		runConformTest(
			new String[] {
				"C.java",
				"interface I {\n" +
				"    public default void foo() {\n" +
				"        System.out.println(\"default\");\n" +
				"    }\n" +
				"}\n" +
				"public class C implements I {\n" +
				"    public static void main(String[] args) {\n" +
				"        C c = new C();\n" +
				"        c.foo();\n" +
				"    }\n" +
				"}\n"
			},
			"default"
			);
	}

	// class implements interface with default method. 
	// - no need to implement this interface method as it is not abstract, but other abstract method exists
	public void testModifiers6() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    default void foo() {}\n" +
				"    void bar();\n" +
				"}\n",
				"C.java",
				"public class C implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 1)\n" + 
			"	public class C implements I {}\n" + 
			"	             ^\n" + 
			"The type C must implement the inherited abstract method I.bar()\n" + 
			"----------\n");
	}

	// a default method has a semicolon body / an undocumented empty body
	public void testModifiers7() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK, JavaCore.ERROR);
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    default void foo();\n" +
				"    default void bar() {}\n" +
				"    default void zork() { /* nop */ }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	default void foo();\n" +
			"	             ^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 3)\n" +
			"	default void bar() {}\n" +
			"	                   ^^\n" +
			"Empty block should be documented\n" +
			"----------\n",
			null/*classLibs*/,
			true/*shouldFlush*/,
			options);
	}

	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// new error message
	public void testObjectMethod1() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    public default String toString () { return \"\";}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in I.java (at line 2)\n" + 
			"	public default String toString () { return \"\";}\n" + 
			"	                      ^^^^^^^^^^^\n" + 
			"A default method cannot override a method from java.lang.Object \n" + 
			"----------\n");
	}
	
	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// when using a type variable this is already reported as a name clash
	public void testObjectMethod2() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I<T> {\n" +
				"    public default boolean equals (T other) { return false;}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in I.java (at line 2)\n" + 
			"	public default boolean equals (T other) { return false;}\n" + 
			"	                       ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equals(T) of type I<T> has the same erasure as equals(Object) of type Object but does not override it\n" + 
			"----------\n");
	}
	
	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// one error for final method is enough
	public void testObjectMethod3() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I<T> {\n" +
				"    @Override\n" +
				"    default public Class<?> getClass() { return null;}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in I.java (at line 3)\n" + 
			"	default public Class<?> getClass() { return null;}\n" + 
			"	                        ^^^^^^^^^^\n" + 
			"Cannot override the final method from Object\n" + 
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// an inherited default methods clashes with another inherited method
	// simple case
	public void testInheritedDefaultOverrides01() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	String foo();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String foo() { return \"\"; }\n" +
				"}\n",
				"I3.java",
				"public interface I3 extends I1, I2 {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 1)\n" + 
			"	public interface I3 extends I1, I2 {\n" + 
			"	                 ^^\n" + 
			"The default method foo() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
	}
	
	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// an inherited default methods clashes with another inherited method
	// indirect inheritance
	public void testInheritedDefaultOverrides02() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	String foo();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String foo() { return \"\"; }\n" +
				"}\n",
				"I1A.java",
				"public interface I1A extends I1 {\n" +
				"}\n",
				"I2A.java",
				"public interface I2A extends I2 {\n" +
				"}\n",
				"I3.java",
				"public interface I3 extends I1A, I2A {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 1)\n" + 
			"	public interface I3 extends I1A, I2A {\n" + 
			"	                 ^^\n" + 
			"The default method foo() inherited from I2 conflicts with another method inherited from I1\n" +
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Parameterized case is already reported as a clash
	public void testInheritedDefaultOverrides03() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"import java.util.List;\n" +
				"public interface I1 {\n" +
				"	String foo(List<String> l);\n" +
				"}\n",
				"I2.java",
				"import java.util.List;\n" +
				"public interface I2 {\n" +
				"   @SuppressWarnings(\"rawtypes\")\n" +
				"	default String foo(List l) { return \"\"; }\n" +
				"}\n",
				"I3.java",
				"import java.util.List;\n" +
				"public interface I3 extends I1, I2 {\n" +
				"   @Override\n" +
				"   String foo(List<String> l);\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 4)\n" + 
			"	String foo(List<String> l);\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(List<String>) of type I3 has the same erasure as foo(List) of type I2 but does not override it\n" + 
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Parameterized case is already reported as a clash - inverse case of previous
	public void testInheritedDefaultOverrides04() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"import java.util.List;\n" +
				"public interface I1 {\n" +
				"	default String foo(List<String> l) { return \"\"; }\n" +
				"}\n",
				"I2.java",
				"import java.util.List;\n" +
				"public interface I2 {\n" +
				"   @SuppressWarnings(\"rawtypes\")\n" +
				"	String foo(List l);\n" +
				"}\n",
				"I3.java",
				"import java.util.List;\n" +
				"public interface I3 extends I1, I2 {\n" +
				"   @Override\n" +
				"   String foo(List<String> l);\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 4)\n" + 
			"	String foo(List<String> l);\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(List<String>) of type I3 has the same erasure as foo(List) of type I2 but does not override it\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}
	
	public void testDefaultNonclash3() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	default Iterable<BiValue<K, V>> asIterable() {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash4() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public interface X extends Map<String, Object> {\n" +
				"   Zork z;\n" +
				"}\n" +
				"\n" +
				"interface Map<K,V> extends MapStream<K, V>  {\n" +
				"   @Override\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"interface MapStream<K, V> {\n" +
				"	Iterable<BiValue<K, V>> asIterable();\n" +
				"}\n" +
				"\n" +
				"interface BiValue<T, U> {\n" +
				"    T getKey();\n" +
				"    U getValue();\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Don't report conflict between the same method inherited on two paths.
	public void testInheritedDefaultOverrides05() {
		runConformTest(
			new String[] {
				"StringList.java",
				"import java.util.Collection;\n" +
				"public abstract class StringList implements Collection<String> {\n" +
				"}\n"
			},
			"");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// extract from SuperTypeTest.test013():
	public void testInheritedDefaultOverrides06() {
		runConformTest(
			new String[] {
				"IterableList.java",
				"import java.util.*;\n" +
				"public interface IterableList<E> extends Iterable<E>, List<E> {}\n" +
				"interface ListIterable<E> extends Iterable<E>, List<E> {}\n" +
				"\n"
			},
			"");
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface
	public void testAbstract01() {
		runConformTest(
			new String[] {
				"I2.java",
				"public interface I2 {\n" +
				"    void test();\n" +
				"}\n",
				"I1.java",
				"public interface I1 extends I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I1 {\n" +
				"}\n"
			});
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method conflicts with independent interface method
	public void testAbstract02() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I1, I2 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 1)\n" + 
			"	public class C implements I1, I2 {\n" + 
			"	             ^\n" + 
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" + 
			"----------\n");
			// Note: javac first complains: C is not abstract and does not override abstract method test() in I1
			//       only when C is marked abstract does the conflict between abstract and default method surface
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method conflicts independent interface method
	// same as above except for order of implements list
	public void testAbstract02a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 1)\n" + 
			"	public class C implements I2, I1 {\n" + 
			"	             ^\n" + 
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" + 
			"----------\n");
			// Note: javac first complains: C is not abstract and does not override abstract method test() in I1
			//       only when C is marked abstract does the conflict between abstract and default method surface
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method does not override independent abstract method
	// class is abstract
	public void testAbstract02b() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public abstract class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 1)\n" + 
			"	public abstract class C implements I2, I1 {\n" + 
			"	                      ^\n" + 
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" + 
			"----------\n");
	}

	// same as above but only interfaces
	public void testAbstract02c() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"I3.java",
				"public interface I3 extends I1, I2 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 1)\n" + 
			"	public interface I3 extends I1, I2 {\n" + 
			"	                 ^^\n" + 
			"The default method test() inherited from I2 conflicts with another method inherited from I1\n" + 
			"----------\n");
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface - class implements both
	public void testAbstract03() {
		runConformTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I1, I2 {\n" +
				"}\n"
			});
	}
	
	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface - class implements both
	// same as above except for order of implements list
	public void testAbstract03a() {
		runConformTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    void test();\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    default void test() {}\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			});
	}

	// JLS 8.1.1.1 abstract Classes
	// default method is not inherited because a more specific abstract method is.
	public void testAbstract04() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    void test();\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 1)\n" + 
			"	public class C implements I2, I1 {\n" + 
			"	             ^\n" + 
			"The type C must implement the inherited abstract method I2.test()\n" + 
			"----------\n");
	}

	// JLS 8.1.1.1 abstract Classes
	// default method is not inherited because a more specific abstract method is.
	// same as above except for order of implements list
	public void testAbstract04a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"    default void test() {}\n" +
				"}\n",
				"I2.java",
				"public interface I2 extends I1 {\n" +
				"    @Override\n" +
				"    void test();\n" +
				"}\n",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C.java (at line 1)\n" + 
			"	public class C implements I2, I1 {\n" + 
			"	             ^\n" + 
			"The type C must implement the inherited abstract method I2.test()\n" + 
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: the conflict scenario
	public void testAbstract05() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"C2.java",
				"public abstract class C2 implements I1, I2 {\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in C2.java (at line 1)\n" + 
			"	public abstract class C2 implements I1, I2 {\n" + 
			"	                      ^^\n" + 
			"Duplicate default methods named value1 with the parameters () and () are inherited from the types I2 and I1\n" + 
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict resolved
	public void testAbstract06() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting methods (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1, I2 {\n" +
				"}\n",
				"C3.java",
				"public class C3 extends C2 {\n" +
				"	@Override\n" +
				"	public Object value1() { return this; } // too week, need a method returning String\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C3.java (at line 3)\n" + 
			"	public Object value1() { return this; } // too week, need a method returning String\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I1.value1()\n" + 
			"----------\n" + 
			"2. ERROR in C3.java (at line 3)\n" + 
			"	public Object value1() { return this; } // too week, need a method returning String\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I2.value1()\n" + 
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict resolved
	// variant: second method is not a default method
	public void testAbstract06a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	String value1();\n" + // conflicts with other default method
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting methods (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1, I2 {\n" +
				"}\n",
				"C3.java",
				"public class C3 extends C2 {\n" +
				"	@Override\n" +
				"	public Object value1() { return this; } // too week, need a method returning String\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C3.java (at line 3)\n" + 
			"	public Object value1() { return this; } // too week, need a method returning String\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I2.value1()\n" + 
			"----------\n" + 
			"2. ERROR in C3.java (at line 3)\n" + 
			"	public Object value1() { return this; } // too week, need a method returning String\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I1.value1()\n" + 
			"----------\n");
	}
	
	// abstract class method trumps otherwise conflicting default methods: conflict not resolved due to insufficient visibility
	public void testAbstract6b() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"p1/C1.java",
				"package p1;\n" +
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trump with package visibility doesn't work
				"}\n",
				"C2.java",
				"public abstract class C2 extends p1.C1 implements I1, I2 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C2.java (at line 1)\n" + 
			"	public abstract class C2 extends p1.C1 implements I1, I2 {\n" + 
			"	                      ^^\n" + 
			"Duplicate default methods named value1 with the parameters () and () are inherited from the types I2 and I1\n" + 
			"----------\n");
	}

	// abstract class method trumps otherwise conflicting default method: only one default method
	public void testAbstract07() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value1() { return null; }\n" +
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting method (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1 {\n" +
				"}\n",
				"C3.java",
				"public class C3 extends C2 {\n" +
				"	@Override\n" +
				"	public Object value1() { return this; } // too week, need a method returning String\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C3.java (at line 3)\n" + 
			"	public Object value1() { return this; } // too week, need a method returning String\n" + 
			"	       ^^^^^^\n" + 
			"The return type is incompatible with I1.value1()\n" + 
			"----------\n");
	}

	// class inherits two override equivalent methods,
	// must be declared abstract, although one of the methods is a default method.
	public void testAbstract08() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"public interface I1 {\n" +
				"	default String value() { return null; }\n" +
				"}\n",
				"C1.java",
				"public abstract class C1 {\n" +
				"	public abstract String value();" +
				"}\n",
				"C2.java",
				"public class C2 extends C1 implements I1 {\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in C2.java (at line 1)\n" + 
			"	public class C2 extends C1 implements I1 {\n" + 
			"	             ^^\n" + 
			"The type C2 must implement the inherited abstract method C1.value()\n" + 
			"----------\n");
	}

	// an annotation type cannot have default methods
	public void testAnnotation1() {
		runNegativeTest(
			false,
			new String[] {
				"I.java",
				"public @interface I {\n" +
				"    default String id() { return \"1\"; }\n" +
				"}\n"
			},
			null,
			null,
			"----------\n" + 
			"1. ERROR in I.java (at line 2)\n" + 
			"	default String id() { return \"1\"; }\n" + 
			"	^^^^^^^\n" + 
			"Syntax error on token \"default\", @ expected\n" + 
			"----------\n",
			JavacTestOptions.JavacHasABug.Javac8AcceptsDefaultMethodInAnnotationType);
	}
	
	// basic situation similar to AmbiguousMethodTest.test009()
	public void testSuperCall1() {
		this.runConformTest(
			new String[] {
				"OrderedSet.java",
				"import java.util.*;\n" +
				"import java.util.stream.Stream;\n" +
				"public interface OrderedSet<E> extends List<E>, Set<E> {\n" +
				"	@Override\n" +
				"	boolean add(E o);\n" +
				"	@Override\n" +
				"	default Stream<E> stream() { return List.super.stream();}\n" +
				"	@Override\n" +
				"	default Stream<E> parallelStream() { return Set.super.parallelStream();}\n" +
				"}\n"
			},
			""
		);
	}

	// some illegal cases
	// - call to indirect super
	// - call to super of outer
	// - target method is not a default method
	// - attempt to use this syntax for a super-ctor call
	public void testSuperCall2() {
		this.runNegativeTest(
			new String[] {
				"T.java",
				"import java.util.*;\n" +
				"import java.util.stream.Stream;\n" +
				"public abstract class T<E> implements OrderedSet<E> {\n" +
				"	@Override\n" +
				"	public Stream<E> stream() {\n" +
				"		return List.super.stream(); // List is not a direct super interface\n" +
				"	}\n" +
				"	@Override\n" +
				"	public Stream<E> parallelStream() { return OrderedSet.super.parallelStream();}\n" + // OK
				"   class Inner {\n" +
				"		public Stream<E> stream() {\n" +
				"			return OrderedSet.super.stream(); // not a super interface of the direct enclosing class\n" +
				"		}\n" +
				"	}\n" +
				"	@Override\n" +
				"	public boolean add(E o) {\n" +
				"		OrderedSet.super.add(o); // target not a default method\n" +
				"	}\n" +
				"	T() {\n" +
				"		OrderedSet.super(); // not applicable for super ctor call\n" +
				"	}\n" +
				"}\n" +
				"interface OrderedSet<E> extends List<E>, Set<E> {\n" +
				"	@Override\n" +
				"	boolean add(E o);\n" +
				"	@Override\n" +
				"	default Stream<E> stream() { return List.super.stream();}\n" +
				"	@Override\n" +
				"	default Stream<E> parallelStream() { return Set.super.parallelStream();}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in T.java (at line 6)\n" + 
			"	return List.super.stream(); // List is not a direct super interface\n" + 
			"	       ^^^^^^^^^^\n" + 
			"No enclosing instance of the type List<E> is accessible in scope\n" + 
			"----------\n" + 
			"2. ERROR in T.java (at line 12)\n" + 
			"	return OrderedSet.super.stream(); // not a super interface of the direct enclosing class\n" + 
			"	       ^^^^^^^^^^^^^^^^\n" + 
			"No enclosing instance of the type OrderedSet<E> is accessible in scope\n" + 
			"----------\n" + 
			"3. ERROR in T.java (at line 17)\n" + 
			"	OrderedSet.super.add(o); // target not a default method\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot directly invoke the abstract method add(E) for the type OrderedSet<E>\n" + 
			"----------\n" + 
			"4. ERROR in T.java (at line 20)\n" + 
			"	OrderedSet.super(); // not applicable for super ctor call\n" + 
			"	^^^^^^^^^^\n" + 
			"Illegal enclosing instance specification for type Object\n" + 
			"----------\n"
		);
	}

	// with execution
	public void testSuperCall3() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I2 {\n" +
				"	@Override\n" +
				"	public void print() {\n" +
				"		I2.super.print();\n" +
				"		System.out.print(\"!\");" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		new X().print();\n" +
				"	}\n" +
				"}\n" +
				"interface I1 {\n" +
				"	default void print() {\n" +
				"		System.out.print(\"O\");\n" +
				"	}\n" +
				"}\n" +
				"interface I2 extends I1 {\n" +
				"	default void print() {\n" +
				"		I1.super.print();\n" +
				"		System.out.print(\"K\");\n" +
				"	}\n" +
				"}\n"
			},
			"OK!"
		);
	}
	
	// Bug 401235 - [1.8][compiler] 'this' reference must be allowed in default methods and local classes
	public void testThisReference1() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I1, I2 {\n" +
				"	@Override\n" +
				"	public String s1() { return \"O\"; }\n" +
				"	@Override\n" +
				"	public String s2() { return \"K\"; }\n" +
				"	public static void main(String... args) {\n" +
				"		X x = new X();\n" +
				"		x.print1();\n" +
				"		x.print2();\n" +
				"	}\n" +
				"}\n" +
				"interface I1 {\n" +
				"	String s1();" +
				"	default void print1() {\n" +
				"		System.out.print(this.s1());\n" + // 'this' as a receiver
				"	}\n" +
				"}\n" +
				"interface I2 {\n" +
				"	String s2();\n" +
				"	default void print2() {\n" +
				"		class Inner {\n" +
				"			String value() { return I2.this.s2(); }\n" + // qualified 'this' refering to the enclosing interface type
				"		}\n" +
				"		System.out.print(new Inner().value());\n" +
				"	}\n" +
				"}\n"
			},
			"OK"
		);
	}
}
