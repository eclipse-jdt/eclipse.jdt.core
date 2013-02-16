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
	public void _testModifiers1a() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1() {}\n" +
			"    public default synchronized void foo2() {}\n" +
			"    stritfp default void foo3() {}\n" + // typo in strictfp
			"    default public strictfp synchronized void foo4() {}\n" +
			"    public strictfp  default synchronized @Annot void foo5() {}\n" +
			"}\n"},
	    "Some nice and few syntax errors - TODO -");
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
	// Default method overrides independent abstract method
	public void testAbstract02() {
		runConformTest(
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
			});
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides independent abstract method
	// same as above except for order of implements list
	public void testAbstract02a() {
		runConformTest(
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
			});
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
}
