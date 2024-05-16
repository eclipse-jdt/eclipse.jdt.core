/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *								bug 282152 - [1.5][compiler] Generics code rejected by Eclipse but accepted by javac
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 401456 - Code compiles from javac/intellij, but fails from eclipse
 *								bug 405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
 *								Bug 408441 - Type mismatch using Arrays.asList with 3 or more implementations of an interface with the interface type as the last parameter
 *								Bug 413958 - Function override returning inherited Generic Type
 *								Bug 415734 - Eclipse gives compilation error calling method with an inferred generic return type
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 423496 - [1.8] Implement new incorporation rule once it becomes available
 *								Bug 426590 - [1.8][compiler] Compiler error with tenary operator
 *								Bug 427216 - [Java8] array to varargs regression
 *								Bug 425031 - [1.8] nondeterministic inference for GenericsRegressionTest.test283353
 *								Bug 430686 - [1.8][compiler] Generics: erroneously reports 'method not applicable for the arguments'
 *								Bug 430759 - [1.8][compiler] SourceTypeBinding cannot be cast to ParameterizedTypeBinding
 *								Bug 431408 - Java 8 (1.8) generics bug
 *								Bug 432603 - [compile][1.7] ecj reports an Error while javac doesn't
 *								Bug 399527 - Type inference problem
 *								Bug 434570 - Generic type mismatch for parametrized class annotation attribute with inner class
 *								Bug 434044 - Java 8 generics thinks single method is ambiguous
 *								Bug 434793 - [1.8][null][compiler] AIOOBE in ParameterizedGenericMethodBinding.substitute when inlining a method
 *								Bug 438337 - StackOverflow after update from Kepler to Luna
 *								Bug 452194 - Code no longer compiles in 4.4.1, but with confusing error
 *								Bug 456459 - Discrepancy between Eclipse compiler and javac - Enums, interfaces, and generics
 *								Bug 456924 - StackOverflowError during compilation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsRegressionTest extends AbstractComparableTest {

	public GenericsRegressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testBug456459" };
//		TESTS_NUMBERS = new int[] { 1465 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return GenericsRegressionTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		compilerOptions.put(CompilerOptions.OPTION_ReportUnusedTypeParameter,CompilerOptions.IGNORE);
		return compilerOptions;
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					public class X implements I {
					    <T extends I> void main(Class<T> clazz) {
					        boolean b =\s
					            clazz == clazz ||\s
					            X.class == X.class ||\s
					            I.class == I.class ||\s
					            clazz == X.class ||\s
					            X.class == clazz ||\s
					            clazz == I.class ||\s
					            I.class == clazz ||\s
					            I.class == X.class ||
					            X.class == I.class;
					    }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					clazz == clazz ||\s
					^^^^^^^^^^^^^^
				Comparing identical expressions
				----------
				2. ERROR in X.java (at line 12)
					I.class == X.class ||
					^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<I> and Class<X>
				----------
				3. ERROR in X.java (at line 13)
					X.class == I.class;
					^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<X> and Class<I>
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					public class X {
					    <T extends I> void main(Class<T> clazz, X x) {
					        boolean b =\s
					            x.getClass() == clazz ||\s
					            clazz == x.getClass();\s
					    }
					}
					"""
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					public final class X {
					    <T extends I> void main(Class<T> clazz, X x) {
					        boolean b =\s
					            x.getClass() == clazz ||\s
					            clazz == x.getClass();\s
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					x.getClass() == clazz ||\s
					^^^^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<capture#1-of ? extends X> and Class<T>
				----------
				2. ERROR in X.java (at line 6)
					clazz == x.getClass();\s
					^^^^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<T> and Class<capture#2-of ? extends X>
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531e() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					public final class X implements I {
					    <T extends I> void main(Class<T> clazz, X x) {
					        boolean b =\s
					            x.getClass() == clazz ||\s
					            clazz == x.getClass();\s
					    }
					}
					"""
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class I {}
					public class X {
					    <T extends I> void main(Class<T> clazz, X x) {
					        boolean b =\s
					            x.getClass() == clazz ||\s
					            clazz == x.getClass();\s
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					x.getClass() == clazz ||\s
					^^^^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<capture#1-of ? extends X> and Class<T>
				----------
				2. ERROR in X.java (at line 6)
					clazz == x.getClass();\s
					^^^^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<T> and Class<capture#2-of ? extends X>
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322531
	public void test322531i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {};
					public class X {
					    public X() {
					    }
					    public <T extends I> void test(Class<T> clazz) {
					        Class<I> ci = I.class;
					        Class<X> ti = X.class;
					        boolean b = ci == X.class ||
					        	        X.class == ci ||
					        			I.class == X.class ||
					        			X.class == I.class ||
					        			ti == I.class ||
					        			I.class == ti ||
					        			ti == ci ||
					        			ci == ti;
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					boolean b = ci == X.class ||
					            ^^^^^^^^^^^^^
				Incompatible operand types Class<I> and Class<X>
				----------
				2. ERROR in X.java (at line 9)
					X.class == ci ||
					^^^^^^^^^^^^^
				Incompatible operand types Class<X> and Class<I>
				----------
				3. ERROR in X.java (at line 10)
					I.class == X.class ||
					^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<I> and Class<X>
				----------
				4. ERROR in X.java (at line 11)
					X.class == I.class ||
					^^^^^^^^^^^^^^^^^^
				Incompatible operand types Class<X> and Class<I>
				----------
				5. ERROR in X.java (at line 12)
					ti == I.class ||
					^^^^^^^^^^^^^
				Incompatible operand types Class<X> and Class<I>
				----------
				6. ERROR in X.java (at line 13)
					I.class == ti ||
					^^^^^^^^^^^^^
				Incompatible operand types Class<I> and Class<X>
				----------
				7. ERROR in X.java (at line 14)
					ti == ci ||
					^^^^^^^^
				Incompatible operand types Class<X> and Class<I>
				----------
				8. ERROR in X.java (at line 15)
					ci == ti;
					^^^^^^^^
				Incompatible operand types Class<I> and Class<X>
				----------
				""");
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
public void test282152() {
    this.runConformTest(
        new String[] {
            "Test.java",
            """
				public interface Test<T extends Number> {
				    public <U> void test(Test<? super U> t, U value);
				    public void setValue(T v);\
				}""",
            "Impl.java",
            """
				public class Impl<T extends Number> implements Test<T>{
				    T val;\
				    public <U> void test(Test<? super U> t, U value) {
				        t.setValue(value);
				    }
				    public void setValue(T v) {
				        this.val = v;
				    }
				}""",
            "Client.java",
            """
				public class Client {
				    void test() {
				        Impl<Integer> t1 = new Impl<Integer>();
				        Double n = Double.valueOf(3.14);
				        t1.test(new Impl<Number>(), n);
				    }
				}
				"""
        },
        ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// violating lower bound
public void test282152b() {
    this.runNegativeTest(
        new String[] {
            "Test.java",
            """
				public interface Test<T extends Number> {
				    public <U> void test(Test<? super U> t, U value);
				    public void setValue(T v);\
				}""",
            "Impl.java",
            """
				public class Impl<T extends Number> implements Test<T>{
				    T val;\
				    public <U> void test(Test<? super U> t, U value) {
				        t.setValue(value);
				    }
				    public void setValue(T v) {
				        this.val = v;
				    }
				}""",
            "Client.java",
            """
				public class Client {
				    void test() {
				        Impl<Integer> t1 = new Impl<Integer>();
				        Number n = Double.valueOf(3.14);
				        t1.test(new Impl<Double>(), n);
				    }
				}
				"""
        },
        """
			----------
			1. ERROR in Client.java (at line 5)
				t1.test(new Impl<Double>(), n);
				   ^^^^
			The method test(Test<? super U>, U) in the type Impl<Integer> is not applicable for the arguments (Impl<Double>, Number)
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// contradictory bounds
public void test282152c() {
    this.runNegativeTest(
        new String[] {
            "Test.java",
            """
				public interface Test<T extends Number> {
				    public <U extends Exception> void test(Test<? super U> t, U value);
				    public void setValue(T v);\
				}"""
        },
        """
			----------
			1. ERROR in Test.java (at line 2)
				public <U extends Exception> void test(Test<? super U> t, U value);
				                                            ^^^^^^^^^
			Bound mismatch: The type ? super U is not a valid substitute for the bounded parameter <T extends Number> of the type Test<T>
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// compatible constraints
public void test282152d() {
    this.runConformTest(
        new String[] {
            "Test.java",
            """
				public interface Test<T extends Number> {
				    public <U extends Integer> void test(Test<? super U> t, U value);
				    public void setValue(T v);\
				}""",
            "Impl.java",
            """
				public class Impl<T extends Number> implements Test<T>{
				    T val;\
				    public <U extends Integer> void test(Test<? super U> t, U value) {
				        t.setValue(value);
				    }
				    public void setValue(T v) {
				        this.val = v;
				    }
				}""",
            "Client.java",
            """
				public class Client {
				    void test() {
				        Impl<Integer> t1 = new Impl<Integer>();
				        Integer i = Integer.valueOf(3);
				        t1.test(new Impl<Integer>(), i);
				    }
				}
				"""
        },
        ""); // no specific success output string
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282152
// direct use of type variable does not involve capture, thus no merging of constraints happens
public void test282152e() {
	this.runNegativeTest(
	    new String[] {
	        "Test.java",
	        """
				public interface Test<T extends Number> {
				    public <U> void test(Test<U> t, U value);
				    public void setValue(T v);\
				}"""
	    },
	    """
			----------
			1. ERROR in Test.java (at line 2)
				public <U> void test(Test<U> t, U value);
				                          ^
			Bound mismatch: The type U is not a valid substitute for the bounded parameter <T extends Number> of the type Test<T>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330869
public void test330869() {
    this.runConformTest(
            new String[] {
                    "X.java",
                    """
						public class X {
						    public <T> T getAdapter(Class<? extends T> adapterType) {
						        T result = null;
						        if (adapterType == Foo.class) {
						        }
						        else if (adapterType == Bar.class) {
						        }
						        return  result;
						     }
						     public class Foo {
						     }
						     public interface Bar {
						     }
						}
						"""
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
					"""
						interface Adaptable {
						    public Object getAdapter(Class clazz);   \s
						}
						public class X implements Adaptable {
						    public Object getAdapter(Class clazz) {
						        return null;
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					public Object getAdapter(Class clazz);   \s
					                         ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""",
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
					"""
						interface Adaptable {
						    public Object getAdapter(Class clazz);   \s
						}
						public class X implements Adaptable {
						    public Object getAdapter(Class clazz) {
						        return null;
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 2)
					public Object getAdapter(Class clazz);   \s
					                         ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. WARNING in X.java (at line 5)
					public Object getAdapter(Class clazz) {
					                         ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""",
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
					"""
						interface Adaptable {
						    public Object getAdapter(Class<String> clazz);   \s
						}
						public class X implements Adaptable {
						    public Object getAdapter(Class clazz) {
						        return null;
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					public Object getAdapter(Class clazz) {
					                         ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""",
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
					"""
						interface Adaptable {
						    public Object getAdapter(Class<String> clazz);   \s
						}
						public class X implements Adaptable {
						    public Object getAdapter(Class clazz) {
						        return null;
						    }
						}
						class Y extends X {
						    @Override
						    public Object getAdapter(Class clazz) {
						        return null;
						    }
						}
						"""

			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					public Object getAdapter(Class clazz) {
					                         ^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				""",
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
					"""
						import java.util.List;
						class Top {
						    public void set(List arg) { } // OK to warn in 1.5 code
						    public List get() { return null; } // OK to warn in 1.5 code
						}
						class Sub extends Top {
						    @Override
						    public void set(List arg) { // should not warn (overrides)
						    }
						    @Override
						    public List get() { // should not warn (overrides)
						        return super.get();
						    }
						}
						public class X {
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					public void set(List arg) { } // OK to warn in 1.5 code
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 4)
					public List get() { return null; } // OK to warn in 1.5 code
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				""",
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
					"""
						import java.util.List;
						class Top {
						    public void set(List arg) { } // OK to warn in 1.5 code
						    public List<String> get() { return null; }
						}
						class Sub extends Top {
						    @Override
						    public void set(List arg) { // should not warn (overrides)
						    }
						    @Override
						    public List get() { // should warn (super's return type is not raw)
						        return super.get();
						    }
						}
						public class X {
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					public void set(List arg) { } // OK to warn in 1.5 code
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 11)
					public List get() { // should warn (super's return type is not raw)
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 11)
					public List get() { // should warn (super's return type is not raw)
					       ^^^^
				Type safety: The return type List for get() from the type Sub needs unchecked conversion to conform to List<String> from the type Top
				----------
				""",
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
					"""
						import java.util.List;
						public class Top {
						    public void set(List arg) { } // OK to warn in 1.5 code
						    public List get() { return null; } // OK to warn in 1.5 code
						    List list; // OK to warn in 1.5 code
						}
						""",
					"Sub.java",
					"""
						import java.util.List;
						public class Sub extends Top {
						    @Override
						    public void set(List arg) { // should not warn (overrides)
						        super.set(arg);
						        arg.set(0, "A"); // should not warn ('arg' is forced raw)
						    }
						    @Override
						    public List get() { // should not warn (overrides)
						        return super.get();
						    }
						}
						""",
					"X.java",
					"""
						import java.util.List;
						public class X {
						    void run() {
						        new Top().list.add("arg"); // should not warn (uses raw field declared elsewhere)
						        new Top().get().add("arg"); // should not warn (uses raw API)
						        List raw= new Top().get(); // OK to warn ('raw' declared here)
						        raw.add("arg"); // OK to warn ('raw' declared here)
						        // When Top#get() is generified, both of the following will fail
						        // with a compile error if type arguments don't match:
						        List<String> unchecked= new Top().get(); // should not warn (forced)
						        unchecked.add("x");
						        // Should not warn about unchecked cast, but should warn about
						        // unnecessary cast:
						        List<String> cast= (List<String>) new Top().get();
						        cast.add("x");
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in Top.java (at line 3)
					public void set(List arg) { } // OK to warn in 1.5 code
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Top.java (at line 4)
					public List get() { return null; } // OK to warn in 1.5 code
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in Top.java (at line 5)
					List list; // OK to warn in 1.5 code
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				----------
				1. WARNING in X.java (at line 6)
					List raw= new Top().get(); // OK to warn ('raw' declared here)
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 7)
					raw.add("arg"); // OK to warn ('raw' declared here)
					^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 14)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from List to List<String>
				----------
				""",
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
					"""
						import java.util.List;
						public class Top {
						    public void set(List arg) { }
						    public List get() { return null; }
						    List list;
						}
						""",
					"Sub.java",
					"""
						import java.util.List;
						public class Sub extends Top {
						    @Override
						    public void set(List arg) {
						        super.set(arg);
						        arg.set(0, "A");
						    }
						    @Override
						    public List get() {
						        return super.get();
						    }
						}
						""",
					"X.java",
					"""
						import java.util.List;
						public class X {
						    void run() {
						        new Top().list.add("arg");
						        new Top().get().add("arg");
						        List raw= new Top().get();
						        raw.add("arg");
						        List<String> unchecked= new Top().get();
						        unchecked.add("x");
						        List<String> cast= (List<String>) new Top().get();
						        cast.add("x");
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in Top.java (at line 3)
					public void set(List arg) { }
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Top.java (at line 4)
					public List get() { return null; }
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in Top.java (at line 5)
					List list;
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				----------
				1. WARNING in Sub.java (at line 4)
					public void set(List arg) {
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Sub.java (at line 6)
					arg.set(0, "A");
					^^^^^^^^^^^^^^^
				Type safety: The method set(int, Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				3. WARNING in Sub.java (at line 9)
					public List get() {
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				----------
				1. WARNING in X.java (at line 4)
					new Top().list.add("arg");
					^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 5)
					new Top().get().add("arg");
					^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 6)
					List raw= new Top().get();
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				4. WARNING in X.java (at line 7)
					raw.add("arg");
					^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				5. WARNING in X.java (at line 8)
					List<String> unchecked= new Top().get();
					                        ^^^^^^^^^^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				6. WARNING in X.java (at line 10)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from List to List<String>
				----------
				7. WARNING in X.java (at line 10)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from List to List<String>
				----------
				""",
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
					"""
						import java.util.List;
						public class Top {
						    public void set(List arg) { }
						    public List get() { return null; }
						    List list;
						}
						""",
					"Sub.java",
					"""
						import java.util.List;
						public class Sub extends Top {
						    @Override
						    public void set(List arg) {
						        super.set(arg);
						        arg.set(0, "A");
						    }
						    @Override
						    public List get() {
						        return super.get();
						    }
						}
						""",
					"X.java",
					"""
						import java.util.List;
						public class X {
						    void run() {
						        new Top().list.add("arg");
						        new Top().get().add("arg");
						        List raw= new Top().get();
						        raw.add("arg");
						        List<String> unchecked= new Top().get();
						        unchecked.add("x");
						        List<String> cast= (List<String>) new Top().get();
						        cast.add("x");
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in Top.java (at line 3)
					public void set(List arg) { }
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Top.java (at line 4)
					public List get() { return null; }
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in Top.java (at line 5)
					List list;
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				----------
				1. WARNING in Sub.java (at line 4)
					public void set(List arg) {
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Sub.java (at line 6)
					arg.set(0, "A");
					^^^^^^^^^^^^^^^
				Type safety: The method set(int, Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				3. WARNING in Sub.java (at line 9)
					public List get() {
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				----------
				1. WARNING in X.java (at line 4)
					new Top().list.add("arg");
					^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 5)
					new Top().get().add("arg");
					^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 6)
					List raw= new Top().get();
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				4. WARNING in X.java (at line 7)
					raw.add("arg");
					^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				5. WARNING in X.java (at line 8)
					List<String> unchecked= new Top().get();
					                        ^^^^^^^^^^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				6. WARNING in X.java (at line 10)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from List to List<String>
				----------
				7. WARNING in X.java (at line 10)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from List to List<String>
				----------
				""",
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
					"""
						import java.util.List;
						class Top {
						    public void set(List arg) { } // OK to warn in 1.5 code
						    public List get() { return null; } // OK to warn in 1.5 code
						}
						class Sub extends Top {
						    @Override
						    public void set(List arg) { // should not warn (overrides)
						        super.set(arg);
						        arg.set(0, "A"); // should not warn ('arg' is forced raw)
						    }
						    @Override
						    public List get() { // should not warn (overrides)
						        return super.get();
						    }
						}
						public class X {
						    void run() {
						        new Top().get().add("arg");
						        List raw= new Top().get(); // OK to warn ('raw' declared here)
						        raw.add("arg"); // OK to warn ('raw' declared here)
						        List<String> unchecked= new Top().get();
						        unchecked.add("x");
						        List<String> cast= (List<String>) new Top().get();
						        cast.add("x");
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					public void set(List arg) { } // OK to warn in 1.5 code
					                ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in X.java (at line 4)
					public List get() { return null; } // OK to warn in 1.5 code
					       ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				3. WARNING in X.java (at line 19)
					new Top().get().add("arg");
					^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				4. WARNING in X.java (at line 20)
					List raw= new Top().get(); // OK to warn ('raw' declared here)
					^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				5. WARNING in X.java (at line 21)
					raw.add("arg"); // OK to warn ('raw' declared here)
					^^^^^^^^^^^^^^
				Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
				----------
				6. WARNING in X.java (at line 22)
					List<String> unchecked= new Top().get();
					                        ^^^^^^^^^^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				7. WARNING in X.java (at line 24)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from List to List<String>
				----------
				8. WARNING in X.java (at line 24)
					List<String> cast= (List<String>) new Top().get();
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unnecessary cast from List to List<String>
				----------
				""",
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
					"""
						import java.util.Arrays;
						import java.util.Set;
						import java.util.HashSet;
						public class X {
						    public void foo(String[] elements) {
							     Set set= new HashSet(Arrays.asList(elements));
						    }
						}
						"""
			},
			"""
				----------
				1. WARNING in X.java (at line 6)
					Set set= new HashSet(Arrays.asList(elements));
					^^^
				Set is a raw type. References to generic type Set<E> should be parameterized
				----------
				2. WARNING in X.java (at line 6)
					Set set= new HashSet(Arrays.asList(elements));
					         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: The constructor HashSet(Collection) belongs to the raw type HashSet. References to generic type HashSet<E> should be parameterized
				----------
				3. WARNING in X.java (at line 6)
					Set set= new HashSet(Arrays.asList(elements));
					             ^^^^^^^
				HashSet is a raw type. References to generic type HashSet<E> should be parameterized
				----------
				""",
			null,
			true,
			customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=338350 (unchecked cast - only unavoidable on raw expression)
public void test338350() {
	String[] testFiles = new String[] {
			"Try.java",
			"""
				import java.lang.reflect.Array;
				import java.util.ArrayList;
				import java.util.List;
				public class Try<E> {
					void fooObj() {
						takeObj((E) Bar.getObject());
						takeObj((E) Bar.getArray());
						takeObj((E) Array.newInstance(Integer.class, 2));
					}
					void takeObj(E obj) { }
					void fooArray() {
						takeArray((E[]) Bar.getArray());
						takeArray((E[]) Array.newInstance(Integer.class, 2));
					}
					void takeArray(E[] array) { }
					<L> void foo(List<L> list) {
						list.toArray((L[]) Bar.getArray());
						list.toArray((L[]) Array.newInstance(Integer.class, 2));
					}
					void bar() {
						List<String> l = (List<String>) Bar.getObject();
						List<String> l2 = Bar.getRawList();
						ArrayList<String> l3 = (ArrayList<String>) Bar.getRawList();
					}
				}
				""",
			"Bar.java",
			"""
				import java.lang.reflect.Array;
				import java.util.ArrayList;
				import java.util.List;
				public class Bar {
					public static Object getObject() {
						return new Object();
					}
					public static Object[] getArray() {
						return (Object[]) Array.newInstance(Integer.class, 2);
					}
					public static List getRawList() {
						return new ArrayList();
					}
				}
				"""
	};
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
			testFiles,
			"""
				----------
				1. WARNING in Try.java (at line 6)
					takeObj((E) Bar.getObject());
					        ^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to E
				----------
				2. WARNING in Try.java (at line 7)
					takeObj((E) Bar.getArray());
					        ^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object[] to E
				----------
				3. WARNING in Try.java (at line 8)
					takeObj((E) Array.newInstance(Integer.class, 2));
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to E
				----------
				4. WARNING in Try.java (at line 12)
					takeArray((E[]) Bar.getArray());
					          ^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object[] to E[]
				----------
				5. WARNING in Try.java (at line 13)
					takeArray((E[]) Array.newInstance(Integer.class, 2));
					          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to E[]
				----------
				6. WARNING in Try.java (at line 17)
					list.toArray((L[]) Bar.getArray());
					             ^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object[] to L[]
				----------
				7. WARNING in Try.java (at line 18)
					list.toArray((L[]) Array.newInstance(Integer.class, 2));
					             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to L[]
				----------
				8. WARNING in Try.java (at line 21)
					List<String> l = (List<String>) Bar.getObject();
					                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to List<String>
				----------
				9. WARNING in Try.java (at line 22)
					List<String> l2 = Bar.getRawList();
					                  ^^^^^^^^^^^^^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				10. WARNING in Try.java (at line 23)
					ArrayList<String> l3 = (ArrayList<String>) Bar.getRawList();
					                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from List to ArrayList<String>
				----------
				----------
				1. WARNING in Bar.java (at line 11)
					public static List getRawList() {
					              ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Bar.java (at line 12)
					return new ArrayList();
					           ^^^^^^^^^
				ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
				----------
				""",
			null,
			true,
			customOptions);
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
			testFiles,
			"""
				----------
				1. WARNING in Try.java (at line 6)
					takeObj((E) Bar.getObject());
					        ^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to E
				----------
				2. WARNING in Try.java (at line 7)
					takeObj((E) Bar.getArray());
					        ^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object[] to E
				----------
				3. WARNING in Try.java (at line 8)
					takeObj((E) Array.newInstance(Integer.class, 2));
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to E
				----------
				4. WARNING in Try.java (at line 12)
					takeArray((E[]) Bar.getArray());
					          ^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object[] to E[]
				----------
				5. WARNING in Try.java (at line 13)
					takeArray((E[]) Array.newInstance(Integer.class, 2));
					          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to E[]
				----------
				6. WARNING in Try.java (at line 17)
					list.toArray((L[]) Bar.getArray());
					             ^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object[] to L[]
				----------
				7. WARNING in Try.java (at line 18)
					list.toArray((L[]) Array.newInstance(Integer.class, 2));
					             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to L[]
				----------
				8. WARNING in Try.java (at line 21)
					List<String> l = (List<String>) Bar.getObject();
					                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from Object to List<String>
				----------
				----------
				1. WARNING in Bar.java (at line 11)
					public static List getRawList() {
					              ^^^^
				List is a raw type. References to generic type List<E> should be parameterized
				----------
				2. WARNING in Bar.java (at line 12)
					return new ArrayList();
					           ^^^^^^^^^
				ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
				----------
				""",
			null,
			true,
			customOptions);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (private access - different packages)
public void test334622a() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
						    private Object foo;
						}
						""",
					"q/Y.java",
					"""
						package q;
						import p.X;
						public class Y {
						    public <T extends X> void test(T t) {
						        System.out.println(t.foo);
						    }
						    Zork z;
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\X.java (at line 3)
					private Object foo;
					               ^^^
				The value of the field X.foo is not used
				----------
				----------
				1. ERROR in q\\Y.java (at line 5)
					System.out.println(t.foo);
					                     ^^^
				The field X.foo is not visible
				----------
				2. ERROR in q\\Y.java (at line 7)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (private access - same package)
public void test334622b() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
						    private Object foo;
						}
						""",
					"p/Y.java",
					"""
						package p;
						public class Y {
						    public <T extends X> void test(T t) {
						        System.out.println(t.foo);
						    }
						    Zork z;
						}
						"""
			},
			"""
				----------
				1. WARNING in p\\X.java (at line 3)
					private Object foo;
					               ^^^
				The value of the field X.foo is not used
				----------
				----------
				1. ERROR in p\\Y.java (at line 4)
					System.out.println(t.foo);
					                     ^^^
				The field X.foo is not visible
				----------
				2. ERROR in p\\Y.java (at line 6)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (member of type variable shouldn't contain private members of class constituting intersection type)
public void test334622c() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    private Object foo;
						    public <T extends X> void test(T t) {
						        System.out.println(t.foo);
						        Zork z;
						    }
						}
						"""
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""" :

			"""
				----------
				1. WARNING in X.java (at line 2)
					private Object foo;
					               ^^^
				The value of the field X.foo is not used
				----------
				2. ERROR in X.java (at line 4)
					System.out.println(t.foo);
					                     ^^^
				The field X.foo is not visible
				----------
				3. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622 (member of type variable shouldn't contain private members of class constituting intersection type)
public void test334622d() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    private Object foo() { return null; }
						    public <T extends X> void test(T t) {
						        t.foo();
						        Zork z;
						    }
						}
						"""
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""" :

			"""
				----------
				1. ERROR in X.java (at line 4)
					t.foo();
					  ^^^
				The method foo() from the type X is not visible
				----------
				2. ERROR in X.java (at line 5)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335751 ([1.7][compiler] Cycle inheritance in type arguments is not detected)
public void test335751() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X<A extends B, B extends A> {}\n"
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<A extends B, B extends A> {}
					               ^
				Illegal forward reference to type parameter B
				----------
				""" :

			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<A extends B, B extends A> {}
					                                      ^
				Cycle detected: a cycle exists in the type hierarchy between B and A
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=334121 ([1.7][compiler] Stackoverflow error if compiled in 1.7 compliance mode)
public void test334121() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X<A extends A> {}\n"
			},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<A extends A> {}
					               ^
				Illegal forward reference to type parameter A
				----------
				""" :

			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X<A extends A> {}
					                         ^
				Cycle detected: the type A cannot extend/implement itself or one of its own member types
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337751
public void test337751() {
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"Project.java",
			"""
				import java.util.Map;
				public class Project {
				    public Map getOptions(boolean b) {
				        return null;
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions14,
		null);

	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				import java.util.Map;
				public class Y {
				    void foo(Project project) {
				        Map<String, String> options=
				                        project != null ? project.getOptions(true) : null;
				        options = project.getOptions(true);
				        options = project == null ? null : project.getOptions(true);
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in Y.java (at line 5)
				project != null ? project.getOptions(true) : null;
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>
			----------
			2. WARNING in Y.java (at line 6)
				options = project.getOptions(true);
				          ^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>
			----------
			3. WARNING in Y.java (at line 7)
				options = project == null ? null : project.getOptions(true);
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The expression of type Map needs unchecked conversion to conform to Map<String,String>
			----------
			""",
		null,
		false,
		compilerOptions15,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=337751
public void test337751a() {
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"Project.java",
			"""
				import java.util.Map;
				public class Project {
				    public Map getOptions(boolean b) {
				        return null;
				    }
				}
				"""
		},
		"",
		null,
		true,
		null,
		compilerOptions14,
		null);

	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				import java.util.Map;
				public class Y {
				    void foo(Project project) {
				        Map<String, String> options=
				                        project != null ? project.getOptions(true) : null;
				        options = project.getOptions(true);
				        options = project == null ? null : project.getOptions(true);
				    }
				}
				"""
		},
		"",
		null,
		false,
		compilerOptions15,
		null,
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337962
public void test337962() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				class Super {
				    protected List fList;
				}
				public class X extends Super {
				    protected List fSubList; // raw type warning (good)
				    {
				        fSubList = new ArrayList();
				 \
				        fList.add(null); // type safety warning (TODO: bad, should be hidden)
				        super.fList.add(null); // type safety warning (TODO: bad, should be hidden)
				        fSubList.add(null); // type safety warning (good, should not be hidden)
				    }
				    void foo(String s) {
				        fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        fSubList.add(s); // type safety warning (good, should not be hidden)
				    }
				    X(String s) {
				        fSubList = new ArrayList();
				 \
				        fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        fSubList.add(s); // type safety warning (good, should not be hidden)
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				protected List fList;
				          ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			2. WARNING in X.java (at line 7)
				protected List fSubList; // raw type warning (good)
				          ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			3. WARNING in X.java (at line 9)
				fSubList = new ArrayList();
				               ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			4. WARNING in X.java (at line 10)
				fList.add(null); // type safety warning (TODO: bad, should be hidden)
				^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			5. WARNING in X.java (at line 11)
				super.fList.add(null); // type safety warning (TODO: bad, should be hidden)
				^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			6. WARNING in X.java (at line 12)
				fSubList.add(null); // type safety warning (good, should not be hidden)
				^^^^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			7. WARNING in X.java (at line 15)
				fList.add(s); // type safety warning (TODO: bad, should be hidden)
				^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			8. WARNING in X.java (at line 16)
				super.fList.add(s); // type safety warning (TODO: bad, should be hidden)
				^^^^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			9. WARNING in X.java (at line 17)
				fSubList.add(s); // type safety warning (good, should not be hidden)
				^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			10. WARNING in X.java (at line 20)
				fSubList = new ArrayList();
				               ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			11. WARNING in X.java (at line 21)
				fList.add(s); // type safety warning (TODO: bad, should be hidden)
				^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			12. WARNING in X.java (at line 22)
				super.fList.add(s); // type safety warning (TODO: bad, should be hidden)
				^^^^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			13. WARNING in X.java (at line 23)
				fSubList.add(s); // type safety warning (good, should not be hidden)
				^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			""",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337962
public void test337962b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				class Super {
				    protected List fList;
				}
				public class X extends Super {
				    protected List fSubList; // raw type warning (good)
				    {
				        fSubList = new ArrayList();
				 \
				        fList.add(null); // type safety warning (TODO: bad, should be hidden)
				        super.fList.add(null); // type safety warning (TODO: bad, should be hidden)
				        fSubList.add(null); // type safety warning (good, should not be hidden)
				    }
				    void foo(String s) {
				        fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        fSubList.add(s); // type safety warning (good, should not be hidden)
				    }
				    X(String s) {
				        fSubList = new ArrayList();
				 \
				        fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        super.fList.add(s); // type safety warning (TODO: bad, should be hidden)
				        fSubList.add(s); // type safety warning (good, should not be hidden)
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				protected List fList;
				          ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			2. WARNING in X.java (at line 7)
				protected List fSubList; // raw type warning (good)
				          ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			3. WARNING in X.java (at line 9)
				fSubList = new ArrayList();
				               ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			4. WARNING in X.java (at line 12)
				fSubList.add(null); // type safety warning (good, should not be hidden)
				^^^^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			5. WARNING in X.java (at line 17)
				fSubList.add(s); // type safety warning (good, should not be hidden)
				^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			6. WARNING in X.java (at line 20)
				fSubList = new ArrayList();
				               ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			7. WARNING in X.java (at line 23)
				fSubList.add(s); // type safety warning (good, should not be hidden)
				^^^^^^^^^^^^^^^
			Type safety: The method add(Object) belongs to the raw type List. References to generic type List<E> should be parameterized
			----------
			""",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338011
public void test338011() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X extends A {
				    public X(Map m) { // should warn about raw type m
				        super(m);
				        m.put("one", 1); // warns about raw method invocation (good)
				    }
				    public X(Map<String, Integer> m, boolean b) {
				        super(m); // shows that parametrizing the parameter type is no problem\s
				        new A(m);
				        m.put("one", 1);
				    }
				}
				class A {
				    public A (Map m) {
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				public X(Map m) { // should warn about raw type m
				         ^^^
			Map is a raw type. References to generic type Map<K,V> should be parameterized
			----------
			2. WARNING in X.java (at line 5)
				m.put("one", 1); // warns about raw method invocation (good)
				^^^^^^^^^^^^^^^
			Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized
			----------
			3. WARNING in X.java (at line 14)
				public A (Map m) {
				          ^^^
			Map is a raw type. References to generic type Map<K,V> should be parameterized
			----------
			""",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338011
public void test338011b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X extends A {
				    public X(Map m) { // should warn about raw type m
				        super(m);
				        m.put("one", 1); // warns about raw method invocation (good)
				    }
				    public X(Map<String, Integer> m, boolean b) {
				        super(m); // shows that parametrizing the parameter type is no problem\s
				        new A(m);
				        m.put("one", 1);
				    }
				}
				class A {
				    public A (Map m) {
				    }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				public X(Map m) { // should warn about raw type m
				         ^^^
			Map is a raw type. References to generic type Map<K,V> should be parameterized
			----------
			2. WARNING in X.java (at line 5)
				m.put("one", 1); // warns about raw method invocation (good)
				^^^^^^^^^^^^^^^
			Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized
			----------
			3. WARNING in X.java (at line 14)
				public A (Map m) {
				          ^^^
			Map is a raw type. References to generic type Map<K,V> should be parameterized
			----------
			""",
		null,
		false,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339478
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test339478a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<String> x = new X<>();
						x.testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<String> x = new X<>();
				                  ^
			'<>' operator is not allowed for source level below 1.7
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339478
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test339478b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<> x1 = null;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<> x1 = null;
				^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Map;
				public class X implements Map<> {
				    static Map<> foo (Map<> x) {\s
				        return null;
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public class X implements Map<> {
				                          ^^^
			Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>
			----------
			2. ERROR in X.java (at line 3)
				static Map<> foo (Map<> x) {\s
				       ^^^
			Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>
			----------
			3. ERROR in X.java (at line 3)
				static Map<> foo (Map<> x) {\s
				                  ^^^
			Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Map;
				public class X  {
				    static Map<> foo () {\s
				        return null;
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				static Map<> foo () {\s
				       ^^^
			Incorrect number of arguments for type Map<K,V>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478e() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    class Y<K> {
				    }
				    public static void main(String [] args) {
				        X<String>.Y<> [] y = null;\s
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				X<String>.Y<> [] y = null;\s
				^^^^^^^^^^^
			Incorrect number of arguments for type X<String>.Y; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478f() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    class Y<K> {
				    }
				    public static void main(String [] args) {
				        X<String>.Y<>  y = null;\s
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				X<String>.Y<>  y = null;\s
				^^^^^^^^^^^
			Incorrect number of arguments for type X<String>.Y; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478g() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    public void foo(Object x) {
				        if (x instanceof X<>) {   \s
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (x instanceof X<>) {   \s
				                 ^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478h() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    public void foo(Object x) throws X.Y<>.LException {
				    }
				    static class Y<T> {
				    static class LException extends Throwable {}
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(Object x) throws X.Y<>.LException {
				                                 ^^^
			Incorrect number of arguments for type X.Y<T>; it cannot be parameterized with arguments <>
			----------
			2. WARNING in X.java (at line 5)
				static class LException extends Throwable {}
				             ^^^^^^^^^^
			The serializable class LException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
public void test339478i() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T>  {
				    public void foo () {
				        Object o = new X<> [10];
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Object o = new X<> [10];
				               ^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478j() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<>[] x1 = null;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<>[] x1 = null;
				^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478k() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X<>[] x1 = null;
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				X<>[] x1 = null;
				^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478l() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<> x1 = null;
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<> x1 = null;
				^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478m() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					X<> f1 = null;
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				X<> f1 = null;
				^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478n() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public void foo(X<> args) {
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				public void foo(X<> args) {
				                ^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			""");
}
public void test339478o() {
	String log_18 =
			"""
		----------
		1. ERROR in X.java (at line 3)
			new X<>(){
			    ^
		'<>' cannot be used with anonymous classes
		----------
		""";
	String log_9 =
			"""
		----------
		1. ERROR in X.java (at line 4)
			void newMethod(){
			     ^^^^^^^^^^^
		The method newMethod() of type new X<Object>(){} must override or implement a supertype method
		----------
		""";
	String errorMsg = this.complianceLevel < ClassFileConstants.JDK9 ? log_18 : log_9;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						new X<>(){
							void newMethod(){
							}
						}.testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"""
			----------
			1. ERROR in X.java (at line 3)
				new X<>(){
				    ^
			'<>' operator is not allowed for source level below 1.7
			----------
			2. ERROR in X.java (at line 3)
				new X<>(){
				    ^
			'<>' cannot be used with anonymous classes
			----------
			""":
			errorMsg);
}
public void test339478p() {
	String log_18 =
			"""
		----------
		1. WARNING in X.java (at line 3)
			X Test = new X<>(){
			^
		X is a raw type. References to generic type X<T> should be parameterized
		----------
		2. ERROR in X.java (at line 3)
			X Test = new X<>(){
			             ^
		'<>' cannot be used with anonymous classes
		----------
		""";
	String log_9 =
			"""
		----------
		1. WARNING in X.java (at line 3)
			X Test = new X<>(){
			^
		X is a raw type. References to generic type X<T> should be parameterized
		----------
		2. ERROR in X.java (at line 3)
			X Test = new X<>(){
					void newMethod(){
					}
				}.testFunction("SUCCESS");
			         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Type mismatch: cannot convert from void to X
		----------
		3. ERROR in X.java (at line 4)
			void newMethod(){
			     ^^^^^^^^^^^
		The method newMethod() of type new X<Object>(){} must override or implement a supertype method
		----------
		""";
	String errorMsg = this.complianceLevel < ClassFileConstants.JDK9 ? log_18 : log_9;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X Test = new X<>(){
							void newMethod(){
							}
						}.testFunction("SUCCESS");
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"""
			----------
			1. WARNING in X.java (at line 3)
				X Test = new X<>(){
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			2. ERROR in X.java (at line 3)
				X Test = new X<>(){
				             ^
			'<>' operator is not allowed for source level below 1.7
			----------
			3. ERROR in X.java (at line 3)
				X Test = new X<>(){
				             ^
			'<>' cannot be used with anonymous classes
			----------
			""" :
			errorMsg);
}
public void test339478q() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						X Test = new X<>();
					}
				}""",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"""
			----------
			1. ERROR in X.java (at line 3)
				X Test = new X<>();
				             ^
			'<>' operator is not allowed for source level below 1.7
			----------
			2. ERROR in X.java (at line 3)
				X Test = new X<>();
				             ^
			The type X is not generic; it cannot be parameterized with arguments <>
			----------
			""":
		"""
			----------
			1. ERROR in X.java (at line 3)
				X Test = new X<>();
				             ^
			The type X is not generic; it cannot be parameterized with arguments <>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334493
public void test334493() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Super<P> {}
				class Y<C> implements Super<Integer>{}
				interface II extends Super<Double>{}
				class S<A> extends Y<Byte> {}
				interface T<B> extends II{}
				public class X {
				    public static void main(String argv[]) {
				        S<Integer> s = null;
				        T<Integer> t = null;
				        t = (T) s;          //casting to raw type, no error
				        System.out.println(t);
				    }
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"""
			----------
			1. ERROR in X.java (at line 10)
				t = (T) s;          //casting to raw type, no error
				    ^^^^^
			Cannot cast from S<Integer> to T
			----------
			2. WARNING in X.java (at line 10)
				t = (T) s;          //casting to raw type, no error
				    ^^^^^
			Type safety: The expression of type T needs unchecked conversion to conform to T<Integer>
			----------
			""" :
			"""
				----------
				1. WARNING in X.java (at line 10)
					t = (T) s;          //casting to raw type, no error
					    ^^^^^
				Type safety: The expression of type T needs unchecked conversion to conform to T<Integer>
				----------
				2. WARNING in X.java (at line 10)
					t = (T) s;          //casting to raw type, no error
					     ^
				T is a raw type. References to generic type T<B> should be parameterized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class C<T>  {
						public abstract Object foo(T x);
					   public Integer foo(String x){ return 1; }
					}
					public class X extends C<String> {
					    zork z;
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						zork z;
						^^^^
					zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class C<T>  {
						public abstract Integer foo(T x);
					   public Object foo(String x){ return 1; }
					}
					public class X extends C<String> {
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						public class X extends C<String> {
						             ^
					The type X must implement the inherited abstract method C<String>.foo(String) to override C<String>.foo(String)
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class B<T> {
						public abstract Object foo(T x);
					}
					abstract class C<T> extends B<T> {
					    public Integer foo(String x){ return 1; }
					}
					public class X extends C<String> {
					    zork z;
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						zork z;
						^^^^
					zork cannot be resolved to a type
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class B<T> {
						public abstract Integer foo(T x);
					}
					abstract class C<T> extends B<T> {
					    public Object foo(String x){ return 1; }
					}
					public class X extends C<String> {
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						public class X extends C<String> {
						             ^
					The type X must implement the inherited abstract method B<String>.foo(String) to override C<String>.foo(String)
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313
public void test334313e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract class C<T>  {
						public abstract Object foo(T x);
					   public static Integer foo(String x){ return 1; }
					}
					public class X extends C<String> {
					}
					"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						public class X extends C<String> {
						             ^
					The static method foo(String) conflicts with the abstract method in C<String>
					----------
					""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347145
public void test347145() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class A {}
					class B<V> extends A {}\s
					class F<T extends A, Y extends B<T>> {
						static <U extends A , V extends B<U>> F<U,V> g() {
							return null;
						}
					}
					public class X  {
					    F<? extends B, ? extends B<? extends B>> f011 = F.g();
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 9)
					F<? extends B, ? extends B<? extends B>> f011 = F.g();
					            ^
				B is a raw type. References to generic type B<V> should be parameterized
				----------
				2. WARNING in X.java (at line 9)
					F<? extends B, ? extends B<? extends B>> f011 = F.g();
					                                     ^
				B is a raw type. References to generic type B<V> should be parameterized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347426
public void test347426() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    class A<T extends B<?>> {  }
					    class B<T extends A<?>> {
					        D<? extends B<T>> x;
					    }
					    class D<T extends B<?>> {}
					    <E extends B<?>> X(E x, D<B<A<?>>> d) {
					        if (x.x == d) {
					            return;
					        }
					    }
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347426
public void test347426b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					    class A<T extends X<?>> {
					        B<? extends A<T>> x;
					    }
					    class B<T extends A<?>> {}
					    boolean b = ((A<?>)null).x == ((B<A<X<?>>>)null);  \s
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347426
public void test347426c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					    class A<T extends X<? extends String>> {
					        B<? extends A<T>> x;
					    }
					    class B<T extends A<?>> {}
					    boolean b = ((A<? extends X<?>>)null).x == ((B<A<X<? extends String>>>)null);      \s
					}
					"""
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=283353
public void test283353() {
	String source =
			"""
		public class X {
		  public static void main(String[] args) {
		    EntityKey entityKey = null;
		    new EntityCondenser().condense(entityKey); \s
		  }
		  public static class EntityCondenser {
		    <I, E extends EntityType<I, E, K>, K extends EntityKey<I>> void condense(K entityKey) {
		    }
		  }
		  public class EntityKey<I> {}
		  public interface EntityType<
		    I,
		    E extends EntityType<I, E, K>,
		    K extends EntityKey<I>> {
		  }
		}
		""";

	runNegativeTest(
			new String[] { "X.java", source },
			"""
				----------
				1. WARNING in X.java (at line 3)
					EntityKey entityKey = null;
					^^^^^^^^^
				X.EntityKey is a raw type. References to generic type X.EntityKey<I> should be parameterized
				----------
				2. WARNING in X.java (at line 4)
					new EntityCondenser().condense(entityKey); \s
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation condense(X.EntityKey) of the generic method condense(K) of type X.EntityCondenser
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347600
public void test347600() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class A {}
					class B<V> extends A {}\s
					class D extends B<E> {}
					class E extends B<D> {}
					public class X<T, Y extends B<U>, U extends B<Y>> {   \s
					    public static <T1, Y1 extends B<U1>, U1 extends B<Y1>> X<T1, Y1, U1> getX() {
					        return null;
					    }
					    X<B, ? extends D, ? extends E> f = getX();  \s
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 9)
					X<B, ? extends D, ? extends E> f = getX();  \s
					  ^
				B is a raw type. References to generic type B<V> should be parameterized
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=347746
public void test347746() {
	 this.runNegativeTest(
	     new String[] {
	         "X.java",
	         """
				public class X {
				    class A<T extends B<?>> {}
				    class B<T extends A<?>> extends D {}
				    class C<T extends D> {}
				    class D {}
				    class E<T extends C<? extends B<?>>> {}
				    <U extends C<V>, V extends B<W>, W extends A<V>> W foo(E<U> e) {
				        return goo(e);
				    }
				    <P extends C<Q>, Q extends B<R>, R extends A<Q>> R goo(E<P> e) {
				        return null;
				    }
				}
				"""
	     },
	     "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348493
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test348493() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					class X2<Z> {}
					public static void main(String[] args) {
						X<String>.X2<> x = new X<String>().new X2<>();
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				X<String>.X2<> x = new X<String>().new X2<>();
				^^^^^^^^^^^^
			Incorrect number of arguments for type X<String>.X2; it cannot be parameterized with arguments <>
			----------
			2. ERROR in X.java (at line 4)
				X<String>.X2<> x = new X<String>().new X2<>();
				                                       ^^
			'<>' operator is not allowed for source level below 1.7
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348493
// To verify that diamond construct is not allowed in source level 1.6 or below
public void test348493a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					public static void main(String[] args) {
						X<> x = new X<>();
					}
					public void testFunction(T param){
						System.out.println(param);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X<> x = new X<>();
				^
			Incorrect number of arguments for type X<T>; it cannot be parameterized with arguments <>
			----------
			2. ERROR in X.java (at line 3)
				X<> x = new X<>();
				            ^
			'<>' operator is not allowed for source level below 1.7
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366131
public void test366131() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) {
				        System.out.println("SUCCESS");
				    }
				}
				class Range<T extends Comparable<? super T>> {
				    public boolean containsNC(T value) {
				        return false;
				    }
				}
				class NumberRange<T extends Number & Comparable<? super T>> extends Range<T> {
				    public boolean contains(Comparable<?> value) {
				        return castTo((Class) null).containsNC((Comparable) null);
				    }
				    public <N extends Number & Comparable<? super N>> NumberRange<N>
				castTo(Class<N> type) {
				        return null;
				    }
				}
				""",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366131
public void test366131b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String [] args) {
				        Zork z;
				    }
				}
				class Range<T extends Comparable<? super T>> {
				    public boolean containsNC(T value) {
				        return false;
				    }
				}
				class NumberRange<T extends Number & Comparable<? super T>> extends Range<T> {
				    public boolean contains(Comparable<?> value) {
				        return castTo((Class) null).containsNC((Comparable) null);
				    }
				    public <N extends Number & Comparable<? super N>> NumberRange<N>
				castTo(Class<N> type) {
				        return null;
				    }
				}
				""",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	return castTo((Class) null).containsNC((Comparable) null);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation castTo(Class) of the generic method castTo(Class<N>) of type NumberRange<T>\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 13)\n" +
		"	return castTo((Class) null).containsNC((Comparable) null);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method containsNC(Comparable) belongs to the raw type Range. References to generic type Range<T> should be parameterized\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 13)\n" +
		"	return castTo((Class) null).containsNC((Comparable) null);\n" +
		"	              ^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Number&Comparable<? super Number&Comparable<? super N>>>\n"
		:
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<Comparable<? super Comparable<? super N>&Number>&Number>\n"
		) +
		"----------\n" +
		"5. WARNING in X.java (at line 13)\n" +
		"	return castTo((Class) null).containsNC((Comparable) null);\n" +
		"	               ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 13)\n" +
		"	return castTo((Class) null).containsNC((Comparable) null);\n" +
		"	                                        ^^^^^^^^^^\n" +
		"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375394
public void test375394() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				public class X {
				    static <C1,C2 extends Collection<Object>> boolean foo(C1 c, C2 c2) {
				        return foo(c2,c);\s
				    }
				}
				""",
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in X.java (at line 4)
				return foo(c2,c);\s
				       ^^^
			Bound mismatch: The generic method foo(C1, C2) of type X is not applicable for the arguments (C2, C1). The inferred type C1 is not a valid substitute for the bounded parameter <C2 extends Collection<Object>>
			----------
			""" :
			"""
				----------
				1. ERROR in X.java (at line 4)
					return foo(c2,c);\s
					       ^^^
				The method foo(C1, C2) in the type X is not applicable for the arguments (C2, C1)
				----------
				""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385780
public void test385780() {
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportUnusedTypeParameter,
			CompilerOptions.ERROR);
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			"X.java",
			"""
				public class X<T> {
				public <S> X() {
				}
				public void ph(int t) {
				}
				}
				interface doNothingInterface<T> {
				}
				class doNothing {
				public <T> void doNothingMethod() {\
				}
				}
				class noerror {
				public <T> void doNothing(T t) {\
				}\
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X<T> {
				               ^
			Unused type parameter T
			----------
			2. ERROR in X.java (at line 2)
				public <S> X() {
				        ^
			Unused type parameter S
			----------
			3. ERROR in X.java (at line 7)
				interface doNothingInterface<T> {
				                             ^
			Unused type parameter T
			----------
			4. ERROR in X.java (at line 10)
				public <T> void doNothingMethod() {}
				        ^
			Unused type parameter T
			----------
			""",
		null, true, customOptions);
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with intermediate assignment, always worked
public void testBug395002_1() {
	runConformTest(new String[] {
		"Client.java",
		"""
			interface SelfBound<S extends SelfBound<S, T>, T> {
			}
			public class Client {
				<A extends SelfBound<?,A>> void foo3(A arg3) {
					SelfBound<?, A> var3 = arg3;
					SelfBound<? extends SelfBound<?, A>, ?> var4 = var3;
				}
			}
			"""
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with direct assignment to local
public void testBug395002_2() {
	runConformTest(new String[] {
		"Client.java",
		"""
			interface SelfBound<S extends SelfBound<S, T>, T> {
			}
			public class Client {
				<A extends SelfBound<?,A>> void foo2(A arg2) {
					SelfBound<? extends SelfBound<?, A>, ?> var2 = arg2;
				}
			}
			"""
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with direct assignment to field
public void testBug395002_3() {
	runConformTest(new String[] {
		"Client.java",
		"""
			interface SelfBound<S extends SelfBound<S, T>, T> {
			}
			public class Client<A extends SelfBound<?,A>>  {
				SelfBound<? extends SelfBound<?, A>, ?> field2;
				void foo2(A arg2) {
					field2 = arg2;
				}
			}
			"""
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// version with argument passing
public void testBug395002_4() {
	runConformTest(new String[] {
		"Client.java",
		"""
			interface SelfBound<S extends SelfBound<S, T>, T> {
			}
			public class Client<A extends SelfBound<?,A>>  {
				void bar(SelfBound<? extends SelfBound<?, A>, ?> argBar) {};
				void foo2(A arg2) {
					bar(arg2);
				}
			}
			"""
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// original problem with invocation of generic type
public void testBug395002_full() {
	runConformTest(new String[] {
		"Bug.java",
		"""
			interface SelfBound<S extends SelfBound<S, T>, T> {
			}
			class Test<X extends SelfBound<? extends Y, ?>, Y> {
			}
			public class Bug<A extends SelfBound<?, A>> {
				public Bug() {
					new Test<A, SelfBound<?, A>>();
				}
			}
			"""
		});
}

// https://bugs.eclipse.org/395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
// combined version with direct assignment to local + original problem w/ invocation of generic type
public void testBug395002_combined() {
	runConformTest(new String[] {
		"Client.java",
		"""
			interface SelfBound<S extends SelfBound<S, T>, T> {
			}
			class Test<X extends SelfBound<? extends Y, ?>, Y> {
			}
			public class Client {
				<A extends SelfBound<?,A>> void foo2(A arg2) {
					Object o = new Test<A, SelfBound<?, A>>();
					SelfBound<? extends SelfBound<?, A>, ?> var2 = arg2;
				}
			}
			"""
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=397888
public void test397888a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeParameter, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
	          CompilerOptions.ENABLED);

	this.runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		 new String[] {
 		"X.java",
         """
			/***
			 * @param <T>
			 */
			public class X <T> {
			/***
			 * @param <S>
			 */
				public <S> void ph(int i) {
				}
			}
			"""
         },
 		"""
			----------
			1. ERROR in X.java (at line 8)
				public <S> void ph(int i) {
				                       ^
			The value of the parameter i is not used
			----------
			""",
 		null, true, customOptions);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=397888
public void test397888b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeParameter, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference,
        CompilerOptions.DISABLED);

	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
        new String[] {
     		   "X.java",
                """
					/***
					 * @param <T>
					 */
					public class X <T> {
					/***
					 * @param <S>
					 */
					public <S> void ph() {
					}
					}
					"""
        },
		"""
			----------
			1. ERROR in X.java (at line 4)
				public class X <T> {
				                ^
			Unused type parameter T
			----------
			2. ERROR in X.java (at line 8)
				public <S> void ph() {
				        ^
			Unused type parameter S
			----------
			""",
		null, true, customOptions);
}
// Bug 401456 - Code compiles from javac/intellij, but fails from eclipse
public void test401456() {
	runConformTest(
		new String[] {
			"App.java",
			"import java.util.List;\n" +
			"\n" +
			"public class App {\n" +
			"\n" +
			"    public interface Command_1<T> {\n" +
			"        public void execute(T o);\n" +
			"    }\n" +
			"    public static class ObservableEventWithArg<T> {\n" +
			"        public class Monitor {\n" +
			"            public Object addListener(final Command_1<T> l) {\n" +
			"                return null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    public static class Context<T> {\n" +
			"          public ObservableEventWithArg<String>.Monitor getSubmissionErrorEventMonitor() {\n" +
			"              return new ObservableEventWithArg<String>().new Monitor();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        compileError(new Context<List<String>>());\n" +
			"    }\n" +
			"\n" +
			"    private static void compileError(Context context) {\n" +
			"        context.getSubmissionErrorEventMonitor().addListener(\n" + // here the inner message send bogusly resolved to ObservableEventWithArg#RAW.Monitor
			"            new Command_1<String>() {\n" +
			"                public void execute(String o) {\n" +
			"                }\n" +
			"            });\n" +
			"    }\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
// original test
public void testBug405706a() {
	runNegativeTest(
		new String[] {
			"TypeUnsafe.java",
			"""
				import java.util.Collection;
				
				public class TypeUnsafe {
					public static <Type,
							CollectionType extends Collection<Type>>
							CollectionType
							nullAsCollection(Class<Type> clazz) {
						return null;
					}
				
					public static void main(String[] args) {
						Collection<Integer> integers = nullAsCollection(String.class);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in TypeUnsafe.java (at line 12)
				Collection<Integer> integers = nullAsCollection(String.class);
				                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Collection<String> to Collection<Integer>
			----------
			""");
}
// https://bugs.eclipse.org/405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
// include compatibility List <: Collection
public void testBug405706b() {
	runNegativeTest(
		new String[] {
			"TypeUnsafe.java",
			"""
				import java.util.Collection;
				import java.util.List;
				
				public class TypeUnsafe {
					public static <Type,
							CollectionType extends List<Type>>
							CollectionType
							nullAsList(Class<Type> clazz) {
						return null;
					}
				
					public static void main(String[] args) {
						Collection<Integer> integers = nullAsList(String.class);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in TypeUnsafe.java (at line 13)
				Collection<Integer> integers = nullAsList(String.class);
				                               ^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from List<String> to Collection<Integer>
			----------
			""");
}

// https://bugs.eclipse.org/408441 - Type mismatch using Arrays.asList with 3 or more implementations of an interface with the interface type as the last parameter
public void testBug408441() {
	runConformTest(
		new String[] {
			"TypeMistmatchIssue.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				
				
				public class TypeMistmatchIssue {
					static interface A {
					}
					static class B implements A {
					}
					static class C implements A {
					}
					static class D implements A {
					}
				\t
					void illustrate() {
						List<Class<? extends A>> no1= Arrays.asList(B.class, A.class);						// compiles
						List<Class<? extends A>> no2= Arrays.asList(C.class, B.class, A.class);				// compiles
						List<Class<? extends A>> no3= Arrays.asList(D.class, B.class, A.class);				// compiles
					\t
						List<Class<? extends A>> no4= Arrays.asList(D.class, C.class, B.class, A.class);	// cannot convert error !!!
				
						List<Class<? extends A>> no5= Arrays.asList(A.class, B.class, C.class, D.class);	// compiles
						List<Class<? extends A>> no6= Arrays.asList(A.class, D.class, C.class, B.class);	// compiles
					}
				}
				"""
		});
}

// https://bugs.eclipse.org/413958 - Function override returning inherited Generic Type
public void testBug413958_1() {
	runConformTest(
		new String[] {
			"TestA.java",
			"public class TestA { }\n",
			"TestB.java",
			"public class TestB { }\n",
			"ReadOnlyWrapper.java",
			"""
				@SuppressWarnings("unchecked")
				public class ReadOnlyWrapper<A extends TestA, B extends TestB> {
				    protected A a;
				    protected B b;
				    public ReadOnlyWrapper(A ax,B bx){
				        this.a = ax;
				        this.b = bx;
				    }
				    public <X extends ReadOnlyWrapper<A,B>> X copy() {
				        return (X) new ReadOnlyWrapper<A,B>(a,b);
				    }
				    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {
				        return (X) new ReadOnlyWrapper<A,B>(a,b);
				    }
				    public A getA() {
				        return this.a;
				    }
				    public B getB() {
				        return this.b;
				    }
				}""",
			"WritableWrapper.java",
			"""
				@SuppressWarnings("unchecked")
				public class WritableWrapper<A extends TestA, B extends TestB> extends ReadOnlyWrapper<A, B> {
				    public WritableWrapper(A ax,B bx){
				        super(ax,bx);
				    }
				    @Override
				    public <X extends ReadOnlyWrapper<A,B>> X copy() {
				        return (X) new WritableWrapper<A, B>(a,b);
				    }
				    @Override
				    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {
				        // Works in Indigo, Fails in Kepler
				        return (X) new WritableWrapper<A,B>(a,b);
				    }
				    public void setA(A ax) {
				        this.a = ax;
				    }
				    public void setB(B bx) {
				        this.b = bx;
				    }
				}
				""",
			"TestGenerics.java",
			"""
				public class TestGenerics {
				    public static void main(String [] args) {
				        final WritableWrapper<TestA, TestB> v1 = new WritableWrapper<TestA, TestB>(new TestA(), new TestB());
				        final WritableWrapper<TestA,TestB> v2 = v1.copy();
				        final WritableWrapper<TestA,TestB> v3 = v1.icopy();
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/413958 - Function override returning inherited Generic Type
// Passing since https://bugs.eclipse.org/423496
public void testBug413958_2() {
	String[] sourceFiles =
		new String[] {
			"TestA.java",
			"public class TestA { }\n",
			"TestB.java",
			"public class TestB { }\n",
			"TestA2.java",
			"public class TestA2 extends TestA { }\n",
			"ReadOnlyWrapper.java",
			"""
				@SuppressWarnings("unchecked")
				public class ReadOnlyWrapper<A extends TestA, B extends TestB> {
				    protected A a;
				    protected B b;
				    public ReadOnlyWrapper(A ax,B bx){
				        this.a = ax;
				        this.b = bx;
				    }
				    public <X extends ReadOnlyWrapper<A,B>> X copy() {
				        return (X) new ReadOnlyWrapper<A,B>(a,b);
				    }
				    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {
				        return (X) new ReadOnlyWrapper<A,B>(a,b);
				    }
				    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy2(TA in) {
				        return (X) new ReadOnlyWrapper<A,B>(a,b);
				    }
				    public A getA() {
				        return this.a;
				    }
				    public B getB() {
				        return this.b;
				    }
				}""",
			"WritableWrapper.java",
			"""
				@SuppressWarnings("unchecked")
				public class WritableWrapper<A extends TestA, B extends TestB> extends ReadOnlyWrapper<A, B> {
				    public WritableWrapper(A ax,B bx){
				        super(ax,bx);
				    }
				    @Override
				    public <X extends ReadOnlyWrapper<A,B>> X copy() {
				        return (X) new WritableWrapper<A, B>(a,b);
				    }
				    @Override
				    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy() {
				        return (X) new WritableWrapper<A,B>(a,b);
				    }
				    @Override
				    public <TA extends TestA,TB extends TestB,X extends ReadOnlyWrapper<TA,TB>> X icopy2(TA in) {
				        return (X) new WritableWrapper<A,B>(a,b);
				    }
				    public void setA(A ax) {
				        this.a = ax;
				    }
				    public void setB(B bx) {
				        this.b = bx;
				    }
				}
				""",
			"TestGenerics.java",
			"""
				public class TestGenerics {
				    public static void main(String [] args) {
				        final WritableWrapper<TestA, TestB> v1 = new WritableWrapper<TestA, TestB>(new TestA(), new TestB());
				        final WritableWrapper<TestA,TestB> v2 = v1.copy();
				        final WritableWrapper<TestA,TestB> v3 = v1.icopy();
				        final WritableWrapper<TestA2,TestB> v4 = v1.icopy();
				        final WritableWrapper<TestA2,TestB> v5 = v1.icopy2(new TestA2());
				    }
				}
				"""
		};
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		runNegativeTest(
			sourceFiles,
			"""
				----------
				1. ERROR in TestGenerics.java (at line 6)
					final WritableWrapper<TestA2,TestB> v4 = v1.icopy();
					                                         ^^^^^^^^^^
				Type mismatch: cannot convert from ReadOnlyWrapper<TestA,TestB> to WritableWrapper<TestA2,TestB>
				----------
				""");
	else
		runConformTest(sourceFiles);
}
public void testBug415734() {
	String compileSrc =
			"""
		import java.util.ArrayList;
		import java.util.List;
		
		public class Compile {
		
		    public <T, Exp extends List<T>> Exp typedNull() {
		        return null;
		    }
		
		    public void call() {
		        ArrayList<String> list = typedNull();
		    }
		}
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.EclipseHasABug.EclipseBug428061,
			new String[] {
				"Compile.java",
				compileSrc
			},
			"""
				----------
				1. ERROR in Compile.java (at line 11)
					ArrayList<String> list = typedNull();
					                         ^^^^^^^^^^^
				Type mismatch: cannot convert from List<Object> to ArrayList<String>
				----------
				""");
	} else {
		runConformTest(
			new String[] {
				"Compile.java",
				compileSrc
			});
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426534, [1.8][compiler] Accessibility of vararg element type not checked for generic methods.
public void test426534() {
	runNegativeTest(
		new String[] {
			"p/B.java",
			"""
				package p;
				class A {
				}
				public class B extends A {
				    public <T extends A> void foo(T ... o) { }
				}
				""",

			"X.java",
			"""
				import p.*;
				public class X  {
				    public static void main(String argv[]) {
				        new B().foo(null, null);
				    }
				}
				"""
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. ERROR in X.java (at line 4)
						new B().foo(null, null);
						        ^^^
					The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here
					----------
					""" :
					"""
						----------
						1. WARNING in p\\B.java (at line 5)
							public <T extends A> void foo(T ... o) { }
							                                    ^
						Type safety: Potential heap pollution via varargs parameter o
						----------
						----------
						1. ERROR in X.java (at line 4)
							new B().foo(null, null);
							        ^^^
						The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426589, [1.8][compiler] Compiler error with generic method/constructor invocation as vargs argument
public void test426589() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void take(String... strings) {
						}
						void test() {
							take(getString());
						}
						private <T> String getString() {
							return "hi";
						}
					}
					"""
			},
			"");
}
public void testBug426590() {
	runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
					\t
					}
				\t
					class B extends A {
					\t
					}
				\t
					class C extends B {
					\t
					}
				\t
					class D {
						D(A a) {
						\t
						}
					\t
						D(boolean b) {
							this(b ? new B() : new C());
						}
					}
				"""
		});
}
public void testBug426590b() {
	runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {
					\t
					}
				\t
					class B extends A {
					\t
					}
				\t
					class C extends B {
					\t
					}
				\t
					class D {
						void bla(boolean b) {
							test(b ? new B() : new C());
						}
					\t
						void test(A a) {
						\t
						}
					}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					<T> void foo (T... p);
				}
				abstract class A implements I {
					public void foo(Object [] p) {}
				}
				public class X extends A {
					public static void main(String[] args) {
						A a = new X();
						a.foo("hello", "world");
					}
				}
				"""

		},
		this.complianceLevel >= ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. WARNING in X.java (at line 2)
					<T> void foo (T... p);
					                   ^
				Type safety: Potential heap pollution via varargs parameter p
				----------
				2. WARNING in X.java (at line 5)
					public void foo(Object [] p) {}
					            ^^^^^^^^^^^^^^^^
				Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
				----------
				3. ERROR in X.java (at line 10)
					a.foo("hello", "world");
					  ^^^
				The method foo(Object[]) in the type A is not applicable for the arguments (String, String)
				----------
				""" :
				this.complianceLevel >= ClassFileConstants.JDK1_7 ?
					"""
						----------
						1. WARNING in X.java (at line 2)
							<T> void foo (T... p);
							                   ^
						Type safety: Potential heap pollution via varargs parameter p
						----------
						2. WARNING in X.java (at line 5)
							public void foo(Object [] p) {}
							            ^^^^^^^^^^^^^^^^
						Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
						----------
						""" :
						"""
							----------
							1. WARNING in X.java (at line 5)
								public void foo(Object [] p) {}
								            ^^^^^^^^^^^^^^^^
							Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
							----------
							""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					 <T> void foo (T... p);
				}
				abstract class A  {
					public void foo(Object [] p) {
						System.out.println("A.foo");
					}
				}
				abstract class B extends A implements I {
				}
				public class X extends B implements I {
					public static void main(String[] args) {
						B b = new X();
						b.foo("hello", "world");
					}
				}
				"""
		},
		this.complianceLevel >= ClassFileConstants.JDK1_8 ?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<T> void foo (T... p);
						                   ^
					Type safety: Potential heap pollution via varargs parameter p
					----------
					2. WARNING in X.java (at line 9)
						abstract class B extends A implements I {
						               ^
					Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
					----------
					3. WARNING in X.java (at line 11)
						public class X extends B implements I {
						             ^
					Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
					----------
					4. ERROR in X.java (at line 14)
						b.foo("hello", "world");
						  ^^^
					The method foo(T...) of type I cannot be invoked as it is overridden by an inapplicable method
					----------
					""" :
				this.complianceLevel >= ClassFileConstants.JDK1_7 ?
						"""
							----------
							1. WARNING in X.java (at line 2)
								<T> void foo (T... p);
								                   ^
							Type safety: Potential heap pollution via varargs parameter p
							----------
							2. WARNING in X.java (at line 9)
								abstract class B extends A implements I {
								               ^
							Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
							----------
							3. WARNING in X.java (at line 11)
								public class X extends B implements I {
								             ^
							Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
							----------
							""" :
							"""
								----------
								1. WARNING in X.java (at line 9)
									abstract class B extends A implements I {
									               ^
								Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
								----------
								2. WARNING in X.java (at line 11)
									public class X extends B implements I {
									             ^
								Varargs methods should only override or be overridden by other varargs methods unlike A.foo(Object[]) and I.foo(Object...)
								----------
								""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426633, [1.8][compiler] Compiler generates code that invokes inapplicable method.
public void test426633b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
					 <T> void foo (T... p);
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
		this.complianceLevel >= ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. WARNING in X.java (at line 2)
						<T> void foo (T... p);
						                   ^
					Type safety: Potential heap pollution via varargs parameter p
					----------
					""" : "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426678, [1.8][compiler] Another issue with vararg type element accessibility
public void test426678() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import p.*;
				public class X  {
				    public static void main(String argv[]) {
				        new B().foo(null, null);
				    }
				}
				""",

			"p/B.java",
			"""
				package p;
				class A {
				}
				public class B extends A {
				    public <T extends A> void foo(T ... o) { System.out.println("PGMB"); }
				    public void foo(Object... o) { System.out.println("MB"); }
				}
				""",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"""
					----------
					1. ERROR in X.java (at line 4)
						new B().foo(null, null);
						        ^^^
					The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here
					----------
					""" :
					"""
						----------
						1. ERROR in X.java (at line 4)
							new B().foo(null, null);
							        ^^^
						The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here
						----------
						----------
						1. WARNING in p\\B.java (at line 5)
							public <T extends A> void foo(T ... o) { System.out.println("PGMB"); }
							                                    ^
						Type safety: Potential heap pollution via varargs parameter o
						----------
						""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426678, [1.8][compiler] Another issue with vararg type element accessibility
public void test426678a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import p.*;
				public class X  {
				    public static void main(String argv[]) {
				        new B().foo(null, null);
				    }
				}
				""",
			"p/A.java",
			"""
				package p;
				public class A {
				}
				""",
			"p/B.java",
			"""
				package p;
				public class B extends A {
				    public <T extends A> void foo(T ... o) { System.out.println("PGMB"); }
				    public void foo(Object... o) { System.out.println("MB"); }
				}
				""",
		},
		"PGMB");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421922, [1.8][compiler] Varargs & Overload - Align to JLS8
public void test421922() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import p.*;
				public class X  {
				    public static void main(String argv[]) {
				        new B().foo(null, null);
				    }
				}
				""",

			"p/B.java",
			"""
				package p;
				interface A {
				}
				public class B implements A {
				    public <T extends A> void foo(T ... o) { System.out.println("PGMB"); }
				    public void foo(Object... o) { System.out.println("MB"); }
				}
				""",
		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"""
			----------
			1. ERROR in X.java (at line 4)
				new B().foo(null, null);
				        ^^^
			The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here
			----------
			""" :
			"""
				----------
				1. ERROR in X.java (at line 4)
					new B().foo(null, null);
					        ^^^
				The method foo(T...) of type B is not applicable as the formal varargs element type T is not accessible here
				----------
				----------
				1. WARNING in p\\B.java (at line 5)
					public <T extends A> void foo(T ... o) { System.out.println("PGMB"); }
					                                    ^
				Type safety: Potential heap pollution via varargs parameter o
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler.
public void test425719() {
	String interfaceMethod = this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"   <T> void foo(List<T> list);\n" :
				"""
					   default <T> void foo(List<T> list) {
						   System.out.println("interface method");
					   }
					""";

	runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"interface I {\n" +
			 interfaceMethod +
			"}\n" +
			"class Base {\n" +
			"    public <T> void foo(List<T> list) {\n" +
			"        System.out.println(\"class method\");\n" +
			"   }\n" +
			"}\n" +
			"public class X extends Base implements I {\n" +
			"	 public static void main(String argv[]) {\n" +
			"	    	new X().foo(new ArrayList<String>());\n" +
			"	    }\n" +
			"}\n",
		},
		"class method");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler.
public void test425719a() {
	String interfaceMethod = this.complianceLevel < ClassFileConstants.JDK1_8 ?
				"   <T> void foo(List<T> list);\n\n\n" :
				"""
					   default <T> void foo(List<T> list) {
						   System.out.println("interface method");
					   }
					""";

	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"interface I {\n" +
			 interfaceMethod +
			"}\n" +
			"abstract class Base {\n" +
			"    public abstract <T> void foo(List<T> list);\n" +
			"}\n" +
			"public abstract class X extends Base implements I {\n" +
			"	 public static void main(String argv[]) {\n" +
			"           X x = new Y();\n" +
			"	    	x.foo(new ArrayList<String>());\n" +
			"	    }\n" +
			"}\n" +
			"class Y extends X {}\n",
		},
		"""
			----------
			1. ERROR in X.java (at line 17)
				class Y extends X {}
				      ^
			The type Y must implement the inherited abstract method Base.foo(List<T>)
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425719, [1.8][compiler] Bogus ambiguous call error from compiler.
public void test425719b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	String interfaceMethod = this.complianceLevel < ClassFileConstants.JDK1_8 ?
				"   <T> void foo(List<T> list);\n\n\n" :
				"""
					   default <T> void foo(List<T> list) {
						   System.out.println("interface method");
					   }
					""";

	runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"interface I {\n" +
			 interfaceMethod +
			"}\n" +
			"abstract class Base {\n" +
			"    public abstract <T> void foo(List<T> list);\n" +
			"}\n" +
			"public abstract class X extends Base implements I {\n" +
			"	 public static void main(String argv[]) {\n" +
			"           X x = new Y();\n" +
			"	    	x.foo(new ArrayList<String>());\n" +
			"	    }\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    public <T> void foo(List<T> list) {\n" +
			"        System.out.println(\"Y.foo\");\n" +
			"    }\n" +
			"}\n",
		},
		"Y.foo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427282,  Internal compiler error: java.lang.ArrayIndexOutOfBoundsException: -1 at org.eclipse.jdt.internal.compiler.ClassFile.traverse
public void test427282() {
	runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		new String[] {
			"X.java",
			"""
				import java.util.Collection;
				public class X {
					public X(String... a) {
					}
					public static <T> T[] a(T[] a, T[] b) {
						return null;
					}
					public static void error() {
						final Collection<X> as = null;
				       for (X a : as) {
				           new X(X.a(new String[0], new String[0]));
				       }
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 10)
				for (X a : as) {
				           ^^
			Null pointer access: The variable as can only be null at this location
			----------
			""");
}
public void testBug427216() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test
				{
				   public static void main(String[] args)
				   {
				      foo(args); // ok in 1.7 and 1.8
				      foo(java.util.Arrays.asList("1").toArray(new String[0]));
						System.out.println("good");
				   }
				
				   private static void foo(String... args) { }
				}
				"""
		},
		"good");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427433, NPE at org.eclipse.jdt.internal.compiler.lookup.Scope.parameterCompatibilityLevel(Scope.java:4755)
public void testBug427433() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void testError() {
						assertEquals(A.e(null, null, null), null);
					}
					public static boolean assertEquals(String a, String b) {
						return false;
					}
					public static boolean assertEquals(Object a, Object b) {
						return false;
					}
				}
				class A {
					public static <T, V> V e(T[] t, V[] v, T object) {
						return null;
					}
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427433, NPE at org.eclipse.jdt.internal.compiler.lookup.Scope.parameterCompatibilityLevel(Scope.java:4755)
// variant to challenge a varargs invocation
public void testBug427433b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void testError() {
						assertEquals(A.e(null, null, null), null);
					}
					public static boolean assertEquals(String a, String b, X... xs) {
						return false;
					}
					public static boolean assertEquals(Object a, Object b, X... xs) {
						return false;
					}
				}
				class A {
					public static <T, V> V e(T[] t, V[] v, T object) {
						return null;
					}
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427438#c3, [1.8][compiler] NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
public void testBug427438c3() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				import java.util.List;
				public class X {
					boolean b;
					public List<A> getLignes() {
						return (List<A>) data(b ? (Serializable) get() : null);
					}
					public List<A> get() {
						return null;
					}
					public <T extends Serializable> T data(T data) {
						return data;
					}
					public class A implements Serializable {
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				return (List<A>) data(b ? (Serializable) get() : null);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Serializable to List<X.A>
			----------
			2. WARNING in X.java (at line 14)
				public class A implements Serializable {
				             ^
			The serializable class A does not declare a static final serialVersionUID field of type long
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427411, [1.8][generics] JDT reports type mismatch when using method that returns generic type
public void test427411() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
				
				    public static void main() {
				        List<Object> list = null;
				        Object o = null;
				        genericMethod(list, genericClassTransformer(genericClassFactory(o)));
				        genericMethod(list, genericClassFactory(o)); // works
				        GenericClass<Iterable<? super Object>> tempVariable = genericClassTransformer(genericClassFactory(o));
				        GenericClass<Iterable<? super Object>> tempVariable2 = genericClassFactory(o); // works
				    }
				    private static <T> void genericMethod(T param1, GenericClass<? super T> param2) {
				        throw new UnsupportedOperationException();
				    }
				    public static <T> GenericClass<Iterable<? super T>> genericClassFactory(T item) {
				        throw new UnsupportedOperationException();
				    }
				    public static <T> GenericClass<T> genericClassTransformer(GenericClass<T> matcher) {
				        throw new UnsupportedOperationException();
				    }
				    private static class GenericClass<T> {
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427728, [1.8] Type Inference rejects calls requiring boxing/unboxing
public void test427728() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static <T> int foo(T t) {
						return 1234;
					}
					public static void main(String[] args) {
				            goo(foo(10));
				        }
					static void goo(Integer i) {
						System.out.println(i);
					}
				}
				"""
		},
		"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427728, [1.8] Type Inference rejects calls requiring boxing/unboxing
public void test427728a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Collections;
				public class X {
					public static void mai(String[] args) {
						Math.max(2345, java.util.Collections.max(Collections.<Integer>emptySet()));
						Math.max(0, java.util.Collections.<Integer>max(Collections.<Integer>emptySet()));
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427736, [1.8][generics] Method not applicable error with identical parameter types
public void test427736() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"Test1.java",
			"""
				class Test1<K, V> {
				 static class Node<K2, V2> {}
				 void keySet(V v) {}
				 /**
				  * See {@link #keySet() keySet()},
				  */
				 class KeySetView {}
				 static <K4, V4> void untree0(Node<K4, V4> hi) {}   \s
				 void untreesomething(Node<K, V> n) {
				   untree0(n);\s
				 }
				}
				"""
		},
		"", null, true, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426836, [1.8] special handling for return type in references to method getClass() ?
public void test426836() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static <T> T id(T t) {
						return t;
					}
					public static void main(String[] args) {
						Class<? extends String> id = id(new X().getClass());
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				Class<? extends String> id = id(new X().getClass());
				                             ^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Class<capture#1-of ? extends X> to Class<? extends String>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428071, [1.8][compiler] Bogus error about incompatible return type during override
public void test428071() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"K1.java",
			"""
				import java.util.List;
				import java.util.Map;
				interface K1 {
					public Map<String,List> get();
				}
				""",
			"K.java",
			"""
				import java.util.List;
				import java.util.Map;
				public class K implements K1 {
					public Map<String, List> get() {
						return null;
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in K1.java (at line 4)
				public Map<String,List> get();
				                  ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			----------
			1. WARNING in K.java (at line 4)
				public Map<String, List> get() {
				                   ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			""",
		null,
		true,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428019, [1.8][compiler] Type inference failures with nested generic invocation.
public void test428019() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
				  static class Obj {}
				  static class Dial<T> {}
				  static void foo(Dial<? super Obj> dial, X context) {
				    context.put(Dial.class, wrap(dial));
				  }
				  <T> void put(Class<T> clazz, T data) {
					System.out.println("put");
				  }
				  static <T> Dial<T> wrap(Dial<T> dl) {
					  return null;
				  }
				  public static void main(String[] args) {
					X.foo(new Dial<Obj>(), new X());
				  }
				}
				"""
		},
		"put");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428285,  [1.8][compiler] ECJ fails to recognize ? super Object == { Object }
public void test428285() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				class Reference<T> {
					ReferenceQueue<? super T>  queue;
				}
				class ReferenceQueue<T> {
				}
				public class X {
				    public static void main(String args[]) {
				            Reference<Object> r = new Reference<Object>();
				            ReferenceQueue<Object> q = r.queue;
				            System.out.println("OK");
				    }
				}
				"""
		},
		"OK");
}
public void testBug428366() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					<T> void m(String s, int i) {}
					<T> void m(String s1, String s2) {}
					void test() {
						m("1", null);
					}
					Zork z;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
public void test429733b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						test(id(1.1d));
					}
					static <S> void test(S value) {
				       System.out.println(value);
					}
					static <T> T id(T t) {
				       return t;
				   }
				}
				"""
		},
		"1.1");
}
public void test429733c() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						new X();
					}
					<S> X(S value) {
				       System.out.println(value);
					}
					static <T> T id(T t) {
				       return t;
				   }
				   X() {
				      this(id(1.1d));
				   }
				}
				"""
		},
		"1.1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426537,  [1.8][inference] Eclipse compiler thinks I<? super J> is compatible with I<J<?>> - raw type J involved
public void testBug426537() { // non generic case
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(J[] list, I<J<?>> i) {
						sort(list, i);
					}
					J[] sort(J[] list, I<? super J> i) {
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
			The method sort(J[], I<? super J>) in the type X is not applicable for the arguments (J[], I<J<?>>)
			----------
			3. WARNING in X.java (at line 5)
				J[] sort(J[] list, I<? super J> i) {
				^
			J is a raw type. References to generic type J<T> should be parameterized
			----------
			4. WARNING in X.java (at line 5)
				J[] sort(J[] list, I<? super J> i) {
				         ^
			J is a raw type. References to generic type J<T> should be parameterized
			----------
			5. WARNING in X.java (at line 5)
				J[] sort(J[] list, I<? super J> i) {
				                             ^
			J is a raw type. References to generic type J<T> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426537,  [1.8][inference] Eclipse compiler thinks I<? super J> is compatible with I<J<?>> - raw type J involved
public void testBug426537_generic() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(J[] list, I<J<?>> i) {
						sort(list, i);
					}
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427957, [1.8] Type inference incorrect when a wildcard is missing
public void testBug427957() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    <T> void sort(T[] a, I<? super T> c) { }
				    void foo(I[] e, I<I<?>> comp) {
				        sort(e, comp);
				    }
				}
				interface I<T> {}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				void foo(I[] e, I<I<?>> comp) {
				         ^
			I is a raw type. References to generic type I<T> should be parameterized
			----------
			2. ERROR in X.java (at line 4)
				sort(e, comp);
				^^^^
			The method sort(T[], I<? super T>) in the type X is not applicable for the arguments (I[], I<I<?>>)
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427992,  [1.8] compiler difference to javac involving a raw array
public void test427992() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return; // uses @Override
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import static org.junit.Assert.assertArrayEquals;
				import java.util.Arrays;
				import org.junit.Test;
				public class X {
				  @Test(expected = IllegalArgumentException.class)
				  public void shouldThrowExceptionWhenClassesAreNotInSameInheritanceTree() {
				    Arrays.sort(new Class[] {Chimp.class, Cat.class}, ClassInheritanceDepthComparator.INSTANCE);
				  }
				  public static class Animal {
				  }
				  public static class Monkey extends Animal {
				  }
				  public static class Chimp extends Monkey {
				  }
				  public static class Cat extends Animal {
				  }
				public static class ClassInheritanceDepthComparator implements Comparator<Class<?>> {
				  public static final ClassInheritanceDepthComparator INSTANCE = new ClassInheritanceDepthComparator();
				  @Override
				  public int compare(Class<?> c1, Class<?> c2) {
				    if(c1.equals(c2)) {
				      return 0;
				    }
				    if(c1.isAssignableFrom(c2)) {
				      return -1;
				    }
				    if(c2.isAssignableFrom(c1)) {
				      return 1;
				    }
				    throw new IllegalArgumentException("classes to compare must be in the same inheritance tree: " + c1 + "; " + c2);
				  }
				}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				import static org.junit.Assert.assertArrayEquals;
				              ^^^^^^^^^
			The import org.junit cannot be resolved
			----------
			2. ERROR in X.java (at line 3)
				import org.junit.Test;
				       ^^^^^^^^^
			The import org.junit cannot be resolved
			----------
			3. ERROR in X.java (at line 5)
				@Test(expected = IllegalArgumentException.class)
				 ^^^^
			Test cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 7)
				Arrays.sort(new Class[] {Chimp.class, Cat.class}, ClassInheritanceDepthComparator.INSTANCE);
				       ^^^^
			The method sort(T[], Comparator<? super T>) in the type Arrays is not applicable for the arguments (Class[], X.ClassInheritanceDepthComparator)
			----------
			5. ERROR in X.java (at line 17)
				public static class ClassInheritanceDepthComparator implements Comparator<Class<?>> {
				                                                               ^^^^^^^^^^
			Comparator cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 20)
				public int compare(Class<?> c1, Class<?> c2) {
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method compare(Class<?>, Class<?>) of type X.ClassInheritanceDepthComparator must override or implement a supertype method
			----------
			""");
}
public void testBug430987() {
	String source =
			"""
		public class X {
		
		  public static interface Foo<T> {
		    // no content
		  }
		
		  public void compileError() {
		    doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
		  }
		
		  public void noCompileError() {
		    Foo foo = any( Foo.class );
		    doSomethingWithFoo( foo, foo );
		  }
		
		  public void fix() {
		    this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
		  }
		
		  public <T> void  doSomethingWithFoo( Foo<T> foo, Foo<T> foo2 ) {
		    // do something
		  }
		
		  public static <T> T any(Class<T> clazz) {
		    return null;
		  }
		
		}
		""";
		runNegativeTest(
			new String[] {
				"X.java",
				source
			},
			"""
				----------
				1. WARNING in X.java (at line 8)
					doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation doSomethingWithFoo(X.Foo, X.Foo) of the generic method doSomethingWithFoo(X.Foo<T>, X.Foo<T>) of type X
				----------
				2. WARNING in X.java (at line 8)
					doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
					                    ^^^^^^^^^^^^^^^^
				Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>
				----------
				3. WARNING in X.java (at line 8)
					doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
					                                      ^^^^^^^^^^^^^^^^
				Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>
				----------
				4. WARNING in X.java (at line 12)
					Foo foo = any( Foo.class );
					^^^
				X.Foo is a raw type. References to generic type X.Foo<T> should be parameterized
				----------
				5. WARNING in X.java (at line 13)
					doSomethingWithFoo( foo, foo );
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation doSomethingWithFoo(X.Foo, X.Foo) of the generic method doSomethingWithFoo(X.Foo<T>, X.Foo<T>) of type X
				----------
				6. WARNING in X.java (at line 13)
					doSomethingWithFoo( foo, foo );
					                    ^^^
				Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>
				----------
				7. WARNING in X.java (at line 13)
					doSomethingWithFoo( foo, foo );
					                         ^^^
				Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>
				----------
				8. WARNING in X.java (at line 17)
					this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation doSomethingWithFoo(X.Foo, X.Foo) of the generic method doSomethingWithFoo(X.Foo<T>, X.Foo<T>) of type X
				----------
				9. WARNING in X.java (at line 17)
					this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
					                                 ^^^^^^^^^^^^^^^^
				Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>
				----------
				10. WARNING in X.java (at line 17)
					this.<Object>doSomethingWithFoo( any( Foo.class ), any( Foo.class ) );
					                                                   ^^^^^^^^^^^^^^^^
				Type safety: The expression of type X.Foo needs unchecked conversion to conform to X.Foo<Object>
				----------
				""");
}
public void testBug430686() {
	runConformTest(
		new String[] {
			"TestClass.java",
			"""
				
				public class TestClass
				{
				    private static class Alice<A extends Alice<A, B>, B extends Bob>
				    {
				    }
				
				    public static class Bob
				    {
				    }
				
				    public void callingMethod()
				    {
				        calledMethod(); // error: The method calledMethod() in the type TestClass is not applicable for the arguments ()
				    }
				
				    private <A extends Alice<A, B>, B extends Bob> A calledMethod()
				    {
				        return null;
				    }
				}
				"""
		});
}
public void testBug430759() {
	runConformTest(
		new String[] {
			"A.java",
			"""
				class CriteriaBuilder {
				
				}
				
				class CriteriaQuery<T> {
				
				}
				
				class Root<T> {
				
				}
				
				public class A<E> {
				
				    protected abstract class CustomGenericQuery<T> {
				    }
				
				    protected <T> T executeCustomSingleQuery(CustomGenericQuery<T> customQuery, Class<T> resultClass) {
					return null;
				    }
				
				    public Long getCount() {
					return executeCustomSingleQuery(
				
					new CustomGenericQuery<Long>() {
					    public void customizeQuery(final Root<E> root, final CriteriaBuilder cb,
						    CriteriaQuery<Long> cq) {
					    }
					}, Long.class);
				    }
				}
				"""
		});
}
public void testBug431408() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"""
				public class EclipseJava8Generics {
				
				  public interface Foo<V> {
				  }
				
				  public static class FooBar<V, T extends Foo<V>> {
				  }
				
				  public static class BaseClass {
				    protected <V> FooBar<V, ? extends Foo<V>> doSomething() {
				      return null;
				    }
				  }
				
				  public static class DerivedClass extends BaseClass {
				    @Override
				    protected <V> FooBar<V, ? extends Foo<V>> doSomething() {
				      //Eclipse 4.3.2 with Java 8 can't compile the next line\s
				      FooBar<V, ? extends Foo<V>> prop = super.doSomething();
				      return prop;
				    }
				  }
				}
				"""
		});
}

public void testBug431581() {
	runNegativeTest(
		new String[] {
			"BugEclipse.java",
			"""
				public class BugEclipse
				{
				  static Dog dog = new Dog();
				  public static void main(String[] args)
				  {
				    System.out.println("bug compile eclipse");
				    Cat cat = getDog(); // <- error here, eclipse compile this line but the execution print ClassCastException
				  }
				  public static <T extends Dog> T getDog()
				  {
				    return (T) dog;
				  }
				  static class Cat {
				  }
				  static class Dog {
				  }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in BugEclipse.java (at line 7)\n" +
		"	Cat cat = getDog(); // <- error here, eclipse compile this line but the execution print ClassCastException\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8	?
		"	          ^^^^^^\n" +
		"Bound mismatch: The generic method getDog() of type BugEclipse is not applicable for the arguments (). The inferred type BugEclipse.Cat&BugEclipse.Dog is not a valid substitute for the bounded parameter <T extends BugEclipse.Dog>\n"
		:
		"	          ^^^^^^^^\n" +
		"Type mismatch: cannot convert from BugEclipse.Dog to BugEclipse.Cat\n"
		) +
		"----------\n" +
		"2. WARNING in BugEclipse.java (at line 11)\n" +
		"	return (T) dog;\n" +
		"	       ^^^^^^^\n" +
		"Type safety: Unchecked cast from BugEclipse.Dog to T\n" +
		"----------\n");
}
public void testBug432603() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.Map;
				import java.util.Map.Entry;
				
				abstract class Optional<T> {
					public static <T> Optional<T> fromNullable(T t) { return null; }
					abstract Optional<T> or(Optional<? extends T> secondChoice);
					abstract T or(Supplier<? extends T> supplier);
					abstract T or(T defaultValue);
				}
				
				interface Supplier<T> { T get(); }
				
				public class Test {
				
				    private static final Object NO_VALUE = new Object();
				
				    public void method(Map<String, ?> map) {
				        for (Entry<String, ?> entry : map.entrySet()) {
				            Optional.fromNullable(entry.getValue()).or(NO_VALUE);
				//                                                  ^^ error here
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 19)
				Optional.fromNullable(entry.getValue()).or(NO_VALUE);
				                                        ^^
			The method or(Optional<? extends capture#2-of ?>) in the type Optional<capture#2-of ?> is not applicable for the arguments (Object)
			----------
			""",
		JavacTestOptions.Excuse.JavacCompilesIncorrectSource);
}
public void testBug432603a() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.Map;
				import java.util.Map.Entry;
				
				abstract class Optional<T> {
					public static <T> Optional<T> fromNullable(T t) { return null; }
					abstract Optional<T> or(Optional<? extends T> secondChoice);
					abstract T or(Supplier<? extends T> supplier);
					abstract T or(T defaultValue);
				}
				
				interface Supplier<T> { T get(); }
				
				public class Test {
				
				    private static final Object NO_VALUE = new Object();
				
				    public void method(Map<String, ?> map) {
				        for (Entry<String, ?> entry : map.entrySet()) {
				            Optional.<Object>fromNullable(entry.getValue()).or(NO_VALUE);
				//                                                  ^^ error here
				        }
				    }
				}
				"""
		});
}
public void testBug399527() {
	runNegativeTest(
		false /*skipJavac */,
		JavacTestOptions.Excuse.JavacCompilesIncorrectSource,
		new String[] {
			"TypeInferenceProblem.java",
			"""
				
				public class TypeInferenceProblem {
				  interface HeaderAccess<T> {
				    T getHeader();
				  }
				
				  interface IExpectationSetters<T> {
				    IExpectationSetters<T> andReturn(T value);
				  }
				
				  static class MocksControl implements IExpectationSetters<Object> {
				    public IExpectationSetters<Object> andReturn(Object value) {
				      return null;
				    }
				  }
				
				  @SuppressWarnings("unchecked")
				  public static <T> IExpectationSetters<T> expect(final T value) {
				    return (IExpectationSetters<T>) new MocksControl();
				  }
				
				  private HeaderAccess<Object> mockHeaderAccess;
				  private HeaderAccess<?> unboundedMockHeaderAccess;
				
				  public void test() {
				    // No error
				    expect(mockHeaderAccess.getHeader()).andReturn(new Object());
				    /*
				     * Error: The method andReturn(capture#1-of ?) in the type
				     * TypeInferenceProblem.IExpectationSetters<capture#1-of ?>\s
				     * is not applicable for the arguments (Object)
				     */
				    expect(unboundedMockHeaderAccess.getHeader()).andReturn(new Object());
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in TypeInferenceProblem.java (at line 33)
				expect(unboundedMockHeaderAccess.getHeader()).andReturn(new Object());
				                                              ^^^^^^^^^
			The method andReturn(capture#1-of ?) in the type TypeInferenceProblem.IExpectationSetters<capture#1-of ?> is not applicable for the arguments (Object)
			----------
			""");
}
public void testBug399527_corrected() {
	runConformTest(
		new String[] {
			"TypeInferenceProblem.java",
			"""
				
				public class TypeInferenceProblem {
				  interface HeaderAccess<T> {
				    T getHeader();
				  }
				
				  interface IExpectationSetters<T> {
				    IExpectationSetters<T> andReturn(T value);
				  }
				
				  static class MocksControl implements IExpectationSetters<Object> {
				    public IExpectationSetters<Object> andReturn(Object value) {
				      return null;
				    }
				  }
				
				  @SuppressWarnings("unchecked")
				  public static <T> IExpectationSetters<T> expect(final T value) {
				    return (IExpectationSetters<T>) new MocksControl();
				  }
				
				  private HeaderAccess<Object> mockHeaderAccess;
				  private HeaderAccess<?> unboundedMockHeaderAccess;
				
				  public void test() {
				    // No error
				    expect(mockHeaderAccess.getHeader()).andReturn(new Object());
				    this.<Object>expect(unboundedMockHeaderAccess.getHeader()).andReturn(new Object());
				  }
				}
				"""
		});
}
public void testBug399527_comment1() {
	String sourceString =
			"""
		public class TypeInferenceProblemMin {
		  interface HeaderAccess<T> {
		    T getHeader();
		  }
		
		  interface IExpectationSetters<T> {
		  }
		
		  public static <T> IExpectationSetters<T> expect(final T value) {
			  return null;
		  }
		
		  private HeaderAccess<?> unboundedMockHeaderAccess;
		 \s
		  public void test() {
		    // no error:
		    Object header = unboundedMockHeaderAccess.getHeader();
		    IExpectationSetters<Object> exp1 = expect(header);
		
		    // Type mismatch: cannot convert from TypeInferenceProblemMin.IExpectationSetters<capture#2-of ?> to TypeInferenceProblemMin.IExpectationSetters<Object>
		    IExpectationSetters<Object> exp2 = expect(unboundedMockHeaderAccess.getHeader());
		  }
		}
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		runNegativeTest(
			new String[] {
				"TypeInferenceProblemMin.java",
				sourceString
			},
			"""
				----------
				1. ERROR in TypeInferenceProblemMin.java (at line 21)
					IExpectationSetters<Object> exp2 = expect(unboundedMockHeaderAccess.getHeader());
					                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from TypeInferenceProblemMin.IExpectationSetters<capture#2-of ?> to TypeInferenceProblemMin.IExpectationSetters<Object>
				----------
				""");
	else
		// conform due to target typing
		runConformTest(
			new String[] {
				"TypeInferenceProblemMin.java",
				sourceString
			});
}
public void testBug434570() {
	runConformTest(
		new String[] {
			"example/Example.java",
			"""
				package example;
				
				import example.Example.Config;
				import example.Example.CustomInitializer;
				
				@Config(initializers = CustomInitializer.class)
				public class Example {
				
					static interface Context {
					}
				
					static interface ConfigurableContext extends Context {
					}
				
					static abstract class AbstractContext implements ConfigurableContext {
					}
				
					static class GenericContext extends AbstractContext {
					}
				
					static interface Initializer<C extends ConfigurableContext> {
					}
				
					static @interface Config {
						Class<? extends Initializer<? extends ConfigurableContext>>[] initializers() default {};
					}
				
					static class CustomInitializer implements Initializer<GenericContext> {
					}
				
					@Config(initializers = CustomInitializer.class)
					static class CompilationSuccess {
					}
				
				}
				"""
		});
}
public void testBug434630() {
	runConformTest(
		new String[] {
			"Foo.java",
			"""
				interface Provider<T> {}
				@interface ProvidedBy {
					Class<? extends Provider<?>> value();\
				}
				
				@ProvidedBy(Foo.SomeProvider.class)
				public interface Foo {
				\t
					public static class SomeProvider implements Provider<Foo> {
				
						public Foo get() {
							return null;
						}
					\t
					}
				}
				"""
		});
}
public void testBug434570_comment3() {
	runConformTest(
		new String[] {
			"TestWontCompile.java",
			"""
				import org.bug.AnnotationWithClassParameter;
				import org.bug.CustomHandler;
				import org.bug.Handler;
				
				
				@AnnotationWithClassParameter(CustomHandler.class)
				public class TestWontCompile extends ATest<Object> {
				\t
					public static void main(String[] args) {
						Class<? extends Handler<?>> h = CustomHandler.class;
					}
				
				}
				""",
			"ATest.java",
			"""
				public abstract class ATest<T> {
				
				}
				""",
			"org/bug/Item.java",
			"""
				package org.bug;
				
				public interface Item {
				
				}
				""",
			"org/bug/CustomItem.java",
			"""
				package org.bug;
				
				public class CustomItem implements Item {
				
				}
				""",
			"org/bug/Handler.java",
			"""
				package org.bug;
				
				public abstract class Handler<T extends Item> {
				
				}
				""",
			"org/bug/CustomHandler.java",
			"""
				package org.bug;
				
				public class CustomHandler extends Handler<CustomItem> {
				
				}
				""",
			"org/bug/AnnotationWithClassParameter.java",
			"""
				package org.bug;
				
				import java.lang.annotation.Documented;
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				
				@Target(ElementType.TYPE)
				@Retention(RetentionPolicy.RUNTIME)
				@Documented
				public @interface AnnotationWithClassParameter {
				\t
					Class<? extends Handler<?>> value();
				
				}
				"""
		});
}
// same test but with null annotations analysis enabled
public void testBug434570_comment3b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	runConformTest(
		new String[] {
			"TestWontCompile.java",
			"""
				import org.bug.AnnotationWithClassParameter;
				import org.bug.CustomHandler;
				import org.bug.Handler;
				
				
				@AnnotationWithClassParameter(CustomHandler.class)
				public class TestWontCompile extends ATest<Object> {
				\t
					public static void main(String[] args) {
						Class<? extends Handler<?>> h = CustomHandler.class;
					}
				
				}
				""",
			"ATest.java",
			"""
				public abstract class ATest<T> {
				
				}
				""",
			"org/bug/Item.java",
			"""
				package org.bug;
				
				public interface Item {
				
				}
				""",
			"org/bug/CustomItem.java",
			"""
				package org.bug;
				
				public class CustomItem implements Item {
				
				}
				""",
			"org/bug/Handler.java",
			"""
				package org.bug;
				
				public abstract class Handler<T extends Item> {
				
				}
				""",
			"org/bug/CustomHandler.java",
			"""
				package org.bug;
				
				public class CustomHandler extends Handler<CustomItem> {
				
				}
				""",
			"org/bug/AnnotationWithClassParameter.java",
			"""
				package org.bug;
				
				import java.lang.annotation.Documented;
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				
				@Target(ElementType.TYPE)
				@Retention(RetentionPolicy.RUNTIME)
				@Documented
				public @interface AnnotationWithClassParameter {
				\t
					Class<? extends Handler<?>> value();
				
				}
				"""
		},
		options);
}
public void testBug434630_comment7() {
	runConformTest(
		new String[] {
			"test/FooImpl.java",
			"""
				package test;
				
				public class FooImpl implements Foo {
				
				}
				""",
			"test/Foo.java",
			"""
				package test;
				interface Provider<T> {}
				@interface ProvidedBy {
					Class<? extends Provider<?>> value();\
				}
				
				@ProvidedBy(Foo.SomeProvider.class)
				public interface Foo {
				\t
					public static class SomeProvider implements Provider<Foo> {
				
						public Foo get() {
							return null;
						}
					\t
					}
				}
				"""
		});
}
public void testBug434044() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"""
				public class EclipseJava8Generics {
				
				  public interface Foo<V> {
				    public V doFoo();
				  }
				
				  public static class FooBar<V, T extends Foo<V>> {
				    public T getBar() {
				      return null;
				    }
				  }
				
				  public static class Factory {
				
				    public static <V, T extends Foo<V>> FooBar<V, T> createFooBar() {
				      return null;
				    }
				  }
				
				  public static void test() {
				    final FooBar<?, ? extends Foo<?>> typedProperty = Factory.createFooBar();
				    //TODO Eclipse Bug 434044
				    final Object propertyValue = typedProperty.getBar().doFoo();
				
				  }
				
				}
				"""
		});
}
public void testBug434044_comment20() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"""
				public class EclipseJava8Generics {
				
				  public interface Foo<V> {
				    public V doFoo();
				  }
				
				  public static class FooBar<V, T extends Foo<V>> {
				    public T getBar() {
				      return null;
				    }
				  }
				
				  public static abstract class AbstractFoo<V> implements Foo<V> {
				  }
				
				  public static class Factory {
				    public static <V, T extends AbstractFoo<V>> FooBar<V, T> createFooBar() {
				      return null;
				    }
				  }
				
				  public static void test() {
				    final FooBar<?, ? extends AbstractFoo<?>> typedProperty = Factory.createFooBar();
				    //TODO Eclipse Bug 434044 still exists
				    final Object propertyValue = typedProperty.getBar().doFoo();
				  }
				
				}
				"""
		});
}
public void testBug434044_comment36() {
	runNegativeTest(
		new String[] {
			"EclipseJava8Generics.java",
			"""
				public class EclipseJava8Generics {
				
				  public interface Nasty {
				    public Object doFoo(Integer a);
				  }
				  public interface Foo<V> {
				    public V doFoo(String a);
				  }
				
				  public static class FooBar<V, T extends Foo<V>> {
				    public T getBar() {
				      return null;
				    }
				  }
				
				  public static abstract class AbstractFoo<V> implements Foo<V>, Nasty {
				  }
				
				  public static class Factory {
				    public static <V, T extends AbstractFoo<V>> FooBar<V, T> createFooBar() {
				      return null;
				    }
				  }
				
				  public static void test() {
				    final FooBar<?, ? extends AbstractFoo<?>> typedProperty = Factory.createFooBar();
				    //TODO Eclipse Bug 434044 still exists
				    final Object propertyValue = typedProperty.getBar().doFoo(null);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in EclipseJava8Generics.java (at line 28)
				final Object propertyValue = typedProperty.getBar().doFoo(null);
				                                                    ^^^^^
			The method doFoo(String) is ambiguous for the type capture#2-of ? extends EclipseJava8Generics.AbstractFoo<?>
			----------
			""");
}
public void testBug434793() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	runConformTest(
		new String[] {
			"Outer.java",
			"""
				import java.util.*;
				
				public class Outer {
					private static class SingletonList<E>
								 extends AbstractList<E>
								 implements java.util.RandomAccess, java.io.Serializable {
						public E get(int i) { throw new RuntimeException(); }
						public int size() { return 0; }
					}
				}
				"""
		},
		options);
}
public void testBug435643() {
	runConformTest(
		new String[] {
			"test/Main.java",
			"""
				package test;
				abstract class ASTNode<T extends ASTNode> implements Iterable<T> {
					public T getChild(int i) { return null; }
				}\s
				abstract class List<T extends ASTNode> extends ASTNode<T> { }
				class Joiner {
					  public static Joiner on(String separator) {
					    return null;
					  }
					  String join(Iterable<?> parts) {
						  return "";
					  }
				}
				class AstFunctions {
					  public static <T extends ASTNode<?>> Function<T, String> prettyPrint() {
						  return null;
					  }
				}
				class Iterables {
					public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable,
						      final Function<? super F, ? extends T> function) {
						return null;
					}
				}
				interface Function<F, T> {
					 T apply(F input);
				}
				public class Main {
				
					  String test(ASTNode<?> node, ASTNode rawNode) {
						rawNode.getChild(0);\s
					   \s
				        @SuppressWarnings("unchecked")
				        List<? extends ASTNode<?>> list = (List<? extends ASTNode<?>>) node;
				        return Joiner.on(", ").join(Iterables.transform(list, AstFunctions.prettyPrint()));
					  }
				
				}
				"""
		});
}
public void testBug438337comment5() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					void test() {
						I18nResource<?> i18nResource = null;
					}
				}
				""",
			"I18nResource.java",
			"public interface I18nResource<E extends Internationalized<? extends I18nResource<E>>> extends BusinessObject {}\n",
			"Internationalized.java",
			"public interface Internationalized<E extends I18nResource<? extends Internationalized<E>>>\n" +
			"    extends BusinessObject {}\n",
			"BusinessObject.java",
			"public interface BusinessObject {}\n"
		});
}
public void testBug438337comment3() {
	runConformTest(
		new String[] {
			"PermissionDrivenPresenter.java",
			"""
				public abstract class PermissionDrivenPresenter<V extends BaseView<? extends Presenter<V>>> extends BasePresenter<V> {
				
				    public void updatePermissions() {
				        getView().setReadOnly(true);
				    }
				}
				""",
			"View.java",
			"public interface View<P extends Presenter<? extends View<P>>> { }\n",
			"Presenter.java",
			"public interface Presenter<V extends View<? extends Presenter<V>>> { }\n",
			"BaseView.java",
			"""
				public abstract class BaseView<P extends Presenter<? extends View<P>>> implements View<P> {
					void setReadOnly(boolean f) {}
				}
				""",
			"BasePresenter.java",
			"""
				public abstract class BasePresenter<V extends View<? extends Presenter<V>>> implements Presenter<V> {
				    public V getView() {
				        return null;
				    }
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422832, Class file triggers StackOverflowError when creating type hierarchy
public void testBug422832() {
	String path = getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator +
			"Bug422832ClassFile" + File.separator + "aspose.pdf.jar";
	String[] libs = getDefaultClassPaths();
	int len = libs.length;
	System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
	libs[len] = path;
	runNegativeTest(
			new String[] {
					"ExampleClass.java",
					"public class ExampleClass extends aspose.b.a.a {}\n",
			},
			"""
				----------
				1. ERROR in ExampleClass.java (at line 1)
					public class ExampleClass extends aspose.b.a.a {}
					             ^^^^^^^^^^^^
				The hierarchy of the type ExampleClass is inconsistent
				----------
				""",
			libs, false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=416480, Error in bytecode generated by ECJ compiler leads to IncompatibleClassChangeError
public void test416480() {
  this.runConformTest(
      new String[] {
          "X.java",
          """
			public class X<T extends Object & Runnable> {
			    private Runnable cast(T obj) {
			        return obj;
			    }
			    public static void main(String[] args) {
			        try {
			            Runnable runnable = new X().cast(new Object());
			        } catch (ClassCastException c) {
			            System.out.println("CCE");
			        }
			    }
			}
			"""
      },
      "CCE");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444024, Type mismatch error in annotation generics assignment which happens "sometimes"
public void test444024() {
		this.runConformTest(
		   new String[] {
			   "ViewpointOrganisationEntity.java",
			   "abstract public class ViewpointOrganisationEntity<T> {\n" +
			   "}\n",
			   "MetaCombo.java",
			   """
				public @interface MetaCombo {
					Class< ? extends IComboDataSet< ? >> dataSet();
				}
				""",
			   "IComboDataSet.java",
			   "public interface IComboDataSet<T> {\n" +
			   "}\n",
			   "ContractantTypeLister.java",
			   "public class ContractantTypeLister implements IComboDataSet<ContractantType> {\n" +
			   "}\n",
			   "ContractantType.java",
			   """
				@MetaCombo(dataSet = ContractantTypeLister.class)
				public class ContractantType extends ViewpointOrganisationEntity<Long>  {
				}
				""",
		       "Contractant.java",
		       """
				public class Contractant extends ViewpointOrganisationEntity<Long> {
					@MetaCombo(dataSet = ContractantTypeLister.class)
					public ContractantType getContractantType() {
						return null;
					}
				}
				""",
		   },
		   "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440019, [1.8][compiler] Type mismatch error with autoboxing/scalar types (works with 1.6)
public void test440019() {
		this.runConformTest(
		   new String[] {
			   "A.java",
			   """
				public class A {
				  <T> T get(String name, T defaultValue) { return defaultValue; }
				  void x() {
				    long a1 = get("key", 0);
				    long a3 = get("key", 0L);
				  }
				}
				""",
		   },
		   "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443596, [1.8][compiler] Failure for overload resolution in case of Generics and Varags
public void test443596() {
	this.runNegativeTest(
		   new String[] {
			   "X.java",
			   """
				public final class X {
				    static interface Predicate<T> { boolean test(T object); }
				    public static <T> Predicate<T> in(Predicate<? extends T> arg) { return null; }
				    public static <T> Predicate<T> and(Predicate<? super T>... arg) { return null; }
				    public static <T> Predicate<T> and(Predicate<? super T> arg0, Predicate<? super T> arg1) { return null; }
				    static class FilteredCollection<E> {
				        Predicate<? super E> predicate;
				        public void error(Predicate<?> arg) { and(predicate, in(arg)); } // no compile
				    }
				}
				""",
		   },
		   this.complianceLevel < ClassFileConstants.JDK1_7 ?
		   "" :
		   """
			----------
			1. WARNING in X.java (at line 4)
				public static <T> Predicate<T> and(Predicate<? super T>... arg) { return null; }
				                                                           ^^^
			Type safety: Potential heap pollution via varargs parameter arg
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446235, Java8 generics and boxing
public void test446235() {
		this.runConformTest(
		   new String[] {
			   "IntegerLongBug.java",
			   """
				public class IntegerLongBug {
					public static void main(String ar[]) {
						Integer number = 1000;
						long numberLong = number; //compiles fine
						long num = getNumber(5000); // compilation error
					}
					public static <T> T getNumber(T num) {
						return num;
					}
				}
				""",
		   },
		   "");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440019, [1.8][compiler] Type mismatch error with autoboxing/scalar types (works with 1.6)
public void test440019_c9() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   """
				public class X {
				    public static final int CORE_POOL_SIZE = 3;
				    public static final int KEEP_ALIVE_TIME = 60; // seconds
				    X(final int size, final long ttl){
				        System.out.println("size: " + size + " " + " ttl: " + ttl);
				    }
				    public static void main(String[] args) {
				        new X(CORE_POOL_SIZE, get(KEEP_ALIVE_TIME)); // [1]
				    }
				    public static <T> T get(T value) {
				        return value;
				    }
				}
				""",
		   },
		   "size: 3  ttl: 60");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446223, [1.8][compiler] Java8 generics eclipse doesn't compile
public void test446223() {
		this.runNegativeTest(
		   false /* skipJavac */,
		   JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		   new String[] {
			   "X.java",
			   """
				public class X {
					public static void main(String ar[]) {
						System.out.println("hi");
						DoNothing();
					}
					public interface Interface1 {
						public void go();
					}
					public interface Interface2<X> {
						public X go2();
					}
					private static <X, T extends Interface2<X> & Interface1> void DoNothing() {
						return;
					}
				}
				""",
		   },
		   """
			----------
			1. WARNING in X.java (at line 9)
				public interface Interface2<X> {
				                            ^
			The type parameter X is hiding the type X
			----------
			2. WARNING in X.java (at line 12)
				private static <X, T extends Interface2<X> & Interface1> void DoNothing() {
				                ^
			The type parameter X is hiding the type X
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444334,  [1.8][compiler] Compiler generates error instead of warning on unchecked conversion
public void test444334() {
		this.runNegativeTest(
		   new String[] {
			   "X.java",
			   """
				import java.util.ArrayList;
				import java.util.List;
				public class X {
				    public static void Main(String[] args) {
				        doSomething(returnClassType(Class.class));
				        doSomething(returnListType(new ArrayList<List>()));
				    }
				    public static <T> void doSomething(Class<T> clazz) {
				        System.out.println(clazz.getSimpleName());
				    }
				    public static <T> T returnClassType(Class<T> clazz) {
				        return null;
				    }
				    public static <T> void doSomething(List<T> list) {
				        System.out.println(list.getClass().getSimpleName());
				    }
				    public static <T> T returnListType(List<T> list) {
				        return null;
				    }
				}
				""",
		   },
		   """
			----------
			1. WARNING in X.java (at line 5)
				doSomething(returnClassType(Class.class));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation doSomething(Class) of the generic method doSomething(Class<T>) of type X
			----------
			2. WARNING in X.java (at line 5)
				doSomething(returnClassType(Class.class));
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The expression of type Class needs unchecked conversion to conform to Class<Object>
			----------
			3. WARNING in X.java (at line 6)
				doSomething(returnListType(new ArrayList<List>()));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation doSomething(List) of the generic method doSomething(List<T>) of type X
			----------
			4. WARNING in X.java (at line 6)
				doSomething(returnListType(new ArrayList<List>()));
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The expression of type List needs unchecked conversion to conform to List<Object>
			----------
			5. WARNING in X.java (at line 6)
				doSomething(returnListType(new ArrayList<List>()));
				                                         ^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438246, [1.8][compiler] Java 8 static methods compilation error
public void test438246() {
		this.runNegativeTest(
		   new String[] {
			   "Foo.java",
			   """
				import java.util.List;
				public abstract class Foo<C>
				{
				  @SuppressWarnings("unchecked")
				  public static <C> void doit( List<Foo<C>> workers )
				  {
				    doit(  workers.toArray( new Foo[workers.size()] ) );
				  }
				  public static <C> void doit( Foo<C>... workers )
				  {
				  }
				}
				""",
		   },
		   this.complianceLevel < ClassFileConstants.JDK1_7 ?
		   "" :
		   """
			----------
			1. WARNING in Foo.java (at line 9)
				public static <C> void doit( Foo<C>... workers )
				                                       ^^^^^^^
			Type safety: Potential heap pollution via varargs parameter workers
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795() {
		this.runNegativeTest(
		   new String[] {
			   "X.java",
			   "public class X<T> {\n" +
			   "	static <T> T element(T [] ta) {\n" +
			   "		return ta[0];\n" +
			   "	}\n" +
			   "	public static void main(String[] args) {\n" +
			   "		int x = element(new int [] { 1234 });\n" +  // check that autoboxing does not kick in for arrays, i.e engine should not slip into loose mode.
			   "	}\n" +
			   "}\n",
		   },
		   """
			----------
			1. ERROR in X.java (at line 6)
				int x = element(new int [] { 1234 });
				        ^^^^^^^
			The method element(T[]) in the type X<T> is not applicable for the arguments (int[])
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795a() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   """
				public class X<T> {
					static <T> T element(int x, T t) {
						System.out.println("Strict");
						return t;
					}
					static <T> T element(T t1, T t2) {
						System.out.println("Loose");
						return t2;
					}
					public static void main(String[] args) {
						int x = element(10, new Integer(20));
					}
				}
				""",
		   },
		   "Strict");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795b() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   """
				public class X<T> {
					static int element(int x, Integer t) {
						System.out.println("non-generic");
						return t;
					}
					static <T> T element(int t1, T t2) {
						System.out.println("generic");
						return t2;
					}
					public static void main(String[] args) {
						int x = element(10, new Integer(20));
					}
				}
				""",
		   },
		   "non-generic");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448795, [1.8][compiler] Inference should discriminate between strict and loose modes
public void test448795c() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   """
				public class X<T> {
					static int element(Integer x, Integer t) {
						System.out.println("non-generic");
						return t;
					}
					static <T> T element(int t1, T t2) {
						System.out.println("generic");
						return t2;
					}
					public static void main(String[] args) {
						int x = element(10, new Integer(20));
					}
				}
				""",
		   },
		   "generic");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434118, [1.8][compiler] Compilation error on generic capture/type inference
public void test434118() {
		this.runConformTest(
		   new String[] {
			   "X.java",
			   """
				public class X {
				    public Object convertFails(Class<?> clazz, String value) {
				        return Enum.valueOf(clazz.asSubclass(Enum.class), value);
				    }
				    public Object convertCompiles(Class<?> clazz, String value) {
				        return Enum.valueOf(clazz.<Enum>asSubclass(Enum.class), value);
				    }
				    public Object convertCompiles2(Class<?> clazz, String value) {
				        Class<? extends Enum> enumType = clazz.asSubclass(Enum.class);
				        return Enum.valueOf(enumType, value);
				    }
				}
				""",
		   },
		   "");
}
public void testBug452194() {
	runNegativeTest(
		new String[]{
			"test/Main.java",
			"""
				package test;
				
				import java.util.Map.Entry;
				
				public class Main {
					public static void main(String[] args) {
						EcoreEMap map = new EcoreEMap();
						map.addUnique(new Object()); //Error here ONLY in 4.4
					}
				}
				
				interface InternalEList<E> {
					public void addUnique(E object);
				}
				
				class EcoreEMap<K, V> implements InternalEList<Entry<K, V>> {
					public void addUnique(Entry<K, V> object) {
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in test\\Main.java (at line 7)
				EcoreEMap map = new EcoreEMap();
				^^^^^^^^^
			EcoreEMap is a raw type. References to generic type EcoreEMap<K,V> should be parameterized
			----------
			2. WARNING in test\\Main.java (at line 7)
				EcoreEMap map = new EcoreEMap();
				                    ^^^^^^^^^
			EcoreEMap is a raw type. References to generic type EcoreEMap<K,V> should be parameterized
			----------
			3. ERROR in test\\Main.java (at line 8)
				map.addUnique(new Object()); //Error here ONLY in 4.4
				    ^^^^^^^^^
			The method addUnique(Map.Entry) in the type EcoreEMap is not applicable for the arguments (Object)
			----------
			""");
}
public void testBug454644() {
	Map<String,String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runNegativeTest(
		new String[] {
			"example/CollectionFactory.java",
			"""
				/*
				 * Copyright 2002-2014 the original author or authors.
				 *
				 * Licensed under the Apache License, Version 2.0 (the "License");
				 * you may not use this file except in compliance with the License.
				 * You may obtain a copy of the License at
				 *
				 *      http://www.apache.org/licenses/LICENSE-2.0
				 *
				 * Unless required by applicable law or agreed to in writing, software
				 * distributed under the License is distributed on an "AS IS" BASIS,
				 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
				 * See the License for the specific language governing permissions and
				 * limitations under the License.
				 */
				
				package example;
				
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.EnumSet;
				import java.util.LinkedHashSet;
				import java.util.LinkedList;
				import java.util.List;
				import java.util.NavigableSet;
				import java.util.Set;
				import java.util.SortedSet;
				import java.util.TreeSet;
				
				/**
				 * The code in this class is taken directly from the Spring Framework for the purpose
				 * of reproducing a bug.
				 * <p>
				 * Specifically, the code comes from {@code org.springframework.core.CollectionFactory}.
				 *
				 * @author Juergen Hoeller
				 * @author Arjen Poutsma
				 * @author Sam Brannen
				 */
				public abstract class CollectionFactory {
				
					@SuppressWarnings({ "unchecked", "cast" })
					public static <E> Collection<E> createApproximateCollection(Object collection, int capacity) {
						if (collection instanceof LinkedList) {
							return new LinkedList<E>();
						}
						else if (collection instanceof List) {
							return new ArrayList<E>(capacity);
						}
						else if (collection instanceof EnumSet) {
							// superfluous cast necessary for bug in Eclipse 4.4.1.
							// return (Collection<E>) EnumSet.copyOf((EnumSet) collection);
				
							// Original code which compiles using OpenJDK 1.8.0_40-ea-b11 and IntelliJ IDEA
							return EnumSet.copyOf((EnumSet) collection);
						}
						else if (collection instanceof SortedSet) {
							return new TreeSet<E>(((SortedSet<E>) collection).comparator());
						}
						else {
							return new LinkedHashSet<E>(capacity);
						}
					}
				
					public static <E> Collection<E> createCollection(Class<?> collectionClass, Class<?> elementType, int capacity) {
						if (collectionClass.isInterface()) {
							if (Set.class.equals(collectionClass) || Collection.class.equals(collectionClass)) {
								return new LinkedHashSet<E>(capacity);
							}
							else if (List.class.equals(collectionClass)) {
								return new ArrayList<E>(capacity);
							}
							else if (SortedSet.class.equals(collectionClass) || NavigableSet.class.equals(collectionClass)) {
								return new TreeSet<E>();
							}
							else {
								throw new IllegalArgumentException("Unsupported Collection interface: " + collectionClass.getName());
							}
						}
						else if (EnumSet.class.equals(collectionClass)) {
							// Assert.notNull(elementType, "Cannot create EnumSet for unknown element type");
				
							// superfluous cast necessary for bug in Eclipse 4.4.1.
							// return (Collection<E>) EnumSet.noneOf((Class) elementType);
				
							// Original code which compiles using OpenJDK 1.8.0_40-ea-b11 and IntelliJ IDEA
							return EnumSet.noneOf((Class) elementType);
						}
						else {
							if (!Collection.class.isAssignableFrom(collectionClass)) {
								throw new IllegalArgumentException("Unsupported Collection type: " + collectionClass.getName());
							}
							try {
								return (Collection<E>) collectionClass.newInstance();
							}
							catch (Exception ex) {
								throw new IllegalArgumentException(
										"Could not instantiate Collection type: " + collectionClass.getName(), ex);
							}
						}
					}
				
				}
				"""
		},
		"----------\n" +
		"1. WARNING in example\\CollectionFactory.java (at line 42)\n" +
		"	@SuppressWarnings({ \"unchecked\", \"cast\" })\n" +
		"	                                 ^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"cast\")\n" +
		"----------\n" +
		"2. WARNING in example\\CollectionFactory.java (at line 55)\n" +
		"	return EnumSet.copyOf((EnumSet) collection);\n" +
		"	                       ^^^^^^^\n" +
		"EnumSet is a raw type. References to generic type EnumSet<E> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation noneOf(Class) of the generic method noneOf(Class<E>) of type EnumSet\n" +
		"----------\n" +
		"4. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type EnumSet needs unchecked conversion to conform to Collection<E>\n" +
		"----------\n" +
		"5. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	                      ^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Type safety: The expression of type Class needs unchecked conversion to conform to Class<E>\n"
		: "Type safety: The expression of type Class needs unchecked conversion to conform to Class<Enum<Enum<E>>>\n") +
		"----------\n" +
		"6. WARNING in example\\CollectionFactory.java (at line 87)\n" +
		"	return EnumSet.noneOf((Class) elementType);\n" +
		"	                       ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"7. WARNING in example\\CollectionFactory.java (at line 94)\n" +
		"	return (Collection<E>) collectionClass.newInstance();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked cast from capture#13-of ? to Collection<E>\n" +
		"----------\n",
		null, true, options);
}
// original test case, documenting existing compiler behavior
public void testBug456459a() {
	runNegativeTest(
		new String[] {
			"EnumTest.java",
			"""
				import java.util.EnumSet;
				public class EnumTest {
				\t
						static enum Cloneables implements Cloneable {
							One, Two, Three;
						}
				\t
						public <T extends Cloneable> T getOne(Class enumType) {
							EnumSet<? extends T> set = EnumSet.allOf(enumType);
							return set.iterator().next();
						}
				}
				"""
		},
		"""
			----------
			1. WARNING in EnumTest.java (at line 8)
				public <T extends Cloneable> T getOne(Class enumType) {
				                                      ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			2. ERROR in EnumTest.java (at line 9)
				EnumSet<? extends T> set = EnumSet.allOf(enumType);
				        ^^^^^^^^^^^
			Bound mismatch: The type ? extends T is not a valid substitute for the bounded parameter <E extends Enum<E>> of the type EnumSet<E>
			----------
			3. WARNING in EnumTest.java (at line 9)
				EnumSet<? extends T> set = EnumSet.allOf(enumType);
				                           ^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation allOf(Class) of the generic method allOf(Class<E>) of type EnumSet
			----------
			4. WARNING in EnumTest.java (at line 9)
				EnumSet<? extends T> set = EnumSet.allOf(enumType);
				                           ^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The expression of type EnumSet needs unchecked conversion to conform to EnumSet<? extends T>
			----------
			5. WARNING in EnumTest.java (at line 9)
				EnumSet<? extends T> set = EnumSet.allOf(enumType);
				                                         ^^^^^^^^
			Type safety: The expression of type Class needs unchecked conversion to conform to Class<Enum<Enum<E>>>
			----------
			""");
}
// simple conflict introduced by additional wildcard bound
public void testBug456459b() {
	runNegativeTest(
		new String[] {
			"EnumTest.java",
			"""
				import java.util.EnumSet;
				public class EnumTest {
				\t
						static enum Cloneables implements Cloneable {
							One, Two, Three;
						}
				\t
						public void getOne(Class enumType) {
							EnumSet<? extends EnumTest> set = null;
						}
				}
				"""
		},
		"""
			----------
			1. WARNING in EnumTest.java (at line 8)
				public void getOne(Class enumType) {
				                   ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			2. ERROR in EnumTest.java (at line 9)
				EnumSet<? extends EnumTest> set = null;
				        ^^^^^^^^^^^^^^^^^^
			Bound mismatch: The type ? extends EnumTest is not a valid substitute for the bounded parameter <E extends Enum<E>> of the type EnumSet<E>
			----------
			""");
}
// indirect conflict via wildcard's bound's bound.
public void testBug456459c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A {};
				class B {};
				public class X<T extends A> {
					<U extends B> void m() {
						X<? extends U> l = null;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				X<? extends U> l = null;
				  ^^^^^^^^^^^
			Bound mismatch: The type ? extends U is not a valid substitute for the bounded parameter <T extends A> of the type X<T>
			----------
			""");
}
public void testBug456924() {
	runConformTest(
		new String[] {
			"Test1.java",
			"""
				class Test1<E> {
					<T extends Test1<T>> void method1(Class<T> t) {
						Test2<? super T> test2 = getTest2(t);
				                // getTest2(t); // --> no error
					}
					<T extends Test1<T>> Test2<? super T> getTest2(Class<T> t){
						return null;
					}
				\t
				}
				
				""",
			"Test2.java",
			"class Test2<E extends Test1<E>>{}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425203, [compiler][1.7][inference] ECJ rejects valid code using bounded wildcards
public void test425203() {
	String source =
			"""
		import java.util.Arrays;
		import java.util.List;
		interface MyFunction<Input, Output> {
			Output apply(Input input);
		}
		public class Test {
			public <Input, Output> List<Output> wrap(MyFunction<? super Input, ? extends Output> function, Input input) {
				return Arrays.asList(function.apply(input));
			}
			public static void main(String[] args) {
				System.out.println("Done");
			}
		}""";
	if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				source,
			},
			"""
				----------
				1. WARNING in Test.java (at line 8)
					return Arrays.asList(function.apply(input));
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: A generic array of capture#2-of ? extends Output is created for a varargs parameter
				----------
				2. ERROR in Test.java (at line 8)
					return Arrays.asList(function.apply(input));
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from List<capture#2-of ? extends Output> to List<Output>
				----------
				""");
	} else {
		runConformTest(new String[]{ "Test.java", source }, "Done");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=489636, [1.8] "Bound mismatch" for a generic method parameter
public void test489636() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				interface I<T> {
					I<T> get();
				}
				public class Test {
				
					@SuppressWarnings("unused")
					private static <T, S extends I<T>> void test(S p) {
						I<T> i = p.get();
						test(i);      // Bound mismatch
					}
				}"""
	});
}

public void testBug498057() {
	runConformTest(
		new String[] {
			"scanner/AbstractScanner.java",
			"package scanner;\n" +
			"import scanner.AbstractScanner.ScannerModel;\n" +
			"public abstract class AbstractScanner<E, M extends ScannerModel<E>> implements Scanner<E> {\n" +
			"	public void scan(ScanListener<E> listener) {\n" +
			"	}\n" +
			"\n" +
			"	public static interface ScannerModel<E> extends ScanListener<E> {\n" +
			"	}\n" +
			"}\n" +
			"",
			"scanner/AbstractSubScanner.java",
			"package scanner;\n" +
			"import scanner.AbstractScanner.ScannerModel;\n" +
			"public abstract class AbstractSubScanner<E> extends AbstractScanner<E, ScannerModel<E>> {\n" +
			"	@Override\n" +
			"	public void scan(ScanListener<E> listener) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"",
			"scanner/ScanListener.java",
			"package scanner;\n" +
			"public interface ScanListener<E> {\n" +
			"}\n" +
			"\n" +
			"",
			"scanner/Scanner.java",
			"package scanner;\n" +
			"\n" +
			"public interface Scanner<E> {\n" +
			"	void scan(ScanListener<E> listener);\n" +
			"}\n" +
			"",
			"scanner/StringScanner.java",
			"package scanner;\n" +
			"\n" +
			"public interface StringScanner extends Scanner<String> {\n" +
			"}\n" +
			"",
		}
	);
	runConformTest(
		false,
		new String[] {
			"scanner/ModifyMe.java",
			"package scanner;\n" +
			"\n" +
			"public class ModifyMe extends AbstractSubScanner<String> implements StringScanner {\n" +
			"}\n" +
			"",
		},
		"",
		"",
		"",
		null
	);
}
public void testBug460491_comment23() {
	runConformTest(
		new String[] {
			"PM.java",
			"""
				public class PM<E extends Enum<E>> {
					public PM(Class<E> clazz) {
					}
				
					enum MyEnum {
					}
				
					public static void main(String[] args) {
						new PM<MyEnum>(MyEnum.class);
					}
				}
				"""
		});
}
public void testBug498486() {
	runConformTest(
			new String[] {
				"t/Ab.java",
				"package t;\n" +
				"public interface Ab<T, O extends Ob<? extends Ob.Id, ?>> {}\n" +
				"",
				"t/At.java",
				"package t;\n" +
				"public interface At<I extends Ob.Id & Comparable<I>, O extends Ob<I, O>> {\n" +
				"}\n" +
				"",
				"t/Ob.java",
				"package t;\n" +
				"public interface Ob<I extends Ob.Id & Comparable<I>, O extends Ob<I, O>> extends At<I, O> {\n" +
				"  interface Id {}\n" +
				"}\n" +
				"",
			}
		);
	runConformTest(
			false,
			new String[] {
				"i/Test.java",
				"package i;\n" +
				"\n" +
				"import t.Ab;\n" +
				"import t.Ob;\n" +
				"\n" +
				"\n" +
				"public class Test {\n" +
				"	<T, I extends Ob.Id & Comparable<I>, O extends Ob<I, O>, A extends Ab<T, O>> A // Erroneous compiler error here on the last O\n" +
				"			m() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			null,
			null,
			null,
			null
		);
}
public void testBug499048() {
	runConformTest(
		new String[] {
			"p/Outer.java",
			"""
				package p;
				public class Outer<S> {
					private static class Inner<T> {}
					Inner<S> test() {
						Outer.Inner<S> inner = new Outer.Inner<S>();
						return inner;
					}
				}
				"""
		});
}
public void testBug499126() {
	runConformTest(
		new String[] {
			"bug_ise_immutablelist/$Immutable.java",
			"package bug_ise_immutablelist;\n" +
			"\n" +
			"public class $Immutable<T> {\n" +
			"}\n" +
			"",
			"bug_ise_immutablelist/Test.java",
			"package bug_ise_immutablelist;\n" +
			"\n" +
			"public class Test {\n" +
			"	public static $Immutable<Object> f;\n" +
			"}\n" +
			"",
		}
	);
	runConformTest(
			false,
			new String[] {
				"Usage.java",
				"public class Usage {\n" +
				"	Object f() {return bug_ise_immutablelist.Test.f;}\n" +
				"}\n" +
				"",
			},
			null,
			null,
			null,
			null
	);
}
public void testBug441905() {
	runConformTest(
		new String[] {
			"EclipseJava8Generics.java",
			"""
				import java.util.List;
				
				public class EclipseJava8Generics {
				
				  public interface Foo<V> {
				  }
				
				  public static class FooBar<V, T extends Foo<V>> {
				  }
				
				  protected void doFoos(List<FooBar<?, ? extends Foo<?>>> fooBars) {
				    FooBar<?, ? extends Foo<?>> fooBar = fooBars.iterator().next();
				    doFoo(fooBar);
				  }
				
				  protected static <F> void doFoo(FooBar<F, ? extends Foo<F>> fooBar) {
				  }
				
				}
				"""
		});
}
public void testBug469297() {
	String source = """
		    import java.util.List;
		   \s
		    public class Test {
		   \s
		        static final void a(Class<? extends List<?>> type) {
		            b(newList(type));
		        }
		   \s
		        static final <T> List<T> b(List<T> list) {
		            return list;
		        }
		   \s
		 @SuppressWarnings("deprecation")
		        static final <L extends List<?>> L newList(Class<L> type) {
		            try {
		                return type.newInstance();
		            }
		            catch (Exception e) {
		                throw new RuntimeException(e);
		            }
		        }
		    }
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTest(new String[] { "Test.java", source });
	} else {
		runNegativeTest(new String[] { "Test.java", source },
			"""
				----------
				1. ERROR in Test.java (at line 6)
					b(newList(type));
					^
				The method b(List<T>) in the type Test is not applicable for the arguments (capture#1-of ? extends List<?>)
				----------
				""");
	}
}
public void testBug508799() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
	runConformTest(
		new String[] {
			"test/A.java",
			"package test;\n" +
			"\n" +
			"public interface A<T extends Iterable> {\n" +
			"    void a();\n" +
			"}\n" +
			"",
			"test/B1.java",
			"package test;\n" +
			"\n" +
			"public interface B1 extends A<Iterable> {\n" +
			"    void b1();\n" +
			"}\n" +
			"",
			"test/B2.java",
			"package test;\n" +
			"\n" +
			"public interface B2 extends A<Iterable> {\n" +
			"    void b2();\n" +
			"}\n" +
			"",
		},
		customOptions
	);
	runConformTest(false,
		new String[] {
			"test/C.java",
			"package test;\n" +
			"\n" +
			"public class C implements B1, B2 {\n" +
			"    public void a() {\n" +
			"    }\n" +
			"\n" +
			"    public void b1() {\n" +
			"    }\n" +
			"\n" +
			"    public void b2() {\n" +
			"    }\n" +
			"}\n" +
			"",
		}, null, customOptions, "", "", "", null
	);
}
public void testBug515614() {
	runConformTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"abstract class Generic<E> {\n" +
			"	interface NestedInterface {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"abstract class X<V> {\n" +
			"	public static <W> X<W> create(Class<W> vclass) {\n" +
			"		return vclass == null ? null : null;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	X<Generic.NestedInterface[]> x = X.create(Generic.NestedInterface[].class);\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug518157A() {
	runConformTest(
		new String[] {
			"RawClassParameterizationBug.java",
			"class RawClassParameterizationBug<Oops> {\n" +
			"\n" +
			"    public interface Example<K,V> {\n" +
			"    }\n" +
			"    \n" +
			"    public static class DefaultExample<K,V> implements Example<K,V> {\n" +
			"    }\n" +
			"    @SuppressWarnings(\"rawtypes\")\n" +
			"    static final Class<? extends Example> fails = DefaultExample.class;\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug518157B() {
	runConformTest(
		new String[] {
			"AlternateRawClassParameterizationBug.java",
			"import java.util.Map;\n" +
			"\n" +
			"class AlternateRawClassParameterizationBug {\n" +
			"\n" +
			"    abstract static class MapEntry<K,V> implements Map.Entry<K, V> {\n" +
			"    }\n" +
			"\n" +
			"    @SuppressWarnings(\"rawtypes\")\n" +
			"    static final Class<? extends Map.Entry> mapFails = MapEntry.class;\n" +
			"\n" +
			"}\n" +
			"",
		}
	);
}
public void testBug521212() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y<U extends Z> {}
				class Z {}
				public class X<T> {
				    public static <V> Y<? extends V> one() {
				        return null;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				public static <V> Y<? extends V> one() {
				                    ^^^^^^^^^^^
			Bound mismatch: The type ? extends V is not a valid substitute for the bounded parameter <U extends Z> of the type Y<U>
			----------
			""");
}
public void testBug526423() {
	runConformTest(
		new String[] {
			"test/compileBug/TestCompileBug.java",
			"""
				package test.compileBug;
				
				import java.util.ArrayList;
				import java.util.LinkedHashMap;
				import java.util.List;
				import java.util.Map;
				import java.util.Map.Entry;
				
				public class TestCompileBug {
				    @SuppressWarnings({ "rawtypes" })
				    private static void cannotCompile(Object token) {
				    		// change the type to List<Entry> and ArrayList<Entry> and it compiles
				    		@SuppressWarnings("unchecked")
				    		List<Map.Entry> identityServicesToTokensMap = new ArrayList<Map.Entry>(((LinkedHashMap) token).entrySet());
				    }
				}"""
		}
	);
}
public void testBug526132() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnavoidableGenericTypeProblems, CompilerOptions.DISABLED);
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				public class Test {
					private Map field = new HashMap();
					private void method() {
						field.put("key", "value");
					}
					private void method() {
						field.put("key", "value");
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in Test.java (at line 4)
				private Map field = new HashMap();
				        ^^^
			Map is a raw type. References to generic type Map<K,V> should be parameterized
			----------
			2. WARNING in Test.java (at line 4)
				private Map field = new HashMap();
				                        ^^^^^^^
			HashMap is a raw type. References to generic type HashMap<K,V> should be parameterized
			----------
			3. ERROR in Test.java (at line 5)
				private void method() {
				             ^^^^^^^^
			Duplicate method method() in type Test
			----------
			4. WARNING in Test.java (at line 5)
				private void method() {
				             ^^^^^^^^
			The method method() from the type Test is never used locally
			----------
			5. WARNING in Test.java (at line 6)
				field.put("key", "value");
				^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized
			----------
			6. ERROR in Test.java (at line 8)
				private void method() {
				             ^^^^^^^^
			Duplicate method method() in type Test
			----------
			7. WARNING in Test.java (at line 9)
				field.put("key", "value");
				^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: The method put(Object, Object) belongs to the raw type Map. References to generic type Map<K,V> should be parameterized
			----------
			""",
	null,
	true,
	customOptions);
}
public void testBug520482() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNAVOIDABLE_GENERIC_TYPE_PROBLEMS, JavaCore.DISABLED);
	runConformTest(
		new String[] {
			"test/A.java",
			"""
				package test;
				
				import java.util.List;
				
				public class A {
				    static List f;
				}
				""",
		},
		customOptions
	);
	runNegativeTest(false,
		new String[] {
			"test/B.java",
			"""
				package test;
				
				public class B extends A {
				   public static test() {
						f.add(new B());
					}
				}
				"""
		},
		null,
		customOptions,
		"""
			----------
			1. ERROR in test\\B.java (at line 4)
				public static test() {
				              ^^^^^^
			Return type for the method is missing
			----------
			""",
		"", "", null
	);
}
public void testBug532137() {
	runConformTest(
		new String[] {
			"subtypes/A.java",
			"package subtypes;\n" +
			"\n" +
			"\n" +
			"public abstract class A<R extends B> {\n" +
			"\n" +
			"	public A() {\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
			"subtypes/B.java",
			"package subtypes;\n" +
			"\n" +
			"public abstract class B  {\n" +
			"\n" +
			"	public B() {\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
			"subtypes/GenericType.java",
			"package subtypes;\n" +
			"public abstract class GenericType<REQ extends A<RES>, RES extends B> {\n" +
			"\n" +
			"}\n" +
			"",
			"subtypes/TestBase.java",
			"package subtypes;\n" +
			"\n" +
			"import java.util.Collections;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public abstract class TestBase {\n" +
			"\n" +
			"	@SuppressWarnings(\"rawtypes\")\n" +
			"	protected List<Class<? extends GenericType>> giveMeAListOfTypes() {\n" +
			"		return Collections.emptyList();\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		}
	);
	runNegativeTest(
		new String[] {
			"subtypes/TestImpl.java",
			"package subtypes;\n" +
			"\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class TestImpl extends TestBase{\n" +
			"\n" +
			"	@Override\n" +
			"    protected List<Class<? extends GenericType>> giveMeAListOfTypes() {\n" +
			"		return super.giveMeAListOfTypes();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. WARNING in subtypes\\TestImpl.java (at line 8)
				protected List<Class<? extends GenericType>> giveMeAListOfTypes() {
				                               ^^^^^^^^^^^
			GenericType is a raw type. References to generic type GenericType<REQ,RES> should be parameterized
			----------
			""",
		null,
		false
	);
}

public void testBug540313() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X/C120644mr.java",
			"""
				package X;
				
				public class C120644mr<V, X extends java.lang.Exception> extends X.C16280iv<V> {
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X\\C120644mr.java (at line 3)
					public class C120644mr<V, X extends java.lang.Exception> extends X.C16280iv<V> {
					                                                                 ^^^^^^^^^^
				X.C16280iv cannot be resolved to a type
				----------
				""";
	runner.runNegativeTest();
}

public void testBug540313a() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X/C120644mr.java",
			"""
				package X;
				
				class Outer {
					class Inner<Z> {}
				}
				public class C120644mr<V, X extends Outer> extends X.Inner<V> {
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X\\C120644mr.java (at line 6)
					public class C120644mr<V, X extends Outer> extends X.Inner<V> {
					                                                   ^^^^^^^
				X.Inner cannot be resolved to a type
				----------
				""";
	runner.runNegativeTest();
}

public void testBug540313b() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X/C120644mr.java",
			"package X;\n" +
			"\n" +
			"class Outer {\n" +
			"	class Inner<Z> {}\n" +
			"}\n" +
			"public class C120644mr<X extends Outer, V> {\n" +
			"	X.Inner<V> inner;\n" + // is this backed by JLS?
			"}\n"
		};
	runner.runConformTest();
}
public void testBug478708() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"bug/IInterface.java",
			"""
				package bug;
				
				public class IInterface {
				
				}
				""",
			"bug/AbstractA.java",
			"""
				package bug;
				
				public abstract class AbstractA<T extends IInterface> {
				
					public abstract class AbstractD<U> {
				
					}
				}
				""",
			"bug/AbstractC.java",
			"""
				package bug;
				
				
				public abstract class AbstractC<T extends IInterface> extends T.AbstractD<E>  {
				
					public AbstractC(AbstractA<T> a) {
						a.super();
					}
				}
				""",
			"bug/E.java",
			"""
				package bug;
				
				public class E {
				
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in bug\\AbstractC.java (at line 4)
					public abstract class AbstractC<T extends IInterface> extends T.AbstractD<E>  {
					                                                              ^^^^^^^^^^^
				T.AbstractD cannot be resolved to a type
				----------
				""";
	runner.runNegativeTest();
}
public void testBug543526() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"C.java",
			"""
				class C {
					<T extends CharSequence & java.util.List<T>> boolean m(T x) {
						return x instanceof String;
					}
				}
				"""
	};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in C.java (at line 3)
					return x instanceof String;
					       ^^^^^^^^^^^^^^^^^^^
				Incompatible conditional operand types T and String
				----------
				""";
	runner.runNegativeTest();
}
public void testBug543526b() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"C.java",
			"class C {\n" +
			"	<T extends CharSequence & java.util.List<T>> boolean m(T x) {\n" +
			"		return x instanceof Comparable<?>;\n" + // no problem casting to an interface
			"	}\n" +
			"}\n"
	};
	runner.runConformTest();
}
public void testBug552388() {
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class A<T extends B> {
				    protected List<C> list = new ArrayList<C>();
				
				    class C {}
				
				    public void createIO() {
				        A<? extends B> x = null;
				        List<A<? extends B>.C> y = x.list;
				    }
				}
				
				class B {
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 11)
				List<A<? extends B>.C> y = x.list;
				                           ^^^^^^
			Type mismatch: cannot convert from List<A<capture#1-of ? extends B>.C> to List<A<? extends B>.C>
			----------
			""");
}
public void testBug552388b() {
	String output = this.complianceLevel > ClassFileConstants.JDK1_6 ?
			"""
				----------
				1. ERROR in A.java (at line 17)
					foo(l);
					^^^
				The method foo(List<A<?>.C>) in the type A<T> is not applicable for the arguments (List<A<T>.C>)
				----------
				2. ERROR in A.java (at line 33)
					foo2(l);\s
					^^^^
				The method foo2(List<A<?>>) in the type A<T> is not applicable for the arguments (List<A<T>>)
				----------
				"""
			:
			"""
				----------
				1. ERROR in A.java (at line 16)
					List<C> l = new ArrayList<>();
					                ^^^^^^^^^
				'<>' operator is not allowed for source level below 1.7
				----------
				2. ERROR in A.java (at line 17)
					foo(l);
					^^^
				The method foo(List<A<?>.C>) in the type A<T> is not applicable for the arguments (List<A<T>.C>)
				----------
				3. ERROR in A.java (at line 32)
					List<A<T>> l = new ArrayList<>();
					                   ^^^^^^^^^
				'<>' operator is not allowed for source level below 1.7
				----------
				4. ERROR in A.java (at line 33)
					foo2(l);\s
					^^^^
				The method foo2(List<A<?>>) in the type A<T> is not applicable for the arguments (List<A<T>>)
				----------
				""";
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				import java.util.*;
				class A<T> {
				    class C {
				        T f;
				    }
				    C c = new C();
				    A(T t) {
				        c = new C();
				        c.f = t;
				    }
				    void foo(List<A<?>.C> list) {
				        list.add(new A<String>("").c);
				        list.add(new A<Number>(3).c);
				    }
				    T test() {
				        List<C> l = new ArrayList<>();
				        foo(l);
				        return l.get(0).f;
				    }
				    public static void main(String... args) {
				//         Number n = new A<Number>(1).test();
				//         System.out.print(n.intValue());
				        Number n = new A<Number>(1).test2();
				        System.out.print(n.intValue());
				    }
				    \s
				    void foo2(List<A<?>> list) {
				        list.add(new A<String>(""));
				        list.add(new A<Number>(3));
				    }
				    T test2() {
				        List<A<T>> l = new ArrayList<>();
				        foo2(l);\s
				        return l.get(0).c.f;
				    }
				\s
				}
				"""
		},
		output);
}
public void testBug561544() {
	if (this.complianceLevel < ClassFileConstants.JDK11)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNAVOIDABLE_GENERIC_TYPE_PROBLEMS, JavaCore.DISABLED);
	runNegativeTest(false,
		new String[] {
			"com/bsbportal/music/C2193c.java",
			"""
				package com.bsbportal.music;
				
				public class C2193c {
				    java.util.List f6088b;
				    public void onResponse(p594x.C14183r<java.lang.String> rVar) {
				        mo12821b((java.util.List<java.lang.String>) this.f6088b);
				    }
				}
				"""
		},
		null,
		customOptions,
		"""
			----------
			1. WARNING in com\\bsbportal\\music\\C2193c.java (at line 4)
				java.util.List f6088b;
				^^^^^^^^^^^^^^
			List is a raw type. References to generic type List<E> should be parameterized
			----------
			2. ERROR in com\\bsbportal\\music\\C2193c.java (at line 5)
				public void onResponse(p594x.C14183r<java.lang.String> rVar) {
				                       ^^^^^
			p594x cannot be resolved to a type
			----------
			3. ERROR in com\\bsbportal\\music\\C2193c.java (at line 6)
				mo12821b((java.util.List<java.lang.String>) this.f6088b);
				^^^^^^^^
			The method mo12821b(List<String>) is undefined for the type C2193c
			----------
			4. WARNING in com\\bsbportal\\music\\C2193c.java (at line 6)
				mo12821b((java.util.List<java.lang.String>) this.f6088b);
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from List to List<String>
			----------
			""",
		"", "", null
	);
}

public void testBug576524() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		this.runConformTest(
			new String[] {
				"Singleton.java",
				"""
					public class Singleton<T> {
					    public static void main(String[] obj) {
					        test(new Pair<Integer,String>());
					    }
					    public static <T> void test(Singleton<T> singleton) {
					        if (singleton instanceof Pair<T,?> pair) {
					            System.out.println("SUCCESS");
					        }
					    }
					}""",
				"Pair.java",
				"public class Pair<T,U> extends Singleton<T> {\n" +
				"}"
			},
			"SUCCESS"
		);
	}
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/472
// If unchecked conversion was necessary for the arguments,
// substitute and erase the return type.
public void testBugGH472_a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ReturnTypeTest.java",
				"""
					public class ReturnTypeTest {
					    <T> T m(Class<T> arg1, Class<T> arg2) { return null; }
					
					    void test(Class c1, Class<Class<String>> c2) throws Exception {
					        m(c1, c2).newInstance();
					    }
					}"""
			}
		);
	}
}

// A variation for the unchecked conversion test case.
// the type arguments contain wildcards like <? extends T>.
public void testBugGH472_b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ReturnTypeTest.java",
				"""
					public class ReturnTypeTest {
					    <T> T m(Class<T> arg1, Class<? extends T> arg2) { return null; }
					
					    void test(Class c1, Class<Class<String>> c2) throws Exception {
					        m(c1, c2).newInstance();
					    }
					}"""
			}
		);
	}
}

// A variation for the unchecked conversion test case.
// the type arguments contain wildcards like <? super T>.
public void testBugGH472_c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ReturnTypeTest.java",
				"""
					public class ReturnTypeTest {
					    <T> T m(Class<T> arg1, Class<? super T> arg2) { return null; }
					
					    void test(Class c1, Class<Class<String>> c2) throws Exception {
					        m(c1, c2).newInstance();
					    }
					}"""
			}
		);
	}
}

// If unchecked conversion was necessary for the arguments,
// substitute and erase the thrown type.
public void testBugGH472_d() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(
			new String[] {
				"ThrowTest.java",
				"""
					public class ThrowTest {
					
					    public static void test(MyDerivedException e, MyType t) {
					        try {
					            new Foo(e, t);
					        } catch (MyDerivedException e2) {}
					    }
					}
					
					class MyException extends Exception {}
					class MyDerivedException extends MyException {}
					
					class MyType<T> {}
					
					class Foo {
					    public <E1 extends MyException> Foo(E1 e, MyType<String> a) throws E1 {
					        throw e;
					    }
					}"""
			}
		);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=540063
// VerifyError with nested static templated instance
public void test540063() {
	this.runConformTest(
		new String[] {
			"SomeClass.java",
			"""
				public class SomeClass {
				  public static void main(String[] args) {
				    String s = Namespace.t.contained.s;
				    System.out.println(s);
				  }
				
				  static class Namespace {
				    static Templated<StringHolder> t = new Templated<StringHolder>(new StringHolder());
				  }
				
				  static class Templated<T> {
				    T contained;
				
				    Templated(T contained) {
				      this.contained = contained;
				    }
				  }
				
				  static class StringHolder {
				    String s = "some string";
				  }
				}
				"""
			},
		"some string"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=540063
// VerifyError with nested static templated instance
public void test540063_2() {
	this.runConformTest(
		new String[] {
			"SomeClass.java",
			"""
				public class SomeClass {
				  public static void main(String[] args) {
				    String s = t.contained.s;
				    System.out.println(s);
				  }
				
				  static Templated<StringHolder> t = new Templated<StringHolder>(new StringHolder());
				
				  static class Templated<T> {
				    T contained;
				
				    Templated(T contained) {
				      this.contained = contained;
				    }
				  }
				
				  static class StringHolder {
				    String s = "some string";
				  }
				}
				"""
		},
		"some string"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=540063
// VerifyError with nested static templated instance
public void test540063_3() {
	this.runConformTest(
		new String[] {
			"SomeClass.java",
			"""
				public class SomeClass {
				  public static void main(String[] args) {
				    String s = Namespace_O.Namespace_M.Namespace_I.t.contained.s;
				    System.out.println(s);
				  }
				
				 static class Namespace_O {
					static class Namespace_M {
						static class Namespace_I {
							static Templated<StringHolder> t = new Templated<StringHolder>(new StringHolder());
						}
					}
				  }
				
				  static class Templated<T> {
				    T contained;
				
				    Templated(T contained) {
				      this.contained = contained;
				    }
				  }
				
				  static class StringHolder {
				    String s = "some string";
				  }
				}
				"""
		},
		"some string"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=570022
// QualifiedNameReference.setGenericCast(...) throws ArrayIndexOutOfBoundsException
public void test570022() {
	this.runNegativeTest(
			new String[] {
					"p000/aazh.java",
					"""
						package p000;
						
						public class aazh {
						    public static p000.abyz<p000.aazh.EnumC0166a> f2789a = p000.abyz.m1243c(p000.aazh.EnumC0166a.DISABLED);
						
						    public enum EnumC0166a {
						        DISABLED;
						       \s
						        public boolean f2796f;
						    }
						}
						""",
					"p000/abam.java",
					"""
						package p000;
						
						public class abam {
						    public Object mo369h() {
						        return p000.aazh.f2789a.f4668a.f2796f;
						    }
						}
						""",
					"p000/abyz.java",
					"""
						package p000;
						
						public class abyz<T> {
						    public volatile T f4668a;
						}
						""",
			},
			"""
				----------
				1. ERROR in p000\\aazh.java (at line 4)
					public static p000.abyz<p000.aazh.EnumC0166a> f2789a = p000.abyz.m1243c(p000.aazh.EnumC0166a.DISABLED);
					                                                                 ^^^^^^
				The method m1243c(aazh.EnumC0166a) is undefined for the type abyz
				----------
				""");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/656
//Compilation error with multiple bounds and package protected abstract method
public void testBugGH656() {
		this.runConformTest(
			new String[] {
				"package1/MyGenericClass.java",
				"""
					package package1;
					import package2.MyAbstract;
					import package2.MyInterface;
					public class MyGenericClass<T extends MyAbstract & MyInterface> { // removing MyInterface works
					}
					""",
				"package2/MyInterface.java",
				"""
					package package2;
					public interface MyInterface {
					     void myMethod();
					}
					""",
				"package2/MyAbstract.java",
				"""
					package package2;
					public abstract class MyAbstract {
						/* package protected! */ abstract void someAbstractMethod();
					}
					"""
			}
		);
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/656
//Compilation error with multiple bounds and package protected abstract method
public void testBugGH656_2() {
		this.runNegativeTest(
			new String[] {
				"package1/MyGenericClass.java",
				"""
					package package1;
					import package2.MyAbstract;
					import package2.MyInterface;
					public class MyGenericClass<T extends MyAbstract & MyInterface> { // removing MyInterface works
					}
					class JavacWillAlsoError extends MyAbstract implements MyInterface {
					
						public void myMethod() {
						}
					
						public void someAbstractMethod() {
						}
					}
					""",
				"package2/MyInterface.java",
				"""
					package package2;
					public interface MyInterface {
					     void myMethod();
					}
					""",
				"package2/MyAbstract.java",
				"""
					package package2;
					public abstract class MyAbstract {
						/* package protected! */ abstract void someAbstractMethod();
					}
					"""
			},
			"""
				----------
				1. ERROR in package1\\MyGenericClass.java (at line 6)
					class JavacWillAlsoError extends MyAbstract implements MyInterface {
					      ^^^^^^^^^^^^^^^^^^
				This class must implement the inherited abstract method MyAbstract.someAbstractMethod(), but cannot override it since it is not visible from JavacWillAlsoError. Either make the type abstract or make the inherited method visible
				----------
				2. WARNING in package1\\MyGenericClass.java (at line 11)
					public void someAbstractMethod() {
					            ^^^^^^^^^^^^^^^^^^^^
				The method JavacWillAlsoError.someAbstractMethod() does not override the inherited method from MyAbstract since it is private to a different package
				----------
				""");
}
}

