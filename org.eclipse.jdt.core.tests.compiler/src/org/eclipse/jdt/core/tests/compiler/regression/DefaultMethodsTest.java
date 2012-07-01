/*******************************************************************************
 * Copyright (c) 2012 GK Software AG and others.
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

// See https://bugs.eclipse.org/380501
// Bug 380501 - [1.8][compiler] Add support for default methods (JSR 335)
public class DefaultMethodsTest extends AbstractComparableTest {

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testModifiers1" };
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
//		runConformTest(
		runNegativeTest(
		new String[] {
			"I.java",
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD) @interface Annot{}\n" +
			"public interface I {\n" +
			"    void foo1() default {}\n" +
			"    public synchronized void foo2() default { System.exit(0); }\n" +
			"    strictfp void foo3() default {}\n" +
			"    public strictfp synchronized void foo4() default {}\n" +
			"    public strictfp synchronized @Annot void foo5() default {}\n" +
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
			"    void foo1() default {}\n" +
			"    public synchronized void foo2() default {}\n" +
			"    stritfp void foo3() default {}\n" + // typo in strictfp
			"    public strictfp synchronized void foo4() default {}\n" +
			"    public strictfp synchronized @Annot void foo5() default {}\n" +
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
			"    native void foo3() default {}\n" +
			"    static void foo4() default {}\n" +
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
			"	native void foo3() default {}\n" +
			"	            ^^^^^^\n" +
			"Illegal modifier for the interface method foo3; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 5)\n" +
			"	static void foo4() default {}\n" +
			"	            ^^^^^^\n" +
			"Illegal modifier for the interface method foo4; only public, abstract, strictfp & synchronized are permitted\n" +
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
			"    public abstract void foo2() default {}\n" +
			"    abstract void foo3() default {}\n" +
			"    void foo4() { }\n" + // implicit "abstract" without "default" doesn't allow a body, either
			"    abstract static void foo5() default {}\n" + // double fault
			"}\n"},
			"----------\n" +
			"1. ERROR in I.java (at line 4)\n" +
			"	public abstract void foo2() default {}\n" +
			"	                     ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 5)\n" +
			"	abstract void foo3() default {}\n" +
			"	              ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 6)\n" +
			"	void foo4() { }\n" +
			"	     ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 7)\n" +
			"	abstract static void foo5() default {}\n" +
			"	                     ^^^^^^\n" +
			"Illegal modifier for the interface method foo5; only public, abstract, strictfp & synchronized are permitted\n" +
			"----------\n" +
			"5. ERROR in I.java (at line 7)\n" +
			"	abstract static void foo5() default {}\n" +
			"	                     ^^^^^^\n" +
			"Abstract methods do not specify a body\n" +
			"----------\n");
	}

	// class implements interface with default method. 
	// - no need to implement this interface method as it is not abstract
	public void testModifiers5() {
// Inject an unrelated compile error to prevent class file verification. TODO revert
//		runConformTest(
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    void foo() default {}\n" +
				"}\n",
				"C.java",
				"public class C implements I {}\n" +
// TODO remove me:
				"public class Wrong{}\n"
			},
			"----------\n" +
			"1. ERROR in C.java (at line 2)\n" +
			"	public class Wrong{}\n" +
			"	             ^^^^^\n" +
			"The public type Wrong must be defined in its own file\n" +
			"----------\n");
	}
	
	// class implements interface with default method. 
	// - no need to implement this interface method as it is not abstract, but other abstract method exists
	public void testModifiers6() {
		runNegativeTest(
			new String[] {
				"I.java",
				"public interface I {\n" +
				"    void foo() default {}\n" +
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
}
