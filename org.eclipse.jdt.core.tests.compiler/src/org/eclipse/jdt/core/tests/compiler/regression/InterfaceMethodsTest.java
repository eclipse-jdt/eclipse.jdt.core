/*******************************************************************************
 * Copyright (c) 2013, 2021 GK Software AG, IBM Corporation and others.
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
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

// See https://bugs.eclipse.org/380501
// Bug 380501 - [1.8][compiler] Add support for default methods (JSR 335)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class InterfaceMethodsTest extends AbstractComparableTest {

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug421543" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Test setUpTest(Test test) throws Exception {
		TestCase.setUpTest(test);
		RegressionTestSetup suite = new RegressionTestSetup(ClassFileConstants.JDK1_8);
		suite.addTest(test);
		return suite;
	}

	public static Class testClass() {
		return InterfaceMethodsTest.class;
	}

	public InterfaceMethodsTest(String name) {
		super(name);
	}

	// default methods with various modifiers, positive cases
	public void testModifiers1() {
		runConformTest(
		new String[] {
			"I.java",
			"""
				import java.lang.annotation.*;
				@Target(ElementType.METHOD) @interface Annot{}
				public interface I {
				    default void foo1()  {}
				    public default void foo2() { System.exit(0); }
				    strictfp default void foo3() {}
				    public default strictfp void foo4() {}
				    public default strictfp @Annot void foo5() {}
				}
				""",
		},
		"");
	}


	// default methods with various modifiers, negative cases
	public void testModifiers1a() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		String op = this.complianceLevel < ClassFileConstants.JDK17 ?
		"----------\n" +
		"1. ERROR in I.java (at line 5)\n" +
		"	public default synchronized void foo2() { System.exit(0); }\n" +
		"	                                 ^^^^^^\n" +
		"Illegal modifier for the interface method foo2; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"2. ERROR in I.java (at line 7)\n" +
		"	public default strictfp synchronized void foo4() {}\n" +
		"	                                          ^^^^^^\n" +
		"Illegal modifier for the interface method foo4; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"3. ERROR in I.java (at line 8)\n" +
		"	public default strictfp synchronized @Annot void foo5() {}\n" +
		"	                                                 ^^^^^^\n" +
		"Illegal modifier for the interface method foo5; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" :
			"""
				----------
				1. ERROR in I.java (at line 5)
					public default synchronized void foo2() { System.exit(0); }
					                                 ^^^^^^
				Illegal modifier for the interface method foo2; only public, private, abstract, default, static and strictfp are permitted
				----------
				2. WARNING in I.java (at line 6)
					strictfp default void foo3() {}
					^^^^^^^^
				Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
				----------
				3. WARNING in I.java (at line 7)
					public default strictfp synchronized void foo4() {}
					               ^^^^^^^^
				Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
				----------
				4. ERROR in I.java (at line 7)
					public default strictfp synchronized void foo4() {}
					                                          ^^^^^^
				Illegal modifier for the interface method foo4; only public, private, abstract, default, static and strictfp are permitted
				----------
				5. WARNING in I.java (at line 8)
					public default strictfp synchronized @Annot void foo5() {}
					               ^^^^^^^^
				Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
				----------
				6. ERROR in I.java (at line 8)
					public default strictfp synchronized @Annot void foo5() {}
					                                                 ^^^^^^
				Illegal modifier for the interface method foo5; only public, private, abstract, default, static and strictfp are permitted
				----------
				""";
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				import java.lang.annotation.*;
				@Target(ElementType.METHOD) @interface Annot{}
				public interface I {
				    default void foo1()  {}
				    public default synchronized void foo2() { System.exit(0); }
				    strictfp default void foo3() {}
				    public default strictfp synchronized void foo4() {}
				    public default strictfp synchronized @Annot void foo5() {}
				}
				"""},
			op);
	}

	// default methods with various modifiers, simple syntax error blows the parser
	public void testModifiers1b() {
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    default void foo1() { System.out.println(3); }\n" +
			"    public default void foo2() {}\n" +
			"    stritfp default void foo3() {}\n" + // typo in strictfp
			"    default public strictfp void foo4() {}\n" +
			"    public strictfp  default @Annot void foo5() {}\n" +
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
			"----------\n" +
			(this.complianceLevel >= ClassFileConstants.JDK17 ?
					"4. WARNING in I.java (at line 7)\n" +
					"	default public strictfp void foo4() {}\n" +
					"	               ^^^^^^^^\n" +
					"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
					"----------\n" +
					"5. WARNING in I.java (at line 8)\n" +
					"	public strictfp  default @Annot void foo5() {}\n" +
					"	       ^^^^^^^^\n" +
					"Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.\n" +
					"----------\n" : "")
			);
	}

	// regular interface with illegal modifiers
	public void testModifiers2() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		String op = this.complianceLevel < ClassFileConstants.JDK17 ?
		"----------\n" +
		"1. ERROR in I.java (at line 5)\n" +
		"	public synchronized void foo2();\n" +
		"	                         ^^^^^^\n" +
		"Illegal modifier for the interface method foo2; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"2. ERROR in I.java (at line 6)\n" +
		"	strictfp void foo3();\n" +
		"	              ^^^^^^\n" +
		"strictfp is not permitted for abstract interface method foo3\n" +
		"----------\n" +
		"3. ERROR in I.java (at line 7)\n" +
		"	public strictfp synchronized void foo4();\n" +
		"	                                  ^^^^^^\n" +
		"strictfp is not permitted for abstract interface method foo4\n" +
		"----------\n" +
		"4. ERROR in I.java (at line 7)\n" +
		"	public strictfp synchronized void foo4();\n" +
		"	                                  ^^^^^^\n" +
		"Illegal modifier for the interface method foo4; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" +
		"5. ERROR in I.java (at line 8)\n" +
		"	public strictfp synchronized @Annot void foo5();\n" +
		"	                                         ^^^^^^\n" +
		"strictfp is not permitted for abstract interface method foo5\n" +
		"----------\n" +
		"6. ERROR in I.java (at line 8)\n" +
		"	public strictfp synchronized @Annot void foo5();\n" +
		"	                                         ^^^^^^\n" +
		"Illegal modifier for the interface method foo5; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
		"----------\n" :
			"""
				----------
				1. ERROR in I.java (at line 5)
					public synchronized void foo2();
					                         ^^^^^^
				Illegal modifier for the interface method foo2; only public, private, abstract, default, static and strictfp are permitted
				----------
				2. WARNING in I.java (at line 6)
					strictfp void foo3();
					^^^^^^^^
				Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
				----------
				3. ERROR in I.java (at line 6)
					strictfp void foo3();
					              ^^^^^^
				strictfp is not permitted for abstract interface method foo3
				----------
				4. WARNING in I.java (at line 7)
					public strictfp synchronized void foo4();
					       ^^^^^^^^
				Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
				----------
				5. ERROR in I.java (at line 7)
					public strictfp synchronized void foo4();
					                                  ^^^^^^
				strictfp is not permitted for abstract interface method foo4
				----------
				6. ERROR in I.java (at line 7)
					public strictfp synchronized void foo4();
					                                  ^^^^^^
				Illegal modifier for the interface method foo4; only public, private, abstract, default, static and strictfp are permitted
				----------
				7. WARNING in I.java (at line 8)
					public strictfp synchronized @Annot void foo5();
					       ^^^^^^^^
				Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
				----------
				8. ERROR in I.java (at line 8)
					public strictfp synchronized @Annot void foo5();
					                                         ^^^^^^
				strictfp is not permitted for abstract interface method foo5
				----------
				9. ERROR in I.java (at line 8)
					public strictfp synchronized @Annot void foo5();
					                                         ^^^^^^
				Illegal modifier for the interface method foo5; only public, private, abstract, default, static and strictfp are permitted
				----------
				""";
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				import java.lang.annotation.*;
				@Target(ElementType.METHOD) @interface Annot{}
				public interface I {
				    void foo1();
				    public synchronized void foo2();
				    strictfp void foo3();
				    public strictfp synchronized void foo4();
				    public strictfp synchronized @Annot void foo5();
				}
				"""},
			op);
	}

	// default & regular methods with modifiers that are illegal even for default methods
	public void testModifiers3() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				    native void foo1();
				    static void foo2();
				    native default void foo3() {}
				    default native void foo4() {}
				    static default void foo5() {}
				    default static void foo6() {}
				}
				"""},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	native void foo1();\n" +
			"	            ^^^^^^\n" +
			"Illegal modifier for the interface method foo1; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 3)\n" +
			"	static void foo2();\n" +
			"	            ^^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 4)\n" +
			"	native default void foo3() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo3; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 5)\n" +
			"	default native void foo4() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n" +
			"5. ERROR in I.java (at line 6)\n" +
			"	static default void foo5() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo5; only one of abstract, default, or static permitted\n" +
			"----------\n" +
			"6. ERROR in I.java (at line 7)\n" +
			"	default static void foo6() {}\n" +
			"	                    ^^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo6; only one of abstract, default, or static permitted\n" +
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
			"""
				----------
				1. ERROR in I.java (at line 4)
					public abstract default void foo2() {}
					                             ^^^^^^
				Illegal combination of modifiers for the interface method foo2; only one of abstract, default, or static permitted
				----------
				2. ERROR in I.java (at line 5)
					default abstract void foo3() {}
					                      ^^^^^^
				Illegal combination of modifiers for the interface method foo3; only one of abstract, default, or static permitted
				----------
				3. ERROR in I.java (at line 6)
					void foo4() { }
					     ^^^^^^
				Abstract methods do not specify a body
				----------
				4. ERROR in I.java (at line 7)
					abstract static default void foo5() {}
					                             ^^^^^^
				Illegal combination of modifiers for the interface method foo5; only one of abstract, default, or static permitted
				----------
				""");
	}

	// class implements interface with default method.
	// - no need to implement this interface method as it is not abstract
	public void testModifiers5() {
		runConformTest(
			new String[] {
				"C.java",
				"""
					public class C implements I {
					    public static void main(String[] args) {
					        new C().foo();
					    }
					}
					""",
				"I.java",
				"""
					public interface I {
					    default void foo() {
					        System.out.println("default");
					    }
					}
					"""
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
				"""
					public interface I {
					    default void foo() {}
					    void bar();
					}
					""",
				"C.java",
				"public class C implements I {}\n"
			},
			"""
				----------
				1. ERROR in C.java (at line 1)
					public class C implements I {}
					             ^
				The type C must implement the inherited abstract method I.bar()
				----------
				""");
	}

	// a default method has a semicolon body / an undocumented empty body
	public void testModifiers7() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK, JavaCore.ERROR);
		runNegativeTest(
			new String[] {
				"I.java",
				"""
					public interface I {
					    default void foo();
					    default void bar() {}
					    default void zork() { /* nop */ }
					}
					"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					default void foo();
					             ^^^^^
				This method requires a body instead of a semicolon
				----------
				2. ERROR in I.java (at line 3)
					default void bar() {}
					                   ^^
				Empty block should be documented
				----------
				""",
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
				"""
					public interface I {
					    public default String toString () { return "";}
					}
					"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					public default String toString () { return "";}
					                      ^^^^^^^^^^^
				A default method cannot override a method from java.lang.Object\s
				----------
				""");
	}

	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// when using a type variable this is already reported as a name clash
	public void testObjectMethod2() {
		runNegativeTest(
			new String[] {
				"I.java",
				"""
					public interface I<T> {
					    public default boolean equals (T other) { return false;}
					}
					"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					public default boolean equals (T other) { return false;}
					                       ^^^^^^^^^^^^^^^^
				Name clash: The method equals(T) of type I<T> has the same erasure as equals(Object) of type Object but does not override it
				----------
				""");
	}

	// JLS 9.4.2  - default method cannot override method from Object
	// Bug 382355 - [1.8][compiler] Compiler accepts erroneous default method
	// one error for final method is enough
	public void testObjectMethod3() {
		runNegativeTest(
			new String[] {
				"I.java",
				"""
					public interface I<T> {
					    @Override
					    default public Class<?> getClass() { return null;}
					}
					"""
			},
			"""
				----------
				1. ERROR in I.java (at line 3)
					default public Class<?> getClass() { return null;}
					                        ^^^^^^^^^^
				Cannot override the final method from Object
				----------
				""");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// an inherited default methods clashes with another inherited method
	// simple case
	public void testInheritedDefaultOverrides01() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						String foo();
					}
					""",
				"I2.java",
				"""
					public interface I2 {
						default String foo() { return ""; }
					}
					""",
				"I3.java",
				"public interface I3 extends I1, I2 {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in I3.java (at line 1)
					public interface I3 extends I1, I2 {
					                 ^^
				The default method foo() inherited from I2 conflicts with another method inherited from I1
				----------
				""");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// an inherited default methods clashes with another inherited method
	// indirect inheritance
	public void testInheritedDefaultOverrides02() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						String foo();
					}
					""",
				"I2.java",
				"""
					public interface I2 {
						default String foo() { return ""; }
					}
					""",
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
			"""
				----------
				1. ERROR in I3.java (at line 1)
					public interface I3 extends I1A, I2A {
					                 ^^
				The default method foo() inherited from I2 conflicts with another method inherited from I1
				----------
				""");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Parameterized case is already reported as a clash
	public void testInheritedDefaultOverrides03() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					import java.util.List;
					public interface I1 {
						String foo(List<String> l);
					}
					""",
				"I2.java",
				"""
					import java.util.List;
					public interface I2 {
					   @SuppressWarnings("rawtypes")
						default String foo(List l) { return ""; }
					}
					""",
				"I3.java",
				"""
					import java.util.List;
					public interface I3 extends I1, I2 {
					   @Override
					   String foo(List<String> l);
					}
					""",
			},
			"""
				----------
				1. ERROR in I3.java (at line 4)
					String foo(List<String> l);
					       ^^^^^^^^^^^^^^^^^^^
				Name clash: The method foo(List<String>) of type I3 has the same erasure as foo(List) of type I2 but does not override it
				----------
				""");
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Parameterized case is already reported as a clash - inverse case of previous
	public void testInheritedDefaultOverrides04() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					import java.util.List;
					public interface I1 {
						default String foo(List<String> l) { return ""; }
					}
					""",
				"I2.java",
				"""
					import java.util.List;
					public interface I2 {
					   @SuppressWarnings("rawtypes")
						String foo(List l);
					}
					""",
				"I3.java",
				"""
					import java.util.List;
					public interface I3 extends I1, I2 {
					   @Override
					   String foo(List<String> l);
					}
					""",
			},
			"""
				----------
				1. ERROR in I3.java (at line 4)
					String foo(List<String> l);
					       ^^^^^^^^^^^^^^^^^^^
				Name clash: The method foo(List<String>) of type I3 has the same erasure as foo(List) of type I2 but does not override it
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public interface X extends Map<String, Object> {
					   Zork z;
					}
					
					interface Map<K,V> extends MapStream<K, V>  {
					   @Override
						default Iterable<BiValue<K, V>> asIterable() {
							return null;
						}
					}
					interface MapStream<K, V> {
						Iterable<BiValue<K, V>> asIterable();
					}
					
					interface BiValue<T, U> {
					    T getKey();
					    U getValue();
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public interface X extends Map<String, Object> {
					   Zork z;
					}
					
					interface Map<K,V> extends MapStream<K, V>  {
					   @Override
						Iterable<BiValue<K, V>> asIterable();
					}
					interface MapStream<K, V> {
						default Iterable<BiValue<K, V>> asIterable() {
					       return null;
					   }
					}
					
					interface BiValue<T, U> {
					    T getKey();
					    U getValue();
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}

	public void testDefaultNonclash3() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public interface X extends Map<String, Object> {
					   Zork z;
					}
					
					interface Map<K,V> extends MapStream<K, V>  {
					   @Override
						default Iterable<BiValue<K, V>> asIterable() {
					       return null;
					   }
					}
					interface MapStream<K, V> {
						default Iterable<BiValue<K, V>> asIterable() {
					       return null;
					   }
					}
					
					interface BiValue<T, U> {
					    T getKey();
					    U getValue();
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390761
	public void testDefaultNonclash4() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public interface X extends Map<String, Object> {
					   Zork z;
					}
					
					interface Map<K,V> extends MapStream<K, V>  {
					   @Override
						Iterable<BiValue<K, V>> asIterable();
					}
					interface MapStream<K, V> {
						Iterable<BiValue<K, V>> asIterable();
					}
					
					interface BiValue<T, U> {
					    T getKey();
					    U getValue();
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420080
	public void testDefaultNonclash5() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends G implements I {
					}
					
					interface I {
						default int foo (){
							return 0;
						}
					}
					
					class G {
						public int foo() {
							return 0;
						}
					}
					"""
			});
	}

	// JLS 9.4.1
	// Bug 382347 - [1.8][compiler] Compiler accepts incorrect default method inheritance
	// Don't report conflict between the same method inherited on two paths.
	public void testInheritedDefaultOverrides05() {
		runConformTest(
			new String[] {
				"StringList.java",
				"""
					import java.util.Collection;
					public abstract class StringList implements Collection<String> {
					}
					"""
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
				"""
					import java.util.*;
					public interface IterableList<E> extends Iterable<E>, List<E> {}
					interface ListIterable<E> extends Iterable<E>, List<E> {}
					
					"""
			},
			"");
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface
	public void testAbstract01() {
		runConformTest(
			new String[] {
				"I2.java",
				"""
					public interface I2 {
					    void test();
					}
					""",
				"I1.java",
				"""
					public interface I1 extends I2 {
					    default void test() {}
					}
					""",
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
				"""
					public interface I1 {
					    void test();
					}
					""",
				"I2.java",
				"""
					public interface I2 {
					    default void test() {}
					}
					""",
				"C.java",
				"public class C implements I1, I2 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in C.java (at line 1)
					public class C implements I1, I2 {
					             ^
				The default method test() inherited from I2 conflicts with another method inherited from I1
				----------
				""");
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
				"""
					public interface I1 {
					    void test();
					}
					""",
				"I2.java",
				"""
					public interface I2 {
					    default void test() {}
					}
					""",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in C.java (at line 1)
					public class C implements I2, I1 {
					             ^
				The default method test() inherited from I2 conflicts with another method inherited from I1
				----------
				""");
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
				"""
					public interface I1 {
					    void test();
					}
					""",
				"I2.java",
				"""
					public interface I2 {
					    default void test() {}
					}
					""",
				"C.java",
				"public abstract class C implements I2, I1 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in C.java (at line 1)
					public abstract class C implements I2, I1 {
					                      ^
				The default method test() inherited from I2 conflicts with another method inherited from I1
				----------
				""");
	}

	// same as above but only interfaces
	public void testAbstract02c() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
					    void test();
					}
					""",
				"I2.java",
				"""
					public interface I2 {
					    default void test() {}
					}
					""",
				"I3.java",
				"public interface I3 extends I1, I2 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in I3.java (at line 1)
					public interface I3 extends I1, I2 {
					                 ^^
				The default method test() inherited from I2 conflicts with another method inherited from I1
				----------
				""");
	}

	// JLS 8.1.1.1 abstract Classes
	// Default method overrides an abstract method from its super interface - class implements both
	public void testAbstract03() {
		runConformTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
					    void test();
					}
					""",
				"I2.java",
				"""
					public interface I2 extends I1 {
					    @Override
					    default void test() {}
					}
					""",
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
				"""
					public interface I1 {
					    void test();
					}
					""",
				"I2.java",
				"""
					public interface I2 extends I1 {
					    @Override
					    default void test() {}
					}
					""",
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
				"""
					public interface I1 {
					    default void test() {}
					}
					""",
				"I2.java",
				"""
					public interface I2 extends I1 {
					    @Override
					    void test();
					}
					""",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in C.java (at line 1)
					public class C implements I2, I1 {
					             ^
				The type C must implement the inherited abstract method I2.test()
				----------
				""");
	}

	// JLS 8.1.1.1 abstract Classes
	// default method is not inherited because a more specific abstract method is.
	// same as above except for order of implements list
	public void testAbstract04a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
					    default void test() {}
					}
					""",
				"I2.java",
				"""
					public interface I2 extends I1 {
					    @Override
					    void test();
					}
					""",
				"C.java",
				"public class C implements I2, I1 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in C.java (at line 1)
					public class C implements I2, I1 {
					             ^
				The type C must implement the inherited abstract method I2.test()
				----------
				""");
	}

	// abstract class method trumps otherwise conflicting default methods: the conflict scenario
	public void testAbstract05() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						default String value1() { return null; }
					}
					""",
				"I2.java",
				"public interface I2 {\n" +
				"	default String value1() { return \"\"; }\n" + // conflicts with other default method
				"}\n",
				"C2.java",
				"public abstract class C2 implements I1, I2 {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in C2.java (at line 1)
					public abstract class C2 implements I1, I2 {
					                      ^^
				Duplicate default methods named value1 with the parameters () and () are inherited from the types I2 and I1
				----------
				""");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict resolved
	public void testAbstract06() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						default String value1() { return null; }
					}
					""",
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
				"""
					public class C3 extends C2 {
						@Override
						public Object value1() { return this; } // too week, need a method returning String
					}
					"""
			},
			"""
				----------
				1. ERROR in C3.java (at line 3)
					public Object value1() { return this; } // too week, need a method returning String
					       ^^^^^^
				The return type is incompatible with I1.value1()
				----------
				2. ERROR in C3.java (at line 3)
					public Object value1() { return this; } // too week, need a method returning String
					       ^^^^^^
				The return type is incompatible with I2.value1()
				----------
				""");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict resolved
	// variant: second method is not a default method
	public void testAbstract06a() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						default String value1() { return null; }
					}
					""",
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
				"""
					public class C3 extends C2 {
						@Override
						public Object value1() { return this; } // too week, need a method returning String
					}
					"""
			},
			"""
				----------
				1. ERROR in C3.java (at line 3)
					public Object value1() { return this; } // too week, need a method returning String
					       ^^^^^^
				The return type is incompatible with I2.value1()
				----------
				2. ERROR in C3.java (at line 3)
					public Object value1() { return this; } // too week, need a method returning String
					       ^^^^^^
				The return type is incompatible with I1.value1()
				----------
				""");
	}

	// abstract class method trumps otherwise conflicting default methods: conflict not resolved due to insufficient visibility
	public void testAbstract6b() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						default String value1() { return null; }
					}
					""",
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
			"""
				----------
				1. ERROR in C2.java (at line 1)
					public abstract class C2 extends p1.C1 implements I1, I2 {
					                      ^^
				Duplicate default methods named value1 with the parameters () and () are inherited from the types I2 and I1
				----------
				""");
	}

	// abstract class method trumps otherwise conflicting default method: only one default method
	public void testAbstract07() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						default String value1() { return null; }
					}
					""",
				"C1.java",
				"public abstract class C1 {\n" +
				"	abstract Object value1();\n" + // trumps the conflicting method (without overriding)
				"}\n",
				"C2.java",
				"public abstract class C2 extends C1 implements I1 {\n" +
				"}\n",
				"C3.java",
				"""
					public class C3 extends C2 {
						@Override
						public Object value1() { return this; } // too week, need a method returning String
					}
					"""
			},
			"""
				----------
				1. ERROR in C3.java (at line 3)
					public Object value1() { return this; } // too week, need a method returning String
					       ^^^^^^
				The return type is incompatible with I1.value1()
				----------
				""");
	}

	// class inherits two override equivalent methods,
	// must be declared abstract, although one of the methods is a default method.
	public void testAbstract08() {
		runNegativeTest(
			new String[] {
				"I1.java",
				"""
					public interface I1 {
						default String value() { return null; }
					}
					""",
				"C1.java",
				"""
					public abstract class C1 {
						public abstract String value();\
					}
					""",
				"C2.java",
				"public class C2 extends C1 implements I1 {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in C2.java (at line 1)
					public class C2 extends C1 implements I1 {
					             ^^
				The type C2 must implement the inherited abstract method C1.value()
				----------
				""");
	}

	// an annotation type cannot have default methods
	public void testAnnotation1() {
		runNegativeTest(
			new String[] {
				"I.java",
				"""
					public @interface I {
					    default String id() { return "1"; }
					}
					"""
			},
			"""
				----------
				1. ERROR in I.java (at line 2)
					default String id() { return "1"; }
					^^^^^^^
				Syntax error on token "default", @ expected
				----------
				""");
	}

	// basic situation similar to AmbiguousMethodTest.test009()
	public void testSuperCall1() throws Exception {
		this.runConformTest(
			new String[] {
				"OrderedSet.java",
				"""
					import java.util.*;
					import java.util.stream.Stream;
					public interface OrderedSet<E> extends List<E>, Set<E> {
						@Override
						boolean add(E o);
						@Override
						default Spliterator<E> spliterator() { if (true) return List.super.spliterator(); else return Set.super.spliterator(); }
					}
					"""
			},
			""
		);
		String expectedOutput =
				"""
			  // Method descriptor #14 ()Ljava/util/Spliterator;
			  // Signature: ()Ljava/util/Spliterator<TE;>;
			  // Stack: 1, Locals: 1
			  public java.util.Spliterator spliterator();
			    0  aload_0 [this]
			    1  invokespecial java.util.List.spliterator() : java.util.Spliterator [17]
			    4  areturn
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "OrderedSet.class", "OrderedSet", expectedOutput);
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
				"   default Spliterator<E> spliterator() { return List.super.spliterator(); }\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in T.java (at line 6)
					return List.super.stream(); // List is not a direct super interface
					       ^^^^^^^^^^
				Illegal reference to super type List, cannot bypass the more specific direct super type OrderedSet
				----------
				2. ERROR in T.java (at line 12)
					return OrderedSet.super.stream(); // not a super interface of the direct enclosing class
					       ^^^^^^^^^^^^^^^^
				No enclosing instance of the type OrderedSet<E> is accessible in scope
				----------
				3. ERROR in T.java (at line 17)
					OrderedSet.super.add(o); // target not a default method
					^^^^^^^^^^^^^^^^^^^^^^^
				Cannot directly invoke the abstract method add(E) for the type OrderedSet<E>
				----------
				4. ERROR in T.java (at line 20)
					OrderedSet.super(); // not applicable for super ctor call
					^^^^^^^^^^
				Illegal enclosing instance specification for type Object
				----------
				"""
		);
	}

	// with execution
	public void testSuperCall3() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X implements I2 {
						@Override
						public void print() {
							I2.super.print();
							System.out.print("!");\
						}
						public static void main(String... args) {
							new X().print();
						}
					}
					interface I1 {
						default void print() {
							System.out.print("O");
						}
					}
					interface I2 extends I1 {
						default void print() {
							I1.super.print();
							System.out.print("K");
						}
					}
					"""
			},
			"OK!"
		);
	}

	// 15.12.1
	// https://bugs.eclipse.org/404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
	public void testSuperCall4() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X implements I2, I1 {
						@Override
						public void print() {
							I1.super.print(); // illegal attempt to skip I2.print()
							System.out.print("!");\
						}
						public static void main(String... args) {
							new X().print();
						}
					}
					interface I1 {
						default void print() {
							System.out.print("O");
						}
					}
					interface I2 extends I1 {
						@Override default void print() {
							System.out.print("K");
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					I1.super.print(); // illegal attempt to skip I2.print()
					^^^^^^^^
				Illegal reference to super type I1, cannot bypass the more specific direct super type I2
				----------
				"""
		);
	}

	// 15.12.1
	// https://bugs.eclipse.org/404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
	public void testSuperCall5() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X implements I2, I1 {
						@Override
						public void print() {
							I1.super.print(); // illegal attempt to skip I2.print()
							System.out.print("!");\
						}
						public static void main(String... args) {
							new X().print();
						}
					}
					interface I1 {
						default void print() {
							System.out.print("O");
						}
					}
					interface I2 extends I1 {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					I1.super.print(); // illegal attempt to skip I2.print()
					^^^^^^^^
				Illegal reference to super type I1, cannot bypass the more specific direct super type I2
				----------
				"""
		);
	}

	// 15.12.3
	// https://bugs.eclipse.org/404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
	public void testSuperCall6() {
		this.runNegativeTest(
			new String[] {
				"SuperOverride.java",
				"""
					interface I0 {
						default void foo() { System.out.println("I0"); }
					}
					
					interface IA extends I0 {}
					
					interface IB extends I0 {
						@Override default void foo() {
							System.out.println("IB");
						}
					}
					interface IX extends IA, IB {
						@Override default void foo() {
							IA.super.foo(); // illegal attempt to skip IB.foo()
						}
					}
					public class SuperOverride implements IX {
						public static void main(String[] args) {
							new SuperOverride().foo();
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in SuperOverride.java (at line 14)
					IA.super.foo(); // illegal attempt to skip IB.foo()
					^^^^^^^^^^^^^^
				Illegal reference to super method foo() from type I0, cannot bypass the more specific override from type IB
				----------
				"""
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test for different legal and illegal keywords for static and default methods in interfaces
	public void testStaticMethod01() {
		runNegativeTest(
				new String[] {
					"I.java",
					"""
						public interface I {
							static void foo() {}
							static void foo1();
							public static default void foo2 () {};
							abstract static void foo3();
							abstract static void foo4() {}
						}"""
				},
				"""
					----------
					1. ERROR in I.java (at line 3)
						static void foo1();
						            ^^^^^^
					This method requires a body instead of a semicolon
					----------
					2. ERROR in I.java (at line 4)
						public static default void foo2 () {};
						                           ^^^^^^^
					Illegal combination of modifiers for the interface method foo2; only one of abstract, default, or static permitted
					----------
					3. ERROR in I.java (at line 5)
						abstract static void foo3();
						                     ^^^^^^
					Illegal combination of modifiers for the interface method foo3; only one of abstract, default, or static permitted
					----------
					4. ERROR in I.java (at line 6)
						abstract static void foo4() {}
						                     ^^^^^^
					Illegal combination of modifiers for the interface method foo4; only one of abstract, default, or static permitted
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test invocation of static methods with different contexts - negative tests
	public void testStaticMethod02() {
		runNegativeTest(
				new String[] {
					"I.java",
					"""
						public interface I {
							public static void foo() {
								bar();
								this.bar();
						   }
							public default void bar () {
								this.foo();
							}
						}
						interface II extends I{
							public static void foobar() {
								super.bar();
						   }
						}
						"""
				},
				"""
					----------
					1. ERROR in I.java (at line 3)
						bar();
						^^^
					Cannot make a static reference to the non-static method bar() from the type I
					----------
					2. ERROR in I.java (at line 4)
						this.bar();
						^^^^
					Cannot use this in a static context
					----------
					3. ERROR in I.java (at line 7)
						this.foo();
						     ^^^
					This static method of interface I can only be accessed as I.foo
					----------
					4. ERROR in I.java (at line 12)
						super.bar();
						^^^^^
					Cannot use super in a static context
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test invocation of static methods with different contexts - positive tests
	public void testStaticMethod03() throws Exception {
		runConformTest(
			new String[] {
				"C.java",
				"""
					interface I {
						public static void foo() {
							System.out.println("I#foo() invoked");
					   }
					}
					interface J extends I {
						public static void foo() {
							System.out.println("J#foo() invoked");
					   }
						public default void bar () {
							foo();
						}
					}
					public class C implements J {
						public static void main(String[] args) {
							C c = new C();
							c.bar();
					       J.foo();
					       I.foo();
						}
					}"""
			},
			"""
				J#foo() invoked
				J#foo() invoked
				I#foo() invoked""");
		String expectedOutput =
				"""
			  // Method descriptor #17 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public static void main(java.lang.String[] args);
			     0  new C [1]
			     3  dup
			     4  invokespecial C() [18]
			     7  astore_1 [c]
			     8  aload_1 [c]
			     9  invokevirtual C.bar() : void [19]
			    12  invokestatic J.foo() : void [22]
			    15  invokestatic I.foo() : void [25]
			    18  return
			      Line numbers:
			        [pc: 0, line: 16]
			        [pc: 8, line: 17]
			        [pc: 12, line: 18]
			        [pc: 15, line: 19]
			        [pc: 18, line: 20]
			      Local variable table:
			        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]
			        [pc: 8, pc: 19] local: c index: 1 type: C
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "C.class", "C", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test invocation of static methods with different contexts - negative tests
	public void testStaticMethod04() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X implements II {
								@Override\
								public void foo() {
									 bar();
									 bar2();
								}
								public static void main(String[] args) {
									bar();
									II.bar();
									(new X()).bar();
									II.bar();
									II ii = new X();
									ii.bar();
									ii.bar2();
									I i = new X();
									i.bar();
							      new I() {}.foo();
								}
							}
							interface I {
								public static void bar() {
									bar2();
								}
								public default void bar2() {
									bar();
								}
							}
							interface II extends I {
								public default void foo() {
									bar();
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						bar();
						^^^
					The method bar() is undefined for the type X
					----------
					2. ERROR in X.java (at line 7)
						bar();
						^^^
					The method bar() is undefined for the type X
					----------
					3. ERROR in X.java (at line 8)
						II.bar();
						   ^^^
					The method bar() is undefined for the type II
					----------
					4. ERROR in X.java (at line 9)
						(new X()).bar();
						          ^^^
					The method bar() is undefined for the type X
					----------
					5. ERROR in X.java (at line 10)
						II.bar();
						   ^^^
					The method bar() is undefined for the type II
					----------
					6. ERROR in X.java (at line 12)
						ii.bar();
						   ^^^
					The method bar() is undefined for the type II
					----------
					7. ERROR in X.java (at line 15)
						i.bar();
						  ^^^
					This static method of interface I can only be accessed as I.bar
					----------
					8. ERROR in X.java (at line 16)
						new I() {}.foo();
						           ^^^
					The method foo() is undefined for the type new I(){}
					----------
					9. ERROR in X.java (at line 21)
						bar2();
						^^^^
					Cannot make a static reference to the non-static method bar2() from the type I
					----------
					10. ERROR in X.java (at line 29)
						bar();
						^^^
					The method bar() is undefined for the type II
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod05() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							interface I {
								static void foo(int x) { }
							}
							interface II extends I {
								static void goo(int x) {}   		// No Error.
							}
							interface III extends II {
								default void foo(int x, int y) {}   // No Error.
								default void goo() {}   			// No Error.
								default void foo(int x) {}   		// No Error.
								default void goo(int x) {}   		// No Error.
							}
							class Y {
								static void goo(int x) {}
							}
							class X extends Y {
								void foo(int x) {}   // No error.
								void goo() {}   	 // No Error.
								void goo(int x) {}   // Error.
							}
							"""
						},
						"""
							----------
							1. ERROR in X.java (at line 19)
								void goo(int x) {}   // Error.
								     ^^^^^^^^^^
							This instance method cannot override the static method from Y
							----------
							""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test that extending interfaces inherit visible fields and inner types.
	public void testStaticMethod06() {
		runConformTest(
				new String[] {
					"C.java",
					"""
						interface I {
							public static String CONST = "CONSTANT";
							public static void foo(String[] args) {
								System.out.println(args[0]);
						   }
						 	public interface Inner {}
						}
						interface J extends I {
							public static void foo() {
								I.foo(new String[]{CONST});
						   }
						 	public interface InnerInner extends Inner {}
						}
						public class C implements J {
							public static void main(String[] args) {
						       J.foo();
						       I.foo(new String[]{"LITERAL"});
							}
						}"""
				},
				"CONSTANT\n" +
				"LITERAL");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	// Test that type parameter from enclosing type is not allowed to be referred to in static interface methods
	public void testStaticMethod07() {
		runNegativeTest(
				new String[] {
					"C.java",
					"""
						interface I <T> {
							public static T foo(T t) {
								return t;\
						   }
						}
						"""
				},
				"""
					----------
					1. ERROR in C.java (at line 2)
						public static T foo(T t) {
						              ^
					Cannot make a static reference to the non-static type T
					----------
					2. ERROR in C.java (at line 2)
						public static T foo(T t) {
						                    ^
					Cannot make a static reference to the non-static type T
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod08() {
		runNegativeTest(
				new String[] {
					"C.java",
					"""
						@interface A {
							static String foo() default "Blah";
						}
						"""
				},
				"""
					----------
					1. ERROR in C.java (at line 2)
						static String foo() default "Blah";
						              ^^^^^
					Illegal modifier for the annotation attribute A.foo; only public & abstract are permitted
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod09() {
		runNegativeTest(
				new String[] {
						"C.java",
						"""
							interface A {
								static void foo() {}
								default void goo(A a) {
									a.foo();
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in C.java (at line 4)
						a.foo();
						  ^^^
					This static method of interface A can only be accessed as A.foo
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod10() {
		runNegativeTest(
				new String[] {
						"C.java",
						"""
							interface A {
								static void foo(long x) {}
								static void foo(int x) {}
								default void goo(A a) {
									a.foo(10);
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in C.java (at line 5)
						a.foo(10);
						  ^^^
					This static method of interface A can only be accessed as A.foo
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod11() {
		runNegativeTest(
				new String[] {
						"C.java",
						"""
							interface A<X> {
								void foo(X x);
							}
							interface B extends A<String> {
							    static void foo(String s) {}
							}
							"""
				},
				"""
					----------
					1. ERROR in C.java (at line 5)
						static void foo(String s) {}
						            ^^^^^^^^^^^^^
					This static method cannot hide the instance method from A<String>
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod12() {
		runNegativeTest(
				new String[] {
						"C.java",
						"""
							interface A<X> {
								static void foo(String x) {}
							}
							interface B extends A<String> {
							    static void foo(String s) {}
							}
							public class X {
							}
							"""
				},
				"""
					----------
					1. WARNING in C.java (at line 1)
						interface A<X> {
						            ^
					The type parameter X is hiding the type X
					----------
					2. ERROR in C.java (at line 7)
						public class X {
						             ^
					The public type X must be defined in its own file
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780
	public void testStaticMethod13() {
		runNegativeTest(
				new String[] {
						"C.java",
						"""
							interface A {
								static void foo(String x) {
							       System.out.println(this);
							       System.out.println(super.hashCode());
							   }
							}
							"""
				},
				"""
					----------
					1. ERROR in C.java (at line 3)
						System.out.println(this);
						                   ^^^^
					Cannot use this in a static context
					----------
					2. ERROR in C.java (at line 4)
						System.out.println(super.hashCode());
						                   ^^^^^
					Cannot use super in a static context
					----------
					""");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406619, [1.8][compiler] Incorrect suggestion that method can be made static.
	public void test406619() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
		compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X {
						default int foo() {
							return 10;
						}
					}
					"""
			},
			"",
			null /* no extra class libraries */,
			true /* flush output directory */,
			compilerOptions /* custom options */
		);
	}

	// class implements interface with default method.
	// - witness for NoSuchMethodError in synthetic method (SuperMethodAccess) - turned out to be a JVM bug
	public void testSuperAccess01() {
		runConformTest(
			new String[] {
				"C.java",
				"""
					interface I {
					    public default void foo() {
					        System.out.println("default");
					    }
					}
					public class C implements I {
					    public static void main(String[] args) {
					        C c = new C();
					        c.foo();
					    }
					}
					"""
			},
			"default"
			);
	}

	// class implements interface with default method.
	// - intermediate public interface
	public void testSuperAccess02() {
		runConformTest(
			new String[] {
				"p1/C.java",
				"""
					package p1;
					public class C implements p2.J {
					    public static void main(String[] args) {
					        C c = new C();
					        c.foo();
					    }
					}
					""",
				"p2/J.java",
				"""
					package p2;
					interface I {
					    public default void foo() {
					        System.out.println("default");
					    }
					}
					public interface J extends I {}
					"""
			},
			"default");
	}

	// https://bugs.eclipse.org/421796 - Bug 421796 - [1.8][compiler] java.lang.AbstractMethodError executing default method code.
	public void testSuperAccess03() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I  {
					    void foo();\s
					}
					
					interface J extends I {
					    default void foo() {
					    }
					}
					
					interface K extends J {
					}
					
					public class X implements K {
						public static void main(String argv[]) {
							X test = new X();
							((J)test).foo();
							test.foo();
						}
					}
					"""
			});
	}

	// Variant of test MethodVerifyTest.test144() from https://bugs.eclipse.org/bugs/show_bug.cgi?id=194034
	public void testBridge01() {
		this.runNegativeTest(
			new String[] {
				"PurebredCatShopImpl.java",
				"""
					import java.util.List;
					interface Pet {}
					interface Cat extends Pet {}
					interface PetShop { default List<Pet> getPets() { return null; } }
					interface CatShop extends PetShop {
						default <V extends Pet> List<? extends Cat> getPets() { return null; }
					}
					interface PurebredCatShop extends CatShop {}
					class CatShopImpl implements CatShop {
						@Override public List<Pet> getPets() { return null; }
					}
					class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {}"""
			},
			"""
				----------
				1. ERROR in PurebredCatShopImpl.java (at line 6)
					default <V extends Pet> List<? extends Cat> getPets() { return null; }
					                                            ^^^^^^^^^
				Name clash: The method getPets() of type CatShop has the same erasure as getPets() of type PetShop but does not override it
				----------
				2. WARNING in PurebredCatShopImpl.java (at line 10)
					@Override public List<Pet> getPets() { return null; }
					                 ^^^^
				Type safety: The return type List<Pet> for getPets() from the type CatShopImpl needs unchecked conversion to conform to List<? extends Cat> from the type CatShop
				----------
				"""
		);
	}
	// yet another variant, checking that exactly one bridge method is created, so that
	// the most specific method is dynamically invoked via all declared types.
	public void testBridge02() {
		this.runConformTest(
			new String[] {
				"PurebredCatShopImpl.java",
				"""
					import java.util.List;
					import java.util.ArrayList;
					interface Pet {}
					interface Cat extends Pet {}
					interface PetShop { default List<Pet> getPets() { return null; } }
					interface CatShop extends PetShop {
						@Override default ArrayList<Pet> getPets() { return null; }
					}
					interface PurebredCatShop extends CatShop {}
					class CatShopImpl implements CatShop {
						@Override public ArrayList<Pet> getPets() { return new ArrayList<>(); }
					}
					public class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {
						public static void main(String... args) {
							PurebredCatShopImpl pcsi = new PurebredCatShopImpl();
							System.out.print(pcsi.getPets().size());
							CatShopImpl csi = pcsi;
							System.out.print(csi.getPets().size());
							CatShop cs = csi;
							System.out.print(cs.getPets().size());
							PetShop ps = cs;
							System.out.print(ps.getPets().size());
						}
					}
					"""
			},
			"0000"
		);
	}

	// modeled after org.eclipse.jdt.core.tests.compiler.regression.AmbiguousMethodTest.test081()
	// see https://bugs.eclipse.org/391376 - [1.8] check interaction of default methods with bridge methods and generics
    // see https://bugs.eclipse.org/404648 - [1.8][compiler] investigate differences between compilers re AmbiguousMethodTest
	public void _testBridge03() {
		runConformTest(
			new String[] {
				"C.java",
				"""
					interface A<ModelType extends D, ValueType> extends
							I<ModelType, ValueType> {
					
						@Override
						public default void doSet(ModelType valueGetter) {
							this.set((ValueType) valueGetter.getObject());
						}
					
						@Override
						public default void set(Object object) {
							System.out.println("In A.set(Object)");
						}
					}
					
					class B implements A<E, CharSequence> {
					
						public void set(CharSequence string) {
							System.out.println("In B.set(CharSequence)");
						}
					}
					
					public class C extends B {
					
						static public void main(String[] args) {
							C c = new C();
							c.run();
						}
					
						public void run() {
							E e = new E<String>(String.class);
							this.doSet(e);
						}
					
					}
					
					class D {
						public Object getObject() {
							return null;
						}
					}
					
					class E<Type extends CharSequence> extends D {
					
						private Class<Type> typeClass;
					
						public E(Class<Type> typeClass) {
							this.typeClass = typeClass;
						}
					
						@Override
						public Type getObject() {
							try {
								return (Type) typeClass.newInstance();
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					
					}
					
					interface I<ModelType, ValueType> {
					
						public void doSet(ModelType model);
					
						public void set(ValueType value);
					
					}
					"""
			},
			"In B.set(CharSequence)");
	}


    // modeled after org.eclipse.jdt.core.tests.compiler.regression.AmbiguousMethodTest.test081()
    // see https://bugs.eclipse.org/391376 - [1.8] check interaction of default methods with bridge methods and generics
    // see https://bugs.eclipse.org/404648 - [1.8][compiler] investigate differences between compilers re AmbiguousMethodTest
    public void _testBridge04() {
        runConformTest(
            new String[] {
                "C.java",
                """
					interface A<ModelType extends D, ValueType> extends
					       I<ModelType, ValueType> {
					
					   @Override
					   public default void doSet(ModelType valueGetter) {
					       this.set((ValueType) valueGetter.getObject());
					   }
					
					   @Override
					   public default void set(Object object) {
					       System.out.println("In A.set(Object)");
					   }
					}
					
					interface B extends A<E, CharSequence> {
					
					   public default void set(CharSequence string) {
					       System.out.println("In B.set(CharSequence)");
					   }
					}
					
					public class C implements B {
					
					   static public void main(String[] args) {
					       C c = new C();
					       c.run();
					   }
					
					   public void run() {
					       E e = new E<String>(String.class);
					       this.doSet(e);
					   }
					
					}
					
					class D {
					   public Object getObject() {
					       return null;
					   }
					}
					
					class E<Type extends CharSequence> extends D {
					
					   private Class<Type> typeClass;
					
					   public E(Class<Type> typeClass) {
					       this.typeClass = typeClass;
					   }
					
					   @Override
					   public Type getObject() {
					       try {
					           return (Type) typeClass.newInstance();
					       } catch (Exception e) {
					           throw new RuntimeException(e);
					       }
					   }
					
					}
					
					interface I<ModelType, ValueType> {
					
					   public void doSet(ModelType model);
					
					   public void set(ValueType value);
					
					}
					"""
            },
            "In B.set(CharSequence)");
    }

    // test for different error messages in modifiers.
	public void test400977() {
		String infMod = this.complianceLevel >= ClassFileConstants.JDK9 ? " private," : "";
		String extra = this.complianceLevel >= ClassFileConstants.JDK17 ?
				"""
					----------
					2. WARNING in I.java (at line 3)
						public abstract default strictfp final void bar();}
						                        ^^^^^^^^
					Floating-point expressions are always strictly evaluated from source level 17. Keyword 'strictfp' is not required.
					""" : "";
		int offset = this.complianceLevel >= ClassFileConstants.JDK17 ? 1 : 0;
		runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I {
				    default abstract void foo();
				    public abstract default strictfp final void bar();\
				}
				"""},
			"----------\n" +
			"1. ERROR in I.java (at line 2)\n" +
			"	default abstract void foo();\n" +
			"	                      ^^^^^\n" +
			"Illegal combination of modifiers for the interface method foo; only one of abstract, default, or static permitted\n" +
			extra +
			"----------\n" +
			(2 + offset) + ". ERROR in I.java (at line 3)\n" +
			"	public abstract default strictfp final void bar();}\n" +
			"	                                            ^^^^^\n" +
			"strictfp is not permitted for abstract interface method bar\n" +
			"----------\n" +
			(3 + offset) + ". ERROR in I.java (at line 3)\n" +
			"	public abstract default strictfp final void bar();}\n" +
			"	                                            ^^^^^\n" +
			"Illegal combination of modifiers for the interface method bar; only one of abstract, default, or static permitted\n" +
			"----------\n" +
			(3 + offset) + ". ERROR in I.java (at line 3)\n" +
			"	public abstract default strictfp final void bar();}\n" +
			"	                                            ^^^^^\n" +
			"Illegal modifier for the interface method bar; only public,"+ infMod +" abstract, default, static and strictfp are permitted\n" +
			"----------\n");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=420084,  [1.8] static interface method cannot be resolved without receiver when imported statically
	public void testBug420084() {
		runNegativeTest(
			new String[] {
				"p/J.java",
				"""
					package p;
					public interface J {
						static int foo(){return 0;}
					}
					""",
				"I.java",
				"""
					import static p.J.foo;
					public interface I {
						static int call() {
							return foo();
						}
					}
					"""
			},
			"");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421543, [1.8][compiler] Compiler fails to recognize default method being turned into abstract by subtytpe
	public void testBug421543() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I  {
						default void foo() {}
					}
					interface J extends I {
						void foo();
					}
					public class X implements J {
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					void foo();
					     ^^^^^
				The method foo() of type J should be tagged with @Override since it actually overrides a superinterface method
				----------
				2. ERROR in X.java (at line 7)
					public class X implements J {
					             ^
				The type X must implement the inherited abstract method J.foo()
				----------
				""");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421543, [1.8][compiler] Compiler fails to recognize default method being turned into abstract by subtytpe
	public void testBug421543a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I<T>  {
						default void foo(T t) {}
					}
					interface J extends I<J> {
						void foo(J t);
					}
					public class X implements J {
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					void foo(J t);
					     ^^^^^^^^
				The method foo(J) of type J should be tagged with @Override since it actually overrides a superinterface method
				----------
				2. ERROR in X.java (at line 7)
					public class X implements J {
					             ^
				The type X must implement the inherited abstract method J.foo(J)
				----------
				""");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421543, [1.8][compiler] Compiler fails to recognize default method being turned into abstract by subtytpe
	public void testBug421543b() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I<T>  {
						void foo(T t);
					}
					@SuppressWarnings("override")
					interface J extends I<J> {
						default void foo(J t) {}
					}
					public class X implements J {
					}
					"""
			},
			"");
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421797, [1.8][compiler] ClassFormatError with default methods & I.super.foo() syntax
	public void testBug421797() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					interface I {
						int m(String s, int val);
						public default int foo(String s, int val) {
							System.out.print(s + " from I.foo:");
							return val * val;\s
						}
					}
					interface T extends I {
						public default int m(String s, int value) {\s
							I i = I.super::foo;\s
							return i.foo(s, value);
						}
					}
					public class X {
						public static void main(String argv[]) { \s
							System.out.println(new T(){}.m("Hello", 1234));
						}
					}
					"""
			},
			"Hello from I.foo:1522756");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422731, [1.8] Ambiguous method not reported on overridden default method
	public void test422731() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X extends Base implements I {
							public static void main(String[] args) {
								X x = new X();
								x.foo((short)5, (short)10);
								x.bar((short)5, (short)10);
							}
							public void foo(short s, int i) {} // No error, but should have been
							public void bar(short s, int i) {} // Correctly reported
						
						}
						interface I {
							public default void foo(int i, short s) {}
						}
						class Base {
							public void bar(int i, short s) {}
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					x.foo((short)5, (short)10);
					  ^^^
				The method foo(short, int) is ambiguous for the type X
				----------
				2. ERROR in X.java (at line 5)
					x.bar((short)5, (short)10);
					  ^^^
				The method bar(short, int) is ambiguous for the type X
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler
	public void test425719() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						   default void foo(Object obj) {
							   System.out.println("interface method");
						   }
						}
						class Base {
						    public void foo(Object obj) {
						        System.out.println("class method");
						   }
						}
						public class X extends Base implements I {
							 public static void main(String argv[]) {
							    	new X().foo(null);
							    }
						}
						""",
			},
			"class method");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425718, [1.8] default method changes access privilege of protected overridden method from Object
	public void test425718() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface I {
						   default Object clone() { return null; };
						}
						public class X  {
						    public void foo() {
						        I x = new I(){};
						        System.out.println(x.clone());
						    }
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					I x = new I(){};
					          ^^^
				The inherited method Object.clone() cannot hide the public abstract method in I
				----------
				2. ERROR in X.java (at line 6)
					I x = new I(){};
					          ^^^
				Exception CloneNotSupportedException in throws clause of Object.clone() is not compatible with I.clone()
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426318, [1.8][compiler] Bogus name clash error in the presence of default methods and varargs
	public void test426318() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						abstract class Y {\s
						    public abstract void foo(Object[] x);
						    public abstract void goo(Object[] x);
						}
						interface I {
						   default public <T> void foo(T... x) {};
						   public abstract <T> void goo(T ... x);
						}
						public abstract class X extends Y implements I {\s
						}
						""",
			},
			"""
				----------
				1. WARNING in X.java (at line 6)
					default public <T> void foo(T... x) {};
					                                 ^
				Type safety: Potential heap pollution via varargs parameter x
				----------
				2. WARNING in X.java (at line 7)
					public abstract <T> void goo(T ... x);
					                                   ^
				Type safety: Potential heap pollution via varargs parameter x
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424914, [1.8][compiler] No error shown for method reference with super enclosed in an interface
	public void test424914() throws Exception {
		this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						interface A {
							String foo();
							String b = super.toString();
							default void fun1() {
								System.out.println((A) super::toString);
								super.toString();
								Object.super.toString();
							}
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					String b = super.toString();
					           ^^^^^
				Cannot use super in a static context
				----------
				2. ERROR in X.java (at line 5)
					System.out.println((A) super::toString);
					                       ^^^^^
				super reference is illegal in interface context
				----------
				3. ERROR in X.java (at line 6)
					super.toString();
					^^^^^
				super reference is illegal in interface context
				----------
				4. ERROR in X.java (at line 7)
					Object.super.toString();
					^^^^^^^^^^^^
				No enclosing instance of the type Object is accessible in scope
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424914, [1.8][compiler] No error shown for method reference with super enclosed in an interface
	public void test424914a() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface B {
							default void foo() {
								System.out.println("B.foo");
							}
						}
						interface A extends B {
							default void foo() {
								System.out.println("A.foo");
								B.super.foo();
							}
						}
						public class X implements A {
							public static void main(String[] args) {
								A a = new X();
								a.foo();
							}
						}
						""",
			},
			"A.foo\n" +
			"B.foo");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427478, [1.8][compiler] Wrong "Duplicate default methods" error on AbstractDoubleSpliterator
	public void test427478() throws Exception { // extracted smaller test case.
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						import java.util.function.Consumer;
						import java.util.function.DoubleConsumer;
						public interface X<T> {
						    default void forEachRemaining(Consumer<? super T> action) {
						    }
						    public interface OfPrimitive<T, T_CONS, T_SPLITR extends OfPrimitive<T, T_CONS, T_SPLITR>> extends X<T> {
						        default void forEachRemaining(T_CONS action) {
						        }
						    }
						    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {
						        default void forEachRemaining(Consumer<? super Double> action) {
						        }
						        default void forEachRemaining(DoubleConsumer action) {
						        }
						    }
						}
						abstract class AbstractDoubleSpliterator implements X.OfDouble {
						}
						""",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427478, [1.8][compiler] Wrong "Duplicate default methods" error on AbstractDoubleSpliterator
	public void test427478a() throws Exception { // full test case.
		this.runConformTest(
			new String[] {
					"Spliterator.java",
					"""
						import java.util.function.Consumer;
						import java.util.function.DoubleConsumer;
						public interface Spliterator<T> {
						    default void forEachRemaining(Consumer<? super T> action) {
						    }
						    public interface OfPrimitive<T, T_CONS, T_SPLITR extends OfPrimitive<T, T_CONS, T_SPLITR>>
						            extends Spliterator<T> {
						        // overloads Spliterator#forEachRemaining(Consumer<? super T>)
						        default void forEachRemaining(T_CONS action) {
						        }
						    }
						    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {
						        @Override // the method from Spliterator
						        default void forEachRemaining(Consumer<? super Double> action) {
						        }
						       \s
						        @Override // the method from OfPrimitive
						        default void forEachRemaining(DoubleConsumer action) {
						        }
						    }
						}
						class Spliterators {
						    /* Error on class: Duplicate default methods named forEachRemaining with
						     * the parameters (Consumer<? super Double>) and (Consumer<? super T>) are
						     * inherited from the types Spliterator.OfDouble and Spliterator<Double>
						     */
						    public abstract static class AbstractDoubleSpliterator implements Spliterator.OfDouble {
						        /* Implementation that prevents the compile error: */
						//        @Override // the method from Spliterator
						//        public void forEachRemaining(Consumer<? super Double> action) {
						//        }
						    }
						}
						""",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423467, [1.8][compiler] wrong error for functional interface with @Override default method
	public void test423467() throws Exception { // full test case.
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						    int foo(String s);
						}
						@FunctionalInterface
						interface A extends I { // wrong compile error (A *is* a functional interface)
						    @Override
						    default int foo(String s) {
						        return -1;
						    }
						    Integer foo(java.io.Serializable s);
						}
						public class X {
						    A a = (s) -> 10;
						}
						@FunctionalInterface
						interface B { // OK
						    default int foo(String s) {
						        return -1;
						    }
						    Integer foo(java.io.Serializable s);
						}
						"""
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438471, Java 1.8 functional interface rejected if it extends an interface which overrides another interface's method
	public void test438471() throws Exception {
		this.runConformTest(
			new String[] {
				"Bar.java",
				"""
					@FunctionalInterface
					public interface Bar extends Overridden {
						void foo();
						@Override
						default void close() {
							System.out.println("bar");
						}
					}
					
					interface Overridden extends AutoCloseable {
						// Works without this overridden method
						@Override
						void close();
					}"""
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436350, [1.8][compiler] Missing bridge method in interface results in AbstractMethodError
	public void test436350() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					    }
					}
					interface GenericInterface<T> {
						T reduce(Integer i);
					}
					interface DoubleInterface extends GenericInterface<Double> {
						default Double reduce(Integer i) {
							return 0.0;
						}
						double reduce(String s);
					}
					""", // =================
			},
			"");
		// 	ensure bridge methods are generated in interfaces.
		String expectedOutput =
				"""
			  public bridge synthetic java.lang.Object reduce(java.lang.Integer arg0);
			    0  aload_0 [this]
			    1  aload_1 [arg0]
			    2  invokeinterface DoubleInterface.reduce(java.lang.Integer) : java.lang.Double [24] [nargs: 2]
			    7  areturn
			""";

		File f = new File(OUTPUT_DIR + File.separator + "DoubleInterface.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436350, [1.8][compiler] Missing bridge method in interface results in AbstractMethodError
	public void test436350a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Iterator;
					import java.util.PrimitiveIterator;
					import java.util.PrimitiveIterator.OfDouble;
					/**
					 * @author Tobias Grasl
					 */
					public class X {
						public static void main(String[] args) {
							final double[] doubles = new double[]{1,2,3};
							OfDouble doubleIterator = new DoubleArrayIterator(doubles);
							Double value = new Reducer<Double>().reduce(doubleIterator, new DoubleInterface() {
								@Override
								public double reduce(OfDouble iterator_) {
									double sum = 0;
									while(iterator_.hasNext()) {
										sum += iterator_.nextDouble();
									}
									return sum;
								}
							});
							System.out.println("Anonymous class value: "+value);
							doubleIterator = new DoubleArrayIterator(doubles);
							value = new Reducer<Double>().reduce(doubleIterator, (DoubleInterface) iterator_ -> {
								double sum = 0;
								while(iterator_.hasNext()) {
									sum += iterator_.nextDouble();
								}
								return sum;
							});
							System.out.println("Lambda expression value: "+value);
						}
						private static class DoubleArrayIterator implements PrimitiveIterator.OfDouble {
							int index = 0;
							private double[] _doubles;
							public DoubleArrayIterator(double[] doubles_) {
								_doubles = doubles_;
							}
							@Override
							public boolean hasNext() {
								return index < _doubles.length;
							}
							@Override
							public double nextDouble() {
								return _doubles[index++];
							}
						};
						interface GenericInterface<T> {
							T reduce(Iterator<T> iterator_);
						}
						interface DoubleInterface extends GenericInterface<Double> {
							default Double reduce(Iterator<Double> iterator_) {
								if(iterator_ instanceof PrimitiveIterator.OfDouble) {
									return reduce((PrimitiveIterator.OfDouble)iterator_);
								}
								return Double.NaN;
							};
							double reduce(PrimitiveIterator.OfDouble iterator_);
						}
						static class Reducer<T> {
							T reduce(Iterator<T> iterator_, GenericInterface<T> reduction_) {
								return reduction_.reduce(iterator_);
							}
						}
					}
					""", // =================
			},
			"Anonymous class value: 6.0\n" +
			"Lambda expression value: 6.0");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437522, [1.8][compiler] Missing compile error in Java 8 mode for Interface.super.field access
	public void testBug437522() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface T {
					    int f = 0;
					    void foo();
					    default String def() { return "T.def"; }
					}
					class S {
					    public static final int f = 0;
					}
					class C extends S implements T {
					    @Override
					    public void foo() {
					        System.out.println(T.super.f); // no error in Java 8 (wrong) without fix
					        System.out.println(T.super.def()); // new JLS8 15.12.1 form (OK)
					        System.out.println(S.super.f); // compile error (correct)
					    }
					}
					class X {
					    T f = new T() {
					        @Override
					        public void foo() {
					            System.out.println(T.super.f); // no error in Java 8 (wrong) without fix
					        }
					    };
					}
					class Y { int f = 1;}
					class Z extends Y {
						int foo2() { return super.f;}
						static int foo() { return super.f;}
					}
					interface T2 { int f = 0; }
					class X2  implements T2 {\t
						int i = T2.super.f;
					}
					interface T3 { int f = 0; }
					interface T4 extends T3 { int f = 0; }
					class X3  implements T4 {\t
						int i = T4.super.f;
					}
					interface T5 { int f = 0; }
					class X5 implements T5 {\t
						static int i = T5.super.f;
					}
					interface T6 { int f = 0; }
					class X6 implements T6 {\t
						static int i = T6.super.f;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					System.out.println(T.super.f); // no error in Java 8 (wrong) without fix
					                   ^^^^^^^
				No enclosing instance of the type T is accessible in scope
				----------
				2. ERROR in X.java (at line 14)
					System.out.println(S.super.f); // compile error (correct)
					                   ^^^^^^^
				No enclosing instance of the type S is accessible in scope
				----------
				3. ERROR in X.java (at line 21)
					System.out.println(T.super.f); // no error in Java 8 (wrong) without fix
					                   ^^^^^^^
				No enclosing instance of the type T is accessible in scope
				----------
				4. ERROR in X.java (at line 28)
					static int foo() { return super.f;}
					                          ^^^^^
				Cannot use super in a static context
				----------
				5. ERROR in X.java (at line 32)
					int i = T2.super.f;
					        ^^^^^^^^
				No enclosing instance of the type T2 is accessible in scope
				----------
				6. ERROR in X.java (at line 37)
					int i = T4.super.f;
					        ^^^^^^^^
				No enclosing instance of the type T4 is accessible in scope
				----------
				7. ERROR in X.java (at line 41)
					static int i = T5.super.f;
					               ^^^^^^^^
				Cannot use super in a static context
				----------
				8. ERROR in X.java (at line 41)
					static int i = T5.super.f;
					               ^^^^^^^^
				No enclosing instance of the type T5 is accessible in scope
				----------
				9. ERROR in X.java (at line 45)
					static int i = T6.super.f;
					               ^^^^^^^^
				Cannot use super in a static context
				----------
				10. ERROR in X.java (at line 45)
					static int i = T6.super.f;
					               ^^^^^^^^
				No enclosing instance of the type T6 is accessible in scope
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437522, [1.8][compiler] Missing compile error in Java 8 mode for Interface.super.field access
	// Example JLS: 15.11.2-1.
	public void testBug437522a() throws Exception {
		runConformTest(
			true,
			new String[] {
				"X.java",
				"""
					interface I  { int x = 0; }
					class T1 implements I { int x = 1; }
					class T2 extends T1   { int x = 2; }
					class T3 extends T2 {
					    int x = 3;
					    void test() {
					        System.out.println("x= "          + x);
					        System.out.println("super.x= "    + super.x);
					        System.out.println("((T2)this).x= " + ((T2)this).x);
					        System.out.println("((T1)this).x= " + ((T1)this).x);
					        System.out.println("((I)this).x= "  + ((I)this).x);
					    }
					}
					public class X {
					    public static void main(String[] args) {
					        new T3().test();
					    }
					}
					""",
			},
			null, null,
			"""
				----------
				1. WARNING in X.java (at line 3)
					class T2 extends T1   { int x = 2; }
					                            ^
				The field T2.x is hiding a field from type T1
				----------
				2. WARNING in X.java (at line 5)
					int x = 3;
					    ^
				The field T3.x is hiding a field from type T2
				----------
				3. WARNING in X.java (at line 11)
					System.out.println("((I)this).x= "  + ((I)this).x);
					                                                ^
				The static field I.x should be accessed in a static way
				----------
				""",
			"""
				x= 3
				super.x= 2
				((T2)this).x= 2
				((T1)this).x= 1
				((I)this).x= 0""",
			"", JavacTestOptions.DEFAULT);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453552, Invalid '@FunctionalInterface error when two interfaces extend the same functional interface.
	public void test453552() throws Exception {
		this.runConformTest(
			new String[] {
				"FunctionalInterface1.java",
				"""
					@FunctionalInterface
					interface FunctionalInterface1 {
					    void methodWithoutDefault();
					}
					@FunctionalInterface
					interface FunctionalInterface2 extends FunctionalInterface1{}
					@FunctionalInterface
					interface FunctionalInterface3 extends FunctionalInterface1{}
					@FunctionalInterface
					interface FunctionalInterface4 extends FunctionalInterface2, FunctionalInterface3{}
					@FunctionalInterface
					interface RunnableFunctionalInterface extends Runnable, FunctionalInterface4{
						default void methodWithoutDefault(){
							// implements methodWithoutDefault
						}
					}
					"""
			});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=453552, Invalid '@FunctionalInterface error when two interfaces extend the same functional interface.
	public void test453552_comment2() throws Exception {
		this.runConformTest(
			new String[] {
				"FI1.java",
				"""
					interface FI1<T,R> {
						R call(T input);
					}
					interface FI2<U,V> {
						V call(U input);
					}
					@FunctionalInterface
					interface FI3<X,Y> extends FI1<X,Y>, FI2<X,Y> {
						Y apply(X input);
					
					   @Override
					   default Y call(X input) {
					       return apply(input);
					   }
					}"""
			});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment0() throws Exception {
		this.runNegativeTest(
			new String[] {
				"D.java",
				"""
					interface A {
					    public default void display() {
					        System.out.println("Display from A");
					    }
					}
					interface B extends A {
						@Override
					    public default void display() {
					        System.out.println("Display from B");
					    }
					}
					interface C extends A {
						@Override
					    public void display();
					}
					public interface D extends B, C {
					}
					"""
			},
			"""
				----------
				1. ERROR in D.java (at line 16)
					public interface D extends B, C {
					                 ^
				The default method display() inherited from B conflicts with another method inherited from C
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment0_a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"D.java",
				"""
					interface A {
					    public default void display() {
					        System.out.println("Display from A");
					    }
					}
					interface B extends A {
						@Override
					    public default void display() {
					        System.out.println("Display from B");
					    }
					}
					interface C extends A {
						@Override
					    public void display();
					}
					public interface D extends C, B {
					}
					"""
			},
			"""
				----------
				1. ERROR in D.java (at line 16)
					public interface D extends C, B {
					                 ^
				The default method display() inherited from B conflicts with another method inherited from C
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment3_a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"""
					import java.util.*;
					interface Z<E> extends X<E>, Y<E> {
					}
					interface Y<E> extends AB<E> {
						@Override
						default Spliterator<E> spliterator() { return null; };
					}
					interface X<E> extends AB<E> {
						@Override
						Spliterator<E> spliterator();
					}
					interface AB<E> {
						default Spliterator<E> spliterator() { return null; }
					}
					"""
			},
			"""
				----------
				1. ERROR in Z.java (at line 2)
					interface Z<E> extends X<E>, Y<E> {
					          ^
				The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
	public void test477891_comment3_b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"""
					import java.util.*;
					interface Z<E> extends Y<E>, X<E> {
					}
					interface Y<E> extends AB<E> {
						@Override
						default Spliterator<E> spliterator() { return null; };
					}
					interface X<E> extends AB<E> {
						@Override
						Spliterator<E> spliterator();
					}
					interface AB<E> {
						default Spliterator<E> spliterator() { return null; }
					}
					"""
			},
			"""
				----------
				1. ERROR in Z.java (at line 2)
					interface Z<E> extends Y<E>, X<E> {
					          ^
				The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
		public void test477891_comment3_c() throws Exception {
			this.runNegativeTest(
				new String[] {
					"Z.java",
					"""
						import java.util.*;
						interface Z<E> extends X<E>, Y<E> {
						}
						interface Y<E> extends AB<E> {
							@Override
							default Spliterator<E> spliterator() { return null; };
						}
						interface X<E> {
							Spliterator<E> spliterator();
						}
						interface AB<E> {
							default Spliterator<E> spliterator() { return null; }
						}
						"""
				},
				"""
					----------
					1. ERROR in Z.java (at line 2)
						interface Z<E> extends X<E>, Y<E> {
						          ^
					The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>
					----------
					""");
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=477891, [1.8] regression caused by the fix for bug 438812: order dependencies in analysis of default method inheritance
		public void test477891_comment3_d() throws Exception {
			this.runNegativeTest(
				new String[] {
					"Z.java",
					"""
						import java.util.*;
						interface Z<E> extends Y<E>, X<E> {
						}
						interface Y<E> extends AB<E> {
							@Override
							default Spliterator<E> spliterator() { return null; };
						}
						interface X<E> {
							Spliterator<E> spliterator();
						}
						interface AB<E> {
							default Spliterator<E> spliterator() { return null; }
						}
						"""
				},
				"""
					----------
					1. ERROR in Z.java (at line 2)
						interface Z<E> extends Y<E>, X<E> {
						          ^
					The default method spliterator() inherited from Y<E> conflicts with another method inherited from X<E>
					----------
					""");
		}
	public void test458547_comment0_a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"JavaTest.java",
				"""
					public class JavaTest {
						interface A {
							default void foo() { }
						}
						interface B {
							void foo();
						}
						interface C {
							void foo();
						}
						interface D extends A, B {
							@Override default void foo() { }
						}
						class E implements A, B, C, D {
						}
					}"""
			},
			"""
				----------
				1. ERROR in JavaTest.java (at line 14)
					class E implements A, B, C, D {
					      ^
				The default method foo() inherited from JavaTest.D conflicts with another method inherited from JavaTest.C
				----------
				""");
	}
	public void test458547_comment0_b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"JavaTest.java",
				"""
					public class JavaTest {
						interface A {
							default void foo() { }
						}
						interface B {
							void foo();
						}
						interface C {
							void foo();
						}
						interface D extends A, B {
							@Override default void foo() { }
						}
						class E implements B, C, A, D {
						}
					}"""
			},
			"""
				----------
				1. ERROR in JavaTest.java (at line 14)
					class E implements B, C, A, D {
					      ^
				The default method foo() inherited from JavaTest.D conflicts with another method inherited from JavaTest.C
				----------
				""");
	}

	public void testBug539743() {
		runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					static <V> void m(V v) {
				
					}
				
					interface Foo {
						@SuppressWarnings("unchecked")
						default <U> U f() {
							return (U) new Object();
						}
					}
				
					static class Bar implements Foo {
						@SuppressWarnings("unchecked")
						@Override
						public Object f() {
							m(Foo.super.f());
							return null;
						}
					}
				}""",
		},
		"");
	}
}
